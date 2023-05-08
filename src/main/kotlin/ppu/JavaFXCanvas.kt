package ppu

import javafx.application.Platform
import javafx.scene.canvas.GraphicsContext

class JavaFXCanvas(
    graphics: GraphicsContext,
) : Canvas {
    private val pw = graphics.pixelWriter

    override fun bulkDrawDots(data: List<Canvas.RenderingData>) {
        Platform.runLater {
            super.bulkDrawDots(data)
        }
    }

    override fun rendered() {}

    override fun drawDot(x: Int, y: Int, r: Int, g: Int, b: Int) {
        val c = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        val actualX = x * 2
        val actualY = y * 2
        pw.setArgb(actualX, actualY, c)
        pw.setArgb(actualX + 1, actualY, c)
        pw.setArgb(actualX, actualY + 1, c)
        pw.setArgb(actualX + 1, actualY + 1, c)
    }
}
