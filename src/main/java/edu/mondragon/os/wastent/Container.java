package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Container extends Thread {

    private Machine machine;
    private List<Item> itemList;
    private Random rand;

    public Container(int id) {
        super("Container " + id);
        this.machine = null;
        this.itemList = new ArrayList<>();
        this.rand = new Random();
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void addItem(Item item) {
        itemList.add(item);
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

    public void arrive() throws InterruptedException {
        System.out.println(this.getName() + " has arrived");
        Thread.sleep(rand.nextInt(30));
    }

    public void beAssigned() throws InterruptedException {
        System.out.println(this.getName() + " has been assigned to " + this.getMachine().getName());
        Thread.sleep(rand.nextInt(30));
    }

    public void beDumped() throws InterruptedException {
        System.out.println(this.getName() + " is being dumped");
        Thread.sleep(rand.nextInt(30));
    }
}
