package org.sherlok;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sherlok.mappings.PipelineDef.createId;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDotUnderscore;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumUnderscore;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.delete;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.setPort;

import java.io.File;
import java.util.List;

import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.Create;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

import spark.Request;
import spark.Response;
import spark.ResponseTransformerRoute;
import spark.Route;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Main Sherlok server
 * 
 * @author renaud@apache.org
 */
public class SherlokServer {
    private static Logger LOG = getLogger(SherlokServer.class);

    private static final int DEFAULT_PORT = 9600;
    private static final String PUBLIC = "public";

    private final static Resolver resolver = new Resolver();

    private static final String PIPELINES = "pipelines";
    private static final String ENGINES = "engines";
    private static final String BUNDLES = "bundles";
    private static final String TYPES = "types";

    /** Files allowed in {@link #PUBLIC} folder (to avoid collision with API) */
    private static final List<String> PUBLIC_WHITELIST = //
    Create.list(".DS_Store", "index.html");

    public static final int STATUS_OK = 200;
    public static final int STATUS_INVALID = 400;
    public static final int STATUS_SERVER_ERROR = 500;
    public static final String JSON = "application/json";

    protected static Object annotate_pipeline(Request request, Response response) {
        String pipelineName = request.params(":pipelineName");
        String version = request.queryParams("version");
        String text = request.queryParams("text");
        try {
            checkNotNull(pipelineName,
                    "'pipeline' request parameter should not be null");
            checkOnlyAlphanumUnderscore(pipelineName);
            checkNotNull(text, "'text' request parameter should not be null");
        } catch (Exception e) {
            response.status(STATUS_INVALID);
            return e.getMessage();
        }

        try {
            response.type("application/json");
            return resolver.resolve(pipelineName, version).annotate(text);
        } catch (Exception e) {
            LOG.error("could not annotate '" + text + "'", e);
            response.status(STATUS_INVALID);
            return e;// TODO proper error handling
        }
    }

    static void init(int port) {

        setPort(port); // TODO get from config
        // TODO setIpAddress("");

        // Static files. E.g. public/css/style.css is made available as
        // http://{host}:{port}/css/style.css
        File external = new File(PUBLIC);
        checkArgument(external.exists(), "Folder for static files '" + PUBLIC
                + "' (resolves to '" + external.getAbsolutePath()
                + "') does not exist");
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
                                    + PUBLIC
                                    + "' must start with an underscore (to avoid collision with the REST API)");
                }
            }
        }
        externalStaticFileLocation(PUBLIC);

        // ANNOTATE
        // ////////////////////////////////////////////////////////////////////////////
        get(new Route("/annotate/:pipelineName", "application/json") {
            @Override
            public Object handle(Request request, Response response) {
                return annotate_pipeline(request, response);
            }
        });
        post(new Route("/annotate/:pipelineName", "application/json") {
            @Override
            public Object handle(Request request, Response response) {
                return annotate_pipeline(request, response);
            }
        });

        // PIPELINES
        // ////////////////////////////////////////////////////////////////////////////
        get(new JsonRoute("/" + PIPELINES) { // LIST
            @Override
            public Object handle(Request request, Response response) {
                try {
                    response.type(JSON);
                    return resolver.getStore().listPipelines();

                } catch (Exception e) {// this error should not happen
                    LOG.error("could not LIST pipelines", e);
                    response.status(STATUS_SERVER_ERROR);
                    return e;
                }
            }
        });
        get(new JsonRoute("/" + PIPELINES + "/:pipelineName/:version") { // GET
            @Override
            public Object handle(Request request, Response response) {
                String pipelineName = request.params(":pipelineName");
                String version = request.params(":version");
                response.type(JSON);
                try {
                    String pId = check(pipelineName, version);
                    PipelineDef pDef = resolver.getStore().getPipelineDef(pId);
                    if (pDef != null) {
                        return pDef;
                    } else
                        throw new ValidationException("no pipeline with id '"
                                + pId + "' found");
                } catch (ValidationException ve) {
                    LOG.info("could not GET pipeline '" + pipelineName + "'",
                            ve);
                    response.status(STATUS_INVALID);
                    return ve.toJson();
                } catch (Exception e) {
                    LOG.error("could not GET pipeline '" + pipelineName + "'",
                            e);
                    response.status(STATUS_SERVER_ERROR);
                    return e;
                }
            }
        });
        put(new Route("/" + PIPELINES, "application/json") { // PUT
            @Override
            public Object handle(Request request, Response response) {
                try {
                    String newPipelineId = resolver.getStore().putPipeline(
                            request.body());
                    resolver.removeFromCache(newPipelineId);
                    response.status(STATUS_OK);
                    return newPipelineId;
                } catch (ValidationException ve) {
                    LOG.info("could not PUT pipeline '" + request.body() + "'",
                            ve);
                    response.status(STATUS_INVALID);
                    response.type(JSON);
                    return ve.toJson();
                } catch (Exception e) {
                    LOG.error("could not put '" + request.body(), e);
                    response.status(STATUS_SERVER_ERROR);
                    return e;
                }
            }
        });
        delete(new JsonRoute("/" + PIPELINES + "/:pipelineName/:version") { // DELETE
            @Override
            public Object handle(Request request, Response response) {
                String pipelineName = request.params(":pipelineName");
                String version = request.params(":version");
                try {
                    String pId = check(pipelineName, version);
                    resolver.getStore().deletePipelineDef(pId);
                    resolver.removeFromCache(pId);
                    return "";
                } catch (ValidationException ve) {
                    LOG.info(
                            "could not DELETE pipeline '" + pipelineName + "'",
                            ve);
                    response.status(STATUS_INVALID);
                    response.type(JSON);
                    return ve.toJson();
                } catch (Exception e) {
                    LOG.error("could not delete '" + pipelineName, e);
                    response.status(STATUS_SERVER_ERROR);
                    return e;
                }
            }
        });
    }

    protected static String check(String pipelineName, String version) {
        checkNotNull(pipelineName, "'pipeline' should not be null");
        checkNotNull(version, "'version' should not be null");
        checkOnlyAlphanumUnderscore(pipelineName);
        checkOnlyAlphanumDotUnderscore(version);
        return createId(pipelineName, version);
    }

    public static void main(String[] args) {
        init(DEFAULT_PORT);
    }

    public static abstract class JsonRoute extends ResponseTransformerRoute {

        protected JsonRoute(String path) {
            super(path);
        }

        @Override
        public String render(Object o) {
            try {
                return FileBased.MAPPER.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);// FIXME
            }
        }
    }
}
