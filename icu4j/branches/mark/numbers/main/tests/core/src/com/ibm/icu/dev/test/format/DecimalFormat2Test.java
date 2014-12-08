/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.text.FieldPosition;
import java.util.Arrays;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormat2;
import com.ibm.icu.text.DigitList2;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;

/**
 * @author markdavis
 *
 */
public class DecimalFormat2Test extends TestFmwk{
    /**
     * 
     */
    private static final int ITERATIONS = 50000000;
    private static final int WARMUP = ITERATIONS/10;

    // Just for quick test
    public static void main(String[] args) {
        new DecimalFormat2Test().run(args);
    }

    public void TestPerf() {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(ULocale.US);
        DecimalFormat2 df2 = new DecimalFormat2(df);
        while (true) {
            String s2 = df2.format(12345);
        }
    }

    public void TestAQuick () {
        int[] tests = {0, -1, 1, -12, 12, -123, 123, -1234, 1234, -12345, 12345};
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(ULocale.US);
        DecimalFormat2 df2 = new DecimalFormat2(df);
        for (int test : tests) {
            String s1 = df.format(test);
            String s2 = df2.format(test);
            assertEquals("", s1, s2);
        }
    }

    public void TestSpeed () {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(new ULocale("ar"));

        for (int value : Arrays.asList(1, 12340, 1234567, Integer.MAX_VALUE)) {
            // warm up
            df.setGroupingUsed(false);
            check(value, df, WARMUP, false);
            df.setGroupingUsed(true);
            check(value, df, WARMUP, false);

            // real checks
            logln("Without grouping");
            df.setGroupingUsed(false);
            check(value, df, ITERATIONS, true);

            logln("With grouping");
            df.setGroupingUsed(true);        
            check(value, df, ITERATIONS, true);
        }
    }

    DecimalFormat plain = (DecimalFormat) DecimalFormat.getInstance(ULocale.ENGLISH);
    {
        plain.setMaximumSignificantDigits(3);
        plain.setMinimumSignificantDigits(3);
    }

    public int check(int value, DecimalFormat df, int iterations, boolean show) {
        int size = 0;
        long start, baseDelta, currentDelta;
        DecimalFormat2 df2 = new DecimalFormat2(df);

        start = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            size += Integer.toString(value).length(); // get the length just to prevent overoptimization
        }
        baseDelta = System.nanoTime() - start;
        if (show) logln("\t" + value + ": \t" + plain.format(baseDelta/(double)iterations) + "\tns, \ttoString:\t1");

        start = System.nanoTime();
        StringBuilder buf = new StringBuilder(19);
        FieldPosition pos = new FieldPosition(0);
        DigitList2 digits = new DigitList2();
        for (int i = 0; i < iterations; ++i) {
            buf.setLength(0);
            size += df2.format(value, digits, buf, pos).length(); // get the length just to prevent overoptimization
        }
        currentDelta = System.nanoTime() - start;
        if (show) logln("\t" + df2.format(value) + ": \t" + plain.format(currentDelta/(double)iterations) + "\tns, \tnew1:\t" + plain.format(currentDelta / (double) baseDelta) + "\tx slower");

        start = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            size += df2.format(value).length(); // get the length just to prevent overoptimization
        }
        currentDelta = System.nanoTime() - start;
        if (show) logln("\t" + df2.format(value) + ": \t" + plain.format(currentDelta/(double)iterations) + "\tns, \tnew2:\t" + plain.format(currentDelta / (double) baseDelta) + "\tx slower");

        start = System.nanoTime();
        for (int i = 0; i < iterations; ++i) {
            size += df.format(value).length(); // get the length just to prevent overoptimization
        }
        currentDelta = System.nanoTime() - start;
        if (show) logln("\t" + df.format(value) + ": \t" + plain.format(currentDelta/(double)iterations) + "\tns, \told:\t" + plain.format(currentDelta / (double) baseDelta) + "\tx slower");
        return size;
    }

}
