/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/UnicodeSet.java,v $
 * $Date: 2001/10/05 23:23:50 $
 * $Revision: 1.37 $
 *
 *****************************************************************************************
 */
package com.ibm.text;

import java.text.*;
import com.ibm.util.Utility;

/**
 * A mutable set of Unicode characters.  Objects of this class
 * represent <em>character classes</em> used in regular expressions.
 * Such classes specify a subset of the set of all Unicode characters,
 * which in this implementation is the characters from U+0000 to
 * U+10FFFF.
 *
 * <p><code>UnicodeSet</code> supports two APIs. The first is the
 * <em>operand</em> API that allows the caller to modify the value of
 * a <code>UnicodeSet</code> object. It conforms to Java 2's
 * <code>java.util.Set</code> interface, although
 * <code>UnicodeSet</code> cannot actually implement that
 * interface. All methods of <code>Set</code> are supported, with the
 * modification that they take a character range or single character
 * instead of an <code>Object</code>, and they take a
 * <code>UnicodeSet</code> instead of a <code>Collection</code>.  The
 * operand API may be thought of in terms of boolean logic: a boolean
 * OR is implemented by <code>add</code>, a boolean AND is implemented
 * by <code>retain</code>, a boolean XOR is implemented by
 * <code>complement</code> taking an argument, and a boolean NOT is
 * implemented by <code>complement</code> with no argument.  In terms
 * of traditional set theory function names, <code>add</code> is a
 * union, <code>retain</code> is an intersection, <code>remove</code>
 * is an asymmetric difference, and <code>complement</code> with no
 * argument is a set complement with respect to the superset range
 * <code>MIN_VALUE-MAX_VALUE</code>
 *
 * <p>The second API is the
 * <code>applyPattern()</code>/<code>toPattern()</code> API from the
 * <code>java.text.Format</code>-derived classes.  Unlike the
 * methods that add characters, add categories, and control the logic
 * of the set, the method <code>applyPattern()</code> sets all
 * attributes of a <code>UnicodeSet</code> at once, based on a
 * string pattern.
 *
 * <p>In addition, the set complement operation is supported through
 * the <code>complement()</code> method.
 *
 * <p><b>Pattern syntax</b></p>
 *
 * Patterns are accepted by the constructors and the
 * <code>applyPattern()</code> methods and returned by the
 * <code>toPattern()</code> method.  These patterns follow a syntax
 * similar to that employed by version 8 regular expression character
 * classes:
 *
 * <blockquote>
 *   <table>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>pattern :=&nbsp; </code></td>
 *       <td valign="top"><code>('[' '^'? item* ']') |
 *       ('[:' '^'? category ':]')</code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>item :=&nbsp; </code></td>
 *       <td valign="top"><code>char | (char '-' char) | pattern-expr<br>
 *       </code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>pattern-expr :=&nbsp; </code></td>
 *       <td valign="top"><code>pattern | pattern-expr pattern |
 *       pattern-expr op pattern<br>
 *       </code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>op :=&nbsp; </code></td>
 *       <td valign="top"><code>'&amp;' | '-'<br>
 *       </code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>special :=&nbsp; </code></td>
 *       <td valign="top"><code>'[' | ']' | '-'<br>
 *       </code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>char :=&nbsp; </code></td>
 *       <td valign="top"><em>any character that is not</em><code> special<br>
 *       | ('\u005C' </code><em>any character</em><code>)<br>
 *       | ('\u005Cu' hex hex hex hex)<br>
 *       </code></td>
 *     </tr>
 *     <tr align="top">
 *       <td nowrap valign="top" align="right"><code>hex :=&nbsp; </code></td>
 *       <td valign="top"><em>any character for which
 *       </em><code>Character.digit(c, 16)</code><em>
 *       returns a non-negative result</em></td>
 *     </tr>
 *     <tr>
 *       <td nowrap valign="top" align="right"><code>category :=&nbsp; </code></td>
 *       <td valign="top"><code>'M' | 'N' | 'Z' | 'C' | 'L' | 'P' |
 *       'S' | 'Mn' | 'Mc' | 'Me' | 'Nd' | 'Nl' | 'No' | 'Zs' | 'Zl' |
 *       'Zp' | 'Cc' | 'Cf' | 'Cs' | 'Co' | 'Cn' | 'Lu' | 'Ll' | 'Lt'
 *       | 'Lm' | 'Lo' | 'Pc' | 'Pd' | 'Ps' | 'Pe' | 'Po' | 'Sm' |
 *       'Sc' | 'Sk' | 'So'</code></td>
 *     </tr>
 *   </table>
 *   <br>
 *   <table border="1">
 *     <tr>
 *       <td>Legend: <table>
 *         <tr>
 *           <td nowrap valign="top"><code>a := b</code></td>
 *           <td width="20" valign="top">&nbsp; </td>
 *           <td valign="top"><code>a</code> may be replaced by <code>b</code> </td>
 *         </tr>
 *         <tr>
 *           <td nowrap valign="top"><code>a?</code></td>
 *           <td valign="top"></td>
 *           <td valign="top">zero or one instance of <code>a</code><br>
 *           </td>
 *         </tr>
 *         <tr>
 *           <td nowrap valign="top"><code>a*</code></td>
 *           <td valign="top"></td>
 *           <td valign="top">one or more instances of <code>a</code><br>
 *           </td>
 *         </tr>
 *         <tr>
 *           <td nowrap valign="top"><code>a | b</code></td>
 *           <td valign="top"></td>
 *           <td valign="top">either <code>a</code> or <code>b</code><br>
 *           </td>
 *         </tr>
 *         <tr>
 *           <td nowrap valign="top"><code>'a'</code></td>
 *           <td valign="top"></td>
 *           <td valign="top">the literal string between the quotes </td>
 *         </tr>
 *       </table>
 *       </td>
 *     </tr>
 *   </table>
 * </blockquote>
 *
 * Any character may be preceded by a backslash in order to remove any special
 * meaning.  White space characters, as defined by Character.isWhitespace(), are
 * ignored, unless they are escaped.
 *
 * Patterns specify individual characters, ranges of characters, and
 * Unicode character categories.  When elements are concatenated, they
 * specify their union.  To complement a set, place a '^' immediately
 * after the opening '[' or '[:'.  In any other location, '^' has no
 * special meaning.
 *
 * <p>Ranges are indicated by placing two a '-' between two
 * characters, as in "a-z".  This specifies the range of all
 * characters from the left to the right, in Unicode order.  If the
 * left and right characters are the same, then the range consists of
 * just that character.  If the left character is greater than the
 * right character it is a syntax error.  If a '-' occurs as the first
 * character after the opening '[' or '[^', or if it occurs as the
 * last character before the closing ']', then it is taken as a
 * literal.  Thus "[a\u005C-b]", "[-ab]", and "[ab-]" all indicate the same
 * set of three characters, 'a', 'b', and '-'.
 *
 * <p>Sets may be intersected using the '&' operator or the asymmetric
 * set difference may be taken using the '-' operator, for example,
 * "[[:L:]&[\u005Cu0000-\u005Cu0FFF]]" indicates the set of all Unicode letters
 * with values less than 4096.  Operators ('&' and '|') have equal
 * precedence and bind left-to-right.  Thus
 * "[[:L:]-[a-z]-[\u005Cu0100-\u005Cu01FF]]" is equivalent to
 * "[[[:L:]-[a-z]]-[\u005Cu0100-\u005Cu01FF]]".  This only really matters for
 * difference; intersection is commutative.
 *
 * <table>
 * <tr valign=top><td nowrap><code>[a]</code><td>The set containing 'a'
 * <tr valign=top><td nowrap><code>[a-z]</code><td>The set containing 'a'
 * through 'z' and all letters in between, in Unicode order
 * <tr valign=top><td nowrap><code>[^a-z]</code><td>The set containing
 * all characters but 'a' through 'z',
 * that is, U+0000 through 'a'-1 and 'z'+1 through U+10FFFF
 * <tr valign=top><td nowrap><code>[[<em>pat1</em>][<em>pat2</em>]]</code>
 * <td>The union of sets specified by <em>pat1</em> and <em>pat2</em>
 * <tr valign=top><td nowrap><code>[[<em>pat1</em>]&[<em>pat2</em>]]</code>
 * <td>The intersection of sets specified by <em>pat1</em> and <em>pat2</em>
 * <tr valign=top><td nowrap><code>[[<em>pat1</em>]-[<em>pat2</em>]]</code>
 * <td>The asymmetric difference of sets specified by <em>pat1</em> and
 * <em>pat2</em>
 * <tr valign=top><td nowrap><code>[:Lu:]</code>
 * <td>The set of characters belonging to the given
 * Unicode category, as defined by <code>Character.getType()</code>; in
 * this case, Unicode uppercase letters
 * <tr valign=top><td nowrap><code>[:L:]</code>
 * <td>The set of characters belonging to all Unicode categories
 * starting wih 'L', that is, <code>[[:Lu:][:Ll:][:Lt:][:Lm:][:Lo:]]</code>.
 * </table>
 *
 * <p><b>Character categories.</b>
 *
 * Character categories are specified using the POSIX-like syntax
 * '[:Lu:]'.  The complement of a category is specified by inserting
 * '^' after the opening '[:'.  The following category names are
 * recognized.  Actual determination of category data uses
 * <code>Character.getType()</code>, so it reflects the underlying
 * implmementation used by <code>Character</code>.  As of Java 2 and
 * JDK 1.1.8, this is Unicode 2.1.2.
 *
 * <pre>
 * Normative
 *     Mn = Mark, Non-Spacing
 *     Mc = Mark, Spacing Combining
 *     Me = Mark, Enclosing
 *
 *     Nd = Number, Decimal Digit
 *     Nl = Number, Letter
 *     No = Number, Other
 *
 *     Zs = Separator, Space
 *     Zl = Separator, Line
 *     Zp = Separator, Paragraph
 *
 *     Cc = Other, Control
 *     Cf = Other, Format
 *     Cs = Other, Surrogate
 *     Co = Other, Private Use
 *     Cn = Other, Not Assigned
 *
 * Informative
 *     Lu = Letter, Uppercase
 *     Ll = Letter, Lowercase
 *     Lt = Letter, Titlecase
 *     Lm = Letter, Modifier
 *     Lo = Letter, Other
 *
 *     Pc = Punctuation, Connector
 *     Pd = Punctuation, Dash
 *     Ps = Punctuation, Open
 *     Pe = Punctuation, Close
 *    *Pi = Punctuation, Initial quote
 *    *Pf = Punctuation, Final quote
 *     Po = Punctuation, Other
 *
 *     Sm = Symbol, Math
 *     Sc = Symbol, Currency
 *     Sk = Symbol, Modifier
 *     So = Symbol, Other
 * </pre>
 * *Unsupported by Java (and hence unsupported by UnicodeSet).
 *
 * @author Alan Liu
 * @version $RCSfile: UnicodeSet.java,v $ $Revision: 1.37 $ $Date: 2001/10/05 23:23:50 $ */
public class UnicodeSet extends UnicodeFilter {

    /* Implementation Notes.
     * NOTE: This conversion has been completed as of 2.0.
     *
     * UnicodeSet currently represents only the characters U+0000 to
     * U+FFFF.  This allows the API to be written in terms of the Java
     * char type, which is natural for Java at this time.  Since the
     * char data type is range-limited, we don't have to do range
     * checks.
     *
     * In order to modify UnicodeSet to work with code points up to
     * U+10FFFF, do the following: (1) Change the value of HIGH to
     * 0x110000.  (2) Change every API that takes or returns a char
     * code point to take or return an int.  (3) For those APIs taking
     * an int code point, add a range check that looks like this:
     *
     * void foo(int ch) {
     *   if (ch < MIN_VALUE || ch > MAX_VALUE) {
     *     throw new IllegalArgumentException("Invalid code point " + ch);
     *   }
     *   // ...
     * }
     *
     * (4) Modify toPattern() to handle characters past 0xFFFF.  (5)
     * Modify applyPattern() to parse escapes from \U100000 to \U10FFFF.
     * Note uppercase U. (6) Modify MIN_VALUE and MAX_VALUE to be of
     * type int.
     */

    private static final int LOW = 0x000000; // LOW <= all valid values. ZERO for codepoints
    private static final int HIGH = 0x110000; // HIGH > all valid values. 10000 for code units.
                                             // 110000 for codepoints

    /**
     * Minimum value that can be stored in a UnicodeSet.
     */
    public static final int MIN_VALUE = LOW;

    /**
     * Maximum value that can be stored in a UnicodeSet.
     */
    public static final int MAX_VALUE = HIGH - 1;

    private int len;      // length used; list may be longer to minimize reallocs
    private int[] list;   // MUST be terminated with HIGH
    private int[] rangeList; // internal buffer
    private int[] buffer; // internal buffer

    /**
     * The pattern representation of this set.  This may not be the
     * most economical pattern.  It is the pattern supplied to
     * applyPattern(), with variables substituted and whitespace
     * removed.  For sets constructed without applyPattern(), or
     * modified using the non-pattern API, this string will be null,
     * indicating that toPattern() must generate a pattern
     * representation from the inversion list.
     */ 
    private String pat = null;

    private static final int START_EXTRA = 16;         // initial storage. Must be >= 0
    private static final int GROW_EXTRA = START_EXTRA; // extra amount for growth. Must be >= 0

    private static final String CATEGORY_NAMES =
        //                    1 1 1 1 1 1 1   1 1 2 2 2 2 2 2 2 2 2
        //0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6   8 9 0 1 2 3 4 5 6 7 8
        "CnLuLlLtLmLoMnMeMcNdNlNoZsZlZpCcCf--CoCsPdPsPePcPoSmScSkSo";

    private static final int UNSUPPORTED_CATEGORY = 17;

    private static final int CATEGORY_COUNT = 29;

    /**
     * A cache mapping character category integers, as returned by
     * Character.getType(), to inversion lists.  Entries are initially
     * null and are created on demand.
     */
    private static final UnicodeSet[] CATEGORY_CACHE =
        new UnicodeSet[CATEGORY_COUNT];

    //----------------------------------------------------------------
    // Public API
    //----------------------------------------------------------------

    /**
     * Constructs an empty set.
     */
    public UnicodeSet() {
        list = new int[1 + START_EXTRA];
        list[len++] = HIGH;
    }

    /**
     * Constructs a copy of an existing set.
     */
    public UnicodeSet(UnicodeSet other) {
        set(other);
    }

    /**
     * Constructs a set containing the given range. If <code>end >
     * start</code> then an empty set is created.
     *
     * @param start first character, inclusive, of range
     * @param end last character, inclusive, of range
     */
    public UnicodeSet(int start, int end) {
        this();
        complement(start, end);
    }

    /**
     * Constructs a set from the given pattern.  See the class description
     * for the syntax of the pattern language.  Whitespace is ignored.
     * @param pattern a string specifying what characters are in the set
     * @exception java.lang.IllegalArgumentException if the pattern contains
     * a syntax error.
     */
    public UnicodeSet(String pattern) {
        this(pattern, true);
    }

    /**
     * Constructs a set from the given pattern.  See the class description
     * for the syntax of the pattern language.
     * @param pattern a string specifying what characters are in the set
     * @param ignoreWhitespace if true, ignore characters for which
     * Character.isWhitespace() returns true
     * @exception java.lang.IllegalArgumentException if the pattern contains
     * a syntax error.
     */
    public UnicodeSet(String pattern, boolean ignoreWhitespace) {
        this();
        applyPattern(pattern, ignoreWhitespace);
    }

    /**
     * Constructs a set from the given pattern.  See the class description
     * for the syntax of the pattern language.
     * @param pattern a string specifying what characters are in the set
     * @param pos on input, the position in pattern at which to start parsing.
     * On output, the position after the last character parsed.
     * @param symbols a symbol table mapping variables to char[] arrays
     * and chars to UnicodeSets
     * @exception java.lang.IllegalArgumentException if the pattern
     * contains a syntax error.
     */
    public UnicodeSet(String pattern, ParsePosition pos, SymbolTable symbols) {
        this();
        applyPattern(pattern, pos, symbols, true);
    }

    /**
     * Constructs a set from the given Unicode character category.
     * @param category an integer indicating the character category as
     * returned by <code>Character.getType()</code>.
     * @exception java.lang.IllegalArgumentException if the given
     * category is invalid.
     */
    public UnicodeSet(int category) {
        if (category < 0 || category >= CATEGORY_COUNT ||
            category == UNSUPPORTED_CATEGORY) {
            throw new IllegalArgumentException("Invalid category");
        }
        set(getCategorySet(category));
    }

    /**
     * Make this object represent the range <code>start - end</code>.
     * If <code>end > start</code> then this object is set to an
     * an empty range.
     *
     * @param start first character in the set, inclusive
     * @rparam end last character in the set, inclusive
     */
    public void set(int start, int end) {
        clear();
        complement(start, end);
    }

    /**
     * Make this object represent the same set as <code>other</code>.
     * @param other a <code>UnicodeSet</code> whose value will be
     * copied to this object
     */
    public void set(UnicodeSet other) {
        list = (int[]) other.list.clone();
        len = other.len;
        pat = other.pat;
    }

    /**
     * Modifies this set to represent the set specified by the given pattern.
     * See the class description for the syntax of the pattern language.
     * Whitespace is ignored.
     * @param pattern a string specifying what characters are in the set
     * @exception java.lang.IllegalArgumentException if the pattern
     * contains a syntax error.
     */
    public final void applyPattern(String pattern) {
        applyPattern(pattern, true);
    }

    /**
     * Modifies this set to represent the set specified by the given pattern,
     * optionally ignoring whitespace.
     * See the class description for the syntax of the pattern language.
     * @param pattern a string specifying what characters are in the set
     * @param ignoreWhitespace if true then characters for which
     * Character.isWhitespace() returns true are ignored
     * @exception java.lang.IllegalArgumentException if the pattern
     * contains a syntax error.
     */
    public void applyPattern(String pattern, boolean ignoreWhitespace) {
        ParsePosition pos = new ParsePosition(0);
        applyPattern(pattern, pos, null, ignoreWhitespace);

        int i = pos.getIndex();
        int n = pattern.length();

        // Skip over trailing whitespace
        if (ignoreWhitespace) {
            while (i < n && Character.isWhitespace(pattern.charAt(i))) {
                ++i;
            }
        }

        if (i != n) {
            throw new IllegalArgumentException("Parse of \"" + pattern +
                                               "\" failed at " + i);
        }
    }

    /**
     * Append the <code>toPattern()</code> representation of a
     * character to the given <code>StringBuffer</code>.
     */
    private static void _appendToPat(StringBuffer buf, int c, boolean useHexEscape) {
        if (useHexEscape) {
            // Use hex escape notation (<backslash>uxxxx or <backslash>Uxxxxxxxx) for anything
            // unprintable
            if (_escapeUnprintable(buf, c)) {
                return;
            }
        }
        // Okay to let ':' pass through
        switch (c) {
        case '[': // SET_OPEN:
        case ']': // SET_CLOSE:
        case '-': // HYPHEN:
        case '^': // COMPLEMENT:
        case '&': // INTERSECTION:
        case '\\': //BACKSLASH:
            buf.append('\\');
            break;
        default:
            // Escape whitespace
            if (UCharacter.isWhitespace(c)) {
                buf.append('\\');
            }
            break;
        }
        UTF16.append(buf, c);
    }

    private static final char[] HEX = {'0','1','2','3','4','5','6','7',
                                       '8','9','A','B','C','D','E','F'};

    /**
     * Return true if the character is NOT printable ASCII.
     *
     * This method should really be in UnicodeString (or similar).  For
     * now, we implement it here and share it with friend classes.
     */
    static boolean _isUnprintable(int c) {
        return !(c == 0x0A || (c >= 0x20 && c <= 0x7E));
    }

    /**
     * Escape unprintable characters using <backslash>uxxxx notation for U+0000 to
     * U+FFFF and <backslash>Uxxxxxxxx for U+10000 and above.  If the character is
     * printable ASCII, then do nothing and return FALSE.  Otherwise,
     * append the escaped notation and return TRUE.
     *
     * This method should really be in UnicodeString.  For now, we
     * implement it here and share it with friend classes.
     */
    static boolean _escapeUnprintable(StringBuffer result, int c) {
        if (_isUnprintable(c)) {
            result.append('\\');
            if ((c & ~0xFFFF) != 0) {
                result.append('U');
                result.append(HEX[0xF&(c>>28)]);
                result.append(HEX[0xF&(c>>24)]);
                result.append(HEX[0xF&(c>>20)]);
                result.append(HEX[0xF&(c>>16)]);
            } else {
                result.append('u');
            }
            result.append(HEX[0xF&(c>>12)]);
            result.append(HEX[0xF&(c>>8)]);
            result.append(HEX[0xF&(c>>4)]);
            result.append(HEX[0xF&c]);
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this set.  If the result of
     * calling this function is passed to a UnicodeSet constructor, it
     * will produce another set that is equal to this one.
     */
    public String toPattern(boolean escapeUnprintable) {
        StringBuffer result = new StringBuffer();
        return _toPattern(result, escapeUnprintable).toString();
    }

    /**
     * Append a string representation of this set to result.  This will be
     * a cleaned version of the string passed to applyPattern(), if there
     * is one.  Otherwise it will be generated.
     */
    private StringBuffer _toPattern(StringBuffer result,
                                    boolean escapeUnprintable) {
        if (pat != null) {
            int i;
            int backslashCount = 0;
            for (i=0; i<pat.length(); ++i) {
                char c = pat.charAt(i);
                if (escapeUnprintable && _isUnprintable(c)) {
                    // If the unprintable character is preceded by an odd
                    // number of backslashes, then it has been escaped.
                    // Before unescaping it, we delete the final
                    // backslash.
                    if ((backslashCount % 2) == 1) {
                        result.setLength(result.length() - 1);
                    }
                    _escapeUnprintable(result, c);
                    backslashCount = 0;
                } else {
                    result.append(c);
                    if (c == '\\') {
                        ++backslashCount;
                    } else {
                        backslashCount = 0;
                    }
                }
            }
            return result;
        }
        
        return _generatePattern(result, escapeUnprintable);
    }

    /**
     * Generate and append a string representation of this set to result.
     * This does not use this.pat, the cleaned up copy of the string
     * passed to applyPattern(). 
     */
    public StringBuffer _generatePattern(StringBuffer result,
                                         boolean escapeUnprintable) {
        result.append('[');

        // Check against the predefined categories.  We implicitly build
        // up ALL category sets the first time toPattern() is called.
        for (int cat=0; cat<CATEGORY_COUNT; ++cat) {
            if (this.equals(getCategorySet(cat))) {
                result.append(':');
                result.append(CATEGORY_NAMES.substring(cat*2, cat*2+2));
                return result.append(":]");
            }
        }

        int count = getRangeCount();

        // If the set contains at least 2 intervals and includes both
        // MIN_VALUE and MAX_VALUE, then the inverse representation will
        // be more economical.
        if (count > 1 &&
            getRangeStart(0) == MIN_VALUE &&
            getRangeEnd(count-1) == MAX_VALUE) {

            // Emit the inverse
            result.append('^');

            for (int i = 1; i < count; ++i) {
                int start = getRangeEnd(i-1)+1;
                int end = getRangeStart(i)-1;
                _appendToPat(result, start, escapeUnprintable);
                if (start != end) {
                    result.append('-');
                    _appendToPat(result, end, escapeUnprintable);
                }
            }
        }

        // Default; emit the ranges as pairs
        else {
            for (int i = 0; i < count; ++i) {
                int start = getRangeStart(i);
                int end = getRangeEnd(i);
                _appendToPat(result, start, escapeUnprintable);
                if (start != end) {
                    result.append('-');
                    _appendToPat(result, end, escapeUnprintable);
                }
            }
        }

        return result.append(']');
    }

    /**
     * Returns the number of elements in this set (its cardinality),
     * <em>n</em>, where <code>0 <= </code><em>n</em><code> <= 65536</code>.
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size() {
        int n = 0;
        int count = getRangeCount();
        for (int i = 0; i < count; ++i) {
            n += getRangeEnd(i) - getRangeStart(i) + 1;
        }
        return n;
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty() {
        return len == 1;
    }

    /**
     * Returns <tt>true</tt> if this set contains every character
     * in the specified range of chars.
     * If <code>end > start</code> then the results of this method
     * are undefined.
     *
     * @return <tt>true</tt> if this set contains the specified range
     * of chars.
     */
    public boolean contains(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        int i = -1;
        while (true) {
            if (start < list[++i]) break;
        }
        return ((i & 1) != 0 && end < list[i]);
    }

    /**
     * Implementation of UnicodeMatcher API.  Returns <tt>true</tt> if
     * this set contains any character whose low byte is the given
     * value.  This is used by <tt>RuleBasedTransliterator</tt> for
     * indexing.
     */
    public boolean matchesIndexValue(byte v) {
        /* The index value v, in the range [0,255], is contained in this set if
         * it is contained in any pair of this set.  Pairs either have the high
         * bytes equal, or unequal.  If the high bytes are equal, then we have
         * aaxx..aayy, where aa is the high byte.  Then v is contained if xx <=
         * v <= yy.  If the high bytes are unequal we have aaxx..bbyy, bb>aa.
         * Then v is contained if xx <= v || v <= yy.  (This is identical to the
         * time zone month containment logic.)
         */
        for (int i=0; i<getRangeCount(); ++i) {
            int low = getRangeStart(i);
            int high = getRangeEnd(i);
            if ((low & ~0xFF) == (high & ~0xFF)) {
                if ((low & 0xFF) <= v && v <= (high & 0xFF)) {
                    return true;
                }
            } else if ((low & 0xFF) <= v || v <= (high & 0xFF)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implementation of UnicodeMatcher.matches().
     */
    public int matches(Replaceable text,
                       int[] offset,
                       int limit,
                       boolean incremental) {
        if (offset[0] == limit) {
            if (contains(TransliterationRule.ETHER)) {
                return incremental ? U_PARTIAL_MATCH : U_MATCH;
            } else {
                return U_MISMATCH;
            }
        } else {
            return super.matches(text, offset, limit, incremental);
        }
    }

    /**
     * Returns the index of the given character within this set, where
     * the set is ordered by ascending code point.  If the character
     * is not in this set, return -1.  The inverse of this method is
     * <code>charAt()</code>.
     * @return an index from 0..size()-1, or -1
     */
    public int indexOf(int c) {
        if (c < MIN_VALUE || c > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        int i = 0;
        int n = 0;
        for (;;) {
            int start = list[i++];
            if (c < start) {
                return -1;
            }
            int limit = list[i++];
            if (c < limit) {
                return n + c - start;
            }
            n += limit - start;
        }
    }

    /**
     * Returns the character at the given index within this set, where
     * the set is ordered by ascending code point.  If the index is
     * out of range, return U+FFFE.  The inverse of this method is
     * <code>indexOf()</code>.
     * @param index an index from 0..size()-1
     */
    public int charAt(int index) {
        if (index >= 0) {
            for (int i=0; i < len;) {
                int start = list[i++];
                int count = list[i++] - start;
                if (index < count) {
                    return (char)(start + index);
                }
                index -= count;
            }
        }
        return '\uFFFE';
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified char.
     *
     * @return <tt>true</tt> if this set contains the specified char.
     */
    public boolean contains(int c) {
        if (c < MIN_VALUE || c > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        // catch degenerate cases (not needed unless HIGH > 0x10000
        if (c == HIGH) {   // catch final, so we don't do it in loop!
            return (len & 1) == 0;  // even length includes everything
        }
        // Set i to the index of the start item greater than ch
        // We know we will terminate without length test!
        // LATER: for large sets, add binary search
        int i = -1;
        while (true) {
            if (c < list[++i]) break;
        }
        return ((i & 1) != 0); // return true if odd
    }

    /**
     * Adds the specified range to this set if it is not already
     * present.  If this set already contains the specified range,
     * the call leaves this set unchanged.  If <code>end > start</code>
     * then an empty range is added, leaving the set unchanged.
     *
     * @param start first character, inclusive, of range to be added
     * to this set.
     * @param end last character, inclusive, of range to be added
     * to this set.
     */
    public void add(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            add(range(start, end), 2, 0);
        }
    }

    /**
     * Adds the specified character to this set if it is not already
     * present.  If this set already contains the specified character,
     * the call leaves this set unchanged.
     */
    public final void add(int c) {
        add(c, c);
    }

    /**
     * Retain only the elements in this set that are contained in the
     * specified range.  If <code>end > start</code> then an empty range is
     * retained, leaving the set empty.
     *
     * @param start first character, inclusive, of range to be retained
     * to this set.
     * @param end last character, inclusive, of range to be retained
     * to this set.
     */
    public void retain(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            retain(range(start, end), 2, 0);
        } else {
            clear();
        }
    }

    /**
     * Retain the specified character from this set if it is present.
     */
    public final void retain(int c) {
        retain(c, c);
    }

    /**
     * Removes the specified range from this set if it is present.
     * The set will not contain the specified range once the call
     * returns.  If <code>end > start</code> then an empty range is
     * removed, leaving the set unchanged.
     *
     * @param start first character, inclusive, of range to be removed
     * from this set.
     * @param end last character, inclusive, of range to be removed
     * from this set.
     */
    public void remove(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            retain(range(start, end), 2, 2);
        }
    }

    /**
     * Removes the specified character from this set if it is present.
     * The set will not contain the specified character once the call
     * returns.
     */
    public final void remove(int c) {
        remove(c, c);
    }

    /**
     * Complements the specified range in this set.  Any character in
     * the range will be removed if it is in this set, or will be
     * added if it is not in this set.  If <code>end > start</code>
     * then an empty range is complemented, leaving the set unchanged.
     *
     * @param start first character, inclusive, of range to be removed
     * from this set.
     * @param end last character, inclusive, of range to be removed
     * from this set.
     */
    public void complement(int start, int end) {
        if (start < MIN_VALUE || start > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < MIN_VALUE || end > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            xor(range(start, end), 2, 0);
        }
    }

    /**
     * Complements the specified character in this set.  The character
     * will be removed if it is in this set, or will be added if it is
     * not in this set.
     */
    public final void complement(int c) {
        complement(c, c);
    }

    /**
     * Inverts this set.  This operation modifies this set so that its
     * value is its complement.  This is equivalent to
     * <code>complement(MIN_VALUE, MAX_VALUE)</code>.
     */
    public void complement() {
        if (list[0] == LOW) {
            System.arraycopy(list, 1, list, 0, len-1);
            --len;
        } else {
            ensureCapacity(len+1);
            System.arraycopy(list, 0, list, 1, len);
            list[0] = LOW;
            ++len;
        }
        pat = null;
    }

    /**
     * Returns <tt>true</tt> if the specified set is a subset
     * of this set.
     *
     * @param c set to be checked for containment in this set.
     * @return <tt>true</tt> if this set contains all of the elements of the
     * 	       specified set.
     */
    public boolean containsAll(UnicodeSet c) {
        // The specified set is a subset if all of its pairs are contained in
        // this set.  It's possible to code this more efficiently in terms of
        // direct manipulation of the inversion lists if the need arises.
        int n = c.getRangeCount();
        for (int i=0; i<n; ++i) {
            if (!contains(c.getRangeStart(i), c.getRangeEnd(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds all of the elements in the specified set to this set if
     * they're not already present.  This operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is unspecified if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c set whose elements are to be added to this set.
     * @see #add(char, char)
     */
    public void addAll(UnicodeSet c) {
        add(c.list, c.len, 0);
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified set.  In other words, removes from this set all of
     * its elements that are not contained in the specified set.  This
     * operation effectively modifies this set so that its value is
     * the <i>intersection</i> of the two sets.
     *
     * @param c set that defines which elements this set will retain.
     */
    public void retainAll(UnicodeSet c) {
        retain(c.list, c.len, 0);
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified set.  This operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * @param c set that defines which elements will be removed from
     *          this set.
     */
    public void removeAll(UnicodeSet c) {
        retain(c.list, c.len, 2);
    }

    /**
     * Complements in this set all elements contained in the specified
     * set.  Any character in the other set will be removed if it is
     * in this set, or will be added if it is not in this set.
     *
     * @param c set that defines which elements will be complemented from
     *          this set.
     */
    public void complementAll(UnicodeSet c) {
        xor(c.list, c.len, 0);
    }

    /**
     * Removes all of the elements from this set.  This set will be
     * empty after this call returns.
     */
    public void clear() {
        list[0] = HIGH;
        len = 1;
        pat = null;
    }

    /**
     * Iteration method that returns the number of ranges contained in
     * this set.
     * @see #getRangeStart
     * @see #getRangeEnd
     */
    public int getRangeCount() {
        return len/2;
    }

    /**
     * Iteration method that returns the first character in the
     * specified range of this set.
     * @exception ArrayIndexOutOfBoundsException if index is outside
     * the range <code>0..getRangeCount()-1</code>
     * @see #getRangeCount
     * @see #getRangeEnd
     */
    public int getRangeStart(int index) {
        return list[index*2];
    }

    /**
     * Iteration method that returns the last character in the
     * specified range of this set.
     * @exception ArrayIndexOutOfBoundsException if index is outside
     * the range <code>0..getRangeCount()-1</code>
     * @see #getRangeStart
     * @see #getRangeEnd
     */
    public int getRangeEnd(int index) {
        return (list[index*2 + 1] - 1);
    }

    /**
     * Reallocate this objects internal structures to take up the least
     * possible space, without changing this object's value.
     */
    public void compact() {
        if (len != list.length) {
            int[] temp = new int[len];
            System.arraycopy(list, 0, temp, 0, len);
            list = temp;
        }
        rangeList = null;
        buffer = null;
    }

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the specified object is also a set, the two sets
     * have the same size, and every member of the specified set is
     * contained in this set (or equivalently, every member of this set is
     * contained in the specified set).
     *
     * @param o Object to be compared for equality with this set.
     * @return <tt>true</tt> if the specified Object is equal to this set.
     */
    public boolean equals(Object o) {
        try {
            UnicodeSet that = (UnicodeSet) o;
            if (len != that.len) return false;
            for (int i = 0; i < len; ++i) {
                if (list[i] != that.list[i]) return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code value for this set.
     *
     * @return the hash code value for this set.
     * @see Object#hashCode()
     */
    public int hashCode() {
        int result = len;
        for (int i = 0; i < len; ++i) {
            result *= 1000003;
            result += list[i];
        }
        return result;
    }

    /**
     * Return a programmer-readable string representation of this object.
     */
    public String toString() {
        return getClass().getName() + '(' + toPattern(false) + ')';
    }

    //----------------------------------------------------------------
    // Implementation: Pattern parsing
    //----------------------------------------------------------------

    /**
     * Parses the given pattern, starting at the given position.  The character
     * at pattern.charAt(pos.getIndex()) must be '[', or the parse fails.
     * Parsing continues until the corresponding closing ']'.  If a syntax error
     * is encountered between the opening and closing brace, the parse fails.
     * Upon return from a successful parse, the ParsePosition is updated to
     * point to the character following the closing ']', and an inversion
     * list for the parsed pattern is returned.  This method
     * calls itself recursively to parse embedded subpatterns.
     *
     * @param pattern the string containing the pattern to be parsed.  The
     * portion of the string from pos.getIndex(), which must be a '[', to the
     * corresponding closing ']', is parsed.
     * @param pos upon entry, the position at which to being parsing.  The
     * character at pattern.charAt(pos.getIndex()) must be a '['.  Upon return
     * from a successful parse, pos.getIndex() is either the character after the
     * closing ']' of the parsed pattern, or pattern.length() if the closing ']'
     * is the last character of the pattern string.
     * @return an inversion list for the parsed substring
     * of <code>pattern</code>
     * @exception java.lang.IllegalArgumentException if the parse fails.
     */
    void applyPattern(String pattern,
                      ParsePosition pos,
                      SymbolTable symbols,
                      boolean ignoreWhitespace) {

        // Need to build the pattern in a temporary string because
        // _applyPattern calls add() etc., which set pat to empty.
        StringBuffer rebuiltPat = new StringBuffer();
        _applyPattern(pattern, pos, symbols, rebuiltPat, ignoreWhitespace);
        pat = rebuiltPat.toString();
    }

    void _applyPattern(String pattern, ParsePosition pos,
                       SymbolTable symbols, StringBuffer rebuiltPat,
                       boolean ignoreWhitespace) {

        // If the pattern contains any of the following, we save a
        // rebuilt (variable-substituted) copy of the source pattern:
        // - a category
        // - an intersection or subtraction operator
        // - an anchor (trailing '$', indicating RBT ether)
        boolean rebuildPattern = false;
        StringBuffer newPat = new StringBuffer("[");
        int nestedPatStart = -1; // see below for usage
        boolean nestedPatDone = false; // see below for usage
        
        boolean invert = false;
        clear();

        final int NONE = -1;
        int lastChar = NONE; // This is either a char (0..10FFFF) or -1
        char lastOp = 0;

        /* This loop iterates over the characters in the pattern.  We start at
         * the position specified by pos.  We exit the loop when either a
         * matching closing ']' is seen, or we read all characters of the
         * pattern.  In the latter case an error will be thrown.
         */

        /* Pattern syntax:
         *  pat := '[' '^'? elem* ']'
         *  elem := a | a '-' a | set | set op set
         *  set := pat | (a set variable)
         *  op := '&' | '-'
         *  a := (a character, possibly defined by a var)
         */

        // mode 0: No chars parsed yet; next must be '['
        // mode 1: '[' seen; if next is '^' or ':' then special
        // mode 2: '[' '^'? seen; parse pattern and close with ']'
        // mode 3: '[:' seen; parse category and close with ':]'
        int mode = 0;
        int colonPos = 0; // Expected pos of ':' in '[:'
        int start = pos.getIndex();
        int i = start;
        int limit = pattern.length();
        /* In the case of an embedded SymbolTable variable, we look it up and
         * then take characters from the resultant char[] array.  These chars
         * are subjected to an extra level of lookup in the SymbolTable in case
         * they are stand-ins for a nested UnicodeSet.  */
        char[] varValueBuffer = null;
        int ivarValueBuffer = 0;
        int anchor = 0;
        int c;
        while (i<limit) {
            /* If the next element is a single character, c will be set to it,
             * and nestedSet will be null.  In this case isLiteral indicates
             * whether the character should assume special meaning if it has
             * one.  If the next element is a nested set, either via a variable
             * reference, or via an embedded "[..]"  or "[:..:]" pattern, then
             * nestedSet will be set to the pairs list for the nested set, and
             * c's value should be ignored.
             */
            UnicodeSet nestedSet = null;
            boolean isLiteral = false;
            if (varValueBuffer != null) {
                if (ivarValueBuffer < varValueBuffer.length) {
                    c = UTF16.charAt(varValueBuffer, 0, varValueBuffer.length, ivarValueBuffer);
                    ivarValueBuffer += UTF16.getCharCount(c);
                    nestedSet = symbols.lookupSet(c); // may be NULL
                    nestedPatDone = false;
                } else {
                    varValueBuffer = null;
                    c = UTF16.charAt(pattern, i);
                    i += UTF16.getCharCount(c);
                }
            } else {
                c = UTF16.charAt(pattern, i);
                i += UTF16.getCharCount(c);
            }

            // Ignore whitespace.  This is not Unicode whitespace, but Java
            // whitespace, a subset of Unicode whitespace.
            if (ignoreWhitespace && UCharacter.isWhitespace(c)) {
                continue;
            }

            // Keep track of the count of characters after an alleged anchor
            if (anchor > 0) {
                ++anchor;
            }

            // Parse the opening '[' and optional following '^'
            switch (mode) {
            case 0:
                if (c == '[') {
                    mode = 1; // Next look for '^'
                    colonPos = i; // Expect ':' at next offset
                    continue;
                } else {
                    throw new IllegalArgumentException("Missing opening '['");
                }
            case 1:
                mode = 2;
                switch (c) {
                case '^':
                    invert = true;
                    newPat.append((char) c);
                    continue; // Back to top to fetch next character
                case ':':
                    if (i-1 == colonPos) {
                        // '[:' cannot have whitespace in it
                        --i; // Backup to the '['
                        c = '[';
                        mode = 3;
                        // Fall through and parse category using the same
                        // code used to parse a nested category.  The mode
                        // will indicate that this is actually top level.
                    }
                    break; // Fall through
                case '-':
                    isLiteral = true; // Treat leading '-' as a literal
                    break; // Fall through
                }
                // else fall through and parse this character normally
            }

            // After opening matter is parsed ("[", "[^", or "[:"), the mode
            // will be 2 if we want a closing ']', or 3 if we should parse a
            // category and close with ":]".

            // Only process escapes, variable references, and nested sets
            // if we are _not_ retrieving characters from the variable
            // buffer.  Characters in the variable buffer have already
            // benn through escape and variable reference processing.
            if (varValueBuffer == null) {
                /* Handle escapes.  If a character is escaped, then it assumes its
                 * literal value.  This is true for all characters, both special
                 * characters and characters with no special meaning.  We also
                 * interpret '\\uxxxx' Unicode escapes here (as literals).
                 */
                if (c == '\\') {
                    int[] offset = new int[] { i };
                    int escaped = Utility.unescapeAt(pattern, offset);
                    if (escaped == -1) {
                        int sta = Math.max(i - 8, 0);
                        int lim = Math.min(i + 16, pattern.length());
                        throw new IllegalArgumentException("Invalid escape sequence " +
                                                           pattern.substring(sta, i-1) +
                                                           "|" +
                                                           pattern.substring(i-1, lim));
                    }
                    i = offset[0];
                    isLiteral = true;
                    c = escaped;
                }

                /* Parse variable references.  These are treated as literals.  If a
                 * variable refers to a UnicodeSet, its stand in character is
                 * returned in the char[] buffer.
                 * Variable names are only parsed if varNameToChar is not null.
                 * Set variables are only looked up if varCharToSet is not null.
                 */
                else if (symbols != null && !isLiteral && c == SymbolTable.SYMBOL_REF) {
                    pos.setIndex(i);
                    String name = symbols.parseReference(pattern, pos, limit);
                    if (name != null) {
                        varValueBuffer = symbols.lookup(name);
                        if (varValueBuffer == null) {
                            throw new IllegalArgumentException("Undefined variable: "
                                                               + name);
                        }
                        ivarValueBuffer = 0;
                        i = pos.getIndex(); // Make i point PAST last char of var name
                    } else {
                        // Got a null; this means we have an isolated $.
                        // Tentatively assume this is an anchor.
                        anchor = 1;
                    }
                    continue; // Back to the top to get varValueBuffer[0]
                }

                /* An opening bracket indicates the first bracket of a nested
                 * subpattern, either a normal pattern or a category pattern.  We
                 * recognize these here and set nestedSet accordingly.
                 */
                else if (!isLiteral && c == '[') {
                    // Record position before nested pattern
                    nestedPatStart = newPat.length();

                    // Handle "[:...:]", representing a character category
                    if (i < pattern.length() && pattern.charAt(i) == ':') {
                        ++i;
                        int j = pattern.indexOf(":]", i);
                        if (j < 0) {
                            throw new IllegalArgumentException("Missing \":]\"");
                        }
                        String scratch = pattern.substring(i, j);
                        nestedSet = new UnicodeSet();
                        nestedSet.applyCategory(scratch);
                        nestedPatDone = true; // We're going to do it just below
                        i = j+2; // Advance i past ":]"

                        // Use a rebuilt pattern.  If we are top level,
                        // then there is already a SET_OPEN in newPat, and
                        // SET_CLOSE will be appended elsewhere.
                        if (mode != 3) {
                            newPat.append('[');
                        }
                        newPat.append(':').append(scratch).append(':');
                        if (mode != 3) {
                            newPat.append(']');
                        }
                        rebuildPattern = true;

                        if (mode == 3) {
                            // Entire pattern is a category; leave parse
                            // loop.  This is one of 2 ways we leave this
                            // loop if the pattern is well-formed.
                            set(nestedSet);
                            mode = 4;
                            break;
                        }
                    } else {
                        // Recurse to get the pairs for this nested set.
                        // Backup i to '['.
                        pos.setIndex(--i);
                        switch (lastOp) {
                        case '-':
                        case '&':
                            newPat.append(lastOp);
                            break;
                        }
                        nestedSet = new UnicodeSet();
                        nestedSet._applyPattern(pattern, pos, symbols, newPat, ignoreWhitespace);
                        nestedPatDone = true;
                        i = pos.getIndex();
                    }
                }
            }

            /* At this point we have either a character c, or a nested set.  If
             * we have encountered a nested set, either embedded in the pattern,
             * or as a variable, we have a non-null nestedSet, and c should be
             * ignored.  Otherwise c is the current character, and isLiteral
             * indicates whether it is an escaped literal (or variable) or a
             * normal unescaped character.  Unescaped characters '-', '&', and
             * ']' have special meanings.
             */
            if (nestedSet != null) {
                if (lastChar != NONE) {
                    if (lastOp != 0) {
                        throw new IllegalArgumentException("Illegal rhs for " + lastChar + lastOp);
                    }
                    add((char) lastChar, (char) lastChar);
                    if (nestedPatDone) {
                        // If there was a character before the nested set,
                        // then we need to insert it in newPat before the
                        // pattern for the nested set.  This position was
                        // recorded in nestedPatStart.
                        StringBuffer s = new StringBuffer();
                        _appendToPat(s, lastChar, false);
                        newPat.insert(nestedPatStart, s.toString());
                    } else {
                        _appendToPat(newPat, lastChar, false);
                    }
                    lastChar = NONE;
                }
                switch (lastOp) {
                case '-':
                    removeAll(nestedSet);
                    break;
                case '&':
                    retainAll(nestedSet);
                    break;
                case 0:
                    addAll(nestedSet);
                    break;
                }

                // Get the pattern for the nested set, if we haven't done so
                // already.
                if (!nestedPatDone) {
                    if (lastOp != 0) {
                        newPat.append(lastOp);
                    }
                    nestedSet._toPattern(newPat, false);
                }
                rebuildPattern = true;

                lastOp = 0;

            } else if (!isLiteral && c == ']') {
                // Final closing delimiter.  This is the only way we leave this
                // loop if the pattern is well-formed.
                if (anchor > 2 || anchor == 1) {
                    throw new IllegalArgumentException("Syntax error near $" + pattern);
                    
                }
                if (anchor == 2) {
                    rebuildPattern = true;
                    newPat.append(SymbolTable.SYMBOL_REF);
                    add(TransliterationRule.ETHER);
                }
                mode = 4;
                break;
            } else if (lastOp == 0 && !isLiteral && (c == '-' || c == '&')) {
                lastOp = (char) c;
            } else if (lastOp == '-') {
                if (lastChar >= c) {
                    // Don't allow redundant (a-a) or empty (b-a) ranges;
                    // these are most likely typos.
                    throw new IllegalArgumentException("Invalid range " + lastChar +
                                                       '-' + c);
                }
                add(lastChar, c);
                _appendToPat(newPat, lastChar, false);
                newPat.append('-');
                _appendToPat(newPat, c, false);
                lastOp = 0;
                lastChar = NONE;
            } else if (lastOp != 0) {
                // We have <set>&<char> or <char>&<char>
                throw new IllegalArgumentException("Unquoted " + lastOp);
            } else {
                if (lastChar != NONE) {
                    // We have <char><char>
                    add((char) lastChar, (char) lastChar);
                    _appendToPat(newPat, lastChar, false);
                }
                lastChar = c;
            }
        }

        if (lastChar != NONE) {
            add(lastChar, lastChar);
            _appendToPat(newPat, lastChar, false);
        }

//      if (mode == 0) {
//          throw new IllegalArgumentException("Missing '[' in \"" +
//                                             pattern.substring(start) + '"');
//      }

        // Handle unprocessed stuff preceding the closing ']'
        if (lastOp == '-') {
            // Trailing '-' is treated as literal
            add(lastOp, lastOp);
            newPat.append('-');
        } else if (lastOp == '&') {
            throw new IllegalArgumentException("Unquoted trailing " + lastOp);
        }

        newPat.append(']');

        /**
         * If we saw a '^' after the initial '[' of this pattern, then perform
         * the complement.  (Inversion after '[:' is handled elsewhere.)
         */
        if (invert) {
            complement();
        }

        if (mode != 4) {
            throw new IllegalArgumentException("Missing ']'");
        }

//      /**
//       * i indexes the last character we parsed or is pattern.length().  In
//       * the latter case, we have run off the end without finding a closing
//       * ']'.  Otherwise, we know i < pattern.length(), and we set the
//       * ParsePosition to the next character to be parsed.
//       */
//      if (i == limit) {
//          throw new IllegalArgumentException("Missing ']' in \"" +
//                                             pattern.substring(start) + '"');
//      }
  
        pos.setIndex(i);

        // Use the rebuilt pattern (newPat) only if necessary.  Prefer the
        // generated pattern.
        if (rebuildPattern) {
            rebuiltPat.append(newPat.toString());
        } else {
            _generatePattern(rebuiltPat, false);
        }

        if (false) {
            // Debug parser
            System.out.println("UnicodeSet(" +
                               pattern.substring(start, i+1) + ") -> " +
                               com.ibm.util.Utility.escape(toString()));
        }
    }

    //----------------------------------------------------------------
    // Implementation: Generation of Unicode categories
    //----------------------------------------------------------------

    /**
     * Sets this object to the given category, given its name.
     * The category name must be either a two-letter name, such as
     * "Lu", or a one letter name, such as "L".  One-letter names
     * indicate the logical union of all two-letter names that start
     * with that letter.  Case is significant.  If the name starts
     * with the character '^' then the complement of the given
     * character set is returned.
     *
     * Although individual categories such as "Lu" are cached, we do
     * not currently cache single-letter categories such as "L" or
     * complements such as "^Lu" or "^L".  It would be easy to cache
     * these as well in a hashtable should the need arise.
     *
     * NEW: The category name can now be a script name, as defined
     * by UScript.
     */
    private void applyCategory(String catName) {
        boolean invert = (catName.length() > 1 &&
                          catName.charAt(0) == '^');
        if (invert) {
            catName = catName.substring(1);
        }

        boolean match = false;

        // BE CAREFUL not to modify the return value from
        // getCategorySet(int).

        // if we have two characters, search the category map for that
        // code and either construct and return a UnicodeSet from the
        // data in the category map or throw an exception
        if (catName.length() == 2) {
            int i = CATEGORY_NAMES.indexOf(catName);
            if (i>=0 && i%2==0) {
                i /= 2;
                if (i != UNSUPPORTED_CATEGORY) {
                    set(getCategorySet(i));
                    match = true;
                }
            }
        } else if (catName.length() == 1) {
            // if we have one character, search the category map for
            // codes beginning with that letter, and union together
            // all of the matching sets that we find (or throw an
            // exception if there are no matches)
            clear();
            for (int i=0; i<CATEGORY_COUNT; ++i) {
                if (i != UNSUPPORTED_CATEGORY &&
                    CATEGORY_NAMES.charAt(2*i) == catName.charAt(0)) {
                    addAll(getCategorySet(i));
                    match = true;
                }
            }
        }

        if (!match) {
            // TODO: Add caching of these, if desired
            int script = UScript.getCode(catName);
            if (script != UScript.USCRIPT_INVALID_CODE) {
                match = true;
                clear();
                int start = -1;
                int end = -2;
                for (int i=MIN_VALUE; i<=MAX_VALUE; ++i) {
                    if (UScript.getScript(i) == script) {
                        if ((end+1) == i) {
                            end = i;
                        } else {
                            if (start >= 0) {
                                add((char) start, (char) end);
                            }
                            start = end = i;
                        }
                    }
                }
                if (start >= 0) {
                    add((char) start, (char) end);
                }
            }
        }

        if (!match) {
            throw new IllegalArgumentException("Illegal category [:" + catName + ":]");
        }

        if (invert) {
            complement();
        }
    }

    /**
     * Returns an inversion list for the given category.  This list is
     * cached and returned again if this method is called again with
     * the same parameter.
     *
     * Callers MUST NOT MODIFY the returned set.
     */
    private static UnicodeSet getCategorySet(int cat) {
        if (CATEGORY_CACHE[cat] == null) {
            // Walk through all Unicode characters, noting the start
            // and end of each range for which Character.getType(c)
            // returns the given category integer.
            UnicodeSet set = new UnicodeSet();
            int start = -1;
            int end = -2;
            for (int i=MIN_VALUE; i<=MAX_VALUE; ++i) {
                if (Character.getType((char)i) == cat) {
                    if ((end+1) == i) {
                        end = i;
                    } else {
                        if (start >= 0) {
                            set.add((char) start, (char) end);
                        }
                        start = end = i;
                    }
                }
            }
            if (start >= 0) {
                set.add((char) start, (char) end);
            }
            CATEGORY_CACHE[cat] = set;
        }
        return CATEGORY_CACHE[cat];
    }

    //----------------------------------------------------------------
    // Implementation: Utility methods
    //----------------------------------------------------------------

    private void ensureCapacity(int newLen) {
        if (newLen <= list.length) return;
        int[] temp = new int[newLen + GROW_EXTRA];
        System.arraycopy(list, 0, temp, 0, len);
        list = temp;
    }

    private void ensureBufferCapacity(int newLen) {
        if (buffer != null && newLen <= buffer.length) return;
        buffer = new int[newLen + GROW_EXTRA];
    }

    /**
     * Assumes start <= end.
     */
    private int[] range(int start, int end) {
        if (rangeList == null) {
            rangeList = new int[] { start, end+1, HIGH };
        } else {
            rangeList[0] = start;
            rangeList[1] = end+1;
        }
        return rangeList;
    }

    //----------------------------------------------------------------
    // Implementation: Fundamental operations
    //----------------------------------------------------------------

    // polarity = 0, 3 is normal: x xor y
    // polarity = 1, 2: x xor ~y == x === y

    private UnicodeSet xor(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b;
        if (polarity == 1 || polarity == 2) {
            b = LOW;
            if (other[j] == LOW) { // skip base if already LOW
                ++j;
                b = other[j];
            }
        } else {
            b = other[j++];
        }
        // simplest of all the routines
        // sort the values, discarding identicals!
        while (true) {
            if (a < b) {
                buffer[k++] = a;
                a = list[i++];
            } else if (b < a) {
                buffer[k++] = b;
                b = other[j++];
            } else if (a != HIGH) { // at this point, a == b
                // discard both values!
                a = list[i++];
                b = other[j++];
            } else { // DONE!
                buffer[k++] = HIGH;
                len = k;
                break;
            }
        }
        // swap list and buffer
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }

    // polarity = 0 is normal: x union y
    // polarity = 2: x union ~y
    // polarity = 1: ~x union y
    // polarity = 3: ~x union ~y

    private UnicodeSet add(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b = other[j++];
        // change from xor is that we have to check overlapping pairs
        // polarity bit 1 means a is second, bit 2 means b is.
        main:
        while (true) {
            switch (polarity) {
              case 0: // both first; take lower if unequal
                if (a < b) { // take a
                    // Back up over overlapping ranges in buffer[]
                    if (k > 0 && a <= buffer[k-1]) {
                        // Pick latter end value in buffer[] vs. list[]
                        a = max(list[i], buffer[--k]);
                    } else {
                        // No overlap
                        buffer[k++] = a;
                        a = list[i];
                    }
                    i++; // Common if/else code factored out
                    polarity ^= 1;
                } else if (b < a) { // take b
                    if (k > 0 && b <= buffer[k-1]) {
                        b = max(other[j], buffer[--k]);
                    } else {
                        buffer[k++] = b;
                        b = other[j];
                    }
                    j++;
                    polarity ^= 2;
                } else { // a == b, take a, drop b
                    if (a == HIGH) break main;
                    // This is symmetrical; it doesn't matter if
                    // we backtrack with a or b. - liu
                    if (k > 0 && a <= buffer[k-1]) {
                        a = max(list[i], buffer[--k]);
                    } else {
                        // No overlap
                        buffer[k++] = a;
                        a = list[i];
                    }
                    i++;
                    polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 3: // both second; take higher if unequal, and drop other
                if (b <= a) { // take a
                    if (a == HIGH) break main;
                    buffer[k++] = a;
                } else { // take b
                    if (b == HIGH) break main;
                    buffer[k++] = b;
                }
                a = list[i++]; polarity ^= 1;   // factored common code
                b = other[j++]; polarity ^= 2;
                break;
              case 1: // a second, b first; if b < a, overlap
                if (a < b) { // no overlap, take a
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else if (b < a) { // OVERLAP, drop b
                    b = other[j++]; polarity ^= 2;
                } else { // a == b, drop both!
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 2: // a first, b second; if a < b, overlap
                if (b < a) { // no overlap, take b
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else  if (a < b) { // OVERLAP, drop a
                    a = list[i++]; polarity ^= 1;
                } else { // a == b, drop both!
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
            }
        }
        buffer[k++] = HIGH;    // terminate
        len = k;
        // swap list and buffer
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }

    // polarity = 0 is normal: x intersect y
    // polarity = 2: x intersect ~y == set-minus
    // polarity = 1: ~x intersect y
    // polarity = 3: ~x intersect ~y

    private UnicodeSet retain(int[] other, int otherLen, int polarity) {
        ensureBufferCapacity(len + otherLen);
        int i = 0, j = 0, k = 0;
        int a = list[i++];
        int b = other[j++];
        // change from xor is that we have to check overlapping pairs
        // polarity bit 1 means a is second, bit 2 means b is.
        main:
        while (true) {
            switch (polarity) {
              case 0: // both first; drop the smaller
                if (a < b) { // drop a
                    a = list[i++]; polarity ^= 1;
                } else if (b < a) { // drop b
                    b = other[j++]; polarity ^= 2;
                } else { // a == b, take one, drop other
                    if (a == HIGH) break main;
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 3: // both second; take lower if unequal
                if (a < b) { // take a
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else if (b < a) { // take b
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else { // a == b, take one, drop other
                    if (a == HIGH) break main;
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 1: // a second, b first;
                if (a < b) { // NO OVERLAP, drop a
                    a = list[i++]; polarity ^= 1;
                } else if (b < a) { // OVERLAP, take b
                    buffer[k++] = b; b = other[j++]; polarity ^= 2;
                } else { // a == b, drop both!
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
              case 2: // a first, b second; if a < b, overlap
                if (b < a) { // no overlap, drop b
                    b = other[j++]; polarity ^= 2;
                } else  if (a < b) { // OVERLAP, take a
                    buffer[k++] = a; a = list[i++]; polarity ^= 1;
                } else { // a == b, drop both!
                    if (a == HIGH) break main;
                    a = list[i++]; polarity ^= 1;
                    b = other[j++]; polarity ^= 2;
                }
                break;
            }
        }
        buffer[k++] = HIGH;    // terminate
        len = k;
        // swap list and buffer
        int[] temp = list;
        list = buffer;
        buffer = temp;
        pat = null;
        return this;
    }

    private static final int max(int a, int b) {
        return (a > b) ? a : b;
    }
}
