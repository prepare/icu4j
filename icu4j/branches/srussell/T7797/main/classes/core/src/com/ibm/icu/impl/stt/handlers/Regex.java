/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt.handlers;

import com.ibm.icu.impl.stt.CharTypes;
import com.ibm.icu.impl.stt.Environment;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.Offsets;
import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Handler for regular expressions. Such expressions may span multiple lines.
 * <p>
 * In applications like an editor where parts of the text might be modified
 * while other parts are not, the user may want to call
 * {@link Expert#leanToFullText} separately on each line and save the initial
 * state of each line (this is the final state of the previous line which can be
 * retrieved using {@link Expert#getState()}. If both the content of a line and
 * its initial state have not changed, the user can be sure that the last
 * <i>full</i> text computed for this line has not changed either.
 * 
 * @see Expert explanation of state
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public class Regex extends TypeHandler {
    static final String[] startStrings = { "", /* 0 *//* dummy */
    "(?#", /* 1 *//* comment (?#...) */
    "(?<", /* 2 *//* named group (?<name> */
    "(?'", /* 3 *//* named group (?'name' */
    "(?(<", /* 4 *//* conditional named back reference (?(<name>) */
    "(?('", /* 5 *//* conditional named back reference (?('name') */
    "(?(", /* 6 *//* conditional named back reference (?(name) */
    "(?&", /* 7 *//* named parentheses reference (?&name) */
    "(?P<", /* 8 *//* named group (?P<name> */
    "\\k<", /* 9 *//* named back reference \k<name> */
    "\\k'", /* 10 *//* named back reference \k'name' */
    "\\k{", /* 11 *//* named back reference \k{name} */
    "(?P=", /* 12 *//* named back reference (?P=name) */
    "\\g{", /* 13 *//* named back reference \g{name} */
    "\\g<", /* 14 *//* subroutine call \g<name> */
    "\\g'", /* 15 *//* subroutine call \g'name' */
    "(?(R&", /* 16 *//* named back reference recursion (?(R&name) */
    "\\Q" /* 17 *//* quoted sequence \Q...\E */
    };
    static final char[] endChars = {
            // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
            '.', ')', '>', '\'', ')', ')', ')', ')', '>', '>', '\'', '}', ')',
            '}', '>', '\'', ')' };
    static final int numberOfStrings = startStrings.length; /* 18 */
    static final int maxSpecial = numberOfStrings;
    private static final Integer STATE_COMMENT = new Integer(1);
    private static final Integer STATE_QUOTED_SEQUENCE = new Integer(17);

    /**
     * Retrieves the number of special cases handled by this handler.
     * 
     * @return the number of special cases for this handler.
     */
    public int getSpecialsCount() {
        return maxSpecial;
    }

    /**
     * Locates occurrences of the syntactic strings and of R, AL, EN, AN
     * characters.
     */
    public int indexOfSpecial(Expert expert, String text, CharTypes charTypes,
            Offsets offsets, int caseNumber, int fromIndex) {
        // In this method, L, R, AL, AN and EN represent bidi categories
        // as defined in the Unicode Bidirectional Algorithm
        // ( http://www.unicode.org/reports/tr9/ ).
        // L represents the category Left to Right character.
        // R represents the category Right to Left character.
        // AL represents the category Arabic Letter.
        // AN represents the category Arabic Number.
        // EN represents the category European Number.
        byte charType;

        if (caseNumber < numberOfStrings) {
            /* 1 *//* comment (?#...) */
            /* 2 *//* named group (?<name> */
            /* 3 *//* named group (?'name' */
            /* 4 *//* conditional named back reference (?(name) */
            /* 5 *//* conditional named back reference (?(<name>) */
            /* 6 *//* conditional named back reference (?('name') */
            /* 7 *//* named parentheses reference (?&name) */
            /* 8 *//* named group (?P<name> */
            /* 9 *//* named back reference \k<name> */
            /* 10 *//* named back reference \k'name' */
            /* 11 *//* named back reference \k{name} */
            /* 12 *//* named back reference (?P=name) */
            /* 13 *//* named back reference \g{name} */
            /* 14 *//* subroutine call \g<name> */
            /* 15 *//* subroutine call \g'name' */
            /* 16 *//* named back reference recursion (?(R&name) */
            /* 17 *//* quoted sequence \Q...\E */
            return text.indexOf(startStrings[caseNumber], fromIndex);
        }
        // there never is a need for a mark before the first char
        if (fromIndex <= 0)
            fromIndex = 1;
        // look for R, AL, AN, EN which are potentially needing a mark
        for (; fromIndex < text.length(); fromIndex++) {
            charType = charTypes.getBidiTypeAt(fromIndex);
            // R and AL will always be examined using processSeparator()
            if (charType == CharTypes.R || charType == CharTypes.AL)
                return fromIndex;

            if (charType == CharTypes.EN || charType == CharTypes.AN) {
                // no need for a mark after the first digit in a number
                if (charTypes.getBidiTypeAt(fromIndex - 1) == charType)
                    continue;

                for (int i = fromIndex - 1; i >= 0; i--) {
                    charType = charTypes.getBidiTypeAt(i);
                    // after a L char, no need for a mark
                    if (charType == CharTypes.L)
                        continue;

                    // digit after R or AL or AN need a mark, except for EN
                    // following AN, but this is a contrived case, so we
                    // don't check for it (and calling processSeparator()
                    // for it will do no harm)
                    if (charType == CharTypes.R || charType == CharTypes.AL
                            || charType == CharTypes.AN)
                        return fromIndex;
                }
                continue;
            }
        }
        return -1;
    }

    /**
     * Processes the special cases.
     */
    public int processSpecial(Expert expert, String text, CharTypes charTypes,
            Offsets offsets, int caseNumber, int separLocation) {
        int location;

        if (separLocation < 0) {
            caseNumber = ((Integer) expert.getState()).intValue(); // TBD
                                                                    // guard
                                                                    // against
                                                                    // "undefined"
            expert.clearState();
        }
        switch (caseNumber) {
        case 1: /* comment (?#...) */
            if (separLocation < 0) {
                // initial state from previous line
                location = 0;
            } else {
                TypeHandler.processSeparator(text, charTypes, offsets,
                        separLocation);
                // skip the opening "(?#"
                location = separLocation + 3;
            }
            location = text.indexOf(')', location);
            if (location < 0) {
                expert.setState(STATE_COMMENT);
                return text.length();
            }
            return location + 1;
        case 2: /* named group (?<name> */
        case 3: /* named group (?'name' */
        case 4: /* conditional named back reference (?(name) */
        case 5: /* conditional named back reference (?(<name>) */
        case 6: /* conditional named back reference (?('name') */
        case 7: /* named parentheses reference (?&name) */
            TypeHandler.processSeparator(text, charTypes, offsets,
                    separLocation);
            // no need for calling processSeparator() for the following
            // cases
            // since the starting string contains a L char
        case 8: /* named group (?P<name> */
        case 9: /* named back reference \k<name> */
        case 10: /* named back reference \k'name' */
        case 11: /* named back reference \k{name} */
        case 12: /* named back reference (?P=name) */
        case 13: /* named back reference \g{name} */
        case 14: /* subroutine call \g<name> */
        case 15: /* subroutine call \g'name' */
        case 16: /* named back reference recursion (?(R&name) */
            // skip the opening string
            location = separLocation + startStrings[caseNumber].length();
            // look for ending character
            location = text.indexOf(endChars[caseNumber], location);
            if (location < 0)
                return text.length();
            return location + 1;
        case 17: /* quoted sequence \Q...\E */
            if (separLocation < 0) {
                // initial state from previous line
                location = 0;
            } else {
                TypeHandler.processSeparator(text, charTypes, offsets,
                        separLocation);
                // skip the opening "\Q"
                location = separLocation + 2;
            }
            location = text.indexOf("\\E", location);
            if (location < 0) {
                expert.setState(STATE_QUOTED_SEQUENCE);
                return text.length();
            }
            // set the charType for the "E" to L (Left to Right character)
            charTypes.setBidiTypeAt(location + 1, CharTypes.L);
            return location + 2;
        case 18: /* R, AL, AN, EN */
            TypeHandler.processSeparator(text, charTypes, offsets,
                    separLocation);
            return separLocation + 1;

        }
        // we should never get here
        return text.length();
    }

    public BidiStructuredProcessor.Orientation getDirection(Expert expert, String text) {
        return getDirection(expert, text, new CharTypes(expert, text));
    }

    /**
     * @return {@link Expert#DIR_RTL DIR_RTL} if the following conditions are
     *         satisfied:
     *         <ul>
     *         <li>The current locale (as expressed by the environment language)
     *         is Arabic.</li>
     *         <li>The first strong character has an RTL direction.</li>
     *         <li>If there is no strong character in the text, the GUI is
     *         mirrored.
     *         </ul>
     *         Otherwise, returns {@link Expert#DIR_LTR DIR_LTR}.
     */
    public BidiStructuredProcessor.Orientation getDirection(Expert expert, String text, CharTypes charTypes) {
        Environment environment = expert.getEnvironment();
        String language = environment.getLocale().getLanguage();
        if (!language.equals("ar"))
            return BidiStructuredProcessor.Orientation.LTR;
        for (int i = 0; i < text.length(); i++) {
            byte charType = charTypes.getBidiTypeAt(i);
            if (charType == CharTypes.AL || charType == CharTypes.R)
                return BidiStructuredProcessor.Orientation.RTL;
            if (charType == CharTypes.L)
                return BidiStructuredProcessor.Orientation.LTR;
        }
        if (environment.getMirrored())
            return BidiStructuredProcessor.Orientation.RTL;
        return BidiStructuredProcessor.Orientation.LTR;
    }
}