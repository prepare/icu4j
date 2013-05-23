/*
 *******************************************************************************
 * Copyright (C) 1996-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;
import java.util.Date;
import java.util.Locale;

import com.ibm.icu.impl.CalendarAstronomer;
import com.ibm.icu.impl.CalendarCache;
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
 * There are two main variants of the Islamic calendar in existence.  The first
 * is the <em>civil</em> calendar, which uses a fixed cycle of alternating 29-
 * and 30-day months, with a leap day added to the last month of 11 out of
 * every 30 years.  This calendar is easily calculated and thus predictable in
 * advance, so it is used as the civil calendar in a number of Arab countries.
 * This is the default behavior of a newly-created <code>IslamicCalendar</code>
 * object.
 * <p>
 * The Islamic <em>religious</em> calendar, however, is based on the <em>observation</em>
 * of the crescent moon.  It is thus affected by the position at which the
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
 * including the one used here, are only approximations of the true astronical
 * calculations.  At present, the approximations used in this class are fairly
 * simplistic; they will be improved in later versions of the code.
 * <p>
 * The {@link #setCivil setCivil} method determines
 * which approach is used to determine the start of a month.  By default, the
 * fixed-cycle civil calendar is used.  However, if <code>setCivil(false)</code>
 * is called, an approximation of the true lunar calendar will be used.
 * <p>
 * This class should not be subclassed.</p>
 * <p>
 * IslamicCalendar usually should be instantiated using 
 * {@link com.ibm.icu.util.Calendar#getInstance(ULocale)} passing in a <code>ULocale</code>
 * with the tag <code>"@calendar=islamic"</code> or <code>"@calendar=islamic-civil"</code>.</p>
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
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
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
    @Deprecated
    public void setCivil(boolean beCivil)
    {
        if (beCivil && cType != CalculationType.ISLAMIC_CIVIL) {
            // The fields of the calendar will become invalid, because the calendar
            // rules are different
            long m = getTimeInMillis();
            cType = CalculationType.ISLAMIC_CIVIL;
            clear();
            setTimeInMillis(m);
        }else if(!beCivil && cType != CalculationType.ISLAMIC){
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
     */
    @Deprecated
    public boolean isCivil() {
    	if(cType == CalculationType.ISLAMIC_CIVIL){
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
    
    private static final String[] UMALQURA_MONTHLENGTH ={
    	    	
    	/* 1318 -1322 */ "010101110100", "100101110110", "010010110111", "001001010111", "010100101011", 
    	/* 1323 -1327 */ "011010010101", "011011001010", "101011010101", "010101011011", "001001011101", 
    	/* 1328 -1332 */ "100100101101", "110010010101", "110101001010", "111010100101", "011011010010", 
    	/* 1333 -1337 */ "101011010101", "010101011010", "101010101011", "010001001011", "011010100101", 
    	/* 1338 -1342 */ "011101010010", "101110101001", "001101110100", "101010110110", "010101010110", 
    	/* 1343 -1347 */ "101010101010", "110101010010", "110110101001", "010111010100", "101011101010", 
    	/* 1348 -1352 */ "010011011101", "001001101110", "100100101110", "101010100110", "110101010100", 
    	/* 1353 -1357 */ "010110101010", "010110110101", "001010110100", "100100110111", "010010011011", 
    	/* 1358 -1362 */ "101001001011", "101100100101", "101101010100", "101101101010", "010101101101", 
    	/* 1363 -1367 */ "010010101101", "101001010101", "110100100101", "111010010010", "111011001001", 
    	/* 1368 -1372 */ "011011010100", "101011101010", "010101101011", "010010101011", "011010000101", 
    	/* 1373 -1377 */ "101101001001", "101110100100", "101110110010", "010110110101", "001010111010", 
    	/* 1378 -1382 */ "100101011011", "010010101011", "010101010101", "011010110010", "011011011001", 
    	/* 1383 -1387 */ "001011101100", "100101101110", "010010101110", "101001010110", "110100101010", 
    	/* 1388 -1392 */ "110101010101", "010110101010", "101010110101", "010010111011", "000001011011", 
    	/* 1393 -1397 */ "100100101011", "101010010101", "001101001010", "101110100101", "010110101010", 
    	/* 1398 -1402 */ "101010110101", "010101010110", "101010010110", "110101001010", "111010100101", 
    	/* 1403 -1407 */ "011101010010", "011011101001", "001101101010", "101010101101", "010101010101", 
    	/* 1408 -1412 */ "101010100101", "101101010010", "101110101001", "010110110100", "100110111010", 
    	/* 1413 -1417 */ "010011011011", "001001011101", "010100101101", "101010100101", "101011010100", 
    	/* 1418 -1422 */ "101011101010", "010101101101", "010010111101", "001000111101", "100100011101", 
    	/* 1423 -1427 */ "101010010101", "101101001010", "101101011010", "010101101101", "001010110110", 
    	/* 1428 -1432 */ "100100111011", "010010011011", "011001010101", "011010101001", "011101010100", 
    	/* 1433 -1437 */ "101101101010", "010101101100", "101010101101", "010101010101", "101100101001", 
    	/* 1438 -1442 */ "101110010010", "101110101001", "010111010100", "101011011010", "010101011010", 
    	/* 1443 -1447 */ "101010101011", "010110010101", "011101001001", "011101100100", "101110101010", 
    	/* 1448 -1452 */ "010110110101", "001010110110", "101001010110", "111001001101","101100100101",
    	/* 1453 -1457 */ "101101010010","101101101010","010110101101","001010101110","100100101111",
    	/* 1458 -1462 */ "010010010111","011001001011","011010100101","011010101100","101011010110",
    	/* 1463 -1467 */ "010101011101","010010011101","101001001101","110100010110","110110010101",
    	/* 1468 -1472 */ "010110101010","010110110101","001010011010","100101011011","010010101100",
    	/* 1473 -1477 */ "010110010101","011011001010","011011100100","101011101010","010011110101",
    	/* 1478 -1480 */ "001010110110","100101010110","101010101010"	
    };
    
    private static final int UMALQURA_YEAR_START = 1318;
    private static final int UMALQURA_YEAR_END = 1480;
    
    private static final GregorianCalendar[] UMALQURA_GREGORIAN_REF = { 
            new GregorianCalendar(1900,3,30,0,0,0),
            new GregorianCalendar(1905,2,7,0,0,0),
            new GregorianCalendar(1910,0,12,0,0,0),
            new GregorianCalendar(1914,10,18,0,0,0),
            new GregorianCalendar(1919,8,25,0,0,0),
            new GregorianCalendar(1924,7,1,0,0,0),
            new GregorianCalendar(1929,5,8,0,0,0),
            new GregorianCalendar(1934,3,14,0,0,0),
            new GregorianCalendar(1939,1,20,0,0,0),
            new GregorianCalendar(1943,11,28,0,0,0),
            new GregorianCalendar(1948,10,2,0,0,0),
            new GregorianCalendar(1953,8,9,0,0,0),
            new GregorianCalendar(1958,6,17,0,0,0),
            new GregorianCalendar(1963,4,24,0,0,0),
            new GregorianCalendar(1968,2,29,0,0,0),
            new GregorianCalendar(1973,1,4,0,0,0),
            new GregorianCalendar(1977,11,11,0,0,0),
            new GregorianCalendar(1982,9,18,0,0,0),
            new GregorianCalendar(1987,7,25,0,0,0),
            new GregorianCalendar(1992,6,1,0,0,0),
            new GregorianCalendar(1997,4,7,0,0,0),
            new GregorianCalendar(2002,2,15,0,0,0),
            new GregorianCalendar(2007,0,20,0,0,0),
            new GregorianCalendar(2011,10,26,0,0,0),
            new GregorianCalendar(2016,9,2,0,0,0),
            new GregorianCalendar(2021,7,9,0,0,0),
            new GregorianCalendar(2026,5,16,0,0,0),
            new GregorianCalendar(2031,3,23,0,0,0),
            new GregorianCalendar(2036,1,29,0,0,0),
            new GregorianCalendar(2041,0,4,0,0,0),
            new GregorianCalendar(2045,10,11,0,0,0),
            new GregorianCalendar(2050,8,18,0,0,0),
            new GregorianCalendar(2055,6,26,0,0,0)
    };
    
    private static final GregorianCalendar UMALQURA_GREGORIAN_REF_LASTDATE = new GregorianCalendar(2058,5,21,0,0,0);

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
         		|| (cType == CalculationType.ISLAMIC_UMALQURA && year < UMALQURA_YEAR_START )) {
            ys = (year-1)*354 + (long)Math.floor((3+11*year)/30.0);
        }else if(cType == CalculationType.ISLAMIC){
            ys = trueMonthStart(12*(year-1));
        }else if(cType == CalculationType.ISLAMIC_UMALQURA){
        	ys = yearStart(UMALQURA_YEAR_START -1);  
        	for(int i=UMALQURA_YEAR_START-1; i< year; i++){  
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
         		|| (cType == CalculationType.ISLAMIC_UMALQURA && year < UMALQURA_YEAR_START )) {
            ms = (long)Math.ceil(29.5*realMonth)
                    + (realYear-1)*354 + (long)Math.floor((3+11*realYear)/30.0);
        }else if(cType == CalculationType.ISLAMIC){
            ms = trueMonthStart(12*(realYear-1) + realMonth);
        }else if(cType == CalculationType.ISLAMIC_UMALQURA){
        	ms = yearStart(year);
        	for(int i=0; i< month; i++){
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
    //private boolean civil = true;
    
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
        		|| (cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START  || extendedYear > UMALQURA_YEAR_END) )) {
            length = 29 + (month+1) % 2;
            if (month == DHU_AL_HIJJAH && civilLeapYear(extendedYear)) {
                length++;
            }
        } else if (cType == CalculationType.ISLAMIC){
            month = 12*(extendedYear-1) + month;
            length = (int)( trueMonthStart(month+1) - trueMonthStart(month) );
        }else if (cType == CalculationType.ISLAMIC_UMALQURA){
        	if(UMALQURA_MONTHLENGTH[extendedYear - UMALQURA_YEAR_START].charAt(month) == '0')
        		return 29;
        	else
        		return 30;
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
        		|| (cType == CalculationType.ISLAMIC_UMALQURA && (extendedYear < UMALQURA_YEAR_START  || extendedYear > UMALQURA_YEAR_END) )) {
            length =  354 + (civilLeapYear(extendedYear) ? 1 : 0);
        } else if (cType == CalculationType.ISLAMIC){
            int month = 12*(extendedYear-1);
            length =  (int)(trueMonthStart(month + 12) - trueMonthStart(month));
        }else if (cType == CalculationType.ISLAMIC_UMALQURA){
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
        long days = julianDay - 1948440;
        
        boolean umalquraRange = false;

        if (cType == CalculationType.ISLAMIC_CIVIL) {
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
        } else if (cType == CalculationType.ISLAMIC_UMALQURA){
        	computeGregorianFields(julianDay);
        	year=getGregorianYear();
            month =getGregorianMonth();
            dayOfMonth =getGregorianDayOfMonth();
            int [] fields = convertGregorianToUmalqura(year, month, dayOfMonth, days);
            year = fields[0];
            month = fields[1];
            dayOfMonth = fields[2];
            dayOfYear = fields[3];  
            if (fields[4]==1){ 
            	umalquraRange = true;
            }
        }

        if(cType == CalculationType.ISLAMIC_CIVIL || cType == CalculationType.ISLAMIC ||
        		(cType == CalculationType.ISLAMIC_UMALQURA && !umalquraRange) ){
	        dayOfMonth = (int)(days - monthStart(year, month)) + 1;
	
	        // Now figure out the day of the year.
	        dayOfYear = (int)(days - monthStart(year, 0) + 1);
        }

        internalSet(ERA, 0);
        internalSet(YEAR, year);
        internalSet(EXTENDED_YEAR, year);
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);       
    }    

    
    private int[] convertGregorianToUmalqura(int year,int month,int day,long days) {
                 
        GregorianCalendar inputGregorianCal = new GregorianCalendar(year,month,day,0,0,0);
        TimeZone inputTimeZone = inputGregorianCal.getTimeZone();
        int grLen = UMALQURA_GREGORIAN_REF.length;
        GregorianCalendar startUmAlqura  = UMALQURA_GREGORIAN_REF[0];
        GregorianCalendar endUmAlqura  = UMALQURA_GREGORIAN_REF_LASTDATE;
        
        startUmAlqura.setTimeZone(inputTimeZone);
        endUmAlqura.setTimeZone(inputTimeZone); 
        
        for (int i=0; i<grLen; i++){
        	UMALQURA_GREGORIAN_REF[i].setTimeZone(inputTimeZone);
        }
        
        Date[] reference_umalqura = new Date[grLen];
        
        for (int i=0; i<grLen; i++){
            reference_umalqura[i] = UMALQURA_GREGORIAN_REF[i].getTime();
        }
          
        Date bdate = startUmAlqura.getTime();
        Date adate = endUmAlqura.getTime();
        Date gDate = inputGregorianCal.getTime();
        
        int umalquraRange =0;
                 
        int referenceIndex = 0, dayOfYear = 0; 
        double dif = 0;
        int[] output= new int [5];
        if (gDate.before(bdate)|| gDate.after(adate)){
        	// Use the civil calendar approximation, which is just arithmetic
            year  = (int)Math.floor( (30 * days + 10646) / 10631.0 );
            month = (int)Math.ceil((days - 29 - yearStart(year)) / 29.5 );
            month = Math.min(month, 11);
        }else{  
        	umalquraRange =1;
            for (referenceIndex = 0; referenceIndex < reference_umalqura.length; referenceIndex++) { 
            	if(gDate.equals(reference_umalqura[referenceIndex])){
            		year=  referenceIndex * 5;
            		month= 0;
                    day= 1;
                    dayOfYear= 1;
                    break;
            	}else if( (referenceIndex == reference_umalqura.length - 1) ||
            			   (gDate.after(reference_umalqura[referenceIndex]) && gDate.before(reference_umalqura[referenceIndex + 1])) ){
	                
            		dif = getDateDifference(gDate,reference_umalqura[referenceIndex]);
	                if(dif > 0){
	                	boolean done = false;
	                	int pos = referenceIndex * 5, monthLength = 0;
		                for(int i=pos; (i < pos + 5) && i + UMALQURA_YEAR_START <=UMALQURA_YEAR_END; i++){
		                	dayOfYear = 1 + (int) dif;
		                	for (int j = 0; j <= 11; j++){
		                		monthLength = handleGetMonthLength(UMALQURA_YEAR_START + i, j);
		                		if ( dif > monthLength){ 
		                			dif-= monthLength;
		                		}else{
		                			year = i;
		                			month = j;
		                			day = (int) dif;
		                			if(day == monthLength){
		                				day = 1;
		                				if(month == 11){
		                					month = 0;
		                					year++;
		                				}else
		                					month++;
		                			}else{
		                				day++;
		                			}
		                			
		                			done = true;
		                			break;
		                		}	
		                	}
		                	if(done){
		                		break;
		                	}
		                }
	                }	                
	                break;	                
	            } 
            }
        }
        
        if(umalquraRange == 1){
	        if(dayOfYear == 355){ 
	        	dayOfYear = 1;      	   
	        }
	        output[0]= UMALQURA_YEAR_START + year;
	        
        }else{
        	output[0]=  year;
        }
        
        output[1]= month;
        output[2]= day;
        output[3]= dayOfYear;
        output[4]= umalquraRange;
        
        return output ;
    }
    
    private static long getDateDifference(Date InputDate, Date ReferenceDate) {        
        long x1 = InputDate.getTime()/86400000;
        long y1 = ReferenceDate.getTime()/86400000;
        long dif = x1 - y1;
        return dif;
    }
    
    
    public enum CalculationType {ISLAMIC, ISLAMIC_CIVIL, ISLAMIC_UMALQURA};
    
    public void setType(CalculationType type){
    	cType = type;   	
    }

    /**
     * {@inheritDoc}
     * @stable ICU 3.8
     */
    public String getType() {
        if(cType == CalculationType.ISLAMIC_CIVIL) {
            return "islamic-civil";
        } else if (cType == CalculationType.ISLAMIC){
            return "islamic";
        }else {
        	return "islamic-um-alqura";
        }
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
