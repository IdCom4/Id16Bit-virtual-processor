package com.idcom4.cpu.components;

import com.idcom4.Context;
import com.idcom4.utils.Pair;
import com.idcom4.utils.logger.ILogger;

import java.util.Arrays;
import java.util.Optional;

public class MemoryMapper_16 extends MemoryBlock_16 {

    MemoryBlock_16[] memoryBlocks;

    public MemoryMapper_16(MemoryBlock_16[] memoryBlocks) {
        // reduce all memory blocks size
        super("MemoryMapper", false, Arrays.stream(memoryBlocks).map(MemoryBlock_16::GetSize).reduce(0, Integer::sum));
        this.memoryBlocks = memoryBlocks;
    }

    @Override
    public short GetValue(int address) {
        var optional = this.GetMemoryBlockFromAddress(address);
        if (optional.isEmpty()) return 0x0;

        Pair<MemoryBlock_16, Integer> memoryData = optional.get();
        MemoryBlock_16 block = memoryData.First;
        Integer blockRelativeAddress = memoryData.Second;

        return block.GetValue(blockRelativeAddress);
    }

    public short GetValue(short addrHigh, short addrLow) {

        return this.GetValue(GetFullAddress(addrHigh, addrLow));
    }

    @Override
    public void SetValue(int address, short value) {
        var optional = this.GetMemoryBlockFromAddress(address);
        if (optional.isEmpty()) return;

        Pair<MemoryBlock_16, Integer> memoryData = optional.get();
        MemoryBlock_16 block = memoryData.First;
        Integer blockRelativeAddress = memoryData.Second;

        block.SetValue(blockRelativeAddress, value);
    }

    public void SetValue(short addrHigh, short addrLow, short value) {
        this.SetValue(GetFullAddress(addrHigh, addrLow), value);
    }

    private int GetFullAddress(short addrHigh, short addrLow) {
        return (addrHigh << 16) | ((int)addrLow & 0xFFFF);
    }

    private Optional<Pair<MemoryBlock_16, Integer>>  GetMemoryBlockFromAddress(int address) {
        for (MemoryBlock_16 memoryBlock : memoryBlocks) {
            if (address < memoryBlock.GetSize()) {
                return Optional.of(new Pair<>(memoryBlock, address));
            }

            address -= memoryBlock.GetSize();
        }

        Context.INSTANCE.logger.errln("Memory Block not found for address: %s (%04x)\n", address, address);
        return Optional.empty();
    }

    public MemoryBlock_16[] GetMemoryBlocks() {
        return memoryBlocks;
    }
}
