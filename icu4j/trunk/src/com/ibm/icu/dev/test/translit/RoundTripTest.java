package com.ibm.test.translit;
import com.ibm.test.*;
import com.ibm.text.*;
import com.ibm.util.Utility;
import java.io.*;
import java.util.BitSet;
import java.text.ParseException;

/**
 * @test
 * @summary Round trip test of Transliterator
 */
public class RoundTripTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new RoundTripTest().run(args);
    }

    public void TestHiragana() throws IOException, ParseException {
        new Test("Latin-Hiragana", 
          TestUtility.LATIN_SCRIPT, TestUtility.HIRAGANA_SCRIPT)
          .test("[a-z]", "[\u3040-\u3094]", this, new Legal());
    }

    public void TestKatakana() throws IOException, ParseException {
        new Test("Latin-Katakana", 
          TestUtility.LATIN_SCRIPT, TestUtility.KATAKANA_SCRIPT)
          .test("[a-z]", "[\u30A1-\u30FA\u30FC]", this, new Legal());
    }

// Some transliterators removed for 2.0

//  public void TestArabic() throws IOException, ParseException {
//      new Test("Latin-Arabic", 
//        TestUtility.LATIN_SCRIPT, TestUtility.ARABIC_SCRIPT)
//        .test("[a-z]", "[\u0620-\u065F-[\u0640]]", this);
//  }

//  public void TestHebrew() throws IOException, ParseException {
//      new Test("Latin-Hebrew", 
//        TestUtility.LATIN_SCRIPT, TestUtility.HEBREW_SCRIPT)
//        .test(null, "[\u05D0-\u05EF]", this);
//  }

//  public void TestHangul() throws IOException, ParseException {
//      Test t = new TestHangul();
//      t.setPairLimit(30); // Don't run full test -- too long
//      t.test(null, null, this);
//  }

    public void TestJamo() throws IOException, ParseException {
        Test t = new Test("Latin-Jamo", 
          TestUtility.LATIN_SCRIPT, TestUtility.JAMO_SCRIPT);
        t.setErrorLimit(200); // Don't run full test -- too long
        //t.test("[[a-z]-[fqvxz]]", null, this);
        t.test("[a-z]", null, this, new Legal());
    }

    public void TestJamoHangul() throws IOException, ParseException {
        Test t = new Test("Latin-Hangul", 
          TestUtility.LATIN_SCRIPT, TestUtility.HANGUL_SCRIPT);
        t.setErrorLimit(50); // Don't run full test -- too long
        t.test("[a-z]", null, this, new Legal());
    }

    public void TestGreek() throws IOException, ParseException {
        new Test("Latin-Greek", 
          TestUtility.LATIN_SCRIPT, TestUtility.GREEK_SCRIPT)
          .test(null, "[\u003B\u00B7[:Greek:]-[\u03D7-\u03EF]]", this, new LegalGreek());
    }

    public void TestCyrillic() throws IOException, ParseException {
        new Test("Latin-Cyrillic", 
          TestUtility.LATIN_SCRIPT, TestUtility.CYRILLIC_SCRIPT)
          .test(null, "[\u0400-\u045F]", this, new Legal());
    }
    
    public static class Legal {
        public boolean is(String sourceString) {return true;}
    }
    
    public static class LegalGreek extends Legal {
        
        public static boolean isVowel(char c) {
            return "\u03B1\u03B5\u03B7\u03B9\u03BF\u03C5\u03C9\u0391\u0395\u0397\u0399\u039F\u03A5\u03A9".indexOf(c) >= 0;
        }
        
        public static boolean isRho(char c) {
            return "\u03C1\u03A1".indexOf(c) >= 0;
        }
        
        public boolean is(String sourceString) { 
            try {
                // Legal greek has breathing marks IFF there is a vowel or RHO at the start
                // IF it has them, it has exactly one.
                // IF it starts with a RHO, then the breathing mark must come before the second letter.
                // Since there are no surrogates in greek, don't worry about them
                String decomp = Normalizer.normalize(sourceString, Normalizer.DECOMP, 0);
                boolean firstIsVowel = false;
                boolean firstIsRho = false;
                boolean noLetterYet = true;
                int breathingCount = 0;
                int letterCount = 0;
                for (int i = 0; i < decomp.length(); ++i) {
                    char c = decomp.charAt(i);
                    if (UCharacter.isLetter(c)) {
                        ++letterCount;
                        if (noLetterYet) {
                            noLetterYet = false;
                            firstIsVowel = isVowel(c);
                            firstIsRho = isRho(c);
                        }
                        if (firstIsRho && letterCount == 2 && breathingCount == 0) return false;
                    }
                    if (c == '\u0313' || c == '\u0314') {
                        ++breathingCount;
                    }
                }
                
                if (firstIsVowel || firstIsRho) return breathingCount == 1;
                return breathingCount == 0;
            } catch (Throwable t) {
                System.out.println(t.getClass().getName() + " " + t.getMessage());
                return true;
            }
        }
    }
    
    static class Test {
    
        PrintWriter out;
    
        private String transliteratorID; 
        private byte sourceScript;
        private byte targetScript;
        private int errorLimit = Integer.MAX_VALUE;
        private int errorCount = 0;
        private int pairLimit  = 0x10000;
        UnicodeSet sourceRange;
        UnicodeSet targetRange;
        TestLog log;
        Legal legalSource;
    
        /*
         * create a test for the given script transliterator.
         */
        Test(String transliteratorID, 
             byte sourceScript, byte targetScript) {
            this.transliteratorID = transliteratorID;
            this.sourceScript = sourceScript;
            this.targetScript = targetScript;
        }
    
        public void setErrorLimit(int limit) {
            errorLimit = limit;
        }
    
        public void setPairLimit(int limit) {
            pairLimit = limit;
        }
      
        public void test(String sourceRange, String targetRange, TestLog log, Legal legalSource) 
          throws java.io.IOException, java.text.ParseException {
            
            this.legalSource = legalSource;
      
            if (sourceRange != null && sourceRange.length() > 0) {
                this.sourceRange = new UnicodeSet(sourceRange);
            }
            if (targetRange != null && targetRange.length() > 0) {
                this.targetRange = new UnicodeSet(targetRange);
            }

            if (this.sourceRange == null) this.sourceRange = new UnicodeSet("[a-zA-Z]");

            this.log = log;

            log.logln(Utility.escape("Source: " + this.sourceRange));
            log.logln(Utility.escape("Target: " + this.targetRange));

            // make a UTF-8 output file we can read with a browser

            // note: check that every transliterator transliterates the null string correctly!

            String logFileName = "test_" + transliteratorID + "_"
                + sourceScript + "_" + targetScript + ".html";

            File lf = new File(logFileName); 
            log.logln("Creating log file " + lf.getAbsoluteFile());

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream(logFileName), "UTF8"), 4*1024));
            //out.write('\uFFEF');    // BOM
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
            out.println("<HTML><HEAD>");
            out.println("<META content=\"text/html; charset=utf-8\" http-equiv=Content-Type></HEAD>");
            out.println("<BODY>");
            try {
                test2();
            } catch (TestTruncated e) {
                out.println(e.getMessage());
            }
            out.println("</BODY></HTML>");
            out.close();

            if (errorCount > 0) {
                log.errln(transliteratorID + " errors: " + errorCount + ", see " + lf.getAbsoluteFile());
            } else {
                log.logln(transliteratorID + " ok");
                new File(logFileName).delete();
            }
        }

        public void test2() {

            Transliterator sourceToTarget = Transliterator.getInstance(transliteratorID);
            Transliterator targetToSource = sourceToTarget.getInverse();

            log.logln("Checking that source characters convert to target - Singles");
            
            BitSet failSourceTarg = new BitSet();

            for (char c = 0; c < 0xFFFF; ++c) {
                if (TestUtility.isUnassigned(c) ||
                    !isSource(c)) continue;
                String cs = String.valueOf(c);
                String targ = sourceToTarget.transliterate(String.valueOf(cs));
                if (!isReceivingTarget(targ)) {
                    logWrongScript("Source-Target", cs, targ);
                    failSourceTarg.set(c);
                }
            }

            log.logln("Checking that source characters convert to target - Doubles");

            for (char c = 0; c < 0xFFFF; ++c) { 
                if (TestUtility.isUnassigned(c) ||
                    !isSource(c)) continue;
                if (failSourceTarg.get(c)) continue;
                
                for (char d = 0; d < 0xFFFF; ++d) {
                    if (TestUtility.isUnassigned(d) ||
                        !isSource(d)) continue;
                    if (failSourceTarg.get(d)) continue;
                    
                    String cs = String.valueOf(c) + d;
                    String targ = sourceToTarget.transliterate(cs);
                    if (!isReceivingTarget(targ)) {
                        logWrongScript("Source-Target", cs, targ);
                    }
                }
            }

            log.logln("Checking that target characters convert to source and back - Singles");
            
            BitSet failTargSource = new BitSet();
            BitSet failRound = new BitSet();

            for (char c = 0; c < 0xFFFF; ++c) {
                if (TestUtility.isUnassigned(c) ||
                    !isTarget(c)) continue;
                String cs = String.valueOf(c);
                String targ = targetToSource.transliterate(cs);
                String reverse = sourceToTarget.transliterate(targ);
                if (!isReceivingSource(targ)) {
                    logWrongScript("Target-Source", cs, targ);
                    failTargSource.set(c);
                } else if (!cs.equals(reverse)) { // (!cs.equalsIgnoreCase(reverse)) {
                    logRoundTripFailure(cs, targ, reverse);
                    failRound.set(c);
                }
            }

            log.logln("Checking that target characters convert to source and back - Doubles");
            int count = 0;
            StringBuffer buf = new StringBuffer("aa");
            for (char c = 0; c < 0xFFFF; ++c) {
                if (TestUtility.isUnassigned(c) ||
                    !isTarget(c)) continue;
                if (++count > pairLimit) {
                    throw new TestTruncated("Test truncated at " + pairLimit + " x 64k pairs");
                }
                buf.setCharAt(0, c);
                log.log(TestUtility.hex(c));
                for (char d = 0; d < 0xFFFF; ++d) {
                    if (TestUtility.isUnassigned(d) ||
                        !isTarget(d)) continue;
                    buf.setCharAt(1, d);
                    String cs = buf.toString();
                    String targ = targetToSource.transliterate(cs);
                    String reverse = sourceToTarget.transliterate(targ);
                    if (!isReceivingSource(targ) && !failTargSource.get(c) && !failTargSource.get(d)) {
                        logWrongScript("Target-Source", cs, targ);
                    } else if (!cs.equalsIgnoreCase(reverse) && !failRound.get(c) && !failRound.get(d)) {
                        logRoundTripFailure(cs, targ, reverse);
                    }
                }
            }
            log.logln("");
        }

        final void logWrongScript(String label, String from, String to) {
            if (++errorCount >= errorLimit) {
                throw new TestTruncated("Test truncated; too many failures");
            }
            out.println("<br>Fail " + label + ": " +
                        from + " (" +
                        TestUtility.hex(from) + ") => " +
                        to + " (" +
                        TestUtility.hex(to) + ")"
                        );
        }

        final void logRoundTripFailure(String from, String to, String back) {
            if (!legalSource.is(from)) return; // skip illegals
            
            if (++errorCount >= errorLimit) {
                throw new TestTruncated("Test truncated; too many failures");
            }
            out.println("<br>Fail Roundtrip: " +
                        from + " (" +
                        TestUtility.hex(from) + ") => " +
                        to + " (" +
                        TestUtility.hex(to) + ") => " +
                        back + " (" +
                        TestUtility.hex(back) + ")" 
                        );
        }

        /*
         * Characters to filter for source-target mapping completeness
         * Typically is base alphabet, minus extended characters
         * Default is ASCII letters for Latin
         */
        public boolean isSource(char c) {
            byte script = TestUtility.getScript(c);
            if (script != sourceScript) return false;
            if (!TestUtility.isLetter(c)) return false;
            if (!sourceRange.contains(c)) return false;
            return true;
        }

        /*
         * Characters to check for target back to source mapping.
         * Typically the same as the target script, plus punctuation
         */
        public boolean isReceivingSource(char c) {
            byte script = TestUtility.getScript(c);
            return (script == sourceScript || script == TestUtility.COMMON_SCRIPT);
        }

        /*
         * Characters to filter for target-source mapping
         * Typically is base alphabet, minus extended characters
         */
        public boolean isTarget(char c) {
            byte script = TestUtility.getScript(c);
            if (script != targetScript) return false;
            if (!TestUtility.isLetter(c)) return false;
            if (targetRange != null && !targetRange.contains(c)) return false;
            return true;
        }

        /*
         * Characters to check for target-source mapping
         * Typically the same as the source script, plus punctuation
         */
        public boolean isReceivingTarget(char c) {
            byte script = TestUtility.getScript(c);
            return (script == targetScript || script == TestUtility.COMMON_SCRIPT);
        }

        final boolean isSource(String s) {
            for (int i = 0; i < s.length(); ++i) {
                if (!isSource(s.charAt(i))) return false;
            }
            return true;
        }

        final boolean isTarget(String s) {
            for (int i = 0; i < s.length(); ++i) {
                if (!isTarget(s.charAt(i))) return false;
            }
            return true;
        }

        final boolean isReceivingSource(String s) {
            for (int i = 0; i < s.length(); ++i) {
                if (!isReceivingSource(s.charAt(i))) return false;
            }
            return true;
        }

        final boolean isReceivingTarget(String s) {
            for (int i = 0; i < s.length(); ++i) {
                if (!isReceivingTarget(s.charAt(i))) return false;
            }
            return true;
        }

        static class TestTruncated extends RuntimeException {
            TestTruncated(String msg) {
                super(msg);
            }
        }
    }

//  static class TestHangul extends Test {
//      TestHangul () {
//          super("Jamo-Hangul", TestUtility.JAMO_SCRIPT, TestUtility.HANGUL_SCRIPT);
//      }
//
//      public boolean isSource(char c) {
//          if (0x1113 <= c && c <= 0x1160) return false;
//          if (0x1176 <= c && c <= 0x11F9) return false;
//          if (0x3131 <= c && c <= 0x318E) return false;
//          return super.isSource(c);
//      }
//  }
}
