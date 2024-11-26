package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    boolean[] scanning;
    List<Machine> machines;
    List<Machine> machineOn;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore isOff;
    private Semaphore machineAvailable;
    private Semaphore inScan;
    private Semaphore scanReady;
    private Semaphore scanDone;


    public WastePlant(List<Machine> machines, int capacity) {
        this.machines = machines;
        this.scanning = new boolean[capacity];
        for (boolean b : scanning) {
            b = false;
        }

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        isOff = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        inScan = new Semaphore(0);
        scanReady = new Semaphore(0);
        scanDone = new Semaphore(0);
        machineOn = new ArrayList<>();
    }

    private Machine findAvailableMachine() throws InterruptedException {
        Machine found = null;
        int smallest = 100;

        machineAvailable.acquire();

        for (Machine machine : machines) {
            if (machine.isOn()) {
                // It assigns the machine that is on and is the least busy
                int size = machine.getItemList().size();
                if (size < smallest) {
                    size = smallest;
                    found = machine;
                }
            }
        }

        return found;
    }

    public void arriveContainer(Container container) throws InterruptedException {
        mutex.acquire();
        container.setMachine(findAvailableMachine());
        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " contains items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");

        mutex.release();

        for (Item item : items) {
            item.start();
        }
    }

    public void readyToScan() throws InterruptedException {
        scanReady.release();
        inScan.acquire();
    }

    public void beTurnedOn(Machine machine) throws InterruptedException {
        isOn.acquire();
        System.out.println(Thread.currentThread().getName() + " was turned on");
        machineAvailable.release();
        machine.setOn(true);
    }

    public void beTurnedOff(Machine machine) throws InterruptedException {
        isOff.acquire();
        System.out.println(Thread.currentThread().getName() + " was turned off");
        machine.setOn(false);
    }

    public void turnMachineOn() throws InterruptedException {
        isOn.release();
    }

    public void turnMachineOff() throws InterruptedException {
        scanDone.acquire();
    }

    public void scanItem() throws InterruptedException {
        scanReady.acquire();
        inScan.release();
        scanDone.release();
    }
}