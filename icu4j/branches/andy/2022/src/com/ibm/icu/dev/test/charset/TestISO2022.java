/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.MissingResourceException;

import com.ibm.icu.charset.CharsetEncoderICU;
import com.ibm.icu.charset.CharsetICU;
import com.ibm.icu.charset.CharsetProviderICU;
import com.ibm.icu.dev.test.TestFmwk;

/**
 * @author andy
 *
 */
public class TestISO2022 extends TestFmwk {
    
    public static void main(String[] args) throws Exception {
        new TestISO2022().run(args);
    }

    private Charset createCharset2022() throws MissingResourceException {
        Charset charset = null;
        try {
        CharsetProviderICU provider = new CharsetProviderICU();
        charset = provider.charsetForName("ISO-2022-JP");

        if (charset==null) {
            errln("TestISO2022.createCharset2022, provider.charsetForName(ISO-2022-JP) returned null");
        }
        }
        catch (MissingResourceException e) {
            errln("TestISO2022.createCharset2022, unexpected exception: " + e.toString());
            throw e;
        }
        return charset;
    }
    
    public void TestCreation() {
        Charset aCharset = createCharset2022();
        CharsetDecoder decoder = aCharset.newDecoder();
        CharsetEncoder encoder = aCharset.newEncoder();
    }


}
