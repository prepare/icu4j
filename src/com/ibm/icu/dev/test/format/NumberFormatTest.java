/*
 *******************************************************************************
 * Copyright (C) 2001-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v1.8.1 : format : NumberFormatTest
 * Source File: $ICU4CRoot/source/test/intltest/numfmtst.cpp
 **/

package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestUtil;
import com.ibm.icu.text.*;
import com.ibm.icu.text.NumberFormat.*;
import com.ibm.icu.util.*;
import com.ibm.icu.impl.LocaleUtility;
import com.ibm.icu.impl.data.ResourceReader;
import com.ibm.icu.impl.data.TokenIterator;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;
import java.util.Locale;

public class NumberFormatTest extends com.ibm.icu.dev.test.TestFmwk {
    private static final char EURO = '\u20ac';

    public static void main(String[] args) throws Exception {
        new NumberFormatTest().run(args);
    }
    
    // Test various patterns
    public void TestPatterns() {
    
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        final String pat[]    = { "#.#", "#.", ".#", "#" };
        int pat_length = pat.length;
        final String newpat[] = { "#0.#", "#0.", "#.0", "#" };
        final String num[]    = { "0",   "0.", ".0", "0" };
        for (int i=0; i<pat_length; ++i)
        {
            DecimalFormat fmt = new DecimalFormat(pat[i], sym);
            String newp = fmt.toPattern();
            if (!newp.equals(newpat[i]))
                errln("FAIL: Pattern " + pat[i] + " should transmute to " + newpat[i] +
                      "; " + newp + " seen instead");
    
            String s = ((NumberFormat)fmt).format(0);
            if (!s.equals(num[i]))
            {
                errln("FAIL: Pattern " + pat[i] + " should format zero as " + num[i] +
                      "; " + s + " seen instead");
                logln("Min integer digits = " + fmt.getMinimumIntegerDigits());
            }
        }
    }
    
    // Test exponential pattern
    public void TestExponential() {
    
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        final String pat[] = { "0.####E0", "00.000E00", "##0.######E000", "0.###E0;[0.###E0]" };
        int pat_length = pat.length;
    
        double val[] = { 0.01234, 123456789, 1.23e300, -3.141592653e-271 };
        int val_length = val.length;
        final String valFormat[] = {
            // 0.####E0
            "1.234E-2", "1.2346E8", "1.23E300", "-3.1416E-271",
            // 00.000E00
            "12.340E-03", "12.346E07", "12.300E299", "-31.416E-272",
            // ##0.######E000
            "12.34E-003", "123.4568E006", "1.23E300", "-314.1593E-273",
            // 0.###E0;[0.###E0]
            "1.234E-2", "1.235E8", "1.23E300", "[3.142E-271]" };
        /*double valParse[] =
            {
                0.01234, 123460000, 1.23E300, -3.1416E-271,
                0.01234, 123460000, 1.23E300, -3.1416E-271,
                0.01234, 123456800, 1.23E300, -3.141593E-271,
                0.01234, 123500000, 1.23E300, -3.142E-271,
            };*/ //The variable is never used
    
        int lval[] = { 0, -1, 1, 123456789 };
        int lval_length = lval.length;
        final String lvalFormat[] = {
            // 0.####E0
            "0E0", "-1E0", "1E0", "1.2346E8",
            // 00.000E00
            "00.000E00", "-10.000E-01", "10.000E-01", "12.346E07",
            // ##0.######E000
            "0E000", "-1E000", "1E000", "123.4568E006",
            // 0.###E0;[0.###E0]
            "0E0", "[1E0]", "1E0", "1.235E8" };
        int lvalParse[] = 
            {
                0, -1, 1, 123460000, 
                0, -1, 1, 123460000, 
                0, -1, 1, 123456800, 
                0, -1, 1, 123500000, 
            };
        int ival = 0, ilval = 0;
        for (int p = 0; p < pat_length; ++p) {
            DecimalFormat fmt = new DecimalFormat(pat[p], sym);
            logln("Pattern \"" + pat[p] + "\" -toPattern-> \"" + fmt.toPattern() + "\"");
            int v;
            for (v = 0; v < val_length; ++v) {
                String s;
                s = ((NumberFormat) fmt).format(val[v]);
                logln(" " + val[v] + " -format-> " + s);
                if (!s.equals(valFormat[v + ival]))
                    errln("FAIL: Expected " + valFormat[v + ival]);
    
                ParsePosition pos = new ParsePosition(0);
                double a = fmt.parse(s, pos).doubleValue();
                if (pos.getIndex() == s.length()) {
                    logln("  -parse-> " + Double.toString(a));
                    // Use epsilon comparison as necessary
                } else
                    errln("FAIL: Partial parse (" + pos.getIndex() + " chars) -> " + a);
            }
            for (v = 0; v < lval_length; ++v) {
                String s;
                s = ((NumberFormat) fmt).format(lval[v]);
                logln(" " + lval[v] + "L -format-> " + s);
                if (!s.equals(lvalFormat[v + ilval]))
                    errln("ERROR: Expected " + lvalFormat[v + ilval] + " Got: " + s);
    
                ParsePosition pos = new ParsePosition(0);
                long a = 0;
                Number A = fmt.parse(s, pos);
                if (A != null) {
                    a = A.longValue();
                    if (pos.getIndex() == s.length()) {
                        logln("  -parse-> " + a);
                        if (a != lvalParse[v + ilval])
                            errln("FAIL: Expected " + lvalParse[v + ilval]);
                    } else
                        errln("FAIL: Partial parse (" + pos.getIndex() + " chars) -> " + Long.toString(a));
                } else {
                    errln("Fail to parse the string: " + s);
                }
            }
            ival += val_length;
            ilval += lval_length;
        }
    }

    // Test the handling of quotes
    public void TestQuotes() {
    
        StringBuffer pat;
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        pat = new StringBuffer("a'fo''o'b#");
        DecimalFormat fmt = new DecimalFormat(pat.toString(), sym);
        String s = ((NumberFormat)fmt).format(123);
        logln("Pattern \"" + pat + "\"");
        logln(" Format 123 . " + s);
        if (!s.equals("afo'ob123")) 
            errln("FAIL: Expected afo'ob123");
        
        s ="";
        pat = new StringBuffer("a''b#");
        fmt = new DecimalFormat(pat.toString(), sym);
        s = ((NumberFormat)fmt).format(123);
        logln("Pattern \"" + pat + "\"");
        logln(" Format 123 . " + s);
        if (!s.equals("a'b123")) 
            errln("FAIL: Expected a'b123");
    }
    
    public void TestParseCurrencyTrailingSymbol() {
        // see sun bug 4709840
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        float val = 12345.67f;
        String str = fmt.format(val);
        logln("val: " + val + " str: " + str);
        try {
            Number num = fmt.parse(str);
            logln("num: " + num);
        } catch (ParseException e) {
            errln("parse of '" + str + "' threw exception: " + e);
        }
    }

    /**
     * Test the handling of the currency symbol in patterns.
     **/
    public void TestCurrencySign() {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        StringBuffer pat = new StringBuffer("");
        char currency = 0x00A4;
        // "\xA4#,##0.00;-\xA4#,##0.00"
        pat.append(currency).append("#,##0.00;-").append(currency).append("#,##0.00");
        DecimalFormat fmt = new DecimalFormat(pat.toString(), sym);
        String s = ((NumberFormat) fmt).format(1234.56);
        pat = new StringBuffer("");
        logln("Pattern \"" + fmt.toPattern() + "\"");
        logln(" Format " + 1234.56 + " . " + s);
        if (!s.equals("$1,234.56"))
            errln("FAIL: Expected $1,234.56");
        s = "";
        s = ((NumberFormat) fmt).format(-1234.56);
        logln(" Format " + Double.toString(-1234.56) + " . " + s);
        if (!s.equals("-$1,234.56"))
            errln("FAIL: Expected -$1,234.56");
    
        pat = new StringBuffer("");
        // "\xA4\xA4 #,##0.00;\xA4\xA4 -#,##0.00"
        pat.append(currency).append(currency).append(" #,##0.00;").append(currency).append(currency).append(" -#,##0.00"); 
        fmt = new DecimalFormat(pat.toString(), sym);
        s = "";
        s = ((NumberFormat) fmt).format(1234.56);
        logln("Pattern \"" + fmt.toPattern() + "\"");
        logln(" Format " + Double.toString(1234.56) + " . " + s);
    
        if (!s.equals("USD 1,234.56"))
            errln("FAIL: Expected USD 1,234.56");
        s = "";
        s = ((NumberFormat) fmt).format(-1234.56);
        logln(" Format " + Double.toString(-1234.56) + " . " + s);
        if (!s.equals("USD -1,234.56"))
            errln("FAIL: Expected USD -1,234.56");
    
    }
    
    /**
     * Test localized currency patterns.
     */
    public void TestCurrency() {
        String[] DATA = {
            "fr", "CA", "", "1,50 $",
            "de", "DE", "", "1,50 \u20AC",
            "de", "DE", "PREEURO", "1,50 DM",
            "fr", "FR", "", "1,50 \u20AC",
            "fr", "FR", "PREEURO", "1,50 F",
        };

        for (int i=0; i<DATA.length; i+=4) {
            Locale locale = new Locale(DATA[i], DATA[i+1], DATA[i+2]);
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            String s = fmt.format(1.50);
            if (s.equals(DATA[i+3])) {
                logln("Ok: 1.50 x " + locale + " => " + s);
            } else {
                logln("FAIL: 1.50 x " + locale + " => " + s +
                      ", expected " + DATA[i+3]);
            }
        }
        
        // format currency with CurrencyAmount
        for (int i=0; i<DATA.length; i+=4) {
            Locale locale = new Locale(DATA[i], DATA[i+1], DATA[i+2]);
            
            Currency curr = Currency.getInstance(locale);
            logln("\nName of the currency is: " + curr.getName(locale, Currency.LONG_NAME, new boolean[] {false}));
            CurrencyAmount cAmt = new CurrencyAmount(1.5, curr);
            logln("CurrencyAmount object's hashCode is: " + cAmt.hashCode()); //cover hashCode
            
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            String sCurr = fmt.format(cAmt);
            if (sCurr.equals(DATA[i+3])) {
                logln("Ok: 1.50 x " + locale + " => " + sCurr);
            } else {
                errln("FAIL: 1.50 x " + locale + " => " + sCurr +
                      ", expected " + DATA[i+3]);
            }            
        }
        
        //Cover MeasureFormat.getCurrencyFormat()
        ULocale save = ULocale.getDefault();
        ULocale.setDefault(ULocale.US);
        MeasureFormat curFmt = MeasureFormat.getCurrencyFormat();
        //Not knowing how to test this factory method,
        //  because the concreat class of MeasureFormat -
        //  CurrencyFormat is just for internal use.
        ULocale.setDefault(save);        
    }

    /**
     * Test the Currency object handling, new as of ICU 2.2.
     */
    public void TestCurrencyObject() {
        NumberFormat fmt = 
            NumberFormat.getCurrencyInstance(Locale.US);
        
        expectCurrency(fmt, null, 1234.56, "$1,234.56");

        expectCurrency(fmt, Currency.getInstance(Locale.FRANCE),
                       1234.56, "\u20AC1,234.56"); // Euro

        expectCurrency(fmt, Currency.getInstance(Locale.JAPAN),
                       1234.56, "\u00A51,235"); // Yen

        expectCurrency(fmt, Currency.getInstance(new Locale("fr", "CH", "")),
                       1234.56, "SwF1,234.55"); // 0.05 rounding

        expectCurrency(fmt, Currency.getInstance(Locale.US),
                       1234.56, "$1,234.56");

        fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        
        expectCurrency(fmt, null, 1234.56, "1 234,56 \u20AC");

        expectCurrency(fmt, Currency.getInstance(Locale.JAPAN),
                       1234.56, "1 235 \u00A5"); // Yen

        expectCurrency(fmt, Currency.getInstance(new Locale("fr", "CH", "")),
                       1234.56, "1 234,55 sFr."); // 0.25 rounding

        expectCurrency(fmt, Currency.getInstance(Locale.US),
                       1234.56, "1 234,56 $");

        expectCurrency(fmt, Currency.getInstance(Locale.FRANCE),
                       1234.56, "1 234,56 \u20AC"); // Euro
    }
    
    public void TestCurrencyPatterns() {
        int i;
        Locale[] locs = NumberFormat.getAvailableLocales();
        for (i=0; i<locs.length; ++i) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(locs[i]);
            // Make sure currency formats do not have a variable number
            // of fraction digits
            int min = nf.getMinimumFractionDigits();
            int max = nf.getMaximumFractionDigits();
            if (min != max) {
                String a = nf.format(1.0);
                String b = nf.format(1.125);
                errln("FAIL: " + locs[i] +
                      " min fraction digits != max fraction digits; "+
                      "x 1.0 => " + a +
                      "; x 1.125 => " + b);
            }
                
            // Make sure EURO currency formats have exactly 2 fraction digits
            if (nf instanceof DecimalFormat) {
                Currency curr = ((DecimalFormat) nf).getCurrency();
                if (curr != null && "EUR".equals(curr.getCurrencyCode())) {
                    if (min != 2 || max != 2) {
                        String a = nf.format(1.0);
                        errln("FAIL: " + locs[i] +
                              " is a EURO format but it does not have 2 fraction digits; "+
                              "x 1.0 => " +
                              a);
                    }
                }
            }
        }
    }

    /**
     * Do rudimentary testing of parsing.
     */
    public void TestParse() {
        String arg = "0.0";
        DecimalFormat format = new DecimalFormat("00");
        double aNumber = 0l;
        try {
            aNumber = format.parse(arg).doubleValue();
        } catch (ParseException e) {
            System.out.println(e);
        }
        logln("parse(" + arg + ") = " + aNumber);
    }
    
    /**
     * Test proper rounding by the format method.
     */
    public void TestRounding487() {
    
        NumberFormat nf = NumberFormat.getInstance();
        roundingTest(nf, 0.00159999, 4, "0.0016");
        roundingTest(nf, 0.00995, 4, "0.01");
    
        roundingTest(nf, 12.3995, 3, "12.4");
    
        roundingTest(nf, 12.4999, 0, "12");
        roundingTest(nf, - 19.5, 0, "-20");
    
    }
    
    /**
     * Test the functioning of the secondary grouping value.
     */
    public void TestSecondaryGrouping() {
    
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat f = new DecimalFormat("#,##,###", US);
    
        expect(f, 123456789L, "12,34,56,789");
        expectPat(f, "#,##,###");
        f.applyPattern("#,###");
    
        f.setSecondaryGroupingSize(4);
        expect(f, 123456789L, "12,3456,789");
        expectPat(f, "#,####,###");
        NumberFormat g = NumberFormat.getInstance(new Locale("hi", "IN"));
    
        String out = "";
        long l = 1876543210L;
        out = g.format(l);
    
        // expect "1,87,65,43,210", but with Hindi digits
        //         01234567890123
        boolean ok = true;
        if (out.length() != 14) {
            ok = false;
        } else {
            for (int i = 0; i < out.length(); ++i) {
                boolean expectGroup = false;
                switch (i) {
                    case 1 :
                    case 4 :
                    case 7 :
                    case 10 :
                        expectGroup = true;
                        break;
                }
                // Later -- fix this to get the actual grouping
                // character from the resource bundle.
                boolean isGroup = (out.charAt(i) == 0x002C);
                if (isGroup != expectGroup) {
                    ok = false;
                    break;
                }
            }
        }
        if (!ok) {
            errln("FAIL  Expected "+ l + " x hi_IN . \"1,87,65,43,210\" (with Hindi digits), got \""
                    + out + "\""); 
        } else {
            logln("Ok    " + l + " x hi_IN . \"" + out + "\"");
        }
    }
    
    public void roundingTest(NumberFormat nf, double x, int maxFractionDigits, final String expected) {
        nf.setMaximumFractionDigits(maxFractionDigits);
        String out = nf.format(x);
        logln(x + " formats with " + maxFractionDigits + " fractional digits to " + out); 
        if (!out.equals(expected))
            errln("FAIL: Expected " + expected);
    }
    
    /**
     * Upgrade to alphaWorks
     */
    public void TestExponent() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt1 = new DecimalFormat("0.###E0", US);
        DecimalFormat fmt2 = new DecimalFormat("0.###E+0", US);
        int n = 1234;
        expect2(fmt1, n, "1.234E3");
        expect2(fmt2, n, "1.234E+3");
        expect(fmt1, "1.234E+3", n); // Either format should parse "E+3"

    }
    
    /**
     * Upgrade to alphaWorks
     */
    public void TestScientific() {
    
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
    
        // Test pattern round-trip
        final String PAT[] = { "#E0", "0.####E0", "00.000E00", "##0.####E000", "0.###E0;[0.###E0]" };
        int PAT_length = PAT.length;
        int DIGITS[] = {
            // min int, max int, min frac, max frac
            0, 1, 0, 0, // "#E0"
            1, 1, 0, 4, // "0.####E0"
            2, 2, 3, 3, // "00.000E00"
            1, 3, 0, 4, // "##0.####E000"
            1, 1, 0, 3, // "0.###E0;[0.###E0]"
        };
        for (int i = 0; i < PAT_length; ++i) {
            String pat = PAT[i];
            DecimalFormat df = new DecimalFormat(pat, US);
            String pat2 = df.toPattern();
            if (pat.equals(pat2)) {
                logln("Ok   Pattern rt \"" + pat + "\" . \"" + pat2 + "\"");
            } else {
                errln("FAIL Pattern rt \"" + pat + "\" . \"" + pat2 + "\"");
            }
            // Make sure digit counts match what we expect
            if (df.getMinimumIntegerDigits() != DIGITS[4 * i]
                || df.getMaximumIntegerDigits() != DIGITS[4 * i + 1]
                || df.getMinimumFractionDigits() != DIGITS[4 * i + 2]
                || df.getMaximumFractionDigits() != DIGITS[4 * i + 3]) {
                errln("FAIL \""+ pat+ "\" min/max int; min/max frac = "
                        + df.getMinimumIntegerDigits() + "/"
                        + df.getMaximumIntegerDigits() + ";"
                        + df.getMinimumFractionDigits() + "/"
                        + df.getMaximumFractionDigits() + ", expect "
                        + DIGITS[4 * i] + "/"
                        + DIGITS[4 * i + 1] + ";" 
                        + DIGITS[4 * i + 2] + "/"
                        + DIGITS[4 * i + 3]); 
            }
        }
    
        expect2(new DecimalFormat("#E0", US), 12345.0, "1.2345E4");
        expect(new DecimalFormat("0E0", US), 12345.0, "1E4");
    
        // pattern of NumberFormat.getScientificInstance(Locale.US) = "0.######E0" not "#E0"
        // so result = 1.234568E4 not 1.2345678901E4
        //when the pattern problem is finalized, delete comment mark'//' 
        //of the following code
        expect2(NumberFormat.getScientificInstance(Locale.US), 12345.678901, "1.2345678901E4");
        logln("Testing NumberFormat.getScientificInstance(ULocale) ..."); 
        expect2(NumberFormat.getScientificInstance(ULocale.US), 12345.678901, "1.2345678901E4");        
    
        expect(new DecimalFormat("##0.###E0", US), 12345.0, "12.34E3");
        expect(new DecimalFormat("##0.###E0", US), 12345.00001, "12.35E3");
        expect2(new DecimalFormat("##0.####E0", US), 12345, "12.345E3");
    
        // pattern of NumberFormat.getScientificInstance(Locale.US) = "0.######E0" not "#E0"
        // so result = 1.234568E4 not 1.2345678901E4
        expect2(NumberFormat.getScientificInstance(Locale.FRANCE), 12345.678901, "1,2345678901E4");
        logln("Testing NumberFormat.getScientificInstance(ULocale) ...");
        expect2(NumberFormat.getScientificInstance(ULocale.FRANCE), 12345.678901, "1,2345678901E4");        
    
        expect(new DecimalFormat("##0.####E0", US), 789.12345e-9, "789.12E-9");
        expect2(new DecimalFormat("##0.####E0", US), 780.e-9, "780E-9");
        expect(new DecimalFormat(".###E0", US), 45678.0, ".457E5");
        expect2(new DecimalFormat(".###E0", US), 0, ".0E0");
        /*
        expect(new DecimalFormat[] { new DecimalFormat("#E0", US),
                                     new DecimalFormat("##E0", US),
                                     new DecimalFormat("####E0", US),
                                     new DecimalFormat("0E0", US),    
                                     new DecimalFormat("00E0", US),   
                                     new DecimalFormat("000E0", US), 
                                   },
               new Long(45678000),
               new String[] { "4.5678E7",
                              "45.678E6",
                              "4567.8E4",
                              "5E7",
                              "46E6",  
                              "457E5",
                            }
               );
        !
        ! Unroll this test into individual tests below...
        !
        */
        expect2(new DecimalFormat("#E0", US), 45678000, "4.5678E7");
        expect2(new DecimalFormat("##E0", US), 45678000, "45.678E6");
        expect2(new DecimalFormat("####E0", US), 45678000, "4567.8E4");
        expect(new DecimalFormat("0E0", US), 45678000, "5E7");
        expect(new DecimalFormat("00E0", US), 45678000, "46E6");
        expect(new DecimalFormat("000E0", US), 45678000, "457E5");
        /*
        expect(new DecimalFormat("###E0", US, status),
               new Object[] { new Double(0.0000123), "12.3E-6",
                              new Double(0.000123), "123E-6",
                              new Double(0.00123), "1.23E-3",
                              new Double(0.0123), "12.3E-3",
                              new Double(0.123), "123E-3",
                              new Double(1.23), "1.23E0",
                              new Double(12.3), "12.3E0",
                              new Double(123), "123E0",
                              new Double(1230), "1.23E3",
                             });
        !
        ! Unroll this test into individual tests below...
        !
        */
        expect2(new DecimalFormat("###E0", US), 0.0000123, "12.3E-6");
        expect2(new DecimalFormat("###E0", US), 0.000123, "123E-6");
        expect2(new DecimalFormat("###E0", US), 0.00123, "1.23E-3");
        expect2(new DecimalFormat("###E0", US), 0.0123, "12.3E-3");
        expect2(new DecimalFormat("###E0", US), 0.123, "123E-3");
        expect2(new DecimalFormat("###E0", US), 1.23, "1.23E0");
        expect2(new DecimalFormat("###E0", US), 12.3, "12.3E0");
        expect2(new DecimalFormat("###E0", US), 123.0, "123E0");
        expect2(new DecimalFormat("###E0", US), 1230.0, "1.23E3");
        /*
        expect(new DecimalFormat("0.#E+00", US, status),
               new Object[] { new Double(0.00012), "1.2E-04",
                              new Long(12000),     "1.2E+04",
                             });
        !
        ! Unroll this test into individual tests below...
        !
        */
        expect2(new DecimalFormat("0.#E+00", US), 0.00012, "1.2E-04");
        expect2(new DecimalFormat("0.#E+00", US), 12000, "1.2E+04");
    }
    
    /**
     * Upgrade to alphaWorks
     */
    public void TestPad() {
    
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        expect2(new DecimalFormat("*^##.##", US), 0, "^^^^0");
        expect2(new DecimalFormat("*^##.##", US), -1.3, "^-1.3");
        expect2(
            new DecimalFormat("##0.0####E0*_ 'g-m/s^2'", US), 
            0, 
            "0.0E0______ g-m/s^2"); 
        expect(
            new DecimalFormat("##0.0####E0*_ 'g-m/s^2'", US), 
            1.0 / 3, 
            "333.333E-3_ g-m/s^2"); 
        expect2(new DecimalFormat("##0.0####*_ 'g-m/s^2'", US), 0, "0.0______ g-m/s^2");
        expect(
            new DecimalFormat("##0.0####*_ 'g-m/s^2'", US), 
            1.0 / 3, 
            "0.33333__ g-m/s^2"); 
    
        // Test padding before a sign
        final String formatStr = "*x#,###,###,##0.0#;*x(###,###,##0.0#)";
        expect2(new DecimalFormat(formatStr, US), -10, "xxxxxxxxxx(10.0)");
        expect2(new DecimalFormat(formatStr, US), -1000, "xxxxxxx(1,000.0)");
        expect2(new DecimalFormat(formatStr, US), -1000000, "xxx(1,000,000.0)");
        expect2(new DecimalFormat(formatStr, US), -100.37, "xxxxxxxx(100.37)");
        expect2(new DecimalFormat(formatStr, US), -10456.37, "xxxxx(10,456.37)");
        expect2(new DecimalFormat(formatStr, US), -1120456.37, "xx(1,120,456.37)");
        expect2(new DecimalFormat(formatStr, US), -112045600.37, "(112,045,600.37)");
        expect2(new DecimalFormat(formatStr, US), -1252045600.37, "(1,252,045,600.37)");
    
        expect2(new DecimalFormat(formatStr, US), 10, "xxxxxxxxxxxx10.0");
        expect2(new DecimalFormat(formatStr, US), 1000, "xxxxxxxxx1,000.0");
        expect2(new DecimalFormat(formatStr, US), 1000000, "xxxxx1,000,000.0");
        expect2(new DecimalFormat(formatStr, US), 100.37, "xxxxxxxxxx100.37");
        expect2(new DecimalFormat(formatStr, US), 10456.37, "xxxxxxx10,456.37");
        expect2(new DecimalFormat(formatStr, US), 1120456.37, "xxxx1,120,456.37");
        expect2(new DecimalFormat(formatStr, US), 112045600.37, "xx112,045,600.37");
        expect2(new DecimalFormat(formatStr, US), 10252045600.37, "10,252,045,600.37");
    
        // Test padding between a sign and a number
        final String formatStr2 = "#,###,###,##0.0#*x;(###,###,##0.0#*x)";
        expect2(new DecimalFormat(formatStr2, US), -10, "(10.0xxxxxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1000, "(1,000.0xxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1000000, "(1,000,000.0xxx)");
        expect2(new DecimalFormat(formatStr2, US), -100.37, "(100.37xxxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -10456.37, "(10,456.37xxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1120456.37, "(1,120,456.37xx)");
        expect2(new DecimalFormat(formatStr2, US), -112045600.37, "(112,045,600.37)");
        expect2(new DecimalFormat(formatStr2, US), -1252045600.37, "(1,252,045,600.37)"); 
    
        expect2(new DecimalFormat(formatStr2, US), 10, "10.0xxxxxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1000, "1,000.0xxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1000000, "1,000,000.0xxxxx");
        expect2(new DecimalFormat(formatStr2, US), 100.37, "100.37xxxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 10456.37, "10,456.37xxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1120456.37, "1,120,456.37xxxx");
        expect2(new DecimalFormat(formatStr2, US), 112045600.37, "112,045,600.37xx");
        expect2(new DecimalFormat(formatStr2, US), 10252045600.37, "10,252,045,600.37");
    
        //testing the setPadCharacter(UnicodeString) and getPadCharacterString()
        DecimalFormat fmt = new DecimalFormat("#", US);
        char padString = 'P';
        fmt.setPadCharacter(padString);
        expectPad(fmt, "*P##.##", DecimalFormat.PAD_BEFORE_PREFIX, 5, padString);
        fmt.setPadCharacter('^');
        expectPad(fmt, "*^#", DecimalFormat.PAD_BEFORE_PREFIX, 1, '^');
        //commented untill implementation is complete
        /*  fmt.setPadCharacter((UnicodeString)"^^^");
          expectPad(fmt, "*^^^#", DecimalFormat.kPadBeforePrefix, 3, (UnicodeString)"^^^");
          padString.remove();
          padString.append((UChar)0x0061);
          padString.append((UChar)0x0302);
          fmt.setPadCharacter(padString);
          UChar patternChars[]={0x002a, 0x0061, 0x0302, 0x0061, 0x0302, 0x0023, 0x0000};
          UnicodeString pattern(patternChars);
          expectPad(fmt, pattern , DecimalFormat.kPadBeforePrefix, 4, padString);
          */
    }
    
    /**
     * Upgrade to alphaWorks
     */
    public void TestPatterns2() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("#", US);
    
        char hat = 0x005E; /*^*/
    
        expectPad(fmt, "*^#", DecimalFormat.PAD_BEFORE_PREFIX, 1, hat);
        expectPad(fmt, "$*^#", DecimalFormat.PAD_AFTER_PREFIX, 2, hat);
        expectPad(fmt, "#*^", DecimalFormat.PAD_BEFORE_SUFFIX, 1, hat);
        expectPad(fmt, "#$*^", DecimalFormat.PAD_AFTER_SUFFIX, 2, hat);
        expectPad(fmt, "$*^$#", -1);
        expectPad(fmt, "#$*^$", -1);
        expectPad(fmt, "'pre'#,##0*x'post'", DecimalFormat.PAD_BEFORE_SUFFIX, 12, (char) 0x0078 /*x*/); 
        expectPad(fmt, "''#0*x", DecimalFormat.PAD_BEFORE_SUFFIX, 3, (char) 0x0078 /*x*/); 
        expectPad(fmt, "'I''ll'*a###.##", DecimalFormat.PAD_AFTER_PREFIX, 10, (char) 0x0061 /*a*/); 
    
        fmt.applyPattern("AA#,##0.00ZZ");
        fmt.setPadCharacter(hat);
    
        fmt.setFormatWidth(10);
    
        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_PREFIX);
        expectPat(fmt, "*^AA#,##0.00ZZ");
    
        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_SUFFIX);
        expectPat(fmt, "AA#,##0.00*^ZZ");
    
        fmt.setPadPosition(DecimalFormat.PAD_AFTER_SUFFIX);
        expectPat(fmt, "AA#,##0.00ZZ*^");
    
        //            12  3456789012
        String exp = "AA*^#,##0.00ZZ";
        fmt.setFormatWidth(12);
        fmt.setPadPosition(DecimalFormat.PAD_AFTER_PREFIX);
        expectPat(fmt, exp);
    
        fmt.setFormatWidth(13);
        //              12  34567890123
        expectPat(fmt, "AA*^##,##0.00ZZ");
    
        fmt.setFormatWidth(14);
        //              12  345678901234
        expectPat(fmt, "AA*^###,##0.00ZZ");
    
        fmt.setFormatWidth(15);
        //              12  3456789012345
        expectPat(fmt, "AA*^####,##0.00ZZ"); // This is the interesting case
    
        fmt.setFormatWidth(16);
        //              12  34567890123456
        expectPat(fmt, "AA*^#,###,##0.00ZZ");
    }

    public void TestRegistration() {
        final ULocale SRC_LOC = ULocale.FRANCE;
        final ULocale SWAP_LOC = ULocale.US;

        class TestFactory extends SimpleNumberFormatFactory {
            NumberFormat currencyStyle;

            TestFactory() {
                super(SRC_LOC, true);
                currencyStyle = NumberFormat.getIntegerInstance(SWAP_LOC);
            }

            public NumberFormat createFormat(ULocale loc, int formatType) {
                if (formatType == FORMAT_CURRENCY) {
                    return currencyStyle;
                }
                return null;
            }
        }
        
        NumberFormat f0 = NumberFormat.getIntegerInstance(SWAP_LOC);
        NumberFormat f1 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat f2 = NumberFormat.getCurrencyInstance(SRC_LOC);
        Object key = NumberFormat.registerFactory(new TestFactory());
        NumberFormat f3 = NumberFormat.getCurrencyInstance(SRC_LOC);
        NumberFormat f4 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat.unregister(key); // restore for other tests
        NumberFormat f5 = NumberFormat.getCurrencyInstance(SRC_LOC);

        float n = 1234.567f;
        logln("f0 swap int: " + f0.format(n));
        logln("f1 src int: " + f1.format(n));
        logln("f2 src cur: " + f2.format(n));
        logln("f3 reg cur: " + f3.format(n));
        logln("f4 reg int: " + f4.format(n));
        logln("f5 unreg cur: " + f5.format(n));

        if (!f3.format(n).equals(f0.format(n))) {
            errln("registered service did not match");
        }
        if (!f4.format(n).equals(f1.format(n))) {
            errln("registered service did not inherit");
        }
        if (!f5.format(n).equals(f2.format(n))) {
            errln("unregistered service did not match original");
        }
    }

    public void TestScientific2() {
        // jb 2552
        DecimalFormat fmt = (DecimalFormat)NumberFormat.getCurrencyInstance();
        Number num = new Double(12.34);
        expect(fmt, num, "$12.34");
        fmt.setScientificNotation(true);
        expect(fmt, num, "$1.23E1");
        fmt.setScientificNotation(false);
        expect(fmt, num, "$12.34");
    }

    public void TestScientificGrouping() {
        // jb 2552
        DecimalFormat fmt = new DecimalFormat("###.##E0");
        expect(fmt, .01234, "12.3E-3");
        expect(fmt, .1234, "123E-3");
        expect(fmt, 1.234, "1.23E0");
        expect(fmt, 12.34, "12.3E0");
        expect(fmt, 123.4, "123E0");
        expect(fmt, 1234, "1.23E3");
    }

    // additional coverage tests
    
    // sigh, can't have static inner classes, why not?

    static final class PI extends Number {
        private PI() {};
        public int intValue() { return (int)Math.PI; }
        public long longValue() { return (long)Math.PI; }
        public float  floatValue() { return (float)Math.PI; }
        public double doubleValue() { return (double)Math.PI; }
        public byte byteValue() { return (byte)Math.PI; }
        public short shortValue() { return (short)Math.PI; }

        public static final Number INSTANCE = new PI();
    }

    public void TestCoverage() {
        NumberFormat fmt = NumberFormat.getNumberInstance(); // default locale
        logln(fmt.format(new BigInteger("1234567890987654321234567890987654321", 10)));

        fmt = NumberFormat.getScientificInstance(); // default locale

        logln(fmt.format(PI.INSTANCE));

        try {
            logln(fmt.format("12345"));
            errln("numberformat of string did not throw exception");
        }
        catch (Exception e) {
        }

        int hash = fmt.hashCode();

        logln("compare to string returns: " + fmt.equals(""));

        // For ICU 2.6 - alan
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("'*&'' '\u00A4' ''&*' #,##0.00", US);
        df.setCurrency(Currency.getInstance("INR"));
        expect2(df, 1.0, "*&' Re. '&* 1.00");
        expect2(df, -2.0, "-*&' Rs. '&* 2.00");
        df.applyPattern("#,##0.00 '*&'' '\u00A4' ''&*'");
        expect2(df, 2.0, "2.00 *&' Rs. '&*");
        expect2(df, -1.0, "-1.00 *&' Re. '&*");

        java.math.BigDecimal r = df.getRoundingIncrement();
        if (r != null) {
            errln("FAIL: rounding = " + r + ", expect null");
        }
        
        if (df.isScientificNotation()) {
            errln("FAIL: isScientificNotation = true, expect false");
        }

        df.applyPattern("0.00000");
        df.setScientificNotation(true);
        if (!df.isScientificNotation()) {
            errln("FAIL: isScientificNotation = false, expect true");
        }
        df.setMinimumExponentDigits((byte)2);
        if (df.getMinimumExponentDigits() != 2) {
            errln("FAIL: getMinimumExponentDigits = " +
                  df.getMinimumExponentDigits() + ", expect 2");
        }
        df.setExponentSignAlwaysShown(true);
        if (!df.isExponentSignAlwaysShown()) {
            errln("FAIL: isExponentSignAlwaysShown = false, expect true");
        }
        df.setSecondaryGroupingSize(0);
        if (df.getSecondaryGroupingSize() != 0) {
            errln("FAIL: getSecondaryGroupingSize = " +
                  df.getSecondaryGroupingSize() + ", expect 0");
        }
        expect2(df, 3.14159, "3.14159E+00");
    }

    public void TestWhiteSpaceParsing() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("a  b#0c  ", US);
        int n = 1234;
        expect(fmt, "a b1234c ", n);
        expect(fmt, "a   b1234c   ", n);
    }

    /**
     * Test currencies whose display name is a ChoiceFormat.
     */
    public void TestComplexCurrency() {
        Locale loc = new Locale("en", "IN", "");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(loc);

        expect2(fmt, 1.0, "Re. 1.00");
        expect(fmt, 1.001, "Re. 1.00"); // tricky
        expect2(fmt, 12345678.0, "Rs. 1,23,45,678.00");
        expect2(fmt, 0.5, "Rs. 0.50");
        expect2(fmt, -1.0, "-Re. 1.00");
        expect2(fmt, -10.0, "-Rs. 10.00");
    }

    public void TestCurrencyKeyword() {
    ULocale locale = new ULocale("th_TH@currency=QQQ");
    NumberFormat format = NumberFormat.getCurrencyInstance(locale);
    String result = format.format(12.34f);
    if (!"QQQ12.34".equals(result)) {
        errln("got unexpected currency: " + result);
    }
    }
    
    public void TestThreadedFormat() {

        class FormatTask implements Runnable {
            DecimalFormat fmt;
            StringBuffer buf;
            boolean inc;
            float num;

            FormatTask(DecimalFormat fmt, int index) {
                this.fmt = fmt;
                this.buf = new StringBuffer();
                this.inc = (index & 0x1) == 0;
                this.num = inc ? 0 : 10000;
            }

            public void run() {
        if (inc) {
            while (num < 10000) {
            buf.append(fmt.format(num) + "\n");
            num += 3.14159;
            }
        } else {
            while (num > 0) {
            buf.append(fmt.format(num) + "\n");
            num -= 3.14159;
            }
        }
        }

        String result() {
        return buf.toString();
        }
    }

        DecimalFormat fmt = new DecimalFormat("0.####");
        FormatTask[] tasks = new FormatTask[8];
        for (int i = 0; i < tasks.length; ++i) {
        tasks[i] = new FormatTask(fmt, i);
    }

    TestUtil.runUntilDone(tasks);

        for (int i = 2; i < tasks.length; i++) {
        String str1 = tasks[i].result();
        String str2 = tasks[i-2].result();
            if (!str1.equals(str2)) {
                System.out.println("mismatch at " + i);
                System.out.println(str1);
                System.out.println(str2);
                errln("decimal format thread mismatch");
                
                break;
            }
            str1 = str2;
        }
    }

    public void TestPerMill() {
        DecimalFormat fmt = new DecimalFormat("###.###\u2030");
        assertEquals("0.4857 x ###.###\u2030",
                     "485.7\u2030", fmt.format(0.4857));

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.ENGLISH);
        sym.setPerMill('m');
        DecimalFormat fmt2 = new DecimalFormat("", sym);
        fmt2.applyLocalizedPattern("###.###m");
        assertEquals("0.4857 x ###.###m",
                     "485.7m", fmt2.format(0.4857));
    }

    public void TestIllegalPatterns() {
        // Test cases:
        // Prefix with "-:" for illegal patterns
        // Prefix with "+:" for legal patterns
        String DATA[] = {
            // Unquoted special characters in the suffix are illegal
            "-:000.000|###",
            "+:000.000'|###'",
        };
        for (int i=0; i<DATA.length; ++i) {
            String pat=DATA[i];
            boolean valid = pat.charAt(0) == '+';
            pat = pat.substring(2);
            Exception e = null;
            try {
                // locale doesn't matter here
                new DecimalFormat(pat);
            } catch (IllegalArgumentException e1) {
                e = e1;
            } catch (IndexOutOfBoundsException e1) {
                e = e1;
            }
            String msg = (e==null) ? "success" : e.getMessage();
            if ((e==null) == valid) {
                logln("Ok: pattern \"" + pat + "\": " + msg);
            } else {
                errln("FAIL: pattern \"" + pat + "\" should have " +
                      (valid?"succeeded":"failed") + "; got " + msg);
            }
        }
    }

    /**
     * Parse a CurrencyAmount using the given NumberFormat, with
     * the 'delim' character separating the number and the currency.
     */
    private static CurrencyAmount parseCurrencyAmount(String str, NumberFormat fmt,
                                                      char delim) 
        throws ParseException {
        int i = str.indexOf(delim);
        return new CurrencyAmount(fmt.parse(str.substring(0,i)),
                                  Currency.getInstance(str.substring(i+1)));
    }

    /**
     * Return an integer representing the next token from this
     * iterator.  The integer will be an index into the given list, or
     * -1 if there are no more tokens, or -2 if the token is not on
     * the list.
     */
    private static int keywordIndex(String tok) {
        for (int i=0; i<KEYWORDS.length; ++i) {
            if (tok.equals(KEYWORDS[i])) {
                return i;
            }
        }
        return -1;
    }

    private static final String KEYWORDS[] = {
        /*0*/ "ref=", // <reference pattern to parse numbers>
        /*1*/ "loc=", // <locale for formats>
        /*2*/ "f:",   // <pattern or '-'> <number> <exp. string>
        /*3*/ "fp:",  // <pattern or '-'> <number> <exp. string> <exp. number>
        /*4*/ "rt:",  // <pattern or '-'> <(exp.) number> <(exp.) string>
        /*5*/ "p:",   // <pattern or '-'> <string> <exp. number>
        /*6*/ "perr:", // <pattern or '-'> <invalid string>
        /*7*/ "pat:", // <pattern or '-'> <exp. toPattern or '-' or 'err'>
        /*8*/ "fpc:", // <loc or '-'> <curr.amt> <exp. string> <exp. curr.amt>
    };

    public void TestCases() {
        String caseFileName = "NumberFormatTestCases.txt";
        java.io.InputStream is = NumberFormatTest.class.getResourceAsStream(caseFileName);

        ResourceReader reader = new ResourceReader(is, caseFileName);
        TokenIterator tokens = new TokenIterator(reader);

        Locale loc = new Locale("en", "US", "");
        DecimalFormat ref = null, fmt = null;
        MeasureFormat mfmt = null;
        String pat = null, str = null, mloc = null;

        try {
            for (;;) {
                String tok = tokens.next();
                if (tok == null) {
                    break;
                }
                String where = "(" + tokens.getLineNumber() + ") ";
                int cmd = keywordIndex(tok);
                switch (cmd) {
                case 0:
                    // ref= <reference pattern>
                    ref = new DecimalFormat(tokens.next(),
                                            new DecimalFormatSymbols(Locale.US));
                    break;
                case 1:
                    // loc= <locale>
                    loc = LocaleUtility.getLocaleFromName(tokens.next());
                    break;
                case 2: // f:
                case 3: // fp:
                case 4: // rt:
                case 5: // p:
                    tok = tokens.next();
                    if (!tok.equals("-")) {
                        pat = tok;
                        try {
                            fmt = new DecimalFormat(pat, new DecimalFormatSymbols(loc));
                        } catch (IllegalArgumentException iae) {
                            errln(where + "Pattern \"" + pat + '"');
                            iae.printStackTrace();
                            tokens.next(); // consume remaining tokens
                            tokens.next();
                            if (cmd == 3) tokens.next();
                            continue;
                        }
                    }
                    str = null;
                    try {
                        if (cmd == 2 || cmd == 3 || cmd == 4) {
                            // f: <pattern or '-'> <number> <exp. string>
                            // fp: <pattern or '-'> <number> <exp. string> <exp. number>
                            // rt: <pattern or '-'> <number> <string>
                            String num = tokens.next();
                            str = tokens.next();
                            Number n = (Number) ref.parse(num);
                            assertEquals(where + '"' + pat + "\".format(" + num + ")",
                                         str, fmt.format(n));
                            if (cmd == 3) { // fp:
                                n = (Number) ref.parse(tokens.next());
                            }
                            if (cmd != 2) { // != f:
                                assertEquals(where + '"' + pat + "\".parse(\"" + str + "\")",
                                             n, fmt.parse(str));
                            } 
                        }
                        // p: <pattern or '-'> <string to parse> <exp. number>
                        else {
                            str = tokens.next();
                            String expstr = tokens.next();
                            Number exp = (Number) ref.parse(expstr);
                            assertEquals(where + '"' + pat + "\".parse(\"" + str + "\")",
                                         exp, fmt.parse(str));
                        }
                    } catch (ParseException e) {
                        errln(where + '"' + pat + "\".parse(\"" + str +
                              "\") threw an exception");
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    // perr: <pattern or '-'> <invalid string>
                    errln("Under construction");
                    return;
                case 7:
                    // pat: <pattern> <exp. toPattern, or '-' or 'err'>
                    String testpat = tokens.next();
                    String exppat  = tokens.next();
                    boolean err    = exppat.equals("err");
                    if (testpat.equals("-")) {
                        if (err) {
                            errln("Invalid command \"pat: - err\" at " +  tokens.describePosition());
                            continue;
                        }
                        testpat = pat;
                    }
                    if (exppat.equals("-")) exppat = testpat;
                    try {
                        DecimalFormat f = null;
                        if (testpat == pat) { // [sic]
                            f = fmt;
                        } else {
                            f = new DecimalFormat(testpat);
                        }
                        if (err) {
                            errln(where + "Invalid pattern \"" + testpat +
                                  "\" was accepted");
                        } else {
                            assertEquals(where + '"' + testpat + "\".toPattern()",
                                         exppat, f.toPattern());
                        }
                    } catch (IllegalArgumentException iae2) {
                        if (err) {
                            logln("Ok: " + where + "Invalid pattern \"" + testpat +
                                  "\" threw an exception");
                        } else {
                            errln(where + "Valid pattern \"" + testpat +
                                  "\" threw an exception");
                            iae2.printStackTrace();
                        }
                    }
                    break;
                case 8: // fpc:
                    tok = tokens.next();
                    if (!tok.equals("-")) {
                        mloc = tok;
                        ULocale l = new ULocale(mloc);
                        try {
                            mfmt = MeasureFormat.getCurrencyFormat(l);
                        } catch (IllegalArgumentException iae) {
                            errln(where + "Loc \"" + tok + '"');
                            iae.printStackTrace();
                            tokens.next(); // consume remaining tokens
                            tokens.next();
                            tokens.next();
                            continue;
                        }
                    }
                    str = null;
                    try {
                        // fpc: <loc or '-'> <curr.amt> <exp. string> <exp. curr.amt>
                        String currAmt = tokens.next();
                        str = tokens.next();
                        CurrencyAmount n = parseCurrencyAmount(currAmt, ref, '/');
                        assertEquals(where + "getCurrencyFormat(" + mloc + ").format(" + currAmt + ")",
                                     str, mfmt.format(n));
                        n = parseCurrencyAmount(tokens.next(), ref, '/');
                        assertEquals(where + "getCurrencyFormat(" + mloc + ").parse(\"" + str + "\")",
                                     n, (CurrencyAmount) mfmt.parseObject(str));
                    } catch (ParseException e) {
                        errln(where + '"' + pat + "\".parse(\"" + str +
                              "\") threw an exception");
                        e.printStackTrace();
                    }
                    break;
                case -1:
                    errln("Unknown command \"" + tok + "\" at " + tokens.describePosition());
                    return;
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------------------
    // Support methods
    //------------------------------------------------------------------

    // Format-Parse test
    public void expect2(NumberFormat fmt, Number n, String exp) {
        // Don't round-trip format test, since we explicitly do it
        expect(fmt, n, exp, false);
        expect(fmt, exp, n);
    }

    // Format-Parse test (convenience)
    public void expect2(NumberFormat fmt, double n, String exp) {
        expect2(fmt, new Double(n), exp);
    }

    // Format-Parse test (convenience)
    public void expect2(NumberFormat fmt, long n, String exp) {
        expect2(fmt, new Long(n), exp);
    }

    // Format test
    public void expect(NumberFormat fmt, Number n, String exp, boolean rt) {
        StringBuffer saw = new StringBuffer();
        FieldPosition pos = new FieldPosition(0); 
        fmt.format(n, saw, pos);
        String pat = ((DecimalFormat)fmt).toPattern();
        if (saw.toString().equals(exp)) {
            logln("Ok   " + n + " x " +
                  pat + " = \"" +
                  saw + "\"");
            // We should be able to round-trip the formatted string =>
            // number => string (but not the other way around: number
            // => string => number2, might have number2 != number):
            if (rt) {
                try {
                    Number n2 = fmt.parse(exp);
                    StringBuffer saw2 = new StringBuffer();
                    fmt.format(n2, saw2, pos);
                    if (!saw2.toString().equals(exp)) {
                        errln("FAIL \"" + exp + "\" => " + n2 +
                              " => \"" + saw2 + '"');
                    }
                } catch (ParseException e) {
                    errln(e.getMessage());
                    return;
                }
            }
        } else {
            errln("FAIL " + n + " x " +
                  pat + " = \"" +
                  saw + "\", expected \"" + exp + "\"");
        }
    }
    
    // Format test (convenience)
    public void expect(NumberFormat fmt, Number n, String exp) {
        expect(fmt, n, exp, true);
    }

    // Format test (convenience)
    public void expect(NumberFormat fmt, double n, String exp) {
        expect(fmt, new Double(n), exp);
    }
    
    // Format test (convenience)
    public void expect(NumberFormat fmt, long n, String exp) {
        expect(fmt, new Long(n), exp);
    }
    
    // Parse test
    public void expect(NumberFormat fmt, String str, Number n) {
        Number num = null;
        try {
            num = (Number) fmt.parse(str);
        } catch (ParseException e) {
            errln(e.getMessage());
            return;
        }
        String pat = ((DecimalFormat)fmt).toPattern();
        // A little tricky here -- make sure Double(12345.0) and
        // Long(12345) match.
        if (num.equals(n) || num.doubleValue() == n.doubleValue()) {
            logln("Ok   \"" + str + "\" x " +
                  pat + " = " +
                  num);
        } else {
            errln("FAIL \"" + str + "\" x " +
                  pat + " = " +
                  num + ", expected " + n);
        }
    }
    
    // Parse test (convenience)
    public void expect(NumberFormat fmt, String str, double n) {
        expect(fmt, str, new Double(n));
    }
    
    // Parse test (convenience)
    public void expect(NumberFormat fmt, String str, long n) {
        expect(fmt, str, new Long(n));
    }
    
    private void expectCurrency(NumberFormat nf, Currency curr,
                                double value, String string) {
        DecimalFormat fmt = (DecimalFormat) nf;
        if (curr != null) {
            fmt.setCurrency(curr);
        }
        String s = fmt.format(value).replace('\u00A0', ' ');

        if (s.equals(string)) {
            logln("Ok: " + value + " x " + curr + " => " + s);
        } else {
            errln("FAIL: " + value + " x " + curr + " => " + s +
                  ", expected " + string);
        }
    }

    public void expectPad(DecimalFormat fmt, String pat, int pos) {
        expectPad(fmt, pat, pos, 0, (char)0);
    }
    
    public void expectPad(DecimalFormat fmt, final String pat, int pos, int width, final char pad) {
        int apos = 0, awidth = 0;
        char apadStr;
        try {
            fmt.applyPattern(pat);
            apos = fmt.getPadPosition();
            awidth = fmt.getFormatWidth();
            apadStr = fmt.getPadCharacter();
        } catch (Exception e) {
            apos = -1;
            awidth = width;
            apadStr = pad;
        }
    
        if (apos == pos && awidth == width && apadStr == pad) {
            logln("Ok   \"" + pat + "\" pos="
                    + apos + ((pos == -1) ? "" : " width=" + awidth + " pad=" + apadStr)); 
        } else {
            errln("FAIL \"" + pat + "\" pos=" + apos + " width="
                    + awidth + " pad=" + apadStr + ", expected "
                    + pos + " " + width + " " + pad); 
        }
    }

    public void expectPat(DecimalFormat fmt, final String exp) {
        String pat = fmt.toPattern();
        if (pat.equals(exp)) {
            logln("Ok   \"" + pat + "\"");
        } else {
            errln("FAIL \"" + pat + "\", expected \"" + exp + "\"");
        }
    }
}
