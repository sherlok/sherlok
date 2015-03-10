package org.sherlok.mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a (UIMA) annotation produced by Sherlok. This mapped JSON entity
 * can parse a variable amount of unmapped properties, e.g.
 * <code>{"begin" : 0,  "end" : 55,  "language" : "en" } </code> that are later
 * stored in the {@link #properties} field.
 * 
 * @author renaud@apache.org
 */
@JsonPropertyOrder(value = { "begin", "end", "type" }, alphabetic = true)
public class JsonAnnotation {

    /** These keys are not considered properties */
    final public static Set<String> NOT_PROPERTIES = new HashSet<>();
    static {
        NOT_PROPERTIES.add("begin");
        NOT_PROPERTIES.add("end");
        NOT_PROPERTIES.add("sofa");
    }

    private int begin = 0, end = 0;

    // only include if there are properties beyond begin/end
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, Object> properties = new HashMap<>();

    public int getBegin() {
        return begin;
    }

    public JsonAnnotation setBegin(int begin) {
        this.begin = begin;
        return this;
    }

    public int getEnd() {
        return end;
    }

    public JsonAnnotation setEnd(int end) {
        this.end = end;
        return this;
    }

    /** "any getter" needed for serialization */
    @JsonAnyGetter
    public Map<String, Object> any() {
        return properties;
    }

    @JsonAnySetter
    public JsonAnnotation addProperty(String name, Object value)
            throws JSONException {
        if (!NOT_PROPERTIES.contains(name))
            properties.put(name, value);
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonIgnore
    public Object getProperty(String property) {
        return properties.get(property);
    }

    @JsonIgnore
    public JsonAnnotation removeProperty(String p) {
        if (properties.containsKey(p))
            properties.remove(p);
        return this;
    }

    /** Equality on start/end and all properties that are equals()'able */
    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonAnnotation) {
            JsonAnnotation other = (JsonAnnotation) o;
            if (this.begin == other.begin && //
                    this.end == other.end) {

                if (this.getProperties().size() == other.getProperties().size()) {

                    for (Entry<String, Object> p : this.properties.entrySet()) {
                        Object op = other.getProperty(p.getKey());
                        // should work fine for str, int,...
                        if (!op.equals(p.getValue())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[" + begin + ":" + end);
        for (Entry<String, Object> p : properties.entrySet()) {
            sb.append(", " + p.getKey() + "='" + p.getValue() + "'");
        }
        sb.append("]");
        return sb.toString();
    }
}