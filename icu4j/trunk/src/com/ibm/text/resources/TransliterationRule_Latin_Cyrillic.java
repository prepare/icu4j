/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Cyrillic.java,v $ 
 * $Date: 2000/03/10 04:07:30 $ 
 * $Revision: 1.2 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Latin_Cyrillic extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule", "" 
            
                /* This class is designed to be a general Latin-Cyrillic
                   transliteration. The standard Russian transliterations
                   are generally used for the letters from Russian,
                   with additional Cyrillic characters given consistent
                   mappings.
                */
            
                + "S-hacek=\u0160;"
                + "s-hacek=\u0161;"

                + "YO=\u0401;"
                + "J=\u0408;"
                + "A=\u0410;"
                + "B=\u0411;"
                + "V=\u0412;"
                + "G=\u0413;"
                + "D=\u0414;"
                + "YE=\u0415;"
                + "ZH=\u0416;"
                + "Z=\u0417;"
                + "YI=\u0418;"
                + "Y=\u0419;"
                + "K=\u041A;"
                + "L=\u041B;"
                + "M=\u041C;"
                + "N=\u041D;"
                + "O=\u041E;"
                + "P=\u041F;"
                + "R=\u0420;"
                + "S=\u0421;"
                + "T=\u0422;"
                + "U=\u0423;"
                + "F=\u0424;"
                + "KH=\u0425;"
                + "TS=\u0426;"
                + "CH=\u0427;"
                + "SH=\u0428;"
                + "SHCH=\u0429;"
                + "HARD=\u042A;"
                + "I=\u042B;"
                + "SOFT=\u042C;"
                + "E=\u042D;"
                + "YU=\u042E;"
                + "YA=\u042F;"
                
                // Lowercase

                + "a=\u0430;"
                + "b=\u0431;"
                + "v=\u0432;"
                + "g=\u0433;"
                + "d=\u0434;"
                + "ye=\u0435;"
                + "zh=\u0436;"
                + "z=\u0437;"
                + "yi=\u0438;"
                + "y=\u0439;"
                + "k=\u043a;"
                + "l=\u043b;"
                + "m=\u043c;"
                + "n=\u043d;"
                + "o=\u043e;"
                + "p=\u043f;"
                + "r=\u0440;"
                + "s=\u0441;"
                + "t=\u0442;"
                + "u=\u0443;"
                + "f=\u0444;"
                + "kh=\u0445;"
                + "ts=\u0446;"
                + "ch=\u0447;"
                + "sh=\u0448;"
                + "shch=\u0449;"
                + "hard=\u044a;"
                + "i=\u044b;"
                + "soft=\u044c;"
                + "e=\u044d;"
                + "yu=\u044e;"
                + "ya=\u044f;"

                + "yo=\u0451;"
                + "j=\u0458;"
                
                // variables
                // some are duplicated so lowercasing works
                
                + "csoft=[eiyEIY];"
                + "CSOFT=[eiyEIY];"
                
                + "BECOMES_H=[{HARD}{hard}];"
                + "becomes_h=[{HARD}{hard}];"
                
                + "BECOMES_S=[{S}{s}];"
                + "becomes_s=[{S}{s}];"
                
                + "BECOMES_C=[{CH}{ch}];"
                + "becomes_c=[{CH}{ch}];"
 
                + "BECOMES_VOWEL=[{A}{E}{I}{O}{U}{a}{e}{i}{o}{u}];"
                + "becomes_vowel=[{A}{E}{I}{O}{U}{a}{e}{i}{o}{u}];"
                
                + "letter=[[:Lu:][:Ll:]];"
                + "lower=[[:Ll:]];"     
                
                /*
                    Modified to combine display transliterator and typing transliterator.
                    The display mapping uses accents for the "soft" vowels.
                    It does not, although it could, use characters like \u0161 instead of digraphs
                    like sh.
                */
                
                // #############################################
                // Special titlecase forms, not duplicated
                // #############################################
                
                + "Ch>{CH};" + "Ch<{CH}({lower};"
                + "Kh>{KH};" + "Kh<{KH}({lower};"
                + "Shch>{SHCH};" + "Shch<{SHCH}({lower};"
                + "Sh>{SH};" + "Sh<{SH}({lower};"
                + "Ts>{TS};"  + "Ts<{TS}({lower};"
                + "Zh>{ZH};" + "Zh<{ZH}({lower};"
                + "Yi>{YI};"  //+ "Yi<{YI}({lower};"
                + "Ye>{YE};"  //+ "Ye<{YE}({lower};"
                + "Yo>{YO};" //+ "Yo<{YO}({lower};"
                + "Yu>{YU};" //+ "Yu<{YU}({lower};"
                + "Ya>{YA};" //+ "Ya<{YA}({lower};"
                
                // #############################################
                // Rules to Duplicate
                // To get the lowercase versions, copy these and lowercase
                // #############################################

                // variant spellings in English
                
                + "SHTCH>{SHCH};"
                + "TCH>{CH};"
                + "TH>{Z};"
                + "Q>{K};"
                + "WH>{V};"
                + "W>{V};"
                + "X>{K}{S};"      //+ "X<{K}{S};"
                
                // Separate letters that would otherwise join
                
                + "SH''<{SH}({BECOMES_C};"
                + "T''<{T}({BECOMES_S};"
                
                + "K''<{K}({BECOMES_H};"
                + "S''<{S}({BECOMES_H};"
                + "T''<{T}({BECOMES_H};"
                + "Z''<{Z}({BECOMES_H};"
                
                + "Y''<{Y}({BECOMES_VOWEL};"
                
                // Main letters

                + "A<>{A};"
                + "B<>{B};"
                + "CH<>{CH};"
                + "D<>{D};"
                + "E<>{E};"
                + "F<>{F};"
                + "G<>{G};"
                + "\u00cc<>{YI};"
                + "I<>{I};"
                + "KH<>{KH};"
                + "K<>{K};"
                + "L<>{L};"
                + "M<>{M};"
                + "N<>{N};"
                + "O<>{O};"
                + "P<>{P};"
                + "R<>{R};"
                + "SHCH<>{SHCH};"
                + "SH>{SH};"       //+ "SH<{SH};"
                + "{S-hacek}<>{SH};"
                + "S<>{S};"
                + "TS<>{TS};"
                + "T<>{T};"
                + "U<>{U};"
                + "V<>{V};"
                //\u00cc\u00c0\u00c8\u00d2\u00d9
                + "YE>{YE};"       //+ "YE<{YE};"
                + "\u00c8<>{YE};"
                + "YO>{YO};"       //+ "YO<{YO};"
                + "\u00d2<>{YO};"
                + "YU>{YU};"       //+ "YU<{YU};"
                + "\u00d9<>{YU};"
                + "YA>{YA};"       //+ "YA<{YA};"
                + "\u00c0<>{YA};"
                + "Y<>{Y};"
                + "ZH<>{ZH};"
                + "Z<>{Z};"

                + "H<>{HARD};"
                + "\u0178<>{SOFT};"
                
                // Non-russian
                
                + "J<>{J};"

                // variant spellings in English
                
                + "C({csoft}>{S};"
                + "C>{K};"

                // #############################################
                // Duplicated Rules
                // Copy and lowercase the above rules
                // #############################################
                
                 // variant spellings in english
                
                + "shtch>{shch};"
                + "tch>{ch};"
                + "th>{z};"
                + "q>{k};"
                + "wh>{v};"
                + "w>{v};"
                + "x>{k}{s};"      //+ "x<{k}{s};"
                
                // separate letters that would otherwise join
                
                + "sh''<{sh}({becomes_c};"
                + "t''<{t}({becomes_s};"
                
                + "k''<{k}({becomes_h};"
                + "s''<{s}({becomes_h};"
                + "t''<{t}({becomes_h};"
                + "z''<{z}({becomes_h};"
                
                + "y''<{y}({becomes_vowel};"
                
                // main letters

                + "a<>{a};"
                + "b<>{b};"
                + "ch<>{ch};"
                + "d<>{d};"
                + "e<>{e};"
                + "f<>{f};"
                + "g<>{g};"
                + "\u00ec<>{yi};"
                + "i<>{i};"
                + "kh<>{kh};"
                + "k<>{k};"
                + "l<>{l};"
                + "m<>{m};"
                + "n<>{n};"
                + "o<>{o};"
                + "p<>{p};"
                + "r<>{r};"
                + "shch<>{shch};"
                + "sh>{sh};"       //+ "sh<{sh};"
                + "{s-hacek}<>{sh};"
                + "s<>{s};"
                + "ts<>{ts};"
                + "t<>{t};"
                + "u<>{u};"
                + "v<>{v};"
                //\u00ec\u00e0\u00e8\u00f2\u00f9
                + "ye>{ye};"       //+ "ye<{ye};"
                + "\u00e8<>{ye};"
                + "yo>{yo};"       //+ "yo<{yo};"
                + "\u00f2<>{yo};"
                + "yu>{yu};"       //+ "yu<{yu};"
                + "\u00f9<>{yu};"
                + "ya>{ya};"       //+ "ya<{ya};"
                + "\u00e0<>{ya};"
                + "y<>{y};"
                + "zh<>{zh};"
                + "z<>{z};"

                + "h<>{hard};"
                + "\u00ff<>{soft};"
                
                // non-russian
                
                + "j<>{j};"

                // variant spellings in english
                
                + "c({csoft}>{s};"
                + "c>{k};"


               
                // #############################################
                // End of Duplicated Rules
                // #############################################
                
                //generally the last rule
                + "''>;"
                //the end
            }
        };
    }
}
