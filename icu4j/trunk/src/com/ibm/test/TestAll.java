/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/test/Attic/TestAll.java,v $ 
 * $Date: 2000/11/18 00:17:43 $ 
 * $Revision: 1.5 $
 *
 *****************************************************************************************
 */
package com.ibm.test;
import com.ibm.test.TestFmwk;
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
        run(new com.ibm.test.bnf.BigNumberFormatTest());
    }
    
    public void TestCompression() throws Exception{
        run(new TestFmwk[] {
            new com.ibm.test.compression.DecompressionTest(),
            new com.ibm.test.compression.ExhaustiveTest()
            });
    }
    
	public void TestNormalizer() throws Exception{
		run(new TestFmwk[] {
            new com.ibm.test.normalizer.BasicTest(),
            new com.ibm.test.normalizer.ExhaustiveTest(),
            new com.ibm.test.normalizer.ConformanceTest(),
            });
	}

	public void TestRuleBasedNumberFormat() throws Exception {
		run(new TestFmwk[] {
            new com.ibm.test.rbnf.RbnfTest(),
            new com.ibm.test.rbnf.RbnfRoundTripTest()
            });
	}

	public void TestRuleBasedBreakIterator() throws Exception {
		run(new TestFmwk[] {
            new com.ibm.test.rbbi.SimpleBITest(),
            new com.ibm.test.rbbi.BreakIteratorTest(),
            new com.ibm.test.rbbi.RBBITest()
            });
	}

	public void TestTranslit() throws Exception {
		run(new TestFmwk[] {
            new com.ibm.test.translit.TransliteratorTest(),
            new com.ibm.test.translit.UnicodeSetTest(),
            new com.ibm.test.translit.UnicodeFilterLogicTest(),
            new com.ibm.test.translit.CompoundTransliteratorTest(),
            new com.ibm.test.translit.UnicodeToHexTransliteratorTest(),
            new com.ibm.test.translit.HexToUnicodeTransliteratorTest()

            });
	}

	public void TestSearch() throws Exception {
		run(new com.ibm.test.search.SearchTest());
	}
	
	public void TestRichEdit() throws Exception {
	    run(new com.ibm.test.richtext.TestAll());
	}

    public void TestCalendar() throws Exception {
	    run(new TestFmwk[] {
            new com.ibm.test.calendar.AstroTest(),
            new com.ibm.test.calendar.CalendarRegression(),
            new com.ibm.test.calendar.CompatibilityTest(),
            new com.ibm.test.calendar.HebrewTest(),
            new com.ibm.test.calendar.IBMCalendarTest(),
            new com.ibm.test.calendar.IslamicTest(),
            new com.ibm.test.calendar.ChineseTest()
            });
    }

    public void TestTimeZone() throws Exception {
	    run(new TestFmwk[] {
            new com.ibm.test.timezone.TimeZoneTest(),
            new com.ibm.test.timezone.TimeZoneRegression(),
            new com.ibm.test.timezone.TimeZoneBoundaryTest()
            });
    }
}
