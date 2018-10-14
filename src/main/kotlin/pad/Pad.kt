package pad

import ext.toInt

class Pad(
        keyEvent: KeyEvent
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

    fun read() = registers[index++].toInt()

    fun write(data: Int) {
        if (data != 0) {
            isSet = true
        } else if (isSet && data == 0) {
            isSet = false
            index = 0
            buffer.forEachIndexed { idx, b -> registers[idx] = b }
        }
    }
}