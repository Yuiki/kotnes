import ext.drawDot
import util.pairs
import util.twoDim
import java.awt.Color
import java.awt.image.BufferedImage

class Character(
        private val data: ByteArray
) {
    private val spritesNum = data.size / 16

    fun render(): BufferedImage {
        val sprites = (0 until spritesNum).map { buildSprite(it) }
        return drawSprite(sprites)
    }

    private fun buildSprite(idx: Int) =
            twoDim(8, 8).apply {
                pairs((0..15), (0..7)).forEach {
                    val (i, j) = it
                    if ((data[idx * 16 + i].toInt() and (0x80 shr j)) != 0) {
                        this[i % 8][j] += 0x01 shl (i / 8)
                    }
                }
            }

    private fun drawSprite(sprites: List<List<IntArray>>): BufferedImage {
        val spritesPerRow = WIDTH / 8
        val height = (spritesNum / spritesPerRow + 1) * 8
        return BufferedImage(WIDTH, height, BufferedImage.TYPE_3BYTE_BGR).apply {
            val g = graphics
            sprites.forEachIndexed { idx, sprite ->
                pairs((0..7), (0..7)).forEach {
                    val (i, j) = it
                    g.color = Color(85 * sprite[i][j], 85 * sprite[i][j], 85 * sprite[i][j])
                    val x = j + idx % spritesPerRow * 8
                    val y = i + idx / spritesPerRow * 8
                    g.drawDot(x, y)
                }
            }
        }
    }

    companion object {
        const val WIDTH = 800
    }
}