package dev.atsushieno.kmmk

import dev.atsushieno.ktmidi.MidiCIProtocolType
import dev.atsushieno.ktmidi.MidiEventType
import dev.atsushieno.ktmidi.MidiMusic
import dev.atsushieno.ktmidi.MidiPlayer
import dev.atsushieno.ktmidi.Ump
import dev.atsushieno.ktmidi.ci.MidiCIProtocolTypeInfo
import dev.atsushieno.ktmidi.ci.midiCIProtocolSet
import dev.atsushieno.ktmidi.messageType
import dev.atsushieno.ktmidi.umpfactory.umpMidi2NoteOff
import dev.atsushieno.ktmidi.umpfactory.umpMidi2NoteOn
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay

object model {
    object state {
        var midiProtocol: Int = MidiCIProtocolType.MIDI1
    }

    val midiDeviceManager = MidiDeviceManager()
    var midiProtocol : Int
        get() = state.midiProtocol
        set(value) { state.midiProtocol = value }

    val compilationDiagnostics = mutableListOf<String>()
    val musics = mutableListOf<MidiMusic>()
    val midiPlayers = mutableListOf<MidiPlayer>()

    var defaultVelocity : Byte = 100

    private fun fillBytesFromInt(dst: ByteArray, offset: Int, src: Int) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            dst[offset + 0] = (src % 0x100).toByte()
            dst[offset + 1] = (src / 0x100 % 0x100).toByte()
            dst[offset + 2] = (src / 0x10000 % 0x100).toByte()
            dst[offset + 3] = (src / 0x1000000).toByte()
        } else {
            dst[offset + 3] = (src % 0x100).toByte()
            dst[offset + 2] = (src / 0x100 % 0x100).toByte()
            dst[offset + 1] = (src / 0x10000 % 0x100).toByte()
            dst[offset + 0] = (src / 0x1000000).toByte()
        }
    }

    private fun convertUmpToBytes(ump: Ump) : ByteArray =
        ByteArray(8).apply {
            fillBytesFromInt(this, 0, ump.int1)
            when (ump.messageType) {
                3, 4 -> fillBytesFromInt(this, 4, ump.int2)
                5 -> {
                    fillBytesFromInt(this, 4, ump.int2)
                    fillBytesFromInt(this, 8, ump.int3)
                    fillBytesFromInt(this, 12, ump.int4)
                }
            }
        }

    private fun sendToAll(bytes: ByteArray, timestamp: Long) {
        midiDeviceManager.midiOutput?.send(bytes, 0, bytes.size, timestamp)
        midiDeviceManager.virtualMidiOutput?.send(bytes, 0, bytes.size, timestamp)
    }

    suspend fun playNote(key: Int) {
        if (midiProtocol == MidiCIProtocolType.MIDI2) {
            val nOn = convertUmpToBytes(Ump(umpMidi2NoteOn(0, 0, key, 0, defaultVelocity * 0x200, 0)))
            sendToAll(nOn, 0)
            delay(1000)
            val nOff = convertUmpToBytes(Ump(umpMidi2NoteOff(0, 0, key, 0, 0, 0)))
            sendToAll(nOff, 0)
        } else {
            val nOn = byteArrayOf(MidiEventType.NOTE_ON, key.toByte(), defaultVelocity)
            sendToAll(nOn, 0)
            delay(1000)
            val nOff = byteArrayOf(MidiEventType.NOTE_OFF, key.toByte(), 0)
            sendToAll(nOff, 0)
        }
    }

    fun sendProgramChange(program: Byte) {
        val bytes = byteArrayOf(MidiEventType.PROGRAM, program)
        sendToAll(bytes, 0)
    }

    fun registerMusic(music: MidiMusic) {
        val output = midiDeviceManager.midiOutput ?: return
        val player = MidiPlayer(music, output)
        midiPlayers.add(player)
        player.finished = Runnable { midiPlayers.remove(player) }
        player.play()
    }

    init {
        midiDeviceManager.midiOutputOpened = {
            if (midiProtocol == MidiCIProtocolType.MIDI2) {
                val bytes = MutableList<Byte>(19) { 0 }
                midiCIProtocolSet(bytes, 0, 0, 0,
                    MidiCIProtocolTypeInfo(0, 0, 0, 0, 0))
                bytes.add(0, 0xF0.toByte())
                bytes.add(0xF7.toByte())
                sendToAll(bytes.toByteArray(), 0)
            }
        }
    }
}