package edu.mondragon.os.wastent;

import java.util.Random;

public class Monitor extends Thread {
        
    private Random rand;

    public Monitor(int id) {
        super("Monitor " + id);
        this.rand = new Random();
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
