/*
 * $RCSfile: IslamicCalendar.java,v $ $Revision: 1.1 $ $Date: 2000/02/10 06:25:55 $
 *
 * (C) Copyright IBM Corp. 1997-1998.  All Rights Reserved.
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 *
 */
package com.ibm.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.ibm.util.CalendarAstronomer.*;

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
 *
 * @see java.util.GregorianCalendar
 *
 * @author Laura Werner
 * @version 1.0
 */
public class IslamicCalendar extends IBMCalendar {

    private static String copyright = "Copyright \u00a9 1997-1998 IBM Corp. All Rights Reserved.";

    //-------------------------------------------------------------------------
    // Constants...
    //-------------------------------------------------------------------------
    
    /** Constant for Muharram, the 1st month of the Islamic year. */
    public static final int MUHARRAM = 0;

    /** Constant for Safar, the 2nd month of the Islamic year. */
    public static final int SAFAR = 1;

    /** Constant for Rabi' al-awwal (or Rabi' I), the 3rd month of the Islamic year. */
    public static final int RABI_1 = 2;

    /** Constant for Rabi' al-thani or (Rabi' II), the 4th month of the Islamic year. */
    public static final int RABI_2 = 3;

    /** Constant for Jumada al-awwal or (Jumada I), the 5th month of the Islamic year. */
    public static final int JUMADA_1 = 4;

    /** Constant for Jumada al-thani or (Jumada II), the 6th month of the Islamic year. */
    public static final int JUMADA_2 = 5;

    /** Constant for Rajab, the 7th month of the Islamic year. */
    public static final int RAJAB = 6;

    /** Constant for Sha'ban, the 8th month of the Islamic year. */
    public static final int SHABAN = 7;

    /** Constant for Ramadan, the 9th month of the Islamic year. */
    public static final int RAMADAN = 8;

    /** Constant for Shawwal, the 10th month of the Islamic year. */
    public static final int SHAWWAL = 9;

    /** Constant for Dhu al-Qi'dah, the 11th month of the Islamic year. */
    public static final int DHU_AL_QIDAH = 10;

    /** Constant for Dhu al-Hijjah, the 12th month of the Islamic year. */
    public static final int DHU_AL_HIJJAH = 11;


    // Useful millisecond constants
    private static final int  SECOND_MS = 1000;
    private static final int  MINUTE_MS = 60*SECOND_MS;
    private static final int  HOUR_MS   = 60*MINUTE_MS;
    private static final long DAY_MS    = 24*HOUR_MS;
    private static final long WEEK_MS   = 7*DAY_MS;

    private static final long HIJRA_MILLIS = -42521587200000L;    // 7/16/622 AD 00:00

    //-------------------------------------------------------------------------
    // Constructors...
    //-------------------------------------------------------------------------

    /**
     * Constructs a default <code>IslamicCalendar</code> using the current time
     * in the default time zone with the default locale.
     */
    public IslamicCalendar()
    {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the given time zone with the default locale.
     * @param zone the given time zone.
     */
    public IslamicCalendar(TimeZone zone)
    {
        this(zone, Locale.getDefault());
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the default time zone with the given locale.
     *
     * @param aLocale the given locale.
     */
    public IslamicCalendar(Locale aLocale)
    {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     *
     * @param aLocale the given locale.
     */
    public IslamicCalendar(TimeZone zone, Locale aLocale)
    {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     */
    public IslamicCalendar(Date date) {
        super(TimeZone.getDefault(), Locale.getDefault());
        this.setTime(date);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year the value used to set the {@link #YEAR YEAR} time field in the calendar.
     *
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Muharram.
     *
     * @param date the value used to set the {@link #DATE DATE} time field in the calendar.
     */
    public IslamicCalendar(int year, int month, int date)
    {
        super(TimeZone.getDefault(), Locale.getDefault());
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
    }

    /**
     * Constructs an <code>IslamicCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year  the value used to set the {@link #YEAR YEAR} time field in the calendar.
     *
     * @param month the value used to set the {@link #MONTH MONTH} time field in the calendar.
     *              Note that the month value is 0-based. e.g., 0 for Muharram.
     *
     * @param date  the value used to set the {@link #DATE DATE} time field in the calendar.
     *
     * @param hour  the value used to set the {@link #HOUR_OF_DAY HOUR_OF_DAY} time field
     *              in the calendar.
     *
     * @param minute the value used to set the {@link #MINUTE MINUTE} time field
     *              in the calendar.
     *
     * @param second the value used to set the {@link #SECOND SECOND} time field
     *              in the calendar.
     */
    public IslamicCalendar(int year, int month, int date, int hour,
                             int minute, int second)
    {
        super(TimeZone.getDefault(), Locale.getDefault());
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
     */
    public void setCivil(boolean beCivil)
    {
        if (civil != beCivil) {
            // The fields of the calendar will become invalid, because the calendar
            // rules are different
            computeTime();
            areFieldsSet = false;
            civil = beCivil;
        }
    }
    
    /**
     * Returns <code>true</code> if this object is using the fixed-cycle civil
     * calendar, or <code>false</code> if using the religious, astronomical
     * calendar.
     */
    public boolean isCivil() {
        return civil;
    }
    
    //-------------------------------------------------------------------------
    // Minimum / Maximum access functions
    //-------------------------------------------------------------------------

    // The minimum and maximum values for all of the fields, for validation
    private static final int MinMax[][] = {
        // Min         Greatest Min    Least Max            Max
        {   0,              0,              0,              0           },  // ERA
        {   1,              1,        5000000,        5000000           },  // YEAR
        {   0,              0,             11,             11           },  // MONTH
        {   0,              0,             51,             52           },  // WEEK_OF_YEAR
        {   0,              0,              5,              6           },  // WEEK_OF_MONTH
        {   1,              1,             29,             30           },  // DAY_OF_MONTH
        {   1,              1,            354,            355           },  // DAY_OF_YEAR
        {   1,              1,              7,              7           },  // DAY_OF_WEEK
        {  -1,             -1,              4,              5           },  // DAY_OF_WEEK_IN_MONTH
        {   0,              0,              1,              1           },  // AM_PM
        {   0,              0,             11,             11           },  // HOUR
        {   0,              0,             23,             23           },  // HOUR_OF_DAY
        {   0,              0,             59,             59           },  // MINUTE
        {   0,              0,             59,             59           },  // SECOND
        {   0,              0,            999,            999           },  // MILLISECOND
        {  -12*HOUR_MS,   -12*HOUR_MS,     12*HOUR_MS,     12*HOUR_MS   },  // ZONE_OFFSET
        {   0,              0,              1*HOUR_MS,      1*HOUR_MS   },  // DST_OFFSET
    };

    /**
     * Returns minimum value for the given field.
     * For example, for {@link #DAY_OF_MONTH DAY_OF_MONTH} this method returns 1,
     *
     * @param field The field whose minimum value is desired.
     *
     * @see java.util.Calendar#getMinimum
     */
    public int getMinimum(int field)
    {
        return MinMax[field][0];
    }

    /**
     * Returns highest minimum value for the given field.  For the Islamic
     * calendar, this always returns the same result as {@link #getMinimum}.
     *
     * @param field The field whose greatest minimum value is desired.
     *
     * @see #getMinimum
     */
    public int getGreatestMinimum(int field)
    {
        return MinMax[field][1];
    }

    /**
     * Returns maximum value for the given field
     * For the {@link #DAY_OF_MONTH DAY_OF_MONTH} field, this method returns 30.
     *
     * @param field The field whose maximum value is desired.
     *
     * @see #getLeastMaximum
     * @see #getActualMaximum
     */
    public int getMaximum(int field)
    {
        return MinMax[field][3];
    }

    /**
     * Returns lowest maximum value for the given field.  For most fields,
     * this returns the same result as {@link #getMaximum getMaximum}.  However,
     * for some fields this can be a lower number. For example,
     * the maximum {@link #DAY_OF_MONTH DAY_OF_MONTH} in the Islamic caleandar varies
     * from month to month, so this method returns 29 while <code>getMaximum</code>
     * returns 30.
     *
     * @param field The field whose least maximum value is desired.
     *
     * @see #getMaximum
     * @see #getActualMaximum
     */
    public int getLeastMaximum(int field)
    {
        return MinMax[field][2];
    }

    /**
     * Return the maximum value that a field could have, given the current date.
     * For example, for the {@link #DAY_OF_MONTH DAY_OF_MONTH} field the actual maximum varies
     * depending on the length of the month, which in turn varies according
     * to either the civil calendar cycle or the actual time of the next new moon.
     *
     * @param field The field whose maximum value is desired.
     *
     * @see #getMaximum
     * @see #getLeastMaximum
     */
    public int getActualMaximum(int field) {
        if (!isSet[YEAR] || !isSet[MONTH]) {
            complete();
        }
        switch (field) {
          case DAY_OF_MONTH:
            return monthLength(fields[YEAR], fields[MONTH]);

          case DAY_OF_YEAR:
            return yearLength(fields[YEAR]);
          
          default:
            return super.getActualMaximum(field);
        }   
    }
        

    //-------------------------------------------------------------------------
    // Functions for converting from field values to milliseconds....
    //-------------------------------------------------------------------------

    /**
     * Converts time field values to UTC as milliseconds.
     *
     * @exception IllegalArgumentException if a field has an invalid value 
     * and {@link #isLenient isLenient} returns <code>false</code>.
     */
    protected void computeTime()
    {
        if (isTimeSet) return;

        if (!isLenient() && !validateFields())
            throw new IllegalArgumentException();

        if (isSet[ERA] && internalGet(ERA) != 0)
            throw new IllegalArgumentException();

        // We need the time zone offset for some of the calculations below.
        // We use the TimeZone object, unless the user has explicitly set the
        // ZONE_OFFSET field.
        TimeZone zone = getTimeZone();
        int zoneOffset = zone.getRawOffset();

        // The year is required.  We don't have to check if it's unset,
        // because if it is, by definition it will be 0.

        int year = internalGet(YEAR);

        if (year <= 0 && !isLenient())
            throw new IllegalArgumentException();

        long dayNumber = 0, date = 0;
        
        // The following code is somewhat convoluted. The various nested
        //  if's handle the different cases of what fields are present.
        if (isSet[MONTH] &&
            (isSet[DATE] ||
             (isSet[DAY_OF_WEEK] &&
              (isSet[WEEK_OF_MONTH] ||
               isSet[DAY_OF_WEEK_IN_MONTH])
                 )
                ))
        {
            // We have the month specified.  Figure out when that month starts
            int month = internalGet(MONTH);
            dayNumber = monthStart(year, month);

            if (isSet[DATE])
            {
                date = internalGet(DATE);
            }
            else
            {
                // Compute from day of week plus week number or from the day of
                // week plus the day of week in month.  The computations are
                // almost identical.

                // Find the day of the week for the first of this month.  This
                // is zero-based, with 0 being the locale-specific first day of
                // the week.  Add 1 to get the 1st day of month.  Subtract
                // getFirstDayOfWeek() to make 0-based.

                int fdm = absoluteDayToDayOfWeek(dayNumber + 1) - getFirstDayOfWeek();
                if (fdm < 0) fdm += 7;

                // Find the start of the first week.  This will be a date from
                // 1..-6.  It represents the locale-specific first day of the
                // week of the first day of the month, ignoring minimal days in
                // first week.
                date = 1 - fdm + internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();

                if (isSet[WEEK_OF_MONTH])
                {
                    // Adjust for minimal days in first week.
                    if ((7 - fdm) < getMinimalDaysInFirstWeek()) date += 7;

                    // Now adjust for the week number.
                    date += 7 * (internalGet(WEEK_OF_MONTH) - 1);
                }
                else
                {
                    // Adjust into the month, if needed.
                    if (date < 1) date += 7;

                    // We are basing this on the day-of-week-in-month.  The only
                    // trickiness occurs if the day-of-week-in-month is
                    // negative.
                    int dim = internalGet(DAY_OF_WEEK_IN_MONTH);
                    if (dim >= 0) date += 7*(dim - 1);
                    else
                    {
                        // Move date to the last of this day-of-week in this
                        // month, then back up as needed.  If dim==-1, we don't
                        // back up at all.  If dim==-2, we back up once, etc.
                        // Don't back up past the first of the given day-of-week
                        // in this month.  Note that we handle -2, -3,
                        // etc. correctly, even though values < -1 are
                        // technically disallowed.
                        date += ((monthLength(year, month) - date) / 7 + dim + 1) * 7;
                    }
                }
            }
        }
        else if (isSet[DAY_OF_YEAR]) {
            dayNumber = yearStart(year) + internalGet(DAY_OF_YEAR);
        }
        else if (isSet[DAY_OF_WEEK] && isSet[WEEK_OF_YEAR])
        {
            dayNumber = yearStart(year);

            // Compute from day of week plus week of year

            // Find the day of the week for the first of this year.  This
            // is zero-based, with 0 being the locale-specific first day of
            // the week.  Add 1 to get the 1st day of month.  Subtract
            // getFirstDayOfWeek() to make 0-based.
            int fdy = absoluteDayToDayOfWeek(dayNumber + 1) - getFirstDayOfWeek();
            if (fdy < 0) fdy += 7;

            // Find the start of the first week.  This may be a valid date
            // from 1..7, or a date before the first, from 0..-6.  It
            // represents the locale-specific first day of the week
            // of the first day of the year.

            // First ignore the minimal days in first week.
            date = 1 - fdy + internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();

            // Adjust for minimal days in first week.
            if ((7 - fdy) < getMinimalDaysInFirstWeek()) date += 7;

            // Now adjust for the week number.
            date += 7 * (internalGet(WEEK_OF_YEAR) - 1);

            dayNumber += date;
        } else {    // Not enough information
            throw new IllegalArgumentException();
        }

        long millis = dayNumber * DAY_MS + HIJRA_MILLIS;
        
        // Add in the days we calculated above
        millis += (date - 1) * DAY_MS;

        // Now we can do the time portion of the conversion.

        int millisInDay = 0;

        // Hours
        if (isSet[HOUR_OF_DAY]) {
            // Don't normalize here; let overflow bump into the next period.
            // This is consistent with how we handle other fields.
            millisInDay += internalGet(HOUR_OF_DAY);

         } else if (isSet[HOUR])
        {
            // Don't normalize here; let overflow bump into the next period.
            // This is consistent with how we handle other fields.
            millisInDay += internalGet(HOUR);

            millisInDay += 12 * internalGet(AM_PM);
        }

        // Minutes. We use the fact that unset == 0
        millisInDay *= 60;
        millisInDay += internalGet(MINUTE);

        // Seconds. unset == 0
        millisInDay *= 60;
        millisInDay += internalGet(SECOND);

        // Milliseconds. unset == 0
        millisInDay *= 1000;
        millisInDay += internalGet(MILLISECOND);

        // Add millis and millisInDay together, to make millis contain the GMT time
        // computed so far, with no DST adjustments
        millis += millisInDay;

        int dstOffset = 0;

        //
        // Compute the time zone offset and DST offset.
        // Since the TimeZone API expects the Gregorian year, month, etc.,
        // We have to convert to local Gregorian time in order to
        // figure out the time zone calculations.  This is a bit slow, but
        // it saves us from doing some *really* nasty calculations here.
        //
        if (getTimeZone().useDaylightTime())
        {
            synchronized(gregorian) {
                gregorian.setTimeZone(zone);
                gregorian.setTime(new Date(millis));
                dstOffset = gregorian.get(DST_OFFSET);
            }
        }

        // Store our final computed GMT time, with DST adjustments.
        time = millis - zoneOffset - dstOffset;
        isTimeSet = true;
    }

    /**
     * Validates the values of the set time fields.
     */
    private boolean validateFields()
    {
        for (int field = 0; field < FIELD_COUNT; field++)
        {
            // Ignore DATE and DAY_OF_YEAR which are handled below
            if (isSet[field] &&
                !boundsCheck(internalGet(field), field))

                return false;
        }

        if (isSet[YEAR])
        {
            int year = internalGet(YEAR);
            if (year < 1)
                return false;
        }

        // Handle DAY_OF_WEEK_IN_MONTH, which must not have the value zero.
        // We've checked against minimum and maximum above already.
        if (isSet[DAY_OF_WEEK_IN_MONTH] &&
            0 == internalGet(DAY_OF_WEEK_IN_MONTH)) return false;

        return true;
    }

    /**
     * Validates the value of the given time field.
     */
    private boolean boundsCheck(int value, int field)
    {
        return value >= getMinimum(field) && value <= getMaximum(field);
    }


    //-------------------------------------------------------------------------
    // Functions for converting from milliseconds to field values
    //-------------------------------------------------------------------------

    /**
     * Converts UTC as milliseconds to time field values.
     * The time is <em>not</em>
     * recomputed first; to recompute the time, then the fields, call the
     * {@link #complete} method.
     */
    protected void computeFields()
    {
        if (areFieldsSet) return;

        // The following algorithm only works for dates after the Hijra (16 July AD 622)
        if (time < HIJRA_MILLIS && !isLenient()) {
            throw new IllegalArgumentException("IslamicCalendar does not handle dates before 1 AH");
        }

        //
        // Compute the time zone offset and DST offset.
        // Since the TimeZone API expects the Gregorian year, month, etc.,
        // We have to convert to local Gregorian time in order to
        // figure out the time zone calculations.  This is a bit slow, but
        // it saves us from doing some *really* nasty calculations here.
        //
        TimeZone zone = getTimeZone();
        int rawOffset = zone.getRawOffset();   // Not including DST
        int dstOffset = 0;
        if (zone.useDaylightTime())
        {
            synchronized (gregorian) {
                gregorian.setTimeZone(zone);
                gregorian.setTime(new Date(time));
                dstOffset += gregorian.get(DST_OFFSET);
            }
        }
        long localMillis = time + rawOffset + dstOffset;
        
        long days = (localMillis - HIJRA_MILLIS) / DAY_MS;
        int millisInDay = (int)(localMillis % DAY_MS);
        
        if (civil) {
            // Use the civil calendar approximation, which is just arithmetic
            int year  = (int)Math.floor( (30 * days + 10646) / 10631.0 );
            int month = (int)Math.ceil((days - 29 - yearStart(year)) / 29.5 );
            month = Math.min(month, 11);
            
            int date = (int)(days - monthStart(year, month)) + 1;
            
            fields[YEAR] = year;
            fields[MONTH] = month;
            fields[DATE] = date;
        } else {
            // Guess at the number of elapsed full months since the epoch
            int months = (int)Math.floor(days / CalendarAstronomer.SYNODIC_MONTH);

            long start = (long)Math.floor(months * CalendarAstronomer.SYNODIC_MONTH - 1);

            if ( days - start >= 28 && MoonAge(time) > 0) {
                // If we're near the end of the month, assume next month and search backwards
                months++;
            }

            // Find out the last time that the new moon was actually visible at this longitude
            // This returns midnight the night that the moon was visible at sunset.
            while ((start = trueMonthStart(months)) > days) {
                // If it was after the date in question, back up a month and try again
                months--;
            }

            fields[YEAR] = months / 12 + 1;
            fields[MONTH] = months % 12;
            fields[DATE] = (int)(days - start) + 1;

        }
        fields[ERA] = 0;

        // Calculate the day of the week.
        int dayOfWeek = absoluteDayToDayOfWeek(days);
        fields[DAY_OF_WEEK] = dayOfWeek;
        fields[WEEK_OF_MONTH] = weekNumber(fields[DATE], dayOfWeek);
        fields[DAY_OF_WEEK_IN_MONTH] = (fields[DATE]-1) / 7 + 1;

        // Now figure out the day of the year.
        int dayOfYear = (int)(days - monthStart(fields[YEAR], fields[MONTH]) + 1);

        fields[DAY_OF_YEAR] = dayOfYear;
        fields[WEEK_OF_YEAR] = weekNumber(dayOfYear, dayOfWeek);

        // Fill in all time-related fields based on millisInDay.

        fields[MILLISECOND] = millisInDay % 1000;
        millisInDay /= 1000;
        fields[SECOND] = millisInDay % 60;
        millisInDay /= 60;
        fields[MINUTE] = millisInDay % 60;
        millisInDay /= 60;
        fields[HOUR_OF_DAY] = millisInDay;
        fields[AM_PM] = millisInDay / 12;
        fields[HOUR] = millisInDay % 12;

        fields[ZONE_OFFSET] = rawOffset;
        fields[DST_OFFSET] = dstOffset;

        areFieldsSet = true;

        // Careful here: We are manually setting the isSet[] flags to true, so we
        // must be sure that the above code actually does set all these fields.
        for (int i=0; i<FIELD_COUNT; ++i) isSet[i] = true;
    }

    //-------------------------------------------------------------------------
    // Assorted calculation utilities
    //

    /**
     * Find the day of the week for a given day
     *
     * @param day   The # of days since the start of the Islamic calendar.
     */
    private static final int absoluteDayToDayOfWeek(long day)
    {
        // Calculate the day of the week.
        // This relies on the fact that the epoch was a Thursday.
        int dayOfWeek = (int)(day + THURSDAY) % 7 + SUNDAY;
        if (dayOfWeek < 0) {
            dayOfWeek += 7;
        }
        return dayOfWeek;
    }

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
        if (civil) {
            return (year-1)*354 + (long)Math.floor((3+11*year)/30.0);
        } else {
            return trueMonthStart(12*(year-1));
        }
    }
    
    /**
     * Return the number of days in the given Islamic year
     */
    private final int yearLength(int year)
    {
        if (civil) {
            return 354 + (civilLeapYear(year) ? 1 : 0);
        } else {
            int month = 12*(year-1);
            return (int)(trueMonthStart(month + 12) - trueMonthStart(month));
        }
    }

    /**
     * Return the day # on which the given month starts.  Days are counted
     * from the Hijri epoch, origin 0.
     *
     * @param year  The hijri year
     * @param year  The hijri month, 0-based
     */
    private long monthStart(int year, int month) {
        if (civil) {
            return (long)Math.ceil(29.5*month)
                    + (year-1)*354 + (long)Math.floor((3+11*year)/30.0);
        } else {
            return trueMonthStart(12*(year-1) + month);
        }
    }
    
    /**
     * Return the length (in days) of the given month.
     *
     * @param year  The hijri year
     * @param year  The hijri month, 0-based
     */
    private final int monthLength(int year, int month)
    {
        int length = 0;
        
        if (civil) {
            length = 29 + (month+1) % 2;
            if (month == DHU_AL_HIJJAH && civilLeapYear(year)) {
                length++;
            }
        } else {
            month = 12*(year-1) + month;
            length = (int)( trueMonthStart(month+1) - trueMonthStart(month) );
        }
        return length;
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
                        + (long)Math.floor(month * CalendarAstronomer.SYNODIC_MONTH - 1) * DAY_MS;

            double age = MoonAge(origin);

            if (MoonAge(origin) >= 0) {
                // The month has already started
                do {
                    origin -= DAY_MS;
                    age = MoonAge(origin);
                } while (age >= 0);
            }
            else {
                // Preceding month has not ended yet.
                do {
                    origin += DAY_MS;
                    age = MoonAge(origin);
                } while (age < 0);
            }

            start = (origin - HIJRA_MILLIS) / DAY_MS + 1;
            
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
    static final double MoonAge(long time)
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

    // We need a GregorianCalendar object for doing time zone calculations
    private static GregorianCalendar gregorian = new GregorianCalendar();
    
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

    static private void debug(String str) {
        if (true) {
            System.out.println(str);
        }
    }
}