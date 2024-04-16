import androidx.compose.ui.window.ComposeUIViewController
import dev.atsushieno.kmmk.App
import dev.atsushieno.kmmk.KmmkComponentContext
import dev.atsushieno.ktmidi.UmpCoreMidiAccess

fun MainViewController() = ComposeUIViewController {
    val kmmk = KmmkComponentContext()
    kmmk.midiDeviceManager.midiAccess = UmpCoreMidiAccess()
    App(kmmk)
}
