package org.sherlok.config;

/**
 * Configuration variable for bundles
 */
public interface ConfigVariable {
    /**
     * Returns the (runtime) path corresponding to this variable
     * 
     * This path should be relative to FileBased.RUTA_RESOURCES_PATH when the
     * RUTA compatibility mode is enabled.
     * 
     * @throws ProcessConfigVariableException
     *             when a runtime error occurs
     */
    public abstract String getProcessedValue()
            throws ProcessConfigVariableException;
}
