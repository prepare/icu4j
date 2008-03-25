/*
******************************************************************************
* Copyright (C) 2003-2008, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.util;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;

import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.LocaleUtility;

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
    private static final String EMPTY_STRING = "";

    // Used in both ULocale and IDParser, so moved up here.
    private static final char UNDERSCORE            = '_';

    // default empty locale
    private static final Locale EMPTY_LOCALE = new Locale("", "");

    /**
     * The root ULocale.
     * @stable ICU 2.8
     */ 
    public static final ULocale ROOT = new ULocale("root", EMPTY_LOCALE);
    
    private static final SimpleCache CACHE = new SimpleCache();

    /**
     * Cache the locale.
     */
    private transient Locale locale;

    /**
     * The raw localeID that we were passed in.
     */
    private String localeID;

    /**
     * Tables used in normalizing portions of the id.
     */
    /* tables updated per http://lcweb.loc.gov/standards/iso639-2/ 
       to include the revisions up to 2001/7/27 *CWB*/
    /* The 3 character codes are the terminology codes like RFC 3066.  
       This is compatible with prior ICU codes */
    /* "in" "iw" "ji" "jw" & "sh" have been withdrawn but are still in 
       the table but now at the end of the table because 
       3 character codes are duplicates.  This avoids bad searches
       going from 3 to 2 character codes.*/
    /* The range qaa-qtz is reserved for local use. */

    private static String[] _languages;
    private static String[] _replacementLanguages;
    private static String[] _obsoleteLanguages;
    private static String[] _languages3;
    private static String[] _obsoleteLanguages3;

    // Avoid initializing languages tables unless we have to.
    private static void initLanguageTables() {
        if (_languages == null) {

            /* This list MUST be in sorted order, and MUST contain the two-letter codes
               if one exists otherwise use the three letter code */
            String[] tempLanguages = {
                "aa",  "ab",  "ace", "ach", "ada", "ady", "ae",  "af",  "afa",
                "afh", "ak",  "akk", "ale", "alg", "am",  "an",  "ang", "apa",
                "ar",  "arc", "arn", "arp", "art", "arw", "as",  "ast",
                "ath", "aus", "av",  "awa", "ay",  "az",  "ba",  "bad",
                "bai", "bal", "ban", "bas", "bat", "be",  "bej",
                "bem", "ber", "bg",  "bh",  "bho", "bi",  "bik", "bin",
                "bla", "bm",  "bn",  "bnt", "bo",  "br",  "bra", "bs",
                "btk", "bua", "bug", "byn", "ca",  "cad", "cai", "car", "cau",
                "ce",  "ceb", "cel", "ch",  "chb", "chg", "chk", "chm",
                "chn", "cho", "chp", "chr", "chy", "cmc", "co",  "cop",
                "cpe", "cpf", "cpp", "cr",  "crh", "crp", "cs",  "csb", "cu",  "cus",
                "cv",  "cy",  "da",  "dak", "dar", "day", "de",  "del", "den",
                "dgr", "din", "doi", "dra", "dsb", "dua", "dum", "dv",  "dyu",
                "dz",  "ee",  "efi", "egy", "eka", "el",  "elx", "en",
                "enm", "eo",  "es",  "et",  "eu",  "ewo", "fa",
                "fan", "fat", "ff",  "fi",  "fiu", "fj",  "fo",  "fon",
                "fr",  "frm", "fro", "fur", "fy",  "ga",  "gaa", "gay",
                "gba", "gd",  "gem", "gez", "gil", "gl",  "gmh", "gn",
                "goh", "gon", "gor", "got", "grb", "grc", "gu",  "gv",
                "gwi", "ha",  "hai", "haw", "he",  "hi",  "hil", "him",
                "hit", "hmn", "ho",  "hr",  "hsb", "ht",  "hu",  "hup", "hy",  "hz",
                "ia",  "iba", "id",  "ie",  "ig",  "ii",  "ijo", "ik",
                "ilo", "inc", "ine", "inh", "io",  "ira", "iro", "is",  "it",
                "iu",  "ja",  "jbo", "jpr", "jrb", "jv",  "ka",  "kaa", "kab",
                "kac", "kam", "kar", "kaw", "kbd", "kg",  "kha", "khi",
                "kho", "ki",  "kj",  "kk",  "kl",  "km",  "kmb", "kn",
                "ko",  "kok", "kos", "kpe", "kr",  "krc", "kro", "kru", "ks",
                "ku",  "kum", "kut", "kv",  "kw",  "ky",  "la",  "lad",
                "lah", "lam", "lb",  "lez", "lg",  "li",  "ln",  "lo",  "lol",
                "loz", "lt",  "lu",  "lua", "lui", "lun", "luo", "lus",
                "lv",  "mad", "mag", "mai", "mak", "man", "map", "mas",
                "mdf", "mdr", "men", "mg",  "mga", "mh",  "mi",  "mic", "min",
                "mis", "mk",  "mkh", "ml",  "mn",  "mnc", "mni", "mno",
                "mo",  "moh", "mos", "mr",  "ms",  "mt",  "mul", "mun",
                "mus", "mwr", "my",  "myn", "myv", "na",  "nah", "nai", "nap",
                "nb",  "nd",  "nds", "ne",  "new", "ng",  "nia", "nic",
                "niu", "nl",  "nn",  "no",  "nog", "non", "nr",  "nso", "nub",
                "nv",  "nwc", "ny",  "nym", "nyn", "nyo", "nzi", "oc",  "oj",
                "om",  "or",  "os",  "osa", "ota", "oto", "pa",  "paa",
                "pag", "pal", "pam", "pap", "pau", "peo", "phi", "phn",
                "pi",  "pl",  "pon", "pra", "pro", "ps",  "pt",  "qu",
                "raj", "rap", "rar", "rm",  "rn",  "ro",  "roa", "rom",
                "ru",  "rup", "rw",  "sa",  "sad", "sah", "sai", "sal", "sam",
                "sas", "sat", "sc",  "sco", "sd",  "se",  "sel", "sem",
                "sg",  "sga", "sgn", "shn", "si",  "sid", "sio", "sit",
                "sk",  "sl",  "sla", "sm",  "sma", "smi", "smj", "smn",
                "sms", "sn",  "snk", "so",  "sog", "son", "sq",  "sr",
                "srr", "ss",  "ssa", "st",  "su",  "suk", "sus", "sux",
                "sv",  "sw",  "syr", "ta",  "tai", "te",  "tem", "ter",
                "tet", "tg",  "th",  "ti",  "tig", "tiv", "tk",  "tkl",
                "tl",  "tlh", "tli", "tmh", "tn",  "to",  "tog", "tpi", "tr",
                "ts",  "tsi", "tt",  "tum", "tup", "tut", "tvl", "tw",
                "ty",  "tyv", "udm", "ug",  "uga", "uk",  "umb", "und", "ur",
                "uz",  "vai", "ve",  "vi",  "vo",  "vot", "wa",  "wak",
                "wal", "war", "was", "wen", "wo",  "xal", "xh",  "yao", "yap",
                "yi",  "yo",  "ypk", "za",  "zap", "zen", "zh",  "znd",
                "zu",  "zun", 
            };

            String[] tempReplacementLanguages = {
                "id", "he", "yi", "jv", "sr", "nb",/* replacement language codes */
            };

            String[] tempObsoleteLanguages = {
                "in", "iw", "ji", "jw", "sh", "no",    /* obsolete language codes */         
            };

            /* This list MUST contain a three-letter code for every two-letter code in the
               list above, and they MUST ne in the same order (i.e., the same language must
               be in the same place in both lists)! */
            String[] tempLanguages3 = {
                /*"aa",  "ab",  "ace", "ach", "ada", "ady", "ae",  "af",  "afa",    */
                "aar", "abk", "ace", "ach", "ada", "ady", "ave", "afr", "afa",
                /*"afh", "ak",  "akk", "ale", "alg", "am",  "an",  "ang", "apa",    */
                "afh", "aka", "akk", "ale", "alg", "amh", "arg", "ang", "apa",
                /*"ar",  "arc", "arn", "arp", "art", "arw", "as",  "ast",    */
                "ara", "arc", "arn", "arp", "art", "arw", "asm", "ast",
                /*"ath", "aus", "av",  "awa", "ay",  "az",  "ba",  "bad",    */
                "ath", "aus", "ava", "awa", "aym", "aze", "bak", "bad",
                /*"bai", "bal", "ban", "bas", "bat", "be",  "bej",    */
                "bai", "bal", "ban", "bas", "bat", "bel", "bej",
                /*"bem", "ber", "bg",  "bh",  "bho", "bi",  "bik", "bin",    */
                "bem", "ber", "bul", "bih", "bho", "bis", "bik", "bin",
                /*"bla", "bm",  "bn",  "bnt", "bo",  "br",  "bra", "bs",     */
                "bla", "bam",  "ben", "bnt", "bod", "bre", "bra", "bos",
                /*"btk", "bua", "bug", "byn", "ca",  "cad", "cai", "car", "cau",    */
                "btk", "bua", "bug", "byn", "cat", "cad", "cai", "car", "cau",
                /*"ce",  "ceb", "cel", "ch",  "chb", "chg", "chk", "chm",    */
                "che", "ceb", "cel", "cha", "chb", "chg", "chk", "chm",
                /*"chn", "cho", "chp", "chr", "chy", "cmc", "co",  "cop",    */
                "chn", "cho", "chp", "chr", "chy", "cmc", "cos", "cop",
                /*"cpe", "cpf", "cpp", "cr",  "crh", "crp", "cs",  "csb", "cu",  "cus",    */
                "cpe", "cpf", "cpp", "cre", "crh", "crp", "ces", "csb", "chu", "cus",
                /*"cv",  "cy",  "da",  "dak", "dar", "day", "de",  "del", "den",    */
                "chv", "cym", "dan", "dak", "dar", "day", "deu", "del", "den",
                /*"dgr", "din", "doi", "dra", "dsb", "dua", "dum", "dv",  "dyu",    */
                "dgr", "din", "doi", "dra", "dsb", "dua", "dum", "div", "dyu",
                /*"dz",  "ee",  "efi", "egy", "eka", "el",  "elx", "en",     */
                "dzo", "ewe", "efi", "egy", "eka", "ell", "elx", "eng",
                /*"enm", "eo",  "es",  "et",  "eu",  "ewo", "fa",     */
                "enm", "epo", "spa", "est", "eus", "ewo", "fas",
                /*"fan", "fat", "ff",  "fi",  "fiu", "fj",  "fo",  "fon",    */
                "fan", "fat", "ful", "fin", "fiu", "fij", "fao", "fon",
                /*"fr",  "frm", "fro", "fur", "fy",  "ga",  "gaa", "gay",    */
                "fra", "frm", "fro", "fur", "fry", "gle", "gaa", "gay",
                /*"gba", "gd",  "gem", "gez", "gil", "gl",  "gmh", "gn",     */
                "gba", "gla", "gem", "gez", "gil", "glg", "gmh", "grn",
                /*"goh", "gon", "gor", "got", "grb", "grc", "gu",  "gv",     */
                "goh", "gon", "gor", "got", "grb", "grc", "guj", "glv",
                /*"gwi", "ha",  "hai", "haw", "he",  "hi",  "hil", "him",    */
                "gwi", "hau", "hai", "haw", "heb", "hin", "hil", "him",
                /*"hit", "hmn", "ho",  "hr",  "hsb", "ht",  "hu",  "hup", "hy",  "hz",     */
                "hit", "hmn", "hmo", "hrv", "hsb", "hat", "hun", "hup", "hye", "her",
                /*"ia",  "iba", "id",  "ie",  "ig",  "ii",  "ijo", "ik",     */
                "ina", "iba", "ind", "ile", "ibo", "iii", "ijo", "ipk",
                /*"ilo", "inc", "ine", "inh", "io",  "ira", "iro", "is",  "it",      */
                "ilo", "inc", "ine", "inh", "ido", "ira", "iro", "isl", "ita",
                /*"iu",  "ja",  "jbo", "jpr", "jrb", "jv",  "ka",  "kaa", "kab",   */
                "iku", "jpn", "jbo", "jpr", "jrb", "jaw", "kat", "kaa", "kab",
                /*"kac", "kam", "kar", "kaw", "kbd", "kg",  "kha", "khi",    */
                "kac", "kam", "kar", "kaw", "kbd", "kon", "kha", "khi",
                /*"kho", "ki",  "kj",  "kk",  "kl",  "km",  "kmb", "kn",     */
                "kho", "kik", "kua", "kaz", "kal", "khm", "kmb", "kan",
                /*"ko",  "kok", "kos", "kpe", "kr",  "krc", "kro", "kru", "ks",     */
                "kor", "kok", "kos", "kpe", "kau", "krc", "kro", "kru", "kas",
                /*"ku",  "kum", "kut", "kv",  "kw",  "ky",  "la",  "lad",    */
                "kur", "kum", "kut", "kom", "cor", "kir", "lat", "lad",
                /*"lah", "lam", "lb",  "lez", "lg",  "li",  "ln",  "lo",  "lol",    */
                "lah", "lam", "ltz", "lez", "lug", "lim", "lin", "lao", "lol",
                /*"loz", "lt",  "lu",  "lua", "lui", "lun", "luo", "lus",    */
                "loz", "lit", "lub", "lua", "lui", "lun", "luo", "lus",
                /*"lv",  "mad", "mag", "mai", "mak", "man", "map", "mas",    */
                "lav", "mad", "mag", "mai", "mak", "man", "map", "mas",
                /*"mdf", "mdr", "men", "mg",  "mga", "mh",  "mi",  "mic", "min",    */
                "mdf", "mdr", "men", "mlg", "mga", "mah", "mri", "mic", "min",
                /*"mis", "mk",  "mkh", "ml",  "mn",  "mnc", "mni", "mno",    */
                "mis", "mkd", "mkh", "mal", "mon", "mnc", "mni", "mno",
                /*"mo",  "moh", "mos", "mr",  "ms",  "mt",  "mul", "mun",    */
                "mol", "moh", "mos", "mar", "msa", "mlt", "mul", "mun",
                /*"mus", "mwr", "my",  "myn", "myv", "na",  "nah", "nai", "nap",    */
                "mus", "mwr", "mya", "myn", "myv", "nau", "nah", "nai", "nap",
                /*"nb",  "nd",  "nds", "ne",  "new", "ng",  "nia", "nic",    */
                "nob", "nde", "nds", "nep", "new", "ndo", "nia", "nic",
                /*"niu", "nl",  "nn",  "no",  "nog", "non", "nr",  "nso", "nub",    */
                "niu", "nld", "nno", "nor", "nog", "non", "nbl", "nso", "nub",
                /*"nv",  "nwc", "ny",  "nym", "nyn", "nyo", "nzi", "oc",  "oj",     */
                "nav", "nwc", "nya", "nym", "nyn", "nyo", "nzi", "oci", "oji",
                /*"om",  "or",  "os",  "osa", "ota", "oto", "pa",  "paa",    */
                "orm", "ori", "oss", "osa", "ota", "oto", "pan", "paa",
                /*"pag", "pal", "pam", "pap", "pau", "peo", "phi", "phn",    */
                "pag", "pal", "pam", "pap", "pau", "peo", "phi", "phn",
                /*"pi",  "pl",  "pon", "pra", "pro", "ps",  "pt",  "qu",     */
                "pli", "pol", "pon", "pra", "pro", "pus", "por", "que",
                /*"raj", "rap", "rar", "rm",  "rn",  "ro",  "roa", "rom",    */
                "raj", "rap", "rar", "roh", "run", "ron", "roa", "rom",
                /*"ru",  "rup", "rw",  "sa",  "sad", "sah", "sai", "sal", "sam",    */
                "rus", "rup", "kin", "san", "sad", "sah", "sai", "sal", "sam",
                /*"sas", "sat", "sc",  "sco", "sd",  "se",  "sel", "sem",    */
                "sas", "sat", "srd", "sco", "snd", "sme", "sel", "sem",
                /*"sg",  "sga", "sgn", "shn", "si",  "sid", "sio", "sit",    */
                "sag", "sga", "sgn", "shn", "sin", "sid", "sio", "sit",
                /*"sk",  "sl",  "sla", "sm",  "sma", "smi", "smj", "smn",    */
                "slk", "slv", "sla", "smo", "sma", "smi", "smj", "smn",
                /*"sms", "sn",  "snk", "so",  "sog", "son", "sq",  "sr",     */
                "sms", "sna", "snk", "som", "sog", "son", "sqi", "srp",
                /*"srr", "ss",  "ssa", "st",  "su",  "suk", "sus", "sux",    */
                "srr", "ssw", "ssa", "sot", "sun", "suk", "sus", "sux",
                /*"sv",  "sw",  "syr", "ta",  "tai", "te",  "tem", "ter",    */
                "swe", "swa", "syr", "tam", "tai", "tel", "tem", "ter",
                /*"tet", "tg",  "th",  "ti",  "tig", "tiv", "tk",  "tkl",    */
                "tet", "tgk", "tha", "tir", "tig", "tiv", "tuk", "tkl",
                /*"tl",  "tlh", "tli", "tmh", "tn",  "to",  "tog", "tpi", "tr",     */
                "tgl", "tlh", "tli", "tmh", "tsn", "ton", "tog", "tpi", "tur",
                /*"ts",  "tsi", "tt",  "tum", "tup", "tut", "tvl", "tw",     */
                "tso", "tsi", "tat", "tum", "tup", "tut", "tvl", "twi",
                /*"ty",  "tyv", "udm", "ug",  "uga", "uk",  "umb", "und", "ur",     */
                "tah", "tyv", "udm", "uig", "uga", "ukr", "umb", "und", "urd",
                /*"uz",  "vai", "ve",  "vi",  "vo",  "vot", "wa",  "wak",    */
                "uzb", "vai", "ven", "vie", "vol", "vot", "wln", "wak",
                /*"wal", "war", "was", "wen", "wo",  "xal", "xh",  "yao", "yap",    */
                "wal", "war", "was", "wen", "wol", "xal", "xho", "yao", "yap",
                /*"yi",  "yo",  "ypk", "za",  "zap", "zen", "zh",  "znd",    */
                "yid", "yor", "ypk", "zha", "zap", "zen", "zho", "znd",
                /*"zu",  "zun",                                              */
                "zul", "zun",  
            };
    
            String[] tempObsoleteLanguages3 = {
                /* "in",  "iw",  "ji",  "jw",  "sh", */
                "ind", "heb", "yid", "jaw", "srp", 
            };

            synchronized (ULocale.class) {
                if (_languages == null) {
                    _languages = tempLanguages;
                    _replacementLanguages = tempReplacementLanguages;
                    _obsoleteLanguages = tempObsoleteLanguages;
                    _languages3 = tempLanguages3;
                    _obsoleteLanguages3 = tempObsoleteLanguages3;
                }
            }
        }
    }

    private static String[] _countries;
    private static String[] _deprecatedCountries;
    private static String[] _replacementCountries;
    private static String[] _obsoleteCountries;
    private static String[] _countries3;
    private static String[] _obsoleteCountries3;  

    // Avoid initializing country tables unless we have to.
    private static void initCountryTables() {    
        if (_countries == null) {
            /* ZR(ZAR) is now CD(COD) and FX(FXX) is PS(PSE) as per
               http://www.evertype.com/standards/iso3166/iso3166-1-en.html 
               added new codes keeping the old ones for compatibility
               updated to include 1999/12/03 revisions *CWB*/
    
            /* RO(ROM) is now RO(ROU) according to 
               http://www.iso.org/iso/en/prods-services/iso3166ma/03updates-on-iso-3166/nlv3e-rou.html
            */
    
            /* This list MUST be in sorted order, and MUST contain only two-letter codes! */
            String[] tempCountries = {
                "AD",  "AE",  "AF",  "AG",  "AI",  "AL",  "AM",  "AN",
                "AO",  "AQ",  "AR",  "AS",  "AT",  "AU",  "AW",  "AX",  "AZ",
                "BA",  "BB",  "BD",  "BE",  "BF",  "BG",  "BH",  "BI",
                "BJ",  "BL",  "BM",  "BN",  "BO",  "BR",  "BS",  "BT",  "BV",
                "BW",  "BY",  "BZ",  "CA",  "CC",  "CD",  "CF",  "CG",
                "CH",  "CI",  "CK",  "CL",  "CM",  "CN",  "CO",  "CR",
                "CU",  "CV",  "CX",  "CY",  "CZ",  "DE",  "DJ",  "DK",
                "DM",  "DO",  "DZ",  "EC",  "EE",  "EG",  "EH",  "ER",
                "ES",  "ET",  "FI",  "FJ",  "FK",  "FM",  "FO",  "FR",
                "GA",  "GB",  "GD",  "GE",  "GF",  "GG",  "GH",  "GI",  "GL",
                "GM",  "GN",  "GP",  "GQ",  "GR",  "GS",  "GT",  "GU",
                "GW",  "GY",  "HK",  "HM",  "HN",  "HR",  "HT",  "HU",
                "ID",  "IE",  "IL",  "IM",  "IN",  "IO",  "IQ",  "IR",  "IS",
                "IT",  "JE",  "JM",  "JO",  "JP",  "KE",  "KG",  "KH",  "KI",
                "KM",  "KN",  "KP",  "KR",  "KW",  "KY",  "KZ",  "LA",
                "LB",  "LC",  "LI",  "LK",  "LR",  "LS",  "LT",  "LU",
                "LV",  "LY",  "MA",  "MC",  "MD",  "ME",  "MF",  "MG",  "MH",  "MK",
                "ML",  "MM",  "MN",  "MO",  "MP",  "MQ",  "MR",  "MS",
                "MT",  "MU",  "MV",  "MW",  "MX",  "MY",  "MZ",  "NA",
                "NC",  "NE",  "NF",  "NG",  "NI",  "NL",  "NO",  "NP",
                "NR",  "NU",  "NZ",  "OM",  "PA",  "PE",  "PF",  "PG",
                "PH",  "PK",  "PL",  "PM",  "PN",  "PR",  "PS",  "PT",
                "PW",  "PY",  "QA",  "RE",  "RO",  "RS",  "RU",  "RW",  "SA",
                "SB",  "SC",  "SD",  "SE",  "SG",  "SH",  "SI",  "SJ",
                "SK",  "SL",  "SM",  "SN",  "SO",  "SR",  "ST",  "SV",
                "SY",  "SZ",  "TC",  "TD",  "TF",  "TG",  "TH",  "TJ",
                "TK",  "TL",  "TM",  "TN",  "TO",  "TR",  "TT",  "TV",
                "TW",  "TZ",  "UA",  "UG",  "UM",  "US",  "UY",  "UZ",
                "VA",  "VC",  "VE",  "VG",  "VI",  "VN",  "VU",  "WF",
                "WS",  "YE",  "YT",  "ZA",  "ZM",  "ZW",
            };

            /* this table is used for 3 letter codes */
            String[] tempObsoleteCountries = {
                "FX",  "CS",  "RO",  "TP",  "YU",  "ZR",  /* obsolete country codes */      
            };
            
            String[] tempDeprecatedCountries = {
               "BU", "CS", "DY", "FX", "HV", "NH", "RH", "TP", "YU", "ZR" /* deprecated country list */
            };
            String[] tempReplacementCountries = {
           /*  "BU", "CS", "DY", "FX", "HV", "NH", "RH", "TP", "YU", "ZR" */
               "MM", "RS", "BJ", "FR", "BF", "VU", "ZW", "TL", "RS", "CD",   /* replacement country codes */      
            };
    
            /* This list MUST contain a three-letter code for every two-letter code in
               the above list, and they MUST be listed in the same order! */
            String[] tempCountries3 = {
                /*  "AD",  "AE",  "AF",  "AG",  "AI",  "AL",  "AM",  "AN",     */
                    "AND", "ARE", "AFG", "ATG", "AIA", "ALB", "ARM", "ANT",
                /*  "AO",  "AQ",  "AR",  "AS",  "AT",  "AU",  "AW",  "AX",  "AZ",     */
                    "AGO", "ATA", "ARG", "ASM", "AUT", "AUS", "ABW", "ALA", "AZE",
                /*  "BA",  "BB",  "BD",  "BE",  "BF",  "BG",  "BH",  "BI",     */
                    "BIH", "BRB", "BGD", "BEL", "BFA", "BGR", "BHR", "BDI",
                /*  "BJ",  "BL",  "BM",  "BN",  "BO",  "BR",  "BS",  "BT",  "BV",     */
                    "BEN", "BLM", "BMU", "BRN", "BOL", "BRA", "BHS", "BTN", "BVT",
                /*  "BW",  "BY",  "BZ",  "CA",  "CC",  "CD",  "CF",  "CG",     */
                    "BWA", "BLR", "BLZ", "CAN", "CCK", "COD", "CAF", "COG",
                /*  "CH",  "CI",  "CK",  "CL",  "CM",  "CN",  "CO",  "CR",     */
                    "CHE", "CIV", "COK", "CHL", "CMR", "CHN", "COL", "CRI",
                /*  "CU",  "CV",  "CX",  "CY",  "CZ",  "DE",  "DJ",  "DK",     */
                    "CUB", "CPV", "CXR", "CYP", "CZE", "DEU", "DJI", "DNK",
                /*  "DM",  "DO",  "DZ",  "EC",  "EE",  "EG",  "EH",  "ER",     */
                    "DMA", "DOM", "DZA", "ECU", "EST", "EGY", "ESH", "ERI",
                /*  "ES",  "ET",  "FI",  "FJ",  "FK",  "FM",  "FO",  "FR",     */
                    "ESP", "ETH", "FIN", "FJI", "FLK", "FSM", "FRO", "FRA",
                /*  "GA",  "GB",  "GD",  "GE",  "GF",  "GG",  "GH",  "GI",  "GL",     */
                    "GAB", "GBR", "GRD", "GEO", "GUF", "GGY", "GHA", "GIB", "GRL",
                /*  "GM",  "GN",  "GP",  "GQ",  "GR",  "GS",  "GT",  "GU",     */
                    "GMB", "GIN", "GLP", "GNQ", "GRC", "SGS", "GTM", "GUM",
                /*  "GW",  "GY",  "HK",  "HM",  "HN",  "HR",  "HT",  "HU",     */
                    "GNB", "GUY", "HKG", "HMD", "HND", "HRV", "HTI", "HUN",
                /*  "ID",  "IE",  "IL",  "IM",  "IN",  "IO",  "IQ",  "IR",  "IS" */
                    "IDN", "IRL", "ISR", "IMN", "IND", "IOT", "IRQ", "IRN", "ISL",
                /*  "IT",  "JE",  "JM",  "JO",  "JP",  "KE",  "KG",  "KH",  "KI",     */
                    "ITA", "JEY", "JAM", "JOR", "JPN", "KEN", "KGZ", "KHM", "KIR",
                /*  "KM",  "KN",  "KP",  "KR",  "KW",  "KY",  "KZ",  "LA",     */
                    "COM", "KNA", "PRK", "KOR", "KWT", "CYM", "KAZ", "LAO",
                /*  "LB",  "LC",  "LI",  "LK",  "LR",  "LS",  "LT",  "LU",     */
                    "LBN", "LCA", "LIE", "LKA", "LBR", "LSO", "LTU", "LUX",
                /*  "LV",  "LY",  "MA",  "MC",  "MD",  "ME",  "MF",  "MG",  "MH",  "MK",     */
                    "LVA", "LBY", "MAR", "MCO", "MDA", "MNE", "MAF", "MDG", "MHL", "MKD",
                /*  "ML",  "MM",  "MN",  "MO",  "MP",  "MQ",  "MR",  "MS",     */
                    "MLI", "MMR", "MNG", "MAC", "MNP", "MTQ", "MRT", "MSR",
                /*  "MT",  "MU",  "MV",  "MW",  "MX",  "MY",  "MZ",  "NA",     */
                    "MLT", "MUS", "MDV", "MWI", "MEX", "MYS", "MOZ", "NAM",
                /*  "NC",  "NE",  "NF",  "NG",  "NI",  "NL",  "NO",  "NP",     */
                    "NCL", "NER", "NFK", "NGA", "NIC", "NLD", "NOR", "NPL",
                /*  "NR",  "NU",  "NZ",  "OM",  "PA",  "PE",  "PF",  "PG",     */
                    "NRU", "NIU", "NZL", "OMN", "PAN", "PER", "PYF", "PNG",
                /*  "PH",  "PK",  "PL",  "PM",  "PN",  "PR",  "PS",  "PT",     */
                    "PHL", "PAK", "POL", "SPM", "PCN", "PRI", "PSE", "PRT",
                /*  "PW",  "PY",  "QA",  "RE",  "RO",  "RS",  "RU",  "RW",  "SA",     */
                    "PLW", "PRY", "QAT", "REU", "ROU", "SRB", "RUS", "RWA", "SAU",
                /*  "SB",  "SC",  "SD",  "SE",  "SG",  "SH",  "SI",  "SJ",     */
                    "SLB", "SYC", "SDN", "SWE", "SGP", "SHN", "SVN", "SJM",
                /*  "SK",  "SL",  "SM",  "SN",  "SO",  "SR",  "ST",  "SV",     */
                    "SVK", "SLE", "SMR", "SEN", "SOM", "SUR", "STP", "SLV",
                /*  "SY",  "SZ",  "TC",  "TD",  "TF",  "TG",  "TH",  "TJ",     */
                    "SYR", "SWZ", "TCA", "TCD", "ATF", "TGO", "THA", "TJK",
                /*  "TK",  "TL",  "TM",  "TN",  "TO",  "TR",  "TT",  "TV",     */
                    "TKL", "TLS", "TKM", "TUN", "TON", "TUR", "TTO", "TUV",
                /*  "TW",  "TZ",  "UA",  "UG",  "UM",  "US",  "UY",  "UZ",     */
                    "TWN", "TZA", "UKR", "UGA", "UMI", "USA", "URY", "UZB",
                /*  "VA",  "VC",  "VE",  "VG",  "VI",  "VN",  "VU",  "WF",     */
                    "VAT", "VCT", "VEN", "VGB", "VIR", "VNM", "VUT", "WLF",
                /*  "WS",  "YE",  "YT",  "ZA",  "ZM",  "ZW"          */
                    "WSM", "YEM", "MYT", "ZAF", "ZMB", "ZWE",
            };
    
            String[] tempObsoleteCountries3 = {
                /*"FX",  "CS",  "RO",  "TP",  "YU",  "ZR",   */
                "FXX", "SCG", "ROM", "TMP", "YUG", "ZAR",    
            };

            synchronized (ULocale.class) {
                if (_countries == null) {
                    _countries = tempCountries;
                    _deprecatedCountries = tempDeprecatedCountries;
                    _replacementCountries = tempReplacementCountries;
                    _obsoleteCountries = tempObsoleteCountries;
                    _countries3 = tempCountries3;
                    _obsoleteCountries3 = tempObsoleteCountries3;
                }
            }
        }
    }

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
    //  { "th_TH_TH",   "th_TH",    ??,         ??,         "th"} //TODO
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
        ULocale result = (ULocale)CACHE.get(loc);
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
                            IDParser p = new IDParser(_javaLocaleMap[i][1]);
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
     * start of the keyword list is indicated by '@', and consists of one
     * or more keyword/value pairs separated by commas.
     * <p>
     * This constructor does not canonicalize the localeID.
     * 
     * @param localeID string representation of the locale, e.g:
     * "en_US", "sy_Cyrl_YU", "zh__pinyin", "es_ES@currency=EUR,collation=traditional"
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
            IDParser p = new IDParser(localeID);
            String base = p.getBaseName();
            for (int i = 0; i < _javaLocaleMap.length; i++) {
                if (base.equals(_javaLocaleMap[i][1]) || base.equals(_javaLocaleMap[i][4])) {
                    if (_javaLocaleMap[i][2] != null) {
                        String val = p.getKeywordValue(_javaLocaleMap[i][2]);
                        if (val != null && val.equals(_javaLocaleMap[i][3])) {
                            p = new IDParser(_javaLocaleMap[i][0]);
                            break;
                        }
                    } else {
                        p = new IDParser(_javaLocaleMap[i][0]);
                        break;
                    }
                }
            }
            String[] names = p.getLanguageScriptCountryVariant();
            locale = new Locale(names[0], names[2], names[3]);
        }
        return locale;
    }

    private static SoftReference nameCacheRef = new SoftReference(Collections.synchronizedMap(new HashMap()));
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
            if (defaultLocale != currentDefault) {
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
        initCountryTables();
        return (String[])_countries.clone();
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
        initLanguageTables();
        return (String[])_languages.clone();
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
        return new IDParser(localeID).getLanguage();
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
        return new IDParser(localeID).getScript();
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
        return new IDParser(localeID).getCountry();
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
        return new IDParser(localeID).getVariant();
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
        return new IDParser(localeID).getBaseName();
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
        Map cache = (Map)nameCacheRef.get();
        if (cache == null) {
            cache = Collections.synchronizedMap(new HashMap());
            nameCacheRef = new SoftReference(cache);
        }
        String name = (String)cache.get(localeID);
        if (name == null) {
            name = new IDParser(localeID).getName();
            cache.put(localeID, name);
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
    public Iterator getKeywords() {
        return getKeywords(localeID);
    }

    /**
     * Returns an iterator over keywords for the specified locale.  If there 
     * are no keywords, returns null.
     * @return an iterator over the keywords in the specified locale, or null
     * if there are no keywords.
     * @stable ICU 3.0
     */
    public static Iterator getKeywords(String localeID){
        return new IDParser(localeID).getKeywords();
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
        return new IDParser(localeID).getKeywordValue(keywordName);
    }

    /**
     * Utility class to parse and normalize locale ids (including POSIX style)
     */
    private static final class IDParser {
        private char[] id;
        private int index;
        private char[] buffer;
        private int blen;
        // um, don't handle POSIX ids unless we request it.  why not?  well... because.
        private boolean canonicalize;
        private boolean hadCountry;

        // used when canonicalizing
        Map keywords;
        String baseName;

        /**
         * Parsing constants.
         */
        private static final char KEYWORD_SEPARATOR     = '@';
        private static final char HYPHEN                = '-';
        private static final char KEYWORD_ASSIGN        = '=';
        private static final char COMMA                 = ',';
        private static final char ITEM_SEPARATOR        = ';';
        private static final char DOT                   = '.';

        private IDParser(String localeID) {
            this(localeID, false);
        }

        private IDParser(String localeID, boolean canonicalize) {
            id = localeID.toCharArray();
            index = 0;
            buffer = new char[id.length + 5];
            blen = 0;
            this.canonicalize = canonicalize;
        }

        private void reset() {
            index = blen = 0;
        }

        // utilities for working on text in the buffer

        /**
         * Append c to the buffer.
         */
        private void append(char c) {
            try {
                buffer[blen] = c;
            }
            catch (IndexOutOfBoundsException e) {
                if (buffer.length > 512) {
                    // something is seriously wrong, let this go
                    throw e;
                }
                char[] nbuffer = new char[buffer.length * 2];
                System.arraycopy(buffer, 0, nbuffer, 0, buffer.length);
                nbuffer[blen] = c;
                buffer = nbuffer;
            }
            ++blen;
        }

        private void addSeparator() {
            append(UNDERSCORE);
        }

        /**
         * Returns the text in the buffer from start to blen as a String.
         */
        private String getString(int start) {
            if (start == blen) {
                return EMPTY_STRING;
            }
            return new String(buffer, start, blen-start);
        }

        /**
         * Set the length of the buffer to pos, then append the string.
         */
        private void set(int pos, String s) {
            this.blen = pos; // no safety
            append(s);
        }

        /**
         * Append the string to the buffer.
         */
        private void append(String s) {
            for (int i = 0; i < s.length(); ++i) {
                append(s.charAt(i));
            }
        }

        // utilities for parsing text out of the id

        /**
         * Character to indicate no more text is available in the id.
         */
        private static final char DONE = '\uffff';

        /**
         * Returns the character at index in the id, and advance index.  The returned character
         * is DONE if index was at the limit of the buffer.  The index is advanced regardless
         * so that decrementing the index will always 'unget' the last character returned.
         */
        private char next() {
            if (index == id.length) {
                index++;
                return DONE; 
            }

            return id[index++];
        }

        /**
         * Advance index until the next terminator or id separator, and leave it there.
         */
        private void skipUntilTerminatorOrIDSeparator() {
            while (!isTerminatorOrIDSeparator(next())) {
            }
            --index;
        }

        /**
         * Returns true if the character at index in the id is a terminator.
         */
        private boolean atTerminator() {
            return index >= id.length || isTerminator(id[index]);
        }

        /*
         * Returns true if the character is an id separator (underscore or hyphen).
         */
        private boolean isIDSeparator(char c) {
            return c == UNDERSCORE || c == HYPHEN;
        }

        /**
         * Returns true if the character is a terminator (keyword separator, dot, or DONE).
         * Dot is a terminator because of the POSIX form, where dot precedes the codepage.
         */
        private boolean isTerminator(char c) {
            // always terminate at DOT, even if not handling POSIX.  It's an error...
            return c == KEYWORD_SEPARATOR || c == DONE || c == DOT;
        }

        /**
         * Returns true if the character is a terminator or id separator.
         */
        private boolean isTerminatorOrIDSeparator(char c) {
            return c == KEYWORD_SEPARATOR || c == UNDERSCORE || c == HYPHEN || 
                c == DONE || c == DOT;   
        }

        /**
         * Returns true if the start of the buffer has an experimental or private language 
         * prefix, the pattern '[ixIX][-_].' shows the syntax checked.
         */
        private boolean haveExperimentalLanguagePrefix() {
            if (id.length > 2) {
                char c = id[1];
                if (c == HYPHEN || c == UNDERSCORE) {
                    c = id[0];
                    return c == 'x' || c == 'X' || c == 'i' || c == 'I';
                }
            }
            return false;
        }

        /**
         * Returns true if a value separator occurs at or after index.
         */
        private boolean haveKeywordAssign() {
            // assume it is safe to start from index
            for (int i = index; i < id.length; ++i) {
                if (id[i] == KEYWORD_ASSIGN) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Advance index past language, and accumulate normalized language code in buffer.
         * Index must be at 0 when this is called.  Index is left at a terminator or id 
         * separator.  Returns the start of the language code in the buffer.
         */
        private int parseLanguage() {
            if (haveExperimentalLanguagePrefix()) {
                append(Character.toLowerCase(id[0]));
                append(HYPHEN);
                index = 2;
            }
        
            char c;
            while(!isTerminatorOrIDSeparator(c = next())) {
                append(Character.toLowerCase(c));
            }
            --index; // unget

            if (blen == 3) {
                initLanguageTables();

                /* convert 3 character code to 2 character code if possible *CWB*/
                String lang = getString(0);
                int offset = findIndex(_languages3, lang);
                if (offset >= 0) {
                    set(0, _languages[offset]);
                } else {
                    offset = findIndex(_obsoleteLanguages3, lang);
                    if (offset >= 0) {
                        set(0, _obsoleteLanguages[offset]);
                    }
                }
            }

            return 0;
        }

        /**
         * Advance index past language.  Index must be at 0 when this is called.  Index
         * is left at a terminator or id separator.
         */
        private void skipLanguage() {
            if (haveExperimentalLanguagePrefix()) {
                index = 2;
            }
            skipUntilTerminatorOrIDSeparator();
        }

        /**
         * Advance index past script, and accumulate normalized script in buffer.
         * Index must be immediately after the language.
         * If the item at this position is not a script (is not four characters
         * long) leave index and buffer unchanged.  Otherwise index is left at
         * a terminator or id separator.  Returns the start of the script code
         * in the buffer (this may be equal to the buffer length, if there is no
         * script).
         */
        private int parseScript() {
            if (!atTerminator()) {
                int oldIndex = index; // save original index
                ++index;

                int oldBlen = blen; // get before append hyphen, if we truncate everything is undone
                char c;
                while(!isTerminatorOrIDSeparator(c = next())) {
                    if (blen == oldBlen) { // first pass
                        addSeparator();
                        append(Character.toUpperCase(c));
                    } else {
                        append(Character.toLowerCase(c));
                    }
                }
                --index; // unget

                /* If it's not exactly 4 characters long, then it's not a script. */
                if (index - oldIndex != 5) { // +1 to account for separator
                    index = oldIndex;
                    blen = oldBlen;
                } else {
                    oldBlen++; // index past hyphen, for clients who want to extract just the script
                }

                return oldBlen;
            }
            return blen;
        }

        /**
         * Advance index past script.
         * Index must be immediately after the language and IDSeparator.
         * If the item at this position is not a script (is not four characters
         * long) leave index.  Otherwise index is left at a terminator or
         * id separator.
         */
        private void skipScript() {
            if (!atTerminator()) {
                int oldIndex = index;
                ++index;

                skipUntilTerminatorOrIDSeparator();
                if (index - oldIndex != 5) { // +1 to account for separator
                    index = oldIndex;
                }
            }
        }

        /**
         * Advance index past country, and accumulate normalized country in buffer.
         * Index must be immediately after the script (if there is one, else language)
         * and IDSeparator.  Return the start of the country code in the buffer.
         */
        private int parseCountry() {
            if (!atTerminator()) {
                int oldIndex = index;
                ++index;

                int oldBlen = blen;
                char c;
                while (!isTerminatorOrIDSeparator(c = next())) {
                    if (oldBlen == blen) { // first, add hyphen
                        hadCountry = true; // we have a country, let variant parsing know
                        addSeparator();
                        ++oldBlen; // increment past hyphen
                    }
                    append(Character.toUpperCase(c));
                }
                --index; // unget

                int charsAppended = blen - oldBlen;

                if (charsAppended == 0) {
                    // Do nothing.
                }
                else if (charsAppended < 2 || charsAppended > 3) {
                    // It's not a country, so return index and blen to
                    // their previous values.
                    index = oldIndex;
                    --oldBlen;
                    blen = oldBlen;
                    hadCountry = false;
                }
                else if (charsAppended == 3) {
                    initCountryTables();

                    /* convert 3 character code to 2 character code if possible *CWB*/
                    int offset = findIndex(_countries3, getString(oldBlen));
                    if (offset >= 0) {
                        set(oldBlen, _countries[offset]);
                    } else {
                        offset = findIndex(_obsoleteCountries3, getString(oldBlen));
                        if (offset >= 0) {
                            set(oldBlen, _obsoleteCountries[offset]);
                        }
                    }
                }

                return oldBlen;
            }

            return blen;
        }  

        /**
         * Advance index past country.
         * Index must be immediately after the script (if there is one, else language)
         * and IDSeparator.
         */
        private void skipCountry() {
            if (!atTerminator()) {
                ++index;
                int oldIndex = index;

                skipUntilTerminatorOrIDSeparator();
                int charsSkipped = index - oldIndex;
                if (charsSkipped < 2 || charsSkipped > 3) { // +1 to account for separator
                    index = oldIndex;
                    /*
                    if (charsSkipped > 1 && isIDSeparator(buffer[index])) {
                        // Check for the situation where there are two
                        // underscores, which is our format for separating
                        // the variant when there is no country.
                        ++index;
                    }
                    */
                }
            }
        }

        /**
         * Advance index past variant, and accumulate normalized variant in buffer.  This ignores
         * the codepage information from POSIX ids.  Index must be immediately after the country
         * or script.  Index is left at the keyword separator or at the end of the text.  Return
         * the start of the variant code in the buffer.
         *
         * In standard form, we can have the following forms:
         * ll__VVVV
         * ll_CC_VVVV
         * ll_Ssss_VVVV
         * ll_Ssss_CC_VVVV
         *
         * This also handles POSIX ids, which can have the following forms (pppp is code page id):
         * ll_CC.pppp          --> ll_CC
         * ll_CC.pppp@VVVV     --> ll_CC_VVVV
         * ll_CC@VVVV          --> ll_CC_VVVV
         *
         * We identify this use of '@' in POSIX ids by looking for an '=' following
         * the '@'.  If there is one, we consider '@' to start a keyword list, instead of
         * being part of a POSIX id.
         *
         * Note:  since it was decided that we want an option to not handle POSIX ids, this
         * becomes a bit more complex.
         */
        private int parseVariant() {
            int oldBlen = blen;

            boolean start = true;
            boolean needSeparator = true;
            boolean skipping = false;
            char c;
            while ((c = next()) != DONE) {
                if (c == DOT) {
                    start = false;
                    skipping = true;
                } else if (c == KEYWORD_SEPARATOR) {
                    if (haveKeywordAssign()) {
                        break;
                    }
                    skipping = false;
                    start = false;
                    needSeparator = true; // add another underscore if we have more text
                } else if (start) {
                    start = false;
                } else if (!skipping) {
                    if (needSeparator) {
                        boolean incOldBlen = blen == oldBlen; // need to skip separators
                        needSeparator = false;
                        if (incOldBlen && !hadCountry) { // no country, we'll need two
                            addSeparator();
                            ++oldBlen; // for sure
                        }
                        addSeparator();
                        if (incOldBlen) { // only for the first separator
                            ++oldBlen;
                        }
                    }
                    c = Character.toUpperCase(c);
                    if (c == HYPHEN || c == COMMA) {
                        c = UNDERSCORE;
                    }
                    append(c);
                }
            }
            --index; // unget
            
            return oldBlen;
        }

        // no need for skipvariant, to get the keywords we'll just scan directly for 
        // the keyword separator

        /**
         * Returns the normalized language id, or the empty string.
         */
        public String getLanguage() {
            reset();
            return getString(parseLanguage());
        }
   
        /**
         * Returns the normalized script id, or the empty string.
         */
        public String getScript() {
            reset();
            skipLanguage();
            return getString(parseScript());
        }
    
        /**
         * return the normalized country id, or the empty string.
         */
        public String getCountry() {
            reset();
            skipLanguage();
            skipScript();
            return getString(parseCountry());
        }

        /**
         * Returns the normalized variant id, or the empty string.
         */
        public String getVariant() {
            reset();
            skipLanguage();
            skipScript();
            skipCountry();
            return getString(parseVariant());
        }

        /**
         * Returns the language, script, country, and variant as separate strings.
         */
        public String[] getLanguageScriptCountryVariant() {
            reset();
            return new String[] {
                getString(parseLanguage()),
                getString(parseScript()),
                getString(parseCountry()),
                getString(parseVariant())
            };
        }

        public void setBaseName(String baseName) {
            this.baseName = baseName;
        }

        public void parseBaseName() {
            if (baseName != null) {
                set(0, baseName);
            } else {
                reset();
                parseLanguage();
                parseScript();
                parseCountry();
                parseVariant();
            
                // catch unwanted trailing underscore after country if there was no variant
                if (blen > 1 && buffer[blen-1] == UNDERSCORE) {
                    --blen;
                }
            }
        }

        /**
         * Returns the normalized base form of the locale id.  The base
         * form does not include keywords.
         */
        public String getBaseName() {
            if (baseName != null) {
                return baseName;
            }
            parseBaseName();
            return getString(0);
        }

        /**
         * Returns the normalized full form of the locale id.  The full
         * form includes keywords if they are present.
         */
        public String getName() {
            parseBaseName();
            parseKeywords();
            return getString(0);
        }

        // keyword utilities

        /**
         * If we have keywords, advance index to the start of the keywords and return true, 
         * otherwise return false.
         */
        private boolean setToKeywordStart() {
            for (int i = index; i < id.length; ++i) {
                if (id[i] == KEYWORD_SEPARATOR) {
                    if (canonicalize) {
                        for (int j = ++i; j < id.length; ++j) { // increment i past separator for return
                            if (id[j] == KEYWORD_ASSIGN) {
                                index = i;
                                return true;
                            }
                        }
                    } else {
                        if (++i < id.length) {
                            index = i;
                            return true;
                        }
                    }
                    break;
                }
            }
            return false;
        }
        
        private static boolean isDoneOrKeywordAssign(char c) {
            return c == DONE || c == KEYWORD_ASSIGN;
        }

        private static boolean isDoneOrItemSeparator(char c) {
            return c == DONE || c == ITEM_SEPARATOR;
        }

        private String getKeyword() {
            int start = index;
            while (!isDoneOrKeywordAssign(next())) {
            }
            --index;
            return new String(id, start, index-start).trim().toLowerCase();
        }

        private String getValue() {
            int start = index;
            while (!isDoneOrItemSeparator(next())) {
            }
            --index;
            return new String(id, start, index-start).trim(); // leave case alone
        }

        private Comparator getKeyComparator() {
            final Comparator comp = new Comparator() {
                    public int compare(Object lhs, Object rhs) {
                        return ((String)lhs).compareTo((String)rhs);
                    }
                };
            return comp;
        }

        /**
         * Returns a map of the keywords and values, or null if there are none.
         */
        private Map getKeywordMap() {
            if (keywords == null) {
                TreeMap m = null;
                if (setToKeywordStart()) {
                    // trim spaces and convert to lower case, both keywords and values.
                    do {
                        String key = getKeyword();
                        if (key.length() == 0) {
                            break;
                        }
                        char c = next();
                        if (c != KEYWORD_ASSIGN) {
                            // throw new IllegalArgumentException("key '" + key + "' missing a value.");
                            if (c == DONE) {
                                break;
                            } else {
                                continue;
                            }
                        }
                        String value = getValue();
                        if (value.length() == 0) {
                            // throw new IllegalArgumentException("key '" + key + "' missing a value.");
                            continue;
                        }
                        if (m == null) {
                            m = new TreeMap(getKeyComparator());
                        } else if (m.containsKey(key)) {
                            // throw new IllegalArgumentException("key '" + key + "' already has a value.");
                            continue;
                        }
                        m.put(key, value);
                    } while (next() == ITEM_SEPARATOR);
                }               
                keywords = m != null ? m : Collections.EMPTY_MAP;
            }

            return keywords;
        }

        /**
         * Parse the keywords and return start of the string in the buffer.
         */
        private int parseKeywords() {
            int oldBlen = blen;
            Map m = getKeywordMap();
            if (!m.isEmpty()) {
                Iterator iter = m.entrySet().iterator();
                boolean first = true;
                while (iter.hasNext()) {
                    append(first ? KEYWORD_SEPARATOR : ITEM_SEPARATOR);
                    first = false;
                    Map.Entry e = (Map.Entry)iter.next();
                    append((String)e.getKey());
                    append(KEYWORD_ASSIGN);
                    append((String)e.getValue());
                }
                if (blen != oldBlen) {
                    ++oldBlen;
                }
            }
            return oldBlen;
        }

        /**
         * Returns an iterator over the keywords, or null if we have an empty map.
         */
        public Iterator getKeywords() {
            Map m = getKeywordMap();
            return m.isEmpty() ? null : m.keySet().iterator();
        }

        /**
         * Returns the value for the named keyword, or null if the keyword is not
         * present.
         */
        public String getKeywordValue(String keywordName) {
            Map m = getKeywordMap();
            return m.isEmpty() ? null : (String)m.get(keywordName.trim().toLowerCase());
        }

        /**
         * Set the keyword value only if it is not already set to something else.
         */
        public void defaultKeywordValue(String keywordName, String value) {
            setKeywordValue(keywordName, value, false);
        }
            
        /**
         * Set the value for the named keyword, or unset it if value is null.  If
         * keywordName itself is null, unset all keywords.  If keywordName is not null,
         * value must not be null.
         */
        public void setKeywordValue(String keywordName, String value) {
            setKeywordValue(keywordName, value, true);
        }

        /**
         * Set the value for the named keyword, or unset it if value is null.  If
         * keywordName itself is null, unset all keywords.  If keywordName is not null,
         * value must not be null.  If reset is true, ignore any previous value for 
         * the keyword, otherwise do not change the keyword (including removal of
         * one or all keywords).
         */
        private void setKeywordValue(String keywordName, String value, boolean reset) {
            if (keywordName == null) {
                if (reset) {
                    // force new map, ignore value
                    keywords = Collections.EMPTY_MAP;
                }
            } else {
                keywordName = keywordName.trim().toLowerCase();
                if (keywordName.length() == 0) {
                    throw new IllegalArgumentException("keyword must not be empty");
                }
                if (value != null) {
                    value = value.trim();
                    if (value.length() == 0) {
                        throw new IllegalArgumentException("value must not be empty");
                    }
                }
                Map m = getKeywordMap();
                if (m.isEmpty()) { // it is EMPTY_MAP
                    if (value != null) {
                        // force new map
                        keywords = new TreeMap(getKeyComparator());
                        keywords.put(keywordName, value.trim());
                    }
                } else {
                    if (reset || !m.containsKey(keywordName)) {
                        if (value != null) {
                            m.put(keywordName, value);
                        } else {
                            m.remove(keywordName);
                            if (m.isEmpty()) {
                                // force new map
                                keywords = Collections.EMPTY_MAP;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * linear search of the string array. the arrays are unfortunately ordered by the
     * two-letter target code, not the three-letter search code, which seems backwards.
     */
    private static int findIndex(String[] array, String target){
        for (int i = 0; i < array.length; i++) {
            if (target.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }    

    /**
     * Returns the canonical name for the specified locale ID.  This is used to convert POSIX
     * and other grandfathered IDs to standard ICU form.
     * @param localeID the locale id
     * @return the canonicalized id
     * @stable ICU 3.0
     */
    public static String canonicalize(String localeID){
        IDParser parser = new IDParser(localeID, true);
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
        IDParser parser = new IDParser(localeID);
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
    public String getISO3Language(){
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
    public static String getISO3Language(String localeID){
        initLanguageTables();

        String language = getLanguage(localeID);
        int offset = findIndex(_languages, language);
        if(offset>=0){
            return _languages3[offset];
        } else {
            offset = findIndex(_obsoleteLanguages, language);
            if (offset >= 0) {
                return _obsoleteLanguages3[offset];
            }
        }
        return EMPTY_STRING;
    }
    
    /**
     * Returns a three-letter abbreviation for this locale's country/region.  If the locale
     * doesn't specify a country, returns the empty string.  Otherwise, returns
     * an uppercase ISO 3166 3-letter country code.
     * @exception MissingResourceException Throws MissingResourceException if the
     * three-letter country abbreviation is not available for this locale.
     * @stable ICU 3.0
     */
    public String getISO3Country(){
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
    public static String getISO3Country(String localeID){
        initCountryTables();

        String country = getCountry(localeID);
        int offset = findIndex(_countries, country);
        if(offset>=0){
            return _countries3[offset];
        }else{
            offset = findIndex(_obsoleteCountries, country);
            if(offset>=0){
                return _obsoleteCountries3[offset];   
            }
        }
        return EMPTY_STRING;
    }
    
    // display names

    /**
     * Utility to fetch locale display data from resource bundle tables.
     */
    private static String getTableString(String tableName, String subtableName, String item, String displayLocaleID) {
        if (item.length() > 0) {
            try {
                ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.
                  getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, displayLocaleID);
                return getTableString(tableName, subtableName, item, bundle);
            } catch (Exception e) {
//              System.out.println("gtsu: " + e.getMessage());
            }
        }
        return item;
    }
        
    /**
     * Utility to fetch locale display data from resource bundle tables.
     */
    private static String getTableString(String tableName, String subtableName, String item, ICUResourceBundle bundle) {
//      System.out.println("gts table: " + tableName + 
//                         " subtable: " + subtableName +
//                         " item: " + item +
//                         " bundle: " + bundle.getULocale());
        try {
            for (;;) {
                // special case currency
                if ("currency".equals(subtableName)) {
                    ICUResourceBundle table = bundle.getWithFallback("Currencies");
                    table = table.getWithFallback(item);
                    return table.getString(1);
                } else {
                    ICUResourceBundle table = bundle.getWithFallback(tableName);
                    try {
                        if (subtableName != null) {
                            table = table.getWithFallback(subtableName);
                        }
                        return table.getStringWithFallback(item);
                    }
                    catch (MissingResourceException e) {
                        
                        if(subtableName==null){
                            try{
                                // may be a deprecated code
                                String currentName = null;
                                if(tableName.equals("Countries")){
                                    currentName = getCurrentCountryID(item);
                                }else if(tableName.equals("Languages")){
                                    currentName = getCurrentLanguageID(item);
                                }
                                return table.getStringWithFallback(currentName);
                            }catch (MissingResourceException ex){/* fall through*/}
                        }
                        
                        // still can't figure out ?.. try the fallback mechanism
                        String fallbackLocale = table.getWithFallback("Fallback").getString();
                        if (fallbackLocale.length() == 0) {
                            fallbackLocale = "root";
                        }
//                      System.out.println("bundle: " + bundle.getULocale() + " fallback: " + fallbackLocale);
                        if(fallbackLocale.equals(table.getULocale().localeID)){
                            return item;
                        }
                        bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, 
                                                                                      fallbackLocale);
//                          System.out.println("fallback from " + table.getULocale() + " to " + fallbackLocale + 
//                                             ", got bundle " + bundle.getULocale());                      
                    }
                }
            }
        }
        catch (Exception e) {
//          System.out.println("gtsi: " + e.getMessage());
        }
        return item;
    }

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

    static String getCurrentCountryID(String oldID){
        initCountryTables();
        int offset = findIndex(_deprecatedCountries, oldID);
        if (offset >= 0) {
            return _replacementCountries[offset];
        }
        return oldID;
    }
    static String getCurrentLanguageID(String oldID){
        initLanguageTables();
        int offset = findIndex(_obsoleteLanguages, oldID);
        if (offset >= 0) {
            return _replacementLanguages[offset];
        }
        return oldID;        
    }


    // displayLocaleID is canonical, localeID need not be since parsing will fix this.
    private static String getDisplayLanguageInternal(String localeID, String displayLocaleID) {
        return getTableString("Languages", null, new IDParser(localeID).getLanguage(), displayLocaleID);
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
        return getTableString("Scripts", null, new IDParser(localeID).getScript(), displayLocaleID);
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
        return getTableString("Countries", null,  new IDParser(localeID).getCountry(), displayLocaleID);
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
        return getTableString("Variants", null, new IDParser(localeID).getVariant(), displayLocaleID);
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
        return getTableString("Keys", null, keyword.trim().toLowerCase(), displayLocaleID);
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
        keyword = keyword.trim().toLowerCase();
        String value = new IDParser(localeID).getKeywordValue(keyword);
        return getTableString("Types", keyword, value, displayLocaleID);
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
        // lang
        // lang (script, country, variant, keyword=value, ...)
        // script, country, variant, keyword=value, ...

        final String[] tableNames = { "Languages", "Scripts", "Countries", "Variants" };

        ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, displayLocaleID);

        StringBuffer buf = new StringBuffer();

        IDParser parser = new IDParser(localeID);
        String[] names = parser.getLanguageScriptCountryVariant();

        boolean haveLanguage = names[0].length() > 0;
        boolean openParen = false;
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            if (name.length() > 0) {
                name = getTableString(tableNames[i], null, name, bundle);
                if (buf.length() > 0) { // need a separator
                    if (haveLanguage & !openParen) {
                        buf.append(" (");
                        openParen = true;
                    } else {
                        buf.append(", ");
                    }
                }
                buf.append(name);
            }
        }

        Map m = parser.getKeywordMap();
        if (!m.isEmpty()) {
            Iterator keys = m.entrySet().iterator();
            while (keys.hasNext()) {
                if (buf.length() > 0) {
                    if (haveLanguage & !openParen) {
                        buf.append(" (");
                        openParen = true;
                    } else {
                        buf.append(", ");
                    }
                }
                Map.Entry e = (Map.Entry)keys.next();
                String key = (String)e.getKey();
                String val = (String)e.getValue();
                buf.append(getTableString("Keys", null, key, bundle));
                buf.append("=");
                buf.append(getTableString("Types", key, val, bundle));
            }
        }

        if (openParen) {
            buf.append(")");
        }
            
        return buf.toString();
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
    public static Type ACTUAL_LOCALE = new Type(0);

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
    public static Type VALID_LOCALE = new Type(1);

    /**
     * Opaque selector enum for <tt>getLocale()</tt>.
     * @see com.ibm.icu.util.ULocale
     * @see com.ibm.icu.util.ULocale#ACTUAL_LOCALE
     * @see com.ibm.icu.util.ULocale#VALID_LOCALE
     * @draft ICU 2.8 (retainAll)
     * @provisional This API might change or be removed in a future release.
     */
    public static final class Type {
        private int localeType;
        private Type(int type) { localeType = type; }
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
        class ULocaleAcceptLanguageQ implements Comparable {
            private double q;
            private double serial;
            public ULocaleAcceptLanguageQ(double theq, int theserial) {
                q = theq;
                serial = theserial;
            }
            public int compareTo(Object o) {
                ULocaleAcceptLanguageQ other = (ULocaleAcceptLanguageQ) o;
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
        TreeMap map = new TreeMap();
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
        ULocale acceptList[] = (ULocale[])map.values().toArray(new ULocale[map.size()]);
        return acceptList;
    }

    private static HashMap _likelySubtagMaximizeMap;
    private static HashMap _likelySubtagsMap;
    
    private static void initLikelySubtagMaximizeMap() {
        if (_likelySubtagMaximizeMap != null) {
            return;
        }
        // We should use CLDR data which will be introduced in CLDR1.5.1.
        // For now, use the hardcoded table below.
        String[][] likelySubtagTable = {
              {
                /* { Afar; ?; ? } => { Afar; Latin; Ethiopia } */
                "aa",
                "aa_Latn_ET"
              }, {
                /* { Afrikaans; ?; ? } => { Afrikaans; Latin; South Africa } */
                "af",
                "af_Latn_ZA"
              }, {
                /* { Akan; ?; ? } => { Akan; Latin; Ghana } */
                "ak",
                "ak_Latn_GH"
              }, {
                /* { Amharic; ?; ? } => { Amharic; Ethiopic; Ethiopia } */
                "am",
                "am_Ethi_ET"
              }, {
                /* { Arabic; ?; ? } => { Arabic; Arabic; Egypt } */
                "ar",
                "ar_Arab_EG"
              }, {
                /* { Assamese; ?; ? } => { Assamese; Bengali; India } */
                "as",
                "as_Beng_IN"
              }, {
                /* { Azerbaijani; ?; ? } => { Azerbaijani; Latin; Azerbaijan } */
                "az",
                "az_Latn_AZ"
              }, {
                /* { Belarusian; ?; ? } => { Belarusian; Cyrillic; Belarus } */
                "be",
                "be_Cyrl_BY"
              }, {
                /* { Bulgarian; ?; ? } => { Bulgarian; Cyrillic; Bulgaria } */
                "bg",
                "bg_Cyrl_BG"
              }, {
                /* { Bengali; ?; ? } => { Bengali; Bengali; Bangladesh } */
                "bn",
                "bn_Beng_BD"
              }, {
                /* { Tibetan; ?; ? } => { Tibetan; Tibetan; China } */
                "bo",
                "bo_Tibt_CN"
              }, {
                /* { Bosnian; ?; ? } => { Bosnian; Latin; Bosnia and Herzegovina } */
                "bs",
                "bs_Latn_BA"
              }, {
                /* { Blin; ?; ? } => { Blin; Ethiopic; Eritrea } */
                "byn",
                "byn_Ethi_ER"
              }, {
                /* { Catalan; ?; ? } => { Catalan; Latin; Spain } */
                "ca",
                "ca_Latn_ES"
              }, {
                /* { Atsam; ?; ? } => { Atsam; Latin; Nigeria } */
                "cch",
                "cch_Latn_NG"
              }, {
                /* { Chamorro; ?; ? } => { Chamorro; Latin; Guam } */
                "ch",
                "ch_Latn_GU"
              }, {
                /* { Chuukese; ?; ? } => { Chuukese; Latin; Micronesia } */
                "chk",
                "chk_Latn_FM"
              }, {
                /* { Coptic; ?; ? } => { Coptic; Arabic; Egypt } */
                "cop",
                "cop_Arab_EG"
              }, {
                /* { Czech; ?; ? } => { Czech; Latin; Czech Republic } */
                "cs",
                "cs_Latn_CZ"
              }, {
                /* { Welsh; ?; ? } => { Welsh; Latin; United Kingdom } */
                "cy",
                "cy_Latn_GB"
              }, {
                /* { Danish; ?; ? } => { Danish; Latin; Denmark } */
                "da",
                "da_Latn_DK"
              }, {
                /* { German; ?; ? } => { German; Latin; Germany } */
                "de",
                "de_Latn_DE"
              }, {
                /* { Divehi; ?; ? } => { Divehi; Thaana; Maldives } */
                "dv",
                "dv_Thaa_MV"
              }, {
                /* { Dzongkha; ?; ? } => { Dzongkha; Tibetan; Bhutan } */
                "dz",
                "dz_Tibt_BT"
              }, {
                /* { Ewe; ?; ? } => { Ewe; Latin; Ghana } */
                "ee",
                "ee_Latn_GH"
              }, {
                /* { Greek; ?; ? } => { Greek; Greek; Greece } */
                "el",
                "el_Grek_GR"
              }, {
                /* { English; ?; ? } => { English; Latin; United States } */
                "en",
                "en_Latn_US"
              }, {
                /* { Spanish; ?; ? } => { Spanish; Latin; Spain } */
                "es",
                "es_Latn_ES"
              }, {
                /* { Estonian; ?; ? } => { Estonian; Latin; Estonia } */
                "et",
                "et_Latn_EE"
              }, {
                /* { Basque; ?; ? } => { Basque; Latin; Spain } */
                "eu",
                "eu_Latn_ES"
              }, {
                /* { Persian; ?; ? } => { Persian; Arabic; Iran } */
                "fa",
                "fa_Arab_IR"
              }, {
                /* { Finnish; ?; ? } => { Finnish; Latin; Finland } */
                "fi",
                "fi_Latn_FI"
              }, {
                /* { Filipino; ?; ? } => { Filipino; Latin; Philippines } */
                "fil",
                "fil_Latn_PH"
              }, {
                /* { Fijian; ?; ? } => { Fijian; Latin; Fiji } */
                "fj",
                "fj_Latn_FJ"
              }, {
                /* { Faroese; ?; ? } => { Faroese; Latin; Faroe Islands } */
                "fo",
                "fo_Latn_FO"
              }, {
                /* { French; ?; ? } => { French; Latin; France } */
                "fr",
                "fr_Latn_FR"
              }, {
                /* { Friulian; ?; ? } => { Friulian; Latin; Italy } */
                "fur",
                "fur_Latn_IT"
              }, {
                /* { Irish; ?; ? } => { Irish; Latin; Ireland } */
                "ga",
                "ga_Latn_IE"
              }, {
                /* { Ga; ?; ? } => { Ga; Latin; Ghana } */
                "gaa",
                "gaa_Latn_GH"
              }, {
                /* { Geez; ?; ? } => { Geez; Ethiopic; Eritrea } */
                "gez",
                "gez_Ethi_ER"
              }, {
                /* { Galician; ?; ? } => { Galician; Latin; Spain } */
                "gl",
                "gl_Latn_ES"
              }, {
                /* { Guarani; ?; ? } => { Guarani; Latin; Paraguay } */
                "gn",
                "gn_Latn_PY"
              }, {
                /* { Gujarati; ?; ? } => { Gujarati; Gujarati; India } */
                "gu",
                "gu_Gujr_IN"
              }, {
                /* { Manx; ?; ? } => { Manx; Latin; United Kingdom } */
                "gv",
                "gv_Latn_GB"
              }, {
                /* { Hausa; ?; ? } => { Hausa; Latin; Nigeria } */
                "ha",
                "ha_Latn_NG"
              }, {
                /* { Hawaiian; ?; ? } => { Hawaiian; Latin; United States } */
                "haw",
                "haw_Latn_US"
              }, {
                /* { Hindi; ?; ? } => { Hindi; Devanagari; India } */
                "hi",
                "hi_Deva_IN"
              }, {
                /* { Croatian; ?; ? } => { Croatian; Latin; Croatia } */
                "hr",
                "hr_Latn_HR"
              }, {
                /* { Haitian; ?; ? } => { Haitian; Latin; Haiti } */
                "ht",
                "ht_Latn_HT"
              }, {
                /* { Hungarian; ?; ? } => { Hungarian; Latin; Hungary } */
                "hu",
                "hu_Latn_HU"
              }, {
                /* { Armenian; ?; ? } => { Armenian; Armenian; Armenia } */
                "hy",
                "hy_Armn_AM"
              }, {
                /* { Indonesian; ?; ? } => { Indonesian; Latin; Indonesia } */
                "id",
                "id_Latn_ID"
              }, {
                /* { Igbo; ?; ? } => { Igbo; Latin; Nigeria } */
                "ig",
                "ig_Latn_NG"
              }, {
                /* { Sichuan Yi; ?; ? } => { Sichuan Yi; Yi; China } */
                "ii",
                "ii_Yiii_CN"
              }, {
                /* { Icelandic; ?; ? } => { Icelandic; Latin; Iceland } */
                "is",
                "is_Latn_IS"
              }, {
                /* { Italian; ?; ? } => { Italian; Latin; Italy } */
                "it",
                "it_Latn_IT"
              }, {
                /* { Inuktitut; ?; ? } */
                /*  => { Inuktitut; Unified Canadian Aboriginal Syllabics; Canada } */
                "iu",
                "iu_Cans_CA"
              }, {
                /* { null; ?; ? } => { null; Hebrew; Israel } */
                "iw",
                "iw_Hebr_IL"
              }, {
                /* { Japanese; ?; ? } => { Japanese; Japanese; Japan } */
                "ja",
                "ja_Jpan_JP"
              }, {
                /* { Georgian; ?; ? } => { Georgian; Georgian; Georgia } */
                "ka",
                "ka_Geor_GE"
              }, {
                /* { Jju; ?; ? } => { Jju; Latin; Nigeria } */
                "kaj",
                "kaj_Latn_NG"
              }, {
                /* { Kamba; ?; ? } => { Kamba; Latin; Kenya } */
                "kam",
                "kam_Latn_KE"
              }, {
                /* { Tyap; ?; ? } => { Tyap; Latin; Nigeria } */
                "kcg",
                "kcg_Latn_NG"
              }, {
                /* { Koro; ?; ? } => { Koro; Latin; Nigeria } */
                "kfo",
                "kfo_Latn_NG"
              }, {
                /* { Kazakh; ?; ? } => { Kazakh; Cyrillic; Kazakhstan } */
                "kk",
                "kk_Cyrl_KZ"
              }, {
                /* { Kalaallisut; ?; ? } => { Kalaallisut; Latin; Greenland } */
                "kl",
                "kl_Latn_GL"
              }, {
                /* { Khmer; ?; ? } => { Khmer; Khmer; Cambodia } */
                "km",
                "km_Khmr_KH"
              }, {
                /* { Kannada; ?; ? } => { Kannada; Kannada; India } */
                "kn",
                "kn_Knda_IN"
              }, {
                /* { Korean; ?; ? } => { Korean; Korean; South Korea } */
                "ko",
                "ko_Kore_KR"
              }, {
                /* { Konkani; ?; ? } => { Konkani; Devanagari; India } */
                "kok",
                "kok_Deva_IN"
              }, {
                /* { Kpelle; ?; ? } => { Kpelle; Latin; Liberia } */
                "kpe",
                "kpe_Latn_LR"
              }, {
                /* { Kurdish; ?; ? } => { Kurdish; Latin; Turkey } */
                "ku",
                "ku_Latn_TR"
              }, {
                /* { Cornish; ?; ? } => { Cornish; Latin; United Kingdom } */
                "kw",
                "kw_Latn_GB"
              }, {
                /* { Kirghiz; ?; ? } => { Kirghiz; Cyrillic; Kyrgyzstan } */
                "ky",
                "ky_Cyrl_KG"
              }, {
                /* { Latin; ?; ? } => { Latin; Latin; Vatican } */
                "la",
                "la_Latn_VA"
              }, {
                /* { Lingala; ?; ? } => { Lingala; Latin; Congo _ Kinshasa } */
                "ln",
                "ln_Latn_CD"
              }, {
                /* { Lao; ?; ? } => { Lao; Lao; Laos } */
                "lo",
                "lo_Laoo_LA"
              }, {
                /* { Lithuanian; ?; ? } => { Lithuanian; Latin; Lithuania } */
                "lt",
                "lt_Latn_LT"
              }, {
                /* { Latvian; ?; ? } => { Latvian; Latin; Latvia } */
                "lv",
                "lv_Latn_LV"
              }, {
                /* { Malagasy; ?; ? } => { Malagasy; Latin; Madagascar } */
                "mg",
                "mg_Latn_MG"
              }, {
                /* { Marshallese; ?; ? } => { Marshallese; Latin; Marshall Islands } */
                "mh",
                "mh_Latn_MH"
              }, {
                /* { Macedonian; ?; ? } => { Macedonian; Cyrillic; Macedonia } */
                "mk",
                "mk_Cyrl_MK"
              }, {
                /* { Malayalam; ?; ? } => { Malayalam; Malayalam; India } */
                "ml",
                "ml_Mlym_IN"
              }, {
                /* { Mongolian; ?; ? } => { Mongolian; Cyrillic; Mongolia } */
                "mn",
                "mn_Cyrl_MN"
              }, {
                /* { Marathi; ?; ? } => { Marathi; Devanagari; India } */
                "mr",
                "mr_Deva_IN"
              }, {
                /* { Malay; ?; ? } => { Malay; Latin; Malaysia } */
                "ms",
                "ms_Latn_MY"
              }, {
                /* { Maltese; ?; ? } => { Maltese; Latin; Malta } */
                "mt",
                "mt_Latn_MT"
              }, {
                /* { Burmese; ?; ? } => { Burmese; Myanmar; Myanmar } */
                "my",
                "my_Mymr_MM"
              }, {
                /* { Nauru; ?; ? } => { Nauru; Latin; Nauru } */
                "na",
                "na_Latn_NR"
              }, {
                /* { Nepali; ?; ? } => { Nepali; Devanagari; Nepal } */
                "ne",
                "ne_Deva_NP"
              }, {
                /* { Niuean; ?; ? } => { Niuean; Latin; Niue } */
                "niu",
                "niu_Latn_NU"
              }, {
                /* { Dutch; ?; ? } => { Dutch; Latin; Netherlands } */
                "nl",
                "nl_Latn_NL"
              }, {
                /* { Norwegian Nynorsk; ?; ? } => { Norwegian Nynorsk; Latin; Norway } */
                "nn",
                "nn_Latn_NO"
              }, {
                /* { Norwegian; ?; ? } => { Norwegian; Latin; Norway } */
                "no",
                "no_Latn_NO"
              }, {
                /* { South Ndebele; ?; ? } => { South Ndebele; Latin; South Africa } */
                "nr",
                "nr_Latn_ZA"
              }, {
                /* { Northern Sotho; ?; ? } => { Northern Sotho; Latin; South Africa } */
                "nso",
                "nso_Latn_ZA"
              }, {
                /* { Nyanja; ?; ? } => { Nyanja; Latin; Malawi } */
                "ny",
                "ny_Latn_MW"
              }, {
                /* { Oromo; ?; ? } => { Oromo; Latin; Ethiopia } */
                "om",
                "om_Latn_ET"
              }, {
                /* { Oriya; ?; ? } => { Oriya; Oriya; India } */
                "or",
                "or_Orya_IN"
              }, {
                /* { Punjabi; ?; ? } => { Punjabi; Gurmukhi; India } */
                "pa",
                "pa_Guru_IN"
              }, {
                /* { Punjabi; Arabic; ? } => { Punjabi; Arabic; Pakistan } */
                "pa_Arab",
                "pa_Arab_PK"
              }, {
                /* { Punjabi; ?; Pakistan } => { Punjabi; Arabic; Pakistan } */
                "pa_PK",
                "pa_Arab_PK"
              }, {
                /* { Papiamento; ?; ? } => { Papiamento; Latin; Netherlands Antilles } */
                "pap",
                "pap_Latn_AN"
              }, {
                /* { Palauan; ?; ? } => { Palauan; Latin; Palau } */
                "pau",
                "pau_Latn_PW"
              }, {
                /* { Polish; ?; ? } => { Polish; Latin; Poland } */
                "pl",
                "pl_Latn_PL"
              }, {
                /* { Pashto; ?; ? } => { Pashto; Arabic; Afghanistan } */
                "ps",
                "ps_Arab_AF"
              }, {
                /* { Portuguese; ?; ? } => { Portuguese; Latin; Brazil } */
                "pt",
                "pt_Latn_BR"
              }, {
                /* { Rundi; ?; ? } => { Rundi; Latin; Burundi } */
                "rn",
                "rn_Latn_BI"
              }, {
                /* { Romanian; ?; ? } => { Romanian; Latin; Romania } */
                "ro",
                "ro_Latn_RO"
              }, {
                /* { Russian; ?; ? } => { Russian; Cyrillic; Russia } */
                "ru",
                "ru_Cyrl_RU"
              }, {
                /* { Kinyarwanda; ?; ? } => { Kinyarwanda; Latin; Rwanda } */
                "rw",
                "rw_Latn_RW"
              }, {
                /* { Sanskrit; ?; ? } => { Sanskrit; Devanagari; India } */
                "sa",
                "sa_Deva_IN"
              }, {
                /* { Northern Sami; ?; ? } => { Northern Sami; Latin; Norway } */
                "se",
                "se_Latn_NO"
              }, {
                /* { Sango; ?; ? } => { Sango; Latin; Central African Republic } */
                "sg",
                "sg_Latn_CF"
              }, {
                /* { Serbo_Croatian; ?; ? } => { Serbian; Latin; Serbia } */
                "sh",
                "sr_Latn_RS"
              }, {
                /* { Sinhalese; ?; ? } => { Sinhalese; Sinhala; Sri Lanka } */
                "si",
                "si_Sinh_LK"
              }, {
                /* { Sidamo; ?; ? } => { Sidamo; Latin; Ethiopia } */
                "sid",
                "sid_Latn_ET"
              }, {
                /* { Slovak; ?; ? } => { Slovak; Latin; Slovakia } */
                "sk",
                "sk_Latn_SK"
              }, {
                /* { Slovenian; ?; ? } => { Slovenian; Latin; Slovenia } */
                "sl",
                "sl_Latn_SI"
              }, {
                /* { Samoan; ?; ? } => { Samoan; Latin; American Samoa } */
                "sm",
                "sm_Latn_AS"
              }, {
                /* { Somali; ?; ? } => { Somali; Latin; Somalia } */
                "so",
                "so_Latn_SO"
              }, {
                /* { Albanian; ?; ? } => { Albanian; Latin; Albania } */
                "sq",
                "sq_Latn_AL"
              }, {
                /* { Serbian; ?; ? } => { Serbian; Cyrillic; Serbia } */
                "sr",
                "sr_Cyrl_RS"
              }, {
                /* { Swati; ?; ? } => { Swati; Latin; South Africa } */
                "ss",
                "ss_Latn_ZA"
              }, {
                /* { Southern Sotho; ?; ? } => { Southern Sotho; Latin; South Africa } */
                "st",
                "st_Latn_ZA"
              }, {
                /* { Sundanese; ?; ? } => { Sundanese; Latin; Indonesia } */
                "su",
                "su_Latn_ID"
              }, {
                /* { Swedish; ?; ? } => { Swedish; Latin; Sweden } */
                "sv",
                "sv_Latn_SE"
              }, {
                /* { Swahili; ?; ? } => { Swahili; Latin; Tanzania } */
                "sw",
                "sw_Latn_TZ"
              }, {
                /* { Syriac; ?; ? } => { Syriac; Syriac; Syria } */
                "syr",
                "syr_Syrc_SY"
              }, {
                /* { Tamil; ?; ? } => { Tamil; Tamil; India } */
                "ta",
                "ta_Taml_IN"
              }, {
                /* { Telugu; ?; ? } => { Telugu; Telugu; India } */
                "te",
                "te_Telu_IN"
              }, {
                /* { Tetum; ?; ? } => { Tetum; Latin; East Timor } */
                "tet",
                "tet_Latn_TL"
              }, {
                /* { Tajik; ?; ? } => { Tajik; Cyrillic; Tajikistan } */
                "tg",
                "tg_Cyrl_TJ"
              }, {
                /* { Thai; ?; ? } => { Thai; Thai; Thailand } */
                "th",
                "th_Thai_TH"
              }, {
                /* { Tigrinya; ?; ? } => { Tigrinya; Ethiopic; Ethiopia } */
                "ti",
                "ti_Ethi_ET"
              }, {
                /* { Tigre; ?; ? } => { Tigre; Ethiopic; Eritrea } */
                "tig",
                "tig_Ethi_ER"
              }, {
                /* { Turkmen; ?; ? } => { Turkmen; Latin; Turkmenistan } */
                "tk",
                "tk_Latn_TM"
              }, {
                /* { Tokelau; ?; ? } => { Tokelau; Latin; Tokelau } */
                "tkl",
                "tkl_Latn_TK"
              }, {
                /* { Tswana; ?; ? } => { Tswana; Latin; South Africa } */
                "tn",
                "tn_Latn_ZA"
              }, {
                /* { Tonga; ?; ? } => { Tonga; Latin; Tonga } */
                "to",
                "to_Latn_TO"
              }, {
                /* { Tok Pisin; ?; ? } => { Tok Pisin; Latin; Papua New Guinea } */
                "tpi",
                "tpi_Latn_PG"
              }, {
                /* { Turkish; ?; ? } => { Turkish; Latin; Turkey } */
                "tr",
                "tr_Latn_TR"
              }, {
                /* { Tsonga; ?; ? } => { Tsonga; Latin; South Africa } */
                "ts",
                "ts_Latn_ZA"
              }, {
                /* { Tatar; ?; ? } => { Tatar; Cyrillic; Russia } */
                "tt",
                "tt_Cyrl_RU"
              }, {
                /* { Tuvalu; ?; ? } => { Tuvalu; Latin; Tuvalu } */
                "tvl",
                "tvl_Latn_TV"
              }, {
                /* { Tahitian; ?; ? } => { Tahitian; Latin; French Polynesia } */
                "ty",
                "ty_Latn_PF"
              }, {
                /* { Ukrainian; ?; ? } => { Ukrainian; Cyrillic; Ukraine } */
                "uk",
                "uk_Cyrl_UA"
              }, {
                /* { ?; ?; ? } => { English; Latin; United States } */
                "und",
                "en_Latn_US"
              }, {
                /* { ?; ?; Andorra } => { Catalan; Latin; Andorra } */
                "und_AD",
                "ca_Latn_AD"
              }, {
                /* { ?; ?; United Arab Emirates } */
                /*  => { Arabic; Arabic; United Arab Emirates } */
                "und_AE",
                "ar_Arab_AE"
              }, {
                /* { ?; ?; Afghanistan } => { Persian; Arabic; Afghanistan } */
                "und_AF",
                "fa_Arab_AF"
              }, {
                /* { ?; ?; Albania } => { Albanian; Latin; Albania } */
                "und_AL",
                "sq_Latn_AL"
              }, {
                /* { ?; ?; Armenia } => { Armenian; Armenian; Armenia } */
                "und_AM",
                "hy_Armn_AM"
              }, {
                /* { ?; ?; Netherlands Antilles } */
                /*  => { Papiamento; Latin; Netherlands Antilles } */
                "und_AN",
                "pap_Latn_AN"
              }, {
                /* { ?; ?; Angola } => { Portuguese; Latin; Angola } */
                "und_AO",
                "pt_Latn_AO"
              }, {
                /* { ?; ?; Argentina } => { Spanish; Latin; Argentina } */
                "und_AR",
                "es_Latn_AR"
              }, {
                /* { ?; ?; American Samoa } => { Samoan; Latin; American Samoa } */
                "und_AS",
                "sm_Latn_AS"
              }, {
                /* { ?; ?; Austria } => { German; Latin; Austria } */
                "und_AT",
                "de_Latn_AT"
              }, {
                /* { ?; ?; Aruba } => { Dutch; Latin; Aruba } */
                "und_AW",
                "nl_Latn_AW"
              }, {
                /* { ?; ?; Aland Islands } => { Swedish; Latin; Aland Islands } */
                "und_AX",
                "sv_Latn_AX"
              }, {
                /* { ?; ?; Azerbaijan } => { Azerbaijani; Latin; Azerbaijan } */
                "und_AZ",
                "az_Latn_AZ"
              }, {
                /* { ?; Arabic; ? } => { Arabic; Arabic; Egypt } */
                "und_Arab",
                "ar_Arab_EG"
              }, {
                /* { ?; Arabic; India } => { Urdu; Arabic; India } */
                "und_Arab_IN",
                "ur_Arab_IN"
              }, {
                /* { ?; Arabic; Pakistan } => { Punjabi; Arabic; Pakistan } */
                "und_Arab_PK",
                "pa_Arab_PK"
              }, {
                /* { ?; Arabic; Senegal } => { Wolof; Arabic; Senegal } */
                "und_Arab_SN",
                "wo_Arab_SN"
              }, {
                /* { ?; Armenian; ? } => { Armenian; Armenian; Armenia } */
                "und_Armn",
                "hy_Armn_AM"
              }, {
                /* { ?; ?; Bosnia and Herzegovina } */
                /*  => { Bosnian; Latin; Bosnia and Herzegovina } */
                "und_BA",
                "bs_Latn_BA"
              }, {
                /* { ?; ?; Bangladesh } => { Bengali; Bengali; Bangladesh } */
                "und_BD",
                "bn_Beng_BD"
              }, {
                /* { ?; ?; Belgium } => { Dutch; Latin; Belgium } */
                "und_BE",
                "nl_Latn_BE"
              }, {
                /* { ?; ?; Burkina Faso } => { French; Latin; Burkina Faso } */
                "und_BF",
                "fr_Latn_BF"
              }, {
                /* { ?; ?; Bulgaria } => { Bulgarian; Cyrillic; Bulgaria } */
                "und_BG",
                "bg_Cyrl_BG"
              }, {
                /* { ?; ?; Bahrain } => { Arabic; Arabic; Bahrain } */
                "und_BH",
                "ar_Arab_BH"
              }, {
                /* { ?; ?; Burundi } => { Rundi; Latin; Burundi } */
                "und_BI",
                "rn_Latn_BI"
              }, {
                /* { ?; ?; Benin } => { French; Latin; Benin } */
                "und_BJ",
                "fr_Latn_BJ"
              }, {
                /* { ?; ?; Brunei } => { Malay; Latin; Brunei } */
                "und_BN",
                "ms_Latn_BN"
              }, {
                /* { ?; ?; Bolivia } => { Spanish; Latin; Bolivia } */
                "und_BO",
                "es_Latn_BO"
              }, {
                /* { ?; ?; Brazil } => { Portuguese; Latin; Brazil } */
                "und_BR",
                "pt_Latn_BR"
              }, {
                /* { ?; ?; Bhutan } => { Dzongkha; Tibetan; Bhutan } */
                "und_BT",
                "dz_Tibt_BT"
              }, {
                /* { ?; ?; Belarus } => { Belarusian; Cyrillic; Belarus } */
                "und_BY",
                "be_Cyrl_BY"
              }, {
                /* { ?; Bengali; ? } => { Bengali; Bengali; Bangladesh } */
                "und_Beng",
                "bn_Beng_BD"
              }, {
                /* { ?; Bengali; India } => { Assamese; Bengali; India } */
                "und_Beng_IN",
                "as_Beng_IN"
              }, {
                /* { ?; ?; Congo _ Kinshasa } => { French; Latin; Congo _ Kinshasa } */
                "und_CD",
                "fr_Latn_CD"
              }, {
                /* { ?; ?; Central African Republic } */
                /*  => { Sango; Latin; Central African Republic } */
                "und_CF",
                "sg_Latn_CF"
              }, {
                /* { ?; ?; Congo _ Brazzaville } */
                /*  => { Lingala; Latin; Congo _ Brazzaville } */
                "und_CG",
                "ln_Latn_CG"
              }, {
                /* { ?; ?; Switzerland } => { German; Latin; Switzerland } */
                "und_CH",
                "de_Latn_CH"
              }, {
                /* { ?; ?; Ivory Coast } => { French; Latin; Ivory Coast } */
                "und_CI",
                "fr_Latn_CI"
              }, {
                /* { ?; ?; Chile } => { Spanish; Latin; Chile } */
                "und_CL",
                "es_Latn_CL"
              }, {
                /* { ?; ?; Cameroon } => { French; Latin; Cameroon } */
                "und_CM",
                "fr_Latn_CM"
              }, {
                /* { ?; ?; China } => { Chinese; Simplified Han; China } */
                "und_CN",
                "zh_Hans_CN"
              }, {
                /* { ?; ?; Colombia } => { Spanish; Latin; Colombia } */
                "und_CO",
                "es_Latn_CO"
              }, {
                /* { ?; ?; Costa Rica } => { Spanish; Latin; Costa Rica } */
                "und_CR",
                "es_Latn_CR"
              }, {
                /* { ?; ?; Cuba } => { Spanish; Latin; Cuba } */
                "und_CU",
                "es_Latn_CU"
              }, {
                /* { ?; ?; Cape Verde } => { Portuguese; Latin; Cape Verde } */
                "und_CV",
                "pt_Latn_CV"
              }, {
                /* { ?; ?; Cyprus } => { Greek; Greek; Cyprus } */
                "und_CY",
                "el_Grek_CY"
              }, {
                /* { ?; ?; Czech Republic } => { Czech; Latin; Czech Republic } */
                "und_CZ",
                "cs_Latn_CZ"
              }, {
                /* { ?; Unified Canadian Aboriginal Syllabics; ? } */
                /*  => { Inuktitut; Unified Canadian Aboriginal Syllabics; Canada } */
                "und_Cans",
                "iu_Cans_CA"
              }, {
                /* { ?; Cyrillic; ? } => { Russian; Cyrillic; Russia } */
                "und_Cyrl",
                "ru_Cyrl_RU"
              }, {
                /* { ?; Cyrillic; Kazakhstan } => { Kazakh; Cyrillic; Kazakhstan } */
                "und_Cyrl_KZ",
                "kk_Cyrl_KZ"
              }, {
                /* { ?; ?; Germany } => { German; Latin; Germany } */
                "und_DE",
                "de_Latn_DE"
              }, {
                /* { ?; ?; Djibouti } => { Arabic; Arabic; Djibouti } */
                "und_DJ",
                "ar_Arab_DJ"
              }, {
                /* { ?; ?; Denmark } => { Danish; Latin; Denmark } */
                "und_DK",
                "da_Latn_DK"
              }, {
                /* { ?; ?; Dominican Republic } => {Spanish; Latin; Dominican Republic } */
                "und_DO",
                "es_Latn_DO"
              }, {
                /* { ?; ?; Algeria } => { Arabic; Arabic; Algeria } */
                "und_DZ",
                "ar_Arab_DZ"
              }, {
                /* { ?; Devanagari; ? } => { Hindi; Devanagari; India } */
                "und_Deva",
                "hi_Deva_IN"
              }, {
                /* { ?; ?; Ecuador } => { Spanish; Latin; Ecuador } */
                "und_EC",
                "es_Latn_EC"
              }, {
                /* { ?; ?; Estonia } => { Estonian; Latin; Estonia } */
                "und_EE",
                "et_Latn_EE"
              }, {
                /* { ?; ?; Egypt } => { Arabic; Arabic; Egypt } */
                "und_EG",
                "ar_Arab_EG"
              }, {
                /* { ?; ?; Western Sahara } => { Arabic; Arabic; Western Sahara } */
                "und_EH",
                "ar_Arab_EH"
              }, {
                /* { ?; ?; Eritrea } => { Tigrinya; Ethiopic; Eritrea } */
                "und_ER",
                "ti_Ethi_ER"
              }, {
                /* { ?; ?; Spain } => { Spanish; Latin; Spain } */
                "und_ES",
                "es_Latn_ES"
              }, {
                /* { ?; ?; Ethiopia } => { Amharic; Ethiopic; Ethiopia } */
                "und_ET",
                "am_Ethi_ET"
              }, {
                /* { ?; Ethiopic; ? } => { Amharic; Ethiopic; Ethiopia } */
                "und_Ethi",
                "am_Ethi_ET"
              }, {
                /* { ?; Ethiopic; Eritrea } => { Blin; Ethiopic; Eritrea } */
                "und_Ethi_ER",
                "byn_Ethi_ER"
              }, {
                /* { ?; ?; Finland } => { Finnish; Latin; Finland } */
                "und_FI",
                "fi_Latn_FI"
              }, {
                /* { ?; ?; Fiji } => { Fijian; Latin; Fiji } */
                "und_FJ",
                "fj_Latn_FJ"
              }, {
                /* { ?; ?; Micronesia } => { Chuukese; Latin; Micronesia } */
                "und_FM",
                "chk_Latn_FM"
              }, {
                /* { ?; ?; Faroe Islands } => { Faroese; Latin; Faroe Islands } */
                "und_FO",
                "fo_Latn_FO"
              }, {
                /* { ?; ?; France } => { French; Latin; France } */
                "und_FR",
                "fr_Latn_FR"
              }, {
                /* { ?; ?; Gabon } => { French; Latin; Gabon } */
                "und_GA",
                "fr_Latn_GA"
              }, {
                /* { ?; ?; Georgia } => { Georgian; Georgian; Georgia } */
                "und_GE",
                "ka_Geor_GE"
              }, {
                /* { ?; ?; French Guiana } => { French; Latin; French Guiana } */
                "und_GF",
                "fr_Latn_GF"
              }, {
                /* { ?; ?; Greenland } => { Kalaallisut; Latin; Greenland } */
                "und_GL",
                "kl_Latn_GL"
              }, {
                /* { ?; ?; Guinea } => { French; Latin; Guinea } */
                "und_GN",
                "fr_Latn_GN"
              }, {
                /* { ?; ?; Guadeloupe } => { French; Latin; Guadeloupe } */
                "und_GP",
                "fr_Latn_GP"
              }, {
                /* { ?; ?; Equatorial Guinea } => { French; Latin; Equatorial Guinea } */
                "und_GQ",
                "fr_Latn_GQ"
              }, {
                /* { ?; ?; Greece } => { Greek; Greek; Greece } */
                "und_GR",
                "el_Grek_GR"
              }, {
                /* { ?; ?; Guatemala } => { Spanish; Latin; Guatemala } */
                "und_GT",
                "es_Latn_GT"
              }, {
                /* { ?; ?; Guam } => { Chamorro; Latin; Guam } */
                "und_GU",
                "ch_Latn_GU"
              }, {
                /* { ?; ?; Guinea_Bissau } => { Portuguese; Latin; Guinea_Bissau } */
                "und_GW",
                "pt_Latn_GW"
              }, {
                /* { ?; Georgian; ? } => { Georgian; Georgian; Georgia } */
                "und_Geor",
                "ka_Geor_GE"
              }, {
                /* { ?; Greek; ? } => { Greek; Greek; Greece } */
                "und_Grek",
                "el_Grek_GR"
              }, {
                /* { ?; Gujarati; ? } => { Gujarati; Gujarati; India } */
                "und_Gujr",
                "gu_Gujr_IN"
              }, {
                /* { ?; Gurmukhi; ? } => { Punjabi; Gurmukhi; India } */
                "und_Guru",
                "pa_Guru_IN"
              }, {
                /* { ?; ?; Hong Kong SAR China } */
                /*  => { Chinese; Traditional Han; Hong Kong SAR China } */
                "und_HK",
                "zh_Hant_HK"
              }, {
                /* { ?; ?; Honduras } => { Spanish; Latin; Honduras } */
                "und_HN",
                "es_Latn_HN"
              }, {
                /* { ?; ?; Croatia } => { Croatian; Latin; Croatia } */
                "und_HR",
                "hr_Latn_HR"
              }, {
                /* { ?; ?; Haiti } => { Haitian; Latin; Haiti } */
                "und_HT",
                "ht_Latn_HT"
              }, {
                /* { ?; ?; Hungary } => { Hungarian; Latin; Hungary } */
                "und_HU",
                "hu_Latn_HU"
              }, {
                /* { ?; Han; ? } => { Chinese; Simplified Han; China } */
                "und_Hani",
                "zh_Hans_CN"
              }, {
                /* { ?; Simplified Han; ? } => { Chinese; Simplified Han; China } */
                "und_Hans",
                "zh_Hans_CN"
              }, {
                /* { ?; Traditional Han; ? } */
                /*  => { Chinese; Traditional Han; Hong Kong SAR China } */
                "und_Hant",
                "zh_Hant_HK"
              }, {
                /* { ?; Hebrew; ? } => { null; Hebrew; Israel } */
                "und_Hebr",
                "iw_Hebr_IL"
              }, {
                /* { ?; ?; Indonesia } => { Sundanese; Latin; Indonesia } */
                "und_ID",
                "su_Latn_ID"
              }, {
                /* { ?; ?; Israel } => { null; Hebrew; Israel } */
                "und_IL",
                "iw_Hebr_IL"
              }, {
                /* { ?; ?; India } => { Hindi; Devanagari; India } */
                "und_IN",
                "hi_Deva_IN"
              }, {
                /* { ?; ?; Iraq } => { Arabic; Arabic; Iraq } */
                "und_IQ",
                "ar_Arab_IQ"
              }, {
                /* { ?; ?; Iran } => { Persian; Arabic; Iran } */
                "und_IR",
                "fa_Arab_IR"
              }, {
                /* { ?; ?; Iceland } => { Icelandic; Latin; Iceland } */
                "und_IS",
                "is_Latn_IS"
              }, {
                /* { ?; ?; Italy } => { Italian; Latin; Italy } */
                "und_IT",
                "it_Latn_IT"
              }, {
                /* { ?; ?; Jordan } => { Arabic; Arabic; Jordan } */
                "und_JO",
                "ar_Arab_JO"
              }, {
                /* { ?; ?; Japan } => { Japanese; Japanese; Japan } */
                "und_JP",
                "ja_Jpan_JP"
              }, {
                /* { ?; Japanese; ? } => { Japanese; Japanese; Japan } */
                "und_Jpan",
                "ja_Jpan_JP"
              }, {
                /* { ?; ?; Kyrgyzstan } => { Kirghiz; Cyrillic; Kyrgyzstan } */
                "und_KG",
                "ky_Cyrl_KG"
              }, {
                /* { ?; ?; Cambodia } => { Khmer; Khmer; Cambodia } */
                "und_KH",
                "km_Khmr_KH"
              }, {
                /* { ?; ?; Comoros } => { Arabic; Arabic; Comoros } */
                "und_KM",
                "ar_Arab_KM"
              }, {
                /* { ?; ?; North Korea } => { Korean; Korean; North Korea } */
                "und_KP",
                "ko_Kore_KP"
              }, {
                /* { ?; ?; South Korea } => { Korean; Korean; South Korea } */
                "und_KR",
                "ko_Kore_KR"
              }, {
                /* { ?; ?; Kuwait } => { Arabic; Arabic; Kuwait } */
                "und_KW",
                "ar_Arab_KW"
              }, {
                /* { ?; ?; Kazakhstan } => { Russian; Cyrillic; Kazakhstan } */
                "und_KZ",
                "ru_Cyrl_KZ"
              }, {
                /* { ?; Khmer; ? } => { Khmer; Khmer; Cambodia } */
                "und_Khmr",
                "km_Khmr_KH"
              }, {
                /* { ?; Kannada; ? } => { Kannada; Kannada; India } */
                "und_Knda",
                "kn_Knda_IN"
              }, {
                /* { ?; Korean; ? } => { Korean; Korean; South Korea } */
                "und_Kore",
                "ko_Kore_KR"
              }, {
                /* { ?; ?; Laos } => { Lao; Lao; Laos } */
                "und_LA",
                "lo_Laoo_LA"
              }, {
                /* { ?; ?; Lebanon } => { Arabic; Arabic; Lebanon } */
                "und_LB",
                "ar_Arab_LB"
              }, {
                /* { ?; ?; Liechtenstein } => { German; Latin; Liechtenstein } */
                "und_LI",
                "de_Latn_LI"
              }, {
                /* { ?; ?; Sri Lanka } => { Sinhalese; Sinhala; Sri Lanka } */
                "und_LK",
                "si_Sinh_LK"
              }, {
                /* { ?; ?; Lesotho } => { Southern Sotho; Latin; Lesotho } */
                "und_LS",
                "st_Latn_LS"
              }, {
                /* { ?; ?; Lithuania } => { Lithuanian; Latin; Lithuania } */
                "und_LT",
                "lt_Latn_LT"
              }, {
                /* { ?; ?; Luxembourg } => { French; Latin; Luxembourg } */
                "und_LU",
                "fr_Latn_LU"
              }, {
                /* { ?; ?; Latvia } => { Latvian; Latin; Latvia } */
                "und_LV",
                "lv_Latn_LV"
              }, {
                /* { ?; ?; Libya } => { Arabic; Arabic; Libya } */
                "und_LY",
                "ar_Arab_LY"
              }, {
                /* { ?; Lao; ? } => { Lao; Lao; Laos } */
                "und_Laoo",
                "lo_Laoo_LA"
              }, {
                /* { ?; Latin; Spain } => { Catalan; Latin; Spain } */
                "und_Latn_ES",
                "ca_Latn_ES"
              }, {
                /* { ?; Latin; Ethiopia } => { Afar; Latin; Ethiopia } */
                "und_Latn_ET",
                "aa_Latn_ET"
              }, {
                /* { ?; Latin; United Kingdom } => { Welsh; Latin; United Kingdom } */
                "und_Latn_GB",
                "cy_Latn_GB"
              }, {
                /* { ?; Latin; Ghana } => { Akan; Latin; Ghana } */
                "und_Latn_GH",
                "ak_Latn_GH"
              }, {
                /* { ?; Latin; Indonesia } => { Indonesian; Latin; Indonesia } */
                "und_Latn_ID",
                "id_Latn_ID"
              }, {
                /* { ?; Latin; Italy } => { Friulian; Latin; Italy } */
                "und_Latn_IT",
                "fur_Latn_IT"
              }, {
                /* { ?; Latin; Nigeria } => { Atsam; Latin; Nigeria } */
                "und_Latn_NG",
                "cch_Latn_NG"
              }, {
                /* { ?; Latin; Turkey } => { Kurdish; Latin; Turkey } */
                "und_Latn_TR",
                "ku_Latn_TR"
              }, {
                /* { ?; Latin; South Africa } => { Afrikaans; Latin; South Africa } */
                "und_Latn_ZA",
                "af_Latn_ZA"
              }, {
                /* { ?; ?; Morocco } => { Arabic; Arabic; Morocco } */
                "und_MA",
                "ar_Arab_MA"
              }, {
                /* { ?; ?; Monaco } => { French; Latin; Monaco } */
                "und_MC",
                "fr_Latn_MC"
              }, {
                /* { ?; ?; Moldova } => { Romanian; Latin; Moldova } */
                "und_MD",
                "ro_Latn_MD"
              }, {
                /* { ?; ?; Montenegro } => { Serbian; Cyrillic; Montenegro } */
                "und_ME",
                "sr_Cyrl_ME"
              }, {
                /* { ?; ?; Madagascar } => { Malagasy; Latin; Madagascar } */
                "und_MG",
                "mg_Latn_MG"
              }, {
                /* { ?; ?; Marshall Islands } => {Marshallese; Latin; Marshall Islands } */
                "und_MH",
                "mh_Latn_MH"
              }, {
                /* { ?; ?; Macedonia } => { Macedonian; Cyrillic; Macedonia } */
                "und_MK",
                "mk_Cyrl_MK"
              }, {
                /* { ?; ?; Mali } => { French; Latin; Mali } */
                "und_ML",
                "fr_Latn_ML"
              }, {
                /* { ?; ?; Myanmar } => { Burmese; Myanmar; Myanmar } */
                "und_MM",
                "my_Mymr_MM"
              }, {
                /* { ?; ?; Mongolia } => { Mongolian; Cyrillic; Mongolia } */
                "und_MN",
                "mn_Cyrl_MN"
              }, {
                /* { ?; ?; Macao SAR China } */
                /*  => { Chinese; Traditional Han; Macao SAR China } */
                "und_MO",
                "zh_Hant_MO"
              }, {
                /* { ?; ?; Martinique } => { French; Latin; Martinique } */
                "und_MQ",
                "fr_Latn_MQ"
              }, {
                /* { ?; ?; Mauritania } => { Arabic; Arabic; Mauritania } */
                "und_MR",
                "ar_Arab_MR"
              }, {
                /* { ?; ?; Malta } => { Maltese; Latin; Malta } */
                "und_MT",
                "mt_Latn_MT"
              }, {
                /* { ?; ?; Maldives } => { Divehi; Thaana; Maldives } */
                "und_MV",
                "dv_Thaa_MV"
              }, {
                /* { ?; ?; Malawi } => { Nyanja; Latin; Malawi } */
                "und_MW",
                "ny_Latn_MW"
              }, {
                /* { ?; ?; Mexico } => { Spanish; Latin; Mexico } */
                "und_MX",
                "es_Latn_MX"
              }, {
                /* { ?; ?; Malaysia } => { Malay; Latin; Malaysia } */
                "und_MY",
                "ms_Latn_MY"
              }, {
                /* { ?; ?; Mozambique } => { Portuguese; Latin; Mozambique } */
                "und_MZ",
                "pt_Latn_MZ"
              }, {
                /* { ?; Malayalam; ? } => { Malayalam; Malayalam; India } */
                "und_Mlym",
                "ml_Mlym_IN"
              }, {
                /* { ?; Myanmar; ? } => { Burmese; Myanmar; Myanmar } */
                "und_Mymr",
                "my_Mymr_MM"
              }, {
                /* { ?; ?; New Caledonia } => { French; Latin; New Caledonia } */
                "und_NC",
                "fr_Latn_NC"
              }, {
                /* { ?; ?; Niger } => { French; Latin; Niger } */
                "und_NE",
                "fr_Latn_NE"
              }, {
                /* { ?; ?; Nigeria } => { Hausa; Latin; Nigeria } */
                "und_NG",
                "ha_Latn_NG"
              }, {
                /* { ?; ?; Nicaragua } => { Spanish; Latin; Nicaragua } */
                "und_NI",
                "es_Latn_NI"
              }, {
                /* { ?; ?; Netherlands } => { Dutch; Latin; Netherlands } */
                "und_NL",
                "nl_Latn_NL"
              }, {
                /* { ?; ?; Norway } => { Norwegian; Latin; Norway } */
                "und_NO",
                "no_Latn_NO"
              }, {
                /* { ?; ?; Nepal } => { Nepali; Devanagari; Nepal } */
                "und_NP",
                "ne_Deva_NP"
              }, {
                /* { ?; ?; Nauru } => { Nauru; Latin; Nauru } */
                "und_NR",
                "na_Latn_NR"
              }, {
                /* { ?; ?; Niue } => { Niuean; Latin; Niue } */
                "und_NU",
                "niu_Latn_NU"
              }, {
                /* { ?; ?; Oman } => { Arabic; Arabic; Oman } */
                "und_OM",
                "ar_Arab_OM"
              }, {
                /* { ?; Oriya; ? } => { Oriya; Oriya; India } */
                "und_Orya",
                "or_Orya_IN"
              }, {
                /* { ?; ?; Panama } => { Spanish; Latin; Panama } */
                "und_PA",
                "es_Latn_PA"
              }, {
                /* { ?; ?; Peru } => { Spanish; Latin; Peru } */
                "und_PE",
                "es_Latn_PE"
              }, {
                /* { ?; ?; French Polynesia } => { Tahitian; Latin; French Polynesia } */
                "und_PF",
                "ty_Latn_PF"
              }, {
                /* { ?; ?; Papua New Guinea } => { Tok Pisin; Latin; Papua New Guinea } */
                "und_PG",
                "tpi_Latn_PG"
              }, {
                /* { ?; ?; Philippines } => { Filipino; Latin; Philippines } */
                "und_PH",
                "fil_Latn_PH"
              }, {
                /* { ?; ?; Poland } => { Polish; Latin; Poland } */
                "und_PL",
                "pl_Latn_PL"
              }, {
                /* { ?; ?; Saint Pierre and Miquelon } */
                /*  => { French; Latin; Saint Pierre and Miquelon } */
                "und_PM",
                "fr_Latn_PM"
              }, {
                /* { ?; ?; Puerto Rico } => { Spanish; Latin; Puerto Rico } */
                "und_PR",
                "es_Latn_PR"
              }, {
                /* { ?; ?; Palestinian Territory } */
                /*  => { Arabic; Arabic; Palestinian Territory } */
                "und_PS",
                "ar_Arab_PS"
              }, {
                /* { ?; ?; Portugal } => { Portuguese; Latin; Portugal } */
                "und_PT",
                "pt_Latn_PT"
              }, {
                /* { ?; ?; Palau } => { Palauan; Latin; Palau } */
                "und_PW",
                "pau_Latn_PW"
              }, {
                /* { ?; ?; Paraguay } => { Guarani; Latin; Paraguay } */
                "und_PY",
                "gn_Latn_PY"
              }, {
                /* { ?; ?; Qatar } => { Arabic; Arabic; Qatar } */
                "und_QA",
                "ar_Arab_QA"
              }, {
                /* { ?; ?; Reunion } => { French; Latin; Reunion } */
                "und_RE",
                "fr_Latn_RE"
              }, {
                /* { ?; ?; Romania } => { Romanian; Latin; Romania } */
                "und_RO",
                "ro_Latn_RO"
              }, {
                /* { ?; ?; Serbia } => { Serbian; Cyrillic; Serbia } */
                "und_RS",
                "sr_Cyrl_RS"
              }, {
                /* { ?; ?; Russia } => { Russian; Cyrillic; Russia } */
                "und_RU",
                "ru_Cyrl_RU"
              }, {
                /* { ?; ?; Rwanda } => { Kinyarwanda; Latin; Rwanda } */
                "und_RW",
                "rw_Latn_RW"
              }, {
                /* { ?; ?; Saudi Arabia } => { Arabic; Arabic; Saudi Arabia } */
                "und_SA",
                "ar_Arab_SA"
              }, {
                /* { ?; ?; Sudan } => { Arabic; Arabic; Sudan } */
                "und_SD",
                "ar_Arab_SD"
              }, {
                /* { ?; ?; Sweden } => { Swedish; Latin; Sweden } */
                "und_SE",
                "sv_Latn_SE"
              }, {
                /* { ?; ?; Singapore } => { Chinese; Simplified Han; Singapore } */
                "und_SG",
                "zh_Hans_SG"
              }, {
                /* { ?; ?; Slovenia } => { Slovenian; Latin; Slovenia } */
                "und_SI",
                "sl_Latn_SI"
              }, {
                /* { ?; ?; Svalbard and Jan Mayen } */
                /*  => { Norwegian; Latin; Svalbard and Jan Mayen } */
                "und_SJ",
                "no_Latn_SJ"
              }, {
                /* { ?; ?; Slovakia } => { Slovak; Latin; Slovakia } */
                "und_SK",
                "sk_Latn_SK"
              }, {
                /* { ?; ?; San Marino } => { Italian; Latin; San Marino } */
                "und_SM",
                "it_Latn_SM"
              }, {
                /* { ?; ?; Senegal } => { French; Latin; Senegal } */
                "und_SN",
                "fr_Latn_SN"
              }, {
                /* { ?; ?; Somalia } => { Somali; Latin; Somalia } */
                "und_SO",
                "so_Latn_SO"
              }, {
                /* { ?; ?; Suriname } => { Dutch; Latin; Suriname } */
                "und_SR",
                "nl_Latn_SR"
              }, {
                /* { ?; ?; Sao Tome and Principe } */
                /*  => { Portuguese; Latin; Sao Tome and Principe } */
                "und_ST",
                "pt_Latn_ST"
              }, {
                /* { ?; ?; El Salvador } => { Spanish; Latin; El Salvador } */
                "und_SV",
                "es_Latn_SV"
              }, {
                /* { ?; ?; Syria } => { Arabic; Arabic; Syria } */
                "und_SY",
                "ar_Arab_SY"
              }, {
                /* { ?; Sinhala; ? } => { Sinhalese; Sinhala; Sri Lanka } */
                "und_Sinh",
                "si_Sinh_LK"
              }, {
                /* { ?; Syriac; ? } => { Syriac; Syriac; Syria } */
                "und_Syrc",
                "syr_Syrc_SY"
              }, {
                /* { ?; ?; Chad } => { Arabic; Arabic; Chad } */
                "und_TD",
                "ar_Arab_TD"
              }, {
                /* { ?; ?; Togo } => { French; Latin; Togo } */
                "und_TG",
                "fr_Latn_TG"
              }, {
                /* { ?; ?; Thailand } => { Thai; Thai; Thailand } */
                "und_TH",
                "th_Thai_TH"
              }, {
                /* { ?; ?; Tajikistan } => { Tajik; Cyrillic; Tajikistan } */
                "und_TJ",
                "tg_Cyrl_TJ"
              }, {
                /* { ?; ?; Tokelau } => { Tokelau; Latin; Tokelau } */
                "und_TK",
                "tkl_Latn_TK"
              }, {
                /* { ?; ?; East Timor } => { Tetum; Latin; East Timor } */
                "und_TL",
                "tet_Latn_TL"
              }, {
                /* { ?; ?; Turkmenistan } => { Turkmen; Latin; Turkmenistan } */
                "und_TM",
                "tk_Latn_TM"
              }, {
                /* { ?; ?; Tunisia } => { Arabic; Arabic; Tunisia } */
                "und_TN",
                "ar_Arab_TN"
              }, {
                /* { ?; ?; Tonga } => { Tonga; Latin; Tonga } */
                "und_TO",
                "to_Latn_TO"
              }, {
                /* { ?; ?; Turkey } => { Turkish; Latin; Turkey } */
                "und_TR",
                "tr_Latn_TR"
              }, {
                /* { ?; ?; Tuvalu } => { Tuvalu; Latin; Tuvalu } */
                "und_TV",
                "tvl_Latn_TV"
              }, {
                /* { ?; ?; Taiwan } => { Chinese; Traditional Han; Taiwan } */
                "und_TW",
                "zh_Hant_TW"
              }, {
                /* { ?; Tamil; ? } => { Tamil; Tamil; India } */
                "und_Taml",
                "ta_Taml_IN"
              }, {
                /* { ?; Telugu; ? } => { Telugu; Telugu; India } */
                "und_Telu",
                "te_Telu_IN"
              }, {
                /* { ?; Thaana; ? } => { Divehi; Thaana; Maldives } */
                "und_Thaa",
                "dv_Thaa_MV"
              }, {
                /* { ?; Thai; ? } => { Thai; Thai; Thailand } */
                "und_Thai",
                "th_Thai_TH"
              }, {
                /* { ?; Tibetan; ? } => { Tibetan; Tibetan; China } */
                "und_Tibt",
                "bo_Tibt_CN"
              }, {
                /* { ?; ?; Ukraine } => { Ukrainian; Cyrillic; Ukraine } */
                "und_UA",
                "uk_Cyrl_UA"
              }, {
                /* { ?; ?; Uruguay } => { Spanish; Latin; Uruguay } */
                "und_UY",
                "es_Latn_UY"
              }, {
                /* { ?; ?; Uzbekistan } => { Uzbek; Cyrillic; Uzbekistan } */
                "und_UZ",
                "uz_Cyrl_UZ"
              }, {
                /* { ?; ?; Vatican } => { Latin; Latin; Vatican } */
                "und_VA",
                "la_Latn_VA"
              }, {
                /* { ?; ?; Venezuela } => { Spanish; Latin; Venezuela } */
                "und_VE",
                "es_Latn_VE"
              }, {
                /* { ?; ?; Vietnam } => { Vietnamese; Latin; Vietnam } */
                "und_VN",
                "vi_Latn_VN"
              }, {
                /* { ?; ?; Vanuatu } => { French; Latin; Vanuatu } */
                "und_VU",
                "fr_Latn_VU"
              }, {
                /* { ?; ?; Wallis and Futuna } => { French; Latin; Wallis and Futuna } */
                "und_WF",
                "fr_Latn_WF"
              }, {
                /* { ?; ?; Samoa } => { Samoan; Latin; Samoa } */
                "und_WS",
                "sm_Latn_WS"
              }, {
                /* { ?; ?; Yemen } => { Arabic; Arabic; Yemen } */
                "und_YE",
                "ar_Arab_YE"
              }, {
                /* { ?; ?; Mayotte } => { French; Latin; Mayotte } */
                "und_YT",
                "fr_Latn_YT"
              }, {
                /* { ?; Yi; ? } => { Sichuan Yi; Yi; China } */
                "und_Yiii",
                "ii_Yiii_CN"
              }, {
                /* { Urdu; ?; ? } => { Urdu; Arabic; India } */
                "ur",
                "ur_Arab_IN"
              }, {
                /* { Uzbek; ?; ? } => { Uzbek; Cyrillic; Uzbekistan } */
                "uz",
                "uz_Cyrl_UZ"
              }, {
                /* { Uzbek; ?; Afghanistan } => { Uzbek; Arabic; Afghanistan } */
                "uz_AF",
                "uz_Arab_AF"
              }, {
                /* { Uzbek; Arabic; ? } => { Uzbek; Arabic; Afghanistan } */
                "uz_Arab",
                "uz_Arab_AF"
              }, {
                /* { Venda; ?; ? } => { Venda; Latin; South Africa } */
                "ve",
                "ve_Latn_ZA"
              }, {
                /* { Vietnamese; ?; ? } => { Vietnamese; Latin; Vietnam } */
                "vi",
                "vi_Latn_VN"
              }, {
                /* { Walamo; ?; ? } => { Walamo; Ethiopic; Ethiopia } */
                "wal",
                "wal_Ethi_ET"
              }, {
                /* { Wolof; ?; ? } => { Wolof; Arabic; Senegal } */
                "wo",
                "wo_Arab_SN"
              }, {
                /* { Wolof; ?; Senegal } => { Wolof; Latin; Senegal } */
                "wo_SN",
                "wo_Latn_SN"
              }, {
                /* { Xhosa; ?; ? } => { Xhosa; Latin; South Africa } */
                "xh",
                "xh_Latn_ZA"
              }, {
                /* { Yoruba; ?; ? } => { Yoruba; Latin; Nigeria } */
                "yo",
                "yo_Latn_NG"
              }, {
                /* { Chinese; ?; ? } => { Chinese; Simplified Han; China } */
                "zh",
                "zh_Hans_CN"
              }, {
                /* { Chinese; ?; Hong Kong SAR China } */
                /*  => { Chinese; Traditional Han; Hong Kong SAR China } */
                "zh_HK",
                "zh_Hant_HK"
              }, {
                /* { Chinese; Han; ? } => { Chinese; Simplified Han; China } */
                "zh_Hani",
                "zh_Hans_CN"
              }, {
                /* { Chinese; Traditional Han; ? } */
                /*  => { Chinese; Traditional Han; Taiwan } */
                "zh_Hant",
                "zh_Hant_TW"
              }, {
                /* { Chinese; ?; Macao SAR China } */
                /*  => { Chinese; Traditional Han; Macao SAR China } */
                "zh_MO",
                "zh_Hant_MO"
              }, {
                /* { Chinese; ?; Taiwan } => { Chinese; Traditional Han; Taiwan } */
                "zh_TW",
                "zh_Hant_TW"
              }, {
                /* { Zulu; ?; ? } => { Zulu; Latin; South Africa } */
                "zu",
                "zu_Latn_ZA"
              }
        };

        HashMap tmpMap = new HashMap();
        for (int i = 0; i < likelySubtagTable.length; i++) {
            ULocale loc = new ULocale(likelySubtagTable[i][1]);
            tmpMap.put(likelySubtagTable[i][0], loc);
        }

        HashMap tmpSubtagsMap = new HashMap();
        for (int i = 0; i < likelySubtagTable.length; ++i) {
            tmpSubtagsMap.put(likelySubtagTable[i][0], likelySubtagTable[i][1]);
        }
      
        synchronized (ULocale.class) {
            if (_likelySubtagMaximizeMap == null) {
                _likelySubtagMaximizeMap = tmpMap;
            }
            if (_likelySubtagsMap == null) {
                _likelySubtagsMap = tmpSubtagsMap;
            }
        }
    }

    private static final String UNDEFINED_LANGUAGE = "und";
    private static final String UNDEFINED_SCRIPT = "Zzzz";
    private static final String UNDEFINED_REGION = "ZZ";
    
    /**
     * Supply most likely subtags to the given locale
     * @param loc The input locale
     * @return A ULocale with most likely subtags filled in.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public static ULocale addLikelySubtag(ULocale loc) {
        initLikelySubtagMaximizeMap();

        // Replace any deprecated subtags with their canonical values.
        // TODO: not yet implemented.

        // If the tag is grandfathered, then return it.
        // TODO: not yet implemented.

        // Remove the script Zzzz and the region ZZ if they occur;
        // change an empty language subtag to 'und'.

        String language = loc.getLanguage();
        String script = loc.getScript();
        String region = loc.getCountry();

        if (language.length() == 0) {
            language = UNDEFINED_LANGUAGE;
        }
        if (script.equals(UNDEFINED_SCRIPT)) {
            script = EMPTY_STRING;
        }
        if (region.equals(UNDEFINED_REGION)) {
            region = EMPTY_STRING;
        }

        // Lookup
        boolean hasScript = script.length() != 0;
        boolean hasRegion = region.length() != 0;
        ULocale match;
        boolean bDone = false;

        if (hasScript && hasRegion) {
            // Lookup language_script_region
            match = (ULocale)_likelySubtagMaximizeMap.get(language + "_" + script + "_" + region);
            if (match != null) {
                language = match.getLanguage();
                script = match.getScript();
                region = match.getCountry();
                bDone = true;
            }
        }
        if (!bDone && hasScript) {
            // Lookup language_script
            match = (ULocale)_likelySubtagMaximizeMap.get(language + "_" + script);
            if (match != null) {
                language = match.getLanguage();
                script = match.getScript();
                if (!hasRegion) {
                    region = match.getCountry();
                }
                bDone = true;
            }
        }
        if (!bDone && hasRegion) {
            // Lookup language_region
            match = (ULocale)_likelySubtagMaximizeMap.get(language + "_" + region);
            if (match != null) {
                language = match.getLanguage();
                region = match.getCountry();
                if (!hasScript) {
                    script = match.getScript();
                }
                bDone = true;
            }
        }
        if (!bDone) {
            // Lookup language
            match = (ULocale)_likelySubtagMaximizeMap.get(language);
            if (match != null) {
                language = match.getLanguage();
                if (!hasScript) {
                    script = match.getScript();
                }
                if (!hasRegion) {
                    region = match.getCountry();
                }
                bDone = true;
            }
        }

        ULocale result = null;

        if (bDone) {
            // Check if we need to create a new locale instance
            if (language.equals(loc.getLanguage())
                    && script.equals(loc.getScript())
                    && region.equals(loc.getCountry())) {
                // Nothing had changed - return the input locale
                result = loc;
            } else {
                StringBuffer buf = new StringBuffer();
                buf.append(language);
                if (script.length() != 0) {
                    buf.append(UNDERSCORE);
                    buf.append(script);
                }
                if (region.length() != 0) {
                    buf.append(UNDERSCORE);
                    buf.append(region);
                }
                String variant = loc.getVariant();
                if (variant.length() != 0) {
                    buf.append(UNDERSCORE);
                    buf.append(variant);
                }
                int keywordsIdx = loc.localeID.indexOf('@');
                if (keywordsIdx >= 0) {
                    buf.append(loc.localeID.substring(keywordsIdx));
                }
                result = new ULocale(buf.toString());
            }
        } else {
            if (hasScript && hasRegion && language != UNDEFINED_LANGUAGE) {
                // If non of these succeed, if the original had language, region
                // and script, return it.
                result = loc;
            } else {
                // Otherwise, signal an error.
                // TODO: For now, we just return the input locale.
                result = loc;
            }
        }

        return result;
    }

    public static ULocale
    addLikelySubtags(ULocale loc)
    {
        initLikelySubtagMaximizeMap();

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

    public static ULocale
    minimizeSubtags(ULocale loc)
    {
        initLikelySubtagMaximizeMap();

        String[] tags = new String[3];

        int trailingIndex = parseTagString(
                loc.localeID,
                tags);

        String originalLang = (String)tags[0];
        String originalScript = (String)tags[1];
        String originalRegion = (String)tags[2];
        String originalTrailing = null;

        /*
         * Create a new tag string that contains just
         * the language, script, and region.  This will
         * normalize the subtags we're interested in,
         * including removing any explicit unknown
         * script or region subtags.  It also remmoves
         * any variants and keywords.
         */
        String originalTag = 
                    createTagString(
                        originalLang,
                        originalScript,
                        originalRegion,
                        null);
     
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
         * Start first with just the language.
         **/
        {
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

        IDParser parser = null;
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
            parser = new IDParser(alternateTags);
    
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
                parser = new IDParser(alternateTags);
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
                parser = new IDParser(alternateTags);
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
        IDParser parser = new IDParser(localeID);
    
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
    
            String likelySubtags = (String)_likelySubtagsMap.get(searchTag);
    
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
    
            String likelySubtags = (String)_likelySubtagsMap.get(searchTag);
    
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
    
            String likelySubtags = (String)_likelySubtagsMap.get(searchTag);
    
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
    
            String likelySubtags = (String)_likelySubtagsMap.get(searchTag);
    
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

}
