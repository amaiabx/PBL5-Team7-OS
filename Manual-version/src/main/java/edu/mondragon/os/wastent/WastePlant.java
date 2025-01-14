package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Worker> workers;

    private Semaphore mutex;

    private Semaphore isOn;
    private Semaphore workerAvailable;
    private Semaphore waitToTurnOff;
    
    private Semaphore[] scanReady;
    private Semaphore[] inScan;
    private Semaphore[] scanDone;
    private Semaphore[] hasLeft;

    public WastePlant(List<Worker> workers, int nWorkers) {
        this.workers = workers;

        mutex = new Semaphore(1);

        isOn = new Semaphore(0);
        workerAvailable = new Semaphore(0);
        waitToTurnOff = new Semaphore(nWorkers);

        // FIFO, the item that is ready to be scanned first is scanned first
        scanReady = new Semaphore[nWorkers];
        createThreads(scanReady, true, nWorkers);
        
        inScan = new Semaphore[nWorkers];
        createThreads(inScan, false, nWorkers);
        
        scanDone = new Semaphore[nWorkers];
        createThreads(scanDone, false, nWorkers);
        
        hasLeft = new Semaphore[nWorkers];
        createThreads(hasLeft, false, nWorkers);
    }

    public void createThreads(Semaphore[] name, boolean fair, int nWorkers) {
        for (int i=0; i < nWorkers; i++) {
            name[i] = new Semaphore(0, fair);
        }
    }

    public Worker findAvailableWorker() throws InterruptedException {
        Worker found = null;
        int smallest = Integer.MAX_VALUE;

        // Wait until a worker has been turned on
        workerAvailable.acquire();
        workerAvailable.release();

        mutex.acquire();
        for (Worker worker : workers) {
            if (worker.isOn()) {
                // Assign the worker that is the least busy
                int size = worker.getItemList().size();
                if (size < smallest) {
                    smallest = size;
                    found = worker;
                }
            }
        }
        mutex.release();

        return found;
    }

    public void arriveContainer(Container container) throws InterruptedException {
        Worker found;

        // Print the items contained in the container
        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " has arrived containing items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        // Find a worker to dump the container in
        found = findAvailableWorker();
        found.setCanBeOff(false);
        
        mutex.acquire();
        // If the container is empty, as it gets items it can't be turned off anymore
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }

        container.setWorker(found);
        
        // Add the items to the worker
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
        // Get the ID of the item's assigned line
        for (Worker w : workers) {
            if (w.getItemList().contains(item)) {
                id = (int) w.getId();
                break;
            }
        }
        // The item is ready to be scanned
        scanReady[id].release();
        mutex.release();

        // The item enters the scan
        inScan[id].acquire();
    }

    public void beTurnedOn(Worker worker) throws InterruptedException {
        // A line is turned on
        isOn.acquire();
        workerAvailable.release();
        worker.setOn(true);
    }

    public void beTurnedOff(Worker worker) throws InterruptedException {
        // A line is turned off
        workerAvailable.acquire();
        worker.setOn(false);
        System.out.println(worker.getName() + " was turned off");
    }

    public void turnWorkerOn() {
        // A monitor turns a line on
        isOn.release();
    }

    public void scanItem(Worker worker) throws InterruptedException {
        // An item enters the scan
        scanReady[(int) worker.getId()].acquire();
        inScan[(int) worker.getId()].release();

        // The scan finishes
        scanDone[(int) worker.getId()].release();
    }

    public void removeItem(Item item, Worker w) {
        // Remove sorted items from the line's scan queue
        if (item.getCategory() != null) {
            w.removeItem(item);
        }
    }

    public void itemScanned(Worker w) throws InterruptedException {
        // Remove the sorted item from the line´s scan queue
        mutex.acquire();
        for (int i = 0; i < w.getItemList().size(); i++) {
            removeItem(w.getItemList().get(i), w);
        }
        mutex.release();

        // The scanned item leaves the scanning station
        hasLeft[(int) w.getId()].acquire();

        // Print the item's assigned line's remaining scan queue
        mutex.acquire();
        System.out.println("\t\t" + w.getName() + " items left to scan [" +
            w.getItemList().stream()
                .map(item -> item.getName().split("Item ")[1]) // Print only the number
                .collect(Collectors.joining(", "))
            + "]");

        // If after the scan the line's queue is empty, it can be turned off by a monitor
        if (w.getItemList().isEmpty() && !w.isCanBeOff()) {
            w.setCanBeOff(true);
            waitToTurnOff.release();
        }
        mutex.release();
        
    }

    public void waitToTurnWorkerOff() throws InterruptedException {
        Worker currentWorker = null;

        // Wait until there is a line that can be turned off
        waitToTurnOff.acquire();
        
        mutex.acquire();
        for (Worker worker : workers) {
            // Select a line that can be turned off (is turned on and its queue is empty)
            if (worker.isCanBeOff() && worker.isOn()) {
                currentWorker = worker;
                break;
            }
        }
        // Turn off the line
        beTurnedOff(currentWorker);
        mutex.release();
    }

    public void finishScan(Item item) throws InterruptedException {
        int id = -1;
        
        mutex.acquire();
        for (Worker w : workers) {
            // Get the ID of the item's assigned line
            if (w.getItemList().contains(item)) {
                id = (int) w.getId();
                break;
            }
        }

        // The item finishes scanning and leaves the station
        mutex.release();
        scanDone[id].acquire();
        hasLeft[id].release();
    }
}