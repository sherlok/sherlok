package org.sherlok;

import static org.slf4j.LoggerFactory.getLogger;
import static spark.Spark.*;

import org.slf4j.Logger;

import spark.*;

/**
 * Main Sherlok server
 * 
 * @author renaud@apache.org
 */
public class SherlokServer {
    private static Logger LOG = getLogger(SherlokServer.class);

    private final static Resolver2 resolver = new Resolver2();

    protected static Object annotate_pipeline(Request request, Response response) {
        String pipeline = request.params(":pipeline");
        String version = request.queryParams("version");
        String text = request.queryParams("text");

        try {
            response.type("application/json");
            return resolver.resolve(pipeline, version).annotate(text);
        } catch (Exception e) {
            LOG.error("could not annotate '" + text + "'", e);
            return e;// TODO proper error handling
        }
    }

    static void init() {

        setPort(9600);

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
