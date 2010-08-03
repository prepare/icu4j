/*
******************************************************************************
* Copyright (C) 1996-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.lang;

import java.util.Iterator;

import com.ibm.icu.impl.Trie2;
import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.util.RangeValueIterator;

/**
 * Class enabling iteration of the codepoints according to their types.
 * Result of each iteration contains the interval of codepoints that have
 * the same type.<br>
 * Not intended for public subclassing.
 * Example of use:<br>
 * <pre>
 * RangeValueIterator iterator = UCharacter.getTypeIterator();
 * RangeValueIterator.Element element = new RangeValueIterator.Element();
 * while (iterator.next(element)) {
 *     System.out.println("Codepoint \\u" + 
 *                        Integer.toHexString(element.start) + 
 *                        " to codepoint \\u" +
 *                        Integer.toHexString(element.limit - 1) + 
 *                        " has the character type " + 
 *                        element.value);
 * }
 * </pre>
 * @author synwee
 * @see com.ibm.icu.util.RangeValueIterator
 * @since release 2.1, Jan 24 2002
 */
class UCharacterTypeIterator implements RangeValueIterator {
    // Only constructed by UCharacter.getTypeIterator().
    UCharacterTypeIterator() {
        reset();
    }

    // implements RangeValueIterator
    public boolean next(Element element) {
        if(trieIterator.hasNext() && !(range=trieIterator.next()).leadSurrogate) {
            element.start=range.startCodePoint;
            element.limit=range.endCodePoint+1;
            element.value=range.value;
            return true;
        } else {
            return false;
        }
    }

    // implements RangeValueIterator
    public void reset() {
        trieIterator=UCharacterProperty.INSTANCE.m_trie_.iterator(MASK_TYPE);
    }

    private Iterator<Trie2.Range> trieIterator;
    private Trie2.Range range;

    private static final class MaskType implements Trie2.ValueMapper {
        // Extracts the general category ("character type") from the trie value.
        public int map(int value) {
            return value & UCharacterProperty.TYPE_MASK;
        }
    }
    private static final MaskType MASK_TYPE=new MaskType();
}
