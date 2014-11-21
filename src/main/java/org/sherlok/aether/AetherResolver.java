package org.sherlok.aether;

import static org.sherlok.utils.Create.list;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class AetherResolver {

    public static final String LOCAL_REPO_PATH = "local_repo";

    public static RepositorySystem newRepositorySystem() {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service
         * to ease manual wiring and using the prepopulated
         * DefaultServiceLocator, we only need to register the repository
         * connector and transporter factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils
                .newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class,
                FileTransporterFactory.class);
        locator.addService(TransporterFactory.class,
                HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl,
                    Throwable exception) {
                exception.printStackTrace();
            }
        });

        return locator.getService(RepositorySystem.class);
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession(
            RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils
                .newSession();

        LocalRepository localRepo = new LocalRepository(LOCAL_REPO_PATH);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(
                session, localRepo));

        // session.setTransferListener(new ConsoleTransferListener());
        // session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories(
            RepositorySystem system, RepositorySystemSession session,
            Map<String, String> otherRepos) throws MalformedURLException {

        List<RemoteRepository> repos = list();
        // local repo (added as remote)
        File localRepo = new File(System.getProperty("user.home")
                + "/.m2/repository/");
        if (localRepo.exists()) {
            repos.add(new RemoteRepository.Builder("local_default", "default",
                    localRepo.toURI().toURL().toString()).build());
        }
        // Maven central
        repos.add(new RemoteRepository.Builder("central", "default",
                "http://central.maven.org/maven2/").build());

        for (Entry<String, String> id_url : otherRepos.entrySet()) {
            repos.add(new RemoteRepository.Builder(id_url.getKey(), "default",
                    id_url.getValue()).build());
        }
        return repos;
    }

}
