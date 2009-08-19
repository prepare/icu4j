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
 * A Trie2Builder is a modifiable, or build-time Trie2.
 * Functions for reading data from the Trie are all from class Trie2.
 * 
 */
public class Trie2Builder extends Trie2 {
    
    
    /**
     * Create a new, empty, writable Trie2. At build time, 32-bit data values are used.
     *
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     */
    public  Trie2Builder(int initialValue, int errorValue) {
    }
    
    
    /**
     * Create a new build time (modifiable) Trie2 whose contents are the same as the source Trie.
     * 
     * @param source
     */
    public Trie2Builder(Trie2 source) {
    }
    
    
    /**
     * Set a value for a code point.
     *
     * @param c the code point
     * @param value the value
     */
    public Trie2Builder set(int c, int value) {
        return this;
    }
    
    /**
     * Set a value in a range of code points [start..end].
     * All code points c with start<=c<=end will get the value if
     * overwrite is TRUE or if the old value is the initial value.
     * Throws UnsupportedOperationException if the Trie2 is frozen.
     *
     * @param start the first code point to get the value
     * @param end the last code point to get the value (inclusive)
     * @param value the value
     * @param overwrite flag for whether old non-initial values are to be overwritten
     */
     public Trie2Builder setRange(int start, int end,
                           int value, boolean overwrite) {
    
        return this;
    }
     
     
     /**
      * Set a value for a lead surrogate code unit.
      * Note that a Trie2 stores separate values for 
      * supplementary code points (via the plain get() and set() interfaces)
      * and lead surrogates, via setForLeadSurrogateCodeUnit() and
      * getFromU16SingleLead()
      *
      * @param lead the lead surrogate code unit (U+D800..U+DBFF)
      * @param value the value
      */
     public Trie2Builder setForLeadSurrogateCodeUnit(int lead, int value) {
         return this;
     }

     
     /**
      * Produce an optimized, read-only Trie2_16 from the Trie being built.
      * The data values must all fit in 16 bits.
      * 
      */
     public Trie2_16 getAsFrozen_16() {
         return null;
     }
      

     /**
      * Produce an optimized, read-only Trie2_32 from the Trie being built.
      * 
      */
     public Trie2_32 getAsFrozen_32() {
         return null;
     }
      

}
