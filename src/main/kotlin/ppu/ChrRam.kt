package ppu

import ram.Ram

class ChrRam(size: Int) {
    var bank: UByte = 0u

    private val ram = Ram(size)

    fun read(addr: Int): Int {
        return ram.read(getActualAddress(addr))
    }

    fun write(addr: Int, data: Int) {
        ram.write(getActualAddress(addr), data)
    }

    private fun getActualAddress(addr: Int): Int {
        return addr + bank.toInt() * 0x2000
    }
}
