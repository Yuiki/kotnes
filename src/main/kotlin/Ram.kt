class Ram(
        size: Int
) {
    private val ram = IntArray(size)

    val size get() = ram.size

    fun read(addr: Int) = ram[addr]

    fun write(addr: Int, data: Int) {
        ram[addr] = data
    }
}