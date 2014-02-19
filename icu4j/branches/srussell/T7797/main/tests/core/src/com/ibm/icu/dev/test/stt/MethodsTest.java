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
import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.text.BidiStructuredProcessor.Orientation;

// NOTE: due to having to change the state engine this test is rendered useless. Left here for reference. 

/**
 * Tests most public methods of BidiComplexEngine
 */
public class MethodsTest extends TestBase {

    private final static int LTR = Bidi.DIRECTION_LEFT_TO_RIGHT;
    private final static int RTL = Bidi.DIRECTION_RIGHT_TO_LEFT;
    private final static Environment envLTR = new Environment(null, false,
            BidiStructuredProcessor.Orientation.LTR);
    private final static Environment envRTL = new Environment(null, false,
            BidiStructuredProcessor.Orientation.RTL);
    private final static Environment envRTLMIR = new Environment(null, true,
            BidiStructuredProcessor.Orientation.RTL);
    private final static Environment envIGN = new Environment(null, false,
            BidiStructuredProcessor.Orientation.IGNORE);
    private final static Environment envCLR = new Environment(null, false,
            BidiStructuredProcessor.Orientation.CONTEXTUAL_LTR);
    private final static Environment envCRL = new Environment(null, false,
            BidiStructuredProcessor.Orientation.CONTEXTUAL_RTL);
    private final static TestHandlerMyComma testMyCommaLL = new TestHandlerMyComma(
            BidiStructuredProcessor.Orientation.LTR, BidiStructuredProcessor.Orientation.LTR);
    private final static TestHandlerMyComma testMyCommaRR = new TestHandlerMyComma(
            BidiStructuredProcessor.Orientation.RTL, BidiStructuredProcessor.Orientation.RTL);
    private final static TestHandlerMyComma testMyCommaRL = new TestHandlerMyComma(
            BidiStructuredProcessor.Orientation.RTL, BidiStructuredProcessor.Orientation.LTR);

    int cntError;

    private static class TestHandlerMyComma extends TypeHandler {

        private final static byte AL = Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;

        final BidiStructuredProcessor.Orientation dirOne;
        final BidiStructuredProcessor.Orientation dirTwo;

        public TestHandlerMyComma(BidiStructuredProcessor.Orientation dir1, BidiStructuredProcessor.Orientation dir2) {
            this.dirOne = dir1;
            this.dirTwo = dir2;
        }

        public String getSeparators(Expert expert) {
            return ","; //$NON-NLS-1$
        }

        public boolean skipProcessing(Expert expert, String text,
                CharTypes charTypes) {
            byte charType = charTypes.getBidiTypeAt(0);
            if (charType == AL)
                return true;
            return false;
        }

        public Orientation getDirection(Expert expert, String text) {
            return getDirection(expert, text, new CharTypes(expert, text));
        }

        public Orientation getDirection(Expert expert, String text, CharTypes charTypes) {
            for (int i = 0; i < text.length(); i++) {
                byte charType = charTypes.getBidiTypeAt(i);
                if (charType == AL)
                    return dirOne;
            }
            return dirTwo;
        }
    }

    private void doTestTools() {

        // This method tests utility methods used by the JUnits
        String data = "56789ABCDEFGHIJKLMNOPQRSTUVWXYZ~#@&><^|`";
        String text = toUT16(data);
        String dat2 = toPseudo(text);
        cntError += assertEquals("", data, dat2);

        text = toPseudo(data);
        cntError += assertEquals("",
                "56789abcdefghijklmnopqrstuvwxyz~#@&><^|`", text);

        text = array_display(null);
        cntError += assertEquals("", "null", text);
    }

    private void doTestState() {
        String data = null, lean = null, full = null, model = null;
        Expert expert;
        //expert = ExpertFactory.getStatefulExpert(BidiStructuredProcessor.StructuredTypes.JAVA);

        data = "A=B+C;/* D=E+F;";
        lean = toUT16(data);
        //full = expert.leanToFullText(lean);
        model = "A@=B@+C@;/* D=E+F;";
        cntError += assertEquals("full1", model, toPseudo(full));

        data = "A=B+C; D=E+F;";
        lean = toUT16(data);
        //full = expert.leanToFullText(lean);
        model = data;
        cntError += assertEquals("full2", model, toPseudo(full));

        data = "SOME MORE COMMENTS";
        lean = toUT16(data);
        //full = expert.leanToFullText(lean);
        model = data;
        cntError += assertEquals("full3", model, toPseudo(full));

        data = "A=B+C;*/ D=E+F;";
        lean = toUT16(data);
        //full = expert.leanToFullText(lean);
        model = "A=B+C;@*/ D@=E@+F;";
        cntError += assertEquals("full4", model, toPseudo(full));
    }

    private void doTestOrientation() {
        Orientation orient = Environment.DEFAULT.getOrientation();
        cntError += assertEquals("orient #1", BidiStructuredProcessor.Orientation.LTR, orient);

        orient = envIGN.getOrientation();
        cntError += assertEquals("orient #2", BidiStructuredProcessor.Orientation.IGNORE, orient);

        orient = envCRL.getOrientation();
        cntError += assertEquals("orient #3",
                BidiStructuredProcessor.Orientation.CONTEXTUAL_RTL, orient);

        //orient = envERR.getOrientation();
        //cntError += assertEquals("orient #4", Environment.ORIENT_UNKNOWN,
        //        orient);
    }

    private void doTestOrient(TypeHandler handler, String label, String data,
            String resLTR, String resRTL, String resCon) {
        String full, lean;

        Expert expertLTR = null;
        // expertLTR = ExpertFactory.getStatefulExpert(handler, envLTR);
        Expert expertRTL = null;
        //expertRTL = ExpertFactory.getStatefulExpert(handler, envRTL);
        Expert expertCRL = null;
        //expertCRL = ExpertFactory.getStatefulExpert(handler, envCRL);

        lean = toUT16(data);
        full = expertLTR.leanToFullText(lean);
        cntError += assertEquals(label + "LTR full", resLTR, toPseudo(full));
        full = expertRTL.leanToFullText(lean);
        cntError += assertEquals("label + RTL full", resRTL, toPseudo(full));
        full = expertCRL.leanToFullText(lean);
        cntError += assertEquals(label + "CON full", resCon, toPseudo(full));
    }

    private void doTestSkipProcessing() {
        doTestOrient(testMyCommaLL, "Skip #1 ", "BCD,EF", "BCD@,EF",
                ">@BCD@,EF@^", "@BCD@,EF");
        doTestOrient(testMyCommaLL, "Skip #2 ", "#CD,EF", "#CD,EF",
                ">@#CD,EF@^", "@#CD,EF");
    }

    private void doTestLeanOffsets() {
        String lean, data, label;
        Expert expert = null;
        //expert = ExpertFactory.getStatefulExpert(BidiStructuredProcessor.StructuredTypes.JAVA);

        int[] offsets;
        int[] model;

        data = "A=B+C;/* D=E+F;";
        lean = toUT16(data);
        offsets = expert.leanBidiCharOffsets(lean);
        model = new int[] { 1, 3, 5 };
        label = "leanBidiCharOffsets() #1 ";
        cntError += assertEquals(label, array_display(model),
                array_display(offsets));
        data = "A=B+C;*/ D=E+F;";
        lean = toUT16(data);
        offsets = expert.leanBidiCharOffsets(lean);
        model = new int[] { 6, 10, 12 };
        label = "leanBidiCharOffsets() #2 ";
        cntError += assertEquals(label, array_display(model),
                array_display(offsets));
    }

    private void doTestFullOffsets(String label, String data, int[] resLTR,
            int[] resRTL, int[] resCon) {
        String full, lean, msg;
        int[] offsets;
        Expert expertLTR = ExpertFactory.getExpert(
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED, envLTR);
        Expert expertRTL = ExpertFactory.getExpert(
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED, envRTL);
        Expert expertCLR = ExpertFactory.getExpert(
                BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED, envCLR);

        lean = toUT16(data);
        full = expertLTR.leanToFullText(lean);
        offsets = expertLTR.fullBidiCharOffsets(full);
        msg = label + "LTR ";
        cntError += assertEquals(msg, array_display(resLTR),
                array_display(offsets));
        full = expertRTL.leanToFullText(lean);
        offsets = expertRTL.fullBidiCharOffsets(full);
        msg = label + "RTL ";
        cntError += assertEquals(msg, array_display(resRTL),
                array_display(offsets));
        full = expertCLR.leanToFullText(lean);
        offsets = expertCLR.fullBidiCharOffsets(full);
        msg = label + "CON ";
        cntError += assertEquals(msg, array_display(resCon),
                array_display(offsets));
    }

    private void doTestMirrored() {
        boolean mirrored;
        mirrored = Environment.DEFAULT.getMirrored();
        cntError += assertFalse("mirrored #1", mirrored);
        Environment env = new Environment(null, true, BidiStructuredProcessor.Orientation.LTR);
        mirrored = env.getMirrored();
        cntError += assertTrue("mirrored #2", mirrored);
    }

    private void doTestDirection() {
        String data, lean, full, model;
        Orientation dirA;
        Orientation dirH;
        Expert expertRL = null;
        //expertRL = ExpertFactory.getStatefulExpert(testMyCommaRL, envLTR);
        dirA = expertRL.getTextDirection(toUT16("###"));
        dirH = expertRL.getTextDirection(toUT16("ABC"));
        cntError += assertTrue("TestDirection #1", dirA.isRtl() && dirH.isLtr());

        Expert expertRR = null;
        //ExpertFactory.getStatefulExpert(testMyCommaRR, envLTR);
        dirA = expertRR.getTextDirection(toUT16("###"));
        dirH = expertRR.getTextDirection(toUT16("ABC"));
        cntError += assertTrue("TestDirection #2", dirA.isRtl() && dirH.isRtl());

        Expert expertLL = null;
        //expertLL = ExpertFactory.getStatefulExpert(testMyCommaLL, envLTR);
        lean = toUT16("ABC,#DEF,HOST,com");
        full = expertLL.leanToFullText(lean);
        cntError += assertEquals("TestDirection #9 full",
                "ABC@,#DEF@,HOST,com", toPseudo(full));

        lean = toUT16("ABC,DEF,HOST,com");
        full = expertLL.leanToFullText(lean);

        cntError += assertEquals("TestDirection #10 full",
                "ABC@,DEF@,HOST,com", toPseudo(full));

        Environment environment = new Environment(null, true,
                BidiStructuredProcessor.Orientation.LTR);
        Expert expert = null;
        //expert = ExpertFactory.getStatefulExpert(testMyCommaRL,environment);
        dirA = expert.getTextDirection(toUT16("###"));
        dirH = expert.getTextDirection(toUT16("ABC"));
        cntError += assertTrue("TestDirection #10.5", dirA.isRtl()
                && dirH.isLtr());

        lean = toUT16("ABC,#DEF,HOST,com");
        full = expert.leanToFullText(lean);
        cntError += assertEquals("TestDirection #11 full",
                "<&ABC,#DEF,HOST,com&^", toPseudo(full));

        data = "ABc,#DEF,HOSt,COM";
        lean = toUT16(data);
        full = expert.leanToFullText(lean);
        model = "<&ABc,#DEF,HOSt,COM&^";
        cntError += assertEquals("TestDirection #12 full", model,
                toPseudo(full));

        data = "ABc,#DEF,HOSt,";
        lean = toUT16(data);
        full = expert.leanToFullText(lean);
        model = "<&ABc,#DEF,HOSt,&^";
        cntError += assertEquals("TestDirection #13 full", model,
                toPseudo(full));

        data = "ABC,DEF,HOST,com";
        lean = toUT16(data);
        full = expert.leanToFullText(lean);
        model = "ABC@,DEF@,HOST,com";
        cntError += assertEquals("TestDirection #14 full", model,
                toPseudo(full));

        data = "--,---,----";
        lean = toUT16(data);
        full = expert.leanToFullText(lean);
        model = "--,---,----";
        cntError += assertEquals("TestDirection #15 full", model,
                toPseudo(full));

        data = "ABC,|DEF,HOST,com";
        lean = toUT16(data);
        full = expert.leanToFullText(lean);

        model = "ABC,|DEF@,HOST,com";
        cntError += assertEquals("TestDirection #16 full", model,
                toPseudo(full));

        data = "ABc,|#DEF,HOST,com";
        lean = toUT16(data);
        //expert = ExpertFactory.getStatefulExpert(testMyCommaRL, envRTLMIR);
        full = expert.leanToFullText(lean);
        model = "ABc,|#DEF,HOST,com";
        cntError += assertEquals("TestDirection #17 full", model,
                toPseudo(full));
        Orientation dir = expert.getTextDirection(lean);
        cntError += assertEquals("Test curDirection", Orientation.RTL, dir);
    }

    public static int main(String[] args) {

        MethodsTest test = new MethodsTest();

        test.doTestTools();

        test.doTestState();

        test.doTestOrientation();

        TypeHandler commaHandler = BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED.getInstance();
        test.doTestOrient(commaHandler, "Methods #1 ", "", "", "", "");
        test.doTestOrient(commaHandler, "Methods #2 ", "abc", "abc", ">@abc@^",
                "abc");
        test.doTestOrient(commaHandler, "Methods #3 ", "ABC", "ABC", ">@ABC@^",
                "@ABC");
        test.doTestOrient(commaHandler, "Methods #4 ", "bcd,ef", "bcd,ef",
                ">@bcd,ef@^", "bcd,ef");
        test.doTestOrient(commaHandler, "Methods #5 ", "BCD,EF", "BCD@,EF",
                ">@BCD@,EF@^", "@BCD@,EF");
        test.doTestOrient(commaHandler, "Methods #6 ", "cde,FG", "cde,FG",
                ">@cde,FG@^", "cde,FG");
        test.doTestOrient(commaHandler, "Methods #7 ", "CDE,fg", "CDE,fg",
                ">@CDE,fg@^", "@CDE,fg");
        test.doTestOrient(commaHandler, "Methods #8 ", "12..def,GH",
                "12..def,GH", ">@12..def,GH@^", "12..def,GH");
        test.doTestOrient(commaHandler, "Methods #9 ", "34..DEF,gh",
                "34..DEF,gh", ">@34..DEF,gh@^", "@34..DEF,gh");

        test.doTestSkipProcessing();

        test.doTestLeanOffsets();

        test.doTestFullOffsets("TestFullOffsets ", "BCD,EF,G",
                new int[] { 3, 7 }, new int[] { 0, 1, 5, 9, 12, 13 },
                new int[] { 0, 4, 8 });

        test.doTestMirrored();

        test.doTestDirection();

        Expert expert = ExpertFactory
                .getExpert(BidiStructuredProcessor.StructuredTypes.COMMA_DELIMITED);
        String data = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
        String lean = toUT16(data);
        String full = expert.leanToFullText(lean);
        String model = "A@,B@,C@,D@,E@,F@,G@,H@,I@,J@,K@,L@,M@,N@,O@,P@,Q@,R@,S@,T@,U@,V@,W@,X@,Y@,Z";
        test.cntError += assertEquals("many inserts", model, toPseudo(full));

        return test.cntError;
    }
}