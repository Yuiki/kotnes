import ext.toUnsignedInt

class ProgramRom(
        private val program: ByteArray
) {
    val size get() = program.size

    fun read(addr: Int) = program[addr].toUnsignedInt()
}