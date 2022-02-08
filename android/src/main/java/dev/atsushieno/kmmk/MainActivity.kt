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

    private var lastBackPressed = System.currentTimeMillis()

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressed < 2000)
                exitProcess(0)
        else
            Toast.makeText(this, "Tap once more to quit", Toast.LENGTH_SHORT).show()
        lastBackPressed = System.currentTimeMillis()
    }
}