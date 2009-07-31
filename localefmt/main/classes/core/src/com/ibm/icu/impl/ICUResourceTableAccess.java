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
 *
 */
public class ICUResourceTableAccess {

    /**
     * Utility to fetch data from resource bundle tables.
     */
    public static String getTableString(String tableName, String subtableName, String item, ICUResourceBundle bundle) {
        try {
            for (;;) {
                // special case currency
                if ("currency".equals(subtableName)) {
                    ICUResourceBundle table = bundle.getWithFallback("Currencies");
                    table = table.getWithFallback(item);
                    return table.getString(1);
                } else {
                    ICUResourceBundle table = bundle.getWithFallback(tableName);
                    try {
                        if (subtableName != null) {
                            table = table.getWithFallback(subtableName);
                        }
                        return table.getStringWithFallback(item);
                    }
                    catch (MissingResourceException e) {

                        if(subtableName==null){
                            try{
                                // may be a deprecated code
                                String currentName = null;
                                if(tableName.equals("Countries")){
                                    currentName = LocaleIDs.getCurrentCountryID(item);
                                }else if(tableName.equals("Languages")){
                                    currentName = LocaleIDs.getCurrentLanguageID(item);
                                }
                                return table.getStringWithFallback(currentName);
                            }catch (MissingResourceException ex){/* fall through*/}
                        }

                        // still can't figure out ?.. try the fallback mechanism
                        String fallbackLocale = table.getWithFallback("Fallback").getString();
                        if (fallbackLocale.length() == 0) {
                            fallbackLocale = "root";
                        }
                        //                      System.out.println("bundle: " + bundle.getULocale() + " fallback: " + fallbackLocale);
                        // if(fallbackLocale.equals(table.getULocale().localeID)){
                        if(fallbackLocale.equals(table.getULocale().getBaseName())){
                            return item;
                        }
                        bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, 
                                fallbackLocale);
                        //                          System.out.println("fallback from " + table.getULocale() + " to " + fallbackLocale + 
                        //                                             ", got bundle " + bundle.getULocale());                      
                    }
                }
            }
        }
        catch (Exception e) {
            //          System.out.println("gtsi: " + e.getMessage());
        }
        return item;
    }

    /**
     * Utility to fetch data from resource bundle tables.
     */
    public static String getTableString(String tableName, String subtableName, String item, String displayLocaleID) {
        if (item.length() > 0) {
            try {
                ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.
                getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, displayLocaleID);
                return getTableString(tableName, subtableName, item, bundle);
            } catch (Exception e) {
                //              System.out.println("gtsu: " + e.getMessage());
            }
        }
        return item;
    }
}
