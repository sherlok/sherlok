package org.sherlok;

import static org.sherlok.utils.Create.map;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.sherlok.PipelineLoader.ClassPathHack;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.BundleDependency;
import org.sherlok.mappings.BundleDef.BundleDependency.DependencyType;
import org.sherlok.utils.AetherResolver;
import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

import freemarker.template.TemplateException;

/**
 * Util to generate bundle JSON from Maven artifacts
 * 
 * <code>-XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512M</code>
 * 
 * */
public class BundleCreator {
    private static Logger LOG = getLogger(BundleCreator.class);

    public static void main(String[] args) throws Exception {

        BundleDef b = new BundleDef();

        b.addRepository(
                "dkpro",
                "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/");

        b.addDependency(new BundleDependency(
                DependencyType.mvn,
                // "junit:junit:3.7"));
                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"));

        solveDependencies2(b);
    }

    static String localRepo = "target/BundleRepo";
    static {
        new File(localRepo).getParentFile().mkdirs();
    }

    static void solveDependencies2(BundleDef b) throws IOException,
            TemplateException, ArtifactResolutionException,
            DependencyCollectionException, IOException, ValidationException {

        RepositorySystem system = AetherResolver.newRepositorySystem();
        RepositorySystemSession session = AetherResolver
                .newRepositorySystemSession(system, localRepo);

        Map<String, String> repositoriesDefs = map();
        for (Entry<String, String> id_url : b.getRepositories().entrySet()) {
            repositoriesDefs.put(id_url.getKey(), id_url.getValue());
        }
        List<RemoteRepository> repos = AetherResolver.newRepositories(system,
                session, repositoriesDefs);

        for (BundleDependency bd : b.getDependencies()) {

            ArtifactRequest artifactRequest = new ArtifactRequest();
            String aId = bd.getGroupId() + ":" + bd.getArtifactId() + ":jar:"
                    + bd.getVersion();
            artifactRequest.setArtifact(new DefaultArtifact(aId));
            artifactRequest.setRepositories(repos);
            ArtifactResult artifactResult = system.resolveArtifact(session,
                    artifactRequest);

            File jar = artifactResult.getArtifact().getFile();
            System.out.println(jar);

            
            ClassPathHack.addFile(jar);
            
            JarInputStream is = new JarInputStream(new FileInputStream(jar));
            JarEntry entry;
            while ((entry = is.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    // System.out.println(entry.getName());

                    try {

                        Class<?> clazz = Class.forName(entry.getName()
                                .replace('/', '.').replace(".class", ""));

                        
                        //clazz.
                        
                        // all uimafit AnnotationEngines
                        if (JCasAnnotator_ImplBase.class
                                .isAssignableFrom(clazz)) {
                            LOG.debug("AnnotationEngine: {}",
                                    clazz.getSimpleName());

                        }
                   // } catch (java.lang.NoClassDefFoundError
                     //       | ClassNotFoundException n) {
                        

                    } catch (Throwable e) {
                        System.err.println(entry.getName() + " " + e);
                    }
                }
            }
            is.close();
        }
    }
}
