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
import static org.junit.Assert.assertTrue;
import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Rule;
import org.junit.Test;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.PipelineDef.TestAnnotation;
import org.sherlok.utils.SherlokTests;
import org.sherlok.utils.Strings;
import org.slf4j.Logger;

public class PipelineLoaderIntegrationTest {
    private static Logger LOG = getLogger(PipelineLoaderIntegrationTest.class);

    public static final String TEST_TEXT = "Jack Burton (born April 29, 1954 in El Paso), "
            + "also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.";

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @Test
    public void testNaturalOrdering() {

        assertEquals(0, "a".compareTo("a"));
        assertEquals(1, "b".compareTo("a"));
        assertEquals(1, "2".compareTo("1"));
        assertTrue(1 < "a".compareTo("1"));

        assertEquals(1, Strings.compareNatural("b", "a"));
        assertEquals(1, Strings.compareNatural("a", "1"));
        assertTrue(0 < Strings.compareNatural("1.2.4", "1.2.2"));
        assertTrue(0 < Strings.compareNatural("1.22.4", "1.2.2"));
        assertTrue(0 < Strings.compareNatural("1.22.4", "1.22"));
        assertTrue(0 < Strings.compareNatural("1.2.a", "1.2.1"));
        assertTrue(0 < Strings.compareNatural("1.21.1", "1.2.a"));
    }

    @Test
    public void testResolve() throws Exception {

        UimaPipeline pipeline = new PipelineLoader(new Controller().load())
                .resolvePipeline("opennlp.ners.en", null);
        String result = (pipeline.annotate(TEST_TEXT));
        LOG.debug(result);

        SherlokTests.assertEquals(list(//
                new TestAnnotation().setBegin(0).setEnd(11)
                        .setType("NamedEntity").addProperty("value", "person")//
                ), result, Comparison.atLeast);
    }
}
