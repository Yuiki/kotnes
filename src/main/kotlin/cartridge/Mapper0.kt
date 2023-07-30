package cartridge

class Mapper0(private val romSize: Int) : Mapper(), AddressSelector by DefaultAddressSelector(romSize)
