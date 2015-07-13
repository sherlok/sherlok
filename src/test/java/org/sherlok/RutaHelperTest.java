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
package org.sherlok;

import static org.junit.Assert.assertEquals;
import static org.sherlok.RutaHelper.parseDeclaredTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.sherlok.RutaHelper.TypeDTO;
import org.sherlok.RutaHelper.TypeFeatureDTO;
import org.sherlok.mappings.SherlokException;

public class RutaHelperTest {

    @Test
    public void testEmpty() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("PACKAGE oh;");
        assertEquals(0, types.size());
        types = parseDeclaredTypes("DECLARE;");
        assertEquals(0, types.size());
        types = parseDeclaredTypes("Declare;");
        assertEquals(0, types.size());
        types = parseDeclaredTypes("DECLARE Dog");
        assertEquals("missing ;, but that is ok", 1, types.size());
    }

    @Test
    public void testBasic() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("PACKAGE oh;\nDECLARE Dog;\nW{REGEXP(\"dog\") -> MARK(Dog)};");
        assertEquals(1, types.size());
        TypeDTO t = types.iterator().next();
        assertEquals("Dog", t.typeName);
    }

    @Test
    public void testComment() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("PACKAGE oh;\nDECLARE//blah\nDog;");
        assertEquals(1, types.size());
        TypeDTO t = types.iterator().next();
        assertEquals("Dog", t.typeName);

        types = parseDeclaredTypes("PACKAGE oh;\nDECLARE Dog;//blah");
        assertEquals(1, types.size());
    }

    @Test
    public void testInline() throws Exception {
        List<TypeDTO> types = new ArrayList<TypeDTO>(
                parseDeclaredTypes("DECLARE Dog, Car, Home;"));
        assertEquals(3, types.size());
        TypeDTO t1 = types.get(1);
        assertEquals("Car", t1.typeName);
        TypeDTO t2 = types.get(2);
        assertEquals("Home", t2.typeName);
    }

    @Test
    public void testSuper() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("DECLARE Aaa Bbb;");
        assertEquals(1, types.size());
        TypeDTO t = types.iterator().next();
        assertEquals("Aaa", t.supertypeName);
        assertEquals("Bbb", t.typeName);
    }

    @Test
    public void testDuplicates() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("DECLARE Aaa Bbb;\nDECLARE Aaa Bbb;");
        assertEquals(1, types.size());
        TypeDTO t = types.iterator().next();
        assertEquals("Aaa", t.supertypeName);
        assertEquals("Bbb", t.typeName);
    }

    @Test
    public void testMultiple() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("DECLARE Aaa;\nDECLARE Bbb;\nDECLARE Ccc;\n\nDECLARE Ddd, Eee;\n");
        assertEquals(5, types.size());
    }

    @Test
    public void testFeatures() throws Exception {
        Set<TypeDTO> types = parseDeclaredTypes("DECLARE Aaa  Bbb  ( INT  cc);");
        assertEquals(1, types.size());
        TypeDTO t = types.iterator().next();
        assertEquals(1, t.getTypeFeatures().size());
        TypeFeatureDTO tf = t.getTypeFeatures().get(0);
        assertEquals("cc", tf.featureName);
        assertEquals("INT", tf.rangeTypeName);
    }

    @Test
    public void testFeatures2() throws Exception {
        List<TypeDTO> types = new ArrayList<TypeDTO>(
                parseDeclaredTypes("DECLARE Aa (INT  cc, Blah dd);"));
        assertEquals(1, types.size());
        TypeDTO t = types.get(0);
        assertEquals(2, t.getTypeFeatures().size());
        TypeFeatureDTO tf = t.getTypeFeatures().get(0);
        assertEquals("cc", tf.featureName);
        assertEquals("INT", tf.rangeTypeName);
        TypeFeatureDTO tf2 = t.getTypeFeatures().get(1);
        assertEquals("dd", tf2.featureName);
        assertEquals("Blah", tf2.rangeTypeName);
    }

    @Test(expected = SherlokException.class)
    public void testFeatures3() throws Exception {
        parseDeclaredTypes("DECLARE Aa (INT);");
    }

    @Test(expected = SherlokException.class)
    public void testFeatures4() throws Exception {
        parseDeclaredTypes("DECLARE Aa (INT asf cc, Blah dd);");
    }
}
