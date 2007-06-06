/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.Serializable;

/**
 * <code>TimeZoneRule</code> is an abstract class representing a rule for time zone.
 * <code>TimeZoneRule</code> has a set of time zone attributes, such as zone name,
 * raw offset (UTC offset for standard time) and daylight saving time offset.
 * There are 3 known concrete subclasses which exnted <code>TimeZoneRule</code>.
 * <code>InitialTimeZoneRule</code> represents the iniital offsets and name.
 * <code>AnnualTimeZoneRule</code> and <code>TimeArrayTimeZoneRule</code> have rules
 * for start times in addition to offsets and name.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public abstract class TimeZoneRule implements Serializable {
    private final String name;
    private final int rawOffset;
    private final int dstSavings;

    /**
     * Constructs a <code>TimeZoneRule</code> with the name, the GMT offset of its
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
     * Returns if this rule represents the same rule and offsets as another.
     * When two <code>TimeZoneRule</code> objects differ only its names, this method returns
     * true.
     *
     * @param other The <code>TimeZoneRule</code> object to be compared with.
     * @return true if the other <code>TimeZoneRule</code> is the same as this one.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean isSameAs(TimeZoneRule other) {
        if (rawOffset == other.rawOffset && dstSavings == other.dstSavings) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns a <code>String</code> representation of this <code>TimeZoneRule</code> object.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("name=" + name);
        buf.append(", stdOffset=" + rawOffset);
        buf.append(", dstSaving=" + dstSavings);
        return buf.toString();
    }
}
