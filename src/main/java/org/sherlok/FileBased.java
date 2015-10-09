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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateId;
import static org.sherlok.utils.CheckThat.validatePath;
import static org.sherlok.utils.Create.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.Def;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.SherlokException;

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
public class FileBased {

    /* package */static final String CONFIG_DIR_PATH = "config/";
    public static final String RUNTIME_DIR_PATH = "runtime/";

    public static final String BUNDLES_PATH = CONFIG_DIR_PATH
            + SherlokServer.BUNDLES + "/";
    public static final String PIPELINES_PATH = CONFIG_DIR_PATH
            + SherlokServer.PIPELINES + "/";
    public static final String RUTA_RESOURCES_PATH = CONFIG_DIR_PATH
            + SherlokServer.RUTA_RESOURCES + "/";
    public static final String PIPELINE_CACHE_PATH = RUNTIME_DIR_PATH
            + "pipelines/";
    public static final String ENGINE_CACHE_PATH = RUNTIME_DIR_PATH
            + "engines/";

    /** 100Mb, in bytes */
    static final long MAX_UPLOAD_SIZE = 100 * 1000000l;

    /** JSON serializer for mapped objects. */
    private static final ObjectMapper MAPPER = new ObjectMapper(
            new JsonFactory());
    static {
        MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        // important for EngineDef.properties
        MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    /** path convention: BUNDLE_PATH{name}_{version}.json */
    private static String getBundlePath(String name, String version) {
        return BUNDLES_PATH + name + "_" + version + ".json";
    }

    /** path convention: PIPELINES_PATH{domain}/{name}_{version}.json */
    private static String getPipelinePath(String domain, String name,
            String version) {
        return PIPELINES_PATH + domain + "/" + name + "_" + version + ".json";
    }

    /**
     * PUTs (writes) this bundle to disk.
     * 
     * @param bundleStr
     *            a {@link BundleDef} as a String
     * @return the {@link BundleDef}, for convenience
     */
    public static BundleDef putBundle(String bundleStr) throws SherlokException {
        try {
            BundleDef b = MAPPER.readValue(bundleStr, BundleDef.class);
            writeBundle(b);
            return b;
        } catch (Exception e) {
            throw new SherlokException(e.getMessage());// FIXME validate better
        }
    }

    /**
     * PUTs (writes) this pipeline to disk. First, validates it.
     * 
     * @param pipelineStr
     *            a {@link PipelineDef} as a String
     * @param engineIds
     *            the engine ids of the controller, used to
     *            {@link PipelineDef#validateEngines(Set)}.
     * @return the {@link PipelineDef}, for convenience
     */
    public static PipelineDef putPipeline(String pipelineStr,
            Set<String> engineIds) throws SherlokException {
        try {
            PipelineDef p = parsePipeline(pipelineStr);
            p.validateEngines(engineIds);
            writePipeline(p);
            return p;
        } catch (Exception e) {
            throw new SherlokException(e.getMessage());// FIXME validate better
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
            throws SherlokException {

        validateArgument(!path.contains(".."), "path cannot contain '..'");
        validateArgument(part.getSize() < MAX_UPLOAD_SIZE,
                "file too large, max allowed " + MAX_UPLOAD_SIZE + " bytes");
        validateArgument(part.getSize() > 0, "file is empty");

        File outFile = new File(RUTA_RESOURCES_PATH, path);
        outFile.getParentFile().mkdirs();
        try {
            FileUtils.copyInputStreamToFile(part.getInputStream(), outFile);
        } catch (IOException e) {
            new SherlokException("could not upload file to", path);
        }
    }

    /** Used e.g. to test a pipeline without writing it out */
    public static PipelineDef parsePipeline(String pipelineStr)
            throws JsonParseException, JsonMappingException, IOException {
        return MAPPER.readValue(pipelineStr, PipelineDef.class);
    }

    /**
     * Writes a {@link Def} to a file<br/>
     * Note: this should be private, but is used in tests...
     * 
     * @param f
     *            the file to write to
     * @param def
     *            the object to write
     */
    public static void write(File f, Def def) throws SherlokException {
        try {
            MAPPER.writeValue(f, def);
        } catch (Exception e) {
            throw new SherlokException("could not write "
                    + def.getClass().getSimpleName(), def.toString())
                    .setDetails(e.getStackTrace());
        }
    }

    /** Writes this object as String, using Jackson's {@link ObjectMapper} */
    public static String writeAsString(Object obj)
            throws JsonProcessingException {
        return MAPPER.writeValueAsString(obj);
    }

    private static void writeBundle(BundleDef b) throws SherlokException {
        b.validate(b.toString());
        File bFile = new File(getBundlePath(b.getName(), b.getVersion()));
        bFile.getParentFile().mkdirs();
        write(bFile, b);
    }

    private static void writePipeline(PipelineDef p) throws SherlokException {
        p.validate(p.toString());
        File defFile = new File(getPipelinePath(p.getDomain(), p.getName(),
                p.getVersion()));
        defFile.getParentFile().mkdirs();
        write(defFile, p);
    }

    /**
     * Read a JSON-serialized object from file and parse it back to an object.
     * Performs extensive error-catching to provide useful information in case
     * of error.
     * 
     * @param f
     *            the file to read
     * @param clazz
     *            the class to cast this object into
     * @return the parsed object
     * @throws SherlokException
     *             if the object cannot be found or parsed
     */
    public static <T> T read(File f, Class<T> clazz) throws SherlokException {
        try {
            return MAPPER.readValue(new FileInputStream(f), clazz);

        } catch (FileNotFoundException io) {
            throw new SherlokException()
                    .setMessage(
                            clazz.getSimpleName().replaceAll("Def$", "")
                                    + " does not exist.")
                    .setObject(f.getName()).setDetails(io.toString());

        } catch (UnrecognizedPropertyException upe) {
            throw new SherlokException()
                    .setMessage(
                            "Unrecognized field \"" + upe.getPropertyName()
                                    + "\"")
                    .setObject(
                            clazz.getSimpleName().replaceAll("Def$", "") + " "
                                    + f.getName())
                    .setDetails(upe.getMessageSuffix());

        } catch (JsonMappingException jme) {

            StringBuilder sb = new StringBuilder();
            sb.append("cannot read ");
            jme.getPathReference(sb);
            sb.append(" in '" + f.getName() + "': ");
            sb.append(" " + jme.getOriginalMessage());

            throw new SherlokException().setMessage(sb.toString().replaceAll(
                    "org\\.sherlok\\.mappings\\.\\w+Def\\[", "["));

        } catch (JsonParseException jpe) {

            throw new SherlokException()
                    .setMessage(
                            "Could not parse JSON object of type "
                                    + clazz.getSimpleName())
                    .setObject(f.getName().replaceAll("Def$", ""))
                    .setDetails(jpe.getMessage());

        } catch (Exception e) {
            throw new SherlokException().setMessage(e.getMessage())
                    .setObject(f.getName().replaceAll("Def$", ""))
                    .setDetails(e.getStackTrace());
        }
    }

    /** @return all {@link BundleDef}s */
    public static Collection<BundleDef> allBundleDefs() throws SherlokException {
        List<BundleDef> ret = list();
        File bPath = new File(BUNDLES_PATH);
        validateArgument(bPath.exists(), "bundles directory does not exist",
                BUNDLES_PATH, "make sure path exists (currently resolves to '"
                        + bPath.getAbsolutePath() + "')");
        for (File bf : newArrayList(iterateFiles(bPath,
                new String[] { "json" }, true))) {
            ret.add(read(bf, BundleDef.class));
        }
        return ret;
    }

    /** @return all {@link PipelineDef}s */
    public static Collection<PipelineDef> allPipelineDefs()
            throws SherlokException {
        List<PipelineDef> ret = list();
        File pPath = new File(PIPELINES_PATH);
        validateArgument(
                pPath.exists(),
                "pipelines directory does not exist",
                PIPELINES_PATH,
                "make sure directory exists  (currently resolves to '"
                        + pPath.getAbsolutePath() + "')");
        for (File bf : newArrayList(iterateFiles(pPath,
                new String[] { "json" }, true))) {
            ret.add(FileBased.read(bf, PipelineDef.class));
        }
        return ret;
    }

    /** @return a list of the paths of all resources */
    public static Collection<String> allResources() throws SherlokException {
        try {
            List<String> resources = list();
            File dir = new File(RUTA_RESOURCES_PATH);
            validateArgument(dir.exists(), "resources directory '"
                    + RUTA_RESOURCES_PATH + "' does not exist (resolves to '"
                    + dir.getAbsolutePath() + "')");
            Iterator<File> fit = FileUtils.iterateFiles(dir, null, true);
            while (fit.hasNext()) {
                File f = fit.next();
                if (!f.getName().startsWith(".")) { // filtering hidden files
                    String relativePath = Paths.get(dir.getAbsolutePath())
                            .relativize(Paths.get(f.getAbsolutePath()))
                            .toString();
                    if (!relativePath.startsWith(".")) {
                        resources.add(relativePath);
                    }
                }
            }
            return resources;
        } catch (Throwable e) { // you never know...
            throw new SherlokException("could not list resources").setDetails(e
                    .getStackTrace());
        }
    }

    public static boolean deleteBundle(String bundleId) throws SherlokException {
        validateId(bundleId, "BundleId not valid: ");
        File defFile = new File(getBundlePath(getName(bundleId),
                getVersion(bundleId)));
        if (!defFile.exists())
            throw new SherlokException(
                    "Cannot delete bundle, since it does not exist", bundleId);
        return defFile.delete();
    }

    public static boolean deletePipeline(String pipelineId, String domain)
            throws SherlokException {
        validateId(pipelineId, "PipelineId not valid: ");
        File defFile = new File(getPipelinePath(domain, getName(pipelineId),
                getVersion(pipelineId)));
        if (!defFile.exists())
            throw new SherlokException(
                    "Cannot delete pipeline, since it does not exist",
                    pipelineId);
        return defFile.delete();
    }

    public static void deleteResource(String path) throws SherlokException {
        validatePath(path);
        File file = new File(RUTA_RESOURCES_PATH, path);
        validateArgument(file.exists(), "could not find file '" + path + "'");
        validateArgument(file.delete(), "could not delete file '" + path + "'");
    }

    /** @return this resource's {@link File} */
    static InputStream getResource(String path) throws SherlokException {
        validatePath(path);
        File file = new File(RUTA_RESOURCES_PATH, path);
        validateArgument(file.exists(), "could not find file '" + path + "'");
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new SherlokException("file not found", path);
        }
    }

    /**
     * Compute the relative path from a give absolute path and the resource
     * directory.
     */
    public static String getRelativePathToResources(File absolutePath) {
        checkArgument(absolutePath.isAbsolute());

        Path absolute = absolutePath.toPath();
        Path base = new File(RUTA_RESOURCES_PATH).getAbsoluteFile().toPath();
        return base.relativize(absolute).toString();
    }

    /*-
    // Util to read and rewrite all {@link Def}s 
    public static void main(String[] args) throws SherlokException {
        Controller controller = new Controller().load();
        for (BundleDef b : controller.listBundles())
            writeBundle(b);
        for (PipelineDef p : controller.listPipelines())
            writePipeline(p);
    }*/
}
