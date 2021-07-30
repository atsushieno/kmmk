package dev.atsushieno.kmmk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import dev.atsushieno.ktmidi.AndroidMidiAccess
import com.arkivanov.decompose.extensions.compose.jetpack.rememberRootComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kmmk = KmmkComponentContext(rememberRootComponent())
        kmmk.midiDeviceManager.midiAccess = AndroidMidiAccess(applicationContext)
        setContent {
            App(kmmk)
        }
    }
}