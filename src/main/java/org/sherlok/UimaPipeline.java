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
import static org.sherlok.utils.CheckThat.validateId;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.FilteringTypeSystem;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.ruta.engine.RutaEngine;
import org.apache.uima.util.CasPool;
import org.sherlok.RutaHelper.TypeDTO;
import org.sherlok.RutaHelper.TypeFeatureDTO;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.ConfigurationFieldParser;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * Manages a UIMA pipeline (configuration, and then use/annotation), based on a
 * {@link PipelineDef}.<br>
 * Lifecycle:<br>
 * <ol>
 * <li>add deps on classpath (to have TypeSystems scannable)</li>
 * <li>create UimaPipeline with script, engines, annotations , ...</li>
 * <li>annotate texts...</li>
 * <li>close</li>
 * </ol>
 * 
 * @author renaud@apache.org
 */
public class UimaPipeline {
    private static Logger LOG = getLogger(UimaPipeline.class);

    private final String pipelineId;
    private final String language;

    private List<AnalysisEngineDescription> aeds = list();
    private AnalysisEngine[] aes = null;

    /** Keeps track of the {@link Type}s added in every Ruta script */
    private TypeSystemDescription tsd;
    private CasPool casPool;
    /** JSON serializer */
    private XmiCasSerializer xcs;

    /**
     * @param pipelineId
     * @param language
     *            , important e.g. for DKpro components.
     * @param engineDefs
     * @param scriptLines
     *            the Ruta script
     * @param annotationIncludes
     * @param annotationFilters
     */
    public UimaPipeline(String pipelineId, String language,
            List<EngineDef> engineDefs, List<String> scriptLines,
            List<String> annotationIncludes, List<String> annotationFilters)
            throws IOException, ValidationException, UIMAException {
        this.pipelineId = pipelineId;
        this.language = language;

        this.tsd = reloadTSD();// needed since we have added new jars to the CP

        initScript(list(scriptLines) /* a copy */, engineDefs);
        initEngines();
        initCasPool();
        filterAnnots(annotationIncludes, annotationFilters);
    }

    static TypeSystemDescription reloadTSD() {
        try {
            TypeSystemDescriptionFactory.forceTypeDescriptorsScan();
            return TypeSystemDescriptionFactory.createTypeSystemDescription();
        } catch (ResourceInitializationException e) {
            throw new RuntimeException(e); // should not happen
        }

    }

    /**
     * Filters the JSON output, either with includes or filters. If no
     * includes/filter is provided, it is just a copy of the 'normal'
     * {@link TypeSystem}.
     */
    private void filterAnnots(List<String> includes, List<String> filters) {

        CAS cas = casPool.getCas();
        TypeSystem ts = cas.getTypeSystem();
        TypeSystem filteredTs;

        if (!includes.isEmpty()) {

            filteredTs = new FilteringTypeSystem();
            Iterator<Type> tit = ts.getTypeIterator();
            while (tit.hasNext()) {
                Type type = tit.next();

                boolean shouldInclude = false;
                for (String include : includes) {
                    if (include.endsWith(".*")
                            && type.getName().startsWith(
                                    include.substring(0, include.length() - 2))) {
                        shouldInclude = true;
                        break;
                    } else if (include.equals(type.getName())) {
                        shouldInclude = true;
                        break;
                    }
                }
                if (shouldInclude) {
                    LOG.trace("including type '{}'", type);
                    ((FilteringTypeSystem) filteredTs).includeType(type);
                }
            }

        } else if (!filters.isEmpty()) {

            filteredTs = new FilteringTypeSystem();
            Iterator<Type> tit = ts.getTypeIterator();
            while (tit.hasNext()) {
                Type type = tit.next();

                boolean shouldFilter = false;
                for (String filter : filters) {
                    if (filter.endsWith(".*")
                            && type.getName().startsWith(
                                    filter.substring(0, filter.length() - 2))) {
                        shouldFilter = true;
                        break;
                    } else if (filter.equals(type.getName())) {
                        shouldFilter = true;
                        break;
                    }
                }
                if (!shouldFilter) {
                    LOG.trace("including type '{}'", type);
                    ((FilteringTypeSystem) filteredTs).includeType(type);
                } else {
                    LOG.trace("filtering type '{}'", type);
                }
            }

        } else { // no filtering or includes --> use full ts
            LOG.trace("including all type");
            filteredTs = ts;
        }
        casPool.releaseCas(cas);

        // initialize JSON writer with filter
        xcs = new XmiCasSerializer(filteredTs);
        xcs.setPrettyPrint(true);
    }

    private void initEngines() throws UIMAException, ValidationException {
        // redirect stdout to catch Ruta script errors
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        System.setOut(new PrintStream(baosOut));
        System.setErr(new PrintStream(baosErr));

        try {
            // initialize Engines
            if (aes == null) { // not initialized yet
                aes = createEngines(aeds
                        .toArray(new AnalysisEngineDescription[aeds.size()]));
            }
        } finally { // so that we restore Sysout in any case

            // catching Ruta script outputs (these contain errors) and errors
            String maybeOut = baosOut.toString();
            System.setOut(origOut); // restore
            String maybeErr = baosErr.toString();
            System.setErr(origErr); // restore

            if (maybeOut.length() > 0)
                LOG.info(maybeOut);
            if (maybeErr.length() > 0)
                LOG.error(maybeOut);

            if (maybeErr.length() > 0)
                throw new ValidationException(maybeErr);
            for (String line : maybeOut.split("\n")) {
                if (line.startsWith("Error in line")) {
                    throw new ValidationException(line);
                }
            }
        }
    }

    private void initCasPool() throws ResourceInitializationException {

        // for (TypeDescription td : tsd.getTypes())
        // LOG.debug("type: {}", td.getName());

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

    public interface Annotate {
        public Object annotate(JCas jCas, AnalysisEngine[] aes)
                throws AnalysisEngineProcessException;
    }

    /**
     * @param annotate
     *            object, using visitor pattern
     */
    public void annotate(Annotate annotate) throws UIMAException, SAXException,
            ValidationException {

        CAS cas = null;
        try {
            // TODO how long to wait?
            cas = casPool.getCas(0);// cas.reset done by casPool
            annotate.annotate(cas.getJCas(), aes);
        } finally {
            casPool.releaseCas(cas);
        }
    }

    /**
     * @param text
     *            the text to annotate
     */
    public String annotate(String text) throws UIMAException, SAXException,
            ValidationException {

        CAS cas = null;
        try {
            // TODO how long to wait?
            cas = casPool.getCas(0);// cas.reset done by casPool
            // for (TypeDescription td : tsd.getTypes())
            // LOG.debug("type: {} <<<< {}", td.getName(),
            // td.getSupertypeName());

            cas.setDocumentText(text);
            cas.setDocumentLanguage(language);

            SimplePipeline.runPipeline(cas, aes);

            if (LOG.isTraceEnabled()) {
                FSIterator<Annotation> it = cas.getJCas().getAnnotationIndex()
                        .iterator();
                while (it.hasNext()) {
                    Annotation a = it.next();
                    StringBuffer sb = new StringBuffer();
                    a.prettyPrint(2, 2, sb, false);
                    LOG.trace("''{}'' {}", a.getCoveredText(), sb.toString());
                }
            }

            StringWriter sw = new StringWriter();
            xcs.serializeJson(cas, sw);
            return sw.toString();

        } catch (AnalysisEngineProcessException aepe) {
            Throwable cause = aepe.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw new ValidationException(cause.getMessage());
            } else {
                throw aepe;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            casPool.releaseCas(cas);
        }
    }

    public void close() {
        for (AnalysisEngine engine : aes) {
            engine.destroy();
        }
    }

    @Override
    public String toString() {
        return pipelineId;
    }

    @SuppressWarnings("unchecked")
    private void initScript(List<String> scriptLines, List<EngineDef> engineDefs)
            throws ResourceInitializationException, IOException,
            ValidationException {

        // load engines
        List<String> engineDescriptions = list();
        for (int i = 0; i < scriptLines.size(); i++) {

            String scriptLine = scriptLines.get(i);

            if (scriptLine.startsWith("ENGINE")) {
                // TODO refactor and abstract
                String pengineId = scriptLine.trim()
                        .substring("ENGINE".length()).trim()
                        .replaceAll(";", "");
                validateId(pengineId, "ENGINE id in '" + pengineId + "'");

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

                        // convert fields strings to primitives
                        Map<String, Object> convertedParameters = map();
                        for (Entry<String, List<String>> en : engineDef
                                .getParameters().entrySet()) {
                            convertedParameters.put(en.getKey(), en.getValue());
                        }
                        for (Field f : classz.getDeclaredFields()) {
                            java.lang.annotation.Annotation[] annots2 = f
                                    .getDeclaredAnnotations();
                            for (java.lang.annotation.Annotation a : annots2) {
                                if (a instanceof ConfigurationParameter) {

                                    String parameterName = ((ConfigurationParameter) a)
                                            .name();
                                    if (engineDef.getParameters().containsKey(
                                            parameterName)) {
                                        List<String> list = engineDef
                                                .getParameter(parameterName);
                                        Object o = ConfigurationFieldParser
                                                .getDefaultValue(
                                                        f,
                                                        list.toArray(new String[list
                                                                .size()]));
                                        convertedParameters.put(parameterName,
                                                o);
                                    }
                                }
                            }
                        }
                        List<Object> flatParams = list();
                        for (Entry<String, Object> en : convertedParameters
                                .entrySet()) {
                            flatParams.add(en.getKey());
                            flatParams.add(en.getValue());
                        }
                        Object[] flatParamsArray = flatParams
                                .toArray(new Object[flatParams.size()]);

                        // create ae and write xml descriptor
                        AnalysisEngineDescription aed = AnalysisEngineFactory
                                .createEngineDescription(classz,
                                        flatParamsArray);
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
            LOG.trace("adding type {}::{}", typeName, supertypeName);

            TypeDescription typeD = tsd.addType(typeName, t.description,
                    supertypeName);
            for (TypeFeatureDTO f : t.getTypeFeatures()) {
                LOG.trace("  adding feat {}::{}", f.featureName,
                        f.getRangeTypeNameCleaned());
                typeD.addFeature(f.featureName, f.description,
                        f.getRangeTypeNameCleaned());
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
}
