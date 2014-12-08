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

public class PipelineDefTest {

    public static PipelineDef getOpennlp_ners() {
        PipelineDef p = new PipelineDef().setDomain("dkpro")
                .addScriptLine("ENGINE opennlp.segmenter.en:1.6.2")
                .addScriptLine("ENGINE opennlp.pos.en:1.6.2")
                .addOutputAnnotation("dkpro.NamedEntity");
        p.setName("OpenNlpEnSegmenter");
        p.setVersion("1.6.2");
        return p;
    }

    @Test
    public void testWriteRead() throws Exception {

        File pf = new File("target/pipelineTest_" + currentTimeMillis()
                + ".json");
        PipelineDef p = getOpennlp_ners();
        FileBased.write(pf, p);
        PipelineDef p2 = FileBased.read(pf, PipelineDef.class);
        p2.validate("");
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getVersion(), p2.getVersion());
        assertEquals(p.getOutput().getAnnotations().size(), p2.getOutput()
                .getAnnotations().size());
    }
}
