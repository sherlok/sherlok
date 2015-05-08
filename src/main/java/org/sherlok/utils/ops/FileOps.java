package org.sherlok.utils.ops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileOps {

    public static String readContent(File file) throws FileNotFoundException {
        InputStream stream = new FileInputStream(file);
        return InputStreamOps.readContent(stream);
    }

}
