/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

/**
 * TimeZoneRule is a class representing a rule for time zone.
 * An instance of TimeZoneRule has a set of time zone attributes,
 * such as zone name, GMT offset and daylight saving time offset.
 * The subclasses may provide methods to get actual time when the
 * time zone defined by the rule takes effect.  Typically, a
 * practical time zone can be defined by a set of TimeZoneRule
 * instances.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class TimeZoneRule {

    private final String name;
    private final int stdOffset;
    private final int dstSaving;

    /**
     * Constructs a TimeZoneRule with the name, the GMT offset of its
     * standard time and the amount of daylight saving offset adjustment.
     * 
     * @param name      The time zone name.
     * @param stdOffset The GMT offset of its standard time in milliseconds.
     * @param dstSaving The amount of daylight saving offset adjustment in milliseconds.
     *                  If this ia a rule for standard time, the value of this argument is 0.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
    */
    public TimeZoneRule(String name, int stdOffset, int dstSaving) {
        this.name = name;
        this.stdOffset = stdOffset;
        this.dstSaving = dstSaving;
    }

    /**
     * Gets the name of this time zone.
     * 
     * @return The name of this time zone.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the standard time offset.
     * 
     * @return The standard time offset from UTC in milliseconds.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getStdOffset() {
        return stdOffset;
    }

    /**
     * Gets the amount of daylight saving delta time from the standard time.
     * 
     * @return  The amount of daylight saving offset used by this rule
     *          in milliseconds.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getDstSaving() {
        return dstSaving;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("name=" + getName());
        buf.append(", stdOffset=" + getStdOffset());
        buf.append(", dstSaving=" + getDstSaving());
        return buf.toString();
    }
}
