import ext.drawDot
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PngCanvas : Canvas {
    private var img: BufferedImage = BufferedImage(256, 224, BufferedImage.TYPE_3BYTE_BGR)

    var i = 0

    override fun drawDot(x: Int, y: Int, r: Int, g: Int, b: Int) {
        val graphics = img.graphics
        graphics.color = Color(r, g, b)
        graphics.drawDot(x, y)
    }

    override fun rendered() {
        ImageIO.write(img, "png", File("img/" + i++.toString() + ".png"))
        img = BufferedImage(256, 224, BufferedImage.TYPE_3BYTE_BGR)
    }
}