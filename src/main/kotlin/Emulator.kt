import ext.toUnsignedInt
import pad.KeyEvent
import pad.Pad

class Emulator(
        rom: Rom,
        canvas: Canvas,
        keyEvent: KeyEvent
) {
    private val interrupts = Interrupts()

    private val ppu = Ppu(PpuBus(Ram(0x4000).apply {
        rom.character.forEachIndexed { idx, data -> write(idx, data.toUnsignedInt() ) }
    }), canvas = canvas, interrupts = interrupts, config = Config(rom.isHorizontalMirror))

    private val ram = Ram(0x2048)

    private val dma = Dma(ppu, ram)

    private val cpu = Cpu(CpuBus(
            ppu,
            Apu(),
            ram,
            ProgramRom(rom.program),
            dma,
            Pad(keyEvent)
    ), interrupts).apply {
        reset()
    }

    fun start() {
        val start = System.currentTimeMillis()
        while (true) {
            var cycle = 0
            if (dma.isProcessing) {
                dma.run()
                cycle = 514
            }
            cycle += cpu.run()
            if (ppu.run(cycle * 3)) {
                break
            }
        }
        val end = System.currentTimeMillis()
        val sleepTime = 16 /* 60 fps */ - (end - start)
        if (sleepTime > 0) {
            Thread.sleep(sleepTime)
        }
        start()
    }
}