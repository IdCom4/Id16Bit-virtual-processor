package com.idcom4.cpu.components;

import com.idcom4.Context;
import com.idcom4.utils.logger.ILogger;

public class ALU_16 {
    
    public record Out(short primaryResult, short secondaryResult, short flags, boolean cmpRequested, boolean cmpTrue) {}

    /*
        OPCODE:

        00000000000 0 0000
        ----------- - ----
        a           b c

        a. unused by the ALU
        b. comparison requested
        c. operation (0x0 -> 0xf = 16)
     */
    public enum Operations {
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
        RIGHT_SHIFT(0xa);

        public final short code;

        private Operations(int code) {
            this.code = (short)code;
        }

        public static Operations fromBytes(short code) {
            for (Operations op : Operations.values()) {
                if (op.code == code) return op;
            }

            Context.INSTANCE.logger.errln("Unknown operation: " + code);
            return Operations.NOOP;
        }
    }

    public enum Comparisons {
        EQUAL(0x0),
        LESS(0x1),
        LESS_OR_EQUAL(0x2),
        GREATER(0x3),
        GREATER_OR_EQUAL(0x4);

        public final short code;

        private Comparisons(int code) {
            this.code = (short)code;
        }

        public static Comparisons fromBytes(short code) {
            for (Comparisons op : Comparisons.values()) {
                if (op.code == code) return op;
            }

            Context.INSTANCE.logger.errln("Unknown comparison: " + code);
            return Comparisons.EQUAL;
        }
    }


    public Out compute(short opcode, short input0, short input1) {

        boolean cmpRequested = (opcode & 0b00010000) != 0;

        if (cmpRequested) {
            Comparisons comparison = Comparisons.fromBytes((short) (opcode & 0b00001111));
            short flags = SetFlags(input0, input1, input0, input1);

            boolean cmpTrue = switch (comparison) {
                case EQUAL -> input0 == input1;
                case LESS -> input0 < input1;
                case LESS_OR_EQUAL -> input0 <= input1;
                case GREATER -> input0 > input1;
                case GREATER_OR_EQUAL -> input0 >= input1;
            };

            return new Out((short) 0, (short) 0, flags, true, cmpTrue);
        }
        else {

            Operations operation = Operations.fromBytes((short) (opcode & 0b00001111));

            return switch (operation) {
                case Operations.NOOP ->         CreateOperationOut(input0, input1, input0, input1);
                case Operations.ADD ->          CreateOperationOut(input0, input1, (short) (input0 + input1), (short) 0);
                case Operations.SUBSTRACT ->    CreateOperationOut(input0, input1, (short) (input0 - input1), (short) 0);
                case Operations.MULTIPLY ->     CreateOperationOut(input0, input1, (short) (input0 * input1), (short) 0);
                case Operations.DIVIDE ->       CreateOperationOut(input0, input1, (short) (input1 == 0 ? -1 : input0 / input1), (short) (input1 == 0 ? 0 : input0 % input1));
                case Operations.MODULO ->       CreateOperationOut(input0, input1, (short) (input1 == 0 ? 0 : input0 % input1), (short) (input1 == 0 ? -1 : input0 / input1));
                case Operations.AND ->          CreateOperationOut(input0, input1, (short) (input0 & input1), (short) 0);
                case Operations.OR ->           CreateOperationOut(input0, input1, (short) (input0 | input1), (short) 0);
                case Operations.XOR ->          CreateOperationOut(input0, input1, (short) (input0 ^ input1), (short) 0);
                case Operations.LEFT_SHIFT ->   CreateOperationOut(input0, input1, (short) (input0 << input1), (short) 0);
                case Operations.RIGHT_SHIFT ->  CreateOperationOut(input0, input1, (short) (input0 >> input1), (short) 0);
            };
        }
    }

    private Out CreateOperationOut(short input0, short input1, short output0, short output2) {
        short flags = SetFlags(input0, input1, output0, output2);
        return new Out(output0, output2, flags, false, false);
    }

    /*
        FLAGS:
        0 0 0 0 0 0 0 0
        a b c d e f g h

        a. input0 == input1
        b. input0 < input1
        c. output0 == 0
        d. output0 < 0
        e. output0 > 0
        f. output1 == 0
        g. output1 < 0
        h. output1 > 0

     */
    private short SetFlags(short input0, short input1, short output0, short output1) {
        // input0 == input1
        short flags = (short) (input0 == input1 ? 1 : 0);
        // input0 < input1
        flags = (short) (flags << 1 | ((input0 < input1) ? 0 : 1));

        // output0 == 0
        flags = (short) (flags << 1 | ((output0 == 0) ? 0 : 1));
        // output0 < 0
        flags = (short) (flags << 1 | ((output0 < 0) ? 0 : 1));
        // output0 > 0
        flags = (short) (flags << 1 | ((output0 > 0) ? 0 : 1));

        // output1 == 0
        flags = (short) (flags << 1 | ((output1 == 0) ? 0 : 1));
        // output1 < 0
        flags = (short) (flags << 1 | ((output1 < 0) ? 0 : 1));
        // output1 > 0
        flags = (short) (flags << 1 | ((output1 > 0) ? 0 : 1));

        return flags;
    }

}
