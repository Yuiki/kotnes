package ext

fun Byte.toUnsignedInt() = this.toInt() and 0xFF

fun Byte.toHex() = String.format("%02X", this)
