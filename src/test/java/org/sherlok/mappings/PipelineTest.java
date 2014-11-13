package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.mappings.PipelineDef.PipelineEngine;

public class PipelineTest {

    public static PipelineDef getOpennlp_ners() {
        PipelineDef p = new PipelineDef().setDomain("dkpro")
                .addEngine(new PipelineEngine("OpenNlpEnSegmenter:1.6.2"))
                .addOutputAnnotation("dkpro.NamedEntity");
        p.setName("OpenNlpEnSegmenter");
        p.setVersion("1.6.2");
        return p;
    }

    @Test
    public void testWriteRead() throws Exception {

        File pf = new File("target/pipelineTest_" + currentTimeMillis()
                + ".json");
        PipelineDef p = getOpennlp_ners();
        FileBased.write(pf, p);
        PipelineDef p2 = FileBased.read(pf, PipelineDef.class);
        p2.validate("");
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getVersion(), p2.getVersion());
        assertEquals(p.getOutput().getAnnotations().size(), p2.getOutput()
                .getAnnotations().size());
    }
}
