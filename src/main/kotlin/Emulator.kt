class Emulator(
        rom: Rom
) {
    private val renderer = Renderer()

    private val ppu = Ppu(PpuBus(Ram(0x4000).apply {
        rom.character.forEachIndexed { idx, data -> write(idx, data.toInt()) }
    }))

    private val cpu = Cpu(CpuBus(
            ppu,
            Apu(),
            Ram(0x2048),
            ProgramRom(rom.program),
            Dma(),
            Pad()
    )).apply {
        reset()
    }

    fun start() {
        while (true) {
            val cycle = cpu.run()
            val renderingData = ppu.run(cycle * 3)
            if (renderingData != null) {
                renderer.render(renderingData)
            }
        }
    }
}