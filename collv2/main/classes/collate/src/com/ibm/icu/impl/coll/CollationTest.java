/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationTest.cpp, ported from collationtest.cpp
*
* @since 2012apr27
* @author Markus W. Scherer
*/

// TODO: try to share code with IntlTestCollator; for example, prettify(CollationKey)

final class CollationTest : public IntlTest {
public:
    CollationTest()
            : fcd(null), nfd(null),
              fileLineNumber(0),
              coll(null) {}

    ~CollationTest() {
        delete coll;
    }

    void runIndexedTest(int index, boolean exec, const char *&name, char *par=null);

    void TestMinMax();
    void TestImplicits();
    void TestNulTerminated();
    void TestIllegalUTF8();
    void TestShortFCDData();
    void TestFCD();
    void TestCollationWeights();
    void TestRootElements();
    void TestTailoredElements();
    void TestDataDriven();

private:
    void checkFCD(const char *name, CollationIterator &ci, CodePointIterator &cpi);
    void checkAllocWeights(CollationWeights &cw,
                           uint32_t lowerLimit, uint32_t upperLimit, int n,
                           int someLength, int minCount);

    static UnicodeString printSortKey(const uint8_t *p, int length);
    static UnicodeString printCollationKey(const CollationKey &key);

    // Helpers & fields for data-driven test.
    static boolean isCROrLF(UChar c) { return c == 0xa || c == 0xd; }
    static boolean isSpace(UChar c) { return c == 9 || c == 0x20 || c == 0x3000; }
    static boolean isSectionStarter(UChar c) { return c == 0x25 || c == 0x2a || c == 0x40; }  // %*@
    int skipSpaces(int i) {
        while(isSpace(fileLine[i])) { ++i; }
        return i;
    }

    boolean readLine(UCHARBUF *f, IcuTestErrorCode &errorCode);
    void parseString(int &start, UnicodeString &prefix, UnicodeString &s);
    Collation.Level parseRelationAndString(UnicodeString &s, IcuTestErrorCode &errorCode);
    void parseAndSetAttribute(IcuTestErrorCode &errorCode);
    void parseAndSetReorderCodes(int start, IcuTestErrorCode &errorCode);
    void buildTailoring(UCHARBUF *f, IcuTestErrorCode &errorCode);
    void setRootCollator(IcuTestErrorCode &errorCode);
    void setLocaleCollator(IcuTestErrorCode &errorCode);

    boolean needsNormalization(const UnicodeString &s);

    boolean getSortKeyParts(const UChar *s, int length,
                          CharString &dest, int partSize,
                          IcuTestErrorCode &errorCode);
    boolean getCollationKey(const char *norm, const UnicodeString &line,
                          const UChar *s, int length,
                          CollationKey &key, IcuTestErrorCode &errorCode);
    boolean checkCompareTwo(const char *norm, const UnicodeString &prevFileLine,
                          const UnicodeString &prevString, const UnicodeString &s,
                          UCollationResult expectedOrder, Collation.Level expectedLevel,
                          IcuTestErrorCode &errorCode);
    void checkCompareStrings(UCHARBUF *f, IcuTestErrorCode &errorCode);

    const Normalizer2 *fcd, *nfd;
    UnicodeString fileLine;
    int fileLineNumber;
    UnicodeString fileTestName;
    Collator *coll;
}

    extern IntlTest *createCollationTest() {
        return new CollationTest();
    }

    void CollationTest.runIndexedTest(int index, boolean exec, const char *&name, char * /*par*/) {
        if(exec) {
            logln("TestSuite CollationTest: ");
        }
        TESTCASE_AUTO_BEGIN;
        TESTCASE_AUTO(TestMinMax);
        TESTCASE_AUTO(TestImplicits);
        TESTCASE_AUTO(TestNulTerminated);
        TESTCASE_AUTO(TestIllegalUTF8);
        TESTCASE_AUTO(TestShortFCDData);
        TESTCASE_AUTO(TestFCD);
        TESTCASE_AUTO(TestCollationWeights);
        TESTCASE_AUTO(TestRootElements);
        TESTCASE_AUTO(TestTailoredElements);
        TESTCASE_AUTO(TestDataDriven);
        TESTCASE_AUTO_END;
    }

    void CollationTest.TestMinMax() {
        IcuTestErrorCode errorCode(*this, "TestMinMax");

        setRootCollator;
        if(errorCode.isFailure()) {
            errorCode.reset();
            return;
        }
        RuleBasedCollator *rbc = dynamic_cast<RuleBasedCollator *>(coll);
        if(rbc == null) {
            errln("the root collator is not a RuleBasedCollator");
            return;
        }

        private static final UChar s[2] = { 0xfffe, 0xffff };
        UVector64 ces;
        rbc.internalGetCEs(UnicodeString(false, s, 2), ces);
        errorCode.assertSuccess();
        if(ces.size() != 2) {
            errln("expected 2 CEs for <FFFE, FFFF>, got %d", (int)ces.size());
            return;
        }
        long ce = ces.elementAti(0);
        long expected =
            (Collation.MERGE_SEPARATOR_PRIMARY << 32) |
            Collation.MERGE_SEPARATOR_LOWER32;
        if(ce != expected) {
            errln("CE(U+fffe)=%04lx != 02.02.02", (long)ce);
        }

        ce = ces.elementAti(1);
        expected = Collation.makeCE(Collation.MAX_PRIMARY);
        if(ce != expected) {
            errln("CE(U+ffff)=%04lx != max..", (long)ce);
        }
    }

    void CollationTest.TestImplicits() {
        IcuTestErrorCode errorCode(*this, "TestImplicits");

        CollationData cd = CollationRoot.getData;
        if(errorCode.logIfFailureAndReset("CollationRoot.getBaseData()")) {
            return;
        }

        // Implicit primary weights should be assigned for the following sets,
        // and sort in ascending order by set and then code point.
        // See http://www.unicode.org/reports/tr10/#Implicit_Weights
        // core Han Unified Ideographs
        UnicodeSet coreHan("[\\p{unified_ideograph}&"
                                "[\\p{Block=CJK_Unified_Ideographs}"
                                "\\p{Block=CJK_Compatibility_Ideographs}]]",
                          errorCode);
        // all other Unified Han ideographs
        UnicodeSet otherHan("[\\p{unified ideograph}-"
                                "[\\p{Block=CJK_Unified_Ideographs}"
                                "\\p{Block=CJK_Compatibility_Ideographs}]]",
                            errorCode);
        UnicodeSet unassigned("[[:Cn:][:Cs:][:Co:]]");
        unassigned.remove(0xfffe, 0xffff);  // These have special CLDR root mappings.
        if(errorCode.logIfFailureAndReset("UnicodeSet")) {
            return;
        }
        const UnicodeSet *sets[] = { &coreHan, &otherHan, &unassigned };
        int prev = 0;
        long prevPrimary = 0;
        UTF16CollationIterator ci(cd, false, null, null, null);
        for(int i = 0; i < LENGTHOF(sets); ++i) {
            LocalPointer<UnicodeSetIterator> iter(new UnicodeSetIterator(*sets[i]));
            while(iter.next()) {
                int c = iter.getCodepoint();
                UnicodeString s(c);
                ci.setText(s.getBuffer(), s.getBuffer() + s.length());
                long ce = ci.nextCE;
                long ce2 = ci.nextCE;
                if(errorCode.logIfFailureAndReset("CollationIterator.nextCE()")) {
                    return;
                }
                if(ce == Collation.NO_CE || ce2 != Collation.NO_CE) {
                    errln("CollationIterator.nextCE(U+%04lx) did not yield exactly one CE", (long)c);
                    continue;
                }
                if((ce & 0xffffffffL) != Collation.COMMON_SEC_AND_TER_CE) {
                    errln("CollationIterator.nextCE(U+%04lx) has non-common sec/ter weights: %08lx",
                          (long)c, ce & 0xffffffffL);
                    continue;
                }
                long primary = ce >>> 32;
                if(!(primary > prevPrimary)) {
                    errln("CE(U+%04lx)=%04lx.. not greater than CE(U+%04lx)=%04lx..",
                          (long)c, (long)primary, (long)prev, (long)prevPrimary);
                }
                prev = c;
                prevPrimary = primary;
            }
        }
    }

    void CollationTest.TestNulTerminated() {
        IcuTestErrorCode errorCode(*this, "TestNulTerminated");
        CollationData data = CollationRoot.getData;
        if(errorCode.logIfFailureAndReset("CollationRoot.getData()")) {
            return;
        }

        private static final UChar s[] = { 0x61, 0x62, 0x61, 0x62, 0 };

        UTF16CollationIterator ci1(data, false, s, s, s + 2);
        UTF16CollationIterator ci2(data, false, s + 2, s + 2, null);
        for(int i = 0;; ++i) {
            long ce1 = ci1.nextCE;
            long ce2 = ci2.nextCE;
            if(errorCode.logIfFailureAndReset("CollationIterator.nextCE()")) {
                return;
            }
            if(ce1 != ce2) {
                errln("CollationIterator.nextCE(with length) != nextCE(NUL-terminated) at CE %d", (int)i);
                break;
            }
            if(ce1 == Collation.NO_CE) { break; }
        }
    }

    void CollationTest.TestIllegalUTF8() {
        IcuTestErrorCode errorCode(*this, "TestIllegalUTF8");

        setRootCollator;
        if(errorCode.isFailure()) {
            errorCode.reset();
            return;
        }
        coll.setAttribute(UCOL_STRENGTH, UCOL_IDENTICAL);

        private static final char *strings[] = {
            // U+FFFD
            "a\xef\xbf\xbdz",
            // illegal byte sequences
            "a\x80z",  // trail byte
            "a\xc1\x81z",  // non-shortest form
            "a\xe0\x82\x83z",  // non-shortest form
            "a\xed\xa0\x80z",  // lead surrogate: would be U+D800
            "a\xed\xbf\xbfz",  // trail surrogate: would be U+DFFF
            "a\xf0\x8f\xbf\xbfz",  // non-shortest form
            "a\xf4\x90\x80\x80z"  // out of range: would be U+110000
        };

        StringPiece fffd(strings[0]);
        for(int i = 1; i < LENGTHOF(strings); ++i) {
            StringPiece illegal(strings[i]);
            UCollationResult order = coll.compareUTF8(fffd, illegal);
            if(order != UCOL_EQUAL) {
                errln("compareUTF8(U+FFFD, string %d with illegal UTF-8)=%d != UCOL_EQUAL",
                      (int)i, order);
            }
        }
    }

    namespace {

    void addLeadSurrogatesForSupplementary(const UnicodeSet &src, UnicodeSet &dest) {
        for(int c = 0x10000; c < 0x110000;) {
            int next = c + 0x400;
            if(src.containsSome(c, next - 1)) {
                dest.add(U16_LEAD(c));
            }
            c = next;
        }
    }

    }  // namespace

    void CollationTest.TestShortFCDData() {
        // See CollationFCD class comments.
        IcuTestErrorCode errorCode(*this, "TestShortFCDData");
        UnicodeSet expectedLccc("[:^lccc=0:]");
        errorCode.assertSuccess();
        expectedLccc.add(0xdc00, 0xdfff);  // add all trail surrogates
        addLeadSurrogatesForSupplementary(expectedLccc, expectedLccc);
        UnicodeSet lccc;  // actual
        for(int c = 0; c <= 0xffff; ++c) {
            if(CollationFCD.hasLccc(c)) { lccc.add(c); }
        }
        UnicodeSet diff(expectedLccc);
        diff.removeAll(lccc);
        diff.remove(0x10000, 0x10ffff);  // hasLccc() only works for the BMP
        UnicodeString empty("[]");
        UnicodeString diffString;
        diff.toPattern(diffString, true);
        assertEquals("CollationFCD.hasLccc() expected-actual", empty, diffString);
        diff = lccc;
        diff.removeAll(expectedLccc);
        diff.toPattern(diffString, true);
        assertEquals("CollationFCD.hasLccc() actual-expected", empty, diffString);

        UnicodeSet expectedTccc("[:^tccc=0:]");
        errorCode.assertSuccess();
        addLeadSurrogatesForSupplementary(expectedLccc, expectedTccc);
        addLeadSurrogatesForSupplementary(expectedTccc, expectedTccc);
        UnicodeSet tccc;  // actual
        for(int c = 0; c <= 0xffff; ++c) {
            if(CollationFCD.hasTccc(c)) { tccc.add(c); }
        }
        diff = expectedTccc;
        diff.removeAll(tccc);
        diff.remove(0x10000, 0x10ffff);  // hasTccc() only works for the BMP
        assertEquals("CollationFCD.hasTccc() expected-actual", empty, diffString);
        diff = tccc;
        diff.removeAll(expectedTccc);
        diff.toPattern(diffString, true);
        assertEquals("CollationFCD.hasTccc() actual-expected", empty, diffString);
    }

    class CodePointIterator {
    public:
        CodePointIterator(const int *cp, int length) : cp(cp), length(length), pos(0) {}
        void resetToStart() { pos = 0; }
        int next() { return (pos < length) ? cp[pos++] : Collation.SENTINEL_CP; }
        int previous() { return (pos > 0) ? cp[--pos] : Collation.SENTINEL_CP; }
        int getLength() { return length; }
        int getIndex() { return (int)pos; }
    private:
        const int *cp;
        int length;
        int pos;
    };

    void CollationTest.checkFCD(const char *name,
                                CollationIterator &ci, CodePointIterator &cpi) {
        IcuTestErrorCode errorCode(*this, "checkFCD");

        // Iterate forward to the limit.
        for(;;) {
            int c1 = ci.nextCodePoint;
            int c2 = cpi.next();
            if(c1 != c2) {
                errln("%s.nextCodePoint(to limit, 1st pass) = U+%04lx != U+%04lx at %d",
                      name, (long)c1, (long)c2, cpi.getIndex());
                return;
            }
            if(c1 < 0) { break; }
        }

        // Iterate backward most of the way.
        for(int n = (cpi.getLength() * 2) / 3; n > 0; --n) {
            int c1 = ci.previousCodePoint;
            int c2 = cpi.previous();
            if(c1 != c2) {
                errln("%s.previousCodePoint() = U+%04lx != U+%04lx at %d",
                      name, (long)c1, (long)c2, cpi.getIndex());
                return;
            }
        }

        // Forward again.
        for(;;) {
            int c1 = ci.nextCodePoint;
            int c2 = cpi.next();
            if(c1 != c2) {
                errln("%s.nextCodePoint(to limit again) = U+%04lx != U+%04lx at %d",
                      name, (long)c1, (long)c2, cpi.getIndex());
                return;
            }
            if(c1 < 0) { break; }
        }

        // Iterate backward to the start.
        for(;;) {
            int c1 = ci.previousCodePoint;
            int c2 = cpi.previous();
            if(c1 != c2) {
                errln("%s.previousCodePoint(to start) = U+%04lx != U+%04lx at %d",
                      name, (long)c1, (long)c2, cpi.getIndex());
                return;
            }
            if(c1 < 0) { break; }
        }
    }

    void CollationTest.TestFCD() {
        IcuTestErrorCode errorCode(*this, "TestFCD");
        CollationData data = CollationRoot.getData;
        if(errorCode.logIfFailureAndReset("CollationRoot.getData()")) {
            return;
        }

        // Input string, not FCD, NUL-terminated.
        private static final UChar s[] = {
            0x308, 0xe1, 0x62, 0x301, 0x327, 0x430, 0x62,
            U16_LEAD(0x1D15F), U16_TRAIL(0x1D15F),  // MUSICAL SYMBOL QUARTER NOTE=1D158 1D165, ccc=0, 216
            0x327, 0x308,  // ccc=202, 230
            U16_LEAD(0x1D16D), U16_TRAIL(0x1D16D),  // MUSICAL SYMBOL COMBINING AUGMENTATION DOT, ccc=226
            U16_LEAD(0x1D15F), U16_TRAIL(0x1D15F),
            U16_LEAD(0x1D16D), U16_TRAIL(0x1D16D),
            0xac01,
            0xe7,  // Character with tccc!=0 decomposed together with mis-ordered sequence.
            U16_LEAD(0x1D16D), U16_TRAIL(0x1D16D), U16_LEAD(0x1D165), U16_TRAIL(0x1D165),
            0xe1,  // Character with tccc!=0 decomposed together with decomposed sequence.
            0xf73, 0xf75,  // Tibetan composite vowels must be decomposed.
            0x4e00, 0xf81,
            0
        };
        // Expected code points.
        private static final int cp[] = {
            0x308, 0xe1, 0x62, 0x327, 0x301, 0x430, 0x62,
            0x1D158, 0x327, 0x1D165, 0x1D16D, 0x308,
            0x1D15F, 0x1D16D,
            0xac01,
            0x63, 0x327, 0x1D165, 0x1D16D,
            0x61,
            0xf71, 0xf71, 0xf72, 0xf74, 0x301,
            0x4e00, 0xf71, 0xf80
        };

        FCDUTF16CollationIterator u16ci(data, false, s, s, null);
        if(errorCode.logIfFailureAndReset("FCDUTF16CollationIterator constructor")) {
            return;
        }
        CodePointIterator cpi(cp, LENGTHOF(cp));
        checkFCD("FCDUTF16CollationIterator", u16ci, cpi);

        cpi.resetToStart();
        UCharIterator iter;
        uiter_setString(&iter, s, LENGTHOF(s) - 1);  // -1: without the terminating NUL
        FCDUIterCollationIterator uici(data, false, iter, 0);
        if(errorCode.logIfFailureAndReset("FCDUIterCollationIterator constructor")) {
            return;
        }
        checkFCD("FCDUIterCollationIterator", uici, cpi);
    }

    void CollationTest.checkAllocWeights(CollationWeights &cw,
                                          uint32_t lowerLimit, uint32_t upperLimit, int n,
                                          int someLength, int minCount) {
        if(!cw.allocWeights(lowerLimit, upperLimit, n)) {
            errln("CollationWeights.allocWeights(%lx, %lx, %ld) = false",
                  (long)lowerLimit, (long)upperLimit, (long)n);
            return;
        }
        long previous = lowerLimit;
        int count = 0;  // number of weights that have someLength
        for(int i = 0; i < n; ++i) {
            uint32_t w = cw.nextWeight();
            if(w == 0xffffffff) {
                errln("CollationWeights.allocWeights(%lx, %lx, %ld).nextWeight() "
                      "returns only %ld weights",
                      (long)lowerLimit, (long)upperLimit, (long)n, (long)i);
                return;
            }
            if(!(previous < w && w < upperLimit)) {
                errln("CollationWeights.allocWeights(%lx, %lx, %ld).nextWeight() "
                      "number %ld . %lx not between %lx and %lx",
                      (long)lowerLimit, (long)upperLimit, (long)n,
                      (long)(i + 1), (long)w, (long)previous, (long)upperLimit);
                return;
            }
            if(CollationWeights.lengthOfWeight(w) == someLength) { ++count; }
        }
        if(count < minCount) {
            errln("CollationWeights.allocWeights(%lx, %lx, %ld).nextWeight() "
                  "returns only %ld < %ld weights of length %d",
                  (long)lowerLimit, (long)upperLimit, (long)n,
                  (long)count, (long)minCount, (int)someLength);
        }
    }

    void CollationTest.TestCollationWeights() {
        CollationWeights cw;

        // Non-compressible primaries use 254 second bytes 02..FF.
        logln("CollationWeights.initForPrimary(non-compressible)");
        cw.initForPrimary(false);
        // Expect 1 weight 11 and 254 weights 12xx.
        checkAllocWeights(cw, 0x10000000, 0x13000000, 255, 1, 1);
        checkAllocWeights(cw, 0x10000000, 0x13000000, 255, 2, 254);
        // Expect 255 two-byte weights from the ranges 10ff, 11xx, 1202.
        checkAllocWeights(cw, 0x10fefe40, 0x12030300, 260, 2, 255);
        // Expect 254 two-byte weights from the ranges 10ff and 11xx.
        checkAllocWeights(cw, 0x10fefe40, 0x12030300, 600, 2, 254);
        // Expect 254^2=64516 three-byte weights.
        // During computation, there should be 3 three-byte ranges
        // 10ffff, 11xxxx, 120202.
        // The middle one should be split 64515:1,
        // and the newly-split-off range and the last ranged lengthened.
        checkAllocWeights(cw, 0x10fffe00, 0x12020300, 1 + 64516 + 254 + 1, 3, 64516);
        // Expect weights 1102 & 1103.
        checkAllocWeights(cw, 0x10ff0000, 0x11040000, 2, 2, 2);
        // Expect weights 102102 & 102103.
        checkAllocWeights(cw, 0x1020ff00, 0x10210400, 2, 3, 2);

        // Compressible primaries use 251 second bytes 04..FE.
        logln("CollationWeights.initForPrimary(compressible)");
        cw.initForPrimary(true);
        // Expect 1 weight 11 and 251 weights 12xx.
        checkAllocWeights(cw, 0x10000000, 0x13000000, 252, 1, 1);
        checkAllocWeights(cw, 0x10000000, 0x13000000, 252, 2, 251);
        // Expect 252 two-byte weights from the ranges 10fe, 11xx, 1204.
        checkAllocWeights(cw, 0x10fdfe40, 0x12050300, 260, 2, 252);
        // Expect weights 1104 & 1105.
        checkAllocWeights(cw, 0x10fe0000, 0x11060000, 2, 2, 2);
        // Expect weights 102102 & 102103.
        checkAllocWeights(cw, 0x1020ff00, 0x10210400, 2, 3, 2);

        // Secondary and tertiary weights use only bytes 3 & 4.
        logln("CollationWeights.initForSecondary()");
        cw.initForSecondary();
        // Expect weights fbxx and all four fc..ff.
        checkAllocWeights(cw, 0xfb20, 0x10000, 20, 3, 4);

        logln("CollationWeights.initForTertiary()");
        cw.initForTertiary();
        // Expect weights 3dxx and both 3e & 3f.
        checkAllocWeights(cw, 0x3d02, 0x4000, 10, 3, 2);
    }

    namespace {

    boolean isValidCE(const CollationRootElements &re, CollationData data,
                    long p, int s, int ctq) {
        int p1 = (int)p >>> 24;
        int p2 = (p >> 16) & 0xff;
        int p3 = (p >> 8) & 0xff;
        int p4 = p & 0xff;
        int s1 = s >> 8;
        int s2 = s & 0xff;
        // ctq = Case, Tertiary, Quaternary
        int c = (ctq & Collation.CASE_MASK) >> 14;
        int t = ctq & Collation.ONLY_TERTIARY_MASK;
        int t1 = t >> 8;
        int t2 = t & 0xff;
        int q = ctq & Collation.QUATERNARY_MASK;
        // No leading zero bytes.
        if((p != 0 && p1 == 0) || (s != 0 && s1 == 0) || (t != 0 && t1 == 0)) {
            return false;
        }
        // No intermediate zero bytes.
        if(p1 != 0 && p2 == 0 && (p & 0xffff) != 0) {
            return false;
        }
        if(p2 != 0 && p3 == 0 && p4 != 0) {
            return false;
        }
        // Minimum & maximum lead bytes.
        if((p1 != 0 && p1 <= Collation.MERGE_SEPARATOR_BYTE) ||
                (s1 != 0 && s1 <= Collation.MERGE_SEPARATOR_BYTE) ||
                (t1 != 0 && t1 <= Collation.MERGE_SEPARATOR_BYTE)) {
            return false;
        }
        if(t1 != 0 && t1 > 0x3f) {
            return false;
        }
        if(c > 2) {
            return false;
        }
        // The valid byte range for the second primary byte depends on compressibility.
        if(p2 != 0) {
            if(data.isCompressibleLeadByte(p1)) {
                if(p2 <= Collation.PRIMARY_COMPRESSION_LOW_BYTE ||
                        Collation.PRIMARY_COMPRESSION_HIGH_BYTE <= p2) {
                    return false;
                }
            } else {
                if(p2 <= Collation.LEVEL_SEPARATOR_BYTE) {
                    return false;
                }
            }
        }
        // Other bytes just need to avoid the level separator.
        // Trailing zeros are ok.
        assert(Collation.LEVEL_SEPARATOR_BYTE == 1);
        if(p3 == Collation.LEVEL_SEPARATOR_BYTE || p4 == Collation.LEVEL_SEPARATOR_BYTE ||
                s2 == Collation.LEVEL_SEPARATOR_BYTE || t2 == Collation.LEVEL_SEPARATOR_BYTE) {
            return false;
        }
        // Well-formed CEs.
        if(p == 0) {
            if(s == 0) {
                if(t == 0) {
                    // Completely ignorable CE.
                    // Quaternary CEs are not supported.
                    if(c != 0 || q != 0) {
                        return false;
                    }
                } else {
                    // Tertiary CE.
                    if(t < re.getTertiaryBoundary() || c != 2) {
                        return false;
                    }
                }
            } else {
                // Secondary CE.
                if(s < re.getSecondaryBoundary() || t == 0 || t >= re.getTertiaryBoundary()) {
                    return false;
                }
            }
        } else {
            // Primary CE.
            if(s == 0 || (Collation.COMMON_WEIGHT16 < s && s <= re.getLastCommonSecondary()) ||
                    s >= re.getSecondaryBoundary()) {
                return false;
            }
            if(t == 0 || t >= re.getTertiaryBoundary()) {
                return false;
            }
        }
        return true;
    }

    boolean isValidCE(const CollationRootElements &re, CollationData data, long ce) {
        long p = ce >>> 32;
        int secTer = (int)ce;
        return isValidCE(re, data, p, secTer >>> 16, secTer & 0xffff);
    }

    class RootElementsIterator {
    public:
        RootElementsIterator(CollationData root)
                : data(root),
                  elements(root.rootElements), length(root.rootElementsLength),
                  pri(0), secTer(0),
                  index((int32_t)elements[CollationRootElements.IX_FIRST_TERTIARY_INDEX]) {}

        boolean next() {
            if(index >= length) { return false; }
            long p = elements[index];
            if(p == CollationRootElements.PRIMARY_SENTINEL) { return false; }
            if((p & CollationRootElements.SEC_TER_DELTA_FLAG) != 0) {
                ++index;
                secTer = p & ~CollationRootElements.SEC_TER_DELTA_FLAG;
                return true;
            }
            if((p & CollationRootElements.PRIMARY_STEP_MASK) != 0) {
                // End of a range, enumerate the primaries in the range.
                int step = (int32_t)p & CollationRootElements.PRIMARY_STEP_MASK;
                p &= 0xffffff00L;
                if(pri == p) {
                    // Finished the range, return the next CE after it.
                    ++index;
                    return next();
                }
                assert(pri < p);
                // Return the next primary in this range.
                boolean isCompressible = data.isCompressiblePrimary(pri);
                if((pri & 0xffff) == 0) {
                    pri = Collation.incTwoBytePrimaryByOffset(pri, isCompressible, step);
                } else {
                    pri = Collation.incThreeBytePrimaryByOffset(pri, isCompressible, step);
                }
                return true;
            }
            // Simple primary CE.
            ++index;
            pri = p;
            secTer = Collation.COMMON_SEC_AND_TER_CE;
            return true;
        }

        long getPrimary() { return pri; }
        uint32_t getSecTer() { return secTer; }

    private:
        CollationData data;
        const uint32_t *elements;
        int length;

        long pri;
        uint32_t secTer;
        int index;
    };

    }  // namespace

    void CollationTest.TestRootElements() {
        IcuTestErrorCode errorCode(*this, "TestRootElements");
        CollationData root = CollationRoot.getData;
        if(errorCode.logIfFailureAndReset("CollationRoot.getData()")) {
            return;
        }
        CollationRootElements rootElements(root.rootElements, root.rootElementsLength);
        RootElementsIterator iter(*root);

        // We check each root CE for validity,
        // and we also verify that there is a tailoring gap between each two CEs.
        CollationWeights cw1c;  // compressible primary weights
        CollationWeights cw1u;  // uncompressible primary weights
        CollationWeights cw2;
        CollationWeights cw3;

        cw1c.initForPrimary(true);
        cw1u.initForPrimary(false);
        cw2.initForSecondary();
        cw3.initForTertiary();

        // Note: The root elements do not include Han-implicit or unassigned-implicit CEs,
        // nor the special merge-separator CE for U+FFFE.
        long prevPri = 0;
        uint32_t prevSec = 0;
        uint32_t prevTer = 0;
        while(iter.next()) {
            long pri = iter.getPrimary();
            int secTer = iter.getSecTer();
            // CollationRootElements CEs must have 0 case and quaternary bits.
            if((secTer & Collation.CASE_AND_QUATERNARY_MASK) != 0) {
                errln("CollationRootElements CE has non-zero case and/or quaternary bits: %08lx %08lx",
                      (long)pri, (long)secTer);
            }
            int sec = secTer >>> 16;
            uint32_t ter = secTer & Collation.ONLY_TERTIARY_MASK;
            uint32_t ctq = ter;
            if(pri == 0 && sec == 0 && ter != 0) {
                // Tertiary CEs must have uppercase bits,
                // but they are not stored in the CollationRootElements.
                ctq |= 0x8000;
            }
            if(!isValidCE(rootElements, *root, pri, sec, ctq)) {
                errln("invalid root CE %08lx %08lx", pri, secTer);
            } else {
                if(pri != prevPri) {
                    uint32_t newWeight = 0;
                    if(prevPri == 0 || prevPri >= Collation.FFFD_PRIMARY) {
                        // There is currently no tailoring gap after primary ignorables,
                        // and we forbid tailoring after U+FFFD and U+FFFF.
                    } else if(root.isCompressiblePrimary(prevPri)) {
                        if(!cw1c.allocWeights(prevPri, pri, 1)) {
                            errln("no primary/compressible tailoring gap between %08lx and %08lx",
                                  (long)prevPri, (long)pri);
                        } else {
                            newWeight = cw1c.nextWeight();
                        }
                    } else {
                        if(!cw1u.allocWeights(prevPri, pri, 1)) {
                            errln("no primary/uncompressible tailoring gap between %08lx and %08lx",
                                  (long)prevPri, (long)pri);
                        } else {
                            newWeight = cw1u.nextWeight();
                        }
                    }
                    if(newWeight != 0 && !(prevPri < newWeight && newWeight < pri)) {
                        errln("mis-allocated primary weight, should get %08lx < %08lx < %08lx",
                              (long)prevPri, (long)newWeight, (long)pri);
                    }
                } else if(sec != prevSec) {
                    uint32_t lowerLimit =
                        prevSec == 0 ? rootElements.getSecondaryBoundary() - 0x100 : prevSec;
                    if(!cw2.allocWeights(lowerLimit, sec, 1)) {
                        errln("no secondary tailoring gap between %04x and %04x", lowerLimit, sec);
                    } else {
                        uint32_t newWeight = cw2.nextWeight();
                        if(!(prevSec < newWeight && newWeight < sec)) {
                            errln("mis-allocated secondary weight, should get %04x < %04x < %04x",
                                  (long)lowerLimit, (long)newWeight, (long)sec);
                        }
                    }
                } else if(ter != prevTer) {
                    uint32_t lowerLimit =
                        prevTer == 0 ? rootElements.getTertiaryBoundary() - 0x100 : prevTer;
                    if(!cw3.allocWeights(lowerLimit, ter, 1)) {
                        errln("no teriary tailoring gap between %04x and %04x", lowerLimit, ter);
                    } else {
                        uint32_t newWeight = cw3.nextWeight();
                        if(!(prevTer < newWeight && newWeight < ter)) {
                            errln("mis-allocated secondary weight, should get %04x < %04x < %04x",
                                  (long)lowerLimit, (long)newWeight, (long)ter);
                        }
                    }
                } else {
                    errln("duplicate root CE %08lx %08lx", (long)pri, (long)secTer);
                }
            }
            prevPri = pri;
            prevSec = sec;
            prevTer = ter;
        }
    }

    void CollationTest.TestTailoredElements() {
        IcuTestErrorCode errorCode(*this, "TestTailoredElements");
        CollationData root = CollationRoot.getData;
        if(errorCode.logIfFailureAndReset("CollationRoot.getData()")) {
            return;
        }
        CollationRootElements rootElements(root.rootElements, root.rootElementsLength);

        UHashtable *prevLocales = uhash_open(uhash_hashChars, uhash_compareChars, null);
        if(errorCode.logIfFailureAndReset("failed to create a hash table")) {
            return;
        }
        uhash_setKeyDeleter(prevLocales, uprv_free);
        // TestRootElements() tests the root collator which does not have tailorings.
        uhash_puti(prevLocales, uprv_strdup(""), 1);
        uhash_puti(prevLocales, uprv_strdup("root"), 1);
        uhash_puti(prevLocales, uprv_strdup("root@collation=standard"), 1);

        UVector64 ces;
        LocalPointer<StringEnumeration> locales(Collator.getAvailableLocales());
        assert(locales.isValid());
        const char *localeID = "root";
        do {
            Locale locale(localeID);
            LocalPointer<StringEnumeration> types(
                    Collator.getKeywordValuesForLocale("collation", locale, false));
            errorCode.assertSuccess();
            const char *type = null;  // default type
            do {
                Locale localeWithType(locale);
                if(type != null) {
                    localeWithType.setKeywordValue("collation", type);
                }
                errorCode.assertSuccess();
                LocalPointer<Collator> coll(Collator.createInstance(localeWithType));
                if(errorCode.logIfFailureAndReset("Collator.createInstance(%s)",
                                                  localeWithType.getName())) {
                    continue;
                }
                Locale actual = coll.getLocale(ULOC_ACTUAL_LOCALE);
                if(uhash_geti(prevLocales, actual.getName()) != 0) {
                    continue;
                }
                uhash_puti(prevLocales, uprv_strdup(actual.getName()), 1);
                errorCode.assertSuccess();
                logln("TestTailoredElements(): requested %s . actual %s",
                      localeWithType.getName(), actual.getName());
                RuleBasedCollator *rbc = dynamic_cast<RuleBasedCollator *>(coll.getAlias());
                if(rbc == null) {
                    continue;
                }
                // Note: It would be better to get tailored strings such that we can
                // identify the prefix, and only get the CEs for the prefix+string,
                // not also for the prefix.
                // There is currently no API for that.
                // It would help in an unusual case where a contraction starting in the prefix
                // extends past its end, and we do not see the intended mapping.
                // For example, for a mapping p|st, if there is also a contraction ps,
                // then we get CEs(ps)+CEs(t), rather than CEs(p|st).
                LocalPointer<UnicodeSet> tailored(coll.getTailoredSet);
                errorCode.assertSuccess();
                UnicodeSetIterator iter(*tailored);
                while(iter.next()) {
                    const UnicodeString &s = iter.getString();
                    ces.removeAllElements();
                    rbc.internalGetCEs(s, ces);
                    errorCode.assertSuccess();
                    for(int i = 0; i < ces.size(); ++i) {
                        long ce = ces.elementAti(i);
                        if(!isValidCE(rootElements, *root, ce)) {
                            errln("invalid tailored CE %016llx at CE index %d from string:",
                                  (long long)ce, (int)i);
                            infoln(prettify(s));
                        }
                    }
                }
            } while((type = types.next(null)) != null);
        } while((localeID = locales.next(null)) != null);
        uhash_close(prevLocales);
    }

    UnicodeString CollationTest.printSortKey(const uint8_t *p, int length) {
        UnicodeString s;
        for(int i = 0; i < length; ++i) {
            if(i > 0) { s.append((UChar)0x20); }
            uint8_t b = p[i];
            if(b == 0) {
                s.append((UChar)0x2e);  // period
            } else if(b == 1) {
                s.append((UChar)0x7c);  // vertical bar
            } else {
                appendHex(b, 2, s);
            }
        }
        return s;
    }

    UnicodeString CollationTest.printCollationKey(const CollationKey &key) {
        int length;
        const uint8_t *p = key.getByteArray(length);
        return printSortKey(p, length);
    }

    boolean CollationTest.readLine(UCHARBUF *f, IcuTestErrorCode &errorCode) {
        int lineLength;
        const UChar *line = ucbuf_readline(f, &lineLength);
        if(line == null || errorCode.isFailure()) {
            fileLine.remove();
            return false;
        }
        ++fileLineNumber;
        // Strip trailing CR/LF, comments, and spaces.
        const UChar *comment = u_memchr(line, 0x23, lineLength);  // '#'
        if(comment != null) {
            lineLength = (int32_t)(comment - line);
        } else {
            while(lineLength > 0 && isCROrLF(line[lineLength - 1])) { --lineLength; }
        }
        while(lineLength > 0 && isSpace(line[lineLength - 1])) { --lineLength; }
        fileLine.setTo(false, line, lineLength);
        return true;
    }

    void CollationTest.parseString(int &start, UnicodeString &prefix, UnicodeString &s,
                                    ) {
        int length = fileLine.length();
        int i;
        for(i = start; i < length && !isSpace(fileLine[i]); ++i) {}
        int pipeIndex = fileLine.indexOf((UChar)0x7c, start, i - start);  // '|'
        if(pipeIndex >= 0) {
            prefix = fileLine.tempSubStringBetween(start, pipeIndex).unescape();
            if(prefix.isEmpty()) {
                errln("empty prefix on line %d", (int)fileLineNumber);
                infoln(fileLine);
                errorCode = U_PARSE_ERROR;
                return;
            }
            start = pipeIndex + 1;
        } else {
            prefix.remove();
        }
        s = fileLine.tempSubStringBetween(start, i).unescape();
        if(s.isEmpty()) {
            errln("empty string on line %d", (int)fileLineNumber);
            infoln(fileLine);
            errorCode = U_PARSE_ERROR;
            return;
        }
        start = i;
    }

    Collation.Level CollationTest.parseRelationAndString(UnicodeString &s, IcuTestErrorCode &errorCode) {
        Collation.Level relation;
        int start;
        if(fileLine[0] == 0x3c) {  // <
            UChar second = fileLine[1];
            start = 2;
            switch(second) {
            case 0x31:  // <1
                relation = Collation.PRIMARY_LEVEL;
                break;
            case 0x32:  // <2
                relation = Collation.SECONDARY_LEVEL;
                break;
            case 0x33:  // <3
                relation = Collation.TERTIARY_LEVEL;
                break;
            case 0x34:  // <4
                relation = Collation.QUATERNARY_LEVEL;
                break;
            case 0x63:  // <c
                relation = Collation.CASE_LEVEL;
                break;
            case 0x69:  // <i
                relation = Collation.IDENTICAL_LEVEL;
                break;
            default:  // just <
                relation = Collation.NO_LEVEL;
                start = 1;
                break;
            }
        } else if(fileLine[0] == 0x3d) {  // =
            relation = Collation.ZERO_LEVEL;
            start = 1;
        } else {
            start = 0;
        }
        if(start == 0 || !isSpace(fileLine[start])) {
            errln("no relation (= < <1 <2 <c <3 <4 <i) at beginning of line %d", (int)fileLineNumber);
            infoln(fileLine);
            errorCode.set(U_PARSE_ERROR);
            return Collation.NO_LEVEL;
        }
        start = skipSpaces(start);
        UnicodeString prefix;
        parseString(start, prefix, s);
        if(errorCode.isSuccess() && !prefix.isEmpty()) {
            errln("prefix string not allowed for test string: on line %d", (int)fileLineNumber);
            infoln(fileLine);
            errorCode.set(U_PARSE_ERROR);
            return Collation.NO_LEVEL;
        }
        if(start < fileLine.length()) {
            errln("unexpected line contents after test string on line %d", (int)fileLineNumber);
            infoln(fileLine);
            errorCode.set(U_PARSE_ERROR);
            return Collation.NO_LEVEL;
        }
        return relation;
    }

    private static final struct {
        const char *name;
        UColAttribute attr;
    } attributes[] = {
        { "backwards", UCOL_FRENCH_COLLATION },
        { "alternate", UCOL_ALTERNATE_HANDLING },
        { "caseFirst", UCOL_CASE_FIRST },
        { "caseLevel", UCOL_CASE_LEVEL },
        // UCOL_NORMALIZATION_MODE is turned on and off automatically.
        { "strength", UCOL_STRENGTH },
        // UCOL_HIRAGANA_QUATERNARY_MODE is deprecated.
        { "numeric", UCOL_NUMERIC_COLLATION }
    };

    private static final struct {
        const char *name;
        UColAttributeValue value;
    } attributeValues[] = {
        { "default", UCOL_DEFAULT },
        { "primary", UCOL_PRIMARY },
        { "secondary", UCOL_SECONDARY },
        { "tertiary", UCOL_TERTIARY },
        { "quaternary", UCOL_QUATERNARY },
        { "identical", UCOL_IDENTICAL },
        { "off", UCOL_OFF },
        { "on", UCOL_ON },
        { "shifted", UCOL_SHIFTED },
        { "non-ignorable", UCOL_NON_IGNORABLE },
        { "lower", UCOL_LOWER_FIRST },
        { "upper", UCOL_UPPER_FIRST }
    };

    void CollationTest.parseAndSetAttribute(IcuTestErrorCode &errorCode) {
        int start = skipSpaces(1);
        int equalPos = fileLine.indexOf(0x3d);
        if(equalPos < 0) {
            if(fileLine.compare(start, 7, UNICODE_STRING("reorder", 7)) == 0) {
                parseAndSetReorderCodes(start + 7);
                return;
            }
            errln("missing '=' on line %d", (int)fileLineNumber);
            infoln(fileLine);
            errorCode.set(U_PARSE_ERROR);
            return;
        }

        UnicodeString attrString = fileLine.tempSubStringBetween(start, equalPos);
        UnicodeString valueString = fileLine.tempSubString(equalPos+1);
        if(attrString == UNICODE_STRING("maxVariable", 11)) {
            UColReorderCode max;
            if(valueString == UNICODE_STRING("space", 5)) {
                max = Collator.ReorderCodes.SPACE;
            } else if(valueString == UNICODE_STRING("punct", 5)) {
                max = Collator.ReorderCodes.PUNCTUATION;
            } else if(valueString == UNICODE_STRING("symbol", 6)) {
                max = Collator.ReorderCodes.SYMBOL;
            } else if(valueString == UNICODE_STRING("currency", 8)) {
                max = Collator.ReorderCodes.CURRENCY;
            } else {
                errln("invalid attribute value name on line %d", (int)fileLineNumber);
                infoln(fileLine);
                errorCode.set(U_PARSE_ERROR);
                return;
            }
            coll.setMaxVariable(max);
            if(errorCode.isFailure()) {
                errln("setMaxVariable() failed on line %d: %s",
                      (int)fileLineNumber.errorName());
                infoln(fileLine);
                return;
            }
            fileLine.remove();
            return;
        }

        UColAttribute attr;
        for(int i = 0;; ++i) {
            if(i == LENGTHOF(attributes)) {
                errln("invalid attribute name on line %d", (int)fileLineNumber);
                infoln(fileLine);
                errorCode.set(U_PARSE_ERROR);
                return;
            }
            if(attrString == UnicodeString(attributes[i].name, -1, US_INV)) {
                attr = attributes[i].attr;
                break;
            }
        }

        UColAttributeValue value;
        for(int i = 0;; ++i) {
            if(i == LENGTHOF(attributeValues)) {
                errln("invalid attribute value name on line %d", (int)fileLineNumber);
                infoln(fileLine);
                errorCode.set(U_PARSE_ERROR);
                return;
            }
            if(valueString == UnicodeString(attributeValues[i].name, -1, US_INV)) {
                value = attributeValues[i].value;
                break;
            }
        }

        coll.setAttribute(attr, value);
        if(errorCode.isFailure()) {
            errln("illegal attribute=value combination on line %d: %s",
                  (int)fileLineNumber.errorName());
            infoln(fileLine);
            return;
        }
        fileLine.remove();
    }

    void CollationTest.parseAndSetReorderCodes(int start, IcuTestErrorCode &errorCode) {
        UVector32 reorderCodes;
        while(start < fileLine.length()) {
            start = skipSpaces(start);
            int limit = start;
            while(limit < fileLine.length() && !isSpace(fileLine[limit])) { ++limit; }
            CharString name;
            name.appendInvariantChars(fileLine.tempSubStringBetween(start, limit));
            int code = CollationRuleParser.getReorderCode(name.data());
            if(code < -1) {
                errln("invalid reorder code '%s' on line %d", name.data(), (int)fileLineNumber);
                infoln(fileLine);
                errorCode.set(U_PARSE_ERROR);
                return;
            }
            reorderCodes.addElement(code);
            start = limit;
        }
        coll.setReorderCodes(reorderCodes.getBuffer(), reorderCodes.size());
        if(errorCode.isFailure()) {
            errln("setReorderCodes() failed on line %d: %s", (int)fileLineNumber.errorName());
            infoln(fileLine);
            return;
        }
        fileLine.remove();
    }

    void CollationTest.buildTailoring(UCHARBUF *f, IcuTestErrorCode &errorCode) {
        UnicodeString rules;
        while(readLine(f)) {
            if(fileLine.isEmpty()) { continue; }
            if(isSectionStarter(fileLine[0])) { break; }
            rules.append(fileLine.unescape());
        }
        if(errorCode.isFailure()) { return; }
        logln(rules);

        UParseError parseError;
        UnicodeString reason;
        delete coll;
        coll = new RuleBasedCollator(rules, parseError, reason);
        if(coll == null) {
            errln("unable to allocate a new collator");
            errorCode.set(U_MEMORY_ALLOCATION_ERROR);
            return;
        }
        if(errorCode.isFailure()) {
            errln("RuleBasedCollator(rules) failed - %s".errorName());
            infoln(UnicodeString("  reason: ") + reason);
            if(parseError.offset >= 0) { infoln("  rules offset: %d", (int)parseError.offset); }
            if(parseError.preContext[0] != 0 || parseError.postContext[0] != 0) {
                infoln(UnicodeString("  snippet: ...") +
                    parseError.preContext + "(!)" + parseError.postContext + "...");
            }
        } else {
            assertEquals("no error reason when RuleBasedCollator(rules) succeeds",
                        UnicodeString(), reason);
        }
    }

    void CollationTest.setRootCollator(IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return; }
        delete coll;
        coll = Collator.createInstance(Locale.getRoot());
        if(errorCode.isFailure()) {
            dataerrln("unable to create a root collator");
            return;
        }
    }

    void CollationTest.setLocaleCollator(IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return; }
        CharString langTag;
        langTag.appendInvariantChars(fileLine.tempSubString(9));
        char localeID[ULOC_FULLNAME_CAPACITY];
        int parsedLength;
        uloc_forLanguageTag(
            langTag.data(), localeID, LENGTHOF(localeID), &parsedLength);
        Locale locale(localeID);
        if(fileLine.length() == 9 ||
                errorCode.isFailure() || errorCode.get() == U_STRING_NOT_TERMINATED_WARNING ||
                parsedLength != langTag.length() || locale.isBogus()) {
            errln("invalid language tag on line %d", (int)fileLineNumber);
            infoln(fileLine);
            if(errorCode.isSuccess()) { errorCode.set(U_PARSE_ERROR); }
            return;
        }

        logln("creating a collator for locale ID %s", locale.getName());
        Collator *newColl = Collator.createInstance(locale);
        if(errorCode.isFailure()) {
            dataerrln("unable to create a collator for locale %s on line %d",
                      locale.getName(), (int)fileLineNumber);
            infoln(fileLine);
            return;
        }
        delete coll;
        coll = newColl;
    }

    boolean CollationTest.needsNormalization(const UnicodeString &s) {
        if(U_FAILURE || !fcd.isNormalized(s)) { return true; }
        // In some sequences with Tibetan composite vowel signs,
        // even if the string passes the FCD check,
        // those composites must be decomposed.
        // Check if s contains 0F71 immediately followed by 0F73 or 0F75 or 0F81.
        int index = 0;
        while((index = s.indexOf((UChar)0xf71, index)) >= 0) {
            if(++index < s.length()) {
                UChar c = s[index];
                if(c == 0xf73 || c == 0xf75 || c == 0xf81) { return true; }
            }
        }
        return false;
    }

    boolean CollationTest.getSortKeyParts(const UChar *s, int length,
                                        CharString &dest, int partSize,
                                        IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return false; }
        uint8_t part[32];
        assert(partSize <= LENGTHOF(part));
        UCharIterator iter;
        uiter_setString(&iter, s, length);
        uint32_t state[2] = { 0, 0 };
        for(;;) {
            int partLength = coll.internalNextSortKeyPart(&iter, state, part, partSize);
            boolean done = partLength < partSize;
            if(done) {
                // At the end, append the next byte as well which should be 00.
                ++partLength;
            }
            dest.append(reinterpret_cast<char *>(part), partLength);
            if(done) {
                return errorCode.isSuccess();
            }
        }
    }

    boolean CollationTest.getCollationKey(const char *norm, const UnicodeString &line,
                                        const UChar *s, int length,
                                        CollationKey &key, IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return false; }
        coll.getCollationKey(s, length, key);
        if(errorCode.isFailure()) {
            infoln(fileTestName);
            errln("Collator(%s).getCollationKey() failed: %s",
                  norm.errorName());
            infoln(line);
            return false;
        }
        int keyLength;
        const uint8_t *keyBytes = key.getByteArray(keyLength);
        if(keyLength == 0 || keyBytes[keyLength - 1] != 0) {
            infoln(fileTestName);
            errln("Collator(%s).getCollationKey() wrote an empty or unterminated key",
                  norm);
            infoln(line);
            infoln(printCollationKey(key));
            return false;
        }

        int numLevels = coll.getAttribute(UCOL_STRENGTH);
        if(numLevels < UCOL_IDENTICAL) {
            ++numLevels;
        } else {
            numLevels = 5;
        }
        if(coll.getAttribute(UCOL_CASE_LEVEL) == UCOL_ON) {
            ++numLevels;
        }
        errorCode.assertSuccess();
        int numLevelSeparators = 0;
        for(int i = 0; i < (keyLength - 1); ++i) {
            uint8_t b = keyBytes[i];
            if(b == 0) {
                infoln(fileTestName);
                errln("Collator(%s).getCollationKey() contains a 00 byte", norm);
                infoln(line);
                infoln(printCollationKey(key));
                return false;
            }
            if(b == 1) { ++numLevelSeparators; }
        }
        if(numLevelSeparators != (numLevels - 1)) {
            infoln(fileTestName);
            errln("Collator(%s).getCollationKey() has %d level separators for %d levels",
                  norm, (int)numLevelSeparators, (int)numLevels);
            infoln(line);
            infoln(printCollationKey(key));
            return false;
        }

        // If s contains U+FFFE, check that merged segments make the same key.
        LocalMemory<uint8_t> mergedKey;
        int mergedKeyLength = 0;
        int mergedKeyCapacity = 0;
        int sLength = (length >= 0) ? length : u_strlen(s);
        int segmentStart = 0;
        for(int i = 0;;) {
            if(i == sLength) {
                if(segmentStart == 0) {
                    // s does not contain any U+FFFE.
                    break;
                }
            } else if(s[i] != 0xfffe) {
                ++i;
                continue;
            }
            // Get the sort key for another segment and merge it into mergedKey.
            CollationKey key1(mergedKey.getAlias(), mergedKeyLength);  // copies the bytes
            CollationKey key2;
            coll.getCollationKey(s + segmentStart, i - segmentStart, key2);
            int key1Length, key2Length;
            const uint8_t *key1Bytes = key1.getByteArray(key1Length);
            const uint8_t *key2Bytes = key2.getByteArray(key2Length);
            uint8_t *dest;
            int minCapacity = key1Length + key2Length;
            if(key1Length > 0) { --minCapacity; }
            if(minCapacity <= mergedKeyCapacity) {
                dest = mergedKey.getAlias();
            } else {
                if(minCapacity <= 200) {
                    mergedKeyCapacity = 200;
                } else if(minCapacity <= 2 * mergedKeyCapacity) {
                    mergedKeyCapacity *= 2;
                } else {
                    mergedKeyCapacity = minCapacity;
                }
                dest = mergedKey.allocateInsteadAndReset(mergedKeyCapacity);
            }
            assert(dest != null || mergedKeyCapacity == 0);
            if(key1Length == 0) {
                // key2 is the sort key for the first segment.
                uprv_memcpy(dest, key2Bytes, key2Length);
                mergedKeyLength = key2Length;
            } else {
                mergedKeyLength =
                    ucol_mergeSortkeys(key1Bytes, key1Length, key2Bytes, key2Length,
                                      dest, mergedKeyCapacity);
            }
            if(i == sLength) { break; }
            segmentStart = ++i;
        }
        if(segmentStart != 0 &&
                (mergedKeyLength != keyLength ||
                uprv_memcmp(mergedKey.getAlias(), keyBytes, keyLength) != 0)) {
            infoln(fileTestName);
            errln("Collator(%s).getCollationKey(with U+FFFE) != "
                  "ucol_mergeSortkeys(segments)",
                  norm);
            infoln(line);
            infoln(printCollationKey(key));
            infoln(printSortKey(mergedKey.getAlias(), mergedKeyLength));
            return false;
        }

        // Check that internalNextSortKeyPart() makes the same key, with several part sizes.
        private static final int partSizes[] = { 32, 3, 1 };
        for(int psi = 0; psi < LENGTHOF(partSizes); ++psi) {
            int partSize = partSizes[psi];
            CharString parts;
            if(!getSortKeyParts(s, length, parts, 32)) {
                infoln(fileTestName);
                errln("Collator(%s).internalNextSortKeyPart(%d) failed: %s",
                      norm, (int)partSize.errorName());
                infoln(line);
                return false;
            }
            if(keyLength != parts.length() || uprv_memcmp(keyBytes, parts.data(), keyLength) != 0) {
                infoln(fileTestName);
                errln("Collator(%s).getCollationKey() != internalNextSortKeyPart(%d)",
                      norm, (int)partSize);
                infoln(line);
                infoln(printCollationKey(key));
                infoln(printSortKey(reinterpret_cast<uint8_t *>(parts.data()), parts.length()));
                return false;
            }
        }
        return true;
    }

    namespace {

    /**
    * Replaces unpaired surrogates with U+FFFD.
    * Returns s if no replacement was made, otherwise buffer.
    */
    const UnicodeString &surrogatesToFFFD(const UnicodeString &s, UnicodeString &buffer) {
        int i = 0;
        while(i < s.length()) {
            int c = s.char32At(i);
            if(U_IS_SURROGATE(c)) {
                if(buffer.length() < i) {
                    buffer.append(s, buffer.length(), i - buffer.length());
                }
                buffer.append((UChar)0xfffd);
            }
            i += Character.charCount(c);
        }
        if(buffer.isEmpty()) {
            return s;
        }
        if(buffer.length() < i) {
            buffer.append(s, buffer.length(), i - buffer.length());
        }
        return buffer;
    }

    }

    boolean CollationTest.checkCompareTwo(const char *norm, const UnicodeString &prevFileLine,
                                        const UnicodeString &prevString, const UnicodeString &s,
                                        UCollationResult expectedOrder, Collation.Level expectedLevel,
                                        IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return false; }

        // Get the sort keys first, for error debug output.
        CollationKey prevKey;
        if(!getCollationKey(norm, prevFileLine, prevString.getBuffer(), prevString.length(),
                            prevKey)) {
            return false;
        }
        CollationKey key;
        if(!getCollationKey(norm, fileLine, s.getBuffer(), s.length(), key)) { return false; }

        UCollationResult order = coll.compare(prevString, s);
        if(order != expectedOrder || errorCode.isFailure()) {
            infoln(fileTestName);
            errln("line %d Collator(%s).compare(previous, current) wrong order: %d != %d (%s)",
                  (int)fileLineNumber, norm, order, expectedOrder.errorName());
            infoln(prevFileLine);
            infoln(fileLine);
            infoln(printCollationKey(prevKey));
            infoln(printCollationKey(key));
            return false;
        }
        order = coll.compare(s, prevString);
        if(order != -expectedOrder || errorCode.isFailure()) {
            infoln(fileTestName);
            errln("line %d Collator(%s).compare(current, previous) wrong order: %d != %d (%s)",
                  (int)fileLineNumber, norm, order, -expectedOrder.errorName());
            infoln(prevFileLine);
            infoln(fileLine);
            infoln(printCollationKey(prevKey));
            infoln(printCollationKey(key));
            return false;
        }
        // Test NUL-termination if the strings do not contain NUL characters.
        boolean containNUL = prevString.indexOf((UChar)0) >= 0 || s.indexOf((UChar)0) >= 0;
        if(!containNUL) {
            order = coll.compare(prevString.getBuffer(), -1, s.getBuffer(), -1);
            if(order != expectedOrder || errorCode.isFailure()) {
                infoln(fileTestName);
                errln("line %d Collator(%s).compare(previous-NUL, current-NUL) wrong order: %d != %d (%s)",
                      (int)fileLineNumber, norm, order, expectedOrder.errorName());
                infoln(prevFileLine);
                infoln(fileLine);
                infoln(printCollationKey(prevKey));
                infoln(printCollationKey(key));
                return false;
            }
            order = coll.compare(s.getBuffer(), -1, prevString.getBuffer(), -1);
            if(order != -expectedOrder || errorCode.isFailure()) {
                infoln(fileTestName);
                errln("line %d Collator(%s).compare(current-NUL, previous-NUL) wrong order: %d != %d (%s)",
                      (int)fileLineNumber, norm, order, -expectedOrder.errorName());
                infoln(prevFileLine);
                infoln(fileLine);
                infoln(printCollationKey(prevKey));
                infoln(printCollationKey(key));
                return false;
            }
        }

        UCharIterator leftIter;
        UCharIterator rightIter;
        uiter_setString(&leftIter, prevString.getBuffer(), prevString.length());
        uiter_setString(&rightIter, s.getBuffer(), s.length());
        order = coll.compare(leftIter, rightIter);
        if(order != expectedOrder || errorCode.isFailure()) {
            infoln(fileTestName);
            errln("line %d Collator(%s).compare(UCharIterator: previous, current) "
                  "wrong order: %d != %d (%s)",
                  (int)fileLineNumber, norm, order, expectedOrder.errorName());
            infoln(prevFileLine);
            infoln(fileLine);
            infoln(printCollationKey(prevKey));
            infoln(printCollationKey(key));
            return false;
        }

        order = prevKey.compareTo(key);
        if(order != expectedOrder || errorCode.isFailure()) {
            infoln(fileTestName);
            errln("line %d Collator(%s).getCollationKey(previous, current).compareTo() wrong order: %d != %d (%s)",
                  (int)fileLineNumber, norm, order, expectedOrder.errorName());
            infoln(prevFileLine);
            infoln(fileLine);
            infoln(printCollationKey(prevKey));
            infoln(printCollationKey(key));
            return false;
        }
        if(order != UCOL_EQUAL && expectedLevel != Collation.NO_LEVEL) {
            int prevKeyLength;
            const uint8_t *prevBytes = prevKey.getByteArray(prevKeyLength);
            int keyLength;
            const uint8_t *bytes = key.getByteArray(keyLength);
            int level = Collation.PRIMARY_LEVEL;
            for(int i = 0;; ++i) {
                uint8_t b = prevBytes[i];
                if(b != bytes[i]) { break; }
                if(b == Collation.LEVEL_SEPARATOR_BYTE) {
                    ++level;
                    if(level == Collation.CASE_LEVEL &&
                            coll.getAttribute(UCOL_CASE_LEVEL) == UCOL_OFF) {
                        ++level;
                    }
                }
            }
            if(level != expectedLevel) {
                infoln(fileTestName);
                errln("line %d Collator(%s).getCollationKey(previous, current).compareTo()=%d wrong level: %d != %d",
                      (int)fileLineNumber, norm, order, level, expectedLevel);
                infoln(prevFileLine);
                infoln(fileLine);
                infoln(printCollationKey(prevKey));
                infoln(printCollationKey(key));
                return false;
            }
        }
        return true;
    }

    void CollationTest.checkCompareStrings(UCHARBUF *f, IcuTestErrorCode &errorCode) {
        if(errorCode.isFailure()) { return; }
        UnicodeString prevFileLine = UNICODE_STRING("(none)", 6);
        UnicodeString prevString, s;
        prevString.getTerminatedBuffer();  // Ensure NUL-termination.
        while(readLine(f)) {
            if(fileLine.isEmpty()) { continue; }
            if(isSectionStarter(fileLine[0])) { break; }
            Collation.Level relation = parseRelationAndString(s);
            if(errorCode.isFailure()) {
                errorCode.reset();
                break;
            }
            UCollationResult expectedOrder = (relation == Collation.ZERO_LEVEL) ? UCOL_EQUAL : UCOL_LESS;
            Collation.Level expectedLevel = relation;
            s.getTerminatedBuffer();  // Ensure NUL-termination.
            boolean isOk = true;
            if(!needsNormalization(prevString) && !needsNormalization(s)) {
                coll.setAttribute(UCOL_NORMALIZATION_MODE, UCOL_OFF);
                isOk = checkCompareTwo("normalization=on", prevFileLine, prevString, s,
                                      expectedOrder, expectedLevel);
            }
            if(isOk) {
                coll.setAttribute(UCOL_NORMALIZATION_MODE, UCOL_ON);
                isOk = checkCompareTwo("normalization=off", prevFileLine, prevString, s,
                                      expectedOrder, expectedLevel);
            }
            if(isOk && (!nfd.isNormalized(prevString) || !nfd.isNormalized(s))) {
                UnicodeString pn = nfd.normalize(prevString);
                UnicodeString n = nfd.normalize(s);
                pn.getTerminatedBuffer();
                n.getTerminatedBuffer();
                errorCode.assertSuccess();
                isOk = checkCompareTwo("NFD input", prevFileLine, pn, n,
                                      expectedOrder, expectedLevel);
            }
            if(!isOk) {
                errorCode.reset();  // already reported
            }
            prevFileLine = fileLine;
            prevString = s;
            prevString.getTerminatedBuffer();  // Ensure NUL-termination.
        }
    }

    void CollationTest.TestDataDriven() {
        IcuTestErrorCode errorCode(*this, "TestDataDriven");

        fcd = Normalizer2Factory.getFCDInstance;
        nfd = Normalizer2Factory.getNFDInstance;
        if(errorCode.logIfFailureAndReset("Normalizer2Factory.getFCDInstance() or getNFDInstance()")) {
            return;
        }

        CharString path(getSourceTestData);
        path.appendPathPart("collationtest.txt");
        const char *codePage = "UTF-8";
        LocalUCHARBUFPointer f(ucbuf_open(path.data(), &codePage, true, false));
        if(errorCode.logIfFailureAndReset("ucbuf_open(collationtest.txt)")) {
            return;
        }
        while(errorCode.isSuccess()) {
            // Read a new line if necessary.
            // Sub-parsers leave the first line set that they do not handle.
            if(fileLine.isEmpty()) {
                if(!readLine(f.getAlias())) { break; }
                continue;
            }
            if(!isSectionStarter(fileLine[0])) {
                errln("syntax error on line %d", (int)fileLineNumber);
                infoln(fileLine);
                return;
            }
            if(fileLine.startsWith(UNICODE_STRING("** test: ", 9))) {
                fileTestName = fileLine;
                logln(fileLine);
                fileLine.remove();
            } else if(fileLine == UNICODE_STRING("@ root", 6)) {
                setRootCollator;
                fileLine.remove();
            } else if(fileLine.startsWith(UNICODE_STRING("@ locale ", 9))) {
                setLocaleCollator;
                fileLine.remove();
            } else if(fileLine == UNICODE_STRING("@ rules", 7)) {
                buildTailoring(f.getAlias());
            } else if(fileLine[0] == 0x25 && isSpace(fileLine[1])) {  // %
                parseAndSetAttribute;
            } else if(fileLine == UNICODE_STRING("* compare", 9)) {
                checkCompareStrings(f.getAlias());
            } else {
                errln("syntax error on line %d", (int)fileLineNumber);
                infoln(fileLine);
                return;
            }
        }
    }
