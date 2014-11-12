package org.sherlok.mappings;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sherlok.CheckThat;

public abstract class Def<T extends Def<?>> {

    /** a unique name for this bundle. Letters, numbers and underscore only */
    protected String name,
    /**
     * a unique version id for this bundle. Letters, numbers, dots and
     * underscore only
     */
    version,
    /** (optional) */
    description;

    // get/set

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getVersion() {
        return version;
    }

    @SuppressWarnings("unchecked")
    public T setVersion(String version) {
        this.version = version;
        return (T) this;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    public boolean validate() {
        try {
            checkNotNull(name, "name should not be null");
            CheckThat.checkOnlyAlphanumUnderscore(name);
            checkNotNull(version, "version should not be null");
            CheckThat.isOnlyAlphanumDotUnderscore(version);
            // TODO more
        } catch (Throwable e) {
            throw new ValidationException(e.getMessage());
        }
        return true;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }

}
