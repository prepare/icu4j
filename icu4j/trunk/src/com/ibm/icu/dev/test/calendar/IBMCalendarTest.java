/*
 *******************************************************************************
 * Copyright (C) 2000, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/calendar/IBMCalendarTest.java,v $ 
 * $Date: 2003/05/09 18:46:44 $ 
 * $Revision: 1.17 $
 *******************************************************************************
 */
package com.ibm.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import com.ibm.icu.impl.LocaleUtility;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.*;

/**
 * @test
 * @summary Tests of new functionality in IBMCalendar
 */
public class IBMCalendarTest extends CalendarTest {

    public static void main(String[] args) throws Exception {
        new IBMCalendarTest().run(args);
    }

    /**
     * Test weekend support in IBMCalendar.
     *
     * NOTE: This test will have to be updated when the isWeekend() etc.
     *       API is finalized later.
     *
     *       In particular, the test will have to be rewritten to instantiate
     *       a Calendar in the given locale (using getInstance()) and call
     *       that Calendar's isWeekend() etc. methods.
     */
    public void TestWeekend() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy G HH:mm:ss.SSS");
        
        // NOTE
        // This test tests for specific locale data.  This is probably okay
        // as far as US data is concerned, but if the Arabic/Bahrain data
        // changes, this test will have to be updated.

        // Test specific days
        Object[] DATA1 = {
            Locale.US, new int[] { // Saturday:Sunday
                2000, Calendar.MARCH, 17, 23,  0, 0, // Fri 23:00
                2000, Calendar.MARCH, 18,  0, -1, 0, // Fri 23:59:59.999
                2000, Calendar.MARCH, 18,  0,  0, 1, // Sat 00:00
                2000, Calendar.MARCH, 18, 15,  0, 1, // Sat 15:00
                2000, Calendar.MARCH, 19, 23,  0, 1, // Sun 23:00
                2000, Calendar.MARCH, 20,  0, -1, 1, // Sun 23:59:59.999
                2000, Calendar.MARCH, 20,  0,  0, 0, // Mon 00:00
                2000, Calendar.MARCH, 20,  8,  0, 0, // Mon 08:00
            },
            new Locale("ar", "BH"), new int[] { // Thursday:Friday
                2000, Calendar.MARCH, 15, 23,  0, 0, // Wed 23:00
                2000, Calendar.MARCH, 16,  0, -1, 0, // Wed 23:59:59.999
                2000, Calendar.MARCH, 16,  0,  0, 1, // Thu 00:00
                2000, Calendar.MARCH, 16, 15,  0, 1, // Thu 15:00
                2000, Calendar.MARCH, 17, 23,  0, 1, // Fri 23:00
                2000, Calendar.MARCH, 18,  0, -1, 1, // Fri 23:59:59.999
                2000, Calendar.MARCH, 18,  0,  0, 0, // Sat 00:00
                2000, Calendar.MARCH, 18,  8,  0, 0, // Sat 08:00
            },
        };

        // Test days of the week
        Object[] DATA2 = {
            Locale.US, new int[] {
                Calendar.MONDAY,   Calendar.WEEKDAY,
                Calendar.FRIDAY,   Calendar.WEEKDAY,
                Calendar.SATURDAY, Calendar.WEEKEND,
                Calendar.SUNDAY,   Calendar.WEEKEND,
            },
            new Locale("ar", "BH"), new int[] { // Thursday:Friday
                Calendar.WEDNESDAY,Calendar.WEEKDAY,
                Calendar.SATURDAY, Calendar.WEEKDAY,
                Calendar.THURSDAY, Calendar.WEEKEND,
                Calendar.FRIDAY,   Calendar.WEEKEND,
            },
        };

        // We only test the getDayOfWeekType() and isWeekend() APIs.
        // The getWeekendTransition() API is tested indirectly via the
        // isWeekend() API, which calls it.

        for (int i1=0; i1<DATA1.length; i1+=2) {
            Locale loc = (Locale)DATA1[i1];
            int[] data = (int[]) DATA1[i1+1];
            Calendar cal = Calendar.getInstance(loc);
            logln("Locale: " + loc);
            for (int i=0; i<data.length; i+=6) {
                cal.clear();
                cal.set(data[i], data[i+1], data[i+2], data[i+3], 0, 0);
                if (data[i+4] != 0) {
                    cal.setTime(new Date(cal.getTime().getTime() + data[i+4]));
                }
                boolean isWeekend = cal.isWeekend();
                boolean ok = isWeekend == (data[i+5] != 0);
                if (ok) {
                    logln("Ok:   " + fmt.format(cal.getTime()) + " isWeekend=" + isWeekend);
                } else {
                    errln("FAIL: " + fmt.format(cal.getTime()) + " isWeekend=" + isWeekend +
                          ", expected=" + (!isWeekend));
                }
            }
        }

        for (int i2=0; i2<DATA2.length; i2+=2) {
            Locale loc = (Locale)DATA2[i2];
            int[] data = (int[]) DATA2[i2+1];
            logln("Locale: " + loc);
            Calendar cal = Calendar.getInstance(loc);
            for (int i=0; i<data.length; i+=2) {
                int type = cal.getDayOfWeekType(data[i]);
                int exp  = data[i+1];
                if (type == exp) {
                    logln("Ok:   DOW " + data[i] + " type=" + type);
                } else {
                    errln("FAIL: DOW " + data[i] + " type=" + type +
                          ", expected=" + exp);
                }
            }
        }
    }

    /**
     * Run a test of a quasi-Gregorian calendar.  This is a calendar
     * that behaves like a Gregorian but has different year/era mappings.
     * The int[] data array should have the format:
     * 
     * { era, year, gregorianYear, month, dayOfMonth, ... }
     */
    void quasiGregorianTest(Calendar cal, int[] data) {
        // As of JDK 1.4.1_01, using the Sun JDK GregorianCalendar as
        // a reference throws us off by one hour.  This is most likely
        // due to the JDK 1.4 incorporation of historical time zones.
        //java.util.Calendar grego = java.util.Calendar.getInstance();
        Calendar grego = Calendar.getInstance();
        for (int i=0; i<data.length; ) {
            int era = data[i++];
            int year = data[i++];
            int gregorianYear = data[i++];
            int month = data[i++];
            int dayOfMonth = data[i++];

            grego.clear();
            grego.set(gregorianYear, month, dayOfMonth);
            Date D = grego.getTime();

            cal.clear();
            cal.set(Calendar.ERA, era);
            cal.set(year, month, dayOfMonth);
            Date d = cal.getTime();
            if (d.equals(D)) {
                logln("OK: " + era + ":" + year + "/" + (month+1) + "/" + dayOfMonth +
                      " => " + d);
            } else {
                errln("Fail: " + era + ":" + year + "/" + (month+1) + "/" + dayOfMonth +
                      " => " + d + ", expected " + D);
            }

            cal.clear();
            cal.setTime(D);
            int e = cal.get(Calendar.ERA);
            int y = cal.get(Calendar.YEAR);
            if (y == year && e == era) {
                logln("OK: " + D + " => " + cal.get(Calendar.ERA) + ":" +
                      cal.get(Calendar.YEAR) + "/" +
                      (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DATE));
            } else {
                logln("Fail: " + D + " => " + cal.get(Calendar.ERA) + ":" +
                      cal.get(Calendar.YEAR) + "/" +
                      (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DATE) +
                      ", expected " + era + ":" + year + "/" + (month+1) + "/" +
                      dayOfMonth);
            }
        }
    }

    /**
     * Verify that BuddhistCalendar shifts years to Buddhist Era but otherwise
     * behaves like GregorianCalendar.
     */
    public void TestBuddhist() {
        quasiGregorianTest(new BuddhistCalendar(),
                           new int[] {
                               // BE 2542 == 1999 CE
                               0, 2542, 1999, Calendar.JUNE, 4
                           });
    }

    public void TestBuddhistCoverage() {
	{
	    // new BuddhistCalendar(TimeZone)
	    BuddhistCalendar cal = new BuddhistCalendar(TimeZone.getDefault());
	}
	
	{
	    // new BuddhistCalendar(Locale)
	    BuddhistCalendar cal = new BuddhistCalendar(Locale.getDefault());
	}

	{
	    // new BuddhistCalendar(TimeZone, Locale)
	    BuddhistCalendar cal = new BuddhistCalendar(TimeZone.getDefault(), Locale.getDefault());
	}

	{
	    // new BuddhistCalendar(Date)
	    BuddhistCalendar cal = new BuddhistCalendar(new Date());
	}

	{
	    // new BuddhistCalendar(int year, int month, int date)
	    BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22);
	}

	{
	    // new BuddhistCalendar(int year, int month, int date, int hour, int minute, int second)
	    BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22, 1, 1, 1);
	}

	{
	    // data
	    BuddhistCalendar cal = new BuddhistCalendar(2543, Calendar.MAY, 22);
	    Date time = cal.getTime();

	    String[] calendarLocales = {
		"th_TH"
	    };

	    String[] formatLocales = {
		"en", "ar", "hu", "th"
	    };

	    for (int i = 0; i < calendarLocales.length; ++i) {
		String calLocName = calendarLocales[i];
		Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
		cal = new BuddhistCalendar(calLocale);

		for (int j = 0; j < formatLocales.length; ++j) {
		    String locName = formatLocales[j];
		    Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
		    DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
		    logln(calLocName + "/" + locName + " --> " + format.format(time));
		}
	    }
	}
    }

    /**
     * Verify that JapaneseCalendar shifts years to Buddhist Era but otherwise
     * behaves like GregorianCalendar.
     */
    public void TestJapanese() {
        // First make sure this test works for GregorianCalendar
        int[] control = {
            GregorianCalendar.AD, 1868, 1868, Calendar.SEPTEMBER, 8,
            GregorianCalendar.AD, 1868, 1868, Calendar.SEPTEMBER, 9,
            GregorianCalendar.AD, 1869, 1869, Calendar.JUNE, 4,
            GregorianCalendar.AD, 1912, 1912, Calendar.JULY, 29,
            GregorianCalendar.AD, 1912, 1912, Calendar.JULY, 30,
            GregorianCalendar.AD, 1912, 1912, Calendar.AUGUST, 1,
        };
        quasiGregorianTest(new GregorianCalendar(), control);

        int[] data = {
            JapaneseCalendar.MEIJI, 1, 1868, Calendar.SEPTEMBER, 8,
            JapaneseCalendar.MEIJI, 1, 1868, Calendar.SEPTEMBER, 9,
            JapaneseCalendar.MEIJI, 2, 1869, Calendar.JUNE, 4,
            JapaneseCalendar.MEIJI, 45, 1912, Calendar.JULY, 29,
            JapaneseCalendar.TAISHO, 1, 1912, Calendar.JULY, 30,
            JapaneseCalendar.TAISHO, 1, 1912, Calendar.AUGUST, 1,
        };
        quasiGregorianTest(new JapaneseCalendar(), data);
    }

    /**
     * Test limits of the Gregorian calendar.
     */
    public void TestGregorianLimits() {
        // Final parameter is either number of days, if > 0, or test
        // duration in seconds, if < 0.
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JANUARY, 1);
        doLimitsTest(new GregorianCalendar(), null, cal.getTime(), -10);
    }
    
    /**
     * Test behavior of fieldDifference around leap years.  Also test a large
     * field difference to check binary search.
     */
    public void TestLeapFieldDifference() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.FEBRUARY, 29);
        Date date2004 = cal.getTime();
        cal.set(2000, Calendar.FEBRUARY, 29);
        Date date2000 = cal.getTime();
        int y = cal.fieldDifference(date2004, Calendar.YEAR);
        int d = cal.fieldDifference(date2004, Calendar.DAY_OF_YEAR);
        if (d == 0) {
            logln("Ok: 2004/Feb/29 - 2000/Feb/29 = " + y + " years, " + d + " days");
        } else {
            errln("FAIL: 2004/Feb/29 - 2000/Feb/29 = " + y + " years, " + d + " days");
        }
        cal.setTime(date2004);
        y = cal.fieldDifference(date2000, Calendar.YEAR);
        d = cal.fieldDifference(date2000, Calendar.DAY_OF_YEAR);
        if (d == 0) {
            logln("Ok: 2000/Feb/29 - 2004/Feb/29 = " + y + " years, " + d + " days");
        } else {
            errln("FAIL: 2000/Feb/29 - 2004/Feb/29 = " + y + " years, " + d + " days");
        }
        // Test large difference
        cal.set(2001, Calendar.APRIL, 5); // 2452005
        Date ayl = cal.getTime();
        cal.set(1964, Calendar.SEPTEMBER, 7); // 2438646
        Date asl = cal.getTime();
        d = cal.fieldDifference(ayl, Calendar.DAY_OF_MONTH);
        cal.setTime(ayl);
        int d2 = cal.fieldDifference(asl, Calendar.DAY_OF_MONTH);
        if (d == -d2 && d == 13359) {
            logln("Ok: large field difference symmetrical " + d);
        } else {
            logln("FAIL: large field difference incorrect " + d + ", " + d2 +
                  ", expect +/- 13359");
        }
    }

    /**
     * Test ms_MY "Malay (Malaysia)" locale.  Bug 1543.
     */
    public void TestMalaysianInstance() {
        Locale loc = new Locale("ms", "MY");  // Malay (Malaysia)
        Calendar cal = Calendar.getInstance(loc);
    }

    /**
     * setFirstDayOfWeek and setMinimalDaysInFirstWeek may change the
     * field <=> time mapping, since they affect the interpretation of
     * the WEEK_OF_MONTH or WEEK_OF_YEAR fields.
     */
    public void TestWeekShift() {
        Calendar cal = new GregorianCalendar(
                             TimeZone.getTimeZone("America/Los_Angeles"),
                             new Locale("en", "US"));
        cal.setTime(new Date(997257600000L)); // Wed Aug 08 01:00:00 PDT 2001
        // In pass one, change the first day of week so that the weeks
        // shift in August 2001.  In pass two, change the minimal days
        // in the first week so that the weeks shift in August 2001.
        //     August 2001     
        // Su Mo Tu We Th Fr Sa
        //           1  2  3  4
        //  5  6  7  8  9 10 11
        // 12 13 14 15 16 17 18
        // 19 20 21 22 23 24 25
        // 26 27 28 29 30 31   
        for (int pass=0; pass<2; ++pass) {
            if (pass==0) {
                cal.setFirstDayOfWeek(Calendar.WEDNESDAY);
                cal.setMinimalDaysInFirstWeek(4);
            } else {
                cal.setFirstDayOfWeek(Calendar.SUNDAY);
                cal.setMinimalDaysInFirstWeek(4);
            }
            cal.add(Calendar.DATE, 1); // Force recalc
            cal.add(Calendar.DATE, -1);

            Date time1 = cal.getTime(); // Get time -- should not change

            // Now change a week parameter and then force a recalc.
            // The bug is that the recalc should not be necessary --
            // calendar should do so automatically.
            if (pass==0) {
                cal.setFirstDayOfWeek(Calendar.THURSDAY);
            } else {
                cal.setMinimalDaysInFirstWeek(5);
            }

            int woy1 = cal.get(Calendar.WEEK_OF_YEAR);
            int wom1 = cal.get(Calendar.WEEK_OF_MONTH);

            cal.add(Calendar.DATE, 1); // Force recalc
            cal.add(Calendar.DATE, -1);

            int woy2 = cal.get(Calendar.WEEK_OF_YEAR);
            int wom2 = cal.get(Calendar.WEEK_OF_MONTH);

            Date time2 = cal.getTime();

            if (!time1.equals(time2)) {
                errln("FAIL: shifting week should not alter time");
            } else {
                logln(time1.toString());
            }
            if (woy1 == woy2 && wom1 == wom2) {
                logln("Ok: WEEK_OF_YEAR: " + woy1 +
                      ", WEEK_OF_MONTH: " + wom1);
            } else {
                errln("FAIL: WEEK_OF_YEAR: " + woy1 + " => " + woy2 +
                      ", WEEK_OF_MONTH: " + wom1 + " => " + wom2 +
                      " after week shift");
            }
        }
    }

    /**
     * Make sure that when adding a day, we actually wind up in a
     * different day.  The DST adjustments we use to keep the hour
     * constant across DST changes can backfire and change the day.
     */
    public void TestTimeZoneTransitionAdd() {
        Locale locale = Locale.US; // could also be CHINA
        SimpleDateFormat dateFormat =
            new SimpleDateFormat("MM/dd/yyyy HH:mm z", locale);

        String tz[] = TimeZone.getAvailableIDs();

        for (int z=0; z<tz.length; ++z) {
            TimeZone t = TimeZone.getTimeZone(tz[z]);
            dateFormat.setTimeZone(t);

            Calendar cal = Calendar.getInstance(t, locale);
            cal.clear();
            // Scan the year 2003, overlapping the edges of the year
            cal.set(Calendar.YEAR, 2002);
            cal.set(Calendar.MONTH, Calendar.DECEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 25);

            for (int i=0; i<365+10; ++i) {
                Date yesterday = cal.getTime();
                int yesterday_day = cal.get(Calendar.DAY_OF_MONTH);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                if (yesterday_day == cal.get(Calendar.DAY_OF_MONTH)) {
                    errln(tz[z] + " " +
                          dateFormat.format(yesterday) + " +1d= " +
                          dateFormat.format(cal.getTime()));
                }
            }
        }
    }

    /**
     * Miscellaneous tests to increase coverage.
     */
    public void TestCoverage() {
        // BuddhistCalendar
        BuddhistCalendar bcal = new BuddhistCalendar();
        int i = bcal.getMinimum(Calendar.ERA);
        bcal.add(Calendar.YEAR, 1);
        bcal.add(Calendar.MONTH, 1);
        Date d = bcal.getTime();

        // CalendarAstronomer
        // (This class should probably be made package-private.)
        CalendarAstronomer astro = new CalendarAstronomer();
        String s = astro.local(0);

        // ChineseCalendar
        ChineseCalendar ccal = new ChineseCalendar(TimeZone.getDefault(),
                                                   Locale.getDefault());
        ccal.add(Calendar.MONTH, 1);
        ccal.add(Calendar.YEAR, 1);
        ccal.roll(Calendar.MONTH, 1);
        ccal.roll(Calendar.YEAR, 1);
        d = ccal.getTime();
    }
}
