package edu.mondragon.os.wastent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread {
    
    private int id;
    private SecureRandom rand;
    private List<Item> itemList;
    private Monitor monitor;
    private WastePlant wastePlant;
    private boolean on;
    private boolean canBeOff;

    public Worker(WastePlant wastePlant, int id) {
        super("🛠️  Worker " + id);
        this.id = id;
        this.rand = new SecureRandom();
        this.itemList = new ArrayList<>();
        this.wastePlant = wastePlant;
        this.monitor = null;
        this.on = false;
        this.canBeOff = true;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean isCanBeOff() {
        return canBeOff;
    }

    public void setCanBeOff(boolean canBeOff) {
        this.canBeOff = canBeOff;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
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

    public void removeItem(Item item) {
        itemList.remove(item);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                if (!this.on) {
                    wastePlant.beTurnedOn(this);
                    System.out.println(this.getName() + "'s line was turned on");
                }
                wastePlant.scanItem(this);
                Thread.sleep(rand.nextInt(2000, 2500));
                wastePlant.itemScanned(this);
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
}
