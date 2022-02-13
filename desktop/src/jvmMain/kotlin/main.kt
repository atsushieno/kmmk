package dev.atsushieno.kmmk
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.singleWindowApplication
import java.io.File
import dev.atsushieno.ktmidi.AlsaMidiAccess
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.RtMidiAccess

fun main(args: Array<String>) = singleWindowApplication {
    val kmmk = KmmkComponentContext()
    kmmk.midiDeviceManager.midiAccess =
        if (File("/dev/snd/seq").exists()) AlsaMidiAccess()
        else if (args.contains("jvm")) JvmMidiAccess()
        else RtMidiAccess()
    App(kmmk)
}
