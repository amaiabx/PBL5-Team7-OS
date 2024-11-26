package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Container extends Thread {

    private Machine machine;
    private List<Item> itemList;
    private Random rand;
    private WastePlant wastePlant;

    public Container(WastePlant wastePlant, int id) {
        super("📦 Container " + id);
        this.machine = null;
        this.itemList = new ArrayList<>();
        this.wastePlant = wastePlant;
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
        try {
            Thread.sleep(rand.nextInt(1000, 10000));
            System.out.println(this.getName() + " has arrived");
            wastePlant.arriveContainer(this);
            System.out.println(this.getName() + " has been assigned to " + this.getMachine().getName());
            Thread.sleep(rand.nextInt(30));
            System.out.println(this.getName() + " is being dumped");
        } catch (InterruptedException e) {
            this.interrupt();
        }
    }

    public void arrive() throws InterruptedException {
        System.out.println(this.getName() + " has arrived");
        Thread.sleep(rand.nextInt(30));
    }

    public void beAssigned(Machine assigned) throws InterruptedException {
        this.machine = assigned;
        System.out.println(this.getName() + " has been assigned to " + this.getMachine().getName());
    }

    public void beDumped() throws InterruptedException {
        System.out.println(this.getName() + " is being dumped");
        Thread.sleep(rand.nextInt(30));
    }
}
