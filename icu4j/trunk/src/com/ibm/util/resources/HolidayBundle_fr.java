// HolidayBundle_fr.java

package com.ibm.util.resources;

import com.ibm.util.*;
import java.util.ListResourceBundle;

public class HolidayBundle_fr extends ListResourceBundle {
    static private final Object[][] fContents = {
        {   "All Saints' Day",      "Toussaint" },
        {   "Armistice Day",        "Jour de l'Armistice" },
        {   "Ascension",            "Ascension" },
        {   "Bastille Day",         "F�te de la Bastille" },
        {   "Benito Ju�rez Day",    "F�te de Benito Ju�rez" },
        {   "Boxing Day",           "Lendemain de No�l" },
        {   "Christmas Eve",        "Veille de No�l" },
        {   "Christmas",            "No�l" },
        {   "Easter Monday",        "P�ques lundi" },
        {   "Easter Sunday",        "P�ques" },
        {   "Epiphany",             "l'�piphanie" },
        {   "Flag Day",             "F�te du Drapeau" },
        {   "Good Friday",          "Vendredi Saint" },
        {   "Halloween",            "Veille de la Toussaint" },
        {   "All Saints' Day",      "Toussaint" },
        {   "Independence Day",     "F�te Ind�pendance" },
        {   "Maundy Thursday",      "Jeudi Saint" },
        {   "Mother's Day",         "F�te des m�res" },
        {   "National Day",         "F�te Nationale" },
        {   "New Year's Day",       "Jour de l'an" },
        {   "Palm Sunday",          "les Rameaux" },
        {   "Pentecost",            "Pentec�te" },
        {   "Shrove Tuesday",       "Mardi Gras" },
        {   "St. Stephen's Day",    "Saint-�tienne" },
        {   "Victoria Day",         "F�te de la Victoria" },
        {   "Victory Day",          "F�te de la Victoire" },
    };

    public synchronized Object[][] getContents() { return fContents; }
};
