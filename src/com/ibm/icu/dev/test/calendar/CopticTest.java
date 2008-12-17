/*
 *******************************************************************************
 * Copyright (C) 2005, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import com.ibm.icu.impl.LocaleUtility;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.CopticCalendar;
import com.ibm.icu.util.EthiopicCalendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * Tests for the <code>CopticCalendar</code> class.
 */
public class CopticTest extends CalendarTest 
{
    public static void main(String args[]) throws Exception {
        new CopticTest().run(args);
    }

    /** Constants to save typing. */
    public static final int TOUT      = CopticCalendar.TOUT;
    public static final int BABA      = CopticCalendar.BABA;
    public static final int HATOR     = CopticCalendar.HATOR;
    public static final int KIAHK     = CopticCalendar.KIAHK;
    public static final int TOBA      = CopticCalendar.TOBA;
    public static final int AMSHIR    = CopticCalendar.AMSHIR;
    public static final int BARAMHAT  = CopticCalendar.BARAMHAT;
    public static final int BARAMOUDA = CopticCalendar.BARAMOUDA;
    public static final int BASHANS   = CopticCalendar.BASHANS;
    public static final int PAONA     = CopticCalendar.PAONA;
    public static final int EPEP      = CopticCalendar.EPEP;
    public static final int MESRA     = CopticCalendar.MESRA;
    public static final int NASIE     = CopticCalendar.NASIE;

    /* Test dates from:
     * "The Amharic Letters of Emperor Theodore of Ethiopia to Queen Victoria and
     * Her Special Envoy", David Appleyard, Girma Selasse Asfaw, Oxford University Press, 
     * June 1 1979, ISBN: 0856726605, Longwood Pr Ltd
     *  
     * Coptic         Gregorian    JD
     * 20/02/1579     29/10/1862  2401443 
     * 29/10/1581     05/07/1865  2402423
     * 22/05/1582     29/01/1866  2402631
     * 10/08/1582     17/04/1866  2402709
     * 28/04/1583     05/01/1867  2402972
     * 05/05/1584     13/01/1868  2403345
     * 
     * --------------------------------------------------
     * 
     * From the Calendrica applet:  http://emr.cs.iit.edu/home/reingold/calendar-book/Calendrica.html
     * 
     * Coptic         Gregorian    JD
     * 07/05/-284     01/01/0000  1721060
     * 08/05/-283     01/01/0001  1721426
     * 06/13/-1       29/08/0283  1824664
     * 
     * 01/01/0000     30/08/0283  1824665
     * 01/01/0001     29/08/0284  1825030
     * 01/01/0002     29/08/0285  1825395
     * 01/01/0003     29/08/0286  1825760
     * 01/01/0004     30/08/0287  1826126
     * 05/13/0000     28/08/0284  1825029 
     * 05/13/0001     28/08/0285  1825394
     * 05/13/0002     28/08/0286  1825759
     * 05/13/0003     28/08/0287  1826124
     * 06/13/0003     29/08/0287  1826125  first coptic leap year
     * 05/13/0004     28/08/0288  1826490
     * 
     * 06/02/1299     13/10/1582  2299159
     * 07/02/1299     14/10/1582  2299160  Julian 04/10/1582
     * 08/02/1299     15/10/1582  2299161
     * 09/02/1299     16/10/1582  2299162
     * 
     * 23/04/1616     01/01/1900  2415021
     * 23/04/1721     01/01/2005  2453372 
     * 05/13/2000     12/09/2284  2555529
     */

    
    /** A huge list of test cases to make sure that computeTime and computeFields
     * work properly for a wide range of data in the civil calendar.
     */
    public void TestCases()
    {
        final TestCase[] tests = {
            //
            // The months in this table are 1-based rather than 0-based,
            // because it's easier to edit that way.
            //                      Coptic
            //          Julian Day  Era  Year  Month Day  WkDay Hour Min Sec
            //
            // Dates from "Emporer Theodore..."

            new TestCase(2401442.5,  1,  1579,    2,  20,  WED,    0,  0,  0), // Gregorian: 20/10/1862
            new TestCase(2402422.5,  1,  1581,   10,  29,  WED,    0,  0,  0), // Gregorian: 05/07/1865
            new TestCase(2402630.5,  1,  1582,    5,  22,  MON,    0,  0,  0), // Gregorian: 29/01/1866
            new TestCase(2402708.5,  1,  1582,    8,  10,  TUE,    0,  0,  0), // Gregorian: 17/04/1866
            new TestCase(2402971.5,  1,  1583,    4,  28,  SAT,    0,  0,  0), // Gregorian: 05/01/1867
            new TestCase(2403344.5,  1,  1584,    5,   5,  MON,    0,  0,  0), // Gregorian: 13/01/1868
            new TestCase(1721059.5,  0,  -284,    5,   7,  SAT,    0,  0,  0), // Gregorian: 01/01/0000
            new TestCase(1721425.5,  0,  -283,    5,   8,  MON,    0,  0,  0), // Gregorian: 01/01/0001
            new TestCase(1824663.5,  0,    -1,   13,   6,  WED,    0,  0,  0), // Gregorian: 29/08/0283
            new TestCase(1824664.5,  1,     0,    1,   1,  THU,    0,  0,  0), // Gregorian: 30/08/0283
            new TestCase(1825029.5,  1,     1,    1,   1,  FRI,    0,  0,  0), // Gregorian: 29/08/0284
            new TestCase(1825394.5,  1,     2,    1,   1,  SAT,    0,  0,  0), // Gregorian: 29/08/0285
            new TestCase(1825759.5,  1,     3,    1,   1,  SUN,    0,  0,  0), // Gregorian: 29/08/0286
            new TestCase(1826125.5,  1,     4,    1,   1,  TUE,    0,  0,  0), // Gregorian: 30/08/0287
            new TestCase(1825028.5,  1,     0,   13,   5,  THU,    0,  0,  0), // Gregorian: 28/08/0284
            new TestCase(1825393.5,  1,     1,   13,   5,  FRI,    0,  0,  0), // Gregorian: 28/08/0285
            new TestCase(1825758.5,  1,     2,   13,   5,  SAT,    0,  0,  0), // Gregorian: 28/08/0286
            new TestCase(1826123.5,  1,     3,   13,   5,  SUN,    0,  0,  0), // Gregorian: 28/08/0287
            new TestCase(1826124.5,  1,     3,   13,   6,  MON,    0,  0,  0), // Gregorian: 29/08/0287
                          // above is first coptic leap year
            new TestCase(1826489.5,  1,     4,   13,   5,  TUE,    0,  0,  0), // Gregorian: 28/08/0288
            new TestCase(2299158.5,  1,  1299,    2,   6,  WED,    0,  0,  0), // Gregorian: 13/10/1582
            new TestCase(2299159.5,  1,  1299,    2,   7,  THU,    0,  0,  0), // Gregorian: 14/10/1582

            new TestCase(2299160.5,  1,  1299,    2,   8,  FRI,    0,  0,  0), // Gregorian: 15/10/1582
            new TestCase(2299161.5,  1,  1299,    2,   9,  SAT,    0,  0,  0), // Gregorian: 16/10/1582

            new TestCase(2415020.5,  1,  1616,    4,  23,  MON,    0,  0,  0), // Gregorian: 01/01/1900
            new TestCase(2453371.5,  1,  1721,    4,  23,  SAT,    0,  0,  0), // Gregorian: 01/01/2005
            new TestCase(2555528.5,  1,  2000,   13,   5,  FRI,    0,  0,  0), // Gregorian: 12/09/2284
        };
        
        CopticCalendar testCalendar = new CopticCalendar();
        testCalendar.setLenient(true);
        doTestCases(tests, testCalendar);
    }

    // basic sanity check that the conversion algorithm round-trips
    public void TestCopticToJD() {
        for (int y = -2; y < 3; ++y) {
            for (int m = 0; m < 12; ++m) { // don't understand rules for 13th month
                for (int d = 1; d < 25; d += 3) { // play it safe on days per month
                    int jd = CopticCalendar.copticToJD(y, m, d);
                    Integer[] res = CopticCalendar.getDateFromJD(jd);
                    if (!(y == res[0].intValue() &&
                          m == res[1].intValue() &&
                          d == res[2].intValue())) {
                        errln("y: " + y +
                              " m: " + m + 
                              " d: " + d + 
                              " --> jd: " + jd +
                              " --> y: " + res[0].intValue() +
                              " m: " + res[1].intValue() +
                              " d: " + res[2].intValue());
                    }
                }
            }
        }
    }

    // basic check to see that we print out eras ok
    // eventually should modify to use locale strings and formatter appropriate to coptic calendar
    public void TestEraStart() {
        CopticCalendar cal = new CopticCalendar(0, 0, 1);
        SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd, yyyy GG");
        assertEquals("Coptic Date", "Thu Jan 01, 0000 AD", fmt.format(cal));
        assertEquals("Gregorian Date", "Thu Aug 30, 0283 AD", fmt.format(cal.getTime()));
    }

    public void TestBasic() {
        CopticCalendar cal = new CopticCalendar();
        cal.clear();
        cal.set(1000, 0, 30);
        logln("1000/0/30-> " +
              cal.get(YEAR) + "/" +
              cal.get(MONTH) + "/" + 
              cal.get(DATE));
        cal.clear();
        cal.set(1, 0, 30);
        logln("1/0/30 -> " +
              cal.get(YEAR) + "/" +
              cal.get(MONTH) + "/" + 
              cal.get(DATE));
    }

    /**
     * Test limits of the Coptic calendar
     */
    public void TestLimits() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 1);
        CopticCalendar coptic = new CopticCalendar();
        if (!skipIfBeforeICU(3,9,0)) {
            doLimitsTest(coptic, null, cal.getTime());
        }
        doTheoreticalLimitsTest(coptic, true);
    }

    public void TestCoverage() {

        {
            // new CopticCalendar(TimeZone)
            CopticCalendar cal = new CopticCalendar(TimeZone.getDefault()); 
            if(cal == null){
                errln("could not create CopticCalendar with TimeZone");
            }
        }

        {
            // new CopticCalendar(ULocale)
            CopticCalendar cal = new CopticCalendar(ULocale.getDefault());
            if(cal == null){
                errln("could not create CopticCalendar with ULocale");
            }
        }
        
        {
            // new CopticCalendar(Locale)
            CopticCalendar cal = new CopticCalendar(Locale.getDefault());
            if(cal == null){
                errln("could not create CopticCalendar with Locale");
            }
        }
        
        {                                                                                       
            // new CopticCalendar(TimeZone, Locale)                                             
            CopticCalendar cal = new CopticCalendar(TimeZone.getDefault(),Locale.getDefault()); 
            if(cal == null){                                                                    
                errln("could not create CopticCalendar with TimeZone, Locale");                 
            }                                                                                   
        }                                                                                       
                                                                                                
        {                                                                                       
            // new CopticCalendar(TimeZone, ULocale)                                            
            CopticCalendar cal = new CopticCalendar(TimeZone.getDefault(),ULocale.getDefault());
            if(cal == null){                                                                    
                errln("could not create CopticCalendar with TimeZone, ULocale");                
            }                                                                                   
        }                                                                                       
        
        {
            // new CopticCalendar(Date)
            CopticCalendar cal = new CopticCalendar(new Date());
            if(cal == null){
                errln("could not create CopticCalendar with Date");
            }
        }

        {
            // new CopticCalendar(int year, int month, int date)
            CopticCalendar cal = new CopticCalendar(1997, CopticCalendar.TOUT, 1);
            if(cal == null){
                errln("could not create CopticCalendar with year,month,date");
            }
        }

        {
            // new CopticCalendar(int year, int month, int date, int hour, int minute, int second)
            CopticCalendar cal = new CopticCalendar(1997, CopticCalendar.TOUT, 1, 1, 1, 1);
            if(cal == null){
                errln("could not create CopticCalendar with year,month,date,hour,minute,second");
            }
        }
    
        {
            // data
            CopticCalendar cal = new CopticCalendar(1997, CopticCalendar.TOUT, 1);
            Date time = cal.getTime();

            String[] calendarLocales = {
                "am_ET", "gez_ET", "ti_ET"
            };

            String[] formatLocales = {
                "en", "am", "am_ET", "gez", "ti"
            };
            for (int i = 0; i < calendarLocales.length; ++i) {
                String calLocName = calendarLocales[i];
                Locale calLocale = LocaleUtility.getLocaleFromName(calLocName);
                cal = new CopticCalendar(calLocale);

                for (int j = 0; j < formatLocales.length; ++j) {
                    String locName = formatLocales[j];
                    Locale formatLocale = LocaleUtility.getLocaleFromName(locName);
                    DateFormat format = DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.FULL, formatLocale);
                    logln(calLocName + "/" + locName + " --> " + format.format(time));
                }
            }
        }
    }

    public void TestYear() {
        // Gregorian Calendar
        Calendar gCal= new GregorianCalendar();
        Date gToday=gCal.getTime();
        gCal.add(GregorianCalendar.MONTH,2);
        Date gFuture=gCal.getTime();
        DateFormat gDF = DateFormat.getDateInstance(gCal,DateFormat.FULL);
        logln("gregorian calendar: " + gDF.format(gToday) +
              " + 2 months = " + gDF.format(gFuture));

        // Coptic Calendar
        CopticCalendar cCal= new CopticCalendar();
        Date cToday=cCal.getTime();
        cCal.add(CopticCalendar.MONTH,2);
        Date cFuture=cCal.getTime();
        DateFormat cDF = DateFormat.getDateInstance(cCal,DateFormat.FULL);
        logln("coptic calendar: " + cDF.format(cToday) +
              " + 2 months = " + cDF.format(cFuture));

        // EthiopicCalendar
        EthiopicCalendar eCal= new EthiopicCalendar();
        Date eToday=eCal.getTime();
        eCal.add(EthiopicCalendar.MONTH,2); // add 2 months
        eCal.setAmeteAlemEra(false);
        Date eFuture=eCal.getTime();
        DateFormat eDF = DateFormat.getDateInstance(eCal,DateFormat.FULL);
        logln("ethiopic calendar: " + eDF.format(eToday) +
              " + 2 months = " + eDF.format(eFuture));		
    }
}
