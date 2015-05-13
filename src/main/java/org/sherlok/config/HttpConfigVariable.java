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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
    private String filename = null; // computed when fetching the remote resource

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
        if (file == null || !file.exists()) {
            try {
                LOG.trace("Downloading file from " + url);

                // open an HTTP connection with the remote server
                URL remote = new URL(this.url);
                HttpURLConnection connection = (HttpURLConnection) remote
                        .openConnection();
                int status = connection.getResponseCode();

                // make sure the request was successful
                if (status != HttpURLConnection.HTTP_OK) {
                    throw new ProcessConfigVariableException(
                            "Status code from HTTP request is not 200: "
                                    + status);
                }

                // save the filename
                String disposition = connection
                        .getHeaderField("Content-Disposition");
                file = extractFileName(disposition);

                // save the remote file
                InputStream input = connection.getInputStream();
                FileOutputStream output = FileUtils.openOutputStream(file);
                IOUtils.copy(input, output);

                output.close();
                input.close();
                connection.disconnect();
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
     * Use the information given by the server if available, otherwise use
     * fallback.
     */
    private File extractFileName(String disposition) {
        if (disposition != null) {
            String key = "filename=";
            int index = disposition.indexOf(key);
            if (index > 0) {
                filename = disposition.substring(index + key.length(),
                        disposition.length() - 1);
            } else {
                extractFallbackFileName();
            }
        } else {
            extractFallbackFileName();
        }

        // it is now safe to return the path to where the file will be saved
        return getPath();
    }

    /**
     * Fallback for filename if the server give us nothing.
     */
    private void extractFallbackFileName() {
        int index = url.lastIndexOf("/");
        if (index > 0) {
            filename = url.substring(index + 1, url.length());
        } else {
            // no name available in URL... use hash as final fallback
            filename = getPathId();
        }
    }

    /**
     * Path to the downloaded resource, or null if it was not yet downloaded
     */
    private File getPath() {
        if (filename == null) {
            return null;
        } else {
            return new File(new File(PATH_BASE, getPathId()), filename);
        }
    }

    /**
     * Hash code of this resource
     */
    private String getPathId() {
        // The hash should have a low collision factor
        return Integer.toHexString(url.hashCode());
    }

}
