/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/format/RbnfTest.java,v $ 
 * $Date: 2003/05/23 17:04:47 $ 
 * $Revision: 1.17 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.dev.test.TestFmwk;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Locale;
import java.text.NumberFormat;

public class RbnfTest extends TestFmwk {
    /**
     * Puts a copyright in the .class file
     */
    private static final String copyrightNotice
        = "Copyright \u00a91997-1999 IBM Corp.  All rights reserved.";

    public static void main(String[] args) {
        RbnfTest test = new RbnfTest();

        try {
            test.run(args);
        }
        catch (Throwable e) {
            System.out.println("Entire test failed because of exception: "
                               + e.toString());
            e.printStackTrace();
        }
    }

    static String fracRules = 
        "%main:\n" +
        // this rule formats the number if it's 1 or more.  It formats
        // the integral part using a DecimalFormat ("#,##0" puts
        // thousands separators in the right places) and the fractional
        // part using %%frac.  If there is no fractional part, it
        // just shows the integral part.
        "    x.0: <#,##0<[ >%%frac>];\n" +
        // this rule formats the number if it's between 0 and 1.  It
        // shows only the fractional part (0.5 shows up as "1/2," not
        // "0 1/2")
        "    0.x: >%%frac>;\n" +
        // the fraction rule set.  This works the same way as the one in the
        // preceding example: We multiply the fractional part of the number
        // being formatted by each rule's base value and use the rule that
        // produces the result closest to 0 (or the first rule that produces 0).
        // Since we only provide rules for the numbers from 2 to 10, we know
        // we'll get a fraction with a denominator between 2 and 10.
        // "<0<" causes the numerator of the fraction to be formatted
        // using numerals
        "%%frac:\n" +
        "    2: 1/2;\n" +
        "    3: <0</3;\n" +
        "    4: <0</4;\n" +
        "    5: <0</5;\n" +
        "    6: <0</6;\n" +
        "    7: <0</7;\n" +
        "    8: <0</8;\n" +
        "    9: <0</9;\n" +
        "   10: <0</10;\n";

    static {
        // mondo hack
	char[] fracRulesArr = fracRules.toCharArray();
        int len = fracRulesArr.length;
        int change = 2;
        for (int i = 0; i < len; ++i) {
            char ch = fracRulesArr[i];
            if (ch == '\n') {
                change = 2; // change ok
            } else if (ch == ':') {
                change = 1; // change, but once we hit a non-space char, don't change
            } else if (ch == ' ') {
                if (change != 0) {
                    fracRulesArr[i] = (char)0x200e;
                }
            } else {
                if (change == 1) {
                    change = 0;
                }
            }
        }
	fracRules = new String(fracRulesArr);
    }

    static final String durationInSecondsRules =
        // main rule set for formatting with words
        "%with-words:\n"
        // take care of singular and plural forms of "second"
        + "    0 seconds; 1 second; =0= seconds;\n"
        // use %%min to format values greater than 60 seconds
        + "    60/60: <%%min<[, >>];\n"
        // use %%hr to format values greater than 3,600 seconds
        // (the ">>>" below causes us to see the number of minutes
        // when when there are zero minutes)
        + "    3600/60: <%%hr<[, >>>];\n"
        // this rule set takes care of the singular and plural forms
        // of "minute"
        + "%%min:\n"
        + "    0 minutes; 1 minute; =0= minutes;\n"
        // this rule set takes care of the singular and plural forms
        // of "hour"
        + "%%hr:\n"
        + "    0 hours; 1 hour; =0= hours;\n"

        // main rule set for formatting in numerals
        + "%in-numerals:\n"
        // values below 60 seconds are shown with "sec."
        + "    =0= sec.;\n"
        // higher values are shown with colons: %%min-sec is used for
        // values below 3,600 seconds...
        + "    60: =%%min-sec=;\n"
        // ...and %%hr-min-sec is used for values of 3,600 seconds
        // and above
        + "    3600: =%%hr-min-sec=;\n"
        // this rule causes values of less than 10 minutes to show without
        // a leading zero
        + "%%min-sec:\n"
        + "    0: :=00=;\n"
        + "    60/60: <0<>>;\n"
        // this rule set is used for values of 3,600 or more.  Minutes are always
        // shown, and always shown with two digits
        + "%%hr-min-sec:\n"
        + "    0: :=00=;\n"
        + "    60/60: <00<>>;\n"
        + "    3600/60: <#,##0<:>>>;\n"
        // the lenient-parse rules allow several different characters to be used
        // as delimiters between hours, minutes, and seconds
        + "%%lenient-parse:\n"
        + "    & : = . = ' ' = -;\n";

    public void TestCoverage() {
        // extra calls to boost coverage numbers
        RuleBasedNumberFormat fmt0 = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        RuleBasedNumberFormat fmt1 = (RuleBasedNumberFormat)fmt0.clone();
        RuleBasedNumberFormat fmt2 = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        if (!fmt0.equals(fmt0)) {
            errln("self equality fails");
        }
        if (!fmt0.equals(fmt1)) {
            errln("clone equality fails");
        }
        if (!fmt0.equals(fmt2)) {
            errln("duplicate equality fails");
        }
        String str = fmt0.toString();
        logln(str);

        RuleBasedNumberFormat fmt3 =  new RuleBasedNumberFormat(durationInSecondsRules);

        if (fmt0.equals(fmt3)) {
            errln("nonequal fails");
        }
        if (!fmt3.equals(fmt3)) {
            errln("self equal 2 fails");
        }
        str = fmt3.toString();
        logln(str);

        String[] names = fmt3.getRuleSetNames();

        try {
            fmt3.setDefaultRuleSet(null);
            fmt3.setDefaultRuleSet("%%foo");
            errln("sdrf %%foo didn't fail");
        }
        catch (Exception e) {
        }

        try {
            fmt3.setDefaultRuleSet("%bogus");
            errln("sdrf %bogus didn't fail");
        }
        catch (Exception e) {
        }

        try {
            str = fmt3.format(2.3, names[0]);
            logln(str);
            str = fmt3.format(2.3, "%%foo");
            errln("format double %%foo didn't fail");
        }
        catch (Exception e) {
        }

        try {
            str = fmt3.format(123L, names[0]);
            logln(str);
            str = fmt3.format(123L, "%%foo");
            errln("format double %%foo didn't fail");
        }
        catch (Exception e) {
        }

        RuleBasedNumberFormat fmt4 = new RuleBasedNumberFormat(fracRules, Locale.ENGLISH);
        RuleBasedNumberFormat fmt5 = new RuleBasedNumberFormat(fracRules, Locale.ENGLISH);
        str = fmt4.toString();
        logln(str);
        if (!fmt4.equals(fmt5)) {
            errln("duplicate 2 equality failed");
        }
        str = fmt4.format(123L);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }

        str = fmt4.format(.000123);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }

        str = fmt4.format(456.000123);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }
    }

    public void TestUndefinedSpellout() {
        Locale greek = new Locale("el", "", "");
        RuleBasedNumberFormat[] formatters = {
            new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.SPELLOUT),
            new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.ORDINAL),
            new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.DURATION),
        };

        String[] data = {
            "0",
            "1",
            "15",
            "20",
            "23",
            "73",
            "88",
            "100",
            "106",
            "127",
            "200",
            "579",
            "1,000",
            "2,000",
            "3,004",
            "4,567",
            "15,943",
            "105,000",
            "2,345,678",
            "-36",
            "-36.91215",
            "234.56789"
        };

        NumberFormat decFormat = NumberFormat.getInstance(Locale.US);
        for (int j = 0; j < formatters.length; ++j) {
            com.ibm.icu.text.NumberFormat formatter = formatters[j];
            logln("formatter[" + j + "]");
            for (int i = 0; i < data.length; ++i) {
                try {
                    String result = formatter.format(decFormat.parse(data[i]));
                    logln("[" + i + "] " + data[i] + " ==> " + result);
                }
                catch (Exception e) {
                    errln("formatter[" + j + "], data[" + i + "] " + data[i] + " threw exception " + e.getMessage());
                }
            }
        }
    }

    /**
     * Perform a simple spot check on the English spellout rules
     */
    public void TestEnglishSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "one" },
            { "15", "fifteen" },
            { "20", "twenty" },
            { "23", "twenty-three" },
            { "73", "seventy-three" },
            { "88", "eighty-eight" },
            { "100", "one hundred" },
            { "106", "one hundred and six" },
            { "127", "one hundred and twenty-seven" },
            { "200", "two hundred" },
            { "579", "five hundred and seventy-nine" },
            { "1,000", "one thousand" },
            { "2,000", "two thousand" },
            { "3,004", "three thousand and four" },
            { "4,567", "four thousand five hundred and sixty-seven" },
            { "15,943", "fifteen thousand nine hundred and forty-three" },
            { "2,345,678", "two million, three hundred and forty-five "
              + "thousand, six hundred and seventy-eight" },
            { "-36", "minus thirty-six" },
            { "234.567", "two hundred and thirty-four point five six seven" }
        };

        doTest(formatter, testData, true);

        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "FOurhundred     thiRTY six", "436" },
            // test spaces before fifty-7 causing lenient parse match of "fifty-" to " fifty"
            // leaving "-7" for remaining parse, resulting in 2643 as the parse result.
            { "fifty-7", "57" },
            { " fifty-7", "57" },
            { "  fifty-7", "57" },
            { "2 thousand six HUNDRED   fifty-7", "2,657" },
            { "fifteen hundred and zero", "1,500" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the English ordinal-abbreviation rules
     */
    public void TestOrdinalAbbreviations() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.ORDINAL);
        String[][] testData = {
            { "1", "1st" },
            { "2", "2nd" },
            { "3", "3rd" },
            { "4", "4th" },
            { "7", "7th" },
            { "10", "10th" },
            { "11", "11th" },
            { "13", "13th" },
            { "20", "20th" },
            { "21", "21st" },
            { "22", "22nd" },
            { "23", "23rd" },
            { "24", "24th" },
            { "33", "33rd" },
            { "102", "102nd" },
            { "312", "312th" },
            { "12,345", "12,345th" }
        };

        doTest(formatter, testData, false);
    }

    /**
     * Perform a simple spot check on the duration-formatting rules
     */
    public void TestDurations() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.DURATION);
        String[][] testData = {
            { "3,600", "1:00:00" },             //move me and I fail
            { "0", "0 sec." },
            { "1", "1 sec." },
            { "24", "24 sec." },
            { "60", "1:00" },
            { "73", "1:13" },
            { "145", "2:25" },
            { "666", "11:06" },
            //            { "3,600", "1:00:00" },
            { "3,740", "1:02:20" },
            { "10,293", "2:51:33" }
        };

        doTest(formatter, testData, true);

        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "2-51-33", "10,293" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the Spanish spellout rules
     */
    public void TestSpanishSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(new Locale("es", "es",
                                                   ""), RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "uno" },
            { "6", "seis" },
            { "16", "diecis\u00e9is" },
            { "20", "veinte" },
            { "24", "veinticuatro" },
            { "26", "veintis\u00e9is" },
            { "73", "setenta y tres" },
            { "88", "ochenta y ocho" },
            { "100", "cien" },
            { "106", "ciento seis" },
            { "127", "ciento veintisiete" },
            { "200", "doscientos" },
            { "579", "quinientos setenta y nueve" },
            { "1,000", "mil" },
            { "2,000", "dos mil" },
            { "3,004", "tres mil cuatro" },
            { "4,567", "cuatro mil quinientos sesenta y siete" },
            { "15,943", "quince mil novecientos cuarenta y tres" },
            { "2,345,678", "dos mill\u00f3n trescientos cuarenta y cinco mil "
              + "seiscientos setenta y ocho"},
            { "-36", "menos treinta y seis" },
            { "234.567", "doscientos treinta y cuatro punto cinco seis siete" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the French spellout rules
     */
    public void TestFrenchSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.FRANCE,
                                        RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "un" },
            { "15", "quinze" },
            { "20", "vingt" },
            { "21", "vingt-et-un" },
            { "23", "vingt-trois" },
            { "62", "soixante-deux" },
            { "70", "soixante-dix" },
            { "71", "soixante et onze" },
            { "73", "soixante-treize" },
            { "80", "quatre-vingts" },
            { "88", "quatre-vingt-huit" },
            { "100", "cent" },
            { "106", "cent six" },
            { "127", "cent vingt-sept" },
            { "200", "deux cents" },
            { "579", "cinq cents soixante-dix-neuf" },
            { "1,000", "mille" },
            { "1,123", "onze cents vingt-trois" },
            { "1,594", "mille cinq cents quatre-vingt-quatorze" },
            { "2,000", "deux mille" },
            { "3,004", "trois mille quatre" },
            { "4,567", "quatre mille cinq cents soixante-sept" },
            { "15,943", "quinze mille neuf cents quarante-trois" },
            { "2,345,678", "deux million trois cents quarante-cinq mille "
              + "six cents soixante-dix-huit" },
            { "-36", "moins trente-six" },
            { "234.567", "deux cents trente-quatre virgule cinq six sept" }
        };

        doTest(formatter, testData, true);

        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "trente-un", "31" },
            { "un cents quatre vingt dix huit", "198" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the Swiss French spellout rules
     */
    public void TestSwissFrenchSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(new Locale("fr", "CH",
                                                   ""), RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "un" },
            { "15", "quinze" },
            { "20", "vingt" },
            { "21", "vingt-et-un" },
            { "23", "vingt-trois" },
            { "62", "soixante-deux" },
            { "70", "septante" },
            { "71", "septante-et-un" },
            { "73", "septante-trois" },
            { "80", "huitante" },
            { "88", "huitante-huit" },
            { "100", "cent" },
            { "106", "cent six" },
            { "127", "cent vingt-sept" },
            { "200", "deux cents" },
            { "579", "cinq cents septante-neuf" },
            { "1,000", "mille" },
            { "1,123", "onze cents vingt-trois" },
            { "1,594", "mille cinq cents nonante-quatre" },
            { "2,000", "deux mille" },
            { "3,004", "trois mille quatre" },
            { "4,567", "quatre mille cinq cents soixante-sept" },
            { "15,943", "quinze mille neuf cents quarante-trois" },
            { "2,345,678", "deux million trois cents quarante-cinq mille "
              + "six cents septante-huit" },
            { "-36", "moins trente-six" },
            { "234.567", "deux cents trente-quatre virgule cinq six sept" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the Italian spellout rules
     */
    public void TestItalianSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.ITALIAN,
                                        RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "uno" },
            { "15", "quindici" },
            { "20", "venti" },
            { "23", "ventitre" },
            { "73", "settantatre" },
            { "88", "ottantotto" },
            { "100", "cento" },
            { "106", "centosei" },
            { "108", "centotto" },
            { "127", "centoventisette" },
            { "181", "centottantuno" },
            { "200", "duecento" },
            { "579", "cinquecentosettantanove" },
            { "1,000", "mille" },
            { "2,000", "duemila" },
            { "3,004", "tremilaquattro" },
            { "4,567", "quattromilacinquecentosessantasette" },
            { "15,943", "quindicimilanovecentoquarantatre" },
            { "-36", "meno trentisei" },
            { "234.567", "duecentotrentiquattro virgola cinque sei sette" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the German spellout rules
     */
    public void TestGermanSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.GERMANY,
                                        RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "1", "eins" },
            { "15", "f\u00fcnfzehn" },
            { "20", "zwanzig" },
            { "23", "dreiundzwanzig" },
            { "73", "dreiundsiebzig" },
            { "88", "achtundachtzig" },
            { "100", "hundert" },
            { "106", "hundertsechs" },
            { "127", "hundertsiebenundzwanzig" },
            { "200", "zweihundert" },
            { "579", "f\u00fcnfhundertneunundsiebzig" },
            { "1,000", "tausend" },
            { "2,000", "zweitausend" },
            { "3,004", "dreitausendvier" },
            { "4,567", "viertausendf\u00fcnfhundertsiebenundsechzig" },
            { "15,943", "f\u00fcnfzehntausendneunhundertdreiundvierzig" },
            { "2,345,678", "zwei Millionen dreihundertf\u00fcnfundvierzigtausend"
              + "sechshundertachtundsiebzig" }
        };

        doTest(formatter, testData, true);

        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "ein Tausend sechs Hundert fuenfunddreissig", "1,635" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the Thai spellout rules
     */
    public void TestThaiSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(new Locale("th", "TH", ""),
                                        RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
            { "0", "\u0e28\u0e39\u0e19\u0e22\u0e4c" },
            { "1", "\u0e2b\u0e19\u0e36\u0e48\u0e07" },
            { "10", "\u0e2a\u0e34\u0e1a" },
            { "11", "\u0e2a\u0e34\u0e1a\u0e40\u0e2d\u0e47\u0e14" },
            { "21", "\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e40\u0e2d\u0e47\u0e14" },
            { "101", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u0e23\u0e49\u0e2d\u0e22\u0e2b\u0e19\u0e36\u0e48\u0e07" },
            { "1.234", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u0e08\u0e38\u0e14\u0e2a\u0e2d\u0e07\u0e2a\u0e32\u0e21\u0e2a\u0e35\u0e48" },
            { "21.45", "\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e40\u0e2d\u0e47\u0e14\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
            { "22.45", "\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e2a\u0e2d\u0e07\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
            { "23.45", "\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
            { "123.45", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u0e23\u0e49\u0e2d\u0e22\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
            { "12,345.678", "\u0E2B\u0E19\u0E36\u0E48\u0E07\u0E2B\u0E21\u0E37\u0E48\u0E19\u0E2A\u0E2D\u0E07\u0E1E\u0E31\u0E19\u0E2A\u0E32\u0E21\u0E23\u0E49\u0E2D\u0E22\u0E2A\u0E35\u0E48\u0E2A\u0E34\u0E1A\u0E2B\u0E49\u0E32\u0E08\u0E38\u0E14\u0E2B\u0E01\u0E40\u0E08\u0E47\u0E14\u0E41\u0E1B\u0E14" },
        };

        doTest(formatter, testData, true);

        /*
          formatter.setLenientParseMode(true);
          String[][] lpTestData = {
          { "ein Tausend sechs Hundert fuenfunddreissig", "1,635" }
          };
          doLenientParseTest(formatter, lpTestData);
        */
    }

    public void TestFractionalRuleSet() {


        RuleBasedNumberFormat formatter =
            new RuleBasedNumberFormat(fracRules, Locale.ENGLISH);

        String[][] testData = {
            { "0", "0" },
            { "1", "1" },
            { "10", "10" },
            { ".1", "1/10" },
            { ".11", "1/9" },
            { ".125", "1/8" },
            { ".1428", "1/7" },
            { ".1667", "1/6" },
            { ".2", "1/5" },
            { ".25", "1/4" },
            { ".333", "1/3" },
            { ".5", "1/2" },
            { "1.1", "1 1/10" },
            { "2.11", "2 1/9" },
            { "3.125", "3 1/8" },
            { "4.1428", "4 1/7" },
            { "5.1667", "5 1/6" },
            { "6.2", "6 1/5" },
            { "7.25", "7 1/4" },
            { "8.333", "8 1/3" },
            { "9.5", "9 1/2" },
            { ".2222", "2/9" },
            { ".4444", "4/9" },
            { ".5555", "5/9" },
            { "1.2856", "1 2/7" }
        };
        doTest(formatter, testData, false); // exact values aren't parsable from fractions
    }

    public void TestSwedishSpellout()
    {
        Locale locale = new Locale("sv", "", "");
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);

        String[][] testDataDefault = {
            { "101", "etthundra\u00aden" },
            { "123", "etthundra\u00adtjugotre" },
            { "1,001", "ettusen en" },
            { "1,100", "ettusen etthundra" },
            { "1,101", "ettusen etthundra\u00aden" },
            { "1,234", "ettusen tv\u00e5hundra\u00adtrettiofyra" },
            { "10,001", "tio\u00adtusen en" },
            { "11,000", "elva\u00adtusen" },
            { "12,000", "tolv\u00adtusen" },
            { "20,000", "tjugo\u00adtusen" },
            { "21,000", "tjugoen\u00adtusen" },
            { "21,001", "tjugoen\u00adtusen en" },
            { "200,000", "tv\u00e5hundra\u00adtusen" },
            { "201,000", "tv\u00e5hundra\u00aden\u00adtusen" },
            { "200,200", "tv\u00e5hundra\u00adtusen tv\u00e5hundra" },
            { "2,002,000", "tv\u00e5 miljoner tv\u00e5\u00adtusen" },
            { "12,345,678", "tolv miljoner trehundra\u00adfyrtiofem\u00adtusen sexhundra\u00adsjuttio\u00e5tta" },
            { "123,456.789", "etthundra\u00adtjugotre\u00adtusen fyrahundra\u00adfemtiosex komma sju \u00e5tta nio" },
            { "-12,345.678", "minus tolv\u00adtusen trehundra\u00adfyrtiofem komma sex sju \u00e5tta" }
        };

        logln("testing default rules");
        doTest(formatter, testDataDefault, true);

        String[][] testDataNeutrum = {
            { "101", "etthundra\u00adett" },
            { "1,001", "ettusen ett" },
            { "1,101", "ettusen etthundra\u00adett" },
            { "10,001", "tio\u00adtusen ett" },
            { "21,001", "tjugoen\u00adtusen ett" }
        };

        formatter.setDefaultRuleSet("%neutrum");
        logln("testing neutrum rules");
        doTest(formatter, testDataNeutrum, true);

        String[][] testDataYear = {
            { "101", "etthundra\u00adett" },
            { "900", "niohundra" },
            { "1,001", "tiohundra\u00adett" },
            { "1,100", "elvahundra" },
            { "1,101", "elvahundra\u00adett" },
            { "1,234", "tolvhundra\u00adtrettiofyra" },
            { "2,001", "tjugohundra\u00adett" },
            { "10,001", "tio\u00adtusen ett" }
        };

        formatter.setDefaultRuleSet("%year");
        logln("testing year rules");
        doTest(formatter, testDataYear, true);
    }

    public void TestBigNumbers() {
        BigInteger bigI = new BigInteger("1234567890", 10);
        StringBuffer buf = new StringBuffer();
        RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        fmt.format(bigI, buf, null);
        logln("big int: " + buf.toString());

        buf.setLength(0);
        BigDecimal bigD = new BigDecimal(bigI);
        fmt.format(bigD, buf, null);
        logln("big dec: " + buf.toString());
    }

  public void TestTrailingSemicolon() {
    String thaiRules = 
        "%default:\n" +
        "  -x: \u0e25\u0e1a>>;\n" +
        "  x.x: <<\u0e08\u0e38\u0e14>>>;\n" +
        "  \u0e28\u0e39\u0e19\u0e22\u0e4c; \u0e2b\u0e19\u0e36\u0e48\u0e07; \u0e2a\u0e2d\u0e07; \u0e2a\u0e32\u0e21;\n" +
        "  \u0e2a\u0e35\u0e48; \u0e2b\u0e49\u0e32; \u0e2b\u0e01; \u0e40\u0e08\u0e47\u0e14; \u0e41\u0e1b\u0e14;\n" +
        "  \u0e40\u0e01\u0e49\u0e32; \u0e2a\u0e34\u0e1a; \u0e2a\u0e34\u0e1a\u0e40\u0e2d\u0e47\u0e14;\n" +
        "  \u0e2a\u0e34\u0e1a\u0e2a\u0e2d\u0e07; \u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21;\n" +
        "  \u0e2a\u0e34\u0e1a\u0e2a\u0e35\u0e48; \u0e2a\u0e34\u0e1a\u0e2b\u0e49\u0e32;\n" +
        "  \u0e2a\u0e34\u0e1a\u0e2b\u0e01; \u0e2a\u0e34\u0e1a\u0e40\u0e08\u0e47\u0e14;\n" +
        "  \u0e2a\u0e34\u0e1a\u0e41\u0e1b\u0e14; \u0e2a\u0e34\u0e1a\u0e40\u0e01\u0e49\u0e32;\n" +
        "  20: \u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  30: \u0e2a\u0e32\u0e21\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  40: \u0e2a\u0e35\u0e48\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  50: \u0e2b\u0e49\u0e32\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  60: \u0e2b\u0e01\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  70: \u0e40\u0e08\u0e47\u0e14\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  80: \u0e41\u0e1b\u0e14\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  90: \u0e40\u0e01\u0e49\u0e32\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
        "  100: <<\u0e23\u0e49\u0e2d\u0e22[>>];\n" +
        "  1000: <<\u0e1e\u0e31\u0e19[>>];\n" +
        "  10000: <<\u0e2b\u0e21\u0e37\u0e48\u0e19[>>];\n" +
        "  100000: <<\u0e41\u0e2a\u0e19[>>];\n" +
        "  1,000,000: <<\u0e25\u0e49\u0e32\u0e19[>>];\n" +
        "  1,000,000,000: <<\u0e1e\u0e31\u0e19\u0e25\u0e49\u0e32\u0e19[>>];\n" +
        "  1,000,000,000,000: <<\u0e25\u0e49\u0e32\u0e19\u0e25\u0e49\u0e32\u0e19[>>];\n" +
        "  1,000,000,000,000,000: =#,##0=;\n" +
        "%%alt-ones:\n" +
        "  \u0e28\u0e39\u0e19\u0e22\u0e4c;\n" +
        "  \u0e40\u0e2d\u0e47\u0e14;\n" +
        "  =%default=;\n ; ;; ";

        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(thaiRules, new Locale("th", "TH", ""));

        String[][] testData = {
            { "0", "\u0e28\u0e39\u0e19\u0e22\u0e4c" },
            { "1", "\u0e2b\u0e19\u0e36\u0e48\u0e07" },
            { "123.45", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u0e23\u0e49\u0e2d\u0e22\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" }
        };
        
        doTest(formatter, testData, true);
    }

    void doTest(RuleBasedNumberFormat formatter, String[][] testData,
                boolean testParsing) {
        NumberFormat decFmt = NumberFormat.getInstance(Locale.US);

        try {
            for (int i = 0; i < testData.length; i++) {
                String number = testData[i][0];
                String expectedWords = testData[i][1];
                logln("test[" + i + "] number: " + number + " target: " + expectedWords);
                Number num = decFmt.parse(number);
                String actualWords = formatter.format(num);

                if (!actualWords.equals(expectedWords)) {
                    errln("Spot check failed: for " + number + ", expected\n    "
                          + expectedWords + ", but got\n    " +
                          actualWords);
                }
                else if (testParsing) {
                    String actualNumber = decFmt.format(formatter
                                                        .parse(actualWords));

                    if (!actualNumber.equals(number)) {
                        errln("Spot check failed: for " + actualWords +
                              ", expected " + number + ", but got " +
                              actualNumber);
                    }
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            errln("Test failed with exception: " + e.toString());
        }
    }

    void doLenientParseTest(RuleBasedNumberFormat formatter,
                            String[][] testData) {
        NumberFormat decFmt = NumberFormat.getInstance(Locale.US);

        try {
            for (int i = 0; i < testData.length; i++) {
                String words = testData[i][0];
                String expectedNumber = testData[i][1];
                String actualNumber = decFmt.format(formatter.parse(words));

                if (!actualNumber.equals(expectedNumber)) {
                    errln("Lenient-parse spot check failed: for "
                          + words + ", expected " + expectedNumber
                          + ", but got " + actualNumber);
                }
            }
        }
        catch (Throwable e) {
            errln("Test failed with exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

