package dev.atsushieno.kmmk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver

private fun isWhiteKey(key: Int) = when (key) { 0, 2, 4, 5, 7, 9, 11 -> true else -> false }

private val rowHeaderWidth = 20.dp
private val keyBorderWidth = 1.dp
private val keyPaddingWidth = 2.dp
private val buttonTextSize = 8.5.sp
private val headerTextSize = 12.sp

@Composable
fun KeyboardRow(kmmk: KmmkComponentContext, octave: Int) {

    Row {
        Text(modifier = Modifier.width(rowHeaderWidth), text = "o$octave", fontSize = headerTextSize)

        for (key in 0..11) {
            val interactionSource = remember { MutableInteractionSource() }

            val note = octave * 12 + key

            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> { kmmk.noteOn(note) }
                        is PressInteraction.Release -> { kmmk.noteOff(note) }
                        is PressInteraction.Cancel -> { kmmk.noteOff(note) }
                    }
                }
            }

            TextButton(interactionSource = interactionSource,
                modifier = Modifier.padding(keyPaddingWidth).height(30.dp).weight(1.0f),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(
                    // FIXME: it is a compromised solution to the situation that Modifier.border() never worked
                    //  as expected to correctly surround the button region... We specify non-White color for
                    //  the white keys indicating that it is in different color than the background
                    containerColor = if (kmmk.noteOnStates[note] > 0) Color.Cyan
                    else if (isWhiteKey(key)) Color.White/*.compositeOver(Color.Gray)*/ else Color.DarkGray,
                    contentColor = if (kmmk.noteOnStates[note] > 0) Color.Black
                    else if (isWhiteKey(key)) Color.DarkGray else Color.White
                ), onClick = {}) {
                Text(text = kmmk.noteNames[key % 12], fontSize = buttonTextSize)
            }
        }
    }
}

fun getCursorKeyInput(e: KeyEvent): KeyEventCursorType {
    return when (e.key) {
        Key.DirectionLeft -> KeyEventCursorType.LEFT
        Key.DirectionRight -> KeyEventCursorType.RIGHT
        Key.DirectionUp -> KeyEventCursorType.UP
        Key.DirectionDown -> KeyEventCursorType.DOWN
        else -> KeyEventCursorType.NONE
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
fun KeyEventRecipient(kmmk: KmmkComponentContext, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val activeKeys = remember { Array (256) {false} }

    Column(modifier = modifier.onKeyEvent { evt ->
            if (isCursorEventHandled(evt, kmmk))
                return@onKeyEvent true

            val note = kmmk.getNoteFromKeyCode(evt.utf16CodePoint)
            if (note < 0)
                return@onKeyEvent false
            if (evt.type == KeyEventType.KeyDown) {
                if (!activeKeys[note]) {
                    activeKeys[note] = true
                    kmmk.noteOn(note)
                }
            } else if (evt.type == KeyEventType.KeyUp) {
                if (activeKeys[note]) {
                    activeKeys[note] = false
                    kmmk.noteOff(note)
                }
            }
            return@onKeyEvent true
        }
        .focusRequester(focusRequester)
        .focusable()
        .clickable { focusRequester.requestFocus() }) {
        content()
    }

    SideEffect {
        focusRequester.requestFocus()
    }
}

@Composable
fun MidiKeyboardButtonsFoldable(kmmk: KmmkComponentContext) {
    var foldState by remember { mutableStateOf(false) }

    Row {
        Text(if (foldState) "[+]" else "[-]", modifier = Modifier.clickable { foldState = !foldState })
    }
    if (!foldState) {
        MidiKeyboardButtons(kmmk)
    }
}

@Composable
fun MidiKeyboardButtons(kmmk: KmmkComponentContext) {
    KeyEventRecipient(kmmk) {
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