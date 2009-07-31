/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.LocaleFormatter;
import com.ibm.icu.util.ULocale;

public class DefaultLocaleFormatter implements LocaleFormatter {
    
    public static final LocaleFormatter INSTANCE = new DefaultLocaleFormatter();
    
    private DefaultLocaleFormatter() {
    }

    public ULocale getDisplayLocale() {
        return ULocale.ROOT;
    }
    
    public String getDisplayLanguage(String lang) {
        return lang;
    }

    public String getDisplayName(ULocale locale) {
        return locale.toString();
    }

    public String getDisplayName(String localeID) {
        return localeID;
    }

    public String getDisplayRegion(String region) {
        return region;
    }

    public String getDisplayScript(String script) {
        return script;
    }

    public String getDisplayVariant(String variant) {
        return variant;
    }

    public String getDisplayKey(String key) {
        return key;
    }

    public String getDisplayKeyValue(String key, String value) {
        return value;
    }
}