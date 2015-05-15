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
package org.sherlok.mappings;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SherlokResultTest {

    @Test
    public void testParseResults() throws Exception {

        String json = IOUtils.toString(SherlokResultTest.class
                .getResourceAsStream("SherlokResult.json"), UTF_8);

        SherlokResult result = SherlokResult.parse(json);

        Map<String, List<JsonAnnotation>> annotations = result.getAnnotations();
        assertEquals(8, annotations.size());
        List<JsonAnnotation> layers = annotations.get("Layer");
        assertEquals(2, layers.size());
        for (JsonAnnotation l : layers) {
            Object oid = l.getProperty("ontologyId");
            assertTrue(oid instanceof String);
            assertTrue(oid.toString().startsWith("HBP_LAYER"));
        }

        assertEquals("layer V and layer iii large pyramidal neurons",
                result.getText());

        assertNull(result.get("bogus"));
    }

    /** These results have references. */
    @Test
    public void testParseResultsFromBerkleyparser() throws Exception {

        String json = IOUtils.toString(SherlokResultTest.class
                .getResourceAsStream("SherlokResult2.json"), UTF_8);

        SherlokResult result = SherlokResult.parse(json);

        Map<String, List<JsonAnnotation>> annotations = result.getAnnotations();
        assertEquals(9, annotations.size());
        List<JsonAnnotation> layers = annotations.get("VP");
        assertEquals(2, layers.size());
        for (JsonAnnotation l : layers) {
            assertEquals(41, l.getEnd());
        }

        assertEquals("The blue house of my childhood was bought: what a pity!",
                result.getText());
    }
}
