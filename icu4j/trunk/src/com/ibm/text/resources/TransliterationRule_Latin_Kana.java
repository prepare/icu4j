/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Kana.java,v $ 
 * $Date: 2000/05/23 16:47:48 $ 
 * $Revision: 1.6 $
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
                //# $Revision: 1.6 $
                // Transliteration rules for Japanese Hiragana and Katakana to
                // romaji
                // lower case roman generates hiragana.
                // upper case roman generates katakana.
                // Uses modified Hepburn. Small changes to make unambiguous.
                // Kunrei-shiki: Hepburn/MHepburn

                //| si:     shi
                //| si ~ya: sha
                //| si ~yu: shu
                //| si ~yo: sho
                //| zi:     ji
                //| zi ~ya: ja
                //| zi ~yu: ju
                //| zi ~yo: jo
                //| ti:     chi
                //| ti ~ya: cha
                //| ti ~yu: chu
                //| ti ~yu: cho
                //| tu:     tsu
                //| di:     ji/dji
                //| du:     zu/dzu
                //| hu:     fu
                //| // for foreign words
                //| se ~i   si
                //| si ~e   she
                //| 
                //| ze ~i   zi
                //| zi ~e   je
                //| 
                //| te ~i   ti
                //| ti ~e   che
                //| te ~u   tu
                //| 
                //| de ~i   di
                //| de ~u   du
                //| de ~i   di
                //| 
                //| he ~u:  hu
                //| hu ~a   fa
                //| hu ~i   fi
                //| hu ~e   he
                //| hu ~o   ho
                //| // Most small forms are generated, but if necessary
                //| // explicit small forms are given with ~a, ~ya, etc.

                //#######################################
                // Definitions of variables to be substituted
                //#######################################
                
                + "$vowel=[aeiou];"
                + "$quote='';"

                // now the kana
                + "$long=\u30FC;"

                + "$a2=\u3041;"
                + "$a=\u3042;"
                + "$i2=\u3043;"
                + "$i=\u3044;"
                + "$u2=\u3045;"
                + "$u=\u3046;"
                + "$e2=\u3047;"
                + "$e=\u3048;"
                + "$o2=\u3049;"
                + "$o=\u304A;"

                + "$ka=\u304B;"
                + "$ga=\u304C;"
                + "$ki=\u304D;"
                + "$gi=\u304E;"
                + "$ku=\u304F;"
                + "$gu=\u3050;"
                + "$ke=\u3051;"
                + "$ge=\u3052;"
                + "$ko=\u3053;"
                + "$go=\u3054;"

                //these are small katakana
                + "$ka2=\u30F5;"
                + "$ke2=\u30F6;"

                + "$sa=\u3055;"
                + "$za=\u3056;"
                + "$si=\u3057;"
                + "$zi=\u3058;"
                + "$su=\u3059;"
                + "$zu=\u305A;"
                + "$se=\u305B;"
                + "$ze=\u305C;"
                + "$so=\u305D;"
                + "$zo=\u305E;"

                + "$ta=\u305F;"
                + "$da=\u3060;"
                + "$ti=\u3061;"
                + "$di=\u3062;"
                + "$tu2=\u3063;"
                + "$tu=\u3064;"
                + "$du=\u3065;"
                + "$te=\u3066;"
                + "$de=\u3067;"
                + "$to=\u3068;"
                + "$do=\u3069;"

                + "$na=\u306A;"
                + "$ni=\u306B;"
                + "$nu=\u306C;"
                + "$ne=\u306D;"
                + "$no=\u306E;"

                + "$ha=\u306F;"
                + "$ba=\u3070;"
                + "$pa=\u3071;"
                + "$hi=\u3072;"
                + "$bi=\u3073;"
                + "$pi=\u3074;"
                + "$hu=\u3075;"
                + "$bu=\u3076;"
                + "$pu=\u3077;"
                + "$he=\u3078;"
                + "$be=\u3079;"
                + "$pe=\u307A;"
                + "$ho=\u307B;"
                + "$bo=\u307C;"
                + "$po=\u307D;"

                + "$ma=\u307E;"
                + "$mi=\u307F;"
                + "$mu=\u3080;"
                + "$me=\u3081;"
                + "$mo=\u3082;"

                + "$ya2=\u3083;"
                + "$ya=\u3084;"
                + "$yu2=\u3085;"
                + "$yu=\u3086;"
                + "$yo2=\u3087;"
                + "$yo=\u3088;"
                
                + "$ra=\u3089;"
                + "$ri=\u308A;"
                + "$ru=\u308B;"
                + "$re=\u308C;"
                + "$ro=\u308D;"

                + "$wa2=\u308E;"
                + "$wa=\u308F;"
                + "$wi=\u3090;"
                + "$we=\u3091;"
                + "$wo=\u3092;"

                + "$n=\u3093;"
                + "$vu=\u3094;"
                
                // alternates, just to make the rules easier
                + "$yi2=\u3043;"
                + "$yi=\u3044;"
                + "$ye2=\u3047;"
                + "$ye=\u3048;"
                + "$wu=$u;"
                // end alternates

                // Katakana

                + "$A2=\u30A1;"
                + "$A=\u30A2;"
                + "$I2=\u30A3;"
                + "$I=\u30A4;"
                + "$U2=\u30A5;"
                + "$U=\u30A6;"
                + "$E2=\u30A7;"
                + "$E=\u30A8;"
                + "$O2=\u30A9;"
                + "$O=\u30AA;"

                + "$KA=\u30AB;"
                + "$GA=\u30AC;"
                + "$KI=\u30AD;"
                + "$GI=\u30AE;"
                + "$KU=\u30AF;"
                + "$GU=\u30B0;"
                + "$KE=\u30B1;"
                + "$GE=\u30B2;"
                + "$KO=\u30B3;"
                + "$GO=\u30B4;"

                //these generate small katakana
                + "$KA2=\u30F5;"
                + "$KE2=\u30F6;"

                + "$SA=\u30B5;"
                + "$ZA=\u30B6;"
                + "$SI=\u30B7;"
                + "$ZI=\u30B8;"
                + "$SU=\u30B9;"
                + "$ZU=\u30BA;"
                + "$SE=\u30BB;"
                + "$ZE=\u30BC;"
                + "$SO=\u30BD;"
                + "$ZO=\u30BE;"

                + "$TA=\u30BF;"
                + "$DA=\u30C0;"
                + "$TI=\u30C1;"
                + "$DI=\u30C2;"
                + "$TU2=\u30C3;"
                + "$TU=\u30C4;"
                + "$DU=\u30C5;"
                + "$TE=\u30C6;"
                + "$DE=\u30C7;"
                + "$TO=\u30C8;"
                + "$DO=\u30C9;"

                + "$NA=\u30CA;"
                + "$NI=\u30CB;"
                + "$NU=\u30CC;"
                + "$NE=\u30CD;"
                + "$NO=\u30CE;"

                + "$HA=\u30CF;"
                + "$BA=\u30D0;"
                + "$PA=\u30D1;"
                + "$HI=\u30D2;"
                + "$BI=\u30D3;"
                + "$PI=\u30D4;"
                + "$HU=\u30D5;"
                + "$BU=\u30D6;"
                + "$PU=\u30D7;"
                + "$HE=\u30D8;"
                + "$BE=\u30D9;"
                + "$PE=\u30DA;"
                + "$HO=\u30DB;"
                + "$BO=\u30DC;"
                + "$PO=\u30DD;"

                + "$MA=\u30DE;"
                + "$MI=\u30DF;"
                + "$MU=\u30E0;"
                + "$ME=\u30E1;"
                + "$MO=\u30E2;"

                + "$YA2=\u30E3;"
                + "$YA=\u30E4;"
                + "$YU2=\u30E5;"
                + "$YU=\u30E6;"
                + "$YO2=\u30E7;"
                + "$YO=\u30E8;"
                + "$WA2=\u30EE;"
                
                // alternates, just to make the rules easier
                + "$YI2=\u30A3;"
                + "$YI=\u30A4;"
                + "$YE2=\u30A7;"
                + "$YE=\u30A8;"
                + "$WU=$U;"
                // end alternates

                + "$RA=\u30E9;"
                + "$RI=\u30EA;"
                + "$RU=\u30EB;"
                + "$RE=\u30EC;"
                + "$RO=\u30ED;"

                + "$VA=\u30F7;"
                + "$VI=\u30F8;"
                + "$VU=\u30F4;"
                + "$VE=\u30F9;"
                + "$VO=\u30FA;"

                + "$WA=\u30EF;"
                + "$WI=\u30F0;"
                + "$WE=\u30F1;"
                + "$WO=\u30F2;"

                + "$N=\u30F3;"
                + "$LONG=\u30FC;"
                + "$QUOTE='';"
                
                // Variables used for double-letters with tsu
                
                + "$K_START=[$KA$KI$KU$KE$KO$ka$ki$ku$ke$ko];"
                + "$G_START=[$GA$GI$GU$GE$GO$ga$gi$gu$ge$go];"
                
                + "$S_START=[$SA$SI$SU$SE$SO$sa$si$su$se$so];"
                + "$Z_START=[$ZA$ZU$ZE$ZO$za$zu$ze$zo];"
                + "$J_START=[$ZI$zi];"
                
                + "$T_START=[$TA$TI$TU$TE$TO$ta$ti$tu$te$to];"
                + "$D_START=[$DA$DI$DU$DE$DO$da$di$du$de$do];"
                
                + "$N_START=[$NA$NI$NU$NE$NO$na$ni$nu$ne$no];"
                
                + "$H_START=[$HA$HI$HE$HO$ha$hi$he$ho];"
                + "$F_START=[$HU$hu];"
                + "$B_START=[$BA$BI$BU$BE$BO$ba$bi$bu$be$bo];"
                + "$P_START=[$PA$PI$PU$PE$PO$pa$pi$pu$pe$po];"
                
                + "$M_START=[$MA$MI$MU$ME$MO$ma$mi$mu$me$mo];"
                
                + "$Y_START=[$YA$YU$YO$ya$yu$yo];"
                
                + "$R_START=[$RA$RI$RU$RE$RO$ra$ri$ru$re$ro];"
                
                + "$W_START=[$WA$WI$WE$WO$wa$wi$we$wo];"
                
                + "$V_START=[$VA$VI$VU$VE$VO$vu];"
                
                // lowercase copies for convenience in making hiragana

                + "$k_start=$K_START;"
                + "$g_start=$G_START;"
                + "$s_start=$S_START;"
                + "$z_start=$Z_START;"
                + "$j_start=$J_START;"
                + "$t_start=$T_START;"
                + "$d_start=$D_START;"
                + "$n_start=$N_START;"
                + "$h_start=$H_START;"
                + "$f_start=$F_START;"
                + "$b_start=$B_START;"
                + "$p_start=$P_START;"
                + "$m_start=$M_START;"
                + "$y_start=$Y_START;"
                + "$r_start=$R_START;"
                + "$w_start=$W_START;"
                + "$v_start=$V_START;"
 
                // remember that the order is very significant:
                // always put longer before shorter elements
                
                //#######################################
                // KATAKANA
                //#######################################

                + "VA<>$VA;"
                + "VI<>$VI;"
                + "VE<>$VE;"
                + "VO<>$VO;"

                //#######################################
                // KATAKANA SHARED
                // These are also used to produce hiragana, by lowercasing
                //#######################################
                
                + "A>$A;"

                + "BA>$BA;"
                + "BI>$BI;"
                + "BU>$BU;"
                + "BE>$BE;"
                + "BO>$BO;"
                + "BY>$BI|'~Y';"

                + "CHI>$TI;"
                + "CH>$TI|'~Y';"

                + "C}I>|S;"
                + "C}E>|S;"

                + "DA>$DA;"
                + "DI>$DE$I2;"
                + "DU>$DE$U2;"
                + "DE>$DE;"
                + "DO>$DO;"
                + "DZU>$DU;"
                + "DJI>$DI;"
                + "DJ>$DI|'~Y';"

                + "E>$E;"

                + "FU>$HU;"

                + "GA>$GA;"
                + "GI>$GI;"
                + "GU>$GU;"
                + "GE>$GE;"
                + "GO>$GO;"
                + "GY>$GI|'~Y';"

                + "HA>$HA;"
                + "HI>$HI;"
                + "HU>$HE$U2;"
                + "HE>$HE;"
                + "HO>$HO;"

                + "I>$I;"

                + "JI>$ZI;"

                + "KA>$KA;"
                + "KI>$KI;"
                + "KU>$KU;"
                + "KE>$KE;"
                + "KO>$KO;"
                + "KY>$KI|'~Y';"

                + "MA>$MA;"
                + "MI>$MI;"
                + "MU>$MU;"
                + "ME>$ME;"
                + "MO>$MO;"
                + "MY>$MI|'~Y';"
                
                + "M}P>$N;"
                + "M}B>$N;"
                + "M}F>$N;"
                + "M}V>$N;"

                + "NA>$NA;"
                + "NI>$NI;"
                + "NU>$NU;"
                + "NE>$NE;"
                + "NO>$NO;"
                + "NY>$NI|'~Y';"

                + "O>$O;"

                + "PA>$PA;"
                + "PI>$PI;"
                + "PU>$PU;"
                + "PE>$PE;"
                + "PO>$PO;"
                + "PY>$PI|'~Y';"

                + "RA>$RA;"
                + "RI>$RI;"
                + "RU>$RU;"
                + "RE>$RE;"
                + "RO>$RO;"
                + "RY>$RI|'~Y';"

                + "SA>$SA;"
                + "SI>$SE$I2;"
                + "SU>$SU;"
                + "SE>$SE;"
                + "SO>$SO;"

                + "SHI>$SI;"
                + "SH>$SI|'~Y';"
                
                + "TA>$TA;"
                + "TI>$TE$I2;"
                + "TU>$TE$U2;"
                + "TE>$TE;"
                + "TO>$TO;"
                
                + "TSU>$TU;"
                //+ "TS>$TU|'~';"

                + "U>$U;"

                + "VU>$VU;"

                + "WA>$WA;"
                + "WI>$WI;"
                + "WU>$WU;"
                + "WE>$WE;"
                + "WO>$WO;"

                + "YA>$YA;"
                + "YI>$YI;"
                + "YU>$YU;"
                + "YE>$YE;"
                + "YO>$YO;"

                + "ZA>$ZA;"
                + "ZI>$ZE$I2;"
                + "ZU>$ZU;"
                + "ZE>$ZE;"
                + "ZO>$ZO;"

                // SMALL FORMS
                
                + "'~A'>$A2;"
                + "'~I'>$I2;"
                + "'~U'>$U2;"
                + "'~E'>$E2;"
                + "'~O'>$O2;"
                + "'~KA'>$KA2;"
                + "'~KE'>$KE2;"
                + "'~TSU'>$TU2;"
                + "'~WA'>$WA2;"
                + "'~YA'>$YA2;"
                + "'~YI'>$YI2;"
                + "'~YU'>$YU2;"
                + "'~YE'>$YE2;"
                + "'~YO'>$YO2;"

                // DOUBLE CONSONANTS
                
                + "B}B>$TU2;"
                + "C}K>$TU2;"
                + "C}C>$TU2;"
                + "C}Q>$TU2;"
                + "D}D>$TU2;"
                + "F}F>$TU2;"
                + "G}G>$TU2;"
                + "H}H>$TU2;"
                + "J}J>$TU2;"
                + "K}K>$TU2;"
                + "L}L>$TU2;"
                + "M}M>$TU2;"
                + "N}N>$TU2;"
                + "P}P>$TU2;"
                + "Q}Q>$TU2;"
                + "R}R>$TU2;"
                + "S}SH>$TU2;"
                + "S}S>$TU2;"
                + "T}CH>$TU2;"
                + "T}T>$TU2;"
                + "V}V>$TU2;"
                + "W}W>$TU2;"
                + "X}X>$TU2;"
                + "Y}Y>$TU2;"
                + "Z}Z>$TU2;"
                
                // ########################################
                // CATCH MISSING VOWELS!
                // THESE ARE TO INSURE COMPLETENESS, THAT
                // ALL ROMAJI MAPS TO KANA
                // ########################################

                //| + "SH>$SI;"
                //| + "TS>$TU;"
                //| + "CH>$TI;"
                //| + "DJ>$DI;"
                //| + "DZ>$DU;"
                
                // THE FOLLOWING ARE NOT REALLY NECESSARY, BUT PRODUCE
                // SLIGHTLY MORE NATURAL RESULTS.
                
              //masked: + "BY>$BI;"
                + "CY>$SE$I2;"
                + "DY>$DE$I2;"
              //masked: + "GY>$GI;"
                + "HY>$HI;"
              //masked: + "KY>$KI;"
              //masked: + "MY>$MI;"
              //masked: + "PY>$PI;"
              //masked: + "RY>$RI;"
                + "SY>$SE$I2;"
                + "TY>$TE$I2;"
                + "ZY>$ZE$I2;"

                // SIMPLE SUBSTITUTIONS USING BACKUP
                
                + "C>|K;"
                + "F>$HU|'~';"
                + "J>$ZI|'~Y';"
                + "L>|R;"
                + "Q>|K;" // BACKUP AND REDO
                + "V>$VU|'~';"
                + "W>$U|'~';"
                + "X>|KS;"

                // WE HAD TO LIST THE LONGER ONES FIRST,
                // SO HERE ARE THE ISOLATED CONSONANTS
                
                + "B>$BU;"
                + "D>$DE;"
              //masked: + "F>$HU;"
                + "G>$GU;"
                + "H>$HE;"
              //masked: + "J>$ZI;"
                + "K>$KU;"
                + "M>$N;"
                + "N>$N;"
                + "P>$PU;"
                + "R>$RU;"
                + "S>$SU;"
                + "T>$TE;"
              //masked: + "V>$BU;"
              //masked: + "W>$U;"
              //masked: + "X>$KU$SU;"
                + "Y>$I;"
                + "Z>$ZU;"
                
                // NOW KANA TO ROMAN

                + "GYA<$GI$YA2;"
                + "GYI<$GI$I2;"
                + "GYU<$GI$YU2;"
                + "GYE<$GI$E2;"
                + "GYO<$GI$YO2;"
                
                + "GA<$GA;"
                + "GI<$GI;"
                + "GU<$GU;"
                + "GE<$GE;"
                + "GO<$GO;"

                + "KYA<$KI$YA2;"
                + "KYI<$KI$I2;"
                + "KYU<$KI$YU2;"
                + "KYE<$KI$E2;"
                + "KYO<$KI$YO2;"

                + "KA<$KA;"
                + "KI<$KI;"
                + "KU<$KU;"
                + "KE<$KE;"
                + "KO<$KO;"

                + "JA<$ZI$YA2;"
                + "JI'~I'<$ZI$I2;" // LIU
                + "JU<$ZI$YU2;"
                + "JE<$ZI$E2;"
                + "JO<$ZI$YO2;"
                + "JI<$ZI;"
                
                + "ZA<$ZA;"
                + "ZI<$ZE$I2;"
                + "ZU<$ZU;"
                + "ZE<$ZE;"
                + "ZO<$ZO;"

                + "SHA<$SI$YA2;"
                + "SHI'~I'<$SI$I2;" // LIU
                + "SHU<$SI$YU2;"
                + "SHE<$SI$E2;"
                + "SHO<$SI$YO2;"
                + "SHI<$SI;"
                
                + "SA<$SA;"
                + "SI<$SE$I2;"
                + "SU<$SU;"
                + "SE<$SE;"
                + "SO<$SO;"

                + "DJA<$DI$YA2;"
                + "DJI'~I'<$DI$I2;" // LIU
                + "DJU<$DI$YU2;"
                + "DJE<$DI$E2;"
                + "DJO<$DI$YO2;"
                + "DJI<$DI;"
                
                + "DZU<$DU;"
                
                + "DA<$DA;"
                + "DI<$DE$I2;"
                + "DU<$DE$U2;"
                + "DE<$DE;"
                + "DO<$DO;"

                + "CHA<$TI$YA2;"
                + "CHI'~I'<$TI$I2;" // LIU
                + "CHU<$TI$YU2;"
                + "CHE<$TI$E2;"
                + "CHO<$TI$YO2;"
                + "CHI<$TI;"
                
                + "TSU<$TU;"
                
                + "TA<$TA;"
                + "TI<$TE$I2;"
                + "TU<$TE$U2;"
                + "TE<$TE;"
                + "TO<$TO;"

                + "NYA<$NI$YA2;"
                + "NYI<$NI$I2;"
                + "NYU<$NI$YU2;"
                + "NYE<$NI$E2;"
                + "NYO<$NI$YO2;"
                
                + "NA<$NA;"
                + "NI<$NI;"
                + "NU<$NU;"
                + "NE<$NE;"
                + "NO<$NO;"

                + "BYA<$BI$YA2;"
                + "BYI<$BI$I2;"
                + "BYU<$BI$YU2;"
                + "BYE<$BI$E2;"
                + "BYO<$BI$YO2;"
                
                + "BA<$BA;"
                + "BI<$BI;"
                + "BU<$BU;"
                + "BE<$BE;"
                + "BO<$BO;"

                + "PYA<$PI$YA2;"
                + "PYI<$PI$I2;"
                + "PYU<$PI$YU2;"
                + "PYE<$PI$E2;"
                + "PYO<$PI$YO2;"
                
                + "PA<$PA;"
                + "PI<$PI;"
                + "PU<$PU;"
                + "PE<$PE;"
                + "PO<$PO;"

                + "FA<$HU$A2;"
                + "FI<$HU$I2;"
                + "FE<$HU$E2;"
                + "FO<$HU$O2;"
                + "FU<$HU;"
                
                + "HA<$HA;"
                + "HI<$HI;"
                + "HU<$HE$U2;"
                + "HE<$HE;"
                + "HO<$HO;"

                + "MYA<$MI$YA2;"
                + "MYI<$MI$I2;"
                + "MYU<$MI$YU2;"
                + "MYE<$MI$E2;"
                + "MYO<$MI$YO2;"
                
                + "MA<$MA;"
                + "MI<$MI;"
                + "MU<$MU;"
                + "ME<$ME;"
                + "MO<$MO;"

                + "YA<$YA;"
                //+ "YE<$YI;"
                + "YU<$YU;"
                //+ "YE<$YE;"
                + "YO<$YO;"

                + "RYA<$RI$YA2;"
                + "RYI<$RI$I2;"
                + "RYU<$RI$YU2;"
                + "RYE<$RI$E2;"
                + "RYO<$RI$YO2;"
                
                + "RA<$RA;"
                + "RI<$RI;"
                + "RU<$RU;"
                + "RE<$RE;"
                + "RO<$RO;"

                + "WA<$WA;"
                + "WI<$WI;"
                + "WE<$WE;"
                + "WO<$WO;"
                //+ "WU<$WU;"

                + "'V~A'<$VU$A2;" // LIU
                + "'V~I'<$VU$I2;" // LIU
                + "'V~E'<$VU$E2;" // LIU
                + "'V~O'<$VU$O2;" // LIU
                + "VU<$VU;"
                
                // DOUBLED LETTERS

                + "N''<$N}$A;"
                + "N''<$N}$I;"
                + "N''<$N}$U;"
                + "N''<$N}$E;"
                + "N''<$N}$O;"
                + "N''<$N}$NA;"
                + "N''<$N}$NI;"
                + "N''<$N}$NU;"
                + "N''<$N}$NE;"
                + "N''<$N}$NO;"
                + "N''<$N}$YA;"
                + "N''<$N}$YU;"
                + "N''<$N}$YO;"
                + "N''<$N}$N;"
                + "N<$N;"
                
                + "N<$TU2}$N_START;"
                + "M<$TU2}$M_START;"
                + "W<$TU2}$W_START;"
                + "Y<$TU2}$Y_START;"
                + "G<$TU2}$G_START;"
                + "K<$TU2}$K_START;"
                + "Z<$TU2}$Z_START;"
                + "J<$TU2}$J_START;"
                + "S<$TU2}$S_START;"
                + "D<$TU2}$D_START;"
                + "T<$TU2}$T_START;"
                + "B<$TU2}$B_START;"
                + "P<$TU2}$P_START;"
                + "H<$TU2}$H_START;"
                + "F<$TU2}$F_START;"
                + "R<$TU2}$R_START;"
                + "V<$TU2}$V_START;"

                + "A<$A;" // MOVED THIS BLOCK DOWN {aliu}
                + "I<$I;"
                + "U<$U;"
                + "E<$E;"
                + "O<$O;"
                
                // SMALL FORMS
                
                + "'~A'<$A2;"
                + "'~I'<$I2;"
                + "'~U'<$U2;"
                + "'~E'<$E2;"
                + "'~O'<$O2;"
                + "'~KA'<$KA2;"
                + "'~KE'<$KE2;"
                + "'~YA'<$YA2;"
                + "'~YU'<$YU2;"
                + "'~YO'<$YO2;"
                + "'~TSU'<$TU2;"
                + "'~WA'<$WA2;"
                
                // LENGTH MARK. LATER, COULD USE CIRCUMFLEX

                + "A<A{$LONG;" // LIU
                + "E<E{$LONG;" // LIU
                + "I<I{$LONG;" // LIU
                + "O<O{$LONG;" // LIU
                + "U<U{$LONG;" // LIU

                //#######################################
                // HIRAGANA
                // These are derived from the above by lowercasing
                //#######################################

                + "a>$a;"

                + "ba>$ba;"
                + "bi>$bi;"
                + "bu>$bu;"
                + "be>$be;"
                + "bo>$bo;"
                + "by>$bi|'~y';"

                + "chi>$ti;"
                + "ch>$ti|'~y';"

                + "c}i>|s;"
                + "c}e>|s;"

                + "da>$da;"
                + "di>$de$i2;"
                + "du>$de$u2;"
                + "de>$de;"
                + "do>$do;"
                + "dzu>$du;"
                + "dji>$di;"
                + "dj>$di|'~y';"

                + "e>$e;"

                + "fu>$hu;"

                + "ga>$ga;"
                + "gi>$gi;"
                + "gu>$gu;"
                + "ge>$ge;"
                + "go>$go;"
                + "gy>$gi|'~y';"

                + "ha>$ha;"
                + "hi>$hi;"
                + "hu>$he$u2;"
                + "he>$he;"
                + "ho>$ho;"

                + "i>$i;"

                + "ji>$zi;"

                + "ka>$ka;"
                + "ki>$ki;"
                + "ku>$ku;"
                + "ke>$ke;"
                + "ko>$ko;"
                + "ky>$ki|'~y';"

                + "ma>$ma;"
                + "mi>$mi;"
                + "mu>$mu;"
                + "me>$me;"
                + "mo>$mo;"
                + "my>$mi|'~y';"
                
                + "m}p>$n;"
                + "m}b>$n;"
                + "m}f>$n;"
                + "m}v>$n;"

                + "na>$na;"
                + "ni>$ni;"
                + "nu>$nu;"
                + "ne>$ne;"
                + "no>$no;"
                + "ny>$ni|'~y';"

                + "o>$o;"

                + "pa>$pa;"
                + "pi>$pi;"
                + "pu>$pu;"
                + "pe>$pe;"
                + "po>$po;"
                + "py>$pi|'~y';"

                + "ra>$ra;"
                + "ri>$ri;"
                + "ru>$ru;"
                + "re>$re;"
                + "ro>$ro;"
                + "ry>$ri|'~y';"

                + "sa>$sa;"
                + "si>$se$i2;"
                + "su>$su;"
                + "se>$se;"
                + "so>$so;"

                + "shi>$si;"
                + "sh>$si|'~y';"
                
                + "ta>$ta;"
                + "ti>$te$i2;"
                + "tu>$te$u2;"
                + "te>$te;"
                + "to>$to;"
                
                + "tsu>$tu;"
                //+ "ts>$tu|'~';"

                + "u>$u;"

                + "vu>$vu;"

                + "wa>$wa;"
                + "wi>$wi;"
                + "wu>$wu;"
                + "we>$we;"
                + "wo>$wo;"

                + "ya>$ya;"
                + "yi>$yi;"
                + "yu>$yu;"
                + "ye>$ye;"
                + "yo>$yo;"

                + "za>$za;"
                + "zi>$ze$i2;"
                + "zu>$zu;"
                + "ze>$ze;"
                + "zo>$zo;"

                // small forms
                
                + "'~a'>$a2;"
                + "'~i'>$i2;"
                + "'~u'>$u2;"
                + "'~e'>$e2;"
                + "'~o'>$o2;"
                + "'~ka'>$ka2;"
                + "'~ke'>$ke2;"
                + "'~tsu'>$tu2;"
                + "'~wa'>$wa2;"
                + "'~ya'>$ya2;"
                + "'~yi'>$yi2;"
                + "'~yu'>$yu2;"
                + "'~ye'>$ye2;"
                + "'~yo'>$yo2;"

                // Double Consonants
                
                + "b}b>$tu2;"
                + "c}k>$tu2;"
                + "c}c>$tu2;"
                + "c}q>$tu2;"
                + "d}d>$tu2;"
                + "f}f>$tu2;"
                + "g}g>$tu2;"
                + "h}h>$tu2;"
                + "j}j>$tu2;"
                + "k}k>$tu2;"
                + "l}l>$tu2;"
                + "m}m>$tu2;"
                + "n}n>$tu2;"
                + "p}p>$tu2;"
                + "q}q>$tu2;"
                + "r}r>$tu2;"
                + "s}sh>$tu2;"
                + "s}s>$tu2;"
                + "t}ch>$tu2;"
                + "t}t>$tu2;"
                + "v}v>$tu2;"
                + "w}w>$tu2;"
                + "x}x>$tu2;"
                + "y}y>$tu2;"
                + "z}z>$tu2;"
                
                // ########################################
                // catch missing vowels!
                // These are to insure completeness, that
                // all romaji maps to kana
                // ########################################

                //| + "sh>$si;"
                //| + "ts>$tu;"
                //| + "ch>$ti;"
                //| + "dj>$di;"
                //| + "dz>$du;"
                
                // the following are not really necessary, but produce
                // slightly more natural results.
                
              //masked: + "by>$bi;"
                + "cy>$se$i2;"
                + "dy>$de$i2;"
              //masked: + "gy>$gi;"
                + "hy>$hi;"
              //masked: + "ky>$ki;"
              //masked: + "my>$mi;"
              //masked: + "py>$pi;"
              //masked: + "ry>$ri;"
                + "sy>$se$i2;"
                + "ty>$te$i2;"
                + "zy>$ze$i2;"

                // simple substitutions using backup
                
                + "c>|k;"
                + "f>$hu|'~';"
                + "j>$zi|'~y';"
                + "l>|r;"
                + "q>|k;" // backup and redo
                + "v>$vu|'~';"
                + "w>$u|'~';"
                + "x>|ks;"

                // We had to list the longer ones first,
                // so here are the isolated consonants
                
                + "b>$bu;"
                + "d>$de;"
              //masked: + "f>$hu;"
                + "g>$gu;"
                + "h>$he;"
              //masked: + "j>$zi;"
                + "k>$ku;"
                + "m>$n;"
                + "n>$n;"
                + "p>$pu;"
                + "r>$ru;"
                + "s>$su;"
                + "t>$te;"
              //masked: + "v>$bu;"
              //masked: + "w>$u;"
              //masked: + "x>$ku$su;"
                + "y>$i;"
                + "z>$zu;"
                
                // NOW KANA TO ROMAN

                + "gya<$gi$ya2;"
                + "gyi<$gi$i2;"
                + "gyu<$gi$yu2;"
                + "gye<$gi$e2;"
                + "gyo<$gi$yo2;"
                
                + "ga<$ga;"
                + "gi<$gi;"
                + "gu<$gu;"
                + "ge<$ge;"
                + "go<$go;"

                + "kya<$ki$ya2;"
                + "kyi<$ki$i2;"
                + "kyu<$ki$yu2;"
                + "kye<$ki$e2;"
                + "kyo<$ki$yo2;"

                + "ka<$ka;"
                + "ki<$ki;"
                + "ku<$ku;"
                + "ke<$ke;"
                + "ko<$ko;"

                + "ja<$zi$ya2;"
                + "ji'~i'<$zi$i2;" // LIU
                + "ju<$zi$yu2;"
                + "je<$zi$e2;"
                + "jo<$zi$yo2;"
                + "ji<$zi;"
                
                + "za<$za;"
                + "zi<$ze$i2;"
                + "zu<$zu;"
                + "ze<$ze;"
                + "zo<$zo;"

                + "sha<$si$ya2;"
                + "shi'~i'<$si$i2;" // LIU
                + "shu<$si$yu2;"
                + "she<$si$e2;"
                + "sho<$si$yo2;"
                + "shi<$si;"
                
                + "sa<$sa;"
                + "si<$se$i2;"
                + "su<$su;"
                + "se<$se;"
                + "so<$so;"

                + "dja<$di$ya2;"
                + "dji'~i'<$di$i2;" // LIU
                + "dju<$di$yu2;"
                + "dje<$di$e2;"
                + "djo<$di$yo2;"
                + "dji<$di;"
                
                + "dzu<$du;"
                
                + "da<$da;"
                + "di<$de$i2;"
                + "du<$de$u2;"
                + "de<$de;"
                + "do<$do;"

                + "cha<$ti$ya2;"
                + "chi'~i'<$ti$i2;" // LIU
                + "chu<$ti$yu2;"
                + "che<$ti$e2;"
                + "cho<$ti$yo2;"
                + "chi<$ti;"
                
                + "tsu<$tu;"
                
                + "ta<$ta;"
                + "ti<$te$i2;"
                + "tu<$te$u2;"
                + "te<$te;"
                + "to<$to;"

                + "nya<$ni$ya2;"
                + "nyi<$ni$i2;"
                + "nyu<$ni$yu2;"
                + "nye<$ni$e2;"
                + "nyo<$ni$yo2;"
                
                + "na<$na;"
                + "ni<$ni;"
                + "nu<$nu;"
                + "ne<$ne;"
                + "no<$no;"

                + "bya<$bi$ya2;"
                + "byi<$bi$i2;"
                + "byu<$bi$yu2;"
                + "bye<$bi$e2;"
                + "byo<$bi$yo2;"
                
                + "ba<$ba;"
                + "bi<$bi;"
                + "bu<$bu;"
                + "be<$be;"
                + "bo<$bo;"

                + "pya<$pi$ya2;"
                + "pyi<$pi$i2;"
                + "pyu<$pi$yu2;"
                + "pye<$pi$e2;"
                + "pyo<$pi$yo2;"
                
                + "pa<$pa;"
                + "pi<$pi;"
                + "pu<$pu;"
                + "pe<$pe;"
                + "po<$po;"

                + "fa<$hu$a2;"
                + "fi<$hu$i2;"
                + "fe<$hu$e2;"
                + "fo<$hu$o2;"
                + "fu<$hu;"
                
                + "ha<$ha;"
                + "hi<$hi;"
                + "hu<$he$u2;"
                + "he<$he;"
                + "ho<$ho;"

                + "mya<$mi$ya2;"
                + "myi<$mi$i2;"
                + "myu<$mi$yu2;"
                + "mye<$mi$e2;"
                + "myo<$mi$yo2;"
                
                + "ma<$ma;"
                + "mi<$mi;"
                + "mu<$mu;"
                + "me<$me;"
                + "mo<$mo;"

                + "ya<$ya;"
                //+ "ye<$yi;"
                + "yu<$yu;"
                //+ "ye<$ye;"
                + "yo<$yo;"

                + "rya<$ri$ya2;"
                + "ryi<$ri$i2;"
                + "ryu<$ri$yu2;"
                + "rye<$ri$e2;"
                + "ryo<$ri$yo2;"
                
                + "ra<$ra;"
                + "ri<$ri;"
                + "ru<$ru;"
                + "re<$re;"
                + "ro<$ro;"

                + "wa<$wa;"
                + "wi<$wi;"
                + "we<$we;"
                + "wo<$wo;"
                //+ "wu<$wu;"

                + "va<$vu$a2;"
                + "vi<$vu$i2;"
                + "ve<$vu$e2;"
                + "vo<$vu$o2;"
                + "vu<$vu;"
                
                // Doubled letters

                + "n''<$n}$a;"
                + "n''<$n}$i;"
                + "n''<$n}$u;"
                + "n''<$n}$e;"
                + "n''<$n}$o;"
                + "n''<$n}$na;"
                + "n''<$n}$ni;"
                + "n''<$n}$nu;"
                + "n''<$n}$ne;"
                + "n''<$n}$no;"
                + "n''<$n}$ya;"
                + "n''<$n}$yu;"
                + "n''<$n}$yo;"
                + "n''<$n}$n;"
                + "n<$n;"
                
                + "n<$tu2}$n_start;"
                + "m<$tu2}$m_start;"
                + "w<$tu2}$w_start;"
                + "y<$tu2}$y_start;"
                + "g<$tu2}$g_start;"
                + "k<$tu2}$k_start;"
                + "z<$tu2}$z_start;"
                + "j<$tu2}$j_start;"
                + "s<$tu2}$s_start;"
                + "d<$tu2}$d_start;"
                + "t<$tu2}$t_start;"
                + "b<$tu2}$b_start;"
                + "p<$tu2}$p_start;"
                + "h<$tu2}$h_start;"
                + "f<$tu2}$f_start;"
                + "r<$tu2}$r_start;"
                + "v<$tu2}$v_start;"

                + "a<$a;" // Moved this block down {aliu}
                + "i<$i;"
                + "u<$u;"
                + "e<$e;"
                + "o<$o;"
                
                // small forms
                
                + "'~a'<$a2;"
                + "'~i'<$i2;"
                + "'~u'<$u2;"
                + "'~e'<$e2;"
                + "'~o'<$o2;"
              //masked: + "'~ka'<$ka2;" ({~ka} is an alias for {~KA})
              //masked: + "'~ke'<$ke2;" ({~ke} is an alias for {~KE})
                + "'~ya'<$ya2;"
                + "'~yu'<$yu2;"
                + "'~yo'<$yo2;"
                + "'~tsu'<$tu2;"
                + "'~wa'<$wa2;"
                
                // length mark. Later, could use circumflex

                + "a<a{$long;" // Liu
                + "e<e{$long;" // Liu
                + "i<i{$long;" // Liu
                + "o<o{$long;" // Liu
                + "u<u{$long;" // Liu

                //#######################################
                // Non-shared stuff goes here
                
                + "'~'>;"        // remove if not used
                + "$quote>;"  // remove if not used
                //+ "<$quote;"
                + "'-'>$long;"

            }
        };
    }
}
