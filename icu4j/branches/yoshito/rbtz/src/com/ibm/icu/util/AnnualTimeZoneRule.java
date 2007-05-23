/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;
import java.util.Date;

import com.ibm.icu.impl.Grego;


/**
 * AnnualTimeZoneRule is a class used for representing a time zone
 * rule which takes effect annually.  Years used in this class are
 * all Gregorian calendar years.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class AnnualTimeZoneRule extends TimeZoneRule {

    private static final long serialVersionUID = -2419464960618908799L;

    /**
     * The constant representing the maximum year used for designating a rule is permanent.
     */
    public static final int MAX_YEAR = Integer.MAX_VALUE;

    private final AnnualDateTimeRule dateTimeRule;
    private final int startYear;
    private final int endYear;

    private static final int MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * Constructs a TimeZoneRule with the name, the GMT offset of its
     * standard time, the amount of daylight saving offset adjustment,
     * the annual start time rule and the start/until years.
     * 
     * @param name          The time zone name.
     * @param rawOffset     The GMT offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in
     *                      milliseconds.  If this ia a rule for standard time,
     *                      the value of this argument is 0.
     * @param dateTimeRule  The start date/time rule repeated annually.
     * @param startYear     The first year when this rule takes effect.
     * @param endYear       The last year when this rule takes effect.  If this
     *                      rule is effective forever in future, specify MAX_YEAR.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public AnnualTimeZoneRule(String name, int rawOffset, int dstSavings,
            AnnualDateTimeRule dateTimeRule, int startYear, int endYear) {
        super(name, rawOffset, dstSavings);
        this.dateTimeRule = dateTimeRule;
        this.startYear = startYear;
        this.endYear = endYear > MAX_YEAR ? MAX_YEAR : endYear;
    }

    /**
     * Gets the start date/time rule associated used by this rule.
     * 
     * @return  An AnnualDateTimeRule which represents the start date/time
     *          rule used by this time zone rule.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public AnnualDateTimeRule getRule() {
        return dateTimeRule;
    }

    /**
     * Gets the first year when this rule takes effect.
     * 
     * @return  The start year of this rule.  The year is in Gregorian calendar
     *          with 0 == 1 BCE, -1 == 2 BCE, etc.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * Gets the end year when this rule takes effect.
     * 
     * @return  The end year of this rule (inclusive). The year is in Gregorian calendar
     *          with 0 == 1 BCE, -1 == 2 BCE, etc.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getEndYear() {
        return endYear;
    }

    /**
     * Gets the time when this rule takes effect in the given year.
     * 
     * @param year              The Gregorian year, with 0 == 1 BCE, -1 == 2 BCE, etc.
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time.
     * 
     * @return  The time when this rule takes effect in the year, or
     *          null if this rule is not applicable in the year.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getStartInYear(int year, int prevRawOffset, int prevDSTSavings) {
        if (year < startYear || year > endYear) {
            return null;
        }

        long ruleDay;
        int type = dateTimeRule.getDateRuleType();

        if (type == AnnualDateTimeRule.DOM) {
            ruleDay = Grego.fieldsToDay(year, dateTimeRule.getRuleMonth(), dateTimeRule.getRuleDayOfMonth());
        } else {
            boolean after = true;
            if (type == AnnualDateTimeRule.DOW) {
                int weeks = dateTimeRule.getRuleWeekInMonth();
                if (weeks > 0) {
                    ruleDay = Grego.fieldsToDay(year, dateTimeRule.getRuleMonth(), 1);
                    ruleDay += 7 * (weeks - 1);
                } else {
                    after = false;
                    ruleDay = Grego.fieldsToDay(year, dateTimeRule.getRuleMonth(), 
                            Grego.monthLength(year, dateTimeRule.getRuleMonth()));
                    ruleDay += 7 * (weeks + 1);
                }
            } else {
                int month = dateTimeRule.getRuleMonth();
                int dom = dateTimeRule.getRuleDayOfMonth();
                if (type == AnnualDateTimeRule.DOW_LEQ_DOM) {
                    after = false;
                    // Handle Feb <=29
                    if (month == Calendar.FEBRUARY && dom == 29 && !Grego.isLeapYear(year)) {
                        dom--;
                    }
                }
                ruleDay = Grego.fieldsToDay(year, month, dom);
            }

            int dow = Grego.dayOfWeek(ruleDay);
            int delta = dateTimeRule.getRuleDayOfWeek() - dow;
            if (after) {
                delta = delta < 0 ? delta + 7 : delta;
            } else {
                delta = delta > 0 ? delta - 7 : delta;
            }
            ruleDay += delta;
        }

        long ruleTime = ruleDay * MILLIS_PER_DAY + dateTimeRule.getRuleMillisInDay();
        if (dateTimeRule.getTimeRuleType() != AnnualDateTimeRule.UNIVERSAL_TIME) {
            ruleTime -= prevRawOffset;
        }
        if (dateTimeRule.getTimeRuleType() == AnnualDateTimeRule.WALL_TIME) {
            ruleTime -= prevDSTSavings;
        }
        return new Date(ruleTime);
    }
    
    /**
     * Gets the very first time when this rule takes effect.
     * 
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * 
     * @return  The very first time when this rule takes effect.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getFirstStart(int prevRawOffset, int prevDSTSavings) {
        return getStartInYear(startYear, prevRawOffset, prevDSTSavings);
    }

    /**
     * Gets the final time when this rule takes effect.
     * 
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * 
     * @return  The very last time when this rule takes effect,
     *          or null if this rule is applied for future dates infinitely.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getFinalStart(int prevRawOffset, int prevDSTSavings) {
        if (endYear == MAX_YEAR) {
            return null;
        }
        return getStartInYear(endYear, prevRawOffset, prevDSTSavings);
    }

    /**
     * Gets the first time when this rule takes effect after the specified time.
     * 
     * @param base              The first time after this time is returned.
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * @param inclusive         Whether the base time is inclusive or not.
     * 
     * @return  The first time when this rule takes effect after the specified time,
     *          or null when this rule never takes effect after the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getNextStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive) {
        int[] fields = Grego.dayToFields(Grego.timeToDay(base), null);
        int year = fields[0];
        if (year < startYear) {
            return getFirstStart(prevRawOffset, prevDSTSavings);
        }
        Date d = getStartInYear(year, prevRawOffset, prevDSTSavings);
        if (d != null && (d.getTime() < base || (!inclusive && (d.getTime() == base)))) {
            d = getStartInYear(year + 1, prevRawOffset, prevDSTSavings);
        }
        return d;
    }

    /**
     * Gets the most recent time when this rule takes effect before the specified time.
     * 
     * @param base              The most recent time when this rule takes effect before
     *                          this time is returned.
     * @param prevRawOffset     The standard time offset from UTC before this rule
     *                          takes effect in milliseconds.
     * @param prevDSTSavings    The amount of daylight saving offset from the
     *                          standard time. 
     * @param inclusive         Whether the base time is inclusive or not.
     * 
     * @return  The most recent time when this rule takes effect before the specified time,
     *          or null when this rule never takes effect before the specified time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public Date getPreviousStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive) {
        int[] fields = Grego.dayToFields(Grego.timeToDay(base), null);
        int year = fields[0];
        if (year > endYear) {
            return getFinalStart(prevRawOffset, prevDSTSavings);
        }
        Date d = getStartInYear(year, prevRawOffset, prevDSTSavings);
        if (d != null && (d.getTime() > base || (!inclusive && (d.getTime() == base)))) {
            d = getStartInYear(year - 1, prevRawOffset, prevDSTSavings);
        }
        return d;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZoneRule#hasStartTimes()
     */
    public boolean hasStartTimes() {
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(", rule={" + dateTimeRule + "}");
        buf.append(", startYear=" + startYear);
        buf.append(", endYear=");
        if (endYear == MAX_YEAR) {
            buf.append("max");
        } else {
            buf.append(endYear);
        }
        return buf.toString();
    }
}
