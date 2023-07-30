package cartridge

import apu.extract
import ext.toUnsignedInt

class Rom(
    private val data: ByteArray,
    private val mapper: Mapper,
) {
    val size get() = data.size

    fun read(addr: Int): Int {
        val actualAddr = mapper.selectActualRomAddress(addr)
        return data[actualAddr].toUnsignedInt()
    }

    fun write(addr: Int, data: Int) {
        val bank = data.toUByte().extract(0..1)
        when (mapper) {
            is Mapper2 -> {
                mapper.bank = bank
            }

            else -> {}
        }
    }
}
