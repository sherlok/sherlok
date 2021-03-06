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

import static org.sherlok.config.ConfigVariableManager.processConfigVariables;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.sherlok.config.NoSuchVariableException;
import org.sherlok.config.ProcessConfigVariableException;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.SherlokException;
import org.sherlok.utils.ConfigurationFieldParser;
import org.sherlok.utils.ops.MapOps;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * Operations on Engine such as generating XML description for RUTA.
 */
public class EngineOps {

    private static Logger LOG = getLogger(EngineOps.class);

    /**
     * Engine descriptor separator
     *
     * used to separate the engine id when creating tmp xml descriptor
     */
    private static final String ENGINE_ID_SEPARATOR = "___";

    /**
     * Generate XML descriptor and return engine's descriptor
     */
    static String generateXmlDescriptor(String engineId,
            List<EngineDef> engineDefs) throws SherlokException,
            ResourceInitializationException {

        EngineDef engineDef = findEngineDefById(engineId, engineDefs);

        // convert fields strings to primitives
        Map<String, Object> engineParameters = extractParameters(engineDef);
        Object[] flatParamsArray = MapOps.flattenParameters(engineParameters);

        // construct AE
        Class<? extends AnalysisComponent> classz = extractAnalysisComponentClass(engineDef);
        String engineDescription = engineDef
                .getIdForDescriptor(ENGINE_ID_SEPARATOR);
        AnalysisEngineDescription aed = AnalysisEngineFactory
                .createEngineDescription(classz, flatParamsArray);

        // generate XML descriptor
        try {
            File tmpEngine = new File(FileBased.ENGINE_CACHE_PATH
                    + engineDescription + ".xml");
            tmpEngine.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(tmpEngine);
            aed.toXML(fos);
        } catch (SAXException | IOException e) {
            throw new RuntimeException("could not write descriptor of "
                    + engineId, e); // should not happen
        }

        return engineDescription;
    }

    /**
     * Extract parameters from the engine definition and use annotation in the
     * corresponding annotator class to convert the parameter value to the right
     * type.
     */
    private static Map<String, Object> extractParameters(EngineDef engineDef)
            throws SherlokException {
        try {
            // build a [param name -> annotated field] mapping
            Map<String, Field> annotatedFields = extractParametersFields(engineDef);

            // load all parameters from the engine definition
            Map<String, Object> convertedParameters = map();
            Map<String, List<String>> defParams = engineDef.getParameters();
            for (Entry<String, List<String>> en : defParams.entrySet()) {
                String parameterName = en.getKey();

                List<String> values = processConfigVariables(en.getValue(),
                        engineDef.getBundle());

                Field field = annotatedFields.get(parameterName);
                if (field == null) {
                    throw new SherlokException(
                            "Expected annotated field in annotator '"
                                    + engineDef.getClassz()
                                    + "' for parameter name '" + parameterName
                                    + "'");
                }

                Object configuredValue = ConfigurationFieldParser
                        .getDefaultValue(field,
                                values.toArray(new String[values.size()]));
                convertedParameters.put(parameterName, configuredValue);
            }

            return convertedParameters;
        } catch (NoSuchVariableException | ProcessConfigVariableException e) {
            throw new SherlokException(e.getMessage()).setObject(engineDef
                    .toString());
        }
    }

    /** Create a map from configuration parameter name to the matching field */
    @SuppressWarnings("unchecked")
    private static Map<String, Field> extractParametersFields(
            EngineDef engineDef) throws SherlokException {
        Class<? extends AnalysisComponent> klass = extractAnalysisComponentClass(engineDef);
        Map<String, Field> params = map();

        // we need to recurse and find annotated field in super classes too
        while (klass != null) {
            for (Field field : klass.getDeclaredFields()) {
                ConfigurationParameter annotation = field
                        .getAnnotation(ConfigurationParameter.class);
                if (annotation != null) {
                    String parameterName = annotation.name();
                    params.put(parameterName, field);
                }
            }

            // find super class (if still implementing AnalysisComponent)
            Class<?> superklass = klass.getSuperclass();
            if (AnalysisComponent.class.isAssignableFrom(superklass)) {
                // LOG.trace("klass " + klass.getName() + " has superklass: "
                // + superklass.getName());
                klass = (Class<? extends AnalysisComponent>) superklass;
            } else {
                LOG.trace("klass " + klass.getName()
                        + " has no matching superclass.");
                klass = null;
            }

        }

        // TODO is it possible that a subclass redefines a annotated field?
        // If yes, we don't handle it properly ATM: the loop should start from
        // the top type instead.

        return params;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends AnalysisComponent> extractAnalysisComponentClass(
            EngineDef engineDef) throws SherlokException {
        try {
            return (Class<? extends AnalysisComponent>) Class.forName(engineDef
                    .getClassz());
        } catch (ClassNotFoundException e) {
            throw new SherlokException().setMessage(
                    "could not find AnalysisComponent class").setObject(
                    engineDef.getClassz());
        }
    }

    /**
     * Find an engine definition in a given list based on its id.
     * 
     * @throws SherlokException
     *             when no such engine exists in the list
     */
    private static EngineDef findEngineDefById(String pengineId,
            List<EngineDef> engineDefs) throws SherlokException {
        for (EngineDef engineDef : engineDefs) {
            if (engineDef.getId().equals(pengineId)) {
                return engineDef;
            }
        }
        throw new SherlokException("pipeline engine not found")
                .setObject(pengineId);
    }
}
