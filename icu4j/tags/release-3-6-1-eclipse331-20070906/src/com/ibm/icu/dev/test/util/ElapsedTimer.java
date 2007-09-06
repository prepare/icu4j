//
//  ElapsedTimer.java
//
//  Created by Steven R. Loomis on 11/11/2005.
//  Copyright 2005-2006 IBM. All rights reserved.
//

package com.ibm.icu.dev.test.util;

import java.util.*;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.MessageFormat;


/**
 * Simple stopwatch timer.
 * Usage:   { ElapsedTimer et = new ElapsedTimer(); 
 *            do_some_stuff;  
 *            System.out.println("It took " + et + " to do stuff."); }
 *
 * Advanced:   { ElapsedTimer et = new ElapsedTimer("Thing2's time: {0}");  // messageformat pattern
 *            do_thing_2();  
 *            System.out.println(et.toString()); }
 *
 * More advanced:  NumberFormat and/or MessageFormat can be provided in the constructor
 */
public final class ElapsedTimer {

    /**
     * Convenience method to print the elasped time (in milliseconds) 
     */
    public static String elapsedTime(long start, long end) {
        return diffTime(getFormat(), start, end);
    }

    public static String elapsedTime(long start) {
        return diffTime(getFormat(), start, System.currentTimeMillis());
    }
    
    // class

    private long startTime = System.currentTimeMillis();
    private NumberFormat myDurationFormat = null;
    private MessageFormat myMsgFormat = null;

    public ElapsedTimer() {            
    }
    
    public ElapsedTimer(MessageFormat aMsgFmt) {
        myMsgFormat = aMsgFmt;
    }

    public ElapsedTimer(NumberFormat aNumFmt) {
        myDurationFormat = aNumFmt;
    }
    
    public ElapsedTimer(MessageFormat aMsgFmt, NumberFormat aNumFmt) {
        myMsgFormat = aMsgFmt;
        myDurationFormat = aNumFmt;
    }

    public ElapsedTimer(String pattern) {
        myMsgFormat = new MessageFormat(pattern);
    }

    public ElapsedTimer(String pattern, NumberFormat aNumFmt) {
        myMsgFormat = new MessageFormat(pattern);
        myDurationFormat = aNumFmt;
    }

    /**
     * @return elapsed time in seconds since object creation
     */
    public final String toString() { 
        long endTime = System.currentTimeMillis();
        String duration = diffTime(myDurationFormat, startTime, endTime);
        if(myMsgFormat == null) {
            return duration;
        } else {
            return myMsgFormat.format(new Object[] {duration});
        }
    }

    private static NumberFormat gFormat = null;
    
    private static NumberFormat getFormat() {
        if(gFormat == null) {
            gFormat = new RuleBasedNumberFormat(Locale.US,
                        RuleBasedNumberFormat.DURATION);
        }
        return gFormat; 
    }
    
    private static String diffTime(NumberFormat fmt, long start, long end) {
        if(fmt==null) {
            fmt = getFormat();
        }
        synchronized(fmt) {
            long age = end - start;
            double diff = (double)age;
            diff = diff/(double)1000.0; // millis per second
            return fmt.format(diff);
        }
    }    
}
