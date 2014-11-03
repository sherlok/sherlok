package org.sherlok;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ResolverTest {

    @Test
    public void test() throws Exception {

        //wipeRepo();
        
        File dir = new File("local_repo/sherlok/sherlok/1");
        dir.mkdirs();
        FileUtils.copyFile(new File("sherlok-default-1.pom"), new File("local_repo/sherlok/sherlok/1/sherlok-1.pom"));
        

        Pipeline pipeline = new Resolver().resolve("default", "1");
        System.out
                .println(pipeline
                        .annotate("Jack Burton (born April 29, 1954 in El Paso), also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards. "));
    }

    public static void wipeRepo() throws IOException {
        deleteDirectory(new File(Sherlok.LOCAL_REPO_PATH));
    }
}
