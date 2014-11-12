package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.mappings.TypesDef.TypeDef;

public class TypesDefTest {

    public static TypesDef getOpennlpTypes() {
        TypesDef e = new TypesDef()
                .addType(new TypeDef()
                        .setShortName("dkpro.NamedEntity")
                        .setClassz(
                                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity")
                        .addProperty("value"));
        return e;
    }

    @Test
    public void testWriteRead() throws Exception {

        File tf = new File("target/typesTest_" + currentTimeMillis() + ".json");
        TypesDef t = getOpennlpTypes();
        FileBased.MAPPER.writeValue(tf, t);
        TypesDef t2 = FileBased.loadTypes(tf);
        t2.validate();
        assertEquals(t.getTypes().size(), t2.getTypes().size());
        // TODO more
    }
}
