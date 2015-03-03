package org.sherlok.mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a (UIMA) annotation produced by Sherlok server
 * 
 * @author renaud@apache.org
 */
@JsonPropertyOrder(value = { "begin", "end", "type" }, alphabetic = true)
public class Annotation {

    /** These keys are not considered properties */
    final public static Set<String> NOT_PROPERTIES = new HashSet<>();
    static {
        NOT_PROPERTIES.add("begin");
        NOT_PROPERTIES.add("end");
        NOT_PROPERTIES.add("@type");
        NOT_PROPERTIES.add("sofa");
    }

    private int begin = 0, end = 0;
    @JsonProperty("@type")
    private String type;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, Object> properties = new HashMap<>();

    // "any getter" needed for serialization
    @JsonAnyGetter
    public Map<String, Object> any() {
        return properties;
    }

    @JsonAnySetter
    public Annotation addProperty(String name, Object value)
            throws JSONException {
        if (!NOT_PROPERTIES.contains(name))
            properties.put(name, value);
        return this;
    }

    public int getBegin() {
        return begin;
    }

    public Annotation setBegin(int begin) {
        this.begin = begin;
        return this;
    }

    public int getEnd() {
        return end;
    }

    public Annotation setEnd(int end) {
        this.end = end;
        return this;
    }

    public String getType() {
        return type;
    }

    public Annotation setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) { // FIXME test on properties, too
        if (o instanceof Annotation) {
            Annotation other = (Annotation) o;
            if (this.begin == other.begin && //
                    this.end == other.end && //
                    this.type.equals(other.type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type + "[" + begin + ":" + end);
        for (Entry<String, Object> p : properties.entrySet()) {
            sb.append(", " + p.getKey() + "='" + p.getValue() + "'");
        }
        sb.append("]");
        return sb.toString();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}