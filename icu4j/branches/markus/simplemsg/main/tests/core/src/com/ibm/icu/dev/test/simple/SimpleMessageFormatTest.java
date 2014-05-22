/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.dev.test.simple;

import java.util.Locale;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.simple.MessageFormat;

public class SimpleMessageFormatTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new SimpleMessageFormatTest().run(args);
    }

    public void TestBasic() {
        assertEquals(
                "one simple argument",
                "Going to Germany and back",
                MessageFormat.formatMessageNamedArgs(
                        Locale.US, "Going to {place} and back", "place", "Germany"));
    }
}
