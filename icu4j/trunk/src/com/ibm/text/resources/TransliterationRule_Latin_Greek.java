/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Greek.java,v $ 
 * $Date: 2000/04/21 21:17:08 $ 
 * $Revision: 1.3 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Latin_Greek extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule", ""
                // ==============================================
                // Modern Greek Transliteration Rules
                //
                // This transliterates modern Greek characters, but using rules
                // that are traditional for Ancient Greek, and
                // thus more resemble Greek words that have become part
                // of English. It differs from the official Greek
                // transliteration, which is more phonetic (since
                // most modern Greek vowels, for example, have
                // degenerated simply to sound like "ee").
                //
                // There are only a few tricky parts.
                // 1. eta and omega don't map directly to Latin vowels,
                //    so we use a macron on e and o, and some
                //    other combinations if they are accented.
                // 2. The accented, diaeresis i and y are substituted too.
                // 3. Some letters use digraphs, like "ph". While typical,
                //    they need some special handling.
                // 4. A gamma before a gamma or a few other letters is
                //    transliterated as an "n", as in "Anglo"
                // 5. An ypsilon after a vowel is a "u", as in
                //    "Mouseio". Otherwise it is a "y" as in "Physikon"
                // 6. The construction of the rules is made simpler by making sure
                //    that most rules for lowercase letters exactly correspond to the
                //    rules for uppercase letters, *except* for the case of the letters
                //    in the rule itself. That way, after modifying the uppercase rules,
                //    you can just copy, paste, and "set to lowercase" to get
                //    the rules for lowercase letters!
                // ==============================================
            
                // ==============================================
                // Variables, used to make the rules more comprehensible
                // and for conditionals.
                // ==============================================

                + "$quote=\";"
                
                // Latin Letters
                
                + "$E_MACRON=\u0112;"
                + "$e_macron=\u0113;"
                + "$O_MACRON=\u014C;"
                + "$o_macron=\u014D;"
                + "$Y_UMLAUT=\u0178;"
                + "$y_umlaut=\u00FF;"
                
                //! // with real accents.
                //! + "$E_MACRON_ACUTE=\u0112\u0301;"
                //! + "$e_macron_acute=\u0113\u0301;"
                //! + "$O_MACRON_ACUTE=\u014C\u0301;"
                //! + "$o_macron_acute=\u014D\u0301;"
                //! + "$y_umlaut_acute=\u00FF\u0301;"
                //! + "$u00ef_acute=\u00ef\u0301;"
                //! + "$u00fc_acute=\u00fc\u0301;"
                //! //
 
                // single letter equivalents
                
                + "$E_MACRON_ACUTE=\u00CA;"
                + "$e_macron_acute=\u00EA;"
                + "$O_MACRON_ACUTE=\u00D4;"
                + "$o_macron_acute=\u00F4;"
                + "$y_umlaut_acute=\u0177;"
                + "$u00ef_acute=\u00EE;"
                + "$u00fc_acute=\u00FB;"       
                
                // Greek Letters

                + "$ALPHA=\u0391;"
                + "$BETA=\u0392;"
                + "$GAMMA=\u0393;"
                + "$DELTA=\u0394;"
                + "$EPSILON=\u0395;"
                + "$ZETA=\u0396;"
                + "$ETA=\u0397;"
                + "$THETA=\u0398;"
                + "$IOTA=\u0399;"
                + "$KAPPA=\u039A;"
                + "$LAMBDA=\u039B;"
                + "$MU=\u039C;"
                + "$NU=\u039D;"
                + "$XI=\u039E;"
                + "$OMICRON=\u039F;"
                + "$PI=\u03A0;"
                + "$RHO=\u03A1;"
                + "$SIGMA=\u03A3;"
                + "$TAU=\u03A4;"
                + "$YPSILON=\u03A5;"
                + "$PHI=\u03A6;"
                + "$CHI=\u03A7;"
                + "$PSI=\u03A8;"
                + "$OMEGA=\u03A9;"

                + "$ALPHA2=\u0386;"
                + "$EPSILON2=\u0388;"
                + "$ETA2=\u0389;"
                + "$IOTA2=\u038A;"
                + "$OMICRON2=\u038C;"
                + "$YPSILON2=\u038E;"
                + "$OMEGA2=\u038F;"
                + "$IOTA_DIAERESIS=\u03AA;"
                + "$YPSILON_DIAERESIS=\u03AB;"

                + "$alpha=\u03B1;"
                + "$beta=\u03B2;"
                + "$gamma=\u03B3;"
                + "$delta=\u03B4;"
                + "$epsilon=\u03B5;"
                + "$zeta=\u03B6;"
                + "$eta=\u03B7;"
                + "$theta=\u03B8;"
                + "$iota=\u03B9;"
                + "$kappa=\u03BA;"
                + "$lambda=\u03BB;"
                + "$mu=\u03BC;"
                + "$nu=\u03BD;"
                + "$xi=\u03BE;"
                + "$omicron=\u03BF;"
                + "$pi=\u03C0;"
                + "$rho=\u03C1;"
                + "$sigma=\u03C3;"
                + "$tau=\u03C4;"
                + "$ypsilon=\u03C5;"
                + "$phi=\u03C6;"
                + "$chi=\u03C7;"
                + "$psi=\u03C8;"
                + "$omega=\u03C9;"

                //forms

                + "$alpha2=\u03AC;"
                + "$epsilon2=\u03AD;"
                + "$eta2=\u03AE;"
                + "$iota2=\u03AF;"
                + "$omicron2=\u03CC;"
                + "$ypsilon2=\u03CD;"
                + "$omega2=\u03CE;"
                + "$iota_diaeresis=\u03CA;"
                + "$ypsilon_diaeresis=\u03CB;"
                + "$iota_diaeresis2=\u0390;"
                + "$ypsilon_diaeresis2=\u03B0;"
                + "$sigma2=\u03C2;"

                // Variables for conditional mappings
                
                // Use lowercase for all variable names, to allow cut/paste below.

                + "$letter=[~[:Lu:][:Ll:]];"
                + "$lower=[[:Ll:]];"
                + "$softener=[eiyEIY];"
                + "$vowel=[aeiouAEIOU"
                +   "$ALPHA$EPSILON$ETA$IOTA$OMICRON$YPSILON$OMEGA"
                +   "$ALPHA2$EPSILON2$ETA2$IOTA2$OMICRON2$YPSILON2$OMEGA2"
                +   "$IOTA_DIAERESIS$YPSILON_DIAERESIS"
                +   "$alpha$epsilon$eta$iota$omicron$ypsilon$omega"
                +   "$alpha2$epsilon2$eta2$iota2$omicron2$ypsilon2$omega2"
                +   "$iota_diaeresis$ypsilon_diaeresis"
                +   "$iota_diaeresis2$ypsilon_diaeresis2"
                +   "];"
                + "$n_gamma=[GKXCgkxc];"
                + "$gamma_n=[$GAMMA$KAPPA$CHI$XI$gamma$kappa$chi$xi];"
                + "$pp=[Pp];"

                // ==============================================
                // Rules
                // ==============================================
                // The following are special titlecases, and should
                // not be copied when duplicating the lowercase
                // ==============================================
                
                + "Th <> $THETA}$lower;"
                + "Ph <> $PHI}$lower;"
                + "Ch <> $CHI}$lower;"
              //masked: + "Ps<$PHI}$lower;"
                
                // Because there is no uppercase forms for final sigma,
                // we had to move all the sigma rules up here.
                
                // Remember to insert ' to preserve round trip, for double letters
                // don't need to do this for the digraphs with h,
                // since it is not created when mapping back from greek
                
                // use special form for s
                
                + "''S <> $pp{$SIGMA;" // handle PS
                + "S <> $SIGMA;"
                
                // The following are a bit tricky. 's' takes two forms in greek
                // final or non final. 
                // We use ~s to represent the abnormal form: final before letter
                // or non-final before non-letter.
                // We use 's to separate p and s (otherwise ps is one letter)
                // so, we break out the following forms:
                
                + "''s < $pp{$sigma}$letter;"
                + "s <          $sigma}$letter;"
                + "~s <         $sigma;"

                + "~s <         $sigma2}$letter;"
                + "''s < $pp{$sigma2;"
                + "s <          $sigma2;"

                + "~s }$letter>$sigma2;"
                + "~s             > $sigma;"
                + "''s }$letter>$sigma;"
                + "''s            > $sigma2;"
                + "s }$letter>$sigma;"
                + "s              > $sigma2;"
                
                // because there are no uppercase forms, had to move these up too.
                
                + "i$quote`>$iota_diaeresis2;"
                + "y$quote`>$ypsilon_diaeresis2;"
                
                + "$u00ef_acute<>$iota_diaeresis2;"
                + "$u00fc_acute<>$vowel{$ypsilon_diaeresis2;"
                + "$y_umlaut_acute<>$ypsilon_diaeresis2;"
                                
                // ==============================================
                // Uppercase Forms.
                // To make lowercase forms, just copy and lowercase below
                // ==============================================
 
                // Typing variants, in case the keyboard doesn't have accents
                
                + "A`>$ALPHA2;"
                + "E`>$EPSILON2;"
                + "EE`>$ETA2;"
                + "EE>$ETA;" 
                + "I`>$IOTA2;"
                + "O`>$OMICRON2;"
                + "OO`>$OMEGA2;"
                + "OO>$OMEGA;"
                + "I$quote>$IOTA_DIAERESIS;"
                + "Y$quote>$YPSILON_DIAERESIS;"
                
                // Basic Letters
                
                + "A<>$ALPHA;"
                + "\u00c1<>$ALPHA2;"
                + "B<>$BETA;"
                + "N }$n_gamma<>$GAMMA}$gamma_n;"
                + "G<>$GAMMA;"
                + "D<>$DELTA;"
                + "''E <> [Ee]{$EPSILON;" // handle EE
                + "E<>$EPSILON;"
                + "\u00c9<>$EPSILON2;"
                + "Z<>$ZETA;"
                + "$E_MACRON_ACUTE<>$ETA2;"
                + "$E_MACRON<>$ETA;"
                + "TH<>$THETA;"
                + "I<>$IOTA;"
                + "\u00cd<>$IOTA2;"
                + "\u00cf<>$IOTA_DIAERESIS;"
                + "K<>$KAPPA;"
                + "L<>$LAMBDA;"
                + "M<>$MU;"
                + "N'' <> $NU}$gamma_n;"
                + "N<>$NU;"
                + "X<>$XI;"
                + "''O <> [Oo]{ $OMICRON;" // handle OO
                + "O<>$OMICRON;"
                + "\u00d3<>$OMICRON2;"
                + "PH<>$PHI;" // needs ordering before P
                + "PS<>$PSI;" // needs ordering before P
                + "P<>$PI;"
                + "R<>$RHO;"
                + "T<>$TAU;"
                + "U <> $vowel{$YPSILON;"
                + "\u00da <> $vowel{$YPSILON2;"
                + "\u00dc <> $vowel{$YPSILON_DIAERESIS;"
                + "Y<>$YPSILON;"
                + "\u00dd<>$YPSILON2;"
                + "$Y_UMLAUT<>$YPSILON_DIAERESIS;"
                + "CH<>$CHI;"
                + "$O_MACRON_ACUTE<>$OMEGA2;"
                + "$O_MACRON<>$OMEGA;"

                // Extra English Letters. Mapped for completeness
                
                + "C}$softener>|S;"
                + "C>|K;"
                + "F>|PH;"
                + "H>|CH;"
                + "J>|I;"
                + "Q>|K;"
                + "V>|U;"
                + "W>|U;"
                
                // ==============================================
                // Lowercase Forms. Just copy above and lowercase
                // ==============================================

                // typing variants, in case the keyboard doesn't have accents
                
                + "a`>$alpha2;"
                + "e`>$epsilon2;"
                + "ee`>$eta2;"
                + "ee>$eta;" 
                + "i`>$iota2;"
                + "o`>$omicron2;"
                + "oo`>$omega2;"
                + "oo>$omega;"
                + "i$quote>$iota_diaeresis;"
                + "y$quote>$ypsilon_diaeresis;"
                
                // basic letters
                
                + "a<>$alpha;"
                + "\u00e1<>$alpha2;"
                + "b<>$beta;"
                + "n }$n_gamma<>$gamma}$gamma_n;"
                + "g<>$gamma;"
                + "d<>$delta;"
                + "''e <> [Ee]{$epsilon;" // handle EE
                + "e<>$epsilon;"
                + "\u00e9<>$epsilon2;"
                + "z<>$zeta;"
                + "$e_macron_acute<>$eta2;"
                + "$e_macron<>$eta;"
                + "th<>$theta;"
                + "i<>$iota;"
                + "\u00ed<>$iota2;"
                + "\u00ef<>$iota_diaeresis;"
                + "k<>$kappa;"
                + "l<>$lambda;"
                + "m<>$mu;"
                + "n'' <> $nu}$gamma_n;"
                + "n<>$nu;"
                + "x<>$xi;"
                + "''o <> [Oo]{ $omicron;" // handle OO
                + "o<>$omicron;"
                + "\u00f3<>$omicron2;"
                + "ph<>$phi;" // needs ordering before p
                + "ps<>$psi;" // needs ordering before p
                + "p<>$pi;"
                + "r<>$rho;"
                + "t<>$tau;"
                + "u <> $vowel{$ypsilon;"
                + "\u00fa <> $vowel{$ypsilon2;"
                + "\u00fc <> $vowel{$ypsilon_diaeresis;"
                + "y<>$ypsilon;"
                + "\u00fd<>$ypsilon2;"
                + "$y_umlaut<>$ypsilon_diaeresis;"
                + "ch<>$chi;"
                + "$o_macron_acute<>$omega2;"
                + "$o_macron<>$omega;"

                // extra english letters. mapped for completeness
                
                + "c}$softener>|s;"
                + "c>|k;"
                + "f>|ph;"
                + "h>|ch;"
                + "j>|i;"
                + "q>|k;"
                + "v>|u;"
                + "w>|u;"
                
                // ====================================
                // Normal final rule: remove '
                // ====================================
                
                //+ "''>;"                
            }
        };
    }
}
