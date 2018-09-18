import util.pairs
import util.twoDim

class Ppu(
        private val bus: PpuBus,
        private val canvas: Canvas
) {
    private val vRam = Ram(0x2000)
    private val palette = PaletteRam()

    private var cycle = 0
    private var line = 0
    private val background = mutableListOf<Tile>()

    private var ppuAddr = 0
    private var isLowerPpuAddr = false
    private val isVRamAddr get() = ppuAddr >= 0x2000
    private var vRamReadBuf = 0

    class Tile(
            val sprite: List<IntArray>,
            val paletteId: Int
    )


    fun run(cycle: Int) {
        this.cycle += cycle

        if (this.cycle >= 341) {
            this.cycle -= 341
            line++

            if (line <= 240 && line % 8 == 0) {
                buildBackground()
            }

            if (line == 262) {
                line = 0
                render()
            }
        }
    }

    private fun render() {
        background.forEachIndexed { idx, tile ->
            val tileX = (idx % 32) * 8
            val tileY = (idx / 32) * 8

            pairs((0 until 8), (0 until 8)).forEach {
                val (i, j) = it
                val paletteIdx = tile.paletteId * 4 + tile.sprite[i][j]
                val colorId = palette.data[paletteIdx]
                val color = COLORS[colorId]
                val x = tileX + j
                val y = tileY + i
                canvas.drawDot(x, y, Color(color[0], color[1], color[2]))
            }
        }
    }

    private fun buildBackground() {
        val tileY = line / 8
        for (tileX in 0 until 32) {
            background += buildTile(tileX, tileY)
        }
    }

    private fun buildTile(tileX: Int, tileY: Int): Tile {
        val tileNumber = tileY * 32 + tileX
        val spriteId = vRam.read(tileNumber)
        val sprite = buildSprite(spriteId)
        val attr = getAttribute(tileX, tileY)
        val blockId = getBlockId(tileX, tileY)
        val paletteId = attr shr (blockId * 2) and 0x03
        return Tile(sprite, paletteId)
    }

    private fun buildSprite(id: Int) =
            twoDim(8, 8).apply {
                pairs((0..15), (0..7)).forEach {
                    val (i, j) = it
                    val ram = bus.read(id * 16 + i)
                    if ((ram and (0x80 shr j)) != 0) {
                        this[i % 8][j] += 0x01 shl (i / 8)
                    }
                }
            }

    private fun getAttribute(tileX: Int, tileY: Int): Int {
        val addr = tileX / 4 + (tileY / 4) * 8 + 0x03C0
        return vRam.read(addr)
    }

    private fun getBlockId(tileX: Int, tileY: Int) =
            (tileX % 4) / 2 + ((tileY % 4) / 2) * 2

    fun read(addr: Int) =
            when (addr) {
                PPUDATA -> readVRam()
                else -> 0
            }

    private fun readVRam(): Int {
        val buf = vRamReadBuf
        vRamReadBuf = if (ppuAddr >= 0x2000) {
            val addr = calcVRamAddr()
            ppuAddr++
            if (addr >= 0x3F00) return vRam.read(addr)
            vRam.read(addr)
        } else {
            bus.read(ppuAddr)
            ppuAddr++
        }
        return buf
    }

    private fun calcVRamAddr()
            = ppuAddr - if (ppuAddr in 0x3000 until 0x3f00) 0x3000 else 0x2000

    fun write(addr: Int, data: Int) {
        when (addr) {
            PPUADDR -> writePpuAddr(data)
            PPUDATA -> writePpuData(data)
        }
    }

    private fun writePpuAddr(data: Int) {
        if (isLowerPpuAddr) {
            ppuAddr += data
            isLowerPpuAddr = false
        } else {
            ppuAddr = data shl 8
            isLowerPpuAddr = true
        }
    }

    private fun writePpuData(data: Int) {
        if (isVRamAddr) {
            if (ppuAddr in 0x3F00 until 0x4000) {
                palette.write(ppuAddr, data)
            } else {
                vRam.write(calcVRamAddr(), data)
            }
        } else {
            bus.write(ppuAddr, data)
        }
        ppuAddr++
    }

    companion object {
        const val PPUADDR = 0x06
        const val PPUDATA = 0x07

        private val COLORS = arrayOf(
                arrayOf(0x80, 0x80, 0x80), arrayOf(0x00, 0x3D, 0xA6), arrayOf(0x00, 0x12, 0xB0), arrayOf(0x44, 0x00, 0x96),
                arrayOf(0xA1, 0x00, 0x5E), arrayOf(0xC7, 0x00, 0x28), arrayOf(0xBA, 0x06, 0x00), arrayOf(0x8C, 0x17, 0x00),
                arrayOf(0x5C, 0x2F, 0x00), arrayOf(0x10, 0x45, 0x00), arrayOf(0x05, 0x4A, 0x00), arrayOf(0x00, 0x47, 0x2E),
                arrayOf(0x00, 0x41, 0x66), arrayOf(0x00, 0x00, 0x00), arrayOf(0x05, 0x05, 0x05), arrayOf(0x05, 0x05, 0x05),
                arrayOf(0xC7, 0xC7, 0xC7), arrayOf(0x00, 0x77, 0xFF), arrayOf(0x21, 0x55, 0xFF), arrayOf(0x82, 0x37, 0xFA),
                arrayOf(0xEB, 0x2F, 0xB5), arrayOf(0xFF, 0x29, 0x50), arrayOf(0xFF, 0x22, 0x00), arrayOf(0xD6, 0x32, 0x00),
                arrayOf(0xC4, 0x62, 0x00), arrayOf(0x35, 0x80, 0x00), arrayOf(0x05, 0x8F, 0x00), arrayOf(0x00, 0x8A, 0x55),
                arrayOf(0x00, 0x99, 0xCC), arrayOf(0x21, 0x21, 0x21), arrayOf(0x09, 0x09, 0x09), arrayOf(0x09, 0x09, 0x09),
                arrayOf(0xFF, 0xFF, 0xFF), arrayOf(0x0F, 0xD7, 0xFF), arrayOf(0x69, 0xA2, 0xFF), arrayOf(0xD4, 0x80, 0xFF),
                arrayOf(0xFF, 0x45, 0xF3), arrayOf(0xFF, 0x61, 0x8B), arrayOf(0xFF, 0x88, 0x33), arrayOf(0xFF, 0x9C, 0x12),
                arrayOf(0xFA, 0xBC, 0x20), arrayOf(0x9F, 0xE3, 0x0E), arrayOf(0x2B, 0xF0, 0x35), arrayOf(0x0C, 0xF0, 0xA4),
                arrayOf(0x05, 0xFB, 0xFF), arrayOf(0x5E, 0x5E, 0x5E), arrayOf(0x0D, 0x0D, 0x0D), arrayOf(0x0D, 0x0D, 0x0D),
                arrayOf(0xFF, 0xFF, 0xFF), arrayOf(0xA6, 0xFC, 0xFF), arrayOf(0xB3, 0xEC, 0xFF), arrayOf(0xDA, 0xAB, 0xEB),
                arrayOf(0xFF, 0xA8, 0xF9), arrayOf(0xFF, 0xAB, 0xB3), arrayOf(0xFF, 0xD2, 0xB0), arrayOf(0xFF, 0xEF, 0xA6),
                arrayOf(0xFF, 0xF7, 0x9C), arrayOf(0xD7, 0xE8, 0x95), arrayOf(0xA6, 0xED, 0xAF), arrayOf(0xA2, 0xF2, 0xDA),
                arrayOf(0x99, 0xFF, 0xFC), arrayOf(0xDD, 0xDD, 0xDD), arrayOf(0x11, 0x11, 0x11), arrayOf(0x11, 0x11, 0x11)
        )
    }
}