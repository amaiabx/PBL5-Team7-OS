package edu.mondragon.os.wastent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Machine extends Thread {
    
    private Random rand;
    private List<Item> itemList;
    private Monitor monitor;
    private WastePlant wastePlant;
    private boolean on;
    private boolean canBeOff;
    private boolean noMore;

    public Machine(WastePlant wastePlant, int id) {
        super("🤖 Machine " + id);
        this.rand = new Random();
        this.itemList = new ArrayList<>();
        this.wastePlant = wastePlant;
        this.monitor = null;
        this.on = false;
        this.noMore = false;
        this.canBeOff = true;
    }

    public boolean isCanBeOff() {
        return canBeOff;
    }

    public void setCanBeOff(boolean canBeOff) {
        this.canBeOff = canBeOff;
    }

    public boolean isNoMore() {
        return noMore;
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
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
        Item item;

        while (!this.isInterrupted()) {
            try {
                if (!this.on) {
                    wastePlant.beTurnedOn(this);
                    System.out.println(this.getName() + " was turned on");
                }
                item = wastePlant.scanItem(this);
                if (item != null) {
                    Thread.sleep(rand.nextInt(1000));
                    wastePlant.itemScanned(this);
                }
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
    }
}
