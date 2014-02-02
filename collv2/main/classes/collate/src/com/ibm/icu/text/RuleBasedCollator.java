/**
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ibm.icu.impl.BOCU;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.Normalizer2Impl;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.impl.Normalizer2Impl.ReorderingBuffer;
import com.ibm.icu.impl.coll.Collation;
import com.ibm.icu.impl.coll.CollationCompare;
import com.ibm.icu.impl.coll.CollationData;
import com.ibm.icu.impl.coll.CollationFastLatin;
import com.ibm.icu.impl.coll.CollationKeys;
import com.ibm.icu.impl.coll.CollationKeys.SortKeyByteSink;
import com.ibm.icu.impl.coll.CollationRoot;
import com.ibm.icu.impl.coll.CollationSettings;
import com.ibm.icu.impl.coll.CollationTailoring;
import com.ibm.icu.impl.coll.FCDUTF16CollationIterator;
import com.ibm.icu.impl.coll.SharedObject;
import com.ibm.icu.impl.coll.UTF16CollationIterator;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.VersionInfo;

/**
 * <p>
 * RuleBasedCollator is a concrete subclass of Collator. It allows customization of the Collator via user-specified rule
 * sets. RuleBasedCollator is designed to be fully compliant to the <a
 * href="http://www.unicode.org/unicode/reports/tr10/">Unicode Collation Algorithm (UCA)</a> and conforms to ISO 14651.
 * </p>
 * 
 * <p>
 * Users are strongly encouraged to read <a href="http://www.icu-project.org/userguide/Collate_Intro.html"> the users
 * guide</a> for more information about the collation service before using this class.
 * </p>
 * 
 * <p>
 * Create a RuleBasedCollator from a locale by calling the getInstance(Locale) factory method in the base class
 * Collator. Collator.getInstance(Locale) creates a RuleBasedCollator object based on the collation rules defined by the
 * argument locale. If a customized collation ordering or attributes is required, use the RuleBasedCollator(String)
 * constructor with the appropriate rules. The customized RuleBasedCollator will base its ordering on UCA, while
 * re-adjusting the attributes and orders of the characters in the specified rule accordingly.
 * </p>
 * 
 * <p>
 * RuleBasedCollator provides correct collation orders for most locales supported in ICU. If specific data for a locale
 * is not available, the orders eventually falls back to the <a href="http://www.unicode.org/unicode/reports/tr10/">UCA
 * collation order </a>.
 * </p>
 * 
 * <p>
 * For information about the collation rule syntax and details about customization, please refer to the <a
 * href="http://www.icu-project.org/userguide/Collate_Customization.html"> Collation customization</a> section of the
 * user's guide.
 * </p>
 * 
 * <p>
 * <strong>Note</strong> that there are some differences between the Collation rule syntax used in Java and ICU4J:
 * 
 * <ul>
 * <li>According to the JDK documentation: <i>
 * <p>
 * Modifier '!' : Turns on Thai/Lao vowel-consonant swapping. If this rule is in force when a Thai vowel of the range
 * &#92;U0E40-&#92;U0E44 precedes a Thai consonant of the range &#92;U0E01-&#92;U0E2E OR a Lao vowel of the range
 * &#92;U0EC0-&#92;U0EC4 precedes a Lao consonant of the range &#92;U0E81-&#92;U0EAE then the vowel is placed after the
 * consonant for collation purposes.
 * </p>
 * <p>
 * If a rule is without the modifier '!', the Thai/Lao vowel-consonant swapping is not turned on.
 * </p>
 * </i>
 * <p>
 * ICU4J's RuleBasedCollator does not support turning off the Thai/Lao vowel-consonant swapping, since the UCA clearly
 * states that it has to be supported to ensure a correct sorting order. If a '!' is encountered, it is ignored.
 * </p>
 * <li>As mentioned in the documentation of the base class Collator, compatibility decomposition mode is not supported.
 * </ul>
 * <p>
 * <strong>Examples</strong>
 * </p>
 * <p>
 * Creating Customized RuleBasedCollators: <blockquote>
 * 
 * <pre>
 * String simple = "&amp; a &lt; b &lt; c &lt; d";
 * RuleBasedCollator simpleCollator = new RuleBasedCollator(simple);
 *
 * String norwegian = "&amp; a , A &lt; b , B &lt; c , C &lt; d , D &lt; e , E "
 *                    + "&lt; f , F &lt; g , G &lt; h , H &lt; i , I &lt; j , "
 *                    + "J &lt; k , K &lt; l , L &lt; m , M &lt; n , N &lt; "
 *                    + "o , O &lt; p , P &lt; q , Q &lt r , R &lt s , S &lt; "
 *                    + "t , T &lt; u , U &lt; v , V &lt; w , W &lt; x , X "
 *                    + "&lt; y , Y &lt; z , Z &lt; &#92;u00E5 = a&#92;u030A "
 *                    + ", &#92;u00C5 = A&#92;u030A ; aa , AA &lt; &#92;u00E6 "
 *                    + ", &#92;u00C6 &lt; &#92;u00F8 , &#92;u00D8";
 * RuleBasedCollator norwegianCollator = new RuleBasedCollator(norwegian);
 * </pre>
 * 
 * </blockquote>
 * 
 * Concatenating rules to combine <code>Collator</code>s: <blockquote>
 * 
 * <pre>
 * // Create an en_US Collator object
 * RuleBasedCollator en_USCollator = (RuleBasedCollator)
 *     Collator.getInstance(new Locale("en", "US", ""));
 * // Create a da_DK Collator object
 * RuleBasedCollator da_DKCollator = (RuleBasedCollator)
 *     Collator.getInstance(new Locale("da", "DK", ""));
 * // Combine the two
 * // First, get the collation rules from en_USCollator
 * String en_USRules = en_USCollator.getRules();
 * // Second, get the collation rules from da_DKCollator
 * String da_DKRules = da_DKCollator.getRules();
 * RuleBasedCollator newCollator =
 *                             new RuleBasedCollator(en_USRules + da_DKRules);
 * // newCollator has the combined rules
 * </pre>
 * 
 * </blockquote>
 * 
 * Making changes to an existing RuleBasedCollator to create a new <code>Collator</code> object, by appending changes to
 * the existing rule: <blockquote>
 * 
 * <pre>
 * // Create a new Collator object with additional rules
 * String addRules = "&amp; C &lt; ch, cH, Ch, CH";
 * RuleBasedCollator myCollator =
 *     new RuleBasedCollator(en_USCollator.getRules() + addRules);
 * // myCollator contains the new rules
 * </pre>
 * 
 * </blockquote>
 * 
 * How to change the order of non-spacing accents: <blockquote>
 * 
 * <pre>
 * // old rule with main accents
 * String oldRules = "= &#92;u0301 ; &#92;u0300 ; &#92;u0302 ; &#92;u0308 "
 *                 + "; &#92;u0327 ; &#92;u0303 ; &#92;u0304 ; &#92;u0305 "
 *                 + "; &#92;u0306 ; &#92;u0307 ; &#92;u0309 ; &#92;u030A "
 *                 + "; &#92;u030B ; &#92;u030C ; &#92;u030D ; &#92;u030E "
 *                 + "; &#92;u030F ; &#92;u0310 ; &#92;u0311 ; &#92;u0312 "
 *                 + "&lt; a , A ; ae, AE ; &#92;u00e6 , &#92;u00c6 "
 *                 + "&lt; b , B &lt; c, C &lt; e, E &amp; C &lt; d , D";
 * // change the order of accent characters
 * String addOn = "&amp; &#92;u0300 ; &#92;u0308 ; &#92;u0302";
 * RuleBasedCollator myCollator = new RuleBasedCollator(oldRules + addOn);
 * </pre>
 * 
 * </blockquote>
 * 
 * Putting in a new primary ordering before the default setting, e.g. sort English characters before or after Japanese
 * characters in the Japanese <code>Collator</code>: <blockquote>
 * 
 * <pre>
 * // get en_US Collator rules
 * RuleBasedCollator en_USCollator
 *                        = (RuleBasedCollator)Collator.getInstance(Locale.US);
 * // add a few Japanese characters to sort before English characters
 * // suppose the last character before the first base letter 'a' in
 * // the English collation rule is &#92;u2212
 * String jaString = "& &#92;u2212 &lt &#92;u3041, &#92;u3042 &lt &#92;u3043, "
 *                   + "&#92;u3044";
 * RuleBasedCollator myJapaneseCollator
 *              = new RuleBasedCollator(en_USCollator.getRules() + jaString);
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <p>
 * This class is not subclassable
 * </p>
 * 
 * @author Syn Wee Quek
 * @stable ICU 2.8
 */
public final class RuleBasedCollator extends Collator {
    // TODO: ICU4C API returns UCollationResult defined as enum.
    // ICU4J uses int - should we define these somewhere?
    private static final int UCOL_EQUAL = 0;
    private static final int UCOL_GREATER = 1;
    private static final int UCOL_LESS = -1;

    // public constructors ---------------------------------------------------

    /**
     * <p>
     * Constructor that takes the argument rules for customization. The collator will be based on UCA, with the
     * attributes and re-ordering of the characters specified in the argument rules.
     * </p>
     * <p>
     * See the user guide's section on <a href="http://www.icu-project.org/userguide/Collate_Customization.html">
     * Collation Customization</a> for details on the rule syntax.
     * </p>
     * 
     * @param rules
     *            the collation rules to build the collation table from.
     * @exception ParseException
     *                and IOException thrown. ParseException thrown when argument rules have an invalid syntax.
     *                IOException thrown when an error occured while reading internal data.
     * @stable ICU 2.8
     */
    public RuleBasedCollator(String rules) throws Exception {
        if (rules == null) {
            throw new IllegalArgumentException("Collation rules can not be null");
        }
        // TODO: build! try to use reflection.
    }

    // public methods --------------------------------------------------------

    /**
     * Clones the RuleBasedCollator
     * 
     * @return a new instance of this RuleBasedCollator object
     * @stable ICU 2.8
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    /**
     * Return a CollationElementIterator for the given String.
     * 
     * @see CollationElementIterator
     * @stable ICU 2.8
     */
    public CollationElementIterator getCollationElementIterator(String source) {
        return new CollationElementIterator(source, this);
    }

    /**
     * Return a CollationElementIterator for the given CharacterIterator. The source iterator's integrity will be
     * preserved since a new copy will be created for use.
     * 
     * @see CollationElementIterator
     * @stable ICU 2.8
     */
    public CollationElementIterator getCollationElementIterator(CharacterIterator source) {
        CharacterIterator newsource = (CharacterIterator) source.clone();
        return new CollationElementIterator(newsource, this);
    }

    /**
     * Return a CollationElementIterator for the given UCharacterIterator. The source iterator's integrity will be
     * preserved since a new copy will be created for use.
     * 
     * @see CollationElementIterator
     * @stable ICU 2.8
     */
    public CollationElementIterator getCollationElementIterator(UCharacterIterator source) {
        return new CollationElementIterator(source, this);
    }

    // Freezable interface implementation -------------------------------------------------

    /**
     * Determines whether the object has been frozen or not.
     * @stable ICU 4.8
     */
    @Override
    public boolean isFrozen() {
        return frozenLock != null;
    }

    /**
     * Freezes the collator.
     * @return the collator itself.
     * @stable ICU 4.8
     */
    @Override
    public Collator freeze() {
        if (!isFrozen()) {
            frozenLock = new ReentrantLock();
            if (collationBuffer == null) {
                collationBuffer = new CollationBuffer(data);
            }
        }
        return this;
    }

    /**
     * Provides for the clone operation. Any clone is initially unfrozen.
     * @stable ICU 4.8
     */
    @Override
    public RuleBasedCollator cloneAsThawed() {
        try {
            RuleBasedCollator result = (RuleBasedCollator) super.clone();
            // since all collation data in the RuleBasedCollator do not change
            // we can safely assign the result.fields to this collator 
            // except in cases where we can't
            result.settings = settings.clone();
            result.collationBuffer = null;
            result.frozenLock = null;
            return result;
        } catch (CloneNotSupportedException e) {
            // Clone is implemented
            return null;
        }
    }

    // public setters --------------------------------------------------------

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen RuleBasedCollator");
        }
    }

    private final CollationSettings getOwnedSettings() {
        return settings.copyOnWrite();
    }

    private final CollationSettings getDefaultSettings() {
        return tailoring.settings.readOnly();
    }

    /**
     * Sets the Hiragana Quaternary mode to be on or off. When the Hiragana Quaternary mode is turned on, the collator
     * positions Hiragana characters before all non-ignorable characters in QUATERNARY strength. This is to produce a
     * correct JIS collation order, distinguishing between Katakana and Hiragana characters.
     *
     * This attribute is an implementation detail of the CLDR Japanese tailoring.
     * The implementation might change to use a different mechanism
     * to achieve the same Japanese sort order.
     * Since ICU 50, this attribute is not settable any more via API functions.
     * 
     * @param flag
     *            true if Hiragana Quaternary mode is to be on, false otherwise
     * @see #setHiraganaQuaternaryDefault
     * @see #isHiraganaQuaternary
     * @deprecated ICU 50 Implementation detail, cannot be set via API, might be removed from implementation.
     */
    public void setHiraganaQuaternary(boolean flag) {
        checkNotFrozen();
    }

    /**
     * Sets the Hiragana Quaternary mode to the initial mode set during construction of the RuleBasedCollator. See
     * setHiraganaQuaternary(boolean) for more details.
     *
     * This attribute is an implementation detail of the CLDR Japanese tailoring.
     * The implementation might change to use a different mechanism
     * to achieve the same Japanese sort order.
     * Since ICU 50, this attribute is not settable any more via API functions.
     * 
     * @see #setHiraganaQuaternary(boolean)
     * @see #isHiraganaQuaternary
     * @deprecated ICU 50 Implementation detail, cannot be set via API, might be removed from implementation.
     */
    public void setHiraganaQuaternaryDefault() {
        checkNotFrozen();
    }

    /**
     * Sets whether uppercase characters sort before lowercase characters or vice versa, in strength TERTIARY. The
     * default mode is false, and so lowercase characters sort before uppercase characters. If true, sort upper case
     * characters first.
     * 
     * @param upperfirst
     *            true to sort uppercase characters before lowercase characters, false to sort lowercase characters
     *            before uppercase characters
     * @see #isLowerCaseFirst
     * @see #isUpperCaseFirst
     * @see #setLowerCaseFirst
     * @see #setCaseFirstDefault
     * @stable ICU 2.8
     */
    public void setUpperCaseFirst(boolean upperfirst) {
        checkNotFrozen();
        if (upperfirst == isUpperCaseFirst()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setCaseFirst(upperfirst ? CollationSettings.CASE_FIRST_AND_UPPER_MASK : 0);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the orders of lower cased characters to sort before upper cased characters, in strength TERTIARY. The
     * default mode is false. If true is set, the RuleBasedCollator will sort lower cased characters before the upper
     * cased ones. Otherwise, if false is set, the RuleBasedCollator will ignore case preferences.
     * 
     * @param lowerfirst
     *            true for sorting lower cased characters before upper cased characters, false to ignore case
     *            preferences.
     * @see #isLowerCaseFirst
     * @see #isUpperCaseFirst
     * @see #setUpperCaseFirst
     * @see #setCaseFirstDefault
     * @stable ICU 2.8
     */
    public void setLowerCaseFirst(boolean lowerfirst) {
        checkNotFrozen();
        if (lowerfirst == isLowerCaseFirst()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setCaseFirst(lowerfirst ? CollationSettings.CASE_FIRST : 0);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the case first mode to the initial mode set during construction of the RuleBasedCollator. See
     * setUpperCaseFirst(boolean) and setLowerCaseFirst(boolean) for more details.
     * 
     * @see #isLowerCaseFirst
     * @see #isUpperCaseFirst
     * @see #setLowerCaseFirst(boolean)
     * @see #setUpperCaseFirst(boolean)
     * @stable ICU 2.8
     */
    public final void setCaseFirstDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setCaseFirstDefault(defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the alternate handling mode to the initial mode set during construction of the RuleBasedCollator. See
     * setAlternateHandling(boolean) for more details.
     * 
     * @see #setAlternateHandlingShifted(boolean)
     * @see #isAlternateHandlingShifted()
     * @stable ICU 2.8
     */
    public void setAlternateHandlingDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setAlternateHandlingDefault(defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the case level mode to the initial mode set during construction of the RuleBasedCollator. See
     * setCaseLevel(boolean) for more details.
     * 
     * @see #setCaseLevel(boolean)
     * @see #isCaseLevel
     * @stable ICU 2.8
     */
    public void setCaseLevelDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlagDefault(CollationSettings.CASE_LEVEL, defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the decomposition mode to the initial mode set during construction of the RuleBasedCollator. See
     * setDecomposition(int) for more details.
     * 
     * @see #getDecomposition
     * @see #setDecomposition(int)
     * @stable ICU 2.8
     */
    public void setDecompositionDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlagDefault(CollationSettings.CHECK_FCD, defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the French collation mode to the initial mode set during construction of the RuleBasedCollator. See
     * setFrenchCollation(boolean) for more details.
     * 
     * @see #isFrenchCollation
     * @see #setFrenchCollation(boolean)
     * @stable ICU 2.8
     */
    public void setFrenchCollationDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlagDefault(CollationSettings.BACKWARD_SECONDARY, defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the collation strength to the initial mode set during the construction of the RuleBasedCollator. See
     * setStrength(int) for more details.
     * 
     * @see #setStrength(int)
     * @see #getStrength
     * @stable ICU 2.8
     */
    public void setStrengthDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setStrengthDefault(defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Method to set numeric collation to its default value. When numeric collation is turned on, this Collator
     * generates a collation key for the numeric value of substrings of digits. This is a way to get '100' to sort AFTER
     * '2'
     * 
     * @see #getNumericCollation
     * @see #setNumericCollation
     * @stable ICU 2.8
     */
    public void setNumericCollationDefault() {
        checkNotFrozen();
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlagDefault(CollationSettings.NUMERIC, defaultSettings.options);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the mode for the direction of SECONDARY weights to be used in French collation. The default value is false,
     * which treats SECONDARY weights in the order they appear. If set to true, the SECONDARY weights will be sorted
     * backwards. See the section on <a href="http://www.icu-project.org/userguide/Collate_ServiceArchitecture.html">
     * French collation</a> for more information.
     * 
     * @param flag
     *            true to set the French collation on, false to set it off
     * @stable ICU 2.8
     * @see #isFrenchCollation
     * @see #setFrenchCollationDefault
     */
    public void setFrenchCollation(boolean flag) {
        checkNotFrozen();
        if(flag == isFrenchCollation()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlag(CollationSettings.BACKWARD_SECONDARY, flag);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the alternate handling for QUATERNARY strength to be either shifted or non-ignorable. See the UCA definition
     * on <a href="http://www.unicode.org/unicode/reports/tr10/#Variable_Weighting"> Alternate Weighting</a>. This
     * attribute will only be effective when QUATERNARY strength is set. The default value for this mode is false,
     * corresponding to the NON_IGNORABLE mode in UCA. In the NON-IGNORABLE mode, the RuleBasedCollator will treats all
     * the codepoints with non-ignorable primary weights in the same way. If the mode is set to true, the behaviour
     * corresponds to SHIFTED defined in UCA, this causes codepoints with PRIMARY orders that are equal or below the
     * variable top value to be ignored in PRIMARY order and moved to the QUATERNARY order.
     * 
     * @param shifted
     *            true if SHIFTED behaviour for alternate handling is desired, false for the NON_IGNORABLE behaviour.
     * @see #isAlternateHandlingShifted
     * @see #setAlternateHandlingDefault
     * @stable ICU 2.8
     */
    public void setAlternateHandlingShifted(boolean shifted) {
        checkNotFrozen();
        if(shifted == isAlternateHandlingShifted()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setAlternateHandlingShifted(shifted);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * <p>
     * When case level is set to true, an additional weight is formed between the SECONDARY and TERTIARY weight, known
     * as the case level. The case level is used to distinguish large and small Japanese Kana characters. Case level
     * could also be used in other situations. For example to distinguish certain Pinyin characters. The default value
     * is false, which means the case level is not generated. The contents of the case level are affected by the case
     * first mode. A simple way to ignore accent differences in a string is to set the strength to PRIMARY and enable
     * case level.
     * </p>
     * <p>
     * See the section on <a href="http://www.icu-project.org/userguide/Collate_ServiceArchitecture.html"> case
     * level</a> for more information.
     * </p>
     * 
     * @param flag
     *            true if case level sorting is required, false otherwise
     * @stable ICU 2.8
     * @see #setCaseLevelDefault
     * @see #isCaseLevel
     */
    public void setCaseLevel(boolean flag) {
        checkNotFrozen();
        if(flag == isCaseLevel()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlag(CollationSettings.CASE_LEVEL, flag);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets the decomposition mode of this Collator.  Setting this
     * decomposition attribute with CANONICAL_DECOMPOSITION allows the
     * Collator to handle un-normalized text properly, producing the
     * same results as if the text were normalized. If
     * NO_DECOMPOSITION is set, it is the user's responsibility to
     * insure that all text is already in the appropriate form before
     * a comparison or before getting a CollationKey. Adjusting
     * decomposition mode allows the user to select between faster and
     * more complete collation behavior.</p>
     *
     * <p>Since a great many of the world's languages do not require
     * text normalization, most locales set NO_DECOMPOSITION as the
     * default decomposition mode.</p>
     *
     * The default decompositon mode for the Collator is
     * NO_DECOMPOSITON, unless specified otherwise by the locale used
     * to create the Collator.</p>
     *
     * <p>See getDecomposition for a description of decomposition
     * mode.</p>
     *
     * @param decomposition the new decomposition mode
     * @see #getDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     * @throws IllegalArgumentException If the given value is not a valid
     *            decomposition mode.
     * @stable ICU 2.8
     */
    @Override
    public void setDecomposition(int decomposition)
    {
        checkNotFrozen();
        boolean flag;
        switch(decomposition) {
        case NO_DECOMPOSITION:
            flag = false;
            break;
        case CANONICAL_DECOMPOSITION:
            flag = true;
            break;
        default:
            throw new IllegalArgumentException("Wrong decomposition mode.");
        }
        if(flag == settings.readOnly().getFlag(CollationSettings.CHECK_FCD)) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlag(CollationSettings.CHECK_FCD, flag);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * Sets this Collator's strength attribute. The strength attribute determines the minimum level of difference
     * considered significant during comparison.
     *
     * <p>See the Collator class description for an example of use.
     * 
     * @param newStrength
     *            the new strength value.
     * @see #getStrength
     * @see #setStrengthDefault
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     * @exception IllegalArgumentException
     *                If the new strength value is not one of PRIMARY, SECONDARY, TERTIARY, QUATERNARY or IDENTICAL.
     * @stable ICU 2.8
     */
    @Override
    public void setStrength(int newStrength) {
        checkNotFrozen();
        if(newStrength == getStrength()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setStrength(newStrength);
        setFastLatinOptions(ownedSettings);
    }

    /**
     * {@icu} Sets the variable top to the top of the specified reordering group.
     * The variable top determines the highest-sorting character
     * which is affected by the alternate handling behavior.
     * If that attribute is set to NON_IGNORABLE, then the variable top has no effect.
     * @param group one of Collator.ReorderCodes.SPACE, Collator.ReorderCodes.PUNCTUATION,
     *              Collator.ReorderCodes.SYMBOL, Collator.ReorderCodes.CURRENCY;
     *              or Collator.ReorderCodes.DEFAULT to restore the default max variable group
     * @return this
     * @see #getMaxVariable
     * @draft ICU 53
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public RuleBasedCollator setMaxVariable(int group) {
        // Convert the reorder code into a MaxVariable number, or UCOL_DEFAULT=-1.
        int value;
        if(group == Collator.ReorderCodes.DEFAULT) {
            value = -1;  // UCOL_DEFAULT
        } else if(Collator.ReorderCodes.FIRST <= group && group <= Collator.ReorderCodes.CURRENCY) {
            value = group - Collator.ReorderCodes.FIRST;
        } else {
            throw new IllegalArgumentException("illegal max variable group " + group);
        }
        int oldValue = settings.readOnly().getMaxVariable();
        if(value == oldValue) {
            return this;
        }
        CollationSettings defaultSettings = getDefaultSettings();
        if(settings.readOnly() == defaultSettings) {
            if(value < 0) {  // UCOL_DEFAULT
                return this;
            }
        }
        CollationSettings ownedSettings = getOwnedSettings();

        if(group == Collator.ReorderCodes.DEFAULT) {
            group = Collator.ReorderCodes.FIRST + defaultSettings.getMaxVariable();
        }
        long varTop = data.getLastPrimaryForGroup(group);
        assert(varTop != 0);
        ownedSettings.setMaxVariable(value, defaultSettings.options);
        ownedSettings.variableTop = varTop;
        setFastLatinOptions(ownedSettings);
        return this;
    }

    /**
     * {@icu} Returns the maximum reordering group whose characters are affected by
     * the alternate handling behavior.
     * @return the maximum variable reordering group.
     * @see #setMaxVariable
     * @draft ICU 53
     * @provisional This API might change or be removed in a future release.
     */
    @Override
    public int getMaxVariable() {
        return Collator.ReorderCodes.FIRST + settings.readOnly().getMaxVariable();
    }

    /**
     * {@icu} Sets the variable top to the primary weight of the specified string.
     *
     * <p>Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See {@link #setMaxVariable(int)}.
     * 
     * @param varTop
     *            one or more (if contraction) characters to which the variable top should be set
     * @return variable top primary weight
     * @exception IllegalArgumentException
     *                is thrown if varTop argument is not a valid variable top element. A variable top element is
     *                invalid when
     *                <ul>
     *                <li>it is a contraction that does not exist in the Collation order
     *                <li>the variable top is beyond
     *                    the last reordering group supported by setMaxVariable()
     *                <li>when the varTop argument is null or zero in length.
     *                </ul>
     * @see #getVariableTop
     * @see RuleBasedCollator#setAlternateHandlingShifted
     * @deprecated ICU 53 Call {@link #setMaxVariable(int)} instead.
     */
    @Override
    public int setVariableTop(String varTop) {
        checkNotFrozen();
        if (varTop == null || varTop.length() == 0) {
            throw new IllegalArgumentException("Variable top argument string can not be null or zero in length.");
        }
        boolean numeric = settings.readOnly().isNumeric();
        long ce1, ce2;
        if(settings.readOnly().dontCheckFCD()) {
            UTF16CollationIterator ci = new UTF16CollationIterator(data, numeric, varTop, 0);
            ce1 = ci.nextCE();
            ce2 = ci.nextCE();
        } else {
            FCDUTF16CollationIterator ci = new FCDUTF16CollationIterator(data, numeric, varTop, 0);
            ce1 = ci.nextCE();
            ce2 = ci.nextCE();
        }
        if(ce1 == Collation.NO_CE || ce2 != Collation.NO_CE) {
            throw new IllegalArgumentException("Variable top argument string must map to exactly one collation element");
        }
        internalSetVariableTop(ce1 >>> 32);
        return (int)settings.readOnly().variableTop;
    }

    /**
     * {@icu} Sets the variable top to the specified primary weight.
     *
     * <p>Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See {@link #setMaxVariable(int)}.
     * 
     * @param varTop primary weight, as returned by setVariableTop or getVariableTop
     * @see #getVariableTop
     * @see #setVariableTop(String)
     * @deprecated ICU 53 Call setMaxVariable() instead.
     */
    @Override
    public void setVariableTop(int varTop) {
        checkNotFrozen();
        internalSetVariableTop(varTop & 0xffffffffL);
    }

    private void internalSetVariableTop(long varTop) {
        if(varTop != settings.readOnly().variableTop) {
            // Pin the variable top to the end of the reordering group which contains it.
            // Only a few special groups are supported.
            int group = data.getGroupForPrimary(varTop);
            if(group < Collator.ReorderCodes.FIRST || Collator.ReorderCodes.CURRENCY < group) {
                throw new IllegalArgumentException("The variable top must be a primary weight in " +
                        "the space/punctuation/symbols/currency symbols range");
            }
            long v = data.getLastPrimaryForGroup(group);
            assert(v != 0 && v >= varTop);
            varTop = v;
            if(varTop != settings.readOnly().variableTop) {
                CollationSettings ownedSettings = getOwnedSettings();
                ownedSettings.setMaxVariable(group - Collator.ReorderCodes.FIRST,
                        getDefaultSettings().options);
                ownedSettings.variableTop = varTop;
                setFastLatinOptions(ownedSettings);
            }
        }
    }

    /**
     * {@icu} When numeric collation is turned on, this Collator generates a collation key for the numeric value of substrings
     * of digits. This is a way to get '100' to sort AFTER '2'
     * 
     * @param flag
     *            true to turn numeric collation on and false to turn it off
     * @see #getNumericCollation
     * @see #setNumericCollationDefault
     * @stable ICU 2.8
     */
    public void setNumericCollation(boolean flag) {
        checkNotFrozen();
        // sort substrings of digits as numbers
        if(flag == getNumericCollation()) { return; }
        CollationSettings ownedSettings = getOwnedSettings();
        ownedSettings.setFlag(CollationSettings.NUMERIC, flag);
        setFastLatinOptions(ownedSettings);
    }

    /** 
     * Sets the reordering codes for this collator.
     * Collation reordering allows scripts and some other defined blocks of characters 
     * to be moved relative to each other as a block. This reordering is done on top of 
     * the DUCET/CLDR standard collation order. Reordering can specify groups to be placed 
     * at the start and/or the end of the collation order.
     * <p>By default, reordering codes specified for the start of the order are placed in the 
     * order given after a group of “special” non-script blocks. These special groups of characters 
     * are space, punctuation, symbol, currency, and digit. These special groups are represented with
     * {@link Collator.ReorderCodes}. Script groups can be intermingled with 
     * these special non-script blocks if those special blocks are explicitly specified in the reordering.
     * <p>The special code {@link Collator.ReorderCodes#OTHERS OTHERS} stands for any script that is not explicitly 
     * mentioned in the list of reordering codes given. Anything that is after {@link Collator.ReorderCodes#OTHERS OTHERS}
     * will go at the very end of the reordering in the order given.
     * <p>The special reorder code {@link Collator.ReorderCodes#DEFAULT DEFAULT} will reset the reordering for this collator
     * to the default for this collator. The default reordering may be the DUCET/CLDR order or may be a reordering that
     * was specified when this collator was created from resource data or from rules. The 
     * {@link Collator.ReorderCodes#DEFAULT DEFAULT} code <b>must</b> be the sole code supplied when it used. If not
     * that will result in an {@link IllegalArgumentException} being thrown.
     * <p>The special reorder code {@link Collator.ReorderCodes#NONE NONE} will remove any reordering for this collator.
     * The result of setting no reordering will be to have the DUCET/CLDR reordering used. The 
     * {@link Collator.ReorderCodes#NONE NONE} code <b>must</b> be the sole code supplied when it used.
     * @param order the reordering codes to apply to this collator; if this is null or an empty array
     * then this clears any existing reordering
     * @throws IllegalArgumentException if the reordering codes are malformed in any way (e.g. duplicates, multiple reset codes, overlapping equivalent scripts)
     * @see #getReorderCodes
     * @see Collator#getEquivalentReorderCodes
     * @stable ICU 4.8
     */ 
    @Override
    public void setReorderCodes(int... order) {
        checkNotFrozen();
        if(order == null ?
                settings.readOnly().reorderCodes.length == 0 :
                Arrays.equals(order, settings.readOnly().reorderCodes)) {
            return;
        }
        int length = (order != null) ? order.length : 0;
        CollationSettings defaultSettings = getDefaultSettings();
        if(length == 1 && order[0] == Collator.ReorderCodes.DEFAULT) {
            if(settings.readOnly() != defaultSettings) {
                CollationSettings ownedSettings = getOwnedSettings();
                ownedSettings.setReordering(defaultSettings.reorderCodes,
                                            defaultSettings.reorderTable);
                setFastLatinOptions(ownedSettings);
            }
            return;
        }
        CollationSettings ownedSettings = getOwnedSettings();
        if(length == 0) {
            ownedSettings.resetReordering();
        } else {
            byte[] reorderTable = new byte[256];
            data.makeReorderTable(order, order.length, reorderTable);
            ownedSettings.setReordering(order.clone(), reorderTable);
        }
        setFastLatinOptions(ownedSettings);
    }

    private void setFastLatinOptions(CollationSettings ownedSettings) {
        ownedSettings.fastLatinOptions = CollationFastLatin.getOptions(
                data, ownedSettings, ownedSettings.fastLatinPrimaries);
    }

    // public getters --------------------------------------------------------

    /**
     * Gets the collation tailoring rules for this RuleBasedCollator.
     * Equivalent to String getRules(false).
     * 
     * @return the collation tailoring rules
     * @see #getRules(boolean)
     * @stable ICU 2.8
     */
    public String getRules() {
        return tailoring.rules;
    }

    /**
     * Returns current rules. The argument defines whether full rules (UCA + tailored) rules are returned or just the
     * tailoring.
     * 
     * <p>The "UCA rules" are an <i>approximation</i> of the root collator's sort order.
     * They are almost never used or useful at runtime and can be removed from the data.
     * See <a href="http://userguide.icu-project.org/collation/customization#TOC-Building-on-Existing-Locales">User Guide:
     * Collation Customization, Building on Existing Locales</a>
     *
     * <p>{@link #getRules()} should normally be used instead.
     * @param fullrules
     *            true if the rules that defines the full set of collation order is required, otherwise false for
     *            returning only the tailored rules
     * @return the current rules that defines this Collator.
     * @see #getRules()
     * @stable ICU 2.6
     */
    public String getRules(boolean fullrules) {
        if (!fullrules) {
            return tailoring.rules;
        }
        return /* TODO: CollationLoader.getRootRules() + */ tailoring.rules;
    }

    /**
     * Get an UnicodeSet that contains all the characters and sequences tailored in this collator.
     * 
     * @return a pointer to a UnicodeSet object containing all the code points and sequences that may sort differently
     *         than in the UCA.
     * @stable ICU 2.4
     */
    @Override
    public UnicodeSet getTailoredSet() {
        UnicodeSet tailored = new UnicodeSet();
        if(data.base != null) {
            // TODO: new TailoredSet(tailored).forData(data);
        }
        return tailored;
    }

    /**
     * Gets unicode sets containing contractions and/or expansions of a collator
     * 
     * @param contractions
     *            if not null, set to contain contractions
     * @param expansions
     *            if not null, set to contain expansions
     * @param addPrefixes
     *            add the prefix contextual elements to contractions
     * @throws Exception
     *             Throws an exception if any errors occurs.
     * @stable ICU 3.4
     */
    public void getContractionsAndExpansions(UnicodeSet contractions, UnicodeSet expansions, boolean addPrefixes)
            throws Exception {
        if (contractions != null) {
            contractions.clear();
        }
        if (expansions != null) {
            expansions.clear();
        }
        // TODO: new ContractionsAndExpansions(contractions, expansions, null, addPrefixes).forData(data);
    }

    /**
     * Adds the contractions that start with character c to the set.
     * Ignores prefixes. Used by AlphabeticIndex.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    void internalAddContractions(int c, UnicodeSet set) {
        // TODO: new ContractionsAndExpansions(set, null, null, false).forCodePoint(data, c);
    }

    /**
     * <p>
     * Get a Collation key for the argument String source from this RuleBasedCollator.
     * </p>
     * <p>
     * General recommendation: <br>
     * If comparison are to be done to the same String multiple times, it would be more efficient to generate
     * CollationKeys for the Strings and use CollationKey.compareTo(CollationKey) for the comparisons. If the each
     * Strings are compared to only once, using the method RuleBasedCollator.compare(String, String) will have a better
     * performance.
     * </p>
     * <p>
     * See the class documentation for an explanation about CollationKeys.
     * </p>
     * 
     * @param source
     *            the text String to be transformed into a collation key.
     * @return the CollationKey for the given String based on this RuleBasedCollator's collation rules. If the source
     *         String is null, a null CollationKey is returned.
     * @see CollationKey
     * @see #compare(String, String)
     * @see #getRawCollationKey
     * @stable ICU 2.8
     */
    @Override
    public CollationKey getCollationKey(String source) {
        if (source == null) {
            return null;
        }
        CollationBuffer buffer = null;
        try {
            buffer = getCollationBuffer();
            return getCollationKey(source, buffer);
        } finally {
            releaseCollationBuffer(buffer);
        }
    }

    private CollationKey getCollationKey(String source, CollationBuffer buffer) {
        buffer.rawCollationKey = getRawCollationKey(source, buffer.rawCollationKey, buffer);
        return new CollationKey(source, buffer.rawCollationKey);
    }

    /**
     * Gets the simpler form of a CollationKey for the String source following the rules of this Collator and stores the
     * result into the user provided argument key. If key has a internal byte array of length that's too small for the
     * result, the internal byte array will be grown to the exact required size.
     * 
     * @param source the text String to be transformed into a RawCollationKey
     * @param key output RawCollationKey to store results
     * @return If key is null, a new instance of RawCollationKey will be created and returned, otherwise the user
     *         provided key will be returned.
     * @see #getCollationKey
     * @see #compare(String, String)
     * @see RawCollationKey
     * @stable ICU 2.8
     */
    @Override
    public RawCollationKey getRawCollationKey(String source, RawCollationKey key) {
        if (source == null) {
            return null;
        }
        CollationBuffer buffer = null;
        try {
            buffer = getCollationBuffer();
            return getRawCollationKey(source, key, buffer);
        } finally {
            releaseCollationBuffer(buffer);
        }
    }

    private static final class CollationKeyByteSink extends SortKeyByteSink {
        CollationKeyByteSink(RawCollationKey key) {
            super(key.bytes);
            key_ = key;
        }

        @Override
        protected void AppendBeyondCapacity(byte[] bytes, int start, int n, int length) {
            // n > 0 && appended_ > capacity_
            if (Resize(n, length)) {
                System.arraycopy(bytes, start, buffer_, length, n);
            }
        }

        @Override
        protected boolean Resize(int appendCapacity, int length) {
            int newCapacity = 2 * capacity_;
            int altCapacity = length + 2 * appendCapacity;
            if (newCapacity < altCapacity) {
                newCapacity = altCapacity;
            }
            if (newCapacity < 200) {
                newCapacity = 200;
            }
            assert key_.size == length;
            buffer_ = key_.ensureCapacity(newCapacity).bytes;
            capacity_ = newCapacity;
            return true;
        }

        private RawCollationKey key_;
    }

    private RawCollationKey getRawCollationKey(String source, RawCollationKey key, CollationBuffer buffer) {
        if (key == null) {
            key = new RawCollationKey();
        }
        CollationKeyByteSink sink = new CollationKeyByteSink(key);
        writeSortKey(source, sink, buffer);
        key.size = sink.NumberOfBytesAppended();
        return key;
    }

    private void writeSortKey(CharSequence s, CollationKeyByteSink sink, CollationBuffer buffer) {
        boolean numeric = settings.readOnly().isNumeric();
        if(settings.readOnly().dontCheckFCD()) {
            buffer.leftUTF16CollIter.setText(numeric, s, 0);
            CollationKeys.writeSortKeyUpToQuaternary(
                    buffer.leftUTF16CollIter, data.compressibleBytes, settings.readOnly(),
                    sink, Collation.PRIMARY_LEVEL,
                    CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        } else {
            buffer.leftFCDUTF16Iter.setText(numeric, s, 0);
            CollationKeys.writeSortKeyUpToQuaternary(
                    buffer.leftFCDUTF16Iter, data.compressibleBytes, settings.readOnly(),
                    sink, Collation.PRIMARY_LEVEL,
                    CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        }
        if(settings.readOnly().getStrength() == IDENTICAL) {
            writeIdenticalLevel(s, sink);
        }
        sink.Append(Collation.TERMINATOR_BYTE);
    }

    private void writeIdenticalLevel(CharSequence s, CollationKeyByteSink sink) {
        // NFD quick check
        int nfdQCYesLimit = data.nfcImpl.decompose(s, 0, s.length(), null);
        sink.Append(Collation.LEVEL_SEPARATOR_BYTE);
        int prev = 0;
        if(nfdQCYesLimit != 0) {
            prev = BOCU.writeIdenticalLevelRun(prev, s, 0, nfdQCYesLimit, sink.key_);
        }
        // Is there non-NFD text?
        if(nfdQCYesLimit == s.length()) { return; }
        int destLengthEstimate = s.length() - nfdQCYesLimit;
        StringBuilder nfd = new StringBuilder();
        data.nfcImpl.decompose(s, nfdQCYesLimit, s.length(), nfd, destLengthEstimate);
        BOCU.writeIdenticalLevelRun(prev, nfd, 0, nfd.length(), sink.key_);
    }

    /**
     * Returns this Collator's strength attribute. The strength attribute
     * determines the minimum level of difference considered significant.
     *
     * <p>{@icunote} This can return QUATERNARY strength, which is not supported by the
     * JDK version.
     *
     * <p>See the Collator class description for more details.
     *
     * @return this Collator's current strength attribute.
     * @see #setStrength
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     * @stable ICU 2.8
     */
    @Override
    public int getStrength() {
        return settings.readOnly().getStrength();
    }

    /**
     * Returns the decomposition mode of this Collator. The decomposition mode
     * determines how Unicode composed characters are handled.
     *
     * <p>See the Collator class description for more details.
     *
     * @return the decomposition mode
     * @see #setDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     * @stable ICU 2.8
     */
    @Override
    public int getDecomposition() {
        return (settings.readOnly().options & CollationSettings.CHECK_FCD) != 0 ?
                CANONICAL_DECOMPOSITION : NO_DECOMPOSITION;
    }

    /**
     * Return true if an uppercase character is sorted before the corresponding lowercase character. See
     * setCaseFirst(boolean) for details.
     * 
     * @see #setUpperCaseFirst
     * @see #setLowerCaseFirst
     * @see #isLowerCaseFirst
     * @see #setCaseFirstDefault
     * @return true if upper cased characters are sorted before lower cased characters, false otherwise
     * @stable ICU 2.8
     */
    public boolean isUpperCaseFirst() {
        return (settings.readOnly().getCaseFirst() == CollationSettings.CASE_FIRST_AND_UPPER_MASK);
    }

    /**
     * Return true if a lowercase character is sorted before the corresponding uppercase character. See
     * setCaseFirst(boolean) for details.
     * 
     * @see #setUpperCaseFirst
     * @see #setLowerCaseFirst
     * @see #isUpperCaseFirst
     * @see #setCaseFirstDefault
     * @return true lower cased characters are sorted before upper cased characters, false otherwise
     * @stable ICU 2.8
     */
    public boolean isLowerCaseFirst() {
        return (settings.readOnly().getCaseFirst() == CollationSettings.CASE_FIRST);
    }

    /**
     * Checks if the alternate handling behaviour is the UCA defined SHIFTED or NON_IGNORABLE. If return value is true,
     * then the alternate handling attribute for the Collator is SHIFTED. Otherwise if return value is false, then the
     * alternate handling attribute for the Collator is NON_IGNORABLE See setAlternateHandlingShifted(boolean) for more
     * details.
     * 
     * @return true or false
     * @see #setAlternateHandlingShifted(boolean)
     * @see #setAlternateHandlingDefault
     * @stable ICU 2.8
     */
    public boolean isAlternateHandlingShifted() {
        return settings.readOnly().getAlternateHandling();
    }

    /**
     * Checks if case level is set to true. See setCaseLevel(boolean) for details.
     * 
     * @return the case level mode
     * @see #setCaseLevelDefault
     * @see #isCaseLevel
     * @see #setCaseLevel(boolean)
     * @stable ICU 2.8
     */
    public boolean isCaseLevel() {
        return (settings.readOnly().options & CollationSettings.CASE_LEVEL) != 0;
    }

    /**
     * Checks if French Collation is set to true. See setFrenchCollation(boolean) for details.
     * 
     * @return true if French Collation is set to true, false otherwise
     * @see #setFrenchCollation(boolean)
     * @see #setFrenchCollationDefault
     * @stable ICU 2.8
     */
    public boolean isFrenchCollation() {
        return (settings.readOnly().options & CollationSettings.BACKWARD_SECONDARY) != 0;
    }

    /**
     * Checks if the Hiragana Quaternary mode is set on. See setHiraganaQuaternary(boolean) for more details.
     *
     * This attribute is an implementation detail of the CLDR Japanese tailoring.
     * The implementation might change to use a different mechanism
     * to achieve the same Japanese sort order.
     * Since ICU 50, this attribute is not settable any more via API functions.
     * 
     * @return flag true if Hiragana Quaternary mode is on, false otherwise
     * @see #setHiraganaQuaternaryDefault
     * @see #setHiraganaQuaternary(boolean)
     * @deprecated ICU 50 Implementation detail, cannot be set via API, might be removed from implementation.
     */
    public boolean isHiraganaQuaternary() {
        return false;  // TODO: change docs to say always returns false?
    }

    /**
     * {@icu} Gets the variable top value of a Collator.
     * 
     * @return the variable top primary weight
     * @see #getMaxVariable
     * @stable ICU 2.6
     */
    @Override
    public int getVariableTop() {
        return (int)settings.readOnly().variableTop;
    }

    /**
     * Method to retrieve the numeric collation value. When numeric collation is turned on, this Collator generates a
     * collation key for the numeric value of substrings of digits. This is a way to get '100' to sort AFTER '2'
     * 
     * @see #setNumericCollation
     * @see #setNumericCollationDefault
     * @return true if numeric collation is turned on, false otherwise
     * @stable ICU 2.8
     */
    public boolean getNumericCollation() {
        return (settings.readOnly().options & CollationSettings.NUMERIC) != 0;
    }

    /**  
     * Retrieves the reordering codes for this collator.
     * These reordering codes are a combination of UScript codes and ReorderCodes.
     * @return a copy of the reordering codes for this collator; 
     * if none are set then returns an empty array
     * @see #setReorderCodes
     * @see Collator#getEquivalentReorderCodes
     * @stable ICU 4.8
     */ 
    @Override
    public int[] getReorderCodes() {
        return settings.readOnly().reorderCodes.clone();
    }

    // public other methods -------------------------------------------------

    /**
     * Compares the equality of two RuleBasedCollator objects. RuleBasedCollator objects are equal if they have the same
     * collation rules and the same attributes.
     * 
     * @param obj
     *            the RuleBasedCollator to be compared to.
     * @return true if this RuleBasedCollator has exactly the same collation behaviour as obj, false otherwise.
     * @stable ICU 2.8
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false; // super does class check
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        // TODO RuleBasedCollator other = (RuleBasedCollator) obj;
        // all other non-transient information is also contained in rules.
        /* TODO -- if (getStrength() != other.getStrength() || getDecomposition() != other.getDecomposition()
                || other.m_caseFirst_ != m_caseFirst_ || other.m_caseSwitch_ != m_caseSwitch_
                || other.m_isAlternateHandlingShifted_ != m_isAlternateHandlingShifted_
                || other.m_isCaseLevel_ != m_isCaseLevel_ || other.m_isFrenchCollation_ != m_isFrenchCollation_
                || other.m_isHiragana4_ != m_isHiragana4_) {
            return false;
        } */
        /*if (m_reorderCodes_ != null ^ other.m_reorderCodes_ != null) {
            return false;
        }
        if (m_reorderCodes_ != null) {
            if (m_reorderCodes_.length != other.m_reorderCodes_.length) {
                return false;
            }
            for (int i = 0; i < m_reorderCodes_.length; i++) {
                if (m_reorderCodes_[i] != other.m_reorderCodes_[i]) {
                    return false;
                }
            }
        }*/
        /* TODO boolean rules = m_rules_ == other.m_rules_;
        if (!rules && (m_rules_ != null && other.m_rules_ != null)) {
            rules = m_rules_.equals(other.m_rules_);
        }
        if (!rules || !ICUDebug.enabled("collation")) {
            return rules;
        } */
        /* TODO if (m_addition3_ != other.m_addition3_ || m_bottom3_ != other.m_bottom3_
                || m_bottomCount3_ != other.m_bottomCount3_ || m_common3_ != other.m_common3_
                || m_mask3_ != other.m_mask3_
                || m_minContractionEnd_ != other.m_minContractionEnd_ || m_minUnsafe_ != other.m_minUnsafe_
                || m_top3_ != other.m_top3_ || m_topCount3_ != other.m_topCount3_
                || !Arrays.equals(m_unsafe_, other.m_unsafe_)) {
            return false;
        } */
        /* TODO if (!m_trie_.equals(other.m_trie_)) {
            // we should use the trie iterator here, but then this part is
            // only used in the test.
            for (int i = UCharacter.MAX_VALUE; i >= UCharacter.MIN_VALUE; i--) {
                int v = m_trie_.getCodePointValue(i);
                int otherv = other.m_trie_.getCodePointValue(i);
                if (v != otherv) {
                    int mask = v & (CE_TAG_MASK_ | CE_SPECIAL_FLAG_);
                    if (mask == (otherv & 0xff000000)) {
                        v &= 0xffffff;
                        otherv &= 0xffffff;
                        if (mask == 0xf1000000) {
                            v -= (m_expansionOffset_ << 4);
                            otherv -= (other.m_expansionOffset_ << 4);
                        } else if (mask == 0xf2000000) {
                            v -= m_contractionOffset_;
                            otherv -= other.m_contractionOffset_;
                        }
                        if (v == otherv) {
                            continue;
                        }
                    }
                    return false;
                }
            }
        }
        if (!Arrays.equals(m_contractionCE_, other.m_contractionCE_)
                || !Arrays.equals(m_contractionEnd_, other.m_contractionEnd_)
                || !Arrays.equals(m_contractionIndex_, other.m_contractionIndex_)
                || !Arrays.equals(m_expansion_, other.m_expansion_)
                || !Arrays.equals(m_expansionEndCE_, other.m_expansionEndCE_)) {
            return false;
        }
        // not comparing paddings
        for (int i = 0; i < m_expansionEndCE_.length; i++) {
            if (m_expansionEndCEMaxSize_[i] != other.m_expansionEndCEMaxSize_[i]) {
                return false;
            }
        } */
        return true;
    }

    /**
     * Generates a unique hash code for this RuleBasedCollator.
     * 
     * @return the unique hash code for this Collator
     * @stable ICU 2.8
     */
    @Override
    public int hashCode() {
        int h = settings.hashCode();
        if(data.base == null) { return h; }  // root collator
        // Do not rely on the rule string, see comments in operator==().
        UnicodeSet set = getTailoredSet();
        UnicodeSetIterator iter = new UnicodeSetIterator(set);
        while(iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
            h ^= data.getCE32(iter.codepoint);
        }
        return h;
    }

    /**
     * Compares the source text String to the target text String according to the collation rules, strength and
     * decomposition mode for this RuleBasedCollator. Returns an integer less than, equal to or greater than zero
     * depending on whether the source String is less than, equal to or greater than the target String. See the Collator
     * class description for an example of use. </p>
     * <p>
     * General recommendation: <br>
     * If comparison are to be done to the same String multiple times, it would be more efficient to generate
     * CollationKeys for the Strings and use CollationKey.compareTo(CollationKey) for the comparisons. If speed
     * performance is critical and object instantiation is to be reduced, further optimization may be achieved by
     * generating a simpler key of the form RawCollationKey and reusing this RawCollationKey object with the method
     * RuleBasedCollator.getRawCollationKey. Internal byte representation can be directly accessed via RawCollationKey
     * and stored for future use. Like CollationKey, RawCollationKey provides a method RawCollationKey.compareTo for key
     * comparisons. If the each Strings are compared to only once, using the method RuleBasedCollator.compare(String,
     * String) will have a better performance.
     * </p>
     * 
     * @param source
     *            the source text String.
     * @param target
     *            the target text String.
     * @return Returns an integer value. Value is less than zero if source is less than target, value is zero if source
     *         and target are equal, value is greater than zero if source is greater than target.
     * @see CollationKey
     * @see #getCollationKey
     * @stable ICU 2.8
     */
    @Override
    public int compare(String source, String target) {
        return doCompare(source, target);
    }

    /**
    * Abstract iterator for identical-level string comparisons.
    * Returns FCD code points and handles temporary switching to NFD.
    *
    * <p>As with CollationIterator,
    * Java NFDIterator instances are partially constructed and cached,
    * and completed when reset for use.
    * C++ NFDIterator instances are stack-allocated.
    */
    private static abstract class NFDIterator {
        /**
         * Partial constructor, must call reset().
         */
        NFDIterator() {}
        final void reset() {
            index = -1;
        }

        /**
         * Returns the next code point from the internal normalization buffer,
         * or else the next text code point.
         * Returns -1 at the end of the text.
         */
        final int nextCodePoint() {
            if(index >= 0) {
                if(index == decomp.length()) {
                    index = -1;
                } else {
                    int c = Character.codePointAt(decomp, index);
                    index += Character.charCount(c);
                    return c;
                }
            }
            return nextRawCodePoint();
        }
        /**
         * @param nfcImpl
         * @param c the last code point returned by nextCodePoint() or nextDecomposedCodePoint()
         * @return the first code point in c's decomposition,
         *         or c itself if it was decomposed already or if it does not decompose
         */
        final int nextDecomposedCodePoint(Normalizer2Impl nfcImpl, int c) {
            if(index >= 0) { return c; }
            decomp = nfcImpl.getDecomposition(c);
            if(decomp == null) { return c; }
            c = Character.codePointAt(decomp, 0);
            index = Character.charCount(c);
            return c;
        }

        /**
         * Returns the next text code point in FCD order.
         * Returns -1 at the end of the text.
         */
        protected abstract int nextRawCodePoint();

        private String decomp;
        private int index;
    }

    private static class UTF16NFDIterator extends NFDIterator {
        UTF16NFDIterator() {}
        void setText(CharSequence seq, int start) {
            reset();
            s = seq;
            pos = start;
        }

        @Override
        protected int nextRawCodePoint() {
            if(pos == s.length()) { return Collation.SENTINEL_CP; }
            int c = Character.codePointAt(s, pos);
            pos += Character.charCount(c);
            return c;
        }

        protected CharSequence s;
        protected int pos;
    }

    private static final class FCDUTF16NFDIterator extends UTF16NFDIterator {
        FCDUTF16NFDIterator() {}
        void setText(Normalizer2Impl nfcImpl, CharSequence seq, int start) {
            reset();
            int spanLimit = nfcImpl.makeFCD(seq, start, seq.length(), null);
            if(spanLimit == seq.length()) {
                s = seq;
                pos = start;
            } else {
                if(str == null) {
                    str = new StringBuilder();
                } else {
                    str.setLength(0);
                }
                str.append(seq, start, spanLimit);
                ReorderingBuffer buffer = new ReorderingBuffer(nfcImpl, str, seq.length() - start);
                nfcImpl.makeFCD(seq, spanLimit, seq.length(), buffer);
                s = str;
                pos = 0;
            }
        }

        private StringBuilder str;
    }

    private static final int compareNFDIter(Normalizer2Impl nfcImpl, NFDIterator left, NFDIterator right) {
        for(;;) {
            // Fetch the next FCD code point from each string.
            int leftCp = left.nextCodePoint();
            int rightCp = right.nextCodePoint();
            if(leftCp == rightCp) {
                if(leftCp < 0) { break; }
                continue;
            }
            // If they are different, then decompose each and compare again.
            if(leftCp < 0) {
                leftCp = -2;  // end of string
            } else if(leftCp == 0xfffe) {
                leftCp = -1;  // U+FFFE: merge separator
            } else {
                leftCp = left.nextDecomposedCodePoint(nfcImpl, leftCp);
            }
            if(rightCp < 0) {
                rightCp = -2;  // end of string
            } else if(rightCp == 0xfffe) {
                rightCp = -1;  // U+FFFE: merge separator
            } else {
                rightCp = right.nextDecomposedCodePoint(nfcImpl, rightCp);
            }
            if(leftCp < rightCp) { return UCOL_LESS; }
            if(leftCp > rightCp) { return UCOL_GREATER; }
        }
        return UCOL_EQUAL;
    }

    /**
     * Compares two CharSequences.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    @Override
    protected int doCompare(CharSequence left, CharSequence right) {
        if(left == right) {
            return UCOL_EQUAL;
        }

        // Identical-prefix test.
        int equalPrefixLength = 0;
        for(;;) {
            if(equalPrefixLength == left.length()) {
                if(equalPrefixLength == right.length()) { return UCOL_EQUAL; }
                break;
            } else if(equalPrefixLength == right.length() ||
                      left.charAt(equalPrefixLength) != right.charAt(equalPrefixLength)) {
                break;
            }
            ++equalPrefixLength;
        }

        CollationSettings roSettings = settings.readOnly();
        boolean numeric = roSettings.isNumeric();
        if(equalPrefixLength > 0) {
            if((equalPrefixLength != left.length() &&
                        data.isUnsafeBackward(left.charAt(equalPrefixLength), numeric)) ||
                    (equalPrefixLength != right.length() &&
                        data.isUnsafeBackward(right.charAt(equalPrefixLength), numeric))) {
                // Identical prefix: Back up to the start of a contraction or reordering sequence.
                while(--equalPrefixLength > 0 &&
                        data.isUnsafeBackward(left.charAt(equalPrefixLength), numeric)) {}
            }
            // Notes:
            // - A longer string can compare equal to a prefix of it if only ignorables follow.
            // - With a backward level, a longer string can compare less-than a prefix of it.

            // Pass the actual start of each string into the CollationIterators,
            // plus the equalPrefixLength position,
            // so that prefix matches back into the equal prefix work.
        }

        int result;
        int fastLatinOptions = roSettings.fastLatinOptions;
        if(fastLatinOptions >= 0 &&
                (equalPrefixLength == left.length() ||
                    left.charAt(equalPrefixLength) <= CollationFastLatin.LATIN_MAX) &&
                (equalPrefixLength == right.length() ||
                    right.charAt(equalPrefixLength) <= CollationFastLatin.LATIN_MAX)) {
            result = CollationFastLatin.compareUTF16(data.fastLatinTable,
                                                      roSettings.fastLatinPrimaries,
                                                      fastLatinOptions,
                                                      left, right, equalPrefixLength);
        } else {
            result = CollationFastLatin.BAIL_OUT_RESULT;
        }

        if(result == CollationFastLatin.BAIL_OUT_RESULT) {
            CollationBuffer buffer = null;
            try {
                buffer = getCollationBuffer();
                if(roSettings.dontCheckFCD()) {
                    buffer.leftUTF16CollIter.setText(numeric, left, equalPrefixLength);
                    buffer.rightUTF16CollIter.setText(numeric, right, equalPrefixLength);
                    result = CollationCompare.compareUpToQuaternary(
                            buffer.leftUTF16CollIter, buffer.rightUTF16CollIter, roSettings);
                } else {
                    buffer.leftFCDUTF16Iter.setText(numeric, left, equalPrefixLength);
                    buffer.rightFCDUTF16Iter.setText(numeric, right, equalPrefixLength);
                    result = CollationCompare.compareUpToQuaternary(
                            buffer.leftFCDUTF16Iter, buffer.rightFCDUTF16Iter, roSettings);
                }
            } finally {
                releaseCollationBuffer(buffer);
            }
        }
        if(result != UCOL_EQUAL || roSettings.getStrength() < Collator.IDENTICAL) {
            return result;
        }

        CollationBuffer buffer = null;
        try {
            buffer = getCollationBuffer();
            // Compare identical level.
            Normalizer2Impl nfcImpl = data.nfcImpl;
            if(roSettings.dontCheckFCD()) {
                buffer.leftUTF16NFDIter.setText(left, equalPrefixLength);
                buffer.rightUTF16NFDIter.setText(right, equalPrefixLength);
                return compareNFDIter(nfcImpl, buffer.leftUTF16NFDIter, buffer.rightUTF16NFDIter);
            } else {
                buffer.leftFCDUTF16NFDIter.setText(nfcImpl, left, equalPrefixLength);
                buffer.rightFCDUTF16NFDIter.setText(nfcImpl, right, equalPrefixLength);
                return compareNFDIter(nfcImpl, buffer.leftFCDUTF16NFDIter, buffer.rightFCDUTF16NFDIter);
            }
        } finally {
            releaseCollationBuffer(buffer);
        }
    }

    // package private constructors ------------------------------------------

    /**
     * <p>
     * Private contructor for use by subclasses. Public access to creating Collators is handled by the API
     * Collator.getInstance() or RuleBasedCollator(String rules).
     * </p>
     * <p>
     * This constructor constructs the UCA collator internally
     * </p>
     */
    RuleBasedCollator() {
        // TODO: rewrite the following temporary hack
        tailoring = CollationRoot.getRoot();
        data = tailoring.data;
        settings = tailoring.settings.clone();
    }

    /**
     * Constructs a RuleBasedCollator from the argument locale.
     * If no resource bundle is associated with the locale, UCA is used instead.
     * 
     * @param locale
     */
    RuleBasedCollator(ULocale locale) {
        try {
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_COLLATION_BASE_NAME, locale);
            if (rb != null) {
                ICUResourceBundle elements = null;

                // Use keywords, if supplied for lookup
                String collkey = locale.getKeywordValue("collation");
                if (collkey != null) {
                    try {
                        elements = rb.getWithFallback("collations/" + collkey);
                    } catch (MissingResourceException e) {
                        // fall through
                    }
                }
                if (elements == null) {
                    // either collation keyword was not supplied or
                    // the keyword was invalid - use default collation for the locale

                    // collations/default should always give a string back
                    // keyword for the real collation data
                    collkey = rb.getStringWithFallback("collations/default");
                    elements = rb.getWithFallback("collations/" + collkey);
                }

                // TODO: Determine actual & valid locale correctly
                ULocale uloc = rb.getULocale();
                setLocale(uloc, uloc);

                // TODO: m_rules_ = elements.getString("Sequence");
                ByteBuffer buf = elements.get("%%CollationBin").getBinary();
                // %%CollationBin
                if (buf != null) {
                    // m_rules_ = (String)rules[1][1];
                    CollatorReader.initRBC(this, buf);
                    /*
                     * BufferedInputStream input = new BufferedInputStream( new ByteArrayInputStream(map)); /*
                     * CollatorReader reader = new CollatorReader(input, false); if (map.length >
                     * MIN_BINARY_DATA_SIZE_) { reader.read(this, null); } else { reader.readHeader(this);
                     * reader.readOptions(this); // duplicating UCA_'s data setWithUCATables(); }
                     */
                    // at this point, we have read in the collator
                    // now we need to check whether the binary image has
                    // the right UCA and other versions
                    /* TODO: delete -- if (!m_UCA_version_.equals(UCA_.m_UCA_version_) || !m_UCD_version_.equals(UCA_.m_UCD_version_)) {
                        init(m_rules_);
                        return;
                    } */
                    // TODO: init();
                    try {
                        UResourceBundle reorderRes = elements.get("%%ReorderCodes");
                        if (reorderRes != null) {
                            int[] reorderCodes = reorderRes.getIntVector();
                            setReorderCodes(reorderCodes);
                        }
                    } catch (MissingResourceException e) {
                        // ignore
                    }
                    return;
                } else {
                    // TODO: init("m_rules_");
                    return;
                }
            }
        } catch (Exception e) {
            // fallthrough
        }
        // TODO: setWithUCAData();
    }

    // package private methods -----------------------------------------------

    /**
     * Test whether a char character is potentially "unsafe" for use as a collation starting point. "Unsafe" characters
     * are combining marks or those belonging to some contraction sequence from the offset 1 onwards. E.g. if "ABC" is
     * the only contraction, then 'B' and 'C' are considered unsafe. If we have another contraction "ZA" with the one
     * above, then 'A', 'B', 'C' are "unsafe" but 'Z' is not.
     * 
     * @param ch
     *            character to determin
     * @return true if ch is unsafe, false otherwise
     */
    final boolean isUnsafe(char ch) {
        // TODO: This does not seem to exist in C++. What does C++ use instead? Inspect call sites.
        return data.isUnsafeBackward(ch, settings.readOnly().isNumeric());
    }

    /**
     * Approximate determination if a char character is at a contraction end. Guaranteed to be true if a character is at
     * the end of a contraction, otherwise it is not deterministic.
     * 
     * @param ch
     *            character to be determined
     */
    final boolean isContractionEnd(char ch) {
        // TODO: This does not seem to exist in C++. What does C++ use instead? Inspect call sites.
        return data.isUnsafeBackward(ch, settings.readOnly().isNumeric());
    }

    /**
     * Frozen state of the collator.
     */
    private Lock frozenLock;

    private static final class CollationBuffer {
        private CollationBuffer(CollationData data) {
            leftUTF16CollIter = new UTF16CollationIterator(data);
            rightUTF16CollIter = new UTF16CollationIterator(data);
            leftFCDUTF16Iter = new FCDUTF16CollationIterator(data);
            rightFCDUTF16Iter = new FCDUTF16CollationIterator(data);
            leftUTF16NFDIter = new UTF16NFDIterator();
            rightUTF16NFDIter = new UTF16NFDIterator();
            leftFCDUTF16NFDIter = new FCDUTF16NFDIterator();
            rightFCDUTF16NFDIter = new FCDUTF16NFDIterator();
        }

        UTF16CollationIterator leftUTF16CollIter;
        UTF16CollationIterator rightUTF16CollIter;
        FCDUTF16CollationIterator leftFCDUTF16Iter;
        FCDUTF16CollationIterator rightFCDUTF16Iter;

        UTF16NFDIterator leftUTF16NFDIter;
        UTF16NFDIterator rightUTF16NFDIter;
        FCDUTF16NFDIterator leftFCDUTF16NFDIter;
        FCDUTF16NFDIterator rightFCDUTF16NFDIter;

        RawCollationKey rawCollationKey;
    }

    /**
     * Get the version of this collator object.
     * 
     * @return the version object associated with this collator
     * @stable ICU 2.8
     */
    @Override
    public VersionInfo getVersion() {
        VersionInfo version = tailoring.version;
        int rtVersion = VersionInfo.UCOL_RUNTIME_VERSION.getMajor();
        return VersionInfo.getInstance(
                version.getMajor() + (rtVersion << 4) + (rtVersion >> 4),
                version.getMinor(), version.getMilli(), version.getMicro());
    }

    /**
     * Get the UCA version of this collator object.
     * 
     * @return the version object associated with this collator
     * @stable ICU 2.8
     */
    @Override
    public VersionInfo getUCAVersion() {
        VersionInfo v = getVersion();
        // Note: This is tied to how the current implementation encodes the UCA version
        // in the overall getVersion().
        // Alternatively, we could load the root collator and get at lower-level data from there.
        // Either way, it will reflect the input collator's UCA version only
        // if it is a known implementation.
        // (C++ comment) It would be cleaner to make this a virtual Collator method.
        // (In Java, it is virtual.)
        return VersionInfo.getInstance(v.getMinor() >> 3, v.getMinor() & 7, v.getMilli() >> 6, 0);
    }

    private CollationBuffer collationBuffer;

    private final CollationBuffer getCollationBuffer() {
        if (isFrozen()) {
            frozenLock.lock();
        } else if (collationBuffer == null) {
            collationBuffer = new CollationBuffer(data);
        }
        return collationBuffer;
    }

    private final void releaseCollationBuffer(CollationBuffer buffer) {
        if (isFrozen()) {
            frozenLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ULocale getLocale(ULocale.Type type) {
        if (type == ULocale.ACTUAL_LOCALE) {
            return actualLocaleIsSameAsValid ? validLocale : tailoring.actualLocale;
        } else if(type == ULocale.VALID_LOCALE) {
            return validLocale;
        } else {
            throw new IllegalArgumentException("unknown ULocale.Type " + type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setLocale(ULocale valid, ULocale actual) {
        // This method is called
        // by other protected functions that checks and makes sure that
        // valid and actual are not null before passing
        assert (valid == null) == (actual == null);
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        // TODO: Starting with Java 7, use Objects.equals(a, b).
        if(Utility.objectEquals(actual, tailoring.actualLocale)) {
            actualLocaleIsSameAsValid = false;
        } else if(tailoring.actualLocale == null) {
            tailoring.actualLocale = actual;
            actualLocaleIsSameAsValid = false;
        } else {
            assert(Utility.objectEquals(actual, valid));
            actualLocaleIsSameAsValid = true;
        }
        validLocale = valid;
    }

    CollationData data;
    SharedObject.Reference<CollationSettings> settings;  // reference-counted
    CollationTailoring tailoring;  // C++: reference-counted
    ULocale validLocale;
    // Note: No need in Java to track which attributes have been set explicitly.
    // int or EnumSet  explicitlySetAttributes;

    boolean actualLocaleIsSameAsValid;
}
