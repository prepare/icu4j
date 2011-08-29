/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

/**
 * 
 * CollationElementBuffer - A circular buffer of CEs from the text being searched.
 * 
 * @author dbesevic
 *
 */

public class CollationElementBuffer 
{
    protected static final int    DEFAULT_CEBUFFER_SIZE = 96;
    protected static final int    CEBUFFER_EXTRA = 32;
    // Some typical max values to make buffer size more reasonable for asymmetric search.
    // #8694 is for a better long-term solution to allocation of this buffer.
    protected static final int    MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L = 8;
    protected static final int    MAX_TARGET_IGNORABLES_PER_PAT_OTHER = 3;
    //#define   MIGHT_BE_JAMO_L(c) ((c >= 0x1100 && c <= 0x115E) || (c >= 0x3131 && c <= 0x314E) || (c >= 0x3165 && c <= 0x3186))

    CEI buf[] = new CEI[DEFAULT_CEBUFFER_SIZE];
    int bufSize;
    int firstIx;
    int limitIx;
    CollationElementIterator ceIter;
    
    public CollationElementBuffer(StringSearch ss)
    {
        
    }
    
    public CEI get (int index)
    {
       return null; 
    }
    
    public CEI getPrevious (int index)
    {
       return null; 
    }
    
    public class CEI {
        long ce;
        int lowIndex;
        int highIndex;    
    }
}
