package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$Latin$Russian extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "xxxxxxxxxxxx" },

            { "Rule",
              // Russian Letters

              "cyA=\u0410\n" +
              "cyBe=\u0411\n" +
              "cyVe=\u0412\n" +
              "cyGe=\u0413\n" +
              "cyDe=\u0414\n" +
              "cyYe=\u0415\n" +
              "cyYo=\u0416\n" +
              "cyZhe=\u0417\n" +
              "cyZe=\u0418\n" +
              "cyYi=\u0419\n" +
              "cyY=\u0419\n" +
              "cyKe=\u041a\n" +
              "cyLe=\u041b\n" +
              "cyMe=\u041c\n" +
              "cyNe=\u041d\n" +
              "cyO=\u041e\n" +
              "cyPe=\u041f\n" +

              "cyRe=\u0420\n" +
              "cySe=\u0421\n" +
              "cyTe=\u0422\n" +
              "cyU=\u0423\n" +
              "cyFe=\u0424\n" +
              "cyKhe=\u0425\n" +
              "cyTse=\u0426\n" +
              "cyChe=\u0427\n" +
              "cyShe=\u0428\n" +
              "cyShche=\u0429\n" +
              "cyHard=\u042a\n" +
              "cyI=\u042b\n" +
              "cySoft=\u042c\n" +
              "cyE=\u042d\n" +
              "cyYu=\u042e\n" +
              "cyYa=\u042f\n" +

              "cya=\u0430\n" +
              "cybe=\u0431\n" +
              "cyve=\u0432\n" +
              "cyge=\u0433\n" +
              "cyde=\u0434\n" +
              "cyye=\u0435\n" +
              "cyzhe=\u0436\n" +
              "cyze=\u0437\n" +
              "cyyi=\u0438\n" +
              "cyy=\u0439\n" +
              "cyke=\u043a\n" +
              "cyle=\u043b\n" +
              "cyme=\u043c\n" +
              "cyne=\u043d\n" +
              "cyo=\u043e\n" +
              "cype=\u043f\n" +

              "cyre=\u0440\n" +
              "cyse=\u0441\n" +
              "cyte=\u0442\n" +
              "cyu=\u0443\n" +
              "cyfe=\u0444\n" +
              "cykhe=\u0445\n" +
              "cytse=\u0446\n" +
              "cyche=\u0447\n" +
              "cyshe=\u0448\n" +
              "cyshche=\u0449\n" +
              "cyhard=\u044a\n" +
              "cyi=\u044b\n" +
              "cysoft=\u044c\n" +
              "cye=\u044d\n" +
              "cyyu=\u044e\n" +
              "cyya=\u044f\n" +

              "cyyo=\u0451\n" +

              "a=[aA]\n" +
              "c=[cC]\n" +
              "e=[eE]\n" +
              "h=[hH]\n" +
              "i=[iI]\n" +
              "o=[oO]\n" +
              "s=[sS]\n" +
              "t=[tT]\n" +
              "u=[uU]\n" +
              "iey=[ieyIEY]\n" +
              "lower=[:Lu:]\n" +

              // convert English to Russian
              "Russian>\u041f\u0420\u0410\u0412\u0414\u0410\u00D1\u0020\u0411\u044d\u043b\u0430\u0440\u0443\u0441\u043a\u0430\u044f\u002c\u0020\u043a\u044b\u0440\u0433\u044b\u0437\u002c\u0020\u041c\u043e\u043b\u0434\u043e\u0432\u044d\u043d\u044f\u0441\u043a\u044d\u002e\n" +

              //special equivs for ay, oy, ...
              "Y{a}{i}>{cyYa}{cyY}\n" +
              "Y{e}{i}>{cyYe}{cyY}\n" +
              "Y{i}{i}>{cyYi}{cyY}\n" +
              "Y{o}{i}>{cyYo}{cyY}\n" +
              "Y{u}{i}>{cyYu}{cyY}\n" +
              "A{i}>{cyA}{cyY}\n" +
              "E{i}>{cyE}{cyY}\n" +
              //skip II, since it is the soft sign
              "O{i}>{cyO}{cyY}\n" +
              "U{i}>{cyU}{cyY}\n" +

              "A>{cyA}\n" +
              "B>{cyBe}\n" +
              "C{h}>{cyChe}\n" +
              "C[{iey}>{cySe}\n" +
              "C>{cyKe}\n" +
              "D>{cyDe}\n" +
              "E>{cyE}\n" +
              "F>{cyFe}\n" +
              "G>{cyGe}\n" +
              "H>{cyHard}\n" +
              "I{i}>{cySoft}\n" +
              "I>{cyI}\n" +
              "J>{cyDe}{cyZhe}\n" +
              "K{h}>{cyKhe}\n" +
              "K>{cyKe}\n" +
              "L>{cyLe}\n" +
              "M>{cyMe}\n" +
              "N>{cyNe}\n" +
              "O>{cyO}\n" +
              "P>{cyPe}\n" +
              "Q{u}>{cyKe}{cyVe}\n" +
              "R>{cyRe}\n" +
              "S{h}{t}{c}{h}>{cyShche}\n" +
              "S{h}{c}{h}>{cyShche}\n" +
              "S{h}>{cyShe}\n" +
              "S>{cySe}\n" +
              "T{c}{h}>{cyChe}\n" +
              "T{h}>{cyZe}\n" +
              "T{s}>{cyTse}\n" +
              "T>{cyTe}\n" +
              "U>{cyU}\n" +
              "V>{cyVe}\n" +
              "W{h}>{cyVe}\n" +
              "W>{cyVe}\n" +
              "X>{cyKe}{cySe}\n" +
              "Y{e}>{cyYe}\n" +
              "Y{o}>{cyYo}\n" +
              "Y{u}>{cyYu}\n" +
              "Y{a}>{cyYa}\n" +
              "Y{i}>{cyYi}\n" +
              "Y>{cyY}\n" +
              "Z{h}>{cyZhe}\n" +
              "Z>{cyZe}\n" +
              "X>{cyKe}{cySe}\n" +

              //lower case: doesn''t solve join bug
              "y{a}{i}>{cyya}{cyy}\n" +
              "y{e}{i}>{cyye}{cyy}\n" +
              "y{i}{i}>{cyyi}{cyy}\n" +
              "y{o}{i}>{cyyo}{cyy}\n" +
              "y{u}{i}>{cyyu}{cyy}\n" +
              "a{i}>{cya}{cyy}\n" +
              "e{i}>{cye}{cyy}\n" +
              //skip ii, since it is the soft sign
              "o{i}>{cyo}{cyy}\n" +
              "u{i}>{cyu}{cyy}\n" +

              "a>{cya}\n" +
              "b>{cybe}\n" +
              "c{h}>{cyche}\n" +
              "c[{iey}>{cyse}\n" +
              "c>{cyke}\n" +
              "d>{cyde}\n" +
              "e>{cye}\n" +
              "f>{cyfe}\n" +
              "g>{cyge}\n" +
              "h>{cyhard}\n" +
              "i{i}>{cysoft}\n" +
              "i>{cyi}\n" +
              "j>{cyde}{cyzhe}\n" +
              "k{h}>{cykhe}\n" +
              "k>{cyke}\n" +
              "l>{cyle}\n" +
              "m>{cyme}\n" +
              "n>{cyne}\n" +
              "o>{cyo}\n" +
              "p>{cype}\n" +
              "q{u}>{cyke}{cyve}\n" +
              "r>{cyre}\n" +
              "s{h}{t}{c}{h}>{cyshche}\n" +
              "s{h}{c}{h}>{cyshche}\n" +
              "s{h}>{cyshe}\n" +
              "s>{cyse}\n" +
              "t{c}{h}>{cyche}\n" +
              "t{h}>{cyze}\n" +
              "t{s}>{cytse}\n" +
              "t>{cyte}\n" +
              "u>{cyu}\n" +
              "v>{cyve}\n" +
              "w{h}>{cyve}\n" +
              "w>{cyve}\n" +
              "x>{cyke}{cyse}\n" +
              "y{e}>{cyye}\n" +
              "y{o}>{cyyo}\n" +
              "y{u}>{cyyu}\n" +
              "y{a}>{cyya}\n" +
              "y{i}>{cyyi}\n" +
              "y>{cyy}\n" +
              "z{h}>{cyzhe}\n" +
              "z>{cyze}\n" +
              "x>{cyke}{cyse}\n" +

              //generally the last rule
              "''>\n" +

              //now Russian to English

              "Y''<{cyY}[{cyA}\n" +
              "Y''<{cyY}[{cyE}\n" +
              "Y''<{cyY}[{cyI}\n" +
              "Y''<{cyY}[{cyO}\n" +
              "Y''<{cyY}[{cyU}\n" +
              "Y''<{cyY}[{cya}\n" +
              "Y''<{cyY}[{cye}\n" +
              "Y''<{cyY}[{cyi}\n" +
              "Y''<{cyY}[{cyo}\n" +
              "Y''<{cyY}[{cyu}\n" +
              "A<{cyA}\n" +
              "B<{cyBe}\n" +
              "J<{cyDe}{cyZhe}\n" +
              "J<{cyDe}{cyzhe}\n" +
              "D<{cyDe}\n" +
              "V<{cyVe}\n" +
              "G<{cyGe}\n" +
              "Zh<{cyZhe}[{lower}\n" +
              "ZH<{cyZhe}\n" +
              "Z''<{cyZe}[{cyHard}\n" +
              "Z''<{cyZe}[{cyhard}\n" +
              "Z<{cyZe}\n" +
              "Ye<{cyYe}[{lower}\n" +
              "YE<{cyYe}\n" +
              "Yo<{cyYo}[{lower}\n" +
              "YO<{cyYo}\n" +
              "Yu<{cyYu}[{lower}\n" +
              "YU<{cyYu}\n" +
              "Ya<{cyYa}[{lower}\n" +
              "YA<{cyYa}\n" +
              "Yi<{cyYi}[{lower}\n" +
              "YI<{cyYi}\n" +
              "Y<{cyY}\n" +
              "Kh<{cyKhe}[{lower}\n" +
              "KH<{cyKhe}\n" +
              "K''<{cyKe}[{cyHard}\n" +
              "K''<{cyKe}[{cyhard}\n" +
              "X<{cyKe}{cySe}\n" +
              "X<{cyKe}{cyse}\n" +
              "K<{cyKe}\n" +
              "L<{cyLe}\n" +
              "M<{cyMe}\n" +
              "N<{cyNe}\n" +
              "O<{cyO}\n" +
              "P<{cyPe}\n" +

              "R<{cyRe}\n" +
              "Shch<{cyShche}[{lower}\n" +
              "SHCH<{cyShche}\n" +
              "Sh''<{cyShe}[{cyche}\n" +
              "SH''<{cyShe}[{cyChe}\n" +
              "Sh<{cyShe}[{lower}\n" +
              "SH<{cyShe}\n" +
              "S''<{cySe}[{cyHard}\n" +
              "S''<{cySe}[{cyhard}\n" +
              "S<{cySe}\n" +
              "Ts<{cyTse}[{lower}\n" +
              "TS<{cyTse}\n" +
              "T''<{cyTe}[{cySe}\n" +
              "T''<{cyTe}[{cyse}\n" +
              "T''<{cyTe}[{cyHard}\n" +
              "T''<{cyTe}[{cyhard}\n" +
              "T<{cyTe}\n" +
              "U<{cyU}\n" +
              "F<{cyFe}\n" +
              "Ch<{cyChe}[{lower}\n" +
              "CH<{cyChe}\n" +
              "H<{cyHard}\n" +
              "I''<{cyI}[{cyI}\n" +
              "I''<{cyI}[{cyi}\n" +
              "I<{cyI}\n" +
              "Ii<{cySoft}[{lower}\n" +
              "II<{cySoft}\n" +
              "E<{cyE}\n" +

              //lowercase
              "y''<{cyy}[{cya}\n" +
              "y''<{cyy}[{cye}\n" +
              "y''<{cyy}[{cyi}\n" +
              "y''<{cyy}[{cyo}\n" +
              "y''<{cyy}[{cyu}\n" +
              "y''<{cyy}[{cyA}\n" +
              "y''<{cyy}[{cyE}\n" +
              "y''<{cyy}[{cyI}\n" +
              "y''<{cyy}[{cyO}\n" +
              "y''<{cyy}[{cyU}\n" +
              "a<{cya}\n" +
              "b<{cybe}\n" +
              "j<{cyde}{cyzhe}\n" +
              "j<{cyde}{cyZhe}\n" +
              "d<{cyde}\n" +
              "v<{cyve}\n" +
              "g<{cyge}\n" +
              "zh<{cyzhe}\n" +
              "z''<{cyze}[{cyhard}\n" +
              "z''<{cyze}[{cyHard}\n" +
              "z<{cyze}\n" +
              "ye<{cyye}\n" +
              "yo<{cyyo}\n" +
              "yu<{cyyu}\n" +
              "ya<{cyya}\n" +
              "yi<{cyyi}\n" +
              "y<{cyy}\n" +
              "kh<{cykhe}\n" +
              "k''<{cyke}[{cyhard}\n" +
              "k''<{cyke}[{cyHard}\n" +
              "x<{cyke}{cyse}\n" +
              "x<{cyke}{cySe}\n" +
              "k<{cyke}\n" +
              "l<{cyle}\n" +
              "m<{cyme}\n" +
              "n<{cyne}\n" +
              "o<{cyo}\n" +
              "p<{cype}\n" +

              "r<{cyre}\n" +
              "shch<{cyshche}\n" +
              "sh''<{cyshe}[{cyche}\n" +
              "sh''<{cyshe}[{cyChe}\n" +
              "sh<{cyshe}\n" +
              "s''<{cyse}[{cyhard}\n" +
              "s''<{cyse}[{cyHard}\n" +
              "s<{cyse}\n" +
              "ts<{cytse}\n" +
              "t''<{cyte}[{cyse}\n" +
              "t''<{cyte}[{cySe}\n" +
              "t''<{cyte}[{cyhard}\n" +
              "t''<{cyte}[{cyHard}\n" +
              "t<{cyte}\n" +
              "u<{cyu}\n" +
              "f<{cyfe}\n" +
              "ch<{cyche}\n" +
              "h<{cyhard}\n" +
              "i''<{cyi}[{cyI}\n" +
              "i''<{cyi}[{cyi}\n" +
              "i<{cyi}\n" +
              "ii<{cysoft}\n" +
              "e<{cye}\n" +

              //generally the last rule
              "''>\n"
              //the end
            }
        };
    }
}
