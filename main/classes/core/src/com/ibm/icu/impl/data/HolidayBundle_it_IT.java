/*
 *******************************************************************************
 * Copyright (C) 1996-2005, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package com.ibm.icu.impl.data;

import com.ibm.icu.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_it_IT extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        SimpleHoliday.EPIPHANY,
        new SimpleHoliday(Calendar.APRIL,      1,  0,    "Liberation Day"),
        new SimpleHoliday(Calendar.MAY,        1,  0,    "Labor Day"),
        SimpleHoliday.ASSUMPTION,
        SimpleHoliday.ALL_SAINTS_DAY,
        SimpleHoliday.IMMACULATE_CONCEPTION,
        SimpleHoliday.CHRISTMAS,
        new SimpleHoliday(Calendar.DECEMBER,  26,  0,    "St. Stephens Day"),
        SimpleHoliday.NEW_YEARS_EVE,

        // Easter and related holidays
        EasterHoliday.EASTER_SUNDAY,
        EasterHoliday.EASTER_MONDAY,
    };
    static private final Object[][] fContents = {
        { "holidays",           fHolidays },
    };
    public synchronized Object[][] getContents() { return fContents; }
}
