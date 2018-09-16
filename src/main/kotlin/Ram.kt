class Ram {
    private val ram = IntArray(0x800)

    fun read(addr: Int) = ram[addr]

    fun write(addr: Int, data: Int) {
        ram[addr] = data
    }
}