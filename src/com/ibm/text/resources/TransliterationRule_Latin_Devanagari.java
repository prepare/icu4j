/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/resources/Attic/TransliterationRule_Latin_Devanagari.java,v $ 
 * $Date: 2000/04/22 00:04:25 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule_Latin_Devanagari extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                //#####################################################################
                //	Keyboard Transliteration Table
                //#####################################################################
                // Conversions should be:
                // 1. complete
                //  * convert every sequence of Latin letters (a to z plus apostrophe) 
                //    to a sequence of Native letters
                //  * convert every sequence of Native letters to Latin letters
                // 2. reversable
                //  * any string of Native converted to Latin and back should be the same
                //  * this is not true for English converted to Native & back, e.g.:
                //		k -> {kaf} -> k
                //		c -> {kaf} -> k
                //#####################################################################
                // Sequences of Latin letters may convert to a single Native letter.
                // When this is the case, an apostrophe can be used to indicate separate
                // letters.$
                // E.g.	sh -> {shin}
                //		s'h -> {sin}{heh}
                // 		ss -> {sad}
                // 		s's -> {sin}{shadda}
                //#####################################################################
                // To Do:
                //	finish adding shadda, add sokoon, fix uppercase
                //	make two transliteration tables: one with vowels, one without
                //#####################################################################
                // Modifications
                //	Devanagari Transliterator:  broken up with consonsants/vowels
                //#####################################################################
                // Unicode character name definitions
                //#####################################################################

                //consonants
                  "$candrabindu=\u0901;"
                + "$bindu=\u0902;"
                + "$visarga=\u0903;"

                // w<vowel> represents the stand-alone form
                + "$wa=\u0905;"
                + "$waa=\u0906;"
                + "$wi=\u0907;"
                + "$wii=\u0908;"
                + "$wu=\u0909;"
                + "$wuu=\u090A;"
                + "$wr=\u090B;"
                + "$wl=\u090C;"
                + "$we=\u090F;"
                + "$wai=\u0910;"
                + "$wo=\u0913;"
                + "$wau=\u0914;"

                + "$ka=\u0915;"
                + "$kha=\u0916;"
                + "$ga=\u0917;"
                + "$gha=\u0918;"
                + "$nga=\u0919;"

                + "$ca=\u091A;"
                + "$cha=\u091B;"
                + "$ja=\u091C;"
                + "$jha=\u091D;"
                + "$nya=\u091E;"

                + "$tta=\u091F;"
                + "$ttha=\u0920;"
                + "$dda=\u0921;"
                + "$ddha=\u0922;"
                + "$nna=\u0923;"

                + "$ta=\u0924;"
                + "$tha=\u0925;"
                + "$da=\u0926;"
                + "$dha=\u0927;"
                + "$na=\u0928;"

                + "$pa=\u092A;"
                + "$pha=\u092B;"
                + "$ba=\u092C;"
                + "$bha=\u092D;"
                + "$ma=\u092E;"

                + "$ya=\u092F;"
                + "$ra=\u0930;"
                + "$rra=\u0931;"
                + "$la=\u0933;"
                + "$va=\u0935;"

                + "$sha=\u0936;"
                + "$ssa=\u0937;"
                + "$sa=\u0938;"
                + "$ha=\u0939;"

                // <vowel> represents the dependent form
                + "$aa=\u093E;"
                + "$i=\u093F;"
                + "$ii=\u0940;"
                + "$u=\u0941;"
                + "$uu=\u0942;"
                + "$rh=\u0943;"
                + "$lh=\u0944;"
                + "$e=\u0947;"
                + "$ai=\u0948;"
                + "$o=\u094B;"
                + "$au=\u094C;"

                + "$virama=\u094D;"

                + "$wrr=\u0960;"
                + "$rrh=\u0962;"

                  + "$danda=\u0964;"
                  + "$doubleDanda=\u0965;"
                  + "$depVowelAbove=[\u093E-\u0940\u0945-\u094C];"
                  + "$depVowelBelow=[\u0941-\u0944];"
                  + "$endThing=[$danda$doubleDanda \u0000-\u08FF\u0980-\uFFFF];"

                // $x was originally called '&'; $z was '%'
                + "$x=[$virama$aa$ai$au$ii$i$uu$u$rrh$rh$lh$e$o];"
                + "$z=[bcdfghjklmnpqrstvwxyz];"

                //#####################################################################
                // convert from Latin letters to Native letters
                //#####################################################################
                //Hindi>\u092d\u093e\u0930\u0924--\u0020\u0926\u0947\u0936\u0020\u092c\u0928\u094d\u0927\u0941\u002e

                // special forms with no good conversion

                + "mm>$bindu;"
                + "x>$visarga;"
 
                // convert to independent forms at start of word or syllable: 
                // e.g. keai -> {ka}{e}{wai}; k'ai -> {ka}{wai}; (ai) -> ({wai})
                // Moved up [LIU]

                + "aa>$waa;"
                + "ai>$wai;"
                + "au>$wau;"
                + "ii>$wii;"
                + "i>$wi;"
                + "uu>$wuu;"
                + "u>$wu;"
                + "rrh>$wrr;"
                + "rh>$wr;"
                + "lh>$wl;"
                + "e>$we;"
                + "o>$wo;"
                + "a>$wa;"

                // normal consonants

                + "kh>$kha|$virama;"
                + "k>$ka|$virama;"
                + "q>$ka|$virama;"
                + "gh>$gha|$virama;"
                + "g>$ga|$virama;"
                + "ng>$nga|$virama;"
                + "ch>$cha|$virama;"
                + "c>$ca|$virama;"
                + "jh>$jha|$virama;"
                + "j>$ja|$virama;"
                + "ny>$nya|$virama;"
                + "tth>$ttha|$virama;"
                + "tt>$tta|$virama;"
                + "ddh>$ddha|$virama;"
                + "dd>$dda|$virama;"
                + "nn>$nna|$virama;"
                + "th>$tha|$virama;"
                + "t>$ta|$virama;"
                + "dh>$dha|$virama;"
                + "d>$da|$virama;"
                + "n>$na|$virama;"
                + "ph>$pha|$virama;"
                + "p>$pa|$virama;"
                + "bh>$bha|$virama;"
                + "b>$ba|$virama;"
                + "m>$ma|$virama;"
                + "y>$ya|$virama;"
                + "r>$ra|$virama;"
                + "l>$la|$virama;"
                + "v>$va|$virama;"
                + "f>$va|$virama;"
                + "w>$va|$virama;"
                + "sh>$sha|$virama;"
                + "ss>$ssa|$virama;"
                + "s>$sa|$virama;"
                + "z>$sa|$virama;"
                + "h>$ha|$virama;"

                  + "'.'>$danda;"
                  + "$danda'.'>$doubleDanda;"
                  + "$depVowelAbove{'~'>$bindu;"
                  + "$depVowelBelow{'~'>$candrabindu;"

                // convert to dependent forms after consonant with no vowel: 
                // e.g. kai -> {ka}{virama}ai -> {ka}{ai}

                + "$virama aa>$aa;"
                + "$virama ai>$ai;"
                + "$virama au>$au;"
                + "$virama ii>$ii;"
                + "$virama i>$i;"
                + "$virama uu>$uu;"
                + "$virama u>$u;"
                + "$virama rrh>$rrh;"
                + "$virama rh>$rh;"
                + "$virama lh>$lh;"
                + "$virama e>$e;"
                + "$virama o>$o;"
                + "$virama a>;"

                // otherwise convert independent forms when separated by ': k'ai -> {ka}{virama}{wai}

                + "$virama''aa>$waa;"
                + "$virama''ai>$wai;"
                + "$virama''au>$wau;"
                + "$virama''ii>$wii;"
                + "$virama''i>$wi;"
                + "$virama''uu>$wuu;"
                + "$virama''u>$wu;"
                + "$virama''rrh>$wrr;"
                + "$virama''rh>$wr;"
                + "$virama''lh>$wl;"
                + "$virama''e>$we;"
                + "$virama''o>$wo;"
                + "$virama''a>$wa;"

                  + "$virama}$endThing>;"

                // convert any left-over apostrophes used for separation

                + "''>;"

                //#####################################################################
                // convert from Native letters to Latin letters
                //#####################################################################

                // special forms with no good conversion

                + "mm<$bindu;"
                + "x<$visarga;"

                // normal consonants

                + "kh<$kha}$x;"
                + "kha<$kha;"
                + "k''<$ka$virama}$ha;"
                + "k<$ka}$x;"
                + "ka<$ka;"
                + "gh<$gha}$x;"
                + "gha<$gha;"
                + "g''<$ga$virama}$ha;"
                + "g<$ga}$x;"
                + "ga<$ga;"
                + "ng<$nga}$x;"
                + "nga<$nga;"
                + "ch<$cha}$x;"
                + "cha<$cha;"
                + "c''<$ca$virama}$ha;"
                + "c<$ca}$x;"
                + "ca<$ca;"
                + "jh<$jha}$x;"
                + "jha<$jha;"
                + "j''<$ja$virama}$ha;"
                + "j<$ja}$x;"
                + "ja<$ja;"
                + "ny<$nya}$x;"
                + "nya<$nya;"
                + "tth<$ttha}$x;"
                + "ttha<$ttha;"
                + "tt''<$tta$virama}$ha;"
                + "tt<$tta}$x;"
                + "tta<$tta;"
                + "ddh<$ddha}$x;"
                + "ddha<$ddha;"
                + "dd''<$dda}$x$ha;"
                + "dd<$dda}$x;"
                + "dda<$dda;"
                + "dh<$dha}$x;"
                + "dha<$dha;"
                + "d''<$da$virama}$ha;"
                + "d''<$da$virama}$ddha;"
                + "d''<$da$virama}$dda;"
                + "d''<$da$virama}$dha;"
                + "d''<$da$virama}$da;"
                + "d<$da}$x;"
                + "da<$da;"
                + "th<$tha}$x;"
                + "tha<$tha;"
                + "t''<$ta$virama}$ha;"
                + "t''<$ta$virama}$ttha;"
                + "t''<$ta$virama}$tta;"
                + "t''<$ta$virama}$tha;"
                + "t''<$ta$virama}$ta;"
                + "t<$ta}$x;"
                + "ta<$ta;"
                + "n''<$na$virama}$ga;"
                + "n''<$na$virama}$ya;"
                + "n<$na}$x;"
                + "na<$na;"
                + "ph<$pha}$x;"
                + "pha<$pha;"
                + "p''<$pa$virama}$ha;"
                + "p<$pa}$x;"
                + "pa<$pa;"
                + "bh<$bha}$x;"
                + "bha<$bha;"
                + "b''<$ba$virama}$ha;"
                + "b<$ba}$x;"
                + "ba<$ba;"
                + "m''<$ma$virama}$ma;"
                + "m''<$ma$virama}$bindu;"
                + "m<$ma}$x;"
                + "ma<$ma;"
                + "y<$ya}$x;"
                + "ya<$ya;"
                + "r''<$ra$virama}$ha;"
                + "r<$ra}$x;"
                + "ra<$ra;"
                + "l''<$la$virama}$ha;"
                + "l<$la}$x;"
                + "la<$la;"
                + "v<$va}$x;"
                + "va<$va;"
                + "sh<$sha}$x;"
                + "sha<$sha;"
                + "ss<$ssa}$x;"
                + "ssa<$ssa;"
                + "s''<$sa$virama}$ha;"
                + "s''<$sa$virama}$sha;"
                + "s''<$sa$virama}$ssa;"
                + "s''<$sa$virama}$sa;"
                + "s<$sa}$x;"
                + "sa<$sa;"
                + "h<$ha}$x;"
                + "ha<$ha;"

                // dependent vowels (should never occur except following consonants)

                + "aa<$aa;"
                + "ai<$ai;"
                + "au<$au;"
                + "ii<$ii;"
                + "i<$i;"
                + "uu<$uu;"
                + "u<$u;"
                + "rrh<$rrh;"
                + "rh<$rh;"
                + "lh<$lh;"
                + "e<$e;"
                + "o<$o;"

                // independent vowels (when following consonants)

                + "''aa<a{$waa;"
                + "''aa<$z{$waa;"
                + "''ai<a{$wai;"
                + "''ai<$z{$wai;"
                + "''au<a{$wau;"
                + "''au<$z{$wau;"
                + "''ii<a{$wii;"
                + "''ii<$z{$wii;"
                + "''i<a{$wi;"
                + "''i<$z{$wi;"
                + "''uu<a{$wuu;"
                + "''uu<$z{$wuu;"
                + "''u<a{$wu;"
                + "''u<$z{$wu;"
                + "''rrh<$z{$wrr;"
                + "''rh<$z{$wr;"
                + "''lh<$z{$wl;"
                + "''e<$z{$we;"
                + "''o<$z{$wo;"
                + "''a<a{$wa;"
                + "''a<$z{$wa;"


                // independent vowels (otherwise)

                + "aa<$waa;"
                + "ai<$wai;"
                + "au<$wau;"
                + "ii<$wii;"
                + "i<$wi;"
                + "uu<$wuu;"
                + "u<$wu;"
                + "rrh<$wrr;"
                + "rh<$wr;"
                + "lh<$wl;"
                + "e<$we;"
                + "o<$wo;"
                + "a<$wa;"

                // blow away any remaining viramas

                + "<$virama;"
            }
        };
    }
}
