package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static ch.epfl.bbp.collections.Create.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.fit.factory.AnalysisEngineFactory;

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
public class EngineDef {

    /** A unique name for this engine. Letters, numbers and underscore only */
    private String name,
    /**
     * A unique version id for this engine. Letters, numbers, dots and
     * underscore only
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
    /** which {@link BundleDef}this engine comes from */
    @JsonProperty("bundle_id")
    String bundleId;
    /** UIMA parameters. Overwrites default parameters */
    private Map<String, Object> parameters = map();
    /** Or you can just specify a Ruta script */
    String script;
    // read/write

    static final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public void write(File f) throws JsonGenerationException,
            JsonMappingException, IOException {
        mapper.writeValue(f, this);
    }

    public static EngineDef load(File f) throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        return mapper.readValue(new FileInputStream(f), EngineDef.class);
    }

    // get/set

    public String getName() {
        return name;
    }

    public EngineDef setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public EngineDef setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public EngineDef setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public EngineDef setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getClassz() {
        return classz;
    }

    public EngineDef setClassz(String classz) {
        this.classz = classz;
        return this;
    }

    public String getBundleId() {
        return bundleId;
    }

    public EngineDef setBundleId(String bundleId) {
        this.bundleId = bundleId;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public EngineDef setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public EngineDef addParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    public Object getParameter(String key) {
        return this.parameters.get(key);
    }

    public String getScript() {
        return script;
    }

    public EngineDef setScript(String script) {
        this.script = script;
        return this;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }

    public boolean validate() {
        // FIXME
        return true;
    }

    /** Flatten the params to be used by the {@link AnalysisEngineFactory} */
    public Object[] getFlatParams() {
        List<Object> flatParams = list();
        for (Entry<String, Object> en : getParameters().entrySet()) {
            flatParams.add(en.getKey());
            flatParams.add(en.getValue());
        }
        return flatParams.toArray(new Object[flatParams.size()]);
    }
}
