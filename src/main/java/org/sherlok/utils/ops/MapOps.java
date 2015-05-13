/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sherlok.utils.ops;

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
