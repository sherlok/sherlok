package org.sherlok.config;


/**
 * Simple text variable.
 */
public class BasicConfigVariable implements ConfigVariable {

    private final String value;

    public BasicConfigVariable(String value) {
        assert value != null;
        this.value = value;
    }

    @Override
    public String getProcessedValue() {
        // nothing special to do
        return value;
    }

}
