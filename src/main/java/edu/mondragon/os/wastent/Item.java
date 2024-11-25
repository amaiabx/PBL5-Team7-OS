package edu.mondragon.os.wastent;

import java.util.Random;

public class Item extends Thread {
          
    private Container container;
    private Category category;
    private Random rand;
    private WastePlant wastePlant;

    public Item(WastePlant wastePlant, int id, Container container) {
        super("Item " + id);
        this.rand = new Random();
        this.container = container;
        this.wastePlant = wastePlant;
        this.category = null;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(rand.nextInt(30));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public void ready() throws InterruptedException {
        System.out.println("\t\t" + this.getName() + " is ready to be scanned");
    }

    public void beScanned() throws InterruptedException {
        System.out.println("\t\t" + this.getName() + " is being be scanned");
    }

    public void beSorted() throws InterruptedException {
        System.out.println("\t\t" + this.getName() + " has been identified as: " + this.getCategory());
    }

    public void leave() throws InterruptedException {
        System.out.println("\t\t" + this.getName() + " has left the scanning station");
    }
}
