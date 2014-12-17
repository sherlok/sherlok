package org.sherlok.utils;

import static org.apache.uima.fit.factory.ConfigurationParameterFactory.isConfigurationParameterField;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UIMA_IllegalArgumentException;
import org.apache.uima.resource.metadata.ConfigurationParameter;

/**
 * @see ConfigurationParameterFactory
 * 
 * @author renaud@apache.org
 */
public class ConfigurationFieldParser {

    /**
     * A mapping from Java class names to UIMA configuration parameter type
     * names. Used by setConfigurationParameters().
     */
    private static final Map<String, String> JAVA_UIMA_TYPE_MAP = new HashMap<String, String>();
    static {
        JAVA_UIMA_TYPE_MAP.put(Boolean.class.getName(),
                ConfigurationParameter.TYPE_BOOLEAN);
        JAVA_UIMA_TYPE_MAP.put(Float.class.getName(),
                ConfigurationParameter.TYPE_FLOAT);
        JAVA_UIMA_TYPE_MAP.put(Double.class.getName(),
                ConfigurationParameter.TYPE_FLOAT);
        JAVA_UIMA_TYPE_MAP.put(Integer.class.getName(),
                ConfigurationParameter.TYPE_INTEGER);
        JAVA_UIMA_TYPE_MAP.put(String.class.getName(),
                ConfigurationParameter.TYPE_STRING);
        JAVA_UIMA_TYPE_MAP.put("boolean", ConfigurationParameter.TYPE_BOOLEAN);
        JAVA_UIMA_TYPE_MAP.put("float", ConfigurationParameter.TYPE_FLOAT);
        JAVA_UIMA_TYPE_MAP.put("double", ConfigurationParameter.TYPE_FLOAT);
        JAVA_UIMA_TYPE_MAP.put("int", ConfigurationParameter.TYPE_INTEGER);

    }

    private static boolean isMultiValued(Field field) {
        Class<?> parameterClass = field.getType();
        if (parameterClass.isArray()) {
            return true;
        } else if (Collection.class.isAssignableFrom(parameterClass)) {
            return true;
        }
        return false;
    }

    /**
     * Determines the default value of an annotated configuration parameter. The
     * returned value is not necessarily the value that the annotated member
     * variable will be instantiated with in ConfigurationParameterInitializer
     * which does extra work to convert the UIMA configuration parameter value
     * to comply with the type of the member variable.
     * 
     * @param field
     *            the field to analyze
     * @return the default value
     */
    public static Object getDefaultValue(Field field, String[] stringValue) {
        if (isConfigurationParameterField(field)) {

            if (stringValue.length == 1
                    && stringValue[0]
                            .equals(org.apache.uima.fit.descriptor.ConfigurationParameter.NO_DEFAULT_VALUE)) {
                return null;
            }

            String valueType = getConfigurationParameterType(field);
            boolean isMultiValued = isMultiValued(field);

            if (!isMultiValued) {
                if (ConfigurationParameter.TYPE_BOOLEAN.equals(valueType)) {
                    return Boolean.parseBoolean(stringValue[0]);
                } else if (ConfigurationParameter.TYPE_FLOAT.equals(valueType)) {
                    return Float.parseFloat(stringValue[0]);
                } else if (ConfigurationParameter.TYPE_INTEGER
                        .equals(valueType)) {
                    return Integer.parseInt(stringValue[0]);
                } else if (ConfigurationParameter.TYPE_STRING.equals(valueType)) {
                    return stringValue[0];
                }
                throw new UIMA_IllegalArgumentException(
                        UIMA_IllegalArgumentException.METADATA_ATTRIBUTE_TYPE_MISMATCH,
                        new Object[] { valueType, "type" });
            } else {
                if (ConfigurationParameter.TYPE_BOOLEAN.equals(valueType)) {
                    Boolean[] returnValues = new Boolean[stringValue.length];
                    for (int i = 0; i < stringValue.length; i++) {
                        returnValues[i] = Boolean.parseBoolean(stringValue[i]);
                    }
                    return returnValues;
                } else if (ConfigurationParameter.TYPE_FLOAT.equals(valueType)) {
                    Float[] returnValues = new Float[stringValue.length];
                    for (int i = 0; i < stringValue.length; i++) {
                        returnValues[i] = Float.parseFloat(stringValue[i]);
                    }
                    return returnValues;
                } else if (ConfigurationParameter.TYPE_INTEGER
                        .equals(valueType)) {
                    Integer[] returnValues = new Integer[stringValue.length];
                    for (int i = 0; i < stringValue.length; i++) {
                        returnValues[i] = Integer.parseInt(stringValue[i]);
                    }
                    return returnValues;
                } else if (ConfigurationParameter.TYPE_STRING.equals(valueType)) {
                    return stringValue;
                }
                throw new UIMA_IllegalArgumentException(
                        UIMA_IllegalArgumentException.METADATA_ATTRIBUTE_TYPE_MISMATCH,
                        new Object[] { valueType, "type" });

            }

        } else {
            throw new IllegalArgumentException(
                    "field is not annotated with annotation of type "
                            + org.apache.uima.fit.descriptor.ConfigurationParameter.class
                                    .getName());
        }
    }

    private static String getConfigurationParameterType(Field field) {
        Class<?> parameterClass = field.getType();
        String parameterClassName;
        if (parameterClass.isArray()) {
            parameterClassName = parameterClass.getComponentType().getName();
        } else if (Collection.class.isAssignableFrom(parameterClass)) {
            ParameterizedType collectionType = (ParameterizedType) field
                    .getGenericType();
            parameterClassName = ((Class<?>) (collectionType
                    .getActualTypeArguments()[0])).getName();
        } else {
            parameterClassName = parameterClass.getName();
        }
        String parameterType = JAVA_UIMA_TYPE_MAP.get(parameterClassName);
        if (parameterType == null) {
            return ConfigurationParameter.TYPE_STRING;
        }
        return parameterType;
    }
}
