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
package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.utils.Create;

public class EngineTest {
//FIXME
//    public static EngineDef getOpenNlpEnSegmenter() {
//        EngineDef e = new EngineDef()
//                .setDomain("dkpro")
//                .setClassz(
//                        "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter")
//                .setBundleId("dkpro_opennlp_en:1.6.2")
//                .addParameter("language", Create.list( "en"));
//        e.setName("OpenNlpEnSegmenter");
//        e.setVersion("1.6.2");
//
//        return e;
//    }
//
//    @Test
//    public void testWriteRead() throws Exception {
//
//        File ef = new File("target/engineTest_" + currentTimeMillis() + ".json");
//        EngineDef e = getOpenNlpEnSegmenter();
//        FileBased.write(ef, e);
//        EngineDef e2 = FileBased.read(ef, EngineDef.class);
//        e2.validate("");
//        assertEquals(e.getName(), e2.getName());
//        assertEquals(e.getVersion(), e2.getVersion());
//        assertEquals(e.getParameters().size(), e2.getParameters().size());
//    }
}
