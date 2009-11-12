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
 * A frozen (read-only) Trie2, holding 32 bit data values.
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

public class Trie2_32 extends Trie2 {
    
    /**
     * Create an empty Trie2_32.  Corresponds to utrie2_openDummy() in the C API.
     * 
     * The trie always returns the initialValue,
     * or the errorValue for out-of-range code points.
     *
     * @param initialValue  the initial value that is set for all code points.
     * @param errorValue the value for out-of-range code points.
     */
    public Trie2_32(int initialValue, int errorValue) { 
        // TODO: implement this.
        // TODO: I think that we should drop this from the API.  I don't see the purpose.
    }
    
    
    /**
     * Internal constructor, not for general use.
     */
    Trie2_32() {
    }
    
    
    /**
     * Get the value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    public final int get(int codePoint) {
        int value;
        int ix;
        
        if (codePoint >= 0) {
            if (codePoint < 0x0d800 || (codePoint > 0x0dbff && codePoint <= 0x0ffff)) {
                // Ordinary BMP code point, excluding leading surrogates.
                // BMP uses a single level lookup.  BMP index starts at offset 0 in the trie index.
                // 32 bit data is stored in the index array itself.
                ix = index[codePoint >> UTRIE2_SHIFT_2];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = data32[ix];
                return value;
            } 
            if (codePoint <= 0xffff) {
                // Lead Surrogate Code Point.  A Separate index section is stored for
                // lead surrogate code units and code points.
                //   The main index has the code unit data.
                //   For this function, we need the code point data.
                // Note: this expression could be refactored for slightly improved efficiency, but
                //       surrogate code points will be so rare in practice that it's not worth it.
                ix = index[UTRIE2_LSCP_INDEX_2_OFFSET + ((codePoint - 0xd800) >> UTRIE2_SHIFT_2)];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = data32[ix];
                return value;
            }
            if (codePoint < highStart) {
                // Supplemental code point, use two-level lookup.
                ix = (UTRIE2_INDEX_1_OFFSET - UTRIE2_OMITTED_BMP_INDEX_1_LENGTH) + (codePoint >> UTRIE2_SHIFT_1);
                ix = index[ix];
                ix += (codePoint >> UTRIE2_SHIFT_2) & UTRIE2_INDEX_2_MASK;
                ix = index[ix];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = data32[ix];
                return value;
            }
            if (codePoint <= 0x10ffff) {
                value = data32[highValueIndex];
                return value;
            }
        }
        
        // Fall through.  The code point is outside of the legal range of 0..0x10ffff.
        return errorValue;
    }

    
    /**
     * Get a trie value for a UTF-16 code unit.
     * 
     * This function returns the same value as get() if the input 
     * character is outside of the lead surrogate range
     * 
     * There are two values stored in a Trie for inputs in the lead
     * surrogate range.  This function returns the alternate value,
     * while Trie2.get() returns the main value.
     * 
     * @param trie the trie
     * @param c the code point or lead surrogate value.
     * @return the value
     */
    public int getFromU16SingleLead(char codePoint){
        int value;
        int ix;
        
        ix = index[codePoint >> UTRIE2_SHIFT_2];
        ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
        value = data32[ix];
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

        
        public Trie2.CharSequenceValues next() {
            int c = Character.codePointAt(text, index);
            int val = get(c);

            fResults.index = index;
            fResults.codePoint = c;
            fResults.value = val;
            index++;
            if (c >= 0x10000) {
                index++;
            }            
            return fResults;
        }

        
        public Trie2.CharSequenceValues previous() {
            int c = Character.codePointBefore(text, index);
            int val = get(c);
            index--;
            if (c >= 0x10000) {
                index--;
            }
            fResults.index = index;
            fResults.codePoint = c;
            fResults.value = val;
            return fResults;
        }
   }

    public CharSequenceIterator iterator(CharSequence text, int index) {
        return new CharSequenceIterator(text, index);
    }
        

    

}
