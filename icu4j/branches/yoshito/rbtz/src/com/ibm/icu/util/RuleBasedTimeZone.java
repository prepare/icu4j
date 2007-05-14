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
import java.util.List;

import com.ibm.icu.impl.Grego;

/**
 * RuleBasedTimeZone is a concrete subclass of TimeZone that allows users to define
 * custom historic time transition rules.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class RuleBasedTimeZone extends TimeZone implements HasTimeZoneTransitions {

    private static final long serialVersionUID = 1L; //TODO

    private final TimeZoneRule initialRule;
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
     * @param initialName       The name of initial time.
     * @param initialStdOffset  The initial UTC offset in milliseconds.
     * @param initialDstSaving  The initial daylight saving amount in milliseconds.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
    */
    public RuleBasedTimeZone(String id, String initialName, int initialStdOffset, int initialDstSaving) {
        super.setID(id);
        initialRule = new TimeZoneRule(initialName, initialStdOffset, initialDstSaving);
    }

    /**
     * Adds a TimeZoneRule.
     * 
     * @param rule A TimeZoneRule.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public void addRule(TimeZoneRule rule) {
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
                    rule = findRuleInFinal(time, local);
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
        offsets[0] = rule.getStdOffset();
        offsets[1] = rule.getDstSaving();
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
        TimeZoneTransition tt = getNextTransition(now);
        if (tt != null && (tt.getTo().getDstSaving() != 0 || tt.getFrom().getDstSaving() != 0)) {
            return true;
        }
        return false;
    }

    // HasReadableTimeZoneTransition implementation

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneTransitions#getNextTransition(long)
     */
    public TimeZoneTransition getNextTransition(long base) {
        update();
        if (historicTransitions == null) {
            return null;
        }
        TimeZoneTransition tt = (TimeZoneTransition)historicTransitions.get(0);
        if (getTransitionTime(tt, true) > base) {
            return tt;
        }
        int idx = historicTransitions.size() - 1;        
        tt = (TimeZoneTransition)historicTransitions.get(idx);
        if (getTransitionTime(tt, true) <= base) {
            if (finalRules != null) {
                // Find a transion time with finalRules
                Date start0 = finalRules[0].getNextStart(base,
                        finalRules[1].getStdOffset(), finalRules[1].getDstSaving(), false);
                Date start1 = finalRules[1].getNextStart(base,
                        finalRules[0].getStdOffset(), finalRules[0].getDstSaving(), false);

                if (start1.after(start0)) {
                    tt = new TimeZoneTransition(start0.getTime(), finalRules[1], finalRules[0]);
                } else {
                    tt = new TimeZoneTransition(start1.getTime(), finalRules[0], finalRules[1]);
                }
            } else {
                return null;
            }
        }
        // Find a transition within the historic transitions
        idx--;
        TimeZoneTransition prev = tt;
        while (idx > 0) {
            tt = (TimeZoneTransition)historicTransitions.get(idx);
            if (getTransitionTime(tt, true) < base) {
                break;
            }
            idx--;
            prev = tt;
        }
        return prev;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneTransitions#getPreviousTransition(long)
     */
    public TimeZoneTransition getPreviousTransition(long base) {
        update();
        if (historicTransitions == null) {
            return null;
        }
        TimeZoneTransition tt = (TimeZoneTransition)historicTransitions.get(0);
        if (getTransitionTime(tt, true) <= base) {
            return null;
        }
        int idx = historicTransitions.size() - 1;        
        tt = (TimeZoneTransition)historicTransitions.get(idx);
        if (getTransitionTime(tt, true) < base) {
            if (finalRules != null) {
                // Find a transion time with finalRules
                Date start0 = finalRules[0].getLastStart(base,
                        finalRules[1].getStdOffset(), finalRules[1].getDstSaving(), false);
                Date start1 = finalRules[1].getLastStart(base,
                        finalRules[0].getStdOffset(), finalRules[0].getDstSaving(), false);

                if (start1.before(start0)) {
                    tt = new TimeZoneTransition(start0.getTime(), finalRules[1], finalRules[0]);
                } else {
                    tt = new TimeZoneTransition(start1.getTime(), finalRules[0], finalRules[1]);
                }
            } else {
                return tt;
            }
        }
        // Find a transition within the historic transitions
        idx--;
        while (idx >= 0) {
            tt = (TimeZoneTransition)historicTransitions.get(idx);
            if (getTransitionTime(tt, true) < base) {
                break;
            }
            idx--;
        }
        return tt;
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
            historicTransitions = new ArrayList();

            TimeZoneRule curRule = initialRule;
            long lastTransitionTime = Grego.MIN_MILLIS;

            // Build the transition array which represents historical time zone
            // transitions.
            if (historicRules != null) {
                BitSet done = new BitSet(historicRules.size()); // for skipping rules already processed

                while (true) {
                    int curStdOffset = curRule.getStdOffset();
                    int curDstSaving = curRule.getDstSaving();
                    long nextTransitionTime = Grego.MAX_MILLIS;
                    TimeZoneRule nextRule = null;
                    Date d;
                    long tt;

                    for (int i = 0; i < historicRules.size(); i++) {
                        if (done.get(i)) {
                            continue;
                        }
                        TimeZoneRule r = (TimeZoneRule)historicRules.get(i);
                        if (r instanceof AnnualTimeZoneRule) {
                            d = ((AnnualTimeZoneRule)r).getNextStart(lastTransitionTime, curStdOffset, curDstSaving, false);
                        } else if (r instanceof TimeArrayTimeZoneRule) {
                            d = ((TimeArrayTimeZoneRule)r).getNextStart(lastTransitionTime);
                        } else {
                            throw new IllegalStateException("Unknow time zone rule type");
                        }
                        if (d == null) {
                            // No more transitions from this rule - skip this rule next time
                            done.set(i);
                        } else {
                            tt = d.getTime();
                            if (tt < nextTransitionTime) {
                                nextTransitionTime = tt;
                                nextRule = r;
                            }
                        }
                    }

                    if (nextRule == null) {
                        // All historic rules were processed.
                        break;
                    }

                    if (finalRules != null) {
                        // Check if one of final rules has earlier transition date
                        for (int i = 0; i < 2 /* finalRules.length */; i++) {
                            d = finalRules[i].getNextStart(lastTransitionTime, curStdOffset, curDstSaving, false);
                            if (d != null) {
                                tt = d.getTime();
                                if (tt < nextTransitionTime) {
                                    nextTransitionTime = tt;
                                    nextRule = finalRules[i];
                                }
                            }
                        }
                    }

                    historicTransitions.add(new TimeZoneTransition(nextTransitionTime, curRule, nextRule));
                    lastTransitionTime = nextTransitionTime;
                    curRule = nextRule;
                }
            }
            if (finalRules != null) {
                // Append the first transition for each
                Date d0 = finalRules[0].getNextStart(lastTransitionTime, curRule.getStdOffset(), curRule.getDstSaving(), false);
                Date d1 = finalRules[1].getNextStart(lastTransitionTime, curRule.getStdOffset(), curRule.getDstSaving(), false);
                if (d1.after(d0)) {
                    historicTransitions.add(new TimeZoneTransition(d0.getTime(), curRule, finalRules[0]));
                    d1 = finalRules[1].getNextStart(d0.getTime(), finalRules[0].getStdOffset(), finalRules[0].getDstSaving(), false);
                    historicTransitions.add(new TimeZoneTransition(d1.getTime(), finalRules[0], finalRules[1]));
                } else {
                    historicTransitions.add(new TimeZoneTransition(d1.getTime(), curRule, finalRules[1]));
                    d0 = finalRules[0].getNextStart(d1.getTime(), finalRules[1].getStdOffset(), finalRules[1].getDstSaving(), false);
                    historicTransitions.add(new TimeZoneTransition(d0.getTime(), finalRules[1], finalRules[0]));
                }
            }
        }
        upToDate = true;
    }

    private TimeZoneRule findRuleInFinal(long time, boolean local) {
        Date start0, start1;
        long base;

        base = local ? time - finalRules[1].getStdOffset() - finalRules[1].getDstSaving() : time;
        start0 = finalRules[0].getLastStart(base, finalRules[1].getStdOffset(), finalRules[1].getDstSaving(), true);
 
        base = local ? time - finalRules[0].getStdOffset() - finalRules[0].getDstSaving() : time;
        start1 = finalRules[1].getLastStart(base, finalRules[0].getStdOffset(), finalRules[0].getDstSaving(), true);

        return start0.after(start1) ? finalRules[0] : finalRules[1];
    }

    private static long getTransitionTime(TimeZoneTransition tzt, boolean local) {
        long time = tzt.getTime();
        if (local) {
            time += tzt.getFrom().getStdOffset() + tzt.getFrom().getDstSaving();
        }
        return time;
    }
}

