/*******************************************************************************
 *   Copyright (C) 1997-2000, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Hiragana_Katakana.java,v $ 
 * $Date: 2000/06/30 00:00:09 $ 
 * $Revision: 1.1 $
 *******************************************************************************
 *   Date        Name        Description
 *   06/29/00    aliu        Creation.
 *******************************************************************************
 */

package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Hiragana_Katakana extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule", "" +

              // Hiragana-Katana

              // This is largely a one-to-one mapping, but it has a
              // few kinks:

              // 1. The Katakana va/vi/ve/vo (30F7-30FA) have no
              // Hiragana equivalents.  We use Hiragana wa/wi/we/wo
              // (308F-3092) with a voicing mark (3099), which is
              // semantically equivalent.  However, this is a non-
              // roundtripping transformation.

              // 2. The Katakana small ka/ke (30F5,30F6) have no
              // Hiragana equiavlents.  We convert them to normal
              // Hiragana ka/ke (304B,3051).  This is a one-way
              // information-losing transformation and precludes
              // round-tripping of 30F5 and 30F6.

              // 3. The combining marks 3099-309C are in the Hiragana
              // block, but they apply to Katakana as well, so we
              // leave the untouched.

              // 4. The Katakana prolonged sound mark 30FC doubles the
              // preceding vowel.  This is a one-way information-
              // losing transformation from Katakana to Hiragana.

              // 5. The Katakana middle dot separates words in foreign
              // expressions; we leave this unmodified.

              // The above points preclude successful round-trip
              // transformations of arbitrary input text.  However,
              // they provide naturalistic results that should conform
              // to natural language expectations.


              // Combining equivalents
              "\u308F\u3099 <> \u30F7;" +
              "\u3090\u3099 <> \u30F8;" +
              "\u3091\u3099 <> \u30F9;" +
              "\u3092\u3099 <> \u30FA;" +

              // One-to-one mappings, main block
              // 3041:3094 <> 30A1:30F4
              // 309D,E <> 30FD,E
              "\u3041 <> \u30A1;" +
              "\u3042 <> \u30A2;" +
              "\u3043 <> \u30A3;" +
              "\u3044 <> \u30A4;" +
              "\u3045 <> \u30A5;" +
              "\u3046 <> \u30A6;" +
              "\u3047 <> \u30A7;" +
              "\u3048 <> \u30A8;" +
              "\u3049 <> \u30A9;" +
              "\u304A <> \u30AA;" +
              "\u304B <> \u30AB;" +
              "\u304C <> \u30AC;" +
              "\u304D <> \u30AD;" +
              "\u304E <> \u30AE;" +
              "\u304F <> \u30AF;" +
              "\u3050 <> \u30B0;" +
              "\u3051 <> \u30B1;" +
              "\u3052 <> \u30B2;" +
              "\u3053 <> \u30B3;" +
              "\u3054 <> \u30B4;" +
              "\u3055 <> \u30B5;" +
              "\u3056 <> \u30B6;" +
              "\u3057 <> \u30B7;" +
              "\u3058 <> \u30B8;" +
              "\u3059 <> \u30B9;" +
              "\u305A <> \u30BA;" +
              "\u305B <> \u30BB;" +
              "\u305C <> \u30BC;" +
              "\u305D <> \u30BD;" +
              "\u305E <> \u30BE;" +
              "\u305F <> \u30BF;" +
              "\u3060 <> \u30C0;" +
              "\u3061 <> \u30C1;" +
              "\u3062 <> \u30C2;" +
              "\u3063 <> \u30C3;" +
              "\u3064 <> \u30C4;" +
              "\u3065 <> \u30C5;" +
              "\u3066 <> \u30C6;" +
              "\u3067 <> \u30C7;" +
              "\u3068 <> \u30C8;" +
              "\u3069 <> \u30C9;" +
              "\u306A <> \u30CA;" +
              "\u306B <> \u30CB;" +
              "\u306C <> \u30CC;" +
              "\u306D <> \u30CD;" +
              "\u306E <> \u30CE;" +
              "\u306F <> \u30CF;" +
              "\u3070 <> \u30D0;" +
              "\u3071 <> \u30D1;" +
              "\u3072 <> \u30D2;" +
              "\u3073 <> \u30D3;" +
              "\u3074 <> \u30D4;" +
              "\u3075 <> \u30D5;" +
              "\u3076 <> \u30D6;" +
              "\u3077 <> \u30D7;" +
              "\u3078 <> \u30D8;" +
              "\u3079 <> \u30D9;" +
              "\u307A <> \u30DA;" +
              "\u307B <> \u30DB;" +
              "\u307C <> \u30DC;" +
              "\u307D <> \u30DD;" +
              "\u307E <> \u30DE;" +
              "\u307F <> \u30DF;" +
              "\u3080 <> \u30E0;" +
              "\u3081 <> \u30E1;" +
              "\u3082 <> \u30E2;" +
              "\u3083 <> \u30E3;" +
              "\u3084 <> \u30E4;" +
              "\u3085 <> \u30E5;" +
              "\u3086 <> \u30E6;" +
              "\u3087 <> \u30E7;" +
              "\u3088 <> \u30E8;" +
              "\u3089 <> \u30E9;" +
              "\u308A <> \u30EA;" +
              "\u308B <> \u30EB;" +
              "\u308C <> \u30EC;" +
              "\u308D <> \u30ED;" +
              "\u308E <> \u30EE;" +
              "\u308F <> \u30EF;" +
              "\u3090 <> \u30F0;" +
              "\u3091 <> \u30F1;" +
              "\u3092 <> \u30F2;" +
              "\u3093 <> \u30F3;" +
              "\u3094 <> \u30F4;" +
              "\u309D <> \u30FD;" +
              "\u309E <> \u30FE;" +

              // Fallback; this is a one-way Katakana-Hiragana xform.
              "\u304B < \u30F5;" +
              "\u3051 < \u30F6;" +

              // Anything followed by a prolonged sound mark 30FC has
              // its final vowel doubled.  This is a Katakana-Hiragana
              // one-way information-losing transformation.  We
              // include the small Katakana (e.g., small A 3041) and
              // do not distinguish them from their large
              // counterparts.  It doesn't make sense to double a
              // small counterpart vowel as a small Hiragana vowel, so
              // we don't do so.  In natural text this should never
              // occur anyway.  If a 30FC is seen without a preceding
              // vowel sound (e.g., after n 30F3) we do not change it.

              "$long = \u30FC;" +

              // The following categories are Hiragana, not Katakana
              // as might be expected, since by the time we get to the
              // 30FC, the preceding character will have already been
              // transformed to Hiragana.

              // {The following mechanically generated from the
              // Unicode 3.0 data:}

              "$xa = [" +
              "\u3041 \u3042 \u304B \u304C \u3055 \u3056" +
              "\u305F \u3060 \u306A \u306F \u3070 \u3071" +
              "\u307E \u3083 \u3084 \u3089 \u308E \u308F" +
              "];" +

              "$xi = [" +
              "\u3043 \u3044 \u304D \u304E \u3057 \u3058" +
              "\u3061 \u3062 \u306B \u3072 \u3073 \u3074" +
              "\u307F \u308A \u3090" +
              "];" +

              "$xu = [" +
              "\u3045 \u3046 \u304F \u3050 \u3059 \u305A" +
              "\u3063 \u3064 \u3065 \u306C \u3075 \u3076" +
              "\u3077 \u3080 \u3085 \u3086 \u308B \u3094" +
              "];" +

              "$xe = [" +
              "\u3047 \u3048 \u3051 \u3052 \u305B \u305C" +
              "\u3066 \u3067 \u306D \u3078 \u3079 \u307A" +
              "\u3081 \u308C \u3091" +
              "];" +

              "$xo = [" +
              "\u3049 \u304A \u3053 \u3054 \u305D \u305E" +
              "\u3068 \u3069 \u306E \u307B \u307C \u307D" +
              "\u3082 \u3087 \u3088 \u308D \u3092" +
              "];" +

              "\u3042 < $xa {$long};" +
              "\u3044 < $xi {$long};" +
              "\u3046 < $xu {$long};" +
              "\u3048 < $xe {$long};" +
              "\u304A < $xo {$long};" +

              ""
            }
        };
    }
}
