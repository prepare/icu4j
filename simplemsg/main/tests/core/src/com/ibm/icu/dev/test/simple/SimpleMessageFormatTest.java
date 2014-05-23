/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.dev.test.simple;

import java.util.Locale;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.simple.MessageFormat;

public class SimpleMessageFormatTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new SimpleMessageFormatTest().run(args);
    }

    public void TestBasic() {
        assertEquals("one simple argument", "Going to Germany and back",
                MessageFormat.formatMessageNamedArgs(
                        Locale.US, "Going to {place} and back", "place", "Germany"));
    }

    public void TestSelect() {
        String msg = "{gender,select,female{her book}male{his book}other{their book}}";
        assertEquals("select female", "her book",
                MessageFormat.formatMessageNamedArgs(
                        Locale.US, msg, "gender", "female"));
        assertEquals("select male", "his book",
                MessageFormat.formatMessageNamedArgs(
                        Locale.US, msg, "gender", "male"));
        assertEquals("select neutral", "their book",
                MessageFormat.formatMessageNamedArgs(
                        Locale.US, msg, "gender", "unknown"));
    }

    public void TestPlural() {
        // Using Serbian, see
        // http://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html
        Locale sr = new Locale("sr");
        String msg =
                "{num,plural,offset:1 =1{only {name}}=2{{name} and one other}" +
                "one{{name} and #-one others}few{{name} and #-few others}" +
                "other{{name} and #... others}}";
        assertEquals("plural 1", "only Peter",
                MessageFormat.formatMessageNamedArgs(
                        sr, msg, "num", 1, "name", "Peter"));
        assertEquals("plural 2", "Paul and one other",
                MessageFormat.formatMessageNamedArgs(
                        sr, msg, "num", 2, "name", "Paul"));
        assertEquals("plural 22", "Mary and 21-one others",
                MessageFormat.formatMessageNamedArgs(
                        sr, msg, "num", 22, "name", "Mary"));
        assertEquals("plural 33", "John and 32-few others",
                MessageFormat.formatMessageNamedArgs(
                        sr, msg, "num", 33, "name", "John"));
        assertEquals("plural 6", "Yoko and 5... others",
                MessageFormat.formatMessageNamedArgs(
                        sr, msg, "num", 6, "name", "Yoko"));
    }

    public void TestSelectAndPlural() {
        Locale ja = Locale.JAPANESE;  // always "other"
        String msg =
                "{gender,select,female{" +
                        "{num,plural,=1{her book}other{her # books}}" +
                "}male{" +
                        "{num,plural,=1{his book}other{his # books}}" +
                "}other{" +
                        "{num,plural,=1{their book}other{their # books}}" +
                "}}";
        assertEquals("female 1", "her book",
                MessageFormat.formatMessageNamedArgs(
                        ja, msg, "gender", "female", "num", 1));
        assertEquals("male 2", "his 2 books",
                MessageFormat.formatMessageNamedArgs(
                        ja, msg, "gender", "male", "num", 2));
        assertEquals("unknown 3000", "their 3,000 books",
                MessageFormat.formatMessageNamedArgs(
                        ja, msg, "gender", "?", "num", 3000));
    }

    public void TestSelectOrdinal() {
        Locale en = Locale.ENGLISH;
        String msg =
                "{num,selectordinal,one{#st floor}two{#nd floor}few{#rd floor}other{#th floor}}";
        assertEquals("91", "91st floor",
                MessageFormat.formatMessageNamedArgs(
                        en, msg, "num", 91));
        assertEquals("22", "22nd floor",
                MessageFormat.formatMessageNamedArgs(
                        en, msg, "num", 22));
        assertEquals("33", "33rd floor",
                MessageFormat.formatMessageNamedArgs(
                        en, msg, "num", 33));
        assertEquals("11", "11th floor",
                MessageFormat.formatMessageNamedArgs(
                        en, msg, "num", 11));
    }
}
