package com.idcom4.mem_manager;

import com.idcom4.Context;
import com.idcom4.cpu.components.MemoryBlock_16;
import com.idcom4.cpu.components.MemoryMapper_16;
import com.idcom4.exceptions.IDException;
import com.idcom4.utils.FileUtils;
import com.idcom4.utils.Json;
import com.idcom4.utils.logger.ILogger;

import java.util.Arrays;

public class MemoryManager {

    record MemBlockData(String name, int size, boolean readonly, boolean persistent, String stateFile) {}

    public static MemoryMapper_16 CreateMemoryMapper(int size) throws IDException {
        try {
            return new MemoryMapper_16(new MemoryBlock_16[] { new MemoryBlock_16("_default", false, size) });
        } catch (Exception e) {
            throw new IDException("Couldn't create the memory mapper", e);
        }
    }

    public static MemoryMapper_16 CreateMemoryMapper(String memMapFileName) throws IDException {
        try {
            MemBlockData[] memBlocksData = GetMemBlockData(memMapFileName);
            if (memBlocksData.length == 0) {
                throw new IDException("Couldn't create the memory mapper, no memory blocks found");
            }

            MemoryBlock_16[] memBlocks = new MemoryBlock_16[memBlocksData.length];

            for (int i = 0; i < memBlocks.length; i++) {
                memBlocks[i] = CreateMemoryBlock(memBlocksData[i]);
            }

            return new MemoryMapper_16(memBlocks);

        } catch (Exception e) {
            throw new IDException("Couldn't create the memory mapper", e);
        }
    }

    private static MemoryBlock_16 CreateMemoryBlock(MemBlockData blockData) {
        if (blockData.stateFile == null) {
            if (blockData.persistent) {
                Context.INSTANCE.logger.errln("[ERR] No state file provided for memory block " + blockData.name + ", setting it as non persistent");
            }

            return new MemoryBlock_16(blockData.name, blockData.readonly, blockData.size);
        }
        else {
            short[] initialState = FileUtils.ReadFileShorts(blockData.stateFile);


            return new MemoryBlock_16(blockData.name, blockData.readonly, initialState);
        }
    }

    public static void SaveMemoryMapper(MemoryMapper_16 memMapper, String memMapFileName) throws IDException {
        try {
            MemBlockData[] memBlocksData = GetMemBlockData(memMapFileName);

            for (MemBlockData memBlockData : memBlocksData) {
                if (memBlockData.persistent && memBlockData.stateFile != null) {
                    MemoryBlock_16 memBlock =
                            Arrays.stream(memMapper.GetMemoryBlocks())
                                    .filter((block) -> block.GetName().equals(memBlockData.name))
                                    .findFirst().orElse(null);

                    if (memBlock == null) continue;

                    SaveMemoryBlock(memBlock, memBlockData.stateFile);
                }
            }
        } catch (Exception e) {
            throw new IDException("Couldn't save the memory mapper", e);
        }
    }

    private static void SaveMemoryBlock(MemoryBlock_16 memBlock, String stateFile) throws IDException {
        FileUtils.WriteFileShorts(stateFile, memBlock.GetContent());
    }

    private static MemBlockData[] GetMemBlockData(String memMapFileName) throws IDException {
        String jsonMemMap = FileUtils.ReadFile(memMapFileName);
        return Json.Deserialize(jsonMemMap, MemBlockData[].class);
    }

}
