/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

/**
 * VTimeZone is a class implementing RFC2445 VTIMEZONE.  You can create a
 * VTimeZone instance from a time zone ID supported by ICU TimeZone.  With
 * the VTimeZone instance created from the ID, you can write out the rule
 * in RFC2445 VTIMEZONE format.  Also, you can create a VTimeZone instance
 * from RFC2445 VTIMEZONE data stream, which allows you to calculate time
 * zone offset by the rules defined by the data.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class VTimeZone extends TimeZone {

    private static final long serialVersionUID = 1L; //TODO

    private TimeZone tz;

    /**
     * Create a VTimeZone instance by the time zone ID.
     * 
     * @param tzid The time zone ID, such as America/New_York
     * @return A VTimeZone initialized by the time zone ID, or null
     * when the ID is unknown.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static VTimeZone create(String tzid) {
        VTimeZone vtz = new VTimeZone();
        vtz.tz = TimeZone.getTimeZone(tzid);
        vtz.setID(tzid);

        return vtz;
    }
    
    /**
     * Create a VTimeZone instance by RFC2445 VTIMEZONE data
     * 
     * @param reader The Reader for VTIMEZONE data input stream
     * @return A VTimeZone initialized by the VTIMEZONE data or
     * null if failed to load the rule from the VTIMEZONE data.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static VTimeZone create(Reader reader) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(int, int, int, int, int, int)
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int milliseconds) {
        return tz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getRawOffset()
     */
    public int getRawOffset() {
        return tz.getRawOffset();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#inDaylightTime(java.util.Date)
     */
    public boolean inDaylightTime(Date date) {
        return tz.inDaylightTime(date);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#setRawOffset(int)
     */
    public void setRawOffset(int offsetMillis) {
        tz.setRawOffset(offsetMillis);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#useDaylightTime()
     */
    public boolean useDaylightTime() {
        return tz.useDaylightTime();
    }

    /**
     * Writes RFC2445 VTIMEZONE data for this time zone
     * 
     * @param writer A Writer used for the output
     * @return true if the VTIMEZONE data is successfully written
     * @throws IOException
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean write(Writer writer) throws IOException {
        return false;
    }

    /**
     * Writes RFC2445 VTIMEZONE data applicalbe for the specified time
     * range
     * 
     * @param writer The Writer used for the output
     * @param start The start time
     * @param end The end time
     * @return true if the VTIMEZONE data is successfully written
     * @throws IOException
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean write(Writer writer, long start, long end) throws IOException {
        return false;
    }

}
