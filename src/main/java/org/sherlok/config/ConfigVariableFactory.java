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
        }

        // TODO http(s) protocols

        throw new ValidationException("unknown type variable", name);
    }

    private static ConfigVariable constructGitVariable(String name,
            Map<String, String> config) throws ValidationException {
        String url = config.get("url");

        if (url == null)
            throw new ValidationException("git variable without url", name);

        String ref = config.get("ref"); // ok if null

        return new GitConfigVariable(url, ref);
    }

    private static ConfigVariable constructBasicVariable(String name,
            Map<String, String> config) throws ValidationException {
        String value = config.get("value");
        if (value == null)
            throw new ValidationException("text variable with no value", name);
        return new BasicConfigVariable(value);
    }

}
