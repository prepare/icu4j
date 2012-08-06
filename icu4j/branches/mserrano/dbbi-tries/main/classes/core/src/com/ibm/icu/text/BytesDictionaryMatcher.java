/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.CharacterIterator;

import com.ibm.icu.impl.Assert;
import com.ibm.icu.util.BytesTrie;
import com.ibm.icu.util.BytesTrie.Result;

class BytesDictionaryMatcher extends DictionaryMatcher {
    private final byte[] characters;
    private final int transform;
    
    public BytesDictionaryMatcher(byte[] chars, int transform) {
        characters = chars;
        Assert.assrt((transform & DictionaryData.TRANSFORM_TYPE_MASK) == DictionaryData.TRANSFORM_TYPE_OFFSET);
        this.transform = transform;
    }
    
    private int transform(int c) {
        if (c == 0x200D) { 
            return 0xFF;
        } else if (c == 0x200C) {
            return 0xFE;
        }

        int delta = ((int)c) - (transform & DictionaryData.TRANSFORM_OFFSET_MASK);
        if (delta < 0 || 0xFD < delta) {
            return -1;
        }
        return delta;
    }

    public int matches(CharacterIterator text_, int maxLength, int[] lengths, int[] count_, int limit, int[] values) {
        UCharacterIterator text = UCharacterIterator.getInstance(text_);
        BytesTrie bt = new BytesTrie(characters, 0);
        int c = text.next();
        Result result = bt.first(transform(c));
        int numChars = 1;
        int count = 0;
        for (;;) {
            if (result.hasValue()) {
                if (count < limit) {
                    if (values != null) {
                        values[count] = bt.getValue();
                    }
                    lengths[count] = numChars;
                    count++;
                }
                if (result == Result.FINAL_VALUE) {
                    break;
                }
            } else if (result == Result.NO_MATCH) {
                break;
            }

            if (numChars >= maxLength) {
                break;
            }

            c = text.next();
            ++numChars;
            result = bt.next(transform(c));
        }
        count_[0] = count;
        return numChars;
    }

    public int getType() {
        return DictionaryData.TRIE_TYPE_BYTES;
    }
}


