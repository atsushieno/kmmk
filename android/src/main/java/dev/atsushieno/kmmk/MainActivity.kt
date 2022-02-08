package dev.atsushieno.kmmk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.DefaultComponentContext
import dev.atsushieno.ktmidi.AndroidMidiAccess
import com.arkivanov.decompose.extensions.compose.jetpack.rememberRootComponent
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val kmmk = KmmkComponentContext()
            kmmk.midiDeviceManager.midiAccess = AndroidMidiAccess(applicationContext)
            App(kmmk)
        }
    }
}