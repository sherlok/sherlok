package org.sherlok.config;


public class ProcessConfigVariableException extends Exception {

    public ProcessConfigVariableException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProcessConfigVariableException(String msg) {
        super(msg);
    }

    private static final long serialVersionUID = -95682363642593272L;

}
