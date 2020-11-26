package com.sth4nothing.bluetoothhid

enum class Separator(val value: Char) {
    PLUS('+') {
        override fun toChar() = '+'
    },
    SPACE(' ') {
        override fun toChar() = ' '
    };

    abstract fun toChar(): Char
    companion object {
        fun fromChar(sep: Char) = values().first{ it.value == sep }
    }
}