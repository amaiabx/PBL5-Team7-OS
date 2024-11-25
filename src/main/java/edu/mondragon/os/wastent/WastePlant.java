package edu.mondragon.os.wastent;

public class WastePlant {

    boolean[] scanning;

    public WastePlant(int capacity) {
        scanning = new boolean[capacity];
        for (boolean b : scanning) {
            b = false;
        }
    }

    public void arriveContainer(Container container) throws InterruptedException {
        container.arrive();
        container.beAssigned();
        container.beDumped();
    }

    public void turnMachineOn(Monitor monitor) throws InterruptedException {
        monitor.turnMachineOn();
    }

    public void turnMachineOff(Monitor monitor) throws InterruptedException {
        monitor.turnMachineOff();
    }

    public void scanItem(Machine machine) throws InterruptedException {
        machine.startScan();
        machine.finishScan();
    }
}