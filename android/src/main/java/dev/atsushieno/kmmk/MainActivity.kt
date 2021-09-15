package dev.atsushieno.kmmk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.atsushieno.ktmidi.AndroidMidiAccess
import com.arkivanov.decompose.extensions.compose.jetpack.rememberRootComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val kmmk = KmmkComponentContext(rememberRootComponent(::KmmkComponentContext))
            kmmk.midiDeviceManager.midiAccess = AndroidMidiAccess(applicationContext)
            App(kmmk)
        }
    }
}