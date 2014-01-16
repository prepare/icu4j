/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.stt;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.ExpertFactory;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.text.BidiStructuredProcessor.Orientation;
import com.ibm.icu.util.ULocale;

/**
 */
public class TestBidiStructuredProcessor extends TestFmwk 
{
    private static final String HEBREW = "iw";
    private static final String HEBREW2 = "he";
    private static final String ARABIC = "ar";
    private static final String FARSI = "fa";
    private static final String URDU = "ur";

    static final private char LRM = 0x200E;
    static final private char RLM = 0x200F;
    static final private char LRE = 0x202A;
    static final private char RLE = 0x202B;
    static final private char PDF = 0x202C;

    public static void main(String[] args) throws Exception 
    {
        new TestBidiStructuredProcessor().run(args);
    }

    public static String toPseudo(String text) {
        char[] chars = text.toCharArray();
        int len = chars.length;

        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z')
                chars[i] = (char) (c + 'a' - 'A');
            else if (c >= 0x05D0 && c < 0x05EA)
                chars[i] = (char) (c + 'A' - 0x05D0);
            else if (c == 0x05EA)
                chars[i] = '~';
            else if (c == 0x0644)
                chars[i] = '#';
            else if (c >= 0x0665 && c <= 0x0669)
                chars[i] = (char) (c + '5' - 0x0665);
            else if (c == LRM)
                chars[i] = '@';
            else if (c == RLM)
                chars[i] = '&';
            else if (c == LRE)
                chars[i] = '>';
            else if (c == RLE)
                chars[i] = '<';
            else if (c == PDF)
                chars[i] = '^';
            else if (c == '\n')
                chars[i] = '|';
            else if (c == '\r')
                chars[i] = '`';
        }
        return new String(chars);
    }

    public static String toUT16(String text) {
        char[] chars = text.toCharArray();
        int len = chars.length;

        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c >= '5' && c <= '9')
                chars[i] = (char) (0x0665 + c - '5');
            else if (c >= 'A' && c <= 'Z')
                chars[i] = (char) (0x05D0 + c - 'A');
            else if (c == '~')
                chars[i] = (char) (0x05EA);
            else if (c == '#')
                chars[i] = (char) (0x0644);
            else if (c == '@')
                chars[i] = LRM;
            else if (c == '&')
                chars[i] = RLM;
            else if (c == '>')
                chars[i] = LRE;
            else if (c == '<')
                chars[i] = RLE;
            else if (c == '^')
                chars[i] = PDF;
            else if (c == '|')
                chars[i] = '\n';
            else if (c == '`')
                chars[i] = '\r';
        }
        return new String(chars);
    }

    static String array_display(int[] array) {
        if (array == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer(50);
        int len = array.length;
        for (int i = 0; i < len; i++) {
            sb.append(array[i]);
            sb.append(' ');
        }
        return sb.toString();
    }
    
    private void doTest1(String data, String result) 
    {
        ULocale.setDefault(ULocale.ENGLISH);
        String full = BidiStructuredProcessor.transform(toUT16(data));
        assertEquals("Util #1 full EN", data, toPseudo(full));
        ULocale.setDefault(new ULocale(HEBREW2));
        full = BidiStructuredProcessor.transform(toUT16(data));
        assertEquals("Util #1 full HE", result, toPseudo(full));
        ULocale.setDefault(new ULocale(ARABIC));
        full = BidiStructuredProcessor.transform(toUT16(data));
        assertEquals("Util #1 full AR", result, toPseudo(full));
        ULocale.setDefault(new ULocale(FARSI));
        full = BidiStructuredProcessor.transform(toUT16(data));
        assertEquals("Util #1 full FA", result, toPseudo(full));
        ULocale.setDefault(new ULocale(URDU));
        full = BidiStructuredProcessor.transform(toUT16(data));
        assertEquals("Util #1 full UR", result, toPseudo(full));
        ULocale.setDefault(new ULocale(HEBREW));
        full = BidiStructuredProcessor.transform(toUT16(data));
        String ful2 = BidiStructuredProcessor.transform(toUT16(data), (String) null);
        assertEquals("Util #1 full", result, toPseudo(full));
        assertEquals("Util #1 ful2", result, toPseudo(ful2));
    }

    private void doTest2(String msg, String data, String result) 
    {
        doTest2(msg, data, result, data);
    }

    private void doTest2(String msg, String data, String result, String resLean) 
    {
        String full = BidiStructuredProcessor.transform(toUT16(data), "*");
        assertEquals(msg + "full", result, toPseudo(full));
    }

    private void doTest3(String msg, String data, String result) 
    {
        doTest3(msg, data, result, data);
    }

    private void doTest3(String msg, String data, String result, String resLean) 
    {
        String full = BidiStructuredProcessor.transform(toUT16(data), BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED);
        assertEquals(msg + "full", result, toPseudo(full));
    }

    // not really testing a part of the exposed API. Left over from refactoring Processor (keeping for historical reference)
    private void doTest4(String msg, String data, int[] offsets, Orientation direction,
            int affixLength, String result) {
        String txt = msg + "text=" + data + "\n    offsets="
                + array_display(offsets) + "\n    direction=" + direction
                + "\n    affixLength=" + affixLength;
        String lean = toUT16(data);
        Expert expert = ExpertFactory.getExpert();
        String full = expert.insertMarks(lean, offsets, direction, affixLength);
        assertEquals(txt, result, toPseudo(full));
    }

    public void TestDefaultDelimiters()
    {
        // Test process() and deprocess() with default delimiters
        doTest1("ABC/DEF/G", ">@ABC@/DEF@/G@^");
    }
    
    public void TestSepcificDelimiters()
    {
        // Test process() with specified delimiters
        ULocale.setDefault(new ULocale(HEBREW));
        doTest2("Util #2.1 - ", "", "");
        doTest2("Util #2.2 - ", ">@ABC@^", ">@ABC@^", "ABC");
        doTest2("Util #2.3 - ", "abc", "abc");
        doTest2("Util #2.4 - ", "!abc", ">@!abc@^");
        doTest2("Util #2.5 - ", "abc!", ">@abc!@^");
        doTest2("Util #2.6 - ", "ABC*DEF*G", ">@ABC@*DEF@*G@^");
    }
    
    public void TestSpcificExpressionType()
    {
        // Test process() with specified expression type
        ULocale.setDefault(new ULocale(HEBREW));
        doTest3("Util #3.1 - ", "ABC,DEF,G", ">@ABC@,DEF@,G@^");
        doTest3("Util #3.2 - ", "", "");
        doTest3("Util #3.3 - ", ">@DEF@^", ">@DEF@^", "DEF");
    }
    
    // not really testing a part of the exposed API. Left over from refactoring Processor (keeping for historical reference)
    public void TestInsertMarks() 
    {
        ULocale.setDefault(new ULocale(HEBREW));
        // Test insertMarks()
        doTest4("Util #4.1 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 0,
                "ABC@DEF@G");
        doTest4("Util #4.2 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 2,
                ">@ABC@DEF@G@^");
        doTest4("Util #4.3 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.RTL, 0,
                "ABC&DEF&G");
        doTest4("Util #4.4 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.RTL, 2,
                "<&ABC&DEF&G&^");
        doTest4("Util #4.5 - ", "", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 0, "");
        doTest4("Util #4.6 - ", "", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 2, "");
        doTest4("Util #4.7 - ", "ABCDEFG", null, BidiStructuredProcessor.Orientation.RTL, 0, "ABCDEFG");
        doTest4("Util #4.8 - ", "ABCDEFG", null, BidiStructuredProcessor.Orientation.RTL, 2, "<&ABCDEFG&^");
    }

}
