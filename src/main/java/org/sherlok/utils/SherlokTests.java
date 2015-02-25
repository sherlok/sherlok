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
package org.sherlok.utils;

import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.ValidationException.ERR_EXPECTED;
import static org.sherlok.utils.ValidationException.ERR_UNEXPECTED;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.PipelineDef.TestAnnotation;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SherlokTests {

    public static void assertEquals(Map<String, TestAnnotation> expecteds,
            String systemString, Comparison comparison)
            throws ValidationException, JSONException, JsonProcessingException {

        // parse
        List<TestAnnotation> systems = null;
        try {
            systems = parseRaw(systemString);
        } catch (JSONException e) {
            throw new ValidationException("could not parse systemAnnots "
                    + systemString, e);
        }

        // validate
        switch (comparison) {
        case atLeast:
            for (TestAnnotation exp : expecteds.values()) {
                if (!systems.contains(exp)) {
                    throw new ValidationException(map(ERR_EXPECTED, exp,
                            "expecteds", expecteds, "system", systems));
                }
            }
            break;

        case exact: // compare 2-ways; give explicit error msg
            for (TestAnnotation exp : expecteds.values()) {
                if (!systems.contains(exp)) {
                    throw new ValidationException(map(ERR_EXPECTED, exp,
                            "expecteds", expecteds, "system", systems));
                }
            }
            for (TestAnnotation sys : systems) {
                if (!expecteds.values().contains(sys)) {
                    throw new ValidationException(map(ERR_UNEXPECTED, sys,
                            "expecteds", expecteds, "system", systems));
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
