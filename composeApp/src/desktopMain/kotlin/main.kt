import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.atsushieno.kmmk.App
import dev.atsushieno.kmmk.KmmkComponentContext
import dev.atsushieno.ktmidi.*

fun main(args: Array<String>) = application {
    val kmmk = KmmkComponentContext()
    kmmk.midiDeviceManager.midiAccess =
        if (/*args.contains("jvm")*/true) JvmMidiAccess()
        else if (args.contains("alsa")) AlsaMidiAccess()
        else if (args.contains("rtmidi")) RtMidiAccess()
        else if (System.getProperty("os.name").contains("Windows"))
            LibreMidiAccess.create(MidiTransportProtocol.MIDI1)
        else
            LibreMidiAccess.create(MidiTransportProtocol.UMP)
    Window(onCloseRequest = ::exitApplication,
        title = "Kmmk: Virtual MIDI Keyboard",
        state = rememberWindowState(width = 640.dp, height = 780.dp)) {
        App(kmmk)
    }
}
