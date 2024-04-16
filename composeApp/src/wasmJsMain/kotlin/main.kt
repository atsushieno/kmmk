import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.atsushieno.kmmk.App
import dev.atsushieno.kmmk.KmmkComponentContext
import dev.atsushieno.ktmidi.WebMidiAccess

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val kmmk = KmmkComponentContext()
    kmmk.midiDeviceManager.midiAccess = WebMidiAccess()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App(kmmk) }
}