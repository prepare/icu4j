/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.text.UnicodeSet.SpanCondition;

/**
 * A helper class used to count, replace, and trim CharSequences based on UnicodeSet matches.
 */
public class UnicodeSetSpanner {

    private UnicodeSet unicodeSet;

    /**
     * Create a spanner from a UnicodeSet. For speed and safety, the UnicodeSet should be frozen. However, this class
     * can be used with a non-frozen version to avoid the cost of freezing.
     * 
     * @param source
     *            the original UnicodeSet
     */
    public UnicodeSetSpanner(UnicodeSet source) {
        unicodeSet = source;
    }

    /**
     * Returns the UnicodeSet used for processing. It is frozen iff the original was.
     * 
     * @return the construction set.
     */
    public UnicodeSet getUnicodeSet() {
        return unicodeSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof UnicodeSetSpanner && unicodeSet.equals(((UnicodeSetSpanner) other).unicodeSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return unicodeSet.hashCode();
    }

    /**
     * Options for replaceFrom and countIn to control how to treat each matched span.
     * 
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public enum Quantifier {
        /**
         * Treat each code point in the span as a separate element to be counted or replaced.
         * 
         * @internal
         * @deprecated This API is ICU internal only.
         */
        CODEPOINT,
        /**
         * Collapse replaced spans. That is, modify/count the entire matching span as a single item, instead of separate
         * code points. This is similar to replacing [x]+ with regex, instead of replacing [x].
         * 
         * @internal
         * @deprecated This API is ICU internal only.
         */
        SPAN,
        // Note: could in the future have an additional option, to have the number of matches in the span.
        // Only an issue when the UnicodeSet contains strings of length > 1.
    }

    /**
     * Returns the number of matching characters found in a character sequence.
     * 
     * @param sequence
     *            the sequence to count characters in
     * @return the count. Zero if there are none.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public int countIn(CharSequence sequence) {
        return countIn(sequence, Quantifier.CODEPOINT, SpanCondition.CONTAINED);
    }

    /**
     * Returns the number of matching characters found in a character sequence.
     * 
     * @param sequence
     *            the sequence to count characters in
     * @return the count. Zero if there are none.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public int countIn(CharSequence sequence, Quantifier quantifier) {
        return countIn(sequence, quantifier, SpanCondition.CONTAINED);
    }

    /**
     * Returns the number of matching characters found in a character sequence.
     * 
     * @param sequence
     *            the sequence to count characters in
     * @param quantifier
     *            (optional) whether to treat the entire span as a match, or individual code points
     * @param countSpan
     *            (optional) the spanCondition to use. CONTAINED means only count the code points in the CONTAINED span;
     *            NOT_CONTAINED is the reverse.
     * @return the count. Zero if there are none.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public int countIn(CharSequence sequence, Quantifier quantifier, SpanCondition countSpan) {
        int count = 0;
        int start = 0;
        SpanCondition skipSpan = countSpan == SpanCondition.CONTAINED ? SpanCondition.NOT_CONTAINED
                : SpanCondition.CONTAINED;
        final int length = sequence.length();
        while (start != length) {
            int endNotContained = unicodeSet.span(sequence, start, skipSpan);
            if (endNotContained == length) {
                break;
            }
            start = unicodeSet.span(sequence, endNotContained, countSpan);
            count += quantifier == Quantifier.SPAN ? 1 : Character.codePointCount(sequence, endNotContained, start);
        }
        return count;
    }

    /**
     * Delete all the matching spans in sequence.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @return modified string.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String deleteFrom(CharSequence sequence) {
        return replaceFrom(sequence, "", Quantifier.SPAN, SpanCondition.CONTAINED);
    }

    /**
     * Delete all matching spans in sequence, according to the operations.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param modifySpan
     *            specify whether to modify the matching spans (CONTAINED) or the non-matching (NOT_CONTAINED)
     * @return modified string.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String deleteFrom(CharSequence sequence, SpanCondition modifySpan) {
        return replaceFrom(sequence, "", Quantifier.SPAN, modifySpan);
    }

    /**
     * Replace all matching spans in sequence by replacement, according to the operations. Warning: if quantifier=SPAN
     * then, even if the UnicodeSet contains strings, it is individual code points in the matching span that are
     * replaced.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @return modified string.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
        return replaceFrom(sequence, replacement, Quantifier.CODEPOINT, SpanCondition.CONTAINED);
    }

    /**
     * Replace all matching spans in sequence by replacement, according to the operations. Warning: if quantifier=SPAN
     * then, even if the UnicodeSet contains strings, it is individual code points in the matching span that are
     * replaced.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @param quantifier
     *            whether to treat the entire span as a match, or individual code points
     * @return modified string.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement, Quantifier quantifier) {
        return replaceFrom(sequence, replacement, quantifier, SpanCondition.CONTAINED);
    }

    /**
     * Replace all matching spans in sequence by replacement, according to the operations. Warning: if quantifier=SPAN
     * then, even if the UnicodeSet contains strings, it is individual code points in the matching span that are
     * replaced.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @param modifySpan
     *            (optional) specify whether to modify the matching spans (CONTAINED) or the non-matching
     *            (NOT_CONTAINED)
     * @param quantifier
     *            (optional) specify whether to collapse or do codepoint by codepoint.
     * @return modified string.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement, Quantifier quantifier,
            SpanCondition modifySpan) {
        SpanCondition copySpan = modifySpan == SpanCondition.CONTAINED ? SpanCondition.NOT_CONTAINED
                : SpanCondition.CONTAINED;
        final boolean remove = replacement.length() == 0;
        StringBuilder result = new StringBuilder();
        // TODO, we can optimize this to
        // avoid this allocation unless needed

        final int length = sequence.length();
        for (int endCopy = 0; endCopy != length;) {
            int endModify = unicodeSet.span(sequence, endCopy, modifySpan);
            if (remove || endModify == 0) {
                // do nothing
            } else if (quantifier == Quantifier.SPAN) {
                result.append(replacement);
            } else {
                int count = Character.codePointCount(sequence, endCopy, endModify);
                for (int i = count; i > 0; --i) {
                    result.append(replacement);
                }
            }
            if (endModify == length) {
                break;
            }
            endCopy = unicodeSet.span(sequence, endModify, copySpan);
            result.append(sequence.subSequence(endModify, endCopy));
        }
        return result.toString();
    }

    /**
     * Options for the trim() method
     * 
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public enum TrimOption {
        /**
         * Trim leading spans (subject to INVERT).
         * 
         * @internal
         * @deprecated This API is ICU internal only.
         */
        LEADING,
        /**
         * Trim leading and trailing spans (subject to INVERT).
         * 
         * @internal
         * @deprecated This API is ICU internal only.
         */
        BOTH,
        /**
         * Trim trailing spans (subject to INVERT).
         * 
         * @internal
         * @deprecated This API is ICU internal only.
         */
        TRAILING;
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching code points at the start or
     * end of the string, depending on the options. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab")}
     * </pre>
     * 
     * ... returns {@code "catbab"}.
     * 
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public CharSequence trim(CharSequence sequence) {
        return trim(sequence, TrimOption.BOTH, SpanCondition.CONTAINED);
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching code points at the start or
     * end of the string, depending on the options. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab")}
     * </pre>
     * 
     * ... returns {@code "catbab"}.
     * 
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public CharSequence trim(CharSequence sequence, TrimOption trimOption) {
        return trim(sequence, trimOption, SpanCondition.CONTAINED);
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching code points at the start or
     * end of the string, depending on the options. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab")}
     * </pre>
     * 
     * ... returns {@code "catbab"}.
     * 
     * @param sequence
     *            the sequence to trim
     * @param trimOption
     *            (optional) LEADING, TRAILING, or BOTH
     * @param modifySpan
     *            (optional) CONTAINED or NOT_CONTAINED
     * @return a subsequence
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public CharSequence trim(CharSequence sequence, TrimOption trimOption, SpanCondition modifySpan) {
        int endLeadContained, startTrailContained;
        final int length = sequence.length();
        if (trimOption != TrimOption.TRAILING) {
            endLeadContained = unicodeSet.span(sequence, modifySpan);
            if (endLeadContained == length) {
                return "";
            }
        } else {
            endLeadContained = 0;
        }
        if (trimOption != TrimOption.LEADING) {
            startTrailContained = unicodeSet.spanBack(sequence, modifySpan);
        } else {
            startTrailContained = length;
        }
        return endLeadContained == 0 && startTrailContained == length ? sequence : sequence.subSequence(
                endLeadContained, startTrailContained);
    }

}
