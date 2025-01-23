package edu.mondragon.os.wastent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Container extends Thread {

    private Worker worker;
    private List<Item> itemList;
    private SecureRandom rand;
    private WastePlant wastePlant;

    public Container(WastePlant wastePlant, int id) {
        super("📦 Container " + id);
        this.worker = null;
        this.itemList = new ArrayList<>();
        this.wastePlant = wastePlant;
        this.rand = new SecureRandom();

    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
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
            Thread.sleep(rand.nextInt(500, 8000));
            wastePlant.arriveContainer(this);
            System.out.println(this.getName() + " has been assigned to " + this.getWorker().getName());
        } catch (InterruptedException e) {
            this.interrupt();
        }
    }
}
