package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;

    private Semaphore mutex;

    private Semaphore isOn;
    private Semaphore machineAvailable;
    private Semaphore waitToTurnOff;
    
    private Semaphore[] scanReady;
    private Semaphore[] inScan;
    private Semaphore[] scanDone;
    private Semaphore[] hasLeft;

    public WastePlant(List<Machine> machines, int nMachines) {
        this.machines = machines;

        mutex = new Semaphore(1);

        isOn = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        waitToTurnOff = new Semaphore(nMachines);
        
        // FIFO, the item that is ready to be scanned first is scanned first
        scanReady = new Semaphore[nMachines];
        createThreads(scanReady, true, nMachines);
        
        inScan = new Semaphore[nMachines];
        createThreads(inScan, false, nMachines);
        
        scanDone = new Semaphore[nMachines];
        createThreads(scanDone, false, nMachines);
        
        hasLeft = new Semaphore[nMachines];
        createThreads(hasLeft, false, nMachines);
    }

    public void createThreads(Semaphore[] name, boolean fair, int nMachines) {
        for (int i=0; i < nMachines; i++) {
            name[i] = new Semaphore(0, fair);
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

        // Print the items contained in the container
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
        // If the container is empty, as it gets items it can't be turned off anymore
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }

        container.setMachine(found);
        
        // Add the items to the machine
        for (Item item : items) {
            found.addItem(item);
        }
        mutex.release();

        // Start the container's item's threads
        for (Item item : items) {
            item.start();
        }
    }

    public void readyToScan(Item item) throws InterruptedException {
        int id = -1;

        mutex.acquire();
        // Get the ID of the item's assigned machine
        for (Machine m : machines) {
            if (m.getItemList().contains(item)) {
                id = (int) m.getId();
                break;
            }
        }
        // The item is ready to be scanned
        scanReady[id].release();
        mutex.release();

        // The item enters the scan
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
        // An item enters the scan
        scanReady[(int) machine.getId()].acquire();
        inScan[(int) machine.getId()].release();

        // The scan finishes
        scanDone[(int) machine.getId()].release();
    }

    public void removeItem(Item item, Machine m) {
        // Remove sorted items from the machine's scan queue
        if (item.getCategory() != null) {
            m.removeItem(item);
        }
    }

    public void itemScanned(Machine m) throws InterruptedException {
        // Remove the sorted item from the machine´s scan queue
        mutex.acquire();
        for (int i = 0; i < m.getItemList().size(); i++) {
            removeItem(m.getItemList().get(i), m);
        }
        mutex.release();

        // The scanned item leaves the scanning station
        hasLeft[(int) m.getId()].acquire();

        // Print the item's assigned machine's remaining scan queue
        mutex.acquire();
        System.out.println("\t\t" + m.getName() + " items left to scan [" +
            m.getItemList().stream()
                .map(item -> item.getName().split("Item ")[1]) // Print only the number
                .collect(Collectors.joining(", "))
            + "]");

        // If after the scan the machine's queue is empty, it can be turned off by a monitor
        if (m.getItemList().isEmpty() && !m.isCanBeOff()) {
            m.setCanBeOff(true);
            waitToTurnOff.release();
        }
        mutex.release();
        
    }

    public void waitToTurnMachineOff() throws InterruptedException {
        Machine currentMachine = null;

        // Wait until there is a machine that can be turned off
        waitToTurnOff.acquire();
        
        mutex.acquire();
        for (Machine machine : machines) {
            // Select a machine that can be turned off (is turned on and its queue is empty)
            if (machine.isCanBeOff() && machine.isOn()) {
                currentMachine = machine;
                break;
            }
        }
        // Turn off the machine
        beTurnedOff(currentMachine);
        mutex.release();
    }

    public void finishScan(Item item) throws InterruptedException {
        int id = -1;
        
        mutex.acquire();
        for (Machine m : machines) {
            // Get the ID of the item's assigned machine
            if (m.getItemList().contains(item)) {
                id = (int) m.getId();
                break;
            }
        }

        // The item finishes scanning and leaves the station
        mutex.release();
        scanDone[id].acquire();
        hasLeft[id].release();
    }
}