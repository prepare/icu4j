/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/calendar/CalendarTest.java,v $ 
 * $Date: 2000/11/18 00:17:58 $ 
 * $Revision: 1.7 $
 *
 *****************************************************************************************
 */

package com.ibm.test.calendar;

import com.ibm.test.*;
import com.ibm.text.DateFormat;
import com.ibm.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Hashtable;
import java.util.Enumeration;
import com.ibm.util.*;

/**
 * A base class for classes that test individual Calendar subclasses.
 * Defines various useful utility methods and constants
 */
public class CalendarTest extends TestFmwk {
    
    // Constants for use by subclasses, solely to save typing
    public final static int SUN = Calendar.SUNDAY;
    public final static int MON = Calendar.MONDAY;
    public final static int TUE = Calendar.TUESDAY;
    public final static int WED = Calendar.WEDNESDAY;
    public final static int THU = Calendar.THURSDAY;
    public final static int FRI = Calendar.FRIDAY;
    public final static int SAT = Calendar.SATURDAY;

    public final static int ERA     = Calendar.ERA;
    public final static int YEAR    = Calendar.YEAR;
    public final static int MONTH   = Calendar.MONTH;
    public final static int DATE    = Calendar.DATE;
    public final static int HOUR    = Calendar.HOUR;
    public final static int MINUTE  = Calendar.MINUTE;
    public final static int SECOND  = Calendar.SECOND;
    public final static int DOY     = Calendar.DAY_OF_YEAR;
    public final static int WOY     = Calendar.WEEK_OF_YEAR;
    public final static int WOM     = Calendar.WEEK_OF_MONTH;
    public final static int DOW     = Calendar.DAY_OF_WEEK;
    public final static int DOWM    = Calendar.DAY_OF_WEEK_IN_MONTH;
    
    public final static SimpleTimeZone UTC = new SimpleTimeZone(0, "GMT");

    private static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
        "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
        "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
        "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
        "JULIAN_DAY", "MILLISECONDS_IN_DAY",
    };

    public static final String fieldName(int f) {
        return (f>=0 && f<FIELD_NAME.length) ?
            FIELD_NAME[f] : ("<Field " + f + ">");
    }

    /**
     * Iterates through a list of calendar <code>TestCase</code> objects and
     * makes sure that the time-to-fields and fields-to-time calculations work
     * correnctly for the values in each test case.
     */
    public void doTestCases(TestCase[] cases, Calendar cal)
    {
        cal.setTimeZone(UTC);
        
        // Get a format to use for printing dates in the calendar system we're testing
        DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.SHORT, -1, Locale.getDefault());
        
        // TODO Fix this to include the ERA ("G") again once ChineseDateFormat
        // is implemented - Liu
        final String pattern = "E, MM/dd/yyyy HH:mm:ss.S z";
    
        ((SimpleDateFormat)format).applyPattern(pattern);

        // This format is used for printing Gregorian dates.
        DateFormat gregFormat = new SimpleDateFormat(pattern);
        gregFormat.setTimeZone(UTC);

        GregorianCalendar pureGreg = new GregorianCalendar(UTC);
        pureGreg.setGregorianChange(Calendar.MIN_DATE);
        DateFormat pureGregFmt = new SimpleDateFormat("E M/d/yyyy G");
        pureGregFmt.setCalendar(pureGreg);
        
        // Now iterate through the test cases and see what happens
        for (int i = 0; i < cases.length; i++)
        {
            TestCase test = cases[i];
            
            //
            // First we want to make sure that the millis -> fields calculation works
            // test.applyTime will call setTime() on the calendar object, and
            // test.fieldsEqual will retrieve all of the field values and make sure
            // that they're the same as the ones in the testcase
            //
            test.applyTime(cal);
            if (!test.fieldsEqual(cal, this)) {
                errln("Fail: (millis=>fields) " +
                      gregFormat.format(test.getTime()) + " => " +
                      format.format(cal.getTime()) +
                      ", expected " + test);
            }

            //
            // If that was OK, check the fields -> millis calculation
            // test.applyFields will set all of the calendar's fields to 
            // match those in the test case.
            //
            cal.clear();
            test.applyFields(cal);
            if (!test.equals(cal)) {
                errln("Fail: (fields=>millis) " + test + " => " +
                      pureGregFmt.format(cal.getTime()) +
                      ", expected " + pureGregFmt.format(test.getTime()));
            }
        }
    }
    
    static public final boolean ROLL = true;
    static public final boolean ADD = false;
    
    /**
     * Process test cases for <code>add</code> and <code>roll</code> methods.
     * Each test case is an array of integers, as follows:
     * <ul>
     *  <li>0: input year
     *  <li>1:       month  (zero-based)
     *  <li>2:       day
     *  <li>3: field to roll or add to
     *  <li>4: amount to roll or add
     *  <li>5: result year
     *  <li>6:        month (zero-based)
     *  <li>7:        day
     * </ul>
     * For example:
     * <pre>
     *   //       input                add by          output
     *   //  year  month     day     field amount    year  month     day
     *   {   5759, HESHVAN,   2,     MONTH,   1,     5759, KISLEV,    2 },
     * </pre>
     *
     * @param roll  <code>true</code> or <code>ROLL</code> to test the <code>roll</code> method;
     *              <code>false</code> or <code>ADD</code> to test the <code>add</code method
     */
    public void doRollAdd(boolean roll, Calendar cal, int[][] tests)
    {
        String name = roll ? "rolling" : "adding";
        
        for (int i = 0; i < tests.length; i++) {
            int[] test = tests[i];

            cal.clear();
            cal.set(test[0], test[1], test[2]);
            if (roll) {
                cal.roll(test[3], test[4]);
            } else {
                cal.add(test[3], test[4]);
            }
            
            if (cal.get(YEAR) != test[5] || cal.get(MONTH) != test[6]
                    || cal.get(DATE) != test[7])
            {
                errln("Error " + name + " "+ ymdToString(test[0], test[1], test[2])
                    + " " + FIELD_NAME[test[3]] + " by " + test[4]
                    + ": expected " + ymdToString(test[5], test[6], test[7])
                    + ", got " + ymdToString(cal));
            }
        }
    }

    /**
     * Test the functions getXxxMinimum() and getXxxMaximum() by marching a
     * test calendar 'cal' through 'numberOfDays' sequential days starting
     * with 'startDate'.  For each date, read a field value along with its
     * reported actual minimum and actual maximum.  These values are
     * checked against one another as well as against getMinimum(),
     * getGreatestMinimum(), getLeastMaximum(), and getMaximum().  We
     * expect to see:
     *
     * 1. minimum <= actualMinimum <= greatestMinimum <=
     *    leastMaximum <= actualMaximum <= maximum
     *
     * 2. actualMinimum <= value <= actualMaximum
     *
     * Note: In addition to outright failures, this test reports some
     * results as warnings.  These are not generally of concern, but they
     * should be evaluated by a human.  To see these, run this test in
     * verbose mode.
     * @param cal the calendar to be tested
     * @param fieldsToTest an array of field values to be tested, e.g., new
     * int[] { Calendar.MONTH, Calendar.DAY_OF_MONTH }.  It only makes
     * sense to test the day fields; the time fields are not tested by this
     * method.  If null, then test all standard fields.
     * @param startDate the first date to test
     * @param testDuration if positive, the number of days to be tested.
     * If negative, the number of seconds to run the test.
     */
    protected void doLimitsTest(Calendar cal, int[] fieldsToTest,
                                Date startDate, int testDuration) {
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTime(startDate);
        logln("Start: " + startDate);

        if (fieldsToTest == null) {
            fieldsToTest = new int[] {
                Calendar.ERA, Calendar.YEAR, Calendar.MONTH,
                Calendar.WEEK_OF_YEAR, Calendar.WEEK_OF_MONTH,
                Calendar.DAY_OF_MONTH, Calendar.DAY_OF_YEAR,
                Calendar.DAY_OF_WEEK_IN_MONTH, Calendar.YEAR_WOY,
                Calendar.EXTENDED_YEAR
            };
        }

        // Keep a record of minima and maxima that we actually see.
        // These are kept in an array of arrays of hashes.
        Hashtable[][] limits = new Hashtable[fieldsToTest.length][2];
        Object nub = new Object(); // Meaningless placeholder

        // This test can run for a long time; show progress.
        long millis = System.currentTimeMillis();
        long mark = millis + 5000; // 5 sec
        millis -= testDuration * 1000; // stop time if testDuration<0

        for (int i=0;
             testDuration>0 ? i<testDuration
                            : System.currentTimeMillis()<millis;
             ++i) {
            if (System.currentTimeMillis() >= mark) {
                logln("(" + i + " days)");
                mark += 5000; // 5 sec
            }
            cal.setTimeInMillis(greg.getTimeInMillis());
            for (int j=0; j<fieldsToTest.length; ++j) {
                int f = fieldsToTest[j];
                int v = cal.get(f);
                int minActual = cal.getActualMinimum(f);
                int maxActual = cal.getActualMaximum(f);
                int minLow = cal.getMinimum(f);
                int minHigh = cal.getGreatestMinimum(f);
                int maxLow = cal.getLeastMaximum(f);
                int maxHigh = cal.getMaximum(f);

                // Fetch the hash for this field and keep track of the
                // minima and maxima.
                Hashtable[] h = limits[j];
                if (h[0] == null) {
                    h[0] = new Hashtable();
                    h[1] = new Hashtable();
                }
                h[0].put(new Integer(minActual), nub);
                h[1].put(new Integer(maxActual), nub);

                if (minActual < minLow || minActual > minHigh) {
                    errln("Fail: " + ymdToString(cal) +
                          " Range for min of " + FIELD_NAME[f] +
                          "=" + minLow + ".." + minHigh +
                          ", actual_min=" + minActual);
                }
                if (maxActual < maxLow || maxActual > maxHigh) {
                    errln("Fail: " + ymdToString(cal) +
                          " Range for max of " + FIELD_NAME[f] +
                          "=" + maxLow + ".." + maxHigh +
                          ", actual_max=" + maxActual);
                }
                if (v < minActual || v > maxActual) {
                    errln("Fail: " + ymdToString(cal) +
                          " " + FIELD_NAME[f] + "=" + v +
                          ", actual range=" + minActual + ".." + maxActual +
                          ", allowed=(" + minLow + ".." + minHigh + ")..(" +
                          maxLow + ".." + maxHigh + ")");
                }
            }
            greg.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Check actual maxima and minima seen against ranges returned
        // by API.
        StringBuffer buf = new StringBuffer();
        for (int j=0; j<fieldsToTest.length; ++j) {
            int f = fieldsToTest[j];
            buf.setLength(0);
            buf.append(FIELD_NAME[f]);
            Hashtable[] h = limits[j];
            boolean fullRangeSeen = true;
            for (int k=0; k<2; ++k) {
                int rangeLow = (k==0) ?
                    cal.getMinimum(f) : cal.getLeastMaximum(f);
                int rangeHigh = (k==0) ?
                    cal.getGreatestMinimum(f) : cal.getMaximum(f);
                // If either the top of the range or the bottom was never
                // seen, then there may be a problem.
                if (h[k].get(new Integer(rangeLow)) == null ||
                    h[k].get(new Integer(rangeHigh)) == null) {
                    fullRangeSeen = false;
                }
                buf.append(k==0 ? " minima seen=(" : "; maxima seen=(");
                for (Enumeration e=h[k].keys(); e.hasMoreElements(); ) {
                    int v = ((Integer) e.nextElement()).intValue();
                    buf.append(" " + v);
                }
                buf.append(") range=" + rangeLow + ".." + rangeHigh);
            }
            if (fullRangeSeen) {
                logln("OK: " + buf.toString());
            } else {
                // This may or may not be an error -- if the range of dates
                // we scan over doesn't happen to contain a minimum or
                // maximum, it doesn't mean some other range won't.
                logln("Warning: " + buf.toString());
            }
        }

        logln("End: " + greg.getTime());
    }

    /**
     * Convert year,month,day values to the form "year/month/day".
     * On input the month value is zero-based, but in the result string it is one-based.
     */
    static public String ymdToString(int year, int month, int day) {
        return "" + year + "/" + (month+1) + "/" + day;
    }

    /**
     * Convert year,month,day values to the form "year/month/day".
     */
    static public String ymdToString(Calendar cal) {
        if (cal instanceof ChineseCalendar) {
            return "" + cal.get(Calendar.EXTENDED_YEAR) + "/" +
                (cal.get(Calendar.MONTH)+1) +
                (cal.get(ChineseCalendar.IS_LEAP_MONTH)==1?"(leap)":"") + "/" +
                cal.get(Calendar.DATE);
        }
        return ymdToString(cal.get(Calendar.EXTENDED_YEAR),
                           cal.get(MONTH), cal.get(DATE));
    }
}
