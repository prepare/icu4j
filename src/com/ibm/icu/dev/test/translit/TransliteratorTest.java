/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/translit/TransliteratorTest.java,v $
 * $Date: 2001/11/09 00:49:49 $
 * $Revision: 1.65 $
 *
 *****************************************************************************************
 */
package com.ibm.test.translit;
import com.ibm.text.*;
import com.ibm.test.*;
import com.ibm.util.Utility;
import java.text.*;
import java.util.*;

/***********************************************************************

                     HOW TO USE THIS TEST FILE
                               -or-
                  How I developed on two platforms
                without losing (too much of) my mind


1. Add new tests by copying/pasting/changing existing tests.  On Java,
   any public void method named Test...() taking no parameters becomes
   a test.  On C++, you need to modify the header and add a line to
   the runIndexedTest() dispatch method.

2. Make liberal use of the expect() method; it is your friend.

3. The tests in this file exactly match those in a sister file on the
   other side.  The two files are:

   icu4j:  src/com/ibm/test/translit/TransliteratorTest.java
   icu4c:  source/test/intltest/transtst.cpp

                  ==> THIS IS THE IMPORTANT PART <==

   When you add a test in this file, add it in transtst.cpp too.
   Give it the same name and put it in the same relative place.  This
   makes maintenance a lot simpler for any poor soul who ends up
   trying to synchronize the tests between icu4j and icu4c.

4. If you MUST enter a test that is NOT paralleled in the sister file,
   then add it in the special non-mirrored section.  These are
   labeled

     "icu4j ONLY"

   or

     "icu4c ONLY"

   Make sure you document the reason the test is here and not there.


Thank you.
The Management
***********************************************************************/

/**
 * @test
 * @summary General test of Transliterator
 */
public class TransliteratorTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new TransliteratorTest().run(args);
    }

    public void TestInstantiation() {
        long ms = System.currentTimeMillis();
        String ID;
        for (Enumeration e = Transliterator.getAvailableIDs(); e.hasMoreElements(); ) {
            ID = (String) e.nextElement();
            Transliterator t = null;
            try {
                t = Transliterator.getInstance(ID);
                // We should get a new instance if we try again
                Transliterator t2 = Transliterator.getInstance(ID);
                if (t != t2) {
                    logln("OK: " + Transliterator.getDisplayName(ID) + " (" + ID + "): " + t);
                } else {
                    errln("FAIL: " + ID + " returned identical instances");
                    t = null;
                }
            } catch (IllegalArgumentException ex) {
                errln("FAIL: " + ID);
                throw ex;
            }

            if (t != null) {
                // Now test toRules
                String rules = null;
                try {
                    rules = t.toRules(true);

                    Transliterator u = Transliterator.createFromRules("x",
                                           rules, Transliterator.FORWARD);
                } catch (IllegalArgumentException ex2) {
                    errln("FAIL: " + ID + ".toRules() => bad rules: " +
                          rules);
                    throw ex2;
                }
            }
        }

        // Now test the failure path
        try {
            ID = "<Not a valid Transliterator ID>";
            Transliterator t = Transliterator.getInstance(ID);
            errln("FAIL: " + ID + " returned " + t);
        } catch (IllegalArgumentException ex) {
            logln("OK: Bogus ID handled properly");
        }

        ms = System.currentTimeMillis() - ms;
        logln("Elapsed time: " + ms + " ms");
    }

    public void TestSimpleRules() {
        /* Example: rules 1. ab>x|y
         *                2. yc>z
         *
         * []|eabcd  start - no match, copy e to tranlated buffer
         * [e]|abcd  match rule 1 - copy output & adjust cursor
         * [ex|y]cd  match rule 2 - copy output & adjust cursor
         * [exz]|d   no match, copy d to transliterated buffer
         * [exzd]|   done
         */
        expect("ab>x|y;" +
               "yc>z",
               "eabcd", "exzd");

        /* Another set of rules:
         *    1. ab>x|yzacw
         *    2. za>q
         *    3. qc>r
         *    4. cw>n
         *
         * []|ab       Rule 1
         * [x|yzacw]   No match
         * [xy|zacw]   Rule 2
         * [xyq|cw]    Rule 4
         * [xyqn]|     Done
         */
        expect("ab>x|yzacw;" +
               "za>q;" +
               "qc>r;" +
               "cw>n",
               "ab", "xyqn");

        /* Test categories
         */
        Transliterator t = new RuleBasedTransliterator("<ID>",
                                                       "$dummy=\uE100;" +
                                                       "$vowel=[aeiouAEIOU];" +
                                                       "$lu=[:Lu:];" +
                                                       "$vowel } $lu > '!';" +
                                                       "$vowel > '&';" +
                                                       "'!' { $lu > '^';" +
                                                       "$lu > '*';" +
                                                       "a>ERROR");
        expect(t, "abcdefgABCDEFGU", "&bcd&fg!^**!^*&");
    }

    /**
     * Test inline set syntax and set variable syntax.
     */
    public void TestInlineSet() {
        expect("{ [:Ll:] } x > y; [:Ll:] > z;", "aAbxq", "zAyzz");
        expect("a[0-9]b > qrs", "1a7b9", "1qrs9");

        expect("$digit = [0-9];" +
               "$alpha = [a-zA-Z];" +
               "$alphanumeric = [$digit $alpha];" + // ***
               "$special = [^$alphanumeric];" +     // ***
               "$alphanumeric > '-';" +
               "$special > '*';",

               "thx-1138", "---*----");
    }

    /**
     * Create some inverses and confirm that they work.  We have to be
     * careful how we do this, since the inverses will not be true
     * inverses -- we can't throw any random string at the composition
     * of the transliterators and expect the identity function.  F x
     * F' != I.  However, if we are careful about the input, we will
     * get the expected results.
     */
    public void TestRuleBasedInverse() {
        String RULES =
            "abc>zyx;" +
            "ab>yz;" +
            "bc>zx;" +
            "ca>xy;" +
            "a>x;" +
            "b>y;" +
            "c>z;" +

            "abc<zyx;" +
            "ab<yz;" +
            "bc<zx;" +
            "ca<xy;" +
            "a<x;" +
            "b<y;" +
            "c<z;" +

            "";

        String[] DATA = {
            // Careful here -- random strings will not work.  If we keep
            // the left side to the domain and the right side to the range
            // we will be okay though (left, abc; right xyz).
            "a", "x",
            "abcacab", "zyxxxyy",
            "caccb", "xyzzy",
        };

        Transliterator fwd = new RuleBasedTransliterator("<ID>", RULES);
        Transliterator rev = new RuleBasedTransliterator("<ID>", RULES,
                                     RuleBasedTransliterator.REVERSE, null);
        for (int i=0; i<DATA.length; i+=2) {
            expect(fwd, DATA[i], DATA[i+1]);
            expect(rev, DATA[i+1], DATA[i]);
        }
    }

    /**
     * Basic test of keyboard.
     */
    public void TestKeyboard() {
        Transliterator t = new RuleBasedTransliterator("<ID>",
                                                       "psch>Y;"
                                                       +"ps>y;"
                                                       +"ch>x;"
                                                       +"a>A;");
        String DATA[] = {
            // insertion, buffer
            "a", "A",
            "p", "Ap",
            "s", "Aps",
            "c", "Apsc",
            "a", "AycA",
            "psch", "AycAY",
            null, "AycAY", // null means finishKeyboardTransliteration
        };

        keyboardAux(t, DATA);
    }

    /**
     * Basic test of keyboard with cursor.
     */
    public void TestKeyboard2() {
        Transliterator t = new RuleBasedTransliterator("<ID>",
                                                       "ych>Y;"
                                                       +"ps>|y;"
                                                       +"ch>x;"
                                                       +"a>A;");
        String DATA[] = {
            // insertion, buffer
            "a", "A",
            "p", "Ap",
            "s", "Ay",
            "c", "Ayc",
            "a", "AycA",
            "p", "AycAp",
            "s", "AycAy",
            "c", "AycAyc",
            "h", "AycAY",
            null, "AycAY", // null means finishKeyboardTransliteration
        };

        keyboardAux(t, DATA);
    }

    /**
     * Test keyboard transliteration with back-replacement.
     */
    public void TestKeyboard3() {
        // We want th>z but t>y.  Furthermore, during keyboard
        // transliteration we want t>y then yh>z if t, then h are
        // typed.
        String RULES =
            "t>|y;" +
            "yh>z;" +
            "";

        String[] DATA = {
            // Column 1: characters to add to buffer (as if typed)
            // Column 2: expected appearance of buffer after
            //           keyboard xliteration.
            "a", "a",
            "b", "ab",
            "t", "aby",
            "c", "abyc",
            "t", "abycy",
            "h", "abycz",
            null, "abycz", // null means finishKeyboardTransliteration
        };

        Transliterator t = new RuleBasedTransliterator("<ID>", RULES);
        keyboardAux(t, DATA);
    }

    private void keyboardAux(Transliterator t, String[] DATA) {
        Transliterator.Position index = new Transliterator.Position();
        ReplaceableString s = new ReplaceableString();
        for (int i=0; i<DATA.length; i+=2) {
            StringBuffer log;
            if (DATA[i] != null) {
                log = new StringBuffer(s.toString() + " + "
                                       + DATA[i]
                                       + " -> ");
                t.transliterate(s, index, DATA[i]);
            } else {
                log = new StringBuffer(s.toString() + " => ");
                t.finishTransliteration(s, index);
            }
            String str = s.toString();
            // Show the start index '{' and the cursor '|'
            log.append(str.substring(0, index.contextStart)).
                append('{').
                append(str.substring(index.contextStart,
                                     index.start)).
                append('|').
                append(str.substring(index.start));
            if (str.equals(DATA[i+1])) {
                logln(log.toString());
            } else {
                errln("FAIL: " + log.toString() + ", expected " + DATA[i+1]);
            }
        }
    }

    // Latin-Arabic has been temporarily removed until it can be
    // done correctly.

//  public void TestArabic() {
//      String DATA[] = {
//          "Arabic",
//              "\u062a\u062a\u0645\u062a\u0639 "+
//              "\u0627\u0644\u0644\u063a\u0629 "+
//              "\u0627\u0644\u0639\u0631\u0628\u0628\u064a\u0629 "+
//              "\u0628\u0628\u0646\u0638\u0645 "+
//              "\u0643\u062a\u0627\u0628\u0628\u064a\u0629 "+
//              "\u062c\u0645\u064a\u0644\u0629"
//      };

//      Transliterator t = Transliterator.getInstance("Latin-Arabic");
//      for (int i=0; i<DATA.length; i+=2) {
//          expect(t, DATA[i], DATA[i+1]);
//      }
//  }

    /**
     * Compose the Kana transliterator forward and reverse and try
     * some strings that should come out unchanged.
     */
    public void TestCompoundKana() {
        Transliterator t = new CompoundTransliterator("Latin-Katakana;Katakana-Latin");
        expect(t, "aaaaa", "aaaaa");
    }

    /**
     * Compose the hex transliterators forward and reverse.
     */
    public void TestCompoundHex() {
        Transliterator a = Transliterator.getInstance("Any-Hex");
        Transliterator b = Transliterator.getInstance("Hex-Any");
        Transliterator[] trans = { a, b };
        Transliterator ab = new CompoundTransliterator(trans);

        // Do some basic tests of b
        expect(b, "\\u0030\\u0031", "01");

        String s = "abcde";
        expect(ab, s, s);

        trans = new Transliterator[] { b, a };
        Transliterator ba = new CompoundTransliterator(trans);
        ReplaceableString str = new ReplaceableString(s);
        a.transliterate(str);
        expect(ba, str.toString(), str.toString());
    }

    /**
     * Do some basic tests of filtering.
     */
    public void TestFiltering() {
        Transliterator hex = Transliterator.getInstance("Any-Hex");
        hex.setFilter(new UnicodeFilter() {
            public boolean contains(int c) {
                return c != 'c';
            }
        });
        String s = "abcde";
        String out = hex.transliterate(s);
        String exp = "\\u0061\\u0062c\\u0064\\u0065";
        if (out.equals(exp)) {
            logln("Ok:   \"" + exp + "\"");
        } else {
            logln("FAIL: \"" + out + "\", wanted \"" + exp + "\"");
        }
    }

    /**
     * Test anchors
     */
    public void TestAnchors() {
        expect("^ab  > 01 ;" +
               " ab  > |8 ;" +
               "  b  > k ;" +
               " 8x$ > 45 ;" +
               " 8x  > 77 ;",

               "ababbabxabx",
               "018k7745");
        expect("$s = [z$] ;" +
               "$s{ab    > 01 ;" +
               "   ab    > |8 ;" +
               "    b    > k ;" +
               "   8x}$s > 45 ;" +
               "   8x    > 77 ;",

               "abzababbabxzabxabx",
               "01z018k45z01x45");
    }

    /**
     * Test pattern quoting and escape mechanisms.
     */
    public void TestPatternQuoting() {
        // Array of 3n items
        // Each item is <rules>, <input>, <expected output>
        String[] DATA = {
            "\u4E01>'[male adult]'", "\u4E01", "[male adult]",
        };

        for (int i=0; i<DATA.length; i+=3) {
            logln("Pattern: " + Utility.escape(DATA[i]));
            Transliterator t = new RuleBasedTransliterator("<ID>", DATA[i]);
            expect(t, DATA[i+1], DATA[i+2]);
        }
    }

    /**
     * Regression test for bugs found in Greek transliteration.
     */
    public void TestJ277() {
        Transliterator gl = Transliterator.getInstance("Greek-Latin; NFD; [:M:]Remove; NFC");

        char sigma = (char)0x3C3;
        char upsilon = (char)0x3C5;
        char nu = (char)0x3BD;
        // not used char PHI = (char)0x3A6;
        char alpha = (char)0x3B1;
        // not used char omega = (char)0x3C9;
        // not used char omicron = (char)0x3BF;
        // not used char epsilon = (char)0x3B5;

        // sigma upsilon nu -> syn
        StringBuffer buf = new StringBuffer();
        buf.append(sigma).append(upsilon).append(nu);
        String syn = buf.toString();
        expect(gl, syn, "syn");

        // sigma alpha upsilon nu -> saun
        buf.setLength(0);
        buf.append(sigma).append(alpha).append(upsilon).append(nu);
        String sayn = buf.toString();
        expect(gl, sayn, "saun");

        // Again, using a smaller rule set
        String rules =
                    "$alpha   = \u03B1;" +
                    "$nu      = \u03BD;" +
                    "$sigma   = \u03C3;" +
                    "$ypsilon = \u03C5;" +
                    "$vowel   = [aeiouAEIOU$alpha$ypsilon];" +
                    "s <>           $sigma;" +
                    "a <>           $alpha;" +
                    "u <>  $vowel { $ypsilon;" +
                    "y <>           $ypsilon;" +
                    "n <>           $nu;";
        RuleBasedTransliterator mini = new RuleBasedTransliterator
            ("mini", rules, Transliterator.REVERSE, null);
        expect(mini, syn, "syn");
        expect(mini, sayn, "saun");

//|    // Transliterate the Greek locale data
//|    Locale el("el");
//|    DateFormatSymbols syms(el, status);
//|    if (U_FAILURE(status)) { errln("FAIL: Transliterator constructor failed"); return; }
//|    int32_t i, count;
//|    const UnicodeString* data = syms.getMonths(count);
//|    for (i=0; i<count; ++i) {
//|        if (data[i].length() == 0) {
//|            continue;
//|        }
//|        UnicodeString out(data[i]);
//|        gl->transliterate(out);
//|        bool_t ok = TRUE;
//|        if (data[i].length() >= 2 && out.length() >= 2 &&
//|            u_isupper(data[i].charAt(0)) && u_islower(data[i].charAt(1))) {
//|            if (!(u_isupper(out.charAt(0)) && u_islower(out.charAt(1)))) {
//|                ok = FALSE;
//|            }
//|        }
//|        if (ok) {
//|            logln(prettify(data[i] + " -> " + out));
//|        } else {
//|            errln(UnicodeString("FAIL: ") + prettify(data[i] + " -> " + out));
//|        }
//|    }
    }

    /**
     * Prefix, suffix support in hex transliterators
     */
    public void TestJ243() {
        // Test default Hex-Any, which should handle
        // \\u, \\U, u+, and U+
        HexToUnicodeTransliterator hex = new HexToUnicodeTransliterator();
        expect(hex, "\\u0041+\\U0042,u+0043uu+0044z", "A+B,CuDz");

        // Try a custom Hex-Any
        // \\uXXXX and &#xXXXX;
        HexToUnicodeTransliterator hex2 = new HexToUnicodeTransliterator("\\\\u###0;&\\#x###0\\;");
        expect(hex2, "\\u61\\u062\\u0063\\u00645\\u66x&#x30;&#x031;&#x0032;&#x00033;",
               "abcd5fx012&#x00033;");

        // Try custom Any-Hex (default is tested elsewhere)
        UnicodeToHexTransliterator hex3 = new UnicodeToHexTransliterator("&\\#x###0;");
        expect(hex3, "012", "&#x30;&#x31;&#x32;");
    }

    public void TestJ329() {

        Object[] DATA = {
            new Boolean(false), "a > b; c > d",
            new Boolean(true),  "a > b; no operator; c > d",
        };

        for (int i=0; i<DATA.length; i+=2) {
            String err = null;
            try {
                Transliterator t = new
                    RuleBasedTransliterator("<ID>",
                                            (String) DATA[i+1],
                                            Transliterator.FORWARD,
                                            null);
            } catch (IllegalArgumentException e) {
                err = e.getMessage();
            }
            boolean gotError = (err != null);
            String desc = (String) DATA[i+1] +
                (gotError ? (" -> error: " + err) : " -> no error");
            if ((err != null) == ((Boolean)DATA[i]).booleanValue()) {
                logln("Ok:   " + desc);
            } else {
                errln("FAIL: " + desc);
            }
        }
    }

    /**
     * Test segments and segment references.
     */
    public void TestSegments() {
        // Array of 3n items
        // Each item is <rules>, <input>, <expected output>
        String[] DATA = {
            "([a-z]) '.' ([0-9]) > $2 '-' $1",
            "abc.123.xyz.456",
            "ab1-c23.xy4-z56",
        };

        for (int i=0; i<DATA.length; i+=3) {
            logln("Pattern: " + Utility.escape(DATA[i]));
            Transliterator t = new RuleBasedTransliterator("<ID>", DATA[i]);
            expect(t, DATA[i+1], DATA[i+2]);
        }
    }

    /**
     * Test cursor positioning outside of the key
     */
    public void TestCursorOffset() {
        // Array of 3n items
        // Each item is <rules>, <input>, <expected output>
        String[] DATA = {
            "pre {alpha} post > | @ ALPHA ;" +
            "eALPHA > beta ;" +
            "pre {beta} post > BETA @@ | ;" +
            "post > xyz",

            "prealphapost prebetapost",
            "prbetaxyz preBETApost",
        };

        for (int i=0; i<DATA.length; i+=3) {
            logln("Pattern: " + Utility.escape(DATA[i]));
            Transliterator t = new RuleBasedTransliterator("<ID>", DATA[i]);
            expect(t, DATA[i+1], DATA[i+2]);
        }
    }

    /**
     * Test zero length and > 1 char length variable values.  Test
     * use of variable refs in UnicodeSets.
     */
    public void TestArbitraryVariableValues() {
        // Array of 3n items
        // Each item is <rules>, <input>, <expected output>
        String[] DATA = {
            "$abe = ab;" +
            "$pat = x[yY]z;" +
            "$ll  = 'a-z';" +
            "$llZ = [$ll];" +
            "$llY = [$ll$pat];" +
            "$emp = ;" +

            "$abe > ABE;" +
            "$pat > END;" +
            "$llZ > 1;" +
            "$llY > 2;" +
            "7$emp 8 > 9;" +
            "",

            "ab xYzxyz stY78",
            "ABE ENDEND 1129",
        };

        for (int i=0; i<DATA.length; i+=3) {
            logln("Pattern: " + Utility.escape(DATA[i]));
            Transliterator t = new RuleBasedTransliterator("<ID>", DATA[i]);
            expect(t, DATA[i+1], DATA[i+2]);
        }
    }

    /**
     * Confirm that the contextStart, contextLimit, start, and limit
     * behave correctly.
     */
    public void TestPositionHandling() {
        // Array of 3n items
        // Each item is <rules>, <input>, <expected output>
        String[] DATA = {
            "a{t} > SS ; {t}b > UU ; {t} > TT ;",
            "xtat txtb", // pos 0,9,0,9
            "xTTaSS TTxUUb",

            "a{t} > SS ; {t}b > UU ; {t} > TT ;",
            "xtat txtb", // pos 2,9,3,8
            "xtaSS TTxUUb",

            "a{t} > SS ; {t}b > UU ; {t} > TT ;",
            "xtat txtb", // pos 3,8,3,8
            "xtaTT TTxTTb",
        };

        // Array of 4n positions -- these go with the DATA array
        // They are: contextStart, contextLimit, start, limit
        int[] POS = {
            0, 9, 0, 9,
            2, 9, 3, 8,
            3, 8, 3, 8,
        };

        int n = DATA.length/3;
        for (int i=0; i<n; i++) {
            Transliterator t = new RuleBasedTransliterator("<ID>", DATA[3*i]);
            Transliterator.Position pos = new Transliterator.Position(
                POS[4*i], POS[4*i+1], POS[4*i+2], POS[4*i+3]);
            ReplaceableString rsource = new ReplaceableString(DATA[3*i+1]);
            t.transliterate(rsource, pos);
            t.finishTransliteration(rsource, pos);
            String result = rsource.toString();
            String exp = DATA[3*i+2];
            expectAux(Utility.escape(DATA[3*i]),
                      DATA[3*i+1] + " -> " + result,
                      result.equals(exp),
                      exp);
        }
    }

    /**
     * Test the Hiragana-Katakana transliterator.
     */
    public void TestHiraganaKatakana() {
        Transliterator hk = Transliterator.getInstance("Hiragana-Katakana");
        Transliterator kh = Transliterator.getInstance("Katakana-Hiragana");

        // Array of 3n items
        // Each item is "hk"|"kh"|"both", <Hiragana>, <Katakana>
        String[] DATA = {
            "both",
            "\u3042\u3090\u3099\u3092\u3050",
            "\u30A2\u30F8\u30F2\u30B0",

            "kh",
            "\u307C\u3051\u3060\u3042\u3093\u30FC",
            "\u30DC\u30F6\u30C0\u30FC\u30F3\u30FC",
        };

        for (int i=0; i<DATA.length; i+=3) {
            switch (DATA[i].charAt(0)) {
            case 'h': // Hiragana-Katakana
                expect(hk, DATA[i+1], DATA[i+2]);
                break;
            case 'k': // Katakana-Hiragana
                expect(kh, DATA[i+2], DATA[i+1]);
                break;
            case 'b': // both
                expect(hk, DATA[i+1], DATA[i+2]);
                expect(kh, DATA[i+2], DATA[i+1]);
                break;
            }
        }

    }

    public void TestCopyJ476() {
        // This is a C++-only copy constructor test
    }

    /**
     * Test inter-Indic transliterators.  These are composed.
     */
    public void TestInterIndic() {
        String ID = "Devanagari-Gujarati";
        Transliterator dg = Transliterator.getInstance(ID);
        if (dg == null) {
            errln("FAIL: getInstance(" + ID + ") returned null");
            return;
        }
        String id = dg.getID();
        if (!id.equals(ID)) {
            errln("FAIL: getInstance(" + ID + ").getID() => " + id);
        }
        String dev = "\u0901\u090B\u0925";
        String guj = "\u0A81\u0A8B\u0AA5";
        expect(dg, dev, guj);
    }

    /**
     * Test filter syntax in IDs. (J23)
     */
    public void TestFilterIDs() {
        String[] DATA = {
            "Any[aeiou]-Hex",
            "Hex[aeiou]-Any",
            "quizzical",
            "q\\u0075\\u0069zz\\u0069c\\u0061l",

            "Any[aeiou]-Hex;Hex[^5]-Any",
            "Any[^5]-Hex;Hex[aeiou]-Any",
            "quizzical",
            "q\\u0075izzical",

            "Null[abc]",
            "Null[abc]",
            "xyz",
            "xyz",
        };

        for (int i=0; i<DATA.length; i+=4) {
            String ID = DATA[i];
            Transliterator t = Transliterator.getInstance(ID);
            expect(t, DATA[i+2], DATA[i+3]);

            // Check the ID
            if (!ID.equals(t.getID())) {
                errln("FAIL: getInstance(" + ID + ").getID() => " +
                      t.getID());
            }

            // Check the inverse
            String uID = DATA[i+1];
            Transliterator u = t.getInverse();
            if (u == null) {
                errln("FAIL: " + ID + ".getInverse() returned NULL");
            } else if (!u.getID().equals(uID)) {
                errln("FAIL: " + ID + ".getInverse().getID() => " +
                      u.getID() + ", expected " + uID);
            }
        }
    }

    /**
     * Test the case mapping transliterators.
     */
    public void TestCaseMap() {
        Transliterator toUpper =
            Transliterator.getInstance("Any-Upper[^xyzXYZ]");
        Transliterator toLower =
            Transliterator.getInstance("Any-Lower[^xyzXYZ]");
        Transliterator toTitle =
            Transliterator.getInstance("Any-Title[^xyzXYZ]");

        expect(toUpper, "The quick brown fox jumped over the lazy dogs.",
               "THE QUICK BROWN FOx JUMPED OVER THE LAzy DOGS.");
        expect(toLower, "The quIck brown fOX jUMPED OVER THE LAzY dogs.",
               "the quick brown foX jumped over the lazY dogs.");
        expect(toTitle, "the quick brown foX caN'T jump over the laZy dogs.",
               "The Quick Brown FoX Can't Jump Over The LaZy Dogs.");
    }

    /**
     * Test the name mapping transliterators.
     */
    public void TestNameMap() {
        Transliterator uni2name =
            Transliterator.getInstance("Any-Name[^abc]");
        Transliterator name2uni =
            Transliterator.getInstance("Name-Any");

        expect(uni2name, "\u00A0abc\u4E01\u00B5\u0A81\uFFFD\uFFFF",
               "{NO-BREAK SPACE}abc{CJK UNIFIED IDEOGRAPH-4E01}{MICRO SIGN}{GUJARATI SIGN CANDRABINDU}{REPLACEMENT CHARACTER}\uFFFF");
        expect(name2uni, "{ NO-BREAK SPACE}abc{  CJK UNIFIED  IDEOGRAPH-4E01  }{x{MICRO SIGN}{GUJARATI SIGN CANDRABINDU}{REPLACEMENT CHARACTER}{",
               "\u00A0abc\u4E01{x\u00B5\u0A81\uFFFD{");
    }

    /**
     * Test liberalized ID syntax.  1006c
     */
    public void TestLiberalizedID() {
        // Some test cases have an expected getID() value of NULL.  This
        // means I have disabled the test case for now.  This stuff is
        // still under development, and I haven't decided whether to make
        // getID() return canonical case yet.  It will all get rewritten
        // with the move to Source-Target/Variant IDs anyway. [aliu]
        String DATA[] = {
            "latin-greek", null /*"Latin-Greek"*/, "case insensitivity",
            "  Null  ", "Null", "whitespace",
            " Latin[a-z]-Greek  ", "Latin[a-z]-Greek", "inline filter",
            "  null  ; latin-greek  ", null /*"Null;Latin-Greek"*/, "compound whitespace",
        };

        for (int i=0; i<DATA.length; i+=3) {
            try {
                Transliterator t = Transliterator.getInstance(DATA[i]);
                if (DATA[i+1] == null || DATA[i+1].equals(t.getID())) {
                    logln("Ok: " + DATA[i+2] +
                          " create ID \"" + DATA[i] + "\" => \"" +
                          t.getID() + "\"");
                } else {
                    errln("FAIL: " + DATA[i+2] +
                          " create ID \"" + DATA[i] + "\" => \"" +
                          t.getID() + "\", exp \"" + DATA[i+1] + "\"");
                }
            } catch (IllegalArgumentException e) {
                errln("FAIL: " + DATA[i+2] +
                      " create ID \"" + DATA[i] + "\"");
            }
        }
    }

    public void TestCreateInstance() {
        Transliterator myTrans = Transliterator.getInstance("Latin-Hangul", Transliterator.REVERSE);
        String newID = myTrans.getID();
        if (!newID.equals("Hangul-Latin")) {
            errln("FAIL: Test for Jitterbug 912 Transliterator::createInstance(id,UTRANS_REVERSE) failed");
        }
    }

    /**
     * Test the normalization transliterator.
     */
    public void TestNormalizationTransliterator() {
        // THE FOLLOWING TWO TABLES ARE COPIED FROM com.ibm.test.normalizer.BasicTest
        // PLEASE KEEP THEM IN SYNC WITH BasicTest.
        String[][] CANON = {
            // Input               Decomposed            Composed
            {"cat",                "cat",                "cat"               },
            {"\u00e0ardvark",      "a\u0300ardvark",     "\u00e0ardvark"     },

            {"\u1e0a",             "D\u0307",            "\u1e0a"            }, // D-dot_above
            {"D\u0307",            "D\u0307",            "\u1e0a"            }, // D dot_above

            {"\u1e0c\u0307",       "D\u0323\u0307",      "\u1e0c\u0307"      }, // D-dot_below dot_above
            {"\u1e0a\u0323",       "D\u0323\u0307",      "\u1e0c\u0307"      }, // D-dot_above dot_below
            {"D\u0307\u0323",      "D\u0323\u0307",      "\u1e0c\u0307"      }, // D dot_below dot_above

            {"\u1e10\u0307\u0323", "D\u0327\u0323\u0307","\u1e10\u0323\u0307"}, // D dot_below cedilla dot_above
            {"D\u0307\u0328\u0323","D\u0328\u0323\u0307","\u1e0c\u0328\u0307"}, // D dot_above ogonek dot_below

            {"\u1E14",             "E\u0304\u0300",      "\u1E14"            }, // E-macron-grave
            {"\u0112\u0300",       "E\u0304\u0300",      "\u1E14"            }, // E-macron + grave
            {"\u00c8\u0304",       "E\u0300\u0304",      "\u00c8\u0304"      }, // E-grave + macron

            {"\u212b",             "A\u030a",            "\u00c5"            }, // angstrom_sign
            {"\u00c5",             "A\u030a",            "\u00c5"            }, // A-ring

            {"\u00fdffin",         "y\u0301ffin",        "\u00fdffin"        },	//updated with 3.0
            {"\u00fd\uFB03n",      "y\u0301\uFB03n",     "\u00fd\uFB03n"     },	//updated with 3.0

            {"Henry IV",           "Henry IV",           "Henry IV"          },
            {"Henry \u2163",       "Henry \u2163",       "Henry \u2163"      },

            {"\u30AC",             "\u30AB\u3099",       "\u30AC"            }, // ga (Katakana)
            {"\u30AB\u3099",       "\u30AB\u3099",       "\u30AC"            }, // ka + ten
            {"\uFF76\uFF9E",       "\uFF76\uFF9E",       "\uFF76\uFF9E"      }, // hw_ka + hw_ten
            {"\u30AB\uFF9E",       "\u30AB\uFF9E",       "\u30AB\uFF9E"      }, // ka + hw_ten
            {"\uFF76\u3099",       "\uFF76\u3099",       "\uFF76\u3099"      }, // hw_ka + ten

            {"A\u0300\u0316",      "A\u0316\u0300",      "\u00C0\u0316"      },
        };

        String[][] COMPAT = {
            // Input               Decomposed            Composed
            {"\uFB4f",             "\u05D0\u05DC",       "\u05D0\u05DC"      }, // Alef-Lamed vs. Alef, Lamed

            {"\u00fdffin",         "y\u0301ffin",        "\u00fdffin"        },	//updated for 3.0
            {"\u00fd\uFB03n",      "y\u0301ffin",        "\u00fdffin"        }, // ffi ligature -> f + f + i

            {"Henry IV",           "Henry IV",           "Henry IV"          },
            {"Henry \u2163",       "Henry IV",           "Henry IV"          },

            {"\u30AC",             "\u30AB\u3099",       "\u30AC"            }, // ga (Katakana)
            {"\u30AB\u3099",       "\u30AB\u3099",       "\u30AC"            }, // ka + ten

            {"\uFF76\u3099",       "\u30AB\u3099",       "\u30AC"            }, // hw_ka + ten
        };

        Transliterator NFD = Transliterator.getInstance("NFD");
        Transliterator NFC = Transliterator.getInstance("NFC");
        for (int i=0; i<CANON.length; ++i) {
            String in = CANON[i][0];
            String expd = CANON[i][1];
            String expc = CANON[i][2];
            expect(NFD, in, expd);
            expect(NFC, in, expc);
        }

        Transliterator NFKD = Transliterator.getInstance("NFKD");
        Transliterator NFKC = Transliterator.getInstance("NFKC");
        for (int i=0; i<COMPAT.length; ++i) {
            String in = COMPAT[i][0];
            String expkd = COMPAT[i][1];
            String expkc = COMPAT[i][2];
            expect(NFKD, in, expkd);
            expect(NFKC, in, expkc);
        }

        Transliterator t = Transliterator.getInstance("NFD; [x]Remove");
        expect(t, "\u010dx", "c\u030C");
    }

    /**
     * Test compound RBT rules.
     */
    public void TestCompoundRBT() {
        // Careful with spacing and ';' here:  Phrase this exactly
        // as toRules() is going to return it.  If toRules() changes
        // with regard to spacing or ';', then adjust this string.
        String rule = "::Hex-Any;\n" +
                      "::Any-Lower;\n" +
                      "a > '.A.';\n" +
                      "b > '.B.';\n" +
                      "::Any[^t]-Upper;";
        Transliterator t = Transliterator.createFromRules("Test", rule, Transliterator.FORWARD);
        if (t == null) {
            errln("FAIL: createFromRules failed");
            return;
        }
        expect(t, "\u0043at in the hat, bat on the mat",
               "C.A.t IN tHE H.A.t, .B..A.t ON tHE M.A.t");
        String r = t.toRules(true);
        if (r.equals(rule)) {
            logln("OK: toRules() => " + r);
        } else {
            errln("FAIL: toRules() => " + r +
                  ", expected " + rule);
        }

        // Now test toRules
        t = Transliterator.getInstance("Greek-Latin; Latin-Cyrillic", Transliterator.FORWARD);
        if (t == null) {
            errln("FAIL: createInstance failed");
            return;
        }
        String exp = "::Greek-Latin;\n::Latin-Cyrillic;";
        r = t.toRules(true);
        if (!r.equals(exp)) {
            errln("FAIL: toRules() => " + r +
                  ", expected " + exp);
        } else {
            logln("OK: toRules() => " + r);
        }

        // Round trip the result of toRules
        t = Transliterator.createFromRules("Test", r, Transliterator.FORWARD);
        if (t == null) {
            errln("FAIL: createFromRules #2 failed");
            return;
        } else {
            logln("OK: createFromRules(" + r + ") succeeded");
        }

        // Test toRules again
        r = t.toRules(true);
        if (!r.equals(exp)) {
            errln("FAIL: toRules() => " + r +
                  ", expected " + exp);
        } else {
            logln("OK: toRules() => " + r);
        }

        // Test Foo(Bar) IDs.  Careful with spacing in id; make it conform
        // to what the regenerated ID will look like.
        String id = "Upper(Lower);(NFKC)";
        t = Transliterator.getInstance(id, Transliterator.FORWARD);
        if (t == null) {
            errln("FAIL: createInstance #2 failed");
            return;
        }
        if (t.getID().equals(id)) {
            logln("OK: created " + id);
        } else {
            errln("FAIL: createInstance(" + id +
                  ").getID() => " + t.getID());
        }

        Transliterator u = t.getInverse();
        if (u == null) {
            errln("FAIL: createInverse failed");
            return;
        }
        exp = "NFKC();Lower(Upper)";
        if (u.getID().equals(exp)) {
            logln("OK: createInverse(" + id + ") => " +
                  u.getID());
        } else {
            errln("FAIL: createInverse(" + id + ") => " +
                  u.getID());
        }
    }

    /**
     * Compound filter semantics were orginially not implemented
     * correctly.  Originally, each component filter f(i) is replaced by
     * f'(i) = f(i) && g, where g is the filter for the compound
     * transliterator.
     *
     * From Mark:
     *
     * Suppose and I have a transliterator X. Internally X is
     * "Greek-Latin; Latin-Cyrillic; Any-Lower". I use a filter [^A].
     *
     * The compound should convert all greek characters (through latin) to
     * cyrillic, then lowercase the result. The filter should say "don't
     * touch 'A' in the original". But because an intermediate result
     * happens to go through "A", the Greek Alpha gets hung up.
     */
    public void TestCompoundFilter() {
        Transliterator t = Transliterator.getInstance
            ("Greek-Latin; Latin-Greek; Lower", Transliterator.FORWARD);
        t.setFilter(new UnicodeSet("[^A]"));

        // Only the 'A' at index 1 should remain unchanged
        expect(t,
               CharsToUnicodeString("BA\\u039A\\u0391"),
               CharsToUnicodeString("\\u03b2A\\u03ba\\u03b1"));
    }

    /**
     * Test the "Remove" transliterator.
     */
    public void TestRemove() {
        Transliterator t = Transliterator.getInstance("Remove[aeiou]");
        expect(t, "The quick brown fox.",
               "Th qck brwn fx.");
    }

    public void TestToRules() {
        String RBT = "rbt";
        String SET = "set";
        String[] DATA = {
            RBT,
            "$a=\\u4E61; [$a] > A;",
            "[\\u4E61] > A;",

            RBT,
            "$white=[[:Zs:][:Zl:]]; $white{a} > A;",
            "[[:Zs:][:Zl:]]{a} > A;",

            SET,
            "[[:Zs:][:Zl:]]",
            "[[:Zs:][:Zl:]]",

            SET,
            "[:Ps:]",
            "[:Ps:]",

            SET,
            "[:L:]",
            "[:L:]",

            SET,
            "[[:L:]-[A]]",
            "[[:L:]-[A]]",

            SET,
            "[~[:Lu:][:Ll:]]",
            "[~[:Lu:][:Ll:]]",

            SET,
            "[~[a-z]]",
            "[~[a-z]]",

            RBT,
            "$white=[:Zs:]; $black=[^$white]; $black{a} > A;",
            "[^[:Zs:]]{a} > A;",

            RBT,
            "$a=[:Zs:]; $b=[[a-z]-$a]; $b{a} > A;",
            "[[a-z]-[:Zs:]]{a} > A;",

            RBT,
            "$a=[:Zs:]; $b=[$a&[a-z]]; $b{a} > A;",
            "[[:Zs:]&[a-z]]{a} > A;",

            RBT,
            "$a=[:Zs:]; $b=[x$a]; $b{a} > A;",
            "[x[:Zs:]]{a} > A;",
        };

        for (int d=0; d < DATA.length; d+=3) {
            if (DATA[d] == RBT) {
                // Transliterator test
                Transliterator t = Transliterator.createFromRules("ID",
                                       DATA[d+1], Transliterator.FORWARD);
                if (t == null) {
                    errln("FAIL: createFromRules failed");
                    return;
                }
                String rules, escapedRules;
                rules = t.toRules(false);
                escapedRules = t.toRules(true);
                String expRules = Utility.unescape(DATA[d+2]);
                String expEscapedRules = DATA[d+2];
                if (rules.equals(expRules)) {
                    logln("Ok: " + DATA[d+1] +
                          " => " + Utility.escape(rules));
                } else {
                    errln("FAIL: " + DATA[d+1] +
                          " => " + Utility.escape(rules + ", exp " + expRules));
                }
                if (escapedRules.equals(expEscapedRules)) {
                    logln("Ok: " + DATA[d+1] +
                          " => " + escapedRules);
                } else {
                    errln("FAIL: " + DATA[d+1] +
                          " => " + escapedRules + ", exp " + expEscapedRules);
                }

            } else {
                // UnicodeSet test
                String pat = DATA[d+1];
                String expToPat = DATA[d+2];
                UnicodeSet set = new UnicodeSet(pat);

                // Adjust spacing etc. as necessary.
                String toPat;
                toPat = set.toPattern(true);
                if (expToPat.equals(toPat)) {
                    logln("Ok: " + pat +
                          " => " + toPat);
                } else {
                    errln("FAIL: " + pat +
                          " => " + Utility.escape(toPat) +
                          ", exp " + Utility.escape(pat));
                }
            }
        }
    }

    public void TestContext() {
        Transliterator.Position pos = new Transliterator.Position(0, 2, 0, 1); // cs cl s l

        expect("de > x; {d}e > y;",
               "de",
               "ye",
               pos);

        expect("ab{c} > z;",
               "xadabdabcy",
               "xadabdabzy");
    }

    static final String CharsToUnicodeString(String s) {
        return Utility.unescape(s);
    }

    public void TestSupplemental() {

        expect(CharsToUnicodeString("$a=\\U00010300; $s=[\\U00010300-\\U00010323];" +
                                    "a > $a; $s > i;"),
               CharsToUnicodeString("ab\\U0001030Fx"),
               CharsToUnicodeString("\\U00010300bix"));

        expect(CharsToUnicodeString("$a=[a-z\\U00010300-\\U00010323];" +
                                    "$b=[A-Z\\U00010400-\\U0001044D];" +
                                    "($a)($b) > $2 $1;"),
               CharsToUnicodeString("aB\\U00010300\\U00010400c\\U00010401\\U00010301D"),
               CharsToUnicodeString("Ba\\U00010400\\U00010300\\U00010401cD\\U00010301"));

        // k|ax\\U00010300xm

        // k|a\\U00010400\\U00010300xm
        // ky|\\U00010400\\U00010300xm
        // ky\\U00010400|\\U00010300xm

        // ky\\U00010400|\\U00010300\\U00010400m
        // ky\\U00010400y|\\U00010400m
        expect(CharsToUnicodeString("$a=[a\\U00010300-\\U00010323];" +
                                    "$a {x} > | @ \\U00010400;" +
                                    "{$a} [^\\u0000-\\uFFFF] > y;"),
               CharsToUnicodeString("kax\\U00010300xm"),
               CharsToUnicodeString("ky\\U00010400y\\U00010400m"));
    }

    public void TestQuantifier() {

        // Make sure @ in a quantified anteContext works
        expect("a+ {b} > | @@ c; A > a; (a+ c) > '(' $1 ')';",
               "AAAAAb",
               "aaa(aac)");

        // Make sure @ in a quantified postContext works
        expect("{b} a+ > c @@ |; (a+) > '(' $1 ')';",
               "baaaaa",
               "caa(aaa)");

        // Make sure @ in a quantified postContext with seg ref works
        expect("{(b)} a+ > $1 @@ |; (a+) > '(' $1 ')';",
               "baaaaa",
               "baa(aaa)");

        // Make sure @ past ante context doesn't enter ante context
        Transliterator.Position pos = new Transliterator.Position(0, 5, 3, 5);
        expect("a+ {b} > | @@ c; x > y; (a+ c) > '(' $1 ')';",
               "xxxab",
               "xxx(ac)",
               pos);

        // Make sure @ past post context doesn't pass limit
        Transliterator.Position pos2 = new Transliterator.Position(0, 4, 0, 2);
        expect("{b} a+ > c @@ |; x > y; a > A;",
               "baxx",
               "caxx",
               pos2);

        // Make sure @ past post context doesn't enter post context
        expect("{b} a+ > c @@ |; x > y; a > A;",
               "baxx",
               "cayy");

        expect("(ab)? c > d;",
               "c abc ababc",
               "d d abd");

        // NOTE: The (ab)+ when referenced just yields a single "ab",
        // not the full sequence of them.  This accords with perl behavior.
        expect("(ab)+ {x} > '(' $1 ')';",
               "x abx ababxy",
               "x ab(ab) abab(ab)y");

        expect("b+ > x;",
               "ac abc abbc abbbc",
               "ac axc axc axc");

        expect("[abc]+ > x;",
               "qac abrc abbcs abtbbc",
               "qx xrx xs xtx");

        expect("q{(ab)+} > x;",
               "qa qab qaba qababc qaba",
               "qa qx qxa qxc qxa");

        expect("q(ab)* > x;",
               "qa qab qaba qababc",
               "xa x xa xc");

        // NOTE: The (ab)+ when referenced just yields a single "ab",
        // not the full sequence of them.  This accords with perl behavior.
        expect("q(ab)* > '(' $1 ')';",
               "qa qab qaba qababc",
               "()a (ab) (ab)a (ab)c");

        // 'foo'+ and 'foo'* -- the quantifier should apply to the entire
        // quoted string
        expect("'ab'+ > x;",
               "bb ab ababb",
               "bb x xb");

        // $foo+ and $foo* -- the quantifier should apply to the entire
        // variable reference
        expect("$var = ab; $var+ > x;",
               "bb ab ababb",
               "bb x xb");
    }

    static class TestFact implements Transliterator.Factory {
        static class NameableNullTrans extends NullTransliterator {
            public NameableNullTrans(String id) {
                setID(id);
            }
        };
        String id;
        public TestFact(String theID) {
            id = theID;
        }
        public Transliterator getInstance(String ignoredID) {
            return new NameableNullTrans(id);
        }
    };

    public void TestSTV() {
        Enumeration es = Transliterator.getAvailableSources();
        for (int i=0; es.hasMoreElements(); ++i) {
            String source = (String) es.nextElement();
            logln("" + i + ": " + source);
            if (source.length() == 0) {
                errln("FAIL: empty source");
                continue;
            }
            Enumeration et = Transliterator.getAvailableTargets(source);
            for (int j=0; et.hasMoreElements(); ++j) {
                String target = (String) et.nextElement();
                logln(" " + j + ": " + target);
                if (target.length() == 0) {
                    errln("FAIL: empty target");
                    continue;
                }
                Enumeration ev = Transliterator.getAvailableVariants(source, target);
                for (int k=0; ev.hasMoreElements(); ++k) {
                    String variant = (String) ev.nextElement();
                    if (variant.length() == 0) {
                        logln("  " + k + ": <empty>");
                    } else {
                        logln("  " + k + ": " + variant);
                    }
                }
            }
        }

        // Test registration
        String[] IDS = { "Fieruwer", "Seoridf-Sweorie", "Oewoir-Oweri/Vsie" };
        for (int i=0; i<3; ++i) {
            Transliterator.registerFactory(IDS[i], new TestFact(IDS[i]));
            try {
                Transliterator t = Transliterator.getInstance(IDS[i]);
                if (t.getID().equals(IDS[i])) {
                    logln("Ok: Registration/creation succeeded for ID " +
                          IDS[i]);
                } else {
                    errln("FAIL: Registration of ID " +
                          IDS[i] + " creates ID " + t.getID());
                }
                Transliterator.unregister(IDS[i]);
                try {
                    t = Transliterator.getInstance(IDS[i]);
                    errln("FAIL: Unregistration failed for ID " +
                          IDS[i] + "; still receiving ID " + t.getID());
                } catch (IllegalArgumentException e2) {
                    // Good; this is what we expect
                    logln("Ok; Unregistered " + IDS[i]);
                }
            } catch (IllegalArgumentException e) {
                errln("FAIL: Registration/creation failed for ID " +
                      IDS[i]);
            }
        }
    }

    /**
     * Test inverse of Greek-Latin; Title()
     */
    public void TestCompoundInverse() {
        Transliterator t = Transliterator.getInstance
            ("Greek-Latin; Title()", Transliterator.REVERSE);
        if (t == null) {
            errln("FAIL: createInstance");
            return;
        }
        String exp = "(Title);Latin-Greek";
        if (t.getID().equals(exp)) {
            logln("Ok: inverse of \"Greek-Latin; Title()\" is \"" +
                  t.getID());
        } else {
            errln("FAIL: inverse of \"Greek-Latin; Title()\" is \"" +
                  t.getID() + "\", expected \"" + exp + "\"");
        }
    }

    /**
     * Test NFD chaining with RBT
     */
    public void TestNFDChainRBT() {
        Transliterator t = Transliterator.createFromRules(
                               "TEST", "::NFD; aa > Q; a > q;",
                               Transliterator.FORWARD);
        expect(t, "aa", "Q");
    }

    /**
     * Inverse of "Null" should be "Null". (J21)
     */
    public void TestNullInverse() {
        Transliterator t = Transliterator.getInstance("Null");
        Transliterator u = t.getInverse();
        if (!u.getID().equals("Null")) {
            errln("FAIL: Inverse of Null should be Null");
        }
    }

    /**
     * Check ID of inverse of alias. (J22)
     */
    public void TestAliasInverseID() {
        String ID = "Latin-Hangul"; // This should be any alias ID with an inverse
        Transliterator t = Transliterator.getInstance(ID);
        Transliterator u = t.getInverse();
        String exp = "Hangul-Latin";
        String got = u.getID();
        if (!got.equals(exp)) {
            errln("FAIL: Inverse of " + ID + " is " + got +
                  ", expected " + exp);
        }
    }

    /**
     * Test IDs of inverses of compound transliterators. (J20)
     */
    public void TestCompoundInverseID() {
        String ID = "Latin-Jamo;NFC(NFD)";
        Transliterator t = Transliterator.getInstance(ID);
        Transliterator u = t.getInverse();
        String exp = "NFD(NFC);Jamo-Latin";
        String got = u.getID();
        if (!got.equals(exp)) {
            errln("FAIL: Inverse of " + ID + " is " + got +
                  ", expected " + exp);
        }
    }

    /**
     * Test undefined variable.
     */
    public void TestUndefinedVariable() {
        String rule = "$initial } a <> \u1161;";
        try {
            Transliterator t = new RuleBasedTransliterator("<ID>", rule);
            t = null;
        } catch (IllegalArgumentException e) {
            logln("OK: Got exception for " + rule + ", as expected: " +
                  e.getMessage());
            return;
        }
        errln("Fail: bogus rule " + rule + " compiled without error");
    }

    /**
     * Test empty context.
     */
    public void TestEmptyContext() {
        expect(" { a } > b;", "xay a ", "xby b ");
    }

    /**
     * Test compound filter ID syntax
     */
    public void TestCompoundFilterID() {
        String[] DATA = {
            // Col. 1 = ID or rule set (latter must start with #)

            // = columns > 1 are null if expect col. 1 to be illegal =

            // Col. 2 = direction, "F..." or "R..."
            // Col. 3 = source string
            // Col. 4 = exp result

            "[abc]; [abc]", null, null, null, // multiple filters
            "Latin-Greek; [abc];", null, null, null, // misplaced filter
            "[b]; Latin-Greek; Upper; ([xyz])", "F", "abc", "a\u0392c",
            "[b]; (Lower); Latin-Greek; Upper(); ([\u0392])", "R", "\u0391\u0392\u0393", "\u0391b\u0393",
            "#\n::[b]; ::Latin-Greek; ::Upper; ::([xyz]);", "F", "abc", "a\u0392c",
            "#\n::[b]; ::(Lower); ::Latin-Greek; ::Upper(); ::([\u0392]);", "R", "\u0391\u0392\u0393", "\u0391b\u0393",
        };

        for (int i=0; i<DATA.length; i+=4) {
            String id = DATA[i];
            int direction = (DATA[i+1] != null && DATA[i+1].charAt(0) == 'R') ?
                Transliterator.REVERSE : Transliterator.FORWARD;
            String source = DATA[i+2];
            String exp = DATA[i+3];
            boolean expOk = (DATA[i+1] != null);
            Transliterator t = null;
            IllegalArgumentException e = null;
            try {
                if (id.charAt(0) == '#') {
                    t = Transliterator.createFromRules("ID", id, direction);
                } else {
                    t = Transliterator.getInstance(id, direction);
                }
            } catch (IllegalArgumentException ee) {
                e = ee;
            }
            boolean ok = (t != null && e == null);
            if (ok == expOk) {
                logln("Ok: " + id + " => " + t +
                      (e != null ? (", " + e.getMessage()) : ""));
                if (source != null) {
                    expect(t, source, exp);
                }
            } else {
                errln("FAIL: " + id + " => " + t +
                      (e != null ? (", " + e.getMessage()) : ""));
            }
        }
    }

    /**
     * Test new property set syntax
     */
    public void TestPropertySet() {
        expect("a>A; \\p{Lu}>x; \\p{Any}>y;", "abcDEF", "Ayyxxx");
        expect("(.+)>'[' $1 ']';", " a stitch \n in time \r saves 9",
               "[ a stitch ]\n[ in time ]\r[ saves 9]");
    }

    /**
     * Test various failure points of the new 2.0 engine.
     */
    public void TestNewEngine() {
        Transliterator t = Transliterator.getInstance("Latin-Hiragana");
        // Katakana should be untouched
        expect(t, "a\u3042\u30A2", "\u3042\u3042\u30A2");

        Transliterator a =
            Transliterator.createFromRules("a", "a > A;", Transliterator.FORWARD);
        Transliterator A =
            Transliterator.createFromRules("A", "A > b;", Transliterator.FORWARD);

        Transliterator array[] = new Transliterator[] {
            a,
            Transliterator.getInstance("NFD"),
            A };

        t = new CompoundTransliterator(array, new UnicodeSet("[:Ll:]"));

        expect(t, "aAaA", "bAbA");

        expect("$smooth = x; $macron = q; [:^L:] { ([aeiouyAEIOUY] $macron?) } [^aeiouyAEIOUY$smooth$macron] > | $1 $smooth ;",
               "a",
               "ax");
        
        String gr =
            "$ddot = \u0308 ;" +
            "$lcgvowel = [\u03b1\u03b5\u03b7\u03b9\u03bf\u03c5\u03c9] ;" +
            "$rough = \u0314 ;" +
            "($lcgvowel+ $ddot?) $rough > h | $1 ;" +
            "\u03b1 <> a ;" +
            "$rough <> h ;";
            
        expect(gr, "\u03B1\u0314", "ha");
    }

    /**
     * Test quantified segment behavior.  We want:
     * ([abc])+ > x $1 x; applied to "cba" produces "xax"
     */
    public void TestQuantifiedSegment() {
        // The normal case
        expect("([abc]+) > x $1 x;", "cba", "xcbax");

        // The tricky case; the quantifier is around the segment
        expect("([abc])+ > x $1 x;", "cba", "xax");

        // Tricky case in reverse direction
        expect("([abc])+ { q > x $1 x;", "cbaq", "cbaxax");

        // Check post-context segment
        expect("{q} ([a-d])+ > '(' $1 ')';", "ddqcba", "dd(a)cba");

        // Test toRule/toPattern for non-quantified segment.
        // Careful with spacing here.
        String r = "([a-c]){q} > x $1 x;";
        Transliterator t = Transliterator.createFromRules("ID", r, Transliterator.FORWARD);
        String rr = t.toRules(true);
        if (!r.equals(rr)) {
            errln("FAIL: \"" + r + "\" x toRules() => \"" + rr + "\"");
        } else {
            logln("Ok: \"" + r + "\" x toRules() => \"" + rr + "\"");
        }

        // Test toRule/toPattern for quantified segment.
        // Careful with spacing here.
        r = "([a-c])+{q} > x $1 x;";
        t = Transliterator.createFromRules("ID", r, Transliterator.FORWARD);
        rr = t.toRules(true);
        if (!r.equals(rr)) {
            errln("FAIL: \"" + r + "\" x toRules() => \"" + rr + "\"");
        } else {
            logln("Ok: \"" + r + "\" x toRules() => \"" + rr + "\"");
        }
    }

    //======================================================================
    // Ram's tests
    //======================================================================
 /* this test performs  test of rules in ISO 15915 */
    public void  TestDevanagariLatinRT(){
        int MAX_LEN= 52;
        String[]  source = {
            "bh\u0101rata",
            "kra",
            "k\u1E63a",
            "khra",
            "gra",
            "\u1E45ra",
            "cra",
            "chra",
            "j\u00F1a",
            "jhra",
            "\u00F1ra",
            "\u1E6Dya",
            "\u1E6Dhra",
            "\u1E0Dya",
        //"r\u0323ya", // \u095c is not valid in Devanagari
            "\u1E0Dhya",
            "\u1E5Bhra",
            "\u1E47ra",
            "tta",
            "thra",
            "dda",
            "dhra",
            "nna",
            "pra",
            "phra",
            "bra",
            "bhra",
            "mra",
            "\u1E49ra",
        //"l\u0331ra",
            "yra",
            "\u1E8Fra",
        //"l-",
            "vra",
            "\u015Bra",
            "\u1E63ra",
            "sra",
            "hma",
            "\u1E6D\u1E6Da",
            "\u1E6D\u1E6Dha",
            "\u1E6Dh\u1E6Dha",
            "\u1E0D\u1E0Da",
            "\u1E0D\u1E0Dha",
            "\u1E6Dya",
            "\u1E6Dhya",
            "\u1E0Dya",
            "\u1E0Dhya",
            // Not roundtrippable --
            // \u0939\u094d\u094d\u092E  - hma
            // \u0939\u094d\u092E         - hma
            // CharsToUnicodeString("hma"),
            "hya",
            "\u015Br\u0325a",
            "\u015Bca",
            "\u0115",
            "san\u0304j\u012Bb s\u0113nagupta",
            "\u0101nand vaddir\u0101ju",
            "\u0101",
            "a"
        };
        String[]  expected = {
            "\u092D\u093E\u0930\u0924",    /* bha\u0304rata */
            "\u0915\u094D\u0930",          /* kra         */
            "\u0915\u094D\u0937",          /* ks\u0323a  */
            "\u0916\u094D\u0930",          /* khra        */
            "\u0917\u094D\u0930",          /* gra         */
            "\u0919\u094D\u0930",          /* n\u0307ra  */
            "\u091A\u094D\u0930",          /* cra         */
            "\u091B\u094D\u0930",          /* chra        */
            "\u091C\u094D\u091E",          /* jn\u0303a  */
            "\u091D\u094D\u0930",          /* jhra        */
            "\u091E\u094D\u0930",          /* n\u0303ra  */
            "\u091F\u094D\u092F",          /* t\u0323ya  */
            "\u0920\u094D\u0930",          /* t\u0323hra */
            "\u0921\u094D\u092F",          /* d\u0323ya  */
        //"\u095C\u094D\u092F",          /* r\u0323ya  */ // \u095c is not valid in Devanagari
            "\u0922\u094D\u092F",          /* d\u0323hya */
            "\u0922\u093C\u094D\u0930",    /* r\u0323hra */
            "\u0923\u094D\u0930",          /* n\u0323ra  */
            "\u0924\u094D\u0924",          /* tta         */
            "\u0925\u094D\u0930",          /* thra        */
            "\u0926\u094D\u0926",          /* dda         */
            "\u0927\u094D\u0930",          /* dhra        */
            "\u0928\u094D\u0928",          /* nna         */
            "\u092A\u094D\u0930",          /* pra         */
            "\u092B\u094D\u0930",          /* phra        */
            "\u092C\u094D\u0930",          /* bra         */
            "\u092D\u094D\u0930",          /* bhra        */
            "\u092E\u094D\u0930",          /* mra         */
            "\u0929\u094D\u0930",          /* n\u0331ra  */
        //"\u0934\u094D\u0930",          /* l\u0331ra  */
            "\u092F\u094D\u0930",          /* yra         */
            "\u092F\u093C\u094D\u0930",    /* y\u0307ra  */
        //"l-",
            "\u0935\u094D\u0930",          /* vra         */
            "\u0936\u094D\u0930",          /* s\u0301ra  */
            "\u0937\u094D\u0930",          /* s\u0323ra  */
            "\u0938\u094D\u0930",          /* sra         */
            "\u0939\u094d\u092E",          /* hma         */
            "\u091F\u094D\u091F",          /* t\u0323t\u0323a  */
            "\u091F\u094D\u0920",          /* t\u0323t\u0323ha */
            "\u0920\u094D\u0920",          /* t\u0323ht\u0323ha*/
            "\u0921\u094D\u0921",          /* d\u0323d\u0323a  */
            "\u0921\u094D\u0922",          /* d\u0323d\u0323ha */
            "\u091F\u094D\u092F",          /* t\u0323ya  */
            "\u0920\u094D\u092F",          /* t\u0323hya */
            "\u0921\u094D\u092F",          /* d\u0323ya  */
            "\u0922\u094D\u092F",          /* d\u0323hya */
        // "hma",                         /* hma         */
            "\u0939\u094D\u092F",          /* hya         */
            "\u0936\u0943",                /* s\u0301r\u0325a  */
            "\u0936\u094D\u091A",          /* s\u0301ca  */
            "\u090d",                      /* e\u0306    */
            "\u0938\u0902\u091C\u0940\u092C\u094D \u0938\u0947\u0928\u0917\u0941\u092A\u094D\u0924",
            "\u0906\u0928\u0902\u0926\u094D \u0935\u0926\u094D\u0926\u093F\u0930\u093E\u091C\u0941",
            "\u0906",
            "\u0905",
        };

        Transliterator latinToDev=Transliterator.getInstance("Latin-Devanagari", Transliterator.FORWARD );
        Transliterator devToLatin=Transliterator.getInstance("Devanagari-Latin", Transliterator.FORWARD);

        String gotResult;
        for(int i= 0; i<MAX_LEN; i++){
            gotResult = source[i];
            expect(latinToDev,(source[i]),(expected[i]));
            expect(devToLatin,(expected[i]),(source[i]));
        }

    }
    public void  TestTeluguLatinRT(){
        int MAX_LEN=10;
        String[]  source = {   
            "raghur\u0101m vi\u015Bvan\u0101dha",                           /* Raghuram Viswanadha    */
            "\u0101nand vaddir\u0101ju",                                    /* Anand Vaddiraju 	      */ 	   
            "r\u0101j\u012Bv ka\u015Barab\u0101da",                         /* Rajeev Kasarabada      */ 
            "san\u0304j\u012Bv ka\u015Barab\u0101da",                       /* sanjeev kasarabada     */
            "san\u0304j\u012Bb sen'gupta",                                  /* sanjib sengupata 	  */ 	   
            "amar\u0113ndra hanum\u0101nula",                               /* Amarendra hanumanula   */ 
            "ravi kum\u0101r vi\u015Bvan\u0101dha",                         /* Ravi Kumar Viswanadha  */
            "\u0101ditya kandr\u0113gula",                                  /* Aditya Kandregula      */
            "\u015Br\u012Bdhar ka\u1E47\u1E6Dama\u015Be\u1E6D\u1E6Di",      /* Shridhar Kantamsetty   */
            "m\u0101dhav de\u015Be\u1E6D\u1E6Di"                            /* Madhav Desetty         */
        };

        String[]  expected = {
            "\u0c30\u0c18\u0c41\u0c30\u0c3e\u0c2e\u0c4d \u0c35\u0c3f\u0c36\u0c4d\u0c35\u0c28\u0c3e\u0c27",
            "\u0c06\u0c28\u0c02\u0c26\u0c4d \u0C35\u0C26\u0C4D\u0C26\u0C3F\u0C30\u0C3E\u0C1C\u0C41",
            "\u0c30\u0c3e\u0c1c\u0c40\u0c35\u0c4d \u0c15\u0c36\u0c30\u0c2c\u0c3e\u0c26",
            "\u0c38\u0c02\u0c1c\u0c40\u0c35\u0c4d \u0c15\u0c36\u0c30\u0c2c\u0c3e\u0c26",
            "\u0c38\u0c02\u0c1c\u0c40\u0c2c\u0c4d \u0c38\u0c46\u0c28\u0c4d\u0c17\u0c41\u0c2a\u0c4d\u0c24",
            "\u0c05\u0c2e\u0c30\u0c47\u0c02\u0c26\u0c4d\u0c30 \u0c39\u0c28\u0c41\u0c2e\u0c3e\u0c28\u0c41\u0c32",
            "\u0c30\u0c35\u0c3f \u0c15\u0c41\u0c2e\u0c3e\u0c30\u0c4d \u0c35\u0c3f\u0c36\u0c4d\u0c35\u0c28\u0c3e\u0c27",
            "\u0c06\u0c26\u0c3f\u0c24\u0c4d\u0c2f \u0C15\u0C02\u0C26\u0C4D\u0C30\u0C47\u0C17\u0C41\u0c32",
            "\u0c36\u0c4d\u0c30\u0c40\u0C27\u0C30\u0C4D \u0c15\u0c02\u0c1f\u0c2e\u0c36\u0c46\u0c1f\u0c4d\u0c1f\u0c3f",
            "\u0c2e\u0c3e\u0c27\u0c35\u0c4d \u0c26\u0c46\u0c36\u0c46\u0c1f\u0c4d\u0c1f\u0c3f",
        };


        Transliterator latinToDev=Transliterator.getInstance("Latin-Telugu", Transliterator.FORWARD);
        Transliterator devToLatin=Transliterator.getInstance("Telugu-Latin", Transliterator.FORWARD);

        String gotResult;
        for(int i= 0; i<MAX_LEN; i++){
            gotResult = source[i];
            expect(latinToDev,(source[i]),(expected[i]));
            expect(devToLatin,(expected[i]),(source[i]));
        }
    }

    public void  TestSanskritLatinRT(){
        int MAX_LEN =15;
        String[]  source = {
            "rmk\u1E63\u0113t",
            "\u015Br\u012Bmad",
            "bhagavadg\u012Bt\u0101",
            "adhy\u0101ya",
            "arjuna",
            "vi\u1E63\u0101da",
            "y\u014Dga",
            "dhr\u0325tar\u0101\u1E63\u1E6Dra",
            "uv\u0101cr\u0325a",
            "dharmak\u1E63\u0113tr\u0113",
            "kuruk\u1E63\u0113tr\u0113",
            "samav\u0113t\u0101",
            "yuyutsava-\u1E25",
            "m\u0101mak\u0101-\u1E25",
        // "p\u0101\u1E47\u1E0Dav\u0101\u015Bcaiva",
            "kimakurvata",
            "san\u0304java",
        };
        String[]  expected = {
            "\u0930\u094D\u092E\u094D\u0915\u094D\u0937\u0947\u0924\u094D",
            "\u0936\u094d\u0930\u0940\u092e\u0926\u094d",
            "\u092d\u0917\u0935\u0926\u094d\u0917\u0940\u0924\u093e",
            "\u0905\u0927\u094d\u092f\u093e\u092f",
            "\u0905\u0930\u094d\u091c\u0941\u0928",
            "\u0935\u093f\u0937\u093e\u0926",
            "\u092f\u094b\u0917",
            "\u0927\u0943\u0924\u0930\u093e\u0937\u094d\u091f\u094d\u0930",
            "\u0909\u0935\u093E\u091A\u0943",
            "\u0927\u0930\u094d\u092e\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947",
            "\u0915\u0941\u0930\u0941\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947",
            "\u0938\u092e\u0935\u0947\u0924\u093e",
            "\u092f\u0941\u092f\u0941\u0924\u094d\u0938\u0935\u0903",
            "\u092e\u093e\u092e\u0915\u093e\u0903",
        //"\u092a\u093e\u0923\u094d\u0921\u0935\u093e\u0936\u094d\u091a\u0948\u0935",
            "\u0915\u093f\u092e\u0915\u0941\u0930\u094d\u0935\u0924",
            "\u0938\u0902\u091c\u0935",
        };

        Transliterator latinToDev=Transliterator.getInstance("Latin-Devanagari", Transliterator.FORWARD);
        Transliterator devToLatin=Transliterator.getInstance("Devanagari-Latin", Transliterator.FORWARD);

        String gotResult;
        for(int i= 0; i<MAX_LEN; i++){
            gotResult = source[i];
            expect(latinToDev,(source[i]),(expected[i]));
            expect(devToLatin,(expected[i]),(source[i]));
        }
    }


    public void  TestCompoundLatinRT(){
        int MAX_LEN =15;
        String[]  source = {
            "rmk\u1E63\u0113t",
            "\u015Br\u012Bmad",
            "bhagavadg\u012Bt\u0101",
            "adhy\u0101ya",
            "arjuna",
            "vi\u1E63\u0101da",
            "y\u014Dga",
            "dhr\u0325tar\u0101\u1E63\u1E6Dra",
            "uv\u0101cr\u0325a",
            "dharmak\u1E63\u0113tr\u0113",
            "kuruk\u1E63\u0113tr\u0113",
            "samav\u0113t\u0101",
            "yuyutsava-\u1E25",
            "m\u0101mak\u0101-\u1E25",
        // "p\u0101\u1E47\u1E0Dav\u0101\u015Bcaiva",
            "kimakurvata",
            "san\u0304java"
        };
        String[]  expected = {
            "\u0930\u094D\u092E\u094D\u0915\u094D\u0937\u0947\u0924\u094D",
            "\u0936\u094d\u0930\u0940\u092e\u0926\u094d",
            "\u092d\u0917\u0935\u0926\u094d\u0917\u0940\u0924\u093e",
            "\u0905\u0927\u094d\u092f\u093e\u092f",
            "\u0905\u0930\u094d\u091c\u0941\u0928",
            "\u0935\u093f\u0937\u093e\u0926",
            "\u092f\u094b\u0917",
            "\u0927\u0943\u0924\u0930\u093e\u0937\u094d\u091f\u094d\u0930",
            "\u0909\u0935\u093E\u091A\u0943",
            "\u0927\u0930\u094d\u092e\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947",
            "\u0915\u0941\u0930\u0941\u0915\u094d\u0937\u0947\u0924\u094d\u0930\u0947",
            "\u0938\u092e\u0935\u0947\u0924\u093e",
            "\u092f\u0941\u092f\u0941\u0924\u094d\u0938\u0935\u0903",
            "\u092e\u093e\u092e\u0915\u093e\u0903",
        //  "\u092a\u093e\u0923\u094d\u0921\u0935\u093e\u0936\u094d\u091a\u0948\u0935",
            "\u0915\u093f\u092e\u0915\u0941\u0930\u094d\u0935\u0924",
            "\u0938\u0902\u091c\u0935"
        };

        Transliterator latinToDevToLatin=Transliterator.getInstance("Latin-Devanagari;Devanagari-Latin", Transliterator.FORWARD);
        Transliterator devToLatinToDev=Transliterator.getInstance("Devanagari-Latin;Latin-Devanagari", Transliterator.FORWARD);

        String gotResult;
        for(int i= 0; i<MAX_LEN; i++){
            gotResult = source[i];
            expect(latinToDevToLatin,(source[i]),(source[i]));
            expect(devToLatinToDev,(expected[i]),(expected[i]));
        }
    }

    /**
     * Test instantiation from a locale.
     */
    public void TestLocaleInstantiation() {
        Transliterator t = Transliterator.getInstance("ru_RU-Latin");
        expect(t, "\u0430", "a");

        t = Transliterator.getInstance("en-el");
        expect(t, "a", "\u03B1");
    }

    /**
     * Test title case handling of accent (should ignore accents)
     */
    public void TestTitleAccents() {
        Transliterator t = Transliterator.getInstance("Title");
        expect(t, "a\u0300b can't abe", "A\u0300b Can't Abe");
    }

    /**
     * Basic test of a locale resource based rule.
     */
    public void TestLocaleResource() {
        String DATA[] = {
            // id                    from             to
            "Latin-Greek/UNGEGN",    "b",             "\u03bc\u03c0",
            "Latin-el",              "b",             "\u03bc\u03c0",
            "Latin-Greek",           "b",             "\u03B2",
            "Greek-Latin/UNGEGN",    "\u03bc\u03c0",  "b",
            "el-Latin",              "\u03bc\u03c0",  "b",
            "Greek-Latin",           "\u03B2",        "b",
        };
        for (int i=0; i<DATA.length; i+=3) {
            Transliterator t = Transliterator.getInstance(DATA[i]);
            expect(t, DATA[i+1], DATA[i+2]);
        }
    }

    /**
     * Make sure parse errors reference the right line.
     */
    public void TestParseError() {
        String rule =
            "a > b;\n" +
            "# more stuff\n" +
            "d << b;";
        try {
            Transliterator t = Transliterator.createFromRules("ID", rule, Transliterator.FORWARD);
        } catch (IllegalArgumentException e) {
            String err = e.getMessage();
            if (err.indexOf("d << b") >= 0) {
                logln("Ok: " + err);
            } else {
                errln("FAIL: " + err);
            }
            return;
        }
        errln("FAIL: no syntax error");
    }

    /**
     * Make sure sets on output are disallowed.
     */
    public void TestOutputSet() {
        String rule = "$set = [a-cm-n]; b > $set;";
        Transliterator t = null;
        try {
            t = Transliterator.createFromRules("ID", rule, Transliterator.FORWARD);
        } catch (IllegalArgumentException e) {
            logln("Ok: " + e.getMessage());
            return;
        }
        errln("FAIL: No syntax error");
    }        

    //======================================================================
    // icu4j ONLY
    // These tests are not mirrored (yet) in icu4c at
    // source/test/intltest/transtst.cpp
    //======================================================================

    /**
     * Test anchor masking
     */
    public void TestAnchorMasking() {
        String rule = "^a > Q; a > q;";
        try {
            Transliterator t = Transliterator.createFromRules("ID", rule, Transliterator.FORWARD);
        } catch (IllegalArgumentException e) {
            errln("FAIL: " + rule + " => " + e);
        }
    }

    //======================================================================
    // Support methods
    //======================================================================
    void expect(String rules,
                String source,
                String expectedResult,
                Transliterator.Position pos) {
        Transliterator t = Transliterator.createFromRules("<ID>", rules, Transliterator.FORWARD);
        expect(t, source, expectedResult, pos);
    }

    void expect(String rules, String source, String expectedResult) {
        expect(rules, source, expectedResult, null);
    }

    void expect(Transliterator t, String source, String expectedResult,
                Transliterator reverseTransliterator) {
        expect(t, source, expectedResult);
        if (reverseTransliterator != null) {
            expect(reverseTransliterator, expectedResult, source);
        }
    }

    void expect(Transliterator t, String source, String expectedResult) {
        expect(t, source, expectedResult, (Transliterator.Position) null);
    }

    void expect(Transliterator t, String source, String expectedResult,
                Transliterator.Position pos) {
        if (pos == null) {
            String result = t.transliterate(source);
            expectAux(t.getID() + ":String", source, result, expectedResult);
        }

        Transliterator.Position index = null;
        if (pos == null) {
            index = new Transliterator.Position();
        } else {
            index = new Transliterator.Position(pos.contextStart, pos.contextLimit,
                                                pos.start, pos.limit);
        }

        ReplaceableString rsource = new ReplaceableString(source);
        if (pos == null) {
            t.transliterate(rsource);
        } else {
            // Do it all at once -- below we do it incrementally
            t.finishTransliteration(rsource, pos);
        }
        String result = rsource.toString();
        expectAux(t.getID() + ":Replaceable", source, result, expectedResult);

        // Test keyboard (incremental) transliteration -- this result
        // must be the same after we finalize (see below).
        StringBuffer log = new StringBuffer();
        rsource.replace(0, rsource.length(), "");
        if (pos != null) {
            rsource.replace(0, 0, source);
            formatInput(log, rsource, index);
            log.append(" -> ");
            t.transliterate(rsource, index);
            formatInput(log, rsource, index);
        } else {
            for (int i=0; i<source.length(); ++i) {
                if (i != 0) {
                    log.append(" + ");
                }
                log.append(source.charAt(i)).append(" -> ");
                t.transliterate(rsource, index, source.charAt(i));
                formatInput(log, rsource, index);
            }
        }

        // As a final step in keyboard transliteration, we must call
        // transliterate to finish off any pending partial matches that
        // were waiting for more input.
        t.finishTransliteration(rsource, index);
        result = rsource.toString();
        log.append(" => ").append(rsource.toString());

        expectAux(t.getID() + ":Keyboard", log.toString(),
                  result.equals(expectedResult),
                  expectedResult);
    }

    /**
     * @param appendTo result is appended to this param.
     * @param input the string being transliterated
     * @param pos the index struct
     */
    StringBuffer formatInput(StringBuffer appendTo,
                             final ReplaceableString input,
                             final Transliterator.Position pos) {
        // Output a string of the form aaa{bbb|ccc|ddd}eee, where
        // the {} indicate the context start and limit, and the ||
        // indicate the start and limit.
        if (0 <= pos.contextStart &&
            pos.contextStart <= pos.start &&
            pos.start <= pos.limit &&
            pos.limit <= pos.contextLimit &&
            pos.contextLimit <= input.length()) {

            String a, b, c, d, e;
            a = input.substring(0, pos.contextStart);
            b = input.substring(pos.contextStart, pos.start);
            c = input.substring(pos.start, pos.limit);
            d = input.substring(pos.limit, pos.contextLimit);
            e = input.substring(pos.contextLimit, input.length());
            appendTo.append(a).append('{').append(b).
                append('|').append(c).append('|').append(d).
                append('}').append(e);
        } else {
            appendTo.append("INVALID Transliterator.Position {cs=" +
                            pos.contextStart + ", s=" + pos.start + ", l=" +
                            pos.limit + ", cl=" + pos.contextLimit + "} on " +
                            input);
        }
        return appendTo;
    }

    void expectAux(String tag, String source,
                   String result, String expectedResult) {
        expectAux(tag, source + " -> " + result,
                  result.equals(expectedResult),
                  expectedResult);
    }

    void expectAux(String tag, String summary, boolean pass,
                   String expectedResult) {
        if (pass) {
            logln("("+tag+") " + Utility.escape(summary));
        } else {
            errln("FAIL: ("+tag+") "
                  + Utility.escape(summary)
                  + ", expected " + Utility.escape(expectedResult));
        }
    }
}
