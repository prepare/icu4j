/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationFCD.java, ported from collationfcd.h/.cpp
*
* @since 2012aug18
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import com.ibm.icu.text.UTF16;

/**
 * Data and functions for the FCD check fast path.
 *
 * The fast path looks at a pair of 16-bit code units and checks
 * whether there is an FCD boundary between them;
 * there is if the first unit has a trailing ccc=0 (!hasTccc(first))
 * or the second unit has a leading ccc=0 (!hasLccc(second)),
 * or both.
 * When the fast path finds a possible non-boundary,
 * then the FCD check slow path looks at the actual sequence of FCD values.
 *
 * This is a pure optimization.
 * The fast path must at least find all possible non-boundaries.
 * If the fast path is too pessimistic, it costs performance.
 *
 * For a pair of BMP characters, the fast path tests are precise (1 bit per character).
 *
 * For a supplementary code point, the two units are its lead and trail surrogates.
 * We set hasTccc(lead)=true if any of its 1024 associated supplementary code points
 * has lccc!=0 or tccc!=0.
 * We set hasLccc(trail)=true for all trail surrogates.
 * As a result, we leave the fast path if the lead surrogate might start a
 * supplementary code point that is not FCD-inert.
 * (So the fast path need not detect that there is a surrogate pair,
 * nor look ahead to the next full code point.)
 *
 * hasLccc(lead)=true if any of its 1024 associated supplementary code points
 * has lccc!=0, for fast boundary checking between BMP & supplementary.
 *
 * hasTccc(trail)=false:
 * It should only be tested for unpaired trail surrogates which are FCD-inert.
 */
final class CollationFCD {
    static boolean hasLccc(int c) {
        assert c <= 0xffff;
        // c can be negative, e.g., Collation.SENTINEL_CP from UCharIterator;
        // that is handled in the first test.
        int i;
        return
            // U+0300 is the first character with lccc!=0.
            c >= 0x300 &&
            (i = lcccIndex[c >> 5]) != 0 &&
            (lcccBits[i] & (1 << (c & 0x1f))) != 0;
    }

    static boolean hasTccc(int c) {
        assert c <= 0xffff;
        // c can be negative, e.g., Collation.SENTINEL_CP from UCharIterator;
        // that is handled in the first test.
        int i;
        return
            // U+00C0 is the first character with tccc!=0.
            c >= 0xc0 &&
            (i = tcccIndex[c >> 5]) != 0 &&
            (tcccBits[i] & (1 << (c & 0x1f))) != 0;
    }

    static boolean mayHaveLccc(int c) {
        // Handles all of Unicode 0..10FFFF.
        // c can be negative, e.g., Collation.SENTINEL_CP.
        // U+0300 is the first character with lccc!=0.
        if(c < 0x300) { return false; }
        if(c > 0xffff) { c = UTF16.getLeadSurrogate(c); }
        int i;
        return
            (i = lcccIndex[c >> 5]) != 0 &&
            (lcccBits[i] & (1 << (c & 0x1f))) != 0;
    }

    /**
     * Tibetan composite vowel signs (U+0F73, U+0F75, U+0F81)
     * must be decomposed before reaching the core collation code,
     * or else some sequences including them, even ones passing the FCD check,
     * do not yield canonically equivalent results.
     *
     * This is a fast and imprecise test.
     *
     * @param c a code point
     * @return true if c is U+0F73, U+0F75 or U+0F81 or one of several other Tibetan characters
     */
    static boolean maybeTibetanCompositeVowel(int c) {
        return (c & 0x1fff01) == 0xf01;
    }

    /**
     * Tibetan composite vowel signs (U+0F73, U+0F75, U+0F81)
     * must be decomposed before reaching the core collation code,
     * or else some sequences including them, even ones passing the FCD check,
     * do not yield canonically equivalent results.
     *
     * They have distinct lccc/tccc combinations: 129/130 or 129/132.
     *
     * @param fcd16 the FCD value (lccc/tccc combination) of a code point
     * @return true if fcd16 is from U+0F73, U+0F75 or U+0F81
     */
    static boolean isFCD16OfTibetanCompositeVowel(char fcd16) {
        return fcd16 == 0x8182 || fcd16 == 0x8184;
    }

    // CollationFCD();  // No instantiation.

    // TODO: machine-generate by: icu/tools/unicode/c/genuca/genuca.cpp

    private static final byte[] lcccIndex={
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,1,1,2,3,0,0,0,0,
0,0,0,0,4,0,0,0,0,0,0,0,5,6,7,0,
8,0,9,0xa,0,0,0xb,0xc,0xd,0xe,0xf,0,0,0,0,0x10,
0x11,0x12,0x13,0,0,0,0,0x14,0,0x15,0x16,0,0,0x15,0x17,0,
0,0x15,0x17,0,0,0x15,0x17,0,0,0x15,0x17,0,0,0,0x17,0,
0,0,0x18,0,0,0x15,0x17,0,0,0,0x17,0,0,0,0x19,0,
0,0x1a,0x1b,0,0,0x1c,0x1b,0,0x1c,0x1d,0,0x1e,0x1f,0,0x20,0,
0,0x21,0,0,0x17,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0x22,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0x23,0x23,0,0,0,0,0x24,0,
0,0,0,0,0,0x25,0,0,0,0x13,0,0,0,0,0,0,
0x26,0,0,0x27,0,0,0,0,0,0x23,0x28,0x10,0,0x29,0,0x2a,
0,0x2b,0,0,0,0,0x2c,0x2d,0,0,0,0,0,0,1,0x2e,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0x2f,0x30,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0x31,0,0,0,0x32,0,0,0,1,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0x33,0,0,0x34,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0x35,0x32,0,0,0x36,0,0,0,0,0,0,0,0,
0x20,0,0,0,0,0,0x28,0x37,0,0x38,0x39,0,0,0x39,0x3a,0,
0,0,0,0,0,0x3b,0x3c,0x3d,0,0,0,0,0,0,0,0x17,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0x3e,0x23,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0x3f,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0x40,0,0,0,0,0,0,0,0,0,0,0,0,0,0
};

    private static final byte[] tcccIndex={
0,0,0,0,0,0,2,3,4,5,6,7,0,8,9,0xa,
0xb,0xc,0,0,0,0,0,0,1,1,0xd,0xe,0xf,0x10,0x11,0,
0x12,0x13,0x14,0x15,0x16,0,0x17,0x18,0,0,0,0,0x19,0x1a,0x1b,0,
0x1c,0x1d,0x1e,0x1f,0,0,0x20,0x21,0x22,0x23,0x24,0,0,0,0,0x25,
0x26,0x27,0x28,0,0,0,0,0x29,0,0x2a,0x2b,0,0,0x2c,0x2d,0,
0,0x2e,0x2f,0,0,0x2c,0x30,0,0,0x2c,0x31,0,0,0,0x30,0,
0,0,0x32,0,0,0x2c,0x30,0,0,0,0x30,0,0,0,0x33,0,
0,0x34,0x35,0,0,0x36,0x35,0,0x36,0x37,0,0x38,0x39,0,0x3a,0,
0,0x3b,0,0,0x30,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0x3c,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0x3d,0x3d,0,0,0,0,0x3e,0,
0,0,0,0,0,0x3f,0,0,0,0x28,0,0,0,0,0,0,
0x40,0,0,0x41,0,0,0,0,0,0x3d,0x42,0x25,0,0x43,0,0x44,
0,0x45,0,0,0,0,0x46,0x47,0,0,0,0,0,0,1,0x48,
1,1,1,1,0x49,1,1,0x4a,0x4b,1,0x4c,0x4d,1,0x4e,0x4f,0x50,
0,0,0,0,0,0,0x51,0x52,0,0x53,0,0,0x54,0x55,0x56,0,
0x57,0x58,0x59,0x5a,0x5b,0x5c,0,0x5d,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0x2c,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0x5e,0,0,0,0x5f,0,0,0,1,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0x60,0x61,0x62,0x63,0x61,0x62,0x64,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0x65,0x5f,0,0,0x66,0,0,0,0,0,0,0,0,
0x3a,0,0,0,0,0,0x42,0x67,0,0x68,0x69,0,0,0x69,0x6a,0,
0,0,0,0,0,0x6b,0x6c,0x6d,0,0,0,0,0,0,0,0x30,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0x6e,0x3d,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0x3c,0x6f,0x70,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0x71,0,0,0,0,0,0,0,0,0,0,0,0,0,0
};

    private static final int[] lcccBits={
0,0xffffffff,0xffff7fff,0xffff,0xf8,0xfffe0000,0xbfffffff,0xb6,0x7ff0000,0xfffff800,0x10000,0x9fc00000,0x3d9f,0x20000,0xffff0000,0x7ff,
0xff800,0xfbc00000,0x3eef,0xe000000,0x7ffffff0,0x10000000,0x1e2000,0x2000,0x602000,0x400,0x7000000,0xf00,0x3000000,0x2a00000,0x3c3e0000,0xdf,
0x40,0x6800000,0xe0000000,0x100000,0x20040000,0x200,0x1800000,0x9fe00001,0x10,0xc00,0xc0040,0x800000,0xfff70000,0x1021fd,0xf000007f,0x1fff0000,
0x1ffe2,0x38000,0x80000000,0xfc00,0x6000000,0x3ff08000,0x30000,0x3ffff,0x3800,0x80000,1,0xc19d0000,2,0x400000,0x35,0x40000000,
0x7f
};
    private static final int[] tcccBits={
0,0xffffffff,0x3e7effbf,0xbe7effbf,0xfffcffff,0x7ef1ff3f,0xfff3f1f8,0x7fffff3f,0x18003,0xdfffe000,0xff31ffcf,0xcfffffff,0xfffc0,0xffff7fff,0xffff,0x1d760,
0x1fc00,0x187c00,0x200708b,0x2000000,0x708b0000,0xc00000,0xf8,0xfccf0006,0x33ffcfc,0xfffe0000,0xbfffffff,0xb6,0x7ff0000,0x7c,0xfffff800,0x10000,
0x9fc80005,0x3d9f,0x20000,0xffff0000,0x7ff,0xff800,0xfbc00000,0x3eef,0xe000000,0x7ffffff0,0x10120200,0xff1e2000,0x10000000,0xb0002000,0x10480000,0x4e002000,
0x2000,0x30002000,0x602100,0x24000400,0x7000000,0xf00,0x3000000,0x2a00000,0x3d7e0000,0xdf,0x40,0x6800000,0xe0000000,0x100000,0x20040000,0x200,
0x1800000,0x9fe00001,0x10,0xc00,0xc0040,0x800000,0xfff70000,0x1021fd,0xf000007f,0xbffffff,0x3ffffff,0x3f3fffff,0xaaff3f3f,0x3fffffff,0x1fdfffff,0xefcfffde,
0x1fdc7fff,0x1fff0000,0x1ffe2,0x800,0xc000000,0x4000,0xe000,0x1210,0x50,0x292,0x333e005,0x333,0xf000,0x3c0f,0x38000,0x80000000,
0xfc00,0x55555000,0x36db02a5,0x46100000,0x47900000,0x3ff08000,0x30000,0x3ffff,0x3800,0x80000,1,0xc19d0000,2,0x400000,0x35,0x5f7ffc00,
0x7fdb,0x7f
};

}
