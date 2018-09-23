class Dma(
        private val ppu: Ppu,
        private val ram: Ram
) {
    var isProcessing = false
    var ramAddr = 0

    fun run() {
        if (!isProcessing) return
        for (i in 0 until 0x100) {
            ppu.transferSprite(i, ram.read(ramAddr + i))
        }
        isProcessing = false
    }

    fun write(data: Int) {
        ramAddr = data shl 8
        isProcessing = true
    }
}