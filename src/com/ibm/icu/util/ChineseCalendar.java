/*********************************************************************
 * Copyright (C) 2000, International Business Machines Corporation and
 * others. All Rights Reserved.
 *********************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/util/ChineseCalendar.java,v $
 * $Date: 2000/11/18 01:07:18 $
 * $Revision: 1.3 $
 */
package com.ibm.util;
import java.util.Date;
import java.util.Locale;

/**
 * <code>ChineseCalendar</code> is a concrete subclass of {@link Calendar}
 * that implements a traditional Chinese calendar.  The traditional Chinese
 * calendar is a lunisolar calendar: Each month starts on a new moon, and
 * the months are numbered according to solar events, specifically, to
 * guarantee that month 11 always contains the winter solstice.  In order
 * to accomplish this, leap months are inserted in certain years.  Leap
 * months are numbered the same as the month they follow.  The decision of
 * which month is a leap month depends on the relative movements of the sun
 * and moon.
 *
 * <p>This class defines one addition field beyond those defined by
 * <code>Calendar</code>: The <code>IS_LEAP_MONTH</code> field takes the
 * value of 0 for normal months, or 1 for leap months.
 *
 * <p>All astronomical computations are performed with respect to a time
 * zone of GMT+8:00 and a longitude of 120 degrees east.  Although some
 * calendars implement a historically more accurate convention of using
 * Beijing's local longitude (116 degrees 25 minutes east) and time zone
 * (GMT+7:45:40) for dates before 1929, we do not implement this here.
 *
 * <p>Years are counted in two different ways in the Chinese calendar.  The
 * first method is by sequential numbering from the 61st year of the reign
 * of Huang Di, 2637 BCE, which is designated year 1 on the Chinese
 * calendar.  The second method uses 60-year cycles from the same starting
 * point, which is designated year 1 of cycle 1.  In this class, the
 * <code>EXTENDED_YEAR</code> field contains the sequential year count.
 * The <code>ERA</code> field contains the cycle number, and the
 * <code>YEAR</code> field contains the year of the cycle, a value between
 * 1 and 60.
 *
 * <p>There is some variation in what is considered the starting point of
 * the calendar, with some sources starting in the first year of the reign
 * of Huang Di, rather than the 61st.  This gives continuous year numbers
 * 60 years greater and cycle numbers one greater than what this class
 * implements.
 *
 * <p>References:<ul>
 * 
 * <li>Dershowitz and Reingold, <i>Calendrical Calculations</i>,
 * Cambridge University Press, 1997</li>
 * 
 * <li>Helmer Aslaksen's
 * <a href="http://www.math.nus.edu.sg/aslaksen/calendar/chinese.shtml">
 * Chinese Calendar page</a></li>
 *
 * <li>The <a href="http://www.tondering.dk/claus/calendar.html">
 * Calendar FAQ</a></li>
 *
 * </ul>
 * @see com.ibm.text.ChineseDateFormat
 * @author Alan Liu
 */
public class ChineseCalendar extends Calendar {

    //------------------------------------------------------------------
    // Developer Notes
    // 
    // Time is represented as a scalar in two ways in this class.  One is
    // the usual UTC epoch millis, that is, milliseconds after January 1,
    // 1970 Gregorian, 0:00:00.000 UTC.  The other is in terms of 'local
    // days.'  This is the number of days after January 1, 1970 Gregorian,
    // local to Beijing, China (since all computations of the Chinese
    // calendar are done in Beijing).  That is, 0 represents January 1,
    // 1970 0:00 Asia/Shanghai.  Conversion of local days to and from
    // standard epoch milliseconds is accomplished by the daysToMillis()
    // and millisToDays() methods.
    // 
    // Several methods use caches to improve performance.  Caches are at
    // the object, not class level, under the assumption that typical
    // usage will be to have one instance of ChineseCalendar at a time.
 
    /**
     * We have one instance per object, and we don't synchronize it because
     * Calendar doesn't support multithreaded execution in the first place.
     */
    private transient CalendarAstronomer astro = new CalendarAstronomer();

    /**
     * Cache that maps Gregorian year to local days of winter solstice.
     * @see winterSolstice
     */
    private transient CalendarCache winterSolsticeCache = new CalendarCache();

    /**
     * Cache that maps Gregorian year to local days of Chinese new year.
     * @see newYear
     */
    private transient CalendarCache newYearCache = new CalendarCache();

    //------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------

    /**
     * Construct a Chinese calendar with the default time zone and locale.
     */
    public ChineseCalendar() {
        super();
    }

    /**
     * Construct a Chinese calendar with the given time zone and locale.
     * @param zone time zone for this calendar
     * @param locale locale for this calendar
     */
    public ChineseCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
    }

    //------------------------------------------------------------------
    // Public constants
    //------------------------------------------------------------------

    /**
     * Field indicating whether or not the current month is a leap month.
     * Should have a value of 0 for non-leap months, and 1 for leap months.
     */
    public static int IS_LEAP_MONTH = BASE_FIELD_COUNT;

    /**
     * Count of fields in this class.
     */
    private static final int FIELD_COUNT = IS_LEAP_MONTH + 1;

    //------------------------------------------------------------------
    // Calendar framework
    //------------------------------------------------------------------

    /**
     * Override Calendar to allocate our additional field.
     */
    protected int[] handleCreateFields() {
        return new int[FIELD_COUNT];
    }

    /**
     * Array defining the limits of field values for this class.  Field
     * limits which are invariant with respect to calendar system and
     * defined by Calendar are left blank.
     *
     * Notes:
     *
     * ERA 5000000 / 60 = 83333.
     *
     * MONTH There are 12 or 13 lunar months in a year.  However, we always
     * number them 0..11, with an intercalated, identically numbered leap
     * month, when necessary.
     *
     * DAY_OF_YEAR In a non-leap year there are 353, 354, or 355 days.  In
     * a leap year there are 383, 384, or 385 days.
     *
     * WEEK_OF_YEAR The least maximum occurs if there are 353 days in the
     * year, and the first 6 are the last week of the previous year.  Then
     * we have 49 full weeks and 4 days in the last week: 6 + 49*7 + 4 =
     * 353.  So the least maximum is 50.  The maximum occurs if there are
     * 385 days in the year, and WOY 1 extends 6 days into the prior year.
     * Then there are 54 full weeks, and 6 days in the last week: 1 + 54*7
     * + 6 = 385.  The 6 days of the last week will fall into WOY 1 of the
     * next year.  Maximum is 55.
     *
     * WEEK_OF_MONTH In a 29 day month, if the first 7 days make up week 1
     * that leaves 3 full weeks and 1 day at the end.  The least maximum is
     * thus 5.  In a 30 days month, if the previous 6 days belong WOM 1 of
     * this month, we have 4 full weeks and 1 days at the end (which
     * technically will be WOM 1 of the next month, but will be reported by
     * time->fields and hence by getActualMaximum as WOM 6 of this month).
     * Maximum is 6.
     *
     * DAY_OF_WEEK_IN_MONTH In a 29 or 30 day month, there are 4 full weeks
     * plus 1 or 2 days at the end, so the maximum is always 5.
     */
    private static final int LIMITS[][] = {
        // Minimum  Greatest    Least  Maximum
        //           Minimum  Maximum
        {        1,        1,   83333,   83333 }, // ERA
        {        1,        1,      70,      70 }, // YEAR
        {        0,        0,      11,      11 }, // MONTH
        {        1,        1,      50,      55 }, // WEEK_OF_YEAR
        {        1,        1,       5,       6 }, // WEEK_OF_MONTH
        {        1,        1,      29,      30 }, // DAY_OF_MONTH
        {        1,        1,     353,     385 }, // DAY_OF_YEAR
        {/*                                  */}, // DAY_OF_WEEK
        {       -1,       -1,       5,       5 }, // DAY_OF_WEEK_IN_MONTH
        {/*                                  */}, // AM_PM
        {/*                                  */}, // HOUR
        {/*                                  */}, // HOUR_OF_DAY
        {/*                                  */}, // MINUTE
        {/*                                  */}, // SECOND
        {/*                                  */}, // MILLISECOND
        {/*                                  */}, // ZONE_OFFSET
        {/*                                  */}, // DST_OFFSET
        { -5000001, -5000001, 5000001, 5000001 }, // YEAR_WOY
        {/*                                  */}, // DOW_LOCAL
        { -5000000, -5000000, 5000000, 5000000 }, // EXTENDED_YEAR
        {/*                                  */}, // JULIAN_DAY
        {/*                                  */}, // MILLISECONDS_IN_DAY
        {        0,        0,       1,       1 }, // IS_LEAP_MONTH
    };

    /**
     * Override Calendar to return the limit value for the given field.
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    /**
     * Implement abstract Calendar method to return the extended year
     * defined by the current fields.  This will use either the ERA and
     * YEAR field as the cycle and year-of-cycle, or the EXTENDED_YEAR
     * field as the continuous year count, depending on which is newer.
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newestStamp(ERA, YEAR, UNSET) <= getStamp(EXTENDED_YEAR)) {
            year = internalGet(EXTENDED_YEAR, 1); // Default to year 1
        } else {
            int cycle = internalGet(ERA, 1) - 1; // 0-based cycle
            year = cycle * 60 + internalGet(YEAR, 1);
        }
        return year;
    }

    /**
     * Override Calendar method to return the number of days in the given
     * extended year and month.
     *
     * <p>Note: This method also reads the IS_LEAP_MONTH field to determine
     * whether or not the given month is a leap month.
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        int thisStart = handleComputeMonthStart(extendedYear, month) -
            EPOCH_JULIAN_DAY + 1; // Julian day -> local days
        int nextStart = newMoonNear(thisStart + SYNODIC_GAP, true);
        return nextStart - thisStart;
    }

    //------------------------------------------------------------------
    // Support methods and constants
    //------------------------------------------------------------------
   
    /**
     * The start year of the Chinese calendar, the 61st year of the reign
     * of Huang Di.  Some sources use the first year of his reign,
     * resulting in EXTENDED_YEAR values 60 years greater and ERA (cycle)
     * values one greater.
     */
    private static final int CHINESE_EPOCH_YEAR = -2636; // Gregorian year

    /**
     * The offset from GMT in milliseconds at which we perform astronomical
     * computations.  Some sources use a different historically accurate
     * offset of GMT+7:45:40 for years before 1929; we do not do this.
     */
    private static final long CHINA_OFFSET = 8*ONE_HOUR;

    /**
     * Value to be added or subtracted from the local days of a new moon to
     * get close to the next or prior new moon, but not cross it.  Must be
     * >= 1 and < CalendarAstronomer.SYNODIC_MONTH.
     */
    private static final int SYNODIC_GAP = 25;

    /**
     * Convert local days to UTC epoch milliseconds.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai
     * @return milliseconds after January 1, 1970 0:00 GMT
     */
    private static final long daysToMillis(int days) {
        return (days * ONE_DAY) - CHINA_OFFSET;
    }

    /**
     * Convert UTC epoch milliseconds to local days.
     * @param millis milliseconds after January 1, 1970 0:00 GMT
     * @return days after January 1, 1970 0:00 Asia/Shanghai
     */
    private static final int millisToDays(long millis) {
        return (int) floorDivide(millis + CHINA_OFFSET, ONE_DAY);
    }

    //------------------------------------------------------------------
    // Astronomical computations
    //------------------------------------------------------------------
    
    /**
     * Return the major solar term on or after December 15 of the given
     * Gregorian year, that is, the winter solstice of the given year.
     * Computations are relative to Asia/Shanghai time zone.
     * @param gyear a Gregorian year
     * @return days after January 1, 1970 0:00 Asia/Shanghai of the
     * winter solstice of the given year
     */
    private int winterSolstice(int gyear) {

        long cacheValue = winterSolsticeCache.get(gyear);

        if (cacheValue == CalendarCache.EMPTY) {
            // In books December 15 is used, but it fails for some years
            // using our algorithms, e.g.: 1298 1391 1492 1553 1560.  That
            // is, winterSolstice(1298) starts search at Dec 14 08:00:00
            // PST 1298 with a final result of Dec 14 10:31:59 PST 1299.
            long ms = daysToMillis(computeGregorianMonthStart(gyear, Calendar.DECEMBER) +
                                   1 - EPOCH_JULIAN_DAY);
            astro.setTime(ms);
            
            // Winter solstice is 270 degrees solar longitude aka Dongzhi
            long solarLong = astro.getSunTime(CalendarAstronomer.WINTER_SOLSTICE,
                                              true);
            cacheValue = millisToDays(solarLong);
            winterSolsticeCache.put(gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    /**
     * Return the closest new moon to the given date, searching either
     * forward or backward in time.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai
     * @param after if true, search for a new moon on or after the given
     * date; otherwise, search for a new moon before it
     * @return days after January 1, 1970 0:00 Asia/Shanghai of the nearest
     * new moon after or before <code>days</code>
     */
    private int newMoonNear(int days, boolean after) {
        
        astro.setTime(daysToMillis(days));
        long newMoon = astro.getMoonTime(CalendarAstronomer.NEW_MOON, after);
        
        return millisToDays(newMoon);
    }

    /**
     * Return the nearest integer number of synodic months between
     * two dates.
     * @param day1 days after January 1, 1970 0:00 Asia/Shanghai
     * @param day2 days after January 1, 1970 0:00 Asia/Shanghai
     * @return the nearest integer number of months between day1 and day2
     */
    private int synodicMonthsBetween(int day1, int day2) {
        return (int) Math.round((day2 - day1) / CalendarAstronomer.SYNODIC_MONTH);
    }

    /**
     * Return the major solar term on or before a given date.  This
     * will be an integer from 1..12, with 1 corresponding to 330 degrees,
     * 2 to 0 degrees, 3 to 30 degrees,..., and 12 to 300 degrees.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai
     */
    private int majorSolarTerm(int days) {
        
        astro.setTime(daysToMillis(days));

        // Compute (floor(solarLongitude / (pi/6)) + 2) % 12
        int term = ((int) Math.floor(6 * astro.getSunLongitude() / Math.PI) + 2) % 12;
        if (term < 1) {
            term += 12;
        }
        return term;
    }

    /**
     * Return true if the given month lacks a major solar term.
     * @param newMoon days after January 1, 1970 0:00 Asia/Shanghai of a new
     * moon
     */
    private boolean hasNoMajorSolarTerm(int newMoon) {
        
        return majorSolarTerm(newMoon) ==
            majorSolarTerm(newMoonNear(newMoon + SYNODIC_GAP, true));
    }

    //------------------------------------------------------------------
    // Time to fields
    //------------------------------------------------------------------
    
    /**
     * Return true if there is a leap month on or after month newMoon1 and
     * at or before month newMoon2.
     * @param newMoon1 days after January 1, 1970 0:00 Asia/Shanghai of a
     * new moon
     * @param newMoon2 days after January 1, 1970 0:00 Asia/Shanghai of a
     * new moon
     */
    private boolean isLeapMonthBetween(int newMoon1, int newMoon2) {

        // This is only needed to debug the timeOfAngle divergence bug.
        // Remove this later. Liu 11/9/00
        // DEBUG
        if (synodicMonthsBetween(newMoon1, newMoon2) >= 50) {
            throw new IllegalArgumentException("isLeapMonthBetween(" + newMoon1 +
                                               ", " + newMoon2 +
                                               "): Invalid parameters");
        }

        return (newMoon2 >= newMoon1) &&
            (isLeapMonthBetween(newMoon1, newMoonNear(newMoon2 - SYNODIC_GAP, false)) ||
             hasNoMajorSolarTerm(newMoon2));
    }

    /**
     * Override Calendar to compute several fields specific to the Chinese
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
     * method is called.  The getGregorianXxx() methods return Gregorian
     * calendar equivalents for the given Julian day.
     *
     * <p>Compute the ChineseCalendar-specific field IS_LEAP_MONTH.
     */
    protected void handleComputeFields(int julianDay) {

        computeChineseFields(julianDay - EPOCH_JULIAN_DAY, // local days
                             getGregorianYear(), getGregorianMonth(),
                             true); // set all fields
    }

    /**
     * Compute fields for the Chinese calendar system.  This method can
     * either set all relevant fields, as required by
     * <code>handleComputeFields()</code>, or it can just set the MONTH and
     * IS_LEAP_MONTH fields, as required by
     * <code>handleComputeMonthStart()</code>.
     * @param days days after January 1, 1970 0:00 Asia/Shanghai of the
     * date to compute fields for
     * @param gyear the Gregorian year of the given date
     * @param gmonth the Gregorian month of the given date
     * @param setAllFields if true, set the EXTENDED_YEAR, ERA, YEAR,
     * DAY_OF_MONTH, and DAY_OF_YEAR fields.  In either case set the MONTH
     * and IS_LEAP_MONTH fields.
     */
    private void computeChineseFields(int days, int gyear, int gmonth,
                                      boolean setAllFields) {

        // Find the winter solstices before and after the target date.
        // These define the boundaries of this Chinese year, specifically,
        // the position of month 11, which always contains the solstice.
        // We want solsticeBefore <= date < solsticeAfter.
        int solsticeBefore;
        int solsticeAfter = winterSolstice(gyear);
        if (days < solsticeAfter) {
            solsticeBefore = winterSolstice(gyear - 1);
        } else {
            solsticeBefore = solsticeAfter;
            solsticeAfter = winterSolstice(gyear + 1);
        }

        // Find the start of the month after month 11.  This will be either
        // the prior month 12 or leap month 11 (very rare).  Also find the
        // start of the following month 11.
        int firstMoon = newMoonNear(solsticeBefore + 1, true);
        int lastMoon = newMoonNear(solsticeAfter + 1, false);
        int thisMoon = newMoonNear(days + 1, false); // Start of this month
        boolean isLeapYear = synodicMonthsBetween(firstMoon, lastMoon) == 12;

        int month = synodicMonthsBetween(firstMoon, thisMoon);
        if (isLeapYear && isLeapMonthBetween(firstMoon, thisMoon)) {
            month--;
        }
        if (month < 1) {
            month += 12;
        }

        boolean isLeapMonth = isLeapYear &&
            hasNoMajorSolarTerm(thisMoon) &&
            !isLeapMonthBetween(firstMoon, newMoonNear(thisMoon - SYNODIC_GAP, false));

        internalSet(MONTH, month-1); // Convert from 1-based to 0-based
        internalSet(IS_LEAP_MONTH, isLeapMonth?1:0);

        if (setAllFields) {

            int year = gyear - CHINESE_EPOCH_YEAR;
            if (month < 11 ||
                gmonth >= Calendar.JULY) {
                year++;
            }
            int dayOfMonth = days - thisMoon + 1;

            internalSet(EXTENDED_YEAR, year);

            // 0->0,60  1->1,1  60->1,60  61->2,1  etc.
            int[] yearOfCycle = new int[1];
            int cycle = floorDivide(year-1, 60, yearOfCycle);
            internalSet(ERA, cycle+1);
            internalSet(YEAR, yearOfCycle[0]+1);

            internalSet(DAY_OF_MONTH, dayOfMonth);

            // Days will be before the first new year we compute if this
            // date is in month 11, leap 11, 12.  There is never a leap 12.
            // New year computations are cached so this should be cheap in
            // the long run.
            int newYear = newYear(gyear);
            if (days < newYear) {
                newYear = newYear(gyear-1);
            }
            internalSet(DAY_OF_YEAR, days - newYear + 1);
        }
    }

    //------------------------------------------------------------------
    // Fields to time
    //------------------------------------------------------------------
    
    /**
     * Return the Chinese new year of the given Gregorian year.
     * @param gyear a Gregorian year
     * @return days after January 1, 1970 0:00 Asia/Shanghai of the
     * Chinese new year of the given year (this will be a new moon)
     */
    private int newYear(int gyear) {

        long cacheValue = newYearCache.get(gyear);

        if (cacheValue == CalendarCache.EMPTY) {

            int solsticeBefore= winterSolstice(gyear - 1);
            int solsticeAfter = winterSolstice(gyear);
            int newMoon1 = newMoonNear(solsticeBefore + 1, true);
            int newMoon2 = newMoonNear(newMoon1 + SYNODIC_GAP, true);
            int newMoon11 = newMoonNear(solsticeAfter + 1, false);
            
            if (synodicMonthsBetween(newMoon1, newMoon11) == 12 &&
                (hasNoMajorSolarTerm(newMoon1) || hasNoMajorSolarTerm(newMoon2))) {
                cacheValue = newMoonNear(newMoon2 + SYNODIC_GAP, true);
            } else {
                cacheValue = newMoon2;
            }

            newYearCache.put(gyear, cacheValue);
        }
        return (int) cacheValue;
    }

    /**
     * Return the Julian day number of day before the first day of the
     * given month in the given extended year.
     * 
     * <p>Note: This method reads the IS_LEAP_MONTH field to determine
     * whether the given month is a leap month.
     * @param eyear the extended year
     * @param month the zero-based month.  The month is also determined
     * by reading the IS_LEAP_MONTH field.
     * @param return the Julian day number of the day before the first
     * day of the given month and year
     */
    protected int handleComputeMonthStart(int eyear, int month) {

        int gyear = eyear + CHINESE_EPOCH_YEAR - 1; // Gregorian year
        int newYear = newYear(gyear);
        int newMoon = newMoonNear(newYear + month * 29, true);
        
        int julianDay = newMoon + EPOCH_JULIAN_DAY;
        int isLeapMonth = internalGet(IS_LEAP_MONTH);

        computeGregorianFields(julianDay);
        
        // Save fields for later restoration
        int saveMonth = internalGet(MONTH);

        // This will modify the MONTH and IS_LEAP_MONTH fields (only)
        computeChineseFields(newMoon, getGregorianYear(),
                             getGregorianMonth(), false);        

        if (month != internalGet(MONTH) ||
            isLeapMonth != internalGet(IS_LEAP_MONTH)) {
            newMoon = newMoonNear(newMoon + SYNODIC_GAP, true);
            julianDay = newMoon + EPOCH_JULIAN_DAY;
        }

        internalSet(MONTH, saveMonth);
        internalSet(IS_LEAP_MONTH, isLeapMonth);

        return julianDay - 1;
    }
}
