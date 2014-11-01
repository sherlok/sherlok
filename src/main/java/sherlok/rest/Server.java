package sherlok.rest;

import static spark.Spark.*;
import spark.*;

public class Server {

    public static void main(String[] args) {

        // A file /public/css/style.css is made available as
        // http://{host}:{port}/css/style.css
        externalStaticFileLocation("public"); // Static files

        get(new Route("/hello") {
            @Override
            public Object handle(Request request, Response response) {

                int i = 12;

                return "Hello Spark MVC Framework!" + i;
            }
        });
        
        
        
        
        get(new Route("/annotate") {
            @Override
            public Object handle(Request request, Response response) {

                int i = 12;

                return "Hello Spark MVC Framework!" + i;
            }
        });
    }
}
