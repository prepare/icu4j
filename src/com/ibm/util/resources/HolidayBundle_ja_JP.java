// HolidayBundle_jp_JP.java

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_ja_JP extends ListResourceBundle {
    static private final Holiday[] fHolidays = {
        new SimpleHoliday(Calendar.FEBRUARY,  11,  0,    "National Foundation Day"),
    };
    static private final Object[][] fContents = {
        {   "holidays",         fHolidays   },
    };
    public synchronized Object[][] getContents() { return fContents; }
};
