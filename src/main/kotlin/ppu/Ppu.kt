package ppu

import interrupts.Interrupts
import ram.Ram
import util.pairs
import util.twoDim

class Ppu(
    private val chrRam: Ram,
    private val canvas: Canvas,
    private val interrupts: Interrupts,
    private val isHorizontalMirror: Boolean,
) {
    private val vRam = PpuVRam()
    private val palette = PaletteRam()

    private var cycle = 0
    private var line = 0
    private val background = mutableListOf<Tile>()

    private var ppuAddr = 0
    private var isLowerPpuAddr = false
    private val isVRamAddr get() = ppuAddr >= 0x2000
    private var vRamReadBuf = 0

    private val spriteRam = Ram(0x100)
    private var spriteRamAddr = 0

    private val registers = IntArray(8)
    private val sprites = arrayOfNulls<SpriteWithAttributes>(0x1000)

    private val isIrqEnabled get() = registers[0] and 0x80 != 0

    private var scrollX = 0
    private var scrollY = 0
    private var nameTableId = 0
    private val scrollTileX get() = (scrollX + (nameTableId % 2) * 256) / 8
    private val scrollTileY get() = (scrollY + (nameTableId / 2) * 240) / 8
    private val tileY get() = line / 8 + scrollTileY

    private val vRamOffset get() = if (registers[0x00] and 0x04 != 0) 32 else 1

    private val isBackgroundEnabled get() = registers[0x01] and 0x08 != 0
    private val isSpriteEnabled get() = registers[0x01] and 0x10 != 0

    private var isHorizontalScroll = true

    private val isSprite8x8 get() = registers[0x00] and 0x20 == 0

    class Tile(
        val sprite: List<IntArray>,
        val paletteId: Int,
        val scrollX: Int,
        val scrollY: Int,
    )

    class SpriteWithAttributes(
        val sprites: List<IntArray>,
        val x: Int,
        val y: Int,
        val attrs: Int,
    )

    private fun hasSpriteHit(): Boolean {
        val y = spriteRam.read(0)
        return y == line && isBackgroundEnabled && isSpriteEnabled
    }

    private fun setSpriteHit() {
        registers[0x02] = registers[0x02] or 0x40
    }

    private fun clearSpriteHit() {
        registers[0x02] = registers[0x02] and 0xBF
    }

    private fun clearVBlank() {
        registers[0x02] = registers[0x02] and 0x7F
    }

    fun run(cycle: Int): Boolean {
        this.cycle += cycle

        if (line == 0) {
            buildSprites()
        }

        if (this.cycle >= 341) {
            this.cycle -= 341
            val currentLine = line++

            if (hasSpriteHit()) {
                setSpriteHit()
            }

            if (currentLine <= 239 && line % 8 == 0 && scrollY <= 240) {
                buildBackground()
            }

            if (currentLine == 240) { // Post-render scanline
                // idle
            }

            if (currentLine == 241) {
                registers[2] = registers[2] or 0x80
                if (isIrqEnabled) {
                    interrupts.isNmiAsserted = true
                }
            }

            if (currentLine == 261) { // Pre-render scanline
                clearVBlank()
                clearSpriteHit()
                line = 0
                render()
                background.clear()
                interrupts.isNmiAsserted = false
                return true
            }
        }

        return false
    }

    private fun render() {
        val renderingData = mutableListOf<Canvas.RenderingData>()
        val colorData = palette.data
        if (isBackgroundEnabled) {
            background.forEachIndexed { idx, tile ->
                val offsetX = tile.scrollX % 8
                val offsetY = tile.scrollY % 8
                val tileX = (idx % 33) * 8
                val tileY = (idx / 33) * 8

                pairs((0 until 8), (0 until 8)).forEach {
                    val (i, j) = it
                    val paletteIdx = tile.paletteId * 4 + tile.sprite[i][j]
                    val colorId = colorData[paletteIdx]
                    val color = COLORS[colorId]
                    val x = tileX + j - offsetX
                    val y = tileY + i - offsetY
                    renderingData += Canvas.RenderingData(x, y, color[0], color[1], color[2])
                }
            }
        }

        if (isSpriteEnabled) {
            sprites.forEach { sprite ->
                if (sprite == null) return@forEach
                val isVerticalReverse = sprite.attrs and 0x80 != 0
                val isHorizontalReverse = sprite.attrs and 0x40 != 0
                val isLowPriority = sprite.attrs and 0x20 != 0
                val paletteId = sprite.attrs and 0x03
                pairs((0 until sprite.sprites.size), (0 until 8)).forEach {
                    val (i, j) = it
                    val x = sprite.x + if (isHorizontalReverse) 7 - j else j
                    val y = sprite.y + if (isVerticalReverse) sprite.sprites.size - 1 - i else i
                    if (!(isLowPriority && shouldPixelHide(x, y)) && sprite.sprites[i][j] != 0) {
                        val colorId = colorData[paletteId * 4 + sprite.sprites[i][j] + 0x10]
                        val color = COLORS[colorId]
                        renderingData += Canvas.RenderingData(x, y, color[0], color[1], color[2])
                    }
                }
            }
        }

        canvas.bulkDrawDots(renderingData)
    }

    private fun buildBackground() {
        val clampedTileY = tileY % 30
        val tableIdOffset = if ((tileY / 30) % 2 != 0) 2 else 0
        for (x in 0..32) {
            val tileX = x + scrollTileX
            val clampedTileX = tileX % 32
            val nameTableId = (tileX / 32) % 2 + tableIdOffset
            val offset = nameTableId * 0x400
            background += buildTile(clampedTileX, clampedTileY, offset)
        }
    }

    private fun buildTile(tileX: Int, tileY: Int, offset: Int): Tile {
        val tileNumber = tileY * 32 + tileX
        val spriteAddr = calcSpriteAddr(tileNumber + offset)
        val spriteId = vRam.read(spriteAddr)
        val bgTableOffset = if (registers[0] and 0x10 != 0) 0x1000 else 0x0000
        val sprite = buildSprite(spriteId, bgTableOffset)
        val attr = getAttribute(tileX, tileY, offset)
        val blockId = getBlockId(tileX, tileY)
        val paletteId = attr shr (blockId * 2) and 0x03
        return Tile(sprite, paletteId, scrollX, scrollY)
    }

    private fun calcSpriteAddr(addr: Int): Int {
        if (!isHorizontalMirror) return addr
        if (addr in 0x400 until 0x800 || addr >= 0x0C00) {
            return addr - 0x400
        }
        return addr
    }

    private fun buildSprites() {
        for (i in 0 until 0x100 step 4) {
            val y = spriteRam.read(i) - 8
            if (y < 0) return
            var spriteId = spriteRam.read(i + 1)
            val attr = spriteRam.read(i + 2)
            val x = spriteRam.read(i + 3)
            var offset: Int
            if (isSprite8x8) {
                offset = if (registers[0] and 0x08 != 0) 0x1000 else 0x0000
            } else {
                offset = 0x1000 * (spriteId and 0x01)
                spriteId = spriteId and 0xFE
            }
            val sprite = buildSprite(spriteId, offset)
            sprites[i / 4] = SpriteWithAttributes(sprite, x, y, attr)
        }
    }

    private fun buildSprite(id: Int, offset: Int = 0): List<IntArray> {
        val height = if (isSprite8x8) 1 else 2
        return twoDim(8, 8 * height).apply {
            (0 until height).forEach { idx ->
                pairs((0..15), (0..7)).forEach {
                    val (i, j) = it
                    val ram = chrRam.read((id + idx) * 16 + i + offset)
                    if ((ram and (0x80 shr j)) != 0) {
                        this[idx * 8 + i % 8][j] += 0x01 shl (i / 8)
                    }
                }
            }
        }
    }

    private fun shouldPixelHide(x: Int, y: Int): Boolean {
        val tileX = x / 8
        val tileY = y / 8
        val bgIdx = tileY * 33 + tileX
        if (bgIdx >= background.size) return true
        val sprite = background[bgIdx].sprite
        return sprite[y % 8][x % 8] % 4 != 0
    }

    private fun getAttribute(tileX: Int, tileY: Int, offset: Int): Int {
        val addr = tileX / 4 + (tileY / 4) * 8 + 0x03C0 + offset
        val spriteAddr = calcSpriteAddr(addr)
        return vRam.read(spriteAddr)
    }

    private fun getBlockId(tileX: Int, tileY: Int) =
        (tileX % 4) / 2 + ((tileY % 4) / 2) * 2

    fun read(addr: Int) =
        when (addr) {
            PPUSTATUS -> {
                val data = registers[0x02]
                isHorizontalScroll = true
                clearVBlank()
                data
            }

            OAMDATA -> {
                spriteRam.read(spriteRamAddr)
            }

            PPUDATA -> readVRam()
            else -> 0
        }

    private fun readVRam(): Int {
        val buf = vRamReadBuf
        if (ppuAddr >= 0x2000) { // VRAM
            val addr = calcVRamAddr()
            ppuAddr += vRamOffset
            vRamReadBuf = vRam.read(addr)
            if (addr >= 0x1F00) {
                return palette.read(addr)
            }
        } else {
            vRamReadBuf = chrRam.read(ppuAddr)
            ppuAddr += vRamOffset
        }
        return buf
    }

    private fun calcVRamAddr() = ppuAddr - if (ppuAddr in 0x3000 until 0x3f00) 0x3000 else 0x2000

    fun write(addr: Int, data: Int) {
        when (addr) {
            OAMADDR -> spriteRamAddr = data
            OAMDATA -> {
                spriteRam.write(spriteRamAddr, data)
                spriteRamAddr++
            }

            PPUSCROLL -> writeScrollData(data)
            PPUADDR -> writePpuAddr(data)
            PPUDATA -> writePpuData(data)
            else -> {
                if (addr == 0) {
                    nameTableId = data and 0x03
                }
                this.registers[addr] = data
            }
        }
    }

    private fun writePpuAddr(data: Int) {
        if (isLowerPpuAddr) {
            ppuAddr += data
            isLowerPpuAddr = false
            nameTableId = (ppuAddr and 0b110000000000) shr 10
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
            chrRam.write(ppuAddr, data)
        }
        ppuAddr += vRamOffset
    }

    private fun writeScrollData(data: Int) {
        if (isHorizontalScroll) {
            scrollX = data and 0xFF
        } else {
            scrollY = data and 0xFF
        }
        isHorizontalScroll = !isHorizontalScroll
    }

    fun transferSprite(idx: Int, data: Int) {
        val addr = idx + spriteRamAddr
        spriteRam.write(addr % 0x100, data)
    }

    companion object {
        const val PPUSTATUS = 0x02
        const val OAMADDR = 0x03
        const val OAMDATA = 0x04
        const val PPUSCROLL = 0x05
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
