/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

/**
 * @author rocketman
 *
 */
public class Maybe<T> {
    private static Maybe<Object> NOTHING = new Maybe<Object>(null);
    private T data;
    
    public static <T> Maybe<T> just(T value) {
        if (value == null) {
            throw new IllegalArgumentException("just requires non null value.");
        }
        return new Maybe<T>(value);
    }
    
    public static <T> Maybe<T> nothing() {
        return (Maybe<T>) NOTHING;
    }
    
    public boolean isNothing() {
        return data == null;
    }
    
    public boolean isSomething() {
        return data != null;
    }
    
    public T getValue() {
        if (data == null) {
            throw new IllegalStateException("instance is nothing.");
        }
        return data;
    }
    
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
