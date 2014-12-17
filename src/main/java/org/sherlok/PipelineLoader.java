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

import static org.sherlok.mappings.Def.createId;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateNotNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.Create.set;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.sherlok.utils.AetherResolver;
import org.sherlok.utils.MavenPom;
import org.sherlok.utils.Strings;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

import freemarker.template.TemplateException;

/**
 * Resolves, loads and caches {@link UimaPipeline}s.
 * 
 * @author renaud@apache.org
 */
public class PipelineLoader {
    private static final Logger LOG = getLogger(PipelineLoader.class);

    private Controller controller;
    private Map<String, UimaPipeline> uimaPipelinesCache = map();

    public PipelineLoader(Controller controller) {
        this.controller = controller;
    }

    /**
     * Checks if such a {@link PipelineDef} exists, and returns the
     * corresponding {@link UimaPipeline} (if it is in cache) or else
     * instantiates it. <br/>
     * Instantiation involves resolving the pipeline engines and the engines
     * bundles, then loading the bundles Maven artifacts into the classpath. <br/>
     * This method is synchronized, so that no two threads can access it during
     * initialization. TODO release the lock earlier
     * 
     * @param pipelineName
     * @param version
     *            the version id, or 'null' / {@link null} to try to fallback on
     *            the 'highest' version (see
     *            {@link Strings#compareNatural(String, String)}
     * @return the {@link UimaPipeline}
     */
    public synchronized UimaPipeline resolvePipeline(String pipelineName,
            String version) throws ValidationException {

        // 0. resolve version (fallback) if version=null
        if (version == null || version.equals("null")) {
            String highestVersion = "0000000000000000000000000000000";
            for (String pId : controller.listPipelineDefNames()) {
                if (getName(pId).equals(pipelineName)) {
                    String pVersion = getVersion(pId);
                    if (Strings.compareNatural(pVersion, highestVersion) > 0) {
                        highestVersion = pVersion;
                    }
                }
            }
            LOG.trace(
                    "resolved pipeline version to '{}' (was previousely '{}')",
                    highestVersion, version);
            version = highestVersion;
        }

        // 1. get pipeline from cache if available
        String pipelineId = createId(pipelineName, version);
        if (uimaPipelinesCache.containsKey(pipelineId)) {
            LOG.trace("pipeline '{}' found in cache", pipelineId);
            return uimaPipelinesCache.get(pipelineId);

        } else {
            // 2. else, load it from pipeline def
            PipelineDef pipelineDef = controller.getPipelineDef(pipelineId);
            validateNotNull(pipelineDef, "could not find Pipeline with Id '"
                    + pipelineId + "'");

            UimaPipeline uimaPipeline = load(pipelineDef);
            uimaPipelinesCache.put(pipelineId, uimaPipeline);
            return uimaPipeline;
        }
    }

    UimaPipeline load(PipelineDef pipelineDef) throws ValidationException {

        // 3. resolve engines (and their bundles)
        List<EngineDef> engineDefs = list();
        Set<BundleDef> bundleDefs = set();
        Map<String, String> repositoriesDefs = map();
        for (String pengineId : pipelineDef.getEnginesFromScript()) {
            EngineDef engineDef = controller.getEngineDef(pengineId);
            validateNotNull(engineDef, "could not find " + pengineId);
            engineDefs.add(engineDef);
            BundleDef bundleDef = controller.getBundleDef(engineDef
                    .getBundleId());
            LOG.trace("adding engineDef '{}' with bundleDef '{}'", engineDef,
                    bundleDef);
            validateNotNull(bundleDef,
                    "could not find bundle '" + engineDef.getBundleId()
                            + "' that is required by engine '" + engineDef
                            + "'");
            bundleDefs.add(bundleDef);
            for (Entry<String, String> id_url : bundleDef.getRepositories()
                    .entrySet()) {
                repositoriesDefs.put(id_url.getKey(), id_url.getValue());
            }
        }

        // 4. solve library dependencies
        try {
            solveDependencies(bundleDefs, pipelineDef.getName(),
                    pipelineDef.getVersion(), repositoriesDefs,
                    engineDefs.size());
        } catch (ArtifactResolutionException e) {
            throw new ValidationException(e.getMessage(), e);
        } catch (DependencyCollectionException e) {
            throw new ValidationException("could not collect dependency: "
                    + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e); // should not happen
        }

        // 5. create UimaPipeline and add components
        UimaPipeline uimaPipeline;
        try {
            uimaPipeline = new UimaPipeline(pipelineDef.getId(),
                    pipelineDef.getLanguage(), engineDefs,
                    pipelineDef.getScriptLines());
        } catch (ResourceInitializationException | IOException e) {
            throw new ValidationException(
                    "could not initialize UIMA pipeline: " + e.getMessage(), e);
        }

        // 6. set annotations to UimaPipeline output
        for (String typeShortName : pipelineDef.getOutput().getAnnotations()) {
            TypeDef typeDef = controller.getTypeDef(typeShortName);
            typeDef.validate(uimaPipeline.getTypeSystemDescription());
            validateNotNull(typeDef, "could not find bundle '" + typeShortName
                    + "' that is required by pipeline '" + pipelineDef.getId()
                    + "'");
            uimaPipeline.addOutputAnnotation(
                    typeDef.getClassz(),
                    typeDef.getProperties().toArray(
                            new String[typeDef.getProperties().size()]));
        }

        // 7. initialize UIMA pipeline and cache it
        try {
            uimaPipeline.initialize();
        } catch (UIMAException e) {
            throw new ValidationException(
                    "could not initialize UIMA pipeline '" + uimaPipeline
                            + "': " + e.getMessage(), e);
        }
        return uimaPipeline;
    }

    private void solveDependencies(Set<BundleDef> bundleDefs,
            String pipelineName, String version,
            Map<String, String> repositoriesDefs, int nrEngines)
            throws IOException, TemplateException, ArtifactResolutionException,
            DependencyCollectionException, IOException, ValidationException {

        // create fake POM from bundles and copy it
        String fakePom = MavenPom.writePom(bundleDefs, pipelineName, version);

        // solve dependecies
        RepositorySystem system = AetherResolver.newRepositorySystem();
        RepositorySystemSession session = AetherResolver
                .newRepositorySystemSession(system);

        Artifact rootArtifact = new DefaultArtifact(fakePom);
        LOG.trace("* rootArtifact: '{}'", rootArtifact);

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
        List<RemoteRepository> repos = AetherResolver.newRepositories(system,
                session, repositoriesDefs);
        List<Dependency> dependencies = p.getDependencies(true);
        if (nrEngines > 0) {
            // TODO better validation of pom
            validateArgument(dependencies.size() > 1,
                    "There must have been an error resolving dependencies");
        }
        for (Dependency dependency : p.getDependencies(true)) {

            Artifact artifact = dependency.getArtifact();
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(repos);

            ArtifactResult artifactResult = system.resolveArtifact(session,
                    artifactRequest);

            artifact = artifactResult.getArtifact();

            if (AetherResolver.localRepo.exists()
                    && AetherResolver.localRepo.canWrite()) {
                // TODO add to local repo
            }

            // add this jar to the classpath (if it has not been added before)
            if (!isAlreadyOnClasspath(artifact.getFile())) {
                ClassPathHack.addFile(artifact.getFile());
                LOG.trace("* resolved artifact '{}', added to classpath: '{}'",
                        artifact, artifact.getFile().getAbsolutePath());
            } else {
                LOG.trace("* resolved artifact '{}', already on classpath",
                        artifact);
            }
        }
    }

    private boolean isAlreadyOnClasspath(File jar) throws IOException {

        if (!FilenameUtils.getExtension(jar.getName()).equals("jar")) {
            return false;

        } else {
            // LOG.trace("jar: " + jar.getName());
            double exists = 0, new_ = 0;
            for (String className : getClasses(jar)) {
                try {
                    Class.forName(className);
                    exists++;
                } catch (Throwable e) {
                    new_++;
                    // LOG.trace("new::" + className);
                }
                if (exists + new_ > 20) {
                    break;
                }
            }
            LOG.trace("new {} exists {}", new_, exists);
            if (exists > 0)
                return true;
            else
                return false;
        }
    }

    private List<String> getClasses(File jar) throws IOException {

        List<String> classNames = new ArrayList<String>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream(jar));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip
                .getNextEntry()) {
            if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                StringBuilder className = new StringBuilder();
                for (String part : entry.getName().split("/")) {
                    if (className.length() != 0)
                        className.append(".");
                    className.append(part);
                    if (part.endsWith(".class"))
                        className.setLength(className.length()
                                - ".class".length());
                }
                classNames.add(className.toString());
            }
        }
        zip.close();
        return classNames;
    }

    /** reflection to bypass encapsulation */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static class ClassPathHack {
        private static final Class[] parameters = new Class[] { URL.class };

        public static void addFile(File f) throws IOException {
            addURL(f.toURI().toURL());
        }

        public static void addURL(URL u) throws IOException {
            URLClassLoader sysloader = (URLClassLoader) ClassLoader
                    .getSystemClassLoader();
            Class sysclass = URLClassLoader.class;

            try {
                Method method = sysclass
                        .getDeclaredMethod("addURL", parameters);
                method.setAccessible(true);
                method.invoke(sysloader, new Object[] { u });
            } catch (Throwable t) {
                t.printStackTrace();
                throw new IOException(
                        "Error, could not add URL to system classloader");
            }
        }
    }

    public void removeFromCache(String pipelineId) {
        uimaPipelinesCache.remove(pipelineId);
    }

    public void clearCache() {
        uimaPipelinesCache.clear();
    }
}
