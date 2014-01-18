/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationfastlatinbuilder.h
*
* @since 2013aug09
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

#include "unicode/unistr.h"
#include "unicode/uobject.h"
#include "collation.h"
#include "collationfastlatin.h"
#include "uvectr64.h"

U_NAMESPACE_BEGIN

struct CollationData;

final class CollationFastLatinBuilder {
public:
    CollationFastLatinBuilder();
    ~CollationFastLatinBuilder();

    boolean forData(CollationData data);

    const char *getTable() {
        return reinterpret_cast<const char *>(result.getBuffer());
    }
    int lengthOfTable() { return result.length(); }

private:
    boolean loadGroups(CollationData data);
    boolean inSameGroup(long p, long q);

    void resetCEs();
    void getCEs(CollationData data);
    boolean getCEsFromCE32(CollationData data, int c, int ce32,
                         );
    boolean getCEsFromContractionCE32(CollationData data, int ce32,
                                    );
    static int getSuffixFirstCharIndex(const UnicodeString &suffix);
    void addContractionEntry(int x, long cce0, long cce1);
    void addUniqueCE(long ce);
    int getMiniCE(long ce);
    boolean encodeUniqueCEs();
    boolean encodeCharCEs();
    boolean encodeExpansions();
    boolean encodeContractions();
    int encodeTwoCEs(long first, long second);

    static boolean isContractionCharCE(long ce) {
        return (ce >>> 32) == Collation.NO_CE_PRIMARY && ce != Collation.NO_CE;
    }

    private static final long CONTRACTION_FLAG = 0x80000000;

    // temporary "buffer"
    long ce0, ce1;

    long charCEs[CollationFastLatin.NUM_FAST_CHARS][2];

    UVector64 contractionCEs;
    UVector64 uniqueCEs;

    /** One 16-bit mini CE per unique CE. */
    char[] miniCEs;

    // These are constant for a given list of CollationData.scripts.
    long firstDigitPrimary;
    long firstLatinPrimary;
    long lastLatinPrimary;
    // This determines the first normal primary weight which is mapped to
    // a short mini primary. It must be >=firstDigitPrimary.
    long firstShortPrimary;

    boolean shortPrimaryOverflow;

    UnicodeString result;
    int headerLength;
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONFASTLATINBUILDER_H__
/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationfastlatinbuilder.cpp
*
* @since 2013aug09
* @author Markus W. Scherer
*/

#define DEBUG_COLLATION_FAST_LATIN_BUILDER 0  // 0 or 1 or 2
#if DEBUG_COLLATION_FAST_LATIN_BUILDER
#include <stdio.h>
#include <string>
#endif

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/ucol.h"
#include "unicode/ucharstrie.h"
#include "unicode/unistr.h"
#include "unicode/uobject.h"
#include "unicode/uscript.h"
#include "cmemory.h"
#include "collation.h"
#include "collationdata.h"
#include "collationfastlatin.h"
#include "collationfastlatinbuilder.h"
#include "uassert.h"
#include "uvectr64.h"

U_NAMESPACE_BEGIN

struct CollationData;

namespace {

/**
 * Compare two signed long values as if they were unsigned.
 */
int32_t
compareInt64AsUnsigned(long a, long b) {
    if((ulong)a < (ulong)b) {
        return -1;
    } else if((ulong)a > (ulong)b) {
        return 1;
    } else {
        return 0;
    }
}

// TODO: Merge this with the near-identical version in collationbasedatabuilder.cpp
/**
 * Like Java Collections.binarySearch(List, String, Comparator).
 *
 * @return the index>=0 where the item was found,
 *         or the index<0 for inserting the string at ~index in sorted order
 */
int32_t
binarySearch(const long list[], int limit, long ce) {
    if (limit == 0) { return ~0; }
    int start = 0;
    for (;;) {
        int i = (start + limit) / 2;
        int cmp = compareInt64AsUnsigned(ce, list[i]);
        if (cmp == 0) {
            return i;
        } else if (cmp < 0) {
            if (i == start) {
                return ~start;  // insert ce before i
            }
            limit = i;
        } else {
            if (i == start) {
                return ~(start + 1);  // insert ce after i
            }
            start = i;
        }
    }
}

}  // namespace

CollationFastLatinBuilder.CollationFastLatinBuilder()
        : ce0(0), ce1(0),
          contractionCEs, uniqueCEs,
          miniCEs(null),
          firstDigitPrimary(0), firstLatinPrimary(0), lastLatinPrimary(0),
          firstShortPrimary(0), shortPrimaryOverflow(false),
          headerLength(0) {
}

CollationFastLatinBuilder.~CollationFastLatinBuilder() {
    uprv_free(miniCEs);
}

boolean
CollationFastLatinBuilder.forData(CollationData data) {
    if(U_FAILURE) { return false; }
    if(!result.isEmpty()) {  // This builder is not reusable.
        errorCode = U_INVALID_STATE_ERROR;
        return false;
    }
    if(!loadGroups(data)) { return false; }

    // Fast handling of digits.
    firstShortPrimary = firstDigitPrimary;
    getCEs(data);
    if(!encodeUniqueCEs) { return false; }
    if(shortPrimaryOverflow) {
        // Give digits long mini primaries,
        // so that there are more short primaries for letters.
        firstShortPrimary = firstLatinPrimary;
        resetCEs();
        getCEs(data);
        if(!encodeUniqueCEs) { return false; }
    }
    // Note: If we still have a short-primary overflow but not a long-primary overflow,
    // then we could calculate how many more long primaries would fit,
    // and set the firstShortPrimary to that many after the current firstShortPrimary,
    // and try again.
    // However, this might only benefit the en_US_POSIX tailoring,
    // and it is simpler to suppress building fast Latin data for it in genrb,
    // or by returning false here if shortPrimaryOverflow.

    boolean ok = !shortPrimaryOverflow &&
            encodeCharCEs && encodeContractions;
    contractionCEs.removeAllElements();  // might reduce heap memory usage
    uniqueCEs.removeAllElements();
    return ok;
}

boolean
CollationFastLatinBuilder.loadGroups(CollationData data) {
    if(U_FAILURE) { return false; }
    result.append(0);  // reserved for version & headerLength
    // The first few reordering groups should be special groups
    // (space, punct, ..., digit) followed by Latn, then Grek and other scripts.
    for(int i = 0;;) {
        if(i >= data.scriptsLength) {
            // no Latn script
            errorCode = U_INTERNAL_PROGRAM_ERROR;
            return false;
        }
        int head = data.scripts[i];
        int lastByte = head & 0xff;  // last primary byte in the group
        int group = data.scripts[i + 2];
        if(group == Collator.ReorderCodes.DIGIT) {
            firstDigitPrimary = (long)(head & 0xff00) << 16;
            headerLength = result.length();
            int r0 = (CollationFastLatin.VERSION << 8) | headerLength;
            result.setCharAt(0, (UChar)r0);
        } else if(group == USCRIPT_LATIN) {
            if(firstDigitPrimary == 0) {
                // no digit group
                errorCode = U_INTERNAL_PROGRAM_ERROR;
                return false;
            }
            firstLatinPrimary = (long)(head & 0xff00) << 16;
            lastLatinPrimary = ((long)lastByte << 24) | 0xffffff;
            break;
        } else if(firstDigitPrimary == 0) {
            // a group below digits
            if(lastByte > 0x7f) {
                // We only use 7 bits for the last byte of a below-digits group.
                // This does not warrant an errorCode, but we do not build a fast Latin table.
                return false;
            }
            result.append((char)lastByte);
        }
        i = i + 2 + data.scripts[i + 1];
    }
    return true;
}

boolean
CollationFastLatinBuilder.inSameGroup(long p, long q) {
    // Both or neither need to be encoded as short primaries,
    // so that we can test only one and use the same bit mask.
    if(p >= firstShortPrimary) {
        return q >= firstShortPrimary;
    } else if(q >= firstShortPrimary) {
        return false;
    }
    // Both or neither must be potentially-variable,
    // so that we can test only one and determine if both are variable.
    if(p >= firstDigitPrimary) {
        return q >= firstDigitPrimary;
    } else if(q >= firstDigitPrimary) {
        return false;
    }
    // Both will be encoded with long mini primaries.
    // They must be in the same special reordering group,
    // so that we can test only one and determine if both are variable.
    p >>= 24;  // first primary byte
    q >>= 24;
    assert(p != 0 && q != 0);
    assert(p <= result[headerLength - 1]);  // the loop will terminate
    for(int i = 1;; ++i) {
        long lastByte = result[i];
        if(p <= lastByte) {
            return q <= lastByte;
        } else if(q <= lastByte) {
            return false;
        }
    }
}

void
CollationFastLatinBuilder.resetCEs() {
    contractionCEs.removeAllElements();
    uniqueCEs.removeAllElements();
    shortPrimaryOverflow = false;
    result.truncate(headerLength);
}

void
CollationFastLatinBuilder.getCEs(CollationData data) {
    if(U_FAILURE) { return; }
    int i = 0;
    for(UChar c = 0;; ++i, ++c) {
        if(c == CollationFastLatin.LATIN_LIMIT) {
            c = CollationFastLatin.PUNCT_START;
        } else if(c == CollationFastLatin.PUNCT_LIMIT) {
            break;
        }
        CollationData d;
        int ce32 = data.getCE32(c);
        if(ce32 == Collation.FALLBACK_CE32) {
            d = data.base;
            ce32 = d.getCE32(c);
        } else {
            d = &data;
        }
        if(getCEsFromCE32(*d, c, ce32)) {
            charCEs[i][0] = ce0;
            charCEs[i][1] = ce1;
            addUniqueCE(ce0);
            addUniqueCE(ce1);
        } else {
            // bail out for c
            charCEs[i][0] = ce0 = Collation.NO_CE;
            charCEs[i][1] = ce1 = 0;
        }
        if(c == 0 && !isContractionCharCE(ce0)) {
            // Always map U+0000 to a contraction.
            // Write a contraction list with only a default value if there is no real contraction.
            assert(contractionCEs.isEmpty());
            addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, ce0, ce1);
            charCEs[0][0] = (Collation.NO_CE_PRIMARY << 32) | CONTRACTION_FLAG;
            charCEs[0][1] = 0;
        }
    }
    // Terminate the last contraction list.
    contractionCEs.addElement(CollationFastLatin.CONTR_CHAR_MASK);
}

boolean
CollationFastLatinBuilder.getCEsFromCE32(CollationData data, int c, int ce32,
                                          ) {
    if(U_FAILURE) { return false; }
    ce32 = data.getFinalCE32(ce32);
    ce1 = 0;
    if(Collation.isSimpleOrLongCE32(ce32)) {
        ce0 = Collation.ceFromCE32(ce32);
    } else {
        switch(Collation.tagFromCE32(ce32)) {
        case Collation.LATIN_EXPANSION_TAG:
            ce0 = Collation.latinCE0FromCE32(ce32);
            ce1 = Collation.latinCE1FromCE32(ce32);
            break;
        case Collation.EXPANSION32_TAG: {
            const uint32_t *ce32s = data.ce32s + Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);
            if(length <= 2) {
                ce0 = Collation.ceFromCE32(ce32s[0]);
                if(length == 2) {
                    ce1 = Collation.ceFromCE32(ce32s[1]);
                }
                break;
            } else {
                return false;
            }
        }
        case Collation.EXPANSION_TAG: {
            const long *ces = data.ces + Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);
            if(length <= 2) {
                ce0 = ces[0];
                if(length == 2) {
                    ce1 = ces[1];
                }
                break;
            } else {
                return false;
            }
        }
        // Note: We could support PREFIX_TAG (assert c>=0)
        // by recursing on its default CE32 and checking that none of the prefixes starts
        // with a fast Latin character.
        // However, currently (2013) there are only the L-before-middle-dot
        // prefix mappings in the Latin range, and those would be rejected anyway.
        case Collation.CONTRACTION_TAG:
            assert(c >= 0);
            return getCEsFromContractionCE32(data, ce32);
        case Collation.OFFSET_TAG:
            assert(c >= 0);
            ce0 = data.getCEFromOffsetCE32(c, ce32);
            break;
        default:
            return false;
        }
    }
    // A mapping can be completely ignorable.
    if(ce0 == 0) { return ce1 == 0; }
    // We do not support an ignorable ce0 unless it is completely ignorable.
    long p0 = ce0 >>> 32;
    if(p0 == 0) { return false; }
    // We only support primaries up to the Latin script.
    if(p0 > lastLatinPrimary) { return false; }
    // We support non-common secondary and case weights only together with short primaries.
    int lower32_0 = (int)ce0;
    if(p0 < firstShortPrimary) {
        int sc0 = lower32_0 & Collation.SECONDARY_AND_CASE_MASK;
        if(sc0 != Collation.COMMON_SECONDARY_CE) { return false; }
    }
    // No below-common tertiary weights.
    if((lower32_0 & Collation.ONLY_TERTIARY_MASK) < Collation.COMMON_WEIGHT16) { return false; }
    if(ce1 != 0) {
        // Both primaries must be in the same group,
        // or both must get short mini primaries,
        // or a short-primary CE is followed by a secondary CE.
        // This is so that we can test the first primary and use the same mask for both,
        // and determine for both whether they are variable.
        long p1 = ce1 >>> 32;
        if(p1 == 0 ? p0 < firstShortPrimary : !inSameGroup(p0, p1)) { return false; }
        int lower32_1 = (int)ce1;
        // No tertiary CEs.
        if((lower32_1 >>> 16) == 0) { return false; }
        // We support non-common secondary and case weights
        // only for secondary CEs or together with short primaries.
        if(p1 != 0 && p1 < firstShortPrimary) {
            int sc1 = lower32_1 & Collation.SECONDARY_AND_CASE_MASK;
            if(sc1 != Collation.COMMON_SECONDARY_CE) { return false; }
        }
        // No below-common tertiary weights.
        if(Utility.compareUnsigned(lower32_1 & Collation.ONLY_TERTIARY_MASK, Collation.COMMON_WEIGHT16) < 0) { return false; }
    }
    // No quaternary weights.
    if(((ce0 | ce1) & Collation.QUATERNARY_MASK) != 0) { return false; }
    return true;
}

boolean
CollationFastLatinBuilder.getCEsFromContractionCE32(CollationData data, int ce32,
                                                     ) {
    if(U_FAILURE) { return false; }
    int trieIndex = Collation.indexFromCE32(ce32);
    ce32 = data.getCE32FromContexts(trieIndex);  // Default if no suffix match.
    // Since the original ce32 is not a prefix mapping,
    // the default ce32 must not be another contraction.
    assert(!Collation.isContractionCE32(ce32));
    int contractionIndex = contractionCEs.size();
    if(getCEsFromCE32(data, Collation.SENTINEL_CP, ce32)) {
        addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, ce0, ce1);
    } else {
        // Bail out for c-without-contraction.
        addContractionEntry(CollationFastLatin.CONTR_CHAR_MASK, Collation.NO_CE, 0);
    }
    // Handle an encodable contraction unless the next contraction is too long
    // and starts with the same character.
    int prevX = -1;
    boolean addContraction = false;
    UCharsTrie.Iterator suffixes(data.contexts, trieIndex + 2, 0);
    while(suffixes.next) {
        const UnicodeString &suffix = suffixes.getString();
        int x = getSuffixFirstCharIndex(suffix);
        if(x < 0) { continue; }  // ignore anything but fast Latin text
        if(x == prevX) {
            if(addContraction) {
                // Bail out for all contractions starting with this character.
                addContractionEntry(x, Collation.NO_CE, 0);
                addContraction = false;
            }
            continue;
        }
        if(addContraction) {
            addContractionEntry(prevX, ce0, ce1);
        }
        ce32 = suffixes.getValue();
        if(suffix.length() == 1 && getCEsFromCE32(data, Collation.SENTINEL_CP, ce32)) {
            addContraction = true;
        } else {
            addContractionEntry(x, Collation.NO_CE, 0);
            addContraction = false;
        }
        prevX = x;
    }
    if(addContraction) {
        addContractionEntry(prevX, ce0, ce1);
    }
    if(U_FAILURE) { return false; }
    // Note: There might not be any fast Latin contractions, but
    // we need to enter contraction handling anyway so that we can bail out
    // when there is a non-fast-Latin character following.
    // For example: Danish &Y<<u+umlaut, when we compare Y vs. u\u0308 we need to see the
    // following umlaut and bail out, rather than return the difference of Y vs. u.
    ce0 = (Collation.NO_CE_PRIMARY << 32) | CONTRACTION_FLAG | contractionIndex;
    ce1 = 0;
    return true;
}

int32_t
CollationFastLatinBuilder.getSuffixFirstCharIndex(const UnicodeString &suffix) {
    int x = CollationFastLatin.getCharIndex(suffix.charAt(0));
    int length = suffix.length();
    if(x >= 0 && length > 0) {
        // Ignore the contraction if it contains non-fast-Latin characters.
        for(int i = 1; i < length; ++i) {
            if(CollationFastLatin.getCharIndex(suffix.charAt(i)) < 0) {
                return -1;
            }
        }
    }
    return x;
}

void
CollationFastLatinBuilder.addContractionEntry(int x, long cce0, long cce1,
                                               ) {
    contractionCEs.addElement(x);
    contractionCEs.addElement(cce0);
    contractionCEs.addElement(cce1);
    addUniqueCE(cce0);
    addUniqueCE(cce1);
}

void
CollationFastLatinBuilder.addUniqueCE(long ce) {
    if(U_FAILURE) { return; }
    if(ce == 0 || (ce >>> 32) == Collation.NO_CE_PRIMARY) { return; }
    ce &= ~(long)Collation.CASE_MASK;  // blank out case bits
    int i = binarySearch(uniqueCEs.getBuffer(), uniqueCEs.size(), ce);
    if(i < 0) {
        uniqueCEs.insertElementAt(ce, ~i);
    }
}

int
CollationFastLatinBuilder.getMiniCE(long ce) {
    ce &= ~(long)Collation.CASE_MASK;  // blank out case bits
    int index = binarySearch(uniqueCEs.getBuffer(), uniqueCEs.size(), ce);
    assert(index >= 0);
    return miniCEs[index];
}

boolean
CollationFastLatinBuilder.encodeUniqueCEs() {
    if(U_FAILURE) { return false; }
    uprv_free(miniCEs);
    miniCEs = (char *)uprv_malloc(uniqueCEs.size() * 2);
    if(miniCEs == null) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return false;
    }
    int group = 1;
    long lastGroupByte = result[group];
    // The lowest unique CE must be at least a secondary CE.
    assert(((int)uniqueCEs.elementAti(0) >>> 16) != 0);
    long prevPrimary = 0;
    int prevSecondary = 0;
    int pri = 0;
    int sec = 0;
    int ter = CollationFastLatin.COMMON_TER;
    for(int i = 0; i < uniqueCEs.size(); ++i) {
        long ce = uniqueCEs.elementAti(i);
        // Note: At least one of the p/s/t weights changes from one unique CE to the next.
        // (uniqueCEs does not store case bits.)
        long p = ce >>> 32;
        if(p != prevPrimary) {
            int p1 = (int)(p >> 24);
            while(p1 > lastGroupByte) {
                assert(pri <= CollationFastLatin.MAX_LONG);
                // Add the last "long primary" in or before the group
                // into the upper 9 bits of the group entry.
                result.setCharAt(group, (UChar)((pri << 4) | lastGroupByte));
                if(++group < headerLength) {  // group is 1-based
                    lastGroupByte = result[group];
                } else {
                    lastGroupByte = 0xff;
                    break;
                }
            }
            if(p < firstShortPrimary) {
                if(pri == 0) {
                    pri = CollationFastLatin.MIN_LONG;
                } else if(pri < CollationFastLatin.MAX_LONG) {
                    pri += CollationFastLatin.LONG_INC;
                } else {
#if DEBUG_COLLATION_FAST_LATIN_BUILDER
                    printf("long-primary overflow for %08x\n", p);
#endif
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
            } else {
                if(pri < CollationFastLatin.MIN_SHORT) {
                    pri = CollationFastLatin.MIN_SHORT;
                } else if(pri < (CollationFastLatin.MAX_SHORT - CollationFastLatin.SHORT_INC)) {
                    // Reserve the highest primary weight for U+FFFF.
                    pri += CollationFastLatin.SHORT_INC;
                } else {
#if DEBUG_COLLATION_FAST_LATIN_BUILDER
                    printf("short-primary overflow for %08x\n", p);
#endif
                    shortPrimaryOverflow = true;
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
            }
            prevPrimary = p;
            prevSecondary = Collation.COMMON_WEIGHT16;
            sec = CollationFastLatin.COMMON_SEC;
            ter = CollationFastLatin.COMMON_TER;
        }
        int lower32 = (int)ce;
        int s = lower32 >>> 16;
        if(s != prevSecondary) {
            if(pri == 0) {
                if(sec == 0) {
                    sec = CollationFastLatin.MIN_SEC_HIGH;
                } else if(sec < CollationFastLatin.MAX_SEC_HIGH) {
                    sec += CollationFastLatin.SEC_INC;
                } else {
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
                prevSecondary = s;
                ter = CollationFastLatin.COMMON_TER;
            } else if(s < Collation.COMMON_WEIGHT16) {
                if(sec == CollationFastLatin.COMMON_SEC) {
                    sec = CollationFastLatin.MIN_SEC_BEFORE;
                } else if(sec < CollationFastLatin.MAX_SEC_BEFORE) {
                    sec += CollationFastLatin.SEC_INC;
                } else {
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
            } else if(s == Collation.COMMON_WEIGHT16) {
                sec = CollationFastLatin.COMMON_SEC;
            } else {
                if(sec < CollationFastLatin.MIN_SEC_AFTER) {
                    sec = CollationFastLatin.MIN_SEC_AFTER;
                } else if(sec < CollationFastLatin.MAX_SEC_AFTER) {
                    sec += CollationFastLatin.SEC_INC;
                } else {
                    miniCEs[i] = CollationFastLatin.BAIL_OUT;
                    continue;
                }
            }
            prevSecondary = s;
            ter = CollationFastLatin.COMMON_TER;
        }
        assert((lower32 & Collation.CASE_MASK) == 0);  // blanked out in uniqueCEs
        int t = lower32 & Collation.ONLY_TERTIARY_MASK;
        if(t > Collation.COMMON_WEIGHT16) {
            if(ter < CollationFastLatin.MAX_TER_AFTER) {
                ++ter;
            } else {
                miniCEs[i] = CollationFastLatin.BAIL_OUT;
                continue;
            }
        }
        if(CollationFastLatin.MIN_LONG <= pri && pri <= CollationFastLatin.MAX_LONG) {
            assert(sec == CollationFastLatin.COMMON_SEC);
            miniCEs[i] = (char)(pri | ter);
        } else {
            miniCEs[i] = (char)(pri | sec | ter);
        }
    }
#if DEBUG_COLLATION_FAST_LATIN_BUILDER
    printf("last mini primary: %04x\n", pri);
#endif
#if DEBUG_COLLATION_FAST_LATIN_BUILDER >= 2
    for(int i = 0; i < uniqueCEs.size(); ++i) {
        long ce = uniqueCEs.elementAti(i);
        printf("unique CE 0x%016lx . 0x%04x\n", ce, miniCEs[i]);
    }
#endif
    return U_SUCCESS;
}

boolean
CollationFastLatinBuilder.encodeCharCEs() {
    if(U_FAILURE) { return false; }
    int miniCEsStart = result.length();
    for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
        result.append(0);  // initialize to completely ignorable
    }
    int indexBase = result.length();
    for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
        long ce = charCEs[i][0];
        if(isContractionCharCE(ce)) { continue; }  // defer contraction
        int miniCE = encodeTwoCEs(ce, charCEs[i][1]);
        if(miniCE > 0xffff) {
            // Note: There is a chance that this new expansion is the same as a previous one,
            // and if so, then we could reuse the other expansion.
            // However, that seems unlikely.
            int expansionIndex = result.length() - indexBase;
            if(expansionIndex > (int32_t)CollationFastLatin.INDEX_MASK) {
                miniCE = CollationFastLatin.BAIL_OUT;
            } else {
                result.append((char)(miniCE >> 16)).append((char)miniCE);
                miniCE = CollationFastLatin.EXPANSION | expansionIndex;
            }
        }
        result.setCharAt(miniCEsStart + i, (UChar)miniCE);
    }
    return U_SUCCESS;
}

boolean
CollationFastLatinBuilder.encodeContractions() {
    // We encode all contraction lists so that the first word of a list
    // terminates the previous list, and we only need one additional terminator at the end.
    if(U_FAILURE) { return false; }
    int indexBase = headerLength + CollationFastLatin.NUM_FAST_CHARS;
    int firstContractionIndex = result.length();
    for(int i = 0; i < CollationFastLatin.NUM_FAST_CHARS; ++i) {
        long ce = charCEs[i][0];
        if(!isContractionCharCE(ce)) { continue; }
        int contractionIndex = result.length() - indexBase;
        if(contractionIndex > (int32_t)CollationFastLatin.INDEX_MASK) {
            result.setCharAt(headerLength + i, CollationFastLatin.BAIL_OUT);
            continue;
        }
        boolean firstTriple = true;
        for(int index = (int32_t)ce & 0x7fffffff;; index += 3) {
            long x = contractionCEs.elementAti(index);
            if(x == CollationFastLatin.CONTR_CHAR_MASK && !firstTriple) { break; }
            long cce0 = contractionCEs.elementAti(index + 1);
            long cce1 = contractionCEs.elementAti(index + 2);
            int miniCE = encodeTwoCEs(cce0, cce1);
            if(miniCE == CollationFastLatin.BAIL_OUT) {
                result.append((UChar)(x | (1 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
            } else if(miniCE <= 0xffff) {
                result.append((UChar)(x | (2 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
                result.append((UChar)miniCE);
            } else {
                result.append((UChar)(x | (3 << CollationFastLatin.CONTR_LENGTH_SHIFT)));
                result.append((char)(miniCE >> 16)).append((char)miniCE);
            }
            firstTriple = false;
        }
        // Note: There is a chance that this new contraction list is the same as a previous one,
        // and if so, then we could truncate the result and reuse the other list.
        // However, that seems unlikely.
        result.setCharAt(headerLength + i,
                         (UChar)(CollationFastLatin.CONTRACTION | contractionIndex));
    }
    if(result.length() > firstContractionIndex) {
        // Terminate the last contraction list.
        result.append((UChar)CollationFastLatin.CONTR_CHAR_MASK);
    }
    if(result.isBogus()) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return false;
    }
#if DEBUG_COLLATION_FAST_LATIN_BUILDER
    printf("** fast Latin %d * 2 = %d bytes\n", result.length(), result.length() * 2);
    puts("   header & below-digit groups map");
    int i = 0;
    for(; i < headerLength; ++i) {
        printf(" %04x", result[i]);
    }
    printf("\n   char mini CEs");
    assert(CollationFastLatin.NUM_FAST_CHARS % 16 == 0);
    for(; i < indexBase; i += 16) {
        int c = i - headerLength;
        if(c >= CollationFastLatin.LATIN_LIMIT) {
            c = CollationFastLatin.PUNCT_START + c - CollationFastLatin.LATIN_LIMIT;
        }
        printf("\n %04x:", c);
        for(int j = 0; j < 16; ++j) {
            printf(" %04x", result[i + j]);
        }
    }
    printf("\n   expansions & contractions");
    for(; i < result.length(); ++i) {
        if((i - indexBase) % 16 == 0) { puts(""); }
        printf(" %04x", result[i]);
    }
    puts("");
#endif
    return true;
}

int
CollationFastLatinBuilder.encodeTwoCEs(long first, long second) {
    if(first == 0) {
        return 0;  // completely ignorable
    }
    if(first == Collation.NO_CE) {
        return CollationFastLatin.BAIL_OUT;
    }
    assert((first >>> 32) != Collation.NO_CE_PRIMARY);

    int miniCE = getMiniCE(first);
    if(miniCE == CollationFastLatin.BAIL_OUT) { return miniCE; }
    if(miniCE >= CollationFastLatin.MIN_SHORT) {
        // Extract & copy the case bits.
        // Shift them from normal CE bits 15..14 to mini CE bits 4..3.
        int c = (((int)first & Collation.CASE_MASK) >> (14 - 3));
        // Only in mini CEs: Ignorable case bits = 0, lowercase = 1.
        c += CollationFastLatin.LOWER_CASE;
        miniCE |= c;
    }
    if(second == 0) { return miniCE; }

    int miniCE1 = getMiniCE(second);
    if(miniCE1 == CollationFastLatin.BAIL_OUT) { return miniCE1; }

    int case1 = (int)second & Collation.CASE_MASK;
    if(miniCE >= CollationFastLatin.MIN_SHORT &&
            (miniCE & CollationFastLatin.SECONDARY_MASK) == CollationFastLatin.COMMON_SEC) {
        // Try to combine the two mini CEs into one.
        int sec1 = miniCE1 & CollationFastLatin.SECONDARY_MASK;
        int ter1 = miniCE1 & CollationFastLatin.TERTIARY_MASK;
        if(sec1 >= CollationFastLatin.MIN_SEC_HIGH && case1 == 0 &&
                ter1 == CollationFastLatin.COMMON_TER) {
            // sec1>=sec_high implies pri1==0.
            return (miniCE & ~CollationFastLatin.SECONDARY_MASK) | sec1;
        }
    }

    if(miniCE1 <= CollationFastLatin.SECONDARY_MASK || CollationFastLatin.MIN_SHORT <= miniCE1) {
        // Secondary CE, or a CE with a short primary, copy the case bits.
        case1 = (case1 >> (14 - 3)) + CollationFastLatin.LOWER_CASE;
        miniCE1 |= case1;
    }
    return (miniCE << 16) | miniCE1;
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
