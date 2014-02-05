/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* IterCollationIterator.java, ported from uitercollationiterator.h/.cpp
*
* @since 2012sep23 (from utf16collationiterator.h)
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import com.ibm.icu.text.UCharacterIterator;

/**
 * UCharIterator-based collation element and character iterator.
 * Handles normalized text, with length or NUL-terminated.
 * Unnormalized text is handled by a subclass.
 */
public class IterCollationIterator extends CollationIterator {
    public IterCollationIterator(CollationData d, boolean numeric, UCharacterIterator ui) {
        super(d, numeric);
        iter = ui;
    }

    @Override
    public void resetToOffset(int newOffset) {
        reset();
        iter.setIndex(newOffset);
    }

    @Override
    public int getOffset() {
        return iter.getIndex();
    }

    @Override
    int nextCodePoint() {
        return iter.nextCodePoint();
    }

    @Override
    int previousCodePoint() {
        return iter.previousCodePoint();
    }

    @Override
    protected long handleNextCE32() {
        int c = iter.next();
        if(c < 0) {
            return NO_CP_AND_CE32;
        }
        return makeCodePointAndCE32Pair(c, trie.getFromU16SingleLead((char)c));
    }

    @Override
    protected char handleGetTrailSurrogate() {
        int trail = iter.next();
        if(!isTrailSurrogate(trail) && trail >= 0) { iter.previous(); }
        return (char)trail;
    }

    @Override
    protected void forwardNumCodePoints(int num) {
        iter.moveCodePointIndex(num);
    }

    @Override
    protected void backwardNumCodePoints(int num) {
        iter.moveCodePointIndex(-num);
    }

    protected UCharacterIterator iter;
}
