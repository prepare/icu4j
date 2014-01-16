/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt;

import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Provides various services related to managing the array of offsets where
 * directional formatting characters should be inserted in a source string.
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 * 
 */
public class Offsets {

    private static final int OFFSET_SIZE = 20;

    private int[] offsets = new int[OFFSET_SIZE];
    private int count; // number of used entries
    private BidiStructuredProcessor.Orientation direction = null; // STT direction
    private int prefixLength;

    /**
     * Default constructor
     */
    public Offsets() {
    }

    /**
     * @return the stored prefix length
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * Stores the prefix length
     * 
     * @param prefLen
     *            value assigned to the prefix length
     */
    public void setPrefixLength(int prefLen) {
        prefixLength = prefLen;
    }

    /**
     * Gets the number of used entries in the offsets array.
     * 
     * @return the number of used entries in the offsets array.
     */
    public int getCount() {
        return count;
    }

    /**
     * Marks that all entries in the offsets array are unused.
     */
    public void clear() {
        count = 0;
    }

    /**
     * Gets the value of a specified entry in the offsets array.
     * 
     * @param index
     *            the index of the entry of interest.
     * 
     * @return the value of the specified entry.
     */
    public int getOffset(int index) {
        return offsets[index];
    }

    /**
     * Inserts an offset value in the offset array so that the array stays in
     * ascending order.
     * 
     * @param charTypes
     *            an object whose methods can be useful to the handler.
     * 
     * @param offset
     *            the value to insert.
     */
    public void insertOffset(CharTypes charTypes, int offset) {
        if (count >= offsets.length) {
            int[] newOffsets = new int[offsets.length * 2];
            System.arraycopy(offsets, 0, newOffsets, 0, count);
            offsets = newOffsets;
        }
        int index = count - 1; // index of greatest member <= offset
        // look up after which member the new offset should be inserted
        while (index >= 0) {
            int wrkOffset = offsets[index];
            if (offset > wrkOffset)
                break;
            if (offset == wrkOffset)
                return; // avoid duplicates
            index--;
        }
        index++; // index now points at where to insert
        int length = count - index; // number of members to move up
        if (length > 0) // shift right all members greater than offset
            System.arraycopy(offsets, index, offsets, index + 1, length);
        offsets[index] = offset;
        count++; // number of used entries
        // if the offset is 0, adding a mark does not change anything
        if (offset < 1)
            return;
        if (charTypes == null)
            return;

        byte charType = charTypes.getBidiTypeAt(offset);
        // if the current char is a strong one or a digit, we change the
        // charType of the previous char to account for the inserted mark.
        if (CharTypes.isStrongOrDigit(charType))
            index = offset - 1;
        else
            // if the current char is a neutral, we change its own charType
            index = offset;

        charTypes.setBidiTypeAt(index,
                charTypes.charTypeForDirection(direction));
    }

    /**
     * Gets all and only the used offset entries.
     * 
     * @return the current used entries of the offsets array.
     */
    public int[] getOffsets() {
        if (count == offsets.length)
            return offsets;
        int[] array = new int[count];
        System.arraycopy(offsets, 0, array, 0, count);
        return array;
    }

}
