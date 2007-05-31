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
public class TimeArrayTimeZoneRule extends TimeZoneTransitionRule {

    private static final long serialVersionUID = -1117109130077415245L;

    private final long[] startTimes;
    private final int timeType;

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
     *                      (January 1, 1970, 00:00:00).
     * @param timeType      The time type of the start times, which is one of
     *                      DataTimeRule.WALL_TIME, STANDARD_TIME and UNIVERSAL_TIME.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeArrayTimeZoneRule(String name, int rawOffset, int dstSavings, long[] startTimes, int timeType) {
        super(name, rawOffset, dstSavings);
        if (startTimes == null || startTimes.length == 0) {
            throw new IllegalArgumentException("No start times are specified.");
        } else {
            this.startTimes = (long[])startTimes.clone();
            Arrays.sort(this.startTimes);
        }
        this.timeType = timeType;
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

    /**
     * Gets the time type of the start times used by this rule.  The return value
     * is either DateTimeRule.WALL_TIME or DateTimeRule.STANDARD_TIME or
     * DateTimeRule.UNIVERSAL_TIME.
     * 
     * @return The time type used of the start times used by this rule.
     */
    public int getTimeType() {
        return timeType;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneTransitionRule#getFirstStart(int, int)
     */
    public Date getFirstStart(int prevRawOffset, int prevDSTSavings) {
        return new Date(getUTC(startTimes[0], prevRawOffset, prevDSTSavings));
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneTransitionRule#getFinalStart(int, int)
     */
    public Date getFinalStart(int prevRawOffset, int prevDSTSavings) {
        return new Date(getUTC(startTimes[startTimes.length - 1], prevRawOffset, prevDSTSavings));
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneTransitionRule#getNextStart(long, int, int, boolean)
     */
    public Date getNextStart(long base, int prevOffset, int prevDSTSavings, boolean inclusive) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            long time = getUTC(startTimes[i], prevOffset, prevDSTSavings);
            if (time < base || (!inclusive && time == base)) {
                break;
            }
        }
        if (i == startTimes.length - 1) {
            return null;
        }
        return new Date(getUTC(startTimes[i + 1], prevOffset, prevDSTSavings));
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneTransitionRule#getPreviousStart(long, int, int, boolean)
     */
    public Date getPreviousStart(long base, int prevOffset, int prevDSTSavings, boolean inclusive) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            long time = getUTC(startTimes[i], prevOffset, prevDSTSavings);
            if (time < base || (inclusive && time == base)) {
                return new Date(time);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneRule#isSameAs(com.ibm.icu.util.TimeZoneRule)
     */
    public boolean isSameAs(TimeZoneRule other) {
        if (!(other instanceof TimeArrayTimeZoneRule)) {
            return false;
        }
        if (timeType == ((TimeArrayTimeZoneRule)other).timeType
                && Arrays.equals(startTimes, ((TimeArrayTimeZoneRule)other).startTimes)) {
            return super.isSameAs(other);
        }
        return false;
    }

    /* Get UTC of the time with the raw/dst offset */
    private long getUTC(long time, int raw, int dst) {
        if (timeType == DateTimeRule.STANDARD_TIME) {
            time -= raw;
        }
        if (timeType == DateTimeRule.WALL_TIME) {
            time -= dst;
        }
        return time;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneRule#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(", timeType=");
        buf.append(timeType);
        buf.append(", startTimes=");
        buf.append(Arrays.toString(startTimes));
        return buf.toString();
    }
}
