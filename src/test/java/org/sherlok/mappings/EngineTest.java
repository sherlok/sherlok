package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sherlok.FileBased;

public class EngineTest {

    public static EngineDef getOpenNlpEnSegmenter() {
        EngineDef e = new EngineDef()
                .setDomain("dkpro")
                .setClassz(
                        "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter")
                .setBundleId("dkpro_opennlp_en:1.6.2")
                .addParameter("language", "en");
        e.setName("OpenNlpEnSegmenter");
        e.setVersion("1.6.2");

        return e;
    }

    @Test
    public void testWriteRead() throws Exception {
        
        File ef = new File("target/engineTest_" + currentTimeMillis() + ".json");
        EngineDef e = getOpenNlpEnSegmenter();
        FileBased.write(ef, e);
        EngineDef e2 = FileBased.loadEngine(ef);
        e2.validate("");
        assertEquals(e.getName(), e2.getName());
        assertEquals(e.getVersion(), e2.getVersion());
        assertEquals(e.getParameters().size(), e2.getParameters().size());
    }
}
