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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.GeneralMidi2
import dev.atsushieno.ktmidi.MidiCIProtocolType
import androidx.compose.ui.graphics.Color
import dev.atsushieno.ktmidi.MidiTransportProtocol
import kotlinx.coroutines.launch

@Composable
fun App(kmmk: KmmkComponentContext) {
    MaterialTheme {
        Column(modifier = Modifier.wrapContentHeight()/*.background(Color(0xE8E8E8E8)*)*/) {
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
    val coroutineScope = rememberCoroutineScope()

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
            // These click handlers look like duplicates, but only either of Modifier.clickable or Checkbox.onCheckedChange is invoked...
            Checkbox(
                checked = kmmk.midiProtocol.value == MidiTransportProtocol.UMP,
                onCheckedChange = {
                    kmmk.onMidiProtocolUpdated()
                    coroutineScope.launch { kmmk.closeAllPorts() }
                })
            Text("MIDI 2.0", Modifier.align(Alignment.CenterVertically))
            MidiDeviceSelector(kmmk)
            Column {
                val midiOutputError by remember { kmmk.midiDeviceManager.midiOutputError }
                if (midiOutputError != null) {
                    var showErrorDetails by remember { mutableStateOf(false) }
                    if (showErrorDetails) {
                        val closeDeviceErrorDialog = { showErrorDetails = false }
                        AlertDialog(onDismissRequest = closeDeviceErrorDialog,
                            confirmButton = { Button(onClick = closeDeviceErrorDialog) { Text("OK") } },
                            title = { Text("MIDI device error") },
                            text = {
                                Column {
                                    Row {
                                        Text("MIDI output is disabled until new device is selected.")
                                    }
                                    Row {
                                        Text(midiOutputError?.message ?: "(error details lost...)")
                                    }
                                }
                            }
                        )
                    }
                    Button(onClick = { showErrorDetails = true }) {
                        Text(text = "!!", color = Color.Red)
                    }
                }
            }
            Column(Modifier.align(Alignment.CenterVertically)) {
                Text(text = "Oct.: ${kmmk.octaveShift.value} / Trans.: ${kmmk.noteShift.value}",
                    modifier = Modifier.padding(12.dp, 0.dp))
            }
        }
    }
}

@Composable
fun MidiDeviceSelector(kmmk: KmmkComponentContext) {
    var midiOutputDialogState by remember { mutableStateOf(false) }
    val coroutineContext = rememberCoroutineScope()

    DropdownMenu(expanded = midiOutputDialogState, onDismissRequest = { midiOutputDialogState = false }) {
        val onClick: (String) -> Unit = { id ->
            if (id.isNotEmpty()) {
                coroutineContext.launch { kmmk.setOutputDevice(id) }
            }
            midiOutputDialogState = false
        }
        val outputPorts = kmmk.midiOutputPorts
        if (outputPorts.any())
            for (d in outputPorts)
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
        val name = if (kmmk.midiDeviceManager.midiOutput?.details?.midiTransportProtocol == kmmk.midiProtocol.value)
            kmmk.midiDeviceManager.midiOutput?.details?.name else null
        Text(name ?: "-- Select MIDI output --")
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
            GeneralMidi2.instrumentNames.drop(programCategoryState * 8).take(8).forEachIndexed { index, program ->
                val programValue = programCategoryState * 8 + index
                DropdownMenuItem(onClick = { onSelectProgram(programValue) }) { Text("${programValue}: $program") }
            }
            DropdownMenuItem(onClick = { onSelectProgram(-2) }) { Text("(Back)") }
        } else {
            val onSelectCategory: (Int) -> Unit = { category ->
                programCategoryState = category
                programCategoryDialogState = false
            }
            GeneralMidi2.categories.forEachIndexed { index, category ->
                DropdownMenuItem(onClick = { onSelectCategory(index) }) { Text("${index * 8}: $category") }
            }
        }
    }
    Card(
        modifier = Modifier.clickable(onClick = { programCategoryDialogState = true }).padding(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
    ) {
        Text(GeneralMidi2.instrumentNames[kmmk.program.value])
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
