package ram

class Ram(
        size: Int
) {
    private val data = IntArray(size)

    val size get() = data.size

    fun read(addr: Int) = data[addr]

    fun write(addr: Int, data: Int) {
        this.data[addr] = data
    }
}