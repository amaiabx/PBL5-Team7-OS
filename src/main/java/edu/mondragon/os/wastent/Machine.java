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
    private boolean on;

    public Machine(WastePlant wastePlant, int id) {
        super("🤖 Machine " + id);
        this.rand = new Random();
        this.itemList = new ArrayList<>();
        this.number = 0;
        this.wastePlant = wastePlant;
        this.monitor = null;
        this.on = false;
    } 

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
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

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void addItem(Item item) {
        itemList.add(item);
    }

    public void removeItem(Item item) {
        itemList.remove(item);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                wastePlant.beTurnedOn(this);
                wastePlant.scanItem();
                System.out.println("\t" + this.getName() + " is scanning an item");
                Thread.sleep(rand.nextInt(30) + 3000);
                System.out.println("\t" + this.getName() + " has finished scanning the item");
                wastePlant.beTurnedOff(this);
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public void startScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " is scanning an item");
        Thread.sleep(rand.nextInt(30) + 3000);
    }

    public void finishScan() throws InterruptedException {
        System.out.println("\t" + this.getName() + " has finished scanning the item");
    }
}
