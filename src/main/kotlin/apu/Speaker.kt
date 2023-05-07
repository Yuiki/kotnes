package apu

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class Speaker {
    private val format = AudioFormat(
        SAMPLE_RATE.toFloat(),
        SAMPLE_SIZE_BITS,
        CHANNELS,
        true,
        false,
    )

    private val line = run {
        val info = DataLine.Info(SourceDataLine::class.java, format)
        AudioSystem.getLine(info) as SourceDataLine
    }

    init {
        line.open(format, BUFFER_SIZE)
        line.start()
    }

    private var buffer = ByteArray(BUFFER_SIZE)
    private var bufferIndex = 0

    fun write(value: Double) {
        if ((line.bufferSize - line.available()) >= DROP_THRESHOLD) return

        // TODO: support DMC
        val volume = (value * VOLUME_LEVEL).toInt().toByte()
        buffer[bufferIndex++] = 0
        buffer[bufferIndex++] = volume
    }

    fun flush() {
        line.write(buffer, 0, bufferIndex)
        bufferIndex = 0
    }

    companion object {
        const val SAMPLE_RATE = 44100

        private const val SAMPLE_SIZE_BITS = 16
        private const val SAMPLE_SIZE_BYTE = SAMPLE_SIZE_BITS / Byte.SIZE_BITS
        private const val CHANNELS = 1
        private const val BUFFER_SIZE = SAMPLE_RATE * SAMPLE_SIZE_BYTE * CHANNELS / 10 // 100ms

        private const val DROP_THRESHOLD = SAMPLE_RATE * SAMPLE_SIZE_BYTE / 1000 * 32 // 32ms

        private const val VOLUME_LEVEL = 255
    }
}
