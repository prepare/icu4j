/**
*******************************************************************************
* Copyright (C) 2001, International Business Machines Corporation and         *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package com.ibm.text;

import com.ibm.text.resources.LocaleScript;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import com.ibm.text.UCharacter;

/**
 * A class to reflect UTR #24: Script Names
 * (based on ISO 15924:2000, "Code for the representation of names of
 * scripts".)  UTR #24 describes the basis for a new Unicode data file,
 * Scripts.txt.
 */
public final class UScript {
    
    public static final int USCRIPT_INVALID_CODE = -1;
    public static final int USCRIPT_COMMON       =  0;  /* Zyyy */
    public static final int USCRIPT_INHERITED    =  1;  /* Qaai */
    public static final int USCRIPT_ARABIC       =  2;  /* Arab */
    public static final int USCRIPT_ARMENIAN     =  3;  /* Armn */
    public static final int USCRIPT_BENGALI      =  4;  /* Beng */
    public static final int USCRIPT_BOPOMOFO     =  5;  /* Bopo */
    public static final int USCRIPT_CHEROKEE     =  6;  /* Cher */
    public static final int USCRIPT_COPTIC       =  7;  /* Qaac */
    public static final int USCRIPT_CYRILLIC     =  8;  /* Cyrl (Cyrs) */
    public static final int USCRIPT_DESERET      =  9;  /* Dsrt */
    public static final int USCRIPT_DEVANAGARI   = 10;  /* Deva */
    public static final int USCRIPT_ETHIOPIC     = 11;  /* Ethi */
    public static final int USCRIPT_GEORGIAN     = 12;  /* Geor (Geon; Geoa) */
    public static final int USCRIPT_GOTHIC       = 13;  /* Goth */
    public static final int USCRIPT_GREEK        = 14;  /* Grek */
    public static final int USCRIPT_GUJARATI     = 15;  /* Gujr */
    public static final int USCRIPT_GURMUKHI     = 16;  /* Guru */
    public static final int USCRIPT_HAN          = 17;  /* Hani */
    public static final int USCRIPT_HANGUL       = 18;  /* Hang */
    public static final int USCRIPT_HEBREW       = 19;  /* Hebr */
    public static final int USCRIPT_HIRAGANA     = 20;  /* Hira */
    public static final int USCRIPT_KANNADA      = 21;  /* Knda */
    public static final int USCRIPT_KATAKANA     = 22;  /* Kana */
    public static final int USCRIPT_KHMER        = 23;  /* Khmr */
    public static final int USCRIPT_LAO          = 24;  /* Laoo */
    public static final int USCRIPT_LATIN        = 25;  /* Latn (Latf; Latg) */
    public static final int USCRIPT_MALAYALAM    = 26;  /* Mlym */
    public static final int USCRIPT_MONGOLIAN    = 27;  /* Mong */
    public static final int USCRIPT_MYANMAR      = 28;  /* Mymr */
    public static final int USCRIPT_OGHAM        = 29;  /* Ogam */
    public static final int USCRIPT_OLD_ITALIC   = 30;  /* Ital */
    public static final int USCRIPT_ORIYA        = 31;  /* Orya */
    public static final int USCRIPT_RUNIC        = 32;  /* Runr */
    public static final int USCRIPT_SINHALA      = 33;  /* Sinh */
    public static final int USCRIPT_SYRIAC       = 34;  /* Syrc (Syrj; Syrn; Syre) */
    public static final int USCRIPT_TAMIL        = 35;  /* Taml */
    public static final int USCRIPT_TELUGU       = 36;  /* Telu */
    public static final int USCRIPT_THAANA       = 37;  /* Thaa */
    public static final int USCRIPT_THAI         = 38;  /* Thai */
    public static final int USCRIPT_TIBETAN      = 39;  /* Tibt */
    public static final int USCRIPT_UCAS         = 40;  /* Cans */
    public static final int USCRIPT_YI           = 41;  /* Yiii */
    public static final int USCRIPT_CODE_LIMIT   = 42; 
    
    private static final String scriptNames[]={
        "ARABIC",               /* USCRIPT_ARABIC     */
        "ARMENIAN",             /* USCRIPT_ARMENIAN   */
        "BENGALI",              /* USCRIPT_BENGALI    */
        "BOPOMOFO",             /* USCRIPT_BOPOMOFO   */
        "CANADIAN-ABORIGINAL",  /* USCRIPT_UCAS       */
        "CHEROKEE",             /* USCRIPT_CHEROKEE   */
        "CYRILLIC",             /* USCRIPT_CYRILLIC   */
        "DESERET",              /* USCRIPT_DESERET    */
        "DEVANAGARI",           /* USCRIPT_DEVANAGARI */
        "ETHIOPIC",             /* USCRIPT_ETHIOPIC   */
        "GEORGIAN",             /* USCRIPT_GEORGIAN   */
        "GOTHIC",               /* USCRIPT_GOTHIC     */
        "GREEK",                /* USCRIPT_GREEK      */
        "GUJARATI",             /* USCRIPT_GUJARATI   */
        "GURMUKHI",             /* USCRIPT_GURMUKHI   */
        "HAN",                  /* USCRIPT_HAN        */
        "HANGUL",               /* USCRIPT_HANGUL     */
        "HEBREW",               /* USCRIPT_HEBREW     */
        "HIRAGANA",             /* USCRIPT_HIRAGANA   */
        "INHERITED",            /* USCRIPT_INHERITED  */
        "KANNADA",              /* USCRIPT_KANNADA    */
        "KATAKANA",             /* USCRIPT_KATAKANA   */
        "KHMER",                /* USCRIPT_KHMER      */
        "LATIN",                /* USCRIPT_LATIN      */
        "MALAYALAM",            /* USCRIPT_MALAYALAM  */
        "MONGOLIAN",            /* USCRIPT_MONGOLIAN  */
        "MYANMAR",              /* USCRIPT_MYANMAR    */
        "OGHAM",                /* USCRIPT_OGHAM      */
        "OLD-ITALIC",           /* USCRIPT_OLD_ITALIC */
        "ORIYA",                /* USCRIPT_ORIYA      */
        "RUNIC",                /* USCRIPT_RUNIC      */
        "SINHALA",              /* USCRIPT_SINHALA    */
        "SYRIAC",               /* USCRIPT_SYRIAC     */
        "TAMIL",                /* USCRIPT_TAMIL      */
        "TELUGU",               /* USCRIPT_TELUGU     */
        "THAANA",               /* USCRIPT_THANA      */
        "THAI",                 /* USCRIPT_THAI       */
        "TIBETAN",              /* USCRIPT_TIBETAN    */
        "UCAS",                 /* USCRIPT_UCAS       */
        "YI",                   /* USCRIPT_YI         */
        
    };

    private static final String scriptAbbr[]= {
        "Arab",       /* USCRIPT_ARABIC     */
        "Armn",       /* USCRIPT_ARMENIAN   */
        "Beng",       /* USCRIPT_BENGALI    */
        "Bopo",       /* USCRIPT_BOPOMOFO   */
        "Cans",       /* USCRIPT_UCAS       */
        "Cher",       /* USCRIPT_CHEROKEE   */
        "Cyrl",       /* USCRIPT_CYRILLIC   */
       /* "Cyrs",  */ /* USCRIPT_CYRILLIC   */
        "Deva",       /* USCRIPT_DEVANAGARI */
        "Dsrt",       /* USCRIPT_DESERET    */
        "Ethi",       /* USCRIPT_ETHIOPIC   */
       /* "Geoa",  */ /* USCRIPT_GEORGIAN   */
       /* "Geon",  */ /* USCRIPT_GEORGIAN   */
        "Geor",       /* USCRIPT_GEORGIAN   */
        "Goth",       /* USCRIPT_GOTHIC     */
        "Grek",       /* USCRIPT_GREEK      */
        "Gujr",       /* USCRIPT_GUJARATI   */
        "Guru",       /* USCRIPT_GURMUKHI   */
        "Hang",       /* USCRIPT_HANGUL     */
        "Hani",       /* USCRIPT_HAN        */
        "Hebr",       /* USCRIPT_HEBREW     */
        "Hira",       /* USCRIPT_HIRAGANA   */
        "Ital",       /* USCRIPT_OLD_ITALIC */
        "Kana",       /* USCRIPT_KATAKANA   */
        "Khmr",       /* USCRIPT_KHMER      */
        "Knda",       /* USCRIPT_KANNADA    */
        "Lao",        /* USCRIPT_LAO        */
        /*"Laoo",  */ /* USCRIPT_LAO        */
        /*"Latf",  */ /* USCRIPT_LATIN      */
        /*"Latg",  */ /* USCRIPT_LATIN      */
        "Latn",       /* USCRIPT_LATIN      */
        "Mlym",       /* USCRIPT_MALAYALAM  */
        "Mong",       /* USCRIPT_MONGOLIAN  */
        "Mymr",       /* USCRIPT_MYANMAR    */
        "Ogam",       /* USCRIPT_OGHAM      */
        "Orya",       /* USCRIPT_ORIYA      */
        "Qaac",       /* USCRIPT_COPTIC     */
        "Qaai",       /* USCRIPT_INHERITED  */
        "Runr",       /* USCRIPT_RUNIC      */
        "Sinh",       /* USCRIPT_SINHALA    */
        "Syrc",       /* USCRIPT_SYRIAC     */
       /* "Syre",  */ /* USCRIPT_SYRIAC     */
       /* "Syrj",  */ /* USCRIPT_SYRIAC     */
       /* "Syrn",  */ /* USCRIPT_SYRIAC     */
        "Taml",       /* USCRIPT_TAMIL      */
        "Telu",       /* USCRIPT_TELUGU     */
        "Thaa",       /* USCRIPT_THANA      */
        "Thai",       /* USCRIPT_THAI       */
        "Tibt",       /* USCRIPT_TIBETAN    */
        "Yiii",       /* USCRIPT_YI         */
        "Zyyy",       /* USCRIPT_COMMON     */   
    };


    private static final int scriptNameCodes[]= {
         USCRIPT_ARABIC     ,
         USCRIPT_ARMENIAN   ,
         USCRIPT_BENGALI    ,
         USCRIPT_BOPOMOFO   ,
         USCRIPT_UCAS       ,
         USCRIPT_CHEROKEE   ,
         USCRIPT_CYRILLIC   ,
         USCRIPT_DESERET    ,
         USCRIPT_DEVANAGARI ,
         USCRIPT_ETHIOPIC   ,
         USCRIPT_GEORGIAN   ,
         USCRIPT_GOTHIC     ,
         USCRIPT_GREEK      ,
         USCRIPT_GUJARATI   ,
         USCRIPT_GURMUKHI   ,
         USCRIPT_HAN        ,
         USCRIPT_HANGUL     ,
         USCRIPT_HEBREW     ,
         USCRIPT_HIRAGANA   ,
         USCRIPT_INHERITED  ,
         USCRIPT_KANNADA    ,
         USCRIPT_KATAKANA   ,
         USCRIPT_KHMER      ,
         USCRIPT_LATIN      ,
         USCRIPT_MALAYALAM  ,
         USCRIPT_MONGOLIAN  ,
         USCRIPT_MYANMAR    ,
         USCRIPT_OGHAM      ,
         USCRIPT_OLD_ITALIC ,
         USCRIPT_ORIYA      ,
         USCRIPT_RUNIC      ,
         USCRIPT_SINHALA    ,
         USCRIPT_SYRIAC     ,
         USCRIPT_TAMIL      ,
         USCRIPT_TELUGU     ,
         USCRIPT_THAANA     ,
         USCRIPT_THAI       ,
         USCRIPT_TIBETAN    ,
         USCRIPT_UCAS       ,
         USCRIPT_YI
    };


    private static final int scriptAbbrCodes[] = {
        USCRIPT_ARABIC     ,
        USCRIPT_ARMENIAN   ,
        USCRIPT_BENGALI    ,
        USCRIPT_BOPOMOFO   ,
        USCRIPT_UCAS       ,
        USCRIPT_CHEROKEE   ,
        USCRIPT_CYRILLIC   ,
       /* USCRIPT_CYRILLIC   , */
        USCRIPT_DEVANAGARI ,
        USCRIPT_DESERET    ,
        USCRIPT_ETHIOPIC   ,
      /*  USCRIPT_GEORGIAN   , */
      /*  USCRIPT_GEORGIAN   , */
        USCRIPT_GEORGIAN   ,
        USCRIPT_GOTHIC     ,
        USCRIPT_GREEK      ,
        USCRIPT_GUJARATI   ,
        USCRIPT_GURMUKHI   ,
        USCRIPT_HANGUL     ,
        USCRIPT_HAN        ,
        USCRIPT_HEBREW     ,
        USCRIPT_HIRAGANA   ,
        USCRIPT_OLD_ITALIC ,
        USCRIPT_KATAKANA   ,
        USCRIPT_KHMER      ,
        USCRIPT_KANNADA    ,
        USCRIPT_LAO        ,
      /*  USCRIPT_LAO        , */
      /*  USCRIPT_LATIN      , */
      /*  USCRIPT_LATIN      , */
        USCRIPT_LATIN      ,
        USCRIPT_MALAYALAM  ,
        USCRIPT_MONGOLIAN  ,
        USCRIPT_MYANMAR    ,
        USCRIPT_OGHAM      ,
        USCRIPT_ORIYA      ,
        USCRIPT_COPTIC     ,
        USCRIPT_INHERITED  ,
        USCRIPT_RUNIC      ,
        USCRIPT_SINHALA    ,
        USCRIPT_SYRIAC     ,
      /*  USCRIPT_SYRIAC     , */
      /*  USCRIPT_SYRIAC     , */
        USCRIPT_SYRIAC     ,
        USCRIPT_TAMIL      ,
        USCRIPT_TELUGU     ,
        USCRIPT_THAANA     ,
        USCRIPT_THAI       ,
        USCRIPT_TIBETAN    ,
        USCRIPT_YI         ,
        USCRIPT_COMMON     , 
    };
    /**********************************************************
     *
     * WARNING: The below map is machine generated
     * by genscrpt after parsing Scripts.txt,
     * plese donot edit unless you know what you are doing
     *
     * TODO: Remove after merging script names to uprops.dat
     *
     **********************************************************
     */
    private static final class UScriptCodeMap {
        int  firstCodepoint;
        int  lastCodepoint;
        int  scriptCode;
        public UScriptCodeMap(int firstCp, int lastCp, int code){
            firstCodepoint = firstCp;
            lastCodepoint  = lastCp;
            scriptCode     = code ;
        }
    };
    
    private static final UScriptCodeMap[] scriptCodeIndex = {
         new UScriptCodeMap(0x00000000, 0x00000000, USCRIPT_COMMON) ,
         new UScriptCodeMap(0x00000041, 0x0000005A, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00000061, 0x0000007A, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000000AA, 0x000000AA, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000000B5, 0x000000B5, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x000000BA, 0x000000BA, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000000C0, 0x000000D6, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000000D8, 0x000000F6, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000000F8, 0x000001BA, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000001BB, 0x000001BB, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000001BC, 0x000001BF, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000001C0, 0x000001C3, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000001C4, 0x0000021F, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00000222, 0x00000233, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00000250, 0x000002AD, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000002B0, 0x000002B8, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000002E0, 0x000002E4, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00000300, 0x0000034E, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00000360, 0x00000362, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0000037A, 0x0000037A, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00000386, 0x00000386, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00000388, 0x0000038A, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x0000038C, 0x0000038C, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x0000038E, 0x000003A1, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x000003A3, 0x000003CE, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x000003D0, 0x000003D7, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x000003DA, 0x000003F5, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00000400, 0x00000481, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x00000483, 0x00000486, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x00000488, 0x00000489, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0000048C, 0x000004C4, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x000004C7, 0x000004C8, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x000004CB, 0x000004CC, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x000004D0, 0x000004F5, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x000004F8, 0x000004F9, USCRIPT_CYRILLIC ) ,
         new UScriptCodeMap(0x00000531, 0x00000556, USCRIPT_ARMENIAN ) ,
         new UScriptCodeMap(0x00000559, 0x00000559, USCRIPT_ARMENIAN ) ,
         new UScriptCodeMap(0x00000561, 0x00000587, USCRIPT_ARMENIAN ) ,
         new UScriptCodeMap(0x00000591, 0x000005A1, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005A3, 0x000005B9, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005BB, 0x000005BD, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005BF, 0x000005BF, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005C1, 0x000005C2, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005C4, 0x000005C4, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000005D0, 0x000005EA, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x000005F0, 0x000005F2, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x00000621, 0x0000063A, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x00000641, 0x0000064A, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000064B, 0x00000655, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00000670, 0x00000670, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00000671, 0x000006D3, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x000006D5, 0x000006D5, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x000006D6, 0x000006DC, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000006DD, 0x000006DE, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000006DF, 0x000006E4, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000006E5, 0x000006E6, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x000006E7, 0x000006E8, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000006EA, 0x000006ED, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000006FA, 0x000006FC, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x00000710, 0x00000710, USCRIPT_SYRIAC ) ,
         new UScriptCodeMap(0x00000711, 0x00000711, USCRIPT_SYRIAC ) ,
         new UScriptCodeMap(0x00000712, 0x0000072C, USCRIPT_SYRIAC ) ,
         new UScriptCodeMap(0x00000730, 0x0000074A, USCRIPT_SYRIAC ) ,
         new UScriptCodeMap(0x00000780, 0x000007A5, USCRIPT_THAANA ) ,
         new UScriptCodeMap(0x000007A6, 0x000007B0, USCRIPT_THAANA ) ,
         new UScriptCodeMap(0x00000901, 0x00000902, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000903, 0x00000903, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000905, 0x00000939, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x0000093C, 0x0000093C, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x0000093D, 0x0000093D, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x0000093E, 0x00000940, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000941, 0x00000948, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000949, 0x0000094C, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x0000094D, 0x0000094D, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000950, 0x00000950, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000951, 0x00000954, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000958, 0x00000961, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000962, 0x00000963, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000966, 0x0000096F, USCRIPT_DEVANAGARI ) ,
         new UScriptCodeMap(0x00000981, 0x00000981, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x00000985, 0x0000098C, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x0000098F, 0x00000990, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x00000993, 0x000009A8, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009AA, 0x000009B0, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009B2, 0x000009B2, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009B6, 0x000009B9, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009BC, 0x000009BC, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009BE, 0x000009C0, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009C1, 0x000009C4, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009C7, 0x000009C8, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009CB, 0x000009CC, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009CD, 0x000009CD, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009D7, 0x000009D7, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009DC, 0x000009DD, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009DF, 0x000009E1, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009E2, 0x000009E3, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009E6, 0x000009EF, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x000009F0, 0x000009F1, USCRIPT_BENGALI ) ,
         new UScriptCodeMap(0x00000A02, 0x00000A02, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A05, 0x00000A0A, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A0F, 0x00000A10, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A13, 0x00000A28, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A2A, 0x00000A30, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A32, 0x00000A33, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A35, 0x00000A36, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A38, 0x00000A39, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A3C, 0x00000A3C, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A3E, 0x00000A40, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A41, 0x00000A42, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A47, 0x00000A48, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A4B, 0x00000A4D, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A59, 0x00000A5C, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A5E, 0x00000A5E, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A66, 0x00000A6F, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A70, 0x00000A71, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A72, 0x00000A74, USCRIPT_GURMUKHI ) ,
         new UScriptCodeMap(0x00000A81, 0x00000A82, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000A83, 0x00000A83, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000A85, 0x00000A8B, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000A8D, 0x00000A8D, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000A8F, 0x00000A91, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000A93, 0x00000AA8, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AAA, 0x00000AB0, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AB2, 0x00000AB3, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AB5, 0x00000AB9, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000ABC, 0x00000ABC, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000ABD, 0x00000ABD, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000ABE, 0x00000AC0, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AC1, 0x00000AC5, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AC7, 0x00000AC8, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AC9, 0x00000AC9, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000ACB, 0x00000ACC, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000ACD, 0x00000ACD, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AD0, 0x00000AD0, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AE0, 0x00000AE0, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000AE6, 0x00000AEF, USCRIPT_GUJARATI ) ,
         new UScriptCodeMap(0x00000B01, 0x00000B01, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B02, 0x00000B03, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B05, 0x00000B0C, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B0F, 0x00000B10, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B13, 0x00000B28, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B2A, 0x00000B30, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B32, 0x00000B33, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B36, 0x00000B39, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B3C, 0x00000B3C, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B3D, 0x00000B3D, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B3E, 0x00000B3E, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B3F, 0x00000B3F, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B40, 0x00000B40, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B41, 0x00000B43, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B47, 0x00000B48, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B4B, 0x00000B4C, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B4D, 0x00000B4D, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B56, 0x00000B56, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B57, 0x00000B57, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B5C, 0x00000B5D, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B5F, 0x00000B61, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B66, 0x00000B6F, USCRIPT_ORIYA ) ,
         new UScriptCodeMap(0x00000B82, 0x00000B82, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B83, 0x00000B83, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B85, 0x00000B8A, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B8E, 0x00000B90, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B92, 0x00000B95, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B99, 0x00000B9A, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B9C, 0x00000B9C, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000B9E, 0x00000B9F, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BA3, 0x00000BA4, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BA8, 0x00000BAA, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BAE, 0x00000BB5, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BB7, 0x00000BB9, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BBE, 0x00000BBF, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BC0, 0x00000BC0, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BC1, 0x00000BC2, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BC6, 0x00000BC8, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BCA, 0x00000BCC, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BCD, 0x00000BCD, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BD7, 0x00000BD7, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BE7, 0x00000BEF, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000BF0, 0x00000BF2, USCRIPT_TAMIL ) ,
         new UScriptCodeMap(0x00000C01, 0x00000C03, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C05, 0x00000C0C, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C0E, 0x00000C10, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C12, 0x00000C28, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C2A, 0x00000C33, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C35, 0x00000C39, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C3E, 0x00000C40, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C41, 0x00000C44, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C46, 0x00000C48, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C4A, 0x00000C4D, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C55, 0x00000C56, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C60, 0x00000C61, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C66, 0x00000C6F, USCRIPT_TELUGU ) ,
         new UScriptCodeMap(0x00000C82, 0x00000C83, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000C85, 0x00000C8C, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000C8E, 0x00000C90, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000C92, 0x00000CA8, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CAA, 0x00000CB3, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CB5, 0x00000CB9, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CBE, 0x00000CBE, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CBF, 0x00000CBF, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CC0, 0x00000CC4, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CC6, 0x00000CC6, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CC7, 0x00000CC8, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CCA, 0x00000CCB, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CCC, 0x00000CCD, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CD5, 0x00000CD6, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CDE, 0x00000CDE, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CE0, 0x00000CE1, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000CE6, 0x00000CEF, USCRIPT_KANNADA ) ,
         new UScriptCodeMap(0x00000D02, 0x00000D03, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D05, 0x00000D0C, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D0E, 0x00000D10, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D12, 0x00000D28, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D2A, 0x00000D39, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D3E, 0x00000D40, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D41, 0x00000D43, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D46, 0x00000D48, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D4A, 0x00000D4C, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D4D, 0x00000D4D, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D57, 0x00000D57, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D60, 0x00000D61, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D66, 0x00000D6F, USCRIPT_MALAYALAM ) ,
         new UScriptCodeMap(0x00000D82, 0x00000D83, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000D85, 0x00000D96, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000D9A, 0x00000DB1, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DB3, 0x00000DBB, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DBD, 0x00000DBD, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DC0, 0x00000DC6, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DCA, 0x00000DCA, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DCF, 0x00000DD1, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DD2, 0x00000DD4, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DD6, 0x00000DD6, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DD8, 0x00000DDF, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000DF2, 0x00000DF3, USCRIPT_SINHALA ) ,
         new UScriptCodeMap(0x00000E01, 0x00000E30, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E31, 0x00000E31, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E32, 0x00000E33, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E34, 0x00000E3A, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E40, 0x00000E45, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E46, 0x00000E46, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E47, 0x00000E4E, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E50, 0x00000E59, USCRIPT_THAI ) ,
         new UScriptCodeMap(0x00000E81, 0x00000E82, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E84, 0x00000E84, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E87, 0x00000E88, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E8A, 0x00000E8A, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E8D, 0x00000E8D, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E94, 0x00000E97, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000E99, 0x00000E9F, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EA1, 0x00000EA3, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EA5, 0x00000EA5, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EA7, 0x00000EA7, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EAA, 0x00000EAB, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EAD, 0x00000EB0, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EB1, 0x00000EB1, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EB2, 0x00000EB3, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EB4, 0x00000EB9, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EBB, 0x00000EBC, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EBD, 0x00000EBD, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EC0, 0x00000EC4, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EC6, 0x00000EC6, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EC8, 0x00000ECD, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000ED0, 0x00000ED9, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000EDC, 0x00000EDD, USCRIPT_LAO ) ,
         new UScriptCodeMap(0x00000F00, 0x00000F00, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F18, 0x00000F19, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F20, 0x00000F29, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F2A, 0x00000F33, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F35, 0x00000F35, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F37, 0x00000F37, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F39, 0x00000F39, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F40, 0x00000F47, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F49, 0x00000F6A, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F71, 0x00000F7E, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F7F, 0x00000F7F, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F80, 0x00000F84, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F86, 0x00000F87, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F88, 0x00000F8B, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F90, 0x00000F97, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000F99, 0x00000FBC, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00000FC6, 0x00000FC6, USCRIPT_TIBETAN ) ,
         new UScriptCodeMap(0x00001000, 0x00001021, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001023, 0x00001027, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001029, 0x0000102A, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x0000102C, 0x0000102C, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x0000102D, 0x00001030, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001031, 0x00001031, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001032, 0x00001032, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001036, 0x00001037, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001038, 0x00001038, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001039, 0x00001039, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001040, 0x00001049, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001050, 0x00001055, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001056, 0x00001057, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x00001058, 0x00001059, USCRIPT_MYANMAR ) ,
         new UScriptCodeMap(0x000010A0, 0x000010C5, USCRIPT_GEORGIAN ) ,
         new UScriptCodeMap(0x000010D0, 0x000010F6, USCRIPT_GEORGIAN ) ,
         new UScriptCodeMap(0x00001100, 0x00001159, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000115F, 0x000011A2, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x000011A8, 0x000011F9, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x00001200, 0x00001206, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001208, 0x00001246, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001248, 0x00001248, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x0000124A, 0x0000124D, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001250, 0x00001256, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001258, 0x00001258, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x0000125A, 0x0000125D, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001260, 0x00001286, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001288, 0x00001288, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x0000128A, 0x0000128D, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001290, 0x000012AE, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012B0, 0x000012B0, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012B2, 0x000012B5, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012B8, 0x000012BE, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012C0, 0x000012C0, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012C2, 0x000012C5, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012C8, 0x000012CE, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012D0, 0x000012D6, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012D8, 0x000012EE, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000012F0, 0x0000130E, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001310, 0x00001310, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001312, 0x00001315, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001318, 0x0000131E, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001320, 0x00001346, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001348, 0x0000135A, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001369, 0x00001371, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x00001372, 0x0000137C, USCRIPT_ETHIOPIC ) ,
         new UScriptCodeMap(0x000013A0, 0x000013F4, USCRIPT_CHEROKEE ) ,
         new UScriptCodeMap(0x00001401, 0x0000166C, USCRIPT_UCAS ) ,
         new UScriptCodeMap(0x0000166F, 0x00001676, USCRIPT_UCAS ) ,
         new UScriptCodeMap(0x00001681, 0x0000169A, USCRIPT_OGHAM ) ,
         new UScriptCodeMap(0x000016A0, 0x000016EA, USCRIPT_RUNIC ) ,
         new UScriptCodeMap(0x000016EE, 0x000016F0, USCRIPT_RUNIC ) ,
         new UScriptCodeMap(0x00001780, 0x000017B3, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017B4, 0x000017B6, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017B7, 0x000017BD, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017BE, 0x000017C5, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017C6, 0x000017C6, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017C7, 0x000017C8, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017C9, 0x000017D3, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x000017E0, 0x000017E9, USCRIPT_KHMER ) ,
         new UScriptCodeMap(0x00001810, 0x00001819, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x00001820, 0x00001842, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x00001843, 0x00001843, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x00001844, 0x00001877, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x00001880, 0x000018A8, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x000018A9, 0x000018A9, USCRIPT_MONGOLIAN ) ,
         new UScriptCodeMap(0x00001E00, 0x00001E9B, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00001EA0, 0x00001EF9, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00001F00, 0x00001F15, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F18, 0x00001F1D, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F20, 0x00001F45, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F48, 0x00001F4D, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F50, 0x00001F57, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F59, 0x00001F59, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F5B, 0x00001F5B, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F5D, 0x00001F5D, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F5F, 0x00001F7D, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001F80, 0x00001FB4, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FB6, 0x00001FBC, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FBE, 0x00001FBE, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FC2, 0x00001FC4, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FC6, 0x00001FCC, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FD0, 0x00001FD3, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FD6, 0x00001FDB, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FE0, 0x00001FEC, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FF2, 0x00001FF4, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x00001FF6, 0x00001FFC, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x0000207F, 0x0000207F, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x000020D0, 0x000020DC, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000020DD, 0x000020E0, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000020E1, 0x000020E1, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x000020E2, 0x000020E3, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00002126, 0x00002126, USCRIPT_GREEK ) ,
         new UScriptCodeMap(0x0000212A, 0x0000212B, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x00002E80, 0x00002E99, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00002E9B, 0x00002EF3, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00002F00, 0x00002FD5, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00003005, 0x00003005, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00003007, 0x00003007, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00003021, 0x00003029, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x0000302A, 0x0000302F, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00003038, 0x0000303A, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00003041, 0x00003094, USCRIPT_HIRAGANA ) ,
         new UScriptCodeMap(0x00003099, 0x0000309A, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0000309D, 0x0000309E, USCRIPT_HIRAGANA ) ,
         new UScriptCodeMap(0x000030A1, 0x000030FA, USCRIPT_KATAKANA ) ,
         new UScriptCodeMap(0x000030FD, 0x000030FE, USCRIPT_KATAKANA ) ,
         new UScriptCodeMap(0x00003105, 0x0000312C, USCRIPT_BOPOMOFO ) ,
         new UScriptCodeMap(0x00003131, 0x0000318E, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x000031A0, 0x000031B7, USCRIPT_BOPOMOFO ) ,
         new UScriptCodeMap(0x00003400, 0x00004DB5, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x00004E00, 0x00009FA5, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x0000A000, 0x0000A48C, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000A490, 0x0000A4A1, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000A4A4, 0x0000A4B3, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000A4B5, 0x0000A4C0, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000A4C2, 0x0000A4C4, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000A4C6, 0x0000A4C6, USCRIPT_YI ) ,
         new UScriptCodeMap(0x0000AC00, 0x0000D7A3, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000F900, 0x0000FA2D, USCRIPT_HAN ) ,
         new UScriptCodeMap(0x0000FB00, 0x0000FB06, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x0000FB13, 0x0000FB17, USCRIPT_ARMENIAN ) ,
         new UScriptCodeMap(0x0000FB1D, 0x0000FB1D, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB1E, 0x0000FB1E, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0000FB1F, 0x0000FB28, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB2A, 0x0000FB36, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB38, 0x0000FB3C, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB3E, 0x0000FB3E, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB40, 0x0000FB41, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB43, 0x0000FB44, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB46, 0x0000FB4F, USCRIPT_HEBREW ) ,
         new UScriptCodeMap(0x0000FB50, 0x0000FBB1, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FBD3, 0x0000FD3D, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FD50, 0x0000FD8F, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FD92, 0x0000FDC7, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FDF0, 0x0000FDFB, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FE20, 0x0000FE23, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0000FE70, 0x0000FE72, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FE74, 0x0000FE74, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FE76, 0x0000FEFC, USCRIPT_ARABIC ) ,
         new UScriptCodeMap(0x0000FF21, 0x0000FF3A, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x0000FF41, 0x0000FF5A, USCRIPT_LATIN ) ,
         new UScriptCodeMap(0x0000FF66, 0x0000FF6F, USCRIPT_KATAKANA ) ,
         new UScriptCodeMap(0x0000FF71, 0x0000FF9D, USCRIPT_KATAKANA ) ,
         new UScriptCodeMap(0x0000FFA0, 0x0000FFBE, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000FFC2, 0x0000FFC7, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000FFCA, 0x0000FFCF, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000FFD2, 0x0000FFD7, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x0000FFDA, 0x0000FFDC, USCRIPT_HANGUL ) ,
         new UScriptCodeMap(0x00010300, 0x0001031E, USCRIPT_OLD_ITALIC ) ,
         new UScriptCodeMap(0x00010330, 0x00010349, USCRIPT_GOTHIC ) ,
         new UScriptCodeMap(0x0001034A, 0x0001034A, USCRIPT_GOTHIC ) ,
         new UScriptCodeMap(0x00010400, 0x00010425, USCRIPT_DESERET ) ,
         new UScriptCodeMap(0x00010428, 0x0001044D, USCRIPT_DESERET ) ,
         new UScriptCodeMap(0x0001D167, 0x0001D169, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0001D17B, 0x0001D182, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0001D185, 0x0001D18B, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x0001D1AA, 0x0001D1AD, USCRIPT_INHERITED ) ,
         new UScriptCodeMap(0x00020000, 0x0002A6D6, USCRIPT_HAN ) ,
    };
    

    /* binary search the string array */
    private static int findStringIndex(String[] sortedArr, String target){
        int size = sortedArr.length;
        int left, middle, right,rc;
        left =0;
        right= size-1;
        
        while(left <= right){
            middle = (left+right)/2;
            rc=sortedArr[middle].compareTo(target);
            if(rc<0){
                left = middle+1;
            }else if(rc >0){
                right = middle -1;
            }else{
                return middle;
            }
        }
        return -1;
    }

    /* linearly search the array and return the index */
    private static int findCodeIndex(int[] unsorted, int target){
        int size = unsorted.length;
        int i=0;
        while(i<size){
            if(target == unsorted[i]){
                return i;
            }
            i++;
        }
        return -1;
    }

    private static final String localeScript = "com.ibm.text.resources.LocaleScript";
     
    private static int findCodeFromLocale(String localeName)
            throws MissingResourceException{
                
        int code = USCRIPT_INVALID_CODE;
        ResourceBundle resB = ResourceBundle.getBundle(localeScript);
        String temp = localeName;
        do{
            try{
                String[] scriptArray = resB.getStringArray(temp);
                int strIndex = findStringIndex(scriptNames,scriptArray[0].toUpperCase());
                if(strIndex>=0 && strIndex < scriptAbbr.length){ 
                    code =  scriptNameCodes[strIndex];
                }
            }catch(MissingResourceException e){
                /* handle fallback */
                int index = temp.indexOf('_');
                if(index >0){
                    temp = temp.substring(0,index);
                }else{
                    temp = "root";
                }
                continue;
            }
        }while(code==USCRIPT_INVALID_CODE);
        
        return code;
     }

    /* binary search the string array */
    private static int findScriptCode(int codepoint){
        int left, middle, right,rc;
        left =0;
        
        right= scriptCodeIndex.length-1;

        while(left <= right){
            middle = (left+right)/2;
            /* check if the codepoint is the valid range */
            if(scriptCodeIndex[middle].firstCodepoint<= codepoint  && 
               scriptCodeIndex[middle].lastCodepoint>= codepoint){
                rc = 0;
            }else if(codepoint> scriptCodeIndex[middle].lastCodepoint){
                rc =-1;
            }else {
                rc = 1;
            }

            if(rc<0){
                left = middle+1;
            }else if(rc >0){
                right = middle -1;
            }else{
                return  scriptCodeIndex[middle].scriptCode;
            }
        }
        return -1;
    }
    
    /**
    * Gets a script code associated with the given locale or ISO 15924 abbreviation or name. 
    * Returns USCRIPT_MALAYAM given "Malayam" OR "Mlym".
    * Returns USCRIPT_LATIN given "en" OR "en_US" 
    * @param nameOrAbbrOrLocale name of the script or ISO 15924 code or locale
    * @return The script code 
    * @exception IllegalArgumentException, MissingResourceException
    * @draft
    */
    public static final int getCode(String nameOrAbbrOrLocale)
        throws IllegalArgumentException, MissingResourceException{
            
        int code = USCRIPT_INVALID_CODE;
        int strIndex=0;
        
        /* try the Names array first */
        strIndex = findStringIndex(scriptNames, nameOrAbbrOrLocale.toUpperCase());
        
        if(strIndex>=0 && strIndex < scriptNames.length){ 
            code =  scriptNameCodes[strIndex];
        }
        /* we did not find in names array so try abbr array*/
        if(code == USCRIPT_INVALID_CODE){
            strIndex = findStringIndex(scriptAbbr, nameOrAbbrOrLocale);
            if(strIndex>=0 && strIndex < scriptAbbr.length){ 
                code =  scriptAbbrCodes[strIndex];
            }
        }
        /* we still haven't found it try locale */
        if(code==USCRIPT_INVALID_CODE){
            //TODO: Confirm with Alan 
            code = findCodeFromLocale(nameOrAbbrOrLocale);
        }
        return code;
    }
    /** 
     * Gets the script code associated with the given codepoint.
     * Returns UScript.USCRIPT_MALAYAM given 0x0D02 
     * @param codepoint UChar32 codepoint
     * @param err the error status code.
     * @return The script code 
     * @exception IllegalArgumentException
     * @draft
     */
    public static final int getScript(int codepoint)
        throws IllegalArgumentException{
        if (codepoint >= UCharacter.MIN_VALUE & codepoint <= UCharacter.MAX_VALUE) {
            return findScriptCode(codepoint);
        }else{
            throw new IllegalArgumentException();
        } 
    }
    
    /**
     * Gets a script name associated with the given script code. 
     * Returns  "Malayam" given USCRIPT_MALAYAM
     * @param scriptCode int script code
     * @return script name as a string in full as given in TR#24
     * @exception IllegalArgumentException
     * @draft
     */
    public static final String getName(int scriptCode)
            throws IllegalArgumentException{
        int index = -1;
        if(scriptCode > USCRIPT_CODE_LIMIT){
            throw new IllegalArgumentException(Integer.toString(scriptCode));
        }
        index = findCodeIndex(scriptNameCodes,scriptCode);
        if(index >=0){
            return scriptNames[index];
        }else{
            throw new IllegalArgumentException(Integer.toString(scriptCode));
        }
    }
    
    /**
     * Gets a script name associated with the given script code. 
     * Returns  "Mlym" given USCRIPT_MALAYAM
     * @param scriptCode int script code 
     * @return script abbreviated name as a string  as given in TR#24
     * @exception IllegalArgumentException
     * @draft
     */
    public static final String getShortName(int scriptCode)
            throws IllegalArgumentException{
        int index = -1;
        if(scriptCode > USCRIPT_CODE_LIMIT){
            throw new IllegalArgumentException(Integer.toString(scriptCode));
        }
        index = findCodeIndex(scriptAbbrCodes,scriptCode);
        if(index >=0){
            return scriptAbbr[index];
        }else{
            throw new IllegalArgumentException(Integer.toString(scriptCode));
        }
    }
}

