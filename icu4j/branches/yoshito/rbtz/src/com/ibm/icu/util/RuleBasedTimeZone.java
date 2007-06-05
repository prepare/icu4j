/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.impl.Grego;
import com.ibm.icu.impl.ICUTimeZone;

/**
 * RuleBasedTimeZone is a concrete subclass of TimeZone that allows users to define
 * custom historic time transition rules.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class RuleBasedTimeZone extends ICUTimeZone {

    private static final long serialVersionUID = 7580833058949327935L;

    private final InitialTimeZoneRule initialRule;
    private List historicRules;
    private AnnualTimeZoneRule[] finalRules;

    private transient List historicTransitions;
    private transient boolean upToDate;

    private static final int MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * Constructs a RuleBasedTimeZone object with the ID, initial name, standard offset
     * and daylight saving amount
     * 
     * @param id                The time zone ID.
     * @param initialRule       The initial time zone rule.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
    */
    public RuleBasedTimeZone(String id, InitialTimeZoneRule initialRule) {
        super.setID(id);
        this.initialRule = initialRule;
    }

    /**
     * Adds the TimeZoneTransitionRule which represents time transitions.
     * 
     * @param rule The TimeZoneTransitionRule.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public void addTransitionRule(TimeZoneTransitionRule rule) {
        if (rule instanceof AnnualTimeZoneRule
                && ((AnnualTimeZoneRule)rule).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
            // One of the final rules applicable in future forever
            if (finalRules == null) {
                finalRules = new AnnualTimeZoneRule[2];
                finalRules[0] = (AnnualTimeZoneRule)rule;
            } else if (finalRules[1] == null) {
                finalRules[1] = (AnnualTimeZoneRule)rule;
            } else {
                // Only a pair of AnnualTimeZoneRule is allowed.
                throw new IllegalStateException("Too many final rules");
            }
        } else {
            // If this is not a final rule, add it to the historic rule list 
            if (historicRules == null) {
                historicRules = new ArrayList();
            }
            historicRules.add(rule);
        }
        // Mark dirty, so transitions are recalculated when offset information is
        // accessed next time.
        upToDate = false;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getTimeZoneRules()
     */
    public TimeZoneRule[] getTimeZoneRules() {
        int size = 1;
        if (historicRules != null) {
            size += historicRules.size();
        }

        if (finalRules != null) {
            if (finalRules[1] != null) {
                size += 2;
            } else {
                size++;
            }
        }
        TimeZoneRule[] rules = new TimeZoneRule[size];
        rules[0] = initialRule;
        
        int idx = 1;
        if (historicRules != null) {
            for (; idx < historicRules.size() + 1; idx++) {
                rules[idx] = (TimeZoneRule)historicRules.get(idx - 1);
            }
        }
        if (finalRules != null) {
            rules[idx++] = finalRules[0];
            if (finalRules[1] != null) {
                rules[idx] = finalRules[1];
            }
        }
        return rules;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(int, int, int, int, int, int)
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int milliseconds) {
        if (era == GregorianCalendar.BC) {
            // Convert to extended year
            year = 1 - year;
        }
        long time = Grego.fieldsToDay(year, month, day) * MILLIS_PER_DAY + milliseconds;
        int[] offsets = new int[2];
        getOffset(time, true, offsets);
        return (offsets[0] + offsets[1]);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(long, boolean, int[])
     */
    public void getOffset(long time, boolean local, int[] offsets) {
        update();
        TimeZoneRule rule;
        if (historicTransitions == null) {
            rule = initialRule;
        } else {
            long tstart = getTransitionTime((TimeZoneTransition)historicTransitions.get(0), local);
            if (time < tstart) {
                rule = initialRule;
            } else {
                int idx = historicTransitions.size() - 1;
                long tend = getTransitionTime((TimeZoneTransition)historicTransitions.get(idx), local);
                if (time > tend) {
                    if (finalRules != null) {
                        rule = findRuleInFinal(time, local);
                    } else {
                        // no final rule, use the last rule
                        rule = ((TimeZoneTransition)historicTransitions.get(idx)).getTo();
                    }
                } else {
                    // Find a historical transition
                    while (idx >= 0) {
                        if (time >= getTransitionTime((TimeZoneTransition)historicTransitions.get(idx), local)) {
                            break;
                        }
                        idx--;
                    }
                    rule = ((TimeZoneTransition)historicTransitions.get(idx)).getTo();
                }
            }
        }
        offsets[0] = rule.getRawOffset();
        offsets[1] = rule.getDSTSavings();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getRawOffset()
     */
    public int getRawOffset() {
        // Note: This implementation returns standard GMT offset
        // as of current time.
        long now = System.currentTimeMillis();
        int[] offsets = new int[2];
        getOffset(now, false, offsets);
        return offsets[0];
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#inDaylightTime(java.util.Date)
     */
    public boolean inDaylightTime(Date date) {
        int[] offsets = new int[2];
        getOffset(date.getTime(), false, offsets);
        return (offsets[1] != 0);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#setRawOffset(int)
     */
    public void setRawOffset(int offsetMillis) {
        // TODO: Do nothing for now..
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#useDaylightTime()
     */
    public boolean useDaylightTime() {
        // Note: This implementation returns true when
        // daylight saving time is used as of now or
        // after the next transition.
        long now = System.currentTimeMillis();
        TimeZoneTransition tt = getNextTransition(now, false);
        if (tt != null && (tt.getTo().getDSTSavings() != 0 || tt.getFrom().getDSTSavings() != 0)) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#hasSameRules(com.ibm.icu.util.TimeZone)
     */
    public boolean hasSameRules(TimeZone other) {
        if (!(other instanceof RuleBasedTimeZone)) {
            // We cannot reasonably compare rules in different types
            return false;
        }
        RuleBasedTimeZone otherRBTZ = (RuleBasedTimeZone)other;

        // initial rule
        if (!initialRule.isSameAs(otherRBTZ.initialRule)) {
            return false;
        }

        // final rules
        if (finalRules != null && otherRBTZ.finalRules != null) {
            for (int i = 0; i < finalRules.length; i++) {
                if (finalRules[i] == null && otherRBTZ.finalRules[i] == null) {
                    continue;
                }
                if (finalRules[i] != null && otherRBTZ.finalRules[i] != null
                        && finalRules[i].isSameAs(otherRBTZ.finalRules[i])) {
                    continue;
                    
                }
                return false;
            }
        } else if (finalRules != null || otherRBTZ.finalRules != null) {
            return false;
        }

        // historic rules
        if (historicRules != null && otherRBTZ.historicRules != null) {
            if (historicRules.size() != otherRBTZ.historicRules.size()) {
                return false;
            }
            Iterator it = historicRules.iterator();
            while (it.hasNext()) {
                TimeZoneRule rule = (TimeZoneRule)it.next();
                Iterator oit = otherRBTZ.historicRules.iterator();
                boolean foundSameRule = false;
                while (oit.hasNext()) {
                    TimeZoneRule orule = (TimeZoneRule)oit.next();
                    if (rule.isSameAs(orule)) {
                        foundSameRule = true;
                        break;
                    }
                }
                if (!foundSameRule) {
                    return false;
                }
            }
        } else if (historicRules != null || otherRBTZ.historicRules != null) {
            return false;
        }
        return true;
    }

    // HasReadableTimeZoneTransition implementation

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getNextTransition(long, boolean)
     */
    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        update();
        if (historicTransitions == null) {
            return null;
        }
        TimeZoneTransition result = null;
        TimeZoneTransition tzt = (TimeZoneTransition)historicTransitions.get(0);
        long tt = getTransitionTime(tzt, false);
        if (tt > base || (inclusive && tt == base)) {
            result = tzt;
        } else {
            int idx = historicTransitions.size() - 1;        
            tzt = (TimeZoneTransition)historicTransitions.get(idx);
            tt = getTransitionTime(tzt, false);
            if (inclusive && tt == base) {
                return tzt;
            } else if (tt <= base) {
                if (finalRules != null) {
                    // Find a transion time with finalRules
                    Date start0 = finalRules[0].getNextStart(base,
                            finalRules[1].getRawOffset(), finalRules[1].getDSTSavings(), inclusive);
                    Date start1 = finalRules[1].getNextStart(base,
                            finalRules[0].getRawOffset(), finalRules[0].getDSTSavings(), inclusive);

                    if (start1.after(start0)) {
                        tzt = new TimeZoneTransition(start0.getTime(), finalRules[1], finalRules[0]);
                    } else {
                        tzt = new TimeZoneTransition(start1.getTime(), finalRules[0], finalRules[1]);
                    }
                    result = tzt;
                } else {
                    return null;
                }
            } else {
                // Find a transition within the historic transitions
                idx--;
                TimeZoneTransition prev = tzt;
                while (idx > 0) {
                    tzt = (TimeZoneTransition)historicTransitions.get(idx);
                    tt = getTransitionTime(tzt, false);
                    if (tt < base || (!inclusive && tt == base)) {
                        break;
                    }
                    idx--;
                    prev = tzt;
                }
                result = prev;
            }
        }
        if (result != null) {
            // For now, this implementation ignore transitions with only zone name changes.
            TimeZoneRule from = result.getFrom();
            TimeZoneRule to = result.getTo();
            if (from.getRawOffset() == to.getRawOffset()
                    && from.getDSTSavings() == to.getDSTSavings()) {
                // No offset changes.  Try next one
                result = getNextTransition(result.getTime(), false /* always exclusive */);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getPreviousTransition(long, boolean)
     */
    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        update();
        if (historicTransitions == null) {
            return null;
        }
        TimeZoneTransition result = null;
        TimeZoneTransition tzt = (TimeZoneTransition)historicTransitions.get(0);
        long tt = getTransitionTime(tzt, false);
        if (inclusive && tt == base) {
            result = tzt;
        } else if (tt >= base) {
            return null;
        } else {
            int idx = historicTransitions.size() - 1;        
            tzt = (TimeZoneTransition)historicTransitions.get(idx);
            tt = getTransitionTime(tzt, false);
            if (inclusive && tt == base) {
                result = tzt;
            } else if (tt < base) {
                if (finalRules != null) {
                    // Find a transion time with finalRules
                    Date start0 = finalRules[0].getPreviousStart(base,
                            finalRules[1].getRawOffset(), finalRules[1].getDSTSavings(), inclusive);
                    Date start1 = finalRules[1].getPreviousStart(base,
                            finalRules[0].getRawOffset(), finalRules[0].getDSTSavings(), inclusive);

                    if (start1.before(start0)) {
                        tzt = new TimeZoneTransition(start0.getTime(), finalRules[1], finalRules[0]);
                    } else {
                        tzt = new TimeZoneTransition(start1.getTime(), finalRules[0], finalRules[1]);
                    }
                }
                result = tzt;
            } else {
                // Find a transition within the historic transitions
                idx--;
                while (idx >= 0) {
                    tzt = (TimeZoneTransition)historicTransitions.get(idx);
                    tt = getTransitionTime(tzt, false);
                    if (tt < base || (inclusive && tt == base)) {
                        break;
                    }
                    idx--;
                }
                result = tzt;                
            }
        }
        if (result != null) {
            // For now, this implementation ignore transitions with only zone name changes.
            TimeZoneRule from = result.getFrom();
            TimeZoneRule to = result.getTo();
            if (from.getRawOffset() == to.getRawOffset()
                    && from.getDSTSavings() == to.getDSTSavings()) {
                // No offset changes.  Try previous one
                result = getPreviousTransition(result.getTime(), false /* always exclusive */);
            }
        }
        return result;
    }
    
    // private stuff
    private void update() {
        if (upToDate) {
            // No rules were added since last time.
            return;
        }

        // Make sure either no final rules or a pair of AnnualTimeZoneRules
        // are available.
        if (finalRules != null && finalRules[1] == null) {
            throw new IllegalStateException("Incomplete final rules");
        }

        // Create a TimezoneTransition and add to the list
        if (historicRules != null || finalRules != null) {
            TimeZoneRule curRule = initialRule;
            long lastTransitionTime = Grego.MIN_MILLIS;

            // Build the transition array which represents historical time zone
            // transitions.
            if (historicRules != null) {
                BitSet done = new BitSet(historicRules.size()); // for skipping rules already processed

                while (true) {
                    int curStdOffset = curRule.getRawOffset();
                    int curDstSavings = curRule.getDSTSavings();
                    long nextTransitionTime = Grego.MAX_MILLIS;
                    TimeZoneRule nextRule = null;
                    Date d;
                    long tt;

                    for (int i = 0; i < historicRules.size(); i++) {
                        if (done.get(i)) {
                            continue;
                        }
                        TimeZoneTransitionRule r = (TimeZoneTransitionRule)historicRules.get(i);
                        d = r.getNextStart(lastTransitionTime, curStdOffset, curDstSavings, false);
                        if (d == null) {
                            // No more transitions from this rule - skip this rule next time
                            done.set(i);
                        } else {
                            if (r == curRule ||
                                    (r.getName().equals(curRule.getName())
                                            && r.getRawOffset() == curRule.getRawOffset()
                                            && r.getDSTSavings() == curRule.getDSTSavings())) {
                                continue;
                            }
                            tt = d.getTime();
                            if (tt < nextTransitionTime) {
                                nextTransitionTime = tt;
                                nextRule = r;
                            }
                        }
                    }

                    if (nextRule ==  null) {
                        // Check if all historic rules are done
                        boolean bDoneAll = true;
                        for (int j = 0; j < historicRules.size(); j++) {
                            if (!done.get(j)) {
                                bDoneAll = false;
                                break;
                            }
                        }
                        if (bDoneAll) {
                            break;
                        }
                    }

                    if (finalRules != null) {
                        // Check if one of final rules has earlier transition date
                        for (int i = 0; i < 2 /* finalRules.length */; i++) {
                            if (finalRules[i] == curRule) {
                                continue;
                            }
                            d = finalRules[i].getNextStart(lastTransitionTime, curStdOffset, curDstSavings, false);
                            if (d != null) {
                                tt = d.getTime();
                                if (tt < nextTransitionTime) {
                                    nextTransitionTime = tt;
                                    nextRule = finalRules[i];
                                }
                            }
                        }
                    }

                    if (nextRule == null) {
                        // Nothing more
                        break;
                    }

                    if (historicTransitions == null) {
                        historicTransitions = new ArrayList();
                    }
                    historicTransitions.add(new TimeZoneTransition(nextTransitionTime, curRule, nextRule));
                    lastTransitionTime = nextTransitionTime;
                    curRule = nextRule;
                }
            }
            if (finalRules != null) {
                if (historicTransitions == null) {
                    historicTransitions = new ArrayList();
                }
                // Append the first transition for each
                Date d0 = finalRules[0].getNextStart(lastTransitionTime, curRule.getRawOffset(), curRule.getDSTSavings(), false);
                Date d1 = finalRules[1].getNextStart(lastTransitionTime, curRule.getRawOffset(), curRule.getDSTSavings(), false);
                if (d1.after(d0)) {
                    historicTransitions.add(new TimeZoneTransition(d0.getTime(), curRule, finalRules[0]));
                    d1 = finalRules[1].getNextStart(d0.getTime(), finalRules[0].getRawOffset(), finalRules[0].getDSTSavings(), false);
                    historicTransitions.add(new TimeZoneTransition(d1.getTime(), finalRules[0], finalRules[1]));
                } else {
                    historicTransitions.add(new TimeZoneTransition(d1.getTime(), curRule, finalRules[1]));
                    d0 = finalRules[0].getNextStart(d1.getTime(), finalRules[1].getRawOffset(), finalRules[1].getDSTSavings(), false);
                    historicTransitions.add(new TimeZoneTransition(d0.getTime(), finalRules[1], finalRules[0]));
                }
            }
        }
        upToDate = true;
    }

    private TimeZoneRule findRuleInFinal(long time, boolean local) {
        if (finalRules == null || finalRules.length != 2 || finalRules[0] == null || finalRules[1] == null) {
            return null;
        }

        Date start0, start1;
        long base;

        base = local ? time - finalRules[1].getRawOffset() - finalRules[1].getDSTSavings() : time;
        start0 = finalRules[0].getPreviousStart(base, finalRules[1].getRawOffset(), finalRules[1].getDSTSavings(), true);
 
        base = local ? time - finalRules[0].getRawOffset() - finalRules[0].getDSTSavings() : time;
        start1 = finalRules[1].getPreviousStart(base, finalRules[0].getRawOffset(), finalRules[0].getDSTSavings(), true);

        return start0.after(start1) ? finalRules[0] : finalRules[1];
    }

    private static long getTransitionTime(TimeZoneTransition tzt, boolean local) {
        long time = tzt.getTime();
        if (local) {
            time += tzt.getFrom().getRawOffset() + tzt.getFrom().getDSTSavings();
        }
        return time;
    }
}

