package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;
    private Item current;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore isOff;
    private Semaphore machineAvailable;
    private Semaphore scanReady;
    private Semaphore inScan;
    private Semaphore scanDone;
    private Semaphore canFinish;
    private Semaphore readyToTurnOff;
    private Semaphore canLeave;


    public WastePlant(List<Machine> machines, int capacity) {
        this.machines = machines;
        this.current = null;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        isOff = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        scanReady = new Semaphore(0, true); // FIFO
        inScan = new Semaphore(0);
        scanDone = new Semaphore(0);
        canFinish = new Semaphore(0);
        readyToTurnOff = new Semaphore(0);
        canLeave = new Semaphore(0);
    }

    private Machine findAvailableMachine() throws InterruptedException {
        Machine found = null;
        int smallest = Integer.MAX_VALUE;

        // Wait until a machine has been turned on

        machineAvailable.acquire();
        machineAvailable.release();

        mutex.acquire();
        for (Machine machine : machines) {
            if (machine.isOn()) {
                // Assign the machine that is the least busy
                int size = machine.getItemList().size();
                if (size < smallest) {
                    smallest = size;
                    found = machine;
                }
            }
        }
        mutex.release();

        return found;
    }

    public void arriveContainer(Container container) throws InterruptedException {
        Machine found;

        
        found = findAvailableMachine();
        container.setMachine(found);
        
        mutex.acquire();

        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " contains items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        for (Item item : items) {
            found.addItem(item);
        }

        mutex.release();

        for (Item item : items) {
            item.start();
        }
    }

    public void readyToScan() throws InterruptedException {
        scanReady.release();
        inScan.acquire();
        current = (Item) Thread.currentThread();
    }

    public void beTurnedOn(Machine machine) throws InterruptedException {
        isOn.acquire();
        machineAvailable.release();
        machine.setOn(true);
    }

    public void beTurnedOff(Machine machine) throws InterruptedException {
        isOff.acquire();
        machineAvailable.acquire();
        machine.setOn(false);
    }

    public void turnMachineOn() {
        isOn.release();
    }

    public void waitToTurnMachineOff() throws InterruptedException {
        readyToTurnOff.acquire();
    }

    public Item scanItem() throws InterruptedException {
        scanReady.acquire();
        inScan.release();
        scanDone.release();
        canFinish.acquire();

        return current;
    }

    public void itemScanned(Machine machine) throws InterruptedException {
        mutex.acquire();
        machine.removeItem(current);
        mutex.release();
        canLeave.release();

        if (machine.getItemList().isEmpty()) {
            readyToTurnOff.release();
        }
    }

    public void finishScan() throws InterruptedException {
        scanDone.acquire();
        canFinish.release();
        canLeave.acquire();
    }
}