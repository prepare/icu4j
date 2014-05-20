/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import static com.ibm.icu.impl.CharacterIteration.DONE32;
import static com.ibm.icu.impl.CharacterIteration.current32;

import java.io.IOException;
import java.text.CharacterIterator;

import com.ibm.icu.impl.Assert;

/**
 * Superclass for dictionary-based break engines that use a wordlist with
 * frequencies associated with each word.
 */
abstract class FrequencyBreakEngine extends DictionaryBreakEngine {
    private static final int maxSnlp = 255;
    private static final int kint32max = Integer.MAX_VALUE;

    private DictionaryMatcher fDictionary = null;

    /**
     * @param dictType The type of dictionary used by the break engine.
     * @param breakTypes The tyHenka1xpes of break iterators that can use this engine.
     * @throws IOException
     */
    FrequencyBreakEngine(String dictType, Integer... breakTypes) throws IOException {
        super(breakTypes);
        fDictionary = DictionaryData.loadDictionaryFor(dictType);
    }

    public boolean equals(Object obj) {
        // Each BreakEngine is normally a singleton, but it's possible to have
        // duplicates during initialization.
        return obj.getClass() == getClass();
    }

    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    int divideUpDictionaryRange(CharacterIterator inText, int startPos, int endPos, DequeI foundBreaks) {
        if (startPos >= endPos) {
            return 0;
        }

        inText.setIndex(startPos);

        int inputLength = endPos - startPos;
        int[] charPositions = new int[inputLength + 1];
        StringBuffer s = new StringBuffer("");
        inText.setIndex(startPos);
        while (inText.getIndex() < endPos) {
            s.append(inText.current());
            inText.next();
        }
        String prenormstr = s.toString();
        boolean isNormalized = Normalizer.quickCheck(prenormstr, Normalizer.NFKC) == Normalizer.YES ||
                               Normalizer.isNormalized(prenormstr, Normalizer.NFKC, 0);
        CharacterIterator text;
        int numChars = 0;
        if (isNormalized) {
            text = new java.text.StringCharacterIterator(prenormstr);
            int index = 0;
            charPositions[0] = 0;
            while (index < prenormstr.length()) {
                int codepoint = prenormstr.codePointAt(index);
                index += Character.charCount(codepoint);
                numChars++;
                charPositions[numChars] = index;
            }
        } else {
            String normStr = Normalizer.normalize(prenormstr, Normalizer.NFKC);
            text = new java.text.StringCharacterIterator(normStr);
            charPositions = new int[normStr.length() + 1];
            Normalizer normalizer = new Normalizer(prenormstr, Normalizer.NFKC, 0);
            int index = 0;
            charPositions[0] = 0;
            while (index < normalizer.endIndex()) {
                normalizer.next();
                numChars++;
                index = normalizer.getIndex();
                charPositions[numChars] = index;
            }
        }
        
        int[] bestSnlp = new int[numChars + 1];
        bestSnlp[0] = 0;
        for (int i = 1; i <= numChars; i++) {
            bestSnlp[i] = kint32max;
        }

        int[] prev = new int[numChars + 1];
        for (int i = 0; i <= numChars; i++) {
            prev[i] = -1;
        }
        
        findBoundaries(text, bestSnlp, prev);

        int t_boundary[] = new int[numChars + 1];
        int numBreaks = 0;
        if (bestSnlp[numChars] == kint32max) {
            t_boundary[numBreaks] = numChars;
            numBreaks++;
        } else {
            for (int i = numChars; i > 0; i = prev[i]) {
                t_boundary[numBreaks] = i;
                numBreaks++;
            }
            Assert.assrt(prev[t_boundary[numBreaks - 1]] == 0);
        }

        if (foundBreaks.size() == 0 || foundBreaks.peek() < startPos) {
            t_boundary[numBreaks++] = 0;
        }

        int correctedNumBreaks = 0;
        for (int i = numBreaks - 1; i >= 0; i--) {
            int pos = charPositions[t_boundary[i]] + startPos;
            if (!(foundBreaks.contains(pos) || pos == startPos)) {
                foundBreaks.push(charPositions[t_boundary[i]] + startPos);
                correctedNumBreaks++;
            }
        }

        if (!foundBreaks.isEmpty() && foundBreaks.peek() == endPos) {
            foundBreaks.pop();
            correctedNumBreaks--;
        }
        if (!foundBreaks.isEmpty()) 
            inText.setIndex(foundBreaks.peek());
        return correctedNumBreaks;
    }

    /***
     * Calculates the most likely sequence of word boundaries in a a piece of
     * normalized text.
     * @param text The text (of length n) to be segmented.
     * @param bestSnlp An array of length n+1 that will be filled with frequency
     *     values for the most likely word boundaries in the text.
     * @param prev An array of length n+1. Each element in the array will be
     *     filled with the index of the most likely previous word in that position.
     */
    protected void findBoundaries(CharacterIterator text, int[] bestSnlp, int[] prev) {
        // From here on out, do the algorithm. Note that our indices
        // refer to indices within the normalized string.
        final int maxWordSize = 20;
        int numChars = bestSnlp.length - 1;
        int values[] = new int[numChars];
        int lengths[] = new int[numChars];
        // dynamic programming to find the best segmentation
        for (int i = 0; i < numChars; i++) {
            text.setIndex(i);
            if (bestSnlp[i] == kint32max) {
                continue;
            }
            
            int maxSearchLength = (i + maxWordSize < numChars) ? maxWordSize : (numChars - i);
            int[] count_ = new int[1];
            fDictionary.matches(text, maxSearchLength, lengths, count_, maxSearchLength, values);
            int count = count_[0];
            
            // if there are no single character matches found in the dictionary 
            // starting with this character, treat character as a 1-character word
            // with the highest value possible (i.e. the least likely to occur).
            if ((count == 0 || lengths[0] != 1) && current32(text) != DONE32) {
                values[count] = maxSnlp;
                lengths[count] = 1;
                count++;
            }

            for (int j = 0; j < count; j++) {
                int newSnlp = bestSnlp[i] + values[j];
                if (newSnlp < bestSnlp[lengths[j] + i]) {
                    bestSnlp[lengths[j] + i] = newSnlp;
                    prev[lengths[j] + i] = i;
                }
            }
        }
    }
}
