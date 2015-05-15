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
package org.sherlok.mappings;

import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mapped JSON response from Sherlok server, and methods to access the
 * annotations.
 *
 * @author renaud@apache.org
 */
public class SherlokResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** UIMA's 'default' view */
    public static final String INITIAL_VIEW = "_InitialView";

    @JsonProperty("_referenced_fss")
    private Map<Integer, JsonAnnotation> referencedFss;

    @JsonProperty("_views")
    /** k: view-name, v: (k: annot-name, v: list of annot or ids). */
    private Map<String, Map<String, List<Object>>> views;

    @JsonProperty("_context")
    private Map<String, Object> context;

    @JsonProperty("_stats")
    private Map<String, Object> stats;

    @JsonProperty("_types")
    private Map<String, Object> types;

    private SherlokResult() { // hide, use parse()
    }

    /**
     * @param json
     *            the raw JSON from Sherlok
     * @return the mapped {@link SherlokResult}
     */
    @JsonIgnore
    public static SherlokResult parse(String json) throws JsonParseException,
            JsonMappingException, IOException {
        return MAPPER.readValue(json, SherlokResult.class);
    }

    /**
     * Low level API: this is usually not necessary, because we use the
     * {@link #INITIAL_VIEW}.
     *
     * @return {@link JsonAnnotation}s from that specific view.
     * @see SherlokResult#getAnnotations()
     */
    @JsonIgnore
    public Map<String, List<JsonAnnotation>> getAnnotations(String view) {
        Map<String, List<JsonAnnotation>> ret = map();

        for (Entry<String, List<Object>> as : views.get(view).entrySet()) {
            String type = as.getKey();
            List<JsonAnnotation> l = list();
            for (Object a : as.getValue()) {
                if (a instanceof Integer) { // reference, e.g. w/ parser output
                    l.add(referencedFss.get(a).removeProperty("_type"));
                } else {
                    l.add(MAPPER.convertValue(a, JsonAnnotation.class));
                }
            }
            ret.put(type, l);
        }
        return ret;
    }

    /**
     * @return all annotations as a map (key: annotation-type, value: a list of
     *         all {@link JsonAnnotation}s for that annotation-type)
     */
    @JsonIgnore
    public Map<String, List<JsonAnnotation>> getAnnotations() {
        return getAnnotations(INITIAL_VIEW);
    }

    /**
     * @param type
     *            the Annotation type
     * @return only {@link JsonAnnotation}s of that type
     */
    @JsonIgnore
    public List<JsonAnnotation> get(String type) {
        return getAnnotations().get(type);
    }

    /** @return the text that was annotated. */
    @JsonIgnore
    public String getText() {
        return referencedFss.get(1).getProperty("sofaString").toString();
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) { // should not happen...
            return getText();
        }
    }
}
