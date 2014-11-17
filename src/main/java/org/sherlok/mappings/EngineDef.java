package org.sherlok.mappings;

import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bundles group together a set of library dependencies.
 * 
 * @author renaud@apache.org
 *
 */
public class EngineDef extends Def {

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
    private List<String> script;

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

    public List<String> getScript() {
        return script;
    }

    public EngineDef setScript(List<String> script) {
        this.script = script;
        return this;
    }

    public boolean validate(String engineObject) throws ValidationException {
        super.validate(engineObject);
        try {
            // TODO more validation
        } catch (Throwable e) {
            throw new ValidationException("" + engineObject + ": "
                    + e.getMessage());
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

    /** @return whether this is a Ruta engine; false if it is a UIMAfit engine */
    @JsonIgnore
    public boolean isRuta() {
        return getScript() != null && getScript().size() > 0;
    }
}
