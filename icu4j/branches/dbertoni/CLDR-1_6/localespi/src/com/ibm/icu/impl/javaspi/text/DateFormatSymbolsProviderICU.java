/*
 *******************************************************************************
 * Copyright (C) 2008, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl.javaspi.text;

import java.text.DateFormatSymbols;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Locale;

import com.ibm.icu.impl.javaspi.ICULocale;
import com.ibm.icu.impl.jdkadapter.DateFormatSymbolsICU;

public class DateFormatSymbolsProviderICU extends DateFormatSymbolsProvider {

    @Override
    public DateFormatSymbols getInstance(Locale locale) {
        com.ibm.icu.text.DateFormatSymbols icuDfs = com.ibm.icu.text.DateFormatSymbols.getInstance(
                ICULocale.canonicalize(locale));
        return DateFormatSymbolsICU.wrap(icuDfs);
    }

    @Override
    public Locale[] getAvailableLocales() {
        return ICULocale.getAvailableLocales();
    }

}
