package org.sherlok;

import static org.sherlok.Sherlok.CONFIG_FILE_PATH;
import static org.sherlok.mappings.PipelineDef.getName;
import static org.sherlok.mappings.PipelineDef.getVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.Def;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.TypesDef;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Single central point to interact with json config files;
 * 
 * @author renaud@apache.org
 */
public class FileBased {

    public static final ObjectMapper MAPPER = new ObjectMapper(
            new JsonFactory());
    static {
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static void write(File f, Def def) throws ValidationException {
        try {
            MAPPER.writeValue(f, def);
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static void writeType(TypesDef t) throws JsonGenerationException,
            JsonMappingException, IOException {
        try {
            File tFile = new File(CONFIG_FILE_PATH + t.getName() + ".json");
            tFile.mkdirs();
            MAPPER.writeValue(tFile, t);
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static TypesDef loadTypes(File f) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), TypesDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static EngineDef loadEngine(File f) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), EngineDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static BundleDef loadBundle(File f) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), BundleDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static PipelineDef loadPipeline(File f) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), PipelineDef.class);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static void writePipeline(PipelineDef def)
            throws ValidationException {
        def.validate(def.toString());
        File defFile = new File(Store.PIPELINES_PATH + def.getDomain() + "/"
                + def.getName() + "_" + def.getVersion() + ".json");
        defFile.getParentFile().mkdirs();
        write(defFile, def);
    }

    public static PipelineDef putPipeline(String pipelineStr) {

        try {
            PipelineDef pipelineDef = MAPPER.readValue(pipelineStr,
                    PipelineDef.class);
            FileBased.writePipeline(pipelineDef);
            return pipelineDef;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static boolean deletePipeline(String pipelineId, String domain) {
        File defFile = new File(Store.PIPELINES_PATH + domain + "/"
                + getName(pipelineId) + "_" + getVersion(pipelineId) + ".json");
        if (!defFile.exists())
            throw new ValidationException("Cannot delete pipeline '"
                    + pipelineId + "', since it does not exist");
        return defFile.delete();
    }
}
