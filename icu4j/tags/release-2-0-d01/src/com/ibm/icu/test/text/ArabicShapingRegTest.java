/*
*******************************************************************************
*   Copyright (C) 2001, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package com.ibm.icu.test.text;

import com.ibm.test.TestFmwk;
import com.ibm.text.ArabicShaping;
import com.ibm.text.ArabicShapingException;

/**
 * Regression test for Arabic shaping.
 */
public class ArabicShapingRegTest extends TestFmwk {

    /* constants copied from ArabicShaping for convenience */

    public static final int LENGTH_GROW_SHRINK = 0;
    public static final int LENGTH_FIXED_SPACES_NEAR = 1;
    public static final int LENGTH_FIXED_SPACES_AT_END = 2;
    public static final int LENGTH_FIXED_SPACES_AT_BEGINNING = 3;

    public static final int TEXT_DIRECTION_LOGICAL = 0;
    public static final int TEXT_DIRECTION_VISUAL_LTR = 4;

    public static final int LETTERS_NOOP = 0;
    public static final int LETTERS_SHAPE = 8;
    public static final int LETTERS_SHAPE_TASHKEEL_ISOLATED = 0x18;
    public static final int LETTERS_UNSHAPE = 0x10;

    public static final int DIGITS_NOOP = 0;
    public static final int DIGITS_EN2AN = 0x20;
    public static final int DIGITS_AN2EN = 0x40;
    public static final int DIGITS_EN2AN_INIT_LR = 0x60;
    public static final int DIGITS_EN2AN_INIT_AL = 0x80;
    private static final int DIGITS_RESERVED = 0xa0;

    public static final int DIGIT_TYPE_AN = 0;
    public static final int DIGIT_TYPE_AN_EXTENDED = 0x100;

    public static class TestData {
        public int type;
        public String source;
        public int flags;
        public String result;
        public int length;
        public Class error;

        public static final int STANDARD = 0;
        public static final int PREFLIGHT = 1;
        public static final int ERROR = 2;

        public static TestData standard(String source, int flags, String result) {
            return new TestData(STANDARD, source, flags, result, 0, null);
        }
        
        public static TestData preflight(String source, int flags, int length) {
            return new TestData(PREFLIGHT, source, flags, null, length, null);
        }

        public static TestData error(String source, int flags, Class error) {
            return new TestData(ERROR, source, flags, null, 0, error);
        }

        private TestData(int type, String source, int flags, String result, int length, Class error) {
            this.type = type;
            this.source = source;
            this.flags = flags;
            this.result = result;
            this.length = length;
            this.error = error;
        }

        private static final String[] typenames = { "standard", "preflight", "error" };

        public String toString() {
            StringBuffer buf = new StringBuffer(super.toString());
            buf.append("[\n");
            buf.append(typenames[type]);
            buf.append(",\n");
            if (source == null) {
                buf.append("null");
            } else {
                buf.append('"');
                buf.append(escapedString(source));
                buf.append('"');
            }
            buf.append(",\n");
            buf.append(Integer.toHexString(flags));
            buf.append(",\n");
            if (result == null) {
                buf.append("null");
            } else {
                buf.append('"');
                buf.append(escapedString(result));
                buf.append('"');
            }
            buf.append(",\n");
            buf.append(length);
            buf.append(",\n");
            buf.append(error);
            buf.append(']');
            return buf.toString();
        }
    }

    private static final String lamAlefSpecialVLTR =
        "\u0020\u0646\u0622\u0644\u0627\u0020" +
     	"\u0646\u0623\u064E\u0644\u0627\u0020" +
     	"\u0646\u0627\u0670\u0644\u0627\u0020" +
     	"\u0646\u0622\u0653\u0644\u0627\u0020" +
     	"\u0646\u0625\u0655\u0644\u0627\u0020" +
     	"\u0646\u0622\u0654\u0644\u0627\u0020" +
     	"\uFEFC\u0639";

    private static final String tashkeelSpecialVLTR = 
        "\u064A\u0628\u0631\u0639\u0020" +
        "\u064A\u0628\u0651\u0631\u064E\u0639\u0020" +
        "\u064C\u064A\u0628\u0631\u064F\u0639\u0020" +
        "\u0628\u0670\u0631\u0670\u0639\u0020" +
        "\u0628\u0653\u0631\u0653\u0639\u0020" +
        "\u0628\u0654\u0631\u0654\u0639\u0020" +
        "\u0628\u0655\u0631\u0655\u0639\u0020";

    private static final String logicalUnshape = 
        "\u0020\u0020\u0020\uFE8D\uFEF5\u0020\uFEE5\u0020\uFE8D\uFEF7\u0020" +
        "\uFED7\uFEFC\u0020\uFEE1\u0020\uFE8D\uFEDF\uFECC\uFEAE\uFE91\uFEF4" +
        "\uFE94\u0020\uFE8D\uFEDF\uFEA4\uFEAE\uFE93\u0020\u0020\u0020\u0020";

    private static final String numSource =
        "\u0031" +  /* en:1 */
        "\u0627" +  /* arabic:alef */
        "\u0032" +  /* en:2 */
        "\u06f3" +  /* an:3 */
        "\u0061" +  /* latin:a */
        "\u0034";   /* en:4 */

    private static final TestData[] tests = {
        /* lam alef special visual ltr */
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\ufee5\u0020\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\ufe76\ufef7\ufe8d\u0020" +
                          "\ufee5\u0020\u0670\ufefb\ufe8d\u0020" +
                          "\ufee5\u0020\u0653\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\u0655\ufef9\ufe8d\u0020" +
                          "\ufee5\u0020\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb\u0020\u0020\u0020\u0020" +
                          "\u0020\u0020"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0020\u0020\u0020\u0020\u0020\u0020" +
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_GROW_SHRINK,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),

        /* TASHKEEL */
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\ufee5\u0020\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\ufe76\ufef7\ufe8d\u0020" +
                          "\ufee5\u0020\u0670\ufefb\ufe8d\u0020" +
                          "\ufee5\u0020\u0653\ufef5\ufe8d\u0020" +
                          "\ufee5\u0020\u0655\ufef9\ufe8d\u0020" +
                          "\ufee5\u0020\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb\u0020\u0020\u0020\u0020" +
                          "\u0020\u0020"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0020\u0020\u0020\u0020\u0020\u0020" +
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),
        TestData.standard(lamAlefSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_GROW_SHRINK,
                          "\u0020\ufee5\ufef5\ufe8d\u0020\ufee5" +
                          "\ufe76\ufef7\ufe8d\u0020\ufee5\u0670" +
                          "\ufefb\ufe8d\u0020\ufee5\u0653\ufef5" +
                          "\ufe8d\u0020\ufee5\u0655\ufef9\ufe8d" +
                          "\u0020\ufee5\u0654\ufef5\ufe8d\u0020" +
                          "\ufefc\ufecb"),

        /* tashkeel special visual ltr */
        TestData.standard(tashkeelSpecialVLTR,
                          LETTERS_SHAPE | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\ufef2\ufe91\ufeae\ufecb\u0020" +
                          "\ufef2\ufe91\ufe7c\ufeae\ufe77\ufecb\u0020" +
                          "\ufe72\ufef2\ufe91\ufeae\ufe79\ufecb\u0020" +
                          "\ufe8f\u0670\ufeae\u0670\ufecb\u0020" +
                          "\ufe8f\u0653\ufeae\u0653\ufecb\u0020" +
                          "\ufe8f\u0654\ufeae\u0654\ufecb\u0020" +
                          "\ufe8f\u0655\ufeae\u0655\ufecb\u0020"),

        TestData.standard(tashkeelSpecialVLTR,
                          LETTERS_SHAPE_TASHKEEL_ISOLATED | TEXT_DIRECTION_VISUAL_LTR | LENGTH_FIXED_SPACES_NEAR,
                          "\ufef2\ufe91\ufeae\ufecb\u0020" +
                          "\ufef2\ufe91\ufe7c\ufeae\ufe76\ufecb\u0020" +
                          "\ufe72\ufef2\ufe91\ufeae\ufe78\ufecb\u0020" +
                          "\ufe8f\u0670\ufeae\u0670\ufecb\u0020" +
                          "\ufe8f\u0653\ufeae\u0653\ufecb\u0020" +
                          "\ufe8f\u0654\ufeae\u0654\ufecb\u0020" +
                          "\ufe8f\u0655\ufeae\u0655\ufecb\u0020"),

        /* logical unshape */
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_NEAR,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0646\u0020\u0627\u0644\u0623\u0642\u0644\u0627" +
                          "\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627\u0644\u062d\u0631" +
                          "\u0629\u0020\u0020\u0020\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_AT_END,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642" +
                          "\u0644\u0627\u0020\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627" +
                          "\u0644\u062d\u0631\u0629\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_FIXED_SPACES_AT_BEGINNING,
                          "\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642\u0644\u0627\u0020" +
                          "\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627\u0644\u062d\u0631" +
                          "\u0629\u0020\u0020\u0020\u0020"),
        TestData.standard(logicalUnshape,
                          LETTERS_UNSHAPE | TEXT_DIRECTION_LOGICAL | LENGTH_GROW_SHRINK,
                          "\u0020\u0020\u0020\u0627\u0644\u0622\u0020\u0646\u0020\u0627\u0644\u0623\u0020\u0642" +
                          "\u0644\u0627\u0020\u0645\u0020\u0627\u0644\u0639\u0631\u0628\u064a\u0629\u0020\u0627" +
                          "\u0644\u062d\u0631\u0629\u0020\u0020\u0020\u0020"),

        /* numbers */
        TestData.standard(numSource, 
                          DIGITS_EN2AN | DIGIT_TYPE_AN, 
                          "\u0661\u0627\u0662\u06f3\u0061\u0664"),
        TestData.standard(numSource, 
                          DIGITS_AN2EN | DIGIT_TYPE_AN_EXTENDED, 
                          "\u0031\u0627\u0032\u0033\u0061\u0034"),
        TestData.standard(numSource, 
                          DIGITS_EN2AN_INIT_LR | DIGIT_TYPE_AN, 
                          "\u0031\u0627\u0662\u06f3\u0061\u0034"),
        TestData.standard(numSource, 
                          DIGITS_EN2AN_INIT_AL | DIGIT_TYPE_AN_EXTENDED, 
                          "\u06f1\u0627\u06f2\u06f3\u0061\u0034"),
        TestData.standard(numSource, 
                          DIGITS_EN2AN_INIT_LR | DIGIT_TYPE_AN | TEXT_DIRECTION_VISUAL_LTR,
                          "\u0661\u0627\u0032\u06f3\u0061\u0034"),
        TestData.standard(numSource, 
                          DIGITS_EN2AN_INIT_AL | DIGIT_TYPE_AN_EXTENDED | TEXT_DIRECTION_VISUAL_LTR,
                          "\u06f1\u0627\u0032\u06f3\u0061\u06f4"),

        /* no-op */
        TestData.standard(numSource, 
                          0,
                          numSource),

        /* preflight */
        TestData.preflight("\u0644\u0627",
                           LETTERS_SHAPE | LENGTH_GROW_SHRINK,
                           1),

        TestData.preflight("\u0644\u0627\u0031",
                           DIGITS_EN2AN | DIGIT_TYPE_AN_EXTENDED | LENGTH_GROW_SHRINK,
                           3),

        TestData.preflight("\u0644\u0644",
                           LETTERS_SHAPE | LENGTH_GROW_SHRINK,
                           2),

        TestData.preflight("\ufef7",
                           LETTERS_UNSHAPE | LENGTH_GROW_SHRINK,
                           2),

        /* bad data */
        TestData.error("\u0020\ufef7\u0644\u0020",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_NEAR,
                       ArabicShapingException.class),

        TestData.error("\u0020\ufef7",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_AT_END,
                       ArabicShapingException.class),

        TestData.error("\ufef7\u0020",
                       LETTERS_UNSHAPE | LENGTH_FIXED_SPACES_AT_BEGINNING,
                       ArabicShapingException.class),

        /* bad options */
        TestData.error("\ufef7",
                       0xffffffff,
                       IllegalArgumentException.class),
    };

    public void testStandard() {
        for (int i = 0; i < tests.length; ++i) {
            TestData test = tests[i];

            Exception ex = null;
            String result = null;
            ArabicShaping shaper = null;
            try {
                shaper = new ArabicShaping(test.flags);
                result = shaper.shape(test.source);
            }
            catch (Exception e) {
                ex = e;
            }
            
            switch (test.type) {
            case TestData.STANDARD:
                if (!test.result.equals(result)) {
                    reportTestFailure(i, test, shaper, result, ex);
                }
                break;
                
            case TestData.PREFLIGHT:
                if (result == null || test.length != result.length()) {
                    reportTestFailure(i, test, shaper, result, ex);
                }
                break;

            case TestData.ERROR:
                if (!test.error.isInstance(ex)) {
                    reportTestFailure(i, test, shaper, result, ex);
                }
                break;
            }
        }                    
    }

    public void reportTestFailure(int index, TestData test, ArabicShaping shaper, String result, Exception error) {
        StringBuffer buf = new StringBuffer();
        buf.append("*** test failure ***\n");
        buf.append("index: " + index + "\n");
        buf.append("test: " + test + "\n");
        buf.append("shaper: " + shaper + "\n");
        buf.append("result: " + escapedString(result) + "\n");
        buf.append("error: " + error + "\n");

        if (result != null && test.result != null && !test.result.equals(result)) {
            for (int i = 0; i < Math.max(test.result.length(), result.length()); ++i) {
                String temp = Integer.toString(i);
                if (temp.length() < 2) {
                    temp = " ".concat(temp);
                }
                char trg = i < test.result.length() ? test.result.charAt(i) : '\uffff';
                char res = i < result.length() ? result.charAt(i) : '\uffff';

                buf.append("[" + temp + "] ");
                buf.append(escapedString("" + trg) + " ");
                buf.append(escapedString("" + res) + " ");
                if (trg != res) {
                    buf.append("***");
                }
                buf.append("\n");
            }
        }
        err(buf.toString());
    }

    private static String escapedString(String str) {
        StringBuffer buf = new StringBuffer(str.length() * 6);
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            buf.append("\\u");
            if (ch < 0x1000) {
                buf.append('0');
            }
            if (ch < 0x0100) {
                buf.append('0');
            }
            if (ch < 0x0010) {
                buf.append('0');
            }
            buf.append(Integer.toHexString(ch));
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        try {
            new ArabicShapingRegTest().run(args);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}

