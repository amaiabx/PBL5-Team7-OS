package edu.mondragon.os.wastent;

import java.util.Random;

public class Monitor extends Thread {
        
    private Random rand;
    private WastePlant wastePlant;

    public Monitor(WastePlant wastePlant, int id) {
        super("Monitor " + id);
        this.wastePlant = wastePlant;
        this.rand = new Random();
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(rand.nextInt(30));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public void turnMachineOn() throws InterruptedException {
        System.out.println(this.getName() + " is turning a machine on");
        Thread.sleep(rand.nextInt(30) + 3000);
    }

    public void turnMachineOff() throws InterruptedException {
        System.out.println(this.getName() + " is turning a machine off");
    }
}
