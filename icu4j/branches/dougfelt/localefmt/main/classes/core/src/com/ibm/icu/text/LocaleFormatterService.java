/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.util.ULocale;

/**
 * Returns LocaleFormatters
 */
public interface LocaleFormatterService {
    /**
     * Returns a LocaleFormatter that formats names translated according to the 
     * provided display locale.
     * @param locale the display locale
     * @return the formatter
     */
    LocaleFormatter get(ULocale locale);
}
