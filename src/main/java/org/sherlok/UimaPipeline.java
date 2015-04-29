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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.json.JsonCasSerializer;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.ruta.engine.RutaEngine;
import org.apache.uima.ruta.tag.TagActionExtension;
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

    private final PipelineDef pipelineDef;
    private final String language;

    private List<AnalysisEngineDescription> aeds = list();
    private AnalysisEngine[] aes = null;

    /** Keeps track of the {@link Type}s added in every Ruta script */
    private TypeSystemDescription tsd;
    private CasPool casPool;
    /** JSON serializer */
    private JsonCasSerializer jsonSerializer;

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
    public UimaPipeline(PipelineDef pipelineDef, List<EngineDef> engineDefs)
            throws IOException, ValidationException, UIMAException {
        this.pipelineDef = pipelineDef;
        this.language = pipelineDef.getLanguage();

        this.tsd = reloadTSD();// needed since we have added new jars to the CP

        initScript(list(pipelineDef.getScriptLines()) /* a copy */, engineDefs);
        initEngines();
        casPool = initCasPool(tsd);
        jsonSerializer = filterAnnots(pipelineDef.getOutput()
                .getAnnotationIncludes(), pipelineDef.getOutput()
                .getAnnotationFilters(), casPool);

        // // ensures Ruta errors can be catched, at last
        // annotate("Some test text to check for Ruta script errors.");
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
     * @return a cas JSON serializer that filters the JSON output, either with
     *         includes or filters. If no includes/filter is provided, no
     *         filtering is performed (it just uses all the annotations from the
     *         {@link TypeSystem}).
     */
    static JsonCasSerializer filterAnnots(List<String> includes,
            List<String> filters, CasPool casPool) {

        CAS cas = casPool.getCas();
        TypeSystem ts = cas.getTypeSystem();
        TypeSystem filteredTs;
        // TODO first include, then filter!
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
        return new JsonCasSerializer().setFilterTypes(
                (TypeSystemImpl) filteredTs).setPrettyPrint(true);
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
            aes = createEngines(aeds.toArray(new AnalysisEngineDescription[aeds
                    .size()]));
        } finally { // so that we restore Sysout in any case

            // catching Ruta script outputs (these contain errors)
            String maybeOut = baosOut.toString();
            System.setOut(origOut); // restore
            String maybeErr = baosErr.toString();
            System.setErr(origErr); // restore

            if (maybeOut.length() > 0)
                LOG.info(maybeOut);
            if (maybeErr.length() > 0)
                LOG.error(maybeOut);

            if (maybeErr.length() > 0)
                throw new ValidationException("Ruta script error", maybeErr);
            for (String line : maybeOut.split("\n")) {
                if (line.startsWith("Error in line")) {
                    throw new ValidationException("Ruta script error on line",
                            line);
                }
            }
        }
    }

    static CasPool initCasPool(TypeSystemDescription tsd)
            throws ResourceInitializationException {

        if (LOG.isTraceEnabled()) {
            for (TypeDescription td : tsd.getTypes()) {
                LOG.trace("type: {}", td.getName());
            }
        }

        AnalysisEngine noOpEngine = AnalysisEngineFactory.createEngine(
                NoOpAnnotator.class, tsd);
        return new CasPool(10, noOpEngine);
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
            // FIXME both if and else bodies are equivalent. Should something
            // special be done or should the if statement be removed?
            // NB: testing sentence, linnaeus and regions: all involved
            // descriptions are primitives.
        }
        return engines;
    }

    public interface Annotate {
        public Object annotate(CAS cas, AnalysisEngine[] aes)
                throws AnalysisEngineProcessException;
    }

    /**
     * @param annotate
     *            visitor pattern
     * @return a payload, defined by 'annotate'
     */
    public Object annotate(Annotate annotate) throws UIMAException,
            SAXException, ValidationException {

        CAS cas = null;
        try {
            // TODO how long to wait?
            cas = casPool.getCas(0);// cas.reset done by casPool
            return annotate.annotate(cas, aes);
        } finally {
            casPool.releaseCas(cas);
        }
    }

    /**
     * @param text
     *            the text to annotate
     */
    public String annotate(String text) throws UIMAException,
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
            jsonSerializer.serialize(cas, sw);

            String json = sw.toString();
            // rename JSON field, for readibility
            json = json.replaceFirst("@cas_feature_structures", "annotations");
            return json;

        } catch (AnalysisEngineProcessException aepe) {
            Throwable cause = aepe.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw new ValidationException("Failed to annotate " + text,
                        cause.getMessage());
            } else {
                throw aepe;
            }
        } catch (IOException se) {
            throw new ValidationException(se);
        } finally {
            casPool.releaseCas(cas);
        }
    }

    public void close() {
        for (AnalysisEngine engine : aes) {
            engine.destroy();
        }
    }

    public PipelineDef getPipelineDef() {
        return pipelineDef;
    }

    @Override
    public String toString() {
        return pipelineDef.toString();
    }

    private void initScript(List<String> scriptLines, List<EngineDef> engineDefs)
            throws ResourceInitializationException, IOException,
            ValidationException {

        // load engines
        List<String> engineDescriptions = list();
        for (int i = 0; i < scriptLines.size(); i++) {

            String scriptLine = scriptLines.get(i);

            if (scriptLine.startsWith("ENGINE")) {
                // find the corresponding engine description
                String engineId = extractEngineId(scriptLine);
                EngineDef engineDef = findEngineDefById(engineId, engineDefs);

                // create ae and write xml descriptor
                String engineDescription = generateXmlDescriptor(engineId,
                        engineDef);
                engineDescriptions.add(engineDescription);

                // update script line
                scriptLines.set(i, "Document{-> EXEC(" + engineDescription
                        + ")}; // " + scriptLine);
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
            // fix type and supertype names (add namespace or convert to root)
            String typeName = nameSpace + "." + t.typeName;
            String supertypeName = t.supertypeName;
            if ("Annotation".equals(supertypeName)) {
                supertypeName = "uima.tcas.Annotation"; // root-superType
            } else if (supertypeName.indexOf('.') == -1) {
                // superType previousely defined in script
                supertypeName = nameSpace + "." + supertypeName;
            }
            LOG.trace("adding type '{}' (supertype '{}')", typeName,
                    supertypeName);

            TypeDescription typeD = tsd.addType(typeName, t.description,
                    supertypeName);
            for (TypeFeatureDTO f : t.getTypeFeatures()) {
                LOG.trace("  adding feat '{}' (range '{}')", f.featureName,
                        f.getRangeTypeNameCleaned());
                typeD.addFeature(f.featureName, f.description,
                        f.getRangeTypeNameCleaned());
            }
        }

        // write Ruta script to tmp file
        // ruta does not like dots
        String scriptName = pipelineDef.getId().replace(".", "_");
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

    /**
     * Engine descriptor separator
     *
     * used to separate the engine id when creating tmp xml descriptor
     */
    private static final String ENGINE_ID_SEPARATOR = "___";

    /**
     * Generate XML descriptor and return engine's descriptor
     */
    private static String generateXmlDescriptor(String engineId,
            EngineDef engineDef)
            throws ValidationException, ResourceInitializationException,
            FileNotFoundException, IOException {

        // convert fields strings to primitives
        Map<String, Object> engineParameters = extractParameters(engineDef);
        Object[] flatParamsArray = flattenParameters(engineParameters);

        // construct AE
        Class<? extends AnalysisComponent> classz = extractAnalysisComponentClass(engineDef);
        String engineDescription = engineDef
                .getIdForDescriptor(ENGINE_ID_SEPARATOR);
        AnalysisEngineDescription aed = AnalysisEngineFactory
                .createEngineDescription(classz, flatParamsArray);

        // generate XML descriptor
        try {
            File tmpEngine = new File(FileBased.RUTA_ENGINE_CACHE_PATH
                    + engineDescription + ".xml");
            tmpEngine.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(tmpEngine);
            aed.toXML(fos);
        } catch (SAXException e) {
            throw new RuntimeException("could not write descriptor of "
                    + engineId, e); // should not happen
        }

        return engineDescription;
    }

    private static Object[] flattenParameters(Map<String, Object> parameters) {
        List<Object> flatParams = list();
        for (Entry<String, Object> en : parameters.entrySet()) {
            flatParams.add(en.getKey());
            flatParams.add(en.getValue());
        }
        Object[] flatParamsArray = flatParams.toArray(new Object[flatParams
                .size()]);
        return flatParamsArray;
    }

    private static Map<String, Object> extractParameters(EngineDef engineDef)
            throws ValidationException {
        Map<String, Object> convertedParameters = map();

        // first, extract all parameters from the engine definition
        Map<String, List<String>> defParams = engineDef.getParameters();
        for (Entry<String, List<String>> en : defParams.entrySet()) {

            List<String> values = processConfigVariables(en.getValue(),
                    engineDef);

            convertedParameters.put(en.getKey(), values);
        }

        // then, extract the parameters from the class definition
        Class<? extends AnalysisComponent> klass = extractAnalysisComponentClass(engineDef);
        for (Field field : klass.getDeclaredFields()) {
            ConfigurationParameter annotation = field
                    .getAnnotation(ConfigurationParameter.class);
            if (annotation != null) {
                String parameterName = annotation.name();

                // if the parameter is also present in the engine definition
                // we override the value with the proper default value

                // TODO not sure why it's a "default" value since it comes from
                // the engine definition

                if (defParams.containsKey(parameterName)) {
                    // TODO since convertedParameters contains all elements of
                    // defParams, it should be possible to process it once only.
                    // But this would need an additional map of String to
                    // List<String>.
                    List<String> list = processConfigVariables(
                            defParams.get(parameterName), engineDef);
                    Object o = ConfigurationFieldParser
                            .getDefaultValue(field, list
                                    .toArray(new String[list.size()]));
                    convertedParameters.put(parameterName, o); // override value
                }
            }
        }
        
        return convertedParameters;
    }

    // Process configuration variables in each value
    private static List<String> processConfigVariables(List<String> values,
            EngineDef engineDef) {
        List<String> processed = list();
        for (String value : values) {
            processed.add(processConfigVariables(value, engineDef));
        }

        return processed;
    }

    // Accepts variable starting by '$' and containing only alpha-numerical
    // characters (+ underscore). The variable can be accessed through the
    // named-capturing group "name".
    private static final Pattern VARIABLE_PATTERN = Pattern
            .compile("\\$(?<name>\\w+)");

    // Process configuration variables
    private static String processConfigVariables(String value,
            EngineDef engineDef) {
        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        Map<String, String> config = engineDef.getBundle().getConfig();

        while (matcher.find()) {
            String name = matcher.group("name");
            String processed = processConfigVariable(name, config);

            // replace all occurrences of "$name" in the original string
            value = value.replace(matcher.group(), processed);
        }

        return value;
    }

    private static String processConfigVariable(String name,
            Map<String, String> config) {
        // TODO handle URLs here (e.g. file, git, ...)

        // fallback: basic substitution
        return config.get(name);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends AnalysisComponent> extractAnalysisComponentClass(
            EngineDef engineDef) throws ValidationException {
        try {
            return (Class<? extends AnalysisComponent>) Class
                    .forName(engineDef.getClassz());
        } catch (ClassNotFoundException e) {
            throw new ValidationException("could not find class "
                    + engineDef.getClassz(), e);
        }
    }

    /**
     * Find an engine definition in a given list based on its id.
     * 
     * @throws ValidationException
     *             when no such engine exists in the list
     */
    private static EngineDef findEngineDefById(String pengineId,
            List<EngineDef> engineDefs) throws ValidationException {
        for (EngineDef engineDef : engineDefs) {
            if (engineDef.getId().equals(pengineId)) {
                return engineDef;
            }
        }

        throw new ValidationException("pipeline engine not found", pengineId);
    }

    private static String extractEngineId(String scriptLine)
            throws ValidationException {
        String pengineId = scriptLine.trim()
                .substring("ENGINE".length()).trim()
                .replaceAll(";", "");
        validateId(pengineId, "ENGINE id in '" + pengineId + "'");
        return pengineId;
    }
}
