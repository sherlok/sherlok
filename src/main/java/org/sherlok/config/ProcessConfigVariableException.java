package org.sherlok.config;

/**
 * This exception is used when a configuration variable cannot be properly
 * processed (e.g. remote resource cannot be fetched, file cannot be written on
 * disk, ...).
 */
public class ProcessConfigVariableException extends Exception {

    public ProcessConfigVariableException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProcessConfigVariableException(String msg) {
        super(msg);
    }

    private static final long serialVersionUID = -95682363642593272L;

}
