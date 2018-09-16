import exception.IllegalRomException
import ext.read
import ext.readAsHex
import ext.readAsInt
import java.io.File

class Rom(rom: File) {
    val program: ByteArray
    val character: ByteArray

    init {
        val romData = rom.inputStream()
        val magicBytes = romData.readAsHex(4)
        if (magicBytes != MAGIC_BYTES) {
            throw IllegalRomException()
        }
        val prgPage = romData.readAsInt(1)
        val chrPage = romData.readAsInt(1)
        val prgSize = prgPage * PRG_PAGE_SIZE
        val chrSize = chrPage * CHR_PAGE_SIZE
        val readHeaderBytes = 6
        romData.read(HEADER_SIZE - readHeaderBytes)
        program = romData.read(prgSize)
        character = romData.read(chrSize)
    }

    companion object {
        const val MAGIC_BYTES = "4E45531A"

        const val HEADER_SIZE = 0x10
        const val PRG_PAGE_SIZE = 0x4000
        const val CHR_PAGE_SIZE = 0x2000
    }
}