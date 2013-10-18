/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.EnumMap;

import com.ibm.icu.impl.CalendarData;
import com.ibm.icu.util.ULocale;


/**
 * Formats relative dates e.g In 5 days; next Sunday; etc.
 * This class is NOT thread-safe.
 * @draft ICU 53
 * @provisional
 */
public class RelativeDateTimeFormatter {
    
    
    /**
     * Represents a Time unit.
     * @draft ICU 53
     * @provisional
     *
     */
    public static enum TimeUnit {
        SECONDS, // eg, 3 seconds ago, or in 3 seconds
        MINUTES, // 3 minutes ago, or in 3 minutes
        HOURS, // 3 hours ago, or in 3 hours
        DAYS, // 3 days ago, or in 3 days
        WEEKS, // 3 weeks ago, or in 3 weeks
        MONTHS, // 3 months ago, or in 3 months
        YEARS, // 3 years ago, or in 3 years
    }
    
    /**
     * Represents a relative unit
     * @draft ICU 53
     * @provisional
     *
     */
    public static enum RelativeUnit {
        SUNDAY, // Last Sunday, This Sunday, Next Sunday, Sunday
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        DAY,  // Yesterday, Today, Tomorrow
        WEEK,
        MONTH,
        YEAR,
        NOW,
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
      
      /**
       * Represents the style. Not yet supported because we don't have CLDR data for this.
       * @draft ICU 53
       * @provisional
       *
       */
      public static enum Style {
          NARROW,
          SHORT,
          FULL,
      }

    private static final EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>> relativeUnitCache =
            new EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>>(RelativeUnit.class);
    
    private static final EnumMap<TimeUnit, QuantityFormatter[]> timeUnitCache =
            new EnumMap<TimeUnit, QuantityFormatter[]>(TimeUnit.class);
    
    static {
        addRelativeUnit(relativeUnitCache, RelativeUnit.DAY, "yesterday", "today", "tomorrow");
        addRelativeUnit(relativeUnitCache, RelativeUnit.MONDAY, "last Monday", "this Monday", "next Monday");
        addRelativeUnit(relativeUnitCache, RelativeUnit.NOW, "now");
        
        QuantityFormatter.Builder qb = new QuantityFormatter.Builder();
        timeUnitCache.put(TimeUnit.DAYS, new QuantityFormatter[] {
                qb.add("one", "{0} day ago").add("other", "{0} days ago").build(),
                qb.add("one", "in {0} day").add("other", "in {0} days").build()});
        timeUnitCache.put(TimeUnit.HOURS, new QuantityFormatter[] {
                qb.add("one", "{0} hour ago").add("other", "{0} hours ago").build(),
                qb.add("one", "in {0} hour").add("other", "in {0} hours").build()});
        timeUnitCache.put(TimeUnit.MINUTES, new QuantityFormatter[] {
                qb.add("one", "{0} minute ago").add("other", "{0} minutes ago").build(),
                qb.add("one", "in {0} minute").add("other", "in {0} minutes").build()});
        timeUnitCache.put(TimeUnit.SECONDS, new QuantityFormatter[] {
                qb.add("one", "{0} second ago").add("other", "{0} seconds ago").build(),
                qb.add("one", "in {0} second").add("other", "in {0} seconds").build()});
    }
    

    private final EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>> relativeUnitMap;
    private final EnumMap<TimeUnit, QuantityFormatter[]> timeUnitMap;
    private final MessageFormat combinedDateAndTime;
    private final PluralRules pluralRules;
    private NumberFormat numberFormat;
    
    /**
     * Returns a RelativeDateTimeFormatter for the default locale.
     * @draft ICU 53
     * @provisional
     */
    public static RelativeDateTimeFormatter getInstance() {
        CalendarData calData = new CalendarData(ULocale.getDefault(), null);
        // TODO: Pull from resource bundles/cache
        return new RelativeDateTimeFormatter(
                relativeUnitCache,
                timeUnitCache,
                new MessageFormat(calData.getDateTimePattern()),
                PluralRules.forLocale(ULocale.getDefault()),
                NumberFormat.getInstance());
    }
    
    /**
     * Returns a RelativeDateTimeFormatter for a particular locale.
     * @draft ICU 53
     * @provisional
     */
    public static RelativeDateTimeFormatter getInstance(ULocale locale) {
        CalendarData calData = new CalendarData(locale, null);
        // TODO: Pull from resource bundles/cache
        return new RelativeDateTimeFormatter(
                relativeUnitCache,
                timeUnitCache,
                new MessageFormat(calData.getDateTimePattern()),
                PluralRules.forLocale(locale),
                NumberFormat.getInstance(locale));
    }
    
    /**
     * Returns a RelativeDateTimeFormatter for a particular locale and style.
     * This is currently not supported because of lack of CLDR data.
     * @draft ICU 53
     * @provisional
     */
    public static RelativeDateTimeFormatter getInstance(ULocale locale, Style style) {
        throw new UnsupportedOperationException("Missing CLDR data.");
    }
         
    private static void addRelativeUnit(
            EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>> relativeUnits,
            RelativeUnit unit,
            String current) {
        EnumMap<RelativeOffset, String> unitStrings =
                new EnumMap<RelativeOffset, String>(RelativeOffset.class);
        unitStrings.put(RelativeOffset.LAST, current);
        unitStrings.put(RelativeOffset.THIS, current);
        unitStrings.put(RelativeOffset.NEXT, current);
        unitStrings.put(RelativeOffset.PLAIN, current);
        relativeUnits.put(unit,  unitStrings);       
    }

    private static void addRelativeUnit(
            EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>> relativeUnits,
            RelativeUnit unit, String last, String current, String next) {
        EnumMap<RelativeOffset, String> unitStrings =
                new EnumMap<RelativeOffset, String>(RelativeOffset.class);
        unitStrings.put(RelativeOffset.LAST, last);
        unitStrings.put(RelativeOffset.THIS, current);
        unitStrings.put(RelativeOffset.NEXT, next);
        unitStrings.put(RelativeOffset.PLAIN, current);
        relativeUnits.put(unit,  unitStrings);
    }

    
    private RelativeDateTimeFormatter(
            EnumMap<RelativeUnit, EnumMap<RelativeOffset, String>> relativeUnitMap,
            EnumMap<TimeUnit, QuantityFormatter[]> timeUnitMap,
            MessageFormat combinedDateAndTime,
            PluralRules pluralRules,
            NumberFormat numberFormat) {
        this.relativeUnitMap = relativeUnitMap;
        this.timeUnitMap = timeUnitMap;
        this.combinedDateAndTime = combinedDateAndTime;
        this.pluralRules = pluralRules;
        this.numberFormat = numberFormat;
    }
    
    /**
     * Formats a quantitative relative date e.g 5 hours ago; in 3 days.
     * @param distance The numerical amount e.g 5. This value is formatted according to this
     *   object's {@link NumberFormat} object.
     * @param unit The time unit. e.g DAYS
     * @param isFuture True if relative date is in future.
     * @return the formatted string
     * @draft ICU 53
     * @provisional
     */
    public String format(double distance, TimeUnit unit, boolean isFuture) {
        return getQuantity(unit, isFuture).format(distance, numberFormat, pluralRules);
    }
    
    private QuantityFormatter getQuantity(TimeUnit unit, boolean isFuture) {
        QuantityFormatter[] quantities = timeUnitMap.get(unit);
        return isFuture ? quantities[1] : quantities[0];
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
        return this.relativeUnitMap.get(unit).get(offset);
    }
    
    /**
     * Specify which NumberFormat object this object should use for
     * formatting numbers.
     * @param nf the NumberFormat object to use. This method makes no copy,
     * so any subsequent changes to nf will affect this object.
     * @see #format(double, TimeUnit, boolean)
     * @draft ICU 53
     * @provisional
     */
    public void setNumberFormat(NumberFormat nf) {
        this.numberFormat = nf;
    }

    /**
     * Combines a relative date string and a time string in this object's locale. This is
     * done with the same date-time separator used for the Gregorian calendar in this
     * locale.
     * @param relativeDateString the relative date e.g 'yesterday'
     * @param timeString the time e.g '3:45'
     * @return the date and time concatenated according to the Gregorian calendar in this
     *   locale e.g 'yesterday, 3:45'
     * @draft ICU 53
     * @provisional
     */
    public String combineDateAndTime(String relativeDateString, String timeString) {
        return this.combinedDateAndTime.format(
            new Object[]{timeString, relativeDateString}, new StringBuffer(), null).toString();
    }
}
