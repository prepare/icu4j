/*
**********************************************************************
* Copyright (c) 2003-2004, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: March 8 2003
* Since: ICU 2.6
**********************************************************************
*/
package com.ibm.icu.dev.test.util;
import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.Utility;

/**
 * @test
 * @summary Test of internal Utility class
 */
public class UtilityTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new UtilityTest().run(args);
    }

    public void TestUnescape() {
        final String input =
            "Sch\\u00f6nes Auto: \\u20ac 11240.\\fPrivates Zeichen: \\U00102345\\e\\cC\\n \\x1b\\x{263a}";

        final String expect = 
            "Sch\u00F6nes Auto: \u20AC 11240.\u000CPrivates Zeichen: \uDBC8\uDF45\u001B\u0003\012 \u001B\u263A";

        String result = Utility.unescape(input);
        if (!result.equals(expect)) {
            errln("FAIL: Utility.unescape() returned " + result + ", exp. " + expect);
        }
    }
    
    public void TestFormat()
    {
        String data[] = {
            "the quick brown fox jumps over the lazy dog",
            // result of this conversion will exceed the original length and
            // cause a newline to be inserted
            "testing space , quotations \"",
            "testing weird supplementary characters \ud800\udc00",
            "testing control characters \u0001 and line breaking!! \n are we done yet?"
        };
        String result[] = {
            "        \"the quick brown fox jumps over the lazy dog\"",
            "        \"testing space , quotations \\042\"",
            "        \"testing weird supplementary characters \\uD800\\uDC00\"",
            "        \"testing control characters \\001 and line breaking!! \\n are we done ye\"+"
                     + Utility.LINE_SEPARATOR + "        \"t?\""
        };
        String result1[] = {
            "\"the quick brown fox jumps over the lazy dog\"",
            "\"testing space , quotations \\042\"",
            "\"testing weird supplementary characters \\uD800\\uDC00\"",
            "\"testing control characters \\001 and line breaking!! \\n are we done yet?\""
        };
        
        for (int i = 0; i < data.length; i ++) {
            assertEquals("formatForSource(\"" + data[i] + "\")",
                         result[i], Utility.formatForSource(data[i]));
        }
        for (int i = 0; i < data.length; i ++) {
            assertEquals("format1ForSource(\"" + data[i] + "\")",
                         result1[i], Utility.format1ForSource(data[i]));
        }
    }
    
    public void TestHighBit()
    {
        int data[] = {-1, -1276, 0, 0xFFFF, 0x1234};
        byte result[] = {-1, -1, -1, 15, 12};
        for (int i = 0; i < data.length; i ++) {
            if (Utility.highBit(data[i]) != result[i]) {
                errln("Fail: Highest bit of \\u" 
                      + Integer.toHexString(data[i]) + " should be "
                      + result[i]);
            }
        }
    }
    
    public void TestCompareUnsigned()
    {
        int data[] = {0, 1, 0x8fffffff, -1, Integer.MAX_VALUE, 
                      Integer.MIN_VALUE, 2342423, -2342423};
        for (int i = 0; i < data.length; i ++) {
            for (int j = 0; j < data.length; j ++) {
                if (Utility.compareUnsigned(data[i], data[j]) 
                    != compareLongUnsigned(data[i], data[j])) {
                    errln("Fail: Unsigned comparison failed with " + data[i] 
                          + " " + data[i + 1]);
                }
            }
        }
    }
    
    private int compareLongUnsigned(int x, int y)
    {
        long x1 = x & 0xFFFFFFFFl;
        long y1 = y & 0xFFFFFFFFl;
        if (x1 < y1) {
            return -1;
        }
        else if (x1 > y1) {
            return 1;
        }
        return 0;
    }
}
