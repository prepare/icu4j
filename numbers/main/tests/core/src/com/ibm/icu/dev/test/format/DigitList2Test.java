/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DigitBuilder;
import com.ibm.icu.text.DigitList2;

/**
 * @author markdavis
 */
public class DigitList2Test extends TestFmwk {
    public static void main(String[] args) {
        new DigitList2Test().run(args);
    }

    public void TestASimple() {
        /*
         * 0 : digits=<>, integerDigits=1 003.40 : digits=<34>, decimalAt=1, integerDigits=3, fractionDigits=2 00340.00
         * : digits=<34>, decimalAt=3, integerDigits=5, fractionDigits=2 00.0340 : digits=<34>, decimalAt=-1,
         * integerDigits=2, fractionDigits=4
         */
        DigitList2 dl = new DigitList2();
        assertEquals("", "", dl.toString());
        dl.setTotalDigits(1);
        assertEquals("", ".0", dl.toString());
        dl.setIntegerDigits(1);
        assertEquals("", "0", dl.toString());

        dl.clear();
        dl.setTotalDigits(6);
        dl.addDigits(3, 0, 4);
        
        checkDigitList("3.04000", dl, 1, 0);
        checkDigitList("0.30400", dl, 1, 1);
        checkDigitList("0.03040", dl, 1, 2);
        checkDigitList("0.00304", dl, 1, 3);
        
        checkDigitList("304.000", dl, 3, 0);
        checkDigitList("030.400", dl, 3, 1);
        checkDigitList("003.040", dl, 3, 2);
        checkDigitList("000.304", dl, 3, 3);
    }

    private void checkDigitList(String expected, DigitList2 dl, int integerDigits, int startDigits) {
        dl.setIntegerDigits(integerDigits);
        dl.setStartDigits(startDigits);
        dl.checkInvariants();
        assertEquals("toString", expected, dl.toString());
        DigitList2 dl2 = dl.clone();
        assertEquals("set", expected, dl2.set(expected).toString());
    }
    
    public void TestBuilder() {
        DigitBuilder db = new DigitBuilder((DecimalFormat)DecimalFormat.getInstance());
        DigitList2 dl = new DigitList2();
        assertEquals("", "0", db.set(0,dl).toString());
        dl.set("1");
        dl.checkInvariants();
        assertEquals("", "1", db.set(1,dl)
                .toString());
        assertEquals("", "150", db.set(150,dl).toString());
        assertEquals("", "2147483647", db.set(Integer.MAX_VALUE,dl).toString());
        assertEquals("", "-2147483648", db.set(Integer.MIN_VALUE,dl).toString());
    }

    public void TestSet() {
        DigitList2 dl = new DigitList2();
        assertEquals("", "10", dl.set("10").toString());
        assertEquals("", "0", dl.set("0").toString());
        assertEquals("", "00", dl.set("00").toString());
        assertEquals("", "1", dl.set("1").toString());
        assertEquals("", "01", dl.set("01").toString());
        assertEquals("", ".1", dl.set(".1").toString());
        assertEquals("", ".10", dl.set(".10").toString());
        assertEquals("", "010.010", dl.set("010.010").toString());
        assertEquals("", String.valueOf(Integer.MAX_VALUE), dl.set(String.valueOf(Integer.MAX_VALUE)).toString());
        assertEquals("", String.valueOf(Integer.MIN_VALUE), dl.set(String.valueOf(Integer.MIN_VALUE)).toString());
    }
}
