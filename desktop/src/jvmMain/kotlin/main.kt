package dev.atsushieno.kmmk
import androidx.compose.desktop.Window
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent

fun main() = Window {
    val kmmk = rememberRootComponent(factory = ::KmmkComponentContext)
    kmmk.midiDeviceManager.midiAccess = if (File("/dev/snd/seq").exists()) AlsaMidiAccess() else JvmMidiAccess()
    App(kmmk)
}
