/*
 * (C) IBM Corp. 1997-1998.  All Rights Reserved.
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */
package com.ibm.text.resources;

import java.util.ListResourceBundle;

/**
 * RuleBasedNumberFormat data for Swiss French.
 *
 * @author Richard Gillam
 * @version $Version$ $Date: 2000/02/10 06:25:52 $
 */
public class NumberFormatRules_fr_CH extends ListResourceBundle {
    /**
     * Puts a copyright in the .class file
     */
    private static final String copyrightNotice
        = "Copyright \u00a91997-1998 IBM Corp.  All rights reserved.";
    public Object[][] getContents() {
        return contents;
    }

    Object[][] contents = {
        /**
         * Spellout rules for Swiss French.  Swiss French differs from French French
         * in that it does have words for 70, 80, and 90.  This rule set shows them,
         * and is simpler as a result.
         */
        { "SpelloutRules",
            "%main:\n"
            + "    -x: moins >>;\n"
            + "    x.x: << virgule >>;\n"
            + "    z\u00e9ro; un; deux; trois; quatre; cinq; six; sept; huit; neuf;\n"
            + "    dix; onze; douze; treize; quatorze; quinze; seize;\n"
            + "        dix-sept; dix-huit; dix-neuf;\n"
            + "    20: vingt[->%%alt-ones>];\n"
            + "    30: trente[->%%alt-ones>];\n"
            + "    40: quarante[->%%alt-ones>];\n"
            + "    50: cinquante[->%%alt-ones>];\n"
            + "    60: soixante[->%%alt-ones>];\n"
                   // notice new words for 70, 80, and 90
            + "    70: septante[->%%alt-ones>];\n"
            + "    80: octante[->%%alt-ones>];\n"
            + "    90: nonante[->%%alt-ones>];\n"
            + "    100: cent[ >>];\n"
            + "    200: << cents[ >>];\n"
            + "    1000: mille[ >>];\n"
            + "    1100>: onze cents[ >>];\n"
            + "    1200: mille >>;\n"
            + "    2000: << mille[ >>];\n"
            + "    1,000,000: << million[ >>];\n"
            + "    1,000,000,000: << milliarde[ >>];\n"
            + "    1,000,000,000,000: << billion[ >>];\n"
            + "    1,000,000,000,000,000: =#,##0=;\n"
            + "%%alt-ones:\n"
            + "    ; et-un; =%main=;" }
        // again, I'm missing information on negative numbers and decimals for
        // these to rule sets.  Also, I'm not 100% sure about Swiss French.  Is
        // this correct?  Is "onze cents" commonly used for 1,100 in both France
        // and Switzerland?  Can someone fill me in on the rules for the other
        // French-speaking countries?  I've heard conflicting opinions on which
        // version is used in Canada, and I understand there's an alternate set
        // of words for 70, 80, and 90 that is used somewhere, but I don't know
        // what those words are or where they're used.
    };
}
