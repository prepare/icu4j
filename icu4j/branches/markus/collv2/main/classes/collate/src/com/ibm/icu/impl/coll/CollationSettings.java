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

/**
 * Collation settings/options/attributes.
 * These are the values that can be changed via API.
 */
final class CollationSettings extends SharedObject {
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

    CollationSettings()
            : options((UCOL_DEFAULT_STRENGTH << STRENGTH_SHIFT) |
                      (MAX_VAR_PUNCT << MAX_VARIABLE_SHIFT)),
              variableTop(0),
              reorderTable(null),
              reorderCodes(null), reorderCodesLength(0), reorderCodesCapacity(0),
              fastLatinOptions(-1) {}

    CollationSettings(const CollationSettings &other);
    virtual ~CollationSettings();

    boolean operator==(const CollationSettings &other);

    boolean operator!=(const CollationSettings &other) {
        return !operator==(other);
    }

    int hashCode();

    void resetReordering();
    void aliasReordering(const int *codes, int length, const uint8_t *table);
    boolean setReordering(const int *codes, int length, const uint8_t table[256]);

    void setStrength(int value, int defaultOptions, UErrorCode &errorCode);

    static int getStrength(int options) {
        return options >> STRENGTH_SHIFT;
    }

    int getStrength() {
        return getStrength(options);
    }

    /** Sets the options bit for an on/off attribute. */
    void setFlag(int bit, UColAttributeValue value,
                 int defaultOptions, UErrorCode &errorCode);

    UColAttributeValue getFlag(int bit) {
        return ((options & bit) != 0) ? UCOL_ON : UCOL_OFF;
    }

    void setCaseFirst(UColAttributeValue value, int defaultOptions, UErrorCode &errorCode);

    UColAttributeValue getCaseFirst() {
        int option = options & CASE_FIRST_AND_UPPER_MASK;
        return (option == 0) ? UCOL_OFF :
                (option == CASE_FIRST) ? UCOL_LOWER_FIRST : UCOL_UPPER_FIRST;
    }

    void setAlternateHandling(UColAttributeValue value,
                              int defaultOptions, UErrorCode &errorCode);

    UColAttributeValue getAlternateHandling() {
        return ((options & ALTERNATE_MASK) == 0) ? UCOL_NON_IGNORABLE : UCOL_SHIFTED;
    }

    void setMaxVariable(int value, int defaultOptions, UErrorCode &errorCode);

    MaxVariable getMaxVariable() {
        return (MaxVariable)((options & MAX_VARIABLE_MASK) >> MAX_VARIABLE_SHIFT);
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

    boolean dontCheckFCD() {
        return (options & CHECK_FCD) == 0;
    }

    boolean hasBackwardSecondary() {
        return (options & BACKWARD_SECONDARY) != 0;
    }

    boolean isNumeric() {
        return (options & NUMERIC) != 0;
    }

    /** CHECK_FCD etc. */
    int options;
    /** Variable-top primary weight. */
    long variableTop;
    /** 256-byte table for reordering permutation of primary lead bytes; null if no reordering. */
    byte[] reorderTable;
    /** Array of reorder codes; ignored if reorderCodesLength == 0. */
    int[] reorderCodes;
    /** Number of reorder codes; 0 if no reordering. */
    int reorderCodesLength;
    /**
     * Capacity of reorderCodes.
     * If 0, then the table and codes are aliases.
     * Otherwise, this object owns the memory via the reorderCodes pointer;
     * the table and the codes are in the same memory block, with the codes first.
     */
    int reorderCodesCapacity;

    /** Options for CollationFastLatin. Negative if disabled. */
    int fastLatinOptions;
    char[] fastLatinPrimaries[0x180];
}

    CollationSettings.CollationSettings(const CollationSettings &other)
            : SharedObject(other),
              options(other.options), variableTop(other.variableTop),
              reorderTable(null),
              reorderCodes(null), reorderCodesLength(0), reorderCodesCapacity(0),
              fastLatinOptions(other.fastLatinOptions) {
        int length = other.reorderCodesLength;
        if(length == 0) {
            U_ASSERT(other.reorderTable == null);
        } else {
            U_ASSERT(other.reorderTable != null);
            if(other.reorderCodesCapacity == 0) {
                aliasReordering(other.reorderCodes, length, other.reorderTable);
            } else {
                setReordering(other.reorderCodes, length, other.reorderTable);
            }
        }
        if(fastLatinOptions >= 0) {
            uprv_memcpy(fastLatinPrimaries, other.fastLatinPrimaries, sizeof(fastLatinPrimaries));
        }
    }

    CollationSettings.~CollationSettings() {
        if(reorderCodesCapacity != 0) {
            uprv_free(const_cast<int *>(reorderCodes));
        }
    }

    boolean
    CollationSettings.operator==(const CollationSettings &other) {
        if(options != other.options) { return false; }
        if((options & ALTERNATE_MASK) != 0 && variableTop != other.variableTop) { return false; }
        if(reorderCodesLength != other.reorderCodesLength) { return false; }
        for(int i = 0; i < reorderCodesLength; ++i) {
            if(reorderCodes[i] != other.reorderCodes[i]) { return false; }
        }
        return true;
    }

    int
    CollationSettings.hashCode() {
        int h = options << 8;
        if((options & ALTERNATE_MASK) != 0) { h ^= variableTop; }
        h ^= reorderCodesLength;
        for(int i = 0; i < reorderCodesLength; ++i) {
            h ^= (reorderCodes[i] << i);
        }
        return h;
    }

    void
    CollationSettings.resetReordering() {
        // When we turn off reordering, we want to set a null permutation
        // rather than a no-op permutation.
        // Keep the memory via reorderCodes and its capacity.
        reorderTable = null;
        reorderCodesLength = 0;
    }

    void
    CollationSettings.aliasReordering(const int *codes, int length, const uint8_t *table) {
        if(length == 0) {
            resetReordering();
        } else {
            // We need to release the memory before setting the alias pointer.
            if(reorderCodesCapacity != 0) {
                uprv_free(const_cast<int *>(reorderCodes));
                reorderCodesCapacity = 0;
            }
            reorderTable = table;
            reorderCodes = codes;
            reorderCodesLength = length;
        }
    }

    boolean
    CollationSettings.setReordering(const int *codes, int length, const uint8_t table[256]) {
        if(length == 0) {
            resetReordering();
        } else {
            uint8_t *ownedTable;
            int *ownedCodes;
            if(length <= reorderCodesCapacity) {
                ownedTable = const_cast<uint8_t *>(reorderTable);
                ownedCodes = const_cast<int *>(reorderCodes);
            } else {
                // Allocate one memory block for the codes and the 16-aligned table.
                int capacity = (length + 3) & ~3;  // round up to a multiple of 4 ints
                uint8_t *bytes = (uint8_t *)uprv_malloc(256 + capacity * 4);
                if(bytes == null) { return false; }
                if(reorderCodesCapacity != 0) {
                    uprv_free(const_cast<int *>(reorderCodes));
                }
                reorderTable = ownedTable = bytes + capacity * 4;
                reorderCodes = ownedCodes = (int *)bytes;
                reorderCodesCapacity = capacity;
            }
            uprv_memcpy(ownedTable, table, 256);
            uprv_memcpy(ownedCodes, codes, length * 4);
            reorderCodesLength = length;
        }
        return true;
    }

    void
    CollationSettings.setStrength(int value, int defaultOptions, UErrorCode &errorCode) {
        if(U_FAILURE) { return; }
        int noStrength = options & ~STRENGTH_MASK;
        switch(value) {
        case UCOL_PRIMARY:
        case UCOL_SECONDARY:
        case UCOL_TERTIARY:
        case UCOL_QUATERNARY:
        case UCOL_IDENTICAL:
            options = noStrength | (value << STRENGTH_SHIFT);
            break;
        case UCOL_DEFAULT:
            options = noStrength | (defaultOptions & STRENGTH_MASK);
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            break;
        }
    }

    void
    CollationSettings.setFlag(int bit, UColAttributeValue value,
                              int defaultOptions, UErrorCode &errorCode) {
        if(U_FAILURE) { return; }
        switch(value) {
        case UCOL_ON:
            options |= bit;
            break;
        case UCOL_OFF:
            options &= ~bit;
            break;
        case UCOL_DEFAULT:
            options = (options & ~bit) | (defaultOptions & bit);
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            break;
        }
    }

    void
    CollationSettings.setCaseFirst(UColAttributeValue value,
                                    int defaultOptions, UErrorCode &errorCode) {
        if(U_FAILURE) { return; }
        int noCaseFirst = options & ~CASE_FIRST_AND_UPPER_MASK;
        switch(value) {
        case UCOL_OFF:
            options = noCaseFirst;
            break;
        case UCOL_LOWER_FIRST:
            options = noCaseFirst | CASE_FIRST;
            break;
        case UCOL_UPPER_FIRST:
            options = noCaseFirst | CASE_FIRST_AND_UPPER_MASK;
            break;
        case UCOL_DEFAULT:
            options = noCaseFirst | (defaultOptions & CASE_FIRST_AND_UPPER_MASK);
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            break;
        }
    }

    void
    CollationSettings.setAlternateHandling(UColAttributeValue value,
                                            int defaultOptions, UErrorCode &errorCode) {
        if(U_FAILURE) { return; }
        int noAlternate = options & ~ALTERNATE_MASK;
        switch(value) {
        case UCOL_NON_IGNORABLE:
            options = noAlternate;
            break;
        case UCOL_SHIFTED:
            options = noAlternate | SHIFTED;
            break;
        case UCOL_DEFAULT:
            options = noAlternate | (defaultOptions & ALTERNATE_MASK);
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            break;
        }
    }

    void
    CollationSettings.setMaxVariable(int value, int defaultOptions, UErrorCode &errorCode) {
        if(U_FAILURE) { return; }
        int noMax = options & ~MAX_VARIABLE_MASK;
        switch(value) {
        case MAX_VAR_SPACE:
        case MAX_VAR_PUNCT:
        case MAX_VAR_SYMBOL:
        case MAX_VAR_CURRENCY:
            options = noMax | (value << MAX_VARIABLE_SHIFT);
            break;
        case UCOL_DEFAULT:
            options = noMax | (defaultOptions & MAX_VARIABLE_MASK);
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            break;
        }
    }
