/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;


/**
 * Formats relative dates e.g In 5 days; next Sunday; etc.
 * This class is NOT thread-safe.
 * @draft ICU 53
 * @provisional
 */
public class RelativeFormat {
    
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
      
    /**
     * Creates a RelativeFormat for the default locale and FULL style.  
     */
    public RelativeFormat() {       
    }
    
    /**
     * Creates a RelativeFormat for the given locale and FULL style.  
     */
    public RelativeFormat(ULocale locale) {       
    }
    
    /**
     * Creates a RelativeFormat for the given locale and style.  
     */
    public RelativeFormat(ULocale locale, Style style) {       
    }
    
    /**
     * Formats a quantitative relative date e.g 5 hours ago; in 3 days.
     * @param distance The numerical amount e.g 5.
     * @param unit The time unit. e.g DAYS
     * @param isFuture True if relative date is in future.
     * @param toAppendTo append relative date here
     * @return toAppendTo
     * @draft ICU 53
     * @provisional
     */
    public StringBuffer format(double distance, TimeUnit unit, boolean isFuture, StringBuffer toAppendTo) {
        // TODO Auto-generated method stub
        return toAppendTo;
    }

    /**
     * Formats a qualitative relative date e.g next week; yesterday.
     * @param offset NEXT, LAST, THIS, etc.
     * @param unit e.g SATURDAY, DAY, MONTH
     * @param toAppendTo append relative date here
     * @return toAppendTo
     * @draft ICU 53
     * @provisional
     */
    public StringBuffer format(RelativeOffset offset, RelativeUnit unit, StringBuffer toAppendTo) {
        // TODO Auto-generated method stub
        return toAppendTo;
    }
    
    /**
     * Sets the NumberFormat object for this formatter to use. If not called,
     * RelativeFormat objects lazily create a default NumberFormat object based on the
     * locale.
     * @param nf the NumberFormat object to use.
     */
    public void setNumberFormat(NumberFormat nf) {
        
    }
}
