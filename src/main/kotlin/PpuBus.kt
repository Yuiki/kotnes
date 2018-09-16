class PpuBus(
        private val characterRam: Ram
) {
    fun read(addr: Int) =
            characterRam.read(addr)

    fun write(addr: Int, data: Int) {
        characterRam.write(addr, data)
    }
}