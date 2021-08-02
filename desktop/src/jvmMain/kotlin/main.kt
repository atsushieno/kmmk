package dev.atsushieno.kmmk
import androidx.compose.desktop.Window
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent

fun main(args: Array<String>) = Window {
    val kmmk = rememberRootComponent(factory = ::KmmkComponentContext)
    kmmk.midiDeviceManager.midiAccess =
        if (File("/dev/snd/seq").exists()) AlsaMidiAccess()
        else if (args.contains("jvm")) JvmMidiAccess()
        else RtMidiAccess()
    App(kmmk)
}
