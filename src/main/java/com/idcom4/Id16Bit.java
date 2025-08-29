package com.idcom4;

import com.idcom4.bios.BIOS;
import com.idcom4.cpu.CPU_16;
import com.idcom4.cpu.components.MemoryMapper_16;


public class Id16Bit {

    private final CPU_16 CPU;
    private boolean running = false;
    private boolean stepForward = false;
    private int msSteps = 0;

    public Id16Bit(MemoryMapper_16 memoryMapper, BIOS bios) {
        this.CPU = new CPU_16(bios.IN, bios.OUT, memoryMapper);

        StartRunningThread();
    }

    public void Start() {
        this.running = true;
    }

    public void Pause() {
        this.running = false;
    }

    public void StepForward() {
        this.Pause();
        this.stepForward = true;
    }

    public void SetMSStep(int ms) {
        this.msSteps = ms;
    }

    private void StartRunningThread() {
        Thread runningThread = new Thread(() -> {
            while (true) {
                if (running || stepForward) {
                    if (stepForward) stepForward = false;

                    CPU.OnClock();

                    // handle sleep
                    if (msSteps > 0) {
                        try {
                            Thread.sleep(msSteps);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });

        runningThread.start();
    }

}
