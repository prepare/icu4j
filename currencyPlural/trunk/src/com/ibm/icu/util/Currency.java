/**
 *******************************************************************************
 * Copyright (C) 2001-2008, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.Serializable;
import java.text.ChoiceFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Vector;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.ICUDebug;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.TextTrieMap;

/**
 * A class encapsulating a currency, as defined by ISO 4217.  A
 * <tt>Currency</tt> object can be created given a <tt>Locale</tt> or
 * given an ISO 4217 code.  Once created, the <tt>Currency</tt> object
 * can return various data necessary to its proper display:
 *
 * <ul><li>A display symbol, for a specific locale
 * <li>The number of fraction digits to display
 * <li>A rounding increment
 * </ul>
 *
 * The <tt>DecimalFormat</tt> class uses these data to display
 * currencies.
 *
 * <p>Note: This class deliberately resembles
 * <tt>java.util.Currency</tt> but it has a completely independent
 * implementation, and adds features not present in the JDK.
 * @author Alan Liu
 * @stable ICU 2.2
 */
public class Currency extends MeasureUnit implements Serializable {
    // using serialver from jdk1.4.2_05
    private static final long serialVersionUID = -5839973855554750484L;
    private static final boolean DEBUG = ICUDebug.enabled("currency");

    // Cache to save currency name trie
    private static ICUCache CURRENCY_NAME_CACHE =  new SimpleCache();

    /**
     * ISO 4217 3-letter code.
     */
    private String isoCode;

    /**
     * Selector for getName() indicating a symbolic name for a
     * currency, such as "$" for USD.
     * @stable ICU 2.6
     */
    public static final int SYMBOL_NAME = 0;

    /**
     * Selector for ucurr_getName indicating the long name for a
     * currency, such as "US Dollar" for USD.
     * @stable ICU 2.6
     */
    public static final int LONG_NAME = 1;
   
    /**
     * Selector for getName() indicating the plural long name for a 
     * currency, such as "US dollar" for USD in "1 US dollar", 
     * and "US dollars" for USD in "2 US dollars".
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public static final int PLURAL_LONG_NAME = 2;

    // begin registry stuff

    // shim for service code
    /* package */ static abstract class ServiceShim {
        abstract ULocale[] getAvailableULocales();
        abstract Locale[] getAvailableLocales();
        abstract Currency createInstance(ULocale l);
        abstract Object registerInstance(Currency c, ULocale l);
        abstract boolean unregister(Object f);
    }

    private static ServiceShim shim;
    private static ServiceShim getShim() {
        // Note: this instantiation is safe on loose-memory-model configurations
        // despite lack of synchronization, since the shim instance has no state--
        // it's all in the class init.  The worst problem is we might instantiate
        // two shim instances, but they'll share the same state so that's ok.
        if (shim == null) {
            try {
                Class cls = Class.forName("com.ibm.icu.util.CurrencyServiceShim");
                shim = (ServiceShim)cls.newInstance();
            }
            catch (Exception e) {
                if(DEBUG){
                    e.printStackTrace();
                }
                throw new RuntimeException(e.getMessage());
            }
        }
        return shim;
    }

    /**
     * Returns a currency object for the default currency in the given
     * locale.
     * @param locale the locale
     * @return the currency object for this locale
     * @stable ICU 2.2
     */
    public static Currency getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * Returns a currency object for the default currency in the given
     * locale.
     * @stable ICU 3.2
     */
    public static Currency getInstance(ULocale locale) {
        String currency = locale.getKeywordValue("currency");
        if (currency != null) {
            return getInstance(currency);
        }

        if (shim == null) {
            return createCurrency(locale);
        }

        return shim.createInstance(locale);
    }

    /**
     * Returns an array of Strings which contain the currency
     * identifiers which are valid for the given locale on the 
     * given date.
     * @param loc the locale for which to retrieve currency codes.
     * @param d the date for which to retrieve currency codes for the given locale.
     * @return The array of ISO currency codes.
     * @draft ICU 4.0
     * @provisional This API might change or be removed in a future release.
     */
    public static String[] getAvailableCurrencyCodes(ULocale loc, Date d) 
    {
        // local variables
        String country = loc.getCountry();
        long dateL = d.getTime();
        long mask = 4294967295L;

            Vector currCodeVector = new Vector();

        // Get supplementalData
        ICUResourceBundle bundle = (ICUResourceBundle)ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,
            "supplementalData",
            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        if (bundle == null)
        {
            // no data
            return null;
        }

        // Work with the supplementalData
        try
        {
            // Process each currency to see which one is valid for the given date.
            // Some regions can have more than one current currency in use for
            // a given date.
            UResourceBundle cm = bundle.get("CurrencyMap");
            UResourceBundle countryArray = cm.get(country);

            // Get valid currencies
            for (int i = 0; i < countryArray.getSize(); i++)
            {
                // get the currency resource
                UResourceBundle currencyReq = countryArray.get(i);
                String curriso = null;
                curriso = currencyReq.getString("id");

                // get the from date
                long fromDate = 0;
                UResourceBundle fromRes = currencyReq.get("from");
                int[] fromArray = fromRes.getIntVector();
                fromDate = (long)fromArray[0] << 32;
                fromDate |= ((long)fromArray[1] & mask);

                // get the to date and check the date range
                if (currencyReq.getSize() > 2)
                {
                    long toDate = 0;
                    UResourceBundle toRes = currencyReq.get("to");
                    int[] toArray = toRes.getIntVector();
                    toDate = (long)toArray[0] << 32;
                    toDate |= ((long)toArray[1] & mask);

                    if ((fromDate <= dateL) && (dateL < toDate))
                    {
                        currCodeVector.addElement(curriso);
                    }
                }
                else
                {
                    if (fromDate <= dateL)
                    {
                        currCodeVector.addElement(curriso);
                    }
                }

            }  // end For loop

            // return the String array if we have matches
            currCodeVector.trimToSize();
            if (currCodeVector.size() != 0)
            {
                return ((String[])currCodeVector.toArray(new String[0]));
            }

        }
        catch (MissingResourceException ex)
        {
            // We don't know about this region.
            // As of CLDR 1.5.1, the data includes deprecated region history too.
            // So if we get here, either the region doesn't exist, or the data is really bad.
            // Deprecated regions should return the last valid currency for that region in the data.
            // We don't try to resolve it to a new region.
        }

        // if we get this far, return nothing
        return null;
    }

    private static final String EUR_STR = "EUR";
    /**
     * Instantiate a currency from a resource bundle found in Locale loc.
     */
    /* package */ static Currency createCurrency(ULocale loc) {
        String country = loc.getCountry();
        String variant = loc.getVariant();
        boolean isPreEuro = variant.equals("PREEURO");
        boolean isEuro = variant.equals("EURO");
        // TODO: ICU4C has service registration, and the currency is requested from the service here.
        ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,"supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        if(bundle==null){
            //throw new MissingResourceException()
            return null;
        }
        try {
            UResourceBundle cm = bundle.get("CurrencyMap");
            String curriso = null;
            UResourceBundle countryArray = cm.get(country);
            // Some regions can have more than one current currency in use.
            // The latest default currency is always the first one.
            UResourceBundle currencyReq = countryArray.get(0);
            curriso = currencyReq.getString("id");
            if (isPreEuro && curriso.equals(EUR_STR)) {
                currencyReq = countryArray.get(1);
                curriso = currencyReq.getString("id");
            }
            else if (isEuro) {
                curriso = EUR_STR;
            }
            if (curriso != null) {
                return new Currency(curriso);
            }
        } catch (MissingResourceException ex) {
            // We don't know about this region.
            // As of CLDR 1.5.1, the data includes deprecated region history too.
            // So if we get here, either the region doesn't exist, or the data is really bad.
            // Deprecated regions should return the last valid currency for that region in the data.
            // We don't try to resolve it to a new region.
        }
        return null;
    }

    /**
     * Returns a currency object given an ISO 4217 3-letter code.
     * @param theISOCode the iso code
     * @return the currency for this iso code
     * @throws NullPoninterException if <code>theISOCode</code> is null.
     * @throws IllegalArgumentException if <code>theISOCode</code> is not a
     *         3-letter alpha code.
     * @stable ICU 2.2
     */
    public static Currency getInstance(String theISOCode) {
        if (theISOCode == null) {
            throw new NullPointerException("The input currency code is null.");
        }
        boolean is3alpha = true;
        if (theISOCode.length() != 3) {
            is3alpha = false;
        } else {
            for (int i = 0; i < 3; i++) {
                char ch = theISOCode.charAt(i);
                if (ch < 'A' || (ch > 'Z' && ch < 'a') || ch > 'z') {
                    is3alpha = false;
                    break;
                }
            }
        }
        if (!is3alpha) {
            throw new IllegalArgumentException(
                    "The input currency code is not 3-letter alphabetic code.");
        }
        return new Currency(theISOCode.toUpperCase(Locale.US));
    }

    /**
     * Registers a new currency for the provided locale.  The returned object
     * is a key that can be used to unregister this currency object.
     * @param currency the currency to register
     * @param locale the ulocale under which to register the currency
     * @return a registry key that can be used to unregister this currency
     * @see #unregister
     * @stable ICU 3.2
     */
    public static Object registerInstance(Currency currency, ULocale locale) {
        return getShim().registerInstance(currency, locale);
    }

    /**
     * Unregister the currency associated with this key (obtained from
     * registerInstance).
     * @param registryKey the registry key returned from registerInstance
     * @see #registerInstance
     * @stable ICU 2.6
     */
    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        }
        if (shim == null) {
            return false;
        }
        return shim.unregister(registryKey);
    }

    /**
     * Return an array of the locales for which a currency
     * is defined.
     * @return an array of the available locales
     * @stable ICU 2.2
     */
    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales(ICUResourceBundle.ICU_BASE_NAME);
        } else {
            return shim.getAvailableLocales();
        }
    }

    /**
     * Return an array of the ulocales for which a currency
     * is defined.
     * @return an array of the available ulocales
     * @stable ICU 3.2
     */
    public static ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales(ICUResourceBundle.ICU_BASE_NAME);
        } else {
            return shim.getAvailableULocales();
        }
    }

    // end registry stuff

    /**
     * Return a hashcode for this currency.
     * @stable ICU 2.2
     */
    public int hashCode() {
        return isoCode.hashCode();
    }

    /**
     * Return true if rhs is a Currency instance,
     * is non-null, and has the same currency code.
     * @stable ICU 2.2
     */
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        if (rhs == this) return true;
        try {
            Currency c = (Currency) rhs;
            return isoCode.equals(c.isoCode);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Returns the ISO 4217 3-letter code for this currency object.
     * @stable ICU 2.2
     */
    public String getCurrencyCode() {
        return isoCode;
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name.
     * @see #getName
     * @stable ICU 3.4
     */
    public String getSymbol() {
        return getSymbol(ULocale.getDefault());
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name.
     * @param loc the Locale for the symbol
     * @see #getName
     * @stable ICU 3.4
     */
    public String getSymbol(Locale loc) {
        return getSymbol(ULocale.forLocale(loc));
    }

    /**
     * Convenience and compatibility override of getName that
     * requests the symbol name.
     * @param uloc the ULocale for the symbol
     * @see #getName
     * @stable ICU 3.4
     */
    public String getSymbol(ULocale uloc) {
        return getName(uloc, SYMBOL_NAME, new boolean[1]);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  
     * This is a convenient method for 
     * getName(ULocale, int, boolean[]); 
     * @stable ICU 3.2
     */
    public String getName(Locale locale,
                          int nameStyle,
                          boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, isChoiceFormat);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  For example, the display name for the USD
     * currency object in the en_US locale is "$".
     * @param locale locale in which to display currency
     * @param nameStyle selector for which kind of name to return.
     *                  The nameStyle should be either SYMBOL_NAME or 
     *                  LONG_NAME. Otherwise, throw IllegalArgumentException.
     * @param isChoiceFormat fill-in; isChoiceFormat[0] is set to true
     * if the returned value is a ChoiceFormat pattern; otherwise it
     * is set to false
     * @return display string for this currency.  If the resource data
     * contains no entry for this currency, then the ISO 4217 code is
     * returned.  If isChoiceFormat[0] is true, then the result is a
     * ChoiceFormat pattern.  Otherwise it is a static string.
     * @throws  IllegalArgumentException  if the nameStyle is not SYMBOL_NAME
     *                                    or LONG_NAME.
     * @stable ICU 3.2
     */
    public String getName(ULocale locale,
                          int nameStyle,
                          boolean[] isChoiceFormat) {

        // Look up the Currencies resource for the given locale.  The
        // Currencies locale data looks like this:
        //|en {
        //|  Currencies {
        //|    USD { "US$", "US Dollar" }
        //|    CHF { "Sw F", "Swiss Franc" }
        //|    INR { "=0#Rs|1#Re|1<Rs", "=0#Rupees|1#Rupee|1<Rupees" }
        //|    //...
        //|  }
        //|}

        if (nameStyle < 0 || nameStyle > 1) {
            throw new IllegalArgumentException();
        }

        String s = null;

         try {
            UResourceBundle rb = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,locale);
            ICUResourceBundle currencies = (ICUResourceBundle)rb.get("Currencies");

            // Fetch resource with multi-level resource inheritance fallback
            s = currencies.getWithFallback(isoCode).getString(nameStyle);
        }catch (MissingResourceException e) {
            //TODO what should be done here?
        }

        // Determine if this is a ChoiceFormat pattern.  One leading mark
        // indicates a ChoiceFormat.  Two indicates a static string that
        // starts with a mark.  In either case, the first mark is ignored,
        // if present.  Marks in the rest of the string have no special
        // meaning.
        isChoiceFormat[0] = false;
        if (s != null) {
            int i=0;
            while (i < s.length() && s.charAt(i) == '=' && i < 2) {
                ++i;
            }
            isChoiceFormat[0]= (i == 1);
            if (i != 0) {
                // Skip over first mark
                s = s.substring(1);
            }
            return s;
        }

        // If we fail to find a match, use the ISO 4217 code
        return isoCode;
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  
     * This is a convenient method of 
     * getName(ULocale, int, String, boolean[]);
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public String getName(Locale locale,
                          int nameStyle,
                          String pluralCount,
                          boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, 
                       pluralCount, isChoiceFormat);
    }

    /**
     * Returns the display name for the given currency in the
     * given locale.  For example, the SYMBOL_NAME for the USD
     * currency object in the en_US locale is "$".
     * The PLURAL_LONG_NAME for the USD currency object when the currency 
     * amount is plural is "US dollars", such as in "3.00 US dollars";
     * while the PLURAL_LONG_NAME for the USD currency object when the currency
     * amount is singular is "US dollar", such as in "1.00 US dollar".
     * @param locale locale in which to display currency
     * @param nameStyle selector for which kind of name to return
     * @param pluralCount plural count string for this locale
     * @param isChoiceFormat fill-in; isChoiceFormat[0] is set to true
     * if the returned value is a ChoiceFormat pattern; otherwise it
     * is set to false
     * @return display string for this currency.  If the resource data
     * contains no entry for this currency, then the ISO 4217 code is
     * returned.  If isChoiceFormat[0] is true, then the result is a
     * ChoiceFormat pattern.  Otherwise it is a static string.
     * @throws  IllegalArgumentException  if the nameStyle is not SYMBOL_NAME
     *                                    or LONG_NAME, or PLURAL_LONG_NAME.
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public String getName(ULocale locale,
                          int nameStyle,
                          String pluralCount,
                          boolean[] isChoiceFormat) {
        if (nameStyle != PLURAL_LONG_NAME) {
            return getName(locale, nameStyle, isChoiceFormat);
        }

        // Look up the CurrencyPlurals resource for the given locale.  The
        // CurrencyPlurals locale data looks like this:
        //|en {
        //|  CurrencyPlurals {
        //|    USD{
        //|      one{"US dollar"}
        //|      other{"US dollars"}
        //|    }
        //|    ...
        //|  }
        //|}
        // 
        // Algorithm detail: http://unicode.org/reports/tr35/#Currencies
        // especially the fallback rule.
        String s = null;
        ICUResourceBundle isoCodeBundle;
        // search at run time, not saved in initialization
        try {
            UResourceBundle rb = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,locale);
            // get handles fallback
            ICUResourceBundle currencies = (ICUResourceBundle)rb.get("CurrencyPlurals");

            // Fetch resource with multi-level resource inheritance fallback
            isoCodeBundle = currencies.getWithFallback(isoCode);
        } catch (MissingResourceException e) {
            // if there is no CurrencyPlurals defined or no plural long names
            // defined in the locale chain, fall back to long name.
            return getName(locale, LONG_NAME, isChoiceFormat);
        }
        try {
            s = isoCodeBundle.getStringWithFallback(pluralCount);
        } catch (MissingResourceException e1) {
            try {
                // if there is no name corresponding to 'pluralCount' defined,
                // fall back to name corresponding to "other".
                s = isoCodeBundle.getStringWithFallback("other");
            } catch (MissingResourceException e) {
                // if there is no name corresponding to plural count "other",
                // fall back to long name.
                return getName(locale, LONG_NAME, isChoiceFormat);
            }
        }
        // No support for choice format for getting plural currency names.
        if (s != null) {
            return s;
        }
        // If we fail to find a match, use the ISO 4217 code
        return isoCode;
    }

    /**
     * Attempt to parse the given string as a currency, either as a
     * display name in the given locale, or as a 3-letter ISO 4217
     * code.  If multiple display names match, then the longest one is
     * selected.  If both a display name and a 3-letter ISO code
     * match, then the display name is preferred, unless it's length
     * is less than 3.
     *
     * @param locale the locale of the display names to match
     * @param text the text to parse
     * @param pos input-output position; on input, the position within
     * text to match; must have 0 <= pos.getIndex() < text.length();
     * on output, the position after the last matched character. If
     * the parse fails, the position in unchanged upon output.
     * @return the ISO 4217 code, as a string, of the best match, or
     * null if there is no match
     *
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public static String parse(ULocale locale, String text, ParsePosition pos) {
        TextTrieMap currencyNameTrie = (TextTrieMap)CURRENCY_NAME_CACHE.get(locale);
        if (currencyNameTrie == null) {
            currencyNameTrie = new TextTrieMap(true);
            setupCurrencyNameTrie(locale, currencyNameTrie);
            CURRENCY_NAME_CACHE.put(locale, currencyNameTrie);
        }
        
        // look for the names
        CurrencyNameResultHandler handler = new CurrencyNameResultHandler();
        currencyNameTrie.find(text, pos.getIndex(), handler);
        List list = handler.getMatchedCurrencyNames();
        int maxLength = 0;
        String isoResult = null;
        if (list != null && list.size() != 0) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                CurrencyStringInfo info = (CurrencyStringInfo)it.next();
                String isoCode = info.getISOCode();
                String currencyString = info.getCurrencyString();
                if (currencyString.length() > maxLength) {
                    maxLength = currencyString.length();
                    isoResult = isoCode;
                }
            }
        }

        int start = pos.getIndex();
        if (isoResult == null || 
            maxLength < 3 && (text.length() - start) >= 3) {
            // If display name parse fails or if it matches fewer than 3
            // characters, try to parse 3-letter ISO.  Do this after the
            // display name processing so 3-letter display names are
            // preferred.  Consider /[A-Z]{3}/ to be valid ISO, and parse
            // it manually--UnicodeSet/regex are too slow and heavy.
            boolean valid = true;
            for (int k=0; k<3; ++k) {
                char ch = text.charAt(start + k); // 16-bit ok
                if (ch < 'A' || ch > 'Z') {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                pos.setIndex(pos.getIndex() + 3);
                return text.substring(start, start+3);
            }
        }
        pos.setIndex(start + maxLength);
        return isoResult;
    }

    private static void setupCurrencyNameTrie(ULocale locale, TextTrieMap trie) {
        // Look up the Currencies resource for the given locale.  The
        // Currencies locale data looks like this:
        //|en {
        //|  Currencies {
        //|    USD { "US$", "US Dollar" }
        //|    CHF { "Sw F", "Swiss Franc" }
        //|    INR { "=0#Rs|1#Re|1<Rs", "=0#Rupees|1#Rupee|1<Rupees" }
        //|    //...
        //|  }
        //|}

        // In the future, resource bundles may implement multi-level
        // fallback.  That is, if a currency is not found in the en_US
        // Currencies data, then the en Currencies data will be searched.
        // Currently, if a Currencies datum exists in en_US and en, the
        // en_US entry hides that in en.

        // We want multi-level fallback for this resource, so we implement
        // it manually.

        // Multi-level resource inheritance fallback loop

        /*
        1. Look at the Currencies array from the locale
            1a. Iterate through it, and check each row to see if row[1] matches
                1a1. If row[1] is a pattern, use ChoiceFormat to attempt a parse
            1b. Upon a match, return the ISO code stored at row[0]
        2. If there is no match, fall back to "en" and try again
        3. If there is no match, fall back to root and try again
        4. If still no match, parse 3-letter ISO {this code is probably unchanged}.
        */
        HashSet visited = new HashSet();
        ULocale parentLocale = locale;
        while (parentLocale != null) {
            UResourceBundle rb = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,parentLocale);
            // We can't cast this to String[][]; the cast has to happen later
            try {
                UResourceBundle currencies = rb.get("Currencies");
                // Do a linear search
                for (int i=0; i<currencies.getSize(); ++i) {
                    UResourceBundle item = currencies.get(i);
                    String ISOCode = item.getKey();
                    if (!visited.contains(ISOCode)) {
                        CurrencyStringInfo info = new CurrencyStringInfo(ISOCode, ISOCode);
                        trie.put(ISOCode, info);

                        String name = item.getString(0);
                        if (name.length() > 1 && name.charAt(0) == '=' &&
                            name.charAt(1) != '=') {
                            // handle choice format here
                            name = name.substring(1);
                            ChoiceFormat choice = new ChoiceFormat(name);
                            Object[] names = choice.getFormats();
                            for (int nameIndex = 0; nameIndex < names.length;
                                 ++nameIndex) {
                                info = new CurrencyStringInfo(ISOCode, 
                                                      (String)names[nameIndex]);
                                trie.put((String)names[nameIndex], info);
                            }
                        } else {
                            info = new CurrencyStringInfo(ISOCode, name);
                            trie.put(name, info);
                        }

                        info = new CurrencyStringInfo(ISOCode, item.getString(1));
                        trie.put(item.getString(1), info);
                        visited.add(ISOCode);
                    }
                }
            }
            catch (MissingResourceException e) {}

            parentLocale = parentLocale.getFallback();
        }
        // Look up the CurrencyPlurals resource for the given locale.  The
        // CurrencyPlurals locale data looks like this:
        //|en {
        //|  CurrencyPlurals {
        //|    USD { 
        //|      one{"US Dollar"}  
        //|      other{"US dollars"} 
        //|    }
        //|    //...
        //|  }
        //|}

        HashMap visitedInMap = new HashMap();
        parentLocale = locale;
        while (parentLocale != null) {
            UResourceBundle rb = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME,parentLocale);
            try {
                UResourceBundle currencies;
                currencies = rb.get("CurrencyPlurals");
                for (int i=0; i<currencies.getSize(); ++i) {
                    UResourceBundle item = currencies.get(i);
                    String ISOCode = item.getKey();
                    HashSet visitPluralCount = (HashSet)visitedInMap.get(ISOCode);
                    if (visitPluralCount == null) {
                        visitPluralCount = new HashSet();
                        visitedInMap.put(ISOCode, visitPluralCount);
                    }
                    for (int j=0; j<item.getSize(); ++j) {
                        String count = item.get(j).getKey();
                        if (!visitPluralCount.contains(count)) {
                            CurrencyStringInfo info = new CurrencyStringInfo(ISOCode, item.get(j).getString());

                            trie.put(item.get(j).getString(), info);
                            visitPluralCount.add(count);
                        }
                    }
                }
            }
            catch (MissingResourceException e) {}

            parentLocale = parentLocale.getFallback();
        }
    }

    private static final class CurrencyStringInfo {
        private String isoCode;
        private String currencyString;

        public CurrencyStringInfo(String isoCode, String currencyString) {
            this.isoCode = isoCode;
            this.currencyString = currencyString;
        }

        private String getISOCode() {
            return isoCode;
        }

        private String getCurrencyString() {
            return currencyString;
        }
    }

    private static class CurrencyNameResultHandler implements TextTrieMap.ResultHandler {
        private ArrayList resultList;
    
        public boolean handlePrefixMatch(int matchLength, Iterator values) {
            if (resultList == null) {
                resultList = new ArrayList();
            }
            while (values.hasNext()) {
                CurrencyStringInfo item = (CurrencyStringInfo)values.next();
                if (item == null) {
                    break;
                }
                int i = 0;
                for (; i < resultList.size(); i++) {
                    CurrencyStringInfo tmp = (CurrencyStringInfo)resultList.get(i);
                    if (item.getISOCode() == tmp.getISOCode()) {
                        if (matchLength > tmp.getCurrencyString().length()) {
                            resultList.set(i, item);
                        }
                        break;
                    }
                }
                if (i == resultList.size()) {
                    // not found in the current list
                    resultList.add(item);
                }
            }
            return true;
        }

        List getMatchedCurrencyNames() {
            if (resultList == null || resultList.size() == 0) {
                return null;
            }
            return resultList;
        }
    }

    /**
     * Returns the number of the number of fraction digits that should
     * be displayed for this currency.
     * @return a non-negative number of fraction digits to be
     * displayed
     * @stable ICU 2.2
     */
    public int getDefaultFractionDigits() {
        return (findData())[0];
    }

    /**
     * Returns the rounding increment for this currency, or 0.0 if no
     * rounding is done by this currency.
     * @return the non-negative rounding increment, or 0.0 if none
     * @stable ICU 2.2
     */
    public double getRoundingIncrement() {
        int[] data = findData();

        int data1 = data[1]; // rounding increment

        // If there is no rounding return 0.0 to indicate no rounding.
        // This is the high-runner case, by far.
        if (data1 == 0) {
            return 0.0;
        }

        int data0 = data[0]; // fraction digits

        // If the meta data is invalid, return 0.0 to indicate no rounding.
        if (data0 < 0 || data0 >= POW10.length) {
            return 0.0;
        }

        // Return data[1] / 10^(data[0]).  The only actual rounding data,
        // as of this writing, is CHF { 2, 25 }.
        return (double) data1 / POW10[data0];
    }

    /**
     * Returns the ISO 4217 code for this currency.
     * @stable ICU 2.2
     */
    public String toString() {
        return isoCode;
    }

    /**
     * Constructs a currency object for the given ISO 4217 3-letter
     * code.  This constructor assumes that the code is valid.
     * 
     * @param theISOCode The iso code used to construct the currency.
     * @draft ICU 3.4
     * @provisional This API might change or be removed in a future release.
     */
    protected Currency(String theISOCode) {
        isoCode = theISOCode;
    }

    /**
     * Internal function to look up currency data.  Result is an array of
     * two Integers.  The first is the fraction digits.  The second is the
     * rounding increment, or 0 if none.  The rounding increment is in
     * units of 10^(-fraction_digits).
     */
    private int[] findData() {

        try {
            // Get CurrencyMeta resource out of root locale file.  [This may
            // move out of the root locale file later; if it does, update this
            // code.]
            UResourceBundle root = ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle currencyMeta = root.get("CurrencyMeta");

            //Integer[] i = null;
            //int defaultPos = -1;
            int[] i = currencyMeta.get(isoCode).getIntVector();

            // Do a linear search for isoCode.  At the same time,
            // record the position of the DEFAULT meta data.  If the
            // meta data becomes large, make this faster.
            /*for (int j=0; j<currencyMeta.length; ++j) {
                Object[] row = currencyMeta[j];
                String s = (String) row[0];
                int c = isoCode.compareToIgnoreCase(s);
                if (c == 0) {
                    i = (Integer[]) row[1];
                    break;
                }
                if ("DEFAULT".equalsIgnoreCase(s)) {
                    defaultPos = j;
                }
                if (c < 0 && defaultPos >= 0) {
                    break;
                }
            }
            */
            if (i == null) {
                i = currencyMeta.get("DEFAULT").getIntVector();
            }

            if (i != null && i.length >= 2) {
                return i;
            }
        }
        catch (MissingResourceException e) {}

        // Config/build error; return hard-coded defaults
        return LAST_RESORT_DATA;
    }

    // Default currency meta data of last resort.  We try to use the
    // defaults encoded in the meta data resource bundle.  If there is a
    // configuration/build error and these are not available, we use these
    // hard-coded defaults (which should be identical).
    private static final int[] LAST_RESORT_DATA = new int[] { 2, 0 };

    // POW10[i] = 10^i
    private static final int[] POW10 = { 1, 10, 100, 1000, 10000, 100000,
                                1000000, 10000000, 100000000, 1000000000 };

    // -------- BEGIN ULocale boilerplate --------

    /**
     * Return the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: This method will be obsoleted.  The implementation is
     * no longer locale-specific and so there is no longer a valid or
     * actual locale associated with the Currency object.  Until
     * it is removed, this method will return the root locale.
     * @param type type of information requested, either {@link
     * com.ibm.icu.util.ULocale#VALID_LOCALE} or {@link
     * com.ibm.icu.util.ULocale#ACTUAL_LOCALE}.
     * @return the information specified by <i>type</i>, or null if
     * this object was not constructed from locale data.
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @obsolete ICU 3.2 to be removed
     * @deprecated This API is obsolete.
     */
    public final ULocale getLocale(ULocale.Type type) {
        ULocale result = (type == ULocale.ACTUAL_LOCALE) ? actualLocale : validLocale;
        if (result == null) {
            return ULocale.ROOT;
        }
        return result;
    }

    /**
     * Set information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @internal
     * @deprecated This API is ICU internal only.
     */
    final void setLocale(ULocale valid, ULocale actual) {
        // Change the following to an assertion later
        if ((valid == null) != (actual == null)) {
            ///CLOVER:OFF
            throw new IllegalArgumentException();
            ///CLOVER:ON
        }
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    /*
     * The most specific locale containing any resource data, or null.
     */
    private ULocale validLocale;

    /*
     * The locale containing data used to construct this object, or null.
     */
    private ULocale actualLocale;

    // -------- END ULocale boilerplate --------
}

//eof
