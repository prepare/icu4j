/*
 **************************************************************************
 * Copyright (C) 2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                           *
 **************************************************************************
 *
 */

package com.ibm.icu.dev.test.timescale;

import com.ibm.icu.util.UniversalTimeScale;
import com.ibm.icu.dev.test.TestFmwk;

/**
 * @author Owner
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TimeScaleDataTest extends TestFmwk {

    /**
     * Default contstructor.
     */
    public TimeScaleDataTest()
    {
    }
    
    private void roundTripTest(long value, int scale)
    {
        long rt = UniversalTimeScale.toLong(UniversalTimeScale.from(value, scale), scale);
        
        if (rt != value) {
            errln("Round-trip error: time scale = " + scale + ", value = " + value + ", round-trip = " + rt);
        }
    }
    
    private void toLimitTest(long toLimit, long fromLimit, int scale)
    {
        long result = UniversalTimeScale.toLong(toLimit, scale);
        
        if (result != fromLimit) {
            errln("toLimit failure: scale = " + scale + ", toLimit = " + toLimit +
                  ", toLong(toLimit, scale) = " + result + ", fromLimit = " + fromLimit);
        }
    }
    
    private void epochOffsetTest(long epochOffset, long units, int scale)
    {
        long universalEpoch = epochOffset * units;
        long local = UniversalTimeScale.toLong(universalEpoch, scale);
        
        if (local != 0) {
            errln("toLong(epochOffset, scale): scale = " + scale + ", epochOffset = " + universalEpoch +
                  ", result = " + local);
        }
        
        local = UniversalTimeScale.toLong(0, scale);
        
        if (local != -epochOffset) {
            errln("toLong(0, scale): scale = " + scale + ", result = " + local);
        }
        
        long universal = UniversalTimeScale.from(-epochOffset, scale);
        
        if (universal != 0) {
            errln("from(-epochOffest, scale): scale = " + scale + ", epochOffset = " + epochOffset +
                  ", result = " + universal);
        }
        
        universal = UniversalTimeScale.from(0, scale);
        
        if (universal != universalEpoch) {
            errln("from(0, scale): scale = " + scale + ", result = " + universal);
        }
    }
    
    public void TestEpochOffsets()
    {
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            UniversalTimeScale.TimeScaleData data = UniversalTimeScale.getTimeScaleData(scale);
            
            epochOffsetTest(data.epochOffset, data.units, scale);
        }
    }

    public void TestFromLimits()
    {
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            UniversalTimeScale.TimeScaleData data = UniversalTimeScale.getTimeScaleData(scale);
            
            roundTripTest(data.fromMin, scale);
            roundTripTest(data.fromMax, scale);
        }
    }
    
    public void TestToLimits()
    {
        for (int scale = 0; scale < UniversalTimeScale.MAX_SCALE; scale += 1) {
            UniversalTimeScale.TimeScaleData data = UniversalTimeScale.getTimeScaleData(scale);
            
            toLimitTest(data.toMin, data.fromMin, scale);
            toLimitTest(data.toMax, data.fromMax, scale);
       }
    }

    public static void main(String[] args)
    {
        new TimeScaleDataTest().run(args);
    }
}
