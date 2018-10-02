import javafx.application.Platform
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color

class JavaFXCanvas(
        private val graphics: GraphicsContext
) : Canvas {
    override fun bulkDrawDot(data: List<Canvas.RenderingData>) {
        Platform.runLater {
            super.bulkDrawDot(data)
        }
    }

    override fun rendered() {}

    override fun drawDot(x: Int, y: Int, r: Int, g: Int, b: Int) {
        val c = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        val pw = graphics.pixelWriter
        pw.setArgb(x, y, c)
    }
}