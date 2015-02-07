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
    public static final FixedPrecision DEFAULT = new FixedPrecision().freeze();
    
    private static final DigitInterval MIN_INTERVAL = new DigitInterval(1, 0).freeze();
    
    private DigitInterval min = MIN_INTERVAL;
    private DigitInterval max = DigitInterval.DEFAULT;
    private SignificantDigitInterval sig = SignificantDigitInterval.DEFAULT;
    
    public DigitInterval getMin() { return min; }
    public DigitInterval getMutableMin() {
        min = safeThaw(min);
        return min;
    }
    public void setMin(DigitInterval interval) {
        this.min = safeSet(interval);
    }
    
    public DigitInterval getMax() { return max; }
    public DigitInterval getMutableMax() {
        max = safeThaw(max);
        return max;
    }
    public void setMax(DigitInterval interval) {
        this.max = safeSet(interval);
    }
    
    public SignificantDigitInterval getSig() { return sig; }
    public SignificantDigitInterval getMutableSig() {
        sig = safeThaw(sig);
        return sig;
    }
    public void setSig(SignificantDigitInterval interval) {
        this.sig = safeSet(interval);
    }
    
    public FixedPrecision clone() {
        FixedPrecision result = super.clone();
        result.min.freeze();
        result.max.freeze();
        result.sig.freeze();
        return result;
    }
}
