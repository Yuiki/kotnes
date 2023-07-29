package exception

import ext.toHex

class UnknownOpcodeException(val opKey: Int) : RuntimeException("0x${opKey.toByte().toHex()} is unknown")
