/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Kana.java,v $ 
 * $Date: 2000/03/10 04:07:31 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Latin_Kana extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            // Lowercase Latin to hiragana
            // Uppercase Latin to katakana

            { "Rule", ""
                //# $Revision: 1.2 $
                // Transliteration rules for Japanese Hiragana and Katakana to
                // romaji
                // lower case roman generates hiragana.
                // upper case roman generates katakana.
                // Uses modified Hepburn. Small changes to make unambiguous.
                // Kunrei-shiki: Hepburn/MHepburn
                /*
                si:     shi
                si ~ya: sha
                si ~yu: shu
                si ~yo: sho
                zi:     ji
                zi ~ya: ja
                zi ~yu: ju
                zi ~yo: jo
                ti:     chi
                ti ~ya: cha
                ti ~yu: chu
                ti ~yu: cho
                tu:     tsu
                di:     ji/dji
                du:     zu/dzu
                hu:     fu
                // for foreign words
                se ~i   si
                si ~e   she
                
                ze ~i   zi
                zi ~e   je
                
                te ~i   ti
                ti ~e   che
                te ~u   tu
                
                de ~i   di
                de ~u   du
                de ~i   di
                
                he ~u:  hu
                hu ~a   fa
                hu ~i   fi
                hu ~e   he
                hu ~o   ho
                // Most small forms are generated, but if necessary
                // explicit small forms are given with ~a, ~ya, etc.
                */
                //#######################################
                // Definitions of variables to be substituted
                //#######################################
                
                + "vowel=[aeiou];"
                + "quote='';"

                // now the kana
                + "long=\u30FC;"

                + "~a=\u3041;"
                + "^a=\u3042;"
                + "~i=\u3043;"
                + "^i=\u3044;"
                + "~u=\u3045;"
                + "^u=\u3046;"
                + "~e=\u3047;"
                + "^e=\u3048;"
                + "~o=\u3049;"
                + "^o=\u304A;"

                + "ka=\u304B;"
                + "ga=\u304C;"
                + "ki=\u304D;"
                + "gi=\u304E;"
                + "ku=\u304F;"
                + "gu=\u3050;"
                + "ke=\u3051;"
                + "ge=\u3052;"
                + "ko=\u3053;"
                + "go=\u3054;"

                //these are small katakana
                + "~ka=\u30F5;"
                + "~ke=\u30F6;"

                + "sa=\u3055;"
                + "za=\u3056;"
                + "si=\u3057;"
                + "zi=\u3058;"
                + "su=\u3059;"
                + "zu=\u305A;"
                + "se=\u305B;"
                + "ze=\u305C;"
                + "so=\u305D;"
                + "zo=\u305E;"

                + "ta=\u305F;"
                + "da=\u3060;"
                + "ti=\u3061;"
                + "di=\u3062;"
                + "~tu=\u3063;"
                + "tu=\u3064;"
                + "du=\u3065;"
                + "te=\u3066;"
                + "de=\u3067;"
                + "to=\u3068;"
                + "do=\u3069;"

                + "na=\u306A;"
                + "ni=\u306B;"
                + "nu=\u306C;"
                + "ne=\u306D;"
                + "no=\u306E;"

                + "ha=\u306F;"
                + "ba=\u3070;"
                + "pa=\u3071;"
                + "hi=\u3072;"
                + "bi=\u3073;"
                + "pi=\u3074;"
                + "hu=\u3075;"
                + "bu=\u3076;"
                + "pu=\u3077;"
                + "he=\u3078;"
                + "be=\u3079;"
                + "pe=\u307A;"
                + "ho=\u307B;"
                + "bo=\u307C;"
                + "po=\u307D;"

                + "ma=\u307E;"
                + "mi=\u307F;"
                + "mu=\u3080;"
                + "me=\u3081;"
                + "mo=\u3082;"

                + "~ya=\u3083;"
                + "ya=\u3084;"
                + "~yu=\u3085;"
                + "yu=\u3086;"
                + "~yo=\u3087;"
                + "yo=\u3088;"
                
                + "ra=\u3089;"
                + "ri=\u308A;"
                + "ru=\u308B;"
                + "re=\u308C;"
                + "ro=\u308D;"

                + "~wa=\u308E;"
                + "wa=\u308F;"
                + "wi=\u3090;"
                + "we=\u3091;"
                + "wo=\u3092;"

                + "^n=\u3093;"
                + "vu=\u3094;"
                
                // alternates, just to make the rules easier
                + "~yi=\u3043;"
                + "yi=\u3044;"
                + "~ye=\u3047;"
                + "ye=\u3048;"
                + "wu={^u};"
                // end alternates

                // Katakana

                + "~A=\u30A1;"
                + "^A=\u30A2;"
                + "~I=\u30A3;"
                + "^I=\u30A4;"
                + "~U=\u30A5;"
                + "^U=\u30A6;"
                + "~E=\u30A7;"
                + "^E=\u30A8;"
                + "~O=\u30A9;"
                + "^O=\u30AA;"

                + "KA=\u30AB;"
                + "GA=\u30AC;"
                + "KI=\u30AD;"
                + "GI=\u30AE;"
                + "KU=\u30AF;"
                + "GU=\u30B0;"
                + "KE=\u30B1;"
                + "GE=\u30B2;"
                + "KO=\u30B3;"
                + "GO=\u30B4;"

                //these generate small katakana
                + "~KA=\u30F5;"
                + "~KE=\u30F6;"

                + "SA=\u30B5;"
                + "ZA=\u30B6;"
                + "SI=\u30B7;"
                + "ZI=\u30B8;"
                + "SU=\u30B9;"
                + "ZU=\u30BA;"
                + "SE=\u30BB;"
                + "ZE=\u30BC;"
                + "SO=\u30BD;"
                + "ZO=\u30BE;"

                + "TA=\u30BF;"
                + "DA=\u30C0;"
                + "TI=\u30C1;"
                + "DI=\u30C2;"
                + "~TU=\u30C3;"
                + "TU=\u30C4;"
                + "DU=\u30C5;"
                + "TE=\u30C6;"
                + "DE=\u30C7;"
                + "TO=\u30C8;"
                + "DO=\u30C9;"

                + "NA=\u30CA;"
                + "NI=\u30CB;"
                + "NU=\u30CC;"
                + "NE=\u30CD;"
                + "NO=\u30CE;"

                + "HA=\u30CF;"
                + "BA=\u30D0;"
                + "PA=\u30D1;"
                + "HI=\u30D2;"
                + "BI=\u30D3;"
                + "PI=\u30D4;"
                + "HU=\u30D5;"
                + "BU=\u30D6;"
                + "PU=\u30D7;"
                + "HE=\u30D8;"
                + "BE=\u30D9;"
                + "PE=\u30DA;"
                + "HO=\u30DB;"
                + "BO=\u30DC;"
                + "PO=\u30DD;"

                + "MA=\u30DE;"
                + "MI=\u30DF;"
                + "MU=\u30E0;"
                + "ME=\u30E1;"
                + "MO=\u30E2;"

                + "~YA=\u30E3;"
                + "YA=\u30E4;"
                + "~YU=\u30E5;"
                + "YU=\u30E6;"
                + "~YO=\u30E7;"
                + "YO=\u30E8;"
                + "~WA=\u30EE;"
                
                // alternates, just to make the rules easier
                + "~YI=\u30A3;"
                + "YI=\u30A4;"
                + "~YE=\u30A7;"
                + "YE=\u30A8;"
                + "WU={^U};"
                // end alternates

                + "RA=\u30E9;"
                + "RI=\u30EA;"
                + "RU=\u30EB;"
                + "RE=\u30EC;"
                + "RO=\u30ED;"

                + "VA=\u30F7;"
                + "VI=\u30F8;"
                + "VU=\u30F4;"
                + "VE=\u30F9;"
                + "VO=\u30FA;"

                + "WA=\u30EF;"
                + "WI=\u30F0;"
                + "WE=\u30F1;"
                + "WO=\u30F2;"

                + "^N=\u30F3;"
                + "LONG=\u30FC;"
                + "QUOTE='';"
                
                // Variables used for double-letters with tsu
                
                + "K-START=[{KA}{KI}{KU}{KE}{KO}{ka}{ki}{ku}{ke}{ko}];"
                + "G-START=[{GA}{GI}{GU}{GE}{GO}{ga}{gi}{gu}{ge}{go}];"
                
                + "S-START=[{SA}{SI}{SU}{SE}{SO}{sa}{si}{su}{se}{so}];"
                + "Z-START=[{ZA}{ZU}{ZE}{ZO}{za}{zu}{ze}{zo}];"
                + "J-START=[{ZI}{zi}];"
                
                + "T-START=[{TA}{TI}{TU}{TE}{TO}{ta}{ti}{tu}{te}{to}];"
                + "D-START=[{DA}{DI}{DU}{DE}{DO}{da}{di}{du}{de}{do}];"
                
                + "N-START=[{NA}{NI}{NU}{NE}{NO}{na}{ni}{nu}{ne}{no}];"
                
                + "H-START=[{HA}{HI}{HE}{HO}{ha}{hi}{he}{ho}];"
                + "F-START=[{HU}{hu}];"
                + "B-START=[{BA}{BI}{BU}{BE}{BO}{ba}{bi}{bu}{be}{bo}];"
                + "P-START=[{PA}{PI}{PU}{PE}{PO}{pa}{pi}{pu}{pe}{po}];"
                
                + "M-START=[{MA}{MI}{MU}{ME}{MO}{ma}{mi}{mu}{me}{mo}];"
                
                + "Y-START=[{YA}{YU}{YO}{ya}{yu}{yo}];"
                
                + "R-START=[{RA}{RI}{RU}{RE}{RO}{ra}{ri}{ru}{re}{ro}];"
                
                + "W-START=[{WA}{WI}{WE}{WO}{wa}{wi}{we}{wo}];"
                
                + "V-START=[{VA}{VI}{VU}{VE}{VO}{vu}];"
                
                // lowercase copies for convenience in making hiragana

                + "k-start={K-START};"
                + "g-start={G-START};"
                + "s-start={S-START};"
                + "z-start={Z-START};"
                + "j-start={J-START};"
                + "t-start={T-START};"
                + "d-start={D-START};"
                + "n-start={N-START};"
                + "h-start={H-START};"
                + "f-start={F-START};"
                + "b-start={B-START};"
                + "p-start={P-START};"
                + "m-start={M-START};"
                + "y-start={Y-START};"
                + "r-start={R-START};"
                + "w-start={W-START};"
                + "v-start={V-START};"
 
                // remember that the order is very significant:
                // always put longer before shorter elements
                
                //#######################################
                // KATAKANA
                //#######################################

                + "VA>{VA};"
                + "VI>{VI};"
                + "VE>{VE};"
                + "VO>{VO};"

                + "VA<{VA};"
                + "VI<{VI};"
                + "VE<{VE};"
                + "VO<{VO};"

                //#######################################
                // KATAKANA SHARED
                // These are also used to produce hiragana, by lowercasing
                //#######################################
                
                + "A>{^A};"

                + "BA>{BA};"
                + "BI>{BI};"
                + "BU>{BU};"
                + "BE>{BE};"
                + "BO>{BO};"
                + "BY>{BI}|~Y;"

                + "CHI>{TI};"
                + "CH>{TI}|~Y;"

                + "C(I>|S;"
                + "C(E>|S;"

                + "DA>{DA};"
                + "DI>{DE}{~I};"
                + "DU>{DE}{~U};"
                + "DE>{DE};"
                + "DO>{DO};"
                + "DZU>{DU};"
                + "DJI>{DI};"
                + "DJ>{DI}|~Y;"

                + "E>{^E};"

                + "FU>{HU};"

                + "GA>{GA};"
                + "GI>{GI};"
                + "GU>{GU};"
                + "GE>{GE};"
                + "GO>{GO};"
                + "GY>{GI}|~Y;"

                + "HA>{HA};"
                + "HI>{HI};"
                + "HU>{HE}{~U};"
                + "HE>{HE};"
                + "HO>{HO};"

                + "I>{^I};"

                + "JI>{ZI};"

                + "KA>{KA};"
                + "KI>{KI};"
                + "KU>{KU};"
                + "KE>{KE};"
                + "KO>{KO};"
                + "KY>{KI}|~Y;"

                + "MA>{MA};"
                + "MI>{MI};"
                + "MU>{MU};"
                + "ME>{ME};"
                + "MO>{MO};"
                + "MY>{MI}|~Y;"
                
                + "M(P>{^N};"
                + "M(B>{^N};"
                + "M(F>{^N};"
                + "M(V>{^N};"

                + "NA>{NA};"
                + "NI>{NI};"
                + "NU>{NU};"
                + "NE>{NE};"
                + "NO>{NO};"
                + "NY>{NI}|~Y;"

                + "O>{^O};"

                + "PA>{PA};"
                + "PI>{PI};"
                + "PU>{PU};"
                + "PE>{PE};"
                + "PO>{PO};"
                + "PY>{PI}|~Y;"

                + "RA>{RA};"
                + "RI>{RI};"
                + "RU>{RU};"
                + "RE>{RE};"
                + "RO>{RO};"
                + "RY>{RI}|~Y;"

                + "SA>{SA};"
                + "SI>{SE}{~I};"
                + "SU>{SU};"
                + "SE>{SE};"
                + "SO>{SO};"

                + "SHI>{SI};"
                + "SH>{SI}|~Y;"
                
                + "TA>{TA};"
                + "TI>{TE}{~I};"
                + "TU>{TE}{~U};"
                + "TE>{TE};"
                + "TO>{TO};"
                
                + "TSU>{TU};"
                //+ "TS>{TU}|~;"

                + "U>{^U};"

                + "VU>{VU};"

                + "WA>{WA};"
                + "WI>{WI};"
                + "WU>{WU};"
                + "WE>{WE};"
                + "WO>{WO};"

                + "YA>{YA};"
                + "YI>{YI};"
                + "YU>{YU};"
                + "YE>{YE};"
                + "YO>{YO};"

                + "ZA>{ZA};"
                + "ZI>{ZE}{~I};"
                + "ZU>{ZU};"
                + "ZE>{ZE};"
                + "ZO>{ZO};"

                // SMALL FORMS
                
                + "~A>{~A};"
                + "~I>{~I};"
                + "~U>{~U};"
                + "~E>{~E};"
                + "~O>{~O};"
                + "~KA>{~KA};"
                + "~KE>{~KE};"
                + "~TSU>{~TU};"
                + "~WA>{~WA};"
                + "~YA>{~YA};"
                + "~YI>{~YI};"
                + "~YU>{~YU};"
                + "~YE>{~YE};"
                + "~YO>{~YO};"

                // DOUBLE CONSONANTS
                
                + "B(B>{~TU};"
                + "C(K>{~TU};"
                + "C(C>{~TU};"
                + "C(Q>{~TU};"
                + "D(D>{~TU};"
                + "F(F>{~TU};"
                + "G(G>{~TU};"
                + "H(H>{~TU};"
                + "J(J>{~TU};"
                + "K(K>{~TU};"
                + "L(L>{~TU};"
                + "M(M>{~TU};"
                + "N(N>{~TU};"
                + "P(P>{~TU};"
                + "Q(Q>{~TU};"
                + "R(R>{~TU};"
                + "S(SH>{~TU};"
                + "S(S>{~TU};"
                + "T(CH>{~TU};"
                + "T(T>{~TU};"
                + "V(V>{~TU};"
                + "W(W>{~TU};"
                + "X(X>{~TU};"
                + "Y(Y>{~TU};"
                + "Z(Z>{~TU};"
                
                // ########################################
                // CATCH MISSING VOWELS!
                // THESE ARE TO INSURE COMPLETENESS, THAT
                // ALL ROMAJI MAPS TO KANA
                // ########################################

                /*
                + "SH>{SI};"
                + "TS>{TU};"
                + "CH>{TI};"
                + "DJ>{DI};"
                + "DZ>{DU};"
                */
                
                // THE FOLLOWING ARE NOT REALLY NECESSARY, BUT PRODUCE
                // SLIGHTLY MORE NATURAL RESULTS.
                
              //masked: + "BY>{BI};"
                + "CY>{SE}{~I};"
                + "DY>{DE}{~I};"
              //masked: + "GY>{GI};"
                + "HY>{HI};"
              //masked: + "KY>{KI};"
              //masked: + "MY>{MI};"
              //masked: + "PY>{PI};"
              //masked: + "RY>{RI};"
                + "SY>{SE}{~I};"
                + "TY>{TE}{~I};"
                + "ZY>{ZE}{~I};"

                // SIMPLE SUBSTITUTIONS USING BACKUP
                
                + "C>|K;"
                + "F>{HU}|~;"
                + "J>{ZI}|~Y;"
                + "L>|R;"
                + "Q>|K;" // BACKUP AND REDO
                + "V>{VU}|~;"
                + "W>{^U}|~;"
                + "X>|KS;"

                // WE HAD TO LIST THE LONGER ONES FIRST,
                // SO HERE ARE THE ISOLATED CONSONANTS
                
                + "B>{BU};"
                + "D>{DE};"
              //masked: + "F>{HU};"
                + "G>{GU};"
                + "H>{HE};"
              //masked: + "J>{ZI};"
                + "K>{KU};"
                + "M>{^N};"
                + "N>{^N};"
                + "P>{PU};"
                + "R>{RU};"
                + "S>{SU};"
                + "T>{TE};"
              //masked: + "V>{BU};"
              //masked: + "W>{^U};"
              //masked: + "X>{KU}{SU};"
                + "Y>{^I};"
                + "Z>{ZU};"
                
                // NOW KANA TO ROMAN

                + "GYA<{GI}{~YA};"
                + "GYI<{GI}{~I};"
                + "GYU<{GI}{~YU};"
                + "GYE<{GI}{~E};"
                + "GYO<{GI}{~YO};"
                
                + "GA<{GA};"
                + "GI<{GI};"
                + "GU<{GU};"
                + "GE<{GE};"
                + "GO<{GO};"

                + "KYA<{KI}{~YA};"
                + "KYI<{KI}{~I};"
                + "KYU<{KI}{~YU};"
                + "KYE<{KI}{~E};"
                + "KYO<{KI}{~YO};"

                + "KA<{KA};"
                + "KI<{KI};"
                + "KU<{KU};"
                + "KE<{KE};"
                + "KO<{KO};"

                + "JA<{ZI}{~YA};"
                + "JI<{ZI}{~I};"
                + "JU<{ZI}{~YU};"
                + "JE<{ZI}{~E};"
                + "JO<{ZI}{~YO};"
                + "JI<{ZI};"
                
                + "ZA<{ZA};"
                + "ZI<{ZE}{~I};"
                + "ZU<{ZU};"
                + "ZE<{ZE};"
                + "ZO<{ZO};"

                + "SHA<{SI}{~YA};"
                + "SHI<{SI}{~I};"
                + "SHU<{SI}{~YU};"
                + "SHE<{SI}{~E};"
                + "SHO<{SI}{~YO};"
                + "SHI<{SI};"
                
                + "SA<{SA};"
                + "SI<{SE}{~I};"
                + "SU<{SU};"
                + "SE<{SE};"
                + "SO<{SO};"

                + "DJA<{DI}{~YA};"
                + "DJI<{DI}{~I};"
                + "DJU<{DI}{~YU};"
                + "DJE<{DI}{~E};"
                + "DJO<{DI}{~YO};"
                + "DJI<{DI};"
                
                + "DZU<{DU};"
                
                + "DA<{DA};"
                + "DI<{DE}{~I};"
                + "DU<{DE}{~U};"
                + "DE<{DE};"
                + "DO<{DO};"

                + "CHA<{TI}{~YA};"
                + "CHI<{TI}{~I};"
                + "CHU<{TI}{~YU};"
                + "CHE<{TI}{~E};"
                + "CHO<{TI}{~YO};"
                + "CHI<{TI};"
                
                + "TSU<{TU};"
                
                + "TA<{TA};"
                + "TI<{TE}{~I};"
                + "TU<{TE}{~U};"
                + "TE<{TE};"
                + "TO<{TO};"

                + "NYA<{NI}{~YA};"
                + "NYI<{NI}{~I};"
                + "NYU<{NI}{~YU};"
                + "NYE<{NI}{~E};"
                + "NYO<{NI}{~YO};"
                
                + "NA<{NA};"
                + "NI<{NI};"
                + "NU<{NU};"
                + "NE<{NE};"
                + "NO<{NO};"

                + "BYA<{BI}{~YA};"
                + "BYI<{BI}{~I};"
                + "BYU<{BI}{~YU};"
                + "BYE<{BI}{~E};"
                + "BYO<{BI}{~YO};"
                
                + "BA<{BA};"
                + "BI<{BI};"
                + "BU<{BU};"
                + "BE<{BE};"
                + "BO<{BO};"

                + "PYA<{PI}{~YA};"
                + "PYI<{PI}{~I};"
                + "PYU<{PI}{~YU};"
                + "PYE<{PI}{~E};"
                + "PYO<{PI}{~YO};"
                
                + "PA<{PA};"
                + "PI<{PI};"
                + "PU<{PU};"
                + "PE<{PE};"
                + "PO<{PO};"

                + "FA<{HU}{~A};"
                + "FI<{HU}{~I};"
                + "FE<{HU}{~E};"
                + "FO<{HU}{~O};"
                + "FU<{HU};"
                
                + "HA<{HA};"
                + "HI<{HI};"
                + "HU<{HE}{~U};"
                + "HE<{HE};"
                + "HO<{HO};"

                + "MYA<{MI}{~YA};"
                + "MYI<{MI}{~I};"
                + "MYU<{MI}{~YU};"
                + "MYE<{MI}{~E};"
                + "MYO<{MI}{~YO};"
                
                + "MA<{MA};"
                + "MI<{MI};"
                + "MU<{MU};"
                + "ME<{ME};"
                + "MO<{MO};"

                + "YA<{YA};"
                //+ "YE<{YI};"
                + "YU<{YU};"
                //+ "YE<{YE};"
                + "YO<{YO};"

                + "RYA<{RI}{~YA};"
                + "RYI<{RI}{~I};"
                + "RYU<{RI}{~YU};"
                + "RYE<{RI}{~E};"
                + "RYO<{RI}{~YO};"
                
                + "RA<{RA};"
                + "RI<{RI};"
                + "RU<{RU};"
                + "RE<{RE};"
                + "RO<{RO};"

                + "WA<{WA};"
                + "WI<{WI};"
                + "WE<{WE};"
                + "WO<{WO};"
                //+ "WU<{WU};"

                + "VA<{VU}{~A};"
                + "VI<{VU}{~I};"
                + "VE<{VU}{~E};"
                + "VO<{VU}{~O};"
                + "VU<{VU};"
                
                // DOUBLED LETTERS

                + "N''<{^N}({^A};"
                + "N''<{^N}({^I};"
                + "N''<{^N}({^U};"
                + "N''<{^N}({^E};"
                + "N''<{^N}({^O};"
                + "N''<{^N}({NA};"
                + "N''<{^N}({NI};"
                + "N''<{^N}({NU};"
                + "N''<{^N}({NE};"
                + "N''<{^N}({NO};"
                + "N''<{^N}({YA};"
                + "N''<{^N}({YU};"
                + "N''<{^N}({YO};"
                + "N''<{^N}({^N};"
                + "N<{^N};"
                
                + "N<{~TU}({N-START};"
                + "M<{~TU}({M-START};"
                + "W<{~TU}({W-START};"
                + "Y<{~TU}({Y-START};"
                + "G<{~TU}({G-START};"
                + "K<{~TU}({K-START};"
                + "Z<{~TU}({Z-START};"
                + "J<{~TU}({J-START};"
                + "S<{~TU}({S-START};"
                + "D<{~TU}({D-START};"
                + "T<{~TU}({T-START};"
                + "B<{~TU}({B-START};"
                + "P<{~TU}({P-START};"
                + "H<{~TU}({H-START};"
                + "F<{~TU}({F-START};"
                + "R<{~TU}({R-START};"
                + "V<{~TU}({V-START};"

                + "A<{^A};" // MOVED THIS BLOCK DOWN {aliu}
                + "I<{^I};"
                + "U<{^U};"
                + "E<{^E};"
                + "O<{^O};"
                
                // SMALL FORMS
                
                + "~A<{~A};"
                + "~I<{~I};"
                + "~U<{~U};"
                + "~E<{~E};"
                + "~O<{~O};"
                + "~KA<{~KA};"
                + "~KE<{~KE};"
                + "~YA<{~YA};"
                + "~YU<{~YU};"
                + "~YO<{~YO};"
                + "~TSU<{~TU};"
                + "~WA<{~WA};"
                
                // LENGTH MARK. LATER, COULD USE CIRCUMFLEX

                + "A<A){LONG};" // LIU
                + "E<E){LONG};" // LIU
                + "I<I){LONG};" // LIU
                + "O<O){LONG};" // LIU
                + "U<U){LONG};" // LIU

                //#######################################
                // HIRAGANA
                // These are derived from the above by lowercasing
                //#######################################

                + "a>{^a};"

                + "ba>{ba};"
                + "bi>{bi};"
                + "bu>{bu};"
                + "be>{be};"
                + "bo>{bo};"
                + "by>{bi}|~y;"

                + "chi>{ti};"
                + "ch>{ti}|~y;"

                + "c(i>|s;"
                + "c(e>|s;"

                + "da>{da};"
                + "di>{de}{~i};"
                + "du>{de}{~u};"
                + "de>{de};"
                + "do>{do};"
                + "dzu>{du};"
                + "dji>{di};"
                + "dj>{di}|~y;"

                + "e>{^e};"

                + "fu>{hu};"

                + "ga>{ga};"
                + "gi>{gi};"
                + "gu>{gu};"
                + "ge>{ge};"
                + "go>{go};"
                + "gy>{gi}|~y;"

                + "ha>{ha};"
                + "hi>{hi};"
                + "hu>{he}{~u};"
                + "he>{he};"
                + "ho>{ho};"

                + "i>{^i};"

                + "ji>{zi};"

                + "ka>{ka};"
                + "ki>{ki};"
                + "ku>{ku};"
                + "ke>{ke};"
                + "ko>{ko};"
                + "ky>{ki}|~y;"

                + "ma>{ma};"
                + "mi>{mi};"
                + "mu>{mu};"
                + "me>{me};"
                + "mo>{mo};"
                + "my>{mi}|~y;"
                
                + "m(p>{^n};"
                + "m(b>{^n};"
                + "m(f>{^n};"
                + "m(v>{^n};"

                + "na>{na};"
                + "ni>{ni};"
                + "nu>{nu};"
                + "ne>{ne};"
                + "no>{no};"
                + "ny>{ni}|~y;"

                + "o>{^o};"

                + "pa>{pa};"
                + "pi>{pi};"
                + "pu>{pu};"
                + "pe>{pe};"
                + "po>{po};"
                + "py>{pi}|~y;"

                + "ra>{ra};"
                + "ri>{ri};"
                + "ru>{ru};"
                + "re>{re};"
                + "ro>{ro};"
                + "ry>{ri}|~y;"

                + "sa>{sa};"
                + "si>{se}{~i};"
                + "su>{su};"
                + "se>{se};"
                + "so>{so};"

                + "shi>{si};"
                + "sh>{si}|~y;"
                
                + "ta>{ta};"
                + "ti>{te}{~i};"
                + "tu>{te}{~u};"
                + "te>{te};"
                + "to>{to};"
                
                + "tsu>{tu};"
                //+ "ts>{tu}|~;"

                + "u>{^u};"

                + "vu>{vu};"

                + "wa>{wa};"
                + "wi>{wi};"
                + "wu>{wu};"
                + "we>{we};"
                + "wo>{wo};"

                + "ya>{ya};"
                + "yi>{yi};"
                + "yu>{yu};"
                + "ye>{ye};"
                + "yo>{yo};"

                + "za>{za};"
                + "zi>{ze}{~i};"
                + "zu>{zu};"
                + "ze>{ze};"
                + "zo>{zo};"

                // small forms
                
                + "~a>{~a};"
                + "~i>{~i};"
                + "~u>{~u};"
                + "~e>{~e};"
                + "~o>{~o};"
                + "~ka>{~ka};"
                + "~ke>{~ke};"
                + "~tsu>{~tu};"
                + "~wa>{~wa};"
                + "~ya>{~ya};"
                + "~yi>{~yi};"
                + "~yu>{~yu};"
                + "~ye>{~ye};"
                + "~yo>{~yo};"

                // Double Consonants
                
                + "b(b>{~tu};"
                + "c(k>{~tu};"
                + "c(c>{~tu};"
                + "c(q>{~tu};"
                + "d(d>{~tu};"
                + "f(f>{~tu};"
                + "g(g>{~tu};"
                + "h(h>{~tu};"
                + "j(j>{~tu};"
                + "k(k>{~tu};"
                + "l(l>{~tu};"
                + "m(m>{~tu};"
                + "n(n>{~tu};"
                + "p(p>{~tu};"
                + "q(q>{~tu};"
                + "r(r>{~tu};"
                + "s(sh>{~tu};"
                + "s(s>{~tu};"
                + "t(ch>{~tu};"
                + "t(t>{~tu};"
                + "v(v>{~tu};"
                + "w(w>{~tu};"
                + "x(x>{~tu};"
                + "y(y>{~tu};"
                + "z(z>{~tu};"
                
                // ########################################
                // catch missing vowels!
                // These are to insure completeness, that
                // all romaji maps to kana
                // ########################################

                /*
                + "sh>{si};"
                + "ts>{tu};"
                + "ch>{ti};"
                + "dj>{di};"
                + "dz>{du};"
                */
                
                // the following are not really necessary, but produce
                // slightly more natural results.
                
              //masked: + "by>{bi};"
                + "cy>{se}{~i};"
                + "dy>{de}{~i};"
              //masked: + "gy>{gi};"
                + "hy>{hi};"
              //masked: + "ky>{ki};"
              //masked: + "my>{mi};"
              //masked: + "py>{pi};"
              //masked: + "ry>{ri};"
                + "sy>{se}{~i};"
                + "ty>{te}{~i};"
                + "zy>{ze}{~i};"

                // simple substitutions using backup
                
                + "c>|k;"
                + "f>{hu}|~;"
                + "j>{zi}|~y;"
                + "l>|r;"
                + "q>|k;" // backup and redo
                + "v>{vu}|~;"
                + "w>{^u}|~;"
                + "x>|ks;"

                // We had to list the longer ones first,
                // so here are the isolated consonants
                
                + "b>{bu};"
                + "d>{de};"
              //masked: + "f>{hu};"
                + "g>{gu};"
                + "h>{he};"
              //masked: + "j>{zi};"
                + "k>{ku};"
                + "m>{^n};"
                + "n>{^n};"
                + "p>{pu};"
                + "r>{ru};"
                + "s>{su};"
                + "t>{te};"
              //masked: + "v>{bu};"
              //masked: + "w>{^u};"
              //masked: + "x>{ku}{su};"
                + "y>{^i};"
                + "z>{zu};"
                
                // NOW KANA TO ROMAN

                + "gya<{gi}{~ya};"
                + "gyi<{gi}{~i};"
                + "gyu<{gi}{~yu};"
                + "gye<{gi}{~e};"
                + "gyo<{gi}{~yo};"
                
                + "ga<{ga};"
                + "gi<{gi};"
                + "gu<{gu};"
                + "ge<{ge};"
                + "go<{go};"

                + "kya<{ki}{~ya};"
                + "kyi<{ki}{~i};"
                + "kyu<{ki}{~yu};"
                + "kye<{ki}{~e};"
                + "kyo<{ki}{~yo};"

                + "ka<{ka};"
                + "ki<{ki};"
                + "ku<{ku};"
                + "ke<{ke};"
                + "ko<{ko};"

                + "ja<{zi}{~ya};"
                + "ji<{zi}{~i};"
                + "ju<{zi}{~yu};"
                + "je<{zi}{~e};"
                + "jo<{zi}{~yo};"
                + "ji<{zi};"
                
                + "za<{za};"
                + "zi<{ze}{~i};"
                + "zu<{zu};"
                + "ze<{ze};"
                + "zo<{zo};"

                + "sha<{si}{~ya};"
                + "shi<{si}{~i};"
                + "shu<{si}{~yu};"
                + "she<{si}{~e};"
                + "sho<{si}{~yo};"
                + "shi<{si};"
                
                + "sa<{sa};"
                + "si<{se}{~i};"
                + "su<{su};"
                + "se<{se};"
                + "so<{so};"

                + "dja<{di}{~ya};"
                + "dji<{di}{~i};"
                + "dju<{di}{~yu};"
                + "dje<{di}{~e};"
                + "djo<{di}{~yo};"
                + "dji<{di};"
                
                + "dzu<{du};"
                
                + "da<{da};"
                + "di<{de}{~i};"
                + "du<{de}{~u};"
                + "de<{de};"
                + "do<{do};"

                + "cha<{ti}{~ya};"
                + "chi<{ti}{~i};"
                + "chu<{ti}{~yu};"
                + "che<{ti}{~e};"
                + "cho<{ti}{~yo};"
                + "chi<{ti};"
                
                + "tsu<{tu};"
                
                + "ta<{ta};"
                + "ti<{te}{~i};"
                + "tu<{te}{~u};"
                + "te<{te};"
                + "to<{to};"

                + "nya<{ni}{~ya};"
                + "nyi<{ni}{~i};"
                + "nyu<{ni}{~yu};"
                + "nye<{ni}{~e};"
                + "nyo<{ni}{~yo};"
                
                + "na<{na};"
                + "ni<{ni};"
                + "nu<{nu};"
                + "ne<{ne};"
                + "no<{no};"

                + "bya<{bi}{~ya};"
                + "byi<{bi}{~i};"
                + "byu<{bi}{~yu};"
                + "bye<{bi}{~e};"
                + "byo<{bi}{~yo};"
                
                + "ba<{ba};"
                + "bi<{bi};"
                + "bu<{bu};"
                + "be<{be};"
                + "bo<{bo};"

                + "pya<{pi}{~ya};"
                + "pyi<{pi}{~i};"
                + "pyu<{pi}{~yu};"
                + "pye<{pi}{~e};"
                + "pyo<{pi}{~yo};"
                
                + "pa<{pa};"
                + "pi<{pi};"
                + "pu<{pu};"
                + "pe<{pe};"
                + "po<{po};"

                + "fa<{hu}{~a};"
                + "fi<{hu}{~i};"
                + "fe<{hu}{~e};"
                + "fo<{hu}{~o};"
                + "fu<{hu};"
                
                + "ha<{ha};"
                + "hi<{hi};"
                + "hu<{he}{~u};"
                + "he<{he};"
                + "ho<{ho};"

                + "mya<{mi}{~ya};"
                + "myi<{mi}{~i};"
                + "myu<{mi}{~yu};"
                + "mye<{mi}{~e};"
                + "myo<{mi}{~yo};"
                
                + "ma<{ma};"
                + "mi<{mi};"
                + "mu<{mu};"
                + "me<{me};"
                + "mo<{mo};"

                + "ya<{ya};"
                //+ "ye<{yi};"
                + "yu<{yu};"
                //+ "ye<{ye};"
                + "yo<{yo};"

                + "rya<{ri}{~ya};"
                + "ryi<{ri}{~i};"
                + "ryu<{ri}{~yu};"
                + "rye<{ri}{~e};"
                + "ryo<{ri}{~yo};"
                
                + "ra<{ra};"
                + "ri<{ri};"
                + "ru<{ru};"
                + "re<{re};"
                + "ro<{ro};"

                + "wa<{wa};"
                + "wi<{wi};"
                + "we<{we};"
                + "wo<{wo};"
                //+ "wu<{wu};"

                + "va<{vu}{~a};"
                + "vi<{vu}{~i};"
                + "ve<{vu}{~e};"
                + "vo<{vu}{~o};"
                + "vu<{vu};"
                
                // Doubled letters

                + "n''<{^n}({^a};"
                + "n''<{^n}({^i};"
                + "n''<{^n}({^u};"
                + "n''<{^n}({^e};"
                + "n''<{^n}({^o};"
                + "n''<{^n}({na};"
                + "n''<{^n}({ni};"
                + "n''<{^n}({nu};"
                + "n''<{^n}({ne};"
                + "n''<{^n}({no};"
                + "n''<{^n}({ya};"
                + "n''<{^n}({yu};"
                + "n''<{^n}({yo};"
                + "n''<{^n}({^n};"
                + "n<{^n};"
                
                + "n<{~tu}({n-start};"
                + "m<{~tu}({m-start};"
                + "w<{~tu}({w-start};"
                + "y<{~tu}({y-start};"
                + "g<{~tu}({g-start};"
                + "k<{~tu}({k-start};"
                + "z<{~tu}({z-start};"
                + "j<{~tu}({j-start};"
                + "s<{~tu}({s-start};"
                + "d<{~tu}({d-start};"
                + "t<{~tu}({t-start};"
                + "b<{~tu}({b-start};"
                + "p<{~tu}({p-start};"
                + "h<{~tu}({h-start};"
                + "f<{~tu}({f-start};"
                + "r<{~tu}({r-start};"
                + "v<{~tu}({v-start};"

                + "a<{^a};" // Moved this block down {aliu}
                + "i<{^i};"
                + "u<{^u};"
                + "e<{^e};"
                + "o<{^o};"
                
                // small forms
                
                + "~a<{~a};"
                + "~i<{~i};"
                + "~u<{~u};"
                + "~e<{~e};"
                + "~o<{~o};"
              //masked: + "~ka<{~ka};" ({~ka} is an alias for {~KA})
              //masked: + "~ke<{~ke};" ({~ke} is an alias for {~KE})
                + "~ya<{~ya};"
                + "~yu<{~yu};"
                + "~yo<{~yo};"
                + "~tsu<{~tu};"
                + "~wa<{~wa};"
                
                // length mark. Later, could use circumflex

                + "a<a){long};" // Liu
                + "e<e){long};" // Liu
                + "i<i){long};" // Liu
                + "o<o){long};" // Liu
                + "u<u){long};" // Liu

                //#######################################
                // Non-shared stuff goes here
                
                + "~>;"        // remove if not used
                + "{quote}>;"  // remove if not used
                //+ "<{quote};"
                + "->{long};"

            }
        };
    }
}
