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

U_NAMESPACE_BEGIN

/**
 * UTF-16 collation element and character iterator.
 * Handles normalized UTF-16 text, with length or NUL-terminated.
 * Unnormalized text is handled by a subclass.
 */
class UTF16CollationIterator extends CollationIterator {
public:
    UTF16CollationIterator(CollationData d, boolean numeric,
                           const UChar *s, const UChar *p, const UChar *lim)
            : CollationIterator(d, numeric),
              start(s), pos(p), limit(lim) {}

    UTF16CollationIterator(const UTF16CollationIterator &other, const UChar *newText);

    virtual ~UTF16CollationIterator();

    virtual boolean operator==(const CollationIterator &other);

    virtual void resetToOffset(int newOffset);

    virtual int getOffset();

    void setText(const UChar *s, const UChar *lim) {
        reset();
        start = pos = s;
        limit = lim;
    }

    virtual int nextCodePoint();

    virtual int previousCodePoint();

protected:
    // Copy constructor only for subclasses which set the pointers.
    UTF16CollationIterator(const UTF16CollationIterator &other)
            : CollationIterator(other),
              start(null), pos(null), limit(null) {}

    virtual long handleNextCE32();

    virtual UChar handleGetTrailSurrogate();

    /* boolean foundNULTerminator(); */

    virtual void forwardNumCodePoints(int num);

    virtual void backwardNumCodePoints(int num);

    // UTF-16 string pointers.
    // limit can be null for NUL-terminated strings.
    const UChar *start, *pos, *limit;
};


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

U_NAMESPACE_BEGIN

UTF16CollationIterator.UTF16CollationIterator(const UTF16CollationIterator &other,
                                               const UChar *newText)
        : CollationIterator(other),
          start(newText),
          pos(newText + (other.pos - other.start)),
          limit(other.limit == null ? null : newText + (other.limit - other.start)) {
}

UTF16CollationIterator.~UTF16CollationIterator() {}

boolean
UTF16CollationIterator.operator==(const CollationIterator &other) {
    if(!CollationIterator.operator==(other)) { return false; }
    const UTF16CollationIterator &o = static_cast<const UTF16CollationIterator &>(other);
    // Compare the iterator state but not the text: Assume that the caller does that.
    return (pos - start) == (o.pos - o.start);
}

void
UTF16CollationIterator.resetToOffset(int newOffset) {
    reset();
    pos = start + newOffset;
}

int32_t
UTF16CollationIterator.getOffset() {
    return (int32_t)(pos - start);
}

long
UTF16CollationIterator.handleNextCE32() {
    if(pos == limit) {
        c = Collation.SENTINEL_CP;
        return Collation.FALLBACK_CE32;
    }
    c = *pos++;
    return UTRIE2_GET32_FROM_U16_SINGLE_LEAD(trie, c);
        return makeCodePointAndCE32Pair(c, result);
}

UChar
UTF16CollationIterator.handleGetTrailSurrogate() {
    if(pos == limit) { return 0; }
    UChar trail;
    if(U16_IS_TRAIL(trail = *pos)) { ++pos; }
    return trail;
}

int
UTF16CollationIterator.nextCodePoint(UErrorCode & /*errorCode*/) {
    if(pos == limit) {
        return Collation.SENTINEL_CP;
    }
    int c = *pos;
    if(c == 0 && limit == null) {
        limit = pos;
        return Collation.SENTINEL_CP;
    }
    ++pos;
    UChar trail;
    if(U16_IS_LEAD(c) && pos != limit && U16_IS_TRAIL(trail = *pos)) {
        ++pos;
        return U16_GET_SUPPLEMENTARY(c, trail);
    } else {
        return c;
    }
}

int
UTF16CollationIterator.previousCodePoint(UErrorCode & /*errorCode*/) {
    if(pos == start) {
        return Collation.SENTINEL_CP;
    }
    int c = *--pos;
    UChar lead;
    if(U16_IS_TRAIL(c) && pos != start && U16_IS_LEAD(lead = *(pos - 1))) {
        --pos;
        return U16_GET_SUPPLEMENTARY(lead, c);
    } else {
        return c;
    }
}

void
UTF16CollationIterator.forwardNumCodePoints(int num, UErrorCode & /*errorCode*/) {
    while(num > 0 && pos != limit) {
        int c = *pos;
        if(c == 0 && limit == null) {
            limit = pos;
            break;
        }
        ++pos;
        --num;
        if(U16_IS_LEAD(c) && pos != limit && U16_IS_TRAIL(*pos)) {
            ++pos;
        }
    }
}

void
UTF16CollationIterator.backwardNumCodePoints(int num, UErrorCode & /*errorCode*/) {
    while(num > 0 && pos != start) {
        int c = *--pos;
        --num;
        if(U16_IS_TRAIL(c) && pos != start && U16_IS_LEAD(*(pos-1))) {
            --pos;
        }
    }
}
