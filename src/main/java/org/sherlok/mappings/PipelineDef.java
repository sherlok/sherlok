package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static org.sherlok.Sherlok.SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A pipeline describes the steps to perform a text mining analysis. This
 * analysis consists of a list of steps to be performed on text (e.g. split
 * words, remove determinants, annotate locations, ...).
 * 
 * @author renaud@apache.org
 */
public class PipelineDef {

    /** A unique name for this pipeline. Letters, numbers and underscore only */
    private String name,
    /**
     * A unique version id for this pipeline. Letters, numbers, dots and
     * underscore only
     */
    version,
    /** which language this pipeline works for (ISO code, or "all") */
    language,
    /**
     * Useful to group pipelines together. Letters, numbers, slashes and
     * underscore only
     */
    domain,
    /** (Optional) */
    description;
    /**
     * Whether this pipeline should be loaded on server startup. Defaults to
     * false
     */
    boolean loadOnStartup = false;

    /** a list of engine definitions */
    private List<PipelineEngine> engines = list();
    /** Controls the output of this pipeline */
    private PipelineOutput output = new PipelineOutput();

    /** An engine definition */
    public static class PipelineEngine {
        private String id, script;

        public PipelineEngine() {
        }

        public PipelineEngine(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        @Override
        public String toString() {
            return id;// TODO
        }
    }

    /** An engine definition */
    public static class PipelineOutput {
        List<String> annotations = list();
        List<String> payloads = list();

        public List<String> getAnnotations() {
            return annotations;
        }

        public PipelineOutput setAnnotations(List<String> annotations) {
            this.annotations = annotations;
            return this;
        }

        public List<String> getPayloads() {
            return payloads;
        }

        public PipelineOutput setPayloads(List<String> payloads) {
            this.payloads = payloads;
            return this;
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

    public static PipelineDef load(File f) throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        return mapper.readValue(new FileInputStream(f), PipelineDef.class);
    }

    // get/set

    public String getName() {
        return name;
    }

    public PipelineDef setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PipelineDef setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public PipelineDef setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public PipelineDef setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PipelineDef setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    public PipelineDef setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
        return this;
    }

    public List<PipelineEngine> getEngines() {
        return engines;
    }

    public PipelineDef setEngines(List<PipelineEngine> engines) {
        this.engines = engines;
        return this;
    }

    public PipelineDef addEngine(PipelineEngine engine) {
        this.engines.add(engine);
        return this;
    }

    public PipelineOutput getOutput() {
        return output;
    }

    public PipelineDef setOutput(PipelineOutput output) {
        this.output = output;
        return this;
    }

    public PipelineDef addOutputAnnotation(String annotation) {
        this.output.annotations.add(annotation);
        return this;
    }

    public PipelineDef addOutputPayload(String payload) {
        this.output.payloads.add(payload);
        return this;
    }

    // utils

    public boolean validate() {
        // FIXME
        return true;
    }

    public static String createId(String pipelineName, String version) {
        return pipelineName + SEPARATOR + version;
    }

    public String createId() {
        return name + SEPARATOR + version;
    }

    @Override
    public String toString() {
        return createId();
    }
}
