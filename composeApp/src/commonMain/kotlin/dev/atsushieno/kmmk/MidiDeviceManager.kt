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

    var midiTransportProtocol = MidiTransportProtocol.MIDI1

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
                        midiProtocol = MidiTransportProtocol.MIDI1
                    )
                    val pcOut = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual In Port",
                        version = "1.0",
                        midiProtocol = MidiTransportProtocol.MIDI1
                    )
                    virtualMidiInput = midiAccessValue.createVirtualOutputReceiver(pcOut).also {
                        it.setMessageReceivedListener { data, start, length, timestampInNanoseconds ->
                            if (data[start] == Midi1Status.SYSEX.toByte()) {
                                device.processInput(0, data.drop(start + 1).take(length - 2))
                                return@setMessageReceivedListener
                            }
                            sendToAll(data.drop(start).take(length).toByteArray(), timestampInNanoseconds)
                        }
                    }
                    virtualMidiOutput = midiAccessValue.createVirtualInputSender(pcIn)

                    val pcIn2 = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual Out UMP Port",
                        version = "1.0",
                        midiProtocol = MidiTransportProtocol.UMP,
                        umpGroup = 1
                    )
                    val pcOut2 = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual In UMP Port",
                        version = "1.0",
                        midiProtocol = MidiTransportProtocol.UMP,
                        umpGroup = 2
                    )
                    virtualMidiInput2 = midiAccessValue.createVirtualOutputReceiver(pcOut2).also {
                        it.setMessageReceivedListener { data, start, length, timestampInNanoseconds ->
                            // FIXME: implement
                            /*if (data[start] == Midi1Status.SYSEX.toByte()) {
                                device.processInput(0, data.drop(start + 1).take(length - 2))
                                return@setMessageReceivedListener
                            }*/
                            sendToAll(data.drop(start).take(length).toByteArray(), timestampInNanoseconds)
                        }
                    }
                    virtualMidiOutput2 = midiAccessValue.createVirtualInputSender(pcIn2)
                } catch (ex: Exception) {
                    println("Failed to create virtual ports (should not be critical). Details:")
                    println(ex)
                }
            }
        }

    val midiInputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.inputs
    val midiOutputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.outputs.toList().filter { it.midiTransportProtocol == midiTransportProtocol }

    suspend fun setMidiInputDeviceId(id: String?) {
        midiInput?.close()
        if (id == null)
            return
        midiInput = midiAccessValue.openInput(id)
        midiInputOpened()
    }

    suspend fun setMidiOutputDeviceId (id: String?) {
        midiOutput?.close()
        if (id == null)
            return
        midiOutput = midiAccessValue.openOutput(id)
        midiOutputError.value = null
        virtualMidiOutputError.value = null
        midiOutputOpened()
    }

    var midiInputOpened : () -> Unit = {}
    var midiOutputOpened : () -> Unit = {}

    var midiInput: MidiInput? = null
    var midiOutput: MidiOutput? = null
    var virtualMidiOutput: MidiOutput? = null
    var virtualMidiInput: MidiInput? = null
    var virtualMidiOutput2: MidiOutput? = null
    var virtualMidiInput2: MidiInput? = null

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
            if (midiTransportProtocol == MidiTransportProtocol.MIDI1 && virtualMidiOutputError.value == null)
                virtualMidiOutput?.send(bytes, 0, bytes.size, timestamp)
            //if (midiTransportProtocol == MidiTransportProtocol.UMP && virtualMidiOutputError.value == null)
            //    virtualMidiOutput2?.send(bytes, 0, bytes.size, timestamp)
        } catch (ex: Exception) {
            virtualMidiOutputError.value = ex
        }
    }

    init {
        midiInput = EmptyMidiAccess.input
        midiOutput = EmptyMidiAccess.output
    }
}
