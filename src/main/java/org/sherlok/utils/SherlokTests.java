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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.sherlok.mappings.JsonAnnotation;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.SherlokException;
import org.sherlok.mappings.SherlokResult;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SherlokTests {

    public static Map<String, List<JsonAnnotation>> assertEquals(
            Map<String, List<JsonAnnotation>> expecteds, String actualsString,
            Comparison comparison) throws SherlokException, JSONException,
            JsonProcessingException {

        // parse
        Map<String, List<JsonAnnotation>> actuals = null;
        try {
            actuals = SherlokResult.parse(actualsString).getAnnotations();
        } catch (IOException e) {
            throw new SherlokException("could not parse actual annotations")
                    .setObject(actualsString).setDetails(e.getStackTrace());
        }

        // validate
        switch (comparison) {
        case atLeast:
            // For each annotation in expecteds we make sure it exists in
            // actuals
            for (Entry<String, List<JsonAnnotation>> entry : expecteds
                    .entrySet()) {

                String expectedKey = entry.getKey();
                List<JsonAnnotation> expectedValues = entry.getValue();

                // Get the corresponding annotations and make sure they exists
                List<JsonAnnotation> actualValues = actuals.get(expectedKey);
                if (actualValues == null) {
                    throw new SherlokException("could not find expected key",
                            expectedKey);// TODO check
                }

                for (JsonAnnotation expected : expectedValues) {
                    // Find an annotation that has the same begin and end in the
                    // actual annotations. Note that two annotations with the
                    // same key and same region are not supported by this test.
                    JsonAnnotation actual = null;
                    for (JsonAnnotation annotation : actualValues) {
                        if (expected.getBegin() == annotation.getBegin()
                                && expected.getEnd() == annotation.getEnd()) {
                            actual = annotation;
                            break;
                        }
                    }

                    // Make sure that such (begin, end) pair was found
                    if (actual == null) {
                        throw new SherlokException(
                                "could not find expected annotation",
                                expected.toString());// TODO
                    }

                    // Make sure that each expected properties exist
                    Map<String, Object> expectedProperties = expected
                            .getProperties();
                    Map<String, Object> actualProperties = actual
                            .getProperties();
                    for (Entry<String, Object> expectedProperty : expectedProperties
                            .entrySet()) {
                        Object actualProperty = actualProperties
                                .get(expectedProperty.getKey());

                        // This isn't a proper recursive solution: it means
                        // that sub-properties must be included in both
                        // expected and actual properties. Hence the "at least"
                        // test is not perfect.
                        if (!actualProperty.equals(expectedProperty.getValue())) {
                            throw new SherlokException("actual property <"
                                    + actualProperty
                                    + "> is not equal to expected property <"
                                    + expectedProperty + ">");
                        }
                    }
                }
            }

            break;

        case exact:// FIXME implement exact comparison
            for (Entry<String, List<JsonAnnotation>> exp : expecteds.entrySet()) {
                String eType = exp.getKey();
                if (!actuals.containsKey(eType)) {
                    throw new SherlokException("could not find expected key",
                            eType);

                } else {
                    for (JsonAnnotation a : exp.getValue()) {
                        boolean found = false;
                        for (JsonAnnotation sa : actuals.get(eType)) {
                            // FIXME currently it is not recursive
                            if (sa.equals(a)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new SherlokException(
                                    "could not find expected annotation",
                                    a.toString());
                        }
                    }
                }
            }
            break;

        /*-
        // compare 2-ways; give explicit error msg
        for (Entry<String, List<Annotation>> exp : expecteds.entrySet()) {
            if (!systems.values().contains(exp)) {
                throw new SherlokException(map(ERR_NOTFOUND, exp,
                        EXPECTED, expecteds, SYSTEM, systems));
            }
        }
        for (List<Annotation> sysAs : systems.values()) {
            for (Annotation sysa : sysAs) {
                if (!expecteds.values().contains(sysa)) {
                    throw new SherlokException(map(ERR_UNEXPECTED, sysa,
                            EXPECTED, expecteds, SYSTEM, systems));
                }
            }
        }
        break;
         */
        }
        return actuals;
    }
}
