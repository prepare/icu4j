/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;

/**
 * Interface for returning display names of locales.  This returns
 * names translated according to the conventions of a single display locale
 * {@see #getDisplayLocale()}.  If no translation is available, the argument
 * (language id, script id, etc.) is returned.
 * @draft ICU 4.4
 */
public interface LocaleFormatter {
    /**
     * Returns the id of the display locale.
     */
    ULocale getDisplayLocale();
    
    /**
     * Returns the translation of the complete locale.
     * @param locale the locale
     * @return the translation
     */
    String getDisplayName(ULocale locale);
    
    /**
     * Returns the translation of the complete locale id.
     * @param localeID the locale id
     * @return the translation
     */
    String getDisplayName(String localeID);
    
    /**
     * Returns the translation of a language.
     * @param lang the language id
     * @return the translation
     */
    String getDisplayLanguage(String lang);
    
    /**
     * Returns the translation of a script.
     * @param script the script id
     * @return the translation
     */
    String getDisplayScript(String script);
    
    /**
     * Returns the translation of a region/country.
     * @param region the region id
     * @return the translation
     */
    String getDisplayRegion(String region);
    
    /**
     * Returns the translation of a variant.
     * @param variant the variant
     * @return the translation
     */
    String getDisplayVariant(String variant);
    
    /**
     * Returns the translation of a locale key.
     * @param key the key
     * @return the translation
     */
    String getDisplayKey(String key);
    
    /**
     * Returns the translation of a key value.
     * @param key the key
     * @param value the value
     * @return the translation of the value
     */
    String getDisplayKeyValue(String key, String value);
}
