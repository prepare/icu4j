/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/impl/data/HolidayBundle_en_CA.java,v $ 
 * $Date: 2000/03/10 04:18:03 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_en_CA extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        new SimpleHoliday(Calendar.MAY,       19, 0,                  "Victoria Day"),
        new SimpleHoliday(Calendar.JULY,       1, 0,                  "Canada Day"),
        new SimpleHoliday(Calendar.AUGUST,     1, Calendar.MONDAY,    "Civic Holiday"),
        new SimpleHoliday(Calendar.SEPTEMBER,  1, Calendar.MONDAY,    "Labor Day"),
        new SimpleHoliday(Calendar.OCTOBER,    8, Calendar.MONDAY,    "Thanksgiving"),
        new SimpleHoliday(Calendar.NOVEMBER,  11, 0,                  "Remembrance Day"),
        SimpleHoliday.CHRISTMAS,
        SimpleHoliday.BOXING_DAY,
        SimpleHoliday.NEW_YEARS_EVE,

        // Easter and related holidays
        //hey {jf} - where are these from?
//        EasterHoliday.GOOD_FRIDAY,
//        EasterHoliday.EASTER_SUNDAY,
//        EasterHoliday.EASTER_MONDAY,
    };

    static private final Object[][] fContents = {
        { "holidays",   fHolidays },

        { "Labor Day",  "Labour Day" },
    };
    public synchronized Object[][] getContents() { return fContents; }
};
