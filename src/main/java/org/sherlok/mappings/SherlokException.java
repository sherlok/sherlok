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
public class SherlokException extends Exception {
    private static final long serialVersionUID = 2101833257950070947L;

    public SherlokException() {
    }

    public SherlokException(String message) {
        this._message = message;
    }

    public SherlokException(String message, String object) {
        this._message = message;
        this.object = object;
    }

    /** In which circumstances did something go wrong? */
    @JsonProperty("when")
    private String when;

    /** What went wrong? */
    @JsonProperty("message")
    private String _message;

    /** More details about what went wrong? */
    private Object details;

    /** What was the object causing the error? */
    private String object;

    /** Suggestion on how to fix the error, if available */
    private String remedy;
    /** The REST route, if available */
    private String route;

    public String getWhen() {
        return when;
    }

    public SherlokException setWhen(String when) {
        this.when = when;
        return this;
    }

    public String getMessage() {
        return _message;
    }

    public SherlokException setMessage(String message) {
        this._message = message;
        return this;
    }

    public Object getDetails() {
        return details;
    }

    public SherlokException setDetails(String details) {
        this.details = details;
        return this;
    }

    public SherlokException setDetails(StackTraceElement[] elements) {

        StringBuilder msg = new StringBuilder();
        for (int i = 0, n = elements.length; i < n && n < 5; i++) {
            msg.append(elements[i].getFileName() + ":"
                    + elements[i].getLineNumber() + ">> "
                    + elements[i].getMethodName() + "(); ");
        }
        setDetails(msg.toString().trim());
        return this;
    }

    /**
     * @param detailsObj
     *            must be JSON seralizable
     */
    public SherlokException setDetails(Object detailsObj) {
        this.details = detailsObj;
        return this;
    }

    public String getObject() {
        return object;
    }

    public SherlokException setObject(String object) {
        this.object = object;
        return this;
    }

    public String getRemedy() {
        return remedy;
    }

    public SherlokException setRemedy(String remedy) {
        this.remedy = remedy;
        return this;
    }

    public SherlokException setRoute(String route) {
        this.route = route;
        return this;
    }

    public String getRoute() {
        return route;
    }

    /** To display errors nicely as JSON */
    public Object toJson() {
        return this;
    }

    @Override
    public String toString() {
        return (getMessage() != null ? getMessage() : "") //
                + (getObject() != null ? " ON OBJECT " + getObject() : "")//
                + (getWhen() != null ? " WHEN " + getWhen() : "")//
                + (getRoute() != null ? " and ROUTE " + getRoute() : "")//
                + (getDetails() != null ? "; DETAILS: " + getDetails() : "")//
                + (getRemedy() != null ? "; REMEDY: " + getRemedy() : "");
    }
}
