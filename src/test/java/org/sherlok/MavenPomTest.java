package org.sherlok;

import static org.sherlok.utils.Create.set;

import org.junit.Test;
import org.sherlok.Controller;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.MavenPom;

public class MavenPomTest {

    @Test
    public void test() throws Exception {

        BundleDef bundle = new Controller().load().getBundleDef(
                "dkpro.opennlp.en:1.6.2");
        MavenPom.writePom(set(bundle), "test", "1");
        // TODO assertions
    }
}
