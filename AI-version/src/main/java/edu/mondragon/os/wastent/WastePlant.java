package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;
    private Machine currentMachine;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore machineAvailable;
    private Semaphore[] scanReady;
    private Semaphore[] inScan;
    private Semaphore[] scanDone;
    private Semaphore[] canFinish;
    private Semaphore waitToTurnOff;
    private Semaphore[] canLeave;
    private Semaphore[] left;

    public WastePlant(List<Machine> machines, int nMachines) {
        this.machines = machines;
        this.currentMachine = null;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        machineAvailable = new Semaphore(0);

        // FIFO, the item that is ready to be scanned first is scanned first
        scanReady = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            scanReady[i] = new Semaphore(0, true);
        }

        inScan = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            inScan[i] = new Semaphore(0);
        }

        scanDone = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            scanDone[i] = new Semaphore(0);
        }

        canFinish = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            canFinish[i] = new Semaphore(0);
        }

        waitToTurnOff = new Semaphore(nMachines);

        canLeave = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            canLeave[i] = new Semaphore(0);
        }

        left = new Semaphore[nMachines];
        for (int i=0; i < nMachines; i++) {
            left[i] = new Semaphore(0);
        }
    }

    public Machine findAvailableMachine() throws InterruptedException {
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

        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " has arrived containing items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        // Find a machine to dump the container in
        found = findAvailableMachine();
        found.setCanBeOff(false);
        
        mutex.acquire();
        
        // If it was empty, as it gets items it can't be turned off anymore
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }

        container.setMachine(found);
        
        // Add the items to the machine
        for (Item item : items) {
            found.addItem(item);
        }

        mutex.release();

        for (Item item : items) {
            item.start();
        }
    }

    public void readyToScan(Item item) throws InterruptedException {
        // An item is ready to be scanned
        int id = -1;
        mutex.acquire();
        for (Machine m : machines) {
            if (m.getItemList().contains(item)) {
                id = (int) m.getId();
            }
        }
        scanReady[id].release();
        mutex.release();
        // An item enters the scan
        inScan[id].acquire();
    }

    public void beTurnedOn(Machine machine) throws InterruptedException {
        // A machine is turned on
        isOn.acquire();
        machineAvailable.release();
        machine.setOn(true);
    }

    public void beTurnedOff(Machine machine) throws InterruptedException {
        // A machine is turned off
        machineAvailable.acquire();
        machine.setOn(false);
        System.out.println(machine.getName() + " was turned off");
    }

    public void turnMachineOn() {
        // A monitor turns a machine on
        isOn.release();
    }

    public void scanItem(Machine machine) throws InterruptedException {
        scanReady[(int) machine.getId()].acquire();
        inScan[(int) machine.getId()].release();
        scanDone[(int) machine.getId()].release();
        canFinish[(int) machine.getId()].acquire();
    }

    public void removeItem(Item item, Machine m) {
        if (item.getCategory() != null) {
            m.removeItem(item);
        }
    }

    public void itemScanned(Machine m) throws InterruptedException {
        mutex.acquire();
        for (int i = 0; i < m.getItemList().size(); i++) {
            removeItem(m.getItemList().get(i), m);
        }
        mutex.release();

        canLeave[(int) m.getId()].release();
        left[(int) m.getId()].acquire();

        mutex.acquire();
        System.out.println("\t\t" + m.getName() + " items left to scan [" +
            m.getItemList().stream()
                .map(item -> item.getName().split("Item ")[1]) // Print only the number
                .collect(Collectors.joining(", "))
            + "]");

        if (m.getItemList().isEmpty() && !m.isCanBeOff()) {
            m.setCanBeOff(true);
            waitToTurnOff.release();
        }
        mutex.release();
        
    }

    public void waitToTurnMachineOff() throws InterruptedException {
        // Wait until there is a machine that can be turned off
        waitToTurnOff.acquire();
        mutex.acquire();

        for (Machine machine : machines) {
            // The machine cannot receive any more containers while it is being turned off
            if (machine.isCanBeOff() && machine.isOn()) {
                currentMachine = machine;
            }
        }
        // Turn off the machine
        beTurnedOff(currentMachine);
        
        mutex.release();
    }

    public void finishScan(Item item) throws InterruptedException {
        // The item finishes scanning and leaves the station
        int id = -1;
        mutex.acquire();
        for (Machine m : machines) {
            if (m.getItemList().contains(item)) {
                id = (int) m.getId();
            }
        }
        mutex.release();
        scanDone[id].acquire();
        canFinish[id].release();
        canLeave[id].acquire();
        left[id].release();
    }
}