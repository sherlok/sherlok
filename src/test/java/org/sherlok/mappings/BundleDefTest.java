/**
 * Copyright (C) 2014-2015 Renaud Richardet
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
package org.sherlok.mappings;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.sherlok.FileBased.read;
import static org.sherlok.mappings.BundleDef.BundleDependency.DependencyType.jar;
import static org.sherlok.mappings.BundleDef.BundleDependency.DependencyType.mvn;
import static org.sherlok.utils.Create.set;

import java.io.File;
import java.util.Set;

import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.mappings.BundleDef.BundleDependency;
import org.sherlok.mappings.BundleDef.EngineDef;
import org.sherlok.utils.ValidationException;

public class BundleDefTest {

    public static BundleDef getDkproOpennlpEn() {
        BundleDef b = (BundleDef) new BundleDef()
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"))
                .addDependency(
                        new BundleDependency(
                                mvn,
                                "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-model-ner-en-organization:20100907.0"))
                .addRepository(
                        "dkpro",
                        "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/");
        b.setName("dkpro_opennlp_en");
        b.setVersion("1.6.2");
        b.setDescription("all opennlp engines and models for English");
        return b;
    }

    @Test
    public void testWriteRead() throws Exception {

        File bf = new File("target/bundleTest_" + currentTimeMillis() + ".json");
        BundleDef b = getDkproOpennlpEn();
        FileBased.write(bf, b);
        BundleDef b2 = read(bf, BundleDef.class);
        b2.validate("");
        assertEquals(b.getName(), b2.getName());
        assertEquals(b.getVersion(), b2.getVersion());
        assertEquals(b.getDependencies().size(), b2.getDependencies().size());
    }

    // TODO test BundleDef.validate()

    @Test
    public void testValidateBundle() throws Exception {
        new BundleDef().setName("a").setVersion("b").validate("");
    }

    @Test(expected = ValidationException.class)
    // $ in domain
    public void testValidateBundle1() throws Exception {
        new BundleDef().setName("a").setVersion("b").setDomain("a$b")
                .validate("");
    }

    @Test
    public void testValidateBundle2() throws Exception {
        new BundleDef().setName("a").setVersion("b").setDomain("a/b")
                .validate("");
    }

    @Test
    public void testBundleDependencyEquality() throws Exception {
        Set<BundleDependency> bdl = set();
        bdl.add(new BundleDependency(mvn, "abc"));
        bdl.add(new BundleDependency(jar, "abc"));
        assertEquals("distinct DependencyTypes", 2, bdl.size());
        bdl.add(new BundleDependency(jar, "abc"));
        assertEquals("same DependencyTypes, should not increase the set", 2,
                bdl.size());
    }

    @Test
    public void testEngineNameFallback() throws Exception {
        assertEquals("MyName", new EngineDef().setClassz("a.b.c.Dclass")
                .setName("MyName").getName());
        assertEquals("if no name is provided, falls back on simple class name",
                "Dclass", new EngineDef().setClassz("a.b.c.Dclass").getName());
        assertEquals("if no name is provided, falls back on simple class name",
                "Dclass", new EngineDef().setClassz("Dclass").getName());
    }
    @Test
    public void testCreateId() throws Exception {
        assertEquals("a:b", Def.createId("a", "b"));
    }
}
