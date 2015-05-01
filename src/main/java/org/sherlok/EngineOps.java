package org.sherlok;

import static org.sherlok.utils.Create.map;

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
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.ConfigurationFieldParser;
import org.sherlok.utils.MapOps;
import org.sherlok.utils.ValidationException;
import org.xml.sax.SAXException;

/**
 * Operations on Engine such as generating XML description for RUTA.
 */
public class EngineOps {

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
            List<EngineDef> engineDefs) throws ValidationException,
            ResourceInitializationException {

        EngineDef engineDef = findEngineDefById(engineId, engineDefs);

        // convert fields strings to primitives
        Map<String, Object> engineParameters = EngineOps
                .extractParameters(engineDef);
        Object[] flatParamsArray = MapOps.flattenParameters(engineParameters);

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
        } catch (SAXException | IOException e) {
            throw new RuntimeException("could not write descriptor of "
                    + engineId, e); // should not happen
        }

        return engineDescription;
    }


    private static Map<String, Object> extractParameters(EngineDef engineDef)
            throws ValidationException {
        Map<String, Object> convertedParameters = map();

        // first, extract all parameters from the engine definition
        Map<String, List<String>> defParams = engineDef.getParameters();
        for (Entry<String, List<String>> en : defParams.entrySet()) {

            List<String> values = ConfigVariableManager.processConfigVariables(
                    en.getValue(), engineDef);

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
                    List<String> list = ConfigVariableManager
                            .processConfigVariables(
                                    defParams.get(parameterName), engineDef);
                    Object o = ConfigurationFieldParser.getDefaultValue(field,
                            list.toArray(new String[list.size()]));
                    convertedParameters.put(parameterName, o); // override value
                }
            }
        }

        return convertedParameters;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends AnalysisComponent> extractAnalysisComponentClass(
            EngineDef engineDef) throws ValidationException {
        try {
            return (Class<? extends AnalysisComponent>) Class.forName(engineDef
                    .getClassz());
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

}
