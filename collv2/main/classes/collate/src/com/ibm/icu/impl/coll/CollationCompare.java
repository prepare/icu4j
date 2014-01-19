/*
*******************************************************************************
* Copyright (C) 1996-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationcompare.h
*
* @since 2012feb14 with new and old collation code
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

final class CollationCompare /* all methods are static */ {
public:
    static UCollationResult compareUpToQuaternary(CollationIterator &left, CollationIterator &right,
                                                  const CollationSettings &settings,
                                                  );
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONCOMPARE_H__
/*
*******************************************************************************
* Copyright (C) 1996-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationcompare.cpp
*
* @since 2012feb14 with new and old collation code
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/ucol.h"
#include "cmemory.h"
#include "collation.h"
#include "collationcompare.h"
#include "collationiterator.h"
#include "collationsettings.h"
#include "uassert.h"

U_NAMESPACE_BEGIN

UCollationResult
CollationCompare.compareUpToQuaternary(CollationIterator &left, CollationIterator &right,
                                        const CollationSettings &settings,
                                        ) {
    if(U_FAILURE) { return UCOL_EQUAL; }

    int options = settings.options;
    long variableTop;
    if((options & CollationSettings.ALTERNATE_MASK) == 0) {
        variableTop = 0;
    } else {
        // +1 so that we can use "<" and primary ignorables test out early.
        variableTop = settings.variableTop + 1;
    }
    boolean anyVariable = false;

    // Fetch CEs, compare primaries, store secondary & tertiary weights.
    U_ALIGN_CODE(16);
    for(;;) {
        // We fetch CEs until we get a non-ignorable primary or reach the end.
        long leftPrimary;
        do {
            long ce = left.nextCE;
            leftPrimary = ce >>> 32;
            if(leftPrimary < variableTop && leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                // Variable CE, shift it to quaternary level.
                // Ignore all following primary ignorables, and shift further variable CEs.
                anyVariable = true;
                do {
                    // Store only the primary of the variable CE.
                    left.setCurrentCE(ce & 0xffffffff00000000L);
                    for(;;) {
                        ce = left.nextCE;
                        leftPrimary = ce >>> 32;
                        if(leftPrimary == 0) {
                            left.setCurrentCE(0);
                        } else {
                            break;
                        }
                    }
                } while(leftPrimary < variableTop &&
                        leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
            }
        } while(leftPrimary == 0);

        long rightPrimary;
        do {
            long ce = right.nextCE;
            rightPrimary = ce >>> 32;
            if(rightPrimary < variableTop && rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                // Variable CE, shift it to quaternary level.
                // Ignore all following primary ignorables, and shift further variable CEs.
                anyVariable = true;
                do {
                    // Store only the primary of the variable CE.
                    right.setCurrentCE(ce & 0xffffffff00000000L);
                    for(;;) {
                        ce = right.nextCE;
                        rightPrimary = ce >>> 32;
                        if(rightPrimary == 0) {
                            right.setCurrentCE(0);
                        } else {
                            break;
                        }
                    }
                } while(rightPrimary < variableTop &&
                        rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
            }
        } while(rightPrimary == 0);

        if(leftPrimary != rightPrimary) {
            // Return the primary difference, with script reordering.
            const uint8_t *reorderTable = settings.reorderTable;
            if (reorderTable != null) {
                leftPrimary = Collation.reorder(reorderTable, leftPrimary);
                rightPrimary = Collation.reorder(reorderTable, rightPrimary);
            }
            return (leftPrimary < rightPrimary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftPrimary == Collation.NO_CE_PRIMARY) { break; }
    }
    if(U_FAILURE) { return UCOL_EQUAL; }

    // Compare the buffered secondary & tertiary weights.
    // We might skip the secondary level but continue with the case level
    // which is turned on separately.
    if(CollationSettings.getStrength(options) >= UCOL_SECONDARY) {
        if((options & CollationSettings.BACKWARD_SECONDARY) == 0) {
            int leftIndex = 0;
            int rightIndex = 0;
            for(;;) {
                int leftSecondary;
                do {
                    leftSecondary = ((int)left.getCE(leftIndex++)) >>> 16;
                } while(leftSecondary == 0);

                int rightSecondary;
                do {
                    rightSecondary = ((int)right.getCE(rightIndex++)) >>> 16;
                } while(rightSecondary == 0);

                if(leftSecondary != rightSecondary) {
                    return (leftSecondary < rightSecondary) ? UCOL_LESS : UCOL_GREATER;
                }
                if(leftSecondary == Collation.NO_CE_WEIGHT16) { break; }
            }
        } else {
            // The backwards secondary level compares secondary weights backwards
            // within segments separated by the merge separator (U+FFFE, weight 02).
            int leftStart = 0;
            int rightStart = 0;
            for(;;) {
                // Find the merge separator or the NO_CE terminator.
                int leftLimit = leftStart;
                long leftLower32;
                while((leftLower32 = left.getCE(leftLimit) & 0xffffffffL) >
                            Collation.MERGE_SEPARATOR_LOWER32 ||
                        leftLower32 == 0) {
                    ++leftLimit;
                }
                int rightLimit = rightStart;
                long rightLower32;
                while((rightLower32 = right.getCE(rightLimit) & 0xffffffffL) >
                            Collation.MERGE_SEPARATOR_LOWER32 ||
                        rightLower32 == 0) {
                    ++rightLimit;
                }

                // Compare the segments.
                int leftIndex = leftLimit;
                int rightIndex = rightLimit;
                for(;;) {
                    int leftSecondary = 0;
                    while(leftSecondary == 0 && leftIndex > leftStart) {
                        leftSecondary = ((int)left.getCE(--leftIndex)) >>> 16;
                    }

                    int rightSecondary = 0;
                    while(rightSecondary == 0 && rightIndex > rightStart) {
                        rightSecondary = ((int)right.getCE(--rightIndex)) >>> 16;
                    }

                    if(leftSecondary != rightSecondary) {
                        return (leftSecondary < rightSecondary) ? UCOL_LESS : UCOL_GREATER;
                    }
                    if(leftSecondary == 0) { break; }
                }

                // Did we reach the end of either string?
                // Both strings have the same number of merge separators,
                // or else there would have been a primary-level difference.
                assert(left.getCE(leftLimit) == right.getCE(rightLimit));
                if(left.getCE(leftLimit) == Collation.NO_CE) { break; }
                // Skip both merge separators and continue.
                leftStart = leftLimit + 1;
                rightStart = rightLimit + 1;
            }
        }
    }

    if((options & CollationSettings.CASE_LEVEL) != 0) {
        int strength = CollationSettings.getStrength(options);
        int leftIndex = 0;
        int rightIndex = 0;
        for(;;) {
            int leftCase, leftLower32, rightCase;
            if(strength == UCOL_PRIMARY) {
                // Primary+caseLevel: Ignore case level weights of primary ignorables.
                // Otherwise we would get a-umlaut > a
                // which is not desirable for accent-insensitive sorting.
                // Check for (lower 32 bits) == 0 as well because variable CEs are stored
                // with only primary weights.
                long ce;
                do {
                    ce = left.getCE(leftIndex++);
                    leftCase = (int)ce;
                } while((ce >>> 32) == 0 || leftCase == 0);
                leftLower32 = leftCase;
                leftCase &= 0xc000;

                do {
                    ce = right.getCE(rightIndex++);
                    rightCase = (int)ce;
                } while((ce >>> 32) == 0 || rightCase == 0);
                rightCase &= 0xc000;
            } else {
                // Secondary+caseLevel: By analogy with the above,
                // ignore case level weights of secondary ignorables.
                //
                // Note: A tertiary CE has uppercase case bits (0.0.ut)
                // to keep tertiary+caseFirst well-formed.
                //
                // Tertiary+caseLevel: Also ignore case level weights of secondary ignorables.
                // Otherwise a tertiary CE's uppercase would be no greater than
                // a primary/secondary CE's uppercase.
                // (See UCA well-formedness condition 2.)
                // We could construct a special case weight higher than uppercase,
                // but it's simpler to always ignore case weights of secondary ignorables,
                // turning 0.0.ut into 0.0.0.t.
                // (See LDML Collation, Case Parameters.)
                do {
                    leftCase = (int)left.getCE(leftIndex++);
                } while((leftCase & 0xffff0000) == 0);
                leftLower32 = leftCase;
                leftCase &= 0xc000;

                do {
                    rightCase = (int)right.getCE(rightIndex++);
                } while((rightCase & 0xffff0000) == 0);
                rightCase &= 0xc000;
            }

            // No need to handle NO_CE and MERGE_SEPARATOR specially:
            // There is one case weight for each previous-level weight,
            // so level length differences were handled there.
            if(leftCase != rightCase) {
                if((options & CollationSettings.UPPER_FIRST) == 0) {
                    return (leftCase < rightCase) ? UCOL_LESS : UCOL_GREATER;
                } else {
                    return (leftCase < rightCase) ? UCOL_GREATER : UCOL_LESS;
                }
            }
            if((leftLower32 >>> 16) == Collation.NO_CE_WEIGHT16) { break; }
        }
    }
    if(CollationSettings.getStrength(options) <= UCOL_SECONDARY) { return UCOL_EQUAL; }

    int tertiaryMask = CollationSettings.getTertiaryMask(options);

    int leftIndex = 0;
    int rightIndex = 0;
    int anyQuaternaries = 0;
    for(;;) {
        int leftLower32, leftTertiary;
        do {
            leftLower32 = (int)left.getCE(leftIndex++);
            anyQuaternaries |= leftLower32;
            assert((leftLower32 & Collation.ONLY_TERTIARY_MASK) != 0 ||
                     (leftLower32 & 0xc0c0) == 0);
            leftTertiary = leftLower32 & tertiaryMask;
        } while(leftTertiary == 0);

        int rightLower32, rightTertiary;
        do {
            rightLower32 = (int)right.getCE(rightIndex++);
            anyQuaternaries |= rightLower32;
            assert((rightLower32 & Collation.ONLY_TERTIARY_MASK) != 0 ||
                     (rightLower32 & 0xc0c0) == 0);
            rightTertiary = rightLower32 & tertiaryMask;
        } while(rightTertiary == 0);

        if(leftTertiary != rightTertiary) {
            if(CollationSettings.sortsTertiaryUpperCaseFirst(options)) {
                // Pass through NO_CE and MERGE_SEPARATOR
                // and keep real tertiary weights larger than the MERGE_SEPARATOR.
                // Do not change the artificial uppercase weight of a tertiary CE (0.0.ut),
                // to keep tertiary CEs well-formed.
                // Their case+tertiary weights must be greater than those of
                // primary and secondary CEs.
                if(leftTertiary > Collation.MERGE_SEPARATOR_WEIGHT16) {
                    if((leftLower32 & 0xffff0000) != 0) {
                        leftTertiary ^= 0xc000;
                    } else {
                        leftTertiary += 0x4000;
                    }
                }
                if(rightTertiary > Collation.MERGE_SEPARATOR_WEIGHT16) {
                    if((rightLower32 & 0xffff0000) != 0) {
                        rightTertiary ^= 0xc000;
                    } else {
                        rightTertiary += 0x4000;
                    }
                }
            }
            return (leftTertiary < rightTertiary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftTertiary == Collation.NO_CE_WEIGHT16) { break; }
    }
    if(CollationSettings.getStrength(options) <= UCOL_TERTIARY) { return UCOL_EQUAL; }

    if(!anyVariable && (anyQuaternaries & 0xc0) == 0) {
        // If there are no "variable" CEs and no non-zero quaternary weights,
        // then there are no quaternary differences.
        return UCOL_EQUAL;
    }

    leftIndex = 0;
    rightIndex = 0;
    for(;;) {
        long leftQuaternary;
        do {
            long ce = left.getCE(leftIndex++);
            leftQuaternary = ce & 0xffff;
            if(leftQuaternary == 0) {
                // Variable primary or completely ignorable.
                leftQuaternary = ce >>> 32;
            } else if(leftQuaternary <= Collation.MERGE_SEPARATOR_WEIGHT16) {
                // Leave NO_CE or MERGE_SEPARATOR as is.
            } else {
                // Regular CE, not tertiary ignorable.
                // Preserve the quaternary weight in bits 7..6.
                leftQuaternary |= 0xffffff3fL;
            }
        } while(leftQuaternary == 0);

        long rightQuaternary;
        do {
            long ce = right.getCE(rightIndex++);
            rightQuaternary = ce & 0xffff;
            if(rightQuaternary == 0) {
                // Variable primary or completely ignorable.
                rightQuaternary = ce >>> 32;
            } else if(rightQuaternary <= Collation.MERGE_SEPARATOR_WEIGHT16) {
                // Leave NO_CE or MERGE_SEPARATOR as is.
            } else {
                // Regular CE, not tertiary ignorable.
                // Preserve the quaternary weight in bits 7..6.
                rightQuaternary |= 0xffffff3fL;
            }
        } while(rightQuaternary == 0);

        if(leftQuaternary != rightQuaternary) {
            // Return the difference, with script reordering.
            const uint8_t *reorderTable = settings.reorderTable;
            if (reorderTable != null) {
                leftQuaternary = Collation.reorder(reorderTable, leftQuaternary);
                rightQuaternary = Collation.reorder(reorderTable, rightQuaternary);
            }
            return (leftQuaternary < rightQuaternary) ? UCOL_LESS : UCOL_GREATER;
        }
        if(leftQuaternary == Collation.NO_CE_WEIGHT16) { break; }
    }
    return UCOL_EQUAL;
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
