package apu

import ext.toInt
import kotlin.math.pow

fun UByte.extract(range: IntRange): UByte {
    val offset = Int.SIZE_BITS - 1 - range.last
    return ((this.toUInt() shl offset) shr (range.first + offset)).toUByte()
}

fun UInt.isSetUInt(digit: UByte): Boolean {
    val mask = 2.0.pow(digit.toInt()).toUInt()
    return this and mask == mask
}

fun UByte.isSetUByte(digit: UByte): Boolean {
    return this.toUInt().isSetUInt(digit)
}

fun UInt.update(data: Boolean, digit: Int): UInt {
    return update(data.toInt(), digit..digit)
}

fun UInt.update(data: UByte, digits: IntRange): UInt {
    return update(data.toInt(), digits)
}

private fun UInt.update(data: Int, range: IntRange): UInt {
    val length = range.last - range.first + 1
    val mask = ((2.0.pow(length).toUInt() - 1u) shl range.first).inv()
    return (this and mask) or (data.toUInt() shl range.first)
}
