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

import org.sherlok.mappings.SherlokException;

public class CheckThat {

    /** Letters, numbers, dots and underscore only */
    private static final Pattern ALPHANUM_DOT_UNDERSCORE = compile("[^a-zA-Z0-9\\._]");
    private static final Pattern ALPHANUM_DOT_UNDERSCORE_SLASH = compile("[^a-zA-Z0-9\\._/]");

    /**
     * @param test
     *            the string to validate
     * @param variableName
     *            name is prepended to exception
     * @throws SherlokException
     *             if <code>test</code> is empty/null, or contains something
     *             else than letters, numbers or dots.
     */
    public static void checkOnlyAlphanumDotUnderscore(String test,
            String variableName) throws SherlokException {
        if (test == null || test.length() == 0) {
            throw new SherlokException(variableName + " cannot be empty or null");
        }
        if (ALPHANUM_DOT_UNDERSCORE.matcher(test).find()) {
            throw new SherlokException(variableName + " '" + test
                    + "' contains something else than"
                    + " letters, numbers or dots");
        }
    }

    /**
     * @param domain
     *            the domain to validate
     * @param context
     *            text is prepended to exception
     * @throws SherlokException
     *             if <code>test</code> is null (empty is ok), or contains '..'
     *             or something else than letters, numbers, dots or forward
     *             slashes.
     */
    public static void validateDomain(String domain) throws SherlokException {
        if (domain == null) {
            throw new SherlokException("domain cannot be null");
        }

        validatePath(domain);

        if (ALPHANUM_DOT_UNDERSCORE_SLASH.matcher(domain).find()) {
            throw new SherlokException("domain '" + domain
                    + "' contains something else than"
                    + " letters, numbers or dots");
        }
    }

    /** Forbids to access the whole computer through... */
    public static void validatePath(String path) throws SherlokException {
        if (path.indexOf("..") != -1) {
            throw new SherlokException("path '" + path
                    + "' can not contain double dots.");
        }
    }

    /**
     * @param id
     *            the id to validate
     * @param variableName
     *            name is prepended to exception
     * @throws SherlokException
     *             if <code>id</code> contains a single column, or if name or
     *             value is not valid.
     */
    public static void validateId(String id, String variableName)
            throws SherlokException {
        if (id.indexOf(SEPARATOR) == -1) {
            throw new SherlokException(variableName + " '" + id
                    + "' should have the format"//
                    + " {name}:{version}, but no column was found.");

        } else if (id.split(SEPARATOR).length != 2) {
            throw new SherlokException(variableName + " '" + id
                    + "' should have the format"//
                    + " {name}:{version}, but more than one column was found.");
        } else {
            String[] splits = id.split(SEPARATOR);
            checkOnlyAlphanumDotUnderscore(splits[0], variableName);
            checkOnlyAlphanumDotUnderscore(splits[1], variableName);
        }
    }

    public static <T> T validateNotNull(T reference, String variableName)
            throws SherlokException {
        if (reference == null) {
            throw new SherlokException(variableName + " should not be null");
        }
        return reference;
    }

    public static void validateArgument(boolean expression, String errorMessage)
            throws SherlokException {
        if (!expression) {
            throw new SherlokException(errorMessage);
        }
    }

    private static final String JAVA_IDENTIFIER = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern VALID_JAVA_IDENTIFIER = compile(JAVA_IDENTIFIER);

    public static void validateJavaIdentifier(String identifier,
            String errorMessage) throws SherlokException {

        if (!VALID_JAVA_IDENTIFIER.matcher(identifier).matches()) {
            throw new SherlokException(errorMessage);
        }
    }

    private static final Pattern VALID_TYPE_IDENTIFIER = compile(JAVA_IDENTIFIER
            + "(\\.\\*)?");

    public static void validateTypeIdentifier(String identifier,
            String errorMessage) throws SherlokException {

        if (!VALID_TYPE_IDENTIFIER.matcher(identifier).matches()) {
            throw new SherlokException(errorMessage);
        }
    }

    public static void validateArgument(boolean expression, String message,
            String object, String remedy) throws SherlokException {
        if (!expression) {
            throw new SherlokException().setMessage(message).setObject(object)
                    .setRemedy(remedy);
        }
    }
}
