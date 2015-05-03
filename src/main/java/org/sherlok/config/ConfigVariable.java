package org.sherlok.config;


/**
 * Configuration variable for bundles
 */
public interface ConfigVariable {
    /**
     * Returns the (runtime) path corresponding to this variable
     * 
     * @throws ProcessConfigVariableException
     *             when a runtime error occurs
     */
    public abstract String getProcessedValue()
            throws ProcessConfigVariableException;
}
