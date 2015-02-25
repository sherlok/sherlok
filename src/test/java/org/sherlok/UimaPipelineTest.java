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
package org.sherlok;

import static org.sherlok.mappings.PipelineDef.PipelineTest.Comparison.atLeast;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Test;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.PipelineDef.TestAnnotation;
import org.sherlok.utils.SherlokTests;
import org.slf4j.Logger;

public class UimaPipelineTest {
    private static Logger LOG = getLogger(UimaPipelineTest.class);

    @Test
    public void testDog() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("01.ruta.annotate.dog", null);
        String result = pipeline.annotate("dog");
        LOG.debug(result);
        SherlokTests.assertEquals(
                map("1",
                        new TestAnnotation().setBegin(0).setEnd(3)
                                .setType("Dog")), result, Comparison.exact);
    }

    @Test
    public void testCountries() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("02.ruta.annotate.countries", null);
        String result = (pipeline.annotate("Switzerland"));
        LOG.debug(result);
        SherlokTests.assertEquals(
                map("1",
                        new TestAnnotation().setBegin(0).setEnd(11)
                                .setType("Country")), result, Comparison.exact);
    }

    @Test
    public void testMaltParser() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("maltparser.en", null);
        String result = (pipeline.annotate("The dog walks on the lake."));
        LOG.debug(result);
        SherlokTests.assertEquals(
                map("678",
                        new TestAnnotation().setBegin(0).setEnd(3)
                                .setType("Dependency")
                                .addProperty("Dependent", 129)
                                .addProperty("Governor", 137)
                                .addProperty("DependencyType", "det")), result,
                atLeast);
    }
}
