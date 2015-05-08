package org.sherlok.utils.ops;

import java.io.InputStream;
import java.util.Scanner;

public class InputStreamOps {
    // TODO move me to a more appropriate location
    public static String readContent(InputStream input) {
        try (Scanner s = new Scanner(input)) {
            // Read until beginning (never reached -> read whole input)
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}
