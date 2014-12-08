/**
 * Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
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

    public String getIdForDescriptor(String separator) {
        return getName().replaceAll("[^A-Za-z0-9]", "_") + separator
                + getVersion().replaceAll("[^A-Za-z0-9]", "_");
    }
}
