package apu

class SweepUnit(
    private val onTimerPeriodUpdate: (period: UInt) -> Unit,
    private val useOneComplement: Boolean, // or two's complement
) {
    var isMute: Boolean = false

    private var counter = 0u
    private var register: UByte = 0u
    private var shouldReload = false

    fun tick(timerPeriod: UInt) {
        if (counter == 0u) {
            updateTimerPeriod(timerPeriod)
        }

        if (counter == 0u || shouldReload) {
            counter = register.extract(4..6).toUInt()
            shouldReload = false
        } else {
            counter--
        }
    }

    fun updateRegister(data: UByte) {
        register = data

        shouldReload = true
    }

    private fun updateTimerPeriod(timerPeriod: UInt) {
        val shiftCount = register.extract(0..2)
        val delta = (timerPeriod shr shiftCount.toInt()).toInt().let {
            val isNegate = register.isSetUByte(3u)
            if (isNegate) -it.let { c -> if (useOneComplement) c - 1 else c } else it
        }
        val targetPeriod = (timerPeriod.toInt() + delta).toUInt()

        isMute = timerPeriod < 8u || targetPeriod > 0x7FFu

        val isEnabled = register.isSetUByte(7u)
        if (isEnabled && !isMute) {
            onTimerPeriodUpdate(targetPeriod)
        }
    }
}
