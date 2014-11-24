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
package org.sherlok.aether;

import static org.sherlok.utils.Create.list;
import static org.slf4j.LoggerFactory.getLogger;

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
import org.slf4j.Logger;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class AetherResolver {
    private static final Logger LOG = getLogger(AetherResolver.class);

    /** Where to store the local artifacts */
    public static final String LOCAL_REPO_PATH = "local_repo";

    public static RepositorySystem newRepositorySystem() {
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
                LOG.error("Aether serviceCreation failed", exception);
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
        // Sherlok repo
        repos.add(new RemoteRepository.Builder("sherlok_deps", "default",
                "https://raw.githubusercontent.com/renaud/sherlok_mavenrepo/master/")
                .build());
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
