package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$Latin$Greek extends ListResourceBundle {
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
                
                // Latin Letters
                
                + "E-MACRON=\u0112;"
                + "e-macron=\u0113;"
                + "O-MACRON=\u014C;"
                + "o-macron=\u014D;"
                + "Y-UMLAUT=\u0178;"
                + "y-umlaut=\u00FF;"
                
                //! // with real accents.
                //! + "E-MACRON-ACUTE=\u0112\u0301;"
                //! + "e-macron-acute=\u0113\u0301;"
                //! + "O-MACRON-ACUTE=\u014C\u0301;"
                //! + "o-macron-acute=\u014D\u0301;"
                //! + "y-umlaut-acute=\u00FF\u0301;"
                //! + "\u00ef-acute=\u00ef\u0301;"
                //! + "\u00fc-acute=\u00fc\u0301;"
                //! //
 
                // single letter equivalents
                
                + "E-MACRON-ACUTE=\u00CA;"
                + "e-macron-acute=\u00EA;"
                + "O-MACRON-ACUTE=\u00D4;"
                + "o-macron-acute=\u00F4;"
                + "y-umlaut-acute=\u0177;"
                + "\u00ef-acute=\u00EE;"
                + "\u00fc-acute=\u00FB;"       
                
                // Greek Letters

                + "ALPHA=\u0391;"
                + "BETA=\u0392;"
                + "GAMMA=\u0393;"
                + "DELTA=\u0394;"
                + "EPSILON=\u0395;"
                + "ZETA=\u0396;"
                + "ETA=\u0397;"
                + "THETA=\u0398;"
                + "IOTA=\u0399;"
                + "KAPPA=\u039A;"
                + "LAMBDA=\u039B;"
                + "MU=\u039C;"
                + "NU=\u039D;"
                + "XI=\u039E;"
                + "OMICRON=\u039F;"
                + "PI=\u03A0;"
                + "RHO=\u03A1;"
                + "SIGMA=\u03A3;"
                + "TAU=\u03A4;"
                + "YPSILON=\u03A5;"
                + "PHI=\u03A6;"
                + "CHI=\u03A7;"
                + "PSI=\u03A8;"
                + "OMEGA=\u03A9;"

                + "ALPHA+=\u0386;"
                + "EPSILON+=\u0388;"
                + "ETA+=\u0389;"
                + "IOTA+=\u038A;"
                + "OMICRON+=\u038C;"
                + "YPSILON+=\u038E;"
                + "OMEGA+=\u038F;"
                + "IOTA_DIAERESIS=\u03AA;"
                + "YPSILON_DIAERESIS=\u03AB;"

                + "alpha=\u03B1;"
                + "beta=\u03B2;"
                + "gamma=\u03B3;"
                + "delta=\u03B4;"
                + "epsilon=\u03B5;"
                + "zeta=\u03B6;"
                + "eta=\u03B7;"
                + "theta=\u03B8;"
                + "iota=\u03B9;"
                + "kappa=\u03BA;"
                + "lambda=\u03BB;"
                + "mu=\u03BC;"
                + "nu=\u03BD;"
                + "xi=\u03BE;"
                + "omicron=\u03BF;"
                + "pi=\u03C0;"
                + "rho=\u03C1;"
                + "sigma=\u03C3;"
                + "tau=\u03C4;"
                + "ypsilon=\u03C5;"
                + "phi=\u03C6;"
                + "chi=\u03C7;"
                + "psi=\u03C8;"
                + "omega=\u03C9;"

                //forms

                + "alpha+=\u03AC;"
                + "epsilon+=\u03AD;"
                + "eta+=\u03AE;"
                + "iota+=\u03AF;"
                + "omicron+=\u03CC;"
                + "ypsilon+=\u03CD;"
                + "omega+=\u03CE;"
                + "iota_diaeresis=\u03CA;"
                + "ypsilon_diaeresis=\u03CB;"
                + "iota_diaeresis+=\u0390;"
                + "ypsilon_diaeresis+=\u03B0;"
                + "sigma+=\u03C2;"

                // Variables for conditional mappings
                
                // Use lowercase for all variable names, to allow cut/paste below.

                + "letter=[~[:Lu:][:Ll:]];"
                + "lower=[[:Ll:]];"
                + "softener=[eiyEIY];"
                + "vowel=[aeiouAEIOU"
                +   "{ALPHA}{EPSILON}{ETA}{IOTA}{OMICRON}{YPSILON}{OMEGA}"
                +   "{ALPHA+}{EPSILON+}{ETA+}{IOTA+}{OMICRON+}{YPSILON+}{OMEGA+}"
                +   "{IOTA_DIAERESIS}{YPSILON_DIAERESIS}"
                +   "{alpha}{epsilon}{eta}{iota}{omicron}{ypsilon}{omega}"
                +   "{alpha+}{epsilon+}{eta+}{iota+}{omicron+}{ypsilon+}{omega+}"
                +   "{iota_diaeresis}{ypsilon_diaeresis}"
                +   "{iota_diaeresis+}{ypsilon_diaeresis+}"
                +   "];"
                + "n-gamma=[GKXCgkxc];"
                + "gamma-n=[{GAMMA}{KAPPA}{CHI}{XI}{gamma}{kappa}{chi}{xi}];"
                + "pp=[Pp];"

                // ==============================================
                // Rules
                // ==============================================
                // The following are special titlecases, and should
                // not be copied when duplicating the lowercase
                // ==============================================
                
                + "Th <> {THETA}({lower};"
                + "Ph <> {PHI}({lower};"
                + "Ch <> {CHI}({lower};"
              //masked: + "Ps<{PHI}({lower};"
                
                // Because there is no uppercase forms for final sigma,
                // we had to move all the sigma rules up here.
                
                // Remember to insert ' to preserve round trip, for double letters
                // don't need to do this for the digraphs with h,
                // since it is not created when mapping back from greek
                
                // use special form for s
                
                + "''S <> ({pp}) {SIGMA} ;" // handle PS
                + "S <> {SIGMA};"
                
                // The following are a bit tricky. 's' takes two forms in greek
                // final or non final. 
                // We use ~s to represent the abnormal form: final before letter
                // or non-final before non-letter.
                // We use 's to separate p and s (otherwise ps is one letter)
                // so, we break out the following forms:
                
                + "''s < ({pp}) {sigma} ({letter});"
                + "s <          {sigma} ({letter});"
                + "~s <         {sigma} ;"

                + "~s <         {sigma+} ({letter});"
                + "''s < ({pp}) {sigma+} ;"
                + "s <          {sigma+} ;"

                + "~s ({letter})  > {sigma+};"
                + "~s             > {sigma};"
                + "''s ({letter}) > {sigma};"
                + "''s            > {sigma+};"
                + "s ({letter})   > {sigma};"
                + "s              > {sigma+};"
                
                // because there are no uppercase forms, had to move these up too.
                
                + "i\"`>{iota_diaeresis+};"
                + "y\"`>{ypsilon_diaeresis+};"
                
                + "{\u00ef-acute} <> {iota_diaeresis+};"
                + "{\u00fc-acute} <> {vowel}){ypsilon_diaeresis+};"
                + "{y-umlaut-acute} <> {ypsilon_diaeresis+};"
                                
                // ==============================================
                // Uppercase Forms.
                // To make lowercase forms, just copy and lowercase below
                // ==============================================
 
                // Typing variants, in case the keyboard doesn't have accents
                
                + "A`>{ALPHA+};"
                + "E`>{EPSILON+};"
                + "EE`>{ETA+};"
                + "EE>{ETA};" 
                + "I`>{IOTA+};"
                + "O`>{OMICRON+};"
                + "OO`>{OMEGA+};"
                + "OO>{OMEGA};"
                + "I\">{IOTA_DIAERESIS};"
                + "Y\">{YPSILON_DIAERESIS};"
                
                // Basic Letters
                
                + "A<>{ALPHA};"
                + "\u00c1<>{ALPHA+};"
                + "B<>{BETA};"
                + "N ({n-gamma}) <> {GAMMA} ({gamma-n});"
                + "G<>{GAMMA};"
                + "D<>{DELTA};"
                + "''E <> ([Ee]){EPSILON};" // handle EE
                + "E<>{EPSILON};"
                + "\u00c9<>{EPSILON+};"
                + "Z<>{ZETA};"
                + "{E-MACRON-ACUTE}<>{ETA+};"
                + "{E-MACRON}<>{ETA};"
                + "TH<>{THETA};"
                + "I<>{IOTA};"
                + "\u00cd<>{IOTA+};"
                + "\u00cf<>{IOTA_DIAERESIS};"
                + "K<>{KAPPA};"
                + "L<>{LAMBDA};"
                + "M<>{MU};"
                + "N'' <> {NU} ({gamma-n});"
                + "N<>{NU};"
                + "X<>{XI};"
                + "''O <> ([Oo]) {OMICRON};" // handle OO
                + "O<>{OMICRON};"
                + "\u00d3<>{OMICRON+};"
                + "PH<>{PHI};" // needs ordering before P
                + "PS<>{PSI};" // needs ordering before P
                + "P<>{PI};"
                + "R<>{RHO};"
                + "T<>{TAU};"
                + "U <> ({vowel}) {YPSILON};"
                + "\u00da <> ({vowel}) {YPSILON+};"
                + "\u00dc <> ({vowel}) {YPSILON_DIAERESIS};"
                + "Y<>{YPSILON};"
                + "\u00dd<>{YPSILON+};"
                + "{Y-UMLAUT}<>{YPSILON_DIAERESIS};"
                + "CH<>{CHI};"
                + "{O-MACRON-ACUTE}<>{OMEGA+};"
                + "{O-MACRON}<>{OMEGA};"

                // Extra English Letters. Mapped for completeness
                
                + "C({softener})>|S;"
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
                
                + "a`>{alpha+};"
                + "e`>{epsilon+};"
                + "ee`>{eta+};"
                + "ee>{eta};" 
                + "i`>{iota+};"
                + "o`>{omicron+};"
                + "oo`>{omega+};"
                + "oo>{omega};"
                + "i\">{iota_diaeresis};"
                + "y\">{ypsilon_diaeresis};"
                
                // basic letters
                
                + "a<>{alpha};"
                + "\u00e1<>{alpha+};"
                + "b<>{beta};"
                + "n ({n-gamma}) <> {gamma} ({gamma-n});"
                + "g<>{gamma};"
                + "d<>{delta};"
                + "''e <> ([Ee]){epsilon};" // handle EE
                + "e<>{epsilon};"
                + "\u00e9<>{epsilon+};"
                + "z<>{zeta};"
                + "{e-macron-acute}<>{eta+};"
                + "{e-macron}<>{eta};"
                + "th<>{theta};"
                + "i<>{iota};"
                + "\u00ed<>{iota+};"
                + "\u00ef<>{iota_diaeresis};"
                + "k<>{kappa};"
                + "l<>{lambda};"
                + "m<>{mu};"
                + "n'' <> {nu} ({gamma-n});"
                + "n<>{nu};"
                + "x<>{xi};"
                + "''o <> ([Oo]) {omicron};" // handle OO
                + "o<>{omicron};"
                + "\u00f3<>{omicron+};"
                + "ph<>{phi};" // needs ordering before p
                + "ps<>{psi};" // needs ordering before p
                + "p<>{pi};"
                + "r<>{rho};"
                + "t<>{tau};"
                + "u <> ({vowel}){ypsilon};"
                + "\u00fa <> ({vowel}){ypsilon+};"
                + "\u00fc <> ({vowel}){ypsilon_diaeresis};"
                + "y<>{ypsilon};"
                + "\u00fd<>{ypsilon+};"
                + "{y-umlaut}<>{ypsilon_diaeresis};"
                + "ch<>{chi};"
                + "{o-macron-acute}<>{omega+};"
                + "{o-macron}<>{omega};"

                // extra english letters. mapped for completeness
                
                + "c({softener})>|s;"
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
