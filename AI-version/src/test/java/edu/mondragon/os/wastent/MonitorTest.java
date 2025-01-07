package edu.mondragon.os.wastent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MonitorTest {

    WastePlant wastePlant;
    Monitor monitor;

    @Test
    public void testMonitor() {
        wastePlant = new WastePlant(null, 0);
        monitor = new Monitor(wastePlant, 0);
        assertTrue(true);
    }
}
