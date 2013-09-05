/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.EnumMap;

import com.ibm.icu.util.ULocale;


/**
 * Formats relative dates e.g In 5 days; next Sunday; etc.
 * This class is NOT thread-safe.
 * @draft ICU 53
 * @provisional
 */
public class RelativeFormatter {
    
    
    /**
     * Represents a Time unit.
     * @draft ICU 53
     * @provisional
     *
     */
    public static enum TimeUnit {
        SECONDS, // eg, 3 seconds ago, or in 3 seconds
        MINUTES, // 3 seconds ago
        HOURS, // 3 seconds ago
        DAYS, // 3 days ago, or in 3 days
        WEEKS, // 3 weeks ago, or in 3 weeks
        MONTHS, // 3 months ago
        YEARS, // 3 years ago
    }
    
    /**
     * Represents a relative unit
     * @draft ICU 53
     * @provisional
     *
     */
    public static enum RelativeUnit {
        SUNDAY, // Last Sunday, This Sunday, Next Sunday, Sunday
        MONDAY, // Last ...
        TUESDAY, // Last ...
        WEDNESDAY, // Last ...
        THURSDAY, // Last ...
        FRIDAY, // Last ...
        SATURDAY, // Last ...
        DAY,  // Yesterday, Today, Tomorrow
        WEEK, // Last week,..
        MONTH, // Next month
        YEAR, // Next year
        NOW, // comes from CLDR relative seconds=0
      }

      /**
       * Represents a relative offset.
       * @draft ICU 53
       * @provisional
       */
      public static enum RelativeOffset {
        LAST, THIS, NEXT, PLAIN; // not all will be available for all units
        // NOW has only PLAIN
        // SUNDAY..SATURDAY have all 4
        // Others have LAST..NEXT
      }
      
      public static enum Style {
          NARROW,
          SHORT,
          FULL,
      }

    private static final EnumMap<RelativeOffset, String> offsetMap =
            new EnumMap<RelativeFormatter.RelativeOffset, String>(RelativeOffset.class);


    private static final EnumMap<RelativeUnit, String> unitMap = 
            new EnumMap<RelativeFormatter.RelativeUnit, String>(RelativeUnit.class);


    private static final EnumMap<TimeUnit, String> timeUnitMap = 
            new EnumMap<RelativeFormatter.TimeUnit, String>(TimeUnit.class);
    
    static {
        offsetMap.put(RelativeOffset.LAST, "last");
        offsetMap.put(RelativeOffset.THIS, "this");
        offsetMap.put(RelativeOffset.NEXT, "next");
        offsetMap.put(RelativeOffset.PLAIN, "");
        
        unitMap.put(RelativeUnit.SUNDAY, "Sunday");
        unitMap.put(RelativeUnit.MONDAY, "Monday");
        unitMap.put(RelativeUnit.TUESDAY, "Tuesday");
        unitMap.put(RelativeUnit.WEDNESDAY, "Wednesday");
        unitMap.put(RelativeUnit.THURSDAY, "Thursday");
        unitMap.put(RelativeUnit.FRIDAY, "Friday");
        unitMap.put(RelativeUnit.SATURDAY, "Saturday");
        unitMap.put(RelativeUnit.WEEK, "day");
        unitMap.put(RelativeUnit.MONTH, "month");
        unitMap.put(RelativeUnit.YEAR, "year");
        
        timeUnitMap.put(TimeUnit.SECONDS, "Sunday");
        timeUnitMap.put(TimeUnit.MINUTES, "Monday");
        timeUnitMap.put(TimeUnit.HOURS, "Tuesday");
        timeUnitMap.put(TimeUnit.DAYS, "Wednesday");
        timeUnitMap.put(TimeUnit.WEEKS, "Thursday");
        timeUnitMap.put(TimeUnit.MONTHS, "Friday");
        timeUnitMap.put(TimeUnit.YEARS, "Saturday");
        
    }
    
    private DecimalFormat decimalFormat;  
      
    /**
     * Creates a RelativeFormat for the default locale and FULL style.  
     */
    public RelativeFormatter() {      
        decimalFormat = new DecimalFormat();
    }
    
    /**
     * Creates a RelativeFormat for the given locale and FULL style.  
     */
    public RelativeFormatter(ULocale locale) {     
        this();
    }
    
    /**
     * Creates a RelativeFormat for the given locale and style.  
     */
    public RelativeFormatter(ULocale locale, Style style) {
        this();
    }
    
    /**
     * Formats a quantitative relative date e.g 5 hours ago; in 3 days.
     * @param distance The numerical amount e.g 5.
     * @param unit The time unit. e.g DAYS
     * @param isFuture True if relative date is in future.
     * @return the formatted string
     * @draft ICU 53
     * @provisional
     */
    public String format(double distance, TimeUnit unit, boolean isFuture) {
        String unitStr = timeUnitMap.get(unit);
        if (isFuture) {
            return MessageFormat.format("{0, plural, one{in # "+unitStr+"}, other{in # "+unitStr+"s}}", distance);
        } else {
            return MessageFormat.format("{0, plural, one{# "+unitStr+" ago}, other{# "+unitStr+"s ago}}", distance);
        }
    }

    /**
     * Formats a qualitative relative date e.g next week; yesterday.
     * @param offset NEXT, LAST, THIS, etc.
     * @param unit e.g SATURDAY, DAY, MONTH
     * @return the formatted string
     * @draft ICU 53
     * @provisional
     */
    public String format(RelativeOffset offset, RelativeUnit unit) {
        switch (unit) {
        case NOW:
            return "now";
        case DAY:
            switch (offset) {
            case LAST:
                return "yesterday";
            case THIS:
                return "today";
            case NEXT:
                return "tomorrow";
            default:
                return "today";
            }
        default:
            return offsetMap.get(offset) + " " + unitMap.get(unit);
        }
    }
    
    /**
     * Sets the NumberFormat object for this formatter to use. If not called,
     * RelativeFormat objects lazily create a default NumberFormat object based on the
     * locale.
     * @param nf the NumberFormat object to use.
     */
    public void setNumberFormat(NumberFormat nf) {
        
    }

    public String combineDateAndTime(String dateClause, String timeClause) {
        return MessageFormat.format("{1} {0}", dateClause, timeClause);
    }
}
