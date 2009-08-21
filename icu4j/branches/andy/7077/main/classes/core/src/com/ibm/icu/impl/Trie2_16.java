/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.Trie2.ValueWidth;


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
     * Create an empty Trie2_16.  Corresponds to utrie2_openDummy() in the C API.
     * 
     * The trie always returns the initialValue,
     * or the errorValue for out-of-range code points.
     *
     * @param initialValue  the initial value that is set for all code points.
     * @param errorValue the value for out-of-range code points.
     */
    public Trie2_16(int initialValue, int errorValue) { 
        super(null);   // TODO: implement this.
    }
    
    /**
     * Construct a Trie2_16 around an unserialized set of Trie2 data.
     * @internal
     */
    Trie2_16(UTrie2 data) {
        super(data);
    }
    
    
    /**
     * Get a value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    public final int get(int codePoint) {
        int value;
        int ix;
        
        if (codePoint > 0) {
            if (codePoint < 0x0d800 || (codePoint > 0x0dbff && codePoint <= 0x0ffff)) {
                // Ordinary BMP code point, excluding leading surrogates.
                // BMP uses a single level lookup.  BMP index starts at offset 0 in the trie index.
                // 16 bit data is stored in the index array itself.
                ix = trie.index[codePoint >> UTRIE2_SHIFT_2];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = trie.index[ix];
                return value;
            } 
            if (codePoint <= 0xffff) {
                // Lead Surrogate Code Point.  A Separate index section is stored for
                // lead surrogate code units and code points.
                //   The main index has the code unit data.
                //   For this function, we need the code point data.
                // Note: this expression could be refactored for slightly improved efficiency, but
                //       surrogate code points will be so rare in practice that it's not worth it.
                ix = trie.index[UTRIE2_LSCP_INDEX_2_OFFSET + ((codePoint - 0xd800) >> UTRIE2_SHIFT_2)];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = trie.index[ix];
                return value;
            }
            if (codePoint < trie.highStart) {
                // Supplemental code point, use two-level lookup.
                ix = (UTRIE2_INDEX_1_OFFSET - UTRIE2_OMITTED_BMP_INDEX_1_LENGTH) + (codePoint >> UTRIE2_SHIFT_1);
                ix = trie.index[ix];
                ix += (codePoint >> UTRIE2_SHIFT_2) & UTRIE2_INDEX_2_MASK;
                ix = trie.index[ix];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = trie.index[ix];
                return value;
            }
            if (codePoint <= 0x10ffff) {
                value = trie.index[trie.highValueIndex];
                return value;
            }
        }
        
        // Fall through.  The code point is outside of the legal range of 0..0x10ffff.
        return trie.errorValue;
    }

    
    /**
     * Get a 16-bit trie value from a UTF-16 single/lead code unit (<=U+ffff).
     * Same as get() if c is a BMP code point except for lead surrogates,
     * but faster.
     * 
     * @param trie the trie
     * @param c the code unit (0x0000 .. 0x0000ffff)
     * @return the value
     */
    int getFromU16SingleLead(int codePoint){
        int value;
        int ix;
        
        if (codePoint > 0) {
            if (codePoint < 0x0ffff) {
                // Ordinary BMP code point, including surrogates.
                // BMP uses a single level lookup.  BMP index starts at offset 0 in the trie index.
                // 16 bit data is stored in the index array itself.
                ix = trie.index[codePoint >> UTRIE2_SHIFT_2];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = trie.index[ix];
                return value;
            } 
            if (codePoint < trie.highStart) {
                // Supplemental code point, use two-level lookup.
                ix = (UTRIE2_INDEX_1_OFFSET - UTRIE2_OMITTED_BMP_INDEX_1_LENGTH) + (codePoint >> UTRIE2_SHIFT_1);
                ix = trie.index[ix];
                ix += (codePoint >> UTRIE2_SHIFT_2) & UTRIE2_INDEX_2_MASK;
                ix = trie.index[ix];
                ix = (ix << UTRIE2_INDEX_SHIFT) + (codePoint & UTRIE2_DATA_MASK);
                value = trie.index[ix];
                return value;
            }
            if (codePoint <= 0x10ffff) {
                value = trie.index[trie.highValueIndex];
                return value;
            }
        }
        
        // Fall through.  The code point is outside of the legal range of 0..0x10ffff.
        return trie.errorValue;
       
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
