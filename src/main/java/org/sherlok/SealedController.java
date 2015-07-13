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

import javax.servlet.http.Part;

import org.sherlok.mappings.SherlokException;

/**
 * Blocks PUT and DELETE methods from {@link Controller}.
 * 
 * @author renaud@apache.org
 */
public class SealedController extends Controller {

    // /////////////////////////////////////////////////////////////////////
    // access API, LIST/GET are ok (inherit), PUT/DELETE are not allowed

    protected String getErrorMsg() {
        return "not allowed in sealed mode";
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
