/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import com.ibm.icu.util.Currency;
import com.ibm.icu.util.ULocale;

/**
 * @author rocketman
 *
 */
public class NumberFormatTestTuple {
    public Maybe<ULocale> locale = Maybe.nothing();
    public Maybe<Currency> currency = Maybe.nothing();
    public Maybe<String> pattern = Maybe.nothing();
    public Maybe<String> format = Maybe.nothing();
    public Maybe<String> output = Maybe.nothing();
    public Maybe<String> comment = Maybe.nothing();
    public Maybe<Integer> minIntegerDigits = Maybe.nothing();
    public Maybe<Integer> maxIntegerDigits = Maybe.nothing();
    public Maybe<Integer> minFractionDigits = Maybe.nothing();
    public Maybe<Integer> maxFractionDigits = Maybe.nothing();
    public Maybe<Integer> minGroupingDigits = Maybe.nothing();
    public Maybe<String> breaks = Maybe.nothing();
    
    public static String[] fieldOrdering = {
        "locale",
        "currency",
        "pattern",
        "format",
        "output",
        "comment",
        "minIntegerDigits",
        "maxIntegerDigits",
        "minFractionDigits",
        "maxFractionDigits",
        "minGroupingDigits",
        "breaks",
    };
    
    static {
        HashSet<String> set = new HashSet<String>();
        for (String s : fieldOrdering) {
            if (!set.add(s)) {
                throw new ExceptionInInitializerError(s + "is a duplicate field.");    
            }
        }
    }
    
    public void setLocale(String value) {
        locale = Maybe.just(new ULocale(value));
    }
    
    public void setCurrency(String value) {
        currency = Maybe.just(Currency.getInstance(value));
    }
    
    public void setPattern(String value) {
        pattern = Maybe.just(value);
    }
    
    public void setFormat(String value) {
        format = Maybe.just(value);
    }
    
    public void setOutput(String value) {
        output = Maybe.just(value);
    }
    
    public void setComment(String value) {
        comment = Maybe.just(value);
    }
    
    public void setMinIntegerDigits(String value) {
        minIntegerDigits = Maybe.just(Integer.valueOf(value));
    }
    
    public void setMaxIntegerDigits(String value) {
        maxIntegerDigits = Maybe.just(Integer.valueOf(value));
    }
    
    public void setMinFractionDigits(String value) {
        minFractionDigits = Maybe.just(Integer.valueOf(value));
    }
    
    public void setMaxFractionDigits(String value) {
        maxFractionDigits = Maybe.just(Integer.valueOf(value));
    }
    
    public void setMinGroupingDigits(String value) {
        minGroupingDigits = Maybe.just(Integer.valueOf(value));
    }
    
    public void setBreaks(String value) {
        breaks = Maybe.just(value);
    }
    
    public void setField(String fieldName, String valueString)
            throws NoSuchMethodException {
        Method m = getClass().getMethod(
                fieldToSetter(fieldName), String.class);
        try {
            m.invoke(this, valueString);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        boolean first = true;
        for (String fieldName : fieldOrdering) {
            try {
                Field field = getClass().getField(fieldName);
                Maybe<?> maybeValue = (Maybe<?>) field.get(this);
                if (maybeValue.isNothing()) {
                    continue;
                }
                if (!first) {
                    result.append(", ");
                }
                first = false;
                result.append(fieldName);
                result.append(": ");
                result.append(maybeValue.getValue());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        result.append("}");
        return result.toString();
    }

    private static String fieldToSetter(String fieldName) {
        return "set"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
    }

}
