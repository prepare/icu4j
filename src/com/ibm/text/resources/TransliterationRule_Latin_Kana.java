/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Kana.java,v $ 
 * $Date: 2000/07/05 23:07:58 $ 
 * $Revision: 1.7 $
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

            { "Rule", ""

              // Japanese hiragana and katakana to and from latin
              // (romaji).  Lower case latin corresponds to hiragana;
              // upper case latin to katakana.  The handling of
              // Hiragana and Katakana is largely the same.  The bulk
              // of the transliterator consists of two identical sets
              // of rules, differing only in case.

              // Because of minor differences between the two blocks
              // (e.g., the existence of small katakana ka and ke, but
              // no corresponding hiragana), some rules exist for only
              // one script.

              // Uses modified Hepburn. Small changes to make
              // unambiguous.

              //| Kunrei-shiki: Hepburn/MHepburn
              //| ------------------------------
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
              
              //| For foreign words:
              //| -----------------
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

              // Most small forms are generated, but if necessary
              // explicit small forms are given with ~a, ~ya, etc.

              //------------------------------------------------------
              // Variables
                
              + "$vowel=[aeiou];"
              + "$QUOTE='';"

              // Hiragana block

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
                
              // Alternates, just to make the rules easier
              + "$yi2=\u3043;"
              + "$yi=\u3044;"
              + "$ye2=\u3047;"
              + "$ye=\u3048;"
              + "$wu=$u;"
              // End alternates

              // Katakana block

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

              + "$KA2=\u30F5;" // Small Katakana KA; no Hiragana equiv.
              + "$KE2=\u30F6;" // Small Katakana KE; no Hiragana equiv.

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
                
              // Alternates, just to make the rules easier
              + "$YI2=\u30A3;"
              + "$YI=\u30A4;"
              + "$YE2=\u30A7;"
              + "$YE=\u30A8;"
              + "$WU=$U;"
              // End alternates

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
                
              // Variables used for doubled-consonants with tsu
                
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
                
              // If $N is followed by $N_QUOTER, then it needs an
              // apostrophe after its romaji form to disambiguate it.
              // E.g., $N $A != $NA, so represent as "n'a", not "na".

              + "$N_QUOTER = [$A $I $U $E $O $NA $NI $NU $NE $NO"
              + "             $YA $YU $YO $N];"

              + "$n_quoter = [$a $i $u $e $o $na $ni $nu $ne $no"
              + "             $ya $yu $yo $n];"

              // Lowercase copies for convenience in making hiragana
              // rule set copy

              + "$long = $LONG;"
              + "$quote = $QUOTE;"
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

              //------------------------------------------------------
              // Katakana rules

              // The rules immediately following are not shared.  That
              // is, they exist only for katakana, not for hiragana.

              + "VA<>$VA;"
              + "VI<>$VI;"
              + "VE<>$VE;"
              + "VO<>$VO;"
              + "'~KA'<>$KA2;"
              + "'~KE'<>$KE2;"

              // ~~~ BEGIN shared rules ~~~

              // The shared rules are copied from katakana to hiragana
              // and then mechanically lowercased.

              + "A<>$A;"

              + "BA<>$BA;"
              + "BYA<$BI$YA2;"
              + "BYI<$BI$I2;"
              + "BYU<$BI$YU2;"
              + "BYE<$BI$E2;"
              + "BYO<$BI$YO2;"
              + "BI<>$BI;"
              + "BU<>$BU;"
              + "BE<>$BE;"
              + "BO<>$BO;"
              + "BY>$BI|'~Y';"

              + "CHA<$TI$YA2;"
              + "CHI'~I'<$TI$I2;" // Liu
              + "CHU<$TI$YU2;"
              + "CHE<$TI$E2;"
              + "CHO<$TI$YO2;"
              + "CHI<>$TI;"
              + "CH>$TI|'~Y';"

              + "C}I>|S;"
              + "C}E>|S;"

              + "DA<>$DA;"
              + "DI<>$DE$I2;"
              + "DU<>$DE$U2;"
              + "DE<>$DE;"
              + "DO<>$DO;"
              + "DZU<>$DU;"
              + "DJA<$DI$YA2;"
              + "DJI'~I'<$DI$I2;" // Liu
              + "DJU<$DI$YU2;"
              + "DJE<$DI$E2;"
              + "DJO<$DI$YO2;"
              + "DJI<>$DI;"
              + "DJ>$DI|'~Y';"

              + "E<>$E;"

              + "FA<$HU$A2;"
              + "FI<$HU$I2;"
              + "FE<$HU$E2;"
              + "FO<$HU$O2;"
              + "FU<>$HU;"

              + "GA<>$GA;"
              + "GYA<$GI$YA2;"
              + "GYI<$GI$I2;"
              + "GYU<$GI$YU2;"
              + "GYE<$GI$E2;"
              + "GYO<$GI$YO2;"
              + "GI<>$GI;"
              + "GU<>$GU;"
              + "GE<>$GE;"
              + "GO<>$GO;"
              + "GY>$GI|'~Y';"

              + "HA<>$HA;"
              + "HI<>$HI;"
              + "HU<>$HE$U2;"
              + "HE<>$HE;"
              + "HO<>$HO;"

              + "I<>$I;"

              + "JA<$ZI$YA2;"
              + "JI'~I'<$ZI$I2;" // Liu
              + "JU<$ZI$YU2;"
              + "JE<$ZI$E2;"
              + "JO<$ZI$YO2;"
              + "JI<>$ZI;"

              + "KA<>$KA;"
              + "KYA<$KI$YA2;"
              + "KYI<$KI$I2;"
              + "KYU<$KI$YU2;"
              + "KYE<$KI$E2;"
              + "KYO<$KI$YO2;"
              + "KI<>$KI;"
              + "KU<>$KU;"
              + "KE<>$KE;"
              + "KO<>$KO;"
              + "KY>$KI|'~Y';"

              + "MA<>$MA;"
              + "MYA<$MI$YA2;"
              + "MYI<$MI$I2;"
              + "MYU<$MI$YU2;"
              + "MYE<$MI$E2;"
              + "MYO<$MI$YO2;"
              + "MI<>$MI;"
              + "MU<>$MU;"
              + "ME<>$ME;"
              + "MO<>$MO;"
              + "MY>$MI|'~Y';"
                
              + "M}P>$N;"
              + "M}B>$N;"
              + "M}F>$N;"
              + "M}V>$N;"

              + "NA<>$NA;"
              + "NYA<$NI$YA2;"
              + "NYI<$NI$I2;"
              + "NYU<$NI$YU2;"
              + "NYE<$NI$E2;"
              + "NYO<$NI$YO2;"
              + "NI<>$NI;"
              + "NU<>$NU;"
              + "NE<>$NE;"
              + "NO<>$NO;"
              + "NY>$NI|'~Y';"

              + "O<>$O;"

              + "PA<>$PA;"
              + "PYA<$PI$YA2;"
              + "PYI<$PI$I2;"
              + "PYU<$PI$YU2;"
              + "PYE<$PI$E2;"
              + "PYO<$PI$YO2;"
              + "PI<>$PI;"
              + "PU<>$PU;"
              + "PE<>$PE;"
              + "PO<>$PO;"
              + "PY>$PI|'~Y';"

              + "RA<>$RA;"
              + "RYA<$RI$YA2;"
              + "RYI<$RI$I2;"
              + "RYU<$RI$YU2;"
              + "RYE<$RI$E2;"
              + "RYO<$RI$YO2;"
              + "RI<>$RI;"
              + "RU<>$RU;"
              + "RE<>$RE;"
              + "RO<>$RO;"
              + "RY>$RI|'~Y';"

              + "SA<>$SA;"
              + "SI<>$SE$I2;"
              + "SU<>$SU;"
              + "SE<>$SE;"
              + "SO<>$SO;"

              + "SHA<$SI$YA2;"
              + "SHI'~I'<$SI$I2;" // Liu
              + "SHU<$SI$YU2;"
              + "SHE<$SI$E2;"
              + "SHO<$SI$YO2;"
              + "SHI<>$SI;"
              + "SH>$SI|'~Y';"
                
              + "TA<>$TA;"
              + "TI<>$TE$I2;"
              + "TU<>$TE$U2;"
              + "TE<>$TE;"
              + "TO<>$TO;"

              // Double consonants

              + "B}B<>$TU2}$B_START;"
              + "C}K>$TU2;"
              + "C}C>$TU2;"
              + "C}Q>$TU2;"
              + "D}D<>$TU2}$D_START;"
              + "F}F<>$TU2}$F_START;"
              + "G}G<>$TU2}$G_START;"
              + "H}H<>$TU2}$H_START;"
              + "J}J<>$TU2}$J_START;"
              + "K}K<>$TU2}$K_START;"
              + "L}L>$TU2;"
              + "M}M<>$TU2}$M_START;"
              + "N}N<>$TU2}$N_START;"
              + "P}P<>$TU2}$P_START;"
              + "Q}Q>$TU2;"
              + "R}R<>$TU2}$R_START;"
              + "S}SH>$TU2;"
              + "S}S<>$TU2}$S_START;"
              + "T}CH>$TU2;"
              + "T}T<>$TU2}$T_START;"
              + "V}V<>$TU2}$V_START;"
              + "W}W<>$TU2}$W_START;"
              + "X}X>$TU2;"
              + "Y}Y<>$TU2}$Y_START;"
              + "Z}Z<>$TU2}$Z_START;"
                
              + "TSU<>$TU;"

              + "U<>$U;"

              + "'V~A'<$VU$A2;" // Liu
              + "'V~I'<$VU$I2;" // Liu
              + "'V~E'<$VU$E2;" // Liu
              + "'V~O'<$VU$O2;" // Liu
              + "VU<>$VU;"

              + "WA<>$WA;"
              + "WI<>$WI;"
              + "WU>$WU;"
              + "WE<>$WE;"
              + "WO<>$WO;"

              + "YA<>$YA;"
              + "YI>$YI;"
              + "YU<>$YU;"
              + "YE>$YE;"
              + "YO<>$YO;"

              + "ZA<>$ZA;"
              + "ZI<>$ZE$I2;"
              + "ZU<>$ZU;"
              + "ZE<>$ZE;"
              + "ZO<>$ZO;"

              // Prolonged vowel mark. This indicates a doubling of
              // the preceding vowel sound in both katakana and
              // hiragana.

              + "A<A{$LONG;" // Liu
              + "E<E{$LONG;" // Liu
              + "I<I{$LONG;" // Liu
              + "O<O{$LONG;" // Liu
              + "U<U{$LONG;" // Liu

              // Small forms
                
              + "'~A'<>$A2;"
              + "'~I'<>$I2;"
              + "'~U'<>$U2;"
              + "'~E'<>$E2;"
              + "'~O'<>$O2;"
              + "'~TSU'<>$TU2;"
              + "'~WA'<>$WA2;"
              + "'~YA'<>$YA2;"
              + "'~YI'>$YI2;"
              + "'~YU'<>$YU2;"
              + "'~YE'>$YE2;"
              + "'~YO'<>$YO2;"

              // One-way latin->kana rules.  These do not occur in
              // well-formed romaji representing actual Japanese text.
              // Their purpose is to make all romaji map to kana of
              // some sort.
                
              // The following are not really necessary, but produce
              // slightly more natural results.
                
              + "CY>$SE$I2;"
              + "DY>$DE$I2;"
              + "HY>$HI;"
              + "SY>$SE$I2;"
              + "TY>$TE$I2;"
              + "ZY>$ZE$I2;"

              // Simple substitutions using backup
                
              + "C>|K;"
              + "F>$HU|'~';"
              + "J>$ZI|'~Y';"
              + "L>|R;"
              + "Q>|K;"
              + "V>$VU|'~';"
              + "W>$U|'~';"
              + "X>|KS;"

              // Isolated consonants listed here so as not to mask
              // longer rules above.
                
              + "B>$BU;"
              + "D>$DE;"
              + "G>$GU;"
              + "H>$HE;"
              + "K>$KU;"
              + "M>$N;"
              + "N''<$N}$N_QUOTER;"
              + "N<>$N;"
              + "P>$PU;"
              + "R>$RU;"
              + "S>$SU;"
              + "T>$TE;"
              + "Y>$I;"
              + "Z>$ZU;"
                
              // ~~~ END shared rules ~~~

              //------------------------------------------------------
              // Hiragana rules

              // Currently, there are no hiragana rules other than the
              // shared rules.
                
              // ~~~ BEGIN shared rules ~~~

              // The shared rules are copied from katakana to hiragana
              // and then mechanically lowercased.

              + "a<>$a;"

              + "ba<>$ba;"
              + "bya<$bi$ya2;"
              + "byi<$bi$i2;"
              + "byu<$bi$yu2;"
              + "bye<$bi$e2;"
              + "byo<$bi$yo2;"
              + "bi<>$bi;"
              + "bu<>$bu;"
              + "be<>$be;"
              + "bo<>$bo;"
              + "by>$bi|'~y';"

              + "cha<$ti$ya2;"
              + "chi'~i'<$ti$i2;" // liu
              + "chu<$ti$yu2;"
              + "che<$ti$e2;"
              + "cho<$ti$yo2;"
              + "chi<>$ti;"
              + "ch>$ti|'~y';"

              + "c}i>|s;"
              + "c}e>|s;"

              + "da<>$da;"
              + "di<>$de$i2;"
              + "du<>$de$u2;"
              + "de<>$de;"
              + "do<>$do;"
              + "dzu<>$du;"
              + "dja<$di$ya2;"
              + "dji'~i'<$di$i2;" // liu
              + "dju<$di$yu2;"
              + "dje<$di$e2;"
              + "djo<$di$yo2;"
              + "dji<>$di;"
              + "dj>$di|'~y';"

              + "e<>$e;"

              + "fa<$hu$a2;"
              + "fi<$hu$i2;"
              + "fe<$hu$e2;"
              + "fo<$hu$o2;"
              + "fu<>$hu;"

              + "ga<>$ga;"
              + "gya<$gi$ya2;"
              + "gyi<$gi$i2;"
              + "gyu<$gi$yu2;"
              + "gye<$gi$e2;"
              + "gyo<$gi$yo2;"
              + "gi<>$gi;"
              + "gu<>$gu;"
              + "ge<>$ge;"
              + "go<>$go;"
              + "gy>$gi|'~y';"

              + "ha<>$ha;"
              + "hi<>$hi;"
              + "hu<>$he$u2;"
              + "he<>$he;"
              + "ho<>$ho;"

              + "i<>$i;"

              + "ja<$zi$ya2;"
              + "ji'~i'<$zi$i2;" // liu
              + "ju<$zi$yu2;"
              + "je<$zi$e2;"
              + "jo<$zi$yo2;"
              + "ji<>$zi;"

              + "ka<>$ka;"
              + "kya<$ki$ya2;"
              + "kyi<$ki$i2;"
              + "kyu<$ki$yu2;"
              + "kye<$ki$e2;"
              + "kyo<$ki$yo2;"
              + "ki<>$ki;"
              + "ku<>$ku;"
              + "ke<>$ke;"
              + "ko<>$ko;"
              + "ky>$ki|'~y';"

              + "ma<>$ma;"
              + "mya<$mi$ya2;"
              + "myi<$mi$i2;"
              + "myu<$mi$yu2;"
              + "mye<$mi$e2;"
              + "myo<$mi$yo2;"
              + "mi<>$mi;"
              + "mu<>$mu;"
              + "me<>$me;"
              + "mo<>$mo;"
              + "my>$mi|'~y';"
                
              + "m}p>$n;"
              + "m}b>$n;"
              + "m}f>$n;"
              + "m}v>$n;"

              + "na<>$na;"
              + "nya<$ni$ya2;"
              + "nyi<$ni$i2;"
              + "nyu<$ni$yu2;"
              + "nye<$ni$e2;"
              + "nyo<$ni$yo2;"
              + "ni<>$ni;"
              + "nu<>$nu;"
              + "ne<>$ne;"
              + "no<>$no;"
              + "ny>$ni|'~y';"

              + "o<>$o;"

              + "pa<>$pa;"
              + "pya<$pi$ya2;"
              + "pyi<$pi$i2;"
              + "pyu<$pi$yu2;"
              + "pye<$pi$e2;"
              + "pyo<$pi$yo2;"
              + "pi<>$pi;"
              + "pu<>$pu;"
              + "pe<>$pe;"
              + "po<>$po;"
              + "py>$pi|'~y';"

              + "ra<>$ra;"
              + "rya<$ri$ya2;"
              + "ryi<$ri$i2;"
              + "ryu<$ri$yu2;"
              + "rye<$ri$e2;"
              + "ryo<$ri$yo2;"
              + "ri<>$ri;"
              + "ru<>$ru;"
              + "re<>$re;"
              + "ro<>$ro;"
              + "ry>$ri|'~y';"

              + "sa<>$sa;"
              + "si<>$se$i2;"
              + "su<>$su;"
              + "se<>$se;"
              + "so<>$so;"

              + "sha<$si$ya2;"
              + "shi'~i'<$si$i2;" // liu
              + "shu<$si$yu2;"
              + "she<$si$e2;"
              + "sho<$si$yo2;"
              + "shi<>$si;"
              + "sh>$si|'~y';"
                
              + "ta<>$ta;"
              + "ti<>$te$i2;"
              + "tu<>$te$u2;"
              + "te<>$te;"
              + "to<>$to;"

              // double consonants

              + "b}b<>$tu2}$b_start;"
              + "c}k>$tu2;"
              + "c}c>$tu2;"
              + "c}q>$tu2;"
              + "d}d<>$tu2}$d_start;"
              + "f}f<>$tu2}$f_start;"
              + "g}g<>$tu2}$g_start;"
              + "h}h<>$tu2}$h_start;"
              + "j}j<>$tu2}$j_start;"
              + "k}k<>$tu2}$k_start;"
              + "l}l>$tu2;"
              + "m}m<>$tu2}$m_start;"
              + "n}n<>$tu2}$n_start;"
              + "p}p<>$tu2}$p_start;"
              + "q}q>$tu2;"
              + "r}r<>$tu2}$r_start;"
              + "s}sh>$tu2;"
              + "s}s<>$tu2}$s_start;"
              + "t}ch>$tu2;"
              + "t}t<>$tu2}$t_start;"
              + "v}v<>$tu2}$v_start;"
              + "w}w<>$tu2}$w_start;"
              + "x}x>$tu2;"
              + "y}y<>$tu2}$y_start;"
              + "z}z<>$tu2}$z_start;"
                
              + "tsu<>$tu;"

              + "u<>$u;"

              + "'v~a'<$vu$a2;" // liu
              + "'v~i'<$vu$i2;" // liu
              + "'v~e'<$vu$e2;" // liu
              + "'v~o'<$vu$o2;" // liu
              + "vu<>$vu;"

              + "wa<>$wa;"
              + "wi<>$wi;"
              + "wu>$wu;"
              + "we<>$we;"
              + "wo<>$wo;"

              + "ya<>$ya;"
              + "yi>$yi;"
              + "yu<>$yu;"
              + "ye>$ye;"
              + "yo<>$yo;"

              + "za<>$za;"
              + "zi<>$ze$i2;"
              + "zu<>$zu;"
              + "ze<>$ze;"
              + "zo<>$zo;"

              // prolonged vowel mark. this indicates a doubling of
              // the preceding vowel sound in both katakana and
              // hiragana.

              + "a<a{$long;" // liu
              + "e<e{$long;" // liu
              + "i<i{$long;" // liu
              + "o<o{$long;" // liu
              + "u<u{$long;" // liu

              // small forms
                
              + "'~a'<>$a2;"
              + "'~i'<>$i2;"
              + "'~u'<>$u2;"
              + "'~e'<>$e2;"
              + "'~o'<>$o2;"
              + "'~tsu'<>$tu2;"
              + "'~wa'<>$wa2;"
              + "'~ya'<>$ya2;"
              + "'~yi'>$yi2;"
              + "'~yu'<>$yu2;"
              + "'~ye'>$ye2;"
              + "'~yo'<>$yo2;"

              // one-way latin->kana rules.  these do not occur in
              // well-formed romaji representing actual japanese text.
              // their purpose is to make all romaji map to kana of
              // some sort.
                
              // the following are not really necessary, but produce
              // slightly more natural results.
                
              + "cy>$se$i2;"
              + "dy>$de$i2;"
              + "hy>$hi;"
              + "sy>$se$i2;"
              + "ty>$te$i2;"
              + "zy>$ze$i2;"

              // simple substitutions using backup
                
              + "c>|k;"
              + "f>$hu|'~';"
              + "j>$zi|'~y';"
              + "l>|r;"
              + "q>|k;"
              + "v>$vu|'~';"
              + "w>$u|'~';"
              + "x>|ks;"

              // isolated consonants listed here so as not to mask
              // longer rules above.
                
              + "b>$bu;"
              + "d>$de;"
              + "g>$gu;"
              + "h>$he;"
              + "k>$ku;"
              + "m>$n;"
              + "n''<$n}$n_quoter;"
              + "n<>$n;"
              + "p>$pu;"
              + "r>$ru;"
              + "s>$su;"
              + "t>$te;"
              + "y>$i;"
              + "z>$zu;"
                
              // ~~~ END shared rules ~~~

              //------------------------------------------------------
              // Final cleanup
                
              + "'~'>;"     // delete stray tildes
              + "$quote>;"  // delete stray quotes
              + "'-'>$long;"
            }
        };
    }
}
