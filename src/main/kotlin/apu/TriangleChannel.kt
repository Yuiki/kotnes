package apu

class TriangleChannel(private val lengthCounter: LengthCounter) {
    val lengthCounterValue: Int get() = lengthCounter.value

    var output: UByte = 0u
        private set

    private var counterReloadValue: UByte = 0u
    private var controlFlag = false
    private var timerPeriod = 0u

    private var timer = Timer(onRing = ::onTimerRing)
    private var sequenceIndex: UByte = 0u

    private var reloadLinearCounter = false
    private var linearCounter = 0

    fun write(addr: Int, data: UByte) {
        when (addr) {
            0x8 -> {
                counterReloadValue = data.extract(0..6)

                controlFlag = data.isSetUByte(7u)
                lengthCounter.isHalt = controlFlag
            }

            // 0x9 is unused

            0xA -> {
                timerPeriod = timerPeriod.update(data, 0..7)
            }

            0xB -> {
                timerPeriod = timerPeriod.update(data.extract(0..2), 8..10)

                reloadLinearCounter = true
                lengthCounter.reset(value = data.extract(3..7).toUInt())
            }
        }
    }

    fun disable() {
        lengthCounter.disable()
    }

    fun tickTimer() {
        timer.tick()
    }

    fun tickLinearCounter() {
        if (reloadLinearCounter) {
            linearCounter = counterReloadValue.toInt()
        } else if (linearCounter > 0) {
            linearCounter--
        }

        if (!controlFlag) {
            reloadLinearCounter = false
        }
    }

    fun tickLengthCounter() {
        lengthCounter.tick()
    }

    private fun onTimerRing(): UInt {
        if (linearCounter > 0 && lengthCounter.value > 0) {
            output = runSequencer()
        }
        return timerPeriod
    }

    private fun runSequencer(): UByte {
        val index = (sequenceIndex and 0x1Fu).also { sequenceIndex++ }
        return if (index < 0x10u) index xor 0xFu else index and 0xFu
    }
}
