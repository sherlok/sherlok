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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.sherlok.FileBased;
import org.slf4j.Logger;

/**
 * HTTP(S) config variable
 * 
 * When processed, this variable will download the file pointed by the given URL
 * and store it locally.
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
    private final Boolean rutaCompatible;

    /**
     * Build a new variable.
     * 
     * @param url
     *            remote resource URL
     * @param rutaCompatible
     *            specify that this variable is used in a RUTA context and need
     *            to be converted to a relative path
     */
    public HttpConfigVariable(String url, Boolean rutaCompatible) {
        assert url != null;
        this.url = url;
        this.rutaCompatible = rutaCompatible;
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

        if (rutaCompatible) {
            return FileBased.getRelativePathToResources(file.getAbsoluteFile());
        } else {
            return file.getAbsolutePath();
        }
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
        return Integer.toHexString(url.hashCode());
    }

}
