// HolidayBundle_it.java

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.Calendar;
import java.util.ListResourceBundle;

public class HolidayBundle_it extends ListResourceBundle {
    static private final Object[][] fContents =
    {
        {   "All Saints' Day",      "Ognissanti" },
        {   "Armistice Day",        "armistizio" },
        {   "Ascension",            "ascensione" },
        {   "Ash Wednesday",        "mercoled� delle ceneri" },
        {   "Boxing Day",           "Santo Stefano" },
        {   "Christmas",            "natale" },
        {   "Easter Sunday",        "pasqua" },
        {   "Epiphany",             "Epifania" },
        {   "Good Friday",          "venerd� santo" },
        {   "Halloween",            "vigilia di Ognissanti" },
        {   "Maundy Thursday",      "gioved� santo" },
        {   "New Year's Day",       "anno nuovo" },
        {   "Palm Sunday",          "domenica delle palme" },
        {   "Pentecost",            "di Pentecoste" },
        {   "Shrove Tuesday",       "martedi grasso" },
        {   "St. Stephen's Day",    "Santo Stefano" },
        {   "Thanksgiving",         "Giorno del Ringraziamento" },

    };
    public synchronized Object[][] getContents() { return fContents; }
};
