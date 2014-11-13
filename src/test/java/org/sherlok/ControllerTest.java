package org.sherlok;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.TypesDef.TypeDef;

public class ControllerTest {

    @Test
    public void test() throws Exception {
        Controller controller = new Controller().load();

        PipelineDef pd = controller.getPipelineDef("opennlp_en_ners:1.6.2");
        assertEquals(true, pd.isLoadOnStartup());
        assertEquals(4, pd.getEngines().size());
        assertEquals("OpenNlpEnSegmenter:1.6.2", pd.getEngines().get(0).getId());

        
        EngineDef ed = controller.getEngineDef("OpenNlpEnSegmenter:1.6.2");
        assertEquals(1, ed.getParameters().size());
        assertEquals("en", ed.getParameter("language"));

        TypeDef td = controller.getTypeDef("dkpro.NamedEntity");
        assertEquals("de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                td.getClassz());

        // TODO more validation        
    }
}
