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

import static org.apache.commons.lang3.StringUtils.join;
import static org.sherlok.mappings.Def.createId;
import static org.sherlok.mappings.Def.getName;
import static org.sherlok.mappings.Def.getVersion;
import static org.sherlok.utils.CheckThat.checkNotNull;
import static org.sherlok.utils.Create.list;
import static org.sherlok.utils.Create.map;
import static org.sherlok.utils.Create.set;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
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
import org.sherlok.aether.AetherResolver;
import org.sherlok.aether.ConsoleDependencyGraphDumper;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.MavenPom;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineEngine;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.sherlok.utils.Strings;
import org.sherlok.utils.ValidationException;

/**
 * Resolves the transitive dependencies of an artifact.
 */
public class PipelineLoader {

    private Controller controller;
    private Map<String, UimaPipeline> uimaPipelinesCache = map();
    private Set<String> jarsAddedToClasspath = set();

    public PipelineLoader(Controller controller) {
        this.controller = controller;
    }

    /**
     * @param pipelineName
     * @param version
     *            or ('null' or null) to try to fallback on the 'highest'
     *            version
     * @return the {@link UimaPipeline}
     */
    public UimaPipeline resolvePipeline(String pipelineName, String version)
            throws ValidationException {

        // 0. resolve (fallback) version=null
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
            version = highestVersion;
        }

        // 1. get pipeline from cache of components
        String pipelineId = createId(pipelineName, version);

        if (uimaPipelinesCache.containsKey(pipelineId)) {
            return uimaPipelinesCache.get(pipelineId);

        } else {
            // 2. else, load it
            // 2.1 read pipeline def
            PipelineDef pipelineDef = controller.getPipelineDef(pipelineId);
            checkNotNull(pipelineDef, "could not find Pipeline with Id '"
                    + pipelineId + "'");

            // 2.3 resolve engines (and their bundles)
            List<EngineDef> engineDefs = list();
            Set<BundleDef> bundleDefs = set();
            Map<String, String> repositoriesDefs = map();
            for (PipelineEngine pengine : pipelineDef.getEngines()) {

                if (pengine.isRuta()) { // Ruta engine
                    engineDefs.add(new EngineDef().setScript(pengine
                            .getScript()));

                } else { // uimaFIT engine
                    EngineDef engineDef = controller.getEngineDef(pengine
                            .getId());
                    checkNotNull(engineDef, "could not find " + pengine);
                    engineDefs.add(engineDef);
                    BundleDef bundleDef = controller.getBundleDef(engineDef
                            .getBundleId());
                    checkNotNull(bundleDef, "could not find bundle '"
                            + engineDef.getBundleId()
                            + "' that is required by engine '" + engineDef
                            + "'");
                    bundleDefs.add(bundleDef);
                    for (Entry<String, String> id_url : bundleDef
                            .getRepositories().entrySet()) {
                        repositoriesDefs
                                .put(id_url.getKey(), id_url.getValue());
                    }
                }
            }

            try {
                // 2.4 create fake POM from bundles and copy it
                String fakePom = MavenPom.writePom(bundleDefs, pipelineName,
                        version);

                // 2.4 solve dependecies
                solveDeps(fakePom, repositoriesDefs);

            } catch (ArtifactResolutionException e) {
                throw new ValidationException("could not resolve artifact: "
                        + e.getMessage(), e);
            } catch (DependencyCollectionException e) {
                throw new ValidationException("could not collect depenency: "
                        + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException(e);// should not happen
            }

            // 3 create UIMA pipeline and add components
            UimaPipeline uimaPipeline = new UimaPipeline(pipelineId,
                    pipelineDef.getLanguage());
            for (EngineDef engineDef : engineDefs) {
                try {
                    if (engineDef.isRuta()) { // Ruta engine
                        try {
                            uimaPipeline.addRutaEngine(engineDef.getScript(),
                                    pipelineId);
                        } catch (Exception e) {
                            throw new ValidationException(
                                    "could not parse script '"
                                            + join(engineDef.getScript(), ";")
                                            + "' from pipeline '" + pipelineId
                                            + "'", e);
                        }

                    } else { // UIMA engine
                        uimaPipeline.add(createEngine(engineDef.getClassz(),
                                engineDef.getFlatParams()));
                    }
                } catch (ResourceInitializationException e) {
                    throw new ValidationException(
                            "could not initialize UIMA pipeline engine '"
                                    + engineDef + "': " + e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    throw new ValidationException("could not find class '"
                            + engineDef.getClassz()
                            + "' to initialize UIMA pipeline engine '"
                            + engineDef + "'");
                }
            }

            // 3.2 set annotations to UIMA pipeline output
            for (String typeShortName : pipelineDef.getOutput()
                    .getAnnotations()) {
                TypeDef typeDef = controller.getTypeDef(typeShortName);
                typeDef.validate(uimaPipeline.getTypeSystemDescription());
                checkNotNull(typeDef, "could not find bundle '" + typeShortName
                        + "' that is required by pipeline '" + pipelineId + "'");
                uimaPipeline.addOutputAnnotation(
                        typeDef.getClassz(),
                        typeDef.getProperties().toArray(
                                new String[typeDef.getProperties().size()]));
            }

            // 3.3 initialize UIMA pipeline and cache it
            try {
                uimaPipeline.initialize();
            } catch (UIMAException e) {
                throw new ValidationException(
                        "could not initialize UIMA pipeline '" + uimaPipeline
                                + "': " + e.getMessage(), e);
            }
            uimaPipelinesCache.put(pipelineId, uimaPipeline);

            return uimaPipeline;
        }
    }

    private void solveDeps(String fakePom, Map<String, String> repositoriesDefs)
            throws ArtifactResolutionException, DependencyCollectionException,
            IOException {

        RepositorySystem system = AetherResolver.newRepositorySystem();
        RepositorySystemSession session = AetherResolver
                .newRepositorySystemSession(system);

        Artifact rootArtifact = new DefaultArtifact(fakePom);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(rootArtifact, ""));
        collectRequest.setRepositories(AetherResolver.newRepositories(system,
                session, new HashMap<String, String>()));

        CollectResult collectResult = system.collectDependencies(session,
                collectRequest);

        collectResult.getRoot().accept(new ConsoleDependencyGraphDumper());

        PreorderNodeListGenerator p = new PreorderNodeListGenerator();
        collectResult.getRoot().accept(p);
        List<RemoteRepository> repos = AetherResolver.newRepositories(system,
                session, repositoriesDefs);
        for (Dependency dependency : p.getDependencies(true)) {

            Artifact artifact = dependency.getArtifact();
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(repos);

            ArtifactResult artifactResult = system.resolveArtifact(session,
                    artifactRequest);

            artifact = artifactResult.getArtifact();
            String id = artifact.toString();

            // add this jar to the classpath (if it has not been added before)
            if (!jarsAddedToClasspath.contains(id)) {
                ClassPathHack.addFile(artifact.getFile());
                jarsAddedToClasspath.add(id);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static AnalysisEngineDescription createEngine(String cName,
            Object... params) throws ClassNotFoundException,
            ResourceInitializationException {

        // instantiate class
        Class<? extends AnalysisComponent> classz = (Class<? extends AnalysisComponent>) Class
                .forName(cName);
        // create ae
        AnalysisEngineDescription aed = AnalysisEngineFactory
                .createEngineDescription(classz, params);

        return aed;
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
}
