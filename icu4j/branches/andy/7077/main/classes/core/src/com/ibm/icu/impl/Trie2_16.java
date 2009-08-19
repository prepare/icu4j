/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;


/**
 * @author aheninger
 * 
 * A frozen (read-only) Trie2, holding 16 bit data values.
 * 
 * A Trie2 is a highly optimized data structure for mapping from Unicode
 * code points (values ranging from 0 to 0x10ffff) to a 16 or 32 bit value.
 *
 * See class Trie2 for descriptions of the API for accessing the contents of a trie.
 * 
 * The fundamental data access methods are declared final in this class, with
 * the intent that applications might gain a little extra performance, when compared
 * with calling the same methods via the abstract UTrie2 base class.
 */
public final class Trie2_16 extends Trie2 {
    
    /**
     * Get a value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    public final int get(int codePoint) {
        //#define UTRIE2_GET16(trie, c) _UTRIE2_GET((trie), index, (trie)->indexLength, (c))
        
        // #define _UTRIE2_GET(trie, data, asciiOffset, c) \
        //    (trie)->data[_UTRIE2_INDEX_FROM_CP(trie, asciiOffset, c)]
        //    (trie)->index[_UTRIE2_INDEX_FROM_CP(trie, (trie)->indexLength, c)]
        
        //#define _UTRIE2_INDEX_FROM_CP(trie, asciiOffset, c) \
       // ((uint32_t)(c)<0xd800 ? \
       //     _UTRIE2_INDEX_RAW(0, (trie)->index, c) : \
        //    (uint32_t)(c)<=0xffff ? \
        //        _UTRIE2_INDEX_RAW( \
        //            (c)<=0xdbff ? UTRIE2_LSCP_INDEX_2_OFFSET-(0xd800>>UTRIE2_SHIFT_2) : 0, \
        //            (trie)->index, c) : \
        //        (uint32_t)(c)>0x10ffff ? \
        //            (asciiOffset)+UTRIE2_BAD_UTF8_DATA_OFFSET : \
        //            (c)>=(trie)->highStart ? \
        //                (trie)->highValueIndex : \
        //                _UTRIE2_INDEX_FROM_SUPP((trie)->index, c))
        
        //  #define _UTRIE2_INDEX_RAW(offset, trieIndex, c) \
        //  (((int32_t)((trieIndex)[(offset)+((c)>>UTRIE2_SHIFT_2)]) \
        //  <<UTRIE2_INDEX_SHIFT)+ \
        //  ((c)&UTRIE2_DATA_MASK))

        
        int value = 0;
        
        if (codePoint < 0x0d800) {
            int index = trie.index1[codePoint >> UTRIE2_SHIFT_2];
            index = (index << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
            value = trie.data16[index];
        } else if (codePoint <= 0xffff) {
            value = 666;   // TODO
        }

        return value;
    }

    
    /**
     * Iterator class that will produce the values from the Trie for
     *  the sequence of code points in an input text.
     *
     *  This class is functionally identical to Trie2.CharSequenceIterator.
     *  Direct use of this class (Trie2_16.CharSequenceIterator) will be 
     *  slightly faster access via the base class. 
     */
    public final class CharSequenceIterator extends Trie2.CharSequenceIterator {
        CharSequenceIterator(CharSequence text, int index) {
            super(text, index);
        }

        
        public Trie2.IterationResults next() {
            int c = Character.codePointAt(text, index);
            int val = get(c);

            fResults.index = index;
            fResults.c = c;
            fResults.val = val;
            index++;
            if (c >= 0x10000) {
                index++;
            }            
            return fResults;
        }

        
        public Trie2.IterationResults previous() {
            int c = Character.codePointBefore(text, index);
            int val = get(c);
            index--;
            if (c >= 0x10000) {
                index--;
            }
            fResults.index = index;
            fResults.c = c;
            fResults.val = val;
            return fResults;
        }
   }

    public CharSequenceIterator iterator(CharSequence text, int index) {
        return new CharSequenceIterator(text, index);
    }
        
    

}
