package pad

import apu.isSetUByte
import ext.toInt

class Pad(
    keyEvent: KeyEvent,
) {
    private var isSet = false
    private var index = 0
    private val registers = Array(8, init = { false })
    private val buffer = Array(8, init = { false })

    init {
        keyEvent.listen(object : KeyEventListener {
            override fun onKeyDown(key: Key) {
                buffer[key.keyCode] = true
            }

            override fun onKeyUp(key: Key) {
                buffer[key.keyCode] = false
            }
        })
    }

    // TODO: fix a bug (use nestest ROM)
    fun read() = registers[index++].toInt()

    fun write(data: Int) {
        val strobe = data.toUByte().isSetUByte(0u)
        if (strobe) {
            isSet = true
        } else if (isSet && !strobe) {
            isSet = false
            index = 0
            buffer.forEachIndexed { idx, b -> registers[idx] = b }
        }
    }
}
