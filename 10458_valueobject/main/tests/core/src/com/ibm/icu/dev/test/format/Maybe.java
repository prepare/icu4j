/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

/**
 * May be either nothing or just some value of type T.
 *
 */
public class Maybe<T> {
    private static Maybe<Object> NOTHING = new Maybe<Object>(null);
    private T data;
    
    /**
     * Returns a Maybe<T> representing just value.
     */
    public static <T> Maybe<T> just(T value) {
        if (value == null) {
            throw new IllegalArgumentException("just requires non null value.");
        }
        return new Maybe<T>(value);
    }
    
    /**
     * Returns a Maybe<T> that represents nothing.
     */
    public static <T> Maybe<T> nothing() {
        return (Maybe<T>) NOTHING;
    }
    
    /**
     * Returns true if this instance represents nothing.
     */
    public boolean isNothing() {
        return data == null;
    }

    /**
     * Returns true if this instance represents just some value.
     */
    public boolean isValue() {
        return data != null;
    }

    /**
     * Returns the value this instance represents.
     * @throws IllegalStateException if this instance represents nothing.
     */
    public T getValue() {
        if (data == null) {
            throw new IllegalStateException("instance is nothing.");
        }
        return data;
    }
    
    /**
     * Returns the value this instance represents. If this instance represents nothing,
     * returns defaultValue.
     */
    public T getValue(T defaultValue) {
        if (data == null) {
            return defaultValue;
        }
        return data;
    }

    private Maybe(T value) {
        data = value;
    }
}
