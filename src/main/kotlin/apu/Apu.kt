package apu

import cpu.Cpu
import ext.toInt

class Apu(
    private val pulse1: PulseChannel,
    private val pulse2: PulseChannel,
    private val triangle: TriangleChannel,
    private val noise: NoiseChannel,
    private val speaker: Speaker,
) {

    private val frameCounter = FrameCounter(
        onHalfFrame = ::onHalfFrame,
        onQuarterFrame = ::onQuarterFrame
    )

    private var accumulativeCpuCyclesForMixer = 0
    private var accumulativeCpuCyclesForApuCycle = 0

    fun read(addr: Int): Int {
        @Suppress("UNREACHABLE_CODE") // false positive
        return when (addr) {
            0x15 -> {
                val p1 = (pulse1.lengthCounterValue > 0).toInt()
                val p2 = (pulse2.lengthCounterValue > 0).toInt()
                val triangle = (triangle.lengthCounterValue > 0).toInt()
                val noise = (noise.lengthCounterValue > 0).toInt()
                // TODO: support DMC and frame interrupt and DMC active
                return p1 + (p2 shl 1) + (triangle shl 2) + (noise shl 3)
            }

            else -> error("Unsupported read (addr: $addr)")
        }
    }

    fun write(addr: Int, data: UByte) {
        when (addr) {
            in 0x0..0x3 -> {
                pulse1.write(addr = addr, data = data)
            }

            in 0x4..0x07 -> {
                pulse2.write(addr = addr - 4, data = data)
            }

            in 0x8..0xB -> {
                triangle.write(addr = addr, data = data)
            }

            in 0xC..0xF -> {
                noise.write(addr = addr, data = data)
            }

            0x15 -> {
                if (!data.isSetUByte(0u)) {
                    pulse1.disable()
                }
                if (!data.isSetUByte(1u)) {
                    pulse2.disable()
                }
                if (!data.isSetUByte(2u)) {
                    triangle.disable()
                }
                if (!data.isSetUByte(3u)) {
                    noise.disable()
                }

                // TODO: support DMC
            }

            0x17 -> {
                frameCounter.updateRegister(data)
            }
        }
    }

    fun run(cycles: Int) {
        for (i in 0 until cycles) {
            onCpuCycle()

            if (++accumulativeCpuCyclesForApuCycle % 2 == 0) {
                onApuCycle()
                accumulativeCpuCyclesForApuCycle = 0
            }

            if (++accumulativeCpuCyclesForMixer == MIXER_STEP_CYCLES) {
                tickMixer()
                accumulativeCpuCyclesForMixer = 0
            }
        }
    }

    fun flush() {
        speaker.flush()
    }

    private fun onCpuCycle() {
        triangle.tickTimer()
        noise.tickTimer()

        frameCounter.tick()
    }

    private fun onApuCycle() {
        pulse1.tickTimer()
        pulse2.tickTimer()
    }

    private fun onQuarterFrame() {
        pulse1.tickEnvelope()
        pulse2.tickEnvelope()
        noise.tickEnvelope()

        triangle.tickLinearCounter()
    }

    private fun onHalfFrame() {
        pulse1.tickLengthCounter()
        pulse2.tickLengthCounter()
        triangle.tickLengthCounter()
        noise.tickLengthCounter()

        pulse1.tickSweepUnit()
        pulse2.tickSweepUnit()
    }

    private fun tickMixer() {
        // Linear Approximation
        // see: https://www.nesdev.org/wiki/APU_Mixer
        val mixedPulse = (pulse1.output + pulse2.output).toByte() * 0.00752
        val mixed = mixedPulse +
                triangle.output.toByte() * 0.00851 +
                noise.output.toByte() * 0.00494
        // TODO: low & high -pass filter
        speaker.write(mixed)
    }

    companion object {
        const val APU_HZ = 240

        private const val MIXER_STEP_CYCLES = Cpu.CPU_HZ / Speaker.SAMPLE_RATE
    }
}
