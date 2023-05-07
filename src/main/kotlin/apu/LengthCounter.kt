package apu

class LengthCounter {
    var value: Int = 1
        private set

    var isHalt: Boolean = false

    fun tick() {
        if (value == 0 || isHalt) return
        value--
    }

    fun reset(value: UInt) {
        this.value = requireNotNull(LENGTH_TABLE[value])
    }

    fun disable() {
        value = 0
    }

    companion object {
        // see: https://www.nesdev.org/wiki/APU_Length_Counter
        private val LENGTH_TABLE = mapOf(
            0b1_1111u to 30,
            0b1_1101u to 28,
            0b1_1011u to 26,
            0b1_1001u to 24,
            0b1_0111u to 22,
            0b1_0101u to 20,
            0b1_0011u to 18,
            0b1_0001u to 16,
            0b0_1111u to 14,
            0b0_1101u to 12,
            0b0_1011u to 10,
            0b0_1001u to 8,
            0b0_0111u to 6,
            0b0_0101u to 4,
            0b0_0011u to 2,
            0b0_0001u to 254,
            0b1_1110u to 32,
            0b1_1100u to 16,
            0b1_1010u to 72,
            0b1_1000u to 192,
            0b1_0110u to 96,
            0b1_0100u to 48,
            0b1_0010u to 24,
            0b1_0000u to 12,
            0b0_1110u to 26,
            0b0_1100u to 14,
            0b0_1010u to 60,
            0b0_1000u to 28,
            0b0_0110u to 160,
            0b0_0100u to 40,
            0b0_0010u to 20,
            0b0_0000u to 10,
        )
    }
}
