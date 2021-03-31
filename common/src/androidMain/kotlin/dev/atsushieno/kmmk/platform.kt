package dev.atsushieno.kmmk

import dev.atsushieno.ktmidi.defaultMidiAccess

actual fun getPlatformName(): String {
    return "Android"
}

actual val midiAccess = defaultMidiAccess
