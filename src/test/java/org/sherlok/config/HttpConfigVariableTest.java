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
package org.sherlok.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.utils.ops.FileOps;

public class HttpConfigVariableTest {

    private static final String VALID_URL_1 = "https://raw.githubusercontent.com/sherlok/sherlok_dependency_test/master/resources/file.txt";
    private static final String VALID_URL_2 = "https://raw.githubusercontent.com/sherlok/sherlok_dependency_test/develop/resources/file.txt";
    private static final String FILENAME = "file.txt";

    private static final String INVALID_URL = "http://bad_example";

    private static final String FILE_CONTENT_1 = "MASTER\n";
    private static final String FILE_CONTENT_2 = "DEVELOP\n";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Remove any cached repository
        HttpConfigVariable.cleanCache();
    }

    @Test
    public final void testGetProcessedValueRutaCompatible()
            throws ProcessConfigVariableException, IOException {
        testGetProcessedValueImpl(VALID_URL_1, FILE_CONTENT_1, true);
    }

    @Test
    public final void testGetProcessedValue()
            throws ProcessConfigVariableException, IOException {
        testGetProcessedValueImpl(VALID_URL_2, FILE_CONTENT_2, false);
    }

    @Test(expected = ProcessConfigVariableException.class)
    public final void testGetProcessedValueInvalidURL()
            throws ProcessConfigVariableException {
        HttpConfigVariable var = new HttpConfigVariable(INVALID_URL, true);
        var.getProcessedValue(); // should fire
    }

    private void testGetProcessedValueImpl(String url, String expectedContent,
            Boolean rutaMode) throws ProcessConfigVariableException,
            IOException {
        HttpConfigVariable var = new HttpConfigVariable(url, rutaMode);
        String val = var.getProcessedValue();
        assertNotNull(val);

        String content = getTestFileContent(val, rutaMode); // should not throw
        assertEquals("checking content", expectedContent, content);

        assertTrue("the filename should be correct", val.endsWith(FILENAME));
    }

    private static String getTestFileContent(String processedValue,
            Boolean rutaCompatible)
            throws IOException {
        File file = null;
        if (rutaCompatible) {
            file = new File(FileBased.RUTA_RESOURCES_PATH, processedValue);
        } else {
            file = new File(processedValue);
        }
        return FileOps.readContent(file);
    }

}
