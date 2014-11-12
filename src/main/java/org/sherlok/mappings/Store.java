package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.map;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.sherlok.Sherlok;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.slf4j.Logger;

public class Store {
    private static final Logger LOG = getLogger(Store.class);

    public static final String TYPES_PATH = "config/types/";
    public static final String BUNDLES_PATH = "config/bundles/";
    public static final String ENGINES_PATH = "config/engines/";
    public static final String PIPELINES_PATH = "config/pipelines/";

    private Map<String, TypeDef> typesDefs;
    private Map<String, BundleDef> bundleDefs;
    private Map<String, EngineDef> engineDefs;
    private Map<String, PipelineDef> pipelineDefs;

    public Store load() {
        LOG.debug("loading Store");

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
        LOG.debug("loaded {} typeDefs", typesDefs.size());

        // BUNDLES
        bundleDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(BUNDLES_PATH),
                new String[] { "json" }, true))) {
            BundleDef bd = FileBased.loadBundle(bf);
            if (bd.validate()) {
                String key = bd.getName() + Sherlok.SEPARATOR + bd.getVersion();
                if (bundleDefs.containsKey(key)) {
                    throw new ValidationException("duplicate bundle ids: '"
                            + key + "'");
                } else {
                    bundleDefs.put(key, bd);
                }
            }
        }
        LOG.debug("loaded {} bundleDefs", bundleDefs.size());

        // ENGINES
        engineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(ENGINES_PATH),
                new String[] { "json" }, true))) {
            EngineDef ed = FileBased.loadEngine(bf);
            if (ed.validate()) {
                String key = ed.getName() + Sherlok.SEPARATOR + ed.getVersion();
                if (engineDefs.containsKey(key)) {
                    throw new ValidationException("duplicate engine ids: '"
                            + key + "'");
                } else {
                    engineDefs.put(key, ed);
                }
            }
        }
        LOG.debug("loaded {} engineDefs", engineDefs.size());

        // PIPELINES
        pipelineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(PIPELINES_PATH),
                new String[] { "json" }, true))) {
            PipelineDef pd = FileBased.loadPipeline(bf);
            if (pd.validate()) {

                if (pipelineDefs.containsKey(pd.getId())) {
                    throw new ValidationException("duplicate pipeline ids: '"
                            + pd.getId() + "'");
                } else {
                    pipelineDefs.put(pd.getId(), pd);
                }
            }
        }
        return this;
    }

    public TypeDef getTypeDef(String typeShortName) {
        return typesDefs.get(typeShortName);
    }

    public BundleDef getBundleDef(String bundleId) {
        return bundleDefs.get(bundleId);
    }

    public EngineDef getEngineDef(String engineId) {
        return engineDefs.get(engineId);
    }

    public PipelineDef getPipelineDef(String pipelineId) {
        return pipelineDefs.get(pipelineId);
    }

    public Set<String> getPipelineDefNames() {
        return pipelineDefs.keySet();
    }
}
