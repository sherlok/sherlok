package org.sherlok.config;

/**
 * Configuration variable for bundles
 * 
 * TODO use class hierarchy to support file, git, http(s) protocols
 */
public class ConfigVariable {
    private String value;

    public ConfigVariable(String value) {
        this.value = value;
    }

    public String getProcessedValue() {
        return value;
    }
}
