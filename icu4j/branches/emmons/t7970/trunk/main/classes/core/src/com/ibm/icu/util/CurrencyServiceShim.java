/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package com.ibm.icu.util;

import java.util.List;
import java.util.Locale;

import com.ibm.icu.impl.ICULocaleService;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.ICUService;
import com.ibm.icu.impl.ICUService.Factory;
import com.ibm.icu.text.CurrencyMetaInfo;
/**
 * This is a package-access implementation of registration for
 * currency.  The shim is instantiated by reflection in Currency, all
 * dependencies on ICUService are located in this file. This structure
 * is to allow ICU4J to be built without service registration support.  
 */
final class CurrencyServiceShim extends Currency.ServiceShim {
    
    Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return service.getAvailableLocales();
    }

    ULocale[] getAvailableULocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return service.getAvailableULocales();
    }

    Currency createInstance(ULocale loc) {
        
        // Don't try to get from the cache if there is no supported country
        // since you're not going to get anything meaningful there.
        
        List<String> supportedCountries = CurrencyMetaInfo.getInstance().regions(null);
        if (service.isDefault() || !supportedCountries.contains(loc.getCountry())) {
            return Currency.createCurrency(loc);
        }
        
        ULocale[] actualLoc = new ULocale[1];
        Currency curr = (Currency)service.get(loc, actualLoc);
        curr.setLocale(null,null); // Data to determine currency is not locale data
        return curr;
    }

    Object registerInstance(Currency currency, ULocale locale) {
        return service.registerObject(currency, locale);
    }
    
    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory)registryKey);
    }

    private static class CFService extends ICULocaleService {
        CFService() {
            super("Currency");

            class CurrencyFactory extends ICUResourceBundleFactory {
                protected Object handleCreate(ULocale loc, int kind, ICUService srvc) {
                    return Currency.createCurrency(loc);
                }
                protected boolean handlesKey(Key key) {
                    List<String> supportedCountries = CurrencyMetaInfo.getInstance().regions(null);
                    ULocale loc = new ULocale(key.canonicalID());
                    return (key != null && supportedCountries.contains(loc.getCountry()));
                }
            }
            
            registerFactory(new CurrencyFactory());
        }
    }
    static final ICULocaleService service = new CFService();
}
