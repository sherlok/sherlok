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

import static java.lang.String.format;
import static org.sherlok.utils.CheckThat.validateId;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Part;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.utils.AetherResolver;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

/**
 * Controller that CRUD's {@link PipelineDef} and {@link BundleDef} from disk
 * (using {@link FileBased}). This the controller for master mode.
 * 
 * @author renaud@apache.org
 */
public class Controller {
    protected static final Logger LOG = getLogger(Controller.class);

    // these act as caching:
    protected Map<String, BundleDef> bundleDefs;
    protected Map<String, EngineDef> engineDefs;
    protected Map<String, PipelineDef> pipelineDefs;

    /**
     * Loads {@link BundleDef}s and {@link PipelineDef}s from
     * {@link AetherResolver#LOCAL_REPO_PATH} folder, using {@link FileBased}.
     */
    public Controller load() throws ValidationException {
        Controller c;
        try {
            c = _load(FileBased.allBundleDefs(), FileBased.allPipelineDefs());
        } catch (ValidationException ve) {
            throw new ValidationException(
                    "could not load pipelines or bundles: " + ve.getMessage());
        }

        LOG.info(
                "Done loading from local File store ('{}'): {} bundles, {} engines and {} pipelines",
                new Object[] { FileBased.CONFIG_DIR_PATH,
                        bundleDefs.size(), engineDefs.size(),
                        pipelineDefs.size() });
        return c;
    }

    /** Loads bundles and pipelines into this controller */
    protected Controller _load(Collection<BundleDef> bundles,
            Collection<PipelineDef> pipelines) throws ValidationException {

        // BUNDLES AND ENGINES
        bundleDefs = map(); // reinit
        engineDefs = map(); // reinit
        for (BundleDef b : bundles) {
            b.validate(b.toString());
            String key = b.getId();
            if (bundleDefs.containsKey(key)) {
                throw new ValidationException("duplicate bundle ids", key);
            } else {
                // validate all engines
                for (EngineDef en : b.getEngines()) {
                    en.validate(en.toString());
                    en.setBundle(b); // this reference is needed later on
                    if (engineDefs.containsKey(en.getId())) {
                        throw new ValidationException("duplicate engine ids",
                                en.getId());
                    }
                }
                // once all are valid, add bundle and all engines
                for (EngineDef en : b.getEngines()) {
                    engineDefs.put(en.getId(), en);
                }
                bundleDefs.put(key, b);
            }
        }

        // PIPELINES
        pipelineDefs = map(); // reinit
        for (PipelineDef pd : pipelines) {
            pd.validate(pd.toString());

            // all engines must have been previousely added above
            for (String pengineId : pd.getEnginesFromScript()) {

                validateId(pengineId, "engine id '" + pengineId
                        + "' in pipeline '" + pd + "'");
                if (!engineDefs.containsKey(pengineId)) {
                    throw new ValidationException(
                            format("engine '%s' defined in pipeline '%s' was not found in any registered bundle.",
                                    pengineId, pd), pengineId);
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

    InputStream getResource(String path) throws ValidationException {
        return FileBased.getResource(path);
    }

    // PUT /////////////////////////////////////////////////////////////
    /** @return the put'ed {@link BundleDef}'s id */
    String putBundle(String bundleStr) throws ValidationException {
        BundleDef b = FileBased.putBundle(bundleStr);
        // update cached bundles and engines
        bundleDefs.put(b.getId(), b);
        for (EngineDef e : b.getEngines()) {
            e.setBundle(b);
            engineDefs.put(e.getId(), e);
        }
        return b.getId();
    }

    /** @return the put'ed {@link PipelineDef}'s id */
    String putPipeline(String pipelineStr) throws ValidationException {
        PipelineDef p = FileBased.putPipeline(pipelineStr, engineDefs.keySet());
        pipelineDefs.put(p.getId(), p);
        return p.getId();
    }

    void putResource(String path, Part part) throws ValidationException {
        FileBased.putResource(path, part);
    }

    // DELETE /////////////////////////////////////////////////////////////
    void deleteBundleDef(String bundleId) throws ValidationException {
        if (!bundleDefs.containsKey(bundleId)) {
            throw new ValidationException("bundle not found", bundleId);
        } else {
            bundleDefs.remove(bundleId);
            FileBased.deleteBundle(bundleId);
        }
    }

    void deletePipelineDef(String pipelineId) throws ValidationException {
        if (!pipelineDefs.containsKey(pipelineId)) {
            throw new ValidationException("pipeline  not found", pipelineId);
        } else {
            String domain = pipelineDefs.get(pipelineId).getDomain();
            pipelineDefs.remove(pipelineId);
            FileBased.deletePipeline(pipelineId, domain);
        }
    }

    void deleteResource(String path) throws ValidationException {
        FileBased.deleteResource(path);
    }
}
