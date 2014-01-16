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
 * Handler adapted to processing e-mail addresses.
 */
public class Email extends DelimsEsc {

    public Email() {
        super("<>.:,;@");
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
     *         <li>The domain part of the email address contains at least one
     *         RTL character.</li>
     *         </ul>
     *         Otherwise, returns {@link Expert#DIR_LTR DIR_LTR}.
     */
    public BidiStructuredProcessor.Orientation getDirection(Expert expert, String text, CharTypes charTypes) {
        String language = expert.getEnvironment().getLocale().getLanguage();
        if (!language.equals("ar"))
            return BidiStructuredProcessor.Orientation.LTR;
        int domainStart;
        domainStart = text.indexOf('@');
        if (domainStart < 0)
            domainStart = 0;
        for (int i = domainStart; i < text.length(); i++) {
            byte charType = charTypes.getBidiTypeAt(i);
            if (charType == CharTypes.AL || charType == CharTypes.R)
                return BidiStructuredProcessor.Orientation.RTL;
        }
        return BidiStructuredProcessor.Orientation.RTL;
    }

    /**
     * @return 2 as number of special cases handled by this handler.
     */
    public int getSpecialsCount() {
        return 2;
    }

    /**
     * @return parentheses and quotation marks as delimiters.
     */
    protected String getDelimiters() {
        return "()\"\"";
    }
}