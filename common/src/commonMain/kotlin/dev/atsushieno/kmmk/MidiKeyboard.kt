package dev.atsushieno.kmmk

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private val noteNames = arrayOf("c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b")
private fun isWhiteKey(key: Int) = when (key) { 0, 2, 4, 5, 7, 9, 11 -> true else -> false }

private val rowHeaderWidth = 20.dp
private val noteWidth = 28.dp
private val keyBorderWidth = 1.dp
private val keyPaddingWidth = 1.dp
private val buttonTextSize = 8.5.sp
private val headerTextSize = 12.sp

@Composable
fun KeyboardRow(octave: Int, onNoteOn: (Int) -> Unit = {}, onNoteOff: (Int) -> Unit = {}) {
    Row {
        Text(modifier = Modifier.width(rowHeaderWidth), text = "o$octave", fontSize = headerTextSize)

        for (key in 0..11) {
            val keyId = "Keyboard Octave$octave Key$key"
            TextButton(modifier = Modifier.padding(keyPaddingWidth).weight(1.0f).border(keyBorderWidth, Color.Black)
                .pointerInput(key1 = keyId) {
                    while (true) {
                        this.awaitPointerEventScope {
                            awaitPointerEvent(pass = PointerEventPass.Main)
                            onNoteOn(octave * 12 + key)
                            while (true)
                                // FIXME: maybe there is some way to filter events to drop only up to `!pressed`...
                                if (awaitPointerEvent(pass = PointerEventPass.Main).changes.any { c -> !c.pressed })
                                    break
                            onNoteOff(octave * 12 + key)
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if(isWhiteKey(key)) Color.White else Color.DarkGray,
                    contentColor = if(isWhiteKey(key)) Color.DarkGray else Color.White),
                onClick = {}) {
                Text(text = noteNames[key % 12], fontSize = buttonTextSize)
            }
        }
    }
}


@Composable
fun MidiKeyboard(onNoteOn: (Int) -> Unit = {}, onNoteOff: (Int) -> Unit = {}, minOctave: Int = 0, maxOctave: Int = 8) {
    val focusRequester = remember { FocusRequester() }
    val activeKeys = remember { Array (256) {false} }

    Column(modifier = Modifier
        .onKeyEvent { evt ->
            val note = model.getNoteFromKeyCode(evt.utf16CodePoint)
            if (note < 0)
                return@onKeyEvent false
            if (evt.type == KeyEventType.KeyDown) {
                if (!activeKeys[note]) {
                    activeKeys[note] = true
                    GlobalScope.launch { model.noteOn(note) }
                }
            } else if (evt.type == KeyEventType.KeyUp) {
                if (activeKeys[note]) {
                    activeKeys[note] = false
                    GlobalScope.launch { model.noteOff(note) }
                }
            }
            return@onKeyEvent true
        }
        .focusRequester(focusRequester)
        .focusable()
        .clickable { focusRequester.requestFocus() }
    ) {
        Row {
            Text(text = "oct.", fontSize = headerTextSize,
                modifier = Modifier.width(rowHeaderWidth))
            for (key in 0..11) {
                Text(text = noteNames[key % 12], fontSize = headerTextSize, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.0f).padding(keyPaddingWidth)
                )
            }
        }
        for (octave in (minOctave..maxOctave).reversed()) {
            KeyboardRow(octave, onNoteOn, onNoteOff)
        }
    }
}
