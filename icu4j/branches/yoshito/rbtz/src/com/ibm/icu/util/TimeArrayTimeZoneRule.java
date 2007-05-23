/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;
import java.util.Arrays;
import java.util.Date;

/**
 * TimeArrayTimeZoneRule represents a time zone rule whose start times are
 * defined by an array of milliseconds since the standard base time.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class TimeArrayTimeZoneRule extends TimeZoneRule {

    private static final long serialVersionUID = 1067689314077468618L;

    private final long[] startTimes;

    /**
     * Constructs a TimeArrayTimeZoneRule with the name, the GMT offset of its
     * standard time, the amount of daylight saving offset adjustment and
     * the array of times when this rule takes effect.
     * 
     * @param name          The time zone name.
     * @param rawOffset     The UTC offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in
     *                      milliseconds.  If this ia a rule for standard time,
     *                      the value of this argument is 0.
     * @param startTimes    The start times in milliseconds since the base time
     *                      (January 1, 1970, 00:00:00 GMT).
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeArrayTimeZoneRule(String name, int rawOffset, int dstSavings, long[] startTimes) {
        super(name, rawOffset, dstSavings);
        if (startTimes == null || startTimes.length == 0) {
            throw new IllegalArgumentException("No start times are specified.");
        } else {
            this.startTimes = (long[])startTimes.clone();
            Arrays.sort(this.startTimes);
        }
    }

    /**
     * Gets the very first time when this rule starts.
     * 
     * @return  The very first time when this rule starts.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getFirstStart() {
        return new Date(startTimes[0]);
    }

    /**
     * Gets the very last time when this rule starts.
     * 
     * @return  The very last time when this rule starts.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getFinalStart() {
        return new Date(startTimes[startTimes.length - 1]);
    }

    /**
     * Gets the very first time when this rule starts after the specified time.
     * 
     * @param base      The very first time after this time is returned.
     * @param inclusive Whether the base time is inclusive or not.
     * @return  The very first time when this rule starts after the specified time,
     *          or null when this rule never starts after the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getNextStart(long base, boolean inclusive) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            if (startTimes[i] < base || (!inclusive && startTimes[i] == base)) {
                break;
            }
        }
        if (i == startTimes.length - 1) {
            return null;
        }
        return new Date(startTimes[i + 1]);
    }

    /**
     * Gets the most recent time when this rule starts before the specified time.
     * 
     * @param base      The most recent time before this time is returned.
     * @param inclusive Whether the base time is inclusive or not.
     * @return  The most recent time when this rule starts before the specified time,
     *          or null when this rule never starts before the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getPreviousStart(long base, boolean inclusive) {
        int i = startTimes.length - 1;

        for (; i >= 0; i--) {
            if (startTimes[i] < base || (inclusive && startTimes[i] == base)) {
                return new Date(startTimes[i]);
            }
        }
        return null;
    }

    /**
     * Gets the array of start times used by this rule.
     * 
     * @return  An array of the start times in milliseconds since the base time
     *          (January 1, 1970, 00:00:00 GMT).
     */
    public long[] getStartTimes() {
        return (long[])startTimes.clone();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneRule#hasStartTimes()
     */
    public boolean hasStartTimes() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneRule#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(", startTimes=" + Arrays.toString(startTimes));
        return buf.toString();
    }
}
