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
 * ScientificFormatter fmt = ScientificFormatter.getMarkupInstance(
 *         en, "<sup>", "</sup>");
 * <pre>
 * // Output: "1.23456×10<sup>-78</sup>"
 * System.out.println(fmt.format(1.23456e-78));
 * </pre>
 *
 * @draft ICU 55
 * @provisional This API might change or be removed in a future release.
 *
 */
public final class ScientificFormatter {
    
    private final String preExponent;
    private final DecimalFormat fmt;
    private final Style style;
    
    /**
     * Gets a ScientificFormatter instance that uses
     * superscript characters for exponents for this locale.
     * @param locale The locale
     * @return The ScientificFormatter instance.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */
    public static ScientificFormatter getSuperscriptInstance(ULocale locale) {
        return getInstanceForLocale(locale, SUPER_SCRIPT); 
     }
     
    /**
     * Gets a ScientificFormatter instance that uses
     * superscript characters for exponents.
     * @param df The DecimalFormat must be configured for scientific
     *   notation.
     * @return the ScientificFormatter instance.
     * 
     * @draft ICU 55
     * @provisional This API might change or be removed in a future release.
     */ 
     public static ScientificFormatter getSuperscriptInstance(
             DecimalFormat df) {
         return getInstance(df, SUPER_SCRIPT); 
     }
 
     /**
      * Gets a ScientificFormatter instance that uses
      * mark up for exponents for this locale.
      * @param locale The locale
      * @param beginMarkup the mark up to start superscript e.g {@code <sup>}
      * @param endMarkup the mark up to end superscript e.g {@code </sup>}
      * @return The ScientificFormatter instance.
      * 
      * @draft ICU 55
      * @provisional This API might change or be removed in a future release.
      */
     public static ScientificFormatter getMarkupInstance(
             ULocale locale,
             CharSequence beginMarkup,
             CharSequence endMarkup) {
         return getInstanceForLocale(
                 locale, new MarkupStyle(beginMarkup.toString(), endMarkup.toString()));
     }
     
     /**
      * Gets a ScientificFormatter instance that uses
      * mark up for exponents.
      * @param df The DecimalFormat must be configured for scientific
      *   notation.
      * @param beginMarkup the mark up to start superscript e.g {@code <sup>}
      * @param endMarkup the mark up to end superscript e.g {@code </sup>}
      * @return The ScientificFormatter instance.
      * 
      * @draft ICU 55
      * @provisional This API might change or be removed in a future release.
      */
     public static ScientificFormatter getMarkupInstance(
             DecimalFormat df,
             CharSequence beginMarkup,
             CharSequence endMarkup) {
         return getInstance(
                 df, new MarkupStyle(beginMarkup.toString(), endMarkup.toString()));
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
         synchronized (fmt) {
             return style.format(
                     fmt.formatToCharacterIterator(number),
                     preExponent);
         }
     }
     
    /**
     * A style type for ScientificFormatter. All Style instances are immutable
     * and thread-safe.
     */
    static abstract class Style {
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
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
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
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
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
    
    static String getPreExponent(DecimalFormatSymbols dfs) {
        StringBuilder preExponent = new StringBuilder();
        preExponent.append(dfs.getExponentMultiplicationSign());
        char[] digits = dfs.getDigits();
        preExponent.append(digits[1]).append(digits[0]);
        return preExponent.toString();
    }
    
    static ScientificFormatter getInstance(
            DecimalFormat decimalFormat, Style style) {
        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();
        return new ScientificFormatter(
                (DecimalFormat) decimalFormat.clone(), getPreExponent(dfs), style);
    }
     
    static ScientificFormatter getInstanceForLocale(
            ULocale locale, Style style) {
        DecimalFormat decimalFormat =
                (DecimalFormat) DecimalFormat.getScientificInstance(locale);
        return new ScientificFormatter(
                decimalFormat,
                getPreExponent(decimalFormat.getDecimalFormatSymbols()),
                style);
    }
    
    static final Style SUPER_SCRIPT = new SuperscriptStyle();
    
    private ScientificFormatter(
            DecimalFormat decimalFormat, String preExponent, Style style) {
        this.fmt = decimalFormat;
        this.preExponent = preExponent;
        this.style = style;
    }
    

}
