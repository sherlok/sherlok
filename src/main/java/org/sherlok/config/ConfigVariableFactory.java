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
package org.sherlok.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.sherlok.utils.ValidationException;
import org.slf4j.Logger;

/**
 * Factory for {@link ConfigVariable}.
 */
public class ConfigVariableFactory {
    protected static final Logger LOG = getLogger(ConfigVariableFactory.class);

    /**
     * Special mode for configuration variable: in order to work properly with
     * in RUTA script, the paths represented by configuration variables need to
     * be relative to FileBased.RUTA_RESOURCES_PATH instead of absolute paths.
     * 
     * MODE_RUTA is a (currently the only) possible value associated with
     * FIELD_MODE in the config mapping.
     */
    private static final String MODE_RUTA = "ruta";

    // FIELD_* denote entries in the config mapping
    private static final String FIELD_MODE = "mode";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_REF = "ref";
    private static final String FIELD_URL = "url";
    private static final String FIELD_TYPE = "type";

    // TYPE_* denote possible values associated with FIELD_TYPE
    // TODO use enum
    private static final String TYPE_HTTP = "http";
    private static final String TYPE_GIT = "git";
    private static final String TYPE_TEXT = "text";

    /**
     * Construct a new configuration variable from a config mapping, or throw an
     * {@link ValidationException} if some data is missing or incorrect.
     */
    public static ConfigVariable factory(String name, Map<String, String> config)
            throws ValidationException {

        String type = config.get(FIELD_TYPE);
        if (type == null || type.equals(TYPE_TEXT)) {
            return constructBasicVariable(name, config);
        } else if (type.equals(TYPE_GIT)) {
            return constructGitVariable(name, config);
        } else if (type.equals(TYPE_HTTP)) {
            return constructHttpVariable(name, config);
        }

        throw new ValidationException("unknown type variable", name);
    }

    /** Create a cleaner object for the given type */
    public static ConfigVariableCleaner cleanerFactory(String type) {
        if (type.equals(TYPE_GIT)) {
            return new ConfigVariableCleaner() {
                @Override
                public boolean clean() {
                    return GitConfigVariable.cleanCache();
                }
            };
        } else if (type.equals(TYPE_HTTP)) {
            return new ConfigVariableCleaner() {
                @Override
                public boolean clean() {
                    return HttpConfigVariable.cleanCache();
                }
            };
        }

        LOG.debug("no ConfigVariableCleaner found for type '{}'", type);
        return null;
    }

    /** Create a cleaner object for all valid types */
    public static ConfigVariableCleaner totalCleanerFactor() {
        return new ConfigVariableCleaner() {
            @Override
            public boolean clean() {
                return GitConfigVariable.cleanCache()
                        && HttpConfigVariable.cleanCache();
            }
        };
    }

    /**
     * Polymorphic cleaner
     * 
     * Classes that implement this interface are responsible for cleaning all
     * files downloaded by one (or many) download protocol(s).
     * 
     * See {@link ConfigVariableFactory#cleanerFactory()} for some concrete
     * examples.
     */
    public interface ConfigVariableCleaner {
        public boolean clean();
    }

    private static ConfigVariable constructHttpVariable(String name,
            Map<String, String> config) throws ValidationException {
        String url = config.get(FIELD_URL);

        if (url == null)
            throw new ValidationException("http variable without url", name);

        Boolean rutaCompatible = getRutaCompatibilityMode(config);

        return new HttpConfigVariable(url, rutaCompatible);
    }

    private static ConfigVariable constructGitVariable(String name,
            Map<String, String> config) throws ValidationException {
        String url = config.get(FIELD_URL);

        if (url == null)
            throw new ValidationException("git variable without url", name);

        String ref = config.get(FIELD_REF); // ok if null

        Boolean rutaCompatible = getRutaCompatibilityMode(config);

        return new GitConfigVariable(url, ref, rutaCompatible);
    }

    private static ConfigVariable constructBasicVariable(String name,
            Map<String, String> config) throws ValidationException {
        String value = config.get(FIELD_VALUE);
        if (value == null)
            throw new ValidationException("text variable with no value", name);
        return new BasicConfigVariable(value);
    }

    /** Check if the RUTA mode is activated for some config mapping. */
    private static Boolean getRutaCompatibilityMode(Map<String, String> config) {
        String mode = config.get(FIELD_MODE);

        // not enable by default
        return mode != null && mode.equals(MODE_RUTA);
    }
}
