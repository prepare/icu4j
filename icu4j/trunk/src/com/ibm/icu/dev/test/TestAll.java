/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/TestAll.java,v $ 
 * $Date: 2002/03/14 22:29:08 $ 
 * $Revision: 1.23 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.dev.test;
import com.ibm.icu.dev.test.TestFmwk;
import java.text.*;
import java.util.*;

/**
 * Top level test used to run all other tests as a batch.
 */
 
public class TestAll extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new TestAll().run(args);
    }

    public void TestBigNumberFormat() throws Exception{
        run(new com.ibm.icu.dev.test.format.BigNumberFormatTest());
    }
    
    public void TestCompression() throws Exception{
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.compression.DecompressionTest(),
            new com.ibm.icu.dev.test.compression.ExhaustiveTest()
                });
    }
    
    public void TestNormalizer() throws Exception{
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.normalizer.BasicTest(),
            new com.ibm.icu.dev.test.normalizer.ExhaustiveTest(),
            new com.ibm.icu.dev.test.normalizer.ConformanceTest(),
            new com.ibm.icu.dev.test.normalizer.TestCanonicalIterator(),
        });
    }

    public void TestRuleBasedNumberFormat() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.format.RbnfTest(),
            new com.ibm.icu.dev.test.format.RbnfRoundTripTest()
                });
    }

    public void TestRuleBasedBreakIterator() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.rbbi.SimpleBITest(),
            new com.ibm.icu.dev.test.rbbi.BreakIteratorTest(),
            new com.ibm.icu.dev.test.rbbi.RBBITest(),
            new com.ibm.icu.dev.test.rbbi.RBBIAPITest()
                });
    }

    public void TestTranslit() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.translit.TransliteratorTest(),
            new com.ibm.icu.dev.test.translit.UnicodeSetTest(),
            new com.ibm.icu.dev.test.translit.UnicodeFilterLogicTest(),
            new com.ibm.icu.dev.test.translit.CompoundTransliteratorTest(),
            new com.ibm.icu.dev.test.translit.UnicodeToHexTransliteratorTest(),
            new com.ibm.icu.dev.test.translit.HexToUnicodeTransliteratorTest(),
            new com.ibm.icu.dev.test.translit.JamoTest(),
            new com.ibm.icu.dev.test.translit.ErrorTest(),
            new com.ibm.icu.dev.test.translit.RoundTripTest(),
            new com.ibm.icu.dev.test.translit.ReplaceableTest()
                });
    }

    public void TestSearch() throws Exception {
        run(new com.ibm.icu.dev.test.search.SearchTest());
    }
	
    public void TestArabicShaping() throws Exception {
        run(new com.ibm.icu.dev.test.shaping.ArabicShapingRegTest());
    }

    public void TestCalendar() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.calendar.AstroTest(),
            new com.ibm.icu.dev.test.calendar.CalendarRegression(),
            new com.ibm.icu.dev.test.calendar.CompatibilityTest(),
            new com.ibm.icu.dev.test.calendar.HebrewTest(),
            new com.ibm.icu.dev.test.calendar.IBMCalendarTest(),
            new com.ibm.icu.dev.test.calendar.IslamicTest(),
            new com.ibm.icu.dev.test.calendar.ChineseTest()
                });
    }

    public void TestTimeZone() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.timezone.TimeZoneTest(),
            new com.ibm.icu.dev.test.timezone.TimeZoneRegression(),
            new com.ibm.icu.dev.test.timezone.TimeZoneBoundaryTest()
                });
    }

    public void TestCharacter() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.lang.UCharacterTest(),
            new com.ibm.icu.dev.test.lang.UTF16Test()
                });
    }
    
    public void TestTrie() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.util.TrieTest()
            });
    }
    
    public void TestUScript() throws Exception {
        run( new TestFmwk[] {
            new com.ibm.icu.dev.test.lang.TestUScript(),
        });
    }
    
    public void TestNumberFormat() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.format.IntlTestNumberFormat(),
            new com.ibm.icu.dev.test.format.IntlTestNumberFormatAPI(),
            new com.ibm.icu.dev.test.format.NumberFormatTest(),
            new com.ibm.icu.dev.test.format.NumberFormatRoundTripTest(),
            new com.ibm.icu.dev.test.format.NumberRegression(),
            new com.ibm.icu.dev.test.format.NumberFormatRegressionTest(),
            new com.ibm.icu.dev.test.format.IntlTestDecimalFormatAPI(),
            new com.ibm.icu.dev.test.format.IntlTestDecimalFormatAPIC(),
            new com.ibm.icu.dev.test.format.IntlTestDecimalFormatSymbols(),
            new com.ibm.icu.dev.test.format.IntlTestDecimalFormatSymbolsC()
                });
    }
    
    public void TestDateFormat() throws Exception {
        run(new TestFmwk[] {
            new com.ibm.icu.dev.test.format.DateFormatMiscTests(),
            new com.ibm.icu.dev.test.format.DateFormatRegressionTest(),
            new com.ibm.icu.dev.test.format.DateFormatRoundTripTest(),
            new com.ibm.icu.dev.test.format.DateFormatTest(),
            new com.ibm.icu.dev.test.format.IntlTestDateFormat(),
            new com.ibm.icu.dev.test.format.IntlTestDateFormatAPI(),
            new com.ibm.icu.dev.test.format.IntlTestDateFormatAPIC(),
            new com.ibm.icu.dev.test.format.IntlTestDateFormatSymbols(),
            new com.ibm.icu.dev.test.format.IntlTestSimpleDateFormatAPI(),
            new com.ibm.icu.dev.test.format.DateFormatRegressionTestJ()
                });
    }
}
