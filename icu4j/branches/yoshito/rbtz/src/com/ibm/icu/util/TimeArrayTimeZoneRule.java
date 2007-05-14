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

    private final long[] startTimes;

    /**
     * Constructs a TimeArrayTimeZoneRule with the name, the GMT offset of its
     * standard time, the amount of daylight saving offset adjustment and
     * the array of times when this rule takes effect.
     * 
     * @param name          The time zone name.
     * @param stdOffset     The GMT offset of its standard time in milliseconds.
     * @param dstSaving     The amount of daylight saving offset adjustment in
     *                      milliseconds.  If this ia a rule for standard time,
     *                      the value of this argument is 0.
     * @param startTimes    The start times in milliseconds since the base time
     *                      (January 1, 1970, 00:00:00 GMT).
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeArrayTimeZoneRule(String name, int stdOffset, int dstSaving, long[] startTimes) {
        super(name, stdOffset, dstSaving);
        if (startTimes == null || startTimes.length == 0) {
            throw new IllegalArgumentException("No start times are specified.");
        } else {
            this.startTimes = new long[startTimes.length];
            System.arraycopy(startTimes, 0, this.startTimes, 0, startTimes.length);
            Arrays.sort(this.startTimes);
        }
    }

    /**
     * Gets the very first time when this rule starts.
     * 
     * @return  The very first time when this rule takes starts,
     *          or null if this rule is an initial rule.
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
     * @return  The very last time when this rule starts,
     *          or null if this rule is an initial rule.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getFinalStart() {
        return new Date(startTimes[startTimes.length - 1]);
    }

    /**
     * Gets the time when this rule starts after the specified time.
     * 
     * @param base  The first time after this time is returned.  The base time
     *              is exclusive.
     * @return  The first time when this rule starts after the specified time,
     *          or null when this rule never starts after the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getNextStart(long base) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            if (startTimes[i] < base) {
                break;
            }
        }
        if (i == startTimes.length - 1) {
            return null;
        }
        return new Date(startTimes[i + 1]);
    }
}
