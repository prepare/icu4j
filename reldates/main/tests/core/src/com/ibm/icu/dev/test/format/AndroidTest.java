/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.RelativeFormatter;
import com.ibm.icu.util.Calendar;

public class AndroidTest {
    
    private static final int FORMAT_ABBREV_RELATIVE = 1;
    private static final int FORMAT_ABBREV_ALL = 2;
    
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
    
    public static final int FORMAT_SHOW_TIME = 0x00001;
    public static final int FORMAT_SHOW_WEEKDAY = 0x00002;
    public static final int FORMAT_SHOW_YEAR = 0x00004;
    public static final int FORMAT_NO_YEAR = 0x00008;
    public static final int FORMAT_SHOW_DATE = 0x00010;
    public static final int FORMAT_NO_MONTH_DAY = 0x00020;
    @Deprecated
    public static final int FORMAT_12HOUR = 0x00040;
    @Deprecated
    public static final int FORMAT_24HOUR = 0x00080;
    @Deprecated
    public static final int FORMAT_CAP_AMPM = 0x00100;
    public static final int FORMAT_NO_NOON = 0x00200;
    @Deprecated
    public static final int FORMAT_CAP_NOON = 0x00400;
    public static final int FORMAT_NO_MIDNIGHT = 0x00800;
    @Deprecated
    public static final int FORMAT_CAP_MIDNIGHT = 0x01000;
    
    public static final int FORMAT_UTC = 0x02000;
    public static final int FORMAT_ABBREV_TIME = 0x04000;
    public static final int FORMAT_ABBREV_WEEKDAY = 0x08000;
    public static final int FORMAT_ABBREV_MONTH = 0x10000;
    public static final int FORMAT_NUMERIC_DATE = 0x20000;
    private static final long NOW = 1300000000000L;

    public static CharSequence getRelativeTimeSpanString(long time, long now, long minResolution, int flags) {
        RelativeFormatter rf = getRelativeFormat((flags & (FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_ALL)) != 0);
        boolean past = (now >= time);
        long duration = Math.abs(now - time);
        
        if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
            return rf.format(
                    duration / SECOND_IN_MILLIS,
                    RelativeFormatter.TimeUnit.SECONDS,
                    !past);
        } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
            return rf.format(
                    duration / MINUTE_IN_MILLIS,
                    RelativeFormatter.TimeUnit.MINUTES,
                    !past);
        } else if (duration < DAY_IN_MILLIS && minResolution < DAY_IN_MILLIS) {
            return rf.format(
                    duration / HOUR_IN_MILLIS,
                    RelativeFormatter.TimeUnit.HOURS,
                    !past);
        } else if (duration < WEEK_IN_MILLIS && minResolution < WEEK_IN_MILLIS) {
            int days = dayDiff(now, time);
            if (days == 0) {
                return rf.format(
                        RelativeFormatter.RelativeOffset.THIS,
                        RelativeFormatter.RelativeUnit.DAY);
            } else if (days == 1) {
                return rf.format(
                        RelativeFormatter.RelativeOffset.NEXT,
                        RelativeFormatter.RelativeUnit.DAY);
            } else if (days == -1) {
                return rf.format(
                        RelativeFormatter.RelativeOffset.LAST,
                        RelativeFormatter.RelativeUnit.DAY);
            } else {
                return rf.format(
                        Math.abs(days),
                        RelativeFormatter.TimeUnit.DAYS,
                        days > 0);
            }
        } else {
            return formatDateRange(null, time, time, flags);
        }
    }
    
    public static CharSequence getRelativeTimeSpanString(Object context, long millis, boolean withPreposition) {
        String result;
        long now = System.currentTimeMillis();
        
        Calendar nowCal = Calendar.getInstance();
        Calendar thenCal = Calendar.getInstance();
        
        nowCal.setTimeInMillis(now);
        thenCal.setTimeInMillis(millis);
        
        if (nowCal.get(Calendar.JULIAN_DAY) == thenCal.get(Calendar.JULIAN_DAY)) {
            // Same day
            int flags = FORMAT_SHOW_TIME;
            result = formatDateRange(context, millis, millis, flags).toString();
        } else if (nowCal.get(Calendar.YEAR) != thenCal.get(Calendar.YEAR)) {
                // Different years
                int flags = FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NUMERIC_DATE;
                result = formatDateRange(context, millis, millis, flags).toString();
        } else {
            // Default
            int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH;
            result = formatDateRange(context, millis, millis, flags).toString();
        }
        return result;    
    }
    
    public static CharSequence getRelativeDateTimeString(Object context, long time, long minRes, long transitionRes, int flags) {
        // Need CLDR data to join relative and aboslute time. e.g yesterday, 12:20.
        // CLDR path: dateTimeFormats/dateTimeFormatLength[@type='full']/dateTimeFormat/pattern.
        // {1} = the date and {0} = the time.
        // ICU path:  calendar/$1/DateTimePatterns
        RelativeFormatter rf = getRelativeFormat((flags & (FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_ALL)) != 0);
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - time);
        
        // getRelativeTimeSpanString() doesn't correctly format relative dates
        // above a week or exact dates below a day, so clamp
        // transitionResolution as needed.
        if (transitionRes > WEEK_IN_MILLIS) {
            transitionRes = WEEK_IN_MILLIS;
        } else if (transitionRes < DAY_IN_MILLIS) {
            transitionRes = DAY_IN_MILLIS;
        }
        
        String dateClause;
        String timeClause = formatDateRange(context, time, time, FORMAT_SHOW_TIME).toString();
        if (duration < transitionRes) {
            dateClause = getRelativeTimeSpanString(time, now, minRes, flags).toString();
        } else {
            dateClause = getRelativeTimeSpanString(context, time, false).toString();
        }
        return rf.combineDateAndTime(dateClause, timeClause);
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


    private static CharSequence formatDateRange(Object context, long startTime, long endTime, int flags) {
        if (startTime != endTime) {
            throw new AssertionError();
        }
        DateFormat df;
        if (flags == FORMAT_SHOW_TIME) {
           df = DateFormat.getPatternInstance("jms"); 
        } else if (flags == (FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH)) {
            df = DateFormat.getPatternInstance("MMMd");
        } else if (flags == (FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NUMERIC_DATE)) {
            df = DateFormat.getPatternInstance("yMd");
        } else {
            df = DateFormat.getPatternInstance("yMd");
        }
        return df.format(new java.util.Date(startTime));
        
    }


    private static RelativeFormatter getRelativeFormat(boolean abbrev) {
        // TODO: support abbreviated style
        return RelativeFormatter.getInstance();
    }
    
    public static void main(String[] args) {
      long[] offsets = {0L, 1700L, 123000L, 10801000L, 431000000L, 1000000000L,
              -1700L, -123000L, -10801000L, -431000000L, -1000000000L};
      long[] resArr = {0L, 1000L, 60000L, 3600000L, 86400000L, 7 * 86400000L};
      for (long offset: offsets) {
          for (long res: resArr) {
              System.out.println(offset+"\t"+res+"\t"+getRelativeTimeSpanString(
                      NOW + offset, NOW, res, 0));
          }
      }

    }

}
