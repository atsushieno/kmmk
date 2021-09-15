package dev.atsushieno.kmmk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size

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
                    backgroundColor = if(kmmk.noteOnStates[note] > 0) Color.Cyan
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
fun MidiKeyboardButtons(kmmk: KmmkComponentContext) {
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
        val minOctave = 0
        val maxOctave = 8
        for (octave in (minOctave..maxOctave).reversed()) {
            KeyboardRow(kmmk, octave)
        }
    }
}

private val isBlackKeyFlags = arrayOf(false, true, false, true, false, false, true, false, true, false, true, false)
private val blackKeyOffsets = arrayOf(0.05f, 0.1f, 0f, 0.05f, 0.15f, 0.25f, 0f)
private val whiteKeyOffsets = arrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)
private val whiteKeyToNotes = arrayOf(0, 2, 4, 5, 7, 9, 11)

@Composable
fun MusicalKeyboardView(kmmk: KmmkComponentContext) {
    // FIXME: `Modifier.align(Alignment.CenterHorizontally)` results in: "undefined reference: align"
    //  what can I import to use `Modifier.align()` that is defined *within* ColumnScope?
    //  https://developer.android.com/reference/kotlin/androidx/compose/foundation/layout/ColumnScope?hl=en#(androidx.compose.ui.Modifier).align(androidx.compose.ui.Alignment.Horizontal)
    Column(modifier = Modifier.padding(20.dp, 20.dp)) {
        Canvas(modifier = Modifier.width(700.dp).height(40.dp)) {
            val octaves = 6
            val numWhiteKeys = 7 * octaves

            val wkWidth = size.width / numWhiteKeys
            val bkWidth = wkWidth * 0.8f
            val bkHeight = 30f

            // We render white keys first, then black keys to overlay appropriately.
            // If we render both in a single loop, then black keys might be incorrectly overdrawn by note-ons on the white keys.

            for (i in 0 until numWhiteKeys) {
                val x = i * wkWidth
                val note = (kmmk.octaveShift.value + i / 7) * 12 + whiteKeyToNotes[i % 7]
                if (note in 0..127 && kmmk.noteOnStates[note] != 0) {
                    drawRect(color = Color.Cyan, topLeft = Offset(x = x, y = 0f), size = Size(wkWidth, 50f))
                }
                drawLine(start = Offset(x = x, y = 0f), end = Offset(x = x, y = 50f), color = Color.Black)
            }

            for (i in 0 until numWhiteKeys) {
                val x = i * wkWidth
                val bkOffset = blackKeyOffsets[i % 7] * wkWidth
                if (bkOffset != 0f) {
                    val note = (kmmk.octaveShift.value + i / 7) * 12 + whiteKeyToNotes[i % 7] + 1
                    val isNoteOn = note in 0..127 && kmmk.noteOnStates[note] != 0
                    drawRect(
                        color = if (isNoteOn) Color.Cyan else Color.Black,
                        topLeft = Offset(x = x + bkOffset + wkWidth / 2, y = 0f),
                        size = Size(bkWidth, bkHeight)
                    )
                }
            }
            // rightmost edge
            drawLine(start = Offset(x = size.width, y = 0f), end = Offset(x = size.width, y = 50f), color = Color.Black)
        }
    }
}