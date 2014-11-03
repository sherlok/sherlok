package sherlok.aether;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.sherlok.Sherlok;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class Booter {

    public static RepositorySystem newRepositorySystem() {
        return sherlok.aether.ManualRepositorySystemFactory
                .newRepositorySystem();
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession(
            RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils
                .newSession();

        LocalRepository localRepo = new LocalRepository(Sherlok.LOCAL_REPO_PATH);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(
                session, localRepo));

      //  session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories(
            RepositorySystem system, RepositorySystemSession session) {
        return new ArrayList<RemoteRepository>(Arrays.asList(
                newCentralRepository(), dkproCentralRepository()));
    }

    private static RemoteRepository newCentralRepository() {
        return new RemoteRepository.Builder("central", "default",
                "http://central.maven.org/maven2/").build();
    }

    private static RemoteRepository dkproCentralRepository() {
        // FIXME add somewhere else
        return new RemoteRepository.Builder(
                "dkpro",
                "default",
                "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/")
                .build();
    }
}
