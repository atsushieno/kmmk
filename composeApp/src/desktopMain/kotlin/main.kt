import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.atsushieno.kmmk.App
import dev.atsushieno.kmmk.KmmkComponentContext
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess
import java.io.File

fun main(args: Array<String>) = application {
    Window(onCloseRequest = ::exitApplication,
        title = "Kmmk: Virtual MIDI Keyboard",
        state = rememberWindowState(width = 640.dp, height = 780.dp)) {
        val kmmk = KmmkComponentContext()
        kmmk.midiDeviceManager.midiAccess =
            if (File("/dev/snd/seq").exists()) AlsaMidiAccess()
            else if (args.contains("jvm")) JvmMidiAccess()
            //else if (System.getProperty("os.name").contains("Mac OS", true) &&
            //    System.getProperty("os.arch").contains("aarch64")) JvmMidiAccess()
            else RtMidiAccess()
        App(kmmk)
    }
}
