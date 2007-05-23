/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;

import com.ibm.icu.util.AnnualTimeZoneRule;
import com.ibm.icu.util.HasTimeZoneRules;
import com.ibm.icu.util.InitialTimeZoneRule;
import com.ibm.icu.util.TimeArrayTimeZoneRule;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZoneRule;
import com.ibm.icu.util.TimeZoneTransition;

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

        if (offsets1[0] != offsets2[0] || offsets1[1] != offsets2[1]) {
            return false;
        }

        // Check transitions in the range
        long time = start;
        while (true) {
            TimeZoneTransition tr1 = getNextTransition(time, false);
            TimeZoneTransition tr2 = ((HasTimeZoneRules)tz).getNextTransition(time, false);

            if (tr1 == null && tr2 == null) {
                break;
            }
            if ((tr1 == null && tr2.getTime() <= end) || (tr2 == null && tr1.getTime() <= end)) {
                return false;
            }
            if (tr1.getTime() > end) {
                if (tr2.getTime() <= end) {
                    return false;
                }
                break;
            }                
            if (tr1.getTime() != tr2.getTime() ||
                    tr1.getTo().getRawOffset() != tr2.getTo().getRawOffset() ||
                    tr1.getTo().getDSTSavings() != tr2.getTo().getDSTSavings()) {
                return false;
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
        ArrayList filteredRules = new ArrayList();

        // Create initial rule
        InitialTimeZoneRule initial = new InitialTimeZoneRule(tzt.getTo().getName(),
                tzt.getTo().getRawOffset(), tzt.getTo().getDSTSavings());
        filteredRules.add(initial);
        isProcessed.set(0);

        // Mark rules which does not need to be processed
        for (int i = 1; i < all.length; i++) {
            Date d;
            if (all[i] instanceof TimeArrayTimeZoneRule) {
                d = ((TimeArrayTimeZoneRule)all[i]).getNextStart(start, false);
            } else if (all[i] instanceof AnnualTimeZoneRule) {
                d = ((AnnualTimeZoneRule)all[i]).getNextStart(start, initial.getRawOffset(),
                        initial.getDSTSavings(), false);
            } else {
                throw new IllegalStateException("Unknown TimeZoneRule type");
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
                Date firstStart = tar.getFirstStart();
                if (firstStart.getTime() > start) {
                    // Just add the rule as is
                    filteredRules.add(tar);
                } else {
                    // Collect transitions after the start time
                    long[] times = tar.getStartTimes();
                    int idx;
                    for (idx = 0; idx < times.length; idx++) {
                        if (times[idx] > start) {
                            break;
                        }
                    }
                    int asize = times.length - idx;
                    if (asize > 0) {
                        long[] newtimes = new long[asize];
                        System.arraycopy(times, idx, newtimes, 0, asize);
                        TimeArrayTimeZoneRule newtar = new TimeArrayTimeZoneRule(tar.getName(),
                                tar.getRawOffset(), tar.getDSTSavings(), newtimes);
                        filteredRules.add(newtar);
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
                    int[] dfields = new int[5];
                    Grego.dayToFields(Grego.timeToDay(tzt.getTime()), dfields);
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
}
