package org.sherlok.config;

/**
 * Thrown when an unknown variable is used, for example by a bundle or pipeline
 * definition.
 */
public class NoSuchVariableException extends Exception {
    public NoSuchVariableException(String name) {
        super("unknown configuration variable " + name);
    }

    private static final long serialVersionUID = -5819642307257618387L;
}
