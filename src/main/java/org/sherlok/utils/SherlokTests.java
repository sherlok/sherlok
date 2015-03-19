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

import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.ValidationException.ERR_NOTFOUND;
import static org.sherlok.utils.ValidationException.EXPECTED;
import static org.sherlok.utils.ValidationException.SYSTEM;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.sherlok.mappings.JsonAnnotation;
import org.sherlok.mappings.PipelineDef.PipelineTest.Comparison;
import org.sherlok.mappings.SherlokResult;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SherlokTests {

    public static Map<String, List<JsonAnnotation>> assertEquals(
            Map<String, List<JsonAnnotation>> expecteds, String systemString,
            Comparison comparison) throws ValidationException, JSONException,
            JsonProcessingException {

        // parse
        Map<String, List<JsonAnnotation>> systems = null;
        try {
            systems = SherlokResult.parse(systemString).getAnnotations();
        } catch (IOException e) {
            throw new ValidationException("could not parse systemAnnots "
                    + systemString, e);
        }

        // validate
        switch (comparison) {
        case atLeast:
        case exact:// FIXME implement exact comparison
            for (Entry<String, List<JsonAnnotation>> exp : expecteds.entrySet()) {
                String eType = exp.getKey();
                if (!systems.containsKey(eType)) {
                    throw new ValidationException(map(ERR_NOTFOUND, eType,
                            EXPECTED, expecteds, SYSTEM, systems));
                } else {
                    for (JsonAnnotation a : exp.getValue()) {
                        boolean found = false;
                        for (JsonAnnotation sa : systems.get(eType)) {
                            if (sa.equals(a)) {
                                found = true;break;
                            }
                        }
                        if (!found) {
                            throw new ValidationException(map(ERR_NOTFOUND, a,
                                    EXPECTED, expecteds, SYSTEM, systems));
                        }
                    }
                }
            }
            break;

        /*-
        // compare 2-ways; give explicit error msg
        for (Entry<String, List<Annotation>> exp : expecteds.entrySet()) {
            if (!systems.values().contains(exp)) {
                throw new ValidationException(map(ERR_NOTFOUND, exp,
                        EXPECTED, expecteds, SYSTEM, systems));
            }
        }
        for (List<Annotation> sysAs : systems.values()) {
            for (Annotation sysa : sysAs) {
                if (!expecteds.values().contains(sysa)) {
                    throw new ValidationException(map(ERR_UNEXPECTED, sysa,
                            EXPECTED, expecteds, SYSTEM, systems));
                }
            }
        }
        break;
         */
        }
        return systems;
    }

}
