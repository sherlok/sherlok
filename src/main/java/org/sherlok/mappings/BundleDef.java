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
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.model.validation.DefaultModelValidator;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bundles group together a set of library dependencies.
 * 
 * @author renaud@apache.org
 */
public class BundleDef extends Def {

    /** a list of all the dependencies of this bundle */
    private List<BundleDependency> dependencies = list();
    /** additional maven repositories */
    private Map<String, String> repositories = map();

    /** A Maven dependency to some external UIMA code. */
    public static class BundleDependency {

        /** Dependencies can only have these formats */
        public enum DependencyType {
            /** corresponds to a released maven artifact */
            mvn, //
            /** any accessible git repository that contains a Maven project */
            git, // TODO git protocol not implemented
            /** corresponds to a local or remote jar */
            jar // TODO jar protocol not implemented
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

    // get/set

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

    public boolean validate(String bundleObject) throws ValidationException {
        super.validate(bundleObject);
        try {
            for (BundleDependency bd : getDependencies()) {
                bd.validate();
            }
        } catch (Throwable e) {
            throw new ValidationException("" + bundleObject + ": "
                    + e.getMessage());
        }
        return true;
    }
}
