/*
**********************************************************************
*   Copyright (c) 2001, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/Attic/TransliteratorParser.java,v $
* $Date: 2001/11/27 21:36:24 $
* $Revision: 1.14 $
**********************************************************************
*/
package com.ibm.text;

import com.ibm.text.resources.ResourceReader;
import com.ibm.util.Utility;
import java.util.Stack;
import java.util.Vector;
import java.text.ParsePosition;

class TransliteratorParser {

    //----------------------------------------------------------------------
    // Data members
    //----------------------------------------------------------------------

    /**
     * PUBLIC data member containing the parsed data object, or null if
     * there were no rules.
     */
    public RuleBasedTransliterator.Data data;

    /**
     * PUBLIC data member.
     * The block of ::IDs, both at the top and at the bottom.
     * Inserted into these may be additional rules at the
     * idSplitPoint.
     */
    public String idBlock;

    /**
     * PUBLIC data member.
     * In a compound RBT, the index at which the RBT rules are
     * inserted into the ID block.  Index 0 means before any IDs
     * in the block.  Index idBlock.length() means after all IDs
     * in the block.  Index is a string index.
     */
    public int idSplitPoint;

    /**
     * PUBLIC data member containing the parsed compound filter, if any.
     */
    public UnicodeSet compoundFilter;


    // The number of rules parsed.  This tells us if there were
    // any actual transliterator rules, or if there were just ::ID
    // block IDs.
    private int ruleCount;

    private int direction;

    /**
     * Temporary symbol table used during parsing.
     */
    private ParseData parseData;

    /**
     * Temporary vector of set variables.  When parsing is complete, this
     * is copied into the array data.variables.  As with data.variables,
     * element 0 corresponds to character data.variablesBase.
     */
    private Vector variablesVector;

    /**
     * The next available stand-in for variables.  This starts at some point in
     * the private use area (discovered dynamically) and increments up toward
     * <code>variableLimit</code>.  At any point during parsing, available
     * variables are <code>variableNext..variableLimit-1</code>.
     */
    private char variableNext;

    /**
     * The last available stand-in for variables.  This is discovered
     * dynamically.  At any point during parsing, available variables are
     * <code>variableNext..variableLimit-1</code>.  During variable definition
     * we use the special value variableLimit-1 as a placeholder.
     */
    private char variableLimit;

    /**
     * When we encounter an undefined variable, we do not immediately signal
     * an error, in case we are defining this variable, e.g., "$a = [a-z];".
     * Instead, we save the name of the undefined variable, and substitute
     * in the placeholder char variableLimit - 1, and decrement
     * variableLimit.
     */
    private String undefinedVariableName;

    /**
     * The stand-in character for the 'dot' set, represented by '.' in
     * patterns.  This is allocated the first time it is needed, and
     * reused thereafter.
     */
    private int dotStandIn = -1;

    //----------------------------------------------------------------------
    // Constants
    //----------------------------------------------------------------------

    // Indicator for ID blocks
    private static final String ID_TOKEN = "::";
    private static final int ID_TOKEN_LEN = 2;

    // Operators
    private static final char VARIABLE_DEF_OP   = '=';
    private static final char FORWARD_RULE_OP   = '>';
    private static final char REVERSE_RULE_OP   = '<';
    private static final char FWDREV_RULE_OP    = '~'; // internal rep of <> op

    private static final String OPERATORS = "=><";
    private static final String HALF_ENDERS = "=><;";

    // Other special characters
    private static final char QUOTE               = '\'';
    private static final char ESCAPE              = '\\';
    private static final char END_OF_RULE         = ';';
    private static final char RULE_COMMENT_CHAR   = '#';

    private static final char CONTEXT_ANTE        = '{'; // ante{key
    private static final char CONTEXT_POST        = '}'; // key}post
    private static final char CURSOR_POS          = '|';
    private static final char CURSOR_OFFSET       = '@';
    private static final char ANCHOR_START        = '^';

    private static final char KLEENE_STAR         = '*';
    private static final char ONE_OR_MORE         = '+';
    private static final char ZERO_OR_ONE         = '?';

    private static final char DOT                 = '.';
    private static final String DOT_SET           = "[^[:Zp:][:Zl:]\r\n$]";

    // By definition, the ANCHOR_END special character is a
    // trailing SymbolTable.SYMBOL_REF character.
    // private static final char ANCHOR_END       = '$';

    // Segments of the input string are delimited by "(" and ")".  In the
    // output string these segments are referenced as "$1", "$2", etc.
    private static final char SEGMENT_OPEN        = '(';
    private static final char SEGMENT_CLOSE       = ')';

    //----------------------------------------------------------------------
    // class ParseData
    //----------------------------------------------------------------------

    /**
     * This class implements the SymbolTable interface.  It is used
     * during parsing to give UnicodeSet access to variables that
     * have been defined so far.  Note that it uses variablesVector,
     * _not_ data.variables.
     */
    private class ParseData implements SymbolTable {

        /**
         * Implement SymbolTable API.
         */
        public char[] lookup(String name) {
            return (char[]) data.variableNames.get(name);
        }

        /**
         * Implement SymbolTable API.
         */
        public UnicodeMatcher lookupMatcher(int ch) {
            // Note that we cannot use data.lookup() because the
            // set array has not been constructed yet.
            int i = ch - data.variablesBase;
            if (i >= 0 && i < variablesVector.size()) {
                return (UnicodeMatcher) variablesVector.elementAt(i);
            }
            return null;
        }

        /**
         * Implement SymbolTable API.  Parse out a symbol reference
         * name.
         */
        public String parseReference(String text, ParsePosition pos, int limit) {
            int start = pos.getIndex();
            int i = start;
            while (i < limit) {
                char c = text.charAt(i);
                if ((i==start && !Character.isUnicodeIdentifierStart(c)) ||
                    !Character.isUnicodeIdentifierPart(c)) {
                    break;
                }
                ++i;
            }
            if (i == start) { // No valid name chars
                return null;
            }
            pos.setIndex(i);
            return text.substring(start, i);
        }
    }

    //----------------------------------------------------------------------
    // classes RuleBody, RuleArray, and RuleReader
    //----------------------------------------------------------------------

    /**
     * A private abstract class representing the interface to rule
     * source code that is broken up into lines.  Handles the
     * folding of lines terminated by a backslash.  This folding
     * is limited; it does not account for comments, quotes, or
     * escapes, so its use to be limited.
     */
    private static abstract class RuleBody {

        /**
         * Retrieve the next line of the source, or return null if
         * none.  Folds lines terminated by a backslash into the
         * next line, without regard for comments, quotes, or
         * escapes.
         */
        String nextLine() {
            String s = handleNextLine();
            if (s != null &&
                s.length() > 0 &&
                s.charAt(s.length() - 1) == '\\') {

                StringBuffer b = new StringBuffer(s);
                do {
                    b.deleteCharAt(b.length()-1);
                    s = handleNextLine();
                    if (s == null) {
                        break;
                    }
                    b.append(s);
                } while (s.length() > 0 &&
                         s.charAt(s.length() - 1) == '\\');

                s = b.toString();
            }
            return s;
        }

        /**
         * Reset to the first line of the source.
         */
        abstract void reset();

        /**
         * Subclass method to return the next line of the source.
         */
        abstract String handleNextLine();
    };

    /**
     * RuleBody subclass for a String[] array.
     */
    private static class RuleArray extends RuleBody {
        String[] array;
        int i;
        public RuleArray(String[] array) { this.array = array; i = 0; }
        public String handleNextLine() {
            return (i < array.length) ? array[i++] : null;
        }
        public void reset() {
            i = 0;
        }
    };

    /**
     * RuleBody subclass for a ResourceReader.
     */
    private static class RuleReader extends RuleBody {
        ResourceReader reader;
        public RuleReader(ResourceReader reader) { this.reader = reader; }
        public String handleNextLine() {
            try {
                return reader.readLine();
            } catch (java.io.IOException e) {}
            return null;
        }
        public void reset() {
            reader.reset();
        }
    };

    //----------------------------------------------------------------------
    // class RuleHalf
    //----------------------------------------------------------------------

    /**
     * A class representing one side of a rule.  This class knows how to
     * parse half of a rule.  It is tightly coupled to the method
     * TransliteratorParser.parseRule().
     */
    private static class RuleHalf {

        public String text;

        public int cursor = -1; // position of cursor in text
        public int ante = -1;   // position of ante context marker '{' in text
        public int post = -1;   // position of post context marker '}' in text

        public int maxRef = -1; // n where maximum segment ref is $n; 1-based

        // Record the offset to the cursor either to the left or to the
        // right of the key.  This is indicated by characters on the output
        // side that allow the cursor to be positioned arbitrarily within
        // the matching text.  For example, abc{def} > | @@@ xyz; changes
        // def to xyz and moves the cursor to before abc.  Offset characters
        // must be at the start or end, and they cannot move the cursor past
        // the ante- or postcontext text.  Placeholders are only valid in
        // output text.  The length of the ante and post context is
        // determined at runtime, because of supplementals and quantifiers.
        public int cursorOffset = 0; // only nonzero on output side

        // Position of first CURSOR_OFFSET on _right_.  This will be -1
        // for |@, -2 for |@@, etc., and 1 for @|, 2 for @@|, etc.
        private int cursorOffsetPos = 0;

        public boolean anchorStart = false;
        public boolean anchorEnd   = false;

        /**
         * UnicodeMatcher objects corresponding to each segment.
         */
        public Vector segments = new Vector();
        
        /**
         * The segment number from 0..n-1 of the next '(' we see
         * during parsing; 0-based.
         */
        private int nextSegmentNumber = 0;

        /**
         * Parse one side of a rule, stopping at either the limit,
         * the END_OF_RULE character, or an operator.
         * @return the index after the terminating character, or
         * if limit was reached, limit
         */
        public int parse(String rule, int pos, int limit,
                         TransliteratorParser parser) {
            int start = pos;
            StringBuffer buf = new StringBuffer();
            pos = parseSection(rule, pos, limit, parser, buf, false);
            text = buf.toString();

            if (cursorOffset > 0 && cursor != cursorOffsetPos) {
                syntaxError("Misplaced " + CURSOR_POS, rule, start);
            }

            return pos;
        }

        /**
         * Parse a section of one side of a rule, stopping at either
         * the limit, the END_OF_RULE character, an operator, or a
         * segment close character.  This method parses both a
         * top-level rule half and a segment within such a rule half.
         * It calls itself recursively to parse segments and nested
         * segments.
         * @param buf buffer into which to accumulate the rule pattern
         * characters, either literal characters from the rule or
         * standins for UnicodeMatcher objects including segments.
         * @param isSegment if true, then we've already seen a '(' and
         * pos on entry points right after it.  Accumulate everything
         * up to the closing ')', put it in a segment matcher object,
         * generate a standin for it, and add the standin to buf.  As
         * a side effect, update the segments vector with a reference
         * to the segment matcher.  This works recursively for nested
         * segments.  If isSegment is false, just accumulate
         * characters into buf.
         * @return the index after the terminating character, or
         * if limit was reached, limit
         */
        private int parseSection(String rule, int pos, int limit,
                                 TransliteratorParser parser,
                                 StringBuffer buf,
                                 boolean isSegment) {
            int start = pos;
            ParsePosition pp = null;
            int quoteStart = -1; // Most recent 'single quoted string'
            int quoteLimit = -1;
            int varStart = -1; // Most recent $variableReference
            int varLimit = -1;
            int[] iref = new int[1];

            // If isSegment, then bufSegStart is the offset in buf to
            // the first character of the segment we are parsing.
            int bufSegStart = 0;
            int segmentNumber = 0;
            if (isSegment) {
                bufSegStart = buf.length();
                segmentNumber = nextSegmentNumber++;
            }

        main:
            while (pos < limit) {
                // Since all syntax characters are in the BMP, fetching
                // 16-bit code units suffices here.
                char c = rule.charAt(pos++);
                if (Character.isWhitespace(c)) {
                    // Ignore whitespace.  Note that this is not Unicode
                    // spaces, but Java spaces -- a subset, representing
                    // whitespace likely to be seen in code.
                    continue;
                }
                // HALF_ENDERS is all chars that end a rule half: "<>=;"
                if (HALF_ENDERS.indexOf(c) >= 0) {
                    if (isSegment) {
                        syntaxError("Unclosed segment", rule, start);
                    }
                    break main;
                }
                if (anchorEnd) {
                    // Text after a presumed end anchor is a syntax err
                    syntaxError("Malformed variable reference", rule, start);
                }
                if (UnicodeSet.resemblesPattern(rule, pos-1)) {
                    if (pp == null) {
                        pp = new ParsePosition(0);
                    }
                    pp.setIndex(pos-1); // Backup to opening '['
                    buf.append(parser.parseSet(rule, pp));
                    pos = pp.getIndex();                    
                    continue;
                }
                // Handle escapes
                if (c == ESCAPE) {
                    if (pos == limit) {
                        syntaxError("Trailing backslash", rule, start);
                    }
                    iref[0] = pos;
                    int escaped = Utility.unescapeAt(rule, iref);
                    pos = iref[0];
                    if (escaped == -1) {
                        syntaxError("Malformed escape", rule, start);
                    }
                    parser.checkVariableRange(escaped, rule, start);
                    UTF16.append(buf, escaped);
                    continue;
                }
                // Handle quoted matter
                if (c == QUOTE) {
                    int iq = rule.indexOf(QUOTE, pos);
                    if (iq == pos) {
                        buf.append(c); // Parse [''] outside quotes as [']
                        ++pos;
                    } else {
                        /* This loop picks up a segment of quoted text of the
                         * form 'aaaa' each time through.  If this segment
                         * hasn't really ended ('aaaa''bbbb') then it keeps
                         * looping, each time adding on a new segment.  When it
                         * reaches the final quote it breaks.
                         */
                        quoteStart = buf.length();
                        for (;;) {
                            if (iq < 0) {
                                syntaxError("Unterminated quote", rule, start);
                            }
                            buf.append(rule.substring(pos, iq));
                            pos = iq+1;
                            if (pos < limit && rule.charAt(pos) == QUOTE) {
                            // Parse [''] inside quotes as [']
                                iq = rule.indexOf(QUOTE, pos+1);
                            // Continue looping
                            } else {
                                break;
                            }
                        }
                        quoteLimit = buf.length();
                        
                        for (iq=quoteStart; iq<quoteLimit; ++iq) {
                            parser.checkVariableRange(buf.charAt(iq), rule, start);
                        }
                    }
                    continue;
                }

                parser.checkVariableRange(c, rule, start);

                switch (c) {
                    
                //------------------------------------------------------
                // Elements allowed within and out of segments
                //------------------------------------------------------
                case ANCHOR_START:
                    if (buf.length() == 0 && !anchorStart) {
                        anchorStart = true;
                    } else {
                        syntaxError("Misplaced anchor start",
                                    rule, start);
                    }
                    break;
                case SEGMENT_OPEN:
                    pos = parseSection(rule, pos, limit, parser, buf, true);
                    break;
                case SymbolTable.SYMBOL_REF:
                    // Handle variable references and segment references "$1" .. "$9"
                    {
                        // A variable reference must be followed immediately
                        // by a Unicode identifier start and zero or more
                        // Unicode identifier part characters, or by a digit
                        // 1..9 if it is a segment reference.
                        if (pos == limit) {
                            // A variable ref character at the end acts as
                            // an anchor to the context limit, as in perl.
                            anchorEnd = true;
                            break;
                        }
                        // Parse "$1" "$2" .. "$9" .. (no upper limit)
                        c = rule.charAt(pos);
                        int r = Character.digit(c, 10);
                        if (r >= 1 && r <= 9) {
                            ++pos;
                            while (pos < limit) {
                                c = rule.charAt(pos);
                                int d = Character.digit(c, 10);
                                if (d < 0) {
                                    break;
                                }
                                if (r > 214748364 ||
                                    (r == 214748364 && d > 7)) {
                                    syntaxError("Undefined segment reference",
                                                rule, start);
                                }
                                r = 10*r + d;
                            }
                            if (r > maxRef) {
                                maxRef = r;
                            }
                            buf.append(parser.getSegmentStandin(r));
                        } else {
                            if (pp == null) { // Lazy create
                                pp = new ParsePosition(0);
                            }
                            pp.setIndex(pos);
                            String name = parser.parseData.
                                parseReference(rule, pp, limit);
                            if (name == null) {
                                // This means the '$' was not followed by a
                                // valid name.  Try to interpret it as an
                                // end anchor then.  If this also doesn't work
                                // (if we see a following character) then signal
                                // an error.
                                anchorEnd = true;
                                break;
                            }
                            pos = pp.getIndex();
                            // If this is a variable definition statement,
                            // then the LHS variable will be undefined.  In
                            // that case appendVariableDef() will append the
                            // special placeholder char variableLimit-1.
                            varStart = buf.length();
                            parser.appendVariableDef(name, buf);
                            varLimit = buf.length();
                        }
                    }
                    break;
                case DOT:
                    buf.append(parser.getDotStandIn());
                    break;
                case KLEENE_STAR:
                case ONE_OR_MORE:
                case ZERO_OR_ONE:
                    // Quantifiers.  We handle single characters, quoted strings,
                    // variable references, and segments.
                    //  a+      matches  aaa
                    //  'foo'+  matches  foofoofoo
                    //  $v+     matches  xyxyxy if $v == xy
                    //  (seg)+  matches  segsegseg
                    {
                        if (isSegment && buf.length() == bufSegStart) {
                            // The */+ immediately follows '('
                            syntaxError("Misplaced quantifier", rule, start);
                            break;
                        } 
 
                        int qstart, qlimit;
                        // The */+ follows an isolated character or quote
                        // or variable reference
                        if (buf.length() == quoteLimit) {
                            // The */+ follows a 'quoted string'
                            qstart = quoteStart;
                            qlimit = quoteLimit;
                        } else if (buf.length() == varLimit) {
                            // The */+ follows a $variableReference
                            qstart = varStart;
                            qlimit = varLimit;
                        } else {
                            // The */+ follows a single character, possibly
                            // a segment standin
                            qstart = buf.length() - 1;
                            qlimit = qstart + 1;
                        }

                        UnicodeMatcher m =
                            new StringMatcher(buf.toString(), qstart, qlimit,
                                              false, parser.data);
                        int min = 0;
                        int max = Quantifier.MAX;
                        switch (c) {
                        case ONE_OR_MORE:
                            min = 1;
                            break;
                        case ZERO_OR_ONE:
                            min = 0;
                            max = 1;
                            break;
                            // case KLEENE_STAR:
                            //    do nothing -- min, max already set
                        }
                        m = new Quantifier(m, min, max);
                        buf.setLength(qstart);
                        buf.append(parser.generateStandInFor(m));
                    }
                    break;

                //------------------------------------------------------
                // Elements allowed ONLY WITHIN segments
                //------------------------------------------------------
                case SEGMENT_CLOSE:
                    if (isSegment) {
                        // We're done parsing a segment.  The relevant
                        // characters are in buf, starting at offset
                        // bufSegStart.  Extract them into a string
                        // matcher, and replace them with a standin
                        // for that matcher.
                        StringMatcher m =
                            new StringMatcher(buf.substring(bufSegStart),
                                              true, parser.data);
                        // Since we call parseSection() recursively,
                        // nested segments will result in segment i+1
                        // getting parsed and stored before segment i;
                        // be careful with the vector handling here.
                        if ((segmentNumber+1) > segments.size()) {
                            segments.setSize(segmentNumber+1);
                        }
                        segments.setElementAt(m, segmentNumber);
                        buf.setLength(bufSegStart);
                        buf.append(parser.generateStandInFor(m));
                        break main;
                    }
                    // If we aren't in a segment, then a segment close
                    // character is a syntax error.
                    syntaxError("Unquoted special", rule, start);
                    break;

                //------------------------------------------------------
                // Elements allowed ONLY OUTSIDE segments
                //------------------------------------------------------
                case CONTEXT_ANTE:
                    if (isSegment) {
                        syntaxError("Illegal character '" + c + "' in segment", rule, start);
                    }
                    if (ante >= 0) {
                        syntaxError("Multiple ante contexts", rule, start);
                    }
                    ante = buf.length();
                    break;
                case CONTEXT_POST:
                    if (isSegment) {
                        syntaxError("Illegal character '" + c + "' in segment", rule, start);
                    }
                    if (post >= 0) {
                        syntaxError("Multiple post contexts", rule, start);
                    }
                    post = buf.length();
                    break;
                case CURSOR_POS:
                    if (isSegment) {
                        syntaxError("Illegal character '" + c + "' in segment", rule, start);
                    }
                    if (cursor >= 0) {
                        syntaxError("Multiple cursors", rule, start);
                    }
                    cursor = buf.length();
                    break;
                case CURSOR_OFFSET:
                    if (isSegment) {
                        syntaxError("Illegal character '" + c + "' in segment", rule, start);
                    }
                    if (cursorOffset < 0) {
                        if (buf.length() > 0) {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                        --cursorOffset;
                    } else if (cursorOffset > 0) {
                        if (buf.length() != cursorOffsetPos || cursor >= 0) {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                        ++cursorOffset;
                    } else {
                        if (cursor == 0 && buf.length() == 0) {
                            cursorOffset = -1;
                        } else if (cursor < 0) {
                            cursorOffsetPos = buf.length();
                            cursorOffset = 1;
                        } else {
                            syntaxError("Misplaced " + c, rule, start);
                        }
                    }
                    break;

                //------------------------------------------------------
                // Non-special characters
                //------------------------------------------------------
                default:
                    // Disallow unquoted characters other than [0-9A-Za-z]
                    // in the printable ASCII range.  These characters are
                    // reserved for possible future use.
                    if (c >= 0x0021 && c <= 0x007E &&
                        !((c >= '0' && c <= '9') ||
                          (c >= 'A' && c <= 'Z') ||
                          (c >= 'a' && c <= 'z'))) {
                        syntaxError("Unquoted " + c, rule, start);
                    }
                    buf.append(c);
                    break;
                }
            }
            return pos;
        }

        /**
         * Remove context.
         */
        void removeContext() {
            text = text.substring(ante < 0 ? 0 : ante,
                                  post < 0 ? text.length() : post);
            ante = post = -1;
            anchorStart = anchorEnd = false;
        }

        /**
         * Create and return a UnicodeMatcher[] array of segments,
         * or null if there are no segments.
         */
        UnicodeMatcher[] createSegments() {
            return (segments.size() == 0) ? null :
                (UnicodeMatcher[]) segments.toArray(new UnicodeMatcher[segments.size()]);
        }
    }

    //----------------------------------------------------------------------
    // PUBLIC methods
    //----------------------------------------------------------------------

    /**
     * Constructor.
     */
    public TransliteratorParser() {
    }

    /**
     * Parse a set of rules.  After the parse completes, examine the public
     * data members for results.
     */
    public void parse(String rules, int direction) {
        parseRules(new RuleArray(new String[] { rules }), direction);
    }
   
    /**
     * Parse a set of rules.  After the parse completes, examine the public
     * data members for results.
     */
    public void parse(ResourceReader rules, int direction) {
        parseRules(new RuleReader(rules), direction);
    }

    //----------------------------------------------------------------------
    // PRIVATE methods
    //----------------------------------------------------------------------

    /**
     * Parse an array of zero or more rules.  The strings in the array are
     * treated as if they were concatenated together, with rule terminators
     * inserted between array elements if not present already.
     *
     * Any previous rules are discarded.  Typically this method is called exactly
     * once, during construction.
     *
     * The member this.data will be set to null if there are no rules.
     *
     * @exception IllegalArgumentException if there is a syntax error in the
     * rules
     */
    void parseRules(RuleBody ruleArray, int dir) {
        data = new RuleBasedTransliterator.Data();
        direction = dir;
        ruleCount = 0;
        compoundFilter = null;

        // By default, rules use part of the private use area
        // E000..F8FF for variables and other stand-ins.  Currently
        // the range F000..F8FF is typically sufficient.  The 'use
        // variable range' pragma allows rule sets to modify this.
        setVariableRange(0xF000, 0xF8FF);

        variablesVector = new Vector();
        parseData = new ParseData();

        StringBuffer errors = null;
        int errorCount = 0;

        ruleArray.reset();

        StringBuffer idBlockResult = new StringBuffer();
        idSplitPoint = -1;
        // The mode marks whether we are in the header ::id block, the
        // rule block, or the footer ::id block.
        // mode == 0: start: rule->1, ::id->0
        // mode == 1: in rules: rule->1, ::id->2
        // mode == 2: in footer rule block: rule->ERROR, ::id->2
        int mode = 0;

        // The compound filter offset is an index into idBlockResult.
        // If it is 0, then the compound filter occurred at the start,
        // and it is the offset to the _start_ of the compound filter
        // pattern.  Otherwise it is the offset to the _limit_ of the
        // compound filter pattern within idBlockResult.
        this.compoundFilter = null;
        int compoundFilterOffset = -1;

    main:
        for (;;) {
            String rule = ruleArray.nextLine();
            if (rule == null) {
                break;
            }
            int pos = 0;
            int limit = rule.length();
            while (pos < limit) {
                char c = rule.charAt(pos++);
                if (Character.isWhitespace(c)) {
                    // Ignore leading whitespace.  Note that this is not
                    // Unicode spaces, but Java spaces -- a subset,
                    // representing whitespace likely to be seen in code.
                    continue;
                }
                // Skip lines starting with the comment character
                if (c == RULE_COMMENT_CHAR) {
                    pos = rule.indexOf("\n", pos) + 1;
                    if (pos == 0) {
                        break; // No "\n" found; rest of rule is a commnet
                    }
                    continue; // Either fall out or restart with next line
                }
                // Often a rule file contains multiple errors.  It's
                // convenient to the rule author if these are all reported
                // at once.  We keep parsing rules even after a failure, up
                // to a specified limit, and report all errors at once.
                try {
                    // We've found the start of a rule or ID.  c is its first
                    // character, and pos points past c.
                    --pos;
                    // Look for an ID token.  Must have at least ID_TOKEN_LEN + 1
                    // chars left.
                    if ((pos + ID_TOKEN_LEN + 1) <= limit &&
                        rule.regionMatches(pos, ID_TOKEN, 0, ID_TOKEN_LEN)) {
                        pos += ID_TOKEN_LEN;
                        c = rule.charAt(pos);
                        while (UCharacter.isWhitespace(c) && pos < limit) {
                            ++pos;
                            c = rule.charAt(pos);
                        }
                        int lengthBefore = idBlockResult.length();
                        if (mode == 1) {
                            mode = 2;
                            // In the forward direction parseID adds elements at the end.
                            // In the reverse direction parseID adds elements at the start.
                            idSplitPoint = (direction == Transliterator.REVERSE) ? 0 : lengthBefore;
                        }
                        int[] p = new int[] { pos };
                        boolean[] sawDelim = new boolean[1];
                        UnicodeSet[] cpdFilter = new UnicodeSet[1];
                        Transliterator.parseID(rule, idBlockResult, p, sawDelim, cpdFilter, direction, false);
                        if (p[0] == pos || !sawDelim[0]) {
                            // Invalid ::id
                            int i1 = pos + 2;
                            while (i1 < rule.length() && rule.charAt(i1) != ';') {
                                ++i1;
                            }
                            throw new IllegalArgumentException("Invalid ::ID " +
                                                               rule.substring(pos, i1));
                        }
                        if (direction == Transliterator.REVERSE && idSplitPoint >= 0) {
                            // In the reverse direction parseID adds elements at the start.
                            idSplitPoint += idBlockResult.length() - lengthBefore;
                        }
                        if (cpdFilter[0] != null) {
                            if (compoundFilter != null) {
                                // Multiple compound filters
                                throw new IllegalArgumentException("Multiple compound filters");
                            }
                            compoundFilter = cpdFilter[0];
                            compoundFilterOffset = (direction == Transliterator.FORWARD) ?
                                lengthBefore : idBlockResult.length();
                        }
                        pos = p[0];
                    } else if (resemblesPragma(rule, pos, limit)) {
                        int ppp = parsePragma(rule, pos, limit);
                        if (ppp < 0) {
                            throw new IllegalArgumentException("Unrecognized pragma: " +
                                                               rule.substring(pos));
                        }
                        pos = ppp;
                    } else {
                        // Parse a rule
                        pos = parseRule(rule, pos, limit);
                        ++ruleCount;
                        if (mode == 2) {
                            // ::id in illegal position (because a rule
                            // occurred after the ::id footer block)
                            throw new IllegalArgumentException("::ID in illegal position");
                        }
                        mode = 1;
                    }
                } catch (IllegalArgumentException e) {
                    if (errorCount == 30) {
                        errors.append("\nMore than 30 errors; further messages squelched");
                        break main;
                    }
                    if (errors == null) {
                        errors = new StringBuffer(e.getMessage());
                    } else {
                        errors.append("\n" + e.getMessage());
                    }
                    ++errorCount;
                    pos = ruleEnd(rule, pos, limit) + 1; // +1 advances past ';'
                }
            }
        }

        idBlock = idBlockResult.toString();

        // Convert the set vector to an array
        data.variables = new UnicodeMatcher[variablesVector.size()];
        variablesVector.copyInto(data.variables);
        variablesVector = null;

        // Do more syntax checking and index the rules
        try {
            if (compoundFilter != null) {
                if ((direction == Transliterator.FORWARD &&
                     compoundFilterOffset != 0) ||
                    (direction == Transliterator.REVERSE &&
                     compoundFilterOffset != idBlock.length())) {
                    throw new IllegalArgumentException("Compound filters misplaced");
                }
            }

            data.ruleSet.freeze();

            if (idSplitPoint < 0) {
                idSplitPoint = idBlock.length();
            }

            if (ruleCount == 0) {
                data = null;
            }
        } catch (IllegalArgumentException e) {
            if (errors == null) {
                errors = new StringBuffer(e.getMessage());
            } else {
                errors.append("\n").append(e.getMessage());
            }
        }

        if (errors != null) {
            throw new IllegalArgumentException(errors.toString());
        }
    }

    /**
     * MAIN PARSER.  Parse the next rule in the given rule string, starting
     * at pos.  Return the index after the last character parsed.  Do not
     * parse characters at or after limit.
     *
     * Important:  The character at pos must be a non-whitespace character
     * that is not the comment character.
     *
     * This method handles quoting, escaping, and whitespace removal.  It
     * parses the end-of-rule character.  It recognizes context and cursor
     * indicators.  Once it does a lexical breakdown of the rule at pos, it
     * creates a rule object and adds it to our rule list.
     *
     * This method is tightly coupled to the inner class RuleHalf.
     */
    private int parseRule(String rule, int pos, int limit) {
        // Locate the left side, operator, and right side
        int start = pos;
        char operator = 0;

        RuleHalf left  = new RuleHalf();
        RuleHalf right = new RuleHalf();

        undefinedVariableName = null;
        pos = left.parse(rule, pos, limit, this);

        if (pos == limit ||
            OPERATORS.indexOf(operator = rule.charAt(--pos)) < 0) {
            syntaxError("No operator pos=" + pos, rule, start);
        }
        ++pos;

        // Found an operator char.  Check for forward-reverse operator.
        if (operator == REVERSE_RULE_OP &&
            (pos < limit && rule.charAt(pos) == FORWARD_RULE_OP)) {
            ++pos;
            operator = FWDREV_RULE_OP;
        }

        pos = right.parse(rule, pos, limit, this);

        if (pos < limit) {
            if (rule.charAt(--pos) == END_OF_RULE) {
                ++pos;
            } else {
                // RuleHalf parser must have terminated at an operator
                syntaxError("Unquoted operator", rule, start);
            }
        }

        if (operator == VARIABLE_DEF_OP) {
            // LHS is the name.  RHS is a single character, either a literal
            // or a set (already parsed).  If RHS is longer than one
            // character, it is either a multi-character string, or multiple
            // sets, or a mixture of chars and sets -- syntax error.

            // We expect to see a single undefined variable (the one being
            // defined).
            if (undefinedVariableName == null) {
                syntaxError("Missing '$' or duplicate definition", rule, start);
            }
            if (left.text.length() != 1 || left.text.charAt(0) != variableLimit) {
                syntaxError("Malformed LHS", rule, start);
            }
            if (left.anchorStart || left.anchorEnd ||
                right.anchorStart || right.anchorEnd) {
                syntaxError("Malformed variable def", rule, start);
            }
            // We allow anything on the right, including an empty string.
            int n = right.text.length();
            char[] value = new char[n];
            right.text.getChars(0, n, value, 0);
            data.variableNames.put(undefinedVariableName, value);

            ++variableLimit;
            return pos;
        }

        // If this is not a variable definition rule, we shouldn't have
        // any undefined variable names.
        if (undefinedVariableName != null) {
            syntaxError("Undefined variable $" + undefinedVariableName,
                        rule, start);
        }

        // If the direction we want doesn't match the rule
        // direction, do nothing.
        if (operator != FWDREV_RULE_OP &&
            ((direction == Transliterator.FORWARD) != (operator == FORWARD_RULE_OP))) {
            return pos;
        }

        // Transform the rule into a forward rule by swapping the
        // sides if necessary.
        if (direction == Transliterator.REVERSE) {
            RuleHalf temp = left;
            left = right;
            right = temp;
        }

        // Remove non-applicable elements in forward-reverse
        // rules.  Bidirectional rules ignore elements that do not
        // apply.
        if (operator == FWDREV_RULE_OP) {
            right.removeContext();
            right.segments.removeAllElements();
            left.cursor = left.maxRef = -1;
            left.cursorOffset = 0;
        }

        // Normalize context
        if (left.ante < 0) {
            left.ante = 0;
        }
        if (left.post < 0) {
            left.post = left.text.length();
        }

        // Context is only allowed on the input side.  Cursors are only
        // allowed on the output side.  Segment delimiters can only appear
        // on the left, and references on the right.  Cursor offset
        // cannot appear without an explicit cursor.  Cursor offset
        // cannot place the cursor outside the limits of the context.
        // Anchors are only allowed on the input side.
        if (right.ante >= 0 || right.post >= 0 || left.cursor >= 0 ||
            right.segments.size() > 0 || left.maxRef >= 0 ||
            (right.cursorOffset != 0 && right.cursor < 0) ||
            // - The following two checks were used to ensure that the
            // - the cursor offset stayed within the ante- or postcontext.
            // - However, with the addition of quantifiers, we have to
            // - allow arbitrary cursor offsets and do runtime checking.
            //(right.cursorOffset > (left.text.length() - left.post)) ||
            //(-right.cursorOffset > left.ante) ||
            right.anchorStart || right.anchorEnd ||
            !isValidOutput(right.text) ||
            left.ante > left.post) {
            syntaxError("Malformed rule", rule, start);
        }

        // Check integrity of segments and segment references.  Each
        // segment's start must have a corresponding limit, and the
        // references must not refer to segments that do not exist.
        if (right.maxRef > left.segments.size()) {
            syntaxError("Undefined segment reference $" + right.maxRef, rule, start);
        }

        data.ruleSet.addRule(new TransliterationRule(
                                     left.text, left.ante, left.post,
                                     right.text, right.cursor, right.cursorOffset,
                                     left.createSegments(),
                                     left.anchorStart, left.anchorEnd,
                                     data));

        return pos;
    }

    /**
     * Return true if the given string looks like valid output, that is,
     * does not contain quantifiers or other special input-only elements.
     */
    private boolean isValidOutput(String output) {
        for (int i=0; i<output.length(); ++i) {
            int c = UTF16.charAt(output, i);
            i += UTF16.getCharCount(c);
            if (parseData.lookupMatcher(c) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the variable range to [start, end] (inclusive).
     */
    private void setVariableRange(int start, int end) {
        if (start > end || start < 0 || end > 0xFFFF) {
            throw new IllegalArgumentException("Invalid variable range " + start + ", " + end);
        }
        
        // Segment references work down; variables work up.  We don't
        // know how many of each we will need.
        data.segmentBase = (char) end;
        data.segmentCount = 0;
        data.variablesBase = variableNext = (char) start; // first private use
        variableLimit = (char) (end + 1);
    }

    /**
     * Assert that the given character is NOT within the variable range.
     * If it is, signal an error.  This is neccesary to ensure that the
     * variable range does not overlap characters used in a rule.
     */
    private void checkVariableRange(int ch, String rule, int start) {
        if (ch >= data.variablesBase && ch < variableLimit) {
            syntaxError("Variable range character in rule", rule, start);
        }
    }

    /**
     * Set the maximum backup to 'backup', in response to a pragma
     * statement.
     */
    private void pragmaMaximumBackup(int backup) {
        //TODO Finish
        throw new IllegalArgumentException("use maximum backup pragma not implemented yet");
    }

    /**
     * Begin normalizing all rules using the given mode, in response
     * to a pragma statement.
     */
    private void pragmaNormalizeRules(Normalizer.Mode mode) {
        //TODO Finish
        throw new IllegalArgumentException("use normalize rules pragma not implemented yet");
    }

    /**
     * Return true if the given rule looks like a pragma.
     * @param pos offset to the first non-whitespace character
     * of the rule.
     * @param limit pointer past the last character of the rule.
     */
    static boolean resemblesPragma(String rule, int pos, int limit) {
        // Must start with /use\s/i
        return Utility.parsePattern(rule, pos, limit, "use ", null) >= 0;
    }

    /**
     * Parse a pragma.  This method assumes resemblesPragma() has
     * already returned true.
     * @param pos offset to the first non-whitespace character
     * of the rule.
     * @param limit pointer past the last character of the rule.
     * @return the position index after the final ';' of the pragma,
     * or -1 on failure.
     */
    private int parsePragma(String rule, int pos, int limit) {
        int[] array = new int[2];

        // resemblesPragma() has already returned true, so we
        // know that pos points to /use\s/i; we can skip 4 characters
        // immediately
        pos += 4;
        
        // Here are the pragmas we recognize:
        // use variable range 0xE000 0xEFFF;
        // use maximum backup 16;
        // use nfd rules;
        int p = Utility.parsePattern(rule, pos, limit, "~variable range # #~;", array);
        if (p >= 0) {
            setVariableRange(array[0], array[1]);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~maximum backup #~;", array);
        if (p >= 0) {
            pragmaMaximumBackup(array[0]);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~nfd rules~;", null);
        if (p >= 0) {
            pragmaNormalizeRules(Normalizer.DECOMP);
            return p;
        }

        p = Utility.parsePattern(rule, pos, limit, "~nfc rules~;", null);
        if (p >= 0) {
            pragmaNormalizeRules(Normalizer.COMPOSE);
            return p;
        }

        // Syntax error: unable to parse pragma
        return -1;
    }

    /**
     * Throw an exception indicating a syntax error.  Search the rule string
     * for the probable end of the rule.  Of course, if the error is that
     * the end of rule marker is missing, then the rule end will not be found.
     * In any case the rule start will be correctly reported.
     * @param msg error description
     * @param rule pattern string
     * @param start position of first character of current rule
     */
    static final void syntaxError(String msg, String rule, int start) {
        int end = ruleEnd(rule, start, rule.length());
        throw new IllegalArgumentException(msg + " in \"" +
                                           Utility.escape(rule.substring(start, end)) + '"');
    }

    static final int ruleEnd(String rule, int start, int limit) {
        int end = quotedIndexOf(rule, start, limit, ";");
        if (end < 0) {
            end = limit;
        }
        return end;
    }

    /**
     * Parse a UnicodeSet out, store it, and return the stand-in character
     * used to represent it.
     */
    private final char parseSet(String rule, ParsePosition pos) {
        UnicodeSet set = new UnicodeSet(rule, pos, parseData);
        if (variableNext >= variableLimit) {
            throw new RuntimeException("Private use variables exhausted");
        }
        set.compact();
        return generateStandInFor(set);
    }

    /**
     * Generate and return a stand-in for a new UnicodeMatcher.  Store
     * the matcher.
     */
    char generateStandInFor(UnicodeMatcher matcher) {
        // assert(matcher != null);
        if (variableNext >= variableLimit) {
            throw new RuntimeException("Variable range exhausted");
        }
        variablesVector.addElement(matcher);
        return variableNext++;
    }

    /**
     * Return the stand-in for the dot set.  It is allocated the first
     * time and reused thereafter.
     */
    char getDotStandIn() {
        if (dotStandIn == -1) {
            dotStandIn = generateStandInFor(new UnicodeSet(DOT_SET));
        }
        return (char) dotStandIn;
    }

    /**
     * Append the value of the given variable name to the given
     * StringBuffer.
     * @exception IllegalArgumentException if the name is unknown.
     */
    private void appendVariableDef(String name, StringBuffer buf) {
        char[] ch = (char[]) data.variableNames.get(name);
        if (ch == null) {
            // We allow one undefined variable so that variable definition
            // statements work.  For the first undefined variable we return
            // the special placeholder variableLimit-1, and save the variable
            // name.
            if (undefinedVariableName == null) {
                undefinedVariableName = name;
                if (variableNext >= variableLimit) {
                    throw new RuntimeException("Private use variables exhausted");
                }
                buf.append((char) --variableLimit);
            } else {
                throw new IllegalArgumentException("Undefined variable $"
                                                   + name);
            }
        } else {
            buf.append(ch);
        }
    }

    char getSegmentStandin(int r) {
        // assert(r>=1);
        if (r > data.segmentCount) {
            data.segmentCount = r;
            variableLimit = (char) (data.segmentBase - r + 1);
            if (variableNext >= variableLimit) {
                throw new IllegalArgumentException("Too many variables / segments");
            }
        }
        return data.getSegmentStandin(r);
    }

    /**
     * Returns the index of the first character in a set, ignoring quoted text.
     * For example, in the string "abc'hide'h", the 'h' in "hide" will not be
     * found by a search for "h".  Unlike String.indexOf(), this method searches
     * not for a single character, but for any character of the string
     * <code>setOfChars</code>.
     * @param text text to be searched
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= text.length()</code>.
     * @param setOfChars string with one or more distinct characters
     * @return Offset of the first character in <code>setOfChars</code>
     * found, or -1 if not found.
     * @see #indexOf
     */
    private static int quotedIndexOf(String text, int start, int limit,
                                     String setOfChars) {
        for (int i=start; i<limit; ++i) {
            char c = text.charAt(i);
            if (c == ESCAPE) {
                ++i;
            } else if (c == QUOTE) {
                while (++i < limit
                       && text.charAt(i) != QUOTE) {}
            } else if (setOfChars.indexOf(c) >= 0) {
                return i;
            }
        }
        return -1;
    }
}

//eof
