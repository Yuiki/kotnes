import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import java.io.File
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}

class Main : Application() {
    override fun start(primaryStage: Stage) {
        val root = Group()
        val canvas = Canvas(256.0, 224.0)
        val g = canvas.graphicsContext2D
        root.children += canvas
        primaryStage.scene = Scene(root)
        primaryStage.title = "Kotnes"
        primaryStage.show()

        thread {
            val classLoader = Main::class.java.classLoader
            val romFile = File(classLoader.getResource("giko011.nes").file)
            val rom = Rom(romFile)
            val emulator = Emulator(rom = rom, canvas = JavaFXCanvas(g), keyEvent = JavaFXKeyEvent(primaryStage.scene))
            emulator.start()
        }
    }
}