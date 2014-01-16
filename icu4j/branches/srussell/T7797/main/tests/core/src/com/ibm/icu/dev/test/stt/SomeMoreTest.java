/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.impl.stt.CharTypes;
import com.ibm.icu.impl.stt.Environment;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.ExpertFactory;
import com.ibm.icu.impl.stt.Offsets;
import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.util.ULocale;

/**
 * Test edge conditions.
 */
public class SomeMoreTest extends TestBase {

    int cntError;

    static class TestHandler1 extends TypeHandler {

        public TestHandler1() {
            // empty constructor
        }

        public int getSpecialsCount() {
            return 1;
        }

        public int indexOfSpecial(Expert expert, String text,
                CharTypes charTypes, Offsets offsets, int caseNumber,
                int fromIndex) {
            return fromIndex;
        }

        public int processSpecial(Expert expert, String text,
                CharTypes charTypes, Offsets offsets, int caseNumber,
                int separLocation) {
            int len = text.length();
            for (int i = len - 1; i >= 0; i--) {
                TypeHandler.insertMark(text, charTypes, offsets, i);
                TypeHandler.insertMark(text, charTypes, offsets, i);
            }
            return len;
        }
    }

    static class TestHandler2 extends TypeHandler {

        public TestHandler2() {
            // empty constructor
        }

        public int getSpecialsCount() {
            return 1;
        }
    }

    static class TestHandler3 extends TypeHandler {

        public TestHandler3() {
            // empty constructor
        }

        public int getSpecialsCount() {
            return 1;
        }

        public int indexOfSpecial(Expert expert, String text,
                CharTypes charTypes, Offsets offsets, int caseNumber,
                int fromIndex) {
            return fromIndex;
        }
    }

    final static Environment env1 = new Environment(new ULocale("en_US"),
            false, BidiStructuredProcessor.Orientation.LTR);
    final static Environment env2 = new Environment(new ULocale("he"), false,
            BidiStructuredProcessor.Orientation.LTR);

    public static int main(String[] args) {
        SomeMoreTest test = new SomeMoreTest();

        test.cntError += assertFalse("", env1.isProcessingNeeded());
        test.cntError += assertTrue("", env2.isProcessingNeeded());

        TypeHandler handler1 = new TestHandler1();
        Expert expert1 = ExpertFactory.getStatefulExpert(handler1, env1);
        String full = expert1.leanToFullText("abcd");
        test.cntError += assertEquals("", "@a@b@c@d", toPseudo(full));

        TypeHandler handler2 = new TestHandler2();
        Expert expert2 = ExpertFactory.getStatefulExpert(handler2, env1);
        boolean catchFlag = false;
        try {
            full = expert2.leanToFullText("abcd");
        } catch (IllegalStateException e) {
            catchFlag = true;
        }
        test.cntError += assertTrue("Catch missing indexOfSpecial", catchFlag);

        TypeHandler handler3 = new TestHandler3();
        Expert expert3 = ExpertFactory.getStatefulExpert(handler3, env1);
        catchFlag = false;
        try {
            full = expert3.leanToFullText("abcd");
        } catch (IllegalStateException e) {
            catchFlag = true;
        }
        test.cntError += assertTrue("Catch missing processSpecial", catchFlag);

        return test.cntError;
    }

}
