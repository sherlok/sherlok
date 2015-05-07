/**
 * Copyright (C) 2014-2015 Renaud Richardet
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

import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDotUnderscore;
import static org.sherlok.utils.CheckThat.validateDomain;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Map.Entry;

import org.sherlok.config.ConfigVariable;
import org.sherlok.config.ConfigVariableFactory;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Abstract class for definitions.
 *
 * @author renaud@apache.org
 */
public abstract class Def {
    protected static final Logger LOG = getLogger(Def.class);

    /** separates name from id. */
    public static final String SEPARATOR = ":";

    /** unique name for this bundle. Letters, numbers, dots and '_' only. */
    protected String name,
    /**
     * a unique version id for this bundle. Letters, numbers, dots and
     * underscore only
     */
    version,
    /** (optional) */
    description = "",
    /**
     * Useful to group engine sets together. Letters, numbers, dots, slashes and
     * underscore only. If empty (""), then defaults to no domain, meaning that
     * pipeline is stored on disk at the root of
     * {@link FileBased#PIPELINES_PATH}.
     */
    domain = "";

    /**
     * (internal) configuration variables which support for protocols such as
     * file, git, ...
     */
    @JsonProperty("config")
    protected Map<String, Map<String, String>> rawConfig = map();

    /** configuration variables (based on rawConfigs) */
    @JsonIgnore
    protected Map<String, ConfigVariable> configVariables = null;

    // get/set

    public String getName() {
        return name;
    }

    public Def setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Def setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Def setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Def setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    @JsonIgnore
    public void validate(String msgName) throws ValidationException {
        checkOnlyAlphanumDotUnderscore(name, msgName + " 'name' ");
        checkOnlyAlphanumDotUnderscore(version, msgName + " 'version' ");
        validateDomain(domain, msgName + " 'domain' ");

        // build and validate configuration variables
        configVariables = buildConfigVariables(rawConfig);
    }

    @JsonIgnore
    /** Creates an id for this def, composed of 'name:version' */
    public static String createId(String name, String version) {
        return name + SEPARATOR + (version == null ? "null" : version);
    }

    public static String getName(String id) {
        return id.split(SEPARATOR)[0];
    }

    public static String getVersion(String id) {
        return id.split(SEPARATOR)[1];
    }

    @JsonIgnore
    public String getId() {
        return name + SEPARATOR + version;
    }

    public Map<String, Map<String, String>> getRawConfig() {
        return rawConfig;
    }

    public Def setRawConfig(Map<String, Map<String, String>> configs) {
        this.rawConfig = configs;
        return this;
    }

    public Def addRawConfig(String var, Map<String, String> config) {
        this.rawConfig.put(var, config);
        return this;
    }

    public Map<String, ConfigVariable> getConfigVariables() {
        if (configVariables == null) {
            // This should not happen. It need to be not validated before...
            try {
                configVariables = buildConfigVariables(rawConfig);
            } catch (ValidationException e) {
                LOG.warn("Configuration variables are not valid "
                        + "and were not validated before!", e);
            }
        }
    
        return configVariables;
    }

    /**
     * Build and validate the configuration variable
     */
    private static Map<String, ConfigVariable> buildConfigVariables(
            Map<String, Map<String, String>> rawConfig)
            throws ValidationException {
        Map<String, ConfigVariable> config = map();

        for (Entry<String, Map<String, String>> entry : rawConfig.entrySet()) {
            String name = entry.getKey();
            ConfigVariable val = ConfigVariableFactory.factory(name,
                    entry.getValue());
            config.put(name, val);
        }

        return config;
    }

    @Override
    public String toString() {
        return getId();
    }
}
