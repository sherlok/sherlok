package org.sherlok;

import static org.sherlok.utils.Create.list;

import java.util.List;

import org.junit.Test;
import org.sherlok.mappings.EngineDef;
import org.sherlok.utils.ValidationException;

public class RutaTests {

    @Test(expected = ValidationException.class)
    public void testValidateRutaScript() throws Exception {

        List<EngineDef> engineDefs = list();
        List<String> scriptLines = list("DECLLLLARE wrong");

        UimaPipeline uimaPipeline = new UimaPipeline("validateRutaScript",
                "en", engineDefs, scriptLines);

        uimaPipeline.initialize();
    }
}
