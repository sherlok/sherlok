package org.sherlok.utils;

import static org.sherlok.utils.Create.list;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.PipelineDef.TestAnnotation;

public class SherlokTests {

    public static void assertEquals(List<TestAnnotation> expectedAnnots,
            String system, Comparison comparison) throws ValidationException {

        // parse
        List<TestAnnotation> systemAnnots = null;
        try {
            systemAnnots = parseRaw(system);
        } catch (JSONException e) {
            throw new ValidationException("could not parse systemAnnots "
                    + system, e);
        }

        // validate
        switch (comparison) {
        case atLeast:
            for (TestAnnotation exp : expectedAnnots) {
                if (!systemAnnots.contains(exp)) {
                    throw new ValidationException("Expected '" + exp
                            + "', but was not found in SYSTEM: " + system);
                }
            }
            break;

        case exact: // compare 2-ways; give explicit error msg
            for (TestAnnotation exp : expectedAnnots) {
                if (!systemAnnots.contains(exp)) {
                    throw new ValidationException("Expected '" + exp
                            + "', but was not found in SYSTEM: " + system);
                }
            }
            for (TestAnnotation sys : systemAnnots) {
                if (!expectedAnnots.contains(sys)) {
                    throw new ValidationException("System found '" + sys
                            + "', but was not found in EXPECTED: " + system);
                }
            }
            break;
        }
    }

    public static TestAnnotation parse(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);

        TestAnnotation t = new TestAnnotation();

        t.setBegin(json.optInt("begin", 0));// happens if =0
        t.setEnd(json.optInt("end", 0));// happens for Sofa
        t.setType(json.getString("@type"));

        // parse remaining properties
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            t.addProperty(key, json.getString(key));
        }
        return t;
    }

    /**
     * @param jsonStr
     *            the raw json string returned by uimaPipeline.annotate()
     * @return a {@link List} of the parsed {@link TestAnnotation}s
     */
    public static List<TestAnnotation> parseRaw(String jsonStr)
            throws JSONException {
        List<TestAnnotation> ret = list();
        JSONObject annots = new JSONObject(jsonStr)
                .getJSONObject("annotations");
        Iterator<?> keys = annots.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            TestAnnotation a = parse(annots.getString(key));
            // skip Sofa and DocumentAnnotation
            if (!a.getType().equals("Sofa")
                    && !a.getType().equals("DocumentAnnotation"))
                ret.add(a);
        }
        return ret;

    }
}
