/*
*******************************************************************************
* Copyright (C) 2012-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatabuilder.h
*
* @since 2012apr01
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

#include "unicode/uniset.h"
#include "unicode/unistr.h"
#include "unicode/uversion.h"
#include "collation.h"
#include "collationdata.h"
#include "collationsettings.h"
#include "normalizer2impl.h"
#include "utrie2.h"
#include "uvectr32.h"
#include "uvectr64.h"
#include "uvector.h"

U_NAMESPACE_BEGIN

struct ConditionalCE32;

class CollationFastLatinBuilder;
class CopyHelper;
class DataBuilderCollationIterator;
class UCharsTrieBuilder;

/**
 * Low-level CollationData builder.
 * Takes (character, CE) pairs and builds them into runtime data structures.
 * Supports characters with context prefixes and contraction suffixes.
 */
class CollationDataBuilder {
public:
    /**
     * Collation element modifier. Interface class for a modifier
     * that changes a tailoring builder's temporary CEs to final CEs.
     * Called for every non-special CE32 and every expansion CE.
     */
    interface CEModifier {
        /** Returns a new CE to replace the non-special input CE32, or else Collation.NO_CE. */
        long modifyCE32(int ce32);
        /** Returns a new CE to replace the input CE, or else Collation.NO_CE. */
        long modifyCE(long ce);
    };

    CollationDataBuilder();

    virtual ~CollationDataBuilder();

    void initForTailoring(CollationData b);

    virtual boolean isCompressibleLeadByte(int b);

    boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(p >> 24);
    }

    /**
     * @return true if this builder has mappings (e.g., add() has been called)
     */
    boolean hasMappings() { return modified; }

    /**
     * @return true if c has CEs in this builder
     */
    boolean isAssigned(int c);

    /**
     * @return the three-byte primary if c maps to a single such CE and has no context data,
     * otherwise returns 0.
     */
    long getLongPrimaryIfSingleCE(int c);

    /**
     * @return the single CE for c.
     * Sets an error code if c does not have a single CE.
     */
    long getSingleCE(int c);

    void add(const UnicodeString &prefix, const UnicodeString &s,
             const long ces[], int cesLength,
             );

    /**
     * Encodes the ces as either the returned ce32 by itself,
     * or by storing an expansion, with the returned ce32 referring to that.
     *
     * add(p, s, ces, cesLength) = addCE32(p, s, encodeCEs(ces, cesLength))
     */
    virtual uint32_t encodeCEs(const long ces[], int cesLength);
    void addCE32(const UnicodeString &prefix, const UnicodeString &s,
                 int ce32);

    /**
     * Sets three-byte-primary CEs for a range of code points in code point order,
     * if it is worth doing; otherwise no change is made.
     * None of the code points in the range should have complex mappings so far
     * (expansions/contractions/prefixes).
     * @param start first code point
     * @param end last code point (inclusive)
     * @param primary primary weight for 'start'
     * @param step per-code point primary-weight increment
     * @param errorCode ICU in/out error code
     * @return true if an OFFSET_TAG range was used for start..end
     */
    boolean maybeSetPrimaryRange(int start, int end,
                               long primary, int step,
                               );

    /**
     * Sets three-byte-primary CEs for a range of code points in code point order.
     * Sets range values if that is worth doing, or else individual values.
     * None of the code points in the range should have complex mappings so far
     * (expansions/contractions/prefixes).
     * @param start first code point
     * @param end last code point (inclusive)
     * @param primary primary weight for 'start'
     * @param step per-code point primary-weight increment
     * @param errorCode ICU in/out error code
     * @return the next primary after 'end': start primary incremented by ((end-start)+1)*step
     */
    uint32_t setPrimaryRangeAndReturnNext(int start, int end,
                                          long primary, int step,
                                          );

    /**
     * Copies all mappings from the src builder, with modifications.
     * This builder here must not be built yet, and should be empty.
     */
    void copyFrom(const CollationDataBuilder &src, const CEModifier &modifier,
                  );

    void optimize(const UnicodeSet &set);
    void suppressContractions(const UnicodeSet &set);

    void enableFastLatin() { fastLatinEnabled = true; }
    virtual void build(CollationData &data);

    /**
     * Looks up CEs for s and appends them to the ces array.
     * Does not handle normalization: s should be in FCD form.
     *
     * Does not write completely ignorable CEs.
     * Does not write beyond Collation.MAX_EXPANSION_LENGTH.
     *
     * @return incremented cesLength
     */
    int getCEs(const UnicodeString &s, long ces[], int cesLength);
    int getCEs(const UnicodeString &prefix, const UnicodeString &s,
                   long ces[], int cesLength);

protected:
    friend class CopyHelper;
    friend class DataBuilderCollationIterator;

    uint32_t getCE32FromOffsetCE32(boolean fromBase, int c, int ce32);

    int addCE(long ce);
    int addCE32(int ce32);
    int addConditionalCE32(const UnicodeString &context, int ce32);

    ConditionalCE32 *getConditionalCE32(int index) {
        return static_cast<ConditionalCE32 *>(conditionalCE32s[index]);
    }
    ConditionalCE32 *getConditionalCE32ForCE32(int ce32) {
        return getConditionalCE32(Collation.indexFromCE32(ce32));
    }

    static uint32_t makeBuilderContextCE32(int index) {
        return Collation.makeCE32FromTagAndIndex(Collation.BUILDER_DATA_TAG, index);
    }
    static boolean isBuilderContextCE32(int ce32) {
        return Collation.hasCE32Tag(ce32, Collation.BUILDER_DATA_TAG);
    }

    static uint32_t encodeOneCEAsCE32(long ce);
    uint32_t encodeOneCE(long ce);
    uint32_t encodeExpansion(const long ces[], int length);
    uint32_t encodeExpansion32(const int newCE32s[], int length);

    uint32_t copyFromBaseCE32(int c, int ce32, boolean withContext);
    /**
     * Copies base contractions to a list of ConditionalCE32.
     * Sets cond.next to the index of the first new item
     * and returns the index of the last new item.
     */
    int copyContractionsFromBaseCE32(UnicodeString &context, int c, int ce32,
                                         ConditionalCE32 *cond);

    boolean getJamoCE32s(uint32_t jamoCE32s[]);
    void setDigitTags();
    void setLeadSurrogates();

    void buildMappings(CollationData &data);

    void clearContexts();
    void buildContexts();
    uint32_t buildContext(ConditionalCE32 *head);
    int addContextTrie(uint32_t defaultCE32, UCharsTrieBuilder &trieBuilder,
                           );

    void buildFastLatinTable(CollationData &data);

    int getCEs(const UnicodeString &s, int start, long ces[], int cesLength);

    static int jamoCpFromIndex(int i) {
        // 0 <= i < CollationData.JAMO_CE32S_LENGTH = 19 + 21 + 27
        if(i < Hangul.JAMO_L_COUNT) { return Hangul.JAMO_L_BASE + i; }
        i -= Hangul.JAMO_L_COUNT;
        if(i < Hangul.JAMO_V_COUNT) { return Hangul.JAMO_V_BASE + i; }
        i -= Hangul.JAMO_V_COUNT;
        // i < 27
        return Hangul.JAMO_T_BASE + 1 + i;
    }

    /** @see Collation.BUILDER_DATA_TAG */
    private static final uint32_t IS_BUILDER_JAMO_CE32 = 0x100;

    const Normalizer2Impl &nfcImpl;
    CollationData base;
    CollationSettings baseSettings;
    Trie2Writable trie;
    UVector32 ce32s;
    UVector64 ce64s;
    UVector conditionalCE32s;  // vector of ConditionalCE32
    // Characters that have context (prefixes or contraction suffixes).
    UnicodeSet contextChars;
    // Serialized UCharsTrie structures for finalized contexts.
    UnicodeString contexts;
    UnicodeSet unsafeBackwardSet;
    boolean modified;

    boolean fastLatinEnabled;
    CollationFastLatinBuilder *fastLatinBuilder;

    DataBuilderCollationIterator *collIter;
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONDATABUILDER_H__
/*
*******************************************************************************
* Copyright (C) 2012-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatabuilder.cpp
*
* (replaced the former ucol_elm.cpp)
*
* @since 2012apr01
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/localpointer.h"
#include "unicode/uchar.h"
#include "unicode/ucharstrie.h"
#include "unicode/ucharstriebuilder.h"
#include "unicode/uniset.h"
#include "unicode/unistr.h"
#include "unicode/usetiter.h"
#include "unicode/utf16.h"
#include "cmemory.h"
#include "collation.h"
#include "collationdata.h"
#include "collationdatabuilder.h"
#include "collationfastlatinbuilder.h"
#include "collationiterator.h"
#include "normalizer2impl.h"
#include "utrie2.h"
#include "uvectr32.h"
#include "uvectr64.h"
#include "uvector.h"

CollationDataBuilder.CEModifier.~CEModifier() {}

/**
 * Build-time context and CE32 for a code point.
 * If a code point has contextual mappings, then the default (no-context) mapping
 * and all conditional mappings are stored in a singly-linked list
 * of ConditionalCE32, sorted by context strings.
 *
 * Context strings sort by prefix length, then by prefix, then by contraction suffix.
 * Context strings must be unique and in ascending order.
 */
struct ConditionalCE32 {
    ConditionalCE32(const UnicodeString &ct, uint32_t ce)
            : context(ct),
              ce32(ce), defaultCE32(Collation.NO_CE32), builtCE32(Collation.NO_CE32),
              next(-1) {}

    boolean hasContext() { return context.length() > 1; }
    int prefixLength() { return context.charAt(0); }

    boolean hasSamePrefixAs(const ConditionalCE32 &other) {
        int length = prefixLength();
        return length == other.prefixLength() &&
            (length == 0 || context.compare(1, length, other.context, 1, length) == 0);
    }

    boolean prefixMatchesBefore(const UnicodeString &s, int i) {
        int length = prefixLength();
        return length <= i &&
            (length == 0 || s.compare(i - length, length, context, 1, length) == 0);
    }

    /**
     * "\0" for the first entry for any code point, with its default CE32.
     *
     * Otherwise one unit with the length of the prefix string,
     * then the prefix string, then the contraction suffix.
     */
    UnicodeString context;
    /**
     * CE32 for the code point and its context.
     * Can be special (e.g., for an expansion) but not contextual (prefix or contraction tag).
     */
    int ce32;
    /**
     * Default CE32 for all contexts with this same prefix.
     * Initially NO_CE32. Set only while building runtime data structures,
     * and only on one of the nodes of a sub-list with the same prefix.
     */
    uint32_t defaultCE32;
    /**
     * CE32 for the built contexts.
     * When fetching CEs from the builder, the contexts are built into their runtime form
     * so that the normal collation implementation can process them.
     * The result is cached in the list head. It is reset when the contexts are modified.
     */
    uint32_t builtCE32;
    /**
     * Index of the next ConditionalCE32.
     * Negative for the end of the list.
     */
    int next;
};

U_CDECL_BEGIN

U_CAPI void U_CALLCONV
uprv_deleteConditionalCE32(void *obj) {
    delete static_cast<ConditionalCE32 *>(obj);
}

U_CDECL_END

/**
 * Build-time collation element and character iterator.
 * Uses the runtime CollationIterator for fetching CEs for a string
 * but reads from the builder's unfinished data structures.
 * In particular, this class reads from the unfinished trie
 * and has to avoid CollationIterator.nextCE() and redirect other
 * calls to data.getCE32() and data.getCE32FromSupplementary().
 *
 * We do this so that we need not implement the collation algorithm
 * again for the builder and make it behave exactly like the runtime code.
 * That would be more difficult to test and maintain than this indirection.
 *
 * Some CE32 tags (for example, the DIGIT_TAG) do not occur in the builder data,
 * so the data accesses from those code paths need not be modified.
 *
 * This class iterates directly over whole code points
 * so that the CollationIterator does not need the finished trie
 * for handling the LEAD_SURROGATE_TAG.
 */
class DataBuilderCollationIterator extends CollationIterator {
public:
    DataBuilderCollationIterator(CollationDataBuilder &b);

    virtual ~DataBuilderCollationIterator();

    int fetchCEs(const UnicodeString &str, int start, long ces[], int cesLength);

    virtual void resetToOffset(int newOffset);
    virtual int getOffset();

    virtual int nextCodePoint();
    virtual int previousCodePoint();

protected:
    virtual void forwardNumCodePoints(int num);
    virtual void backwardNumCodePoints(int num);

    virtual uint32_t getDataCE32(int c);
    virtual uint32_t getCE32FromBuilderData(int ce32);

    CollationDataBuilder &builder;
    CollationData builderData;
    uint32_t jamoCE32s[CollationData.JAMO_CE32S_LENGTH];
    const UnicodeString *s;
    int pos;
};

DataBuilderCollationIterator.DataBuilderCollationIterator(CollationDataBuilder &b)
        : CollationIterator(&builderData, /*numeric=*/ false),
          builder(b), builderData(b.nfcImpl),
          s(null), pos(0) {
    builderData.base = builder.base;
    // Set all of the jamoCE32s[] to indirection CE32s.
    for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {  // Count across Jamo types.
        int jamo = CollationDataBuilder.jamoCpFromIndex(j);
        jamoCE32s[j] = Collation.makeCE32FromTagAndIndex(Collation.BUILDER_DATA_TAG, jamo) |
                CollationDataBuilder.IS_BUILDER_JAMO_CE32;
    }
    builderData.jamoCE32s = jamoCE32s;
}

DataBuilderCollationIterator.~DataBuilderCollationIterator() {}

int32_t
DataBuilderCollationIterator.fetchCEs(const UnicodeString &str, int start,
                                       long ces[], int cesLength) {
    // Set the pointers each time, in case they changed due to reallocation.
    builderData.ce32s = reinterpret_cast<const uint32_t *>(builder.ce32s.getBuffer());
    builderData.ces = builder.ce64s.getBuffer();
    builderData.contexts = builder.contexts.getBuffer();
    // Modified copy of CollationIterator.nextCE() and CollationIterator.nextCEFromCE32().
    reset();
    s = &str;
    pos = start;
    UErrorCode errorCode = U_ZERO_ERROR;
    while(pos < s.length()) {
        // No need to keep all CEs in the iterator buffer.
        clearCEs();
        int c = s.char32At(pos);
        pos += Character.charCount(c);
        int ce32 = utrie2_get32(builder.trie, c);
        CollationData d;
        if(ce32 == Collation.FALLBACK_CE32) {
            d = builder.base;
            ce32 = builder.base.getCE32(c);
        } else {
            d = &builderData;
        }
        appendCEsFromCE32(d, c, ce32, /*forward=*/ true);
        assert(U_SUCCESS);
        for(int i = 0; i < getCEsLength(); ++i) {
            long ce = getCE(i);
            if(ce != 0) {
                if(cesLength < Collation.MAX_EXPANSION_LENGTH) {
                    ces[cesLength] = ce;
                }
                ++cesLength;
            }
        }
    }
    return cesLength;
}

void
DataBuilderCollationIterator.resetToOffset(int newOffset) {
    reset();
    pos = newOffset;
}

int32_t
DataBuilderCollationIterator.getOffset() {
    return pos;
}

int
DataBuilderCollationIterator.nextCodePoint(UErrorCode & /*errorCode*/) {
    if(pos == s.length()) {
        return Collation.SENTINEL_CP;
    }
    int c = s.char32At(pos);
    pos += Character.charCount(c);
    return c;
}

int
DataBuilderCollationIterator.previousCodePoint(UErrorCode & /*errorCode*/) {
    if(pos == 0) {
        return Collation.SENTINEL_CP;
    }
    int c = s.char32At(pos - 1);
    pos -= Character.charCount(c);
    return c;
}

void
DataBuilderCollationIterator.forwardNumCodePoints(int num, UErrorCode & /*errorCode*/) {
    pos = s.offsetByCodePoints(pos, num);
}

void
DataBuilderCollationIterator.backwardNumCodePoints(int num, UErrorCode & /*errorCode*/) {
    pos = s.offsetByCodePoints(pos, -num);
}

uint32_t
DataBuilderCollationIterator.getDataCE32(int c) {
    return utrie2_get32(builder.trie, c);
}

uint32_t
DataBuilderCollationIterator.getCE32FromBuilderData(int ce32) {
    assert(Collation.hasCE32Tag(ce32, Collation.BUILDER_DATA_TAG));
    if((ce32 & CollationDataBuilder.IS_BUILDER_JAMO_CE32) != 0) {
        int jamo = Collation.indexFromCE32(ce32);
        return utrie2_get32(builder.trie, jamo);
    } else {
        ConditionalCE32 *cond = builder.getConditionalCE32ForCE32(ce32);
        if(cond.builtCE32 == Collation.NO_CE32) {
            // Build the context-sensitive mappings into their runtime form and cache the result.
            cond.builtCE32 = builder.buildContext(cond);
            if(errorCode == U_BUFFER_OVERFLOW_ERROR) {
                errorCode = U_ZERO_ERROR;
                builder.clearContexts();
                cond.builtCE32 = builder.buildContext(cond);
            }
            builderData.contexts = builder.contexts.getBuffer();
        }
        return cond.builtCE32;
    }
}

// ------------------------------------------------------------------------- ***

CollationDataBuilder.CollationDataBuilder()
        : nfcImpl(*Normalizer2Factory.getNFCImpl),
          base(null), baseSettings(null),
          trie(null),
          ce32s, ce64s, conditionalCE32s,
          modified(false),
          fastLatinEnabled(false), fastLatinBuilder(null),
          collIter(null) {
    // Reserve the first CE32 for U+0000.
    ce32s.addElement(0);
    conditionalCE32s.setDeleter(uprv_deleteConditionalCE32);
}

CollationDataBuilder.~CollationDataBuilder() {
    utrie2_close(trie);
    delete fastLatinBuilder;
    delete collIter;
}

void
CollationDataBuilder.initForTailoring(CollationData b) {
    if(U_FAILURE) { return; }
    if(trie != null) {
        errorCode = U_INVALID_STATE_ERROR;
        return;
    }
    if(b == null) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return;
    }
    base = b;

    // For a tailoring, the default is to fall back to the base.
    trie = utrie2_open(Collation.FALLBACK_CE32, Collation.FFFD_CE32, &errorCode);

    // Set the Latin-1 letters block so that it is allocated first in the data array,
    // to try to improve locality of reference when sorting Latin-1 text.
    // Do not use utrie2_setRange32() since that will not actually allocate blocks
    // that are filled with the default value.
    // ASCII (0..7F) is already preallocated anyway.
    for(int c = 0xc0; c <= 0xff; ++c) {
        utrie2_set32(trie, c, Collation.FALLBACK_CE32, &errorCode);
    }

    // Hangul syllables are not tailorable (except via tailoring Jamos).
    // Always set the Hangul tag to help performance.
    // Do this here, rather than in buildMappings(),
    // so that we see the HANGUL_TAG in various assertions.
    uint32_t hangulCE32 = Collation.makeCE32FromTagAndIndex(Collation.HANGUL_TAG, 0);
    utrie2_setRange32(trie, Hangul.HANGUL_BASE, Hangul.HANGUL_END, hangulCE32, true, &errorCode);

    // Copy the set contents but don't copy/clone the set as a whole because
    // that would copy the isFrozen state too.
    unsafeBackwardSet.addAll(*b.unsafeBackwardSet);

    if(U_FAILURE) { return; }
}

boolean
CollationDataBuilder.maybeSetPrimaryRange(int start, int end,
                                           long primary, int step,
                                           ) {
    if(U_FAILURE) { return false; }
    assert(start <= end);
    // TODO: Do we need to check what values are currently set for start..end?
    // An offset range is worth it only if we can achieve an overlap between
    // adjacent UTrie2 blocks of 32 code points each.
    // An offset CE is also a little more expensive to look up and compute
    // than a simple CE.
    // If the range spans at least three UTrie2 block boundaries (> 64 code points),
    // then we take it.
    // If the range spans one or two block boundaries and there are
    // at least 4 code points on either side, then we take it.
    // (We could additionally require a minimum range length of, say, 16.)
    int blockDelta = (end >> 5) - (start >> 5);
    if(2 <= step && step <= 0x7f &&
            (blockDelta >= 3 ||
            (blockDelta > 0 && (start & 0x1f) <= 0x1c && (end & 0x1f) >= 3))) {
        long dataCE = (primary << 32) | (start << 8) | step;
        if(isCompressiblePrimary(primary)) { dataCE |= 0x80; }
        int index = addCE(dataCE);
        if(U_FAILURE) { return 0; }
        if(index > Collation.MAX_INDEX) {
            errorCode = U_BUFFER_OVERFLOW_ERROR;
            return 0;
        }
        uint32_t offsetCE32 = Collation.makeCE32FromTagAndIndex(Collation.OFFSET_TAG, index);
        utrie2_setRange32(trie, start, end, offsetCE32, true, &errorCode);
        modified = true;
        return true;
    } else {
        return false;
    }
}

uint32_t
CollationDataBuilder.setPrimaryRangeAndReturnNext(int start, int end,
                                                   long primary, int step,
                                                   ) {
    if(U_FAILURE) { return 0; }
    boolean isCompressible = isCompressiblePrimary(primary);
    if(maybeSetPrimaryRange(start, end, primary, step)) {
        return Collation.incThreeBytePrimaryByOffset(primary, isCompressible,
                                                      (end - start + 1) * step);
    } else {
        // Short range: Set individual CE32s.
        for(;;) {
            utrie2_set32(trie, start, Collation.makeLongPrimaryCE32(primary), &errorCode);
            ++start;
            primary = Collation.incThreeBytePrimaryByOffset(primary, isCompressible, step);
            if(start > end) { return primary; }
        }
        modified = true;
    }
}

uint32_t
CollationDataBuilder.getCE32FromOffsetCE32(boolean fromBase, int c, int ce32) {
    int i = Collation.indexFromCE32(ce32);
    long dataCE = fromBase ? base.ces[i] : ce64s.elementAti(i);
    long p = Collation.getThreeBytePrimaryForOffsetData(c, dataCE);
    return Collation.makeLongPrimaryCE32(p);
}

boolean
CollationDataBuilder.isCompressibleLeadByte(uint32_t b) {
    return base.isCompressibleLeadByte(b);
}

boolean
CollationDataBuilder.isAssigned(int c) {
    return Collation.isAssignedCE32(utrie2_get32(trie, c));
}

uint32_t
CollationDataBuilder.getLongPrimaryIfSingleCE(int c) {
    int ce32 = utrie2_get32(trie, c);
    if(Collation.isLongPrimaryCE32(ce32)) {
        return Collation.primaryFromLongPrimaryCE32(ce32);
    } else {
        return 0;
    }
}

long
CollationDataBuilder.getSingleCE(int c) {
    if(U_FAILURE) { return 0; }
    boolean fromBase = false;
    int ce32 = utrie2_get32(trie, c);
    if(ce32 == Collation.FALLBACK_CE32) {
        fromBase = true;
        ce32 = base.getCE32(c);
    }
    while(Collation.isSpecialCE32(ce32)) {
        switch(Collation.tagFromCE32(ce32)) {
        case Collation.LATIN_EXPANSION_TAG:
        case Collation.BUILDER_DATA_TAG:
        case Collation.PREFIX_TAG:
        case Collation.CONTRACTION_TAG:
        case Collation.HANGUL_TAG:
        case Collation.LEAD_SURROGATE_TAG:
            errorCode = U_UNSUPPORTED_ERROR;
            return 0;
        case Collation.FALLBACK_TAG:
        case Collation.RESERVED_TAG_3:
            errorCode = U_INTERNAL_PROGRAM_ERROR;
            return 0;
        case Collation.LONG_PRIMARY_TAG:
            return Collation.ceFromLongPrimaryCE32(ce32);
        case Collation.LONG_SECONDARY_TAG:
            return Collation.ceFromLongSecondaryCE32(ce32);
        case Collation.EXPANSION32_TAG:
            if(Collation.lengthFromCE32(ce32) == 1) {
                int i = Collation.indexFromCE32(ce32);
                ce32 = fromBase ? base.ce32s[i] : ce32s.elementAti(i);
                break;
            } else {
                errorCode = U_UNSUPPORTED_ERROR;
                return 0;
            }
        case Collation.EXPANSION_TAG: {
            if(Collation.lengthFromCE32(ce32) == 1) {
                int i = Collation.indexFromCE32(ce32);
                return fromBase ? base.ces[i] : ce64s.elementAti(i);
            } else {
                errorCode = U_UNSUPPORTED_ERROR;
                return 0;
            }
        }
        case Collation.DIGIT_TAG:
            // Fetch the non-numeric-collation CE32 and continue.
            ce32 = ce32s.elementAti(Collation.indexFromCE32(ce32));
            break;
        case Collation.U0000_TAG:
            assert(c == 0);
            // Fetch the normal ce32 for U+0000 and continue.
            ce32 = fromBase ? base.ce32s[0] : ce32s.elementAti(0);
            break;
        case Collation.OFFSET_TAG:
            ce32 = getCE32FromOffsetCE32(fromBase, c, ce32);
            break;
        case Collation.IMPLICIT_TAG:
            return Collation.unassignedCEFromCodePoint(c);
        }
    }
    return Collation.ceFromSimpleCE32(ce32);
}

int32_t
CollationDataBuilder.addCE(long ce) {
    int length = ce64s.size();
    for(int i = 0; i < length; ++i) {
        if(ce == ce64s.elementAti(i)) { return i; }
    }
    ce64s.addElement(ce);
    return length;
}

int32_t
CollationDataBuilder.addCE32(int ce32) {
    int length = ce32s.size();
    for(int i = 0; i < length; ++i) {
        if(ce32 == (uint32_t)ce32s.elementAti(i)) { return i; }
    }
    ce32s.addElement((int32_t)ce32);  
    return length;
}

int32_t
CollationDataBuilder.addConditionalCE32(const UnicodeString &context, int ce32,
                                         ) {
    if(U_FAILURE) { return -1; }
    assert(!context.isEmpty());
    int index = conditionalCE32s.size();
    if(index > Collation.MAX_INDEX) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return -1;
    }
    ConditionalCE32 *cond = new ConditionalCE32(context, ce32);
    if(cond == null) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return -1;
    }
    conditionalCE32s.addElement(cond);
    return index;
}

void
CollationDataBuilder.add(const UnicodeString &prefix, const UnicodeString &s,
                          const long ces[], int cesLength,
                          ) {
    int ce32 = encodeCEs(ces, cesLength);
    addCE32(prefix, s, ce32);
}

void
CollationDataBuilder.addCE32(const UnicodeString &prefix, const UnicodeString &s,
                              int ce32) {
    if(U_FAILURE) { return; }
    if(s.isEmpty()) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return;
    }
    if(trie == null || utrie2_isFrozen(trie)) {
        errorCode = U_INVALID_STATE_ERROR;
        return;
    }
    int c = s.char32At(0);
    int cLength = Character.charCount(c);
    uint32_t oldCE32 = utrie2_get32(trie, c);
    boolean hasContext = !prefix.isEmpty() || s.length() > cLength;
    if(oldCE32 == Collation.FALLBACK_CE32) {
        // First tailoring for c.
        // If c has contextual base mappings or if we add a contextual mapping,
        // then copy the base mappings.
        // Otherwise we just override the base mapping.
        uint32_t baseCE32 = base.getFinalCE32(base.getCE32(c));
        if(hasContext || Collation.ce32HasContext(baseCE32)) {
            oldCE32 = copyFromBaseCE32(c, baseCE32, true);
            utrie2_set32(trie, c, oldCE32, &errorCode);
            if(U_FAILURE) { return; }
        }
    }
    if(!hasContext) {
        // No prefix, no contraction.
        if(!isBuilderContextCE32(oldCE32)) {
            utrie2_set32(trie, c, ce32, &errorCode);
        } else {
            ConditionalCE32 *cond = getConditionalCE32ForCE32(oldCE32);
            cond.builtCE32 = Collation.NO_CE32;
            cond.ce32 = ce32;
        }
    } else {
        ConditionalCE32 *cond;
        if(!isBuilderContextCE32(oldCE32)) {
            // Replace the simple oldCE32 with a builder context CE32
            // pointing to a new ConditionalCE32 list head.
            int index = addConditionalCE32(UnicodeString((UChar)0), oldCE32);
            if(U_FAILURE) { return; }
            uint32_t contextCE32 = makeBuilderContextCE32(index);
            utrie2_set32(trie, c, contextCE32, &errorCode);
            contextChars.add(c);
            cond = getConditionalCE32(index);
        } else {
            cond = getConditionalCE32ForCE32(oldCE32);
            cond.builtCE32 = Collation.NO_CE32;
        }
        UnicodeString suffix(s, cLength);
        UnicodeString context((UChar)prefix.length());
        context.append(prefix).append(suffix);
        unsafeBackwardSet.addAll(suffix);
        for(;;) {
            // invariant: context > cond.context
            int next = cond.next;
            if(next < 0) {
                // Append a new ConditionalCE32 after cond.
                int index = addConditionalCE32(context, ce32);
                if(U_FAILURE) { return; }
                cond.next = index;
                break;
            }
            ConditionalCE32 *nextCond = getConditionalCE32(next);
            int8_t cmp = context.compare(nextCond.context);
            if(cmp < 0) {
                // Insert a new ConditionalCE32 between cond and nextCond.
                int index = addConditionalCE32(context, ce32);
                if(U_FAILURE) { return; }
                cond.next = index;
                getConditionalCE32(index).next = next;
                break;
            } else if(cmp == 0) {
                // Same context as before, overwrite its ce32.
                nextCond.ce32 = ce32;
                break;
            }
            cond = nextCond;
        }
    }
    modified = true;
}

uint32_t
CollationDataBuilder.encodeOneCEAsCE32(long ce) {
    long p = ce >>> 32;
    int lower32 = (int)ce;
    int t = lower32 & 0xffff;
    assert((t & 0xc000) != 0xc000);  // Impossible case bits 11 mark special CE32s.
    if((ce & 0xffff00ff00ffL) == 0) {
        // normal form ppppsstt
        return p | (lower32 >>> 16) | (t >> 8);
    } else if((ce & 0xffffffffffL) == Collation.COMMON_SEC_AND_TER_CE) {
        // long-primary form ppppppC1
        return Collation.makeLongPrimaryCE32(p);
    } else if(p == 0 && (t & 0xff) == 0) {
        // long-secondary form ssssttC2
        return Collation.makeLongSecondaryCE32(lower32);
    }
    return Collation.NO_CE32;
}

uint32_t
CollationDataBuilder.encodeOneCE(long ce) {
    // Try to encode one CE as one CE32.
    int ce32 = encodeOneCEAsCE32(ce);
    if(ce32 != Collation.NO_CE32) { return ce32; }
    int index = addCE(ce);
    if(U_FAILURE) { return 0; }
    if(index > Collation.MAX_INDEX) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return 0;
    }
    return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION_TAG, index, 1);
}

uint32_t
CollationDataBuilder.encodeCEs(const long ces[], int cesLength,
                                ) {
    if(U_FAILURE) { return 0; }
    if(cesLength < 0 || cesLength > Collation.MAX_EXPANSION_LENGTH) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return 0;
    }
    if(trie == null || utrie2_isFrozen(trie)) {
        errorCode = U_INVALID_STATE_ERROR;
        return 0;
    }
    if(cesLength == 0) {
        // Convenience: We cannot map to nothing, but we can map to a completely ignorable CE.
        // Do this here so that callers need not do it.
        return encodeOneCEAsCE32(0);
    } else if(cesLength == 1) {
        return encodeOneCE(ces[0]);
    } else if(cesLength == 2) {
        // Try to encode two CEs as one CE32.
        long ce0 = ces[0];
        long ce1 = ces[1];
        long p0 = ce0 >>> 32;
        if((ce0 & 0xffffffffff00ffL) == Collation.COMMON_SECONDARY_CE &&
                (ce1 & 0xffffffff00ffffffL) == Collation.COMMON_TERTIARY_CE &&
                p0 != 0) {
            // Latin mini expansion
            return
                p0 |
                ((ce0 & 0xff00) << 8) |
                ((ce1 >> 16) & 0xff00) |
                Collation.SPECIAL_CE32_LOW_BYTE |
                Collation.LATIN_EXPANSION_TAG;
        }
    }
    // Try to encode two or more CEs as CE32s.
    int newCE32s[Collation.MAX_EXPANSION_LENGTH];
    for(int i = 0;; ++i) {
        if(i == cesLength) {
            return encodeExpansion32(newCE32s, cesLength);
        }
        int ce32 = encodeOneCEAsCE32(ces[i]);
        if(ce32 == Collation.NO_CE32) { break; }
        newCE32s[i] = (int32_t)ce32;
    }
    return encodeExpansion(ces, cesLength);
}

uint32_t
CollationDataBuilder.encodeExpansion(const long ces[], int length) {
    if(U_FAILURE) { return 0; }
    // See if this sequence of CEs has already been stored.
    long first = ces[0];
    int ce64sMax = ce64s.size() - length;
    for(int i = 0; i <= ce64sMax; ++i) {
        if(first == ce64s.elementAti(i)) {
            if(i > Collation.MAX_INDEX) {
                errorCode = U_BUFFER_OVERFLOW_ERROR;
                return 0;
            }
            for(int j = 1;; ++j) {
                if(j == length) {
                    return Collation.makeCE32FromTagIndexAndLength(
                            Collation.EXPANSION_TAG, i, length);
                }
                if(ce64s.elementAti(i + j) != ces[j]) { break; }
            }
        }
    }
    // Store the new sequence.
    int i = ce64s.size();
    if(i > Collation.MAX_INDEX) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return 0;
    }
    for(int j = 0; j < length; ++j) {
        ce64s.addElement(ces[j]);
    }
    return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION_TAG, i, length);
}

uint32_t
CollationDataBuilder.encodeExpansion32(const int newCE32s[], int length,
                                        ) {
    if(U_FAILURE) { return 0; }
    // See if this sequence of CE32s has already been stored.
    int first = newCE32s[0];
    int ce32sMax = ce32s.size() - length;
    for(int i = 0; i <= ce32sMax; ++i) {
        if(first == ce32s.elementAti(i)) {
            if(i > Collation.MAX_INDEX) {
                errorCode = U_BUFFER_OVERFLOW_ERROR;
                return 0;
            }
            for(int j = 1;; ++j) {
                if(j == length) {
                    return Collation.makeCE32FromTagIndexAndLength(
                            Collation.EXPANSION32_TAG, i, length);
                }
                if(ce32s.elementAti(i + j) != newCE32s[j]) { break; }
            }
        }
    }
    // Store the new sequence.
    int i = ce32s.size();
    if(i > Collation.MAX_INDEX) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return 0;
    }
    for(int j = 0; j < length; ++j) {
        ce32s.addElement(newCE32s[j]);
    }
    return Collation.makeCE32FromTagIndexAndLength(Collation.EXPANSION32_TAG, i, length);
}

uint32_t
CollationDataBuilder.copyFromBaseCE32(int c, int ce32, boolean withContext,
                                       ) {
    if(U_FAILURE) { return 0; }
    if(!Collation.isSpecialCE32(ce32)) { return ce32; }
    switch(Collation.tagFromCE32(ce32)) {
    case Collation.LONG_PRIMARY_TAG:
    case Collation.LONG_SECONDARY_TAG:
    case Collation.LATIN_EXPANSION_TAG:
        // copy as is
        break;
    case Collation.EXPANSION32_TAG: {
        const uint32_t *baseCE32s = base.ce32s + Collation.indexFromCE32(ce32);
        int length = Collation.lengthFromCE32(ce32);
        ce32 = encodeExpansion32(
            reinterpret_cast<const int *>(baseCE32s), length);
        break;
    }
    case Collation.EXPANSION_TAG: {
        const long *baseCEs = base.ces + Collation.indexFromCE32(ce32);
        int length = Collation.lengthFromCE32(ce32);
        ce32 = encodeExpansion(baseCEs, length);
        break;
    }
    case Collation.PREFIX_TAG: {
        // Flatten prefixes and nested suffixes (contractions)
        // into a linear list of ConditionalCE32.
        int trieIndex = Collation.indexFromCE32(ce32);
        ce32 = base.getCE32FromContexts(trieIndex);  // Default if no prefix match.
        if(!withContext) {
            return copyFromBaseCE32(c, ce32, false);
        }
        ConditionalCE32 head(UnicodeString(), 0);
        UnicodeString context((UChar)0);
        int index;
        if(Collation.isContractionCE32(ce32)) {
            index = copyContractionsFromBaseCE32(context, c, ce32, &head);
        } else {
            ce32 = copyFromBaseCE32(c, ce32, true);
            head.next = index = addConditionalCE32(context, ce32);
        }
        if(U_FAILURE) { return 0; }
        ConditionalCE32 *cond = getConditionalCE32(index);  // the last ConditionalCE32 so far
        UCharsTrie.Iterator prefixes(base.contexts, trieIndex + 2, 0);
        while(prefixes.next) {
            context = prefixes.getString();
            context.reverse();
            context.insert(0, (UChar)context.length());
            ce32 = (uint32_t)prefixes.getValue();
            if(Collation.isContractionCE32(ce32)) {
                index = copyContractionsFromBaseCE32(context, c, ce32, cond);
            } else {
                ce32 = copyFromBaseCE32(c, ce32, true);
                cond.next = index = addConditionalCE32(context, ce32);
            }
            if(U_FAILURE) { return 0; }
            cond = getConditionalCE32(index);
        }
        ce32 = makeBuilderContextCE32(head.next);
        contextChars.add(c);
        break;
    }
    case Collation.CONTRACTION_TAG: {
        if(!withContext) {
            int index = Collation.indexFromCE32(ce32);
            ce32 = base.getCE32FromContexts(index);  // Default if no suffix match.
            return copyFromBaseCE32(c, ce32, false);
        }
        ConditionalCE32 head(UnicodeString(), 0);
        UnicodeString context((UChar)0);
        copyContractionsFromBaseCE32(context, c, ce32, &head);
        ce32 = makeBuilderContextCE32(head.next);
        contextChars.add(c);
        break;
    }
    case Collation.HANGUL_TAG:
        errorCode = U_UNSUPPORTED_ERROR;  // We forbid tailoring of Hangul syllables.
        break;
    case Collation.OFFSET_TAG:
        ce32 = getCE32FromOffsetCE32(true, c, ce32);
        break;
    case Collation.IMPLICIT_TAG:
        ce32 = encodeOneCE(Collation.unassignedCEFromCodePoint(c));
        break;
    default:
        assert(false);  // require ce32 == base.getFinalCE32(ce32)
        break;
    }
    return ce32;
}

int32_t
CollationDataBuilder.copyContractionsFromBaseCE32(UnicodeString &context, int c, int ce32,
                                                   ConditionalCE32 *cond) {
    int trieIndex = Collation.indexFromCE32(ce32);
    int index;
    if((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
        // No match on the single code point.
        // We are underneath a prefix, and the default mapping is just
        // a fallback to the mappings for a shorter prefix.
        assert(context.length() > 1);
        index = -1;
    } else {
        ce32 = base.getCE32FromContexts(trieIndex);  // Default if no suffix match.
        assert(!Collation.isContractionCE32(ce32));
        ce32 = copyFromBaseCE32(c, ce32, true);
        cond.next = index = addConditionalCE32(context, ce32);
        cond = getConditionalCE32(index);
    }

    int suffixStart = context.length();
    UCharsTrie.Iterator suffixes(base.contexts, trieIndex + 2, 0);
    while(suffixes.next()) {
        context.append(suffixes.getString());
        ce32 = copyFromBaseCE32(c, (uint32_t)suffixes.getValue(), true);
        cond.next = index = addConditionalCE32(context, ce32);
        // No need to update the unsafeBackwardSet because the tailoring set
        // is already a copy of the base set.
        cond = getConditionalCE32(index);
        context.truncate(suffixStart);
    }
    assert(index >= 0);
    return index;
}

class CopyHelper {
public:
    CopyHelper(const CollationDataBuilder &s, CollationDataBuilder &d,
               const CollationDataBuilder.CEModifier &m, UErrorCode &initialErrorCode)
            : src(s), dest(d), modifier(m),
              errorCode(initialErrorCode) {}

    boolean copyRangeCE32(int start, int end, int ce32) {
        ce32 = copyCE32(ce32);
        utrie2_setRange32(dest.trie, start, end, ce32, true, &errorCode);
        if(CollationDataBuilder.isBuilderContextCE32(ce32)) {
            dest.contextChars.add(start, end);
        }
        return U_SUCCESS;
    }

    uint32_t copyCE32(int ce32) {
        if(!Collation.isSpecialCE32(ce32)) {
            long ce = modifier.modifyCE32(ce32);
            if(ce != Collation.NO_CE) {
                ce32 = dest.encodeOneCE(ce);
            }
        } else {
            int tag = Collation.tagFromCE32(ce32);
            if(tag == Collation.EXPANSION32_TAG) {
                const uint32_t *srcCE32s = reinterpret_cast<uint32_t *>(src.ce32s.getBuffer());
                srcCE32s += Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                // Inspect the source CE32s. Just copy them if none are modified.
                // Otherwise copy to modifiedCEs, with modifications.
                boolean isModified = false;
                for(int i = 0; i < length; ++i) {
                    ce32 = srcCE32s[i];
                    long ce;
                    if(Collation.isSpecialCE32(ce32) ||
                            (ce = modifier.modifyCE32(ce32)) == Collation.NO_CE) {
                        if(isModified) {
                            modifiedCEs[i] = Collation.ceFromCE32(ce32);
                        }
                    } else {
                        if(!isModified) {
                            for(int j = 0; j < i; ++j) {
                                modifiedCEs[j] = Collation.ceFromCE32(srcCE32s[j]);
                            }
                            isModified = true;
                        }
                        modifiedCEs[i] = ce;
                    }
                }
                if(isModified) {
                    ce32 = dest.encodeCEs(modifiedCEs, length);
                } else {
                    ce32 = dest.encodeExpansion32(
                        reinterpret_cast<const int *>(srcCE32s), length);
                }
            } else if(tag == Collation.EXPANSION_TAG) {
                const long *srcCEs = src.ce64s.getBuffer();
                srcCEs += Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                // Inspect the source CEs. Just copy them if none are modified.
                // Otherwise copy to modifiedCEs, with modifications.
                boolean isModified = false;
                for(int i = 0; i < length; ++i) {
                    long srcCE = srcCEs[i];
                    long ce = modifier.modifyCE(srcCE);
                    if(ce == Collation.NO_CE) {
                        if(isModified) {
                            modifiedCEs[i] = srcCE;
                        }
                    } else {
                        if(!isModified) {
                            for(int j = 0; j < i; ++j) {
                                modifiedCEs[j] = srcCEs[j];
                            }
                            isModified = true;
                        }
                        modifiedCEs[i] = ce;
                    }
                }
                if(isModified) {
                    ce32 = dest.encodeCEs(modifiedCEs, length);
                } else {
                    ce32 = dest.encodeExpansion(srcCEs, length);
                }
            } else if(tag == Collation.BUILDER_DATA_TAG) {
                // Copy the list of ConditionalCE32.
                ConditionalCE32 *cond = src.getConditionalCE32ForCE32(ce32);
                assert(!cond.hasContext());
                int destIndex = dest.addConditionalCE32(
                        cond.context, copyCE32(cond.ce32));
                ce32 = CollationDataBuilder.makeBuilderContextCE32(destIndex);
                while(cond.next >= 0) {
                    cond = src.getConditionalCE32(cond.next);
                    ConditionalCE32 *prevDestCond = dest.getConditionalCE32(destIndex);
                    destIndex = dest.addConditionalCE32(
                            cond.context, copyCE32(cond.ce32));
                    int suffixStart = cond.prefixLength() + 1;
                    dest.unsafeBackwardSet.addAll(cond.context.tempSubString(suffixStart));
                    prevDestCond.next = destIndex;
                }
            } else {
                // Just copy long CEs and Latin mini expansions (and other expected values) as is,
                // assuming that the modifier would not modify them.
                assert(tag == Collation.LONG_PRIMARY_TAG ||
                        tag == Collation.LONG_SECONDARY_TAG ||
                        tag == Collation.LATIN_EXPANSION_TAG ||
                        tag == Collation.HANGUL_TAG);
            }
        }
        return ce32;
    }

    const CollationDataBuilder &src;
    CollationDataBuilder &dest;
    const CollationDataBuilder.CEModifier &modifier;
    long modifiedCEs[Collation.MAX_EXPANSION_LENGTH];
    UErrorCode errorCode;
};

U_CDECL_BEGIN

static boolean U_CALLCONV
enumRangeForCopy(const void *context, int start, int end, uint32_t value) {
    return
        value == Collation.UNASSIGNED_CE32 || value == Collation.FALLBACK_CE32 ||
        ((CopyHelper *)context).copyRangeCE32(start, end, value);
}

U_CDECL_END

void
CollationDataBuilder.copyFrom(const CollationDataBuilder &src, const CEModifier &modifier,
                               ) {
    if(U_FAILURE) { return; }
    if(trie == null || utrie2_isFrozen(trie)) {
        errorCode = U_INVALID_STATE_ERROR;
        return;
    }
    CopyHelper helper(src, *this, modifier);
    utrie2_enum(src.trie, null, enumRangeForCopy, &helper);
    errorCode = helper.errorCode;
    // Update the contextChars and the unsafeBackwardSet while copying,
    // in case a character had conditional mappings in the source builder
    // and they were removed later.
    modified |= src.modified;
}

void
CollationDataBuilder.optimize(const UnicodeSet &set) {
    if(U_FAILURE || set.isEmpty()) { return; }
    UnicodeSetIterator iter(set);
    while(iter.next() && !iter.isString()) {
        int c = iter.getCodepoint();
        int ce32 = utrie2_get32(trie, c);
        if(ce32 == Collation.FALLBACK_CE32) {
            ce32 = base.getFinalCE32(base.getCE32(c));
            ce32 = copyFromBaseCE32(c, ce32, true);
            utrie2_set32(trie, c, ce32, &errorCode);
        }
    }
    modified = true;
}

void
CollationDataBuilder.suppressContractions(const UnicodeSet &set) {
    if(U_FAILURE || set.isEmpty()) { return; }
    UnicodeSetIterator iter(set);
    while(iter.next() && !iter.isString()) {
        int c = iter.getCodepoint();
        int ce32 = utrie2_get32(trie, c);
        if(ce32 == Collation.FALLBACK_CE32) {
            ce32 = base.getFinalCE32(base.getCE32(c));
            if(Collation.ce32HasContext(ce32)) {
                ce32 = copyFromBaseCE32(c, ce32, false /* without context */);
                utrie2_set32(trie, c, ce32, &errorCode);
            }
        } else if(isBuilderContextCE32(ce32)) {
            ce32 = getConditionalCE32ForCE32(ce32).ce32;
            // Simply abandon the list of ConditionalCE32.
            // The caller will copy this builder in the end,
            // eliminating unreachable data.
            utrie2_set32(trie, c, ce32, &errorCode);
            contextChars.remove(c);
        }
    }
    modified = true;
}

boolean
CollationDataBuilder.getJamoCE32s(uint32_t jamoCE32s[]) {
    if(U_FAILURE) { return false; }
    boolean anyJamoAssigned = base == null;  // always set jamoCE32s in the base data
    boolean needToCopyFromBase = false;
    for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {  // Count across Jamo types.
        int jamo = jamoCpFromIndex(j);
        boolean fromBase = false;
        int ce32 = utrie2_get32(trie, jamo);
        anyJamoAssigned |= Collation.isAssignedCE32(ce32);
        // TODO: Try to prevent [optimize [Jamo]] from counting as anyJamoAssigned.
        // (As of CLDR 24 [2013] the Korean tailoring does not optimize conjoining Jamo.)
        if(ce32 == Collation.FALLBACK_CE32) {
            fromBase = true;
            ce32 = base.getCE32(jamo);
        }
        if(Collation.isSpecialCE32(ce32)) {
            switch(Collation.tagFromCE32(ce32)) {
            case Collation.LONG_PRIMARY_TAG:
            case Collation.LONG_SECONDARY_TAG:
            case Collation.LATIN_EXPANSION_TAG:
                // Copy the ce32 as-is.
                break;
            case Collation.EXPANSION32_TAG:
            case Collation.EXPANSION_TAG:
            case Collation.PREFIX_TAG:
            case Collation.CONTRACTION_TAG:
                if(fromBase) {
                    // Defer copying until we know if anyJamoAssigned.
                    ce32 = Collation.FALLBACK_CE32;
                    needToCopyFromBase = true;
                }
                break;
            case Collation.IMPLICIT_TAG:
                // An unassigned Jamo should only occur in tests with incomplete bases.
                assert(fromBase);
                ce32 = Collation.FALLBACK_CE32;
                needToCopyFromBase = true;
                break;
            case Collation.OFFSET_TAG:
                ce32 = getCE32FromOffsetCE32(fromBase, jamo, ce32);
                break;
            case Collation.FALLBACK_TAG:
            case Collation.RESERVED_TAG_3:
            case Collation.BUILDER_DATA_TAG:
            case Collation.DIGIT_TAG:
            case Collation.U0000_TAG:
            case Collation.HANGUL_TAG:
            case Collation.LEAD_SURROGATE_TAG:
                errorCode = U_INTERNAL_PROGRAM_ERROR;
                return false;
            }
        }
        jamoCE32s[j] = ce32;
    }
    if(anyJamoAssigned && needToCopyFromBase) {
        for(int j = 0; j < CollationData.JAMO_CE32S_LENGTH; ++j) {
            if(jamoCE32s[j] == Collation.FALLBACK_CE32) {
                int jamo = jamoCpFromIndex(j);
                jamoCE32s[j] = copyFromBaseCE32(jamo, base.getCE32(jamo),
                                                /*withContext=*/ true);
            }
        }
    }
    return anyJamoAssigned && U_SUCCESS;
}

void
CollationDataBuilder.setDigitTags() {
    UnicodeSet digits(UNICODE_STRING_SIMPLE("[:Nd:]"));
    if(U_FAILURE) { return; }
    UnicodeSetIterator iter(digits);
    while(iter.next()) {
        assert(!iter.isString());
        int c = iter.getCodepoint();
        int ce32 = utrie2_get32(trie, c);
        if(ce32 != Collation.FALLBACK_CE32 && ce32 != Collation.UNASSIGNED_CE32) {
            int index = addCE32(ce32);
            if(U_FAILURE) { return; }
            if(index > Collation.MAX_INDEX) {
                errorCode = U_BUFFER_OVERFLOW_ERROR;
                return;
            }
            ce32 = Collation.makeCE32FromTagIndexAndLength(
                    Collation.DIGIT_TAG, index, u_charDigitValue(c));
            utrie2_set32(trie, c, ce32, &errorCode);
        }
    }
}

U_CDECL_BEGIN

static boolean U_CALLCONV
enumRangeLeadValue(const void *context, int /*start*/, int /*end*/, uint32_t value) {
    int *pValue = (int *)context;
    if(value == Collation.UNASSIGNED_CE32) {
        value = Collation.LEAD_ALL_UNASSIGNED;
    } else if(value == Collation.FALLBACK_CE32) {
        value = Collation.LEAD_ALL_FALLBACK;
    } else {
        *pValue = Collation.LEAD_MIXED;
        return false;
    }
    if(*pValue < 0) {
        *pValue = (int32_t)value;
    } else if(*pValue != (int32_t)value) {
        *pValue = Collation.LEAD_MIXED;
        return false;
    }
    return true;
}

U_CDECL_END

void
CollationDataBuilder.setLeadSurrogates() {
    for(UChar lead = 0xd800; lead < 0xdc00; ++lead) {
        int value = -1;
        utrie2_enumForLeadSurrogate(trie, lead, null, enumRangeLeadValue, &value);
        utrie2_set32ForLeadSurrogateCodeUnit(
            trie, lead,
            Collation.makeCE32FromTagAndIndex(Collation.LEAD_SURROGATE_TAG, 0) | (uint32_t)value,
            &errorCode);
    }
}

void
CollationDataBuilder.build(CollationData &data) {
    buildMappings(data);
    if(base != null) {
        data.numericPrimary = base.numericPrimary;
        data.compressibleBytes = base.compressibleBytes;
        data.scripts = base.scripts;
        data.scriptsLength = base.scriptsLength;
    }
    buildFastLatinTable(data);
}

void
CollationDataBuilder.buildMappings(CollationData &data) {
    if(U_FAILURE) { return; }
    if(trie == null || utrie2_isFrozen(trie)) {
        errorCode = U_INVALID_STATE_ERROR;
        return;
    }

    buildContexts;

    uint32_t jamoCE32s[CollationData.JAMO_CE32S_LENGTH];
    int jamoIndex = -1;
    if(getJamoCE32s(jamoCE32s)) {
        jamoIndex = ce32s.size();
        for(int i = 0; i < CollationData.JAMO_CE32S_LENGTH; ++i) {
            ce32s.addElement((int32_t)jamoCE32s[i]);
        }
        // Small optimization: Use a bit in the Hangul ce32
        // to indicate that none of the Jamo CE32s are isSpecialCE32()
        // (as it should be in the root collator).
        // It allows CollationIterator to avoid recursive function calls and per-Jamo tests.
        // In order to still have good trie compression and keep this code simple,
        // we only set this flag if a whole block of 588 Hangul syllables starting with
        // a common leading consonant (Jamo L) has this property.
        boolean isAnyJamoVTSpecial = false;
        for(int i = Hangul.JAMO_L_COUNT; i < CollationData.JAMO_CE32S_LENGTH; ++i) {
            if(Collation.isSpecialCE32(jamoCE32s[i])) {
                isAnyJamoVTSpecial = true;
                break;
            }
        }
        uint32_t hangulCE32 = Collation.makeCE32FromTagAndIndex(Collation.HANGUL_TAG, 0);
        int c = Hangul.HANGUL_BASE;
        for(int i = 0; i < Hangul.JAMO_L_COUNT; ++i) {  // iterate over the Jamo L
            int ce32 = hangulCE32;
            if(!isAnyJamoVTSpecial && !Collation.isSpecialCE32(jamoCE32s[i])) {
                ce32 |= Collation.HANGUL_NO_SPECIAL_JAMO;
            }
            int limit = c + Hangul.JAMO_VT_COUNT;
            utrie2_setRange32(trie, c, limit - 1, ce32, true, &errorCode);
            c = limit;
        }
    } else {
        // Copy the Hangul CE32s from the base in blocks per Jamo L,
        // assuming that HANGUL_NO_SPECIAL_JAMO is set or not set for whole blocks.
        for(int c = Hangul.HANGUL_BASE; c < Hangul.HANGUL_LIMIT;) {
            int ce32 = base.getCE32(c);
            assert(Collation.hasCE32Tag(ce32, Collation.HANGUL_TAG));
            int limit = c + Hangul.JAMO_VT_COUNT;
            utrie2_setRange32(trie, c, limit - 1, ce32, true, &errorCode);
            c = limit;
        }
    }

    setDigitTags;
    setLeadSurrogates;

    // For U+0000, move its normal ce32 into CE32s[0] and set U0000_TAG.
    ce32s.setElementAt((int32_t)utrie2_get32(trie, 0), 0);
    utrie2_set32(trie, 0, Collation.makeCE32FromTagAndIndex(Collation.U0000_TAG, 0), &errorCode);

    utrie2_freeze(trie, UTRIE2_32_VALUE_BITS, &errorCode);
    if(U_FAILURE) { return; }

    // Mark each lead surrogate as "unsafe"
    // if any of its 1024 associated supplementary code points is "unsafe".
    int c = 0x10000;
    for(UChar lead = 0xd800; lead < 0xdc00; ++lead, c += 0x400) {
        if(unsafeBackwardSet.containsSome(c, c + 0x3ff)) {
            unsafeBackwardSet.add(lead);
        }
    }
    unsafeBackwardSet.freeze();

    data.trie = trie;
    data.ce32s = reinterpret_cast<const uint32_t *>(ce32s.getBuffer());
    data.ces = ce64s.getBuffer();
    data.contexts = contexts.getBuffer();

    data.ce32sLength = ce32s.size();
    data.cesLength = ce64s.size();
    data.contextsLength = contexts.length();

    data.base = base;
    if(jamoIndex >= 0) {
        data.jamoCE32s = data.ce32s + jamoIndex;
    } else {
        data.jamoCE32s = base.jamoCE32s;
    }
    data.unsafeBackwardSet = &unsafeBackwardSet;
}

void
CollationDataBuilder.clearContexts() {
    contexts.remove();
    UnicodeSetIterator iter(contextChars);
    while(iter.next()) {
        assert(!iter.isString());
        int ce32 = utrie2_get32(trie, iter.getCodepoint());
        assert(isBuilderContextCE32(ce32));
        getConditionalCE32ForCE32(ce32).builtCE32 = Collation.NO_CE32;
    }
}

void
CollationDataBuilder.buildContexts() {
    if(U_FAILURE) { return; }
    // Ignore abandoned lists and the cached builtCE32,
    // and build all contexts from scratch.
    contexts.remove();
    UnicodeSetIterator iter(contextChars);
    while(iter.next()) {
        assert(!iter.isString());
        int c = iter.getCodepoint();
        int ce32 = utrie2_get32(trie, c);
        if(!isBuilderContextCE32(ce32)) {
            // Impossible: No context data for c in contextChars.
            errorCode = U_INTERNAL_PROGRAM_ERROR;
            return;
        }
        ConditionalCE32 *cond = getConditionalCE32ForCE32(ce32);
        ce32 = buildContext(cond);
        utrie2_set32(trie, c, ce32, &errorCode);
    }
}

uint32_t
CollationDataBuilder.buildContext(ConditionalCE32 *head) {
    if(U_FAILURE) { return 0; }
    // The list head must have no context.
    assert(!head.hasContext());
    // The list head must be followed by one or more nodes that all do have context.
    assert(head.next >= 0);
    UCharsTrieBuilder prefixBuilder;
    UCharsTrieBuilder contractionBuilder;
    for(ConditionalCE32 *cond = head;; cond = getConditionalCE32(cond.next)) {
        // After the list head, the prefix or suffix can be empty, but not both.
        assert(cond == head || cond.hasContext());
        int prefixLength = cond.prefixLength();
        UnicodeString prefix(cond.context, 0, prefixLength + 1);
        // Collect all contraction suffixes for one prefix.
        ConditionalCE32 *firstCond = cond;
        ConditionalCE32 *lastCond = cond;
        while(cond.next >= 0 &&
                (cond = getConditionalCE32(cond.next)).context.startsWith(prefix)) {
            lastCond = cond;
        }
        int ce32;
        int suffixStart = prefixLength + 1;  // == prefix.length()
        if(lastCond.context.length() == suffixStart) {
            // One prefix without contraction suffix.
            assert(firstCond == lastCond);
            ce32 = lastCond.ce32;
            cond = lastCond;
        } else {
            // Build the contractions trie.
            contractionBuilder.clear();
            // Entry for an empty suffix, to be stored before the trie.
            uint32_t emptySuffixCE32;
            uint32_t flags = 0;
            if(firstCond.context.length() == suffixStart) {
                // There is a mapping for the prefix and the single character c. (p|c)
                // If no other suffix matches, then we return this value.
                emptySuffixCE32 = firstCond.ce32;
                cond = getConditionalCE32(firstCond.next);
            } else {
                // There is no mapping for the prefix and just the single character.
                // (There is no p|c, only p|cd, p|ce etc.)
                flags |= Collation.CONTRACT_SINGLE_CP_NO_MATCH;
                // When the prefix matches but none of the prefix-specific suffixes,
                // then we fall back to the mappings with the next-longest prefix,
                // and ultimately to mappings with no prefix.
                // Each fallback might be another set of contractions.
                // For example, if there are mappings for ch, p|cd, p|ce, but not for p|c,
                // then in text "pch" we find the ch contraction.
                for(cond = head;; cond = getConditionalCE32(cond.next)) {
                    int length = cond.prefixLength();
                    if(length == prefixLength) { break; }
                    if(cond.defaultCE32 != Collation.NO_CE32 &&
                            (length==0 || prefix.endsWith(cond.context, 1, length))) {
                        emptySuffixCE32 = cond.defaultCE32;
                    }
                }
                cond = firstCond;
            }
            // Optimization: Set a flag when
            // the first character of every contraction suffix has lccc!=0.
            // Short-circuits contraction matching when a normal letter follows.
            flags |= Collation.CONTRACT_NEXT_CCC;
            // Add all of the non-empty suffixes into the contraction trie.
            for(;;) {
                UnicodeString suffix(cond.context, suffixStart);
                char fcd16 = nfcImpl.getFCD16(suffix.char32At(0));
                if(fcd16 <= 0xff) {
                    flags &= ~Collation.CONTRACT_NEXT_CCC;
                }
                fcd16 = nfcImpl.getFCD16(suffix.char32At(suffix.length() - 1));
                if(fcd16 > 0xff) {
                    // The last suffix character has lccc!=0, allowing for discontiguous contractions.
                    flags |= Collation.CONTRACT_TRAILING_CCC;
                }
                contractionBuilder.add(suffix, (int32_t)cond.ce32);
                if(cond == lastCond) { break; }
                cond = getConditionalCE32(cond.next);
            }
            int index = addContextTrie(emptySuffixCE32, contractionBuilder);
            if(U_FAILURE) { return 0; }
            if(index > Collation.MAX_INDEX) {
                errorCode = U_BUFFER_OVERFLOW_ERROR;
                return 0;
            }
            ce32 = Collation.makeCE32FromTagAndIndex(Collation.CONTRACTION_TAG, index) | flags;
        }
        assert(cond == lastCond);
        firstCond.defaultCE32 = ce32;
        if(prefixLength == 0) {
            if(cond.next < 0) {
                // No non-empty prefixes, only contractions.
                return ce32;
            }
        } else {
            prefix.remove(0, 1);  // Remove the length unit.
            prefix.reverse();
            prefixBuilder.add(prefix, (int32_t)ce32);
            if(cond.next < 0) { break; }
        }
    }
    assert(head.defaultCE32 != Collation.NO_CE32);
    int index = addContextTrie(head.defaultCE32, prefixBuilder);
    if(U_FAILURE) { return 0; }
    if(index > Collation.MAX_INDEX) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return 0;
    }
    return Collation.makeCE32FromTagAndIndex(Collation.PREFIX_TAG, index);
}

int32_t
CollationDataBuilder.addContextTrie(int defaultCE32, UCharsTrieBuilder trieBuilder) {
    UnicodeString context;
    context.append((char)(defaultCE32 >> 16)).append((char)defaultCE32);
    UnicodeString trieString;
    context.append(trieBuilder.buildUnicodeString(USTRINGTRIE_BUILD_SMALL, trieString));
    if(U_FAILURE) { return -1; }
    int index = contexts.indexOf(context);
    if(index < 0) {
        index = contexts.length();
        contexts.append(context);
    }
    return index;
}

void
CollationDataBuilder.buildFastLatinTable(CollationData &data) {
    if(U_FAILURE || !fastLatinEnabled) { return; }

    delete fastLatinBuilder;
    fastLatinBuilder = new CollationFastLatinBuilder;
    if(fastLatinBuilder == null) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return;
    }
    if(fastLatinBuilder.forData(data)) {
        const char *table = fastLatinBuilder.getTable();
        int length = fastLatinBuilder.lengthOfTable();
        if(base != null && length == base.fastLatinTableLength &&
                uprv_memcmp(table, base.fastLatinTable, length * 2) == 0) {
            // Same fast Latin table as in the base, use that one instead.
            delete fastLatinBuilder;
            fastLatinBuilder = null;
            table = base.fastLatinTable;
        }
        data.fastLatinTable = table;
        data.fastLatinTableLength = length;
    } else {
        delete fastLatinBuilder;
        fastLatinBuilder = null;
    }
}

int32_t
CollationDataBuilder.getCEs(const UnicodeString &s, long ces[], int cesLength) {
    return getCEs(s, 0, ces, cesLength);
}

int32_t
CollationDataBuilder.getCEs(const UnicodeString &prefix, const UnicodeString &s,
                             long ces[], int cesLength) {
    int prefixLength = prefix.length();
    if(prefixLength == 0) {
        return getCEs(s, 0, ces, cesLength);
    } else {
        return getCEs(prefix + s, prefixLength, ces, cesLength);
    }
}

int32_t
CollationDataBuilder.getCEs(const UnicodeString &s, int start,
                             long ces[], int cesLength) {
    if(collIter == null) {
        collIter = new DataBuilderCollationIterator(*this);
        if(collIter == null) { return 0; }
    }
    return collIter.fetchCEs(s, start, ces, cesLength);
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
