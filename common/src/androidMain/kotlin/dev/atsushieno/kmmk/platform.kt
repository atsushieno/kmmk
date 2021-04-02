package dev.atsushieno.kmmk

import dev.atsushieno.ktmidi.MidiAccessManager

actual fun getPlatformName(): String {
    return "Android"
}

actual val midiAccess = MidiAccessManager.empty
