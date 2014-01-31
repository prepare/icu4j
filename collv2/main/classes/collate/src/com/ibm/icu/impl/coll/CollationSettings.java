/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationSettings.java, ported from collationsettings.h/.cpp
*
* @since 2013feb07
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import java.util.Arrays;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator.AttributeValue;

/**
 * Collation settings/options/attributes.
 * These are the values that can be changed via API.
 */
public final class CollationSettings extends SharedObject {
    /**
     * Options bit 0: Perform the FCD check on the input text and deliver normalized text.
     */
    static final int CHECK_FCD = 1;
    /**
     * Options bit 1: Numeric collation.
     * Also known as CODAN = COllate Digits As Numbers.
     *
     * Treat digit sequences as numbers with CE sequences in numeric order,
     * rather than returning a normal CE for each digit.
     */
    static final int NUMERIC = 2;
    /**
     * "Shifted" alternate handling, see ALTERNATE_MASK.
     */
    static final int SHIFTED = 4;
    /**
     * Options bits 3..2: Alternate-handling mask. 0 for non-ignorable.
     * Reserve values 8 and 0xc for shift-trimmed and blanked.
     */
    static final int ALTERNATE_MASK = 0xc;
    /**
     * Options bits 6..4: The 3-bit maxVariable value bit field is shifted by this value.
     */
    static final int MAX_VARIABLE_SHIFT = 4;
    /** maxVariable options bit mask before shifting. */
    static final int MAX_VARIABLE_MASK = 0x70;
    /** Options bit 7: Reserved/unused/0. */
    /**
     * Options bit 8: Sort uppercase first if caseLevel or caseFirst is on.
     */
    static final int UPPER_FIRST = 0x100;
    /**
     * Options bit 9: Keep the case bits in the tertiary weight (they trump other tertiary values)
     * unless case level is on (when they are *moved* into the separate case level).
     * By default, the case bits are removed from the tertiary weight (ignored).
     *
     * When CASE_FIRST is off, UPPER_FIRST must be off too, corresponding to
     * the tri-value UCOL_CASE_FIRST attribute: UCOL_OFF vs. UCOL_LOWER_FIRST vs. UCOL_UPPER_FIRST.
     */
    static final int CASE_FIRST = 0x200;
    /**
     * Options bit mask for caseFirst and upperFirst, before shifting.
     * Same value as caseFirst==upperFirst.
     */
    static final int CASE_FIRST_AND_UPPER_MASK = CASE_FIRST | UPPER_FIRST;
    /**
     * Options bit 10: Insert the case level between the secondary and tertiary levels.
     */
    static final int CASE_LEVEL = 0x400;
    /**
     * Options bit 11: Compare secondary weights backwards. ("French secondary")
     */
    static final int BACKWARD_SECONDARY = 0x800;
    /**
     * Options bits 15..12: The 4-bit strength value bit field is shifted by this value.
     * It is the top used bit field in the options. (No need to mask after shifting.)
     */
    static final int STRENGTH_SHIFT = 12;
    /** Strength options bit mask before shifting. */
    static final int STRENGTH_MASK = 0xf000;

    /** maxVariable values */
    static final int MAX_VAR_SPACE = 0;
    static final int MAX_VAR_PUNCT = 1;
    static final int MAX_VAR_SYMBOL = 2;
    static final int MAX_VAR_CURRENCY = 3;

    CollationSettings() {}

    @Override
    public CollationSettings clone() {
        CollationSettings newSettings = (CollationSettings)super.clone();
        // Note: The reorderTable and reorderCodes need not be cloned
        // because, in Java, they only get replaced but not modified.
        newSettings.fastLatinPrimaries = fastLatinPrimaries.clone();
        return newSettings;
    }

    @Override
    public boolean equals(Object other) {
        if(!this.getClass().equals(other.getClass())) { return false; }
        CollationSettings o = (CollationSettings)other;
        if(options != o.options) { return false; }
        if((options & ALTERNATE_MASK) != 0 && variableTop != o.variableTop) { return false; }
        if(!Arrays.equals(reorderCodes, o.reorderCodes)) { return false; }
        return true;
    }

    @Override
    public int hashCode() {
        int h = options << 8;
        if((options & ALTERNATE_MASK) != 0) { h ^= variableTop; }
        h ^= reorderCodes.length;
        for(int i = 0; i < reorderCodes.length; ++i) {
            h ^= (reorderCodes[i] << i);
        }
        return h;
    }

    void resetReordering() {
        // When we turn off reordering, we want to set a null permutation
        // rather than a no-op permutation.
        reorderTable = null;
        reorderCodes = EMPTY_INT_ARRAY;
    }
    // No aliasReordering() in Java. Use setReordering(). See comments near reorderCodes.
    void setReordering(int[] codes, byte[] table) {
        assert (codes.length == 0) == (table == null);
        reorderTable = table;
        reorderCodes = codes;
    }

    // TODO: Most setters probably need to be split into set() vs. setDefault() to match the Java Collator API.
    void setStrength(int value, int defaultOptions) {
        int noStrength = options & ~STRENGTH_MASK;
        switch(value) {
        case Collator.PRIMARY:
        case Collator.SECONDARY:
        case Collator.TERTIARY:
        case Collator.QUATERNARY:
        case Collator.IDENTICAL:
            options = noStrength | (value << STRENGTH_SHIFT);
            break;
        case AttributeValue.DEFAULT_:
            options = noStrength | (defaultOptions & STRENGTH_MASK);
            break;
        default:
            throw new IllegalArgumentException("illegal strength value " + value);  // TODO: review message text
        }
    }

    static int getStrength(int options) {
        return options >> STRENGTH_SHIFT;
    }

    public int getStrength() {
        return getStrength(options);
    }

    /** Sets the options bit for an on/off attribute. */
    void setFlag(int bit, int value, int defaultOptions) {
        switch(value) {
        case AttributeValue.ON_:
            options |= bit;
            break;
        case AttributeValue.OFF_:
            options &= ~bit;
            break;
        case AttributeValue.DEFAULT_:
            options = (options & ~bit) | (defaultOptions & bit);
            break;
        default:
            throw new IllegalArgumentException("illegal boolean value " + value);  // TODO: review message text
        }
    }

    // TODO: return boolean?
    int getFlag(int bit) {
        return ((options & bit) != 0) ? AttributeValue.ON_ : AttributeValue.OFF_;
    }

    void setCaseFirst(int value, int defaultOptions) {
        int noCaseFirst = options & ~CASE_FIRST_AND_UPPER_MASK;
        switch(value) {
        case AttributeValue.OFF_:
            options = noCaseFirst;
            break;
        case AttributeValue.LOWER_FIRST_:
            options = noCaseFirst | CASE_FIRST;
            break;
        case AttributeValue.UPPER_FIRST_:
            options = noCaseFirst | CASE_FIRST_AND_UPPER_MASK;
            break;
        case AttributeValue.DEFAULT_:
            options = noCaseFirst | (defaultOptions & CASE_FIRST_AND_UPPER_MASK);
            break;
        default:
            throw new IllegalArgumentException("illegal caseFirst value " + value);  // TODO: review message text
        }
    }

    int getCaseFirst() {
        int option = options & CASE_FIRST_AND_UPPER_MASK;
        return (option == 0) ? AttributeValue.OFF_ :
                (option == CASE_FIRST) ? AttributeValue.LOWER_FIRST_ : AttributeValue.UPPER_FIRST_;
    }

    void setAlternateHandling(int value, int defaultOptions) {
        int noAlternate = options & ~ALTERNATE_MASK;
        switch(value) {
        case AttributeValue.NON_IGNORABLE_:
            options = noAlternate;
            break;
        case AttributeValue.SHIFTED_:
            options = noAlternate | SHIFTED;
            break;
        case AttributeValue.DEFAULT_:
            options = noAlternate | (defaultOptions & ALTERNATE_MASK);
            break;
        default:
            throw new IllegalArgumentException("illegal alternate-handling value " + value);  // TODO: review message text
        }
    }

    int getAlternateHandling() {
        return ((options & ALTERNATE_MASK) == 0) ? AttributeValue.NON_IGNORABLE_ : AttributeValue.SHIFTED_;
    }

    void setMaxVariable(int value, int defaultOptions) {
        int noMax = options & ~MAX_VARIABLE_MASK;
        switch(value) {
        case MAX_VAR_SPACE:
        case MAX_VAR_PUNCT:
        case MAX_VAR_SYMBOL:
        case MAX_VAR_CURRENCY:
            options = noMax | (value << MAX_VARIABLE_SHIFT);
            break;
        case AttributeValue.DEFAULT_:
            options = noMax | (defaultOptions & MAX_VARIABLE_MASK);
            break;
        default:
            throw new IllegalArgumentException("illegal maxVariable value " + value);  // TODO: review message text
        }
    }

    int getMaxVariable() {
        return (options & MAX_VARIABLE_MASK) >> MAX_VARIABLE_SHIFT;
    }

    /**
     * Include case bits in the tertiary level if caseLevel=off and caseFirst!=off.
     */
    static boolean isTertiaryWithCaseBits(int options) {
        return (options & (CASE_LEVEL | CASE_FIRST)) == CASE_FIRST;
    }
    static int getTertiaryMask(int options) {
        // Remove the case bits from the tertiary weight when caseLevel is on or caseFirst is off.
        return isTertiaryWithCaseBits(options) ?
                Collation.CASE_AND_TERTIARY_MASK : Collation.ONLY_TERTIARY_MASK;
    }

    static boolean sortsTertiaryUpperCaseFirst(int options) {
        // On tertiary level, consider case bits and sort uppercase first
        // if caseLevel is off and caseFirst==upperFirst.
        return (options & (CASE_LEVEL | CASE_FIRST_AND_UPPER_MASK)) == CASE_FIRST_AND_UPPER_MASK;
    }

    public boolean dontCheckFCD() {
        return (options & CHECK_FCD) == 0;
    }

    boolean hasBackwardSecondary() {
        return (options & BACKWARD_SECONDARY) != 0;
    }

    public boolean isNumeric() {
        return (options & NUMERIC) != 0;
    }

    /** CHECK_FCD etc. */
    int options = (AttributeValue.DEFAULT_STRENGTH_ << STRENGTH_SHIFT) |
            (MAX_VAR_PUNCT << MAX_VARIABLE_SHIFT);
    /** Variable-top primary weight. */
    long variableTop;
    /** 256-byte table for reordering permutation of primary lead bytes; null if no reordering. */
    byte[] reorderTable;
    /** Array of reorder codes; ignored if length == 0. */
    int[] reorderCodes = EMPTY_INT_ARRAY;
    // Note: In C++, we keep a memory block around for the reorder codes and the permutation table,
    // and modify them for new codes.
    // In Java, we simply copy references and then never modify the array contents.
    // The caller must abandon the arrays.
    // Reorder codes from the public setter API must be cloned.
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    /** Options for CollationFastLatin. Negative if disabled. */
    public int fastLatinOptions = -1;
    // fastLatinPrimaries.length must be equal to CollationFastLatin.LATIN_LIMIT,
    // but we do not import CollationFastLatin to reduce circular dependencies.
    public char[] fastLatinPrimaries = new char[0x180];  // mutable contents
}
