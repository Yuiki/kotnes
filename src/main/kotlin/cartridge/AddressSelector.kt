package cartridge

interface AddressSelector {
    fun selectActualRomAddress(addr: Int): Int
}

class DefaultAddressSelector(private val romSize: Int) : AddressSelector {
    override fun selectActualRomAddress(addr: Int): Int {
        val offset = if (addr >= 0x4000 && romSize <= 0x4000) 0x4000 else 0
        return addr + offset
    }
}

