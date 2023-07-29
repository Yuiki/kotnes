package ppu

import ram.Ram

class PpuVRam {
    private val ram = Ram(0x2000)

    fun read(addr: Int): Int {
        val actual = if (addr >= 0x1000) addr - 0x1000 else addr
        return ram.read(actual)
    }

    fun write(addr: Int, data: Int) {
        ram.write(addr, data)
    }
}
