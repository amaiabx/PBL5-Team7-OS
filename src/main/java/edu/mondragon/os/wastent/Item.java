package edu.mondragon.os.wastent;

import java.util.Random;

public class Item extends Thread {
          
    private Container container;
    private Category category;
    private Random rand;
    private WastePlant wastePlant;

    public Item(WastePlant wastePlant, int id, Container container) {
        super("♻️  Item " + id);
        this.rand = new Random();
        this.container = container;
        this.wastePlant = wastePlant;
        this.category = null;
    }

    public Container getContainer() {
        return container;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(rand.nextInt(500, 2500));
            System.out.println("\t" + this.getName() + " is ready to be scanned");
            wastePlant.readyToScan();
            System.out.println("\t\t" + this.getName() + " is being scanned");

            switch (rand.nextInt(4)) {
                case 0:
                    this.setCategory(Category.CARDBOARD);
                    break;
                case 1:
                    this.setCategory(Category.GLASS);
                    break;
                case 2:
                    this.setCategory(Category.ORGANIC);
                    break;
                default:
                    this.setCategory(Category.PLASTIC);
                    break;
            }

            wastePlant.finishScan();
            System.out.println("\t\t" + this.getName() + " has been identified as: " + this.getCategory());
            System.out.println("\t\t\t" + this.getName() + " has left the scanning station");
        } catch (InterruptedException e) {
            this.interrupt();
        }
        
    }
}
