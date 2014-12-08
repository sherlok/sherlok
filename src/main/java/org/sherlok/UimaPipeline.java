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
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_DESCRIPTOR_PATHS;
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_MAIN_SCRIPT;
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_RESOURCE_PATHS;
import static org.apache.uima.ruta.engine.RutaEngine.PARAM_SCRIPT_PATHS;
import static org.apache.uima.ruta.engine.RutaEngine.SCRIPT_FILE_EXTENSION;
import static org.apache.uima.util.FileUtils.saveString2File;
import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FilteringTypeSystem;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.ruta.engine.RutaEngine;
import org.apache.uima.util.CasPool;
import org.sherlok.RutaHelper.TypeDTO;
import org.sherlok.RutaHelper.TypeFeatureDTO;
import org.sherlok.mappings.EngineDef;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * 
 * Manages a UIMA pipeline (configuration, and then use/annotation). Lifecycle:<br>
 * <ol>
 * <li>add deps on classpath (to have TypeSystems scannable)</li>
 * <li>create UimaPipeline with script and engines</li>
 * <li>add output fields</li>
 * <li>initialize</li>
 * <li>annotate texts...</li>
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

    private CasPool casPool;

    private List<String> scriptLines;
    private List<EngineDef> engineDefs;

    /**
     * @param pipelineId
     * @param engineDefs
     * @param list
     * @param engineDefs
     * @param lang
     *            language, important e.g. for DKpro components.
     */
    public UimaPipeline(String pipelineId, String language,
            List<EngineDef> engineDefs, List<String> scriptLines)
            throws ResourceInitializationException, IOException,
            ValidationException {
        this.pipelineId = pipelineId;
        this.language = language;
        this.engineDefs = engineDefs;
        this.scriptLines = list(scriptLines); // a copy

        try {
            // needed since we might have added new jars to the classpath
            TypeSystemDescriptionFactory.forceTypeDescriptorsScan();
            tsd = TypeSystemDescriptionFactory.createTypeSystemDescription();
        } catch (ResourceInitializationException e) {
            throw new RuntimeException(e);// should not happen
        }
        initScript();
    }

    public void addOutputAnnotation(String typeName, String... properties) {
        filter.includeType(typeName, properties);
    }

    /** Initializes Engines and CAS */
    public void initialize() throws UIMAException {
        // initialize Engines
        if (engines == null) { // not initialized yet
            engines = createEngines(aeds
                    .toArray(new AnalysisEngineDescription[aeds.size()]));
        }
        // initialize JSON writer (incl. filter)
        if (xcs == null) {
            xcs = new XmiCasSerializer(filter);
            xcs.setPrettyPrint(true);
        }

        // for (TypeDescription td : tsd.getTypes())
        // LOG.debug("type: {}", td);

        AnalysisEngine noOpEngine = AnalysisEngineFactory.createEngine(
                NoOpAnnotator.class, tsd);
        casPool = new CasPool(10, noOpEngine);
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

        CAS cas = null;
        try {
            cas = casPool.getCas(0);
            // cas.reset() is done by casPool

            cas.setDocumentText(text);
            cas.setDocumentLanguage(language);

            SimplePipeline.runPipeline(cas, engines);

            StringWriter sw = new StringWriter();
            xcs.serializeJson(cas, sw);
            return sw.toString();

        } catch (Exception e) {
            throw e;
        } finally {
            casPool.releaseCas(cas);
        }
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

    @SuppressWarnings("unchecked")
    private void initScript() throws ResourceInitializationException,
            IOException, ValidationException {

        // load engines
        List<String> engineDescriptions = list();
        for (int i = 0; i < scriptLines.size(); i++) {

            String scriptLine = scriptLines.get(i);

            if (scriptLine.startsWith("ENGINE ")) {
                // TODO refactor and abstract
                String pengineId = scriptLine.trim()
                        .substring("ENGINE ".length()).replaceAll(";", "");

                boolean found = false;
                for (EngineDef engineDef : engineDefs) {
                    if (engineDef.getId().equals(pengineId)) {

                        found = true;
                        String engineDescription = engineDef
                                .getIdForDescriptor("___");

                        // instantiate class
                        Class<? extends AnalysisComponent> classz;
                        try {
                            classz = (Class<? extends AnalysisComponent>) Class
                                    .forName(engineDef.getClassz());
                        } catch (ClassNotFoundException e) {
                            throw new ValidationException(
                                    "could not find class "
                                            + engineDef.getClassz(), e);
                        }
                        // create ae and write xml descriptor
                        AnalysisEngineDescription aed = AnalysisEngineFactory
                                .createEngineDescription(classz,
                                        engineDef.getFlatParams());
                        try {
                            File tmpEngine = new File(
                                    FileBased.RUTA_ENGINE_CACHE_PATH
                                            + engineDef
                                                    .getIdForDescriptor("___")
                                            + ".xml");
                            tmpEngine.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(
                                    tmpEngine);
                            aed.toXML(fos);
                        } catch (SAXException e) {
                            throw new RuntimeException(
                                    "could not write descriptor of "
                                            + pengineId, e); // should not
                                                             // happen
                        }
                        engineDescriptions.add(engineDescription);
                        scriptLines.set(i, "Document{-> EXEC("
                                + engineDescription + ")}; // " + scriptLine);
                    }
                }
                if (!found) {
                    throw new ValidationException(pengineId + "not found");
                }
            }
        }

        // ensure PACKAGE is present in script
        String script = StringUtils.join(scriptLines, "\n").trim();
        String nameSpace;
        if (!script.startsWith("PACKAGE")) {
            nameSpace = "org.sherlok.ruta";
            script = "PACKAGE " + nameSpace + ";\n" + script;
        } else {
            nameSpace = script.substring(0, script.indexOf(";"))
                    .replaceFirst("PACKAGE", "").trim();
        }

        // add engines to script
        if (!engineDescriptions.isEmpty()) {
            String scriptTmp = script.split("\n")[0] + "\n\n";
            String rest = "\n" + script.substring(script.indexOf("\n"));
            for (String engineDescription : engineDescriptions) {
                scriptTmp += "ENGINE " + engineDescription + ";\n";
            }
            script = scriptTmp + rest;
        }

        // add types
        for (TypeDTO t : RutaHelper.parseDeclaredTypes(script)) {
            // fix type and supertype names (add namespace)
            String typeName = nameSpace + "." + t.typeName;
            String supertypeName = t.supertypeName;
            if (supertypeName.indexOf('.') == -1) {
                supertypeName = nameSpace + "." + supertypeName;
            }
            LOG.debug("adding type {}::{}", typeName, supertypeName);

            TypeDescription typeD = tsd.addType(typeName, t.description,
                    supertypeName);
            for (TypeFeatureDTO f : t.getTypeFeatures()) {
                typeD.addFeature(f.featureName, f.description, f.rangeTypeName);
            }
        }

        // write Ruta script to tmp file
        String scriptName = pipelineId.replace(".", "_");// ruta not like dots
        File scriptFile = new File(FileBased.RUTA_PIPELINE_CACHE_PATH
                + scriptName + SCRIPT_FILE_EXTENSION);
        scriptFile.getParentFile().mkdirs();
        saveString2File(script, scriptFile);

        aeds.add(createEngineDescription(RutaEngine.class, //
                PARAM_SCRIPT_PATHS, scriptFile.getParent(), //
                PARAM_RESOURCE_PATHS, FileBased.RUTA_RESOURCES_PATH, //
                PARAM_DESCRIPTOR_PATHS, FileBased.RUTA_ENGINE_CACHE_PATH,//
                RutaEngine.PARAM_ADDITIONAL_ENGINES, engineDescriptions,//
                PARAM_MAIN_SCRIPT, scriptName));
    }

    public TypeSystemDescription getTypeSystemDescription() {
        return tsd;
    }
}
