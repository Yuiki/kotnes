package apu

import cpu.Cpu

class FrameCounter(
    private val onQuarterFrame: () -> Any,
    private val onHalfFrame: () -> Any,
) {

    private var value = 0

    private var register: UByte = 0u

    private var accumulativeCycles = 0

    fun tick() {
        if (++accumulativeCycles < STEP_CYCLES) return
        accumulativeCycles = 0

        val mode = register.isSetUByte(7u)
        if (mode) {
            runFiveStepSequence()
        } else {
            runFourStepSequence()
        }
    }

    fun updateRegister(data: UByte) {
        register = data
    }

    private fun runFourStepSequence() {
        onQuarterFrame()

        when (value) {
            1, 3 -> onHalfFrame()
        }

        if (value == 3) {
            value = 0
        } else {
            value++
        }
    }

    private fun runFiveStepSequence() {
        when (value) {
            0, 1, 2, 4 -> onQuarterFrame()
        }
        when (value) {
            1, 4 -> onHalfFrame()
        }

        if (value == 4) {
            value = 0
        } else {
            value++
        }
    }

    companion object {
        private const val STEP_CYCLES = Cpu.CPU_HZ / Apu.APU_HZ
    }
}
