package ext

import java.io.FileInputStream
import java.math.BigInteger

fun FileInputStream.read(len: Int) =
    ByteArray(len).apply { read(this) }

fun FileInputStream.readAsHex(len: Int) =
    read(len).toHex()

fun FileInputStream.readAsInt(len: Int) =
    BigInteger(read(len).toHex(), 16).toInt()
