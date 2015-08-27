package org.sherlok;

import static org.junit.Assume.assumeFalse;
import static org.sherlok.FileBased.allPipelineDefs;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineTest;
import org.sherlok.utils.MavenPom;
import org.sherlok.utils.SherlokTests;
import org.slf4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class SherlokPipelineTest {
    protected static final Logger LOG = getLogger(SherlokPipelineTest.class);

    public static void testPipeline(String pipelineId) throws Exception {

        // skip this test if "skipSlowTests" is set to true
        //FIXME trying assumeFalse("true".equals(System.getProperty("skipSlowTests")));

        Controller controller = new Controller().load();
        PipelineDef pipelineDef = controller.getPipelineDef(pipelineId);
        LOG.debug("testing {}", pipelineDef);

        boolean hasExpectations = false;
        for (PipelineTest t : pipelineDef.getTests()) {
            if (!t.getExpected().isEmpty()) {
                hasExpectations = true;
                break;
            }
        }

        if (hasExpectations) {

            PipelineLoader pipelineLoader = new PipelineLoader(controller);
            UimaPipeline p = pipelineLoader.resolvePipeline(
                    pipelineDef.getName(), pipelineDef.getVersion());

            for (PipelineTest test : p.getPipelineDef().getTests()) {

                if (test.getExpected() != null
                        && !test.getExpected().toString().isEmpty()) {
                    String systemOut = p.annotate(test.getInput());
                    SherlokTests.assertEquals(test.getExpected(), systemOut,
                            test.getComparison());
                } else {
                    LOG.debug("  no output for {}", test.getInput());
                }
            }

            LOG.debug("+++passed                                      ");

        } else {
            LOG.debug("---no tests                                      ");
        }
    }

    // util to propose tests for pipline that have none
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

                if (test.getExpected() == null
                        || test.getExpected().toString().isEmpty()) {
                    String systemOut = pipeline.annotate(test.getInput());

                    LOG.debug("TEST: {}\nPROPOSED: {}", pipelineDef + "::"
                            + test.getInput(), systemOut);
                }
            }
        }
    }

    @Test
    @Ignore
    public void testAnnotateDogs() throws Exception {
        testPipeline("01.ruta.annotate.dog:1");
    }

    // generates
    public static void main(String[] args) throws Exception {

        for (String p : new Controller().load().listPipelineDefNames()) {

            String className = "Pipeline_" + p.replaceAll("[^a-zA-Z0-9]", "_")
                    + "Test";
            LOG.info("generating test classes for pipeline " + p
                    + " with name " + className);

            File testFile = new File("src/test/java/org/sherlok/pipelinetests/"
                    + className + ".java");
            if (!testFile.exists()) {

                // Freemarker configuration object
                Configuration cfg = new Configuration();
                cfg.setClassForTemplateLoading(MavenPom.class, "/");

                Template template = cfg.getTemplate("sherlokPipelineTest.ftl");

                // Bind variables
                Map<String, Object> data = new HashMap<String, Object>();

                data.put("className", className);
                data.put("pipelineId", p);

                Writer writer = new FileWriter(testFile);
                template.process(data, writer);
                writer.close();
                LOG.info("new testfile written to '{}'", testFile.getName());
            }
        }
    }
}
