package org.sherlok.mappings;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sherlok.Sherlok.SEPARATOR;

import org.sherlok.utils.CheckThat;
import org.sherlok.utils.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class for definitions.
 * 
 * @author renaud@apache.org
 */
public abstract class Def {

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

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean validate(String msgName) {
        try {
            checkNotNull(name, "name should not be null");
            CheckThat.checkOnlyAlphanumUnderscore(name);
            checkNotNull(version, "version should not be null");
            CheckThat.checkOnlyAlphanumDotUnderscore(version);
            // TODO more
        } catch (Throwable e) {
            throw new ValidationException("" + msgName + ": " + e.getMessage());
        }
        return true;
    }

    /** Creates an id for this def, composed of 'name:version' */
    public static String createId(String name, String version) {
        return name + SEPARATOR + (version == null ? "null" : version);
    }

    public static String getName(String id) {
        return id.split(SEPARATOR)[0];
    }

    public static String getVersion(String id) {
        return id.split(SEPARATOR)[1];
    }

    @JsonIgnore
    public String getId() {
        return name + SEPARATOR + version;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
