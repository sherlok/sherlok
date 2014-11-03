package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static ch.epfl.bbp.collections.Create.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
public class Bundle {

    /** a unique name for this bundle. Letters, numbers and underscore only */
    private String name,
    /**
     * a unique version id for this bundle. Letters, numbers, dots and
     * underscore only
     */
    version,
    /** (optional) */
    description;
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
    }

    // read/write

    static final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public void write(File f) throws JsonGenerationException,
            JsonMappingException, IOException {
        mapper.writeValue(f, this);
    }

    public static Bundle load(File f) throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        return mapper.readValue(new FileInputStream(f), Bundle.class);
    }

    // get/set

    public String getName() {
        return name;
    }

    public Bundle setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Bundle setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Bundle setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<BundleDependency> getDependencies() {
        return dependencies;
    }

    public Bundle setDependencies(List<BundleDependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Bundle addDependency(BundleDependency dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    public Map<String, String> getRepositories() {
        return repositories;
    }

    public Bundle setRepositories(Map<String, String> repositories) {
        this.repositories = repositories;
        return this;
    }

    public Bundle addRepository(String id, String url) {
        this.repositories.put(id, url);
        return this;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
