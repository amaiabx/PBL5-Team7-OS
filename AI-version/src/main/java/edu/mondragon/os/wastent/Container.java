package edu.mondragon.os.wastent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Container extends Thread {

    private Machine machine;
    private List<Item> itemList;
    private SecureRandom rand;
    private WastePlant wastePlant;

    public Container(WastePlant wastePlant, int id) {
        super("📦 Container " + id);
        this.machine = null;
        this.itemList = new ArrayList<>();
        this.wastePlant = wastePlant;
        this.rand = new SecureRandom();

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
            Thread.sleep(rand.nextInt(8000));
            wastePlant.arriveContainer(this);
            System.out.println(this.getName() + " has been assigned to " + this.getMachine().getName());
        } catch (InterruptedException e) {
            this.interrupt();
        }
    }
}
