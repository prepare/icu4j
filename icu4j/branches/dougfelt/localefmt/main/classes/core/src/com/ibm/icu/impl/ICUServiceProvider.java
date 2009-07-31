/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.LocaleFormatterService;

/**
 * Provides ICU services.
 */
public class ICUServiceProvider {
    private static ICUServiceProvider nexus;
    
    public static ICUServiceProvider instance() {
        if (nexus == null) {
            nexus = new ICUServiceProvider();
        }
        return nexus;
    }
    
    private volatile LocaleFormatterService localeFormatterService;
    
    private ICUServiceProvider() {
    }
    
    private ICUServiceProvider(Builder b) {
        this.localeFormatterService = b.localeFormatterService;
    }
    
    public LocaleFormatterService getLocaleFormatterService() {
        if (localeFormatterService == null) {
            localeFormatterService = new ICULocaleFormatterService();
        }
        return localeFormatterService;
    }
    
    private static class Builder {
        private LocaleFormatterService localeFormatterService;
        
        public Builder setLocaleFormatterService(LocaleFormatterService s) {
            this.localeFormatterService = s;
            return this;
        }
        
        public void init() {
            if (nexus != null) {
                throw new IllegalStateException("nexus already initialized");
            }
            nexus = new ICUServiceProvider(this);
        }
    }
}
