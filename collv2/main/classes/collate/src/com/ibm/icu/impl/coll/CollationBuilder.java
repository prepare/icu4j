/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationBuilder.java, ported from collationbuilder.h/.cpp
*
* @since 2013may06
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import java.util.Arrays;

import com.ibm.icu.util.VersionInfo;

final class CollationBuilder extends CollationRuleParser.Sink {
public:
    CollationBuilder(const CollationTailoring *base);
    virtual ~CollationBuilder();

    void enableFastLatin() { fastLatinEnabled = true; }

    CollationTailoring *parseAndBuild(const UnicodeString &ruleString,
                                      const VersionInfo rulesVersion,
                                      CollationRuleParser.Importer *importer,
                                      UParseError *outParseError,
                                      );

    const char *getErrorReason() { return errorReason; }

private:
    friend class CEFinalizer;

    /** Implements CollationRuleParser.Sink. */
    virtual void addReset(int strength, const UnicodeString &str,
                          const char *&errorReason);

    long getSpecialResetPosition(const UnicodeString &str,
                                    const char *&parserErrorReason);

    /** Implements CollationRuleParser.Sink. */
    virtual void addRelation(int strength, const UnicodeString &prefix,
                             const UnicodeString &str, const UnicodeString &extension,
                             const char *&errorReason);

    /**
     * Picks one of the current CEs and finds or inserts a node in the graph
     * for the CE + strength.
     */
    int findOrInsertNodeForCEs(int strength, const char *&parserErrorReason,
                                   );
    int findOrInsertNodeForRootCE(long ce, int strength);
    /** Finds or inserts the node for a root CE's primary weight. */
    int findOrInsertNodeForPrimary(long p);
    /** Finds or inserts the node for a secondary or tertiary weight. */
    int findOrInsertWeakNode(int index, int weight16, int level,
                                 );

    /**
     * Makes and inserts a new tailored node into the list, after the one at index.
     * Skips over nodes of weaker strength to maintain collation order
     * ("postpone insertion").
     * @return the new node's index
     */
    int insertTailoredNodeAfter(int index, int strength);

    /**
     * Inserts a new node into the list, between list-adjacent items.
     * The node's previous and next indexes must not be set yet.
     * @return the new node's index
     */
    int insertNodeBetween(int index, int nextIndex, long node,
                              );

    /**
     * Finds the node which implies or contains a common=05 weight of the given strength
     * (secondary or tertiary).
     * Skips weaker nodes and tailored nodes if the current node is stronger
     * and is followed by an explicit-common-weight node.
     * Always returns the input index if that node is no stronger than the given strength.
     */
    int findCommonNode(int index, int strength);

    void setCaseBits(const UnicodeString &nfdString,
                     const char *&parserErrorReason);

    /** Implements CollationRuleParser.Sink. */
    virtual void suppressContractions(const UnicodeSet &set, const char *&parserErrorReason,
                                      );

    /** Implements CollationRuleParser.Sink. */
    virtual void optimize(const UnicodeSet &set, const char *&parserErrorReason,
                          );

    /**
     * Adds the mapping and its canonical closure.
     * Takes ce32=dataBuilder.encodeCEs(...) so that the data builder
     * need not re-encode the CEs multiple times.
     */
    int addWithClosure(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                            const long newCEs[], int newCEsLength, int ce32,
                            );
    int addOnlyClosure(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                            const long newCEs[], int newCEsLength, int ce32,
                            );
    void addTailComposites(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                           );
    boolean mergeCompositeIntoString(const UnicodeString &nfdString, int indexAfterLastStarter,
                                   int composite, const UnicodeString &decomp,
                                   UnicodeString &newNFDString, UnicodeString &newString,
                                   );

    boolean ignorePrefix(const UnicodeString &s);
    boolean ignoreString(const UnicodeString &s);
    boolean isFCD(const UnicodeString &s);

    void closeOverComposites();

    int addIfDifferent(const UnicodeString &prefix, const UnicodeString &str,
                            const long newCEs[], int newCEsLength, int ce32,
                            );
    static boolean sameCEs(const long ces1[], int ces1Length,
                         const long ces2[], int ces2Length);

    /**
     * Walks the tailoring graph and overwrites tailored nodes with new CEs.
     * After this, the graph is destroyed.
     * The nodes array can then be used only as a source of tailored CEs.
     */
    void makeTailoredCEs();
    /**
     * Counts the tailored nodes of the given strength up to the next node
     * which is either stronger or has an explicit weight of this strength.
     */
    static int countTailoredNodes(const long *nodesArray, int i, int strength);

    /** Replaces temporary CEs with the final CEs they point to. */
    void finalizeCEs();

    /**
     * Encodes "temporary CE" data into a CE that fits into the CE32 data structure,
     * with 2-byte primary, 1-byte secondary and 6-bit tertiary,
     * with valid CE byte values.
     *
     * The index must not exceed 20 bits (0xfffff).
     * The strength must fit into 2 bits (UCOL_PRIMARY..UCOL_QUATERNARY).
     *
     * Temporary CEs are distinguished from real CEs by their use of
     * secondary weights 06..45 which are otherwise reserved for compressed sort keys.
     *
     * The case bits are unused and available.
     */
    static long tempCEFromIndexAndStrength(int index, int strength) {
        return
            // CE byte offsets, to ensure valid CE bytes, and case bits 11
            0x4040000006002000L +
            // index bits 19..13 . primary byte 1 = CE bits 63..56 (byte values 40..BF)
            ((long)(index & 0xfe000) << 43) +
            // index bits 12..6 . primary byte 2 = CE bits 55..48 (byte values 40..BF)
            ((long)(index & 0x1fc0) << 42) +
            // index bits 5..0 . secondary byte 1 = CE bits 31..24 (byte values 06..45)
            ((index & 0x3f) << 24) +
            // strength bits 1..0 . tertiary byte 1 = CE bits 13..8 (byte values 20..23)
            (strength << 8);
    }
    static int indexFromTempCE(long tempCE) {
        tempCE -= 0x4040000006002000L;
        return
            ((int32_t)(tempCE >> 43) & 0xfe000) |
            ((int32_t)(tempCE >> 42) & 0x1fc0) |
            ((int32_t)(tempCE >> 24) & 0x3f);
    }
    static int strengthFromTempCE(long tempCE) {
        return ((int32_t)tempCE >> 8) & 3;
    }
    static boolean isTempCE(long ce) {
        int sec = (int)ce >>> 24;
        return 6 <= sec && sec <= 0x45;
    }

    static int indexFromTempCE32(int tempCE32) {
        tempCE32 -= 0x40400620;
        return
            ((int32_t)(tempCE32 >> 11) & 0xfe000) |
            ((int32_t)(tempCE32 >> 10) & 0x1fc0) |
            ((int32_t)(tempCE32 >> 8) & 0x3f);
    }
    static boolean isTempCE32(int ce32) {
        return
            (ce32 & 0xff) >= 2 &&  // not a long-primary/long-secondary CE32
            6 <= ((ce32 >> 8) & 0xff) && ((ce32 >> 8) & 0xff) <= 0x45;
    }

    static int ceStrength(long ce);

    /** The secondary/tertiary lower limit for tailoring before the common weight. */
    private static final int BEFORE_WEIGHT16 = Collation.MERGE_SEPARATOR_WEIGHT16;

    /** At most 1M nodes, limited by the 20 bits in node bit fields. */
    private static final int MAX_INDEX = 0xfffff;
    /**
     * Node bit 6 is set on a primary node if there are tailored nodes
     * with secondary values below the common secondary weight (05),
     * from a reset-secondary-before (&[before 2]).
     */
    private static final int HAS_BEFORE2 = 0x40;
    /**
     * Node bit 5 is set on a primary or secondary node if there are tailored nodes
     * with tertiary values below the common tertiary weight (05),
     * from a reset-tertiary-before (&[before 3]).
     */
    private static final int HAS_BEFORE3 = 0x20;
    /**
     * Node bit 3 distinguishes a tailored node, which has no weight value,
     * from a node with an explicit (root or default) weight.
     */
    private static final int IS_TAILORED = 8;

    static long nodeFromWeight32(long weight32) {
        return weight32 << 32;
    }
    static long nodeFromWeight16(int weight16) {
        return (long)weight16 << 48;
    }
    static long nodeFromPreviousIndex(int previous) {
        return (long)previous << 28;
    }
    static long nodeFromNextIndex(int next) {
        return next << 8;
    }
    static long nodeFromStrength(int strength) {
        return strength;
    }

    static long weight32FromNode(long node) {
        return node >>> 32;
    }
    static int weight16FromNode(long node) {
        return (int)(node >> 48) & 0xffff;
    }
    static int previousIndexFromNode(long node) {
        return (int)(node >> 28) & MAX_INDEX;
    }
    static int nextIndexFromNode(long node) {
        return ((int)node >> 8) & MAX_INDEX;
    }
    static int strengthFromNode(long node) {
        return (int)node & 3;
    }

    static int nodeHasBefore2(long node) {
        return (node & HAS_BEFORE2) != 0;
    }
    static int nodeHasBefore3(long node) {
        return (node & HAS_BEFORE3) != 0;
    }
    static int nodeHasAnyBefore(long node) {
        return (node & (HAS_BEFORE2 | HAS_BEFORE3)) != 0;
    }
    static int isTailoredNode(long node) {
        return (node & IS_TAILORED) != 0;
    }

    static long changeNodePreviousIndex(long node, int previous) {
        return (node & 0xffff00000fffffffL) | nodeFromPreviousIndex(previous);
    }
    static long changeNodeNextIndex(long node, int next) {
        return (node & 0xfffffffff00000ffL) | nodeFromNextIndex(next);
    }

    const Normalizer2 &nfd, &fcd;
    const Normalizer2Impl &nfcImpl;

    const CollationTailoring *base;
    CollationData baseData;
    const CollationRootElements rootElements;
    long variableTop;

    CollationDataBuilder *dataBuilder;
    boolean fastLatinEnabled;
    UnicodeSet optimizeSet;
    const char *errorReason;

    long ces[Collation.MAX_EXPANSION_LENGTH];
    int cesLength;

    /**
     * Indexes of nodes with root primary weights, sorted by primary.
     * Compact form of a TreeMap from root primary to node index.
     *
     * This is a performance optimization for finding reset positions.
     * Without this, we would have to search through the entire nodes list.
     * It also allows storing root primary weights in list head nodes,
     * without previous index, leaving room in root primary nodes for 32-bit primary weights.
     */
    UVector32 rootPrimaryIndexes;
    /**
     * Data structure for assigning tailored weights and CEs.
     * Doubly-linked lists of nodes in mostly collation order.
     * Each list starts with a root primary node and ends with a nextIndex of 0.
     *
     * When there are any nodes in the list, then there is always a root primary node at index 0.
     * This allows some code not to have to check explicitly for nextIndex==0.
     *
     * Root primary nodes have 32-bit weights but do not have previous indexes.
     * All other nodes have at most 16-bit weights and do have previous indexes.
     *
     * Nodes with explicit weights store root collator weights,
     * or default weak weights (e.g., secondary 05) for stronger nodes.
     * "Tailored" nodes, with the IS_TAILORED bit set,
     * do not store explicit weights but rather
     * create a difference of a certain strength from the preceding node.
     *
     * A root node is followed by either
     * - a root/default node of the same strength, or
     * - a root/default node of the next-weaker strength, or
     * - a tailored node of the same strength.
     *
     * A node of a given strength normally implies "common" weights on weaker levels.
     *
     * A node with HAS_BEFORE2 must be immediately followed by
     * a secondary node with BEFORE_WEIGHT16, then a secondary tailored node,
     * and later an explicit common-secondary node.
     * (&[before 2] resets to the BEFORE_WEIGHT16 node so that
     * the following addRelation(secondary) tailors right after that.
     * If we did not have this node and instead were to reset on the primary node,
     * then addRelation(secondary) would skip forward to the the COMMON_WEIGHT16 node.)
     *
     * All secondary tailored nodes between these two explicit ones
     * will be assigned lower-than-common secondary weights.
     * If the flag is not set, then there are no explicit secondary nodes
     * with the common or lower weights.
     *
     * Same for HAS_BEFORE3 for tertiary nodes and weights.
     * A node must not have both flags set.
     *
     * Tailored CEs are initially represented in a CollationDataBuilder as temporary CEs
     * which point to stable indexes in this list,
     * and temporary CEs stored in a CollationDataBuilder only point to tailored nodes.
     *
     * A temporary CE in the ces[] array may point to a non-tailored reset-before-position node,
     * until the next relation is added.
     *
     * At the end, the tailored weights are allocated as necessary,
     * then the tailored nodes are replaced with final CEs,
     * and the CollationData is rewritten by replacing temporary CEs with final ones.
     *
     * We cannot simply insert new nodes in the middle of the array
     * because that would invalidate the indexes stored in existing temporary CEs.
     * We need to use a linked graph with stable indexes to existing nodes.
     * A doubly-linked list seems easiest to maintain.
     *
     * Each node is stored as an long, with its fields stored as bit fields.
     *
     * Root primary node:
     * - primary weight: 32 bits 63..32
     * - reserved/unused/zero: 4 bits 31..28
     *
     * Weaker root nodes & tailored nodes:
     * - a weight: 16 bits 63..48
     *   + a root or default weight for a non-tailored node
     *   + unused/zero for a tailored node
     * - index to the previous node: 20 bits 47..28
     *
     * All types of nodes:
     * - index to the next node: 20 bits 27..8
     *   + nextIndex=0 in last node per root-primary list
     * - reserved/unused/zero bits: bits 7, 4, 2
     * - HAS_BEFORE2: bit 6
     * - HAS_BEFORE3: bit 5
     * - IS_TAILORED: bit 3
     * - the difference strength (primary/secondary/tertiary/quaternary): 2 bits 1..0
     *
     * We could allocate structs with pointers, but we would have to store them
     * in a pointer list so that they can be indexed from temporary CEs,
     * and they would require more memory allocations.
     */
    UVector64 nodes;
}

    private static final class BundleImporter implements CollationRuleParser.Importer {
    public:
        BundleImporter() : rules(null) {}
        virtual const UnicodeString *getRules(
                const char *localeID, const char *collationType);

    private:
        UnicodeString *rules;
    };

    const UnicodeString *
    BundleImporter.getRules(
            const char *localeID, const char *collationType) {
        return rules = CollationLoader.loadRules(localeID, collationType);
    }

    // CollationBuilder implementation ----------------------------------------- ***

    CollationBuilder.CollationBuilder(const CollationTailoring *b)
            : nfd(*Normalizer2.getNFDInstance),
              fcd(*Normalizer2Factory.getFCDInstance),
              nfcImpl(*Normalizer2Factory.getNFCImpl),
              base(b),
              baseData(b.data),
              rootElements(b.data.rootElements, b.data.rootElementsLength),
              variableTop(0),
              dataBuilder(new CollationDataBuilder), fastLatinEnabled(false),
              errorReason(null),
              cesLength(0),
              rootPrimaryIndexes, nodes {
        nfcImpl.ensureCanonIterData;
        if(U_FAILURE) {
            errorReason = "CollationBuilder fields initialization failed";
            return;
        }
        if(dataBuilder == null) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        dataBuilder.initForTailoring(baseData);
        if(U_FAILURE) {
            errorReason = "CollationBuilder initialization failed";
        }
    }

    CollationBuilder.~CollationBuilder() {
        delete dataBuilder;
    }

    CollationTailoring parseAndBuild(String ruleString) {
        if(baseData.rootElements == null) {
            errorCode = U_MISSING_RESOURCE_ERROR;
            errorReason = "missing root elements data, tailoring not supported";
            return null;
        }
        LocalPointer<CollationTailoring> tailoring(new CollationTailoring(base.settings));
        if(tailoring.isNull() || tailoring.isBogus()) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return null;
        }
        CollationRuleParser parser(baseData);
        if(U_FAILURE) { return null; }
        // Note: This always bases &[last variable] and &[first regular]
        // on the root collator's maxVariable/variableTop.
        // If we wanted this to change after [maxVariable x], then we would keep
        // the tailoring.settings pointer here and read its variableTop when we need it.
        // See http://unicode.org/cldr/trac/ticket/6070
        variableTop = base.settings.variableTop;
        parser.setSink(this);
        // In Java, there is only one Importer implementation.
        // In C++, the importer is a parameter for this method.
        parser.setImporter(new BundleImporter());
        parser.parse(ruleString, *tailoring, outParseError);
        errorReason = parser.getErrorReason();
        if(U_FAILURE) { return null; }
        if(dataBuilder.hasMappings()) {
            makeTailoredCEs;
            closeOverComposites;
            finalizeCEs;
            // Copy all of ASCII, and Latin-1 letters, into each tailoring.
            optimizeSet.add(0, 0x7f);
            optimizeSet.add(0xc0, 0xff);
            // Hangul is decomposed on the fly during collation,
            // and the tailoring data is always built with HANGUL_TAG specials.
            optimizeSet.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
            dataBuilder.optimize(optimizeSet);
            tailoring.ensureOwnedData;
            if(U_FAILURE) { return null; }
            if(fastLatinEnabled) { dataBuilder.enableFastLatin(); }
            dataBuilder.build(*tailoring.ownedData);
            tailoring.builder = dataBuilder;
            dataBuilder = null;
        } else {
            tailoring.data = baseData;
        }
        if(U_FAILURE) { return null; }
        tailoring.rules = ruleString;
        tailoring.rules.getTerminatedBuffer();  // ensure NUL-termination
        // In Java, we do not have a rules version.
        // In C++, the genrb build tool reads and supplies one,
        // and the rulesVersion is a parameter for this method.
        VersionInfo rulesVersion = VersionInfo.getInstance(0, 0, 0, 0);
        tailoring.setVersion(base.version, rulesVersion);
        return tailoring.orphan();
    }

    void
    CollationBuilder.addReset(int strength, const UnicodeString &str,
                              const char *&parserErrorReason) {
        if(U_FAILURE) { return; }
        assert(!str.isEmpty());
        if(str.charAt(0) == CollationRuleParser.POS_LEAD) {
            ces[0] = getSpecialResetPosition(str, parserErrorReason);
            cesLength = 1;
            if(U_FAILURE) { return; }
            assert((ces[0] & Collation.CASE_AND_QUATERNARY_MASK) == 0);
        } else {
            // normal reset to a character or string
            UnicodeString nfdString = nfd.normalize(str);
            if(U_FAILURE) {
                parserErrorReason = "normalizing the reset position";
                return;
            }
            cesLength = dataBuilder.getCEs(nfdString, ces, 0);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
                parserErrorReason = "reset position maps to too many collation elements (more than 31)";
                return;
            }
        }
        if(strength == UCOL_IDENTICAL) { return; }  // simple reset-at-position

        // &[before strength]position
        assert(UCOL_PRIMARY <= strength && strength <= UCOL_TERTIARY);
        int index = findOrInsertNodeForCEs(strength, parserErrorReason);
        if(U_FAILURE) { return; }

        long node = nodes.elementAti(index);
        // If the index is for a "weaker" tailored node,
        // then skip backwards over this and further "weaker" nodes.
        while(strengthFromNode(node) > strength) {
            index = previousIndexFromNode(node);
            node = nodes.elementAti(index);
        }

        // Find or insert a node whose index we will put into a temporary CE.
        if(strengthFromNode(node) == strength && isTailoredNode(node)) {
            // Reset to just before this same-strength tailored node.
            index = previousIndexFromNode(node);
        } else if(strength == UCOL_PRIMARY) {
            // root primary node (has no previous index)
            long p = weight32FromNode(node);
            if(p == 0) {
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "reset primary-before ignorable not possible";
                return;
            }
            if(p <= rootElements.getFirstPrimary()) {
                // There is no primary gap between ignorables and the space-first-primary.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "reset primary-before first non-ignorable not supported";
                return;
            }
            if(p == Collation.FIRST_TRAILING_PRIMARY) {
                // We do not support tailoring to an unassigned-implicit CE.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "reset primary-before [first trailing] not supported";
                return;
            }
            p = rootElements.getPrimaryBefore(p, baseData.isCompressiblePrimary(p));
            index = findOrInsertNodeForPrimary(p);
            // Go to the last node in this list:
            // Tailor after the last node between adjacent root nodes.
            for(;;) {
                node = nodes.elementAti(index);
                int nextIndex = nextIndexFromNode(node);
                if(nextIndex == 0) { break; }
                index = nextIndex;
            }
        } else {
            // &[before 2] or &[before 3]
            index = findCommonNode(index, UCOL_SECONDARY);
            if(strength >= UCOL_TERTIARY) {
                index = findCommonNode(index, UCOL_TERTIARY);
            }
            node = nodes.elementAti(index);
            if(strengthFromNode(node) == strength) {
                // Found a same-strength node with an explicit weight.
                int weight16 = weight16FromNode(node);
                if(weight16 == 0) {
                    errorCode = U_UNSUPPORTED_ERROR;
                    if(strength == UCOL_SECONDARY) {
                        parserErrorReason = "reset secondary-before secondary ignorable not possible";
                    } else {
                        parserErrorReason = "reset tertiary-before completely ignorable not possible";
                    }
                    return;
                }
                assert(weight16 >= Collation.COMMON_WEIGHT16);
                int previousIndex = previousIndexFromNode(node);
                if(weight16 == Collation.COMMON_WEIGHT16) {
                    // Reset to just before this same-strength common-weight node.
                    index = previousIndex;
                } else {
                    // A non-common weight is only possible from a root CE.
                    // Find the higher-level weights, which must all be explicit,
                    // and then find the preceding weight for this level.
                    long previousWeight16 = 0;
                    int previousWeightIndex = -1;
                    int i = index;
                    if(strength == UCOL_SECONDARY) {
                        long p;
                        do {
                            i = previousIndexFromNode(node);
                            node = nodes.elementAti(i);
                            if(strengthFromNode(node) == UCOL_SECONDARY && !isTailoredNode(node) &&
                                    previousWeightIndex < 0) {
                                previousWeightIndex = i;
                                previousWeight16 = weight16FromNode(node);
                            }
                        } while(strengthFromNode(node) > UCOL_PRIMARY);
                        assert(!isTailoredNode(node));
                        p = weight32FromNode(node);
                        weight16 = rootElements.getSecondaryBefore(p, weight16);
                    } else {
                        long p;
                        int s;
                        do {
                            i = previousIndexFromNode(node);
                            node = nodes.elementAti(i);
                            if(strengthFromNode(node) == UCOL_TERTIARY && !isTailoredNode(node) &&
                                    previousWeightIndex < 0) {
                                previousWeightIndex = i;
                                previousWeight16 = weight16FromNode(node);
                            }
                        } while(strengthFromNode(node) > UCOL_SECONDARY);
                        assert(!isTailoredNode(node));
                        if(strengthFromNode(node) == UCOL_SECONDARY) {
                            s = weight16FromNode(node);
                            do {
                                i = previousIndexFromNode(node);
                                node = nodes.elementAti(i);
                            } while(strengthFromNode(node) > UCOL_PRIMARY);
                            assert(!isTailoredNode(node));
                        } else {
                            assert(!nodeHasBefore2(node));
                            s = Collation.COMMON_WEIGHT16;
                        }
                        p = weight32FromNode(node);
                        weight16 = rootElements.getTertiaryBefore(p, s, weight16);
                        assert((weight16 & ~Collation.ONLY_TERTIARY_MASK) == 0);
                    }
                    // Find or insert the new explicit weight before the current one.
                    if(previousWeightIndex >= 0 && weight16 == previousWeight16) {
                        // Tailor after the last node between adjacent root nodes.
                        index = previousIndex;
                    } else {
                        node = nodeFromWeight16(weight16) | nodeFromStrength(strength);
                        index = insertNodeBetween(previousIndex, index, node);
                    }
                }
            } else {
                // Found a stronger node with implied strength-common weight.
                long hasBefore3 = 0;
                if(strength == UCOL_SECONDARY) {
                    assert(!nodeHasBefore2(node));
                    // Move the HAS_BEFORE3 flag from the parent node
                    // to the new secondary common node.
                    hasBefore3 = node & HAS_BEFORE3;
                    node = (node & ~(long)HAS_BEFORE3) | HAS_BEFORE2;
                } else {
                    assert(!nodeHasBefore3(node));
                    node |= HAS_BEFORE3;
                }
                nodes.setElementAt(node, index);
                int nextIndex = nextIndexFromNode(node);
                // Insert default nodes with weights 02 and 05, reset to the 02 node.
                node = nodeFromWeight16(BEFORE_WEIGHT16) | nodeFromStrength(strength);
                index = insertNodeBetween(index, nextIndex, node);
                node = nodeFromWeight16(Collation.COMMON_WEIGHT16) | hasBefore3 |
                        nodeFromStrength(strength);
                insertNodeBetween(index, nextIndex, node);
            }
            // Strength of the temporary CE = strength of its reset position.
            // Code above raises an error if the before-strength is stronger.
            strength = ceStrength(ces[cesLength - 1]);
        }
        if(U_FAILURE) {
            parserErrorReason = "inserting reset position for &[before n]";
            return;
        }
        ces[cesLength - 1] = tempCEFromIndexAndStrength(index, strength);
    }

    long
    CollationBuilder.getSpecialResetPosition(const UnicodeString &str,
                                              const char *&parserErrorReason) {
        assert(str.length() == 2);
        long ce;
        int strength = UCOL_PRIMARY;
        boolean isBoundary = false;
        int pos = str.charAt(1) - CollationRuleParser.POS_BASE;
        assert(0 <= pos && pos <= CollationRuleParser.LAST_TRAILING);
        switch(pos) {
        case CollationRuleParser.FIRST_TERTIARY_IGNORABLE:
            // Quaternary CEs are not supported.
            // Non-zero quaternary weights are possible only on tertiary or stronger CEs.
            return 0;
        case CollationRuleParser.LAST_TERTIARY_IGNORABLE:
            return 0;
        case CollationRuleParser.FIRST_SECONDARY_IGNORABLE: {
            // Look for a tailored tertiary node after [0, 0, 0].
            int index = findOrInsertNodeForRootCE(0, UCOL_TERTIARY);
            if(U_FAILURE) { return 0; }
            long node = nodes.elementAti(index);
            if((index = nextIndexFromNode(node)) != 0) {
                node = nodes.elementAti(index);
                assert(strengthFromNode(node) <= UCOL_TERTIARY);
                if(isTailoredNode(node) && strengthFromNode(node) == UCOL_TERTIARY) {
                    return tempCEFromIndexAndStrength(index, UCOL_TERTIARY);
                }
            }
            return rootElements.getFirstTertiaryCE();
            // No need to look for nodeHasAnyBefore() on a tertiary node.
        }
        case CollationRuleParser.LAST_SECONDARY_IGNORABLE:
            ce = rootElements.getLastTertiaryCE();
            strength = UCOL_TERTIARY;
            break;
        case CollationRuleParser.FIRST_PRIMARY_IGNORABLE: {
            // Look for a tailored secondary node after [0, 0, *].
            int index = findOrInsertNodeForRootCE(0, UCOL_SECONDARY);
            if(U_FAILURE) { return 0; }
            long node = nodes.elementAti(index);
            while((index = nextIndexFromNode(node)) != 0) {
                node = nodes.elementAti(index);
                strength = strengthFromNode(node);
                if(strength < UCOL_SECONDARY) { break; }
                if(strength == UCOL_SECONDARY) {
                    if(isTailoredNode(node)) {
                        if(nodeHasBefore3(node)) {
                            index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                            assert(isTailoredNode(nodes.elementAti(index)));
                        }
                        return tempCEFromIndexAndStrength(index, UCOL_SECONDARY);
                    } else {
                        break;
                    }
                }
            }
            ce = rootElements.getFirstSecondaryCE();
            strength = UCOL_SECONDARY;
            break;
        }
        case CollationRuleParser.LAST_PRIMARY_IGNORABLE:
            ce = rootElements.getLastSecondaryCE();
            strength = UCOL_SECONDARY;
            break;
        case CollationRuleParser.FIRST_VARIABLE:
            ce = rootElements.getFirstPrimaryCE();
            isBoundary = true;  // FractionalUCA.txt: FDD1 00A0, SPACE first primary
            break;
        case CollationRuleParser.LAST_VARIABLE:
            ce = rootElements.lastCEWithPrimaryBefore(variableTop + 1);
            break;
        case CollationRuleParser.FIRST_REGULAR:
            ce = rootElements.firstCEWithPrimaryAtLeast(variableTop + 1);
            isBoundary = true;  // FractionalUCA.txt: FDD1 263A, SYMBOL first primary
            break;
        case CollationRuleParser.LAST_REGULAR:
            // Use the Hani-first-primary rather than the actual last "regular" CE before it,
            // for backward compatibility with behavior before the introduction of
            // script-first-primary CEs in the root collator.
            ce = rootElements.firstCEWithPrimaryAtLeast(
                baseData.getFirstPrimaryForGroup(USCRIPT_HAN));
            break;
        case CollationRuleParser.FIRST_IMPLICIT: {
            int ce32 = baseData.getCE32(0x4e00);
            assert(Collation.hasCE32Tag(ce32, Collation.OFFSET_TAG));
            ce = baseData.getCEFromOffsetCE32(0x4e00, ce32);
            break;
        }
        case CollationRuleParser.LAST_IMPLICIT:
            // We do not support tailoring to an unassigned-implicit CE.
            errorCode = U_UNSUPPORTED_ERROR;
            parserErrorReason = "reset to [last implicit] not supported";
            return 0;
        case CollationRuleParser.FIRST_TRAILING:
            ce = Collation.makeCE(Collation.FIRST_TRAILING_PRIMARY);
            isBoundary = true;  // trailing first primary (there is no mapping for it)
            break;
        case CollationRuleParser.LAST_TRAILING:
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            parserErrorReason = "LDML forbids tailoring to U+FFFF";
            return 0;
        default:
            assert(false);
            return 0;
        }

        int index = findOrInsertNodeForRootCE(ce, strength);
        if(U_FAILURE) { return 0; }
        long node = nodes.elementAti(index);
        if((pos & 1) == 0) {
            // even pos = [first xyz]
            if(!nodeHasAnyBefore(node) && isBoundary) {
                // A <group> first primary boundary is artificially added to FractionalUCA.txt.
                // It is reachable via its special contraction, but is not normally used.
                // Find the first character tailored after the boundary CE,
                // or the first real root CE after it.
                if((index = nextIndexFromNode(node)) != 0) {
                    // If there is a following node, then it must be tailored
                    // because there are no root CEs with a boundary primary
                    // and non-common secondary/tertiary weights.
                    node = nodes.elementAti(index);
                    assert(isTailoredNode(node));
                    ce = tempCEFromIndexAndStrength(index, strength);
                } else {
                    assert(strength == UCOL_PRIMARY);
                    long p = ce >>> 32;
                    int pIndex = rootElements.findPrimary(p);
                    boolean isCompressible = baseData.isCompressiblePrimary(p);
                    p = rootElements.getPrimaryAfter(p, pIndex, isCompressible);
                    ce = Collation.makeCE(p);
                    index = findOrInsertNodeForRootCE(ce, UCOL_PRIMARY);
                    if(U_FAILURE) { return 0; }
                    node = nodes.elementAti(index);
                }
            }
            if(nodeHasAnyBefore(node)) {
                // Get the first node that was tailored before this one at a weaker strength.
                if(nodeHasBefore2(node)) {
                    index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                    node = nodes.elementAti(index);
                }
                if(nodeHasBefore3(node)) {
                    index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                }
                assert(isTailoredNode(nodes.elementAti(index)));
                ce = tempCEFromIndexAndStrength(index, strength);
            }
        } else {
            // odd pos = [last xyz]
            // Find the last node that was tailored after the [last xyz]
            // at a strength no greater than the position's strength.
            for(;;) {
                int nextIndex = nextIndexFromNode(node);
                if(nextIndex == 0) { break; }
                int nextNode = nodes.elementAti(nextIndex);
                if(strengthFromNode(nextNode) < strength) { break; }
                index = nextIndex;
                node = nextNode;
            }
            // Do not make a temporary CE for a root node.
            // This last node might be the node for the root CE itself,
            // or a node with a common secondary or tertiary weight.
            if(isTailoredNode(node)) {
                ce = tempCEFromIndexAndStrength(index, strength);
            }
        }
        return ce;
    }

    void
    CollationBuilder.addRelation(int strength, const UnicodeString &prefix,
                                  const UnicodeString &str, const UnicodeString &extension,
                                  const char *&parserErrorReason) {
        if(U_FAILURE) { return; }
        UnicodeString nfdPrefix;
        if(!prefix.isEmpty()) {
            nfd.normalize(prefix, nfdPrefix);
            if(U_FAILURE) {
                parserErrorReason = "normalizing the relation prefix";
                return;
            }
        }
        UnicodeString nfdString = nfd.normalize(str);
        if(U_FAILURE) {
            parserErrorReason = "normalizing the relation string";
            return;
        }

        // The runtime code decomposes Hangul syllables on the fly,
        // with recursive processing but without making the Jamo pieces visible for matching.
        // It does not work with certain types of contextual mappings.
        int nfdLength = nfdString.length();
        if(nfdLength >= 2) {
            UChar c = nfdString.charAt(0);
            if(Hangul.isJamoL(c) || Hangul.isJamoV(c)) {
                // While handling a Hangul syllable, contractions starting with Jamo L or V
                // would not see the following Jamo of that syllable.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "contractions starting with conjoining Jamo L or V not supported";
                return;
            }
            c = nfdString.charAt(nfdLength - 1);
            if(Hangul.isJamoL(c) ||
                    (Hangul.isJamoV(c) && Hangul.isJamoL(nfdString.charAt(nfdLength - 2)))) {
                // A contraction ending with Jamo L or L+V would require
                // generating Hangul syllables in addTailComposites() (588 for a Jamo L),
                // or decomposing a following Hangul syllable on the fly, during contraction matching.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "contractions ending with conjoining Jamo L or L+V not supported";
                return;
            }
            // A Hangul syllable completely inside a contraction is ok.
        }
        // Note: If there is a prefix, then the parser checked that
        // both the prefix and the string beging with NFC boundaries (not Jamo V or T).
        // Therefore: prefix.isEmpty() || !isJamoVOrT(nfdString.charAt(0))
        // (While handling a Hangul syllable, prefixes on Jamo V or T
        // would not see the previous Jamo of that syllable.)

        if(strength != UCOL_IDENTICAL) {
            // Find the node index after which we insert the new tailored node.
            int index = findOrInsertNodeForCEs(strength, parserErrorReason);
            assert(cesLength > 0);
            long ce = ces[cesLength - 1];
            if(strength == UCOL_PRIMARY && !isTempCE(ce) && (ce >>> 32) == 0) {
                // There is no primary gap between ignorables and the space-first-primary.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "tailoring primary after ignorables not supported";
                return;
            }
            if(strength == UCOL_QUATERNARY && ce == 0) {
                // The CE data structure does not support non-zero quaternary weights
                // on tertiary ignorables.
                errorCode = U_UNSUPPORTED_ERROR;
                parserErrorReason = "tailoring quaternary after tertiary ignorables not supported";
                return;
            }
            // Insert the new tailored node.
            index = insertTailoredNodeAfter(index, strength);
            if(U_FAILURE) {
                parserErrorReason = "modifying collation elements";
                return;
            }
            // Strength of the temporary CE:
            // The new relation may yield a stronger CE but not a weaker one.
            int tempStrength = ceStrength(ce);
            if(strength < tempStrength) { tempStrength = strength; }
            ces[cesLength - 1] = tempCEFromIndexAndStrength(index, tempStrength);
        }

        setCaseBits(nfdString, parserErrorReason);
        if(U_FAILURE) { return; }

        int cesLengthBeforeExtension = cesLength;
        if(!extension.isEmpty()) {
            UnicodeString nfdExtension = nfd.normalize(extension);
            if(U_FAILURE) {
                parserErrorReason = "normalizing the relation extension";
                return;
            }
            cesLength = dataBuilder.getCEs(nfdExtension, ces, cesLength);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                errorCode = U_ILLEGAL_ARGUMENT_ERROR;
                parserErrorReason =
                    "extension string adds too many collation elements (more than 31 total)";
                return;
            }
        }
        int ce32 = Collation.UNASSIGNED_CE32;
        if((prefix != nfdPrefix || str != nfdString) &&
                !ignorePrefix(prefix) && !ignoreString(str)) {
            // Map from the original input to the CEs.
            // We do this in case the canonical closure is incomplete,
            // so that it is possible to explicitly provide the missing mappings.
            ce32 = addIfDifferent(prefix, str, ces, cesLength, ce32);
        }
        addWithClosure(nfdPrefix, nfdString, ces, cesLength, ce32);
        if(U_FAILURE) {
            parserErrorReason = "writing collation elements";
            return;
        }
        cesLength = cesLengthBeforeExtension;
    }

    int32_t
    CollationBuilder.findOrInsertNodeForCEs(int strength, const char *&parserErrorReason,
                                            ) {
        if(U_FAILURE) { return 0; }
        assert(UCOL_PRIMARY <= strength && strength <= UCOL_QUATERNARY);

        // Find the last CE that is at least as "strong" as the requested difference.
        // Note: Stronger is smaller (UCOL_PRIMARY=0).
        long ce;
        for(;; --cesLength) {
            if(cesLength == 0) {
                ce = ces[0] = 0;
                cesLength = 1;
                break;
            } else {
                ce = ces[cesLength - 1];
            }
            if(ceStrength(ce) <= strength) { break; }
        }

        if(isTempCE(ce)) {
            // No need to findCommonNode() here for lower levels
            // because insertTailoredNodeAfter() will do that anyway.
            return indexFromTempCE(ce);
        }

        // root CE
        if((int)(ce >>> 56) == Collation.UNASSIGNED_IMPLICIT_BYTE) {
            errorCode = U_UNSUPPORTED_ERROR;
            parserErrorReason = "tailoring relative to an unassigned code point not supported";
            return 0;
        }
        return findOrInsertNodeForRootCE(ce, strength);
    }

    int32_t
    CollationBuilder.findOrInsertNodeForRootCE(long ce, int strength) {
        if(U_FAILURE) { return 0; }
        assert((int)(ce >>> 56) != Collation.UNASSIGNED_IMPLICIT_BYTE);

        // Find or insert the node for each of the root CE's weights,
        // down to the requested level/strength.
        // Root CEs must have common=zero quaternary weights (for which we never insert any nodes).
        assert((ce & 0xc0) == 0);
        int index = findOrInsertNodeForPrimary(ce >>> 32 );
        if(strength >= UCOL_SECONDARY) {
            int lower32 = (int)ce;
            index = findOrInsertWeakNode(index, lower32 >>> 16, UCOL_SECONDARY);
            if(strength >= UCOL_TERTIARY) {
                index = findOrInsertWeakNode(index, lower32 & Collation.ONLY_TERTIARY_MASK,
                                            UCOL_TERTIARY);
            }
        }
        return index;
    }

    namespace {

    /**
    * Like Java Collections.binarySearch(List, key, Comparator).
    *
    * @return the index>=0 where the item was found,
    *         or the index<0 for inserting the string at ~index in sorted order
    *         (index into rootPrimaryIndexes)
    */
    int32_t
    binarySearchForRootPrimaryNode(const int *rootPrimaryIndexes, int length,
                                  const long *nodes, long p) {
        if(length == 0) { return ~0; }
        int start = 0;
        int limit = length;
        for (;;) {
            int i = (start + limit) / 2;
            long node = nodes[rootPrimaryIndexes[i]];
            long nodePrimary = node >>> 32;  // weight32FromNode(node)
            if (p == nodePrimary) {
                return i;
            } else if (p < nodePrimary) {
                if (i == start) {
                    return ~start;  // insert s before i
                }
                limit = i;
            } else {
                if (i == start) {
                    return ~(start + 1);  // insert s after i
                }
                start = i;
            }
        }
    }

    }  // namespace

    int32_t
    CollationBuilder.findOrInsertNodeForPrimary(long p) {
        if(U_FAILURE) { return 0; }

        int rootIndex = binarySearchForRootPrimaryNode(
            rootPrimaryIndexes.getBuffer(), rootPrimaryIndexes.size(), nodes.getBuffer(), p);
        if(rootIndex >= 0) {
            return rootPrimaryIndexes.elementAti(rootIndex);
        } else {
            // Start a new list of nodes with this primary.
            int index = nodes.size();
            nodes.addElement(nodeFromWeight32(p));
            rootPrimaryIndexes.insertElementAt(index, ~rootIndex);
            return index;
        }
    }

    int32_t
    CollationBuilder.findOrInsertWeakNode(int index, int weight16, int level) {
        if(U_FAILURE) { return 0; }
        assert(0 <= index && index < nodes.size());

        assert(weight16 == 0 || weight16 >= Collation.COMMON_WEIGHT16);
        // Only reset-before inserts common weights.
        if(weight16 == Collation.COMMON_WEIGHT16) {
            return findCommonNode(index, level);
        }
        // Find the root CE's weight for this level.
        // Postpone insertion if not found:
        // Insert the new root node before the next stronger node,
        // or before the next root node with the same strength and a larger weight.
        long node = nodes.elementAti(index);
        int nextIndex;
        while((nextIndex = nextIndexFromNode(node)) != 0) {
            node = nodes.elementAti(nextIndex);
            int nextStrength = strengthFromNode(node);
            if(nextStrength <= level) {
                // Insert before a stronger node.
                if(nextStrength < level) { break; }
                // nextStrength == level
                if(!isTailoredNode(node)) {
                    int nextWeight16 = weight16FromNode(node);
                    if(nextWeight16 == weight16) {
                        // Found the node for the root CE up to this level.
                        return nextIndex;
                    }
                    // Insert before a node with a larger same-strength weight.
                    if(nextWeight16 > weight16) { break; }
                }
            }
            // Skip the next node.
            index = nextIndex;
        }
        node = nodeFromWeight16(weight16) | nodeFromStrength(level);
        return insertNodeBetween(index, nextIndex, node);
    }

    int32_t
    CollationBuilder.insertTailoredNodeAfter(int index, int strength) {
        if(U_FAILURE) { return 0; }
        assert(0 <= index && index < nodes.size());
        if(strength >= UCOL_SECONDARY) {
            index = findCommonNode(index, UCOL_SECONDARY);
            if(strength >= UCOL_TERTIARY) {
                index = findCommonNode(index, UCOL_TERTIARY);
            }
        }
        // Postpone insertion:
        // Insert the new node before the next one with a strength at least as strong.
        long node = nodes.elementAti(index);
        int nextIndex;
        while((nextIndex = nextIndexFromNode(node)) != 0) {
            node = nodes.elementAti(nextIndex);
            if(strengthFromNode(node) <= strength) { break; }
            // Skip the next node which has a weaker (larger) strength than the new one.
            index = nextIndex;
        }
        node = IS_TAILORED | nodeFromStrength(strength);
        return insertNodeBetween(index, nextIndex, node);
    }

    int32_t
    CollationBuilder.insertNodeBetween(int index, int nextIndex, long node,
                                        ) {
        if(U_FAILURE) { return 0; }
        assert(previousIndexFromNode(node) == 0);
        assert(nextIndexFromNode(node) == 0);
        assert(nextIndexFromNode(nodes.elementAti(index)) == nextIndex);
        // Append the new node and link it to the existing nodes.
        int newIndex = nodes.size();
        node |= nodeFromPreviousIndex(index) | nodeFromNextIndex(nextIndex);
        nodes.addElement(node);
        if(U_FAILURE) { return 0; }
        // nodes[index].nextIndex = newIndex
        node = nodes.elementAti(index);
        nodes.setElementAt(changeNodeNextIndex(node, newIndex), index);
        // nodes[nextIndex].previousIndex = newIndex
        if(nextIndex != 0) {
            node = nodes.elementAti(nextIndex);
            nodes.setElementAt(changeNodePreviousIndex(node, newIndex), nextIndex);
        }
        return newIndex;
    }

    int32_t
    CollationBuilder.findCommonNode(int index, int strength) {
        assert(UCOL_SECONDARY <= strength && strength <= UCOL_TERTIARY);
        long node = nodes.elementAti(index);
        if(strengthFromNode(node) >= strength) {
            // The current node is no stronger.
            return index;
        }
        if(strength == UCOL_SECONDARY ? !nodeHasBefore2(node) : !nodeHasBefore3(node)) {
            // The current node implies the strength-common weight.
            return index;
        }
        index = nextIndexFromNode(node);
        node = nodes.elementAti(index);
        assert(!isTailoredNode(node) && strengthFromNode(node) == strength &&
                weight16FromNode(node) == BEFORE_WEIGHT16);
        // Skip to the explicit common node.
        do {
            index = nextIndexFromNode(node);
            node = nodes.elementAti(index);
            assert(strengthFromNode(node) >= strength);
        } while(isTailoredNode(node) || strengthFromNode(node) > strength);
        assert(weight16FromNode(node) == Collation.COMMON_WEIGHT16);
        return index;
    }

    void
    CollationBuilder.setCaseBits(const UnicodeString &nfdString,
                                  const char *&parserErrorReason) {
        if(U_FAILURE) { return; }
        int numTailoredPrimaries = 0;
        for(int i = 0; i < cesLength; ++i) {
            if(ceStrength(ces[i]) == UCOL_PRIMARY) { ++numTailoredPrimaries; }
        }
        // We should not be able to get too many case bits because
        // cesLength<=31==MAX_EXPANSION_LENGTH.
        // 31 pairs of case bits fit into an long without setting its sign bit.
        assert(numTailoredPrimaries <= 31);

        long cases = 0;
        if(numTailoredPrimaries > 0) {
            const UChar *s = nfdString.getBuffer();
            UTF16CollationIterator baseCEs(baseData, false, s, s, s + nfdString.length());
            int baseCEsLength = baseCEs.fetchCEs - 1;
            if(U_FAILURE) {
                parserErrorReason = "fetching root CEs for tailored string";
                return;
            }
            assert(baseCEsLength >= 0 && baseCEs.getCE(baseCEsLength) == Collation.NO_CE);

            int lastCase = 0;
            int numBasePrimaries = 0;
            for(int i = 0; i < baseCEsLength; ++i) {
                long ce = baseCEs.getCE(i);
                if((ce >>> 32) != 0) {
                    ++numBasePrimaries;
                    int c = ((int)ce >> 14) & 3;
                    assert(c == 0 || c == 2);  // lowercase or uppercase, no mixed case in any base CE
                    if(numBasePrimaries < numTailoredPrimaries) {
                        cases |= (long)c << ((numBasePrimaries - 1) * 2);
                    } else if(numBasePrimaries == numTailoredPrimaries) {
                        lastCase = c;
                    } else if(c != lastCase) {
                        // There are more base primary CEs than tailored primaries.
                        // Set mixed case if the case bits of the remainder differ.
                        lastCase = 1;
                        // Nothing more can change.
                        break;
                    }
                }
            }
            if(numBasePrimaries >= numTailoredPrimaries) {
                cases |= (long)lastCase << ((numTailoredPrimaries - 1) * 2);
            }
        }

        for(int i = 0; i < cesLength; ++i) {
            long ce = ces[i] & 0xffffffffffff3fffL;  // clear old case bits
            int strength = ceStrength(ce);
            if(strength == UCOL_PRIMARY) {
                ce |= (cases & 3) << 14;
                cases >>>= 2;
            } else if(strength == UCOL_TERTIARY) {
                // Tertiary CEs must have uppercase bits.
                // See the LDML spec, and comments in class CollationCompare.
                ce |= 0x8000;
            }
            // Tertiary ignorable CEs must have 0 case bits.
            // We set 0 case bits for secondary CEs too
            // since currently only U+0345 is cased and maps to a secondary CE,
            // and it is lowercase. Other secondaries are uncased.
            // See [[:Cased:]&[:uca1=:]] where uca1 queries the root primary weight.
            ces[i] = ce;
        }
    }

    void
    CollationBuilder.suppressContractions(const UnicodeSet &set, const char *&parserErrorReason,
                                          ) {
        if(U_FAILURE) { return; }
        dataBuilder.suppressContractions(set);
        if(U_FAILURE) {
            parserErrorReason = "application of [suppressContractions [set]] failed";
        }
    }

    void
    CollationBuilder.optimize(const UnicodeSet &set, const char *& /* parserErrorReason */,
                              ) {
        if(U_FAILURE) { return; }
        optimizeSet.addAll(set);
    }

    int
    CollationBuilder.addWithClosure(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                                    const long newCEs[], int newCEsLength, int ce32,
                                    ) {
        // Map from the NFD input to the CEs.
        ce32 = addIfDifferent(nfdPrefix, nfdString, newCEs, newCEsLength, ce32);
        ce32 = addOnlyClosure(nfdPrefix, nfdString, newCEs, newCEsLength, ce32);
        addTailComposites(nfdPrefix, nfdString);
        return ce32;
    }

    int
    CollationBuilder.addOnlyClosure(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                                    const long newCEs[], int newCEsLength, int ce32,
                                    ) {
        if(U_FAILURE) { return ce32; }

        // Map from canonically equivalent input to the CEs. (But not from the all-NFD input.)
        if(nfdPrefix.isEmpty()) {
            CanonicalIterator stringIter(nfdString);
            if(U_FAILURE) { return ce32; }
            UnicodeString prefix;
            for(;;) {
                UnicodeString str = stringIter.next();
                if(str.isBogus()) { break; }
                if(ignoreString(str) || str == nfdString) { continue; }
                ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
                if(U_FAILURE) { return ce32; }
            }
        } else {
            CanonicalIterator prefixIter(nfdPrefix);
            CanonicalIterator stringIter(nfdString);
            if(U_FAILURE) { return ce32; }
            for(;;) {
                UnicodeString prefix = prefixIter.next();
                if(prefix.isBogus()) { break; }
                if(ignorePrefix(prefix)) { continue; }
                boolean samePrefix = prefix == nfdPrefix;
                for(;;) {
                    UnicodeString str = stringIter.next();
                    if(str.isBogus()) { break; }
                    if(ignoreString(str) || (samePrefix && str == nfdString)) { continue; }
                    ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
                    if(U_FAILURE) { return ce32; }
                }
                stringIter.reset();
            }
        }
        return ce32;
    }

    void
    CollationBuilder.addTailComposites(const UnicodeString &nfdPrefix, const UnicodeString &nfdString,
                                        ) {
        if(U_FAILURE) { return; }

        // Look for the last starter in the NFD string.
        int lastStarter;
        int indexAfterLastStarter = nfdString.length();
        for(;;) {
            if(indexAfterLastStarter == 0) { return; }  // no starter at all
            lastStarter = nfdString.char32At(indexAfterLastStarter - 1);
            if(nfd.getCombiningClass(lastStarter) == 0) { break; }
            indexAfterLastStarter -= Character.charCount(lastStarter);
        }
        // No closure to Hangul syllables since we decompose them on the fly.
        if(Hangul.isJamoL(lastStarter)) { return; }

        // Are there any composites whose decomposition starts with the lastStarter?
        // Note: Normalizer2Impl does not currently return start sets for NFC_QC=Maybe characters.
        // We might find some more equivalent mappings here if it did.
        UnicodeSet composites;
        if(!nfcImpl.getCanonStartSet(lastStarter, composites)) { return; }

        UnicodeString decomp;
        UnicodeString newNFDString, newString;
        long newCEs[Collation.MAX_EXPANSION_LENGTH];
        UnicodeSetIterator iter(composites);
        while(iter.next()) {
            assert(!iter.isString());
            int composite = iter.getCodepoint();
            nfd.getDecomposition(composite, decomp);
            if(!mergeCompositeIntoString(nfdString, indexAfterLastStarter, composite, decomp,
                                        newNFDString, newString)) {
                continue;
            }
            int newCEsLength = dataBuilder.getCEs(nfdPrefix, newNFDString, newCEs, 0);
            if(newCEsLength > Collation.MAX_EXPANSION_LENGTH) {
                // Ignore mappings that we cannot store.
                continue;
            }
            // Note: It is possible that the newCEs do not make use of the mapping
            // for which we are adding the tail composites, in which case we might be adding
            // unnecessary mappings.
            // For example, when we add tail composites for ae^ (^=combining circumflex),
            // UCA discontiguous-contraction matching does not find any matches
            // for ae_^ (_=any combining diacritic below) *unless* there is also
            // a contraction mapping for ae.
            // Thus, if there is no ae contraction, then the ae^ mapping is ignored
            // while fetching the newCEs for ae_^.
            // TODO: Try to detect this effectively.
            // (Alternatively, print a warning when prefix contractions are missing.)

            // We do not need an explicit mapping for the NFD strings.
            // It is fine if the NFD input collates like this via a sequence of mappings.
            // It also saves a little bit of space, and may reduce the set of characters with contractions.
            int ce32 = addIfDifferent(nfdPrefix, newString,
                                          newCEs, newCEsLength, Collation.UNASSIGNED_CE32);
            if(ce32 != Collation.UNASSIGNED_CE32) {
                // was different, was added
                addOnlyClosure(nfdPrefix, newNFDString, newCEs, newCEsLength, ce32);
            }
        }
    }

    boolean
    CollationBuilder.mergeCompositeIntoString(const UnicodeString &nfdString,
                                              int indexAfterLastStarter,
                                              int composite, const UnicodeString &decomp,
                                              UnicodeString &newNFDString, UnicodeString &newString,
                                              ) {
        if(U_FAILURE) { return false; }
        assert(nfdString.char32At(indexAfterLastStarter - 1) == decomp.char32At(0));
        int lastStarterLength = decomp.offsetByCodePoints(0, 1);
        if(lastStarterLength == decomp.length()) {
            // Singleton decompositions should be found by addWithClosure()
            // and the CanonicalIterator, so we can ignore them here.
            return false;
        }
        if(nfdString.compare(indexAfterLastStarter, 0x7fffffff,
                            decomp, lastStarterLength, 0x7fffffff) == 0) {
            // same strings, nothing new to be found here
            return false;
        }

        // Make new FCD strings that combine a composite, or its decomposition,
        // into the nfdString's last starter and the combining marks following it.
        // Make an NFD version, and a version with the composite.
        newNFDString.setTo(nfdString, 0, indexAfterLastStarter);
        newString.setTo(nfdString, 0, indexAfterLastStarter - lastStarterLength).append(composite);

        // The following is related to discontiguous contraction matching,
        // but builds only FCD strings (or else returns false).
        int sourceIndex = indexAfterLastStarter;
        int decompIndex = lastStarterLength;
        // Small optimization: We keep the source character across loop iterations
        // because we do not always consume it,
        // and then need not fetch it again nor look up its combining class again.
        int sourceChar = Collation.SENTINEL_CP;
        // The cc variables need to be declared before the loop so that at the end
        // they are set to the last combining classes seen.
        uint8_t sourceCC = 0;
        uint8_t decompCC = 0;
        for(;;) {
            if(sourceChar < 0) {
                if(sourceIndex >= nfdString.length()) { break; }
                sourceChar = nfdString.char32At(sourceIndex);
                sourceCC = nfd.getCombiningClass(sourceChar);
                assert(sourceCC != 0);
            }
            // We consume a decomposition character in each iteration.
            if(decompIndex >= decomp.length()) { break; }
            int decompChar = decomp.char32At(decompIndex);
            decompCC = nfd.getCombiningClass(decompChar);
            // Compare the two characters and their combining classes.
            if(decompCC == 0) {
                // Unable to merge because the source contains a non-zero combining mark
                // but the composite's decomposition contains another starter.
                // The strings would not be equivalent.
                return false;
            } else if(sourceCC < decompCC) {
                // Composite + sourceChar would not be FCD.
                return false;
            } else if(decompCC < sourceCC) {
                newNFDString.append(decompChar);
                decompIndex += Character.charCount(decompChar);
            } else if(decompChar != sourceChar) {
                // Blocked because same combining class.
                return false;
            } else {  // match: decompChar == sourceChar
                newNFDString.append(decompChar);
                decompIndex += Character.charCount(decompChar);
                sourceIndex += Character.charCount(decompChar);
                sourceChar = Collation.SENTINEL_CP;
            }
        }
        // We are at the end of at least one of the two inputs.
        if(sourceChar >= 0) {  // more characters from nfdString but not from decomp
            if(sourceCC < decompCC) {
                // Appending the next source character to the composite would not be FCD.
                return false;
            }
            newNFDString.append(nfdString, sourceIndex, 0x7fffffff);
            newString.append(nfdString, sourceIndex, 0x7fffffff);
        } else if(decompIndex < decomp.length()) {  // more characters from decomp, not from nfdString
            newNFDString.append(decomp, decompIndex, 0x7fffffff);
        }
        assert(nfd.isNormalized(newNFDString));
        assert(fcd.isNormalized(newString));
        assert(nfd.normalize(newString) == newNFDString);  // canonically equivalent
        return true;
    }

    boolean
    CollationBuilder.ignorePrefix(const UnicodeString &s) {
        // Do not map non-FCD prefixes.
        return !isFCD(s);
    }

    boolean
    CollationBuilder.ignoreString(const UnicodeString &s) {
        // Do not map non-FCD strings.
        // Do not map strings that start with Hangul syllables: We decompose those on the fly.
        return !isFCD(s) || Hangul.isHangul(s.charAt(0));
    }

    boolean
    CollationBuilder.isFCD(const UnicodeString &s) {
        return fcd.isNormalized(s);
    }

    void
    CollationBuilder.closeOverComposites() {
        UnicodeSet composites(UNICODE_STRING_SIMPLE("[:NFD_QC=N:]"));  // Java: static final
        if(U_FAILURE) { return; }
        // Hangul is decomposed on the fly during collation.
        composites.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
        UnicodeString prefix;  // empty
        UnicodeString nfdString;
        UnicodeSetIterator iter(composites);
        while(iter.next()) {
            assert(!iter.isString());
            nfd.getDecomposition(iter.getCodepoint(), nfdString);
            cesLength = dataBuilder.getCEs(nfdString, ces, 0);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                // Too many CEs from the decomposition (unusual), ignore this composite.
                // We could add a capacity parameter to getCEs() and reallocate if necessary.
                // However, this can only really happen in contrived cases.
                continue;
            }
            const UnicodeString &composite(iter.getString());
            addIfDifferent(prefix, composite, ces, cesLength, Collation.UNASSIGNED_CE32);
        }
    }

    int
    CollationBuilder.addIfDifferent(const UnicodeString &prefix, const UnicodeString &str,
                                    const long newCEs[], int newCEsLength, int ce32,
                                    ) {
        if(U_FAILURE) { return ce32; }
        long oldCEs[Collation.MAX_EXPANSION_LENGTH];
        int oldCEsLength = dataBuilder.getCEs(prefix, str, oldCEs, 0);
        if(!sameCEs(newCEs, newCEsLength, oldCEs, oldCEsLength)) {
            if(ce32 == Collation.UNASSIGNED_CE32) {
                ce32 = dataBuilder.encodeCEs(newCEs, newCEsLength);
            }
            dataBuilder.addCE32(prefix, str, ce32);
        }
        return ce32;
    }

    boolean
    CollationBuilder.sameCEs(const long ces1[], int ces1Length,
                              const long ces2[], int ces2Length) {
        if(ces1Length != ces2Length) {
            return false;
        }
        assert(ces1Length <= Collation.MAX_EXPANSION_LENGTH);
        for(int i = 0; i < ces1Length; ++i) {
            if(ces1[i] != ces2[i]) { return false; }
        }
        return true;
    }

    #ifdef DEBUG_COLLATION_BUILDER

    int
    alignWeightRight(int w) {
        if(w != 0) {
            while((w & 0xff) == 0) { w >>>= 8; }
        }
        return w;
    }

    #endif

    void
    CollationBuilder.makeTailoredCEs() {
        if(U_FAILURE) { return; }

        CollationWeights primaries, secondaries, tertiaries;
        long *nodesArray = nodes.getBuffer();

        for(int rpi = 0; rpi < rootPrimaryIndexes.size(); ++rpi) {
            int i = rootPrimaryIndexes.elementAti(rpi);
            long node = nodesArray[i];
            long p = weight32FromNode(node);
            int s = p == 0 ? 0 : Collation.COMMON_WEIGHT16;
            int t = s;
            int q = 0;
            boolean pIsTailored = false;
            boolean sIsTailored = false;
            boolean tIsTailored = false;
    #ifdef DEBUG_COLLATION_BUILDER
            printf("\nprimary     %lx\n", (long)alignWeightRight(p));
    #endif
            int pIndex = p == 0 ? 0 : rootElements.findPrimary(p);
            int nextIndex = nextIndexFromNode(node);
            while(nextIndex != 0) {
                i = nextIndex;
                node = nodesArray[i];
                nextIndex = nextIndexFromNode(node);
                int strength = strengthFromNode(node);
                if(strength == UCOL_QUATERNARY) {
                    assert(isTailoredNode(node));
    #ifdef DEBUG_COLLATION_BUILDER
                    printf("      quat+     ");
    #endif
                    if(q == 3) {
                        errorCode = U_BUFFER_OVERFLOW_ERROR;
                        errorReason = "quaternary tailoring gap too small";
                        return;
                    }
                    ++q;
                } else {
                    if(strength == UCOL_TERTIARY) {
                        if(isTailoredNode(node)) {
    #ifdef DEBUG_COLLATION_BUILDER
                            printf("    ter+        ");
    #endif
                            if(!tIsTailored) {
                                // First tailored tertiary node for [p, s].
                                int tCount = countTailoredNodes(nodesArray, nextIndex,
                                                                    UCOL_TERTIARY) + 1;
                                int tLimit;
                                if(t == 0) {
                                    // Gap at the beginning of the tertiary CE range.
                                    t = rootElements.getTertiaryBoundary() - 0x100;
                                    tLimit = rootElements.getFirstTertiaryCE() & Collation.ONLY_TERTIARY_MASK;
                                } else if(t == BEFORE_WEIGHT16) {
                                    tLimit = Collation.COMMON_WEIGHT16;
                                } else if(!pIsTailored && !sIsTailored) {
                                    // p and s are root weights.
                                    tLimit = rootElements.getTertiaryAfter(pIndex, s, t);
                                } else {
                                    // [p, s] is tailored.
                                    assert(t == Collation.COMMON_WEIGHT16);
                                    tLimit = rootElements.getTertiaryBoundary();
                                }
                                assert(tLimit == 0x4000 || (tLimit & ~Collation.ONLY_TERTIARY_MASK) == 0);
                                tertiaries.initForTertiary();
                                if(!tertiaries.allocWeights(t, tLimit, tCount)) {
                                    errorCode = U_BUFFER_OVERFLOW_ERROR;
                                    errorReason = "tertiary tailoring gap too small";
                                    return;
                                }
                                tIsTailored = true;
                            }
                            t = tertiaries.nextWeight();
                            assert(t != 0xffffffff);
                        } else {
                            t = weight16FromNode(node);
                            tIsTailored = false;
    #ifdef DEBUG_COLLATION_BUILDER
                            printf("    ter     %lx\n", (long)alignWeightRight(t));
    #endif
                        }
                    } else {
                        if(strength == UCOL_SECONDARY) {
                            if(isTailoredNode(node)) {
    #ifdef DEBUG_COLLATION_BUILDER
                                printf("  sec+          ");
    #endif
                                if(!sIsTailored) {
                                    // First tailored secondary node for p.
                                    int sCount = countTailoredNodes(nodesArray, nextIndex,
                                                                        UCOL_SECONDARY) + 1;
                                    int sLimit;
                                    if(s == 0) {
                                        // Gap at the beginning of the secondary CE range.
                                        s = rootElements.getSecondaryBoundary() - 0x100;
                                        sLimit = (int)(rootElements.getFirstSecondaryCE() >> 16);
                                    } else if(s == BEFORE_WEIGHT16) {
                                        sLimit = Collation.COMMON_WEIGHT16;
                                    } else if(!pIsTailored) {
                                        // p is a root primary.
                                        sLimit = rootElements.getSecondaryAfter(pIndex, s);
                                    } else {
                                        // p is a tailored primary.
                                        assert(s == Collation.COMMON_WEIGHT16);
                                        sLimit = rootElements.getSecondaryBoundary();
                                    }
                                    if(s == Collation.COMMON_WEIGHT16) {
                                        // Do not tailor into the getSortKey() range of
                                        // compressed common secondaries.
                                        s = rootElements.getLastCommonSecondary();
                                    }
                                    secondaries.initForSecondary();
                                    if(!secondaries.allocWeights(s, sLimit, sCount)) {
                                        errorCode = U_BUFFER_OVERFLOW_ERROR;
                                        errorReason = "secondary tailoring gap too small";
                                        return;
                                    }
                                    sIsTailored = true;
                                }
                                s = secondaries.nextWeight();
                                assert(s != 0xffffffff);
                            } else {
                                s = weight16FromNode(node);
                                sIsTailored = false;
    #ifdef DEBUG_COLLATION_BUILDER
                                printf("  sec       %lx\n", (long)alignWeightRight(s));
    #endif
                            }
                        } else /* UCOL_PRIMARY */ {
                            assert(isTailoredNode(node));
    #ifdef DEBUG_COLLATION_BUILDER
                            printf("pri+            ");
    #endif
                            if(!pIsTailored) {
                                // First tailored primary node in this list.
                                int pCount = countTailoredNodes(nodesArray, nextIndex,
                                                                    UCOL_PRIMARY) + 1;
                                boolean isCompressible = baseData.isCompressiblePrimary(p);
                                long pLimit =
                                    rootElements.getPrimaryAfter(p, pIndex, isCompressible);
                                primaries.initForPrimary(isCompressible);
                                if(!primaries.allocWeights(p, pLimit, pCount)) {
                                    errorCode = U_BUFFER_OVERFLOW_ERROR;  // TODO: introduce a more specific UErrorCode?
                                    errorReason = "primary tailoring gap too small";
                                    return;
                                }
                                pIsTailored = true;
                            }
                            p = primaries.nextWeight();
                            assert(p != 0xffffffff);
                            s = Collation.COMMON_WEIGHT16;
                            sIsTailored = false;
                        }
                        t = s == 0 ? 0 : Collation.COMMON_WEIGHT16;
                        tIsTailored = false;
                    }
                    q = 0;
                }
                if(isTailoredNode(node)) {
                    nodesArray[i] = Collation.makeCE(p, s, t, q);
    #ifdef DEBUG_COLLATION_BUILDER
                    printf("%016llx\n", (long long)nodesArray[i]);
    #endif
                }
            }
        }
    }

    int32_t
    CollationBuilder.countTailoredNodes(const long *nodesArray, int i, int strength) {
        int count = 0;
        for(;;) {
            if(i == 0) { break; }
            long node = nodesArray[i];
            if(strengthFromNode(node) < strength) { break; }
            if(strengthFromNode(node) == strength) {
                if(isTailoredNode(node)) {
                    ++count;
                } else {
                    break;
                }
            }
            i = nextIndexFromNode(node);
        }
        return count;
    }

    class CEFinalizer implements CollationDataBuilder.CEModifier {
    public:
        CEFinalizer(const long *ces) : finalCEs(ces) {}
        virtual ~CEFinalizer();
        virtual long modifyCE32(int ce32) {
            assert(!Collation.isSpecialCE32(ce32));
            if(CollationBuilder.isTempCE32(ce32)) {
                // retain case bits
                return finalCEs[CollationBuilder.indexFromTempCE32(ce32)] | ((ce32 & 0xc0) << 8);
            } else {
                return Collation.NO_CE;
            }
        }
        virtual long modifyCE(long ce) {
            if(CollationBuilder.isTempCE(ce)) {
                // retain case bits
                return finalCEs[CollationBuilder.indexFromTempCE(ce)] | (ce & 0xc000);
            } else {
                return Collation.NO_CE;
            }
        }

    private:
        const long *finalCEs;
    };

    CEFinalizer.~CEFinalizer() {}

    void
    CollationBuilder.finalizeCEs() {
        if(U_FAILURE) { return; }
        LocalPointer<CollationDataBuilder> newBuilder(new CollationDataBuilder);
        if(newBuilder.isNull()) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        newBuilder.initForTailoring(baseData);
        CEFinalizer finalizer(nodes.getBuffer());
        newBuilder.copyFrom(*dataBuilder, finalizer);
        if(U_FAILURE) { return; }
        delete dataBuilder;
        dataBuilder = newBuilder.orphan();
    }

    int32_t
    CollationBuilder.ceStrength(long ce) {
        return
            isTempCE(ce) ? strengthFromTempCE(ce) :
            (ce & 0xff00000000000000L) != 0 ? UCOL_PRIMARY :
            ((int)ce & 0xff000000) != 0 ? UCOL_SECONDARY :
            ce != 0 ? UCOL_TERTIARY :
            UCOL_IDENTICAL;
    }

    U_CAPI UCollator * U_EXPORT2
    ucol_openRules(const UChar *rules, int rulesLength,
                  UColAttributeValue normalizationMode, UCollationStrength strength,
                  UParseError *parseError, UErrorCode *pErrorCode) {
        if(U_FAILURE(*pErrorCode)) { return null; }
        if(rules == null && rulesLength != 0) {
            *pErrorCode = U_ILLEGAL_ARGUMENT_ERROR;
            return null;
        }
        RuleBasedCollator *coll = new RuleBasedCollator();
        if(coll == null) {
            *pErrorCode = U_MEMORY_ALLOCATION_ERROR;
            return null;
        }
        UnicodeString r((boolean)(rulesLength < 0), rules, rulesLength);
        coll.internalBuildTailoring(r, strength, normalizationMode, parseError, null, *pErrorCode);
        if(U_FAILURE(*pErrorCode)) {
            delete coll;
            return null;
        }
        return coll.toUCollator();
    }

    private static final int internalBufferSize = 512;

    // The @internal ucol_getUnsafeSet() was moved here from ucol_sit.cpp
    // because it calls UnicodeSet "builder" code that depends on all Unicode properties,
    // and the rest of the collation "runtime" code only depends on normalization.
    // This function is not related to the collation builder,
    // but it did not seem worth moving it into its own .cpp file,
    // nor rewriting it to use lower-level UnicodeSet and Normalizer2Impl methods.
    U_CAPI int U_EXPORT2
    ucol_getUnsafeSet( const UCollator *coll,
                      USet *unsafe,
                      UErrorCode *status)
    {
        UChar buffer[internalBufferSize];
        int len = 0;

        uset_clear(unsafe);

        // cccpattern = "[[:^tccc=0:][:^lccc=0:]]", unfortunately variant
        private static final UChar cccpattern[25] = { 0x5b, 0x5b, 0x3a, 0x5e, 0x74, 0x63, 0x63, 0x63, 0x3d, 0x30, 0x3a, 0x5d,
                                        0x5b, 0x3a, 0x5e, 0x6c, 0x63, 0x63, 0x63, 0x3d, 0x30, 0x3a, 0x5d, 0x5d, 0x00 };

        // add chars that fail the fcd check
        uset_applyPattern(unsafe, cccpattern, 24, USET_IGNORE_SPACE, status);

        // add lead/trail surrogates
        // (trail surrogates should need to be unsafe only if the caller tests for UTF-16 code *units*,
        // not when testing code *points*)
        uset_addRange(unsafe, 0xd800, 0xdfff);

        USet *contractions = uset_open(0,0);

        int i = 0, j = 0;
        int contsSize = ucol_getContractions(coll, contractions, status);
        int c = 0;
        // Contraction set consists only of strings
        // to get unsafe code points, we need to
        // break the strings apart and add them to the unsafe set
        for(i = 0; i < contsSize; i++) {
            len = uset_getItem(contractions, i, null, null, buffer, internalBufferSize, status);
            if(len > 0) {
                j = 0;
                while(j < len) {
                    U16_NEXT(buffer, j, len, c);
                    if(j < len) {
                        uset_add(unsafe, c);
                    }
                }
            }
        }

        uset_close(contractions);

        return uset_size(unsafe);
    }
