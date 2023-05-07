package apu

class PulseChannel(
    private val envelopeGenerator: EnvelopeGenerator,
    private val lengthCounter: LengthCounter,
    isChannelOne: Boolean, // or ch. 2
) {
    val lengthCounterValue: Int get() = lengthCounter.value

    val output: UByte
        get() {
            val volume = envelopeGenerator.output
            if (
                timerPeriod < 8u ||
                lengthCounter.value == 0 ||
                sweepUnit.isMute
            ) return 0u

            return (sequencerOutput * volume).toUByte()
        }

    private var dutyCycle: UByte = 0u

    private val timer = Timer(onRing = ::onTimerRing)
    private var timerPeriod = 0u

    private var sequenceCounter = 0

    private var sequencerOutput: UByte = 0u

    private val sweepUnit = SweepUnit(
        onTimerPeriodUpdate = { timerPeriod = it },
        useOneComplement = isChannelOne,
    )

    fun write(addr: Int, data: UByte) {
        when (addr) {
            0x0 -> {
                envelopeGenerator.updateRegister(register = data)
                lengthCounter.isHalt = data.isSetUByte(5u)
                dutyCycle = data.extract(6..7)
            }

            0x1 -> {
                sweepUnit.updateRegister(data)
            }

            0x2 -> {
                timerPeriod = timerPeriod.update(data, 0..7)
            }

            0x3 -> {
                timerPeriod = timerPeriod.update(data.extract(0..2), 8..10) // TODO: is needed?

                resetSequenceCounter()
                envelopeGenerator.setStartFlag()
                lengthCounter.reset(data.extract(3..7).toUInt())
            }
        }
    }

    fun disable() {
        lengthCounter.disable()
    }

    fun tickTimer() {
        timer.tick()
    }

    fun tickEnvelope() {
        envelopeGenerator.tick()
    }

    fun tickLengthCounter() {
        lengthCounter.tick()
    }

    fun tickSweepUnit() {
        sweepUnit.tick(timerPeriod = timerPeriod)
    }

    private fun onTimerRing(): UInt {
        sequencerOutput = runSequencer(dutyCycle = dutyCycle)
        return timerPeriod
    }

    // returns 0 or 1
    private fun runSequencer(dutyCycle: UByte): UByte {
        val waveform = WAVEFORMS[dutyCycle.toInt()]
        val output = waveform[sequenceCounter].toUByte()

        if (sequenceCounter < 7) {
            sequenceCounter++
        } else {
            resetSequenceCounter()
        }
        return output
    }

    private fun resetSequenceCounter() {
        sequenceCounter = 0
    }

    companion object {
        private val WAVEFORMS = arrayOf(
            intArrayOf(0, 1, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 1, 1, 0, 0, 0, 0, 0),
            intArrayOf(0, 1, 1, 1, 1, 0, 0, 0),
            intArrayOf(1, 0, 0, 1, 1, 1, 1, 1),
        )
    }
}
