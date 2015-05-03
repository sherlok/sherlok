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

import static java.lang.Character.isLetterOrDigit;
import static java.util.regex.Pattern.compile;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDotUnderscore;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.maven.model.validation.DefaultModelValidator;
import org.sherlok.Controller;
import org.sherlok.config.ConfigVariable;
import org.sherlok.config.ConfigVariableFactory;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Engine sets group together a set of engines and their library dependencies.
 *
 * @author renaud@apache.org
 */
// ensure property output order
@JsonPropertyOrder(value = { "name", "version", "description", "domain",
        "dependencies", "repositories", "engines", "config" }, alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BundleDef extends Def {

    /** a list of all library dependencies. */
    private List<BundleDependency> dependencies = list();

    /** additional maven repositories. */
    private Map<String, String> repositories = map();

    private List<EngineDef> engines = list();
    
    /**
     * (internal) configuration variables which support for protocols such as
     * file, git, ...
     */
    @JsonProperty("config")
    private Map<String, Map<String, String>> rawConfig = map();

    /** configuration variables (based on rawConfigs) */
    @JsonIgnore
    private Map<String, ConfigVariable> configVariables = null;

    /** A Maven dependency to some external UIMA code. */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class BundleDependency {

        /** Dependencies can only have these formats. */
        public enum DependencyType {
            /** Default value, corresponds to a released maven artifact. */
            mvn, //
            /** any accessible git repository that contains a Maven project. */
            git, // TODO xLATER git protocol not implemented
            /** corresponds to a local or remote jar. */
            jar // TODO xLATER jar protocol not implemented
        }

        private DependencyType type = DependencyType.mvn;
        /** Format: {group id}:{artifact id}:{version} */
        private String value;

        public BundleDependency() {
        }

        public BundleDependency(DependencyType type, String value) {
            this.type = type;
            this.value = value;
        }

        public DependencyType getType() {
            return type;
        }

        public void setType(DependencyType type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @JsonIgnore
        public String getGroupId() {
            return value.split(SEPARATOR)[0];
        }

        @JsonIgnore
        public String getArtifactId() {
            return value.split(SEPARATOR)[1];
        }

        @JsonIgnore
        public String getVersion() {
            return value.split(SEPARATOR)[2];
        }

        @JsonIgnore
        public int hashCode() {
            return value.hashCode();
        }

        @JsonIgnore
        public boolean equals(Object obj) {
            if (obj instanceof BundleDependency) {
                BundleDependency o = (BundleDependency) obj;
                if (o.value.equals(value) && o.type == type) {
                    return true;
                }
            }
            return false;
        }

        @JsonIgnore
        public String toString() {
            return value + " (" + type + ")";
        };

        /** @see {@link DefaultModelValidator} */
        private static final Pattern VALIDATE_ID = compile("[A-Za-z0-9_\\-.]+");

        @JsonIgnore
        public void validate() throws ValidationException {

            validateArgument(isLetterOrDigit(value.charAt(0)),
                    "'value' should start with a letter or a digit, but was '"
                            + value + "'");
            validateArgument(value.split(SEPARATOR).length == 3,
                    "'value' should contain exactly 3 columns (':'), but was '"
                            + value + "'");
            validateArgument(
                    VALIDATE_ID.matcher(getGroupId()).matches(),
                    "invalid group id '" + getGroupId() + "' for bundle "
                            + this + ", allowed characters are "
                            + VALIDATE_ID.toString());
            validateArgument(
                    VALIDATE_ID.matcher(getArtifactId()).matches(),
                    "invalid artifact id '" + getArtifactId() + "' for bundle "
                            + this + ", allowed characters are "
                            + VALIDATE_ID.toString());
            validateArgument(
                    VALIDATE_ID.matcher(getVersion()).matches(),
                    "invalid version id '" + getVersion() + "' for bundle "
                            + this + ", allowed characters are "
                            + VALIDATE_ID.toString());
        }
    }

    @JsonPropertyOrder({ "name", "class", "description", "parameters" })
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class EngineDef {

        /**
         * an optional unique name for this bundle. Letters, numbers and
         * underscore only
         */
        private String name,

        /** (optional) */
        description;

        /** the Java UIMA class name of this engine. */
        @JsonProperty("class")
        private String classz;

        /** UIMA parameters. To overwrite default parameters */
        // TODO serialize without [ ], by creating custom 
        // serializer, see Def.LineSerializer
        private Map<String, List<String>> parameters = map();

        /** TRANSITIVE (JsonIgnore), dynamically set by the bundle. */
        @JsonIgnore
        private BundleDef bundle;

        // get/set

        /** @return the engine name. Falls back on {@link #classz's simple name}. */
        public String getName() {
            if (name != null) {
                return name;
            } else if (classz != null) {
                if (classz.contains(".")) {
                    return classz.substring(classz.lastIndexOf(".") + 1,
                            classz.length());
                } else { // no dot in class name --> just return it
                    return classz;
                }
            }
            return null;
        }

        public EngineDef setName(String name) {
            this.name = name;
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

        public Map<String, List<String>> getParameters() {
            return parameters;
        }

        public EngineDef setParameters(Map<String, List<String>> parameters) {
            this.parameters = parameters;
            return this;
        }

        public EngineDef addParameter(String key, List<String> value) {
            this.parameters.put(key, value);
            return this;
        }

        public List<String> getParameter(String key) {
            return this.parameters.get(key);
        }

        public boolean validate(String msgName) throws ValidationException {
            checkOnlyAlphanumDotUnderscore(name, msgName);
            return true;
        }

        public BundleDef getBundle() {
            return bundle;
        }

        /** Is set at load time by {@link Controller#_load()}. */
        public EngineDef setBundle(final BundleDef bundle) {
            this.bundle = bundle;
            return this;
        }

        /** Needs bundle to be set (see {@link #setBundle()}). */
        @JsonIgnore
        public String getId() {
            return createId(name, bundle.getVersion());
        }

        /** @return the id without non-alphanumeric, separated by separator */
        public String getIdForDescriptor(final String separator) {
            return getName().replaceAll("[^A-Za-z0-9]", "_") + separator
                    + bundle.getVersion().replaceAll("[^A-Za-z0-9]", "_");
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public List<BundleDependency> getDependencies() {
        return dependencies;
    }

    public BundleDef setDependencies(List<BundleDependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BundleDef addDependency(BundleDependency dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    public Map<String, String> getRepositories() {
        return repositories;
    }

    public BundleDef setRepositories(Map<String, String> repositories) {
        this.repositories = repositories;
        return this;
    }

    public BundleDef addRepository(String id, String url) {
        this.repositories.put(id, url);
        return this;
    }

    public List<EngineDef> getEngines() {
        return engines;
    }

    public BundleDef setEngines(List<EngineDef> engines) {
        this.engines = engines;
        return this;
    }

    public BundleDef addEngine(EngineDef engine) {
        this.engines.add(engine);
        return this;
    }

    public Map<String, Map<String, String>> getRawConfig() {
        return rawConfig;
    }

    public BundleDef setRawConfig(Map<String, Map<String, String>> configs) {
        this.rawConfig = configs;
        return this;
    }

    public BundleDef addRawConfig(String var, Map<String, String> config) {
        this.rawConfig.put(var, config);
        return this;
    }

    @Override
    public void validate(String bundleObject) throws ValidationException {
        super.validate(bundleObject);
        try {
            for (BundleDependency bd : getDependencies()) {
                bd.validate();
            }
            for (EngineDef e : getEngines()) {
                e.validate(bundleObject);
            }
            // build and validate configuration variables
            configVariables = buildConfigVariables(rawConfig);

            // TODO validate usage of variables in engines and make sure no
            // unknown variable are used
        } catch (Throwable e) {
            throw new ValidationException("invalid bundle '" + bundleObject
                    + "'", e.getMessage());
        }
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
}
