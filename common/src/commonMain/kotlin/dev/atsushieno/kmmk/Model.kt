package dev.atsushieno.kmmk

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.atsushieno.ktmidi.MidiCIProtocolType
import dev.atsushieno.ktmidi.MidiChannelStatus
import dev.atsushieno.ktmidi.MidiMusic
import dev.atsushieno.ktmidi.Midi2Music
import dev.atsushieno.ktmidi.MidiPlayer
import dev.atsushieno.ktmidi.Midi1Player
import dev.atsushieno.ktmidi.Midi2Player
import dev.atsushieno.ktmidi.Ump
import dev.atsushieno.ktmidi.UmpFactory
import dev.atsushieno.ktmidi.ci.MidiCIProtocolTypeInfo
import dev.atsushieno.ktmidi.ci.midiCIProtocolSet
import dev.atsushieno.ktmidi.toBytes
import dev.atsushieno.mugene.MmlCompiler
import dev.atsushieno.mugene.MmlException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import com.arkivanov.decompose.ComponentContext
import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Runnable

interface Kmmk {}

// FIXME: should be declared as an interface?
class KmmkComponentContext(
    componentContext: ComponentContext
) : Kmmk, ComponentContext by componentContext {
    val noteNames = arrayOf("c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b")

    var mmlText = mutableStateOf("")

    // In this application, we record the *number of* note-ons for each key, instead of an on-off state flag
    // so that it can technically send more than one note on operations on the same key.
    var noteOnStates = SnapshotStateList<Int>().also { it.addAll(List(128) { 0 }) }

    var shouldRecordMml = mutableStateOf(false)

    var useDrumChannel = mutableStateOf(false)

    var shouldOutputNoteLength = mutableStateOf(false)
    var currentTempo = mutableStateOf(120.0)

    val midiDeviceManager = MidiDeviceManager()
    var midiProtocol = mutableStateOf(MidiCIProtocolType.MIDI1)

    val compilationDiagnostics = mutableListOf<String>()
    val musics = mutableListOf<MidiMusic>()
    val midiPlayers = mutableListOf<MidiPlayer>()

    var defaultVelocity : Byte = 100
    private var currentMmlOctave = 5
    private var lastNoteOnTime = Clock.System.now()

    // FIXME: once we sort out which development model to take, take it out from "model".
    var program = mutableStateOf(0)

    private fun sendToAll(bytes: ByteArray, timestamp: Long) {
        midiDeviceManager.midiOutput?.send(bytes, 0, bytes.size, timestamp)
        midiDeviceManager.virtualMidiOutput?.send(bytes, 0, bytes.size, timestamp)
    }

    private val targetChannel: Int
        get() = if (useDrumChannel.value) 9 else 0

    @OptIn(ExperimentalTime::class)
    private fun calculateLength(msec: Double) : String {
        if (!shouldOutputNoteLength.value)
            return ""
        // 500 = full note at BPM 120. Minimum by 1/8.
        val n8th = (currentTempo.value / 120.0 * msec / (500.0 / 8)).toInt()
        val longPart = "1^".repeat(n8th / 8)
        val remaining = arrayOf("0", "8", "4", "4.", "2", "2^8", "2.", "2..")
        return longPart + remaining[n8th % 8]
    }

    @OptIn(ExperimentalTime::class)
    fun noteOn(key: Int) {
        if (key < 0 || key >= 128) // invalid operation
            return

        val existingPlayingNotes = noteOnStates.any { it > 0 }

        noteOnStates[key]++

        if (midiProtocol.value == MidiCIProtocolType.MIDI2) {
            val nOn = Ump(UmpFactory.midi2NoteOn(0, targetChannel, key, 0, defaultVelocity * 0x200, 0)).toBytes()
            sendToAll(nOn, 0)
        } else {
            val nOn = byteArrayOf((MidiChannelStatus.NOTE_ON + targetChannel).toByte(), key.toByte(), defaultVelocity)
            sendToAll(nOn, 0)
        }

        if (shouldRecordMml.value) {
            val msec = (Clock.System.now() - lastNoteOnTime).inWholeMilliseconds.toDouble()
            val lengthSpec = calculateLength(msec)

            // Output & or 0 if there is overlapped note. If it's within 100ms, then it is chord ('0'), otherwise '&'.
            if (existingPlayingNotes)
                mmlText.value += if (msec < 100) "0" else "$lengthSpec &"
            else
                mmlText.value += " "
            // put relative octave changes.
            val oct = key / 12
            for (i in 0 until oct - currentMmlOctave)
                mmlText.value += '>'
            for (i in 0 until currentMmlOctave - oct)
                mmlText.value += '<'
            mmlText.value += " ${noteNames[key % 12]}"

            currentMmlOctave = oct
            lastNoteOnTime = Clock.System.now()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun noteOff(key: Int) {
        if (key < 0 || key >= 128 || noteOnStates[key] == 0) // invalid operation
            return
        noteOnStates[key]--

        if (shouldRecordMml.value && noteOnStates.all { it == 0 }) { // chord notes already had length, so do not output length for them.
            val msec = (Clock.System.now() - lastNoteOnTime).inWholeMilliseconds.toDouble()
            val lengthSpec = calculateLength(msec)
            mmlText.value += lengthSpec
        }

        if (midiProtocol.value == MidiCIProtocolType.MIDI2) {
            val nOff = Ump(UmpFactory.midi2NoteOff(0, targetChannel, key, 0, 0, 0)).toBytes()
            sendToAll(nOff, 0)
        } else {
            val nOff = byteArrayOf((MidiChannelStatus.NOTE_OFF + targetChannel).toByte(), key.toByte(), 0)
            sendToAll(nOff, 0)
        }
    }

    fun sendProgramChange(programToChange: Int) {
        this.program.value = programToChange
        if (midiProtocol.value == MidiCIProtocolType.MIDI2) {
            val nOff = Ump(UmpFactory.midi2Program(0, targetChannel, 0, programToChange, 0, 0)).toBytes()
            sendToAll(nOff, 0)
        } else {
            val bytes = byteArrayOf((MidiChannelStatus.PROGRAM + targetChannel).toByte(), programToChange.toByte())
            sendToAll(bytes, 0)
        }
    }

    fun registerMusic1(music: MidiMusic, playOnInput: Boolean) {
        val output = (if (playOnInput) midiDeviceManager.virtualMidiOutput else midiDeviceManager.midiOutput) ?: return
        val player = Midi1Player(music, output)
        midiPlayers.add(player)
        player.finished = Runnable { midiPlayers.remove(player) }
        player.play()
    }

    fun registerMusic2(music: Midi2Music, playOnInput: Boolean) {
        val output = (if (playOnInput) midiDeviceManager.virtualMidiOutput else midiDeviceManager.midiOutput) ?: return
        val player = Midi2Player(music, output)
        midiPlayers.add(player)
        player.finished = Runnable { midiPlayers.remove(player) }
        player.play()
    }

    var octaveShift = mutableStateOf(2)
    var noteShift = mutableStateOf(0)

    class KeyTonalitySettings(val name: String, val notesPerKey: Array<Array<Int>>)

    val tonalities = arrayOf(
        KeyTonalitySettings("Diatonic", arrayOf(
            arrayOf(44, 46, Int.MIN_VALUE, 49, 51, Int.MIN_VALUE, 54, 56, 58, Int.MIN_VALUE, 61, 63, Int.MIN_VALUE, 66),
            arrayOf(45, 47, 48, 50, 52, 53, 55, 57, 59, 60, 62, 64, 65, 67),
            arrayOf(32, 34, Int.MIN_VALUE, 37, 39, Int.MIN_VALUE, 42, 44, 46, Int.MIN_VALUE, 49, 51),
            arrayOf(33, 35, 36, 38, 40, 41, 43, 45, 47, 48, 50, 52, 53)
        )), KeyTonalitySettings("Chromatic", arrayOf(
            arrayOf(44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68),
            arrayOf(45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69),
            arrayOf(32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56),
            arrayOf(33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57)
        ))
    )

    class KeyboardConfiguration(val name: String, val keys: Array<String>)

    val keyboards = arrayOf(
        KeyboardConfiguration("ASCII Qwerty", arrayOf("1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm")),
        KeyboardConfiguration("US101", arrayOf("1234567890_=", "qwertyuiop[]", "asdfghjkl;']", "zxcvbnm,./")),
        KeyboardConfiguration("JP106", arrayOf("1234567890-^\\", "qwertyuiop@[", "asdfghjkl;:]", "zxcvbnm,./"))
    )

    var selectedKeyboard = mutableStateOf(0)
    var selectedTonality = mutableStateOf(0)

    fun setKeyboard(index: Int) {
        selectedKeyboard.value = index
    }
    fun setTonality(index: Int) {
        selectedTonality.value = index
    }

    fun getNoteFromKeyCode(utf16CodePoint: Int): Int {
        val ch = utf16CodePoint.toChar()
        keyboards[selectedKeyboard.value].keys.forEachIndexed { indexOfLines, line ->
            val idx = line.indexOf(ch)
            val notesPerKey = tonalities[selectedTonality.value].notesPerKey
            if (idx >= 0 && idx < notesPerKey[indexOfLines].size)
                return notesPerKey[indexOfLines][idx] + octaveShift.value * 12 + noteShift.value
        }
        return -1
    }

    fun setOutputDevice(id: String) {
        midiDeviceManager.midiOutputDeviceId = id
    }

    fun playMml(mml: String, playOnInput: Boolean) {
        val mmlModified = "0 $mml"
        val compiler = MmlCompiler.create()
        compilationDiagnostics.clear()
        compiler.report = { verbosity, location, message -> compilationDiagnostics.add("$verbosity $location: $message") }
        try {
            if (midiProtocol.value == MidiCIProtocolType.MIDI2) {
                val music = compiler.compile2(true, false, mmlModified)
                registerMusic2(music, playOnInput)
            } else {
                val music = compiler.compile(false, mmlModified)
                registerMusic1(music, playOnInput)
            }
        } catch(ex: MmlException) {
            println(ex)
        }
    }

    fun onMidiProtocolUpdated() {
        midiProtocol.value = if (midiProtocol.value == MidiCIProtocolType.MIDI2) MidiCIProtocolType.MIDI1 else MidiCIProtocolType.MIDI2

        // Generate a MIDI CI Set New Protocol Message...
        val bytes = MutableList<Byte>(19) { 0 }
        val protocolValue: Byte = if (midiProtocol.value == MidiCIProtocolType.MIDI2) 2 else 1
        midiCIProtocolSet(bytes, 0, 0, 0,
            MidiCIProtocolTypeInfo(protocolValue, 0, 0, 0, 0))
        bytes.add(0, 0xF0.toByte())
        bytes.add(0xF7.toByte())

        // ...and send it...
        if (protocolValue * 1 == 2) {
            // ... in MIDI1 sysex
            sendToAll(bytes.toByteArray(), 0)
        } else {
            // ... in MIDI2 UMP.
            val umpInBytes = mutableListOf<Byte>()
            UmpFactory.sysex7Process(0, bytes, { p, _ ->
                umpInBytes.addAll(Ump(p).toBytes().toTypedArray())
            }, null)
            sendToAll(umpInBytes.toByteArray(), 0)
        }
        // S6.6 "After the Initiator sends this Set New Protocol message, it shall switch its
        // own Protocol while also waiting 100ms to allow the Responder to switch Protocol."
        runBlocking {
            delay(100)
        }
    }
}
