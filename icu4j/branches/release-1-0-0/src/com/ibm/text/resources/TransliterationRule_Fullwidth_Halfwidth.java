/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Fullwidth_Halfwidth.java,v $ 
 * $Date: 2000/03/10 04:07:29 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Fullwidth_Halfwidth extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule", ""
            
            /* Mechanically generated from Unicode Character Database
             */

    // multicharacter

    + "\u30AC<>\uFF76\uFF9E;"   //  to KATAKANA LETTER GA
    + "\u30AE<>\uFF77\uFF9E;"   //  to KATAKANA LETTER GI
    + "\u30B0<>\uFF78\uFF9E;"   //  to KATAKANA LETTER GU
    + "\u30B2<>\uFF79\uFF9E;"   //  to KATAKANA LETTER GE
    + "\u30B4<>\uFF7A\uFF9E;"   //  to KATAKANA LETTER GO
    + "\u30B6<>\uFF7B\uFF9E;"   //  to KATAKANA LETTER ZA
    + "\u30B8<>\uFF7C\uFF9E;"   //  to KATAKANA LETTER ZI
    + "\u30BA<>\uFF7D\uFF9E;"   //  to KATAKANA LETTER ZU
    + "\u30BC<>\uFF7E\uFF9E;"   //  to KATAKANA LETTER ZE
    + "\u30BE<>\uFF7F\uFF9E;"   //  to KATAKANA LETTER ZO
    + "\u30C0<>\uFF80\uFF9E;"   //  to KATAKANA LETTER DA
    + "\u30C2<>\uFF81\uFF9E;"   //  to KATAKANA LETTER DI
    + "\u30C5<>\uFF82\uFF9E;"   //  to KATAKANA LETTER DU
    + "\u30C7<>\uFF83\uFF9E;"   //  to KATAKANA LETTER DE
    + "\u30C9<>\uFF84\uFF9E;"   //  to KATAKANA LETTER DO
    + "\u30D0<>\uFF8A\uFF9E;"   //  to KATAKANA LETTER BA
    + "\u30D1<>\uFF8A\uFF9F;"   //  to KATAKANA LETTER PA
    + "\u30D3<>\uFF8B\uFF9E;"   //  to KATAKANA LETTER BI
    + "\u30D4<>\uFF8B\uFF9F;"   //  to KATAKANA LETTER PI
    + "\u30D6<>\uFF8C\uFF9E;"   //  to KATAKANA LETTER BU
    + "\u30D7<>\uFF8C\uFF9F;"   //  to KATAKANA LETTER PU
    + "\u30D9<>\uFF8D\uFF9E;"   //  to KATAKANA LETTER BE
    + "\u30DA<>\uFF8D\uFF9F;"   //  to KATAKANA LETTER PE
    + "\u30DC<>\uFF8E\uFF9E;"   //  to KATAKANA LETTER BO
    + "\u30DD<>\uFF8E\uFF9F;"   //  to KATAKANA LETTER PO
    + "\u30F4<>\uFF73\uFF9E;"   //  to KATAKANA LETTER VU
    + "\u30F7<>\uFF9C\uFF9E;"   //  to KATAKANA LETTER VA
    + "\u30FA<>\uFF66\uFF9E;"   //  to KATAKANA LETTER VO
    
    // single character

    + "\uFF01<>'!';"    //  from FULLWIDTH EXCLAMATION MARK
    + "\uFF02<>'\"';"   //  from FULLWIDTH QUOTATION MARK
    + "\uFF03<>'#';"    //  from FULLWIDTH NUMBER SIGN
    + "\uFF04<>'$';"    //  from FULLWIDTH DOLLAR SIGN
    + "\uFF05<>'%';"    //  from FULLWIDTH PERCENT SIGN
    + "\uFF06<>'&';"    //  from FULLWIDTH AMPERSAND
    + "\uFF07<>'';" //  from FULLWIDTH APOSTROPHE
    + "\uFF08<>'(';"    //  from FULLWIDTH LEFT PARENTHESIS
    + "\uFF09<>')';"    //  from FULLWIDTH RIGHT PARENTHESIS
    + "\uFF0A<>'*';"    //  from FULLWIDTH ASTERISK
    + "\uFF0B<>'+';"    //  from FULLWIDTH PLUS SIGN
    + "\uFF0C<>',';"    //  from FULLWIDTH COMMA
    + "\uFF0D<>'-';"    //  from FULLWIDTH HYPHEN-MINUS
    + "\uFF0E<>'.';"    //  from FULLWIDTH FULL STOP
    + "\uFF0F<>'/';"    //  from FULLWIDTH SOLIDUS
    + "\uFF10<>'0';"    //  from FULLWIDTH DIGIT ZERO
    + "\uFF11<>'1';"    //  from FULLWIDTH DIGIT ONE
    + "\uFF12<>'2';"    //  from FULLWIDTH DIGIT TWO
    + "\uFF13<>'3';"    //  from FULLWIDTH DIGIT THREE
    + "\uFF14<>'4';"    //  from FULLWIDTH DIGIT FOUR
    + "\uFF15<>'5';"    //  from FULLWIDTH DIGIT FIVE
    + "\uFF16<>'6';"    //  from FULLWIDTH DIGIT SIX
    + "\uFF17<>'7';"    //  from FULLWIDTH DIGIT SEVEN
    + "\uFF18<>'8';"    //  from FULLWIDTH DIGIT EIGHT
    + "\uFF19<>'9';"    //  from FULLWIDTH DIGIT NINE
    + "\uFF1A<>':';"    //  from FULLWIDTH COLON
    + "\uFF1B<>';';"    //  from FULLWIDTH SEMICOLON
    + "\uFF1C<>'<';"    //  from FULLWIDTH LESS-THAN SIGN
    + "\uFF1D<>'=';"    //  from FULLWIDTH EQUALS SIGN
    + "\uFF1E<>'>';"    //  from FULLWIDTH GREATER-THAN SIGN
    + "\uFF1F<>'?';"    //  from FULLWIDTH QUESTION MARK
    + "\uFF20<>'@';"    //  from FULLWIDTH COMMERCIAL AT
    + "\uFF21<>A;"  //  from FULLWIDTH LATIN CAPITAL LETTER A
    + "\uFF22<>B;"  //  from FULLWIDTH LATIN CAPITAL LETTER B
    + "\uFF23<>C;"  //  from FULLWIDTH LATIN CAPITAL LETTER C
    + "\uFF24<>D;"  //  from FULLWIDTH LATIN CAPITAL LETTER D
    + "\uFF25<>E;"  //  from FULLWIDTH LATIN CAPITAL LETTER E
    + "\uFF26<>F;"  //  from FULLWIDTH LATIN CAPITAL LETTER F
    + "\uFF27<>G;"  //  from FULLWIDTH LATIN CAPITAL LETTER G
    + "\uFF28<>H;"  //  from FULLWIDTH LATIN CAPITAL LETTER H
    + "\uFF29<>I;"  //  from FULLWIDTH LATIN CAPITAL LETTER I
    + "\uFF2A<>J;"  //  from FULLWIDTH LATIN CAPITAL LETTER J
    + "\uFF2B<>K;"  //  from FULLWIDTH LATIN CAPITAL LETTER K
    + "\uFF2C<>L;"  //  from FULLWIDTH LATIN CAPITAL LETTER L
    + "\uFF2D<>M;"  //  from FULLWIDTH LATIN CAPITAL LETTER M
    + "\uFF2E<>N;"  //  from FULLWIDTH LATIN CAPITAL LETTER N
    + "\uFF2F<>O;"  //  from FULLWIDTH LATIN CAPITAL LETTER O
    + "\uFF30<>P;"  //  from FULLWIDTH LATIN CAPITAL LETTER P
    + "\uFF31<>Q;"  //  from FULLWIDTH LATIN CAPITAL LETTER Q
    + "\uFF32<>R;"  //  from FULLWIDTH LATIN CAPITAL LETTER R
    + "\uFF33<>S;"  //  from FULLWIDTH LATIN CAPITAL LETTER S
    + "\uFF34<>T;"  //  from FULLWIDTH LATIN CAPITAL LETTER T
    + "\uFF35<>U;"  //  from FULLWIDTH LATIN CAPITAL LETTER U
    + "\uFF36<>V;"  //  from FULLWIDTH LATIN CAPITAL LETTER V
    + "\uFF37<>W;"  //  from FULLWIDTH LATIN CAPITAL LETTER W
    + "\uFF38<>X;"  //  from FULLWIDTH LATIN CAPITAL LETTER X
    + "\uFF39<>Y;"  //  from FULLWIDTH LATIN CAPITAL LETTER Y
    + "\uFF3A<>Z;"  //  from FULLWIDTH LATIN CAPITAL LETTER Z
    + "\uFF3B<>'[';"    //  from FULLWIDTH LEFT SQUARE BRACKET
    + "\uFF3C<>'\\';"    //  from FULLWIDTH REVERSE SOLIDUS {double escape - aliu}
    + "\uFF3D<>']';"    //  from FULLWIDTH RIGHT SQUARE BRACKET
    + "\uFF3E<>'^';"    //  from FULLWIDTH CIRCUMFLEX ACCENT
    + "\uFF3F<>'_';"    //  from FULLWIDTH LOW LINE
    + "\uFF40<>'`';"    //  from FULLWIDTH GRAVE ACCENT
    + "\uFF41<>a;"  //  from FULLWIDTH LATIN SMALL LETTER A
    + "\uFF42<>b;"  //  from FULLWIDTH LATIN SMALL LETTER B
    + "\uFF43<>c;"  //  from FULLWIDTH LATIN SMALL LETTER C
    + "\uFF44<>d;"  //  from FULLWIDTH LATIN SMALL LETTER D
    + "\uFF45<>e;"  //  from FULLWIDTH LATIN SMALL LETTER E
    + "\uFF46<>f;"  //  from FULLWIDTH LATIN SMALL LETTER F
    + "\uFF47<>g;"  //  from FULLWIDTH LATIN SMALL LETTER G
    + "\uFF48<>h;"  //  from FULLWIDTH LATIN SMALL LETTER H
    + "\uFF49<>i;"  //  from FULLWIDTH LATIN SMALL LETTER I
    + "\uFF4A<>j;"  //  from FULLWIDTH LATIN SMALL LETTER J
    + "\uFF4B<>k;"  //  from FULLWIDTH LATIN SMALL LETTER K
    + "\uFF4C<>l;"  //  from FULLWIDTH LATIN SMALL LETTER L
    + "\uFF4D<>m;"  //  from FULLWIDTH LATIN SMALL LETTER M
    + "\uFF4E<>n;"  //  from FULLWIDTH LATIN SMALL LETTER N
    + "\uFF4F<>o;"  //  from FULLWIDTH LATIN SMALL LETTER O
    + "\uFF50<>p;"  //  from FULLWIDTH LATIN SMALL LETTER P
    + "\uFF51<>q;"  //  from FULLWIDTH LATIN SMALL LETTER Q
    + "\uFF52<>r;"  //  from FULLWIDTH LATIN SMALL LETTER R
    + "\uFF53<>s;"  //  from FULLWIDTH LATIN SMALL LETTER S
    + "\uFF54<>t;"  //  from FULLWIDTH LATIN SMALL LETTER T
    + "\uFF55<>u;"  //  from FULLWIDTH LATIN SMALL LETTER U
    + "\uFF56<>v;"  //  from FULLWIDTH LATIN SMALL LETTER V
    + "\uFF57<>w;"  //  from FULLWIDTH LATIN SMALL LETTER W
    + "\uFF58<>x;"  //  from FULLWIDTH LATIN SMALL LETTER X
    + "\uFF59<>y;"  //  from FULLWIDTH LATIN SMALL LETTER Y
    + "\uFF5A<>z;"  //  from FULLWIDTH LATIN SMALL LETTER Z
    + "\uFF5B<>'{';"    //  from FULLWIDTH LEFT CURLY BRACKET
    + "\uFF5C<>'|';"    //  from FULLWIDTH VERTICAL LINE
    + "\uFF5D<>'}';"    //  from FULLWIDTH RIGHT CURLY BRACKET
    + "\uFF5E<>'~';"    //  from FULLWIDTH TILDE
    + "\u3002<>\uFF61;" //  to HALFWIDTH IDEOGRAPHIC FULL STOP
    + "\u300C<>\uFF62;" //  to HALFWIDTH LEFT CORNER BRACKET
    + "\u300D<>\uFF63;" //  to HALFWIDTH RIGHT CORNER BRACKET
    + "\u3001<>\uFF64;" //  to HALFWIDTH IDEOGRAPHIC COMMA
    + "\u30FB<>\uFF65;" //  to HALFWIDTH KATAKANA MIDDLE DOT
    + "\u30F2<>\uFF66;" //  to HALFWIDTH KATAKANA LETTER WO
    + "\u30A1<>\uFF67;" //  to HALFWIDTH KATAKANA LETTER SMALL A
    + "\u30A3<>\uFF68;" //  to HALFWIDTH KATAKANA LETTER SMALL I
    + "\u30A5<>\uFF69;" //  to HALFWIDTH KATAKANA LETTER SMALL U
    + "\u30A7<>\uFF6A;" //  to HALFWIDTH KATAKANA LETTER SMALL E
    + "\u30A9<>\uFF6B;" //  to HALFWIDTH KATAKANA LETTER SMALL O
    + "\u30E3<>\uFF6C;" //  to HALFWIDTH KATAKANA LETTER SMALL YA
    + "\u30E5<>\uFF6D;" //  to HALFWIDTH KATAKANA LETTER SMALL YU
    + "\u30E7<>\uFF6E;" //  to HALFWIDTH KATAKANA LETTER SMALL YO
    + "\u30C3<>\uFF6F;" //  to HALFWIDTH KATAKANA LETTER SMALL TU
    + "\u30FC<>\uFF70;" //  to HALFWIDTH KATAKANA-HIRAGANA PROLONGED SOUND MARK
    + "\u30A2<>\uFF71;" //  to HALFWIDTH KATAKANA LETTER A
    + "\u30A4<>\uFF72;" //  to HALFWIDTH KATAKANA LETTER I
    + "\u30A6<>\uFF73;" //  to HALFWIDTH KATAKANA LETTER U
    + "\u30A8<>\uFF74;" //  to HALFWIDTH KATAKANA LETTER E
    + "\u30AA<>\uFF75;" //  to HALFWIDTH KATAKANA LETTER O
    + "\u30AB<>\uFF76;" //  to HALFWIDTH KATAKANA LETTER KA
    + "\u30AD<>\uFF77;" //  to HALFWIDTH KATAKANA LETTER KI
    + "\u30AF<>\uFF78;" //  to HALFWIDTH KATAKANA LETTER KU
    + "\u30B1<>\uFF79;" //  to HALFWIDTH KATAKANA LETTER KE
    + "\u30B3<>\uFF7A;" //  to HALFWIDTH KATAKANA LETTER KO
    + "\u30B5<>\uFF7B;" //  to HALFWIDTH KATAKANA LETTER SA
    + "\u30B7<>\uFF7C;" //  to HALFWIDTH KATAKANA LETTER SI
    + "\u30B9<>\uFF7D;" //  to HALFWIDTH KATAKANA LETTER SU
    + "\u30BB<>\uFF7E;" //  to HALFWIDTH KATAKANA LETTER SE
    + "\u30BD<>\uFF7F;" //  to HALFWIDTH KATAKANA LETTER SO
    + "\u30BF<>\uFF80;" //  to HALFWIDTH KATAKANA LETTER TA
    + "\u30C1<>\uFF81;" //  to HALFWIDTH KATAKANA LETTER TI
    + "\u30C4<>\uFF82;" //  to HALFWIDTH KATAKANA LETTER TU
    + "\u30C6<>\uFF83;" //  to HALFWIDTH KATAKANA LETTER TE
    + "\u30C8<>\uFF84;" //  to HALFWIDTH KATAKANA LETTER TO
    + "\u30CA<>\uFF85;" //  to HALFWIDTH KATAKANA LETTER NA
    + "\u30CB<>\uFF86;" //  to HALFWIDTH KATAKANA LETTER NI
    + "\u30CC<>\uFF87;" //  to HALFWIDTH KATAKANA LETTER NU
    + "\u30CD<>\uFF88;" //  to HALFWIDTH KATAKANA LETTER NE
    + "\u30CE<>\uFF89;" //  to HALFWIDTH KATAKANA LETTER NO
    + "\u30CF<>\uFF8A;" //  to HALFWIDTH KATAKANA LETTER HA
    + "\u30D2<>\uFF8B;" //  to HALFWIDTH KATAKANA LETTER HI
    + "\u30D5<>\uFF8C;" //  to HALFWIDTH KATAKANA LETTER HU
    + "\u30D8<>\uFF8D;" //  to HALFWIDTH KATAKANA LETTER HE
    + "\u30DB<>\uFF8E;" //  to HALFWIDTH KATAKANA LETTER HO
    + "\u30DE<>\uFF8F;" //  to HALFWIDTH KATAKANA LETTER MA
    + "\u30DF<>\uFF90;" //  to HALFWIDTH KATAKANA LETTER MI
    + "\u30E0<>\uFF91;" //  to HALFWIDTH KATAKANA LETTER MU
    + "\u30E1<>\uFF92;" //  to HALFWIDTH KATAKANA LETTER ME
    + "\u30E2<>\uFF93;" //  to HALFWIDTH KATAKANA LETTER MO
    + "\u30E4<>\uFF94;" //  to HALFWIDTH KATAKANA LETTER YA
    + "\u30E6<>\uFF95;" //  to HALFWIDTH KATAKANA LETTER YU
    + "\u30E8<>\uFF96;" //  to HALFWIDTH KATAKANA LETTER YO
    + "\u30E9<>\uFF97;" //  to HALFWIDTH KATAKANA LETTER RA
    + "\u30EA<>\uFF98;" //  to HALFWIDTH KATAKANA LETTER RI
    + "\u30EB<>\uFF99;" //  to HALFWIDTH KATAKANA LETTER RU
    + "\u30EC<>\uFF9A;" //  to HALFWIDTH KATAKANA LETTER RE
    + "\u30ED<>\uFF9B;" //  to HALFWIDTH KATAKANA LETTER RO
    + "\u30EF<>\uFF9C;" //  to HALFWIDTH KATAKANA LETTER WA
    + "\u30F3<>\uFF9D;" //  to HALFWIDTH KATAKANA LETTER N
    + "\u3099<>\uFF9E;" //  to HALFWIDTH KATAKANA VOICED SOUND MARK
    + "\u309A<>\uFF9F;" //  to HALFWIDTH KATAKANA SEMI-VOICED SOUND MARK
    + "\u1160<>\uFFA0;" //  to HALFWIDTH HANGUL FILLER
    + "\u1100<>\uFFA1;" //  to HALFWIDTH HANGUL LETTER KIYEOK
    + "\u1101<>\uFFA2;" //  to HALFWIDTH HANGUL LETTER SSANGKIYEOK
    + "\u11AA<>\uFFA3;" //  to HALFWIDTH HANGUL LETTER KIYEOK-SIOS
    + "\u1102<>\uFFA4;" //  to HALFWIDTH HANGUL LETTER NIEUN
    + "\u11AC<>\uFFA5;" //  to HALFWIDTH HANGUL LETTER NIEUN-CIEUC
    + "\u11AD<>\uFFA6;" //  to HALFWIDTH HANGUL LETTER NIEUN-HIEUH
    + "\u1103<>\uFFA7;" //  to HALFWIDTH HANGUL LETTER TIKEUT
    + "\u1104<>\uFFA8;" //  to HALFWIDTH HANGUL LETTER SSANGTIKEUT
    + "\u1105<>\uFFA9;" //  to HALFWIDTH HANGUL LETTER RIEUL
    + "\u11B0<>\uFFAA;" //  to HALFWIDTH HANGUL LETTER RIEUL-KIYEOK
    + "\u11B1<>\uFFAB;" //  to HALFWIDTH HANGUL LETTER RIEUL-MIEUM
    + "\u11B2<>\uFFAC;" //  to HALFWIDTH HANGUL LETTER RIEUL-PIEUP
    + "\u11B3<>\uFFAD;" //  to HALFWIDTH HANGUL LETTER RIEUL-SIOS
    + "\u11B4<>\uFFAE;" //  to HALFWIDTH HANGUL LETTER RIEUL-THIEUTH
    + "\u11B5<>\uFFAF;" //  to HALFWIDTH HANGUL LETTER RIEUL-PHIEUPH
    + "\u111A<>\uFFB0;" //  to HALFWIDTH HANGUL LETTER RIEUL-HIEUH
    + "\u1106<>\uFFB1;" //  to HALFWIDTH HANGUL LETTER MIEUM
    + "\u1107<>\uFFB2;" //  to HALFWIDTH HANGUL LETTER PIEUP
    + "\u1108<>\uFFB3;" //  to HALFWIDTH HANGUL LETTER SSANGPIEUP
    + "\u1121<>\uFFB4;" //  to HALFWIDTH HANGUL LETTER PIEUP-SIOS
    + "\u1109<>\uFFB5;" //  to HALFWIDTH HANGUL LETTER SIOS
    + "\u110A<>\uFFB6;" //  to HALFWIDTH HANGUL LETTER SSANGSIOS
    + "\u110B<>\uFFB7;" //  to HALFWIDTH HANGUL LETTER IEUNG
    + "\u110C<>\uFFB8;" //  to HALFWIDTH HANGUL LETTER CIEUC
    + "\u110D<>\uFFB9;" //  to HALFWIDTH HANGUL LETTER SSANGCIEUC
    + "\u110E<>\uFFBA;" //  to HALFWIDTH HANGUL LETTER CHIEUCH
    + "\u110F<>\uFFBB;" //  to HALFWIDTH HANGUL LETTER KHIEUKH
    + "\u1110<>\uFFBC;" //  to HALFWIDTH HANGUL LETTER THIEUTH
    + "\u1111<>\uFFBD;" //  to HALFWIDTH HANGUL LETTER PHIEUPH
    + "\u1112<>\uFFBE;" //  to HALFWIDTH HANGUL LETTER HIEUH
    + "\u1161<>\uFFC2;" //  to HALFWIDTH HANGUL LETTER A
    + "\u1162<>\uFFC3;" //  to HALFWIDTH HANGUL LETTER AE
    + "\u1163<>\uFFC4;" //  to HALFWIDTH HANGUL LETTER YA
    + "\u1164<>\uFFC5;" //  to HALFWIDTH HANGUL LETTER YAE
    + "\u1165<>\uFFC6;" //  to HALFWIDTH HANGUL LETTER EO
    + "\u1166<>\uFFC7;" //  to HALFWIDTH HANGUL LETTER E
    + "\u1167<>\uFFCA;" //  to HALFWIDTH HANGUL LETTER YEO
    + "\u1168<>\uFFCB;" //  to HALFWIDTH HANGUL LETTER YE
    + "\u1169<>\uFFCC;" //  to HALFWIDTH HANGUL LETTER O
    + "\u116A<>\uFFCD;" //  to HALFWIDTH HANGUL LETTER WA
    + "\u116B<>\uFFCE;" //  to HALFWIDTH HANGUL LETTER WAE
    + "\u116C<>\uFFCF;" //  to HALFWIDTH HANGUL LETTER OE
    + "\u116D<>\uFFD2;" //  to HALFWIDTH HANGUL LETTER YO
    + "\u116E<>\uFFD3;" //  to HALFWIDTH HANGUL LETTER U
    + "\u116F<>\uFFD4;" //  to HALFWIDTH HANGUL LETTER WEO
    + "\u1170<>\uFFD5;" //  to HALFWIDTH HANGUL LETTER WE
    + "\u1171<>\uFFD6;" //  to HALFWIDTH HANGUL LETTER WI
    + "\u1172<>\uFFD7;" //  to HALFWIDTH HANGUL LETTER YU
    + "\u1173<>\uFFDA;" //  to HALFWIDTH HANGUL LETTER EU
    + "\u1174<>\uFFDB;" //  to HALFWIDTH HANGUL LETTER YI
    + "\u1175<>\uFFDC;" //  to HALFWIDTH HANGUL LETTER I
    + "\uFFE0<>'\u00a2';"    //  from FULLWIDTH CENT SIGN
    + "\uFFE1<>'\u00a3';"    //  from FULLWIDTH POUND SIGN
    + "\uFFE2<>'\u00ac';"    //  from FULLWIDTH NOT SIGN
    + "\uFFE3<>' '\u0304;"  //  from FULLWIDTH MACRON
    + "\uFFE4<>'\u00a6';"    //  from FULLWIDTH BROKEN BAR
    + "\uFFE5<>'\u00a5';"    //  from FULLWIDTH YEN SIGN
    + "\uFFE6<>\u20A9;" //  from FULLWIDTH WON SIGN
    + "\u2502<>\uFFE8;" //  to HALFWIDTH FORMS LIGHT VERTICAL
    + "\u2190<>\uFFE9;" //  to HALFWIDTH LEFTWARDS ARROW
    + "\u2191<>\uFFEA;" //  to HALFWIDTH UPWARDS ARROW
    + "\u2192<>\uFFEB;" //  to HALFWIDTH RIGHTWARDS ARROW
    + "\u2193<>\uFFEC;" //  to HALFWIDTH DOWNWARDS ARROW
    + "\u25A0<>\uFFED;" //  to HALFWIDTH BLACK SQUARE
    + "\u25CB<>\uFFEE;" //  to HALFWIDTH WHITE CIRCLE

            }
        };
    }
}
