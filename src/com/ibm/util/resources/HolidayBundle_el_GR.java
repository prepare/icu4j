/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/util/resources/Attic/HolidayBundle_el_GR.java,v $ 
 * $Date: 2000/03/10 04:18:02 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_el_GR extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        SimpleHoliday.NEW_YEARS_DAY,
        SimpleHoliday.EPIPHANY,

        new SimpleHoliday(Calendar.MARCH,     25,  0,    "Independence Day"),

        SimpleHoliday.MAY_DAY,
        SimpleHoliday.ASSUMPTION,

        new SimpleHoliday(Calendar.OCTOBER,   28,  0,    "Ochi Day"),

        SimpleHoliday.CHRISTMAS,
        SimpleHoliday.BOXING_DAY,

        // Easter and related holidays in the Orthodox calendar
        new EasterHoliday(-2,   true, "Good Friday"),
        new EasterHoliday( 0,   true, "Easter Sunday"),
        new EasterHoliday( 1,   true, "Easter Monday"),
        new EasterHoliday(50,   true, "Whit Monday"),
    };
    static private final Object[][] fContents = {
        {   "holidays",         fHolidays   },
    };
    public synchronized Object[][] getContents() { return fContents; }
};
