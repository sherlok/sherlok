/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sherlok.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.sherlok.FileBased.allPipelineDefs;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.sherlok.Controller;
import org.sherlok.PipelineLoader;
import org.sherlok.UimaPipeline;
import org.sherlok.integration.MethodNameLoggerWatcher;
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
            String systemOut = pipeline.annotate(test.getInput());
            SherlokTests.assertEquals(test.getExpected(), systemOut,
                    test.getComparison());
        }
    }

    @Test
    @SuppressWarnings("static-access")
    public void testAllPipelines() throws Exception {

        // skip this test if "skipSlowTests" is set to true
        assumeFalse("true".equals(System.getProperty("skipSlowTests")));

        PipelineLoader pipelineLoader = new PipelineLoader(
                new Controller().load());

        for (PipelineDef pipelineDef : allPipelineDefs()) {
            LOG.debug("testing {}", pipelineDef);

            UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                    pipelineDef.getName(), pipelineDef.getVersion());

            for (PipelineTest test : pipeline.getPipelineDef().getTests()) {

                if (test.getExpected() != null
                        && !test.getExpected().toString().isEmpty()) {
                    String systemOut = pipeline.annotate(test.getInput());
                    SherlokTests.assertEquals(test.getExpected(), systemOut,
                            test.getComparison());
                } else {
                    LOG.debug("  no output for {}", test.getInput());
                }
            }

            for (int i = 0; i < 5; i++) {
                System.gc();
                Thread.currentThread().yield();
            }
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
}
