package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRuleLatinArabic extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "HasInverse", "1" },

            { "Rule",
                // To Do: finish adding shadda, add sokoon

                "alefmadda=\u0622\n"+
                "alefuhamza=\u0623\n"+
                "wauuhamza=\u0624\n"+
                "alefhamza=\u0625\n"+
                "yehuhamza=\u0626\n"+
                "alef=\u0627\n"+
                "beh=\u0628\n"+
                "tehmarbuta=\u0629\n"+
                "teh=\u062A\n"+
                "theh=\u062B\n"+
                "geem=\u062C\n"+
                "hah=\u062D\n"+
                "kha=\u062E\n"+
                "dal=\u062F\n"+
                "dhal=\u0630\n"+
                "reh=\u0631\n"+
                "zain=\u0632\n"+
                "seen=\u0633\n"+
                "sheen=\u0634\n"+
                "sad=\u0635\n"+
                "dad=\u0636\n"+
                "tah=\u0637\n"+
                "zah=\u0638\n"+
                "ein=\u0639\n"+
                "ghein=\u063A\n"+
                "feh=\u0641\n"+
                "qaaf=\u0642\n"+
                "kaf=\u0643\n"+
                "lam=\u0644\n"+
                "meem=\u0645\n"+
                "noon=\u0646\n"+
                "heh=\u0647\n"+
                "wau=\u0648\n"+
                "yehmaqsura=\u0649\n"+
                "yeh=\u064A\n"+
                "peh=\u06A4\n"+

                "hamza=\u0621\n"+
                "fathatein=\u064B\n"+
                "dammatein=\u064C\n"+
                "kasratein=\u064D\n"+
                "fatha=\u064E\n"+
                "damma=\u064F\n"+
                "kasra=\u0650\n"+
                "shadda=\u0651\n"+
                "sokoon=\u0652\n"+

                // convert English to Arabic
                "Arabic>"+
                "\u062a\u062a\u0645\u062a\u0639\u0020"+
                "\u0627\u0644\u0644\u063a\u0629\u0020"+
                "\u0627\u0644\u0639\u0631\u0628\u0628\u064a\u0629\u0020"+
                "\u0628\u0628\u0646\u0638\u0645\u0020"+
                "\u0643\u062a\u0627\u0628\u0628\u064a\u0629\u0020"+
                "\u062c\u0645\u064a\u0644\u0629\n"+

                "ai>{alefmadda}\n"+
                "ae>{alefuhamza}\n"+
                "ao>{alefhamza}\n"+
                "aa>{alef}\n"+
                "an>{fathatein}\n"+
                "a>{fatha}\n"+
                "b>{beh}\n"+
                "c>{kaf}\n"+
                "{dhal}]dh>{shadda}\n"+
                "dh>{dhal}\n"+
                "{dad}]dd>{shadda}\n"+
                "dd>{dad}\n"+
                "{dal}]d>{shadda}\n"+
                "d>{dal}\n"+
                "e>{ein}\n"+
                "f>{feh}\n"+
                "gh>{ghein}\n"+
                "g>{geem}\n"+
                "hh>{hah}\n"+
                "h>{heh}\n"+
                "ii>{kasratein}\n"+
                "i>{kasra}\n"+
                "j>{geem}\n"+
                "kh>{kha}\n"+
                "k>{kaf}\n"+
                "l>{lam}\n"+
                "m>{meem}\n"+
                "n>{noon}\n"+
                "o>{hamza}\n"+
                "p>{peh}\n"+
                "q>{qaaf}\n"+
                "r>{reh}\n"+
                "sh>{sheen}\n"+
                "ss>{sad}\n"+
                "s>{seen}\n"+
                "th>{theh}\n"+
                "tm>{tehmarbuta}\n"+
                "tt>{tah}\n"+
                "t>{teh}\n"+
                "uu>{dammatein}\n"+
                "u>{damma}\n"+
                "v>{beh}\n"+
                "we>{wauuhamza}\n"+
                "w>{wau}\n"+
                "x>{kaf}{shadda}{seen}\n"+
                "ye>{yehuhamza}\n"+
                "ym>{yehmaqsura}\n"+
                "y>{yeh}\n"+
                "zz>{zah}\n"+
                "z>{zain}\n"+

                "0>\u0660\n"+ // Arabic digit 0
                "1>\u0661\n"+ // Arabic digit 1
                "2>\u0662\n"+ // Arabic digit 2
                "3>\u0663\n"+ // Arabic digit 3
                "4>\u0664\n"+ // Arabic digit 4
                "5>\u0665\n"+ // Arabic digit 5
                "6>\u0666\n"+ // Arabic digit 6
                "7>\u0667\n"+ // Arabic digit 7
                "8>\u0668\n"+ // Arabic digit 8
                "9>\u0669\n"+ // Arabic digit 9
                "%>\u066A\n"+ // Arabic %
                ".>\u066B\n"+ // Arabic decimal separator
                ",>\u066C\n"+ // Arabic thousands separator
                "*>\u066D\n"+ // Arabic five-pointed star

                "`0>0\n"+ // Escaped forms of the above
                "`1>1\n"+
                "`2>2\n"+
                "`3>3\n"+
                "`4>4\n"+
                "`5>5\n"+
                "`6>6\n"+
                "`7>7\n"+
                "`8>8\n"+
                "`9>9\n"+
                "`%>%\n"+
                "`.>.\n"+
                "`,>,\n"+
                "`*>*\n"+
                "``>`\n"+

                "''>\n"+

                // now Arabic to English

                "''ai<a]{alefmadda}\n"+
                "ai<{alefmadda}\n"+
                "''ae<a]{alefuhamza}\n"+
                "ae<{alefuhamza}\n"+
                "''ao<a]{alefhamza}\n"+
                "ao<{alefhamza}\n"+
                "''aa<a]{alef}\n"+
                "aa<{alef}\n"+
                "''an<a]{fathatein}\n"+
                "an<{fathatein}\n"+
                "''a<a]{fatha}\n"+
                "a<{fatha}\n"+
                "b<{beh}\n"+
                "''dh<d]{dhal}\n"+
                "dh<{dhal}\n"+
                "''dd<d]{dad}\n"+
                "dd<{dad}\n"+
                "''d<d]{dal}\n"+
                "d<{dal}\n"+
                "''e<a]{ein}\n"+
                "''e<w]{ein}\n"+
                "''e<y]{ein}\n"+
                "e<{ein}\n"+
                "f<{feh}\n"+
                "gh<{ghein}\n"+
                "''hh<d]{hah}\n"+
                "''hh<t]{hah}\n"+
                "''hh<k]{hah}\n"+
                "''hh<s]{hah}\n"+
                "hh<{hah}\n"+
                "''h<d]{heh}\n"+
                "''h<t]{heh}\n"+
                "''h<k]{heh}\n"+
                "''h<s]{heh}\n"+
                "h<{heh}\n"+
                "''ii<i]{kasratein}\n"+
                "ii<{kasratein}\n"+
                "''i<i]{kasra}\n"+
                "i<{kasra}\n"+
                "j<{geem}\n"+
                "kh<{kha}\n"+
                "x<{kaf}{shadda}{seen}\n"+
                "k<{kaf}\n"+
                "l<{lam}\n"+
                "''m<y]{meem}\n"+
                "''m<t]{meem}\n"+
                "m<{meem}\n"+
                "n<{noon}\n"+
                "''o<a]{hamza}\n"+
                "o<{hamza}\n"+
                "p<{peh}\n"+
                "q<{qaaf}\n"+
                "r<{reh}\n"+
                "sh<{sheen}\n"+
                "''ss<s]{sad}\n"+
                "ss<{sad}\n"+
                "''s<s]{seen}\n"+
                "s<{seen}\n"+
                "th<{theh}\n"+
                "tm<{tehmarbuta}\n"+
                "''tt<t]{tah}\n"+
                "tt<{tah}\n"+
                "''t<t]{teh}\n"+
                "t<{teh}\n"+
                "''uu<u]{dammatein}\n"+
                "uu<{dammatein}\n"+
                "''u<u]{damma}\n"+
                "u<{damma}\n"+
                "we<{wauuhamza}\n"+
                "w<{wau}\n"+
                "ye<{yehuhamza}\n"+
                "ym<{yehmaqsura}\n"+
                "''y<y]{yeh}\n"+
                "y<{yeh}\n"+
                "''zz<z]{zah}\n"+
                "zz<{zah}\n"+
                "''z<z]{zain}\n"+
                "z<{zain}\n"+

                "dh<dh]{shadda}\n"+
                "dd<dd]{shadda}\n"+
                "''d<d]{shadda}\n"
            }
        };
    }
}
