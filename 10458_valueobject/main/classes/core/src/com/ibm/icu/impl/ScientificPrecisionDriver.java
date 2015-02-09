/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.impl;

/**
 * @author rocketman
 *
 */
public class ScientificPrecisionDriver {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ScientificPrecision sciPrecision = new ScientificPrecision();
        sciPrecision.getMutableMantissa().getMutableMax().setIntDigitCount(3);
        sciPrecision.getMutableMantissa().getMutableMax().setFracDigitCount(5);
        ScientificPrecision sciPrecision2 = new ScientificPrecision();
        sciPrecision2.setMantissa(sciPrecision.getMantissa());
        sciPrecision2.getMutableMantissa().getMutableSig().setMax(4);
        sciPrecision2.getMutableMantissa().getMutableMax().setFracDigitCount(7);
        ScientificPrecision sciPrecision3 = sciPrecision2.clone();
        sciPrecision3.getMutableMantissa().getMutableSig().setMax(6);
        System.out.println(sciPrecision);
        System.out.println(sciPrecision2);
        System.out.println(sciPrecision3);
    }

}
