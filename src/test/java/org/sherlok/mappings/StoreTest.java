package org.sherlok.mappings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sherlok.mappings.TypesDef.TypeDef;

public class StoreTest {

    @Test
    public void test() {
        Store store = new Store().load();

        PipelineDef pd = store.getPipelineDef("OpenNlpEnNers:1.6.2");
        assertEquals(true, pd.isLoadOnStartup());
        assertEquals(1, pd.getEngines().size());
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
