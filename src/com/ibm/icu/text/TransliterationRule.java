/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/TransliterationRule.java,v $ 
 * $Date: 2001/09/19 17:43:38 $ 
 * $Revision: 1.27 $
 *
 *****************************************************************************************
 */
package com.ibm.text;

import com.ibm.util.Utility;

/**
 * A transliteration rule used by
 * <code>RuleBasedTransliterator</code>.
 * <code>TransliterationRule</code> is an immutable object.
 *
 * <p>A rule consists of an input pattern and an output string.  When
 * the input pattern is matched, the output string is emitted.  The
 * input pattern consists of zero or more characters which are matched
 * exactly (the key) and optional context.  Context must match if it
 * is specified.  Context may be specified before the key, after the
 * key, or both.  The key, preceding context, and following context
 * may contain variables.  Variables represent a set of Unicode
 * characters, such as the letters <i>a</i> through <i>z</i>.
 * Variables are detected by looking up each character in a supplied
 * variable list to see if it has been so defined. 
 *
 * <p>A rule may contain segments in its input string and segment references in
 * its output string.  A segment is a substring of the input pattern, indicated
 * by an offset and limit.  The segment may span the preceding or following
 * context.  A segment reference is a special character in the output string
 * that causes a segment of the input string (not the input pattern) to be
 * copied to the output string.  The range of special characters that represent
 * segment references is defined by RuleBasedTransliterator.Data.
 *
 * <p>Example: The rule "([a-z]) . ([0-9]) > $2 . $1" will change the input
 * string "abc.123" to "ab1.c23".
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 * @version $RCSfile: TransliterationRule.java,v $ $Revision: 1.27 $ $Date: 2001/09/19 17:43:38 $
 */
class TransliterationRule {
    /**
     * Constant returned by <code>getMatchDegree()</code> indicating a mismatch
     * between the text and this rule.  One or more characters of the context or
     * key do not match the text.
     * @see #getMatchDegree
     */
    public static final int MISMATCH      = 0;

    /**
     * Constant returned by <code>getMatchDegree()</code> indicating a partial
     * match between the text and this rule.  All characters of the text match
     * the corresponding context or key, but more characters are required for a
     * complete match.  There are some key or context characters at the end of
     * the pattern that remain unmatched because the text isn't long enough.
     * @see #getMatchDegree
     */
    public static final int PARTIAL_MATCH = 1;

    /**
     * Constant returned by <code>getMatchDegree()</code> indicating a complete
     * match between the text and this rule.  The text matches all context and
     * key characters.
     * @see #getMatchDegree
     */
    public static final int FULL_MATCH    = 2;

    /**
     * The string that must be matched, consisting of the anteContext, key,
     * and postContext, concatenated together, in that order.  Some components
     * may be empty (zero length).
     * @see anteContextLength
     * @see keyLength
     */
    private String pattern;

    /**
     * The string that is emitted if the key, anteContext, and postContext
     * are matched.
     */
    private String output;

    /**
     * Array of segments.  These are segments of the input string that may be
     * referenced and appear in the output string.  Each segment is stored as an
     * offset, limit pair.  Segments are referenced by a 1-based index;
     * reference i thus includes characters at offset segments[2*i-2] to
     * segments[2*i-1]-1 in the pattern string.
     *
     * In the output string, a segment reference is indicated by a character in
     * a special range, as defined by RuleBasedTransliterator.Data.
     *
     * Most rules have no segments, in which case segments is null, and the
     * output string need not be checked for segment reference characters.
     */
    private int[] segments;

    /**
     * The length of the string that must match before the key.  If
     * zero, then there is no matching requirement before the key.
     * Substring [0,anteContextLength) of pattern is the anteContext.
     */
    private int anteContextLength;

    /**
     * The length of the key.  Substring [anteContextLength,
     * anteContextLength + keyLength) is the key.
     */
    private int keyLength;

    /**
     * The position of the cursor after emitting the output string, from 0 to
     * output.length().  For most rules with no special cursor specification,
     * the cursorPos is output.length().
     */
    private int cursorPos;

    private RuleBasedTransliterator.Data data;

    /**
     * The character at index i, where i < contextStart || i >= contextLimit,
     * is ETHER.  This allows explicit matching by rules and UnicodeSets
     * of text outside the context.  In traditional terms, this allows anchoring
     * at the start and/or end.
     */
    static final char ETHER = '\uFFFF';

    private static final char APOSTROPHE = '\'';
    private static final char BACKSLASH  = '\\';

    private static final String COPYRIGHT =
        "\u00A9 IBM Corporation 1999. All rights reserved.";

    /**
     * Construct a new rule with the given input, output text, and other
     * attributes.  A cursor position may be specified for the output text.
     * @param input input string, including key and optional ante and
     * post context
     * @param anteContextPos offset into input to end of ante context, or -1 if
     * none.  Must be <= input.length() if not -1.
     * @param postContextPos offset into input to start of post context, or -1
     * if none.  Must be <= input.length() if not -1, and must be >=
     * anteContextPos.
     * @param output output string
     * @param cursorPos offset into output at which cursor is located, or -1 if
     * none.  If less than zero, then the cursor is placed after the
     * <code>output</code>; that is, -1 is equivalent to
     * <code>output.length()</code>.  If greater than
     * <code>output.length()</code> then an exception is thrown.
     * @param cursorOffset an offset to be added to cursorPos to position the
     * cursor either in the ante context, if < 0, or in the post context, if >
     * 0.  For example, the rule "abc{def} > | @@@ xyz;" changes "def" to
     * "xyz" and moves the cursor to before "a".  It would have a cursorOffset
     * of -3.
     * @param segs array of 2n integers.  Each of n pairs consists of offset,
     * limit for a segment of the input string.  Characters in the output string
     * refer to these segments if they are in a special range determined by the
     * associated RuleBasedTransliterator.Data object.  May be null if there are
     * no segments.  The caller is responsible for validating that segments
     * are well-formed.
     * @param anchorStart true if the the rule is anchored on the left to
     * the context start
     * @param anchorEnd true if the rule is anchored on the right to the
     * context limit
     */
    public TransliterationRule(String input,
                               int anteContextPos, int postContextPos,
                               String output,
                               int cursorPos, int cursorOffset,
                               int[] segs,
                               boolean anchorStart, boolean anchorEnd,
                               RuleBasedTransliterator.Data theData) {
        // Do range checks only when warranted to save time
        if (anteContextPos < 0) {
            anteContextLength = 0;
        } else {
            if (anteContextPos > input.length()) {
                throw new IllegalArgumentException("Invalid ante context");
            }
            anteContextLength = anteContextPos;
        }
        if (postContextPos < 0) {
            keyLength = input.length() - anteContextLength;
        } else {
            if (postContextPos < anteContextLength ||
                postContextPos > input.length()) {
                throw new IllegalArgumentException("Invalid post context");
            }
            keyLength = postContextPos - anteContextLength;
        }
        if (cursorPos < 0) {
            cursorPos = output.length();
        }
        if (cursorPos > output.length()) {
            throw new IllegalArgumentException("Invalid cursor position");
        }
        this.cursorPos = cursorPos + cursorOffset;
        pattern = input;
        this.output = output;
        // We don't validate the segments array.  The caller must
        // guarantee that the segments are well-formed.
        this.segments = segs;

        // Implement anchors by inserting an ETHER character on the
        // left or right.  If on the left, then the indices must be
        // incremented.  If on the right, no index change is
        // necessary.
        if (anchorStart || anchorEnd) {
            StringBuffer buf = new StringBuffer();
            if (anchorStart) {
                buf.append(ETHER);
                ++anteContextLength;
                // Adjust segment offsets
                if (segments != null) {
                    for (int i=0; i<segments.length; ++i) {
                        ++segments[i];
                    }
                }
            }
            buf.append(input);
            if (anchorEnd) {
                buf.append(ETHER);
            }
            pattern = buf.toString();
        }

        data = theData;
    }

    /**
     * Construct a new rule with the given input, output text, and other
     * attributes.  A cursor position may be specified for the output text.
     * @param input input string, including key and optional ante and
     * post context
     * @param anteContextPos offset into input to end of ante context, or -1 if
     * none.  Must be <= input.length() if not -1.
     * @param postContextPos offset into input to start of post context, or -1
     * if none.  Must be <= input.length() if not -1, and must be >=
     * anteContextPos.
     * @param output output string
     * @param cursorPos offset into output at which cursor is located, or -1 if
     * none.  If less than zero, then the cursor is placed after the
     * <code>output</code>; that is, -1 is equivalent to
     * <code>output.length()</code>.  If greater than
     * <code>output.length()</code> then an exception is thrown.
     */
    //public TransliterationRule(String input,
    //                           int anteContextPos, int postContextPos,
    //                           String output,
    //                           int cursorPos) {
    //    this(input, anteContextPos, postContextPos,
    //         output, cursorPos, 0, null, false, false);
    //}

    public void setData(RuleBasedTransliterator.Data theData) {
        data = theData;
    }

    /**
     * Return the position of the cursor within the output string.
     * @return a value from 0 to <code>getOutput().length()</code>, inclusive.
     */
    public int getCursorPos() {
        return cursorPos;
    }

    /**
     * Return the preceding context length.  This method is needed to
     * support the <code>Transliterator</code> method
     * <code>getMaximumContextLength()</code>.
     */
    public int getAnteContextLength() {
        return anteContextLength;
    }

    /**
     * Internal method.  Returns 8-bit index value for this rule.
     * This is the low byte of the first character of the key,
     * unless the first character of the key is a set.  If it's a
     * set, or otherwise can match multiple keys, the index value is -1.
     */
    final int getIndexValue(RuleBasedTransliterator.Data variables) {
        if (anteContextLength == pattern.length()) {
            // A pattern with just ante context {such as foo)>bar} can
            // match any key.
            return -1;
        }
        char c = pattern.charAt(anteContextLength);
        return variables.lookupSet(c) == null ? (c & 0xFF) : -1;
    }

    /**
     * Do a replacement of the input pattern with the output text in
     * the given string, at the given offset.  This method assumes
     * that a match has already been found in the given text at the
     * given position.
     * @param text the text containing the substring to be replaced
     * @param offset the offset into the text at which the pattern
     * matches.  This is the offset to the point after the ante
     * context, if any, and before the match string and any post
     * context.
     * @param data the RuleBasedTransliterator.Data object specifying
     * context for this transliterator.
     * @return the change in the length of the text
     */
    public int replace(Replaceable text, int offset,
                       RuleBasedTransliterator.Data data) {
        if (segments == null) {
            text.replace(offset, offset + keyLength, output);
            return output.length() - keyLength;
        } else {
            /* When there are segments to be copied, use the Replaceable.copy()
             * API in order to retain out-of-band data.  Copy everything to the
             * point after the key, then delete the key.  That is, copy things
             * into offset + keyLength, then replace offset .. offset +
             * keyLength with the empty string.
             *
             * Minimize the number of calls to Replaceable.replace() and
             * Replaceable.copy().
             */
            int textStart = offset - anteContextLength;
            int dest = offset + keyLength; // copy new text to here
            StringBuffer buf = new StringBuffer();
            for (int i=0; i<output.length(); ++i) {
                char c = output.charAt(i);
                int b = data.lookupSegmentReference(c);
                if (b < 0) {
                    // Accumulate straight (non-segment) text.
                    buf.append(c);
                } else {
                    // Insert any accumulated straight text.
                    if (buf.length() > 0) {
                        text.replace(dest, dest, buf.toString());
                        dest += buf.length();
                        buf.setLength(0);
                    }
                    // Copy segment with out-of-band data
                    b *= 2;
                    text.copy(textStart + segments[b],
                              textStart + segments[b+1], dest);
                    dest += segments[b+1] - segments[b];
                }
                
            }
            // Insert any accumulated straight text.
            if (buf.length() > 0) {
                text.replace(dest, dest, buf.toString());
                dest += buf.length();
            }
            // Delete the key
            text.replace(offset, offset + keyLength, "");
            return dest - (offset + keyLength) - keyLength;
        }
    }

    /**
     * Internal method.  Returns true if this rule matches the given
     * index value.  The index value is an 8-bit integer, 0..255,
     * representing the low byte of the first character of the key.
     * It matches this rule if it matches the first character of the
     * key, or if the first character of the key is a set, and the set
     * contains any character with a low byte equal to the index
     * value.  If the rule contains only ante context, as in foo)>bar,
     * then it will match any key.
     */
    final boolean matchesIndexValue(int v, RuleBasedTransliterator.Data variables) {
        if (anteContextLength == pattern.length()) {
            // A pattern with just ante context {such as foo)>bar} can
            // match any key.
            return true;
        }
        char c = pattern.charAt(anteContextLength);
        UnicodeSet set = variables.lookupSet(c);
        return set == null ? (c & 0xFF) == v : set.containsIndexValue(v);
    }

    /**
     * Return true if this rule masks another rule.  If r1 masks r2 then
     * r1 matches any input string that r2 matches.  If r1 masks r2 and r2 masks
     * r1 then r1 == r2.  Examples: "a>x" masks "ab>y".  "a>x" masks "a[b]>y".
     * "[c]a>x" masks "[dc]a>y".
     */
    public boolean masks(TransliterationRule r2) {
        /* Rule r1 masks rule r2 if the string formed of the
         * antecontext, key, and postcontext overlaps in the following
         * way:
         *
         * r1:      aakkkpppp
         * r2:     aaakkkkkpppp
         *            ^
         * 
         * The strings must be aligned at the first character of the
         * key.  The length of r1 to the left of the alignment point
         * must be <= the length of r2 to the left; ditto for the
         * right.  The characters of r1 must equal (or be a superset
         * of) the corresponding characters of r2.  The superset
         * operation should be performed to check for UnicodeSet
         * masking.
         */

        /* LIMITATION of the current mask algorithm: Some rule
         * maskings are currently not detected.  For example,
         * "{Lu}]a>x" masks "A]a>y".  This can be added later. TODO
         */

        int left = anteContextLength;
        int left2 = r2.anteContextLength;
        int right = pattern.length() - left;
        int right2 = r2.pattern.length() - left2;
        return left <= left2 && right <= right2 &&
            r2.pattern.substring(left2 - left).startsWith(pattern);
    }

    /**
     * Return a string representation of this object.
     * @return string representation of this object
     */
    public String toString() {
        return getClass().getName() + '{'
            + Utility.escape((anteContextLength > 0 ? (pattern.substring(0, anteContextLength) +
                                              " {") : "")
                     + pattern.substring(anteContextLength, anteContextLength + keyLength)
                     + (anteContextLength + keyLength < pattern.length() ?
                        ("} " + pattern.substring(anteContextLength + keyLength)) : "")
                     + " > "
                     + (cursorPos < output.length()
                        ? (output.substring(0, cursorPos) + '|' + output.substring(cursorPos))
                        : output))
            + '}';
    }

    /**
     * Return true if this rule matches the given text.
     * @param text the text, both translated and untranslated
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= text.length()</code>.
     * @param cursor position at which to translate next, representing offset
     * into text.  This value must be between <code>start</code> and
     * <code>limit</code>.
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     */
    public final boolean matches(Replaceable text,
                                 Transliterator.Position pos,
                                 RuleBasedTransliterator.Data variables,
                                 UnicodeFilter filter) {
        // Match anteContext, key, and postContext
        int cursor = pos.start - anteContextLength;
        // Quick length check; this is a performance win for long rules.
        // Widen by one (on both sides) to allow anchor matching.
        if (cursor < (pos.contextStart - 1)
            || (cursor + pattern.length()) > (pos.contextLimit + 1)) {
            return false;
        }
        for (int i=0; i<pattern.length(); ++i, ++cursor) {
            if (!charMatches(pattern.charAt(i), text, cursor, pos,
                             variables, filter)) {
                return false;
            }
        }
        return true;
    }

//|    /**
//|     * Array of quantifiers.  Each quantifier is represented by 4
//|     * integers: The start and limit (in the pattern), the minimum
//|     * count, and the maximum count.  Counts are inclusive.
//|     * quant.length is always a multiple of 4.  quant may be null.  If
//|     * quant is not null, it must have a length >= 4.  Quants are
//|     * arranged in order of increasing start index, and secondarily in
//|     * order of increasing limit index.  They may be nested but they
//|     * may not otherwise overlap.
//|     */
//|    private int[] quant;
//|
//|    /**
//|     */
//|    boolean matchAndReplace(Replaceable text,
//|                            Transliterator.Position pos,
//|                            RuleBasedTransliterator.Data data) {
//|        // Set the cursor to point to the start of the anteContext.
//|        // The textPos is an index into the source text.
//|        int textPos = pos.start - anteContextLength;
//|        
//|        int patternLen = pattern.length();
//|
//|        // patternPos is the relative position in the pattern text, from
//|        // 0..patternLen-1.
//|        int patternPos = 0;
//|
//|        // Local array of match data.  Match i corresponds to quant i.
//|        // Each match is described by 2 integers: match start (in the
//|        // source text) and match limit.  If the match is empty then
//|        // match start == match limit.  Match count is not stored; if
//|        // the count fell in the legal range, we accept it; if not, we
//|        // return with a match failure.  We also store two integers
//|        // at the start; [0] is the index to the next quant to be
//|        // matched, and [1] is unused.
//|        int[] matchState = null;
//|
//|        int iQuant = 0;
//|        int quantStart = -1;
//|
//|        if (quant != null) {
//|            quantStart = quant[iQuant];
//|
//|            matchState = new int[2 + (quant.length / 2)];
//|            for (int i=0; i<matchState.length; ++i) {
//|                matchState[i] = -1;
//|            }
//|
//|            matchState[0] = 4;
//|        }
//|
//|        while (patternPos < patternLen) {
//|            if (patternPos == quantStart) {
//|                // Match a quant, including repetitions and nested quants
//|                int newTextPos = matchQuant(text, pos, data,
//|                                           textPos, iQuant, matchState);
//|                if (newTextPos < 0) {
//|                    // Match failure
//|                    return newTextPos;
//|                }
//|
//|                // Match success
//|                textPos = newTextPos;
//|
//|                // Update patternPos to point after the quant we just matched
//|                patternPos = quant[iQuant+1];
//|
//|                // Update the next quant
//|                iQuant = matchState[0];
//|                if (iQuant < quant.length) {
//|                    quantStart = quant[iQuant];
//|                    matchState[0] += 4;
//|                } else {
//|                    quantStart = -1; // No more quants
//|                }
//|
//|                continue;
//|            }
//|
//|            // Do a single-character match test, with the filtering etc.
//|            // embodied in the Replaceable object.
//|            if (!charMatches(pattern.charAt(patternPos), text, textPos, data)) {
//|                // On match failure, return
//|                return false;
//|            }
//|
//|            ++textPos;
//|            ++patternPos;
//|        }
//|
//|        // We've successfully matched the pattern.  All the match data
//|        // is in matchState[].
//|    }
//|
//|    /**
//|     * @param matchState stores the current match status.  For
//|     * quant i, matchState[2+2*i] stores the start and
//|     * matchState[3+2*i] stores the limit index in the matched
//|     * source text.  matchState[0] stores the next unmatched
//|     * quant index * 4.
//|     */
//|    private int matchQuant(Replaceable text,
//|                           Transliterator.Position pos,
//|                           RuleBasedTransliterator.Data data,
//|                           int textPos,
//|                           int iQuant,
//|                           int[] matchState) {
//|        // assert(quant != null);
//|        // assert(iQuant < quant.length);
//|        // assert(matchState != null);
//|        // assert(matchState.length == quant.length/2 + 2);
//|
//|        int nextIQuant = matchState[0];
//|        int nextQuantStart = -1;
//|        if (nextIQuant < quant.length) {
//|            nextQuantStart = quant[nextIQuant];
//|            matchState[0] += 4;
//|        }
//|
//|        int patternPos = quant[iQuant];
//|        int quantLimit = quant[iQuant+1];
//|
//|        // Save our backup position in case we fail to match a
//|        // quant repetition.
//|        int backupTextPos = textPos;
//|
//|        // Save our starting match position
//|        matchState[2*iQuant + 2] = textPos;
//|
//|        int matchCount = 0;
//|
//|        for (;;) {
//|            // If we are at the start of the next quant, then match it
//|            // recursively.  This will (if successful) move the patternPos
//|            // to the next quant limit, and increment the next iQuant
//|            // stored in matchState[0] -- but it will not, of course
//|            // update our nextIQuant; we have to do after we return.
//|            if (patternPos == nextQuantStart) {
//|                textPos = matchQuant(text, pos, data, textPos, nextIQuant, matchState);
//|                if (textPos < 0) {
//|                    return textPos; // value <0 indicates match failure
//|                }
//|
//|                // We have successfully done a recursive quant match
//|                // so we know the patternPos is at the next quantLimit.
//|                patternPos = quant[nextIQuant+1];
//|
//|                // Update nextIQuant and nextQuantStart.
//|                nextIQuant = matchState[0];
//|                if (nextIQuant < quant.length) {
//|                    nextQuantStart = quant[nextIQuant];
//|                    matchState[0] += 4;
//|                } else {
//|                    nextQuantStart = -1;
//|                }
//|
//|                continue;
//|            }
//|
//|            // We are not at the start of a nested quant, so do
//|            // a normal character match.
//|            if (charMatches(pattern.charAt(patternPos), text, textPos, data)) {
//|                // Match success -- continue
//|                ++textPos;
//|                ++patternPos;
//|                // If we have matched a full segment, then save a new
//|                // backup position and see about repeating.
//|                if (patternPos == quantLimit) {
//|                    backupTextPos = textPos;
//|                    ++matchCount;
//|                    // If we are allowed to have more matched, be greedy;
//|                    // backup the patternPos and see if we have another match
//|                    if (matchCount < quant[iQuant+3]) {
//|                        patternPos = quant[iQuant];
//|                        continue;
//|                    }
//|
//|                    // We have exhausted the maximum match count, so we
//|                    // are done.  Save our limit position and return.
//|                    matchState[2*iQuant + 3] = textPos; // Limit
//|                    return textPos;
//|                }
//|            }
//|
//|            // Match failure
//|            else {
//|                // Backup to our last successful position and see
//|                // if we matched the proper count for this quant.
//|                textPos = backupTextPos;
//|                
//|                // assert(matchCount <= quant[iQuant+3]
//|                if (matchCount >= quant[iQuant+2]) {
//|                    matchState[2*iQuant + 3] = textPos; // Limit
//|                    return textPos;
//|                }
//|
//|                // We failed to make the minimum match count.
//|                return -1;
//|            }
//|        }
//|    }

    /**
     * Return the degree of match between this rule and the given text.  The
     * degree of match may be mismatch, a partial match, or a full match.  A
     * mismatch means at least one character of the text does not match the
     * context or key.  A partial match means some context and key characters
     * match, but the text is not long enough to match all of them.  A full
     * match means all context and key characters match.
     * @param text the text, both translated and untranslated
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= text.length()</code>.
     * @param cursor position at which to translate next, representing offset
     * into text.  This value must be between <code>start</code> and
     * <code>limit</code>.
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     * @return one of <code>MISMATCH</code>, <code>PARTIAL_MATCH</code>, or
     * <code>FULL_MATCH</code>.
     * @see #MISMATCH
     * @see #PARTIAL_MATCH
     * @see #FULL_MATCH
     */
    public int getMatchDegree(Replaceable text,
                              Transliterator.Position pos,
                              RuleBasedTransliterator.Data variables,
                              UnicodeFilter filter) {
        int len = getRegionMatchLength(text, pos, variables, filter);
        return len < anteContextLength ? MISMATCH :
            (len < pattern.length() ? PARTIAL_MATCH : FULL_MATCH);
    }

    /**
     * Return the number of characters of the text that match this rule.  If
     * there is a mismatch, return -1.  If the text is not long enough to match
     * any characters, return 0.
     * @param text the text, both translated and untranslated
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= text.length()</code>.
     * @param cursor position at which to translate next, representing offset
     * into text.  This value must be between <code>start</code> and
     * <code>limit</code>.
     * @param variables a dictionary of variables mapping <code>Character</code>
     * to <code>UnicodeSet</code>
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     * @return -1 if there is a mismatch, 0 if the text is not long enough to
     * match any characters, otherwise the number of characters of text that
     * match this rule.
     */
    protected int getRegionMatchLength(Replaceable text,
                                       Transliterator.Position pos,
                                       RuleBasedTransliterator.Data variables,
                                       UnicodeFilter filter) {
        int cursor = pos.start - anteContextLength;
        // Quick length check; this is a performance win for long rules.
        // Widen by one to allow anchor matching.
        if (cursor < (pos.contextStart - 1)) {
            return -1;
        }
        int i;
        for (i=0; i<pattern.length() && cursor<pos.contextLimit; ++i, ++cursor) {
            if (!charMatches(pattern.charAt(i), text, cursor, pos,
                             variables, filter)) {
                return -1;
            }
        }
        return i;
    }

    /**
     * Return true if the given key matches the given text.  This method
     * accounts for the fact that the key character may represent a character
     * set.  Note that the key and text characters may not be interchanged
     * without altering the results.
     * @param keyChar a character in the match key
     * @param textChar a character in the text being transliterated
     * @param variables a dictionary of variables mapping <code>Character</code>
     * to <code>UnicodeSet</code>
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     */
    protected static final boolean charMatches(char keyChar, Replaceable text,
                                               int index, Transliterator.Position pos,
                                               RuleBasedTransliterator.Data variables,
                                               UnicodeFilter filter) {
        UnicodeSet set = null;
        char textChar = (index >= pos.contextStart && index < pos.contextLimit)
            ? text.charAt(index) : ETHER;
        return (filter == null || filter.contains(textChar)) &&
            (((set = variables.lookupSet(keyChar)) == null) ?
             keyChar == textChar : set.contains(textChar));
    }

    /**
     * Append a character to a rule that is being built up.  To flush
     * the quoteBuf to rule, make one final call with isLiteral == true.
     * If there is no final character, pass in (int)-1 as c.
     * @param rule the string to append the character to
     * @param c the character to append, or (int)-1 if none.
     * @param isLiteral if true, then the given character should not be
     * quoted or escaped.  Usually this means it is a syntactic element
     * such as > or $
     * @param escapeUnprintable if true, then unprintable characters
     * should be escaped using <backslash>uxxxx or <backslash>Uxxxxxxxx.  These escapes will
     * appear outside of quotes.
     * @param quoteBuf a buffer which is used to build up quoted
     * substrings.  The caller should initially supply an empty buffer,
     * and thereafter should not modify the buffer.  The buffer should be
     * cleared out by, at the end, calling this method with a literal
     * character.
     */
    protected void appendToRule(StringBuffer rule,
                                int c,
                                boolean isLiteral,
                                boolean escapeUnprintable,
                                StringBuffer quoteBuf) {
        // If we are escaping unprintables, then escape them outside
        // quotes.  <backslash>u and <backslash>U are not recognized within quotes.  The same
        // logic applies to literals, but literals are never escaped.
        if (isLiteral ||
            (escapeUnprintable && UnicodeSet._isUnprintable(c))) {
            if (quoteBuf.length() > 0) {
                // We prefer backslash APOSTROPHE to double APOSTROPHE
                // (more readable, less similar to ") so if there are
                // double APOSTROPHEs at the ends, we pull them outside
                // of the quote.

                // If the first thing in the quoteBuf is APOSTROPHE
                // (doubled) then pull it out.
                while (quoteBuf.length() >= 2 &&
                       quoteBuf.charAt(0) == APOSTROPHE &&
                       quoteBuf.charAt(1) == APOSTROPHE) {
                    rule.append(BACKSLASH).append(APOSTROPHE);
                    quoteBuf.delete(0, 2);
                }
                // If the last thing in the quoteBuf is APOSTROPHE
                // (doubled) then remove and count it and add it after.
                int trailingCount = 0;
                while (quoteBuf.length() >= 2 &&
                       quoteBuf.charAt(quoteBuf.length()-2) == APOSTROPHE &&
                       quoteBuf.charAt(quoteBuf.length()-1) == APOSTROPHE) {
                    quoteBuf.setLength(quoteBuf.length()-2);
                    ++trailingCount;
                }
                if (quoteBuf.length() > 0) {
                    rule.append(APOSTROPHE);
                    rule.append(quoteBuf);
                    rule.append(APOSTROPHE);
                    quoteBuf.setLength(0);
                }
                while (trailingCount-- > 0) {
                    rule.append(BACKSLASH).append(APOSTROPHE);
                }
            }
            if (c != -1) {
                if (!escapeUnprintable || !UnicodeSet._escapeUnprintable(rule, c)) {
                    UTF16.append(rule, c);
                }
            }
        }

        // Escape ' and '\' and don't begin a quote just for them
        else if (quoteBuf.length() == 0 &&
                 (c == APOSTROPHE || c == BACKSLASH)) {
            rule.append(BACKSLASH).append((char)c);
        }

        // Specials (printable ascii that isn't [0-9a-zA-Z]) and
        // whitespace need quoting.  Also append stuff to quotes if we are
        // building up a quoted substring already.
        else if (quoteBuf.length() > 0 ||
                 (c >= 0x0021 && c <= 0x007E &&
                  !((c >= 0x0030/*'0'*/ && c <= 0x0039/*'9'*/) ||
                    (c >= 0x0041/*'A'*/ && c <= 0x005A/*'Z'*/) ||
                    (c >= 0x0061/*'a'*/ && c <= 0x007A/*'z'*/))) ||
                 UCharacter.isWhitespace(c)) {
            UTF16.append(quoteBuf, c);
            // Double ' within a quote
            if (c == APOSTROPHE) {
                quoteBuf.append((char)c);
            }
        }

        // Otherwise just append
        else {
            UTF16.append(rule, c);
        }

        //System.out.println("rule=" + rule.toString() + " qb=" + quoteBuf.toString());
    }

    protected final void appendToRule(StringBuffer rule,
                                      String text,
                                      boolean isLiteral,
                                      boolean escapeUnprintable,
                                      StringBuffer quoteBuf) {
        for (int i=0; i<text.length(); ++i) {
            appendToRule(rule, text.charAt(i), isLiteral, escapeUnprintable, quoteBuf);
        }
    }

    static private int[] POW10 = {1, 10, 100, 1000, 10000, 100000, 1000000,
                                  10000000, 100000000, 1000000000};

    static private final int SEGMENTS_COUNT(int[] segments) {
        // TODO
        //return segments[0];
        return segments.length / 2;
    }

    /**
     * Create a source string that represents this rule.  Append it to the
     * given string.
     */
    public String toRule(boolean escapeUnprintable) {
        int i;
        
        StringBuffer rule = new StringBuffer();

        //|| int iseg = FIRST_SEG_POS_INDEX-1;
        int iseg = -1;
        int nextSeg = -1;
        // Build an array of booleans specifying open vs. close paren
        boolean[] isOpen = null;
        if (segments != null) {
            isOpen = new boolean[2*SEGMENTS_COUNT(segments)];
            for (i=0; i<2*SEGMENTS_COUNT(segments); i+=2) {
                //|| isOpen[SEGMENTS_NUM(segments,i)  -FIRST_SEG_POS_INDEX] = true;
                //|| isOpen[SEGMENTS_NUM(segments,i+1)-FIRST_SEG_POS_INDEX] = false;
                isOpen[i]   = true;
                isOpen[i+1] = false;
            }
            nextSeg = segments[++iseg];
        }

        // Accumulate special characters (and non-specials following them)
        // into quoteBuf.  Append quoteBuf, within single quotes, when
        // a non-quoted element must be inserted.
        StringBuffer quoteBuf = new StringBuffer();

        // Do not emit the braces '{' '}' around the pattern if there
        // is neither anteContext nor postContext.
        boolean emitBraces =
            (anteContextLength != 0) || (keyLength != pattern.length());

        // Emit the input pattern
        for (i=0; i<pattern.length(); ++i) {
            if (emitBraces && i == anteContextLength) {
                appendToRule(rule, '{', true, escapeUnprintable, quoteBuf);
            }

            // Append either '(' or ')' if we are at a segment index
            if (i == nextSeg) {
                //||appendToRule(rule, isOpen[iseg-FIRST_SEG_POS_INDEX] ?
                //||                 '(' : ')',
                //||                 true, escapeUnprintable, quoteBuf);
                appendToRule(rule, isOpen[iseg] ?
                                 '(' : ')',
                                 true, escapeUnprintable, quoteBuf);
                nextSeg = segments[++iseg];
            }

            if (emitBraces && i == (anteContextLength + keyLength)) {
                appendToRule(rule, '}', true, escapeUnprintable, quoteBuf);
            }

            char c = pattern.charAt(i);
            UnicodeSet set = data.lookupSet(c);
            if (set == null) {
                appendToRule(rule, c, false, escapeUnprintable, quoteBuf);
            } else {
                appendToRule(rule, set.toPattern(escapeUnprintable),
                              true, escapeUnprintable, quoteBuf);
            }
            //||UnicodeMatcher matcher = data.lookup(c);
            //||if (matcher == null) {
            //||    appendToRule(rule, c, false, escapeUnprintable, quoteBuf);
            //||} else {
            //||    appendToRule(rule, matcher.toPattern(escapeUnprintable),
            //||                  true, escapeUnprintable, quoteBuf);
            //||}
        }

        if (i == nextSeg) {
            // assert(!isOpen[iSeg-FIRST_SEG_POS_INDEX]);
            appendToRule(rule, ')', true, escapeUnprintable, quoteBuf);
        }

        if (emitBraces && i == (anteContextLength + keyLength)) {
            appendToRule(rule, '}', true, escapeUnprintable, quoteBuf);
        }

        appendToRule(rule, " > ", true, escapeUnprintable, quoteBuf);

        // Emit the output pattern

        // Handle a cursor preceding the output
        int cursor = cursorPos;
        if (cursor < 0) {
            while (cursor++ < 0) {
                appendToRule(rule, '@', true, escapeUnprintable, quoteBuf);
            }
            // Fall through and append '|' below
        }

        for (i=0; i<output.length(); ++i) {
            if (i == cursor) {
                appendToRule(rule, '|', true, escapeUnprintable, quoteBuf);
            }
            char c = output.charAt(i);
            int seg = data.lookupSegmentReference(c);
            if (seg < 0) {
                appendToRule(rule, c, false, escapeUnprintable, quoteBuf);
            } else {
                ++seg; // make 1-based
                appendToRule(rule, 0x20, true, escapeUnprintable, quoteBuf);
                rule.append(0x24 /*$*/);
                boolean show = false; // true if we should display digits
                for (int p=9; p>=0; --p) {
                    int d = seg / POW10[p];
                    seg -= d * POW10[p];
                    if (d != 0 || p == 0) {
                        show = true;
                    }
                    if (show) {
                        rule.append((char)(48+d));
                    }
                }            
                rule.append(' ');
            }
        }

        // Handle a cursor after the output.  Use > rather than >= because
        // if cursor == output.length() it is at the end of the output,
        // which is the default position, so we need not emit it.
        if (cursor > output.length()) {
            cursor -= output.length();
            while (cursor-- > 0) {
                appendToRule(rule, '@', true, escapeUnprintable, quoteBuf);
            }
            appendToRule(rule, '|', true, escapeUnprintable, quoteBuf);
        }

        appendToRule(rule, ';', true, escapeUnprintable, quoteBuf);

        return rule.toString();
    }
}

/**
 * $Log: TransliterationRule.java,v $
 * Revision 1.27  2001/09/19 17:43:38  alan
 * jitterbug 60: initial implementation of toRules()
 *
 * Revision 1.26  2001/06/29 22:35:41  alan4j
 * Implement Any-Upper Any-Lower and Any-Title transliterators
 *
 * Revision 1.25  2000/11/29 19:12:32  alan4j
 * Update docs
 *
 * Revision 1.24  2000/08/30 20:40:30  alan4j
 * Implement anchors.
 *
 * Revision 1.23  2000/06/29 21:59:23  alan4j
 * Fix handling of Transliterator.Position fields
 *
 * Revision 1.22  2000/05/18 21:37:19  alan
 * Update docs
 *
 * Revision 1.21  2000/04/28 01:22:01  alan
 * Update syntax displayed by toString
 *
 * Revision 1.20  2000/04/25 17:17:37  alan
 * Add Replaceable.copy to retain out-of-band info during reordering.
 *
 * Revision 1.19  2000/04/25 01:42:58  alan
 * Allow arbitrary length variable values. Clean up Data API. Update javadocs.
 *
 * Revision 1.18  2000/04/22 01:25:10  alan
 * Add support for cursor positioner '@'; update javadoc
 *
 * Revision 1.17  2000/04/21 21:16:40  alan
 * Modify rule syntax
 *
 * Revision 1.16  2000/04/19 16:34:18  alan
 * Add segment support.
 *
 * Revision 1.15  2000/04/12 20:17:45  alan
 * Delegate replace operation to rule object
 *
 * Revision 1.14  2000/03/10 04:07:24  johnf
 * Copyright update
 *
 * Revision 1.13  2000/02/10 07:36:25  johnf
 * fixed imports for com.ibm.util.Utility
 *
 * Revision 1.12  2000/02/03 18:11:19  Alan
 * Use array rather than hashtable for char-to-set map
 *
 * Revision 1.11  2000/01/27 18:59:19  Alan
 * Use Position rather than int[] and move all subclass overrides to one method (handleTransliterate)
 *
 * Revision 1.10  2000/01/18 20:36:17  Alan
 * Make UnicodeSet inherit from UnicodeFilter
 *
 * Revision 1.9  2000/01/18 02:38:55  Alan
 * Fix filtering bug.
 *
 * Revision 1.8  2000/01/13 23:53:23  Alan
 * Fix bugs found during ICU port
 *
 * Revision 1.7  2000/01/11 04:12:06  Alan
 * Cleanup, embellish comments
 *
 * Revision 1.6  2000/01/11 02:25:03  Alan
 * Rewrite UnicodeSet and RBT parsers for better performance and new syntax
 *
 * Revision 1.5  2000/01/04 21:43:57  Alan
 * Add rule indexing, and move masking check to TransliterationRuleSet.
 *
 * Revision 1.4  1999/12/22 01:40:54  Alan
 * Consolidate rule pattern anteContext, key, and postContext into one string.
 *
 * Revision 1.3  1999/12/22 01:05:54  Alan
 * Improve masking checking; turn it off by default, for better performance
 *
 * Revision 1.2  1999/12/21 23:58:44  Alan
 * Detect a>x masking a>y
 */
