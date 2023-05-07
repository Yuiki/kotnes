package apu

class NoiseChannel(
    private val envelopeGenerator: EnvelopeGenerator,
    private val lengthCounter: LengthCounter,
) {
    val lengthCounterValue: Int get() = lengthCounter.value

    val output: UByte
        get() {
            if (lengthCounter.value == 0 || shiftRegister.isSetUInt(0u)) return 0u
            return envelopeGenerator.output
        }

    private var mode = false
    private var periodSetting: UByte = 0u

    private var timer = Timer(onRing = ::onTimerRing)
    private var shiftRegister = 1u

    fun write(addr: Int, data: UByte) {
        when (addr) {
            0xC -> {
                lengthCounter.isHalt = data.isSetUByte(5u)
                envelopeGenerator.updateRegister(register = data)
            }

            // 0xD is unused

            0xE -> {
                periodSetting = data.extract(0..3)
                mode = data.isSetUByte(7u)
            }

            0xF -> {
                val value = data.extract(3..7)
                lengthCounter.reset(value.toUInt())
                envelopeGenerator.setStartFlag()
            }
        }
    }

    fun disable() {
        lengthCounter.disable()
    }

    fun tickLengthCounter() {
        lengthCounter.tick()
    }

    fun tickEnvelope() {
        envelopeGenerator.tick()
    }

    fun tickTimer() {
        timer.tick()
    }

    private fun onTimerRing(): UInt {
        runShiftRegister()
        return PERIOD_TABLE[periodSetting.toInt()].toUInt()
    }

    private fun runShiftRegister() {
        val feedBackZero = shiftRegister.isSetUInt(0u)
        val feedbackOne = (if (mode) shiftRegister.isSetUInt(6u) else shiftRegister.isSetUInt(1u))
        val feedback = feedBackZero xor feedbackOne

        shiftRegister = (shiftRegister shr 1).update(feedback, 14)
    }

    companion object {
        private val PERIOD_TABLE =
            intArrayOf(4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068)
    }
}
