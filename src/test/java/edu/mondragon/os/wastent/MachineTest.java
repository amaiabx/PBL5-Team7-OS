package edu.mondragon.os.wastent;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class MachineTest {

    WastePlant wastePlant;
    Machine machine;
    
    @Before
    public void setup() {
        wastePlant = new WastePlant(null, 0);
        machine = new Machine(wastePlant, 0);
    }
    
    @Test
    public void testCanBeOffGetSet() {
        machine.setCanBeOff(true);
        assertEquals(true, machine.isCanBeOff());
    }

    @Test
    public void testNoMoreGetSet() {
        machine.setNoMore(true);
        assertEquals(true, machine.isNoMore());
    }

    @Test
    public void testOnGetSet() {
        machine.setOn(true);
        assertEquals(true, machine.isOn());
    }

    @Test
    public void testMonitorSet() {
        Monitor monitor = new Monitor(wastePlant, 0);
        machine.setMonitor(monitor);
        assertEquals(monitor, machine.getMonitor());
    }
    
    @Test
    public void testItemListGet() {
        List<Item> itemList = new ArrayList<>();
        Container container = new Container(wastePlant, 0);

        itemList.add(new Item(wastePlant, 0, container));
        itemList.add(new Item(wastePlant, 1, container));
        machine.addItem(itemList.get(0));
        machine.addItem(itemList.get(1));
        assertEquals(itemList, machine.getItemList());

        machine.removeItem(itemList.get(1));
        itemList.remove(1);
        assertEquals(itemList, machine.getItemList());
    }        


}
