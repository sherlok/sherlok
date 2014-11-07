package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.map;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Map;

import org.sherlok.Sherlok;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.slf4j.Logger;

public class Store {
    private static Logger LOG = getLogger(Store.class);

    public static final String TYPES_PATH = "config/types/";
    public static final String BUNDLES_PATH = "config/bundles/";
    public static final String ENGINES_PATH = "config/engines/";
    public static final String PIPELINES_PATH = "config/pipelines/";

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
            try {
                TypesDef tdefs = TypesDef.load(tf);
                for (TypeDef type : tdefs.getTypes()) {
                    String key = type.getShortName();
                    if (typesDefs.containsKey(key)) {
                        throw new RuntimeException(); // TODO
                    } else {
                        typesDefs.put(key, type);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        // BUNDLES
        bundleDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(BUNDLES_PATH),
                new String[] { "json" }, true))) {
            try {
                BundleDef bd = BundleDef.load(bf);
                if (bd.validate()) {

                    String key = bd.getName() + Sherlok.SEPARATOR
                            + bd.getVersion();
                    if (bundleDefs.containsKey(key)) {
                        throw new RuntimeException(); // TODO
                    } else {
                        bundleDefs.put(key, bd);
                    }
                } else {
                    throw new RuntimeException(); // TODO
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        // ENGINES
        engineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(ENGINES_PATH),
                new String[] { "json" }, true))) {
            try {
                EngineDef ed = EngineDef.load(bf);
                if (ed.validate()) {

                    String key = ed.getName() + Sherlok.SEPARATOR
                            + ed.getVersion();
                    if (engineDefs.containsKey(key)) {
                        throw new RuntimeException(); // TODO
                    } else {
                        engineDefs.put(key, ed);
                    }
                } else {
                    throw new RuntimeException(); // TODO
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        // PIPELINES
        pipelineDefs = map(); // reinit
        // read Defs
        for (File bf : newArrayList(iterateFiles(new File(PIPELINES_PATH),
                new String[] { "json" }, true))) {
            try {
                PipelineDef pd = PipelineDef.load(bf);
                if (pd.validate()) {

                    String key = pd.getName() + Sherlok.SEPARATOR
                            + pd.getVersion();
                    if (pipelineDefs.containsKey(key)) {
                        throw new RuntimeException(); // TODO
                    } else {
                        pipelineDefs.put(key, pd);
                    }
                } else {
                    throw new RuntimeException(); // TODO
                }
            } catch (Exception e) {
                // TODO: handle exception
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
}
