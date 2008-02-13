//##header J2SE15
/*
*   Copyright (C) 2008, International Business Machines
*   Corporation and others.  All Rights Reserved.
*/

package com.ibm.icu.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
//import java.util.Date;
import java.util.Locale;
//import java.util.MissingResourceException;

//import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.DateInterval;
import com.ibm.icu.text.DateIntervalInfo;

/**
 *
 * <code>DateIntervalFormat</code> is a class for formatting and parsing date 
 * intervals in a language-independent manner. 
 * <P>
 * Date interval means from one date to another date,
 * for example, from "Jan 11, 2008" to "Jan 18, 2008".
 * We introduced class <code>DateInterval</code> to represent it.
 * <code>DateInterval</code> is a pair of <code>UDate</code>, which is 
 * the standard milliseconds since 24:00 GMT, Jan 1, 1970.
 * <P>
 * <code>DateIntervalFormat</code> formats a <code>DateInterval</code> into
 * text. And it parses text into <code>DateInterval</code>, 
 * although initially, parsing is not supported. 
 * <P>
 * Formatting a <code>DateInterval</code> is pattern-driven. It is very
 * similar to formatting in <code>DateIntervalFormat</code>.
 * We introduce class <code>DateIntervalInfo</code> to save date interval 
 * patterns, similar to date time pattern in <code>DateIntervalFormat</code>.
 * <P>
 * <code>DateIntervalFormat</code> needs the following information for correct 
 * formatting: time zone, calendar type, pattern, date format symbols, 
 * and DateIntervalInfo. 
 * <P>
 * Clients are encouraged to create a date-time interval formatter using 
 * locale and pre-defined skeleton macros
 * <pre>
 * DateIntervalFormat.getInstance(String skeleton, boolean adjustWidth, Locale);
 * </pre>
 * <P>
 * Following are the predefined skeleton macros ( defined in udat.h).
 * <P>
 * If clients decided to create <code>DateIntervalFormat</code> object 
 * by supplying their own interval patterns, they can do so with 
 * <pre>
 * DateIntervalFormat.getInstance(String skeleton,
 *  *        boolean adjustWidth, ,DateIntervalInfo);
 * </pre>
 * Here, <code>DateIntervalFormat</code> object is initialized with the interval * patterns client supplied. It provides flexibility for powerful usage.
 *
 * <P>
 * <code>DateIntervalFormat</code> uses the same syntax as that of
 * Date/Time format.
 * 
 * <P>
 * Code Sample:
 * <pre>
 * \code
 *   // the date interval object which the DateIntervalFormat formats on
 *   // and parses into
 *   DateInterval  dtInterval = new DateInterval(1000*3600*24, 1000*3600*24*2);
 *   DateIntervalFormat dtIntervalFmt = DateIntervalFormat.getInstance(
 *                           DAY_MONTH_YEAR_FULL_FORMAT,
 *                           FALSE, Locale("en", "GB", ""));
 *   String dateIntervalString;
 *   // formatting
 *   dtIntervalFmt.format(dtInterval, dateIntervalString);
 * \endcode
 * </pre>
 */

public class DateIntervalFormat extends UFormat {

    // FIXME: SERIAL version ID
    
    // Following is a set of skeletons having predefined interval patterns
    // in resource file.
    /**
     * Predefined skeleton -- long format with day, month, year, and 
     * day of week
     */
    public static final String DAY_MONTH_YEAR_DOW_LONG_FORMAT = "EEEEdMMMMy";
    
    /**
     * Predefined skeleton -- long format with day, month, and year
     */
    public static final String DAY_MONTH_YEAR_LONG_FORMAT = "dMMMMy";
    
    /**
     * Predefined skeleton -- long format with day and month
     */
    public static final String DAY_MONTH_LONG_FORMAT = "dMMMM";
    
    /**
     * Predefined skeleton -- long format with month and year
     */
    public static final String MONTH_YEAR_LONG_FORMAT = "MMMMy";
    
    /**
     * Predefined skeleton -- long format with day, month, and day of week
     */
    public static final String DAY_MONTH_DOW_LONG_FORMAT = "EEEEdMMMM";
    
    /**
     * Predefined skeleton -- medium format with day, month, year, and 
     * day of week
     */
    public static final String DAY_MONTH_YEAR_DOW_MEDIUM_FORMAT = "EEEdMMMy";
    
    /**
     * Predefined skeleton -- medium format with day, month, and year
     */
    public static final String DAY_MONTH_YEAR_MEDIUM_FORMAT = "dMMMy";
    
    /**
     * Predefined skeleton -- medium format with day and month
     */
    public static final String DAY_MONTH_MEDIUM_FORMAT = "dMMM";
    
    /**
     * Predefined skeleton -- medium format with month and year
     */
    public static final String MONTH_YEAR_MEDIUM_FORMAT = "MMMy";
    
    /**
     * Predefined skeleton -- medium format with day, month, and day of week
     */
    public static final String DAY_MONTH_DOW_MEDIUM_FORMAT = "EEEdMMM";

    /**
     * Predefined skeleton -- short format with day, month, year, and 
     * day of week
     */
    public static final String DAY_MONTH_YEAR_DOW_SHORT_FORMAT = "EEEdMy";
    
    /**
     * Predefined skeleton -- short format with day, month, and year
     */
    public static final String DAY_MONTH_YEAR_SHORT_FORMAT = "dMy";
    
    /**
     * Predefined skeleton -- short format with day and month
     */
    public static final String DAY_MONTH_SHORT_FORMAT = "dM";
    
    /**
     * Predefined skeleton -- short format with month and year
     */
    public static final String MONTH_YEAR_SHORT_FORMAT = "My";
    
    /**
     * Predefined skeleton -- short format with day, month, and day of week
     */
    public static final String DAY_MONTH_DOW_SHORT_FORMAT = "EEEdM";
    
    /**
     * Predefined skeleton -- short format with day only
     */
    public static final String DAY_ONLY_SHORT_FORMAT = "d";
    
    /**
     * Predefined skeleton -- short format with day and day of week
     */
    public static final String DAY_DOW_SHORT_FORMAT = "EEEd";
    
    /**
     * Predefined skeleton -- short format with year only
     */
    public static final String YEAR_ONLY_SHORT_FORMAT = "y";
    
    /**
     * Predefined skeleton -- short format with month only
     */
    public static final String MONTH_ONLY_SHORT_FORMAT = "M";
    
    /**
     * Predefined skeleton -- medium format with month only
     */
    public static final String MONTH_ONLY_MEDIUM_FORMAT = "MMM";
    
    /**
     * Predefined skeleton -- long format with month only
     */
    public static final String MONTH_ONLY_LONG_FORMAT = "MMMM";
    
    /**
     * Predefined skeleton -- format with hour and minute
     */
    public static final String HOUR_MINUTE_FORMAT = "hm";
    
    /**
     * Predefined skeleton -- format with hour, minute, and generic time zone
     */
    public static final String HOUR_MINUTE_GENERAL_TZ_FORMAT = "hmv";
    
    /**
     * Predefined skeleton -- format with hour, minute, and specific time zone
     */
    public static final String HOUR_MINUTE_DAYLIGNT_TZ_FORMAT = "hmz";
    
    /**
     * Predefined skeleton -- format with hour only
     */
    public static final String HOUR_ONLY_FORMAT = "h";
    
    /**
     * Predefined skeleton -- format with hour and generic time zone
     */
    public static final String HOUR_GENERAL_TZ_FORMAT = "hv";
    
    /**
     * Predefined skeleton -- format with hour and specific time zone
     */
    public static final String HOUR_DAYLIGNT_TZ_FORMAT = "hz";


    /**
     * skeleton for pre-defined date format in DateTimePatterns
     */
    private static final String[] DATE_FORMAT_SKELETON = {
        "EEEEdMMMMy",  // full
        "dMMMMy",      // long
        "dMMMy",       // medium
        "dMy",         // short
    };

    /**
     * The interval patterns for this formatter.
     */
    private DateIntervalInfo     fIntervalPattern;

    /**
     * The DateFormat object used to format single pattern
     */
    private SimpleDateFormat     fDateFormat;

    /**
     * The 2 calendars with the from and to date.
     * could re-use the calendar in fDateFormat,
     * but keeping 2 calendars make it clear and clean.
     */
    private Calendar fFromCalendar;
    private Calendar fToCalendar;


    /**
     * Construct a DateIntervalFormat from <code>DateFormat</code>
     * and a <code>DateIntervalInfo</code>.
     * <code>DateFormat</code> provides the timezone, calendar,
     * full pattern, and date format symbols information.
     * It should be a <code>SimpleDateFormat</code> object which 
     * has a pattern in it.
     * the <code>DateIntervalInfo</code> provides the interval patterns.
     *
     * @param dtfmt     the <code>SimpleDateFormat</code> object to be adopted.
     * @param dtitvinf  the <code>DateIntervalInfo</code> object to be adopted.
     * @draft ICU 4.0
     */
    private DateIntervalFormat(DateFormat dtfmt, DateIntervalInfo dtItvInfo)
    {
        fIntervalPattern = dtItvInfo;
        fDateFormat = (SimpleDateFormat) dtfmt;
        fFromCalendar = (Calendar) dtfmt.getCalendar().clone();
        fToCalendar = (Calendar) dtfmt.getCalendar().clone();
    }



    /**
     * Construct a DateIntervalFormat from <code>DateFormat</code>
     * and a given locale.
     * <code>DateFormat</code> provides the timezone, calendar,
     * full pattern, and date format symbols information.
     * It should be a <code>SimpleDateFormat</code> object which 
     * has a pattern in it.
     *
     * The given <code>Locale</code> provides the interval patterns.
     * A skeleton is derived from the full pattern. 
     * And the interval patterns are those the skeleton maps to
     * in the given locale.
     * For example, for en_GB, if the pattern in the <code>SimpleDateFormat</code>
     * is "EEE, d MMM, yyyy", the skeleton is "EEEdMMMyyy".
     * And the interval patterns are:
     * "EEE, d MMM, yyyy - EEE, d MMM, yyyy" for year differs,
     * "EEE, d MMM - EEE, d MMM, yyyy" for month differs,
     * "EEE, d - EEE, d MMM, yyyy" for day differs,
     * @param dtfmt     the <code>DateFormat</code> object to be adopted.
     * @param locale    the given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    private static DateIntervalFormat get(DateFormat dtfmt, ULocale locale)
    {
        DateTimePatternGenerator dtptg = DateTimePatternGenerator.getInstance(locale);
        String skeleton = dtptg.getSkeleton(((SimpleDateFormat)dtfmt).getPattern());
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale, skeleton);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }


    /**
     * Construct a DateIntervalFormat from default locale. 
     * It uses the interval patterns of skeleton ("dMyhm") 
     * from the default locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance()
    {
        DateFormat dtfmt = DateFormat.getInstance();
        DateIntervalInfo dtitvinf = new DateIntervalInfo();
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * It uses the interval patterns of skeleton ("dMyhm") 
     * from the given locale.
     * @param locale    the given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance(Locale locale)
    {
        return getInstance(ULocale.forLocale(locale));
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * It uses the interval patterns of skeleton ("dMyhm") 
     * from the given locale.
     * @param locale    the given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance(ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, 
                                                   DateFormat.SHORT, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }




    /**
     * Construct a DateIntervalFormat using default locale.
     * The interval pattern is based on the date format only.
     * For full date format, interval pattern is based on skeleton "EEEEdMMMMy".
     * For long date format, interval pattern is based on skeleton "dMMMMy".
     * For medium date format, interval pattern is based on skeleton "dMMMy".
     * For short date format, interval pattern is based on skeleton "dMy".
     * @param style     The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getDateIntervalInstance(int style)
    {
        return getDateIntervalInstance(style, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the date format only.
     * For full date format, interval pattern is based on skeleton "EEEEdMMMMy".
     * For long date format, interval pattern is based on skeleton "dMMMMy".
     * For medium date format, interval pattern is based on skeleton "dMMMy".
     * For short date format, interval pattern is based on skeleton "dMy".
     * @param style     The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getDateIntervalInstance(int style, Locale locale)
    {
        return getDateIntervalInstance(style, ULocale.forLocale(locale));
    }



    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the date format only.
     * For full date format, interval pattern is based on skeleton "EEEEdMMMMy".
     * For long date format, interval pattern is based on skeleton "dMMMMy".
     * For medium date format, interval pattern is based on skeleton "dMMMy".
     * For short date format, interval pattern is based on skeleton "dMy".
     * @param style     The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getDateIntervalInstance(int style, ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getDateInstance(style, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale, 
                                                 DATE_FORMAT_SKELETON[style]);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }


    /**
     * Construct a DateIntervalFormat using default locale.
     * The interval pattern is based on the time format only.
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getTimeIntervalInstance(int style)
    {
        return getTimeIntervalInstance(style, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the time format only.
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getTimeIntervalInstance(int style, Locale locale)
    {
        return getTimeIntervalInstance(style, ULocale.forLocale(locale));
    }



    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the time format only.
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getTimeIntervalInstance(int style, ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getTimeInstance(style, locale);
        return get(dtfmt, locale);
    }


    /**
     * Construct a DateIntervalFormat using default locale.
     * The interval pattern is based on the date/time format.
     * @param dateStyle The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param timeStyle The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getDateTimeIntervalInstance(int dateStyle, int timeStyle)
    {
        return getDateTimeIntervalInstance(dateStyle, timeStyle, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the date/time format.
     * @param dateStyle The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param timeStyle The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getDateTimeIntervalInstance(int dateStyle, int timeStyle, Locale locale)
    {
        return getDateTimeIntervalInstance(dateStyle, timeStyle, ULocale.forLocale(locale));
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the date/time format.
     * @param dateStyle The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param timeStyle The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getDateTimeIntervalInstance(int dateStyle, int timeStyle,ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        return get(dtfmt, locale);
    }


    /**
     * Construct a DateIntervalFormat from skeleton and  the default locale.
     * Users are encouraged to use the class instance skeleton variables,
     * such as DAY_MONTH_FULL_FORMAT as skeleton parameter.
     * For example: 
     * DateIntervalFormat.getInstance(DAY_MONTH_FULL_FORMAT,FALSE, loc);
     * 
     * The given <code>Locale</code> provides the interval patterns.
     * For example, for en_GB, if skeleton is DAY_MONTH_YEAR_DOW_MEDIUM_FORMAT,
     * which is "EEEdMMMyyy",
     * the interval patterns defined in resource file to above skeleton are:
     * "EEE, d MMM, yyyy - EEE, d MMM, yyyy" for year differs,
     * "EEE, d MMM - EEE, d MMM, yyyy" for month differs,
     * "EEE, d - EEE, d MMM, yyyy" for day differs,
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth)
                                                 
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat from skeleton and a given locale.

     * The given <code>Locale</code> provides the interval patterns.
     * For example, for en_GB, if skeleton is DAY_MONTH_YEAR_DOW_MEDIUM_FORMAT,
     * which is "EEEdMMMyyy",
     * the interval patterns defined in resource file to above skeleton are:
     * "EEE, d MMM, yyyy - EEE, d MMM, yyyy" for year differs,
     * "EEE, d MMM - EEE, d MMM, yyyy" for month differs,
     * "EEE, d - EEE, d MMM, yyyy" for day differs,
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth, Locale locale)  
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.forLocale(locale));
    }


    /**
     * Construct a DateIntervalFormat from skeleton and a given locale.

     * The given <code>Locale</code> provides the interval patterns.
     * For example, for en_GB, if skeleton is DAY_MONTH_YEAR_DOW_MEDIUM_FORMAT,
     * which is "EEEdMMMyyy",
     * the interval patterns defined in resource file to above skeleton are:
     * "EEE, d MMM, yyyy - EEE, d MMM, yyyy" for year differs,
     * "EEE, d MMM - EEE, d MMM, yyyy" for month differs,
     * "EEE, d - EEE, d MMM, yyyy" for day differs,
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth, ULocale locale)  
    {
        DateFormat dtfmt = DateFormat.getInstance(skeleton, adjustFieldWidth, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale, skeleton);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }



    /**
     * Construct a DateIntervalFormat from skeleton
     *  <code>DateIntervalInfo</code>, and default locale.
     *
     * Users are encouraged to use the class instance skeleton variables,
     * such as DAY_MONTH_FULL_FORMAT as skeleton parameter.
     *
     * the <code>DateIntervalInfo</code> provides the interval patterns.
     *
     * User are encouraged to set default interval pattern in DateIntervalInfo
     * as well, if they want to set other interval patterns ( instead of
     * reading the interval patterns from resource files).
     * When the corresponding interval pattern for a largest calendar different
     * field is not found ( if user not set it ), interval format fallback to
     * the default interval pattern.
     * If user does not provide default interval pattern, it fallback to
     * "{date0} - {date1}" 
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param dtitvinf  the <code>DateIntervalInfo</code> object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance(String skeleton, 
                                                   boolean adjustFieldWidth,
                                                   DateIntervalInfo dtitvinf)
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.getDefault(), dtitvinf);
    }



    /**
     * Construct a DateIntervalFormat from skeleton
     * a <code>DateIntervalInfo</code>, and the given locale.
     *
     * the <code>DateIntervalInfo</code> provides the interval patterns.
     *
     * User are encouraged to set default interval pattern in DateIntervalInfo
     * as well, if they want to set other interval patterns ( instead of
     * reading the interval patterns from resource files).
     * When the corresponding interval pattern for a largest calendar different
     * field is not found ( if user not set it ), interval format fallback to
     * the default interval pattern.
     * If user does not provide default interval pattern, it fallback to
     * "{date0} - {date1}" 
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @param dtitvinf  the <code>DateIntervalInfo</code> object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance(String skeleton,
                                                 boolean adjustFieldWidth,
                                                 Locale locale, 
                                                 DateIntervalInfo dtitvinf)
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.forLocale(locale), dtitvinf);
    }



    /**
     * Construct a DateIntervalFormat from skeleton
     * a <code>DateIntervalInfo</code>, and the given locale.
     *
     * the <code>DateIntervalInfo</code> provides the interval patterns.
     *
     * User are encouraged to set default interval pattern in DateIntervalInfo
     * as well, if they want to set other interval patterns ( instead of
     * reading the interval patterns from resource files).
     * When the corresponding interval pattern for a largest calendar different
     * field is not found ( if user not set it ), interval format fallback to
     * the default interval pattern.
     * If user does not provide default interval pattern, it fallback to
     * "{date0} - {date1}" 
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @param dtitvinf  the <code>DateIntervalInfo</code> object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     */
    public static final DateIntervalFormat getInstance(String skeleton,
                                                 boolean adjustFieldWidth,
                                                 ULocale locale, 
                                                 DateIntervalInfo dtitvinf)
    {
        DateFormat dtfmt = DateFormat.getInstance(skeleton, adjustFieldWidth, locale);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }


    /**
     * Clone this Format object polymorphically. 
     * @return    A copy of the object.
     * @exception IllegalStateException  if clone is not supported
     * @draft ICU 4.0
     */
    public Object clone() throws IllegalStateException
    {
        try {
            DateIntervalFormat other = (DateIntervalFormat) super.clone();
            other.fDateFormat = (SimpleDateFormat) fDateFormat.clone();
            other.fIntervalPattern = (DateIntervalInfo) fIntervalPattern.clone();
            other.fFromCalendar = (Calendar) fFromCalendar.clone();
            other.fToCalendar = (Calendar) fToCalendar.clone();
            return other;
        } catch ( IllegalStateException e) {
            throw new IllegalStateException("clone not supported");
        }
    }


    /**
     * Format an object to produce a string. This method handles Formattable
     * objects with a <code>DateInterval</code> type. 
     * If a the Formattable object type is not a <code>DateInterval</code>,
     * then it returns a failing UErrorCode.
     *
     * @param obj               The object to format. 
     *                          Must be a <code>DateInterval</code>.
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param fieldPosition     On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @exception IllegalArgumentException  if the formatted object is not 
     *                                      DateInterval object
     * @draft ICU 4.0
     */
    public final StringBuffer 
        format(Object obj, StringBuffer appendTo, FieldPosition fieldPosition)
        throws IllegalArgumentException
    {
        if ( obj instanceof DateInterval ) {
            return format( (DateInterval)obj, appendTo, fieldPosition);
        }
        else {
            throw new IllegalArgumentException("Cannot format given Object (" + obj.getClass().getName() + ") as a DateInterval");
        }
    }
                                    
                                    

    /**
     * Format a <code>DateInterval</code> to produce a string. 
     *
     * @param dtInterval        <code>DateInterval</code> to be formatted.
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param fieldPosition     On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @draft ICU 4.0
     */
    public final StringBuffer format(DateInterval dtInterval,
                                     StringBuffer appendTo,
                                     FieldPosition fieldPosition)
    {
        fFromCalendar.setTimeInMillis(dtInterval.getFromDate());
        fToCalendar.setTimeInMillis(dtInterval.getToDate());
        return format(fFromCalendar, fToCalendar, appendTo, fieldPosition);
    }
                                    
                                    
    /**
     * Format a <code>DateInterval</code> to produce a string. 
     *
     * @param fromCalendar      calendar set to the from date in date interval
     *                          to be formatted into date interval stirng
     * @param toCalendar        calendar set to the to date in date interval
     *                          to be formatted into date interval stirng
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param pos               On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @exception IllegalArgumentException  if the two calendars are not equivalent
     * @draft ICU 4.0
     */
    public final StringBuffer format(Calendar fromCalendar,
                                     Calendar toCalendar,
                                     StringBuffer appendTo,
                                     FieldPosition pos)
                              throws IllegalArgumentException
    {
        // not support different calendar types and time zones
        if ( !fromCalendar.isEquivalentTo(toCalendar) ) {
            throw new IllegalArgumentException("can not format on two different calendars");
        }
    
        // First, find the largest different calendar field.
        int field = Calendar.MILLISECONDS_IN_DAY + 1; //BASE_FIELD_COUNT
    
        // FIXME: use for loop? better readability, worse performance
        if ( fromCalendar.get(Calendar.ERA) != toCalendar.get(Calendar.ERA) ) {
            field = Calendar.ERA;
        } else if ( fromCalendar.get(Calendar.YEAR) != 
                    toCalendar.get(Calendar.YEAR) ) {
            field = Calendar.YEAR;
        } else if ( fromCalendar.get(Calendar.MONTH) !=
                    toCalendar.get(Calendar.MONTH) ) {
            field = Calendar.MONTH;
        } else if ( fromCalendar.get(Calendar.DATE) !=
                    toCalendar.get(Calendar.DATE) ) {
            field = Calendar.DATE;
        } else if ( fromCalendar.get(Calendar.AM_PM) !=
                    toCalendar.get(Calendar.AM_PM) ) {
            field = Calendar.AM_PM;
        } else if ( fromCalendar.get(Calendar.HOUR) !=
                    toCalendar.get(Calendar.HOUR) ) {
            field = Calendar.HOUR;
        } else if ( fromCalendar.get(Calendar.MINUTE) !=
                    toCalendar.get(Calendar.MINUTE) ) {
            field = Calendar.MINUTE;
        }
    
        if ( field == Calendar.MILLISECONDS_IN_DAY + 1 ) {
            /* ignore the second/millisecond etc. small fields' difference.
             * use single date when all the above are the same.
             * FIXME: should we? or fall back?
             */
            return fDateFormat.format(fromCalendar, appendTo, pos);
        }
        
        // get interval pattern
        String intervalPattern = fIntervalPattern.getIntervalPattern(field);
    
        if ( intervalPattern == null ) {
            if ( fDateFormat.smallerFieldUnit(field) ) {
                /* the largest different calendar field is small than
                 * the smallest calendar field in pattern,
                 * return single date format.
                 */
                return fDateFormat.format(fromCalendar, appendTo, pos);
            }
    
            /* following only happen if 
             * 1. the largest different calendar field is ERA, or
             * 2. user create a default DateIntervalInfo and 
             * add interval pattern, then pass this DateIntervalInfo to create
             * a DateIntervalFormat.
             * otherwise, the DateIntervalInfo in DateIntervalFormat is created
             * from locale, and the interval pattern should not be empty
             */
            intervalPattern = fIntervalPattern.getFallbackIntervalPattern();
            if ( intervalPattern == null ) {
                /* user did not setFallbackIntervalPattern,
                 * the final fall back is:
                 * {date0} - {date1}
                 */
                fDateFormat.format(fromCalendar, appendTo, pos);
                // default separator is " EN_DASH ". FIXME: static final?
                appendTo.append(" \u2013 ");
                fDateFormat.format(toCalendar, appendTo, pos);
                return appendTo;
            }
        }
    
        /* split the interval pattern into 2 parts.
         * it assumes the interval pattern must have pattern letters in it.
         * If user set interval pattern as following:
         * setIntervalPattern(Calendar.YEAR, "'all diff'").
         * the interval string wont be "all diff", it will be a single
         * date time pattern
         */
        boolean inQuote = false;
        char prevCh = 0;
        int count = 0;
    
        /* repeatedPattern used to record whether a pattern has already seen.
           It is a pattern applies to first calendar if it is first time seen,
           otherwise, it is a pattern applies to the second calendar
         */
        int[] patternRepeated = 
        {
        //       A   B   C   D   E   F   G   H   I   J   K   L   M   N   O
            0, 0, 0, 0, 0,  0, 0,  0,  0, 0, 0, 0, 0,  0, 0, 0,
        //   P   Q   R   S   T   U   V   W   X   Y   Z
            0, 0, 0, 0, 0,  0, 0,  0,  0, 0, 0, 0, 0,  0, 0, 0,
        //       a   b   c   d   e   f   g   h   i   j   k   l   m   n   o
            0, 0, 0, 0, 0,  0, 0,  0,  0, 0, 0, 0, 0,  0, 0, 0,
        //   p   q   r   s   t   u   v   w   x   y   z
            0, 0, 0, 0, 0,  0, 0,  0,  0, 0, 0, 0, 0,  0, 0, 0,
        };
    
        int patternCharBase = 0x40;
        
        /* loop through the pattern string character by character looking for
         * the first repeated pattern letter, which breaks the interval pattern
         * into 2 parts. 
         */
        int i;
        for (i = 0; i < intervalPattern.length(); ++i) {
            char ch = intervalPattern.charAt(i);
            
            if (ch != prevCh && count > 0) {
                // check the repeativeness of pattern letter
                int repeated = patternRepeated[(int)(prevCh - patternCharBase)];
                if ( repeated == 0 ) {
                    patternRepeated[prevCh - patternCharBase] = 1;
                } else {
                    break;
                }
                count = 0;
            }
            if (ch == '\'') {
                // Consecutive single quotes are a single quote literal,
                // either outside of quotes or between quotes
                if ((i+1) < intervalPattern.length() && intervalPattern.charAt(i+1) == '\'') {
                    ++i;
                } else {
                    inQuote = ! inQuote;
                }
            } 
            else if ( ! inQuote && ((ch >= 0x0061 /*'a'*/ && ch <= 0x007A /*'z'*/) 
                        || (ch >= 0x0041 /*'A'*/ && ch <= 0x005A /*'Z'*/))) {
                // ch is a date-time pattern character 
                prevCh = ch;
                ++count;
            }
        }
    
        int splitPoint = i - count;
        if ( splitPoint < intervalPattern.length() ) {
            Calendar firstCal;
            Calendar secondCal;
            if ( fIntervalPattern.firstDateInPtnIsLaterDate() ) {
                firstCal = toCalendar;
                secondCal = fromCalendar;
            } else {
                firstCal = fromCalendar;
                secondCal = toCalendar;
            }
            // break the interval pattern into 2 parts
            fDateFormat.applyPattern(intervalPattern.substring(0, splitPoint));
            fDateFormat.format(firstCal, appendTo, pos);
            fDateFormat.applyPattern(intervalPattern.substring(splitPoint, intervalPattern.length()));
            fDateFormat.format(secondCal, appendTo, pos);
        } else {
            // single date pattern
            fDateFormat.format(fromCalendar, appendTo, pos);
        }
        return appendTo;
    }


    /**
     * Format (by algorithm) <code>DateInterval</code> to produce a string. 
     * It is supposed to be used only by CLDR survey tool.
     *
     * @param fromCalendar      calendar set to the from date in date interval
     *                          to be formatted into date interval stirng
     * @param toCalendar        calendar set to the to date in date interval
     *                          to be formatted into date interval stirng
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param fieldPosition     On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @internal ICU 4.0
     */
    /* moved to SimpleDateFormat, no need to be in DateIntervalFormat
    public final StringBuffer formatByAlgorithm(Calendar fromCalendar,
                                                Calendar toCalendar,
                                                StringBuffer appendTo,
                                                FieldPosition fieldPosition)
                              throws IllegalArgumentException
    {
        // not support different calendar types and time zones
        if ( !fromCalendar.isEquivalentTo(toCalendar) ) {
            throw new IllegalArgumentException("can not format on two different calendars");
        }
    
        try {
            return fDateFormat.format(fromCalendar, toCalendar, appendTo, fieldPosition);
        } catch ( IllegalArgumentException e ) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    */


    /**
     * Parse a string to produce an object. This methods handles parsing of
     * date time interval strings into Formattable objects with 
     * <code>DateInterval</code> type, which is a pair of UDate.
     * <P>
     * In ICU 4.0, date interval format is not supported.
     * <P>
     * Before calling, set parse_pos.index to the offset you want to start
     * parsing at in the source. After calling, parse_pos.index is the end of
     * the text you parsed. If error occurs, index is unchanged.
     * <P>
     * When parsing, leading whitespace is discarded (with a successful parse),
     * while trailing whitespace is left as is.
     * <P>
     * See Format.parseObject() for more.
     *
     * @param source    The string to be parsed into an object.
     * @param parse_pos The position to start parsing at. Upon return
     *                  this param is set to the position after the
     *                  last character successfully parsed. If the
     *                  source is not parsed successfully, this param
     *                  will remain unchanged.
     * @return          A newly created Formattable* object, or NULL
     *                  on failure.  
     * @exception IllegalStateException  always throwed since parsing is not supported
     * @draft ICU 4.0
     */
    public Object parseObject(String source, ParsePosition parse_pos) 
                  throws IllegalStateException
    {
        throw new IllegalStateException("parsing is not supported");
    }


    /**
     * Gets the date time interval patterns.
     * @return a copy of the date time interval patterns associated with
     * this date interval formatter.
     * @draft ICU 4.0
     */
    public DateIntervalInfo getDateIntervalInfo()
    {
        return fIntervalPattern;
    }


    /**
     * Set the date time interval patterns. 
     * @param newItvPattern   the given interval patterns to copy.
     * @draft ICU 4.0
     */
    public void setDateIntervalInfo(DateIntervalInfo newItvPattern)
    {
        fIntervalPattern = newItvPattern;
    }


    /**
     * Gets the date formatter
     * @return a copy of the date formatter associated with
     * this date interval formatter.
     * @draft ICU 4.0
     */
    public DateFormat getDateFormat()
    {
        return fDateFormat;
    }


    /**
     * Set the date formatter.
     * @param newDateFormat   the given date formatter to copy.
     *                        caller needs to make sure that
     *                        it is a SimpleDateFormatter.
     * @exception IllegalArgumentException  if the passing in DateFormat is not
     *                                      a simpel date formatter
     * @draft ICU 4.0
     */
    public void setDateFormat(DateFormat newDateFormat) 
                throws IllegalArgumentException
    {
        if ( newDateFormat instanceof SimpleDateFormat ) {
            fDateFormat = (SimpleDateFormat) newDateFormat;
            fFromCalendar = (Calendar) fDateFormat.getCalendar().clone();
            fToCalendar = (Calendar) fDateFormat.getCalendar().clone();
        } else {
            throw new IllegalArgumentException("Can not setDateFormat using non SimpleDateFormat");
        }
    }

};
 
