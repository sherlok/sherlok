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

import static org.sherlok.utils.CheckThat.validateNotNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.set;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.BundleDependency;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.SherlokException;
import org.sherlok.utils.MavenPom;
import org.sherlok.utils.MavenPom.RepoDefDTO;
import org.slf4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Util to generate Maven xml pom from pipeline. Called from appify.sh script
 * 
 * @author renaud@apache.org
 */
public class Appify {
    private static Logger LOG = getLogger(Appify.class);

    public static void main(String[] args) throws Exception {

        if (args == null || args.length != 2) {// USAGE
            System.out.println("usage: pipeline_id sherlok_version");
        } else {

            String pipelineId = args[0];
            String sherlok_version = args[1];

            Controller controller = new Controller().load();
            PipelineDef pipelineDef = controller.getPipelineDef(pipelineId);

            // 3. create a list of engines (and their bundles) to resolve
            Set<BundleDef> bundleDefsToResolve = set();
            for (String pengineId : pipelineDef.getEnginesFromScript()) {
                EngineDef en = controller.getEngineDef(pengineId);
                if (en == null) {
                    throw new SherlokException();
                }
                validateNotNull(en,
                        "could not find engine '" + pengineId
                                + "' as defined in pipeline '"
                                + pipelineDef.getId() + "'");
                BundleDef b = en.getBundle();
                validateNotNull(b, "could not find bundle '" + b
                        + "' that is required by engine '" + en + "'");
                LOG.info("adding engineDef '{}' with bundleDef '{}'", en, b);
                bundleDefsToResolve.add(b);
            }

            writePom(pipelineDef, bundleDefsToResolve, sherlok_version);
        }
    }

    /**
     * Generates and writes a Maven pom.xml in local_repo
     * @param pipeline
     * @param bundleDefs
     * @param sherlok_version
     */
    public static void writePom(PipelineDef pipeline, Set<BundleDef> bundleDefs,
            String sherlok_version) throws IOException, TemplateException {

        // Freemarker configuration object
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(MavenPom.class, "/");

        Template template = cfg.getTemplate("fakePom_appify.ftl");

        // Bind variables
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("now", System.currentTimeMillis());
        data.put("pipelineName", pipeline.getName());
        data.put("pipelineVersion", pipeline.getVersion());
        data.put("sherlokVersion", sherlok_version);

        List<BundleDependency> deps = list();
        for (BundleDef bundleDef : bundleDefs) {
            deps.addAll(bundleDef.getDependencies());
        }
        data.put("deps", deps);

        // order matters, so that sherlok github repo can be inserted first
        Set<RepoDefDTO> repoDefs = new LinkedHashSet<RepoDefDTO>();
        for (BundleDef bundleDef : bundleDefs) {
            for (Entry<String, String> id_url : bundleDef.getRepositories()
                    .entrySet()) {
                RepoDefDTO rd = new RepoDefDTO(id_url.getKey(),
                        id_url.getValue());
                if (repoDefs.add(rd))
                    LOG.trace("adding repo id '{}', url '{}' to pom", rd.id,
                            rd.url);
            }
        }
        data.put("repos", repoDefs);

        // File output
        File dir = new File(FileBased.PIPELINE_CACHE_PATH);
        dir.mkdirs();

        File pomFile = new File(dir, pipeline.getId() + ".pom.xml");
        Writer writer = new FileWriter(pomFile);
        template.process(data, writer);
        writer.flush();
        writer.close();
        LOG.info("pomFile written to '{}'", pomFile.getAbsolutePath());
    }

}
