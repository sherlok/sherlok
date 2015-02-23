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
