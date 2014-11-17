package org.sherlok.mappings;

import static org.sherlok.utils.Create.list;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sherlok.aether.AetherResolver;
import org.sherlok.mappings.BundleDef.BundleDependency;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MavenPom {

    public static class RepoDef {
        public String id, url;

        public String getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }
    }

    public static String writePom(Set<BundleDef> bundleDefs,
            String pipelineName, String version) throws IOException,
            TemplateException {

        // Freemarker configuration object
        Configuration cfg = new Configuration();

        Template template = cfg.getTemplate("src/main/resources/fakePom.ftl");

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

        List<RepoDef> repoDefs = list();
        for (BundleDef bundleDef : bundleDefs) {
            for (Entry<String, String> id_url : bundleDef.getRepositories()
                    .entrySet()) {
                RepoDef rd = new RepoDef();
                rd.id = id_url.getKey();
                rd.url = id_url.getValue();
                repoDefs.add(rd);
            }
        }
        data.put("repos", repoDefs);

        // File output
        File dir = new File(AetherResolver.LOCAL_REPO_PATH + "/org/sherlok/"
                + pipelineName + "/" + version);
        dir.mkdirs();

        Writer file = new FileWriter(new File(dir, pipelineName + "-" + version
                + ".pom"));
        template.process(data, file);
        file.flush();
        file.close();

        return "org.sherlok:" + pipelineName + ":pom:" + version;
    }
}
