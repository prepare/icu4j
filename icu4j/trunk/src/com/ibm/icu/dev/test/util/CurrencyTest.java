/*
 **********************************************************************
 * Copyright (c) 2002-2008, International Business Machines
 * Corporation and others.  All Rights Reserved.
 **********************************************************************
 * Author: Alan Liu
 * Created: December 18 2002
 * Since: ICU 2.4
 **********************************************************************
 */

package com.ibm.icu.dev.test.util;
import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.util.*;

import java.util.Locale;
import java.util.Date;

/**
 * @test
 * @summary General test of Currency
 */
public class CurrencyTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new CurrencyTest().run(args);
    }

    /**
     * Test of basic API.
     */
    public void TestAPI() {
        Currency usd = Currency.getInstance("USD");
        /*int hash = */usd.hashCode();
        Currency jpy = Currency.getInstance("JPY");
        if (usd.equals(jpy)) {
            errln("FAIL: USD == JPY");
        }
        if (usd.equals("abc")) {
            errln("FAIL: USD == (String)");
        }
        if (usd.equals(null)) {
            errln("FAIL: USD == (null)");
        }
        if (!usd.equals(usd)) {
            errln("FAIL: USD != USD");
        }

        Locale[] avail = Currency.getAvailableLocales();
        if(avail==null){
            errln("FAIL: getAvailableLocales returned null");
        }

    try {
      usd.getName(ULocale.US, 5, new boolean[1]);
      errln("expected getName with invalid type parameter to throw exception");
    }
    catch (Exception e) {
    	logln("PASS: getName failed as expected");
    }
    }

    /**
     * Test registration.
     */
    public void TestRegistration() {
        final Currency jpy = Currency.getInstance("JPY");
        final Currency usd = Currency.getInstance(Locale.US);

    try {
      Currency.unregister(null); // should fail, coverage
      errln("expected unregister of null to throw exception");
    }
    catch (Exception e) {
    	logln("PASS: unregister of null failed as expected");
    }

    if (Currency.unregister("")) { // coverage
      errln("unregister before register erroneously succeeded");
    }

        ULocale fu_FU = new ULocale("fu_FU");

        Object key1 = Currency.registerInstance(jpy, ULocale.US);
        Object key2 = Currency.registerInstance(jpy, fu_FU);

        Currency nus = Currency.getInstance(Locale.US);
        if (!nus.equals(jpy)) {
            errln("expected " + jpy + " but got: " + nus);
        }

        // converage, make sure default factory works
        Currency nus1 = Currency.getInstance(Locale.JAPAN);
        if (!nus1.equals(jpy)) {
            errln("expected " + jpy + " but got: " + nus1);
        }

        ULocale[] locales = Currency.getAvailableULocales();
        boolean found = false;
        for (int i = 0; i < locales.length; ++i) {
            if (locales[i].equals(fu_FU)) {
                found = true;
                break;
            }
        }
        if (!found) {
            errln("did not find locale" + fu_FU + " in currency locales");
        }

        if (!Currency.unregister(key1)) {
            errln("unable to unregister currency using key1");
        }
        if (!Currency.unregister(key2)) {
            errln("unable to unregister currency using key2");
        }

        Currency nus2 = Currency.getInstance(Locale.US);
        if (!nus2.equals(usd)) {
            errln("expected " + usd + " but got: " + nus2);
        }

        locales = Currency.getAvailableULocales();
        found = false;
        for (int i = 0; i < locales.length; ++i) {
            if (locales[i].equals(fu_FU)) {
                found = true;
                break;
            }
        }
        if (found) {
            errln("found locale" + fu_FU + " in currency locales after unregister");
        }
    }

    /**
     * Test names.
     */
    public void TestNames() {
        // Do a basic check of getName()
        // USD { "US$", "US Dollar"            } // 04/04/1792-
        ULocale en = ULocale.ENGLISH;
        boolean[] isChoiceFormat = new boolean[1];
        Currency usd = Currency.getInstance("USD");
        // Warning: HARD-CODED LOCALE DATA in this test.  If it fails, CHECK
        // THE LOCALE DATA before diving into the code.
        if (!noData()) {
            assertEquals("USD.getName(SYMBOL_NAME)",
                         "$",
                         usd.getName(en, Currency.SYMBOL_NAME, isChoiceFormat));
            assertEquals("USD.getName(LONG_NAME)",
                         "US Dollar",
                         usd.getName(en, Currency.LONG_NAME, isChoiceFormat));
        }
        // TODO add more tests later
    }

    public void TestCoverage() {
        Currency usd = Currency.getInstance("USD");
        if (!noData()) {
        assertEquals("USD.getSymbol()",
                "$",
                usd.getSymbol());
        }
        assertEquals("USD.getLocale()",
        		ULocale.ROOT,
				usd.getLocale(null));
    }

    public void TestCurrencyKeyword() {
        ULocale locale = new ULocale("th_TH@collation=traditional;currency=QQQ");
        Currency currency = Currency.getInstance(locale);
        String result = currency.getCurrencyCode();
        if (!"QQQ".equals(result)) {
            errln("got unexpected currency: " + result);
        }
    }

	public void TestCurrencyByDate()
	{
		// local Variables
		Currency currency;
        String result;		
 
 	    // Cycle through historical currencies 
		currency = Currency.getInstance(new ULocale("eo_AM"), new Date(-630720000000L));
		result = currency.getCurrencyCode();
		if (!"AMD".equals(result))
		{
			errln("didn't return AMD for eo_AM returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AM"), new Date(0L));
		result = currency.getCurrencyCode();
		if (!"SUR".equals(result))
		{
			errln("didn't return SUR for eo_AM returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AM"), new Date(693792000000L));
		result = currency.getCurrencyCode();
		if (!"RUR".equals(result))
		{
			errln("didn't return RUR for eo_AM returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AM"), new Date(977616000000L));
		result = currency.getCurrencyCode();
		if (!"AMD".equals(result))
		{
			errln("didn't return AMD for eo_AM returned: " + result);
		}

        // Locale AD has multiple currencies at once
		currency = Currency.getInstance(new ULocale("eo_AD"), new Date(977616000000L));
		result = currency.getCurrencyCode();
		if (!"EUR".equals(result))
		{
			errln("didn't return EUR for eo_AD returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AD"), new Date(0L));
		result = currency.getCurrencyCode();
		if (!"ESP".equals(result))
		{
			errln("didn't return ESP for eo_AD returned: " + result);
		}

        // Locale UA has gap between years 1994 - 1996
		currency = Currency.getInstance(new ULocale("eo_UA"), new Date(788400000000L));
		result = currency.getCurrencyCode();
		if (!"UAH".equals(result))
		{
			errln("didn't return UAH for eo_UA returned: " + result);
		}

 	    // Cycle through historical currencies 
		currency = Currency.getInstance(new ULocale("eo_AO"), new Date(977616000000L));
		result = currency.getCurrencyCode();
		if (!"AOA".equals(result))
		{
			errln("didn't return AOA for eo_AO returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AO"), new Date(819936000000L));
		result = currency.getCurrencyCode();
		if (!"AOR".equals(result))
		{
			errln("didn't return AOR for eo_AO returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AO"), new Date(662256000000L));
		result = currency.getCurrencyCode();
		if (!"AON".equals(result))
		{
			errln("didn't return AON for eo_AO returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AO"), new Date(315360000000L));
		result = currency.getCurrencyCode();
		if (!"AOK".equals(result))
		{
			errln("didn't return AOK for eo_AO returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("eo_AO"), new Date(0L));
		result = currency.getCurrencyCode();
		if (!"AOA".equals(result))
		{
			errln("didn't return AOA for eo_AO returned: " + result);
		}

        // Test EURO support
		currency = Currency.getInstance(new ULocale("en_US"), new Date(System.currentTimeMillis()));
		result = currency.getCurrencyCode();
		if (!"USD".equals(result))
		{
			errln("didn't return USD for en_US returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("en_US_PREEURO"), new Date(System.currentTimeMillis()));
		result = currency.getCurrencyCode();
		if (!"USD".equals(result))
		{
			errln("didn't return USD for en_US_PREEURO returned: " + result);
		}

		currency = Currency.getInstance(new ULocale("en_US_Q"), new Date(System.currentTimeMillis()));
		result = currency.getCurrencyCode();
		if (!"USD".equals(result))
		{
			errln("didn't return USD for en_US_Q returned: " + result);
		}

		// non-existant locale
 		currency = Currency.getInstance(new ULocale("en_QQ"), new Date(System.currentTimeMillis()));
		if (currency != null)
		{
			errln("didn't return NULL for en_QQ");
		}

	}

    public void TestDeprecatedCurrencyFormat() {
        // bug 5952
        Locale locale = new Locale("sr", "QQ");
        DecimalFormatSymbols icuSymbols = new 
        com.ibm.icu.text.DecimalFormatSymbols(locale);
        String symbol = icuSymbols.getCurrencySymbol();
        Currency currency = icuSymbols.getCurrency();
        String expectCur = null;
        String expectSym = "\u00A4";
        if(!symbol.toString().equals(expectSym) || currency != null) {
            errln("for " + locale + " expected " + expectSym+"/"+expectCur + " but got " + symbol+"/"+currency);
        } else {
            logln("for " + locale + " expected " + expectSym+"/"+expectCur + " and got " + symbol+"/"+currency);
        }
    }
}
