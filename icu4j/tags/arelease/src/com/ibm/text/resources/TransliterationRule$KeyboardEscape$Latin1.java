package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRuleKeyboardEscapeLatin1 extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Keyboard transliterator for Latin-1 block" },

            { "Rule",
                "esc=''\n"
                + "grave=`\n"
                + "acute=''\n"
                + "hat=^\n"
                + "tilde=~\n"
                + "umlaut=:\n"
                + "ring=.\n"
                + "cedilla=,\n"
                + "slash=/\n"
                + "super=^\n"

                // Make keyboard entry of {esc} possible
                // and of backslash
                + "'\\'{esc}>{esc}\n"
                + "'\\\\'>'\\'\n"
              
                // Long keys
                + "cur{esc}>\u00A4\n"
                + "sec{esc}>\u00A7\n"
                + "not{esc}>\u00AC\n"
                + "mul{esc}>\u00D7\n"
                + "div{esc}>\u00F7\n"

                + " {esc}>\u00A0\n" // non-breaking space
                + "!{esc}>\u00A1\n" // inverted exclamation
                + "c/{esc}>\u00A2\n" // cent sign
                + "lb{esc}>\u00A3\n" // pound sign
                + "'|'{esc}>\u00A6\n" // broken vertical bar
                + ":{esc}>\u00A8\n" // umlaut
                + "{super}a{esc}>\u00AA\n" // feminine ordinal
                + "'<<'{esc}>\u00AB\n"
                + "r{esc}>\u00AE\n"
                + "--{esc}>\u00AF\n"
                + "-{esc}>\u00AD\n"
                + "+-{esc}>\u00B1\n"
                + "{super}2{esc}>\u00B2\n"
                + "{super}3{esc}>\u00B3\n"
                + "{acute}{esc}>\u00B4\n"
                + "m{esc}>\u00B5\n"
                + "para{esc}>\u00B6\n"
                + "dot{esc}>\u00B7\n"
                + "{cedilla}{esc}>\u00B8\n"
                + "{super}1{esc}>\u00B9\n"
                + "{super}o{esc}>\u00BA\n" // masculine ordinal
                + "'>>'{esc}>\u00BB\n"
                + "1/4{esc}>\u00BC\n"
                + "1/2{esc}>\u00BD\n"
                + "3/4{esc}>\u00BE\n"
                + "?{esc}>\u00BF\n"
                + "A{grave}{esc}>\u00C0\n"
                + "A{acute}{esc}>\u00C1\n"
                + "A{hat}{esc}>\u00C2\n"
                + "A{tilde}{esc}>\u00C3\n"
                + "A{umlaut}{esc}>\u00C4\n"
                + "A{ring}{esc}>\u00C5\n"
                + "AE{esc}>\u00C6\n"
                + "C{cedilla}{esc}>\u00C7\n"
                + "E{grave}{esc}>\u00C8\n"
                + "E{acute}{esc}>\u00C9\n"
                + "E{hat}{esc}>\u00CA\n"
                + "E{umlaut}{esc}>\u00CB\n"
                + "I{grave}{esc}>\u00CC\n"
                + "I{acute}{esc}>\u00CD\n"
                + "I{hat}{esc}>\u00CE\n"
                + "I{umlaut}{esc}>\u00CF\n"
                + "D-{esc}>\u00D0\n"
                + "N{tilde}{esc}>\u00D1\n"
                + "O{grave}{esc}>\u00D2\n"
                + "O{acute}{esc}>\u00D3\n"
                + "O{hat}{esc}>\u00D4\n"
                + "O{tilde}{esc}>\u00D5\n"
                + "O{umlaut}{esc}>\u00D6\n"
                + "O{slash}{esc}>\u00D8\n"
                + "U{grave}{esc}>\u00D9\n"
                + "U{acute}{esc}>\u00DA\n"
                + "U{hat}{esc}>\u00DB\n"
                + "U{umlaut}{esc}>\u00DC\n"
                + "Y{acute}{esc}>\u00DD\n"
                + "TH{esc}>\u00DE\n"
                + "ss{esc}>\u00DF\n"
                + "a{grave}{esc}>\u00E0\n"
                + "a{acute}{esc}>\u00E1\n"
                + "a{hat}{esc}>\u00E2\n"
                + "a{tilde}{esc}>\u00E3\n"
                + "a{umlaut}{esc}>\u00E4\n"
                + "a{ring}{esc}>\u00E5\n"
                + "ae{esc}>\u00E6\n"
                + "c{cedilla}{esc}>\u00E7\n"
                + "c{esc}>\u00A9\n" // copyright - after c{cedilla}
                + "e{grave}{esc}>\u00E8\n"
                + "e{acute}{esc}>\u00E9\n"
                + "e{hat}{esc}>\u00EA\n"
                + "e{umlaut}{esc}>\u00EB\n"
                + "i{grave}{esc}>\u00EC\n"
                + "i{acute}{esc}>\u00ED\n"
                + "i{hat}{esc}>\u00EE\n"
                + "i{umlaut}{esc}>\u00EF\n"
                + "d-{esc}>\u00F0\n"
                + "n{tilde}{esc}>\u00F1\n"
                + "o{grave}{esc}>\u00F2\n"
                + "o{acute}{esc}>\u00F3\n"
                + "o{hat}{esc}>\u00F4\n"
                + "o{tilde}{esc}>\u00F5\n"
                + "o{umlaut}{esc}>\u00F6\n"
                + "o{slash}{esc}>\u00F8\n"
                + "o{esc}>\u00B0\n"
                + "u{grave}{esc}>\u00F9\n"
                + "u{acute}{esc}>\u00FA\n"
                + "u{hat}{esc}>\u00FB\n"
                + "u{umlaut}{esc}>\u00FC\n"
                + "y{acute}{esc}>\u00FD\n"
                + "y{esc}>\u00A5\n" // yen sign
                + "th{esc}>\u00FE\n"
                + "ss{esc}>\u00FF\n"
            }
        };
    }
}
