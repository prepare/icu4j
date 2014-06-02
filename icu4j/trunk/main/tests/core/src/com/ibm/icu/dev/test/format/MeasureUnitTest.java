/*
 *******************************************************************************
 * Copyright (C) 2013-2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.serializable.SerializableTest;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.text.MeasureFormat.FormatWidth;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.TimeUnit;
import com.ibm.icu.util.TimeUnitAmount;
import com.ibm.icu.util.ULocale;

/**
 * See https://sites.google.com/site/icusite/processes/release/tasks/standards?pli=1
 * for information on how to update with each new release.
 * @author markdavis
 */
public class MeasureUnitTest extends TestFmwk {
    
    private static final String[] DRAFT_VERSIONS = {"52", "53"};
    
    private static final HashSet<String> DRAFT_VERSION_SET = new HashSet<String>();
    
    private static final HashSet<String> TIME_CODES = new HashSet<String>();
    
    private static final String[][] JAVA_VERSIONS = {
        {"G_FORCE", "53"},
        {"DEGREE", "53"},
        {"ARC_MINUTE", "53"},
        {"ARC_SECOND", "53"},
        {"ACRE", "53"},
        {"HECTARE", "53"},
        {"SQUARE_FOOT", "53"},
        {"SQUARE_KILOMETER", "53"},
        {"SQUARE_METER", "53"},
        {"SQUARE_MILE", "53"},
        {"MILLISECOND", "53"},
        {"CENTIMETER", "53"},
        {"FOOT", "53"},
        {"INCH", "53"},
        {"KILOMETER", "53"},
        {"LIGHT_YEAR", "53"},
        {"METER", "53"},
        {"MILE", "53"},
        {"MILLIMETER", "53"},
        {"PICOMETER", "53"},
        {"YARD", "53"},
        {"GRAM", "53"},
        {"KILOGRAM", "53"},
        {"OUNCE", "53"},
        {"POUND", "53"},
        {"HORSEPOWER", "53"},
        {"KILOWATT", "53"},
        {"WATT", "53"},
        {"HECTOPASCAL", "53"},
        {"INCH_HG", "53"},
        {"MILLIBAR", "53"},
        {"KILOMETER_PER_HOUR", "53"},
        {"METER_PER_SECOND", "53"},
        {"MILE_PER_HOUR", "53"},
        {"CELSIUS", "53"},
        {"FAHRENHEIT", "53"},
        {"CUBIC_KILOMETER", "53"},
        {"CUBIC_MILE", "53"},
        {"LITER", "53"},
        {"YEAR", "53"},
        {"MONTH", "53"},
        {"WEEK", "53"},
        {"DAY", "53"},
        {"HOUR", "53"},
        {"MINUTE", "53"},
        {"SECOND", "53"},
    };
    
    private static final HashMap<String, String> JAVA_VERSION_MAP = new HashMap<String, String>();
    
    static {
        TIME_CODES.add("year");
        TIME_CODES.add("month");
        TIME_CODES.add("week");
        TIME_CODES.add("day");
        TIME_CODES.add("hour");
        TIME_CODES.add("minute");
        TIME_CODES.add("second");
        for (String verNum : DRAFT_VERSIONS) {
            DRAFT_VERSION_SET.add(verNum);
        }
        for (String[] funcNameAndVersion : JAVA_VERSIONS) {
            JAVA_VERSION_MAP.put(funcNameAndVersion[0], funcNameAndVersion[1]);
        }
    }
    
    /**
     * @author markdavis
     *
     */
    public static void main(String[] args) {
        //generateConstants(); if (true) return;
        new MeasureUnitTest().run(args);
    }
    
    public void TestCompatible53_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.DEGREE,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.MILLISECOND,
                MeasureUnit.CENTIMETER,
                MeasureUnit.FOOT,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MILE,
                MeasureUnit.MILLIMETER,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.POUND,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.LITER,
                MeasureUnit.YEAR,
                MeasureUnit.MONTH,
                MeasureUnit.WEEK,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MINUTE,
                MeasureUnit.SECOND,
        };
        assertEquals("", 46, units.length);
    }
    
    public void TestExamplesInDocs() {
        MeasureFormat fmtFr = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.SHORT);
        Measure measure = new Measure(23, MeasureUnit.CELSIUS);
        assertEquals("23 °C", "23 °C", fmtFr.format(measure));                
        Measure measureF = new Measure(70, MeasureUnit.FAHRENHEIT);
        assertEquals("70 °F", "70 °F", fmtFr.format(measureF));
        MeasureFormat fmtFrFull = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.WIDE);
        assertEquals(
                "70 pied et 5,3 pouces",
                "70 pieds et 5,3 pouces",
                fmtFrFull.formatMeasures(
                        new Measure(70, MeasureUnit.FOOT),
                        new Measure(5.3, MeasureUnit.INCH)));
        assertEquals(
                "1 pied et 1 pouce",
                "1 pied et 1 pouce",
                fmtFrFull.formatMeasures(
                        new Measure(1, MeasureUnit.FOOT),
                        new Measure(1, MeasureUnit.INCH)));
        MeasureFormat fmtFrNarrow = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.NARROW);
        assertEquals(
                "1′ 1″",
                "1′ 1″",
                fmtFrNarrow.formatMeasures(
                        new Measure(1, MeasureUnit.FOOT),
                        new Measure(1, MeasureUnit.INCH)));
        MeasureFormat fmtEn = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals(
                "1 inch, 2 feet",
                "1 inch, 2 feet",
                fmtEn.formatMeasures(
                        new Measure(1, MeasureUnit.INCH),
                        new Measure(2, MeasureUnit.FOOT)));
    }
    
    public void TestFormatPeriodEn() {
        TimeUnitAmount[] _19m = {new TimeUnitAmount(19.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _1h_23_5s = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(23.5, TimeUnit.SECOND)};
        TimeUnitAmount[] _1h_23_5m = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(23.5, TimeUnit.MINUTE)};
        TimeUnitAmount[] _1h_0m_23s = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(23.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _2y_5M_3w_4d = {
                new TimeUnitAmount(2.0, TimeUnit.YEAR),
                new TimeUnitAmount(5.0, TimeUnit.MONTH),
                new TimeUnitAmount(3.0, TimeUnit.WEEK),
                new TimeUnitAmount(4.0, TimeUnit.DAY)};
        TimeUnitAmount[] _1m_59_9996s = {
                new TimeUnitAmount(1.0, TimeUnit.MINUTE),
                new TimeUnitAmount(59.9996, TimeUnit.SECOND)};
        TimeUnitAmount[] _5h_17m = {
                new TimeUnitAmount(5.0, TimeUnit.HOUR),
                new TimeUnitAmount(17.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _neg5h_17m = {
                new TimeUnitAmount(-5.0, TimeUnit.HOUR),
                new TimeUnitAmount(17.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _19m_28s = {
                new TimeUnitAmount(19.0, TimeUnit.MINUTE),
                new TimeUnitAmount(28.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _0h_0m_9s = {
                new TimeUnitAmount(0.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(9.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _0h_0m_17s = {
                new TimeUnitAmount(0.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(17.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _6h_56_92m = {
                new TimeUnitAmount(6.0, TimeUnit.HOUR),
                new TimeUnitAmount(56.92, TimeUnit.MINUTE)};
        TimeUnitAmount[] _3h_4s_5m = {
                new TimeUnitAmount(3.0, TimeUnit.HOUR),
                new TimeUnitAmount(4.0, TimeUnit.SECOND),
                new TimeUnitAmount(5.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _6_7h_56_92m = {
                new TimeUnitAmount(6.7, TimeUnit.HOUR),
                new TimeUnitAmount(56.92, TimeUnit.MINUTE)};
        TimeUnitAmount[] _3h_5h = {
                new TimeUnitAmount(3.0, TimeUnit.HOUR),
                new TimeUnitAmount(5.0, TimeUnit.HOUR)};
        
        Object[][] fullData = {
                {_1m_59_9996s, "1 minute, 59.9996 seconds"},
                {_19m, "19 minutes"},
                {_1h_23_5s, "1 hour, 23.5 seconds"},
                {_1h_23_5m, "1 hour, 23.5 minutes"},
                {_1h_0m_23s, "1 hour, 0 minutes, 23 seconds"},
                {_2y_5M_3w_4d, "2 years, 5 months, 3 weeks, 4 days"}};
        Object[][] abbrevData = {
                {_1m_59_9996s, "1 min, 59.9996 secs"},
                {_19m, "19 mins"},
                {_1h_23_5s, "1 hr, 23.5 secs"},
                {_1h_23_5m, "1 hr, 23.5 mins"},
                {_1h_0m_23s, "1 hr, 0 mins, 23 secs"},
                {_2y_5M_3w_4d, "2 yrs, 5 mths, 3 wks, 4 days"}};
        Object[][] narrowData = {
                {_1m_59_9996s, "1m 59.9996s"},
                {_19m, "19m"},
                {_1h_23_5s, "1h 23.5s"},
                {_1h_23_5m, "1h 23.5m"},
                {_1h_0m_23s, "1h 0m 23s"},
                {_2y_5M_3w_4d, "2y 5m 3w 4d"}};
        
        
        Object[][] numericData = {
                {_1m_59_9996s, "1:59.9996"},
                {_19m, "19m"},
                {_1h_23_5s, "1:00:23.5"},
                {_1h_0m_23s, "1:00:23"},
                {_1h_23_5m, "1:23.5"},
                {_5h_17m, "5:17"},
                {_neg5h_17m, "-5h 17m"},
                {_19m_28s, "19:28"},
                {_2y_5M_3w_4d, "2y 5m 3w 4d"},
                {_0h_0m_9s, "0:00:09"},
                {_6h_56_92m, "6:56.92"},
                {_6_7h_56_92m, "6:56.92"},
                {_3h_4s_5m, "3h 4s 5m"},
                {_3h_5h, "3h 5h"}};
        Object[][] fullDataDe = {
                {_1m_59_9996s, "1 Minute und 59,9996 Sekunden"},
                {_19m, "19 Minuten"},
                {_1h_23_5s, "1 Stunde und 23,5 Sekunden"},
                {_1h_23_5m, "1 Stunde und 23,5 Minuten"},
                {_1h_0m_23s, "1 Stunde, 0 Minuten und 23 Sekunden"},
                {_2y_5M_3w_4d, "2 Jahre, 5 Monate, 3 Wochen und 4 Tage"}};
        Object[][] numericDataDe = {
                {_1m_59_9996s, "1:59,9996"},
                {_19m, "19 Min."},
                {_1h_23_5s, "1:00:23,5"},
                {_1h_0m_23s, "1:00:23"},
                {_1h_23_5m, "1:23,5"},
                {_5h_17m, "5:17"},
                {_19m_28s, "19:28"},
                {_2y_5M_3w_4d, "2 J, 5 M, 3 W und 4 T"},
                {_0h_0m_17s, "0:00:17"},
                {_6h_56_92m, "6:56,92"},
                {_3h_5h, "3 Std., 5 Std."}};
        
        NumberFormat nf = NumberFormat.getNumberInstance(ULocale.ENGLISH);
        nf.setMaximumFractionDigits(4);
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE, nf);
        verifyFormatPeriod("en FULL", mf, fullData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT, nf);
        verifyFormatPeriod("en SHORT", mf, abbrevData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NARROW, nf);
        verifyFormatPeriod("en NARROW", mf, narrowData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NUMERIC, nf);
        verifyFormatPeriod("en NUMERIC", mf, numericData);
        nf = NumberFormat.getNumberInstance(ULocale.GERMAN);
        nf.setMaximumFractionDigits(4);
        mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.WIDE, nf);
        verifyFormatPeriod("de FULL", mf, fullDataDe);
        mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.NUMERIC, nf);
        verifyFormatPeriod("de NUMERIC", mf, numericDataDe);
    }
    
    private void verifyFormatPeriod(String desc, MeasureFormat mf, Object[][] testData) {
        StringBuilder builder = new StringBuilder();
        boolean failure = false;
        for (Object[] testCase : testData) {
            String actual = mf.format((Measure[]) testCase[0]);
            if (!testCase[1].equals(actual)) {
                builder.append(String.format("%s: Expected: '%s', got: '%s'\n", desc, testCase[1], actual));
                failure = true;
            }
        }
        if (failure) {
            errln(builder.toString());
        }
    }
    
    public void Test10219FractionalPlurals() {
        double[] values = {1.588, 1.011};
        String[][] expected = {
                {"1 minute", "1.5 minutes", "1.58 minutes"},
                {"1 minute", "1.0 minutes", "1.01 minutes"}
        };
        for (int j = 0; j < values.length; j++) {
            for (int i = 0; i < expected[j].length; i++) {
                NumberFormat nf = NumberFormat.getNumberInstance(ULocale.ENGLISH);
                nf.setRoundingMode(BigDecimal.ROUND_DOWN);
                nf.setMinimumFractionDigits(i);
                nf.setMaximumFractionDigits(i);
                MeasureFormat mf = MeasureFormat.getInstance(
                        ULocale.ENGLISH, FormatWidth.WIDE, nf);
                assertEquals("Test10219", expected[j][i], mf.format(new Measure(values[j], MeasureUnit.MINUTE)));
            }
        }   
    }
    
    public void TestGreek() {
        String[] locales = {"el_GR", "el"};
        final MeasureUnit[] units = new MeasureUnit[]{
                MeasureUnit.SECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.HOUR,
                MeasureUnit.DAY,
                MeasureUnit.WEEK,
                MeasureUnit.MONTH,
                MeasureUnit.YEAR};
        FormatWidth[] styles = new FormatWidth[] {FormatWidth.WIDE, FormatWidth.SHORT};
        int[] numbers = new int[] {1, 7};
        String[] expected = {
                "1 δευτερόλεπτο",
                "1 λεπτό",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδομάδα",
                "1 μήνας",
                "1 έτος",
                "1 δευτ.",
                "1 λεπ.",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδ.",
                "1 μήν.",
                "1 έτος",
                "7 δευτερόλεπτα",
                "7 λεπτά",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδομάδες",
                "7 μήνες",
                "7 έτη",
                "7 δευτ.",
                "7 λεπ.",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδ.",
                "7 μήν.",
                "7 έτη",
                "1 δευτερόλεπτο",
                "1 λεπτό",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδομάδα",
                "1 μήνας",
                "1 έτος",
                "1 δευτ.",
                "1 λεπ.",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδ.",
                "1 μήν.",
                "1 έτος",
                "7 δευτερόλεπτα",
                "7 λεπτά",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδομάδες",
                "7 μήνες",
                "7 έτη",
                "7 δευτ.",
                "7 λεπ.",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδ.",
                "7 μήν.",
                "7 έτη"};
        int counter = 0;
        String formatted;
        for ( int locIndex = 0; locIndex < locales.length; ++locIndex ) {
            for( int numIndex = 0; numIndex < numbers.length; ++numIndex ) {
                for ( int styleIndex = 0; styleIndex < styles.length; ++styleIndex ) {
                    for ( int unitIndex = 0; unitIndex < units.length; ++unitIndex ) {
                        Measure m = new Measure(numbers[numIndex], units[unitIndex]);
                        MeasureFormat fmt = MeasureFormat.getInstance(new ULocale(locales[locIndex]), styles[styleIndex]);
                        formatted = fmt.format(m);
                        assertEquals(
                                "locale: " + locales[locIndex]
                                        + ", style: " + styles[styleIndex]
                                                + ", units: " + units[unitIndex]
                                                        + ", value: " + numbers[numIndex],
                                                expected[counter], formatted);
                        ++counter;
                    }
                }
            }
        }
    }

    public void testAUnit() {
        String lastType = null;
        for (MeasureUnit expected : MeasureUnit.getAvailable()) {
            String type = expected.getType();
            String code = expected.getSubtype();
            if (!type.equals(lastType)) {
                logln(type);
                lastType = type;
            }
            MeasureUnit actual = MeasureUnit.internalGetInstance(type, code);
            assertSame("Identity check", expected, actual);
        }
    }
    
    public void testFormatSingleArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 meters", mf.format(new Measure(5, MeasureUnit.METER)));
    }
    
    public void testFormatMeasuresZeroArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "", mf.formatMeasures());
    }
    
    public void testFormatMeasuresOneArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 meters", mf.formatMeasures(new Measure(5, MeasureUnit.METER)));
    }
    
    

    public void testMultiples() {
        ULocale russia = new ULocale("ru");
        Object[][] data = new Object[][] {
                {ULocale.ENGLISH, FormatWidth.WIDE, "2 miles, 1 foot, 2.3 inches"},
                {ULocale.ENGLISH, FormatWidth.SHORT, "2 mi, 1 ft, 2.3 in"},
                {ULocale.ENGLISH, FormatWidth.NARROW, "2mi 1\u2032 2.3\u2033"},
                {russia, FormatWidth.WIDE, "2 \u043C\u0438\u043B\u0438, 1 \u0444\u0443\u0442 \u0438 2,3 \u0434\u044E\u0439\u043C\u0430"},
                {russia, FormatWidth.SHORT, "2 \u043C\u0438\u043B\u0438, 1 \u0444\u0443\u0442, 2,3 \u0434\u044E\u0439\u043C\u0430"},
                {russia, FormatWidth.NARROW, "2 \u043C\u0438\u043B\u044C 1 \u0444\u0443\u0442 2,3 \u0434\u044E\u0439\u043C\u0430"},
        };
        for (Object[] row : data) {
            MeasureFormat mf = MeasureFormat.getInstance(
                    (ULocale) row[0], (FormatWidth) row[1]);
            assertEquals(
                    "testMultiples",
                    row[2],
                    mf.formatMeasures(
                            new Measure(2, MeasureUnit.MILE), 
                            new Measure(1, MeasureUnit.FOOT), 
                            new Measure(2.3, MeasureUnit.INCH)));
        }
    }

    public void testGram() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        assertEquals(
                "testGram",
                "1 g",
                mf.format(new Measure(1, MeasureUnit.GRAM)));
        assertEquals(
                "testGram",
                "1 G",
                mf.format(new Measure(1, MeasureUnit.G_FORCE)));
    }
    
    public void testCurrencies() {
        Measure USD_1 = new Measure(1.0, Currency.getInstance("USD"));
        Measure USD_2 = new Measure(2.0, Currency.getInstance("USD"));
        Measure USD_NEG_1 = new Measure(-1.0, Currency.getInstance("USD"));
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("Wide currency", "-1.00 US dollars", mf.format(USD_NEG_1));
        assertEquals("Wide currency", "1.00 US dollars", mf.format(USD_1));
        assertEquals("Wide currency", "2.00 US dollars", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        assertEquals("short currency", "-USD1.00", mf.format(USD_NEG_1));
        assertEquals("short currency", "USD1.00", mf.format(USD_1));
        assertEquals("short currency", "USD2.00", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NARROW);
        assertEquals("narrow currency", "-$1.00", mf.format(USD_NEG_1));
        assertEquals("narrow currency", "$1.00", mf.format(USD_1));
        assertEquals("narrow currency", "$2.00", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NUMERIC);
        assertEquals("numeric currency", "-$1.00", mf.format(USD_NEG_1));
        assertEquals("numeric currency", "$1.00", mf.format(USD_1));
        assertEquals("numeric currency", "$2.00", mf.format(USD_2));
        
        mf = MeasureFormat.getInstance(ULocale.JAPAN, FormatWidth.WIDE);
        assertEquals("Wide currency", "-1.00 \u7C73\u30C9\u30EB", mf.format(USD_NEG_1));
        assertEquals("Wide currency", "1.00 \u7C73\u30C9\u30EB", mf.format(USD_1));
        assertEquals("Wide currency", "2.00 \u7C73\u30C9\u30EB", mf.format(USD_2));
    }
    
    public void testFieldPosition() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.SHORT);
        FieldPosition pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        fmt.format(new Measure(43.5, MeasureUnit.FOOT), new StringBuffer("123456: "), pos);
        assertEquals("beginIndex", 10, pos.getBeginIndex());
        assertEquals("endIndex", 11, pos.getEndIndex());
        
        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        fmt.format(new Measure(43, MeasureUnit.FOOT), new StringBuffer(), pos);
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 0, pos.getEndIndex());
    }
    
    public void testFieldPositionMultiple() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.SHORT);
        FieldPosition pos = new FieldPosition(NumberFormat.Field.INTEGER);
        String result = fmt.formatMeasures(
                new StringBuilder(),
                pos,
                new Measure(354, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER)).toString();
        assertEquals("result", "354 m, 23 cm", result);
        
        // According to javadocs for {@link Format#format} FieldPosition is set to
        // beginning and end of first such field encountered instead of the last
        // such field encountered.
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 3, pos.getEndIndex());
        
        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(354, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5.4, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 354 m, 23 cm, 5.4 mm", result);
        assertEquals("beginIndex", 23, pos.getBeginIndex());
        assertEquals("endIndex", 24, pos.getEndIndex());
        
        result = fmt.formatMeasures(
                new StringBuilder(),
                pos,
                new Measure(3, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5.4, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "3 m, 23 cm, 5.4 mm", result);
        assertEquals("beginIndex", 13, pos.getBeginIndex());
        assertEquals("endIndex", 14, pos.getEndIndex());
        
        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(3, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 3 m, 23 cm, 5 mm", result);
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 0, pos.getEndIndex());
        
        pos = new FieldPosition(NumberFormat.Field.INTEGER);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(57, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 57 mm", result);
        assertEquals("beginIndex", 8, pos.getBeginIndex());
        assertEquals("endIndex", 10, pos.getEndIndex());
        
    }
    
    public void testOldFormatWithList() {
        List<Measure> measures = new ArrayList<Measure>(2);
        measures.add(new Measure(5, MeasureUnit.ACRE));
        measures.add(new Measure(3000, MeasureUnit.SQUARE_FOOT));
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 acres, 3,000 square feet", fmt.format(measures));
        assertEquals("", "5 acres", fmt.format(measures.subList(0, 1)));
        List<String> badList = new ArrayList<String>();
        badList.add("be");
        badList.add("you");
        try {
            fmt.format(badList);
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expected) {
           // Expected 
        }
    }
    
    public void testOldFormatWithArray() {
        Measure[] measures = new Measure[] {
                new Measure(5, MeasureUnit.ACRE),
                new Measure(3000, MeasureUnit.SQUARE_FOOT),  
        };
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 acres, 3,000 square feet", fmt.format(measures));
    }
    
    public void testOldFormatBadArg() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        try {
            fmt.format("be");
            fail("Expected IllegalArgumentExceptino.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public void testEqHashCode() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.SHORT);
        MeasureFormat mfeq = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.SHORT);
        MeasureFormat mfne = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.WIDE);
        MeasureFormat mfne2 = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        verifyEqualsHashCode(mf, mfeq, mfne);
        verifyEqualsHashCode(mf, mfeq, mfne2);
    }
    
    public void testGetLocale() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.SHORT);
        assertEquals("", ULocale.GERMAN, mf.getLocale(ULocale.VALID_LOCALE));
    }
    
    public void TestSerial() {
        checkStreamingEquality(MeasureUnit.CELSIUS);
        checkStreamingEquality(MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.NARROW));
        checkStreamingEquality(Currency.getInstance("EUR"));
        checkStreamingEquality(MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.SHORT));
        checkStreamingEquality(MeasureFormat.getCurrencyFormat(ULocale.ITALIAN));
    }
    
    public void TestSerialFormatWidthEnum() {
        // FormatWidth enum values must map to the same ordinal values for all time in order for
        // serialization to work.
        assertEquals("FormatWidth.WIDE", 0, FormatWidth.WIDE.ordinal());
        assertEquals("FormatWidth.SHORT", 1, FormatWidth.SHORT.ordinal());
        assertEquals("FormatWidth.NARROW", 2, FormatWidth.NARROW.ordinal());
        assertEquals("FormatWidth.NUMERIC", 3, FormatWidth.NUMERIC.ordinal());
    }
    
    public void testCurrencyFormatStandInForMeasureFormat() {
        MeasureFormat mf = MeasureFormat.getCurrencyFormat(ULocale.ENGLISH);
        assertEquals(
                "70 feet, 5.3 inches",
                "70 feet, 5.3 inches",
                mf.formatMeasures(
                        new Measure(70, MeasureUnit.FOOT),
                        new Measure(5.3, MeasureUnit.INCH)));
        assertEquals("getLocale", ULocale.ENGLISH, mf.getLocale());
        assertEquals("getNumberFormat", ULocale.ENGLISH, mf.getNumberFormat().getLocale(ULocale.VALID_LOCALE));
        assertEquals("getWidth", MeasureFormat.FormatWidth.WIDE, mf.getWidth());
    }
    
    public void testDoubleZero() {
        ULocale en = new ULocale("en");
        NumberFormat nf = NumberFormat.getInstance(en);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        MeasureFormat mf = MeasureFormat.getInstance(en, FormatWidth.WIDE, nf);
        assertEquals(
                "Positive Rounding",
                "4 hours, 23 minutes, 16.00 seconds",
                mf.formatMeasures(
                        new Measure(4.7, MeasureUnit.HOUR),
                        new Measure(23, MeasureUnit.MINUTE),
                        new Measure(16, MeasureUnit.SECOND)));
        assertEquals(
                "Negative Rounding",
                "-4 hours, 23 minutes, 16.00 seconds",
                mf.formatMeasures(
                        new Measure(-4.7, MeasureUnit.HOUR),
                        new Measure(23, MeasureUnit.MINUTE),
                        new Measure(16, MeasureUnit.SECOND)));
        
    }
    
    static void generateCXXHConstants(String thisVersion) {
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.println();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String code = unit.getSubtype();
                String name = toCamelCase(unit);
                String javaName = toJAVAName(unit);
                checkForDup(seen, name, unit);
                System.out.println("    /**");
                System.out.println("     * Returns unit of " + type + ": " + code + ".");
                System.out.println("     * Caller owns returned value and must free it.");
                System.out.println("     * @param status ICU error code.");
                if (isDraft(javaName)) {
                    System.out.println("     * @draft ICU " + getVersion(javaName, thisVersion));
                } else {
                    System.out.println("     * @stable ICU " + getVersion(javaName, thisVersion));
                }
                System.out.println("     */");
                System.out.printf("    static MeasureUnit *create%s(UErrorCode &status);\n\n", name);
            }
        }    
    }
    
    private static void checkForDup(
            Map<String, MeasureUnit> seen, String name, MeasureUnit unit) {
        if (seen.containsKey(name)) {
            throw new RuntimeException("\nCollision!!" + unit + ", " + seen.get(name));
        } else {
            seen.put(name, unit);
        }
    }

    
    
    static void updateJAVAVersions(String thisVersion) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String javaName = toJAVAName(unit);
                checkForDup(seen, javaName, unit);
                if (!JAVA_VERSION_MAP.containsKey(javaName)) {
                    System.out.printf("        {\"%s\", \"%s\"},\n", javaName, thisVersion);
                }
            }
        }    
    }
    
    static TreeMap<String, List<MeasureUnit>> getAllUnits() {
        TreeMap<String, List<MeasureUnit>> allUnits = new TreeMap<String, List<MeasureUnit>>();
        for (String type : MeasureUnit.getAvailableTypes()) {
            ArrayList<MeasureUnit> units = new ArrayList<MeasureUnit>(MeasureUnit.getAvailable(type));
            Collections.sort(
                    units,
                    new Comparator<MeasureUnit>() {

                        public int compare(MeasureUnit o1, MeasureUnit o2) {
                            return o1.getSubtype().compareTo(o2.getSubtype());
                        }
                        
                    });
            allUnits.put(type, units);
        }
        return allUnits;
    }
    
    static void generateCXXConstants() {
        System.out.println("static final MeasureUnit");
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        System.out.println("static const int32_t gOffsets[] = {");
        int index = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            System.out.printf("    %d,\n", index);
            index += entry.getValue().size();
        }
        System.out.printf("    %d\n", index);
        System.out.println("};");
        System.out.println();
        System.out.println("static const int32_t gIndexes[] = {");
        index = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            System.out.printf("    %d,\n", index);
            if (!entry.getKey().equals("currency")) {
                index += entry.getValue().size();
            }
        }
        System.out.printf("    %d\n", index);
        System.out.println("};");
        System.out.println();
        System.out.println("static const char * const gTypes[] = {");
        boolean first = true;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (!first) {
                System.out.println(",");
            }
            System.out.print("    \"" + entry.getKey() + "\"");
            first = false;
        }    
        System.out.println();
        System.out.println("};");
        System.out.println();
        System.out.println("static const char * const gSubTypes[] = {");
        first = true;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            for (MeasureUnit unit : entry.getValue()) {
                if (!first) {
                    System.out.println(",");
                }
                System.out.print("    \"" + unit.getSubtype() + "\"");
                first = false;
            }
        }    
        System.out.println();
        System.out.println("};");
        System.out.println();
        
        int typeIdx = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            int subTypeIdx = 0;
            String type = entry.getKey();
            if (type.equals("currency")) {
                typeIdx++;
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String name = toCamelCase(unit);
                checkForDup(seen, name, unit);
                System.out.printf("MeasureUnit *MeasureUnit::create%s(UErrorCode &status) {\n", name);
                System.out.printf("    return MeasureUnit::create(%d, %d, status);\n", typeIdx, subTypeIdx);
                System.out.println("}");
                System.out.println();
                subTypeIdx++;
            }
            typeIdx++;
        }    
    }

    private static String toCamelCase(MeasureUnit unit) {
        StringBuilder result = new StringBuilder();
        boolean caps = true;
        String code = unit.getSubtype();
        int len = code.length();
        for (int i = 0; i < len; i++) {
            char ch = code.charAt(i);
            if (ch == '-') {
                caps = true;
            } else if (caps) {
                result.append(Character.toUpperCase(ch));
                caps = false;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
    
    static boolean isTypeHidden(String type) {
        return "currency".equals(type);
    }
    
    static void generateBackwardCompatibilityTest(String version) {
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.println();
        System.out.printf("    public void TestCompatible%s() {\n", version.replace(".", "_"));
        System.out.println("        MeasureUnit[] units = {");
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        int count = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (isTypeHidden(entry.getKey())) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String javaName = toJAVAName(unit);
                checkForDup(seen, javaName, unit);
                System.out.printf("                MeasureUnit.%s,\n", javaName);
                count++;
            }
        }
        System.out.println("        };");
        System.out.printf("        assertEquals(\"\",  %d, units.length);\n", count);
        System.out.println("    }");
    }
    
    static void generateCXXBackwardCompatibilityTest(String version) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.printf("void MeasureFormatTest::TestCompatible%s() {\n", version.replace(".", "_"));
        System.out.println("    UErrorCode status = U_ZERO_ERROR;");
        System.out.println("    LocalPointer<MeasureUnit> measureUnit;");
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (isTypeHidden(entry.getKey())) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String camelCase = toCamelCase(unit);
                checkForDup(seen, camelCase, unit);
                System.out.printf("    measureUnit.adoptInstead(MeasureUnit::create%s(status));\n", camelCase);
            }
        }
        System.out.println("    assertSuccess(\"\", status);");
        System.out.println("}");
    }
    
    static String toJAVAName(MeasureUnit unit) {
        String code = unit.getSubtype();
        String type = unit.getType();
        String name = code.toUpperCase(Locale.ENGLISH).replace("-", "_");
        if (type.equals("angle")) {
            if (code.equals("minute") || code.equals("second")) {
                name = "ARC_" + name;
            }
        }
        return name;
    }

    static void generateConstants(String thisVersion) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (isTypeHidden(type)) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String name = toJAVAName(unit);
                String code = unit.getSubtype();
                checkForDup(seen, name, unit);
                System.out.println("    /**");
                System.out.println("     * Constant for unit of " + type +
                        ": " +
                        code);
                // Special case JAVA had old constants for time from before.
                if ("duration".equals(type) && TIME_CODES.contains(code)) {
                    System.out.println("     * @stable ICU 4.0");
                }
                else if (isDraft(name)) {
                    System.out.println("     * @draft ICU " + getVersion(name, thisVersion));
                    System.out.println("     * @provisional This API might change or be removed in a future release.");
                } else {
                    System.out.println("     * @stable ICU " + getVersion(name, thisVersion));
                }
                System.out.println("    */");
                if ("duration".equals(type) && TIME_CODES.contains(code)) {
                    System.out.println("    public static final TimeUnit " + name + " = (TimeUnit) MeasureUnit.internalGetInstance(\"" +
                            type +
                            "\", \"" +
                            code +
                            "\");");
                } else {
                    System.out.println("    public static final MeasureUnit " + name + " = MeasureUnit.internalGetInstance(\"" +
                            type +
                            "\", \"" +
                            code +
                            "\");");
                }
                System.out.println();
            }
        }
    }
    
    private static String getVersion(String javaName, String thisVersion) {
        String version = JAVA_VERSION_MAP.get(javaName);
        if (version == null) {
            return thisVersion;
        }
        return version;
    }

    private static boolean isDraft(String javaName) {
        String version = JAVA_VERSION_MAP.get(javaName);
        if (version == null) {
            return true;
        }
        return DRAFT_VERSION_SET.contains(version);
    }

    public <T extends Serializable> void checkStreamingEquality(T item) {
        try {
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOut);
          objectOutputStream.writeObject(item);
          objectOutputStream.close();
          byte[] contents = byteOut.toByteArray();
          logln("bytes: " + contents.length + "; " + item.getClass() + ": " + showBytes(contents));
          ByteArrayInputStream byteIn = new ByteArrayInputStream(contents);
          ObjectInputStream objectInputStream = new ObjectInputStream(byteIn);
          Object obj = objectInputStream.readObject();
          assertEquals("Streamed Object equals ", item, obj);
        } catch (IOException e) {
          e.printStackTrace();
          assertNull("Test Serialization " + item.getClass(), e);
        } catch (ClassNotFoundException e) {
          assertNull("Test Serialization " + item.getClass(), e);
        }
      }

    /**
     * @param contents
     * @return
     */
    private String showBytes(byte[] contents) {
      StringBuilder b = new StringBuilder('[');
      for (int i = 0; i < contents.length; ++i) {
        int item = contents[i] & 0xFF;
        if (item >= 0x20 && item <= 0x7F) {
          b.append((char) item);
        } else {
          b.append('(').append(Utility.hex(item, 2)).append(')');
        }
      }
      return b.append(']').toString();
    }
    
    private void verifyEqualsHashCode(Object o, Object eq, Object ne) {
        assertEquals("verifyEqualsHashCodeSame", o, o);
        assertEquals("verifyEqualsHashCodeEq", o, eq);
        assertNotEquals("verifyEqualsHashCodeNe", o, ne);
        assertNotEquals("verifyEqualsHashCodeEqTrans", eq, ne);
        assertEquals("verifyEqualsHashCodeHashEq", o.hashCode(), eq.hashCode());
        
        // May be a flaky test, but generally should be true.
        // May need to comment this out later.
        assertNotEquals("verifyEqualsHashCodeHashNe", o.hashCode(), ne.hashCode());
    }
    
    public static class MeasureUnitHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            MeasureUnit items[] = {
                    MeasureUnit.CELSIUS,
                    Currency.getInstance("EUR")               
            };
            return items;
        }
        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureUnit a1 = (MeasureUnit) a;
            MeasureUnit b1 = (MeasureUnit) b;
            return a1.getType().equals(b1.getType())
                    && a1.getSubtype().equals(b1.getSubtype());
        }
    }
   
    public static class MeasureFormatHandler  implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            MeasureFormat items[] = {
                    MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.SHORT),
                    MeasureFormat.getInstance(
                            ULocale.FRANCE,
                            FormatWidth.WIDE,
                            NumberFormat.getIntegerInstance(ULocale.CANADA_FRENCH)),
            };
            return items;
        }
        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureFormat a1 = (MeasureFormat) a;
            MeasureFormat b1 = (MeasureFormat) b;
            return a1.getLocale().equals(b1.getLocale())
                    && a1.getWidth().equals(b1.getWidth())
                    && a1.getNumberFormat().equals(b1.getNumberFormat())
                    ;
        }
    }
}
