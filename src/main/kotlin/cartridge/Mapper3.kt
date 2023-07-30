package cartridge

class Mapper3(romSize: Int) : Mapper(), AddressSelector by DefaultAddressSelector(romSize)
