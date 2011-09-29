/*
 *******************************************************************************
 * Copyright (C) 1996-2011, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

import com.ibm.icu.impl.CharacterIteratorWrapper;
import com.ibm.icu.impl.Norm2AllModes;
import com.ibm.icu.impl.Normalizer2Impl;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.util.ULocale;

/**
 * <p>
 * <code>StringSearch</code> is the concrete subclass of 
 * <code>SearchIterator</code> that provides language-sensitive text searching 
 * based on the comparison rules defined in a {@link RuleBasedCollator} object.
 * </p>
 * <p>
 * <code>StringSearch</code> uses a version of the fast Boyer-Moore search
 * algorithm that has been adapted to work with the large character set of
 * Unicode. Refer to 
 * <a href="http://www.icu-project.org/docs/papers/efficient_text_searching_in_java.html">
 * "Efficient Text Searching in Java"</a>, published in the 
 * <i>Java Report</i> on February, 1999, for further information on the 
 * algorithm.
 * </p>
 * <p>
 * Users are also strongly encouraged to read the section on 
 * <a href="http://www.icu-project.org/userguide/searchString.html">
 * String Search</a> and 
 * <a href="http://www.icu-project.org/userguide/Collate_Intro.html">
 * Collation</a> in the user guide before attempting to use this class.
 * </p>
 * <p>
 * String searching becomes a little complicated when accents are encountered at
 * match boundaries. If a match is found and it has preceding or trailing 
 * accents not part of the match, the result returned will include the 
 * preceding accents up to the first base character, if the pattern searched 
 * for starts an accent. Likewise, 
 * if the pattern ends with an accent, all trailing accents up to the first
 * base character will be included in the result.
 * </p>
 * <p>
 * For example, if a match is found in target text "a&#92;u0325&#92;u0300" for 
 * the pattern
 * "a&#92;u0325", the result returned by StringSearch will be the index 0 and
 * length 3 &lt;0, 3&gt;. If a match is found in the target 
 * "a&#92;u0325&#92;u0300" 
 * for the pattern "&#92;u0300", then the result will be index 1 and length 2 
 * <1, 2>.
 * </p>
 * <p>
 * In the case where the decomposition mode is on for the RuleBasedCollator,
 * all matches that starts or ends with an accent will have its results include 
 * preceding or following accents respectively. For example, if pattern "a" is
 * looked for in the target text "&aacute;&#92;u0325", the result will be
 * index 0 and length 2 &lt;0, 2&gt;.
 * </p>
 * <p>
 * The StringSearch class provides two options to handle accent matching 
 * described below:
 * </p>
 * <p>
 * Let S' be the sub-string of a text string S between the offsets start and 
 * end &lt;start, end&gt;.
 * <br>
 * A pattern string P matches a text string S at the offsets &lt;start, 
 * length&gt; 
 * <br>
 * if
 * <pre> 
 * option 1. P matches some canonical equivalent string of S'. Suppose the 
 *           RuleBasedCollator used for searching has a collation strength of 
 *           TERTIARY, all accents are non-ignorable. If the pattern 
 *           "a&#92;u0300" is searched in the target text 
 *           "a&#92;u0325&#92;u0300", 
 *           a match will be found, since the target text is canonically 
 *           equivalent to "a&#92;u0300&#92;u0325"
 * option 2. P matches S' and if P starts or ends with a combining mark, 
 *           there exists no non-ignorable combining mark before or after S' 
 *           in S respectively. Following the example above, the pattern 
 *           "a&#92;u0300" will not find a match in "a&#92;u0325&#92;u0300", 
 *           since
 *           there exists a non-ignorable accent '&#92;u0325' in the middle of 
 *           'a' and '&#92;u0300'. Even with a target text of 
 *           "a&#92;u0300&#92;u0325" a match will not be found because of the 
 *           non-ignorable trailing accent &#92;u0325.
 * </pre>
 * Option 2. will be the default mode for dealing with boundary accents unless
 * specified via the API setCanonical(boolean).
 * One restriction is to be noted for option 1. Currently there are no 
 * composite characters that consists of a character with combining class > 0 
 * before a character with combining class == 0. However, if such a character 
 * exists in the future, the StringSearch may not work correctly with option 1
 * when such characters are encountered.
 * </p>
 * <p>
 * <tt>SearchIterator</tt> provides APIs to specify the starting position 
 * within the text string to be searched, e.g. <tt>setIndex</tt>,
 * <tt>preceding</tt> and <tt>following</tt>. Since the starting position will 
 * be set as it is specified, please take note that there are some dangerous 
 * positions which the search may render incorrect results:
 * <ul>
 * <li> The midst of a substring that requires decomposition.
 * <li> If the following match is to be found, the position should not be the
 *      second character which requires to be swapped with the preceding 
 *      character. Vice versa, if the preceding match is to be found, 
 *      position to search from should not be the first character which 
 *      requires to be swapped with the next character. E.g certain Thai and
 *      Lao characters require swapping.
 * <li> If a following pattern match is to be found, any position within a 
 *      contracting sequence except the first will fail. Vice versa if a 
 *      preceding pattern match is to be found, a invalid starting point 
 *      would be any character within a contracting sequence except the last.
 * </ul>
 * </p>
 * <p>
 * Though collator attributes will be taken into consideration while 
 * performing matches, there are no APIs provided in StringSearch for setting 
 * and getting the attributes. These attributes can be set by getting the 
 * collator from <tt>getCollator</tt> and using the APIs in 
 * <tt>com.ibm.icu.text.Collator</tt>. To update StringSearch to the new 
 * collator attributes, <tt>reset()</tt> or 
 * <tt>setCollator(RuleBasedCollator)</tt> has to be called.
 * </p>
 * <p>
 * Consult the 
 * <a href="http://www.icu-project.org/userguide/searchString.html">
 * String Search</a> user guide and the <code>SearchIterator</code> 
 * documentation for more information and examples of use.
 * </p>
 * <p>
 * This class is not subclassable
 * </p>
 * @see SearchIterator
 * @see RuleBasedCollator
 * @author Laura Werner, synwee
 * @stable ICU 2.0
 */
// internal notes: all methods do not guarantee the correct status of the 
// characteriterator. the caller has to maintain the original index position
// if necessary. methods could change the index position as it deems fit
public final class StringSearch extends SearchIterator
{
    
    // public constructors --------------------------------------------------
    
    /**
     * Initializes the iterator to use the language-specific rules defined in 
     * the argument collator to search for argument pattern in the argument 
     * target text. The argument breakiter is used to define logical matches.
     * See super class documentation for more details on the use of the target 
     * text and BreakIterator.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param collator RuleBasedCollator that defines the language rules
     * @param breakiter A {@link BreakIterator} that is used to determine the 
     *                boundaries of a logical match. This argument can be null.
     * @exception IllegalArgumentException thrown when argument target is null,
     *            or of length 0
     * @see BreakIterator
     * @see RuleBasedCollator
     * @see SearchIterator
     * @stable ICU 2.0
     */
    public StringSearch(String pattern, CharacterIterator target,
                        RuleBasedCollator collator, BreakIterator breakiter) 
    {
        super(target, breakiter);
        m_textBeginOffset_ = targetText.getBeginIndex();
        m_textLimitOffset_ = targetText.getEndIndex();
        m_collator_ = collator;
        m_colEIter_ = m_collator_.getCollationElementIterator(target);
        m_utilColEIter_ = collator.getCollationElementIterator("");
        m_ceMask_ = getMask(m_collator_.getStrength());
        m_isCanonicalMatch_ = false;
        m_pattern_ = new Pattern(pattern);
        m_matchedIndex_ = DONE;
        m_charBreakIter_ = BreakIterator.getCharacterInstance(/*m_collator_.getLocale(ULocale.ACTUAL_LOCALE)*/);
        m_charBreakIter_.setText(target);
        initialize();
    }

    /**
     * Initializes the iterator to use the language-specific rules defined in 
     * the argument collator to search for argument pattern in the argument 
     * target text. No BreakIterators are set to test for logical matches.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param collator RuleBasedCollator that defines the language rules
     * @exception IllegalArgumentException thrown when argument target is null,
     *            or of length 0
     * @see RuleBasedCollator
     * @see SearchIterator
     * @stable ICU 2.0
     */
    public StringSearch(String pattern, CharacterIterator target,
                        RuleBasedCollator collator) 
    {
        this(pattern, target, collator, null/*BreakIterator.getCharacterInstance()*/);
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the argument locale to search for 
     * argument pattern in the argument target text. 
     * See super class documentation for more details on the use of the target 
     * text and BreakIterator.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param locale locale to use for language and break iterator rules
     * @exception IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the specified locale is not a RuleBasedCollator.
     * @see BreakIterator
     * @see RuleBasedCollator
     * @see SearchIterator
     * @stable ICU 2.0
     */
    public StringSearch(String pattern, CharacterIterator target, Locale locale)
    {
        this(pattern, target, ULocale.forLocale(locale));
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the argument locale to search for 
     * argument pattern in the argument target text. 
     * See super class documentation for more details on the use of the target 
     * text and BreakIterator.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param locale ulocale to use for language and break iterator rules
     * @exception IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the specified locale is not a RuleBasedCollator.
     * @see BreakIterator
     * @see RuleBasedCollator
     * @see SearchIterator
     * @stable ICU 3.2
     */
    public StringSearch(String pattern, CharacterIterator target, ULocale locale)
    {
        this(pattern, target, (RuleBasedCollator)Collator.getInstance(locale),
             null/*BreakIterator.getCharacterInstance(locale)*/);
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the default locale to search for 
     * argument pattern in the argument target text. 
     * See super class documentation for more details on the use of the target 
     * text and BreakIterator.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @exception IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the default locale is not a RuleBasedCollator.
     * @see BreakIterator
     * @see RuleBasedCollator
     * @see SearchIterator
     * @stable ICU 2.0
     */
    public StringSearch(String pattern, String target) 
    {
        this(pattern, new StringCharacterIterator(target),
             (RuleBasedCollator)Collator.getInstance(),
             null/*BreakIterator.getCharacterInstance()*/);
    }

    // public getters -----------------------------------------------------
    
    /**
     * <p>
     * Gets the RuleBasedCollator used for the language rules.
     * </p>
     * <p>
     * Since StringSearch depends on the returned RuleBasedCollator, any 
     * changes to the RuleBasedCollator result should follow with a call to 
     * either StringSearch.reset() or 
     * StringSearch.setCollator(RuleBasedCollator) to ensure the correct 
     * search behaviour.
     * </p>
     * @return RuleBasedCollator used by this StringSearch
     * @see RuleBasedCollator
     * @see #setCollator
     * @stable ICU 2.0
     */
    public RuleBasedCollator getCollator() 
    {
        return m_collator_;
    }
    
    /**
     * Returns the pattern for which StringSearch is searching for.
     * @return the pattern searched for
     * @stable ICU 2.0
     */
    public String getPattern() 
    {
        return m_pattern_.targetText;
    }
    
    /**
     * Return the index in the target text where the iterator is currently 
     * positioned at. 
     * If the iteration has gone past the end of the target text or past 
     * the beginning for a backwards search, {@link #DONE} is returned.
     * @return index in the target text where the iterator is currently 
     *         positioned at
     * @stable ICU 2.8
     */
    public int getIndex() 
    {
        int result = m_colEIter_.getOffset();
        if (isOutOfBounds(m_textBeginOffset_, m_textLimitOffset_, result)) {
            return DONE;
        }
        return result;
    }
    
    /**
     * Determines whether canonical matches (option 1, as described in the 
     * class documentation) is set.
     * See setCanonical(boolean) for more information.
     * @see #setCanonical
     * @return true if canonical matches is set, false otherwise
     * @stable ICU 2.8
     */
    public boolean isCanonical() 
    {
        return m_isCanonicalMatch_;
    }
    
    // public setters -----------------------------------------------------
    
    /**
     * <p>
     * Sets the RuleBasedCollator to be used for language-specific searching.
     * </p>
     * <p>
     * This method causes internal data such as Boyer-Moore shift tables
     * to be recalculated, but the iterator's position is unchanged.
     * </p>
     * @param collator to use for this StringSearch
     * @exception IllegalArgumentException thrown when collator is null
     * @see #getCollator
     * @stable ICU 2.0
     */
    public void setCollator(RuleBasedCollator collator) 
    {
        if (collator == null) {
            throw new IllegalArgumentException("Collator can not be null");
        }
        m_collator_ = collator;
        m_ceMask_ = getMask(m_collator_.getStrength());
        // if status is a failure, ucol_getAttribute returns UCOL_DEFAULT
        initialize();
        m_colEIter_.setCollator(m_collator_);
        m_utilColEIter_.setCollator(m_collator_);
        m_charBreakIter_ = BreakIterator.getCharacterInstance(/*collator.getLocale(ULocale.VALID_LOCALE)*/);
        m_charBreakIter_.setText(targetText);
    }
    
    /**
     * <p>
     * Set the pattern to search for.  
     * </p>
     * <p>
     * This method causes internal data such as Boyer-Moore shift tables
     * to be recalculated, but the iterator's position is unchanged.
     * </p>
     * @param pattern for searching
     * @see #getPattern
     * @exception IllegalArgumentException thrown if pattern is null or of
     *               length 0
     * @stable ICU 2.0
     */
    public void setPattern(String pattern) 
    {
        if (pattern == null || pattern.length() <= 0) {
            throw new IllegalArgumentException(
                    "Pattern to search for can not be null or of length 0");
        }
        m_pattern_.targetText = pattern;
        initialize();
    }
    
    /**
      * Set the target text to be searched. Text iteration will hence begin at 
     * the start of the text string. This method is useful if you want to 
     * re-use an iterator to search within a different body of text.
     * @param text new text iterator to look for match, 
     * @exception IllegalArgumentException thrown when text is null or has
     *            0 length
     * @see #getTarget
     * @stable ICU 2.8
     */
    public void setTarget(CharacterIterator text)
    {
        super.setTarget(text);
        m_textBeginOffset_ = targetText.getBeginIndex();
        m_textLimitOffset_ = targetText.getEndIndex();
        m_colEIter_.setText(targetText);
        m_charBreakIter_.setText(targetText);
    }
    
    /**
     * <p>
     * Sets the position in the target text which the next search will start 
     * from to the argument. This method clears all previous states.
     * </p>
     * <p>
     * This method takes the argument position and sets the position in the 
     * target text accordingly, without checking if position is pointing to a 
     * valid starting point to begin searching.
     * </p>
     * <p>
     * Search positions that may render incorrect results are highlighted in 
     * the class documentation.
     * </p>
     * @param position index to start next search from.
     * @exception IndexOutOfBoundsException thrown if argument position is out
     *            of the target text range.
     * @see #getIndex
     * @stable ICU 2.8
     */
    public void setIndex(int position)
    {
        super.setIndex(position);
        m_matchedIndex_ = DONE;
        m_colEIter_.setExactOffset(position);
    }
    
    /**
     * <p>
     * Set the canonical match mode. See class documentation for details.
     * The default setting for this property is false.
     * </p>
     * @param allowCanonical flag indicator if canonical matches are allowed
     * @see #isCanonical
     * @stable ICU 2.8
     */
    public void setCanonical(boolean allowCanonical)
    {
        m_isCanonicalMatch_ = allowCanonical;
        if (m_isCanonicalMatch_ == true) {
            if (m_canonicalPrefixAccents_ == null) {
                m_canonicalPrefixAccents_ = new StringBuilder();
            }
            else {
                m_canonicalPrefixAccents_.delete(0, 
                                            m_canonicalPrefixAccents_.length());
            }
            if (m_canonicalSuffixAccents_ == null) {
                m_canonicalSuffixAccents_ = new StringBuilder();
            }
            else {
                m_canonicalSuffixAccents_.delete(0, 
                                            m_canonicalSuffixAccents_.length());
            }
        }
    }
    
    // public miscellaneous methods -----------------------------------------
    
    /** 
     * <p>
     * Resets the search iteration. All properties will be reset to the 
     * default value.
     * </p>
     * <p>
     * Search will begin at the start of the target text if a forward iteration 
     * is initiated before a backwards iteration. Otherwise if a 
     * backwards iteration is initiated before a forwards iteration, the search 
     * will begin at the end of the target text.
     * </p>
     * <p>
     * Canonical match option will be reset to false, ie an exact match.
     * </p>
     * @stable ICU 2.8
     */
    public void reset()
    {
        // reset is setting the attributes that are already in string search, 
        // hence all attributes in the collator should be retrieved without any 
        // problems
        super.reset();
        m_isCanonicalMatch_ = false;
        m_ceMask_ = getMask(m_collator_.getStrength());
        // if status is a failure, ucol_getAttribute returns UCOL_DEFAULT
        initialize();
        m_colEIter_.setCollator(m_collator_);
        m_colEIter_.reset();
        m_utilColEIter_.setCollator(m_collator_);
    }

    // protected methods -----------------------------------------------------
    
    /**
     * <p>
     * Concrete method to provide the mechanism 
     * for finding the next <b>forwards</b> match in the target text.
     * See super class documentation for its use.
     * </p>  
     * @param start index in the target text at which the forwards search 
     *        should begin.
     * @return the starting index of the next forwards match if found, DONE 
     *         otherwise
     * @see #handlePrevious(int)
     * @see #DONE
     * @stable ICU 2.8
     */
    protected int handleNext(int start)
    {
        if (m_pattern_.m_CELength_ == 0) {
            matchLength = 0;
            if (m_matchedIndex_ == DONE && start == m_textBeginOffset_) {
                m_matchedIndex_ = start;
                return m_matchedIndex_;
            }
            
            targetText.setIndex(start);
            char ch = targetText.current();
            // ch can never be done, it is handled by next()
            char ch2 = targetText.next();
            if (ch2 == CharacterIterator.DONE) {
                m_matchedIndex_ = DONE;    
            }
            else {
                m_matchedIndex_ = targetText.getIndex();
            }
            if (UTF16.isLeadSurrogate(ch) && UTF16.isTrailSurrogate(ch2)) {
                targetText.next();
                m_matchedIndex_ = targetText.getIndex();
            }
        }
        else {
            if (matchLength <= 0) {
                // we must have reversed direction after we reached the start
                // of the target text
                // see SearchIterator next(), it checks the bounds and returns
                // if it exceeds the range. It does not allow setting of
                // m_matchedIndex
                if (start == m_textBeginOffset_) {
                    m_matchedIndex_ = DONE;
                }
                else {
                    // for boundary check purposes. this will ensure that the
                    // next match will not preceed the current offset
                    // note search->matchedIndex will always be set to something
                    // in the code
                    m_matchedIndex_ = start - 1;
                }
            }
    
            // status checked below
            if (m_isCanonicalMatch_) {
                // can't use exact here since extra accents are allowed.
                handleNextCanonical(start);
            }
            else {
                handleNextExact(start);
            }
        }
        if (m_matchedIndex_ == DONE) {
            targetText.setIndex(m_textLimitOffset_);
        }
        else {
            targetText.setIndex(m_matchedIndex_);
        }
        return m_matchedIndex_;
    }
    
    /**
     * <p>
     * Concrete method to provide the mechanism 
     * for finding the next <b>backwards</b> match in the target text.
     * See super class documentation for its use.
     * </p>  
     * @param start index in the target text at which the backwards search 
     *        should begin.
     * @return the starting index of the next backwards match if found, DONE 
     *         otherwise
     * @see #handleNext(int)
     * @see #DONE
     * @stable ICU 2.8
     */
    protected int handlePrevious(int start)
    {
        if (m_pattern_.m_CELength_ == 0) {
            matchLength = 0;
            // start can never be DONE or 0, it is handled in previous
            targetText.setIndex(start);
            char ch = targetText.previous();
            if (ch == CharacterIterator.DONE) {
                m_matchedIndex_ = DONE;
            }
            else {
                m_matchedIndex_ = targetText.getIndex();
                if (UTF16.isTrailSurrogate(ch)) {
                    if (UTF16.isLeadSurrogate(targetText.previous())) {
                        m_matchedIndex_ = targetText.getIndex();
                    }
                }
            }            
        }
        else {
            if (matchLength == 0) {
                // we must have reversed direction after we reached the end
                // of the target text
                // see SearchIterator next(), it checks the bounds and returns
                // if it exceeds the range. It does not allow setting of
                // m_matchedIndex
                m_matchedIndex_ = DONE;
            }
            if (m_isCanonicalMatch_) {
                // can't use exact here since extra accents are allowed.
                handlePreviousCanonical(start);
            }
            else {
                handlePreviousExact(start);
            }
        }

        if (m_matchedIndex_ == DONE) {
            targetText.setIndex(m_textBeginOffset_);
        }
        else {
            targetText.setIndex(m_matchedIndex_);
        }
        return m_matchedIndex_;
    }

    // private static inner classes ----------------------------------------
    
    private static class Pattern 
    {
        // protected methods -----------------------------------------------
        
        /**
         * Pattern string
         */
        protected String targetText;
        /**
         * Array containing the collation elements of targetText
         */
        protected int m_CE_[];
        /**
         * Number of collation elements in m_CE_
         */
        protected int m_CELength_; 
        /**
         * Array containing the collation elements of targetText
         */
        protected long m_PCE_[];
        /**
         * Number of collation elements in m_CE_
         */
        protected int m_PCELength_; 
        /**
         * Flag indicator if targetText starts with an accent
         */
        protected boolean m_hasPrefixAccents_;
        /**
         * Flag indicator if targetText ends with an accent
         */
        protected boolean m_hasSuffixAccents_;
        /**
         * Default number of characters to shift for Boyer Moore
         */
        protected int m_defaultShiftSize_;
        /**
         * Number of characters to shift for Boyer Moore, depending on the
         * source text to search
         */
        protected char m_shift_[];
        /**
         * Number of characters to shift backwards for Boyer Moore, depending 
         * on the source text to search
         */
        protected char m_backShift_[];
        
        // protected constructors ------------------------------------------
        
        /**
         * Empty constructor 
         */
        protected Pattern(String pattern) 
        {
            targetText = pattern;
            m_CE_ = new int[INITIAL_ARRAY_SIZE_];    
            m_CELength_ = 0;
            m_PCE_ = new long[INITIAL_ARRAY_SIZE_];    
            m_PCELength_ = 0;
            m_hasPrefixAccents_ = false;
            m_hasSuffixAccents_ = false;
            m_defaultShiftSize_ = 1;        
            m_shift_ = new char[MAX_TABLE_SIZE_];
            m_backShift_ = new char[MAX_TABLE_SIZE_];
        }
    }


    // private data members ------------------------------------------------
    
    /**
     * target text begin offset. Each targetText has a valid contiguous region 
     * to iterate and this data member is the offset to the first such
     * character in the region.
     */
    private int m_textBeginOffset_;
    /**
     * target text limit offset. Each targetText has a valid contiguous region 
     * to iterate and this data member is the offset to 1 after the last such
     * character in the region.
     */
    private int m_textLimitOffset_;
    /**
     * Upon completion of a search, m_matchIndex_ will store starting offset in
     * m_text for the match. The Value DONE is the default value. 
     * If we are not at the start of the text or the end of the text and 
     * m_matchedIndex_ is DONE it means that we can find any more matches in 
     * that particular direction
     */
    private int m_matchedIndex_; 
    /**
     * Current pattern to search for
     */
    private Pattern m_pattern_;
    /**
     * Collator whose rules are used to perform the search
     */
    private RuleBasedCollator m_collator_;
    /** 
     * The collation element iterator for the text source.
     */
    private CollationElementIterator m_colEIter_;
    /** 
     * Utility collation element, used throughout program for temporary 
     * iteration.
     */
    private CollationElementIterator m_utilColEIter_;
    /**
     * The mask used on the collation elements to retrieve the valid strength
     * weight 
     */
    private int m_ceMask_;
    /**
     * Buffer storing accents during a canonical search
     */
    private StringBuilder m_canonicalPrefixAccents_;
    /**
     * Buffer storing accents during a canonical search
     */
    private StringBuilder m_canonicalSuffixAccents_;
    /**
     * Flag to indicate if canonical search is to be done.
     * E.g looking for "a\u0300" in "a\u0318\u0300" will yield the match at 0.
     */
    private boolean m_isCanonicalMatch_;
    /**
     * Character break iterator for boundary checking.
     */
    private BreakIterator m_charBreakIter_;
    private final Normalizer2Impl m_nfcImpl_ = Norm2AllModes.getNFCInstance().impl;
    /**
     * Size of the shift tables
     */
    private static final int MAX_TABLE_SIZE_ = 257; 
    /**
     * Initial array size
     */
    private static final int INITIAL_ARRAY_SIZE_ = 256;
    /**
     * Utility mask
     */
    private static final int SECOND_LAST_BYTE_SHIFT_ = 8;
    /**
     * Utility mask
     */
    private static final int LAST_BYTE_MASK_ = 0xff;
    /**
     * Utility buffer for return values and temporary storage
     */
    private int m_utilBuffer_[] = new int[2];
    /**
     *  Unsigned 32-Bit Integer Mask
     */
    private static final long UNSIGNED_32BIT_MASK = 0xffffffffL;
    /**
     *  UCompareCEsResult enums
     */
    private static final int U_CE_MATCH = -1;
    private static final int U_CE_NO_MATCH = 0;
    private static final int U_CE_SKIP_TARG = 1;
    private static final int U_CE_SKIP_PATN = 2;
    private static final int U_CE_LEVEL2_BASE = 0x00000005;
    private static final int U_CE_LEVEL3_BASE = 0x00050000;
    
    // TODO: check out where are attributes (USearchAttribute) in Java ICU
    private static final int USEARCH_ANY_BASE_WEIGHT_IS_WILDCARD = 4;
    
    // private methods -------------------------------------------------------

    /**
     * Hash a collation element from its full size (32 bits) down into a
     * value that can be used as an index into the shift tables.  Right
     * now we do a modulus by the size of the hash table.
     * @param ce collation element
     * @return collapsed version of the collation element
     */
    private static final int hash(int ce) 
    {
        // the old value UCOL_PRIMARYORDER(ce) % MAX_TABLE_SIZE_ does not work
        // well with the new collation where most of the latin 1 characters
        // are of the value xx000xxx. their hashes will most of the time be 0
        // to be discussed on the hash algo.
        return CollationElementIterator.primaryOrder(ce) % MAX_TABLE_SIZE_;
    }

    private final char getFCD(int c) {
        return (char)m_nfcImpl_.getFCD16(c);
    }
    /**
     * Gets the fcd value for a character at the argument index.
     * This method takes into accounts of the supplementary characters.
     * Note this method changes the offset in the character iterator.
     * @param str UTF16 string where character for fcd retrieval resides
     * @param offset position of the character whose fcd is to be retrieved
     * @return fcd value
     */
    private final char getFCD(CharacterIterator str, int offset)
    {
        char ch = str.setIndex(offset);
        int result = m_nfcImpl_.getFCD16FromSingleLead(ch);
        if (result != 0 && Character.isHighSurrogate(ch)) {
            char c2 = str.next();
            if (Character.isLowSurrogate(c2)) {
                result = m_nfcImpl_.getFCD16(Character.toCodePoint(ch, c2));
            } else {
                result = 0;
            }
        }
        return (char)result;
    }
    /**
     * Gets the FCD value for the code point before the input offset.
     * Modifies the iterator's index.
     * @param iter text iterator
     * @param offset index after the character to test
     * @return FCD value for the character before offset
     */
    private final int getFCDBefore(CharacterIterator iter, int offset) {
        int result;
        iter.setIndex(offset);
        char c = iter.previous();
        if (UTF16.isSurrogate(c)) {
            if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c)) {
                result = 0;
            } else {
                char lead = iter.previous();
                if (Character.isHighSurrogate(lead)) {
                    result = m_nfcImpl_.getFCD16(Character.toCodePoint(lead, c));
                } else {
                    result = 0;
                }
            }
        } else {
            result = m_nfcImpl_.getFCD16FromSingleLead(c);
        }
        return result;
    }
    /**
     * Gets the fcd value for a character at the argument index.
     * This method takes into accounts of the supplementary characters.
     * @param str UTF16 string where character for fcd retrieval resides
     * @param offset position of the character whose fcd is to be retrieved
     * @return fcd value
     */
    private final char getFCD(String str, int offset)
    {
        char ch = str.charAt(offset);
        int result = m_nfcImpl_.getFCD16FromSingleLead(ch);
        if (result != 0 && Character.isHighSurrogate(ch)) {
            char c2;
            if (++offset < str.length() && Character.isLowSurrogate(c2 = str.charAt(offset))) {
                result = m_nfcImpl_.getFCD16(Character.toCodePoint(ch, c2));
            } else {
                result = 0;
            }
        }
        return (char)result;
    }

    /**
    * Getting the modified collation elements taking into account the collation 
    * attributes
    * @param ce 
    * @return the modified collation element
    */
    private final int getCE(int ce)
    {
        // note for tertiary we can't use the collator->tertiaryMask, that
        // is a preprocessed mask that takes into account case options. since
        // we are only concerned with exact matches, we don't need that.
        ce &= m_ceMask_;
        
        if (m_collator_.isAlternateHandlingShifted()) {
            // alternate handling here, since only the 16 most significant 
            // digits is only used, we can safely do a compare without masking
            // if the ce is a variable, we mask and get only the primary values
            // no shifting to quartenary is required since all primary values
            // less than variabletop will need to be masked off anyway.
            if (((m_collator_.m_variableTopValue_  << 16) & UNSIGNED_32BIT_MASK) > (ce & UNSIGNED_32BIT_MASK)) {
                if (m_collator_.getStrength() == Collator.QUATERNARY) {
                    ce = CollationElementIterator.primaryOrder(ce);
                }
                else { 
                    ce = CollationElementIterator.IGNORABLE;
                }
            }
        }
    
        return ce;
    }
    
    /**
     * Appends a int to a int array, increasing the size of the array when 
     * we are out of space.
     * @param offset in array to append to
     * @param value to append
     * @param array to append to
     * @return the array appended to, this could be a new and bigger array
     */
    private static final int[] append(int offset, int value, int array[])
    {
        if (offset >= array.length) {
            int temp[] = new int[offset + INITIAL_ARRAY_SIZE_];
            System.arraycopy(array, 0, temp, 0, array.length);
            array = temp;
        }
        array[offset] = value;
        return array;
    }
    
    /**
     * Appends a long to a long array, increasing the size of the array when 
     * we are out of space.
     * @param offset in array to append to
     * @param value to append
     * @param array to append to
     * @return the array appended to, this could be a new and bigger array
     */
    private static final long[] appendLong(int offset, long value, long array[])
    {
        if (offset >= array.length) {
            long temp[] = new long[offset + INITIAL_ARRAY_SIZE_];
            System.arraycopy(array, 0, temp, 0, array.length);
            array = temp;
        }
        array[offset] = value;
        return array;
    }
    
    /**
     * Initializing the ce table for a pattern. Stores non-ignorable collation 
     * keys. Table size will be estimated by the size of the pattern text. 
     * Table expansion will be perform as we go along. Adding 1 to ensure that 
     * the table size definitely increases.
     * Internal method, status assumed to be a success.
     * @return total number of expansions 
     */
    private final int initializePatternCETable()
    {
        m_utilColEIter_.setText(m_pattern_.targetText);
        
        int offset = 0;
        int result = 0;
        int ce = m_utilColEIter_.next();
    
        while (ce != CollationElementIterator.NULLORDER) {
            int newce = getCE(ce);
            if (newce != CollationElementIterator.IGNORABLE) {
                m_pattern_.m_CE_ = append(offset, newce, m_pattern_.m_CE_);
                offset ++;            
            }
            result += m_utilColEIter_.getMaxExpansion(ce) - 1;
            ce = m_utilColEIter_.next();
        }
    
        m_pattern_.m_CE_ = append(offset, 0, m_pattern_.m_CE_);
        m_pattern_.m_CELength_ = offset;
    
        return result;
    }
    
    /**
    * Initializing the pce table for a pattern.
    * Stores non-ignorable collation keys.
    * Table size will be estimated by the size of the pattern text. Table
    * expansion will be perform as we go along. Adding 1 to ensure that the table
    * size definitely increases.
    * Internal method, status assumed to be a success.
    * @param strsrch string search data
    * @param status output error if any, caller to check status before calling
    *               method, status assumed to be success when passed in.
    * @return total number of expansions
    */
    private final int initializePatternPCETable()
    {
        m_utilColEIter_.setText(m_pattern_.targetText);
        
        int offset = 0;
        int result = 0;
        
        m_utilColEIter_.initPCE();
        m_utilColEIter_.reset();
        long pce = m_utilColEIter_.nextProcessed().getCE();
    
        while (pce != CollationElementIterator.PROCESSED_NULLORDER) {
            if (pce != CollationElementIterator.IGNORABLE) {
                m_pattern_.m_PCE_ = appendLong(offset, pce, m_pattern_.m_PCE_);
                offset ++;            
            }
            pce = m_utilColEIter_.nextProcessed().getCE();
        }
    
        m_pattern_.m_PCE_ = appendLong(offset, 0, m_pattern_.m_PCE_);
        m_pattern_.m_PCELength_ = offset;
    
        return result;
    }    
    
    /**
     * Initializes the pattern struct.
     * Internal method, status assumed to be success.
     * @return expansionsize the total expansion size of the pattern
     */ 
    private final int initializePattern()
    {
        if (m_collator_.getStrength() == Collator.PRIMARY) {
            m_pattern_.m_hasPrefixAccents_ = false;
            m_pattern_.m_hasSuffixAccents_ = false;
        } else {
            m_pattern_.m_hasPrefixAccents_ = (getFCD(m_pattern_.targetText, 0) 
                                                 >> SECOND_LAST_BYTE_SHIFT_) != 0;
            m_pattern_.m_hasSuffixAccents_ = (getFCD(m_pattern_.targetText.codePointBefore(
                                                        m_pattern_.targetText.length()))
                                                & LAST_BYTE_MASK_) != 0;
        }
        // since intializePattern is an internal method status is a success.
        return initializePatternCETable();   
    }
    
    /**
     * Initializing shift tables, with the default values.
     * If a corresponding default value is 0, the shift table is not set.
     * @param shift table for forwards shift 
     * @param backshift table for backwards shift
     * @param cetable table containing pattern ce
     * @param cesize size of the pattern ces
     * @param expansionsize total size of the expansions
     * @param defaultforward the default forward value
     * @param defaultbackward the default backward value
     */
     private final void setShiftTable(char shift[], 
                                                    char backshift[], 
                                                    int cetable[], int cesize, 
                                                      int expansionsize,
                                                    char defaultforward,
                                                      char defaultbackward)
    {
        // estimate the value to shift. to do that we estimate the smallest 
        // number of characters to give the relevant ces, ie approximately
        // the number of ces minus their expansion, since expansions can come 
        // from a character.
        for (int count = 0; count < MAX_TABLE_SIZE_; count ++) {
            shift[count] = defaultforward;
        }
        cesize --; // down to the last index
        for (int count = 0; count < cesize; count ++) {
            // number of ces from right of array to the count
            int temp = defaultforward - count - 1;
            shift[hash(cetable[count])] = temp > 1 ? ((char)temp) : 1;
        }
        shift[hash(cetable[cesize])] = 1;
        // for ignorables we just shift by one. see test examples.
        shift[hash(0)] = 1;
        
        for (int count = 0; count < MAX_TABLE_SIZE_; count ++) {
            backshift[count] = defaultbackward;
        }
        for (int count = cesize; count > 0; count --) {
            // the original value count does not seem to work
            backshift[hash(cetable[count])] = (char)(count > expansionsize ? 
                                                      count - expansionsize : 1);
        }
        backshift[hash(cetable[0])] = 1;
        backshift[hash(0)] = 1;
    }
    
    /**
     * <p>Building of the pattern collation element list and the Boyer Moore 
     * StringSearch table.</p>
     * <p>The canonical match will only be performed after the default match 
     * fails.</p>
     * <p>For both cases we need to remember the size of the composed and 
     * decomposed versions of the string. Since the Boyer-Moore shift 
     * calculations shifts by a number of characters in the text and tries to 
     * match the pattern from that offset, the shift value can not be too large 
     * in case we miss some characters. To choose a right shift size, we 
     * estimate the NFC form of the and use its size as a shift guide. The NFC 
     * form should be the small possible representation of the pattern. Anyways, 
     * we'll err on the smaller shift size. Hence the calculation for 
     * minlength. Canonical match will be performed slightly differently. We'll 
     * split the pattern into 3 parts, the prefix accents (PA), the middle 
     * string bounded by the first and last base character (MS), the ending 
     * accents (EA). Matches will be done on MS first, and only when we match 
     * MS then some processing will be required for the prefix and end accents 
     * in order to determine if they match PA and EA. Hence the default shift 
     * values for the canonical match will take the size of either end's accent 
     * into consideration. Forwards search will take the end accents into 
     * consideration for the default shift values and the backwards search will 
     * take the prefix accents into consideration.</p>
     * <p>If pattern has no non-ignorable ce, we return a illegal argument 
     * error.</p>
     */ 
    private final void initialize()
    {
        m_nfcImpl_.getFCDTrie();  // ensure the FCD data is initialized
        int expandlength  = initializePattern();   
        if (m_pattern_.m_CELength_ > 0) {
            char minlength = (char)(m_pattern_.m_CELength_ > expandlength 
                                ? m_pattern_.m_CELength_ - expandlength : 1);
            m_pattern_.m_defaultShiftSize_ = minlength;
            setShiftTable(m_pattern_.m_shift_, m_pattern_.m_backShift_, 
                          m_pattern_.m_CE_, m_pattern_.m_CELength_, 
                          expandlength, minlength, minlength);
        }
        else {
            m_pattern_.m_defaultShiftSize_ = 0;
        }
    }
    
    /**
     * Determine whether the search text bounded by the offset start and end is 
     * one or more whole units of text as determined by the breakiterator in 
     * StringSearch.
     * @param start target text start offset
     * @param end target text end offset
     */
    private final boolean isBreakUnit(int start, int end) 
    {
        if (breakIterator != null) {
            int startindex = breakIterator.first();
            int endindex   = breakIterator.last();
            
            // out-of-range indexes are never boundary positions
            if (start < startindex || start > endindex || end < startindex 
                || end > endindex) {
                return false;
            }
            // otherwise, we can use following() on the position before the 
            // specified one and return true of the position we get back is the 
            // one the user specified
            boolean result = (start == startindex 
                              || breakIterator.following(start - 1) == start) 
                             && (end == endindex 
                                  || breakIterator.following(end - 1) == end);
            if (result) {
                // iterates the individual ces
                m_utilColEIter_.setText(
                    new CharacterIteratorWrapper(targetText), start);
                for (int count = 0; count < m_pattern_.m_CELength_;
                     count ++) {
                    int ce = getCE(m_utilColEIter_.next());
                    if (ce == CollationElementIterator.IGNORABLE) {
                        count --;
                        continue;
                    }
                    if (ce != m_pattern_.m_CE_[count]) {
                        return false;
                    }
                }
                int nextce = m_utilColEIter_.next();
                while (m_utilColEIter_.getOffset() == end 
                       && getCE(nextce) == CollationElementIterator.IGNORABLE) {
                    nextce = m_utilColEIter_.next();       
                }
                if (nextce != CollationElementIterator.NULLORDER 
                    && m_utilColEIter_.getOffset() == end) {
                    // extra collation elements at the end of the match
                    return false;
                }
            }
            return result;
        }
        return true;
    }

    /**
     * Getting the next base character offset if current offset is an accent, 
     * or the current offset if the current character contains a base character. 
     * accents the following base character will be returned
     * @param text string
     * @param textoffset current offset
     * @param textlength length of text string
     * @return the next base character or the current offset
     *         if the current character is contains a base character.
     */
    private final int getNextBaseOffset(CharacterIterator text, int textoffset)
    {
        if (textoffset >= text.getEndIndex()) {
            return textoffset;
        }
        // iteration ends with reading CharacterIterator.DONE which has fcd==0
        char c = text.setIndex(textoffset);
        for (;;) {
            if ((m_nfcImpl_.getFCD16FromSingleLead(c) >> SECOND_LAST_BYTE_SHIFT_) == 0) {
                return textoffset;
            }
            char next = text.next();
            if (Character.isSurrogatePair(c, next)) {
                int fcd = m_nfcImpl_.getFCD16(Character.toCodePoint(c, next));
                if ((fcd >> SECOND_LAST_BYTE_SHIFT_) == 0) {
                    return textoffset;
                }
                next = text.next();
                textoffset += 2;
            } else {
                ++textoffset;
            }
            c = next;
        }
    }

    /**
     * Gets the next base character offset depending on the string search 
     * pattern data
     * @param textoffset one offset away from the last character
     *                   to search for.
     * @return start index of the next base character or the current offset
     *         if the current character is contains a base character.
     */
    private final int getNextBaseOffset(int textoffset)
    {
        if (m_pattern_.m_hasSuffixAccents_ && textoffset < m_textLimitOffset_) {
            if ((getFCDBefore(targetText, textoffset) & LAST_BYTE_MASK_) != 0) {
                return getNextBaseOffset(targetText, textoffset);
            }
        }
        return textoffset;
    }

    /**
     * Shifting the collation element iterator position forward to prepare for
     * a following match. If the last character is a unsafe character, we'll 
     * only shift by 1 to capture contractions, normalization etc.
     * Internal method, status assumed to be success.
     * @param textoffset start text position to do search
     * @param ce the text ce which failed the match.
     * @param patternceindex index of the ce within the pattern ce buffer which
     *        failed the match
     * @return final offset
     */
    private int shiftForward(int textoffset, int ce, int patternceindex)
                                    
    {
        if (ce != CollationElementIterator.NULLORDER) {
            int shift = m_pattern_.m_shift_[hash(ce)];
            // this is to adjust for characters in the middle of the 
            // substring for matching that failed.
            int adjust = m_pattern_.m_CELength_ - patternceindex;
            if (adjust > 1 && shift >= adjust) {
                shift -= adjust - 1;
            }
            textoffset += shift;
        }
        else {
            textoffset += m_pattern_.m_defaultShiftSize_;
        }
         
        textoffset = getNextBaseOffset(textoffset);
        // check for unsafe characters
        // * if it is the start or middle of a contraction: to be done after 
        //   a initial match is found
        // * thai or lao base consonant character: similar to contraction
        // * high surrogate character: similar to contraction
        // * next character is a accent: shift to the next base character
        return textoffset;
    }
    
    /**
     * Gets the offset to the next safe point in text.
     * ie. not the middle of a contraction, swappable characters or 
     * supplementary characters.
     * @param textoffset offset in string
     * @param end offset in string
     * @return offset to the next safe character
     */
    private final int getNextSafeOffset(int textoffset, int end)
    {
        int result = textoffset; // first contraction character
        targetText.setIndex(result);
        while (result != end && 
            m_collator_.isUnsafe(targetText.current())) {
               result ++;
               targetText.setIndex(result);
        }
        return result; 
    }
    
    /** 
     * This checks for accents in the potential match started with a composite 
     * character.
     * This is really painful... we have to check that composite character do 
     * not have any extra accents. We have to normalize the potential match and 
     * find the immediate decomposed character before the match.
     * The first composite character would have been taken care of by the fcd 
     * checks in checkForwardExactMatch.
     * This is the slow path after the fcd of the first character and 
     * the last character has been checked by checkForwardExactMatch and we 
     * determine that the potential match has extra non-ignorable preceding
     * ces.
     * E.g. looking for \u0301 acute in \u01FA A ring above and acute, 
     * checkExtraMatchAccent should fail since there is a middle ring in 
     * \u01FA Note here that accents checking are slow and cautioned in the API 
     * docs.
     * Internal method, status assumed to be a success, caller should check 
     * status before calling this method
     * @param start index of the potential unfriendly composite character
     * @param end index of the potential unfriendly composite character
     * @return true if there is non-ignorable accents before at the beginning
     *              of the match, false otherwise.
     */
    private final boolean checkExtraMatchAccents(int start, int end)
    {
        boolean result = false;
        if (m_pattern_.m_hasPrefixAccents_) {
            targetText.setIndex(start);
            
            if (UTF16.isLeadSurrogate(targetText.next())) {
                if (!UTF16.isTrailSurrogate(targetText.next())) {
                    targetText.previous();
                }
            }
            // we are only concerned with the first composite character
            String str = getString(targetText, start, end);
            if (Normalizer.quickCheck(str, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
                int safeoffset = getNextSafeOffset(start, end);
                if (safeoffset != end) {
                    safeoffset ++;
                }
                String decomp = Normalizer.decompose(
                                str.substring(0, safeoffset - start), false);
                m_utilColEIter_.setText(decomp);
                int firstce = m_pattern_.m_CE_[0];
                boolean ignorable = true;
                int ce = CollationElementIterator.IGNORABLE;
                int offset = 0;
                while (ce != firstce) {
                    offset = m_utilColEIter_.getOffset();
                    if (ce != firstce 
                        && ce != CollationElementIterator.IGNORABLE) {
                        ignorable = false;
                    }
                    ce = m_utilColEIter_.next();
                }
                m_utilColEIter_.setExactOffset(offset); // back up 1 to the 
                m_utilColEIter_.previous();             // right offset
                offset = m_utilColEIter_.getOffset();
                result = !ignorable && (UCharacter.getCombiningClass(
                                            UTF16.charAt(decomp, offset)) != 0);
            }
        }
    
        return result;
    }
    
    /**
    * Used by exact matches, checks if there are accents before the match. 
    * This is really painful... we have to check that composite characters at
    * the start of the matches have to not have any extra accents. 
    * We check the FCD of the character first, if it starts with an accent and 
    * the first pattern ce does not match the first ce of the character, we 
    * bail.
    * Otherwise we try normalizing the first composite 
    * character and find the immediate decomposed character before the match to 
    * see if it is an non-ignorable accent.
    * Now normalizing the first composite character is enough because we ensure 
    * that when the match is passed in here with extra beginning ces, the 
    * first or last ce that match has to occur within the first character.
    * E.g. looking for \u0301 acute in \u01FA A ring above and acute, 
    * checkExtraMatchAccent should fail since there is a middle ring in \u01FA
    * Note here that accents checking are slow and cautioned in the API docs.
    * @param start offset 
    * @param end offset
    * @return true if there are accents on either side of the match, 
    *         false otherwise
    */
    private final boolean hasAccentsBeforeMatch(int start, int end) 
    {
        if (m_pattern_.m_hasPrefixAccents_) {
            // we have been iterating forwards previously
            boolean ignorable = true;
            int firstce = m_pattern_.m_CE_[0];
            m_colEIter_.setExactOffset(start);
            int ce  = getCE(m_colEIter_.next());
            while (ce != firstce) {
                if (ce != CollationElementIterator.IGNORABLE) {
                    ignorable = false;
                }
                ce = getCE(m_colEIter_.next());
            }
            if (!ignorable && m_colEIter_.isInBuffer()) {
                // within normalization buffer, discontiguous handled here
                return true;
            }
    
            // within text
            boolean accent = (getFCD(targetText, start) >> SECOND_LAST_BYTE_SHIFT_)
                                                        != 0; 
            if (!accent) {
                return checkExtraMatchAccents(start, end);
            }
            if (!ignorable) {
                return true;
            }
            if (start > m_textBeginOffset_) {
                targetText.setIndex(start);
                targetText.previous();
                if ((getFCD(targetText, targetText.getIndex()) & LAST_BYTE_MASK_) 
                                                                        != 0) {
                    m_colEIter_.setExactOffset(start);
                    ce = m_colEIter_.previous();
                    if (ce != CollationElementIterator.NULLORDER 
                        && ce != CollationElementIterator.IGNORABLE) {
                        return true;
                    }
                }
            }
        }
      
        return false;
    }
    
    /**
     * Used by exact matches, checks if there are accents bounding the match.
     * Note this is the initial boundary check. If the potential match
     * starts or ends with composite characters, the accents in those
     * characters will be determined later.
     * Not doing backwards iteration here, since discontiguos contraction for 
     * backwards collation element iterator, use up too many characters.
     * E.g. looking for \u030A ring in \u01FA A ring above and acute, 
     * should fail since there is a acute at the end of \u01FA
     * Note here that accents checking are slow and cautioned in the API docs.
     * @param start offset of match
     * @param end end offset of the match
     * @return true if there are accents on either side of the match, 
     *         false otherwise
     */
    private final boolean hasAccentsAfterMatch(int start, int end) 
    {
        if (m_pattern_.m_hasSuffixAccents_) {
            targetText.setIndex(end);
            if (end > m_textBeginOffset_ 
                && UTF16.isTrailSurrogate(targetText.previous())) {
                if (targetText.getIndex() > m_textBeginOffset_ &&
                    !UTF16.isLeadSurrogate(targetText.previous())) {
                    targetText.next();
                }
            }
            if ((getFCD(targetText, targetText.getIndex()) & LAST_BYTE_MASK_) != 0) {
                int firstce  = m_pattern_.m_CE_[0];
                m_colEIter_.setExactOffset(start);
                while (getCE(m_colEIter_.next()) != firstce) {
                }
                int count = 1;
                while (count < m_pattern_.m_CELength_) {
                    if (getCE(m_colEIter_.next()) 
                        == CollationElementIterator.IGNORABLE) {
                        count --;
                    }
                    count ++;
                }
                //int ce = getCE(m_colEIter_.next());
                int ce = m_colEIter_.next();
                if (ce != CollationElementIterator.NULLORDER 
                        && ce != CollationElementIterator.IGNORABLE) {
                    ce = getCE(ce);
                }
                if (ce != CollationElementIterator.NULLORDER 
                            && ce != CollationElementIterator.IGNORABLE) {
                    if (m_colEIter_.getOffset() <= end) {
                        return true;
                    }
                    if ((getFCD(targetText, end) >> SECOND_LAST_BYTE_SHIFT_) 
                        != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
    * Checks if the offset runs out of the text string range
    * @param textstart offset of the first character in the range
    * @param textlimit limit offset of the text string range
    * @param offset to test
    * @return true if offset is out of bounds, false otherwise
    */
    private static final boolean isOutOfBounds(int textstart, int textlimit, 
                                                int offset)
    {
        return offset < textstart || offset > textlimit;
    }
    
    /**
     * Checks for identical match
     * @param strsrch string search data
     * @param start offset of possible match
     * @param end offset of possible match
     * @return true if identical match is found
     */
    private final boolean checkIdentical(int start, int end) 
    {
        if (m_collator_.getStrength() != Collator.IDENTICAL) {
            return true;
        }
    
        String textstr = getString(targetText, start, end - start);
        if (Normalizer.quickCheck(textstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            textstr = Normalizer.decompose(textstr, false);
        }
        String patternstr = m_pattern_.targetText;
        if (Normalizer.quickCheck(patternstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            patternstr = Normalizer.decompose(patternstr, false);
        }
        return textstr.equals(patternstr);
    }
    
    /**
     * Checks to see if the match is repeated
     * @param start new match start index
     * @param limit new match limit index
     * @return true if the the match is repeated, false otherwise
     */
    private final boolean checkRepeatedMatch(int start, int limit)
    {
        if (m_matchedIndex_ == DONE) {
            return false;
        }
        int end = limit - 1; // last character in the match
        int lastmatchend = m_matchedIndex_ + matchLength - 1; 
        if (!isOverlapping()) {
            return (start >= m_matchedIndex_ && start <= lastmatchend) 
                    || (end >= m_matchedIndex_ && end <= lastmatchend)
                    || (start <= m_matchedIndex_ && end >= lastmatchend);
                      
        }
        return start <= m_matchedIndex_ && end >= lastmatchend;
    }
    
    /**
     * Checks match for contraction. 
     * If the match ends with a partial contraction we fail.
     * If the match starts too far off (because of backwards iteration) we try 
     * to chip off the extra characters depending on whether a breakiterator 
     * has been used.
     * Temporary utility buffer used to return modified start and end.
     * @param start offset of potential match, to be modified if necessary
     * @param end offset of potential match, to be modified if necessary
     * @return true if match passes the contraction test, false otherwise.
     */
    private final boolean checkNextExactContractionMatch(int start, int end) 
    {
        // This part checks if either ends of the match contains potential 
        // contraction. If so we'll have to iterate through them
        char endchar = 0;
        if (end < m_textLimitOffset_) {
            targetText.setIndex(end);
            endchar = targetText.current();
        }
        char poststartchar = 0;
        if (start + 1 < m_textLimitOffset_) {
            targetText.setIndex(start + 1);
            poststartchar = targetText.current();
        }
        if (m_collator_.isUnsafe(endchar) 
            || m_collator_.isUnsafe(poststartchar)) {
            // expansion prefix, what's left to iterate
            int bufferedCEOffset = m_colEIter_.m_CEBufferOffset_;
            boolean hasBufferedCE = bufferedCEOffset > 0;
            m_colEIter_.setExactOffset(start);
            int temp = start;
            while (bufferedCEOffset > 0) {
                // getting rid of the redundant ce, caused by setOffset.
                // since backward contraction/expansion may have extra ces if 
                // we are in the normalization buffer, hasAccentsBeforeMatch 
                // would have taken care of it.
                // E.g. the character \u01FA will have an expansion of 3, but 
                // if we are only looking for acute and ring \u030A and \u0301, 
                // we'll have to skip the first ce in the expansion buffer.
                m_colEIter_.next();
                if (m_colEIter_.getOffset() != temp) {
                    start = temp;
                    temp  = m_colEIter_.getOffset();
                }
                bufferedCEOffset --;
            }
    
            int count = 0;
            while (count < m_pattern_.m_CELength_) {
                int ce = getCE(m_colEIter_.next());
                if (ce == CollationElementIterator.IGNORABLE) {
                    continue;
                }
                if (hasBufferedCE && count == 0 
                    && m_colEIter_.getOffset() != temp) {
                    start = temp;
                    temp   = m_colEIter_.getOffset();
                }
                if (ce != m_pattern_.m_CE_[count]) {
                    end ++;
                    end = getNextBaseOffset(end);  
                    m_utilBuffer_[0] = start;
                    m_utilBuffer_[1] = end;
                    return false;
                }
                count ++;
            }
        } 
        m_utilBuffer_[0] = start;
        m_utilBuffer_[1] = end;
        return true;
    }
    
    
    /**
     * Checks and sets the match information if found.
     * Checks 
     * <ul>
     * <li> the potential match does not repeat the previous match
     * <li> boundaries are correct
     * <li> exact matches has no extra accents
     * <li> identical matchesb
     * <li> potential match does not end in the middle of a contraction
     * </ul>
     * Otherwise the offset will be shifted to the next character.
     * The result m_matchIndex_ and m_matchLength_ will be set to the truncated
     * more fitting result value.
     * Uses the temporary utility buffer for storing the modified textoffset.
     * @param textoffset offset in the collation element text.
     * @return true if the match is valid, false otherwise
     */
    private final boolean checkNextExactMatch(int textoffset)
    {
        int start = m_colEIter_.getOffset();        
        if (!checkNextExactContractionMatch(start, textoffset)) {
            // returns the modified textoffset
            m_utilBuffer_[0] = m_utilBuffer_[1];
            return false;
        }
    
        start = m_utilBuffer_[0];
        textoffset = m_utilBuffer_[1];
        // this totally matches, however we need to check if it is repeating
        if (!isBreakUnit(start, textoffset) 
            || checkRepeatedMatch(start, textoffset) 
            || hasAccentsBeforeMatch(start, textoffset) 
            || !checkIdentical(start, textoffset) 
            || hasAccentsAfterMatch(start, textoffset)) {
            textoffset ++;
            textoffset = getNextBaseOffset(textoffset);  
            m_utilBuffer_[0] = textoffset;
            return false;
        }
        
        if (m_collator_.getStrength() == Collator.PRIMARY) {
            textoffset = checkBreakBoundary(textoffset);
        }
            
        // totally match, we will get rid of the ending ignorables.
        m_matchedIndex_  = start;
        matchLength = textoffset - start;
        return true;
    }
    
    /**
    * Getting the previous base character offset, or the current offset if the 
    * current character is a base character
    * @param text the source text to work on
    * @param textoffset one offset after the current character
    * @return the offset of the next character after the base character or the 
    *             first composed character with accents
    */
    private final int getPreviousBaseOffset(CharacterIterator text, 
                                            int textoffset)
    {
        if (textoffset > m_textBeginOffset_) {
            while (true) {
                int result = textoffset;
                text.setIndex(result);
                if (UTF16.isTrailSurrogate(text.previous())) {
                    if (text.getIndex() != text.getBeginIndex() &&
                        !UTF16.isLeadSurrogate(text.previous())) {
                        text.next();
                    }
                }
                textoffset = text.getIndex();
                char fcd = getFCD(text, textoffset);
                if ((fcd >> SECOND_LAST_BYTE_SHIFT_) == 0) {
                    if ((fcd & LAST_BYTE_MASK_) != 0) {
                        return textoffset;
                    }
                    return result;
                }
                if (textoffset == m_textBeginOffset_) {
                    return m_textBeginOffset_;
                }
            }
        }
        return textoffset;
    }
    
    /**
    * Getting the indexes of the accents that are not blocked in the argument
    * accent array
    * @param accents accents in nfd.
    * @param accentsindex array to store the indexes of accents in accents that 
    *         are not blocked
    * @return the length of populated accentsindex
    */
    private int getUnblockedAccentIndex(StringBuilder accents, 
                                        int accentsindex[])
    {
        int index = 0;
        int length = accents.length();
        int cclass = 0;
        int result = 0;
        while (index < length) {
            int codepoint = UTF16.charAt(accents, index);
            int tempclass = UCharacter.getCombiningClass(codepoint);
            if (tempclass != cclass) {
                cclass = tempclass;
                accentsindex[result] = index;
                result ++;
            }
            if (UCharacter.isSupplementary(codepoint)) {
                index += 2;
            }
            else {
                index ++;
            }
        }
        accentsindex[result] = length;
        return result;
    }

    /**
     * Appends 3 StringBuilder/CharacterIterator together into a destination 
     * string buffer.
     * @param source1 string buffer
     * @param source2 character iterator
     * @param start2 start of the character iterator to merge
     * @param end2 end of the character iterator to merge
     * @param source3 string buffer
     * @return appended string buffer
     */
    private static final StringBuilder merge(StringBuilder source1, 
                                             CharacterIterator source2,
                                             int start2, int end2,
                                             StringBuilder source3) 
    {
        StringBuilder result = new StringBuilder();    
        if (source1 != null && source1.length() != 0) {
            result.append(source1);
        }
        source2.setIndex(start2);
        while (source2.getIndex() < end2) {
            result.append(source2.current());
            source2.next();
        }
        if (source3 != null && source3.length() != 0) {
            result.append(source3);
        }
        return result;
    }
    
    /**
    * Running through a collation element iterator to see if the contents 
    * matches pattern in string search data
    * @param coleiter collation element iterator to test
    * @return true if a match if found, false otherwise
    */
    private final boolean checkCollationMatch(CollationElementIterator coleiter)
    {
        int patternceindex = m_pattern_.m_CELength_;
        int offset = 0;
        while (patternceindex > 0) {
            int ce = getCE(coleiter.next());
            if (ce == CollationElementIterator.IGNORABLE) {
                continue;
            }
            if (ce != m_pattern_.m_CE_[offset]) {
                return false;
            }
            offset ++;
            patternceindex --;
        }
        return true;
    }
    
    /**
     * Rearranges the front accents to try matching.
     * Prefix accents in the text will be grouped according to their combining 
     * class and the groups will be mixed and matched to try find the perfect 
     * match with the pattern.
     * So for instance looking for "\u0301" in "\u030A\u0301\u0325"
     * step 1: split "\u030A\u0301" into 6 other type of potential accent 
     *            substrings "\u030A", "\u0301", "\u0325", "\u030A\u0301", 
     *            "\u030A\u0325", "\u0301\u0325".
     * step 2: check if any of the generated substrings matches the pattern.
     * Internal method, status is assumed to be success, caller has to check 
     * status before calling this method.
     * @param start first offset of the accents to start searching
     * @param end start of the last accent set
     * @return DONE if a match is not found, otherwise return the starting
     *         offset of the match. Note this start includes all preceding 
     *            accents.
     */
    private int doNextCanonicalPrefixMatch(int start, int end)
    {
        if ((getFCD(targetText, start) & LAST_BYTE_MASK_) == 0) {
            // die... failed at a base character
            return DONE;
        }
    
        start = targetText.getIndex(); // index changed by fcd
        int offset = getNextBaseOffset(targetText, start);
        start = getPreviousBaseOffset(start);
    
        StringBuilder accents = new StringBuilder();
        String accentstr = getString(targetText, start, offset - start);
        // normalizing the offensive string
        if (Normalizer.quickCheck(accentstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            accentstr = Normalizer.decompose(accentstr, false);
        }
        accents.append(accentstr);
            
        int accentsindex[] = new int[INITIAL_ARRAY_SIZE_];      
        int accentsize = getUnblockedAccentIndex(accents, accentsindex);
        int count = (2 << (accentsize - 1)) - 1;  
        while (count > 0) {
            // copy the base characters
            m_canonicalPrefixAccents_.delete(0, 
                                        m_canonicalPrefixAccents_.length());
            int k = 0;
            for (; k < accentsindex[0]; k ++) {
                m_canonicalPrefixAccents_.append(accents.charAt(k));
            }
            // forming all possible canonical rearrangement by dropping
            // sets of accents
            for (int i = 0; i <= accentsize - 1; i ++) {
                int mask = 1 << (accentsize - i - 1);
                if ((count & mask) != 0) {
                    for (int j = accentsindex[i]; j < accentsindex[i + 1]; 
                                                                        j ++) {
                        m_canonicalPrefixAccents_.append(accents.charAt(j));
                    }
                }
            }
            StringBuilder match = merge(m_canonicalPrefixAccents_,
                                       targetText, offset, end,
                                       m_canonicalSuffixAccents_);
                
            // if status is a failure, ucol_setText does nothing.
            // run the collator iterator through this match
            m_utilColEIter_.setText(match.toString());
            if (checkCollationMatch(m_utilColEIter_)) {
                 return start;
            }
            count --;
        }
        return DONE;
    }

    /**
    * Gets the offset to the safe point in text before textoffset.
    * ie. not the middle of a contraction, swappable characters or 
    * supplementary characters.
    * @param start offset in string
    * @param textoffset offset in string
    * @return offset to the previous safe character
    */
    private final int getPreviousSafeOffset(int start, int textoffset)
    {
        int result = textoffset; // first contraction character
        targetText.setIndex(textoffset);
        while (result >= start && m_collator_.isUnsafe(targetText.previous())) {
            result = targetText.getIndex();
        }
        if (result != start) {
            // the first contraction character is consider unsafe here
            result = targetText.getIndex(); // originally result --;
        }
        return result; 
    }

    /**
     * Take the rearranged end accents and tries matching. If match failed at
     * a seperate preceding set of accents (seperated from the rearranged on by
     * at least a base character) then we rearrange the preceding accents and 
     * tries matching again.
     * We allow skipping of the ends of the accent set if the ces do not match. 
     * However if the failure is found before the accent set, it fails.
     * Internal method, status assumed to be success, caller has to check 
     * status before calling this method.
     * @param textoffset of the start of the rearranged accent
     * @return DONE if a match is not found, otherwise return the starting
     *         offset of the match. Note this start includes all preceding 
     *         accents.
     */
    private int doNextCanonicalSuffixMatch(int textoffset)
    {
        int safelength = 0;
        StringBuilder safetext;
        int safeoffset = m_textBeginOffset_; 
        
        if (textoffset != m_textBeginOffset_ 
            && m_canonicalSuffixAccents_.length() > 0
            && m_collator_.isUnsafe(m_canonicalSuffixAccents_.charAt(0))) {
            safeoffset     = getPreviousSafeOffset(m_textBeginOffset_, 
                                                    textoffset);
            safelength     = textoffset - safeoffset;
            safetext       = merge(null, targetText, safeoffset, textoffset, 
                                   m_canonicalSuffixAccents_);
        }
        else {
            safetext = m_canonicalSuffixAccents_;
        }
    
        // if status is a failure, ucol_setText does nothing
        CollationElementIterator coleiter = m_utilColEIter_;
        coleiter.setText(safetext.toString());
        // status checked in loop below
    
        int ceindex = m_pattern_.m_CELength_ - 1;
        boolean isSafe = true; // indication flag for position in safe zone
        
        while (ceindex >= 0) {
            int textce = coleiter.previous();
            if (textce == CollationElementIterator.NULLORDER) {
                // check if we have passed the safe buffer
                if (coleiter == m_colEIter_) {
                    return DONE;
                }
                coleiter = m_colEIter_;
                if (safetext != m_canonicalSuffixAccents_) {
                    safetext.delete(0, safetext.length());
                }
                coleiter.setExactOffset(safeoffset);
                // status checked at the start of the loop
                isSafe = false;
                continue;
            }
            textce = getCE(textce);
            if (textce != CollationElementIterator.IGNORABLE 
                && textce != m_pattern_.m_CE_[ceindex]) {
                // do the beginning stuff
                int failedoffset = coleiter.getOffset();
                if (isSafe && failedoffset >= safelength) {
                    // alas... no hope. failed at rearranged accent set
                    return DONE;
                }
                else {
                    if (isSafe) {
                        failedoffset += safeoffset;
                    }
                    
                    // try rearranging the front accents
                    int result = doNextCanonicalPrefixMatch(failedoffset, 
                                                            textoffset);
                    if (result != DONE) {
                        // if status is a failure, ucol_setOffset does nothing
                        m_colEIter_.setExactOffset(result);
                    }
                    return result;
                }
            }
            if (textce == m_pattern_.m_CE_[ceindex]) {
                ceindex --;
            }
        }
        // set offset here
        if (isSafe) {
            int result = coleiter.getOffset();
            // sets the text iterator with the correct expansion and offset
            int leftoverces = coleiter.m_CEBufferOffset_;
            if (result >= safelength) { 
                result = textoffset;
            }
            else {
                result += safeoffset;
            }
            m_colEIter_.setExactOffset(result);
            m_colEIter_.m_CEBufferOffset_ = leftoverces;
            return result;
        }
        
        return coleiter.getOffset();              
    }
    
    /**
     * Trying out the substring and sees if it can be a canonical match.
     * This will try normalizing the end accents and arranging them into 
     * canonical equivalents and check their corresponding ces with the pattern 
     * ce.
     * Suffix accents in the text will be grouped according to their combining 
     * class and the groups will be mixed and matched to try find the perfect 
     * match with the pattern.
     * So for instance looking for "\u0301" in "\u030A\u0301\u0325"
     * step 1: split "\u030A\u0301" into 6 other type of potential accent 
     *         substrings
     *         "\u030A", "\u0301", "\u0325", "\u030A\u0301", "\u030A\u0325", 
     *         "\u0301\u0325".
     * step 2: check if any of the generated substrings matches the pattern.
     * @param textoffset end offset in the collation element text that ends with 
     *                   the accents to be rearranged
     * @return true if the match is valid, false otherwise
     */
    private boolean doNextCanonicalMatch(int textoffset)
    {
        int offset = m_colEIter_.getOffset();
        targetText.setIndex(textoffset);
        if (UTF16.isTrailSurrogate(targetText.previous()) 
            && targetText.getIndex() > m_textBeginOffset_) { 
            if (!UTF16.isLeadSurrogate(targetText.previous())) {
                targetText.next();
            }
        }
        if ((getFCD(targetText, targetText.getIndex()) & LAST_BYTE_MASK_) == 0) {
            if (m_pattern_.m_hasPrefixAccents_) {
                offset = doNextCanonicalPrefixMatch(offset, textoffset);
                if (offset != DONE) {
                    m_colEIter_.setExactOffset(offset);
                    return true;
                }
            }
            return false;
        }
    
        if (!m_pattern_.m_hasSuffixAccents_) {
            return false;
        }
    
        StringBuilder accents = new StringBuilder();
        // offset to the last base character in substring to search
        int baseoffset = getPreviousBaseOffset(targetText, textoffset);
        // normalizing the offensive string
        String accentstr = getString(targetText, baseoffset, 
                                     textoffset - baseoffset);
        if (Normalizer.quickCheck(accentstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            accentstr = Normalizer.decompose(accentstr, false);
        }
        accents.append(accentstr);
        // status checked in loop below
            
        int accentsindex[] = new int[INITIAL_ARRAY_SIZE_];
        int size = getUnblockedAccentIndex(accents, accentsindex);
    
        // 2 power n - 1 plus the full set of accents
        int  count = (2 << (size - 1)) - 1;  
        while (count > 0) {
            m_canonicalSuffixAccents_.delete(0, 
                                           m_canonicalSuffixAccents_.length());
            // copy the base characters
            for (int k = 0; k < accentsindex[0]; k ++) {
                m_canonicalSuffixAccents_.append(accents.charAt(k));
            }
            // forming all possible canonical rearrangement by dropping
            // sets of accents
            for (int i = 0; i <= size - 1; i ++) {
                int mask = 1 << (size - i - 1);
                if ((count & mask) != 0) {
                    for (int j = accentsindex[i]; j < accentsindex[i + 1]; 
                        j ++) {
                        m_canonicalSuffixAccents_.append(accents.charAt(j));
                    }
                }
            }
            offset = doNextCanonicalSuffixMatch(baseoffset);
            if (offset != DONE) {
                return true; // match found
            }
            count --;
        }
        return false;
    }
    
    /**
     * Gets the previous base character offset depending on the string search 
     * pattern data
     * @param strsrch string search data
     * @param textoffset current offset, current character
     * @return the offset of the next character after this base character or 
     *             itself if it is a composed character with accents
     */
    private final int getPreviousBaseOffset(int textoffset)
    {
        if (m_pattern_.m_hasPrefixAccents_ && textoffset > m_textBeginOffset_) {
            int offset = textoffset;
            if ((getFCD(targetText, offset) >> SECOND_LAST_BYTE_SHIFT_) != 0) {
                return getPreviousBaseOffset(targetText, textoffset);
            }
        }
        return textoffset;
    }
    
    /**
     * Checks match for contraction. 
     * If the match ends with a partial contraction we fail.
     * If the match starts too far off (because of backwards iteration) we try 
     * to chip off the extra characters.
     * Uses the temporary util buffer for return values of the modified start
     * and end.
     * @param start offset of potential match, to be modified if necessary
     * @param end offset of potential match, to be modified if necessary
     * @return true if match passes the contraction test, false otherwise. 
     */
    private boolean checkNextCanonicalContractionMatch(int start, int end) 
    {
        // This part checks if either ends of the match contains potential 
        // contraction. If so we'll have to iterate through them
        char schar = 0;
        char echar = 0;
        if (end < m_textLimitOffset_) {
            targetText.setIndex(end);
            echar = targetText.current();
        }
        if (start < m_textLimitOffset_) {
            targetText.setIndex(start + 1);
            schar = targetText.current();
        }
        if (m_collator_.isUnsafe(echar) || m_collator_.isUnsafe(schar)) {
            int expansion  = m_colEIter_.m_CEBufferOffset_;
            boolean hasExpansion = expansion > 0;
            m_colEIter_.setExactOffset(start);
            int temp = start;
            while (expansion > 0) {
                // getting rid of the redundant ce, caused by setOffset.
                // since backward contraction/expansion may have extra ces if 
                // we are in the normalization buffer, hasAccentsBeforeMatch 
                // would have taken care of it.
                // E.g. the character \u01FA will have an expansion of 3, but 
                // if we are only looking for acute and ring \u030A and \u0301, 
                // we'll have to skip the first ce in the expansion buffer.
                m_colEIter_.next();
                if (m_colEIter_.getOffset() != temp) {
                    start = temp;
                    temp  = m_colEIter_.getOffset();
                }
                expansion --;
            }
    
            int count = 0;
            while (count < m_pattern_.m_CELength_) {
                int ce = getCE(m_colEIter_.next());
                // status checked below, note that if status is a failure
                // ucol_next returns UCOL_NULLORDER
                if (ce == CollationElementIterator.IGNORABLE) {
                    continue;
                }
                if (hasExpansion && count == 0 
                    && m_colEIter_.getOffset() != temp) {
                    start = temp;
                    temp = m_colEIter_.getOffset();
                }
    
                if (count == 0 && ce != m_pattern_.m_CE_[0]) {
                    // accents may have extra starting ces, this occurs when a 
                    // pure accent pattern is matched without rearrangement
                    // text \u0325\u0300 and looking for \u0300
                    int expected = m_pattern_.m_CE_[0]; 
                    if ((getFCD(targetText, start) & LAST_BYTE_MASK_) != 0) {
                        ce = getCE(m_colEIter_.next());
                        while (ce != expected 
                               && ce != CollationElementIterator.NULLORDER 
                               && m_colEIter_.getOffset() <= end) {
                            ce = getCE(m_colEIter_.next());
                        }
                    }
                }
                if (ce != m_pattern_.m_CE_[count]) {
                    end ++;
                    end = getNextBaseOffset(end);  
                    m_utilBuffer_[0] = start;
                    m_utilBuffer_[1] = end;
                    return false;
                }
                count ++;
            }
        } 
        m_utilBuffer_[0] = start;
        m_utilBuffer_[1] = end;
        return true;
    }

    /**
     * Checks and sets the match information if found.
     * Checks 
     * <ul>
     * <li> the potential match does not repeat the previous match
     * <li> boundaries are correct
     * <li> potential match does not end in the middle of a contraction
     * <li> identical matches
     * </ul>
     * Otherwise the offset will be shifted to the next character.
     * The result m_matchIndex_ and m_matchLength_ will be set to the truncated
     * more fitting result value.
     * Uses the temporary utility buffer for storing the modified textoffset.
     * @param textoffset offset in the collation element text.
     * @return true if the match is valid, false otherwise
     */
    private boolean checkNextCanonicalMatch(int textoffset)
    {
        // to ensure that the start and ends are not composite characters
        // if we have a canonical accent match
        if ((m_pattern_.m_hasSuffixAccents_ 
                && m_canonicalSuffixAccents_.length() != 0) || 
            (m_pattern_.m_hasPrefixAccents_ 
                && m_canonicalPrefixAccents_.length() != 0)) {
            m_matchedIndex_ = getPreviousBaseOffset(m_colEIter_.getOffset());
            matchLength = textoffset - m_matchedIndex_;
            return true;
        }
    
        int start = m_colEIter_.getOffset();
        if (!checkNextCanonicalContractionMatch(start, textoffset)) {
            // return the modified textoffset
            m_utilBuffer_[0] = m_utilBuffer_[1]; 
            return false;
        }
        start = m_utilBuffer_[0];
        textoffset = m_utilBuffer_[1];
        start = getPreviousBaseOffset(start);
        // this totally matches, however we need to check if it is repeating
        if (checkRepeatedMatch(start, textoffset) 
            || !isBreakUnit(start, textoffset) 
            || !checkIdentical(start, textoffset)) {
            textoffset ++;
            textoffset = getNextBaseOffset(targetText, textoffset);
            m_utilBuffer_[0] = textoffset;
            return false;
        }
        
        m_matchedIndex_  = start;
        matchLength = textoffset - start;
        return true;
    }
    
    /**
     * Shifting the collation element iterator position forward to prepare for
     * a preceding match. If the first character is a unsafe character, we'll 
     * only shift by 1 to capture contractions, normalization etc.
     * @param textoffset start text position to do search
     * @param ce the text ce which failed the match.
     * @param patternceindex index of the ce within the pattern ce buffer which
     *        failed the match
     * @return final offset
     */
    private int reverseShift(int textoffset, int ce, int patternceindex)
    {         
        if (isOverlapping()) {
            if (textoffset != m_textLimitOffset_) {
                textoffset --;
            }
            else {
                textoffset -= m_pattern_.m_defaultShiftSize_;
            }
        }
        else {
            if (ce != CollationElementIterator.NULLORDER) {
                int shift = m_pattern_.m_backShift_[hash(ce)];
                
                // this is to adjust for characters in the middle of the substring 
                // for matching that failed.
                int adjust = patternceindex;
                if (adjust > 1 && shift > adjust) {
                    shift -= adjust - 1;
                }
                textoffset -= shift;
            }
            else {
                textoffset -= m_pattern_.m_defaultShiftSize_;
            }
        }    
        
        textoffset = getPreviousBaseOffset(textoffset);
        return textoffset;
    }

    /**
     * Checks match for contraction. 
     * If the match starts with a partial contraction we fail.
     * Uses the temporary utility buffer to return the modified start and end.
     * @param start offset of potential match, to be modified if necessary
     * @param end offset of potential match, to be modified if necessary
     * @return true if match passes the contraction test, false otherwise.
     */
    private boolean checkPreviousExactContractionMatch(int start, int end) 
    {
        // This part checks if either ends of the match contains potential 
        // contraction. If so we'll have to iterate through them
        char echar = 0;
        if (end < m_textLimitOffset_) {
            targetText.setIndex(end);
            echar = targetText.current();
        }
        char schar = 0;
        if (start + 1 < m_textLimitOffset_) {
            targetText.setIndex(start + 1);
            schar = targetText.current();
        }
        if (m_collator_.isUnsafe(echar) || m_collator_.isUnsafe(schar)) {
            // expansion suffix, what's left to iterate
            int expansion = m_colEIter_.m_CEBufferSize_ 
                                            - m_colEIter_.m_CEBufferOffset_;
            boolean hasExpansion = expansion > 0;
            m_colEIter_.setExactOffset(end);
            int temp = end;
            while (expansion > 0) {
                // getting rid of the redundant ce
                // since forward contraction/expansion may have extra ces
                // if we are in the normalization buffer, hasAccentsBeforeMatch
                // would have taken care of it.
                // E.g. the character \u01FA will have an expansion of 3, but if
                // we are only looking for A ring A\u030A, we'll have to skip the 
                // last ce in the expansion buffer
                m_colEIter_.previous();
                if (m_colEIter_.getOffset() != temp) {
                    end = temp;
                    temp = m_colEIter_.getOffset();
                }
                expansion --;
            }
    
            int count = m_pattern_.m_CELength_;
            while (count > 0) {
                int ce = getCE(m_colEIter_.previous());
                // status checked below, note that if status is a failure
                // ucol_previous returns UCOL_NULLORDER
                if (ce == CollationElementIterator.IGNORABLE) {
                    continue;
                }
                if (hasExpansion && count == 0 
                    && m_colEIter_.getOffset() != temp) {
                    end = temp;
                    temp = m_colEIter_.getOffset();
                }
                if (ce != m_pattern_.m_CE_[count - 1]) {
                    start --;
                    start = getPreviousBaseOffset(targetText, start);
                    m_utilBuffer_[0] = start;
                    m_utilBuffer_[1] = end;
                    return false;
                }
                count --;
            }
        } 
        m_utilBuffer_[0] = start;
        m_utilBuffer_[1] = end;
        return true;
    }
    
    /**
     * Checks and sets the match information if found.
     * Checks 
     * <ul>
     * <li> the current match does not repeat the last match
     * <li> boundaries are correct
     * <li> exact matches has no extra accents
     * <li> identical matches
     * </ul>
     * Otherwise the offset will be shifted to the preceding character.
     * Uses the temporary utility buffer to store the modified textoffset.
     * @param textoffset offset in the collation element text. the returned value
     *        will be the truncated start offset of the match or the new start 
     *        search offset.
     * @return true if the match is valid, false otherwise
     */
    private final boolean checkPreviousExactMatch(int textoffset)
    {
        // to ensure that the start and ends are not composite characters
        int end = m_colEIter_.getOffset();        
        if (!checkPreviousExactContractionMatch(textoffset, end)) {
            return false;
        }
        textoffset = m_utilBuffer_[0];
        end = m_utilBuffer_[1];
            
        // this totally matches, however we need to check if it is repeating
        // the old match
        if (checkRepeatedMatch(textoffset, end) 
            || !isBreakUnit(textoffset, end) 
            || hasAccentsBeforeMatch(textoffset, end) 
            || !checkIdentical(textoffset, end) 
            || hasAccentsAfterMatch(textoffset, end)) {
            textoffset --;
            textoffset = getPreviousBaseOffset(targetText, textoffset);
            m_utilBuffer_[0] = textoffset;
            return false;
        }
        
        if (m_collator_.getStrength() == Collator.PRIMARY) {
            end = checkBreakBoundary(end);
        }
        
        m_matchedIndex_ = textoffset;
        matchLength = end - textoffset;
        return true;
    }

    /**
     * Rearranges the end accents to try matching.
     * Suffix accents in the text will be grouped according to their combining 
     * class and the groups will be mixed and matched to try find the perfect 
     * match with the pattern.
     * So for instance looking for "\u0301" in "\u030A\u0301\u0325"
     * step 1: split "\u030A\u0301" into 6 other type of potential accent 
     *             substrings
     *         "\u030A", "\u0301", "\u0325", "\u030A\u0301", "\u030A\u0325", 
     *         "\u0301\u0325".
     * step 2: check if any of the generated substrings matches the pattern.
     * @param start offset of the first base character
     * @param end start of the last accent set
     * @return DONE if a match is not found, otherwise return the ending
     *         offset of the match. Note this start includes all following 
     *         accents.
     */
    private int doPreviousCanonicalSuffixMatch(int start, int end)
    {
        targetText.setIndex(end);
        if (UTF16.isTrailSurrogate(targetText.previous()) 
            && targetText.getIndex() > m_textBeginOffset_) {
            if (!UTF16.isLeadSurrogate(targetText.previous())) {
                targetText.next();
            } 
        }
        if ((getFCD(targetText, targetText.getIndex()) & LAST_BYTE_MASK_) == 0) {
            // die... failed at a base character
            return DONE;
        }
        end = getNextBaseOffset(targetText, end);
    
        StringBuilder accents = new StringBuilder();
        int offset = getPreviousBaseOffset(targetText, end);
        // normalizing the offensive string
        String accentstr = getString(targetText, offset, end - offset);
        if (Normalizer.quickCheck(accentstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            accentstr = Normalizer.decompose(accentstr, false);
        }
        accents.append(accentstr);    
            
        int accentsindex[] = new int[INITIAL_ARRAY_SIZE_];      
        int accentsize = getUnblockedAccentIndex(accents, accentsindex);
        int count = (2 << (accentsize - 1)) - 1;  
        while (count > 0) {
            m_canonicalSuffixAccents_.delete(0, 
                                           m_canonicalSuffixAccents_.length());
            // copy the base characters
            for (int k = 0; k < accentsindex[0]; k ++) {
                 m_canonicalSuffixAccents_.append(accents.charAt(k));
            }
            // forming all possible canonical rearrangement by dropping
            // sets of accents
            for (int i = 0; i <= accentsize - 1; i ++) {
                int mask = 1 << (accentsize - i - 1);
                if ((count & mask) != 0) {
                    for (int j = accentsindex[i]; j < accentsindex[i + 1]; 
                                                                        j ++) {
                        m_canonicalSuffixAccents_.append(accents.charAt(j));
                    }
                }
            }
            StringBuilder match = merge(m_canonicalPrefixAccents_, targetText,
                                        start, offset, 
                                        m_canonicalSuffixAccents_);
            // run the collator iterator through this match
            // if status is a failure ucol_setText does nothing
            m_utilColEIter_.setText(match.toString());
            if (checkCollationMatch(m_utilColEIter_)) {
                return end;
            }
            count --;
        }
        return DONE;
    }
    
    /**
     * Take the rearranged start accents and tries matching. If match failed at
     * a seperate following set of accents (seperated from the rearranged on by
     * at least a base character) then we rearrange the preceding accents and 
     * tries matching again.
     * We allow skipping of the ends of the accent set if the ces do not match. 
     * However if the failure is found before the accent set, it fails.
     * Internal method, status assumed to be success, caller has to check 
     * status before calling this method.
     * @param textoffset of the ends of the rearranged accent
     * @return DONE if a match is not found, otherwise return the ending offset 
     *             of the match. Note this start includes all following accents.
     */
    private int doPreviousCanonicalPrefixMatch(int textoffset)
    {
       // int safelength = 0;
        StringBuilder safetext;
        int safeoffset = textoffset;
    
        if (textoffset > m_textBeginOffset_
            && m_collator_.isUnsafe(m_canonicalPrefixAccents_.charAt(
                                    m_canonicalPrefixAccents_.length() - 1))) {
            safeoffset = getNextSafeOffset(textoffset, m_textLimitOffset_);
            //safelength = safeoffset - textoffset;
            safetext = merge(m_canonicalPrefixAccents_, targetText, textoffset, 
                             safeoffset, null);
        }
        else {
            safetext = m_canonicalPrefixAccents_;
        }
    
        // if status is a failure, ucol_setText does nothing
        CollationElementIterator coleiter = m_utilColEIter_;
        coleiter.setText(safetext.toString());
        // status checked in loop below
        
        int ceindex = 0;
        boolean isSafe = true; // safe zone indication flag for position
        int prefixlength = m_canonicalPrefixAccents_.length();
        
        while (ceindex < m_pattern_.m_CELength_) {
            int textce = coleiter.next();
            if (textce == CollationElementIterator.NULLORDER) {
                // check if we have passed the safe buffer
                if (coleiter == m_colEIter_) {
                    return DONE;
                }
                if (safetext != m_canonicalPrefixAccents_) {
                    safetext.delete(0, safetext.length());
                }
                coleiter = m_colEIter_;
                coleiter.setExactOffset(safeoffset);
                // status checked at the start of the loop
                isSafe = false;
                continue;
            }
            textce = getCE(textce);
            if (textce != CollationElementIterator.IGNORABLE 
                && textce != m_pattern_.m_CE_[ceindex]) {
                // do the beginning stuff
                int failedoffset = coleiter.getOffset();
                if (isSafe && failedoffset <= prefixlength) {
                    // alas... no hope. failed at rearranged accent set
                    return DONE;
                }
                else {
                    if (isSafe) {
                        failedoffset = safeoffset - failedoffset;
                        if (safetext != m_canonicalPrefixAccents_) {
                            safetext.delete(0, safetext.length());
                        }
                    }
                    
                    // try rearranging the end accents
                    int result = doPreviousCanonicalSuffixMatch(textoffset, 
                                                                failedoffset);
                    if (result != DONE) {
                        // if status is a failure, ucol_setOffset does nothing
                        m_colEIter_.setExactOffset(result);
                    }
                    return result;
                }
            }
            if (textce == m_pattern_.m_CE_[ceindex]) {
                ceindex ++;
            }
        }
        // set offset here
        if (isSafe) {
            int result = coleiter.getOffset();
            // sets the text iterator here with the correct expansion and offset
            int leftoverces = coleiter.m_CEBufferSize_ 
                                                - coleiter.m_CEBufferOffset_;
            if (result <= prefixlength) { 
                result = textoffset;
            }
            else {
                result = textoffset + (safeoffset - result);
            }
            m_colEIter_.setExactOffset(result);
            m_colEIter_.m_CEBufferOffset_ = m_colEIter_.m_CEBufferSize_ 
                                                                - leftoverces;
            return result;
        }
        
        return coleiter.getOffset();              
    }
    
    /**
     * Trying out the substring and sees if it can be a canonical match.
     * This will try normalizing the starting accents and arranging them into 
     * canonical equivalents and check their corresponding ces with the pattern 
     * ce.
     * Prefix accents in the text will be grouped according to their combining 
     * class and the groups will be mixed and matched to try find the perfect 
     * match with the pattern.
     * So for instance looking for "\u0301" in "\u030A\u0301\u0325"
     * step 1: split "\u030A\u0301" into 6 other type of potential accent 
     *            substrings
     *         "\u030A", "\u0301", "\u0325", "\u030A\u0301", "\u030A\u0325", 
     *         "\u0301\u0325".
     * step 2: check if any of the generated substrings matches the pattern.
     * @param textoffset start offset in the collation element text that starts 
     *                   with the accents to be rearranged
     * @return true if the match is valid, false otherwise
     */
    private boolean doPreviousCanonicalMatch(int textoffset)
    {
        int offset = m_colEIter_.getOffset();
        if ((getFCD(targetText, textoffset) >> SECOND_LAST_BYTE_SHIFT_) == 0) {
            if (m_pattern_.m_hasSuffixAccents_) {
                offset = doPreviousCanonicalSuffixMatch(textoffset, offset);
                if (offset != DONE) {
                    m_colEIter_.setExactOffset(offset);
                    return true;
                }
            }
            return false;
        }
    
        if (!m_pattern_.m_hasPrefixAccents_) {
            return false;
        }
    
        StringBuilder accents = new StringBuilder();
        // offset to the last base character in substring to search
        int baseoffset = getNextBaseOffset(targetText, textoffset);
        // normalizing the offensive string
        String textstr = getString(targetText, textoffset, 
                                                    baseoffset - textoffset);
        if (Normalizer.quickCheck(textstr, Normalizer.NFD,0) 
                                                    == Normalizer.NO) {
            textstr = Normalizer.decompose(textstr, false);
        }
        accents.append(textstr);
        // status checked in loop
            
        int accentsindex[] = new int[INITIAL_ARRAY_SIZE_];
        int size = getUnblockedAccentIndex(accents, accentsindex);
    
        // 2 power n - 1 plus the full set of accents
        int count = (2 << (size - 1)) - 1;  
        while (count > 0) {
            m_canonicalPrefixAccents_.delete(0, 
                                        m_canonicalPrefixAccents_.length());
            // copy the base characters
            for (int k = 0; k < accentsindex[0]; k ++) {
                m_canonicalPrefixAccents_.append(accents.charAt(k));
            }
            // forming all possible canonical rearrangement by dropping
            // sets of accents
            for (int i = 0; i <= size - 1; i ++) {
                int mask = 1 << (size - i - 1);
                if ((count & mask) != 0) {
                    for (int j = accentsindex[i]; j < accentsindex[i + 1]; 
                         j ++) {
                        m_canonicalPrefixAccents_.append(accents.charAt(j));
                    }
                }
            }
            offset = doPreviousCanonicalPrefixMatch(baseoffset);
            if (offset != DONE) {
                return true; // match found
            }
            count --;
        }
        return false;
    }
    
    /**
     * Checks match for contraction. 
     * If the match starts with a partial contraction we fail.
     * Uses the temporary utility buffer to return the modified start and end.
     * @param start offset of potential match, to be modified if necessary
     * @param end offset of potential match, to be modified if necessary
     * @return true if match passes the contraction test, false otherwise.
     */
    private boolean checkPreviousCanonicalContractionMatch(int start, int end) 
    {
        int temp = end;
        // This part checks if either ends of the match contains potential 
        // contraction. If so we'll have to iterate through them
        char echar = 0;
        char schar = 0;
        if (end < m_textLimitOffset_) {
            targetText.setIndex(end);
            echar = targetText.current();
        }
        if (start + 1 < m_textLimitOffset_) {
            targetText.setIndex(start + 1);
            schar = targetText.current();
        }
        if (m_collator_.isUnsafe(echar) || m_collator_.isUnsafe(schar)) {
            int expansion = m_colEIter_.m_CEBufferSize_ 
                                            - m_colEIter_.m_CEBufferOffset_;
            boolean hasExpansion = expansion > 0;
            m_colEIter_.setExactOffset(end);
            while (expansion > 0) {
                // getting rid of the redundant ce
                // since forward contraction/expansion may have extra ces
                // if we are in the normalization buffer, hasAccentsBeforeMatch
                // would have taken care of it.
                // E.g. the character \u01FA will have an expansion of 3, but 
                // if we are only looking for A ring A\u030A, we'll have to 
                // skip the last ce in the expansion buffer
                m_colEIter_.previous();
                if (m_colEIter_.getOffset() != temp) {
                    end = temp;
                    temp = m_colEIter_.getOffset();
                }
                expansion --;
            }
    
            int count = m_pattern_.m_CELength_;
            while (count > 0) {
                int ce = getCE(m_colEIter_.previous());
                // status checked below, note that if status is a failure
                // previous() returns NULLORDER
                if (ce == CollationElementIterator.IGNORABLE) {
                    continue;
                }
                if (hasExpansion && count == 0 
                    && m_colEIter_.getOffset() != temp) {
                    end = temp;
                    temp = m_colEIter_.getOffset();
                }
                if (count == m_pattern_.m_CELength_ 
                    && ce != m_pattern_.m_CE_[m_pattern_.m_CELength_ - 1]) {
                    // accents may have extra starting ces, this occurs when a 
                    // pure accent pattern is matched without rearrangement
                    int expected = m_pattern_.m_CE_[m_pattern_.m_CELength_ - 1];
                    targetText.setIndex(end);
                    if (UTF16.isTrailSurrogate(targetText.previous())) {
                        if (targetText.getIndex() > m_textBeginOffset_ &&
                            !UTF16.isLeadSurrogate(targetText.previous())) {
                            targetText.next();
                        }
                    }
                    end = targetText.getIndex();
                    if ((getFCD(targetText, end) & LAST_BYTE_MASK_) != 0) {
                        ce = getCE(m_colEIter_.previous());
                        while (ce != expected 
                                && ce != CollationElementIterator.NULLORDER 
                                && m_colEIter_.getOffset() <= start) {
                            ce = getCE(m_colEIter_.previous());
                        }
                    }
                }
                if (ce != m_pattern_.m_CE_[count - 1]) {
                    start --;
                    start = getPreviousBaseOffset(start);
                    m_utilBuffer_[0] = start;
                    m_utilBuffer_[1] = end;
                    return false;
                }
                count --;
            }
        } 
        m_utilBuffer_[0] = start;
        m_utilBuffer_[1] = end;
        return true;
    }
    
    /**
     * Checks and sets the match information if found.
     * Checks 
     * <ul>
     * <li> the potential match does not repeat the previous match
     * <li> boundaries are correct
     * <li> potential match does not end in the middle of a contraction
     * <li> identical matches
     * </ul>
     * Otherwise the offset will be shifted to the next character.
     * Uses the temporary utility buffer for storing the modified textoffset.
     * @param textoffset offset in the collation element text. the returned 
     *             value will be the truncated start offset of the match or the 
     *             new start search offset.
     * @return true if the match is valid, false otherwise
     */
    private boolean checkPreviousCanonicalMatch(int textoffset)
    {
        // to ensure that the start and ends are not composite characters
        // if we have a canonical accent match
        if (m_pattern_.m_hasSuffixAccents_ 
            && m_canonicalSuffixAccents_.length() != 0 
            || m_pattern_.m_hasPrefixAccents_ 
            && m_canonicalPrefixAccents_.length() != 0) {
            m_matchedIndex_ = textoffset;
            matchLength = getNextBaseOffset(m_colEIter_.getOffset()) 
                                                                - textoffset;
            return true;
        }
    
        int end = m_colEIter_.getOffset();
        if (!checkPreviousCanonicalContractionMatch(textoffset, end)) {
            // storing the modified textoffset
            return false;
        }
        textoffset = m_utilBuffer_[0];
        end = m_utilBuffer_[1];
        end = getNextBaseOffset(end);
        // this totally matches, however we need to check if it is repeating
        if (checkRepeatedMatch(textoffset, end) 
            || !isBreakUnit(textoffset, end) 
            || !checkIdentical(textoffset, end)) {
            textoffset --;
            textoffset = getPreviousBaseOffset(textoffset);
            m_utilBuffer_[0] = textoffset;
            return false;
        }
        
        m_matchedIndex_ = textoffset;
        matchLength = end - textoffset;
        return true;
    }
    
    /*
     * Returns TRUE if index is on a break boundary. 
     */
    private boolean isBreakBoundary(int index) 
    {
        BreakIterator breakIter = breakIterator;
        
        if (breakIter == null) {
            breakIter = m_charBreakIter_;
        }
        
        return ((breakIter != null) && breakIter.isBoundary(index));
    }
    
    /*
     * Find the next break boundary after startIndex. If the UStringSearch object
     * has an external break iterator, use that. Otherwise use the internal character
     * break iterator.
     */
    private int nextBoundaryAfter(int startIndex) 
    {
        if (m_charBreakIter_ != null) {
            return m_charBreakIter_.following(startIndex);
        }

        return startIndex;
    }
    
    private static int compareCE64s(long targCE, long patCE, long compareType) {
        if (targCE == patCE) {
            return U_CE_MATCH;
        }
        if (compareType == 0) {
            return U_CE_NO_MATCH;
        }
        
        long targCEshifted = targCE >> 32;
        long patCEshifted = patCE >> 32;
        long mask;

        mask = 0xFFFF0000;
        int targLev1 = (int)(targCEshifted & mask);
        int patLev1 = (int)(patCEshifted & mask);
        if ( targLev1 != patLev1 ) {
            if ( targLev1 == 0 ) {
                return U_CE_SKIP_TARG;
            }
            if ( patLev1 == 0 && compareType == USEARCH_ANY_BASE_WEIGHT_IS_WILDCARD ) {
                return U_CE_SKIP_PATN;
            }
            return U_CE_NO_MATCH;
        }

        mask = 0x0000FFFF;
        int targLev2 = (int)(targCEshifted & mask);
        int patLev2 = (int)(patCEshifted & mask);
        if ( targLev2 != patLev2 ) {
            if ( targLev2 == 0 ) {
                return U_CE_SKIP_TARG;
            }
            if ( patLev2 == 0 && compareType == USEARCH_ANY_BASE_WEIGHT_IS_WILDCARD ) {
                return U_CE_SKIP_PATN;
            }
            return (patLev2 == U_CE_LEVEL2_BASE || (compareType == USEARCH_ANY_BASE_WEIGHT_IS_WILDCARD && targLev2 == U_CE_LEVEL2_BASE) )?
                U_CE_MATCH: U_CE_NO_MATCH;
        }
        
        mask = 0xFFFF0000;
        int targLev3 = (int)(targCE & mask);
        int patLev3 = (int)(patCE & mask);
        if ( targLev3 != patLev3 ) {
            return (patLev3 == U_CE_LEVEL3_BASE || (compareType == USEARCH_ANY_BASE_WEIGHT_IS_WILDCARD && targLev3 == U_CE_LEVEL3_BASE) )?
                U_CE_MATCH: U_CE_NO_MATCH;
       }

        return U_CE_MATCH;
    }
    
    protected int getPCELength()
    {
        return m_pattern_.m_PCELength_;
    }
    
    /**
     *  Simple forward search for the pattern, starting at a specified index,
     *     and using using a default set search options.
     *
     *  The collator options, such as UCOL_STRENGTH and UCOL_NORMALIZTION, are honored.
     *
     *  The UStringSearch options USEARCH_CANONICAL_MATCH, USEARCH_OVERLAP and
     *  any Break Iterator are ignored.
     *
     *  Matches obey the following constraints:
     *
     *      Characters at the start or end positions of a match that are ignorable
     *      for collation are not included as part of the match, unless they
     *      are part of a combining sequence, as described below.
     *
     *      A match will not include a partial combining sequence.  Combining
     *      character sequences  are considered to be  inseperable units,
     *      and either match the pattern completely, or are considered to not match
     *      at all.  Thus, for example, an A followed a combining accent mark will 
     *      not be found when searching for a plain (unaccented) A.   (unless
     *      the collation strength has been set to ignore all accents).
     *
     *      When beginning a search, the initial starting position, startIdx,
     *      is assumed to be an acceptable match boundary with respect to
     *      combining characters.  A combining sequence that spans across the
     *      starting point will not supress a match beginning at startIdx.
     *
     *      Characters that expand to multiple collation elements
     *      (German sharp-S becoming 'ss', or the composed forms of accented
     *      characters, for example) also must match completely.
     *      Searching for a single 's' in a string containing only a sharp-s will 
     *      find no match.
     *
     *
     *  @param startIdx   The index into the text to begin the search.
     *          
     *  @return           TRUE if a match was found, FALSE otherwise.
     *
     *  @internal
     */
    private boolean search( int startIdx )
    {
        // Input parameter sanity check.
        if( (m_pattern_.m_CELength_ == 0)            ||
            (startIdx < 0)                           ||
            (startIdx > m_textLimitOffset_) ||
            (m_pattern_.m_CE_ == null) ) {
               return false;
        }

        if (m_pattern_.m_PCELength_ == 0) {
            initializePatternPCETable();
        }

        m_colEIter_.setOffset(startIdx);
        CollationElementBuffer ceb = new CollationElementBuffer(this, m_colEIter_);
        
        int targetIx = 0;
        CollationElementBuffer.CEI targetCEI = null;
        int patIx;
        boolean found;

        int  mStart = -1;
        int  mLimit = -1;
        int  minLimit;
        int  maxLimit;
        
        // Outer loop moves over match starting positions in the
        //      target CE space.
        // Here we see the target as a sequence of collation elements, resulting from the following:
        // 1. Target characters were decomposed, and (if appropriate) other compressions and expansions are applied
        //    (for example, digraphs such as IJ may be broken into two characters).
        // 2. An int64_t CE weight is determined for each resulting unit (high 16 bits are primary strength, next
        //    16 bits are secondary, next 16 (the high 16 bits of the low 32-bit half) are tertiary. Any of these
        //    fields that are for strengths below that of the collator are set to 0. If this makes the int64_t
        //    CE weight 0 (as for a combining diacritic with secondary weight when the collator strentgh is primary),
        //    then the CE is deleted, so the following code sees only CEs that are relevant.
        // For each CE, the lowIndex and highIndex correspond to where this CE begins and ends in the original text.
        // If lowIndex==highIndex, either the CE resulted from an expansion/decomposition of one of the original text
        // characters, or the CE marks the limit of the target text (in which case the CE weight is UCOL_PROCESSED_NULLORDER).
        //
        for(targetIx=0; ; targetIx++)
        {
            found = true;
            //  Inner loop checks for a match beginning at each
            //  position from the outer loop.
            int targetIxOffset = 0;
            long patCE = 0;
            // For targetIx > 0, this ceb.get gets a CE that is as far back in the ring buffer
            // (compared to the last CE fetched for the previous targetIx value) as we need to go
            // for this targetIx value, so if it is non-NULL then other ceb.get calls should be OK.
            CollationElementBuffer.CEI firstCEI = ceb.get(targetIx);
            if (firstCEI == null) {
                found = false;
                break;
            }
            
            
            for (patIx=0; patIx < m_pattern_.m_PCELength_; patIx++) {
                patCE = m_pattern_.m_PCE_[patIx];
                targetCEI = ceb.get(targetIx + patIx + targetIxOffset);
                //  Compare CE from target string with CE from the pattern.
                //    Note that the target CE will be UCOL_PROCESSED_NULLORDER if we reach the end of input,
                //    which will fail the compare, below.
                // TODO: use strsrch->search->elementComparisonType instead 1
                int ceMatch = compareCE64s(targetCEI.ce, patCE, 1);
                if ( ceMatch == U_CE_NO_MATCH ) {
                    found = false;
                    break;
                } else if ( ceMatch > U_CE_NO_MATCH ) {
                    if ( ceMatch == U_CE_SKIP_TARG ) {
                        // redo with same patCE, next targCE
                        patIx--;
                        targetIxOffset++;
                    } else { // ceMatch == U_CE_SKIP_PATN
                        // redo with same targCE, next patCE
                        targetIxOffset--;
                    }
                }
            }
            targetIxOffset += m_pattern_.m_PCELength_; // this is now the offset in target CE space to end of the match so far

            if (!found && ((targetCEI == null) || (targetCEI.ce != CollationElementIterator.PROCESSED_NULLORDER))) {
                // No match at this targetIx.  Try again at the next.
                continue;
            }

            if (!found) {
                // No match at all, we have run off the end of the target text.
                break;
            }


            // We have found a match in CE space.
            // Now determine the bounds in string index space.
            //  There still is a chance of match failure if the CE range not correspond to
            //     an acceptable character range.
            //
            CollationElementBuffer.CEI lastCEI  = ceb.get(targetIx + targetIxOffset - 1);

            mStart   = firstCEI.lowIndex;
            minLimit = lastCEI.lowIndex;

            // Look at the CE following the match.  If it is UCOL_NULLORDER the match
            //   extended to the end of input, and the match is good.

            // Look at the high and low indices of the CE following the match. If
            // they are the same it means one of two things:
            //    1. The match extended to the last CE from the target text, which is OK, or
            //    2. The last CE that was part of the match is in an expansion that extends
            //       to the first CE after the match. In this case, we reject the match.
            CollationElementBuffer.CEI nextCEI = null;
            // TODO: implement strsrch->search->elementComparisonType == 0 instead of false
            if (false) {
                nextCEI  = ceb.get(targetIx + targetIxOffset);
                maxLimit = nextCEI.lowIndex;
                if (nextCEI.lowIndex == nextCEI.highIndex && nextCEI.ce != CollationElementIterator.PROCESSED_NULLORDER) {
                    found = false;
                }
            } else {
                for ( ; ; ++targetIxOffset ) {
                    nextCEI = ceb.get(targetIx + targetIxOffset);
                    maxLimit = nextCEI.lowIndex;
                    // If we are at the end of the target too, match succeeds
                    if (  nextCEI.ce == CollationElementIterator.PROCESSED_NULLORDER ) {
                        break;
                    }
                    // As long as the next CE has primary weight of 0,
                    // it is part of the last target element matched by the pattern;
                    // make sure it can be part of a match with the last patCE
                    // TODO: check unsigned bit operation
                    if ( (((nextCEI.ce) >> 32) & 0xFFFF0000L) == 0 ) {
                        // TODO: use strsrch->search->elementComparisonType instead 1
                        int ceMatch = compareCE64s(nextCEI.ce, patCE, 1);
                        if ( ceMatch == U_CE_NO_MATCH || ceMatch == U_CE_SKIP_PATN ) {
                            found = false;
                            break;
                        }
                    // If lowIndex == highIndex, this target CE is part of an expansion of the last matched
                    // target element, but it has non-zero primary weight => match fails
                    } else if ( nextCEI.lowIndex == nextCEI.highIndex ) {
                        found = false;
                        break;
                    // Else the target CE is not part of an expansion of the last matched element, match succeeds
                    } else {
                        break;
                    }
                }
            }


            // Check for the start of the match being within a combining sequence.
            //   This can happen if the pattern itself begins with a combining char, and
            //   the match found combining marks in the target text that were attached
            //    to something else.
            //   This type of match should be rejected for not completely consuming a
            //   combining sequence.
            if (!isBreakBoundary(mStart)) {
                found = false;
            }

            // Check for the start of the match being within an Collation Element Expansion,
            //   meaning that the first char of the match is only partially matched.
            //   With exapnsions, the first CE will report the index of the source
            //   character, and all subsequent (expansions) CEs will report the source index of the
            //    _following_ character.
            int secondIx = firstCEI.highIndex;
            if (mStart == secondIx) {
                found = false;
            }

            //  Advance the match end position to the first acceptable match boundary.
            //    This advances the index over any combining charcters.
            mLimit = maxLimit;
            if (minLimit < maxLimit) {
                // When the last CE's low index is same with its high index, the CE is likely
                // a part of expansion. In this case, the index is located just after the
                // character corresponding to the CEs compared above. If the index is right
                // at the break boundary, move the position to the next boundary will result
                // incorrect match length when there are ignorable characters exist between
                // the position and the next character produces CE(s). See ticket#8482.
                if (minLimit == lastCEI.highIndex && isBreakBoundary(minLimit)) {
                    mLimit = minLimit;
                } else {
                    int nba = nextBoundaryAfter(minLimit);
                    if (nba >= lastCEI.highIndex) {
                        mLimit = nba;
                    }
                }
            }

            // If advancing to the end of a combining sequence in character indexing space
            //   advanced us beyond the end of the match in CE space, reject this match.
            if (mLimit > maxLimit) {
                found = false;
            }

            if (!isBreakBoundary(mLimit)) {
                found = false;
            }

            if (!checkIdentical(mStart, mLimit)) {
                found = false;
            }

            if (found) {
                break;
            }
        }

        // All Done.  Store back the match bounds to the caller.
        //
        if (found == false) {
            mLimit = -1;
            mStart = -1;
        } else {
            m_matchedIndex_ = mStart;
            matchLength = mLimit - mStart;
        }

        return found;
    }
    
    /**
     *  Simple backwards search for the pattern, starting at a specified index,
     *     and using using a default set search options.
     *
     *  The collator options, such as UCOL_STRENGTH and UCOL_NORMALIZTION, are honored.
     *
     *  The UStringSearch options USEARCH_CANONICAL_MATCH, USEARCH_OVERLAP and
     *  any Break Iterator are ignored.
     *
     *  Matches obey the following constraints:
     *
     *      Characters at the start or end positions of a match that are ignorable
     *      for collation are not included as part of the match, unless they
     *      are part of a combining sequence, as described below.
     *
     *      A match will not include a partial combining sequence.  Combining
     *      character sequences  are considered to be  inseperable units,
     *      and either match the pattern completely, or are considered to not match
     *      at all.  Thus, for example, an A followed a combining accent mark will 
     *      not be found when searching for a plain (unaccented) A.   (unless
     *      the collation strength has been set to ignore all accents).
     *
     *      When beginning a search, the initial starting position, startIdx,
     *      is assumed to be an acceptable match boundary with respect to
     *      combining characters.  A combining sequence that spans across the
     *      starting point will not supress a match beginning at startIdx.
     *
     *      Characters that expand to multiple collation elements
     *      (German sharp-S becoming 'ss', or the composed forms of accented
     *      characters, for example) also must match completely.
     *      Searching for a single 's' in a string containing only a sharp-s will 
     *      find no match.
     *
     *
     *  @param strsrch    the UStringSearch struct, which references both
     *                    the text to be searched  and the pattern being sought.
     *  @param startIdx   The index into the text to begin the search.
     *  @param matchStart An out parameter, the starting index of the matched text.
     *                    This parameter may be NULL.
     *                    A value of -1 will be returned if no match was found.
     *  @param matchLimit Out parameter, the index of the first position following the matched text.
     *                    The matchLimit will be at a suitable position for beginning a subsequent search
     *                    in the input text.
     *                    This parameter may be NULL.
     *                    A value of -1 will be returned if no match was found.
     *          
     *  @param status     Report any errors.  Note that no match found is not an error.
     *  @return           TRUE if a match was found, FALSE otherwise.
     *
     *  @internal
     */
    private boolean searchBackwards( int startIdx )
    {
        // Input parameter sanity check.
        if(m_pattern_.m_CELength_ == 0   ||
           startIdx < 0                  ||
           startIdx > m_textLimitOffset_ ||
           m_pattern_.m_CE_ == null) {
               return false;
        }

        if (m_pattern_.m_PCELength_ == 0) {
            initializePatternPCETable();
        }

        CollationElementBuffer ceb = new CollationElementBuffer(this, m_colEIter_);
        int targetIx = 0;

        /*
         * Pre-load the buffer with the CE's for the grapheme
         * after our starting position so that we're sure that
         * we can look at the CE following the match when we
         * check the match boundaries.
         *
         * This will also pre-fetch the first CE that we'll
         * consider for the match.
         */
        if (startIdx < m_textLimitOffset_) {
            int next = m_charBreakIter_.following(startIdx);
            m_colEIter_.setOffset(next);
            for (targetIx = 0; ; targetIx += 1) {
                if (ceb.getPrevious(targetIx).lowIndex < startIdx) {
                    break;
                }
            }
        } else {
            m_colEIter_.setOffset(startIdx);
        }

        CollationElementBuffer.CEI targetCEI = null;
        int patIx;
        boolean found;

        int  limitIx = targetIx;
        int  mStart = -1;
        int  mLimit = -1;
        int  minLimit;
        int  maxLimit;

        // Outer loop moves over match starting positions in the
        //      target CE space.
        // Here, targetIx values increase toward the beginning of the base text (i.e. we get the text CEs in reverse order).
        // But  patIx is 0 at the beginning of the pattern and increases toward the end.
        // So this loop performs a comparison starting with the end of pattern, and prcessd toward the beginning of the pattern
        // and the beginning of the base text.
        for(targetIx = limitIx; ; targetIx += 1)
        {
            found = true;
            // For targetIx > limitIx, this ceb.getPrevious gets a CE that is as far back in the ring buffer
            // (compared to the last CE fetched for the previous targetIx value) as we need to go
            // for this targetIx value, so if it is non-NULL then other ceb.getPrevious calls should be OK.
            CollationElementBuffer.CEI lastCEI  = ceb.getPrevious(targetIx);
            if (lastCEI == null) {
                found = false;
                break;
            }
            //  Inner loop checks for a match beginning at each
            //  position from the outer loop.
            int targetIxOffset = 0;
            for (patIx = m_pattern_.m_PCELength_ - 1; patIx >= 0; patIx -= 1) {
                long patCE = m_pattern_.m_PCE_[patIx];

                targetCEI = ceb.getPrevious(targetIx + m_pattern_.m_PCELength_ - 1 - patIx + targetIxOffset);
                //  Compare CE from target string with CE from the pattern.
                //    Note that the target CE will be UCOL_NULLORDER if we reach the end of input,
                //    which will fail the compare, below.
                // TODO: use strsrch->search->elementComparisonType instead 1
                int ceMatch = compareCE64s(targetCEI.ce, patCE, 1);
                if ( ceMatch == U_CE_NO_MATCH ) {
                    found = false;
                    break;
                } else if ( ceMatch > U_CE_NO_MATCH ) {
                    if ( ceMatch == U_CE_SKIP_TARG ) {
                        // redo with same patCE, next targCE
                        patIx++;
                        targetIxOffset++;
                    } else { // ceMatch == U_CE_SKIP_PATN
                        // redo with same targCE, next patCE
                        targetIxOffset--;
                    }
                }
            }

            if (!found && ((targetCEI == null) || (targetCEI.ce != CollationElementIterator.PROCESSED_NULLORDER))) {
                // No match at this targetIx.  Try again at the next.
                continue;
            }

            if (!found) {
                // No match at all, we have run off the end of the target text.
                break;
            }

            // We have found a match in CE space.
            // Now determine the bounds in string index space.
            //  There still is a chance of match failure if the CE range not correspond to
            //     an acceptable character range.
            //
            CollationElementBuffer.CEI firstCEI = ceb.getPrevious(targetIx + m_pattern_.m_PCELength_ - 1 + targetIxOffset);
            mStart   = firstCEI.lowIndex;

            // Check for the start of the match being within a combining sequence.
            //   This can happen if the pattern itself begins with a combining char, and
            //   the match found combining marks in the target text that were attached
            //    to something else.
            //   This type of match should be rejected for not completely consuming a
            //   combining sequence.
            if (!isBreakBoundary(mStart)) {
                found = false;
            }

            // Look at the high index of the first CE in the match. If it's the same as the
            // low index, the first CE in the match is in the middle of an expansion.
            if (mStart == firstCEI.highIndex) {
                found = false;
            }


            minLimit = lastCEI.lowIndex;

            if (targetIx > 0) {
                // Look at the CE following the match.  If it is UCOL_NULLORDER the match
                //   extended to the end of input, and the match is good.

                // Look at the high and low indices of the CE following the match. If
                // they are the same it means one of two things:
                //    1. The match extended to the last CE from the target text, which is OK, or
                //    2. The last CE that was part of the match is in an expansion that extends
                //       to the first CE after the match. In this case, we reject the match.
                CollationElementBuffer.CEI nextCEI  = ceb.getPrevious(targetIx - 1);

                if (nextCEI.lowIndex == nextCEI.highIndex && nextCEI.ce != CollationElementIterator.PROCESSED_NULLORDER) {
                    found = false;
                }

                mLimit = maxLimit = nextCEI.lowIndex;

                //  Advance the match end position to the first acceptable match boundary.
                //    This advances the index over any combining charcters.
                if (minLimit < maxLimit) {
                    int nba = nextBoundaryAfter(minLimit);

                    if (nba >= lastCEI.highIndex) {
                        mLimit = nba;
                    }
                }

                // If advancing to the end of a combining sequence in character indexing space
                //   advanced us beyond the end of the match in CE space, reject this match.
                if (mLimit > maxLimit) {
                    found = false;
                }

                // Make sure the end of the match is on a break boundary
                if (!isBreakBoundary(mLimit)) {
                    found = false;
                }

            } else {
                // No non-ignorable CEs after this point.
                // The maximum position is detected by boundary after
                // the last non-ignorable CE. Combining sequence
                // across the start index will be truncated.
                int nba = nextBoundaryAfter(minLimit);
                mLimit = maxLimit = (nba > 0) && (startIdx > nba) ? nba : startIdx;
            }

            if (! checkIdentical(mStart, mLimit)) {
                found = false;
            }

            if (found) {
                break;
            }
        }

        // All Done.  Store back the match bounds to the caller.
        //
        if (found == false) {
            mLimit = -1;
            mStart = -1;
        } else {
            m_matchedIndex_ = mStart;
            matchLength = mLimit - mStart;
        }

        return found;
    }
    
    
    
    /**
     * Method that does the next exact match
     * @param start the offset to start shifting from and performing the 
     *        next exact match
     */
    private void handleNextExact(int start)
    {
//        int textoffset = shiftForward(start, 
//                                         CollationElementIterator.NULLORDER,
//                                         m_pattern_.m_CELength_);
//        int targetce = CollationElementIterator.IGNORABLE;
//        while (textoffset <= m_textLimitOffset_) {
//            m_colEIter_.setExactOffset(textoffset);
//            int patternceindex = m_pattern_.m_CELength_ - 1;
//            boolean found = false;
//            int lastce = CollationElementIterator.NULLORDER;
//            
//            while (true) {
//                // finding the last pattern ce match, imagine composite 
//                // characters. for example: search for pattern A in text \u00C0
//                // we'll have to skip \u0300 the grave first before we get to A
//                targetce = m_colEIter_.previous();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (targetce == CollationElementIterator.IGNORABLE && 
//                    m_colEIter_.isInBuffer()) { 
//                    // this is for the text \u0315\u0300 that requires 
//                    // normalization and pattern \u0300, where \u0315 is ignorable
//                    continue;
//                }
//                if (lastce == CollationElementIterator.NULLORDER 
//                    || lastce == CollationElementIterator.IGNORABLE) {
//                    lastce = targetce;
//                }
//                if (targetce == m_pattern_.m_CE_[patternceindex]) {
//                    // the first ce can be a contraction
//                    found = true;
//                    break;
//                }
//                if (m_colEIter_.m_CEBufferOffset_ <= 0) {
//                    found = false;
//                    break;
//                }
//            }
//    
//            while (found && patternceindex > 0) {
//                lastce = targetce;
//                targetce = m_colEIter_.previous();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (targetce == CollationElementIterator.IGNORABLE) {
//                    continue;
//                }
//    
//                patternceindex --;
//                found = found && targetce == m_pattern_.m_CE_[patternceindex]; 
//            }
//            
//            targetce = lastce;
//    
//            if (!found) {
//                textoffset = shiftForward(textoffset, lastce, patternceindex);
//                // status checked at loop.
//                patternceindex = m_pattern_.m_CELength_;
//                continue;
//            }
//            
//            if (checkNextExactMatch(textoffset)) {
//                // status checked in ucol_setOffset
//                return;
//            }
//            textoffset = m_utilBuffer_[0];
//        }
//        setMatchNotFound();
        int textOffset = m_colEIter_.getOffset();

        if (!search(textOffset)) {
            setMatchNotFound();
        }

    }

    /**
     * Method that does the next canonical match
     * @param start the offset to start shifting from and performing the 
     *        next canonical match
     */
    private void handleNextCanonical(int start)
    {
//        boolean hasPatternAccents = 
//           m_pattern_.m_hasSuffixAccents_ || m_pattern_.m_hasPrefixAccents_;
//              
//        // shifting it check for setting offset
//        // if setOffset is called previously or there was no previous match, we
//        // leave the offset as it is.
//        int textoffset = shiftForward(start, CollationElementIterator.NULLORDER, 
//                                        m_pattern_.m_CELength_);
//        m_canonicalPrefixAccents_.delete(0, m_canonicalPrefixAccents_.length());
//        m_canonicalSuffixAccents_.delete(0, m_canonicalSuffixAccents_.length());
//        int targetce = CollationElementIterator.IGNORABLE;
//        
//        while (textoffset <= m_textLimitOffset_)
//        {
//            m_colEIter_.setExactOffset(textoffset);
//            int patternceindex = m_pattern_.m_CELength_ - 1;
//            boolean found = false;
//            int lastce = CollationElementIterator.NULLORDER;
//            
//            while (true) {
//                // finding the last pattern ce match, imagine composite characters
//                // for example: search for pattern A in text \u00C0
//                // we'll have to skip \u0300 the grave first before we get to A
//                targetce = m_colEIter_.previous();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (lastce == CollationElementIterator.NULLORDER 
//                            || lastce == CollationElementIterator.IGNORABLE) {
//                    lastce = targetce;
//                }
//                if (targetce == m_pattern_.m_CE_[patternceindex]) {
//                    // the first ce can be a contraction
//                    found = true;
//                    break;
//                }
//                if (m_colEIter_.m_CEBufferOffset_ <= 0) {
//                    found = false;
//                    break;
//                }
//            }
//            
//            while (found && patternceindex > 0) {
//                targetce    = m_colEIter_.previous();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce    = getCE(targetce);
//                if (targetce == CollationElementIterator.IGNORABLE) {
//                    continue;
//                }
//    
//                patternceindex --;
//                found = found && targetce == m_pattern_.m_CE_[patternceindex]; 
//            }
//    
//            // initializing the rearranged accent array
//            if (hasPatternAccents && !found) {
//                found = doNextCanonicalMatch(textoffset);
//            }
//    
//            if (!found) {
//                textoffset = shiftForward(textoffset, lastce, patternceindex);
//                // status checked at loop
//                patternceindex = m_pattern_.m_CELength_;
//                continue;
//            }
//            
//            if (checkNextCanonicalMatch(textoffset)) {
//                return;
//            }
//            textoffset = m_utilBuffer_[0];
//        }
//        setMatchNotFound();
      int textOffset = m_colEIter_.getOffset();

                if (!search(textOffset)) {
                    setMatchNotFound();
                }

    }
    
    /**
     * Method that does the previous exact match
     * @param start the offset to start shifting from and performing the 
     *        previous exact match
     */
    private void handlePreviousExact(int start)
    {
//        int textoffset = reverseShift(start, CollationElementIterator.NULLORDER, 
//                                      m_pattern_.m_CELength_);
//        while (textoffset >= m_textBeginOffset_)
//        {
//            m_colEIter_.setExactOffset(textoffset);
//            int patternceindex = 1;
//            int targetce = CollationElementIterator.IGNORABLE;
//            boolean found = false;
//            int firstce = CollationElementIterator.NULLORDER;
//            
//            while (true) {
//                // finding the first pattern ce match, imagine composite 
//                // characters. for example: search for pattern \u0300 in text 
//                // \u00C0, we'll have to skip A first before we get to 
//                // \u0300 the grave accent
//                targetce = m_colEIter_.next();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (firstce == CollationElementIterator.NULLORDER 
//                    || firstce == CollationElementIterator.IGNORABLE) {
//                    firstce = targetce;
//                }
//                if (targetce == CollationElementIterator.IGNORABLE && m_collator_.getStrength() != Collator.PRIMARY) {
//                    continue;
//                }         
//                if (targetce == m_pattern_.m_CE_[0]) {
//                    found = true;
//                    break;
//                }
//                if (m_colEIter_.m_CEBufferOffset_ == -1 
//                    || m_colEIter_.m_CEBufferOffset_ 
//                                            == m_colEIter_.m_CEBufferSize_) {
//                    // checking for accents in composite character
//                    found = false;
//                    break;
//                }
//            }
//    
//            //targetce = firstce;
//            
//            while (found && patternceindex < m_pattern_.m_CELength_) {
//                firstce = targetce;
//                targetce = m_colEIter_.next();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (targetce == CollationElementIterator.IGNORABLE) {
//                    continue;
//                }
//    
//                found = found && targetce == m_pattern_.m_CE_[patternceindex]; 
//                patternceindex ++;
//            }
//            
//            targetce = firstce;
//    
//            if (!found) {
//                textoffset = reverseShift(textoffset, targetce, patternceindex);
//                patternceindex = 0;
//                continue;
//            }
//            
//            if (checkPreviousExactMatch(textoffset)) {
//                return;
//            }
//            textoffset = m_utilBuffer_[0];
//        }
//        setMatchNotFound();
        
        int textOffset;

        if (isOverlapping()) {
            if (m_matchedIndex_ != DONE) {
                textOffset = m_matchedIndex_ + matchLength - 1;
            } else {
                // move the start position at the end of possible match
                initializePatternPCETable();
                for (int nPCEs = 0; nPCEs < m_pattern_.m_PCELength_ - 1; nPCEs++) {
                    long pce = m_colEIter_.nextProcessed().getCE();
                    if (pce == CollationElementIterator.PROCESSED_NULLORDER) {
                        // at the end of the text
                        break;
                    }
                }
                textOffset = m_colEIter_.getOffset();
            }
        } else {
            textOffset = m_colEIter_.getOffset();
        }

        if (!searchBackwards(textOffset)) {
            setMatchNotFound();
        }
        
    }
    
    /**
     * Method that does the previous canonical match
     * @param start the offset to start shifting from and performing the 
     *        previous canonical match
     */
    private void handlePreviousCanonical(int start)
    {
//        boolean hasPatternAccents = 
//           m_pattern_.m_hasSuffixAccents_ || m_pattern_.m_hasPrefixAccents_;
//              
//        // shifting it check for setting offset
//        // if setOffset is called previously or there was no previous match, we
//        // leave the offset as it is.
//        int textoffset = reverseShift(start, CollationElementIterator.NULLORDER, 
//                                          m_pattern_.m_CELength_);
//        m_canonicalPrefixAccents_.delete(0, m_canonicalPrefixAccents_.length());
//        m_canonicalSuffixAccents_.delete(0, m_canonicalSuffixAccents_.length());
//        
//        while (textoffset >= m_textBeginOffset_)
//        {
//            m_colEIter_.setExactOffset(textoffset);
//            int patternceindex = 1;
//            int targetce = CollationElementIterator.IGNORABLE;
//            boolean found = false;
//            int firstce = CollationElementIterator.NULLORDER;
//            
//            while (true) {
//                // finding the first pattern ce match, imagine composite 
//                // characters. for example: search for pattern \u0300 in text 
//                // \u00C0, we'll have to skip A first before we get to 
//                // \u0300 the grave accent
//                targetce = m_colEIter_.next();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (firstce == CollationElementIterator.NULLORDER 
//                    || firstce == CollationElementIterator.IGNORABLE) {
//                    firstce = targetce;
//                }
//                
//                if (targetce == m_pattern_.m_CE_[0]) {
//                    // the first ce can be a contraction
//                    found = true;
//                    break;
//                }
//                if (m_colEIter_.m_CEBufferOffset_ == -1 
//                    || m_colEIter_.m_CEBufferOffset_ 
//                                            == m_colEIter_.m_CEBufferSize_) {
//                    // checking for accents in composite character
//                    found = false;
//                    break;
//                }
//            }
//    
//            targetce = firstce;
//            
//            while (found && patternceindex < m_pattern_.m_CELength_) {
//                targetce = m_colEIter_.next();
//                if (targetce == CollationElementIterator.NULLORDER) {
//                    found = false;
//                    break;
//                }
//                targetce = getCE(targetce);
//                if (targetce == CollationElementIterator.IGNORABLE) {
//                    continue;
//                }
//    
//                found = found && targetce == m_pattern_.m_CE_[patternceindex]; 
//                patternceindex ++;
//            }
//    
//            // initializing the rearranged accent array
//            if (hasPatternAccents && !found) {
//                found = doPreviousCanonicalMatch(textoffset);
//            }
//    
//            if (!found) {
//                textoffset = reverseShift(textoffset, targetce, patternceindex);
//                patternceindex = 0;
//                continue;
//            }
//    
//            if (checkPreviousCanonicalMatch(textoffset)) {
//                return;
//            }
//            textoffset = m_utilBuffer_[0];
//        }
//        setMatchNotFound();
      int textOffset;

                if (isOverlapping()) {
                    if (m_matchedIndex_ != DONE) {
                        textOffset = m_matchedIndex_ + matchLength - 1;
                    } else {
                        // move the start position at the end of possible match
                        initializePatternPCETable();
                        for (int nPCEs = 0; nPCEs < m_pattern_.m_PCELength_ - 1; nPCEs++) {
                            long pce = m_colEIter_.nextProcessed().getCE();
                            if (pce == CollationElementIterator.PROCESSED_NULLORDER) {
                                // at the end of the text
                                break;
                            }
                        }
                        textOffset = m_colEIter_.getOffset();
                    }
                } else {
                    textOffset = m_colEIter_.getOffset();
                }

                if (!searchBackwards(textOffset)) {
                    setMatchNotFound();
                }
    }
    
    /**
     * Gets a substring out of a CharacterIterator
     * @param text CharacterIterator
     * @param start start offset
     * @param length of substring
     * @return substring from text starting at start and length length
     */
    private static final String getString(CharacterIterator text, int start,
                                            int length)
    {
        StringBuilder result = new StringBuilder(length);
        int offset = text.getIndex();
        text.setIndex(start);
        for (int i = 0; i < length; i ++) {
            result.append(text.current());
            text.next();
        }
        text.setIndex(offset);
        return result.toString();
    }
    
    /**
     * Getting the mask for collation strength
     * @param strength collation strength
      * @return collation element mask
     */
    private static final int getMask(int strength) 
    {
        switch (strength) 
        {
            case Collator.PRIMARY:
                return RuleBasedCollator.CE_PRIMARY_MASK_;
            case Collator.SECONDARY:
                return RuleBasedCollator.CE_SECONDARY_MASK_ 
                       | RuleBasedCollator.CE_PRIMARY_MASK_;
            default:
                return RuleBasedCollator.CE_TERTIARY_MASK_ 
                       | RuleBasedCollator.CE_SECONDARY_MASK_ 
                       | RuleBasedCollator.CE_PRIMARY_MASK_;
        }
    }
    
    /**
     * Sets match not found 
     */
    private void setMatchNotFound() 
    {
        // this method resets the match result regardless of the error status.
        m_matchedIndex_ = DONE;
        setMatchLength(0);
    }
    
    /**
     * Check the boundaries of the match.
     */
    private int checkBreakBoundary(int end) {
        if (!m_charBreakIter_.isBoundary(end)) {
            end = m_charBreakIter_.following(end);
        }
        return end;
    }
}
