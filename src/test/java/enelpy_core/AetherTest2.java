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
import java.util.Arrays;
import java.util.Collection;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

public class AetherTest2 {
/*-
    @Test
    public void test() throws Exception {

        File local = new File("local_repo");

        Collection<RemoteRepository> remotes = Arrays
                .asList(new RemoteRepository("maven-central", "default",
                        "http://repo1.maven.org/maven2/"),
                        new RemoteRepository(
                                "dkpro",
                                "default",
                                "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local"));

        add(local, remotes, "de.tudarmstadt.ukp.dkpro.core",
                "de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl", "jar", "1.6.2");
        add(local, remotes, "de.tudarmstadt.ukp.dkpro.core",
                "de.tudarmstadt.ukp.dkpro.core.opennlp-asl", "jar", "1.6.2");

        add(local, remotes, "de.tudarmstadt.ukp.dkpro.core",
                "de.tudarmstadt.ukp.dkpro.core.maltparser-asl", "jar", "1.6.2");
        add(local,
                remotes,
                "de.tudarmstadt.ukp.dkpro.core",
                "de.tudarmstadt.ukp.dkpro.core.maltparser-upstream-parser-en-linear",
                "jar", "20120312");

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
    AnalysisEngineDescription createEngine(String cName, Object... params)
            throws ClassNotFoundException, ResourceInitializationException {

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

    private void add(File local, Collection<RemoteRepository> remotes,
            String groupId, String artifactId, String classifier, String version)
            throws DependencyResolutionException, IOException {

        Collection<Artifact> deps = new Aether(remotes, local).resolve(
                // new DefaultArtifact("junit", "junit-dep", "", "jar", "4.10"),
                new DefaultArtifact(groupId, artifactId, classifier, version),
                "runtime");

        for (Artifact artifact : deps) {
            System.out.println("LOADING:: " + artifact);
            ClassPathHack.addFile(artifact.getFile());
        }
    }

    / reflection to bypass encapsulation while using deprecated methods /
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

    // JarClassLoader jcl = new JarClassLoader();
    // jcl.add("myjar.jar"); //Load jar file
    // jcl.add(new URL("http://myserver.com/myjar.jar")); //Load jar from a URL
    // jcl.add(new FileInputStream("myotherjar.jar")); //Load jar file from
    // stream
    // jcl.add("myclassfolder/"); //Load class folder
    // jcl.add("myjarlib/"); //Recursively load all jar files in the
    // folder/sub-folder(s)
    //
    // JclObjectFactory factory = JclObjectFactory.getInstance();
    //
    // //Create object of loaded class
    // Object obj = factory.create(jcl,"mypackage.MyClass");
*/
}
