/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.netbeans.gradle.android.layout.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author arsi
 */
public class MultiUnionMap<K, V> implements Map<K, V> {

    private final List<Map<K, V>> maps;

    public MultiUnionMap(List<Map<K, V>> maps) {
        this.maps = maps;
    }

    @Override
    public int size() {
        int size = 0;
        for (Map<K, V> map : maps) {
            size += map.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Map<K, V> map : maps) {
            if (!map.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        for (Map<K, V> map : maps) {
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Map<K, V> map : maps) {
            if (map.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        for (Map<K, V> map : maps) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<V> values() {
        List<V> tmp = new ArrayList<>();
        for (Map<K, V> map : maps) {
            tmp.addAll(map.values());
        }
        return tmp;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
