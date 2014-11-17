package org.sherlok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.sherlok.utils.Strings;
import org.slf4j.Logger;

public class PipelineLoaderIntegrationTest {
    private static Logger LOG = getLogger(PipelineLoaderIntegrationTest.class);

    public static final String TEST_TEXT = "Jack Burton (born April 29, 1954 in El Paso), "
            + "also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.";

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

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("opennlp.ners.en", null);
        String result = (pipeline.annotate(TEST_TEXT));
        LOG.debug(result);

        JSONObject jsonObject = new JSONObject(result);
        // JSONObject context = jsonObject.getJSONObject("@context");
        JSONObject annotations = jsonObject
                .getJSONObject("@cas_feature_structures");
        JSONArray names = annotations.names();
        assertEquals(5, names.length());
        Object firstPerson = annotations.get("538");
        assertEquals(
                "{\"sofa\":1,\"@type\":\"NamedEntity\",\"value\":\"person\",\"end\":11}",
                firstPerson.toString());
    }
}
