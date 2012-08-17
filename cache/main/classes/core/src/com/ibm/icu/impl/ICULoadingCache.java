/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

/**
 * Interface for very simple caching.
 * 
 * @param <K> The key type of cache
 * @param <V> the value type of cache
 * @author Travis Keep
 */
public interface ICULoadingCache<K, V> {
    /**
     * Invalidates all entries in this cache. This causes any subsequent calls
     * to {@link #get} to load the new value.
     */
    void invalidateAll();
    
    /**
     * Gets value from cache. If no value for key is present in cache, the cache
     * loads the value, stores it in the cache, and returns that value. If
     * another thread is already loading the value for key then the calling
     * thread blocks until the value is available.
     * 
     * @param key the cache key
     * @return the cache value. May return null if no cache value could be
     * loaded for given key.
     */
    V get(K key);
    
    /**
     * Like {@link #get}, but if no value for key is present in the cache,
     * immediately returns null.
     * @param key the cache key
     * @return the cache value or null if no value for key stored in cache.
     */
    V getIfPresent(Object key);
}
