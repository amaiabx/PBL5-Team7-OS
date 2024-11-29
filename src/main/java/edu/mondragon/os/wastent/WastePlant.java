package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;
    private Item currentItem;
    private Item cur;
    private Machine currentMachine;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore machineAvailable;
    private Semaphore scanReady;
    private Semaphore inScan;
    private Semaphore scanDone;
    private Semaphore canFinish;
    private Semaphore waitToTurnOff;
    private Semaphore canLeave;
    private Semaphore left;

    public WastePlant(List<Machine> machines, int nMachines) {
        this.machines = machines;
        this.currentItem = null;
        this.cur = null;
        this.currentMachine = null;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        scanReady = new Semaphore(0, true); // FIFO
        inScan = new Semaphore(0);
        scanDone = new Semaphore(0);
        canFinish = new Semaphore(0);
        waitToTurnOff = new Semaphore(nMachines);
        canLeave = new Semaphore(0);
        left = new Semaphore(0);
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

        if (found == null) {
            turnMachineOn();
            beTurnedOn(machines.get(0));
            found = machines.get(0);
        }

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
        scanReady.release();
        cur = (Item) Thread.currentThread();
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
        Item current;
        current = cur;
        mutex.acquire();
        // If the item belongs to the machine, scan it
        if (machine.getItemList().contains(current)) {
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
        mutex.acquire();

        // Remove scanned items from list
        for (Machine m : machines) {
            if (m.isOn()) {
                for (int i = 0; i < m.getItemList().size(); i++) {
                    Item item = m.getItemList().get(i);
                    if (item.getCategory() != null) {
                        m.removeItem(item);
                    }
                }
            }
        }

        mutex.release();

        canLeave.release();
        left.acquire();

        mutex.acquire();

        for (Machine m : machines) {
            if (m.isOn()) {
                System.out.println("\t\t" + m.getName() + " items left to scan [" +
                m.getItemList().stream()
                    .map(item -> item.getName().split("Item ")[1]) // Print only the number
                    .collect(Collectors.joining(", "))
                + "]");
            }

            if (m.getItemList().isEmpty() && !m.isCanBeOff()) {
                m.setCanBeOff(true);
                waitToTurnOff.release();
            }
        }

        mutex.release();
        
        
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
        left.release();
    }
}