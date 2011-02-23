/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.Date;

import com.ibm.icu.util.BasicTimeZone;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZoneRule;
import com.ibm.icu.util.TimeZoneTransition;

/**
 * @author yumaoka
 *
 */
public class ICUTimeZone extends BasicTimeZone {

    private static final long serialVersionUID = 4733188155126057601L;

    private BasicTimeZone _btz;

    /**
     * Constructs a ICUTimeZone by wrapping
     * a BasicTimeZone.
     * 
     * Note: This constructor does not clone the given
     * time zone for efficiency. Caller should not modify
     * the BaseTimeZone.
     * 
     * @param imtz The ImmutableTimeZone instance
     */
    public ICUTimeZone(ImmutableTimeZone imtz) {
        super.setID(imtz.getID());
        _btz = imtz;
    }

    // TimeZone methods -------------------------------------------------------

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(int, int, int, int, int, int)
     */
    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        return _btz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#setRawOffset(int)
     */
    @Override
    public void setRawOffset(int offsetMillis) {
        if (_btz instanceof ImmutableTimeZone) {
            _btz = ((ImmutableTimeZone) _btz).getBasicTimeZone();
        }
        _btz.setRawOffset(offsetMillis);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getRawOffset()
     */
    @Override
    public int getRawOffset() {
        return _btz.getRawOffset();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#useDaylightTime()
     */
    @Override
    public boolean useDaylightTime() {
        return _btz.useDaylightTime();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#inDaylightTime(java.util.Date)
     */
    @Override
    public boolean inDaylightTime(Date date) {
        return _btz.inDaylightTime(date);
    }

    // TimeZone non-abstract methods ------------------------------------------

//    /*
//     * (non-Javadoc)
//     * @see com.ibm.icu.util.TimeZone#setID(java.lang.String)
//     */
//    public void setID(String ID) {
//        super.setID(ID);
//    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getDSTSavings()
     */
    public int getDSTSavings() {
        return _btz.getDSTSavings();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(long, boolean, int[])
     */
    public void getOffset(long date, boolean local, int[] offsets) {
        _btz.getOffset(date, local, offsets);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#hasSameRules(com.ibm.icu.util.TimeZone)
     */
    public boolean hasSameRules(TimeZone other) {
        return _btz.hasSameRules(other);
    }

    // Object methods ----------------------------------------------------------

    public Object clone() {
        ICUTimeZone copy = (ICUTimeZone)super.clone();
        copy._btz = getBasicTimeZone();
        return copy;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ICUTimeZone) {
            return _btz.equals(((ICUTimeZone)obj)._btz);
        }
        return false;
    }

    public int hashCode() {
        return _btz.hashCode();
    }

    // BasicTimeZone methods --------------------------------------------------

    /* (non-Javadoc)
     * @see com.ibm.icu.util.BasicTimeZone#getNextTransition(long, boolean)
     */
    @Override
    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        return _btz.getNextTransition(base, inclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.BasicTimeZone#getPreviousTransition(long, boolean)
     */
    @Override
    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        return _btz.getPreviousTransition(base, inclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.BasicTimeZone#getTimeZoneRules()
     */
    @Override
    public TimeZoneRule[] getTimeZoneRules() {
        return _btz.getTimeZoneRules();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.BasicTimeZone#getOffsetFromLocal(long, int, int, int[])
     */
    public void getOffsetFromLocal(long date,
            int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        _btz.getOffsetFromLocal(date, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
    }

    // ICUTimeZone specific methods -------------------------------------

    /**
     * Returns a safe BasicTimeZone clone or a ImmutableTimeZone
     */
    public BasicTimeZone getBasicTimeZone() {
        if (_btz instanceof ImmutableTimeZone) {
            return _btz;
        }
        return (BasicTimeZone)_btz.clone();
    }
}
