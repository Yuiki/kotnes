package ppu

interface Canvas {
    fun bulkDrawDots(data: List<RenderingData>) {
        data.forEach { drawDot(it.x, it.y, it.r, it.g, it.b) }
    }

    fun drawDot(x: Int, y: Int, r: Int, g: Int, b: Int)
    fun rendered()

    class RenderingData(
            val x: Int,
            val y: Int,
            val r: Int,
            val g: Int,
            val b: Int
    )
}