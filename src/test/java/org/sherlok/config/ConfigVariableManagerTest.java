package org.sherlok.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sherlok.mappings.Def;
import org.sherlok.utils.ops.FileOps;

public class ConfigVariableManagerTest {

    private static final Def TEST_DEF = createValueDef();

    private static final String TEST_URL = "https://github.com/sherlok/sherlok_dependency_test.git";
    private static final String FILE_CONTENT_MASTER = "MASTER\n";

    private static Def createValueDef() {
        Def def = new DummyDef();

        Map<String, String> var = map("type", "git", "url", TEST_URL);
        def.addRawConfig("var", var);

        Map<String, String> x = map("value", "resources");
        def.addRawConfig("x", x);

        Map<String, String> y = map("value", "file.txt", "type", "text");
        def.addRawConfig("y", y);

        Map<String, String> z = map("value", "ZZ");
        def.addRawConfig("z", z);

        return def;
    }

    @Test
    public final void testProcessConfigVariables()
            throws NoSuchVariableException, ProcessConfigVariableException,
            IOException {
        List<String> input = list("$var/$x/$y", "$$", "$$x/$x", "$$$$z$z$$$z");
        List<String> output = ConfigVariableManager.processConfigVariables(
                input, TEST_DEF);

        assertEquals(input.size(), output.size());
        assertTrue(output.get(0) + " ends with the proper suffix", output
                .get(0).endsWith("/resources/file.txt"));
        String content = FileOps.readContent(new File(output.get(0)));
        assertEquals(content, FILE_CONTENT_MASTER);
        assertEquals("$", output.get(1));
        assertEquals("$x/resources", output.get(2));
        assertEquals("$$zZZ$$z", output.get(3));
    }
    
    private static class DummyDef extends Def {
        /* Because Def is abstract */
    }

}
