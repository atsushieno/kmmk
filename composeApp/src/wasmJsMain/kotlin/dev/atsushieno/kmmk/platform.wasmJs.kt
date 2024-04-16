package dev.atsushieno.kmmk

import androidx.compose.ui.input.key.KeyEvent
import org.jetbrains.skiko.SkikoKey

actual fun getCursorKeyInput(e: KeyEvent): KeyEventCursorType {
    return when (e.nativeKeyEvent.key) {
        SkikoKey.KEY_LEFT -> KeyEventCursorType.LEFT
        SkikoKey.KEY_RIGHT -> KeyEventCursorType.RIGHT
        SkikoKey.KEY_UP -> KeyEventCursorType.UP
        SkikoKey.KEY_DOWN -> KeyEventCursorType.DOWN
        else -> KeyEventCursorType.NONE
    }
}