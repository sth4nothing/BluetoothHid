package com.sth4nothing.bluetoothhid

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.view.KeyEvent
import java.util.*

/** Extensions which expose hidden API **/

fun BluetoothAdapter.setScanMode(mode: Int, duration: Int): Boolean =
    this::class.java.getMethod("setScanMode", Int::class.java, Int::class.java).invoke(this, mode, duration) as Boolean

fun BluetoothDevice.cancelBondProcess(): Boolean =
    this::class.java.getMethod("cancelBondProcess").invoke(this) as Boolean

fun BluetoothDevice.removeBond(): Boolean =
    this::class.java.getMethod("removeBond").invoke(this) as Boolean

fun Char.toKeyCode(): Int =
    when(this) {
        in 'a'..'z' -> this - 'a' + KeyEvent.KEYCODE_A
        in 'A'..'Z' -> this - 'A' + KeyEvent.KEYCODE_A
        in '0'..'9' -> this - '0' + KeyEvent.KEYCODE_0
        '!' -> KeyEvent.KEYCODE_1
        '@' -> KeyEvent.KEYCODE_2
        '#' -> KeyEvent.KEYCODE_3
        '$' -> KeyEvent.KEYCODE_4
        '%' -> KeyEvent.KEYCODE_5
        '^' -> KeyEvent.KEYCODE_6
        '&' -> KeyEvent.KEYCODE_7
        '*' -> KeyEvent.KEYCODE_8
        '(' -> KeyEvent.KEYCODE_9
        ')' -> KeyEvent.KEYCODE_0
        '`', '~' -> KeyEvent.KEYCODE_GRAVE
        '-', '_' -> KeyEvent.KEYCODE_MINUS
        '=', '+' -> KeyEvent.KEYCODE_EQUALS
        '[', '{' -> KeyEvent.KEYCODE_LEFT_BRACKET
        ']', '}' -> KeyEvent.KEYCODE_RIGHT_BRACKET
        '\\', '|' -> KeyEvent.KEYCODE_BACKSLASH
        ';', ':' -> KeyEvent.KEYCODE_SEMICOLON
        '\'', '"' -> KeyEvent.KEYCODE_APOSTROPHE
        ',', '<' -> KeyEvent.KEYCODE_COMMA
        '.', '>' -> KeyEvent.KEYCODE_PERIOD
        '/', '?' -> KeyEvent.KEYCODE_SLASH
        ' ' -> KeyEvent.KEYCODE_SPACE
        else -> 0
    }

fun String.toKeyCode(): Int =
    if (this.length == 1) this.first().toKeyCode()
    else when (this.toLowerCase(Locale.getDefault())) {
        "home" -> KeyEvent.KEYCODE_MOVE_HOME
        "end" -> KeyEvent.KEYCODE_MOVE_END
        "page_up", "pgup" -> KeyEvent.KEYCODE_PAGE_UP
        "page_down", "pgdn" -> KeyEvent.KEYCODE_PAGE_DOWN
        "left" -> KeyEvent.KEYCODE_DPAD_LEFT
        "right" -> KeyEvent.KEYCODE_DPAD_RIGHT
        "up" -> KeyEvent.KEYCODE_DPAD_UP
        "down" -> KeyEvent.KEYCODE_DPAD_DOWN
        "enter" -> KeyEvent.KEYCODE_ENTER
        "space" -> KeyEvent.KEYCODE_SPACE
        "tab" -> KeyEvent.KEYCODE_TAB
        "backspace" -> KeyEvent.KEYCODE_DEL
        "delete", "del" -> KeyEvent.KEYCODE_FORWARD_DEL
        "insert", "ins" -> KeyEvent.KEYCODE_INSERT
        "escape", "esc" -> KeyEvent.KEYCODE_ESCAPE
        "caps_lock" -> KeyEvent.KEYCODE_CAPS_LOCK
        "num_lock" -> KeyEvent.KEYCODE_NUM_LOCK
        "print_screen", "prtsc" -> KeyEvent.KEYCODE_SYSRQ
        "menu" -> KeyEvent.KEYCODE_MENU
        "f1" -> KeyEvent.KEYCODE_F1
        "f2" -> KeyEvent.KEYCODE_F2
        "f3" -> KeyEvent.KEYCODE_F3
        "f4" -> KeyEvent.KEYCODE_F4
        "f5" -> KeyEvent.KEYCODE_F5
        "f6" -> KeyEvent.KEYCODE_F6
        "f7" -> KeyEvent.KEYCODE_F7
        "f8" -> KeyEvent.KEYCODE_F8
        "f9" -> KeyEvent.KEYCODE_F9
        "f10" -> KeyEvent.KEYCODE_F10
        "f11" -> KeyEvent.KEYCODE_F11
        "f12" -> KeyEvent.KEYCODE_F12
//        "num0", "n0" -> KeyEvent.KEYCODE_NUMPAD_0
//        "num1", "n1" -> KeyEvent.KEYCODE_NUMPAD_1
//        "num2", "n2" -> KeyEvent.KEYCODE_NUMPAD_2
//        "num3", "n3" -> KeyEvent.KEYCODE_NUMPAD_3
//        "num4", "n4" -> KeyEvent.KEYCODE_NUMPAD_4
//        "num5", "n5" -> KeyEvent.KEYCODE_NUMPAD_5
//        "num6", "n6" -> KeyEvent.KEYCODE_NUMPAD_6
//        "num7", "n7" -> KeyEvent.KEYCODE_NUMPAD_7
//        "num8", "n8" -> KeyEvent.KEYCODE_NUMPAD_8
//        "num9", "n9" -> KeyEvent.KEYCODE_NUMPAD_9
        else -> 0
    }


