/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/Attic/SymbolTable.java,v $ 
 * $Date: 2000/08/30 20:40:30 $ 
 * $Revision: 1.6 $
 *
 *****************************************************************************************
 */
package com.ibm.text;
import java.text.ParsePosition;

/**
 * An interface that maps strings to objects.  This interface defines
 * both lookup protocol and parsing.  This allows different components
 * to share a symbol table and to handle name parsing uniformly.  It
 * is expected that client parse code look for the SYMBOL_REF
 * character and, when seen, attempt to parse the characters after it
 * using parseReference().
 *
 * <p>Currently, RuleBasedTransliterator and UnicodeSet use this
 * interface to share variable definitions.
 */
public interface SymbolTable {

    /**
     * The character preceding a symbol reference name.
     */
    static final char SYMBOL_REF = '$';

    /**
     * Lookup the characters associated with this string and return it.
     * Return <tt>null</tt> if no such name exists.  The resultant
     * array may have length zero.
     */
    char[] lookup(String s);

    /**
     * Lookup the UnicodeSet associated with the given character, and
     * return it.  Return <tt>null</tt> if not found.
     */
    UnicodeSet lookupSet(char ch);

    /**
     * Parse a symbol reference name from the given string, starting
     * at the given position.  If no valid symbol reference name is
     * found, return null and leave pos unchanged.
     * @param text the text to parse for the name
     * @param pos on entry, the index of the first character to parse.
     * This is the character following the SYMBOL_REF character.  On
     * exit, the index after the last parsed character.
     * @param limit the index after the last character to be parsed.
     * @return the parsed name.
     */
    String parseReference(String text, ParsePosition pos, int limit);
}
