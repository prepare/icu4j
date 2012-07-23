/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.CharacterIterator;

/**
 * The DictionaryMatcher interface is used to allow arbitrary "types" of
 * back-end data structures to be used with the break iteration code.
 */
interface DictionaryMatcher {
    /**
     * Find dictionary words that match the text.
     * 
     * @param text A CharacterIterator representing the text. The iterator is
     *            left after the longest prefix match in the dictionary.
     * @param maxLength The maximum number of code units to match.
     * @param lengths An array that is filled with the lengths of words that matched.
     * @param count Filled with the number of elements output in lengths.
     * @param limit The size of the lengths array; this limits the number of words output.
     * @param values Filled with the weight values associated with the various words.
     * @return The number of characters in text that were matched.
     */
    public int matches(CharacterIterator text, int maxLength, int[] lengths,
            int[] count, int limit, int[] values);

    /**
     * @return the kind of dictionary that this matcher is using
     */
    public int getType();
}
