class Registers {
    var a = 0 // accumulator
    var x = 0 // index register
    var y = 0 // index register
    var s = 0 // stack pointer register
    val sp get() = s + 0x0100 // stack pointer
    var n = false // status negative
    var v = false // status overflow
    var r = false // status reserved
    var b = false // status break
    var d = false // status decimal
    var i = false // status interrupt
    var z = false // status zero
    var c = false // status carry
    var pc = 0 // program counter
}