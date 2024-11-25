package edu.mondragon.os.wastent;

import java.util.Random;

public class Machine extends Thread {
    
    private Random rand;
    private Monitor monitor;

    public Machine(int id) {
        super("Machine " + id);
        this.rand = new Random();
        this.monitor = null;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(rand.nextInt(10));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
}
