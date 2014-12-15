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

/**
 * Whenever some input is not valid. The error message should provide meaningful
 * about 1) what object type was not valid 2) what object id and 3) what exactly
 * was not valid.
 * 
 * @author renaud@apache.org
 */
@SuppressWarnings("serial")
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Exception e) {
        super(e);
    }

    public ValidationException(String msg, Exception e) {
        super(msg, e);
    }

    public static class ValidationErrorMessage {
        String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return errorMessage.toString();
        }
    }

    /** To display errors nicely as JSON */
    public Object toJson() {
        ValidationErrorMessage m = new ValidationErrorMessage();
        m.errorMessage = getMessage();
        return m;
    }
}
