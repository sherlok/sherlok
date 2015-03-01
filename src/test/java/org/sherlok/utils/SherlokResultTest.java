package org.sherlok.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sherlok.mappings.PipelineDef.TestAnnotation;

public class SherlokResultTest {

    @Test
    public void test() throws Exception {

        String json = IOUtils.toString(SherlokResultTest.class
                .getResourceAsStream("SherlokResult.json"), UTF_8);

        SherlokResult result = SherlokResult.parse(json);

        Map<Integer, TestAnnotation> annotations = result.getAnnotations();
        assertEquals(9, annotations.size());

        Map<Integer, TestAnnotation> sentences = result
                .get("Sentence");
        assertEquals(1, sentences.size());

        assertEquals(0, result.get("bogus").size());
    }
}
