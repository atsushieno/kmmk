package dev.atsushieno.kmmk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.MidiCIProtocolType
import dev.atsushieno.mugene.MmlCompiler
import dev.atsushieno.mugene.MmlException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        Column {
            val instrumentOnClick = {
            }
            val presetsOnClick = {
            }
            val midiInputOnClick: (String) -> Unit = {
            }
            val midiOutputOnClick: (String) -> Unit = {
            }
            MidiSettingsView(instrumentOnClick = instrumentOnClick, presetsOnClick = presetsOnClick, midiInputOnClick = midiInputOnClick, midiOutputOnClick = midiOutputOnClick)
            MidiKeyboard(onNote = { key ->
                GlobalScope.launch {
                    model.playNote(key)
                }
            })
            MmlPad()
        }
    }
}

@Composable
fun MidiSettingsView(midiInputOnClick: (String) -> Unit,
                     midiOutputOnClick: (String) -> Unit,
                     instrumentOnClick: () -> Unit,
                     presetsOnClick: () -> Unit) {
    Column {
        Row {
            var midiInputDialogState by remember { mutableStateOf(false) }
            var midiOutputDialogState by remember { mutableStateOf(false) }

            if (midiInputDialogState) {
                // show dialog
                Column {
                    val onClick: (String) -> Unit = { id ->
                        if (id.isNotEmpty()) {
                            model.midiDeviceManager.midiInputDeviceId = id
                            midiInputOnClick(id)
                        }
                        midiInputDialogState = false
                    }
                    if (model.midiDeviceManager.midiInputPorts.any())
                        for (d in model.midiDeviceManager.midiInputPorts)
                            Text(
                                modifier = Modifier.clickable(onClick = { onClick(d.id) }),
                                text = d.name ?: "(unnamed)"
                            )
                    else
                        Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(no MIDI input)")
                    Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(Cancel)")
                }
            } else {
                Card(
                    modifier = Modifier.clickable(onClick = { midiInputDialogState = true }).padding(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                ) {
                    Text(model.midiDeviceManager.midiInput?.details?.name ?: "-- Select MIDI input --")
                }
            }

            if (midiOutputDialogState) {
                Column {
                    val onClick: (String) -> Unit = { id ->
                        if (id.isNotEmpty()) {
                            model.midiDeviceManager.midiOutputDeviceId = id
                            midiOutputOnClick(id)
                        }
                        midiOutputDialogState = false
                    }
                    if (model.midiDeviceManager.midiOutputPorts.any())
                        for (d in model.midiDeviceManager.midiOutputPorts)
                            Text(
                                modifier = Modifier.clickable(onClick = { onClick(d.id) }),
                                text = d.name ?: "(unnamed)"
                            )
                    else
                        Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(no MIDI output)")
                    Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(Cancel)")
                }
            } else {
                Card(
                    modifier = Modifier.clickable(onClick = { midiOutputDialogState = true }).padding(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                ) {
                    Text(model.midiDeviceManager.midiOutput?.details?.name ?: "-- Select MIDI output --")
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

fun playMml(mml: String) {
    val mmlModified = "0 $mml"
    val compiler = MmlCompiler.create()
    model.compilationDiagnostics.clear()
    compiler.report = { verbosity, location, message -> model.compilationDiagnostics.add("$verbosity $location: $message") }
    try {
        val music = compiler.compile(false, mmlModified)
        model.registerMusic(music)
    } catch(ex: MmlException) {
        println(ex)
    }
}

@Composable
fun MmlPad() {
    var mmlState by remember { mutableStateOf("") }
    val mmlOnClick = { s:String -> playMml(s) }
    var midi2EnabledState by remember { mutableStateOf(model.midiProtocol == MidiCIProtocolType.MIDI2) }

    Row {
        Checkbox(checked = midi2EnabledState, onCheckedChange = { value ->
            midi2EnabledState = value
            model.midiProtocol = if (value) MidiCIProtocolType.MIDI2 else MidiCIProtocolType.MIDI1
        })
        Text("Send MIDI 2.0 UMPs")
    }
    Row {
        Button(onClick = { mmlOnClick(mmlState) }, modifier = Modifier.align(Alignment.CenterVertically)) {
            Text("Run")
        }
        OutlinedTextField(
            value = mmlState,
            placeholder = { Text("enter MML text here (no need for track id)") },
            onValueChange = { mmlState = it },
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(100.dp),
        )
    }
}


