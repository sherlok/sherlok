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

import org.junit.Test;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;

public class ControllerTest {

    @Test
    public void test() throws Exception {
        Controller controller = new Controller().load();

        PipelineDef pd = controller.getPipelineDef("opennlp.ners.en:1.6.2");
        assertEquals(false, pd.isLoadOnStartup());
        assertEquals(4, pd.getEnginesFromScript().size());
        assertEquals("ENGINE opennlp.segmenter.en:1.6.2;", pd.getScriptLines()
                .get(0));

        EngineDef ed = controller.getEngineDef("opennlp.segmenter.en:1.6.2");
        assertEquals(1, ed.getParameters().size());
        assertEquals("en", ed.getParameter("language").get(0));

        // TODO more validation
    }
}
