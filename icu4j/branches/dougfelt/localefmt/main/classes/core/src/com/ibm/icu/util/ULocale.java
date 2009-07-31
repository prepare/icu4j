/*
******************************************************************************
* Copyright (C) 2003-2009, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.util;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.ICULocaleFormatter;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.ICUResourceTableAccess;
import com.ibm.icu.impl.LocaleIDParser;
import com.ibm.icu.impl.LocaleIDs;
import com.ibm.icu.impl.LocaleUtility;
import com.ibm.icu.impl.ICUServiceProvider;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.impl.locale.AsciiUtil;
import com.ibm.icu.impl.locale.BaseLocale;
import com.ibm.icu.impl.locale.InternalLocaleBuilder;
import com.ibm.icu.impl.locale.LanguageTag;
import com.ibm.icu.impl.locale.LocaleExtensions;
import com.ibm.icu.impl.locale.LocaleSyntaxException;
import com.ibm.icu.impl.locale.LanguageTag.Extension;
import com.ibm.icu.text.LocaleFormatter;

/**
 * A class analogous to {@link java.util.Locale} that provides additional
 * support for ICU protocol.  In ICU 3.0 this class is enhanced to support
 * RFC 3066 language identifiers.
 *
 * <p>Many classes and services in ICU follow a factory idiom, in
 * which a factory method or object responds to a client request with
 * an object.  The request includes a locale (the <i>requested</i>
 * locale), and the returned object is constructed using data for that
 * locale.  The system may lack data for the requested locale, in
 * which case the locale fallback mechanism will be invoked until a
 * populated locale is found (the <i>valid</i> locale).  Furthermore,
 * even when a populated locale is found (the <i>valid</i> locale),
 * further fallback may be required to reach a locale containing the
 * specific data required by the service (the <i>actual</i> locale).
 *
 * <p>ULocale performs <b>'normalization'</b> and <b>'canonicalization'</b> of locale ids.
 * Normalization 'cleans up' ICU locale ids as follows:
 * <ul>
 * <li>language, script, country, variant, and keywords are properly cased<br>
 * (lower, title, upper, upper, and lower case respectively)</li>
 * <li>hyphens used as separators are converted to underscores</li>
 * <li>three-letter language and country ids are converted to two-letter
 * equivalents where available</li>
 * <li>surrounding spaces are removed from keywords and values</li>
 * <li>if there are multiple keywords, they are put in sorted order</li>
 * </ul>
 * Canonicalization additionally performs the following:
 * <ul>
 * <li>POSIX ids are converted to ICU format IDs</li>
 * <li>'grandfathered' 3066 ids are converted to ICU standard form</li>
 * <li>'PREEURO' and 'EURO' variants are converted to currency keyword form, with the currency
 * id appropriate to the country of the locale (for PREEURO) or EUR (for EURO).
 * </ul>
 * All ULocale constructors automatically normalize the locale id.  To handle
 * POSIX ids, <code>canonicalize</code> can be called to convert the id
 * to canonical form, or the <code>canonicalInstance</code> factory method
 * can be called.</p>
 *
 * <p>This class provides selectors {@link #VALID_LOCALE} and {@link
 * #ACTUAL_LOCALE} intended for use in methods named
 * <tt>getLocale()</tt>.  These methods exist in several ICU classes,
 * including {@link com.ibm.icu.util.Calendar}, {@link
 * com.ibm.icu.util.Currency}, {@link com.ibm.icu.text.UFormat},
 * {@link com.ibm.icu.text.BreakIterator}, {@link
 * com.ibm.icu.text.Collator}, {@link
 * com.ibm.icu.text.DateFormatSymbols}, and {@link
 * com.ibm.icu.text.DecimalFormatSymbols} and their subclasses, if
 * any.  Once an object of one of these classes has been created,
 * <tt>getLocale()</tt> may be called on it to determine the valid and
 * actual locale arrived at during the object's construction.
 *
 * <p>Note: The <tt>getLocale()</tt> method will be implemented in ICU
 * 3.0; ICU 2.8 contains a partial preview implementation.  The
 * <i>actual</i> locale is returned correctly, but the <i>valid</i>
 * locale is not, in most cases.
 *
 * @see java.util.Locale
 * @author weiv
 * @author Alan Liu
 * @author Ram Viswanadha
 * @stable ICU 2.8 
 */
public final class ULocale implements Serializable {
    // using serialver from jdk1.4.2_05
    private static final long serialVersionUID = 3715177670352309217L;

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale ENGLISH = new ULocale("en", Locale.ENGLISH);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale FRENCH = new ULocale("fr", Locale.FRENCH);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale GERMAN = new ULocale("de", Locale.GERMAN);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale ITALIAN = new ULocale("it", Locale.ITALIAN);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale JAPANESE = new ULocale("ja", Locale.JAPANESE);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale KOREAN = new ULocale("ko", Locale.KOREAN);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale CHINESE = new ULocale("zh", Locale.CHINESE);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale SIMPLIFIED_CHINESE = new ULocale("zh_Hans", Locale.CHINESE);

    /** 
     * Useful constant for language.
     * @stable ICU 3.0
     */
    public static final ULocale TRADITIONAL_CHINESE = new ULocale("zh_Hant", Locale.CHINESE);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale FRANCE = new ULocale("fr_FR", Locale.FRANCE);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale GERMANY = new ULocale("de_DE", Locale.GERMANY);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale ITALY = new ULocale("it_IT", Locale.ITALY);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale JAPAN = new ULocale("ja_JP", Locale.JAPAN);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale KOREA = new ULocale("ko_KR", Locale.KOREA);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale CHINA = new ULocale("zh_Hans_CN", Locale.CHINA);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale PRC = CHINA;

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale TAIWAN = new ULocale("zh_Hant_TW", Locale.TAIWAN);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale UK = new ULocale("en_GB", Locale.UK);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale US = new ULocale("en_US", Locale.US);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale CANADA = new ULocale("en_CA", Locale.CANADA);

    /** 
     * Useful constant for country/region.
     * @stable ICU 3.0
     */
    public static final ULocale CANADA_FRENCH = new ULocale("fr_CA", Locale.CANADA_FRENCH);

    /**
     * Handy constant.
     */
    static final String EMPTY_STRING = "";

    // Used in both ULocale and IDParser, so moved up here.
    static final char UNDERSCORE            = '_';

    // default empty locale
    private static final Locale EMPTY_LOCALE = new Locale("", "");

    /**
     * The root ULocale.
     * @stable ICU 2.8
     */ 
    public static final ULocale ROOT = new ULocale("root", EMPTY_LOCALE);
    
    private static final SimpleCache<Locale, ULocale> CACHE = new SimpleCache<Locale, ULocale>();

    /**
     * Cache the locale.
     */
    private transient Locale locale;

    /**
     * The raw localeID that we were passed in.
     */
    private String localeID;

    private static String[][] CANONICALIZE_MAP;
    private static String[][] variantsToKeywords;

    private static void initCANONICALIZE_MAP() {
        if (CANONICALIZE_MAP == null) {
            /**
             * This table lists pairs of locale ids for canonicalization.  The
             * The 1st item is the normalized id. The 2nd item is the
             * canonicalized id. The 3rd is the keyword. The 4th is the keyword value.
             */
            String[][] tempCANONICALIZE_MAP = {
//              { EMPTY_STRING,     "en_US_POSIX", null, null }, /* .NET name */
                { "C",              "en_US_POSIX", null, null }, /* POSIX name */
                { "art_LOJBAN",     "jbo", null, null }, /* registered name */
                { "az_AZ_CYRL",     "az_Cyrl_AZ", null, null }, /* .NET name */
                { "az_AZ_LATN",     "az_Latn_AZ", null, null }, /* .NET name */
                { "ca_ES_PREEURO",  "ca_ES", "currency", "ESP" },
                { "cel_GAULISH",    "cel__GAULISH", null, null }, /* registered name */
                { "de_1901",        "de__1901", null, null }, /* registered name */
                { "de_1906",        "de__1906", null, null }, /* registered name */
                { "de__PHONEBOOK",  "de", "collation", "phonebook" }, /* Old ICU name */
                { "de_AT_PREEURO",  "de_AT", "currency", "ATS" },
                { "de_DE_PREEURO",  "de_DE", "currency", "DEM" },
                { "de_LU_PREEURO",  "de_LU", "currency", "EUR" },
                { "el_GR_PREEURO",  "el_GR", "currency", "GRD" },
                { "en_BOONT",       "en__BOONT", null, null }, /* registered name */
                { "en_SCOUSE",      "en__SCOUSE", null, null }, /* registered name */
                { "en_BE_PREEURO",  "en_BE", "currency", "BEF" },
                { "en_IE_PREEURO",  "en_IE", "currency", "IEP" },
                { "es__TRADITIONAL", "es", "collation", "traditional" }, /* Old ICU name */
                { "es_ES_PREEURO",  "es_ES", "currency", "ESP" },
                { "eu_ES_PREEURO",  "eu_ES", "currency", "ESP" },
                { "fi_FI_PREEURO",  "fi_FI", "currency", "FIM" },
                { "fr_BE_PREEURO",  "fr_BE", "currency", "BEF" },
                { "fr_FR_PREEURO",  "fr_FR", "currency", "FRF" },
                { "fr_LU_PREEURO",  "fr_LU", "currency", "LUF" },
                { "ga_IE_PREEURO",  "ga_IE", "currency", "IEP" },
                { "gl_ES_PREEURO",  "gl_ES", "currency", "ESP" },
                { "hi__DIRECT",     "hi", "collation", "direct" }, /* Old ICU name */
                { "it_IT_PREEURO",  "it_IT", "currency", "ITL" },
                { "ja_JP_TRADITIONAL", "ja_JP", "calendar", "japanese" },
//              { "nb_NO_NY",       "nn_NO", null, null },
                { "nl_BE_PREEURO",  "nl_BE", "currency", "BEF" },
                { "nl_NL_PREEURO",  "nl_NL", "currency", "NLG" },
                { "pt_PT_PREEURO",  "pt_PT", "currency", "PTE" },
                { "sl_ROZAJ",       "sl__ROZAJ", null, null }, /* registered name */
                { "sr_SP_CYRL",     "sr_Cyrl_RS", null, null }, /* .NET name */
                { "sr_SP_LATN",     "sr_Latn_RS", null, null }, /* .NET name */
                { "sr_YU_CYRILLIC", "sr_Cyrl_RS", null, null }, /* Linux name */
                { "th_TH_TRADITIONAL", "th_TH", "calendar", "buddhist" }, /* Old ICU name */
                { "uz_UZ_CYRILLIC", "uz_Cyrl_UZ", null, null }, /* Linux name */
                { "uz_UZ_CYRL",     "uz_Cyrl_UZ", null, null }, /* .NET name */
                { "uz_UZ_LATN",     "uz_Latn_UZ", null, null }, /* .NET name */
                { "zh_CHS",         "zh_Hans", null, null }, /* .NET name */
                { "zh_CHT",         "zh_Hant", null, null }, /* .NET name */
                { "zh_GAN",         "zh__GAN", null, null }, /* registered name */
                { "zh_GUOYU",       "zh", null, null }, /* registered name */
                { "zh_HAKKA",       "zh__HAKKA", null, null }, /* registered name */
                { "zh_MIN",         "zh__MIN", null, null }, /* registered name */
                { "zh_MIN_NAN",     "zh__MINNAN", null, null }, /* registered name */
                { "zh_WUU",         "zh__WUU", null, null }, /* registered name */
                { "zh_XIANG",       "zh__XIANG", null, null }, /* registered name */
                { "zh_YUE",         "zh__YUE", null, null } /* registered name */
            };
    
            synchronized (ULocale.class) {
                if (CANONICALIZE_MAP == null) {
                    CANONICALIZE_MAP = tempCANONICALIZE_MAP;
                }
            }
        }
        if (variantsToKeywords == null) {
            /**
             * This table lists pairs of locale ids for canonicalization.  The
             * The first item is the normalized variant id.
             */
            String[][] tempVariantsToKeywords = {
                    { "EURO",   "currency", "EUR" },
                    { "PINYIN", "collation", "pinyin" }, /* Solaris variant */
                    { "STROKE", "collation", "stroke" }  /* Solaris variant */
            };
    
            synchronized (ULocale.class) {
                if (variantsToKeywords == null) {
                    variantsToKeywords = tempVariantsToKeywords;
                }
            }
        }
    }

    /*
     * This table is used for mapping between ICU and special Java
     * locales.  When an ICU locale matches <minumum base> with
     * <keyword>/<value>, the ICU locale is mapped to <Java> locale.
     * For example, both ja_JP@calendar=japanese and ja@calendar=japanese
     * are mapped to Java locale "ja_JP_JP".  ICU locale "nn" is mapped
     * to Java locale "no_NO_NY".
     */
    private static final String[][] _javaLocaleMap = {
    //  { <Java>,       <ICU base>, <keyword>,  <value>,    <minimum base>
        { "ja_JP_JP",   "ja_JP",    "calendar", "japanese", "ja"},
        { "no_NO_NY",   "nn_NO",    null,       null,       "nn"},
        { "th_TH_TH",   "th_TH",    "numbers",  "thai",     "th"},
    };

    /**
     * Private constructor used by static initializers.
     */
    private ULocale(String localeID, Locale locale) {
        this.localeID = localeID;
        this.locale = locale;
    }

    /**
     * Construct a ULocale object from a {@link java.util.Locale}.
     * @param loc a JDK locale
     * @stable ICU 2.8
     * @internal
     */
    private ULocale(Locale loc) {
        this.localeID = getName(forLocale(loc).toString());
        this.locale = loc;
    }

    /**
     * Return a ULocale object for a {@link java.util.Locale}.
     * The ULocale is canonicalized.
     * @param loc a JDK locale
     * @stable ICU 3.2
     */
    public static ULocale forLocale(Locale loc) {
        if (loc == null) {
            return null;
        }
        ULocale result = CACHE.get(loc);
        if (result == null) {
            if (defaultULocale != null && loc == defaultULocale.locale) {
            result = defaultULocale;
        } else {
                String locStr = loc.toString();
                if (locStr.length() == 0) {
                    result = ROOT;
                } else {
                    for (int i = 0; i < _javaLocaleMap.length; i++) {
                        if (_javaLocaleMap[i][0].equals(locStr)) {
                            LocaleIDParser p = new LocaleIDParser(_javaLocaleMap[i][1]);
                            p.setKeywordValue(_javaLocaleMap[i][2], _javaLocaleMap[i][3]);
                            locStr = p.getName();
                            break;
                        }
                    }
                    result = new ULocale(locStr, loc);
                }
            }
            CACHE.put(loc, result);
        }
        return result;
    }

    /**
     * Construct a ULocale from a RFC 3066 locale ID. The locale ID consists
     * of optional language, script, country, and variant fields in that order, 
     * separated by underscores, followed by an optional keyword list.  The
     * script, if present, is four characters long-- this distinguishes it
     * from a country code, which is two characters long.  Other fields
     * are distinguished by position as indicated by the underscores.  The
     * start of the keyword list is indicated by '@', and consists of two
     * or more keyword/value pairs separated by semicolons(';').
     * <p>
     * This constructor does not canonicalize the localeID.  So, for
     * example, "zh__pinyin" remains unchanged instead of converting
     * to "zh@collation=pinyin".  By default ICU only recognizes the
     * latter as specifying pinyin collation.  Use {@link #createCanonical}
     * or {@link #canonicalize} if you need to canonicalize the localeID.
     * 
     * @param localeID string representation of the locale, e.g:
     * "en_US", "sy_Cyrl_YU", "zh__pinyin", "es_ES@currency=EUR;collation=traditional"
     * @stable ICU 2.8
     */ 
    public ULocale(String localeID) {
        this.localeID = getName(localeID);
    }

    /**
     * Convenience overload of ULocale(String, String, String) for 
     * compatibility with java.util.Locale.
     * @see #ULocale(String, String, String)
     * @stable ICU 3.4
     */
    public ULocale(String a, String b) {
        this(a, b, null);
    }

    /**
     * Construct a ULocale from a localeID constructed from the three 'fields' a, b, and c.  These
     * fields are concatenated using underscores to form a localeID of
     * the form a_b_c, which is then handled like the localeID passed
     * to <code>ULocale(String localeID)</code>.  
     *
     * <p>Java locale strings consisting of language, country, and
     * variant will be handled by this form, since the country code
     * (being shorter than four letters long) will not be interpreted
     * as a script code.  If a script code is present, the final
     * argument ('c') will be interpreted as the country code.  It is
     * recommended that this constructor only be used to ease porting,
     * and that clients instead use the single-argument constructor
     * when constructing a ULocale from a localeID.
     * @param a first component of the locale id
     * @param b second component of the locale id
     * @param c third component of the locale id
     * @see #ULocale(String)
     * @stable ICU 3.0 
     */
    public ULocale(String a, String b, String c) {
        localeID = getName(lscvToID(a, b, c, EMPTY_STRING));
    }

    /**
     * Create a ULocale from the id by first canonicalizing the id.
     * @param nonCanonicalID the locale id to canonicalize
     * @return the locale created from the canonical version of the ID.
     * @stable ICU 3.0
     */
    public static ULocale createCanonical(String nonCanonicalID) {
        return new ULocale(canonicalize(nonCanonicalID), (Locale)null);
    }

    private static String lscvToID(String lang, String script, String country, String variant) {
        StringBuffer buf = new StringBuffer();
     
        if (lang != null && lang.length() > 0) {
            buf.append(lang);
        }
        if (script != null && script.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(script);
        }
        if (country != null && country.length() > 0) {
            buf.append(UNDERSCORE);
            buf.append(country);
        }
        if (variant != null && variant.length() > 0) {
            if (country == null || country.length() == 0) {
                buf.append(UNDERSCORE);
            }
            buf.append(UNDERSCORE);
            buf.append(variant);
        }
        return buf.toString();
    }

    /**
     * Convert this ULocale object to a {@link java.util.Locale}.
     * @return a JDK locale that either exactly represents this object
     * or is the closest approximation.
     * @stable ICU 2.8
     */
    public Locale toLocale() {
        if (locale == null) {
            LocaleIDParser p = new LocaleIDParser(localeID);
            String base = p.getBaseName();
            for (int i = 0; i < _javaLocaleMap.length; i++) {
                if (base.equals(_javaLocaleMap[i][1]) || base.equals(_javaLocaleMap[i][4])) {
                    if (_javaLocaleMap[i][2] != null) {
                        String val = p.getKeywordValue(_javaLocaleMap[i][2]);
                        if (val != null && val.equals(_javaLocaleMap[i][3])) {
                            p = new LocaleIDParser(_javaLocaleMap[i][0]);
                            break;
                        }
                    } else {
                        p = new LocaleIDParser(_javaLocaleMap[i][0]);
                        break;
                    }
                }
            }
            String[] names = p.getLanguageScriptCountryVariant();
            locale = new Locale(names[0], names[2], names[3]);
        }
        return locale;
    }

    private static ICUCache<String, String> nameCache = new SimpleCache<String, String>();
    /**
     * Keep our own default ULocale.
     */
    private static Locale defaultLocale = Locale.getDefault();
    private static ULocale defaultULocale = new ULocale(defaultLocale);

    /**
     * Returns the current default ULocale.
     * @stable ICU 2.8
     */ 
    public static ULocale getDefault() {
        synchronized (ULocale.class) {
            Locale currentDefault = Locale.getDefault();
            if (!defaultLocale.equals(currentDefault)) {
                defaultLocale = currentDefault;
                defaultULocale = new ULocale(defaultLocale);
            }
            return defaultULocale;
        }
    }

    /**
     * Sets the default ULocale.  This also sets the default Locale.
     * If the caller does not have write permission to the
     * user.language property, a security exception will be thrown,
     * and the default ULocale will remain unchanged.
     * @param newLocale the new default locale
     * @throws SecurityException
     *        if a security manager exists and its
     *        <code>checkPermission</code> method doesn't allow the operation.
     * @throws NullPointerException if <code>newLocale</code> is null
     * @see SecurityManager#checkPermission(java.security.Permission)
     * @see java.util.PropertyPermission
     * @stable ICU 3.0 
     */
    public static synchronized void setDefault(ULocale newLocale){
        Locale.setDefault(newLocale.toLocale());
        defaultULocale = newLocale;
    }
    
    /**
     * This is for compatibility with Locale-- in actuality, since ULocale is
     * immutable, there is no reason to clone it, so this API returns 'this'.
     * @stable ICU 3.0
     */
    public Object clone() {
        return this;
    }

    /**
     * Returns the hashCode.
     * @stable ICU 3.0
     */
    public int hashCode() {
        return localeID.hashCode();
    }
    
    /**
     * Returns true if the other object is another ULocale with the
     * same full name, or is a String localeID that matches the full name.
     * Note that since names are not canonicalized, two ULocales that
     * function identically might not compare equal.
     *
     * @return true if this Locale is equal to the specified object.
     * @stable ICU 3.0 
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof String) {
            return localeID.equals((String)obj);   
        }
        if (obj instanceof ULocale) {
            return localeID.equals(((ULocale)obj).localeID);
        }
        return false;
    }
    
    /**
     * Returns a list of all installed locales.
     * @stable ICU 3.0
     */
    public static ULocale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    /**
     * Returns a list of all 2-letter country codes defined in ISO 3166.
     * Can be used to create Locales.
     * @stable ICU 3.0
     */
    public static String[] getISOCountries() {
        return LocaleIDs.getISOCountries();
    }

    /**
     * Returns a list of all 2-letter language codes defined in ISO 639.
     * Can be used to create Locales.
     * [NOTE:  ISO 639 is not a stable standard-- some languages' codes have changed.
     * The list this function returns includes both the new and the old codes for the
     * languages whose codes have changed.]
     * @stable ICU 3.0
     */
    public static String[] getISOLanguages() {
        return LocaleIDs.getISOLanguages();
    }

    /**
     * Returns the language code for this locale, which will either be the empty string
     * or a lowercase ISO 639 code.
     * @see #getDisplayLanguage()
     * @see #getDisplayLanguage(ULocale)
     * @stable ICU 3.0
     */
    public String getLanguage() {
        return getLanguage(localeID);
    }
    
    /**
     * Returns the language code for the locale ID,
     * which will either be the empty string
     * or a lowercase ISO 639 code.
     * @see #getDisplayLanguage()
     * @see #getDisplayLanguage(ULocale)
     * @stable ICU 3.0
     */
    public static String getLanguage(String localeID) {
        return new LocaleIDParser(localeID).getLanguage();
    }
     
    /**
     * Returns the script code for this locale, which might be the empty string.
     * @see #getDisplayScript()
     * @see #getDisplayScript(ULocale)
     * @stable ICU 3.0
     */
    public String getScript() {
        return getScript(localeID);
    }

    /**
     * Returns the script code for the specified locale, which might be the empty string.
     * @see #getDisplayScript()
     * @see #getDisplayScript(ULocale)
     * @stable ICU 3.0
     */
    public static String getScript(String localeID) {
        return new LocaleIDParser(localeID).getScript();
    }
    
    /**
     * Returns the country/region code for this locale, which will either be the empty string
     * or an uppercase ISO 3166 2-letter code.
     * @see #getDisplayCountry()
     * @see #getDisplayCountry(ULocale)
     * @stable ICU 3.0
     */
    public String getCountry() {
        return getCountry(localeID);
    }

    /**
     * Returns the country/region code for this locale, which will either be the empty string
     * or an uppercase ISO 3166 2-letter code.
     * @param localeID
     * @see #getDisplayCountry()
     * @see #getDisplayCountry(ULocale)
     * @stable ICU 3.0
     */
    public static String getCountry(String localeID) {
        return new LocaleIDParser(localeID).getCountry();
    }
    
    /**
     * Returns the variant code for this locale, which might be the empty string.
     * @see #getDisplayVariant()
     * @see #getDisplayVariant(ULocale)
     * @stable ICU 3.0
     */
    public String getVariant() {
        return getVariant(localeID);
    }

    /**
     * Returns the variant code for the specified locale, which might be the empty string.
     * @see #getDisplayVariant()
     * @see #getDisplayVariant(ULocale)
     * @stable ICU 3.0
     */
    public static String getVariant(String localeID) {
        return new LocaleIDParser(localeID).getVariant();
    }

    /**
     * Returns the fallback locale for the specified locale, which might be the empty string.
     * @stable ICU 3.2
     */
    public static String getFallback(String localeID) {
        return getFallbackString(getName(localeID));
    }

    /**
     * Returns the fallback locale for this locale.  If this locale is root, returns null.
     * @stable ICU 3.2
     */
    public ULocale getFallback() {
        if (localeID.length() == 0 || localeID.charAt(0) == '@') {
            return null;
        }
        return new ULocale(getFallbackString(localeID), (Locale)null);
    }

    /**
     * Return the given (canonical) locale id minus the last part before the tags.
     */
    private static String getFallbackString(String fallback) {
        int limit = fallback.indexOf('@');
        if (limit == -1) {
            limit = fallback.length();
        }
        int start = fallback.lastIndexOf('_', limit);
        if (start == -1) {
            start = 0;
        }
        return fallback.substring(0, start) + fallback.substring(limit);
    }

    /**
     * Returns the (normalized) base name for this locale.
     * @return the base name as a String.
     * @stable ICU 3.0
     */
    public String getBaseName() {
        return getBaseName(localeID);
    }
    
    /**
     * Returns the (normalized) base name for the specified locale.
     * @param localeID the locale ID as a string
     * @return the base name as a String.
     * @stable ICU 3.0
     */
    public static String getBaseName(String localeID){
        if (localeID.indexOf('@') == -1) {
            return localeID;
        }
        return new LocaleIDParser(localeID).getBaseName();
    }

    /**
     * Returns the (normalized) full name for this locale.
     *
     * @return String the full name of the localeID
     * @stable ICU 3.0
     */ 
    public String getName() {
        return localeID; // always normalized
    }

    /**
     * Returns the (normalized) full name for the specified locale.
     *
     * @param localeID the localeID as a string
     * @return String the full name of the localeID
     * @stable ICU 3.0
     */
    public static String getName(String localeID){
        String name = nameCache.get(localeID);
        if (name == null) {
            name = new LocaleIDParser(localeID).getName();
            nameCache.put(localeID, name);
        }
        return name;
    }

    /**
     * Returns a string representation of this object.
     * @stable ICU 3.0
     */
    public String toString() {
        return localeID;
    }

    /**
     * Returns an iterator over keywords for this locale.  If there 
     * are no keywords, returns null.
     * @return iterator over keywords, or null if there are no keywords.
     * @stable ICU 3.0
     */
    public Iterator<String> getKeywords() {
        return getKeywords(localeID);
    }

    /**
     * Returns an iterator over keywords for the specified locale.  If there 
     * are no keywords, returns null.
     * @return an iterator over the keywords in the specified locale, or null
     * if there are no keywords.
     * @stable ICU 3.0
     */
    public static Iterator<String> getKeywords(String localeID){
        return new LocaleIDParser(localeID).getKeywords();
    }

    /**
     * Returns the value for a keyword in this locale. If the keyword is not defined, returns null.
     * @param keywordName name of the keyword whose value is desired. Case insensitive.
     * @return the value of the keyword, or null.
     * @stable ICU 3.0
     */
    public String getKeywordValue(String keywordName){
        return getKeywordValue(localeID, keywordName);
    }
    
    /**
     * Returns the value for a keyword in the specified locale. If the keyword is not defined, returns null. 
     * The locale name does not need to be normalized.
     * @param keywordName name of the keyword whose value is desired. Case insensitive.
     * @return String the value of the keyword as a string
     * @stable ICU 3.0
     */
    public static String getKeywordValue(String localeID, String keywordName) {
        return new LocaleIDParser(localeID).getKeywordValue(keywordName);
    }

    /**
     * Returns the canonical name for the specified locale ID.  This is used to convert POSIX
     * and other grandfathered IDs to standard ICU form.
     * @param localeID the locale id
     * @return the canonicalized id
     * @stable ICU 3.0
     */
    public static String canonicalize(String localeID){
        LocaleIDParser parser = new LocaleIDParser(localeID, true);
        String baseName = parser.getBaseName();
        boolean foundVariant = false;
      
        // formerly, we always set to en_US_POSIX if the basename was empty, but
        // now we require that the entire id be empty, so that "@foo=bar"
        // will pass through unchanged.
        // {dlf} I'd rather keep "" unchanged.
        if (localeID.equals("")) {
            return "";
//              return "en_US_POSIX";
        }

        // we have an ID in the form xx_Yyyy_ZZ_KKKKK

        initCANONICALIZE_MAP();

        /* convert the variants to appropriate ID */
        for (int i = 0; i < variantsToKeywords.length; i++) {
            String[] vals = variantsToKeywords[i];
            int idx = baseName.lastIndexOf("_" + vals[0]);
            if (idx > -1) {
                foundVariant = true;

                baseName = baseName.substring(0, idx);
                if (baseName.endsWith("_")) {
                    baseName = baseName.substring(0, --idx);
                }
                parser.setBaseName(baseName);
                parser.defaultKeywordValue(vals[1], vals[2]);
                break;
            }
        }

        /* See if this is an already known locale */
        for (int i = 0; i < CANONICALIZE_MAP.length; i++) {
            if (CANONICALIZE_MAP[i][0].equals(baseName)) {
                foundVariant = true;

                String[] vals = CANONICALIZE_MAP[i];
                parser.setBaseName(vals[1]);
                if (vals[2] != null) {
                    parser.defaultKeywordValue(vals[2], vals[3]);
                }
                break;
            }
        }

        /* total mondo hack for Norwegian, fortunately the main NY case is handled earlier */
        if (!foundVariant) {
            if (parser.getLanguage().equals("nb") && parser.getVariant().equals("NY")) {
                parser.setBaseName(lscvToID("nn", parser.getScript(), parser.getCountry(), null));
            }
        }

        return parser.getName();
    }
    
    /**
     * Given a keyword and a value, return a new locale with an updated
     * keyword and value.  If keyword is null, this removes all keywords from the locale id.
     * Otherwise, if the value is null, this removes the value for this keyword from the
     * locale id.  Otherwise, this adds/replaces the value for this keyword in the locale id.
     * The keyword and value must not be empty.
     * @param keyword the keyword to add/remove, or null to remove all keywords.
     * @param value the value to add/set, or null to remove this particular keyword.
     * @return the updated locale
     * @stable ICU 3.2
     */
    public ULocale setKeywordValue(String keyword, String value) {
        return new ULocale(setKeywordValue(localeID, keyword, value), (Locale)null);
    }

    /**
     * Given a locale id, a keyword, and a value, return a new locale id with an updated
     * keyword and value.  If keyword is null, this removes all keywords from the locale id.
     * Otherwise, if the value is null, this removes the value for this keyword from the
     * locale id.  Otherwise, this adds/replaces the value for this keyword in the locale id.
     * The keyword and value must not be empty.
     * @param localeID the locale id to modify
     * @param keyword the keyword to add/remove, or null to remove all keywords.
     * @param value the value to add/set, or null to remove this particular keyword.
     * @return the updated locale id
     * @stable ICU 3.2
     */
    public static String setKeywordValue(String localeID, String keyword, String value) {
        LocaleIDParser parser = new LocaleIDParser(localeID);
        parser.setKeywordValue(keyword, value);
        return parser.getName();
    }

    /*
     * Given a locale id, a keyword, and a value, return a new locale id with an updated
     * keyword and value, if the keyword does not already have a value.  The keyword and
     * value must not be null or empty.
     * @param localeID the locale id to modify
     * @param keyword the keyword to add, if not already present
     * @param value the value to add, if not already present
     * @return the updated locale id
     * @internal
     */
/*    private static String defaultKeywordValue(String localeID, String keyword, String value) {
        IDParser parser = new IDParser(localeID);
        parser.defaultKeywordValue(keyword, value);
        return parser.getName();
    }*/

    /**
     * Returns a three-letter abbreviation for this locale's language.  If the locale
     * doesn't specify a language, returns the empty string.  Otherwise, returns
     * a lowercase ISO 639-2/T language code.
     * The ISO 639-2 language codes can be found on-line at
     *   <a href="ftp://dkuug.dk/i18n/iso-639-2.txt"><code>ftp://dkuug.dk/i18n/iso-639-2.txt</code></a>
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter language abbreviation is not available for this locale.
     * @stable ICU 3.0
     */
    public String getISO3Language() {
        return getISO3Language(localeID);
    }

    /**
     * Returns a three-letter abbreviation for this locale's language.  If the locale
     * doesn't specify a language, returns the empty string.  Otherwise, returns
     * a lowercase ISO 639-2/T language code.
     * The ISO 639-2 language codes can be found on-line at
     *   <a href="ftp://dkuug.dk/i18n/iso-639-2.txt"><code>ftp://dkuug.dk/i18n/iso-639-2.txt</code></a>
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter language abbreviation is not available for this locale.
     * @stable ICU 3.0
     */
    public static String getISO3Language(String localeID) {
        return LocaleIDs.getISO3Language(getLanguage(localeID));
    }
    
    /**
     * Returns a three-letter abbreviation for this locale's country/region.  If the locale
     * doesn't specify a country, returns the empty string.  Otherwise, returns
     * an uppercase ISO 3166 3-letter country code.
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter country abbreviation is not available for this locale.
     * @stable ICU 3.0
     */
    public String getISO3Country() {
        return getISO3Country(localeID);
    }
    
    /**
     * Returns a three-letter abbreviation for this locale's country/region.  If the locale
     * doesn't specify a country, returns the empty string.  Otherwise, returns
     * an uppercase ISO 3166 3-letter country code.
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter country abbreviation is not available for this locale.
     * @stable ICU 3.0
     */
    public static String getISO3Country(String localeID) {
        return LocaleIDs.getISO3Country(getCountry(localeID));
    }
    
    // display names

 
    /**
     * Returns this locale's language localized for display in the default locale.
     * @return the localized language name.
     * @stable ICU 3.0
     */
    public String getDisplayLanguage() {
        return getDisplayLanguageInternal(localeID, getDefault().localeID);
    }

    /**
     * Returns this locale's language localized for display in the provided locale.
     * @param displayLocale the locale in which to display the name.
     * @return the localized language name.
     * @stable ICU 3.0
     */
    public String getDisplayLanguage(ULocale displayLocale) {
        return getDisplayLanguageInternal(localeID, displayLocale.localeID);
    }
    
    /**
     * Returns a locale's language localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose language will be displayed
     * @param displayLocaleID the id of the locale in which to display the name.
     * @return the localized language name.
     * @stable ICU 3.0
     */
    public static String getDisplayLanguage(String localeID, String displayLocaleID) {
        return getDisplayLanguageInternal(localeID, getName(displayLocaleID));
    }

    /**
     * Returns a locale's language localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose language will be displayed.
     * @param displayLocale the locale in which to display the name.
     * @return the localized language name.
     * @stable ICU 3.0
     */
    public static String getDisplayLanguage(String localeID, ULocale displayLocale) {
        return getDisplayLanguageInternal(localeID, displayLocale.localeID);
    } 


    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayLanguageInternal(String localeID, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayLanguage(new LocaleIDParser(localeID).getLanguage());
    }
 
    /**
     * Returns this locale's script localized for display in the default locale.
     * @return the localized script name.
     * @stable ICU 3.0
     */
    public String getDisplayScript() {
        return getDisplayScriptInternal(localeID, getDefault().localeID);
    }

    /**
     * Returns this locale's script localized for display in the provided locale.
     * @param displayLocale the locale in which to display the name.
     * @return the localized script name.
     * @stable ICU 3.0
     */
    public String getDisplayScript(ULocale displayLocale) {
        return getDisplayScriptInternal(localeID, displayLocale.localeID);
    }
    
    /**
     * Returns a locale's script localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose script will be displayed
     * @param displayLocaleID the id of the locale in which to display the name.
     * @return the localized script name.
     * @stable ICU 3.0
     */
    public static String getDisplayScript(String localeID, String displayLocaleID) {
        return getDisplayScriptInternal(localeID, getName(displayLocaleID));
    }

    /**
     * Returns a locale's script localized for display in the provided locale.
     * @param localeID the id of the locale whose script will be displayed.
     * @param displayLocale the locale in which to display the name.
     * @return the localized script name.
     * @stable ICU 3.0
     */
    public static String getDisplayScript(String localeID, ULocale displayLocale) {
        return getDisplayScriptInternal(localeID, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayScriptInternal(String localeID, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayScript(new LocaleIDParser(localeID).getScript());
    }

    /**
     * Returns this locale's country localized for display in the default locale.
     * @return the localized country name.
     * @stable ICU 3.0
     */
    public String getDisplayCountry() {
        return getDisplayCountryInternal(localeID, getDefault().localeID);
    }
    
    /**
     * Returns this locale's country localized for display in the provided locale.
     * @param displayLocale the locale in which to display the name.
     * @return the localized country name.
     * @stable ICU 3.0
     */
    public String getDisplayCountry(ULocale displayLocale){
        return getDisplayCountryInternal(localeID, displayLocale.localeID);   
    }
    
    /**
     * Returns a locale's country localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose country will be displayed
     * @param displayLocaleID the id of the locale in which to display the name.
     * @return the localized country name.
     * @stable ICU 3.0
     */
    public static String getDisplayCountry(String localeID, String displayLocaleID) {
        return getDisplayCountryInternal(localeID, getName(displayLocaleID));
    }

    /**
     * Returns a locale's country localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose country will be displayed.
     * @param displayLocale the locale in which to display the name.
     * @return the localized country name.
     * @stable ICU 3.0
     */
    public static String getDisplayCountry(String localeID, ULocale displayLocale) {
        return getDisplayCountryInternal(localeID, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayCountryInternal(String localeID, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayRegion(new LocaleIDParser(localeID).getCountry());
    }
    
    /**
     * Returns this locale's variant localized for display in the default locale.
     * @return the localized variant name.
     * @stable ICU 3.0
     */
    public String getDisplayVariant() {
        return getDisplayVariantInternal(localeID, getDefault().localeID);   
    }

    /**
     * Returns this locale's variant localized for display in the provided locale.
     * @param displayLocale the locale in which to display the name.
     * @return the localized variant name.
     * @stable ICU 3.0
     */
    public String getDisplayVariant(ULocale displayLocale) {
        return getDisplayVariantInternal(localeID, displayLocale.localeID);   
    }
    
    /**
     * Returns a locale's variant localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose variant will be displayed
     * @param displayLocaleID the id of the locale in which to display the name.
     * @return the localized variant name.
     * @stable ICU 3.0
     */
    public static String getDisplayVariant(String localeID, String displayLocaleID){
        return getDisplayVariantInternal(localeID, getName(displayLocaleID));
    }
    
    /**
     * Returns a locale's variant localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose variant will be displayed.
     * @param displayLocale the locale in which to display the name.
     * @return the localized variant name.
     * @stable ICU 3.0
     */
    public static String getDisplayVariant(String localeID, ULocale displayLocale) {
        return getDisplayVariantInternal(localeID, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayVariantInternal(String localeID, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayVariant(new LocaleIDParser(localeID).getVariant());
    }

    /**
     * Returns a keyword localized for display in the default locale.
     * @param keyword the keyword to be displayed.
     * @return the localized keyword name.
     * @see #getKeywords()
     * @stable ICU 3.0
     */
    public static String getDisplayKeyword(String keyword) {
        return getDisplayKeywordInternal(keyword, getDefault().localeID);   
    }
    
    /**
     * Returns a keyword localized for display in the specified locale.
     * @param keyword the keyword to be displayed.
     * @param displayLocaleID the id of the locale in which to display the keyword.
     * @return the localized keyword name.
     * @see #getKeywords(String)
     * @stable ICU 3.0
     */
    public static String getDisplayKeyword(String keyword, String displayLocaleID) {
        return getDisplayKeywordInternal(keyword, getName(displayLocaleID));   
    }

    /**
     * Returns a keyword localized for display in the specified locale.
     * @param keyword the keyword to be displayed.
     * @param displayLocale the locale in which to display the keyword.
     * @return the localized keyword name.
     * @see #getKeywords(String)
     * @stable ICU 3.0
     */
    public static String getDisplayKeyword(String keyword, ULocale displayLocale) {
        return getDisplayKeywordInternal(keyword, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayKeywordInternal(String keyword, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayKey(keyword);
    }

    /**
     * Returns a keyword value localized for display in the default locale.
     * @param keyword the keyword whose value is to be displayed.
     * @return the localized value name.
     * @stable ICU 3.0
     */
    public String getDisplayKeywordValue(String keyword) {
        return getDisplayKeywordValueInternal(localeID, keyword, getDefault().localeID);
    }
    
    /**
     * Returns a keyword value localized for display in the specified locale.
     * @param keyword the keyword whose value is to be displayed.
     * @param displayLocale the locale in which to display the value.
     * @return the localized value name.
     * @stable ICU 3.0
     */
    public String getDisplayKeywordValue(String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(localeID, keyword, displayLocale.localeID);   
    }

    /**
     * Returns a keyword value localized for display in the specified locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose keyword value is to be displayed.
     * @param keyword the keyword whose value is to be displayed.
     * @param displayLocaleID the id of the locale in which to display the value.
     * @return the localized value name.
     * @stable ICU 3.0
     */
    public static String getDisplayKeywordValue(String localeID, String keyword, String displayLocaleID) {
        return getDisplayKeywordValueInternal(localeID, keyword, getName(displayLocaleID));
    }

    /**
     * Returns a keyword value localized for display in the specified locale.
     * This is a cover for the ICU4C API.
     * @param localeID the id of the locale whose keyword value is to be displayed.
     * @param keyword the keyword whose value is to be displayed.
     * @param displayLocale the id of the locale in which to display the value.
     * @return the localized value name.
     * @stable ICU 3.0
     */
    public static String getDisplayKeywordValue(String localeID, String keyword, ULocale displayLocale) {
        return getDisplayKeywordValueInternal(localeID, keyword, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayKeywordValueInternal(String localeID, String keyword, String displayLocaleID) {
        keyword = AsciiUtil.toLowerString(keyword.trim());
        String value = new LocaleIDParser(localeID).getKeywordValue(keyword);
        return getFormatter(displayLocaleID).getDisplayKeyValue(keyword, value);
    }
    
    /**
     * Returns this locale name localized for display in the default locale.
     * @return the localized locale name.
     * @stable ICU 3.0
     */
    public String getDisplayName() {
        return getDisplayNameInternal(localeID, getDefault().localeID);
    }
    
    /**
     * Returns this locale name localized for display in the provided locale.
     * @param displayLocale the locale in which to display the locale name.
     * @return the localized locale name.
     * @stable ICU 3.0
     */
    public String getDisplayName(ULocale displayLocale) {
        return getDisplayNameInternal(localeID, displayLocale.localeID);
    }
    
    /**
     * Returns the locale ID localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the locale whose name is to be displayed.
     * @param displayLocaleID the id of the locale in which to display the locale name.
     * @return the localized locale name.
     * @stable ICU 3.0
     */
    public static String getDisplayName(String localeID, String displayLocaleID) {
        return getDisplayNameInternal(localeID, getName(displayLocaleID));
    }

    /**
     * Returns the locale ID localized for display in the provided locale.
     * This is a cover for the ICU4C API.
     * @param localeID the locale whose name is to be displayed.
     * @param displayLocale the locale in which to display the locale name.
     * @return the localized locale name.
     * @stable ICU 3.0
     */
    public static String getDisplayName(String localeID, ULocale displayLocale) {
        return getDisplayNameInternal(localeID, displayLocale.localeID);
    }

    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayNameInternal(String localeID, String displayLocaleID) {
        return getFormatter(displayLocaleID).getDisplayName(localeID);
    }
    
    private static LocaleFormatter getFormatter(String displayLocale) {
        ULocale locale = new ULocale(displayLocale);
        return ICUServiceProvider.instance().getLocaleFormatterService().get(locale);
    }

    /**
     * Returns this locale's layout orientation for characters.  The possible
     * values are "left-to-right", "right-to-left", "top-to-bottom" or
     * "bottom-to-top".
     * @return The locale's layout orientation for characters.
     * @stable ICU 4.0
     */
    public String getCharacterOrientation() {
        return ICUResourceTableAccess.getTableString("layout", null, "characters", getName());
    }

    /**
     * Returns this locale's layout orientation for lines.  The possible
     * values are "left-to-right", "right-to-left", "top-to-bottom" or
     * "bottom-to-top".
     * @return The locale's layout orientation for lines.
     * @stable ICU 4.0
     */
    public String getLineOrientation() {
        return ICUResourceTableAccess.getTableString("layout", null, "lines", getName());
    }

    /** 
     * Selector for <tt>getLocale()</tt> indicating the locale of the
     * resource containing the data.  This is always at or above the
     * valid locale.  If the valid locale does not contain the
     * specific data being requested, then the actual locale will be
     * above the valid locale.  If the object was not constructed from
     * locale data, then the valid locale is <i>null</i>.
     *
     * @draft ICU 2.8 (retain)
     * @provisional This API might change or be removed in a future release.
     */
    public static Type ACTUAL_LOCALE = new Type();

    /** 
     * Selector for <tt>getLocale()</tt> indicating the most specific
     * locale for which any data exists.  This is always at or above
     * the requested locale, and at or below the actual locale.  If
     * the requested locale does not correspond to any resource data,
     * then the valid locale will be above the requested locale.  If
     * the object was not constructed from locale data, then the
     * actual locale is <i>null</i>.
     *
     * <p>Note: The valid locale will be returned correctly in ICU
     * 3.0 or later.  In ICU 2.8, it is not returned correctly.
     * @draft ICU 2.8 (retain)
     * @provisional This API might change or be removed in a future release.
     */ 
    public static Type VALID_LOCALE = new Type();

    /**
     * Opaque selector enum for <tt>getLocale()</tt>.
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @draft ICU 2.8 (retainAll)
     * @provisional This API might change or be removed in a future release.
     */
    public static final class Type {
        private Type() {}
    }

  /**
    * Based on a HTTP formatted list of acceptable locales, determine an available locale for the user.
    * NullPointerException is thrown if acceptLanguageList or availableLocales is
    * null.  If fallback is non-null, it will contain true if a fallback locale (one
    * not in the acceptLanguageList) was returned.  The value on entry is ignored. 
    * ULocale will be one of the locales in availableLocales, or the ROOT ULocale if
    * if a ROOT locale was used as a fallback (because nothing else in
    * availableLocales matched).  No ULocale array element should be null; behavior
    * is undefined if this is the case.
    * @param acceptLanguageList list in HTTP "Accept-Language:" format of acceptable locales
    * @param availableLocales list of available locales. One of these will be returned.
    * @param fallback if non-null, a 1-element array containing a boolean to be set with the fallback status
    * @return one of the locales from the availableLocales list, or null if none match
    * @stable ICU 3.4
    */

    public static ULocale acceptLanguage(String acceptLanguageList, ULocale[] availableLocales, 
                                         boolean[] fallback) {
        if (acceptLanguageList == null) {
            throw new NullPointerException();
        }
        ULocale acceptList[] = null;
        try {
            acceptList = parseAcceptLanguage(acceptLanguageList, true);
        } catch (ParseException pe) {
            acceptList = null;
        }
        if (acceptList == null) {
            return null;
        }
        return acceptLanguage(acceptList, availableLocales, fallback);
    }

    /**
    * Based on a list of acceptable locales, determine an available locale for the user.
    * NullPointerException is thrown if acceptLanguageList or availableLocales is
    * null.  If fallback is non-null, it will contain true if a fallback locale (one
    * not in the acceptLanguageList) was returned.  The value on entry is ignored. 
    * ULocale will be one of the locales in availableLocales, or the ROOT ULocale if
    * if a ROOT locale was used as a fallback (because nothing else in
    * availableLocales matched).  No ULocale array element should be null; behavior
    * is undefined if this is the case.
    * @param acceptLanguageList list of acceptable locales
    * @param availableLocales list of available locales. One of these will be returned.
    * @param fallback if non-null, a 1-element array containing a boolean to be set with the fallback status
    * @return one of the locales from the availableLocales list, or null if none match
    * @stable ICU 3.4
    */

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, ULocale[]
    availableLocales, boolean[] fallback) {
        // fallbacklist
        int i,j;
        if(fallback != null) {
            fallback[0]=true;
        }
        for(i=0;i<acceptLanguageList.length;i++) {
            ULocale aLocale = acceptLanguageList[i];
            boolean[] setFallback = fallback;
            do {
                for(j=0;j<availableLocales.length;j++) {
                    if(availableLocales[j].equals(aLocale)) {
                        if(setFallback != null) {
                            setFallback[0]=false; // first time with this locale - not a fallback.
                        }
                        return availableLocales[j];
                    }
                }
                Locale loc = aLocale.toLocale();
                Locale parent = LocaleUtility.fallback(loc);
                if(parent != null) {
                    aLocale = new ULocale(parent);
                } else {
                    aLocale = null;
                }
                setFallback = null; // Do not set fallback in later iterations
            } while (aLocale != null);
        }
        return null;
    }

   /**
    * Based on a HTTP formatted list of acceptable locales, determine an available locale for the user.
    * NullPointerException is thrown if acceptLanguageList or availableLocales is
    * null.  If fallback is non-null, it will contain true if a fallback locale (one
    * not in the acceptLanguageList) was returned.  The value on entry is ignored. 
    * ULocale will be one of the locales in availableLocales, or the ROOT ULocale if
    * if a ROOT locale was used as a fallback (because nothing else in
    * availableLocales matched).  No ULocale array element should be null; behavior
    * is undefined if this is the case.
    * This function will choose a locale from the ULocale.getAvailableLocales() list as available.
    * @param acceptLanguageList list in HTTP "Accept-Language:" format of acceptable locales
    * @param fallback if non-null, a 1-element array containing a boolean to be set with the fallback status
    * @return one of the locales from the ULocale.getAvailableLocales() list, or null if none match
    * @stable ICU 3.4
    */

    public static ULocale acceptLanguage(String acceptLanguageList, boolean[] fallback) {
        return acceptLanguage(acceptLanguageList, ULocale.getAvailableLocales(),
                                fallback);
    }

   /**
    * Based on an ordered array of acceptable locales, determine an available locale for the user.
    * NullPointerException is thrown if acceptLanguageList or availableLocales is
    * null.  If fallback is non-null, it will contain true if a fallback locale (one
    * not in the acceptLanguageList) was returned.  The value on entry is ignored. 
    * ULocale will be one of the locales in availableLocales, or the ROOT ULocale if
    * if a ROOT locale was used as a fallback (because nothing else in
    * availableLocales matched).  No ULocale array element should be null; behavior
    * is undefined if this is the case.
    * This function will choose a locale from the ULocale.getAvailableLocales() list as available.
    * @param acceptLanguageList ordered array of acceptable locales (preferred are listed first)
    * @param fallback if non-null, a 1-element array containing a boolean to be set with the fallback status
    * @return one of the locales from the ULocale.getAvailableLocales() list, or null if none match
    * @stable ICU 3.4
    */

    public static ULocale acceptLanguage(ULocale[] acceptLanguageList, boolean[]
                                         fallback) {
        return acceptLanguage(acceptLanguageList, ULocale.getAvailableLocales(),
                fallback);
    }

    /**
     * Package local method used for parsing Accept-Language string
     * @internal ICU 3.8
     */
    static ULocale[] parseAcceptLanguage(String acceptLanguage, boolean isLenient) throws ParseException {
        /**
         * @internal ICU 3.4
         */
        class ULocaleAcceptLanguageQ implements Comparable<ULocaleAcceptLanguageQ> {
            private double q;
            private double serial;
            public ULocaleAcceptLanguageQ(double theq, int theserial) {
                q = theq;
                serial = theserial;
            }
            public int compareTo(ULocaleAcceptLanguageQ other) {
                if (q > other.q) { // reverse - to sort in descending order
                    return -1;
                } else if (q < other.q) {
                    return 1;
                }
                if (serial < other.serial) {
                    return -1;
                } else if (serial > other.serial) {
                    return 1;
                } else {
                    return 0; // same object
                }
            }
        }

        // parse out the acceptLanguage into an array
        TreeMap<ULocaleAcceptLanguageQ, ULocale> map = new TreeMap<ULocaleAcceptLanguageQ, ULocale>();
        StringBuffer languageRangeBuf = new StringBuffer();
        StringBuffer qvalBuf = new StringBuffer();
        int state = 0;
        acceptLanguage += ","; // append comma to simplify the parsing code
        int n;
        boolean subTag = false;
        boolean q1 = false;
        for (n = 0; n < acceptLanguage.length(); n++) {
            boolean gotLanguageQ = false;
            char c = acceptLanguage.charAt(n);
            switch (state) {
            case 0: // before language-range start
                if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                    // in language-range
                    languageRangeBuf.append(c);
                    state = 1;
                    subTag = false;
                } else if (c == '*') {
                    languageRangeBuf.append(c);
                    state = 2;
                } else if (c != ' ' && c != '\t') {
                    // invalid character
                    state = -1;
                }
                break;
            case 1: // in language-range
                if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                    languageRangeBuf.append(c);
                } else if (c == '-') {
                    subTag = true;
                    languageRangeBuf.append(c);
                } else if (c == '_') {
                    if (isLenient) {
                        subTag = true;
                        languageRangeBuf.append(c);
                    } else {
                        state = -1;
                    }
                } else if ('0' <= c && c <= '9') {
                    if (subTag) {
                        languageRangeBuf.append(c);
                    } else {
                        // DIGIT is allowed only in language sub tag
                        state = -1;
                    }
                } else if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c == ' ' || c == '\t') {
                    // language-range end
                    state = 3;
                } else if (c == ';') {
                    // before q
                    state = 4;
                } else {
                    // invalid character for language-range
                    state = -1;
                }
                break;
            case 2: // saw wild card range
                if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c == ' ' || c == '\t') {
                    // language-range end
                    state = 3;
                } else if (c == ';') {
                    // before q
                    state = 4;
                } else {
                    // invalid
                    state = -1;
                }
                break;
            case 3: // language-range end
                if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c == ';') {
                    // before q
                    state =4;
                } else if (c != ' ' && c != '\t') {
                    // invalid
                    state = -1;
                }
                break;
            case 4: // before q
                if (c == 'q') {
                    // before equal
                    state = 5;
                } else if (c != ' ' && c != '\t') {
                    // invalid
                    state = -1;
                }
                break;
            case 5: // before equal
                if (c == '=') {
                    // before q value
                    state = 6;
                } else if (c != ' ' && c != '\t') {
                    // invalid
                    state = -1;
                }
                break;
            case 6: // before q value
                if (c == '0') {
                    // q value start with 0
                    q1 = false;
                    qvalBuf.append(c);
                    state = 7;
                } else if (c == '1') {
                    // q value start with 1
                    qvalBuf.append(c);
                    state = 7;
                } else if (c == '.') {
                    if (isLenient) {
                        qvalBuf.append(c);
                        state = 8;
                    } else {
                        state = -1;
                    }
                } else if (c != ' ' && c != '\t') {
                    // invalid
                    state = -1;
                }
                break;
            case 7: // q value start
                if (c == '.') {
                    // before q value fraction part
                    qvalBuf.append(c);
                    state = 8;
                } else if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c == ' ' || c == '\t') {
                    // after q value
                    state = 10;
                } else {
                    // invalid
                    state = -1;
                }
                break;
            case 8: // before q value fraction part
                if ('0' <= c || c <= '9') {
                    if (q1 && c != '0' && !isLenient) {
                        // if q value starts with 1, the fraction part must be 0
                        state = -1;
                    } else {
                        // in q value fraction part
                        qvalBuf.append(c);
                        state = 9;
                    }
                } else {
                    // invalid
                    state = -1;
                }
                break;
            case 9: // in q value fraction part
                if ('0' <= c && c <= '9') {
                    if (q1 && c != '0') {
                        // if q value starts with 1, the fraction part must be 0
                        state = -1;
                    } else {
                        qvalBuf.append(c);
                    }
                } else if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c == ' ' || c == '\t') {
                    // after q value
                    state = 10;
                } else {
                    // invalid
                    state = -1;
                }
                break;
            case 10: // after q value
                if (c == ',') {
                    // language-q end
                    gotLanguageQ = true;
                } else if (c != ' ' && c != '\t') {
                    // invalid
                    state = -1;
                }
                break;
            }
            if (state == -1) {
                // error state
                throw new ParseException("Invalid Accept-Language", n);
            }
            if (gotLanguageQ) {
                double q = 1.0;
                if (qvalBuf.length() != 0) {
                    try {
                        q = Double.parseDouble(qvalBuf.toString());
                    } catch (NumberFormatException nfe) {
                        // Already validated, so it should never happen
                        q = 1.0;
                    }
                    if (q > 1.0) {
                        q = 1.0;
                    }
                }
                if (languageRangeBuf.charAt(0) != '*') {
                    int serial = map.size();
                    ULocaleAcceptLanguageQ entry = new ULocaleAcceptLanguageQ(q, serial);
                    map.put(entry, new ULocale(canonicalize(languageRangeBuf.toString()))); // sort in reverse order..   1.0, 0.9, 0.8 .. etc                    
                }

                // reset buffer and parse state
                languageRangeBuf.setLength(0);
                qvalBuf.setLength(0);
                state = 0;
            }
        }
        if (state != 0) {
            // Well, the parser should handle all cases.  So just in case.
            throw new ParseException("Invalid AcceptlLanguage", n);
        }

        // pull out the map 
        ULocale acceptList[] = map.values().toArray(new ULocale[map.size()]);
        return acceptList;
    }

    private static final String UNDEFINED_LANGUAGE = "und";
    private static final String UNDEFINED_SCRIPT = "Zzzz";
    private static final String UNDEFINED_REGION = "ZZ";

    /**
     * Add the likely subtags for a provided locale ID, per the algorithm described
     * in the following CLDR technical report:
     *
     *   http://www.unicode.org/reports/tr35/#Likely_Subtags
     *
     * If the provided ULocale instance is already in the maximal form, or there is no
     * data available available for maximization, it will be returned.  For example,
     * "und-Zzzz" cannot be maximized, since there is no reasonable maximization.
     * Otherwise, a new ULocale instance with the maximal form is returned.
     * 
     * Examples:
     *
     * "en" maximizes to "en_Latn_US"
     *
     * "de" maximizes to "de_Latn_US"
     *
     * "sr" maximizes to "sr_Cyrl_RS"
     *
     * "sh" maximizes to "sr_Latn_RS" (Note this will not reverse.)
     *
     * "zh_Hani" maximizes to "zh_Hans_CN" (Note this will not reverse.)
     *
     * @param loc The ULocale to maximize
     * @return The maximized ULocale instance.
     * @stable ICU 4.0
     */
    public static ULocale
    addLikelySubtags(ULocale loc)
    {
        String[] tags = new String[3];
        String trailing = null;
  
        int trailingIndex = parseTagString(
            loc.localeID,
            tags);

        if (trailingIndex < loc.localeID.length()) {
            trailing = loc.localeID.substring(trailingIndex);
        }

        String newLocaleID =
            createLikelySubtagsString(
                (String)tags[0],
                (String)tags[1],
                (String)tags[2],
                trailing);

        return newLocaleID == null ? loc : new ULocale(newLocaleID);
    }

    /**
     * Minimize the subtags for a provided locale ID, per the algorithm described
     * in the following CLDR technical report:
     *
     *   http://www.unicode.org/reports/tr35/#Likely_Subtags
     *
     * If the provided ULocale instance is already in the minimal form, or there
     * is no data available for minimization, it will be returned.  Since the
     * minimization algorithm relies on proper maximization, see the comments
     * for addLikelySubtags for reasons why there might not be any data.
     *
     * Examples:
     *
     * "en_Latn_US" minimizes to "en"
     *
     * "de_Latn_US" minimizes to "de"
     *
     * "sr_Cyrl_RS" minimizes to "sr"
     *
     * "zh_Hant_TW" minimizes to "zh_TW" (The region is preferred to the
     * script, and minimizing to "zh" would imply "zh_Hans_CN".)
     *
     * @param loc The ULocale to minimize
     * @return The minimized ULocale instance.
     * @stable ICU 4.0
     */
    public static ULocale
    minimizeSubtags(ULocale loc)
    {
        String[] tags = new String[3];

        int trailingIndex = parseTagString(
                loc.localeID,
                tags);

        String originalLang = (String)tags[0];
        String originalScript = (String)tags[1];
        String originalRegion = (String)tags[2];
        String originalTrailing = null;

        if (trailingIndex < loc.localeID.length()) {
            /*
             * Create a String that contains everything
             * after the language, script, and region.
             */
            originalTrailing = loc.localeID.substring(trailingIndex);
        }

        /**
         * First, we need to first get the maximization
         * by adding any likely subtags.
         **/
        String maximizedLocaleID =
            createLikelySubtagsString(
                originalLang,
                originalScript,
                originalRegion,
                null);

        /**
         * If maximization fails, there's nothing
         * we can do.
         **/
        if (isEmptyString(maximizedLocaleID)) {
            return loc;
        }
        else {
            /**
             * Start first with just the language.
             **/
            String tag =
                createLikelySubtagsString(
                    originalLang,
                    null,
                    null,
                    null);

            if (tag.equals(maximizedLocaleID)) {
                String newLocaleID =
                    createTagString(
                        originalLang,
                        null,
                        null,
                        originalTrailing);

                return new ULocale(newLocaleID);
            }
        }

        /**
         * Next, try the language and region.
         **/
        if (originalRegion.length() != 0) {

            String tag =
                createLikelySubtagsString(
                    originalLang,
                    null,
                    originalRegion,
                    null);

            if (tag.equals(maximizedLocaleID)) {
                String newLocaleID =
                    createTagString(
                        originalLang,
                        null,
                        originalRegion,
                        originalTrailing);

                return new ULocale(newLocaleID);
            }
        }

        /**
         * Finally, try the language and script.  This is our last chance,
         * since trying with all three subtags would only yield the
         * maximal version that we already have.
         **/
        if (originalRegion.length() != 0 &&
            originalScript.length() != 0) {

            String tag =
                createLikelySubtagsString(
                    originalLang,
                    originalScript,
                    null,
                    null);

            if (tag.equals(maximizedLocaleID)) {
                String newLocaleID =
                    createTagString(
                        originalLang,
                        originalScript,
                        null,
                        originalTrailing);

                return new ULocale(newLocaleID);
            }
        }

        return loc;
    }

    /**
     * A trivial utility function that checks for a null
     * reference or checks the length of the supplied String.
     *
     *   @param string The string to check
     *
     *   @return true if the String is empty, or if the reference is null.
     */
    private static boolean isEmptyString(String string) {
      return string == null || string.length() == 0;
    }
    
    /**
     * Append a tag to a StringBuffer, adding the separator if necessary.The tag must
     * not be a zero-length string.
     *
     * @param tag The tag to add.
     * @param buffer The output buffer.
     **/
    private static void
    appendTag(
        String tag,
        StringBuffer buffer) {
    
        if (buffer.length() != 0) {
            buffer.append(UNDERSCORE);
        }
    
        buffer.append(tag);
    }
    
    /**
     * Create a tag string from the supplied parameters.  The lang, script and region
     * parameters may be null references.
     *
     * If any of the language, script or region parameters are empty, and the alternateTags
     * parameter is not null, it will be parsed for potential language, script and region tags
     * to be used when constructing the new tag.  If the alternateTags parameter is null, or
     * it contains no language tag, the default tag for the unknown language is used.
     *
     * @param lang The language tag to use.
     * @param script The script tag to use.
     * @param region The region tag to use.
     * @param trailing Any trailing data to append to the new tag.
     * @param alternateTags A string containing any alternate tags.
     * @return The new tag string.
     **/
    private static String
    createTagString(
        String lang,
        String script,
        String region,
        String trailing,
        String alternateTags) {

        LocaleIDParser parser = null;
        boolean regionAppended = false;

        StringBuffer tag = new StringBuffer();
    
        if (!isEmptyString(lang)) {
            appendTag(
                lang,
                tag);
        }
        else if (isEmptyString(alternateTags)) {
            /*
             * Append the value for an unknown language, if
             * we found no language.
             */
            appendTag(
                UNDEFINED_LANGUAGE,
                tag);
        }
        else {
            parser = new LocaleIDParser(alternateTags);
    
            String alternateLang = parser.getLanguage();
    
            /*
             * Append the value for an unknown language, if
             * we found no language.
             */
            appendTag(
                !isEmptyString(alternateLang) ? alternateLang : UNDEFINED_LANGUAGE,
                tag);
        }
    
        if (!isEmptyString(script)) {
            appendTag(
                script,
                tag);
        }
        else if (!isEmptyString(alternateTags)) {
            /*
             * Parse the alternateTags string for the script.
             */
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
    
            String alternateScript = parser.getScript();
    
            if (!isEmptyString(alternateScript)) {
                appendTag(
                    alternateScript,
                    tag);
            }
        }
    
        if (!isEmptyString(region)) {
            appendTag(
                region,
                tag);

            regionAppended = true;
        }
        else if (!isEmptyString(alternateTags)) {
            /*
             * Parse the alternateTags string for the region.
             */
            if (parser == null) {
                parser = new LocaleIDParser(alternateTags);
            }
    
            String alternateRegion = parser.getCountry();
    
            if (!isEmptyString(alternateRegion)) {
                appendTag(
                    alternateRegion,
                    tag);

                regionAppended = true;
            }
        }
    
        if (trailing != null && trailing.length() > 1) {
            /*
             * The current ICU format expects two underscores
             * will separate the variant from the preceeding
             * parts of the tag, if there is no region.
             */
            int separators = 0;

            if (trailing.charAt(0) == UNDERSCORE) { 
                if (trailing.charAt(1) == UNDERSCORE) {
                    separators = 2;
                }
                }
                else {
                    separators = 1;
                }

            if (regionAppended) {
                /*
                 * If we appended a region, we may need to strip
                 * the extra separator from the variant portion.
                 */
                if (separators == 2) {
                    tag.append(trailing.substring(1));
                }
                else {
                    tag.append(trailing);
                }
            }
            else {
                /*
                 * If we did not append a region, we may need to add
                 * an extra separator to the variant portion.
                 */
                if (separators == 1) {
                    tag.append(UNDERSCORE);
                }
                tag.append(trailing);
            }
        }
    
        return tag.toString();
    }
    
    /**
     * Create a tag string from the supplied parameters.  The lang, script and region
     * parameters may be null references.If the lang parameter is an empty string, the
     * default value for an unknown language is written to the output buffer.
     *
     * @param lang The language tag to use.
     * @param script The script tag to use.
     * @param region The region tag to use.
     * @param trailing Any trailing data to append to the new tag.
     * @return The new String.
     **/
    static String
    createTagString(
            String lang,
            String script,
            String region,
            String trailing) {
    
        return createTagString(
                    lang,
                    script,
                    region,
                    trailing,
                    null);
    }
    
    /**
     * Parse the language, script, and region subtags from a tag string, and return the results.
     *
     * This function does not return the canonical strings for the unknown script and region.
     *
     * @param localeID The locale ID to parse.
     * @param tags An array of three String references to return the subtag strings.
     * @return The number of chars of the localeID parameter consumed.
     **/
    private static int
    parseTagString(
        String localeID,
        String tags[])
    {
        LocaleIDParser parser = new LocaleIDParser(localeID);
    
        String lang = parser.getLanguage();
        String script = parser.getScript();
        String region = parser.getCountry();
    
        if (isEmptyString(lang)) {
            tags[0] = UNDEFINED_LANGUAGE;
        }
        else {
            tags[0] = lang;
        }
    
        if (script.equals(UNDEFINED_SCRIPT)) {
            tags[1] = "";
        }
        else {
            tags[1] = script;
        }
        
        if (region.equals(UNDEFINED_REGION)) {
            tags[2] = "";
        }
        else {
            tags[2] = region;
        }
    
        /*
         * Search for the variant.  If there is one, then return the index of
         * the preceeding separator.
         * If there's no variant, search for the keyword delimiter,
         * and return its index.  Otherwise, return the length of the
         * string.
         * 
         * $TOTO(dbertoni) we need to take into account that we might
         * find a part of the language as the variant, since it can
         * can have a variant portion that is long enough to contain
         * the same characters as the variant. 
         */
        String variant = parser.getVariant();
    
        if (!isEmptyString(variant)){
            int index = localeID.indexOf(variant); 

            
            return  index > 0 ? index - 1 : index;
        }
        else
        {
            int index = localeID.indexOf('@');
    
            return index == -1 ? localeID.length() : index;
        }
    }
    
    private static String
    lookupLikelySubtags(String localeId) {
        UResourceBundle bundle =
            UResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_BASE_NAME, "likelySubtags");
        try {
            return bundle.getString(localeId);
        }
        catch(MissingResourceException e) {
            return null;
        }
    }

    private static String
    createLikelySubtagsString(
        String lang,
        String script,
        String region,
        String variants) {
    
        /**
         * Try the language with the script and region first.
         **/
        if (!isEmptyString(script) && !isEmptyString(region)) {
    
            String searchTag =
                createTagString(
                    lang,
                    script,
                    region,
                    null);
    
            String likelySubtags = lookupLikelySubtags(searchTag);

            /*
            if (likelySubtags == null) {
                if (likelySubtags2 != null) {
                    System.err.println("Tag mismatch: \"(null)\" \"" + likelySubtags2 + "\"");
                }
            }
            else if (likelySubtags2 == null) {
                System.err.println("Tag mismatch: \"" + likelySubtags + "\" \"(null)\"");
            }
            else if (!likelySubtags.equals(likelySubtags2)) {
                System.err.println("Tag mismatch: \"" + likelySubtags + "\" \"" + likelySubtags2 + "\"");
            }
            */
            if (likelySubtags != null) {
                // Always use the language tag from the
                // maximal string, since it may be more
                // specific than the one provided.
                return createTagString(
                            null,
                            null,
                            null,
                            variants,
                            likelySubtags);
            }
        }
    
        /**
         * Try the language with just the script.
         **/
        if (!isEmptyString(script)) {
    
            String searchTag =
                createTagString(
                    lang,
                    script,
                    null,
                    null);
    
            String likelySubtags = lookupLikelySubtags(searchTag);    
            if (likelySubtags != null) {
                // Always use the language tag from the
                // maximal string, since it may be more
                // specific than the one provided.
                return createTagString(
                            null,
                            null,
                            region,
                            variants,
                            likelySubtags);
            }
        }
    
        /**
         * Try the language with just the region.
         **/
        if (!isEmptyString(region)) {
    
            String searchTag =
                createTagString(
                    lang,
                    null,
                    region,
                    null);
    
            String likelySubtags = lookupLikelySubtags(searchTag);    
    
            if (likelySubtags != null) {
                // Always use the language tag from the
                // maximal string, since it may be more
                // specific than the one provided.
                return createTagString(
                            null,
                            script,
                            null,
                            variants,
                            likelySubtags);
            }
        }
    
        /**
         * Finally, try just the language.
         **/
        {
            String searchTag =
                createTagString(
                    lang,
                    null,
                    null,
                    null);
    
            String likelySubtags = lookupLikelySubtags(searchTag);    
  
            if (likelySubtags != null) {
                // Always use the language tag from the
                // maximal string, since it may be more
                // specific than the one provided.
                return createTagString(
                            null,
                            script,
                            region,
                            variants,
                            likelySubtags);
            }
        }
    
        return null;
    }

    // --------------------------------
    //      BCP47/OpenJDK APIs
    // --------------------------------

    /**
     * The key for private use locale extension.
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    static public final char PRIVATE_USE_EXTENSION = 'x';

    /**
     * The key for LDML extension.
     * @see #getExtension(char)
     * @see Builder#setExtension(char, String)
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    static public final char LDML_EXTENSION = 'u';

    /**
     * Returns the extension associated with the specified extension key, or
     * null if there is no extension associated with the key.  The key must
     * be one of <code>[0-9A-Za-z]</code>.
     * <BR><STRONG>Note:</STRONG>Extension key 'x' and 'X' is reserved for BCP47
     * private use.  To get the private use value, use <code>PRIVATE_USE_KEY</code>.
     * <p>
     * @param key the extension key
     * @return the extension, or null if this locale 
     * defines no extension for the specified key.
     * @throws IllegalArgumentException if the key is not valid.
     * @see #PRIVATE_USE_EXTENSION
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public String getExtension(char key) {
        if (!LocaleExtensions.isValidExtensionKey(key)) {
            throw new IllegalArgumentException("Invalid extension key: " + key);
        }
        return extensions().getExtensionValue(key);
    }

    /**
     * Returns the set of extension keys associated with this locale, or null
     * if it has no extensions.  The returned set is immutable.
     * @return the set of extension keys, or null if this locale has
     * no extensions.
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public Set<Character> getExtensionKeys() {
        return extensions().getExtensionKeys();
    }

    /**
     * Returns the LDML keyword value ('type') associated with
     * the specified LDML key for this locale.  LDML keywords are specified
     * by the 'u' extension and consist of key/type pairs.  The key must be
     * two alphanumeric characters in length, or an IllegalArgumentException
     * is thrown.
     * @param key the LDML key
     * @return the value ('type') associated with the key, or null if the 
     * locale does not define a value for the key.
     * @throws IllegalArgumentException if the key is not valid.
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public String getLDMLExtensionValue(String key) {
        if (!LocaleExtensions.isValidLDMLKey(key)) {
            throw new IllegalArgumentException("Invalid LDML key: " + key);
        }
        return extensions().getLDMLKeywordType(key);
    }

    /**
     * Returns the set of keys for LDML keywords defined by this locale, or
     * null if this locale has no locale extension.  The returned set is
     * immutable.
     * @return The set of the LDML keys, or null
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public Set<String> getLDMLExtensionKeys() {
        return extensions().getLDMLKeywordKeys();
    }

    /**
     * Returns a well-formed language tag representing this locale.
     * <p>
     * <b>Note</b>: If the language, country, or variant fields do
     * not satisfy BCP47 language tag syntax requirements, they are
     * omitted from the result.  For example, using the constructor it
     * is possible to create a Locale instance with digits in the
     * language field, or only two characters in the variant field.
     * Since these are not well-formed BCP47 language tag syntax, they
     * cannot be expressed in BCP47.  Since such 'legacy' locales lose
     * information when converting to BCP47, it is
     * recommended that clients switch to conforming locales.
     * <p>
     * <b>Note</b>: Underscores in the variant tag are normalized to
     * hyphen, and all fields, keys, and values are normalized to
     * lower case.
     * @return a BCP47 language tag representing the locale.
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public String toLanguageTag() {
        return LanguageTag.toLanguageTag(base(), extensions());
    }

    /**
     * Returns a locale for the specified language tag string.  If the
     * specified language tag contains any ill-formed subtags, the first
     * such subtag and all following subtags are ignored.
     * <p>
     * This implements the 'Language-Tag' production of BCP47, and so supports
     * grandfathered (regular and irregular) as well as private use language
     * tags.  Private use tags are represented as 'und-x-whatever', and 
     * grandfathered tags are converted to their canonical replacements where
     * they exist.  Note that a few grandfathered tags have no modern replacement,
     * these will be converted using the fallback described in the first paragraph,
     * so some information might be lost.
     * <p>
     * For a list of grandfathered tags, see 
     * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ltru-4646bis-21.txt">
     * RFC4646</a> 
     * (<span style="background-color: #00ccff; font-weight: bold">Currently Draft, 
     * remove or reference final version before release.</span>)
     * @param langtag the language tag
     * @return the locale that best represents the language tag
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public static ULocale forLanguageTag(String langtag) {
//        ULocale locale = ULocale.ROOT;
        ULocale locale = new ULocale("");
        LanguageTag tag = null;
        while (true) {
            try {
                tag = LanguageTag.parse(langtag);

                Builder bldr = new Builder();

                String extlang = tag.getExtlang(0);
                String language = (extlang == null) ? tag.getLanguage() : extlang;
                // do nothing with language code "und"
                if (!language.equals("und")) {
                    bldr.setLanguage(language);
                }

                bldr.setScript(tag.getScript())
                    .setRegion(tag.getRegion()).setVariant(tag.getVariant());

                // setExtension may throw an exception if
                // it contains malformed LDML keys.
                Set<Extension> exts = tag.getExtensions();
                if (exts != null) {
                    for (Extension e : exts) {
                        bldr.setExtension(e.getSingleton(), e.getValue());
                    }
                }
                bldr.setExtension(PRIVATE_USE_EXTENSION, tag.getPrivateUse());
                locale = bldr.create();
                break;
            } catch (LocaleSyntaxException e) {
                // this exception was thrown by LanguageTag#parse
                // - fall through
            } catch (IllformedLocaleException e) {
                // this expection was thrown by setExtension with
                // malformed LDML keys - fall through
            }
            // remove the last subtag and try it again
            int idx = langtag.lastIndexOf('-');
            if (idx == -1) {
                // no more subtags
                break;
            }
            langtag = langtag.substring(0, idx);
        }

        return locale;
    }


    /**
     * Builder is used to build instances of Locale from values 
     * configured by the setter.  
     * <p>
     * Builder supports the 'langtag' production of RFC 4646.
     * Language tags consist of the ASCII digits, upper and lower case
     * letters, and hyphen (which appears only as a field separator).
     * As a convenience, underscores are accepted and normalized to
     * hyphen.  Values with any other character are ill-formed.  Since
     * language tags are case-insensitive, they are normalized
     * to lower case, case distinctions are <b>not</b>
     * preserved by the builder.
     * <p>
     * Note that since this implements 'langtag' and not 'Language-Tag',
     * grandfathered language tags are not supported by the builder.
     * Clients should use {@link #forLanguageTag} instead.
     * <p> 
     * Builders can be reused; <code>clear()</code> resets all fields
     * to their default values.
     * @see Builder#create
     * @see Builder#clear
     * 
     * @draft ICU 4.2
     * @provisional This API might change or be removed in a future release.
     */
    public static final class Builder {

        private InternalLocaleBuilder _locbld = new InternalLocaleBuilder();

        /**
         * Constructs an empty Builder.
         * The default values of all fields, extensions, and private
         * use information are empty, the language is undefined.
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder() {
        }

        /**
         * Resets the builder to match the provided locale.  The previous state
         * of the builder is discarded.  Fields that do not
         * conform to BCP47 syntax are ill-formed.
         * @param loc the locale
         * @return this builder
         * @throws IllformedLocaleException if <code>loc</code> has any ill-formed
         * fields.
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setLocale(ULocale loc) {
            clear();
            setLanguage(loc.getLanguage())
                .setScript(loc.getScript())
                .setRegion(loc.getCountry())
                .setVariant(loc.getVariant());

            Set<Character> extKeys = loc.getExtensionKeys();
            if (extKeys != null) {
                for (Character k : extKeys) {
                    char key = k.charValue();
                    String value = loc.getExtension(key);
                    if (value != null && value.length() > 0) {
                        setExtension(key, value);
                    }
                }
            }
            return this;
        }

        /**
         * Resets the builder to match the provided language tag.  The previous state
         * of the builder is discarded.
         * @param langtag the language tag
         * @return this builder
         * @throws IllformedLocaleException if <code>langtag</code> is ill-formed.
         * @see #forLanguageTag(String)
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setLanguageTag(String langtag) {
            clear();
            LanguageTag tag = null;
            try {
                tag = LanguageTag.parse(langtag);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }

            // base locale fields
            String extlang = tag.getExtlang(0);
            String language = extlang == null ? tag.getLanguage() : extlang;
            setLanguage(language.equals("und") ? "": language).setScript(tag.getScript())
                .setRegion(tag.getRegion()).setVariant(tag.getVariant());

            // extensions
            Set<Extension> exts = tag.getExtensions();
            if (exts != null) {
                for (Extension e : exts) {
                    setExtension(e.getSingleton(), e.getValue());
                    //TODO: setExtension may throw an IllformedLocaleException.
                    //      In this csae, error index must be recalculated.
                }
            }
            // private use
            setExtension(PRIVATE_USE_EXTENSION, tag.getPrivateUse());
            return this;
        }

        /**
         * Sets the language.  If language is the empty string,
         * the language is defaulted.  Language should be a two or 
         * three-letter language code as defined in ISO639.
         * Well-formed values are any string of two to eight ASCII letters.
         * @param language the language
         * @return this builder
         * @throws IllformedLocaleException if <code>language</code> is ill-formed
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setLanguage(String language) {
            try {
                _locbld.setLanguage(language);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the script.  If script is 
         * the empty string, the script is defaulted.  Scripts should
         * be a four-letter script code as defined in ISO 15924.
         * Well-formed values are any string of four ASCII letters.
         * @param script the script
         * @return this builder
         * @throws IllformedLocaleException if <code>script</code> is ill-formed
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setScript(String script) {
            try {
                _locbld.setScript(script);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the region.  If region is
         * the empty string, the region is defaulted.  Regions should
         * be a two-letter ISO 3166 code or a three-digit M. 49 code.
         * Well-formed values are any two-letter or three-digit
         * string.
         * @param region the region
         * @return this builder
         * @throws IllformedLocaleException if <code>region</code> is ill-formed
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setRegion(String region) {
            try {
                _locbld.setRegion(region);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the variant.  If variant is
         * or the empty string, the variant is defaulted.  Variants
         * should be registered variants (see 
         * <a href="http://www.iana.org/assignments/language-subtag-registry">
         * IANA Language Subtag Registry</a>) for the prefix.  Well-formed
         * variants are any series of fields of either four characters
         * starting with a digit, or five to eight alphanumeric
         * characters, separated by hyphen or underscore.
         * @param variant the variant
         * @return this builder
         * @throws IllformedLocaleException if <code>variant</code> is ill-formed
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setVariant(String variant) {
            try {
                _locbld.setVariant(variant);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the extension for the given key.  If the value is
         * the empty string, the extension is removed.  Legal
         * keys are the <code>[0-9A-WY-Za-wy-z]</code>.  Well-formed
         * values are any series of fields of two to eight
         * alphanumeric characters, separated by hyphen or underscore.
         * <p>
         * <b>note</b>:The extension 'u' is used for LDML Keywords.
         * Setting the 'u' extension replaces any existing LDML
         * keywords with those defined in the extension.  To be
         * well-formed, a value for the 'u' extension must meet the
         * additional constraint that the number of fields be even
         * (fields represent key value pairs, where the value is
         * mandatory), and that the keys and values be legal locale
         * extension keys and values.
         * @param key the extension key
         * @param value the extension value
         * @return this builder
         * @throws IllformedLocaleException if <code>key</code> is illegal 
         * or <code>value</code> is ill-formed
         * @see #setLDMLExtensionValue
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setExtension(char key, String value) {
            try {
                _locbld.setExtension(key, value);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Sets the LDML keyword value ('type') for the given key.  If the
         * value is the empty string, the LDML keyword is removed.
         * Well-formed keys are strings of two alphanumeric characters. Well-formed
         * values are strings of three to eight alphanumeric characters.
         * <p>
         * <b>Note</b>:Setting the 'u' extension replaces all LDML
         * keywords with those defined in the extension.
         * @param key the LDML extension key
         * @param value the LDML extension value
         * @return this builder
         * @throws IllformedLocaleException if <code>key</code> or <code>value</code>
         * is ill-formed
         * @see #setExtension(char, String)
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder setLDMLExtensionValue(String key, String value) {
            try {
                _locbld.setLDMLExtensionValue(key, value);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }

        /**
         * Resets the builder to its initial, default state.
         * @return this builder
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder clear() {
            _locbld.clear();
            return this;
        }

        /**
         * Resets the extensions to their initial, default state.
         * Language, script, region and variant are unchanged.
         * @return this builder
         * @see #setExtension(char, String)
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public Builder clearExtensions() {
            _locbld.removeLocaleExtensions();
            return this;
        }

        /**
         * Returns an instance of locale created from the fields set
         * on this builder.
         * @return a new locale
         * 
         * @draft ICU 4.2
         * @provisional This API might change or be removed in a future release.
         */
        public ULocale create() {
            return getInstance(_locbld.getBaseLocale(), _locbld.getLocaleExtensions());
        }
    }

    private static ULocale getInstance(BaseLocale base, LocaleExtensions ext) {
        StringBuffer id = new StringBuffer(base.getID());

        TreeMap<String, String> kwds = null;
        Set<Character> extKeys = ext.getExtensionKeys();
        if (extKeys != null) {
            // legacy locale ID assume LDML keywords and
            // other extensions are at the same level.
            // e.g. @a=ext-for-aa;calendar=japanese;m=ext-for-mm;x=priv-use
            kwds = new TreeMap<String, String>();
            boolean hasLDMLKeywords = false;
            for (Character key : extKeys) {
                if (key.charValue() == 'u') {
                    // LDML keywords
                    hasLDMLKeywords = true;
                    continue;
                }
                String value = ext.getExtensionValue(key.charValue());
                kwds.put(String.valueOf(key), value);
            }

            if (hasLDMLKeywords) {
                Set<String> ldmlKeys = ext.getLDMLKeywordKeys();
                if (ldmlKeys != null) {
                    for (String bcpKey : ldmlKeys) {
                        String bcpValue = ext.getLDMLKeywordType(bcpKey);
                        // transform to legacy key/type
                        String lkey = bcp47ToLDMLKey(bcpKey);
                        String ltype = bcp47ToLDMLType(lkey, bcpValue);
                        kwds.put(lkey, ltype);
                    }
                }
            }

            if (kwds.size() > 0) {
                id.append("@");
                Set<Map.Entry<String, String>> kset = kwds.entrySet();
                boolean insertSep = false;
                for (Map.Entry<String, String> kwd : kset) {
                    if (insertSep) {
                        id.append(";");
                    } else {
                        insertSep = true;
                    }
                    id.append(kwd.getKey());
                    id.append("=");
                    id.append(kwd.getValue());
                }
            }
        }

        return new ULocale(id.toString());
    }

    private BaseLocale base() {
        String language = getLanguage();
        if (equals(ULocale.ROOT)) {
            language = "";
        }
        return BaseLocale.getInstance(language, getScript(), getCountry(), getVariant());
    }

    private LocaleExtensions extensions() {
        Iterator<String> kwitr = getKeywords();
        if (kwitr == null) {
            return LocaleExtensions.EMPTY_EXTENSIONS;
        }

        TreeMap<Character, String> extMap = null;
        TreeMap<String, String> ldmlKwMap = null;

        while (kwitr.hasNext()) {
            String key = (String)kwitr.next();
            String value = getKeywordValue(key);
            if (key.length() == 1) {
                // non LDML extension or private use
                // We want to keep only valid subtags
                boolean isPrivUse = (key.charAt(0) == PRIVATE_USE_EXTENSION);
                if (isPrivUse || LanguageTag.isExtensionSingleton(key)) {
                    boolean isValid = true;
                    String[] subtags = Utility.split(value, '-');
                    for (int i = 0; i < subtags.length; i++) {
                        if (isPrivUse) {
                            if (!LanguageTag.isPrivateuseValueSubtag(subtags[i])) {
                                isValid = false;
                                break;
                            }
                        } else {
                            if (!LanguageTag.isExtensionSubtag(subtags[i])) {
                                isValid = false;
                                break;
                            }
                        }
                    }
                    if (isValid) {
                        if (extMap == null) {
                            extMap = new TreeMap<Character, String>();
                        }
                        extMap.put(new Character(key.charAt(0)), value.intern());
                    }
                }
            } else {
                // LDML keyword
                String bcpKey = ldmlKeyToBCP47(key);
                String bcpVal = ldmlTypeToBCP47(key, value);
                if (bcpKey != null && bcpVal != null) {
                    if (ldmlKwMap == null) {
                        ldmlKwMap = new TreeMap<String, String>();
                    }
                    ldmlKwMap.put(bcpKey.intern(), bcpVal.intern());
                }
            }
        }

        if (ldmlKwMap != null) {
            // create LDML extension string
            StringBuilder buf = new StringBuilder();
            LocaleExtensions.keywordsToString(ldmlKwMap, buf);
            if (extMap == null) {
                extMap = new TreeMap<Character, String>();
            }
            extMap.put(new Character('u'), buf.toString().intern());
        }

        return LocaleExtensions.getInstance(extMap, ldmlKwMap);
    }

    //
    // LDML legacy/BCP47 key and type mapping functions
    //
    private static String ldmlKeyToBCP47(String key) {
        UResourceBundle supplemental = UResourceBundle.getBundleInstance(
                                            ICUResourceBundle.ICU_BASE_NAME,
                                            "supplementalData",
                                            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle bcp47Mappings = supplemental.get("bcp47KeywordMappings");
        UResourceBundle keyMaps = bcp47Mappings.get("key");

        // normalize key to lowercase
        key = AsciiUtil.toLowerString(key);
        String bcpKey = null;
        try {
            bcpKey = keyMaps.getString(key);
        } catch (MissingResourceException mre) {
            // fall through
        }

        if (bcpKey == null) {
            if (key.length() == 2 && LanguageTag.isExtensionSubtag(key)) {
                return key;
            }
            return null;
        }
        return bcpKey;
    }

    private static String bcp47ToLDMLKey(String bcpKey) {
        UResourceBundle supplemental = UResourceBundle.getBundleInstance(
                                            ICUResourceBundle.ICU_BASE_NAME,
                                            "supplementalData",
                                            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle bcp47Mappings = supplemental.get("bcp47KeywordMappings");
        UResourceBundle keyMaps = bcp47Mappings.get("key");

        // normalize bcp key to lowercase
        bcpKey = AsciiUtil.toLowerString(bcpKey);
        String key = null;
        for (int i = 0; i < keyMaps.getSize(); i++) {
            UResourceBundle keyMap = keyMaps.get(i);
            if (bcpKey.equals(keyMap.getString())) {
                key = keyMap.getKey();
                break;
            }
        }
        if (key == null) {
            return bcpKey;
        }
        return key;
    }

    private static String ldmlTypeToBCP47(String key, String type) {
        UResourceBundle supplemental = UResourceBundle.getBundleInstance(
                                            ICUResourceBundle.ICU_BASE_NAME,
                                            "supplementalData",
                                            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle bcp47Mappings = supplemental.get("bcp47KeywordMappings");

        // normalize key/type to lowercase
        key = AsciiUtil.toLowerString(key);
        type = AsciiUtil.toLowerString(type);
        String bcpType = null;
        try {
            UResourceBundle typeMaps = bcp47Mappings.get(key);
            String tmp = key.equals("timezone") ? type.replaceAll("/", ":") : type;
            bcpType = typeMaps.getString(tmp);
        } catch (MissingResourceException mre) {
            // fall through
        }
        
        if (bcpType == null) {
            int typeLen = type.length();
            if (typeLen >= 3 && typeLen <= 8 && LanguageTag.isExtensionSubtag(type)) {
                return type;
            }
            return null;
        }
        return bcpType;
    }

    private static String bcp47ToLDMLType(String key, String bcpType) {
        UResourceBundle supplemental = UResourceBundle.getBundleInstance(
                ICUResourceBundle.ICU_BASE_NAME,
                "supplementalData",
                ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle bcp47Mappings = supplemental.get("bcp47KeywordMappings");

        // normalize key/bcpType to lowercase
        key = AsciiUtil.toLowerString(key);
        bcpType = AsciiUtil.toLowerString(bcpType);

        String type = null;
        try {
            UResourceBundle typeMaps = bcp47Mappings.get(key);

            // Note:    Linear search for time zone ID might be too slow.
            //          ICU services do not use timezone keywords for now.
            //          In future, we may need to build the optimized inverse
            //          lookup table.

            for (int i = 0; i < typeMaps.getSize(); i++) {
                UResourceBundle typeMap = typeMaps.get(i);
                if (bcpType.equals(typeMap.getString())) {
                    type = typeMap.getKey();
                    if (key.equals("timezone")) {
                        type = type.replaceAll(":", "/");
                    }
                    break;
                }
            }
        } catch (MissingResourceException mre) {
            // fall through
        }

        if (type == null) {
            return bcpType;
        }
        return type;
    }
}
