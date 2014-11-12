package org.sherlok;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.sherlok.Sherlok.CONFIG_DIR_PATH;
import static org.sherlok.mappings.Def.createId;
import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineEngine;
import org.sherlok.mappings.TypesDef;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

public class Store {
    private static final Logger LOG = getLogger(Store.class);

    public static final String TYPES_PATH = CONFIG_DIR_PATH + "types/";
    public static final String BUNDLES_PATH = CONFIG_DIR_PATH + "bundles/";
    public static final String ENGINES_PATH = CONFIG_DIR_PATH + "engines/";
    public static final String PIPELINES_PATH = CONFIG_DIR_PATH + "pipelines/";

    private Map<String, TypeDef> typesDefs;
    private Map<String, BundleDef> bundleDefs;
    private Map<String, EngineDef> engineDefs;
    private Map<String, PipelineDef> pipelineDefs;

    public Store load() {

        // Types
        typesDefs = map(); // reinit
        // read Defs
        for (File tf : newArrayList(iterateFiles(new File(TYPES_PATH),
                new String[] { "json" }, true))) {
            TypesDef tdefs = FileBased.loadTypes(tf);
            for (TypeDef type : tdefs.getTypes()) {
                String key = type.getShortName();
                if (typesDefs.containsKey(key)) {
                    throw new ValidationException("duplicate types: '" + key
                            + "'");
                } else {
                    typesDefs.put(key, type);
                }
            }
        }

        // BUNDLES
        bundleDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(BUNDLES_PATH),
                new String[] { "json" }, true))) {
            BundleDef bd = FileBased.loadBundle(bf);
            bd.validate(bd.toString());
            String key = createId(bd.getName(), bd.getVersion());
            if (bundleDefs.containsKey(key)) {
                throw new ValidationException("duplicate bundle ids: '" + key
                        + "'");
            } else {
                bundleDefs.put(key, bd);
            }
        }

        // ENGINES
        engineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(ENGINES_PATH),
                new String[] { "json" }, true))) {

            EngineDef ed = FileBased.loadEngine(bf);
            ed.validate(ed.toString());

            // bundle must be found
            if (!bundleDefs.containsKey(ed.getBundleId())) {
                throw new ValidationException(
                        "no bundle def found for engine '" + ed + "'");
            }
            // no duplicate engine ids
            if (engineDefs.containsKey(ed.getId())) {
                throw new ValidationException("duplicate engine ids: '" + ed
                        + "'");
            } else {
                engineDefs.put(ed.getId(), ed);
            }
        }

        // PIPELINES
        pipelineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(PIPELINES_PATH),
                new String[] { "json" }, true))) {

            PipelineDef pd = FileBased.loadPipeline(bf);
            pd.validate(pd.toString());

            // all engines must be found
            for (PipelineEngine pe : pd.getEngines()) {
                if (pe.getId() != null && !engineDefs.containsKey(pe.getId())) {
                    throw new ValidationException("no engine def found for '"
                            + pe + "' in pipeline '" + pe + "'");
                }
            }
            // no duplicate pipeline ids
            if (pipelineDefs.containsKey(pd.getId())) {
                throw new ValidationException("duplicate pipeline id '"
                        + pd.getId() + "'");
            } else {
                pipelineDefs.put(pd.getId(), pd);
            }
        }

        LOG.debug(
                "Done loading from Store: {} typeDefs, {} bundleDefs, {} engineDefs, {} pipelineDefs",
                new Object[] { typesDefs.size(), bundleDefs.size(),
                        engineDefs.size(), pipelineDefs.size() });
        return this;
    }

    // access API, package visibility

    TypeDef getTypeDef(String typeShortName) {
        return typesDefs.get(typeShortName);
    }

    BundleDef getBundleDef(String bundleId) {
        return bundleDefs.get(bundleId);
    }

    EngineDef getEngineDef(String engineId) {
        return engineDefs.get(engineId);
    }

    PipelineDef getPipelineDef(String pipelineId) {
        return pipelineDefs.get(pipelineId);
    }

    Set<String> getPipelineDefNames() {
        return pipelineDefs.keySet();
    }

    Collection<PipelineDef> listPipelines() {
        return pipelineDefs.values();
    }

    String putPipeline(String pipelineStr) throws ValidationException {
        PipelineDef newPipeline = FileBased.putPipeline(pipelineStr);
        pipelineDefs.put(newPipeline.getId(), newPipeline);
        return newPipeline.getId();
    }

    void deletePipelineDef(String pipelineId) {
        if (pipelineDefs.containsKey(pipelineId)) {
            String domain = pipelineDefs.get(pipelineId).getDomain();
            pipelineDefs.remove(pipelineId);
            FileBased.deletePipeline(pipelineId, domain);

        } else
            throw new ValidationException("pipeline not found");
    }
}
