/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* FCDIterCollationIterator.java, ported from uitercollationiterator.h/.cpp
*
* @since 2012sep23 (from utf16collationiterator.h)
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

/**
 * Incrementally checks the input text for FCD and normalizes where necessary.
 */
final class FCDIterCollationIterator extends IterCollationIterator {
public:
    FCDUIterCollationIterator(CollationData data, boolean numeric, UCharIterator &ui, int startIndex)
            : UIterCollationIterator(data, numeric, ui),
              state(ITER_CHECK_FWD), start(startIndex),
              nfcImpl(data.nfcImpl) {}

    virtual ~FCDUIterCollationIterator();

    virtual void resetToOffset(int newOffset);

    virtual int getOffset();

    virtual int nextCodePoint();

    virtual int previousCodePoint();

protected:
    virtual long handleNextCE32();

    virtual UChar handleGetTrailSurrogate();

    virtual void forwardNumCodePoints(int num);

    virtual void backwardNumCodePoints(int num);

private:
    /**
     * Switches to forward checking if possible.
     */
    void switchToForward();

    /**
     * Extends the FCD text segment forward or normalizes around pos.
     * @return true if success
     */
    boolean nextSegment();

    /**
     * Switches to backward checking.
     */
    void switchToBackward();

    /**
     * Extends the FCD text segment backward or normalizes around pos.
     * @return true if success
     */
    boolean previousSegment();

    boolean normalize(const UnicodeString &s);

    enum State {
        /**
         * The input text [start..(iter index)[ passes the FCD check.
         * Moving forward checks incrementally.
         * pos & limit are undefined.
         */
        ITER_CHECK_FWD,
        /**
         * The input text [(iter index)..limit[ passes the FCD check.
         * Moving backward checks incrementally.
         * start & pos are undefined.
         */
        ITER_CHECK_BWD,
        /**
         * The input text [start..limit[ passes the FCD check.
         * pos tracks the current text index.
         */
        ITER_IN_FCD_SEGMENT,
        /**
         * The input text [start..limit[ failed the FCD check and was normalized.
         * pos tracks the current index in the normalized string.
         * The text iterator is at the limit index.
         */
        IN_NORM_ITER_AT_LIMIT,
        /**
         * The input text [start..limit[ failed the FCD check and was normalized.
         * pos tracks the current index in the normalized string.
         * The text iterator is at the start index.
         */
        IN_NORM_ITER_AT_START
    };

    State state;

    int start;
    int pos;
    int limit;

    const Normalizer2Impl &nfcImpl;
    UnicodeString normalized;
}

// FCDUIterCollationIterator ----------------------------------------------- ***

    FCDUIterCollationIterator.~FCDUIterCollationIterator() {}

    void
    FCDUIterCollationIterator.resetToOffset(int newOffset) {
        UIterCollationIterator.resetToOffset(newOffset);
        start = newOffset;
        state = ITER_CHECK_FWD;
    }

    int32_t
    FCDUIterCollationIterator.getOffset() {
        if(state <= ITER_CHECK_BWD) {
            return iter.getIndex(&iter, UITER_CURRENT);
        } else if(state == ITER_IN_FCD_SEGMENT) {
            return pos;
        } else if(pos == 0) {
            return start;
        } else {
            return limit;
        }
    }

    long
    FCDUIterCollationIterator.handleNextCE32() {
        for(;;) {
            if(state == ITER_CHECK_FWD) {
                c = iter.next(&iter);
                if(c < 0) {
                    return Collation.FALLBACK_CE32;
                }
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasLccc(iter.current(&iter))) {
                        iter.previous(&iter);
                        if(!nextSegment) {
                            c = Collation.SENTINEL_CP;
                            return Collation.FALLBACK_CE32;
                        }
                        continue;
                    }
                }
                break;
            } else if(state == ITER_IN_FCD_SEGMENT && pos != limit) {
                c = iter.next(&iter);
                ++pos;
                assert(c >= 0);
                break;
            } else if(state >= IN_NORM_ITER_AT_LIMIT && pos != normalized.length()) {
                c = normalized[pos++];
                break;
            } else {
                switchToForward();
            }
        }
        return UTRIE2_GET32_FROM_U16_SINGLE_LEAD(trie, c);
            return makeCodePointAndCE32Pair(c, result);
    }

    UChar
    FCDUIterCollationIterator.handleGetTrailSurrogate() {
        if(state <= ITER_IN_FCD_SEGMENT) {
            int trail = iter.next(&iter);
            if(U16_IS_TRAIL(trail)) {
                if(state == ITER_IN_FCD_SEGMENT) { ++pos; }
            } else if(trail >= 0) {
                iter.previous(&iter);
            }
            return (UChar)trail;
        } else {
            assert(pos < normalized.length());
            UChar trail;
            if(U16_IS_TRAIL(trail = normalized[pos])) { ++pos; }
            return trail;
        }
    }

    int
    FCDUIterCollationIterator.nextCodePoint() {
        int c;
        for(;;) {
            if(state == ITER_CHECK_FWD) {
                c = iter.next(&iter);
                if(c < 0) {
                    return c;
                }
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasLccc(iter.current(&iter))) {
                        iter.previous(&iter);
                        if(!nextSegment) {
                            return Collation.SENTINEL_CP;
                        }
                        continue;
                    }
                }
                if(U16_IS_LEAD(c)) {
                    int trail = iter.next(&iter);
                    if(U16_IS_TRAIL(trail)) {
                        return U16_GET_SUPPLEMENTARY(c, trail);
                    } else if(trail >= 0) {
                        iter.previous(&iter);
                    }
                }
                return c;
            } else if(state == ITER_IN_FCD_SEGMENT && pos != limit) {
                c = uiter_next32(&iter);
                pos += Character.charCount(c);
                assert(c >= 0);
                return c;
            } else if(state >= IN_NORM_ITER_AT_LIMIT && pos != normalized.length()) {
                c = normalized.char32At(pos);
                pos += Character.charCount(c);
                return c;
            } else {
                switchToForward();
            }
        }
    }

    int
    FCDUIterCollationIterator.previousCodePoint() {
        int c;
        for(;;) {
            if(state == ITER_CHECK_BWD) {
                c = iter.previous(&iter);
                if(c < 0) {
                    start = pos = 0;
                    state = ITER_IN_FCD_SEGMENT;
                    return Collation.SENTINEL_CP;
                }
                if(CollationFCD.hasLccc(c)) {
                    int prev = Collation.SENTINEL_CP;
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasTccc(prev = iter.previous(&iter))) {
                        iter.next(&iter);
                        if(prev >= 0) {
                            iter.next(&iter);
                        }
                        if(!previousSegment) {
                            return Collation.SENTINEL_CP;
                        }
                        continue;
                    }
                    // hasLccc(trail)=true for all trail surrogates
                    if(U16_IS_TRAIL(c)) {
                        if(prev < 0) {
                            prev = iter.previous(&iter);
                        }
                        if(U16_IS_LEAD(prev)) {
                            return U16_GET_SUPPLEMENTARY(prev, c);
                        }
                    }
                    if(prev >= 0) {
                        iter.next(&iter);
                    }
                }
                return c;
            } else if(state == ITER_IN_FCD_SEGMENT && pos != start) {
                c = uiter_previous32(&iter);
                pos -= Character.charCount(c);
                assert(c >= 0);
                return c;
            } else if(state >= IN_NORM_ITER_AT_LIMIT && pos != 0) {
                c = normalized.char32At(pos - 1);
                pos -= Character.charCount(c);
                return c;
            } else {
                switchToBackward();
            }
        }
    }

    void
    FCDUIterCollationIterator.forwardNumCodePoints(int num) {
        // Specify the class to avoid a virtual-function indirection.
        // In Java, we would declare this class final.
        while(num > 0 && FCDUIterCollationIterator.nextCodePoint >= 0) {
            --num;
        }
    }

    void
    FCDUIterCollationIterator.backwardNumCodePoints(int num) {
        // Specify the class to avoid a virtual-function indirection.
        // In Java, we would declare this class final.
        while(num > 0 && FCDUIterCollationIterator.previousCodePoint >= 0) {
            --num;
        }
    }

    void
    FCDUIterCollationIterator.switchToForward() {
        assert(state == ITER_CHECK_BWD ||
                (state == ITER_IN_FCD_SEGMENT && pos == limit) ||
                (state >= IN_NORM_ITER_AT_LIMIT && pos == normalized.length()));
        if(state == ITER_CHECK_BWD) {
            // Turn around from backward checking.
            start = pos = iter.getIndex(&iter, UITER_CURRENT);
            if(pos == limit) {
                state = ITER_CHECK_FWD;  // Check forward.
            } else {  // pos < limit
                state = ITER_IN_FCD_SEGMENT;  // Stay in FCD segment.
            }
        } else {
            // Reached the end of the FCD segment.
            if(state == ITER_IN_FCD_SEGMENT) {
                // The input text segment is FCD, extend it forward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking forward from it.
                if(state == IN_NORM_ITER_AT_START) {
                    iter.move(&iter, limit - start, UITER_CURRENT);
                }
                start = limit;
            }
            state = ITER_CHECK_FWD;
        }
    }

    boolean
    FCDUIterCollationIterator.nextSegment() {
        if(U_FAILURE) { return false; }
        assert(state == ITER_CHECK_FWD);
        // The input text [start..(iter index)[ passes the FCD check.
        pos = iter.getIndex(&iter, UITER_CURRENT);
        // Collect the characters being checked, in case they need to be normalized.
        UnicodeString s;
        uint8_t prevCC = 0;
        for(;;) {
            // Fetch the next character and its fcd16 value.
            int c = uiter_next32(&iter);
            if(c < 0) { break; }
            char fcd16 = nfcImpl.getFCD16(c);
            uint8_t leadCC = (uint8_t)(fcd16 >> 8);
            if(leadCC == 0 && !s.isEmpty()) {
                // FCD boundary before this character.
                uiter_previous32(&iter);
                break;
            }
            s.append(c);
            if(leadCC != 0 && (prevCC > leadCC || CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the next FCD boundary and normalize.
                for(;;) {
                    c = uiter_next32(&iter);
                    if(c < 0) { break; }
                    if(nfcImpl.getFCD16(c) <= 0xff) {
                        uiter_previous32(&iter);
                        break;
                    }
                    s.append(c);
                }
                if(!normalize(s)) { return false; }
                start = pos;
                limit = pos + s.length();
                state = IN_NORM_ITER_AT_LIMIT;
                pos = 0;
                return true;
            }
            prevCC = (uint8_t)fcd16;
            if(prevCC == 0) {
                // FCD boundary after the last character.
                break;
            }
        }
        limit = pos + s.length();
        assert(pos != limit);
        iter.move(&iter, -s.length(), UITER_CURRENT);
        state = ITER_IN_FCD_SEGMENT;
        return true;
    }

    void
    FCDUIterCollationIterator.switchToBackward() {
        assert(state == ITER_CHECK_FWD ||
                (state == ITER_IN_FCD_SEGMENT && pos == start) ||
                (state >= IN_NORM_ITER_AT_LIMIT && pos == 0));
        if(state == ITER_CHECK_FWD) {
            // Turn around from forward checking.
            limit = pos = iter.getIndex(&iter, UITER_CURRENT);
            if(pos == start) {
                state = ITER_CHECK_BWD;  // Check backward.
            } else {  // pos > start
                state = ITER_IN_FCD_SEGMENT;  // Stay in FCD segment.
            }
        } else {
            // Reached the start of the FCD segment.
            if(state == ITER_IN_FCD_SEGMENT) {
                // The input text segment is FCD, extend it backward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking backward from it.
                if(state == IN_NORM_ITER_AT_LIMIT) {
                    iter.move(&iter, start - limit, UITER_CURRENT);
                }
                limit = start;
            }
            state = ITER_CHECK_BWD;
        }
    }

    boolean
    FCDUIterCollationIterator.previousSegment() {
        if(U_FAILURE) { return Collation.SENTINEL_CP; }
        assert(state == ITER_CHECK_BWD);
        // The input text [(iter index)..limit[ passes the FCD check.
        pos = iter.getIndex(&iter, UITER_CURRENT);
        // Collect the characters being checked, in case they need to be normalized.
        UnicodeString s;
        uint8_t nextCC = 0;
        for(;;) {
            // Fetch the previous character and its fcd16 value.
            int c = uiter_previous32(&iter);
            if(c < 0) { break; }
            char fcd16 = nfcImpl.getFCD16(c);
            uint8_t trailCC = (uint8_t)fcd16;
            if(trailCC == 0 && !s.isEmpty()) {
                // FCD boundary after this character.
                uiter_next32(&iter);
                break;
            }
            s.append(c);
            if(trailCC != 0 && ((nextCC != 0 && trailCC > nextCC) ||
                                CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the previous FCD boundary and normalize.
                while(fcd16 > 0xff) {
                    c = uiter_previous32(&iter);
                    if(c < 0) { break; }
                    fcd16 = nfcImpl.getFCD16(c);
                    if(fcd16 == 0) {
                        uiter_next32(&iter);
                        break;
                    }
                    s.append(c);
                }
                s.reverse();
                if(!normalize(s)) { return false; }
                limit = pos;
                start = pos - s.length();
                state = IN_NORM_ITER_AT_START;
                pos = normalized.length();
                return true;
            }
            nextCC = (uint8_t)(fcd16 >> 8);
            if(nextCC == 0) {
                // FCD boundary before the following character.
                break;
            }
        }
        start = pos - s.length();
        assert(pos != start);
        iter.move(&iter, s.length(), UITER_CURRENT);
        state = ITER_IN_FCD_SEGMENT;
        return true;
    }

    boolean
    FCDUIterCollationIterator.normalize(const UnicodeString &s) {
        // NFD without argument checking.
        assert(U_SUCCESS);
        nfcImpl.decompose(s, normalized);
        return U_SUCCESS;
    }
