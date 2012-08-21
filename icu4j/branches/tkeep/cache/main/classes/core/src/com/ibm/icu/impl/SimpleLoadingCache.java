/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of ICULoadingCache that uses a ConcurrentHashMap
 * internally. Subclasses must override the {@link #load} method to specify
 * the value for a key when there is a cache miss. If there is no value for a
 * given key, {@link #load} may return null. null values are never cached,
 * so {@link #load} is called each time {@link #get} is called for a key with a
 * missing value.
 * 
 * The keys in the internal map are the same as the keys in this cache. The
 * values in the internal map are soft references to values in this cache allowing
 * the GC to possibly reclaim that memory should free memory become low. If the GC
 * does reclaim a value, the entire cache entry for that reclaimed value will
 * eventually be removed to save more space. This is accomplished using a ReferenceQueue
 * and amortizing the processing of that reference queue over calls to {@link #get}
 * and {link #getIfPresent}. Each call to those functions will process at most
 * 10 reclaimed values by physically removing them from the map. By limiting to
 * 10, we ensure that the execution time of {@link #get} remains deterministic.
 * 
 * @author Travis Keep
 */
public abstract class SimpleLoadingCache<K, V> implements ICULoadingCache<K, V> {
     
    // Maximum number of reclaimed entries to remove from the cache with each call
    // to get() or getIfPresent()
    private static final int MAX_TO_CLEAN_UP = 10;

    private final ConcurrentHashMap<K, HashValueReference<K, V>> map =
        new ConcurrentHashMap<K, HashValueReference<K, V>>();
    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
    
    public void invalidateAll() {
      map.clear();      
    }

    public V get(K key) {
        V result = getIfPresent(key);
        if (result == null) {
            V value = load(key);
            if (value == null) {
                return null;
            }
            return map.putIfAbsent(key, new HashValueReference<K, V>(key, value, queue)).get();
        }
        return result;
    }

    public V getIfPresent(Object key) {
        removeStaleEntries();
        SoftReference<V> reference = map.get(key);
        return reference == null ? null : reference.get();
    }
    
    /**
     * Loads the value for given key when there is a cache miss.
     * @param key the key passed to {@link #get}
     * @return the value for key. If null, then the corresponding call to {@link #get}
     * also returns null for that key, but null values are never cached. Otherwise the
     * cache may grow exceedingly large with bad keys.
     */
    protected abstract V load(K key);
    
    private void removeStaleEntries() {
        int numProcessed = 0;
        for (HashValueReference<K, V> reference = poll();
                reference != null && numProcessed < MAX_TO_CLEAN_UP;
                reference = poll()) {
            map.remove(reference.getKey(), reference);
            numProcessed++;
        }
    }
    
    @SuppressWarnings("unchecked")
    private HashValueReference<K, V> poll() {
       return (HashValueReference<K, V>) queue.poll();
    }
   
    private static class HashValueReference<K, V> extends SoftReference<V> {

        private final K key;
        
        public HashValueReference(K key, V value, ReferenceQueue<? super V> q) {
            super(value, q);
            this.key = key;
        }
        
        public K getKey() {
            return key;
        }
        
    }

}
