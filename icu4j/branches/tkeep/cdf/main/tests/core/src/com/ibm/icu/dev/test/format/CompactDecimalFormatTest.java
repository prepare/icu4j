/*
 *******************************************************************************
 * Copyright (C) 1996-2012, Google, International Business Machines Corporation and
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.CompactDecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.NumberFormat.CompactStyle;
import com.ibm.icu.util.ULocale;

public class CompactDecimalFormatTest extends TestFmwk {

    public static void main(String[] args) {
        new CompactDecimalFormatTest().run(args);
    }

    Object[][] EnglishTestData = {
            // default is 2 digits of accuracy
            {0.0d, "0.0"},
            {0.1d, "0.1"},
            {1d, "1"},
            {1234, "1.2K"},
            {12345, "12K"},
            {123456, "120K"},
            {1234567, "1.2M"},
            {12345678, "12M"},
            {123456789, "120M"},
            {1234567890, "1.2B"},
            {12345678901f, "12B"},
            {123456789012f, "120B"},
            {1234567890123f, "1.2T"},
            {12345678901234f, "12T"},
            {123456789012345f, "120T"},
            {12345678901234567890f, "12000000T"},
    };

    Object[][] SerbianTestDataShort = {
            {1234, "1200"},
            {12345, "12K"},
            {20789, "21 хиљ"},
            {123456, "120 хиљ"},
            {1234567, "1,2 мил"},
            {12345678, "12 мил"},
            {123456789, "120 мил"},
            {1234567890, "1,2 млрд"},
            {12345678901f, "12 млрд"},
            {123456789012f, "120 млрд"},
            {1234567890123f, "1,2 бил"},
            {12345678901234f, "12 бил"},
            {123456789012345f, "120 бил"},
    };

    Object[][] SerbianTestDataLong = {
            {1234, "1,2 хиљада"},
            {12345, "12 хиљада"},
            {21789, "22 хиљаде"},
            {123456, "120 хиљада"},
            {999999, "1 милион"},
            {1234567, "1,2 милиона"},
            {12345678, "12 милиона"},
            {123456789, "120 милиона"},
            {1234567890, "1,2 милијарди"},
            {12345678901f, "12 милијарди"},
            {20890123456f, "21 милијарда"},
            {21890123456f, "22 милијарде"},
            {123456789012f, "120 милијарди"},
            {1234567890123f, "1,2 трилиона"},
            {12345678901234f, "12 трилиона"},
            {123456789012345f, "120 трилиона"},
    };

    Object[][] SwahiliTestData = {
            {1234, "elfu\u00a01.2"},
            {12345, "elfu\u00a012"},
            {123456, "laki1.2"},
            {1234567, "M1.2"},
            {12345678, "M12"},
            {123456789, "M120"},
            {1234567890, "B1.2"},
            {12345678901f, "B12"},
            {123456789012f, "B120"},
            {1234567890123f, "T1.2"},
            {12345678901234f, "T12"},
            {12345678901234567890f, "T12000000"},
    };

    public void TestEnglishShort() {
        checkLocale(ULocale.ENGLISH, CompactStyle.SHORT, EnglishTestData);
    }

    public void TestSerbianShort() {
        checkLocale(ULocale.forLanguageTag("sr"), CompactStyle.SHORT, SerbianTestDataShort);
    }

    public void TestSerbianLong() {
        checkLocale(ULocale.forLanguageTag("sr"), CompactStyle.LONG, SerbianTestDataLong);
    }

    public void TestSwahiliShort() {
        checkLocale(ULocale.forLanguageTag("sw"), CompactStyle.SHORT, SwahiliTestData);
    }

    public void checkLocale(ULocale locale, CompactStyle style, Object[][] testData) {
        CompactDecimalFormat cdf = NumberFormat.getCompactDecimalInstance(locale, style);
        for (Object[] row : testData) {
            assertEquals(locale + " (" + locale.getDisplayName(locale) + ")", row[1], cdf.format(row[0]));
        }
    }
}
