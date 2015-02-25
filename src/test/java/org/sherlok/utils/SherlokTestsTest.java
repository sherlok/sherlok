package org.sherlok.utils;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.sherlok.FileBased.allPipelineDefs;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.sherlok.Controller;
import org.sherlok.MethodNameLoggerWatcher;
import org.sherlok.PipelineLoader;
import org.sherlok.UimaPipeline;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineTest;
import org.sherlok.mappings.PipelineDef.TestAnnotation;
import org.slf4j.Logger;

public class SherlokTestsTest {
    private static final Logger LOG = getLogger(SherlokTestsTest.class);

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @Test
    public void testParseTestAnnotation() throws Exception {
        TestAnnotation a = SherlokTests
                .parse("{\"@type\" : \"Layer\",  \"sofa\" : 1, \"end\" : 21,  \"ontologyId\" : \"HBP_NEUROTRANSMITTER:0000003\" }");
        assertEquals("Layer", a.getType());
        assertEquals(0, a.getBegin());
        assertEquals(21, a.getEnd());
        assertEquals("HBP_NEUROTRANSMITTER:0000003",
                a.getProperties().get("ontologyId"));
    }

    @Test
    public void testAnnotateDogs() throws Exception {

        PipelineLoader pipelineLoader = new PipelineLoader(
                new Controller().load());

        UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                "01.ruta.annotate.dog", "1");

        for (PipelineTest test : pipeline.getPipelineDef().getTests()) {
            String systemOut = pipeline.annotate(test.getIn());
            SherlokTests.assertEquals(test.getOut(), systemOut,
                    test.getComparison());
        }
    }

    @Test
    @SuppressWarnings("static-access")
    public void testAllPipelines() throws Exception {

        PipelineLoader pipelineLoader = new PipelineLoader(
                new Controller().load());

        for (PipelineDef pipelineDef : allPipelineDefs()) {
            LOG.debug("testing {}", pipelineDef);

            UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                    pipelineDef.getName(), pipelineDef.getVersion());

            for (PipelineTest test : pipeline.getPipelineDef().getTests()) {

                if (test.getOut() != null
                        && !test.getOut().toString().isEmpty()) {
                    String systemOut = pipeline.annotate(test.getIn());
                    SherlokTests.assertEquals(test.getOut(), systemOut,
                            test.getComparison());
                } else {
                    LOG.debug("  no output for {}", test.getIn());
                }
            }

            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.currentThread().yield();
            }
        }
    }

    @Test
    @Ignore
    public void testFillinPipelineTests() throws Exception {

        PipelineLoader pipelineLoader = new PipelineLoader(
                new Controller().load());

        for (PipelineDef pipelineDef : allPipelineDefs()) {
            LOG.debug("testing {}", pipelineDef);

            UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                    pipelineDef.getName(), pipelineDef.getVersion());

            for (PipelineTest test : pipeline.getPipelineDef().getTests()) {

                if (test.getOut() == null || test.getOut().toString().isEmpty()) {
                    String systemOut = pipeline.annotate(test.getIn());

                    LOG.debug("TEST: {}\nPROPOSED: {}", pipelineDef + "::"
                            + test.getIn(), systemOut);
                }
            }
        }
    }
}
