/*
*******************************************************************************
* Copyright (C) 2010-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* utf16collationiterator.h
*
* @since 2010oct27
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

#include "cmemory.h"
#include "collation.h"
#include "collationdata.h"
#include "collationiterator.h"
#include "normalizer2impl.h"

/**
 * Incrementally checks the input text for FCD and normalizes where necessary.
 */
final class FCDUTF16CollationIterator extends UTF16CollationIterator {
public:
    FCDUTF16CollationIterator(CollationData data, boolean numeric,
                              const UChar *s, const UChar *p, const UChar *lim)
            : UTF16CollationIterator(data, numeric, s, p, lim),
              rawStart(s), segmentStart(p), segmentLimit(null), rawLimit(lim),
              nfcImpl(data.nfcImpl),
              checkDir(1) {}

    FCDUTF16CollationIterator(const FCDUTF16CollationIterator &other, const UChar *newText);

    virtual ~FCDUTF16CollationIterator();

    virtual boolean operator==(const CollationIterator &other);

    virtual void resetToOffset(int newOffset);

    virtual int getOffset();

    virtual int nextCodePoint();

    virtual int previousCodePoint();

protected:
    virtual long handleNextCE32();

    /* boolean foundNULTerminator(); */

    virtual void forwardNumCodePoints(int num);

    virtual void backwardNumCodePoints(int num);

private:
    /**
     * Switches to forward checking if possible.
     * To be called when checkDir < 0 || (checkDir == 0 && pos == limit).
     * Returns with checkDir > 0 || (checkDir == 0 && pos != limit).
     */
    void switchToForward();

    /**
     * Extend the FCD text segment forward or normalize around pos.
     * To be called when checkDir > 0 && pos != limit.
     * @return true if success, checkDir == 0 and pos != limit
     */
    boolean nextSegment();

    /**
     * Switches to backward checking.
     * To be called when checkDir > 0 || (checkDir == 0 && pos == start).
     * Returns with checkDir < 0 || (checkDir == 0 && pos != start).
     */
    void switchToBackward();

    /**
     * Extend the FCD text segment backward or normalize around pos.
     * To be called when checkDir < 0 && pos != start.
     * @return true if success, checkDir == 0 and pos != start
     */
    boolean previousSegment();

    boolean normalize(const UChar *from, const UChar *to);

    // Text pointers: The input text is [rawStart, rawLimit[
    // where rawLimit can be null for NUL-terminated text.
    //
    // checkDir > 0:
    //
    // The input text [segmentStart..pos[ passes the FCD check.
    // Moving forward checks incrementally.
    // segmentLimit is undefined. limit == rawLimit.
    //
    // checkDir < 0:
    // The input text [pos..segmentLimit[ passes the FCD check.
    // Moving backward checks incrementally.
    // segmentStart is undefined, start == rawStart.
    //
    // checkDir == 0:
    //
    // The input text [segmentStart..segmentLimit[ is being processed.
    // These pointers are at FCD boundaries.
    // Either this text segment already passes the FCD check
    // and segmentStart==start<=pos<=limit==segmentLimit,
    // or the current segment had to be normalized so that
    // [segmentStart..segmentLimit[ turned into the normalized string,
    // corresponding to normalized.getBuffer()==start<=pos<=limit==start+normalized.length().
    const UChar *rawStart;
    const UChar *segmentStart;
    const UChar *segmentLimit;
    // rawLimit==null for a NUL-terminated string.
    const UChar *rawLimit;

    const Normalizer2Impl &nfcImpl;
    UnicodeString normalized;
    // Direction of incremental FCD check. See comments before rawStart.
    int8_t checkDir;
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __UTF16COLLATIONITERATOR_H__
/*
*******************************************************************************
* Copyright (C) 2010-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* utf16collationiterator.cpp
*
* @since 2010oct27
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "charstr.h"
#include "cmemory.h"
#include "collation.h"
#include "collationdata.h"
#include "collationfcd.h"
#include "collationiterator.h"
#include "normalizer2impl.h"
#include "uassert.h"
#include "utf16collationiterator.h"

// FCDUTF16CollationIterator ----------------------------------------------- ***

FCDUTF16CollationIterator.FCDUTF16CollationIterator(const FCDUTF16CollationIterator &other,
                                                     const UChar *newText)
        : UTF16CollationIterator(other),
          rawStart(newText),
          segmentStart(newText + (other.segmentStart - other.rawStart)),
          segmentLimit(other.segmentLimit == null ? null : newText + (other.segmentLimit - other.rawStart)),
          rawLimit(other.rawLimit == null ? null : newText + (other.rawLimit - other.rawStart)),
          nfcImpl(other.nfcImpl),
          normalized(other.normalized),
          checkDir(other.checkDir) {
    if(checkDir != 0 || other.start == other.segmentStart) {
        start = newText + (other.start - other.rawStart);
        pos = newText + (other.pos - other.rawStart);
        limit = other.limit == null ? null : newText + (other.limit - other.rawStart);
    } else {
        start = normalized.getBuffer();
        pos = start + (other.pos - other.start);
        limit = start + normalized.length();
    }
}

FCDUTF16CollationIterator.~FCDUTF16CollationIterator() {}

boolean
FCDUTF16CollationIterator.operator==(const CollationIterator &other) {
    // Skip the UTF16CollationIterator and call its parent.
    if(!CollationIterator.operator==(other)) { return false; }
    const FCDUTF16CollationIterator &o = static_cast<const FCDUTF16CollationIterator &>(other);
    // Compare the iterator state but not the text: Assume that the caller does that.
    if(checkDir != o.checkDir) { return false; }
    if(checkDir == 0 && (start == segmentStart) != (o.start == o.segmentStart)) { return false; }
    if(checkDir != 0 || start == segmentStart) {
        return (pos - rawStart) == (o.pos - o.rawStart);
    } else {
        return (segmentStart - rawStart) == (o.segmentStart - o.rawStart) &&
                (pos - start) == (o.pos - o.start);
    }
}

void
FCDUTF16CollationIterator.resetToOffset(int newOffset) {
    reset();
    start = segmentStart = pos = rawStart + newOffset;
    limit = rawLimit;
    checkDir = 1;
}

int32_t
FCDUTF16CollationIterator.getOffset() {
    if(checkDir != 0 || start == segmentStart) {
        return (int32_t)(pos - rawStart);
    } else if(pos == start) {
        return (int32_t)(segmentStart - rawStart);
    } else {
        return (int32_t)(segmentLimit - rawStart);
    }
}

long
FCDUTF16CollationIterator.handleNextCE32() {
    for(;;) {
        if(checkDir > 0) {
            if(pos == limit) {
                c = Collation.SENTINEL_CP;
                return Collation.FALLBACK_CE32;
            }
            c = *pos++;
            if(CollationFCD.hasTccc(c)) {
                if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                        (pos != limit && CollationFCD.hasLccc(*pos))) {
                    --pos;
                    if(!nextSegment) {
                        c = Collation.SENTINEL_CP;
                        return Collation.FALLBACK_CE32;
                    }
                    c = *pos++;
                }
            }
            break;
        } else if(checkDir == 0 && pos != limit) {
            c = *pos++;
            break;
        } else {
            switchToForward();
        }
    }
    return UTRIE2_GET32_FROM_U16_SINGLE_LEAD(trie, c);
        return makeCodePointAndCE32Pair(c, result);
}

int
FCDUTF16CollationIterator.nextCodePoint() {
    int c;
    for(;;) {
        if(checkDir > 0) {
            if(pos == limit) {
                return Collation.SENTINEL_CP;
            }
            c = *pos++;
            if(CollationFCD.hasTccc(c)) {
                if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                        (pos != limit && CollationFCD.hasLccc(*pos))) {
                    --pos;
                    if(!nextSegment) {
                        return Collation.SENTINEL_CP;
                    }
                    c = *pos++;
                }
            } else if(c == 0 && limit == null) {
                limit = rawLimit = --pos;
                return Collation.SENTINEL_CP;
            }
            break;
        } else if(checkDir == 0 && pos != limit) {
            c = *pos++;
            break;
        } else {
            switchToForward();
        }
    }
    UChar trail;
    if(U16_IS_LEAD(c) && pos != limit && U16_IS_TRAIL(trail = *pos)) {
        ++pos;
        return U16_GET_SUPPLEMENTARY(c, trail);
    } else {
        return c;
    }
}

int
FCDUTF16CollationIterator.previousCodePoint() {
    int c;
    for(;;) {
        if(checkDir < 0) {
            if(pos == start) {
                return Collation.SENTINEL_CP;
            }
            c = *--pos;
            if(CollationFCD.hasLccc(c)) {
                if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                        (pos != start && CollationFCD.hasTccc(*(pos - 1)))) {
                    ++pos;
                    if(!previousSegment) {
                        return Collation.SENTINEL_CP;
                    }
                    c = *--pos;
                }
            }
            break;
        } else if(checkDir == 0 && pos != start) {
            c = *--pos;
            break;
        } else {
            switchToBackward();
        }
    }
    UChar lead;
    if(U16_IS_TRAIL(c) && pos != start && U16_IS_LEAD(lead = *(pos - 1))) {
        --pos;
        return U16_GET_SUPPLEMENTARY(lead, c);
    } else {
        return c;
    }
}

void
FCDUTF16CollationIterator.forwardNumCodePoints(int num) {
    // Specify the class to avoid a virtual-function indirection.
    // In Java, we would declare this class final.
    while(num > 0 && FCDUTF16CollationIterator.nextCodePoint >= 0) {
        --num;
    }
}

void
FCDUTF16CollationIterator.backwardNumCodePoints(int num) {
    // Specify the class to avoid a virtual-function indirection.
    // In Java, we would declare this class final.
    while(num > 0 && FCDUTF16CollationIterator.previousCodePoint >= 0) {
        --num;
    }
}

void
FCDUTF16CollationIterator.switchToForward() {
    assert(checkDir < 0 || (checkDir == 0 && pos == limit));
    if(checkDir < 0) {
        // Turn around from backward checking.
        start = segmentStart = pos;
        if(pos == segmentLimit) {
            limit = rawLimit;
            checkDir = 1;  // Check forward.
        } else {  // pos < segmentLimit
            checkDir = 0;  // Stay in FCD segment.
        }
    } else {
        // Reached the end of the FCD segment.
        if(start == segmentStart) {
            // The input text segment is FCD, extend it forward.
        } else {
            // The input text segment needed to be normalized.
            // Switch to checking forward from it.
            pos = start = segmentStart = segmentLimit;
            // Note: If this segment is at the end of the input text,
            // then it might help to return false to indicate that, so that
            // we do not have to re-check and normalize when we turn around and go backwards.
            // However, that would complicate the call sites for an optimization of an unusual case.
        }
        limit = rawLimit;
        checkDir = 1;
    }
}

boolean
FCDUTF16CollationIterator.nextSegment() {
    if(U_FAILURE) { return false; }
    assert(checkDir > 0 && pos != limit);
    // The input text [segmentStart..pos[ passes the FCD check.
    const UChar *p = pos;
    uint8_t prevCC = 0;
    for(;;) {
        // Fetch the next character's fcd16 value.
        const UChar *q = p;
        char fcd16 = nfcImpl.nextFCD16(p, rawLimit);
        uint8_t leadCC = (uint8_t)(fcd16 >> 8);
        if(leadCC == 0 && q != pos) {
            // FCD boundary before the [q, p[ character.
            limit = segmentLimit = q;
            break;
        }
        if(leadCC != 0 && (prevCC > leadCC || CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
            // Fails FCD check. Find the next FCD boundary and normalize.
            do {
                q = p;
            } while(p != rawLimit && nfcImpl.nextFCD16(p, rawLimit) > 0xff);
            if(!normalize(pos, q)) { return false; }
            pos = start;
            break;
        }
        prevCC = (uint8_t)fcd16;
        if(p == rawLimit || prevCC == 0) {
            // FCD boundary after the last character.
            limit = segmentLimit = p;
            break;
        }
    }
    assert(pos != limit);
    checkDir = 0;
    return true;
}

void
FCDUTF16CollationIterator.switchToBackward() {
    assert(checkDir > 0 || (checkDir == 0 && pos == start));
    if(checkDir > 0) {
        // Turn around from forward checking.
        limit = segmentLimit = pos;
        if(pos == segmentStart) {
            start = rawStart;
            checkDir = -1;  // Check backward.
        } else {  // pos > segmentStart
            checkDir = 0;  // Stay in FCD segment.
        }
    } else {
        // Reached the start of the FCD segment.
        if(start == segmentStart) {
            // The input text segment is FCD, extend it backward.
        } else {
            // The input text segment needed to be normalized.
            // Switch to checking backward from it.
            pos = limit = segmentLimit = segmentStart;
        }
        start = rawStart;
        checkDir = -1;
    }
}

boolean
FCDUTF16CollationIterator.previousSegment() {
    if(U_FAILURE) { return Collation.SENTINEL_CP; }
    assert(checkDir < 0 && pos != start);
    // The input text [pos..segmentLimit[ passes the FCD check.
    const UChar *p = pos;
    uint8_t nextCC = 0;
    for(;;) {
        // Fetch the previous character's fcd16 value.
        const UChar *q = p;
        char fcd16 = nfcImpl.previousFCD16(rawStart, p);
        uint8_t trailCC = (uint8_t)fcd16;
        if(trailCC == 0 && q != pos) {
            // FCD boundary after the [p, q[ character.
            start = segmentStart = q;
            break;
        }
        if(trailCC != 0 && ((nextCC != 0 && trailCC > nextCC) ||
                            CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
            // Fails FCD check. Find the previous FCD boundary and normalize.
            do {
                q = p;
            } while(fcd16 > 0xff && p != rawStart &&
                    (fcd16 = nfcImpl.previousFCD16(rawStart, p)) != 0);
            if(!normalize(q, pos)) { return false; }
            pos = limit;
            break;
        }
        nextCC = (uint8_t)(fcd16 >> 8);
        if(p == rawStart || nextCC == 0) {
            // FCD boundary before the following character.
            start = segmentStart = p;
            break;
        }
    }
    assert(pos != start);
    checkDir = 0;
    return true;
}

boolean
FCDUTF16CollationIterator.normalize(const UChar *from, const UChar *to) {
    // NFD without argument checking.
    assert(U_SUCCESS);
    nfcImpl.decompose(from, to, normalized, (int32_t)(to - from));
    if(U_FAILURE) { return false; }
    // Switch collation processing into the FCD buffer
    // with the result of normalizing [segmentStart, segmentLimit[.
    segmentStart = from;
    segmentLimit = to;
    start = normalized.getBuffer();
    limit = start + normalized.length();
    return true;
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
