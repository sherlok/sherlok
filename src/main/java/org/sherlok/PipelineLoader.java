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

import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.sherlok.mappings.Def.createId;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.AetherResolver.LOCAL_REPO_PATH;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.CheckThat.validateNotNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.Create.set;
import static org.sherlok.utils.ValidationException.ERR;
import static org.sherlok.utils.ValidationException.MSG;
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

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
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
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.mappings.PipelineDef;
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

    private final Controller controller;
    /** caches resolved {@link UimaPipeline}s so can be reused for annotating */
    private Map<String, UimaPipeline> uimaPipelinesCache = map();

    public PipelineLoader(Controller controller) {
        this.controller = controller;
    }

    /**
     * Checks if such a {@link PipelineDef} exists, and returns the
     * corresponding {@link UimaPipeline} (if it is in cache) or else
     * instantiates it. <br/>
     * Instantiation involves resolving the pipeline engines and the engines
     * bundles, then loading the bundles Maven artifacts into the current
     * classpath. <br/>
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
            // init with a lexicographically lowsest value
            String highestVersion = "0000000000000000000000000000000";
            // find highest version
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
        String pipelineId = createId(pipelineName, version);

        // 1. get pipeline from cache if available
        if (uimaPipelinesCache.containsKey(pipelineId)) {
            LOG.trace("pipeline '{}' found in cache", pipelineId);
            return uimaPipelinesCache.get(pipelineId);

        } else {
            // 2. else, load it from pipeline def
            PipelineDef pipelineDef = controller.getPipelineDef(pipelineId);
            validateNotNull(pipelineDef, "could not find Pipeline with Id '"
                    + pipelineId + "'"); // should not happen here, though...

            UimaPipeline uimaPipeline = load(pipelineDef);
            uimaPipelinesCache.put(pipelineId, uimaPipeline);
            return uimaPipeline;
        }
    }

    /** Just loads that pipeline. No caching. */
    UimaPipeline load(PipelineDef pipelineDef) throws ValidationException {

        pipelineDef.validate("could not validate pipeline wit Id '"
                + pipelineDef.getId() + "',"); // just to make sure...

        // 3. create a list of engines (and their bundles) to resolve
        List<EngineDef> engineDefsUsedInP = list();
        Set<BundleDef> bundleDefsToResolve = set();
        for (String pengineId : pipelineDef.getEnginesFromScript()) {
            EngineDef en = controller.getEngineDef(pengineId);
            validateNotNull(en, "could not find engine '" + pengineId
                    + "' as defined in pipeline '" + pipelineDef.getId() + "'");
            BundleDef b = en.getBundle();
            validateNotNull(b, "could not find bundle '" + b
                    + "' that is required by engine '" + en + "'");
            LOG.trace("adding engineDef '{}' with bundleDef '{}'", en, b);
            engineDefsUsedInP.add(en);
            bundleDefsToResolve.add(b);
        }

        // 4. solve (download) bundle dependencies
        try {
            solveDependencies(pipelineDef.getName(), pipelineDef.getVersion(),
                    bundleDefsToResolve, engineDefsUsedInP.size());
        } catch (ArtifactResolutionException e) {
            throw new ValidationException(map(MSG, "Failed to load pipeline: "
                    + e.getMessage(), ERR, pipelineDef.getId()));
        } catch (DependencyCollectionException e) {
            throw new ValidationException("could not collect dependency: "
                    + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e); // should not happen
        }

        // 5. create UimaPipeline
        UimaPipeline uimaPipeline;
        try {
            uimaPipeline = new UimaPipeline(pipelineDef, engineDefsUsedInP);
        } catch (IOException | UIMAException e) {
            throw new ValidationException(
                    "could not initialize UIMA pipeline: " + e.getMessage(), e);
        }

        return uimaPipeline;
    }

    public void cleanLocalRepo() throws IOException {
        FileUtils.deleteDirectory(new File(AetherResolver.LOCAL_REPO_PATH));
    }

    /**
     * Resolves a maven dependency tree, download the dependencies and add them
     * (jars) to the classpath
     */
    static void solveDependencies(String pipelineName, String version,
            Set<BundleDef> bundleDefs, int nrEngines) throws IOException,
            TemplateException, ArtifactResolutionException,
            DependencyCollectionException, IOException, ValidationException {

        // create fake POM that contains all bundle deps
        String fakePom = MavenPom.writePom(bundleDefs, pipelineName, version);
        Artifact rootArtifact = new DefaultArtifact(fakePom);
        LOG.trace("* rootArtifact: '{}'", rootArtifact);

        // repositorysystem, with our remote repository urls
        RepositorySystem system = AetherResolver.newRepositorySystem();
        RepositorySystemSession session = AetherResolver
                .newRepositorySystemSession(system, LOCAL_REPO_PATH);
        Map<String, String> repositoriesDefs = map();
        for (BundleDef b : bundleDefs) {
            for (Entry<String, String> id_url : b.getRepositories().entrySet()) {
                repositoriesDefs.put(id_url.getKey(), id_url.getValue());
            }
        }
        List<RemoteRepository> repos = AetherResolver.newRepositories(system,
                session, repositoriesDefs);

        // solve dependencies
        CollectResult collectResult = system.collectDependencies(
                session,
                new CollectRequest(new Dependency(rootArtifact, ""),
                        AetherResolver.newRepositories(system, session,
                                new HashMap<String, String>())));
        collectResult.getRoot().accept(
                new AetherResolver.ConsoleDependencyGraphDumper());
        PreorderNodeListGenerator p = new PreorderNodeListGenerator();
        collectResult.getRoot().accept(p);
        List<Dependency> dependencies = p.getDependencies(true);
        // validate for syntax problems in pom (can go unnoticed otherwise)
        if (nrEngines > 0) {
            // TODO better validation of pom
            validateArgument(dependencies.size() > 1,
                    "There must have been an error resolving dependencies");
        }

        // now do the real fetching, and add jars to classpath
        for (Dependency dependency : dependencies) {
            Artifact resolvedArtifact = system.resolveArtifact(session,
                    new ArtifactRequest(dependency.getArtifact(), repos, ""))
                    .getArtifact();
            File jar = resolvedArtifact.getFile();

            // add downloaded artifact to local ~/.m2/repository, if possible
            // FIXME test that downloaded artifact to local ~/.m2/repository
            if (AetherResolver.localRepo.exists()
                    && AetherResolver.localRepo.canWrite()) {
                File sherlokRepo = new File(AetherResolver.LOCAL_REPO_PATH);
                String canonicalPath = jar.getCanonicalPath();
                String relative = canonicalPath.substring(sherlokRepo
                        .getAbsolutePath().length(), canonicalPath.length());
                File localRepoFile = new File(AetherResolver.localRepo,
                        relative);
                if (!localRepoFile.exists()) {
                    LOG.trace("artifact '{}' added to local maven repo",
                            jar.getName());
                    copyFile(jar, localRepoFile);
                }
            }

            // add this jar to the classpath (if it has not been added before)
            if (!isAlreadyOnClasspath(jar)) {
                ClassPathHack.addFile(jar);
                LOG.trace("* resolved artifact '{}', added to classpath: '{}'",
                        resolvedArtifact, jar.getAbsolutePath());
            } else {
                LOG.trace("* resolved artifact '{}', already on classpath",
                        resolvedArtifact);
            }
        }
    }

    /**
     * Samples classes from that jar, to see if they are already on the
     * classpath. If some classes are already, then assume this jar was already
     * on the classpath.<br>
     * The goal is to avoid overloading the classpath...
     */
    private static boolean isAlreadyOnClasspath(File jar) throws IOException {

        if (!getExtension(jar.getName()).equals("jar")) { // filter poms
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
                if (exists + new_ > 20) {// we have sampled enough classes
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

    /** Get all classes from this jar file */
    private static List<String> getClasses(File jar) throws IOException {
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

    /** Reflection to bypass encapsulation. Yeah... */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class ClassPathHack {
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

    /** clears (flushes) that pipeline from the cache */
    public void removeFromCache(String pipelineId) {
        uimaPipelinesCache.remove(pipelineId);
    }

    /** clears (flushes) all cached pipelines */
    public void clearCache() {
        uimaPipelinesCache.clear();
    }
}
