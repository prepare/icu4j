/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/format/RbnfTest.java,v $ 
 * $Date: 2001/11/12 20:02:46 $ 
 * $Revision: 1.7 $
 *
 *****************************************************************************************
 */
package com.ibm.test.rbnf;

import com.ibm.text.RuleBasedNumberFormat;
import com.ibm.test.TestFmwk;

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
            com.ibm.text.NumberFormat formatter = formatters[j];
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
            { "3,600", "1:00:00" },		//move me and I fail
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
            { "80", "octante" },
            { "88", "octante-huit" },
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
     * Perform a simple spot check on the German spellout rules
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

    void doTest(RuleBasedNumberFormat formatter, String[][] testData,
                boolean testParsing) {
        NumberFormat decFmt = NumberFormat.getInstance(Locale.US);

        try {
            for (int i = 0; i < testData.length; i++) {
                String number = testData[i][0];
                String expectedWords = testData[i][1];
                String actualWords = formatter.format(decFmt.parse(number));

                if (!actualWords.equals(expectedWords)) {
                    errln("Spot check failed: for " + number + ", expected "
                                + expectedWords + ", but got " +
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
            errln("Test failed with exception: " + e.toString());
            e.printStackTrace();
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

