package org.sherlok.config;

import java.util.Map;

import org.sherlok.utils.ValidationException;

/**
 * Factory for {@link ConfigVariable}
 */
public class ConfigVariableFactory {

    public static ConfigVariable factory(String name, Map<String, String> config)
            throws ValidationException {

        String type = config.get("type");
        if (type == null || type.equals("text")) {
            return constructBasicVariable(name, config);
        } else if (type.equals("git")) {
            return constructGitVariable(name, config);
        } else if (type.equals("http")) {
            return constructHttpVariable(name, config);
        }

        throw new ValidationException("unknown type variable", name);
    }

    private static ConfigVariable constructHttpVariable(String name,
            Map<String, String> config) throws ValidationException {
        String url = config.get("url");

        if (url == null)
            throw new ValidationException("http variable without url", name);

        Boolean rutaCompatible = getRutaCompatibilityMode(config);

        return new HttpConfigVariable(url, rutaCompatible);
    }

    private static ConfigVariable constructGitVariable(String name,
            Map<String, String> config) throws ValidationException {
        String url = config.get("url");

        if (url == null)
            throw new ValidationException("git variable without url", name);

        String ref = config.get("ref"); // ok if null

        Boolean rutaCompatible = getRutaCompatibilityMode(config);

        return new GitConfigVariable(url, ref, rutaCompatible);
    }

    private static ConfigVariable constructBasicVariable(String name,
            Map<String, String> config) throws ValidationException {
        String value = config.get("value");
        if (value == null)
            throw new ValidationException("text variable with no value", name);
        return new BasicConfigVariable(value);
    }

    private static Boolean getRutaCompatibilityMode(Map<String, String> config) {
        String mode = config.get("mode");

        // not enable by default
        return mode != null && mode.equals("ruta");
    }

}
