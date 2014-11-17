package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.mappings.TypesDef.TypeDef;

public class TypesDefTest {

    public static TypesDef getOpennlpTypes() {
        TypesDef t = new TypesDef()
                .addType(new TypeDef()
                        .setShortName("dkpro.NamedEntity")
                        .setDescription("The desc")
                        .setColor("red")
                        .setClassz(
                                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity")
                        .addProperty("value"));
        return t;
    }

    @Test
    public void testWriteRead() throws Exception {

        File tf = new File("target/typesTest_" + currentTimeMillis() + ".json");
        TypesDef t = getOpennlpTypes();
        FileBased.write(tf, t);
        TypesDef t2 = FileBased.read(tf, TypesDef.class);
        assertEquals(t.getTypes().size(), t2.getTypes().size());
        assertEquals(t.getTypes().get(0).getClassz(), t.getTypes().get(0)
                .getClassz());
        assertEquals(t.getTypes().get(0).getColor(), t.getTypes().get(0)
                .getColor());
        assertEquals(t.getTypes().get(0).getDescription(), t.getTypes().get(0)
                .getDescription());
        assertEquals(t.getTypes().get(0).getShortName(), t.getTypes().get(0)
                .getShortName());
        assertEquals(t.getTypes().get(0).getProperties().get(0), t.getTypes()
                .get(0).getProperties().get(0));
    }

    @Test
    public void testShortName() {
        TypesDef t = new TypesDef().addType(new TypeDef().setClassz("a.b.C"));
        assertEquals("C", t.getTypes().get(0).getShortName());

        TypesDef t2 = new TypesDef().addType(new TypeDef().setClassz("Ccc"));
        assertEquals("Ccc", t2.getTypes().get(0).getShortName());
    }
}
