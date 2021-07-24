package dev.atsushieno.kmmk

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.runBlocking


private val noteNames = arrayOf("c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b")
private fun isWhiteKey(key: Int) = when (key) { 0, 2, 4, 5, 7, 9, 11 -> true else -> false }

private val rowHeaderWidth = 32.dp
private val noteWidth = 24.dp
private val keyBorderWidth = 1.dp
private val keyPaddingWidth = 1.dp

@Composable
fun KeyboardRow(octave: Int, onNoteOn: (Int) -> Unit = {}, onNoteOff: (Int) -> Unit = {}) {
    Row {
        Text(modifier = Modifier.width(rowHeaderWidth), text = "o$octave")
        for (key in 0..11) {
            val keyId = "Keyboard Octave$octave Key$key"
            Button(modifier = Modifier.padding(keyPaddingWidth).width(noteWidth).border(keyBorderWidth, Color.Black)
                .pointerInput(key1 = keyId) {
                    this.awaitPointerEventScope {
                        while (true) {
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
                colors = ButtonDefaults.buttonColors(if(isWhiteKey(key)) Color.White else Color.DarkGray), onClick = {}) {
            }
        }
    }
}

@Composable
fun MidiKeyboard(onNoteOn: (Int) -> Unit = {}, onNoteOff: (Int) -> Unit = {}, minOctave: Int = 0, maxOctave: Int = 8) {
    Column {
        Row {
            Text(text = "oct.", fontSize = 0.75.em,
                modifier = Modifier.width(rowHeaderWidth))
            for (key in 0..11) {
                Text(text = noteNames[key % 12], fontSize = 0.75.em, textAlign = TextAlign.Center,
                    modifier = Modifier.width(noteWidth + keyBorderWidth * 2).padding(keyPaddingWidth)
                )
            }
        }
        for (octave in (minOctave..maxOctave).reversed()) {
            KeyboardRow(octave, onNoteOn, onNoteOff)
        }
    }
}
