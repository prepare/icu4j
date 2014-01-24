/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* TailoredSet.java, ported from collationsets.h/.cpp
*
* @since 2013feb09
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

/**
 * Finds the set of characters and strings that sort differently in the tailoring
 * from the base data.
 *
 * Every mapping in the tailoring needs to be compared to the base,
 * because some mappings are copied for optimization, and
 * all contractions for a character are copied if any contractions for that character
 * are added, modified or removed.
 *
 * It might be simpler to re-parse the rule string, but:
 * - That would require duplicating some of the from-rules builder code.
 * - That would make the runtime code depend on the builder.
 * - That would only work if we have the rule string, and we allow users to
 *   omit the rule string from data files.
 */
final class TailoredSet {
public:
    TailoredSet(UnicodeSet *t)
            : data(null), baseData(null),
              tailored(t),
              suffix(null),
              errorCode(U_ZERO_ERROR) {}

    void forData(CollationData d);

    /**
     * @return U_SUCCESS in C++, void in Java
     * @internal only public for access by callback
     */
    boolean handleCE32(int start, int end, int ce32);

private:
    void compare(int c, int ce32, int baseCE32);
    void comparePrefixes(int c, const UChar *p, const UChar *q);
    void compareContractions(int c, const UChar *p, const UChar *q);

    void addPrefixes(CollationData d, int c, const UChar *p);
    void addPrefix(CollationData d, const UnicodeString &pfx, int c, int ce32);
    void addContractions(int c, const UChar *p);
    void addSuffix(int c, const UnicodeString &sfx);
    void add(int c);

    /** Prefixes are reversed in the data structure. */
    void setPrefix(const UnicodeString &pfx) {
        unreversedPrefix = pfx;
        unreversedPrefix.reverse();
    }
    void resetPrefix() {
        unreversedPrefix.remove();
    }

    CollationData data;
    CollationData baseData;
    UnicodeSet *tailored;
    UnicodeString unreversedPrefix;
    const UnicodeString *suffix;
    UErrorCode errorCode;
}

    static boolean U_CALLCONV
    enumTailoredRange(const void *context, int start, int end, int ce32) {
        if(ce32 == Collation.FALLBACK_CE32) {
            return true;  // fallback to base, not tailored
        }
        TailoredSet *ts = (TailoredSet *)context;
        return ts.handleCE32(start, end, ce32);
    }

    void
    TailoredSet.forData(CollationData d, UErrorCode &ec) {
        if(U_FAILURE(ec)) { return; }
        errorCode = ec;  // Preserve info & warning codes.
        data = d;
        baseData = d.base;
        assert(baseData != null);
        utrie2_enum(data.trie, null, enumTailoredRange, this);
        ec = errorCode;
    }

    boolean
    TailoredSet.handleCE32(int start, int end, int ce32) {
        assert(ce32 != Collation.FALLBACK_CE32);
        if(Collation.isSpecialCE32(ce32)) {
            ce32 = data.getIndirectCE32(ce32);
            if(ce32 == Collation.FALLBACK_CE32) {
                return U_SUCCESS;
            }
        }
        do {
            int baseCE32 = baseData.getFinalCE32(baseData.getCE32(start));
            // Do not just continue if ce32 == baseCE32 because
            // contractions and expansions in different data objects
            // normally differ even if they have the same data offsets.
            if(Collation.isSelfContainedCE32(ce32) && Collation.isSelfContainedCE32(baseCE32)) {
                // fastpath
                if(ce32 != baseCE32) {
                    tailored.add(start);
                }
            } else {
                compare(start, ce32, baseCE32);
            }
        } while(++start <= end);
        return U_SUCCESS;
    }

    void
    TailoredSet.compare(int c, int ce32, int baseCE32) {
        if(Collation.isPrefixCE32(ce32)) {
            int dataIndex = Collation.indexFromCE32(ce32);
            ce32 = data.getFinalCE32(data.getCE32FromContexts(dataIndex));
            if(Collation.isPrefixCE32(baseCE32)) {
                int baseIndex = Collation.indexFromCE32(baseCE32);
                baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
                comparePrefixes(c, data.contexts, dataIndex + 2, baseData.contexts, baseIndex + 2);
            } else {
                addPrefixes(data, c, data.contexts, dataIndex + 2);
            }
        } else if(Collation.isPrefixCE32(baseCE32)) {
            int baseIndex = Collation.indexFromCE32(baseCE32);
            baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
            addPrefixes(baseData, c, baseData.contexts, baseIndex + 2);
        }

        if(Collation.isContractionCE32(ce32)) {
            int dataIndex = Collation.indexFromCE32(ce32);
            if((ce32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
                ce32 = Collation.NO_CE32;
            } else {
                ce32 = data.getFinalCE32(data.getCE32FromContexts(dataIndex));
            }
            if(Collation.isContractionCE32(baseCE32)) {
                int baseIndex = Collation.indexFromCE32(baseCE32);
                if((baseCE32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) != 0) {
                    baseCE32 = Collation.NO_CE32;
                } else {
                    baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
                }
                compareContractions(c, data.contexts, dataIndex + 2, baseData.contexts, baseIndex + 2);
            } else {
                addContractions(c, data.contexts, dataIndex + 2);
            }
        } else if(Collation.isContractionCE32(baseCE32)) {
            int baseIndex = Collation.indexFromCE32(baseCE32);
            baseCE32 = baseData.getFinalCE32(baseData.getCE32FromContexts(baseIndex));
            addContractions(c, baseData.contexts, baseIndex + 2);
        }

        int tag;
        if(Collation.isSpecialCE32(ce32)) {
            tag = Collation.tagFromCE32(ce32);
            assert(tag != Collation.PREFIX_TAG);
            assert(tag != Collation.CONTRACTION_TAG);
            // Currently, the tailoring data builder does not write offset tags.
            // They might be useful for saving space,
            // but they would complicate the builder,
            // and in tailorings we assume that performance of tailored characters is more important.
            assert(tag != Collation.OFFSET_TAG);
        } else {
            tag = -1;
        }
        int baseTag;
        if(Collation.isSpecialCE32(baseCE32)) {
            baseTag = Collation.tagFromCE32(baseCE32);
            assert(baseTag != Collation.PREFIX_TAG);
            assert(baseTag != Collation.CONTRACTION_TAG);
        } else {
            baseTag = -1;
        }

        // Non-contextual mappings, expansions, etc.
        if(baseTag == Collation.OFFSET_TAG) {
            // We might be comparing a tailoring CE which is a copy of
            // a base offset-tag CE, via the [optimize [set]] syntax
            // or when a single-character mapping was copied for tailored contractions.
            // Offset tags always result in long-primary CEs,
            // with common secondary/tertiary weights.
            if(!Collation.isLongPrimaryCE32(ce32)) {
                add(c);
                return;
            }
            long dataCE = baseData.ces[Collation.indexFromCE32(baseCE32)];
            long p = Collation.getThreeBytePrimaryForOffsetData(c, dataCE);
            if(Collation.primaryFromLongPrimaryCE32(ce32) != p) {
                add(c);
                return;
            }
        }

        if(tag != baseTag) {
            add(c);
            return;
        }

        if(tag == Collation.EXPANSION32_TAG) {
            const uint32_t *ce32s = data.ce32s + Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);

            const uint32_t *baseCE32s = baseData.ce32s + Collation.indexFromCE32(baseCE32);
            int baseLength = Collation.lengthFromCE32(baseCE32);

            if(length != baseLength) {
                add(c);
                return;
            }
            for(int i = 0; i < length; ++i) {
                if(ce32s[i] != baseCE32s[i]) {
                    add(c);
                    break;
                }
            }
        } else if(tag == Collation.EXPANSION_TAG) {
            const long *ces = data.ces + Collation.indexFromCE32(ce32);
            int length = Collation.lengthFromCE32(ce32);

            const long *baseCEs = baseData.ces + Collation.indexFromCE32(baseCE32);
            int baseLength = Collation.lengthFromCE32(baseCE32);

            if(length != baseLength) {
                add(c);
                return;
            }
            for(int i = 0; i < length; ++i) {
                if(ces[i] != baseCEs[i]) {
                    add(c);
                    break;
                }
            }
        } else if(tag == Collation.HANGUL_TAG) {
            UChar jamos[3];
            int length = Hangul.decompose(c, jamos);
            if(tailored.contains(jamos[0]) || tailored.contains(jamos[1]) ||
                    (length == 3 && tailored.contains(jamos[2]))) {
                add(c);
            }
        } else if(ce32 != baseCE32) {
            add(c);
        }
    }

    void
    TailoredSet.comparePrefixes(int c, const UChar *p, const UChar *q) {
        // Parallel iteration over prefixes of both tables.
        UCharsTrie.Iterator prefixes(p, 0);
        UCharsTrie.Iterator basePrefixes(q, 0);
        const UnicodeString *tp = null;  // Tailoring prefix.
        const UnicodeString *bp = null;  // Base prefix.
        // Use a string with a U+FFFF as the limit sentinel.
        // U+FFFF is untailorable and will not occur in prefixes.
        UnicodeString none((UChar)0xffff);
        for(;;) {
            if(tp == null) {
                if(prefixes.next) {
                    tp = &prefixes.getString();
                } else {
                    tp = &none;
                }
            }
            if(bp == null) {
                if(basePrefixes.next) {
                    bp = &basePrefixes.getString();
                } else {
                    bp = &none;
                }
            }
            if(tp == &none && bp == &none) { break; }
            int cmp = tp.compare(*bp);
            if(cmp < 0) {
                // tp occurs in the tailoring but not in the base.
                addPrefix(data, *tp, c, prefixes.getValue());
                tp = null;
            } else if(cmp > 0) {
                // bp occurs in the base but not in the tailoring.
                addPrefix(baseData, *bp, c, basePrefixes.getValue());
                bp = null;
            } else {
                setPrefix(*tp);
                compare(c, prefixes.getValue(), basePrefixes.getValue());
                resetPrefix();
                tp = null;
                bp = null;
            }
        }
    }

    void
    TailoredSet.compareContractions(int c, const UChar *p, const UChar *q) {
        // Parallel iteration over suffixes of both tables.
        UCharsTrie.Iterator suffixes(p, 0);
        UCharsTrie.Iterator baseSuffixes(q, 0);
        const UnicodeString *ts = null;  // Tailoring suffix.
        const UnicodeString *bs = null;  // Base suffix.
        // Use a string with two U+FFFF as the limit sentinel.
        // U+FFFF is untailorable and will not occur in contractions except maybe
        // as a single suffix character for a root-collator boundary contraction.
        UnicodeString none((UChar)0xffff);
        none.append((UChar)0xffff);
        for(;;) {
            if(ts == null) {
                if(suffixes.next) {
                    ts = &suffixes.getString();
                } else {
                    ts = &none;
                }
            }
            if(bs == null) {
                if(baseSuffixes.next) {
                    bs = &baseSuffixes.getString();
                } else {
                    bs = &none;
                }
            }
            if(ts == &none && bs == &none) { break; }
            int cmp = ts.compare(*bs);
            if(cmp < 0) {
                // ts occurs in the tailoring but not in the base.
                addSuffix(c, *ts);
                ts = null;
            } else if(cmp > 0) {
                // bs occurs in the base but not in the tailoring.
                addSuffix(c, *bs);
                bs = null;
            } else {
                suffix = ts;
                compare(c, suffixes.getValue(), baseSuffixes.getValue());
                suffix = null;
                ts = null;
                bs = null;
            }
        }
    }

    void
    TailoredSet.addPrefixes(CollationData d, int c, const UChar *p) {
        UCharsTrie.Iterator prefixes(p, 0);
        while(prefixes.next) {
            addPrefix(d, prefixes.getString(), c, prefixes.getValue());
        }
    }

    void
    TailoredSet.addPrefix(CollationData d, const UnicodeString &pfx, int c, int ce32) {
        setPrefix(pfx);
        ce32 = d.getFinalCE32(ce32);
        if(Collation.isContractionCE32(ce32)) {
            const UChar *p = d.contexts + Collation.indexFromCE32(ce32);
            addContractions(c, p + 2);
        }
        tailored.add(UnicodeString(unreversedPrefix).append(c));
        resetPrefix();
    }

    void
    TailoredSet.addContractions(int c, const UChar *p) {
        UCharsTrie.Iterator suffixes(p, 0);
        while(suffixes.next) {
            addSuffix(c, suffixes.getString());
        }
    }

    void
    TailoredSet.addSuffix(int c, const UnicodeString &sfx) {
        tailored.add(UnicodeString(unreversedPrefix).append(c).append(sfx));
    }

    void
    TailoredSet.add(int c) {
        if(unreversedPrefix.isEmpty() && suffix == null) {
            tailored.add(c);
        } else {
            UnicodeString s(unreversedPrefix);
            s.append(c);
            if(suffix != null) {
                s.append(*suffix);
            }
            tailored.add(s);
        }
    }
