package org.sherlok.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sherlok.FileBased;
import org.sherlok.utils.ops.FileOps;

public class HttpConfigVariableTest {

    private static final String VALID_URL = "https://raw.githubusercontent.com/sherlok/sherlok_dependency_test/master/resources/file.txt";
    private static final String INVALID_URL = "http://bad_example";

    private static final String FILE_CONTENT = "MASTER\n";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Remove any cached repository
        HttpConfigVariable.cleanCache();
    }

    @Test
    public final void testGetProcessedValue()
            throws ProcessConfigVariableException, IOException {
        HttpConfigVariable var = new HttpConfigVariable(VALID_URL);
        String val = var.getProcessedValue();
        assertNotNull(val);

        String content = getTestFileContent(val); // should not throw
        assertEquals("checking content", FILE_CONTENT, content);
    }

    @Test(expected = ProcessConfigVariableException.class)
    public final void testGetProcessedValueInvalidURL()
            throws ProcessConfigVariableException {
        HttpConfigVariable var = new HttpConfigVariable(INVALID_URL);
        var.getProcessedValue(); // should fire
    }

    private static String getTestFileContent(String processedValue)
            throws IOException {
        File file = new File(FileBased.RUTA_RESOURCES_PATH, processedValue);
        return FileOps.readContent(file);
    }

}
