/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/compression/TestAll.java,v $
 * $Date: 2003/02/05 05:45:16 $
 * $Revision: 1.2 $
 *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.compression;

import com.ibm.icu.dev.test.TestFmwk.TestGroup;

/**
 * Top level test used to run all other tests as a batch.
 */
public class TestAll extends TestGroup {
    public static void main(String[] args) throws Exception {
        new TestAll().run(args);
    }

    public TestAll() {
        super(new String[] {
            "DecompressionTest",
            "ExhaustiveTest",
        });
    }

    public static final String CLASS_TARGET_NAME = "Compression";
}
