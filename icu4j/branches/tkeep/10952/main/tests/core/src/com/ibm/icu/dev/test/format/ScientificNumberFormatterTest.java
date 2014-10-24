/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.ScientificNumberFormatter;
import com.ibm.icu.util.ULocale;

/**
 * @author rocketman
 *
 */
public class ScientificNumberFormatterTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new ScientificNumberFormatterTest().run(args);
    }
    
    public void TestBasic() {
        ULocale en = new ULocale("en");
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                en, "<sup>", "</sup>");
        ScientificNumberFormatter fmt2 = ScientificNumberFormatter.getSuperscriptInstance(en);
        assertEquals(
                "insetMarkup",
                "1.23456\u00d710<sup>-78</sup>",
                fmt.format(1.23456e-78));
        assertEquals(
                "toSuperscriptExponentDigits",
                "1.23456\u00d710\u207b\u2077\u2078",
                fmt2.format(1.23456e-78));
    }

    public void TestPlusSignInExponentMarkup() {
        ULocale en = new ULocale("en");
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getScientificInstance(en);
        decfmt.applyPattern("0.00E+0");
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                decfmt, "<sup>", "</sup>");
                
        assertEquals(
                "",
                "6.02\u00d710<sup>+23</sup>",
                fmt.format(6.02e23));
    }

    
    public void TestPlusSignInExponentSuperscript() {
        ULocale en = new ULocale("en");
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getScientificInstance(en);
        decfmt.applyPattern("0.00E+0");
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getSuperscriptInstance(
                decfmt);
        assertEquals(
                "",
                "6.02\u00d710\u207a\u00b2\u00b3",
                fmt.format(6.02e23));
    }
    
    public void TestFixedDecimalMarkup() {
        ULocale en = new ULocale("en");
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getInstance(en);
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                decfmt, "<sup>", "</sup>");
        assertEquals(
                "",
                "123,456",
                fmt.format(123456.0));
    }
    
    public void TestFixedDecimalSuperscript() {
        ULocale en = new ULocale("en");
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getInstance(en);
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getSuperscriptInstance(decfmt);
        assertEquals(
                "",
                "123,456",
                fmt.format(123456.0));
    }
}
