package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$StraightQuotes$CurlyQuotes extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            {   "Rule",
                // Rewritten using character codes [LIU]
                "white=[[:Zs:][:Zl:][:Zp:]];"
                + "black=[^{white}];"
                + "open=[:Ps:];"
                + "dquote=\";"

                + "lAng=\u3008;"
                + "ldAng=\u300A;"
                + "lBrk='[';"
                + "lBrc='{';"

                + "lquote=\u2018;"
                + "rquote=\u2019;"
                + "ldquote=\u201C;"
                + "rdquote=\u201D;"

                + "ldguill=\u00AB;"
                + "rdguill=\u00BB;"
                + "lguill=\u2039;"
                + "rguill=\u203A;"

                + "mdash=\u2014;"

                //#######################################
                // Conversions from input
                //#######################################

                // join single quotes
                + "{lquote}''>{ldquote};"
                + "{lquote}{lquote}>{ldquote};"
                + "{rquote}''>{rdquote};"
                + "{rquote}{rquote}>{rdquote};"

                //smart single quotes
                + "{white})''>{lquote};"
                + "{open})''>{lquote};"
                + "{black})''>{rquote};"
                + "''>{lquote};"

                //smart doubles
                + "{white}){dquote}>{ldquote};"
                + "{open}){dquote}>{ldquote};"
                + "{black}){dquote}>{rdquote};"
                + "{dquote}>{ldquote};"

                // join single guillemets
                + "{rguill}{rguill}>{rdguill};"
                + "'>>'>{rdguill};"
                + "{lguill}{lguill}>{ldguill};"
                + "'<<'>{ldguill};"

                // prevent double spaces
                + "\\ )\\ >;"

                // join hyphens into dash
                + "-->{mdash};"

                //#######################################
                // Conversions back to input
                //#######################################

                //smart quotes
                + "''<{lquote};"
                + "''<{rquote};"
                + "{dquote}<{ldquote};"
                + "{dquote}<{rdquote};"

                //hyphens
                + "--<{mdash};"
            }
        };
    }
}
