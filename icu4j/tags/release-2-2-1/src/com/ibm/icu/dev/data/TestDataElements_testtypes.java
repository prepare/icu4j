/**
 *******************************************************************************
 * Copyright (C) 2001-2002, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/data/TestDataElements_testtypes.java,v $
 * $Date: 2002/08/13 21:47:30 $
 * $Revision: 1.2 $
 *
 *******************************************************************************
 */
package com.ibm.icu.dev.data;

import java.util.ListResourceBundle;
import com.ibm.icu.impl.ICUListResourceBundle;

public class TestDataElements_testtypes extends ICUListResourceBundle {

    public TestDataElements_testtypes  () {
          super.contents = data;
    }
    private static Object[][] data = new Object[][] { 
                {
                    "binarytest",
                    new ICUListResourceBundle.CompressedBinary(
                        "\u0000\u000F\u0001\u0203\u0405\u0607\u0809\u0A0B" +
                        "\u0C0D\u0E00"),
                },
                {
                    "emptyarray",
                    new String[] { 
                    },
                },
                {
                    "emptybin",
                    new ICUListResourceBundle.CompressedBinary(null),
                },
                {
                    "emptyexplicitstring",
                    "",
                },
                {
                    "emptyint",
                    new Integer(0),
                },
                {
                    "emptyintv",
                    new Integer[] {
                    },
                },
                {
                    "emptystring",
                    "",
                },
                {
                    "emptytable",
                    new Object[][]{
                    },
                },
                {
                    "importtest",
                    new ICUListResourceBundle.CompressedBinary(
                        "\u0000\u000F\u0001\u0203\u0405\u0607\u0809\u0A0B" +
                        "\u0C0D\u0E00"),
                },
                {
                    "integerarray",
                    new Integer[] {
                        new Integer(1),
                        new Integer(2),
                        new Integer(3),
                        new Integer(-3),
                        new Integer(4),
                        new Integer(5),
                        new Integer(6),
                        new Integer(7),
                    },
                },
                {
                    "menu",
                    new Object[][]{
                        {
                            "file",
                            new Object[][]{
                                {
                                    "exit",
                                    "Exit",
                                },
                                {
                                    "open",
                                    "Open",
                                },
                                {
                                    "save",
                                    "Save",
                                },
                            },
                        },
                    },
                },
                {
                    "minusone",
                    new Integer(-1),
                },
                {
                    "one",
                    new Integer(1),
                },
                {
                    "onehundredtwentythree",
                    new Integer(123),
                },
                {
                    "plusone",
                    new Integer(1),
                },
                {
                    "string",
                    new String[] { 
                    },
                },
                {
                    "stringTable",
                    new Object[]{
                        new String[] { 
                        },

                    },
                },
                {
                    "test_underscores",
                    "test message ....",
                },
                {
                    "testescape",
                    "tab:\u0009 cr:\f ff:\u000C newline:\n backslash:\\" +
                    " quote=\\\' doubleQuote=\\\" singlequoutes=''",
                },
                {
                    "zerotest",
                    "abc\u0000def",
                },
    };
}
