package dev.atsushieno.kmmk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.atsushieno.ktmidi.MidiCIProtocolType
import dev.atsushieno.ktmidi.MidiChannelStatus
import dev.atsushieno.ktmidi.MidiMusic
import dev.atsushieno.ktmidi.MidiPlayer
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

interface Kmmk {}

// FIXME: should be declared as an interface?
class KmmkComponentContext(
    componentContext: ComponentContext
) : Kmmk, ComponentContext by componentContext {
    val noteNames = arrayOf("c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b")

    private var savedMmlText = ""
    var mmlText
        get() = savedMmlText
        set(v) {
            savedMmlText = v
            mmlTextState.value = v
        }
    // FIXME: once we sort out which development model to take, take it out from "model".
    var mmlTextState = mutableStateOf(mmlText)

    // In this application, we record the *number of* note-ons for each key, instead of an on-off state flag
    // so that it can technically send more than one note on operations on the same key.
    var noteOnStates = SnapshotStateList<Int>().also { it.addAll(List(128) { 0 }) }

    var shouldRecordMml = false

    val midiDeviceManager = MidiDeviceManager()
    var midiProtocol = MidiCIProtocolType.MIDI1

    val compilationDiagnostics = mutableListOf<String>()
    val musics = mutableListOf<MidiMusic>()
    val midiPlayers = mutableListOf<MidiPlayer>()

    var defaultVelocity : Byte = 100

    private fun sendToAll(bytes: ByteArray, timestamp: Long) {
        midiDeviceManager.midiOutput?.send(bytes, 0, bytes.size, timestamp)
        midiDeviceManager.virtualMidiOutput?.send(bytes, 0, bytes.size, timestamp)
    }

    fun noteOn(key: Int) {
        if (key < 0 || key >= 128) // invalid operation
            return
        noteOnStates[key]++

        if (midiProtocol == MidiCIProtocolType.MIDI2) {
            val nOn = Ump(UmpFactory.midi2NoteOn(0, 0, key, 0, defaultVelocity * 0x200, 0)).toBytes()
            sendToAll(nOn, 0)
        } else {
            val nOn = byteArrayOf(MidiChannelStatus.NOTE_ON.toByte(), key.toByte(), defaultVelocity)
            sendToAll(nOn, 0)
        }

        if (shouldRecordMml)
            mmlText += " o${key / 12}${noteNames[key % 12]}"
    }
    fun noteOff(key: Int) {
        if (key < 0 || key >= 128 || noteOnStates[key] == 0) // invalid operation
            return
        noteOnStates[key]--

        if (midiProtocol == MidiCIProtocolType.MIDI2) {
            val nOff = Ump(UmpFactory.midi2NoteOff(0, 0, key, 0, 0, 0)).toBytes()
            sendToAll(nOff, 0)
        } else {
            val nOff = byteArrayOf(MidiChannelStatus.NOTE_OFF.toByte(), key.toByte(), 0)
            sendToAll(nOff, 0)
        }
    }

    fun sendProgramChange(program: Byte) {
        val bytes = byteArrayOf(MidiChannelStatus.PROGRAM.toByte(), program)
        sendToAll(bytes, 0)
    }

    fun registerMusic(music: MidiMusic) {
        val output = midiDeviceManager.midiOutput ?: return
        val player = MidiPlayer(music, output)
        midiPlayers.add(player)
        player.finished = Runnable { midiPlayers.remove(player) }
        player.play()
    }

    private var octaveShift = 2

    class KeyTonalitySettings(val name: String, val notesPerKey: Array<Array<Int>>)

    private val tonalities = arrayOf(
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

    private val keyboards = arrayOf(
        KeyboardConfiguration("ASCII Qwerty", arrayOf("1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm")),
        KeyboardConfiguration("JP106", arrayOf("1234567890-^\\", "qwertyuiop@[", "asdfghjkl;:]", "zxcvbnm,./"))
    )

    private var selectedKeyboard = mutableStateOf(0)
    private var selectedTonality = mutableStateOf(0)

    fun getNoteFromKeyCode(utf16CodePoint: Int): Int {
        val ch = utf16CodePoint.toChar()
        keyboards[selectedKeyboard.value].keys.forEachIndexed { indexOfLines, line ->
            val idx = line.indexOf(ch)
            val notesPerKey = tonalities[selectedTonality.value].notesPerKey
            if (idx >= 0 && idx < notesPerKey[indexOfLines].size)
                return notesPerKey[indexOfLines][idx] + octaveShift * 12
        }
        return -1
    }

    fun setOutputDevice(id: String) {
        midiDeviceManager.midiOutputDeviceId = id
    }

    fun playMml(mml: String) {
        val mmlModified = "0 $mml"
        val compiler = MmlCompiler.create()
        compilationDiagnostics.clear()
        compiler.report = { verbosity, location, message -> compilationDiagnostics.add("$verbosity $location: $message") }
        try {
            val music = compiler.compile(false, mmlModified)
            registerMusic(music)
        } catch(ex: MmlException) {
            println(ex)
        }
    }

    init {
        midiDeviceManager.midiOutputOpened = {
            if (midiProtocol == MidiCIProtocolType.MIDI2) {
                // MIDI CI Set New Protocol Message
                val bytes = MutableList<Byte>(19) { 0 }
                midiCIProtocolSet(bytes, 0, 0, 0,
                    MidiCIProtocolTypeInfo(2, 0, 0, 0, 0))
                bytes.add(0, 0xF0.toByte())
                bytes.add(0xF7.toByte())
                sendToAll(bytes.toByteArray(), 0)
                // S6.6 "After the Initiator sends this Set New Protocol message, it shall switch its
                // own Protocol while also waiting 100ms to allow the Responder to switch Protocol."
                runBlocking {
                    delay(100)
                }
            }
        }
    }
}
