/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/impl/data/Attic/HebrewCalendarSymbols_fi.java,v $ 
 * $Date: 2000/09/19 18:37:36 $ 
 * $Revision: 1.3 $
 *
 *****************************************************************************************
 */
package com.ibm.util.resources;

import java.util.ListResourceBundle;

/**
 * Finnish date format symbols for the Hebrew Calendar
 */
public class HebrewCalendarSymbols_fi extends ListResourceBundle {

    private static String copyright = "Copyright \u00a9 1998 IBM Corp. All Rights Reserved.";

    static final Object[][] fContents = {
        { "Name",       "Juutalainen kalenteri" },
        
        { "MonthNames", new String[] {
                "Ti\u0161r\u00ECkuu",       // Tishri
                "He\u0161v\u00E1nkuu",      // Heshvan
                "Kisl\u00E9vkuu",           // Kislev
                "Tev\u00E9tkuu",            // Tevet
                "\u0160evatkuu",            // Shevat
                "Ad\u00E1rkuu",             // Adar I
                "Ad\u00E1rkuu II",          // Adar
                "Nis\u00E1nkuu",            // Nisan
                "Ijj\u00E1rkuu",            // Iyar
                "Siv\u00E1nkuu",            // Sivan
                "Tamm\u00FAzkuu",           // Tamuz
                "Abkuu",                    // Av
                "El\u00FAlkuu",             // Elul
            } },

        { "Eras", new String[] { 
                "AM"                        // Anno Mundi
            } },
    };

    public synchronized Object[][] getContents() {
        return fContents;
    }
};
