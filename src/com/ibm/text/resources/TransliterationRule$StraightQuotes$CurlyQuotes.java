package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRuleStraightQuotesCurlyQuotes extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Use left and right double quotes" },

            {   "Rule",
                // Rewritten using character codes [LIU]
                "white=[[:Zs:][:Zl:][:Zp:]]\n"
                + "black=[^[:Zs:][:Zl:][:Zp:]]\n"
                + "open=[[:Ps:]]\n"
                + "dquote=\"\n"

                + "lAng=\u3008\n"
                + "ldAng=\u300A\n"
                + "lBrk='['\n"
                + "lBrc='{'\n"

                + "lquote=\u2018\n"
                + "rquote=\u2019\n"
                + "ldquote=\u201C\n"
                + "rdquote=\u201D\n"

                + "ldguill=\u00AB\n"
                + "rdguill=\u00BB\n"
                + "lguill=\u2039\n"
                + "rguill=\u203A\n"

                + "mdash=\u2014\n"

                //#######################################
                // Conversions from input
                //#######################################

                // join single quotes
                + "{lquote}''>{ldquote}\n"
                + "{lquote}{lquote}>{ldquote}\n"
                + "{rquote}''>{rdquote}\n"
                + "{rquote}{rquote}>{rdquote}\n"

                //smart single quotes
                + "{white}]''>{lquote}\n"
                + "{open}]''>{lquote}\n"
                + "{black}]''>{rquote}\n"
                + "''>{lquote}\n"

                //smart doubles
                + "{white}]{dquote}>{ldquote}\n"
                + "{open}]{dquote}>{ldquote}\n"
                + "{black}]{dquote}>{rdquote}\n"
                + "{dquote}>{ldquote}\n"

                // join single guillemets
                + "{rguill}{rguill}>{rdguill}\n"
                + "'>>'>{rdguill}\n"
                + "{lguill}{lguill}>{ldguill}\n"
                + "'<<'>{ldguill}\n"

                // prevent double spaces
                + " ] >\n"

                // join hyphens into dash
                + "-->{mdash}\n"

                //#######################################
                // Conversions back to input
                //#######################################

                //smart quotes
                + "''<{lquote}\n"
                + "''<{rquote}\n"
                + "{dquote}<{ldquote}\n"
                + "{dquote}<{rdquote}\n"

                //hyphens
                + "--<{mdash}\n"
            }
        };
    }
}
