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
 * Handler for structured text composed of SQL statements. Such a structured
 * text may span multiple lines.
 * <p>
 * In applications like an editor where parts of the text might be modified
 * while other parts are not, the user may want to call
 * {@link Expert#leanToFullText} separately on each line and save the initial
 * state of each line (this is the final state of the previous line which can be
 * retrieved by calling {@link Expert#getState()}. If both the content of a line
 * and its initial state have not changed, the user can be sure that the last
 * <i>full</i> text computed for this line has not changed either.
 * 
 * @see Expert explanation of state
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public class Sql extends TypeHandler {
    static final String lineSep = System.getProperty("line.separator");
    private static final Integer STATE_LITERAL = new Integer(2);
    private static final Integer STATE_SLASH_ASTER_COMMENT = new Integer(4);

    public Sql() {
        super("\t!#%&()*+,-./:;<=>?|[]{}");
    }

    /**
     * @return 5 as the number of special cases handled by this handler.
     */
    public int getSpecialsCount() {
        return 5;
    }

    /**
     * Locates occurrences of 5 special strings:
     * <ol>
     * <li>spaces</li>
     * <li>literals starting with apostrophe</li>
     * <li>identifiers starting with quotation mark</li>
     * <li>comments starting with slash-asterisk</li>
     * <li>comments starting with hyphen-hyphen</li>
     * </ol>
     */
    public int indexOfSpecial(Expert expert, String text, CharTypes charTypes,
            Offsets offsets, int caseNumber, int fromIndex) {
        switch (caseNumber) {
        case 1: /* space */
            return text.indexOf(" ", fromIndex);
        case 2: /* literal */
            return text.indexOf('\'', fromIndex);
        case 3: /* delimited identifier */
            return text.indexOf('"', fromIndex);
        case 4: /* slash-aster comment */
            return text.indexOf("/*", fromIndex);
        case 5: /* hyphen-hyphen comment */
            return text.indexOf("--", fromIndex);
        }
        // we should never get here
        return -1;
    }

    /**
     * Processes the 5 special cases as follows.
     * <ol>
     * <li>skip the run of spaces</li>
     * <li>look for a matching apostrophe and skip until after it</li>
     * <li>look for a matching quotation mark and skip until after it</li>
     * <li>skip until after the closing asterisk-slash</li>
     * <li>skip until after a line separator</li>
     * </ol>
     */
    public int processSpecial(Expert expert, String text, BidiTransformStateImpl state, CharTypes charTypes,
            Offsets offsets, int caseNumber, int separLocation) {
        int location;

        TypeHandler.processSeparator(text, charTypes, offsets, separLocation);
        if (separLocation < 0) {
            caseNumber = ((Integer) state.getState()).intValue(); // TBD
                                                                    // guard
                                                                    // against
                                                                    // "undefined"
            state.clear();
        }
        switch (caseNumber) {
        case 1: /* space */
            separLocation++;
            while (separLocation < text.length()
                    && text.charAt(separLocation) == ' ') {
                charTypes.setBidiTypeAt(separLocation, CharTypes.WS);
                separLocation++;
            }
            return separLocation;
        case 2: /* literal */
            location = separLocation + 1;
            while (true) {
                location = text.indexOf('\'', location);
                if (location < 0) {
                    state.setState(STATE_LITERAL);
                    return text.length();
                }
                if ((location + 1) < text.length()
                        && text.charAt(location + 1) == '\'') {
                    location += 2;
                    continue;
                }
                return location + 1;
            }
        case 3: /* delimited identifier */
            location = separLocation + 1;
            while (true) {
                location = text.indexOf('"', location);
                if (location < 0)
                    return text.length();

                if ((location + 1) < text.length()
                        && text.charAt(location + 1) == '"') {
                    location += 2;
                    continue;
                }
                return location + 1;
            }
        case 4: /* slash-aster comment */
            if (separLocation < 0) // continuation line
                location = 0;
            else
                location = separLocation + 2; // skip the opening
                                                // slash-aster
            location = text.indexOf("*/", location);
            if (location < 0) {
                state.setState(STATE_SLASH_ASTER_COMMENT);
                return text.length();
            }
            // we need to call processSeparator since text may follow the
            // end of comment immediately without even a space
            TypeHandler.processSeparator(text, charTypes, offsets, location);
            return location + 2;
        case 5: /* hyphen-hyphen comment */
            location = text.indexOf(lineSep, separLocation + 2);
            if (location < 0)
                return text.length();
            return location + lineSep.length();
        }
        // we should never get here
        return text.length();
    }
}