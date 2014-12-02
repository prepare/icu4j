/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.ParsePosition;

/**
 * This class uses the DigitBuilder to build a digit list, and and the DigitFormatter to format those digits.
 * But then it uses other logic to add the right prefixes and suffixes.
 * <p>This is just a proof of concept right not, not fleshed out to the point where you could call it a design.
 * <p>Key features are:
 * <ol>
 * <li>Separation into digitlist building, formatting digits, and adding prefix/suffixes
 * <li>Thread-safe, immutable format classes.
 * </ol>
 * It doesn't do many things:
 * <ol>
 * <li>the min/max integer/fraction/significant digits or rounding
 * <li>hindi group separators
 * <li>doubles, BigIntegers, Doubles (but these can all be handled in the DigitList building).
 * <li>prefixes and suffixes. Want to be able to do:
 * <ol>
 * <li>3%, 1 foot, 3 feet, 3M feet, $3.00, 3-5M feet, $3M, $3-5M, $3+M feet...
 * </ol>
 * <li>
 * </ol
 * @author markdavis
 */
public class DecimalFormat2 {

    private final DigitFormatter digitFormatter;
    private final DigitBuilder builder;
    private final String negativePrefix;
    private final String positivePrefix;
    private final String negativeSuffix;
    private final String positiveSuffix;
    
    /**
     * For now, build from decimalFormat
     * @param decimalFormat
     */
    public DecimalFormat2(DecimalFormat decimalFormat) {
        negativePrefix = decimalFormat.getNegativePrefix();
        positivePrefix = decimalFormat.getPositivePrefix();
        negativeSuffix = decimalFormat.getNegativeSuffix();
        positiveSuffix = decimalFormat.getPositiveSuffix();
        digitFormatter = new DigitFormatter(decimalFormat);
        builder = new DigitBuilder(decimalFormat);
    }

    /**
     * Format single item
     * @param decimalFormat
     */
    public <T extends Appendable> T format(long value, DigitList2 digits, T toAppendTo, FieldPosition pos) {
        try {
            builder.set(value, digits);
            boolean negative = digits.isNegative();
            toAppendTo.append(negative ? negativePrefix : positivePrefix);
            digitFormatter.appendDigits(digits, toAppendTo);
            toAppendTo.append(negative ? negativeSuffix : positiveSuffix);
            return toAppendTo;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Format single item
     * @param decimalFormat
     */
    public <T extends Appendable> T format(DigitList2 digits, T toAppendTo, FieldPosition pos) {
        try {
            boolean negative = digits.isNegative();
            toAppendTo.append(negative ? negativePrefix : positivePrefix);
            digitFormatter.appendDigits(digits, toAppendTo);
            toAppendTo.append(negative ? negativeSuffix : positiveSuffix);
            return toAppendTo;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Format range item
     * @param decimalFormat
     */
    public <T extends Appendable> T format(DigitList2 digits1, DigitList2 digits2, T toAppendTo, FieldPosition pos) {
        try {
            format(digits1, toAppendTo, pos);
            toAppendTo.append(" – "); // for now, get pattern later
            format(digits2, toAppendTo, pos);
            return toAppendTo;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public final String format(long number) {
        StringBuilder buf = new StringBuilder(19);
        FieldPosition pos = new FieldPosition(0);
        DigitList2 digits = new DigitList2();
        format(number, digits, buf, pos);
        return buf.toString();
    }
    
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return format(((Number) obj).longValue(), toAppendTo, pos);
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
}
