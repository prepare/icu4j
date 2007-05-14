package com.ibm.icu.util;

/**
 * The HasTimeZoneTransitions interface may be implemented by TimeZone
 * implementation classes to allow users to access time zone transition
 * information.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public interface HasTimeZoneTransitions {
    /**
     * Gets the first time zone transition after the base time.
     * 
     * @param base  The base time (exclusive).
     *               
     * @return  A Date holding the first time zone transition time after the given
     *          base time, or null if no time zone transitions are available after
     *          the base time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneTransition getNextTransition(long base);

    /**
     * Gets the last time zone transition before the base time.
     * 
     * @param base  The base time (exclusive).
     *               
     * @return  A Date holding the last time zone transition time before the given
     *          base time, or null if no time zone transitions are available before
     *          the base time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public TimeZoneTransition getPreviousTransition(long base);
}
