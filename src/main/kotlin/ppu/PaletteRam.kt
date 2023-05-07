package ppu

import ram.Ram

class PaletteRam {
    private val ram = Ram(0x20)

    val data
        get() =
            (0 until ram.size).map {
                ram.read(it)
            }.mapIndexed { idx, data ->
                return@mapIndexed when {
                    isSpriteMirror(idx) -> ram.read(idx - 0x10)
                    isBackgroundMirror(idx) -> ram.read(0x00)
                    else -> data
                }
            }.toIntArray()

    fun write(addr: Int, data: Int) {
        ram.write(calcPaletteAddr(addr), data)
    }

    private fun calcPaletteAddr(addr: Int): Int {
        val paletteAddr = (addr and 0xFF) % 0x20
        return paletteAddr - if (isSpriteMirror(paletteAddr)) 0x10 else 0
    }

    private fun isSpriteMirror(addr: Int) =
        addr == 0x10 || addr == 0x14 || addr == 0x18 || addr == 0x1C

    private fun isBackgroundMirror(addr: Int) =
        addr == 0x04 || addr == 0x08 || addr == 0x0c
}
