package org.sherlok.uima.engines;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;
import static org.sherlok.uima.engines.KeepLargestAnnotationAnnotator.PARAM_ANNOTATION_CLASS;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.examples.type.Token;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class KeepLargestAnnotationAnnotatorTest {

    public static JCas getTestCas(String text) throws UIMAException {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage("en");
        return jCas;
    }

    @Test
    public void test() throws Exception {

        JCas jCas = getTestCas("bla cortex bla brain bla. bli bli brain bli bli");

        new Token(jCas, 4, 10).addToIndexes();
        new Token(jCas, 15, 20).addToIndexes();
        new Token(jCas, 34, 39).addToIndexes();
        runPipeline(
                jCas,
                createEngine(KeepLargestAnnotationAnnotator.class,
                        PARAM_ANNOTATION_CLASS, Token.class.getName()));
        assertEquals("no overlap, keeps all annots", 3,
                select(jCas, Token.class).size());

        // first annotation is covering other 2.
        jCas = getTestCas("bla cortex bla brain bla. bli bli brain bli bli");
        new Token(jCas, 4, 10).addToIndexes();
        new Token(jCas, 5, 10).addToIndexes();
        new Token(jCas, 6, 9).addToIndexes();
        runPipeline(
                jCas,
                createEngine(KeepLargestAnnotationAnnotator.class,
                        PARAM_ANNOTATION_CLASS, Token.class.getName()));
        assertEquals("first annotation is covering other 2", 1,
                select(jCas, Token.class).size());

        // overlap
        jCas = getTestCas("bla cortex bla brain bla. bli bli brain bli bli");
        new Token(jCas, 4, 10).addToIndexes();
        new Token(jCas, 5, 11).addToIndexes();
        runPipeline(
                jCas,
                createEngine(KeepLargestAnnotationAnnotator.class,
                        PARAM_ANNOTATION_CLASS, Token.class.getName()));
        assertEquals("overlap, keeps all annots", 2, select(jCas, Token.class)
                .size());
    }
}
