package edu.mondragon.os.wastent;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class WorkerTest {

    WastePlant wastePlant;
    Worker worker;
    
    @Before
    public void setup() {
        wastePlant = new WastePlant(null, 0);
        worker = new Worker(wastePlant, 0);
    }
    
    @Test
    public void testCanBeOffGetSet() {
        worker.setCanBeOff(true);
        assertEquals(true, worker.isCanBeOff());
    }

    @Test
    public void testOnGetSet() {
        worker.setOn(true);
        assertEquals(true, worker.isOn());
    }

    @Test
    public void testMonitorSet() {
        Monitor monitor = new Monitor(wastePlant, 0);
        worker.setMonitor(monitor);
        assertEquals(monitor, worker.getMonitor());
    }
    
    @Test
    public void testItemListGet() {
        List<Item> itemList = new ArrayList<>();
        Container container = new Container(wastePlant, 0);

        itemList.add(new Item(wastePlant, 0, container));
        itemList.add(new Item(wastePlant, 1, container));
        worker.addItem(itemList.get(0));
        worker.addItem(itemList.get(1));
        assertEquals(itemList, worker.getItemList());

        worker.removeItem(itemList.get(1));
        itemList.remove(1);
        assertEquals(itemList, worker.getItemList());
    }        


}
