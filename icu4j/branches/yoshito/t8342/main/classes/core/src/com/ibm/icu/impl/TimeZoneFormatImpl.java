/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.Date;

import com.ibm.icu.text.TimeZoneFormat;
import com.ibm.icu.text.TimeZoneNames.NameType;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public class TimeZoneFormatImpl extends TimeZoneFormat {

    private ULocale _locale;

    private transient boolean _frozen;

    public TimeZoneFormatImpl(ULocale locale) {
        
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongGeneric(TimeZone tz, long date) {
        // TODO location fallback and partial location name
        String name = getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_GENERIC, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongSpecific(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongSpecific(TimeZone tz, long date) {
        boolean isDaylight = tz.inDaylightTime(new Date(date));
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_DAYLIGHT, date, null) :
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_STANDARD, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatShortGeneric(TimeZone tz, long date) {
        // TODO location fallback and partial location name
        String name = getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_GENERIC, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortSpecific(com.ibm.icu.util.TimeZone, long, boolean)
     */
    @Override
    protected String handleFormatShortSpecific(TimeZone tz, long date, boolean all) {
        boolean isDaylight = tz.inDaylightTime(new Date(date));
        boolean[] isCommonlyUsed = new boolean[1];
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_DAYLIGHT, date, isCommonlyUsed) :
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_STANDARD, date, isCommonlyUsed);

        if (!all && !isCommonlyUsed[0]) {
            name = null;
        }
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatGenericLocation(com.ibm.icu.util.TimeZone)
     */
    @Override
    protected String handleFormatGenericLocation(TimeZone tz) {
        // TODO format location
        String name = getTimeZoneNames().getExemplarLocationName(tz.getID());
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongGeneric(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseLongGeneric(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongSpecific(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseLongSpecific(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortGeneric(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseShortGeneric(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortSpecific(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseShortSpecific(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseGenericLocation(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseGenericLocation(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#isFrozen()
     */
    public boolean isFrozen() {
        return _frozen;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#freeze()
     */
    public TimeZoneFormat freeze() {
        _frozen = true;
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#cloneAsThawed()
     */
    public TimeZoneFormat cloneAsThawed() {
        TimeZoneFormatImpl copy = (TimeZoneFormatImpl)super.clone();
        copy._frozen = false;
        return copy;
    }

}
