/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.BitSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.ibm.icu.util.AnnualTimeZoneRule;
import com.ibm.icu.util.DateTimeRule;
import com.ibm.icu.util.HasTimeZoneRules;
import com.ibm.icu.util.InitialTimeZoneRule;
import com.ibm.icu.util.TimeArrayTimeZoneRule;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZoneRule;
import com.ibm.icu.util.TimeZoneTransition;
import com.ibm.icu.util.TimeZoneTransitionRule;

/**
 * ICUTimeZone is an abstract class extends TimeZone and
 * implements HasTimeZoneTransitions.  ICU TimeZone implementation
 * classes should extend this abstract class.
 */
public abstract class ICUTimeZone extends TimeZone implements HasTimeZoneRules {

    protected final int MILLIS_PER_DAY = 24*60*60*1000;

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneTransitions#hasEquivalentTransitions(com.ibm.icu.util.TimeZone, long, long)
     */
    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end) {
        return hasEquivalentTransitions(tz, start, end, false);
    }

    /**
     * Checks if the time zone has equivalent transitions in the time range.
     * This method returns true when all of transition times, from/to standard
     * offsets and DST savings used by this time zone match the other in the
     * time range.
     * 
     * @param tz    The instance of TimeZone
     * @param start The start time of the evaluated time range (inclusive)
     * @param end   The end time of the evaluated time range (inclusive)
     * @param ignoreDstAmount
     *              When true, any transitions with only daylight saving amount
     *              changes will be ignored, except either of them is zero.
     *              For example, a transition from rawoffset 3:00/dstsavings 1:00
     *              to rawoffset 2:00/dstsavings 2:00 is excluded from the comparison,
     *              but a transtion from rawoffset 2:00/dstsavings 1:00 to
     *              rawoffset 3:00/dstsavings 0:00 is included.
     * 
     * @return true if the other time zone has the equivalent transitions in the
     * time range.  When tz is not implementing HasTimeZoneTransitions, this method
     * returns false.
     * 
     * @internal ICU 3.8
     * @deprecated This API is for internal ICU use only
     */
    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end, boolean ignoreDstAmount) {
        if (hasSameRules(tz)) {
            return true;
        }
        if (!(tz instanceof HasTimeZoneRules)) {
            return false;
        }

        // Check the offsets at the start time
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];

        getOffset(start, false, offsets1);
        tz.getOffset(start, false, offsets2);

        if (ignoreDstAmount) {
            if ((offsets1[0] + offsets1[1] != offsets2[0] + offsets2[1])
                || (offsets1[1] != 0 && offsets2[1] == 0)
                || (offsets1[1] == 0 && offsets2[1] != 0)) {
                return false;
            }
        } else {
            if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
                return false;
            }            
        }

        // Check transitions in the range
        long time = start;
        while (true) {
            TimeZoneTransition tr1 = getNextTransition(time, false);
            TimeZoneTransition tr2 = ((HasTimeZoneRules)tz).getNextTransition(time, false);

            if (ignoreDstAmount) {
                // Skip a transition which only differ the amount of DST savings
                if (tr1 != null
                        && (tr1.getFrom().getRawOffset() + tr1.getFrom().getDSTSavings()
                                == tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings())
                        && (tr1.getFrom().getDSTSavings() != 0 && tr1.getTo().getDSTSavings() != 0)) {
                    tr1 = getNextTransition(tr1.getTime(), false);
                }
                if (tr2 != null
                        && (tr2.getFrom().getRawOffset() + tr2.getFrom().getDSTSavings()
                                == tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings())
                        && (tr2.getFrom().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() != 0)) {
                    tr2 = getNextTransition(tr2.getTime(), false);
                }
            }

            boolean inRange1 = false;
            boolean inRange2 = false;
            if (tr1 != null) {
                if (tr1.getTime() <= end) {
                    inRange1 = true;
                }
            }
            if (tr2 != null) {
                if (tr2.getTime() <= end) {
                    inRange2 = true;
                }
            }
            if (!inRange1 && !inRange2) {
                // No more transition in the range
                break;
            }
            if (!inRange1 || !inRange2) {
                return false;
            }
            if (tr1.getTime() != tr2.getTime()) {
                return false;
            }
            if (ignoreDstAmount) {
                if (tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings()
                            != tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings()
                        || tr1.getTo().getDSTSavings() != 0 &&  tr2.getTo().getDSTSavings() == 0
                        || tr1.getTo().getDSTSavings() == 0 &&  tr2.getTo().getDSTSavings() != 0) {
                    return false;
                }
            } else {
                if (tr1.getTo().getRawOffset() != tr2.getTo().getRawOffset() ||
                    tr1.getTo().getDSTSavings() != tr2.getTo().getDSTSavings()) {
                    return false;
                }
            }
            time = tr1.getTime();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getTimeZoneRules(long)
     */
    public TimeZoneRule[] getTimeZoneRules(long start) {
        TimeZoneRule[] all = getTimeZoneRules();
        TimeZoneTransition tzt = getPreviousTransition(start, true);
        if (tzt == null) {
            // No need to filter out rules only applicable to time before the start
            return all;
        }

        BitSet isProcessed = new BitSet(all.length);
        List filteredRules = new LinkedList();

        // Create initial rule
        InitialTimeZoneRule initial = new InitialTimeZoneRule(tzt.getTo().getName(),
                tzt.getTo().getRawOffset(), tzt.getTo().getDSTSavings());
        filteredRules.add(initial);
        isProcessed.set(0);

        // Mark rules which does not need to be processed
        for (int i = 1; i < all.length; i++) {
            Date d;
            if (all[i] instanceof TimeZoneTransitionRule) {
                d = ((TimeZoneTransitionRule)all[i]).getNextStart(start, initial.getRawOffset(),
                        initial.getDSTSavings(), false);
            } else {
                throw new IllegalStateException("Illegal TimeZoneRule type");
            }
            if (d == null) {
                isProcessed.set(i);
            }
        }

        long time = start;
        boolean bFinalStd = false, bFinalDst = false;
        while(!bFinalStd || !bFinalDst) {
            tzt = getNextTransition(time, false);
            if (tzt == null) {
                break;
            }
            time = tzt.getTime();

            TimeZoneRule toRule = tzt.getTo();
            int ruleIdx = 1;
            for (; ruleIdx < all.length; ruleIdx++) {
                if (all[ruleIdx].equals(toRule)) {
                    break;
                }
            }
            if (ruleIdx >= all.length) {
                throw new IllegalStateException("The rule was not found");
            }
            if (isProcessed.get(ruleIdx)) {
                continue;
            }
            if (toRule instanceof TimeArrayTimeZoneRule) {
                TimeArrayTimeZoneRule tar = (TimeArrayTimeZoneRule)toRule;

                // Get the previous raw offset and DST savings before the very first start time
                long t = start;
                while(true) {
                    tzt = getNextTransition(t, false);
                    if (tzt == null) {
                        break;
                    }
                    if (tzt.getTo().equals(tar)) {
                        break;
                    }
                    t = tzt.getTime();
                }
                if (tzt != null) {
                    // Check if the entire start times to be added
                    Date firstStart = tar.getFirstStart(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings());
                    if (firstStart.getTime() > start) {
                        // Just add the rule as is
                        filteredRules.add(tar);
                    } else {
                        // Collect transitions after the start time
                        long[] times = tar.getStartTimes();
                        int timeType = tar.getTimeType();
                        int idx;
                        for (idx = 0; idx < times.length; idx++) {
                            t = times[idx];
                            if (timeType == DateTimeRule.STANDARD_TIME) {
                                t -= tzt.getFrom().getRawOffset();
                            }
                            if (timeType == DateTimeRule.WALL_TIME) {
                                t -= tzt.getFrom().getDSTSavings();
                            }
                            if (t > start) {
                                break;
                            }
                        }
                        int asize = times.length - idx;
                        if (asize > 0) {
                            long[] newtimes = new long[asize];
                            System.arraycopy(times, idx, newtimes, 0, asize);
                            TimeArrayTimeZoneRule newtar = new TimeArrayTimeZoneRule(tar.getName(),
                                    tar.getRawOffset(), tar.getDSTSavings(), newtimes, tar.getTimeType());
                            filteredRules.add(newtar);
                        }
                    }
                }
            } else if (toRule instanceof AnnualTimeZoneRule) {
                AnnualTimeZoneRule ar = (AnnualTimeZoneRule)toRule;
                Date firstStart = ar.getFirstStart(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings());
                if (firstStart.getTime() == tzt.getTime()) {
                    // Just add the rule as is
                    filteredRules.add(ar);
                } else {
                    // Calculate the transition year
                    int[] dfields = new int[6];
                    Grego.timeToFields(tzt.getTime(), dfields);
                    // Recreate the rule
                    AnnualTimeZoneRule newar = new AnnualTimeZoneRule(ar.getName(), ar.getRawOffset(), ar.getDSTSavings(),
                            ar.getRule(), dfields[0], ar.getEndYear());
                    filteredRules.add(newar);
                }
                // Check if this is a final rule
                if (ar.getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                    // After both final standard and dst rule are processed,
                    // exit this while loop.
                    if (ar.getDSTSavings() == 0) {
                        bFinalStd = true;
                    } else {
                        bFinalDst = true;
                    }
                }
            }
            isProcessed.set(ruleIdx);
        }
        TimeZoneRule[] rules = new TimeZoneRule[filteredRules.size()];
        filteredRules.toArray(rules);
        return rules;
    }

    private static final long MILLIS_PER_YEAR = 365*24*60*60*1000L;

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getSimpleTimeZoneRules(long)
     */
    public TimeZoneRule[] getSimpleTimeZoneRules(long date) {
        AnnualTimeZoneRule[] annualRules = null;
        InitialTimeZoneRule initialRule = null;
        // Get the next transition
        TimeZoneTransition tr = getNextTransition(date, false);
        if (tr != null) {
            String initialName = tr.getFrom().getName();
            int initialRaw = tr.getFrom().getRawOffset();
            int initialDst = tr.getFrom().getDSTSavings();

            // Check if the next transition is either DST->STD or STD->DST and
            // within roughly 1 year from the specified date
            long nextTransitionTime = tr.getTime();
            if (((tr.getFrom().getDSTSavings() == 0 && tr.getTo().getDSTSavings() != 0)
                    || (tr.getFrom().getDSTSavings() != 0 && tr.getTo().getDSTSavings() == 0))
                        && date + MILLIS_PER_YEAR > nextTransitionTime) {
                // Get the next next transition
                annualRules = new AnnualTimeZoneRule[2];
                // Get local wall time for the transition time
                int dtfields[] = Grego.timeToFields(nextTransitionTime + tr.getFrom().getRawOffset() + tr.getFrom().getDSTSavings(), null);
                int weekInMonth = Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]);
                // Create DOW rule
                DateTimeRule dtr = new DateTimeRule(dtfields[1], weekInMonth, dtfields[3], dtfields[5], DateTimeRule.WALL_TIME);
                annualRules[0] = new AnnualTimeZoneRule(tr.getTo().getName(), tr.getTo().getRawOffset(), tr.getTo().getDSTSavings(),
                        dtr, dtfields[0], AnnualTimeZoneRule.MAX_YEAR);

                tr = getNextTransition(nextTransitionTime, false);
                AnnualTimeZoneRule secondRule = null;
                if (tr != null) {
                    // Check if the next next transition is either DST->STD or STD->DST
                    // and within roughly 1 year from the next transition
                    if (((tr.getFrom().getDSTSavings() == 0 && tr.getTo().getDSTSavings() != 0)
                            || (tr.getFrom().getDSTSavings() != 0 && tr.getTo().getDSTSavings() == 0))
                                && nextTransitionTime + MILLIS_PER_YEAR > tr.getTime()) {
                        // Generate another DOW rule
                        dtfields = Grego.timeToFields(tr.getTime() + tr.getFrom().getRawOffset() + tr.getFrom().getDSTSavings(), dtfields);
                        weekInMonth = Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]);
                        dtr = new DateTimeRule(dtfields[1], weekInMonth, dtfields[3], dtfields[5], DateTimeRule.WALL_TIME);
                        secondRule = new AnnualTimeZoneRule(tr.getTo().getName(), tr.getTo().getRawOffset(), tr.getTo().getDSTSavings(),
                                dtr, dtfields[0] - 1, AnnualTimeZoneRule.MAX_YEAR);
                        // Make sure this rule can be applied to the specified date
                        Date d = secondRule.getPreviousStart(date, tr.getFrom().getRawOffset(), tr.getFrom().getDSTSavings(), true);
                        if (d != null && d.getTime() <= date
                                && initialRaw == tr.getTo().getRawOffset()
                                && initialDst == tr.getTo().getDSTSavings()) {
                            // We can use this rule as the second transition rule
                            annualRules[1] = secondRule;
                        }
                    }
                }
                if (annualRules[1] == null) {
                    // Try previous transition
                    tr = getPreviousTransition(date, true);
                    if (tr != null) {
                        // Check if the previous transition is either DST->STD or STD->DST.
                        // The actual transition time does not matter here.
                        if ((tr.getFrom().getDSTSavings() == 0 && tr.getTo().getDSTSavings() != 0)
                                || (tr.getFrom().getDSTSavings() != 0 && tr.getTo().getDSTSavings() == 0)) {
                            // Generate another DOW rule
                            dtfields = Grego.timeToFields(tr.getTime() + tr.getFrom().getRawOffset() + tr.getFrom().getDSTSavings(), dtfields);
                            weekInMonth = Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]);
                            dtr = new DateTimeRule(dtfields[1], weekInMonth, dtfields[3], dtfields[5], DateTimeRule.WALL_TIME);
                            secondRule = new AnnualTimeZoneRule(tr.getTo().getName(), tr.getTo().getRawOffset(), tr.getTo().getDSTSavings(),
                                    dtr, annualRules[0].getStartYear() - 1, AnnualTimeZoneRule.MAX_YEAR);
                            // Check if this rule start after the first rule after the specified date
                            Date d = secondRule.getNextStart(date, tr.getFrom().getRawOffset(), tr.getFrom().getDSTSavings(), false);
                            if (d.getTime() > nextTransitionTime) {
                                // We can use this rule as the second transition rule
                                annualRules[1] = secondRule;
                            }
                        }
                    }
                }
                if (annualRules[1] == null) {
                    // Cannot generate a good pair of AnnualTimeZoneRule
                    annualRules = null;
                } else {
                    // The initial rule should represent the rule before the previous transition
                    initialName = annualRules[0].getName();
                    initialRaw = annualRules[0].getRawOffset();
                    initialDst = annualRules[0].getDSTSavings();
                }
            }
            initialRule = new InitialTimeZoneRule(initialName, initialRaw, initialDst);
        } else {
            // Try the previous one
            tr = getPreviousTransition(date, true);
            if (tr != null) {
                initialRule = new InitialTimeZoneRule(tr.getTo().getName(),
                        tr.getTo().getRawOffset(), tr.getTo().getDSTSavings());
            } else {
                // No transitions in the past.  Just use the current offsets
                int[] offsets = new int[2];
                getOffset(date, false, offsets);
                initialRule = new InitialTimeZoneRule(getID(), offsets[0], offsets[1]);
            }
        }

        TimeZoneRule[] result = null;
        if (annualRules == null) {
            result = new TimeZoneRule[1];
            result[0] = initialRule;
        } else {
            result = new TimeZoneRule[3];
            result[0] = initialRule;
            result[1] = annualRules[0];
            result[2] = annualRules[1];
        }

        return result;
    }
}
