/**
 * Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
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

    /** @return true if 'id' contains a single column */
    public static void validateId(String id) throws ValidationException {
        if (id.indexOf(SEPARATOR) == -1) {
            throw new ValidationException(id + " must contain a column (':')");

        } else if (id.split(SEPARATOR).length != 2) {
            throw new ValidationException("'" + id
                    + "' must contain a single column (':')");
        } else {
            String[] splits = id.split(SEPARATOR);
            checkOnlyAlphanumDot(splits[0]);
            checkOnlyAlphanumDot(splits[1]);
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
}
