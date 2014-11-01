package enelpy_core;

import java.io.IOException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

public class SingleAbstractReader extends JCasCollectionReader_ImplBase {

    public static String getText() {
        return "The quick brown fox jumps over the lazy dog";
    }

    /** only true for one doc, then false */
    private boolean hasNext = true;

    @Override
    public void getNext(JCas jcas) throws IOException, CollectionException {

        hasNext = false;
jcas.setDocumentLanguage("en");
        jcas.setDocumentText(getText());
    }

    public boolean hasNext() throws IOException, CollectionException {
        return hasNext;
    }

    public Progress[] getProgress() {// nope
        return null;
    }
}
