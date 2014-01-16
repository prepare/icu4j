/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.impl.stt.StringRecord;
import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Tests the StringRecord class
 */
public class StringRecordTest extends TestBase {
    public static int main(String[] args) {
        int cntError = 0;
        StringRecord sr;
        boolean catchFlag;
        // check handling of invalid arguments
        catchFlag = false;
        try {
            sr = StringRecord
                    .addRecord(null, 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 0, 1);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch null string argument", catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 1, null, 0, 1);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch null handler argument", catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 0, BidiStructuredProcessor.StructuredTypes.EMAIL, 0,
                    1);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch invalid segment count argument",
                catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 1, BidiStructuredProcessor.StructuredTypes.EMAIL, -1,
                    1);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch invalid start argument", catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 4,
                    1);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch invalid start argument", catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 0,
                    0);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch invalid limit argument", catchFlag);
        catchFlag = false;
        try {
            sr = StringRecord.addRecord("abc", 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 0,
                    5);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch invalid limit argument", catchFlag);

        int poolSize = StringRecord.POOLSIZE;
        int lim = poolSize / 2;
        sr = StringRecord.getRecord("XXX");
        cntError += assertNull(sr);
        for (int i = 0; i < lim; i++) {
            String str = Integer.toString(i);
            sr = StringRecord.addRecord(str, 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 0, 1);
        }
        sr = StringRecord.getRecord(null);
        cntError += assertNull(sr);
        sr = StringRecord.getRecord("");
        cntError += assertNull(sr);

        for (int i = 0; i < poolSize; i++) {
            String str = Integer.toString(i);
            sr = StringRecord.getRecord(str);
            if (i < lim)
                cntError += assertFalse("", null == sr);
            else
                cntError += assertTrue("", null == sr);
        }

        for (int i = lim; i <= poolSize; i++) {
            String str = Integer.toString(i);
            sr = StringRecord.addRecord(str, 1, BidiStructuredProcessor.StructuredTypes.EMAIL, 0, 1);
        }
        for (int i = 1; i <= poolSize; i++) {
            String str = Integer.toString(i);
            sr = StringRecord.getRecord(str);
            cntError += assertNotNull(sr);
        }
        sr = StringRecord.getRecord("0");
        cntError += assertNull(sr);
        sr = StringRecord.addRecord("thisisalongstring", 3,
                BidiStructuredProcessor.StructuredTypes.EMAIL, 0, 2);
        sr.addSegment(BidiStructuredProcessor.StructuredTypes.JAVA, 4, 5);
        sr.addSegment(BidiStructuredProcessor.StructuredTypes.FILE, 6, 7);
        catchFlag = false;
        try {
            sr.addSegment(BidiStructuredProcessor.StructuredTypes.EMAIL, 10, 13);
        } catch (IllegalStateException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch too many segments", catchFlag);
        cntError += assertEquals("", 3, sr.getSegmentCount());
        cntError += assertEquals("", BidiStructuredProcessor.StructuredTypes.EMAIL, sr.getHandler(0));
        cntError += assertEquals("", BidiStructuredProcessor.StructuredTypes.JAVA, sr.getHandler(1));
        cntError += assertEquals("", BidiStructuredProcessor.StructuredTypes.FILE, sr.getHandler(2));
        cntError += assertEquals("", 0, sr.getStart(0));
        cntError += assertEquals("", 4, sr.getStart(1));
        cntError += assertEquals("", 6, sr.getStart(2));
        cntError += assertEquals("", 2, sr.getLimit(0));
        cntError += assertEquals("", 5, sr.getLimit(1));
        cntError += assertEquals("", 7, sr.getLimit(2));
        catchFlag = false;
        try {
            sr.getLimit(3);
        } catch (IllegalArgumentException e) {
            catchFlag = true;
        }
        cntError += assertTrue("Catch segment number too large", catchFlag);

        StringRecord.clear();
        for (int i = 0; i <= poolSize; i++) {
            String str = Integer.toString(i);
            sr = StringRecord.getRecord(str);
            cntError += assertNull(sr);
        }
        return cntError;
    }
}
