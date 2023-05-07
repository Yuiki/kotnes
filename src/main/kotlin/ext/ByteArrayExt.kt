package ext

fun ByteArray.toHex() =
    this.map { String.format("%02X", it) }.reduce { acc, s -> acc + s }
