/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/impl/data/Attic/LocaleElements.java,v $ 
 * $Date: 2000/06/30 00:00:09 $ 
 * $Revision: 1.3 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class LocaleElements extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "TransliteratorNamePattern",
                /* Format for the display name of a Transliterator.
                 * This is the language-neutral form of this resource.
                 */
                "{0,choice,0#|1#{1}|2#{1}-{2}}", // Display name
            },
            { "RuleBasedTransliteratorIDs",
                /* These IDs must have the form "From-To" or "SingleName".
                 * The ID "From-To" will be sought in the resource bundle
                 * TransliteratorRuleFromTo.java in the resources package.
                 * The ID "SingleName" will be sought in
                 * TransliteratorSingleName.java.  In addition, a reverse
                 * transliterator may be specified using "*To-From", which
                 * indicates TransliteratorRuleFromTo.java with a
                 * RuleBasedTransliterator.REVERSE constructor parameter.
                 */
              new String[] {

                  // Bidirectional rules

                  "Fullwidth-Halfwidth",
                  "*Halfwidth-Fullwidth",

                  "Latin-Arabic",
                  "*Arabic-Latin",

                  "Latin-Cyrillic",
                  "*Cyrillic-Latin",

                  "Latin-Devanagari",
                  "*Devanagari-Latin",

                  "Latin-Greek",
                  "*Greek-Latin",

                  "Latin-Hebrew",
                  "*Hebrew-Latin",

                  "Latin-Jamo",
                  "*Jamo-Latin",

                  "Latin-Kana",
                  "*Kana-Latin",

                  "Hiragana-Katakana",
                  "*Katakana-Hiragana",

                  "StraightQuotes-CurlyQuotes",
                  "*CurlyQuotes-StraightQuotes",

                  // One way rules (forward only)

                  "Han-Pinyin",
                  "Kanji-English",
                  "Kanji-OnRomaji",
                  "KeyboardEscape-Latin1",
                  "UnicodeName-UnicodeChar"
              }
            },
        };
    }
}
