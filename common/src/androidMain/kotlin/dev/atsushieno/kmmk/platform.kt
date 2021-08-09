package dev.atsushieno.kmmk

import androidx.compose.ui.input.key.KeyEvent

actual fun getCursorKeyInput(e: KeyEvent) =
    when (e.nativeKeyEvent.keyCode) {
        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> KeyEventCursorType.LEFT
        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> KeyEventCursorType.RIGHT
        android.view.KeyEvent.KEYCODE_DPAD_UP -> KeyEventCursorType.UP
        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> KeyEventCursorType.DOWN
        else -> KeyEventCursorType.NONE
    }
