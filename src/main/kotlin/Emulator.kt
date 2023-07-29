import apu.*
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
import kotlin.math.round

class Emulator(
    cartridge: Cartridge,
    canvas: Canvas,
    keyEvent: KeyEvent,
) {
    private val interrupts = Interrupts()
    private val chrRam = Ram(cartridge.character.size).apply {
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

    private val apu = Apu(
        pulse1 = PulseChannel(
            envelopeGenerator = EnvelopeGenerator(),
            lengthCounter = LengthCounter(),
            isChannelOne = true,
        ),
        pulse2 = PulseChannel(
            envelopeGenerator = EnvelopeGenerator(),
            lengthCounter = LengthCounter(),
            isChannelOne = false,
        ),
        triangle = TriangleChannel(
            lengthCounter = LengthCounter(),
        ),
        noise = NoiseChannel(
            envelopeGenerator = EnvelopeGenerator(),
            lengthCounter = LengthCounter(),
        ),
        speaker = Speaker(),
    )
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

    private var sleepMargin = 0L

    fun start() {
        while (true) {
            val frameStartNs = System.nanoTime()
            stepFrame()
            val frameEndNs = System.nanoTime()

            val sleepTimeNs = (FRAME_NS - (frameEndNs - frameStartNs)) + sleepMargin
            val sleepTimeMs = sleepTimeNs / 1000_000
            val sleepStartNs = System.nanoTime()
            if (sleepTimeMs > 0) {
                Thread.sleep(sleepTimeMs)
            }
            val sleepEnd = System.nanoTime()
            sleepMargin = sleepTimeNs - (sleepEnd - sleepStartNs)
        }
    }

    private fun stepFrame() {
        while (true) {
            var cpuCycle = 0
            if (dma.isProcessing) {
                dma.run()
                cpuCycle = 514
            }
            cpuCycle += cpu.run()
            apu.run(cpuCycle)
            if (ppu.run(cpuCycle * 3)) {
                break
            }
        }
        apu.flush()
    }

    companion object {
        private val FRAME_NS = round(1.0 / 60 * 1000_000_000).toInt() // 60fps
    }
}
