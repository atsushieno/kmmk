package dev.atsushieno.kmmk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.GeneralMidi
import dev.atsushieno.ktmidi.MidiCIProtocolType
import dev.atsushieno.composempp.material.AlertDialog
import dev.atsushieno.composempp.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.ui.graphics.Color

@Composable
fun App(kmmk: KmmkComponentContext) {
    MaterialTheme {
        Column(modifier = Modifier.wrapContentHeight().background(Color(0xE8E8E8E8))) {
            AppSettingsView(kmmk)
            MusicalKeyboardView(kmmk)
            MidiKeyboardButtonsFoldable(kmmk)
            MmlPad(kmmk)
            if (!kmmk.compilationDiagnostics.isEmpty()) {
                val compilationDianogsticsState = remember { kmmk.compilationDiagnostics }
                val closeDialog = { compilationDianogsticsState.clear() }
                AlertDialog(onDismissRequest = closeDialog,
                    confirmButton = { Button(onClick = closeDialog) { Text("OK") } },
                    text = { Text(compilationDianogsticsState.joinToString("\n")) }
                )
            }
        }
    }
}

@Composable
fun AppSettingsView(kmmk: KmmkComponentContext) {

    Column {
        Row {
            Column {
                ProgramSelector(kmmk)
            }
            Column {
                TonalitySelector(kmmk)
            }
            Column {
                KeyboardLayoutSelector(kmmk)
            }
        }
        Row {
            Column {
                MidiDeviceSelector(kmmk)
            }
            Column {
                Text(text = "Oct.: ${kmmk.octaveShift.value} / Trans.: ${kmmk.noteShift.value}",
                    modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
fun MidiDeviceSelector(kmmk: KmmkComponentContext) {
    var midiOutputDialogState by remember { mutableStateOf(false) }

    DropdownMenu(expanded = midiOutputDialogState, onDismissRequest = { midiOutputDialogState = false}) {
        val onClick: (String) -> Unit = { id ->
            if (id.isNotEmpty()) {
                kmmk.setOutputDevice(id)
            }
            midiOutputDialogState = false
        }
        if (kmmk.midiOutputPorts.any())
            for (d in kmmk.midiOutputPorts)
                DropdownMenuItem(onClick = { onClick(d.id) }) {
                    Text(d.name ?: "(unnamed)")
                }
        else
            DropdownMenuItem(onClick = { onClick("") }) { Text("(no MIDI output)") }
        DropdownMenuItem(onClick = { onClick("") }) { Text("(Cancel)") }
    }
    Card(
        modifier = Modifier.clickable(onClick = {
            kmmk.updateMidiDeviceList()
            midiOutputDialogState = true
        }).padding(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
    ) {
        Text(kmmk.midiDeviceManager.midiOutput?.details?.name ?: "-- Select MIDI output --")
    }
}

@Composable
fun ProgramSelector(kmmk: KmmkComponentContext) {
    var programCategoryDialogState by remember { mutableStateOf(false) }
    var programCategoryState by remember { mutableStateOf(-1) }

    DropdownMenu(expanded = programCategoryDialogState || programCategoryState >= 0, onDismissRequest = { programCategoryDialogState = false}) {
        if (programCategoryState >= 0) {
            //Text(text = "${programCategoryState * 8}: ${GeneralMidi.INSTRUMENT_CATEGORIES[programCategoryState]} -> ")
            val onSelectProgram: (Int) -> Unit = { selection ->
                if (selection >= 0)
                    kmmk.sendProgramChange(selection)
                programCategoryState = -1
                programCategoryDialogState = (selection == -2)
            }
            GeneralMidi.INSTRUMENT_NAMES.drop(programCategoryState * 8).take(8).forEachIndexed { index, program ->
                val programValue = programCategoryState * 8 + index
                DropdownMenuItem(onClick = { onSelectProgram(programValue) }) { Text("${programValue}: $program") }
            }
            DropdownMenuItem(onClick = { onSelectProgram(-2) }) { Text("(Back)") }
        } else {
            val onSelectCategory: (Int) -> Unit = { category ->
                programCategoryState = category
                programCategoryDialogState = false
            }
            GeneralMidi.INSTRUMENT_CATEGORIES.forEachIndexed { index, category ->
                DropdownMenuItem(onClick = { onSelectCategory(index) }) { Text("${index * 8}: $category") }
            }
        }
    }
    Card(
        modifier = Modifier.clickable(onClick = { programCategoryDialogState = true }).padding(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
    ) {
        Text(GeneralMidi.INSTRUMENT_NAMES[kmmk.program.value])
    }
}

@Composable
fun TonalitySelector(kmmk: KmmkComponentContext) {
    var tonalityDialogState by remember { mutableStateOf(false) }
    val onTonalitySelected = { index:Int ->
        if (index >= 0)
            kmmk.setTonality(index)
        tonalityDialogState = false
    }

    DropdownMenu(expanded = tonalityDialogState, onDismissRequest = { tonalityDialogState = false}) {
        kmmk.tonalities.forEachIndexed { index, tonality ->
            DropdownMenuItem(onClick = { onTonalitySelected(index) }) { Text(tonality.name) }
        }
    }
    Card(modifier = Modifier.clickable { tonalityDialogState = true }.padding(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
    ) {
        Text(text = kmmk.tonalities[kmmk.selectedTonality.value].name)
    }
}

@Composable
fun KeyboardLayoutSelector(kmmk: KmmkComponentContext) {
    var keyboardDialogState by remember { mutableStateOf(false) }
    val onKeyboardSelected = { index:Int ->
        if (index >= 0)
            kmmk.setKeyboard(index)
        keyboardDialogState = false
    }

    DropdownMenu(expanded = keyboardDialogState, onDismissRequest = { keyboardDialogState = false}) {
        kmmk.keyboards.forEachIndexed { index, keyboard ->
            DropdownMenuItem(onClick = { onKeyboardSelected(index) }) { Text(text = keyboard.name) }
        }
    }
    Card(modifier = Modifier.clickable { keyboardDialogState = true }.padding(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
    ) {
        Text(text = kmmk.keyboards[kmmk.selectedKeyboard.value].name)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MmlPad(kmmk: KmmkComponentContext) {
    Column {
        Row {
            // These click handlers look like duplicates, but only either of Modifier.clickable or Checkbox.onCheckedChange is invoked...
            Row(modifier = Modifier.padding(6.dp).clickable { kmmk.onMidiProtocolUpdated() }) {
                Checkbox(
                    checked = kmmk.midiProtocol.value == MidiCIProtocolType.MIDI2,
                    onCheckedChange = { kmmk.onMidiProtocolUpdated() })
                Text("MIDI 2.0")
            }
            val onRecordMmlChecked = {
                kmmk.shouldRecordMml.value = !kmmk.shouldRecordMml.value
            }
            Row(modifier = Modifier.padding(6.dp).clickable { onRecordMmlChecked() }) {
                Checkbox(checked = kmmk.shouldRecordMml.value, onCheckedChange = { onRecordMmlChecked() })
                Text("Record MML")
            }
            val onUseDrumChannelChecked = {
                kmmk.useDrumChannel.value = !kmmk.useDrumChannel.value
            }
            Row(modifier = Modifier.padding(6.dp).clickable { onUseDrumChannelChecked() }) {
                Checkbox(checked = kmmk.useDrumChannel.value, onCheckedChange = { onUseDrumChannelChecked() })
                Text("Use 10ch.")
            }
        }
        Row {
            val onShouldOutputNoteLengthChecked = {
                kmmk.shouldOutputNoteLength.value = !kmmk.shouldOutputNoteLength.value
            }
            Row(modifier = Modifier.padding(6.dp).clickable { onShouldOutputNoteLengthChecked() }) {
                Checkbox(checked = kmmk.shouldOutputNoteLength.value, onCheckedChange = {  onShouldOutputNoteLengthChecked() })
                Text("Output note length.")
                Text("BPM: ${kmmk.currentTempo.value.toInt()}")
                Slider(value = kmmk.currentTempo.value.toFloat(),
                    onValueChange = { kmmk.currentTempo.value = it.toDouble() },
                    valueRange = 60f .. 240f)
            }
        }
        Row {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Button(
                    modifier = Modifier.padding(6.dp),
                    onClick = { kmmk.playMml(kmmk.mmlText.value, false) }
                ) {
                    Text("Play")
                }
                Button(
                    modifier = Modifier.padding(6.dp),
                    onClick = { kmmk.playMml(kmmk.mmlText.value, true) }
                ) {
                    Text("Send as Input")
                }
            }
            OutlinedTextField(
                value = kmmk.mmlText.value,
                onValueChange = { value ->
                    kmmk.mmlText.value = value
                },
                placeholder = { Text("enter MML text here (no need for track id)") },
                modifier = Modifier.fillMaxWidth().padding(12.dp).height(150.dp),
            )
        }
    }
}
