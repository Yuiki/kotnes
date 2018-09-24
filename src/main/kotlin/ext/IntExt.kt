package ext

fun Int.toHex(padding: Int = 2) = String.format("%0${padding}X", this)