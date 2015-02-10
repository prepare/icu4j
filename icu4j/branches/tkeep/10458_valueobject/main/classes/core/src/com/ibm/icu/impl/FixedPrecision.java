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
public class FixedPrecision extends ValueObject<FixedPrecision> {
    
    private static final DigitInterval MIN_INTERVAL = new DigitInterval(1, 0).freeze();
    
    private DigitInterval min = MIN_INTERVAL;
    private DigitInterval max = DigitInterval.DEFAULT;
    private SignificantDigitInterval sig = SignificantDigitInterval.DEFAULT;
    
    public static final FixedPrecision DEFAULT = new FixedPrecision().freeze();
    
    public DigitInterval getMin() { return min; }
    public DigitInterval getMutableMin() {
        checkThawed();
        min = thaw(min);
        return min;
    }
    public void setMin(DigitInterval interval) {
        checkThawed();
        this.min = interval.freeze();
    }
    
    public DigitInterval getMax() { return max; }
    public DigitInterval getMutableMax() {
        checkThawed();
        max = thaw(max);
        return max;
    }
    public void setMax(DigitInterval interval) {
        checkThawed();
        this.max = interval.freeze();
    }
    
    public SignificantDigitInterval getSig() { return sig; }
    public SignificantDigitInterval getMutableSig() {
        checkThawed();
        sig = thaw(sig);
        return sig;
    }
    public void setSig(SignificantDigitInterval interval) {
        checkThawed();
        this.sig = interval.freeze();
    }
    
    @Override
    protected void freezeValueFields() {
        min.freeze();
        max.freeze();
        sig.freeze();
    }
    
    @Override
    public String toString() {
        return String.format("{min: %s, max: %s, sig: %s}", min, max, sig);
    }
   
}
