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

import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;

import java.util.List;

import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A pipeline describes the steps to perform a text mining analysis. This
 * analysis consists of a script that defines a list of steps to be performed on
 * text (e.g. split words, remove determinants, annotate locations, ...).
 * 
 * @author renaud@apache.org
 */
public class PipelineDef extends Def {

    /** Which language this pipeline works for (ISO code). Defaults to 'en' */
    private String language = "en",
    /**
     * Useful to group pipelines together. Letters, numbers, slashes and
     * underscore only. Defaults to empty.
     */
    domain = "";
    /**
     * Whether this pipeline should be loaded on server startup. Defaults to
     * false
     */
    boolean loadOnStartup = false;

    /** The list of engine definitions */
    @JsonProperty("script")
    private List<String> scriptLines = list();
    /** Controls the output of this pipeline */
    private PipelineOutput output = new PipelineOutput();

    /** Embedded (integration) tests */
    private List<PipelineTest> tests = list();

    /** Output definition (output annotations and payload) */
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

        @Override
        public String toString() {
            return in + "::" + out;
        }
    }

    // Get/Set ////////////////////////////////////////////////////////////////

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

    public List<String> getScriptLines() {
        return scriptLines;
    }

    public PipelineDef setScriptLines(List<String> scriptLines) {
        this.scriptLines = scriptLines;
        return this;
    }

    public PipelineDef addScriptLine(String scriptLine) {
        this.scriptLines.add(scriptLine);
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

    // Utilities //////////////////////////////////////////////////////////////

    public boolean validate(String pipelineObject) throws ValidationException {
        super.validate(pipelineObject);
        try {
            validateArgument(language != null, "'language' can not be null");
            validateArgument(language.length() > 0,
                    "'language' can not be empty");
            validateArgument(domain.indexOf("..") == -1,
                    "'domain' can not contain double dots: '" + domain + "'");
            if (tests.isEmpty()) {
                LOG.warn("no tests for pipeline '{}'", getId());
            }
            if (output.getAnnotations().isEmpty()) {
                LOG.warn("no output annotations for pipeline '{}'", getId());
            }
        } catch (Throwable e) {
            throw new ValidationException("" + pipelineObject + ": "
                    + e.getMessage());
        }
        return true;
    }

    @JsonIgnore
    public List<String> getEnginesFromScript() {
        return getEnginesFromScript(getScriptLines());
    }

    public static List<String> getEnginesFromScript(List<String> scriptLines) {
        List<String> engineIds = list();
        for (String scriptLine : scriptLines) {
            if (scriptLine.startsWith("ENGINE ")) {
                String pengineId = scriptLine.trim()
                        .substring("ENGINE ".length()).replaceAll(";", "");
                engineIds.add(pengineId);
            }
        }
        return engineIds;
    }
}
