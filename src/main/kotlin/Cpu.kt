import exception.UnknownOpcodeException
import ext.toHex
import ext.toInt

class Cpu(
        private val bus: CpuBus
) {
    private var registers = Registers()
    private var hasBranched = false

    fun reset() {
        registers = Registers().apply {
            //pc = readWord(0xFFFC)
            pc = 0xc000
        }
    }

    data class OperandData(val operand: Int, val additionalCycle: Int = 0)

    private fun getOperand(mode: AddressingMode) =
            when (mode) {
                AddressingMode.ACCUMULATOR -> OperandData(0x00)
                AddressingMode.IMMEDIATE -> OperandData(fetch(registers.pc))
                AddressingMode.ABSOLUTE -> OperandData(fetchWord(registers.pc))
                AddressingMode.ZERO_PAGE -> OperandData(fetch(registers.pc))
                AddressingMode.ZERO_PAGE_X -> OperandData(fetch(registers.pc) + registers.x and 0xFF)
                AddressingMode.ZERO_PAGE_Y -> OperandData(fetch(registers.pc) + registers.y and 0xFF)
                AddressingMode.ABSOLUTE_X -> {
                    val addr = fetchWord(registers.pc) + registers.x and 0xFFFF
                    val additionalCycle = (addr and 0xFF00 != (addr + registers.x) and 0xFF00).toInt()
                    OperandData(addr, additionalCycle)
                }
                AddressingMode.ABSOLUTE_Y -> {
                    val addr = fetchWord(registers.pc) + registers.y and 0xFFFF
                    val additionalCycle = (addr and 0xFF00 != (addr + registers.y) and 0xFF00).toInt()
                    OperandData(addr, additionalCycle)
                }
                AddressingMode.IMPLIED -> OperandData(0x00)
                AddressingMode.RELATIVE -> {
                    val baseAddr = fetch(registers.pc)
                    val addr = baseAddr + if (baseAddr < 0x80) registers.pc else registers.pc - 0x100
                    val additionalCycle = (addr and 0xFF00 != registers.pc and 0xFF00).toInt()
                    OperandData(addr, additionalCycle)
                }
                AddressingMode.INDIRECT_X -> {
                    val baseAddr = (fetch(registers.pc) + registers.x) and 0xFF
                    val addr = (read(baseAddr) + (read((baseAddr + 1) and 0xFF) shl 8)) and 0xFFFF
                    OperandData(addr)
                }
                AddressingMode.INDIRECT_Y -> {
                    val fetchedAddr = fetch(registers.pc)
                    val baseAddr = read(fetchedAddr) + (read((fetchedAddr + 1) and 0xFF) shl 8)
                    val addr = baseAddr + registers.y
                    val additionalCycle = (addr and 0xFF00 != baseAddr and 0xFF00).toInt()
                    OperandData(addr and 0xFFFF, additionalCycle)
                }
                AddressingMode.INDIRECT -> {
                    val baseAddr = fetchWord(registers.pc)
                    val addr = (read(baseAddr) + (read((baseAddr and 0xFF00) or (((baseAddr and 0xFF) + 1) and 0xFF)) shl 8)) and 0xFFFF
                    OperandData(addr)
                }
            }

    private fun fetch(addr: Int): Int {
        registers.pc += 1
        return read(addr)
    }

    private fun fetchWord(addr: Int): Int {
        registers.pc += 2
        return readWord(addr)
    }

    private fun read(addr: Int) = bus.read(addr)

    private fun readWord(addr: Int) = read(addr) or (read(addr + 1) shl 8)

    private fun write(addr: Int, data: Int) {
        bus.write(addr, data)
    }

    private fun push(data: Int) {
        write(0x100 or (registers.sp and 0xFF), data)
        registers.sp--
    }

    private fun pop(): Int {
        registers.sp++
        return read(0x100 or (registers.sp and 0xFF))
    }

    private fun branch(addr: Int) {
        hasBranched = true
        registers.pc = addr
    }

    private fun pushStatus() {
        push(registers.status)
    }

    private fun popStatus() {
        val status = pop()
        registers.n = status and 0x80 != 0
        registers.v = status and 0x40 != 0
        registers.r = status and 0x20 != 0
        registers.b = status and 0x10 != 0
        registers.d = status and 0x08 != 0
        registers.i = status and 0x04 != 0
        registers.z = status and 0x02 != 0
        registers.c = status and 0x01 != 0
    }

    private fun popPc() {
        registers.pc = pop()
        registers.pc += pop() shl 8
    }

    private fun exec(instruction: Instruction, mode: AddressingMode, operand: Int) {
        hasBranched = false

        when (instruction) {
            Instruction.ADC -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result: Int = data + registers.a + registers.c.toInt()
                registers.n = result and 0x80 != 0
                registers.v = registers.a xor data and 0x80 == 0 && registers.a xor result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.c = result > 0xFF
                registers.a = result and 0xFF
            }
            Instruction.SBC -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result: Int = registers.a - data - (!registers.c).toInt()
                registers.n = result and 0x80 != 0
                registers.v = registers.a xor data and 0x80 != 0 && registers.a xor result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.c = result >= 0
                registers.a = result and 0xFF
            }
            Instruction.AND -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = data and registers.a
                registers.n = result and 0x80 != 0
                registers.z = result == 0
                registers.a = result and 0xFF
            }
            Instruction.ORA -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = data or registers.a
                registers.n = result and 0x80 != 0
                registers.z = result == 0
                registers.a = result and 0xFF
            }
            Instruction.EOR -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = data xor registers.a
                registers.n = result and 0x80 != 0
                registers.z = result == 0
                registers.a = result and 0xFF
            }
            Instruction.ASL -> {
                if (mode == AddressingMode.ACCUMULATOR) {
                    val result = (registers.a shl 1) and 0xFF
                    registers.n = result != 0
                    registers.z = result == 0
                    registers.c = registers.a and 0x80 != 0
                    registers.a = result
                } else {
                    val data = read(operand)
                    val result = (data shl 1) and 0xFF
                    registers.n = result != 0
                    registers.z = result == 0
                    registers.c = data and 0x80 != 0
                    write(operand, result)
                }
            }
            Instruction.LSR -> {
                if (mode == AddressingMode.ACCUMULATOR) {
                    val result = (registers.a shr 1) and 0xFF
                    registers.z = result == 0
                    registers.c = registers.a and 0x01 != 0
                    registers.a = result
                } else {
                    val data = read(operand)
                    val result = data shr 1
                    registers.z = result == 0
                    registers.c = registers.a and 0x01 != 0
                    write(operand, result)
                }
                registers.n = false
            }
            Instruction.ROL -> {
                if (mode == AddressingMode.ACCUMULATOR) {
                    val result = (registers.a shl 1) and 0xFF or registers.c.toInt()
                    registers.n = result and 0x80 != 0
                    registers.z = result == 0
                    registers.c = registers.a and 0x80 != 0
                    registers.a = result
                } else {
                    val data = read(operand)
                    val result = (data shl 1) and 0xFF or registers.c.toInt()
                    registers.n = result and 0x80 != 0
                    registers.z = result == 0
                    registers.c = data and 0x80 != 0
                    write(operand, result)
                }
            }
            Instruction.ROR -> {
                if (mode == AddressingMode.ACCUMULATOR) {
                    val result = (registers.a shr 1) or if (registers.c) 0x80 else 0x00
                    registers.n = result and 0x80 != 0
                    registers.z = result == 0
                    registers.c = registers.a and 0x01 != 0
                    registers.a = result
                } else {
                    val data = read(operand)
                    val result = (data shr 1) and 0xFF or if (registers.c) 0x80 else 0x00
                    registers.n = result and 0x80 != 0
                    registers.z = result == 0
                    registers.c = data and 0x01 != 0
                    write(operand, result)
                }
            }
            Instruction.BCC -> if (!registers.c) branch(operand)
            Instruction.BCS -> if (registers.c) branch(operand)
            Instruction.BEQ -> if (registers.z) branch(operand)
            Instruction.BNE -> if (!registers.z) branch(operand)
            Instruction.BVC -> if (!registers.v) branch(operand)
            Instruction.BVS -> if (registers.v) branch(operand)
            Instruction.BPL -> if (!registers.n) branch(operand)
            Instruction.BMI -> if (registers.n) branch(operand)
            Instruction.BIT -> {
                val data = read(operand)
                registers.n = data and 0x80 != 0
                registers.v = data and 0x40 != 0
                registers.z = registers.a and data == 0
            }
            Instruction.JMP -> branch(operand)
            Instruction.JSR -> {
                val pc = registers.pc - 1
                push(pc shr 8 and 0xFF)
                push(pc and 0xFF)
                branch(operand)
            }
            Instruction.RTS -> {
                popPc()
                registers.pc++
            }
            Instruction.BRK -> {
                registers.pc++
                registers.b = true
                push(registers.pc shr 8 and 0xFF)
                push(registers.pc shr 0xFF)
                pushStatus()
                if (registers.i) {
                    registers.pc = readWord(0xFFFE)
                }
                registers.i = true
            }
            Instruction.RTI -> {
                popStatus()
                popPc()
                this.registers.r = true
            }
            Instruction.CMP -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = registers.a - data
                registers.n = result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.c = result >= 0
            }
            Instruction.CPX -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = registers.x - data
                registers.n = result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.c = result >= 0
            }
            Instruction.CPY -> {
                val data = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                val result = registers.y - data
                registers.n = result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.c = result >= 0
            }
            Instruction.INC -> {
                val data = read(operand) + 1 and 0xFF
                registers.n = data and 0x80 != 0
                registers.z = data == 0
                write(operand, data)
            }
            Instruction.DEC -> {
                val data = read(operand) - 1 and 0xFF
                registers.n = data and 0x80 != 0
                registers.z = data == 0
                write(operand, data)
            }
            Instruction.INX -> {
                registers.x = registers.x + 1 and 0xFF
                registers.n = registers.x and 0x80 != 0
                registers.z = registers.x == 0
            }
            Instruction.DEX -> {
                registers.x = registers.x - 1 and 0xFF
                registers.n = registers.x and 0x80 != 0
                registers.z = registers.x == 0
            }
            Instruction.INY -> {
                registers.y = registers.y + 1 and 0xFF
                registers.n = registers.y and 0x80 != 0
                registers.z = registers.y == 0
            }
            Instruction.DEY -> {
                registers.y = registers.y - 1 and 0xFF
                registers.n = registers.y and 0x80 != 0
                registers.z = registers.y == 0
            }
            Instruction.CLC -> registers.c = false
            Instruction.SEC -> registers.c = true
            Instruction.CLI -> registers.i = false
            Instruction.SEI -> registers.i = true
            Instruction.CLD -> registers.d = false
            Instruction.SED -> registers.d = true
            Instruction.CLV -> registers.v = false
            Instruction.LDA -> {
                registers.a = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a == 0
            }
            Instruction.LDX -> {
                registers.x = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                registers.n = registers.x and 0x80 != 0
                registers.z = registers.x == 0
            }
            Instruction.LDY -> {
                registers.y = if (mode == AddressingMode.IMMEDIATE) operand else read(operand)
                registers.n = registers.y and 0x80 != 0
                registers.z = registers.y == 0
            }
            Instruction.STA -> write(operand, registers.a)
            Instruction.STX -> write(operand, registers.x)
            Instruction.STY -> write(operand, registers.y)
            Instruction.TAX -> {
                registers.x = registers.a
                registers.n = registers.x and 0x80 != 0
                registers.z = registers.x == 0
            }
            Instruction.TXA -> {
                registers.a = registers.x
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a == 0
            }
            Instruction.TAY -> {
                registers.y = registers.a
                registers.n = registers.y and 0x80 != 0
                registers.z = registers.y == 0
            }
            Instruction.TYA -> {
                registers.a = registers.y
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a == 0
            }
            Instruction.TSX -> {
                registers.x = registers.sp
                registers.n = registers.x and 0x80 != 0
                registers.z = registers.x == 0
            }
            Instruction.TXS -> registers.sp = registers.x
            Instruction.PHA -> push(registers.a)
            Instruction.PLA -> {
                registers.a = pop()
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a == 0
            }
            Instruction.PHP -> {
                registers.b = true
                pushStatus()
                registers.b = false
            }
            Instruction.PLP -> {
                popStatus()
                registers.r = true
                registers.b = false
            }
            Instruction.NOP -> {
            }
            Instruction.NOPD -> {
                registers.pc++
            }
            Instruction.NOPI -> {
                registers.pc += 2
            }
            Instruction.LAX -> {
                registers.a = read(operand)
                registers.x = registers.a
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a == 0
            }
            Instruction.SAX -> {
                val result = registers.a and registers.x
                write(operand, result)
            }
            Instruction.DCP -> {
                val data = read(operand) - 1 and 0xFF
                registers.n = ((registers.a - data) and 0x1FF) and 0x80 != 0
                registers.z = (registers.a - data) and 0x1FF == 0
                write(operand, data)
            }
            Instruction.ISC -> {
                val data = (read(operand) + 1) and 0xFF
                val result = (data.inv() and 0xFF) + registers.a + registers.c.toInt()
                val overflow = ((registers.a xor data) and 0x80 == 0) && ((registers.a xor result) and 0x80) != 0
                registers.v = overflow
                registers.c = result > 0xFF
                registers.n = result and 0x80 != 0
                registers.z = result == 0
                registers.a = result and 0xFF
                write(operand, data)
            }
            Instruction.SLO -> {
                var data = read(operand)
                registers.c = data and 0x80 != 0
                data = (data shl 1) and 0xFF
                registers.a = registers.a or data
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a and 0xFF == 0
                write(operand, data)
            }
            Instruction.RLA -> {
                val data = (read(operand) shl 1) + registers.c.toInt()
                registers.c = data and 0x100 != 0
                registers.a = (data and registers.a) and 0xFF
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a and 0xFF == 0
                write(operand, data)
            }
            Instruction.SRE -> {
                var data = read(operand)
                registers.c = data and 0x01 != 0
                data = data shr 1
                registers.a = registers.a xor data
                registers.n = registers.a and 0x80 != 0
                registers.z = registers.a and 0xFF == 0
                write(operand, data)
            }
            Instruction.RRA -> {
                var data = read(operand)
                val carry = data and 0x01 != 0
                data = (data shr 1) or if (registers.c) 0x80 else 0x00
                val result = data + registers.a + carry.toInt()
                val overflow = ((registers.a xor data) and 0x80) == 0 && ((registers.a xor result) and 0x80) != 0
                registers.v = overflow
                registers.n = result and 0x80 != 0
                registers.z = result and 0xFF == 0
                registers.a = result and 0xFF
                registers.c = result > 0xFF
                write(operand, data)
            }
        }
    }

    fun run(): Int {
        val pc = registers.pc
        val opcode = opcodes[fetch(pc)] ?: throw UnknownOpcodeException()
        val (instruction, mode, cycle) = opcode
        println("${pc.toHex()}\t${instruction.name}\tA:${registers.a.toHex()}\tX:${registers.x.toHex()}\tY:${registers.y.toHex()}\tP:${registers.status.toHex()}\tSP:${registers.sp.toHex()}")
        val (operand, additionalCycle) = getOperand(mode)
        exec(instruction, mode, operand)
        return cycle + additionalCycle + hasBranched.toInt()
    }
}