package org.sherlok;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.Create.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.TypesDef;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Single entry point to interact with JSON config files for {@link TypesDef}s,
 * {@link BundleDef}s, {@link EngineDef}s and {@link PipelineDef}s. All these
 * JSON files are stored in the directory {@link Sherlok#CONFIG_DIR_PATH}.
 * 
 * @author renaud@apache.org
 */
public class FileBased {

    public static final String CONFIG_DIR_PATH = "config/";

    protected static final String TYPES_PATH = CONFIG_DIR_PATH + "types/";
    protected static final String BUNDLES_PATH = CONFIG_DIR_PATH + "bundles/";
    protected static final String ENGINES_PATH = CONFIG_DIR_PATH + "engines/";
    protected static final String PIPELINES_PATH = CONFIG_DIR_PATH
            + "pipelines/";
    protected static final String RUTA_RESOURCES_PATH = CONFIG_DIR_PATH
            + "ruta/";
    protected static final String RUTA_PIPELINE_CACHE_PATH = RUTA_RESOURCES_PATH
            + ".pipelines/";

    private static final ObjectMapper MAPPER = new ObjectMapper(
            new JsonFactory());
    static {
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static BundleDef putBundle(String bundleStr)
            throws ValidationException {
        try {
            BundleDef bundleDef = MAPPER.readValue(bundleStr, BundleDef.class);
            writeBundle(bundleDef);
            return bundleDef;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static EngineDef putEngine(String engineStr)
            throws ValidationException {
        try {
            EngineDef engineDef = MAPPER.readValue(engineStr, EngineDef.class);
            writeEngine(engineDef);
            return engineDef;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static PipelineDef putPipeline(String pipelineStr)
            throws ValidationException {
        try {
            PipelineDef pipelineDef = MAPPER.readValue(pipelineStr,
                    PipelineDef.class);
            writePipeline(pipelineDef);
            return pipelineDef;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static void write(File f, Object def) throws ValidationException {
        try {
            MAPPER.writeValue(f, def);
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    public static String writeAsString(Object obj)
            throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    @SuppressWarnings("unused")
    // TODO use me!
    private static void writeType(TypesDef t) throws JsonGenerationException,
            JsonMappingException, IOException, ValidationException {
        try {
            File tFile = new File(TYPES_PATH + t.getName() + ".json");
            tFile.getParentFile().mkdirs();
            MAPPER.writeValue(tFile, t);
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    private static void writeBundle(BundleDef def) throws ValidationException {
        def.validate(def.toString());
        File defFile = new File(BUNDLES_PATH + def.getName() + "_"
                + def.getVersion() + ".json");
        defFile.getParentFile().mkdirs();
        write(defFile, def);
    }

    private static void writeEngine(EngineDef def) throws ValidationException {
        def.validate(def.toString());
        File defFile = new File(ENGINES_PATH + def.getDomain() + "/"
                + def.getName() + "_" + def.getVersion() + ".json");
        defFile.getParentFile().mkdirs();
        write(defFile, def);
    }

    private static void writePipeline(PipelineDef def)
            throws ValidationException {
        def.validate(def.toString());
        File defFile = new File(PIPELINES_PATH + def.getDomain() + "/"
                + def.getName() + "_" + def.getVersion() + ".json");
        defFile.getParentFile().mkdirs();
        write(defFile, def);
    }

    public static <T> T read(File f, Class<T> clazz) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), clazz);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static Collection<TypesDef> allTypesDefs()
            throws ValidationException {
        List<TypesDef> ret = list();
        for (File tf : newArrayList(iterateFiles(new File(TYPES_PATH),
                new String[] { "json" }, true))) {
            ret.add(read(tf, TypesDef.class));
        }
        return ret;
    }

    public static Collection<BundleDef> allBundleDefs()
            throws ValidationException {
        List<BundleDef> ret = list();
        for (File bf : newArrayList(iterateFiles(new File(BUNDLES_PATH),
                new String[] { "json" }, true))) {
            ret.add(read(bf, BundleDef.class));
        }
        return ret;
    }

    public static Collection<EngineDef> allEngineDefs()
            throws ValidationException {
        List<EngineDef> ret = list();
        for (File bf : newArrayList(iterateFiles(new File(ENGINES_PATH),
                new String[] { "json" }, true))) {
            ret.add(read(bf, EngineDef.class));
        }
        return ret;
    }

    public static Collection<PipelineDef> allPipelineDefs()
            throws ValidationException {
        List<PipelineDef> ret = list();
        for (File bf : newArrayList(iterateFiles(new File(PIPELINES_PATH),
                new String[] { "json" }, true))) {
            ret.add(FileBased.read(bf, PipelineDef.class));
        }
        return ret;
    }

    public static boolean deleteBundle(String bundleId)
            throws ValidationException {
        File defFile = new File(BUNDLES_PATH + getName(bundleId) + "_"
                + getVersion(bundleId) + ".json");
        if (!defFile.exists())
            throw new ValidationException("Cannot delete bundle '" + bundleId
                    + "', since it does not exist");
        return defFile.delete();
    }

    public static boolean deleteEngine(String engineId, String domain)
            throws ValidationException {
        File defFile = new File(ENGINES_PATH + domain + "/" + getName(engineId)
                + "_" + getVersion(engineId) + ".json");
        if (!defFile.exists())
            throw new ValidationException("Cannot delete engine '" + engineId
                    + "', since it does not exist");
        return defFile.delete();
    }

    public static boolean deletePipeline(String pipelineId, String domain)
            throws ValidationException {
        File defFile = new File(PIPELINES_PATH + domain + "/"
                + getName(pipelineId) + "_" + getVersion(pipelineId) + ".json");
        if (!defFile.exists())
            throw new ValidationException("Cannot delete pipeline '"
                    + pipelineId + "', since it does not exist");
        return defFile.delete();
    }
}
