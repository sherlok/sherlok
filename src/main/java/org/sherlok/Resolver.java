package org.sherlok;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sherlok.mappings.PipelineDef.createId;
import static org.sherlok.mappings.PipelineDef.getName;
import static org.sherlok.mappings.PipelineDef.getVersion;
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
import org.apache.uima.collection.metadata.CpeDescriptorException;
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
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.EngineDef;
import org.sherlok.mappings.MavenPom;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineEngine;
import org.sherlok.utils.Strings;
import org.xml.sax.SAXException;

import sherlok.aether.Booter;
import sherlok.aether.ConsoleDependencyGraphDumper;
import freemarker.template.TemplateException;

/**
 * Resolves the transitive dependencies of an artifact.
 */
public class Resolver {

    private Store store;

    public Resolver() {
        this.store = new Store().load();
    }

    private Map<String, Pipeline> pipelineCache = map();

    public Pipeline resolve(String pipelineName, String version)
            throws UIMAException, ArtifactResolutionException,
            DependencyCollectionException, IOException, ClassNotFoundException,
            SAXException, CpeDescriptorException, TemplateException {

        // 0. resolve version=null
        if (version == null || version.equals("null")) {
            String highestVersion = "0000000000000000000000000000000";
            for (String pId : store.getPipelineDefNames()) {
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

        if (pipelineCache.containsKey(pipelineId)) {
            return pipelineCache.get(pipelineId);

        } else {
            // 2. else, load it
            // 2.1 read pipeline def
            PipelineDef pipelineDef = store.getPipelineDef(pipelineId);
            checkNotNull(pipelineDef, "could not find Pipeline with Id '"
                    + pipelineId + "'");

            // 2.3 resolve engines (and their bundles)
            List<EngineDef> engineDefs = list();
            Set<BundleDef> bundleDefs = set();
            Map<String, String> repositoriesDefs = map();
            for (PipelineEngine pengine : pipelineDef.getEngines()) {
                EngineDef engineDef = store.getEngineDef(pengine.getId());
                checkNotNull(engineDef, "could not find " + pengine);
                engineDefs.add(engineDef);
                BundleDef bundleDef = store.getBundleDef(engineDef
                        .getBundleId());
                checkNotNull(bundleDef, "could not find " + bundleDef);
                bundleDefs.add(bundleDef);
                for (Entry<String, String> id_url : bundleDef.getRepositories()
                        .entrySet()) {
                    repositoriesDefs.put(id_url.getKey(), id_url.getValue());
                }
            }

            // 2.4 create fake POM from bundles and copy it
            String fakePom = MavenPom.writePom(bundleDefs, pipelineName,
                    version);

            // 2.4 solve dependecies
            solveDeps(fakePom, repositoriesDefs);

            // 3 create pipeline and add components
            Pipeline pipeline = new Pipeline(pipelineId);
            for (EngineDef engineDef : engineDefs) {
                pipeline.add(createEngine(engineDef.getClassz(),
                        engineDef.getFlatParams()));
            }

            // 3.2 set annotations to output
            pipeline.addOutputAnnotation(
                    "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                    "value");
            // FIXME

            // 3.3 initialize pipeline and cache it
            pipeline.initialize();
            pipelineCache.put(pipelineId, pipeline);

            return pipeline;
        }
    }

    private void solveDeps(String fakePom, Map<String, String> repositoriesDefs)
            throws ArtifactResolutionException, DependencyCollectionException,
            IOException {

        RepositorySystem system = Booter.newRepositorySystem();
        RepositorySystemSession session = Booter
                .newRepositorySystemSession(system);

        Artifact rootArtifact = new DefaultArtifact(fakePom);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(rootArtifact, ""));
        collectRequest.setRepositories(Booter.newRepositories(system, session,
                new HashMap<String, String>()));

        CollectResult collectResult = system.collectDependencies(session,
                collectRequest);

        collectResult.getRoot().accept(new ConsoleDependencyGraphDumper());

        PreorderNodeListGenerator p = new PreorderNodeListGenerator();
        collectResult.getRoot().accept(p);
        for (Dependency dependency : p.getDependencies(true)) {

            Artifact artifact = dependency.getArtifact();
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(Booter.newRepositories(system,
                    session, repositoriesDefs));

            ArtifactResult artifactResult = system.resolveArtifact(session,
                    artifactRequest);
            // System.out.println("RESOLVED:: " + artifactResult.isResolved());

            artifact = artifactResult.getArtifact();

            ClassPathHack.addFile(artifact.getFile());

            // System.out
            // .println("FILE:: " + artifact.getFile().getAbsolutePath());
        }
    }

    @SuppressWarnings("unchecked")
    static AnalysisEngineDescription createEngine(String cName,
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
    public static class ClassPathHack {
        private static final Class[] parameters = new Class[] { URL.class };

        public static void addFile(String s) throws IOException {
            File f = new File(s);
            addFile(f);
        }

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

    public Store getStore() {
        return store;
    }

    public void removeFromCache(String pipelineId) {
        pipelineCache.remove(pipelineId);
    }
}
