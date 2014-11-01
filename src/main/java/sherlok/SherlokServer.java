package sherlok;

import static spark.Spark.*;

import org.apache.uima.UIMAException;
import org.xml.sax.SAXException;

import spark.*;

public class SherlokServer {

    static void init() {

        final Resolver resolver = new Resolver();

        setPort(9876);

        // Static files. E.g. public/css/style.css is made available as
        // http://{host}:{port}/css/style.css
        externalStaticFileLocation("public");

        post(new Route("/annotate/:pipeline", "application/json") {
            @Override
            public Object handle(Request request, Response response) {

                String pipeline = request.params(":pipeline");
                String version = request.queryParams("version");
                String text = request.queryParams("text");

                try {
                    response.type("application/json");
                    return resolver.resolve(pipeline, version).annotate(text);
                } catch (Exception e) {
                    return e;// TODO proper error handling
                }
            }
        });
    }

    public static void main(String[] args) {
        init();
    }
}
