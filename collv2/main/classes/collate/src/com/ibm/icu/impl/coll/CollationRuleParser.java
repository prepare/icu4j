/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationRuleParser.java, ported from collationruleparser.h/.cpp
*
* @since 2013apr10
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

final class CollationRuleParser {
public:
    /** Special reset positions. */
    enum Position {
        FIRST_TERTIARY_IGNORABLE,
        LAST_TERTIARY_IGNORABLE,
        FIRST_SECONDARY_IGNORABLE,
        LAST_SECONDARY_IGNORABLE,
        FIRST_PRIMARY_IGNORABLE,
        LAST_PRIMARY_IGNORABLE,
        FIRST_VARIABLE,
        LAST_VARIABLE,
        FIRST_REGULAR,
        LAST_REGULAR,
        FIRST_IMPLICIT,
        LAST_IMPLICIT,
        FIRST_TRAILING,
        LAST_TRAILING
    };

    /**
     * First character of contractions that encode special reset positions.
     * U+FFFE cannot be tailored via rule syntax.
     *
     * The second contraction character is POS_BASE + Position.
     */
    private static final UChar POS_LEAD = 0xfffe;
    /**
     * Base for the second character of contractions that encode special reset positions.
     * Braille characters U+28xx are printable and normalization-inert.
     * @see POS_LEAD
     */
    private static final UChar POS_BASE = 0x2800;

    abstract class Sink {
        /**
         * Adds a reset.
         * strength=UCOL_IDENTICAL for &str.
         * strength=UCOL_PRIMARY/UCOL_SECONDARY/UCOL_TERTIARY for &[before n]str where n=1/2/3.
         */
        abstract void addReset(int strength, const UnicodeString &str,
                              const char *&errorReason);
        /**
         * Adds a relation with strength and prefix | str / extension.
         */
        abstract void addRelation(int strength, const UnicodeString &prefix,
                                 const UnicodeString &str, const UnicodeString &extension,
                                 const char *&errorReason);

        virtual void suppressContractions(const UnicodeSet &set, const char *&errorReason,
                                          );

        virtual void optimize(const UnicodeSet &set, const char *&errorReason,
                              );
    }

    interface Importer {
        String getRules(
                const char *localeID, const char *collationType,
                const char *&errorReason);
    }

    /**
     * Constructor.
     * The Sink must be set before parsing.
     * The Importer can be set, otherwise [import locale] syntax is not supported.
     */
    CollationRuleParser(CollationData base);
    ~CollationRuleParser();

    /**
     * Sets the pointer to a Sink object.
     * The pointer is aliased: Pointer copy without cloning or taking ownership.
     */
    void setSink(Sink *sinkAlias) {
        sink = sinkAlias;
    }

    /**
     * Sets the pointer to an Importer object.
     * The pointer is aliased: Pointer copy without cloning or taking ownership.
     */
    void setImporter(Importer *importerAlias) {
        importer = importerAlias;
    }

    void parse(const UnicodeString &ruleString,
               CollationTailoring &outTailoring,
               UParseError *outParseError,
               );

    const char *getErrorReason() { return errorReason; }

    /**
     * Gets a script or reorder code from its string representation.
     * @return the script/reorder code, or
     * -1==Collator.ReorderCodes.REORDER_CODE_DEFAULT, or
     * -2 if not recognized
     */
    static int getReorderCode(const char *word);

private:
    /** UCOL_PRIMARY=0 .. UCOL_IDENTICAL=15 */
    private static final int STRENGTH_MASK = 0xf;
    private static final int STARRED_FLAG = 0x10;
    private static final int OFFSET_SHIFT = 8;

    void parse(const UnicodeString &ruleString);
    void parseRuleChain();
    int parseResetAndPosition();
    int parseRelationOperator();
    void parseRelationStrings(int strength, int i);
    void parseStarredCharacters(int strength, int i);
    int parseTailoringString(int i, UnicodeString &raw);
    int parseString(int i, UnicodeString &raw);

    /**
     * Sets str to a contraction of U+FFFE and (U+2800 + Position).
     * @return rule index after the special reset position
     */
    int parseSpecialPosition(int i, UnicodeString &str);
    void parseSetting();
    void parseReordering(const UnicodeString &raw);
    static UColAttributeValue getOnOffValue(const UnicodeString &s);

    int parseUnicodeSet(int i, UnicodeSet &set);
    int readWords(int i, UnicodeString &raw);
    int skipComment(int i);

    void setParseError(const char *reason);
    void setErrorContext();

    /**
     * ASCII [:P:] and [:S:]:
     * [\u0021-\u002F \u003A-\u0040 \u005B-\u0060 \u007B-\u007E]
     */
    static boolean isSyntaxChar(int c);
    int skipWhiteSpace(int i);

    const Normalizer2 &nfd, &nfc;

    const UnicodeString *rules;
    CollationData const baseData;
    CollationTailoring *tailoring;
    CollationSettings *settings;
    UParseError *parseError;
    const char *errorReason;

    Sink *sink;
    Importer *importer;

    int ruleIndex;
}

    private static final UChar BEFORE[] = { 0x5b, 0x62, 0x65, 0x66, 0x6f, 0x72, 0x65, 0 };  // "[before"
    const int BEFORE_LENGTH = 7;

    }  // namespace

    CollationRuleParser.Sink.~Sink() {}

    void
    CollationRuleParser.Sink.suppressContractions(const UnicodeSet &, const char *&, UErrorCode &) {}

    void
    CollationRuleParser.Sink.optimize(const UnicodeSet &, const char *&, UErrorCode &) {}

    CollationRuleParser.Importer.~Importer() {}

    CollationRuleParser.CollationRuleParser(CollationData base)
            : nfd(*Normalizer2.getNFDInstance),
              nfc(*Normalizer2.getNFCInstance),
              rules(null), baseData(base), settings(null),
              parseError(null), errorReason(null),
              sink(null), importer(null),
              ruleIndex(0) {
    }

    CollationRuleParser.~CollationRuleParser() {
    }

    void
    CollationRuleParser.parse(const UnicodeString &ruleString,
                              CollationTailoring &outTailoring,
                              UParseError *outParseError,
                              ) {
        if(U_FAILURE) { return; }
        tailoring = &outTailoring;
        settings = SharedObject.copyOnWrite(outTailoring.settings);
        if(settings == null) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        parseError = outParseError;
        if(parseError != null) {
            parseError.line = 0;
            parseError.offset = -1;
            parseError.preContext[0] = 0;
            parseError.postContext[0] = 0;
        }
        errorReason = null;
        parse(ruleString);
    }

    void
    CollationRuleParser.parse(const UnicodeString &ruleString) {
        if(U_FAILURE) { return; }
        rules = &ruleString;
        ruleIndex = 0;

        while(ruleIndex < rules.length()) {
            UChar c = rules.charAt(ruleIndex);
            if(PatternProps.isWhiteSpace(c)) {
                ++ruleIndex;
                continue;
            }
            switch(c) {
            case 0x26:  // '&'
                parseRuleChain;
                break;
            case 0x5b:  // '['
                parseSetting;
                break;
            case 0x23:  // '#' starts a comment, until the end of the line
                ruleIndex = skipComment(ruleIndex + 1);
                break;
            case 0x40:  // '@' is equivalent to [backwards 2]
                settings.setFlag(CollationSettings.BACKWARD_SECONDARY,
                                  UCOL_ON, 0);
                ++ruleIndex;
                break;
            case 0x21:  // '!' used to turn on Thai/Lao character reversal
                // Accept but ignore. The root collator has contractions
                // that are equivalent to the character reversal, where appropriate.
                ++ruleIndex;
                break;
            default:
                setParseError("expected a reset or setting or comment");
                break;
            }
            if(U_FAILURE) { return; }
        }
    }

    void
    CollationRuleParser.parseRuleChain() {
        int resetStrength = parseResetAndPosition;
        boolean isFirstRelation = true;
        for(;;) {
            int result = parseRelationOperator;
            if(U_FAILURE) { return; }
            if(result < 0) {
                if(ruleIndex < rules.length() && rules.charAt(ruleIndex) == 0x23) {
                    // '#' starts a comment, until the end of the line
                    ruleIndex = skipComment(ruleIndex + 1);
                    continue;
                }
                if(isFirstRelation) {
                    setParseError("reset not followed by a relation");
                }
                return;
            }
            int strength = result & STRENGTH_MASK;
            if(resetStrength < UCOL_IDENTICAL) {
                // reset-before rule chain
                if(isFirstRelation) {
                    if(strength != resetStrength) {
                        setParseError("reset-before strength differs from its first relation");
                        return;
                    }
                } else {
                    if(strength < resetStrength) {
                        setParseError("reset-before strength followed by a stronger relation");
                        return;
                    }
                }
            }
            int i = ruleIndex + (result >> OFFSET_SHIFT);  // skip over the relation operator
            if((result & STARRED_FLAG) == 0) {
                parseRelationStrings(strength, i);
            } else {
                parseStarredCharacters(strength, i);
            }
            if(U_FAILURE) { return; }
            isFirstRelation = false;
        }
    }

    int32_t
    CollationRuleParser.parseResetAndPosition() {
        if(U_FAILURE) { return UCOL_DEFAULT; }
        int i = skipWhiteSpace(ruleIndex + 1);
        int j;
        UChar c;
        int resetStrength;
        if(rules.compare(i, BEFORE_LENGTH, BEFORE, 0, BEFORE_LENGTH) == 0 &&
                (j = i + BEFORE_LENGTH) < rules.length() &&
                PatternProps.isWhiteSpace(rules.charAt(j)) &&
                ((j = skipWhiteSpace(j + 1)) + 1) < rules.length() &&
                0x31 <= (c = rules.charAt(j)) && c <= 0x33 &&
                rules.charAt(j + 1) == 0x5d) {
            // &[before n] with n=1 or 2 or 3
            resetStrength = UCOL_PRIMARY + (c - 0x31);
            i = skipWhiteSpace(j + 2);
        } else {
            resetStrength = UCOL_IDENTICAL;
        }
        if(i >= rules.length()) {
            setParseError("reset without position");
            return UCOL_DEFAULT;
        }
        UnicodeString str;
        if(rules.charAt(i) == 0x5b) {  // '['
            i = parseSpecialPosition(i, str);
        } else {
            i = parseTailoringString(i, str);
        }
        sink.addReset(resetStrength, str, errorReason);
        if(U_FAILURE) { setErrorContext(); }
        ruleIndex = i;
        return resetStrength;
    }

    int32_t
    CollationRuleParser.parseRelationOperator() {
        if(U_FAILURE) { return UCOL_DEFAULT; }
        ruleIndex = skipWhiteSpace(ruleIndex);
        if(ruleIndex >= rules.length()) { return UCOL_DEFAULT; }
        int strength;
        int i = ruleIndex;
        UChar c = rules.charAt(i++);
        switch(c) {
        case 0x3c:  // '<'
            if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<
                ++i;
                if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<<
                    ++i;
                    if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<<<
                        ++i;
                        strength = UCOL_QUATERNARY;
                    } else {
                        strength = UCOL_TERTIARY;
                    }
                } else {
                    strength = UCOL_SECONDARY;
                }
            } else {
                strength = UCOL_PRIMARY;
            }
            if(i < rules.length() && rules.charAt(i) == 0x2a) {  // '*'
                ++i;
                strength |= STARRED_FLAG;
            }
            break;
        case 0x3b:  // ';' same as <<
            strength = UCOL_SECONDARY;
            break;
        case 0x2c:  // ',' same as <<<
            strength = UCOL_TERTIARY;
            break;
        case 0x3d:  // '='
            strength = UCOL_IDENTICAL;
            if(i < rules.length() && rules.charAt(i) == 0x2a) {  // '*'
                ++i;
                strength |= STARRED_FLAG;
            }
            break;
        default:
            return UCOL_DEFAULT;
        }
        return ((i - ruleIndex) << OFFSET_SHIFT) | strength;
    }

    void
    CollationRuleParser.parseRelationStrings(int strength, int i) {
        // Parse
        //     prefix | str / extension
        // where prefix and extension are optional.
        UnicodeString prefix, str, extension;
        i = parseTailoringString(i, str);
        if(U_FAILURE) { return; }
        UChar next = (i < rules.length()) ? rules.charAt(i) : 0;
        if(next == 0x7c) {  // '|' separates the context prefix from the string.
            prefix = str;
            i = parseTailoringString(i + 1, str);
            if(U_FAILURE) { return; }
            next = (i < rules.length()) ? rules.charAt(i) : 0;
        }
        if(next == 0x2f) {  // '/' separates the string from the extension.
            i = parseTailoringString(i + 1, extension);
        }
        if(!prefix.isEmpty()) {
            int prefix0 = prefix.char32At(0);
            int c = str.char32At(0);
            if(!nfc.hasBoundaryBefore(prefix0) || !nfc.hasBoundaryBefore(c)) {
                setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary",
                              errorCode);
                return;
            }
        }
        sink.addRelation(strength, prefix, str, extension, errorReason);
        if(U_FAILURE) { setErrorContext(); }
        ruleIndex = i;
    }

    void
    CollationRuleParser.parseStarredCharacters(int strength, int i) {
        UnicodeString empty, raw;
        i = parseString(skipWhiteSpace(i), raw);
        if(U_FAILURE) { return; }
        if(raw.isEmpty()) {
            setParseError("missing starred-relation string");
            return;
        }
        int prev = -1;
        int j = 0;
        for(;;) {
            while(j < raw.length()) {
                int c = raw.char32At(j);
    #if 0  // TODO: reenable: http://unicode.org/cldr/trac/ticket/6738
                if(!nfd.isInert(c)) {
                    setParseError("starred-relation string is not all NFD-inert");
                    return;
                }
    #endif
                sink.addRelation(strength, empty, UnicodeString(c), empty, errorReason);
                if(U_FAILURE) {
                    setErrorContext();
                    return;
                }
                j += Character.charCount(c);
                prev = c;
            }
            if(i >= rules.length() || rules.charAt(i) != 0x2d) {  // '-'
                break;
            }
            if(prev < 0) {
                setParseError("range without start in starred-relation string");
                return;
            }
            i = parseString(i + 1, raw);
            if(U_FAILURE) { return; }
            if(raw.isEmpty()) {
                setParseError("range without end in starred-relation string");
                return;
            }
            int c = raw.char32At(0);
            if(c < prev) {
                setParseError("range start greater than end in starred-relation string");
                return;
            }
            // range prev-c
            UnicodeString s;
            while(++prev <= c) {
    #if 0  // TODO: reenable: http://unicode.org/cldr/trac/ticket/6738
                if(!nfd.isInert(prev)) {
                    setParseError("starred-relation string range is not all NFD-inert");
                    return;
                }
    #endif
                if(U_IS_SURROGATE(prev)) {
                    setParseError("starred-relation string range contains a surrogate");
                    return;
                }
                if(0xfffd <= prev && prev <= 0xffff) {
                    setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                    return;
                }
                s.setTo(prev);
                sink.addRelation(strength, empty, s, empty, errorReason);
                if(U_FAILURE) {
                    setErrorContext();
                    return;
                }
            }
            prev = -1;
            j = Character.charCount(c);
        }
        ruleIndex = skipWhiteSpace(i);
    }

    int32_t
    CollationRuleParser.parseTailoringString(int i, UnicodeString &raw) {
        i = parseString(skipWhiteSpace(i), raw);
        if(raw.isEmpty()) {
            setParseError("missing relation string");
        }
        return skipWhiteSpace(i);
    }

    int32_t
    CollationRuleParser.parseString(int i, UnicodeString &raw) {
        if(U_FAILURE) { return i; }
        raw.remove();
        while(i < rules.length()) {
            int c = rules.charAt(i++);
            if(isSyntaxChar(c)) {
                if(c == 0x27) {  // apostrophe
                    if(i < rules.length() && rules.charAt(i) == 0x27) {
                        // Double apostrophe, encodes a single one.
                        raw.append((UChar)0x27);
                        ++i;
                        continue;
                    }
                    // Quote literal text until the next single apostrophe.
                    for(;;) {
                        if(i == rules.length()) {
                            setParseError("quoted literal text missing terminating apostrophe");
                            return i;
                        }
                        c = rules.charAt(i++);
                        if(c == 0x27) {
                            if(i < rules.length() && rules.charAt(i) == 0x27) {
                                // Double apostrophe inside quoted literal text,
                                // still encodes a single apostrophe.
                                ++i;
                            } else {
                                break;
                            }
                        }
                        raw.append((UChar)c);
                    }
                } else if(c == 0x5c) {  // backslash
                    if(i == rules.length()) {
                        setParseError("backslash escape at the end of the rule string");
                        return i;
                    }
                    c = rules.char32At(i);
                    raw.append(c);
                    i += Character.charCount(c);
                } else {
                    // Any other syntax character terminates a string.
                    --i;
                    break;
                }
            } else if(PatternProps.isWhiteSpace(c)) {
                // Unquoted white space terminates a string.
                --i;
                break;
            } else {
                raw.append((UChar)c);
            }
        }
        for(int j = 0; j < raw.length();) {
            int c = raw.char32At(j);
            if(U_IS_SURROGATE(c)) {
                setParseError("string contains an unpaired surrogate");
                return i;
            }
            if(0xfffd <= c && c <= 0xffff) {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i;
            }
            j += Character.charCount(c);
        }
        return i;
    }

    namespace {

    private static final char *const positions[] = {
        "first tertiary ignorable",
        "last tertiary ignorable",
        "first secondary ignorable",
        "last secondary ignorable",
        "first primary ignorable",
        "last primary ignorable",
        "first variable",
        "last variable",
        "first regular",
        "last regular",
        "first implicit",
        "last implicit",
        "first trailing",
        "last trailing"
    };

    }  // namespace

    int32_t
    CollationRuleParser.parseSpecialPosition(int i, UnicodeString &str) {
        if(U_FAILURE) { return 0; }
        UnicodeString raw;
        int j = readWords(i + 1, raw);
        if(j > i && rules.charAt(j) == 0x5d && !raw.isEmpty()) {  // words end with ]
            ++j;
            for(int pos = 0; pos < LENGTHOF(positions); ++pos) {
                if(raw == UnicodeString(positions[pos], -1, US_INV)) {
                    str.setTo((UChar)POS_LEAD).append((UChar)(POS_BASE + pos));
                    return j;
                }
            }
            if(raw == UNICODE_STRING_SIMPLE("top")) {
                str.setTo((UChar)POS_LEAD).append((UChar)(POS_BASE + LAST_REGULAR));
                return j;
            }
            if(raw == UNICODE_STRING_SIMPLE("variable top")) {
                str.setTo((UChar)POS_LEAD).append((UChar)(POS_BASE + LAST_VARIABLE));
                return j;
            }
        }
        setParseError("not a valid special reset position");
        return i;
    }

    void
    CollationRuleParser.parseSetting() {
        if(U_FAILURE) { return; }
        UnicodeString raw;
        int i = ruleIndex + 1;
        int j = readWords(i, raw);
        if(j <= i || raw.isEmpty()) {
            setParseError("expected a setting/option at '['");
        }
        if(rules.charAt(j) == 0x5d) {  // words end with ]
            ++j;
            if(raw.startsWith(UNICODE_STRING_SIMPLE("reorder")) &&
                    (raw.length() == 7 || raw.charAt(7) == 0x20)) {
                parseReordering(raw);
                ruleIndex = j;
                return;
            }
            if(raw == UNICODE_STRING_SIMPLE("backwards 2")) {
                settings.setFlag(CollationSettings.BACKWARD_SECONDARY,
                                  UCOL_ON, 0);
                ruleIndex = j;
                return;
            }
            UnicodeString v;
            int valueIndex = raw.lastIndexOf((UChar)0x20);
            if(valueIndex >= 0) {
                v.setTo(raw, valueIndex + 1);
                raw.truncate(valueIndex);
            }
            if(raw == UNICODE_STRING_SIMPLE("strength") && v.length() == 1) {
                int value = UCOL_DEFAULT;
                UChar c = v.charAt(0);
                if(0x31 <= c && c <= 0x34) {  // 1..4
                    value = UCOL_PRIMARY + (c - 0x31);
                } else if(c == 0x49) {  // 'I'
                    value = UCOL_IDENTICAL;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setStrength(value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("alternate")) {
                UColAttributeValue value = UCOL_DEFAULT;
                if(v == UNICODE_STRING_SIMPLE("non-ignorable")) {
                    value = UCOL_NON_IGNORABLE;
                } else if(v == UNICODE_STRING_SIMPLE("shifted")) {
                    value = UCOL_SHIFTED;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setAlternateHandling(value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("maxVariable")) {
                int value = UCOL_DEFAULT;
                if(v == UNICODE_STRING_SIMPLE("space")) {
                    value = CollationSettings.MAX_VAR_SPACE;
                } else if(v == UNICODE_STRING_SIMPLE("punct")) {
                    value = CollationSettings.MAX_VAR_PUNCT;
                } else if(v == UNICODE_STRING_SIMPLE("symbol")) {
                    value = CollationSettings.MAX_VAR_SYMBOL;
                } else if(v == UNICODE_STRING_SIMPLE("currency")) {
                    value = CollationSettings.MAX_VAR_CURRENCY;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setMaxVariable(value, 0);
                    settings.variableTop = baseData.getLastPrimaryForGroup(
                        Collator.ReorderCodes.FIRST + value);
                    assert(settings.variableTop != 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("caseFirst")) {
                UColAttributeValue value = UCOL_DEFAULT;
                if(v == UNICODE_STRING_SIMPLE("off")) {
                    value = UCOL_OFF;
                } else if(v == UNICODE_STRING_SIMPLE("lower")) {
                    value = UCOL_LOWER_FIRST;
                } else if(v == UNICODE_STRING_SIMPLE("upper")) {
                    value = UCOL_UPPER_FIRST;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setCaseFirst(value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("caseLevel")) {
                UColAttributeValue value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.CASE_LEVEL, value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("normalization")) {
                UColAttributeValue value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.CHECK_FCD, value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("numericOrdering")) {
                UColAttributeValue value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.NUMERIC, value, 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("hiraganaQ")) {
                UColAttributeValue value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
    #if 0  // TODO: remove [hiraganaQ on] from ja.txt and re-enable this check
                    if(value == UCOL_ON) {
                        setParseError("[hiraganaQ on] is not supported");
                    }
    #endif
                    ruleIndex = j;
                    return;
                }
            } else if(raw == UNICODE_STRING_SIMPLE("import")) {
                CharString lang;
                lang.appendInvariantChars(v);
                if(errorCode == U_MEMORY_ALLOCATION_ERROR) { return; }
                // BCP 47 language tag . ICU locale ID
                char localeID[ULOC_FULLNAME_CAPACITY];
                int parsedLength;
                int length = uloc_forLanguageTag(lang.data(), localeID, ULOC_FULLNAME_CAPACITY,
                                                    &parsedLength, &errorCode);
                if(U_FAILURE ||
                        parsedLength != lang.length() || length >= ULOC_FULLNAME_CAPACITY) {
                    errorCode = U_ZERO_ERROR;
                    setParseError("expected language tag in [import langTag]");
                    return;
                }
                // localeID minus all keywords
                char baseID[ULOC_FULLNAME_CAPACITY];
                length = uloc_getBaseName(localeID, baseID, ULOC_FULLNAME_CAPACITY, &errorCode);
                if(U_FAILURE || length >= ULOC_KEYWORDS_CAPACITY) {
                    errorCode = U_ZERO_ERROR;
                    setParseError("expected language tag in [import langTag]");
                    return;
                }
                // @collation=type, or length=0 if not specified
                char collationType[ULOC_KEYWORDS_CAPACITY];
                length = uloc_getKeywordValue(localeID, "collation",
                                              collationType, ULOC_KEYWORDS_CAPACITY,
                                              &errorCode);
                if(U_FAILURE || length >= ULOC_KEYWORDS_CAPACITY) {
                    errorCode = U_ZERO_ERROR;
                    setParseError("expected language tag in [import langTag]");
                    return;
                }
                if(importer == null) {
                    setParseError("[import langTag] is not supported");
                } else {
                    const UnicodeString *importedRules =
                        importer.getRules(baseID,
                                          length > 0 ? collationType : "standard",
                                          errorReason);
                    if(U_FAILURE) {
                        if(errorReason == null) {
                            errorReason = "[import langTag] failed";
                        }
                        setErrorContext();
                        return;
                    }
                    const UnicodeString *outerRules = rules;
                    int outerRuleIndex = ruleIndex;
                    parse(*importedRules);
                    if(U_FAILURE) {
                        if(parseError != null) {
                            parseError.offset = outerRuleIndex;
                        }
                    }
                    rules = outerRules;
                    ruleIndex = j;
                }
                return;
            }
        } else if(rules.charAt(j) == 0x5b) {  // words end with [
            UnicodeSet set;
            j = parseUnicodeSet(j, set);
            if(U_FAILURE) { return; }
            if(raw == UNICODE_STRING_SIMPLE("optimize")) {
                sink.optimize(set, errorReason);
                if(U_FAILURE) { setErrorContext(); }
                ruleIndex = j;
                return;
            } else if(raw == UNICODE_STRING_SIMPLE("suppressContractions")) {
                sink.suppressContractions(set, errorReason);
                if(U_FAILURE) { setErrorContext(); }
                ruleIndex = j;
                return;
            }
        }
        setParseError("not a valid setting/option");
    }

    void
    CollationRuleParser.parseReordering(const UnicodeString &raw) {
        if(U_FAILURE) { return; }
        int i = 7;  // after "reorder"
        if(i == raw.length()) {
            // empty [reorder] with no codes
            settings.resetReordering();
            return;
        }
        // Parse the codes in [reorder aa bb cc].
        UVector32 reorderCodes;
        if(U_FAILURE) { return; }
        CharString word;
        while(i < raw.length()) {
            ++i;  // skip the word-separating space
            int limit = raw.indexOf((UChar)0x20, i);
            if(limit < 0) { limit = raw.length(); }
            word.clear().appendInvariantChars(raw.tempSubStringBetween(i, limit));
            if(U_FAILURE) { return; }
            int code = getReorderCode(word.data());
            if(code < 0) {
                setParseError("unknown script or reorder code");
                return;
            }
            reorderCodes.addElement(code);
            if(U_FAILURE) { return; }
            i = limit;
        }
        int length = reorderCodes.size();
        if(length == 1 && reorderCodes.elementAti(0) == Collator.ReorderCodes.DEFAULT) {
            // The root collator does not have a reordering, by definition.
            settings.resetReordering();
            return;
        }
        uint8_t table[256];
        baseData.makeReorderTable(reorderCodes.getBuffer(), length, table);
        if(U_FAILURE) { return; }
        if(!settings.setReordering(reorderCodes.getBuffer(), length, table)) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
        }
    }

    private static final char *const gSpecialReorderCodes[] = {
        "space", "punct", "symbol", "currency", "digit"
    };

    int32_t
    CollationRuleParser.getReorderCode(const char *word) {
        for(int i = 0; i < LENGTHOF(gSpecialReorderCodes); ++i) {
            if(uprv_stricmp(word, gSpecialReorderCodes[i]) == 0) {
                return Collator.ReorderCodes.FIRST + i;
            }
        }
        int script = u_getPropertyValueEnum(UCHAR_SCRIPT, word);
        if(script >= 0) {
            return script;
        }
        if(uprv_stricmp(word, "default") == 0) {
            return Collator.ReorderCodes.DEFAULT;
        }
        return -2;
    }

    UColAttributeValue
    CollationRuleParser.getOnOffValue(const UnicodeString &s) {
        if(s == UNICODE_STRING_SIMPLE("on")) {
            return UCOL_ON;
        } else if(s == UNICODE_STRING_SIMPLE("off")) {
            return UCOL_OFF;
        } else {
            return UCOL_DEFAULT;
        }
    }

    int32_t
    CollationRuleParser.parseUnicodeSet(int i, UnicodeSet &set) {
        // Collect a UnicodeSet pattern between a balanced pair of [brackets].
        int level = 0;
        int j = i;
        for(;;) {
            if(j == rules.length()) {
                setParseError("unbalanced UnicodeSet pattern brackets");
                return j;
            }
            UChar c = rules.charAt(j++);
            if(c == 0x5b) {  // '['
                ++level;
            } else if(c == 0x5d) {  // ']'
                if(--level == 0) { break; }
            }
        }
        set.applyPattern(rules.tempSubStringBetween(i, j));
        if(U_FAILURE) {
            errorCode = U_ZERO_ERROR;
            setParseError("not a valid UnicodeSet pattern");
            return j;
        }
        j = skipWhiteSpace(j);
        if(j == rules.length() || rules.charAt(j) != 0x5d) {
            setParseError("missing option-terminating ']' after UnicodeSet pattern");
            return j;
        }
        return ++j;
    }

    int32_t
    CollationRuleParser.readWords(int i, UnicodeString &raw) {
        private static final UChar sp = 0x20;
        raw.remove();
        i = skipWhiteSpace(i);
        for(;;) {
            if(i >= rules.length()) { return 0; }
            UChar c = rules.charAt(i);
            if(isSyntaxChar(c) && c != 0x2d && c != 0x5f) {  // syntax except -_
                if(raw.isEmpty()) { return i; }
                if(raw.endsWith(&sp, 1)) {  // remove trailing space
                    raw.truncate(raw.length() - 1);
                }
                return i;
            }
            if(PatternProps.isWhiteSpace(c)) {
                raw.append(0x20);
                i = skipWhiteSpace(i + 1);
            } else {
                raw.append(c);
                ++i;
            }
        }
    }

    int32_t
    CollationRuleParser.skipComment(int i) {
        // skip to past the newline
        while(i < rules.length()) {
            UChar c = rules.charAt(i++);
            // LF or FF or CR or NEL or LS or PS
            if(c == 0xa || c == 0xc || c == 0xd || c == 0x85 || c == 0x2028 || c == 0x2029) {
                // Unicode Newline Guidelines: "A readline function should stop at NLF, LS, FF, or PS."
                // NLF (new line function) = CR or LF or CR+LF or NEL.
                // No need to collect all of CR+LF because a following LF will be ignored anyway.
                break;
            }
        }
        return i;
    }

    void
    CollationRuleParser.setParseError(const char *reason) {
        if(U_FAILURE) { return; }
        // Error code consistent with the old parser (from ca. 2001),
        // rather than U_PARSE_ERROR;
        errorCode = U_INVALID_FORMAT_ERROR;
        errorReason = reason;
        if(parseError != null) { setErrorContext(); }
    }

    void
    CollationRuleParser.setErrorContext() {
        if(parseError == null) { return; }

        // Note: This relies on the calling code maintaining the ruleIndex
        // at a position that is useful for debugging.
        // For example, at the beginning of a reset or relation etc.
        parseError.offset = ruleIndex;
        parseError.line = 0;  // We are not counting line numbers.

        // before ruleIndex
        int start = ruleIndex - (U_PARSE_CONTEXT_LEN - 1);
        if(start < 0) {
            start = 0;
        } else if(start > 0 && U16_IS_TRAIL(rules.charAt(start))) {
            ++start;
        }
        int length = ruleIndex - start;
        rules.extract(start, length, parseError.preContext);
        parseError.preContext[length] = 0;

        // starting from ruleIndex
        length = rules.length() - ruleIndex;
        if(length >= U_PARSE_CONTEXT_LEN) {
            length = U_PARSE_CONTEXT_LEN - 1;
            if(U16_IS_LEAD(rules.charAt(ruleIndex + length - 1))) {
                --length;
            }
        }
        rules.extract(ruleIndex, length, parseError.postContext);
        parseError.postContext[length] = 0;
    }

    boolean
    CollationRuleParser.isSyntaxChar(int c) {
        return 0x21 <= c && c <= 0x7e &&
                (c <= 0x2f || (0x3a <= c && c <= 0x40) ||
                (0x5b <= c && c <= 0x60) || (0x7b <= c));
    }

    int32_t
    CollationRuleParser.skipWhiteSpace(int i) {
        while(i < rules.length() && PatternProps.isWhiteSpace(rules.charAt(i))) {
            ++i;
        }
        return i;
    }
