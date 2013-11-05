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
 * Formats simple relative dates. There are two types of relative dates that
 * it handles:
 * <ul>
 *   <li>relative dates with a quantity e.g "in 5 days"</li>
 *   <li>relative dates without a quantity e.g "next Tuesday"</li>
 * </ul>
 * <p>
 * This API is very basic and is intended to be a building block for more
 * fancy APIs. The caller tells it exactly what to display in a locale
 * independent way. While this class automatically provides the correct plural
 * forms, the grammatical form is otherwise as neutral as possible. It is the
 * caller's responsibility to handle cut-off logic such as deciding between
 * displaying "in 7 days" or "in 1 week." This API supports relative dates
 * involving one single unit. This API does not support relative dates
 * involving compound units.
 * e.g "in 5 days and 4 hours" nor does it support parsing.
 * This class is NOT thread-safe.
 * <p>
 * Here are some examples of use:
 * <blockquote>
 * <pre>
 * RelativeDateTimeFormatter fmt = RelativeDateTimeFormatter.getInstance();
 * fmt.format(1, Direction.NEXT, RelativeUnit.DAYS); // "in 1 day"
 * fmt.format(3, Direction.NEXT, RelativeUnit.DAYS); // "in 3 days"
 * fmt.format(3.2, Direction.LAST, RelativeUnit.YEARS); // "3.2 years ago"
 * 
 * fmt.format(Direction.LAST, AbsoluteUnit.SUNDAY); // "last Sunday"
 * fmt.format(Direction.THIS, AbsoluteUnit.SUNDAY); // "this Sunday"
 * fmt.format(Direction.NEXT, AbsoluteUnit.SUNDAY); // "next Sunday"
 * fmt.format(Direction.PLAIN, AbsoluteUnit.SUNDAY); // "Sunday"
 * 
 * fmt.format(Direction.LAST, AbsoluteUnit.DAY); // "yesterday"
 * fmt.format(Direction.THIS, AbsoluteUnit.DAY); // "today"
 * fmt.format(Direction.NEXT, AbsoluteUnit.DAY); // "tomorrow"
 * 
 * fmt.format(Direction.PLAIN, AbsoluteUnit.NOW); // "now"
 * </pre>
 * </blockquote>
 * <p>
 * In the future, we may add more forms, such as abbreviated/short forms
 * (3 secs ago), and relative day periods ("yesterday afternoon"), etc.
 * 
 * @draft ICU 53
 * @provisional
 */
public final class RelativeDateTimeFormatter { 
    
    /**
     * Represents the unit for formatting a relative date. e.g "in 5 days"
     * or "in 3 months"
     * @draft ICU 53
     * @provisional
     */
    public static enum RelativeUnit {
        
        /**
         * Seconds
         * @draft ICU 53
         * @provisional
         */
        SECONDS,
        
        /**
         * Minutes
         * @draft ICU 53
         * @provisional
         */
        MINUTES,
        
       /**
        * Hours
        * @draft ICU 53
        * @provisional
        */
        HOURS,
        
        /**
         * Days
         * @draft ICU 53
         * @provisional
         */
        DAYS,
        
        /**
         * Weeks
         * @draft ICU 53
         * @provisional
         */
        WEEKS,
        
        /**
         * Months
         * @draft ICU 53
         * @provisional
         */
        MONTHS,
        
        /**
         * Years
         * @draft ICU 53
         * @provisional
         */
        YEARS,
    }
    
    /**
     * Represents an absolute unit.
     * @draft ICU 53
     * @provisional
     */
    public static enum AbsoluteUnit {
        
       /**
        * Sunday
        * @draft ICU 53
        * @provisional
        */
        SUNDAY,
        
        /**
         * Monday
         * @draft ICU 53
         * @provisional
         */
        MONDAY,
        
        /**
         * Tuesday
         * @draft ICU 53
         * @provisional
         */
        TUESDAY,
        
        /**
         * Wednesday
         * @draft ICU 53
         * @provisional
         */
        WEDNESDAY,
        
        /**
         * Thursday
         * @draft ICU 53
         * @provisional
         */
        THURSDAY,
        
        /**
         * Friday
         * @draft ICU 53
         * @provisional
         */
        FRIDAY,
        
        /**
         * Saturday
         * @draft ICU 53
         * @provisional
         */
        SATURDAY,
        
        /**
         * Day
         * @draft ICU 53
         * @provisional
         */
        DAY,
        
        /**
         * Week
         * @draft ICU 53
         * @provisional
         */
        WEEK,
        
        /**
         * Month
         * @draft ICU 53
         * @provisional
         */
        MONTH,
        
        /**
         * Year
         * @draft ICU 53
         * @provisional
         */
        YEAR,
        
        /**
         * Now
         * @draft ICU 53
         * @provisional
         */
        NOW,
      }

      /**
       * Represents a direction for an absolute unit e.g "Next Tuesday"
       * or "Last Tuesday"
       * @draft ICU 53
       * @provisional
       */
      public static enum Direction {
          
          /**
           * Last
           * @draft ICU 53
           * @provisional
           */  
        LAST,
        
        /**
         * This
         * @draft ICU 53
         * @provisional
         */
        THIS,
        
        /**
         * Next
         * @draft ICU 53
         * @provisional
         */
        NEXT,
        
        /**
         * Plain, which means the absence of a qualifier
         * @draft ICU 53
         * @provisional
         */
        PLAIN;
      }
    
    /**
     * Returns a RelativeDateTimeFormatter for the default locale.
     * @draft ICU 53
     * @provisional
     */
    public static RelativeDateTimeFormatter getInstance() {
        CalendarData calData = new CalendarData(ULocale.getDefault(), null);
        // TODO: Pull from resource bundles/cache
        return new RelativeDateTimeFormatter(
                qualitativeUnitCache,
                quantitativeUnitCache,
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
                qualitativeUnitCache,
                quantitativeUnitCache,
                new MessageFormat(calData.getDateTimePattern()),
                PluralRules.forLocale(locale),
                NumberFormat.getInstance(locale));
    }
    
           
    /**
     * Formats a relative date with a quantity such as "in 5 days" or
     * "3 months ago"
     * @param quantity The numerical amount e.g 5. This value is formatted
     * according to this object's {@link NumberFormat} object.
     * @param direction NEXT means a future relative date; LAST means a past
     * relative date.
     * @param unit the unit e.g day? month? year?
     * @return the formatted string
     * @throws IllegalArgumentException if direction is something other than
     * NEXT or LAST.
     * @draft ICU 53
     * @provisional
     */
    public String format(double quantity, Direction direction, RelativeUnit unit) {
        if (direction != Direction.LAST && direction != Direction.NEXT) {
            throw new IllegalArgumentException("direction must be NEXT or LAST");
        }
        return getQuantity(unit, direction == Direction.NEXT).format(quantity, numberFormat, pluralRules);
    }
    
    /**
     * Formats a relative date without a quantity.
     * @param direction NEXT, LAST, THIS, etc.
     * @param unit e.g SATURDAY, DAY, MONTH
     * @return the formatted string
     * @throws IllegalArgumentException if the direction is incompatible with
     * unit this can occur with NOW which can only take PLAIN.
     * @draft ICU 53
     * @provisional
     */
    public String format(Direction direction, AbsoluteUnit unit) {
        return this.qualitativeUnitMap.get(unit).get(direction);
    }
    
    /**
     * Specify which NumberFormat object this object should use for
     * formatting numbers. By default this object uses the default
     * NumberFormat object for this object's locale.
     * @param nf the NumberFormat object to use. This method makes
     *  its own defensive copy of nf so that subsequent
     *  changes to nf will not affect the operation of this object.
     * @see #format(double, Direction, RelativeUnit)
     * @draft ICU 53
     * @provisional
     */
    public void setNumberFormat(NumberFormat nf) {
        this.numberFormat = (NumberFormat) nf.clone();
    }

    /**
     * Combines a relative date string and a time string in this object's
     * locale. This is done with the same date-time separator used for the
     * default calendar in this locale.
     * @param relativeDateString the relative date e.g 'yesterday'
     * @param timeString the time e.g '3:45'
     * @return the date and time concatenated according to the default
     * calendar in this locale e.g 'yesterday, 3:45'
     * @draft ICU 53
     * @provisional
     */
    public String combineDateAndTime(String relativeDateString, String timeString) {
        return this.combinedDateAndTime.format(
            new Object[]{timeString, relativeDateString}, new StringBuffer(), null).toString();
    }
    
    private static void addQualitativeUnit(
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> qualitativeUnits,
            AbsoluteUnit unit,
            String current) {
        EnumMap<Direction, String> unitStrings =
                new EnumMap<Direction, String>(Direction.class);
        unitStrings.put(Direction.LAST, current);
        unitStrings.put(Direction.THIS, current);
        unitStrings.put(Direction.NEXT, current);
        unitStrings.put(Direction.PLAIN, current);
        qualitativeUnits.put(unit,  unitStrings);       
    }

    private static void addQualitativeUnit(
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> qualitativeUnits,
            AbsoluteUnit unit, String last, String current, String next) {
        EnumMap<Direction, String> unitStrings =
                new EnumMap<Direction, String>(Direction.class);
        unitStrings.put(Direction.LAST, last);
        unitStrings.put(Direction.THIS, current);
        unitStrings.put(Direction.NEXT, next);
        unitStrings.put(Direction.PLAIN, current);
        qualitativeUnits.put(unit,  unitStrings);
    }
 
    private RelativeDateTimeFormatter(
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> qualitativeUnitMap,
            EnumMap<RelativeUnit, QuantityFormatter[]> quantitativeUnitMap,
            MessageFormat combinedDateAndTime,
            PluralRules pluralRules,
            NumberFormat numberFormat) {
        this.qualitativeUnitMap = qualitativeUnitMap;
        this.quantitativeUnitMap = quantitativeUnitMap;
        this.combinedDateAndTime = combinedDateAndTime;
        this.pluralRules = pluralRules;
        this.numberFormat = numberFormat;
    }
    
    private QuantityFormatter getQuantity(RelativeUnit unit, boolean isFuture) {
        QuantityFormatter[] quantities = quantitativeUnitMap.get(unit);
        return isFuture ? quantities[1] : quantities[0];
    }
    
    private static final EnumMap<AbsoluteUnit, EnumMap<Direction, String>> qualitativeUnitCache =
            new EnumMap<AbsoluteUnit, EnumMap<Direction, String>>(AbsoluteUnit.class);
    
    private static final EnumMap<RelativeUnit, QuantityFormatter[]> quantitativeUnitCache =
            new EnumMap<RelativeUnit, QuantityFormatter[]>(RelativeUnit.class);
    
    static {
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.DAY, "yesterday", "today", "tomorrow");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.WEEK, "last week", "this week", "next week");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.MONTH, "last month", "this month", "next month");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.YEAR, "last year", "this year", "next year");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.MONDAY, "last Monday", "this Monday", "next Monday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.TUESDAY, "last Tuesday", "this Tuesday", "next Tuesday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.WEDNESDAY, "last Wednesday", "this Wednesday", "next Wednesday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.THURSDAY, "last Thursday", "this Thursday", "next Thursday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.FRIDAY, "last Friday", "this Friday", "next Friday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.SATURDAY, "last Saturday", "this Saturday", "next Saturday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.SUNDAY, "last Sunday", "this Sunday", "next Sunday");
        addQualitativeUnit(qualitativeUnitCache, AbsoluteUnit.NOW, "now");
        
        QuantityFormatter.Builder qb = new QuantityFormatter.Builder();
        quantitativeUnitCache.put(RelativeUnit.YEARS, new QuantityFormatter[] {
                qb.add("one", "{0} year ago").add("other", "{0} years ago").build(),
                qb.add("one", "in {0} year").add("other", "in {0} years").build()});
        quantitativeUnitCache.put(RelativeUnit.MONTHS, new QuantityFormatter[] {
                qb.add("one", "{0} month ago").add("other", "{0} months ago").build(),
                qb.add("one", "in {0} month").add("other", "in {0} months").build()});
        quantitativeUnitCache.put(RelativeUnit.WEEKS, new QuantityFormatter[] {
                qb.add("one", "{0} week ago").add("other", "{0} weeks ago").build(),
                qb.add("one", "in {0} week").add("other", "in {0} weeks").build()});
        quantitativeUnitCache.put(RelativeUnit.DAYS, new QuantityFormatter[] {
                qb.add("one", "{0} day ago").add("other", "{0} days ago").build(),
                qb.add("one", "in {0} day").add("other", "in {0} days").build()});
        quantitativeUnitCache.put(RelativeUnit.HOURS, new QuantityFormatter[] {
                qb.add("one", "{0} hour ago").add("other", "{0} hours ago").build(),
                qb.add("one", "in {0} hour").add("other", "in {0} hours").build()});
        quantitativeUnitCache.put(RelativeUnit.MINUTES, new QuantityFormatter[] {
                qb.add("one", "{0} minute ago").add("other", "{0} minutes ago").build(),
                qb.add("one", "in {0} minute").add("other", "in {0} minutes").build()});
        quantitativeUnitCache.put(RelativeUnit.SECONDS, new QuantityFormatter[] {
                qb.add("one", "{0} second ago").add("other", "{0} seconds ago").build(),
                qb.add("one", "in {0} second").add("other", "in {0} seconds").build()});
    }
    

    private final EnumMap<AbsoluteUnit, EnumMap<Direction, String>> qualitativeUnitMap;
    private final EnumMap<RelativeUnit, QuantityFormatter[]> quantitativeUnitMap;
    private final MessageFormat combinedDateAndTime;
    private final PluralRules pluralRules;
    private NumberFormat numberFormat;
}
