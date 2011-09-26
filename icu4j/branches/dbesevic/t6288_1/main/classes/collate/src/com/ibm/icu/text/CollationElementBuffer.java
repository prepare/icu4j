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
    
    public CollationElementBuffer()
    {
        
    }
    
    public CollationElementBuffer(StringSearch ss, CollationElementIterator iter)
    {
        bufSize = ss.getPCELength() + CEBUFFER_EXTRA;
        
        ceIter  = iter;
        firstIx = 0;
        limitIx = 0;

        ceIter.initPCE();
    }
    
// Get the CE with the specified index.
//  Index must be in the range
//         n-history_size < index < n+1
//  where n is the largest index to have been fetched by some previous call to this function.
//  The CE value will be UCOL__PROCESSED_NULLORDER at end of input.
//
    public CEI get (int index)
    {
        int i = index % bufSize;

        if (index >= firstIx && index < limitIx) {
            // The request was for an entry already in our buffer.
            //  Just return it.
            return buf[i];
        }

        // Caller is requesting a new, never accessed before, CE.
        //   Verify that it is the next one in sequence, which is all
        //   that is allowed.
        if (index != limitIx) {
            return null;
        }

        // Manage the circular CE buffer indexing
        limitIx++;

        if (limitIx - firstIx >= bufSize) {
            // The buffer is full, knock out the lowest-indexed entry.
            firstIx++;
        }

        buf[i] = ceIter.nextProcessed();

        return buf[i];
    }
    
// Get the CE with the specified index.
//  Index must be in the range
//         n-history_size < index < n+1
//  where n is the largest index to have been fetched by some previous call to this function.
//  The CE value will be UCOL__PROCESSED_NULLORDER at end of input.
//
    public CEI getPrevious (int index)
    {
        int i = index % bufSize;

        if (index>=firstIx && index<limitIx) {
            // The request was for an entry already in our buffer.
            //  Just return it.
            return buf[i];
        }

        // Caller is requesting a new, never accessed before, CE.
        //   Verify that it is the next one in sequence, which is all
        //   that is allowed.
        if (index != limitIx) {
           return null;
        }

        // Manage the circular CE buffer indexing
        limitIx++;

        if (limitIx - firstIx >= bufSize) {
            // The buffer is full, knock out the lowest-indexed entry.
            firstIx++;
        }

        buf[i] = ceIter.previousProcessed();

        return buf[i];
    } 
   
    public CEI createCEI()
    {
        return new CEI();
    }
    
    public class CEI {
        long ce;
        int lowIndex;
        int highIndex; 
        
        public CEI ()
        {
            ce = 0;
            lowIndex = 0;
            highIndex = 0;
        }
        
        public int getLow() { return lowIndex; }
        public int getHigh() { return highIndex; }
        public long getCE() { return ce; }
    }


}
