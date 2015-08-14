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
import static com.google.common.io.Files.createTempDir;
import static java.lang.System.currentTimeMillis;
import static org.sherlok.mappings.Def.createId;
import static org.sherlok.utils.AetherResolver.LOCAL_REPO_PATH;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDotUnderscore;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateNotNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.delete;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setIpAddress;
import static spark.Spark.setPort;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.sherlok.config.ConfigVariableFactory;
import org.sherlok.config.ConfigVariableFactory.ConfigVariableCleaner;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.JsonAnnotation;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineTest;
import org.sherlok.mappings.SherlokException;
import org.sherlok.utils.LogMessagesCache;
import org.sherlok.utils.SherlokTests;
import org.slf4j.Logger;

import spark.Request;
import spark.Response;
import spark.ResponseTransformerRoute;
import spark.Route;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * REST-endpoint for Sherlok
 * 
 * @author renaud@apache.org
 */
public class SherlokServer {
    private static Logger LOG = getLogger(SherlokServer.class);

    /** Route for annotating */
    public static final String ANNOTATE = "annotate";
    /** Route for testing */
    public static final String TEST = "test";
    /** Route and path for pipelines */
    public static final String PIPELINES = "pipelines";
    /** Route and path for bundles */
    public static final String BUNDLES = "bundles";
    /** Route and path for Ruta resources */
    public static final String RUTA_RESOURCES = "resources";
    /** Route and path for cleaning runtime resources */
    public static final String CLEAN = "clean";
    public static final String REMOTE_RESOURCES = "remote_resources";
    public static final String LOGS = "logs";

    public static final int STATUS_OK = 200;
    public static final int STATUS_INVALID = 400;
    public static final int STATUS_MISSING = 404;
    public static final int STATUS_SERVER_ERROR = 500;
    public static final String STATUS = "status";

    public static final String JSON = "application/json";

    /** Location for temp uploaded files */
    protected static final MultipartConfigElement RUTA_RESOURCES_UPLOAD_CONFIG = new MultipartConfigElement(
            createTempDir().getAbsolutePath());

    // LOGO, see http://www.kammerl.de/ascii/AsciiSignature.php font 'thin'
    private static final String LOGO = "\n,---.|              |         |    \n`---.|---.,---.,---.|    ,---.|__/ \n    ||   ||---'|    |    |   ||  \\ \n`---'`   '`---'`    `---'`---'`   `\n";
    public static final String GIT_COMMIT_ID = getGitCommitId();
    private static final String VERSION = "Sherlok Server        v. "
            + GIT_COMMIT_ID + "\n";
    static { // print at server startup
        System.out.println(LOGO + VERSION);
    }
    private static final long START = System.currentTimeMillis();

    /** Path to public folder */
    private static final String PUBLIC = "public";
    /** Files allowed in {@link #PUBLIC} folder (to avoid collision with API) */
    private static final List<String> PUBLIC_WHITELIST = //
    list(".DS_Store", "index.html");

    /** Called at server startup (main). Registers all {@link Route}s */
    public static void init(int port, String ip, String masterUrl,
            boolean sealed) throws SherlokException {

        // CONFIG
        // ////////////////////////////////////////////////////////////////////////////
        final Controller controller;
        if (masterUrl != null) { // slave
            System.out.println("Starting in SLAVE mode (master url:'"
                    + masterUrl + "'");
            controller = new SlaveController(masterUrl).load();
        } else if (sealed) { // sealed
            controller = new SealedController().load();
        } else { // master
            controller = new Controller().load();
        }
        final PipelineLoader pipelineLoader = new PipelineLoader(controller);

        setPort(port);
        setIpAddress(ip);
        // E.g. public/a/b.txt available as http://{host}:{port}/a/b.txt
        externalStaticFileLocation(PUBLIC);
        validatePluginsNames(PUBLIC);

        // ROUTES: ANNOTATE & TEST
        // ////////////////////////////////////////////////////////////////////////////
        get(new Route("/" + ANNOTATE + "/:name", JSON) {
            @Override
            public Object handle(Request req, Response resp) {
                return annotateRequest(req, resp, pipelineLoader);
            }
        });
        post(new Route("/" + ANNOTATE + "/:name", JSON) {// same, but POST
            @Override
            public Object handle(Request req, Response resp) {
                return annotateRequest(req, resp, pipelineLoader);
            }
        });

        /** Testing only, does not store the pipeline */
        post(new JsonRoute("/" + TEST) {
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    // parse pipeline
                    PipelineDef pipeline = FileBased.parsePipeline(req.body());
                    UimaPipeline uimaPipeline = pipelineLoader.load(pipeline);

                    boolean isPassed = true;
                    Map<Integer, Object> passed = map(), failed = map();

                    for (int i = 0; i < pipeline.getTests().size(); i++) {
                        PipelineTest test = pipeline.getTests().get(i);
                        try {
                            String systemStr = uimaPipeline.annotate(test
                                    .getInput());
                            Map<String, List<JsonAnnotation>> system = SherlokTests
                                    .assertEquals(test.getExpected(),
                                            systemStr, test.getComparison());
                            passed.put(
                                    i,
                                    map("expected", test.getExpected(),
                                            "system", system));
                        } catch (SherlokException e) {
                            isPassed = false;
                            failed.put(i, e.setWhen(test.getInput()));
                        }
                    }
                    if (isPassed) {
                        return map("passed", passed);
                    } else {
                        resp.status(STATUS_INVALID);
                        return map("passed", passed, "failed", failed);
                    }

                } catch (SherlokException se) {
                    return invalid("GET /" + TEST + " pipeline '" + req.body()
                            + "'", se, resp);
                } catch (Exception e) {
                    return error("GET /" + TEST + " pipeline '" + req.body()
                            + "'", e, resp);
                }
            }
        });

        /** Test a stored pipeline */
        get(new JsonRoute("/" + TEST + "/:name") {
            @Override
            public Object handle(Request req, Response resp) {

                String pipelineName = req.params(":name");
                String version = req.queryParams("version");
                resp.type(JSON);

                try { // test
                    checkOnlyAlphanumDotUnderscore(pipelineName,
                            "'pipeline' req parameter");

                    UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                            pipelineName, version);

                    for (PipelineTest test : pipeline.getPipelineDef()
                            .getTests()) {
                        String actual = pipeline.annotate(test.getInput());
                        SherlokTests.assertEquals(test.getExpected(), actual,
                                test.getComparison());
                    }
                    return map("status", "passed");

                } catch (SherlokException se) {
                    return invalid("GET /" + TEST + "/" + pipelineName, se,
                            resp);
                } catch (Exception e) {
                    return error("GET /" + TEST + "/" + pipelineName, e, resp);
                }
            }
        });

        // ROUTES: PIPELINES
        // ////////////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + PIPELINES) { // LIST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    resp.type(JSON);
                    return controller.listPipelines();
                } catch (Exception e) { // should not happen, though
                    return error("LIST pipelines", e, resp);
                }
            }
        });
        get(new JsonRoute("/" + PIPELINES + "/:name/:version") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":name");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    PipelineDef pDef = controller.getPipelineDef(id);
                    if (pDef != null) {
                        resp.type(JSON);
                        return pDef;
                    } else
                        throw new SherlokException("pipeline id not found")
                                .setObject(id);
                } catch (SherlokException ve) {

                    return invalid("GET pipeline '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET pipeline '" + name + "'", e, resp);
                }
            }
        });
        post(new JsonRoute("/" + PIPELINES) { // POST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String newId = controller.putPipeline(req.body());
                    pipelineLoader.removeFromCache(newId);
                    resp.status(STATUS_OK);
                    resp.type(JSON);
                    return map(STATUS, "created", "pipeline_id", newId);
                } catch (SherlokException ve) {
                    return invalid("POST pipeline '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("POST '" + req.body(), e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + PIPELINES + "/:name/:version") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":name");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    controller.deletePipelineDef(id);
                    pipelineLoader.removeFromCache(id);
                    resp.status(STATUS_OK);
                    resp.type(JSON);
                    return map(STATUS, "deleted", "pipeline_id", id);
                } catch (SherlokException ve) {
                    return invalid("DELETE pipeline '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("DELETE '" + name, e, resp);
                }
            }
        });

        // ROUTES: BUNDLES
        // ////////////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + BUNDLES) { // LIST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    resp.type(JSON);
                    return controller.listBundles();
                } catch (Exception e) { // should not happen, though
                    return error("LIST bundle", e, resp);
                }
            }
        });
        get(new JsonRoute("/" + BUNDLES + "/:name/:version") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":name");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    BundleDef b = controller.getBundleDef(id);
                    if (b != null) {
                        resp.type(JSON);
                        return b;
                    } else {
                        throw new SherlokException("bundle id not found")
                                .setObject(id);
                    }
                } catch (SherlokException ve) {
                    return invalid("GET bundle '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET bundle '" + name + "'", e, resp);
                }
            }
        });
        post(new JsonRoute("/" + BUNDLES) { // POST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String newId = controller.putBundle(req.body());
                    resp.status(STATUS_OK);
                    resp.type(JSON);
                    return map(STATUS, "created", "bundle_id", newId);
                } catch (SherlokException ve) {
                    return invalid("POST bundle '" + req.body() + "'", ve, resp);
                } catch (Exception e) {
                    return error("POST '" + req.body(), e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + BUNDLES + "/:name/:version") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":name");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    controller.deleteBundleDef(id);
                    resp.status(STATUS_OK);
                    resp.type(JSON);
                    return map(STATUS, "deleted", "bundle_id", id);
                } catch (SherlokException ve) {
                    return invalid("DELETE bundle '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("DELETE bundle '" + name, e, resp);
                }
            }
        });

        // ROUTES: RUTA RESOURCES
        // ////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + RUTA_RESOURCES) { // LIST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    resp.type(JSON);
                    return controller.listResources();
                } catch (Exception e) { // should not happen, though
                    return error("LIST resources", e, resp);
                }
            }
        });
        get(new Route("/" + RUTA_RESOURCES + "/*") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String path = req.splat()[0];
                try {
                    IOUtils.copy(controller.getResource(path), //
                            resp.raw().getOutputStream());
                    String fileName = path.contains("/") ? path.substring(
                            path.lastIndexOf('/'), path.length()) : path;
                    resp.type("application/octet-stream");
                    resp.header("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"");
                    return "";
                } catch (SherlokException ve) {
                    resp.status(STATUS_MISSING);
                    resp.type(JSON);
                    return ve.toJson();
                } catch (Exception e) {
                    return error("GET resource '" + path + "'", e, resp);
                }
            }
        });
        post(new JsonRoute("/" + RUTA_RESOURCES + "/*") { // POST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String path = req.splat()[0];
                    // for Jetty...
                    req.raw().setAttribute("org.eclipse.multipartConfig",
                            RUTA_RESOURCES_UPLOAD_CONFIG);
                    resp.type(JSON);

                    Part part;
                    try {
                        part = req.raw().getPart("file");
                    } catch (Exception e) {
                        return invalid("POST resource: Invalid part 'file'",
                                new SherlokException(e.getMessage()), resp);
                    }
                    controller.putResource(path, part);
                    resp.status(STATUS_OK);
                    return map(STATUS, "created", "resource_path", path);
                } catch (SherlokException ve) {
                    return invalid("POST resource '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("POST '" + req.body(), e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + RUTA_RESOURCES + "/*") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String path = req.splat()[0];
                    controller.deleteResource(path);
                    resp.status(STATUS_OK);
                    resp.type(JSON);
                    return map(STATUS, "deleted", "resource_path", path);
                } catch (SherlokException ve) {
                    return invalid("DELETE resource '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("DELETE '" + req.body(), e, resp);
                }
            }
        });

        // ROUTES: CLEANING; curl -XDELETE http://localhost:9600/clean/pipelines
        // ////////////////////////////////////////////////////////////////////
        delete(new JsonRoute("/" + CLEAN + "/" + PIPELINES) {// RELOAD PIPELINES
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    pipelineLoader.clearCache();
                    return map("status", PIPELINES + " reloaded");
                } catch (Exception e) {
                    return error(CLEAN + "/" + PIPELINES, e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + CLEAN + "/" + LOCAL_REPO_PATH) { // LOCAL
                                                                    // REPOSITORY
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String clean = req.queryParams("clean");
                    if (clean != null) {
                        pipelineLoader.cleanLocalRepo();
                    }
                    controller.load();
                    pipelineLoader.clearCache();
                    return map("status", LOCAL_REPO_PATH + " reloaded");
                } catch (SherlokException ve) {
                    return invalid(CLEAN + "/" + LOCAL_REPO_PATH, ve, resp);
                } catch (Exception e) {
                    return error(CLEAN + "/" + LOCAL_REPO_PATH, e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + CLEAN + "/" + REMOTE_RESOURCES + "/:type") {
            @Override
            public Object handle(Request req, Response resp) {
                resp.type(JSON);

                String type = req.params(":type");

                ConfigVariableCleaner cleaner = ConfigVariableFactory
                        .cleanerFactory(type);
                if (cleaner == null) {
                    return invalid(CLEAN + "/" + REMOTE_RESOURCES + "/" + type,
                            new SherlokException("unknown type")
                                    .setObject(type), resp);
                }

                if (!cleaner.clean()) {
                    return invalid(CLEAN + "/" + REMOTE_RESOURCES + "/" + type,
                            new SherlokException("failed to clean type")
                                    .setObject(type), resp);
                }
                pipelineLoader.clearCache();
                resp.status(STATUS_OK);
                return map("status", "cleaned");
            }
        });
        delete(new JsonRoute("/" + CLEAN + "/" + REMOTE_RESOURCES) {
            @Override
            public Object handle(Request req, Response resp) {
                resp.type(JSON);

                ConfigVariableCleaner cleaner = ConfigVariableFactory
                        .totalCleanerFactor();

                if (!cleaner.clean()) {
                    return invalid(CLEAN + "/" + REMOTE_RESOURCES,
                            new SherlokException(
                                    "failed to clean some runtime resources"),
                            resp);
                }
                pipelineLoader.clearCache();
                resp.status(STATUS_OK);
                return map("status", "cleaned");
            }
        });

        // ROUTES: LOGS
        // ////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + LOGS) { // LOGS
            @Override
            public Object handle(Request req, Response resp) {
                return map("logs", LogMessagesCache.getLogMessages(),//
                        "sherlok_version", GIT_COMMIT_ID,//
                        "start_time", START);
            }
        });
    }

    protected static Object annotateRequest(Request req, Response resp,
            PipelineLoader pipelineLoader) {
        String pipelineName = req.params(":name");
        String version = getRequestParam(req, "version");
        String text = getRequestParam(req, "text");
        try {
            checkOnlyAlphanumDotUnderscore(pipelineName,
                    "'pipeline' req parameter");
            validateNotNull(text, "'text' request parameter");
            validateArgument(text.length() > 0,
                    "'text' req parameter should not be empty");
        } catch (SherlokException ve) {
            Object inv = invalid("annotate text  '" + text + "'", ve, resp);
            try {
                return FileBased.writeAsString(inv);
            } catch (JsonProcessingException e) {
                return inv;
            }
        }

        // annotate
        try {
            resp.type(JSON);
            resp.header("Access-Control-Allow-Origin", "*");

            long start = currentTimeMillis(); // stats
            UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                    pipelineName, version);
            long resolved = currentTimeMillis(), //
            resolve = resolved - start;
            String json = pipeline.annotate(text);
            long annotate = currentTimeMillis() - resolved;

            // remove last '}' of json, append some stats
            StringBuilder sb = new StringBuilder(json.substring(0,
                    json.length() - 1));
            sb.append(",\n  \"_stats\" : {\n" //
                    + "    \"_pipeline_resolution\": " + resolve
                    + ",\n"
                    + "    \"_annotation\": " + annotate + "\n  }\n}");

            return sb.toString();

        } catch (SherlokException ve) {
            Object inv = invalid("annotate text  '" + text + "'", ve, resp);
            try {
                return FileBased.writeAsString(inv);
            } catch (JsonProcessingException e) {
                return inv;
            }
        } catch (Exception e) {
            return error("annotate text '" + text + "'", e, resp);
        }
    }

    /**
     * Extract `param` from `req`, either from the request itself or from its
     * body (which is expected to be a JSON object), in that order.
     * 
     * @return null if the parameter is not found
     */
    private static String getRequestParam(Request req, String param) {
        String value = req.queryParams(param);

        // If the param is not in the header, try its body
        if (value == null) {
            try {
                value = new JSONObject(req.body()).getString(param);
            } catch (JSONException e) {
                // Value not found/body format is not JSON.
                LOG.warn("Request {} doesn't contain any value for param {}",
                        req.raw(), param);
                // We do nothing about it since this can happen when using GET
                // request; the error should be handled elsewhere in any case.
            }
        }

        return value;
    }

    /** Ensures that no file or directory in pluginFolder can collide with API */
    private static void validatePluginsNames(String pluginFolder)
            throws SherlokException {

        File external = new File(pluginFolder);
        validateArgument(external.exists(),
                "Folder for static files '" + pluginFolder + "' (resolves to '"
                        + external.getAbsolutePath() + "') does not exist");
        for (File plugin : external.listFiles()) {
            if (plugin.isDirectory()) {
                validateArgument(
                        plugin.getName().startsWith("_"),
                        "Plugin '"
                                + plugin.getName()
                                + "' must start with an underscore (to avoid collision with the REST API)");
            } else {
                if (!PUBLIC_WHITELIST.contains(plugin.getName())) {
                    validateArgument(
                            plugin.getName().startsWith("_"),
                            "File  '"
                                    + plugin.getName()
                                    + "' in plugin directory '"
                                    + pluginFolder
                                    + "' must start with an underscore (to avoid collision with the REST API)");
                }
            }
        }
    }

    /** validates name and version @return id */
    private static String check(String name, String version)
            throws SherlokException {
        checkOnlyAlphanumDotUnderscore(name, "'name'");
        checkOnlyAlphanumDotUnderscore(version, "'version'");
        return createId(name, version);
    }

    /** Set {@link Response} as invalid. */
    private static Object invalid(String route, SherlokException se,
            Response resp) {
        se.setRoute(route);
        LOG.info("could not process '" + route + "', " + se.getMessage(), se);
        resp.status(STATUS_INVALID);
        resp.type(JSON);
        return se.toJson();
    }

    /** Set {@link Response} as erroneous. */
    private static Object error(String errorMsg, Exception e, Response resp) {
        LOG.error("could not " + errorMsg, e);
        resp.status(STATUS_SERVER_ERROR);
        return e;
    }

    /** Transformer {@link Route} that returns a JSON response. */
    public static abstract class JsonRoute extends ResponseTransformerRoute {

        protected JsonRoute(String path) {
            super(path);
        }

        @Override
        public String render(Object o) {
            try {
                return FileBased.writeAsString(o);
            } catch (Exception e) {
                throw new RuntimeException(e); // should not happen
            }
        }
    }

    /*- TODO xLATER not working: compressing server output
    // resp.header("Content-Encoding", "gzip");
    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        String outStr = out.toString("UTF-8");
        return outStr;
    }*/

    /** Using git-commit-id-plugin maven plugin */
    private static final String getGitCommitId() {
        try {
            Properties properties = new Properties();
            properties.load(SherlokServer.class.getClassLoader()
                    .getResourceAsStream("git.properties"));

            return properties.get("git.commit.id").toString().substring(0, 10);
        } catch (Exception e) {
            return "";
        }
    }

    public static double getJavaVersion() {
        String version = System.getProperty("java.version"); // e.g. 1.7.0_10
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos + 1);
        return Double.parseDouble(version.substring(0, pos));
    }

    // MAIN stuff (to start Sherlok server)
    // ////////////////////////////////////////////////////////////////////////////
    public static final int DEFAULT_PORT = 9600;
    public static final String DEFAULT_IP = "0.0.0.0";

    /** Configuration arguments from command line */
    static class CliArguments {
        @Parameter(names = "-port", description = "Which port to use.")
        int port = DEFAULT_PORT;
        @Parameter(names = "-address", description = "Which ip address to use.")
        String address = DEFAULT_IP;
        @Parameter(names = "-master-url", description = "Turns slave mode on. Specifies master URL")
        String masterUrl = null;
        @Parameter(names = "--sealed", description = "Turns sealed mode on.")
        boolean sealed = false;
    }

    public static void main(String[] args) throws Exception {
        checkArgument(getJavaVersion() >= 1.7d,
                "Sherlok needs at least Java 1.7, you have " + getJavaVersion());
        CliArguments argParser = new CliArguments();
        new JCommander(argParser, args);
        try {
            init(argParser.port, argParser.address, argParser.masterUrl,
                    argParser.sealed);
        } catch (SherlokException e) {
            System.err.println("fatal error: " + e.toString());
        }
    }
}
