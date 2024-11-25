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

    public void beTurnedOn() throws InterruptedException {
        System.out.println(this.getName() + " has been turned on");
    }
    
    public void startScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " is scanning an item");
        Thread.sleep(rand.nextInt(30) + 3000);
    }

    public void finishScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " has finished scanning the item");
    }

    public void beTurnedOff() throws InterruptedException {
        System.out.println(this.getName() + " has been turned off");
    }
}
