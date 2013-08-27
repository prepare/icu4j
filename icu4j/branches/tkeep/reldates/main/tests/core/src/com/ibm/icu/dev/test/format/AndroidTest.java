/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.text.RelativeFormat;
import com.ibm.icu.util.Calendar;

public class AndroidTest {
    
    private static final int FORMAT_ABBREV_RELATIVE = 1;
    private static final int FORMAT_ABBREV_ALL = 2;
    
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

    public static CharSequence getRelativetimeSpanString(long time, long now, long minResolution, int flags) {
        RelativeFormat rf = getRelativeFormat((flags & (FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_ALL)) != 0);
        boolean past = (now >= time);
        long duration = Math.abs(now - time);
        
        if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
            return rf.format(
                    duration / SECOND_IN_MILLIS,
                    RelativeFormat.RelativeTimeUnit.SECONDS,
                    !past,
                    new StringBuffer());
        } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
            return rf.format(
                    duration / MINUTE_IN_MILLIS,
                    RelativeFormat.RelativeTimeUnit.MINUTES,
                    !past,
                    new StringBuffer());
        } else if (duration < DAY_IN_MILLIS && minResolution < DAY_IN_MILLIS) {
            return rf.format(
                    duration / HOUR_IN_MILLIS,
                    RelativeFormat.RelativeTimeUnit.HOURS,
                    !past,
                    new StringBuffer());
        } else if (duration < WEEK_IN_MILLIS && minResolution < WEEK_IN_MILLIS) {
            int days = dayDiff(now, time);
            if (days == 0) {
                return rf.format(
                        RelativeFormat.RelativeOffset.THIS,
                        RelativeFormat.RelativeUnit.DAY,
                        new StringBuffer());
            } else if (days == 1) {
                return rf.format(
                        RelativeFormat.RelativeOffset.NEXT,
                        RelativeFormat.RelativeUnit.DAY,
                        new StringBuffer());
            } else if (days == -1) {
                return rf.format(
                        RelativeFormat.RelativeOffset.LAST,
                        RelativeFormat.RelativeUnit.DAY,
                        new StringBuffer());
            } else {
                return rf.format(
                        Math.abs(days),
                        RelativeFormat.RelativeTimeUnit.DAYS,
                        days > 0,
                        new StringBuffer());
            }
        } else {
            return formatDateRange(null, time, time, flags);
        }
    }
    
    public static CharSequence getRelativeTimeSpanString(Object context, long millis, boolean withPreposition) {
      // ICU cannot support the prepositions at this time. But everything else can be implemented with existing
        // ICU.
        // This function formats relative to the current time. e.g
        // "14:23; Aug 5; Jan 12, 2015.
        return nil;
    }
    
    public static CharSequence getRelativeDateTimeString(Object context, long time, long minRes, long transitionRes, int flags) {
      // Need CLDR data to join relative and aboslute time. e.g yesterday, 12:20.
        return nil;
        
    }

    
    private static int dayDiff(long start, long end) {
       Calendar startCal = Calendar.getInstance();
       Calendar endCal = Calendar.getInstance();
       startCal.setTimeInMillis(start);
       endCal.setTimeInMillis(end);
       startCal.set(Calendar.MILLISECONDS_IN_DAY, 0);
       endCal.set(Calendar.MILLISECONDS_IN_DAY, 0);
       return startCal.fieldDifference(endCal.getTime(), Calendar.DAY_OF_MONTH);
    }


    private static String formatDateRange(Object context, long time, long time2, int flags) {
      return "Revert to formatDateRange";
    }


    private static RelativeFormat getRelativeFormat(boolean abbrev) {
        // TODO: support abbreviated style
        return new RelativeFormat();
    }


    public static void main(String[] args) {
      

    }

}
