/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/LowercaseTransliterator.java,v $ 
 * $Date: 2002/04/03 00:00:00 $ 
 * $Revision: 1.10 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.text;
import java.util.*;
import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.impl.UCharacterIterator;

/**
 * A transliterator that performs locale-sensitive toLower()
 * case mapping.
 */
class LowercaseTransliterator extends Transliterator{

    /**
     * Package accessible ID.
     */
    static final String _ID = "Any-Lower";
    
    // TODO: Add variants for tr, az, lt, default = default locale

    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new LowercaseTransliterator(Locale.US);
            }
        });

        Transliterator.registerSpecialInverse("Lower", "Upper", true);
    }

    private Locale loc;

    /**
     * Constructs a transliterator.
     */

    public LowercaseTransliterator(Locale loc) {
        super(_ID, null);
        this.loc = loc;
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    protected void handleTransliterate(Replaceable text,
                                       Position offsets, boolean isIncremental) {
        int textPos = offsets.start;
        if (textPos >= offsets.limit) return;

        // get string for context
        // TODO: add convenience method to do this, since we do it all over
        
        UCharacterIterator original = new UCharacterIterator(text);
        
        // Walk through original string
        // If there is a case change, modify corresponding position in replaceable
        
        int limit = offsets.limit;
        int cp;
        int oldLen;
        
        while (textPos < limit) {
        	original.setIndex(textPos);
            cp = original.currentCodePoint();
            oldLen = UTF16.getCharCount(cp);
            int newLen = m_charppty_.toLowerCase(loc, cp, original, buffer);
            if (newLen >= 0) {
                text.replace(textPos, textPos + oldLen, buffer, 0, newLen);
                if (newLen != oldLen) {
                    textPos += newLen;
                    offsets.limit += newLen - oldLen;
                    offsets.contextLimit += newLen - oldLen;
                    continue;
                }
            }
            textPos += oldLen;
        }
        offsets.start = offsets.limit;
    }
    
    private char buffer[] = new char[UCharacterProperty.MAX_CASE_MAP_SIZE];
    /**
     * Character properties data base
     */
    private static final UCharacterProperty m_charppty_ = 
    							            UCharacterProperty.getInstance(); 
}
