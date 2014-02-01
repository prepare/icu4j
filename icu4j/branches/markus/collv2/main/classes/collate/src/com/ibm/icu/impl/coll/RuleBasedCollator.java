/*
******************************************************************************
* Copyright (C) 1996-2014, International Business Machines Corporation and
* others. All Rights Reserved.
******************************************************************************
*/

/**
 * \file 
 * \brief C++ API: The RuleBasedCollator class implements the Collator abstract base class.
 */

/**
* File tblcoll.h
*
* Created by: Helena Shih
*
* Modification History:
*
*  Date        Name        Description
*  2/5/97      aliu        Added streamIn and streamOut methods.  Added
*                          constructor which reads RuleBasedCollator object from
*                          a binary file.  Added writeToFile method which streams
*                          RuleBasedCollator out to a binary file.  The streamIn
*                          and streamOut methods use istream and ostream objects
*                          in binary mode.
*  2/12/97     aliu        Modified to use TableCollationData sub-object to
*                          hold invariant data.
*  2/13/97     aliu        Moved several methods into this class from Collation.
*                          Added a private RuleBasedCollator(Locale&) constructor,
*                          to be used by Collator.createDefault().  General
*                          clean up.
*  2/20/97     helena      Added clone, operator==, operator!=, operator=, and copy
*                          constructor and getDynamicClassID.
*  3/5/97      aliu        Modified constructFromFile() to add parameter
*                          specifying whether or not binary loading is to be
*                          attempted.  This is required for dynamic rule loading.
* 05/07/97     helena      Added memory allocation error detection.
*  6/17/97     helena      Added IDENTICAL strength for compare, changed getRules to
*                          use MergeCollation.getPattern.
*  6/20/97     helena      Java class name change.
*  8/18/97     helena      Added internal API documentation.
* 09/03/97     helena      Added createCollationKeyValues().
* 02/10/98     damiba      Added compare with "length" parameter
* 08/05/98     erm         Synched with 1.2 version of RuleBasedCollator.java
* 04/23/99     stephen     Removed EDecompositionMode, merged with
*                          Normalizer.EMode
* 06/14/99     stephen     Removed kResourceBundleSuffix
* 11/02/99     helena      Collator performance enhancements.  Eliminates the
*                          UnicodeString construction and special case for NO_OP.
* 11/23/99     srl         More performance enhancements. Updates to NormalizerIterator
*                          internal state management.
* 12/15/99     aliu        Update to support Thai collation.  Move NormalizerIterator
*                          to implementation file.
* 01/29/01     synwee      Modified into a C++ wrapper which calls C API
*                          (ucol.h)
* 2012-2013    markus      Rewritten in C++ again.
*/

/**
 * The RuleBasedCollator class provides the implementation of
 * Collator, using data-driven tables. The user can create a customized
 * table-based collation.
 * <p>
 * For more information about the collation service see 
 * <a href="http://userguide.icu-project.org/collation">the User Guide</a>.
 * <p>
 * Collation service provides correct sorting orders for most locales supported in ICU. 
 * If specific data for a locale is not available, the orders eventually falls back
 * to the <a href="http://www.unicode.org/reports/tr35/tr35-collation.html#Root_Collation">CLDR root sort order</a>. 
 * <p>
 * Sort ordering may be customized by providing your own set of rules. For more on
 * this subject see the <a href="http://userguide.icu-project.org/collation/customization">
 * Collation Customization</a> section of the User Guide.
 * <p>
 * Note, RuleBasedCollator is not to be subclassed.
 * @see        Collator
 */
class RuleBasedCollator extends Collator {
public:
    /**
     * RuleBasedCollator constructor. This takes the table rules and builds a
     * collation table out of them. Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @param rules the collation rules to build the collation table from.
     * @param status reporting a success or an error.
     * @see Locale
     * @stable ICU 2.0
     */
    RuleBasedCollator(const UnicodeString& rules, UErrorCode& status);

    /**
     * RuleBasedCollator constructor. This takes the table rules and builds a
     * collation table out of them. Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @param rules the collation rules to build the collation table from.
     * @param collationStrength default strength for comparison
     * @param status reporting a success or an error.
     * @see Locale
     * @stable ICU 2.0
     */
    RuleBasedCollator(const UnicodeString& rules,
                       ECollationStrength collationStrength,
                       UErrorCode& status);

    /**
     * RuleBasedCollator constructor. This takes the table rules and builds a
     * collation table out of them. Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @param rules the collation rules to build the collation table from.
     * @param decompositionMode the normalisation mode
     * @param status reporting a success or an error.
     * @see Locale
     * @stable ICU 2.0
     */
    RuleBasedCollator(const UnicodeString& rules,
                    UColAttributeValue decompositionMode,
                    UErrorCode& status);

    /**
     * RuleBasedCollator constructor. This takes the table rules and builds a
     * collation table out of them. Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @param rules the collation rules to build the collation table from.
     * @param collationStrength default strength for comparison
     * @param decompositionMode the normalisation mode
     * @param status reporting a success or an error.
     * @see Locale
     * @stable ICU 2.0
     */
    RuleBasedCollator(const UnicodeString& rules,
                    ECollationStrength collationStrength,
                    UColAttributeValue decompositionMode,
                    UErrorCode& status);

    /**
     * TODO: document & propose as public API -- not needed in Java
     * @internal
     */
    RuleBasedCollator(const UnicodeString &rules,
                      UParseError &parseError, UnicodeString &reason,
                      );

    /** Opens a collator from a collator binary image created using
    *  cloneBinary. Binary image used in instantiation of the 
    *  collator remains owned by the user and should stay around for 
    *  the lifetime of the collator. The API also takes a base collator
    *  which usually should be the root collator.
    *  @param bin binary image owned by the user and required through the
    *             lifetime of the collator
    *  @param length size of the image. If negative, the API will try to
    *                figure out the length of the image
    *  @param base fallback collator, usually root. The base is required to be
    *              present through the lifetime of the collator. Currently 
    *              it cannot be null.
    *  @param status for catching errors
    *  @return newly created collator
    *  @see cloneBinary
    *  @stable ICU 3.4
    */
    RuleBasedCollator(const uint8_t *bin, int length, 
                    const RuleBasedCollator *base, 
                    UErrorCode &status);

    /**
     * Assignment operator.
     * @param other other RuleBasedCollator object to copy from.
     * @stable ICU 2.0
     */
    RuleBasedCollator& operator=(const RuleBasedCollator& other);

    /**
     * Returns true if argument is the same as this object.
     * @param other Collator object to be compared.
     * @return true if arguments is the same as this object.
     * @stable ICU 2.0
     */
    virtual boolean operator==(const Collator& other);

    /**
     * Makes a copy of this object.
     * @return a copy of this object, owned by the caller
     * @stable ICU 2.0
     */
    @Override
    public RuleBasedCollator clone() {
        RuleBasedCollator c = super.clone();
        c.settings = settings.clone();
        return c;
    }

    /**
     * Creates a collation element iterator for the source string. The caller of
     * this method is responsible for the memory management of the return
     * pointer.
     * @param source the string over which the CollationElementIterator will
     *        iterate.
     * @return the collation element iterator of the source string using this as
     *         the based Collator.
     * @stable ICU 2.2
     */
    virtual CollationElementIterator* createCollationElementIterator(
                                           const UnicodeString& source);

    /**
     * Creates a collation element iterator for the source. The caller of this
     * method is responsible for the memory management of the returned pointer.
     * @param source the CharacterIterator which produces the characters over
     *        which the CollationElementItgerator will iterate.
     * @return the collation element iterator of the source using this as the
     *         based Collator.
     * @stable ICU 2.2
     */
    virtual CollationElementIterator* createCollationElementIterator(
                                         const CharacterIterator& source);

    // Make deprecated versions of Collator.compare() visible.
    using Collator.compare;

    /**
    * The comparison function compares the character data stored in two
    * different strings. Returns information about whether a string is less 
    * than, greater than or equal to another string.
    * @param source the source string to be compared with.
    * @param target the string that is to be compared with the source string.
    * @param status possible error code
    * @return Returns an enum value. UCOL_GREATER if source is greater
    * than target; UCOL_EQUAL if source is equal to target; UCOL_LESS if source is less
    * than target
    * @stable ICU 2.6
    **/
    virtual UCollationResult compare(const UnicodeString& source,
                                     const UnicodeString& target,
                                     UErrorCode &status);

    /**
    * Does the same thing as compare but limits the comparison to a specified 
    * length
    * @param source the source string to be compared with.
    * @param target the string that is to be compared with the source string.
    * @param length the length the comparison is limited to
    * @param status possible error code
    * @return Returns an enum value. UCOL_GREATER if source (up to the specified 
    *         length) is greater than target; UCOL_EQUAL if source (up to specified 
    *         length) is equal to target; UCOL_LESS if source (up to the specified 
    *         length) is less  than target.
    * @stable ICU 2.6
    */
    virtual UCollationResult compare(const UnicodeString& source,
                                     const UnicodeString& target,
                                     int length,
                                     UErrorCode &status);

    /**
    * The comparison function compares the character data stored in two
    * different string arrays. Returns information about whether a string array 
    * is less than, greater than or equal to another string array.
    * @param source the source string array to be compared with.
    * @param sourceLength the length of the source string array.  If this value
    *        is equal to -1, the string array is null-terminated.
    * @param target the string that is to be compared with the source string.
    * @param targetLength the length of the target string array.  If this value
    *        is equal to -1, the string array is null-terminated.
    * @param status possible error code
    * @return Returns an enum value. UCOL_GREATER if source is greater
    * than target; UCOL_EQUAL if source is equal to target; UCOL_LESS if source is less
    * than target
    * @stable ICU 2.6
    */
    virtual UCollationResult compare(const UChar* source, int sourceLength,
                                     const UChar* target, int targetLength,
                                     UErrorCode &status);

    /**
     * Compares two strings using the Collator.
     * Returns whether the first one compares less than/equal to/greater than
     * the second one.
     * This version takes UCharIterator input.
     * @param sIter the first ("source") string iterator
     * @param tIter the second ("target") string iterator
     * @param status ICU status
     * @return UCOL_LESS, UCOL_EQUAL or UCOL_GREATER
     * @stable ICU 4.2
     */
    virtual UCollationResult compare(UCharIterator &sIter,
                                     UCharIterator &tIter,
                                     UErrorCode &status);

    /**
    * Transforms a specified region of the string into a series of characters
    * that can be compared with CollationKey.compare. Use a CollationKey when
    * you need to do repeated comparisions on the same string. For a single
    * comparison the compare method will be faster.
    * @param source the source string.
    * @param key the transformed key of the source string.
    * @param status the error code status.
    * @return the transformed key.
    * @see CollationKey
    * @stable ICU 2.0
    */
    virtual CollationKey& getCollationKey(const UnicodeString& source,
                                          CollationKey& key,
                                          UErrorCode& status);

    /**
    * Transforms a specified region of the string into a series of characters
    * that can be compared with CollationKey.compare. Use a CollationKey when
    * you need to do repeated comparisions on the same string. For a single
    * comparison the compare method will be faster.
    * @param source the source string.
    * @param sourceLength the length of the source string.
    * @param key the transformed key of the source string.
    * @param status the error code status.
    * @return the transformed key.
    * @see CollationKey
    * @stable ICU 2.0
    */
    virtual CollationKey& getCollationKey(const UChar *source,
                                          int sourceLength,
                                          CollationKey& key,
                                          UErrorCode& status);

    /**
     * Generates the hash code for the rule-based collation object.
     * @return the hash code.
     * @stable ICU 2.0
     */
    virtual int hashCode();

    /**
    * Gets the locale of the Collator
    * @param type can be either requested, valid or actual locale. For more
    *             information see the definition of ULocDataLocaleType in
    *             uloc.h
    * @param status the error code status.
    * @return locale where the collation data lives. If the collator
    *         was instantiated from rules, locale is empty.
    * @deprecated ICU 2.8 likely to change in ICU 3.0, based on feedback
    */
    virtual Locale getLocale(ULocDataLocaleType type, UErrorCode& status);

    /**
     * Gets the tailoring rules for this collator.
     * @return the collation tailoring from which this collator was created
     * @stable ICU 2.0
     */
    const UnicodeString& getRules();

    /**
     * Gets the version information for a Collator.
     * @param info the version # information, the result will be filled in
     * @stable ICU 2.0
     */
    virtual void getVersion(VersionInfo info);

#ifndef U_HIDE_DEPRECATED_API 
    /**
     * Returns the maximum length of any expansion sequences that end with the
     * specified comparison order.
     *
     * This is specific to the kind of collation element values and sequences
     * returned by the CollationElementIterator.
     * Call CollationElementIterator.getMaxExpansion() instead.
     *
     * @param order a collation order returned by CollationElementIterator.previous
     *              or CollationElementIterator.next.
     * @return maximum size of the expansion sequences ending with the collation
     *         element, or 1 if the collation element does not occur at the end of
     *         any expansion sequence
     * @see CollationElementIterator#getMaxExpansion
     * @deprecated ICU 51 Use CollationElementIterator.getMaxExpansion() instead.
     */
    int getMaxExpansion(int order);
#endif  /* U_HIDE_DEPRECATED_API */

#ifndef U_HIDE_DEPRECATED_API 
    /**
     * Do not use this method: The caller and the ICU library might use different heaps.
     * Use cloneBinary() instead which writes to caller-provided memory.
     *
     * Returns a binary format of this collator.
     * @param length Returns the length of the data, in bytes
     * @param status the error code status.
     * @return memory, owned by the caller, of size 'length' bytes.
     * @deprecated ICU 52. Use cloneBinary() instead.
     */
    uint8_t *cloneRuleData(int &length, UErrorCode &status);
#endif  /* U_HIDE_DEPRECATED_API */

    /** Creates a binary image of a collator. This binary image can be stored and 
    *  later used to instantiate a collator using ucol_openBinary.
    *  This API supports preflighting.
    *  @param buffer a fill-in buffer to receive the binary image
    *  @param capacity capacity of the destination buffer
    *  @param status for catching errors
    *  @return size of the image
    *  @see ucol_openBinary
    *  @stable ICU 3.4
    */
    int cloneBinary(uint8_t *buffer, int capacity, UErrorCode &status);

    /**
     * Returns current rules. Delta defines whether full rules are returned or
     * just the tailoring.
     *
     * getRules() should normally be used instead.
     * See http://userguide.icu-project.org/collation/customization#TOC-Building-on-Existing-Locales
     * @param delta one of UCOL_TAILORING_ONLY, UCOL_FULL_RULES.
     * @param buffer UnicodeString to store the result rules
     * @stable ICU 2.2
     * @see UCOL_FULL_RULES
     */
    void getRules(UColRuleOption delta, UnicodeString &buffer);

    /**
     * Universal attribute setter
     * @param attr attribute type
     * @param value attribute value
     * @param status to indicate whether the operation went on smoothly or there were errors
     * @stable ICU 2.2
     */
    virtual void setAttribute(UColAttribute attr, UColAttributeValue value,
                              UErrorCode &status);

    /**
     * Universal attribute getter.
     * @param attr attribute type
     * @param status to indicate whether the operation went on smoothly or there were errors
     * @return attribute value
     * @stable ICU 2.2
     */
    virtual UColAttributeValue getAttribute(UColAttribute attr,
                                            UErrorCode &status);

    /**
     * Sets the variable top to the primary weight of the specified string.
     *
     * Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See setMaxVariable().
     * @param varTop one or more (if contraction) UChars to which the variable top should be set
     * @param len length of variable top string. If -1 it is considered to be zero terminated.
     * @param status error code. If error code is set, the return value is undefined. Errors set by this function are: <br>
     *    U_CE_NOT_FOUND_ERROR if more than one character was passed and there is no such contraction<br>
     *    U_ILLEGAL_ARGUMENT_ERROR if the variable top is beyond
     *    the last reordering group supported by setMaxVariable()
     * @return variable top primary weight
     * @deprecated ICU 53 Call setMaxVariable() instead.
     */
    virtual uint32_t setVariableTop(const UChar *varTop, int len, UErrorCode &status);

    /**
     * Sets the variable top to the primary weight of the specified string.
     *
     * Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See setMaxVariable().
     * @param varTop a UnicodeString size 1 or more (if contraction) of UChars to which the variable top should be set
     * @param status error code. If error code is set, the return value is undefined. Errors set by this function are: <br>
     *    U_CE_NOT_FOUND_ERROR if more than one character was passed and there is no such contraction<br>
     *    U_ILLEGAL_ARGUMENT_ERROR if the variable top is beyond
     *    the last reordering group supported by setMaxVariable()
     * @return variable top primary weight
     * @deprecated ICU 53 Call setMaxVariable() instead.
     */
    virtual uint32_t setVariableTop(const UnicodeString &varTop, UErrorCode &status);

    /**
     * Sets the variable top to the specified primary weight.
     *
     * Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See setMaxVariable().
     * @param varTop primary weight, as returned by setVariableTop or ucol_getVariableTop
     * @param status error code
     * @deprecated ICU 53 Call setMaxVariable() instead.
     */
    virtual void setVariableTop(uint32_t varTop, UErrorCode &status);

    /**
     * Gets the variable top value of a Collator.
     * @param status error code (not changed by function). If error code is set, the return value is undefined.
     * @return the variable top primary weight
     * @see getMaxVariable
     * @stable ICU 2.0
     */
    virtual uint32_t getVariableTop(UErrorCode &status);

    /**
     * Get a UnicodeSet that contains all the characters and sequences tailored in 
     * this collator.
     * @param status      error code of the operation
     * @return a pointer to a UnicodeSet object containing all the 
     *         code points and sequences that may sort differently than
     *         in the root collator. The object must be disposed of by using delete
     * @stable ICU 2.4
     */
    virtual UnicodeSet *getTailoredSet(UErrorCode &status);

    /**
     * Get the sort key as an array of bytes from a UnicodeString.
     * @param source string to be processed.
     * @param result buffer to store result in. If null, number of bytes needed
     *        will be returned.
     * @param resultLength length of the result buffer. If if not enough the
     *        buffer will be filled to capacity.
     * @return Number of bytes needed for storing the sort key
     * @stable ICU 2.0
     */
    virtual int getSortKey(const UnicodeString& source, uint8_t *result,
                               int resultLength);

    /**
     * Get the sort key as an array of bytes from a UChar buffer.
     * @param source string to be processed.
     * @param sourceLength length of string to be processed. If -1, the string
     *        is 0 terminated and length will be decided by the function.
     * @param result buffer to store result in. If null, number of bytes needed
     *        will be returned.
     * @param resultLength length of the result buffer. If if not enough the
     *        buffer will be filled to capacity.
     * @return Number of bytes needed for storing the sort key
     * @stable ICU 2.2
     */
    virtual int getSortKey(const UChar *source, int sourceLength,
                               uint8_t *result, int resultLength);

    /**
     * Retrieves the reordering codes for this collator.
     * @param dest The array to fill with the script ordering.
     * @param destCapacity The length of dest. If it is 0, then dest may be null and the function
     *  will only return the length of the result without writing any of the result string (pre-flighting).
     * @param status A reference to an error code value, which must not indicate
     * a failure before the function call.
     * @return The length of the script ordering array.
     * @see ucol_setReorderCodes
     * @see Collator#getEquivalentReorderCodes
     * @see Collator#setReorderCodes
     * @stable ICU 4.8 
     */
     virtual int getReorderCodes(int *dest,
                                     int destCapacity,
                                     UErrorCode& status);

    /**
     * Sets the ordering of scripts for this collator.
     * @param reorderCodes An array of script codes in the new order. This can be null if the 
     * length is also set to 0. An empty array will clear any reordering codes on the collator.
     * @param reorderCodesLength The length of reorderCodes.
     * @param status error code
     * @see Collator#getReorderCodes
     * @see Collator#getEquivalentReorderCodes
     * @stable ICU 4.8 
     */
     virtual void setReorderCodes(const int* reorderCodes,
                                  int reorderCodesLength,
                                  UErrorCode& status) ;

#ifndef U_HIDE_INTERNAL_API
    /**
     * Only for use in ucol_openRules().
     * @internal
     */
    RuleBasedCollator();

    /**
     * Implements ucol_getLocaleByType().
     * Needed because the lifetime of the locale ID string must match that of the collator.
     * getLocale() returns a copy of a Locale, with minimal lifetime in a C wrapper.
     * @internal
     */
    const char *internalGetLocaleID(ULocDataLocaleType type);

    /**
     * Implements ucol_getContractionsAndExpansions().
     * Gets this collator's sets of contraction strings and/or
     * characters and strings that map to multiple collation elements (expansions).
     * If addPrefixes is true, then contractions that are expressed as
     * prefix/pre-context rules are included.
     * @param contractions if not null, the set to hold the contractions
     * @param expansions if not null, the set to hold the expansions
     * @param addPrefixes include prefix contextual mappings
     * @param errorCode in/out ICU error code
     * @internal
     */
    void internalGetContractionsAndExpansions(
            UnicodeSet *contractions, UnicodeSet *expansions,
            boolean addPrefixes);

    /**
     * Adds the contractions that start with character c to the set.
     * Ignores prefixes. Used by AlphabeticIndex.
     * @internal
     */
    void internalAddContractions(int c, UnicodeSet &set);

    /**
     * Implements from-rule constructors, and ucol_openRules().
     * @internal
     */
    void internalBuildTailoring(
            const UnicodeString &rules,
            int strength,
            UColAttributeValue decompositionMode,
            UParseError *outParseError, UnicodeString *outReason,
            );

    /** @internal */
    static RuleBasedCollator *rbcFromUCollator(UCollator *uc) {
        return dynamic_cast<RuleBasedCollator *>(fromUCollator(uc));
    }
    /** @internal */
    static const RuleBasedCollator *rbcFromUCollator(const UCollator *uc) {
        return dynamic_cast<const RuleBasedCollator *>(fromUCollator(uc));
    }

    /**
     * Appends the CEs for the string to the vector.
     * @internal for tests & tools
     */
    void internalGetCEs(const UnicodeString &str, UVector64 &ces);
#endif  // U_HIDE_INTERNAL_API

protected:
   /**
    * Used internally by registration to define the requested and valid locales.
    * @param requestedLocale the requested locale
    * @param validLocale the valid locale
    * @param actualLocale the actual locale
    * @internal
    */
    virtual void setLocales(const Locale& requestedLocale, const Locale& validLocale, const Locale& actualLocale);

private:
    friend class CollationElementIterator;
    friend class Collator;

    RuleBasedCollator(const CollationTailoring *t);

    /**
     * Enumeration of attributes that are relevant for short definition strings
     * (e.g., ucol_getShortDefinitionString()).
     * Effectively extends UColAttribute.
     */
    enum Attributes {
        ATTR_VARIABLE_TOP = UCOL_ATTRIBUTE_COUNT,
        ATTR_LIMIT
    };

    void adoptTailoring(CollationTailoring *t);

    // Both lengths must be <0 or else both must be >=0.
    UCollationResult doCompare(const UChar *left, int leftLength,
                               const UChar *right, int rightLength,
                               );

    void writeSortKey(const UChar *s, int length,
                      SortKeyByteSink &sink);

    void writeIdenticalLevel(const UChar *s, const UChar *limit,
                             SortKeyByteSink &sink);

    CollationSettings getDefaultSettings();

    // TODO: Use an EnumSet?
    void setAttributeDefault(int attribute) {
        explicitlySetAttributes &= ~(1 << attribute);
    }
    void setAttributeExplicitly(int attribute) {
        explicitlySetAttributes |= 1 << attribute;
    }
    boolean attributeHasBeenSetExplicitly(int attribute) {
        assert(0 <= attribute < ATTR_LIMIT);
        return (boolean)((explicitlySetAttributes & (1 << attribute)) != 0);
    }

    static void computeMaxExpansions(const CollationTailoring *t);
    boolean initMaxExpansions();

    void setFastLatinOptions(CollationSettings &ownedSettings);

    CollationData data;
    SharedObject.Reference<CollationSettings> settings;  // reference-counted
    CollationTailoring tailoring;  // C++: reference-counted
    ULocale validLocale;
    uint32_t explicitlySetAttributes;

    boolean actualLocaleIsSameAsValid;
}

    RuleBasedCollator.RuleBasedCollator(const RuleBasedCollator &other)
            : Collator(other),
              data(other.data),
              settings(other.settings),
              tailoring(other.tailoring),
              validLocale(other.validLocale),
              explicitlySetAttributes(other.explicitlySetAttributes),
              actualLocaleIsSameAsValid(other.actualLocaleIsSameAsValid) {
        settings.addRef();
        tailoring.addRef();
    }

    RuleBasedCollator.RuleBasedCollator(const uint8_t *bin, int length,
                                        const RuleBasedCollator *base)
            : data(null),
              settings(null),
              tailoring(null),
              validLocale(""),
              explicitlySetAttributes(0),
              actualLocaleIsSameAsValid(false) {
        if(U_FAILURE) { return; }
        if(bin == null || length <= 0 || base == null) {
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            return;
        }
        const CollationTailoring *root = CollationRoot.getRoot;
        if(U_FAILURE) { return; }
        if(base.tailoring != root) {
            errorCode = U_UNSUPPORTED_ERROR;
            return;
        }
        LocalPointer<CollationTailoring> t(new CollationTailoring(base.tailoring.settings));
        if(t.isNull() || t.isBogus()) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        CollationDataReader.read(base.tailoring, bin, length, *t);
        if(U_FAILURE) { return; }
        t.actualLocale.setToBogus();
        adoptTailoring(t.orphan());
    }

    RuleBasedCollator.RuleBasedCollator(const CollationTailoring *t)
            : data(t.data),
              settings(t.settings),
              tailoring(t),
              validLocale(t.actualLocale),
              explicitlySetAttributes(0),
              actualLocaleIsSameAsValid(false) {
        settings.addRef();
        tailoring.addRef();
    }

    void
    RuleBasedCollator.adoptTailoring(CollationTailoring *t) {
        assert(settings == null && data == null && tailoring == null);
        data = t.data;
        settings = t.settings;
        settings.addRef();
        t.addRef();
        tailoring = t;
        validLocale = t.actualLocale;
        actualLocaleIsSameAsValid = false;
    }

    RuleBasedCollator &RuleBasedCollator.operator=(const RuleBasedCollator &other) {
        if(this == &other) { return *this; }
        SharedObject.copyPtr(other.settings, settings);
        SharedObject.copyPtr(other.tailoring, tailoring);
        data = tailoring.data;
        validLocale = other.validLocale;
        explicitlySetAttributes = other.explicitlySetAttributes;
        actualLocaleIsSameAsValid = other.actualLocaleIsSameAsValid;
        return *this;
    }

    UOBJECT_DEFINE_RTTI_IMPLEMENTATION(RuleBasedCollator)

    boolean
    RuleBasedCollator.operator==(const Collator& other) {
        if(this == &other) { return true; }
        if(!Collator.operator==(other)) { return false; }
        const RuleBasedCollator &o = static_cast<const RuleBasedCollator &>(other);
        if(*settings != *o.settings) { return false; }
        if(data == o.data) { return true; }
        boolean thisIsRoot = data.base == null;
        boolean otherIsRoot = o.data.base == null;
        assert(!thisIsRoot || !otherIsRoot);  // otherwise their data pointers should be ==
        if(thisIsRoot != otherIsRoot) { return false; }
        if((thisIsRoot || !tailoring.rules.isEmpty()) &&
                (otherIsRoot || !o.tailoring.rules.isEmpty())) {
            // Shortcut: If both collators have valid rule strings, then compare those.
            if(tailoring.rules == o.tailoring.rules) { return true; }
        }
        // Different rule strings can result in the same or equivalent tailoring.
        // The rule strings are optional in ICU resource bundles, although included by default.
        // cloneBinary() drops the rule string.
        UErrorCode errorCode = U_ZERO_ERROR;
        LocalPointer<UnicodeSet> thisTailored(getTailoredSet);
        LocalPointer<UnicodeSet> otherTailored(o.getTailoredSet);
        if(U_FAILURE) { return false; }
        if(*thisTailored != *otherTailored) { return false; }
        // For completeness, we should compare all of the mappings;
        // or we should create a list of strings, sort it with one collator,
        // and check if both collators compare adjacent strings the same
        // (order & strength, down to quaternary); or similar.
        // Testing equality of collators seems unusual.
        return true;
    }

    int32_t
    RuleBasedCollator.hashCode() {
        int h = settings.hashCode();
        if(data.base == null) { return h; }  // root collator
        // Do not rely on the rule string, see comments in operator==().
        UErrorCode errorCode = U_ZERO_ERROR;
        LocalPointer<UnicodeSet> set(getTailoredSet);
        if(U_FAILURE) { return 0; }
        UnicodeSetIterator iter(*set);
        while(iter.next() && !iter.isString()) {
            h ^= data.getCE32(iter.getCodepoint());
        }
        return h;
    }

    void
    RuleBasedCollator.setLocales(ULocale requested, ULocale valid,
                                  ULocale actual) {
        if(actual == tailoring.actualLocale) {
            actualLocaleIsSameAsValid = false;
        } else if(tailoring.actualLocale.isBogus()) {
            tailoring.actualLocale = actual;
            actualLocaleIsSameAsValid = false;
        } else {
            assert(actual == valid);
            actualLocaleIsSameAsValid = true;
        }
        validLocale = valid;
        // Ignore requested, see also ticket #10477.
    }

    Locale
    RuleBasedCollator.getLocale(ULocDataLocaleType type, UErrorCode& errorCode) {
        if(U_FAILURE) {
            return Locale.getRoot();
        }
        switch(type) {
        case ULOC_ACTUAL_LOCALE:
            return actualLocaleIsSameAsValid ? validLocale : tailoring.actualLocale;
        case ULOC_VALID_LOCALE:
        case ULOC_REQUESTED_LOCALE:  // TODO: Drop this, see ticket #10477.
            return validLocale;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            return Locale.getRoot();
        }
    }

    const char *
    RuleBasedCollator.internalGetLocaleID(ULocDataLocaleType type) {
        if(U_FAILURE) {
            return null;
        }
        const Locale *result;
        switch(type) {
        case ULOC_ACTUAL_LOCALE:
            result = actualLocaleIsSameAsValid ? &validLocale : &tailoring.actualLocale;
            break;
        case ULOC_VALID_LOCALE:
        case ULOC_REQUESTED_LOCALE:  // TODO: Drop this, see ticket #10477.
            result = &validLocale;
            break;
        default:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            return null;
        }
        if(result.isBogus()) { return null; }
        const char *id = result.getName();
        return id[0] == 0 ? "root" : id;
    }

    const UnicodeString&
    RuleBasedCollator.getRules() {
        return tailoring.rules;
    }

    void
    RuleBasedCollator.getRules(UColRuleOption delta, UnicodeString &buffer) {
        if(delta == UCOL_TAILORING_ONLY) {
            buffer = tailoring.rules;
            return;
        }
        // UCOL_FULL_RULES
        buffer.remove();
        CollationLoader.appendRootRules(buffer);
        buffer.append(tailoring.rules).getTerminatedBuffer();
    }

    void
    RuleBasedCollator.getVersion(VersionInfo version) {
        uprv_memcpy(version, tailoring.version, U_MAX_VERSION_LENGTH);
        version[0] += (VersionInfo.UCOL_RUNTIME_VERSION << 4) + (VersionInfo.UCOL_RUNTIME_VERSION >> 4);
    }

    UnicodeSet *
    RuleBasedCollator.getTailoredSet() {
        UnicodeSet *tailored = new UnicodeSet();
        if(tailored == null) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return null;
        }
        if(data.base != null) {
            TailoredSet(tailored).forData(data);
            if(U_FAILURE) {
                delete tailored;
                return null;
            }
        }
        return tailored;
    }

    void
    RuleBasedCollator.internalGetContractionsAndExpansions(
            UnicodeSet *contractions, UnicodeSet *expansions,
            boolean addPrefixes) {
        if(U_FAILURE) { return; }
        if(contractions != null) {
            contractions.clear();
        }
        if(expansions != null) {
            expansions.clear();
        }
        ContractionsAndExpansions(contractions, expansions, null, addPrefixes).forData(data);
    }

    void
    RuleBasedCollator.internalAddContractions(int c, UnicodeSet &set) {
        ContractionsAndExpansions(&set, null, null, false).forCodePoint(data, c);
    }

    void
    RuleBasedCollator.internalGetCEs(const UnicodeString &str, UVector64 &ces,
                                      ) {
        if(U_FAILURE) { return; }
        const UChar *s = str.getBuffer();
        const UChar *limit = s + str.length();
        boolean numeric = settings.isNumeric();
        if(settings.dontCheckFCD()) {
            UTF16CollationIterator iter(data, numeric, s, s, limit);
            long ce;
            while((ce = iter.nextCE) != Collation.NO_CE) {
                ces.addElement(ce);
            }
        } else {
            FCDUTF16CollationIterator iter(data, numeric, s, s, limit);
            long ce;
            while((ce = iter.nextCE) != Collation.NO_CE) {
                ces.addElement(ce);
            }
        }
    }

    void
    RuleBasedCollator.computeMaxExpansions(const CollationTailoring *t) {
        t.maxExpansions = CollationElementIterator.computeMaxExpansions(t.data);
    }

    boolean
    RuleBasedCollator.initMaxExpansions() {
        umtx_initOnce(tailoring.maxExpansionsInitOnce, computeMaxExpansions, tailoring);
        return U_SUCCESS;
    }

    CollationElementIterator *
    RuleBasedCollator.createCollationElementIterator(const UnicodeString& source) {
        UErrorCode errorCode = U_ZERO_ERROR;
        if(!initMaxExpansions()) { return null; }
        CollationElementIterator *cei = new CollationElementIterator(source, this);
        if(U_FAILURE) {
            delete cei;
            return null;
        }
        return cei;
    }

    CollationElementIterator *
    RuleBasedCollator.createCollationElementIterator(const CharacterIterator& source) {
        UErrorCode errorCode = U_ZERO_ERROR;
        if(!initMaxExpansions()) { return null; }
        CollationElementIterator *cei = new CollationElementIterator(source, this);
        if(U_FAILURE) {
            delete cei;
            return null;
        }
        return cei;
    }

    int32_t
    RuleBasedCollator.getMaxExpansion(int order) {
        return CollationElementIterator.getMaxExpansion(tailoring.maxExpansions, order);
    }
