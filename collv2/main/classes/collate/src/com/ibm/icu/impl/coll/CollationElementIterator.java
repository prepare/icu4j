/*
 ******************************************************************************
 *   Copyright (C) 1997-2013, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 ******************************************************************************
 */

/**
 * \file 
 * \brief C++ API: Collation Element Iterator.
 */

/**
* File coleitr.h
*
* Created by: Helena Shih
*
* Modification History:
*
*  Date       Name        Description
*
*  8/18/97    helena      Added internal API documentation.
* 08/03/98    erm         Synched with 1.2 version CollationElementIterator.java
* 12/10/99    aliu        Ported Thai collation support from Java.
* 01/25/01    swquek      Modified into a C++ wrapper calling C APIs (ucoliter.h)
* 02/19/01    swquek      Removed CollationElementsIterator() since it is 
*                         private constructor and no calls are made to it
* 2012-2013   markus      Rewritten in C++ again.
*/

package com.ibm.icu.impl.coll;

/**
* The CollationElementIterator class is used as an iterator to walk through     
* each character of an international string. Use the iterator to return the
* ordering priority of the positioned character. The ordering priority of a 
* character, which we refer to as a key, defines how a character is collated in 
* the given collation object.
* For example, consider the following in Slovak and in traditional Spanish collation:
* <pre>
*        "ca" . the first key is key('c') and second key is key('a').
*        "cha" . the first key is key('ch') and second key is key('a').</pre>
* And in German phonebook collation,
* <pre> \htmlonly       "&#x00E6;b". the first key is key('a'), the second key is key('e'), and
*        the third key is key('b'). \endhtmlonly </pre>
* The key of a character, is an integer composed of primary order(short),
* secondary order(char), and tertiary order(char). Java strictly defines the 
* size and signedness of its primitive data types. Therefore, the static
* functions primaryOrder(), secondaryOrder(), and tertiaryOrder() return 
* int to ensure the correctness of the key value.
* <p>Example of the iterator usage: (without error checking)
* <pre>
* \code
*   void CollationElementIterator_Example()
*   {
*       UnicodeString str = "This is a test";
*       UErrorCode success = U_ZERO_ERROR;
*       RuleBasedCollator* rbc =
*           (RuleBasedCollator*) RuleBasedCollator.createInstance(success);
*       CollationElementIterator* c =
*           rbc.createCollationElementIterator( str );
*       int order = c.next(success);
*       c.reset();
*       order = c.previous(success);
*       delete c;
*       delete rbc;
*   }
* \endcode
* </pre>
* <p>
* The method next() returns the collation order of the next character based on
* the comparison level of the collator. The method previous() returns the
* collation order of the previous character based on the comparison level of
* the collator. The Collation Element Iterator moves only in one direction
* between calls to reset(), setOffset(), or setText(). That is, next() 
* and previous() can not be inter-used. Whenever previous() is to be called after 
* next() or vice versa, reset(), setOffset() or setText() has to be called first
* to reset the status, shifting pointers to either the end or the start of
* the string (reset() or setText()), or the specified position (setOffset()).
* Hence at the next call of next() or previous(), the first or last collation order,
* or collation order at the spefcifieid position will be returned. If a change of
* direction is done without one of these calls, the result is undefined.
* <p>
* The result of a forward iterate (next()) and reversed result of the backward
* iterate (previous()) on the same string are equivalent, if collation orders
* with the value 0 are ignored.
* Character based on the comparison level of the collator.  A collation order 
* consists of primary order, secondary order and tertiary order.  The data 
* type of the collation order is <strong>int32_t</strong>. 
*
* Note, CollationElementIterator should not be subclassed.
* @see     Collator
* @see     RuleBasedCollator
* @version 1.8 Jan 16 2001
*/
class CollationElementIterator {
public: 

    // CollationElementIterator public data member ------------------------------

    enum {
        /**
         * nullORDER indicates that an error has occured while processing
         * @stable ICU 2.0
         */
        nullORDER = (int32_t)0xffffffff
    };

    // CollationElementIterator public constructor/destructor -------------------

    /**
    * Copy constructor.
    *
    * @param other    the object to be copied from
    * @stable ICU 2.0
    */
    CollationElementIterator(const CollationElementIterator& other);

    /** 
    * Destructor
    * @stable ICU 2.0
    */
    virtual ~CollationElementIterator();

    // CollationElementIterator public methods ----------------------------------

    /**
    * Returns true if "other" is the same as "this"
    *
    * @param other    the object to be compared
    * @return         true if "other" is the same as "this"
    * @stable ICU 2.0
    */
    boolean operator==(const CollationElementIterator& other);

    /**
    * Returns true if "other" is not the same as "this".
    *
    * @param other    the object to be compared
    * @return         true if "other" is not the same as "this"
    * @stable ICU 2.0
    */
    boolean operator!=(const CollationElementIterator& other);

    /**
    * Resets the cursor to the beginning of the string.
    * @stable ICU 2.0
    */
    void reset();

    /**
    * Gets the ordering priority of the next character in the string.
    * @param status the error code status.
    * @return the next character's ordering. otherwise returns nullORDER if an 
    *         error has occured or if the end of string has been reached
    * @stable ICU 2.0
    */
    int next(UErrorCode& status);

    /**
    * Get the ordering priority of the previous collation element in the string.
    * @param status the error code status.
    * @return the previous element's ordering. otherwise returns nullORDER if an 
    *         error has occured or if the start of string has been reached
    * @stable ICU 2.0
    */
    int previous(UErrorCode& status);

    /**
    * Gets the primary order of a collation order.
    * @param order the collation order
    * @return the primary order of a collation order.
    * @stable ICU 2.0
    */
    static int primaryOrder(int order);

    /**
    * Gets the secondary order of a collation order.
    * @param order the collation order
    * @return the secondary order of a collation order.
    * @stable ICU 2.0
    */
    static int secondaryOrder(int order);

    /**
    * Gets the tertiary order of a collation order.
    * @param order the collation order
    * @return the tertiary order of a collation order.
    * @stable ICU 2.0
    */
    static int tertiaryOrder(int order);

    /**
    * Return the maximum length of any expansion sequences that end with the 
    * specified comparison order.
    * @param order a collation order returned by previous or next.
    * @return maximum size of the expansion sequences ending with the collation 
    *         element or 1 if collation element does not occur at the end of any 
    *         expansion sequence
    * @stable ICU 2.0
    */
    int getMaxExpansion(int order);

    /**
    * Gets the comparison order in the desired strength. Ignore the other
    * differences.
    * @param order The order value
    * @stable ICU 2.0
    */
    int strengthOrder(int order);

    /**
    * Sets the source string.
    * @param str the source string.
    * @param status the error code status.
    * @stable ICU 2.0
    */
    void setText(const UnicodeString& str, UErrorCode& status);

    /**
    * Sets the source string.
    * @param str the source character iterator.
    * @param status the error code status.
    * @stable ICU 2.0
    */
    void setText(CharacterIterator& str, UErrorCode& status);

    /**
    * Checks if a comparison order is ignorable.
    * @param order the collation order.
    * @return true if a character is ignorable, false otherwise.
    * @stable ICU 2.0
    */
    static boolean isIgnorable(int order);

    /**
    * Gets the offset of the currently processed character in the source string.
    * @return the offset of the character.
    * @stable ICU 2.0
    */
    int getOffset();

    /**
    * Sets the offset of the currently processed character in the source string.
    * @param newOffset the new offset.
    * @param status the error code status.
    * @return the offset of the character.
    * @stable ICU 2.0
    */
    void setOffset(int newOffset, UErrorCode& status);

private:
    friend class RuleBasedCollator;
    friend class UCollationPCE;

    /**
    * CollationElementIterator constructor. This takes the source string and the 
    * collation object. The cursor will walk thru the source string based on the 
    * predefined collation rules. If the source string is empty, nullORDER will 
    * be returned on the calls to next().
    * @param sourceText    the source string.
    * @param order         the collation object.
    * @param status        the error code status.
    */
    CollationElementIterator(const UnicodeString& sourceText,
        const RuleBasedCollator* order, UErrorCode& status);
    // Note: The constructors should take settings & tailoring, not a collator,
    // to avoid circular dependencies.
    // However, for operator==() we would need to be able to compare tailoring data for equality
    // without making CollationData or CollationTailoring depend on TailoredSet.
    // (See the implementation of RuleBasedCollator.operator==().)
    // That might require creating an intermediate class that would be used
    // by both CollationElementIterator and RuleBasedCollator
    // but only contain the part of RBC== related to data and rules.

    /**
    * CollationElementIterator constructor. This takes the source string and the 
    * collation object.  The cursor will walk thru the source string based on the 
    * predefined collation rules.  If the source string is empty, nullORDER will 
    * be returned on the calls to next().
    * @param sourceText    the source string.
    * @param order         the collation object.
    * @param status        the error code status.
    */
    CollationElementIterator(const CharacterIterator& sourceText,
        const RuleBasedCollator* order, UErrorCode& status);

    /**
    * Assignment operator
    *
    * @param other    the object to be copied
    */
    const CollationElementIterator&
        operator=(const CollationElementIterator& other);

    CollationElementIterator(); // default constructor not implemented

    /** Normalizes dir_=1 (just after setOffset()) to dir_=0 (just after reset()). */
    int8_t normalizeDir() { return dir_ == 1 ? 0 : dir_; }

    static UHashtable *computeMaxExpansions(CollationData data);

    static int getMaxExpansion(const UHashtable *maxExpansions, int order);

    // CollationElementIterator private data members ----------------------------

    CollationIterator *iter_;  // owned
    const RuleBasedCollator *rbc_;  // aliased
    int otherHalf_;
    /**
     * <0: backwards; 0: just after reset() (previous() begins from end);
     * 1: just after setOffset(); >1: forward
     */
    int8_t dir_;
    /**
     * Stores offsets from expansions and from unsafe-backwards iteration,
     * so that getOffset() returns intermediate offsets for the CEs
     * that are consistent with forward iteration.
     */
    UVector32 *offsets_;

    UnicodeString string_;
}

    // CollationElementIterator method definitions --------------------------

    int CollationElementIterator.primaryOrder(int order)
    {
        return (order >> 16) & 0xffff;
    }

    int CollationElementIterator.secondaryOrder(int order)
    {
        return (order >> 8) & 0xff;
    }

    int CollationElementIterator.tertiaryOrder(int order)
    {
        return order & 0xff;
    }

    boolean CollationElementIterator.isIgnorable(int order)
    {
        return (order & 0xffff0000) == 0;
    }

    /* CollationElementIterator public constructor/destructor ------------------ */

    CollationElementIterator.CollationElementIterator(
                                            const CollationElementIterator& other) 
            : UObject(other), iter_(null), rbc_(null), otherHalf_(0), dir_(0), offsets_(null) {
        *this = other;
    }

    CollationElementIterator.~CollationElementIterator()
    {
        delete iter_;
        delete offsets_;
    }

    /* CollationElementIterator public methods --------------------------------- */

    namespace {

    int getFirstHalf(long p, int lower32) {
        return ((int)p & 0xffff0000) | ((lower32 >> 16) & 0xff00) | ((lower32 >> 8) & 0xff);
    }
    int getSecondHalf(long p, int lower32) {
        return ((int)p << 16) | ((lower32 >> 8) & 0xff00) | (lower32 & 0x3f);
    }
    boolean ceNeedsTwoParts(long ce) {
        return (ce & 0xffff00ff003fL) != 0;
    }

    }  // namespace

    int32_t CollationElementIterator.getOffset() const
    {
        if (dir_ < 0 && offsets_ != null && !offsets_.isEmpty()) {
            // CollationIterator.previousCE() decrements the CEs length
            // while it pops CEs from its internal buffer.
            int i = iter_.getCEsLength();
            if (otherHalf_ != 0) {
                // Return the trailing CE offset while we are in the middle of a 64-bit CE.
                ++i;
            }
            assert(i < offsets_.size());
            return offsets_.elementAti(i);
        }
        return iter_.getOffset();
    }

    /**
    * Get the ordering priority of the next character in the string.
    * @return the next character's ordering. Returns nullORDER if an error has 
    *         occured or if the end of string has been reached
    */
    int32_t CollationElementIterator.next(UErrorCode& status)
    {
        if (U_FAILURE(status)) { return nullORDER; }
        if (dir_ > 1) {
            // Continue forward iteration. Test this first.
            if (otherHalf_ != 0) {
                int oh = otherHalf_;
                otherHalf_ = 0;
                return oh;
            }
        } else if (dir_ == 1) {
            // next() after setOffset()
            dir_ = 2;
        } else if (dir_ == 0) {
            // The iter_ is already reset to the start of the text.
            dir_ = 2;
        } else /* dir_ < 0 */ {
            // illegal change of direction
            status = U_INVALID_STATE_ERROR;
            return nullORDER;
        }
        // No need to keep all CEs in the buffer when we iterate.
        iter_.clearCEsIfNoneRemaining();
        long ce = iter_.nextCE(status);
        if (ce == Collation.NO_CE) { return nullORDER; }
        // Turn the 64-bit CE into two old-style 32-bit CEs, without quaternary bits.
        long p = ce >>> 32;
        int lower32 = (int)ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf != 0) {
            otherHalf_ = secondHalf | 0xc0;  // continuation CE
        }
        return firstHalf;
    }

    boolean CollationElementIterator.operator!=(
                                      const CollationElementIterator& other) const
    {
        return !(*this == other);
    }

    boolean CollationElementIterator.operator==(
                                        const CollationElementIterator& that) const
    {
        if (this == &that) {
            return true;
        }

        return
            (rbc_ == that.rbc_ || *rbc_ == *that.rbc_) &&
            otherHalf_ == that.otherHalf_ &&
            normalizeDir() == that.normalizeDir() &&
            string_ == that.string_ &&
            *iter_ == *that.iter_;
    }

    /**
    * Get the ordering priority of the previous collation element in the string.
    * @param status the error code status.
    * @return the previous element's ordering. Returns nullORDER if an error has 
    *         occured or if the start of string has been reached.
    */
    int32_t CollationElementIterator.previous(UErrorCode& status)
    {
        if (U_FAILURE(status)) { return nullORDER; }
        if (dir_ < 0) {
            // Continue backwards iteration. Test this first.
            if (otherHalf_ != 0) {
                int oh = otherHalf_;
                otherHalf_ = 0;
                return oh;
            }
        } else if (dir_ == 0) {
            iter_.resetToOffset(string_.length());
            dir_ = -1;
        } else if (dir_ == 1) {
            // previous() after setOffset()
            dir_ = -1;
        } else /* dir_ > 1 */ {
            // illegal change of direction
            status = U_INVALID_STATE_ERROR;
            return nullORDER;
        }
        if (offsets_ == null) {
            offsets_ = new UVector32(status);
            if (offsets_ == null) {
                status = U_MEMORY_ALLOCATION_ERROR;
                return nullORDER;
            }
        }
        // If we already have expansion CEs, then we also have offsets.
        // Otherwise remember the trailing offset in case we need to
        // write offsets for an artificial expansion.
        int limitOffset = iter_.getCEsLength() == 0 ? iter_.getOffset() : 0;
        long ce = iter_.previousCE(*offsets_, status);
        if (ce == Collation.NO_CE) { return nullORDER; }
        // Turn the 64-bit CE into two old-style 32-bit CEs, without quaternary bits.
        long p = ce >>> 32;
        int lower32 = (int)ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf != 0) {
            if (offsets_.isEmpty()) {
                // When we convert a single 64-bit CE into two 32-bit CEs,
                // we need to make this artificial expansion behave like a normal expansion.
                // See CollationIterator.previousCE().
                offsets_.addElement(iter_.getOffset(), status);
                offsets_.addElement(limitOffset, status);
            }
            otherHalf_ = firstHalf;
            return secondHalf | 0xc0;  // continuation CE
        }
        return firstHalf;
    }

    /**
    * Resets the cursor to the beginning of the string.
    */
    void CollationElementIterator.reset()
    {
        iter_ .resetToOffset(0);
        otherHalf_ = 0;
        dir_ = 0;
    }

    void CollationElementIterator.setOffset(int newOffset, 
                                            UErrorCode& status)
    {
        if (U_FAILURE(status)) { return; }
        iter_.resetToOffset(newOffset);
        otherHalf_ = 0;
        dir_ = 1;
    }

    /**
    * Sets the source to the new source string.
    */
    void CollationElementIterator.setText(const UnicodeString& source,
                                          UErrorCode& status)
    {
        if (U_FAILURE(status)) {
            return;
        }

        string_ = source;
        const UChar *s = string_.getBuffer();
        CollationIterator *newIter;
        boolean numeric = rbc_.settings.isNumeric();
        if (rbc_.settings.dontCheckFCD()) {
            newIter = new UTF16CollationIterator(rbc_.data, numeric, s, s, s + string_.length());
        } else {
            newIter = new FCDUTF16CollationIterator(rbc_.data, numeric, s, s, s + string_.length());
        }
        if (newIter == null) {
            status = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        delete iter_;
        iter_ = newIter;
        otherHalf_ = 0;
        dir_ = 0;
    }

    // Sets the source to the new character iterator.
    void CollationElementIterator.setText(CharacterIterator& source, 
                                          UErrorCode& status)
    {
        if (U_FAILURE(status)) 
            return;

        source.getText(string_);
        setText(string_, status);
    }

    int32_t CollationElementIterator.strengthOrder(int order) const
    {
        UColAttributeValue s = (UColAttributeValue)rbc_.settings.getStrength();
        // Mask off the unwanted differences.
        if (s == UCOL_PRIMARY) {
            order &= 0xffff0000;
        }
        else if (s == UCOL_SECONDARY) {
            order &= 0xffffff00;
        }

        return order;
    }

    /* CollationElementIterator private constructors/destructors --------------- */

    /** 
    * This is the "real" constructor for this class; it constructs an iterator
    * over the source text using the specified collator
    */
    CollationElementIterator.CollationElementIterator(
                                                  const UnicodeString &source,
                                                  const RuleBasedCollator *coll,
                                                  UErrorCode &status)
            : iter_(null), rbc_(coll), otherHalf_(0), dir_(0), offsets_(null) {
        setText(source, status);
    }

    /** 
    * This is the "real" constructor for this class; it constructs an iterator over 
    * the source text using the specified collator
    */
    CollationElementIterator.CollationElementIterator(
                                              const CharacterIterator &source,
                                              const RuleBasedCollator *coll,
                                              UErrorCode &status)
            : iter_(null), rbc_(coll), otherHalf_(0), dir_(0), offsets_(null) {
        // We only call source.getText() which should be const anyway.
        setText(const_cast<CharacterIterator &>(source), status);
    }

    /* CollationElementIterator private methods -------------------------------- */

    const CollationElementIterator& CollationElementIterator.operator=(
                                            const CollationElementIterator& other)
    {
        if (this == &other) {
            return *this;
        }

        CollationIterator *newIter;
        const FCDUTF16CollationIterator *otherFCDIter =
                dynamic_cast<const FCDUTF16CollationIterator *>(other.iter_);
        if(otherFCDIter != null) {
            newIter = new FCDUTF16CollationIterator(*otherFCDIter, string_.getBuffer());
        } else {
            const UTF16CollationIterator *otherIter =
                    dynamic_cast<const UTF16CollationIterator *>(other.iter_);
            if(otherIter != null) {
                newIter = new UTF16CollationIterator(*otherIter, string_.getBuffer());
            } else {
                newIter = null;
            }
        }
        if(newIter != null) {
            delete iter_;
            iter_ = newIter;
            rbc_ = other.rbc_;
            otherHalf_ = other.otherHalf_;
            dir_ = other.dir_;

            string_ = other.string_;
        }
        if(other.dir_ < 0 && other.offsets_ != null && !other.offsets_.isEmpty()) {
            UErrorCode errorCode = U_ZERO_ERROR;
            if(offsets_ == null) {
                offsets_ = new UVector32(other.offsets_.size());
            }
            if(offsets_ != null) {
                offsets_.assign(*other.offsets_);
            }
        }
        return *this;
    }

    namespace {

    class MaxExpSink implements ContractionsAndExpansions.CESink {
    public:
        MaxExpSink(UHashtable *h, UErrorCode &ec) : maxExpansions(h)(ec) {}
        virtual ~MaxExpSink();
        virtual void handleCE(long /*ce*/) {}
        virtual void handleExpansion(const long ces[], int length) {
            if (length <= 1) {
                // We do not need to add single CEs into the map.
                return;
            }
            int count = 0;  // number of CE "halves"
            for (int i = 0; i < length; ++i) {
                count += ceNeedsTwoParts(ces[i]) ? 2 : 1;
            }
            // last "half" of the last CE
            long ce = ces[length - 1];
            long p = ce >>> 32;
            int lower32 = (int)ce;
            int lastHalf = getSecondHalf(p, lower32);
            if (lastHalf == 0) {
                lastHalf = getFirstHalf(p, lower32);
                assert(lastHalf != 0);
            } else {
                lastHalf |= 0xc0;  // old-style continuation CE
            }
            if (count > uhash_igeti(maxExpansions, (int32_t)lastHalf)) {
                uhash_iputi(maxExpansions, (int32_t)lastHalf, count, &errorCode);
            }
        }

    private:
        UHashtable *maxExpansions;
        ;
    };

    MaxExpSink.~MaxExpSink() {}

    }  // namespace

    UHashtable *
    CollationElementIterator.computeMaxExpansions(CollationData data) {
        if (U_FAILURE) { return null; }
        UHashtable *maxExpansions = uhash_open(uhash_hashLong, uhash_compareLong,
                                              uhash_compareLong, &errorCode);
        if (U_FAILURE) { return null; }
        MaxExpSink sink(maxExpansions);
        ContractionsAndExpansions(null, null, &sink, true).forData(data);
        if (U_FAILURE) {
            uhash_close(maxExpansions);
            return null;
        }
        return maxExpansions;
    }

    int32_t
    CollationElementIterator.getMaxExpansion(int order) {
        return getMaxExpansion(rbc_.tailoring.maxExpansions, order);
    }

    int32_t
    CollationElementIterator.getMaxExpansion(const UHashtable *maxExpansions, int order) {
        if (order == 0) { return 1; }
        int max;
        if(maxExpansions != null && (max = uhash_igeti(maxExpansions, order)) != 0) {
            return max;
        }
        if ((order & 0xc0) == 0xc0) {
            // old-style continuation CE
            return 2;
        } else {
            return 1;
        }
    }

    /**  
    * This indicates an error has occured during processing or if no more CEs is 
    * to be returned.
    * @stable ICU 2.0
    */
    #define UCOL_nullORDER        ((int32_t)0xFFFFFFFF)

    /** 
    * The UCollationElements struct.
    * For usage in C programs.
    * @stable ICU 2.0
    */
    typedef struct UCollationElements UCollationElements;

/**
 * \file
 * \brief C API: UCollationElements
 *
 * The UCollationElements API is used as an iterator to walk through each 
 * character of an international string. Use the iterator to return the
 * ordering priority of the positioned character. The ordering priority of a 
 * character, which we refer to as a key, defines how a character is collated 
 * in the given collation object.
 * For example, consider the following in Slovak and in traditional Spanish collation:
 * <pre>
 * .       "ca" . the first key is key('c') and second key is key('a').
 * .       "cha" . the first key is key('ch') and second key is key('a').
 * </pre>
 * And in German phonebook collation,
 * <pre>
 * .       "<ae ligature>b". the first key is key('a'), the second key is key('e'), and
 * .       the third key is key('b').
 * </pre>
 * <p>Example of the iterator usage: (without error checking)
 * <pre>
 * .  void CollationElementIterator_Example()
 * .  {
 * .      UChar *s;
 * .      t_int32 order, primaryOrder;
 * .      UCollationElements *c;
 * .      UCollatorOld *coll;
 * .      UErrorCode success = U_ZERO_ERROR;
 * .      s=(UChar*)malloc(sizeof(UChar) * (strlen("This is a test")+1) );
 * .      u_uastrcpy(s, "This is a test");
 * .      coll = ucol_open(null, &success);
 * .      c = ucol_openElements(coll, str, u_strlen(str), &status);
 * .      order = ucol_next(c, &success);
 * .      ucol_reset(c);
 * .      order = ucol_prev(c, &success);
 * .      free(s);
 * .      ucol_close(coll);
 * .      ucol_closeElements(c);
 * .  }
 * </pre>
 * <p>
 * ucol_next() returns the collation order of the next.
 * ucol_prev() returns the collation order of the previous character.
 * The Collation Element Iterator moves only in one direction between calls to
 * ucol_reset. That is, ucol_next() and ucol_prev can not be inter-used. 
 * Whenever ucol_prev is to be called after ucol_next() or vice versa, 
 * ucol_reset has to be called first to reset the status, shifting pointers to 
 * either the end or the start of the string. Hence at the next call of 
 * ucol_prev or ucol_next, the first or last collation order will be returned. 
 * If a change of direction is done without a ucol_reset, the result is 
 * undefined.
 * The result of a forward iterate (ucol_next) and reversed result of the  
 * backward iterate (ucol_prev) on the same string are equivalent, if 
 * collation orders with the value 0 are ignored.
 * Character based on the comparison level of the collator.  A collation order 
 * consists of primary order, secondary order and tertiary order.  The data 
 * type of the collation order is <strong>int32_t</strong>. 
 *
 * @see UCollator
 */

/**
 * Open the collation elements for a string.
 *
 * @param coll The collator containing the desired collation rules.
 * @param text The text to iterate over.
 * @param textLength The number of characters in text, or -1 if null-terminated
 * @param status A pointer to a UErrorCode to receive any errors.
 * @return a struct containing collation element information
 * @stable ICU 2.0
 */
U_STABLE UCollationElements* U_EXPORT2 
ucol_openElements(const UCollator  *coll,
                  const UChar      *text,
                        int    textLength,
                        UErrorCode *status);


/**
 * get a hash code for a key... Not very useful!
 * @param key    the given key.
 * @param length the size of the key array.
 * @return       the hash code.
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_keyHashCode(const uint8_t* key, int length);

/**
 * Close a UCollationElements.
 * Once closed, a UCollationElements may no longer be used.
 * @param elems The UCollationElements to close.
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_closeElements(UCollationElements *elems);

/**
 * Reset the collation elements to their initial state.
 * This will move the 'cursor' to the beginning of the text.
 * Property settings for collation will be reset to the current status.
 * @param elems The UCollationElements to reset.
 * @see ucol_next
 * @see ucol_previous
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_reset(UCollationElements *elems);

/**
 * Get the ordering priority of the next collation element in the text.
 * A single character may contain more than one collation element.
 * @param elems The UCollationElements containing the text.
 * @param status A pointer to a UErrorCode to receive any errors.
 * @return The next collation elements ordering, otherwise returns nullORDER 
 *         if an error has occured or if the end of string has been reached
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_next(UCollationElements *elems, UErrorCode *status);

/**
 * Get the ordering priority of the previous collation element in the text.
 * A single character may contain more than one collation element.
 * Note that internally a stack is used to store buffered collation elements. 
 * @param elems The UCollationElements containing the text.
 * @param status A pointer to a UErrorCode to receive any errors. Noteably 
 *               a U_BUFFER_OVERFLOW_ERROR is returned if the internal stack
 *               buffer has been exhausted.
 * @return The previous collation elements ordering, otherwise returns 
 *         nullORDER if an error has occured or if the start of string has 
 *         been reached.
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_previous(UCollationElements *elems, UErrorCode *status);

/**
 * Get the maximum length of any expansion sequences that end with the 
 * specified comparison order.
 * This is useful for .... ?
 * @param elems The UCollationElements containing the text.
 * @param order A collation order returned by previous or next.
 * @return maximum size of the expansion sequences ending with the collation 
 *         element or 1 if collation element does not occur at the end of any 
 *         expansion sequence
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_getMaxExpansion(const UCollationElements *elems, int order);

/**
 * Set the text containing the collation elements.
 * Property settings for collation will remain the same.
 * In order to reset the iterator to the current collation property settings,
 * the API reset() has to be called.
 * @param elems The UCollationElements to set.
 * @param text The source text containing the collation elements.
 * @param textLength The length of text, or -1 if null-terminated.
 * @param status A pointer to a UErrorCode to receive any errors.
 * @see ucol_getText
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_setText(      UCollationElements *elems, 
             const UChar              *text,
                   int            textLength,
                   UErrorCode         *status);

/**
 * Get the offset of the current source character.
 * This is an offset into the text of the character containing the current
 * collation elements.
 * @param elems The UCollationElements to query.
 * @return The offset of the current source character.
 * @see ucol_setOffset
 * @stable ICU 2.0
 */
U_STABLE int U_EXPORT2 
ucol_getOffset(const UCollationElements *elems);

/**
 * Set the offset of the current source character.
 * This is an offset into the text of the character to be processed.
 * Property settings for collation will remain the same.
 * In order to reset the iterator to the current collation property settings,
 * the API reset() has to be called.
 * @param elems The UCollationElements to set.
 * @param offset The desired character offset.
 * @param status A pointer to a UErrorCode to receive any errors.
 * @see ucol_getOffset
 * @stable ICU 2.0
 */
U_STABLE void U_EXPORT2 
ucol_setOffset(UCollationElements *elems,
               int        offset,
               UErrorCode         *status);

/**
* Get the primary order of a collation order.
* @param order the collation order
* @return the primary order of a collation order.
* @stable ICU 2.6
*/
U_STABLE int U_EXPORT2
ucol_primaryOrder (int order); 

/**
* Get the secondary order of a collation order.
* @param order the collation order
* @return the secondary order of a collation order.
* @stable ICU 2.6
*/
U_STABLE int U_EXPORT2
ucol_secondaryOrder (int order); 

/**
* Get the tertiary order of a collation order.
* @param order the collation order
* @return the tertiary order of a collation order.
* @stable ICU 2.6
*/
U_STABLE int U_EXPORT2
ucol_tertiaryOrder (int order); 

#define BUFFER_LENGTH             100

#define DEFAULT_BUFFER_SIZE 16
#define BUFFER_GROW 8

#define ARRAY_SIZE(array) (sizeof array / sizeof array[0])

#define ARRAY_COPY(dst, src, count) uprv_memcpy((void *) (dst), (void *) (src), (count) * sizeof (src)[0])

#define NEW_ARRAY(type, count) (type *) uprv_malloc((count) * sizeof(type))

#define GROW_ARRAY(array, newSize) uprv_realloc((void *) (array), (newSize) * sizeof (array)[0])

#define DELETE_ARRAY(array) uprv_free((void *) (array))

struct RCEI
{
    int ce;
    int  low;
    int  high;
};

U_NAMESPACE_BEGIN

struct RCEBuffer
{
    RCEI    defaultBuffer[DEFAULT_BUFFER_SIZE];
    RCEI   *buffer;
    int bufferIndex;
    int bufferSize;

    RCEBuffer();
    ~RCEBuffer();

    boolean empty();
    void  put(int ce, int ixLow, int ixHigh);
    const RCEI *get();
};

RCEBuffer.RCEBuffer()
{
    buffer = defaultBuffer;
    bufferIndex = 0;
    bufferSize = LENGTHOF(defaultBuffer);
}

RCEBuffer.~RCEBuffer()
{
    if (buffer != defaultBuffer) {
        DELETE_ARRAY(buffer);
    }
}

boolean RCEBuffer.empty() const
{
    return bufferIndex <= 0;
}

void RCEBuffer.put(int ce, int ixLow, int ixHigh)
{
    if (bufferIndex >= bufferSize) {
        RCEI *newBuffer = NEW_ARRAY(RCEI, bufferSize + BUFFER_GROW);

        ARRAY_COPY(newBuffer, buffer, bufferSize);

        if (buffer != defaultBuffer) {
            DELETE_ARRAY(buffer);
        }

        buffer = newBuffer;
        bufferSize += BUFFER_GROW;
    }

    buffer[bufferIndex].ce   = ce;
    buffer[bufferIndex].low  = ixLow;
    buffer[bufferIndex].high = ixHigh;

    bufferIndex += 1;
}

const RCEI *RCEBuffer.get()
{
    if (bufferIndex > 0) {
     return &buffer[--bufferIndex];
    }

    return null;
}

PCEBuffer.PCEBuffer()
{
    buffer = defaultBuffer;
    bufferIndex = 0;
    bufferSize = LENGTHOF(defaultBuffer);
}

PCEBuffer.~PCEBuffer()
{
    if (buffer != defaultBuffer) {
        DELETE_ARRAY(buffer);
    }
}

void PCEBuffer.reset()
{
    bufferIndex = 0;
}

boolean PCEBuffer.empty() const
{
    return bufferIndex <= 0;
}

void PCEBuffer.put(long ce, int ixLow, int ixHigh)
{
    if (bufferIndex >= bufferSize) {
        PCEI *newBuffer = NEW_ARRAY(PCEI, bufferSize + BUFFER_GROW);

        ARRAY_COPY(newBuffer, buffer, bufferSize);

        if (buffer != defaultBuffer) {
            DELETE_ARRAY(buffer);
        }

        buffer = newBuffer;
        bufferSize += BUFFER_GROW;
    }

    buffer[bufferIndex].ce   = ce;
    buffer[bufferIndex].low  = ixLow;
    buffer[bufferIndex].high = ixHigh;

    bufferIndex += 1;
}

const PCEI *PCEBuffer.get()
{
    if (bufferIndex > 0) {
     return &buffer[--bufferIndex];
    }

    return null;
}

UCollationPCE.UCollationPCE(UCollationElements *elems) { init(elems); }

UCollationPCE.UCollationPCE(CollationElementIterator *iter) { init(iter); }

void UCollationPCE.init(UCollationElements *elems) {
    init(CollationElementIterator.fromUCollationElements(elems));
}

void UCollationPCE.init(CollationElementIterator *iter)
{
    cei = iter;
    init(*iter.rbc_);
}

void UCollationPCE.init(const Collator &coll)
{
    UErrorCode status = U_ZERO_ERROR;

    strength    = coll.getAttribute(UCOL_STRENGTH, status);
    toShift     = coll.getAttribute(UCOL_ALTERNATE_HANDLING, status) == UCOL_SHIFTED;
    isShifted   = false;
    variableTop = coll.getVariableTop(status);
}

UCollationPCE.~UCollationPCE()
{
    // nothing to do
}

long UCollationPCE.processCE(int ce)
{
    long primary = 0, secondary = 0, tertiary = 0, quaternary = 0;

    // This is clean, but somewhat slow...
    // We could apply the mask to ce and then
    // just get all three orders...
    switch(strength) {
    default:
        tertiary = ucol_tertiaryOrder(ce);
        /* note fall-through */

    case UCOL_SECONDARY:
        secondary = ucol_secondaryOrder(ce);
        /* note fall-through */

    case UCOL_PRIMARY:
        primary = ucol_primaryOrder(ce);
    }

    // **** This should probably handle continuations too.  ****
    // **** That means that we need 24 bits for the primary ****
    // **** instead of the 16 that we're currently using.   ****
    // **** So we can lay out the 64 bits as: 24.12.12.16.  ****
    // **** Another complication with continuations is that ****
    // **** the *second* CE is marked as a continuation, so ****
    // **** we always have to peek ahead to know how long   ****
    // **** the primary is...                               ****
    if ((toShift && variableTop > ce && primary != 0)
                || (isShifted && primary == 0)) {

        if (primary == 0) {
            return UCOL_IGNORABLE;
        }

        if (strength >= UCOL_QUATERNARY) {
            quaternary = primary;
        }

        primary = secondary = tertiary = 0;
        isShifted = true;
    } else {
        if (strength >= UCOL_QUATERNARY) {
            quaternary = 0xFFFF;
        }

        isShifted = false;
    }

    return primary << 48 | secondary << 32 | tertiary << 16 | quaternary;
}

U_NAMESPACE_END

/* public methods ---------------------------------------------------- */

U_CAPI UCollationElements* U_EXPORT2
ucol_openElements(const UCollator  *coll,
                  const UChar      *text,
                        int    textLength,
                        UErrorCode *status)
{
    if (U_FAILURE(*status)) {
        return null;
    }
    if (coll == null || (text == null && textLength != 0)) {
        *status = U_ILLEGAL_ARGUMENT_ERROR;
        return null;
    }
    const RuleBasedCollator *rbc = RuleBasedCollator.rbcFromUCollator(coll);
    if (rbc == null) {
        *status = U_UNSUPPORTED_ERROR;  // coll is a Collator but not a RuleBasedCollator
        return null;
    }

    UnicodeString s((boolean)(textLength < 0), text, textLength);
    CollationElementIterator *cei = rbc.createCollationElementIterator(s);
    if (cei == null) {
        *status = U_MEMORY_ALLOCATION_ERROR;
        return null;
    }

    return cei.toUCollationElements();
}


U_CAPI void U_EXPORT2
ucol_closeElements(UCollationElements *elems)
{
    delete CollationElementIterator.fromUCollationElements(elems);
}

U_CAPI void U_EXPORT2
ucol_reset(UCollationElements *elems)
{
    CollationElementIterator.fromUCollationElements(elems).reset();
}

U_CAPI int U_EXPORT2
ucol_next(UCollationElements *elems, 
          UErrorCode         *status)
{
    if (U_FAILURE(*status)) {
        return UCOL_nullORDER;
    }

    return CollationElementIterator.fromUCollationElements(elems).next(*status);
}

U_NAMESPACE_BEGIN

long
UCollationPCE.nextProcessed(
                   int            *ixLow,
                   int            *ixHigh,
                   UErrorCode         *status)
{
    long result = UCOL_IGNORABLE;
    int low = 0, high = 0;

    if (U_FAILURE(*status)) {
        return UCOL_PROCESSED_nullORDER;
    }

    pceBuffer.reset();

    do {
        low = cei.getOffset();
        int ce = cei.next(*status);
        high = cei.getOffset();

        if (ce == UCOL_nullORDER) {
             result = UCOL_PROCESSED_nullORDER;
             break;
        }

        result = processCE(ce);
    } while (result == UCOL_IGNORABLE);

    if (ixLow != null) {
        *ixLow = low;
    }

    if (ixHigh != null) {
        *ixHigh = high;
    }

    return result;
}

U_NAMESPACE_END

U_CAPI int U_EXPORT2
ucol_previous(UCollationElements *elems,
              UErrorCode         *status)
{
    if(U_FAILURE(*status)) {
        return UCOL_nullORDER;
    }
    return CollationElementIterator.fromUCollationElements(elems).previous(*status);
}

U_NAMESPACE_BEGIN

long
UCollationPCE.previousProcessed(
                   int            *ixLow,
                   int            *ixHigh,
                   UErrorCode         *status)
{
    long result = UCOL_IGNORABLE;
    int  low = 0, high = 0;

    if (U_FAILURE(*status)) {
        return UCOL_PROCESSED_nullORDER;
    }

    // pceBuffer.reset();

    while (pceBuffer.empty()) {
        // buffer raw CEs up to non-ignorable primary
        RCEBuffer rceb;
        int ce;
        
        // **** do we need to reset rceb, or will it always be empty at this point ****
        do {
            high = cei.getOffset();
            ce   = cei.previous(*status);
            low  = cei.getOffset();

            if (ce == UCOL_nullORDER) {
                if (! rceb.empty()) {
                    break;
                }

                goto finish;
            }

            rceb.put(ce, low, high);
        } while ((ce & UCOL_PRIMARYORDERMASK) == 0 || isContinuation(ce));

        // process the raw CEs
        while (! rceb.empty()) {
            const RCEI *rcei = rceb.get();

            result = processCE(rcei.ce);

            if (result != UCOL_IGNORABLE) {
                pceBuffer.put(result, rcei.low, rcei.high);
            }
        }
    }

finish:
    if (pceBuffer.empty()) {
        // **** Is -1 the right value for ixLow, ixHigh? ****
        if (ixLow != null) {
                *ixLow = -1;
        }
        
        if (ixHigh != null) {
                *ixHigh = -1
                ;
        }
        return UCOL_PROCESSED_nullORDER;
    }

    const PCEI *pcei = pceBuffer.get();

    if (ixLow != null) {
        *ixLow = pcei.low;
    }

    if (ixHigh != null) {
        *ixHigh = pcei.high;
    }

    return pcei.ce;
}

U_NAMESPACE_END

U_CAPI int U_EXPORT2
ucol_getMaxExpansion(const UCollationElements *elems,
                           int            order)
{
    return CollationElementIterator.fromUCollationElements(elems).getMaxExpansion(order);

    // TODO: The old code masked the order according to strength and then did a binary search.
    // However this was probably at least partially broken because of the following comment.
    // Still, it might have found a match when this version may not.

    // FIXME: with a masked search, there might be more than one hit,
    // so we need to look forward and backward from the match to find all
    // of the hits...
}

U_CAPI void U_EXPORT2
ucol_setText(      UCollationElements *elems,
             const UChar              *text,
                   int            textLength,
                   UErrorCode         *status)
{
    if (U_FAILURE(*status)) {
        return;
    }

    if ((text == null && textLength != 0)) {
        *status = U_ILLEGAL_ARGUMENT_ERROR;
        return;
    }
    UnicodeString s((boolean)(textLength < 0), text, textLength);
    return CollationElementIterator.fromUCollationElements(elems).setText(s, *status);
}

U_CAPI int U_EXPORT2
ucol_getOffset(const UCollationElements *elems)
{
    return CollationElementIterator.fromUCollationElements(elems).getOffset();
}

U_CAPI void U_EXPORT2
ucol_setOffset(UCollationElements    *elems,
               int           offset,
               UErrorCode            *status)
{
    if (U_FAILURE(*status)) {
        return;
    }

    CollationElementIterator.fromUCollationElements(elems).setOffset(offset, *status);
}

U_CAPI int U_EXPORT2
ucol_primaryOrder (int order) 
{
    return (order >> 16) & 0xffff;
}

U_CAPI int U_EXPORT2
ucol_secondaryOrder (int order) 
{
    return (order >> 8) & 0xff;
}

U_CAPI int U_EXPORT2
ucol_tertiaryOrder (int order) 
{
    return order & 0xff;
}
