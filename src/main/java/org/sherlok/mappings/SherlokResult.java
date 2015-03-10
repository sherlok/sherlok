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
 * The JSON response from Sherlok server, mapped as a Java object.
 * 
 * @author renaud@apache.org
 */
public class SherlokResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("_referenced_fss")
    private Map<Integer, JsonAnnotation> referencedFss;

    @JsonProperty("_views")
    /** k: view-name, v: (k: annot-name, v: list of annot or ids)*/
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

    @JsonIgnore
    public List<JsonAnnotation> get(String type) {
        return getAnnotations().get(type);
    }

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

    @JsonIgnore
    public Map<String, List<JsonAnnotation>> getAnnotations() {
        return getAnnotations("_InitialView");
    }

    @JsonIgnore
    public String getText() {
        return referencedFss.get(1).getProperty("sofaString").toString();
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {// should not happen...
            return getText();
        }
    }
}
