package com.ibm.test.calendar;
import com.ibm.util.*;
import java.util.Date;
import java.util.Locale;
import com.ibm.text.*;

/**
 * @test 1.32 99/11/14
 * @bug 4031502 4035301 4040996 4051765 4059654 4061476 4070502 4071197 4071385
 * 4073929 4083167 4086724 4092362 4095407 4096231 4096539 4100311 4103271
 * 4106136 4108764 4114578 4118384 4125881 4125892 4136399 4141665 4142933
 * 4145158 4145983 4147269 4149677 4162587 4165343 4166109 4167060 4173516
 * 4174361 4177484 4197699 4209071 4288792
 */
public class CalendarRegression extends com.ibm.test.TestFmwk {

    public static void main(String[] args) throws Exception {
        new CalendarRegression().run(args);
    }

    static final String[] FIELD_NAME = {
        "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
        "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
        "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
        "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
        "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
        "JULIAN_DAY", "MILLISECONDS_IN_DAY",
    };

    /*
      Synopsis: java.sql.Timestamp constructor works wrong on Windows 95

      ==== Here is the test ==== 
      public static void main (String args[]) { 
        java.sql.Timestamp t= new java.sql.Timestamp(0,15,5,5,8,13,123456700); 
        logln("expected=1901-04-05 05:08:13.1234567"); 
        logln(" result="+t); 
      } 
      
      ==== Here is the output of the test on Solaris or NT ==== 
      expected=1901-04-05 05:08:13.1234567 
      result=1901-04-05 05:08:13.1234567 
      
      ==== Here is the output of the test on Windows95 ==== 
      expected=1901-04-05 05:08:13.1234567 
      result=1901-04-05 06:08:13.1234567 
      */

    public void Test4031502() {
        // This bug actually occurs on Windows NT as well, and doesn't
        // require the host zone to be set; it can be set in Java.
        String[] ids = TimeZone.getAvailableIDs();
        boolean bad = false;
        for (int i=0; i<ids.length; ++i) {
            TimeZone zone = TimeZone.getTimeZone(ids[i]);
            GregorianCalendar cal = new GregorianCalendar(zone);
            cal.clear();
            cal.set(1900, 15, 5, 5, 8, 13);
            if (cal.get(Calendar.HOUR) != 5) {
                logln("Fail: " + zone.getID() + " " +
                      zone.useDaylightTime() + " " +
                      cal.get(Calendar.DST_OFFSET) / (60*60*1000) + " " +
                      zone.getRawOffset() / (60*60*1000) +
                      ": HOUR = " + cal.get(Calendar.HOUR));
                bad = true;
            } else if (false) { // Change to true to debug
                logln("OK: " + zone.getID() + " " +
                      zone.useDaylightTime() + " " +
                      cal.get(Calendar.DST_OFFSET) / (60*60*1000) + " " +
                      zone.getRawOffset() / (60*60*1000) +
                      ": HOUR = " + cal.get(Calendar.HOUR));
            }
        }
        if (bad) errln("TimeZone problems with GC");
    }

    public void Test4035301() {
        GregorianCalendar c = new GregorianCalendar(98, 8, 7);
        GregorianCalendar d = new GregorianCalendar(98, 8, 7);
        if (c.after(d) ||
            c.after(c) ||
            c.before(d) ||
            c.before(c) ||
            !c.equals(c) ||
            !c.equals(d))
            errln("Fail");
    }

    public void Test4040996() {
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        Calendar calendar = new GregorianCalendar(pdt);

        calendar.set(Calendar.MONTH,3);
        calendar.set(Calendar.DAY_OF_MONTH,18);
        calendar.set(Calendar.SECOND, 30);

        logln("MONTH: " + calendar.get(Calendar.MONTH));
        logln("DAY_OF_MONTH: " + 
                           calendar.get(Calendar.DAY_OF_MONTH));
        logln("MINUTE: " + calendar.get(Calendar.MINUTE));
        logln("SECOND: " + calendar.get(Calendar.SECOND));

        calendar.add(Calendar.SECOND,6);
        //This will print out todays date for MONTH and DAY_OF_MONTH
        //instead of the date it was set to.
        //This happens when adding MILLISECOND or MINUTE also
        logln("MONTH: " + calendar.get(Calendar.MONTH));
        logln("DAY_OF_MONTH: " + 
                           calendar.get(Calendar.DAY_OF_MONTH));
        logln("MINUTE: " + calendar.get(Calendar.MINUTE));
        logln("SECOND: " + calendar.get(Calendar.SECOND));
        if (calendar.get(Calendar.MONTH) != 3 ||
            calendar.get(Calendar.DAY_OF_MONTH) != 18 ||
            calendar.get(Calendar.SECOND) != 36)
            errln("Fail: Calendar.add misbehaves");
    }

    public void Test4051765() {
        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        cal.set(Calendar.DAY_OF_WEEK, 0);
        try {
            cal.getTime();
            errln("Fail: DAY_OF_WEEK 0 should be disallowed");
        }
        catch (IllegalArgumentException e) {
            return;
        }
    }
    
    /* User error - no bug here
    public void Test4059524() {
        // Create calendar for April 10, 1997
        GregorianCalendar calendar  = new GregorianCalendar();
        // print out a bunch of interesting things
        logln("ERA: " + calendar.get(calendar.ERA));
        logln("YEAR: " + calendar.get(calendar.YEAR));
        logln("MONTH: " + calendar.get(calendar.MONTH));
        logln("WEEK_OF_YEAR: " + 
                           calendar.get(calendar.WEEK_OF_YEAR));
        logln("WEEK_OF_MONTH: " + 
                           calendar.get(calendar.WEEK_OF_MONTH));
        logln("DATE: " + calendar.get(calendar.DATE));
        logln("DAY_OF_MONTH: " + 
                           calendar.get(calendar.DAY_OF_MONTH));
        logln("DAY_OF_YEAR: " + calendar.get(calendar.DAY_OF_YEAR));
        logln("DAY_OF_WEEK: " + calendar.get(calendar.DAY_OF_WEEK));
        logln("DAY_OF_WEEK_IN_MONTH: " +
                           calendar.get(calendar.DAY_OF_WEEK_IN_MONTH));
        logln("AM_PM: " + calendar.get(calendar.AM_PM));
        logln("HOUR: " + calendar.get(calendar.HOUR));
        logln("HOUR_OF_DAY: " + calendar.get(calendar.HOUR_OF_DAY));
        logln("MINUTE: " + calendar.get(calendar.MINUTE));
        logln("SECOND: " + calendar.get(calendar.SECOND));
        logln("MILLISECOND: " + calendar.get(calendar.MILLISECOND));
        logln("ZONE_OFFSET: "
                           + (calendar.get(calendar.ZONE_OFFSET)/(60*60*1000)));
        logln("DST_OFFSET: "
                           + (calendar.get(calendar.DST_OFFSET)/(60*60*1000)));
        calendar  = new GregorianCalendar(1997,3,10); 
        calendar.getTime();                        
        logln("April 10, 1997");
        logln("ERA: " + calendar.get(calendar.ERA));
        logln("YEAR: " + calendar.get(calendar.YEAR));
        logln("MONTH: " + calendar.get(calendar.MONTH));
        logln("WEEK_OF_YEAR: " + 
                           calendar.get(calendar.WEEK_OF_YEAR));
        logln("WEEK_OF_MONTH: " + 
                           calendar.get(calendar.WEEK_OF_MONTH));
        logln("DATE: " + calendar.get(calendar.DATE));
        logln("DAY_OF_MONTH: " + 
                           calendar.get(calendar.DAY_OF_MONTH));
        logln("DAY_OF_YEAR: " + calendar.get(calendar.DAY_OF_YEAR));
        logln("DAY_OF_WEEK: " + calendar.get(calendar.DAY_OF_WEEK));
        logln("DAY_OF_WEEK_IN_MONTH: " + calendar.get(calendar.DAY_OF_WEEK_IN_MONTH));
        logln("AM_PM: " + calendar.get(calendar.AM_PM));
        logln("HOUR: " + calendar.get(calendar.HOUR));
        logln("HOUR_OF_DAY: " + calendar.get(calendar.HOUR_OF_DAY));
        logln("MINUTE: " + calendar.get(calendar.MINUTE));
        logln("SECOND: " + calendar.get(calendar.SECOND));
        logln("MILLISECOND: " + calendar.get(calendar.MILLISECOND));
        logln("ZONE_OFFSET: "
                           + (calendar.get(calendar.ZONE_OFFSET)/(60*60*1000))); // in hours
        logln("DST_OFFSET: "
                           + (calendar.get(calendar.DST_OFFSET)/(60*60*1000))); // in hours
    }
    */

    public void Test4059654() {
        GregorianCalendar gc = new GregorianCalendar();
        
        gc.set(1997, 3, 1, 15, 16, 17); // April 1, 1997

        gc.set(Calendar.HOUR, 0);
        gc.set(Calendar.AM_PM, Calendar.AM);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);

        Date cd = gc.getTime();
        Date exp = new Date(97, 3, 1, 0, 0, 0);
        if (!cd.equals(exp))
            errln("Fail: Calendar.set broken. Got " + cd + " Want " + exp);
    }

    public void Test4061476() {
        SimpleDateFormat fmt = new SimpleDateFormat("ddMMMyy", Locale.UK);
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"), 
                                                     Locale.UK);
        fmt.setCalendar(cal);
        try
            {
                Date date = fmt.parse("29MAY97");
                cal.setTime(date);
            }
        catch (Exception e) {
            System.out.print("");
        }
        cal.set(Calendar.HOUR_OF_DAY, 13);
        logln("Hour: "+cal.get(Calendar.HOUR_OF_DAY));
        cal.add(Calendar.HOUR_OF_DAY, 6);
        logln("Hour: "+cal.get(Calendar.HOUR_OF_DAY));
        if (cal.get(Calendar.HOUR_OF_DAY) != 19)
            errln("Fail: Want 19 Got " + cal.get(Calendar.HOUR_OF_DAY));
    }

    public void Test4070502() {
        Date d = getAssociatedDate(new Date(98, 0, 30));
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            errln("Fail: Want weekday Got " + d);
    }

    /**
     * Get the associated date starting from a specified date
     * NOTE: the unnecessary "getTime()'s" below are a work-around for a
     * bug in jdk 1.1.3 (and probably earlier versions also)
     * <p>
     * @param date The date to start from
     */
    public static Date getAssociatedDate(Date d) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(d);
        //cal.add(field, amount); //<-- PROBLEM SEEN WITH field = DATE,MONTH 
        // cal.getTime();  // <--- REMOVE THIS TO SEE BUG
        while (true) {
            int wd = cal.get(Calendar.DAY_OF_WEEK);
            if (wd == Calendar.SATURDAY || wd == Calendar.SUNDAY) {
                cal.add(Calendar.DATE, 1);
                // cal.getTime();
            }
            else
                break;
        }
        return cal.getTime();
    }

    public void Test4071197() {
        dowTest(false);
        dowTest(true);
    }

    void dowTest(boolean lenient) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(1997, Calendar.AUGUST, 12); // Wednesday
        // cal.getTime(); // Force update
        cal.setLenient(lenient);
        cal.set(1996, Calendar.DECEMBER, 1); // Set the date to be December 1, 1996
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int min = cal.getMinimum(Calendar.DAY_OF_WEEK);
        int max = cal.getMaximum(Calendar.DAY_OF_WEEK);
        logln(cal.getTime().toString());
        if (min != Calendar.SUNDAY || max != Calendar.SATURDAY)
            errln("FAIL: Min/max bad");
        if (dow < min || dow > max) 
            errln("FAIL: Day of week " + dow + " out of range");
        if (dow != Calendar.SUNDAY) 
            errln("FAIL: Day of week should be SUNDAY Got " + dow);
    }

    public void Test4071385() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(98, Calendar.JUNE, 24));
        cal.set(Calendar.MONTH, Calendar.NOVEMBER); // change a field
        logln(cal.getTime().toString());
        if (!cal.getTime().equals(new Date(98, Calendar.NOVEMBER, 24)))
            errln("Fail");
    }

    public void Test4073929() {
        GregorianCalendar foo1 = new GregorianCalendar(1997, 8, 27);
        foo1.add(Calendar.DAY_OF_MONTH, +1);
        int testyear = foo1.get(Calendar.YEAR);
        int testmonth = foo1.get(Calendar.MONTH);
        int testday = foo1.get(Calendar.DAY_OF_MONTH);
        if (testyear != 1997 ||
            testmonth != 8 ||
            testday != 28)
            errln("Fail: Calendar not initialized");
    }

    public void Test4083167() {
        TimeZone saveZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Date firstDate = new Date();
            Calendar cal = new GregorianCalendar();
            cal.setTime(firstDate);
            long firstMillisInDay = cal.get(Calendar.HOUR_OF_DAY) * 3600000L +
                cal.get(Calendar.MINUTE) * 60000L +
                cal.get(Calendar.SECOND) * 1000L +
                cal.get(Calendar.MILLISECOND);
            
            logln("Current time: " + firstDate.toString());

            for (int validity=0; validity<30; validity++) {
                Date lastDate = new Date(firstDate.getTime() +
                                         (long)validity*1000*24*60*60);
                cal.setTime(lastDate);
                long millisInDay = cal.get(Calendar.HOUR_OF_DAY) * 3600000L +
                    cal.get(Calendar.MINUTE) * 60000L +
                    cal.get(Calendar.SECOND) * 1000L +
                    cal.get(Calendar.MILLISECOND);
                if (firstMillisInDay != millisInDay) 
                    errln("Day has shifted " + lastDate);
            }
        }
        finally {
            TimeZone.setDefault(saveZone);
        }
    }

    public void Test4086724() {
        SimpleDateFormat date;
        TimeZone saveZone = TimeZone.getDefault();
        Locale saveLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.UK); 
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            date=new SimpleDateFormat("dd MMM yyy (zzzz) 'is in week' ww"); 
            Calendar cal=Calendar.getInstance(); 
            cal.set(1997,Calendar.SEPTEMBER,30); 
            Date now=cal.getTime(); 
            logln(date.format(now)); 
            cal.set(1997,Calendar.JANUARY,1); 
            now=cal.getTime(); 
            logln(date.format(now)); 
            cal.set(1997,Calendar.JANUARY,8); 
            now=cal.getTime(); 
            logln(date.format(now)); 
            cal.set(1996,Calendar.DECEMBER,31); 
            now=cal.getTime(); 
            logln(date.format(now)); 
        }
        finally {
            Locale.setDefault(saveLocale);
            TimeZone.setDefault(saveZone);
        }
        logln("*** THE RESULTS OF THIS TEST MUST BE VERIFIED MANUALLY ***");
    }

    public void Test4092362() {
        GregorianCalendar cal1 = new GregorianCalendar(1997, 10, 11, 10, 20, 40); 
        /*cal1.set( Calendar.YEAR, 1997 ); 
        cal1.set( Calendar.MONTH, 10 ); 
        cal1.set( Calendar.DATE, 11 ); 
        cal1.set( Calendar.HOUR, 10 ); 
        cal1.set( Calendar.MINUTE, 20 ); 
        cal1.set( Calendar.SECOND, 40 ); */

        logln( " Cal1 = " + cal1.getTime().getTime() ); 
        logln( " Cal1 time in ms = " + cal1.get(Calendar.MILLISECOND) ); 
        for( int k = 0; k < 100 ; k++ ) {
            System.out.print(""); 
        }

        GregorianCalendar cal2 = new GregorianCalendar(1997, 10, 11, 10, 20, 40); 
        /*cal2.set( Calendar.YEAR, 1997 ); 
        cal2.set( Calendar.MONTH, 10 ); 
        cal2.set( Calendar.DATE, 11 ); 
        cal2.set( Calendar.HOUR, 10 ); 
        cal2.set( Calendar.MINUTE, 20 ); 
        cal2.set( Calendar.SECOND, 40 ); */

        logln( " Cal2 = " + cal2.getTime().getTime() ); 
        logln( " Cal2 time in ms = " + cal2.get(Calendar.MILLISECOND) ); 
        if( !cal1.equals( cal2 ) ) 
            errln("Fail: Milliseconds randomized");
    }

    public void Test4095407() {
        GregorianCalendar a = new GregorianCalendar(1997,Calendar.NOVEMBER, 13);
        int dow = a.get(Calendar.DAY_OF_WEEK);
        if (dow != Calendar.THURSDAY)
            errln("Fail: Want THURSDAY Got " + dow);
    }

    public void Test4096231() {
        TimeZone GMT = TimeZone.getTimeZone("GMT");
        TimeZone PST = TimeZone.getTimeZone("PST");
        int sec = 0, min = 0, hr = 0, day = 1, month = 10, year = 1997;
                            
        Calendar cal1 = new GregorianCalendar(PST);
        cal1.setTime(new Date(880698639000L));
        int p;
        logln("PST 1 is: " + (p=cal1.get(cal1.HOUR_OF_DAY)));
        cal1.setTimeZone(GMT);
        // Issue 1: Changing the timezone doesn't change the
        //          represented time.
        int h1,h2;
        logln("GMT 1 is: " + (h1=cal1.get(cal1.HOUR_OF_DAY)));
        cal1.setTime(new Date(880698639000L));
        logln("GMT 2 is: " + (h2=cal1.get(cal1.HOUR_OF_DAY)));
        // Note: This test had a bug in it.  It wanted h1!=h2, when
        // what was meant was h1!=p.  Fixed this concurrent with fix
        // to 4177484.
        if (p == h1 || h1 != h2)
            errln("Fail: Hour same in different zones");

        Calendar cal2 = new GregorianCalendar(GMT);
        Calendar cal3 = new GregorianCalendar(PST);
        cal2.set(Calendar.MILLISECOND, 0);
        cal3.set(Calendar.MILLISECOND, 0);

        cal2.set(cal1.get(cal1.YEAR),
                 cal1.get(cal1.MONTH),
                 cal1.get(cal1.DAY_OF_MONTH),
                 cal1.get(cal1.HOUR_OF_DAY),
                 cal1.get(cal1.MINUTE),
                 cal1.get(cal1.SECOND));

        long t1,t2,t3,t4;
        logln("RGMT 1 is: " + (t1=cal2.getTime().getTime()));
        cal3.set(year, month, day, hr, min, sec);
        logln("RPST 1 is: " + (t2=cal3.getTime().getTime()));
        cal3.setTimeZone(GMT);
        logln("RGMT 2 is: " + (t3=cal3.getTime().getTime()));
        cal3.set(cal1.get(cal1.YEAR),
                 cal1.get(cal1.MONTH),
                 cal1.get(cal1.DAY_OF_MONTH),
                 cal1.get(cal1.HOUR_OF_DAY),
                 cal1.get(cal1.MINUTE),
                 cal1.get(cal1.SECOND));
        // Issue 2: Calendar continues to use the timezone in its
        //          constructor for set() conversions, regardless
        //          of calls to setTimeZone()
        logln("RGMT 3 is: " + (t4=cal3.getTime().getTime()));
        if (t1 == t2 ||
            t1 != t4 ||
            t2 != t3)
            errln("Fail: Calendar zone behavior faulty");
    }

    public void Test4096539() {
        int[] y = {31,28,31,30,31,30,31,31,30,31,30,31};

        for (int x=0;x<12;x++) {
            GregorianCalendar gc = new 
                GregorianCalendar(1997,x,y[x]);
            int m1,m2;
            log((m1=gc.get(Calendar.MONTH)+1)+"/"+
                             gc.get(Calendar.DATE)+"/"+gc.get(Calendar.YEAR)+
                             " + 1mo = ");

            gc.add(Calendar.MONTH, 1);
            logln((m2=gc.get(Calendar.MONTH)+1)+"/"+
                               gc.get(Calendar.DATE)+"/"+gc.get(Calendar.YEAR)
                               );
            int m = (m1 % 12) + 1;
            if (m2 != m)
                errln("Fail: Want " + m + " Got " + m2);
        }
        
    }

    public void Test4100311() {
        GregorianCalendar cal = (GregorianCalendar)Calendar.getInstance();
        cal.set(Calendar.YEAR, 1997);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date d = cal.getTime();             // Should be Jan 1
        logln(d.toString());
        if (cal.get(Calendar.DAY_OF_YEAR) != 1)
            errln("Fail: DAY_OF_YEAR not set");
    }

    public void Test4103271() {
        SimpleDateFormat sdf = new SimpleDateFormat(); 
        int numYears=40, startYear=1997, numDays=15; 
        String output, testDesc; 
        GregorianCalendar testCal = (GregorianCalendar)Calendar.getInstance(); 
        testCal.clear();
        sdf.setCalendar(testCal); 
        sdf.applyPattern("d MMM yyyy"); 
        boolean fail = false;
        for (int firstDay=1; firstDay<=2; firstDay++) { 
            for (int minDays=1; minDays<=7; minDays++) { 
                testCal.setMinimalDaysInFirstWeek(minDays); 
                testCal.setFirstDayOfWeek(firstDay); 
                testDesc = ("Test" + String.valueOf(firstDay) + String.valueOf(minDays)); 
                logln(testDesc + " => 1st day of week=" +
                                   String.valueOf(firstDay) +
                                   ", minimum days in first week=" +
                                   String.valueOf(minDays)); 
                for (int j=startYear; j<=startYear+numYears; j++) { 
                    testCal.set(j,11,25); 
                    for(int i=0; i<numDays; i++) { 
                        testCal.add(Calendar.DATE,1); 
                        String calWOY; 
                        int actWOY = testCal.get(Calendar.WEEK_OF_YEAR);
                        if (actWOY < 1 || actWOY > 53) {
                            Date d = testCal.getTime(); 
                            calWOY = String.valueOf(actWOY); 
                            output = testDesc + " - " + sdf.format(d) + "\t"; 
                            output = output + "\t" + calWOY; 
                            logln(output); 
                            fail = true;
                        }
                    } 
                } 
            } 
        } 

        int[] DATA = {
            3, 52, 52, 52, 52, 52, 52, 52,
                1,  1,  1,  1,  1,  1,  1,
                2,  2,  2,  2,  2,  2,  2,
            4, 52, 52, 52, 52, 52, 52, 52,
               53, 53, 53, 53, 53, 53, 53,
                1,  1,  1,  1,  1,  1,  1,
        };
        testCal.setFirstDayOfWeek(Calendar.SUNDAY);
        for (int j=0; j<DATA.length; j+=22) {
            logln("Minimal days in first week = " + DATA[j] +
                               "  Week starts on Sunday");
            testCal.setMinimalDaysInFirstWeek(DATA[j]);
            testCal.set(1997, Calendar.DECEMBER, 21);
            for (int i=0; i<21; ++i) {
                int woy = testCal.get(Calendar.WEEK_OF_YEAR);
                log(testCal.getTime() + " " + woy);
                if (woy != DATA[j + 1 + i]) {
                    log(" ERROR");
                    fail = true;
                }
                //logln();
                
                // Now compute the time from the fields, and make sure we
                // get the same answer back.  This is a round-trip test.
                Date save = testCal.getTime();
                testCal.clear();
                testCal.set(Calendar.YEAR, DATA[j+1+i] < 25 ? 1998 : 1997);
                testCal.set(Calendar.WEEK_OF_YEAR, DATA[j+1+i]);
                testCal.set(Calendar.DAY_OF_WEEK, (i%7) + Calendar.SUNDAY);
                if (!testCal.getTime().equals(save)) {
                    logln("  Parse failed: " + testCal.getTime());
                    fail= true;
                }

                testCal.setTime(save);
                testCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        // Test field disambiguation with a few special hard-coded cases.
        // This shouldn't fail if the above cases aren't failing.
        Object[] DISAM = {
            new Integer(1998), new Integer(1), new Integer(Calendar.SUNDAY),
                new Date(97, Calendar.DECEMBER, 28),
            new Integer(1998), new Integer(2), new Integer(Calendar.SATURDAY),
                new Date(98, Calendar.JANUARY, 10),
            new Integer(1998), new Integer(53), new Integer(Calendar.THURSDAY),
                new Date(98, Calendar.DECEMBER, 31),
            new Integer(1998), new Integer(53), new Integer(Calendar.FRIDAY),
                new Date(99, Calendar.JANUARY, 1),
        };
        testCal.setMinimalDaysInFirstWeek(3);
        testCal.setFirstDayOfWeek(Calendar.SUNDAY);
        for (int i=0; i<DISAM.length; i+=4) {
            int y = ((Integer)DISAM[i]).intValue();
            int woy = ((Integer)DISAM[i+1]).intValue();
            int dow = ((Integer)DISAM[i+2]).intValue();
            Date exp = (Date)DISAM[i+3];
            testCal.clear();
            testCal.set(Calendar.YEAR, y);
            testCal.set(Calendar.WEEK_OF_YEAR, woy);
            testCal.set(Calendar.DAY_OF_WEEK, dow);
            log(y + "-W" + woy +
                             "-DOW" + dow + " expect:" + exp +
                             " got:" + testCal.getTime());
            if (!testCal.getTime().equals(exp)) {
                log("  FAIL");
                fail = true;
            }
            //logln();
        }

        // Now try adding and rolling
        Object ADD = new Object();
        Object ROLL = new Object();
        Object[] ADDROLL = {
            ADD, new Integer(1), new Date(98, Calendar.DECEMBER, 25), new Date(99, Calendar.JANUARY, 1),
            ADD, new Integer(1), new Date(97, Calendar.DECEMBER, 28), new Date(98, Calendar.JANUARY, 4),
            ROLL, new Integer(1), new Date(98, Calendar.DECEMBER, 27), new Date(97, Calendar.DECEMBER, 28),
            ROLL, new Integer(1), new Date(99, Calendar.JANUARY, 2), new Date(98, Calendar.JANUARY, 3),
        };
        testCal.setMinimalDaysInFirstWeek(3);
        testCal.setFirstDayOfWeek(Calendar.SUNDAY);
        for (int i=0; i<ADDROLL.length; i+=4) {
            int amount = ((Integer)ADDROLL[i+1]).intValue();
            Date before = (Date)ADDROLL[i+2];
            Date after = (Date)ADDROLL[i+3];

            testCal.setTime(before);
            if (ADDROLL[i] == ADD) testCal.add(Calendar.WEEK_OF_YEAR, amount);
            else testCal.roll(Calendar.WEEK_OF_YEAR, amount);
            log((ADDROLL[i]==ADD?"add(WOY,":"roll(WOY,") +
                             amount + ") " + before + " => " +
                             testCal.getTime());
            if (!after.equals(testCal.getTime())) {
                logln("  exp:" + after + "  FAIL");
                fail = true;
            }
            else logln(" ok");

            testCal.setTime(after);
            if (ADDROLL[i] == ADD) testCal.add(Calendar.WEEK_OF_YEAR, -amount);
            else testCal.roll(Calendar.WEEK_OF_YEAR, -amount);
            log((ADDROLL[i]==ADD?"add(WOY,":"roll(WOY,") +
                             (-amount) + ") " + after + " => " +
                             testCal.getTime());
            if (!before.equals(testCal.getTime())) {
                logln("  exp:" + before + "  FAIL");
                fail = true;
            }
            else logln(" ok");
        }

        if (fail) errln("Fail: Week of year misbehaving");
    } 

    public void Test4106136() {
        Locale saveLocale = Locale.getDefault();
        try {
            Locale[] locales = { Locale.CHINESE, Locale.CHINA };
            for (int i=0; i<locales.length; ++i) {
                Locale.setDefault(locales[i]);
                int[] n = {
                    Calendar.getAvailableLocales().length,
                    DateFormat.getAvailableLocales().length,
                    NumberFormat.getAvailableLocales().length
                };
                for (int j=0; j<n.length; ++j) {
                    if (n[j] == 0)
                        errln("Fail: No locales for " + locales[i]);
                }
            }
        }
        finally {
            Locale.setDefault(saveLocale);
        }
    }

    public void Test4108764() {
        Date d00 = new Date(97, Calendar.MARCH, 15, 12, 00, 00);
        Date d01 = new Date(97, Calendar.MARCH, 15, 12, 00, 56);
        Date d10 = new Date(97, Calendar.MARCH, 15, 12, 34, 00);
        Date d11 = new Date(97, Calendar.MARCH, 15, 12, 34, 56);
        Date dM  = new Date(97, Calendar.JANUARY, 15, 12, 34, 56);
        Date epoch = new Date(70, Calendar.JANUARY, 1);

        Calendar cal = Calendar.getInstance(); 
        cal.setTime(d11);

        cal.clear( Calendar.MINUTE ); 
        logln(cal.getTime().toString()); 
        if (!cal.getTime().equals(d01)) {
            errln("Fail: " + d11 + " clear(MINUTE) => expect " +
                  d01 + ", got " + cal.getTime());
        }

        cal.set( Calendar.SECOND, 0 ); 
        logln(cal.getTime().toString()); 
        if (!cal.getTime().equals(d00))
            errln("Fail: set(SECOND, 0) broken");

        cal.setTime(d11);
        cal.set( Calendar.SECOND, 0 ); 
        logln(cal.getTime().toString()); 
        if (!cal.getTime().equals(d10))
            errln("Fail: set(SECOND, 0) broken #2");

        cal.clear( Calendar.MINUTE ); 
        logln(cal.getTime().toString()); 
        if (!cal.getTime().equals(d00))
            errln("Fail: clear(MINUTE) broken #2");

        cal.clear();
        logln(cal.getTime().toString());
        if (!cal.getTime().equals(epoch))
            errln("Fail: after clear() expect " + epoch + ", got " + cal.getTime());

        cal.setTime(d11);
        cal.clear( Calendar.MONTH ); 
        logln(cal.getTime().toString()); 
        if (!cal.getTime().equals(dM)) {
            errln("Fail: " + d11 + " clear(MONTH) => expect " +
                  dM + ", got " + cal.getTime());
        }
    }

    public void Test4114578() {
        int ONE_HOUR = 60*60*1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("PST"));
        long onset = new Date(98, Calendar.APRIL, 5, 1, 0).getTime() + ONE_HOUR;
        long cease = new Date(98, Calendar.OCTOBER, 25, 0, 0).getTime() + 2*ONE_HOUR;

        boolean fail = false;
        
        final int ADD = 1;
        final int ROLL = 2;

        long[] DATA = {
            // Start            Action   Amt    Expected_change
            onset - ONE_HOUR,   ADD,      1,     ONE_HOUR,
            onset,              ADD,     -1,    -ONE_HOUR,
            onset - ONE_HOUR,   ROLL,     1,     ONE_HOUR,
            onset,              ROLL,    -1,    -ONE_HOUR,
            cease - ONE_HOUR,   ADD,      1,     ONE_HOUR,
            cease,              ADD,     -1,    -ONE_HOUR,
            cease - ONE_HOUR,   ROLL,     1,     ONE_HOUR,
            cease,              ROLL,    -1,    -ONE_HOUR,
        };

        for (int i=0; i<DATA.length; i+=4) {
            Date date = new Date(DATA[i]);
            int amt = (int) DATA[i+2];
            long expectedChange = DATA[i+3];
            
            log(date.toString());
            cal.setTime(date);

            switch ((int) DATA[i+1]) {
            case ADD:
                log(" add (HOUR," + (amt<0?"":"+")+amt + ")= ");
                cal.add(Calendar.HOUR, amt);
                break;
            case ROLL:
                log(" roll(HOUR," + (amt<0?"":"+")+amt + ")= ");
                cal.roll(Calendar.HOUR, amt);
                break;
            }

            log(cal.getTime().toString());

            long change = cal.getTime().getTime() - date.getTime();
            if (change != expectedChange) {
                fail = true;
                logln(" FAIL");
            }
            else logln(" OK");
        }

        if (fail) errln("Fail: roll/add misbehaves around DST onset/cease");
    }

    /**
     * Make sure maximum for HOUR field is 11, not 12.
     */
    public void Test4118384() {
        Calendar cal = Calendar.getInstance();
        if (cal.getMaximum(Calendar.HOUR) != 11 ||
            cal.getLeastMaximum(Calendar.HOUR) != 11 ||
            cal.getActualMaximum(Calendar.HOUR) != 11)
            errln("Fail: maximum of HOUR field should be 11");
    }

    /**
     * Check isLeapYear for BC years.
     */
    public void Test4125881() {
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        DateFormat fmt = new SimpleDateFormat("MMMM d, yyyy G");
        cal.clear();
        for (int y=-20; y<=10; ++y) {
            cal.set(Calendar.ERA, y < 1 ? GregorianCalendar.BC : GregorianCalendar.AD);
            cal.set(Calendar.YEAR, y < 1 ? 1 - y : y);
            logln(y + " = " + fmt.format(cal.getTime()) + " " +
                               cal.isLeapYear(y));
            if (cal.isLeapYear(y) != ((y+40)%4 == 0))
                errln("Leap years broken");
        }
    }

    // I am disabling this test -- it is currently failing because of a bug
    // in Sun's latest change to STZ.getOffset().  I have filed a Sun bug
    // against this problem.

    // Re-enabled after 'porting' TZ and STZ from java.util to com.ibm.util.
    /**
     * Prove that GregorianCalendar is proleptic (it used to cut off
     * at 45 BC, and not have leap years before then).
     */
    public void Test4125892() {
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        DateFormat fmt = new SimpleDateFormat("MMMM d, yyyy G");
        fmt = null;
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 81); // 81 BC is a leap year (proleptically)
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DATE, 28);
        cal.add(Calendar.DATE, 1);
        if (cal.get(Calendar.DATE) != 29 ||
            !cal.isLeapYear(-80)) // -80 == 81 BC
            errln("Calendar not proleptic");
    }

    /**
     * Calendar and GregorianCalendar hashCode() methods need improvement.
     * Calendar needs a good implementation that subclasses can override,
     * and GregorianCalendar should use that implementation.
     */
    public void Test4136399() {
        /* Note: This test is actually more strict than it has to be.
         * Technically, there is no requirement that unequal objects have
         * unequal hashes.  We only require equal objects to have equal hashes.
         * It is desirable for unequal objects to have distributed hashes, but
         * there is no hard requirement here.
         *
         * In this test we make assumptions about certain attributes of calendar
         * objects getting represented in the hash, which need not always be the
         * case (although it does work currently with the given test). */
        Calendar a = Calendar.getInstance();
        Calendar b = (Calendar)a.clone();
        if (a.hashCode() != b.hashCode()) {
            errln("Calendar hash code unequal for cloned objects");
        }

        b.setMinimalDaysInFirstWeek(7 - a.getMinimalDaysInFirstWeek());
        if (a.hashCode() == b.hashCode()) {
            errln("Calendar hash code ignores minimal days in first week");
        }
        b.setMinimalDaysInFirstWeek(a.getMinimalDaysInFirstWeek());

        b.setFirstDayOfWeek((a.getFirstDayOfWeek() % 7) + 1); // Next day
        if (a.hashCode() == b.hashCode()) {
            errln("Calendar hash code ignores first day of week");
        }
        b.setFirstDayOfWeek(a.getFirstDayOfWeek());

        b.setLenient(!a.isLenient());
        if (a.hashCode() == b.hashCode()) {
            errln("Calendar hash code ignores lenient setting");
        }
        b.setLenient(a.isLenient());
        
        // Assume getTimeZone() returns a reference, not a clone
        // of a reference -- this is true as of this writing
        b.getTimeZone().setRawOffset(a.getTimeZone().getRawOffset() + 60*60*1000);
        if (a.hashCode() == b.hashCode()) {
            errln("Calendar hash code ignores zone");
        }
        b.getTimeZone().setRawOffset(a.getTimeZone().getRawOffset());

        GregorianCalendar c = new GregorianCalendar();
        GregorianCalendar d = (GregorianCalendar)c.clone();
        if (c.hashCode() != d.hashCode()) {
            errln("GregorianCalendar hash code unequal for clones objects");
        }
        Date cutover = c.getGregorianChange();
        d.setGregorianChange(new Date(cutover.getTime() + 24*60*60*1000));
        if (c.hashCode() == d.hashCode()) {
            errln("GregorianCalendar hash code ignores cutover");
        }        
    }

    /**
     * GregorianCalendar.equals() ignores cutover date
     */
    public void Test4141665() {
        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendar cal2 = (GregorianCalendar)cal.clone();
        Date cut = cal.getGregorianChange();
        Date cut2 = new Date(cut.getTime() + 100*24*60*60*1000L); // 100 days later
        if (!cal.equals(cal2)) {
            errln("Cloned GregorianCalendars not equal");
        }
        cal2.setGregorianChange(cut2);
        if (cal.equals(cal2)) {
            errln("GregorianCalendar.equals() ignores cutover");
        }
    }
    
    /**
     * Bug states that ArrayIndexOutOfBoundsException is thrown by GregorianCalendar.roll()
     * when IllegalArgumentException should be.
     */
    public void Test4142933() {
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            calendar.roll(-1, true);
            errln("Test failed, no exception trown");
        }
        catch (IllegalArgumentException e) {
            // OK: Do nothing
            // logln("Test passed");
            System.out.print("");
        }
        catch (Exception e) {
            errln("Test failed. Unexpected exception is thrown: " + e);
            e.printStackTrace();
        } 
    }

    /**
     * GregorianCalendar handling of Dates Long.MIN_VALUE and Long.MAX_VALUE is
     * confusing; unless the time zone has a raw offset of zero, one or the
     * other of these will wrap.  We've modified the test given in the bug
     * report to therefore only check the behavior of a calendar with a zero raw
     * offset zone.
     */
    public void Test4145158() {
        GregorianCalendar calendar = new GregorianCalendar();

        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

        calendar.setTime(new Date(Long.MIN_VALUE));
        int year1 = calendar.get(Calendar.YEAR);
        int era1 = calendar.get(Calendar.ERA);
        
        calendar.setTime(new Date(Long.MAX_VALUE));
        int year2 = calendar.get(Calendar.YEAR);
        int era2 = calendar.get(Calendar.ERA);
        
        if (year1 == year2 && era1 == era2) {
            errln("Fail: Long.MIN_VALUE or Long.MAX_VALUE wrapping around");
        }
    }

    /**
     * Maximum value for YEAR field wrong.
     */
    public void Test4145983() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date[] DATES = { new Date(Long.MAX_VALUE), new Date(Long.MIN_VALUE) };
        for (int i=0; i<DATES.length; ++i) {
            calendar.setTime(DATES[i]);
            int year = calendar.get(Calendar.YEAR);
            int maxYear = calendar.getMaximum(Calendar.YEAR);
            if (year > maxYear) {
                errln("Failed for "+DATES[i].getTime()+" ms: year=" +
                      year + ", maxYear=" + maxYear);
            }
        }
    }

    /**
     * This is a bug in the validation code of GregorianCalendar.  As reported,
     * the bug seems worse than it really is, due to a bug in the way the bug
     * report test was written.  In reality the bug is restricted to the DAY_OF_YEAR
     * field. - liu 6/29/98
     */
    public void Test4147269() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setLenient(false);
        Date date = new Date(1996-1900, Calendar.JANUARY, 3); // Arbitrary date
        for (int field = 0; field < calendar.getFieldCount(); field++) {
            calendar.setTime(date);
            // Note: In the bug report, getActualMaximum() was called instead
            // of getMaximum() -- this was an error.  The validation code doesn't
            // use getActualMaximum(), since that's too costly.
            int max = calendar.getMaximum(field);
            int value = max+1;
            calendar.set(field, value); 
            try {
                calendar.getTime(); // Force time computation
                // We expect an exception to be thrown. If we fall through
                // to the next line, then we have a bug.
                errln("Test failed with field " + FIELD_NAME[field] +
                      ", date before: " + date +
                      ", date after: " + calendar.getTime() +
                      ", value: " + value + " (max = " + max +")");
            } catch (IllegalArgumentException e) {
                System.out.print("");
            } 
        }
    }

    /**
     * Reported bug is that a GregorianCalendar with a cutover of Date(Long.MAX_VALUE)
     * doesn't behave as a pure Julian calendar.
     * CANNOT REPRODUCE THIS BUG
     */
    public void Test4149677() {
        TimeZone[] zones = { TimeZone.getTimeZone("GMT"),
                             TimeZone.getTimeZone("PST"),
                             TimeZone.getTimeZone("EAT") };
        for (int i=0; i<zones.length; ++i) {
            GregorianCalendar calendar = new GregorianCalendar(zones[i]);

            // Make sure extreme values don't wrap around
            calendar.setTime(new Date(Long.MIN_VALUE));
            if (calendar.get(Calendar.ERA) != GregorianCalendar.BC) {
                errln("Fail: Long.MIN_VALUE ms has an AD year");
            }
            calendar.setTime(new Date(Long.MAX_VALUE));
            if (calendar.get(Calendar.ERA) != GregorianCalendar.AD) {
                errln("Fail: Long.MAX_VALUE ms has a BC year");
            }

            calendar.setGregorianChange(new Date(Long.MAX_VALUE));
            // to obtain a pure Julian calendar
            
            boolean is100Leap = calendar.isLeapYear(100);
            if (!is100Leap) {
                errln("test failed with zone " + zones[i].getID());
                errln(" cutover date is Calendar.MAX_DATE");
                errln(" isLeapYear(100) returns: " + is100Leap);
            }
        }
    }

    /**
     * Calendar and Date HOUR broken.  If HOUR is out-of-range, Calendar
     * and Date classes will misbehave.
     */
    public void Test4162587() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        TimeZone.setDefault(tz);
        GregorianCalendar cal = new GregorianCalendar(tz);
        Date d;
        
        for (int i=0; i<5; ++i) {
            if (i>0) logln("---");

            cal.clear();
            cal.set(1998, Calendar.APRIL, 5, i, 0);
            d = cal.getTime();
            String s0 = d.toString();
            logln("0 " + i + ": " + s0);

            cal.clear();
            cal.set(1998, Calendar.APRIL, 4, i+24, 0);
            d = cal.getTime();
            String sPlus = d.toString();
            logln("+ " + i + ": " + sPlus);

            cal.clear();
            cal.set(1998, Calendar.APRIL, 6, i-24, 0);
            d = cal.getTime();
            String sMinus = d.toString();
            logln("- " + i + ": " + sMinus);

            if (!s0.equals(sPlus) || !s0.equals(sMinus)) {
                errln("Fail: All three lines must match");
            }
        }
    }

    /**
     * Adding 12 months behaves differently from adding 1 year
     */
    public void Test4165343() {
        GregorianCalendar calendar = new GregorianCalendar(1996, Calendar.FEBRUARY, 29);
        Date start = calendar.getTime();
        logln("init date: " + start);
        calendar.add(Calendar.MONTH, 12); 
        Date date1 = calendar.getTime();
        logln("after adding 12 months: " + date1);
        calendar.setTime(start);
        calendar.add(Calendar.YEAR, 1);
        Date date2 = calendar.getTime();
        logln("after adding one year : " + date2);
        if (date1.equals(date2)) {
            logln("Test passed");
        } else {
            errln("Test failed");
        }
    }

    /**
     * GregorianCalendar.getActualMaximum() does not account for first day of week.
     */
    public void Test4166109() {
        /* Test month:
         *
         *      March 1998
         * Su Mo Tu We Th Fr Sa
         *  1  2  3  4  5  6  7
         *  8  9 10 11 12 13 14
         * 15 16 17 18 19 20 21
         * 22 23 24 25 26 27 28
         * 29 30 31
         */
        boolean passed = true;
        int field = Calendar.WEEK_OF_MONTH;

        GregorianCalendar calendar = new GregorianCalendar(Locale.US);
        calendar.set(1998, Calendar.MARCH, 1);
        calendar.setMinimalDaysInFirstWeek(1);
        logln("Date:  " + calendar.getTime());

        int firstInMonth = calendar.get(Calendar.DAY_OF_MONTH);

        for (int firstInWeek = Calendar.SUNDAY; firstInWeek <= Calendar.SATURDAY; firstInWeek++) {
            calendar.setFirstDayOfWeek(firstInWeek);
            int returned = calendar.getActualMaximum(field);
            int expected = (31 + ((firstInMonth - firstInWeek + 7)% 7) + 6) / 7;

            logln("First day of week = " + firstInWeek +
                  "  getActualMaximum(WEEK_OF_MONTH) = " + returned +
                  "  expected = " + expected +
                  ((returned == expected) ? "  ok" : "  FAIL"));

            if (returned != expected) {
                passed = false;
            }
        }
        if (!passed) {
            errln("Test failed");
        }
    }

    /**
     * Calendar.getActualMaximum(YEAR) works wrong.
     */
    public void Test4167060() {
        int field = Calendar.YEAR;
        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy G",
                                                 Locale.US);

        GregorianCalendar calendars[] = {
            new GregorianCalendar(100, Calendar.NOVEMBER, 1),
            new GregorianCalendar(-99 /*100BC*/, Calendar.JANUARY, 1),
            new GregorianCalendar(1996, Calendar.FEBRUARY, 29),
        };

        String[] id = { "Hybrid", "Gregorian", "Julian" };

        for (int k=0; k<3; ++k) {
            logln("--- " + id[k] + " ---");

            for (int j=0; j<calendars.length; ++j) {
                GregorianCalendar calendar = calendars[j];
                if (k == 1) {
                    calendar.setGregorianChange(new Date(Long.MIN_VALUE));
                } else if (k == 2) {
                    calendar.setGregorianChange(new Date(Long.MAX_VALUE));
                }

                format.setCalendar((Calendar)calendar.clone());

                Date dateBefore = calendar.getTime();

                int maxYear = calendar.getActualMaximum(field);
                logln("maxYear: " + maxYear + " for " + format.format(calendar.getTime()));
                logln("date before: " + format.format(dateBefore));

                int years[] = {2000, maxYear-1, maxYear, maxYear+1};

                for (int i = 0; i < years.length; i++) {
                    boolean valid = years[i] <= maxYear;
                    calendar.set(field, years[i]);
                    Date dateAfter = calendar.getTime();
                    int newYear = calendar.get(field);
                    calendar.setTime(dateBefore); // restore calendar for next use

                    logln(" Year " + years[i] + (valid? " ok " : " bad") +
                          " => " + format.format(dateAfter));
                    if (valid && newYear != years[i]) {
                        errln("  FAIL: " + newYear + " should be valid; date, month and time shouldn't change");
                    } else if (!valid && newYear == years[i]) {
                        // We no longer require strict year maxima.  That is, the calendar
                        // algorithm may work for values > the stated maximum.
                        //errln("  FAIL: " + newYear + " should be invalid");
                        logln("  Note: " + newYear + " > maximum, but still valid");
                    }
                }
            }
        }
    }

    /**
     * Calendar.roll broken
     * This bug relies on the TimeZone bug 4173604 to also be fixed.
     */
    public void Test4173516() {
        int fieldsList[][] = {
            { 1997, Calendar.FEBRUARY,  1, 10, 45, 15, 900 },
            { 1999, Calendar.DECEMBER, 22, 23, 59, 59, 999 }
        };
        int limit = 40;
        GregorianCalendar cal = new GregorianCalendar();

        cal.setTime(new Date(0));
        cal.roll(Calendar.HOUR,  0x7F000000);
        cal.roll(Calendar.HOUR, -0x7F000000);
        if (cal.getTime().getTime() != 0) {
            errln("Hour rolling broken");
        }

        for (int op=0; op<2; ++op) {
            logln("Testing GregorianCalendar " +
                  (op==0 ? "add" : "roll"));
            for (int field=0; field < cal.getFieldCount(); ++field) {
                if (field != Calendar.ZONE_OFFSET &&
                    field != Calendar.DST_OFFSET) {
                    for (int j=0; j<fieldsList.length; ++j) {
                        int fields[] = fieldsList[j];
                        cal.clear();
                        cal.set(fields[0], fields[1], fields[2],
                                fields[3], fields[4], fields[5]);
                        cal.set(Calendar.MILLISECOND, fields[6]);
                        for (int i = 0; i < 2*limit; i++) {
                            if (op == 0) {
                                cal.add(field, i < limit ? 1 : -1);
                            } else {
                                cal.roll(field, i < limit ? 1 : -1);
                            }
                        }
                        if (cal.get(Calendar.YEAR) != fields[0] ||
                            cal.get(Calendar.MONTH) != fields[1] ||
                            cal.get(Calendar.DATE) != fields[2] ||
                            cal.get(Calendar.HOUR_OF_DAY) != fields[3] ||
                            cal.get(Calendar.MINUTE) != fields[4] ||
                            cal.get(Calendar.SECOND) != fields[5] ||
                            cal.get(Calendar.MILLISECOND) != fields[6]) {
                            errln("Field " + field +
                                  " (" + FIELD_NAME[field] +
                                  ") FAIL, expected " +
                                  fields[0] +
                                  "/" + (fields[1] + 1) +
                                  "/" + fields[2] +
                                  " " + fields[3] +
                                  ":" + fields[4] +
                                  ":" + fields[5] +
                                  "." + fields[6] +
                                  ", got " + cal.get(Calendar.YEAR) +
                                  "/" + (cal.get(Calendar.MONTH) + 1) +
                                  "/" + cal.get(Calendar.DATE) +
                                  " " + cal.get(Calendar.HOUR_OF_DAY) +
                                  ":" + cal.get(Calendar.MINUTE) +
                                  ":" + cal.get(Calendar.SECOND) +
                                  "." + cal.get(Calendar.MILLISECOND));
                            cal.clear();
                            cal.set(fields[0], fields[1], fields[2],
                                    fields[3], fields[4], fields[5]);
                            cal.set(Calendar.MILLISECOND, fields[6]);
                            logln("Start date: " + cal.get(Calendar.YEAR) +
                                  "/" + (cal.get(Calendar.MONTH) + 1) +
                                  "/" + cal.get(Calendar.DATE) +
                                  " " + cal.get(Calendar.HOUR_OF_DAY) +
                                  ":" + cal.get(Calendar.MINUTE) +
                                  ":" + cal.get(Calendar.SECOND) +
                                  "." + cal.get(Calendar.MILLISECOND));
                            long prev = cal.getTime().getTime();
                            for (int i = 0; i < 2*limit; i++) {
                                if (op == 0) {
                                    cal.add(field, i < limit ? 1 : -1);
                                } else {
                                    cal.roll(field, i < limit ? 1 : -1);
                                }
                                long t = cal.getTime().getTime();
                                long delta = t - prev;
                                prev = t;
                                logln((op == 0 ? "add(" : "roll(") + FIELD_NAME[field] +
                                      (i < limit ? ", +1) => " : ", -1) => ") +
                                      cal.get(Calendar.YEAR) +
                                      "/" + (cal.get(Calendar.MONTH) + 1) +
                                      "/" + cal.get(Calendar.DATE) +
                                      " " + cal.get(Calendar.HOUR_OF_DAY) +
                                      ":" + cal.get(Calendar.MINUTE) +
                                      ":" + cal.get(Calendar.SECOND) +
                                      "." + cal.get(Calendar.MILLISECOND) +
                                      " delta=" + delta + " ms");
                            }
                        }
                    }
                }
            }
        }
    }

    public void Test4174361() {
        GregorianCalendar calendar = new GregorianCalendar(1996, 1, 29);

        calendar.add(Calendar.MONTH, 10); 
        Date date1 = calendar.getTime();
        date1 = null;
        int d1 = calendar.get(Calendar.DAY_OF_MONTH);

        calendar = new GregorianCalendar(1996, 1, 29);
        calendar.add(Calendar.MONTH, 11); 
        Date date2 = calendar.getTime();
        date2 = null;
        int d2 = calendar.get(Calendar.DAY_OF_MONTH);

        if (d1 != d2) {
            errln("adding months to Feb 29 broken");
        }
    }

    /**
     * Calendar does not update field values when setTimeZone is called.
     */
    public void Test4177484() {
        TimeZone PST = TimeZone.getTimeZone("PST");
        TimeZone EST = TimeZone.getTimeZone("EST");

        Calendar cal = Calendar.getInstance(PST, Locale.US);
        cal.clear();
        cal.set(1999, 3, 21, 15, 5, 0); // Arbitrary
        int h1 = cal.get(Calendar.HOUR_OF_DAY);
        cal.setTimeZone(EST);
        int h2 = cal.get(Calendar.HOUR_OF_DAY);
        if (h1 == h2) {
            errln("FAIL: Fields not updated after setTimeZone");
        }

        // getTime() must NOT change when time zone is changed.
        // getTime() returns zone-independent time in ms.
        cal.clear();
        cal.setTimeZone(PST);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        Date pst10 = cal.getTime();
        cal.setTimeZone(EST);
        Date est10 = cal.getTime();
        if (!pst10.equals(est10)) {
            errln("FAIL: setTimeZone changed time");
        }
    }

    /**
     * Week of year is wrong at the start and end of the year.
     */
    public void Test4197699() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        DateFormat fmt = new SimpleDateFormat("E dd MMM yyyy  'DOY='D 'WOY='w");
        fmt.setCalendar(cal);

        int[] DATA = {
            2000,  Calendar.JANUARY,   1,   52,
            2001,  Calendar.DECEMBER,  31,  1,
        };

        for (int i=0; i<DATA.length; ) {
            cal.set(DATA[i++], DATA[i++], DATA[i++]);
            int expWOY = DATA[i++];
            int actWOY = cal.get(Calendar.WEEK_OF_YEAR);
            if (expWOY == actWOY) {
                logln("Ok: " + fmt.format(cal.getTime()));
            } else {
                errln("FAIL: " + fmt.format(cal.getTime())
                      + ", expected WOY=" + expWOY);
                cal.add(Calendar.DATE, -8);
                for (int j=0; j<14; ++j) {
                    cal.add(Calendar.DATE, 1);
                    logln(fmt.format(cal.getTime()));
                }
            }
        }
    }

    /**
     * Calendar DAY_OF_WEEK_IN_MONTH fields->time broken.  The problem
     * is in the field disambiguation code in GregorianCalendar.  This
     * code is supposed to choose the most recent set of fields
     * among the following:
     *
     *   MONTH + DAY_OF_MONTH
     *   MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
     *   MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
     *   DAY_OF_YEAR
     *   WEEK_OF_YEAR + DAY_OF_WEEK
     */
    public void Test4209071() {
        Calendar cal = Calendar.getInstance(Locale.US);

        // General field setting test
        int Y = 1995 - 1900;

        Object[] FIELD_DATA = {
            // Add new test cases as needed.

            // 0
            new int[] {}, new Date(Y, Calendar.JANUARY, 1),
            // 1
            new int[] { Calendar.MONTH, Calendar.MARCH },
            new Date(Y, Calendar.MARCH, 1),
            // 2
            new int[] { Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY },
            new Date(Y, Calendar.JANUARY, 4),
            // 3
            new int[] { Calendar.DAY_OF_WEEK, Calendar.THURSDAY,
                        Calendar.DAY_OF_MONTH, 18, },
            new Date(Y, Calendar.JANUARY, 18),
            // 4
            new int[] { Calendar.DAY_OF_MONTH, 18,
                        Calendar.DAY_OF_WEEK, Calendar.THURSDAY, },
            new Date(Y, Calendar.JANUARY, 18),
            // 5  (WOM -1 is in previous month)
            new int[] { Calendar.DAY_OF_MONTH, 18,
                        Calendar.WEEK_OF_MONTH, -1,
                        Calendar.DAY_OF_WEEK, Calendar.THURSDAY, },
            new Date(Y-1, Calendar.DECEMBER, 22),
            // 6
            new int[] { Calendar.DAY_OF_MONTH, 18,
                        Calendar.WEEK_OF_MONTH, 4,
                        Calendar.DAY_OF_WEEK, Calendar.THURSDAY, },
            new Date(Y, Calendar.JANUARY, 26),
            // 7  (DIM -1 is in same month)
            new int[] { Calendar.DAY_OF_MONTH, 18,
                        Calendar.DAY_OF_WEEK_IN_MONTH, -1,
                        Calendar.DAY_OF_WEEK, Calendar.THURSDAY, },
            new Date(Y, Calendar.JANUARY, 26),
            // 8
            new int[] { Calendar.WEEK_OF_YEAR, 9,
                        Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY, },
            new Date(Y, Calendar.MARCH, 1),
            // 9
            new int[] { Calendar.MONTH, Calendar.OCTOBER,
                        Calendar.DAY_OF_WEEK_IN_MONTH, 1,
                        Calendar.DAY_OF_WEEK, Calendar.FRIDAY, },
            new Date(Y, Calendar.OCTOBER, 6),
            // 10
            new int[] { Calendar.MONTH, Calendar.OCTOBER,
                        Calendar.WEEK_OF_MONTH, 2,
                        Calendar.DAY_OF_WEEK, Calendar.FRIDAY, },
            new Date(Y, Calendar.OCTOBER, 13),
            // 11
            new int[] { Calendar.MONTH, Calendar.OCTOBER,
                        Calendar.DAY_OF_MONTH, 15,
                        Calendar.DAY_OF_YEAR, 222, },
            new Date(Y, Calendar.AUGUST, 10),
            // 12
            new int[] { Calendar.DAY_OF_WEEK, Calendar.THURSDAY,
                        Calendar.MONTH, Calendar.DECEMBER, },
            new Date(Y, Calendar.DECEMBER, 7),
        };

        for (int i=0; i<FIELD_DATA.length; i+=2) {
            int[] fields = (int[]) FIELD_DATA[i];
            Date exp = (Date) FIELD_DATA[i+1];
            
            cal.clear();
            cal.set(Calendar.YEAR, Y + 1900);
            for (int j=0; j<fields.length; j+=2) {
                cal.set(fields[j], fields[j+1]);
            }
            
            Date act = cal.getTime();
            if (!act.equals(exp)) {
                errln("FAIL: Test " + (i/2) + " got " + act +
                      ", want " + exp +
                      " (see test/java/util/Calendar/CalendarRegression.java");
            }
        }

        // Test specific failure reported in bug
        Object[] DATA = {
            new Integer(1), new Date(1997-1900, Calendar.JANUARY, 5),
            new Integer(4), new Date(1997-1900, Calendar.JANUARY, 26),
            new Integer(8), new Date(1997-1900, Calendar.FEBRUARY, 23),
            new Integer(-1), new Date(1997-1900, Calendar.JANUARY, 26),
            new Integer(-4), new Date(1997-1900, Calendar.JANUARY, 5),
            new Integer(-8), new Date(1996-1900, Calendar.DECEMBER, 8),
        };
        for (int i=0; i<DATA.length; i+=2) {
            cal.clear();
            cal.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                    ((Number) DATA[i]).intValue());
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.YEAR, 1997);
            Date actual = cal.getTime();
            if (!actual.equals(DATA[i+1])) {
                errln("FAIL: Sunday " + DATA[i] +
                      " of Jan 1997 -> " + actual +
                      ", want " + DATA[i+1]);
            }
        }
    }

    /**
     * WEEK_OF_YEAR computed incorrectly.  A failure of this test can indicate
     * a problem in several different places in the 
     */
    public void Test4288792() throws Exception 
    {
	TimeZone savedTZ = TimeZone.getDefault();
	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	GregorianCalendar cal = new GregorianCalendar();
        
	for (int i = 1900; i < 2100; i++) {
	    for (int j1 = 1; j1 <= 7; j1++) {
		// Loop for MinimalDaysInFirstWeek: 1..7
		for (int j = Calendar.SUNDAY; j <= Calendar.SATURDAY; j++) {
		    // Loop for FirstDayOfWeek: SUNDAY..SATURDAY
		    cal.clear();
		    cal.setMinimalDaysInFirstWeek(j1);
		    cal.setFirstDayOfWeek(j);
                    // Set the calendar to the first day of the last week
                    // of the year.  This may overlap some of the start of
                    // the next year; that is, the last week of 1999 may
                    // include some of January 2000.  Use the add() method
                    // to advance through the week.  For each day, call
                    // get(WEEK_OF_YEAR).  The result should be the same
                    // for the whole week.  Note that a bug in
                    // getActualMaximum() will break this test.
		    cal.set(Calendar.YEAR, i);
		    int maxWeek = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
		    cal.set(Calendar.WEEK_OF_YEAR, maxWeek);
		    cal.set(Calendar.DAY_OF_WEEK, j);
		    for (int k = 1; k < 7; k++) {
			cal.add(Calendar.DATE, 1);
			int WOY = cal.get(Calendar.WEEK_OF_YEAR);
			if (WOY != maxWeek) {
			    errln(cal.getTime() + ",got=" + WOY
				  + ",expected=" + maxWeek 
				  + ",min=" + j1 + ",first=" + j);
			}
		    }
                    // Now advance the calendar one more day.  This should
                    // put it at the first day of week 1 of the next year.
		    cal.add(Calendar.DATE, 1);
		    int WOY = cal.get(Calendar.WEEK_OF_YEAR);
		    if (WOY != 1) {
			errln(cal.getTime() + ",got=" + WOY 
			      + ",expected=1,min=" + j1 + ",first" + j);
		    }
		}
	    }
	}
	TimeZone.setDefault(savedTZ);
    }

    /**
     * Test fieldDifference().
     */
    public void TestJ438() throws Exception {
        int DATA[] = {
            2000, Calendar.JANUARY, 20,   2010, Calendar.JUNE, 15,
            2010, Calendar.JUNE, 15,      2000, Calendar.JANUARY, 20,
            1964, Calendar.SEPTEMBER, 7,  1999, Calendar.JUNE, 4,
            1999, Calendar.JUNE, 4,       1964, Calendar.SEPTEMBER, 7,
        };
        Calendar cal = Calendar.getInstance(Locale.US);
        for (int i=0; i<DATA.length; i+=6) {
            int y1 = DATA[i];
            int m1 = DATA[i+1];
            int d1 = DATA[i+2];
            int y2 = DATA[i+3];
            int m2 = DATA[i+4];
            int d2 = DATA[i+5];

            cal.clear();
            cal.set(y1, m1, d1);
            Date date1 = cal.getTime();
            cal.set(y2, m2, d2);
            Date date2 = cal.getTime();

            cal.setTime(date1);
            int dy = cal.fieldDifference(date2, Calendar.YEAR);
            int dm = cal.fieldDifference(date2, Calendar.MONTH);
            int dd = cal.fieldDifference(date2, Calendar.DATE);

            logln("" + date2 + " - " + date1 + " = " +
                  dy + "y " + dm + "m " + dd + "d");

            cal.setTime(date1);
            cal.add(Calendar.YEAR, dy);
            cal.add(Calendar.MONTH, dm);
            cal.add(Calendar.DATE, dd);
            Date date22 = cal.getTime();
            if (!date2.equals(date22)) {
                errln("FAIL: " + date1 + " + " +
                      dy + "y " + dm + "m " + dd + "d = " +
                      date22 + ", exp " + date2);
            } else {
                logln("Ok: " + date1 + " + " +
                      dy + "y " + dm + "m " + dd + "d = " +
                      date22);
            }
        }
    }

    /**
     * Set behavior of DST_OFFSET field.  ICU4J Jitterbug 9.
     */
    public void TestJ9() {
        int HOURS = 60*60*1000;
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("PST"),
                                             Locale.US);

        final int END_FIELDS = 0x1234;

        int[] DATA = {
            // With no explicit ZONE/DST expect 12:00 am
            Calendar.MONTH, Calendar.JUNE,
            END_FIELDS,
            0, 0, // expected hour, min

            // Normal ZONE/DST for June 1 Pacific is 8:00/1:00
            Calendar.MONTH, Calendar.JUNE,
            Calendar.ZONE_OFFSET, -8*HOURS,
            Calendar.DST_OFFSET, HOURS,
            END_FIELDS,
            0, 0, // expected hour, min

            // With ZONE/DST of 8:00/0:30 expect time of 12:30 am
            Calendar.MONTH, Calendar.JUNE,
            Calendar.ZONE_OFFSET, -8*HOURS,
            Calendar.DST_OFFSET, HOURS/2,
            END_FIELDS,
            0, 30, // expected hour, min

            // With ZONE/DST of 8:00/UNSET expect time of 1:00 am
            Calendar.MONTH, Calendar.JUNE,
            Calendar.ZONE_OFFSET, -8*HOURS,
            END_FIELDS,
            1, 0, // expected hour, min

            // With ZONE/DST of UNSET/0:30 expect 4:30 pm (day before)
            Calendar.MONTH, Calendar.JUNE,
            Calendar.DST_OFFSET, HOURS/2,
            END_FIELDS,
            16, 30, // expected hour, min
        };

        for (int i=0; i<DATA.length; ) {
            int start = i;
            cal.clear();

            // Set fields
            while (DATA[i] != END_FIELDS) {
                cal.set(DATA[i++], DATA[i++]);
            }
            ++i; // skip over END_FIELDS

            // Get hour/minute
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int m = cal.get(Calendar.MINUTE);

            // Check
            if (h != DATA[i] || m != DATA[i+1]) {
                errln("Fail: expected " + DATA[i] + ":" + DATA[i+1] +
                      ", got " + h + ":" + m + " after:");
                while (DATA[start] != END_FIELDS) {
                    logln("set(" + FIELD_NAME[DATA[start++]] +
                          ", " + DATA[start++] + ");");
                }
            }

            i += 2; // skip over expected hour, min
        }
    }

    /**
     * DateFormat class mistakes date style and time style as follows:
     * - DateFormat.getDateTimeInstance takes date style as time
     * style, and time style as date style
     * - If a Calendar is passed to
     * DateFormat.getDateInstance, it returns time instance
     * - If a Calendar
     * is passed to DateFormat.getTimeInstance, it returns date instance
     */
    public void TestDateFormatFactoryJ26() {
        TimeZone zone = TimeZone.getDefault();
        try {
            Locale loc = Locale.US;
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            Date date = new Date(2001, Calendar.APRIL, 5, 17, 43, 53);
            Calendar cal = Calendar.getInstance(loc);
            Object[] DATA = {
                DateFormat.getDateInstance(DateFormat.SHORT, loc),
                "DateFormat.getDateInstance(DateFormat.SHORT, loc)",
                "4/5/01",

                DateFormat.getTimeInstance(DateFormat.SHORT, loc),
                "DateFormat.getTimeInstance(DateFormat.SHORT, loc)",
                "5:43 PM",

                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, loc),
                "DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, loc)",
                "Friday, April 5, 3901 5:43 PM",

                DateFormat.getDateInstance(cal, DateFormat.SHORT, loc),
                "DateFormat.getDateInstance(cal, DateFormat.SHORT, loc)",
                "4/5/01",

                DateFormat.getTimeInstance(cal, DateFormat.SHORT, loc),
                "DateFormat.getTimeInstance(cal, DateFormat.SHORT, loc)",
                "5:43 PM",

                DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.SHORT, loc),
                "DateFormat.getDateTimeInstance(cal, DateFormat.FULL, DateFormat.SHORT, loc)",
                "Friday, April 5, 3901 5:43 PM",
            
                cal.getDateTimeFormat(DateFormat.SHORT, DateFormat.FULL, loc),
                "cal.getDateTimeFormat(DateFormat.SHORT, DateFormat.FULL, loc)",
                "4/5/01 5:43:53 PM PST",

                cal.getDateTimeFormat(DateFormat.FULL, DateFormat.SHORT, loc),
                "cal.getDateTimeFormat(DateFormat.FULL, DateFormat.SHORT, loc)",
                "Friday, April 5, 3901 5:43 PM",
            };
            for (int i=0; i<DATA.length; i+=3) {
                DateFormat df = (DateFormat) DATA[i];
                String desc = (String) DATA[i+1];
                String exp = (String) DATA[i+2];
                String got = df.format(date);
                if (got.equals(exp)) {
                    logln("Ok: " + desc + " => " + got);
                } else {
                    errln("FAIL: " + desc + " => " + got + ", expected " + exp);
                }
            }
        } finally {
            TimeZone.setDefault(zone);
        }
    }
}

//eof
