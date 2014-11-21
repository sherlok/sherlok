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

import static com.google.common.base.Joiner.on;
import static org.sherlok.utils.CheckThat.checkArgument;
import static org.sherlok.utils.CheckThat.checkValidId;
import static org.sherlok.utils.Create.list;

import java.util.List;

import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A pipeline describes the steps to perform a text mining analysis. This
 * analysis consists of a list of steps to be performed on text (e.g. split
 * words, remove determinants, annotate locations, ...).
 * 
 * @author renaud@apache.org
 */
public class PipelineDef extends Def {

    /** which language this pipeline works for (ISO code) */
    private String language = "en",
    /**
     * Useful to group pipelines together. Letters, numbers, slashes and
     * underscore only
     */
    domain = "";
    /**
     * Whether this pipeline should be loaded on server startup. Defaults to
     * false
     */
    boolean loadOnStartup = false;

    /** a list of engine definitions */
    private List<PipelineEngine> engines = list();
    /** Controls the output of this pipeline */
    private PipelineOutput output = new PipelineOutput();

    /** tests */
    private List<PipelineTest> tests = list();

    /** An engine definition */
    public static class PipelineEngine {
        private String id;
        private List<String> script;

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

        public List<String> getScript() {
            return script;
        }

        public void setScript(List<String> script) {
            this.script = script;
        }

        /**
         * @return whether this is a Ruta engine; false if it is a UIMAfit
         *         engine
         */
        @JsonIgnore
        public boolean isRuta() {
            return getScript() != null && getScript().size() > 0;
        }

        @Override
        public String toString() {
            if (id != null)
                return id;
            else
                return on(",").join(script);
        }

        public void validate() throws ValidationException {
            checkArgument(id != null || script != null,
                    "Either id or script should be provided for engine '"
                            + this + "'.");
            if (id != null) {
                checkValidId(id);
            }
        }
    }

    /** Output definition: annots and payload */
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

    /** An engine definition */
    public static class PipelineTest {
        private String in, out;

        public String getIn() {
            return in;
        }

        public PipelineTest setIn(String in) {
            this.in = in;
            return this;
        }

        public String getOut() {
            return out;
        }

        public PipelineTest setOut(String out) {
            this.out = out;
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

    public List<PipelineTest> getTests() {
        return tests;
    }

    public PipelineDef setTests(List<PipelineTest> tests) {
        this.tests = tests;
        return this;
    }

    public void addTests(PipelineTest test) {
        this.tests.add(test);
    }

    // TODO ignore in json or not?
    // ** @return convenience info about how to use this pipeline for
    // annotating */
    // public String getUsage() {
    // return "/annotate/" + name + "?version=" + version
    // + "&text=the text to annotate";
    // }

    // utils

    public boolean validate(String pipelineObject) throws ValidationException {
        super.validate(pipelineObject);
        try {
            checkArgument(domain.indexOf("..") == -1,
                    "'domain' can not contain double dots: '" + domain + "'");
            for (PipelineEngine engine : engines) {
                engine.validate();
            }
            // TODO more validation
        } catch (Throwable e) {
            throw new ValidationException("" + pipelineObject + ": "
                    + e.getMessage());
        }
        return true;
    }
}
