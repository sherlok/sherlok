package org.sherlok.mappings;

import static org.sherlok.utils.CheckThat.checkNotNull;
import static org.sherlok.utils.Create.list;

import java.util.List;

import org.apache.uima.cas.Type;
import org.sherlok.utils.ValidationException;

/**
 * Description of UIMA Types
 * 
 * @author renaud@apache.org
 */
public class TypesDef {

    /** a unique name for this bundle. Letters, numbers and underscore only */
    protected String name;

    /** all types */
    private List<TypeDef> types = list();

    /** Represents a UIMA {@link Type}. */
    public static class TypeDef {
        /** The {@link Type} class name */
        private String classz,
        /** Short name to be used in output. Should be unique. */
        shortName,
        /** CSS color, for client (optional) */
        color,
        /** Description of this type (optional) */
        description;
        /** Properties to be outputed(optional) */
        private List<String> properties = list();

        public String getClassz() {
            return classz;
        }

        public TypeDef setClassz(String classz) {
            this.classz = classz;
            return this;
        }

        public String getShortName() {
            return shortName;
        }

        public TypeDef setShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public String getColor() {
            return color;
        }

        public TypeDef setColor(String color) {
            this.color = color;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public TypeDef setDescription(String description) {
            this.description = description;
            return this;
        }

        public List<String> getProperties() {
            return properties;
        }

        public TypeDef setProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public TypeDef addProperty(String property) {
            this.properties.add(property);
            return this;
        }

        /** WARNING: for this to work, all UIMA jars MUST be on the classpath! */
        public boolean validate() throws ValidationException {
            if (!isUimaAnnotation(getClassz())) {
                throw new ValidationException("'" + toString()
                        + "'is not a valid UIMA Annotation class");
            }
            try {
                checkNotNull(shortName, "'shortName' of '" + toString()
                        + "' should not be null");
            } catch (Exception e) {
                new ValidationException(e.getMessage());
            }
            return true;
        }

        @Override
        public String toString() {
            return shortName + "[" + classz + "]";
        }
    }

    public String getName() {
        return name;
    }

    public TypesDef setName(String name) {
        this.name = name;
        return this;
    }

    public List<TypeDef> getTypes() {
        return types;
    }

    public TypesDef setTypes(List<TypeDef> types) {
        this.types = types;
        return this;
    }

    public TypesDef addType(TypeDef type) {
        this.types.add(type);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TypeDef type : types) {
            sb.append(type).append("/n");
        }
        return sb.toString();
    }

    @SuppressWarnings("unused")
    private static boolean isUimaAnnotation(String value) {
        // try to load the class
        Class<?> clasz = null;
        try {

            clasz = Class.forName(value);
            // System.out.println("testing" + clasz.getName());
        } catch (Exception e) {
            return false;
        }

        while (true) {
            Class<?> superclass = clasz.getSuperclass();
            // System.out
            // .println("  |-superclass " + superclass.getName());
            if (superclass.getName().equals(
                    "org.apache.uima.jcas.cas.AnnotationBase")) {
                return true; // free, otherwise loop
            } else if (superclass == null) {
                return false;
                // fail(value
                // +
                // " should be an instance of AnnotationBase (I could not find it, but it is defined in TypeSystem.java)");
            }
            clasz = superclass;
        }
    }
}
