/**
 * Copyright (C) 2014-2015 Renaud Richardet
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
package org.sherlok.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.sherlok.FileBased;
import org.slf4j.Logger;

/**
 * Git config variable
 * 
 * When processed, this variable will download the given git repository and
 * store it locally.
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
    private final Boolean rutaCompatible;

    /**
     * Construct a config variable for git repository
     * 
     * @param url
     *            mandatory url to repository
     * @param ref
     *            can be a SHA, a branch, a tag or null (for default master
     *            branch)
     * @param rutaCompatible
     *            specify that this variable is used in a RUTA context and need
     *            to be converted to a relative path
     */
    public GitConfigVariable(String url, String ref, Boolean rutaCompatible) {
        assert url != null;
        this.url = url;
        this.ref = ref != null ? ref : "master";
        this.rutaCompatible = rutaCompatible;
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

        if (rutaCompatible) {
            return FileBased.getRelativePathToResources(dir.getAbsoluteFile());
        } else {
            return dir.getAbsolutePath();
        }
    }

    // TODO move me to a more appropriate location
    private static void runCommand(File workingDirectory, String command)
            throws ProcessConfigVariableException {
        try {
            LOG.trace("Running system command '" + command + "' in "
                    + workingDirectory);

            Process clone = Runtime.getRuntime().exec(command, null,
                    workingDirectory.getAbsoluteFile());

            int status = clone.waitFor();
            if (status != 0) {
                InputStream stderr = clone.getErrorStream();
                String msg = "Cloning failed with status " + status
                        + ". stderr was: " + IOUtils.toString(stderr);
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

    /**
     * Path to the downloaded resource
     */
    private File getPath() {
        return new File(PATH_BASE, getPathId());
    }

    /**
     * Hash code of this resource
     */
    private String getPathId() {
        // The hash should have a low collision factor
        return Integer.toHexString((url + "@" + ref).hashCode());
    }

}
