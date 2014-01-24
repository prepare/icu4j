/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* ContractionsAndExpansions.java, ported from collationsets.h/.cpp
*
* @since 2013feb09
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

final class ContractionsAndExpansions {
public:
    interface CESink {
        void handleCE(long ce);
        void handleExpansion(const long ces[], int length);
    };

    ContractionsAndExpansions(UnicodeSet *con, UnicodeSet *exp, CESink *s, boolean prefixes)
            : data(null), tailoring(null),
              contractions(con), expansions(exp),
              sink(s),
              addPrefixes(prefixes),
              checkTailored(0),
              suffix(null),
              errorCode(U_ZERO_ERROR) {}

    void forData(CollationData d);
    void forCodePoint(CollationData d, int c, UErrorCode &ec);

    // all following: @internal, only public for access by callback

    void handleCE32(int start, int end, int ce32);

    void handlePrefixes(int start, int end, int ce32);
    void handleContractions(int start, int end, int ce32);

    void addExpansions(int start, int end);
    void addStrings(int start, int end, UnicodeSet *set);

    /** Prefixes are reversed in the data structure. */
    void setPrefix(const UnicodeString &pfx) {
        unreversedPrefix = pfx;
        unreversedPrefix.reverse();
    }
    void resetPrefix() {
        unreversedPrefix.remove();
    }

    CollationData data;
    CollationData tailoring;
    UnicodeSet *contractions;
    UnicodeSet *expansions;
    CESink *sink;
    boolean addPrefixes;
    int8_t checkTailored;  // -1: collected tailored  +1: exclude tailored
    UnicodeSet tailored;
    UnicodeSet ranges;
    UnicodeString unreversedPrefix;
    const UnicodeString *suffix;
    long ces[Collation.MAX_EXPANSION_LENGTH];
    UErrorCode errorCode;
}

    ContractionsAndExpansions.CESink.~CESink() {}

    static boolean U_CALLCONV
    enumCnERange(const void *context, int start, int end, int ce32) {
        ContractionsAndExpansions *cne = (ContractionsAndExpansions *)context;
        if(cne.checkTailored == 0) {
            // There is no tailoring.
            // No need to collect nor check the tailored set.
        } else if(cne.checkTailored < 0) {
            // Collect the set of code points with mappings in the tailoring data.
            if(ce32 == Collation.FALLBACK_CE32) {
                return true;  // fallback to base, not tailored
            } else {
                cne.tailored.add(start, end);
            }
            // checkTailored > 0: Exclude tailored ranges from the base data enumeration.
        } else if(start == end) {
            if(cne.tailored.contains(start)) {
                return true;
            }
        } else if(cne.tailored.containsSome(start, end)) {
            cne.ranges.set(start, end).removeAll(cne.tailored);
            int count = cne.ranges.getRangeCount();
            for(int i = 0; i < count; ++i) {
                cne.handleCE32(cne.ranges.getRangeStart(i), cne.ranges.getRangeEnd(i), ce32);
            }
            return U_SUCCESS(cne.errorCode);
        }
        cne.handleCE32(start, end, ce32);
        return U_SUCCESS(cne.errorCode);
    }

    void
    ContractionsAndExpansions.forData(CollationData d, UErrorCode &ec) {
        if(U_FAILURE(ec)) { return; }
        errorCode = ec;  // Preserve info & warning codes.
        // Add all from the data, can be tailoring or base.
        if(d.base != null) {
            checkTailored = -1;
        }
        data = d;
        utrie2_enum(data.trie, null, enumCnERange, this);
        if(d.base == null || U_FAILURE) {
            ec = errorCode;
            return;
        }
        // Add all from the base data but only for un-tailored code points.
        tailored.freeze();
        checkTailored = 1;
        tailoring = d;
        data = d.base;
        utrie2_enum(data.trie, null, enumCnERange, this);
        ec = errorCode;
    }

    void
    ContractionsAndExpansions.forCodePoint(CollationData d, int c, UErrorCode &ec) {
        if(U_FAILURE(ec)) { return; }
        errorCode = ec;  // Preserve info & warning codes.
        int ce32 = d.getCE32(c);
        if(ce32 == Collation.FALLBACK_CE32) {
            d = d.base;
            ce32 = d.getCE32(c);
        }
        data = d;
        handleCE32(c, c, ce32);
        ec = errorCode;
    }

    void
    ContractionsAndExpansions.handleCE32(int start, int end, int ce32) {
        for(;;) {
            if((ce32 & 0xff) < Collation.SPECIAL_CE32_LOW_BYTE) {
                // !isSpecialCE32()
                if(sink != null) {
                    sink.handleCE(Collation.ceFromSimpleCE32(ce32));
                }
                return;
            }
            switch(Collation.tagFromCE32(ce32)) {
            case Collation.FALLBACK_TAG:
                return;
            case Collation.RESERVED_TAG_3:
            case Collation.BUILDER_DATA_TAG:
            case Collation.LEAD_SURROGATE_TAG:
                if(U_SUCCESS) { errorCode = U_INTERNAL_PROGRAM_ERROR; }
                return;
            case Collation.LONG_PRIMARY_TAG:
                if(sink != null) {
                    sink.handleCE(Collation.ceFromLongPrimaryCE32(ce32));
                }
                return;
            case Collation.LONG_SECONDARY_TAG:
                if(sink != null) {
                    sink.handleCE(Collation.ceFromLongSecondaryCE32(ce32));
                }
                return;
            case Collation.LATIN_EXPANSION_TAG:
                if(sink != null) {
                    ces[0] = Collation.latinCE0FromCE32(ce32);
                    ces[1] = Collation.latinCE1FromCE32(ce32);
                    sink.handleExpansion(ces, 2);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if(unreversedPrefix.isEmpty()) {
                    addExpansions(start, end);
                }
                return;
            case Collation.EXPANSION32_TAG:
                if(sink != null) {
                    const uint32_t *ce32s = data.ce32s + Collation.indexFromCE32(ce32);
                    int length = Collation.lengthFromCE32(ce32);
                    for(int i = 0; i < length; ++i) {
                        ces[i] = Collation.ceFromCE32(*ce32s++);
                    }
                    sink.handleExpansion(ces, length);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if(unreversedPrefix.isEmpty()) {
                    addExpansions(start, end);
                }
                return;
            case Collation.EXPANSION_TAG:
                if(sink != null) {
                    int length = Collation.lengthFromCE32(ce32);
                    sink.handleExpansion(data.ces + Collation.indexFromCE32(ce32), length);
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if(unreversedPrefix.isEmpty()) {
                    addExpansions(start, end);
                }
                return;
            case Collation.PREFIX_TAG:
                handlePrefixes(start, end, ce32);
                return;
            case Collation.CONTRACTION_TAG:
                handleContractions(start, end, ce32);
                return;
            case Collation.DIGIT_TAG:
                // Fetch the non-numeric-collation CE32 and continue.
                ce32 = data.ce32s[Collation.indexFromCE32(ce32)];
                break;
            case Collation.U0000_TAG:
                assert(start == 0 && end == 0);
                // Fetch the normal ce32 for U+0000 and continue.
                ce32 = data.ce32s[0];
                break;
            case Collation.HANGUL_TAG:
                if(sink != null) {
                    // TODO: This should be optimized,
                    // especially if [start..end] is the complete Hangul range. (assert that)
                    UTF16CollationIterator iter(data, false, null, null, null);
                    UChar hangul[1] = { 0 };
                    for(int c = start; c <= end; ++c) {
                        hangul[0] = (UChar)c;
                        iter.setText(hangul, hangul + 1);
                        int length = iter.fetchCEs;
                        if(U_FAILURE) { return; }
                        // Ignore the terminating non-CE.
                        assert(length >= 2 && iter.getCE(length - 1) == Collation.NO_CE);
                        sink.handleExpansion(iter.getCEs(), length - 1);
                    }
                }
                // Optimization: If we have a prefix,
                // then the relevant strings have been added already.
                if(unreversedPrefix.isEmpty()) {
                    addExpansions(start, end);
                }
                return;
            case Collation.OFFSET_TAG:
                // Currently no need to send offset CEs to the sink.
                return;
            case Collation.IMPLICIT_TAG:
                // Currently no need to send implicit CEs to the sink.
                return;
            }
        }
    }

    void
    ContractionsAndExpansions.handlePrefixes(
            int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        ce32 = data.getCE32FromContexts(index);  // Default if no prefix match.
        handleCE32(start, end, ce32);
        if(!addPrefixes) { return; }
        UCharsTrie.Iterator prefixes(data.contexts, index + 2, 0);
        while(prefixes.next) {
            setPrefix(prefixes.getString());
            // Prefix/pre-context mappings are special kinds of contractions
            // that always yield expansions.
            addStrings(start, end, contractions);
            addStrings(start, end, expansions);
            handleCE32(start, end, prefixes.getValue());
        }
        resetPrefix();
    }

    void
    ContractionsAndExpansions.handleContractions(
            int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        if((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
            // No match on the single code point.
            // We are underneath a prefix, and the default mapping is just
            // a fallback to the mappings for a shorter prefix.
            assert(!unreversedPrefix.isEmpty());
        } else {
            ce32 = data.getCE32FromContexts(index);  // Default if no suffix match.
            assert(!Collation.isContractionCE32(ce32));
            handleCE32(start, end, ce32);
        }
        UCharsTrie.Iterator suffixes(data.contexts, index + 2, 0);
        while(suffixes.next) {
            suffix = &suffixes.getString();
            addStrings(start, end, contractions);
            if(!unreversedPrefix.isEmpty()) {
                addStrings(start, end, expansions);
            }
            handleCE32(start, end, suffixes.getValue());
        }
        suffix = null;
    }

    void
    ContractionsAndExpansions.addExpansions(int start, int end) {
        if(unreversedPrefix.isEmpty() && suffix == null) {
            if(expansions != null) {
                expansions.add(start, end);
            }
        } else {
            addStrings(start, end, expansions);
        }
    }

    void
    ContractionsAndExpansions.addStrings(int start, int end, UnicodeSet *set) {
        if(set == null) { return; }
        UnicodeString s(unreversedPrefix);
        do {
            s.append(start);
            if(suffix != null) {
                s.append(*suffix);
            }
            set.add(s);
            s.truncate(unreversedPrefix.length());
        } while(++start <= end);
    }
