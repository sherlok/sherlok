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

import static org.sherlok.utils.Create.map;

import java.util.Map;

import org.sherlok.FileBased;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Whenever some input is not valid. The error message should provide meaningful
 * about 1) what object type was not valid 2) what object id and 3) what exactly
 * was not valid.
 * 
 * @author renaud@apache.org
 */
@SuppressWarnings("serial")
public class ValidationException extends Exception {

    public static final String ERR_NOTFOUND = "not_found";
    public static final String EXPECTED = "expected";
    public static final String SYSTEM = "system";
    public static final String ERR_UNEXPECTED = "unexpected";
    public static final String ERR = "error_value";
    public static final String MSG = "message";
    public static final String STATUS = "status";

    private Map<String, ?> map;

    @Deprecated
    /** use ValidationException(JSONObject json) instead */
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable t) {
        super(t);
    }

    public ValidationException(String msg, Throwable t) {
        super(msg, t);
    }

    public ValidationException(Map<String, ?> m) {
        this.map = m;
    }

    public ValidationException(String errorMsg, String errorValue) {
        this(map(MSG, errorMsg, ERR, errorValue));
    }

    public Map<String, ?> getMap() {
        return map;
    }

    /** To display errors nicely as JSON */
    public Object toJson() {
        if (map != null) {
            return map;
        } else {
            return map(ERR, getMessage(), "cause", getCause());
        }
    }

    @Override
    public String toString() {
        try {
            return FileBased.writeAsString(toJson());
        } catch (JsonProcessingException e) {
            return getMessage();
        }
    }
}
