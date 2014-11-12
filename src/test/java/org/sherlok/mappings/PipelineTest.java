package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.sherlok.mappings.PipelineDef.PipelineEngine;

public class PipelineTest {

    public static PipelineDef getOpennlp_ners() {
        PipelineDef e = new PipelineDef().setName("OpenNlpEnSegmenter")
                .setVersion("1.6.2").setDomain("dkpro")
                .addEngine(new PipelineEngine("OpenNlpEnSegmenter"))
                .addOutputAnnotation("dkpro.NamedEntity");
        return e;
    }

    @Test
    public void testWriteRead() throws Exception {

        File pf = new File("target/pipelineTest_" + currentTimeMillis()
                + ".json");
        PipelineDef p = getOpennlp_ners();
        FileBased.write(pf, p);
        PipelineDef p2 = FileBased.loadPipeline(pf);
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getVersion(), p2.getVersion());
        assertEquals(p.getOutput().getAnnotations().size(), p2.getOutput()
                .getAnnotations().size());
    }

    // TODO test parsing
}
