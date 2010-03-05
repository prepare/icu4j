/*
*******************************************************************************
* Copyright (C) 2009-2010, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/
package com.ibm.icu.dev.test.text;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.TestUtil;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.SpoofChecker;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;
import java.util.Set;
import java.util.LinkedHashSet;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;


public class SpoofCheckerTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new SpoofCheckerTest().run(args);
    }

  void TEST_ASSERT(boolean expr) {
    if ((expr)==false) {
      errln("Assertion Failure.\n");
    }
  }

  void TEST_ASSERT_EQ(int a, int b) {
    if (a != b) {
      errln(String.format("Test Failure: %d != %d\n", a, b));
    }
  }

  void TEST_ASSERT_NE(Object a, Object b) {
    if (a == b) {
      errln(String.format("Test Failure: (%s) == (%s) \n", a.toString(), b.toString()));
    }
  }


  /*
   *   setup() and teardown()
   *         macros to handle the boilerplate around setting up test case.
   *         Put arbitrary test code between SETUP and TEARDOWN.
   *         "sc" is the ready-to-go  SpoofChecker for use in the tests.
   */
  SpoofChecker sc;
  SpoofChecker.Builder builder;
  void setup() {
    builder = new SpoofChecker.Builder();
    sc = builder.build();
  }

  void teardown() { 
    sc = null;
  }


  /*
   *  Identifiers for verifying that spoof checking is minimally alive and working.
   */
  char[] goodLatinChars = {(char)0x75, (char)0x77};
  String goodLatin = new String(goodLatinChars);    /* "uw", all ASCII             */
  /*   (not confusable)          */
  char[] scMixedChars = {(char)0x73, (char)0x0441};
  String scMixed  = new String(scMixedChars);   /* "sc", with Cyrillic 'c'     */
  /*   (mixed script, confusable */

  char[] scLatinChars = {(char)0x73,  (char)0x63};
  String scLatin  = new String(scLatinChars);    /* "sc", plain ascii.        */
  char[] goodCyrlChars = {(char)0x438, (char)0x43B};
  String goodCyrl = new String(goodCyrlChars);   /* Plain lower case Cyrillic letters,
                                                    no latin confusables         */

  char[] goodGreekChars = {(char)0x3c0, (char)0x3c6};
  String goodGreek   = new String(goodGreekChars);   /* Plain lower case Greek letters */

  char[] lll_Latin_aChars = {(char)0x6c, (char)0x49, (char)0x31};
  String lll_Latin_a = new String(lll_Latin_aChars);   /* lI1, all ASCII */

  /*  Full-width I, Small Roman Numeral fifty, Latin Cap Letter IOTA*/
  char[] lll_Latin_bChars = {(char)0xff29, (char)0x217c, (char)0x196};
  String lll_Latin_b = new String(lll_Latin_bChars);     

  char[] lll_CyrlChars = {(char)0x0406, (char)0x04C0, (char)0x31};
  String lll_Cyrl    = new String(lll_CyrlChars);

  /* The skeleton transform for all of thes 'lll' lookalikes is all ascii digit 1. */
  char[] lll_SkelChars = {(char)0x31, (char)0x31, (char)0x31};
  String lll_Skel = new String(lll_SkelChars);

    /*
     *  basic ctor
     */
  public void TestUSpoof() {
    setup();
    teardown();
  }

    /*
     *  Test build from source rules.
     */
  public void TestOpenFromSourceRules() {
    setup();
    String fileName;
    Reader confusables;
    Reader confusablesWholeScript;
    SpoofChecker rsc;

    try {
      fileName = "unicode" + File.separator + "confusables.txt";
      confusables = TestUtil.getDataReader(fileName);
      fileName = "unicode" + File.separator + "confusablesWholeScript.txt";
      confusablesWholeScript = TestUtil.getDataReader(fileName);
    
      rsc = builder.setData(confusables, confusablesWholeScript).build();
    } catch (java.io.IOException e) {
      errln("Create from source rules failed: input file error.");
    } catch (SpoofChecker.SpoofCheckerException e) {
      errln("Create from source rules failed: data format error.");
    }
    rsc = null;
    /*  printf("ParseError Line is %d\n", pe.line);  */
    teardown();
  }

    /*
     * openFromSerialized and serialize
     */
    /*
      setup();
      int        serializedSize = 0;
      int        actualLength = 0;
      char           *buf;
      SpoofChecker  *sc2;
      int         checkResults;


      serializedSize = uspoof_serialize(sc, null, 0, &status);
      TEST_ASSERT_EQ(status, U_BUFFER_OVERFLOW_ERROR);
      TEST_ASSERT(serializedSize > 0);

      // Serialize the default spoof checker
      status = U_ZERO_ERROR;
      buf = (char *)malloc(serializedSize + 10);
      TEST_ASSERT(buf != null);
      buf[serializedSize] = 42;
      uspoof_serialize(sc, buf, serializedSize, &status);
      TEST_ASSERT_EQ(42, buf[serializedSize]);

      // Create a new spoof checker from the freshly serialized data
      sc2 = uspoof_openFromSerialized(buf, serializedSize+10, &actualLength, &status);
      TEST_ASSERT_NE(null, sc2);
      TEST_ASSERT_EQ(serializedSize, actualLength);

      // Verify that the new spoof checker at least wiggles
      checkResults = sc2.check(goodLatin);
      TEST_ASSERT_EQ(0, checkResults);

      checkResults = sc2.check(scMixed);
      TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT | SpoofChecker.MIXED_SCRIPT_CONFUSABLE, checkResults);

      uspoof_close(sc2);
      teardown();
    */ 


    /*
     * Set & Get Check Flags
     */
  public void TestGetSetChecks1() {
    setup();
    int t;
    sc = builder.setChecks(SpoofChecker.ALL_CHECKS).build();
    t = sc.getChecks();
    TEST_ASSERT_EQ(t, SpoofChecker.ALL_CHECKS);

    sc = builder.setChecks(0).build();
    t = sc.getChecks();
    TEST_ASSERT_EQ(0, t);

    int checks = SpoofChecker.WHOLE_SCRIPT_CONFUSABLE | SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.ANY_CASE;
    sc = builder.setChecks(checks).build();
    t = sc.getChecks();
    TEST_ASSERT_EQ(checks, t);
    teardown();
  }

    /*
     * get & setAllowedChars
     */
  public void TestGetSetAllowedChars() {
    setup();
    UnicodeSet us;
    UnicodeSet uset;

    uset = sc.getAllowedChars();
    TEST_ASSERT(uset.isFrozen());
    us = new UnicodeSet((int)0x41, (int)0x5A);   /*  [A-Z]  */
    sc = builder.setAllowedChars(us).build();
    TEST_ASSERT_NE(us, sc.getAllowedChars());
    TEST_ASSERT(us.equals(sc.getAllowedChars()));
    teardown();
  }

    /*
     *  clone()
     */
    /*
      setup();
      SpoofChecker clone1 = null;
      SpoofChecker clone2 = null;
      int        checkResults = 0;

      clone1 = uspoof_clone(sc, &status);
      TEST_ASSERT_NE(clone1, sc);

      clone2 = uspoof_clone(clone1, &status);
      TEST_ASSERT_NE(clone2, clone1);

      uspoof_close(clone1);

      // Verify that the cloned spoof checker is alive
      checkResults = clone2.check(goodLatin);
      TEST_ASSERT_EQ(0, checkResults);

      checkResults = clone2.check(scMixed);
      TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT | SpoofChecker.MIXED_SCRIPT_CONFUSABLE, checkResults);
      uspoof_close(clone2);
      teardown();
    */

    /*
     *  get & set Checks
     */
  public void TestGetSetChecks() {
    setup();
    int   checks;
    int   checks2;
    boolean checkResults;

    checks = sc.getChecks();
    TEST_ASSERT_EQ(SpoofChecker.ALL_CHECKS, checks);

    checks &= ~(SpoofChecker.SINGLE_SCRIPT | SpoofChecker.MIXED_SCRIPT_CONFUSABLE);
    sc = builder.setChecks(checks).build();
    checks2 = sc.getChecks();
    TEST_ASSERT_EQ(checks, checks2);

    /* The checks that were disabled just above are the same ones that the "scMixed" test fails.
       So with those tests gone checking that Identifier should now succeed */
    checkResults = sc.check(scMixed);
    TEST_ASSERT(false == checkResults);
    teardown();
  }

    /*
     *  AllowedLoacles
     */
  public void TestAllowedLoacles() {
    setup();
    Set<ULocale> allowedLocales = new LinkedHashSet<ULocale>();
    boolean checkResults;

    /* Default allowed locales list should be empty */
    allowedLocales = sc.getAllowedLocales();
    TEST_ASSERT(allowedLocales.isEmpty());

    /* Allow en and ru, which should enable Latin and Cyrillic only to pass */
    ULocale enloc = new ULocale("en");
    ULocale ruloc = new ULocale("ru_RU");
    allowedLocales.add(enloc);
    allowedLocales.add(ruloc);
    sc = builder.setAllowedLocales(allowedLocales).build();
    allowedLocales = sc.getAllowedLocales();
    TEST_ASSERT(allowedLocales.contains(enloc));
    TEST_ASSERT(allowedLocales.contains(ruloc));

    /* Limit checks to SpoofChecker.CHAR_LIMIT.  Some of the test data has whole script confusables also,
     * which we don't want to see in this test. */
    sc = builder.setChecks(SpoofChecker.CHAR_LIMIT).build();

    SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
    checkResults = sc.check(goodLatin);
    TEST_ASSERT(false == checkResults);

    checkResults = sc.check(goodGreek, result);
    TEST_ASSERT_EQ(SpoofChecker.CHAR_LIMIT, result.status);

    checkResults = sc.check(goodCyrl);
    TEST_ASSERT(false == checkResults);

    /* Reset with an empty locale list, which should allow all characters to pass */
    allowedLocales = new LinkedHashSet<ULocale>();
    sc = builder.setAllowedLocales(allowedLocales).build();

    checkResults = sc.check(goodGreek);
    TEST_ASSERT(false == checkResults);
    teardown();
  }

  /*
   * AllowedChars   set/get the UnicodeSet of allowed characters.
   */
  public void TestAllowedChars() {
    setup();
    UnicodeSet  set;
    UnicodeSet  tmpSet;
    boolean checkResults;

    /* By default, we should see no restriction; the UnicodeSet should allow all characters. */
    set = sc.getAllowedChars();
    tmpSet = new UnicodeSet(0, 0x10ffff);
    TEST_ASSERT(tmpSet.equals(set));

    /* Setting the allowed chars should enable the check. */
    sc = builder.setChecks(SpoofChecker.ALL_CHECKS & ~SpoofChecker.CHAR_LIMIT).build();

    /* Remove a character that is in our good Latin test identifier from the allowed chars set. */
    tmpSet.remove(goodLatin.charAt(1));
    sc = builder.setAllowedChars(tmpSet).build();

    /* Latin Identifier should now fail; other non-latin test cases should still be OK */
    SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
    checkResults = sc.check(goodLatin, result);
    TEST_ASSERT(checkResults);
    TEST_ASSERT_EQ(SpoofChecker.CHAR_LIMIT, result.status);

    checkResults = sc.check(goodGreek, result);
    TEST_ASSERT(checkResults);
    TEST_ASSERT_EQ(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE, result.status);
    teardown();
  }

  public void TestCheck() {
    setup();
    SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
    boolean checkResults;

    result.position = 666;
    checkResults = sc.check(goodLatin, result);
    TEST_ASSERT(false == checkResults);
    TEST_ASSERT_EQ(666, result.position);

    checkResults = sc.check(goodCyrl, result);
    TEST_ASSERT(false == checkResults);

    result.position = 666;
    checkResults = sc.check(scMixed, result);
    TEST_ASSERT(true == checkResults);
    TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.SINGLE_SCRIPT , result.status);
    TEST_ASSERT_EQ(2, result.position);
    teardown();
  }

  public void TestAreConfusable1() {
    setup();
    int  checkResults;
    checkResults = sc.areConfusable(scLatin, scMixed);
    TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE, checkResults);

    checkResults = sc.areConfusable(goodGreek, scLatin);
    TEST_ASSERT_EQ(0, checkResults);

    checkResults = sc.areConfusable(lll_Latin_a, lll_Latin_b);
    TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, checkResults);
    teardown();
  }

  public void TestGetSkeleton() {
    setup();
    String dest;
    dest = sc.getSkeleton(SpoofChecker.ANY_CASE, lll_Latin_a);
    TEST_ASSERT(lll_Skel.equals(dest));
    TEST_ASSERT_EQ(lll_Skel.length(), dest.length());
    TEST_ASSERT_EQ(3, dest.length());
    teardown();
  }

  /**
   * IntlTestSpoof is the top level test class for the Unicode Spoof detection tests
   */

  // Test the USpoofDetector API functions that require C++
  // The pure C part of the API, which is most of it, is tested in cintltst
  /**
   * IntlTestSpoof tests for USpoofDetector
   */
  public void TestSpoofAPI() {

    setup();
    String s = "uvw";
    SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
    result.position = 666;
    boolean checkResults = sc.check(s, result);
    TEST_ASSERT(false == checkResults);
    TEST_ASSERT_EQ(666, result.position);  // not changed
    teardown();

    setup();
    String s1 = "cxs";
    String s2 = Utility.unescape("\\u0441\\u0445\\u0455");  // Cyrillic "cxs"
    int checkResult = sc.areConfusable(s1, s2);
    TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE, checkResult);
    teardown();

    setup();
    s = "I1l0O";
    String dest = sc.getSkeleton(SpoofChecker.ANY_CASE, s);
    TEST_ASSERT(dest.equals("11100"));
    teardown();
  }

  // testSkeleton.   Spot check a number of confusable skeleton substitutions from the 
  //                 Unicode data file confusables.txt
  //                 Test cases chosen for substitutions of various lengths, and 
  //                 membership in different mapping tables.
  public void TestSkeleton() {
    int ML = 0;
    int SL = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
    int MA = SpoofChecker.ANY_CASE;
    int SA = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE | SpoofChecker.ANY_CASE;

    setup();
    // A long "identifier" that will overflow implementation stack buffers, forcing heap allocations.
    checkSkeleton(sc, SL,
        " A long 'identifier' that will overflow implementation stack buffers, forcing heap allocations."
      + " A long 'identifier' that will overflow implementation stack buffers, forcing heap allocations."
      + " A long 'identifier' that will overflow implementation stack buffers, forcing heap allocations."
      + " A long 'identifier' that will overflow implementation stack buffers, forcing heap allocations.",
        " A 1ong \\u02b9identifier\\u02b9 that wi11 overf1ow imp1ementation stack buffers, forcing heap a11ocations."
      + " A 1ong \\u02b9identifier\\u02b9 that wi11 overf1ow imp1ementation stack buffers, forcing heap a11ocations."
      + " A 1ong \\u02b9identifier\\u02b9 that wi11 overf1ow imp1ementation stack buffers, forcing heap a11ocations."
      + " A 1ong \\u02b9identifier\\u02b9 that wi11 overf1ow imp1ementation stack buffers, forcing heap a11ocations.");

    // FC5F ;	FE74 0651 ;   ML  #* ARABIC LIGATURE SHADDA WITH KASRATAN ISOLATED FORM to
    //                                ARABIC KASRATAN ISOLATED FORM, ARABIC SHADDA	
    //    This character NFKD normalizes to \u0020 \u064d \u0651, so its confusable mapping 
    //    is never used in creating a skeleton.
    checkSkeleton(sc, SL, "\\uFC5F", " \\u064d\\u0651");

    checkSkeleton(sc, SL, "nochange", "nochange");
    checkSkeleton(sc, MA, "love", "1ove");   // lower case l to digit 1
    checkSkeleton(sc, ML, "OOPS", "OOPS");
    checkSkeleton(sc, MA, "OOPS", "00PS");   // Letter O to digit 0 in any case mode only
    checkSkeleton(sc, SL, "\\u059c", "\\u0301");
    checkSkeleton(sc, SL, "\\u2A74", "\\u003A\\u003A\\u003D");
    checkSkeleton(sc, SL, "\\u247E", "\\u0028\\u0031\\u0031\\u0029");
    checkSkeleton(sc, SL, "\\uFDFB", "\\u062C\\u0644\\u0020\\u062C\\u0644\\u0627\\u0644\\u0647");

    // This mapping exists in the ML and MA tables, does not exist in SL, SA
    //0C83 ;	0C03 ;	ML	# ( ಃ → ః ) KANNADA SIGN VISARGA → TELUGU SIGN VISARGA	# {source:513}
    checkSkeleton(sc, SL, "\\u0C83", "\\u0C83");
    checkSkeleton(sc, SA, "\\u0C83", "\\u0C83");
    checkSkeleton(sc, ML, "\\u0C83", "\\u0C03");
    checkSkeleton(sc, MA, "\\u0C83", "\\u0C03");

    // 0391 ; 0041 ; MA # ( Α → A ) GREEK CAPITAL LETTER ALPHA to LATIN CAPITAL LETTER A 
    // This mapping exists only in the MA table.
    checkSkeleton(sc, MA, "\\u0391", "A");
    checkSkeleton(sc, SA, "\\u0391", "\\u0391");
    checkSkeleton(sc, ML, "\\u0391", "\\u0391");
    checkSkeleton(sc, SL, "\\u0391", "\\u0391");

    // 13CF ;  0062 ;  MA  #  CHEROKEE LETTER SI to LATIN SMALL LETTER B  
    // This mapping exists in the ML and MA tables
    checkSkeleton(sc, ML, "\\u13CF", "b");
    checkSkeleton(sc, MA, "\\u13CF", "b");
    checkSkeleton(sc, SL, "\\u13CF", "\\u13CF");
    checkSkeleton(sc, SA, "\\u13CF", "\\u13CF");

    // 0022 ;  02B9 02B9 ;  SA  #*  QUOTATION MARK to MODIFIER LETTER PRIME, MODIFIER LETTER PRIME 
    // all tables.
    checkSkeleton(sc, SL, "\\u0022", "\\u02B9\\u02B9");
    checkSkeleton(sc, SA, "\\u0022", "\\u02B9\\u02B9");
    checkSkeleton(sc, ML, "\\u0022", "\\u02B9\\u02B9");
    checkSkeleton(sc, MA, "\\u0022", "\\u02B9\\u02B9");

    teardown();
  }


  // Internal function to run a single skeleton test case.
  //
  //  Run a single confusable skeleton transformation test case.
  //
  void checkSkeleton(SpoofChecker sc, int type, String input,  String expected) {
    String uInput = Utility.unescape(input);
    String uExpected = Utility.unescape(expected);
    String actual;
    actual = sc.getSkeleton(type, uInput);
    if (!uExpected.equals(actual)) {
      errln("Actual and Expected skeletons differ.");
      errln((" Actual   Skeleton: \"") + actual + ("\"\n") +
            (" Expected Skeleton: \"") + uExpected + ("\""));
    }
  }

  public void TestAreConfusable() {
    setup();
    String s1 = "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. " +
                "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. ";
    String s2 = "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. " +
                "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. ";
    TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, sc.areConfusable(s1, s2));
    teardown();
  }

  public void TestInvisible() {
    setup();
    String  s = Utility.unescape("abcd\\u0301ef");
    SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
    result.position = -42;
    TEST_ASSERT(false == sc.check(s, result));
    TEST_ASSERT_EQ(0, result.status);
    TEST_ASSERT(result.position == -42);  // unchanged

    String  s2 = Utility.unescape("abcd\\u0301\\u0302\\u0301ef");
    TEST_ASSERT(true == sc.check(s2, result));
    TEST_ASSERT_EQ(SpoofChecker.INVISIBLE, result.status);
    TEST_ASSERT_EQ(7, result.position);

    // Two acute accents, one from the composed a with acute accent, \u00e1,
    // and one separate.
    result.position = -42;
    String  s3 = Utility.unescape("abcd\\u00e1\\u0301xyz");
    TEST_ASSERT(true == sc.check(s3, result));
    TEST_ASSERT_EQ(SpoofChecker.INVISIBLE, result.status);
    TEST_ASSERT_EQ(7, result.position);
    teardown();
  }

}
