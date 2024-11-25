package edu.mondragon.os.wastent;

import java.util.Random;

public class Item extends Thread {
          
    private Container container;
    private Category category;
    private Random rand;

    public Item(int id) {
        super("Item " + id);
        this.rand = new Random();
        this.container = null;
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
                Thread.sleep(rand.nextInt(10));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }  
}
