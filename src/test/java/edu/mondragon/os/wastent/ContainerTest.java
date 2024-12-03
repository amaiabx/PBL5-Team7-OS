package edu.mondragon.os.wastent;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ContainerTest {
    
    WastePlant wastePlant;
    Container container;

    @Before
    public void setup() {
        wastePlant = new WastePlant(null, 0);
        container = new Container(wastePlant, 0);
    }

    @Test
    public void testMachineGetSet() {
        Machine machine = new Machine(wastePlant, 0);
        container.setMachine(machine);
        assertEquals(machine, container.getMachine());
    }
    
    @Test
    public void testItemListGet() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(wastePlant, 0, container));
        container.addItem(itemList.get(0));
        assertEquals(itemList, container.getItemList());
    }
}
