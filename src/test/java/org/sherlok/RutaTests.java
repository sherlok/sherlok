package org.sherlok;

import static org.sherlok.utils.Create.list;

import java.util.List;

import org.junit.Test;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.ValidationException;

public class RutaTests {

    @Test(expected = ValidationException.class)
    public void testValidateRutaScript() throws Exception {

        List<EngineDef> engineDefs = list();
        List<String> scriptLines = list("DECLARE Wrong"); // missing ';' at end

        PipelineDef pd = (PipelineDef) new PipelineDef()//
                .setLanguage("en")//
                .setScriptLines(scriptLines)//
                .setName("validateRutaScript");

        new UimaPipeline(pd, engineDefs);
    }

    @Test
    public void testValidateRutaScript2() throws Exception {

        List<EngineDef> engineDefs = list();
        List<String> scriptLines = list("DECLARE Ok;");

        PipelineDef pd = (PipelineDef) new PipelineDef()//
                .setLanguage("en")//
                .setScriptLines(scriptLines)//
                .setName("validateRutaScript");

        new UimaPipeline(pd, engineDefs);
    }

    // TODO test with DECLAREeee Wrong;
}
