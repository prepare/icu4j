/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.Serializable;

/**
 * TimeZoneRule is an abstract class representing a rule for time zone.
 * TimeZoneRule has a set of time zone attributes, such as zone name,
 * raw offset (UTC offset for standard time) and daylight saving time offset.
 * There are 3 known subclasses which exnted TimeZoneRule.  InitialTimeZoneRule
 * represents the iniital offsets and name.  AnnualTimeZoneRule and
 * TimeArrayTimeZoneRule have rules for start times in addition to
 * offsets and name.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public abstract class TimeZoneRule implements Serializable {
    private final String name;
    private final int rawOffset;
    private final int dstSavings;

    /**
     * Constructs a TimeZoneRule with the name, the GMT offset of its
     * standard time and the amount of daylight saving offset adjustment.
     * 
     * @param name      The time zone name.
     * @param rawOffset The UTC offset of its standard time in milliseconds.
     * @param dstSaving The amount of daylight saving offset adjustment in milliseconds.
     *                  If this ia a rule for standard time, the value of this argument is 0.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
    */
    public TimeZoneRule(String name, int rawOffset, int dstSavings) {
        this.name = name;
        this.rawOffset = rawOffset;
        this.dstSavings = dstSavings;
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
    public int getRawOffset() {
        return rawOffset;
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
    public int getDSTSavings() {
        return dstSavings;
    }

    /**
     * Returns whether the rule has start times.
     * 
     * @return true if this rule has start times.
     */
    public abstract boolean hasStartTimes();
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("name=" + name);
        buf.append(", stdOffset=" + rawOffset);
        buf.append(", dstSaving=" + dstSavings);
        return buf.toString();
    }
}
