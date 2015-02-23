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

import static com.google.common.io.Files.createTempDir;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.currentTimeMillis;
import static org.sherlok.mappings.Def.createId;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDot;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateNotNull;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.delete;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.setIpAddress;
import static spark.Spark.setPort;

import java.io.File;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineTest;
import org.sherlok.utils.Create;
import org.sherlok.utils.SherlokTests;
import org.sherlok.utils.ValidationException;
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
    static final String ANNOTATE = "annotate";
    /** Route for testing */
    static final String TEST = "test";
    /** Route and path for pipelines */
    static final String PIPELINES = "pipelines";
    /** Route and path for bundles */
    static final String BUNDLES = "bundles";
    /** Route and path for Ruta resources */
    static final String RUTA_RESOURCES = "resources";
    /** Location for the temporary uploaded files */
    protected static final MultipartConfigElement RUTA_RESOURCES_UPLOAD_CONFIG = new MultipartConfigElement(
            createTempDir().getAbsolutePath());

    static final String TEST_ONLY = "testonly";

    public static final int STATUS_OK = 200;
    public static final int STATUS_INVALID = 400;
    public static final int STATUS_MISSING = 404;
    public static final int STATUS_SERVER_ERROR = 500;
    public static final String JSON = "application/json";

    // LOGO http://www.kammerl.de/ascii/AsciiSignature.php font 'thin'
    private static final String LOGO = "\n,---.|              |         |    \n`---.|---.,---.,---.|    ,---.|__/ \n    ||   ||---'|    |    |   ||  \\ \n`---'`   '`---'`    `---'`---'`   `\n\n";
    private static final String VERSION = "Sherlok Server          version 0.1\n";
    static {
        System.out.println(LOGO + VERSION);
    }

    /** Path to public folder */
    private static final String PUBLIC = "public";
    /** Files allowed in {@link #PUBLIC} folder (to avoid collision with API) */
    private static final List<String> PUBLIC_WHITELIST = //
    Create.list(".DS_Store", "index.html");

    public static final String TEST_TEXT = "Using this calibration procedure, we find that mature granule cells (doublecortin-) contain approximately 40 microm, and newborn granule cells (doublecortin+) contain 0-20 microm calbindin-D28k. U.S. employers added the largest number of workers in nearly three years in October and wages increased, which could bring the Federal Reserve closer to raising interest rates. Nonfarm payrolls surged by 321,000 last month, the most since January of 2012, the Labor Department said on Friday. The unemployment rate held steady at a six-year low of 5.8 percent. Data for September and October were revised to show 44,000 more jobs created than previously reported. Economists polled by Reuters had forecast payrolls increasing by only 230,000 last month. November marked the 10th straight month that job growth has exceeded 200,000, the longest stretch since 1994, and further confirmed the economy is weathering slowdowns in China and the euro zone, as well as a recession in Japan.";

    /** Called at server startup. Registers all {@link Route}s */
    public static PipelineLoader init(int port, String ip)
            throws ValidationException {

        final Controller controller = new Controller().load();
        final PipelineLoader pipelineLoader = new PipelineLoader(controller);

        // config
        setPort(port);
        setIpAddress(ip);
        // Static files. E.g. public/css/style.css available as
        // http://{host}:{port}/css/style.css
        externalStaticFileLocation(PUBLIC);
        validatePluginsNames(PUBLIC);
        
        try {// prevent error "This must be done before route mapping has begun"
            Thread.sleep(20);
        } catch (InterruptedException e) {// nope
        }

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

        get(new Route("/" + TEST + "/:name", JSON) {
            @Override
            public Object handle(Request req, Response resp) {

                String pipelineName = req.params(":name");
                String version = req.queryParams("version");
                resp.type(JSON);

                try { // test
                    checkOnlyAlphanumDot(pipelineName,
                            "'pipeline' req parameter");

                    UimaPipeline pipeline = pipelineLoader.resolvePipeline(
                            pipelineName, version);

                    for (PipelineTest test : pipeline.getPipelineDef()
                            .getTests()) {
                        String systemOut = pipeline.annotate(test.getIn());
                        SherlokTests.assertEquals(test.getOut(), systemOut,
                                test.getComparison());
                    }
                } catch (ValidationException ve) {
                    return invalid("test failed: ", ve, resp);
                } catch (Exception e) {
                    return error("test failed: ", e, resp);
                }

                return "All test passed :-)";
            }
        });

        // ROUTES: UTILS
        // ////////////////////////////////////////////////////////////////////////////
        get(new Route("/reload") {
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    controller.load();
                    pipelineLoader.clearCache();
                    return "OK";
                } catch (ValidationException ve) {
                    return invalid("reload", ve, resp);
                } catch (Exception e) {
                    return error("reload", e, resp);
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
                } catch (Exception e) {// this error should not happen
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
                        throw new ValidationException("no pipeline with id '"
                                + id + "' found");
                } catch (ValidationException ve) {
                    return invalid("GET pipeline '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET pipeline '" + name + "'", e, resp);
                }
            }
        });
        put(new Route("/" + PIPELINES, JSON) { // PUT
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    boolean testOnly = parseBoolean(req.queryParams(TEST_ONLY));
                    if (testOnly) {
                        // load
                        PipelineDef parsedPipeline = FileBased
                                .parsePipeline(req.body());
                        UimaPipeline uimaPipeline = pipelineLoader
                                .load(parsedPipeline);
                        // test
                        String test = TEST_TEXT;
                        if (!parsedPipeline.getTests().isEmpty()) {
                            test = parsedPipeline.getTests().get(0).getIn();
                        }
                        uimaPipeline.annotate(test);
                        return "OK";
                    } else {
                        String newId = controller.putPipeline(req.body());
                        pipelineLoader.removeFromCache(newId);
                        resp.status(STATUS_OK);
                        return newId;
                    }
                } catch (ValidationException ve) {
                    return invalid("PUT pipeline '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("PUT '" + req.body(), e, resp);
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
                    return "";
                } catch (ValidationException ve) {
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
                } catch (Exception e) {// this error should not happen
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
                    } else
                        throw new ValidationException("no bundle with id '"
                                + id + "' found");
                } catch (ValidationException ve) {
                    return invalid("GET bundle '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET bundle '" + name + "'", e, resp);
                }
            }
        });
        put(new Route("/" + BUNDLES, JSON) { // PUT
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String newId = controller.putBundle(req.body());
                    resp.status(STATUS_OK);
                    return newId;
                } catch (ValidationException ve) {
                    return invalid("PUT bundle '" + req.body() + "'", ve, resp);
                } catch (Exception e) {
                    return error("PUT '" + req.body(), e, resp);
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
                    return "";
                } catch (ValidationException ve) {
                    return invalid("DELETE bundle '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("DELETE bundle '" + name, e, resp);
                }
            }
        });

        // ROUTES: RUTA RESOURCES
        // ////////////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + RUTA_RESOURCES) { // LIST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    resp.type(JSON);
                    return controller.listResources();
                } catch (Exception e) {// this error should not happen
                    return error("LIST resources", e, resp);
                }
            }
        });
        get(new Route("/" + RUTA_RESOURCES + "/:path") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String path = req.params(":path");
                try {
                    File f = FileBased.getResource(path);
                    resp.type("application/octet-stream");
                    resp.header("Content-Disposition",
                            "attachment; filename=\"" + f.getName() + "\"");
                    return f;
                } catch (ValidationException ve) {
                    return invalid("GET resource '" + path + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET resource '" + path + "'", e, resp);
                }
            }
        });
        put(new Route("/" + RUTA_RESOURCES + "/:path", JSON) { // PUT
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String path = req.params(":path");
                    req.raw().setAttribute("org.eclipse.multipartConfig",
                            RUTA_RESOURCES_UPLOAD_CONFIG);
                    Part part = req.raw().getPart("file");
                    FileBased.putResource(path, part);
                    resp.status(STATUS_OK);
                    return "OK";
                } catch (ValidationException ve) {
                    return invalid("PUT resource '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("PUT '" + req.body(), e, resp);
                }
            }
        });
        return pipelineLoader;
    }

    protected static Object annotateRequest(Request req, Response resp,
            PipelineLoader pipelineLoader) {
        String pipelineName = req.params(":name");
        String version = req.queryParams("version");
        String text = req.queryParams("text");
        try {
            checkOnlyAlphanumDot(pipelineName, "'pipeline' req parameter");
            validateNotNull(text, "'text' req parameter should not be null");
            validateArgument(text.length() > 0,
                    "'text' req parameter should not be empty");
        } catch (ValidationException ve) {
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
            sb.append(",\n  \"stats\" : {\n" //
                    + "    \"pipeline_resolution\": " + resolve
                    + ",\n"
                    + "    \"annotation\": " + annotate + "\n  }\n}");

            return sb.toString();

        } catch (ValidationException ve) {
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

    /** Ensures that no file or directory in pluginFolder can collide with API */
    private static void validatePluginsNames(String pluginFolder)
            throws ValidationException {

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

    private static String check(String name, String version)
            throws ValidationException {
        checkOnlyAlphanumDot(name, "'name'");
        checkOnlyAlphanumDot(version, "'version'");
        return createId(name, version);
    }

    /** Set {@link Response} as invalid. */
    private static Object invalid(String errorMsg, ValidationException ve,
            Response resp) {
        LOG.info("could not " + errorMsg + " " + ve.getMessage(), ve);
        resp.status(STATUS_INVALID);
        resp.type(JSON);
        return ve.toJson();
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
    }

    public static void main(String[] args) throws Exception {
        CliArguments argParser = new CliArguments();
        new JCommander(argParser, args);
        init(argParser.port, argParser.address);
    }
}
