package org.sherlok;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sherlok.CheckThat.checkOnlyAlphanumUnderscore;
import static org.sherlok.CheckThat.isOnlyAlphanumDotUnderscore;
import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setPort;

import org.slf4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Main Sherlok server
 * 
 * @author renaud@apache.org
 */
public class SherlokServer {
    private static Logger LOG = getLogger(SherlokServer.class);

    private static final int DEFAULT_PORT = 9600;

    private final static Resolver resolver = new Resolver();

    protected static Object annotate_pipeline(Request request, Response response) {
        String pipeline = request.params(":pipeline");
        String version = request.queryParams("version");
        String text = request.queryParams("text");
        try {
            checkNotNull(pipeline,
                    "'pipeline' request parameter should not be null");
            checkOnlyAlphanumUnderscore(pipeline);
            checkNotNull(text, "'text' request parameter should not be null");
        } catch (Exception e) {
            response.status(412);
            return e.getMessage();
        }

        try {
            response.type("application/json");
            return resolver.resolve(pipeline, version).annotate(text);
        } catch (Exception e) {
            LOG.error("could not annotate '" + text + "'", e);
            response.status(412);
            return e;// TODO proper error handling
        }
    }

    static void init() {

        setPort(DEFAULT_PORT);
        // TODO setIpAddress("");

        // Static files. E.g. public/css/style.css is made available as
        // http://{host}:{port}/css/style.css
        externalStaticFileLocation("public");

        get(new Route("/annotate/:pipeline", "application/json") {
            @Override
            public Object handle(Request request, Response response) {
                return annotate_pipeline(request, response);
            }
        });
        post(new Route("/annotate/:pipeline", "application/json") {
            @Override
            public Object handle(Request request, Response response) {
                return annotate_pipeline(request, response);
            }
        });
    }

    public static void main(String[] args) {
        init();
    }
}
