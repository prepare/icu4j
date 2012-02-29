/*
 *******************************************************************************
 * Copyright (C) 2008-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl.javaspi.util;

import java.util.Locale;
import java.util.spi.CurrencyNameProvider;

import com.ibm.icu.impl.javaspi.ICULocaleServiceProvider;
import com.ibm.icu.util.Currency;

public class CurrencyNameProviderICU extends CurrencyNameProvider {

    @Override
    public String getSymbol(String currencyCode, Locale locale) {
        Currency cur = Currency.getInstance(currencyCode);
        String sym = cur.getSymbol(ICULocaleServiceProvider.canonicalize(locale));
        if (sym.length() == 0 || sym.equals(currencyCode)) {
            return null;
        }
        return sym;
    }

    public String getDisplayName(String currencyCode, Locale locale) {
        Currency cur = Currency.getInstance(currencyCode);
        String name = cur.getDisplayName(locale);
        if (name.length() == 0 || name.equals(currencyCode)) {
            return null;
        }
        return name;
    }

    @Override
    public Locale[] getAvailableLocales() {
        return ICULocaleServiceProvider.getAvailableLocales();
    }

}
