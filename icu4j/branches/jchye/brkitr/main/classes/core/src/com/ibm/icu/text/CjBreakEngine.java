/*
 *******************************************************************************
 * Copyright (C) 2012-2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import static com.ibm.icu.impl.CharacterIteration.current32;
import static com.ibm.icu.impl.CharacterIteration.next32;

import java.io.IOException;
import java.text.CharacterIterator;

/**
 * Word frequency-based break engine for Chinese and Japanese.
 */
class CjBreakEngine extends FrequencyBreakEngine {
    private static final UnicodeSet fHanWordSet = new UnicodeSet();
    private static final UnicodeSet fKatakanaWordSet = new UnicodeSet();
    private static final UnicodeSet fHiraganaWordSet = new UnicodeSet();
    static {
        fHanWordSet.applyPattern("[:Han:]");
        fKatakanaWordSet.applyPattern("[[:Katakana:]\\uff9e\\uff9f]");
        fHiraganaWordSet.applyPattern("[:Hiragana:]");
        
        // freeze them all
        fHanWordSet.freeze();
        fKatakanaWordSet.freeze();
        fHiraganaWordSet.freeze();
    }
    
    public CjBreakEngine() throws IOException {
        super("Hira", BreakIterator.KIND_WORD);
        UnicodeSet cjSet = new UnicodeSet();
        cjSet = new UnicodeSet();
        cjSet.addAll(fHanWordSet);
        cjSet.addAll(fKatakanaWordSet);
        cjSet.addAll(fHiraganaWordSet);
        cjSet.add(0xFF70); // HALFWIDTH KATAKANA-HIRAGANA PROLONGED SOUND MARK
        cjSet.add(0x30FC); // KATAKANA-HIRAGANA PROLONGED SOUND MARK
        setCharacters(cjSet);
    }
    
    private static final int kMaxKatakanaLength = 8;
    private static final int kMaxKatakanaGroupLength = 20;
    private static int getKatakanaCost(int wordlength) {
        int katakanaCost[] =  new int[] { 8192, 984, 408, 240, 204, 252, 300, 372, 480 };
        return (wordlength > kMaxKatakanaLength) ? 8192 : katakanaCost[wordlength];
    }
    
    private static boolean isKatakana(int value) {
        return (value >= 0x30A1 && value <= 0x30FE && value != 0x30FB) ||
                (value >= 0xFF66 && value <= 0xFF9F);
    }
    
    protected void findBoundaries(CharacterIterator text, int[] bestSnlp, int[] prev) {
        super.findBoundaries(text, bestSnlp, prev);
        int numChars = bestSnlp.length - 1;

        // In Japanese, single-character Katakana words are pretty rare.
        // So we apply the following heuristic to Katakana: any continuous
        // run of Katakana characters is considered a candidate word with
        // a default cost specified in the katakanaCost table according 
        // to its length.
        boolean is_prev_katakana = false;
        for (int i = 0; i < numChars; i++) {
            text.setIndex(i);
            boolean is_katakana = isKatakana(current32(text));
            if (!is_prev_katakana && is_katakana) {
                int j = i + 1;
                next32(text);
                while (j < numChars && (j - i) < kMaxKatakanaGroupLength && isKatakana(current32(text))) {
                    next32(text);
                    ++j;
                }
                
                if ((j - i) < kMaxKatakanaGroupLength) {
                    int newSnlp = bestSnlp[i] + getKatakanaCost(j - i);
                    if (newSnlp < bestSnlp[j]) {
                        bestSnlp[j] = newSnlp;
                        prev[j] = i;
                    }
                }
            }
            is_prev_katakana = is_katakana;
        }
    }
}
