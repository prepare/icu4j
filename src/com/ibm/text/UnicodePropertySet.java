/*
**********************************************************************
*   Copyright (c) 2001, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/Attic/UnicodePropertySet.java,v $
* $Date: 2001/10/17 19:17:06 $
* $Revision: 1.1 $
**********************************************************************
*/
package com.ibm.text;

import java.text.*;
import java.util.*;
import com.ibm.util.Utility;

/**
 * INTERNAL CLASS implementing the UnicodeSet properties as outlined
 * at:
 *
 * http://oss.software.ibm.com/cvs/icu/~checkout~/icuhtml/design/unicodeset_properties.html
 *
 * Recognized syntax:
 *
 * [:foo:] [:^foo:] - white space not allowed within "[:" or ":]"
 * \p{foo} \P{foo}  - white space not allowed within "\p" or "\P"
 *
 * Other than the above restrictions, white space is ignored.  Case
 * is ignored except in "\p" and "\P".
 *
 * This class cannot be instantiated.  It has a public static method,
 * createPropertySet(), with takes a pattern to be parsed and returns
 * a new UnicodeSet.  Another public static method,
 * resemblesPattern(), returns true if a given pattern string appears
 * to be a property set pattern, and therefore should be passed in to
 * createPropertySet().
 *
 * NOTE: Current implementation is incomplete.  The following list
 * indicates which properties are supported.
 *
 *    + GeneralCategory
 *      CombiningClass
 *      BidiClass
 *      DecompositionType
 *    + NumericValue
 *      NumericType
 *      EastAsianWidth
 *      LineBreak
 *      JoiningType
 *    + Script
 *
 * '+' indicates a supported property.
 *
 * @author Alan Liu
 * @version $RCSfile: UnicodePropertySet.java,v $ $Revision: 1.1 $ $Date: 2001/10/17 19:17:06 $
 */
class UnicodePropertySet {

    private static final Hashtable NAME_MAP = new Hashtable();

    private static final Hashtable CATEGORY_MAP = new Hashtable();

    /**
     * A cache mapping character category integers, as returned by
     * UCharacter.getType(), to sets.  Entries are initially
     * null and are created on demand.
     */
    private static final UnicodeSet[] CATEGORY_CACHE =
        new UnicodeSet[UCharacterCategory.CHAR_CATEGORY_COUNT];

    /**
     * A cache mapping script integers, as defined by
     * UScript, to sets.  Entries are initially
     * null and are created on demand.
     */
    private static final UnicodeSet[] SCRIPT_CACHE =
        new UnicodeSet[UScript.CODE_LIMIT];

    // Special value codes
    private static final int ANY = -1; // general category: all code points

    //----------------------------------------------------------------
    // Public API
    //----------------------------------------------------------------

    /**
     * Return true if the given position, in the given pattern, appears
     * to be the start of a property set pattern [:foo:], \p{foo}, or
     * \P{foo}.
     */
    public static boolean resemblesPattern(String pattern, int pos) {
        // Patterns are at least 5 characters long
        if ((pos+5) > pattern.length()) {
            return false;
        }

        // Look for an opening [:, [:^, \p, or \P
        return pattern.regionMatches(pos, "[:", 0, 2) ||
            pattern.regionMatches(true, pos, "\\p", 0, 2);
    }

    /**
     * Create a UnicodeSet by parsing the given pattern at the given
     * parse position.
     *
     * @param pattern the pattern string
     * @param ppos on entry, the position at which to begin parsing.
     * This shold be one of the locations marked '^':
     *
     *   [:blah:]     \p{blah}     \P{blah}
     *   ^       %    ^       %    ^       %
     *
     * On return, the position after the last character parsed, that is,
     * the locations marked '%'.  If the parse fails, ppos is returned
     * unchanged.
     * @return a newly-constructed UnicodeSet object, or null upon
     * failure.
     */
    public static UnicodeSet createFromPattern(String pattern, ParsePosition ppos) {

        UnicodeSet set = null;

        int pos = ppos.getIndex();

        // On entry, ppos should point to one of the following locations:

        // Minimum length is 5 characters, e.g. \p{L}
        if ((pos+5) > pattern.length()) {
            return null;
        }

        boolean posix = false; // true for [:pat:], false for \p{pat} \P{pat}
        boolean invert = false;

        // Look for an opening [:, [:^, \p, or \P
        if (pattern.regionMatches(pos, "[:", 0, 2)) {
            posix = true;
            pos = skipWhitespace(pattern, pos+2);
            if (pos < pattern.length() && pattern.charAt(pos) == '^') {
                ++pos;
                invert = true;
            }
        } else if (pattern.regionMatches(true, pos, "\\p", 0, 2)) {
            invert = (pattern.charAt(pos+1) == 'P');
            pos = skipWhitespace(pattern, pos+2);
            if (pos == pattern.length() || pattern.charAt(pos++) != '{') {
                // Syntax error; "\p" or "\P" not followed by "{"
                return null;
            }
        } else {
            // Open delimiter not seen
            return null;
        }

        // Look for the matching close delimiter, either :] or }
        int close = pattern.indexOf(posix ? ":]" : "}", pos);
        if (close < 0) {
            // Syntax error; close delimiter missing
            return null;
        }

        // Look for an '=' sign.  If this is present, we will parse a
        // medium \p{gc=Cf} or long \p{GeneralCategory=Format}
        // pattern.
        int equals = pattern.indexOf('=', pos);
        if (equals >= 0 && equals < close) {
            // Equals seen; parse medium/long pattern
            String typeName = munge(pattern, pos, equals);
            String valueName = munge(pattern, equals+1, close);
            SetFactory factory;
            factory = (SetFactory) NAME_MAP.get(typeName);
            if (factory == null) {
                // Syntax error; type name not recognized
                return null;
            }
            set = factory.create(valueName);
        } else {
            // No equals seen; parse short format \p{Cf}
            String shortName = munge(pattern, pos, close);

            // First try general category
            set = createCategorySet(shortName);

            // If this fails, try script
            if (set == null) {
                set = createScriptSet(shortName);
            }
        }

        if (invert) {
            set.complement();
        }

        // Move to the limit position after the close delimiter
        ppos.setIndex(close + (posix ? 2 : 1));

        return set;
    }

    //----------------------------------------------------------------
    // Property set factory classes
    // NOTE: This will change/go away when we implement UCharacter
    // based property retrieval.
    //----------------------------------------------------------------

    static interface SetFactory {

        UnicodeSet create(String valueName);
    }

    static class NumericValueFactory implements SetFactory {
        NumericValueFactory() {}
        public UnicodeSet create(String valueName) {
            double value = Double.parseDouble(valueName);
            final int ivalue = (int) value;
            if (ivalue != value || ivalue < 0) {
                // UCharacter doesn't support negative or non-integral
                // values, so just return an empty set
                return new UnicodeSet();
            }
            return createSetFromFilter(new Filter() {
                public boolean contains(int cp) {
                    return UCharacter.getUnicodeNumericValue(cp) == ivalue;
                }
            });
        }
    }

    //----------------------------------------------------------------
    // Property set factory static methods
    // NOTE: This will change/go away when we implement UCharacter
    // based property retrieval.
    //----------------------------------------------------------------

    /**
     * Given a general category value name, create a corresponding
     * set and return it, or return null if the name is invalid.
     * @param valueName a pre-munged general category value name
     */
    private static UnicodeSet createCategorySet(String valueName) {
        Integer valueObj;
        valueObj = (Integer) CATEGORY_MAP.get(valueName);
        if (valueObj == null) {
            return null;
        }
        int valueCode = valueObj.intValue();

        UnicodeSet set = new UnicodeSet();
        if (valueCode == ANY) {
            set.complement();
            return set;
        }
        for (int cat=0; cat<UCharacterCategory.CHAR_CATEGORY_COUNT; ++cat) {
            if ((valueCode & (1 << cat)) != 0) {
                set.addAll(UnicodePropertySet.getCategorySet(cat));
            }
        }
        return set;
    }

    /**
     * Given a script value name, create a corresponding set and
     * return it, or return null if the name is invalid.
     * @param valueName a pre-munged script value name
     */
    private static UnicodeSet createScriptSet(String valueName) {
        int script = UScript.getCode(valueName);
        if (script == UScript.INVALID_CODE) {
            // Syntax error; unknown short name
            return null;
        }
        return new UnicodeSet(getScriptSet(script));
    }

    //----------------------------------------------------------------
    // Utility methods
    //----------------------------------------------------------------

    /**
     * Returns a UnicodeSet for the given category.  This set is
     * cached and returned again if this method is called again with
     * the same parameter.
     *
     * Callers MUST NOT MODIFY the returned set.
     */
    private static UnicodeSet getCategorySet(final int cat) {
        if (CATEGORY_CACHE[cat] == null) {
            CATEGORY_CACHE[cat] =
                createSetFromFilter(new Filter() {
                    public boolean contains(int cp) {
                        return UCharacter.getType(cp) == cat;
                    }
                });
        }
        return CATEGORY_CACHE[cat];
    }

    /**
     * Returns a UnicodeSet for the given script.  This set is
     * cached and returned again if this method is called again with
     * the same parameter.
     *
     * Callers MUST NOT MODIFY the returned set.
     */
    private static UnicodeSet getScriptSet(final int script) {
        if (SCRIPT_CACHE[script] == null) {
            SCRIPT_CACHE[script] =
                createSetFromFilter(new Filter() {
                    public boolean contains(int cp) {
                        return UScript.getScript(cp) == script;
                    }
                });
        }
        return SCRIPT_CACHE[script];
    }

    /**
     * Given a string, munge it to upper case and lose the whitespace.
     * So "General Category " becomes "GENERALCATEGORY".  We munge all
     * type and value strings, and store all type and value keys
     * pre-munged.
     */
    private static String munge(String str, int start, int limit) {
        StringBuffer buf = new StringBuffer();
        for (int i=start; i<limit; ) {
            int c = UTF16.charAt(str, i);
            i += UTF16.getCharCount(c);
            if (!UCharacter.isWhitespace(c)) {
                UTF16.append(buf, UCharacter.toUpperCase(c));
            }
        }
        return buf.toString();
    }

    /**
     * Skip over a sequence of zero or more white space characters
     * at pos.  Return the index of the first non-white-space character
     * at or after pos, or str.length(), if there is none.
     */
    private static int skipWhitespace(String str, int pos) {
        while (pos < str.length()) {
            int c = UTF16.charAt(str, pos);
            if (!UCharacter.isWhitespace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    //----------------------------------------------------------------
    // Generic filter-based scanning code
    //
    // NOTE: In general, we don't want to do this!  This is a temporary
    // implementation until we have time for something that examines
    // the underlying UCharacter data structures in an intelligent
    // way.  Iterating over all code points is dumb.  What we want to
    // do, for instance, is iterate over internally-stored ranges
    // of characters that have a given property.
    //----------------------------------------------------------------

    static interface Filter {
        boolean contains(int codePoint);
    }

    static UnicodeSet createSetFromFilter(Filter filter) {
        // Walk through all Unicode characters, noting the start
        // and end of each range for which filter.contain(c) is
        // true.  Add each range to a set.
        UnicodeSet set = new UnicodeSet();
        int start = -1;
        int end = -2;

        // TODO Extend this up to UnicodeSet.MAX_VALUE when we have
        // better performance; i.e., when this code can get moved into
        // the UCharacter class and not have to iterate over code
        // points.  Right now it's way too slow to iterate to 10FFFF.

        for (int i=UnicodeSet.MIN_VALUE; i<=0xFFFF; ++i) {
            if (filter.contains(i)) {
                if ((end+1) == i) {
                    end = i;
                } else {
                    if (start >= 0) {
                        set.add(start, end);
                    }
                    start = end = i;
                }
            }
        }
        if (start >= 0) {
            set.add(start, end);
        }
        return set;
    }

    //----------------------------------------------------------------
    // Type and value name maps
    //----------------------------------------------------------------

    /**
     * Add a type mapping to the name map.
     */
    private static void addType(String shortName, String longName,
                                SetFactory factory) {
        // DEBUGGING CODE: DISABLE FOR PRODUCTION BUILD
        if (true) {
            if (NAME_MAP.get(shortName) != null) {
                throw new InternalError("Duplicate name " + shortName);
            }
            if (NAME_MAP.get(longName) != null) {
                throw new InternalError("Duplicate name " + longName);
            }
        }

        NAME_MAP.put(shortName, factory);
        NAME_MAP.put(longName, factory);
    }

    /**
     * Add a value mapping to the name map.
     */
    private static void addValue(Hashtable map,
                                 String shortName, String longName,
                                 int value) {
        // DEBUGGING CODE: DISABLE FOR PRODUCTION BUILD
        if (true) {
            if (map.get(shortName) != null) {
                throw new InternalError("Duplicate name " + shortName);
            }
            if (longName != null && map.get(longName) != null) {
                throw new InternalError("Duplicate name " + longName);
            }
        }

        Integer valueObj = new Integer(value);
        map.put(shortName, valueObj);
        if (longName != null) {
            map.put(longName, valueObj);
        }
    }

    static {
        // NOTE:  We munge all search keys to have no whitespace
        // and upper case.  As such, all stored keys should have
        // this format.

        // Load the map with type data

        addType("GC", "GENERALCATEGORY", new SetFactory() {
            public UnicodeSet create(String valueName) {
                return createCategorySet(valueName);
            }
        });

        //addType("CC", "COMBININGCLASS", COMBINING_CLASS);
        //addType("BC", "BIDICLASS", BIDI_CLASS);
        //addType("DT", "DECOMPOSITIONTYPE", DECOMPOSITION_TYPE);

        addType("NV", "NUMERICVALUE", new NumericValueFactory());

        //addType("NT", "NUMERICTYPE", NUMERIC_TYPE);
        //addType("EA", "EASTASIANWIDTH", EAST_ASIAN_WIDTH);
        //addType("LB", "LINEBREAK", LINE_BREAK);
        //addType("JT", "JOININGTYPE", JOINING_TYPE);

        addType("SC", "SCRIPT", new SetFactory() {
            public UnicodeSet create(String valueName) {
                return createScriptSet(valueName);
            }
        });

        // Load the map with value data

        // General Category

        addValue(CATEGORY_MAP, "ANY", null, ANY); // special case

        addValue(CATEGORY_MAP, "C", "OTHER",
                 (1 << UCharacterCategory.CONTROL) |
                 (1 << UCharacterCategory.FORMAT) |
                 (1 << UCharacterCategory.GENERAL_OTHER_TYPES) |
                 (1 << UCharacterCategory.PRIVATE_USE) |
                 (1 << UCharacterCategory.SURROGATE));

        addValue(CATEGORY_MAP, "CC", "CONTROL",
                 1 << UCharacterCategory.CONTROL);
        addValue(CATEGORY_MAP, "CF", "FORMAT",
                 1 << UCharacterCategory.FORMAT);
        addValue(CATEGORY_MAP, "CN", "UNASSIGNED",
                 1 << UCharacterCategory.GENERAL_OTHER_TYPES);
        addValue(CATEGORY_MAP, "CO", "PRIVATEUSE",
                 1 << UCharacterCategory.PRIVATE_USE);
        addValue(CATEGORY_MAP, "CS", "SURROGATE",
                 1 << UCharacterCategory.SURROGATE);

        addValue(CATEGORY_MAP, "L", "LETTER",
                 (1 << UCharacterCategory.LOWERCASE_LETTER) |
                 (1 << UCharacterCategory.MODIFIER_LETTER) |
                 (1 << UCharacterCategory.OTHER_LETTER) |
                 (1 << UCharacterCategory.TITLECASE_LETTER) |
                 (1 << UCharacterCategory.UPPERCASE_LETTER));

        addValue(CATEGORY_MAP, "LL", "LOWERCASELETTER",
                 1 << UCharacterCategory.LOWERCASE_LETTER);
        addValue(CATEGORY_MAP, "LM", "MODIFIERLETTER",
                 1 << UCharacterCategory.MODIFIER_LETTER);
        addValue(CATEGORY_MAP, "LO", "OTHERLETTER",
                 1 << UCharacterCategory.OTHER_LETTER);
        addValue(CATEGORY_MAP, "LT", "TITLECASELETTER",
                 1 << UCharacterCategory.TITLECASE_LETTER);
        addValue(CATEGORY_MAP, "LU", "UPPERCASELETTER",
                 1 << UCharacterCategory.UPPERCASE_LETTER);

        addValue(CATEGORY_MAP, "M", "MARK",
                 (1 << UCharacterCategory.NON_SPACING_MARK) |
                 (1 << UCharacterCategory.COMBINING_SPACING_MARK) |
                 (1 << UCharacterCategory.ENCLOSING_MARK));

        addValue(CATEGORY_MAP, "MN", "NONSPACINGMARK",
                 1 << UCharacterCategory.NON_SPACING_MARK);
        addValue(CATEGORY_MAP, "MC", "SPACINGMARK",
                 1 << UCharacterCategory.COMBINING_SPACING_MARK);
        addValue(CATEGORY_MAP, "ME", "ENCLOSINGMARK",
                 1 << UCharacterCategory.ENCLOSING_MARK);

        addValue(CATEGORY_MAP, "N", "NUMBER",
                 (1 << UCharacterCategory.DECIMAL_DIGIT_NUMBER) |
                 (1 << UCharacterCategory.LETTER_NUMBER) |
                 (1 << UCharacterCategory.OTHER_NUMBER));

        addValue(CATEGORY_MAP, "ND", "DECIMALNUMBER",
                 1 << UCharacterCategory.DECIMAL_DIGIT_NUMBER);
        addValue(CATEGORY_MAP, "NL", "LETTERNUMBER",
                 1 << UCharacterCategory.LETTER_NUMBER);
        addValue(CATEGORY_MAP, "NO", "OTHERNUMBER",
                 1 << UCharacterCategory.OTHER_NUMBER);

        addValue(CATEGORY_MAP, "P", "PUNCTUATION",
                 (1 << UCharacterCategory.CONNECTOR_PUNCTUATION) |
                 (1 << UCharacterCategory.DASH_PUNCTUATION) |
                 (1 << UCharacterCategory.END_PUNCTUATION) |
                 (1 << UCharacterCategory.FINAL_PUNCTUATION) |
                 (1 << UCharacterCategory.INITIAL_PUNCTUATION) |
                 (1 << UCharacterCategory.OTHER_PUNCTUATION) |
                 (1 << UCharacterCategory.START_PUNCTUATION));

        addValue(CATEGORY_MAP, "PC", "CONNECTORPUNCTUATION",
                 1 << UCharacterCategory.CONNECTOR_PUNCTUATION);
        addValue(CATEGORY_MAP, "PD", "DASHPUNCTUATION",
                 1 << UCharacterCategory.DASH_PUNCTUATION);
        addValue(CATEGORY_MAP, "PE", "ENDPUNCTUATION",
                 1 << UCharacterCategory.END_PUNCTUATION);
        addValue(CATEGORY_MAP, "PF", "FINALPUNCTUATION",
                 1 << UCharacterCategory.FINAL_PUNCTUATION);
        addValue(CATEGORY_MAP, "PI", "INITIALPUNCTUATION",
                 1 << UCharacterCategory.INITIAL_PUNCTUATION);
        addValue(CATEGORY_MAP, "PO", "OTHERPUNCTUATION",
                 1 << UCharacterCategory.OTHER_PUNCTUATION);
        addValue(CATEGORY_MAP, "PS", "STARTPUNCTUATION",
                 1 << UCharacterCategory.START_PUNCTUATION);

        addValue(CATEGORY_MAP, "S", "SYMBOL",
                 (1 << UCharacterCategory.CURRENCY_SYMBOL) |
                 (1 << UCharacterCategory.MODIFIER_SYMBOL) |
                 (1 << UCharacterCategory.MATH_SYMBOL) |
                 (1 << UCharacterCategory.OTHER_SYMBOL));

        addValue(CATEGORY_MAP, "SC", "CURRENCYSYMBOL",
                 1 << UCharacterCategory.CURRENCY_SYMBOL);
        addValue(CATEGORY_MAP, "SK", "MODIFIERSYMBOL",
                 1 << UCharacterCategory.MODIFIER_SYMBOL);
        addValue(CATEGORY_MAP, "SM", "MATHSYMBOL",
                 1 << UCharacterCategory.MATH_SYMBOL);
        addValue(CATEGORY_MAP, "SO", "OTHERSYMBOL",
                 1 << UCharacterCategory.OTHER_SYMBOL);

        addValue(CATEGORY_MAP, "Z", "SEPARATOR",
                 (1 << UCharacterCategory.LINE_SEPARATOR) |
                 (1 << UCharacterCategory.PARAGRAPH_SEPARATOR) |
                 (1 << UCharacterCategory.SPACE_SEPARATOR));

        addValue(CATEGORY_MAP, "ZL", "LINESEPARATOR",
                 1 << UCharacterCategory.LINE_SEPARATOR);
        addValue(CATEGORY_MAP, "ZP", "PARAGRAPHSEPARATOR",
                 1 << UCharacterCategory.PARAGRAPH_SEPARATOR);
        addValue(CATEGORY_MAP, "ZS", "SPACESEPARATOR",
                 1 << UCharacterCategory.SPACE_SEPARATOR);
    }
}
