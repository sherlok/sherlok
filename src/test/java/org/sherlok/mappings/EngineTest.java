package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class EngineTest {

    public static EngineDef getOpenNlpEnSegmenter() {
        EngineDef e = new EngineDef()
                .setName("OpenNlpEnSegmenter")
                .setVersion("1.6.2")
                .setDomain("dkpro")
                .setClassz(
                        "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter")
                .setBundleId("dkpro_opennlp_en:1.6.2")
                .addParameter("language", "en");

        return e;
    }

    @Test
    public void testWriteRead() throws Exception {

        File ef = new File("target/engineTest_" + currentTimeMillis() + ".json");
        EngineDef e = getOpenNlpEnSegmenter();
        FileBased.write(ef,e);
        EngineDef e2 = FileBased.loadEngine(ef);
        assertEquals(e.getName(), e2.getName());
        assertEquals(e.getVersion(), e2.getVersion());
        assertEquals(e.getParameters().size(), e2.getParameters().size());
    }

    // TODO test parsing
}
