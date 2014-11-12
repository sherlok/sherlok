package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static ch.epfl.bbp.collections.Create.map;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.fit.factory.AnalysisEngineFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bundles group together a set of library dependencies.
 * 
 * @author renaud@apache.org
 *
 */
public class EngineDef extends Def<EngineDef> {

    /**
     * Useful to group engines together. Letters, numbers, slashes and
     * underscore only
     */
    private String domain;
    /** the Java UIMA class name of this engine */
    @JsonProperty("class")
    private String classz;
    /** which {@link BundleDef}this engine comes from */
    @JsonProperty("bundle_id")
    private String bundleId;
    /** UIMA parameters. Overwrites default parameters */
    private Map<String, Object> parameters = map();
    /** Or you can just specify a Ruta script */
    String script;

    // get/set

    public String getDomain() {
        return domain;
    }

    public EngineDef setDomain(String domain) {
        this.domain = domain;
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

    public boolean validate() {
        super.validate();
        try {
            // TODO more
        } catch (Throwable e) {
            throw new ValidationException(e.getMessage());
        }
        return true;
    }

    /** Flatten the params to be used by the {@link AnalysisEngineFactory} */
    @JsonIgnore
    public Object[] getFlatParams() {
        List<Object> flatParams = list();
        for (Entry<String, Object> en : getParameters().entrySet()) {
            flatParams.add(en.getKey());
            flatParams.add(en.getValue());
        }
        return flatParams.toArray(new Object[flatParams.size()]);
    }
}
