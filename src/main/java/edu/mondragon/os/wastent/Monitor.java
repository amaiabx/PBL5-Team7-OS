package edu.mondragon.os.wastent;

import java.util.Random;

public class Monitor extends Thread {
        
    private Random rand;
    private WastePlant wastePlant;

    public Monitor(WastePlant wastePlant, int id) {
        super("👷 Monitor " + id);
        this.wastePlant = wastePlant;
        this.rand = new Random();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(rand.nextInt(500));
            System.out.println(this.getName() + " is turning a machine on");
            Thread.sleep(rand.nextInt(1000, 2000));
            wastePlant.turnMachineOn();
            Thread.sleep(rand.nextInt(5000, 10000));
            System.out.println(this.getName() + " is waiting to turn a machine off");
            wastePlant.waitToTurnMachineOff();
        } catch (InterruptedException e) {
            this.interrupt();
        }
        
    }
}
