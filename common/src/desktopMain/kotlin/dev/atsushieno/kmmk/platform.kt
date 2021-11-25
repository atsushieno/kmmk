package dev.atsushieno.kmmk

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode

actual fun getCursorKeyInput(e: KeyEvent) =
    when (e.key.nativeKeyCode) {
        java.awt.event.KeyEvent.VK_LEFT -> KeyEventCursorType.LEFT
        java.awt.event.KeyEvent.VK_RIGHT -> KeyEventCursorType.RIGHT
        java.awt.event.KeyEvent.VK_UP -> KeyEventCursorType.UP
        java.awt.event.KeyEvent.VK_DOWN -> KeyEventCursorType.DOWN
        else -> KeyEventCursorType.NONE
    }

