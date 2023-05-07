package cartridge

import ext.toUnsignedInt

class Rom(
    private val data: ByteArray,
) {
    val size get() = data.size

    fun read(addr: Int) = data[addr].toUnsignedInt()
}
