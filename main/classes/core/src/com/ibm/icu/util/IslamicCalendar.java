/*
 *******************************************************************************
 * Copyright (C) 1996-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;

import com.ibm.icu.impl.CalendarAstronomer;
import com.ibm.icu.impl.CalendarCache;
import com.ibm.icu.impl.CalendarUtil;
import com.ibm.icu.util.ULocale.Category;

/**
 * <code>IslamicCalendar</code> is a subclass of <code>Calendar</code>
 * that that implements the Islamic civil and religious calendars.  It
 * is used as the civil calendar in most of the Arab world and the
 * liturgical calendar of the Islamic faith worldwide.  This calendar
 * is also known as the "Hijri" calendar, since it starts at the time
 * of Mohammed's emigration (or "hijra") to Medinah on Thursday, 
 * July 15, 622 AD (Julian).
 * <p>
 * The Islamic calendar is strictly lunar, and thus an Islamic year of twelve
 * lunar months does not correspond to the solar year used by most other
 * calendar systems, including the Gregorian.  An Islamic year is, on average,
 * about 354 days long, so each successive Islamic year starts about 11 days
 * earlier in the corresponding Gregorian year.
 * <p>
 * Each month of the calendar starts when the new moon's crescent is visible
 * at sunset.  However, in order to keep the time fields in this class
 * synchronized with those of the other calendars and with local clock time,
 * we treat days and months as beginning at midnight,
 * roughly 6 hours after the corresponding sunset.
 * <p>
 * There are three main variants of the Islamic calendar in existence.  The first
 * is the <em>civil</em> calendar, which uses a fixed cycle of alternating 29-
 * and 30-day months, with a leap day added to the last month of 11 out of
 * every 30 years.  This calendar is easily calculated and thus predictable in
 * advance, so it is used as the civil calendar in a number of Arab countries.
 * This is the default behavior of a newly-created <code>IslamicCalendar</code>
 * object.
 * <p>
 * The Islamic <em>religious</em> calendar and Saudi Arabia's <em>Umm al-Qura</em> 
 * calendar, however, are based on the <em>observation</em> of the crescent moon.  
 * It is thus affected by the position at which the
 * observations are made, seasonal variations in the time of sunset, the
 * eccentricities of the moon's orbit, and even the weather at the observation
 * site.  This makes it impossible to calculate in advance, and it causes the
 * start of a month in the religious calendar to differ from the civil calendar
 * by up to three days.
 * <p>
 * Using astronomical calculations for the position of the sun and moon, the
 * moon's illumination, and other factors, it is possible to determine the start
 * of a lunar month with a fairly high degree of certainty.  However, these
 * calculations are extremely complicated and thus slow, so most algorithms,
 * including the one used here, are only approximations of the true astronomical
 * calculations.  At present, the approximations used in this class are fairly
 * simplistic; they will be improved in later versions of the code.
 * <p>
 * Like the Islamic religious calendar, <em>Umm al-Qura</em> is also based 
 * on the sighting method of the crescent moon but is standardized by Saudi Arabia.
 * <p>  
 * The {@link #setType setType} method determines
 * which approach is used to determine the start of a month.  By default, the
 * fixed-cycle <em>civil</em> calendar is used.  However, if <code>setType(ISLAMIC)</code>
 * is called, an approximation of the true lunar calendar will be used.
 * Similarly, if <code>setType(ISLAMIC_UMALQURA)</code> is called, an approximation 
 * of the Umm al-Qura lunar calendar will be used.
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * IslamicCalendar usually should be instantiated using 
 * {@link com.ibm.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=islamic"</code> or <code>"@calendar=islamic-civil"</code> 
 * or <code>"@calendar=islamic-umalqura"</code>.</p>
 *
 * @see com.ibm.icu.util.GregorianCalendar
 * @see com.ibm.icu.util.Calendar
 *
 * @author Laura Werner
 * @author Alan Liu
 * @stable ICU 2.8
 */
public class IslamicCalendar extends Calendar {
    // jdk1.4.2 serialver
    private static final long serialVersionUID = -6253365474073869325L;

    //-------------------------------------------------------------------------
    // Constants...
    //-------------------------------------------------------------------------
    
    /**
     * Constant for Muharram, the 1st month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int MUHARRAM = 0;

    /**
     * Constant for Safar, the 2nd month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int SAFAR = 1;

    /**
     * Constant for Rabi' al-awwal (or Rabi' I), the 3rd month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int RABI_1 = 2;

    /**
     * Constant for Rabi' al-thani or (Rabi' II), the 4th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int RABI_2 = 3;

    /**
     * Constant for Jumada al-awwal or (Jumada I), the 5th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int JUMADA_1 = 4;

    /**
     * Constant for Jumada al-thani or (Jumada II), the 6th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int JUMADA_2 = 5;

    /**
     * Constant for Rajab, the 7th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int RAJAB = 6;

    /**
     * Constant for Sha'ban, the 8th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int SHABAN = 7;

    /**
     * Constant for Ramadan, the 9th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int RAMADAN = 8;

    /**
     * Constant for Shawwal, the 10th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int SHAWWAL = 9;

    /**
     * Constant for Dhu al-Qi'dah, the 11th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int DHU_AL_QIDAH = 10;

    /**
     * Constant for Dhu al-Hijjah, the 12th month of the Islamic year. 
     * @stable ICU 2.8 
     */
    public static final int DHU_AL_HIJJAH = 11;


    private static final long HIJRA_MILLIS = -42521587200000L;    // 7/16/622 AD 00:00

    /**
     * Friday EPOC
     */
    private static final long CIVIL_EPOC = 1948440;
    /**
     * Thursday EPOC
     */
    private static final long ASTRONOMICAL_EPOC = 1948439;

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>IslamicCalendar</code> using the current time
     * in the default time zone with the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     * @stable ICU 2.8
     */
    public IslamicCalendar()
    {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the given time zone with the default <code>FORMAT</code> locale.
     * @param zone the given time zone.
     * @see Category#FORMAT
     * @stable ICU 2.8
     */
    public IslamicCalendar(TimeZone zone)
    {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale the given locale.
     * @stable ICU 2.8
     */
    public IslamicCalendar(Locale aLocale)
    {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param locale the given ulocale.
     * @stable ICU 3.2
     */
    public IslamicCalendar(ULocale locale)
    {
        this(TimeZone.getDefault(), locale);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     * @param aLocale the given locale.
     * @stable ICU 2.8
     */
    public IslamicCalendar(TimeZone zone, Locale aLocale)
    {
        this(zone, ULocale.forLocale(aLocale));
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     * @param locale the given ulocale.
     * @stable ICU 3.2
     */
    public IslamicCalendar(TimeZone zone, ULocale locale)
    {
        super(zone, locale);
        setCalcTypeForLocale(locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param date      The date to which the new calendar is set.
     * @see Category#FORMAT
     * @stable ICU 2.8
     */
    public IslamicCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.setTime(date);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date set
     * in the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year the value used to set the {@link #YEAR YEAR} time field in the calendar.
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Muharram.
     * @param date the value used to set the {@link #DATE DATE} time field in the calendar.
     * @see Category#FORMAT
     * @stable ICU 2.8
     */
    public IslamicCalendar(int year, int month, int date)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date
     * and time set for the default time zone with the default <code>FORMAT</code> locale.
     *
     * @param year  the value used to set the {@link #YEAR YEAR} time field in the calendar.
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Muharram.
     * @param date  the value used to set the {@link #DATE DATE} time field in the calendar.
     * @param hour  the value used to set the {@link #HOUR_OF_DAY HOUR_OF_DAY} time field
     *              in the calendar.
     * @param minute the value used to set the {@link #MINUTE MINUTE} time field
     *              in the calendar.
     * @param second the value used to set the {@link #SECOND SECOND} time field
     *              in the calendar.
     * @see Category#FORMAT
     * @stable ICU 2.8
     */
    public IslamicCalendar(int year, int month, int date, int hour,
                             int minute, int second)
    {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
        this.set(Calendar.HOUR_OF_DAY, hour);
        this.set(Calendar.MINUTE, minute);
        this.set(Calendar.SECOND, second);
    }

    /**
     * Determines whether this object uses the fixed-cycle Islamic civil calendar
     * or an approximation of the religious, astronomical calendar.
     *
     * @param beCivil   <code>true</code> to use the civil calendar,
     *                  <code>false</code> to use the astronomical calendar.
     * @stable ICU 2.8
     */
    public void setCivil(boolean beCivil)
    {
        civil = beCivil;
        
        if (beCivil && cType != CalculationType.ISLAMIC_CIVIL) {
            // The fields of the calendar will become invalid, because the calendar
            // rules are different
            long m = getTimeInMillis();
            cType = CalculationType.ISLAMIC_CIVIL;
            clear();
            setTimeInMillis(m);
        } else if(!beCivil && cType != CalculationType.ISLAMIC) {
            // The fields of the calendar will become invalid, because the calendar
            // rules are different
            long m = getTimeInMillis();
            cType = CalculationType.ISLAMIC;
            clear();
            setTimeInMillis(m);
        }
    }
    
    /**
     * Returns <code>true</code> if this object is using the fixed-cycle civil
     * calendar, or <code>false</code> if using the religious, astronomical
     * calendar.
     * @stable ICU 2.8
     * 
     */
    public boolean isCivil() {
    	if(cType == CalculationType.ISLAMIC_CIVIL) {
            return true;
    	}
        return false;
    }
    
    //-------------------------------------------------------------------------
    // Minimum / Maximum access functions
    //-------------------------------------------------------------------------

    // Note: Current IslamicCalendar implementation does not work
    // well with negative years.

    private static final int LIMITS[][] = {
        // Minimum  Greatest     Least   Maximum
        //           Minimum   Maximum
        {        0,        0,        0,        0}, // ERA
        {        1,        1,  5000000,  5000000}, // YEAR
        {        0,        0,       11,       11}, // MONTH
        {        1,        1,       50,       51}, // WEEK_OF_YEAR
        {/*                                   */}, // WEEK_OF_MONTH
        {        1,        1,       29,       30}, // DAY_OF_MONTH
        {        1,        1,      354,      355}, // DAY_OF_YEAR
        {/*                                   */}, // DAY_OF_WEEK
        {       -1,       -1,        5,        5}, // DAY_OF_WEEK_IN_MONTH
        {/*                                   */}, // AM_PM
        {/*                                   */}, // HOUR
        {/*                                   */}, // HOUR_OF_DAY
        {/*                                   */}, // MINUTE
        {/*                                   */}, // SECOND
        {/*                                   */}, // MILLISECOND
        {/*                                   */}, // ZONE_OFFSET
        {/*                                   */}, // DST_OFFSET
        {        1,        1,  5000000,  5000000}, // YEAR_WOY
        {/*                                   */}, // DOW_LOCAL
        {        1,        1,  5000000,  5000000}, // EXTENDED_YEAR
        {/*                                   */}, // JULIAN_DAY
        {/*                                   */}, // MILLISECONDS_IN_DAY
    };
    
    /*
     * bit map array where a bit turned on represents a month with 30 days. 
     */
    private static final int[] UMALQURA_MONTHLENGTH = {
        //* 1318 -1322 */ "0101 0111 0100", "1001 0111 0110", "0100 1011 0111", "0010 0101 0111", "0101 0010 1011",
                               0x0574,           0x0975,           0x06A7,           0x0257,           0x052B,            
        //* 1323 -1327 */ "0110 1001 0101", "0110 1100 1010", "1010 1101 0101", "0101 0101 1011", "0010 0101 1101",                 
                               0x0695,           0x06CA,           0x0AD5,           0x055B,           0x025B,                  
        //* 1328 -1332 */ "1001 0010 1101", "1100 1001 0101", "1101 0100 1010", "1110 1010 0101", "0110 1101 0010",                 
                               0x092D,           0x0C95,           0x0D4A,           0x0E5B,           0x025B,                  
        //* 1333 -1337 */ "1010 1101 0101", "0101 0101 1010", "1010 1010 1011", "0100 0100 1011", "0110 1010 0101",                 
                               0x0AD5,           0x055A,           0x0AAB,           0x044B,           0x06A5,                  
        //* 1338 -1342 */ "0111 0101 0010", "1011 1010 1001", "0011 0111 0100", "1010 1011 0110", "0101 0101 0110",                  
                               0x0752,           0x0BA9,           0x0374,           0x0AB6,           0x0556,                  
        //* 1343 -1347 */ "1010 1010 1010", "1101 0101 0010", "1101 1010 1001", "0101 1101 0100", "1010 1110 1010", 
                               0x0AAA,           0x0D52,           0x0DA9,           0x05D4,           0x0AEA,                  
        //* 1348 -1352 */ "0100 1101 1101", "0010 0110 1110", "1001 0010 1110", "1010 1010 0110", "1101 0101 0100", 
                               0x04DD,           0x026E,           0x092E,           0x0AA6,           0x0D54,                  
        //* 1353 -1357 */ "0101 1010 1010", "0101 1011 0101", "0010 1011 0100", "1001 0011 0111", "0100 1001 1011", 
                               0x05AA,           0x05B5,           0x02B4,           0x0937,           0x049B,                  
        //* 1358 -1362 */ "1010 0100 1011", "1011 0010 0101", "1011 0101 0100", "1011 0110 1010", "0101 0110 1101", 
                               0x0A4B,           0x0B25,           0x0B54,           0x0B6A,           0x056D,                  
        //* 1363 -1367 */ "0100 1010 1101", "1010 0101 0101", "1101 0010 0101", "1110 1001 0010", "1110 1100 1001", 
                               0x04AD,           0x0A55,           0x0D25,           0x0E92,           0x0EC9,                  
        //* 1368 -1372 */ "0110 1101 0100", "1010 1110 1010", "0101 0110 1011", "0100 1010 1011", "0110 1000 0101", 
                               0x06D4,           0x0ADA,           0x056B,           0x04AB,           0x0685,                  
        //* 1373 -1377 */ "1011 0100 1001", "1011 1010 0100", "1011 1011 0010", "0101 1011 0101", "0010 1011 1010", 
                               0x0B49,           0x0BA4,           0x0BB2,           0x05B5,           0x02BA,                  
        //* 1378 -1382 */ "1001 0101 1011", "0100 1010 1011", "0101 0101 0101", "0110 1011 0010", "0110 1101 1001", 
                               0x095B,           0x04AB,           0x0555,           0x06B2,           0x06D9,                  
        //* 1383 -1387 */ "0010 1110 1100", "1001 0110 1110", "0100 1010 1110", "1010 0101 0110", "1101 0010 1010", 
                               0x02EC,           0x096E,           0x04AE,           0x0A56,           0x0D2A,                  
        //* 1388 -1392 */ "1101 0101 0101", "0101 1010 1010", "1010 1011 0101", "0100 1011 1011", "0000 0101 1011", 
                               0x0D55,           0x05AA,           0x0AB5,           0x04BB,           0x005B,                  
        //* 1393 -1397 */ "1001 0010 1011", "1010 1001 0101", "0011 0100 1010", "1011 1010 0101", "0101 1010 1010", 
                               0x092B,           0x0A95,           0x034A,           0x0BA5,           0x05AA,                  
        //* 1398 -1402 */ "1010 1011 0101", "0101 0101 0110", "1010 1001 0110", "1101 0100 1010", "1110 1010 0101", 
                               0x0AB5,           0x0556,           0x0A96,           0x0B4A,           0x0EA5,                  
        //* 1403 -1407 */ "0111 0101 0010", "0110 1110 1001", "0011 0110 1010", "1010 1010 1101", "0101 0101 0101", 
                               0x0752,           0x06E9,           0x036A,           0x0AAD,           0x0555,                  
        //* 1408 -1412 */ "1010 1010 0101", "1011 0101 0010", "1011 1010 1001", "0101 1011 0100", "1001 1011 1010", 
                               0x0AA5,           0x0B52,           0x0BA9,           0x05B4,           0x09BA,                  
        //* 1413 -1417 */ "0100 1101 1011", "0010 0101 1101", "0101 0010 1101", "1010 1010 0101", "1010 1101 0100", 
                               0x04DB,           0x025D,           0x052D,           0x0AA5,           0x0AD4,              
        //* 1418 -1422 */ "1010 1110 1010", "0101 0110 1101", "0100 1011 1101", "0010 0011 1101", "1001 0001 1101", 
                               0x0AEA,           0x056D,           0x04BD,           0x023D,           0x091D,                  
        //* 1423 -1427 */ "1010 1001 0101", "1011 0100 1010", "1011 0101 1010", "0101 0110 1101", "0010 1011 0110", 
                               0x0A95,           0x0B4A,           0x0B5A,           0x056D,           0x02B6,                  
        //* 1428 -1432 */ "1001 0011 1011", "0100 1001 1011", "0110 0101 0101", "0110 1010 1001", "0111 0101 0100", 
                               0x093B,           0x049B,           0x0655,           0x06A9,           0x0754,                  
        //* 1433 -1437 */ "1011 0110 1010", "0101 0110 1100", "1010 1010 1101", "0101 0101 0101", "1011 0010 1001", 
                               0x0B6A,           0x056C,           0x0AAD,           0x0555,           0x0B29,                  
        //* 1438 -1442 */ "1011 1001 0010", "1011 1010 1001", "0101 1101 0100", "1010 1101 1010", "0101 0101 1010", 
                               0x0B92,           0x0BA9,           0x05D4,           0x0ADA,           0x055A,                  
        //* 1443 -1447 */ "1010 1010 1011", "0101 1001 0101", "0111 0100 1001", "0111 0110 0100", "1011 1010 1010", 
                               0x0AAB,           0x0595,           0x0749,           0x0764,           0x0BAA,                  
        //* 1448 -1452 */ "0101 1011 0101", "0010 1011 0110", "1010 0101 0110", "1110 0100 1101", "1011 0010 0101",
                               0x05B5,           0x02B6,           0x0A56,           0x0E4D,           0x0B25,                  
        //* 1453 -1457 */ "1011 0101 0010", "1011 0110 1010", "0101 1010 1101", "0010 1010 1110", "1001 0010 1111",
                               0x0B52,           0x0B6A,           0x05AD,           0x02AE,           0x092F,                  
        //* 1458 -1462 */ "0100 1001 0111", "0110 0100 1011", "0110 1010 0101", "0110 1010 1100", "1010 1101 0110",
                               0x0497,           0x064B,           0x06A5,           0x06AC,           0x0AD6,                  
        //* 1463 -1467 */ "0101 0101 1101", "0100 1001 1101", "1010 0100 1101", "1101 0001 0110", "1101 1001 0101",
                               0x055D,           0x049D,           0x0A4D,           0x0D16,           0x0D95,                  
        //* 1468 -1472 */ "0101 1010 1010", "0101 1011 0101", "0010 1001 1010", "1001 0101 1011", "0100 1010 1100",
                               0x05AA,           0x05B5,           0x029A,           0x095B,           0x04AC,                  
        //* 1473 -1477 */ "0101 1001 0101", "0110 1100 1010", "0110 1110 0100", "1010 1110 1010", "0100 1111 0101",
                               0x0595,           0x06CA,           0x06E4,           0x0AEA,           0x04F5,                  
        //* 1478 -1480 */ "0010 1011 0110", "1001 0101 0110", "1010 1010 1010"   
                               0x02B6,           0x0956,           0x0AAA                  
    };
    
    private static final int UMALQURA_YEAR_START = 1318;
    private static final int UMALQURA_YEAR_END = 1480;
    
  
    /**
     * @stable ICU 2.8
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    //-------------------------------------------------------------------------
    // Assorted calculation utilities
    //

// Unused code - Alan 2003-05
//    /**
//     * Find the day of the week for a given day
//     *
//     * @param day   The # of days since the start of the Islamic calendar.
//     */
//    // private and uncalled, perhaps not used yet?
//    private static final int absoluteDayToDayOfWeek(long day)
//    {
//        // Calculate the day of the week.
//        // This relies on the fact that the epoch was a Thursday.
//        int dayOfWeek = (int)(day + THURSDAY) % 7 + SUNDAY;
//        if (dayOfWeek < 0) {
//            dayOfWeek += 7;
//        }
//        return dayOfWeek;
//    }

    /**
     * Determine whether a year is a leap year in the Islamic civil calendar
     */
    private final static boolean civilLeapYear(int year)
    {
        return (14 + 11 * year) % 30 < 11;
    }
    
    /**
     * Return the day # on which the given year starts.  Days are counted
     * from the Hijri epoch, origin 0.
     */
    private long yearStart(int year) {
        long ys = 0;
    	 if (cType == CalculationType.ISLAMIC_CIVIL
    	        || cType == CalculationType.ISLAMIC_TBLA
         		|| (cType == CalculationType.ISLAMIC_UMALQURA && year < UMALQURA_YEAR_START )) {
             ys = (year-1)*354 + (long)Math.floor((3+11*year)/30.0);
        } else if(cType == CalculationType.ISLAMIC) {
             ys = trueMonthStart(12*(year-1));
        } else if(cType == CalculationType.ISLAMIC_UMALQURA){
             ys = yearStart(UMALQURA_YEAR_START -1);  
             ys += handleGetYearLength(UMALQURA_YEAR_START -1);
        	for(int i=UMALQURA_YEAR_START; i< year; i++) {  
                ys+= handleGetYearLength(i);
            }
        }    	
        return ys;
    }
    
    /**
     * Return the day # on which the given month starts.  Days are counted
     * from the Hijri epoch, origin 0.
     *
     * @param year  The hijri year
     * @param month  The hijri month, 0-based
     */
    private long monthStart(int year, int month) {
        // Normalize year/month in case month is outside the normal bounds, which may occur
        // in the case of an add operation
        int realYear = year + month / 12;
        int realMonth = month % 12;
        long ms = 0;
        if (cType == CalculationType.ISLAMIC_CIVIL
                || cType == CalculationType.ISLAMIC_TBLA
         		|| (cType == CalculationType.ISLAMIC_UMALQURA && year < UMALQURA_YEAR_START )) {
            ms = (long)Math.ceil(29.5*realMonth)
                    + (realYear-1)*354 + (long)Math.floor((3+11*realYear)/30.0);
        } else if(cType == CalculationType.ISLAMIC) {
            ms = trueMonthStart(12*(realYear-1) + realMonth);
        } else if(cType == CalculationType.ISLAMIC_UMALQURA) {
            ms = yearStart(year);
        	for(int i=0; i< month; i++) {
                ms+= handleGetMonthLength(year, i);
            }
        }
        
        return ms;
    }
    
    /**
     * Find the day number on which a particular month of the true/lunar
     * Islamic calendar starts.
     *
     * @param month The month in question, origin 0 from the Hijri epoch
     *
     * @return The day number on which the given month starts.
     */
    private static final long trueMonthStart(long month)
    {
        long start = cache.get(month);

        if (start == CalendarCache.EMPTY)
        {
            // Make a guess at when the month started, using the average length
            long origin = HIJRA_MILLIS 
                        + (long)Math.floor(month * CalendarAstronomer.SYNODIC_MONTH) * ONE_DAY;

            double age = moonAge(origin);

            if (moonAge(origin) >= 0) {
                // The month has already started
                do {
                    origin -= ONE_DAY;
                    age = moonAge(origin);
                } while (age >= 0);
            }
            else {
                // Preceding month has not ended yet.
                do {
                    origin += ONE_DAY;
                    age = moonAge(origin);
                } while (age < 0);
            }

            start = (origin - HIJRA_MILLIS) / ONE_DAY + 1;
            
            cache.put(month, start);
        }
        return start;
    }

    /**
     * Return the "age" of the moon at the given time; this is the difference
     * in ecliptic latitude between the moon and the sun.  This method simply
     * calls CalendarAstronomer.moonAge, converts to degrees, 
     * and adjusts the resultto be in the range [-180, 180].
     *
     * @param time  The time at which the moon's age is desired,
     *              in millis since 1/1/1970.
     */
    static final double moonAge(long time)
    {
        double age = 0;
        
        synchronized(astro) {
            astro.setTime(time);
            age = astro.getMoonAge();
        }
        // Convert to degrees and normalize...
        age = age * 180 / Math.PI;
        if (age > 180) {
            age = age - 360;
        }

        return age;
    }

    //-------------------------------------------------------------------------
    // Internal data....
    //
    
    // And an Astronomer object for the moon age calculations
    private static CalendarAstronomer astro = new CalendarAstronomer();
    
    private static CalendarCache cache = new CalendarCache();
    
    /**
     * <code>true</code> if this object uses the fixed-cycle Islamic civil calendar,
     * and <code>false</code> if it approximates the true religious calendar using
     * astronomical calculations for the time of the new moon.
     *
     * @serial
     */
    private boolean civil = true;
    
    /**
     * determines the type of calculation to use for this instance
     * 
     * @serial
     * @draft ICU 52
     */
    private CalculationType cType = CalculationType.ISLAMIC_CIVIL;

    //----------------------------------------------------------------------
    // Calendar framework
    //----------------------------------------------------------------------

    /**
     * Return the length (in days) of the given month.
     *
     * @param extendedYear  The hijri year
     * @param month The hijri month, 0-based
     * @stable ICU 2.8
     */
    protected int handleGetMonthLength(int extendedYear, int month) {

        int length = 0;
        
        if (cType == CalculationType.ISLAMIC_CIVIL
                || cType == CalculationType.ISLAMIC_TBLA
        		|| (cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START  || extendedYear > UMALQURA_YEAR_END) )) {
            length = 29 + (month+1) % 2;
            if (month == DHU_AL_HIJJAH && civilLeapYear(extendedYear)) {
                length++;
            } 
        } else if (cType == CalculationType.ISLAMIC) {
            month = 12*(extendedYear-1) + month;
            length = (int)( trueMonthStart(month+1) - trueMonthStart(month) );
        }else if (cType == CalculationType.ISLAMIC_UMALQURA){            
            int idx = (extendedYear - UMALQURA_YEAR_START);     // calculate year offset into bit map array
            int mask = (0x01 << (11 - month));                  // set mask for bit corresponding to month            
            if((UMALQURA_MONTHLENGTH[idx] & mask) == 0 )    
                length = 29;
            else
                length = 30;
        }
        return length;
    }

    /**
     * Return the number of days in the given Islamic year
     * @stable ICU 2.8
     */
    protected int handleGetYearLength(int extendedYear) {
        int length =0; 
        if (cType == CalculationType.ISLAMIC_CIVIL
                || cType == CalculationType.ISLAMIC_TBLA
        		|| (cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START  || extendedYear > UMALQURA_YEAR_END) )) {
            length =  354 + (civilLeapYear(extendedYear) ? 1 : 0);
        } else if (cType == CalculationType.ISLAMIC) {
            int month = 12*(extendedYear-1);
            length =  (int)(trueMonthStart(month + 12) - trueMonthStart(month));
        } else if (cType == CalculationType.ISLAMIC_UMALQURA) {
            for(int i=0; i<12; i++)
                length += handleGetMonthLength(extendedYear, i);
        }
        
        return length;
    }
    
    //-------------------------------------------------------------------------
    // Functions for converting from field values to milliseconds....
    //-------------------------------------------------------------------------

    // Return JD of start of given month/year
    /**
     * @stable ICU 2.8
     */
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        return (int) monthStart(eyear, month) + 1948439;
    }    

    //-------------------------------------------------------------------------
    // Functions for converting from milliseconds to field values
    //-------------------------------------------------------------------------

    /**
     * @stable ICU 2.8
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else {
            year = internalGet(YEAR, 1); // Default to year 1
        }
        return year;
    }

    /**
     * Override Calendar to compute several fields specific to the Islamic
     * calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     * 
     * The DAY_OF_WEEK and DOW_LOCAL fields are already set when this
     * method is called. The getGregorianXxx() methods return Gregorian
     * calendar equivalents for the given Julian day.
     * @stable ICU 2.8
     */
    protected void handleComputeFields(int julianDay) {
        int year =0, month=0, dayOfMonth=0, dayOfYear=0;
        long monthStart;
        long days = julianDay - CIVIL_EPOC;

        if (cType == CalculationType.ISLAMIC_CIVIL || cType == CalculationType.ISLAMIC_TBLA) {
            if (cType == CalculationType.ISLAMIC_TBLA) {
                days = julianDay - ASTRONOMICAL_EPOC;
            }
            // Use the civil calendar approximation, which is just arithmetic
            year  = (int)Math.floor( (30 * days + 10646) / 10631.0 );
            month = (int)Math.ceil((days - 29 - yearStart(year)) / 29.5 );
            month = Math.min(month, 11);
        } else if (cType == CalculationType.ISLAMIC){
            // Guess at the number of elapsed full months since the epoch
            int months = (int)Math.floor(days / CalendarAstronomer.SYNODIC_MONTH);

            monthStart = (long)Math.floor(months * CalendarAstronomer.SYNODIC_MONTH - 1);

            if ( days - monthStart >= 25 && moonAge(internalGetTimeInMillis()) > 0) {
                // If we're near the end of the month, assume next month and search backwards
                months++;
            }

            // Find out the last time that the new moon was actually visible at this longitude
            // This returns midnight the night that the moon was visible at sunset.
            while ((monthStart = trueMonthStart(months)) > days) {
                // If it was after the date in question, back up a month and try again
                months--;
            }

            year = months / 12 + 1;
            month = months % 12;
        } else if (cType == CalculationType.ISLAMIC_UMALQURA) {
            long umalquraStartdays = yearStart(UMALQURA_YEAR_START);
        	if( days < umalquraStartdays) {
                // Use Civil calculation
                year  = (int)Math.floor( (30 * days + 10646) / 10631.0 );
                month = (int)Math.ceil((days - 29 - yearStart(year)) / 29.5 );
                month = Math.min(month, 11);
        	} else {
                int y =UMALQURA_YEAR_START-1, m =0;
                long d = 1;
        		while(d > 0) { 
                    y++; 
                    d = days - yearStart(y) +1;
        			if(d == handleGetYearLength(y)) {
                        m=11;
                        break;
        			} else if(d < handleGetYearLength(y) ) {
                        int monthLen = handleGetMonthLength(y, m); 
                        m=0;
    					while(d > monthLen) {
                            d -= monthLen;
                            m++;
                            monthLen = handleGetMonthLength(y, m);
                        }
                        break;
                    }
                }
                year = y;
                month = m;
            }
        }

       
        dayOfMonth = (int)(days - monthStart(year, month)) + 1;

        // Now figure out the day of the year.
        dayOfYear = (int)(days - monthStart(year, 0) + 1);
        

        internalSet(ERA, 0);
        internalSet(YEAR, year);
        internalSet(EXTENDED_YEAR, year);
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);       
    }    
    
    /**
     *  enumeration of available calendar calculation types
     *  
     * @draft ICU 52
     */
    public enum CalculationType {ISLAMIC, ISLAMIC_CIVIL, ISLAMIC_UMALQURA, ISLAMIC_TBLA};
    
    /**
     * sets the calculation type for this calendar.
     * 
     * @draft ICU 52
     */
    public void setType(CalculationType type) {
        cType = type;
        
        // ensure civil property is up-to-date
        if(cType == CalculationType.ISLAMIC_CIVIL) 
            civil = true;
        else
            civil = false;
    }

    /**
     * set type based on locale
     */
    private void setCalcTypeForLocale(ULocale locale) {
        String localeCalType = CalendarUtil.getCalendarType(locale);
        if("islamic-civil".equals(localeCalType)) 
            setType(CalculationType.ISLAMIC_CIVIL);
        else if("islamic-umalqura".equals(localeCalType)) 
            setType(CalculationType.ISLAMIC_UMALQURA);
        else if("islamic-tbla".equals(localeCalType)) 
            setType(CalculationType.ISLAMIC_TBLA);
        else
            setType(CalculationType.ISLAMIC);       // needs to be last so it's always the default
    }

    
    /**
     * {@inheritDoc}
     * @stable ICU 3.8
     */
    public String getType() {
        if(cType == CalculationType.ISLAMIC_CIVIL) {
            return "islamic-civil";
        } else if (cType == CalculationType.ISLAMIC_TBLA) {
            return "islamic-tbla";
        } else if (cType == CalculationType.ISLAMIC) {
            return "islamic";
        } else {
            return "islamic-umalqura";
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException 
    {
            in.defaultReadObject();
            
            if(cType != CalculationType.ISLAMIC_CIVIL)
                return;     // we've serialized something new, nothing else to do
            
            // new calculation type is civil (default) but civil is false. This will only happen
            // when deserializing a non-civil calender so need to adjust new CalculationType to match 
            // serialized form
            if(!civil)
                cType = CalculationType.ISLAMIC;  
    }
    
    /*
    private static CalendarFactory factory;
    public static CalendarFactory factory() {
        if (factory == null) {
            factory = new CalendarFactory() {
                public Calendar create(TimeZone tz, ULocale loc) {
                    return new IslamicCalendar(tz, loc);
                }

                public String factoryName() {
                    return "Islamic";
                }
            };
        }
        return factory;
    }
    */
}
