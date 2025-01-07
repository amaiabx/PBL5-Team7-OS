package edu.mondragon.os.wastent;

import java.security.SecureRandom;

public class Monitor extends Thread {
        
    private SecureRandom rand;
    private WastePlant wastePlant;

    public Monitor(WastePlant wastePlant, int id) {
        super("👷 Monitor " + id);
        this.wastePlant = wastePlant;
        this.rand = new SecureRandom();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(rand.nextInt(500));
            System.out.println(this.getName() + " is turning a machine on");
            Thread.sleep(rand.nextInt(1000, 2000));
            wastePlant.turnMachineOn();
            Thread.sleep(rand.nextInt(7000, 13000));
            System.out.println(this.getName() + " is waiting to turn a machine off");
            wastePlant.waitToTurnMachineOff();
        } catch (InterruptedException e) {
            this.interrupt();
        }
        
    }
}
