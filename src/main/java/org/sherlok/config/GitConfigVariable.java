package org.sherlok.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.sherlok.utils.ops.InputStreamOps;
import org.slf4j.Logger;

/**
 * Git config variable
 * 
 * TODO add ability to cleanup copy that are no longer used
 * 
 * TODO add ability to fetch new changes
 */
public class GitConfigVariable implements ConfigVariable {

    private static Logger LOG = getLogger(GitConfigVariable.class);
    private static final File PATH_BASE;

    static {
        // TODO add environment variable to let the user select another
        // directory if he wants to.
        PATH_BASE = new File("config/runtime/git/");

        // Create the runtime location for git repositories and make sure
        // we can use it.
        PATH_BASE.mkdirs();
        assert PATH_BASE.isDirectory();
        assert PATH_BASE.canRead();
        assert PATH_BASE.canWrite();
    }

    /**
     * This removes EVERYTHING that was downloaded
     */
    public static void cleanCache() {
        if (PATH_BASE.exists()) {
            try {
                // NB: File.delete() is NOT recursive!
                FileUtils.deleteDirectory(PATH_BASE);
            } catch (IOException e) {
                String error = "GitConfigVariable couldn't properly clean its cache";
                LOG.debug(error, e);
            }
        }
    }

    private final String url;
    private final String ref; // SHA or branch/tag

    /**
     * Construct a config variable for git repository
     * 
     * @param url
     *            mandatory url to repository
     * @param ref
     *            can be a SHA, a branch, a tag or null (for default master
     *            branch)
     */
    public GitConfigVariable(String url, String ref) {
        assert url != null;
        this.url = url;
        this.ref = ref != null ? ref : "master";
    }

    @Override
    public String getProcessedValue() throws ProcessConfigVariableException {
        File dir = getPath();

        // if needed, clone the directory and checkout the reference
        if (!dir.exists()) {
            dir.mkdirs();
            runCommand(dir, getCloneCommand());
            runCommand(dir, getCheckoutCommand());
        }

        return dir.getAbsolutePath();
    }

    // TODO move me to a more appropriate location
    private static void runCommand(File workingDirectory, String command)
            throws ProcessConfigVariableException {
        try {
            Process clone = Runtime.getRuntime().exec(command, null,
                    workingDirectory.getAbsoluteFile());

            int status = clone.waitFor();
            if (status != 0) {
                InputStream stderr = clone.getErrorStream();
                String msg = "Cloning failed with status " + status
                        + ". stderr was: " + InputStreamOps.readContent(stderr);
                throw new ProcessConfigVariableException(msg);
            }
        } catch (IOException | InterruptedException e) {
            // TODO is it OK to do nothing about InterruptedException?
            throw new ProcessConfigVariableException(
                    "An IO error occured while executing " + command, e);
        }
    }

    private String getCloneCommand() {
        return "git clone " + url + " .";
    }

    private String getCheckoutCommand() {
        return "git checkout " + ref;
    }

    private File getPath() {
        return new File(PATH_BASE, getPathId());
    }

    private String getPathId() {
        // The hash should have a low collision factor and should be enough for
        // our needs.
        Integer id = (url + "@" + ref).hashCode(); // "unique" ID
        return id.toString();
    }

}
