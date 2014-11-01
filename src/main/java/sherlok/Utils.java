package sherlok;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

public class Utils {

    public static JCas getCas(String text) throws UIMAException {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");// important for DK pro components TODO
        return jCas;
    }
}
