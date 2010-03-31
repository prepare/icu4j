/*
 *******************************************************************************
 * Copyright (C) 1996-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import com.ibm.icu.dev.test.TestFmwk;

import com.ibm.icu.impl.LocaleUtility;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.EasterHoliday;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.Holiday;
import com.ibm.icu.util.RangeDateRule;
import com.ibm.icu.util.SimpleDateRule;
import com.ibm.icu.util.SimpleHoliday;

/**
 * Tests for the <code>Holiday</code> class.
 */
public class HolidayTest extends TestFmwk {
    public static void main(String args[]) throws Exception {
        new HolidayTest().run(args);
    }

    static final Calendar cal = new GregorianCalendar(1, 0, 1);
    static final Date longTimeAgo = cal.getTime();
    static final Date now = new Date();
    static final long awhile = 3600L * 24 * 28; // 28 days

    public void TestAPI() {
        {
            // getHolidays
            Holiday[] holidays = Holiday.getHolidays();
            exerciseHolidays(holidays, Locale.getDefault());
        }

        {
            // getHolidays(Locale)
            String[] localeNames =
            {
                "en_US",
                "da",
                "da_DK",
                "de",
                "de_AT",
                "de_DE",
                "el",
                "el_GR",
                "en",
                "en_CA",
                "en_GB",
                "es",
                "es_MX",
                "fr",
                "fr_CA",
                "fr_FR",
                "it",
                "it_IT",
                "iw",
                "iw_IL",
                "ja",
                "ja_JP",
            };

            for (int i = 0; i < localeNames.length; ++i) {
                Locale locale = LocaleUtility.getLocaleFromName(localeNames[i]);
                Holiday[] holidays = Holiday.getHolidays(locale);
                exerciseHolidays(holidays, locale);
            }
        }
    }

    void exerciseHolidays(Holiday[] holidays, Locale locale) {
        for (int i = 0; i < holidays.length; ++i) {
            exerciseHoliday(holidays[i], locale);
        }
    }

    void exerciseHoliday(Holiday h, Locale locale) {
        logln("holiday: " + h.getDisplayName());
        logln("holiday in " + locale + ": " + h.getDisplayName(locale));

        Date first = h.firstAfter(longTimeAgo);
        logln("firstAfter: " + longTimeAgo + " is " + first);
        if (first == null) {
            first = longTimeAgo;
        }
        first.setTime(first.getTime() + awhile);

        Date second = h.firstBetween(first, now);
        logln("firstBetween: " + first + " and " + now + " is " + second);
        if (second == null) {
            second = now;
        }

        logln("is on " + first + ": " + h.isOn(first));
        logln("is on " + now + ": " + h.isOn(now));
        logln(
              "is between "
              + first
              + " and "
              + now
              + ": "
              + h.isBetween(first, now));
        logln(
              "is between "
              + first
              + " and "
              + second
              + ": "
              + h.isBetween(first, second));

        //        logln("rule: " + h.getRule().toString());

        //        h.setRule(h.getRule());
    }
    
    public void TestCoverage(){
        Holiday[] h = { new EasterHoliday("Ram's Easter"),
                        new SimpleHoliday(2, 29, 0, "Leap year", 1900, 2100)};
        exerciseHolidays(h, Locale.getDefault());

        RangeDateRule rdr = new RangeDateRule();
        rdr.add(new SimpleDateRule(7, 10));
        Date mbd = getDate(1953, Calendar.JULY, 10);
        Date dbd = getDate(1958, Calendar.AUGUST, 15);
        Date nbd = getDate(1990, Calendar.DECEMBER, 17);
        Date abd = getDate(1992, Calendar.SEPTEMBER, 16);
        Date xbd = getDate(1976, Calendar.JULY, 4);
        Date ybd = getDate(2003, Calendar.DECEMBER, 8);
        rdr.add(new SimpleDateRule(Calendar.JULY, 10, Calendar.MONDAY, false));
        rdr.add(dbd, new SimpleDateRule(Calendar.AUGUST, 15, Calendar.WEDNESDAY, true));
        rdr.add(xbd, null);
        rdr.add(nbd, new SimpleDateRule(Calendar.DECEMBER, 17, Calendar.MONDAY, false));
        rdr.add(ybd, null);

        logln("first after " + mbd + " is " + rdr.firstAfter(mbd));
        logln("first between " + mbd + " and " + dbd + " is " + rdr.firstBetween(mbd, dbd));
        logln("first between " + dbd + " and " + nbd + " is " + rdr.firstBetween(dbd, nbd));
        logln("first between " + nbd + " and " + abd + " is " + rdr.firstBetween(nbd, abd));
        logln("first between " + abd + " and " + xbd + " is " + rdr.firstBetween(abd, xbd));
        logln("first between " + abd + " and " + null + " is " + rdr.firstBetween(abd, null));
        logln("first between " + xbd + " and " + null + " is " + rdr.firstBetween(xbd, null));
        
        //getRule, setRule
        logln("The rule in the holiday: " + h[1].getRule());
        exerciseHoliday(h[1], Locale.getDefault());
        h[1].setRule(rdr);
        logln("Set the new rule to the SimpleHoliday ...");
        if (!rdr.equals(h[1].getRule())) {
            errln("FAIL: getRule and setRule not matched.");
        }
        exerciseHoliday(h[1], Locale.getDefault());
    }

    public void TestIsOn() {
        // jb 1901
        SimpleHoliday sh = new SimpleHoliday(Calendar.AUGUST, 15, "Doug's Day", 1958, 2058);
        
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        
        Date d0 = cal.getTime();
        cal.add(Calendar.SECOND, 1);
        Date d1 = cal.getTime();
        cal.add(Calendar.SECOND, -2);
        Date d2 = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date d3 = cal.getTime();
        cal.add(Calendar.SECOND, 1);
        Date d4 = cal.getTime();
        cal.add(Calendar.SECOND, -2);
        cal.set(Calendar.YEAR, 1957);
        Date d5 = cal.getTime();
        cal.set(Calendar.YEAR, 1958);
        Date d6 = cal.getTime();
        cal.set(Calendar.YEAR, 2058);
        Date d7 = cal.getTime();
        cal.set(Calendar.YEAR, 2059);
        Date d8 = cal.getTime();

        Date[] dates = { d0, d1, d2, d3, d4, d5, d6, d7, d8 };
        boolean[] isOns = { true, true, false, true, false, false, true, true, false };
        for (int i = 0; i < dates.length; ++i) {
            Date d = dates[i];
            logln("\ndate: " + d);
            boolean isOn = sh.isOn(d);
            logln("isOnDate: " + isOn);
            if (isOn != isOns[i]) {
                errln("date: " + d + " should be on Doug's Day!");
            }
            Date h = sh.firstAfter(d);
            logln("firstAfter: " + h);
        }
    }
}
