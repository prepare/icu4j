/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.CharacterIterator;
import java.util.Deque;
import com.ibm.icu.impl.CharacterIteration;

abstract class DictionaryBreakEngine implements LanguageBreakEngine {
    UnicodeSet fSet = new UnicodeSet();
    private final int fTypes;

    /**
     * @param breakTypes A mask of the break iterators that can use this engine.
     *  For example, (1 << KIND_WORD) | (1 << KIND_LINE) could be used by 
     *  word iterators and line iterators, but not any other kind.
     */
    public DictionaryBreakEngine(int breakTypes) {
        // TODO: consider using a java.util.BitSet with nbits <= 32
        fTypes = breakTypes;
    }

    public boolean handles(int c, int breakType) {
        return (breakType >= 0 && breakType < 32) &&   // breakType is in range
                ((1 << breakType) & fTypes) != 0 &&    // this type can use us
                fSet.contains(c);                      // we recognize the character
    }

    public int findBreaks(CharacterIterator text, int startPos, int endPos, 
            boolean reverse, int breakType, Deque<Integer> foundBreaks) {
         int result = 0;
       
         // Find the span of characters included in the set.
         //   The span to break begins at the current position int the text, and
         //   extends towards the start or end of the text, depending on 'reverse'.

        int start = text.getIndex();
        int current;
        int rangeStart;
        int rangeEnd;
        int c = CharacterIteration.current32(text);
        if (reverse) {
            boolean isDict = fSet.contains(c);
            while ((current = text.getIndex()) > startPos && isDict) {
                c = CharacterIteration.previous32(text);
                isDict = fSet.contains(c);
            }
            rangeStart = (current < startPos) ? startPos :
                                                current + (isDict ? 0 : 1);
            rangeEnd = start + 1;
        } else {
            while ((current = text.getIndex()) < endPos && fSet.contains(c)) {
                CharacterIteration.next32(text);
                c = CharacterIteration.current32(text);
            }
            rangeStart = start;
            rangeEnd = current;
        }

        // if (breakType >= 0 && breakType < 32 && (((uint32_t)1 << breakType) & fTypes)) {
        result = divideUpDictionaryRange(text, rangeStart, rangeEnd, foundBreaks);
        text.setIndex(current);

        return result;
    }
    
    void setCharacters(UnicodeSet set) {
        fSet = new UnicodeSet(set);
        fSet.compact();
    }

    /**
     * <p>Divide up a range of known dictionary characters handled by this break engine.</p>
     *
     * @param text A UText representing the text
     * @param rangeStart The start of the range of dictionary characters
     * @param rangeEnd The end of the range of dictionary characters
     * @param foundBreaks Output of break positions. Positions are pushed.
     *                    Pre-existing contents of the output stack are unaltered.
     * @return The number of breaks found
     */
     abstract int divideUpDictionaryRange(CharacterIterator text,
                                          int               rangeStart,
                                          int               rangeEnd,
                                          Deque<Integer>    foundBreaks );
}
