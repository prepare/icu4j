/*
******************************************************************************
*   Copyright (C) 1996-2013, International Business Machines
*   Corporation and others.  All Rights Reserved.
******************************************************************************
*/

/**
 * \file 
 * \brief C++ API: Collation Service.
 */
 
/**
* File coll.h
*
* Created by: Helena Shih
*
* Modification History:
*
*  Date        Name        Description
* 02/5/97      aliu        Modified createDefault to load collation data from
*                          binary files when possible.  Added related methods
*                          createCollationFromFile, chopLocale, createPathName.
* 02/11/97     aliu        Added members addToCache, findInCache, and fgCache.
* 02/12/97     aliu        Modified to create objects from RuleBasedCollator cache.
*                          Moved cache out of Collation class.
* 02/13/97     aliu        Moved several methods out of this class and into
*                          RuleBasedCollator, with modifications.  Modified
*                          createDefault() to call new RuleBasedCollator(Locale&)
*                          constructor.  General clean up and documentation.
* 02/20/97     helena      Added clone, operator==, operator!=, operator=, copy
*                          constructor and getDynamicClassID.
* 03/25/97     helena      Updated with platform independent data types.
* 05/06/97     helena      Added memory allocation error detection.
* 06/20/97     helena      Java class name change.
* 09/03/97     helena      Added createCollationKeyValues().
* 02/10/98     damiba      Added compare() with length as parameter.
* 04/23/99     stephen     Removed EDecompositionMode, merged with
*                          Normalizer.EMode.
* 11/02/99     helena      Collator performance enhancements.  Eliminates the
*                          UnicodeString construction and special case for NO_OP.
* 11/23/99     srl         More performance enhancements. Inlining of
*                          critical accessors.
* 05/15/00     helena      Added version information API.
* 01/29/01     synwee      Modified into a C++ wrapper which calls C apis
*                          (ucol.h).
* 2012-2013    markus      Rewritten in C++ again.
*/

#ifndef COLL_H
#define COLL_H

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/uobject.h"
#include "unicode/ucol.h"
#include "unicode/normlzr.h"
#include "unicode/locid.h"
#include "unicode/uniset.h"
#include "unicode/umisc.h"
#include "unicode/uiter.h"
#include "unicode/stringpiece.h"

U_NAMESPACE_BEGIN

class StringEnumeration;

#if !UCONFIG_NO_SERVICE
/**
 * @stable ICU 2.6
 */
class CollatorFactory;
#endif

/**
* @stable ICU 2.0
*/
class CollationKey;

/**
* The <code>Collator</code> class performs locale-sensitive string
* comparison.<br>
* You use this class to build searching and sorting routines for natural
* language text.
* <p>
* <code>Collator</code> is an abstract base class. Subclasses implement
* specific collation strategies. One subclass,
* <code>RuleBasedCollator</code>, is currently provided and is applicable
* to a wide set of languages. Other subclasses may be created to handle more
* specialized needs.
* <p>
* Like other locale-sensitive classes, you can use the static factory method,
* <code>createInstance</code>, to obtain the appropriate
* <code>Collator</code> object for a given locale. You will only need to
* look at the subclasses of <code>Collator</code> if you need to
* understand the details of a particular collation strategy or if you need to
* modify that strategy.
* <p>
* The following example shows how to compare two strings using the
* <code>Collator</code> for the default locale.
* \htmlonly<blockquote>\endhtmlonly
* <pre>
* \code
* // Compare two strings in the default locale
* UErrorCode success = U_ZERO_ERROR;
* Collator* myCollator = Collator.createInstance(success);
* if (myCollator.compare("abc", "ABC") < 0)
*   cout << "abc is less than ABC" << endl;
* else
*   cout << "abc is greater than or equal to ABC" << endl;
* \endcode
* </pre>
* \htmlonly</blockquote>\endhtmlonly
* <p>
* You can set a <code>Collator</code>'s <em>strength</em> property to
* determine the level of difference considered significant in comparisons.
* Five strengths are provided: <code>PRIMARY</code>, <code>SECONDARY</code>,
* <code>TERTIARY</code>, <code>QUATERNARY</code> and <code>IDENTICAL</code>.
* The exact assignment of strengths to language features is locale dependent.
* For example, in Czech, "e" and "f" are considered primary differences,
* while "e" and "\u00EA" are secondary differences, "e" and "E" are tertiary
* differences and "e" and "e" are identical. The following shows how both case
* and accents could be ignored for US English.
* \htmlonly<blockquote>\endhtmlonly
* <pre>
* \code
* //Get the Collator for US English and set its strength to PRIMARY
* UErrorCode success = U_ZERO_ERROR;
* Collator* usCollator = Collator.createInstance(Locale.getUS(), success);
* usCollator.setStrength(Collator.PRIMARY);
* if (usCollator.compare("abc", "ABC") == 0)
*     cout << "'abc' and 'ABC' strings are equivalent with strength PRIMARY" << endl;
* \endcode
* </pre>
* \htmlonly</blockquote>\endhtmlonly
* <p>
* For comparing strings exactly once, the <code>compare</code> method
* provides the best performance. When sorting a list of strings however, it
* is generally necessary to compare each string multiple times. In this case,
* sort keys provide better performance. The <code>getSortKey</code> methods
* convert a string to a series of bytes that can be compared bitwise against
* other sort keys using <code>strcmp()</code>. Sort keys are written as
* zero-terminated byte strings. They consist of several substrings, one for
* each collation strength level, that are delimited by 0x01 bytes.
* If the string code points are appended for UCOL_IDENTICAL, then they are
* processed for correct code point order comparison and may contain 0x01
* bytes but not zero bytes.
* </p>
* <p>
* Another set of APIs returns a <code>CollationKey</code> object that wraps
* the sort key bytes instead of returning the bytes themselves.
* </p>
* <p>
* <strong>Note:</strong> <code>Collator</code>s with different Locale,
* and CollationStrength settings will return different sort
* orders for the same set of strings. Locales have specific collation rules,
* and the way in which secondary and tertiary differences are taken into
* account, for example, will result in a different sorting order for same
* strings.
* </p>
* @see         RuleBasedCollator
* @see         CollationKey
* @see         CollationElementIterator
* @see         Locale
* @see         Normalizer
* @version     2.0 11/15/01
*/

class Collator {
public:

    // Collator public enums -----------------------------------------------

    /**
     * Base letter represents a primary difference. Set comparison level to
     * PRIMARY to ignore secondary and tertiary differences.<br>
     * Use this to set the strength of a Collator object.<br>
     * Example of primary difference, "abc" &lt; "abd"
     *
     * Diacritical differences on the same base letter represent a secondary
     * difference. Set comparison level to SECONDARY to ignore tertiary
     * differences. Use this to set the strength of a Collator object.<br>
     * Example of secondary difference, "&auml;" >> "a".
     *
     * Uppercase and lowercase versions of the same character represents a
     * tertiary difference.  Set comparison level to TERTIARY to include all
     * comparison differences. Use this to set the strength of a Collator
     * object.<br>
     * Example of tertiary difference, "abc" &lt;&lt;&lt; "ABC".
     *
     * Two characters are considered "identical" when they have the same unicode
     * spellings.<br>
     * For example, "&auml;" == "&auml;".
     *
     * UCollationStrength is also used to determine the strength of sort keys
     * generated from Collator objects.
     * @stable ICU 2.0
     */
    enum ECollationStrength
    {
        PRIMARY    = UCOL_PRIMARY,  // 0
        SECONDARY  = UCOL_SECONDARY,  // 1
        TERTIARY   = UCOL_TERTIARY,  // 2
        QUATERNARY = UCOL_QUATERNARY,  // 3
        IDENTICAL  = UCOL_IDENTICAL  // 15
    };

    /**
     * LESS is returned if source string is compared to be less than target
     * string in the compare() method.
     * EQUAL is returned if source string is compared to be equal to target
     * string in the compare() method.
     * GREATER is returned if source string is compared to be greater than
     * target string in the compare() method.
     * @see Collator#compare
     * @deprecated ICU 2.6. Use C enum UCollationResult defined in ucol.h
     */
    enum EComparisonResult
    {
        LESS = UCOL_LESS,  // -1
        EQUAL = UCOL_EQUAL,  // 0
        GREATER = UCOL_GREATER  // 1
    };

    // Collator public destructor -----------------------------------------

    /**
     * Destructor
     * @stable ICU 2.0
     */
    virtual ~Collator();

    // Collator public methods --------------------------------------------

    /**
     * Returns true if "other" is the same as "this".
     *
     * The base class implementation returns true if "other" has the same type/class as "this":
     * <code>typeid(*this) == typeid(other)</code>.
     *
     * Subclass implementations should do something like the following:
     * <pre>
     *   if (this == &other) { return true; }
     *   if (!Collator.operator==(other)) { return false; }  // not the same class
     *
     *   const MyCollator &o = (const MyCollator&)other;
     *   (compare this vs. o's subclass fields)
     * </pre>
     * @param other Collator object to be compared
     * @return true if other is the same as this.
     * @stable ICU 2.0
     */
    virtual boolean operator==(const Collator& other);

    /**
     * Returns true if "other" is not the same as "this".
     * Calls ! operator==(const Collator&) which works for all subclasses.
     * @param other Collator object to be compared
     * @return true if other is not the same as this.
     * @stable ICU 2.0
     */
    virtual boolean operator!=(const Collator& other);

    /**
     * Makes a copy of this object.
     * @return a copy of this object, owned by the caller
     * @stable ICU 2.0
     */
    virtual Collator* clone() = 0;

    /**
     * Creates the Collator object for the current default locale.
     * The default locale is determined by Locale.getDefault.
     * The UErrorCode& err parameter is used to return status information to the user.
     * To check whether the construction succeeded or not, you should check the
     * value of U_SUCCESS(err).  If you wish more detailed information, you can
     * check for informational error results which still indicate success.
     * U_USING_FALLBACK_ERROR indicates that a fall back locale was used. For
     * example, 'de_CH' was requested, but nothing was found there, so 'de' was
     * used. U_USING_DEFAULT_ERROR indicates that the default locale data was
     * used; neither the requested locale nor any of its fall back locales
     * could be found.
     * The caller owns the returned object and is responsible for deleting it.
     *
     * @param err    the error code status.
     * @return       the collation object of the default locale.(for example, en_US)
     * @see Locale#getDefault
     * @stable ICU 2.0
     */
    static Collator* U_EXPORT2 createInstance(UErrorCode&  err);

    /**
     * Gets the table-based collation object for the desired locale. The
     * resource of the desired locale will be loaded.
     * Locale.getRoot() is the base collation table and all other languages are
     * built on top of it with additional language-specific modifications.
     * The UErrorCode& err parameter is used to return status information to the user.
     * To check whether the construction succeeded or not, you should check
     * the value of U_SUCCESS(err).  If you wish more detailed information, you
     * can check for informational error results which still indicate success.
     * U_USING_FALLBACK_ERROR indicates that a fall back locale was used.  For
     * example, 'de_CH' was requested, but nothing was found there, so 'de' was
     * used.  U_USING_DEFAULT_ERROR indicates that the default locale data was
     * used; neither the requested locale nor any of its fall back locales
     * could be found.
     * The caller owns the returned object and is responsible for deleting it.
     * @param loc    The locale ID for which to open a collator.
     * @param err    the error code status.
     * @return       the created table-based collation object based on the desired
     *               locale.
     * @see Locale
     * @see ResourceLoader
     * @stable ICU 2.2
     */
    static Collator* U_EXPORT2 createInstance(const Locale& loc, UErrorCode& err);

    /**
     * The comparison function compares the character data stored in two
     * different strings. Returns information about whether a string is less
     * than, greater than or equal to another string.
     * @param source the source string to be compared with.
     * @param target the string that is to be compared with the source string.
     * @return Returns a byte value. GREATER if source is greater
     * than target; EQUAL if source is equal to target; LESS if source is less
     * than target
     * @deprecated ICU 2.6 use the overload with UErrorCode &
     */
    virtual EComparisonResult compare(const UnicodeString& source,
                                      const UnicodeString& target);

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
     */
    virtual UCollationResult compare(const UnicodeString& source,
                                      const UnicodeString& target,
                                      UErrorCode &status) = 0;

    /**
     * Does the same thing as compare but limits the comparison to a specified
     * length
     * @param source the source string to be compared with.
     * @param target the string that is to be compared with the source string.
     * @param length the length the comparison is limited to
     * @return Returns a byte value. GREATER if source (up to the specified
     *         length) is greater than target; EQUAL if source (up to specified
     *         length) is equal to target; LESS if source (up to the specified
     *         length) is less  than target.
     * @deprecated ICU 2.6 use the overload with UErrorCode &
     */
    virtual EComparisonResult compare(const UnicodeString& source,
                                      const UnicodeString& target,
                                      int length);

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
                                      UErrorCode &status) = 0;

    /**
     * The comparison function compares the character data stored in two
     * different string arrays. Returns information about whether a string array
     * is less than, greater than or equal to another string array.
     * <p>Example of use:
     * <pre>
     * .       UChar ABC[] = {0x41, 0x42, 0x43, 0};  // = "ABC"
     * .       UChar abc[] = {0x61, 0x62, 0x63, 0};  // = "abc"
     * .       UErrorCode status = U_ZERO_ERROR;
     * .       Collator *myCollation =
     * .                         Collator.createInstance(Locale.getUS(), status);
     * .       if (U_FAILURE(status)) return;
     * .       myCollation.setStrength(Collator.PRIMARY);
     * .       // result would be Collator.EQUAL ("abc" == "ABC")
     * .       // (no primary difference between "abc" and "ABC")
     * .       Collator.EComparisonResult result =
     * .                             myCollation.compare(abc, 3, ABC, 3);
     * .       myCollation.setStrength(Collator.TERTIARY);
     * .       // result would be Collator.LESS ("abc" &lt;&lt;&lt; "ABC")
     * .       // (with tertiary difference between "abc" and "ABC")
     * .       result = myCollation.compare(abc, 3, ABC, 3);
     * </pre>
     * @param source the source string array to be compared with.
     * @param sourceLength the length of the source string array.  If this value
     *        is equal to -1, the string array is null-terminated.
     * @param target the string that is to be compared with the source string.
     * @param targetLength the length of the target string array.  If this value
     *        is equal to -1, the string array is null-terminated.
     * @return Returns a byte value. GREATER if source is greater than target;
     *         EQUAL if source is equal to target; LESS if source is less than
     *         target
     * @deprecated ICU 2.6 use the overload with UErrorCode &
     */
    virtual EComparisonResult compare(const UChar* source, int sourceLength,
                                      const UChar* target, int targetLength)
                                      const;

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
                                      UErrorCode &status) = 0;

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
     * Compares two UTF-8 strings using the Collator.
     * Returns whether the first one compares less than/equal to/greater than
     * the second one.
     * This version takes UTF-8 input.
     * Note that a StringPiece can be implicitly constructed
     * from a std.string or a NUL-terminated const char * string.
     * @param source the first UTF-8 string
     * @param target the second UTF-8 string
     * @param status ICU status
     * @return UCOL_LESS, UCOL_EQUAL or UCOL_GREATER
     * @stable ICU 4.2
     */
    virtual UCollationResult compareUTF8(const StringPiece &source,
                                         const StringPiece &target,
                                         UErrorCode &status);

    /**
     * Transforms the string into a series of characters that can be compared
     * with CollationKey.compareTo. It is not possible to restore the original
     * string from the chars in the sort key.  The generated sort key handles
     * only a limited number of ignorable characters.
     * <p>Use CollationKey.equals or CollationKey.compare to compare the
     * generated sort keys.
     * If the source string is null, a null collation key will be returned.
     * @param source the source string to be transformed into a sort key.
     * @param key the collation key to be filled in
     * @param status the error code status.
     * @return the collation key of the string based on the collation rules.
     * @see CollationKey#compare
     * @stable ICU 2.0
     */
    virtual CollationKey& getCollationKey(const UnicodeString&  source,
                                          CollationKey& key,
                                          UErrorCode& status) = 0;

    /**
     * Transforms the string into a series of characters that can be compared
     * with CollationKey.compareTo. It is not possible to restore the original
     * string from the chars in the sort key.  The generated sort key handles
     * only a limited number of ignorable characters.
     * <p>Use CollationKey.equals or CollationKey.compare to compare the
     * generated sort keys.
     * <p>If the source string is null, a null collation key will be returned.
     * @param source the source string to be transformed into a sort key.
     * @param sourceLength length of the collation key
     * @param key the collation key to be filled in
     * @param status the error code status.
     * @return the collation key of the string based on the collation rules.
     * @see CollationKey#compare
     * @stable ICU 2.0
     */
    virtual CollationKey& getCollationKey(const UChar*source,
                                          int sourceLength,
                                          CollationKey& key,
                                          UErrorCode& status) = 0;
    /**
     * Generates the hash code for the collation object
     * @stable ICU 2.0
     */
    virtual int hashCode() = 0;

    /**
     * Gets the locale of the Collator
     *
     * @param type can be either requested, valid or actual locale. For more
     *             information see the definition of ULocDataLocaleType in
     *             uloc.h
     * @param status the error code status.
     * @return locale where the collation data lives. If the collator
     *         was instantiated from rules, locale is empty.
     * @deprecated ICU 2.8 This API is under consideration for revision
     * in ICU 3.0.
     */
    virtual Locale getLocale(ULocDataLocaleType type, UErrorCode& status) = 0;

    /**
     * Convenience method for comparing two strings based on the collation rules.
     * @param source the source string to be compared with.
     * @param target the target string to be compared with.
     * @return true if the first string is greater than the second one,
     *         according to the collation rules. false, otherwise.
     * @see Collator#compare
     * @stable ICU 2.0
     */
    boolean greater(const UnicodeString& source, const UnicodeString& target)
                  const;

    /**
     * Convenience method for comparing two strings based on the collation rules.
     * @param source the source string to be compared with.
     * @param target the target string to be compared with.
     * @return true if the first string is greater than or equal to the second
     *         one, according to the collation rules. false, otherwise.
     * @see Collator#compare
     * @stable ICU 2.0
     */
    boolean greaterOrEqual(const UnicodeString& source,
                         const UnicodeString& target);

    /**
     * Convenience method for comparing two strings based on the collation rules.
     * @param source the source string to be compared with.
     * @param target the target string to be compared with.
     * @return true if the strings are equal according to the collation rules.
     *         false, otherwise.
     * @see Collator#compare
     * @stable ICU 2.0
     */
    boolean equals(const UnicodeString& source, const UnicodeString& target);

    /**
     * Determines the minimum strength that will be used in comparison or
     * transformation.
     * <p>E.g. with strength == SECONDARY, the tertiary difference is ignored
     * <p>E.g. with strength == PRIMARY, the secondary and tertiary difference
     * are ignored.
     * @return the current comparison level.
     * @see Collator#setStrength
     * @deprecated ICU 2.6 Use getAttribute(UCOL_STRENGTH...) instead
     */
    virtual ECollationStrength getStrength();

    /**
     * Sets the minimum strength to be used in comparison or transformation.
     * <p>Example of use:
     * <pre>
     *  \code
     *  UErrorCode status = U_ZERO_ERROR;
     *  Collator*myCollation = Collator.createInstance(Locale.getUS(), status);
     *  if (U_FAILURE(status)) return;
     *  myCollation.setStrength(Collator.PRIMARY);
     *  // result will be "abc" == "ABC"
     *  // tertiary differences will be ignored
     *  Collator.ComparisonResult result = myCollation.compare("abc", "ABC");
     * \endcode
     * </pre>
     * @see Collator#getStrength
     * @param newStrength the new comparison level.
     * @deprecated ICU 2.6 Use setAttribute(UCOL_STRENGTH...) instead
     */
    virtual void setStrength(ECollationStrength newStrength);

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
     * @see UScriptCode
     * @see UColReorderCode
     * @stable ICU 4.8 
     */
     virtual int getReorderCodes(int *dest,
                                     int destCapacity,
                                     UErrorCode& status);

    /**
     * Sets the ordering of scripts for this collator.
     *
     * <p>The reordering codes are a combination of script codes and reorder codes.
     * @param reorderCodes An array of script codes in the new order. This can be null if the 
     * length is also set to 0. An empty array will clear any reordering codes on the collator.
     * @param reorderCodesLength The length of reorderCodes.
     * @param status error code
     * @see Collator#getReorderCodes
     * @see Collator#getEquivalentReorderCodes
     * @see UScriptCode
     * @see UColReorderCode
     * @stable ICU 4.8 
     */
     virtual void setReorderCodes(const int* reorderCodes,
                                  int reorderCodesLength,
                                  UErrorCode& status) ;

    /**
     * Retrieves the reorder codes that are grouped with the given reorder code. Some reorder
     * codes will be grouped and must reorder together.
     * @param reorderCode The reorder code to determine equivalence for. 
     * @param dest The array to fill with the script equivalence reordering codes.
     * @param destCapacity The length of dest. If it is 0, then dest may be null and the 
     * function will only return the length of the result without writing any of the result 
     * string (pre-flighting).
     * @param status A reference to an error code value, which must not indicate 
     * a failure before the function call.
     * @return The length of the of the reordering code equivalence array.
     * @see ucol_setReorderCodes
     * @see Collator#getReorderCodes
     * @see Collator#setReorderCodes
     * @see UScriptCode
     * @see UColReorderCode
     * @stable ICU 4.8 
     */
    static int U_EXPORT2 getEquivalentReorderCodes(int reorderCode,
                                int* dest,
                                int destCapacity,
                                UErrorCode& status);

    /**
     * Get name of the object for the desired Locale, in the desired langauge
     * @param objectLocale must be from getAvailableLocales
     * @param displayLocale specifies the desired locale for output
     * @param name the fill-in parameter of the return value
     * @return display-able name of the object for the object locale in the
     *         desired language
     * @stable ICU 2.0
     */
    static UnicodeString& U_EXPORT2 getDisplayName(const Locale& objectLocale,
                                         const Locale& displayLocale,
                                         UnicodeString& name);

    /**
    * Get name of the object for the desired Locale, in the langauge of the
    * default locale.
    * @param objectLocale must be from getAvailableLocales
    * @param name the fill-in parameter of the return value
    * @return name of the object for the desired locale in the default language
    * @stable ICU 2.0
    */
    static UnicodeString& U_EXPORT2 getDisplayName(const Locale& objectLocale,
                                         UnicodeString& name);

    /**
     * Get the set of Locales for which Collations are installed.
     *
     * <p>Note this does not include locales supported by registered collators.
     * If collators might have been registered, use the overload of getAvailableLocales
     * that returns a StringEnumeration.</p>
     *
     * @param count the output parameter of number of elements in the locale list
     * @return the list of available locales for which collations are installed
     * @stable ICU 2.0
     */
    private static final Locale* U_EXPORT2 getAvailableLocales(int32_t& count);

    /**
     * Return a StringEnumeration over the locales available at the time of the call,
     * including registered locales.  If a severe error occurs (such as out of memory
     * condition) this will return null. If there is no locale data, an empty enumeration
     * will be returned.
     * @return a StringEnumeration over the locales available at the time of the call
     * @stable ICU 2.6
     */
    static StringEnumeration* U_EXPORT2 getAvailableLocales();

    /**
     * Create a string enumerator of all possible keywords that are relevant to
     * collation. At this point, the only recognized keyword for this
     * service is "collation".
     * @param status input-output error code
     * @return a string enumeration over locale strings. The caller is
     * responsible for closing the result.
     * @stable ICU 3.0
     */
    static StringEnumeration* U_EXPORT2 getKeywords(UErrorCode& status);

    /**
     * Given a keyword, create a string enumeration of all values
     * for that keyword that are currently in use.
     * @param keyword a particular keyword as enumerated by
     * ucol_getKeywords. If any other keyword is passed in, status is set
     * to U_ILLEGAL_ARGUMENT_ERROR.
     * @param status input-output error code
     * @return a string enumeration over collation keyword values, or null
     * upon error. The caller is responsible for deleting the result.
     * @stable ICU 3.0
     */
    static StringEnumeration* U_EXPORT2 getKeywordValues(const char *keyword, UErrorCode& status);

    /**
     * Given a key and a locale, returns an array of string values in a preferred
     * order that would make a difference. These are all and only those values where
     * the open (creation) of the service with the locale formed from the input locale
     * plus input keyword and that value has different behavior than creation with the
     * input locale alone.
     * @param keyword        one of the keys supported by this service.  For now, only
     *                      "collation" is supported.
     * @param locale        the locale
     * @param commonlyUsed  if set to true it will return only commonly used values
     *                      with the given locale in preferred order.  Otherwise,
     *                      it will return all the available values for the locale.
     * @param status ICU status
     * @return a string enumeration over keyword values for the given key and the locale.
     * @stable ICU 4.2
     */
    static StringEnumeration* U_EXPORT2 getKeywordValuesForLocale(const char* keyword, const Locale& locale,
                                                                    boolean commonlyUsed, UErrorCode& status);

    /**
     * Return the functionally equivalent locale for the given
     * requested locale, with respect to given keyword, for the
     * collation service.  If two locales return the same result, then
     * collators instantiated for these locales will behave
     * equivalently.  The converse is not always true; two collators
     * may in fact be equivalent, but return different results, due to
     * internal details.  The return result has no other meaning than
     * that stated above, and implies nothing as to the relationship
     * between the two locales.  This is intended for use by
     * applications who wish to cache collators, or otherwise reuse
     * collators when possible.  The functional equivalent may change
     * over time.  For more information, please see the <a
     * href="http://userguide.icu-project.org/locale#TOC-Locales-and-Services">
     * Locales and Services</a> section of the ICU User Guide.
     * @param keyword a particular keyword as enumerated by
     * ucol_getKeywords.
     * @param locale the requested locale
     * @param isAvailable reference to a fillin parameter that
     * indicates whether the requested locale was 'available' to the
     * collation service. A locale is defined as 'available' if it
     * physically exists within the collation locale data.
     * @param status reference to input-output error code
     * @return the functionally equivalent collation locale, or the root
     * locale upon error.
     * @stable ICU 3.0
     */
    static Locale U_EXPORT2 getFunctionalEquivalent(const char* keyword, const Locale& locale,
                                          boolean& isAvailable, UErrorCode& status);

#if !UCONFIG_NO_SERVICE
    /**
     * Register a new Collator.  The collator will be adopted.
     * @param toAdopt the Collator instance to be adopted
     * @param locale the locale with which the collator will be associated
     * @param status the in/out status code, no special meanings are assigned
     * @return a registry key that can be used to unregister this collator
     * @stable ICU 2.6
     */
    static URegistryKey U_EXPORT2 registerInstance(Collator* toAdopt, const Locale& locale, UErrorCode& status);

    /**
     * Register a new CollatorFactory.  The factory will be adopted.
     * @param toAdopt the CollatorFactory instance to be adopted
     * @param status the in/out status code, no special meanings are assigned
     * @return a registry key that can be used to unregister this collator
     * @stable ICU 2.6
     */
    static URegistryKey U_EXPORT2 registerFactory(CollatorFactory* toAdopt, UErrorCode& status);

    /**
     * Unregister a previously-registered Collator or CollatorFactory
     * using the key returned from the register call.  Key becomes
     * invalid after a successful call and should not be used again.
     * The object corresponding to the key will be deleted.
     * @param key the registry key returned by a previous call to registerInstance
     * @param status the in/out status code, no special meanings are assigned
     * @return true if the collator for the key was successfully unregistered
     * @stable ICU 2.6
     */
    static boolean U_EXPORT2 unregister(URegistryKey key, UErrorCode& status);
#endif /* UCONFIG_NO_SERVICE */

    /**
     * Gets the version information for a Collator.
     * @param info the version # information, the result will be filled in
     * @stable ICU 2.0
     */
    virtual void getVersion(VersionInfo info) = 0;

    /**
     * Returns a unique class ID POLYMORPHICALLY. Pure virtual method.
     * This method is to implement a simple version of RTTI, since not all C++
     * compilers support genuine RTTI. Polymorphic operator==() and clone()
     * methods call this method.
     * @return The class ID for this object. All objects of a given class have
     *         the same class ID.  Objects of other classes have different class
     *         IDs.
     * @stable ICU 2.0
     */
    virtual UClassID getDynamicClassID() = 0;

    /**
     * Universal attribute setter
     * @param attr attribute type
     * @param value attribute value
     * @param status to indicate whether the operation went on smoothly or
     *        there were errors
     * @stable ICU 2.2
     */
    abstract void setAttribute(UColAttribute attr, UColAttributeValue value,
                              UErrorCode &status);

    /**
     * Universal attribute getter
     * @param attr attribute type
     * @param status to indicate whether the operation went on smoothly or
     *        there were errors
     * @return attribute value
     * @stable ICU 2.2
     */
    virtual UColAttributeValue getAttribute(UColAttribute attr,
                                            UErrorCode &status) = 0;

    /**
     * Sets the variable top to the top of the specified reordering group.
     * The variable top determines the highest-sorting character
     * which is affected by UCOL_ALTERNATE_HANDLING.
     * If that attribute is set to UCOL_NON_IGNORABLE, then the variable top has no effect.
     *
     * The base class implementation sets U_UNSUPPORTED_ERROR.
     * @param group one of Collator.ReorderCodes.SPACE, Collator.ReorderCodes.PUNCTUATION,
     *              Collator.ReorderCodes.SYMBOL, Collator.ReorderCodes.CURRENCY;
     *              or Collator.ReorderCodes.DEFAULT to restore the default max variable group
     * @param errorCode Standard ICU error code. Its input value must
     *                  pass the U_SUCCESS() test, or else the function returns
     *                  immediately. Check for U_FAILURE() on output or use with
     *                  function chaining. (See User Guide for details.)
     * @return *this
     * @see getMaxVariable
     * @draft ICU 53
     */
    virtual Collator &setMaxVariable(UColReorderCode group);

    /**
     * Returns the maximum reordering group whose characters are affected by UCOL_ALTERNATE_HANDLING.
     *
     * The base class implementation returns Collator.ReorderCodes.PUNCTUATION.
     * @return the maximum variable reordering group.
     * @see setMaxVariable
     * @draft ICU 53
     */
    virtual UColReorderCode getMaxVariable();

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
    abstract uint32_t setVariableTop(const UChar *varTop, int len, UErrorCode &status);

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
    abstract uint32_t setVariableTop(const UnicodeString &varTop, UErrorCode &status);

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
    abstract void setVariableTop(uint32_t varTop, UErrorCode &status);

    /**
     * Gets the variable top value of a Collator.
     * @param status error code (not changed by function). If error code is set, the return value is undefined.
     * @return the variable top primary weight
     * @see getMaxVariable
     * @stable ICU 2.0
     */
    virtual uint32_t getVariableTop(UErrorCode &status) = 0;

    /**
     * Get a UnicodeSet that contains all the characters and sequences
     * tailored in this collator.
     * @param status      error code of the operation
     * @return a pointer to a UnicodeSet object containing all the
     *         code points and sequences that may sort differently than
     *         in the root collator. The object must be disposed of by using delete
     * @stable ICU 2.4
     */
    virtual UnicodeSet *getTailoredSet(UErrorCode &status);

    /**
     * Same as clone().
     * The base class implementation simply calls clone().
     * @return a copy of this object, owned by the caller
     * @see clone()
     * @deprecated ICU 50 no need to have two methods for cloning
     */
    virtual Collator* safeClone();

    /**
     * Get the sort key as an array of bytes from a UnicodeString.
     * Sort key byte arrays are zero-terminated and can be compared using
     * strcmp().
     * @param source string to be processed.
     * @param result buffer to store result in. If null, number of bytes needed
     *        will be returned.
     * @param resultLength length of the result buffer. If if not enough the
     *        buffer will be filled to capacity.
     * @return Number of bytes needed for storing the sort key
     * @stable ICU 2.2
     */
    virtual int getSortKey(const UnicodeString& source,
                              uint8_t* result,
                              int resultLength) = 0;

    /**
     * Get the sort key as an array of bytes from a UChar buffer.
     * Sort key byte arrays are zero-terminated and can be compared using
     * strcmp().
     * @param source string to be processed.
     * @param sourceLength length of string to be processed.
     *        If -1, the string is 0 terminated and length will be decided by the
     *        function.
     * @param result buffer to store result in. If null, number of bytes needed
     *        will be returned.
     * @param resultLength length of the result buffer. If if not enough the
     *        buffer will be filled to capacity.
     * @return Number of bytes needed for storing the sort key
     * @stable ICU 2.2
     */
    virtual int getSortKey(const UChar*source, int sourceLength,
                               uint8_t*result, int resultLength) = 0;

    /**
     * Produce a bound for a given sortkey and a number of levels.
     * Return value is always the number of bytes needed, regardless of
     * whether the result buffer was big enough or even valid.<br>
     * Resulting bounds can be used to produce a range of strings that are
     * between upper and lower bounds. For example, if bounds are produced
     * for a sortkey of string "smith", strings between upper and lower
     * bounds with one level would include "Smith", "SMITH", "sMiTh".<br>
     * There are two upper bounds that can be produced. If UCOL_BOUND_UPPER
     * is produced, strings matched would be as above. However, if bound
     * produced using UCOL_BOUND_UPPER_LONG is used, the above example will
     * also match "Smithsonian" and similar.<br>
     * For more on usage, see example in cintltst/capitst.c in procedure
     * TestBounds.
     * Sort keys may be compared using <TT>strcmp</TT>.
     * @param source The source sortkey.
     * @param sourceLength The length of source, or -1 if null-terminated.
     *                     (If an unmodified sortkey is passed, it is always null
     *                      terminated).
     * @param boundType Type of bound required. It can be UCOL_BOUND_LOWER, which
     *                  produces a lower inclusive bound, UCOL_BOUND_UPPER, that
     *                  produces upper bound that matches strings of the same length
     *                  or UCOL_BOUND_UPPER_LONG that matches strings that have the
     *                  same starting substring as the source string.
     * @param noOfLevels  Number of levels required in the resulting bound (for most
     *                    uses, the recommended value is 1). See users guide for
     *                    explanation on number of levels a sortkey can have.
     * @param result A pointer to a buffer to receive the resulting sortkey.
     * @param resultLength The maximum size of result.
     * @param status Used for returning error code if something went wrong. If the
     *               number of levels requested is higher than the number of levels
     *               in the source key, a warning (U_SORT_KEY_TOO_SHORT_WARNING) is
     *               issued.
     * @return The size needed to fully store the bound.
     * @see ucol_keyHashCode
     * @stable ICU 2.1
     */
    static int U_EXPORT2 getBound(const uint8_t       *source,
            int             sourceLength,
            UColBoundMode       boundType,
            uint32_t            noOfLevels,
            uint8_t             *result,
            int             resultLength,
            UErrorCode          &status);


protected:

    // Collator protected constructors -------------------------------------

    /**
    * Default constructor.
    * Constructor is different from the old default Collator constructor.
    * The task for determing the default collation strength and normalization
    * mode is left to the child class.
    * @stable ICU 2.0
    */
    Collator();

#ifndef U_HIDE_DEPRECATED_API
    /**
    * Constructor.
    * Empty constructor, does not handle the arguments.
    * This constructor is done for backward compatibility with 1.7 and 1.8.
    * The task for handling the argument collation strength and normalization
    * mode is left to the child class.
    * @param collationStrength collation strength
    * @param decompositionMode
    * @deprecated ICU 2.4. Subclasses should use the default constructor
    * instead and handle the strength and normalization mode themselves.
    */
    Collator(UCollationStrength collationStrength,
             UNormalizationMode decompositionMode);
#endif  /* U_HIDE_DEPRECATED_API */

    /**
    * Copy constructor.
    * @param other Collator object to be copied from
    * @stable ICU 2.0
    */
    Collator(const Collator& other);

    // Collator protected methods -----------------------------------------


   /**
    * Used internally by registration to define the requested and valid locales.
    * @param requestedLocale the requested locale
    * @param validLocale the valid locale
    * @param actualLocale the actual locale
    * @internal
    */
    virtual void setLocales(const Locale& requestedLocale, const Locale& validLocale, const Locale& actualLocale);

public:
    /** Get the short definition string for a collator. This internal API harvests the collator's
     *  locale and the attribute set and produces a string that can be used for opening 
     *  a collator with the same properties using the ucol_openFromShortString API.
     *  This string will be normalized.
     *  The structure and the syntax of the string is defined in the "Naming collators"
     *  section of the users guide: 
     *  http://userguide.icu-project.org/collation/concepts#TOC-Collator-naming-scheme
     *  This function supports preflighting.
     * 
     *  This is internal, and intended to be used with delegate converters.
     *
     *  @param locale a locale that will appear as a collators locale in the resulting
     *                short string definition. If null, the locale will be harvested 
     *                from the collator.
     *  @param buffer space to hold the resulting string
     *  @param capacity capacity of the buffer
     *  @param status for returning errors. All the preflighting errors are featured
     *  @return length of the resulting string
     *  @see ucol_openFromShortString
     *  @see ucol_normalizeShortDefinitionString
     *  @see ucol_getShortDefinitionString
     *  @internal
     */
    virtual int internalGetShortDefinitionString(const char *locale,
                                                     char *buffer,
                                                     int capacity,
                                                     UErrorCode &status);

    /**
     * Implements ucol_strcollUTF8().
     * @internal
     */
    virtual UCollationResult internalCompareUTF8(
            const char *left, int leftLength,
            const char *right, int rightLength,
            );

    /**
     * Implements ucol_nextSortKeyPart().
     * @internal
     */
    virtual int
    internalNextSortKeyPart(
            UCharIterator *iter, uint32_t state[2],
            uint8_t *dest, int count);

#ifndef U_HIDE_INTERNAL_API
    /** @internal */
    static Collator *fromUCollator(UCollator *uc) {
        return reinterpret_cast<Collator *>(uc);
    }
    /** @internal */
    static const Collator *fromUCollator(const UCollator *uc) {
        return reinterpret_cast<const Collator *>(uc);
    }
    /** @internal */
    UCollator *toUCollator() {
        return reinterpret_cast<UCollator *>(this);
    }
    /** @internal */
    const UCollator *toUCollator() {
        return reinterpret_cast<const UCollator *>(this);
    }
#endif  // U_HIDE_INTERNAL_API

private:
    /**
     * Assignment operator. Private for now.
     */
    Collator& operator=(const Collator& other);

    friend class CFactory;
    friend class SimpleCFactory;
    friend class ICUCollatorFactory;
    friend class ICUCollatorService;
    static Collator* makeInstance(const Locale& desiredLocale,
                                  UErrorCode& status);
};

#if !UCONFIG_NO_SERVICE
/**
 * A factory, used with registerFactory, the creates multiple collators and provides
 * display names for them.  A factory supports some number of locales-- these are the
 * locales for which it can create collators.  The factory can be visible, in which
 * case the supported locales will be enumerated by getAvailableLocales, or invisible,
 * in which they are not.  Invisible locales are still supported, they are just not
 * listed by getAvailableLocales.
 * <p>
 * If standard locale display names are sufficient, Collator instances can
 * be registered using registerInstance instead.</p>
 * <p>
 * Note: if the collators are to be used from C APIs, they must be instances
 * of RuleBasedCollator.</p>
 *
 * @stable ICU 2.6
 */
class CollatorFactory {
public:

    /**
     * Destructor
     * @stable ICU 3.0
     */
    virtual ~CollatorFactory();

    /**
     * Return true if this factory is visible.  Default is true.
     * If not visible, the locales supported by this factory will not
     * be listed by getAvailableLocales.
     * @return true if the factory is visible.
     * @stable ICU 2.6
     */
    virtual boolean visible();

    /**
     * Return a collator for the provided locale.  If the locale
     * is not supported, return null.
     * @param loc the locale identifying the collator to be created.
     * @return a new collator if the locale is supported, otherwise null.
     * @stable ICU 2.6
     */
    abstract Collator* createCollator(const Locale& loc);

    /**
     * Return the name of the collator for the objectLocale, localized for the displayLocale.
     * If objectLocale is not supported, or the factory is not visible, set the result string
     * to bogus.
     * @param objectLocale the locale identifying the collator
     * @param displayLocale the locale for which the display name of the collator should be localized
     * @param result an output parameter for the display name, set to bogus if not supported.
     * @return the display name
     * @stable ICU 2.6
     */
    virtual  UnicodeString& getDisplayName(const Locale& objectLocale,
                                           const Locale& displayLocale,
                                           UnicodeString& result);

    /**
     * Return an array of all the locale names directly supported by this factory.
     * The number of names is returned in count.  This array is owned by the factory.
     * Its contents must never change.
     * @param count output parameter for the number of locales supported by the factory
     * @param status the in/out error code
     * @return a pointer to an array of count UnicodeStrings.
     * @stable ICU 2.6
     */
    abstract const UnicodeString * getSupportedIDs(int &count, UErrorCode& status);
};
#endif /* UCONFIG_NO_SERVICE */

// Collator methods -----------------------------------------------

U_NAMESPACE_END

#endif /* #if !UCONFIG_NO_COLLATION */

#endif
/*
 ******************************************************************************
 * Copyright (C) 1996-2013, International Business Machines Corporation and
 * others. All Rights Reserved.
 ******************************************************************************
 */

/**
 * File coll.cpp
 *
 * Created by: Helena Shih
 *
 * Modification History:
 *
 *  Date        Name        Description
 *  2/5/97      aliu        Modified createDefault to load collation data from
 *                          binary files when possible.  Added related methods
 *                          createCollationFromFile, chopLocale, createPathName.
 *  2/11/97     aliu        Added methods addToCache, findInCache, which implement
 *                          a Collation cache.  Modified createDefault to look in
 *                          cache first, and also to store newly created Collation
 *                          objects in the cache.  Modified to not use gLocPath.
 *  2/12/97     aliu        Modified to create objects from RuleBasedCollator cache.
 *                          Moved cache out of Collation class.
 *  2/13/97     aliu        Moved several methods out of this class and into
 *                          RuleBasedCollator, with modifications.  Modified
 *                          createDefault() to call new RuleBasedCollator(Locale&)
 *                          constructor.  General clean up and documentation.
 *  2/20/97     helena      Added clone, operator==, operator!=, operator=, and copy
 *                          constructor.
 * 05/06/97     helena      Added memory allocation error detection.
 * 05/08/97     helena      Added createInstance().
 *  6/20/97     helena      Java class name change.
 * 04/23/99     stephen     Removed EDecompositionMode, merged with 
 *                          Normalizer.EMode
 * 11/23/9      srl         Inlining of some critical functions
 * 01/29/01     synwee      Modified into a C++ wrapper calling C APIs (ucol.h)
 * 2012-2013    markus      Rewritten in C++ again.
 */

#include "utypeinfo.h"  // for 'typeid' to work 

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/coll.h"
#include "unicode/tblcoll.h"
#include "collationdata.h"
#include "collationroot.h"
#include "collationtailoring.h"
#include "ucol_imp.h"
#include "cstring.h"
#include "cmemory.h"
#include "umutex.h"
#include "servloc.h"
#include "uassert.h"
#include "ustrenum.h"
#include "uresimp.h"
#include "ucln_in.h"

static icu.Locale* availableLocaleList = null;
static int  availableLocaleListCount;
static icu.ICULocaleService* gService = null;
static icu.UInitOnce gServiceInitOnce = U_INITONCE_INITIALIZER;
static icu.UInitOnce gAvailableLocaleListInitOnce;

/**
 * Release all static memory held by collator.
 */
U_CDECL_BEGIN
static boolean U_CALLCONV collator_cleanup() {
#if !UCONFIG_NO_SERVICE
    if (gService) {
        delete gService;
        gService = null;
    }
    gServiceInitOnce.reset();
#endif
    if (availableLocaleList) {
        delete []availableLocaleList;
        availableLocaleList = null;
    }
    availableLocaleListCount = 0;
    gAvailableLocaleListInitOnce.reset();
    return true;
}

U_CDECL_END

U_NAMESPACE_BEGIN

#if !UCONFIG_NO_SERVICE

// ------------------------------------------
//
// Registration
//

//-------------------------------------------

CollatorFactory.~CollatorFactory() {}

//-------------------------------------------

boolean
CollatorFactory.visible() {
    return true;
}

//-------------------------------------------

UnicodeString& 
CollatorFactory.getDisplayName(const Locale& objectLocale, 
                                const Locale& displayLocale,
                                UnicodeString& result)
{
  return objectLocale.getDisplayName(displayLocale, result);
}

// -------------------------------------

class ICUCollatorFactory extends ICUResourceBundleFactory {
 public:
    ICUCollatorFactory() : ICUResourceBundleFactory(UnicodeString(U_ICUDATA_COLL, -1, US_INV)) { }
    virtual ~ICUCollatorFactory();
 protected:
    virtual UObject* create(const ICUServiceKey& key, const ICUService* service, UErrorCode& status);
};

ICUCollatorFactory.~ICUCollatorFactory() {}

UObject*
ICUCollatorFactory.create(const ICUServiceKey& key, const ICUService* /* service */, UErrorCode& status) {
    if (handlesKey(key, status)) {
        const LocaleKey& lkey = (const LocaleKey&)key;
        Locale loc;
        // make sure the requested locale is correct
        // default LocaleFactory uses currentLocale since that's the one vetted by handlesKey
        // but for ICU rb resources we use the actual one since it will fallback again
        lkey.canonicalLocale(loc);
        
        return Collator.makeInstance(loc, status);
    }
    return null;
}

// -------------------------------------

class ICUCollatorService extendsICULocaleService {
public:
    ICUCollatorService()
        : ICULocaleService(UNICODE_STRING_SIMPLE("Collator"))
    {
        UErrorCode status = U_ZERO_ERROR;
        registerFactory(new ICUCollatorFactory(), status);
    }

    virtual ~ICUCollatorService();

    virtual UObject* cloneInstance(UObject* instance) {
        return ((Collator*)instance).clone();
    }
    
    virtual UObject* handleDefault(const ICUServiceKey& key, UnicodeString* actualID, UErrorCode& status) {
        LocaleKey& lkey = (LocaleKey&)key;
        if (actualID) {
            // Ugly Hack Alert! We return an empty actualID to signal
            // to callers that this is a default object, not a "real"
            // service-created object. (TODO remove in 3.0) [aliu]
            actualID.truncate(0);
        }
        Locale loc("");
        lkey.canonicalLocale(loc);
        return Collator.makeInstance(loc, status);
    }
    
    virtual UObject* getKey(ICUServiceKey& key, UnicodeString* actualReturn, UErrorCode& status) {
        UnicodeString ar;
        if (actualReturn == null) {
            actualReturn = &ar;
        }
        Collator* result = (Collator*)ICULocaleService.getKey(key, actualReturn, status);
        // Ugly Hack Alert! If the actualReturn length is zero, this
        // means we got a default object, not a "real" service-created
        // object.  We don't call setLocales() on a default object,
        // because that will overwrite its correct built-in locale
        // metadata (valid & actual) with our incorrect data (all we
        // have is the requested locale). (TODO remove in 3.0) [aliu]
        if (result && actualReturn.length() > 0) {
            const LocaleKey& lkey = (const LocaleKey&)key;
            Locale canonicalLocale("");
            Locale currentLocale("");
            
            LocaleUtility.initLocaleFromName(*actualReturn, currentLocale);
            result.setLocales(lkey.canonicalLocale(canonicalLocale), currentLocale, currentLocale);
        }
        return result;
    }

    virtual boolean isDefault() {
        return countFactories() == 1;
    }
};

ICUCollatorService.~ICUCollatorService() {}

// -------------------------------------

static void U_CALLCONV initService() {
    gService = new ICUCollatorService();
    ucln_i18n_registerCleanup(UCLN_I18N_COLLATOR, collator_cleanup);
}


static ICULocaleService* 
getService()
{
    umtx_initOnce(gServiceInitOnce, &initService);
    return gService;
}

// -------------------------------------

static boolean
hasService() 
{
    boolean retVal = !gServiceInitOnce.isReset() && (getService() != null);
    return retVal;
}

#endif /* UCONFIG_NO_SERVICE */

static void U_CALLCONV 
initAvailableLocaleList(UErrorCode &status) {
    assert(availableLocaleListCount == 0);
    assert(availableLocaleList == null);
    // for now, there is a hardcoded list, so just walk through that list and set it up.
    UResourceBundle *index = null;
    UResourceBundle installed;
    int i = 0;
    
    ures_initStackObject(&installed);
    index = ures_openDirect(U_ICUDATA_COLL, "res_index", &status);
    ures_getByKey(index, "InstalledLocales", &installed, &status);
    
    if(U_SUCCESS(status)) {
        availableLocaleListCount = ures_getSize(&installed);
        availableLocaleList = new Locale[availableLocaleListCount];
        
        if (availableLocaleList != null) {
            ures_resetIterator(&installed);
            while(ures_hasNext(&installed)) {
                const char *tempKey = null;
                ures_getNextString(&installed, null, &tempKey, &status);
                availableLocaleList[i++] = Locale(tempKey);
            }
        }
        assert(availableLocaleListCount == i);
        ures_close(&installed);
    }
    ures_close(index);
    ucln_i18n_registerCleanup(UCLN_I18N_COLLATOR, collator_cleanup);
}

static boolean isAvailableLocaleListInitialized(UErrorCode &status) {
    umtx_initOnce(gAvailableLocaleListInitOnce, &initAvailableLocaleList, status);
    return U_SUCCESS(status);
}


// Collator public methods -----------------------------------------------

Collator* U_EXPORT2 Collator.createInstance(UErrorCode& success) 
{
    return createInstance(Locale.getDefault(), success);
}

Collator* U_EXPORT2 Collator.createInstance(const Locale& desiredLocale,
                                   UErrorCode& status)
{
    if (U_FAILURE(status)) 
        return 0;
    
#if !UCONFIG_NO_SERVICE
    if (hasService()) {
        Locale actualLoc;
        Collator *result =
            (Collator*)gService.get(desiredLocale, &actualLoc, status);

        // Ugly Hack Alert! If the returned locale is empty (not root,
        // but empty -- getName() == "") then that means the service
        // returned a default object, not a "real" service object.  In
        // that case, the locale metadata (valid & actual) is setup
        // correctly already, and we don't want to overwrite it. (TODO
        // remove in 3.0) [aliu]
        if (*actualLoc.getName() != 0) {
            result.setLocales(desiredLocale, actualLoc, actualLoc);
        }
        return result;
    }
#endif
    return makeInstance(desiredLocale, status);
}


Collator* Collator.makeInstance(const Locale&  desiredLocale, 
                                         UErrorCode& status)
{
    Locale validLocale("");
    const CollationTailoring *t =
        CollationLoader.loadTailoring(desiredLocale, validLocale, status);
    if (U_SUCCESS(status)) {
        Collator *result = new RuleBasedCollator(t);
        if (result != null) {
            result.setLocales(desiredLocale, validLocale, t.actualLocale);
            return result;
        }
        status = U_MEMORY_ALLOCATION_ERROR;
    }
    if (t != null) {
        t.deleteIfZeroRefCount();
    }
    return null;
}

Collator *
Collator.safeClone() {
    return clone();
}

// implement deprecated, previously abstract method
Collator.EComparisonResult Collator.compare(const UnicodeString& source, 
                                    const UnicodeString& target) const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (EComparisonResult)compare(source, target, ec);
}

// implement deprecated, previously abstract method
Collator.EComparisonResult Collator.compare(const UnicodeString& source,
                                    const UnicodeString& target,
                                    int length) const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (EComparisonResult)compare(source, target, length, ec);
}

// implement deprecated, previously abstract method
Collator.EComparisonResult Collator.compare(const UChar* source, int sourceLength,
                                    const UChar* target, int targetLength) 
                                    const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (EComparisonResult)compare(source, sourceLength, target, targetLength, ec);
}

UCollationResult Collator.compare(UCharIterator &/*sIter*/,
                                   UCharIterator &/*tIter*/,
                                   UErrorCode &status) {
    if(U_SUCCESS(status)) {
        // Not implemented in the base class.
        status = U_UNSUPPORTED_ERROR;
    }
    return UCOL_EQUAL;
}

UCollationResult Collator.compareUTF8(const StringPiece &source,
                                       const StringPiece &target,
                                       UErrorCode &status) {
    if(U_FAILURE(status)) {
        return UCOL_EQUAL;
    }
    UCharIterator sIter, tIter;
    uiter_setUTF8(&sIter, source.data(), source.length());
    uiter_setUTF8(&tIter, target.data(), target.length());
    return compare(sIter, tIter, status);
}

boolean Collator.equals(const UnicodeString& source, 
                       const UnicodeString& target) const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (compare(source, target, ec) == UCOL_EQUAL);
}

boolean Collator.greaterOrEqual(const UnicodeString& source, 
                               const UnicodeString& target) const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (compare(source, target, ec) != UCOL_LESS);
}

boolean Collator.greater(const UnicodeString& source, 
                        const UnicodeString& target) const
{
    UErrorCode ec = U_ZERO_ERROR;
    return (compare(source, target, ec) == UCOL_GREATER);
}

// this API  ignores registered collators, since it returns an
// array of indefinite lifetime
const Locale* U_EXPORT2 Collator.getAvailableLocales(int32_t& count) 
{
    UErrorCode status = U_ZERO_ERROR;
    Locale *result = null;
    count = 0;
    if (isAvailableLocaleListInitialized(status))
    {
        result = availableLocaleList;
        count = availableLocaleListCount;
    }
    return result;
}

UnicodeString& U_EXPORT2 Collator.getDisplayName(const Locale& objectLocale,
                                        const Locale& displayLocale,
                                        UnicodeString& name)
{
#if !UCONFIG_NO_SERVICE
    if (hasService()) {
        UnicodeString locNameStr;
        LocaleUtility.initNameFromLocale(objectLocale, locNameStr);
        return gService.getDisplayName(locNameStr, name, displayLocale);
    }
#endif
    return objectLocale.getDisplayName(displayLocale, name);
}

UnicodeString& U_EXPORT2 Collator.getDisplayName(const Locale& objectLocale,
                                        UnicodeString& name)
{   
    return getDisplayName(objectLocale, Locale.getDefault(), name);
}

/* This is useless information */
/*void Collator.getVersion(VersionInfo versionInfo) const
{
  if (versionInfo!=null)
    uprv_memcpy(versionInfo, fVersion, U_MAX_VERSION_LENGTH);
}
*/

// UCollator protected constructor destructor ----------------------------

/**
* Default constructor.
* Constructor is different from the old default Collator constructor.
* The task for determing the default collation strength and normalization mode
* is left to the child class.
*/
Collator.Collator()
: UObject()
{
}

/**
* Constructor.
* Empty constructor, does not handle the arguments.
* This constructor is done for backward compatibility with 1.7 and 1.8.
* The task for handling the argument collation strength and normalization 
* mode is left to the child class.
* @param collationStrength collation strength
* @param decompositionMode
* @deprecated 2.4 use the default constructor instead
*/
Collator.Collator(UCollationStrength, UNormalizationMode )
: UObject()
{
}

Collator.~Collator()
{
}

Collator.Collator(const Collator &other)
    : UObject(other)
{
}

boolean Collator.operator==(const Collator& other) const
{
    // Subclasses: Call this method and then add more specific checks.
    return typeid(*this) == typeid(other);
}

boolean Collator.operator!=(const Collator& other) const
{
    return (boolean)!(*this == other);
}

int32_t U_EXPORT2 Collator.getBound(const uint8_t       *source,
                           int             sourceLength,
                           UColBoundMode       boundType,
                           uint32_t            noOfLevels,
                           uint8_t             *result,
                           int             resultLength,
                           UErrorCode          &status)
{
    return ucol_getBound(source, sourceLength, boundType, noOfLevels, result, resultLength, &status);
}

void
Collator.setLocales(const Locale& /* requestedLocale */, const Locale& /* validLocale */, const Locale& /*actualLocale*/) {
}

UnicodeSet *Collator.getTailoredSet(UErrorCode &status) const
{
    if(U_FAILURE(status)) {
        return null;
    }
    // everything can be changed
    return new UnicodeSet(0, 0x10FFFF);
}

// -------------------------------------

#if !UCONFIG_NO_SERVICE
URegistryKey U_EXPORT2
Collator.registerInstance(Collator* toAdopt, const Locale& locale, UErrorCode& status) 
{
    if (U_SUCCESS(status)) {
        return getService().registerInstance(toAdopt, locale, status);
    }
    return null;
}

// -------------------------------------

class CFactory extendsLocaleKeyFactory {
private:
    CollatorFactory* _delegate;
    Hashtable* _ids;
    
public:
    CFactory(CollatorFactory* delegate, UErrorCode& status) 
        : LocaleKeyFactory(delegate.visible() ? VISIBLE : INVISIBLE)
        , _delegate(delegate)
        , _ids(null)
    {
        if (U_SUCCESS(status)) {
            int count = 0;
            _ids = new Hashtable(status);
            if (_ids) {
                const UnicodeString * idlist = _delegate.getSupportedIDs(count, status);
                for (int i = 0; i < count; ++i) {
                    _ids.put(idlist[i], (void*)this, status);
                    if (U_FAILURE(status)) {
                        delete _ids;
                        _ids = null;
                        return;
                    }
                }
            } else {
                status = U_MEMORY_ALLOCATION_ERROR;
            }
        }
    }

    virtual ~CFactory();

    virtual UObject* create(const ICUServiceKey& key, const ICUService* service, UErrorCode& status);
    
protected:
    virtual const Hashtable* getSupportedIDs(UErrorCode& status) const
    {
        if (U_SUCCESS(status)) {
            return _ids;
        }
        return null;
    }
    
    virtual UnicodeString&
        getDisplayName(const UnicodeString& id, const Locale& locale, UnicodeString& result);
};

CFactory.~CFactory()
{
    delete _delegate;
    delete _ids;
}

UObject* 
CFactory.create(const ICUServiceKey& key, const ICUService* /* service */, UErrorCode& status) const
{
    if (handlesKey(key, status)) {
        const LocaleKey& lkey = (const LocaleKey&)key;
        Locale validLoc;
        lkey.currentLocale(validLoc);
        return _delegate.createCollator(validLoc);
    }
    return null;
}

UnicodeString&
CFactory.getDisplayName(const UnicodeString& id, const Locale& locale, UnicodeString& result) 
{
    if ((_coverage & 0x1) == 0) {
        UErrorCode status = U_ZERO_ERROR;
        const Hashtable* ids = getSupportedIDs(status);
        if (ids && (ids.get(id) != null)) {
            Locale loc;
            LocaleUtility.initLocaleFromName(id, loc);
            return _delegate.getDisplayName(loc, locale, result);
        }
    }
    result.setToBogus();
    return result;
}

URegistryKey U_EXPORT2
Collator.registerFactory(CollatorFactory* toAdopt, UErrorCode& status)
{
    if (U_SUCCESS(status)) {
        CFactory* f = new CFactory(toAdopt, status);
        if (f) {
            return getService().registerFactory(f, status);
        }
        status = U_MEMORY_ALLOCATION_ERROR;
    }
    return null;
}

// -------------------------------------

boolean U_EXPORT2
Collator.unregister(URegistryKey key, UErrorCode& status) 
{
    if (U_SUCCESS(status)) {
        if (hasService()) {
            return gService.unregister(key, status);
        }
        status = U_ILLEGAL_ARGUMENT_ERROR;
    }
    return false;
}
#endif /* UCONFIG_NO_SERVICE */

class CollationLocaleListEnumeration extendsStringEnumeration {
private:
    int index;
public:
    static UClassID U_EXPORT2 getStaticClassID();
    virtual UClassID getDynamicClassID();
public:
    CollationLocaleListEnumeration()
        : index(0)
    {
        // The global variables should already be initialized.
        //isAvailableLocaleListInitialized(status);
    }

    virtual ~CollationLocaleListEnumeration();

    virtual StringEnumeration * clone() const
    {
        CollationLocaleListEnumeration *result = new CollationLocaleListEnumeration();
        if (result) {
            result.index = index;
        }
        return result;
    }

    virtual int count(UErrorCode &/*status*/) {
        return availableLocaleListCount;
    }

    virtual const char* next(int32_t* resultLength, UErrorCode& /*status*/) {
        const char* result;
        if(index < availableLocaleListCount) {
            result = availableLocaleList[index++].getName();
            if(resultLength != null) {
                *resultLength = (int32_t)uprv_strlen(result);
            }
        } else {
            if(resultLength != null) {
                *resultLength = 0;
            }
            result = null;
        }
        return result;
    }

    virtual const UnicodeString* snext(UErrorCode& status) {
        int resultLength = 0;
        const char *s = next(&resultLength, status);
        return setChars(s, resultLength, status);
    }

    virtual void reset(UErrorCode& /*status*/) {
        index = 0;
    }
};

CollationLocaleListEnumeration.~CollationLocaleListEnumeration() {}

UOBJECT_DEFINE_RTTI_IMPLEMENTATION(CollationLocaleListEnumeration)


// -------------------------------------

StringEnumeration* U_EXPORT2
Collator.getAvailableLocales()
{
#if !UCONFIG_NO_SERVICE
    if (hasService()) {
        return getService().getAvailableLocales();
    }
#endif /* UCONFIG_NO_SERVICE */
    UErrorCode status = U_ZERO_ERROR;
    if (isAvailableLocaleListInitialized(status)) {
        return new CollationLocaleListEnumeration();
    }
    return null;
}

StringEnumeration* U_EXPORT2
Collator.getKeywords(UErrorCode& status) {
    // This is a wrapper over ucol_getKeywords
    UEnumeration* uenum = ucol_getKeywords(&status);
    if (U_FAILURE(status)) {
        uenum_close(uenum);
        return null;
    }
    return new UStringEnumeration(uenum);
}

StringEnumeration* U_EXPORT2
Collator.getKeywordValues(const char *keyword, UErrorCode& status) {
    // This is a wrapper over ucol_getKeywordValues
    UEnumeration* uenum = ucol_getKeywordValues(keyword, &status);
    if (U_FAILURE(status)) {
        uenum_close(uenum);
        return null;
    }
    return new UStringEnumeration(uenum);
}

StringEnumeration* U_EXPORT2
Collator.getKeywordValuesForLocale(const char* key, const Locale& locale,
                                    boolean commonlyUsed, UErrorCode& status) {
    // This is a wrapper over ucol_getKeywordValuesForLocale
    UEnumeration *uenum = ucol_getKeywordValuesForLocale(key, locale.getName(),
                                                        commonlyUsed, &status);
    if (U_FAILURE(status)) {
        uenum_close(uenum);
        return null;
    }
    return new UStringEnumeration(uenum);
}

Locale U_EXPORT2
Collator.getFunctionalEquivalent(const char* keyword, const Locale& locale,
                                  boolean& isAvailable, UErrorCode& status) {
    // This is a wrapper over ucol_getFunctionalEquivalent
    char loc[ULOC_FULLNAME_CAPACITY];
    /*int32_t len =*/ ucol_getFunctionalEquivalent(loc, sizeof(loc),
                    keyword, locale.getName(), &isAvailable, &status);
    if (U_FAILURE(status)) {
        *loc = 0; // root
    }
    return Locale.createFromName(loc);
}

Collator.ECollationStrength
Collator.getStrength() {
    UErrorCode intStatus = U_ZERO_ERROR;
    return (ECollationStrength)getAttribute(UCOL_STRENGTH, intStatus);
}

void
Collator.setStrength(ECollationStrength newStrength) {
    UErrorCode intStatus = U_ZERO_ERROR;
    setAttribute(UCOL_STRENGTH, (UColAttributeValue)newStrength, intStatus);
}

Collator &
Collator.setMaxVariable(UColReorderCode /*group*/) {
    if (U_SUCCESS) {
        errorCode = U_UNSUPPORTED_ERROR;
    }
    return *this;
}

UColReorderCode
Collator.getMaxVariable() {
    return Collator.ReorderCodes.PUNCTUATION;
}

int32_t
Collator.getReorderCodes(int32_t* /* dest*/,
                          int /* destCapacity*/,
                          UErrorCode& status) const
{
    if (U_SUCCESS(status)) {
        status = U_UNSUPPORTED_ERROR;
    }
    return 0;
}

void
Collator.setReorderCodes(const int* /* reorderCodes */,
                          int /* reorderCodesLength */,
                          UErrorCode& status)
{
    if (U_SUCCESS(status)) {
        status = U_UNSUPPORTED_ERROR;
    }
}

int32_t
Collator.getEquivalentReorderCodes(int reorderCode,
                                    int *dest, int capacity,
                                    ) {
    if(U_FAILURE) { return 0; }
    if(capacity < 0 || (dest == null && capacity > 0)) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return 0;
    }
    CollationData baseData = CollationRoot.getData;
    if(U_FAILURE) { return 0; }
    return baseData.getEquivalentScripts(reorderCode, dest, capacity);
}

int32_t
Collator.internalGetShortDefinitionString(const char * /*locale*/,
                                                             char * /*buffer*/,
                                                             int /*capacity*/,
                                                             UErrorCode &status) {
  if(U_SUCCESS(status)) {
    status = U_UNSUPPORTED_ERROR; /* Shouldn't happen, internal function */
  }
  return 0;
}

UCollationResult
Collator.internalCompareUTF8(const char *left, int leftLength,
                              const char *right, int rightLength,
                              ) {
    if(U_FAILURE) { return UCOL_EQUAL; }
    if((left == null && leftLength != 0) || (right == null && rightLength != 0)) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return UCOL_EQUAL;
    }
    return compareUTF8(
            StringPiece(left, (leftLength < 0) ? uprv_strlen(left) : leftLength),
            StringPiece(right, (rightLength < 0) ? uprv_strlen(right) : rightLength),
            errorCode);
}

int32_t
Collator.internalNextSortKeyPart(UCharIterator * /*iter*/, uint32_t /*state*/[2],
                                  uint8_t * /*dest*/, int /*count*/) {
    if (U_SUCCESS) {
        errorCode = U_UNSUPPORTED_ERROR;
    }
    return 0;
}

// UCollator private data members ----------------------------------------

/* This is useless information */
/*const VersionInfo Collator.fVersion = {1, 1, 0, 0};*/

// -------------------------------------

U_NAMESPACE_END

#endif /* #if !UCONFIG_NO_COLLATION */

/* eof */


/*
*******************************************************************************
* Copyright (c) 1996-2013, International Business Machines Corporation and others.
* All Rights Reserved.
*******************************************************************************
*/

#ifndef UCOL_H
#define UCOL_H

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/unorm.h"
#include "unicode/localpointer.h"
#include "unicode/parseerr.h"
#include "unicode/uloc.h"
#include "unicode/uset.h"
#include "unicode/uscript.h"

/**
 * \file
 * \brief C API: Collator 
 *
 * <h2> Collator C API </h2>
 *
 * The C API for Collator performs locale-sensitive
 * string comparison. You use this service to build
 * searching and sorting routines for natural language text.
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
 * @see         UCollationResult
 * @see         UNormalizationMode
 * @see         UCollationStrength
 * @see         UCollationElements
 */

/** A collator.
*  For usage in C programs.
*/
struct UCollator;
/** structure representing a collator object instance 
 * @stable ICU 2.0
 */
typedef struct UCollator UCollator;


/**
 * UCOL_LESS is returned if source string is compared to be less than target
 * string in the ucol_strcoll() method.
 * UCOL_EQUAL is returned if source string is compared to be equal to target
 * string in the ucol_strcoll() method.
 * UCOL_GREATER is returned if source string is compared to be greater than
 * target string in the ucol_strcoll() method.
 * @see ucol_strcoll()
 * <p>
 * Possible values for a comparison result 
 * @stable ICU 2.0
 */
typedef enum {
  /** string a == string b */
  UCOL_EQUAL    = 0,
  /** string a > string b */
  UCOL_GREATER    = 1,
  /** string a < string b */
  UCOL_LESS    = -1
} UCollationResult ;


/** Enum containing attribute values for controling collation behavior.
 * Here are all the allowable values. Not every attribute can take every value. The only
 * universal value is UCOL_DEFAULT, which resets the attribute value to the predefined  
 * value for that locale 
 * @stable ICU 2.0
 */
typedef enum {
  /** accepted by most attributes */
  UCOL_DEFAULT = -1,

  /** Primary collation strength */
  UCOL_PRIMARY = 0,
  /** Secondary collation strength */
  UCOL_SECONDARY = 1,
  /** Tertiary collation strength */
  UCOL_TERTIARY = 2,
  /** Default collation strength */
  UCOL_DEFAULT_STRENGTH = UCOL_TERTIARY,
  UCOL_CE_STRENGTH_LIMIT,
  /** Quaternary collation strength */
  UCOL_QUATERNARY=3,
  /** Identical collation strength */
  UCOL_IDENTICAL=15,
  UCOL_STRENGTH_LIMIT,

  /** Turn the feature off - works for UCOL_FRENCH_COLLATION, 
      UCOL_CASE_LEVEL, UCOL_HIRAGANA_QUATERNARY_MODE
      & UCOL_DECOMPOSITION_MODE*/
  UCOL_OFF = 16,
  /** Turn the feature on - works for UCOL_FRENCH_COLLATION, 
      UCOL_CASE_LEVEL, UCOL_HIRAGANA_QUATERNARY_MODE
      & UCOL_DECOMPOSITION_MODE*/
  UCOL_ON = 17,
  
  /** Valid for UCOL_ALTERNATE_HANDLING. Alternate handling will be shifted */
  UCOL_SHIFTED = 20,
  /** Valid for UCOL_ALTERNATE_HANDLING. Alternate handling will be non ignorable */
  UCOL_NON_IGNORABLE = 21,

  /** Valid for UCOL_CASE_FIRST - 
      lower case sorts before upper case */
  UCOL_LOWER_FIRST = 24,
  /** upper case sorts before lower case */
  UCOL_UPPER_FIRST = 25,

  UCOL_ATTRIBUTE_VALUE_COUNT

} UColAttributeValue;

/**
 * Enum containing the codes for reordering segments of the collation table that are not script
 * codes. These reordering codes are to be used in conjunction with the script codes.
 * @see ucol_getReorderCodes
 * @see ucol_setReorderCodes
 * @see ucol_getEquivalentReorderCodes
 * @see UScriptCode
 * @stable ICU 4.8
 */
 typedef enum {
   /**
    * A special reordering code that is used to specify the default
    * reordering codes for a locale.
    * @stable ICU 4.8
    */   
    Collator.ReorderCodes.DEFAULT       = -1,
   /**
    * A special reordering code that is used to specify no reordering codes.
    * @stable ICU 4.8
    */   
    Collator.ReorderCodes.NONE          = USCRIPT_UNKNOWN,
   /**
    * A special reordering code that is used to specify all other codes used for
    * reordering except for the codes lised as UColReorderCode values and those
    * listed explicitly in a reordering.
    * @stable ICU 4.8
    */   
    Collator.ReorderCodes.OTHERS        = USCRIPT_UNKNOWN,
   /**
    * Characters with the space property.
    * This is equivalent to the rule value "space".
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.SPACE         = 0x1000,
   /**
    * The first entry in the enumeration of reordering groups. This is intended for use in
    * range checking and enumeration of the reorder codes.
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.FIRST         = Collator.ReorderCodes.SPACE,
   /**
    * Characters with the punctuation property.
    * This is equivalent to the rule value "punct".
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.PUNCTUATION   = 0x1001,
   /**
    * Characters with the symbol property.
    * This is equivalent to the rule value "symbol".
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.SYMBOL        = 0x1002,
   /**
    * Characters with the currency property.
    * This is equivalent to the rule value "currency".
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.CURRENCY      = 0x1003,
   /**
    * Characters with the digit property.
    * This is equivalent to the rule value "digit".
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.DIGIT         = 0x1004,
   /**
    * The limit of the reorder codes. This is intended for use in range checking 
    * and enumeration of the reorder codes.
    * @stable ICU 4.8
    */    
    Collator.ReorderCodes.LIMIT         = 0x1005
} UColReorderCode;

/**
 * Base letter represents a primary difference.  Set comparison
 * level to UCOL_PRIMARY to ignore secondary and tertiary differences.
 * Use this to set the strength of a Collator object.
 * Example of primary difference, "abc" &lt; "abd"
 * 
 * Diacritical differences on the same base letter represent a secondary
 * difference.  Set comparison level to UCOL_SECONDARY to ignore tertiary
 * differences. Use this to set the strength of a Collator object.
 * Example of secondary difference, "&auml;" >> "a".
 *
 * Uppercase and lowercase versions of the same character represents a
 * tertiary difference.  Set comparison level to UCOL_TERTIARY to include
 * all comparison differences. Use this to set the strength of a Collator
 * object.
 * Example of tertiary difference, "abc" &lt;&lt;&lt; "ABC".
 *
 * Two characters are considered "identical" when they have the same
 * unicode spellings.  UCOL_IDENTICAL.
 * For example, "&auml;" == "&auml;".
 *
 * UCollationStrength is also used to determine the strength of sort keys 
 * generated from UCollator objects
 * These values can be now found in the UColAttributeValue enum.
 * @stable ICU 2.0
 **/
typedef UColAttributeValue UCollationStrength;

/** Attributes that collation service understands. All the attributes can take UCOL_DEFAULT
 * value, as well as the values specific to each one. 
 * @stable ICU 2.0
 */
typedef enum {
     /** Attribute for direction of secondary weights - used in Canadian French.
      * Acceptable values are UCOL_ON, which results in secondary weights
      * being considered backwards and UCOL_OFF which treats secondary
      * weights in the order they appear.
      * @stable ICU 2.0
      */
     UCOL_FRENCH_COLLATION, 
     /** Attribute for handling variable elements.
      * Acceptable values are UCOL_NON_IGNORABLE (default)
      * which treats all the codepoints with non-ignorable 
      * primary weights in the same way,
      * and UCOL_SHIFTED which causes codepoints with primary 
      * weights that are equal or below the variable top value
      * to be ignored on primary level and moved to the quaternary 
      * level.
      * @stable ICU 2.0
      */
     UCOL_ALTERNATE_HANDLING, 
     /** Controls the ordering of upper and lower case letters.
      * Acceptable values are UCOL_OFF (default), which orders
      * upper and lower case letters in accordance to their tertiary
      * weights, UCOL_UPPER_FIRST which forces upper case letters to 
      * sort before lower case letters, and UCOL_LOWER_FIRST which does 
      * the opposite.
      * @stable ICU 2.0
      */
     UCOL_CASE_FIRST, 
     /** Controls whether an extra case level (positioned before the third
      * level) is generated or not. Acceptable values are UCOL_OFF (default), 
      * when case level is not generated, and UCOL_ON which causes the case
      * level to be generated. Contents of the case level are affected by
      * the value of UCOL_CASE_FIRST attribute. A simple way to ignore 
      * accent differences in a string is to set the strength to UCOL_PRIMARY
      * and enable case level.
      * @stable ICU 2.0
      */
     UCOL_CASE_LEVEL,
     /** Controls whether the normalization check and necessary normalizations
      * are performed. When set to UCOL_OFF (default) no normalization check
      * is performed. The correctness of the result is guaranteed only if the 
      * input data is in so-called FCD form (see users manual for more info).
      * When set to UCOL_ON, an incremental check is performed to see whether
      * the input data is in the FCD form. If the data is not in the FCD form,
      * incremental NFD normalization is performed.
      * @stable ICU 2.0
      */
     UCOL_NORMALIZATION_MODE, 
     /** An alias for UCOL_NORMALIZATION_MODE attribute.
      * @stable ICU 2.0
      */
     UCOL_DECOMPOSITION_MODE = UCOL_NORMALIZATION_MODE,
     /** The strength attribute. Can be either UCOL_PRIMARY, UCOL_SECONDARY,
      * UCOL_TERTIARY, UCOL_QUATERNARY or UCOL_IDENTICAL. The usual strength
      * for most locales (except Japanese) is tertiary. Quaternary strength 
      * is useful when combined with shifted setting for alternate handling
      * attribute and for JIS x 4061 collation, when it is used to distinguish
      * between Katakana  and Hiragana (this is achieved by setting the 
      * UCOL_HIRAGANA_QUATERNARY mode to on. Otherwise, quaternary level
      * is affected only by the number of non ignorable code points in
      * the string. Identical strength is rarely useful, as it amounts 
      * to codepoints of the NFD form of the string.
      * @stable ICU 2.0
      */
     UCOL_STRENGTH,  
#ifndef U_HIDE_DEPRECATED_API
     /** When turned on, this attribute positions Hiragana before all  
      * non-ignorables on quaternary level This is a sneaky way to produce JIS
      * sort order.
      *
      * This attribute is an implementation detail of the CLDR Japanese tailoring.
      * The implementation might change to use a different mechanism
      * to achieve the same Japanese sort order.
      * Since ICU 50, this attribute is not settable any more via API functions.
      * @deprecated ICU 50 Implementation detail, cannot be set via API, might be removed from implementation.
      */
     UCOL_HIRAGANA_QUATERNARY_MODE = UCOL_STRENGTH + 1,
#endif  /* U_HIDE_DEPRECATED_API */
     /** When turned on, this attribute generates a collation key
      * for the numeric value of substrings of digits.
      * This is a way to get '100' to sort AFTER '2'. Note that the longest
      * digit substring that can be treated as a single collation element is
      * 254 digits (not counting leading zeros). If a digit substring is
      * longer than that, the digits beyond the limit will be treated as a
      * separate digit substring associated with a separate collation element.
      * @stable ICU 2.8
      */
     UCOL_NUMERIC_COLLATION = UCOL_STRENGTH + 2, 
     /**
      * The number of UColAttribute constants.
      * @stable ICU 2.0
      */
     UCOL_ATTRIBUTE_COUNT
} UColAttribute;

/** Options for retrieving the rule string 
 *  @stable ICU 2.0
 */
typedef enum {
  /**
   * Retrieves the tailoring rules only.
   * Same as calling the version of getRules() without UColRuleOption.
   * @stable ICU 2.0
   */
  UCOL_TAILORING_ONLY, 
  /**
   * Retrieves the "UCA rules" concatenated with the tailoring rules.
   * The "UCA rules" are an <i>approximation</i> of the root collator's sort order.
   * They are almost never used or useful at runtime and can be removed from the data.
   * See http://userguide.icu-project.org/collation/customization#TOC-Building-on-Existing-Locales
   * @stable ICU 2.0
   */
  UCOL_FULL_RULES 
} UColRuleOption ;

/**
 * Open a UCollator for comparing strings.
 * The UCollator pointer is used in all the calls to the Collation 
 * service. After finished, collator must be disposed of by calling
 * {@link #ucol_close }.
 * @param loc The locale containing the required collation rules. 
 *            Special values for locales can be passed in - 
 *            if null is passed for the locale, the default locale
 *            collation rules will be used. If empty string ("") or
 *            "root" are passed, the root collator will be returned.
 * @param status A pointer to a UErrorCode to receive any errors
 * @return A pointer to a UCollator, or 0 if an error occurred.
 * @see ucol_openRules
 * @see ucol_safeClone
 * @see ucol_close
 * @stable ICU 2.0
 */
U_STABLE UCollator* U_EXPORT2 
ucol_open(const char *loc, UErrorCode *status);

/**
 * Produce a UCollator instance according to the rules supplied.
 * The rules are used to change the default ordering, defined in the
 * UCA in a process called tailoring. The resulting UCollator pointer
 * can be used in the same way as the one obtained by {@link #ucol_strcoll }.
 * @param rules A string describing the collation rules. For the syntax
 *              of the rules please see users guide.
 * @param rulesLength The length of rules, or -1 if null-terminated.
 * @param normalizationMode The normalization mode: One of
 *             UCOL_OFF     (expect the text to not need normalization),
 *             UCOL_ON      (normalize), or
 *             UCOL_DEFAULT (set the mode according to the rules)
 * @param strength The default collation strength; one of UCOL_PRIMARY, UCOL_SECONDARY,
 * UCOL_TERTIARY, UCOL_IDENTICAL,UCOL_DEFAULT_STRENGTH - can be also set in the rules.
 * @param parseError  A pointer to UParseError to recieve information about errors
 *                    occurred during parsing. This argument can currently be set
 *                    to null, but at users own risk. Please provide a real structure.
 * @param status A pointer to a UErrorCode to receive any errors
 * @return A pointer to a UCollator. It is not guaranteed that null be returned in case
 *         of error - please use status argument to check for errors.
 * @see ucol_open
 * @see ucol_safeClone
 * @see ucol_close
 * @stable ICU 2.0
 */
U_STABLE UCollator* U_EXPORT2 
ucol_openRules( const UChar        *rules,
                int            rulesLength,
                UColAttributeValue normalizationMode,
                UCollationStrength strength,
                UParseError        *parseError,
                UErrorCode         *status);

/** 
 * Open a collator defined by a short form string.
 * The structure and the syntax of the string is defined in the "Naming collators"
 * section of the users guide: 
 * http://userguide.icu-project.org/collation/concepts#TOC-Collator-naming-scheme
 * Attributes are overriden by the subsequent attributes. So, for "S2_S3", final
 * strength will be 3. 3066bis locale overrides individual locale parts.
 * The call to this function is equivalent to a call to ucol_open, followed by a 
 * series of calls to ucol_setAttribute and ucol_setVariableTop.
 * @param definition A short string containing a locale and a set of attributes. 
 *                   Attributes not explicitly mentioned are left at the default
 *                   state for a locale.
 * @param parseError if not null, structure that will get filled with error's pre
 *                   and post context in case of error.
 * @param forceDefaults if false, the settings that are the same as the collator 
 *                   default settings will not be applied (for example, setting
 *                   French secondary on a French collator would not be executed). 
 *                   If true, all the settings will be applied regardless of the 
 *                   collator default value. If the definition
 *                   strings are to be cached, should be set to false.
 * @param status     Error code. Apart from regular error conditions connected to 
 *                   instantiating collators (like out of memory or similar), this
 *                   API will return an error if an invalid attribute or attribute/value
 *                   combination is specified.
 * @return           A pointer to a UCollator or 0 if an error occured (including an 
 *                   invalid attribute).
 * @see ucol_open
 * @see ucol_setAttribute
 * @see ucol_setVariableTop
 * @see ucol_getShortDefinitionString
 * @see ucol_normalizeShortDefinitionString
 * @stable ICU 3.0
 *
 */
U_STABLE UCollator* U_EXPORT2
ucol_openFromShortString( const char *definition,
                          boolean forceDefaults,
                          UParseError *parseError,
                          UErrorCode *status);

#ifndef U_HIDE_DEPRECATED_API
/**
 * Get a set containing the contractions defined by the collator. The set includes
 * both the root collator's contractions and the contractions defined by the collator. This set
 * will contain only strings. If a tailoring explicitly suppresses contractions from 
 * the root collator (like Russian), removed contractions will not be in the resulting set.
 * @param coll collator 
 * @param conts the set to hold the result. It gets emptied before
 *              contractions are added. 
 * @param status to hold the error code
 * @return the size of the contraction set
 *
 * @deprecated ICU 3.4, use ucol_getContractionsAndExpansions instead
 */
U_DEPRECATED int U_EXPORT2
ucol_getContractions( const UCollator *coll,
                  USet *conts,
                  UErrorCode *status);
#endif  /* U_HIDE_DEPRECATED_API */

/**
 * Get a set containing the expansions defined by the collator. The set includes
 * both the root collator's expansions and the expansions defined by the tailoring
 * @param coll collator
 * @param contractions if not null, the set to hold the contractions
 * @param expansions if not null, the set to hold the expansions
 * @param addPrefixes add the prefix contextual elements to contractions
 * @param status to hold the error code
 *
 * @stable ICU 3.4
 */
U_STABLE void U_EXPORT2
ucol_getContractionsAndExpansions( const UCollator *coll,
                  USet *contractions, USet *expansions,
                  boolean addPrefixes, UErrorCode *status);

/** 
 * Close a UCollator.
 * Once closed, a UCollator should not be used. Every open collator should
 * be closed. Otherwise, a memory leak will result.
 * @param coll The UCollator to close.
 * @see ucol_open
 * @see ucol_openRules
 * @see ucol_safeClone
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_close(UCollator *coll);

#if U_SHOW_CPLUSPLUS_API

U_NAMESPACE_BEGIN

/**
 * \class LocalUCollatorPointer
 * "Smart pointer" class, closes a UCollator via ucol_close().
 * For most methods see the LocalPointerBase base class.
 *
 * @see LocalPointerBase
 * @see LocalPointer
 * @stable ICU 4.4
 */
U_DEFINE_LOCAL_OPEN_POINTER(LocalUCollatorPointer, UCollator, ucol_close);

U_NAMESPACE_END

#endif

/**
 * Compare two strings.
 * The strings will be compared using the options already specified.
 * @param coll The UCollator containing the comparison rules.
 * @param source The source string.
 * @param sourceLength The length of source, or -1 if null-terminated.
 * @param target The target string.
 * @param targetLength The length of target, or -1 if null-terminated.
 * @return The result of comparing the strings; one of UCOL_EQUAL,
 * UCOL_GREATER, UCOL_LESS
 * @see ucol_greater
 * @see ucol_greaterOrEqual
 * @see ucol_equal
 * @stable ICU 2.0
 */
U_STABLE UCollationResult U_EXPORT2 
ucol_strcoll(    const    UCollator    *coll,
        const    UChar        *source,
        int            sourceLength,
        const    UChar        *target,
        int            targetLength);

/** 
* Compare two strings in UTF-8. 
* The strings will be compared using the options already specified. 
* Note: When input string contains malformed a UTF-8 byte sequence, 
* this function treats these bytes as REPLACEMENT CHARACTER (U+FFFD).
* @param coll The UCollator containing the comparison rules. 
* @param source The source UTF-8 string. 
* @param sourceLength The length of source, or -1 if null-terminated. 
* @param target The target UTF-8 string. 
* @param targetLength The length of target, or -1 if null-terminated. 
* @param status A pointer to a UErrorCode to receive any errors 
* @return The result of comparing the strings; one of UCOL_EQUAL, 
* UCOL_GREATER, UCOL_LESS 
* @see ucol_greater 
* @see ucol_greaterOrEqual 
* @see ucol_equal 
* @stable ICU 50 
*/ 
U_STABLE UCollationResult U_EXPORT2
ucol_strcollUTF8(
        const UCollator *coll,
        const char      *source,
        int         sourceLength,
        const char      *target,
        int         targetLength,
        UErrorCode      *status);

/**
 * Determine if one string is greater than another.
 * This function is equivalent to {@link #ucol_strcoll } == UCOL_GREATER
 * @param coll The UCollator containing the comparison rules.
 * @param source The source string.
 * @param sourceLength The length of source, or -1 if null-terminated.
 * @param target The target string.
 * @param targetLength The length of target, or -1 if null-terminated.
 * @return true if source is greater than target, false otherwise.
 * @see ucol_strcoll
 * @see ucol_greaterOrEqual
 * @see ucol_equal
 * @stable ICU 2.0
 */
U_STABLE boolean U_EXPORT2 
ucol_greater(const UCollator *coll,
             const UChar     *source, int sourceLength,
             const UChar     *target, int targetLength);

/**
 * Determine if one string is greater than or equal to another.
 * This function is equivalent to {@link #ucol_strcoll } != UCOL_LESS
 * @param coll The UCollator containing the comparison rules.
 * @param source The source string.
 * @param sourceLength The length of source, or -1 if null-terminated.
 * @param target The target string.
 * @param targetLength The length of target, or -1 if null-terminated.
 * @return true if source is greater than or equal to target, false otherwise.
 * @see ucol_strcoll
 * @see ucol_greater
 * @see ucol_equal
 * @stable ICU 2.0
 */
U_STABLE boolean U_EXPORT2 
ucol_greaterOrEqual(const UCollator *coll,
                    const UChar     *source, int sourceLength,
                    const UChar     *target, int targetLength);

/**
 * Compare two strings for equality.
 * This function is equivalent to {@link #ucol_strcoll } == UCOL_EQUAL
 * @param coll The UCollator containing the comparison rules.
 * @param source The source string.
 * @param sourceLength The length of source, or -1 if null-terminated.
 * @param target The target string.
 * @param targetLength The length of target, or -1 if null-terminated.
 * @return true if source is equal to target, false otherwise
 * @see ucol_strcoll
 * @see ucol_greater
 * @see ucol_greaterOrEqual
 * @stable ICU 2.0
 */
U_STABLE boolean U_EXPORT2 
ucol_equal(const UCollator *coll,
           const UChar     *source, int sourceLength,
           const UChar     *target, int targetLength);

/**
 * Compare two UTF-8 encoded trings.
 * The strings will be compared using the options already specified.
 * @param coll The UCollator containing the comparison rules.
 * @param sIter The source string iterator.
 * @param tIter The target string iterator.
 * @return The result of comparing the strings; one of UCOL_EQUAL,
 * UCOL_GREATER, UCOL_LESS
 * @param status A pointer to a UErrorCode to receive any errors
 * @see ucol_strcoll
 * @stable ICU 2.6
 */
U_STABLE UCollationResult U_EXPORT2 
ucol_strcollIter(  const    UCollator    *coll,
                  UCharIterator *sIter,
                  UCharIterator *tIter,
                  UErrorCode *status);

/**
 * Get the collation strength used in a UCollator.
 * The strength influences how strings are compared.
 * @param coll The UCollator to query.
 * @return The collation strength; one of UCOL_PRIMARY, UCOL_SECONDARY,
 * UCOL_TERTIARY, UCOL_QUATERNARY, UCOL_IDENTICAL
 * @see ucol_setStrength
 * @stable ICU 2.0
 */
U_STABLE UCollationStrength U_EXPORT2 
ucol_getStrength(const UCollator *coll);

/**
 * Set the collation strength used in a UCollator.
 * The strength influences how strings are compared.
 * @param coll The UCollator to set.
 * @param strength The desired collation strength; one of UCOL_PRIMARY, 
 * UCOL_SECONDARY, UCOL_TERTIARY, UCOL_QUATERNARY, UCOL_IDENTICAL, UCOL_DEFAULT
 * @see ucol_getStrength
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_setStrength(UCollator *coll,
                 UCollationStrength strength);

/**
 * Retrieves the reordering codes for this collator.
 * These reordering codes are a combination of UScript codes and UColReorderCode entries.
 * @param coll The UCollator to query.
 * @param dest The array to fill with the script ordering.
 * @param destCapacity The length of dest. If it is 0, then dest may be null and the function 
 * will only return the length of the result without writing any of the result string (pre-flighting).
 * @param pErrorCode Must be a valid pointer to an error code value, which must not indicate a 
 * failure before the function call.
 * @return The number of reordering codes written to the dest array.
 * @see ucol_setReorderCodes
 * @see ucol_getEquivalentReorderCodes
 * @see UScriptCode
 * @see UColReorderCode
 * @stable ICU 4.8
 */
U_STABLE int U_EXPORT2 
ucol_getReorderCodes(const UCollator* coll,
                    int* dest,
                    int destCapacity,
                    UErrorCode *pErrorCode);
/** 
 * Sets the reordering codes for this collator.
 * Collation reordering allows scripts and some other defined blocks of characters 
 * to be moved relative to each other as a block. This reordering is done on top of 
 * the DUCET/CLDR standard collation order. Reordering can specify groups to be placed 
 * at the start and/or the end of the collation order. These groups are specified using
 * UScript codes and UColReorderCode entries.
 * <p>By default, reordering codes specified for the start of the order are placed in the 
 * order given after a group of "special" non-script blocks. These special groups of characters 
 * are space, punctuation, symbol, currency, and digit. These special groups are represented with
 * UColReorderCode entries. Script groups can be intermingled with 
 * these special non-script blocks if those special blocks are explicitly specified in the reordering.
 * <p>The special code OTHERS stands for any script that is not explicitly 
 * mentioned in the list of reordering codes given. Anything that is after OTHERS
 * will go at the very end of the reordering in the order given.
 * <p>The special reorder code DEFAULT will reset the reordering for this collator
 * to the default for this collator. The default reordering may be the DUCET/CLDR order or may be a reordering that
 * was specified when this collator was created from resource data or from rules. The 
 * DEFAULT code <b>must</b> be the sole code supplied when it used. If not
 * that will result in a U_ILLEGAL_ARGUMENT_ERROR being set.
 * <p>The special reorder code NONE will remove any reordering for this collator.
 * The result of setting no reordering will be to have the DUCET/CLDR ordering used. The 
 * NONE code <b>must</b> be the sole code supplied when it used.
 * @param coll The UCollator to set.
 * @param reorderCodes An array of script codes in the new order. This can be null if the 
 * length is also set to 0. An empty array will clear any reordering codes on the collator.
 * @param reorderCodesLength The length of reorderCodes.
 * @param pErrorCode Must be a valid pointer to an error code value, which must not indicate a
 * failure before the function call.
 * @see ucol_getReorderCodes
 * @see ucol_getEquivalentReorderCodes
 * @see UScriptCode
 * @see UColReorderCode
 * @stable ICU 4.8
 */ 
U_STABLE void U_EXPORT2 
ucol_setReorderCodes(UCollator* coll,
                    const int* reorderCodes,
                    int reorderCodesLength,
                    UErrorCode *pErrorCode);

/**
 * Retrieves the reorder codes that are grouped with the given reorder code. Some reorder
 * codes will be grouped and must reorder together.
 * @param reorderCode The reorder code to determine equivalence for.
 * @param dest The array to fill with the script ordering.
 * @param destCapacity The length of dest. If it is 0, then dest may be null and the function
 * will only return the length of the result without writing any of the result string (pre-flighting).
 * @param pErrorCode Must be a valid pointer to an error code value, which must not indicate 
 * a failure before the function call.
 * @return The number of reordering codes written to the dest array.
 * @see ucol_setReorderCodes
 * @see ucol_getReorderCodes
 * @see UScriptCode
 * @see UColReorderCode
 * @stable ICU 4.8
 */
U_STABLE int U_EXPORT2 
ucol_getEquivalentReorderCodes(int reorderCode,
                    int* dest,
                    int destCapacity,
                    UErrorCode *pErrorCode);

/**
 * Get the display name for a UCollator.
 * The display name is suitable for presentation to a user.
 * @param objLoc The locale of the collator in question.
 * @param dispLoc The locale for display.
 * @param result A pointer to a buffer to receive the attribute.
 * @param resultLength The maximum size of result.
 * @param status A pointer to a UErrorCode to receive any errors
 * @return The total buffer size needed; if greater than resultLength,
 * the output was truncated.
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_getDisplayName(    const    char        *objLoc,
            const    char        *dispLoc,
            UChar             *result,
            int         resultLength,
            UErrorCode        *status);

/**
 * Get a locale for which collation rules are available.
 * A UCollator in a locale returned by this function will perform the correct
 * collation for the locale.
 * @param localeIndex The index of the desired locale.
 * @return A locale for which collation rules are available, or 0 if none.
 * @see ucol_countAvailable
 * @stable ICU 2.0
 */
U_STABLE const char* U_EXPORT2 
ucol_getAvailable(int localeIndex);

/**
 * Determine how many locales have collation rules available.
 * This function is most useful as determining the loop ending condition for
 * calls to {@link #ucol_getAvailable }.
 * @return The number of locales for which collation rules are available.
 * @see ucol_getAvailable
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_countAvailable();

#if !UCONFIG_NO_SERVICE
/**
 * Create a string enumerator of all locales for which a valid
 * collator may be opened.
 * @param status input-output error code
 * @return a string enumeration over locale strings. The caller is
 * responsible for closing the result.
 * @stable ICU 3.0
 */
U_STABLE UEnumeration* U_EXPORT2
ucol_openAvailableLocales(UErrorCode *status);
#endif

/**
 * Create a string enumerator of all possible keywords that are relevant to
 * collation. At this point, the only recognized keyword for this
 * service is "collation".
 * @param status input-output error code
 * @return a string enumeration over locale strings. The caller is
 * responsible for closing the result.
 * @stable ICU 3.0
 */
U_STABLE UEnumeration* U_EXPORT2
ucol_getKeywords(UErrorCode *status);

/**
 * Given a keyword, create a string enumeration of all values
 * for that keyword that are currently in use.
 * @param keyword a particular keyword as enumerated by
 * ucol_getKeywords. If any other keyword is passed in, *status is set
 * to U_ILLEGAL_ARGUMENT_ERROR.
 * @param status input-output error code
 * @return a string enumeration over collation keyword values, or null
 * upon error. The caller is responsible for closing the result.
 * @stable ICU 3.0
 */
U_STABLE UEnumeration* U_EXPORT2
ucol_getKeywordValues(const char *keyword, UErrorCode *status);

/**
 * Given a key and a locale, returns an array of string values in a preferred
 * order that would make a difference. These are all and only those values where
 * the open (creation) of the service with the locale formed from the input locale
 * plus input keyword and that value has different behavior than creation with the
 * input locale alone.
 * @param key           one of the keys supported by this service.  For now, only
 *                      "collation" is supported.
 * @param locale        the locale
 * @param commonlyUsed  if set to true it will return only commonly used values
 *                      with the given locale in preferred order.  Otherwise,
 *                      it will return all the available values for the locale.
 * @param status error status
 * @return a string enumeration over keyword values for the given key and the locale.
 * @stable ICU 4.2
 */
U_STABLE UEnumeration* U_EXPORT2
ucol_getKeywordValuesForLocale(const char* key,
                               const char* locale,
                               boolean commonlyUsed,
                               UErrorCode* status);

/**
 * Return the functionally equivalent locale for the given
 * requested locale, with respect to given keyword, for the
 * collation service.  If two locales return the same result, then
 * collators instantiated for these locales will behave
 * equivalently.  The converse is not always true; two collators
 * may in fact be equivalent, but return different results, due to
 * internal details.  The return result has no other meaning than
 * that stated above, and implies nothing as to the relationship
 * between the two locales.  This is intended for use by
 * applications who wish to cache collators, or otherwise reuse
 * collators when possible.  The functional equivalent may change
 * over time.  For more information, please see the <a
 * href="http://userguide.icu-project.org/locale#TOC-Locales-and-Services">
 * Locales and Services</a> section of the ICU User Guide.
 * @param result fillin for the functionally equivalent locale
 * @param resultCapacity capacity of the fillin buffer
 * @param keyword a particular keyword as enumerated by
 * ucol_getKeywords.
 * @param locale the requested locale
 * @param isAvailable if non-null, pointer to a fillin parameter that
 * indicates whether the requested locale was 'available' to the
 * collation service. A locale is defined as 'available' if it
 * physically exists within the collation locale data.
 * @param status pointer to input-output error code
 * @return the actual buffer size needed for the locale.  If greater
 * than resultCapacity, the returned full name will be truncated and
 * an error code will be returned.
 * @stable ICU 3.0
 */
U_STABLE int U_EXPORT2
ucol_getFunctionalEquivalent(char* result, int resultCapacity,
                             const char* keyword, const char* locale,
                             boolean* isAvailable, UErrorCode* status);

/**
 * Get the collation tailoring rules from a UCollator.
 * The rules will follow the rule syntax.
 * @param coll The UCollator to query.
 * @param length 
 * @return The collation tailoring rules.
 * @stable ICU 2.0
 */
U_STABLE const UChar* U_EXPORT2 
ucol_getRules(    const    UCollator    *coll, 
        int            *length);

/** Get the short definition string for a collator. This API harvests the collator's
 *  locale and the attribute set and produces a string that can be used for opening 
 *  a collator with the same properties using the ucol_openFromShortString API.
 *  This string will be normalized.
 *  The structure and the syntax of the string is defined in the "Naming collators"
 *  section of the users guide: 
 *  http://userguide.icu-project.org/collation/concepts#TOC-Collator-naming-scheme
 *  This API supports preflighting.
 *  @param coll a collator
 *  @param locale a locale that will appear as a collators locale in the resulting
 *                short string definition. If null, the locale will be harvested 
 *                from the collator.
 *  @param buffer space to hold the resulting string
 *  @param capacity capacity of the buffer
 *  @param status for returning errors. All the preflighting errors are featured
 *  @return length of the resulting string
 *  @see ucol_openFromShortString
 *  @see ucol_normalizeShortDefinitionString
 *  @stable ICU 3.0
 */
U_STABLE int U_EXPORT2
ucol_getShortDefinitionString(const UCollator *coll,
                              const char *locale,
                              char *buffer,
                              int capacity,
                              UErrorCode *status);

/** Verifies and normalizes short definition string.
 *  Normalized short definition string has all the option sorted by the argument name,
 *  so that equivalent definition strings are the same. 
 *  This API supports preflighting.
 *  @param source definition string
 *  @param destination space to hold the resulting string
 *  @param capacity capacity of the buffer
 *  @param parseError if not null, structure that will get filled with error's pre
 *                   and post context in case of error.
 *  @param status     Error code. This API will return an error if an invalid attribute 
 *                    or attribute/value combination is specified. All the preflighting 
 *                    errors are also featured
 *  @return length of the resulting normalized string.
 *
 *  @see ucol_openFromShortString
 *  @see ucol_getShortDefinitionString
 * 
 *  @stable ICU 3.0
 */

U_STABLE int U_EXPORT2
ucol_normalizeShortDefinitionString(const char *source,
                                    char *destination,
                                    int capacity,
                                    UParseError *parseError,
                                    UErrorCode *status);


/**
 * Get a sort key for a string from a UCollator.
 * Sort keys may be compared using <TT>strcmp</TT>.
 *
 * Like ICU functions that write to an output buffer, the buffer contents
 * is undefined if the buffer capacity (resultLength parameter) is too small.
 * Unlike ICU functions that write a string to an output buffer,
 * the terminating zero byte is counted in the sort key length.
 * @param coll The UCollator containing the collation rules.
 * @param source The string to transform.
 * @param sourceLength The length of source, or -1 if null-terminated.
 * @param result A pointer to a buffer to receive the attribute.
 * @param resultLength The maximum size of result.
 * @return The size needed to fully store the sort key.
 *      If there was an internal error generating the sort key,
 *      a zero value is returned.
 * @see ucol_keyHashCode
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_getSortKey(const    UCollator    *coll,
        const    UChar        *source,
        int        sourceLength,
        uint8_t        *result,
        int        resultLength);


/** Gets the next count bytes of a sort key. Caller needs
 *  to preserve state array between calls and to provide
 *  the same type of UCharIterator set with the same string.
 *  The destination buffer provided must be big enough to store
 *  the number of requested bytes.
 *
 *  The generated sort key may or may not be compatible with
 *  sort keys generated using ucol_getSortKey().
 *  @param coll The UCollator containing the collation rules.
 *  @param iter UCharIterator containing the string we need 
 *              the sort key to be calculated for.
 *  @param state Opaque state of sortkey iteration.
 *  @param dest Buffer to hold the resulting sortkey part
 *  @param count number of sort key bytes required.
 *  @param status error code indicator.
 *  @return the actual number of bytes of a sortkey. It can be
 *          smaller than count if we have reached the end of 
 *          the sort key.
 *  @stable ICU 2.6
 */
U_STABLE int U_EXPORT2 
ucol_nextSortKeyPart(const UCollator *coll,
                     UCharIterator *iter,
                     uint32_t state[2],
                     uint8_t *dest, int count,
                     UErrorCode *status);

/** enum that is taken by ucol_getBound API 
 * See below for explanation                
 * do not change the values assigned to the 
 * members of this enum. Underlying code    
 * depends on them having these numbers     
 * @stable ICU 2.0
 */
typedef enum {
  /** lower bound */
  UCOL_BOUND_LOWER = 0,
  /** upper bound that will match strings of exact size */
  UCOL_BOUND_UPPER = 1,
  /** upper bound that will match all the strings that have the same initial substring as the given string */
  UCOL_BOUND_UPPER_LONG = 2,
  UCOL_BOUND_VALUE_COUNT
} UColBoundMode;

/**
 * Produce a bound for a given sortkey and a number of levels.
 * Return value is always the number of bytes needed, regardless of 
 * whether the result buffer was big enough or even valid.<br>
 * Resulting bounds can be used to produce a range of strings that are
 * between upper and lower bounds. For example, if bounds are produced
 * for a sortkey of string "smith", strings between upper and lower 
 * bounds with one level would include "Smith", "SMITH", "sMiTh".<br>
 * There are two upper bounds that can be produced. If UCOL_BOUND_UPPER
 * is produced, strings matched would be as above. However, if bound
 * produced using UCOL_BOUND_UPPER_LONG is used, the above example will
 * also match "Smithsonian" and similar.<br>
 * For more on usage, see example in cintltst/capitst.c in procedure
 * TestBounds.
 * Sort keys may be compared using <TT>strcmp</TT>.
 * @param source The source sortkey.
 * @param sourceLength The length of source, or -1 if null-terminated. 
 *                     (If an unmodified sortkey is passed, it is always null 
 *                      terminated).
 * @param boundType Type of bound required. It can be UCOL_BOUND_LOWER, which 
 *                  produces a lower inclusive bound, UCOL_BOUND_UPPER, that 
 *                  produces upper bound that matches strings of the same length 
 *                  or UCOL_BOUND_UPPER_LONG that matches strings that have the 
 *                  same starting substring as the source string.
 * @param noOfLevels  Number of levels required in the resulting bound (for most 
 *                    uses, the recommended value is 1). See users guide for 
 *                    explanation on number of levels a sortkey can have.
 * @param result A pointer to a buffer to receive the resulting sortkey.
 * @param resultLength The maximum size of result.
 * @param status Used for returning error code if something went wrong. If the 
 *               number of levels requested is higher than the number of levels
 *               in the source key, a warning (U_SORT_KEY_TOO_SHORT_WARNING) is 
 *               issued.
 * @return The size needed to fully store the bound. 
 * @see ucol_keyHashCode
 * @stable ICU 2.1
 */
U_STABLE int U_EXPORT2 
ucol_getBound(const uint8_t       *source,
        int             sourceLength,
        UColBoundMode       boundType,
        uint32_t            noOfLevels,
        uint8_t             *result,
        int             resultLength,
        UErrorCode          *status);
        
/**
 * Gets the version information for a Collator. Version is currently
 * an opaque 32-bit number which depends, among other things, on major
 * versions of the collator tailoring and UCA.
 * @param coll The UCollator to query.
 * @param info the version # information, the result will be filled in
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2
ucol_getVersion(const UCollator* coll, VersionInfo info);

/**
 * Gets the UCA version information for a Collator. Version is the
 * UCA version number (3.1.1, 4.0).
 * @param coll The UCollator to query.
 * @param info the version # information, the result will be filled in
 * @stable ICU 2.8
 */
U_STABLE void U_EXPORT2
ucol_getUCAVersion(const UCollator* coll, VersionInfo info);

/**
 * Merges two sort keys. The levels are merged with their corresponding counterparts
 * (primaries with primaries, secondaries with secondaries etc.). Between the values
 * from the same level a separator is inserted.
 *
 * This is useful, for example, for combining sort keys from first and last names
 * to sort such pairs.
 * It is possible to merge multiple sort keys by consecutively merging
 * another one with the intermediate result.
 *
 * The length of the merge result is the sum of the lengths of the input sort keys.
 *
 * Example (uncompressed):
 * <pre>191B1D 01 050505 01 910505 00
 * 1F2123 01 050505 01 910505 00</pre>
 * will be merged as 
 * <pre>191B1D 02 1F2123 01 050505 02 050505 01 910505 02 910505 00</pre>
 *
 * If the destination buffer is not big enough, then its contents are undefined.
 * If any of source lengths are zero or any of the source pointers are null/undefined,
 * the result is of size zero.
 *
 * @param src1 the first sort key
 * @param src1Length the length of the first sort key, including the zero byte at the end;
 *        can be -1 if the function is to find the length
 * @param src2 the second sort key
 * @param src2Length the length of the second sort key, including the zero byte at the end;
 *        can be -1 if the function is to find the length
 * @param dest the buffer where the merged sort key is written,
 *        can be null if destCapacity==0
 * @param destCapacity the number of bytes in the dest buffer
 * @return the length of the merged sort key, src1Length+src2Length;
 *         can be larger than destCapacity, or 0 if an error occurs (only for illegal arguments),
 *         in which cases the contents of dest is undefined
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_mergeSortkeys(const uint8_t *src1, int src1Length,
                   const uint8_t *src2, int src2Length,
                   uint8_t *dest, int destCapacity);

/**
 * Universal attribute setter
 * @param coll collator which attributes are to be changed
 * @param attr attribute type 
 * @param value attribute value
 * @param status to indicate whether the operation went on smoothly or there were errors
 * @see UColAttribute
 * @see UColAttributeValue
 * @see ucol_getAttribute
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_setAttribute(UCollator *coll, UColAttribute attr, UColAttributeValue value, UErrorCode *status);

/**
 * Universal attribute getter
 * @param coll collator which attributes are to be changed
 * @param attr attribute type
 * @return attribute value
 * @param status to indicate whether the operation went on smoothly or there were errors
 * @see UColAttribute
 * @see UColAttributeValue
 * @see ucol_setAttribute
 * @stable ICU 2.0
 */
U_STABLE UColAttributeValue  U_EXPORT2 
ucol_getAttribute(const UCollator *coll, UColAttribute attr, UErrorCode *status);

#ifndef U_HIDE_DRAFT_API

/**
 * Sets the variable top to the top of the specified reordering group.
 * The variable top determines the highest-sorting character
 * which is affected by UCOL_ALTERNATE_HANDLING.
 * If that attribute is set to UCOL_NON_IGNORABLE, then the variable top has no effect.
 * @param coll the collator
 * @param group one of Collator.ReorderCodes.SPACE, Collator.ReorderCodes.PUNCTUATION,
 *              Collator.ReorderCodes.SYMBOL, Collator.ReorderCodes.CURRENCY;
 *              or Collator.ReorderCodes.DEFAULT to restore the default max variable group
 * @param pErrorCode Standard ICU error code. Its input value must
 *                   pass the U_SUCCESS() test, or else the function returns
 *                   immediately. Check for U_FAILURE() on output or use with
 *                   function chaining. (See User Guide for details.)
 * @see ucol_getMaxVariable
 * @draft ICU 53
 */
U_DRAFT void U_EXPORT2
ucol_setMaxVariable(UCollator *coll, UColReorderCode group, UErrorCode *pErrorCode);

/**
 * Returns the maximum reordering group whose characters are affected by UCOL_ALTERNATE_HANDLING.
 * @param coll the collator
 * @return the maximum variable reordering group.
 * @see ucol_setMaxVariable
 * @draft ICU 53
 */
U_DRAFT UColReorderCode U_EXPORT2
ucol_getMaxVariable(const UCollator *coll);

#endif  /* U_HIDE_DRAFT_API */

/**
 * Sets the variable top to the primary weight of the specified string.
 *
 * Beginning with ICU 53, the variable top is pinned to
 * the top of one of the supported reordering groups,
 * and it must not be beyond the last of those groups.
 * See ucol_setMaxVariable().
 * @param coll the collator
 * @param varTop one or more (if contraction) UChars to which the variable top should be set
 * @param len length of variable top string. If -1 it is considered to be zero terminated.
 * @param status error code. If error code is set, the return value is undefined.
 *               Errors set by this function are:<br>
 *    U_CE_NOT_FOUND_ERROR if more than one character was passed and there is no such contraction<br>
 *    U_ILLEGAL_ARGUMENT_ERROR if the variable top is beyond
 *    the last reordering group supported by ucol_setMaxVariable()
 * @return variable top primary weight
 * @see ucol_getVariableTop
 * @see ucol_restoreVariableTop
 * @deprecated ICU 53 Call ucol_setMaxVariable() instead.
 */
U_DEPRECATED uint32_t U_EXPORT2 
ucol_setVariableTop(UCollator *coll, 
                    const UChar *varTop, int len, 
                    UErrorCode *status);

/** 
 * Gets the variable top value of a Collator. 
 * @param coll collator which variable top needs to be retrieved
 * @param status error code (not changed by function). If error code is set, 
 *               the return value is undefined.
 * @return the variable top primary weight
 * @see ucol_getMaxVariable
 * @see ucol_setVariableTop
 * @see ucol_restoreVariableTop
 * @stable ICU 2.0
 */
U_STABLE uint32_t U_EXPORT2 ucol_getVariableTop(const UCollator *coll, UErrorCode *status);

/**
 * Sets the variable top to the specified primary weight.
 *
 * Beginning with ICU 53, the variable top is pinned to
 * the top of one of the supported reordering groups,
 * and it must not be beyond the last of those groups.
 * See ucol_setMaxVariable().
 * @param varTop primary weight, as returned by ucol_setVariableTop or ucol_getVariableTop
 * @param status error code
 * @see ucol_getVariableTop
 * @see ucol_setVariableTop
 * @deprecated ICU 53 Call ucol_setMaxVariable() instead.
 */
U_DEPRECATED void U_EXPORT2 
ucol_restoreVariableTop(UCollator *coll, const uint32_t varTop, UErrorCode *status);

/**
 * Thread safe cloning operation. The result is a clone of a given collator.
 * @param coll collator to be cloned
 * @param stackBuffer <em>Deprecated functionality as of ICU 52, use null.</em><br>
 * user allocated space for the new clone. 
 * If null new memory will be allocated. 
 *  If buffer is not large enough, new memory will be allocated.
 *  Clients can use the U_COL_SAFECLONE_BUFFERSIZE.
 * @param pBufferSize <em>Deprecated functionality as of ICU 52, use null or 1.</em><br>
 *  pointer to size of allocated space. 
 *  If *pBufferSize == 0, a sufficient size for use in cloning will 
 *  be returned ('pre-flighting')
 *  If *pBufferSize is not enough for a stack-based safe clone, 
 *  new memory will be allocated.
 * @param status to indicate whether the operation went on smoothly or there were errors
 *    An informational status value, U_SAFECLONE_ALLOCATED_ERROR, is used if any
 * allocations were necessary.
 * @return pointer to the new clone
 * @see ucol_open
 * @see ucol_openRules
 * @see ucol_close
 * @stable ICU 2.0
 */
U_STABLE UCollator* U_EXPORT2 
ucol_safeClone(const UCollator *coll,
               void            *stackBuffer,
               int         *pBufferSize,
               UErrorCode      *status);

#ifndef U_HIDE_DEPRECATED_API

/** default memory size for the new clone.
 * @deprecated ICU 52. Do not rely on ucol_safeClone() cloning into any provided buffer.
 */
#define U_COL_SAFECLONE_BUFFERSIZE 1

#endif /* U_HIDE_DEPRECATED_API */

/**
 * Returns current rules. Delta defines whether full rules are returned or just the tailoring. 
 * Returns number of UChars needed to store rules. If buffer is null or bufferLen is not enough 
 * to store rules, will store up to available space.
 *
 * ucol_getRules() should normally be used instead.
 * See http://userguide.icu-project.org/collation/customization#TOC-Building-on-Existing-Locales
 * @param coll collator to get the rules from
 * @param delta one of UCOL_TAILORING_ONLY, UCOL_FULL_RULES. 
 * @param buffer buffer to store the result in. If null, you'll get no rules.
 * @param bufferLen length of buffer to store rules in. If less than needed you'll get only the part that fits in.
 * @return current rules
 * @stable ICU 2.0
 * @see UCOL_FULL_RULES
 */
U_STABLE int U_EXPORT2 
ucol_getRulesEx(const UCollator *coll, UColRuleOption delta, UChar *buffer, int bufferLen);

#ifndef U_HIDE_DEPRECATED_API
/**
 * gets the locale name of the collator. If the collator
 * is instantiated from the rules, then this function returns
 * null.
 * @param coll The UCollator for which the locale is needed
 * @param type You can choose between requested, valid and actual
 *             locale. For description see the definition of
 *             ULocDataLocaleType in uloc.h
 * @param status error code of the operation
 * @return real locale name from which the collation data comes. 
 *         If the collator was instantiated from rules, returns
 *         null.
 * @deprecated ICU 2.8 Use ucol_getLocaleByType instead
 */
U_DEPRECATED const char * U_EXPORT2
ucol_getLocale(const UCollator *coll, ULocDataLocaleType type, UErrorCode *status);
#endif  /* U_HIDE_DEPRECATED_API */

/**
 * gets the locale name of the collator. If the collator
 * is instantiated from the rules, then this function returns
 * null.
 * @param coll The UCollator for which the locale is needed
 * @param type You can choose between requested, valid and actual
 *             locale. For description see the definition of
 *             ULocDataLocaleType in uloc.h
 * @param status error code of the operation
 * @return real locale name from which the collation data comes. 
 *         If the collator was instantiated from rules, returns
 *         null.
 * @stable ICU 2.8
 */
U_STABLE const char * U_EXPORT2
ucol_getLocaleByType(const UCollator *coll, ULocDataLocaleType type, UErrorCode *status);

/**
 * Get a Unicode set that contains all the characters and sequences tailored in 
 * this collator. The result must be disposed of by using uset_close.
 * @param coll        The UCollator for which we want to get tailored chars
 * @param status      error code of the operation
 * @return a pointer to newly created USet. Must be be disposed by using uset_close
 * @see ucol_openRules
 * @see uset_close
 * @stable ICU 2.4
 */
U_STABLE USet * U_EXPORT2
ucol_getTailoredSet(const UCollator *coll, UErrorCode *status);

#ifndef U_HIDE_INTERNAL_API
/** Calculates the set of unsafe code points, given a collator.
 *   A character is unsafe if you could append any character and cause the ordering to alter significantly.
 *   Collation sorts in normalized order, so anything that rearranges in normalization can cause this.
 *   Thus if you have a character like a_umlaut, and you add a lower_dot to it,
 *   then it normalizes to a_lower_dot + umlaut, and sorts differently.
 *  @param coll Collator
 *  @param unsafe a fill-in set to receive the unsafe points
 *  @param status for catching errors
 *  @return number of elements in the set
 *  @internal ICU 3.0
 */
U_INTERNAL int U_EXPORT2
ucol_getUnsafeSet( const UCollator *coll,
                  USet *unsafe,
                  UErrorCode *status);

/** Touches all resources needed for instantiating a collator from a short string definition,
 *  thus filling up the cache.
 * @param definition A short string containing a locale and a set of attributes. 
 *                   Attributes not explicitly mentioned are left at the default
 *                   state for a locale.
 * @param parseError if not null, structure that will get filled with error's pre
 *                   and post context in case of error.
 * @param forceDefaults if false, the settings that are the same as the collator 
 *                   default settings will not be applied (for example, setting
 *                   French secondary on a French collator would not be executed). 
 *                   If true, all the settings will be applied regardless of the 
 *                   collator default value. If the definition
 *                   strings are to be cached, should be set to false.
 * @param status     Error code. Apart from regular error conditions connected to 
 *                   instantiating collators (like out of memory or similar), this
 *                   API will return an error if an invalid attribute or attribute/value
 *                   combination is specified.
 * @see ucol_openFromShortString
 * @internal ICU 3.2.1
 */
U_INTERNAL void U_EXPORT2
ucol_prepareShortStringOpen( const char *definition,
                          boolean forceDefaults,
                          UParseError *parseError,
                          UErrorCode *status);
#endif  /* U_HIDE_INTERNAL_API */

/** Creates a binary image of a collator. This binary image can be stored and 
 *  later used to instantiate a collator using ucol_openBinary.
 *  This API supports preflighting.
 *  @param coll Collator
 *  @param buffer a fill-in buffer to receive the binary image
 *  @param capacity capacity of the destination buffer
 *  @param status for catching errors
 *  @return size of the image
 *  @see ucol_openBinary
 *  @stable ICU 3.2
 */
U_STABLE int U_EXPORT2
ucol_cloneBinary(const UCollator *coll,
                 uint8_t *buffer, int capacity,
                 UErrorCode *status);

/** Opens a collator from a collator binary image created using
 *  ucol_cloneBinary. Binary image used in instantiation of the 
 *  collator remains owned by the user and should stay around for 
 *  the lifetime of the collator. The API also takes a base collator
 *  which usually should be the root collator.
 *  @param bin binary image owned by the user and required through the
 *             lifetime of the collator
 *  @param length size of the image. If negative, the API will try to
 *                figure out the length of the image
 *  @param base fallback collator, usually the root collator. Base is required to be
 *              present through the lifetime of the collator. Currently 
 *              it cannot be null.
 *  @param status for catching errors
 *  @return newly created collator
 *  @see ucol_cloneBinary
 *  @stable ICU 3.2
 */
U_STABLE UCollator* U_EXPORT2
ucol_openBinary(const uint8_t *bin, int length, 
                const UCollator *base, 
                UErrorCode *status);


#endif /* #if !UCONFIG_NO_COLLATION */

#endif
/*
*******************************************************************************
*   Copyright (C) 1996-2013, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   file name:  ucol.cpp
*   encoding:   US-ASCII
*   tab size:   8 (not used)
*   indentation:4
*
* Modification history
* Date        Name      Comments
* 1996-1999   various members of ICU team maintained C API for collation framework
* 02/16/2001  synwee    Added internal method getPrevSpecialCE
* 03/01/2001  synwee    Added maxexpansion functionality.
* 03/16/2001  weiv      Collation framework is rewritten in C and made UCA compliant
* 2012-2013   markus    Rewritten in C++ again.
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/coll.h"
#include "unicode/tblcoll.h"
#include "unicode/bytestream.h"
#include "unicode/coleitr.h"
#include "unicode/ucoleitr.h"
#include "unicode/ustring.h"
#include "cmemory.h"
#include "collation.h"
#include "cstring.h"
#include "putilimp.h"
#include "uassert.h"
#include "utracimp.h"

U_NAMESPACE_USE

U_CAPI UCollator* U_EXPORT2
ucol_openBinary(const uint8_t *bin, int length,
                const UCollator *base,
                UErrorCode *status)
{
    if(U_FAILURE(*status)) { return null; }
    RuleBasedCollator *coll = new RuleBasedCollator(
            bin, length,
            RuleBasedCollator.rbcFromUCollator(base),
            *status);
    if(coll == null) {
        *status = U_MEMORY_ALLOCATION_ERROR;
        return null;
    }
    if(U_FAILURE(*status)) {
        delete coll;
        return null;
    }
    return coll.toUCollator();
}

U_CAPI int U_EXPORT2
ucol_cloneBinary(const UCollator *coll,
                 uint8_t *buffer, int capacity,
                 UErrorCode *status)
{
    if(U_FAILURE(*status)) {
        return 0;
    }
    const RuleBasedCollator *rbc = RuleBasedCollator.rbcFromUCollator(coll);
    if(rbc == null && coll != null) {
        *status = U_UNSUPPORTED_ERROR;
        return 0;
    }
    return rbc.cloneBinary(buffer, capacity, *status);
}

U_CAPI UCollator* U_EXPORT2
ucol_safeClone(const UCollator *coll, void * /*stackBuffer*/, int * pBufferSize, UErrorCode *status)
{
    if (status == null || U_FAILURE(*status)){
        return null;
    }
    if (coll == null) {
       *status = U_ILLEGAL_ARGUMENT_ERROR;
        return null;
    }
    if (pBufferSize != null) {
        int inputSize = *pBufferSize;
        *pBufferSize = 1;
        if (inputSize == 0) {
            return null;  // preflighting for deprecated functionality
        }
    }
    Collator *newColl = Collator.fromUCollator(coll).clone();
    if (newColl == null) {
        *status = U_MEMORY_ALLOCATION_ERROR;
    } else {
        *status = U_SAFECLONE_ALLOCATED_WARNING;
    }
    return newColl.toUCollator();
}

U_CAPI void U_EXPORT2
ucol_close(UCollator *coll)
{
    UTRACE_ENTRY_OC(UTRACE_UCOL_CLOSE);
    UTRACE_DATA1(UTRACE_INFO, "coll = %p", coll);
    if(coll != null) {
        delete Collator.fromUCollator(coll);
    }
    UTRACE_EXIT();
}

U_CAPI int U_EXPORT2
ucol_mergeSortkeys(const uint8_t *src1, int src1Length,
                   const uint8_t *src2, int src2Length,
                   uint8_t *dest, int destCapacity) {
    /* check arguments */
    if( src1==null || src1Length<-1 || src1Length==0 || (src1Length>0 && src1[src1Length-1]!=0) ||
        src2==null || src2Length<-1 || src2Length==0 || (src2Length>0 && src2[src2Length-1]!=0) ||
        destCapacity<0 || (destCapacity>0 && dest==null)
    ) {
        /* error, attempt to write a zero byte and return 0 */
        if(dest!=null && destCapacity>0) {
            *dest=0;
        }
        return 0;
    }

    /* check lengths and capacity */
    if(src1Length<0) {
        src1Length=(int32_t)uprv_strlen((const char *)src1)+1;
    }
    if(src2Length<0) {
        src2Length=(int32_t)uprv_strlen((const char *)src2)+1;
    }

    int destLength=src1Length+src2Length;
    if(destLength>destCapacity) {
        /* the merged sort key does not fit into the destination */
        return destLength;
    }

    /* merge the sort keys with the same number of levels */
    uint8_t *p=dest;
    for(;;) {
        /* copy level from src1 not including 00 or 01 */
        uint8_t b;
        while((b=*src1)>=2) {
            ++src1;
            *p++=b;
        }

        /* add a 02 merge separator */
        *p++=2;

        /* copy level from src2 not including 00 or 01 */
        while((b=*src2)>=2) {
            ++src2;
            *p++=b;
        }

        /* if both sort keys have another level, then add a 01 level separator and continue */
        if(*src1==1 && *src2==1) {
            ++src1;
            ++src2;
            *p++=1;
        } else {
            break;
        }
    }

    /*
     * here, at least one sort key is finished now, but the other one
     * might have some contents left from containing more levels;
     * that contents is just appended to the result
     */
    if(*src1!=0) {
        /* src1 is not finished, therefore *src2==0, and src1 is appended */
        src2=src1;
    }
    /* append src2, "the other, unfinished sort key" */
    while((*p++=*src2++)!=0) {}

    /* the actual length might be less than destLength if either sort key contained illegally embedded zero bytes */
    return (int32_t)(p-dest);
}

U_CAPI int U_EXPORT2
ucol_getSortKey(const    UCollator    *coll,
        const    UChar        *source,
        int        sourceLength,
        uint8_t        *result,
        int        resultLength)
{
    UTRACE_ENTRY(UTRACE_UCOL_GET_SORTKEY);
    if (UTRACE_LEVEL(UTRACE_VERBOSE)) {
        UTRACE_DATA3(UTRACE_VERBOSE, "coll=%p, source string = %vh ", coll, source,
            ((sourceLength==-1 && source!=null) ? u_strlen(source) : sourceLength));
    }

    int keySize = Collator.fromUCollator(coll).
            getSortKey(source, sourceLength, result, resultLength);

    UTRACE_DATA2(UTRACE_VERBOSE, "Sort Key = %vb", result, keySize);
    UTRACE_EXIT_VALUE(keySize);
    return keySize;
}

U_CAPI int U_EXPORT2
ucol_nextSortKeyPart(const UCollator *coll,
                     UCharIterator *iter,
                     uint32_t state[2],
                     uint8_t *dest, int count,
                     UErrorCode *status)
{
    /* error checking */
    if(status==null || U_FAILURE(*status)) {
        return 0;
    }
    UTRACE_ENTRY(UTRACE_UCOL_NEXTSORTKEYPART);
    UTRACE_DATA6(UTRACE_VERBOSE, "coll=%p, iter=%p, state=%d %d, dest=%p, count=%d",
                  coll, iter, state[0], state[1], dest, count);

    int i = Collator.fromUCollator(coll).
            internalNextSortKeyPart(iter, state, dest, count, *status);

    // Return number of meaningful sortkey bytes.
    UTRACE_DATA4(UTRACE_VERBOSE, "dest = %vb, state=%d %d",
                  dest,i, state[0], state[1]);
    UTRACE_EXIT_VALUE_STATUS(i, *status);
    return i;
}

/**
 * Produce a bound for a given sortkey and a number of levels.
 */
U_CAPI int U_EXPORT2
ucol_getBound(const uint8_t       *source,
        int             sourceLength,
        UColBoundMode       boundType,
        uint32_t            noOfLevels,
        uint8_t             *result,
        int             resultLength,
        UErrorCode          *status)
{
    // consistency checks
    if(status == null || U_FAILURE(*status)) {
        return 0;
    }
    if(source == null) {
        *status = U_ILLEGAL_ARGUMENT_ERROR;
        return 0;
    }

    int sourceIndex = 0;
    // Scan the string until we skip enough of the key OR reach the end of the key
    do {
        sourceIndex++;
        if(source[sourceIndex] == Collation.LEVEL_SEPARATOR_BYTE) {
            noOfLevels--;
        }
    } while (noOfLevels > 0
        && (source[sourceIndex] != 0 || sourceIndex < sourceLength));

    if((source[sourceIndex] == 0 || sourceIndex == sourceLength)
        && noOfLevels > 0) {
            *status = U_SORT_KEY_TOO_SHORT_WARNING;
    }


    // READ ME: this code assumes that the values for boundType
    // enum will not changes. They are set so that the enum value
    // corresponds to the number of extra bytes each bound type
    // needs.
    if(result != null && resultLength >= sourceIndex+boundType) {
        uprv_memcpy(result, source, sourceIndex);
        switch(boundType) {
            // Lower bound just gets terminated. No extra bytes
        case UCOL_BOUND_LOWER: // = 0
            break;
            // Upper bound needs one extra byte
        case UCOL_BOUND_UPPER: // = 1
            result[sourceIndex++] = 2;
            break;
            // Upper long bound needs two extra bytes
        case UCOL_BOUND_UPPER_LONG: // = 2
            result[sourceIndex++] = 0xFF;
            result[sourceIndex++] = 0xFF;
            break;
        default:
            *status = U_ILLEGAL_ARGUMENT_ERROR;
            return 0;
        }
        result[sourceIndex++] = 0;

        return sourceIndex;
    } else {
        return sourceIndex+boundType+1;
    }
}

U_CAPI void U_EXPORT2
ucol_setMaxVariable(UCollator *coll, UColReorderCode group, UErrorCode *pErrorCode) {
    if(U_FAILURE(*pErrorCode)) { return; }
    Collator.fromUCollator(coll).setMaxVariable(group, *pErrorCode);
}

U_CAPI UColReorderCode U_EXPORT2
ucol_getMaxVariable(const UCollator *coll) {
    return Collator.fromUCollator(coll).getMaxVariable();
}

U_CAPI uint32_t  U_EXPORT2
ucol_setVariableTop(UCollator *coll, const UChar *varTop, int len, UErrorCode *status) {
    if(U_FAILURE(*status) || coll == null) {
        return 0;
    }
    return Collator.fromUCollator(coll).setVariableTop(varTop, len, *status);
}

U_CAPI uint32_t U_EXPORT2 ucol_getVariableTop(const UCollator *coll, UErrorCode *status) {
    if(U_FAILURE(*status) || coll == null) {
        return 0;
    }
    return Collator.fromUCollator(coll).getVariableTop(*status);
}

U_CAPI void  U_EXPORT2
ucol_restoreVariableTop(UCollator *coll, const uint32_t varTop, UErrorCode *status) {
    if(U_FAILURE(*status) || coll == null) {
        return;
    }
    Collator.fromUCollator(coll).setVariableTop(varTop, *status);
}

U_CAPI void  U_EXPORT2
ucol_setAttribute(UCollator *coll, UColAttribute attr, UColAttributeValue value, UErrorCode *status) {
    if(U_FAILURE(*status) || coll == null) {
      return;
    }

    Collator.fromUCollator(coll).setAttribute(attr, value, *status);
}

U_CAPI UColAttributeValue  U_EXPORT2
ucol_getAttribute(const UCollator *coll, UColAttribute attr, UErrorCode *status) {
    if(U_FAILURE(*status) || coll == null) {
      return UCOL_DEFAULT;
    }

    return Collator.fromUCollator(coll).getAttribute(attr, *status);
}

U_CAPI void U_EXPORT2
ucol_setStrength(    UCollator                *coll,
            UCollationStrength        strength)
{
    UErrorCode status = U_ZERO_ERROR;
    ucol_setAttribute(coll, UCOL_STRENGTH, strength, &status);
}

U_CAPI UCollationStrength U_EXPORT2
ucol_getStrength(const UCollator *coll)
{
    UErrorCode status = U_ZERO_ERROR;
    return ucol_getAttribute(coll, UCOL_STRENGTH, &status);
}

U_CAPI int U_EXPORT2 
ucol_getReorderCodes(const UCollator *coll,
                    int *dest,
                    int destCapacity,
                    UErrorCode *status) {
    if (U_FAILURE(*status)) {
        return 0;
    }

    return Collator.fromUCollator(coll).getReorderCodes(dest, destCapacity, *status);
}

U_CAPI void U_EXPORT2 
ucol_setReorderCodes(UCollator* coll,
                    const int* reorderCodes,
                    int reorderCodesLength,
                    UErrorCode *status) {
    if (U_FAILURE(*status)) {
        return;
    }

    Collator.fromUCollator(coll).setReorderCodes(reorderCodes, reorderCodesLength, *status);
}

U_CAPI int U_EXPORT2 
ucol_getEquivalentReorderCodes(int reorderCode,
                    int* dest,
                    int destCapacity,
                    UErrorCode *pErrorCode) {
    return Collator.getEquivalentReorderCodes(reorderCode, dest, destCapacity, *pErrorCode);
}

U_CAPI void U_EXPORT2
ucol_getVersion(const UCollator* coll,
                VersionInfo versionInfo)
{
    Collator.fromUCollator(coll).getVersion(versionInfo);
}

U_CAPI UCollationResult U_EXPORT2
ucol_strcollIter( const UCollator    *coll,
                 UCharIterator *sIter,
                 UCharIterator *tIter,
                 UErrorCode         *status)
{
    if(!status || U_FAILURE(*status)) {
        return UCOL_EQUAL;
    }

    UTRACE_ENTRY(UTRACE_UCOL_STRCOLLITER);
    UTRACE_DATA3(UTRACE_VERBOSE, "coll=%p, sIter=%p, tIter=%p", coll, sIter, tIter);

    if(sIter == null || tIter == null || coll == null) {
        *status = U_ILLEGAL_ARGUMENT_ERROR;
        UTRACE_EXIT_VALUE_STATUS(UCOL_EQUAL, *status);
        return UCOL_EQUAL;
    }

    UCollationResult result = Collator.fromUCollator(coll).compare(*sIter, *tIter, *status);

    UTRACE_EXIT_VALUE_STATUS(result, *status);
    return result;
}


/*                                                                      */
/* ucol_strcoll     Main public API string comparison function          */
/*                                                                      */
U_CAPI UCollationResult U_EXPORT2
ucol_strcoll( const UCollator    *coll,
              const UChar        *source,
              int            sourceLength,
              const UChar        *target,
              int            targetLength)
{
    U_ALIGN_CODE(16);

    UTRACE_ENTRY(UTRACE_UCOL_STRCOLL);
    if (UTRACE_LEVEL(UTRACE_VERBOSE)) {
        UTRACE_DATA3(UTRACE_VERBOSE, "coll=%p, source=%p, target=%p", coll, source, target);
        UTRACE_DATA2(UTRACE_VERBOSE, "source string = %vh ", source, sourceLength);
        UTRACE_DATA2(UTRACE_VERBOSE, "target string = %vh ", target, targetLength);
    }

    UErrorCode status = U_ZERO_ERROR;
    UCollationResult returnVal = Collator.fromUCollator(coll).
            compare(source, sourceLength, target, targetLength, status);
    UTRACE_EXIT_VALUE_STATUS(returnVal, status);
    return returnVal;
}

U_CAPI UCollationResult U_EXPORT2
ucol_strcollUTF8(
        const UCollator *coll,
        const char      *source,
        int         sourceLength,
        const char      *target,
        int         targetLength,
        UErrorCode      *status)
{
    U_ALIGN_CODE(16);

    UTRACE_ENTRY(UTRACE_UCOL_STRCOLLUTF8);
    if (UTRACE_LEVEL(UTRACE_VERBOSE)) {
        UTRACE_DATA3(UTRACE_VERBOSE, "coll=%p, source=%p, target=%p", coll, source, target);
        UTRACE_DATA2(UTRACE_VERBOSE, "source string = %vb ", source, sourceLength);
        UTRACE_DATA2(UTRACE_VERBOSE, "target string = %vb ", target, targetLength);
    }

    if (U_FAILURE(*status)) {
        /* do nothing */
        UTRACE_EXIT_VALUE_STATUS(UCOL_EQUAL, *status);
        return UCOL_EQUAL;
    }

    UCollationResult returnVal = Collator.fromUCollator(coll).internalCompareUTF8(
            source, sourceLength, target, targetLength, *status);
    UTRACE_EXIT_VALUE_STATUS(returnVal, *status);
    return returnVal;
}


/* convenience function for comparing strings */
U_CAPI boolean U_EXPORT2
ucol_greater(    const    UCollator        *coll,
        const    UChar            *source,
        int            sourceLength,
        const    UChar            *target,
        int            targetLength)
{
    return (ucol_strcoll(coll, source, sourceLength, target, targetLength)
        == UCOL_GREATER);
}

/* convenience function for comparing strings */
U_CAPI boolean U_EXPORT2
ucol_greaterOrEqual(    const    UCollator    *coll,
            const    UChar        *source,
            int        sourceLength,
            const    UChar        *target,
            int        targetLength)
{
    return (ucol_strcoll(coll, source, sourceLength, target, targetLength)
        != UCOL_LESS);
}

/* convenience function for comparing strings */
U_CAPI boolean U_EXPORT2
ucol_equal(        const    UCollator        *coll,
            const    UChar            *source,
            int            sourceLength,
            const    UChar            *target,
            int            targetLength)
{
    return (ucol_strcoll(coll, source, sourceLength, target, targetLength)
        == UCOL_EQUAL);
}

U_CAPI void U_EXPORT2
ucol_getUCAVersion(const UCollator* coll, VersionInfo info) {
    const Collator *c = Collator.fromUCollator(coll);
    if(c != null) {
        VersionInfo v;
        c.getVersion(v);
        // Note: This is tied to how the current implementation encodes the UCA version
        // in the overall getVersion().
        // Alternatively, we could load the root collator and get at lower-level data from there.
        // Either way, it will reflect the input collator's UCA version only
        // if it is a known implementation.
        // It would be cleaner to make this a virtual Collator method.
        info[0] = v[1] >> 3;
        info[1] = v[1] & 7;
        info[2] = v[2] >> 6;
        info[3] = 0;
    }
}

U_CAPI const UChar * U_EXPORT2
ucol_getRules(const UCollator *coll, int *length) {
    const RuleBasedCollator *rbc = RuleBasedCollator.rbcFromUCollator(coll);
    // OK to crash if coll==null: We do not want to check "this" pointers.
    if(rbc != null || coll == null) {
        const UnicodeString &rules = rbc.getRules();
        assert(rules.getBuffer()[rules.length()] == 0);
        *length = rules.length();
        return rules.getBuffer();
    }
    private static final UChar _NUL = 0;
    *length = 0;
    return &_NUL;
}

U_CAPI int U_EXPORT2
ucol_getRulesEx(const UCollator *coll, UColRuleOption delta, UChar *buffer, int bufferLen) {
    UnicodeString rules;
    const RuleBasedCollator *rbc = RuleBasedCollator.rbcFromUCollator(coll);
    if(rbc != null || coll == null) {
        rbc.getRules(delta, rules);
    }
    if(buffer != null && bufferLen > 0) {
        UErrorCode errorCode = U_ZERO_ERROR;
        return rules.extract(buffer, bufferLen);
    } else {
        return rules.length();
    }
}

U_CAPI const char * U_EXPORT2
ucol_getLocale(const UCollator *coll, ULocDataLocaleType type, UErrorCode *status) {
    return ucol_getLocaleByType(coll, type, status);
}

U_CAPI const char * U_EXPORT2
ucol_getLocaleByType(const UCollator *coll, ULocDataLocaleType type, UErrorCode *status) {
    if(U_FAILURE(*status)) {
        return null;
    }
    UTRACE_ENTRY(UTRACE_UCOL_GETLOCALE);
    UTRACE_DATA1(UTRACE_INFO, "coll=%p", coll);

    const char *result;
    const RuleBasedCollator *rbc = RuleBasedCollator.rbcFromUCollator(coll);
    if(rbc == null && coll != null) {
        *status = U_UNSUPPORTED_ERROR;
        result = null;
    } else {
        result = rbc.internalGetLocaleID(type, *status);
    }

    UTRACE_DATA1(UTRACE_INFO, "result = %s", result);
    UTRACE_EXIT_STATUS(*status);
    return result;
}

U_CAPI USet * U_EXPORT2
ucol_getTailoredSet(const UCollator *coll, UErrorCode *status) {
    if(U_FAILURE(*status)) {
        return null;
    }
    UnicodeSet *set = Collator.fromUCollator(coll).getTailoredSet(*status);
    if(U_FAILURE(*status)) {
        delete set;
        return null;
    }
    return set.toUSet();
}

U_CAPI boolean U_EXPORT2
ucol_equals(const UCollator *source, const UCollator *target) {
    return source == target ||
        (*Collator.fromUCollator(source)) == (*Collator.fromUCollator(target));
}

#endif /* #if !UCONFIG_NO_COLLATION */
