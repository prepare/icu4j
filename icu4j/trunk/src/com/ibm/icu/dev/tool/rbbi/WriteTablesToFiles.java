/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/rbbi/Attic/WriteTablesToFiles.java,v $ 
 * $Date: 2003/05/14 18:36:43 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.dev.tool.rbbi;

import java.io.*;
import com.ibm.icu.text.*;

public class WriteTablesToFiles {
    public static void main(String[] args) throws IOException {
        writeBatch(true);
        writeBatch(false);
    }
    
    public static void writeBatch(boolean littleEndian) throws IOException {
        BreakIterator bi;
        String suffix = (littleEndian ? "LE" : "BE");
        
        bi = BreakIterator.getCharacterInstance();
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "char" + suffix + ".brk"), littleEndian);
        
        bi = BreakIterator.getWordInstance();
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "word" + suffix + ".brk"), littleEndian);
        
        bi = BreakIterator.getLineInstance();
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "line" + suffix + ".brk"), littleEndian);
        
        bi = BreakIterator.getSentenceInstance();
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "sent" + suffix + ".brk"), littleEndian);

        bi = BreakIterator.getTitleInstance();
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "title" + suffix + ".brk"), littleEndian);

        java.util.Locale thai = new java.util.Locale("th", "", "");
        bi = BreakIterator.getWordInstance(thai);
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "word_th" + suffix + ".brk"), littleEndian);

        bi = BreakIterator.getLineInstance(thai);
        ((RuleBasedBreakIterator)bi).writeTablesToFile(new FileOutputStream(
                    "line_th" + suffix + ".brk"), littleEndian);
    }
}
