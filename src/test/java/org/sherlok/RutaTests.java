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
import static org.sherlok.utils.Create.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.SherlokResult;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO xLATER use faster test engines than opennlp
public class RutaTests {
    private static final Logger LOG = LoggerFactory.getLogger(RutaTests.class);

    @Test
    public void testValidateRutaScriptOk() throws Exception {

        PipelineDef pd = (PipelineDef) new PipelineDef()//
                .setScriptLines(list("DECLARE Ok;"))//
                .setName("testValidateRutaScriptOk");
        new UimaPipeline(pd, new ArrayList<EngineDef>());
    }

    @Test(expected = ValidationException.class)
    public void testValidateRutaScriptMissingColumn() throws Exception {

        PipelineDef pd = (PipelineDef) new PipelineDef()//
                .setScriptLines(list("DECLARE Wrong")) // missing ';' at end
                .setName("testValidateRutaScriptMissingColumn");
        new UimaPipeline(pd, new ArrayList<EngineDef>());
    }

    @Test(expected = ValidationException.class)
    @Ignore
    // FIXME testValidateRutaScriptWrongKeyword
    public void testValidateRutaScriptWrongKeyword() throws Exception {

        PipelineDef pd = (PipelineDef) new PipelineDef()//
                .setScriptLines(list("DECLAREeee Wrong;")) // wrong keyword
                .setName("testValidateRutaScriptWrongKeyword");
        new UimaPipeline(pd, new ArrayList<EngineDef>());
    }

    @Test
    @Ignore
    // FIXME testUimaEngineThenRuta
    public void testUimaEngineThenRuta() throws Exception {

        List<String> scriptLines = list(
                "ENGINE opennlp.segmenter.en:1.7.0;",//
                "WORDLIST CountriesList = 'countries.txt';",//
                "DECLARE Country;",//
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence{-> MARKFAST(Country, CountriesList)};");

        PipelineDef pd = (PipelineDef) new PipelineDef()
                .setScriptLines(scriptLines).setName("testUimaEngineThenRuta")
                .setVersion("0.1");

        UimaPipeline up = new PipelineLoader(new Controller().load()).load(pd);

        SherlokResult result = SherlokResult.parse(up
                .annotate("Italy is a nice country!"));

        assertEquals("should have 1 Sentence", 1, result.get("Sentence").size());
        assertEquals("should have 1 Country", 1, result.get("Country").size());
    }

    @Test
    public void testUimaEngineThenRuta2() throws Exception {

        List<String> scriptLines = list(
                "ENGINE opennlp.segmenter.en:1.7.0;",//
                "DECLARE MySentence;",//
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence{-> MARK(MySentence)};");

        PipelineDef pd = (PipelineDef) new PipelineDef()
                .setScriptLines(scriptLines).setName("testUimaEngineThenRuta2")
                .setVersion("0.1");

        UimaPipeline up = new PipelineLoader(new Controller().load()).load(pd);

        SherlokResult result = SherlokResult
                .parse(up
                        .annotate("A sample sentence that gets identified by OpenNLP."));

        assertEquals("should have 1 Sentence", 1, result.get("Sentence").size());
        assertEquals("should have 1 MySentence", 1, result.get("MySentence")
                .size());
    }

    @Test
    public void testRutaThenUimaEngines() throws Exception {

        List<String> scriptLines = list(
                "\"A sample sentence\" -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;",//
                "\"A\" -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;",//
                "\"sample\" -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;",//
                "\"sentence\" -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;",//
                "ENGINE opennlp.pos.en:1.7.0;");

        PipelineDef pd = (PipelineDef) new PipelineDef()
                .setScriptLines(scriptLines).setName("testRutaThenUimaEngines")
                .setVersion("0.1");

        UimaPipeline up = new PipelineLoader(new Controller().load()).load(pd);
        String json = up.annotate("A sample sentence.");
        LOG.debug(json);
        SherlokResult result = SherlokResult.parse(json);

        assertEquals("should have 1 Sentence", 1, result.get("Sentence").size());
        assertEquals("should have 3 tokens", 3, result.get("Token").size());
        assertEquals("should have 1 article", 1, result.get("ART").size());
        assertEquals("should have 2 nouns", 2, result.get("NN").size());
    }
}
