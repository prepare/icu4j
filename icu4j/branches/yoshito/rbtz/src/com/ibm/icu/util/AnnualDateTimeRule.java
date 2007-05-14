package com.ibm.icu.util;

/**
 * AnnualDateTimeRule is a class representing a rule for
 * annually repeating date and time.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class AnnualDateTimeRule {
    /**
     * Date rule type defined by exact day of month.
     * For example, March 14.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DOM = 0;

    /**
     * Date rule type defined by day of week in month.
     * For exmaple, 2nd Sunday in March.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DOW = 1;

    /**
     * Date rule type defined by first day of week on or
     * after exact day of month.
     * For example, 1st Monday on or after March 15.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DOM_GEQ_DOM = 2;

    /**
     * Date rule type defined by last day of week on or
     * before exact day of month.
     * For example, last Saturday on or before March 15.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int DOM_LEQ_DOM = 3;
    
    /**
     * Time rule type for local wall time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int WALL_TIME = 0;

    /**
     * Time rule type for local standard time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int STANDARD_TIME = 1;

    /**
     * Time rule type for coordinated universal time.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static final int UNIVERSAL_TIME = 2;

    // private stuff
    private final int dateRuleType;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int weekInMonth;

    private final int timeRuleType;
    private final int millisInDay;

    /**
     * Constructs an AnnualDateTimeRule by the day of month and
     * the time rule.  The date rule type for an instance created by
     * this constructor is DOM.
     * 
     * @param month         The rule month, for example, Calendar.JANUARY
     * @param dayOfMonth    The day of month, 1-based.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, WALL_TIME or STANDARD_TIME or UNIVERSAL_TIME.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public AnnualDateTimeRule(int month, int dayOfMonth,
            int millisInDay, int timeType) {
        dateRuleType = DOM;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        
        // not used by this rule type
        this.dayOfWeek = 0;
        this.weekInMonth = 0;
    }

    /**
     * Constructs an AnnualDateTimeRule by the day of week and its oridinal
     * number and the time rule.  The date rule type for an instance created
     * by this constructor is DOW.
     * 
     * @param month         The rule month, for example, Calendar.JANUARY.
     * @param weekInMonth   The ordinal number of the day of week.  Negative number
     *                      may be used for specifying a rule date counted from the
     *                      end of the rule month.
     * @param dayOfWeek     The day of week, for example, Calendar.SUNDAY.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, WALL_TIME or STANDARD_TIME or UNIVERSAL_TIME.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public AnnualDateTimeRule(int month, int weekInMonth, int dayOfWeek,
            int millisInDay, int timeType) {
        dateRuleType = DOW;
        this.month = month;
        this.weekInMonth = weekInMonth;
        this.dayOfWeek = dayOfWeek;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;

        // not used by this rule type
        this.dayOfMonth = 0;
    }

    /**
     * Constructs an AnnualDateTimeRule by the first/last day of week
     * on or after/before the day of month and the time rule.  The date rule
     * type for an instance created by this constructor is either
     * DOM_GEQ_DOM or DOM_LEQ_DOM.
     * 
     * @param month         The rule month, for example, Calendar.JANUARY
     * @param dayOfMonth    The day of month, 1-based.
     * @param dayOfWeek     The day of week, for example, Calendar.SUNDAY.
     * @param after         true if the rule date is on or after the day of month.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, WALL_TIME or STANDARD_TIME or UNIVERSAL_TIME.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public AnnualDateTimeRule(int month, int dayOfMonth, int dayOfWeek, boolean after,
            int millisInDay, int timeType) {
        this.dateRuleType = after ? DOM_GEQ_DOM : DOM_LEQ_DOM;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;

        // not used by this rule type
        this.weekInMonth = 0;
    }

    /**
     * Gets the date rule type, such as DOM
     * 
     * @return The date rule type.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getDateRuleType() {
        return dateRuleType;
    }

    /**
     * Gets the rule month.
     * 
     * @return The rule month.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getRuleMonth() {
        return month;
    }

    /**
     * Gets the rule day of month.  When the date rule type
     * is DOW, the value is always 0.
     * 
     * @return The rule day of month
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getRuleDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Gets the rule day of week.  When the date rule type
     * is DOM, the value is always 0.
     * 
     * @return The rule day of week.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getRuleDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Gets the rule day of week ordinal number in the month.
     * When the date rule type is not DOW, the value is
     * always 0.
     * 
     * @return The rule day of week ordinal number in the month.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getRuleWeekInMonth() {
        return weekInMonth;
    }

    /**
     * Gets the time rule type
     * 
     * @return The time rule type, either WALL_TIME or STANDARD_TIME
     *         or UNIVERSAL_TIME.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getTimeRuleType() {
        return timeRuleType;
    }

    /**
     * Gets the rule time in the rule day.
     * 
     * @return The time in the rule day in milliseconds.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public int getRuleMillisInDay() {
        return millisInDay;
    }
}
