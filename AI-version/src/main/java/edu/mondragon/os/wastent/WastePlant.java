package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Machine> machines;
    int nMachines;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore machineAvailable;
    private Semaphore waitToTurnOff;

    private List<BlockingQueue<Item>> scanReady;
    private List<BlockingQueue<Item>> inScan;
    private List<BlockingQueue<Item>> scanDone;
    private List<BlockingQueue<Item>> hasLeft;

    public WastePlant(List<Machine> machines, int nMachines) {
        this.machines = machines;
        this.nMachines = nMachines;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        machineAvailable = new Semaphore(0);
        waitToTurnOff = new Semaphore(nMachines);

        scanReady = new ArrayList<>();
        inScan = new ArrayList<>();
        scanDone = new ArrayList<>();
        hasLeft = new ArrayList<>();

        for (int i = 0; i < nMachines; i++) {
            scanReady.add(new LinkedBlockingQueue<>());
            inScan.add(new LinkedBlockingQueue<>(1));
            scanDone.add(new LinkedBlockingQueue<>());
            hasLeft.add(new LinkedBlockingQueue<>());
        }
    }


    
    // CONTAINER FUNCTIONS



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
        // If the line is empty, as it gets items it can't be turned off anymore
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



    // MONITOR FUNCTIONS



    public void turnMachineOn() {
        // A monitor turns a machine on
        isOn.release();
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



    // MACHINE FUNCTIONS



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

    public void scanItem(Machine machine) throws InterruptedException {
        // An item enters the scan
        Item item = scanReady.get((int) machine.getId()).take();
        inScan.get((int) machine.getId()).put(item);

        // The scan finishes
        scanDone.get((int) machine.getId()).put(item);
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
        hasLeft.get((int) m.getId()).take();

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



    // ITEM FUNCTIONS



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
        mutex.release();
        
        // The item is ready to be scanned
        scanReady.get(id).put(item);

        // The item enters the scan
        inScan.get(id).take();
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

        scanDone.get(id).take();
        hasLeft.get(id).put(item);
    }    
}