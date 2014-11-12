package org.sherlok.mappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FileBased {

    protected static final ObjectMapper mapper = new ObjectMapper(
            new JsonFactory());
    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static void write(File f, Def<?> def)
            throws JsonGenerationException, JsonMappingException, IOException {
        mapper.writeValue(f, def);
    }

    public static void write(File f, TypesDef t)
            throws JsonGenerationException, JsonMappingException, IOException {
        mapper.writeValue(f, t);
    }

    public static TypesDef loadTypes(File f) throws ValidationException {
        try {
            return mapper.readValue(new FileInputStream(f), TypesDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static EngineDef loadEngine(File f) throws ValidationException {
        try {
            return mapper.readValue(new FileInputStream(f), EngineDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static PipelineDef loadPipeline(File f) throws ValidationException {
        try {
            return mapper.readValue(new FileInputStream(f), PipelineDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static BundleDef loadBundle(File f) throws ValidationException {
        try {
            return mapper.readValue(new FileInputStream(f), BundleDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

}
