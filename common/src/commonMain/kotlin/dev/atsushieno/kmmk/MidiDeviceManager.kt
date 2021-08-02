package dev.atsushieno.kmmk

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
    // FIXME: fix API async-ness in ktmidi
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
                midiInput = if (id != null) midiAccessValue.openInputAsync(id) else emptyMidiInput
                midiInputOpened()
            }
        }
    var midiOutputDeviceId: String?
        get() = midiOutput?.details?.id
        set(id) {
            runBlocking {
                if (midiOutput != null)
                    midiOutput!!.close()
                midiOutput = if (id != null) midiAccessValue.openOutputAsync(id) else emptyMidiOutput
                midiOutputOpened()
            }
        }

    var midiInputOpened : () -> Unit = {}
    var midiOutputOpened : () -> Unit = {}

    var midiInput: MidiInput? = null
    var midiOutput: MidiOutput? = null
    var virtualMidiOutput: MidiOutput? = null

    init {
        runBlocking {
            emptyMidiInput = emptyMidiAccess.openInputAsync(emptyMidiAccess.inputs.first().id)
            emptyMidiOutput = emptyMidiAccess.openOutputAsync(emptyMidiAccess.outputs.first().id)
        }
    }
}
