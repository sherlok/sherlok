package org.sherlok.utils;

import static org.sherlok.utils.Create.list;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Operations on Maps
 */
public class MapOps {

    /**
     * Flatten key-value pairs into an array of Objects
     */
    public static <K, V> Object[] flattenParameters(Map<K, V> parameters) {
        List<Object> flatParams = list();
        for (Entry<K, V> en : parameters.entrySet()) {
            flatParams.add(en.getKey());
            flatParams.add(en.getValue());
        }
        Object[] flatParamsArray = flatParams.toArray();
        return flatParamsArray;
    }

}
