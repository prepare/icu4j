/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

/**
 * @author rocketman
 *
 */
public class DigitInterval extends ValueObject<DigitInterval> {
    private int largestExclusive = Integer.MAX_VALUE;
    private int smallestInclusive = Integer.MIN_VALUE;
    
    public static final DigitInterval DEFAULT = new DigitInterval().freeze();
    
    public DigitInterval() {       
    }
    
    public DigitInterval(int intCount, int fracCount) {
        setIntDigitCount(intCount);
        setFracDigitCount(fracCount);
    }
    
    public void expandToContain(DigitInterval rhs) {
        checkThawed();
        if (smallestInclusive > rhs.smallestInclusive) {
            smallestInclusive = rhs.smallestInclusive;
        }
        if (largestExclusive < rhs.largestExclusive) {
            largestExclusive = rhs.largestExclusive;
        }
    
    }
    
    public void setIntDigitCount(int count) {
        checkThawed();
        largestExclusive = count < 0 ? Integer.MAX_VALUE : count;
    }
    
    public void setFracDigitCount(int count) {
        checkThawed();
        smallestInclusive = count < 0 ? Integer.MIN_VALUE : -count;
    }
    
    public int getIntDigitCount() { return largestExclusive; }
    
    public int getFracDigitCount() {
        return smallestInclusive == Integer.MIN_VALUE ? Integer.MAX_VALUE : -smallestInclusive;
    }
    
    @Override
    public String toString() {
        return String.format("{smallestInclusive: %d, largestExclusive: %d", smallestInclusive, largestExclusive);
    }
}
