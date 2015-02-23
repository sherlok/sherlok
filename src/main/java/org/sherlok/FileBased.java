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

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.Def;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Single entry point to interact with JSON config files for {@link BundleDef}s,
 * {@link PipelineDef}s and Resources. All these JSON files are stored in the
 * directory {@link Sherlok#CONFIG_DIR_PATH}.
 * 
 * @author renaud@apache.org
 */
/**
 * @author richarde
 *
 */
public class FileBased {

    private static final String CONFIG_DIR_PATH = "config/";

    static final String TYPES_PATH = CONFIG_DIR_PATH + "types/";
    static final String BUNDLES_PATH = CONFIG_DIR_PATH + "bundles/";
    static final String PIPELINES_PATH = CONFIG_DIR_PATH + "pipelines/";
    static final String RUTA_RESOURCES_PATH = CONFIG_DIR_PATH + "resources/";
    static final String RUTA_PIPELINE_CACHE_PATH = RUTA_RESOURCES_PATH
            + ".pipelines/";
    static final String RUTA_ENGINE_CACHE_PATH = RUTA_RESOURCES_PATH
            + ".engines/";

    /** 50Mb, in bytes */
    static final long MAX_UPLOAD_SIZE = 50 * 1000000l;

    private static final ObjectMapper MAPPER = new ObjectMapper(
            new JsonFactory());
    static {
        MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        // TODO indent json output does not work, is important for readability
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    /**
     * PUTs (writes) this bundle to disk.
     * 
     * @param bundleStr
     *            a {@link BundleDef} as a String
     * @return the {@link BundleDef}, for convenience
     */
    public static BundleDef putBundle(String bundleStr)
            throws ValidationException {
        try {
            BundleDef b = MAPPER.readValue(bundleStr, BundleDef.class);
            writeBundle(b);
            return b;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    /** Used e.g. to test a pipeline without writing it out */
    public static PipelineDef parsePipeline(String pipelineStr)
            throws JsonParseException, JsonMappingException, IOException {
        return MAPPER.readValue(pipelineStr, PipelineDef.class);
    }

    /**
     * PUTs (writes) this pipeline to disk.
     * 
     * @param bundleStr
     *            a {@link PipelineDef} as a String
     * @return the {@link PipelineDef}, for convenience
     */
    public static PipelineDef putPipeline(String pipelineStr)
            throws ValidationException {
        try {
            PipelineDef pipelineDef = parsePipeline(pipelineStr);
            writePipeline(pipelineDef);
            return pipelineDef;
        } catch (Exception e) {
            throw new ValidationException(e);// TODO validate better
        }
    }

    /**
     * PUTs (writes) this resource to disk.
     * 
     * @param path
     *            the path where to write this resource to
     * @param part
     *            holds the resource file itself
     */
    public static void putResource(String path, Part part)
            throws ValidationException {

        validateArgument(!path.contains(".."), "path cannot contain '..'");
        validateArgument(part.getSize() < MAX_UPLOAD_SIZE,
                "file too large, max allowed " + MAX_UPLOAD_SIZE + " bytes");
        validateArgument(part.getSize() > 0, "file is empty");

        File outFile = new File(RUTA_RESOURCES_PATH, path);
        outFile.getParentFile().mkdirs();
        try {
            FileUtils.copyInputStreamToFile(part.getInputStream(), outFile);
        } catch (IOException e) {
            new ValidationException("could not upload file to '" + path + "'");
        }
    }

    /**
     * Writes a {@link Def} to a file<br/>
     * Note: this should be private, but is used in tests...
     * 
     * @param f
     *            the file to write to
     * @param def
     *            the object to write
     * @throws ValidationException
     */
    public static void write(File f, Object def) throws ValidationException {
        try {
            MAPPER.writeValue(f, def);
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    /** Writes this object as String, using Jackson {@link ObjectMapper} */
    public static String writeAsString(Object obj)
            throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    private static void writeBundle(BundleDef b) throws ValidationException {
        b.validate(b.toString());
        File esFile = new File(BUNDLES_PATH + b.getName() + "_"
                + b.getVersion() + ".json");
        esFile.getParentFile().mkdirs();
        write(esFile, b);
    }

    private static void writePipeline(PipelineDef def)
            throws ValidationException {
        def.validate(def.toString());
        File defFile = new File(PIPELINES_PATH + def.getDomain() + "/"
                + def.getName() + "_" + def.getVersion() + ".json");
        defFile.getParentFile().mkdirs();
        write(defFile, def);
    }

    /**
     * Read a JSON-serialized object from file and parse it back to an object.
     * 
     * @param f
     *            the file to read
     * @param clazz
     *            the class to cast this object into
     * @return the parsed object
     * @throws ValidationException
     *             if the object cannot be found or parsed
     */
    public static <T> T read(File f, Class<T> clazz) throws ValidationException {
        try {
            return MAPPER.readValue(new FileInputStream(f), clazz);
        } catch (UnrecognizedPropertyException upe) {
            String msg = "Unrecognized field \"" + upe.getPropertyName()
                    + "\" in file '" + f.getName() + "',  "
                    + upe.getMessageSuffix();
            throw new ValidationException(msg, upe);
        } catch (JsonMappingException jme) {

            StringBuilder sb = new StringBuilder();
            sb.append("cannot read ");
            jme.getPathReference(sb);
            sb.append(" in " + f.getName() + ": ");
            sb.append(" " + jme.getOriginalMessage());

            throw new ValidationException(sb.toString().replaceAll(
                    "org\\.sherlok\\.mappings\\.\\w+Def\\[", "["), jme);
        } catch (JsonParseException jpe) {
            throw new ValidationException("Could not read "
                    + clazz.getSimpleName() + " '"
                    + f.getName().replaceAll("Def$", "") + "', "
                    + jpe.getMessage());
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    /** @return all {@link BundleDef}s */
    public static Collection<BundleDef> allBundleDefs()
            throws ValidationException {
        List<BundleDef> ret = list();
        File bPath = new File(BUNDLES_PATH);
        validateArgument(bPath.exists(), "bundles directory '" + BUNDLES_PATH
                + "' does not exist (resolves to '" + bPath.getAbsolutePath()
                + "')");
        for (File bf : newArrayList(iterateFiles(bPath,
                new String[] { "json" }, true))) {
            ret.add(read(bf, BundleDef.class));
        }
        return ret;
    }

    /** @return all {@link PipelineDef}s */
    public static Collection<PipelineDef> allPipelineDefs()
            throws ValidationException {
        List<PipelineDef> ret = list();
        File pPath = new File(PIPELINES_PATH);
        validateArgument(
                pPath.exists(),
                "pipelines directory '" + PIPELINES_PATH
                        + "' does not exist (resolves to '"
                        + pPath.getAbsolutePath() + "')");
        for (File bf : newArrayList(iterateFiles(pPath,
                new String[] { "json" }, true))) {
            ret.add(FileBased.read(bf, PipelineDef.class));
        }
        return ret;
    }

    /** @return paths to all resources */
    public static Collection<String> allResources() throws ValidationException {
        try {
            List<String> resources = list();
            File dir = new File(RUTA_RESOURCES_PATH);
            validateArgument(dir.exists(), "resources directory '"
                    + RUTA_RESOURCES_PATH + "' does not exist (resolves to '"
                    + dir.getAbsolutePath() + "')");
            Iterator<File> fit = FileUtils.iterateFiles(dir, null, true);
            while (fit.hasNext()) {
                File f = fit.next();
                if (!f.getName().startsWith(".")) {
                    String relativePath = Paths.get(dir.getAbsolutePath())
                            .relativize(Paths.get(f.getAbsolutePath()))
                            .toString();
                    // + "/" + f.getName();
                    if (!relativePath.startsWith(".")) {
                        resources.add(relativePath);
                    }
                }
            }
            return resources;
        } catch (Throwable e) { // you never know...
            throw new ValidationException("could not list resources", e);
        }
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

    public static boolean deletePipeline(String pipelineId, String domain)
            throws ValidationException {
        File defFile = new File(PIPELINES_PATH + domain + "/"
                + getName(pipelineId) + "_" + getVersion(pipelineId) + ".json");
        if (!defFile.exists())
            throw new ValidationException("Cannot delete pipeline '"
                    + pipelineId + "', since it does not exist");
        return defFile.delete();
    }

    public static void deleteResource(String path) throws ValidationException {
        File file = new File(RUTA_RESOURCES_PATH, path);
        validateArgument(file.exists(), "could not find file '" + path + "'");
        validateArgument(file.delete(), "could not delete file '" + path + "'");
    }

    /** @return this resource's {@link File} */
    static File getResource(String path) throws ValidationException {
        File file = new File(RUTA_RESOURCES_PATH, path);
        validateArgument(file.exists(), "could not find file '" + path + "'");
        return file;
    }

    /** Util to read and rewrite all {@link Def}s */
    /*-
    public static void main(String[] args) throws ValidationException {
        Controller controller = new Controller().load();
        for (BundleDef b : controller.listBundles())
            writeBundle(b);
        for (PipelineDef p : controller.listPipelines())
            writePipeline(p);
    }*/
}
