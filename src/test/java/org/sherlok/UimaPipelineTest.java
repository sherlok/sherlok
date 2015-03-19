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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.Test;
import org.sherlok.mappings.JsonAnnotation;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineOutput;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.SherlokResult;
import org.sherlok.utils.Create;
import org.sherlok.utils.SherlokTests;
import org.slf4j.Logger;

public class UimaPipelineTest {
    private static Logger LOG = getLogger(UimaPipelineTest.class);

    @Test
    public void testDog() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("01.ruta.annotate.dog", null);
        String json = pipeline.annotate("dog");
        LOG.debug(json);
        SherlokTests.assertEquals(
                map("Dog", list(new JsonAnnotation().setBegin(0).setEnd(3))),
                json, Comparison.exact);
    }

    @Test
    public void testLangdetect() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("langdetect", null);

        String json = pipeline.annotate("C'est vraiment chouette comme truc.");

        List<JsonAnnotation> doc = SherlokResult.parse(json).get(
                "DocumentAnnotation");
        assertEquals("fr", doc.get(0).getProperties().get("language"));
    }

    @Test
    public void testCountries() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("02.ruta.annotate.countries", null);
        String json = (pipeline.annotate("Switzerland"));
        LOG.debug(json);
        SherlokTests.assertEquals(
                map("Country", Create.list(new JsonAnnotation().setBegin(0)
                        .setEnd(11))), json, Comparison.exact);
    }

    @Test
    public void testMaltParser() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("maltparser.en", null);
        String json = (pipeline.annotate("The dog walks on the lake."));
        SherlokResult result = SherlokResult.parse(json);
        LOG.debug(result.toString());
        List<JsonAnnotation> deps = result.get("Dependency");

        assertEquals(6, deps.size());
        JsonAnnotation dep = deps.get(0);
        assertEquals("det", dep.getProperty("DependencyType"));
    }

    @Test
    public void testFiltering() throws Exception {

        List<String> scriptLines = list("ENGINE opennlp.segmenter.en:1.7.0;", //
                "ENGINE opennlp.pos.en:1.7.0;");

        PipelineDef pd = (PipelineDef) new PipelineDef()
                .setScriptLines(scriptLines)
                .setOutput(
                        new PipelineOutput().setAnnotationFilters(list( //
                                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription")))
                .setName("testUseUimaEngines").setVersion("0.1");

        UimaPipeline up = new PipelineLoader(new Controller().load()).load(pd);
        String json = up.annotate("A sample sentence.");
        LOG.debug(json);
        SherlokResult result = SherlokResult.parse(json);

        assertEquals("should have 1 sentence", 1, result.get("Sentence").size());
        assertEquals("should have 4 tokens", 4, result.get("Token").size());
        assertEquals("should have 1 article", 1, result.get("ART").size());
        assertEquals("should have 2 nouns", 2, result.get("NN").size());
        assertEquals("should have 1 punctuation", 1, result.get("PUNC").size());
        assertNull("should have no more TagDescription",
                result.get("TagDescription"));
    }
}
