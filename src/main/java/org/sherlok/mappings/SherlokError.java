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
package org.sherlok.mappings;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

import java.util.Map;

import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Meaningful error messages, intended for JSON consumption.
 * 
 * @author renaud@apache.org
 */
// ensure property output order
@JsonPropertyOrder(value = { "when", "message", "object", "remedy", "details" })
@JsonIgnoreProperties(value = { "stackTrace", "localizedMessage" })
@JsonInclude(NON_DEFAULT)
public class SherlokError extends ValidationException {
    private static final long serialVersionUID = 2101833257950070947L;

    public SherlokError(Map<String, ?> m) { // FIXME delete
        super(m);
    }

    public SherlokError() {
        // TODO Auto-generated constructor stub
    }

    /** In which circumstances did something go wrong? */
    @JsonProperty("when")
    private String when;

    /** What went wrong? */
    @JsonProperty("message")
    private String _message;

    /** More details about what went wrong? */
    private String details;

    /** What was the object causing the error? */
    private String object;

    /** Suggestion on how to fix the error, if available */
    private String remedy;

    public String getWhen() {
        return when;
    }

    public SherlokError setWhen(String when) {
        this.when = when;
        return this;
    }

    public String getMessage() {
        return _message;
    }

    public SherlokError setMessage(String message) {
        this._message = message;
        return this;
    }

    public String getDetails() {
        return details;
    }

    public SherlokError setDetails(String details) {
        this.details = details;
        return this;
    }

    public SherlokError setDetails(StackTraceElement[] elements) {

        StringBuilder msg = new StringBuilder();
        for (int i = 0, n = elements.length; i < n && n < 5; i++) {
            msg.append(elements[i].getFileName() + ":"
                    + elements[i].getLineNumber() + ">> "
                    + elements[i].getMethodName() + "(); ");
        }
        setDetails(msg.toString().trim());
        return this;
    }

    public String getObject() {
        return object;
    }

    public SherlokError setObject(String object) {
        this.object = object;
        return this;
    }

    public String getRemedy() {
        return remedy;
    }

    public SherlokError setRemedy(String remedy) {
        this.remedy = remedy;
        return this;
    }

    /** To display errors nicely as JSON */
    public Object toJson() {
        return this;
    }
}
