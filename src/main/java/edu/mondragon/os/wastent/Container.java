package edu.mondragon.os.wastent;

import java.util.Random;

public class Container extends Thread {

    private Machine machine;
    private Random rand;

    public Container(int id) {
        super("Container " + id);
        this.rand = new Random();
        this.machine = null;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
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
