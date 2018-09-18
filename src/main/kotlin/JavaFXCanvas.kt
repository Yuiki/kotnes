import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

class JavaFXCanvas(
        private val graphics: GraphicsContext
) : Canvas {
    override fun drawDot(x: Int, y: Int, r: Int, g: Int, b: Int) {
        graphics.pixelWriter.setColor(x, y, Color.rgb(r, g, b))
    }
}