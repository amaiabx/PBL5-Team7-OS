package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;
    private Item currentItem, cur;
    private Machine currentMachine;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore isOff;
    private Semaphore machineAvailable;
    private Semaphore scanReady;
    private Semaphore inScan;
    private Semaphore scanDone;
    private Semaphore canFinish;
    private Semaphore readyToTurnOff;
    private Semaphore waitToTurnOff;
    private Semaphore canLeave;


    public WastePlant(List<Machine> machines, int capacity) {
        this.machines = machines;
        this.currentItem = null;
        this.cur = null;
        this.currentMachine = null;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        isOff = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        scanReady = new Semaphore(0, true); // FIFO
        inScan = new Semaphore(0);
        scanDone = new Semaphore(0);
        canFinish = new Semaphore(0);
        readyToTurnOff = new Semaphore(0);
        waitToTurnOff = new Semaphore(2);
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
            if (machine.isOn() && !machine.isNoMore()) {
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
        found.setCanBeOff(false);
        
        mutex.acquire();
        
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }


        container.setMachine(found);
        

        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " has arrived containing items [" +
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
        mutex.acquire();
        cur = (Item) Thread.currentThread();
        scanReady.release();
        mutex.release();
        inScan.acquire();
        currentItem = (Item) Thread.currentThread();
    }

    public void beTurnedOn(Machine machine) throws InterruptedException {
        isOn.acquire();
        machineAvailable.release();
        machine.setOn(true);
    }

    public void beTurnedOff(Machine machine) throws InterruptedException {
        machineAvailable.acquire();
        machine.setOn(false);
        System.out.println(machine.getName() + " was turned off");
    }

    public void turnMachineOn() {
        isOn.release();
    }

    public Item scanItem(Machine machine) throws InterruptedException {
        mutex.acquire();
        // If the item belongs to the machine, scan it
        if (machine.getItemList().contains(cur)) {
            mutex.release();
            scanReady.acquire();
            inScan.release();
            scanDone.release();
            canFinish.acquire();
            return currentItem;
        }
        mutex.release();
        return null;
    }

    public void itemScanned(Machine machine) throws InterruptedException {
        machine.removeItem(currentItem);
        canLeave.release();

        System.out.println("\t\t" + machine.getName() + " items left to scan [" +
        machine.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        if (machine.getItemList().isEmpty()) {
            machine.setCanBeOff(true);
            waitToTurnOff.release();
        }
    }

    public void waitToTurnMachineOff() throws InterruptedException {
        waitToTurnOff.acquire();

        mutex.acquire();

        for (Machine machine : machines) {
            // If the machine isn't scanning anything, it can be turned off
            if (machine.isCanBeOff() && machine.isOn()) {
                machine.setNoMore(true);
                currentMachine = machine;
            }
        }
        if (currentMachine != null) beTurnedOff(currentMachine);
        
        mutex.release();
    }

    public void finishScan() throws InterruptedException {
        scanDone.acquire();
        canFinish.release();
        canLeave.acquire();
    }
}