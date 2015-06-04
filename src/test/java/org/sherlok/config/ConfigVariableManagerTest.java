/**
 * Copyright (C) 2014-2015 Renaud Richardet
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
package org.sherlok.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sherlok.config.GitConfigVariableTest.FILE_CONTENT_MASTER;
import static org.sherlok.config.GitConfigVariableTest.TEST_URL;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.mappings.Def;
import org.sherlok.utils.ops.FileOps;

public class ConfigVariableManagerTest {

    private static final Def TEST_DEF = createValueDef();

    private static Def createValueDef() {
        Def def = new DummyDef();

        Map<String, String> var = map("type", "git", "url", TEST_URL, "mode",
                "ruta");
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

        assertEquals(
                "each input value should have a corresponding output value",
                input.size(), output.size());
        assertTrue(output.get(0) + " ends with the proper suffix", output
                .get(0).endsWith("/resources/file.txt"));
        String content = FileOps.readContent(new File(
                FileBased.RUTA_RESOURCES_PATH, output.get(0)));
        assertEquals("the downloaded content should be as expected", content,
                FILE_CONTENT_MASTER);
        assertEquals("$$ should be transformed into $", "$", output.get(1));
        assertEquals("$$x shoud be transformed into $x", "$x/resources",
                output.get(2));
        assertEquals("excapting $ should work as intended", "$$zZZ$$z",
                output.get(3));
    }
    
    private static class DummyDef extends Def {
        /* Because Def is abstract */
    }

}
