/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.Map;

import com.ibm.icu.impl.locale.AsciiUtil;
import com.ibm.icu.text.LocaleFormatter;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 *
 */
public class ICULocaleFormatter implements LocaleFormatter {
    private final String displayID;
    private final ULocale displayLocale;

    /**
     * @param baseName
     */
    public ICULocaleFormatter(ULocale locale) {
        this.displayLocale = locale;
        this.displayID = locale.getBaseName();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayLocale()
     */
    public ULocale getDisplayLocale() {
        return displayLocale;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayName(com.ibm.icu.util.ULocale)
     */
    public String getDisplayName(ULocale locale) {
        return getDisplayName(locale.toString());
    }
    
    public String getDisplayName(String localeID) {
        // lang
        // lang (script, country, variant, keyword=value, ...)
        // script, country, variant, keyword=value, ...

        final String[] tableNames = { "Languages", "Scripts", "Countries", "Variants" };

        ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, displayID);

        StringBuffer buf = new StringBuffer();

        LocaleIDParser parser = new LocaleIDParser(localeID);
        String[] names = parser.getLanguageScriptCountryVariant();

        boolean haveLanguage = names[0].length() > 0;
        boolean openParen = false;
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            if (name.length() > 0) {
                name = ICUResourceTableAccess.getTableString(tableNames[i], null, name, bundle);
                if (buf.length() > 0) { // need a separator
                    if (haveLanguage & !openParen) {
                        buf.append(" (");
                        openParen = true;
                    } else {
                        buf.append(", ");
                    }
                }
                buf.append(name);
            }
        }

        Map<String, String> m = parser.getKeywordMap();
        if (!m.isEmpty()) {
            for (Map.Entry<String, String> e : m.entrySet()) {
                if (buf.length() > 0) {
                    if (haveLanguage & !openParen) {
                        buf.append(" (");
                        openParen = true;
                    } else {
                        buf.append(", ");
                    }
                }
                String key = e.getKey();
                buf.append(ICUResourceTableAccess.getTableString("Keys", null, key, bundle));
                buf.append("=");
                buf.append(ICUResourceTableAccess.getTableString("Types", key, e.getValue(), bundle));
            }
        }

        if (openParen) {
            buf.append(")");
        }
            
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayLanguage(com.ibm.icu.util.ULocale)
     */
    public String getDisplayLanguage(String lang) {
        return ICUResourceTableAccess.getTableString("Languages", null, lang, displayID);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayScript(com.ibm.icu.util.ULocale)
     */
    public String getDisplayScript(String script) {
        return ICUResourceTableAccess.getTableString("Scripts", null, script, displayID);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayRegion(com.ibm.icu.util.ULocale)
     */
    public String getDisplayRegion(String region) {
        return ICUResourceTableAccess.getTableString("Countries", null,  region, displayID);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayVariant(com.ibm.icu.util.ULocale)
     */
    public String getDisplayVariant(String variant) {
        return ICUResourceTableAccess.getTableString("Variants", null, variant, displayID);
    }
    
    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayKey(java.lang.String)
     */
    public String getDisplayKey(String key) {
        return ICUResourceTableAccess.getTableString("Keys", null, AsciiUtil.toLowerString(key.trim()), displayID);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatter#getDisplayKeyValue(java.lang.String, java.lang.String)
     */
    public String getDisplayKeyValue(String key, String value) {
        return ICUResourceTableAccess.getTableString("Types", key, value, displayID);
    }
}
