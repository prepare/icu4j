/*
 *******************************************************************************
 * Copyright (C) 2001, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/format/DateFormatMiscTests.java,v $ 
 * $Date: 2002/02/16 03:05:08 $ 
 * $Revision: 1.5 $
 *
 *****************************************************************************************
 */

/** 
 * Port From:   ICU4C v1.8.1 : format : DateFormatMiscTests
 * Source File: $ICU4CRoot/source/test/intltest/miscdtfm.cpp
 **/

package com.ibm.icu.dev.test.format;

import com.ibm.icu.lang.*;
import com.ibm.icu.text.*;
import com.ibm.icu.util.*;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Locale;
import java.util.Date;

/** 
 * Performs miscellaneous tests for DateFormat, SimpleDateFormat, DateFormatSymbols
 **/
public class DateFormatMiscTests extends com.ibm.icu.dev.test.TestFmwk {

    public static void main(String[] args) throws Exception{
        new DateFormatMiscTests().run(args);
    }
    
    /*
     * @bug 4097450
     */
    public void Test4097450() {
        //
        // Date parse requiring 4 digit year.
        //
        String dstring[] = {
            "97", "1997", "97", "1997", "01", "2001", "01", "2001",
             "1", "1", "11", "11", "111", "111"}; 
    
        String dformat[] = 
            {
                "yy", "yy", "yyyy", "yyyy", "yy", "yy", "yyyy", "yyyy", 
                "yy", "yyyy", "yy", "yyyy", "yy", "yyyy"};         
    
        SimpleDateFormat formatter;
        SimpleDateFormat resultFormatter = new SimpleDateFormat("yyyy");
        logln("Format\tSource\tResult");
        logln("-------\t-------\t-------");
        for (int i = 0; i < dstring.length ; i++) {
            log(dformat[i] + "\t" + dstring[i] + "\t");
            formatter = new SimpleDateFormat(dformat[i]);
            try {
                StringBuffer str = new StringBuffer("");
                FieldPosition pos = new FieldPosition(0);
                logln(resultFormatter.format(formatter.parse(dstring[i]), str, pos).toString()); 
            }
            catch (ParseException exception) {
                errln("exception --> " + exception);
            }
            logln("");
        }
    }
    
    /* @Bug 4099975
     * SimpleDateFormat constructor SimpleDateFormat(String, DateFormatSymbols)
     * should clone the DateFormatSymbols parameter
     */
    public void Test4099975new() {
        Date d = new Date();
        //test SimpleDateFormat Constructor
        {
            DateFormatSymbols symbols = new DateFormatSymbols(Locale.US);
            SimpleDateFormat df = new SimpleDateFormat("E hh:mm", symbols);
            SimpleDateFormat dfClone = (SimpleDateFormat) df.clone();
            
            logln(df.toLocalizedPattern());
            String s0 = df.format(d);
            String s_dfClone = dfClone.format(d);
            
            symbols.setLocalPatternChars("abcdefghijklmonpqr"); // change value of field
            logln(df.toLocalizedPattern());
            String s1 = df.format(d);
            
            if (!s1.equals(s0) || !s1.equals(s_dfClone)) {
                errln("Constructor: the formats are not equal");
            }
            if (!df.equals(dfClone)) {
                errln("The Clone Object does not equal with the orignal source");
            }
        }
        //test SimpleDateFormat.setDateFormatSymbols()
        {
            DateFormatSymbols symbols = new DateFormatSymbols(Locale.US);
            SimpleDateFormat df = new SimpleDateFormat("E hh:mm");
            df.setDateFormatSymbols(symbols);
            SimpleDateFormat dfClone = (SimpleDateFormat) df.clone();
            
            logln(df.toLocalizedPattern());
            String s0 = df.format(d);
            String s_dfClone = dfClone.format(d);
            
            symbols.setLocalPatternChars("abcdefghijklmonpqr"); // change value of field
            logln(df.toLocalizedPattern());
            String s1 = df.format(d);
            
            if (!s1.equals(s0) || !s1.equals(s_dfClone)) {
                errln("setDateFormatSymbols: the formats are not equal");
            }
            if (!df.equals(dfClone)) {
                errln("The Clone Object does not equal with the orignal source");
            }
        }
    }
    
    /*
     * @bug 4117335
     */
    public void Test4117335() {
        //char bcC[] = {0x7D00, 0x5143, 0x524D};
        String bc = "\u7D00\u5143\u524D";
        String ad = "\u897f\u66a6";
        //char adC[] = {0x897F, 0x66A6};
        String jstLong = "\u65e5\u672c\u6a19\u6e96\u6642";
        //char jstLongC[] = {0x65e5, 0x672c, 0x6a19, 0x6e96, 0x6642}; //The variable is never used
        String jstShort = "JST";
    
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.JAPAN);
        final String[] eras = symbols.getEras();
        //int eraCount = eras.length; //The variable is never used
        logln("BC = " + eras[0]);
        if (!eras[0].equals(bc)) {
            errln("*** Should have been " + bc);
        }
    
        logln("AD = " + eras[1]);
        if (!eras[1].equals(ad)) {
            errln("*** Should have been " + ad);
        }
    
        final String zones[][] = symbols.getZoneStrings();
        //int rowCount = zones.length, colCount = zones[0].length; //The variable is never used
        logln("Long zone name = " + zones[0][1]);
        if (!zones[0][1].equals(jstLong)) {
            errln("*** Should have been " + jstLong);
        }
        logln("Short zone name = " + zones[0][2]);
        if (!zones[0][2].equals(jstShort)) {
            errln("*** Should have been " + jstShort);
        }
        logln("Long zone name = " + zones[0][3]);
        if (zones[0][3] != jstLong) {
            errln("*** Should have been " + jstLong);
        }
        logln("SHORT zone name = " + zones[0][4]);
        if (zones[0][4] != jstShort) {
            errln("*** Should have been " + jstShort);
        }
    }
}
