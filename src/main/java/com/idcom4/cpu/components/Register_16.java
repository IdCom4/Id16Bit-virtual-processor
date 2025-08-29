package com.idcom4.cpu.components;

public class Register_16 extends Register<Short> {

    public Register_16() {
        super((short) 0);
    }

    public Register_16(short value) {
        super(value);
    }

    @Override
    public Short Increment() {
        this.value = (short) (this.value + 1);
        return this.value;
    }

    @Override
    public Short Decrement() {
        this.value = (short) (this.value - 1);
        return this.value;
    }
}
