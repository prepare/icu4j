/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.IOException;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.lang.UScript;

/**
 * Word frequency-based break engine for Chinese and Japanese.
 */
public class ThaiFrequencyBreakEngine extends FrequencyBreakEngine {
    private static UnicodeSet fThaiWordSet;
    
    static {
        // Initialize UnicodeSets
        fThaiWordSet = new UnicodeSet();
        fThaiWordSet.applyPattern("[[:Thai:]&[:LineBreak=SA:]]");
        fThaiWordSet.compact();
        
        // Freeze the static UnicodeSet
        fThaiWordSet.freeze();
    }

    public ThaiFrequencyBreakEngine() throws IOException {
        super("Thai", BreakIterator.KIND_WORD, BreakIterator.KIND_LINE);
        setCharacters(fThaiWordSet);
    }

    public boolean handles(int c, int breakType) {
        if (breakType == BreakIterator.KIND_WORD || breakType == BreakIterator.KIND_LINE) {
            int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
            return (script == UScript.THAI);
        }
        return false;
    }
}
