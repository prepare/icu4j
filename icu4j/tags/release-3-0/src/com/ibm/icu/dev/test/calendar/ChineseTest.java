/*********************************************************************
 * Copyright (C) 2000-2004, International Business Machines Corporation and
 * others. All Rights Reserved.
 *********************************************************************
 */
package com.ibm.icu.dev.test.calendar;
import com.ibm.icu.util.*;
import com.ibm.icu.text.*;
import java.util.Date;

/**
 * Test of ChineseCalendar.
 *
 * Leap months in this century:
 * Wed May 23 2001 = 4638-04*-01, Year 18, Cycle 78
 * Sun Mar 21 2004 = 4641-02*-01, Year 21, Cycle 78
 * Thu Aug 24 2006 = 4643-07*-01, Year 23, Cycle 78
 * Tue Jun 23 2009 = 4646-05*-01, Year 26, Cycle 78
 * Mon May 21 2012 = 4649-04*-01, Year 29, Cycle 78
 * Fri Oct 24 2014 = 4651-09*-01, Year 31, Cycle 78
 * Sun Jul 23 2017 = 4654-06*-01, Year 34, Cycle 78
 * Sat May 23 2020 = 4657-04*-01, Year 37, Cycle 78
 * Wed Mar 22 2023 = 4660-02*-01, Year 40, Cycle 78
 * Fri Jul 25 2025 = 4662-06*-01, Year 42, Cycle 78
 * Fri Jun 23 2028 = 4665-05*-01, Year 45, Cycle 78
 * Tue Apr 22 2031 = 4668-03*-01, Year 48, Cycle 78
 * Thu Dec 22 2033 = 4670-11*-01, Year 50, Cycle 78
 * Wed Jul 23 2036 = 4673-06*-01, Year 53, Cycle 78
 * Wed Jun 22 2039 = 4676-05*-01, Year 56, Cycle 78
 * Sat Mar 22 2042 = 4679-02*-01, Year 59, Cycle 78
 * Tue Aug 23 2044 = 4681-07*-01, Year 01, Cycle 79
 * Sun Jun 23 2047 = 4684-05*-01, Year 04, Cycle 79
 * Thu Apr 21 2050 = 4687-03*-01, Year 07, Cycle 79
 * Mon Sep 23 2052 = 4689-08*-01, Year 09, Cycle 79
 * Sat Jul 24 2055 = 4692-06*-01, Year 12, Cycle 79
 * Wed May 22 2058 = 4695-04*-01, Year 15, Cycle 79
 * Wed Apr 20 2061 = 4698-03*-01, Year 18, Cycle 79
 * Fri Aug 24 2063 = 4700-07*-01, Year 20, Cycle 79
 * Wed Jun 23 2066 = 4703-05*-01, Year 23, Cycle 79
 * Tue May 21 2069 = 4706-04*-01, Year 26, Cycle 79
 * Thu Sep 24 2071 = 4708-08*-01, Year 28, Cycle 79
 * Tue Jul 24 2074 = 4711-06*-01, Year 31, Cycle 79
 * Sat May 22 2077 = 4714-04*-01, Year 34, Cycle 79
 * Sat Apr 20 2080 = 4717-03*-01, Year 37, Cycle 79
 * Mon Aug 24 2082 = 4719-07*-01, Year 39, Cycle 79
 * Fri Jun 22 2085 = 4722-05*-01, Year 42, Cycle 79
 * Fri May 21 2088 = 4725-04*-01, Year 45, Cycle 79
 * Sun Sep 24 2090 = 4727-08*-01, Year 47, Cycle 79
 * Thu Jul 23 2093 = 4730-06*-01, Year 50, Cycle 79
 * Tue May 22 2096 = 4733-04*-01, Year 53, Cycle 79
 * Sun Mar 22 2099 = 4736-02*-01, Year 56, Cycle 79
 */
public class ChineseTest extends CalendarTest {

    public static void main(String args[]) throws Exception {
        new ChineseTest().run(args);
    }

    /**
     * Test basic mapping to and from Gregorian.
     */
    public void TestMapping() {

        final int[] DATA = {
            // (Note: months are 1-based)
            // Gregorian    Chinese
            1964,  9,  4,   4601,  7,0, 28,
            1964,  9,  5,   4601,  7,0, 29,
            1964,  9,  6,   4601,  8,0,  1,
            1964,  9,  7,   4601,  8,0,  2,
            1961, 12, 25,   4598, 11,0, 18,
            1999,  6,  4,   4636,  4,0, 21,
            
            1990,  5, 23,   4627,  4,0, 29,
            1990,  5, 24,   4627,  5,0,  1,
            1990,  6, 22,   4627,  5,0, 30,
            1990,  6, 23,   4627,  5,1,  1,
            1990,  7, 20,   4627,  5,1, 28,
            1990,  7, 21,   4627,  5,1, 29,
            1990,  7, 22,   4627,  6,0,  1,
        };

        ChineseCalendar cal = new ChineseCalendar();
        StringBuffer buf = new StringBuffer();

        logln("Gregorian -> Chinese");
        //java.util.Calendar grego = java.util.Calendar.getInstance();
        Calendar grego = Calendar.getInstance();
        grego.clear();
        for (int i=0; i<DATA.length; ) {
            grego.set(DATA[i++], DATA[i++]-1, DATA[i++]);
            Date date = grego.getTime();
            cal.setTime(date);
            int y = cal.get(Calendar.EXTENDED_YEAR);
            int m = cal.get(Calendar.MONTH)+1; // 0-based -> 1-based
            int L = cal.get(ChineseCalendar.IS_LEAP_MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            int yE = DATA[i++]; // Expected y, m, isLeapMonth, d
            int mE = DATA[i++]; // 1-based
            int LE = DATA[i++];
            int dE = DATA[i++];
            buf.setLength(0);
            buf.append(date + " -> ");
            buf.append(y + "/" + m + (L==1?"(leap)":"") + "/" + d);
            if (y == yE && m == mE && L == LE && d == dE) {
                logln("OK: " + buf.toString());
            } else {
                errln("Fail: " + buf.toString() + ", expected " +
                      yE + "/" + mE + (LE==1?"(leap)":"") + "/" + dE);
            }
        }

        logln("Chinese -> Gregorian");
        for (int i=0; i<DATA.length; ) {
            grego.set(DATA[i++], DATA[i++]-1, DATA[i++]);
            Date dexp = grego.getTime();
            int cyear = DATA[i++];
            int cmonth = DATA[i++];
            int cisleapmonth = DATA[i++];
            int cdayofmonth = DATA[i++];
            cal.clear();
            cal.set(Calendar.EXTENDED_YEAR, cyear);
            cal.set(Calendar.MONTH, cmonth-1);
            cal.set(ChineseCalendar.IS_LEAP_MONTH, cisleapmonth);
            cal.set(Calendar.DAY_OF_MONTH, cdayofmonth);
            Date date = cal.getTime();
            buf.setLength(0);
            buf.append(cyear + "/" + cmonth +
                       (cisleapmonth==1?"(leap)":"") + "/" + cdayofmonth);
            buf.append(" -> " + date);
            if (date.equals(dexp)) {
                logln("OK: " + buf.toString());
            } else {
                errln("Fail: " + buf.toString() + ", expected " + dexp);
            }
        }
    }

    /**
     * Make sure no Gregorian dates map to Chinese 1-based day of
     * month zero.  This was a problem with some of the astronomical
     * new moon determinations.
     */
    public void TestZeroDOM() {
        ChineseCalendar cal = new ChineseCalendar();
        GregorianCalendar greg = new GregorianCalendar(1989, Calendar.SEPTEMBER, 1);
        logln("Start: " + greg.getTime());
        for (int i=0; i<1000; ++i) {
            cal.setTimeInMillis(greg.getTimeInMillis());
            if (cal.get(Calendar.DAY_OF_MONTH) == 0) {
                errln("Fail: " + greg.getTime() + " -> " +
                      cal.get(Calendar.EXTENDED_YEAR) + "/" +
                      cal.get(Calendar.MONTH) +
                      (cal.get(ChineseCalendar.IS_LEAP_MONTH)==1?"(leap)":"") +
                      "/" + cal.get(Calendar.DAY_OF_MONTH));
            }
            greg.add(Calendar.DAY_OF_YEAR, 1);
        }
        logln("End: " + greg.getTime());
    }

    /**
     * Test minimum and maximum functions.
     */
    public void TestLimits() {
        // The number of days and the start date can be adjusted
        // arbitrarily to either speed up the test or make it more
        // thorough, but try to test at least a full year, preferably a
        // full non-leap and a full leap year.

        // Final parameter is either number of days, if > 0, or test
        // duration in seconds, if < 0.
        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        tempcal.set(1989, Calendar.NOVEMBER, 1);
        doLimitsTest(new ChineseCalendar(), null, tempcal.getTime(), -10);
    }

    /**
     * Run through several standard tests from Dershowitz & Reingold.
     */
    public void TestJulianDayMapping() {

        final TestCase[] tests = {
            //
            // From Dershowitz & Reingold, "Calendrical Calculations".
            //
            // The months in this table are 1-based rather than 0-based.
            //
            // * Failing fields->millis
            // ** Millis->fields gives 0-based month -1
            // These failures were fixed by changing the start search date
            // for the winter solstice from Dec 15 to Dec 1.
            // 
            //                  Julian Day   Era  Year Month  Leap   DOM WkDay
            new ChineseTestCase(1507231.5,   35,   11,    6, false,   12,  SUN),
            new ChineseTestCase(1660037.5,   42,    9,   10, false,   27,  WED),
            new ChineseTestCase(1746893.5,   46,    7,    8, false,    4,  WED),
            new ChineseTestCase(1770641.5,   47,   12,    8, false,    9,  SUN),
            new ChineseTestCase(1892731.5,   52,   46,   11, false,   20,  WED),
            new ChineseTestCase(1931579.5,   54,   33,    4, false,    5,  MON),
            new ChineseTestCase(1974851.5,   56,   31,   10, false,   15,  SAT),
            new ChineseTestCase(2091164.5,   61,   50,    3, false,    7,  SUN),
            new ChineseTestCase(2121509.5,   63,   13,    4, false,   24,  SUN),
            new ChineseTestCase(2155779.5,   64,   47,    2, false,    9,  FRI),
            new ChineseTestCase(2174029.5,   65,   37,    2, false,    9,  SAT),
            new ChineseTestCase(2191584.5,   66,   25,    2, false,   23,  FRI),
            new ChineseTestCase(2195261.5,   66,   35,    3, false,    9,  SUN), //*
            new ChineseTestCase(2229274.5,   68,    8,    5, false,    2,  SUN), //*
            new ChineseTestCase(2245580.5,   68,   53,    1, false,    8,  WED), //**
            new ChineseTestCase(2266100.5,   69,   49,    3, false,    4,  SAT), 
            new ChineseTestCase(2288542.5,   70,   50,    8, false,    2,  SAT), //*
            new ChineseTestCase(2290901.5,   70,   57,    1, false,   29,  SAT), //*
            new ChineseTestCase(2323140.5,   72,   25,    4,  true,   20,  WED), //*
            new ChineseTestCase(2334848.5,   72,   57,    6, false,    5,  SUN),
            new ChineseTestCase(2348020.5,   73,   33,    6, false,    6,  FRI),
            new ChineseTestCase(2366978.5,   74,   25,    5, false,    5,  SUN),
            new ChineseTestCase(2385648.5,   75,   16,    6, false,   12,  MON),
            new ChineseTestCase(2392825.5,   75,   36,    2, false,   13,  WED),
            new ChineseTestCase(2416223.5,   76,   40,    3, false,   22,  SUN),
            new ChineseTestCase(2425848.5,   77,    6,    7, false,   21,  SUN),
            new ChineseTestCase(2430266.5,   77,   18,    8, false,    9,  MON),
            new ChineseTestCase(2430833.5,   77,   20,    3, false,   15,  MON),
            new ChineseTestCase(2431004.5,   77,   20,    9, false,    9,  THU),
            new ChineseTestCase(2448698.5,   78,    9,    2, false,   14,  TUE),
            new ChineseTestCase(2450138.5,   78,   13,    1, false,    7,  SUN),
            new ChineseTestCase(2465737.5,   78,   55,   10, false,   14,  WED),
            new ChineseTestCase(2486076.5,   79,   51,    6, false,    7,  SUN),

            // Additional tests not from D&R
            new ChineseTestCase(2467496.5,   78,   60,    8, false,    2,  FRI), // year 60
        };

        ChineseCalendar cal = new ChineseCalendar();
        cal.setLenient(true);
        doTestCases(tests, cal);
    }

    /**
     * Test formatting.
     */
    public void TestFormat() {
        ChineseCalendar cal = new ChineseCalendar();
        DateFormat fmt = DateFormat.getDateTimeInstance(cal,
                                    DateFormat.DEFAULT, DateFormat.DEFAULT);

        java.util.Calendar tempcal = java.util.Calendar.getInstance();
        tempcal.clear();
        
        Date[] DATA = new Date[2];
        tempcal.set(2001, Calendar.MAY, 22);
        DATA[0] = tempcal.getTime();
        tempcal.set(2001, Calendar.MAY, 23);
        DATA[1] = tempcal.getTime();
        // Wed May 23 2001 = Month 4(leap), Day 1, Year 18, Cycle 78
        
        for (int i=0; i<DATA.length; ++i) {
            String s = fmt.format(DATA[i]);
            try {
                Date e = fmt.parse(s);
                if (e.equals(DATA[i])) {
                    logln("Ok: " + DATA[i] + " -> " + s + " -> " + e);
                } else {
                    errln("FAIL: " + DATA[i] + " -> " + s + " -> " + e);
                }
            } catch (java.text.ParseException e) {
                errln("Fail: " + s + " -> parse failure at " + e.getErrorOffset());
                errln(e.toString());
            }
        }
    }

    /**
     * Make sure IS_LEAP_MONTH participates in field resolution.
     */
    public void TestResolution() {
        ChineseCalendar cal = new ChineseCalendar();
        DateFormat fmt = DateFormat.getDateInstance(cal, DateFormat.DEFAULT);

        // May 22 2001 = y4638 m4 d30 doy119
        // May 23 2001 = y4638 m4* d1 doy120

        final int THE_YEAR = 4638;
        final int END = -1;

        int[] DATA = {
            // Format:
            // (field, value)+, END, exp.month, exp.isLeapMonth, exp.DOM
            // Note: exp.month is ONE-BASED

            // If we set DAY_OF_YEAR only, that should be used
            Calendar.DAY_OF_YEAR, 1,
            END,
            1,0,1, // Expect 1-1
            
            // If we set MONTH only, that should be used
            ChineseCalendar.IS_LEAP_MONTH, 1,
            Calendar.DAY_OF_MONTH, 1,
            Calendar.MONTH, 3,
            END,
            4,1,1, // Expect 4*-1
            
            // If we set the DOY last, that should take precedence
            Calendar.MONTH, 1, // Should ignore
            ChineseCalendar.IS_LEAP_MONTH, 1, // Should ignore
            Calendar.DAY_OF_MONTH, 1, // Should ignore
            Calendar.DAY_OF_YEAR, 121,
            END,
            4,1,2, // Expect 4*-2
            
            // I've disabled this test because it doesn't work this way,
            // not even with a GregorianCalendar!  MONTH alone isn't enough
            // to supersede DAY_OF_YEAR.  Some other month-related field is
            // also required. - Liu 11/28/00
            //! // If we set MONTH last, that should take precedence
            //! ChineseCalendar.IS_LEAP_MONTH, 1,
            //! Calendar.DAY_OF_MONTH, 1,
            //! Calendar.DAY_OF_YEAR, 5, // Should ignore
            //! Calendar.MONTH, 3,
            //! END,
            //! 4,1,1, // Expect 4*-1
            
            // If we set IS_LEAP_MONTH last, that should take precedence
            Calendar.MONTH, 3,
            Calendar.DAY_OF_MONTH, 1,
            Calendar.DAY_OF_YEAR, 5, // Should ignore
            ChineseCalendar.IS_LEAP_MONTH, 1,
            END,
            4,1,1, // Expect 4*-1
        };

        StringBuffer buf = new StringBuffer();
        for (int i=0; i<DATA.length; ) {
            cal.clear();
            cal.set(Calendar.EXTENDED_YEAR, THE_YEAR);
            buf.setLength(0);
            buf.append("EXTENDED_YEAR=" + THE_YEAR);
            while (DATA[i] != END) {
                cal.set(DATA[i++], DATA[i++]);
                buf.append(" " + fieldName(DATA[i-2]) + "=" + DATA[i-1]);
            }
            ++i; // Skip over END mark
            int expMonth = DATA[i++]-1;
            int expIsLeapMonth = DATA[i++];
            int expDOM = DATA[i++];
            int month = cal.get(Calendar.MONTH);
            int isLeapMonth = cal.get(ChineseCalendar.IS_LEAP_MONTH);
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            if (expMonth == month && expIsLeapMonth == isLeapMonth &&
                dom == expDOM) {
                logln("OK: " + buf + " => " + fmt.format(cal.getTime()));
            } else {
                String s = fmt.format(cal.getTime());
                cal.clear();
                cal.set(Calendar.EXTENDED_YEAR, THE_YEAR);
                cal.set(Calendar.MONTH, expMonth);
                cal.set(ChineseCalendar.IS_LEAP_MONTH, expIsLeapMonth);
                cal.set(Calendar.DAY_OF_MONTH, expDOM);
                errln("Fail: " + buf + " => " + s +
                      "=" + (month+1) + "," + isLeapMonth + "," + dom +
                      ", expected " + fmt.format(cal.getTime()) +
                      "=" + (expMonth+1) + "," + expIsLeapMonth + "," + expDOM);
            }
        }
    }

    /**
     * Test the behavior of fields that are out of range.
     */
    public void TestOutOfRange() {
        int[] DATA = new int[] {
            // Input       Output
            4638, 13,  1,   4639,  1,  1,
            4638, 18,  1,   4639,  6,  1,
            4639,  0,  1,   4638, 12,  1,
            4639, -6,  1,   4638,  6,  1,
            4638,  1, 32,   4638,  2,  2, // 1-4638 has 30 days
            4638,  2, -1,   4638,  1, 29,
        };
        ChineseCalendar cal = new ChineseCalendar();
        for (int i=0; i<DATA.length; ) {
            int y1 = DATA[i++];
            int m1 = DATA[i++]-1;
            int d1 = DATA[i++];
            int y2 = DATA[i++];
            int m2 = DATA[i++]-1;
            int d2 = DATA[i++];
            cal.clear();
            cal.set(Calendar.EXTENDED_YEAR, y1);
            cal.set(MONTH, m1);
            cal.set(DATE, d1);
            int y = cal.get(Calendar.EXTENDED_YEAR);
            int m = cal.get(MONTH);
            int d = cal.get(DATE);
            if (y!=y2 || m!=m2 || d!=d2) {
                errln("Fail: " + y1 + "/" + (m1+1) + "/" + d1 + " resolves to " +
                      y + "/" + (m+1) + "/" + d + ", expected " +
                      y2 + "/" + (m2+1) + "/" + d2);
            } else  if (isVerbose()) {
                logln("OK: " + y1 + "/" + (m1+1) + "/" + d1 + " resolves to " +
                      y + "/" + (m+1) + "/" + d);
            }
        }
    }

    /**
     * Test the behavior of ChineseCalendar.add().  The only real
     * nastiness with roll is the MONTH field around leap months.
     */
    public void TestAdd() {
        int[][] tests = new int[][] {
            // MONTHS ARE 1-BASED HERE
            // input               add           output
            // year  mon    day    field amount  year  mon    day
            {  4642,   3,0,  15,   MONTH,   3,   4642,   6,0,  15 }, // normal
            {  4639,  12,0,  15,   MONTH,   1,   4640,   1,0,  15 }, // across year
            {  4640,   1,0,  15,   MONTH,  -1,   4639,  12,0,  15 }, // across year
            {  4638,   3,0,  15,   MONTH,   3,   4638,   5,0,  15 }, // 4=leap
            {  4638,   3,0,  15,   MONTH,   2,   4638,   4,1,  15 }, // 4=leap
            {  4638,   4,0,  15,   MONTH,   1,   4638,   4,1,  15 }, // 4=leap
            {  4638,   4,1,  15,   MONTH,   1,   4638,   5,0,  15 }, // 4=leap
            {  4638,   4,0,  30,   MONTH,   1,   4638,   4,1,  29 }, // dom should pin
            {  4638,   4,0,  30,   MONTH,   2,   4638,   5,0,  30 }, // no dom pin
            {  4638,   4,0,  30,   MONTH,   3,   4638,   6,0,  29 }, // dom should pin
        };
       
        ChineseCalendar cal = new ChineseCalendar();
        doRollAdd(ADD, cal, tests);
    }

    /**
     * Test the behavior of ChineseCalendar.roll().  The only real
     * nastiness with roll is the MONTH field around leap months.
     */
    public void TestRoll() {
        int[][] tests = new int[][] {
            // MONTHS ARE 1-BASED HERE
            // input               add           output
            // year  mon    day    field amount  year  mon    day
            {  4642,   3,0,  15,   MONTH,   3,   4642,   6,0,  15 }, // normal
            {  4642,   3,0,  15,   MONTH,  11,   4642,   2,0,  15 }, // normal
            {  4639,  12,0,  15,   MONTH,   1,   4639,   1,0,  15 }, // across year
            {  4640,   1,0,  15,   MONTH,  -1,   4640,  12,0,  15 }, // across year
            {  4638,   3,0,  15,   MONTH,   3,   4638,   5,0,  15 }, // 4=leap
            {  4638,   3,0,  15,   MONTH,  16,   4638,   5,0,  15 }, // 4=leap
            {  4638,   3,0,  15,   MONTH,   2,   4638,   4,1,  15 }, // 4=leap
            {  4638,   3,0,  15,   MONTH,  28,   4638,   4,1,  15 }, // 4=leap
            {  4638,   4,0,  15,   MONTH,   1,   4638,   4,1,  15 }, // 4=leap
            {  4638,   4,0,  15,   MONTH, -12,   4638,   4,1,  15 }, // 4=leap
            {  4638,   4,1,  15,   MONTH,   1,   4638,   5,0,  15 }, // 4=leap
            {  4638,   4,1,  15,   MONTH, -25,   4638,   5,0,  15 }, // 4=leap
            {  4638,   4,0,  30,   MONTH,   1,   4638,   4,1,  29 }, // dom should pin
            {  4638,   4,0,  30,   MONTH,  14,   4638,   4,1,  29 }, // dom should pin
            {  4638,   4,0,  30,   MONTH,  15,   4638,   5,0,  30 }, // no dom pin
            {  4638,   4,0,  30,   MONTH, -10,   4638,   6,0,  29 }, // dom should pin
        };
       
        ChineseCalendar cal = new ChineseCalendar();
        doRollAdd(ROLL, cal, tests);
    }
    
    void doRollAdd(boolean roll, ChineseCalendar cal, int[][] tests) {
        String name = roll ? "rolling" : "adding";
        
        for (int i = 0; i < tests.length; i++) {
            int[] test = tests[i];

            cal.clear();
                cal.set(Calendar.EXTENDED_YEAR, test[0]);
                cal.set(Calendar.MONTH, test[1]-1);
                cal.set(ChineseCalendar.IS_LEAP_MONTH, test[2]);
                cal.set(Calendar.DAY_OF_MONTH, test[3]);
            if (roll) {
                cal.roll(test[4], test[5]);
            } else {
                cal.add(test[4], test[5]);
            }
            if (cal.get(Calendar.EXTENDED_YEAR) != test[6] ||
                cal.get(MONTH) != (test[7]-1) ||
                cal.get(ChineseCalendar.IS_LEAP_MONTH) != test[8] ||
                cal.get(DATE) != test[9]) {
                errln("Fail: " + name + " " +
                      ymdToString(test[0], test[1]-1, test[2], test[3])
                      + " " + fieldName(test[4]) + " by " + test[5]
                      + ": expected " +
                      ymdToString(test[6], test[7]-1, test[8], test[9])
                      + ", got " + ymdToString(cal));
            } else if (isVerbose()) {
                logln("OK: " + name + " " +
                      ymdToString(test[0], test[1]-1, test[2], test[3])
                    + " " + fieldName(test[4]) + " by " + test[5]
                    + ": got " + ymdToString(cal));
            }
        }
    }

    /**
     * Convert year,month,day values to the form "year/month/day".
     * On input the month value is zero-based, but in the result string it is one-based.
     */
    static public String ymdToString(int year, int month, int isLeapMonth, int day) {
        return "" + year + "/" + (month+1) +
            ((isLeapMonth!=0)?"(leap)":"") +
            "/" + day;
    }

//    public void TestFindLeapMonths() {
//        ChineseCalendar cal = new ChineseCalendar();
//        cal.setTime(new Date(2000-1900, Calendar.JANUARY, 1));
//        long end = new Date(2100-1900, Calendar.JANUARY, 1).getTime();
//        ChineseDateFormat fmt = (ChineseDateFormat) DateFormat.getInstance(cal);
//        fmt.applyPattern("u-MMl-dd, 'Year' y, 'Cycle' G");
//        while (cal.getTimeInMillis() < end) {
//            if (cal.get(ChineseCalendar.IS_LEAP_MONTH) != 0) {
//                cal.set(Calendar.DAY_OF_MONTH, 1);
//                logln(cal.getTime() + " = " + fmt.format(cal.getTime()));
//                cal.set(Calendar.DAY_OF_MONTH, 29);
//            }
//            cal.add(Calendar.DAY_OF_YEAR, 25);
//        }
//    }

	public void TestCoverage() {
		ChineseCalendar cal = new ChineseCalendar();
		ChineseDateFormat fmt = (ChineseDateFormat)DateFormat.getInstance(cal);
		fmt.applyPattern("llyyll");
		Date time = getDate(2100, Calendar.JANUARY, 1);
		String str = fmt.format(time);
		try {
			Date e = fmt.parse(str);
			logln("chinese calendar time: " + time + " result: " + str + " --> " + e);
		} catch (java.text.ParseException ex) {
			logln(ex.getMessage()); // chinese calendar can't parse this, no error for now
		}
	}
}
