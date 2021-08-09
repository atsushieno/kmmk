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
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.GeneralMidi
import dev.atsushieno.ktmidi.MidiCIProtocolType

@Composable
fun App(kmmk: KmmkComponentContext) {
    MaterialTheme {
        Column {
            MidiSettingsView(kmmk)
            MidiKeyboard(kmmk)
            MmlPad(kmmk)
        }
    }
}

@Composable
fun MidiSettingsView(kmmk: KmmkComponentContext) {
    var midiInputDialogState by remember { mutableStateOf(false) }
    var midiOutputDialogState by remember { mutableStateOf(false) }

    Column {
        Row {

            /*
            if (midiInputDialogState) {
                // show dialog
                Column {
                    val onClick: (String) -> Unit = { id ->
                        if (id.isNotEmpty()) {
                            kmmk.midiDeviceManager.midiInputDeviceId = id
                            midiInputOnClick(id)
                        }
                        midiInputDialogState = false
                    }
                    if (kmmk.midiDeviceManager.midiInputPorts.any())
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
            */

            Column {
                if (midiOutputDialogState) {
                    val onClick: (String) -> Unit = { id ->
                        if (id.isNotEmpty()) {
                            kmmk.setOutputDevice(id)
                        }
                        midiOutputDialogState = false
                    }
                    if (kmmk.midiDeviceManager.midiOutputPorts.any())
                        for (d in kmmk.midiDeviceManager.midiOutputPorts)
                            Text(
                                modifier = Modifier.clickable(onClick = { onClick(d.id) }),
                                text = d.name ?: "(unnamed)"
                            )
                    else
                        Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(no MIDI output)")
                    Text(modifier = Modifier.clickable(onClick = { onClick("") }), text = "(Cancel)")
                } else {
                    Card(
                        modifier = Modifier.clickable(onClick = { midiOutputDialogState = true }).padding(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                    ) {
                        Text(kmmk.midiDeviceManager.midiOutput?.details?.name ?: "-- Select MIDI output --")
                    }
                }
            }
        }
        Row {
            Column {
                ProgramSelector(kmmk)
                /*
                Card(
                    modifier = Modifier.clickable(onClick = presetsOnClick).padding(12.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                ) {
                    Text("General MIDI Instruments Set")
                }
                */
            }
            Column {
                var tonalityDialogState by remember { mutableStateOf(false) }
                val onTonalitySelected = { index:Int ->
                    if (index >= 0)
                        kmmk.setTonality(index)
                    tonalityDialogState = false
                }

                if (tonalityDialogState) {
                    kmmk.tonalities.forEachIndexed { index, tonality ->
                        Text(text = tonality.name, modifier = Modifier.clickable { onTonalitySelected(index) })
                    }
                    Text(text = "(Cancel)", modifier = Modifier.clickable { onTonalitySelected(-1) })
                } else {
                    Card(modifier = Modifier.clickable { tonalityDialogState = true }.padding(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                    ) {
                        Text(text = kmmk.tonalities[kmmk.selectedTonality.value].name)
                    }
                }
            }
            Column {
                var keyboardDialogState by remember { mutableStateOf(false) }
                val onKeyboardSelected = { index:Int ->
                    if (index >= 0)
                        kmmk.setKeyboard(index)
                    keyboardDialogState = false
                }

                if (keyboardDialogState) {
                    kmmk.keyboards.forEachIndexed { index, keyboard ->
                        Text(text = keyboard.name, modifier = Modifier.clickable { onKeyboardSelected(index) })
                    }
                    Text(text = "(Cancel)", modifier = Modifier.clickable { onKeyboardSelected(-1) })
                } else {
                    Card(modifier = Modifier.clickable { keyboardDialogState = true }.padding(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
                    ) {
                        Text(text = kmmk.keyboards[kmmk.selectedKeyboard.value].name)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramSelector(kmmk: KmmkComponentContext) {
    var programCategoryDialogState by remember { mutableStateOf(false) }
    var programCategoryState by remember { mutableStateOf(-1) }

    if (programCategoryState >= 0) {
        Text(text = "${programCategoryState * 8}: ${GeneralMidi.INSTRUMENT_CATEGORIES[programCategoryState]} -> ")
        Column {
            val onSelectProgram: (Int) -> Unit = { selection ->
                if (selection >= 0)
                    kmmk.sendProgramChange(selection)
                programCategoryState = -1
                programCategoryDialogState = (selection == -2)
            }
            GeneralMidi.INSTRUMENT_NAMES.drop(programCategoryState * 8).take(8).forEachIndexed { index, program ->
                val programValue = programCategoryState * 8 + index
                Text(
                    modifier = Modifier.clickable(onClick = { onSelectProgram(programValue) }),
                    text = "${programValue}: $program"
                )
            }
            Text(modifier = Modifier.clickable(onClick = { onSelectProgram(-2) }), text = "(Back)")
            Text(modifier = Modifier.clickable(onClick = { onSelectProgram(-1) }), text = "(Cancel)")
        }
    } else {
        if (programCategoryDialogState) {
            Column {
                val onSelectCategory: (Int) -> Unit = { category ->
                    programCategoryState = category
                    programCategoryDialogState = false
                }
                GeneralMidi.INSTRUMENT_CATEGORIES.forEachIndexed { index, category ->
                    Text(
                        modifier = Modifier.clickable(onClick = { onSelectCategory(index) }),
                        text = "${index * 8}: $category"
                    )
                }
                Text(modifier = Modifier.clickable(onClick = { onSelectCategory(-1) }), text = "(Cancel)")
            }
        } else {
            Card(
                modifier = Modifier.clickable(onClick = { programCategoryDialogState = true }).padding(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
            ) {
                Text(GeneralMidi.INSTRUMENT_NAMES[kmmk.program.value]);
            }
        }
    }
}

@Composable
fun MmlPad(kmmk: KmmkComponentContext) {
    val mmlState by remember { kmmk.mmlTextState }
    val mmlOnClick = { s:String -> kmmk.playMml(s) }
    var midi2EnabledState by remember { mutableStateOf(kmmk.midiProtocol == MidiCIProtocolType.MIDI2) }
    var shouldRecordMmlState by remember { mutableStateOf(kmmk.shouldRecordMml) }

    Column {
        Row {
            val onMidi2Checked = {
                midi2EnabledState = !midi2EnabledState
                kmmk.midiProtocol = if (midi2EnabledState) MidiCIProtocolType.MIDI2 else MidiCIProtocolType.MIDI1
            }
            Row(modifier = Modifier.padding(6.dp).clickable{ onMidi2Checked() }) {
                Checkbox(checked = midi2EnabledState, onCheckedChange = { onMidi2Checked() })
                Text("MIDI 2.0")
            }
            val onRecordMmlChecked = {
                shouldRecordMmlState = !shouldRecordMmlState
                kmmk.shouldRecordMml = shouldRecordMmlState
            }
            Row(modifier = Modifier.padding(6.dp).clickable { onRecordMmlChecked() }) {
                Checkbox(checked = shouldRecordMmlState, onCheckedChange = {  onRecordMmlChecked() })
                Text("Record MML")
            }
        }
        Row {
            Button(onClick = { mmlOnClick(mmlState) }, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text("Play")
            }
            OutlinedTextField(
                value = mmlState,
                onValueChange = { value ->
                    kmmk.mmlText = value
                },
                placeholder = { Text("enter MML text here (no need for track id)") },
                modifier = Modifier.fillMaxWidth().padding(12.dp).height(150.dp),
            )
        }
    }
}


