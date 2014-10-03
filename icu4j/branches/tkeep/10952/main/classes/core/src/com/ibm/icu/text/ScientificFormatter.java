/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.util.ULocale;

/**
 *A formatter that formats in user-friendly scientific notation.
 * 
 * ScientificFormatter instances are immutable and thread-safe.
 *
 * Sample code:
 * <pre>
 * ULocale en = new ULocale("en");
 * ScientificFormatter fmt = ScientificFormatter.getInstanceForLocale(
 *         en, ScientificFormatter.getMarkupStyle("<sup>", "</sup>"));
 * <pre>
 * // Output: "1.23456×10<sup>-78</sup>"
 * System.out.println(fmt.format(1.23456e-78));
 * </pre>
 *
 * @draft ICU 55
 * @provisional This API might change or be removed in a future release.
 *
 */
public class ScientificFormatter {
    
    private final String preExponent;
    private final DecimalFormat fmt;
    private final Style style;
    
    /**
     * A style type for ScientificFormatter. All Style instances are immutable
     * and thread-safe.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static abstract class Style {
        abstract String format(
                AttributedCharacterIterator iterator,
                String preExponent); // '* 10^'
        
        static void append(
                AttributedCharacterIterator iterator,
                int start,
                int limit,
                StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            for (int i = start; i < limit; i++) {
                result.append(iterator.current());
                iterator.next();
            }
            iterator.setIndex(oldIndex);
        }
    }
    
    static class MarkupStyle extends Style {
        
        private final String beginMarkup;
        private final String endMarkup;
        
        MarkupStyle(String beginMarkup, String endMarkup) {
            this.beginMarkup = beginMarkup;
            this.endMarkup = endMarkup;
        }
        
        @Override
        String format(
                AttributedCharacterIterator iterator,
                String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            boolean exponentSymbolFieldPresent = false;
            boolean exponentFieldPresent = false;
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    exponentSymbolFieldPresent = true;
                    append(
                            iterator,
                            copyFromOffset,
                            iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL),
                            result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                    result.append(beginMarkup);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    exponentFieldPresent = true;
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(
                            iterator,
                            copyFromOffset,
                            limit,
                            result);
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                    result.append(endMarkup);
                } else {
                    iterator.next();
                }
            }
            if (!exponentSymbolFieldPresent || !exponentFieldPresent) {
                throw new IllegalArgumentException("Must start with standard e notation.");
            }
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
    }
    
    static class SuperscriptStyle extends Style {
        
        private static final char[] SUPERSCRIPT_DIGITS = {
            0x2070, 0xB9, 0xB2, 0xB3, 0x2074, 0x2075, 0x2076, 0x2077, 0x2078, 0x2079
        };
        
        private static final char SUPERSCRIPT_PLUS_SIGN = 0x207A;
        private static final char SUPERSCRIPT_MINUS_SIGN = 0x207B;
        
        @Override
        String format(
                AttributedCharacterIterator iterator,
                String preExponent) { 
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            boolean exponentSymbolFieldPresent = false;
            boolean exponentFieldPresent = false;
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    exponentSymbolFieldPresent = true;
                    append(
                            iterator,
                            copyFromOffset,
                            iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL),
                            result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SIGN)) {
                    int start = iterator.getRunStart(NumberFormat.Field.EXPONENT_SIGN);
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SIGN);
                    int aChar = char32AtAndAdvance(iterator);
                    if (DecimalFormat.minusSigns.contains(aChar)) {
                        append(
                                iterator,
                                copyFromOffset,
                                start,
                                result);
                        result.append(SUPERSCRIPT_MINUS_SIGN);
                    } else if (DecimalFormat.plusSigns.contains(aChar)) {
                        append(
                                iterator,
                                copyFromOffset,
                                start,
                                result);
                        result.append(SUPERSCRIPT_PLUS_SIGN);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    exponentFieldPresent = true;
                    int start = iterator.getRunStart(NumberFormat.Field.EXPONENT);
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(
                            iterator,
                            copyFromOffset,
                            start,
                            result);
                    copyAsSuperscript(iterator, start, limit, result);
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                } else {
                    iterator.next();
                }
            } 
            if (!exponentSymbolFieldPresent || !exponentFieldPresent) {
                throw new IllegalArgumentException("Must start with standard e notation.");
            }
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
        
        private static void copyAsSuperscript(
                AttributedCharacterIterator iterator, int start, int limit, StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            while (iterator.getIndex() < limit) {
                int aChar = char32AtAndAdvance(iterator);
                int digit = UCharacter.digit(aChar);
                if (digit < 0) {
                    throw new IllegalArgumentException();
                }
                result.append(SUPERSCRIPT_DIGITS[digit]);
            }
            iterator.setIndex(oldIndex);
        }
        
        private static int char32AtAndAdvance(AttributedCharacterIterator iterator) {
            char c1 = iterator.current();
            iterator.next();
            if (UCharacter.isHighSurrogate(c1)) {
                char c2 = iterator.current();
                if (c2 != CharacterIterator.DONE) {
                    if (UCharacter.isLowSurrogate(c2)) {
                        iterator.next();
                        return UCharacter.toCodePoint(c1, c2);
                    }
                }
            }
            return c1;
        }
            
    }
    
    private static final Style superScriptStyleInstance = new SuperscriptStyle();

    /**
     * Returns the superscript style.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static Style getSuperscriptStyle() {
        return superScriptStyleInstance;
    }
    
    /**
     * Returns s markup style
     * @param beginMarkup The html tag to start superscript e.g "<sup>"
     * @param endMarkup The html tag to end superscript e.g "</sup>"
     * @return the style for using markup with the given tags.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static Style getMarkupStyle(
            CharSequence beginMarkup, CharSequence endMarkup) {
        return new MarkupStyle(beginMarkup.toString(), endMarkup.toString());
    }
    
    /**
     * Gets the ScientificFormatter instance
     * @param decimalFormat The DecimalFormat must be configured for scientific
     *   notation.
     * @param style The formatting style.
     * @return the ScientificFormatter instance.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static ScientificFormatter getInstance(
            DecimalFormat decimalFormat, ScientificFormatter.Style style) {
        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();
        return new ScientificFormatter(
                (DecimalFormat) decimalFormat.clone(), getPreExponent(dfs), style);
    }
    
    /**
     * Gets a ScientificFormatter instance for this locale.
     * @param locale The locale
     * @param style the formatting style.
     * @return The ScientificFormatter instance.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static ScientificFormatter getInstanceForLocale(
            ULocale locale, ScientificFormatter.Style style) {
        DecimalFormat decimalFormat =
                (DecimalFormat) DecimalFormat.getScientificInstance(locale);
        return new ScientificFormatter(
                decimalFormat,
                getPreExponent(decimalFormat.getDecimalFormatSymbols()),
                style);
    }
    
    /**
     * Formats a number
     * @param number Can be a double, int, Number or
     *  anything that DecimalFormat#format(Object) accepts.
     * @return the formatted string.
     *
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public String format(Object number) {
        AttributedCharacterIterator iterator;
        synchronized (fmt) {
            iterator = fmt.formatToCharacterIterator(number);
        }
        return style.format(iterator, preExponent);
    }
    
    private ScientificFormatter(
            DecimalFormat decimalFormat, String preExponent, Style style) {
        this.fmt = decimalFormat;
        this.preExponent = preExponent;
        this.style = style;
    }
    
    static String getPreExponent(DecimalFormatSymbols dfs) {
        StringBuilder preExponent = new StringBuilder();
        preExponent.append(dfs.getExponentMultiplicationSign());
        char[] digits = dfs.getDigits();
        preExponent.append(digits[1]).append(digits[0]);
        return preExponent.toString();
    }

}
