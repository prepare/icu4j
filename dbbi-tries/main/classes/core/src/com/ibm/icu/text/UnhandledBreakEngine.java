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

import static com.ibm.icu.impl.CharacterIteration.*;

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

    public int findBreaks(CharacterIterator text, int startPos, int endPos,
            boolean reverse, int breakType, Stack<Integer> foundBreaks) {
        // TODO: modify later
        text.setIndex(endPos);
        return 0;
    }

    public void handleChar(int c, int breakType) {
        if (breakType >= 0 && breakType < fHandled.length && c != DONE32) {
            if (!fHandled[breakType].contains(c)) {
                int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
                fHandled[breakType].applyIntPropertyValue(UProperty.SCRIPT, script);
            }
        }
        return;
    }
}
