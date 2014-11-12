package org.sherlok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Test;
import org.slf4j.Logger;

public class ResolverTest {
    private static Logger LOG = getLogger(ResolverTest.class);

    @Test
    public void testNaturalOrdering() {

        assertEquals(0, "a".compareTo("a"));
        assertEquals(1, "b".compareTo("a"));
        assertEquals(1, "2".compareTo("1"));
        assertTrue(1 < "a".compareTo("1"));

        assertEquals(1, Strings.compareNatural("b", "a"));
        assertEquals(1, Strings.compareNatural("a", "1"));
        assertTrue(0 < Strings.compareNatural("1.2.4", "1.2.2"));
        assertTrue(0 < Strings.compareNatural("1.22.4", "1.2.2"));
        assertTrue(0 < Strings.compareNatural("1.22.4", "1.22"));
        assertTrue(0 < Strings.compareNatural("1.2.a", "1.2.1"));
        assertTrue(0 < Strings.compareNatural("1.21.1", "1.2.a"));
    }

    @Test
    public void testResolve() throws Exception {

        Pipeline pipeline = new Resolver().resolve("opennlp_en_ners", null);
        String result = (pipeline
                .annotate("Jack Burton (born April 29, 1954 in El Paso), also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards. "));
        LOG.debug(result);
    }
}
