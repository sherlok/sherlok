package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.sherlok.mappings.BundleDef.BundleDependency.DependencyType.mvn;

import java.io.File;

import org.junit.Test;
import org.sherlok.mappings.BundleDef.BundleDependency;

public class BundleTest {

    public static BundleDef getDkproOpennlpEn() {
        BundleDef b = new BundleDef()
                .setName("dkpro_opennlp_en")
                .setVersion("1.6.2")
                .setDescription("all opennlp engines and models for English")
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"))
                .addDependency(
                        new BundleDependency(mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.2"))
                .addDependency(
                        new BundleDependency(mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.2"))
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-model-ner-en-person:20130624.1"))
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-model-ner-en-organization:20100907.0"))
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-model-ner-en-location:20100907.0"))
                .addRepository(
                        "dkpro",
                        "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/");
        return b;
    }

    @Test
    public void testWriteRead() throws Exception {

        File bf = new File("target/bundleTest_" + currentTimeMillis() + ".json");
        BundleDef b = getDkproOpennlpEn();
        b.write(bf);
        BundleDef b2 = BundleDef.load(bf);
        assertEquals(b.getName(), b2.getName());
        assertEquals(b.getVersion(), b2.getVersion());
        assertEquals(b.getDependencies().size(), b2.getDependencies().size());
    }

    // TODO test parsing
}
