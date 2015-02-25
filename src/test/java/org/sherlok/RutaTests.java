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
