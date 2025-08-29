package com.idcom4;

import com.idcom4.bios.BIOS;
import com.idcom4.cpu.components.MemoryMapper_16;
import com.idcom4.exceptions.IDException;
import com.idcom4.infra.options.Options;
import com.idcom4.infra.options.OptionsManager;
import com.idcom4.infra.options.OptionsPrinter;
import com.idcom4.mem_manager.MemoryManager;
import com.idcom4.utils.logger.ConsoleLogger;


public class Main {
    public static void main(String[] args) throws IDException {

        // get options
        Options options = OptionsManager.GetOptions(args);

        // print help if asked
        if (options.GetHelp()) {
            OptionsPrinter.PrintAvailableOptions();
            return ;
        }

        // validate that there is at least a mmap file
        if (options.GetMmapFile() == null)
            throw new IDException("no mmap file provided");

        // setup logger and context
        ConsoleLogger logger = new ConsoleLogger();
        logger.setEnabled(options.GetLog());
        Context.initContext(logger);

        // init memory
        MemoryMapper_16 memoryMapper = MemoryManager.CreateMemoryMapper(options.GetMmapFile());

        // init computer
        Id16Bit computer = new Id16Bit(memoryMapper, new BIOS());

        // set delay if any
        if (options.GetDelay() > 0)
            computer.SetMSStep(options.GetDelay());

        // handle Ctrl + C SIGINT to save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            computer.Pause();
            try {
                MemoryManager.SaveMemoryMapper(memoryMapper, options.GetMmapFile());
            } catch (IDException e) {
                throw new RuntimeException(e);
            }
        }));

        // start the computer
        computer.Start();
    }
}