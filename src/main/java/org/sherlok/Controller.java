/**
 * Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
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

import static org.sherlok.utils.CheckThat.validateId;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

public class Controller {
    private static final Logger LOG = getLogger(Controller.class);

    private Map<String, BundleDef> bundleDefs;
    private Map<String, EngineDef> engineDefs;
    private Map<String, PipelineDef> pipelineDefs;

    public Controller load() throws ValidationException {

        // BUNDLES AND ENGINES
        bundleDefs = map(); // reinit
        engineDefs = map(); // reinit
        for (BundleDef b : FileBased.allBundleDefs()) {
            b.validate(b.toString());
            String key = b.getId();
            if (bundleDefs.containsKey(key)) {
                throw new ValidationException("duplicate bundle ids", key);
            } else {
                // validate all engines
                for (EngineDef en : b.getEngines()) {
                    en.validate(en.toString());
                    en.setBundle(b);
                    if (engineDefs.containsKey(en.getId())) {
                        throw new ValidationException("duplicate engine ids",
                                en.getId());
                    }
                }
                // add bundle and all engines
                for (EngineDef en : b.getEngines()) {
                    engineDefs.put(en.getId(), en);
                }
                bundleDefs.put(key, b);
            }
        }

        // PIPELINES
        pipelineDefs = map(); // reinit
        for (PipelineDef pd : FileBased.allPipelineDefs()) {
            pd.validate(pd.toString());

            // all engines must be found
            for (String pengineId : pd.getEnginesFromScript()) {

                validateId(pengineId, "engine id '" + pengineId
                        + "' in pipeline '" + pd + "'");
                if (!engineDefs.containsKey(pengineId)) {
                    throw new ValidationException(
                            "engine def not found in pipeline '" + pd + "'",
                            pengineId);
                }
            }
            // no duplicate pipeline ids
            if (pipelineDefs.containsKey(pd.getId())) {
                throw new ValidationException("duplicate pipeline id",
                        pd.getId());
            } else {
                pipelineDefs.put(pd.getId(), pd);
            }
        }

        LOG.debug(
                "Done loading from Store: {} bundles, {} engines, {} pipelines",
                new Object[] { bundleDefs.size(), engineDefs.size(),
                        pipelineDefs.size() });
        return this;
    }

    // /////////////////////////////////////////////////////////////////////
    // access API, package visibility

    // LIST all /////////////////////////////////////////////////////////////

    Collection<BundleDef> listBundles() {
        return bundleDefs.values();
    }

    Collection<PipelineDef> listPipelines() {
        return pipelineDefs.values();
    }

    Collection<String> listResources() throws ValidationException {
        return FileBased.allResources();
    }

    // LIST all names /////////////////////////////////////////////////////////
    Set<String> listBundleDefNames() {
        return bundleDefs.keySet();
    }

    Set<String> listPipelineDefNames() {
        return pipelineDefs.keySet();
    }

    // GET by name /////////////////////////////////////////////////////////

    BundleDef getBundleDef(String bundleId) {
        return bundleDefs.get(bundleId);
    }

    EngineDef getEngineDef(String engineId) {
        return engineDefs.get(engineId);
    }

    PipelineDef getPipelineDef(String pipelineId) {
        return pipelineDefs.get(pipelineId);
    }

    // PUT /////////////////////////////////////////////////////////////
    String putBundle(String bundleStr) throws ValidationException {
        BundleDef newBundle = FileBased.putBundle(bundleStr);
        bundleDefs.put(newBundle.getId(), newBundle);
        return newBundle.getId();
    }

    String putPipeline(String pipelineStr) throws ValidationException {
        PipelineDef newPipeline = FileBased.putPipeline(pipelineStr);
        pipelineDefs.put(newPipeline.getId(), newPipeline);
        return newPipeline.getId();
    }

    // DELETE /////////////////////////////////////////////////////////////
    void deleteBundleDef(String bundleId) throws ValidationException {
        if (!bundleDefs.containsKey(bundleId)) {
            throw new ValidationException("bundle not found", bundleId);
        }
        FileBased.deleteBundle(bundleId);
        bundleDefs.remove(bundleId);
    }

    void deletePipelineDef(String pipelineId) throws ValidationException {
        if (!pipelineDefs.containsKey(pipelineId)) {
            throw new ValidationException("pipeline  not found", pipelineId);
        }
        String domain = pipelineDefs.get(pipelineId).getDomain();
        FileBased.deletePipeline(pipelineId, domain);
        pipelineDefs.remove(pipelineId);
    }
}
