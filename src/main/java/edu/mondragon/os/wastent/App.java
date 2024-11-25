package edu.mondragon.os.wastent;

public class App {

    final static int NMONITORS = 3;
    final static int NMACHINES = 3;
    final static int NITEM = 30;

    private Monitor monitor[];
    private Machine machine[];
    private Item item[];

    public App() {
        monitor = new Monitor[NMONITORS];
        machine = new Machine[NMACHINES];
        item = new Item[NITEM];
    }

    public void createThreads() {
        for (int i = 0; i < NMONITORS; i++) {
            monitor[i] = new Monitor(i);
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i] = new Machine(i);
        }
        for (int i = 0; i < NITEM; i++) {
            item[i] = new Item(i);
        }
    }

    public void startThreads() {
        for (int i = 0; i < NMONITORS; i++) {
            monitor[i].start();
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i].start();
        }
        for (int i = 0; i < NITEM; i++) {
            item[i].start();
        }
    }

    public void interruptThreads() {
        for (int i = 0; i < NMONITORS; i++) {
            monitor[i].interrupt();
        }
        for (int i = 0; i < NMACHINES; i++) {
            machine[i].interrupt();
        }
        for (int i = 0; i < NITEM; i++) {
            item[i].interrupt();
        }
    }

    public void waitEndOfThreads() {
        try {
            for (int i = 0; i < NMONITORS; i++) {
                monitor[i].join();
            }
            for (int i = 0; i < NMACHINES; i++) {
                machine[i].join();
            }
            for (int i = 0; i < NITEM; i++) {
                item[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        App app = new App();

        app.createThreads();
        app.startThreads();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        app.interruptThreads();
        app.waitEndOfThreads();
    }
}