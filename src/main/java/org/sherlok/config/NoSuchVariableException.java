package org.sherlok.config;

public class NoSuchVariableException extends Exception {
    public NoSuchVariableException(String name) {
        super("unknown configuration variable " + name);
    }

    private static final long serialVersionUID = -5819642307257618387L;
}
