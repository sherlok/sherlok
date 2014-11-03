package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class PipelineTest {

    public static Pipeline getOpennlp_ners() {
        Pipeline e = new Pipeline().setName("OpenNlpEnSegmenter")
                .setVersion("1.6.2").setDomain("dkpro")
                .addOutputAnnotation("bla");

        return e;
    }

    @Test
    public void testWriteRead() throws Exception {

        File pf = new File("target/pipelineTest_" + currentTimeMillis()
                + ".json");
        Pipeline p = getOpennlp_ners();
        p.write(pf);
        Pipeline p2 = Pipeline.load(pf);
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getVersion(), p2.getVersion());
        assertEquals(p.getOutput().getAnnotations().size(), p2.getOutput()
                .getAnnotations().size());

    }

    // TODO test parsing

}
