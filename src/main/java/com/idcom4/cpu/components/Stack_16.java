package com.idcom4.cpu.components;

public class Stack_16 extends MemoryBlock_16 {

    private final Register_16 stackPointer;

    public Stack_16(int size, Register_16 stackPointer) {
        super("Stack", false, size);

        this.stackPointer = stackPointer;
    }

    public void Push(short value) {
        short pointer = stackPointer.GetValue();
        this.SetValue(pointer, value);

        stackPointer.SetValue((short)(pointer + 1));
    }

    public short Pop() {
        stackPointer.SetValue((short) (stackPointer.GetValue() - 1));

        return this.GetValue(stackPointer.GetValue());
    }
}
