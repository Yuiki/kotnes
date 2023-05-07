package apu

// determine volume
class EnvelopeGenerator {
    var output: UByte = 0u
        private set

    private var register: UByte = 0u
    private val volume get() = register.extract(range = 0..3)
    private val useConstantVolume get() = register.isSetUByte(4u) // otherwise, use decay
    private val isLoop get() = register.isSetUByte(5u)

    private var startFlag = false
    private var dividerCounter: UByte = 0u
    private var decayLevelCounter: UByte = 0u

    fun tick() {
        if (startFlag) {
            startFlag = false
            resetDecayLevelCounter()
            reloadDivider()
        } else {
            if (dividerCounter > 0u) {
                dividerCounter--
            } else {
                reloadDivider()
                tickDecayLevelCounter()
            }
        }

        output = if (useConstantVolume) volume else decayLevelCounter
    }

    fun setStartFlag() {
        startFlag = true
    }

    fun updateRegister(register: UByte) {
        this.register = register
    }

    private fun reloadDivider() {
        dividerCounter = volume
    }

    private fun tickDecayLevelCounter() {
        if (decayLevelCounter > 0u) {
            decayLevelCounter--
        } else if (isLoop) {
            resetDecayLevelCounter()
        }
    }

    private fun resetDecayLevelCounter() {
        decayLevelCounter = 15u
    }
}
