/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.ibm.icu.impl.Trie.DataManipulate;

/**
 * @author aheninger
 *
 * This is a common implementation of a Unicode trie.
 * It is a kind of compressed, serializable table of 16- or 32-bit values associated with
 * Unicode code points (0..0x10ffff). (A map from code points to integers.)
 *
 * This is the second common version of a Unicode trie (hence the name Trie2).
 */
public class Trie2 {

   /**
    * Selectors for the width of a UTrie2 data value.
    */   
    public enum Trie2ValueBits {
        UTRIE2_16_VALUE_BITS,
        UTRIE2_32_VALUE_BITS
    }
    
    /**
     * When iterating over the contents of a Trie2, Elements fo this type are produced.
     *
     */
    public class Trie2EnumRange {
        public int   startCodePoint;
        public int   endCodePoint;     // Inclusive.
        public int   value;
    }
    
    
    /**
     * Create a frozen trie from its serialized form.
     * Inverse of utrie2_serialize().
     *
     * @param valueBits selects the data entry size; results in an
     *                  TODO:<which> exception if it does not match the serialized form
     * @param data an input stream to the serialized form of a UTrie2.  
     * @param pActualLength receives the actual number of bytes at data taken up by the trie data;
     *                      can be NULL
     * @param pErrorCode an in/out ICU UErrorCode
     * @return the unserialized trie
     *
     * @see utrie2_open
     * @see utrie2_serialize
     */
    public static Trie2  createFromSerialized(Trie2ValueBits valueBits, 
                                InputStream data) 
        throws IOException {
        return null;
    }
    
    
    /**
     * Create a frozen, empty "dummy" trie.
     * A dummy trie is an empty trie, used when a real data trie cannot
     * be loaded. Equivalent to calling utrie2_open() and utrie2_freeze(),
     * but without internally creating and compacting/serializing the
     * builder data structure.
     *
     * The trie always returns the initialValue,
     * or the errorValue for out-of-range code points and illegal UTF-8.
     *
     * @param valueBits selects the data entry size
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @param pErrorCode an in/out ICU UErrorCode
     * @return the dummy trie
     *
     * @see utrie2_openFromSerialized
     * @see utrie2_open
     */
    public static Trie2 createDummy(Trie2ValueBits valueBits,
                                    int initialValue, 
                                    int errorValue) {
        return null;
    }
    /**
     * Get a value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    public int get(int codePoint) {
        return 0;
    }

    
    /**
     *  Return an iterator over the value ranges in this Trie2.
     *  TODO:  this will create a lot of little Trie2EnumRange objects.
     *         We could provide a variant with a value object that is
     *            reused, filling in new values on each call to the Iterator object.
     *  Note:  doesn't provide the value remapping function that ICU4C has.  But this
     *         is a one-liner to add by the caller; can we just omit it.
     * @return
     */
    public Iterator<Trie2EnumRange> iterator() {
        return null;
    }
    
    /* Building a trie ---------------------------------------------------------- */

    
    
    /**
     * Open an empty, writable Trie2. At build time, 32-bit data values are used.
     * utrie2_freeze() takes a valueBits parameter
     * which determines the data value width in the serialized and frozen forms.
     *
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @param pErrorCode an in/out ICU UErrorCode
     * @return a pointer to the allocated and initialized new trie
     */
    public static Trie2 Create(int initialValue, int errorValue) {
        return null;
    }

    

}
