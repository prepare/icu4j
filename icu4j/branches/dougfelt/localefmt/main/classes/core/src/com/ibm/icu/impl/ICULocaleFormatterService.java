/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.LocaleFormatter;
import com.ibm.icu.text.LocaleFormatterService;
import com.ibm.icu.util.ULocale;

/**
 */
public class ICULocaleFormatterService implements LocaleFormatterService {

    /* (non-Javadoc)
     * @see com.ibm.icu.text.LocaleFormatterService#get(com.ibm.icu.util.ULocale)
     */
    public LocaleFormatter get(ULocale locale) {
        return new ICULocaleFormatter(locale);
    }
}
