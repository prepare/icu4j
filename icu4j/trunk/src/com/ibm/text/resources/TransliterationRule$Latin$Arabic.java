package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$Latin$Arabic extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                // To Do: finish adding shadda, add sokoon

                "alefmadda=\u0622;"+
                "alefuhamza=\u0623;"+
                "wauuhamza=\u0624;"+
                "alefhamza=\u0625;"+
                "yehuhamza=\u0626;"+
                "alef=\u0627;"+
                "beh=\u0628;"+
                "tehmarbuta=\u0629;"+
                "teh=\u062A;"+
                "theh=\u062B;"+
                "geem=\u062C;"+
                "hah=\u062D;"+
                "kha=\u062E;"+
                "dal=\u062F;"+
                "dhal=\u0630;"+
                "reh=\u0631;"+
                "zain=\u0632;"+
                "seen=\u0633;"+
                "sheen=\u0634;"+
                "sad=\u0635;"+
                "dad=\u0636;"+
                "tah=\u0637;"+
                "zah=\u0638;"+
                "ein=\u0639;"+
                "ghein=\u063A;"+
                "feh=\u0641;"+
                "qaaf=\u0642;"+
                "kaf=\u0643;"+
                "lam=\u0644;"+
                "meem=\u0645;"+
                "noon=\u0646;"+
                "heh=\u0647;"+
                "wau=\u0648;"+
                "yehmaqsura=\u0649;"+
                "yeh=\u064A;"+
                "peh=\u06A4;"+

                "hamza=\u0621;"+
                "fathatein=\u064B;"+
                "dammatein=\u064C;"+
                "kasratein=\u064D;"+
                "fatha=\u064E;"+
                "damma=\u064F;"+
                "kasra=\u0650;"+
                "shadda=\u0651;"+
                "sokoon=\u0652;"+

                // convert English to Arabic
                "Arabic>"+
                "\u062a\u062a\u0645\u062a\u0639\u0020"+
                "\u0627\u0644\u0644\u063a\u0629\u0020"+
                "\u0627\u0644\u0639\u0631\u0628\u0628\u064a\u0629\u0020"+
                "\u0628\u0628\u0646\u0638\u0645\u0020"+
                "\u0643\u062a\u0627\u0628\u0628\u064a\u0629\u0020"+
                "\u062c\u0645\u064a\u0644\u0629;"+

                "ai>{alefmadda};"+
                "ae>{alefuhamza};"+
                "ao>{alefhamza};"+
                "aa>{alef};"+
                "an>{fathatein};"+
                "a>{fatha};"+
                "b>{beh};"+
                "c>{kaf};"+
                "{dhal}]dh>{shadda};"+
                "dh>{dhal};"+
                "{dad}]dd>{shadda};"+
                "dd>{dad};"+
                "{dal}]d>{shadda};"+
                "d>{dal};"+
                "e>{ein};"+
                "f>{feh};"+
                "gh>{ghein};"+
                "g>{geem};"+
                "hh>{hah};"+
                "h>{heh};"+
                "ii>{kasratein};"+
                "i>{kasra};"+
                "j>{geem};"+
                "kh>{kha};"+
                "k>{kaf};"+
                "l>{lam};"+
                "m>{meem};"+
                "n>{noon};"+
                "o>{hamza};"+
                "p>{peh};"+
                "q>{qaaf};"+
                "r>{reh};"+
                "sh>{sheen};"+
                "ss>{sad};"+
                "s>{seen};"+
                "th>{theh};"+
                "tm>{tehmarbuta};"+
                "tt>{tah};"+
                "t>{teh};"+
                "uu>{dammatein};"+
                "u>{damma};"+
                "v>{beh};"+
                "we>{wauuhamza};"+
                "w>{wau};"+
                "x>{kaf}{shadda}{seen};"+
                "ye>{yehuhamza};"+
                "ym>{yehmaqsura};"+
                "y>{yeh};"+
                "zz>{zah};"+
                "z>{zain};"+

                "0>\u0660;"+ // Arabic digit 0
                "1>\u0661;"+ // Arabic digit 1
                "2>\u0662;"+ // Arabic digit 2
                "3>\u0663;"+ // Arabic digit 3
                "4>\u0664;"+ // Arabic digit 4
                "5>\u0665;"+ // Arabic digit 5
                "6>\u0666;"+ // Arabic digit 6
                "7>\u0667;"+ // Arabic digit 7
                "8>\u0668;"+ // Arabic digit 8
                "9>\u0669;"+ // Arabic digit 9
                "%>\u066A;"+ // Arabic %
                ".>\u066B;"+ // Arabic decimal separator
                ",>\u066C;"+ // Arabic thousands separator
                "*>\u066D;"+ // Arabic five-pointed star

                "`0>0;"+ // Escaped forms of the above
                "`1>1;"+
                "`2>2;"+
                "`3>3;"+
                "`4>4;"+
                "`5>5;"+
                "`6>6;"+
                "`7>7;"+
                "`8>8;"+
                "`9>9;"+
                "`%>%;"+
                "`.>.;"+
                "`,>,;"+
                "`*>*;"+
                "``>`;"+

                "''>;"+

                // now Arabic to English

                "''ai<a]{alefmadda};"+
                "ai<{alefmadda};"+
                "''ae<a]{alefuhamza};"+
                "ae<{alefuhamza};"+
                "''ao<a]{alefhamza};"+
                "ao<{alefhamza};"+
                "''aa<a]{alef};"+
                "aa<{alef};"+
                "''an<a]{fathatein};"+
                "an<{fathatein};"+
                "''a<a]{fatha};"+
                "a<{fatha};"+
                "b<{beh};"+
                "''dh<d]{dhal};"+
                "dh<{dhal};"+
                "''dd<d]{dad};"+
                "dd<{dad};"+
                "''d<d]{dal};"+
                "d<{dal};"+
                "''e<a]{ein};"+
                "''e<w]{ein};"+
                "''e<y]{ein};"+
                "e<{ein};"+
                "f<{feh};"+
                "gh<{ghein};"+
                "''hh<d]{hah};"+
                "''hh<t]{hah};"+
                "''hh<k]{hah};"+
                "''hh<s]{hah};"+
                "hh<{hah};"+
                "''h<d]{heh};"+
                "''h<t]{heh};"+
                "''h<k]{heh};"+
                "''h<s]{heh};"+
                "h<{heh};"+
                "''ii<i]{kasratein};"+
                "ii<{kasratein};"+
                "''i<i]{kasra};"+
                "i<{kasra};"+
                "j<{geem};"+
                "kh<{kha};"+
                "x<{kaf}{shadda}{seen};"+
                "k<{kaf};"+
                "l<{lam};"+
                "''m<y]{meem};"+
                "''m<t]{meem};"+
                "m<{meem};"+
                "n<{noon};"+
                "''o<a]{hamza};"+
                "o<{hamza};"+
                "p<{peh};"+
                "q<{qaaf};"+
                "r<{reh};"+
                "sh<{sheen};"+
                "''ss<s]{sad};"+
                "ss<{sad};"+
                "''s<s]{seen};"+
                "s<{seen};"+
                "th<{theh};"+
                "tm<{tehmarbuta};"+
                "''tt<t]{tah};"+
                "tt<{tah};"+
                "''t<t]{teh};"+
                "t<{teh};"+
                "''uu<u]{dammatein};"+
                "uu<{dammatein};"+
                "''u<u]{damma};"+
                "u<{damma};"+
                "we<{wauuhamza};"+
                "w<{wau};"+
                "ye<{yehuhamza};"+
                "ym<{yehmaqsura};"+
                "''y<y]{yeh};"+
                "y<{yeh};"+
                "''zz<z]{zah};"+
                "zz<{zah};"+
                "''z<z]{zain};"+
                "z<{zain};"+

                "dh<dh]{shadda};"+
                "dd<dd]{shadda};"+
                "''d<d]{shadda};"
            }
        };
    }
}
