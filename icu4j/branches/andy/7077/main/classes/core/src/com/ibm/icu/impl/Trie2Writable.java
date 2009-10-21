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
 * A Trie2Writable is a modifiable, or build-time Trie2.
 * Functions for reading data from the Trie are all from class Trie2.
 * 
 */
public class Trie2Writable extends Trie2 {
    
    
    /**
     * Create a new, empty, writable Trie2. At build time, 32-bit data values are used.
     *
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     */
    public  Trie2Writable(int initialValue, int errorValue) {
        // TODO: implement this.
        
        // This constructor corresponds to utrie2_open() in ICU4C.
    }
    
    
    /**
     * Create a new build time (modifiable) Trie2 whose contents are the same as the source Trie.
     * 
     * @param source
     */
    public Trie2Writable(Trie2 source) {
        // TODO: implement this.
    }
        
    
    /**
     * Set a value for a code point.
     *
     * @param c the code point
     * @param value the value
     */
    public Trie2Writable set(int c, int value) {
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
     public Trie2Writable setRange(int start, int end,
                           int value, boolean overwrite) {
    
        return this;
    }
     
     
     /**
      * Set a value for a UTF-16 code unit.
      * Note that a Trie2 stores separate values for 
      * supplementary code points in the lead surrogate range
      * (accessed via the plain set() and get() interfaces)
      * and for lead surrogate code units.
      * 
      * The lead surrogate code unit values are set via this function and
      * read by the function getFromU16SingleLead().
      * 
      * For code units outside of the lead surrogate range, this function
      * behaves identically to set().
      *
      * @param lead A UTF-16 code unit. 
      * @param value the value
      */
     public Trie2Writable setForLeadSurrogateCodeUnit(char codeUnit, int value) {
         return this;
     }

     
     /**
      * Produce an optimized, read-only Trie2_16 from the Trie being built.
      * The data values must all fit as an unsigned 16 bit value.
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


     /**
      * Get the value for a code point as stored in the trie.
      *
      * @param trie the trie
      * @param codePoint the code point
      * @return the value
      */
    @Override
    public int get(int codePoint) {
        // TODO Auto-generated method stub
        return 0;
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
    @Override
    public int getFromU16SingleLead(char c) {
        // TODO Auto-generated method stub
        return 0;
    }
      
    /* Start with allocation of 16k data entries. */
    private static int UNEWTRIE2_INITIAL_DATA_LENGTH = 1<<14;

    /* Grow about 8x each time. */
    private static int UNEWTRIE2_MEDIUM_DATA_LENGTH = 1<<17;

    // Private data members.  From struct UNewTrie2 in ICU4C
    //
    private  int[]   index1 = new int[UNEWTRIE2_INDEX_1_LENGTH];
    private  int[]   index2 = new int[UNEWTRIE2_MAX_INDEX_2_LENGTH];
    private  int[]   data   = new int[UNEWTRIE2_INITIAL_DATA_LENGTH];

    private  int     initialValue, errorValue;
    private  int     index2Length, dataCapacity, dataLength;
    private  int     firstFreeBlock;
    private  int     index2NullOffset, dataNullOffset;
    private  int     highStart;
    private  boolean isCompacted;

    /*
     * Multi-purpose per-data-block table.
     *
     * Before compacting:
     *
     * Per-data-block reference counters/free-block list.
     *  0: unused
     * >0: reference counter (number of index-2 entries pointing here)
     * <0: next free data block in free-block list
     *
     * While compacting:
     *
     * Map of adjusted indexes, used in compactData() and compactIndex2().
     * Maps from original indexes to new ones.
     */
     private  int[]   map = new int[UNEWTRIE2_MAX_DATA_LENGTH>>UTRIE2_SHIFT_2];

}
