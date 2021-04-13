package dev.atsushieno.kmmk
import androidx.compose.desktop.Window
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess

fun main() = Window {
    model.midiDeviceManager.midiAccess = if (File("/dev/snd/seq").exists()) AlsaMidiAccess() else JvmMidiAccess()
    App()
}
