import util.pairs
import util.twoDim

class Ppu(
        private val bus: PpuBus
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

    class RenderingData(
            val background: List<Tile>,
            val palette: IntArray
    )

    fun run(cycle: Int): RenderingData? {
        this.cycle += cycle

        if (this.cycle >= 341) {
            this.cycle -= 341
            line++

            if (line <= 240 && line % 8 == 0) {
                buildBackground()
            }

            if (line == 262) {
                line = 0
                return RenderingData(background, palette.data)
            }
        }

        return null
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
            if (addr >= 0x3F00) return vRam.read(addr)
            vRam.read(addr)
        } else {
            bus.read(ppuAddr)
        }
        ppuAddr++
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
            ppuAddr += data shl 8
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
    }

    companion object {
        const val PPUADDR = 0x06
        const val PPUDATA = 0x07
    }
}