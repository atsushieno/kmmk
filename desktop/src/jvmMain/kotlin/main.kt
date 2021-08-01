package dev.atsushieno.kmmk
import androidx.compose.desktop.Window
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess

fun main(args: Array<String>) = Window {
    model.midiDeviceManager.midiAccess =
        if (File("/dev/snd/seq").exists()) AlsaMidiAccess()
        else if (args.contains("jvm")) JvmMidiAccess()
        else RtMidiAccess()
    App()
}
