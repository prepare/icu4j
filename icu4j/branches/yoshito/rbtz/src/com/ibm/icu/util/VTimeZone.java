/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.icu.impl.Grego;
import com.ibm.icu.impl.ICUTimeZone;

/**
 * VTimeZone is a class implementing RFC2445 VTIMEZONE.  You can create a
 * VTimeZone instance from a time zone ID supported by ICU TimeZone.  With
 * the VTimeZone instance created from the ID, you can write out the rule
 * in RFC2445 VTIMEZONE format.  Also, you can create a VTimeZone instance
 * from RFC2445 VTIMEZONE data stream, which allows you to calculate time
 * zone offset by the rules defined by the data.
 * 
 * @draft ICU 3.8
 * @provisional This API might change or be removed in a future release.
 */
public class VTimeZone extends ICUTimeZone {

    private static final long serialVersionUID = 1L; //TODO

    private TimeZone tz;
    private List vtzlines;

    /**
     * Create a VTimeZone instance by the time zone ID.
     * 
     * @param tzid The time zone ID, such as America/New_York
     * @return A VTimeZone initialized by the time zone ID, or null
     * when the ID is unknown.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static VTimeZone create(String tzid) {
        VTimeZone vtz = new VTimeZone();
        vtz.tz = TimeZone.getTimeZone(tzid);
        vtz.setID(tzid);

        return vtz;
    }
    
    /**
     * Create a VTimeZone instance by RFC2445 VTIMEZONE data
     * 
     * @param reader The Reader for VTIMEZONE data input stream
     * @return A VTimeZone initialized by the VTIMEZONE data or
     * null if failed to load the rule from the VTIMEZONE data.
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public static VTimeZone create(Reader reader) {
        VTimeZone vtz = new VTimeZone();
        if (vtz.load(reader)) {
            return vtz;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getOffset(int, int, int, int, int, int)
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int milliseconds) {
        return tz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#getRawOffset()
     */
    public int getRawOffset() {
        return tz.getRawOffset();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#inDaylightTime(java.util.Date)
     */
    public boolean inDaylightTime(Date date) {
        return tz.inDaylightTime(date);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#setRawOffset(int)
     */
    public void setRawOffset(int offsetMillis) {
        tz.setRawOffset(offsetMillis);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.TimeZone#useDaylightTime()
     */
    public boolean useDaylightTime() {
        return tz.useDaylightTime();
    }

    /**
     * Writes RFC2445 VTIMEZONE data for this time zone
     * 
     * @param writer A Writer used for the output
     * @return true if the VTIMEZONE data is successfully written
     * @throws IOException
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean write(Writer writer) throws IOException {
//        BufferedWriter bw = new BufferedWriter(writer);
//        if (vtzlines != null) {
//            try {
//                Iterator it = vtzlines.iterator();
//                while (it.hasNext()) {
//                    bw.write((String)it.next());
//                }
//                bw.flush();
//            }
//            catch (IOException ioe) {
//                return false;
//            }
//        }
//        // if this VTIMEZONE was not created from VTIMEZONE data..
//        writeHeader(writer);
//
//        TimeZoneRule[] rules = ((HasTimeZoneRules)tz).getTimeZoneRules();
//=       if (rules == null || rules.size() == 0) {
//    ----------------------------------------------------------
//            writeBasic(writer, getTZ().getRawOffset(), null, null);
//        }
//        else {
//            Iterator it = rules.iterator();
//            while (it.hasNext()) {
//                ZoneRule rule = (ZoneRule)it.next();
//                int from = resolveFromOffset(rule, rules, getTZ().getRawOffset());
//                boolean success = writeRule(writer, rule, from, null, null);
//                if (!success) {
//                    return false;
//                }
//            }            
//        }
//        writeFooter(writer);
//        writer.flush();
        return true;

    }

    /**
     * Writes RFC2445 VTIMEZONE data applicalbe for the specified time
     * range
     * 
     * @param writer The Writer used for the output
     * @param start The start time
     * @param end The end time
     * @return true if the VTIMEZONE data is successfully written
     * @throws IOException
     * 
     * @draft ICU 3.8
     * @provisional This API might change or be removed in a future release.
     */
    public boolean write(Writer writer, long start, long end) throws IOException {
        //TODO
        return false;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getNextTransition(long, boolean)
     */
    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        return ((HasTimeZoneRules)tz).getNextTransition(base, inclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getPreviousTransition(long, boolean)
     */
    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        return ((HasTimeZoneRules)tz).getPreviousTransition(base, inclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.impl.ICUTimeZone#hasEquivalentTransitions(com.ibm.icu.util.TimeZone, long, long)
     */
    public boolean hasEquivalentTransitions(TimeZone other, long start, long end) {
        return ((HasTimeZoneRules)tz).hasEquivalentTransitions(other, start, end);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.HasTimeZoneRules#getTimeZoneRules()
     */
    public TimeZoneRule[] getTimeZoneRules() {
        return ((HasTimeZoneRules)tz).getTimeZoneRules();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.impl.ICUTimeZone#getTimeZoneRules(long)
     */
    public TimeZoneRule[] getTimeZoneRules(long start) {
        return ((HasTimeZoneRules)tz).getTimeZoneRules(start);
    }

    // private stuff ------------------------------------------------------

    /* Hide the constructor */
    private VTimeZone() {
    }

    // Default DST savings
    private static final int DEF_DSTSAVINGS = 60*60*1000; // 1 hour

    // minumum/max
    private static final long MIN_TIME = Long.MIN_VALUE;
    private static final long MAX_TIME = Long.MAX_VALUE;

    // RFC2445 VTIMEZONE TOKENS
    private static final String ICAL_BEGIN_VTIMEZONE = "BEGIN:VTIMEZONE";
    private static final String ICAL_END_VTIMEZONE = "END:VTIMEZONE";
    private static final String ICAL_BEGIN = "BEGIN";
    private static final String ICAL_END = "END";
    private static final String ICAL_VTIMEZONE = "VTIMEZONE";
    private static final String ICAL_TZID = "TZID";
    private static final String ICAL_STANDARD = "STANDARD";
    private static final String ICAL_DAYLIGHT = "DAYLIGHT";
    private static final String ICAL_DTSTART = "DTSTART";
    private static final String ICAL_TZOFFSETFROM = "TZOFFSETFROM";
    private static final String ICAL_TZOFFSETTO = "TZOFFSETTO";
    private static final String ICAL_RDATE = "RDATE";
    private static final String ICAL_RRULE = "RRULE";
    private static final String ICAL_TZNAME = "TZNAME";

    private static final String ICAL_FREQ = "FREQ";
    private static final String ICAL_UNTIL = "UNTIL";
    private static final String ICAL_YEARLY = "YEARLY";
    private static final String ICAL_BYMONTH = "BYMONTH";
    private static final String ICAL_BYDAY = "BYDAY";
    private static final String ICAL_BYMONTHDAY = "BYMONTHDAY";

    private static final String[] ICAL_DOW_NAMES = 
    {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

    /*
     * Read the input stream to locate the VTIMEZONE block and
     * parse the contents to initialze this VTimeZone object.
     * The reader skips other RFC2445 message headers.  After
     * the parse is completed, the reader points at the beginning
     * of the header field just after the end of VTIMEZONE block.
     * When VTIMEZONE block is found and this object is successfully
     * initialized by the rules described in the data, this method
     * returns true.  Otherwise, returns false.
     */
    private boolean load(Reader reader) {
        // Read VTIMEZONE block into string array
        try {
            vtzlines = new LinkedList();
            boolean eol = false;
            boolean start = false;
            boolean success = false;
            StringBuffer line = new StringBuffer();
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    // end of file
                    if (start && line.indexOf(ICAL_END_VTIMEZONE) == 0) {
                        vtzlines.add(line.toString());
                        success = true;
                    }
                    break;
                }
                if (ch == 0x0D) {
                    // CR, must be followed by LF by the definition in RFC2445
                    continue;
                }

                if (eol) {
                    if (ch != 0x09 && ch != 0x20) {
                        // NOT followed by TAB/SP -> new line
                        if (start) {
                            if (line.length() > 0) {
                                vtzlines.add(line.toString());
                            }
                        }
                        line.setLength(0);
                        if (ch != 0x0A) {
                            line.append((char)ch);
                        }
                    }
                    eol = false;
                }
                else {
                    if (ch == 0x0A) {
                        // LF
                        eol = true;
                        if (start) {
                            if (line.indexOf(ICAL_END_VTIMEZONE) == 0) {
                                vtzlines.add(line.toString());
                                success = true;
                                break;
                            }
                        }
                        else {
                            if (line.indexOf(ICAL_BEGIN_VTIMEZONE) == 0) {
                                vtzlines.add(line.toString());
                                line.setLength(0);
                                start = true;
                                eol = false;
                            }
                        }
                    }
                    else {
                        line.append((char)ch);
                    }
                }
            }
            if (!success) {
                return false;
            }
        }
        catch (IOException ioe) {
            return false;
        }
        return parse();
    }

    // parser state
    private static final int INI = 0;   // Initial state
    private static final int VTZ = 1;   // In VTIMEZONE
    private static final int TZI = 2;   // In STANDARD or DAYLIGHT
    private static final int END = 3;   // End state
    private static final int ERR = 4;   // Error state

    /*
     * Parse VTIMEZONE data and create a RuleBasedTimeZone
     */
    private boolean parse() {
        if (vtzlines == null || vtzlines.size() == 0) {
            return false;
        }

        // timezone ID
        String tzid = null;

        int state = INI;
        boolean dst = false;    // current zone type
        String from = null;     // current zone from offset
        String to = null;       // current zone offset
        String tzname = null;   // current zone name
        String dtstart = null;  // current zone starts
        boolean isRRULE = false;// true if the rule is described by RRULE
        List dates = null;      // list of RDATE or RRULE strings
        List rules = new LinkedList();   // rule list
        int initialOffset = 0;  // initial offset
        long firstStart = MAX_TIME; // the earliest rule start time

        Iterator it = vtzlines.iterator();

        while (it.hasNext()) {
            String line = (String)it.next();

            int valueSep = line.indexOf(":");
            if (valueSep < 0) {
                continue;
            }
            String name = line.substring(0, valueSep);
            String value = line.substring(valueSep + 1);

            switch (state) {
            case INI:
                if (name.equals(ICAL_BEGIN) && value.equals(ICAL_VTIMEZONE)) {
                    state = VTZ;
                }
                else {
                    // remove leading lines
                    it.remove();
                }
                break;
            case VTZ:
                if (name.equals(ICAL_TZID)) {
                    tzid = value;
                }
                else if (name.equals(ICAL_BEGIN)) {
                    boolean isDST = value.equals(ICAL_DAYLIGHT);
                    if (value.equals(ICAL_STANDARD) || isDST) {
                        // tzid must be ready at this point
                        if (tzid == null) {
                            state = ERR;
                            break;
                        }
                        // initialize current zone properties
                        dates = null;
                        isRRULE = false;
                        from = null;
                        to = null;
                        tzname = null;
                        dst = isDST;
                        state = TZI;
                    }
                    else {
                        // BEGIN property other than STANDARD/DAYLIGHT
                        // must not be there.
                        state = ERR;
                        break;
                    }
                }
                else if (name.equals(ICAL_END) /* && value.equals(ICAL_VTIMEZONE) */) {
                    state = END;
                }
                break;

            case TZI:
                if (name.equals(ICAL_DTSTART)) {
                    dtstart = value;
                }
                else if (name.equals(ICAL_TZNAME)) {
                    tzname = value;
                }
                else if (name.equals(ICAL_TZOFFSETFROM)) {
                    from = value;
                }
                else if (name.equals(ICAL_TZOFFSETTO)) {
                    to = value;
                }
                else if (name.equals(ICAL_RDATE)) {
                    // RDATE mixed with RRULE is not supported
                    if (isRRULE) {
                        state = ERR;
                        break;
                    }
                    if (dates == null) {
                        dates = new LinkedList();
                    }
                    // RDATE value may contain multiple date delimited
                    // by comma
                    StringTokenizer st = new StringTokenizer(value, ",");
                    while (st.hasMoreTokens()) {
                        String date = st.nextToken();
                        dates.add(date);
                    }
                }
                else if (name.equals(ICAL_RRULE)) {
                    // RRULE mixed with RDATE is not supported
                    if (!isRRULE && dates != null) {
                        state = ERR;
                        break;
                    }
                    else if (dates == null) {
                        dates = new LinkedList();
                    }
                    isRRULE = true;
                    dates.add(value);
                }
                else if (name.equals(ICAL_END)) {
                    // Mandatory properties
                    if (dtstart == null || from == null || to == null || dates == null) {
                        state = ERR;
                        break;
                    }
                    // if tzname is not available, create one from tzid
                    if (tzname == null) {
                        tzname = getDefaultTZName(tzid, dst);
                    }

                    // create a time zone rule
                    TimeZoneRule rule = null;
                    int fromOffset = 0;
                    int toOffset = 0;
                    int rawOffset = 0;
                    int dstSavings = 0;
                    long start = 0;
                    try {
                        // Parse TZOFFSETFROM/TZOFFSETTO
                        fromOffset = offsetStrToMillis(from);
                        toOffset = offsetStrToMillis(to);
                        
                        if (dst) {
                            // If daylight, use the previous offset as rawoffset if positive
                            if (toOffset - fromOffset > 0) {
                                rawOffset = fromOffset;
                                dstSavings = toOffset - fromOffset;
                            } else {
                                // This is rare case..  just use 1 hour DST savings
                                rawOffset = toOffset - DEF_DSTSAVINGS;
                                dstSavings = DEF_DSTSAVINGS;                                
                            }
                        } else {
                            rawOffset = toOffset;
                            dstSavings = 0;
                        }

                        // start time
                        start = parseICalDateTimeString(dtstart, fromOffset);

                        // Create the rule
                        Date actualStart = null;
                        if (isRRULE) {
                            rule = createRuleByRRULE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                            if (rule != null) {
                                actualStart = ((AnnualTimeZoneRule)rule).getFirstStart(fromOffset, 0);
                            }
                        }
                        else {
                            rule = createRuleByRDATE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                            if (rule != null) {
                                actualStart = ((TimeArrayTimeZoneRule)rule).getFirstStart();
                            }
                        }
                        if (actualStart.getTime() < firstStart) {
                            // save from offset information for the earliest rule
                            firstStart = actualStart.getTime();
                            initialOffset = fromOffset;
                        }
                    } catch (IllegalArgumentException iae) {
                        // bad format - rule == null..
                    }

                    if (rule == null) {
                        state = ERR;
                        break;
                    }
                    rules.add(rule);
                    state = VTZ;
                }
                break;

            case END:
                // just removing trailing lines
                it.remove();
            }

            if (state == ERR) {
                vtzlines = null;
                return false;
            }
        }

        // Must have at least one rule
        if (rules.size() == 0) {
            return false;
        }

        // Create a initial rule
        InitialTimeZoneRule initialRule = new InitialTimeZoneRule(getDefaultTZName(tzid, false),
                initialOffset, 0);

        // Finally, create the RuleBasedTimeZone
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tzid, initialRule);
        Iterator rit = rules.iterator();
        while(rit.hasNext()) {
            rbtz.addTransitionRule((TimeZoneRule)rit.next());
        }
        tz = rbtz;
        return true;
    }

    /*
     * Create a default TZNAME from TZID
     */
    private static String getDefaultTZName(String tzid, boolean isDST) {
        if (isDST) {
            return tzid + "(DST)";
        }
        return tzid + "(STD)";
    }

    /*
     * Create a TimeZoneRule by the RRULE definition
     */
    private static TimeZoneRule createRuleByRRULE(String tzname,
            int rawOffset, int dstSavings, long start, List dates, int fromOffset) {
        if (dates == null || dates.size() == 0) {
            return null;
        }
        // Parse the first rule
        String rrule = (String)dates.get(0);

        long until[] = new long[1];
        int[] ruleFields = parseRRULE(rrule, until);
        if (ruleFields == null) {
            // Invalid RRULE
            return null;
        }

        int month = ruleFields[0];
        int dayOfWeek = ruleFields[1];
        int nthDayOfWeek = ruleFields[2];
        int dayOfMonth = ruleFields[3];

        if (dates.size() == 1) {
            // No more rules
            if (ruleFields.length > 4) {
                // Multiple BYMONTHDAY values

                if (ruleFields.length != 10 || month == -1 || dayOfWeek == 0) {
                    // Only support the rule using 7 continuous days
                    // BYMONTH and BYDAY must be set at the same time
                    return null;
                }
                int firstDay = 31; // max possible number of dates in a month
                int days[] = new int[7];
                for (int i = 0; i < 7; i++) {
                    days[i] = ruleFields[3 + i];
                    // Resolve negative day numbers.  A negative day number should
                    // not be used in February, but if we see such case, we use 28
                    // as the base.
                    days[i] = days[i] > 0 ? days[i] : Grego.monthLength(2007 /*any non-leap year*/, month) + days[i] + 1;
                    firstDay = days[i] < firstDay ? days[i] : firstDay;
                }
                // Make sure days are continuous
                for (int i = 1; i < 7; i++) {
                    boolean found = false;
                    for (int j = 0; j < 7; j++) {
                        if (days[j] == firstDay + i) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // days are not continuous
                        return null;
                    }
                }
                // Use DOW_GEQ_DOM rule with firstDay as the start date
                dayOfMonth = firstDay;
            }
        }
        else {
            // Check if BYMONTH + BYMONTHDAY + BYDAY rule with multiple RRULE lines.
            // Otherwise, not supported.
            if (month == -1 || dayOfWeek == 0 || dayOfMonth == 0) {
                // This is not the case
                return null;
            }
            // Parse the rest of rules if number of rules is not exceeding 7.
            // We can only support 7 continuous days starting from a day of month.
            if (dates.size() > 7) {
                return null;
            }

            // Note: To check valid date range across multiple rule is a little
            // bit complicated.  For now, this code is not doing strict range
            // checking across month boundary

            int earliestMonth = month;
            int daysCount = ruleFields.length - 3;
            int earliestDay = 31;
            for (int i = 0; i < daysCount; i++) {
                int dom = ruleFields[3 + i];
                dom = dom > 0 ? dom : Grego.monthLength(2007/*any non-leap year*/, month) + dom + 1;
                earliestDay = dom < earliestDay ? dom : earliestDay;
            }

            int anotherMonth = -1;
            for (int i = 1; i < dates.size(); i++) {
                rrule = (String)dates.get(i);
                long[] unt = new long[1];
                int[] fields = parseRRULE(rrule, unt);

                // If UNTIL is newer than previous one, use the one
                if (unt[0] > until[0]) {
                    until = unt;
                }
                
                // Check if BYMONTH + BYMONTHDAY + BYDAY rule
                if (fields[0] == -1 || fields[1] == 0 || fields[3] == 0) {
                    return null;
                }
                // Count number of BYMONTHDAY
                int count = fields.length - 3;
                if (daysCount + count > 7) {
                    // We cannot support BYMONTHDAY more than 7
                    return null;
                }
                // Check if the same BYDAY is used.  Otherwise, we cannot
                // support the rule
                if (fields[1] != dayOfWeek) {
                    return null;
                }
                // Check if the month is same or right next to the primary month
                if (fields[0] != month) {
                    if (anotherMonth == -1) {
                        int diff = fields[0] - month;
                        if (diff == -11 || diff == -1) {
                            // Previous month
                            anotherMonth = fields[0];
                            earliestMonth = anotherMonth;
                            // Reset earliest day
                            earliestDay = 31;
                        }
                        else if (diff == 11 || diff == 1) {
                            // Next month
                            anotherMonth = fields[0];
                        }
                        else {
                            // The day range cannot exceed more than 2 months
                            return null;
                        }
                    }
                    else if (fields[0] != month && fields[0] != anotherMonth) {
                        // The day range cannot exceed more than 2 months
                        return null;
                    }
                }
                // If ealier month, go through days to find the earliest day
                if (fields[0] == earliestMonth) {
                    for (int j = 0; j < count; j++) {
                        int dom = fields[3 + j];
                        dom = dom > 0 ? dom : Grego.monthLength(2007/*any non-leap year*/, month) + dom + 1;
                        earliestDay = dom < earliestDay ? dom : earliestDay;
                    }
                }
                daysCount += count;
            }
            if (daysCount != 7) {
                // Number of BYMONTHDAY entries must be 7
                return null;
            }
            month = earliestMonth;
            dayOfMonth = earliestDay;
        }

        // Calculate start/end year and missing fields
        long startDay = Grego.timeToDay(start + fromOffset);
        int[] dfields = Grego.dayToFields(startDay, null);
        int startYear = dfields[0];
        if (month == -1) {
            // If MYMONTH is not set, use the month of DTSTART
            month = dfields[1];
        }
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth == 0) {
            // If only YEARLY is set, use the day of DTSTART as BYMONTHDAY
            dayOfMonth = dfields[2];
        }
        int timeInDay = (int)(start + fromOffset - startDay*(24*60*60*1000));

        int endYear = AnnualTimeZoneRule.MAX_YEAR;
        if (until[0] != MIN_TIME) {
            Grego.dayToFields(Grego.timeToDay(until[0]), dfields);
            endYear = dfields[0];
        }

        // Create the AnnualDateTimeRule
        AnnualDateTimeRule adtr = null;
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            // Day in month rule, for example, 15th day in the month
            adtr = new AnnualDateTimeRule(month, dayOfMonth, timeInDay, AnnualDateTimeRule.WALL_TIME);
        }
        else if (dayOfWeek != 0 && nthDayOfWeek != 0 && dayOfMonth == 0) {
            // Nth day of week rule, for example, last Sunday
            adtr = new AnnualDateTimeRule(month, nthDayOfWeek, dayOfWeek, timeInDay, AnnualDateTimeRule.WALL_TIME);
        }
        else if (dayOfWeek != 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            // First day of week after day of month rule, for example,
            // first Sunday after 15th day in the month
            adtr = new AnnualDateTimeRule(month, dayOfMonth, dayOfWeek, true, timeInDay, AnnualDateTimeRule.WALL_TIME);
        }
        else {
            // RRULE attributes are insufficient
            return null;
        }

        return new AnnualTimeZoneRule(tzname, rawOffset, dstSavings, adtr, startYear, endYear);
    }

    /*
     * Parse individual RRULE
     * 
     * On return -
     * 
     * int[0] month calculated by BYMONTH - 1, or -1 when not found
     * int[1] day of week in BYDAY, or 0 when not found
     * int[2] day of week ordinal number in BYDAY, or 0 when not found
     * int[i >= 3] day of month, which could be multiple values, or 0 when not found
     * 
     *  or
     * 
     * null on any error cases, for exmaple, FREQ=YEARLY is not available
     * 
     * When UNTIL attribute is available, the time will be set to until[0],
     * otherwise, MIN_TIME
     */
    private static int[] parseRRULE(String rrule, long[] until) {
        int month = -1;
        int dayOfWeek = 0;
        int nthDayOfWeek = 0;
        int[] dayOfMonth = null;

        long untilTime = MIN_TIME;
        boolean yearly = false;
        boolean parseError = false;
        StringTokenizer st= new StringTokenizer(rrule, ";");

        while (st.hasMoreTokens()) {
            String attr, value;
            String prop = st.nextToken();
            int sep = prop.indexOf("=");
            if (sep != -1) {
                attr = prop.substring(0, sep);
                value = prop.substring(sep + 1);
            }
            else {
                parseError = true;
                break;
            }

            if (attr.equals(ICAL_FREQ)) {
                // only support YEARLY frequency type
                if (value.equals(ICAL_YEARLY)) {
                    yearly = true;
                }
                else {
                    parseError = true;
                    break;                        
                }
            }
            else if (attr.equals(ICAL_UNTIL)) {
                // ISO8601 UTC format, for example, "20060315T020000Z"
                try {
                    untilTime = parseICalDateTimeString(value, 0);
                } catch (IllegalArgumentException iae) {
                    parseError = true;
                    break;
                }
            }
            else if (attr.equals(ICAL_BYMONTH)) {
                // Note: BYMONTH may contain multiple months, but only single month make sense for
                // VTIMEZONE property.
                if (value.length() > 2) {
                    parseError = true;
                    break;
                }
                try {
                    month = Integer.parseInt(value) - 1;
                    if (month < 0 || month >= 12) {
                        parseError = true;
                    }
                }
                catch (NumberFormatException nfe) {
                    parseError = true;
                    break;
                }
            }
            else if (attr.equals(ICAL_BYDAY)) {
                // Note: BYDAY may contain multiple day of week separated by comma.  It is unlikely used for
                // VTIMEZONE property.  We do not support the case.

                // 2-letter format is used just for representing a day of week, for example, "SU" for Sunday
                // 3 or 4-letter format is used for represeinging Nth day of week, for example, "-1SA" for last Saturday
                int length = value.length();
                if (length < 2 || length > 4) {
                    parseError = true;
                    break;
                }
                if (length > 2) {
                    // Nth day of week
                    int sign = 1;
                    if (value.charAt(0) == '+') {
                        sign = 1;
                    }
                    else if (value.charAt(0) == '-') {
                        sign = -1;
                    }
                    else if (length == 4) {
                        parseError = true;
                        break;
                    }
                    try {
                        int n = Integer.parseInt(value.substring(length - 3, length - 2));
                        if (n == 0 || n > 4) {
                            parseError = true;
                            break;
                        }
                        nthDayOfWeek = n * sign;
                    }
                    catch(NumberFormatException nfe) {
                        parseError = true;
                        break;
                    }
                    value = value.substring(length - 2);
                }
                int wday;
                for (wday = 0; wday < ICAL_DOW_NAMES.length; wday++) {
                    if (value.equals(ICAL_DOW_NAMES[wday])) {
                        break;
                    }
                }
                if (wday < ICAL_DOW_NAMES.length) {
                    // Sunday(1) - Saturday(7)
                    dayOfWeek = wday + 1;
                }
                else {
                    parseError = true;
                    break;
                }
            }
            else if (attr.equals(ICAL_BYMONTHDAY)) {
                // Note: BYMONTHDAY may contain multiple days delimitted by comma
                //
                // A value of BYMONTHDAY could be negative, for example, -1 means
                // the last day in a month
                StringTokenizer days = new StringTokenizer(value, ",");
                int count = days.countTokens();
                dayOfMonth = new int[count];
                int index = 0;
                while(days.hasMoreTokens()) {
                    try {
                        dayOfMonth[index++] = Integer.parseInt(days.nextToken());
                    }
                    catch (NumberFormatException nfe) {
                        parseError = true;
                        break;
                    }
                }
            }
        }

        if (parseError) {
            return null;
        }
        if (!yearly) {
            // FREQ=YEARLY must be set
            return null;
        }

        until[0] = untilTime;

        int[] results;
        if (dayOfMonth == null) {
            results = new int[4];
            results[3] = 0;
        }
        else {
            results = new int[3 + dayOfMonth.length];
            for (int i = 0; i < dayOfMonth.length; i++) {
                results[3 + i] = dayOfMonth[i];
            }
        }
        results[0] = month;
        results[1] = dayOfWeek;
        results[2] = nthDayOfWeek;
        return results;
    }
    
    /*
     * Create a TimeZoneRule by the RDATE definition
     */
    private static TimeZoneRule createRuleByRDATE(String tzname,
            int rawOffset, int dstSavings, long start, List dates, int fromOffset) {
        if (dates == null || dates.size() == 0) {
            return null;
        }
        // Create an array of transition times
        long[] times = new long[dates.size()];
        Iterator it = dates.iterator();
        int idx = 0;
        try {
            while(it.hasNext()) {
                times[idx++] = parseICalDateTimeString((String)it.next(), fromOffset);
            }
        } catch (IllegalArgumentException iae) {
            return null;
        }
        return new TimeArrayTimeZoneRule(tzname, rawOffset, dstSavings, times);
    }

    private static final int MILLIS_PER_DAY = 24*60*60*1000;
    private static final int MILLIS_PER_HOUR = 60*60*1000;
    private static final int MILLIS_PER_MINUTE = 60*1000;
    private static final int MILLIS_PER_SECOND = 1000;

    /*
     * Parse RFC2445 Date-Time form #1 DATE WITH LOCAL TIME and
     * #2 DATE WITH UTC TIME
     */
    private static long parseICalDateTimeString(String str, int offset) {
        int year = 0, month = 0, day = 0, hour = 0, min = 0, sec = 0;
        boolean isUTC = false;
        boolean isValid = false;
        do {
            if (str == null) {
                break;
            }

            int length = str.length();
            if (length != 15 && length != 16) {
                // FORM#1 15 characters, such as "20060317T142115"
                // FORM#2 16 characters, such as "20060317T142115Z"
                break;
            }
            if (str.charAt(8) != 'T') {
                // charcter "T" must be used for separating date and time
                break;
            }
            if (length == 16) {
                if (str.charAt(15) != 'Z') {
                    // invalid format
                    break;
                }
                isUTC = true;
            }

            try {
                year = Integer.parseInt(str.substring(0, 4));
                month = Integer.parseInt(str.substring(4, 6)) - 1;  // 0-based
                day = Integer.parseInt(str.substring(6, 8));
                hour = Integer.parseInt(str.substring(9, 11));
                min = Integer.parseInt(str.substring(11, 13));
                sec = Integer.parseInt(str.substring(13, 15));
            }
            catch (NumberFormatException nfe) {
                break;
            }

            // check valid range
            int maxDayOfMonth = Grego.monthLength(year, month);
            if (year < 0 || month < 0 || month > 11 || day < 1 || day > maxDayOfMonth ||
                    hour < 0 || hour >= 24 || min < 0 || min >= 60 || sec < 0 || sec >= 60) {
                break;
            }

            isValid = true;
        } while(false);

        if (!isValid) {
            throw new IllegalArgumentException("Invalid date time string format");
        }
        // Calculate the time
        long time = Grego.fieldsToDay(year, month, day) * MILLIS_PER_DAY;
        time += (hour*MILLIS_PER_HOUR + min*MILLIS_PER_MINUTE + sec*MILLIS_PER_SECOND);
        if (!isUTC) {
            time -= offset;
        }
        return time;
    }

    /*
     * Convert RFC2445 utc-offset string to milliseconds
     */
    private static int offsetStrToMillis(String str) {
        boolean isValid = false;
        int sign = 0, hour = 0, min = 0, sec = 0;

        do {
            if (str == null) {
                break;
            }
            int length = str.length();
            if (length != 5 && length != 7) {
                // utf-offset must be 5 or 7 characters
                break;
            }
            // sign
            char s = str.charAt(0);
            if (s == '+') {
                sign = 1;
            }
            else if (s == '-') {
                sign = -1;
            }
            else {
                // utf-offset must start with "+" or "-"
                break;
            }

            try {
                hour = Integer.parseInt(str.substring(1, 3));
                min = Integer.parseInt(str.substring(3, 5));
                if (length == 7) {
                    sec = Integer.parseInt(str.substring(5, 7));
                }
            }
            catch (NumberFormatException nfe) {
                break;
            }
            isValid = true;
        } while(false);

        if (!isValid) {
            throw new IllegalArgumentException("Bad offset string");
        }
        int millis = sign * ((hour * 60 + min) * 60 + sec) * 1000;
        return millis;
    }
}
