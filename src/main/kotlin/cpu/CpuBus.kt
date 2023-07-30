package cpu

import apu.Apu
import apu.extract
import cartridge.Rom
import dma.Dma
import pad.Pad
import ppu.Ppu
import ram.Ram

class CpuBus(
    private val ppu: Ppu,
    private val apu: Apu,
    private val ram: Ram,
    private val rom: Rom,
    private val dma: Dma,
    private val pad: Pad,
) {
    fun read(addr: Int) =
        when {
            addr < 0x2000 -> ram.read(addr % 0x800)
            addr < 0x4000 -> ppu.read((addr - 0x2000) % 8)
            addr == 0x4015 -> apu.read(0x15)
            addr == 0x4016 -> pad.read()
            addr >= 0xC000 -> {
                val offset = -if (rom.size <= 0x4000) 0xC000 else 0x8000
                rom.read(addr + offset)
            }

            addr >= 0x8000 -> rom.read(addr - 0x8000)
            else -> 0
        }

    fun write(addr: Int, data: Int) {
        when {
            addr < 0x2000 -> ram.write(addr % 0x800, data)
            addr < 0x4000 -> ppu.write((addr - 0x2000) % 8, data)
            addr == 0x4014 -> dma.write(data)
            addr == 0x4016 -> pad.write(data)
            addr < 0x4020 -> apu.write(addr - 0x4000, data.toUByte())
            addr in 0x8000..0xFFFE -> {
                ppu.selectBank(data.toUByte().extract(0..1))
            }
        }
    }
}
