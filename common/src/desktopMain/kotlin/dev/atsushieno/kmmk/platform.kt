package dev.atsushieno.kmmk

import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess

actual fun getPlatformName(): String {
    return "Desktop"
}

actual val midiAccess : MidiAccess = AlsaMidiAccess()
