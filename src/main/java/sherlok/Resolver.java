/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package sherlok;

import static ch.epfl.bbp.collections.Create.map;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

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
import org.xml.sax.SAXException;

import sherlok.aether.Booter;
import sherlok.aether.ConsoleDependencyGraphDumper;
import ch.epfl.bbp.collections.Create;

/**
 * Collects the transitive dependencies of an artifact.
 */
public class Resolver {

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

    private void solveDeps(String fakePom) throws ArtifactResolutionException,
            DependencyCollectionException, IOException {

        RepositorySystem system = Booter.newRepositorySystem();
        RepositorySystemSession session = Booter
                .newRepositorySystemSession(system);

        Artifact rootArtifact = new DefaultArtifact(fakePom);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(rootArtifact, ""));
        collectRequest.setRepositories(Booter.newRepositories(system, session));

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
                    session));

            ArtifactResult artifactResult = system.resolveArtifact(session,
                    artifactRequest);
            System.out.println("RESOLVED:: " + artifactResult.isResolved());

            artifact = artifactResult.getArtifact();

            ClassPathHack.addFile(artifact.getFile());

            System.out
                    .println("FILE:: " + artifact.getFile().getAbsolutePath());
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

    public static class AED {
        String componentClass;
        Object[] params;

        public AED(String componentClass) {
            this.componentClass = componentClass;
            this.params = new Object[0];
        }

        public AED(String componentClass, Object... params) {
            this.componentClass = componentClass;
            this.params = params;
        }

        // AED add(Object param) {
        // params.add(param);
        // return this;
        // }

        public Object[] getParams() {
            // return params.toArray(new Object[params.size()]);
            return params;
        }
    }

    private static final String SEP = ":::";
    private Map<String, Pipeline> pipelineCache = map();

    public Pipeline resolve(String pipelineName, String version)
            throws UIMAException, ArtifactResolutionException,
            DependencyCollectionException, IOException, ClassNotFoundException,
            SAXException, CpeDescriptorException {

        // 1. get pipeline from cache of components
        if (pipelineCache.containsKey(pipelineName + SEP + version)) {
            return pipelineCache.get(pipelineName + SEP + version);

        } else {
            // 2. else, create it
            // 2.1 read pipeline def
            // 2.2 resolve engines
            List<AED> aeds = Create.list();
            aeds.add(new AED(
                    "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter"));
            aeds.add(new AED(
                    "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger"));
            aeds.add(new AED(
                    "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder",
                    "modelVariant", "person"));
            aeds.add(new AED(
                    "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder",
                    "modelVariant", "location"));

            // 2.3 resolve bundles

            // 2.4 create fake POM from bundles and copy it
            String fakePom = "sherlok:sherlok:pom:1";
            // 2.4 solve dependecies
            solveDeps(fakePom);

            // 3 create pipeline and add components
            Pipeline p = new Pipeline(pipelineName, version);
            for (AED aed : aeds) {
                p.add(createEngine(aed.componentClass, aed.getParams()));
            }

            // 3.2 set annotations to output
            p.addOutputAnnotation("de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity");

            // 3.3 initialize pipeline and cache it
            p.initialize();
            pipelineCache.put(pipelineName + SEP + version, p);
            return p;
        }
    }
}
