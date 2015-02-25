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
package org.sherlok.utils;

import static java.util.regex.Pattern.compile;
import static org.sherlok.mappings.Def.SEPARATOR;

import java.util.regex.Pattern;

public class CheckThat {

    /** Letters, numbers, dots and underscore only */
    private static final Pattern ALPHANUM_DOT = compile("[^a-zA-Z0-9\\._]");

    /**
     * @param test
     *            the string to validate
     * @param context
     *            text is prepended to exception
     * @throws ValidationException
     *             if <code>test</code> is empty/null, or contains something
     *             else than letters, numbers or dots.
     */
    public static void checkOnlyAlphanumDot(String test, String context)
            throws ValidationException {
        if (test == null || test.length() == 0) {
            throw new ValidationException(context + ": cannot be empty or null");
        }
        if (ALPHANUM_DOT.matcher(test).find()) {
            throw new ValidationException(context + ": '" + test
                    + "' contains something else than"
                    + " letters, numbers or dots");
        }
    }

    /**
     * @param id
     *            the id to validate
     * @param context
     *            text is prepended to exception
     * @throws ValidationException
     *             if <code>id</code> contains a single column, or if name or
     *             value is not valid.
     */
    public static void validateId(String id, String context)
            throws ValidationException {
        if (id.indexOf(SEPARATOR) == -1) {
            throw new ValidationException(context + ": '" + id
                    + "' should have the format"//
                    + " {name}:{version}, but no column was found.");

        } else if (id.split(SEPARATOR).length !=2) {
            throw new ValidationException(context + ": '" + id
                    + "' should have the format"//
                    + " {name}:{version}, but more than one column was found.");
        } else {
            String[] splits = id.split(SEPARATOR);
            checkOnlyAlphanumDot(splits[0], context);
            checkOnlyAlphanumDot(splits[1], context);
        }
    }

    public static <T> T validateNotNull(T reference, String errorMessage)
            throws ValidationException {
        if (reference == null) {
            throw new ValidationException(errorMessage);
        }
        return reference;
    }

    public static void validateArgument(boolean expression, String errorMessage)
            throws ValidationException {
        if (!expression) {
            throw new ValidationException(errorMessage);
        }
    }

    private static final String JAVA_IDENTIFIER = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern VALID_JAVA_IDENTIFIER = compile(JAVA_IDENTIFIER);

    public static void validateJavaIdentifier(String identifier,
            String errorMessage) throws ValidationException {

        if (!VALID_JAVA_IDENTIFIER.matcher(identifier).matches()) {
            throw new ValidationException(errorMessage);
        }
    }

    private static final Pattern VALID_TYPE_IDENTIFIER = compile(JAVA_IDENTIFIER
            + "(\\.\\*)?");

    public static void validateTypeIdentifier(String identifier,
            String errorMessage) throws ValidationException {

        if (!VALID_TYPE_IDENTIFIER.matcher(identifier).matches()) {
            throw new ValidationException(errorMessage);
        }
    }
}
