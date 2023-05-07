package apu

class Timer(
    private val onRing: () -> UInt, // returns new counter
) {
    private var counter = 0u

    fun tick() {
        if (counter > 0u) {
            counter--
            return
        }

        counter = onRing()
    }
}
