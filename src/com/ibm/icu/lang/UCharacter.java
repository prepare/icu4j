/**
*******************************************************************************
* Copyright (C) 1996-2001, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/lang/UCharacter.java,v $ 
* $Date: 2002/11/22 22:53:13 $ 
* $Revision: 1.53 $
*
*******************************************************************************
*/

package com.ibm.icu.lang;

import java.util.Locale;
import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.util.RangeValueIterator;
import com.ibm.icu.util.ValueIterator;
import com.ibm.icu.util.VersionInfo;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.impl.NormalizerImpl;
import com.ibm.icu.impl.UCharacterUtility;
import com.ibm.icu.impl.UCharacterName;
import com.ibm.icu.impl.UCharacterNameChoice;
import com.ibm.icu.impl.UPropertyAliases;

/**
* <p>
* The UCharacter class provides extensions to the 
* <a href=http://java.sun.com/j2se/1.3/docs/api/java/lang/Character.html>
* java.lang.Character</a> class. These extensions provide support for 
* Unicode 3.1 properties and together with the <a href=UTF16.html>UTF16</a> 
* class, provide support for supplementary characters (those with code 
* points above U+FFFF).
* </p>
* <p>
* Code points are represented in these API using ints. While it would be 
* more convenient in Java to have a separate primitive datatype for them, 
* ints suffice in the meantime.
* </p>
* <p>
* To use this class please add the jar file name icu4j.jar to the 
* class path, since it contains data files which supply the information used 
* by this file.<br>
* E.g. In Windows <br>
* <code>set CLASSPATH=%CLASSPATH%;$JAR_FILE_PATH/ucharacter.jar</code>.<br>
* Otherwise, another method would be to copy the files uprops.dat and 
* unames.icu from the icu4j source subdirectory 
* <i>$ICU4J_SRC/src/com.ibm.icu.impl.data</i> to your class directory 
* <i>$ICU4J_CLASS/com.ibm.icu.impl.data</i>.
* </p>
* <p>
* Aside from the additions for UTF-16 support, and the updated Unicode 3.1
* properties, the main differences between UCharacter and Character are:
* <ul>
* <li> UCharacter is not designed to be a char wrapper and does not have 
*      APIs to which involves management of that single char.<br>
*      These include: 
*      <ul>
*        <li> char charValue(), 
*        <li> int compareTo(java.lang.Character, java.lang.Character), etc.
*      </ul>
* <li> UCharacter does not include Character APIs that are deprecated, not 
*      does it include the Java-specific character information, such as 
*      boolean isJavaIdentifierPart(char ch).
* <li> Character maps characters 'A' - 'Z' and 'a' - 'z' to the numeric 
*      values '10' - '35'. UCharacter also does this in digit and
*      getNumericValue, to adhere to the java semantics of these
*      methods.  New methods unicodeDigit, and
*      getUnicodeNumericValue do not treat the above code points 
*      as having numeric values.  This is a semantic change from ICU4J 1.3.1.
* </ul>
* <p>
* Further detail differences can be determined from the program 
*        <a href = http://oss.software.ibm.com/developerworks/opensource/cvs/icu4j/~checkout~/icu4j/src/com/ibm/icu/dev/test/lang/UCharacterCompare.java>
*        com.ibm.icu.dev.test.lang.UCharacterCompare</a>
* </p>
* <p>
* This class is not subclassable
* </p>
* @author Syn Wee Quek
* @since oct 06 2000
* @see com.ibm.icu.lang.UCharacterCategory
* @see com.ibm.icu.lang.UCharacterDirection
*/

/*
 * notes: 
 * 1) forDigit is not provided since there is no difference between the 
 * icu4c version and the jdk version
 */
 
public final class UCharacter
{ 
    // public inner classes ----------------------------------------------
      
    /**
     * A family of character subsets representing the character blocks in the 
     * Unicode specification, generated from Unicode Data file Blocks.txt. 
     * Character blocks generally define characters used for a specific script 
     * or purpose. A character is contained by at most one Unicode block. 
     * @draft ICU 2.4
     */
    public static final class UnicodeBlock extends Character.Subset 
    {
        // blocks objects ---------------------------------------------------
        
        /** 
         * @draft ICU 2.4
         */
        public static final UnicodeBlock BASIC_LATIN 
                                        = new UnicodeBlock("BASIC_LATIN", 1);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LATIN_1_SUPPLEMENT 
                                   = new UnicodeBlock("LATIN_1_SUPPLEMENT", 2);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LATIN_EXTENDED_A
                                    = new UnicodeBlock("LATIN_EXTENDED_A", 3);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LATIN_EXTENDED_B 
                                    = new UnicodeBlock("LATIN_EXTENDED_B", 4);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock IPA_EXTENSIONS 
                                    = new UnicodeBlock("IPA_EXTENSIONS", 5);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS 
                           = new UnicodeBlock("SPACING_MODIFIER_LETTERS", 6);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS 
                           = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 7);
        /**
         * Unicode 3.2 renames this block to "Greek and Coptic".
         * @draft ICU 2.4
         */
        public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", 8);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CYRILLIC 
                                            = new UnicodeBlock("CYRILLIC", 9);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ARMENIAN 
                                            = new UnicodeBlock("ARMENIAN", 10);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HEBREW 
                                            = new UnicodeBlock("HEBREW", 11);  
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ARABIC
                                            = new UnicodeBlock("ARABIC", 12);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SYRIAC 
                                            = new UnicodeBlock("SYRIAC", 13);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock THAANA 
                                            = new UnicodeBlock("THAANA", 14);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock DEVANAGARI 
                                        = new UnicodeBlock("DEVANAGARI", 15);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BENGALI 
                                            = new UnicodeBlock("BENGALI", 16);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GURMUKHI 
                                            = new UnicodeBlock("GURMUKHI", 17);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GUJARATI 
                                            = new UnicodeBlock("GUJARATI", 18);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA", 19);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL", 20);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TELUGU 
                                            = new UnicodeBlock("TELUGU", 21);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KANNADA 
                                            = new UnicodeBlock("KANNADA", 22);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MALAYALAM 
                                            = new UnicodeBlock("MALAYALAM", 23);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SINHALA 
                                            = new UnicodeBlock("SINHALA", 24);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock THAI = new UnicodeBlock("THAI", 25);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LAO = new UnicodeBlock("LAO", 26);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TIBETAN 
                                            = new UnicodeBlock("TIBETAN", 27);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MYANMAR 
                                            = new UnicodeBlock("MYANMAR", 28);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GEORGIAN 
                                            = new UnicodeBlock("GEORGIAN", 29);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HANGUL_JAMO 
                                        = new UnicodeBlock("HANGUL_JAMO", 30);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ETHIOPIC 
                                        = new UnicodeBlock("ETHIOPIC", 31);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CHEROKEE 
                                        = new UnicodeBlock("CHEROKEE", 32);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS 
               = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 33);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 34);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC", 35);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER", 36);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MONGOLIAN 
                                            = new UnicodeBlock("MONGOLIAN", 37);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL 
                            = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 38);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GREEK_EXTENDED 
                                    = new UnicodeBlock("GREEK_EXTENDED", 39);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GENERAL_PUNCTUATION 
                                = new UnicodeBlock("GENERAL_PUNCTUATION", 40);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS 
                        = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 41);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CURRENCY_SYMBOLS 
                                    = new UnicodeBlock("CURRENCY_SYMBOLS", 42);
        /**
         * Unicode 3.2 renames this block to "Combining Diacritical Marks for 
         * Symbols".
         * @draft ICU 2.4
         */
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS 
                        = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 43);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LETTERLIKE_SYMBOLS 
                                = new UnicodeBlock("LETTERLIKE_SYMBOLS", 44);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock NUMBER_FORMS 
                                        = new UnicodeBlock("NUMBER_FORMS", 45);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ARROWS 
                                            = new UnicodeBlock("ARROWS", 46);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MATHEMATICAL_OPERATORS 
                            = new UnicodeBlock("MATHEMATICAL_OPERATORS", 47);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL 
                            = new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 48);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CONTROL_PICTURES 
                                    = new UnicodeBlock("CONTROL_PICTURES", 49);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION 
                        = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 50);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS 
                            = new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 51);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BOX_DRAWING 
                                        = new UnicodeBlock("BOX_DRAWING", 52);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BLOCK_ELEMENTS 
                                    = new UnicodeBlock("BLOCK_ELEMENTS", 53);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GEOMETRIC_SHAPES 
                                    = new UnicodeBlock("GEOMETRIC_SHAPES", 54);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS 
                                = new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 55);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock DINGBATS 
                                            = new UnicodeBlock("DINGBATS", 56);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BRAILLE_PATTERNS 
                                    = new UnicodeBlock("BRAILLE_PATTERNS", 57);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT 
                            = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 58);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KANGXI_RADICALS 
                                    = new UnicodeBlock("KANGXI_RADICALS", 59);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS 
                = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 60);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION 
                        = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 61);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HIRAGANA 
                                            = new UnicodeBlock("HIRAGANA", 62);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KATAKANA 
                                            = new UnicodeBlock("KATAKANA", 63);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BOPOMOFO 
                                            = new UnicodeBlock("BOPOMOFO", 64);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO 
                            = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 65);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KANBUN 
                                            = new UnicodeBlock("KANBUN", 66);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BOPOMOFO_EXTENDED 
                                = new UnicodeBlock("BOPOMOFO_EXTENDED", 67);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS 
                    = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 68);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_COMPATIBILITY 
                                = new UnicodeBlock("CJK_COMPATIBILITY", 69);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A 
                = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 70);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS 
                            = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 71);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock YI_SYLLABLES 
                                        = new UnicodeBlock("YI_SYLLABLES", 72);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock YI_RADICALS 
                                        = new UnicodeBlock("YI_RADICALS", 73);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HANGUL_SYLLABLES 
                                    = new UnicodeBlock("HANGUL_SYLLABLES", 74);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HIGH_SURROGATES 
                                    = new UnicodeBlock("HIGH_SURROGATES", 75);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES 
                        = new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", 76);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock LOW_SURROGATES 
                                    = new UnicodeBlock("LOW_SURROGATES", 77);
        /**
         * Same as public static final int PRIVATE_USE.
         * Until Unicode 3.1.1; the corresponding block name was "Private Use";
         * and multiple code point ranges had this block.
         * Unicode 3.2 renames the block for the BMP PUA to "Private Use Area" 
         * and adds separate blocks for the supplementary PUAs.
         * @draft ICU 2.4
         */
        public static final UnicodeBlock PRIVATE_USE_AREA 
                                = new UnicodeBlock("PRIVATE_USE_AREA",  78);
        /**
         * Same as public static final int PRIVATE_USE_AREA.
         * Until Unicode 3.1.1; the corresponding block name was "Private Use";
         * and multiple code point ranges had this block.
         * Unicode 3.2 renames the block for the BMP PUA to "Private Use Area" 
         * and adds separate blocks for the supplementary PUAs.
         * @draft ICU 2.4
         */
        public static final UnicodeBlock PRIVATE_USE = PRIVATE_USE_AREA;
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS 
                       = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 79);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS 
                        = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 80);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A 
                        = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 81);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock COMBINING_HALF_MARKS 
                            = new UnicodeBlock("COMBINING_HALF_MARKS", 82);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS 
                            = new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 83);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SMALL_FORM_VARIANTS 
                                = new UnicodeBlock("SMALL_FORM_VARIANTS", 84);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B 
                        = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 85);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SPECIALS 
                                            = new UnicodeBlock("SPECIALS", 86);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS 
                        = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 87);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock OLD_ITALIC 
                                        = new UnicodeBlock("OLD_ITALIC", 88);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock GOTHIC 
                                            = new UnicodeBlock("GOTHIC", 89);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock DESERET 
                                            = new UnicodeBlock("DESERET", 90);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS 
                        = new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", 91);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MUSICAL_SYMBOLS 
                                    = new UnicodeBlock("MUSICAL_SYMBOLS", 92);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS 
                = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 93);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B  
                = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 94);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock 
            CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT 
            = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", 95);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TAGS = new UnicodeBlock("TAGS", 96);
    
        // New blocks in Unicode 3.2
    
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY 
                            = new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", 97);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TAGALOG 
                                            = new UnicodeBlock("TAGALOG", 98);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock HANUNOO 
                                            = new UnicodeBlock("HANUNOO", 99);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock BUHID = new UnicodeBlock("BUHID", 100);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock TAGBANWA 
                                            = new UnicodeBlock("TAGBANWA", 101);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A 
                = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 102);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A 
                            = new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", 103);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B 
                            = new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", 104);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B 
                = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 105);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS 
                = new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 106);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS 
                    = new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", 107);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock VARIATION_SELECTORS 
                                = new UnicodeBlock("VARIATION_SELECTORS", 108);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A 
                    = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 109);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B 
                    = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 110);
        /** 
         * @draft ICU 2.4 
         */
        public static final UnicodeBlock INVALID_CODE 
                                        = new UnicodeBlock("INVALID_CODE", -1);
           
        // block id corresponding to icu4c -----------------------------------
           
        /** 
         * @draft ICU 2.4 
         */
        public static final int INVALID_CODE_ID = -1;                          
        /** 
         * @draft ICU 2.4
         */
        public static final int BASIC_LATIN_ID = 1;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LATIN_1_SUPPLEMENT_ID = 2;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LATIN_EXTENDED_A_ID = 3;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LATIN_EXTENDED_B_ID = 4;
        /** 
         * @draft ICU 2.4 
         */
        public static final int IPA_EXTENSIONS_ID = 5;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SPACING_MODIFIER_LETTERS_ID = 6;
        /** 
         * @draft ICU 2.4 
         */
        public static final int COMBINING_DIACRITICAL_MARKS_ID = 7;
        /**
         * Unicode 3.2 renames this block to "Greek and Coptic".
         * @draft ICU 2.4
         */
        public static final int GREEK_ID = 8;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CYRILLIC_ID = 9;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ARMENIAN_ID = 10;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HEBREW_ID = 11;  
        /** 
         * @draft ICU 2.4 
         */
        public static final int ARABIC_ID = 12;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SYRIAC_ID = 13;
        /** 
         * @draft ICU 2.4 
         */
        public static final int THAANA_ID = 14;
        /** 
         * @draft ICU 2.4 
         */
        public static final int DEVANAGARI_ID = 15;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BENGALI_ID = 16;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GURMUKHI_ID = 17;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GUJARATI_ID = 18;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ORIYA_ID = 19;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TAMIL_ID = 20;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TELUGU_ID = 21;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KANNADA_ID = 22;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MALAYALAM_ID = 23;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SINHALA_ID = 24;
        /** 
         * @draft ICU 2.4 
         */
        public static final int THAI_ID = 25;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LAO_ID = 26;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TIBETAN_ID = 27;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MYANMAR_ID = 28;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GEORGIAN_ID = 29;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HANGUL_JAMO_ID = 30;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ETHIOPIC_ID = 31;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CHEROKEE_ID = 32;
        /** 
         * @draft ICU 2.4 
         */
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_ID = 33;
        /** 
         * @draft ICU 2.4 
         */
        public static final int OGHAM_ID = 34;
        /** 
         * @draft ICU 2.4 
         */
        public static final int RUNIC_ID = 35;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KHMER_ID = 36;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MONGOLIAN_ID = 37;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LATIN_EXTENDED_ADDITIONAL_ID = 38;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GREEK_EXTENDED_ID = 39;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GENERAL_PUNCTUATION_ID = 40;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPERSCRIPTS_AND_SUBSCRIPTS_ID = 41;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CURRENCY_SYMBOLS_ID = 42;
        /**
         * Unicode 3.2 renames this block to "Combining Diacritical Marks for 
         * Symbols".
         * @draft ICU 2.4
         */
        public static final int COMBINING_MARKS_FOR_SYMBOLS_ID = 43;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LETTERLIKE_SYMBOLS_ID = 44;
        /** 
         * @draft ICU 2.4 
         */
        public static final int NUMBER_FORMS_ID = 45;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ARROWS_ID = 46;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MATHEMATICAL_OPERATORS_ID = 47;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MISCELLANEOUS_TECHNICAL_ID = 48;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CONTROL_PICTURES_ID = 49;
        /** 
         * @draft ICU 2.4 
         */
        public static final int OPTICAL_CHARACTER_RECOGNITION_ID = 50;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ENCLOSED_ALPHANUMERICS_ID = 51;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BOX_DRAWING_ID = 52;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BLOCK_ELEMENTS_ID = 53;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GEOMETRIC_SHAPES_ID = 54;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MISCELLANEOUS_SYMBOLS_ID = 55;
        /** 
         * @draft ICU 2.4 
         */
        public static final int DINGBATS_ID = 56;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BRAILLE_PATTERNS_ID = 57;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_RADICALS_SUPPLEMENT_ID = 58;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KANGXI_RADICALS_ID = 59;
        /** 
         * @draft ICU 2.4 
         */
        public static final int IDEOGRAPHIC_DESCRIPTION_CHARACTERS_ID = 60;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_SYMBOLS_AND_PUNCTUATION_ID = 61;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HIRAGANA_ID = 62;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KATAKANA_ID = 63;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BOPOMOFO_ID = 64;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HANGUL_COMPATIBILITY_JAMO_ID = 65;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KANBUN_ID = 66;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BOPOMOFO_EXTENDED_ID = 67;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ENCLOSED_CJK_LETTERS_AND_MONTHS_ID = 68;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_COMPATIBILITY_ID = 69;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A_ID = 70;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_UNIFIED_IDEOGRAPHS_ID = 71;
        /** 
         * @draft ICU 2.4 
         */
        public static final int YI_SYLLABLES_ID = 72;
        /** 
         * @draft ICU 2.4 
         */
        public static final int YI_RADICALS_ID = 73;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HANGUL_SYLLABLES_ID = 74;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HIGH_SURROGATES_ID = 75;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HIGH_PRIVATE_USE_SURROGATES_ID = 76;
        /** 
         * @draft ICU 2.4 
         */
        public static final int LOW_SURROGATES_ID = 77;
        /**
         * Same as public static final int PRIVATE_USE.
         * Until Unicode 3.1.1; the corresponding block name was "Private Use";
         * and multiple code point ranges had this block.
         * Unicode 3.2 renames the block for the BMP PUA to "Private Use Area" 
         * and adds separate blocks for the supplementary PUAs.
         * @draft ICU 2.4
         */
        public static final int PRIVATE_USE_AREA_ID = 78;
        /**
         * Same as public static final int PRIVATE_USE_AREA.
         * Until Unicode 3.1.1; the corresponding block name was "Private Use";
         * and multiple code point ranges had this block.
         * Unicode 3.2 renames the block for the BMP PUA to "Private Use Area" 
         * and adds separate blocks for the supplementary PUAs.
         * @draft ICU 2.4
         */
        public static final int PRIVATE_USE_ID = PRIVATE_USE_AREA_ID;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_ID = 79;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ALPHABETIC_PRESENTATION_FORMS_ID = 80;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ARABIC_PRESENTATION_FORMS_A_ID = 81;
        /** 
         * @draft ICU 2.4 
         */
        public static final int COMBINING_HALF_MARKS_ID = 82;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_COMPATIBILITY_FORMS_ID = 83;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SMALL_FORM_VARIANTS_ID = 84;
        /** 
         * @draft ICU 2.4 
         */
        public static final int ARABIC_PRESENTATION_FORMS_B_ID = 85;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SPECIALS_ID = 86;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HALFWIDTH_AND_FULLWIDTH_FORMS_ID = 87;
        /** 
         * @draft ICU 2.4 
         */
        public static final int OLD_ITALIC_ID = 88;
        /** 
         * @draft ICU 2.4 
         */
        public static final int GOTHIC_ID = 89;
        /** 
         * @draft ICU 2.4 
         */
        public static final int DESERET_ID = 90;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BYZANTINE_MUSICAL_SYMBOLS_ID = 91;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MUSICAL_SYMBOLS_ID = 92;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93;
        /** 
         * @draft ICU 2.4 
         */
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B_ID = 94;
        /** 
         * @draft ICU 2.4 
         */
        public static final int 
            CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID = 95;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TAGS_ID = 96;
    
        // New blocks in Unicode 3.2
    
        /** 
         * @draft ICU 2.4 
         */
        public static final int CYRILLIC_SUPPLEMENTARY_ID = 97;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TAGALOG_ID = 98;
        /** 
         * @draft ICU 2.4 
         */
        public static final int HANUNOO_ID = 99;
        /** 
         * @draft ICU 2.4 
         */
        public static final int BUHID_ID = 100;
        /** 
         * @draft ICU 2.4 
         */
        public static final int TAGBANWA_ID = 101;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A_ID = 102;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPPLEMENTAL_ARROWS_A_ID = 103;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPPLEMENTAL_ARROWS_B_ID = 104;
        /** 
         * @draft ICU 2.4 
         */
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B_ID = 105;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPPLEMENTAL_MATHEMATICAL_OPERATORS_ID = 106;
        /** 
         * @draft ICU 2.4 
         */
        public static final int KATAKANA_PHONETIC_EXTENSIONS_ID = 107;
        /** 
         * @draft ICU 2.4 
         */
        public static final int VARIATION_SELECTORS_ID = 108;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109;
        /** 
         * @draft ICU 2.4 
         */
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110;
        
        /** 
         * @draft ICU 2.4 
         */
        public static final int COUNT = 111;
        
        // public methods --------------------------------------------------
        
        /** 
         * Gets the only instance of the UnicodeBlock with the argument ID.
         * If no such ID exists, a INVALID_CODE UnicodeBlock will be returned.
         * @param id UnicodeBlock ID
         * @return the only instance of the UnicodeBlock with the argument ID
         *         if it exists, otherwise a INVALID_CODE UnicodeBlock will be 
         *         returned.
         * @draft ICU 2.4
         */
        public static UnicodeBlock getInstance(int id)
        {
            if (id > 0 && id < BLOCKS_.length) {
                return BLOCKS_[id];
            }
            return INVALID_CODE;
        }
        
        /**
         * Returns the Unicode allocation block that contains the code point,
         * or null if the code point is not a member of a defined block.
         * @param ch code point to be tested
         * @return the Unicode allocation block that contains the code point
         * @draft ICU 2.4
         */
        public static UnicodeBlock of(int ch)
        {
            if (ch > MAX_VALUE) {
                return INVALID_CODE;
            }

            return UnicodeBlock.getInstance((PROPERTY_.getAdditional(ch, 0)
                                            & BLOCK_MASK_) >> BLOCK_SHIFT_);
        }
        
        /**
         * Returns the type ID of this Unicode block
         * @return integer type ID of this Unicode block
         * @draft ICU 2.4
         */
        public int getID()
        {
            return m_id_;
        }
        
        // private data members ---------------------------------------------
        
        /**
         * Array of UnicodeBlocks, for easy access in getInstance(int)
         */
        private final static UnicodeBlock BLOCKS_[] = {
                null, BASIC_LATIN, 
                LATIN_1_SUPPLEMENT, LATIN_EXTENDED_A, 
                LATIN_EXTENDED_B, IPA_EXTENSIONS, 
                SPACING_MODIFIER_LETTERS, COMBINING_DIACRITICAL_MARKS,
                GREEK, CYRILLIC,
                ARMENIAN, HEBREW,
                ARABIC, SYRIAC, 
                THAANA, DEVANAGARI, 
                BENGALI, GURMUKHI, 
                GUJARATI, ORIYA, 
                TAMIL, TELUGU, 
                KANNADA, MALAYALAM, 
                SINHALA, THAI, 
                LAO, TIBETAN, 
                MYANMAR, GEORGIAN, 
                HANGUL_JAMO, ETHIOPIC, 
                CHEROKEE, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,
                OGHAM, RUNIC, 
                KHMER, MONGOLIAN, 
                LATIN_EXTENDED_ADDITIONAL, GREEK_EXTENDED, 
                GENERAL_PUNCTUATION, SUPERSCRIPTS_AND_SUBSCRIPTS,
                CURRENCY_SYMBOLS, COMBINING_MARKS_FOR_SYMBOLS, 
                LETTERLIKE_SYMBOLS, NUMBER_FORMS, 
                ARROWS, MATHEMATICAL_OPERATORS, 
                MISCELLANEOUS_TECHNICAL, CONTROL_PICTURES,
                OPTICAL_CHARACTER_RECOGNITION, ENCLOSED_ALPHANUMERICS,
                BOX_DRAWING, BLOCK_ELEMENTS,
                GEOMETRIC_SHAPES, MISCELLANEOUS_SYMBOLS,
                DINGBATS, BRAILLE_PATTERNS,
                CJK_RADICALS_SUPPLEMENT, KANGXI_RADICALS,
                IDEOGRAPHIC_DESCRIPTION_CHARACTERS, CJK_SYMBOLS_AND_PUNCTUATION,
                HIRAGANA, KATAKANA, 
                BOPOMOFO, HANGUL_COMPATIBILITY_JAMO,
                KANBUN, BOPOMOFO_EXTENDED, 
                ENCLOSED_CJK_LETTERS_AND_MONTHS, CJK_COMPATIBILITY,
                CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, CJK_UNIFIED_IDEOGRAPHS,
                YI_SYLLABLES, YI_RADICALS, 
                HANGUL_SYLLABLES, HIGH_SURROGATES,
                HIGH_PRIVATE_USE_SURROGATES, LOW_SURROGATES,
                PRIVATE_USE_AREA, CJK_COMPATIBILITY_IDEOGRAPHS,
                ALPHABETIC_PRESENTATION_FORMS, ARABIC_PRESENTATION_FORMS_A,
                COMBINING_HALF_MARKS, CJK_COMPATIBILITY_FORMS,
                SMALL_FORM_VARIANTS, ARABIC_PRESENTATION_FORMS_B,
                SPECIALS, HALFWIDTH_AND_FULLWIDTH_FORMS,
                OLD_ITALIC, GOTHIC, 
                DESERET, BYZANTINE_MUSICAL_SYMBOLS,
                MUSICAL_SYMBOLS, MATHEMATICAL_ALPHANUMERIC_SYMBOLS,
                CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, 
                CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, 
                TAGS, CYRILLIC_SUPPLEMENTARY,
                TAGALOG, HANUNOO, 
                BUHID, TAGBANWA, 
                MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A, SUPPLEMENTAL_ARROWS_A,
                SUPPLEMENTAL_ARROWS_B, MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,
                SUPPLEMENTAL_MATHEMATICAL_OPERATORS, 
                KATAKANA_PHONETIC_EXTENSIONS,
                VARIATION_SELECTORS, SUPPLEMENTARY_PRIVATE_USE_AREA_A,
                SUPPLEMENTARY_PRIVATE_USE_AREA_B
        };    
        /**
         * Identification code for this UnicodeBlock
         */
        private int m_id_;
        
        // private constructor ----------------------------------------------
        
        /**
         * UnicodeBlock constructor
         * @param name name of this UnicodeBlock
         * @param id unique id of this UnicodeBlock
         * @exception NullPointerException if name is <code>null</code>
         */
        private UnicodeBlock(String name, int id)
        {
            super(name);
            m_id_ = id;
        }
    };
    
    /**
     * East Asian Width constants.
     * @see UProperty#EAST_ASIAN_WIDTH
     * @see UCharacter#getIntPropertyValue
     * @draft ICU 2.4
     */
    public static interface EastAsianWidth 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int NEUTRAL = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int AMBIGUOUS = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int HALFWIDTH = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int FULLWIDTH = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int NARROW = 4;
        /**
         * @draft ICU 2.4
         */
        public static final int WIDE = 5;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 6;
    };

    /**
     * Decomposition Type constants.
     * @see UProperty#DECOMPOSITION_TYPE
     * @draft ICU 2.4
     */
    public static interface DecompositionType 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int NONE = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int CANONICAL = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int COMPAT = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int CIRCLE = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int FINAL = 4;
        /**
         * @draft ICU 2.4
         */
        public static final int FONT = 5;
        /**
         * @draft ICU 2.4
         */
        public static final int FRACTION = 6;
        /**
         * @draft ICU 2.4
         */
        public static final int INITIAL = 7;
        /**
         * @draft ICU 2.4
         */
        public static final int ISOLATED = 8;
        /**
         * @draft ICU 2.4
         */
        public static final int MEDIAL = 9;
        /**
         * @draft ICU 2.4
         */
        public static final int NARROW = 10;
        /**
         * @draft ICU 2.4
         */
        public static final int NOBREAK = 11;
        /**
         * @draft ICU 2.4
         */
        public static final int SMALL = 12;
        /**
         * @draft ICU 2.4
         */
        public static final int SQUARE = 13;
        /**
         * @draft ICU 2.4
         */
        public static final int SUB = 14;
        /**
         * @draft ICU 2.4
         */
        public static final int SUPER = 15;
        /**
         * @draft ICU 2.4
         */
        public static final int VERTICAL = 16;
        /**
         * @draft ICU 2.4
         */
        public static final int WIDE = 17;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 18;
    };
    
    /**
     * Joining Type constants.
     * @see UProperty#JOINING_TYPE
     * @draft ICU 2.4
     */
    public static interface JoiningType 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int NON_JOINING = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int JOIN_CAUSING = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int DUAL_JOINING = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int LEFT_JOINING = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int RIGHT_JOINING = 4;
        /**
         * @draft ICU 2.4
         */
        public static final int TRANSPARENT = 5;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 6;
    };
    
    /**
     * Joining Group constants.
     * @see UProperty#JOINING_GROUP
     * @draft ICU 2.4
     */
    public static interface JoiningGroup 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int NO_JOINING_GROUP = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int AIN = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int ALAPH = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int ALEF = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int BEH = 4;
        /**
         * @draft ICU 2.4
         */
        public static final int BETH = 5;
        /**
         * @draft ICU 2.4
         */
        public static final int DAL = 6;
        /**
         * @draft ICU 2.4
         */
        public static final int DALATH_RISH = 7;
        /**
         * @draft ICU 2.4
         */
        public static final int E = 8;
        /**
         * @draft ICU 2.4
         */
        public static final int FEH = 9;
        /**
         * @draft ICU 2.4
         */
        public static final int FINAL_SEMKATH = 10;
        /**
         * @draft ICU 2.4
         */
        public static final int GAF = 11;
        /**
         * @draft ICU 2.4
         */
        public static final int GAMAL = 12;
        /** 
         * @draft ICU 2.4
         */
        public static final int HAH = 13;
        /**
         * @draft ICU 2.4
         */
        public static final int HAMZA_ON_HEH_GOAL = 14;
        /**
         * @draft ICU 2.4
         */
        public static final int HE = 15;
        /**
         * @draft ICU 2.4
         */
        public static final int HEH = 16;
        /**
         * @draft ICU 2.4
         */
        public static final int HEH_GOAL = 17;
        /**
         * @draft ICU 2.4
         */
        public static final int HETH = 18;
        /**
         * @draft ICU 2.4
         */
        public static final int KAF = 19;
        /**
         * @draft ICU 2.4
         */
        public static final int KAPH = 20;
        /**
         * @draft ICU 2.4
         */
        public static final int KNOTTED_HEH = 21;
        /**
         * @draft ICU 2.4
         */
        public static final int LAM = 22;
        /**
         * @draft ICU 2.4
         */
        public static final int LAMADH = 23;
        /**
         * @draft ICU 2.4
         */
        public static final int MEEM = 24;
        /**
         * @draft ICU 2.4
         */
        public static final int MIM = 25;
        /**
         * @draft ICU 2.4
         */
        public static final int NOON = 26;
        /**
         * @draft ICU 2.4
         */
        public static final int NUN = 27;
        /**
         * @draft ICU 2.4
         */
        public static final int PE = 28;
        /**
         * @draft ICU 2.4
         */
        public static final int QAF = 29;
        /**
         * @draft ICU 2.4
         */
        public static final int QAPH = 30;
        /**
         * @draft ICU 2.4
         */
        public static final int REH = 31;
        /**
         * @draft ICU 2.4
         */
        public static final int REVERSED_PE = 32;
        /**
         * @draft ICU 2.4
         */
        public static final int SAD = 33;
        /**
         * @draft ICU 2.4
         */
        public static final int SADHE = 34;
        /**
         * @draft ICU 2.4
         */
        public static final int SEEN = 35;
        /**
         * @draft ICU 2.4
         */
        public static final int SEMKATH = 36;
        /**
         * @draft ICU 2.4
         */
        public static final int SHIN = 37;
        /**
         * @draft ICU 2.4
         */
        public static final int SWASH_KAF = 38;
        /**
         * @draft ICU 2.4
         */
        public static final int SYRIAC_WAW = 39;
        /**
         * @draft ICU 2.4
         */
        public static final int TAH = 40;
        /**
         * @draft ICU 2.4
         */
        public static final int TAW = 41;
        /**
         * @draft ICU 2.4
         */
        public static final int TEH_MARBUTA = 42;
        /**
         * @draft ICU 2.4
         */
        public static final int TETH = 43;
        /**
         * @draft ICU 2.4
         */
        public static final int WAW = 44;
        /**
         * @draft ICU 2.4
         */
        public static final int YEH = 45;
        /**
         * @draft ICU 2.4
         */
        public static final int YEH_BARREE = 46;
        /**
         * @draft ICU 2.4
         */
        public static final int YEH_WITH_TAIL = 47;
        /**
         * @draft ICU 2.4
         */
        public static final int YUDH = 48;
        /**
         * @draft ICU 2.4
         */
        public static final int YUDH_HE = 49;
        /**
         * @draft ICU 2.4
         */
        public static final int ZAIN = 50;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 51;
    };
    
    /**
     * Line Break constants.
     * @see UProperty#LINE_BREAK
     * @draft ICU 2.4
     */
    public static interface LineBreak 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int UNKNOWN = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int AMBIGUOUS = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int ALPHABETIC = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int BREAK_BOTH = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int BREAK_AFTER = 4;
        /**
         * @draft ICU 2.4
         */
        public static final int BREAK_BEFORE = 5;
        /**
         * @draft ICU 2.4
         */
        public static final int MANDATORY_BREAK = 6;
        /**
         * @draft ICU 2.4
         */
        public static final int CONTINGENT_BREAK = 7;
        /**
         * @draft ICU 2.4
         */
        public static final int CLOSE_PUNCTUATION = 8;
        /**
         * @draft ICU 2.4
         */
        public static final int COMBINING_MARK = 9;
        /**
         * @draft ICU 2.4
         */
        public static final int CARRIAGE_RETURN = 10;
        /**
         * @draft ICU 2.4
         */
        public static final int EXCLAMATION = 11;
        /**
         * @draft ICU 2.4
         */
        public static final int GLUE = 12;
        /**
         * @draft ICU 2.4
         */
        public static final int HYPHEN = 13;
        /**
         * @draft ICU 2.4
         */
        public static final int IDEOGRAPHIC = 14;
        /**
         * @draft ICU 2.4
         */
        public static final int INSEPERABLE = 15;
        /**
         * @draft ICU 2.4
         */
        public static final int INFIX_NUMERIC = 16;
        /**
         * @draft ICU 2.4
         */
        public static final int LINE_FEED = 17;
        /**
         * @draft ICU 2.4
         */
        public static final int NONSTARTER = 18;
        /**
         * @draft ICU 2.4
         */
        public static final int NUMERIC = 19;
        /**
         * @draft ICU 2.4
         */
        public static final int OPEN_PUNCTUATION = 20;
        /**
         * @draft ICU 2.4
         */
        public static final int POSTFIX_NUMERIC = 21;
        /**
         * @draft ICU 2.4
         */
        public static final int PREFIX_NUMERIC = 22;
        /**
         * @draft ICU 2.4
         */
        public static final int QUOTATION = 23;
        /**
         * @draft ICU 2.4
         */
        public static final int COMPLEX_CONTEXT = 24;
        /**
         * @draft ICU 2.4
         */
        public static final int SURROGATE = 25;
        /**
         * @draft ICU 2.4
         */
        public static final int SPACE = 26;
        /**
         * @draft ICU 2.4
         */
        public static final int BREAK_SYMBOLS = 27;
        /**
         * @draft ICU 2.4
         */
        public static final int ZWSPACE = 28;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 29;
    };
    
    /**
     * Numeric Type constants.
     * @see UProperty#NUMERIC_TYPE
     * @draft ICU 2.4
     */
    public static interface NumericType 
    {
        /**
         * @draft ICU 2.4
         */
        public static final int NONE = 0;
        /**
         * @draft ICU 2.4
         */
        public static final int DECIMAL = 1;
        /**
         * @draft ICU 2.4
         */
        public static final int DIGIT = 2;
        /**
         * @draft ICU 2.4
         */
        public static final int NUMERIC = 3;
        /**
         * @draft ICU 2.4
         */
        public static final int COUNT = 4;
    }; 
           
    // public data members -----------------------------------------------
  
    /** 
    * The lowest Unicode code point value.
    */
    public static final int MIN_VALUE = UTF16.CODEPOINT_MIN_VALUE;

    /**
    * The highest Unicode code point value (scalar value) according to the 
    * Unicode Standard. 
    * This is a 21-bit value (21 bits, rounded up).<br>
    * Up-to-date Unicode implementation of java.lang.Character.MIN_VALUE
    */
    public static final int MAX_VALUE = UTF16.CODEPOINT_MAX_VALUE; 
      
    /**
    * The minimum value for Supplementary code points
    */
    public static final int SUPPLEMENTARY_MIN_VALUE = 
                                          UTF16.SUPPLEMENTARY_MIN_VALUE;
      
    /**
    * Unicode value used when translating into Unicode encoding form and there 
    * is no existing character.
    */
	public static final int REPLACEMENT_CHAR = '\uFFFD';
    	
    /**
     * Special value that is returned by getUnicodeNumericValue(int) when no 
     * numeric value is defined for a code point.
     * @draft 2.4
     * @see #getUnicodeNumericValue
     */
    public static final double NO_NUMERIC_VALUE = -123456789;
    
    // public methods ----------------------------------------------------
      
    /**
    * Retrieves the numeric value of a decimal digit code point.
    * <br>This method observes the semantics of
    * <code>java.lang.Character.digit()</code>.  Note that this
    * will return positive values for code points for which isDigit
    * returns false, just like java.lang.Character.
    * <br><em>Semantic Change:</em> In release 1.3.1 and
    * prior, this did not treat the European letters as having a
    * digit value, and also treated numeric letters and other numbers as 
    * digits.  
    * This has been changed to conform to the java semantics.
    * <br>A code point is a valid digit if and only if:
    * <ul>
    *   <li>ch is a decimal digit or one of the european letters, and
    *   <li>the value of ch is less than the specified radix.
    * </ul>
    * @param ch the code point to query
    * @param radix the radix
    * @return the numeric value represented by the code point in the
    * specified radix, or -1 if the code point is not a decimal digit
    * or if its value is too large for the radix
    */
    public static int digit(int ch, int radix)
    {
        // when ch is out of bounds getProperty == 0
        int props       = PROPERTY_.getProperty(ch);
        int numericType = getNumericType(props);
        
        int result = -1;
        if (numericType == NumericType.DECIMAL) {
        	// if props == 0, it will just fall through and return -1
        	if (isNotExceptionIndicator(props)) {
            	// not contained in exception data
            	result = UCharacterProperty.getSignedValue(props);
            }
            else {
            	int index = UCharacterProperty.getExceptionIndex(props);
            	if (PROPERTY_.hasExceptionValue(index, 
                                   UCharacterProperty.EXC_NUMERIC_VALUE_)) {
                	return PROPERTY_.getException(index, 
                                      UCharacterProperty.EXC_NUMERIC_VALUE_); 
                }
            }
        }
        
        if (result < 0 && radix > 10) {
            result = getEuropeanDigit(ch);
        }
        
        if (result < 0 || result >= radix) {
            return -1;
        }
        return result;
    }
    
    /**
    * Retrieves the numeric value of a decimal digit code point.
    * <br>This is a convenience overload of <code>digit(int, int)</code> 
    * that provides a decimal radix.
    * <br><em>Semantic Change:</em> In release 1.3.1 and prior, this
    * treated numeric letters and other numbers as digits.  This has
    * been changed to conform to the java semantics.
    * @param ch the code point to query
    * @return the numeric value represented by the code point,
    * or -1 if the code point is not a decimal digit or if its
    * value is too large for a decimal radix 
    */
    public static int digit(int ch)
    {
        return digit(ch, DECIMAL_RADIX_);
    }

    /** 
     * Returns the numeric value of the code point as a nonnegative 
     * integer.
     * <br>If the code point does not have a numeric value, then -1 is returned. 
     * <br>
     * If the code point has a numeric value that cannot be represented as a 
     * nonnegative integer (for example, a fractional value), then -2 is 
     * returned.
     * @param ch the code point to query
     * @return the numeric value of the code point, or -1 if it has no numeric 
     * value, or -2 if it has a numeric value that cannot be represented as a 
     * nonnegative integer
     */
    public static int getNumericValue(int ch)
    {
        int props = PROPERTY_.getProperty(ch);
        int numericType = getNumericType(props);
        
        int result = -1;
        if (numericType == NumericType.DECIMAL) {
            result = -2;
        }
        if (numericType != NumericType.NONE) {
            // if props == 0, it will just fall through and return -1
            if (isNotExceptionIndicator(props)) {
                // not contained in exception data
                return UCharacterProperty.getSignedValue(props);
            }
            
            int index = UCharacterProperty.getExceptionIndex(props);
            if (!PROPERTY_.hasExceptionValue(index, 
                               UCharacterProperty.EXC_DENOMINATOR_VALUE_) && 
                PROPERTY_.hasExceptionValue(index, 
                                   UCharacterProperty.EXC_NUMERIC_VALUE_)) {
                return PROPERTY_.getException(index, 
                                     UCharacterProperty.EXC_NUMERIC_VALUE_);
            }
        }
        
        if (result < 0) {
            int europeannumeric = getEuropeanDigit(ch);
            if (europeannumeric >= 0) {
                return europeannumeric;
            }
        }
        
        return result;
    }

   /*
    * Returns the Unicode numeric value of the code point as a nonnegative 
    * integer.
    * <br>If the code point does not have a numeric value, then -1 is returned. <br>
    * If the code point has a numeric value that cannot be represented as a 
    * nonnegative integer (for example, a fractional value), then -2 is 
    * returned.
    * This returns values other than -1 for all and only those code points 
    * whose type is a numeric type.
    * @param ch the code point to query
    * @return the numeric value of the code point, or -1 if it has no numeric 
    * value, or -2 if it has a numeric value that cannot be represented as a 
    * nonnegative integer
    public static int getUnicodeNumericValue(int ch)
    {
        return getNumericValueInternal(ch, false);
    }
    */
    
    /**
     * <p>Get the numeric value for a Unicode code point as defined in the 
     * Unicode Character Database.</p>
     * <p>A "double" return type is necessary because some numeric values are 
     * fractions, negative, or too large for int.</p>
     * <p>For characters without any numeric values in the Unicode Character 
     * Database, this function will return NO_NUMERIC_VALUE.</p>
     * <p><em>API Change:</em> In release 2.2 and prior, this API has a
     * return type int and returns -1 when the argument ch does not have a 
     * corresponding numeric value. This has been changed to synch with ICU4C
     * </p>
     * @param ch Code point to get the numeric value for.
     * @return numeric value of ch, or NO_NUMERIC_VALUE if none is defined.
     * @draft 2.4
     */
    public static double getUnicodeNumericValue(int ch)
    {
        // equivalent to c version double u_getNumericValue(UChar32 c)
        int props = PROPERTY_.getProperty(ch);
        int numericType = getNumericType(props);
        if (numericType > NumericType.NONE && numericType < NumericType.COUNT) {
            if (isNotExceptionIndicator(props)) {
                return UCharacterProperty.getSignedValue(props);
            } 
            else {
                int index = UCharacterProperty.getExceptionIndex(props);
                boolean nex = false;
                boolean dex = false;
                double numerator = 0;
                if (PROPERTY_.hasExceptionValue(index, 
                               UCharacterProperty.EXC_NUMERIC_VALUE_)) {
                    int num = PROPERTY_.getException(index, 
                                         UCharacterProperty.EXC_NUMERIC_VALUE_);
                    // There are special values for huge numbers that are 
                    // powers of ten. genprops/store.c documents:
                    // if numericValue = 0x7fffff00 + x then 
                    // numericValue = 10 ^ x
                    if (num >= NUMERATOR_POWER_LIMIT_) {
                        num &= 0xff;
                        // 10^x without math.h
                        numerator = Math.pow(10, num);
                    } 
                    else {
                        numerator = num;
                    }
                    nex = true;
                }
                double denominator = 0;
                if (PROPERTY_.hasExceptionValue(index, 
                                  UCharacterProperty.EXC_DENOMINATOR_VALUE_)) {
                    denominator = PROPERTY_.getException(index, 
                                     UCharacterProperty.EXC_DENOMINATOR_VALUE_);
                    // faster path not in c
                    if (numerator != 0) {
                        return numerator / denominator;
                    }
                    dex = true;
                } 
        
                if (nex) {
                    if (dex) {
                        return numerator / denominator;
                    } 
                    return numerator;
                }
                if (dex) {
                    return 1 / denominator;
                }
            }
        }
        return NO_NUMERIC_VALUE;
    }
  
    /**
    * Returns a value indicating a code point's Unicode category.
    * Up-to-date Unicode implementation of java.lang.Character.getType() except 
    * for the above mentioned code points that had their category changed.<br>
    * Return results are constants from the interface 
    * <a href=UCharacterCategory.html>UCharacterCategory</a>
    * @param ch code point whose type is to be determined
    * @return category which is a value of UCharacterCategory
    */
    public static int getType(int ch)
    {
        // when ch is out of bounds getProperty == 0
        // return UCharacterProperty.TYPE_MASK & PROPERTY_.getProperty(ch);
        return PROPERTY_.getProperty(ch) & UCharacterProperty.TYPE_MASK;
    }
       
    /**
    * Determines if a code point has a defined meaning in the up-to-date Unicode
    * standard.
    * E.g. supplementary code points though allocated space are not defined in 
    * Unicode yet.<br>
    * Up-to-date Unicode implementation of java.lang.Character.isDefined()
    * @param ch code point to be determined if it is defined in the most current 
    *        version of Unicode
    * @return true if this code point is defined in unicode
    */
    public static boolean isDefined(int ch)
    {
        return getType(ch) != 0;
    }
                                    
   /**
    * Determines if a code point is a Java digit.
    * <br>This method observes the semantics of
    * <code>java.lang.Character.isDigit()</code>.  It returns true for
    * decimal digits only.
    * <br><em>Semantic Change:</em> In release 1.3.1 and prior, this
    * treated numeric letters and other numbers as digits.  This has
    * been changed to conform to the java semantics.
    * @param ch code point to query
    * @return true if this code point is a digit */
    public static boolean isDigit(int ch)
    {
        return getType(ch) == UCharacterCategory.DECIMAL_DIGIT_NUMBER;
    }

    /**
    * Determines if the specified code point is an ISO control character.
    * A code point is considered to be an ISO control character if it is in the 
    * range &#92u0000 through &#92u001F or in the range &#92u007F through 
    * &#92u009F.<br>
    * Up-to-date Unicode implementation of java.lang.Character.isISOControl()
    * @param ch code point to determine if it is an ISO control character
    * @return true if code point is a ISO control character
    */
    public static boolean isISOControl(int ch)
    {
        return ch >= 0 && ch <= APPLICATION_PROGRAM_COMMAND_ && 
            ((ch <= UNIT_SEPARATOR_) || (ch >= DELETE_));
    }
                                    
    /**
    * Determines if the specified code point is a letter.
    * Up-to-date Unicode implementation of java.lang.Character.isLetter()
    * @param ch code point to determine if it is a letter
    * @return true if code point is a letter
    */
    public static boolean isLetter(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.UPPERCASE_LETTER || 
            cat == UCharacterCategory.LOWERCASE_LETTER || 
            cat == UCharacterCategory.TITLECASE_LETTER || 
            cat == UCharacterCategory.MODIFIER_LETTER ||
            cat == UCharacterCategory.OTHER_LETTER;
    }
                
    /**
    * Determines if the specified code point is a letter or digit.
    * Note this method, unlike java.lang.Character does not regard the ascii 
    * characters 'A' - 'Z' and 'a' - 'z' as digits.
    * @param ch code point to determine if it is a letter or a digit
    * @return true if code point is a letter or a digit
    */
    public static boolean isLetterOrDigit(int ch)
    {
        int cat = getType(ch);
        return cat == UCharacterCategory.UPPERCASE_LETTER 
               || cat == UCharacterCategory.LOWERCASE_LETTER 
               || cat == UCharacterCategory.TITLECASE_LETTER 
               || cat == UCharacterCategory.MODIFIER_LETTER 
               || cat == UCharacterCategory.OTHER_LETTER 
               || cat == UCharacterCategory.DECIMAL_DIGIT_NUMBER;
    }
        
    /**
    * Determines if the specified code point is a lowercase character.
    * UnicodeData only contains case mappings for code points where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br> For more information about Unicode case mapping please 
    * refer to the <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * Up-to-date Unicode implementation of java.lang.Character.isLowerCase()
    * @param ch code point to determine if it is in lowercase
    * @return true if code point is a lowercase character
    */
    public static boolean isLowerCase(int ch)
    {
        // if props == 0, it will just fall through and return false
        return getType(ch) == UCharacterCategory.LOWERCASE_LETTER;
    }
       
    /**
    * Determines if the specified code point is a white space character.
    * A code point is considered to be an whitespace character if and only
    * if it satisfies one of the following criteria:
    * <ul>
    * <li> It is a Unicode space separator (category "Zs"), but is not
    *      a no-break space (&#92u00A0 or &#92u202F or &#92uFEFF).
    * <li> It is a Unicode line separator (category "Zl").
    * <li> It is a Unicode paragraph separator (category "Zp").
    * <li> It is &#92u0009, HORIZONTAL TABULATION. 
    * <li> It is &#92u000A, LINE FEED. 
    * <li> It is &#92u000B, VERTICAL TABULATION. 
    * <li> It is &#92u000C, FORM FEED. 
    * <li> It is &#92u000D, CARRIAGE RETURN. 
    * <li> It is &#92u001C, FILE SEPARATOR. 
    * <li> It is &#92u001D, GROUP SEPARATOR. 
    * <li> It is &#92u001E, RECORD SEPARATOR. 
    * <li> It is &#92u001F, UNIT SEPARATOR. 
    * </ul>
    *
    * Up-to-date Unicode implementation of java.lang.Character.isWhitespace().
    * @param ch code point to determine if it is a white space
    * @return true if the specified code point is a white space character
    */
    public static boolean isWhitespace(int ch)
    {
        int cat = getType(ch);
        // exclude no-break spaces
        // if props == 0, it will just fall through and return false
        return (cat == UCharacterCategory.SPACE_SEPARATOR || 
                cat == UCharacterCategory.LINE_SEPARATOR ||
                cat == UCharacterCategory.PARAGRAPH_SEPARATOR) && 
                (ch != NO_BREAK_SPACE_) && 
                (ch != NARROW_NO_BREAK_SPACE_) && 
                (ch != ZERO_WIDTH_NO_BREAK_SPACE_) ||
                // TAB VT LF FF CR FS GS RS US NL are all control characters
                // that are white spaces.
                (ch >= 0x9 && ch <= 0xd) || (ch >= 0x1c && ch <= 0x1f);
    }
       
    /**
    * Determines if the specified code point is a Unicode specified space 
    * character, i.e. if code point is in the category Zs, Zl and Zp.
    * Up-to-date Unicode implementation of java.lang.Character.isSpaceChar().
    * @param ch code point to determine if it is a space
    * @return true if the specified code point is a space character
    */
    public static boolean isSpaceChar(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.SPACE_SEPARATOR || 
            cat == UCharacterCategory.LINE_SEPARATOR ||
            cat == UCharacterCategory.PARAGRAPH_SEPARATOR;
    }
                                    
    /**
    * Determines if the specified code point is a titlecase character.
    * UnicodeData only contains case mappings for code points where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br>
    * For more information about Unicode case mapping please refer to the 
    * <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * Up-to-date Unicode implementation of java.lang.Character.isTitleCase().
    * @param ch code point to determine if it is in title case
    * @return true if the specified code point is a titlecase character
    */
    public static boolean isTitleCase(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.TITLECASE_LETTER;
    }
       
    /**
    * Determines if the specified code point may be any part of a Unicode 
    * identifier other than the starting character.
    * A code point may be part of a Unicode identifier if and only if it is one 
    * of the following: 
    * <ul>
    * <li> Lu Uppercase letter
    * <li> Ll Lowercase letter
    * <li> Lt Titlecase letter
    * <li> Lm Modifier letter
    * <li> Lo Other letter
    * <li> Nl Letter number
    * <li> Pc Connecting punctuation character 
    * <li> Nd decimal number
    * <li> Mc Spacing combining mark 
    * <li> Mn Non-spacing mark 
    * <li> Cf formatting code
    * </ul>
    * Up-to-date Unicode implementation of 
    * java.lang.Character.isUnicodeIdentifierPart().<br>
    * See <a href=http://www.unicode.org/unicode/reports/tr8/>UTR #8</a>.
    * @param ch code point to determine if is can be part of a Unicode identifier
    * @return true if code point is any character belonging a unicode identifier
    *         suffix after the first character
    */
    public static boolean isUnicodeIdentifierPart(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.UPPERCASE_LETTER || 
            cat == UCharacterCategory.LOWERCASE_LETTER || 
            cat == UCharacterCategory.TITLECASE_LETTER || 
            cat == UCharacterCategory.MODIFIER_LETTER ||
            cat == UCharacterCategory.OTHER_LETTER || 
            cat == UCharacterCategory.LETTER_NUMBER ||
            cat == UCharacterCategory.CONNECTOR_PUNCTUATION ||
            cat == UCharacterCategory.DECIMAL_DIGIT_NUMBER ||
            cat == UCharacterCategory.COMBINING_SPACING_MARK || 
            cat == UCharacterCategory.NON_SPACING_MARK || 
            // cat == UCharacterCategory.FORMAT;
            isIdentifierIgnorable(ch);
    }
                       
    /**
    * Determines if the specified code point is permissible as the first 
    * character in a Unicode identifier.
    * A code point may start a Unicode identifier if it is of type either 
    * <ul> 
    * <li> Lu Uppercase letter
    * <li> Ll Lowercase letter
    * <li> Lt Titlecase letter
    * <li> Lm Modifier letter
    * <li> Lo Other letter
    * <li> Nl Letter number
    * </ul>
    * Up-to-date Unicode implementation of 
    * java.lang.Character.isUnicodeIdentifierStart().<br>
    * See <a href=http://www.unicode.org/unicode/reports/tr8/>UTR #8</a>.
    * @param ch code point to determine if it can start a Unicode identifier
    * @return true if code point is the first character belonging a unicode 
    *              identifier
    */
    public static boolean isUnicodeIdentifierStart(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.UPPERCASE_LETTER || 
            cat == UCharacterCategory.LOWERCASE_LETTER || 
            cat == UCharacterCategory.TITLECASE_LETTER || 
            cat == UCharacterCategory.MODIFIER_LETTER ||
            cat == UCharacterCategory.OTHER_LETTER || 
            cat == UCharacterCategory.LETTER_NUMBER;
    }

    /**
    * Determines if the specified code point should be regarded as an ignorable
    * character in a Unicode identifier.
    * A character is ignorable in the Unicode standard if it is of the type Cf, 
    * Formatting code.<br>
    * Up-to-date Unicode implementation of 
    * java.lang.Character.isIdentifierIgnorable().<br>
    * See <a href=http://www.unicode.org/unicode/reports/tr8/>UTR #8</a>.
    * @param ch code point to be determined if it can be ignored in a Unicode 
    *        identifier.
    * @return true if the code point is ignorable
    */
    public static boolean isIdentifierIgnorable(int ch)
    {
        // see java.lang.Character.isIdentifierIgnorable() on range of 
        // ignorable characters.
        return ch <= 8 || (ch >= 0xe && ch <= 0x1b) 
               || (ch >= 0x7f && ch <= 0x9f) 
               || (ch >= 0x200a && ch <= 0x200f) 
               || (ch >= 0x206a && ch <= 0x206f) || ch == 0xfeff;
    }
                      
    /**
    * Determines if the specified code point is an uppercase character.
    * UnicodeData only contains case mappings for code point where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br> 
    * For language specific case conversion behavior, use 
    * toUpperCase(locale, str). <br>
    * For example, the case conversion for dot-less i and dotted I in Turkish,
    * or for final sigma in Greek.
    * For more information about Unicode case mapping please refer to the 
    * <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * Up-to-date Unicode implementation of java.lang.Character.isUpperCase().
    * @param ch code point to determine if it is in uppercase
    * @return true if the code point is an uppercase character
    */
    public static boolean isUpperCase(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.UPPERCASE_LETTER;
    }
                       
    /**
    * The given code point is mapped to its lowercase equivalent; if the code 
    * point has no lowercase equivalent, the code point itself is returned.
    * UnicodeData only contains case mappings for code point where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br> 
    * For language specific case conversion behavior, use 
    * toLowerCase(locale, str). <br>
    * For example, the case conversion for dot-less i and dotted I in Turkish,
    * or for final sigma in Greek.
    * For more information about Unicode case mapping please refer to the 
    * <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * Up-to-date Unicode implementation of java.lang.Character.toLowerCase()
    * @param ch code point whose lowercase equivalent is to be retrieved
    * @return the lowercase equivalent code point
    */
    public static int toLowerCase(int ch)
    {
        // when ch is out of bounds getProperty == 0   
        int props = PROPERTY_.getProperty(ch);
        // if props == 0, it will just fall through and return itself
        if(isNotExceptionIndicator(props)) {
            int cat = UCharacterProperty.TYPE_MASK & props;
            if (cat == UCharacterCategory.UPPERCASE_LETTER || 
                cat == UCharacterCategory.TITLECASE_LETTER) {
                return ch + UCharacterProperty.getSignedValue(props);
            }
        } 
        else 
        {
            int index = UCharacterProperty.getExceptionIndex(props);
            if (PROPERTY_.hasExceptionValue(index, 
                                       UCharacterProperty.EXC_LOWERCASE_)) {
                return PROPERTY_.getException(index, 
                                         UCharacterProperty.EXC_LOWERCASE_); 
            }
        }
        return ch;
    }

    /**
    * Converts argument code point and returns a String object representing the 
    * code point's value in UTF16 format.
    * The result is a string whose length is 1 for non-supplementary code points, 
    * 2 otherwise.<br>
    * com.ibm.ibm.icu.UTF16 can be used to parse Strings generated by this 
    * function.<br>
    * Up-to-date Unicode implementation of java.lang.Character.toString()
    * @param ch code point
    * @return string representation of the code point, null if code point is not
    *         defined in unicode
    */
    public static String toString(int ch)
    {
        if (ch < MIN_VALUE || ch > MAX_VALUE) {
            return null;
        }
        
        if (ch < SUPPLEMENTARY_MIN_VALUE) {
            return String.valueOf((char)ch);
        }
        
        StringBuffer result = new StringBuffer();
        result.append(UTF16.getLeadSurrogate(ch));
        result.append(UTF16.getTrailSurrogate(ch));
        return result.toString();
    }
                                    
    /**
    * Converts the code point argument to titlecase.
    * UnicodeData only contains case mappings for code points where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br> 
    * There are only four Unicode characters that are truly titlecase forms
    * that are distinct from uppercase forms.
    * For more information about Unicode case mapping please refer
    * to the <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * If no titlecase is available, the uppercase is returned. If no uppercase 
    * is available, the code point itself is returned.<br>
    * Up-to-date Unicode implementation of java.lang.Character.toTitleCase()
    * @param ch code point  whose title case is to be retrieved
    * @return titlecase code point
    */
    public static int toTitleCase(int ch)
    {
        // when ch is out of bounds getProperty == 0
        int props = PROPERTY_.getProperty(ch);
        // if props == 0, it will just fall through and return itself
        if (isNotExceptionIndicator(props)) {
            if ((UCharacterProperty.TYPE_MASK & props) == 
                UCharacterCategory.LOWERCASE_LETTER) {
                // here, titlecase is same as uppercase
                return ch - UCharacterProperty.getSignedValue(props);
            }
        } 
        else {
            int index = UCharacterProperty.getExceptionIndex(props);
            if (PROPERTY_.hasExceptionValue(index, 
                                         UCharacterProperty.EXC_TITLECASE_)) {
                return PROPERTY_.getException(index,
                                         UCharacterProperty.EXC_TITLECASE_);
            }
            else {
                // here, titlecase is same as uppercase
                if (PROPERTY_.hasExceptionValue(index, 
                                       UCharacterProperty.EXC_UPPERCASE_)) {
                    return PROPERTY_.getException(index, 
                                         UCharacterProperty.EXC_UPPERCASE_); 
                }
            }
        }
        return ch; // no mapping - return c itself
    }
       
    /**
    * Converts the character argument to uppercase.
    * UnicodeData only contains case mappings for characters where they are 
    * one-to-one mappings; it also omits information about context-sensitive 
    * case mappings.<br> 
    * For more information about Unicode case mapping please refer
    * to the <a href=http://www.unicode.org/unicode/reports/tr21/>
    * Technical report #21</a>.<br>
    * If no uppercase is available, the character itself is returned.<br>
    * Up-to-date Unicode implementation of java.lang.Character.toUpperCase()
    * @param ch code point whose uppercase is to be retrieved
    * @return uppercase code point
    */
    public static int toUpperCase(int ch)
    {
        // when ch is out of bounds getProperty == 0
        int props = PROPERTY_.getProperty(ch);
        // if props == 0, it will just fall through and return itself
        if (isNotExceptionIndicator(props)) {
            if ((UCharacterProperty.TYPE_MASK & props) == 
                UCharacterCategory.LOWERCASE_LETTER) {
                // here, titlecase is same as uppercase */
                return ch - UCharacterProperty.getSignedValue(props);
            }
        }
        else 
        {
            int index = UCharacterProperty.getExceptionIndex(props);
            if (PROPERTY_.hasExceptionValue(index, 
                                         UCharacterProperty.EXC_UPPERCASE_)) {
                return PROPERTY_.getException(index, 
                                         UCharacterProperty.EXC_UPPERCASE_); 
            }
        }
        return ch; // no mapping - return c itself
    }
       
    // extra methods not in java.lang.Character --------------------------
       
    /**
    * Determines if the code point is a supplementary character.
    * A code point is a supplementary character if and only if it is greater than
    * <a href=#SUPPLEMENTARY_MIN_VALUE>SUPPLEMENTARY_MIN_VALUE</a>
    * @param ch code point to be determined if it is in the supplementary plane
    * @return true if code point is a supplementary character
    */
    public static boolean isSupplementary(int ch)
    {
        return ch >= UCharacter.SUPPLEMENTARY_MIN_VALUE && 
            ch <= UCharacter.MAX_VALUE;
    }
      
    /**
    * Determines if the code point is in the BMP plane.
    * @param ch code point to be determined if it is not a supplementary 
    *        character
    * @return true if code point is not a supplementary character
    */
    public static boolean isBMP(int ch) 
    {
        return (ch >= 0 && ch <= LAST_CHAR_MASK_);
    }

    /**
    * Determines whether the specified code point is a printable character 
    * according to the Unicode standard.
    * @param ch code point to be determined if it is printable
    * @return true if the code point is a printable character
    */
    public static boolean isPrintable(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return (cat != UCharacterCategory.UNASSIGNED && 
            cat != UCharacterCategory.CONTROL && 
            cat != UCharacterCategory.FORMAT &&
            cat != UCharacterCategory.PRIVATE_USE &&
            cat != UCharacterCategory.SURROGATE &&
            cat != UCharacterCategory.GENERAL_OTHER_TYPES);
    }

    /**
    * Determines whether the specified code point is of base form.
    * A code point of base form does not graphically combine with preceding 
    * characters, and is neither a control nor a format character.
    * @param ch code point to be determined if it is of base form
    * @return true if the code point is of base form
    */
    public static boolean isBaseForm(int ch)
    {
        int cat = getType(ch);
        // if props == 0, it will just fall through and return false
        return cat == UCharacterCategory.DECIMAL_DIGIT_NUMBER || 
            cat == UCharacterCategory.OTHER_NUMBER || 
            cat == UCharacterCategory.LETTER_NUMBER || 
            cat == UCharacterCategory.UPPERCASE_LETTER || 
            cat == UCharacterCategory.LOWERCASE_LETTER || 
            cat == UCharacterCategory.TITLECASE_LETTER ||
            cat == UCharacterCategory.MODIFIER_LETTER || 
            cat == UCharacterCategory.OTHER_LETTER || 
            cat == UCharacterCategory.NON_SPACING_MARK || 
            cat == UCharacterCategory.ENCLOSING_MARK ||
            cat == UCharacterCategory.COMBINING_SPACING_MARK;
    }

    /**
    * Returns the Bidirection property of a code point.
    * For example, 0x0041 (letter A) has the LEFT_TO_RIGHT directional 
    * property.<br>
    * Result returned belongs to the interface 
    * <a href=UCharacterDirection.html>UCharacterDirection</a>
    * @param ch the code point to be determined its direction
    * @return direction constant from UCharacterDirection.
    */
    public static int getDirection(int ch)
    {
        // when ch is out of bounds getProperty == 0
        return (PROPERTY_.getProperty(ch) >> BIDI_SHIFT_) 
                                                     & BIDI_MASK_AFTER_SHIFT_;
    }

    /**
    * Determines whether the code point has the "mirrored" property.
    * This property is set for characters that are commonly used in
    * Right-To-Left contexts and need to be displayed with a "mirrored"
    * glyph.
    * @param ch code point whose mirror is to be determined
    * @return true if the code point has the "mirrored" property
    */
    public static boolean isMirrored(int ch)
    {
        // when ch is out of bounds getProperty == 0
        return (PROPERTY_.getProperty(ch) & UCharacterProperty.MIRROR_MASK) 
                                                                         != 0;
    }

    /**
    * Maps the specified code point to a "mirror-image" code point.
    * For code points with the "mirrored" property, implementations sometimes 
    * need a "poor man's" mapping to another code point such that the default 
    * glyph may serve as the mirror-image of the default glyph of the specified
    * code point.<br> 
    * This is useful for text conversion to and from codepages with visual 
    * order, and for displays without glyph selection capabilities.
    * @param ch code point whose mirror is to be retrieved
    * @return another code point that may serve as a mirror-image substitute, or 
    *         ch itself if there is no such mapping or ch does not have the 
    *         "mirrored" property
    */
    public static int getMirror(int ch)
    {
        // when ch is out of bounds getProperty == 0
        int props = PROPERTY_.getProperty(ch);
        // mirrored - the value is a mirror offset
        // if props == 0, it will just fall through and return false
        if ((props & UCharacterProperty.MIRROR_MASK) != 0) {
            if(isNotExceptionIndicator(props)) {
                return ch + UCharacterProperty.getSignedValue(props);
            }
            else 
            {
                int index = UCharacterProperty.getExceptionIndex(props);
                if (PROPERTY_.hasExceptionValue(index, 
                                    UCharacterProperty.EXC_MIRROR_MAPPING_)) 
                return PROPERTY_.getException(index, 
                                     UCharacterProperty.EXC_MIRROR_MAPPING_);   
            }
        }
        return ch;
    }
      
    /**
    * Gets the combining class of the argument codepoint
    * @param ch code point whose combining is to be retrieved
    * @return the combining class of the codepoint
    */
    public static int getCombiningClass(int ch)
    {
    	if (ch < MIN_VALUE || ch > MAX_VALUE) {
    		throw new IllegalArgumentException("Codepoint out of bounds");
    	}
    	return NormalizerImpl.getCombiningClass(ch);
    }
      
    /**
    * A code point is illegal if and only if
    * <ul>
    * <li> Out of bounds, less than 0 or greater than UCharacter.MAX_VALUE
    * <li> A surrogate value, 0xD800 to 0xDFFF
    * <li> Not-a-character, having the form 0x xxFFFF or 0x xxFFFE
    * </ul>
    * Note: legal does not mean that it is assigned in this version of Unicode.
    * @param ch code point to determine if it is a legal code point by itself
    * @return true if and only if legal. 
    */
    public static boolean isLegal(int ch) 
    {
        if (ch < MIN_VALUE) {
            return false;
        }
        if (ch < UTF16.SURROGATE_MIN_VALUE) {
            return true;
        }
        if (ch <= UTF16.SURROGATE_MAX_VALUE) {
            return false;
        }
        if (UCharacterUtility.isNonCharacter(ch)) {
            return false;
        }
        return (ch <= MAX_VALUE);
    }
      
    /**
    * A string is legal iff all its code points are legal.
    * A code point is illegal if and only if
    * <ul>
    * <li> Out of bounds, less than 0 or greater than UCharacter.MAX_VALUE
    * <li> A surrogate value, 0xD800 to 0xDFFF
    * <li> Not-a-character, having the form 0x xxFFFF or 0x xxFFFE
    * </ul>
    * Note: legal does not mean that it is assigned in this version of Unicode.
    * @param ch code point to determine if it is a legal code point by itself
    * @return true if and only if legal. 
    */
    public static boolean isLegal(String str) 
    {
        int size = str.length();
        int codepoint;
        for (int i = 0; i < size; i ++)
        {
            codepoint = UTF16.charAt(str, i);
            if (!isLegal(codepoint)) {
                return false;
            }
            if (isSupplementary(codepoint)) {
                i ++;
            }
        }
        return true;
    }

    /**
    * Gets the version of Unicode data used. 
    * @return the unicode version number used
    */
    public static VersionInfo getUnicodeVersion()
    {
        return PROPERTY_.m_unicodeVersion_;
    }
      
    /**
    * Retrieve the most current Unicode name of the argument code point, or 
    * null if the character is unassigned or outside the range 
    * UCharacter.MIN_VALUE and UCharacter.MAX_VALUE or does not have a name.
    * <br>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param ch the code point for which to get the name
    * @return most current Unicode name
    */
    public static String getName(int ch)
    {
        return NAME_.getName(ch, UCharacterNameChoice.UNICODE_CHAR_NAME);
    }
      
    /**
    * Retrieve the earlier version 1.0 Unicode name of the argument code point,
    * or null if the character is unassigned or outside the range 
    * UCharacter.MIN_VALUE and UCharacter.MAX_VALUE or does not have a name.
    * <br>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param ch the code point for which to get the name
    * @return version 1.0 Unicode name
    */
    public static String getName1_0(int ch)
    {
        return NAME_.getName(ch, 
                             UCharacterNameChoice.UNICODE_10_CHAR_NAME);
    }
    
    /**
    * <p>Retrieves a name for a valid codepoint. Unlike, getName(int) and
    * getName1_0(int), this method will return a name even for codepoints that
    * are not assigned a name in UnicodeData.txt.
    * </p>
    * The names are returned in the following order.
    * <ul>
    * <li> Most current Unicode name if there is any
    * <li> Unicode 1.0 name if there is any
    * <li> Extended name in the form of "<codepoint_type-codepoint_hex_digits>". 
    *      E.g. <noncharacter-fffe>
    * </ul>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param ch the code point for which to get the name
    * @return a name for the argument codepoint
    * @draft 2.1
    */
    public static String getExtendedName(int ch) 
    {
        return NAME_.getName(ch, UCharacterNameChoice.EXTENDED_CHAR_NAME);
    }
    
    /**
     * Get the ISO 10646 comment for a character.
     * The ISO 10646 comment is an informative field in the Unicode Character
     * Database (UnicodeData.txt field 11) and is from the ISO 10646 names list.
     * @param ch The code point for which to get the ISO comment.
     *           It must be <code>0<=c<=0x10ffff</code>.
     * @return The ISO comment, or null if there is no comment for this 
     *         character.
     * @draft ICU 2.4
     */
    public static String getISOComment(int ch)
    {
        if (ch < UCharacter.MIN_VALUE || ch > UCharacter.MAX_VALUE) {
            return null;
        }
        
        String result = NAME_.getGroupName(ch, 
                                           UCharacterNameChoice.ISO_COMMENT_);
        return result;
    }
      
    /**
    * <p>Find a Unicode code point by its most current Unicode name and 
    * return its code point value. All Unicode names are in uppercase.</p>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param name most current Unicode character name whose code point is to be 
    *        returned
    * @return code point or -1 if name is not found
    */
    public static int getCharFromName(String name)
    {
        return NAME_.getCharFromName(
                            UCharacterNameChoice.UNICODE_CHAR_NAME, name);
    }
      
    /**
    * <p>Find a Unicode character by its version 1.0 Unicode name and return 
    * its code point value. All Unicode names are in uppercase.</p>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param name Unicode 1.0 code point name whose code point is to 
    *             returned
    * @return code point or -1 if name is not found
    */
    public static int getCharFromName1_0(String name)
    {
        return NAME_.getCharFromName(
                         UCharacterNameChoice.UNICODE_10_CHAR_NAME, name);
    }
    
    /**
    * <p>Find a Unicode character by either its name and return its code 
    * point value. All Unicode names are in uppercase. 
    * Extended names are all lowercase except for numbers and are contained
    * within angle brackets.</p>
    * The names are searched in the following order
    * <ul>
    * <li> Most current Unicode name if there is any
    * <li> Unicode 1.0 name if there is any
    * <li> Extended name in the form of "<codepoint_type-codepoint_hex_digits>". 
    *      E.g. <noncharacter-FFFE>
    * </ul>
    * Note calling any methods related to code point names, e.g. get*Name*() 
    * incurs a one-time initialisation cost to construct the name tables.
    * @param name codepoint name
    * @return code point associated with the name or -1 if the name is not
    *         found.
    * @draft 2.1
    */
    public static int getCharFromExtendedName(String name)
    {
        return NAME_.getCharFromName(
                            UCharacterNameChoice.EXTENDED_CHAR_NAME, name);
    }

    /**
     * Return the Unicode name for a given property, as given in the
     * Unicode database file PropertyAliases.txt.  Most properties
     * have more than one name.  The nameChoice determines which one
     * is returned.
     *
     * @param property UProperty selector.
     *
     * @param nameChoice UProperty.NameChoice selector for which name
     * to get.  All properties have a long name.  Most have a short
     * name, but some do not.  Unicode allows for additional names; if
     * present these will be returned by UProperty.NameChoice.LONG + i,
     * where i=1, 2,...
     *
     * @return a name, or null if Unicode explicitly defines no name
     * ("n/a") for a given property/nameChoice.  If a given nameChoice
     * throws an exception, then all larger values of nameChoice will
     * throw an exception.  If null is returned for a given
     * nameChoice, then other nameChoice values may return non-null
     * results.
     *
     * @exception IllegalArgumentException thrown if property or
     * nameChoice are invalid.
     *
     * @see UProperty
     * @see UProperty.NameChoice
     * @since ICU 2.4
     * @draft 2.4
     */
    public static String getPropertyName(int property,
                                         int nameChoice) {
        return PNAMES_.getPropertyName(property, nameChoice);
    }

    /**
     * Return the UProperty selector for a given property name, as
     * specified in the Unicode database file PropertyAliases.txt.
     * Short, long, and any other variants are recognized.
     *
     * @param propertyAlias the property name to be matched.  The name
     * is compared using "loose matching" as described in
     * PropertyAliases.txt.
     *
     * @return a UProperty enum.
     *
     * @exception IllegalArgumentException thrown if propertyAlias
     * is not recognized.
     *
     * @see UProperty
     * @since ICU 2.4
     * @draft 2.4
     */
    public static int getPropertyEnum(String propertyAlias) {
        return PNAMES_.getPropertyEnum(propertyAlias);
    }

    /**
     * Return the Unicode name for a given property value, as given in
     * the Unicode database file PropertyValueAliases.txt.  Most
     * values have more than one name.  The nameChoice determines
     * which one is returned.
     *
     * @param property UProperty selector in the range
     * UProperty.INT_START <= x < UProperty.INT_LIMIT or
     * UProperty.BINARY_START <= x < UProperty.BINARY_LIMIT.
     *
     * @param value selector for a value for the given property.  In
     * general, valid values range from 0 up to some maximum.  There
     * are a few exceptions: (1.) UProperty.BLOCK values begin at the
     * non-zero value BASIC_LATIN.getID().  (2.)
     * UProperty.CANONICAL_COMBINING_CLASS values are not contiguous
     * and range from 0..240.  (3.)  UProperty.GENERAL_CATEGORY values
     * are mask values produced by left-shifting 1 by
     * UCharacter.getType().  This allows grouped categories such as
     * [:L:] to be represented.  Mask values are non-contiguous.
     *
     * @param nameChoice UProperty.NameChoice selector for which name
     * to get.  All values have a long name.  Most have a short name,
     * but some do not.  Unicode allows for additional names; if
     * present these will be returned by UProperty.NameChoice.LONG + i,
     * where i=1, 2,...
     *
     * @return a name, or null if Unicode explicitly defines no name
     * ("n/a") for a given property/value/nameChoice.  If a given
     * nameChoice throws an exception, then all larger values of
     * nameChoice will throw an exception.  If null is returned for a
     * given nameChoice, then other nameChoice values may return
     * non-null results.
     *
     * @exception IllegalArgumentException thrown if property, value,
     * or nameChoice are invalid.
     *
     * @see UProperty
     * @see UProperty.NameChoice
     * @since ICU 2.4
     * @draft 2.4
     */
    public static String getPropertyValueName(int property,
                                              int value,
                                              int nameChoice) {
        return PNAMES_.getPropertyValueName(property, value, nameChoice);
    }

    /**
     * Return the property value integer for a given value name, as
     * specified in the Unicode database file PropertyValueAliases.txt.
     * Short, long, and any other variants are recognized.
     *
     * @param prop the UProperty selector for the property to which
     * the given value alias belongs.  It should be in the range
     * UProperty.INT_START <= x < UProperty.INT_LIMIT or
     * UProperty.BINARY_START <= x < UProperty.BINARY_LIMIT; only
     * these properties define value names and enums.
     *
     * @param valueAlias the value name to be matched.  The name is
     * compared using "loose matching" as described in
     * PropertyValueAliases.txt.
     *
     * @return a value integer.  Note: UProperty.GENERAL_CATEGORY
     * values are mask values produced by left-shifting 1 by
     * UCharacter.getType().  This allows grouped categories such as
     * [:L:] to be represented.
     *
     * @see UProperty
     * @since ICU 2.4
     * @draft 2.4
     */
    public static int getPropertyValueEnum(int property,
                                           String valueAlias) {
        return PNAMES_.getPropertyValueEnum(property, valueAlias);
    }
      
    /**
    * Returns a code point corresponding to the two UTF16 characters.
    * @param lead the lead char
    * @param trail the trail char
    * @return code point if surrogate characters are valid.
    * @exception IllegalArgumentException thrown when argument characters do
    *            not form a valid codepoint
    */
    public static int getCodePoint(char lead, char trail) 
    {
        if (lead >= UTF16.LEAD_SURROGATE_MIN_VALUE && 
	        lead <= UTF16.LEAD_SURROGATE_MAX_VALUE &&
            trail >= UTF16.TRAIL_SURROGATE_MIN_VALUE && 
	        trail <= UTF16.TRAIL_SURROGATE_MAX_VALUE) {
            return UCharacterProperty.getRawSupplementary(lead, trail);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }
      
    /**
    * Returns the code point corresponding to the UTF16 character.
    * @param char16 the UTF16 character
    * @return code point if argument is a valid character.
    * @exception IllegalArgumentException thrown when char16 is not a valid
    *            codepoint
    */
    public static int getCodePoint(char char16) 
    {
        if (UCharacter.isLegal(char16)) {
            return char16;
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }
      
    /**
    * Gets uppercase version of the argument string. 
    * Casing is dependent on the default locale and context-sensitive.
    * @param str source string to be performed on
    * @return uppercase version of the argument string
    */
    public static String toUpperCase(String str)
    {
        return toUpperCase(Locale.getDefault(), str);
    }
      
    /**
    * Gets lowercase version of the argument string. 
    * Casing is dependent on the default locale and context-sensitive
    * @param str source string to be performed on
    * @return lowercase version of the argument string
    */
    public static String toLowerCase(String str)
    {
        return toLowerCase(Locale.getDefault(), str);
    }
    
    /**
    * <p>Gets the titlecase version of the argument string.</p>
    * <p>Position for titlecasing is determined by the argument break 
    * iterator, hence the user can customized his break iterator for 
    * a specialized titlecasing. In this case only the forward iteration 
    * needs to be implemented.
    * If the break iterator passed in is null, the default Unicode algorithm
    * will be used to determine the titlecase positions.
    * </p>
    * <p>Only positions returned by the break iterator will be title cased,
    * character in between the positions will all be in lower case.</p>
    * <p>Casing is dependent on the default locale and context-sensitive</p>
    * @param str source string to be performed on
    * @param breakiter break iterator to determine the positions in which
    *        the character should be title cased.
    * @return lowercase version of the argument string
    * @draft 2.1
    */
    public static String toTitleCase(String str, BreakIterator breakiter)
    {
        return toTitleCase(Locale.getDefault(), str, breakiter);
    }
      
    /**
    * Gets uppercase version of the argument string. 
    * Casing is dependent on the argument locale and context-sensitive.
    * @param locale which string is to be converted in
    * @param str source string to be performed on
    * @return uppercase version of the argument string
    */
    public static String toUpperCase(Locale locale, String str)
    {
    	if (locale == null) {
    		locale = Locale.getDefault();
    	}
        return PROPERTY_.toUpperCase(locale, str, 0, str.length());
    }
      
    /**
    * Gets lowercase version of the argument string. 
    * Casing is dependent on the argument locale and context-sensitive
    * @param locale which string is to be converted in
    * @param str source string to be performed on
    * @return lowercase version of the argument string
    */
    public static String toLowerCase(Locale locale, String str)
    {
    	int length = str.length();
    	StringBuffer result = new StringBuffer(length);
    	if (locale == null) {
    		locale = Locale.getDefault();
    	}
        PROPERTY_.toLowerCase(locale, str, 0, length, result);
        return result.toString();
    }
    
    /**
    * <p>Gets the titlecase version of the argument string.</p>
    * <p>Position for titlecasing is determined by the argument break 
    * iterator, hence the user can customized his break iterator for 
    * a specialized titlecasing. In this case only the forward iteration 
    * needs to be implemented.
    * If the break iterator passed in is null, the default Unicode algorithm
    * will be used to determine the titlecase positions.
    * </p>
    * <p>Only positions returned by the break iterator will be title cased,
    * character in between the positions will all be in lower case.</p>
    * <p>Casing is dependent on the argument locale and context-sensitive</p>
    * @param locale which string is to be converted in
    * @param str source string to be performed on
    * @param breakiter break iterator to determine the positions in which
    *        the character should be title cased.
    * @return lowercase version of the argument string
    * @draft 2.1
    */
    public static String toTitleCase(Locale locale, String str, 
                                     BreakIterator breakiter)
    {
        if (breakiter == null) {
        	if (locale == null) {
        		locale = Locale.getDefault();
        	}
            breakiter = BreakIterator.getWordInstance(locale);
        }
        return PROPERTY_.toTitleCase(locale, str, breakiter);
    }
    
    /**
    * The given character is mapped to its case folding equivalent according to
    * UnicodeData.txt and CaseFolding.txt; if the character has no case folding 
    * equivalent, the character itself is returned.
    * Only "simple", single-code point case folding mappings are used.
    * For "full", multiple-code point mappings use the API 
    * foldCase(String str, boolean defaultmapping).
    * @param ch             the character to be converted
    * @param defaultmapping Indicates if all mappings defined in CaseFolding.txt 
    *                       is to be used, otherwise the mappings for dotted I 
    *                       and dotless i marked with 'I' in CaseFolding.txt will 
    *                       be skipped.
    * @return               the case folding equivalent of the character, if any;
    *                       otherwise the character itself.
    * @see                  #foldCase(String, boolean)
    */
    public static int foldCase(int ch, boolean defaultmapping)
    {
        // Some special cases are hardcoded because their conditions cannot be
        // parsed and processed from CaseFolding.txt.
        // Unicode 3.2 CaseFolding.txt specifies for its status field:
        // # C: common case folding, common mappings shared by both simple and 
        // full mappings.
        // # F: full case folding, mappings that cause strings to grow in 
        // length. Multiple characters are separated by spaces.
        // # S: simple case folding, mappings to single characters where 
        // different from F.
        // # T: special case for uppercase I and dotted uppercase I
        // #    - For non-Turkic languages, this mapping is normally not used.
        // #    - For Turkic languages (tr, az), this mapping can be used 
        // instead of the normal mapping for these characters.
        // # Usage:
        // #  A. To do a simple case folding, use the mappings with status 
        // C + S.
        // #  B. To do a full case folding, use the mappings with status C + F.
        // #    The mappings with status T can be used or omitted depending on 
        // the desired case-folding behavior. 
        // (The default option is to exclude them.)
        // Unicode 3.2 has 'T' mappings as follows:
        // 0049; T; 0131; # LATIN CAPITAL LETTER I
        // 0130; T; 0069; # LATIN CAPITAL LETTER I WITH DOT ABOVE
        // while the default mappings for these code points are:
        // 0049; C; 0069; # LATIN CAPITAL LETTER I
        // 0130; F; 0069 0307; # LATIN CAPITAL LETTER I WITH DOT ABOVE
        // U+0130 is otherwise lowercased to U+0069 (UnicodeData.txt).
        // In case this code is used with CaseFolding.txt from an older version 
        // of Unicode where CaseFolding.txt contains mappings with a status of 
        // 'I' that have the opposite polarity ('I' mappings are included by 
        // default but excluded for Turkic), we must also hardcode the Unicode 
        // 3.2 mappings for the code points with 'I' mappings. 
        // Unicode 3.1.1 has 'I' mappings for U+0130 and U+0131.
        // Unicode 3.2 has a 'T' mapping for U+0130, and lowercases U+0131 to 
        // itself (see UnicodeData.txt).
        // when ch is out of bounds getProperty == 0
        int props = PROPERTY_.getProperty(ch);
        if (isNotExceptionIndicator(props)) {
            int type = UCharacterProperty.TYPE_MASK & props;
            if (type == UCharacterCategory.UPPERCASE_LETTER ||
                type == UCharacterCategory.TITLECASE_LETTER) {
                return ch + UCharacterProperty.getSignedValue(props);
            }
        } 
        else {
            int index = UCharacterProperty.getExceptionIndex(props);
            if (PROPERTY_.hasExceptionValue(index, 
                                      UCharacterProperty.EXC_CASE_FOLDING_)) {
                int exception = PROPERTY_.getException(index, 
                                      UCharacterProperty.EXC_CASE_FOLDING_);
                if (exception != 0) {
                    int foldedcasech = 
                         PROPERTY_.getFoldCase(exception & LAST_CHAR_MASK_);
                    if (foldedcasech != 0){
                        return foldedcasech;
                    }
                }
                else {
                    // special case folding mappings, hardcoded
                    if (defaultmapping) { 
                        // default mappings
                        if (ch == 0x49 || ch == 0x130) { 
                            // 0049; C; 0069; # LATIN CAPITAL LETTER I */
                            // no simple default mapping for U+0130, 
                            // use UnicodeData.txt
                            return UCharacterProperty.LATIN_SMALL_LETTER_I_;
                        } 
                    } 
                    else {
                        // Turkic mappings 
                        if (ch == 0x49) {
                            // 0049; T; 0131; # LATIN CAPITAL LETTER I
                            return 0x131;
                        } 
                        else if (ch == 0x130) {
                            // 0130; T; 0069; 
                            // # LATIN CAPITAL LETTER I WITH DOT ABOVE
                            return 0x69;
                        }
                    }
                    // return ch itself because it is excluded from case folding
                    return ch;
                }                                  
            }
            if (PROPERTY_.hasExceptionValue(index, 
                                       UCharacterProperty.EXC_LOWERCASE_)) {  
                // not else! - allow to fall through from above
                return PROPERTY_.getException(index, 
                                         UCharacterProperty.EXC_LOWERCASE_);
            }
        }
            
        return ch; // no mapping - return the character itself
    }

    /**
    * The given string is mapped to its case folding equivalent according to
    * UnicodeData.txt and CaseFolding.txt; if any character has no case folding 
    * equivalent, the character itself is returned.
    * "Full", multiple-code point case folding mappings are returned here.
    * For "simple" single-code point mappings use the API 
    * foldCase(int ch, boolean defaultmapping).
    * @param str            the String to be converted
    * @param defaultmapping Indicates if all mappings defined in CaseFolding.txt 
    *                       is to be used, otherwise the mappings for dotted I 
    *                       and dotless i marked with 'I' in CaseFolding.txt will 
    *                       be skipped.
    * @return               the case folding equivalent of the character, if any;
    *                       otherwise the character itself.
    * @see                  #foldCase(int, boolean)
    */
    public static String foldCase(String str, boolean defaultmapping)
    {
        int          size   = str.length();
        StringBuffer result = new StringBuffer(size);
        int          offset  = 0;
        int          ch;

        // case mapping loop
        while (offset < size) {
            ch = UTF16.charAt(str, offset);
            offset += UTF16.getCharCount(ch);
            int props = PROPERTY_.getProperty(ch);
            if (isNotExceptionIndicator(props)) {
                int type = UCharacterProperty.TYPE_MASK & props;
                if (type == UCharacterCategory.UPPERCASE_LETTER ||
                    type == UCharacterCategory.TITLECASE_LETTER) {
                    ch += UCharacterProperty.getSignedValue(props);
                }
            }  
            else {
                int index = UCharacterProperty.getExceptionIndex(props);
                if (PROPERTY_.hasExceptionValue(index, 
                                    UCharacterProperty.EXC_CASE_FOLDING_)) {
                    int exception = PROPERTY_.getException(index, 
                                      UCharacterProperty.EXC_CASE_FOLDING_);                             
                    if (exception != 0) {
                        PROPERTY_.getFoldCase(exception & LAST_CHAR_MASK_, 
                                             exception >> SHIFT_24_, result);
                    } 
                    else {
                        // special case folding mappings, hardcoded
                        if (ch != 0x49 && ch != 0x130) {
                            // return ch itself because there is no special 
                            // mapping for it
                            UTF16.append(result, ch);
                            continue;
                        }
                        if (defaultmapping) {
                            // default mappings
                            if (ch == 0x49) {
                                // 0049; C; 0069; # LATIN CAPITAL LETTER I
                                result.append(
                                    UCharacterProperty.LATIN_SMALL_LETTER_I_);
                            }
                            else if (ch == 0x130) {
                                // 0130; F; 0069 0307; 
                                // # LATIN CAPITAL LETTER I WITH DOT ABOVE
                                result.append(
                                    UCharacterProperty.LATIN_SMALL_LETTER_I_);
                                result.append((char)0x307);
                            }
                        }
                        else {
                            // Turkic mappings
                            if (ch == 0x49) {
                                // 0049; T; 0131; # LATIN CAPITAL LETTER I
                                result.append((char)0x131);
                            } 
                            else if (ch == 0x130) {
                                // 0130; T; 0069; 
                                // # LATIN CAPITAL LETTER I WITH DOT ABOVE
                                result.append(
                                    UCharacterProperty.LATIN_SMALL_LETTER_I_);
                            }
                        }
                    }
                    // do not fall through to the output of c
                    continue;
                } 
                else {
                    if (PROPERTY_.hasExceptionValue(index, 
                                         UCharacterProperty.EXC_LOWERCASE_)) {
                        ch = PROPERTY_.getException(index, 
                                          UCharacterProperty.EXC_LOWERCASE_);
                    }
                }
                
            }

            // handle 1:1 code point mappings from UnicodeData.txt
            UTF16.append(result, ch);
        }
        
        return result.toString();
    }
    
    /**
    * Return numeric value of Han code points.
    * <br> This returns the value of Han 'numeric' code points,
    * including those for zero, ten, hundred, thousand, ten thousand,
    * and hundred million.  Unicode does not consider these to be
    * numeric. This includes both the standard and 'checkwriting'
    * characters, the 'big circle' zero character, and the standard
    * zero character.
    * @draft
    * @param ch code point to query
    * @return value if it is a Han 'numeric character,' otherwise return -1.  
    */
    public static int getHanNumericValue(int ch)
    {
        switch(ch)
        {
        case IDEOGRAPHIC_NUMBER_ZERO_ :
        case CJK_IDEOGRAPH_COMPLEX_ZERO_ :
            return 0; // Han Zero
        case CJK_IDEOGRAPH_FIRST_ :
        case CJK_IDEOGRAPH_COMPLEX_ONE_ :
            return 1; // Han One
        case CJK_IDEOGRAPH_SECOND_ :
        case CJK_IDEOGRAPH_COMPLEX_TWO_ :
            return 2; // Han Two
        case CJK_IDEOGRAPH_THIRD_ :
        case CJK_IDEOGRAPH_COMPLEX_THREE_ :
            return 3; // Han Three
        case CJK_IDEOGRAPH_FOURTH_ :
        case CJK_IDEOGRAPH_COMPLEX_FOUR_ :
            return 4; // Han Four
        case CJK_IDEOGRAPH_FIFTH_ :
        case CJK_IDEOGRAPH_COMPLEX_FIVE_ :
            return 5; // Han Five
        case CJK_IDEOGRAPH_SIXTH_ :
        case CJK_IDEOGRAPH_COMPLEX_SIX_ :
            return 6; // Han Six
        case CJK_IDEOGRAPH_SEVENTH_ :
        case CJK_IDEOGRAPH_COMPLEX_SEVEN_ :
            return 7; // Han Seven
        case CJK_IDEOGRAPH_EIGHTH_ : 
        case CJK_IDEOGRAPH_COMPLEX_EIGHT_ :
            return 8; // Han Eight
        case CJK_IDEOGRAPH_NINETH_ :
        case CJK_IDEOGRAPH_COMPLEX_NINE_ :
            return 9; // Han Nine
        case CJK_IDEOGRAPH_TEN_ :
        case CJK_IDEOGRAPH_COMPLEX_TEN_ :
            return 10;
        case CJK_IDEOGRAPH_HUNDRED_ :
        case CJK_IDEOGRAPH_COMPLEX_HUNDRED_ :
            return 100;
        case CJK_IDEOGRAPH_THOUSAND_ :
        case CJK_IDEOGRAPH_COMPLEX_THOUSAND_ :
            return 1000;
        case CJK_IDEOGRAPH_TEN_THOUSAND_ :
            return 10000;
        case CJK_IDEOGRAPH_HUNDRED_MILLION_ :
            return 100000000;
        }
        return -1; // no value
    }
    
    /**
    * <p>Gets an iterator for character types, iterating over codepoints.</p>
    * Example of use:<br>
    * <pre>
    * RangeValueIterator iterator = UCharacter.getTypeIterator();
    * RangeValueIterator.Element element = new RangeValueIterator.Element();
    * while (iterator.next(element)) {
    *     System.out.println("Codepoint \\u" + 
    *                        Integer.toHexString(element.start) + 
    *                        " to codepoint \\u" +
    *                        Integer.toHexString(element.limit - 1) + 
    *                        " has the character type " + 
    *                        element.value);
    * }
    * </pre>
    * @return an iterator 
    * @draft 2.1
    */
    public static RangeValueIterator getTypeIterator()
    {
        return new UCharacterTypeIterator(PROPERTY_);
    }

	/**
    * <p>Gets an iterator for character names, iterating over codepoints.</p>
    * <p>This API only gets the iterator for the modern, most up-to-date 
    * Unicode names. For older 1.0 Unicode names use get1_0NameIterator() or
    * for extended names use getExtendedNameIterator().</p>
    * Example of use:<br>
    * <pre>
    * ValueIterator iterator = UCharacter.getNameIterator();
    * ValueIterator.Element element = new ValueIterator.Element();
    * while (iterator.next(element)) {
    *     System.out.println("Codepoint \\u" + 
    *                        Integer.toHexString(element.codepoint) +
    *                        " has the name " + (String)element.value);
    * }
    * </pre>
    * <p>The maximal range which the name iterator iterates is from 
    * UCharacter.MIN_VALUE to UCharacter.MAX_VALUE.</p>
    * @return an iterator 
    * @draft 2.1
    */
    public static ValueIterator getNameIterator()
    {
        return new UCharacterNameIterator(NAME_,
                                   UCharacterNameChoice.UNICODE_CHAR_NAME);
    }
    
    /**
    * <p>Gets an iterator for character names, iterating over codepoints.</p>
    * <p>This API only gets the iterator for the older 1.0 Unicode names. 
    * For modern, most up-to-date Unicode names use getNameIterator() or
    * for extended names use getExtendedNameIterator().</p>
    * Example of use:<br>
    * <pre>
    * ValueIterator iterator = UCharacter.get1_0NameIterator();
    * ValueIterator.Element element = new ValueIterator.Element();
    * while (iterator.next(element)) {
    *     System.out.println("Codepoint \\u" + 
    *                        Integer.toHexString(element.codepoint) +
    *                        " has the name " + (String)element.value);
    * }
    * </pre>
    * <p>The maximal range which the name iterator iterates is from 
    * @return an iterator 
    * @draft 2.1
    */
    public static ValueIterator getName1_0Iterator()
    {
        return new UCharacterNameIterator(NAME_,
                                 UCharacterNameChoice.UNICODE_10_CHAR_NAME);
    }
    
    /**
    * <p>Gets an iterator for character names, iterating over codepoints.</p>
    * <p>This API only gets the iterator for the extended names. 
    * For modern, most up-to-date Unicode names use getNameIterator() or
    * for older 1.0 Unicode names use get1_0NameIterator().</p>
    * Example of use:<br>
    * <pre>
    * ValueIterator iterator = UCharacter.getExtendedNameIterator();
    * ValueIterator.Element element = new ValueIterator.Element();
    * while (iterator.next(element)) {
    *     System.out.println("Codepoint \\u" + 
    *                        Integer.toHexString(element.codepoint) +
    *                        " has the name " + (String)element.value);
    * }
    * </pre>
    * <p>The maximal range which the name iterator iterates is from 
    * @return an iterator 
    * @draft 2.1
    */
    public static ValueIterator getExtendedNameIterator()
    {
        return new UCharacterNameIterator(NAME_,
                                 UCharacterNameChoice.EXTENDED_CHAR_NAME);
    }
    
    /**
     * <p>Get the "age" of the code point.</p>
     * <p>The "age" is the Unicode version when the code point was first
     * designated (as a non-character or for Private Use) or assigned a 
     * character.
     * <p>This can be useful to avoid emitting code points to receiving 
     * processes that do not accept newer characters.</p>
     * <p>The data is from the UCD file DerivedAge.txt.</p>
     * @param ch The code point.
     * @return the Unicode version number
     * @draft ICU 2.1
     */
    public static VersionInfo getAge(int ch) 
    {
    	if (ch < MIN_VALUE || ch > MAX_VALUE) {
    		throw new IllegalArgumentException("Codepoint out of bounds");
    	}
    	return PROPERTY_.getAge(ch);
    }
    
    /**
	 * <p>Check a binary Unicode property for a code point.</p> 
	 * <p>Unicode, especially in version 3.2, defines many more properties 
	 * than the original set in UnicodeData.txt.</p>
	 * <p>This API is intended to reflect Unicode properties as defined in 
	 * the Unicode Character Database (UCD) and Unicode Technical Reports 
	 * (UTR).</p>
	 * <p>For details about the properties see 
	 * <a href=http://www.unicode.org/>http://www.unicode.org/</a>.</p>
	 * <p>For names of Unicode properties see the UCD file 
	 * PropertyAliases.txt.</p>
	 * <p>This API does not check the validity of the codepoint.</p>
	 * <p>Important: If ICU is built with UCD files from Unicode versions 
	 * below 3.2, then properties marked with "new" are not or 
	 * not fully available.</p>
	 * @param codepoint Code point to test.
	 * @param property selector constant from com.ibm.icu.lang.UProperty, 
	 *        identifies which binary property to check.
	 * @return true or false according to the binary Unicode property value 
	 *         for ch. Also false if property is out of bounds or if the 
	 *         Unicode version does not have data for the property at all, or 
	 *         not for this code point.
	 * @see com.ibm.icu.lang.UProperty
	 * @draft ICU 2.1
	 */
	public static boolean hasBinaryProperty(int ch, int property) 
	{
		if (ch < MIN_VALUE || ch > MAX_VALUE) {
    		throw new IllegalArgumentException("Codepoint out of bounds");
    	}
    	return PROPERTY_.hasBinaryProperty(ch, property);
	}
	
	/**
	 * <p>Check if a code point has the Alphabetic Unicode property.</p> 
	 * <p>Same as UCharacter.hasBinaryProperty(ch, UProperty.ALPHABETIC).</p>
	 * <p>Different from UCharacter.isLetter(ch)!</p> 
	 * @draft ICU 2.1
	 * @param ch codepoint to be tested
	 */
	public static boolean isUAlphabetic(int ch)
	{
		return hasBinaryProperty(ch, UProperty.ALPHABETIC);
	}

	/**
	 * <p>Check if a code point has the Lowercase Unicode property.</p>
	 * <p>Same as UCharacter.hasBinaryProperty(ch, UProperty.LOWERCASE).</p>
	 * <p>This is different from UCharacter.isLowerCase(ch)!</p>
	 * @param ch codepoint to be tested
	 * @draft ICU 2.1
	 */
	public static boolean isULowercase(int ch) 
	{
		return hasBinaryProperty(ch, UProperty.LOWERCASE);
	}

	/**
	 * <p>Check if a code point has the Uppercase Unicode property.</p>
	 * <p>Same as UCharacter.hasBinaryProperty(ch, UProperty.UPPERCASE).</p>
	 * <p>This is different from UCharacter.isUpperCase(ch)!</p>
	 * @param ch codepoint to be tested
	 * @draft ICU 2.1
	 */
	public static boolean isUUppercase(int ch) 
	{
		return hasBinaryProperty(ch, UProperty.UPPERCASE);
	}

	/**
	 * <p>Check if a code point has the White_Space Unicode property.</p>
	 * <p>Same as UCharacter.hasBinaryProperty(ch, UProperty.WHITE_SPACE).</p>
	 * <p>This is different from both UCharacter.isSpace(ch) and 
	 * UCharacter.isWhitespace(ch)!</p>
	 * @param ch codepoint to be tested
	 * @draft ICU 2.1
	 */
	public static boolean isUWhiteSpace(int ch) 
	{
		return hasBinaryProperty(ch, UProperty.WHITE_SPACE);
	}
    
    /**
     * <p>Gets the property value for an Unicode property type of a code point. 
     * Also returns binary property values.</p>
     * <p>Unicode, especially in version 3.2, defines many more properties than 
     * the original set in UnicodeData.txt.</p>
     * <p>The properties APIs are intended to reflect Unicode properties as 
     * defined in the Unicode Character Database (UCD) and Unicode Technical 
     * Reports (UTR). For details about the properties see 
     * http://www.unicode.org/.</p>
     * <p>For names of Unicode properties see the UCD file PropertyAliases.txt.
     * </p>
     * <pre>
     * Sample usage:
     * int ea = UCharacter.getIntPropertyValue(c, UProperty.EAST_ASIAN_WIDTH);
     * int ideo = UCharacter.getIntPropertyValue(c, UProperty.IDEOGRAPHIC);
     * boolean b = (ideo == 1) ? true : false; 
     * </pre>
     * @param ch code point to test.
     * @param which UProperty selector constant, identifies which binary 
     *        property to check. Must be UProperty.BINARY_START &lt;= which
     *        &lt; UProperty.BINARY_LIMIT or UProperty.INT_START &lt;= which
     *        &lt; UProperty.INT_LIMIT.
     * @return numeric value that is directly the property value or,
     *         for enumerated properties, corresponds to the numeric value of 
     *         the enumerated constant of the respective property value 
     *         enumeration type (cast to enum type if necessary).
     *         Returns 0 or 1 (for false / true) for binary Unicode properties.
     *         Returns 0 if which is out of bounds or if the Unicode version
     *         does not have data for the property at all, or not for this code 
     *         point.
     * @see UProperty
     * @see #hasBinaryProperty
     * @see #getIntPropertyMinValue
     * @see #getIntPropertyMaxValue
     * @see #getUnicodeVersion
     * @draft ICU 2.4
     */
    public static int getIntPropertyValue(int ch, int type)
    {
        if (type < UProperty.BINARY_START) {
            return 0; // undefined
        } 
        else if (type < UProperty.BINARY_LIMIT) {
            return hasBinaryProperty(ch, type) ? 1 : 0;
        } 
        else if (type < UProperty.INT_START) {
            return 0; // undefined
        } 
        else if (type < UProperty.INT_LIMIT) {
            int result = 0;
            switch (type) {
            case UProperty.BIDI_CLASS:
                return getDirection(ch);
            case UProperty.BLOCK:
                return UnicodeBlock.of(ch).getID();
            case UProperty.CANONICAL_COMBINING_CLASS:
                return getCombiningClass(ch);
            case UProperty.DECOMPOSITION_TYPE:
                return PROPERTY_.getAdditional(ch, 2) 
                       & DECOMPOSITION_TYPE_MASK_;
            case UProperty.EAST_ASIAN_WIDTH:
                return (PROPERTY_.getAdditional(ch, 0)
                       & EAST_ASIAN_MASK_) >> EAST_ASIAN_SHIFT_;
            case UProperty.GENERAL_CATEGORY:
                return getType(ch);
            case UProperty.JOINING_GROUP:
                return (PROPERTY_.getAdditional(ch, 2) 
                       & JOINING_GROUP_MASK_) >> JOINING_GROUP_SHIFT_;
            case UProperty.JOINING_TYPE:
                // ArabicShaping.txt:
                // Note: Characters of joining type T and most characters of 
                // joining type U are not explicitly listed in this file.
                // Characters of joining type T can [be] derived by the following formula:
                //   T = Mn + Cf - ZWNJ - ZWJ
                result = (PROPERTY_.getAdditional(ch, 2) 
                         & JOINING_TYPE_MASK_) >> JOINING_TYPE_SHIFT_;
                if (result == 0 && ch != ZERO_WIDTH_NON_JOINER_ 
                                && ch != ZERO_WIDTH_JOINER_) {
                    int t = getType(ch);
                    if (t == UCharacterCategory.NON_SPACING_MARK
                        || t == UCharacterCategory.FORMAT) {
                        result = JoiningType.TRANSPARENT;
                    }
                }
                return result;
            case UProperty.LINE_BREAK:
                /*
                 * LineBreak.txt:
                 *  - Assigned characters that are not listed explicitly are given the value
                 *    "AL".
                 *  - Unassigned characters are given the value "XX".
                 * ...
                 * E000..F8FF;XX # <Private Use, First>..<Private Use, Last>
                 * F0000..FFFFD;XX # <Plane 15 Private Use, First>..<Plane 15 Private Use, Last>
                 * 100000..10FFFD;XX # <Plane 16 Private Use, First>..<Plane 16 Private Use, Last>
                 */
                result = (PROPERTY_.getAdditional(ch, 0) 
                             & LINE_BREAK_MASK_) >> LINE_BREAK_SHIFT_;
                if (result == 0) {
                    int t = getType(ch);
                    if (t != UCharacterCategory.UNASSIGNED 
                        && t != UCharacterCategory.PRIVATE_USE) {
                        result = LineBreak.ALPHABETIC;
                    }
                }
                return result;
            case UProperty.NUMERIC_TYPE:
                return getNumericType(PROPERTY_.getProperty(ch));
            case UProperty.SCRIPT:
                return UScript.getScript(ch);
            }
        } 
        return 0; // undefined
    }
    
    /**
     * Get the minimum value for an integer/binary Unicode property type.
     * Can be used together with UCharacter.getIntPropertyMaxValue(int)
     * to allocate arrays of com.ibm.icu.text.UnicodeSet or similar.
     * @param which UProperty selector constant, identifies which binary 
     *        property to check. Must be UProperty.BINARY_START &lt;= which
     *        &lt; UProperty.BINARY_LIMIT or UProperty.INT_START &lt;= which
     *        &lt; UProperty.INT_LIMIT.
     * @return Minimum value returned by UCharacter.getIntPropertyValue(int) 
     *         for a Unicode property. 0 if the property 
     *         selector is out of range.
     * @see UProperty
     * @see #hasBinaryProperty
     * @see #getUnicodeVersion
     * @see #getIntPropertyMaxValue
     * @see #getIntPropertyValue
     * @draft ICU 2.4
     */
    public static int getIntPropertyMinValue(int type)
    {
        switch (type) {
            case UProperty.BLOCK:
                return UnicodeBlock.INVALID_CODE.getID();
        }
        return 0; // undefined; and: all other properties have a minimum value 
                  // of 0
    }

    
    /**
     * Get the maximum value for an integer/binary Unicode property.
     * Can be used together with UCharacter.getIntPropertyMinValue(int)
     * to allocate arrays of com.ibm.icu.text.UnicodeSet or similar.
     * Examples for min/max values (for Unicode 3.2):
     * <ul>
     * <li> UProperty.BIDI_CLASS:    0/18 (UCharacterDirection.LEFT_TO_RIGHT/UCharacterDirection.BOUNDARY_NEUTRAL)
     * <li> UProperty.SCRIPT:        0/45 (UScript.COMMON/UScript.TAGBANWA)
     * <li> UProperty.IDEOGRAPHIC:   0/1  (false/true)
     * </ul>
     * For undefined UProperty constant values, min/max values will be 0/-1.
     * @param which UProperty selector constant, identifies which binary 
     *        property to check. Must be UProperty.BINARY_START &lt;= which
     *        &lt; UProperty.BINARY_LIMIT or UProperty.INT_START &lt;= which
     *        &lt; UProperty.INT_LIMIT.
     * @return Maximum value returned by u_getIntPropertyValue for a Unicode property.
     *         <= 0 if the property selector is out of range.
     * @see UProperty
     * @see #hasBinaryProperty
     * @see #getUnicodeVersion
     * @see #getIntPropertyMaxValue
     * @see #getIntPropertyValue
     * @draft ICU 2.4
     */
    public static int getIntPropertyMaxValue(int type)
    { 
        if (type < UProperty.BINARY_START) {
            return -1; // undefined
        } 
        else if (type < UProperty.BINARY_LIMIT) {
            return 1; // maximum TRUE for all binary properties
        } 
        else if (type < UProperty.INT_START) {
            return -1; // undefined
        } 
        else if (type < UProperty.INT_LIMIT) {
            int max = 0;
            switch (type) {
            case UProperty.BIDI_CLASS:
                return UCharacterDirection.CHAR_DIRECTION_COUNT - 1;
            case UProperty.BLOCK:
                max = (PROPERTY_.getMaxBlockScriptValues()
                      & BLOCK_MASK_) >> BLOCK_SHIFT_;
                if (max == 0) {
                    max = UnicodeBlock.COUNT - 1;
                }
                return max;
            case UProperty.CANONICAL_COMBINING_CLASS:
                return 0xff; // TODO do we need to be more precise, 
                             // getting the actual maximum?
            case UProperty.DECOMPOSITION_TYPE:
                return DecompositionType.COUNT - 1;
            case UProperty.EAST_ASIAN_WIDTH:
                return EastAsianWidth.COUNT - 1;
            case UProperty.GENERAL_CATEGORY:
                return UCharacterCategory.CHAR_CATEGORY_COUNT - 1;
            case UProperty.JOINING_GROUP:
                return JoiningGroup.COUNT - 1;
            case UProperty.JOINING_TYPE:
                return JoiningType.COUNT - 1;
            case UProperty.LINE_BREAK:
                return LineBreak.COUNT - 1;
            case UProperty.NUMERIC_TYPE:
                return NumericType.COUNT - 1;
            case UProperty.SCRIPT:
                max = PROPERTY_.getMaxBlockScriptValues() & SCRIPT_MASK_;
                if (max == 0) {
                    max = UScript.CODE_LIMIT - 1;
                }
                return max;
            }
        } 
        return -1; // undefined
    }

    // protected data members --------------------------------------------
    
    /**
    * Database storing the sets of character name
    */
    protected static final UCharacterName NAME_;

    /**
     * Singleton object encapsulating the imported pnames.icu property aliases
     */
    protected static final UPropertyAliases PNAMES_;
      
    // block to initialise name database and unicode 1.0 data 
    static
    {
        try
        {
            NAME_ = UCharacterName.getInstance();
            PNAMES_ = new UPropertyAliases();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
        
    // private variables -------------------------------------------------
    
    /**
    * Database storing the sets of character property
    */
    private static final UCharacterProperty PROPERTY_;
                                                    
	// block to initialise character property database
    static
    {
        try
        {
            PROPERTY_ = UCharacterProperty.getInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }                                                    
   
    /**
    * To get the last character out from a data type
    */
    private static final int LAST_CHAR_MASK_ = 0xFFFF;
      
    /**
    * To get the last byte out from a data type
    */
    private static final int LAST_BYTE_MASK_ = 0xFF;
      
    /**
    * Shift 16 bits
    */
    private static final int SHIFT_16_ = 16;
      
    /**
    * Shift 24 bits
    */
    private static final int SHIFT_24_ = 24;  
    
    /**
    * Decimal radix
    */
    private static final int DECIMAL_RADIX_ = 10;
      
    /**
    * No break space code point
    */
    private static final int NO_BREAK_SPACE_ = 0xA0;
      
    /**
    * Narrow no break space code point
    */
    private static final int NARROW_NO_BREAK_SPACE_ = 0x202F;
      
    /**
    * Zero width no break space code point
    */
    private static final int ZERO_WIDTH_NO_BREAK_SPACE_ = 0xFEFF;
      
    /**
    * Ideographic number zero code point
    */
    private static final int IDEOGRAPHIC_NUMBER_ZERO_ = 0x3007;
            
    /**
    * CJK Ideograph, First code point
    */
    private static final int CJK_IDEOGRAPH_FIRST_ = 0x4e00;
      
    /**
    * CJK Ideograph, Second code point
    */
    private static final int CJK_IDEOGRAPH_SECOND_ = 0x4e8c;
            
    /**
    * CJK Ideograph, Third code point
    */
    private static final int CJK_IDEOGRAPH_THIRD_ = 0x4e09;
      
    /**
    * CJK Ideograph, Fourth code point
    */
    private static final int CJK_IDEOGRAPH_FOURTH_ = 0x56d8;
      
    /**
    * CJK Ideograph, FIFTH code point
    */
    private static final int CJK_IDEOGRAPH_FIFTH_ = 0x4e94;
      
    /**
    * CJK Ideograph, Sixth code point
    */
    private static final int CJK_IDEOGRAPH_SIXTH_ = 0x516d;
            
    /**
    * CJK Ideograph, Seventh code point
    */
    private static final int CJK_IDEOGRAPH_SEVENTH_ = 0x4e03;
      
    /**
    * CJK Ideograph, Eighth code point
    */
    private static final int CJK_IDEOGRAPH_EIGHTH_ = 0x516b;
      
    /**
    * CJK Ideograph, Nineth code point
    */
    private static final int CJK_IDEOGRAPH_NINETH_ = 0x4e5d;
      
    /**
    * Application Program command code point
    */
    private static final int APPLICATION_PROGRAM_COMMAND_ = 0x009F;
      
    /**
    * Unit separator code point
    */
    private static final int UNIT_SEPARATOR_ = 0x001F;
      
    /**
    * Delete code point
    */
    private static final int DELETE_ = 0x007F;
    /**
    * ISO control character first range upper limit 0x0 - 0x1F
    */
    private static final int ISO_CONTROL_FIRST_RANGE_MAX_ = 0x1F;
    /**
     * Shift to get numeric type
     */
    private static final int NUMERIC_TYPE_SHIFT_ = 12;
    /**
     * Mask to get numeric type
     */
    private static final int NUMERIC_TYPE_MASK_ = 0x7;
    /**
    * Shift to get bidi bits
    */
    private static final int BIDI_SHIFT_ = 6;
      
    /**
    * Mask to be applied after shifting to get bidi bits
    */
    private static final int BIDI_MASK_AFTER_SHIFT_ = 0x1F;
    /**
    * Han digit characters
    */
    private static final int CJK_IDEOGRAPH_COMPLEX_ZERO_     = 0x96f6;    
    private static final int CJK_IDEOGRAPH_COMPLEX_ONE_      = 0x58f9;    
    private static final int CJK_IDEOGRAPH_COMPLEX_TWO_      = 0x8cb3;    
    private static final int CJK_IDEOGRAPH_COMPLEX_THREE_    = 0x53c3;    
    private static final int CJK_IDEOGRAPH_COMPLEX_FOUR_     = 0x8086;    
    private static final int CJK_IDEOGRAPH_COMPLEX_FIVE_     = 0x4f0d;    
    private static final int CJK_IDEOGRAPH_COMPLEX_SIX_      = 0x9678;    
    private static final int CJK_IDEOGRAPH_COMPLEX_SEVEN_    = 0x67d2;    
    private static final int CJK_IDEOGRAPH_COMPLEX_EIGHT_    = 0x634c;    
    private static final int CJK_IDEOGRAPH_COMPLEX_NINE_     = 0x7396;    
    private static final int CJK_IDEOGRAPH_TEN_              = 0x5341;    
    private static final int CJK_IDEOGRAPH_COMPLEX_TEN_      = 0x62fe;    
    private static final int CJK_IDEOGRAPH_HUNDRED_          = 0x767e;    
    private static final int CJK_IDEOGRAPH_COMPLEX_HUNDRED_  = 0x4f70;    
    private static final int CJK_IDEOGRAPH_THOUSAND_         = 0x5343;    
    private static final int CJK_IDEOGRAPH_COMPLEX_THOUSAND_ = 0x4edf;    
    private static final int CJK_IDEOGRAPH_TEN_THOUSAND_     = 0x824c;    
    private static final int CJK_IDEOGRAPH_HUNDRED_MILLION_  = 0x5104;
    /**
     * <p>Numerator power limit.
     * There are special values for huge numbers that are powers of ten.</p>
     * <p>c version genprops/store.c documents:
     * if numericValue = 0x7fffff00 + x then numericValue = 10 ^ x</p>
     */
    private static final int NUMERATOR_POWER_LIMIT_ = 0x7fffff00;
    /**
     * Integer properties mask and shift values for joining type.
     * Equivalent to icu4c UPROPS_JT_MASK. 
     */    
    private static final int JOINING_TYPE_MASK_ = 0x00003800;
    /**
     * Integer properties mask and shift values for joining type.
     * Equivalent to icu4c UPROPS_JT_SHIFT. 
     */    
    private static final int JOINING_TYPE_SHIFT_ = 11;
    /**
     * Integer properties mask and shift values for joining group.
     * Equivalent to icu4c UPROPS_JG_MASK. 
     */    
    private static final int JOINING_GROUP_MASK_ = 0x000007e0;
    /**
     * Integer properties mask and shift values for joining group.
     * Equivalent to icu4c UPROPS_JG_SHIFT. 
     */    
    private static final int JOINING_GROUP_SHIFT_ = 5;
    /**
     * Integer properties mask for decomposition type.
     * Equivalent to icu4c UPROPS_DT_MASK. 
     */    
    private static final int DECOMPOSITION_TYPE_MASK_ = 0x0000001f;
    /**
     * Integer properties mask and shift values for East Asian cell width.
     * Equivalent to icu4c UPROPS_EA_MASK 
     */    
    private static final int EAST_ASIAN_MASK_ = 0x00038000;
    /**
     * Integer properties mask and shift values for East Asian cell width.
     * Equivalent to icu4c UPROPS_EA_SHIFT 
     */    
    private static final int EAST_ASIAN_SHIFT_ = 15;
    /**
     * Zero Width Non Joiner.
     * Equivalent to icu4c ZWNJ.
     */
    private static final int ZERO_WIDTH_NON_JOINER_ = 0x200c;
    /**
     * Zero Width Joiner
     * Equivalent to icu4c ZWJ. 
     */
    private static final int ZERO_WIDTH_JOINER_ = 0x200d;
    /**
     * Integer properties mask and shift values for line breaks.
     * Equivalent to icu4c UPROPS_LB_MASK 
     */    
    private static final int LINE_BREAK_MASK_ = 0x007C0000;
    /**
     * Integer properties mask and shift values for line breaks.
     * Equivalent to icu4c UPROPS_LB_SHIFT 
     */    
    private static final int LINE_BREAK_SHIFT_ = 18;
    /**
     * Integer properties mask and shift values for blocks.
     * Equivalent to icu4c UPROPS_BLOCK_MASK 
     */    
    private static final int BLOCK_MASK_ = 0x00007f80;
    /**
     * Integer properties mask and shift values for blocks.
     * Equivalent to icu4c UPROPS_BLOCK_SHIFT 
     */    
    private static final int BLOCK_SHIFT_ = 7;
    /**
     * Integer properties mask and shift values for scripts.
     * Equivalent to icu4c UPROPS_SHIFT_MASK
     */    
    private static final int SCRIPT_MASK_ = 0x0000007f;
                           
    // private constructor -----------------------------------------------
      
    /**
    * Private constructor to prevent instantiation
    */
    private UCharacter()
    {
    }
      
    // private methods ---------------------------------------------------
    
    private static int getEuropeanDigit(int ch) {
        if (ch <= 0x7a) {
            if (ch >= 0x41 && ch <= 0x5a) {
                return ch + 10 - 0x41;
            } else if (ch >= 0x61) {
                return ch + 10 - 0x61;
            }
        } else if (ch >= 0xff21) {
            if (ch <= 0xff3a) {
                return ch + 10 - 0xff21;
            } else if (ch >= 0xff41 && ch <= 0xff5a) {
                return ch + 10 - 0xff41;
            }
        }
        return -1;
    }
    
    /**
     * Gets the numeric type of the property argument
     * @param props 32 bit property
     * @return the numeric type
     */
    private static int getNumericType(int props)
    {
        return (props >> NUMERIC_TYPE_SHIFT_) & NUMERIC_TYPE_MASK_;
    }
    
    /**
     * Checks if the property value has a exception indicator
     * @param props 32 bit property value
     * @return true if property does not have a exception indicator, false
     *          otherwise
     */     
    private static boolean isNotExceptionIndicator(int props)
    {
         return (props & UCharacterProperty.EXCEPTION_MASK) == 0;
    }
}

