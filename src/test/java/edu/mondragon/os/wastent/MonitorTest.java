package edu.mondragon.os.wastent;

import org.junit.Before;
import org.junit.Test;

public class MonitorTest {

    WastePlant wastePlant;
    Monitor monitor;

    @Before
    public void setup() {
        wastePlant = new WastePlant(null, 0);
        monitor = new Monitor(wastePlant, 0);
    }

    @Test
    public void testRun() {
    }
}
