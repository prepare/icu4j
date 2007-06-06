/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

/**
 * <code>InitialTimeZoneRule</code> is a subclass of <code>TimeZoneRule</code> and used for initial
 * rule in ICU <code>TimeZone</code> concrete classes.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class InitialTimeZoneRule extends TimeZoneRule {

    private static final long serialVersionUID = -1284335506635215178L;

    /**
     * Constructs an <code>InitialTimeZoneRule</code> with the name, the GMT offset of its
     * standard time and the amount of daylight saving offset adjustment.
     * 
     * @param name          The time zone name.
     * @param rawOffset     The UTC offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in milliseconds.
     *                      If this ia a rule for standard time, the value of this argument is 0.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
    */
    public InitialTimeZoneRule(String name, int rawOffset, int dstSavings) {
        super(name, rawOffset, dstSavings);
    }

    /**
     * {@inheritDoc}
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean isSameAs(TimeZoneRule other) {
        if (!(other instanceof InitialTimeZoneRule)) {
            return false;
        }
        return super.isSameAs(other);
    }
}
