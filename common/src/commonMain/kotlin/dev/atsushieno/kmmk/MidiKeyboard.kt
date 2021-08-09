package dev.atsushieno.kmmk

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private fun isWhiteKey(key: Int) = when (key) { 0, 2, 4, 5, 7, 9, 11 -> true else -> false }

private val rowHeaderWidth = 20.dp
private val keyBorderWidth = 1.dp
private val keyPaddingWidth = 1.dp
private val buttonTextSize = 8.5.sp
private val headerTextSize = 12.sp

@Composable
fun KeyboardRow(kmmk: KmmkComponentContext, octave: Int) {

    Row {
        Text(modifier = Modifier.width(rowHeaderWidth), text = "o$octave", fontSize = headerTextSize)

        for (key in 0..11) {
            val note = octave * 12 + key
            val keyId = "Keyboard Octave$octave Key$key"
            TextButton(modifier = Modifier.padding(keyPaddingWidth)
                .weight(1.0f)
                .border(keyBorderWidth, Color.Black)
                .pointerInput(key1 = keyId) {
                    while (true) {
                        this.awaitPointerEventScope {
                            awaitPointerEvent(pass = PointerEventPass.Main)
                            kmmk.noteOn(note)
                            while (true)
                                // FIXME: maybe there is some way to filter events to drop only up to `!pressed`...
                                if (awaitPointerEvent(pass = PointerEventPass.Main).changes.any { c -> !c.pressed })
                                    break
                            kmmk.noteOff(note)
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if(kmmk.noteOnStates[note] > 0) Color.Yellow
                        else if(isWhiteKey(key)) Color.White else Color.DarkGray,
                    contentColor = if(kmmk.noteOnStates[note] > 0) Color.Black
                        else if(isWhiteKey(key)) Color.DarkGray else Color.White),
                onClick = {}) {
                Text(text = kmmk.noteNames[key % 12], fontSize = buttonTextSize)
            }
        }
    }
}


fun isCursorEventHandled(evt: KeyEvent, kmmk: KmmkComponentContext): Boolean {
    val cursor = getCursorKeyInput(evt)
    if (cursor == KeyEventCursorType.NONE)
        return false
    if (evt.type == KeyEventType.KeyUp && evt.isShiftPressed)
        when (cursor) {
            KeyEventCursorType.UP -> kmmk.octaveShift.value++
            KeyEventCursorType.DOWN -> kmmk.octaveShift.value--
            KeyEventCursorType.LEFT -> kmmk.noteShift.value--
            KeyEventCursorType.RIGHT -> kmmk.noteShift.value++
            else -> return false
        }
    // return true even if it was not handled as KEY_UP or SHIFT pressed - it causes annoying focus move on Android.
    return true
}

@Composable
fun MidiKeyboard(kmmk: KmmkComponentContext) {
    val focusRequester = remember { FocusRequester() }
    val activeKeys = remember { Array (256) {false} }

    Column(modifier = Modifier
        .onKeyEvent { evt ->
            if (isCursorEventHandled(evt, kmmk))
                return@onKeyEvent true

            val note = kmmk.getNoteFromKeyCode(evt.utf16CodePoint)
            if (note < 0)
                return@onKeyEvent false
            if (evt.type == KeyEventType.KeyDown) {
                if (!activeKeys[note]) {
                    activeKeys[note] = true
                    GlobalScope.launch { kmmk.noteOn(note) }
                }
            } else if (evt.type == KeyEventType.KeyUp) {
                if (activeKeys[note]) {
                    activeKeys[note] = false
                    GlobalScope.launch { kmmk.noteOff(note) }
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
                Text(text = kmmk.noteNames[key % 12], fontSize = headerTextSize, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.0f).padding(keyPaddingWidth)
                )
            }
        }
        val minOctave = 0
        val maxOctave = 8
        for (octave in (minOctave..maxOctave).reversed()) {
            KeyboardRow(kmmk, octave)
        }
    }
}
