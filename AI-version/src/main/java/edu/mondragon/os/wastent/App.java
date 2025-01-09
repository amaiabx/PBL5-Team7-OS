package edu.mondragon.os.wastent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class App {

    static final int NMONITORS = 4;
    static final int NMACHINES = 4;
    static final int NCONTAINERS = 5;
    static final int NITEMS = 30;
    
    private WastePlant wastePlant;

    private Monitor[] monitor;
    private Machine[] machine;
    private Container[] container;
    private Item[] item;

    private List<Machine> machines;
    private SecureRandom rand;

    public App() {
        machines = new ArrayList<>();

        wastePlant = new WastePlant(machines, NMACHINES);

        monitor = new Monitor[NMONITORS];
        machine = new Machine[NMACHINES];
        container = new Container[NCONTAINERS];
        item = new Item[NITEMS];

        this.rand = new SecureRandom();
    }

    public void createThreads() {
        Container itemContainer;

        for (int i = 0; i < NMONITORS; i++) {
            monitor[i] = new Monitor(wastePlant, i);
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i] = new Machine(wastePlant, i);
            machines.add(machine[i]);
        }
        for (int i = 0; i < NCONTAINERS; i++) {
            container[i] = new Container(wastePlant, i);
        }
        for (int i = 0; i < NITEMS; i++) {
            itemContainer = container[rand.nextInt(NCONTAINERS)]; // Assign the item to a random container
            item[i] = new Item(wastePlant, i+1, itemContainer);
            item[i].getContainer().addItem(item[i]);
        }
    }

    public void startThreads() {
        for (int i = 0; i < NMONITORS; i++) {
            monitor[i].start();
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i].start();
        }
        for (int i = 0; i < NCONTAINERS; i++) {
            container[i].start();
        }
    }

    public void waitEndOfThreads() throws InterruptedException {
        for (int i = 0; i < NMONITORS; i++) {
            monitor[i].join();
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i].interrupt();
        }
        for (int i = 0; i < NCONTAINERS; i++) {
            container[i].join();
        }
        for (int i = 0; i < NITEMS; i++) {
            item[i].join();
        }
    }

    public static void main(String[] args) {

        App app = new App();

        app.createThreads();
        app.startThreads();

        try {
            app.waitEndOfThreads();
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}