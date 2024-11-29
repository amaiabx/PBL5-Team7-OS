package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class App {

    static final int NMONITORS = 5;
    static final int NMACHINES = 5;
    static final int NCONTAINERS = 10;
    static final int NITEMS = 50;
    static final int MAXITEMS = 5;
    
    private WastePlant wastePlant;

    private Monitor[] monitor;
    private Machine[] machine;
    private Container[] container;
    private Item[] item;

    private List<Machine> machines;


    private Random rand;
    private Set<Item> startedItems;


    public App() {
        machines = new ArrayList<>();

        wastePlant = new WastePlant(machines, NMACHINES);

        monitor = new Monitor[NMONITORS];
        machine = new Machine[NMACHINES];
        container = new Container[NCONTAINERS];
        item = new Item[NITEMS];

        this.rand = new Random();
        startedItems = Collections.synchronizedSet(new HashSet<>());

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

    public void waitEndOfThreads() {
        try {
            for (int i = 0; i < NMONITORS; i++) {
                monitor[i].join();
            }
            for (int i = 0; i < NMACHINES; i++) {
                machine[i].interrupt();
            }
            for (int i = 0; i < NCONTAINERS; i++) {
                container[i].join();
            }
            synchronized (startedItems) {
                for (Item i : startedItems) {
                    i.join(); // Join only started items
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        App app = new App();

        app.createThreads();
        app.startThreads();

        app.waitEndOfThreads();
    }
}