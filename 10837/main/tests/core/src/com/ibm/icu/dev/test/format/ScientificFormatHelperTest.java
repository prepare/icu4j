/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.text.AttributedCharacterIterator;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.ScientificFormatHelper;
import com.ibm.icu.util.ULocale;

/**
 * @author rocketman
 *
 */
public class ScientificFormatHelperTest extends TestFmwk {
    
    public static void main(String[] args) throws Exception {
        new ScientificFormatHelperTest().run(args);
    }
    
    public void TestBasic() {
        ULocale en = new ULocale("en");
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getScientificInstance(en);
        AttributedCharacterIterator iterator = decfmt.formatToCharacterIterator(1.23456e-78);
        ScientificFormatHelper helper = ScientificFormatHelper.getInstance(
                decfmt.getDecimalFormatSymbols());
        assertEquals(
                "insetMarkup",
                "1.23456\u00d710<sup>-78</sup>",
                helper.insertMarkup(iterator, "<sup>", "</sup>"));
        assertEquals(
                "toSuperscriptExponentDigits",
                "1.23456\u00d710\u207b\u2077\u2078",
                helper.toSuperscriptExponentDigits(iterator));
    }

}
