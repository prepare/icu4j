/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationfastlatin.h
*
* @since 2013aug09
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

struct CollationData;
struct CollationSettings;

final class CollationFastLatin /* all static */ {
public:
    /**
     * Fast Latin format version (one byte 1..FF).
     * Must be incremented for any runtime-incompatible changes,
     * in particular, for changes to any of the following constants.
     *
     * When the major version number of the main data format changes,
     * we can reset this fast Latin version to 1.
     */
    private static final int VERSION = 1;

    private static final int LATIN_MAX = 0x17f;
    private static final int LATIN_LIMIT = LATIN_MAX + 1;

    private static final int LATIN_MAX_UTF8_LEAD = 0xc5;  // UTF-8 lead byte of LATIN_MAX

    private static final int PUNCT_START = 0x2000;
    private static final int PUNCT_LIMIT = 0x2040;

    // excludes U+FFFE & U+FFFF
    private static final int NUM_FAST_CHARS = LATIN_LIMIT + (PUNCT_LIMIT - PUNCT_START);

    // Note on the supported weight ranges:
    // Analysis of UCA 6.3 and CLDR 23 non-search tailorings shows that
    // the CEs for characters in the above ranges, excluding expansions with length >2,
    // excluding contractions of >2 characters, and other restrictions
    // (see the builder's getCEsFromCE32()),
    // use at most about 150 primary weights,
    // where about 94 primary weights are possibly-variable (space/punct/symbol/currency),
    // at most 4 secondary before-common weights,
    // at most 4 secondary after-common weights,
    // at most 16 secondary high weights (in secondary CEs), and
    // at most 4 tertiary after-common weights.
    // The following ranges are designed to support slightly more weights than that.
    // (en_US_POSIX is unusual: It creates about 64 variable + 116 Latin primaries.)

    // Digits may use long primaries (preserving more short ones)
    // or short primaries (faster) without changing this data structure.
    // (If we supported numeric collation, then digits would have to have long primaries
    // so that special handling does not affect the fast path.)

    private static final int SHORT_PRIMARY_MASK = 0xfc00;  // bits 15..10
    private static final int INDEX_MASK = 0x3ff;  // bits 9..0 for expansions & contractions
    private static final int SECONDARY_MASK = 0x3e0;  // bits 9..5
    private static final int CASE_MASK = 0x18;  // bits 4..3
    private static final int LONG_PRIMARY_MASK = 0xfff8;  // bits 15..3
    private static final int TERTIARY_MASK = 7;  // bits 2..0
    private static final int CASE_AND_TERTIARY_MASK = CASE_MASK | TERTIARY_MASK;

    private static final int TWO_SHORT_PRIMARIES_MASK =
            (SHORT_PRIMARY_MASK << 16) | SHORT_PRIMARY_MASK;  // 0xfc00fc00
    private static final int TWO_LONG_PRIMARIES_MASK =
            (LONG_PRIMARY_MASK << 16) | LONG_PRIMARY_MASK;  // 0xfff8fff8
    private static final int TWO_SECONDARIES_MASK =
            (SECONDARY_MASK << 16) | SECONDARY_MASK;  // 0x3e003e0
    private static final int TWO_CASES_MASK =
            (CASE_MASK << 16) | CASE_MASK;  // 0x180018
    private static final int TWO_TERTIARIES_MASK =
            (TERTIARY_MASK << 16) | TERTIARY_MASK;  // 0x70007

    /**
     * Contraction with one fast Latin character.
     * Use INDEX_MASK to find the start of the contraction list after the fixed table.
     * The first entry contains the default mapping.
     * Otherwise use CONTR_CHAR_MASK for the contraction character index
     * (in ascending order).
     * Use CONTR_LENGTH_SHIFT for the length of the entry
     * (1=BAIL_OUT, 2=one CE, 3=two CEs).
     *
     * Also, U+0000 maps to a contraction entry, so that the fast path need not
     * check for NUL termination.
     * It usually maps to a contraction list with only the completely ignorable default value.
     */
    private static final int CONTRACTION = 0x400;
    /**
     * An expansion encodes two CEs.
     * Use INDEX_MASK to find the pair of CEs after the fixed table.
     *
     * The higher a mini CE value, the easier it is to process.
     * For expansions and higher, no context needs to be considered.
     */
    private static final int EXPANSION = 0x800;
    /**
     * Encodes one CE with a long/low mini primary (there are 128).
     * All potentially-variable primaries must be in this range,
     * to make the short-primary path as fast as possible.
     */
    private static final int MIN_LONG = 0xc00;
    private static final int LONG_INC = 8;
    private static final int MAX_LONG = 0xff8;
    /**
     * Encodes one CE with a short/high primary (there are 60),
     * plus a secondary CE if the secondary weight is high.
     * Fast handling: At least all letter primaries should be in this range.
     */
    private static final int MIN_SHORT = 0x1000;
    private static final int SHORT_INC = 0x400;
    /** The highest primary weight is reserved for U+FFFF. */
    private static final int MAX_SHORT = SHORT_PRIMARY_MASK;

    private static final int MIN_SEC_BEFORE = 0;  // must add SEC_OFFSET
    private static final int SEC_INC = 0x20;
    private static final int MAX_SEC_BEFORE = MIN_SEC_BEFORE + 4 * SEC_INC;  // 5 before common
    private static final int COMMON_SEC = MAX_SEC_BEFORE + SEC_INC;
    private static final int MIN_SEC_AFTER = COMMON_SEC + SEC_INC;
    private static final int MAX_SEC_AFTER = MIN_SEC_AFTER + 5 * SEC_INC;  // 6 after common
    private static final int MIN_SEC_HIGH = MAX_SEC_AFTER + SEC_INC;  // 20 high secondaries
    private static final int MAX_SEC_HIGH = SECONDARY_MASK;

    /**
     * Lookup: Add this offset to secondary weights, except for completely ignorable CEs.
     * Must be greater than any special value, e.g., MERGE_WEIGHT.
     * The exact value is not relevant for the format version.
     */
    private static final int SEC_OFFSET = SEC_INC;
    private static final int COMMON_SEC_PLUS_OFFSET = COMMON_SEC + SEC_OFFSET;

    private static final int TWO_SEC_OFFSETS =
            (SEC_OFFSET << 16) | SEC_OFFSET;  // 0x200020
    private static final int TWO_COMMON_SEC_PLUS_OFFSET =
            (COMMON_SEC_PLUS_OFFSET << 16) | COMMON_SEC_PLUS_OFFSET;

    private static final int LOWER_CASE = 8;  // case bits include this offset
    private static final int TWO_LOWER_CASES = (LOWER_CASE << 16) | LOWER_CASE;  // 0x80008

    private static final int COMMON_TER = 0;  // must add TER_OFFSET
    private static final int MAX_TER_AFTER = 7;  // 7 after common

    /**
     * Lookup: Add this offset to tertiary weights, except for completely ignorable CEs.
     * Must be greater than any special value, e.g., MERGE_WEIGHT.
     * Must be greater than case bits as well, so that with combined case+tertiary weights
     * plus the offset the tertiary bits does not spill over into the case bits.
     * The exact value is not relevant for the format version.
     */
    private static final int TER_OFFSET = SEC_OFFSET;
    private static final int COMMON_TER_PLUS_OFFSET = COMMON_TER + TER_OFFSET;

    private static final int TWO_TER_OFFSETS = (TER_OFFSET << 16) | TER_OFFSET;
    private static final int TWO_COMMON_TER_PLUS_OFFSET =
            (COMMON_TER_PLUS_OFFSET << 16) | COMMON_TER_PLUS_OFFSET;

    private static final int MERGE_WEIGHT = 3;
    private static final int EOS = 2;  // end of string
    private static final int BAIL_OUT = 1;

    /**
     * Contraction result first word bits 8..0 contain the
     * second contraction character, as a char index 0..NUM_FAST_CHARS-1.
     * Each contraction list is terminated with a word containing CONTR_CHAR_MASK.
     */
    private static final int CONTR_CHAR_MASK = 0x1ff;
    /**
     * Contraction result first word bits 10..9 contain the result length:
     * 1=bail out, 2=one mini CE, 3=two mini CEs
     */
    private static final int CONTR_LENGTH_SHIFT = 9;

    /**
     * Comparison return value when the regular comparison must be used.
     * The exact value is not relevant for the format version.
     */
    private static final int BAIL_OUT_RESULT = -2;

    static int getCharIndex(UChar c) {
        if(c <= LATIN_MAX) {
            return c;
        } else if(PUNCT_START <= c && c < PUNCT_LIMIT) {
            return c - (PUNCT_START - LATIN_LIMIT);
        } else {
            // Not a fast Latin character.
            // Note: U+FFFE & U+FFFF are forbidden in tailorings
            // and thus do not occur in any contractions.
            return -1;
        }
    }

    /**
     * Computes the options value for the compare functions
     * and writes the precomputed primary weights.
     * Returns -1 if the Latin fastpath is not supported for the data and settings.
     * The capacity must be LATIN_LIMIT.
     */
    static int getOptions(CollationData data, const CollationSettings &settings,
                              char *primaries, int capacity);

    static int compareUTF16(const char *table, const char *primaries, int options,
                                const UChar *left, int leftLength,
                                const UChar *right, int rightLength);

    static int compareUTF8(const char *table, const char *primaries, int options,
                               const uint8_t *left, int leftLength,
                               const uint8_t *right, int rightLength);

private:
    static int lookup(const char *table, int c);

    static int nextPair(const char *table, int c, int ce,
                             const UChar *s16, int &sIndex, int &sLength);

    static int getPrimaries(int variableTop, int pair) {
        int ce = pair & 0xffff;
        if(ce >= MIN_SHORT) { return pair & TWO_SHORT_PRIMARIES_MASK; }
        if(ce > variableTop) { return pair & TWO_LONG_PRIMARIES_MASK; }
        if(ce >= MIN_LONG) { return 0; }  // variable
        return pair;  // special mini CE
    }
    static int getSecondariesFromOneShortCE(int ce) {
        ce &= SECONDARY_MASK;
        if(ce < MIN_SEC_HIGH) {
            return ce + SEC_OFFSET;
        } else {
            return ((ce + SEC_OFFSET) << 16) | COMMON_SEC_PLUS_OFFSET;
        }
    }
    static int getSecondaries(int variableTop, int pair);
    static int getCases(int variableTop, boolean strengthIsPrimary, int pair);
    static int getTertiaries(int variableTop, boolean withCaseBits, int pair);
    static int getQuaternaries(int variableTop, int pair);

private:
    CollationFastLatin();  // no constructor
};

/*
 * Format of the CollationFastLatin data table.
 * CollationFastLatin.VERSION = 1.
 *
 * This table contains data for a Latin-text collation fastpath.
 * The data is stored as an array of char which contains the following parts.
 *
 * char  -- version & header length
 *   Bits 15..8: version, must match the VERSION
 *         7..0: length of the header
 *
 * char varTops[header length - 1]
 *   Each of these values maps the variable top lead byte of a supported maxVariable group
 *   to the highest CollationFastLatin long-primary weight.
 *   The values are stored in ascending order.
 *   Bits 15..7: max fast-Latin long-primary weight (bits 11..3 shifted left by 4 bits)
 *         6..0: regular primary lead byte
 *
 * char miniCEs[0x1c0]
 *   A mini collation element for each character U+0000..U+017F and U+2000..U+203F.
 *   Each value encodes one or two mini CEs (two are possible if the first one
 *   has a short mini primary and the second one is a secondary CE, i.e., primary == 0),
 *   or points to an expansion or to a contraction table.
 *   U+0000 always has a contraction entry,
 *   so that NUL-termination need not be tested in the fastpath.
 *   If the collation elements for a character or contraction cannot be encoded in this format,
 *   then the BAIL_OUT value is stored.
 *   For details see the comments for the class constants.
 *
 * char expansions[variable length];
 *   Expansion mini CEs contain an offset relative to just after the miniCEs table.
 *   An expansions contains exactly 2 mini CEs.
 *
 * char contractions[variable length];
 *   Contraction mini CEs contain an offset relative to just after the miniCEs table.
 *   It points to a list of tuples which map from a contraction suffix character to a result.
 *   First char of each tuple:
 *     Bits 10..9: Length of the result (1..3), see comments on CONTR_LENGTH_SHIFT.
 *     Bits  8..0: Contraction character, see comments on CONTR_CHAR_MASK.
 *   This is followed by 0, 1, or 2 char according to the length.
 *   Each list is terminated by an entry with CONTR_CHAR_MASK.
 *   Each list starts with such an entry which also contains the default result
 *   for when there is no contraction match.
 */

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONFASTLATIN_H__
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationfastlatin.cpp
*
* @since 2013aug18
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/ucol.h"
#include "collationdata.h"
#include "collationfastlatin.h"
#include "collationsettings.h"
#include "putilimp.h"  // U_ALIGN_CODE
#include "uassert.h"

U_NAMESPACE_BEGIN

int32_t
CollationFastLatin.getOptions(CollationData data, const CollationSettings &settings,
                               char *primaries, int capacity) {
    const char *table = data.fastLatinTable;
    if(table == null) { return -1; }
    assert(capacity == LATIN_LIMIT);
    if(capacity != LATIN_LIMIT) { return -1; }

    int miniVarTop;
    if((settings.options & CollationSettings.ALTERNATE_MASK) == 0) {
        // No mini primaries are variable, set a variableTop just below the
        // lowest long mini primary.
        miniVarTop = MIN_LONG - 1;
    } else {
        int v1 = (int)(settings.variableTop >> 24);
        int headerLength = *table & 0xff;
        int i = headerLength - 1;
        if(i <= 0 || v1 > (table[i] & 0x7f)) {
            return -1;  // variableTop >= digits, should not occur
        }
        while(i > 1 && v1 <= (table[i - 1] & 0x7f)) { --i; }
        // In the table header, the miniVarTop is in bits 15..7, with 4 zero bits 19..16 implied.
        // Shift right to make it comparable with long mini primaries in bits 15..3.
        miniVarTop = (table[i] & 0xff80) >> 4;
    }

    const uint8_t *reorderTable = settings.reorderTable;
    if(reorderTable != null) {
        const char *scripts = data.scripts;
        int length = data.scriptsLength;
        int prevLastByte = 0;
        for(int i = 0; i < length;) {
            // reordered last byte of the group
            int lastByte = reorderTable[scripts[i] & 0xff];
            if(lastByte < prevLastByte) {
                // The permutation affects the groups up to Latin.
                return -1;
            }
            if(scripts[i + 2] == USCRIPT_LATIN) { break; }
            i = i + 2 + scripts[i + 1];
            prevLastByte = lastByte;
        }
    }

    table += (table[0] & 0xff);  // skip the header
    for(int c = 0; c < LATIN_LIMIT; ++c) {
        int p = table[c];
        if(p >= MIN_SHORT) {
            p &= SHORT_PRIMARY_MASK;
        } else if(p > miniVarTop) {
            p &= LONG_PRIMARY_MASK;
        } else {
            p = 0;
        }
        primaries[c] = (char)p;
    }
    if((settings.options & CollationSettings.NUMERIC) != 0) {
        // Bail out for digits.
        for(int c = 0x30; c <= 0x39; ++c) { primaries[c] = 0; }
    }

    // Shift the miniVarTop above other options.
    return ((int32_t)miniVarTop << 16) | settings.options;
}

int32_t
CollationFastLatin.compareUTF16(const char *table, const char *primaries, int options,
                                 const UChar *left, int leftLength,
                                 const UChar *right, int rightLength) {
    // This is a modified copy of CollationCompare.compareUpToQuaternary(),
    // optimized for common Latin text.
    // Keep them in sync!
    // Keep compareUTF16() and compareUTF8() in sync very closely!

    assert((table[0] >> 8) == VERSION);
    table += (table[0] & 0xff);  // skip the header
    int variableTop = options >> 16;  // see getOptions()
    options &= 0xffff;  // needed for CollationSettings.getStrength() to work

    // Check for supported characters, fetch mini CEs, and compare primaries.
    U_ALIGN_CODE(16);
    int leftIndex = 0, rightIndex = 0;
    /**
     * Single mini CE or a pair.
     * The current mini CE is in the lower 16 bits, the next one is in the upper 16 bits.
     * If there is only one, then it is in the lower bits, and the upper bits are 0.
     */
    int leftPair = 0, rightPair = 0;
    for(;;) {
        // We fetch CEs until we get a non-ignorable primary or reach the end.
        while(leftPair == 0) {
            if(leftIndex == leftLength) {
                leftPair = EOS;
                break;
            }
            int c = left[leftIndex++];
            if(c <= LATIN_MAX) {
                leftPair = primaries[c];
                if(leftPair != 0) { break; }
                if(c <= 0x39 && c >= 0x30 && (options & CollationSettings.NUMERIC) != 0) {
                    return BAIL_OUT_RESULT;
                }
                leftPair = table[c];
            } else if(PUNCT_START <= c && c < PUNCT_LIMIT) {
                leftPair = table[c - PUNCT_START + LATIN_LIMIT];
            } else {
                leftPair = lookup(table, c);
            }
            if(leftPair >= MIN_SHORT) {
                leftPair &= SHORT_PRIMARY_MASK;
                break;
            } else if(leftPair > variableTop) {
                leftPair &= LONG_PRIMARY_MASK;
                break;
            } else {
                leftPair = nextPair(table, c, leftPair, left, null, leftIndex, leftLength);
                if(leftPair == BAIL_OUT) { return BAIL_OUT_RESULT; }
                leftPair = getPrimaries(variableTop, leftPair);
            }
        }

        while(rightPair == 0) {
            if(rightIndex == rightLength) {
                rightPair = EOS;
                break;
            }
            int c = right[rightIndex++];
            if(c <= LATIN_MAX) {
                rightPair = primaries[c];
                if(rightPair != 0) { break; }
                if(c <= 0x39 && c >= 0x30 && (options & CollationSettings.NUMERIC) != 0) {
                    return BAIL_OUT_RESULT;
                }
                rightPair = table[c];
            } else if(PUNCT_START <= c && c < PUNCT_LIMIT) {
                rightPair = table[c - PUNCT_START + LATIN_LIMIT];
            } else {
                rightPair = lookup(table, c);
            }
            if(rightPair >= MIN_SHORT) {
                rightPair &= SHORT_PRIMARY_MASK;
                break;
            } else if(rightPair > variableTop) {
                rightPair &= LONG_PRIMARY_MASK;
                break;
            } else {
                rightPair = nextPair(table, c, rightPair, right, null, rightIndex, rightLength);
                if(rightPair == BAIL_OUT) { return BAIL_OUT_RESULT; }
                rightPair = getPrimaries(variableTop, rightPair);
            }
        }

        if(leftPair == rightPair) {
            if(leftPair == EOS) { break; }
            leftPair = rightPair = 0;
            continue;
        }
        int leftPrimary = leftPair & 0xffff;
        int rightPrimary = rightPair & 0xffff;
        if(leftPrimary != rightPrimary) {
            // Return the primary difference.
            return (leftPrimary < rightPrimary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftPair == EOS) { break; }
        leftPair >>>= 16;
        rightPair >>>= 16;
    }
    // In the following, we need to re-fetch each character because we did not buffer the CEs,
    // but we know that the string is well-formed and
    // only contains supported characters and mappings.

    // We might skip the secondary level but continue with the case level
    // which is turned on separately.
    if(CollationSettings.getStrength(options) >= UCOL_SECONDARY) {
        leftIndex = rightIndex = 0;
        leftPair = rightPair = 0;
        for(;;) {
            while(leftPair == 0) {
                if(leftIndex == leftLength) {
                    leftPair = EOS;
                    break;
                }
                int c = left[leftIndex++];
                if(c <= LATIN_MAX) {
                    leftPair = table[c];
                } else if(PUNCT_START <= c && c < PUNCT_LIMIT) {
                    leftPair = table[c - PUNCT_START + LATIN_LIMIT];
                } else {
                    leftPair = lookup(table, c);
                }
                if(leftPair >= MIN_SHORT) {
                    leftPair = getSecondariesFromOneShortCE(leftPair);
                    break;
                } else if(leftPair > variableTop) {
                    leftPair = COMMON_SEC_PLUS_OFFSET;
                    break;
                } else {
                    leftPair = nextPair(table, c, leftPair, left, null, leftIndex, leftLength);
                    leftPair = getSecondaries(variableTop, leftPair);
                }
            }

            while(rightPair == 0) {
                if(rightIndex == rightLength) {
                    rightPair = EOS;
                    break;
                }
                int c = right[rightIndex++];
                if(c <= LATIN_MAX) {
                    rightPair = table[c];
                } else if(PUNCT_START <= c && c < PUNCT_LIMIT) {
                    rightPair = table[c - PUNCT_START + LATIN_LIMIT];
                } else {
                    rightPair = lookup(table, c);
                }
                if(rightPair >= MIN_SHORT) {
                    rightPair = getSecondariesFromOneShortCE(rightPair);
                    break;
                } else if(rightPair > variableTop) {
                    rightPair = COMMON_SEC_PLUS_OFFSET;
                    break;
                } else {
                    rightPair = nextPair(table, c, rightPair, right, null, rightIndex, rightLength);
                    rightPair = getSecondaries(variableTop, rightPair);
                }
            }

            if(leftPair == rightPair) {
                if(leftPair == EOS) { break; }
                leftPair = rightPair = 0;
                continue;
            }
            int leftSecondary = leftPair & 0xffff;
            int rightSecondary = rightPair & 0xffff;
            if(leftSecondary != rightSecondary) {
                if((options & CollationSettings.BACKWARD_SECONDARY) != 0) {
                    // Full support for backwards secondary requires backwards contraction matching
                    // and moving backwards between merge separators.
                    return BAIL_OUT_RESULT;
                }
                return (leftSecondary < rightSecondary) ? UCOL_LESS : UCOL_GREATER;
            }
            if(leftPair == EOS) { break; }
            leftPair >>>= 16;
            rightPair >>>= 16;
        }
    }

    if((options & CollationSettings.CASE_LEVEL) != 0) {
        boolean strengthIsPrimary = CollationSettings.getStrength(options) == UCOL_PRIMARY;
        leftIndex = rightIndex = 0;
        leftPair = rightPair = 0;
        for(;;) {
            while(leftPair == 0) {
                if(leftIndex == leftLength) {
                    leftPair = EOS;
                    break;
                }
                int c = left[leftIndex++];
                leftPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
                if(leftPair < MIN_LONG) {
                    leftPair = nextPair(table, c, leftPair, left, null, leftIndex, leftLength);
                }
                leftPair = getCases(variableTop, strengthIsPrimary, leftPair);
            }

            while(rightPair == 0) {
                if(rightIndex == rightLength) {
                    rightPair = EOS;
                    break;
                }
                int c = right[rightIndex++];
                rightPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
                if(rightPair < MIN_LONG) {
                    rightPair = nextPair(table, c, rightPair, right, null, rightIndex, rightLength);
                }
                rightPair = getCases(variableTop, strengthIsPrimary, rightPair);
            }

            if(leftPair == rightPair) {
                if(leftPair == EOS) { break; }
                leftPair = rightPair = 0;
                continue;
            }
            int leftCase = leftPair & 0xffff;
            int rightCase = rightPair & 0xffff;
            if(leftCase != rightCase) {
                if((options & CollationSettings.UPPER_FIRST) == 0) {
                    return (leftCase < rightCase) ? UCOL_LESS : UCOL_GREATER;
                } else {
                    return (leftCase < rightCase) ? UCOL_GREATER : UCOL_LESS;
                }
            }
            if(leftPair == EOS) { break; }
            leftPair >>>= 16;
            rightPair >>>= 16;
        }
    }
    if(CollationSettings.getStrength(options) <= UCOL_SECONDARY) { return UCOL_EQUAL; }

    // Remove the case bits from the tertiary weight when caseLevel is on or caseFirst is off.
    boolean withCaseBits = CollationSettings.isTertiaryWithCaseBits(options);

    leftIndex = rightIndex = 0;
    leftPair = rightPair = 0;
    for(;;) {
        while(leftPair == 0) {
            if(leftIndex == leftLength) {
                leftPair = EOS;
                break;
            }
            int c = left[leftIndex++];
            leftPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
            if(leftPair < MIN_LONG) {
                leftPair = nextPair(table, c, leftPair, left, null, leftIndex, leftLength);
            }
            leftPair = getTertiaries(variableTop, withCaseBits, leftPair);
        }

        while(rightPair == 0) {
            if(rightIndex == rightLength) {
                rightPair = EOS;
                break;
            }
            int c = right[rightIndex++];
            rightPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
            if(rightPair < MIN_LONG) {
                rightPair = nextPair(table, c, rightPair, right, null, rightIndex, rightLength);
            }
            rightPair = getTertiaries(variableTop, withCaseBits, rightPair);
        }

        if(leftPair == rightPair) {
            if(leftPair == EOS) { break; }
            leftPair = rightPair = 0;
            continue;
        }
        int leftTertiary = leftPair & 0xffff;
        int rightTertiary = rightPair & 0xffff;
        if(leftTertiary != rightTertiary) {
            if(CollationSettings.sortsTertiaryUpperCaseFirst(options)) {
                // Pass through EOS and MERGE_WEIGHT
                // and keep real tertiary weights larger than the MERGE_WEIGHT.
                // Tertiary CEs (secondary ignorables) are not supported in fast Latin.
                if(leftTertiary > MERGE_WEIGHT) {
                    leftTertiary ^= CASE_MASK;
                }
                if(rightTertiary > MERGE_WEIGHT) {
                    rightTertiary ^= CASE_MASK;
                }
            }
            return (leftTertiary < rightTertiary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftPair == EOS) { break; }
        leftPair >>>= 16;
        rightPair >>>= 16;
    }
    if(CollationSettings.getStrength(options) <= UCOL_TERTIARY) { return UCOL_EQUAL; }

    leftIndex = rightIndex = 0;
    leftPair = rightPair = 0;
    for(;;) {
        while(leftPair == 0) {
            if(leftIndex == leftLength) {
                leftPair = EOS;
                break;
            }
            int c = left[leftIndex++];
            leftPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
            if(leftPair < MIN_LONG) {
                leftPair = nextPair(table, c, leftPair, left, null, leftIndex, leftLength);
            }
            leftPair = getQuaternaries(variableTop, leftPair);
        }

        while(rightPair == 0) {
            if(rightIndex == rightLength) {
                rightPair = EOS;
                break;
            }
            int c = right[rightIndex++];
            rightPair = (c <= LATIN_MAX) ? table[c] : lookup(table, c);
            if(rightPair < MIN_LONG) {
                rightPair = nextPair(table, c, rightPair, right, null, rightIndex, rightLength);
            }
            rightPair = getQuaternaries(variableTop, rightPair);
        }

        if(leftPair == rightPair) {
            if(leftPair == EOS) { break; }
            leftPair = rightPair = 0;
            continue;
        }
        int leftQuaternary = leftPair & 0xffff;
        int rightQuaternary = rightPair & 0xffff;
        if(leftQuaternary != rightQuaternary) {
            return (leftQuaternary < rightQuaternary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftPair == EOS) { break; }
        leftPair >>>= 16;
        rightPair >>>= 16;
    }
    return UCOL_EQUAL;
}

int
CollationFastLatin.lookup(char[] table, int c) {
    assert(c > LATIN_MAX);
    if(PUNCT_START <= c && c < PUNCT_LIMIT) {
        return table[c - PUNCT_START + LATIN_LIMIT];
    } else if(c == 0xfffe) {
        return MERGE_WEIGHT;
    } else if(c == 0xffff) {
        return MAX_SHORT | COMMON_SEC | LOWER_CASE | COMMON_TER;
    } else {
        return BAIL_OUT;
    }
}

int
CollationFastLatin.nextPair(char[] table, int c, int ce,
                             const UChar *s16, int &sIndex, int &sLength) {
    if(ce >= MIN_LONG || ce < CONTRACTION) {
        return ce;  // simple or special mini CE
    } else if(ce >= EXPANSION) {
        int index = NUM_FAST_CHARS + (ce & INDEX_MASK);
        return ((int)table[index + 1] << 16) | table[index];
    } else /* ce >= CONTRACTION */ {
        if(c == 0 && sLength < 0) {
            sLength = sIndex - 1;
            return EOS;
        }
        // Contraction list: Default mapping followed by
        // 0 or more single-character contraction suffix mappings.
        int index = NUM_FAST_CHARS + (ce & INDEX_MASK);
        if(sIndex != sLength) {
            // Read the next character.
            int c2;
            int nextIndex = sIndex;
            c2 = s16[nextIndex++];
            if(c2 > LATIN_MAX) {
                if(PUNCT_START <= c2 && c2 < PUNCT_LIMIT) {
                    c2 = c2 - PUNCT_START + LATIN_LIMIT;  // 2000..203F . 0180..01BF
                } else if(c2 == 0xfffe || c2 == 0xffff) {
                    c2 = -1;  // U+FFFE & U+FFFF cannot occur in contractions.
                } else {
                    return BAIL_OUT;
                }
            }
            if(c2 == 0 && sLength < 0) {
                sLength = sIndex;
                c2 = -1;
            }
            // Look for the next character in the contraction suffix list,
            // which is in ascending order of single suffix characters.
            int i = index;
            int head = table[i];  // first skip the default mapping
            int x;
            do {
                i += head >> CONTR_LENGTH_SHIFT;
                head = table[i];
                x = head & CONTR_CHAR_MASK;
            } while(x < c2);
            if(x == c2) {
                index = i;
                sIndex = nextIndex;
            }
        }
        // Return the CE or CEs for the default or contraction mapping.
        int length = table[index] >> CONTR_LENGTH_SHIFT;
        if(length == 1) {
            return BAIL_OUT;
        }
        ce = table[index + 1];
        if(length == 2) {
            return ce;
        } else {
            return ((int)table[index + 2] << 16) | ce;
        }
    }
}

int
CollationFastLatin.getSecondaries(int variableTop, int pair) {
    if(pair <= 0xffff) {
        // one mini CE
        if(pair >= MIN_SHORT) {
            pair = getSecondariesFromOneShortCE(pair);
        } else if(pair > variableTop) {
            pair = COMMON_SEC_PLUS_OFFSET;
        } else if(pair >= MIN_LONG) {
            pair = 0;  // variable
        }
        // else special mini CE
    } else {
        int ce = pair & 0xffff;
        if(ce >= MIN_SHORT) {
            pair = (pair & TWO_SECONDARIES_MASK) + TWO_SEC_OFFSETS;
        } else if(ce > variableTop) {
            pair = TWO_COMMON_SEC_PLUS_OFFSET;
        } else {
            assert(ce >= MIN_LONG);
            pair = 0;  // variable
        }
    }
    return pair;
}

int
CollationFastLatin.getCases(int variableTop, boolean strengthIsPrimary, int pair) {
    // Primary+caseLevel: Ignore case level weights of primary ignorables.
    // Otherwise: Ignore case level weights of secondary ignorables.
    // For details see the comments in the CollationCompare class.
    // Tertiary CEs (secondary ignorables) are not supported in fast Latin.
    if(pair <= 0xffff) {
        // one mini CE
        if(pair >= MIN_SHORT) {
            // A high secondary weight means we really have two CEs,
            // a primary CE and a secondary CE.
            int ce = pair;
            pair &= CASE_MASK;  // explicit weight of primary CE
            if(!strengthIsPrimary && (ce & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                pair |= LOWER_CASE << 16;  // implied weight of secondary CE
            }
        } else if(pair > variableTop) {
            pair = LOWER_CASE;
        } else if(pair >= MIN_LONG) {
            pair = 0;  // variable
        }
        // else special mini CE
    } else {
        // two mini CEs, same primary groups, neither expands like above
        int ce = pair & 0xffff;
        if(ce >= MIN_SHORT) {
            if(strengthIsPrimary && (pair & (SHORT_PRIMARY_MASK << 16)) == 0) {
                pair &= CASE_MASK;
            } else {
                pair &= TWO_CASES_MASK;
            }
        } else if(ce > variableTop) {
            pair = TWO_LOWER_CASES;
        } else {
            assert(ce >= MIN_LONG);
            pair = 0;  // variable
        }
    }
    return pair;
}

int
CollationFastLatin.getTertiaries(int variableTop, boolean withCaseBits, int pair) {
    if(pair <= 0xffff) {
        // one mini CE
        if(pair >= MIN_SHORT) {
            // A high secondary weight means we really have two CEs,
            // a primary CE and a secondary CE.
            int ce = pair;
            if(withCaseBits) {
                pair = (pair & CASE_AND_TERTIARY_MASK) + TER_OFFSET;
                if((ce & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                    pair |= (LOWER_CASE | COMMON_TER_PLUS_OFFSET) << 16;
                }
            } else {
                pair = (pair & TERTIARY_MASK) + TER_OFFSET;
                if((ce & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                    pair |= COMMON_TER_PLUS_OFFSET << 16;
                }
            }
        } else if(pair > variableTop) {
            pair = (pair & TERTIARY_MASK) + TER_OFFSET;
            if(withCaseBits) {
                pair |= LOWER_CASE;
            }
        } else if(pair >= MIN_LONG) {
            pair = 0;  // variable
        }
        // else special mini CE
    } else {
        // two mini CEs, same primary groups, neither expands like above
        int ce = pair & 0xffff;
        if(ce >= MIN_SHORT) {
            if(withCaseBits) {
                pair &= TWO_CASES_MASK | TWO_TERTIARIES_MASK;
            } else {
                pair &= TWO_TERTIARIES_MASK;
            }
            pair += TWO_TER_OFFSETS;
        } else if(ce > variableTop) {
            pair = (pair & TWO_TERTIARIES_MASK) + TWO_TER_OFFSETS;
            if(withCaseBits) {
                pair |= TWO_LOWER_CASES;
            }
        } else {
            assert(ce >= MIN_LONG);
            pair = 0;  // variable
        }
    }
    return pair;
}

int
CollationFastLatin.getQuaternaries(int variableTop, int pair) {
    // Return the primary weight of a variable CE,
    // or the maximum primary weight for a non-variable, not-completely-ignorable CE.
    if(pair <= 0xffff) {
        // one mini CE
        if(pair >= MIN_SHORT) {
            // A high secondary weight means we really have two CEs,
            // a primary CE and a secondary CE.
            if((pair & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                pair = TWO_SHORT_PRIMARIES_MASK;
            } else {
                pair = SHORT_PRIMARY_MASK;
            }
        } else if(pair > variableTop) {
            pair = SHORT_PRIMARY_MASK;
        } else if(pair >= MIN_LONG) {
            pair &= LONG_PRIMARY_MASK;  // variable
        }
        // else special mini CE
    } else {
        // two mini CEs, same primary groups, neither expands like above
        int ce = pair & 0xffff;
        if(ce > variableTop) {
            pair = TWO_SHORT_PRIMARIES_MASK;
        } else {
            assert(ce >= MIN_LONG);
            pair &= TWO_LONG_PRIMARIES_MASK;  // variable
        }
    }
    return pair;
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
