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
package org.sherlok.ruta.extensions;

import static org.sherlok.utils.Create.map;

import java.util.Map;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.ruta.engine.Ruta;
import org.junit.Ignore;
import org.junit.Test;

public class OboActionTest {

    @Test
    @Ignore
    // TODO implement OBO annotator
    public void test() throws Exception {

        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("The red fox jumps over the blue fence");

        String script = "PACKAGE org.sherlok.example;\n" + "DECLARE Blah;\n"
                + "Document{->OBO(Blah, \"test.obo\")};";
        Map<String, Object> parameters = map();

        parameters.put("additionalExtensions",
                new String[] { OboActionExtension.class.getName() });

        Ruta.apply(jCas.getCas(), script, parameters);
    }
}
