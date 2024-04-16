package dev.atsushieno.kmmk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.atsushieno.ktmidi.AndroidMidiAccess
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val kmmk = KmmkComponentContext()
        kmmk.midiDeviceManager.midiAccess = AndroidMidiAccess(applicationContext)
        setContent {
            App(kmmk)
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            private var lastBackPressed = System.currentTimeMillis()
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - lastBackPressed < 2000) {
                    finish()
                    exitProcess(0)
                }
                else
                    Toast.makeText(this@MainActivity, "Tap once more to quit", Toast.LENGTH_SHORT).show()
                lastBackPressed = System.currentTimeMillis()
            }
        })
    }
}
