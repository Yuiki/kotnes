import javafx.scene.canvas.GraphicsContext

class JavaFXCanvas(
        private val g: GraphicsContext
) : Canvas {
    init {
        g.lineWidth = 1.0
    }

    override fun drawDot(x: Int, y: Int, color: Color) {
        g.pixelWriter.setColor(x, y, javafx.scene.paint.Color.rgb(color.r, color.g, color.b))
    }
}