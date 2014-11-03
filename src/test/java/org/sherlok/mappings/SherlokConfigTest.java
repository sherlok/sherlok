package org.sherlok.mappings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sherlok.mappings.SherlokConfig;

public class SherlokConfigTest {

    @Test
    public void test() throws Exception {
        SherlokConfig config = SherlokConfig.load();
        assertEquals(9600, config.getPort());
    }
}
