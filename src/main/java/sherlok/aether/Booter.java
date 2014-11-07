package sherlok.aether;

import static ch.epfl.bbp.collections.Create.list;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

        // session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories(
            RepositorySystem system, RepositorySystemSession session,
            Map<String, String> otherRepos) {
        List<RemoteRepository> repos = list(newCentralRepository());// ,
        for (Entry<String, String> id_url : otherRepos.entrySet()) {
            repos.add(new RemoteRepository.Builder(id_url.getKey(), "default",
                    id_url.getValue()).build());
        }
        return repos;
    }

    private static RemoteRepository newCentralRepository() {
        return new RemoteRepository.Builder("central", "default",
                "http://central.maven.org/maven2/").build();
    }
}
