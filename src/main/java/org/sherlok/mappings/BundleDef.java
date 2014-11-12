package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static ch.epfl.bbp.collections.Create.map;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sherlok.CheckThat;
import org.sherlok.Sherlok;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 */
public class BundleDef extends Def<BundleDef> {

    /** a list of all the dependencies of this bundle */
    private List<BundleDependency> dependencies = list();
    /** additional maven repositories */
    private Map<String, String> repositories = map();

    /** A dependency to some external UIMA code. */
    public static class BundleDependency {
        /** Dependencies can only have these formats */
        enum DependencyType {
            /** corresponds to a released maven artifact */
            mvn, //
            /** any accessible git repository that contains a Maven project */
            git, // TODO
            /** corresponds to a local or remote jar */
            jar // TODO
        }

        private DependencyType type;
        String value;

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
            return value.split(Sherlok.SEPARATOR)[0];
        }

        @JsonIgnore
        public String getArtifactId() {
            return value.split(Sherlok.SEPARATOR)[1];
        }

        @JsonIgnore
        public String getVersion() {
            return value.split(Sherlok.SEPARATOR)[2];
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

    public boolean validate() {
        super.validate();
        try {
            // TODO more
        } catch (Throwable e) {
            throw new ValidationException(e.getMessage());
        }
        return true;
    }
}
