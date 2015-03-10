package org.sherlok.mappings;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SherlokResultTest {

    @Test
    public void test() throws Exception {

        String json = IOUtils.toString(SherlokResultTest.class
                .getResourceAsStream("SherlokResult.json"), UTF_8);

        SherlokResult result = SherlokResult.parse(json);

        Map<String, List<JsonAnnotation>> annotations = result.getAnnotations();
        assertEquals(8, annotations.size());
        List<JsonAnnotation> layers = annotations.get("Layer");
        assertEquals(2, layers.size());
        for (JsonAnnotation l : layers) {
            Object oid = l.getProperty("ontologyId");
            assertTrue(oid instanceof String);
            assertTrue(oid.toString().startsWith("HBP_LAYER"));
        }

        assertEquals("layer V and layer iii large pyramidal neurons",
                result.getText());

        assertNull(result.get("bogus"));
    }

    /** This one has references */
    @Test
    public void testBerkleyparser() throws Exception {

        String json = IOUtils.toString(SherlokResultTest.class
                .getResourceAsStream("SherlokResult2.json"), UTF_8);

        SherlokResult result = SherlokResult.parse(json);

        Map<String, List<JsonAnnotation>> annotations = result.getAnnotations();
        assertEquals(9, annotations.size());
        List<JsonAnnotation> layers = annotations.get("VP");
        assertEquals(2, layers.size());
        for (JsonAnnotation l : layers) {
            assertEquals(41, l.getEnd());
        }

        assertEquals("The blue house of my childhood was bought: what a pity!",
                result.getText());
    }
}
