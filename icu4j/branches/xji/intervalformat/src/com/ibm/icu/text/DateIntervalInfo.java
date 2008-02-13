//##header J2SE15
/*
*   Copyright (C) 2008, International Business Machines
*   Corporation and others.  All Rights Reserved.
*/

package com.ibm.icu.text;

import java.util.MissingResourceException;

import com.ibm.icu.impl.CalendarData;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

/**
 * <code>DateIntervalInfo</code> is a public class for encapsulating localizable
 * date time interval patterns. It is used by <code>DateIntervalFormat</code>.
 *
 * <P>
 * Logically, the interval patterns are mappings 
 * from (skeleton, the_largest_different_calendar_field)  
 * to (date_interval_pattern).
 * <P>
 * A skeleton just includes the pattern letter and lengths,
 * without the punctuations and string literals in a pattern.
 * For example, the skeleton for pattern "d 'on' MMM" is
 * "dMMM".
 *
 * FIXME: more documents on skeleton
 *
 * <P>
 * For example, for a skeleton "dMMMy" in  en_US, if the largest different 
 * calendar field between date1 and date2 is "year", the date interval pattern 
 * is "MMM d, yyyy - MMM d, yyyy", such as "Jan 10, 2007 - Jan 10, 2008".
 * <P>
 * If the largest different calendar field between date1 and date2 is "month", 
 * the date interval pattern is "MMM d - MMM d, yyyy", 
 * such as "Jan 10 - Feb 10, 2007".
 * <P>
 * If the largest different calendar field between date1 and date2 is "day", 
 * the date interval pattern is ""MMM d-d, yyyy", such as "Jan 10-20, 2007".
 * <P>
 * If all the available fields have the exact same value, it generates a single 
 * date string. For example, if the interval skeleton is "dMMMMy" ( with only
 * day, month, and year), the interval pattern from "Jan 10, 2007" to 
 * "Jan 10, 2007" is "Jan 10, 2007".
 * <P>
 * For a skeleton "MMMy", if the largest different calendar field between date1
 * and date2 is "month". the interval pattern is "MMM-MMM, yyyy", 
 * such as "Jan-Feb, 2007".
 *
 * <P>
 * The recommendated way to create a <code>DateIntervalFormat</code> object is 
 * <pre>
 * DateIntervalFormat::getInstance(DateFormat*, ULocale&).
 * </pre>
 * By using a ULocale parameter, the <code>DateIntervalFormat</code> object is 
 * initialized with the default interval patterns for a given or default locale.
 * <P>
 * If clients decided to create <code>DateIntervalFormat</code> object 
 * by supplying their own interval patterns, they can do so with 
 * <pre>
 * DateIntervalFormat::getInstance(DateFormat*, DateIntervalInfo*).
 * </pre>
 * Here, <code>DateIntervalFormat</code> object is initialized with the interval * patterns client supplied. It provides flexibility for powerful usage.
 *
 * <P>
 * After a <code>DateIntervalInfo</code> object is created, clients may modify
 * the interval patterns using setIntervalPatterns function as so desired.
 * Currently, the mininum supported calendar field on which the client can
 * set interval pattern is UCAL_MINUTE.
 * <P>
 * <code>DateIntervalInfo</code> objects are clonable. 
 * When clients obtain a <code>DateIntervalInfo</code> object, 
 * they can feel free to modify it as necessary.
 * <P>
 * <code>DateIntervalInfo</code> are not expected to be subclassed. 
 * Data for a calendar is loaded out of resource bundles. 
 * To ICU 4.0, date interval patterns are only supported in Gregorian calendar. 
 * @draft ICU 4.0
**/

public class DateIntervalInfo implements Cloneable  {
    private static final char[] CALENDAR_FIELD_TO_PATTERN_LETTER = 
    {
        'G', 'y', 'M',
        'w', 'W', 'd', 
        'D', 'E', 'F',
        'a', 'h', 'H',
        'm',
    };

    // the mininum different calendar field is UCAL_MINUTE
    private static final int MINIMUM_SUPPORTED_CALENDAR_FIELD = Calendar.MINUTE;
    // the skeleton on which the interval patterns based.
    private String fSkeleton;

    //private String fIntervalPatterns[MINIMUM_SUPPORTED_CALENDAR_FIELD+1];
    private String[] fIntervalPatterns;

    // default interval pattern on the skeleton, {0} - {1}
    private String fFallbackIntervalPattern;

    // fall-back interval pattern, {0} - {1}, which is not skeleton dependent.
    // {0} - {1}, where {0} and {1} are SHORT date format
    private String fDateFallbackIntervalPattern;
    // {0} - {1}, where {0} and {1} are SHORT time format
    private String fTimeFallbackIntervalPattern;
    // {0} - {1}, where {0} and {1} are SHORT date/time format
    private String fDateTimeFallbackIntervalPattern;
    
    /**
     * Whether the first date in interval pattern is later date or not.
     * By default, the first part of the pattern in the interval pattern is 
     * earlier date, for example, "d MMM" in "d MMM - d MMM yyy".
     * If an interval pattern is prefixed with "later_first:", for example
     * "later_first:d MMM - d MMM yyyy", it means the first part of the pattern
     * is later date. 
     * For example, given 2 date, Jan 10, 2007 to Feb 10, 2007.
     * If the pattern is "d MMM - d MMM yyyy", the interval format is
     * "10 Jan - 10 Feb, 2007".
     * If the pattern is "later_first:d MMM - d MMM yyyy", the interval format
     * is "10 Feb - 10 Jan, 2007"
     */
    private boolean  fFirstDateInPtnIsLaterDate;


    /**
     * Defaul constructor.
     * It does not initialize any interval patterns.
     * It should be followed by setIntervalPattern(), 
     * and is recommended to be used only for powerful users.
     * @internal ICU 4.0
     */
    public DateIntervalInfo()
    {
        fFirstDateInPtnIsLaterDate = false;
        fIntervalPatterns = new String[MINIMUM_SUPPORTED_CALENDAR_FIELD+1];
    }


    /** 
     * Construct <code>DateIntervalInfo</code> for the given local,
     * Gregorian calendar, and date/time skeleton "dMyhm".
     * @param locale  the interval patterns are loaded from the Gregorian 
     *                calendar data in this locale.
     * @draft ICU 4.0
     */
    public DateIntervalInfo(ULocale locale) 
    {
        init(locale, "dMyhm");
    }


    /** 
     * Construct <code>DateIntervalInfo</code> for the given local, 
     * Gregorian calendar and skeleton.
     * @param locale    the interval patterns are loaded from the 
     *                  Gregorian calendar data in this locale.
     * @param skeleton  the interval patterns are loaded from this skeleton
     *                  in the calendar data.
     * @draft ICU 4.0
     */
    public DateIntervalInfo(ULocale locale, String skeleton)
    {
        init(locale, skeleton);
    }


    /** 
     * init the DateIntervalInfo from locale and skeleton.
     * @param locale   the given locale.
     * @param skeleton the given skeleton on which the interval patterns based.
     * @draft ICU 4.0 
     *
     * It mainly init the following fields: fIntervalPattern[];
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
     * The process is:
     * 1. calculate fFallbackIntervalPattern, which is defined in resource file and
     *    mostly {0} - {1}. The single date/time pattern is the pattern based on 
     *    skeleton. Or could use the  fPattern in SimpleDateFormat.
     *    For example, for en_US, skeleton "dMMM", single date pattern 
     *    is "MMM d", fall back interval pattern will be "MMM d - MMM d".
     *
     * 2. calculate other 3 fallback patterns. 
     *    fDateFallbackIntervalPattern - use SHORT date format as single date format
     *    fTimeFallbackIntervalPattern - use SHORT time format as single time format
     *    fDateTimeFallbackIntervalPattern - use SHORT date and time format as
     *                                       single date format
     *
     * 3. fill Interval Patterns for year/month/day/am_pm/hour/minute differs.
     *
     *    check whether skeleton found
     * 3.1 No
     *     if the largest different calendar field on which the interval pattern
     *     based on exists in the skeleton, fallback to fFallbackIntervalPattern;
     *     elsefallback to 
     *         fDateFallbackIntervalPattern or
     *         fTimeFallbackIntervalPattern or
     *         fallback to fDateTimeFallbackIntervalPattern;
     *
     * 3.2 Yes
     *     if skeleton only contains date pattern,
     *     fill interval patterns for year/month/day differs from resource file,
     *
     *     else if skeleton only contains time pattern,
     *     fill interval patterns for am_pm/hour/minute differs from resource file,
     *     fill interval patterns for year/month/day differs as 
     *     fDateTimeFallbackIntervalPattern
     *
     *     else ( skeleton contains both date and time )
     *     fill interval patterns for year/month/day differs as
     *         fFallbackIntervalPattern if year/month/day exists in skeleton, 
     *         fDateTimeFallbackIntervalPattern if not exists
     *     fill interval patterns for am_pm/hour/minute differs as
     *         concatnation of (single date pattern) and (time interval pattern)
     */
    private void init(ULocale locale, String skeleton)
    {
        fSkeleton = skeleton;
        fFirstDateInPtnIsLaterDate = false;
        fIntervalPatterns = new String[MINIMUM_SUPPORTED_CALENDAR_FIELD+1];
    
        DateTimePatternGenerator dtpng = DateTimePatternGenerator.getInstance(locale);
    
        /* Check whether the skeleton is a combination of date and time.
         * For the complication reason 1 explained above.
         */
/*
        String dateSkeleton = new String(skeleton.length());
        String timeSkeleton = new String(skeleton.length());
        String normalizedTimeSkeleton = new String(skeleton.length());
        String normalizedDateSkeleton = new String(skeleton.length());
*/
        StringBuffer date = new StringBuffer(skeleton.length());
        StringBuffer normalizedDate = new StringBuffer(skeleton.length());
        StringBuffer time = new StringBuffer(skeleton.length());
        StringBuffer normalizedTime = new StringBuffer(skeleton.length());

        /* the difference between time skeleton and normalizedTimeSkeleton are:
         * 1. both 'H' and 'h' are normalized as 'h' in normalized time skeleton,
         * 2. 'a' is omitted in normalized time skeleton.
         * 3. there is only one appearance for 'h', 'm','v', 'z' in normalized time
         *    skeleton
         *
         * The difference between date skeleton and normalizedDateSkeleton are:
         * 1. both 'y' and 'd' are appeared only once in normalizeDateSkeleton
         * 2. 'E' and 'EE' are normalized into 'EEE'
         * 3. 'MM' is normalized into 'M'
         */
        getDateTimeSkeleton(skeleton, date, normalizedDate,
                            time, normalizedTime);
    
        String dateSkeleton = new String(date);
        String timeSkeleton = new String(time);
        String normalizedTimeSkeleton = new String(normalizedTime);
        String normalizedDateSkeleton = new String(normalizedDate);
        // key-value pair: largest_different_calendar_unit, interval_pattern
        CalendarData calData = new CalendarData(locale, null);
        String[] patterns = calData.get("DateTimePatterns").getStringArray();
   
        /* getByKey return result owned by calData.
         * It override the dateTimePatternsRes get earlier.
         * That is why we need to save above data for further process.
         */
        // FIXME: should the following be always present in resource bundle?
        //        at least in root.txt? then, the following try/catch is not
        //        neede
        
        ICUResourceBundle itvDtPtnResource = null;
        try {
            itvDtPtnResource = calData.get("IntervalDateTimePatterns");
        } catch ( MissingResourceException e) {
            // it is ok, will fallback to {date0} - {date1}
            return;
        }
    
        genFallbackPattern(dtpng, itvDtPtnResource, skeleton, patterns);
    
    
        /*
         * fill fIntervalPattern from resource file.
         * fill interval pattern for year/month/day differs for date only skeleton.
         * fill interval pattern for ampm/hour/minute differ for time only and 
         * date/time skeleton.  
         */
        boolean found = initIntvPtnFromRes(itvDtPtnResource, normalizedDateSkeleton, normalizedTimeSkeleton);
    
        if ( found == false ) {
            // skeleton not found
           String shortFormatFallback;
            // set interval patterns for year, month, date.
            if ( timeSkeleton.length() == 0 ) {
                shortFormatFallback = fDateFallbackIntervalPattern;
            } else {
                shortFormatFallback = fDateTimeFallbackIntervalPattern;
            }
    
            if ( fieldExistsInSkeleton(Calendar.YEAR, skeleton) ) {
                fIntervalPatterns[Calendar.YEAR] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.YEAR] = shortFormatFallback;
            }
            if ( fieldExistsInSkeleton(Calendar.MONTH, skeleton) ) {
                fIntervalPatterns[Calendar.MONTH] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.MONTH] = shortFormatFallback;
            }
            if ( fieldExistsInSkeleton(Calendar.DATE, skeleton) ) {
                fIntervalPatterns[Calendar.DATE] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.DATE] = shortFormatFallback;
            }
    
            if ( timeSkeleton.length() != 0 ) {
                // set interval pattern for am_pm/hour/minute.
                if ( dateSkeleton.length() == 0 ) {
                    shortFormatFallback = fTimeFallbackIntervalPattern;
                } else {
                    shortFormatFallback = fDateTimeFallbackIntervalPattern;
                }
    
                if ( fieldExistsInSkeleton(Calendar.AM_PM, timeSkeleton) ||
                     fieldExistsInSkeleton(Calendar.HOUR_OF_DAY, timeSkeleton) ) {
                    fIntervalPatterns[Calendar.AM_PM] = fFallbackIntervalPattern;
                } else {
                    fIntervalPatterns[Calendar.AM_PM] = shortFormatFallback;
                }
                if ( fieldExistsInSkeleton(Calendar.HOUR, timeSkeleton) ||
                     fieldExistsInSkeleton(Calendar.HOUR_OF_DAY, timeSkeleton) ) {
                    fIntervalPatterns[Calendar.HOUR] = fFallbackIntervalPattern;
                } else {
                    fIntervalPatterns[Calendar.HOUR] = shortFormatFallback;
                }
                if ( fieldExistsInSkeleton(Calendar.MINUTE, timeSkeleton) ) {
                    fIntervalPatterns[Calendar.MINUTE] = fFallbackIntervalPattern;
                } else {
                    fIntervalPatterns[Calendar.MINUTE] = shortFormatFallback;
                }
            }
            return; // end of skeleton not found
        } 
    
        // skeleton found
        if ( timeSkeleton.length() == 0 ) {
        } else if ( dateSkeleton.length() == 0 ) {
            // fill interval patterns for year/month/day differ 
            fIntervalPatterns[Calendar.DATE] = fDateTimeFallbackIntervalPattern;
            fIntervalPatterns[Calendar.MONTH] = fDateTimeFallbackIntervalPattern;
            fIntervalPatterns[Calendar.YEAR] = fDateTimeFallbackIntervalPattern;
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
            if ( fieldExistsInSkeleton(Calendar.YEAR, dateSkeleton) ) {
                fIntervalPatterns[Calendar.YEAR] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.YEAR] = fDateTimeFallbackIntervalPattern;
            }
            if ( fieldExistsInSkeleton(Calendar.MONTH, dateSkeleton) ) {
                fIntervalPatterns[Calendar.MONTH] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.MONTH] = fDateTimeFallbackIntervalPattern;
            }
            if ( fieldExistsInSkeleton(Calendar.DATE, dateSkeleton) ) {
                fIntervalPatterns[Calendar.DATE] = fFallbackIntervalPattern;
            } else {
                fIntervalPatterns[Calendar.DATE] = fDateTimeFallbackIntervalPattern;
            }
            
            /*
             * 2) otherwise, present the date followed by the 
             * range expression for the time. 
             */
            String datePattern =dtpng.getBestPattern(dateSkeleton.toString());
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.AM_PM);
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.HOUR);
            concatSingleDate2TimeInterval(patterns[8], datePattern, Calendar.MINUTE);
        }
    }


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
        // dateSkeleton follows the sequence of E*d*M*y*
        // timeSkeleton follows the sequence of hm*[v|z]?
        int lastIndex;
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
        if ( MCount != 0 ) {
            if ( MCount < 3 ) {
                normalizedDateSkeleton.append('M');
            } else {
                for ( i = 0; i < MCount && i < 5; ++i ) {
                     normalizedDateSkeleton.append('M');
                }
            }
        }
        if ( yCount != 0 ) {
            normalizedDateSkeleton.append('y');
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
        
/*
        date = dateSkeleton.toString();
        time = timeSkeleton.toString();
        normalizedDate = normalizedDateSkeleton.toString();
        normalizedTime = normalizedTimeSkeleton.toString();
*/
    }


    /**
     * generate the following fallback patterns:
     * fFallbackIntervalPattern;
     * fDateFallbackIntervalPattern;
     * fTimeFallbackIntervalPattern;
     * fDateTimeFallbackIntervalPattern;
     *
     * @param dtpng    the date time pattern generator,
     *                 used to generate pattern from skeleton.
     *                 NOTE: dtpng should be aant, 
     *                 but DateTimePatternGenerator::getBestPattern() 
     *                 is not afunction, 
     *                 so, can not declare dtpng asant.
     * @param itvDtPtnResource       interval date time patterns resource
     * @param skeleton               on which the fFallbackIntervalPattern based
     * @param dateTimePatterns       date and time format patterns
     * @draft ICU 4.0 
     */
    private void genFallbackPattern(DateTimePatternGenerator dtpng,
                                    ICUResourceBundle itvDtPtnResource,
                                    String skeleton,
                                    String[] dateTimePatterns)
    {
        // generate fFallbackIntervalPattern based on skeleton
        String pattern = dtpng.getBestPattern(skeleton);
        fFallbackIntervalPattern = genFallbackFromPattern(pattern, itvDtPtnResource);
    
        /* generate the other 3 fallback patterns use SHORT format
         *    fDateFallbackIntervalPattern - 
         *                use SHORT date format as single date format
         *    fTimeFallbackIntervalPattern - 
         *                use SHORT time format as single time format
         *    fDateTimeFallbackIntervalPattern - 
         *                use SHORT date and time format as
         */
    
        fDateFallbackIntervalPattern = genFallbackFromPattern(dateTimePatterns[4 + DateFormat.SHORT], itvDtPtnResource);
        fTimeFallbackIntervalPattern = genFallbackFromPattern(dateTimePatterns[DateFormat.SHORT], itvDtPtnResource);
                               
        String combinedPattern = MessageFormat.format(dateTimePatterns[8], 
                       new Object[] { dateTimePatterns[DateFormat.SHORT], 
                                      dateTimePatterns[4 + DateFormat.SHORT]});

        fDateTimeFallbackIntervalPattern = genFallbackFromPattern(combinedPattern, itvDtPtnResource);
    }


    /**
     * generate fallback {pattern} - {pattern} from pattern.
     * @param pattern           the given pattern, based on which interval 
     *                          pattern is generated.
     * @param itvDtPtnResource  interval date time pattern resource
     * @return                  the fallback interval pattern
     * @draft ICU 4.0 
     */
    private String genFallbackFromPattern(String pattern,
                                        ICUResourceBundle itvDtPtnResource)
    {
        // FIXME: right?
        String fallbackPattern = itvDtPtnResource.getStringWithFallback("Fallback");
        // check whether it is later_first
        int firstPatternIndex = fallbackPattern.indexOf("{0}");
        int secondPatternIndex = fallbackPattern.indexOf("{1}");
        if ( firstPatternIndex > secondPatternIndex ) {
            fFirstDateInPtnIsLaterDate = true;
        }
        return MessageFormat.format(fallbackPattern, new Object[] 
                                               {pattern, pattern});
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
        char fieldChar = CALENDAR_FIELD_TO_PATTERN_LETTER[field];
        return ( (skeleton.indexOf(fieldChar) == -1) ? false : true ) ;
    }


    /**
     * initialize interval pattern from resource file
     *
     * fill interval pattern for year/month/day differs for date only skeleton.
     * fill interval pattern for ampm/hour/minute differ for time only and 
     * date/time skeleton.  
     * @param intervalDateTimePtn    interval date time pattern resource
     * @param dateSkeleton           date skeleton
     * @param timeSkeleton           time skeleton
     * @param found                  whether the skeleton is found in 
     *                               IntervalDateTimePatterns in resource file
     * @draft ICU 4.0 
     */
    private boolean initIntvPtnFromRes(ICUResourceBundle intervalDateTimePtn,
                                       String dateSkeleton, 
                                       String timeSkeleton)
    {
        ICUResourceBundle intervalPatterns;
        try {
            if ( timeSkeleton.length() != 0  ) {
                intervalPatterns = intervalDateTimePtn.getWithFallback(timeSkeleton);
            } else {
                intervalPatterns = intervalDateTimePtn.getWithFallback(dateSkeleton);
            }
        } catch ( MissingResourceException e ) {
            return false;
        }
    
        boolean laterFirst = false; // assume it applies to all patterns

        for ( int index = 0; index < intervalPatterns.getSize(); ++index ) {
            String key = intervalPatterns.get(index).getKey();
            String pattern = intervalPatterns.get(index).getString();
            // FIXME: use pre-defined string literal
            int calendarField = Calendar.MILLISECONDS_IN_DAY + 1;
            if ( key.compareTo("year") == 0 ) {
                calendarField = Calendar.YEAR;    
            } else if ( key.compareTo("month") == 0 ) {
                calendarField = Calendar.MONTH;
            } else if ( key.compareTo("day") == 0 ) {
                calendarField = Calendar.DATE;
            } else if ( key.compareTo("am-pm") == 0 ) {
                calendarField = Calendar.AM_PM;    
            } else if ( key.compareTo("hour") == 0 ) {
                calendarField = Calendar.HOUR;    
            } else if ( key.compareTo("minute") == 0 ) {
                calendarField = Calendar.MINUTE;    
            }
         
            /* check prefix "later_first:" in pattern.
             * assume it applies to all patterns, so only check it once.
             */
            if ( index == 0 ) {
                int prefixLength = "later_first:".length();
                if ( pattern.length() > prefixLength &&
                     pattern.substring(0,prefixLength).compareTo("later_first:") == 0 ) {
                        fFirstDateInPtnIsLaterDate = true;
                        pattern = pattern.substring(prefixLength, pattern.length());
                }
            }
          
            if ( calendarField != Calendar.MILLISECONDS_IN_DAY + 1 ) {
                fIntervalPatterns[calendarField] = pattern;
            }
        }
        return true;
    }


    /**
     * Concat a single date pattern with a time interval pattern,
     * set it into the fIntervalPattern[field], while field is time field.
     * This is used to handle time interval patterns on skeleton with
     * both time and date. Present the date followed by 
     * the range expression for the time.
     * @param dtfmt                  date and time format
     * @param datePattern            date pattern
     * @param field                  time calendar field: AM_PM, HOUR, MINUTE
     * @draft ICU 4.0 
     */
    private void concatSingleDate2TimeInterval(String dtfmt,
                                               String datePattern,
                                               int field)
    {

        String  timeIntervalPattern = fIntervalPatterns[field];
        fIntervalPatterns[field] = MessageFormat.format(dtfmt, new Object[] 
                                         {timeIntervalPattern, datePattern});
    }


    /** 
     * Provides a way for client to build interval patterns.
     * User couldruct <code>DateIntervalInfo</code> by providing 
     * a list of patterns.
     * <P>
     * For example:
     * <pre>
     * DateIntervalInfo* dIntervalInfo = new DateIntervalInfo();
     * dIntervalInfo->setIntervalPattern(UCAL_YEAR, "'from' YYYY-M-d 'to' YYYY-M-d"); 
     * dIntervalInfo->setIntervalPattern(UCAL_MONTH, "'from' YYYY MMM d 'to' MMM d");
     * dIntervalInfo->setIntervalPattern(UCAL_DAY, "YYYY MMM d-d");
     * dIntervalInfo->setFallbackIntervalPattern("yyyy mm dd - yyyy mm dd");
     * </pre>
     *
     * Restriction: the minimum calendar field to set interval pattern is MINUTE
     *
     * @param lrgDiffCalUnit   the largest different calendar unit.
     * @param intervalPattern  the interval pattern on the largest different
     *                         calendar unit.
     *                         For example, if <code>lrgDiffCalUnit</code> is 
     *                         "year", the interval pattern for en_US when year
     *                         is different could be "'from' yyyy 'to' yyyy".
     * @exception IllegalArgumentException  if setting interval pattern on 
     *                            a calendar field that is smaller
     *                            than the MINIMUM_SUPPORTED_CALENDAR_FIELD 
     * @draft ICU 4.0
     */
    public void setIntervalPattern(int lrgDiffCalUnit, 
                                   String intervalPattern)
                                   throws IllegalArgumentException
    {
        if ( lrgDiffCalUnit > MINIMUM_SUPPORTED_CALENDAR_FIELD ) {
            throw new IllegalArgumentException("calendar field is larger than MINIMUM_SUPPORTED_CALENDAR_FIELD");
        }
        // check for "later_first:" prefix
        int prefixLength = "later_first:".length();

        if ( intervalPattern.length() > prefixLength &&
             intervalPattern.substring(0, prefixLength).compareTo("later_first:") == 0 ) {
            fFirstDateInPtnIsLaterDate = true;
            intervalPattern = intervalPattern.substring(prefixLength, intervalPattern.length());
        } 
        fIntervalPatterns[lrgDiffCalUnit] = intervalPattern;
        if ( lrgDiffCalUnit == Calendar.HOUR_OF_DAY ) {
            fIntervalPatterns[Calendar.AM_PM] = intervalPattern;
            fIntervalPatterns[Calendar.HOUR] = intervalPattern;
        }
    }

    /**
     * Get the interval pattern given the largest different calendar field.
     * @param field      the largest different calendar field
     * @return interval pattern
     * @exception IllegalArgumentException  if getting interval pattern on 
     *                            a calendar field that is smaller
     *                            than the MINIMUM_SUPPORTED_CALENDAR_FIELD 
     * @draft ICU 4.0 
     */
    public String getIntervalPattern(int field) throws IllegalArgumentException
    {
        if ( field > MINIMUM_SUPPORTED_CALENDAR_FIELD ) {
            throw new IllegalArgumentException("calendar field is larger than MINIMUM_SUPPORTED_CALENDAR_FIELD");
        }
        return fIntervalPatterns[field];
    }

    /**
     * Get the fallback interval pattern.
     * @return fallback interval pattern
     * @draft ICU 4.0 
     */
    public String getFallbackIntervalPattern()
    {
        return fFallbackIntervalPattern;
    }

    /**
     * Set the fallback interval pattern.
     * Fall-back interval pattern is get from locale resource.
     * If a user want to set their own fall-back interval pattern,
     * they can do so by calling the following method.
     * For users whoruct DateIntervalInfo() by defaultructor,
     * all interval patterns ( including fall-back ) are not set,
     * those users need to call setIntervalPattern() to set their own
     * interval patterns, and call setFallbackIntervalPattern() to set
     * their own fall-back interval patterns. If a certain interval pattern
     * ( for example, the interval pattern when 'year' is different ) is not
     * found, fall-back pattern will be used. 
     * For those users who set all their patterns ( instead of calling 
     * non-defaultructor to lsetructor get those patterns from 
     * locale ), if they do not set the fall-back interval pattern, 
     * it will be fall-back to '{date0} - {date1}'.
     *
     * @param fallbackPattern     fall-back interval pattern.
     * @draft ICU 4.0 
     */
    public void setFallbackIntervalPattern(String fallbackPattern)
    {
        // check for "later_first:" prefix
        int prefixLength = "later_first:".length();

        if ( fallbackPattern.length() > prefixLength &&
             fallbackPattern.substring(0, prefixLength).compareTo("later_first:") == 0 ) {
            fFirstDateInPtnIsLaterDate = true;
            fallbackPattern = fallbackPattern.substring(prefixLength, fallbackPattern.length());
        }
        fFallbackIntervalPattern = fallbackPattern;
    }



    /**
     * check whether the first date in interval pattern is the later date.
     * An example of interval pattern is "d MMM, yyyy - d MMM, yyyy",
     * in which, the first part of the patter is the earlier date.
     * If the interval pattern is prefixed with "later_first:", for example:
     * "later_first:d MMM, yyyy - d MMM, yyyy", it means the first part
     * of the patter is the later date.
     * @return  true if the first date in interval pattern is the later date.
     * @draft ICU 4.0
     */
    public boolean firstDateInPtnIsLaterDate() {
        return fFirstDateInPtnIsLaterDate;
    }

    /**
     * Clone this object.
     * @return     a copy of the object
     * @exception  IllegalStateException  If clone is not supported
     * @draft    ICU4.0
     */
    public Object clone() throws IllegalStateException
    {
        try {
            DateIntervalInfo other = (DateIntervalInfo) super.clone();
            // FIXME: others
            other.fIntervalPatterns = fIntervalPatterns;
            other.fFirstDateInPtnIsLaterDate = fFirstDateInPtnIsLaterDate;
            return other;
        } catch ( CloneNotSupportedException e ) {
            throw new  IllegalStateException("clone is not supported");
        }
    }

};// end class DateIntervalInfo
