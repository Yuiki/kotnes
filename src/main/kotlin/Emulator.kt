import apu.Apu
import cartridge.Cartridge
import cartridge.Rom
import cpu.Cpu
import cpu.CpuBus
import dma.Dma
import ext.toUnsignedInt
import interrupts.Interrupts
import pad.KeyEvent
import pad.Pad
import ppu.Canvas
import ppu.Ppu
import ram.Ram

class Emulator(
        cartridge: Cartridge,
        canvas: Canvas,
        keyEvent: KeyEvent
) {
    private val interrupts = Interrupts()
    private val chrRam = Ram(0x4000).apply {
        cartridge.character.forEachIndexed { idx, data ->
            write(idx, data.toUnsignedInt())
        }
    }

    private val ppu = Ppu(
            chrRam = chrRam,
            canvas = canvas,
            interrupts = interrupts,
            isHorizontalMirror = cartridge.isHorizontalMirror
    )

    private val apu = Apu()
    private val wRam = Ram(0x2048)
    private val prgRom = Rom(cartridge.program)
    private val dma = Dma(ppu, wRam)
    private val pad = Pad(keyEvent)

    private val cpuBus = CpuBus(
            ppu = ppu,
            apu = apu,
            ram = wRam,
            rom = prgRom,
            dma = dma,
            pad = pad
    )

    private val cpu = Cpu(
            bus = cpuBus,
            interrupts = interrupts
    )

    fun start() {
        val start = System.currentTimeMillis()
        stepFrame()
        val end = System.currentTimeMillis()
        val sleepTime = 16 /* 60 fps */ - (end - start)
        if (sleepTime > 0) {
            Thread.sleep(sleepTime)
        }
        start()
    }

    private fun stepFrame() {
        while (true) {
            var cpuCycle = 0
            if (dma.isProcessing) {
                dma.run()
                cpuCycle = 514
            }
            cpuCycle += cpu.run()
            if (ppu.run(cpuCycle * 3)) {
                break
            }
        }
    }
}