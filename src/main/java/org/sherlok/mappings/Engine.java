package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Bundles group together a set of library dependencies.
 * 
 * @author renaud@apache.org
 *
 */
public class Engine {

    /** A unique name for this engine. Letters, numbers and underscore only */
    private String name,
    /**
     * A unique version id for this engine. Letters, numbers, dots and underscore
     * only
     */
    version,
    /**
     * Useful to group engines together. Letters, numbers, slashes and
     * underscore only
     */
    domain,
    /** (Optional) */
    description;
    /** the Java UIMA class name of this engine */
    @JsonProperty("class")
    String classz;
    /** which {@link Bundle}this engine comes from */
    String bundle;
    /** UIMA parameters. Overwrites default parameters */
    private Map<String, Object> parameters = map();

    // read/write

    static final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public void write(File f) throws JsonGenerationException,
            JsonMappingException, IOException {
        mapper.writeValue(f, this);
    }

    public static Engine load(File f) throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        return mapper.readValue(new FileInputStream(f), Engine.class);
    }

    // get/set

    public String getName() {
        return name;
    }

    public Engine setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Engine setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Engine setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Engine setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getClassz() {
        return classz;
    }

    public Engine setClassz(String classz) {
        this.classz = classz;
        return this;
    }

    public String getBundle() {
        return bundle;
    }

    public Engine setBundle(String bundle) {
        this.bundle = bundle;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Engine setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public Engine addParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
