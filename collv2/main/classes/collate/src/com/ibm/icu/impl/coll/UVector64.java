/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 *
 * @since 2014feb10
 * @author Markus W. Scherer
 */
package com.ibm.icu.impl.coll;

// TODO: There must be a Java class for a growable array of longs without auto-boxing to Long?!
// Keep the API parallel to the C++ version for ease of porting. Port methods only as needed.
// If & when we start using something else, we might keep this as a thin wrapper for porting.
public final class UVector64 {
    public UVector64() {}
    public int size() { return length; }
    public long elementAti(int i) { return buffer[i]; }
    public long[] getBuffer() { return buffer; }
    public void addElement(long e) {
        if(length >= buffer.length) {
            int newCapacity = buffer.length <= 0xffff ? 4 * buffer.length : 2 * buffer.length;
            long[] newBuffer = new long[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }
        buffer[length++] = e;
    }
    public void setElementAt(long elem, int index) { buffer[index] = elem; }
    public void removeAllElements() {
        length = 0;
    }
    public boolean isEmpty() { return length == 0; }
    private long[] buffer = new long[32];
    private int length = 0;
}
