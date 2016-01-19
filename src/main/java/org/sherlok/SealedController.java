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

import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.set;

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.Part;

import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.SherlokException;

/**
 * Restricts LIST and GET to {@link #pipelineId}; Blocks PUT and DELETE methods
 * from {@link Controller}.
 * 
 * @author renaud@apache.org
 */
public class SealedController extends Controller {

    // /////////////////////////////////////////////////////////////////////
    // access API, LIST/GET are ok (inherit), PUT/DELETE are not allowed

    private String pipelineId = null;

    public SealedController() {// should not get called...
    }

    public SealedController(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    protected String getErrorMsg() {
        return "not allowed in sealed mode";
    }

    // /////////////////////////////////////////////////////////////////////
    // access API, package visibility

    // LIST all /////////////////////////////////////////////////////////////

    Collection<PipelineDef> listPipelines() throws SherlokException {
        for (PipelineDef p : pipelineDefs.values()) {
            if (p.getId().equals(pipelineId)) {
                return list(p);
            }
        }
        throw new SherlokException(getErrorMsg(), "LIST pipelines");
    }

    // LIST all names /////////////////////////////////////////////////////////

    Set<String> listPipelineDefNames() {
        return set(pipelineId);
    }

    // GET by name /////////////////////////////////////////////////////////

    PipelineDef getPipelineDef(String pipelineId) throws SherlokException {
        if (pipelineId.equals(pipelineId)) {
            return pipelineDefs.get(pipelineId);
        } else {
            throw new SherlokException(getErrorMsg(),
                    "GET pipeline with id " + pipelineId);
        }
    }

    // PUT /////////////////////////////////////////////////////////////
    String putBundle(String bundleStr) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "PUT Bundle");
    }

    String putPipeline(String pipelineStr) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "PUT Pipeline");
    }

    void putResource(String path, Part part) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "PUT Resource");
    }

    // DELETE /////////////////////////////////////////////////////////////
    void deleteBundleDef(String bundleId) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "DELETE BundleDef");
    }

    void deletePipelineDef(String pipelineId) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "DELETE PipelineDef");
    }

    void deleteResource(String path) throws SherlokException {
        throw new SherlokException(getErrorMsg(), "DELETE Resource");
    }
}
