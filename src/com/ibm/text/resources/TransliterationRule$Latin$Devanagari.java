package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRuleLatinDevanagari extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Latin to Devanagari" },

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
                  "candrabindu=\u0901\n"
                + "bindu=\u0902\n"
                + "visarga=\u0903\n"

                // w<vowel> represents the stand-alone form
                + "wa=\u0905\n"
                + "waa=\u0906\n"
                + "wi=\u0907\n"
                + "wii=\u0908\n"
                + "wu=\u0909\n"
                + "wuu=\u090A\n"
                + "wr=\u090B\n"
                + "wl=\u090C\n"
                + "we=\u090F\n"
                + "wai=\u0910\n"
                + "wo=\u0913\n"
                + "wau=\u0914\n"

                + "ka=\u0915\n"
                + "kha=\u0916\n"
                + "ga=\u0917\n"
                + "gha=\u0918\n"
                + "nga=\u0919\n"

                + "ca=\u091A\n"
                + "cha=\u091B\n"
                + "ja=\u091C\n"
                + "jha=\u091D\n"
                + "nya=\u091E\n"

                + "tta=\u091F\n"
                + "ttha=\u0920\n"
                + "dda=\u0921\n"
                + "ddha=\u0922\n"
                + "nna=\u0923\n"

                + "ta=\u0924\n"
                + "tha=\u0925\n"
                + "da=\u0926\n"
                + "dha=\u0927\n"
                + "na=\u0928\n"

                + "pa=\u092A\n"
                + "pha=\u092B\n"
                + "ba=\u092C\n"
                + "bha=\u092D\n"
                + "ma=\u092E\n"

                + "ya=\u092F\n"
                + "ra=\u0930\n"
                + "rra=\u0931\n"
                + "la=\u0933\n"
                + "va=\u0935\n"

                + "sha=\u0936\n"
                + "ssa=\u0937\n"
                + "sa=\u0938\n"
                + "ha=\u0939\n"

                // <vowel> represents the dependent form
                + "aa=\u093E\n"
                + "i=\u093F\n"
                + "ii=\u0940\n"
                + "u=\u0941\n"
                + "uu=\u0942\n"
                + "rh=\u0943\n"
                + "lh=\u0944\n"
                + "e=\u0947\n"
                + "ai=\u0948\n"
                + "o=\u094B\n"
                + "au=\u094C\n"

                + "virama=\u094D\n"

                + "wrr=\u0960\n"
                + "rrh=\u0962\n"

                  + "danda=\u0964\n"
                  + "doubleDanda=\u0965\n"
                  + "depVowelAbove=[\u093E-\u0940\u0945-\u094C]\n"
                  + "depVowelBelow=[\u0941-\u0944]\n"
                  + "endThing=[{danda}{doubleDanda}\u0000-\u08FF\u0980-\uFFFF]\n"

                + "&=[{virama}{aa}{ai}{au}{ii}{i}{uu}{u}{rrh}{rh}{lh}{e}{o}]\n"
                + "%=[bcdfghjklmnpqrstvwxyz]\n"

                //#####################################################################
                // convert from Latin letters to Native letters
                //#####################################################################
                //Hindi>\u092d\u093e\u0930\u0924--\u0020\u0926\u0947\u0936\u0020\u092c\u0928\u094d\u0927\u0941\u002e

                // special forms with no good conversion

                + "mm>{bindu}\n"
                + "x>{visarga}\n"
 
                // convert to independent forms at start of word or syllable: 
                // e.g. keai -> {ka}{e}{wai}; k'ai -> {ka}{wai}; (ai) -> ({wai})
                // Moved up [LIU]

                + "aa>{waa}\n"
                + "ai>{wai}\n"
                + "au>{wau}\n"
                + "ii>{wii}\n"
                + "i>{wi}\n"
                + "uu>{wuu}\n"
                + "u>{wu}\n"
                + "rrh>{wrr}\n"
                + "rh>{wr}\n"
                + "lh>{wl}\n"
                + "e>{we}\n"
                + "o>{wo}\n"
                + "a>{wa}\n"

                // normal consonants

                + "kh>{kha}|{virama}\n"
                + "k>{ka}|{virama}\n"
                + "q>{ka}|{virama}\n"
                + "gh>{gha}|{virama}\n"
                + "g>{ga}|{virama}\n"
                + "ng>{nga}|{virama}\n"
                + "ch>{cha}|{virama}\n"
                + "c>{ca}|{virama}\n"
                + "jh>{jha}|{virama}\n"
                + "j>{ja}|{virama}\n"
                + "ny>{nya}|{virama}\n"
                + "tth>{ttha}|{virama}\n"
                + "tt>{tta}|{virama}\n"
                + "ddh>{ddha}|{virama}\n"
                + "dd>{dda}|{virama}\n"
                + "nn>{nna}|{virama}\n"
                + "th>{tha}|{virama}\n"
                + "t>{ta}|{virama}\n"
                + "dh>{dha}|{virama}\n"
                + "d>{da}|{virama}\n"
                + "n>{na}|{virama}\n"
                + "ph>{pha}|{virama}\n"
                + "p>{pa}|{virama}\n"
                + "bh>{bha}|{virama}\n"
                + "b>{ba}|{virama}\n"
                + "m>{ma}|{virama}\n"
                + "y>{ya}|{virama}\n"
                + "r>{ra}|{virama}\n"
                + "l>{la}|{virama}\n"
                + "v>{va}|{virama}\n"
                + "f>{va}|{virama}\n"
                + "w>{va}|{virama}\n"
                + "sh>{sha}|{virama}\n"
                + "ss>{ssa}|{virama}\n"
                + "s>{sa}|{virama}\n"
                + "z>{sa}|{virama}\n"
                + "h>{ha}|{virama}\n"

                  + ".>{danda}\n"
                  + "{danda}.>{doubleDanda}\n"
                  + "{depVowelAbove}]~>{bindu}\n"
                  + "{depVowelBelow}]~>{candrabindu}\n"

                // convert to dependent forms after consonant with no vowel: 
                // e.g. kai -> {ka}{virama}ai -> {ka}{ai}

                + "{virama}aa>{aa}\n"
                + "{virama}ai>{ai}\n"
                + "{virama}au>{au}\n"
                + "{virama}ii>{ii}\n"
                + "{virama}i>{i}\n"
                + "{virama}uu>{uu}\n"
                + "{virama}u>{u}\n"
                + "{virama}rrh>{rrh}\n"
                + "{virama}rh>{rh}\n"
                + "{virama}lh>{lh}\n"
                + "{virama}e>{e}\n"
                + "{virama}o>{o}\n"
                + "{virama}a>\n"

                // otherwise convert independent forms when separated by ': k'ai -> {ka}{virama}{wai}

                + "{virama}''aa>{waa}\n"
                + "{virama}''ai>{wai}\n"
                + "{virama}''au>{wau}\n"
                + "{virama}''ii>{wii}\n"
                + "{virama}''i>{wi}\n"
                + "{virama}''uu>{wuu}\n"
                + "{virama}''u>{wu}\n"
                + "{virama}''rrh>{wrr}\n"
                + "{virama}''rh>{wr}\n"
                + "{virama}''lh>{wl}\n"
                + "{virama}''e>{we}\n"
                + "{virama}''o>{wo}\n"
                + "{virama}''a>{wa}\n"

                  + "{virama}[{endThing}>\n"

                // convert any left-over apostrophes used for separation

                + "''>\n"

                //#####################################################################
                // convert from Native letters to Latin letters
                //#####################################################################

                // special forms with no good conversion

                + "mm<{bindu}\n"
                + "x<{visarga}\n"

                // normal consonants

                + "kh<{kha}[&\n"
                + "kha<{kha}\n"
                + "k''<{ka}{virama}[{ha}\n"
                + "k<{ka}[&\n"
                + "ka<{ka}\n"
                + "gh<{gha}[&\n"
                + "gha<{gha}\n"
                + "g''<{ga}{virama}[{ha}\n"
                + "g<{ga}[&\n"
                + "ga<{ga}\n"
                + "ng<{nga}[&\n"
                + "nga<{nga}\n"
                + "ch<{cha}[&\n"
                + "cha<{cha}\n"
                + "c''<{ca}{virama}[{ha}\n"
                + "c<{ca}[&\n"
                + "ca<{ca}\n"
                + "jh<{jha}[&\n"
                + "jha<{jha}\n"
                + "j''<{ja}{virama}[{ha}\n"
                + "j<{ja}[&\n"
                + "ja<{ja}\n"
                + "ny<{nya}[&\n"
                + "nya<{nya}\n"
                + "tth<{ttha}[&\n"
                + "ttha<{ttha}\n"
                + "tt''<{tta}{virama}[{ha}\n"
                + "tt<{tta}[&\n"
                + "tta<{tta}\n"
                + "ddh<{ddha}[&\n"
                + "ddha<{ddha}\n"
                + "dd''<{dda}[&{ha}\n"
                + "dd<{dda}[&\n"
                + "dda<{dda}\n"
                + "dh<{dha}[&\n"
                + "dha<{dha}\n"
                + "d''<{da}{virama}[{ha}\n"
                + "d''<{da}{virama}[{ddha}\n"
                + "d''<{da}{virama}[{dda}\n"
                + "d''<{da}{virama}[{dha}\n"
                + "d''<{da}{virama}[{da}\n"
                + "d<{da}[&\n"
                + "da<{da}\n"
                + "th<{tha}[&\n"
                + "tha<{tha}\n"
                + "t''<{ta}{virama}[{ha}\n"
                + "t''<{ta}{virama}[{ttha}\n"
                + "t''<{ta}{virama}[{tta}\n"
                + "t''<{ta}{virama}[{tha}\n"
                + "t''<{ta}{virama}[{ta}\n"
                + "t<{ta}[&\n"
                + "ta<{ta}\n"
                + "n''<{na}{virama}[{ga}\n"
                + "n''<{na}{virama}[{ya}\n"
                + "n<{na}[&\n"
                + "na<{na}\n"
                + "ph<{pha}[&\n"
                + "pha<{pha}\n"
                + "p''<{pa}{virama}[{ha}\n"
                + "p<{pa}[&\n"
                + "pa<{pa}\n"
                + "bh<{bha}[&\n"
                + "bha<{bha}\n"
                + "b''<{ba}{virama}[{ha}\n"
                + "b<{ba}[&\n"
                + "ba<{ba}\n"
                + "m''<{ma}{virama}[{ma}\n"
                + "m''<{ma}{virama}[{bindu}\n"
                + "m<{ma}[&\n"
                + "ma<{ma}\n"
                + "y<{ya}[&\n"
                + "ya<{ya}\n"
                + "r''<{ra}{virama}[{ha}\n"
                + "r<{ra}[&\n"
                + "ra<{ra}\n"
                + "l''<{la}{virama}[{ha}\n"
                + "l<{la}[&\n"
                + "la<{la}\n"
                + "v<{va}[&\n"
                + "va<{va}\n"
                + "sh<{sha}[&\n"
                + "sha<{sha}\n"
                + "ss<{ssa}[&\n"
                + "ssa<{ssa}\n"
                + "s''<{sa}{virama}[{ha}\n"
                + "s''<{sa}{virama}[{sha}\n"
                + "s''<{sa}{virama}[{ssa}\n"
                + "s''<{sa}{virama}[{sa}\n"
                + "s<{sa}[&\n"
                + "sa<{sa}\n"
                + "h<{ha}[&\n"
                + "ha<{ha}\n"

                // dependent vowels (should never occur except following consonants)

                + "aa<{aa}\n"
                + "ai<{ai}\n"
                + "au<{au}\n"
                + "ii<{ii}\n"
                + "i<{i}\n"
                + "uu<{uu}\n"
                + "u<{u}\n"
                + "rrh<{rrh}\n"
                + "rh<{rh}\n"
                + "lh<{lh}\n"
                + "e<{e}\n"
                + "o<{o}\n"

                // independent vowels (when following consonants)

                + "''aa<a]{waa}\n"
                + "''aa<%]{waa}\n"
                + "''ai<a]{wai}\n"
                + "''ai<%]{wai}\n"
                + "''au<a]{wau}\n"
                + "''au<%]{wau}\n"
                + "''ii<a]{wii}\n"
                + "''ii<%]{wii}\n"
                + "''i<a]{wi}\n"
                + "''i<%]{wi}\n"
                + "''uu<a]{wuu}\n"
                + "''uu<%]{wuu}\n"
                + "''u<a]{wu}\n"
                + "''u<%]{wu}\n"
                + "''rrh<%]{wrr}\n"
                + "''rh<%]{wr}\n"
                + "''lh<%]{wl}\n"
                + "''e<%]{we}\n"
                + "''o<%]{wo}\n"
                + "''a<a]{wa}\n"
                + "''a<%]{wa}\n"


                // independent vowels (otherwise)

                + "aa<{waa}\n"
                + "ai<{wai}\n"
                + "au<{wau}\n"
                + "ii<{wii}\n"
                + "i<{wi}\n"
                + "uu<{wuu}\n"
                + "u<{wu}\n"
                + "rrh<{wrr}\n"
                + "rh<{wr}\n"
                + "lh<{wl}\n"
                + "e<{we}\n"
                + "o<{wo}\n"
                + "a<{wa}\n"

                // blow away any remaining viramas

                + "<{virama}\n"
            }
        };
    }
}
