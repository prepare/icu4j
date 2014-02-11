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

// TODO: There must be a Java class for a growable array of ints without auto-boxing to Integer?!
// Keep the API parallel to the C++ version for ease of porting. Port methods only as needed.
// If & when we start using something else, we might keep this as a thin wrapper for porting.
public final class UVector32 {
    public UVector32() {}
    public int size() { return length; }
    public int elementAti(int i) { return buffer[i]; }
    public int[] getBuffer() { return buffer; }
    public void addElement(int e) {
        if(length >= buffer.length) {
            int newCapacity = buffer.length <= 0xffff ? 4 * buffer.length : 2 * buffer.length;
            int[] newBuffer = new int[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }
        buffer[length++] = e;
    }
    public void setElementAt(int elem, int index) { buffer[index] = elem; }
    public void removeAllElements() {
        length = 0;
    }
    private int[] buffer = new int[32];
    private int length = 0;
}
