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
package org.sherlok.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to create lists, maps and sets.
 * 
 * @author renaud@apache.org
 */
public class Create {

    public static <E> ArrayList<E> list() {
        return new ArrayList<E>();
    }

    @SafeVarargs
    public static <E> List<E> list(E... elements) {
        ArrayList<E> list = new ArrayList<E>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> ArrayList<E> list(Iterable<E> it) {
        ArrayList<E> list = new ArrayList<E>();
        for (E e : it) {
            list.add(e);
        }
        return list;
    }

    public static <E> HashSet<E> set() {
        return new HashSet<E>();
    }

    @SafeVarargs
    public static <E> HashSet<E> set(E... elements) {
        HashSet<E> set = new HashSet<E>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    public static <K, V> HashMap<K, V> map() {
        return new HashMap<K, V>();
    }

    public static <K, V> Map<K, V> map(K key, V value) {
        HashMap<K, V> map = map();
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, V> map(K key, V value, K key2, V value2) {
        Map<K, V> map = map(key, value);
        map.put(key2, value2);
        return map;
    }

    public static <K, V> Map<K, V> map(K key, V value, K key2, V value2,
            K key3, V value3) {
        Map<K, V> map = map(key, value, key2, value2);
        map.put(key3, value3);
        return map;
    }
}
