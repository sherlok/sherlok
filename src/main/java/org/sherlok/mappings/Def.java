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
package org.sherlok.mappings;

import static org.sherlok.utils.CheckThat.checkNotNull;
import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDot;

import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class for definitions.
 * 
 * @author renaud@apache.org
 */
public abstract class Def {

    public static final String SEPARATOR = ":";

    /** a unique name for this bundle. Letters, numbers and underscore only */
    protected String name,
    /**
     * a unique version id for this bundle. Letters, numbers, dots and
     * underscore only
     */
    version,
    /** (optional) */
    description;

    // get/set

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean validate(String msgName) throws ValidationException {
        try {
            checkNotNull(name, "name should not be null");
            checkOnlyAlphanumDot(name);
            checkNotNull(version, "version should not be null");
            checkOnlyAlphanumDot(version);
            // TODO more validation
        } catch (Throwable e) {
            throw new ValidationException("" + msgName + ": " + e.getMessage());
        }
        return true;
    }

    /** Creates an id for this def, composed of 'name:version' */
    public static String createId(String name, String version) {
        return name + SEPARATOR + (version == null ? "null" : version);
    }

    public static String getName(String id) {
        return id.split(SEPARATOR)[0];
    }

    public static String getVersion(String id) {
        return id.split(SEPARATOR)[1];
    }

    @JsonIgnore
    public String getId() {
        return name + SEPARATOR + version;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
