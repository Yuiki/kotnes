import cartridge.Cartridge
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.stage.Stage
import pad.JavaFXKeyEvent
import ppu.JavaFXCanvas
import java.io.File
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    Application.launch(Main::class.java)
}

class Main : Application() {
    override fun start(primaryStage: Stage) {
        val root = Group()
        val canvas = Canvas(WIDTH, HEIGHT)
        root.children += canvas
        primaryStage.scene = Scene(root)
        primaryStage.title = TITLE
        primaryStage.show()

        thread {
            val classLoader = Main::class.java.classLoader
            val romFile = File(classLoader.getResource(ROM_NAME).file)
            val rom = Cartridge(romFile)
            val g = canvas.graphicsContext2D

            Emulator(
                cartridge = rom,
                canvas = JavaFXCanvas(g),
                keyEvent = JavaFXKeyEvent(primaryStage.scene)
            ).start()
        }
    }

    companion object {
        const val TITLE = "kotnes"

        const val WIDTH = 256.0 * 2
        const val HEIGHT = 224.0 * 2

        const val ROM_NAME = "test.nes"
    }
}
