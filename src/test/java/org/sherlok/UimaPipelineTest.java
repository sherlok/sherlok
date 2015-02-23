/**
 * Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
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

import static org.junit.Assert.assertEquals;
import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

import org.json.JSONArray;
import org.json.JSONObject;
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
        SherlokTests
                .assertEquals(list(new TestAnnotation().setBegin(0).setEnd(3)
                        .setType("Dog")), result, Comparison.exact);
    }

    @Test
    public void testCountries() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("02.ruta.annotate.countries", null);
        String result = (pipeline.annotate("Switzerland"));
        LOG.debug(result);
        SherlokTests.assertEquals(
                list(new TestAnnotation().setBegin(0).setEnd(11)
                        .setType("Country")), result, Comparison.exact);
    }

    @Test
    public void testMaltParser() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("maltparser.en", null);
        String result = (pipeline.annotate("The dog walks on the lake."));
        LOG.debug(result);

        JSONObject jsonObject = new JSONObject(result);
        JSONObject annotations = jsonObject.getJSONObject("annotations");
        JSONArray names = annotations.names();
        assertEquals(8, names.length());
        Object country = annotations.get("678");
        assertEquals(
                "{\"sofa\":1,\"Governor\":137,\"Dependent\":129,\"@type\":\"Dependency\",\"DependencyType\":\"det\",\"end\":3}",
                country.toString());
    }
}
