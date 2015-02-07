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
public class SignificantDigitInterval extends ValueObject<SignificantDigitInterval> {
    
    private int max = Integer.MAX_VALUE;
    private int min = 0;
    
    public static final SignificantDigitInterval DEFAULT = new SignificantDigitInterval().freeze();
    
    public void setMax(int count) {
        checkThawed();
        max = count <= 0 ? Integer.MAX_VALUE : count;
    }
    
    public int getMax() { return max; }
    
    public void setMin(int count) {
        min = count <= 0 ? 0 : count;
    }
    
    public int getMin() { return min; }
}
