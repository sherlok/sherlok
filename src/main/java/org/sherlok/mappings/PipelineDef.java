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
import static org.sherlok.utils.CheckThat.validateTypeIdentifier;
import static org.sherlok.utils.Create.list;

import java.util.List;

import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A pipeline describes the steps to perform a text mining analysis. This
 * analysis consists of a script that defines a list of steps to be performed on
 * text (e.g. split words, remove determinants, annotate locations, ...).
 * 
 * @author renaud@apache.org
 */
// ensure property output order
@JsonPropertyOrder(value = { "name", "version", "description", "language",
        "domain", "loadOnStartup", "scriptLines", "output", "tests" }, alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PipelineDef extends Def {

    /** Which language this pipeline works for (ISO code). Defaults to 'en' */
    private String language = "en";

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

        @JsonProperty("filter_annotations")
        List<String> annotationFilters = list();
        @JsonProperty("include_annotations")
        List<String> annotationIncludes = list();
        List<String> payloads = list();

        public List<String> getAnnotationFilters() {
            return annotationFilters;
        }

        public PipelineOutput setAnnotationFilters(
                List<String> annotationFilters) {
            this.annotationFilters = annotationFilters;
            return this;
        }

        public List<String> getAnnotationIncludes() {
            return annotationIncludes;
        }

        public PipelineOutput setAnnotationIncludes(
                List<String> annotationIncludes) {
            this.annotationIncludes = annotationIncludes;
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

            // output
            validateArgument(
                    !(!output.annotationFilters.isEmpty() && !output.annotationIncludes
                            .isEmpty()),
                    "either provide 'filter_annotations' or 'include_annotations', but not both for pipeline '"
                            + getId() + "'");

            String typeMsg = "It should be a list of valid identifiers, separated by dots, "
                    + "and optionally ending with '.*' to match all subsequent types. "
                    + "Identifiers must be composed of letters, numbers, the underscore"
                    + " _ and the dollar sign $. Identifiers may only begin with a letter, "
                    + "the underscore or a dollar sign.";
            for (String type : output.annotationFilters) {
                validateTypeIdentifier(type, "output annotation filter '"
                        + type + "' is not valid." + typeMsg);
            }
            for (String type : output.annotationIncludes) {
                validateTypeIdentifier(type, "output annotation includes '"
                        + type + "' is not valid." + typeMsg);
            }
            if (tests.isEmpty()) {
                LOG.warn("no tests for pipeline '{}'", getId());
            }

        } catch (Throwable e) {
            throw new ValidationException("" + pipelineObject + ": "
                    + e.getMessage());
        }
        return true;
    }

    /**
     * Extract all {@link EngineDef}s from theses script lines, by scanning for
     * lines starting with "ENGINE"
     */
    @JsonIgnore
    public List<String> getEnginesFromScript() {
        return getEnginesFromScript(getScriptLines());
    }

    /**
     * Extract all {@link EngineDef}s from theses script lines, by scanning for
     * lines starting with "ENGINE"
     */
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
