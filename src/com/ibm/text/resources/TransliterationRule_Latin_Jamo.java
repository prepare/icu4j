/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Jamo.java,v $ 
 * $Date: 2000/05/01 20:56:34 $ 
 * $Revision: 1.7 $
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
  + "$INITIAL=[bcdghjklmnpst];"
  + "$medial=[\u1160-\u11A7];"
  + "$MEDIAL=[aeiou];" // as a left context
  + "$comp_med=[\u1160\u1176-\u11A7];" // compound medials and filler
  + "$final=[\u11A8-\u11F9];" // added - aliu
  + "$vowel=[aeiouwy$medial];"
              // following line used to read "..$medial$final]"
              // assume this was a typo - liu
  + "$consonant=[bcdfghjklmnpqrstvxz$initial$final];"
  + "$ye_=[yeYE];"
  + "$ywe_=[yweYWE];"
  + "$yw_=[ywYW];"
  + "$nl_=[nlNL];"
  + "$gnl_=[gnlGNL];"
  + "$lsgb_=[lsgbLSGB];"
  + "$ywao_=[ywaoYWAO];"
  + "$bl_=[blBL];"

              + "$ieung = \u110b;"

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

  // Below, insert an empty consonant in front of a vowel, if there is no Initial in front.


// General strategy.
// 
// 1. We support both the normal Jamo block, 1100 - 117F, and the
// compatibility block, 3130 - 318F.  The former uses lowercase latin;
// the latter uses uppercase.  See notes below for details of the
// compatibility block.  Remaining items in this list pertain to the
// normal Jamo block.
// 
// 2. Canonical syllables should transliterate without special
// characters.  Canonical syllables are either IMF or IM.
// 
// 3. We want to support round-trip integrity from jamo to latin and back
// to Jamo.  To do this we have to mark the jamo with special characters
// when they occur in non-canonical positions.
// 
// 4. When initial jamo occur in a non-canonical position, they are
// marked with a leading '['.
// 
// 5. When final jamo occur in a non-canonical position, they are marked
// with a trailing ']'.
// 
// 6. When medial jamo occur in a non-canonical position, they are marked
// with a leading '~'.
// 
// 7. Compound jamo characters are handled by enclosing them in
// parentheses.  Initials are '((x)', medials are '(x)', and finals are
// '(x))'.
// 
// 8. Disambiguation of 'g' + 'g' vs. 'gg' is accomplished by inserting a
// '' character between them.
// 
// 9. IEUNG is used to mark medials not occuring after initials.
// Isolated IEUNG is transliterated as a back tick.
// 
// 10. Some old special case and completeness rules have been commented
// out.  These can be reintroduced (and the existing rules modified as
// needed) so long as round-trip integrity is maintained.
  
// We use the uppercase latin letters for the compatibility Jamo
// U+3130 - U+318F.  The following rules are generated
// programmatically by a perl script that analyzes the Unicode
// database.  These rules are much simpler because there are no
// separate code points for initial vs. final consonants, so no
// contextual rules are needed.  The one wrinkle is, as usual, the
// need to distinguish doubles from two singles, that is, GG vs G G.
// The perl script finds these special cases by exhaustive search and
// adds only the minimal rules needed to resolve these cases.  The one
// modification that is made by hand is to replace '' with '/' so as
// not to conflict with the normal IEUNG in the standard Jamo range. -
// liu
+ "A '' <> {\u314F} [\u3153\u3161\u3154];"
+ "B '' <> {\u3142} [\u3142\u3143];"
+ "D '' <> {\u3137} [\u3137\u3138];"
+ "E '' <> {\u3154} [\u315A\u3157\u315C];"
+ "G '' <> {\u3131} [\u3132\u3133\u3131\u3146\u3145];"
+ "J '' <> {\u3148} [\u3149\u3148];"
+ "L '' <> {\u3139} [\u3132\u3133\u3131\u3141\u3142\u3143\u3146\u3145\u314C\u314D];"
+ "N '' <> {\u3134} [\u3149\u3148\u314E];"
+ "O '' <> {\u3157} [\u3153\u3161\u3154];"
+ "S '' <> {\u3145} [\u3146\u3145];"
+ "WA '' <> {\u3158} [\u3153\u3161\u3154];"
+ "WE '' <> {\u315E} [\u315A\u3157];"
+ "YA '' <> {\u3151} [\u3153\u3161\u3154];"
+ "YE '' <> {\u3156} [\u315A\u3157];"
+ "YU <> \u3160;"
+ "YO <> \u315B;"
+ "YI <> \u3162;"
+ "YEO <> \u3155;"
+ "YE <> \u3156;"
+ "YAE <> \u3152;"
+ "YA <> \u3151;"
+ "WI <> \u315F;"
+ "WEO <> \u315D;"
+ "WE <> \u315E;"
+ "WAE <> \u3159;"
+ "WA <> \u3158;"
+ "U <> \u315C;"
+ "T <> \u314C;"
+ "S S <> \u3146;"
+ "S <> \u3145;"
+ "P <> \u314D;"
+ "OE <> \u315A;"
+ "O <> \u3157;"
+ "N J <> \u3135;"
+ "N H <> \u3136;"
+ "N <> \u3134;"
+ "M <> \u3141;"
+ "L T <> \u313E;"
+ "L S <> \u313D;"
+ "L P <> \u313F;"
+ "L M <> \u313B;"
+ "L G <> \u313A;"
+ "L B <> \u313C;"
+ "L <> \u3139;"
+ "K <> \u314B;"
+ "J J <> \u3149;"
+ "J <> \u3148;"
+ "I <> \u3163;"
+ "H <> \u314E;"
+ "G S <> \u3133;"
+ "G G <> \u3132;"
+ "G <> \u3131;"
+ "EU <> \u3161;"
+ "EO <> \u3153;"
+ "E <> \u3154;"
+ "D D <> \u3138;"
+ "D <> \u3137;"
+ "C <> \u314A;"
+ "B B <> \u3143;"
+ "B <> \u3142;"
+ "AE <> \u3150;"
+ "A <> \u314F;"
+ "'/' <> \u3147;"
+ "'(' YU YEO ')' <> \u318A;"
+ "'(' YU YE ')' <> \u318B;"
+ "'(' YU I ')' <> \u318C;"
+ "'(' YR ')' <> \u3186;"
+ "'(' YO YAE ')' <> \u3188;"
+ "'(' YO YA ')' <> \u3187;"
+ "'(' YO I ')' <> \u3189;"
+ "'(' YES S ')' <> \u3182;"
+ "'(' YES PAN ')' <> \u3183;"
+ "'(' YES ')' <> \u3181;"
+ "'(' S N ')' <> \u317B;"
+ "'(' S J ')' <> \u317E;"
+ "'(' S G ')' <> \u317A;"
+ "'(' S D ')' <> \u317C;"
+ "'(' S B ')' <> \u317D;"
+ "'(' PAN ')' <> \u317F;"
+ "'(' P '' ')' <> \u3184;"
+ "'(' N S ')' <> \u3167;"
+ "'(' N PAN ')' <> \u3168;"
+ "'(' N N ')' <> \u3165;"
+ "'(' N D ')' <> \u3166;"
+ "'(' M S ')' <> \u316F;"
+ "'(' M PAN ')' <> \u3170;"
+ "'(' M B ')' <> \u316E;"
+ "'(' M '' ')' <> \u3171;"
+ "'(' L YR ')' <> \u316D;"
+ "'(' L PAN ')' <> \u316C;"
+ "'(' L H ')' <> \u3140;"
+ "'(' L G S ')' <> \u3169;"
+ "'(' L D ')' <> \u316A;"
+ "'(' L B S ')' <> \u316B;"
+ "'(' HJF ')' <> \u3164;"
+ "'(' H H ')' <> \u3185;"
+ "'(' B T ')' <> \u3177;"
+ "'(' B S G ')' <> \u3174;"
+ "'(' B S D ')' <> \u3175;"
+ "'(' B S ')' <> \u3144;"
+ "'(' B J ')' <> \u3176;"
+ "'(' B G ')' <> \u3172;"
+ "'(' B D ')' <> \u3173;"
+ "'(' B B '' ')' <> \u3179;"
+ "'(' B '' ')' <> \u3178;"
+ "'(' AR I ')' <> \u318E;"
+ "'(' AR ')' <> \u318D;"
+ "'(' '' '' ')' <> \u3180;"

  // APOSTROPHE

  // As always, an apostrophe is used to separate digraphs into
  // singles. That is, if you really wanted [KAN][GGAN], instead
  // of [KANG][GAN] you would write "kan'ggan".

  // Rules for inserting ' when mapping separated digraphs back
  // from Hangul to Latin. Catch every letter that can be the
  // LAST of a digraph (or multigraph) AND first of an initial

  // special insertion for funny sequences of vowels, and for empty consonant

//  + "'' < l{ }\u11c0;"      // hangul jongseong thieuth
//  + "'' < $lsgb_{}\u11ba;" // hangul jongseong sios
//  + "'' < l{ }\u11c1;"      // hangul jongseong phieuph
//  + "'' < l{ }\u11b7;"      // hangul jongseong mieum
//  + "'' < n{ }\u11bd;"      // hangul jongseong cieuc
//  + "'' < $nl_{}\u11c2;"   // hangul jongseong hieuh
//  + "'' < $gnl_{}\u11a9;"  // hangul jongseong ssangkiyeok
//  + "'' < $bl_{}\u11b8;"   // hangul jongseong pieup
//  + "'' < d{ }\u11ae;"      // hangul jongseong tikeut
//  
//  + "'' < $ye_{}\u116e;"   // hangul jungseong u
//  + "'' < $ywe_{}\u1169;"  // hangul jungseong o
//  + "'' < $yw_{}\u1175;"   // hangul jungseong i
//  + "'' < $ywao_{}\u1166;" // hangul jungseong e
//  + "'' < $yw_{}\u1161;"   // hangul jungseong a
//  
//  + "'' < l{ }\u1110;"      // hangul choseong thieuth
//  + "'' < $lsgb_{}\u110a;" // hangul choseong ssangsios
//  + "'' < $lsgb_{}\u1109;" // hangul choseong sios
//  + "'' < l{ }\u1111;"      // hangul choseong phieuph
//  + "'' < l{ }\u1106;"      // hangul choseong mieum
//  + "'' < n{ }\u110c;"      // hangul choseong cieuc
//  + "'' < n{ }\u110d;"
//  + "'' < $nl_{}\u1112;"   // hangul choseong hieuh
//  + "'' < $gnl_{}\u1101;"  // hangul choseong ssangkiyeok
//  + "'' < $gnl_{}\u1100;"  // hangul choseong kiyeok
//  + "'' < d{ }\u1103;"      // hangul choseong tikeut
//  + "'' < d{ }\u1104;"
//  + "'' < $bl_{}\u1107;"   // hangul choseong pieup
//  + "'' < $bl_{}\u1108;"
 
// We transliterate the compound Jamo code points using ((x) for
// initials, (x) for medials, and (x)) for finals. - liu
+ " '((' n g ')' <> \u1113;"
+ " '((' n n ')' <> \u1114;"
+ " '((' n d ')' <> \u1115;"
+ " '((' n b ')' <> \u1116;"
+ " '((' d g ')' <> \u1117;"
+ " '((' l n ')' <> \u1118;"
+ " '((' l l ')' <> \u1119;"
+ " '((' l h ')' <> \u111A;"
+ " '((' l '' ')' <> \u111B;"
+ " '((' m b ')' <> \u111C;"
+ " '((' m '' ')' <> \u111D;"
+ " '((' b g ')' <> \u111E;"
+ " '((' b n ')' <> \u111F;"
+ " '((' b d ')' <> \u1120;"
+ " '((' b s ')' <> \u1121;"
+ " '((' b s g ')' <> \u1122;"
+ " '((' b s d ')' <> \u1123;"
+ " '((' b s b ')' <> \u1124;"
+ " '((' b s s ')' <> \u1125;"
+ " '((' b s j ')' <> \u1126;"
+ " '((' b j ')' <> \u1127;"
+ " '((' b c ')' <> \u1128;"
+ " '((' b t ')' <> \u1129;"
+ " '((' b p ')' <> \u112A;"
+ " '((' b '' ')' <> \u112B;"
+ " '((' b b '' ')' <> \u112C;"
+ " '((' s g ')' <> \u112D;"
+ " '((' s n ')' <> \u112E;"
+ " '((' s d ')' <> \u112F;"
+ " '((' s l ')' <> \u1130;"
+ " '((' s m ')' <> \u1131;"
+ " '((' s b ')' <> \u1132;"
+ " '((' s b g ')' <> \u1133;"
+ " '((' s s s ')' <> \u1134;"
+ " '((' s '' ')' <> \u1135;"
+ " '((' s j ')' <> \u1136;"
+ " '((' s c ')' <> \u1137;"
+ " '((' s k ')' <> \u1138;"
+ " '((' s t ')' <> \u1139;"
+ " '((' s p ')' <> \u113A;"
+ " '((' s h ')' <> \u113B;"
+ " '((' chs ')' <> \u113C;"
+ " '((' chs chs ')' <> \u113D;"
+ " '((' ces ')' <> \u113E;"
+ " '((' ces ces ')' <> \u113F;"
+ " '((' pan ')' <> \u1140;"
+ " '((' '' g ')' <> \u1141;"
+ " '((' '' d ')' <> \u1142;"
+ " '((' '' m ')' <> \u1143;"
+ " '((' '' b ')' <> \u1144;"
+ " '((' '' s ')' <> \u1145;"
+ " '((' '' pan ')' <> \u1146;"
+ " '((' '' '' ')' <> \u1147;"
+ " '((' '' j ')' <> \u1148;"
+ " '((' '' c ')' <> \u1149;"
+ " '((' '' t ')' <> \u114A;"
+ " '((' '' p ')' <> \u114B;"
+ " '((' yes ')' <> \u114C;"
+ " '((' j '' ')' <> \u114D;"
+ " '((' chc ')' <> \u114E;"
+ " '((' chc chc ')' <> \u114F;"
+ " '((' cec ')' <> \u1150;"
+ " '((' cec cec ')' <> \u1151;"
+ " '((' c k ')' <> \u1152;"
+ " '((' c h ')' <> \u1153;"
+ " '((' cch ')' <> \u1154;"
+ " '((' ceh ')' <> \u1155;"
+ " '((' p b ')' <> \u1156;"
+ " '((' p '' ')' <> \u1157;"
+ " '((' h h ')' <> \u1158;"
+ " '((' yr ')' <> \u1159;"
+ " '((' hcf ')' <> \u115F;"
+ " '(' ahjf ')' <> \u1160;" // must start with vowel, hence 'a' + hjf
+ " '(' a o ')' <> \u1176;"
+ " '(' a u ')' <> \u1177;"
+ " '(' ya o ')' <> \u1178;"
+ " '(' ya yo ')' <> \u1179;"
+ " '(' eo o ')' <> \u117A;"
+ " '(' eo u ')' <> \u117B;"
+ " '(' eo eu ')' <> \u117C;"
+ " '(' yeo o ')' <> \u117D;"
+ " '(' yeo u ')' <> \u117E;"
+ " '(' o eo ')' <> \u117F;"
+ " '(' o e ')' <> \u1180;"
+ " '(' o ye ')' <> \u1181;"
+ " '(' o o ')' <> \u1182;"
+ " '(' o u ')' <> \u1183;"
+ " '(' yo ya ')' <> \u1184;"
+ " '(' yo yae ')' <> \u1185;"
+ " '(' yo yeo ')' <> \u1186;"
+ " '(' yo o ')' <> \u1187;"
+ " '(' yo i ')' <> \u1188;"
+ " '(' u a ')' <> \u1189;"
+ " '(' u ae ')' <> \u118A;"
+ " '(' u eo eu ')' <> \u118B;"
+ " '(' u ye ')' <> \u118C;"
+ " '(' u u ')' <> \u118D;"
+ " '(' yu a ')' <> \u118E;"
+ " '(' yu eo ')' <> \u118F;"
+ " '(' yu e ')' <> \u1190;"
+ " '(' yu yeo ')' <> \u1191;"
+ " '(' yu ye ')' <> \u1192;"
+ " '(' yu u ')' <> \u1193;"
+ " '(' yu i ')' <> \u1194;"
+ " '(' eu u ')' <> \u1195;"
+ " '(' eu eu ')' <> \u1196;"
+ " '(' yi u ')' <> \u1197;"
+ " '(' i a ')' <> \u1198;"
+ " '(' i ya ')' <> \u1199;"
+ " '(' i o ')' <> \u119A;"
+ " '(' i u ')' <> \u119B;"
+ " '(' i eu ')' <> \u119C;"
+ " '(' i ar ')' <> \u119D;"
+ " '(' ar ')' <> \u119E;"
+ " '(' ar eo ')' <> \u119F;"
+ " '(' ar u ')' <> \u11A0;"
+ " '(' ar i ')' <> \u11A1;"
+ " '(' ar ar ')' <> \u11A2;"
+ " '(' g l '))' <> \u11C3;"
+ " '(' g s g '))' <> \u11C4;"
+ " '(' n g '))' <> \u11C5;"
+ " '(' n d '))' <> \u11C6;"
+ " '(' n s '))' <> \u11C7;"
+ " '(' n pan '))' <> \u11C8;"
+ " '(' n t '))' <> \u11C9;"
+ " '(' d g '))' <> \u11CA;"
+ " '(' d l '))' <> \u11CB;"
+ " '(' l g s '))' <> \u11CC;"
+ " '(' l n '))' <> \u11CD;"
+ " '(' l d '))' <> \u11CE;"
+ " '(' l d h '))' <> \u11CF;"
+ " '(' l l '))' <> \u11D0;"
+ " '(' l m g '))' <> \u11D1;"
+ " '(' l m s '))' <> \u11D2;"
+ " '(' l b s '))' <> \u11D3;"
+ " '(' l b h '))' <> \u11D4;"
+ " '(' l b ng '))' <> \u11D5;"
+ " '(' l s s '))' <> \u11D6;"
+ " '(' l pan '))' <> \u11D7;"
+ " '(' l k '))' <> \u11D8;"
+ " '(' l yr '))' <> \u11D9;"
+ " '(' m g '))' <> \u11DA;"
+ " '(' m l '))' <> \u11DB;"
+ " '(' m b '))' <> \u11DC;"
+ " '(' m s '))' <> \u11DD;"
+ " '(' m s s '))' <> \u11DE;"
+ " '(' m pan '))' <> \u11DF;"
+ " '(' m c '))' <> \u11E0;"
+ " '(' m h '))' <> \u11E1;"
+ " '(' m ng '))' <> \u11E2;"
+ " '(' b l '))' <> \u11E3;"
+ " '(' b p '))' <> \u11E4;"
+ " '(' b h '))' <> \u11E5;"
+ " '(' b ng '))' <> \u11E6;"
+ " '(' s g '))' <> \u11E7;"
+ " '(' s d '))' <> \u11E8;"
+ " '(' s l '))' <> \u11E9;"
+ " '(' s b '))' <> \u11EA;"
+ " '(' pan '))' <> \u11EB;"
+ " '(' ng g '))' <> \u11EC;"
+ " '(' ng g g '))' <> \u11ED;"
+ " '(' ng ng '))' <> \u11EE;"
+ " '(' ng k '))' <> \u11EF;"
+ " '(' yes '))' <> \u11F0;"
+ " '(' yes s '))' <> \u11F1;"
+ " '(' yes pan '))' <> \u11F2;"
+ " '(' p b '))' <> \u11F3;"
+ " '(' p ng '))' <> \u11F4;"
+ " '(' h n '))' <> \u11F5;"
+ " '(' h l '))' <> \u11F6;"
+ " '(' h m '))' <> \u11F7;"
+ " '(' h b '))' <> \u11F8;"
+ " '(' yr '))' <> \u11F9;"


  // INITIALS

              // Added }$vowel post context - liu
  + "bb}$vowel<>\u1108 } $vowel;"
  + "jj}$vowel<>\u110d } $vowel;"
  + "dd}$vowel<>\u1104 } $vowel;"
  + "t }$vowel<>\u1110 } $vowel;"  // hangul choseong thieuth
  + "ss}$vowel<>\u110a } $vowel;"  // hangul choseong ssangsios
  + "s }$vowel<>\u1109 } $vowel;"  // hangul choseong sios
  + "p }$vowel<>\u1111 } $vowel;"  // hangul choseong phieuph
  + "n }$vowel<>\u1102 } $vowel;"  // hangul choseong nieun
  + "m }$vowel<>\u1106 } $vowel;"  // hangul choseong mieum
  + "l }$vowel<>\u1105 } $vowel;"  // hangul choseong rieul
  + "k }$vowel<>\u110f } $vowel;"  // hangul choseong khieukh
  + "j }$vowel<>\u110c } $vowel;"  // hangul choseong cieuc
  + "h }$vowel<>\u1112 } $vowel;"  // hangul choseong hieuh
  + "gg}$vowel<>\u1101 } $vowel;"  // hangul choseong ssangkiyeok
  + "g }$vowel<>\u1100 } $vowel;"  // hangul choseong kiyeok
  + "d }$vowel<>\u1103 } $vowel;"  // hangul choseong tikeut
  + "c }$vowel<>\u110e } $vowel;"  // hangul choseong chieuch
  + "b }$vowel<>\u1107 } $vowel;"  // hangul choseong pieup  

              // Take care of initial-compound medial - '(' $vowel - liu
  + "bb} '(' $vowel <> \u1108 } $comp_med;"
  + "jj} '(' $vowel <> \u110d } $comp_med;"
  + "dd} '(' $vowel <> \u1104 } $comp_med;"
  + "t } '(' $vowel <> \u1110 } $comp_med;"  // hangul choseong thieuth
  + "ss} '(' $vowel <> \u110a } $comp_med;"  // hangul choseong ssangsios
  + "s } '(' $vowel <> \u1109 } $comp_med;"  // hangul choseong sios
  + "p } '(' $vowel <> \u1111 } $comp_med;"  // hangul choseong phieuph
  + "n } '(' $vowel <> \u1102 } $comp_med;"  // hangul choseong nieun
  + "m } '(' $vowel <> \u1106 } $comp_med;"  // hangul choseong mieum
  + "l } '(' $vowel <> \u1105 } $comp_med;"  // hangul choseong rieul
  + "k } '(' $vowel <> \u110f } $comp_med;"  // hangul choseong khieukh
  + "j } '(' $vowel <> \u110c } $comp_med;"  // hangul choseong cieuc
  + "h } '(' $vowel <> \u1112 } $comp_med;"  // hangul choseong hieuh
  + "gg} '(' $vowel <> \u1101 } $comp_med;"  // hangul choseong ssangkiyeok
  + "g } '(' $vowel <> \u1100 } $comp_med;"  // hangul choseong kiyeok
  + "d } '(' $vowel <> \u1103 } $comp_med;"  // hangul choseong tikeut
  + "c } '(' $vowel <> \u110e } $comp_med;"  // hangul choseong chieuch
  + "b } '(' $vowel <> \u1107 } $comp_med;"  // hangul choseong pieup  
  
              // Mark non-canonical initials with '[' - liu
  + "'[' bb <> \u1108;"
  + "'[' jj <> \u110d;"
  + "'[' dd <> \u1104;"
  + "'[' t  <> \u1110;"  // hangul choseong thieuth
  + "'[' ss <> \u110a;"  // hangul choseong ssangsios
  + "'[' s  <> \u1109;"  // hangul choseong sios
  + "'[' p  <> \u1111;"  // hangul choseong phieuph
  + "'[' n  <> \u1102;"  // hangul choseong nieun
  + "'[' m  <> \u1106;"  // hangul choseong mieum
  + "'[' l  <> \u1105;"  // hangul choseong rieul
  + "'[' k  <> \u110f;"  // hangul choseong khieukh
  + "'[' j  <> \u110c;"  // hangul choseong cieuc
  + "'[' h  <> \u1112;"  // hangul choseong hieuh
  + "'[' gg <> \u1101;"  // hangul choseong ssangkiyeok
  + "'[' g  <> \u1100;"  // hangul choseong kiyeok
  + "'[' d  <> \u1103;"  // hangul choseong tikeut
  + "'[' c  <> \u110e;"  // hangul choseong chieuch
  + "'[' b  <> \u1107;"  // hangul choseong pieup  


  // If we have gotten through to these rules, and we start with
  // a consonant, then the remaining mappings would be to F,
  // because must have CC (or C<non-letter>), not CV.
  // If we have F before us, then
  // we would end up with FF, which is wrong. The simplest fix is
  // to still make it an initial, but also insert an "u",
  // so we end up with F, I, u, and then continue with the C

  // special, only initial
//  + "bb > \u1108\u116e;"  // bb u hangul choseong ssangpieup
//  + "jj > \u110d\u116e;"  // jj u hangul choseong ssangcieuc
//  + "dd > \u1104\u116e;"  // dd u hangul choseong ssangtikeut
  
//  + "$final{ t > \u1110\u116e;"   // hangul choseong thieuth
//  + "$final{ ss> \u110a\u116e;"   // hangul choseong ssangsios
//  + "$final{ s > \u1109\u116e;"   // hangul choseong sios
//  + "$final{ p > \u1111\u116e;"   // hangul choseong phieuph
//  + "$final{ n > \u1102\u116e;"   // hangul choseong nieun
//  + "$final{ m > \u1106\u116e;"   // hangul choseong mieum
//  + "$final{ l > \u1105\u116e;"   // hangul choseong rieul
//  + "$final{ k > \u110f\u116e;"   // hangul choseong khieukh
//  + "$final{ j > \u110c\u116e;"   // hangul choseong cieuc
//  + "$final{ h > \u1112\u116e;"   // hangul choseong hieuh
//  + "$final{ gg> \u1101\u116e;"   // hangul choseong ssangkiyeok
//  + "$final{ g > \u1100\u116e;"   // hangul choseong kiyeok
//  + "$final{ d > \u1103\u116e;"   // hangul choseong tikeut
//  + "$final{ c > \u110e\u116e;"   // hangul choseong chieuch
//  + "$final{ b > \u1107\u116e;"   // hangul choseong pieup
  
  // MEDIALS after INITIALS
  
  // MEDIALS (vowels) not after INITIALs
              // Added left $initial context - liu
  + "$initial{ yu <> $INITIAL{ \u1172;"   // hangul jungseong yu
  + "$initial{ yo <> $INITIAL{ \u116d;"   // hangul jungseong yo
  + "$initial{ yi <> $INITIAL{ \u1174;"   // hangul jungseong yi
  + "$initial{ yeo<> $INITIAL{ \u1167;"   // hangul jungseong yeo
  + "$initial{ ye <> $INITIAL{ \u1168;"   // hangul jungseong ye
  + "$initial{ yae<> $INITIAL{ \u1164;"   // hangul jungseong yae
  + "$initial{ ya <> $INITIAL{ \u1163;"   // hangul jungseong ya
  + "$initial{ wi <> $INITIAL{ \u1171;"   // hangul jungseong wi
  + "$initial{ weo<> $INITIAL{ \u116f;"   // hangul jungseong weo
  + "$initial{ we <> $INITIAL{ \u1170;"   // hangul jungseong we
  + "$initial{ wae<> $INITIAL{ \u116b;"   // hangul jungseong wae
  + "$initial{ wa <> $INITIAL{ \u116a;"   // hangul jungseong wa
  + "$initial{ u  <> $INITIAL{ \u116e;"   // hangul jungseong u
  + "$initial{ oe <> $INITIAL{ \u116c;"   // hangul jungseong oe
  + "$initial{ o  <> $INITIAL{ \u1169;"   // hangul jungseong o
  + "$initial{ i  <> $INITIAL{ \u1175;"   // hangul jungseong i
  + "$initial{ eu <> $INITIAL{ \u1173;"   // hangul jungseong eu
  + "$initial{ eo <> $INITIAL{ \u1165;"   // hangul jungseong eo
  + "$initial{ e  <> $INITIAL{ \u1166;"   // hangul jungseong e
  + "$initial{ ae <> $INITIAL{ \u1162;"   // hangul jungseong ae
  + "$initial{ a  <> $INITIAL{ \u1161;"   // hangul jungseong a
  
              // Handle non-canonical isolated jungseong - liu  
  + "'~'yu <> \u1172;"  // hangul jungseong yu
  + "'~'yo <> \u116d;"  // hangul jungseong yo
  + "'~'yi <> \u1174;"  // hangul jungseong yi
  + "'~'yeo<> \u1167;"  // hangul jungseong yeo
  + "'~'ye <> \u1168;"  // hangul jungseong ye
  + "'~'yae<> \u1164;"  // hangul jungseong yae
  + "'~'ya <> \u1163;"  // hangul jungseong ya
  + "'~'wi <> \u1171;"  // hangul jungseong wi
  + "'~'weo<> \u116f;"  // hangul jungseong weo
  + "'~'we <> \u1170;"  // hangul jungseong we
  + "'~'wae<> \u116b;"  // hangul jungseong wae
  + "'~'wa <> \u116a;"  // hangul jungseong wa
  + "'~'u  <> \u116e;"  // hangul jungseong u
  + "'~'oe <> \u116c;"  // hangul jungseong oe
  + "'~'o  <> \u1169;"  // hangul jungseong o
  + "'~'i  <> \u1175;"  // hangul jungseong i
  + "'~'eu <> \u1173;"  // hangul jungseong eu
  + "'~'eo <> \u1165;"  // hangul jungseong eo
  + "'~'e  <> \u1166;"  // hangul jungseong e
  + "'~'ae <> \u1162;"  // hangul jungseong ae
  + "'~'a  <> \u1161;"  // hangul jungseong a

  // MEDIALS (vowels) not after INITIALs
              // Changed from > to <> - liu
  + "yu <> $ieung \u1172;"   // hangul jungseong yu
  + "yo <> $ieung \u116d;"   // hangul jungseong yo
  + "yi <> $ieung \u1174;"   // hangul jungseong yi
  + "yeo<> $ieung \u1167;"   // hangul jungseong yeo
  + "ye <> $ieung \u1168;"   // hangul jungseong ye
  + "yae<> $ieung \u1164;"   // hangul jungseong yae
  + "ya <> $ieung \u1163;"   // hangul jungseong ya
  + "wi <> $ieung \u1171;"   // hangul jungseong wi
  + "weo<> $ieung \u116f;"   // hangul jungseong weo
  + "we <> $ieung \u1170;"   // hangul jungseong we
  + "wae<> $ieung \u116b;"   // hangul jungseong wae
  + "wa <> $ieung \u116a;"   // hangul jungseong wa
  + "u  <> $ieung \u116e;"   // hangul jungseong u
  + "oe <> $ieung \u116c;"   // hangul jungseong oe
  + "o  <> $ieung \u1169;"   // hangul jungseong o
  + "i  <> $ieung \u1175;"   // hangul jungseong i
  + "eu <> $ieung \u1173;"   // hangul jungseong eu
  + "eo <> $ieung \u1165;"   // hangul jungseong eo
  + "e  <> $ieung \u1166;"   // hangul jungseong e
  + "ae <> $ieung \u1162;"   // hangul jungseong ae
  + "a  <> $ieung \u1161;"   // hangul jungseong a

+"\\` <> $ieung;"
  // Moved down so as not to mask above rules - liu  
  // + "'' < $consonant{$ieung;" // insert a break between any consonant and the empty consonant.
  //  + "$medial{}$vowel<>$ieung;"  // HANGUL CHOSEONG IEUNG


  // FINALS
  
  + " '' t  <> $consonant { \u11c0;"   // hangul jongseong thieuth
  + " '' ss <> $consonant { \u11bb;"   // hangul jongseong ssangsios
  + " '' s  <> $consonant { \u11ba;"   // hangul jongseong sios
  + " '' p  <> $consonant { \u11c1;"   // hangul jongseong phieuph
  + " '' nj <> $consonant { \u11ac;"   // hangul jongseong nieun-cieuc
  + " '' nh <> $consonant { \u11ad;"   // hangul jongseong nieun-hieuh
  + " '' ng <> $consonant { \u11bc;"   // hangul jongseong ieung
  + " '' n  <> $consonant { \u11ab;"   // hangul jongseong nieun
  + " '' m  <> $consonant { \u11b7;"   // hangul jongseong mieum
  + " '' lt <> $consonant { \u11b4;"   // hangul jongseong rieul-thieuth
  + " '' ls <> $consonant { \u11b3;"   // hangul jongseong rieul-sios
  + " '' lp <> $consonant { \u11b5;"   // hangul jongseong rieul-phieuph
  + " '' lm <> $consonant { \u11b1;"   // hangul jongseong rieul-mieum
  + " '' lh <> $consonant { \u11b6;"   // hangul jongseong rieul-hieuh
  + " '' lg <> $consonant { \u11b0;"   // hangul jongseong rieul-kiyeok
  + " '' lb <> $consonant { \u11b2;"   // hangul jongseong rieul-pieup
  + " '' l  <> $consonant { \u11af;"   // hangul jongseong rieul
  + " '' k  <> $consonant { \u11bf;"   // hangul jongseong khieukh
  + " '' j  <> $consonant { \u11bd;"   // hangul jongseong cieuc
  + " '' h  <> $consonant { \u11c2;"   // hangul jongseong hieuh
  + " '' gs <> $consonant { \u11aa;"   // hangul jongseong kiyeok-sios
  + " '' gg <> $consonant { \u11a9;"   // hangul jongseong ssangkiyeok
  + " '' g  <> $consonant { \u11a8;"   // hangul jongseong kiyeok
  + " '' d  <> $consonant { \u11ae;"   // hangul jongseong tikeut
  + " '' c  <> $consonant { \u11be;"   // hangul jongseong chieuch
  + " '' bs <> $consonant { \u11b9;"   // hangul jongseong pieup-sios
  + " '' b  <> $consonant { \u11b8;"   // hangul jongseong pieup

  + "t  ']'> \u11c0;"   // hangul jongseong thieuth
  + "ss ']'> \u11bb;"   // hangul jongseong ssangsios
  + "s  ']'> \u11ba;"   // hangul jongseong sios
  + "p  ']'> \u11c1;"   // hangul jongseong phieuph
  + "nj ']'> \u11ac;"   // hangul jongseong nieun-cieuc
  + "nh ']'> \u11ad;"   // hangul jongseong nieun-hieuh
  + "ng ']'> \u11bc;"   // hangul jongseong ieung
  + "n  ']'> \u11ab;"   // hangul jongseong nieun
  + "m  ']'> \u11b7;"   // hangul jongseong mieum
  + "lt ']'> \u11b4;"   // hangul jongseong rieul-thieuth
  + "ls ']'> \u11b3;"   // hangul jongseong rieul-sios
  + "lp ']'> \u11b5;"   // hangul jongseong rieul-phieuph
  + "lm ']'> \u11b1;"   // hangul jongseong rieul-mieum
  + "lh ']'> \u11b6;"   // hangul jongseong rieul-hieuh
  + "lg ']'> \u11b0;"   // hangul jongseong rieul-kiyeok
  + "lb ']'> \u11b2;"   // hangul jongseong rieul-pieup
  + "l  ']'> \u11af;"   // hangul jongseong rieul
  + "k  ']'> \u11bf;"   // hangul jongseong khieukh
  + "j  ']'> \u11bd;"   // hangul jongseong cieuc
  + "h  ']'> \u11c2;"   // hangul jongseong hieuh
  + "gs ']'> \u11aa;"   // hangul jongseong kiyeok-sios
  + "gg ']'> \u11a9;"   // hangul jongseong ssangkiyeok
  + "g  ']'> \u11a8;"   // hangul jongseong kiyeok
  + "d  ']'> \u11ae;"   // hangul jongseong tikeut
  + "c  ']'> \u11be;"   // hangul jongseong chieuch
  + "bs ']'> \u11b9;"   // hangul jongseong pieup-sios
  + "b  ']'> \u11b8;"   // hangul jongseong pieup

  + "$medial{ t  <> $MEDIAL{ \u11c0;"   // hangul jongseong thieuth
  + "$medial{ ss <> $MEDIAL{ \u11bb;"   // hangul jongseong ssangsios
  + "$medial{ s  <> $MEDIAL{ \u11ba;"   // hangul jongseong sios
  + "$medial{ p  <> $MEDIAL{ \u11c1;"   // hangul jongseong phieuph
  + "$medial{ nj <> $MEDIAL{ \u11ac;"   // hangul jongseong nieun-cieuc
  + "$medial{ nh <> $MEDIAL{ \u11ad;"   // hangul jongseong nieun-hieuh
  + "$medial{ ng <> $MEDIAL{ \u11bc;"   // hangul jongseong ieung
  + "$medial{ n  <> $MEDIAL{ \u11ab;"   // hangul jongseong nieun
  + "$medial{ m  <> $MEDIAL{ \u11b7;"   // hangul jongseong mieum
  + "$medial{ lt <> $MEDIAL{ \u11b4;"   // hangul jongseong rieul-thieuth
  + "$medial{ ls <> $MEDIAL{ \u11b3;"   // hangul jongseong rieul-sios
  + "$medial{ lp <> $MEDIAL{ \u11b5;"   // hangul jongseong rieul-phieuph
  + "$medial{ lm <> $MEDIAL{ \u11b1;"   // hangul jongseong rieul-mieum
  + "$medial{ lh <> $MEDIAL{ \u11b6;"   // hangul jongseong rieul-hieuh
  + "$medial{ lg <> $MEDIAL{ \u11b0;"   // hangul jongseong rieul-kiyeok
  + "$medial{ lb <> $MEDIAL{ \u11b2;"   // hangul jongseong rieul-pieup
  + "$medial{ l  <> $MEDIAL{ \u11af;"   // hangul jongseong rieul
  + "$medial{ k  <> $MEDIAL{ \u11bf;"   // hangul jongseong khieukh
  + "$medial{ j  <> $MEDIAL{ \u11bd;"   // hangul jongseong cieuc
  + "$medial{ h  <> $MEDIAL{ \u11c2;"   // hangul jongseong hieuh
  + "$medial{ gs <> $MEDIAL{ \u11aa;"   // hangul jongseong kiyeok-sios
  + "$medial{ gg <> $MEDIAL{ \u11a9;"   // hangul jongseong ssangkiyeok
  + "$medial{ g  <> $MEDIAL{ \u11a8;"   // hangul jongseong kiyeok
  + "$medial{ d  <> $MEDIAL{ \u11ae;"   // hangul jongseong tikeut
  + "$medial{ c  <> $MEDIAL{ \u11be;"   // hangul jongseong chieuch
  + "$medial{ bs <> $MEDIAL{ \u11b9;"   // hangul jongseong pieup-sios
  + "$medial{ b  <> $MEDIAL{ \u11b8;"   // hangul jongseong pieup

  + "t  ']'< \u11c0;"   // hangul jongseong thieuth
  + "ss ']'< \u11bb;"   // hangul jongseong ssangsios
  + "s  ']'< \u11ba;"   // hangul jongseong sios
  + "p  ']'< \u11c1;"   // hangul jongseong phieuph
  + "nj ']'< \u11ac;"   // hangul jongseong nieun-cieuc
  + "nh ']'< \u11ad;"   // hangul jongseong nieun-hieuh
  + "ng ']'< \u11bc;"   // hangul jongseong ieung
  + "n  ']'< \u11ab;"   // hangul jongseong nieun
  + "m  ']'< \u11b7;"   // hangul jongseong mieum
  + "lt ']'< \u11b4;"   // hangul jongseong rieul-thieuth
  + "ls ']'< \u11b3;"   // hangul jongseong rieul-sios
  + "lp ']'< \u11b5;"   // hangul jongseong rieul-phieuph
  + "lm ']'< \u11b1;"   // hangul jongseong rieul-mieum
  + "lh ']'< \u11b6;"   // hangul jongseong rieul-hieuh
  + "lg ']'< \u11b0;"   // hangul jongseong rieul-kiyeok
  + "lb ']'< \u11b2;"   // hangul jongseong rieul-pieup
  + "l  ']'< \u11af;"   // hangul jongseong rieul
  + "k  ']'< \u11bf;"   // hangul jongseong khieukh
  + "j  ']'< \u11bd;"   // hangul jongseong cieuc
  + "h  ']'< \u11c2;"   // hangul jongseong hieuh
  + "gs ']'< \u11aa;"   // hangul jongseong kiyeok-sios
  + "gg ']'< \u11a9;"   // hangul jongseong ssangkiyeok
  + "g  ']'< \u11a8;"   // hangul jongseong kiyeok
  + "d  ']'< \u11ae;"   // hangul jongseong tikeut
  + "c  ']'< \u11be;"   // hangul jongseong chieuch
  + "bs ']'< \u11b9;"   // hangul jongseong pieup-sios
  + "b  ']'< \u11b8;"   // hangul jongseong pieup

  // extra English letters

//  + "z > |s;"
//  //{ + "Z > |s;" } masked
//  + "x > |ks;"
//  + "X > |ks;"
//  + "v > |b;"
//  + "V > |b;"
//  + "r > |l;"
//  + "R > |l;"
//  + "q > |k;"
//  + "Q > |k;"
//  + "f > |p;"
//  + "F > |p;"
//  //{ + "c > |k;" } masked
//  + "C > |k;"
  
//  + "y > \u1172;"   // hangul jungseong yu
//  + "w > \u1171;"   // hangul jungseong wi
            }
        };
    }
}
