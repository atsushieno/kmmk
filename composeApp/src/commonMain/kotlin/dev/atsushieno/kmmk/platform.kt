package dev.atsushieno.kmmk

import androidx.compose.ui.input.key.KeyEvent

enum class KeyEventCursorType {
    NONE,
    LEFT,
    RIGHT,
    UP,
    DOWN
}

expect fun getCursorKeyInput(e: KeyEvent): KeyEventCursorType
