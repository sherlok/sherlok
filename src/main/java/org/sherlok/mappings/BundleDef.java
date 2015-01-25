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

import static java.util.regex.Pattern.compile;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDot;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.maven.model.validation.DefaultModelValidator;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Engine sets group together a set of engines and their library dependencies.
 * 
 * @author renaud@apache.org
 */
// ensure property output order
@JsonPropertyOrder(value = { "name", "version", "description", "domain",
        "dependencies", "repositories", "engines" }, alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BundleDef extends Def {

    /** a list of all library dependencies */
    private List<BundleDependency> dependencies = list();

    /** additional maven repositories */
    private Map<String, String> repositories = map();

    private List<EngineDef> engines = list();

    /** A Maven dependency to some external UIMA code. */
    public static class BundleDependency {

        /** Dependencies can only have these formats */
        public enum DependencyType {
            /** Default value, corresponds to a released maven artifact */
            mvn, //
            /** any accessible git repository that contains a Maven project */
            git, // TODO xLATER git protocol not implemented
            /** corresponds to a local or remote jar */
            jar // TODO xLATER jar protocol not implemented
        }

        private DependencyType type = DependencyType.mvn;
        /** Format: {group id}:{artifact id}:{version} */
        private String value;

        public BundleDependency() {
        }

        /**
         * A maven dependency
         * 
         * @param value
         *            artifact specification with format
         *            <code>{group id}:{artifact
         *            id}:{version}</code>
         */
        public BundleDependency(String value) {
            this(DependencyType.mvn, value);
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
            if (obj instanceof BundleDependency
                    && ((BundleDependency) obj).value.equals(value)) {
                return true;
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

        /** the Java UIMA class name of this engine */
        @JsonProperty("class")
        private String classz;

        /** UIMA parameters. To overwrite default parameters */
        private Map<String, List<String>> parameters = map();

        /** TRANSITIVE (JsonIgnore), dynamically set by the bundle */
        @JsonIgnore
        private BundleDef bundle;

        // get/set

        public String getName() {
            return name; // FIXME fallback
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

        // @JsonSerialize(using=ListConverter.class)
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
            checkOnlyAlphanumDot(name, msgName);
            return true;
        }

        public BundleDef getBundle() {
            return bundle;
        }

        public EngineDef setBundle(BundleDef bundle) {
            this.bundle = bundle;
            return this;
        }

        /** needs bundle */
        @JsonIgnore
        public String getId() {
            return createId(name, bundle.getVersion());
        }

        public String getIdForDescriptor(String separator) {
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

    public boolean validate(String bundleObject) throws ValidationException {
        super.validate(bundleObject);
        try {
            // TODO validateArgument(checkOnlyAlphanumDot(domain),
            // "domain not valid: '"+domain+"'");
            for (BundleDependency bd : getDependencies()) {
                bd.validate();
            }
            // TODO validate engines
        } catch (Throwable e) {
            throw new ValidationException("" + bundleObject + ": "
                    + e.getMessage());
        }
        return true;
    }

    // LATER
    public static class ListConverter extends
            JsonSerializer<Map<String, List<String>>> {

        @Override
        public void serialize(Map<String, List<String>> params,
                JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {

            jgen.writeStartObject();

            for (Entry<String, List<String>> en : params.entrySet()) {

                if (en.getValue().size() > 1) {
                    jgen.writeArrayFieldStart(en.getKey());
                    for (String val : en.getValue()) {
                        jgen.writeString(val);
                    }
                    jgen.writeEndArray();
                } else {
                    jgen.writeStringField(en.getKey(), en.getValue().get(0));
                }
            }
            jgen.writeEndObject();
        }
    }

}
