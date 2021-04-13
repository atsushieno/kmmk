package dev.atsushieno.kmmk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiPortDetails

@Composable
fun App() {
    MaterialTheme {
        val topDrawerState = remember { DrawerState(DrawerValue.Closed) }
        val topDrawerContent : @Composable ColumnScope.() -> Unit = @Composable {
        }

        Column {
            val instrumentOnClick = {
            }
            val presetsOnClick = {
            }
            val midiInputOnClick = {
            }
            val midiOutputOnClick = {
            }
            MidiSettingsView(instrumentOnClick = instrumentOnClick, presetsOnClick = presetsOnClick, midiInputOnClick = midiInputOnClick, midiOutputOnClick = midiOutputOnClick)
            MidiKeyboard()
            Controllers()
            MmlPad()
        }
    }
}

@Composable
fun MidiSettingsView(midiInputOnClick: () -> Unit,
                     midiOutputOnClick: () -> Unit,
                     instrumentOnClick: () -> Unit,
                     presetsOnClick: () -> Unit) {
    Column {
        Row {
            val midiInputDialogState = remember { mutableStateOf(false) }
            val midiOutputDialogState = remember { mutableStateOf(false) }

            if (midiInputDialogState.value) {
                // show dialog
                Column {
                    val onClick : () -> Unit = { midiInputDialogState.value = false }
                    if (model.midiInputPorts.any())
                        for (d in model.midiInputPorts)
                            Text(modifier = Modifier.clickable(onClick = onClick), text = d.name ?: "(unnamed)")
                    else
                        Text(modifier = Modifier.clickable(onClick = onClick), text = "(no MIDI input)")
                }
            } else {
                Card(
                    modifier = Modifier.clickable(onClick = { midiInputDialogState.value = true }).padding(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                ) {
                    Text("-- Select MIDI input --")
                }
            }

            if (midiOutputDialogState.value) {
                Column {
                    val onClick = { midiOutputDialogState.value = false }
                    if (model.midiOutputPorts.any())
                        for (d in model.midiOutputPorts)
                            Text(modifier = Modifier.clickable (onClick = onClick), text = d.name ?: "(unnamed)")
                    else
                        Text(modifier = Modifier.clickable(onClick = onClick), text = "(no MIDI output)")
                }
            } else {
                Card(
                    modifier = Modifier.clickable(onClick = { midiOutputDialogState.value = true }).padding(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                ) {
                    Text("-- Select MIDI output --")
                }
            }
        }
        Row {
            Card(
                modifier = Modifier.clickable(onClick = instrumentOnClick).padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
            ) {

                Text("Acoustic Piano 1")
            }
            Card(
                modifier = Modifier.clickable(onClick = presetsOnClick).padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
            ) {
                Text("General MIDI Instruments Set")
            }
        }
    }
}

@Composable
fun Controllers() {

}

@Composable
fun MmlPad() {
    var mmlState by remember { mutableStateOf("mml") }
    val mmlOnClick = { playMml() }

    Row {
        Button(onClick = mmlOnClick, modifier = Modifier.align(Alignment.CenterVertically)) {
            Text("Run")
        }
        OutlinedTextField(
            value = mmlState,
            onValueChange = { mmlState = it },
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(100.dp),
        )
    }
}

fun playMml() {
    println("play MML clicked")
}

lateinit var midiAccess : MidiAccess

object model {

    val midiInputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.inputs
    val midiOutputPorts : Iterable<MidiPortDetails>
        get() = midiAccess.outputs
}