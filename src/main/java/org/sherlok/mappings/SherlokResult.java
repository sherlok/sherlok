package org.sherlok.mappings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JSON response from Sherlok server, mapped as a Java object.
 * 
 * @author renaud@apache.org
 */
public class SherlokResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("@cas_views")
    private Map<Integer, List<Integer>> views;

    @JsonProperty("@context")
    private Map<String, Object> context;

    @JsonProperty("annotations")
    private Map<Integer, Annotation> annotations;

    @JsonProperty("stats")
    private Map<String, Object> stats;

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
    public List<Annotation> get(String type) {
        List<Annotation> ret = new ArrayList<>();
        for (Entry<Integer, Annotation> a : annotations.entrySet()) {
            if (a.getValue().getType().equals(type)) {
                ret.add(a.getValue());
            }
        }
        return ret;
    }

    public Map<Integer, Annotation> getAnnotations() {
        return annotations;
    }
}
