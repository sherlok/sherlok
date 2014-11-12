package org.sherlok;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sherlok.Store;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.TypesDef;
import org.sherlok.mappings.TypesDef.TypeDef;

public class StoreTest {

    @Test
    public void test() {
        Store store = new Store().load();

        PipelineDef pd = store.getPipelineDef("opennlp_en_ners:1.6.2");
        assertEquals(true, pd.isLoadOnStartup());
        assertEquals(4, pd.getEngines().size());
        assertEquals("OpenNlpEnSegmenter:1.6.2", pd.getEngines().get(0).getId());

        
        EngineDef ed = store.getEngineDef("OpenNlpEnSegmenter:1.6.2");
        assertEquals(1, ed.getParameters().size());
        assertEquals("en", ed.getParameter("language"));

        TypeDef td = store.getTypeDef("dkpro.NamedEntity");
        assertEquals("de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                td.getClassz());

        // TODO more
    }
}
