package com.idcom4.cpu;

import com.idcom4.Context;
import com.idcom4.cpu.components.*;

import java.util.Arrays;

public class CPU_16 {

    record InstructionPayload(
            short opcode,
            short param0, short param1,
            boolean param0AsIntLit, boolean param1AsIntLit,
            boolean param0AsPointer, boolean param1AsPointer,
            boolean param0AsMemAddr, boolean param1AsMemAddr
    ) {}

    enum DataEndpoints {
        R0(0x0),                    // static address
        R1(0x1),                    // static address
        R2(0x2),                    // static address
        R3(0x3),                    // static address
        R_ACCU0(0x4),               // static address
        R_ACCU1(0x5),               // static address
        R_FLAGS(0x6),               // static address
        R_SPTR(0x7),                // static address
        R_EXPTR(0x8),               // static address
        R_MEM_EXTENSION(0x9),       // static address
        R_INTERRUPT_CODE(0xa),      // static address
        STACK(0xb),                 // static address
        IN(0xc),                    // static address
        OUT(0xd),                   // static address
        MEM_INTERRUPT(0xe),         // dynamic address, reserved for root interrupt handling
        MEM_START(0x19);            // dynamic address

        public final short address;

        DataEndpoints(int address) {
            this.address = (short)address;
        }
    }

    enum Masks {
        PARAM0_AS_INT_LIT(0b1000000000000000),
        PARAM1_AS_INT_LIT(0b0100000000000000),
        PARAM0_AS_POINTER(0b0010000000000000),
        PARAM1_AS_POINTER(0b0001000000000000),
        PARAM0_AS_MEMORY_ADDR(0b0000100000000000),
        PARAM1_AS_MEMORY_ADDR(0b0000010000000000),
        OPCODE(0b0000001111111111);

        public final short value;

        Masks(int value) {
            this.value = (short)value;
        }
    }

    enum OPCodes {
        NOOP(0x0),
        ADD(0x1),
        SUBSTRACT(0x2),
        DIVIDE(0x3),
        MULTIPLY(0x4),
        MODULO(0x5),
        AND(0x6),
        OR(0x7),
        XOR(0x8),
        LEFT_SHIFT(0x9),
        RIGHT_SHIFT(0xa),
        MATH_OPS(0xf),
        // reserved for the ALU up to 0xf
        JMP_EQ(0x10),
        JMP_LESS(0x11),
        JMP_LESS_EQ(0x12),
        JMP_GREATER(0x13),
        JMP_GREATER_EQ(0x14),
        // reserved for comparison up to 0x1f
        MOVE(0x20),
        INTERRUPT(0x21),
        OP_COUNT(0x22);

        public final short code;

        OPCodes(int code) {
            this.code = (short)code;
        }

        public static OPCodes fromBytes(short code) {
            for (OPCodes op : OPCodes.values()) {
                if (op.code == code) return op;
            }

            Context.INSTANCE.logger.errln("Unknown opcode: " + code);
            return OPCodes.NOOP;
        }
    }

    enum EInterrupts {
        NONE(0x0),
        END_OF_EX(0x1),
        PAUSE_EX(0x2);

        public final short code;

        EInterrupts(int code) {
            this.code = (short)code;
        }

        public static EInterrupts fromBytes(short code) {
            for (EInterrupts op : EInterrupts.values()) {
                if (op.code == code) return op;
            }

            Context.INSTANCE.logger.errln("Unknown interrupt: " + code);
            return EInterrupts.NONE;
        }
    }

    interface IInstructionHandler {
        void handle(InstructionPayload payload);
    }

    private IInstructionHandler[] instructionHandlers;
    private Register_16[] registers;

    // IO
    private final IIn_16 input;
    private final IOut_16 output;

    // interrupt
    private boolean interrupt;

    // general purpose registers
    private Register_16 r0;
    private Register_16 r1;
    private Register_16 r2;
    private Register_16 r3;

    private Register_16 accumulator0;
    private Register_16 accumulator1;

    private Register_16 flags;

    private Register_16 stackPointer;
    private Register_16 executionPointer;

    private Register_16 memoryAddrExtension;

    private Register_16 interruptCode;

    private final MemoryMapper_16 memoryMapper;

    private final Stack_16 stack;

    private final ALU_16 ALU;

    public CPU_16(IIn_16 input, IOut_16 output, MemoryMapper_16 memoryMapper) {

        this.input = input;
        this.output = output;
        this.memoryMapper = memoryMapper;

        this.InitRegisters();

        this.InitInstructionHandlers();

        this.ALU = new ALU_16();
        this.stack = new Stack_16(Short.MAX_VALUE, this.stackPointer);
    }

    /*
        | OPCODE | PARAM_0 | PARAM_1 |
        OPCODE:
        0 0 0 0 0 0 00000 0 0000
        - - - - - - ----- - ----
        a b c d e f g     h i

        a. 1 = use param0 as an int literal
        b. 1 = use param1 as an int literal
        c. 1 = use the value stored at the address in param0
        d. 1 = use the value stored at the address in param1
        e. 1 = the value of param0 is a memory address
        f. 1 = the value of param1 is a memory address
        (else it represents a fix location, such as the registers or the stack)
        g. logic operations
        h/i. math operations

        OPCODES:    NAME                        | PARAM0            | PARAM1        | NOTE
        0x0 -> 0xf  math operations               val 0               val 1           output0 to accumulator0, output1 to accumulator1, flags set
        0x10        jump if equal                 val 0               val 1           flags set, jump to address stored in r3 if true
        0x11        jump if less                  val 0               val 1           flags set, jump to address stored in r3 if true
        0x12        jump if less/equal            val 0               val 1           flags set, jump to address stored in r3 if true
        0x13        jump if greater               val 0               val 1           flags set, jump to address stored in r3 if true
        0x14        jump if greater/equal         val 0               val 1           flags set, jump to address stored in r3 if true
        0x20        move                          val 0               val 1           move the value tied to value 0 to the address tied to value 1
        0x21        interrupt                     interrupt code      -               triggers an interrupt
     */
    public void OnClock() {
        // DO STUFF

        if (interrupt) {
            this.ClearInterrupt();
            this.executionPointer.SetValue(DataEndpoints.MEM_INTERRUPT.address);
        } else {

            int ptr = this.executionPointer.GetValue();

            short opcode = memoryMapper.GetValue(this.executionPointer.GetValue());
            short param0 = memoryMapper.GetValue(this.executionPointer.Increment());
            short param1 = memoryMapper.GetValue(this.executionPointer.Increment());
            this.executionPointer.Increment();

            Context.INSTANCE.logger.logln("EXPTR: %d (%x) | [opcode]: %04x\t[0]: %04x\t[1]: %04x", ptr, ptr, ((int)opcode & 0xFFFF), ((int)param0 & 0xFFFF), ((int)param1 & 0xFFFF));

            boolean param0AsIntLit = (opcode & Masks.PARAM0_AS_INT_LIT.value) != 0;
            boolean param1AsIntLit = (opcode & Masks.PARAM1_AS_INT_LIT.value) != 0;
            boolean param0AsPointer = (opcode & Masks.PARAM0_AS_POINTER.value) != 0;
            boolean param1AsPointer = (opcode & Masks.PARAM1_AS_POINTER.value) != 0;
            boolean param0AsMemAddr = (opcode & Masks.PARAM0_AS_MEMORY_ADDR.value) != 0;
            boolean param1AsMemAddr = (opcode & Masks.PARAM1_AS_MEMORY_ADDR.value) != 0;

            short cleanOpcode = (short) (opcode & Masks.OPCODE.value);

            InstructionPayload payload = new InstructionPayload(
                    cleanOpcode,
                    param0, param1,
                    param0AsIntLit,  param1AsIntLit,
                    param0AsPointer, param1AsPointer,
                    param0AsMemAddr, param1AsMemAddr
            );

            if (cleanOpcode < this.instructionHandlers.length)
                this.instructionHandlers[cleanOpcode].handle(payload);
            else
                this.UnknownInstructionHandler(payload);

            Context.INSTANCE.logger.logln("");
        }
    }

    private void InitRegisters() {
        this.InitGeneralPurposeRegisters();
        this.InitDedicatedRegisters();

        this.registers = new Register_16[11];
        this.registers[DataEndpoints.R0.address] = this.r0;
        this.registers[DataEndpoints.R1.address] = this.r1;
        this.registers[DataEndpoints.R2.address] = this.r2;
        this.registers[DataEndpoints.R3.address] = this.r3;
        this.registers[DataEndpoints.R_ACCU0.address] = this.accumulator0;
        this.registers[DataEndpoints.R_ACCU1.address] = this.accumulator1;
        this.registers[DataEndpoints.R_FLAGS.address] = this.flags;
        this.registers[DataEndpoints.R_SPTR.address] = this.stackPointer;
        this.registers[DataEndpoints.R_EXPTR.address] = this.executionPointer;
        this.registers[DataEndpoints.R_MEM_EXTENSION.address] = this.memoryAddrExtension;
        this.registers[DataEndpoints.R_INTERRUPT_CODE.address] = this.interruptCode;
    }

    private void InitGeneralPurposeRegisters() {
        this.r0 = new Register_16((short) 0);
        this.r1 = new Register_16((short) 0);
        this.r2 = new Register_16((short) 0);
        this.r3 = new Register_16((short) 0);
    }

    private void InitDedicatedRegisters() {

        this.accumulator0 = new Register_16((short) 0);
        this.accumulator1 = new Register_16((short) 0);

        this.flags = new Register_16((short) 0);
        this.stackPointer = new Register_16((short) 0);
        this.memoryAddrExtension = new Register_16((short) 0);
        this.interruptCode = new Register_16((short) 0);

        // start execution at first memory byte
        this.executionPointer = new Register_16(DataEndpoints.MEM_START.address);
    }

    private void InitInstructionHandlers() {
        this.instructionHandlers = new IInstructionHandler[OPCodes.OP_COUNT.code];
        Arrays.fill(this.instructionHandlers, (IInstructionHandler)this::UnknownInstructionHandler);

        // math operations
        for (int i = 0; i < OPCodes.MATH_OPS.code; i++) {
            this.instructionHandlers[i] = this::MathInstructionHandler;
        }

        // jump instructions
        for (int i = OPCodes.JMP_EQ.code; i <= OPCodes.JMP_GREATER_EQ.code; i++) {
           this.instructionHandlers[i] = this::JumpInstructionHandler;
        }

        this.instructionHandlers[OPCodes.MOVE.code] = this::MoveInstructionHandler;
        this.instructionHandlers[OPCodes.INTERRUPT.code] = this::InterruptInstructionHandler;
    }

    private void UnknownInstructionHandler(InstructionPayload payload) {
        Context.INSTANCE.logger.errln("Unknown instruction: " + payload.opcode);
    }

    private void MathInstructionHandler(InstructionPayload payload) {

        short value0 = this.GetParamValue(payload.param0, payload.param0AsIntLit, payload.param0AsPointer, payload.param0AsMemAddr);
        short value1 = this.GetParamValue(payload.param1, payload.param1AsIntLit, payload.param1AsPointer, payload.param1AsMemAddr);

        ALU_16.Out out = this.ALU.compute(payload.opcode, value0, value1);

        this.accumulator0.SetValue(out.primaryResult());
        this.accumulator1.SetValue(out.secondaryResult());
        this.flags.SetValue(out.flags());


        // print everything
        Context.INSTANCE.logger.log(">\tMATH: %d (%04x) and %d (%04x) = %d (%04x)\n", ((int)value0 & 0xFFFF), ((int)value0 & 0xFFFF), ((int)value1 & 0xFFFF), ((int)value1 & 0xFFFF), ((int)out.primaryResult() & 0xFFFF), ((int)out.primaryResult() & 0xFFFF));
    }

    private void JumpInstructionHandler(InstructionPayload payload) {
        short value0 = this.GetParamValue(payload.param0, payload.param0AsIntLit, payload.param0AsPointer, payload.param0AsMemAddr);
        short value1 = this.GetParamValue(payload.param1, payload.param1AsIntLit, payload.param1AsPointer, payload.param1AsMemAddr);

        ALU_16.Out out = this.ALU.compute(payload.opcode, value0, value1);

        Context.INSTANCE.logger.log(">\tCMP: %d (%04x) and %d (%04x) = %b\n", ((int)value0 & 0xFFFF), ((int)value0 & 0xFFFF), ((int)value1 & 0xFFFF), ((int)value1 & 0xFFFF), out.cmpTrue());

        if (out.cmpTrue()) this.executionPointer.SetValue(this.r3.GetValue());

        this.flags.SetValue(out.flags());
    }

    private void MoveInstructionHandler(InstructionPayload payload) {
        short value0 = this.GetParamValue(payload.param0, payload.param0AsIntLit, payload.param0AsPointer, payload.param0AsMemAddr);
        short value1 = this.GetAddress(payload.param1, payload.param1AsPointer);

        Context.INSTANCE.logger.log(">\tMOVE: %d (%04x) to %d (%04x)\n", ((int)value0 & 0xFFFF), ((int)value0 & 0xFFFF), ((int)value1 & 0xFFFF), ((int)value1 & 0xFFFF));

        this.SetValueAtAddress(value0, value1, payload.param1AsMemAddr || payload.param1AsPointer);
    }

    private void InterruptInstructionHandler(InstructionPayload payload) {
        short value0 = this.GetParamValue(payload.param0, payload.param0AsIntLit, payload.param0AsPointer, payload.param0AsMemAddr);

        this.interruptCode.SetValue(value0);
        this.interrupt = true;
    }

    private short GetAddress(short value, boolean asPointer) {
        if (asPointer) {
            return this.GetValueAtAddress(value, false);
        }

        return value;
    }

    private short GetParamValue(short value, boolean asIntLit, boolean asPointer, boolean isMemAddr) {
        if (asIntLit) return value;
        else if (asPointer) {
            short address = this.GetValueAtAddress(value, false);
            return this.GetValueAtAddress(address, true);
        }
        else if (isMemAddr) {
            return this.GetValueAtAddress(value, true);
        }

        else return this.GetValueAtAddress(value, false);
    }

    private short GetValueAtAddress(short address, boolean isMemAddr) {
        if (isMemAddr) {
            return this.memoryMapper.GetValue(this.memoryAddrExtension.GetValue(), address);
        }
        else if (address >= 0 && address < registers.length) {
            return registers[address].GetValue();
        }
        else if (address == DataEndpoints.STACK.address) {
            return this.stack.Pop();
        }
        else if (address == DataEndpoints.IN.address) {
            return this.input.GetValue();
        }

        return 0;
    }

    private void SetValueAtAddress(short value, short address, boolean isMemAddr) {
        if (isMemAddr) {
            this.memoryMapper.SetValue(this.memoryAddrExtension.GetValue(), address, value);
        }
        else if (address >= 0 && address < registers.length) {
            registers[address].SetValue(value);
        }
        else if (address == DataEndpoints.STACK.address) {
            this.stack.Push(value);
        }
        else if (address == DataEndpoints.OUT.address) {
            this.output.SetValue(value);
        }
        else {
            Context.INSTANCE.logger.errln("value " + value + " going nowhere: address = " + address);
        }
    }

    public void Interrupt(short interruptCode) {
        this.interrupt = true;
        this.interruptCode.SetValue(interruptCode);
    }

    private void ClearInterrupt() {
        this.interrupt = false;
    }
}
