package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Machine extends Thread {
    
    private Random rand;
    private List<Item> itemList;
    private int number;
    private Monitor monitor;
    private WastePlant wastePlant;

    public Machine(WastePlant wastePlant, int id) {
        super("Machine " + id);
        this.rand = new Random();
        this.itemList = new ArrayList<>();
        this.number = 0;
        this.wastePlant = wastePlant;
        this.monitor = null;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
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
                Thread.sleep(rand.nextInt(30));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public void beTurnedOn() throws InterruptedException {
        System.out.println(this.getName() + " has been turned on");
    }
    
    public void startScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " is scanning an item");
        Thread.sleep(rand.nextInt(30) + 3000);
    }

    public void finishScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " has finished scanning the item");
    }

    public void beTurnedOff() throws InterruptedException {
        System.out.println(this.getName() + " has been turned off");
    }
}
