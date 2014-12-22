package org.sherlok;

import static org.sherlok.utils.Create.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.ValidationException;

public class RutaTests {

    @Test(expected = ValidationException.class)
    public void testValidateRutaScript() throws Exception {

        List<EngineDef> engineDefs = list();
        List<String> scriptLines = list("DECLLLLARE wrong");

        new UimaPipeline("validateRutaScript", "en", engineDefs, scriptLines,
                new ArrayList<String>(), new ArrayList<String>());
    }
}
