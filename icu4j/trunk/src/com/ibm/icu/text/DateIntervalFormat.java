//##header J2SE15
//#if defined(FOUNDATION10) || defined(J2SE13)
//#else
/*
*   Copyright (C) 2008, International Business Machines
*   Corporation and others.  All Rights Reserved.
*/

package com.ibm.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.CalendarData;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.DateInterval;
import com.ibm.icu.text.DateIntervalInfo;
import com.ibm.icu.text.SimpleDateFormat;


/**
 * DateIntervalFormat is a class for formatting and parsing date 
 * intervals in a language-independent manner. 
 *
 * <P>
 * Date interval means from one date to another date,
 * for example, from "Jan 11, 2008" to "Jan 18, 2008".
 * We introduced class DateInterval to represent it.
 * DateInterval is a pair of UDate, which is 
 * the standard milliseconds since 24:00 GMT, Jan 1, 1970.
 *
 * <P>
 * DateIntervalFormat formats a DateInterval into
 * text as compactly as possible. 
 * For example, the date interval format from "Jan 11, 2008" to "Jan 18,. 2008"
 * is "Jan 11-18, 2008" for English.
 * And it parses text into DateInterval, 
 * although initially, parsing is not supported. 
 *
 * <P>
 * There is no structural information in date time patterns. 
 * For any punctuations and string literals inside a date time pattern, 
 * we do not know whether it is just a separator, or a prefix, or a suffix. 
 * Without such information, so, it is difficult to generate a sub-pattern 
 * (or super-pattern) by algorithm.
 * So, formatting a DateInterval is pattern-driven. It is very
 * similar to formatting in SimpleDateFormat.
 * We introduce class DateIntervalInfo to save date interval 
 * patterns, similar to date time pattern in SimpleDateFormat.
 *
 * <P>
 * Logically, the interval patterns are mappings
 * from (skeleton, the_largest_different_calendar_field)
 * to (date_interval_pattern).
 *
 * <P>
 * A skeleton 
 * <ol>
 * <li>
 * only keeps the field pattern letter and ignores all other parts 
 * in a pattern, such as space, punctuations, and string literals.
 * <li>
 * hides the order of fields. 
 * <li>
 * might hide a field's pattern letter length.
 *
 * For those non-digit calendar fields, the pattern letter length is 
 * important, such as MMM, MMMM, and MMMMM; EEE and EEEE, 
 * and the field's pattern letter length is honored.
 *    
 * For the digit calendar fields,  such as M or MM, d or dd, yy or yyyy, 
 * the field pattern length is ignored and the best match, which is defined 
 * in date time patterns, will be returned without honor the field pattern
 * letter length in skeleton.
 * </ol>
 *
 * <P>
 * There is a set of pre-defined static skeleton strings.
 * The skeletons defined consist of the desired calendar field set 
 * (for example,  DAY, MONTH, YEAR) and the format length (long, medium, short)
 * used in date time patterns.
 * 
 * For example, skeleton YEAR_MONTH_MEDIUM_FORMAT consists month and year,
 * and it's corresponding full pattern is medium format date pattern.
 * So, the skeleton is "yMMM", for English, the full pattern is "MMM yyyy", 
 * which is the format by removing DATE from medium date format.
 *
 * For example, skeleton YEAR_MONTH_DOW_DAY_MEDIUM_FORMAT consists day, month,
 * year, and day-of-week, and it's corresponding full pattern is the medium
 * format date pattern. So, the skeleton is "yMMMEEEd", for English,
 * the full pattern is "EEE, MMM d, yyyy", which is the medium date format
 * plus day-of-week.
 *
 * <P>
 * The calendar fields we support for interval formatting are:
 * year, month, date, day-of-week, am-pm, hour, hour-of-day, and minute.
 * Those calendar fields can be defined in the following order:
 * year >  month > date > hour (in day) >  minute 
 *  
 * The largest different calendar fields between 2 calendars is the
 * first different calendar field in above order.
 *
 * For example: the largest different calendar fields between "Jan 10, 2007" 
 * and "Feb 20, 2008" is year.
 *   
 * <P>
 * There are pre-defined interval patterns for those pre-defined skeletons
 * in locales' resource files.
 * For example, for a skeleton YEAR_MONTH_DAY_MEDIUM_FORMAT, which is  "yMMMd",
 * in  en_US, if the largest different calendar field between date1 and date2 
 * is "year", the date interval pattern  is "MMM d, yyyy - MMM d, yyyy", 
 * such as "Jan 10, 2007 - Jan 10, 2008".
 * If the largest different calendar field between date1 and date2 is "month",
 * the date interval pattern is "MMM d - MMM d, yyyy",
 * such as "Jan 10 - Feb 10, 2007".
 * If the largest different calendar field between date1 and date2 is "day",
 * the date interval pattern is ""MMM d-d, yyyy", such as "Jan 10-20, 2007".
 *
 * For date skeleton, the interval patterns when year, or month, or date is 
 * different are defined in resource files.
 * For time skeleton, the interval patterns when am/pm, or hour, or minute is
 * different are defined in resource files.
 *
 * <P>
 * If a skeleton is not found in a locale's DateIntervalInfo, which means
 * the interval patterns for the skeleton is not defined in resource file,
 * the interval pattern will falls back to the interval "fallback" pattern 
 * defined in resource file.
 * If the interval "fallback" pattern is not defined, the default fall-back
 * is "{date0} - {data1}".
 *
 * <P>
 * For the combination of date and time, 
 * The rule to genearte interval patterns are:
 * <ul>
 * <li>
 *    1) when the year, month, or day differs, falls back to fall-back
 *    interval pattern, which mostly is the concatenate the two original 
 *    expressions with a separator between, 
 *    For example, interval pattern from "Jan 10, 2007 10:10 am" 
 *    to "Jan 11, 2007 10:10am" is 
 *    "Jan 10, 2007 10:10 am - Jan 11, 2007 10:10am" 
 * <li>
 *    2) otherwise, present the date followed by the range expression 
 *    for the time.
 *    For example, interval pattern from "Jan 10, 2007 10:10 am" 
 *    to "Jan 10, 2007 11:10am" is "Jan 10, 2007 10:10 am - 11:10am" 
 * </ul>
 *
 *
 * <P>
 * If two dates are the same, the interval pattern is the single date pattern.
 * For example, interval pattern from "Jan 10, 2007" to "Jan 10, 2007" is 
 * "Jan 10, 2007".
 *
 * Or if the presenting fields between 2 dates have the exact same values,
 * the interval pattern is the  single date pattern. 
 * For example, if user only requests year and month,
 * the interval pattern from "Jan 10, 2007" to "Jan 20, 2007" is "Jan 2007".
 *
 * <P>
 * DateIntervalFormat needs the following information for correct 
 * formatting: time zone, calendar type, pattern, date format symbols, 
 * and date interval patterns.
 * It can be instantiated in several ways:
 * <ul>
 * <li>
 * 1. create a date interval instance based on default or given locale plus 
 *    date format style FULL, or LONG, or MEDIUM, or SHORT.
 * 2. create a time interval instance based on default or given locale plus 
 *    time format style FULL, or LONG, or MEDIUM, or SHORT.
 * 3. create date and time interval instance based on default or given locale
 *    plus data and time format style
 * 4. create an instance using default or given locale plus default skeleton, 
 *    which  is "dMyhm"
 * 5. create an instance using default or given locale plus given skeleton.
 *    Users are encouraged to created date interval formatter this way and 
 *    to use the pre-defined skeleton macros, such as
 *    YEAR_MONTH_SHORT_FORMAT, which consists the calendar fields and
 *    the format style. 
 * 6. create an instance using default or given locale plus given skeleton
 *    plus a given DateIntervalInfo.
 *    This factory method is for powerful users who want to provide their own 
 *    interval patterns. 
 *    Locale provides the timezone, calendar, and format symbols information.
 *    Local plus skeleton provides full pattern information.
 *    DateIntervalInfo provides the date interval patterns.
 * <li>
 *
 * <P>
 * For the calendar field pattern letter, such as G, y, M, d, a, h, H, m, s etc.
 * DateIntervalFormat uses the same syntax as that of
 * DateTime format.
 * 
 * <P>
 * Code Sample: general usage
 * <pre>
 * \code
 *   // the date interval object which the DateIntervalFormat formats on
 *   // and parses into
 *   DateInterval dtInterval = new DateInterval(1000*3600*24L, 1000*3600*24*2L);
 *   DateIntervalFormat dtIntervalFmt = DateIntervalFormat.getInstance(
 *                   YEAR_MONTH_DAY_FULL_FORMAT, false, Locale("en", "GB", ""));
 *   StringBuffer str = new StringBuffer("");
 *   FieldPosition pos = new FieldPosition(0);
 *   // formatting
 *   dtIntervalFmt.format(dtInterval, dateIntervalString, pos);
 * \endcode
 * </pre>
 *
 * <P>
 * Code Sample: for powerful users who wants to use their own interval pattern
 * <pre>
 * \code
 *     import com.ibm.icu.text.DateIntervalInfo;
 *     import com.ibm.icu.text.DateIntervalFormat;
 *     ....................
 *     
 *     // Get DateIntervalFormat instance using default locale
 *     DateIntervalFormat dtitvfmt = DateIntervalFormat.getInstance();
 *     
 *     // Create a SimpleDateFormat object as usual.
 *     // The SimpleDateFormat object has calendar, timezone, format symbols, 
 *     // and full pattern, which are needed for date interval formatter.
 *    * SimpleDateFormat dtfmt = new SimpleDateFormat("yyyy 'year' MMM 'month' dd 'day'", locale);
 *     
 *     // Set the new SimpleDateFormat object just created as the date formatter in date interval formatter
 *     dtitvfmt.setDateFormat(dtfmt);
 *     
 *     // Create an empty DateIntervalInfo object, which does not have any interval patterns inside.
 *     dtitvinf = new DateIntervalInfo();
 *     
 *     // a series of set interval patterns.
 *     // Only ERA, YEAR, MONTH, DATE,  DAY_OF_MONTH, DAY_OF_WEEK, AM_PM,  HOUR, HOUR_OF_DAY, and MINUTE  are supported.
 *     dtitvinf.setIntervalPattern("yMMMd", Calendar.YEAR, "'y ~ y'"); 
 *     dtitvinf.setIntervalPattern("yMMMd", Calendar.MONTH, "yyyy 'diff' MMM d - MMM d");
 *     dtitvinf.setIntervalPattern("yMMMd", Calendar.DATE, "yyyy MMM d ~ d");
 *     dtitvinf.setIntervalPattern("yMMMd", Calendar.HOUR_OF_DAY, "yyyy MMM d HH:mm ~ HH:mm");
 *     
 *     // Set fallback interval pattern. Fallback pattern is used when interval pattern is not found.
 *     // If the fall-back pattern is not set,  falls back to {date0} - {date1} if interval pattern is not found.
 *     dtitvinf.setFallbackIntervalPattern("{0} - {1}");
 *     
 *     // Set above DateIntervalInfo object as the interval patterns of date interval formatter
 *     dtitvfmt.setDateIntervalInfo(dtitvinf);
 *     
 *     // Prepare to format
 *     pos = new FieldPosition(0);
 *     str = new StringBuffer("");
 *     
 *     // The 2 calendars should be equivalent, otherwise,  IllegalArgumentException will be thrown by format()
 *     Calendar fromCalendar = (Calendar) dtfmt.getCalendar().clone();
 *     Calendar toCalendar = (Calendar) dtfmt.getCalendar().clone();
 *     fromCalendar.setTimeInMillis(....);
 *     toCalendar.setTimeInMillis(...);
 *     
 *     //Formatting given 2 calendars
 *     dtitvfmt.format(fromCalendar, toCalendar, str, pos);
 * 
 * \endcode
 * </pre>
 */

public class DateIntervalFormat extends UFormat {

    private static final long serialVersionUID = 1;

    /* Below are a set of pre-defined skeletons.
     * They have pre-defined interval patterns in resource files.
     * Users are encouraged to use them in date interval format factory methods.
     *
     * <P>
     * A skeleton 
     * <ul>
     * <li>
     * 1. only keeps the field pattern letter and ignores all other parts 
     *    in a pattern, such as space, punctuations, and string literals.
     * <li>
     * 2. hides the order of fields. 
     * <li>
     * 3. might hide a field's pattern letter length.
     *
     *    For those non-digit calendar fields, the pattern letter length is 
     *    important, such as MMM, MMMM, and MMMMM; EEE and EEEE, 
     *    and the field's pattern letter length is honored.
     *    
     *    For the digit calendar fields,  such as M or MM, d or dd, yy or yyyy, 
     *    the field pattern length is ignored and the best match, which is 
     *    defined in date time patterns, will be returned without honor 
     *    the field pattern letter length in skeleton.
     * </ul>
     *
     * <P>
     * For example, given skeleton YEAR_MONTH_DAY_SHORT_FORMAT, which is "yMd",
     * for English, the full pattern is "M/d/yy", which is the short format
     * of date pattern having DAY, MONTH, and YEAR.
     * 
     * <P>
     * The skeletons defined below consists of the desired calendar field set 
     * (for example, DAY, MONTH, YEAR) and the format length (long, medium, 
     * short) used in date time patterns.
     * 
     * For example, skeleton YEAR_MONTH_MEDIUM_FORMAT consists month and year,
     * and it's corresponding full pattern is medium format date pattern.
     * So, the skeleton is "yMMM", for English, the full pattern is "MMM yyyy", 
     * which is the format by removing DATE from medium date format.
     *
     * For example, skeleton YEAR_MONTH_DOW_DAY_MEDIUM_FORMAT consists day, 
     * month, year, and day-of-week, and it's corresponding full pattern is 
     * the medium format date pattern. So, the skeleton is "yMMMEEEd", 
     * for English, the full pattern is "EEE, MMM d, yyyy", which is 
     * the medium date format plus day-of-week.
     */
    /**
     * Predefined skeleton -- long format with day, month, year, and 
     * day of week
     */
    public static final String YEAR_MONTH_DOW_DAY_LONG_FORMAT = "yMMMMEEEEd";
    
    /**
     * Predefined skeleton -- long format with day, month, and year
     */
    public static final String YEAR_MONTH_DAY_LONG_FORMAT = "yMMMMd";
    
    /**
     * Predefined skeleton -- long format with day and month
     */
    public static final String MONTH_DAY_LONG_FORMAT = "MMMMd";
    
    /**
     * Predefined skeleton -- long format with month and year
     */
    public static final String YEAR_MONTH_LONG_FORMAT = "yMMMM";
    
    /**
     * Predefined skeleton -- long format with day, month, and day of week
     */
    public static final String MONTH_DOW_DAY_LONG_FORMAT = "MMMMEEEEd";
    
    /**
     * Predefined skeleton -- medium format with day, month, year, and 
     * day of week
     */
    public static final String YEAR_MONTH_DOW_DAY_MEDIUM_FORMAT = "yMMMEEEd";
    
    /**
     * Predefined skeleton -- medium format with day, month, and year
     */
    public static final String YEAR_MONTH_DAY_MEDIUM_FORMAT = "yMMMd";
    
    /**
     * Predefined skeleton -- medium format with day and month
     */
    public static final String MONTH_DAY_MEDIUM_FORMAT = "MMMd";
    
    /**
     * Predefined skeleton -- medium format with month and year
     */
    public static final String YEAR_MONTH_MEDIUM_FORMAT = "yMMM";
    
    /**
     * Predefined skeleton -- medium format with day, month, and day of week
     */
    public static final String MONTH_DOW_DAY_MEDIUM_FORMAT = "MMMEEEd";

    /**
     * Predefined skeleton -- short format with day, month, year, and 
     * day of week
     */
    public static final String YEAR_MONTH_DOW_DAY_SHORT_FORMAT = "yMEEEd";
    
    /**
     * Predefined skeleton -- short format with day, month, and year
     */
    public static final String YEAR_MONTH_DAY_SHORT_FORMAT = "yMd";
    
    /**
     * Predefined skeleton -- short format with day and month
     */
    public static final String MONTH_DAY_SHORT_FORMAT = "Md";
    
    /**
     * Predefined skeleton -- short format with month and year
     */
    public static final String YEAR_MONTH_SHORT_FORMAT = "yM";
    
    /**
     * Predefined skeleton -- short format with day, month, and day of week
     */
    public static final String MONTH_DOW_DAY_SHORT_FORMAT = "MEEEd";
    
    /**
     * Predefined skeleton -- short format with day only
     */
    public static final String DAY_ONLY_SHORT_FORMAT = "d";
    
    /**
     * Predefined skeleton -- short format with day and day of week
     */
    public static final String DOW_DAY_SHORT_FORMAT = "EEEd";
    
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
     * Used to save the information for a skeleton's best match skeleton.
     * It is package accessible since it is used in DateIntervalInfo too.
     */
    static final class BestMatchInfo {
        // the best match skeleton
        final String bestMatchSkeleton;
        // 0 means the best matched skeleton is the same as input skeleton
        // 1 means the fields are the same, but field width are different
        // 2 means the only difference between fields are v/z,
        // -1 means there are other fields difference
        final int    bestMatchDistanceInfo;
        BestMatchInfo(String bestSkeleton, int difference) {
            bestMatchSkeleton = bestSkeleton;
            bestMatchDistanceInfo = difference;
        }
    }


    /**
     * Used to save the information on a skeleton and its best match.
     */
    private static final class SkeletonAndItsBestMatch {
        final String skeleton;
        final String bestMatchSkeleton;
        SkeletonAndItsBestMatch(String skeleton, String bestMatch) {
            this.skeleton = skeleton;
            bestMatchSkeleton = bestMatch;
        }
    }


    /**
     * skeleton for pre-defined date format in DateTimePatterns
     */
    private static final String[] DATE_FORMAT_SKELETON = {
        YEAR_MONTH_DOW_DAY_LONG_FORMAT,
        YEAR_MONTH_DAY_LONG_FORMAT,
        YEAR_MONTH_DAY_MEDIUM_FORMAT,
        YEAR_MONTH_DAY_SHORT_FORMAT,
    };

    // Cache for the locale interval pattern
    private static ICUCache LOCAL_PATTERN_CACHE = new SimpleCache();

    
    /**
     * The interval patterns for this locale.
     */
    private DateIntervalInfo     fInfo;

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
     * Following are transient interval information
     * relavent (locale) to this formatter.
     */
    private String fSkeleton = null;
    // HashMap<String, String>  calendar_field -> interval pattern
    private transient Map fIntervalPatterns = null;
    
   
    /**
     * default constructor 
     * @draft ICU 4.0
     */
    private DateIntervalFormat() {
    }

    /**
     * Construct a DateIntervalFormat from DateFormat and a DateIntervalInfo.
     *
     * This is the convenient override of 
     * DateIntervalFormat(DateFormat, DateIntervalInfo, String) 
     * with the String value as null.
     *
     * @param dtfmt     the SimpleDateFormat object to be adopted.
     * @param dtitvinf  the DateIntervalInfo object to be adopted.
     * @draft ICU 4.0
     */
    private DateIntervalFormat(DateFormat dtfmt, DateIntervalInfo dtItvInfo)
    {
        this(dtfmt, dtItvInfo, null);
    }


    /**
     * Construct a DateIntervalFormat from DateFormat,
     * a DateIntervalInfo, and skeleton.
     * DateFormat provides the timezone, calendar,
     * full pattern, and date format symbols information.
     * It should be a SimpleDateFormat object which 
     * has a pattern in it.
     * the DateIntervalInfo provides the interval patterns.
     *
     * @param dtfmt     the SimpleDateFormat object to be adopted.
     * @param dtitvinf  the DateIntervalInfo object to be adopted.
     * @param skeleton  the skeleton of the date formatter
     * @draft ICU 4.0
     */
    private DateIntervalFormat(DateFormat dtfmt, DateIntervalInfo dtItvInfo,
                               String skeleton)
    {
        // freeze date interval info
        dtItvInfo.freeze();
        fSkeleton = skeleton;
        fInfo = dtItvInfo;
        fDateFormat = (SimpleDateFormat) dtfmt;
        fFromCalendar = (Calendar) dtfmt.getCalendar().clone();
        fToCalendar = (Calendar) dtfmt.getCalendar().clone();
        initializePattern();
    }


    /**
     * Construct a DateIntervalFormat from default locale and
     * default date time instance. 
     *
     * This is a convenient override of getDateTimeIntervalInstance() with
     * the date and time style value as DEFAULT.
     *
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getInstance()
    {
        return getInstance(ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale and
     * default date time instance.
     *
     * This is a convenient override of getDateTimeIntervalInstance() with
     * the date and time style value as DEFAULT.
     *
     * @param locale    the given locale.
     * @return          a date time interval formatter which the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getInstance(Locale locale)
    {
        return getInstance(ULocale.forLocale(locale));
    }


    /**
     * Construct a DateIntervalFormat using given locale and
     * default date time instance.
     *
     * This is a convenient override of getDateTimeIntervalInstance() with
     * the date and time style value as DEFAULT.
     *
     * @param locale    the given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getInstance(ULocale locale)
    {
        return getDateTimeIntervalInstance(DateFormat.DEFAULT, 
                                           DateFormat.DEFAULT, locale);
    }




    /**
     * Construct a DateIntervalFormat using default locale.
     *
     * This is a convenient override of 
     * getDateIntervalInstance(int, ULocale)
     * with the locale value as default locale.
     *
     * @param style     The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getDateIntervalInstance(int style)
    {
        return getDateIntervalInstance(style, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     *
     * This is a convenient override of
     * getDateIntervalInstance(int, ULocale)
     *
     * @param style     The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getDateIntervalInstance(int style, ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getDateInstance(style, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale);
        // for CJK, even for non-short format,
        // get skeleton will always return yMd.
        // so, assign it directly instead of getting if from getSkeleton().
        String skeleton = DATE_FORMAT_SKELETON[style];
        return new DateIntervalFormat(dtfmt, dtitvinf, skeleton);
    }


    /**
     * Construct a DateIntervalFormat using default locale.
     * The interval pattern is based on the time format only.
     *
     * This is the convenient override of getDateTimeIntervalInstance()
     * with the date style as NONE and a given time style.
     *
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getTimeIntervalInstance(int style)
    {
        return getTimeIntervalInstance(style, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the time format only.
     *
     * This is the convenient override of getDateTimeIntervalInstance()
     * with the date style as NONE and a given time style.
     *
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getTimeIntervalInstance(int style, Locale locale)
    {
        return getTimeIntervalInstance(style, ULocale.forLocale(locale));
    }



    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the time format only.
     *
     * This is the convenient override of getDateTimeIntervalInstance()
     * with the date style as NONE and a given time style.
     *
     * @param style     The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getTimeIntervalInstance(int style, ULocale locale)
    {
        return getDateTimeIntervalInstance(DateFormat.NONE, style, locale);
    }


    /**
     * Construct a DateIntervalFormat using default locale.
     * The interval pattern is based on the date/time format.
     *
     * This is a convenient override of 
     * getDateTimeIntervalInstance(int, int, ULocale)
     * with the locale value as default locale.
     *
     * @param dateStyle The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param timeStyle The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getDateTimeIntervalInstance(int dateStyle, int timeStyle)
    {
        return getDateTimeIntervalInstance(dateStyle, timeStyle, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat using given locale.
     * The interval pattern is based on the date/time format.
     *
     * This is a convenient override of 
     * getDateTimeIntervalInstance(int, int, ULocale)
     *
     * @param dateStyle The given date formatting style. For example,
     *                  SHORT for "M/d/yy" in the US locale.
     * @param timeStyle The given time formatting style. For example,
     *                  SHORT for "h:mm a" in the US locale.
     * @param locale    The given locale.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getDateTimeIntervalInstance(int dateStyle, int timeStyle,ULocale locale)
    {
        DateFormat dtfmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale);
        return new DateIntervalFormat(dtfmt, dtitvinf);
    }


    /**
     * Construct a DateIntervalFormat from skeleton and  the default locale.
     *
     * This is a convenient override of 
     * getInstance(String skeleton, boolean adjustFieldWidth, ULocale locale)  
     * with the value of locale as default locale.
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth)
                                                 
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.getDefault());
    }


    /**
     * Construct a DateIntervalFormat from skeleton and a given locale.
     *
     * This is a convenient override of 
     * getInstance(String skeleton, boolean adjustFieldWidth, ULocale locale)  
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth, Locale locale)  
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.forLocale(locale));
    }


    /**
     * Construct a DateIntervalFormat from skeleton and a given locale.
     *
     * There are 27 class instance skeleton variables defined,
     * such as MONTH_DAY_FULL_FORMAT, YEAR_MONTH_DOW_DAY_LONG_FORMAT etc.
     *
     * Those skeletons have pre-defined interval patterns in resource files.
     * Users are encouraged to use them. 
     * For example:
     * DateIntervalFormat.getInstance(MONTH_DAY_FULL_FORMAT, false, loc);
     * 
     * The given Locale provides the interval patterns.
     * For example, for en_GB, if skeleton is YEAR_MONTH_DOW_DAY_MEDIUM_FORMAT,
     * which is "yMMMEEEd",
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
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat 
        getInstance(String skeleton, boolean adjustFieldWidth, ULocale locale)  
    {
        DateFormat dtfmt = DateFormat.getInstance(skeleton, adjustFieldWidth, locale);
        DateIntervalInfo dtitvinf = new DateIntervalInfo(locale);
        return new DateIntervalFormat(dtfmt, dtitvinf, skeleton);
    }



    /**
     * Construct a DateIntervalFormat from skeleton
     *  DateIntervalInfo, and default locale.
     *
     * This is a convenient override of
     * getInstance(String skeleton, boolean adjustFieldWidth, 
     *             ULocale locale, DateIntervalInfo dtitvinf)
     * with the locale value as default locale.
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param dtitvinf  the DateIntervalInfo object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getInstance(String skeleton, 
                                                   boolean adjustFieldWidth,
                                                   DateIntervalInfo dtitvinf)
    {
        return getInstance(skeleton, adjustFieldWidth, ULocale.getDefault(), dtitvinf);
    }



    /**
     * Construct a DateIntervalFormat from skeleton
     * a DateIntervalInfo, and the given locale.
     *
     * This is a convenient override of
     * getInstance(String skeleton, boolean adjustFieldWidth, 
     *             ULocale locale, DateIntervalInfo dtitvinf)
     *
     * @param skeleton  the skeleton on which interval format based.
     * @param adjustFieldWidth  whether adjust the skeleton field width or not
     *                          It is used for DateTimePatternGenerator on 
     *                          whether to adjust field width when get 
     *                          full pattern from skeleton
     * @param locale    the given locale
     * @param dtitvinf  the DateIntervalInfo object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
     * a DateIntervalInfo, and the given locale.
     *
     * There are 27 class instance skeleton variables defined,
     * such as MONTH_DAY_FULL_FORMAT, YEAR_MONTH_DOW_DAY_LONG_FORMAT etc.
     *
     * Those skeletons have pre-defined interval patterns in resource files.
     * Users are encouraged to use them. 
     * For example:
     * DateIntervalFormat.getInstance(MONTH_DAY_FULL_FORMAT, false, loc,itvinf);
     *
     * the DateIntervalInfo provides the interval patterns.
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
     * @param dtitvinf  the DateIntervalInfo object to be adopted.
     * @return          a date time interval formatter whick the caller owns.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static final DateIntervalFormat getInstance(String skeleton,
                                                 boolean adjustFieldWidth,
                                                 ULocale locale, 
                                                 DateIntervalInfo dtitvinf)
    {
        DateFormat dtfmt = DateFormat.getInstance(skeleton, adjustFieldWidth, locale);
        LOCAL_PATTERN_CACHE.clear();
        // clone. If it is frozen, clone returns itself, otherwise, clone
        // returns a copy.
        dtitvinf = (DateIntervalInfo)dtitvinf.clone(); 
        return new DateIntervalFormat(dtfmt, dtitvinf, skeleton);
    }


    /**
     * Clone this Format object polymorphically. 
     * @return    A copy of the object.
     * @throws    IllegalStateException  if clone is not supported
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public Object clone() throws IllegalStateException
    {
        try {
            DateIntervalFormat other = (DateIntervalFormat) super.clone();
            other.fDateFormat = (SimpleDateFormat) fDateFormat.clone();
            other.fInfo = (DateIntervalInfo) fInfo.clone();
            other.fFromCalendar = (Calendar) fFromCalendar.clone();
            other.fToCalendar = (Calendar) fToCalendar.clone();
            other.fSkeleton = fSkeleton;
            other.fIntervalPatterns = fIntervalPatterns;
            return other;
        } catch ( IllegalStateException e) {
            throw new IllegalStateException("clone not supported");
        }
    }


    /**
     * Format an object to produce a string. This method handles Formattable
     * objects with a DateInterval type. 
     * If a the Formattable object type is not a DateInterval,
     * IllegalArgumentException is thrown.
     *
     * @param obj               The object to format. 
     *                          Must be a DateInterval.
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param fieldPosition     On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @throws    IllegalArgumentException  if the formatted object is not 
     *                                      DateInterval object
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
     * Format a DateInterval to produce a string. 
     *
     * @param dtInterval        DateInterval to be formatted.
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param fieldPosition     On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @return                  Reference to 'appendTo' parameter.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
     * Format 2 Calendars to produce a string. 
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
     * @throws    IllegalArgumentException  if the two calendars are not equivalent
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
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
        } else {
            /* ignore the second/millisecond etc. small fields' difference.
             * use single date when all the above are the same.
             */
            return fDateFormat.format(fromCalendar, appendTo, pos);
        }
        
        // get interval pattern
        DateIntervalInfo.PatternInfo intervalPattern = 
          (DateIntervalInfo.PatternInfo)fIntervalPatterns.get(
              DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
    
        if ( intervalPattern == null ) {
            if ( fDateFormat.isFieldUnitIgnored(field) ) {
                /* the largest different calendar field is small than
                 * the smallest calendar field in pattern,
                 * return single date format.
                 */
                return fDateFormat.format(fromCalendar, appendTo, pos);
            }
    
            return fallbackFormat(fromCalendar, toCalendar, appendTo, pos);
        }
    
        // If the first part in interval pattern is empty, 
        // the 2nd part of it saves the full-pattern used in fall-back.
        // For a 'real' interval pattern, the first part will never be empty.
        if ( intervalPattern.getFirstPart() == null ) {
            // fall back
            return fallbackFormat(fromCalendar, toCalendar, appendTo, pos,
                                    intervalPattern.getSecondPart());
        }
        Calendar firstCal;
        Calendar secondCal;
        if ( intervalPattern.firstDateInPtnIsLaterDate() ) {
            firstCal = toCalendar;
            secondCal = fromCalendar;
        } else {
            firstCal = fromCalendar;
            secondCal = toCalendar;
        }
        // break the interval pattern into 2 parts
        // first part should not be empty, 
        String originalPattern = fDateFormat.toPattern();
        fDateFormat.applyPattern(intervalPattern.getFirstPart());
        fDateFormat.format(firstCal, appendTo, pos);
        if ( intervalPattern.getSecondPart() != null ) {
            fDateFormat.applyPattern(intervalPattern.getSecondPart());
            fDateFormat.format(secondCal, appendTo, pos);
        }
        fDateFormat.applyPattern(originalPattern);
        return appendTo;
    }


    /**
     * Format 2 Calendars to using fall-back interval pattern
     *
     * The full pattern used in this fall-back format is the
     * full pattern of the date formatter.
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
     * @draft ICU 4.0
     */
    private final StringBuffer fallbackFormat(Calendar fromCalendar,
                                              Calendar toCalendar,
                                              StringBuffer appendTo,
                                              FieldPosition pos)  {
            // the fall back
            StringBuffer earlierDate = new StringBuffer(64);
            earlierDate = fDateFormat.format(fromCalendar, earlierDate, pos);
            StringBuffer laterDate = new StringBuffer(64);
            laterDate = fDateFormat.format(toCalendar, laterDate, pos);
            String fallbackPattern = fInfo.getFallbackIntervalPattern();
            String fallback = MessageFormat.format(fallbackPattern, new Object[]
                            {earlierDate.toString(), laterDate.toString()});
            appendTo.append(fallback);
            return appendTo;
    }




    /**
     * Format 2 Calendars to using fall-back interval pattern
     *
     * This fall-back pattern is generated on a given full pattern,
     * not the full pattern of the date formatter.
     *
     * @param fromCalendar      calendar set to the from date in date interval
     *                          to be formatted into date interval stirng
     * @param toCalendar        calendar set to the to date in date interval
     *                          to be formatted into date interval stirng
     * @param appendTo          Output parameter to receive result.
     *                          Result is appended to existing contents.
     * @param pos               On input: an alignment field, if desired.
     *                          On output: the offsets of the alignment field.
     * @param fullPattern       the full pattern need to apply to date formatter
     * @return                  Reference to 'appendTo' parameter.
     * @draft ICU 4.0
     */
    private final StringBuffer fallbackFormat(Calendar fromCalendar,
                                              Calendar toCalendar,
                                              StringBuffer appendTo,
                                              FieldPosition pos, 
                                              String fullPattern)  {
            String originalPattern = fDateFormat.toPattern();
            fDateFormat.applyPattern(fullPattern);
            fallbackFormat(fromCalendar, toCalendar, appendTo, pos);
            fDateFormat.applyPattern(originalPattern);
            return appendTo;
    }


    /**
     * Parse a string to produce an object. This methods handles parsing of
     * date time interval strings into Formattable objects with 
     * DateInterval type, which is a pair of UDate.
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
     * @throws UnsupportedOperationException  always thrown since parsing is not supported
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public Object parseObject(String source, ParsePosition parse_pos) 
                  throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("parsing is not supported");
    }


    /**
     * Gets the date time interval patterns.
     * @return a copy of the date time interval patterns associated with
     * this date interval formatter.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public DateIntervalInfo getDateIntervalInfo()
    {
        return (DateIntervalInfo)fInfo.clone();
    }


    /**
     * Set the date time interval patterns. 
     * @param newItvPattern   the given interval patterns to copy.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public void setDateIntervalInfo(DateIntervalInfo newItvPattern)
    {
        // clone it. If it is frozen, the clone returns itself.
        // Otherwise, clone returns a copy
        fInfo = (DateIntervalInfo)newItvPattern.clone();
        fInfo.freeze(); // freeze it
        LOCAL_PATTERN_CACHE.clear();
        if ( fDateFormat != null ) {
            initializePattern();
        }
    }


    /**
     * Gets the date formatter
     * @return a copy of the date formatter associated with
     * this date interval formatter.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public DateFormat getDateFormat()
    {
        return (DateFormat)fDateFormat.clone();
    }


    /**
     * Set the date formatter.
     * @param newDateFormat   the given date formatter to copy.
     *                        caller needs to make sure that
     *                        it is a SimpleDateFormatter.
     * @throws IllegalArgumentException  if the passing in DateFormat is not
     *                                   a simpel date formatter
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public void setDateFormat(DateFormat newDateFormat) 
                throws IllegalArgumentException
    {
        if ( newDateFormat instanceof SimpleDateFormat ) {
            fDateFormat = (SimpleDateFormat) newDateFormat;
            fFromCalendar = (Calendar) fDateFormat.getCalendar().clone();
            fToCalendar = (Calendar) fDateFormat.getCalendar().clone();
            fSkeleton = null;
            if ( fInfo != null ) {
                initializePattern();
            }
        } else {
            throw new IllegalArgumentException("Can not setDateFormat using non SimpleDateFormat");
        }
    }



    /**
     *  Below are for generating interval patterns locale to the formatter 
     */

    /**
     * Initialize interval patterns locale to this formatter.
     * @draft ICU 4.0
     */
    private void initializePattern() { 
        String fullPattern = ((SimpleDateFormat)fDateFormat).toPattern();
        ULocale locale = ((SimpleDateFormat)fDateFormat).getLocale();
        String key;
        if ( fSkeleton != null ) {
            key = locale.toString() + "+" + fullPattern + "+" + fSkeleton;
        } else {
            key = locale.toString() + "+" + fullPattern;
        }
        Map patterns = (Map) LOCAL_PATTERN_CACHE.get(key);
        if ( patterns == null ) {
            HashMap intervalPatterns = initializeIntervalPattern(fullPattern, locale);
            patterns = Collections.unmodifiableMap(intervalPatterns);
            LOCAL_PATTERN_CACHE.put(key, patterns);
        } 
        fIntervalPatterns = patterns;
    }



    /** 
     * Initialize interval patterns locale to this formatter
     * 
     * This code is a bit complicated since 
     * 1. the interval patterns saved in resource bundle files are interval
     *    patterns based on date or time only.
     *    It does not have interval patterns based on both date and time.
     *    Interval patterns on both date and time are algorithm generated.
     *
     *    For example, it has interval patterns on skeleton "dMy" and "hm",
     *    but it does not have interval patterns on skeleton "dMyhm".
     *    
     *    The rule to genearte interval patterns for both date and time skeleton are
     *    1) when the year, month, or day differs, concatenate the two original 
     *    expressions with a separator between, 
     *    For example, interval pattern from "Jan 10, 2007 10:10 am" 
     *    to "Jan 11, 2007 10:10am" is 
     *    "Jan 10, 2007 10:10 am - Jan 11, 2007 10:10am" 
     *
     *    2) otherwise, present the date followed by the range expression 
     *    for the time.
     *    For example, interval pattern from "Jan 10, 2007 10:10 am" 
     *    to "Jan 10, 2007 11:10am" is 
     *    "Jan 10, 2007 10:10 am - 11:10am" 
     *
     * 2. even a pattern does not request a certion calendar field,
     *    the interval pattern needs to include such field if such fields are
     *    different between 2 dates.
     *    For example, a pattern/skeleton is "hm", but the interval pattern 
     *    includes year, month, and date when year, month, and date differs.
     * 
     *
     * @param fullPattern  formatter's full pattern
     * @param locale       the given locale.
     * @return             interval patterns' hash map
     * @draft ICU 4.0 
     */
    private HashMap initializeIntervalPattern(String fullPattern, ULocale locale) {
        DateTimePatternGenerator dtpng = DateTimePatternGenerator.getInstance(locale);
        if ( fSkeleton == null ) {
            // fSkeleton is already set by getDateIntervalInstance()
            // or by getInstance(String skeleton, .... )
            fSkeleton = dtpng.getSkeleton(fullPattern);
        }
        String skeleton = fSkeleton;

        HashMap intervalPatterns = new HashMap();

        /* Check whether the skeleton is a combination of date and time.
         * For the complication reason 1 explained above.
         */
        StringBuffer date = new StringBuffer(skeleton.length());
        StringBuffer normalizedDate = new StringBuffer(skeleton.length());
        StringBuffer time = new StringBuffer(skeleton.length());
        StringBuffer normalizedTime = new StringBuffer(skeleton.length());

        /* the difference between time skeleton and normalizedTimeSkeleton are:
         * 1. both 'H' and 'h' are normalized as 'h' in normalized time skeleton,
         * 2. 'a' is omitted in normalized time skeleton.
         * 3. there is only one appearance for 'h', 'm','v', 'z' in normalized 
         *    time skeleton
         *
         * The difference between date skeleton and normalizedDateSkeleton are:
         * 1. both 'y' and 'd' appear only once in normalizeDateSkeleton
         * 2. 'E' and 'EE' are normalized into 'EEE'
         * 3. 'MM' is normalized into 'M'
         */
        getDateTimeSkeleton(skeleton, date, normalizedDate,
                            time, normalizedTime);

        String dateSkeleton = date.toString();
        String timeSkeleton = time.toString();
        String normalizedDateSkeleton = normalizedDate.toString();
        String normalizedTimeSkeleton = normalizedTime.toString();

        boolean found = genSeparateDateTimePtn(normalizedDateSkeleton, 
                                               normalizedTimeSkeleton,
                                               intervalPatterns);

        if ( found == false ) {
            // use fallback
            // TODO: if user asks "m", but "d" differ
            //StringBuffer skeleton = new StringBuffer(skeleton);
            if ( time.length() != 0 ) {
                //genFallbackForNotFound(Calendar.MINUTE, skeleton, dtpng);
                //genFallbackForNotFound(Calendar.HOUR, skeleton, dtpng);
                //genFallbackForNotFound(Calendar.AM_PM, skeleton, dtpng);
                if ( date.length() == 0 ) {
                    // prefix with yMd
                    timeSkeleton = YEAR_MONTH_DAY_SHORT_FORMAT + timeSkeleton;
                    String pattern =dtpng.getBestPattern(timeSkeleton);
                    // for fall back interval patterns,
                    // the first part of the pattern is empty,
                    // the second part of the pattern is the full-pattern
                    // should be used in fall-back.
                    DateIntervalInfo.PatternInfo ptn = 
                        new DateIntervalInfo.PatternInfo(null, pattern,
                                                     fInfo.getDefaultOrder());
                    intervalPatterns.put(DateIntervalInfo.
                        CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.DATE], ptn);
                    // share interval pattern
                    intervalPatterns.put(DateIntervalInfo.
                        CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.MONTH], ptn);
                    // share interval pattern
                    intervalPatterns.put(DateIntervalInfo.
                        CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.YEAR], ptn);
                } else {
                    //genFallbackForNotFound(Calendar.DATE, skeleton, dtpng);
                    //genFallbackForNotFound(Calendar.MONTH, skeleton, dtpng);
                    //genFallbackForNotFound(Calendar.YEAR, skeleton, dtpng);
                }
            } else {
                    //genFallbackForNotFound(Calendar.DATE, skeleton, dtpng);
                    //genFallbackForNotFound(Calendar.MONTH, skeleton, dtpng);
                    //genFallbackForNotFound(Calendar.YEAR, skeleton, dtpng);
            }
            return intervalPatterns;
        } // end of skeleton not found
        // interval patterns for skeleton are found in resource 
        if ( time.length() == 0 ) {
            // done
        } else if ( date.length() == 0 ) {
            // need to set up patterns for y/M/d differ
            /* result from following looks confusing.
             * for example: 10 10:10 - 11 10:10, it is not
             * clear that the first 10 is the 10th day
            time.insert(0, 'd');
            genFallbackPattern(Calendar.DATE, time, dtpng);
            time.insert(0, 'M');
            genFallbackPattern(Calendar.MONTH, time, dtpng);
            time.insert(0, 'y');
            genFallbackPattern(Calendar.YEAR, time, dtpng);
            */
            // prefix with yMd
            timeSkeleton = YEAR_MONTH_DAY_SHORT_FORMAT + timeSkeleton;
            String pattern =dtpng.getBestPattern(timeSkeleton);
            // for fall back interval patterns,
            // the first part of the pattern is empty,
            // the second part of the pattern is the full-pattern
            // should be used in fall-back.
            DateIntervalInfo.PatternInfo ptn = new DateIntervalInfo.PatternInfo(
                                    null, pattern, fInfo.getDefaultOrder());
            intervalPatterns.put(DateIntervalInfo.
                CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.DATE], ptn);
            intervalPatterns.put(DateIntervalInfo.
                CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.MONTH], ptn);
            intervalPatterns.put(DateIntervalInfo.
                CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.YEAR], ptn);
        } else {
            /* if both present,
             * 1) when the year, month, or day differs, 
             * concatenate the two original expressions with a separator between, 
             * 2) otherwise, present the date followed by the 
             * range expression for the time. 
             */
            /*
             * 1) when the year, month, or day differs, 
             * concatenate the two original expressions with a separator between, 
             */
            // if field exists, use fall back
            if ( !fieldExistsInSkeleton(Calendar.DATE, dateSkeleton) ) {
                // prefix skeleton with 'd'
                skeleton = DateIntervalInfo.
                    CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.DATE] + skeleton;
                genFallbackPattern(Calendar.DATE, skeleton, dtpng, intervalPatterns);
            }
            if ( !fieldExistsInSkeleton(Calendar.MONTH, dateSkeleton) ) {
                // then prefix skeleton with 'M'
                skeleton = DateIntervalInfo.
                    CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.MONTH] + skeleton;
                genFallbackPattern(Calendar.MONTH, skeleton, dtpng, intervalPatterns);
            }
            if ( !fieldExistsInSkeleton(Calendar.YEAR, dateSkeleton) ) {
                // then prefix skeleton with 'y'
                skeleton = DateIntervalInfo.
                    CALENDAR_FIELD_TO_PATTERN_LETTER[Calendar.YEAR] + skeleton;
                genFallbackPattern(Calendar.YEAR, skeleton, dtpng, intervalPatterns);
            }
            
            /*
             * 2) otherwise, present the date followed by the 
             * range expression for the time. 
             */
            // Need the Date/Time pattern for concatnation the date with
            // the time interval.
            // The date/time pattern ( such as {0} {1} ) is saved in
            // calendar, that is why need to get the CalendarData here.
            CalendarData calData = new CalendarData(locale, null);
            String[] patterns = calData.get("DateTimePatterns").getStringArray();
            String datePattern =dtpng.getBestPattern(dateSkeleton);
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.AM_PM, intervalPatterns);
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.HOUR, intervalPatterns);
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.MINUTE, intervalPatterns);
        }

        return intervalPatterns;
    }


    /**
     * Generate fall back interval pattern given a calendar field,
     * a skeleton, and a date time pattern generator
     * @param field      the largest different calendar field
     * @param skeleton   a skeleton
     * @param dtpng      date time pattern generator
     * @param intervalPatterns interval patterns
     * @draft ICU 4.0 
     */
    private void genFallbackPattern(int field, String skeleton,
                                    DateTimePatternGenerator dtpng,
                                    HashMap intervalPatterns) {
        String pattern =dtpng.getBestPattern(skeleton);
        // for fall back interval patterns,
        // the first part of the pattern is empty,
        // the second part of the pattern is the full-pattern
        // should be used in fall-back.
        DateIntervalInfo.PatternInfo ptn = new DateIntervalInfo.PatternInfo(
                                    null, pattern, fInfo.getDefaultOrder());
        intervalPatterns.put( 
            DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], ptn);
    }



    /*
    private void genFallbackForNotFound(String field, StringBuffer skeleton,
                                        DateTimePatternGenerator dtpng) {
        if ( SimpleDateFormat.isFieldUnitIgnored(skeleton.toString(), field) ) {
            // single date
            DateIntervalInfo.PatternInfo ptnInfo = 
                new DateIntervalInfo.PatternInfo(null, fDateFormat.toPattern(),
                                                 fInfo.getDefaultOrder());
            fIntervalPatterns.put(field, ptnInfo);
            return;
        } else if ( skeleton.indexOf(field) == -1 ) {
            skeleton.insert(0,field);
            genFallbackPattern(field, skeleton, dtpng);
        }
    }
    */

    /** 
     * get separated date and time skeleton from a combined skeleton.
     *
     * The difference between date skeleton and normalizedDateSkeleton are:
     * 1. both 'y' and 'd' are appeared only once in normalizeDateSkeleton
     * 2. 'E' and 'EE' are normalized into 'EEE'
     * 3. 'MM' is normalized into 'M'
     *
     ** the difference between time skeleton and normalizedTimeSkeleton are:
     * 1. both 'H' and 'h' are normalized as 'h' in normalized time skeleton,
     * 2. 'a' is omitted in normalized time skeleton.
     * 3. there is only one appearance for 'h', 'm','v', 'z' in normalized time
     *    skeleton
     *
     *
     *  @param skeleton               given combined skeleton.
     *  @param date                   Output parameter for date only skeleton.
     *  @param normalizedDate         Output parameter for normalized date only
     *
     *  @param time                   Output parameter for time only skeleton.
     *  @param normalizedTime         Output parameter for normalized time only
     *                                skeleton.
     *
     * @draft ICU 4.0 
     */
    private static void getDateTimeSkeleton(String skeleton,
                                            StringBuffer dateSkeleton,
                                            StringBuffer normalizedDateSkeleton,
                                            StringBuffer timeSkeleton,
                                            StringBuffer normalizedTimeSkeleton)
    {
        // dateSkeleton follows the sequence of y*M*E*d*
        // timeSkeleton follows the sequence of hm*[v|z]?
        int i;
        int ECount = 0;
        int dCount = 0;
        int MCount = 0;
        int yCount = 0;
        int hCount = 0;
        int mCount = 0;
        int vCount = 0;
        int zCount = 0;
    
        for (i = 0; i < skeleton.length(); ++i) {
            char ch = skeleton.charAt(i);
            switch ( ch ) {
              case 'E':
                dateSkeleton.append(ch);
                ++ECount;
                break;
              case 'd':
                dateSkeleton.append(ch);
                ++dCount;
                break;
              case 'M':
                dateSkeleton.append(ch);
                ++MCount;
                break;
              case 'y':
                dateSkeleton.append(ch);
                ++yCount;
                break;
              case 'G':
              case 'Y':
              case 'u':
              case 'Q':
              case 'q':
              case 'L':
              case 'l':
              case 'W':
              case 'w':
              case 'D':
              case 'F':
              case 'g':
              case 'e':
              case 'c':
                normalizedDateSkeleton.append(ch);
                dateSkeleton.append(ch);
                break;
              case 'a':
                // 'a' is implicitly handled 
                timeSkeleton.append(ch);
                break;
              case 'h':
              case 'H':
                timeSkeleton.append(ch);
                ++hCount;
                break;
              case 'm':
                timeSkeleton.append(ch);
                ++mCount;
                break;
              case 'z':
                ++zCount;
                timeSkeleton.append(ch);
                break;
              case 'v':
                ++vCount;
                timeSkeleton.append(ch);
                break;
              // FIXME: what is the difference between CAP_V/Z and LOW_V/Z
              case 'V':
              case 'Z':
              case 'k':
              case 'K':
              case 'j':
              case 's':
              case 'S':
              case 'A':
                timeSkeleton.append(ch);
                normalizedTimeSkeleton.append(ch);
                break;     
            }
        }
    
        /* generate normalized form for date*/
        if ( yCount != 0 ) {
            normalizedDateSkeleton.append('y');
        }
        if ( MCount != 0 ) {
            if ( MCount < 3 ) {
                normalizedDateSkeleton.append('M');
            } else {
                for ( i = 0; i < MCount && i < 5; ++i ) {
                     normalizedDateSkeleton.append('M');
                }
            }
        }
        if ( ECount != 0 ) {
            if ( ECount <= 3 ) {
                normalizedDateSkeleton.append('E');
                normalizedDateSkeleton.append('E');
                normalizedDateSkeleton.append('E');
            } else {
                for ( i = 0; i < ECount && i < 5; ++i ) {
                     normalizedDateSkeleton.append('E');
                }
            }
        }
        if ( dCount != 0 ) {
            normalizedDateSkeleton.append('d');
        }
    
        /* generate normalized form for time */
        if ( hCount != 0 ) {
            normalizedTimeSkeleton.append('h');
        }
        if ( mCount != 0 ) {
            normalizedTimeSkeleton.append('m');
        }
        if ( zCount != 0 ) {
            normalizedTimeSkeleton.append('z');
        }
        if ( vCount != 0 ) {
            normalizedTimeSkeleton.append('v');
        }
    }



    /**
     * Generate date or time interval pattern from resource.
     *
     * It needs to handle the following: 
     * 1. need to adjust field width.
     *    For example, the interval patterns saved in DateIntervalInfo
     *    includes "dMMMy", but not "dMMMMy".
     *    Need to get interval patterns for dMMMMy from dMMMy.
     *    Another example, the interval patterns saved in DateIntervalInfo
     *    includes "hmv", but not "hmz".
     *    Need to get interval patterns for "hmz' from 'hmv'
     *
     * 2. there might be no pattern for 'y' differ for skeleton "Md",
     *    in order to get interval patterns for 'y' differ,
     *    need to look for it from skeleton 'yMd'
     *
     * @param dateSkeleton   normalized date skeleton
     * @param timeSkeleton   normalized time skeleton
     * @param intervalPatterns interval patterns
     * @return whether there is interval patterns for the skeleton.
     *         true if there is, false otherwise
     * @draft ICU 4.0
     */

    private boolean genSeparateDateTimePtn(String dateSkeleton, 
                                           String timeSkeleton,
                                           HashMap intervalPatterns)
    {
        String skeleton;
        // if both date and time skeleton present,
        // the final interval pattern might include time interval patterns
        // ( when, am_pm, hour, minute differ ),
        // but not date interval patterns ( when year, month, day differ ).
        // For year/month/day differ, it falls back to fall-back pattern.
        if ( timeSkeleton.length() != 0  ) {
            skeleton = timeSkeleton;
        } else {
            skeleton = dateSkeleton;
        }

        /* interval patterns for skeleton "dMMMy" (but not "dMMMMy") 
         * are defined in resource,
         * interval patterns for skeleton "dMMMMy" are calculated by
         * 1. get the best match skeleton for "dMMMMy", which is "dMMMy"
         * 2. get the interval patterns for "dMMMy",
         * 3. extend "MMM" to "MMMM" in above interval patterns for "dMMMMy" 
         * getBestSkeleton() is step 1.
         */
        // best skeleton, and the difference information
        BestMatchInfo retValue = fInfo.getBestSkeleton(skeleton);
        String bestSkeleton = retValue.bestMatchSkeleton;
        int differenceInfo =  retValue.bestMatchDistanceInfo;
   
        // difference:
        // 0 means the best matched skeleton is the same as input skeleton
        // 1 means the fields are the same, but field width are different
        // 2 means the only difference between fields are v/z,
        // -1 means there are other fields difference 
        if ( differenceInfo == -1 ) { 
            // skeleton has different fields, not only  v/z difference
            return false;
        }

        if ( timeSkeleton.length() == 0 ) {
            // only has date skeleton
            genIntervalPattern(Calendar.DATE, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            SkeletonAndItsBestMatch skeletons = genIntervalPattern(
                                                  Calendar.MONTH, skeleton, 
                                                  bestSkeleton, differenceInfo,
                                                  intervalPatterns);
            if ( skeletons != null ) {
                bestSkeleton = skeletons.skeleton;
                skeleton = skeletons.bestMatchSkeleton;
            }
            genIntervalPattern(Calendar.YEAR, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
        } else {
            genIntervalPattern(Calendar.MINUTE, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            genIntervalPattern(Calendar.HOUR, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
            genIntervalPattern(Calendar.AM_PM, skeleton, bestSkeleton, differenceInfo, intervalPatterns);
        }
        return true;

    }



    /**
     * Generate interval pattern from existing resource
     *
     * It not only save the interval patterns,
     * but also return the skeleton and its best match skeleton.
     *
     * @param field           largest different calendar field
     * @param skeleton        skeleton
     * @param bestSkeleton    the best match skeleton which has interval pattern
     *                        defined in resource
     * @param differenceInfo  the difference between skeleton and best skeleton
     *         0 means the best matched skeleton is the same as input skeleton
     *         1 means the fields are the same, but field width are different
     *         2 means the only difference between fields are v/z,
     *        -1 means there are other fields difference 
     *
     * @param intervalPatterns interval patterns
     *
     * @return  an extended skeleton or extended best skeleton if applicable.
     *          null otherwise.
     * @draft ICU 4.0
     */
    private SkeletonAndItsBestMatch genIntervalPattern(
                   int field, String skeleton, String bestSkeleton, 
                   int differenceInfo, HashMap intervalPatterns) {
        SkeletonAndItsBestMatch retValue = null;
        DateIntervalInfo.PatternInfo pattern = fInfo.getIntervalPattern(
                                           bestSkeleton, field);
        if ( pattern == null ) {
            // single date
            if ( SimpleDateFormat.isFieldUnitIgnored(bestSkeleton, field) ) {
                DateIntervalInfo.PatternInfo ptnInfo = 
                    new DateIntervalInfo.PatternInfo(fDateFormat.toPattern(),
                                                     null, 
                                                     fInfo.getDefaultOrder());
                intervalPatterns.put(DateIntervalInfo.
                    CALENDAR_FIELD_TO_PATTERN_LETTER[field], ptnInfo);
                return null;
            }

            // for 24 hour system, interval patterns in resource file
            // might not include pattern when am_pm differ, 
            // which should be the same as hour differ.
            // add it here for simplicity
            if ( field == Calendar.AM_PM ) {
                 pattern = fInfo.getIntervalPattern(bestSkeleton, 
                                                         Calendar.HOUR);
                 if ( pattern != null ) {
                      // share
                      intervalPatterns.put(DateIntervalInfo.
                          CALENDAR_FIELD_TO_PATTERN_LETTER[field], 
                          pattern);
                 }
                 return null;
            } 
            // else, looking for pattern when 'y' differ for 'dMMMM' skeleton,
            // first, get best match pattern "MMMd",
            // since there is no pattern for 'y' differs for skeleton 'MMMd',
            // need to look for it from skeleton 'yMMMd',
            // if found, adjust field width in interval pattern from
            // "MMM" to "MMMM".
            String fieldLetter = 
                DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field];
            bestSkeleton = fieldLetter + bestSkeleton;
            skeleton = fieldLetter + skeleton;
            pattern = fInfo.getIntervalPattern(bestSkeleton, field);
            if ( pattern != null ) {
                retValue = new SkeletonAndItsBestMatch(skeleton, bestSkeleton);
            }
        } 
        if ( pattern != null ) {
            if ( differenceInfo != 0 ) {
                String part1 = adjustFieldWidth(skeleton, bestSkeleton, 
                                   pattern.getFirstPart(), differenceInfo);
                String part2 = adjustFieldWidth(skeleton, bestSkeleton, 
                                   pattern.getSecondPart(), differenceInfo);
                pattern =  new DateIntervalInfo.PatternInfo(part1, part2, 
                                           pattern.firstDateInPtnIsLaterDate());
            } else {
                // pattern is immutable, no need to clone; 
                // pattern = (DateIntervalInfo.PatternInfo)pattern.clone();
            }
            intervalPatterns.put(
              DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], pattern);
        }
        return retValue;
    }




    /**
     * Adjust field width in best match interval pattern to match
     * the field width in input skeleton.
     *
     * TODO (xji) make a general solution
     * The adjusting rule can be:
     * 1. always adjust
     * 2. never adjust
     * 3. default adjust, which means adjust according to the following rules
     * 3.1 always adjust string, such as MMM and MMMM
     * 3.2 never adjust between string and numeric, such as MM and MMM
     * 3.3 always adjust year
     * 3.4 do not adjust 'd', 'h', or 'm' if h presents
     * 3.5 do not adjust 'M' if it is numeric(?)
     *
     * Since date interval format is well-formed format,
     * date and time skeletons are normalized previously,
     * till this stage, the adjust here is only "adjust strings, such as MMM
     * and MMMM, EEE and EEEE.
     *
     * @param inputSkeleton            the input skeleton
     * @param bestMatchSkeleton        the best match skeleton
     * @param bestMatchIntervalpattern the best match interval pattern
     * @param differenceInfo           the difference between 2 skeletons
     *                                 1 means only field width differs
     *                                 2 means v/z exchange
     * @return the adjusted interval pattern
     * @draft ICU 4.0
     */
    private static String adjustFieldWidth(String inputSkeleton,
                                    String bestMatchSkeleton,
                                    String bestMatchIntervalPattern,
                                    int differenceInfo ) {
        
        if ( bestMatchIntervalPattern == null ) {
            return null; // the 2nd part could be null
        }
        int[] inputSkeletonFieldWidth = new int[58];
        int[] bestMatchSkeletonFieldWidth = new int[58];

        /* initialize as following
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
        */


        DateIntervalInfo.parseSkeleton(inputSkeleton, inputSkeletonFieldWidth);
        DateIntervalInfo.parseSkeleton(bestMatchSkeleton, bestMatchSkeletonFieldWidth);
        if ( differenceInfo == 2 ) {
            bestMatchIntervalPattern = bestMatchIntervalPattern.replace('v', 'z');
        }

        StringBuffer adjustedPtn = new StringBuffer(bestMatchIntervalPattern);

        boolean inQuote = false;
        char prevCh = 0;
        int count = 0;
    
        int PATTERN_CHAR_BASE = 0x41;
        
        // loop through the pattern string character by character 
        int adjustedPtnLength = adjustedPtn.length();
        for (int i = 0; i < adjustedPtnLength; ++i) {
            char ch = adjustedPtn.charAt(i);
            if (ch != prevCh && count > 0) {
                // check the repeativeness of pattern letter
                char skeletonChar = prevCh;
                if ( skeletonChar == 'L' ) {
                    // for skeleton "M+", the pattern is "...L..." 
                    skeletonChar = 'M';
                }
                int fieldCount = bestMatchSkeletonFieldWidth[(int)(skeletonChar - PATTERN_CHAR_BASE)];
                int inputFieldCount = inputSkeletonFieldWidth[(int)(skeletonChar - PATTERN_CHAR_BASE)];
                if ( fieldCount == count && inputFieldCount > fieldCount ) {
                    count = inputFieldCount - fieldCount;
                    for ( int j = 0; j < count; ++j ) {
                        adjustedPtn.insert(i, prevCh);    
                    }                    
                    i += count;
                    adjustedPtnLength += count;
                }
                count = 0;
            }
            if (ch == '\'') {
                // Consecutive single quotes are a single quote literal,
                // either outside of quotes or between quotes
                if ((i+1) < adjustedPtn.length() && adjustedPtn.charAt(i+1) == '\'') {
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
        if ( count > 0 ) {
            // last item
            // check the repeativeness of pattern letter
            char skeletonChar = prevCh;
            if ( skeletonChar == 'L' ) {
                // for skeleton "M+", the pattern is "...L..." 
                skeletonChar = 'M';
            }
            int fieldCount = bestMatchSkeletonFieldWidth[(int)(skeletonChar - PATTERN_CHAR_BASE)];
            int inputFieldCount = inputSkeletonFieldWidth[(int)(skeletonChar - PATTERN_CHAR_BASE)];
            if ( fieldCount == count && inputFieldCount > fieldCount ) {
                count = inputFieldCount - fieldCount;
                for ( int j = 0; j < count; ++j ) {
                    adjustedPtn.append(prevCh);    
                }                    
            }
        }
        return adjustedPtn.toString();
    }


    /**
     * Concat a single date pattern with a time interval pattern,
     * set it into the intervalPatterns, while field is time field.
     * This is used to handle time interval patterns on skeleton with
     * both time and date. Present the date followed by 
     * the range expression for the time.
     * @param dtfmt                  date and time format
     * @param datePattern            date pattern
     * @param field                  time calendar field: AM_PM, HOUR, MINUTE
     * @param intervalPatterns       interval patterns
     * @draft ICU 4.0 
     */
    private void concatSingleDate2TimeInterval(String dtfmt,
                                               String datePattern,
                                               int field,
                                               HashMap intervalPatterns)
    {

        DateIntervalInfo.PatternInfo  timeItvPtnInfo = 
          (DateIntervalInfo.PatternInfo)intervalPatterns.get(
              DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field]);
        if ( timeItvPtnInfo != null ) {
            String timeIntervalPattern = timeItvPtnInfo.getFirstPart() + 
                                         timeItvPtnInfo.getSecondPart();
            String pattern = MessageFormat.format(dtfmt, new Object[] 
                                         {timeIntervalPattern, datePattern});
            timeItvPtnInfo = DateIntervalInfo.genPatternInfo(pattern,
                                timeItvPtnInfo.firstDateInPtnIsLaterDate());
            intervalPatterns.put(
              DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field], timeItvPtnInfo);
        } 
        // else: fall back
        // it should not happen if the interval format defined is valid
    }


    /**
     * check whether a calendar field present in a skeleton.
     * @param field      calendar field need to check
     * @param skeleton   given skeleton on which to check the calendar field
     * @return           true if field present in a skeleton.
     * @draft ICU 4.0 
     */
    private static boolean fieldExistsInSkeleton(int field, String skeleton)
    {
        String fieldChar = DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[field];
        return ( (skeleton.indexOf(fieldChar) == -1) ? false : true ) ;
    }


    /**
     * readObject.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializePattern();
    }
}
//#endif
