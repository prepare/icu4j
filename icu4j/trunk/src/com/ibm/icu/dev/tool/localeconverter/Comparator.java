/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.tool.localeconverter;

public interface Comparator {
    /**
        returns 0 if objects are equal, -1 if a is less than b, 1 otherwise.
    */
    public int compare(Object a, Object b);
}
