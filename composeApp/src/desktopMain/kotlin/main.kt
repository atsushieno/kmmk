import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.atsushieno.kmmk.App
import dev.atsushieno.kmmk.KmmkComponentContext
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.LibreMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess
import java.io.File

fun main(args: Array<String>) = application {
    val kmmk = KmmkComponentContext()
    kmmk.midiDeviceManager.midiAccess =
        if (args.contains("jvm")) JvmMidiAccess()
        else if (args.contains("alsa")) AlsaMidiAccess()
        else if (args.contains("rtmidi")) RtMidiAccess()
        else LibreMidiAccess.create(1)
    Window(onCloseRequest = ::exitApplication,
        title = "Kmmk: Virtual MIDI Keyboard",
        state = rememberWindowState(width = 640.dp, height = 780.dp)) {
        App(kmmk)
    }
}
