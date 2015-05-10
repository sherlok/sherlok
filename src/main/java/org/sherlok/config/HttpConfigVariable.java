package org.sherlok.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.sherlok.FileBased;
import org.slf4j.Logger;

/**
 * HTTP(S) config variable
 * 
 * TODO allow archive extraction through an extra setting
 */
public class HttpConfigVariable implements ConfigVariable {

    private static Logger LOG = getLogger(GitConfigVariable.class);
    private static final File PATH_BASE;

    static {
        // TODO add environment variable to let the user select another
        // directory if he wants to.
        PATH_BASE = new File("config/runtime/http/");

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
                String error = "HttpConfigVariable couldn't properly clean its cache";
                LOG.debug(error, e);
            }
        }
    }

    private final String url;

    public HttpConfigVariable(String url) {
        assert url != null;
        this.url = url;
    }

    @Override
    public String getProcessedValue() throws ProcessConfigVariableException {
        File file = getPath();

        // if needed, download the file
        if (!file.exists()) {
            file.getParentFile().mkdirs();

            try {
                // FIXME the name of the file should be preserved for some annotators
                // see http://stackoverflow.com/a/13109832/520217 for implementation details.
                LOG.trace("Downloading file from " + url);
                FileUtils.copyURLToFile(new URL(url), file);
            } catch (IOException e) {
                String msg = "Failed to download " + url;
                throw new ProcessConfigVariableException(msg, e);
            }
        }

        return FileBased.getRelativePathToResources(file.getAbsoluteFile());
    }

    private File getPath() {
        return new File(PATH_BASE, getPathId());
    }

    private String getPathId() {
        // The hash should have a low collision factor
        return Integer.toHexString(url.hashCode());
    }

}
