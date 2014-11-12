package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;
import static org.sherlok.Sherlok.SEPARATOR;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A pipeline describes the steps to perform a text mining analysis. This
 * analysis consists of a list of steps to be performed on text (e.g. split
 * words, remove determinants, annotate locations, ...).
 * 
 * @author renaud@apache.org
 */
public class PipelineDef extends Def<PipelineDef> {

    /** which language this pipeline works for (ISO code, or "all") */
    private String language,
    /**
     * Useful to group pipelines together. Letters, numbers, slashes and
     * underscore only
     */
    domain;
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

    // get/set

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
        super.validate();
        try {
            // TODO more
        } catch (Throwable e) {
            throw new ValidationException(e.getMessage());
        }
        return true;
    }

    /** Creates an id for this pipeline, composed of 'name:version' */
    public static String createId(String pipelineName, String version) {
        return pipelineName + SEPARATOR + (version == null ? "null" : version);
    }

    public static String getName(String pipelineId) {
        return pipelineId.split(SEPARATOR)[0];
    }

    public static String getVersion(String pipelineId) {
        return pipelineId.split(SEPARATOR)[1];
    }

    @JsonIgnore
    public String getId() {
        return name + SEPARATOR + version;
    }

    @Override
    public String toString() {
        return getId();
    }
}
