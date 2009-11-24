/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.MissingResourceException;

import com.ibm.icu.util.UResourceBundle;

/**
 * Static utility functions for probing resource tables, used by ULocale and
 * LocaleDisplayNames.
 */
public class ICUResourceTableAccess {
    /**
     * Utility to fetch locale display data from resource bundle tables.
     * TODO: delete this
     */
    public static String getTableString(String tableName, String subtableName, String item,
            String displayLocaleID) {
        try {
            if (item.length() > 0) {
                // hack this for now
                String path = null;
                if ("currency".equals(subtableName)) {
                    path = ICUResourceBundle.ICU_CURR_BASE_NAME;
                } else if ("layout".equals(tableName)) {
                    path = ICUResourceBundle.ICU_BASE_NAME;
                } else if ("Countries".equals(tableName)) {
                    path = ICUResourceBundle.ICU_REGION_BASE_NAME;
                } else if ("Currencies".equals(tableName)) {
                    path = ICUResourceBundle.ICU_CURR_BASE_NAME;
                } else if ("locale".equals(tableName)) {
                    path = ICUResourceBundle.ICU_BASE_NAME;
                } else {
                    path = ICUResourceBundle.ICU_LANG_BASE_NAME;
                }
                ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.
                    getBundleInstance(path, displayLocaleID);
                return getTableStringFromBundle(tableName, subtableName, item, bundle);
            }
        } catch (Exception e) {
//          System.out.println("gtsu: " + e.getMessage());
        }
        return item;
    }
    /**
     * Utility to fetch locale display data from resource bundle tables.
     */
    public static String getTableStringFromBundle(String tableName, String subtableName,
            String item, ICUResourceBundle bundle) {
//      System.out.println("gts table: " + tableName +
//                         " subtable: " + subtableName +
//                         " item: " + item +
//                         " bundle: " + bundle.getULocale());
        try {
            for (;;) {
                // special case currency
                if ("currency".equals(subtableName)) {
                    ICUResourceBundle table = bundle.getWithFallback("Currencies");
                    table = table.getWithFallback(item);
                    return table.getString(1);
                } else {
                    ICUResourceBundle table = ICUResourceBundle.findResourceWithFallback(tableName, bundle, null);
                    if (table == null) {
                        return item;
                    }
                    ICUResourceBundle stable = table;
                    if (subtableName != null) {
                        stable = ICUResourceBundle.findResourceWithFallback(subtableName, table, null);
                    }
                    if (stable != null) {
                        ICUResourceBundle sbundle = ICUResourceBundle.findResourceWithFallback(item, stable, null);
                        if (sbundle != null) {
                            return sbundle.getString(); // exception throw here is really an exception
                        }
                    }

                    // if we get here, stable was null, or sbundle was null
                    if (subtableName == null) {
                        // may be a deprecated code
                        String currentName = null;
                        if (tableName.equals("Countries")) {
                            currentName = LocaleIDs.getCurrentCountryID(item);
                        } else if (tableName.equals("Languages")) {
                            currentName = LocaleIDs.getCurrentLanguageID(item);
                        }
                        ICUResourceBundle sbundle = ICUResourceBundle.findResourceWithFallback(currentName, table, null);
                        if (sbundle != null) {
                            return sbundle.getString(); // exception throw here is really an exception
                        }
                    }

                    // still can't figure out ?.. try the fallback mechanism
                    ICUResourceBundle fbundle = ICUResourceBundle.findResourceWithFallback("Fallback", table, null);
                    if (fbundle == null) {
                        return item;
                    }

                    String fallbackLocale = fbundle.getString(); // again, a real exception here
                    if (fallbackLocale.length() == 0) {
                        fallbackLocale = "root";
                    }
                    //                      System.out.println("bundle: " + bundle.getULocale() + " fallback: " + fallbackLocale);
                    if (fallbackLocale.equals(table.getULocale().getName())) {
                        return item;
                    }
                    bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                            bundle.getBaseName(), fallbackLocale);
                    //                          System.out.println("fallback from " + table.getULocale() + " to " + fallbackLocale +
                    //                                             ", got bundle " + bundle.getULocale());
                }
            }
        }
        catch (Exception e) {
//          System.out.println("gtsi: " + e.getMessage());
        }
        return item;
    }

}
