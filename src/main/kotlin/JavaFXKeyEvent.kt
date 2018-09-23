import javafx.scene.Scene
import javafx.scene.input.KeyCode

class JavaFXKeyEvent(
        private val scene: Scene
): KeyEvent {
    override fun listen(listener: KeyEventListener) {
        scene.setOnKeyPressed {
            when (it.code) {
                KeyCode.K -> listener.onKeyDown(Key.A)
                KeyCode.J -> listener.onKeyDown(Key.B)
                KeyCode.SHIFT -> listener.onKeyDown(Key.SELECT)
                KeyCode.ENTER -> listener.onKeyDown(Key.START)
                KeyCode.W -> listener.onKeyDown(Key.UP)
                KeyCode.S -> listener.onKeyDown(Key.DOWN)
                KeyCode.A -> listener.onKeyDown(Key.LEFT)
                KeyCode.D -> listener.onKeyDown(Key.RIGHT)
                else -> {}
            }
        }
        scene.setOnKeyReleased {
            when (it.code) {
                KeyCode.K -> listener.onKeyUp(Key.A)
                KeyCode.J -> listener.onKeyUp(Key.B)
                KeyCode.SHIFT -> listener.onKeyUp(Key.SELECT)
                KeyCode.ENTER -> listener.onKeyUp(Key.START)
                KeyCode.W -> listener.onKeyUp(Key.UP)
                KeyCode.S -> listener.onKeyUp(Key.DOWN)
                KeyCode.A -> listener.onKeyUp(Key.LEFT)
                KeyCode.F -> listener.onKeyUp(Key.RIGHT)
                else -> {}
            }
        }
    }
}