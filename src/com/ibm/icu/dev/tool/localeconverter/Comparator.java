/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/localeconverter/Comparator.java,v $ 
 * $Date: 2002/01/31 01:21:27 $ 
 * $Revision: 1.1 $
 *
 *****************************************************************************************
 */
package com.ibm.tools.localeconverter;

public interface Comparator {
    /**
        returns 0 if objects are equal, -1 if a is less than b, 1 otherwise.
    */
    public int compare(Object a, Object b);
}
