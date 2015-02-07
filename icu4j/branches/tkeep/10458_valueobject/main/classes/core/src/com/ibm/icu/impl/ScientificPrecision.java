/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.impl;


public class ScientificPrecision extends ValueObject<ScientificPrecision> {
    public static final ScientificPrecision DEFAULT = new ScientificPrecision().freeze();
    
    private FixedPrecision mantissa = FixedPrecision.DEFAULT;
    
    public FixedPrecision getMantissa() { return mantissa; }
    public FixedPrecision getMutableMantissa() {
        mantissa = safeThaw(mantissa);
        return mantissa;
    }
    public void setMantissa(FixedPrecision m) {
        this.mantissa = safeSet(m);
    }
    
    @Override
    protected void freezeFields() {
        mantissa.freeze();
    }

}
