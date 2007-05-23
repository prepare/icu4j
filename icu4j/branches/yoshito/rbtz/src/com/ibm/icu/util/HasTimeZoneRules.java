/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

/**
 * The HasTimeZoneTransitions interface may be implemented by TimeZone
 * implementation subclasses to allow users to access time zone transition
 * information.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public interface HasTimeZoneRules {
    /**
     * Gets the first time zone transition after the base time.
     * 
     * @param base      The base time.
     * @param inclusive Whether the base time is inclusive or not.
     *               
     * @return  A Date holding the first time zone transition time after the given
     *          base time, or null if no time zone transitions are available after
     *          the base time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneTransition getNextTransition(long base, boolean inclusive);

    /**
     * Gets the last time zone transition before the base time.
     * 
     * @param base      The base time.
     * @param inclusive Whether the base time is inclusive or not.
     *               
     * @return  A Date holding the last time zone transition time before the given
     *          base time, or null if no time zone transitions are available before
     *          the base time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive);

    /**
     * Checks if the time zone has equivalent transitions in the time range.
     * This method returns true when all of transition times, from/to standard
     * offsets and DST savings used by this time zone match the other in the
     * time range.
     * 
     * @param tz    The instance of TimeZone
     * @param start The start time of the evaluated time range (inclusive)
     * @param end   The end time of the evaluated time range (inclusive)
     * 
     * @return true if the other time zone has the equivalent transitions in the
     * time range.  When tz is not implementing HasTimeZoneTransitions, this method
     * returns false.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end);


    /**
     * Gets the array of TimeZoneRule which represents the rule of this time zone
     * object.  The first element in the result array will be always an instance of
     * InitialTimeZoneRule.  The rest are either AnnualTimeZoneRule or
     * TimeArrayTimeZoneRule instances.
     * 
     * @return The array of TimeZoneRule which represents this time zone.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneRule[] getTimeZoneRules();
    
    /**
     * Gets the array of TimeZoneRule which represents the rule of this time zone
     * object since the specified cut over time.  The first element in the result
     * array will be always an instance of InitialTimeZoneRule.  The rest are
     * either AnnualTimeZoneRule or TimeArrayTimeZoneRule instances.
     * 
     * @param start The cut over time (inclusive).
     * @return The array of TimeZoneRule which represents this time zone since
     * the cut over time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneRule[] getTimeZoneRules(long start);
}
