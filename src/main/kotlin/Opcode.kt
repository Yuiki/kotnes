data class Opcode(
        val instruction: Instruction,
        val mode: AddressingMode,
        val cycle: Int
)

enum class Instruction {
    ADC, SBC, AND, ORA, EOR, ASL, LSR, ROL, ROR, BCC,
    BCS, BEQ, BNE, BVC, BVS, BPL, BMI, BIT, JMP, JSR,
    RTS, BRK, RTI, CMP, CPX, CPY, INC, DEC, INX, DEX,
    INY, DEY, CLC, SEC, CLI, SEI, CLD, SED, CLV, LDA,
    LDX, LDY, STA, STX, STY, TAX, TXA, TAY, TYA, TSX,
    TXS, PHA, PLA, PHP, PLP, NOP
}

enum class AddressingMode {
    ACCUMULATOR,
    IMMEDIATE,
    ABSOLUTE,
    ZERO_PAGE,
    ZERO_PAGE_X,
    ZERO_PAGE_Y,
    ABSOLUTE_X,
    ABSOLUTE_Y,
    IMPLIED,
    RELATIVE,
    INDIRECT_X,
    INDIRECT_Y,
    INDIRECT
}

val opcodes = mapOf(
        0x69 to Opcode(Instruction.ADC, AddressingMode.IMMEDIATE, 2),
        0x65 to Opcode(Instruction.ADC, AddressingMode.ZERO_PAGE, 3),
        0x75 to Opcode(Instruction.ADC, AddressingMode.ZERO_PAGE_X, 4),
        0x6D to Opcode(Instruction.ADC, AddressingMode.ABSOLUTE, 4),
        0x7D to Opcode(Instruction.ADC, AddressingMode.ABSOLUTE_X, 4),
        0x79 to Opcode(Instruction.ADC, AddressingMode.ABSOLUTE_Y, 4),
        0x61 to Opcode(Instruction.ADC, AddressingMode.INDIRECT_X, 6),
        0x71 to Opcode(Instruction.ADC, AddressingMode.INDIRECT_Y, 5),
        0xE9 to Opcode(Instruction.SBC, AddressingMode.IMMEDIATE, 2),
        0xE5 to Opcode(Instruction.SBC, AddressingMode.ZERO_PAGE, 3),
        0xF5 to Opcode(Instruction.SBC, AddressingMode.ZERO_PAGE_X, 4),
        0xED to Opcode(Instruction.SBC, AddressingMode.ABSOLUTE, 4),
        0xFD to Opcode(Instruction.SBC, AddressingMode.ABSOLUTE_X, 4),
        0xF9 to Opcode(Instruction.SBC, AddressingMode.ABSOLUTE_Y, 4),
        0xE1 to Opcode(Instruction.SBC, AddressingMode.INDIRECT_X, 6),
        0xF1 to Opcode(Instruction.SBC, AddressingMode.INDIRECT_Y, 5),
        0x29 to Opcode(Instruction.AND, AddressingMode.IMMEDIATE, 2),
        0x25 to Opcode(Instruction.AND, AddressingMode.ZERO_PAGE, 3),
        0x35 to Opcode(Instruction.AND, AddressingMode.ZERO_PAGE_X, 4),
        0x2D to Opcode(Instruction.AND, AddressingMode.ABSOLUTE, 4),
        0x3D to Opcode(Instruction.AND, AddressingMode.ABSOLUTE_X, 4),
        0x39 to Opcode(Instruction.AND, AddressingMode.ABSOLUTE_Y, 4),
        0x21 to Opcode(Instruction.AND, AddressingMode.INDIRECT_X, 6),
        0x31 to Opcode(Instruction.AND, AddressingMode.INDIRECT_Y, 5),
        0x09 to Opcode(Instruction.ORA, AddressingMode.IMMEDIATE, 2),
        0x05 to Opcode(Instruction.ORA, AddressingMode.ZERO_PAGE, 3),
        0x15 to Opcode(Instruction.ORA, AddressingMode.ZERO_PAGE_X, 4),
        0x0D to Opcode(Instruction.ORA, AddressingMode.ABSOLUTE, 4),
        0x1D to Opcode(Instruction.ORA, AddressingMode.ABSOLUTE_X, 4),
        0x19 to Opcode(Instruction.ORA, AddressingMode.ABSOLUTE_Y, 4),
        0x01 to Opcode(Instruction.ORA, AddressingMode.INDIRECT_X, 6),
        0x11 to Opcode(Instruction.ORA, AddressingMode.INDIRECT_Y, 5),
        0x49 to Opcode(Instruction.EOR, AddressingMode.IMMEDIATE, 2),
        0x45 to Opcode(Instruction.EOR, AddressingMode.ZERO_PAGE, 3),
        0x55 to Opcode(Instruction.EOR, AddressingMode.ZERO_PAGE_X, 4),
        0x4D to Opcode(Instruction.EOR, AddressingMode.ABSOLUTE, 4),
        0x5D to Opcode(Instruction.EOR, AddressingMode.ABSOLUTE_X, 4),
        0x59 to Opcode(Instruction.EOR, AddressingMode.ABSOLUTE_Y, 4),
        0x41 to Opcode(Instruction.EOR, AddressingMode.INDIRECT_X, 6),
        0x51 to Opcode(Instruction.EOR, AddressingMode.INDIRECT_Y, 5),
        0x0A to Opcode(Instruction.ASL, AddressingMode.ACCUMULATOR, 2),
        0x06 to Opcode(Instruction.ASL, AddressingMode.ZERO_PAGE, 5),
        0x16 to Opcode(Instruction.ASL, AddressingMode.ZERO_PAGE_X, 6),
        0x0E to Opcode(Instruction.ASL, AddressingMode.ABSOLUTE, 6),
        0x1E to Opcode(Instruction.ASL, AddressingMode.ABSOLUTE_X, 6),
        0x4A to Opcode(Instruction.LSR, AddressingMode.ACCUMULATOR, 2),
        0x46 to Opcode(Instruction.LSR, AddressingMode.ZERO_PAGE, 5),
        0x56 to Opcode(Instruction.LSR, AddressingMode.ZERO_PAGE_X, 6),
        0x4E to Opcode(Instruction.LSR, AddressingMode.ABSOLUTE, 6),
        0x5E to Opcode(Instruction.LSR, AddressingMode.ABSOLUTE_X, 6),
        0x2A to Opcode(Instruction.ROL, AddressingMode.ACCUMULATOR, 2),
        0x26 to Opcode(Instruction.ROL, AddressingMode.ZERO_PAGE, 5),
        0x36 to Opcode(Instruction.ROL, AddressingMode.ZERO_PAGE_X, 6),
        0x2E to Opcode(Instruction.ROL, AddressingMode.ABSOLUTE, 6),
        0x3E to Opcode(Instruction.ROL, AddressingMode.ABSOLUTE_X, 6),
        0x6A to Opcode(Instruction.ROR, AddressingMode.ACCUMULATOR, 2),
        0x66 to Opcode(Instruction.ROR, AddressingMode.ZERO_PAGE, 5),
        0x76 to Opcode(Instruction.ROR, AddressingMode.ZERO_PAGE_X, 6),
        0x6E to Opcode(Instruction.ROR, AddressingMode.ABSOLUTE, 6),
        0x7E to Opcode(Instruction.ROR, AddressingMode.ABSOLUTE_X, 6),
        0x90 to Opcode(Instruction.BCC, AddressingMode.RELATIVE, 2),
        0xB0 to Opcode(Instruction.BCS, AddressingMode.RELATIVE, 2),
        0xF0 to Opcode(Instruction.BEQ, AddressingMode.RELATIVE, 2),
        0xD0 to Opcode(Instruction.BNE, AddressingMode.RELATIVE, 2),
        0x50 to Opcode(Instruction.BVC, AddressingMode.RELATIVE, 2),
        0x70 to Opcode(Instruction.BVS, AddressingMode.RELATIVE, 2),
        0x10 to Opcode(Instruction.BPL, AddressingMode.RELATIVE, 2),
        0x30 to Opcode(Instruction.BMI, AddressingMode.RELATIVE, 2),
        0x24 to Opcode(Instruction.BIT, AddressingMode.ZERO_PAGE, 3),
        0x2C to Opcode(Instruction.BIT, AddressingMode.ABSOLUTE, 4),
        0x4C to Opcode(Instruction.JMP, AddressingMode.ABSOLUTE, 3),
        0x6C to Opcode(Instruction.JMP, AddressingMode.INDIRECT, 4),
        0x20 to Opcode(Instruction.JSR, AddressingMode.ABSOLUTE, 6),
        0x60 to Opcode(Instruction.RTS, AddressingMode.IMPLIED, 6),
        0x00 to Opcode(Instruction.BRK, AddressingMode.IMPLIED, 7),
        0x40 to Opcode(Instruction.RTI, AddressingMode.IMPLIED, 6),
        0xC9 to Opcode(Instruction.CMP, AddressingMode.IMMEDIATE, 2),
        0xC5 to Opcode(Instruction.CMP, AddressingMode.ZERO_PAGE, 3),
        0xD5 to Opcode(Instruction.CMP, AddressingMode.ZERO_PAGE_X, 4),
        0xCD to Opcode(Instruction.CMP, AddressingMode.ABSOLUTE, 4),
        0xDD to Opcode(Instruction.CMP, AddressingMode.ABSOLUTE_X, 4),
        0xD9 to Opcode(Instruction.CMP, AddressingMode.ABSOLUTE_Y, 4),
        0xC1 to Opcode(Instruction.CMP, AddressingMode.INDIRECT_X, 6),
        0xD1 to Opcode(Instruction.CMP, AddressingMode.INDIRECT_Y, 5),
        0xE0 to Opcode(Instruction.CPX, AddressingMode.IMMEDIATE, 2),
        0xE4 to Opcode(Instruction.CPX, AddressingMode.ZERO_PAGE, 3),
        0xEC to Opcode(Instruction.CPX, AddressingMode.ABSOLUTE, 4),
        0xC0 to Opcode(Instruction.CPY, AddressingMode.IMMEDIATE, 2),
        0xC4 to Opcode(Instruction.CPY, AddressingMode.ZERO_PAGE, 3),
        0xCC to Opcode(Instruction.CPY, AddressingMode.ABSOLUTE, 4),
        0xE6 to Opcode(Instruction.INC, AddressingMode.ZERO_PAGE, 5),
        0xF6 to Opcode(Instruction.INC, AddressingMode.ZERO_PAGE_X, 6),
        0xEE to Opcode(Instruction.INC, AddressingMode.ABSOLUTE, 6),
        0xFE to Opcode(Instruction.INC, AddressingMode.ABSOLUTE_X, 6),
        0xC6 to Opcode(Instruction.DEC, AddressingMode.ZERO_PAGE, 5),
        0xD6 to Opcode(Instruction.DEC, AddressingMode.ZERO_PAGE_X, 6),
        0xCE to Opcode(Instruction.DEC, AddressingMode.ABSOLUTE, 6),
        0xDE to Opcode(Instruction.DEC, AddressingMode.ABSOLUTE_X, 6),
        0xE8 to Opcode(Instruction.INX, AddressingMode.IMPLIED, 2),
        0xCA to Opcode(Instruction.DEX, AddressingMode.IMPLIED, 2),
        0xC8 to Opcode(Instruction.INY, AddressingMode.IMPLIED, 2),
        0x88 to Opcode(Instruction.DEY, AddressingMode.IMPLIED, 2),
        0x18 to Opcode(Instruction.CLC, AddressingMode.IMPLIED, 2),
        0x38 to Opcode(Instruction.SEC, AddressingMode.IMPLIED, 2),
        0x58 to Opcode(Instruction.CLI, AddressingMode.IMPLIED, 2),
        0x78 to Opcode(Instruction.SEI, AddressingMode.IMPLIED, 2),
        0xD8 to Opcode(Instruction.CLD, AddressingMode.IMPLIED, 2),
        0xF8 to Opcode(Instruction.SED, AddressingMode.IMPLIED, 2),
        0xB8 to Opcode(Instruction.CLV, AddressingMode.IMPLIED, 2),
        0xA9 to Opcode(Instruction.LDA, AddressingMode.IMMEDIATE, 2),
        0xA5 to Opcode(Instruction.LDA, AddressingMode.ZERO_PAGE, 3),
        0xB5 to Opcode(Instruction.LDA, AddressingMode.ZERO_PAGE_X, 4),
        0xAD to Opcode(Instruction.LDA, AddressingMode.ABSOLUTE, 4),
        0xBD to Opcode(Instruction.LDA, AddressingMode.ABSOLUTE_X, 4),
        0xB9 to Opcode(Instruction.LDA, AddressingMode.ABSOLUTE_Y, 4),
        0xA1 to Opcode(Instruction.LDA, AddressingMode.INDIRECT_X, 6),
        0xB1 to Opcode(Instruction.LDA, AddressingMode.INDIRECT_Y, 5),
        0xA2 to Opcode(Instruction.LDX, AddressingMode.IMMEDIATE, 2),
        0xA6 to Opcode(Instruction.LDX, AddressingMode.ZERO_PAGE, 3),
        0xB6 to Opcode(Instruction.LDX, AddressingMode.ZERO_PAGE_Y, 4),
        0xAE to Opcode(Instruction.LDX, AddressingMode.ABSOLUTE, 4),
        0xBE to Opcode(Instruction.LDX, AddressingMode.ABSOLUTE_Y, 4),
        0xA0 to Opcode(Instruction.LDY, AddressingMode.IMMEDIATE, 2),
        0xA4 to Opcode(Instruction.LDY, AddressingMode.ZERO_PAGE, 3),
        0xB4 to Opcode(Instruction.LDY, AddressingMode.ZERO_PAGE_X, 4),
        0xAC to Opcode(Instruction.LDY, AddressingMode.ABSOLUTE, 4),
        0xBC to Opcode(Instruction.LDY, AddressingMode.ABSOLUTE_X, 4),
        0x85 to Opcode(Instruction.STA, AddressingMode.ZERO_PAGE, 3),
        0x95 to Opcode(Instruction.STA, AddressingMode.ZERO_PAGE_X, 4),
        0x8D to Opcode(Instruction.STA, AddressingMode.ABSOLUTE, 4),
        0x9D to Opcode(Instruction.STA, AddressingMode.ABSOLUTE_X, 4),
        0x99 to Opcode(Instruction.STA, AddressingMode.ABSOLUTE_Y, 4),
        0x81 to Opcode(Instruction.STA, AddressingMode.INDIRECT_X, 6),
        0x91 to Opcode(Instruction.STA, AddressingMode.INDIRECT_Y, 5),
        0x86 to Opcode(Instruction.STX, AddressingMode.ZERO_PAGE, 3),
        0x96 to Opcode(Instruction.STX, AddressingMode.ZERO_PAGE_Y, 4),
        0x8E to Opcode(Instruction.STX, AddressingMode.ABSOLUTE, 4),
        0x84 to Opcode(Instruction.STY, AddressingMode.ZERO_PAGE, 3),
        0x94 to Opcode(Instruction.STY, AddressingMode.ZERO_PAGE_X, 4),
        0x8C to Opcode(Instruction.STY, AddressingMode.ABSOLUTE, 4),
        0xAA to Opcode(Instruction.TAX, AddressingMode.IMPLIED, 2),
        0x8A to Opcode(Instruction.TXA, AddressingMode.IMPLIED, 2),
        0xA8 to Opcode(Instruction.TAY, AddressingMode.IMPLIED, 2),
        0x98 to Opcode(Instruction.TYA, AddressingMode.IMPLIED, 2),
        0x9A to Opcode(Instruction.TXS, AddressingMode.IMPLIED, 2),
        0xBA to Opcode(Instruction.TSX, AddressingMode.IMPLIED, 2),
        0x48 to Opcode(Instruction.PHA, AddressingMode.IMPLIED, 3),
        0x68 to Opcode(Instruction.PLA, AddressingMode.IMPLIED, 4),
        0x08 to Opcode(Instruction.PHP, AddressingMode.IMPLIED, 3),
        0x28 to Opcode(Instruction.PLP, AddressingMode.IMPLIED, 4),
        0xEA to Opcode(Instruction.NOP, AddressingMode.IMPLIED, 2)
)