/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt.handlers;

import com.ibm.icu.impl.stt.CharTypes;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.Offsets;

/**
 * A base handler for structured text composed of two parts separated by a
 * separator. The first occurrence of the separator delimits the end of the
 * first part and the start of the second part. Further occurrences of the
 * separator, if any, are treated like regular characters of the second text
 * part. The handler makes sure that the text be presented in the form (assuming
 * that the equal sign is the separator):
 * 
 * <pre>
 * part1 = part2
 * </pre>
 * 
 * The string returned by {@link TypeHandler#getSeparators getSeparators} for
 * this handler should contain exactly one character. Additional characters will
 * be ignored.
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public class Single extends TypeHandler {

    public Single(String separator) {
        super(separator);
    }

    /**
     * Locates occurrences of the separator.
     * 
     * @see #getSeparators getSeparators
     */
    public int indexOfSpecial(Expert expert, String text, CharTypes charTypes,
            Offsets offsets, int caseNumber, int fromIndex) {
        return text.indexOf(this.getSeparators(expert).charAt(0), fromIndex);
    }

    /**
     * Inserts a mark before the separator if needed and skips to the end of the
     * source string.
     * 
     * @return the length of <code>text</code>.
     */
    public int processSpecial(Expert expert, String text, CharTypes charTypes,
            Offsets offsets, int caseNumber, int separLocation) {
        TypeHandler.processSeparator(text, charTypes, offsets, separLocation);
        return text.length();
    }

    /**
     * Returns 1 as number of special cases handled by this handler.
     * 
     * @return 1.
     */
    public int getSpecialsCount() {
        return 1;
    }
}