/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Jamo.java,v $ 
 * $Date: 2000/04/27 22:17:47 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;
import java.util.ListResourceBundle;

public class TransliterationRule_Latin_Jamo extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule", ""

  // VARIABLES

  + "$initial=[\u1100-\u115F];"
  + "$medial=[\u1160-\u11A7];"
  + "$final=[\u11A8-\u11F9];" // added - aliu
  + "$vowel=[aeiouwyAEIOUWY$medial];"
              // following line used to read "..$medial$final]"
              // assume this was a typo - liu
  + "$consonant=[bcdfghjklmnpqrstvxzBCDFGHJKLMNPQRSTVXZ$initial$final];"
  + "$ye=[yeYE];"
  + "$ywe=[yweYWE];"
  + "$yw=[ywYW];"
  + "$nl=[nlNL];"
  + "$gnl=[gnlGNL];"
  + "$lsgb=[lsgbLSGB];"
  + "$ywao=[ywaoYWAO];"
  + "$bl=[blBL];"

  // RULES

  // Hangul structure is IMF or IM
  // So you can have, because of adjacent sequences
  // IM, but not II or IF
  // MF or MI, but not MM
  // FI, but not FF or FM

  // For English, we just have C or V.
  // To generate valid Hangul:
  // Vowels:
  // We insert IEUNG between VV, and otherwise map V to M
  // We also insert IEUNG if there is no
  // Consonants:
  // We don't break doubles
  // Cases like lmgg, we have to break at lm
  // So to guess whether a consonant is I or F
  // we map all C's to F, except when followed by a vowel, e.g.
  // X[{vowel}>CHOSEONG (initial)
  // X>JONGSEONG (final)

  // special insertion for funny sequences of vowels, and for empty consonant

  + "'' < $consonant{\u110B;" // insert a break between any consonant and the empty consonant.
  + "$medial{}$vowel<>\u110B;"  // HANGUL CHOSEONG IEUNG
  
  // Below, insert an empty consonant in front of a vowel, if there is no Initial in front.
  
  // Fix casing.
  // Because Korean is caseless, we just want to treat everything as
  // lowercase.
  // we could do this by always preceeding this transliterator with
  // an upper-lowercase transformation, but that wouldn't invert nicely.
  // We use the "revisit" syntax to just convert latin to latin
  // so that we can avoid
  // having to restate all the Latin=>Jamo rules, with the I/F handling.

  // We don't have to add titlecase, since that will be picked up
  // since the first letter is converted, then revisited. E.g.
  // |Gg => |gg => {sang kiyeok}
  // We do have to have all caps, since otherwise we could get:
  // |GG => |gG => {kiyeok}|G => {kiyeok}|g => {kiyeok}{kiyeok}

  + "Z > |z;"
  + "YU > |yu;"
  + "YO > |yo;"
  + "YI > |yi;"
  + "YEO > |yeo;"
  + "YE > |ye;"
  + "YAE > |yae;"
  + "YA > |ya;"
  + "Y > |y;"
  + "WI > |wi;"
  + "WEO > |weo;"
  + "WE > |we;"
  + "WAE > |wae;"
  + "WA > |wa;"
  + "W > |w;"
  + "U > |u;"
  + "T > |t;"
  + "SS > |ss;"
  + "S > |s;"
  + "P > |p;"
  + "OE > |oe;"
  + "O > |o;"
  + "NJ > |nj;"
  + "NH > |nh;"
  + "NG > |ng;"
  + "N > |n;"
  + "M > |m;"
  + "LT > |lt;"
  + "LS > |ls;"
  + "LP > |lp;"
  + "LM > |lm;"
  + "LH > |lh;"
  + "LG > |lg;"
  + "LB > |lb;"
  + "L > |l;"
  + "K > |k;"
  + "JJ > |jj;"
  + "J > |j;"
  + "I > |i;"
  + "H > |h;"
  + "GS > |gs;"
  + "GG > |gg;"
  + "G > |g;"
  + "EU > |eu;"
  + "EO > |eo;"
  + "E > |e;"
  + "DD > |dd;"
  + "D > |d;"
  + "BS > |bs;"
  + "BB > |bb;"
  + "B > |b;"
  + "AE > |ae;"
  + "A > |a;"

  // APOSTROPHE

  // As always, an apostrophe is used to separate digraphs into
  // singles. That is, if you really wanted [KAN][GGAN], instead
  // of [KANG][GAN] you would write "kan'ggan".

  // Rules for inserting ' when mapping separated digraphs back
  // from Hangul to Latin. Catch every letter that can be the
  // LAST of a digraph (or multigraph) AND first of an initial

  + "'' < l{ }\u11c0;"      // hangul jongseong thieuth
  + "'' < $lsgb{}\u11ba;" // hangul jongseong sios
  + "'' < l{ }\u11c1;"      // hangul jongseong phieuph
  + "'' < l{ }\u11b7;"      // hangul jongseong mieum
  + "'' < n{ }\u11bd;"      // hangul jongseong cieuc
  + "'' < $nl{}\u11c2;"   // hangul jongseong hieuh
  + "'' < $gnl{}\u11a9;"  // hangul jongseong ssangkiyeok
  + "'' < $bl{}\u11b8;"   // hangul jongseong pieup
  + "'' < d{ }\u11ae;"      // hangul jongseong tikeut
  
  + "'' < $ye{}\u116e;"   // hangul jungseong u
  + "'' < $ywe{}\u1169;"  // hangul jungseong o
  + "'' < $yw{}\u1175;"   // hangul jungseong i
  + "'' < $ywao{}\u1166;" // hangul jungseong e
  + "'' < $yw{}\u1161;"   // hangul jungseong a
  
  + "'' < l{ }\u1110;"      // hangul choseong thieuth
  + "'' < $lsgb{}\u110a;" // hangul choseong ssangsios
  + "'' < $lsgb{}\u1109;" // hangul choseong sios
  + "'' < l{ }\u1111;"      // hangul choseong phieuph
  + "'' < l{ }\u1106;"      // hangul choseong mieum
  + "'' < n{ }\u110c;"      // hangul choseong cieuc
  + "'' < n{ }\u110d;"
  + "'' < $nl{}\u1112;"   // hangul choseong hieuh
  + "'' < $gnl{}\u1101;"  // hangul choseong ssangkiyeok
  + "'' < $gnl{}\u1100;"  // hangul choseong kiyeok
  + "'' < d{ }\u1103;"      // hangul choseong tikeut
  + "'' < d{ }\u1104;"
  + "'' < $bl{}\u1107;"   // hangul choseong pieup
  + "'' < $bl{}\u1108;"

  // INITIALS

  + "t }$vowel<>\u1110;"   // hangul choseong thieuth
  + "ss }$vowel<>\u110a;"  // hangul choseong ssangsios
  + "s }$vowel<>\u1109;"   // hangul choseong sios
  + "p }$vowel<>\u1111;"   // hangul choseong phieuph
  + "n }$vowel<>\u1102;"   // hangul choseong nieun
  + "m }$vowel<>\u1106;"   // hangul choseong mieum
  + "l }$vowel<>\u1105;"   // hangul choseong rieul
  + "k }$vowel<>\u110f;"   // hangul choseong khieukh
  + "j }$vowel<>\u110c;"   // hangul choseong cieuc
  + "h }$vowel<>\u1112;"   // hangul choseong hieuh
  + "gg }$vowel<>\u1101;"  // hangul choseong ssangkiyeok
  + "g }$vowel<>\u1100;"   // hangul choseong kiyeok
  + "d }$vowel<>\u1103;"   // hangul choseong tikeut
  + "c }$vowel<>\u110e;"   // hangul choseong chieuch
  + "b }$vowel<>\u1107;"   // hangul choseong pieup  
  + "bb }$vowel<>\u1108;"
  + "jj }$vowel<>\u110d;"
  + "dd }$vowel<>\u1104;"
  
  // If we have gotten through to these rules, and we start with
  // a consonant, then the remaining mappings would be to F,
  // because must have CC (or C<non-letter>), not CV.
  // If we have F before us, then
  // we would end up with FF, which is wrong. The simplest fix is
  // to still make it an initial, but also insert an "u",
  // so we end up with F, I, u, and then continue with the C

  // special, only initial
  + "bb > \u1108\u116e;"  // hangul choseong ssangpieup
  + "jj > \u1108\u110d;"   // hangul choseong ssangcieuc
  + "dd > \u1108\u1104;"   // hangul choseong ssangtikeut
  
  + "$final{ t > \u1110\u116e;"   // hangul choseong thieuth
  + "$final{ ss > \u110a\u116e;"  // hangul choseong ssangsios
  + "$final{ s > \u1109\u116e;"   // hangul choseong sios
  + "$final{ p > \u1111\u116e;"   // hangul choseong phieuph
  + "$final{ n > \u1102\u116e;"   // hangul choseong nieun
  + "$final{ m > \u1106\u116e;"   // hangul choseong mieum
  + "$final{ l > \u1105\u116e;"   // hangul choseong rieul
  + "$final{ k > \u110f\u116e;"   // hangul choseong khieukh
  + "$final{ j > \u110c\u116e;"   // hangul choseong cieuc
  + "$final{ h > \u1112\u116e;"   // hangul choseong hieuh
  + "$final{ gg > \u1101\u116e;"  // hangul choseong ssangkiyeok
  + "$final{ g > \u1100\u116e;"   // hangul choseong kiyeok
  + "$final{ d > \u1103\u116e;"   // hangul choseong tikeut
  + "$final{ c > \u110e\u116e;"   // hangul choseong chieuch
  + "$final{ b > \u1107\u116e;"   // hangul choseong pieup
  
  // MEDIALS after INITIALS
  
  + "$initial{ yu <> \u1172;"   // hangul jungseong yu
  + "$initial{ yo <> \u116d;"   // hangul jungseong yo
  + "$initial{ yi <> \u1174;"   // hangul jungseong yi
  + "$initial{ yeo <> \u1167;"  // hangul jungseong yeo
  + "$initial{ ye <> \u1168;"   // hangul jungseong ye
  + "$initial{ yae <> \u1164;"  // hangul jungseong yae
  + "$initial{ ya <> \u1163;"   // hangul jungseong ya
  + "$initial{ wi <> \u1171;"   // hangul jungseong wi
  + "$initial{ weo <> \u116f;"  // hangul jungseong weo
  + "$initial{ we <> \u1170;"   // hangul jungseong we
  + "$initial{ wae <> \u116b;"  // hangul jungseong wae
  + "$initial{ wa <> \u116a;"   // hangul jungseong wa
  + "$initial{ u <> \u116e;"    // hangul jungseong u
  + "$initial{ oe <> \u116c;"   // hangul jungseong oe
  + "$initial{ o <> \u1169;"    // hangul jungseong o
  + "$initial{ i <> \u1175;"    // hangul jungseong i
  + "$initial{ eu <> \u1173;"   // hangul jungseong eu
  + "$initial{ eo <> \u1165;"   // hangul jungseong eo
  + "$initial{ e <> \u1166;"    // hangul jungseong e
  + "$initial{ ae <> \u1162;"   // hangul jungseong ae
  + "$initial{ a <> \u1161;"    // hangul jungseong a
  
  // MEDIALS (vowels) not after INITIALs
  
  + "yu > \u110B\u1172;"   // hangul jungseong yu
  + "yo > \u110B\u116d;"   // hangul jungseong yo
  + "yi > \u110B\u1174;"   // hangul jungseong yi
  + "yeo > \u110B\u1167;"  // hangul jungseong yeo
  + "ye > \u110B\u1168;"   // hangul jungseong ye
  + "yae > \u110B\u1164;"  // hangul jungseong yae
  + "ya > \u110B\u1163;"   // hangul jungseong ya
  + "wi > \u110B\u1171;"   // hangul jungseong wi
  + "weo > \u110B\u116f;"  // hangul jungseong weo
  + "we > \u110B\u1170;"   // hangul jungseong we
  + "wae > \u110B\u116b;"  // hangul jungseong wae
  + "wa > \u110B\u116a;"   // hangul jungseong wa
  + "u > \u110B\u116e;"    // hangul jungseong u
  + "oe > \u110B\u116c;"   // hangul jungseong oe
  + "o > \u110B\u1169;"    // hangul jungseong o
  + "i > \u110B\u1175;"    // hangul jungseong i
  + "eu > \u110B\u1173;"   // hangul jungseong eu
  + "eo > \u110B\u1165;"   // hangul jungseong eo
  + "e > \u110B\u1166;"    // hangul jungseong e
  + "ae > \u110B\u1162;"   // hangul jungseong ae
  + "a > \u110B\u1161;"    // hangul jungseong a
  

  // FINALS
  
  + "t <> \u11c0;"    // hangul jongseong thieuth
  + "ss <> \u11bb;"   // hangul jongseong ssangsios
  + "s <> \u11ba;"    // hangul jongseong sios
  + "p <> \u11c1;"    // hangul jongseong phieuph
  + "nj <> \u11ac;"   // hangul jongseong nieun-cieuc
  + "nh <> \u11ad;"   // hangul jongseong nieun-hieuh
  + "ng <> \u11bc;"   // hangul jongseong ieung
  + "n <> \u11ab;"    // hangul jongseong nieun
  + "m <> \u11b7;"    // hangul jongseong mieum
  + "lt <> \u11b4;"   // hangul jongseong rieul-thieuth
  + "ls <> \u11b3;"   // hangul jongseong rieul-sios
  + "lp <> \u11b5;"   // hangul jongseong rieul-phieuph
  + "lm <> \u11b1;"   // hangul jongseong rieul-mieum
  + "lh <> \u11b6;"   // hangul jongseong rieul-hieuh
  + "lg <> \u11b0;"   // hangul jongseong rieul-kiyeok
  + "lb <> \u11b2;"   // hangul jongseong rieul-pieup
  + "l <> \u11af;"    // hangul jongseong rieul
  + "k <> \u11bf;"    // hangul jongseong khieukh
  + "j <> \u11bd;"    // hangul jongseong cieuc
  + "h <> \u11c2;"    // hangul jongseong hieuh
  + "gs <> \u11aa;"   // hangul jongseong kiyeok-sios
  + "gg <> \u11a9;"   // hangul jongseong ssangkiyeok
  + "g <> \u11a8;"    // hangul jongseong kiyeok
  + "d <> \u11ae;"    // hangul jongseong tikeut
  + "c <> \u11be;"     // hangul jongseong chieuch
  + "bs <> \u11b9;"   // hangul jongseong pieup-sios
  + "b <> \u11b8;"    // hangul jongseong pieup

  // extra English letters
  // {moved to bottom - aliu}

  + "z > |s;"
  //{ + "Z > |s;" } masked
  + "x > |ks;"
  + "X > |ks;"
  + "v > |b;"
  + "V > |b;"
  + "r > |l;"
  + "R > |l;"
  + "q > |k;"
  + "Q > |k;"
  + "f > |p;"
  + "F > |p;"
  //{ + "c > |k;" } masked
  + "C > |k;"
  
  + "y > \u1172;"   // hangul jungseong yu
  + "w > \u1171;"   // hangul jungseong wi
  

  // ====================================
  // Normal final rule: remove '
  // ====================================

  + "''>;"
            }
        };
    }
}
