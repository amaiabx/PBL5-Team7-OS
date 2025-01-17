package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Worker> workers;
    int nWorkers;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore workerAvailable;
    private Semaphore waitToTurnOff;

    private List<BlockingQueue<Item>> scanReady;
    private List<BlockingQueue<Item>> inScan;
    private List<BlockingQueue<Item>> scanDone;
    private List<BlockingQueue<Item>> hasLeft;

    public WastePlant(List<Worker> workers, int nWorkers) {
        this.workers = workers;
        this.nWorkers = nWorkers;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        workerAvailable = new Semaphore(0);
        waitToTurnOff = new Semaphore(nWorkers);

        scanReady = new ArrayList<>();
        inScan = new ArrayList<>();
        scanDone = new ArrayList<>();
        hasLeft = new ArrayList<>();

        for (int i = 0; i < nWorkers; i++) {
            scanReady.add(new LinkedBlockingQueue<>());
            inScan.add(new LinkedBlockingQueue<>(1));
            scanDone.add(new LinkedBlockingQueue<>());
            hasLeft.add(new LinkedBlockingQueue<>());
        }
    }


    
    // CONTAINER FUNCTIONS



    public void arriveContainer(Container container) throws InterruptedException {
        Worker found;

        // Print the items contained in the container
        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " has arrived containing items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        // Find a line to dump the container in
        found = findAvailableWorker();
        found.setCanBeOff(false);
        
        mutex.acquire(); 
        // If the line is empty, as it gets items it can't be turned off anymore
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }

        container.setWorker(found);
        
        // Add the items to the line
        for (Item item : items) {
            found.addItem(item);
        }
        mutex.release();

        // Start the line's item's threads
        for (Item item : items) {
            item.start();
        }
    }



    // MONITOR FUNCTIONS



    public void turnLineOn() {
        // A monitor turns a line on
        isOn.release();
    }

    public void waitToTurnLineOff() throws InterruptedException {
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
        // Turn off the worker
        beTurnedOff(currentWorker);
        mutex.release();
    }



    // WORKER FUNCTIONS



    public Worker findAvailableWorker() throws InterruptedException {
        Worker found = null;
        int smallest = Integer.MAX_VALUE;

        // Wait until a line has been turned on
        workerAvailable.acquire();
        workerAvailable.release();

        mutex.acquire();
        for (Worker worker : workers) {
            if (worker.isOn()) {
                // Assign the Worker that is the least busy
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
        System.out.println(worker.getName() + "'s line was turned off");
    }

    public void sortItem(Worker worker) throws InterruptedException {
        // An item is scanned
        Item item = scanReady.get((int) worker.getId()).take();
        inScan.get((int) worker.getId()).put(item);

        // The scan finishes
        scanDone.get((int) worker.getId()).put(item);
    }

    public void removeItem(Item item, Worker w) {
        // Remove sorted items from the line's scan queue
        if (item.getCategory() != null) {
            w.removeItem(item);
        }
    }

    public void itemSorted(Worker w) throws InterruptedException {
        // Remove the sorted item from the line's scan queue
        mutex.acquire();
        for (int i = 0; i < w.getItemList().size(); i++) {
            removeItem(w.getItemList().get(i), w);
        }
        mutex.release();

        // The scanned item leaves the scanning station
        hasLeft.get((int) w.getId()).take();

        // Print the item's assigned worker's remaining scan queue
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



    // ITEM FUNCTIONS



    public void readyToSort(Item item) throws InterruptedException {
        int id = -1;

        mutex.acquire();
        // Get the ID of the item's assigned worker
        for (Worker w : workers) {
            if (w.getItemList().contains(item)) {
                id = (int) w.getId();
                break;
            }
        }
        mutex.release();
        
        // The item is ready to be scanned
        scanReady.get(id).put(item);

        // The item enters the scan
        inScan.get(id).take();
    }

    public void finishSorting(Item item) throws InterruptedException {
        int id = -1;
        
        mutex.acquire();
        for (Worker w : workers) {
            // Get the ID of the item's assigned worker
            if (w.getItemList().contains(item)) {
                id = (int) w.getId();
                break;
            }
        }

        // The item finishes sorting and leaves the station
        mutex.release();

        scanDone.get(id).take();
        hasLeft.get(id).put(item);
    }    
}