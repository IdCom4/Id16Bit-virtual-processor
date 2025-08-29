package com.idcom4.bios;

import com.idcom4.cpu.components.IIn_16;
import com.idcom4.cpu.components.IOut_16;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BIOS {

    private final BlockingQueue<Character> inputQueue;

    private int buildersIndex = 0;
    private final List<StringBuilder> builders = new ArrayList<>(Collections.singleton(new StringBuilder(8192)));
    public final IOut_16 OUT;
    public final IIn_16 IN;

    public BIOS() {
        this.inputQueue = StartNonBlockingUserInputReading();


        this.IN = () -> {
            Character c = inputQueue.poll();
            if (c == null) return (short) 0;
            return (short)c.charValue();
        };


        this.OUT = (short value) -> {
            // get current string builder
            StringBuilder sb = builders.get(buildersIndex);

            // if PRINT_SIG
            if (value == -1) {
                // print the content of each non-empty string builders
                for (StringBuilder _sb : builders) {
                    if (!_sb.isEmpty()) {
                        System.out.print(sb);
                        sb.setLength(0);
                    }
                }

                // and reset index
                buildersIndex = 0;
            }
            // if new char to store
            else {
                // check if it can be stored in current string builder
                if (sb.length() >= sb.capacity() - 1) {
                    // if not create a new sb if needed
                    if (buildersIndex >= builders.size() - 1) {
                        sb = this.GetNewStringBuilder(sb.capacity());
                        builders.add(sb);
                    }
                    // or use a previously created one
                    else {
                        sb = builders.get(++buildersIndex);
                    }
                }

                // store the char in it
                sb.append((char) value);
            }
        };
    }

    private static BlockingQueue<Character> StartNonBlockingUserInputReading() {
        BlockingQueue<Character> inputQueue = new LinkedBlockingQueue<>();

        Thread readerThread = new Thread(() -> {
            try {
                int ch;
                while ((ch = System.in.read()) != -1) {
                    inputQueue.offer((char) ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        return inputQueue;
    }

    private StringBuilder GetNewStringBuilder(int size) {
        return new StringBuilder(size);
    }
}
