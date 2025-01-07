package edu.mondragon.os.wastent;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ItemTest {
        
    WastePlant wastePlant;
    Container container;
    Item item;

    @Before
    public void setup() {
        wastePlant = new WastePlant(null, 0);
        container = new Container(wastePlant, 0);
        item = new Item(wastePlant, 0, container);
    }

    @Test
    public void testContainerGet() {
        assertEquals(container, item.getContainer());
    }

    @Test
    public void testCategoryGetSet() {
        item.setCategory(Category.CARDBOARD);
        assertEquals(Category.CARDBOARD, item.getCategory());
    }
}
