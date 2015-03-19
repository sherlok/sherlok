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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static org.sherlok.utils.CheckThat.validateDomain;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateTypeIdentifier;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.ValidationException.ERR;
import static org.sherlok.utils.ValidationException.ERR_NOTFOUND;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sherlok.Controller;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.CheckThat;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
@JsonInclude(NON_DEFAULT)
public class PipelineDef extends Def {

    /** Which language this pipeline works for (ISO code). Defaults to 'en' */
    private String language = "en";

    /** The list of engine definitions */
    @JsonProperty("script")
    @JsonSerialize(using = ListSerializer.class)
    private List<String> scriptLines = list();
    /** Controls the output of this pipeline */
    private PipelineOutput output = new PipelineOutput();

    /** Embedded (integration) tests */
    @JsonInclude(ALWAYS)
    private List<PipelineTest> tests = list();

    /** Whether to run this pipeline when it is loaded. Defaults true */
    private boolean warmup = true;

    /** Defines what kind of annotation this pipeline outputs */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class PipelineOutput {

        @JsonProperty("filter_annotations")
        private List<String> annotationFilters = list();
        @JsonProperty("include_annotations")
        private List<String> annotationIncludes = list();

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
    }

    /**
     * Tests one single input string against a list of expected
     * {@link JsonAnnotation}s
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_DEFAULT)
    @JsonPropertyOrder(value = { "input", "expected", "comparison" }, alphabetic = true)
    public static class PipelineTest {

        public enum Comparison {
            /** all expected {@link JsonAnnotation}s are present in system */
            atLeast,
            /** expected and system {@link JsonAnnotation}s are exactly equals */
            exact;
        }

        private String input;
        private Map<String, List<JsonAnnotation>> expected = map();
        private Comparison comparison = Comparison.atLeast; // default

        public String getInput() {
            return input;
        }

        public PipelineTest setInput(String input) {
            this.input = input;
            return this;
        }

        public Map<String, List<JsonAnnotation>> getExpected() {
            return expected;
        }

        public PipelineTest setExpected(
                Map<String, List<JsonAnnotation>> expected) {
            this.expected = expected;
            return this;
        }

        public Comparison getComparison() {
            return comparison;
        }

        public void setComparison(Comparison comparison) {
            this.comparison = comparison;
        }

        @Override
        public String toString() {
            return input + "::" + expected;
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

    public List<PipelineTest> getTests() {
        return tests;
    }

    public PipelineDef setTests(List<PipelineTest> tests) {
        this.tests = tests;
        return this;
    }

    public PipelineDef addTest(PipelineTest test) {
        this.tests.add(test);
        return this;
    }

    public boolean isWarmup() {
        return warmup;
    }

    public void setWarmup(boolean warmup) {
        this.warmup = warmup;
    }

    // Utilities //////////////////////////////////////////////////////////////

    @Override
    public void validate(String errorMsg) throws ValidationException {
        super.validate(errorMsg);
        try {
            validateArgument(language != null, "'language' can not be null");
            validateArgument(language.length() > 0,
                    "'language' can not be empty");
            validateDomain(domain, errorMsg);

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
            throw new ValidationException("" + errorMsg + ": " + e.getMessage());
        }
    }

    /**
     * Ensures that this pipeline only uses engines defined in
     * {@link Controller}'s engines
     * 
     * @param engineIds
     *            the engine ids of the controller
     */
    public void validateEngines(Set<String> engineIds)
            throws ValidationException {
        for (String engineFromScript : getEnginesFromScript()) {
            if (!engineIds.contains(engineFromScript)) {
                throw new ValidationException(map(ERR,
                        "engine declared in pipeline '" + getId()
                                + "' but not found", ERR_NOTFOUND,
                        engineFromScript));
            }
        }
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
            if (scriptLine.startsWith("ENGINE")) {
                String pengineId = scriptLine.trim()
                        .substring("ENGINE".length()).trim()
                        .replaceAll(";", "");
                engineIds.add(pengineId);
            }
        }
        return engineIds;
    }

    /** Adds newlines between entries */
    @SuppressWarnings("rawtypes")
    public static class ListSerializer extends JsonSerializer<List> {

        @Override
        public boolean isEmpty(final List value) {
            return value.isEmpty();
        }

        @Override
        public Class<List> handledType() {
            return List.class;
        }

        @Override
        public void serialize(final List value, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeStartArray();
            for (Object item : value) {
                jgen.writeRaw('\n');
                jgen.writeObject(item);
            }
            jgen.writeRaw('\n');
            jgen.writeEndArray();
        }
    }

}
