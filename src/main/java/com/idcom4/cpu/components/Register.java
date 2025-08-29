package com.idcom4.cpu.components;

public abstract class Register<T extends Number> {

    protected T value;

    public Register(T value) {
        this.value = value;
    }

    public T GetValue() {
        return value;
    }

    public void SetValue(T value) {
        this.value = value;
    }

    abstract public T Increment();

    abstract public T Decrement();
}
