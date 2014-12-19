package org.sherlok;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.Part;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.utils.ValidationException;

import com.google.common.io.Files;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileBasedTest {

    static final String uploadFile = "testUploadFile.txt";

    @Test(expected = ValidationException.class)
    public void test01_GetInexistentFile() throws Exception {
        FileBased.getResource("test/" + uploadFile);
    }

    @Test
    public void test02_UploadFile() throws Exception {
        FileBased.putResource("test/" + uploadFile, new Part() {

            @Override
            public void write(String fileName) throws IOException {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public long getSize() {
                return 1;
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return FileBasedTest.class.getResourceAsStream(uploadFile);
            }

            @Override
            public Collection<String> getHeaders(String name) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public Collection<String> getHeaderNames() {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public String getHeader(String name) {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public String getContentType() {
                throw new UnsupportedOperationException("not implemented");
            }

            @Override
            public void delete() throws IOException {
                throw new UnsupportedOperationException("not implemented");
            }
        });
    }

    @Test
    public void test03_GetFile() throws Exception {
        File f = FileBased.getResource("test/" + uploadFile);
        assertEquals("a test file\n", Files.toString(f, UTF_8));
    }

    @Test
    public void test04_ListFile() throws Exception {
        Collection<String> resources = FileBased.allResources();
        assertTrue(resources.contains("test/" + uploadFile));
    }

    @Test
    public void test05_DeleteFile() throws Exception {
        FileBased.deleteResource("test/" + uploadFile);
    }
}
