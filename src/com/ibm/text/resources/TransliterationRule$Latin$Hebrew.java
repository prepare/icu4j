package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$Latin$Hebrew extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Latin to Hebrew" },

            { "Rule",
                //variable names, derived from the Unicode names.

                "POINT_SHEVA=\u05B0\n"
                + "POINT_HATAF_SEGOL=\u05B1\n"
                + "POINT_HATAF_PATAH=\u05B2\n"
                + "POINT_HATAF_QAMATS=\u05B3\n"
                + "POINT_HIRIQ=\u05B4\n"
                + "POINT_TSERE=\u05B5\n"
                + "POINT_SEGOL=\u05B6\n"
                + "POINT_PATAH=\u05B7\n"
                + "POINT_QAMATS=\u05B8\n"
                + "POINT_HOLAM=\u05B9\n"
                + "POINT_QUBUTS=\u05BB\n"
                + "POINT_DAGESH_OR_MAPIQ=\u05BC\n"
                + "POINT_METEG=\u05BD\n"
                + "PUNCTUATION_MAQAF=\u05BE\n"
                + "POINT_RAFE=\u05BF\n"
                + "PUNCTUATION_PASEQ=\u05C0\n"
                + "POINT_SHIN_DOT=\u05C1\n"
                + "POINT_SIN_DOT=\u05C2\n"
                + "PUNCTUATION_SOF_PASUQ=\u05C3\n"
                + "ALEF=\u05D0\n"
                + "BET=\u05D1\n"
                + "GIMEL=\u05D2\n"
                + "DALET=\u05D3\n"
                + "HE=\u05D4\n"
                + "VAV=\u05D5\n"
                + "ZAYIN=\u05D6\n"
                + "HET=\u05D7\n"
                + "TET=\u05D8\n"
                + "YOD=\u05D9\n"
                + "FINAL_KAF=\u05DA\n"
                + "KAF=\u05DB\n"
                + "LAMED=\u05DC\n"
                + "FINAL_MEM=\u05DD\n"
                + "MEM=\u05DE\n"
                + "FINAL_NUN=\u05DF\n"
                + "NUN=\u05E0\n"
                + "SAMEKH=\u05E1\n"
                + "AYIN=\u05E2\n"
                + "FINAL_PE=\u05E3\n"
                + "PE=\u05E4\n"
                + "FINAL_TSADI=\u05E5\n"
                + "TSADI=\u05E6\n"
                + "QOF=\u05E7\n"
                + "RESH=\u05E8\n"
                + "SHIN=\u05E9\n"
                + "TAV=\u05EA\n"
                + "YIDDISH_DOUBLE_VAV=\u05F0\n"
                + "YIDDISH_VAV_YOD=\u05F1\n"
                + "YIDDISH_DOUBLE_YOD=\u05F2\n"
                + "PUNCTUATION_GERESH=\u05F3\n"
                + "PUNCTUATION_GERSHAYIM=\u05F4\n"

                //wildcards
                //The values can be anything we don't use in this file: start at E000.

                + "letter=[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ]\n"

                + "softvowel=[eiyEIY]\n"

                + "vowellike=[{ALEF}{AYIN}{YOD}{VAV}]\n"

                //?>{POINT_SHEVA}
                //?>{POINT_HATAF_SEGOL}
                //?>{POINT_HATAF_PATAH}
                //?>{POINT_HATAF_QAMATS}
                //?>{POINT_HIRIQ}
                //?>{POINT_TSERE}
                //?>{POINT_SEGOL}
                //?>{POINT_PATAH}
                //?>{POINT_QAMATS}
                //?>{POINT_HOLAM}
                //?>{POINT_QUBUTS}
                //?>{POINT_DAGESH_OR_MAPIQ}
                //?>{POINT_METEG}
                //?>{PUNCTUATION_MAQAF}
                //?>{POINT_RAFE}
                //?>{PUNCTUATION_PASEQ}
                //?>{POINT_SHIN_DOT}
                //?>{POINT_SIN_DOT}
                //?>{PUNCTUATION_SOF_PASUQ}

                + "a>{ALEF}\n"
                + "A>{ALEF}\n"

                + "b>{BET}\n"
                + "B>{BET}\n"

                + "c[{softvowel}>{SAMEKH}\n"
                + "C[{softvowel}>{SAMEKH}\n"
                + "c[{letter}>{KAF}\n"
                + "C[{letter}>{KAF}\n"
                + "c>{FINAL_KAF}\n"
                + "C>{FINAL_KAF}\n"

                + "d>{DALET}\n"
                + "D>{DALET}\n"

                + "e>{AYIN}\n"
                + "E>{AYIN}\n"

                + "f[{letter}>{PE}\n"
                + "f>{FINAL_PE}\n"
                + "F[{letter}>{PE}\n"
                + "F>{FINAL_PE}\n"

                + "g>{GIMEL}\n"
                + "G>{GIMEL}\n"

                + "h>{HE}\n"
                + "H>{HE}\n"

                + "i>{YOD}\n"
                + "I>{YOD}\n"

                + "j>{DALET}{SHIN}\n"
                + "J>{DALET}{SHIN}\n"

                + "kH>{HET}\n"
                + "kh>{HET}\n"
                + "KH>{HET}\n"
                + "Kh>{HET}\n"
                + "k[{letter}>{KAF}\n"
                + "K[{letter}>{KAF}\n"
                + "k>{FINAL_KAF}\n"
                + "K>{FINAL_KAF}\n"

                + "l>{LAMED}\n"
                + "L>{LAMED}\n"

                + "m[{letter}>{MEM}\n"
                + "m>{FINAL_MEM}\n"
                + "M[{letter}>{MEM}\n"
                + "M>{FINAL_MEM}\n"

                + "n[{letter}>{NUN}\n"
                + "n>{FINAL_NUN}\n"
                + "N[{letter}>{NUN}\n"
                + "N>{FINAL_NUN}\n"

                + "o>{VAV}\n"
                + "O>{VAV}\n"

                + "p[{letter}>{PE}\n"
                + "p>{FINAL_PE}\n"
                + "P[{letter}>{PE}\n"
                + "P>{FINAL_PE}\n"

                + "q>{QOF}\n"
                + "Q>{QOF}\n"

                + "r>{RESH}\n"
                + "R>{RESH}\n"

                + "sH>{SHIN}\n"
                + "sh>{SHIN}\n"
                + "SH>{SHIN}\n"
                + "Sh>{SHIN}\n"
                + "s>{SAMEKH}\n"
                + "S>{SAMEKH}\n"

                + "th>{TAV}\n"
                + "tH>{TAV}\n"
                + "TH>{TAV}\n"
                + "Th>{TAV}\n"
                + "tS[{letter}>{TSADI}\n"
                + "ts[{letter}>{TSADI}\n"
                + "Ts[{letter}>{TSADI}\n"
                + "TS[{letter}>{TSADI}\n"
                + "tS>{FINAL_TSADI}\n"
                + "ts>{FINAL_TSADI}\n"
                + "Ts>{FINAL_TSADI}\n"
                + "TS>{FINAL_TSADI}\n"
                + "t>{TET}\n"
                + "T>{TET}\n"

                + "u>{VAV}\n"
                + "U>{VAV}\n"

                + "v>{VAV}\n"
                + "V>{VAV}\n"

                + "w>{VAV}\n"
                + "W>{VAV}\n"

                + "x>{KAF}{SAMEKH}\n"
                + "X>{KAF}{SAMEKH}\n"

                + "y>{YOD}\n"
                + "Y>{YOD}\n"

                + "z>{ZAYIN}\n"
                + "Z>{ZAYIN}\n"

                //#?>{YIDDISH_DOUBLE_VAV}
                //?>{YIDDISH_VAV_YOD}
                //?>{YIDDISH_DOUBLE_YOD}
                //?>{PUNCTUATION_GERESH}
                //?>{PUNCTUATION_GERSHAYIM}

                + "''>\n"

                //{POINT_SHEVA}>@
                //{POINT_HATAF_SEGOL}>@
                //{POINT_HATAF_PATAH}>@
                //{POINT_HATAF_QAMATS}>@
                //{POINT_HIRIQ}>@
                //{POINT_TSERE}>@
                //{POINT_SEGOL}>@
                //{POINT_PATAH}>@
                //{POINT_QAMATS}>@
                //{POINT_HOLAM}>@
                //{POINT_QUBUTS}>@
                //{POINT_DAGESH_OR_MAPIQ}>@
                //{POINT_METEG}>@
                //{PUNCTUATION_MAQAF}>@
                //{POINT_RAFE}>@
                //{PUNCTUATION_PASEQ}>@
                //{POINT_SHIN_DOT}>@
                //{POINT_SIN_DOT}>@
                //{PUNCTUATION_SOF_PASUQ}>@

                + "a<{ALEF}\n"
                + "e<{AYIN}\n"
                + "b<{BET}\n"
                + "d<{DALET}\n"
                + "k<{FINAL_KAF}\n"
                + "m<{FINAL_MEM}\n"
                + "n<{FINAL_NUN}\n"
                + "p<{FINAL_PE}\n"
                + "ts<{FINAL_TSADI}\n"
                + "g<{GIMEL}\n"
                + "kh<{HET}\n"
                + "h<{HE}\n"
                + "k''<{KAF}[{HE}\n"
                + "k<{KAF}\n"
                + "l<{LAMED}\n"
                + "m<{MEM}\n"
                + "n<{NUN}\n"
                + "p<{PE}\n"
                + "q<{QOF}\n"
                + "r<{RESH}\n"
                + "s''<{SAMEKH}[{HE}\n"
                + "s<{SAMEKH}\n"
                + "sh<{SHIN}\n"
                + "th<{TAV}\n"
                + "t''<{TET}[{HE}\n"
                + "t''<{TET}[{HE}\n"
                + "t''<{TET}[{SAMEKH}\n"
                + "t''<{TET}[{SHIN}\n"
                + "t<{TET}\n"
                + "ts<{TSADI}\n"
                + "v<{VAV}[{vowellike}\n"
                + "u<{VAV}\n"
                + "y<{YOD}\n"
                + "z<{ZAYIN}\n"

                //{YIDDISH_DOUBLE_VAV}>@
                //{YIDDISH_VAV_YOD}>@
                //{YIDDISH_DOUBLE_YOD}>@
                //{PUNCTUATION_GERESH}>@
                //{PUNCTUATION_GERSHAYIM}>@

                + "<''\n"
            }
        };
    }
}
