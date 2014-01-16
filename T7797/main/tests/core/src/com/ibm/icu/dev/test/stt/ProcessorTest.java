/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.ExpertFactory;
import com.ibm.icu.impl.stt.Processor;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.text.BidiStructuredProcessor.Orientation;
import com.ibm.icu.util.ULocale;

/**
 * Tests methods in BidiComplexUtil
 */

public class ProcessorTest extends TestBase {

    private static final String HEBREW = "iw";

    private static final String HEBREW2 = "he";

    private static final String ARABIC = "ar";

    private static final String FARSI = "fa";

    private static final String URDU = "ur";

    // private ULocale locale = ULocale.getDefault();
    int cntError;

    private void doTest1(String data, String result) {
        ULocale.setDefault(ULocale.ENGLISH);
        String full = Processor.process(toUT16(data));
        cntError += assertEquals("Util #1 full EN - ", data, toPseudo(full));
        ULocale.setDefault(new ULocale(HEBREW2));
        full = Processor.process(toUT16(data));
        cntError += assertEquals("Util #1 full HE - ", result, toPseudo(full));
        ULocale.setDefault(new ULocale(ARABIC));
        full = Processor.process(toUT16(data));
        cntError += assertEquals("Util #1 full AR - ", result, toPseudo(full));
        ULocale.setDefault(new ULocale(FARSI));
        full = Processor.process(toUT16(data));
        cntError += assertEquals("Util #1 full FA - ", result, toPseudo(full));
        ULocale.setDefault(new ULocale(URDU));
        full = Processor.process(toUT16(data));
        cntError += assertEquals("Util #1 full UR - ", result, toPseudo(full));
        ULocale.setDefault(new ULocale(HEBREW));
        full = Processor.process(toUT16(data));
        String ful2 = Processor.process(toUT16(data), (String) null);
        cntError += assertEquals("Util #1 full - ", result, toPseudo(full));
        cntError += assertEquals("Util #1 ful2 - ", result, toPseudo(ful2));
        String lean = Processor.deprocess(full);
        cntError += assertEquals("Util #1 lean - ", data, toPseudo(lean));
    }

    private void doTest2(String msg, String data, String result) {
        doTest2(msg, data, result, data);
    }

    private void doTest2(String msg, String data, String result, String resLean) {
        String full = Processor.process(toUT16(data), "*");
        cntError += assertEquals(msg + "full", result, toPseudo(full));
        String lean = Processor.deprocess(full);
        cntError += assertEquals(msg + "lean", resLean, toPseudo(lean));
    }

    private void doTest3(String msg, String data, String result) {
        doTest3(msg, data, result, data);
    }

    private void doTest3(String msg, String data, String result, String resLean) {
        String full = Processor.processTyped(toUT16(data),
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED);
        cntError += assertEquals(msg + "full", result, toPseudo(full));
        String lean = Processor.deprocessTyped(full,
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED);
        cntError += assertEquals(msg + "lean", resLean, toPseudo(lean));
    }

    private void doTest4(String msg, String data, int[] offsets, Orientation direction,
            int affixLength, String result) {
        String txt = msg + "text=" + data + "\n    offsets="
                + array_display(offsets) + "\n    direction=" + direction
                + "\n    affixLength=" + affixLength;
        String lean = toUT16(data);
        Expert expert = ExpertFactory.getExpert();
        String full = expert.insertMarks(lean, offsets, direction, affixLength);
        cntError += assertEquals(txt, result, toPseudo(full));
    }

    public static int main(String[] args) {
        ProcessorTest test = new ProcessorTest();
        // Test process() and deprocess() with default delimiters
        test.doTest1("ABC/DEF/G", ">@ABC@/DEF@/G@^");
        // Test process() and deprocess() with specified delimiters
        test.doTest2("Util #2.1 - ", "", "");
        test.doTest2("Util #2.2 - ", ">@ABC@^", ">@ABC@^", "ABC");
        test.doTest2("Util #2.3 - ", "abc", "abc");
        test.doTest2("Util #2.4 - ", "!abc", ">@!abc@^");
        test.doTest2("Util #2.5 - ", "abc!", ">@abc!@^");
        test.doTest2("Util #2.6 - ", "ABC*DEF*G", ">@ABC@*DEF@*G@^");
        // Test process() and deprocess() with specified expression type
        test.doTest3("Util #3.1 - ", "ABC,DEF,G", ">@ABC@,DEF@,G@^");
        test.doTest3("Util #3.2 - ", "", "");
        test.doTest3("Util #3.3 - ", ">@DEF@^", ">@DEF@^", "DEF");
        // Test insertMarks()
        test.doTest4("Util #4.1 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 0,
                "ABC@DEF@G");
        test.doTest4("Util #4.2 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 2,
                ">@ABC@DEF@G@^");
        test.doTest4("Util #4.3 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.RTL, 0,
                "ABC&DEF&G");
        test.doTest4("Util #4.4 - ", "ABCDEFG", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.RTL, 2,
                "<&ABC&DEF&G&^");
        test.doTest4("Util #4.5 - ", "", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 0, "");
        test.doTest4("Util #4.6 - ", "", new int[] { 3, 6 }, BidiStructuredProcessor.Orientation.LTR, 2, "");
        test.doTest4("Util #4.7 - ", "ABCDEFG", null, BidiStructuredProcessor.Orientation.RTL, 0, "ABCDEFG");
        test.doTest4("Util #4.8 - ", "ABCDEFG", null, BidiStructuredProcessor.Orientation.RTL, 2, "<&ABCDEFG&^");

        return test.cntError;
    }
}
