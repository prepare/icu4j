/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.impl;


public class ScientificPrecision extends ValueObject<ScientificPrecision> {
    
    private FixedPrecision mantissa = FixedPrecision.DEFAULT;
    
    public static final ScientificPrecision DEFAULT = new ScientificPrecision().freeze();
    
    public FixedPrecision getMantissa() { return mantissa; }
    public FixedPrecision getMutableMantissa() {
        checkThawed();
        mantissa = thaw(mantissa);
        return mantissa;
    }
    public void setMantissa(FixedPrecision m) {
        checkThawed();
        this.mantissa = m.freeze();
    }
    
    @Override
    protected void freezeValueFields() {
        mantissa.freeze();
    }
    
    @Override
    public String toString() {
        return String.format("{Mantissa: %s}", mantissa.toString());
    }

}
