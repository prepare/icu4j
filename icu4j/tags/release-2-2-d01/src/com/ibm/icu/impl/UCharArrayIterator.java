/**
*******************************************************************************
* Copyright (C) 1996-2001, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/impl/UCharArrayIterator.java,v $ 
* $Date: 2002/06/20 01:18:09 $ 
* $Revision: 1.1 $
*
*******************************************************************************
*/

package com.ibm.icu.impl;


/**
 * @author Doug Felt
 *
 */

public final class UCharArrayIterator extends UCharacterIterator {
    private final char[] text;
    private final int start;
    private final int limit;
    private int pos;

    public UCharArrayIterator(char[] text, int start, int limit) {
        if (start < 0 || limit > text.length || start > limit) {
            throw new IllegalArgumentException("start: " + start + " or limit: "
                                               + limit + " out of range [0, " 
                                               + text.length + ")");
        }
        this.text = text;
        this.start = start;
        this.limit = limit;

        this.pos = start;
    }

    public int current() {
        return pos < limit ? text[pos] : DONE;
    }

    public int getLength() {
        return limit - start;
    }

    public int getIndex() {
        return pos - start;
    }

    public int next() {
        return pos < limit ? text[pos++] : DONE;
    }

    public int previous() {
        return pos > start ? text[--pos] : DONE;
    }

    public void setIndex(int index) {
        if (index < 0 || index > limit - start) {
            throw new IndexOutOfBoundsException("index: " + index + 
                                                " out of range [0, " 
                                                + (limit - start) + ")");
        }
        pos = start + index;
    }

    public int getText(char[] fillIn, int offset) {
        int len = limit - start;
        System.arraycopy(text, start, fillIn, offset, len);
        return len;
    }

    public String getString() {
        return new String(text, start, limit - start);
    }
    /**
     * Creates a copy of this iterator, does not clone the underlying 
     * <code>Replaceable</code>object
     * @return copy of this iterator
     */
    public Object clone(){
        try {
          return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // never invoked
        }
    }
}