/*
 *******************************************************************************
 * Copyright (C) 1996-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.math.BigInteger;

/**
 * <code>DigitList</code> handles the transcoding between numeric values and strings of characters. It only represents
 * non-negative numbers. The division of labor between <code>DigitList</code> and <code>DecimalFormat</code> is that
 * <code>DigitList</code> handles the radix 10 representation issues and numeric conversion, including rounding;
 * <code>DecimalFormat</code> handles the locale-specific issues such as positive and negative representation, digit
 * grouping, decimal point, currency, and so on.
 *
 * <p>
 * A <code>DigitList</code> is a representation of a finite numeric value. <code>DigitList</code> objects do not
 * represent <code>NaN</code> or infinite values. A <code>DigitList</code> value can be converted to a
 * <code>BigDecimal</code> without loss of precision. Conversion to other numeric formats may involve loss of precision,
 * depending on the specific value.
 *
 * <p>
 * The <code>DigitList</code> representation consists of a string of characters, which are the digits radix 10, from '0'
 * to '9'. It also has a base 10 exponent associated with it. The value represented by a <code>DigitList</code> object
 * can be computed by mulitplying the fraction <em>f</em>, where 0 <= <em>f</em> < 1, derived by placing all the digits
 * of the list to the right of the decimal point, by 10^exponent.
 *
 * @see java.util.Locale
 * @see java.text.Format
 * @see NumberFormat
 * @see DecimalFormat
 * @see java.text.ChoiceFormat
 * @see java.text.MessageFormat
 * @version 1.18 08/12/98
 * @author Mark Davis, Alan Liu
 * */
public class DigitBuilder {
    /**
     * The maximum number of significant digits in an IEEE 754 double, that is, in a Java double. This must not be
     * increased, or garbage digits will be generated, and should not be decreased, or accuracy will be lost.
     */
    static final int MAX_LONG_DIGITS = 19; // == Long.toString(Long.MAX_VALUE).length()

    private static String LONG_MIN_REP = Long.toString(Long.MIN_VALUE);
    
    private final int maxFractionalDigits;
    private final int minFractionalDigits;
    private final int maxIntegerDigits;
    private final int minIntegerDigits;
    private final int minSignificantDigits;
    private final int maxSignificantDigits;

    /**
     * @param decimalFormat
     */
    public DigitBuilder(DecimalFormat decimalFormat) {
        maxFractionalDigits = decimalFormat.getMaximumFractionDigits();
        minFractionalDigits = decimalFormat.getMinimumFractionDigits();
        maxIntegerDigits = decimalFormat.getMaximumIntegerDigits();
        minIntegerDigits = decimalFormat.getMaximumIntegerDigits();
        maxSignificantDigits = decimalFormat.getMaximumSignificantDigits();
        minSignificantDigits = decimalFormat.getMaximumSignificantDigits();
    }


    /**
     * Set the digit list to a representation of the given long value.
     * 
     * @param source
     *            Value to be converted; must be >= 0 or == Long.MIN_VALUE.
     * @param maximumDigits
     *            The most digits which should be converted. If maximumDigits is lower than the number of significant
     *            digits in source, the representation will be rounded. Ignored if <= 0.
     */
    public final DigitList2 set(long source, DigitList2 target) {
        target.clear();
        if (source == 0) {
            target.setIntegerDigits(1);
            target.setTotalDigits(1);
        } else if (source == Long.MIN_VALUE) {
            target.set(LONG_MIN_REP);
        } else {
            if (source < 0) {
                target.setNegative(true);
                source = -source;
            }
            byte[] buf = new byte[MAX_LONG_DIGITS];
            int left = getChars(source, MAX_LONG_DIGITS, buf);

            // Don't copy trailing zeros
            // we are guaranteed that there is at least one non-zero digit,
            // so we don't have to check lower bounds
            int right;
            for (right = MAX_LONG_DIGITS - 1; buf[right] == 0; --right) {
            }
            target.setDigits(buf, left, right+1);
            target.setStartDigits(0);
            target.setIntegerDigits(MAX_LONG_DIGITS-left);
            target.setTotalDigits(MAX_LONG_DIGITS-left);
        }
        return target;
    }
    
    static int getChars(long i, int index, byte[] buf) {
        long q;
        int r;
        int charPos = index;

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }
        return getChars((int)i, charPos, buf);
//        // Get 2 digits/iteration using ints
//        int q2;
//        int i2 = (int)i;
//        while (i2 >= 65536) {
//            q2 = i2 / 100;
//            // really: r = i2 - (q * 100);
//            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
//            i2 = q2;
//            buf[--charPos] = DigitOnes[r];
//            buf[--charPos] = DigitTens[r];
//        }
//
//        // Fall thru to fast mode for smaller numbers
//        // assert(i2 <= 65536, i2);
//        for (;;) {
//            q2 = (i2 * 52429) >>> (16+3);
//            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
//            buf[--charPos] = (byte)r;
//            i2 = q2;
//            if (i2 == 0) break;
//        }
//        return charPos;
    }

    static int getChars(int i2, int index, byte[] buf) {
        int r;
        int charPos = index;

        // Get 2 digits/iteration using ints
        int q2;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16+3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = (byte)r;
            i2 = q2;
            if (i2 == 0) break;
        }
        return charPos;
    }
    
    final static byte [] DigitTens = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        } ;

    final static byte [] DigitOnes = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        } ;
}
