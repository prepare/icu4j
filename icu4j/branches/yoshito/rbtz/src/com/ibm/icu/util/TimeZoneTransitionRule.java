/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.util.Date;

/**
 * <code>TimeZoneTransitionRule</code> is an abstract class extending
 * <code>TimeZoneRule</code> and representing time zone transitions.
 * A concrete subclass will provide methods to access times when the rule
 * takes effect.  There are two known concrete subclases - <code>AnnualTimeZoneRule</code>,
 * which defines the start times by annually repeated date time rule, and
 * <code>TimeArrayTimeZoneRule</code>, which defines the start times by a simple
 * array of times.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public abstract class TimeZoneTransitionRule extends TimeZoneRule {

    /**
     * Constructs a <code>TimeZoneTransitionRule</code> with the name, the GMT offset
     * of its standard time and the amount of daylight saving offset adjustment.
     * 
     * @param name          The time zone name.
     * @param rawOffset     The UTC offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in milliseconds.
     *                      If this ia a rule for standard time, the value of this argument is 0.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneTransitionRule(String name, int rawOffset, int dstSavings) {
        super(name, rawOffset, dstSavings);
    }

    /**
     * Gets the very first time when this rule takes effect.
     * 
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * 
     * @return  The very first time when this rule takes effect.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public abstract Date getFirstStart(int prevRawOffset, int prevDSTSavings);

    /**
     * Gets the final time when this rule takes effect.
     * 
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * 
     * @return  The very last time when this rule takes effect,
     *          or null if this rule is applied for future dates infinitely.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public abstract Date getFinalStart(int prevRawOffset, int prevDSTSavings);

    /**
     * Gets the first time when this rule takes effect after the specified time.
     * 
     * @param base              The first time after this time is returned.
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * @param inclusive         Whether the base time is inclusive or not.
     * 
     * @return  The first time when this rule takes effect after the specified time,
     *          or null when this rule never takes effect after the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public abstract Date getNextStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive);

    /**
     * Gets the most recent time when this rule takes effect before the specified time.
     * 
     * @param base              The most recent time when this rule takes effect before
     *                          this time is returned.
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * @param inclusive         Whether the base time is inclusive or not.
     * 
     * @return  The most recent time when this rule takes effect before the specified time,
     *          or null when this rule never takes effect before the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public abstract Date getPreviousStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive);
}
