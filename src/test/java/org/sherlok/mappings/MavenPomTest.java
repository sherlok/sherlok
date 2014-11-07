package org.sherlok.mappings;

import static ch.epfl.bbp.collections.Create.set;
import static org.junit.Assert.*;

import org.junit.Test;

import ch.epfl.bbp.collections.Create;

public class MavenPomTest {

    @Test
    public void test() throws Exception {

        BundleDef bundle = new Store().load().getBundleDef(
                "dkpro_opennlp_en:1.6.2");
        MavenPom.writePom(set(bundle), "test", "1");
    }
}
