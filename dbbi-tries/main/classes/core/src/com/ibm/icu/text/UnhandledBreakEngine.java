/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.CharacterIterator;
import java.util.Stack;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.lang.UScript;

public final class UnhandledBreakEngine implements LanguageBreakEngine {
    private final UnicodeSet[] fHandled = new UnicodeSet[BreakIterator.KIND_TITLE + 1];
    public UnhandledBreakEngine() {
        for (int i = 0; i < fHandled.length; i++) {
            fHandled[i] = new UnicodeSet();
        }
    }
    
    public boolean handles(int c, int breakType) {
        return (breakType >= 0 && breakType < fHandled.length) && 
                (fHandled[breakType].contains(c));
    }

    public int findBreaks(CharacterIterator text_, int startPos, int endPos,
            boolean reverse, int breakType, Stack<Integer> foundBreaks) {
        UCharacterIterator text = UCharacterIterator.getInstance(text_);
        if (breakType >= 0 && breakType < fHandled.length) {
            UnicodeSet handled = fHandled[breakType];
            int c = text.current();
            if (reverse) {
                while (text.getIndex() > startPos && handled.contains(c)) {
                    c = text.previous();
                }
            } else {
                while (text.getIndex() < endPos && handled.contains(c)) {
                    c = text.next();
                }
            }
        }
        text_.setIndex(text.getIndex());
        return 0;
    }

    public void handleChar(int c, int breakType) {
        if (breakType >= 0 && breakType < fHandled.length) {
            int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
            if (script != UScript.COMMON &&
                script != UScript.INHERITED &&
                script != UScript.UNKNOWN) {
                if (!fHandled[breakType].contains(c)) {
                    fHandled[breakType].applyIntPropertyValue(UProperty.SCRIPT,
                            script);
                }
            }
        }
    }
}
