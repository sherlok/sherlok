package org.sherlok.utils;

import static org.sherlok.utils.Create.map;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sherlok.mappings.PipelineDef.TestAnnotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SherlokResult {

    @JsonProperty("@cas_views")
    private Map<Integer, List<Integer>> views;

    @JsonProperty("@context")
    private Map<String, Object> context;

    @JsonProperty("annotations")
    private Map<Integer, TestAnnotation> annotations;

    private static ObjectMapper MAPPER = new ObjectMapper();

    private SherlokResult() {// hide
    }

    public static SherlokResult parse(String json) throws JsonParseException,
            JsonMappingException, IOException {
        return MAPPER.readValue(json, SherlokResult.class);
    }

    public Map<Integer, TestAnnotation> getAnnotations() {
        return annotations;
    }

    public Map<Integer, TestAnnotation> get(String type) {
        Map<Integer, TestAnnotation> ret = map();
        for (Entry<Integer, TestAnnotation> a : annotations.entrySet()) {
            if (a.getValue().getType().equals(type)) {
                ret.put(a.getKey(), a.getValue());
            }
        }
        return ret;
    }
}
