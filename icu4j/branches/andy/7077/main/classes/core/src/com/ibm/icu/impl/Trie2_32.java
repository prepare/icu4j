/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.Trie2.UTrie2;

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
        super(null);   // TODO: implement this.
    }
    
    /**
     * Construct a Trie2_32 around an unserialized set of Trie2 data.
     * @internal
     */
    Trie2_32(UTrie2 data) {
        super(data);
    }
    
    

}
