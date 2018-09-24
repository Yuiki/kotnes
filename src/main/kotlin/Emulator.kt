class Emulator(
        rom: Rom,
        canvas: Canvas,
        keyEvent: KeyEvent
) {
    private val interrupts = Interrupts()

    private val ppu = Ppu(PpuBus(Ram(0x4000).apply {
        rom.character.forEachIndexed { idx, data -> write(idx, data.toInt()) }
    }), canvas = canvas, interrupts = interrupts)

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
        while (true) {
            var cycle = 0
            if (dma.isProcessing) {
                dma.run()
                cycle = 514
            }
            cycle += cpu.run()
            ppu.run(cycle * 3)
        }
    }
}