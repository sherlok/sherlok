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
package org.sherlok;

import static org.sherlok.SherlokServer.BUNDLES;
import static org.sherlok.SherlokServer.PIPELINES;
import static org.sherlok.SherlokServer.RUTA_RESOURCES;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.SherlokException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used when running {@link SherlokServer} in slave mode, that is:
 * {@link BundleDef}s and {@link PipelineDef}s are retrieved from another master
 * Sherlok server (see {@link #load()}).<br>
 * Same as {@link SealedController}: blocks PUT and DELETE methods from
 * {@link Controller}.
 * 
 * @author renaud@apache.org
 */
public class SlaveController extends SealedController {

    private String masterUrl;
    private String tmpDir; // to store resources

    public SlaveController(String masterUrl) {
        this.masterUrl = masterUrl;
        this.tmpDir = System.getProperty("java.io.tmpdir");
    }

    /**
     * Loads {@link BundleDef}s and {@link PipelineDef}s from another master
     * Sherlok server
     */
    @Override
    public SlaveController load() throws SherlokException {
        final ObjectMapper MAPPER = new ObjectMapper();

        // GET from master
        List<PipelineDef> remotePipelineDefs;
        List<BundleDef> remoteBundleDefs;
        try {
            remotePipelineDefs = MAPPER.readValue(
                    new URL(masterUrl + PIPELINES),
                    new TypeReference<List<PipelineDef>>() {
                    });
            remoteBundleDefs = MAPPER.readValue(new URL(masterUrl + BUNDLES),
                    new TypeReference<List<BundleDef>>() {
                    });
        } catch (Exception e) {
            throw new SherlokException(
                    "failed to load remote bundles and pipelines from master_url, "
                            + e.getMessage()).setObject(masterUrl).setDetails(
                    e.getStackTrace());
        }

        Controller c;
        try {
            c = _load(remoteBundleDefs, remotePipelineDefs);
        } catch (SherlokException e) {
            throw e.setWhen("loading pipelines and bundles (note: remote loading ok)");
        }

        LOG.debug(
                "SLAVE: Done loading from master '{}': {} bundles, {} engines, {} pipelines. Using tmp dir '{}'",
                new Object[] { masterUrl, bundleDefs.size(), engineDefs.size(),
                        pipelineDefs.size(), tmpDir });
        return (SlaveController) c;
    }

    // /////////////////////////////////////////////////////////////////////
    // access API, LIST/GET are ok (inherit), PUT/DELETE are not allowed

    /** get the resource from Master */
    @Override
    InputStream getResource(String path) throws SherlokException {
        String urlStr = masterUrl + RUTA_RESOURCES + "/" + path;
        try {
            URL url = new URL(urlStr);
            return url.openStream();
        } catch (MalformedURLException e) {
            throw new SherlokException("malformed url", urlStr);
        } catch (IOException e) {
            throw new SherlokException("could not load resource from Master",
                    path);
        }
    }

    @Override
    protected String getErrorMsg() {
        return "not allowed in slave mode";
    }
}
