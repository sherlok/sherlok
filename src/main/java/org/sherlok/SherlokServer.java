package org.sherlok;

import static org.sherlok.mappings.Def.createId;
import static org.sherlok.utils.CheckThat.checkArgument;
import static org.sherlok.utils.CheckThat.checkNotNull;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDot;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.delete;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.setPort;

import java.io.File;
import java.util.List;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.Create;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

import spark.Request;
import spark.Response;
import spark.ResponseTransformerRoute;
import spark.Route;

/**
 * REST-endpoint for Sherlok
 * 
 * @author renaud@apache.org
 */
public class SherlokServer {
    private static Logger LOG = getLogger(SherlokServer.class);

    private static final int DEFAULT_PORT = 9600;
    private static final String PUBLIC = "public";

    // http://www.kammerl.de/ascii/AsciiSignature.php font 'thin'
    private static final String LOGO = "\n,---.|              |         |    \n`---.|---.,---.,---.|    ,---.|__/ \n    ||   ||---'|    |    |   ||  \\ \n`---'`   '`---'`    `---'`---'`   `\n\n";
    private static final String VERSION = "Sherlok Server          version 0.1\n";
    static {
        System.out.println(LOGO + VERSION);
    }

    static final String PIPELINES = "pipelines";
    static final String ENGINES = "engines";
    static final String BUNDLES = "bundles";
    static final String TYPES = "types";

    /** Files allowed in {@link #PUBLIC} folder (to avoid collision with API) */
    private static final List<String> PUBLIC_WHITELIST = //
    Create.list(".DS_Store", "index.html");

    public static final int STATUS_OK = 200;
    public static final int STATUS_INVALID = 400;
    public static final int STATUS_SERVER_ERROR = 500;
    public static final String JSON = "application/json";

    protected static Object annotateRequest(Request req, Response resp,
            PipelineLoader pipelineLoader) {
        String pipelineName = req.params(":pipelineName");
        String version = req.queryParams("version");
        String text = req.queryParams("text");
        try {
            checkNotNull(pipelineName,
                    "'pipeline' req parameter should not be null");
            checkOnlyAlphanumDot(pipelineName);
            checkNotNull(text, "'text' req parameter should not be null");
            checkArgument(text.length() > 0,
                    "'text' req parameter should not be empty");
        } catch (ValidationException ve) {
            return invalid("could not annotate text  '" + text + "'", ve, resp);
        }
        // annotate
        try {
            resp.type("application/json");
            return (pipelineLoader.resolvePipeline(pipelineName, version)
                    .annotate(text));
        } catch (ValidationException ve) {
            return invalid("could not annotate text  '" + text + "'", ve, resp);
        } catch (Exception e) {
            return error("could not annotate text '" + text + "'", e, resp);
        }
    }

    static void init(int port) throws ValidationException {

        final Controller controller = new Controller().load();
        final PipelineLoader pipelineLoader = new PipelineLoader(controller);
        // config
        setPort(port);
        // Static files. E.g. public/css/style.css is made available as
        // http://{host}:{port}/css/style.css
        externalStaticFileLocation(PUBLIC);
        // TODO setIpAddress("");
        validatePlugins(PUBLIC);

        // ROUTES: ANNOTATE
        // ////////////////////////////////////////////////////////////////////////////
        get(new Route("/annotate/:pipelineName", "application/json") {
            @Override
            public Object handle(Request req, Response resp) {
                return annotateRequest(req, resp, pipelineLoader);
            }
        });
        post(new Route("/annotate/:pipelineName", "application/json") {
            @Override
            public Object handle(Request req, Response resp) {
                return annotateRequest(req, resp, pipelineLoader);
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
        get(new JsonRoute("/" + PIPELINES + "/:pipelineName/:version") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":pipelineName");
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
        put(new Route("/" + PIPELINES, "application/json") { // PUT
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String newId = controller.putPipeline(req.body());
                    pipelineLoader.removeFromCache(newId);
                    resp.status(STATUS_OK);
                    return newId;
                } catch (ValidationException ve) {
                    return invalid("PUT pipeline '" + req.body() + "'", ve,
                            resp);
                } catch (Exception e) {
                    return error("PUT '" + req.body(), e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + PIPELINES + "/:pipelineName/:version") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":pipelineName");
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

        // ROUTES: ENGINES
        // ////////////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + ENGINES) { // LIST
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    resp.type(JSON);
                    return controller.listEngines();
                } catch (Exception e) {// this error should not happen
                    return error("LIST engines", e, resp);
                }
            }
        });
        get(new JsonRoute("/" + ENGINES + "/:engineName/:version") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":engineName");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    EngineDef pDef = controller.getEngineDef(id);
                    if (pDef != null) {
                        resp.type(JSON);
                        return pDef;
                    } else
                        throw new ValidationException("no engine with id '"
                                + id + "' found");
                } catch (ValidationException ve) {
                    return invalid("GET engine '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("GET engine '" + name + "'", e, resp);
                }
            }
        });
        put(new Route("/" + ENGINES, "application/json") { // PUT
            @Override
            public Object handle(Request req, Response resp) {
                try {
                    String newId = controller.putEngine(req.body());
                    resp.status(STATUS_OK);
                    return newId;
                } catch (ValidationException ve) {
                    return invalid("PUT engine '" + req.body() + "'", ve, resp);
                } catch (Exception e) {
                    return error("PUT '" + req.body(), e, resp);
                }
            }
        });
        delete(new JsonRoute("/" + ENGINES + "/:engineName/:version") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":engineName");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    controller.deleteEngineDef(id);
                    return "";
                } catch (ValidationException ve) {
                    return invalid("DELETE engine '" + name + "'", ve, resp);
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
                    return error("LIST bundles", e, resp);
                }
            }
        });
        get(new JsonRoute("/" + BUNDLES + "/:bundleName/:version") { // GET
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":bundleName");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    BundleDef pDef = controller.getBundleDef(id);
                    if (pDef != null) {
                        resp.type(JSON);
                        return pDef;
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
        put(new Route("/" + BUNDLES, "application/json") { // PUT
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
        delete(new JsonRoute("/" + BUNDLES + "/:bundleName/:version") { // DELETE
            @Override
            public Object handle(Request req, Response resp) {
                String name = req.params(":bundleName");
                String version = req.params(":version");
                try {
                    String id = check(name, version);
                    controller.deleteBundleDef(id);
                    return "";
                } catch (ValidationException ve) {
                    return invalid("DELETE bundle '" + name + "'", ve, resp);
                } catch (Exception e) {
                    return error("DELETE '" + name, e, resp);
                }
            }
        });
    }

    /** Ensures that no file or directory in pluginFolder can collide with API */
    private static void validatePlugins(String pluginFolder)
            throws ValidationException {

        File external = new File(pluginFolder);
        checkArgument(external.exists(),
                "Folder for static files '" + pluginFolder + "' (resolves to '"
                        + external.getAbsolutePath() + "') does not exist");
        for (File plugin : external.listFiles()) {
            if (plugin.isDirectory()) {
                checkArgument(
                        plugin.getName().startsWith("_"),
                        "Plugin '"
                                + plugin.getName()
                                + "' must start with an underscore (to avoid collision with the REST API)");
            } else {
                if (!PUBLIC_WHITELIST.contains(plugin.getName())) {
                    checkArgument(
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
        checkNotNull(name, "'name' should not be null");
        checkNotNull(version, "'version' should not be null");
        checkOnlyAlphanumDot(name);
        checkOnlyAlphanumDot(version);
        return createId(name, version);
    }

    private static Object invalid(String errorMsg, ValidationException ve,
            Response resp) {
        LOG.info("could not " + errorMsg, ve);
        resp.status(STATUS_INVALID);
        resp.type(JSON);
        return ve.toJson();
    }

    private static Object error(String errorMsg, Exception e, Response resp) {
        LOG.error("could not " + errorMsg, e);
        resp.status(STATUS_SERVER_ERROR);
        return e;
    }

    /** Transformer that returns a JSON resp */
    public static abstract class JsonRoute extends ResponseTransformerRoute {

        protected JsonRoute(String path) {
            super(path);
        }

        @Override
        public String render(Object o) {
            try {
                return FileBased.writeAsString(o);
            } catch (Exception e) {
                throw new RuntimeException(e);// should not happen
            }
        }
    }

    /*- TODO LATER not working
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

    public static void main(String[] args) throws Exception {
        init(DEFAULT_PORT);
    }
}
