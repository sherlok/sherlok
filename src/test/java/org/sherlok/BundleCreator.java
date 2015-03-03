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

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.sherlok.utils.AetherResolver.LOCAL_REPO_PATH;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.Create.set;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.sherlok.PipelineLoader.ClassPathHack;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.BundleDependency;
import org.sherlok.mappings.BundleDef.BundleDependency.DependencyType;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.AetherResolver;
import org.sherlok.utils.MavenPom;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Util to generate bundle JSON from Maven artifacts
 */
public class BundleCreator {
    private static Logger LOG = getLogger(BundleCreator.class);

    public static void main(String[] args) throws Exception {

        BundleDef b1 = (BundleDef) new BundleDef().setName("myname").setVersion("myversion");
        // b1.addRepository(
        // "dkpro",
        // "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/");
        b1.addDependency(new BundleDependency(DependencyType.mvn,

        // Mallet Sherlok
        // "org.sherlok:sherlok_mallet:0.0.1-SNAPSHOT"
                // "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.7.0"));
                // "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.7.0"));

                // Mallet LDA
                // "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.mallet-asl:1.7.0"

                // MST parser
                //"de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.mstparser-asl:1.7.0"

                // langdetect
        "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.langdetect-asl:1.7.0"
        ));
        Set<BundleDef> bundleDefs = set(b1);

        createBundle(bundleDefs);
    }

    private static void createBundle(Set<BundleDef> bundleDefs)
            throws Exception {

        // create fake POM from bundles and copy it
        String fakePom = MavenPom.writePom(bundleDefs, "BundleCreator",
                System.currentTimeMillis() + "");// must be unique
        Artifact rootArtifact = new DefaultArtifact(fakePom);
        LOG.trace("* rootArtifact: '{}'", rootArtifact);

        // add remote repository urls
        RepositorySystem system = AetherResolver.newRepositorySystem();
        RepositorySystemSession session = AetherResolver
                .newRepositorySystemSession(system,
                        AetherResolver.LOCAL_REPO_PATH);
        Map<String, String> repositoriesDefs = map();
        for (BundleDef b : bundleDefs) {
            b.validate(b.getId());
            for (Entry<String, String> id_url : b.getRepositories().entrySet()) {
                repositoriesDefs.put(id_url.getKey(), id_url.getValue());
            }
        }
        List<RemoteRepository> repos = AetherResolver.newRepositories(system,
                session, repositoriesDefs);

        // solve dependencies
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(rootArtifact, ""));
        collectRequest.setRepositories(AetherResolver.newRepositories(system,
                session, new HashMap<String, String>()));
        CollectResult collectResult = system.collectDependencies(session,
                collectRequest);
        collectResult.getRoot().accept(
                new AetherResolver.ConsoleDependencyGraphDumper());
        PreorderNodeListGenerator p = new PreorderNodeListGenerator();
        collectResult.getRoot().accept(p);

        // now do the real fetching, and add jars to classpath
        List<Artifact> resolvedArtifacts = list();
        for (Dependency dependency : p.getDependencies(true)) {
            Artifact resolvedArtifact = system.resolveArtifact(session,
                    new ArtifactRequest(dependency.getArtifact(), repos, ""))
                    .getArtifact();
            resolvedArtifacts.add(resolvedArtifact);
            File jar = resolvedArtifact.getFile();

            // add this jar to the classpath
            ClassPathHack.addFile(jar);
            LOG.trace("* resolved artifact '{}', added to classpath: '{}'",
                    resolvedArtifact, jar.getAbsolutePath());
        }

        BundleDef createdBundle = new BundleDef();
        createdBundle.setVersion("TODO!");
        createdBundle.setName("TODO!");
        for (BundleDef bundleDef : bundleDefs) {
            for (BundleDependency dep : bundleDef.getDependencies()) {
                createdBundle.addDependency(dep);
            }
        }

        for (Artifact a : resolvedArtifacts) {

            // only consider artifacts that were included in the initial bundle
            boolean found = false;
            for (BundleDef bundleDef : bundleDefs) {
                for (BundleDependency dep : bundleDef.getDependencies()) {
                    if (a.getArtifactId().equals(dep.getArtifactId())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {

                JarInputStream is = new JarInputStream(new FileInputStream(
                        a.getFile()));
                JarEntry entry;
                while ((entry = is.getNextJarEntry()) != null) {
                    if (entry.getName().endsWith(".class")) {

                        try {
                            Class<?> clazz = Class.forName(entry.getName()
                                    .replace('/', '.').replace(".class", ""));

                            // all uimafit AnnotationEngines
                            if (JCasAnnotator_ImplBase.class
                                    .isAssignableFrom(clazz)) {
                                LOG.debug("AnnotationEngine: {}",
                                        clazz.getSimpleName());

                                final EngineDef engine = new EngineDef()
                                        .setClassz(clazz.getName())
                                        .setName(clazz.getSimpleName())
                                        .setBundle(createdBundle);
                                createdBundle.addEngine(engine);
                                LOG.debug("{}", engine);

                                ReflectionUtils.doWithFields(clazz,
                                        new FieldCallback() {
                                            public void doWith(final Field f)
                                                    throws IllegalArgumentException,
                                                    IllegalAccessException {

                                                ConfigurationParameter c = f
                                                        .getAnnotation(ConfigurationParameter.class);
                                                if (c != null) {
                                                    LOG.debug(
                                                            "* param: {} {} {} {}",
                                                            new Object[] {
                                                                    c.name(),
                                                                    c.defaultValue(),
                                                                    c.mandatory(),
                                                                    f.getType() });

                                                    String deflt = c
                                                            .mandatory() ? "TODO"
                                                            : "IS OPTIONAL";

                                                    String value = c
                                                            .defaultValue()[0]
                                                            .equals(ConfigurationParameter.NO_DEFAULT_VALUE) ? deflt
                                                            : c.defaultValue()[0]
                                                                    .toString();
                                                    engine.addParameter(
                                                            c.name(),
                                                            list(value));
                                                }
                                            }
                                        });
                            }
                        } catch (Throwable e) {
                            System.err.println("something wrong with class "
                                    + entry.getName() + " " + e);
                        }
                    }
                }
                is.close();
            }
        }

        // delete fake pom
        deleteDirectory(new File(LOCAL_REPO_PATH + "/org/sherlok/BundleCreator"));

        System.out.println(FileBased.writeAsString(createdBundle));
    }
}
