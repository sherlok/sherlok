package org.sherlok.utils;

import static org.sherlok.mappings.Def.SEPARATOR;

import java.util.regex.Pattern;

public class CheckThat {

    /** Letters, numbers, dots and underscore only */
    private static final Pattern ALPHANUM_DOT = Pattern
            .compile("[^a-zA-Z0-9\\._]");

    public static void checkOnlyAlphanumDot(String test)
            throws ValidationException {
        if (ALPHANUM_DOT.matcher(test).find()) {
            throw new ValidationException("'" + test
                    + "' contains something else than"
                    + " letters, numbers or dots");
        }
    }

    public static void checkValidId(String id) throws ValidationException {
        if (id.indexOf(SEPARATOR) == -1) {
            throw new ValidationException(id + " must contain a column (':')");

        } else if (id.split(SEPARATOR).length != 2) {
            throw new ValidationException("'" + id
                    + "' must contain a single column (':')");
        }
    }

    public static <T> T checkNotNull(T reference, String errorMessage)
            throws ValidationException {
        if (reference == null) {
            throw new ValidationException(errorMessage);
        }
        return reference;
    }

    public static void checkArgument(boolean expression, String errorMessage)
            throws ValidationException {
        if (!expression) {
            throw new ValidationException(errorMessage);
        }
    }

}
