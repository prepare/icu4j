/*
 *******************************************************************************
 * Copyright (C) 2005-2008, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.util.Date;
import java.util.Locale;

/**
 * Base class for EthiopicCalendar and CopticCalendar.
 * @internal
 * @deprecated This API is ICU internal only.
 */
abstract class CECalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = -999547623066414271L;

    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        0,        0,       1,       1 }, // ERA
        {        1,        1, 5000000, 5000000 }, // YEAR
        {        0,        0,      12,      12 }, // MONTH
        {        1,        1,      52,      53 }, // WEEK_OF_YEAR
        {/*                                  */}, // WEEK_OF_MONTH
        {        1,        1,       5,      30 }, // DAY_OF_MONTH
        {        1,        1,     365,     366 }, // DAY_OF_YEAR
        {/*                                  */}, // DAY_OF_WEEK
        {       -1,       -1,       1,       5 }, // DAY_OF_WEEK_IN_MONTH
        {/*                                  */}, // AM_PM
        {/*                                  */}, // HOUR
        {/*                                  */}, // HOUR_OF_DAY
        {/*                                  */}, // MINUTE
        {/*                                  */}, // SECOND
        {/*                                  */}, // MILLISECOND
        {/*                                  */}, // ZONE_OFFSET
        {/*                                  */}, // DST_OFFSET
        { -5000000, -5000000, 5000000, 5000000 }, // YEAR_WOY
        {/*                                  */}, // DOW_LOCAL
        { -5000000, -5000000, 5000000, 5000000 }, // EXTENDED_YEAR
        {/*                                  */}, // JULIAN_DAY
        {/*                                  */}, // MILLISECONDS_IN_DAY
    };

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>CECalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    protected CECalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault());
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the default locale.
     *
     * @param zone The time zone for the new calendar.
     */
    protected CECalendar(TimeZone zone) {
        this(zone, ULocale.getDefault());
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale The locale for the new calendar.
     */
    protected CECalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale The locale for the new calendar.
     */
    protected CECalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param aLocale The locale for the new calendar.
     */
    protected CECalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>CECalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone The time zone for the new calendar.
     *
     * @param locale The locale for the new calendar.
     */
    protected CECalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     */
    protected CECalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault());
        this.set(year, month, date);
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    protected CECalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault());
        this.setTime(date);
    }

    /**
     * Constructs a <code>CECalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field.
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for Tishri.
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     */
    protected CECalendar(int year, int month, int date, int hour,
                         int minute, int second)
    {
        super(TimeZone.getDefault(), ULocale.getDefault());
        this.set(year, month, date, hour, minute, second);
    }

    //-------------------------------------------------------------------------
    // Calendar framework
    //-------------------------------------------------------------------------

    /**
     * The Coptic and Ethiopic calendars differ only in their epochs.
     * This method must be implemented by CECalendar subclasses to
     * return the date offset from Julian.
     */
    abstract protected int getJDEpochOffset();

    /**
     * Return JD of start of given month/extended year
     */
    protected int handleComputeMonthStart(int eyear,
                                          int emonth,
                                          boolean useMonth) {
        return ceToJD(eyear, emonth, 0, getJDEpochOffset());
    }

    /**
     * Calculate the limit for a specified type of limit and field
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    //-------------------------------------------------------------------------
    // Calendar framework
    //-------------------------------------------------------------------------

    /**
     * Convert an Coptic/Ethiopic year, month and day to a Julian day
     * @param year the extended year
     * @param month the month
     * @param day the day
     * @return Julian day
     */
    public static int ceToJD(long year, int month, int day, int jdEpochOffset) {

        // Julian<->Ethiopic algorithms from:
        // "Calendars in Ethiopia", Berhanu Beyene, Manfred Kudlek, International Conference
        // of Ethiopian Studies XV, Hamburg, 2003

        return (int) (
            (jdEpochOffset+365)     // difference from Julian epoch to 1,1,1
            + 365 * (year - 1)      // number of days from years
            + floorDivide(year, 4)  // extra day of leap year
            + 30 * (month + 1)      // number of days from months
            + day                   // number of days for present month
            - 31                    // slack?
            );
    }

    /**
     * Convert a Julian day to an Coptic/Ethiopic year, month and day
     */
    public static void jdToCE(int julianDay, int jdEpochOffset, int[] fields) {
        int c4; // number of 4 year cycle (1461 days)
        int[] r4 = new int[1]; // remainder of 4 year cycle, always positive

        c4 = floorDivide(julianDay - jdEpochOffset, 1461, r4);

        // exteded year
        fields[0] = 4 * c4 + (r4[0]/365 - r4[0]/1460); // 4 * <number of 4year cycle> + <years within the last cycle>

        int doy = (r4[0] == 1460) ? 365 : (r4[0] % 365); // days in present year

        // month
        fields[1] = doy / 30; // 30 -> Coptic/Ethiopic month length up to 12th month
        // day
        fields[2] = (doy % 30) + 1; // 1-based days in a month
    }
}
