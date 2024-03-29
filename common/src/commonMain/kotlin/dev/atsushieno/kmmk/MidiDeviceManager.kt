package dev.atsushieno.kmmk

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.atsushieno.ktmidi.EmptyMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiInput
import dev.atsushieno.ktmidi.MidiOutput
import dev.atsushieno.ktmidi.MidiPortDetails
import dev.atsushieno.ktmidi.PortCreatorContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MidiDeviceManager {
    private val emptyMidiAccess = EmptyMidiAccess()
    private var emptyMidiInput: MidiInput
    private var emptyMidiOutput: MidiOutput
    private var midiAccessValue: MidiAccess = emptyMidiAccess

    var midiAccess: MidiAccess
        get() = midiAccessValue
        set(value) {
            midiAccessValue = value
            midiInput = emptyMidiInput
            midiOutput = emptyMidiOutput
            GlobalScope.launch {
                try {
                    val pc = PortCreatorContext(
                        manufacturer = "Kmmk project",
                        applicationName = "Kmmk",
                        portName = "Kmmk Virtual Port",
                        version = "1.0"
                    )
                    virtualMidiOutput = midiAccessValue.createVirtualInputSender(pc)
                } catch (ex: Exception) {
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
            runBlocking {
                midiInput?.close()
                midiInput = if (id != null) midiAccessValue.openInput(id) else emptyMidiInput
                midiInputOpened()
            }
        }

    var midiOutputDeviceId: String?
        get() = midiOutput?.details?.id
        set(id) {
            runBlocking {
                midiOutput?.close()
                midiOutput = if (id != null) midiAccessValue.openOutput(id) else emptyMidiOutput
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
        runBlocking {
            emptyMidiInput = emptyMidiAccess.openInput(emptyMidiAccess.inputs.first().id)
            emptyMidiOutput = emptyMidiAccess.openOutput(emptyMidiAccess.outputs.first().id)
        }
    }
}
