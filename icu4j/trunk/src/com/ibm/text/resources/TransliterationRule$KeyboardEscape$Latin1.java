package com.ibm.text.resources;

import java.util.ListResourceBundle;

public class TransliterationRule$KeyboardEscape$Latin1 extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Keyboard transliterator for Latin-1 block" },

            { "Rule",
                "esc='';"
                + "grave=`;"
                + "acute='';"
                + "hat=^;"
                + "tilde=~;"
                + "umlaut=:;"
                + "ring=.;"
                + "cedilla=,;"
                + "slash=/;"
                + "super=^;"

                // Make keyboard entry of {esc} possible
                // and of backslash
                + "'\\'{esc}>{esc};"
                + "'\\\\'>'\\';"
              
                // Long keys
                + "cur{esc}>\u00A4;"
                + "sec{esc}>\u00A7;"
                + "not{esc}>\u00AC;"
                + "mul{esc}>\u00D7;"
                + "div{esc}>\u00F7;"

                + " {esc}>\u00A0;" // non-breaking space
                + "!{esc}>\u00A1;" // inverted exclamation
                + "c/{esc}>\u00A2;" // cent sign
                + "lb{esc}>\u00A3;" // pound sign
                + "'|'{esc}>\u00A6;" // broken vertical bar
                + ":{esc}>\u00A8;" // umlaut
                + "{super}a{esc}>\u00AA;" // feminine ordinal
                + "'<<'{esc}>\u00AB;"
                + "r{esc}>\u00AE;"
                + "--{esc}>\u00AF;"
                + "-{esc}>\u00AD;"
                + "+-{esc}>\u00B1;"
                + "{super}2{esc}>\u00B2;"
                + "{super}3{esc}>\u00B3;"
                + "{acute}{esc}>\u00B4;"
                + "m{esc}>\u00B5;"
                + "para{esc}>\u00B6;"
                + "dot{esc}>\u00B7;"
                + "{cedilla}{esc}>\u00B8;"
                + "{super}1{esc}>\u00B9;"
                + "{super}o{esc}>\u00BA;" // masculine ordinal
                + "'>>'{esc}>\u00BB;"
                + "1/4{esc}>\u00BC;"
                + "1/2{esc}>\u00BD;"
                + "3/4{esc}>\u00BE;"
                + "?{esc}>\u00BF;"
                + "A{grave}{esc}>\u00C0;"
                + "A{acute}{esc}>\u00C1;"
                + "A{hat}{esc}>\u00C2;"
                + "A{tilde}{esc}>\u00C3;"
                + "A{umlaut}{esc}>\u00C4;"
                + "A{ring}{esc}>\u00C5;"
                + "AE{esc}>\u00C6;"
                + "C{cedilla}{esc}>\u00C7;"
                + "E{grave}{esc}>\u00C8;"
                + "E{acute}{esc}>\u00C9;"
                + "E{hat}{esc}>\u00CA;"
                + "E{umlaut}{esc}>\u00CB;"
                + "I{grave}{esc}>\u00CC;"
                + "I{acute}{esc}>\u00CD;"
                + "I{hat}{esc}>\u00CE;"
                + "I{umlaut}{esc}>\u00CF;"
                + "D-{esc}>\u00D0;"
                + "N{tilde}{esc}>\u00D1;"
                + "O{grave}{esc}>\u00D2;"
                + "O{acute}{esc}>\u00D3;"
                + "O{hat}{esc}>\u00D4;"
                + "O{tilde}{esc}>\u00D5;"
                + "O{umlaut}{esc}>\u00D6;"
                + "O{slash}{esc}>\u00D8;"
                + "U{grave}{esc}>\u00D9;"
                + "U{acute}{esc}>\u00DA;"
                + "U{hat}{esc}>\u00DB;"
                + "U{umlaut}{esc}>\u00DC;"
                + "Y{acute}{esc}>\u00DD;"
                + "TH{esc}>\u00DE;"
                + "ss{esc}>\u00DF;"
                + "a{grave}{esc}>\u00E0;"
                + "a{acute}{esc}>\u00E1;"
                + "a{hat}{esc}>\u00E2;"
                + "a{tilde}{esc}>\u00E3;"
                + "a{umlaut}{esc}>\u00E4;"
                + "a{ring}{esc}>\u00E5;"
                + "ae{esc}>\u00E6;"
                + "c{cedilla}{esc}>\u00E7;"
                + "c{esc}>\u00A9;" // copyright - after c{cedilla}
                + "e{grave}{esc}>\u00E8;"
                + "e{acute}{esc}>\u00E9;"
                + "e{hat}{esc}>\u00EA;"
                + "e{umlaut}{esc}>\u00EB;"
                + "i{grave}{esc}>\u00EC;"
                + "i{acute}{esc}>\u00ED;"
                + "i{hat}{esc}>\u00EE;"
                + "i{umlaut}{esc}>\u00EF;"
                + "d-{esc}>\u00F0;"
                + "n{tilde}{esc}>\u00F1;"
                + "o{grave}{esc}>\u00F2;"
                + "o{acute}{esc}>\u00F3;"
                + "o{hat}{esc}>\u00F4;"
                + "o{tilde}{esc}>\u00F5;"
                + "o{umlaut}{esc}>\u00F6;"
                + "o{slash}{esc}>\u00F8;"
                + "o{esc}>\u00B0;"
                + "u{grave}{esc}>\u00F9;"
                + "u{acute}{esc}>\u00FA;"
                + "u{hat}{esc}>\u00FB;"
                + "u{umlaut}{esc}>\u00FC;"
                + "y{acute}{esc}>\u00FD;"
                + "y{esc}>\u00A5;" // yen sign
                + "th{esc}>\u00FE;"
                + "ss{esc}>\u00FF;"
            }
        };
    }
}
