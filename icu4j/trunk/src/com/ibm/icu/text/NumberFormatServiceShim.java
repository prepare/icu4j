/*
 *******************************************************************************
 * Copyright (C) 2003, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package com.ibm.icu.text;

import java.util.Locale;
import java.util.Set;

//import com.ibm.icu.impl.ICULocaleData;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.ICUService;
import com.ibm.icu.impl.ICUService.Factory;
import com.ibm.icu.impl.ICUService.Key;
import com.ibm.icu.impl.ICULocaleService;
import com.ibm.icu.impl.ICULocaleService.LocaleKey;
import com.ibm.icu.impl.ICULocaleService.LocaleKeyFactory;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.NumberFormat.NumberFormatFactory;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

class NumberFormatServiceShim extends NumberFormat.NumberFormatShim {

    Locale[] getAvailableLocales() {
        if (service.isDefault()) {
            return ICUResourceBundle.getAvailableLocales(UResourceBundle.ICU_BASE_NAME);
        }
        return service.getAvailableLocales();
    }

    private static final class NFFactory extends LocaleKeyFactory {
        private NumberFormatFactory delegate;

        NFFactory(NumberFormatFactory delegate) {
            super(delegate.visible() ? VISIBLE : INVISIBLE);

            this.delegate = delegate;
        }

        public Object create(Key key, ICUService service) {
            if (handlesKey(key)) {
                LocaleKey lkey = (LocaleKey)key;
                Locale loc = lkey.canonicalLocale();
                int kind = lkey.kind();

                Object result = delegate.createFormat(loc, kind);
                if (result == null) {
                    result = service.getKey(key, null, this);
                }
                return result;
            }
            return null;
        }

        protected Set getSupportedIDs() {
            return delegate.getSupportedLocaleNames();
        }
    }

    Object registerFactory(NumberFormatFactory factory) {
        return service.registerFactory(new NFFactory(factory));
    }

    boolean unregister(Object registryKey) {
        return service.unregisterFactory((Factory)registryKey);
    }

    NumberFormat createInstance(Locale desiredLocale, int choice) {
        if (service.isDefault()) {
            return NumberFormat.createInstance(desiredLocale, choice);
        }
        Locale[] actualLoc = new Locale[1];
        NumberFormat fmt = (NumberFormat)service.get(desiredLocale, choice,
                                                     actualLoc);
        ULocale uloc = new ULocale(actualLoc[0]);
        fmt.setLocale(uloc, uloc); // services make no distinction between actual & valid
        return fmt;
    }

    private static class NFService extends ICULocaleService {
        NFService() {
            super("NumberFormat");

            class RBNumberFormatFactory extends ICUResourceBundleFactory {
                protected Object handleCreate(Locale loc, int kind, ICUService service) {
                    return NumberFormat.createInstance(loc, kind);
                }
            }
                
            this.registerFactory(new RBNumberFormatFactory());
            markDefault();
        }
    }
    private static ICULocaleService service = new NFService();
}
