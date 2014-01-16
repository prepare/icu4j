/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

public class TestSuite {

    public static void main(String[] args) {
        int cntError = 0;
        cntError += ExtensibilityTest.main(args);
        cntError += MethodsTest.main(args);
        cntError += FullToLeanTest.main(args);
        cntError += ExtensionsTest.main(args);
        cntError += MathTest.main(args);
        cntError += SomeMoreTest.main(args);
        cntError += ProcessorTest.main(args);
        cntError += StringRecordTest.main(args);
        if (cntError > 0)
            System.out.println("Found " + cntError + " errors");
    }
}