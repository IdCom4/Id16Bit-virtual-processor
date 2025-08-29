package com.idcom4.cpu.components;

import com.idcom4.Context;

public class MemoryBlock_16 {

    private final int size;
    private final short[] content;
    private final String name;
    private final boolean readOnly;

    public MemoryBlock_16(String name, boolean readonly, int size) {

        this.name = name;
        this.readOnly = readonly;
        this.size = size;
        this.content = new short[size];
    }

    public MemoryBlock_16(String name, boolean readonly, short[] content) {

        this.name = name;
        this.readOnly = readonly;
        this.size = content.length;
        this.content = content;
    }

    public short GetValue(int address) {
        if (this.IsAddressOutOfBound(address)) return 0;
        return content[address];
    }

    public void SetValue(int address, short value) {
        if (this.readOnly || this.IsAddressOutOfBound(address)) return;
        content[address] = value;
    }

    public int GetSize() {
        return size;
    }

    public short[] GetContent() {
        return content;
    }

    private boolean IsAddressOutOfBound(int address) {
        if (address < 0 || address >= size) {
            Context.INSTANCE.logger.errln("Memory block \"" + this.name + "\" address overflow: " + address + " [size: " + size + "]");
            return true;
        }

        return false;
    }

    public String  GetName() {
        return name;
    }
    public boolean IsReadOnly() {
        return readOnly;
    }
}
