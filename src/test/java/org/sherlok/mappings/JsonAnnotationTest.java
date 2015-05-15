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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.sherlok.utils.Create.map;

import org.junit.Test;

public class JsonAnnotationTest {

    @Test
    public void testEquals() {

        assertEquals("equals since begin/end defaults to 0",
                new JsonAnnotation(), //
                new JsonAnnotation().setBegin(0));
        assertEquals(new JsonAnnotation().setBegin(1).setEnd(2), //
                new JsonAnnotation().setBegin(1).setEnd(2));

        // begin end
        assertNotEquals(new JsonAnnotation().setBegin(0).setEnd(2), //
                new JsonAnnotation().setBegin(1).setEnd(2));
        assertNotEquals(new JsonAnnotation().setBegin(0), //
                new JsonAnnotation().setBegin(1));

        // properties
        assertEquals(new JsonAnnotation().addProperty("a", 1), //
                new JsonAnnotation().addProperty("a", 1));
        assertNotEquals("different prop value",
                new JsonAnnotation().addProperty("a", 1), //
                new JsonAnnotation().addProperty("a", 2));
        assertNotEquals("different prop name",
                new JsonAnnotation().addProperty("a", 1), //
                new JsonAnnotation().addProperty("b", 1));
        assertNotEquals(new JsonAnnotation().addProperty("a", 1), //
                new JsonAnnotation());
        assertEquals("same prop twice is ok/equals", //
                new JsonAnnotation().addProperty("a", 1), //
                new JsonAnnotation().addProperty("a", 1).addProperty("a", 1));
        assertEquals("remove property", //
                new JsonAnnotation().addProperty("a", 1).removeProperty("a"), //
                new JsonAnnotation());

        // more complex property types
        assertEquals(new JsonAnnotation().addProperty("a", "aa"), //
                new JsonAnnotation().addProperty("a", "aa"));
        assertNotEquals("different prop value", //
                new JsonAnnotation().addProperty("a", "aa"), //
                new JsonAnnotation().addProperty("a", "ab"));
        assertEquals(new JsonAnnotation().addProperty("a", 1f), //
                new JsonAnnotation().addProperty("a", 1f));
        assertNotEquals("float != double", //
                new JsonAnnotation().addProperty("a", 1f), //
                new JsonAnnotation().addProperty("a", 1d));
        assertEquals("maps",
                new JsonAnnotation().addProperty("a", map("1", 2)),
                new JsonAnnotation().addProperty("a", map("1", 2)));
    }
}
