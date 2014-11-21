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
package org.sherlok;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_MAIN_SCRIPT;
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_SCRIPT_PATHS;
import static org.apache.uima.ruta.engine.RutaEngine.SCRIPT_FILE_EXTENSION;
import static org.apache.uima.util.FileUtils.saveString2File;
import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FilteringTypeSystem;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.ruta.engine.RutaEngine;
import org.sherlok.RutaHelper.TypeDTO;
import org.sherlok.RutaHelper.TypeFeatureDTO;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * 
 * Manages a UIMA pipeline (configuration, and then use/annotation). Lifecycle:<br>
 * <ol>
 * <li>create Pipeline</li>
 * <li>add engines</li>
 * <li>add output fields</li>
 * <li>initialize</li>
 * <li>annotate texts</li>
 * <li>close</li>
 * </ol>
 * 
 * @author renaud@apache.org
 */
public class UimaPipeline {
    private static Logger LOG = getLogger(UimaPipeline.class);

    private String pipelineId;
    private String language;

    private List<AnalysisEngineDescription> aeds = list();
    private AnalysisEngine[] engines = null;
    /** Filters the JSON output */
    private FilteringTypeSystem filter = new FilteringTypeSystem();
    /** Keeps track of the {@link Type}s added in every Ruta script */
    private TypeSystemDescription tsd;

    private XmiCasSerializer xcs;
    private JCas jCas;

    /**
     * @param pipelineId
     * @param lang
     *            language, important e.g. for DKpro components.
     */
    public UimaPipeline(String pipelineId, String language) {
        this.pipelineId = pipelineId;
        this.language = language;
        try {
            tsd = TypeSystemDescriptionFactory.createTypeSystemDescription();
            // for(TypeDescription t: tsd.getTypes())
            // System.err.println(t);
        } catch (ResourceInitializationException e) {
            throw new RuntimeException(e);// should not happen
        }
    }

    public void add(AnalysisEngineDescription aDesc) {
        if (engines != null)
            throw new IllegalArgumentException(
                    "cannot add more engines after first call to process()");
        aeds.add(aDesc);
    }

    public void addOutputAnnotation(String typeName, String... properties) {
        filter.includeType(typeName, properties);
    }

    public void initialize() throws UIMAException {
        // initialize Engines
        if (engines == null) {
            engines = createEngines(aeds
                    .toArray(new AnalysisEngineDescription[aeds.size()]));
        }
        // initialize JSON writer (incl. filter)
        if (xcs == null) {
            xcs = new XmiCasSerializer(filter);
            xcs.setPrettyPrint(true);
        }

        // initialize CAS
        jCas = JCasFactory.createJCas(tsd);
    }

    private static AnalysisEngine[] createEngines(
            AnalysisEngineDescription... descs) throws UIMAException {
        AnalysisEngine[] engines = new AnalysisEngine[descs.length];
        for (int i = 0; i < engines.length; ++i) {
            if (descs[i].isPrimitive()) {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            } else {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            }
        }
        return engines;
    }

    /**
     * @param text
     *            the text to annotate
     */
    public String annotate(String text) throws UIMAException, SAXException {

        JCas annotated = annotateJCas(text);

        StringWriter sw = new StringWriter();
        xcs.serializeJson(annotated.getCas(), sw);
        return sw.toString();
    }

    /**
     * @param text
     *            the text to annotate
     */
    public JCas annotateJCas(String text) throws UIMAException, SAXException {

        jCas.reset();
        jCas.setDocumentText(text);
        jCas.setDocumentLanguage(language);

        SimplePipeline.runPipeline(jCas, engines);

        return jCas;
    }

    public void close() {
        for (AnalysisEngine engine : engines) {
            engine.destroy();
        }
    }

    @Override
    public String toString() {
        return pipelineId;
    }

    public void addRutaEngine(List<String> scriptLines, String pipelineId)
            throws ResourceInitializationException, IOException,
            ValidationException {

        String script = StringUtils.join(scriptLines, "\n").trim();
        String nameSpace;
        if (!script.startsWith("PACKAGE")) {
            nameSpace = "org.sherlok.ruta";
            script = "PACKAGE " + nameSpace + ";\n\n" + script;
        } else {
            nameSpace = script.substring(0, script.indexOf(";"))
                    .replaceAll("PACKAGE", "").trim();
        }

        for (TypeDTO t : RutaHelper.parseDeclaredTypes(script)) {
            LOG.debug("adding type {}", t);
            TypeDescription typeD = tsd.addType(nameSpace + "." + t.typeName,
                    t.description, t.supertypeName);
            for (TypeFeatureDTO f : t.getTypeFeatures()) {
                typeD.addFeature(f.featureName, f.description, f.rangeTypeName);
            }
        }

        // write Ruta script to file
        // ruta does not like dots
        String scriptName = pipelineId.replace(".", "_");
        File scriptFile = new File(FileBased.RUTA_PIPELINE_CACHE_PATH
                + scriptName + SCRIPT_FILE_EXTENSION);
        scriptFile.getParentFile().mkdirs();
        saveString2File(script, scriptFile);

        aeds.add(createEngineDescription(RutaEngine.class, //
                PARAM_SCRIPT_PATHS, scriptFile.getParent(), //
                RutaEngine.PARAM_RESOURCE_PATHS, FileBased.RUTA_RESOURCES_PATH, //
                PARAM_MAIN_SCRIPT, scriptName));
    }

    public TypeSystemDescription getTypeSystemDescription() {
        return tsd;
    }
}
