//##header J2SE15
/*
 *******************************************************************************
 * Copyright (C) 1996-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk.TestGroup;

/**
 * Top level test used to run all other tests as a batch.
 */

public class TestAll extends TestGroup {

    public static void main(String[] args) {
        new TestAll().run(args);
    }

    public TestAll() {
        super(new String[] {
                  "TestAll$RBNF",
                  "TestAll$NumberFormat",
                  "TestAll$DateFormat",
                  "TestAll$MessageFormat",
                  "TestAll$PluralFormat",
                  "com.ibm.icu.dev.test.format.BigNumberFormatTest",
                  "com.ibm.icu.dev.test.format.GlobalizationPreferencesTest",
                  "DataDrivenFormatTest"
              },
              "Formatting Tests");
    }

    public static class RBNF extends TestGroup {
        public RBNF() {
            super(new String[] {
                "RbnfTest",
                "RbnfRoundTripTest",
        "RBNFParseTest",
            });
        }
    }

    public static class NumberFormat extends TestGroup {
        public NumberFormat() {
            super(new String[] {
                "IntlTestNumberFormat",
                "IntlTestNumberFormatAPI",
                "NumberFormatTest",
                "NumberFormatRegistrationTest",
                "NumberFormatRoundTripTest",
                "NumberRegression",
                "NumberFormatRegressionTest",
                "IntlTestDecimalFormatAPI",
                "IntlTestDecimalFormatAPIC",
                "IntlTestDecimalFormatSymbols",
                "IntlTestDecimalFormatSymbolsC",
            });
        }
    }

    public static class DateFormat extends TestGroup {
        public DateFormat() {
            super(new String[] {
                "DateFormatMiscTests",
                "DateFormatRegressionTest",
                "DateFormatRoundTripTest",
                "DateFormatTest",
                "IntlTestDateFormat",
                "IntlTestDateFormatAPI",
                "IntlTestDateFormatAPIC",
                "IntlTestDateFormatSymbols",
//#if defined(FOUNDATION10) || defined(J2SE13)
//#else
                "DateTimeGeneratorTest",
//#endif
                "IntlTestSimpleDateFormatAPI",
                "DateFormatRegressionTestJ",
            });
        }
    }
    
    public static class PluralFormat extends TestGroup {
        public PluralFormat() {
            super(new String[] {
                "PluralFormatUnitTest",
                "PluralFormatTest",
                "PluralRulesTest",
            });
        }
    }

    public static class MessageFormat extends TestGroup {
        public MessageFormat() {
            super(new String[] {
                "TestMessageFormat",
                "MessageRegression",
            });
        }
    }

    public static final String CLASS_TARGET_NAME = "Format";
}

