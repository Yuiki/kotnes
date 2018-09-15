package ext

import java.awt.Graphics

fun Graphics.drawDot(x: Int, y: Int) {
    drawLine(x, y, x, y)
}