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
package org.sherlok.utils;

import static org.sherlok.utils.AetherResolver.SHERLOK_REPO_ID;
import static org.sherlok.utils.AetherResolver.SHERLOK_REPO_URL;
import static org.sherlok.utils.Create.list;
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
import org.slf4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MavenPom {
    private static final Logger LOG = getLogger(MavenPom.class);

    public static class RepoDefDTO {
        public String id, url;

        public RepoDefDTO(String id, String url) {
            this.id = id;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RepoDefDTO && ((RepoDefDTO) obj).id.equals(id)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    /**
     * Generates and writes a Maven pom.xml in local_repo
     * 
     * @param bundleDefs
     * @param pipelineName
     * @param version
     * @return pom id for aether
     */
    public static String writePom(Set<BundleDef> bundleDefs,
            String pipelineName, String version) throws IOException,
            TemplateException {

        // Freemarker configuration object
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(MavenPom.class, "/");

        Template template = cfg.getTemplate("fakePom.ftl");

        // Bind variables
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("now", System.currentTimeMillis());
        data.put("pipelineName", pipelineName);
        data.put("pipelineVersion", version);

        List<BundleDependency> deps = list();
        for (BundleDef bundleDef : bundleDefs) {
            deps.addAll(bundleDef.getDependencies());
        }
        data.put("deps", deps);

        // order matters, so that sherlok github repo can be inserted first
        Set<RepoDefDTO> repoDefs = new LinkedHashSet<RepoDefDTO>();
        // add sherlok repository first
        RepoDefDTO rds = new RepoDefDTO(SHERLOK_REPO_ID, SHERLOK_REPO_URL);
        if (repoDefs.add(rds))
            LOG.trace("adding sherlok repo id '{}', url '{}' to pom", rds.id,
                    rds.url);
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
        File dir = new File(AetherResolver.LOCAL_REPO_PATH + "/org/sherlok/"
                + pipelineName + "/" + version);
        dir.mkdirs();

        File pomFile = new File(dir, pipelineName + "-" + version + ".pom");
        Writer writer = new FileWriter(pomFile);
        template.process(data, writer);
        writer.flush();
        writer.close();
        LOG.trace("pomFile written to '{}'", pomFile.getAbsolutePath());

        return "org.sherlok:" + pipelineName + ":pom:" + version;
    }
}
