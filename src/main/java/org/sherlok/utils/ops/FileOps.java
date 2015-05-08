package org.sherlok.utils.ops;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class FileOps {

    public static String readContent(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toString(inputStream);
        }
    }

}
