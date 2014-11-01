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
package enelpy_core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.selectAll;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.examples.util.Booter;
import org.eclipse.aether.examples.util.ConsoleDependencyGraphDumper;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

/**
 * Collects the transitive dependencies of an artifact.
 */
public class GetDependencyTreeMine3 {

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

    private static void solveDeps() throws ArtifactResolutionException,
            DependencyCollectionException, IOException {

        RepositorySystem system = Booter.newRepositorySystem();
        RepositorySystemSession session = Booter
                .newRepositorySystemSession(system);

        Artifact rootArtifact = new DefaultArtifact(
        // "org.apache.maven:maven-aether-provider:3.1.0");
        // "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.2");
                "enelpy:enelpy:pom:1");

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

    public static void main(String[] args) throws Exception {

        // well-behaved Java packages work relative to the
        // context classloader. Others don't (like commons-logging)
        solveDeps();

        AnalysisEngineDescription seg = createEngine("de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter");
        AnalysisEngineDescription pos = createEngine("de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger");

        // AnalysisEngineDescription parser = createEngineDescription(
        // MaltParser.class, PARAM_LANGUAGE, "en", PARAM_VARIANT, "linear");

        AnalysisEngineDescription printDeps = createEngineDescription(PrintDeps.class);

        // JCas jCas = UimaUtils.getCas("The cat sits on the mat.");
        // runPipeline(jCas.getCas(), seg, pos, parser, printDeps);
        CollectionReaderDescription cr = createReaderDescription(SingleAbstractReader.class);
        runPipeline(cr, seg, pos, printDeps);// , parser);
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

    public static class PrintDeps extends JCasAnnotator_ImplBase {

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {

            for (TOP a : selectAll(jCas)) {
                System.out.println(a);
            }

            // for (Dependency dep : select(jCas, Dependency.class)) {
            // System.out.println("dep: [" + dep.getDependencyType()
            // + "] \t gov: [" + dep.getGovernor().getCoveredText()
            // + "] \t dep: [" + dep.getDependent().getCoveredText()
            // + "]");
            // }
        }
    }

}
