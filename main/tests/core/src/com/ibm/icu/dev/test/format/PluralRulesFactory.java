/*
 *******************************************************************************
 * Copyright (C) 2013, Google Inc, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.PluralRules.PluralType;
import com.ibm.icu.util.ULocale;

/**
 * @author markdavis
 *
 */
public abstract class PluralRulesFactory extends PluralRules.Factory {

    abstract boolean hasOverride(ULocale locale);

    public abstract PluralRules forLocale(ULocale locale, PluralType ordinal);

    public abstract ULocale[] getAvailableULocales();

    public abstract ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable);

    static final PluralRulesFactory NORMAL = new PluralRulesFactoryVanilla();

    private PluralRulesFactory() {}

    static class PluralRulesFactoryVanilla extends PluralRulesFactory {
        @Override
        boolean hasOverride(ULocale locale) {
            return false;
        }
        @Override
        public PluralRules forLocale(ULocale locale, PluralType ordinal) {
            return PluralRules.forLocale(locale, ordinal);
        }
        @Override
        public ULocale[] getAvailableULocales() {
            return PluralRules.getAvailableULocales();
        }
        @Override
        public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
            return PluralRules.getFunctionalEquivalent(locale, isAvailable);
        }
    }
}
