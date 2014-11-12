package spark;

/**
 * Utility class to shutdown the Spark server during unit tests. Required
 * because of package scope of {@link Spark#stop()}.
 * 
 * @author renaud@apache.org
 */
public class StopServer {

    public static void stop() {
        Spark.stop();
    }
}
