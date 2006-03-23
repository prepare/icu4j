/*
**********************************************************************
* Copyright (c) 2004-2006, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 20, 2004
* Since: ICU 3.0
**********************************************************************
*/
package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;

/**
 * A formatter for Measure objects.  This is an abstract base class.
 *
 * <p>To format or parse a Measure object, first create a formatter
 * object using a MeasureFormat factory method.  Then use that
 * object's format and parse methods.
 *
 * @see com.ibm.icu.text.UFormat
 * @author Alan Liu
 * @draft ICU 3.0
 * @provisional This API might change or be removed in a future release.
 */
public abstract class MeasureFormat extends UFormat {
    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -7182021401701778240L;

    /**
     * @internal
     */
    protected MeasureFormat() {};
    
    /**
     * Return a formatter for CurrencyAmount objects in the given
     * locale.
     * @param locale desired locale
     * @return a formatter object
     * @draft ICU 3.0
     * @provisional This API might change or be removed in a future release.
     */
    public static MeasureFormat getCurrencyFormat(ULocale locale) {
        return new CurrencyFormat(locale);
    }

    /**
     * Return a formatter for CurrencyAmount objects in the default
     * locale.
     * @return a formatter object
     * @draft ICU 3.0
     * @provisional This API might change or be removed in a future release.
     */
    public static MeasureFormat getCurrencyFormat() {
        return getCurrencyFormat(ULocale.getDefault());
    }
}
