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
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.ValidationException.ERR;
import static org.sherlok.utils.ValidationException.MSG;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.ValidationException;

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
    public SlaveController load() throws ValidationException {
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
            throw new ValidationException(map(MSG,
                    "failed to load bundles and pipelines from master",
                    "master_url", masterUrl, ERR, e.getMessage()));
        }

        Controller c = _load(remoteBundleDefs, remotePipelineDefs);

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
    InputStream getResource(String path) throws ValidationException {
        try {
            URL url = new URL(masterUrl + RUTA_RESOURCES + "/" + path);
            return url.openStream();
        } catch (MalformedURLException e) {
            throw new ValidationException(e);
        } catch (IOException e) {
            throw new ValidationException(map(MSG,
                    "could not load resource from Master", ERR, path));
        }
    }

    @Override
    protected String getErrorMsg() {
        return "not allowed in slave mode";
    }
}
