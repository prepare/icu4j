/**
*******************************************************************************
* Copyright (C) 1999-2002, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package com.ibm.icu.dev.test.lang;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.lang.UScriptRun;
import com.ibm.icu.dev.test.TestFmwk;

public class TestUScriptRun extends TestFmwk
{
    public TestUScriptRun()
    {
        // nothing
    }
    
    public static void main(String[] args) throws Exception {
        new TestUScriptRun().run(args);
    }

    private static final class RunTestData
    {
        String runText;
        int    runScript;
        
        public RunTestData(String theText, int theScriptCode)
        {
            runText   = theText;
            runScript = theScriptCode;
        }
    };
    
    private static final RunTestData[] testData = {
        new RunTestData("\u0020\u0946\u0939\u093F\u0928\u094D\u0926\u0940\u0020", UScript.DEVANAGARI),
        new RunTestData("\u0627\u0644\u0639\u0631\u0628\u064A\u0629\u0020", UScript.ARABIC),
        new RunTestData("\u0420\u0443\u0441\u0441\u043A\u0438\u0439\u0020", UScript.CYRILLIC),
        new RunTestData("English (", UScript.LATIN),
        new RunTestData("\u0E44\u0E17\u0E22", UScript.THAI),
        new RunTestData(") ", UScript.LATIN),
        new RunTestData("\u6F22\u5B75", UScript.HAN),
        new RunTestData("\u3068\u3072\u3089\u304C\u306A\u3068", UScript.HIRAGANA),
        new RunTestData("\u30AB\u30BF\u30AB\u30CA", UScript.KATAKANA),
        new RunTestData("\uD801\uDC00\uD801\uDC01\uD801\uDC02\uD801\uDC03", UScript.DESERET)
    };
    
    private void CheckScriptRuns(UScriptRun scriptRun, int[] runStarts, RunTestData[] testData)
    {
        int run, runStart, runLimit;
        int runScript;

        /* iterate over all the runs */
        run = 0;
        while (scriptRun.next()) {
            runStart  = scriptRun.getScriptStart();
            runLimit  = scriptRun.getScriptLimit();
            runScript = scriptRun.getScriptCode();
            
            if (runStart != runStarts[run]) {
                errln("Incorrect start offset for run " + run + ": expected " + runStarts[run] + ", got " + runStart);
            }

            if (runLimit != runStarts[run + 1]) {
                errln("Incorrect limit offset for run " + run + ": expected " + runStarts[run + 1] + ", got " + runLimit);
            }

            if (runScript != testData[run].runScript) {
                errln("Incorrect script for run " + run + ": expected \"" + UScript.getName(testData[run].runScript) + "\", got \"" + UScript.getName(runScript) + "\"");
            }
            
            run += 1;

            /* stop when we've seen all the runs we expect to see */
            if (run >= testData.length) {
                break;
            }
        }

        /* Complain if we didn't see then number of runs we expected */
        if (run != testData.length) {
            errln("Incorrect number of runs: expected " + testData.length + ", got " + run);
        }
    }

    public void TestContstruction()
    {
        UScriptRun scriptRun = null;
        char[] dummy = {'d', 'u', 'm', 'm', 'y'};
        
        try {
            scriptRun = new UScriptRun(null, 0, 100);
            errln("new UScriptRun(null, 0, 100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(null, 100, 0);
            errln("new UScriptRun(null, 100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(null, 0, -100);
            errln("new UScriptRun(null, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(null, -100, 0);
            errln("new UScriptRun(null, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(dummy, 0, 6);
            errln("new UScriptRun(dummy, 0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(dummy, 6, 0);
            errln("new UScriptRun(dummy, 6, 0) did not produce an IllegalArgumentException!");
        }catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(dummy, 0, -100);
            errln("new UScriptRun(dummy, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun = new UScriptRun(dummy, -100, 0);
            errln("new UScriptRun(dummy, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }

    }
    
    public void TestReset()
    {
        UScriptRun scriptRun = null;
        char[] dummy = {'d', 'u', 'm', 'm', 'y'};
        
        try {
            scriptRun = new UScriptRun();
        } catch (IllegalArgumentException iae) {
            errln("new UScriptRun() produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset(0, 100);
            errln("scriptRun.reset(0, 100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(100, 0);
            errln("scriptRun.reset(100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(0, -100);
            errln("scriptRun.reset(0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(-100, 0);
            errln("scriptRun.reset(-100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(dummy, 0, 6);
            errln("scriptRun.reset(dummy, 0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(dummy, 6, 0);
            errln("scriptRun.reset(dummy, 6, 0) did not produce an IllegalArgumentException!");
        }catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(dummy, 0, -100);
            errln("scriptRun.reset(dummy, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(dummy, -100, 0);
            errln("scriptRun.reset(dummy, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(dummy, 0, dummy.length);
        } catch (IllegalArgumentException iae) {
            errln("scriptRun.reset(dummy, 0, dummy.length) produced an IllegalArgumentException!");
        }
        
        
        try {
            scriptRun.reset(0, 6);
            errln("scriptRun.reset(0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
        
        try {
            scriptRun.reset(6, 0);
            errln("scriptRun.reset(6, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
        }
    }
    
    public void TestRuns()
    {
        int stringLimit = 0;
        int[] runStarts = new int[testData.length + 1];
        String testString = "";
        UScriptRun scriptRun = null;
        
        /*
         * Fill in the test string and the runStarts array.
         */
        for (int run = 0; run < testData.length; run += 1) {
            runStarts[run] = stringLimit;
            stringLimit += testData[run].runText.length();
            testString  += testData[run].runText;
        }

        /* The limit of the last run */ 
        runStarts[testData.length] = stringLimit;
        
        try {
            scriptRun = new UScriptRun(testString.toCharArray());
            CheckScriptRuns(scriptRun, runStarts, testData);
        } catch (IllegalArgumentException iae) {
            errln("new UScriptRun(testString.toCharArray()) produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset();
            CheckScriptRuns(scriptRun, runStarts, testData);
        } catch (IllegalArgumentException iae) {
            errln("scriptRun.reset() on a valid UScriptRun produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun = new UScriptRun();
            
            if (scriptRun.next()) {
                errln("scriptRun.next() on an empty UScriptRun returned true!");
            }
        } catch (IllegalArgumentException iae) {
            errln("new UScriptRun() produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset(testString.toCharArray(), 0, testString.length());
            CheckScriptRuns(scriptRun, runStarts, testData);
        } catch (IllegalArgumentException iae) {
            errln("scriptRun.reset(testString.toCharArray(), 0, testString.length) produced an IllegalArgumentException!");
        }
    }
}
