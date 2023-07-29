package ext

fun ByteArray.toHex() =
    this.map { it.toHex() }.reduce { acc, s -> acc + s }
