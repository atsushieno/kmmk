package dev.atsushieno.kmmk
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent

fun main(args: Array<String>) = application {
    val windowState = rememberWindowState(size = DpSize(750.dp, 800.dp))
    Window(state = windowState, onCloseRequest = ::exitApplication, title = "Virtual MIDI Keyboard Kmmk") {
        val kmmk = rememberRootComponent(factory = ::KmmkComponentContext)
        kmmk.midiDeviceManager.midiAccess =
            if (File("/dev/snd/seq").exists()) AlsaMidiAccess()
            else if (args.contains("jvm")) JvmMidiAccess()
            else RtMidiAccess()
        App(kmmk)
    }
}
