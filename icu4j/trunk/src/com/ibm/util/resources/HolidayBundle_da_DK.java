/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/util/resources/Attic/HolidayBundle_da_DK.java,v $ 
 * $Date: 2000/03/10 04:18:02 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_da_DK extends ListResourceBundle
{
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        new SimpleHoliday(Calendar.APRIL,   30, -Calendar.FRIDAY, "General Prayer Day"),
        new SimpleHoliday(Calendar.JUNE,    5,                    "Constitution Day"),
        SimpleHoliday.CHRISTMAS_EVE,
        SimpleHoliday.CHRISTMAS,
        SimpleHoliday.BOXING_DAY,
        SimpleHoliday.NEW_YEARS_EVE,

        // Easter and related holidays
        EasterHoliday.MAUNDY_THURSDAY,
        EasterHoliday.GOOD_FRIDAY,
        EasterHoliday.EASTER_SUNDAY,
        EasterHoliday.EASTER_MONDAY,
        EasterHoliday.ASCENSION,
        EasterHoliday.WHIT_MONDAY,
    };

    static private final Object[][] fContents = {
        {   "holidays",             fHolidays },
    };
    public synchronized Object[][] getContents() { return fContents; }
};
