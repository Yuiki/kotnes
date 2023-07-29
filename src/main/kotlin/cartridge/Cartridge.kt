package cartridge

import apu.extract
import apu.isSetUByte
import ext.read
import ext.readAsHex
import ext.readAsInt
import java.io.File

class Cartridge(rom: File) {
    val program: ByteArray
    val character: ByteArray
    val isHorizontalMirror: Boolean
    private val mapper: UByte

    init {
        val romData = rom.inputStream()
        val magicBytes = romData.readAsHex(4)
        if (magicBytes != MAGIC_BYTES) {
            throw IllegalArgumentException("The file is not iNES file.")
        }
        val prgPage = romData.readAsInt(1)
        val chrPage = romData.readAsInt(1)
        val prgSize = prgPage * PRG_PAGE_SIZE
        val chrSize = chrPage * CHR_PAGE_SIZE
        val flg6 = romData.readAsInt(1).toUByte()
        isHorizontalMirror = !flg6.isSetUByte(0u)
        mapper = flg6.extract(4..7)
        println("Mapper: $mapper")
        val flg7 = romData.readAsInt(1)
        val flg8 = romData.readAsInt(1)
        val flg9 = romData.readAsInt(1)
        val flg10 = romData.readAsInt(1)
        romData.read(5) // unused padding
        program = romData.read(prgSize)
        character = romData.read(chrSize)
    }

    companion object {
        const val MAGIC_BYTES = "4E45531A"

        const val PRG_PAGE_SIZE = 0x4000 // 16KB
        const val CHR_PAGE_SIZE = 0x2000
    }
}
