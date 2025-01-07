package edu.mondragon.os.wastent;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WastePlant {

    List<Worker> workers;
    private Item cur;
    private Worker currentWorker;

    private Semaphore mutex;
    private Semaphore isOn;
    private Semaphore workerAvailable;
    private Semaphore scanReady;
    private Semaphore inScan;
    private Semaphore scanDone;
    private Semaphore canFinish;
    private Semaphore waitToTurnOff;
    private Semaphore canLeave;
    private Semaphore left;

    public WastePlant(List<Worker> workers, int nWorkers) {
        this.workers = workers;
        this.cur = null;
        this.currentWorker = null;

        mutex = new Semaphore(1);
        isOn = new Semaphore(0);
        workerAvailable = new Semaphore(0);

        // FIFO, the item that is ready to be scanned first is scanned first
        scanReady = new Semaphore(0, true);

        inScan = new Semaphore(0);
        scanDone = new Semaphore(0);
        canFinish = new Semaphore(0);
        waitToTurnOff = new Semaphore(nWorkers);
        canLeave = new Semaphore(0);
        left = new Semaphore(0);
    }

    public Worker findAvailableWorker() throws InterruptedException {
        Worker found = null;
        int smallest = Integer.MAX_VALUE;

        // Wait until a worker has been turned on
        workerAvailable.acquire();
        workerAvailable.release();

        mutex.acquire();
        for (Worker worker : workers) {
            if (worker.isOn() && !worker.isNoMore()) {
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
        
        // Find a worker to dump the container in
        found = findAvailableWorker();
        found.setCanBeOff(false);
        
        mutex.acquire();
        
        // If it was empty, as it gets items it can't be turned off anymore
        if (found.getItemList().isEmpty()) {
            waitToTurnOff.acquire();
        }

        container.setWorker(found);
        
        List<Item> items = container.getItemList();

        System.out.println(container.getName() + " has arrived containing items [" +
        container.getItemList().stream()
            .map(item -> item.getName().split("Item ")[1]) // Print only the number
            .collect(Collectors.joining(", "))
        + "]");
        
        // Add the items to the worker
        for (Item item : items) {
            found.addItem(item);
        }

        mutex.release();

        for (Item item : items) {
            item.start();
        }
    }

    public void readyToScan() throws InterruptedException {
        // An item is ready to be scanned
        scanReady.release();
        cur = (Item) Thread.currentThread();
        // An item enters the scan
        inScan.acquire();
    }

    public void beTurnedOn(Worker worker) throws InterruptedException {
        // A worker is turned on
        isOn.acquire();
        workerAvailable.release();
        worker.setOn(true);
    }

    public void beTurnedOff(Worker worker) throws InterruptedException {
        // A worker is turned off
        workerAvailable.acquire();
        worker.setOn(false);
        System.out.println(worker.getName() + "'s line was turned off");
    }

    public void turnWorkerOn() {
        // A monitor turns a worker on
        isOn.release();
    }

    public Item scanItem(Worker worker) throws InterruptedException {
        Item current;
        current = cur;
        mutex.acquire();
        // If the item belongs to the worker, scan it
        if (worker.getItemList().contains(current)) {
            mutex.release();
            scanReady.acquire();
            inScan.release();
            scanDone.release();
            canFinish.acquire();
            return cur;
        }
        mutex.release();
        return null;
    }

    public void removeItem(Item item, Worker m) {
        if (item.getCategory() != null) {
            m.removeItem(item);
        }
    }

    public void itemScanned() throws InterruptedException {
        mutex.acquire();

        // Remove scanned items from list
        for (Worker m : workers) {
            if (m.isOn()) {
                for (int i = 0; i < m.getItemList().size(); i++) {
                    removeItem(m.getItemList().get(i), m);
                }
            }
        }

        mutex.release();

        canLeave.release();
        left.acquire();

        mutex.acquire();

        for (Worker m : workers) {
            if (m.isOn()) {
                System.out.println("\t\t" + m.getName() + " items left to scan [" +
                m.getItemList().stream()
                    .map(item -> item.getName().split("Item ")[1]) // Print only the number
                    .collect(Collectors.joining(", "))
                + "]");
            }

            // If that was the last item, the worker can now be turned off
            if (m.getItemList().isEmpty() && !m.isCanBeOff()) {
                m.setCanBeOff(true);
                waitToTurnOff.release();
            }
        }

        mutex.release();
        
        
    }

    public void waitToTurnWorkerOff() throws InterruptedException {
        // Wait until there is a worker that can be turned off
        waitToTurnOff.acquire();
        mutex.acquire();

        for (Worker worker : workers) {
            // The worker cannot receive any more containers while it is being turned off
            if (worker.isCanBeOff() && worker.isOn()) {
                worker.setNoMore(true);
                currentWorker = worker;
            }
        }
        // Turn off the worker
        beTurnedOff(currentWorker);
        
        mutex.release();
    }

    public void finishScan() throws InterruptedException {
        // The item finishes scanning and leaves the station
        scanDone.acquire();
        canFinish.release();
        canLeave.acquire();
        left.release();
    }
}