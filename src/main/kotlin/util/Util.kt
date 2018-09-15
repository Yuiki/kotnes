package util

fun pairs(x: IntRange, y: IntRange) = x.flatMap { i -> y.map { j -> Pair(i, j) } }

fun twoDim(x: Int, y: Int) = (0 until y).map { IntArray(x) }