/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.impl.stt.Environment;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.ExpertFactory;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.util.ULocale;

/**
 * Tests all plug-in extensions
 */
public class ExtensionsTest extends TestBase {

    int cntError;

    private Environment env = Environment.DEFAULT;
    private Environment envArabic = new Environment(new ULocale("ar"), false,
            BidiStructuredProcessor.Orientation.LTR);
    private Environment envHebrew = new Environment(new ULocale("he"), false,
            BidiStructuredProcessor.Orientation.LTR);

    private Expert expert;

    private void doTest1(String label, String data, String result) {
        String full = expert.leanToFullText(toUT16(data));
        cntError += assertEquals(label + " data = " + data, result,
                toPseudo(full));
    }

    private void doTest2(String label, String data, String result) {
        String full = expert.leanToFullText(data);
        cntError += assertEquals(label + " data = " + data, result,
                toPseudo(full));
    }

    private void doTest3(String label, String data, String result) {
        String full = expert.leanToFullText(toUT16(data));
        cntError += assertEquals(label + " data = " + data, result,
                toPseudo(full));
    }

    public static int main(String[] args) {
        String data;
        ExtensionsTest test = new ExtensionsTest();

        test.expert = ExpertFactory.getExpert(
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED, test.env);
        test.doTest1("Comma #1", "ab,cd, AB, CD, EFG", "ab,cd, AB@, CD@, EFG");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.EMAIL,
                test.env);
        test.doTest1("Email #1", "abc.DEF:GHI", "abc.DEF@:GHI");
        test.doTest1("Email #2", "DEF.GHI \"A.B\":JK ",
                "DEF@.GHI @\"A.B\"@:JK ");
        test.doTest1("Email #3", "DEF,GHI (A,B);JK ", "DEF@,GHI @(A,B)@;JK ");
        test.doTest1("Email #4", "DEF.GHI (A.B :JK ", "DEF@.GHI @(A.B :JK ");
        test.env = test.envArabic;
        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.EMAIL,
                test.env);
        test.doTest1("Email #5", "#EF.GHI \"A.B\":JK ",
                "<&#EF.GHI \"A.B\":JK &^");
        test.doTest1("Email #6", "#EF,GHI (A,B);JK ", "<&#EF,GHI (A,B);JK &^");
        test.doTest1("Email #7", "#EF.GHI (A.B :JK ", "<&#EF.GHI (A.B :JK &^");
        data = toUT16("peter.pan") + "@" + toUT16("#EF.GHI");
        test.doTest2("Email #8", data, "<&peter&.pan@#EF.GHI&^");
        test.env = test.envHebrew;
        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.EMAIL,
                test.env);
        data = toUT16("peter.pan") + "@" + toUT16("DEF.GHI");
        test.doTest2("Email #9", data, "peter.pan@DEF@.GHI");

        test.expert = ExpertFactory
                .getExpert(BidiStructuredProcessor.StructuredTypes.FILE, test.env);
        test.doTest1("File #1", "c:\\A\\B\\FILE.EXT", "c:\\A@\\B@\\FILE@.EXT");

        test.expert = ExpertFactory.getStatefulExpert(BidiStructuredProcessor.StructuredTypes.JAVA,
                test.env);
        test.doTest1("Java #1", "A = B + C;", "A@ = B@ + C;");
        test.doTest1("Java #2", "A   = B + C;", "A@   = B@ + C;");
        test.doTest1("Java #3", "A = \"B+C\"+D;", "A@ = \"B+C\"@+D;");
        test.doTest1("Java #4", "A = \"B+C+D;", "A@ = \"B+C+D;");
        test.doTest1("Java #5", "A = \"B\\\"C\"+D;", "A@ = \"B\\\"C\"@+D;");
        test.doTest1("Java #6", "A = /*B+C*/ D;", "A@ = /*B+C@*/ D;");
        test.doTest1("Java #7", "A = /*B+C* D;", "A@ = /*B+C* D;");
        test.doTest1("Java #8", "X+Y+Z */ B;  ", "X+Y+Z @*/ B;  ");
        test.doTest1("Java #9", "A = //B+C* D;", "A@ = //B+C* D;");
        test.doTest1("Java #10", "A = //B+C`|D+E;", "A@ = //B+C`|D@+E;");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.PROPERTY,
                test.env);
        test.doTest1("Property #0", "NAME,VAL1,VAL2", "NAME,VAL1,VAL2");
        test.doTest1("Property #1", "NAME=VAL1,VAL2", "NAME@=VAL1,VAL2");
        test.doTest1("Property #2", "NAME=VAL1,VAL2=VAL3",
                "NAME@=VAL1,VAL2=VAL3");

        test.expert = ExpertFactory.getStatefulExpert(
                BidiStructuredProcessor.StructuredTypes.REGEXP, test.env);
        data = toUT16("ABC(?") + "#" + toUT16("DEF)GHI");
        test.doTest2("Regex #0.0", data, "A@B@C@(?#DEF)@G@H@I");
        data = toUT16("ABC(?") + "#" + toUT16("DEF");
        test.doTest2("Regex #0.1", data, "A@B@C@(?#DEF");
        test.doTest1("Regex #0.2", "GHI)JKL", "GHI)@J@K@L");
        data = toUT16("ABC(?") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #1", data, "A@B@C@(?<DEF>@G@H@I");
        test.doTest1("Regex #2.0", "ABC(?'DEF'GHI", "A@B@C@(?'DEF'@G@H@I");
        test.doTest1("Regex #2.1", "ABC(?'DEFGHI", "A@B@C@(?'DEFGHI");
        data = toUT16("ABC(?(") + "<" + toUT16("DEF") + ">" + toUT16(")GHI");
        test.doTest2("Regex #3", data, "A@B@C@(?(<DEF>)@G@H@I");
        test.doTest1("Regex #4", "ABC(?('DEF')GHI", "A@B@C@(?('DEF')@G@H@I");
        test.doTest1("Regex #5", "ABC(?(DEF)GHI", "A@B@C@(?(DEF)@G@H@I");
        data = toUT16("ABC(?") + "&" + toUT16("DEF)GHI");
        test.doTest2("Regex #6", data, "A@B@C@(?&DEF)@G@H@I");
        data = toUT16("ABC(?") + "P<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #7", data, "A@B@C(?p<DEF>@G@H@I");
        data = toUT16("ABC\\k") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #8", data, "A@B@C\\k<DEF>@G@H@I");
        test.doTest1("Regex #9", "ABC\\k'DEF'GHI", "A@B@C\\k'DEF'@G@H@I");
        test.doTest1("Regex #10", "ABC\\k{DEF}GHI", "A@B@C\\k{DEF}@G@H@I");
        data = toUT16("ABC(?") + "P=" + toUT16("DEF)GHI");
        test.doTest2("Regex #11", data, "A@B@C(?p=DEF)@G@H@I");
        test.doTest1("Regex #12", "ABC\\g{DEF}GHI", "A@B@C\\g{DEF}@G@H@I");
        data = toUT16("ABC\\g") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #13", data, "A@B@C\\g<DEF>@G@H@I");
        test.doTest1("Regex #14", "ABC\\g'DEF'GHI", "A@B@C\\g'DEF'@G@H@I");
        data = toUT16("ABC(?(") + "R&" + toUT16("DEF)GHI");
        test.doTest2("Regex #15", data, "A@B@C(?(r&DEF)@G@H@I");
        data = toUT16("ABC") + "\\Q" + toUT16("DEF") + "\\E" + toUT16("GHI");
        test.doTest2("Regex #16.0", data, "A@B@C\\qDEF\\eG@H@I");
        data = toUT16("ABC") + "\\Q" + toUT16("DEF");
        test.doTest2("Regex #16.1", data, "A@B@C\\qDEF");
        data = toUT16("GHI") + "\\E" + toUT16("JKL");
        test.doTest2("Regex #16.2", data, "GHI\\eJ@K@L");
        test.doTest1("Regex #17.0", "abc[d-h]ijk", "abc[d-h]ijk");
        test.doTest1("Regex #17.1", "aBc[d-H]iJk", "aBc[d-H]iJk");
        test.doTest1("Regex #17.2", "aB*[!-H]iJ2", "aB*[!-@H]iJ@2");
        test.doTest1("Regex #17.3", "aB*[1-2]J3", "aB*[@1-2]J@3");
        test.doTest1("Regex #17.4", "aB*[5-6]J3", "aB*[@5-@6]@J@3");
        test.doTest1("Regex #17.5", "a*[5-6]J3", "a*[5-@6]@J@3");
        test.doTest1("Regex #17.6", "aB*123", "aB*@123");
        test.doTest1("Regex #17.7", "aB*567", "aB*@567");

        test.env = test.envArabic;
        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.REGEXP,
                test.env);
        data = toUT16("#BC(?") + "#" + toUT16("DEF)GHI");
        test.doTest2("Regex #0.0", data, "<&#BC(?#DEF)GHI&^");
        data = toUT16("#BC(?") + "#" + toUT16("DEF");
        test.doTest2("Regex #0.1", data, "<&#BC(?#DEF&^");
        test.doTest1("Regex #0.2", "#HI)JKL", "<&#HI)JKL&^");
        data = toUT16("#BC(?") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #1", data, "<&#BC(?<DEF>GHI&^");
        test.doTest1("Regex #2.0", "#BC(?'DEF'GHI", "<&#BC(?'DEF'GHI&^");
        test.doTest1("Regex #2.1", "#BC(?'DEFGHI", "<&#BC(?'DEFGHI&^");
        data = toUT16("#BC(?(") + "<" + toUT16("DEF") + ">" + toUT16(")GHI");
        test.doTest2("Regex #3", data, "<&#BC(?(<DEF>)GHI&^");
        test.doTest1("Regex #4", "#BC(?('DEF')GHI", "<&#BC(?('DEF')GHI&^");
        test.doTest1("Regex #5", "#BC(?(DEF)GHI", "<&#BC(?(DEF)GHI&^");
        data = toUT16("#BC(?") + "&" + toUT16("DEF)GHI");
        test.doTest2("Regex #6", data, "<&#BC(?&DEF)GHI&^");
        data = toUT16("#BC(?") + "P<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #7", data, "<&#BC(?p<DEF>GHI&^");
        data = toUT16("#BC\\k") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #8", data, "<&#BC\\k<DEF>GHI&^");
        test.doTest1("Regex #9", "#BC\\k'DEF'GHI", "<&#BC\\k'DEF'GHI&^");
        test.doTest1("Regex #10", "#BC\\k{DEF}GHI", "<&#BC\\k{DEF}GHI&^");
        data = toUT16("#BC(?") + "P=" + toUT16("DEF)GHI");
        test.doTest2("Regex #11", data, "<&#BC(?p=DEF)GHI&^");
        test.doTest1("Regex #12", "#BC\\g{DEF}GHI", "<&#BC\\g{DEF}GHI&^");
        data = toUT16("#BC\\g") + "<" + toUT16("DEF") + ">" + toUT16("GHI");
        test.doTest2("Regex #13", data, "<&#BC\\g<DEF>GHI&^");
        test.doTest1("Regex #14", "#BC\\g'DEF'GHI", "<&#BC\\g'DEF'GHI&^");
        data = toUT16("#BC(?(") + "R&" + toUT16("DEF)GHI");
        test.doTest2("Regex #15", data, "<&#BC(?(r&DEF)GHI&^");
        data = toUT16("#BC") + "\\Q" + toUT16("DEF") + "\\E" + toUT16("GHI");
        test.doTest2("Regex #16.0", data, "<&#BC\\qDEF\\eGHI&^");
        data = toUT16("#BC") + "\\Q" + toUT16("DEF");
        test.doTest2("Regex #16.1", data, "<&#BC\\qDEF&^");
        data = toUT16("#HI") + "\\E" + toUT16("JKL");
        test.doTest2("Regex #16.2", data, "<&#HI\\eJKL&^");
        test.env = test.envHebrew;

        test.expert = ExpertFactory.getStatefulExpert(BidiStructuredProcessor.StructuredTypes.SQL,
                test.env);
        test.doTest1("SQL #0", "abc GHI", "abc GHI");
        test.doTest1("SQL #1", "abc DEF   GHI", "abc DEF@   GHI");
        test.doTest1("SQL #2", "ABC, DEF,   GHI", "ABC@, DEF@,   GHI");
        test.doTest1("SQL #3", "ABC'DEF GHI' JKL,MN", "ABC@'DEF GHI'@ JKL@,MN");
        test.doTest1("SQL #4.0", "ABC'DEF GHI JKL", "ABC@'DEF GHI JKL");
        test.doTest1("SQL #4.1", "MNO PQ' RS,TUV", "MNO PQ'@ RS@,TUV");
        test.doTest1("SQL #5", "ABC\"DEF GHI\" JKL,MN",
                "ABC@\"DEF GHI\"@ JKL@,MN");
        test.doTest1("SQL #6", "ABC\"DEF GHI JKL", "ABC@\"DEF GHI JKL");
        test.doTest1("SQL #7", "ABC/*DEF GHI*/ JKL,MN",
                "ABC@/*DEF GHI@*/ JKL@,MN");
        test.doTest1("SQL #8.0", "ABC/*DEF GHI JKL", "ABC@/*DEF GHI JKL");
        test.doTest1("SQL #8.1", "MNO PQ*/RS,TUV", "MNO PQ@*/RS@,TUV");
        test.doTest1("SQL #9", "ABC--DEF GHI JKL", "ABC@--DEF GHI JKL");
        test.doTest1("SQL #10", "ABC--DEF--GHI,JKL", "ABC@--DEF--GHI,JKL");
        test.doTest1("SQL #11", "ABC'DEF '' G I' JKL,MN",
                "ABC@'DEF '' G I'@ JKL@,MN");
        test.doTest1("SQL #12", "ABC\"DEF \"\" G I\" JKL,MN",
                "ABC@\"DEF \"\" G I\"@ JKL@,MN");
        test.doTest1("SQL #13", "ABC--DEF GHI`|JKL MN",
                "ABC@--DEF GHI`|JKL@ MN");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.SYSTEM_USER,
                test.env);
        test.doTest1("System #1", "HOST(JACK)", "HOST@(JACK)");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.UNDERSCORE,
                test.env);
        test.doTest1("Underscore #1", "A_B_C_d_e_F_G", "A@_B@_C_d_e_F@_G");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.URL, test.env);
        test.doTest1("URL #1", "WWW.DOMAIN.COM/DIR1/DIR2/dir3/DIR4",
                "WWW@.DOMAIN@.COM@/DIR1@/DIR2/dir3/DIR4");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.XPATH,
                test.env);
        test.doTest1("Xpath #1", "abc(DEF)GHI", "abc(DEF@)GHI");
        test.doTest1("Xpath #2", "DEF.GHI \"A.B\":JK ",
                "DEF@.GHI@ \"A.B\"@:JK ");
        test.doTest1("Xpath #3", "DEF!GHI 'A!B'=JK ", "DEF@!GHI@ 'A!B'@=JK ");
        test.doTest1("Xpath #4", "DEF.GHI 'A.B :JK ", "DEF@.GHI@ 'A.B :JK ");

        test.expert = ExpertFactory.getExpert(BidiStructuredProcessor.StructuredTypes.EMAIL,
                test.env);
        test.doTest3("DelimsEsc #1", "abc.DEF.GHI", "abc.DEF@.GHI");
        test.doTest3("DelimsEsc #2", "DEF.GHI (A:B);JK ",
                "DEF@.GHI @(A:B)@;JK ");
        test.doTest3("DelimsEsc #3", "DEF.GHI (A:B);JK ",
                "DEF@.GHI @(A:B)@;JK ");
        test.doTest3("DelimsEsc #4", "DEF.GHI (A:B\\):C) ;JK ",
                "DEF@.GHI @(A:B\\):C) @;JK ");
        test.doTest3("DelimsEsc #5", "DEF.GHI (A\\\\\\):C) ;JK ",
                "DEF@.GHI @(A\\\\\\):C) @;JK ");
        test.doTest3("DelimsEsc #6", "DEF.GHI (A\\\\):C ;JK ",
                "DEF@.GHI @(A\\\\)@:C @;JK ");
        test.doTest3("DelimsEsc #7", "DEF.GHI (A\\):C ;JK ",
                "DEF@.GHI @(A\\):C ;JK ");

        return test.cntError;
    }
}
