/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt.handlers;

import com.ibm.icu.impl.stt.CharTypes;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Handler adapted to processing arithmetic expressions with a possible
 * right-to-left base direction.
 */
public class Math extends TypeHandler {
    public Math() {
        super("+-/*()=");
    }

    public BidiStructuredProcessor.Orientation getDirection(Expert expert, String text) {
        return getDirection(expert, text, new CharTypes(expert, text));
    }

    /**
     * @return {@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#RTL} if the following conditions are
     *         satisfied:
     *         <ul>
     *         <li>The current locale (as expressed by the environment language)
     *         is Arabic.</li>
     *         <li>The first strong character is an Arabic letter.</li>
     *         <li>If there is no strong character in the text, there is at
     *         least one Arabic-Indic digit in the text.</li>
     *         </ul>
     *         Otherwise, returns {@link com.ibm.icu.text.BidiStructuredProcessor.Orientation#LTR}.
     */
    public BidiStructuredProcessor.Orientation getDirection(Expert expert, String text, CharTypes charTypes) {
        String language = expert.getEnvironment().getLocale().getLanguage();
        if (!language.equals("ar"))
            return BidiStructuredProcessor.Orientation.LTR;
        boolean flagAN = false;
        for (int i = 0; i < text.length(); i++) {
            byte charType = charTypes.getBidiTypeAt(i);
            if (charType == CharTypes.AL)
                return BidiStructuredProcessor.Orientation.RTL;
            if (charType == CharTypes.L || charType == CharTypes.R)
                return BidiStructuredProcessor.Orientation.LTR;
            if (charType == CharTypes.AN)
                flagAN = true;
        }
        if (flagAN)
            return BidiStructuredProcessor.Orientation.RTL;
        return BidiStructuredProcessor.Orientation.LTR;
    }
}