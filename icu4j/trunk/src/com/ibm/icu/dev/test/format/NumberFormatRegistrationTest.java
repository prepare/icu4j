/*
 *******************************************************************************
 * Copyright (C) 2003, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/format/NumberFormatRegistrationTest.java,v $
 * $Date: 2003/06/05 21:15:20 $
 * $Revision: 1.4 $
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.text.*;
import com.ibm.icu.text.NumberFormat.*;

import java.util.Arrays;
import java.util.Locale;

public class NumberFormatRegistrationTest extends com.ibm.icu.dev.test.TestFmwk {

    public static void main(String[] args) {
        new NumberFormatRegistrationTest().run(args);
    }

    public void TestRegistration() {
        final Locale SRC_LOC = Locale.FRANCE;
        final Locale SWAP_LOC = Locale.US;

        class TestFactory extends SimpleNumberFormatFactory {
            NumberFormat currencyStyle;

            TestFactory() {
		this(SRC_LOC, SWAP_LOC);
            }

	    TestFactory(Locale srcLoc, Locale swapLoc) {
		super(srcLoc);
		currencyStyle = NumberFormat.getIntegerInstance(swapLoc);
	    }

            public NumberFormat createFormat(Locale loc, int formatType) {
                if (formatType == FORMAT_CURRENCY) {
                    return currencyStyle;
                }
                return null;
            }
        }

	{
	    // coverage before registration

	    try {
		NumberFormat.unregister(null);
		errln("did not throw exception on null unregister");
	    }
	    catch (Exception e) {
	    }

	    try {
		NumberFormat.registerFactory(null);
		errln("did not throw exception on null register");
	    }
	    catch (Exception e) {
	    }

            try {
                NumberFormat.unregister("");
		errln("unregister of empty string key succeeded");
	    }
            catch (Exception e) {
            }
	}

	Locale fu_FU = new Locale("fu", "FU");
        NumberFormat f0 = NumberFormat.getIntegerInstance(SWAP_LOC);
        NumberFormat f1 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat f2 = NumberFormat.getCurrencyInstance(SRC_LOC);
        Object key = NumberFormat.registerFactory(new TestFactory());
	Object key2 = NumberFormat.registerFactory(new TestFactory(fu_FU, Locale.GERMANY));
	if (!Arrays.asList(NumberFormat.getAvailableLocales()).contains(fu_FU)) {
	    errln("did not list fu_FU");
	}
        NumberFormat f3 = NumberFormat.getCurrencyInstance(SRC_LOC);
        NumberFormat f4 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat.unregister(key); // restore for other tests
        NumberFormat f5 = NumberFormat.getCurrencyInstance(SRC_LOC);

	NumberFormat.unregister(key2);

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

	// coverage
	NumberFormat f6 = NumberFormat.getNumberInstance(fu_FU);
    }
}
