package dev.atsushieno.kmmk

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.atsushieno.ktmidi.*
import dev.atsushieno.ktmidi.ci.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class MidiDeviceManager {
    private val emptyMidiAccess = EmptyMidiAccess()
    private var midiAccessValue: MidiAccess = emptyMidiAccess

    private val muid = Random.nextInt() and 0x7F7F7F7F
    private val config = MidiCIDeviceConfiguration().apply {
        deviceInfo = MidiCIDeviceInfo(1,2,3,4, "atsushieno", "Kmmk", "kmmk", "0.1")
        productInstanceId = deviceInfo.toString()
    }
    val device = MidiCIDevice(muid, config,
        sendCIOutput = { group, data ->
            val midi1Bytes = listOf(Midi1Status.SYSEX.toByte()) + data + listOf(Midi1Status.SYSEX_END.toByte())
            sendToAll(midi1Bytes.toByteArray(), 0)
                       },
        sendMidiMessageReport = { group, protocol, data ->
            // FIXME: respect group and protocol
            sendToAll(data.toByteArray(), 0)
        }).apply {
        with(profileHost) {
            profiles.add(MidiCIProfile(MidiCIProfileId(listOf(0x7E, 1, 2, 3, 4)), 0, 0x7F, true, 0))
            profiles.add(MidiCIProfile(MidiCIProfileId(listOf(0x7E, 5, 6, 7, 8)), 0, 0x7F, true, 0))
        }
    }

    var midiAccess: MidiAccess
        get() = midiAccessValue
        set(value) {
            midiAccessValue = value
            GlobalScope.launch {
                try {
                    val pcIn = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual Out Port",
                        version = "1.0",
                        //midiProtocol = MidiCIProtocolType.MIDI2, // if applicable
                        //umpGroup = 1
                    )
                    val pcOut = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual In Port",
                        version = "1.0",
                        //midiProtocol = MidiCIProtocolType.MIDI2, // if applicable
                        //umpGroup = 2
                    )
                    virtualMidiInput = midiAccessValue.createVirtualOutputReceiver(pcOut)
                    virtualMidiInput!!.setMessageReceivedListener { data, start, length, timestampInNanoseconds ->
                        if (data[start] == Midi1Status.SYSEX.toByte()) {
                            device.processInput(0, data.drop(start + 1).take(length - 2))
                            return@setMessageReceivedListener
                        }

                        val s = data.drop(start).take(length).joinToString(" ") {
                            it.toString(16)
                        }
                        sendToAll(data.drop(start).take(length).toByteArray(), timestampInNanoseconds)
                    }
                    virtualMidiOutput = midiAccessValue.createVirtualInputSender(pcIn)
                } catch (ex: Exception) {
                    println("Failed to create virtual ports (should not be critical). Details:")
                    println(ex)
                }
            }
        }

    val midiInputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.inputs
    val midiOutputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.outputs

    var midiInputDeviceId: String?
        get() = midiInput?.details?.id
        set(id) {
            GlobalScope.launch {
                val old = midiInput
                midiInput = if (id != null) midiAccessValue.openInput(id) else return@launch
                old?.close()
                midiInputOpened()
            }
        }

    var midiOutputDeviceId: String?
        get() = midiOutput?.details?.id
        set(id) {
            GlobalScope.launch {
                val old = midiOutput
                midiOutput = if (id != null) midiAccessValue.openOutput(id) else return@launch
                old?.close()
                midiOutputError.value = null
                virtualMidiOutputError.value = null
                midiOutputOpened()
            }
        }

    var midiInputOpened : () -> Unit = {}
    var midiOutputOpened : () -> Unit = {}

    var midiInput: MidiInput? = null
    var midiOutput: MidiOutput? = null
    var virtualMidiOutput: MidiOutput? = null
    var virtualMidiInput: MidiInput? = null

    var midiOutputError = mutableStateOf<Exception?>(null)
    var virtualMidiOutputError = mutableStateOf<Exception?>(null)

    fun sendToAll(bytes: ByteArray, timestamp: Long) {
        try {
            if (midiOutputError.value == null)
                midiOutput?.send(bytes, 0, bytes.size, timestamp)
        } catch (ex: Exception) {
            midiOutputError.value = ex
        }
        try {
            if (virtualMidiOutputError.value == null)
                virtualMidiOutput?.send(bytes, 0, bytes.size, timestamp)
        } catch (ex: Exception) {
            virtualMidiOutputError.value = ex
        }
    }

    init {
        midiInput = EmptyMidiAccess.input
        midiOutput = EmptyMidiAccess.output
    }
}
