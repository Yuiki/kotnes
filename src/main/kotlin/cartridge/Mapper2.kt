package cartridge

class Mapper2 : Mapper(), AddressSelector {
    var bank: UByte = 0u

    override fun selectActualRomAddress(addr: Int): Int {
        return addr + bank.toInt() * 0x4000
    }
}
