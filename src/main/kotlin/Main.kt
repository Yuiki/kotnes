import java.io.File

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val classLoader = Main::class.java.classLoader
            val romFile = File(classLoader.getResource("helloworld.nes").file)
            val rom = Rom(romFile)
            val emulator = Emulator(rom)
            emulator.start()
        }
    }
}