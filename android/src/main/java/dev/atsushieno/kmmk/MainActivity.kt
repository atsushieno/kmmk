package dev.atsushieno.kmmk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import dev.atsushieno.ktmidi.AndroidMidiAccess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        midiAccess = AndroidMidiAccess(applicationContext)
        setContent {
            App()
        }
    }
}