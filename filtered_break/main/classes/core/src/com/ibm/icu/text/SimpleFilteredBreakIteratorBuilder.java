/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.text.CharacterIterator;
import java.util.HashSet;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.util.BytesTrie;
import com.ibm.icu.util.CharsTrie;
import com.ibm.icu.util.CharsTrieBuilder;
import com.ibm.icu.util.StringTrieBuilder;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

class SimpleFilteredSentenceBreakIterator extends BreakIterator {

    private BreakIterator delegate;
    private CharacterIterator text;
    private CharsTrie backwardsTrie; // i.e. ".srM" for Mrs.
    private CharsTrie forwardsPartialTrie; // Has ".a" for "a.M."

    /**
     * @param adoptBreakIterator
     *            break iterator to adopt
     * @param forwardsPartialTrie
     *            forward & partial char trie to adopt
     * @param backwardsTrie
     *            backward trie to adopt
     */
    public SimpleFilteredSentenceBreakIterator(BreakIterator adoptBreakIterator, CharsTrie forwardsPartialTrie,
            CharsTrie backwardsTrie) {
        this.delegate = adoptBreakIterator;
        this.forwardsPartialTrie = forwardsPartialTrie;
        this.backwardsTrie = backwardsTrie;
    }

    @Override
    // TODO: Finish this & you are almost done....
    public int next() {
        int n = delegate.next();
        if (n == BreakIterator.DONE || // at end or
                backwardsTrie == null) { // .. no backwards table loaded == no exceptions
            return n;
        }
        // UCharacterIterator text;
        text = delegate.getText();
        do { // outer loop runs once per underlying break (from fDelegate).
             // loops while 'n' points to an exception.
            text.setIndex(n);
            backwardsTrie.reset();
            char ch;

            // Assume a space is following the '.' (so we handle the case: "Mr. /Brown")
            if ((ch = text.previous()) == ' ') { // TODO: skip a class of chars here??
                // TODO only do this the 1st time?
            } else {
                ch = text.next();
            }

            BytesTrie.Result r = BytesTrie.Result.INTERMEDIATE_VALUE;

            int bestPosn = -1;
            int bestValue = -1;

            while ((ch = text.previous()) != BreakIterator.DONE && // more to consume backwards and..
                    ((r = backwardsTrie.nextForCodePoint(ch)).hasNext())) {// more in the trie
                if (r.hasValue()) { // remember the best match so far
                    bestPosn = text.getIndex();
                    bestValue = backwardsTrie.getValue();
                }
            }

            if (r.matches()) { // exact match?
                bestValue = backwardsTrie.getValue();
                bestPosn = text.getIndex();
            }

            if (bestPosn >= 0) {
                if (bestValue == SimpleFilteredBreakIteratorBuilder.MATCH) { // exact match!
                    n = delegate.next(); // skip this one. Find the next lowerlevel break.
                    if (n == BreakIterator.DONE)
                        return n;
                    continue; // See if the next is another exception.
                } else if (bestValue == SimpleFilteredBreakIteratorBuilder.PARTIAL && forwardsPartialTrie != null) { 
                    // make sure there's a forward trie
                    // We matched the "Ph." in "Ph.D." - now we need to run everything through the forwards trie
                    // to see if it matches something going forward.
                    forwardsPartialTrie.reset();

                    BytesTrie.Result rfwd = BytesTrie.Result.INTERMEDIATE_VALUE;
                    text.setIndex(bestPosn); // hope that's close ..
                    while ((ch = text.next()) != BreakIterator.DONE
                            && ((rfwd = forwardsPartialTrie.nextForCodePoint(ch)).hasNext())) {
                    }
                    if (rfwd.matches()) {
                        // only full matches here, nothing to check
                        // skip the next:
                        n = delegate.next();
                        if (n == BreakIterator.DONE)
                            return n;
                        continue;
                    } else {
                        // no match (no exception) -return the 'underlying' break
                        return n;
                    }
                } else {
                    return n; // internal error and/or no forwards trie
                }
            } else {
                return n; // No match - so exit. Not an exception.
            }
        } while (n != BreakIterator.DONE);
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        SimpleFilteredSentenceBreakIterator other = (SimpleFilteredSentenceBreakIterator) obj;
        return delegate.equals(other.delegate) && text.equals(other.text) && backwardsTrie.equals(other.backwardsTrie)
                && forwardsPartialTrie.equals(other.forwardsPartialTrie);
    }

    @Override
    public Object clone() {
        SimpleFilteredSentenceBreakIterator other = (SimpleFilteredSentenceBreakIterator) super.clone();
        return other;
    }

    // the followings are auto-generated methods from BreakIterator, may not need it here
    @Override
    public int first() {
        return delegate.first();
    }

    @Override
    public int last() {
        return delegate.last();
    }

    @Override
    public int next(int n) {
        return delegate.next(n);
    }

    @Override
    public int previous() {
        return delegate.previous();
    }

    @Override
    public int following(int offset) {
        return delegate.following(offset);
    }

    @Override
    public int current() {
        return delegate.current();
    }

    @Override
    public CharacterIterator getText() {
        return delegate.getText();
    }

    @Override
    public void setText(CharacterIterator newText) {
        delegate.setText(newText);
    }
}

/**
 * @author tomzhang
 * 
 */
public class SimpleFilteredBreakIteratorBuilder extends FilteredBreakIteratorBuilder {
    /**
     * filter set to store all exceptions
     */
    private HashSet<String> filterSet;

    static final int PARTIAL = (1 << 0); // < partial - need to run through forward trie
    static final int MATCH = (1 << 1); // < exact match - skip this one.
    static final int SuppressInReverse = (1 << 0);
    static final int AddToForward = (1 << 1);

    public SimpleFilteredBreakIteratorBuilder(ULocale loc) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                ICUResourceBundle.ICU_BRKITR_BASE_NAME, loc);
        ICUResourceBundle exceptions = rb.findWithFallback("exceptions");
        ICUResourceBundle breaks = exceptions.findWithFallback("SentenceBreak");

        filterSet = new HashSet<String>();
        if (breaks != null) {
            for (int index = 0, size = breaks.getSize(); index < size; ++index) {
                ICUResourceBundle b = (ICUResourceBundle) breaks.get(index);
                String br = b.getString();

                System.out.println("exception is " + br);

                filterSet.add(br);
            }
        }
    }

    public SimpleFilteredBreakIteratorBuilder() {
        filterSet = new HashSet<String>();
    }

    @Override
    Boolean suppressBreakAfter(String str) {
        if (filterSet == null) {
            filterSet = new HashSet<String>();
        }
        return filterSet.add(str);
    }

    @Override
    Boolean unsuppressBreakAfter(String str) {
        if (filterSet == null) {
            return false;
        } else {
            return filterSet.remove(str);
        }
    }

    @Override
    BreakIterator build(BreakIterator adoptBreakIterator) {
        CharsTrieBuilder builder = new CharsTrieBuilder();
        CharsTrieBuilder builder2 = new CharsTrieBuilder();

        int revCount = 0;
        int fwdCount = 0;

        int subCount = filterSet.size();
        String[] ustrs = new String[subCount];
        int[] partials = new int[subCount];

        CharsTrie backwardsTrie = null; // i.e. ".srM" for Mrs.
        CharsTrie forwardsPartialTrie = null; // Has ".a" for "a.M."

        int i = 0;
        for (String s : filterSet) {
            ustrs[i] = s; // copy by value?
            partials[i] = 0; // default: no partial
            i++;
        }

        for (i = 0; i < subCount; i++) {
            String oriStr = ustrs[i];
            int nn = oriStr.indexOf('.'); // TODO: non-'.' abbreviations
            if (nn > -1 && (nn + 1) != oriStr.length()) {
                // is partial.
                // is it unique?
                int sameAs = -1;
                for (int j = 0; j < subCount; j++) {
                    if (j == i)
                        continue;
                    if (oriStr.equals(ustrs[j].substring(0, nn + 1))) {
                        if (partials[j] == 0) { // hasn't been processed yet
                            partials[j] = SuppressInReverse | AddToForward;
                        } else if ((partials[j] & SuppressInReverse) != 0) {
                            sameAs = j; // the other entry is already in the reverse table.
                        }
                    }
                }

                String prefix = oriStr.substring(0, nn + 1);
                // && (partials[i] == 0)
                if ((sameAs == -1) && (partials[i] == 0)) {
                    // first one - add the prefix to the reverse table.
                    ustrs[i] = new StringBuffer(prefix).reverse().toString() + oriStr.substring(nn + 1);
                    // new StringBuffer(prefix).reverse().toString();
                    builder.add(prefix, PARTIAL);
                    revCount++;
                    partials[i] = SuppressInReverse | AddToForward;
                }
            }
        }

        for (i = 0; i < subCount; i++) {
            if (partials[i] == 0) {
                ustrs[i] = new StringBuffer(ustrs[i]).reverse().toString();
                builder.add(ustrs[i], MATCH);
                revCount++;
            } else {
                // an optimization would be to only add the portion after the '.'
                // for example, for "Ph.D." we store ".hP" in the reverse table. We could just store "D." in the
                // forward,
                // instead of "Ph.D." since we already know the "Ph." part is a match.
                // would need the trie to be able to hold 0-length strings, though.
                builder2.add(ustrs[i], MATCH); // forward
                fwdCount++;
            }
        }

        if (revCount > 0) {
            backwardsTrie = builder.build(StringTrieBuilder.Option.FAST);
        }

        if (fwdCount > 0) {
            forwardsPartialTrie = builder2.build(StringTrieBuilder.Option.FAST);
        }
        return new SimpleFilteredSentenceBreakIterator(adoptBreakIterator, forwardsPartialTrie, backwardsTrie);
    }
}
