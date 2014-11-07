package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.uima.cas.Type;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * UIMA Types
 * 
 * @author renaud@apache.org
 */
public class TypesDef {

    /** all types */
    private List<TypeDef> types = list();

    /** Represents a UIMA {@link Type}. */
    public static class TypeDef {
        private String classz, shortName, color, description;
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

        @Override
        public String toString() {
            return shortName + "[" + classz + "]";
        }
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

    // read/write

    static final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public void write(File f) throws JsonGenerationException,
            JsonMappingException, IOException {
        mapper.writeValue(f, this);
    }

    public static TypesDef load(File f) throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        return mapper.readValue(new FileInputStream(f), TypesDef.class);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TypeDef type : types) {
            sb.append(type).append("/n");
        }
        return sb.toString();
    }

    public boolean validate() {
        // FIXME
        return true;
    }
}
