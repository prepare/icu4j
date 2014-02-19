/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt;

import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.text.BidiStructuredProcessor;
import com.ibm.icu.text.BidiStructuredProcessor.Orientation;
import com.ibm.icu.text.BidiTransformState;

/**
 * Implements Expert; the members of this class should not be accessed directly
 * by users but only through the Expert interface.
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 * 
 */
public class ExpertImpl implements Expert {

    static final String EMPTY_STRING = "";
    static final int PREFIX_LENGTH = 2;
    static final int SUFFIX_LENGTH = 2;
    static final int FIXES_LENGTH = PREFIX_LENGTH + SUFFIX_LENGTH;
    static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * The structured text handler utilized by this expert.
     */
    protected final TypeHandler handler;
    /**
     * The environment associated with the expert.
     */
    protected final Environment environment;

    /**
     * Constructor used in {@link ExpertFactory}; this constructor should not be
     * invoked directly by users, only through methods in ExpertFactory.
     * 
     * @param structuredTextHandler
     *            the structured text handler used by this expert.
     * @param environment
     *            the environment associated with this expert.
     */
    public ExpertImpl(TypeHandler structuredTextHandler,
            Environment environment) {
        this.handler = structuredTextHandler;
        this.environment = environment;
    }

    /**
     * This method should be invoked only through {@link Expert#getTypeHandler}.
     */
    public TypeHandler getTypeHandler() {
        return handler;
    }

    /**
     * This method should be invoked only through {@link Expert#getEnvironment}.
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * This method should be invoked only through
     * {@link Expert#getTextDirection}.
     */
    public BidiStructuredProcessor.Orientation getTextDirection(String text) {
        return handler.getDirection(this, text);
    }

    long computeNextLocation(String text, CharTypes charTypes, Offsets offsets,
            int[] locations, int curPos) {
        String separators = handler.getSeparators(this);
        int separCount = separators.length();
        int specialsCount = handler.getSpecialsCount();
        int len = text.length();
        int nextLocation = len;
        int idxLocation = 0;
        // Start with special sequences to give them precedence over simple
        // separators. This may apply to cases like slash+asterisk versus slash.
        for (int i = 0; i < specialsCount; i++) {
            int location = locations[separCount + i];
            if (location < curPos) {
                location = handler.indexOfSpecial(this, text, charTypes,
                        offsets, i + 1, curPos);
                if (location < 0)
                    location = len;
                locations[separCount + i] = location;
            }
            if (location < nextLocation) {
                nextLocation = location;
                idxLocation = separCount + i;
            }
        }
        for (int i = 0; i < separCount; i++) {
            int location = locations[i];
            if (location < curPos) {
                location = text.indexOf(separators.charAt(i), curPos);
                if (location < 0)
                    location = len;
                locations[i] = location;
            }
            if (location < nextLocation) {
                nextLocation = location;
                idxLocation = i;
            }
        }
        return nextLocation + (((long) idxLocation) << 32);
    }

    /**
     * @see TypeHandler#processSeparator TypeHandler.processSeparator
     */
    static public void processSeparator(String text, CharTypes charTypes,
            Offsets offsets, int separLocation) {
        int len = text.length();
        BidiStructuredProcessor.Orientation direction = charTypes.getDirection();
        if (!direction.isLtr()) {
            // the structured text base direction is RTL
            for (int i = separLocation - 1; i >= 0; i--) {
                byte charType = charTypes.getBidiTypeAt(i);
                if (CharTypes.isRightToLeft(charType))
                    return;
                if (CharTypes.isLeftToRight(charType)) {
                    for (int j = separLocation; j < len; j++) {
                        charType = charTypes.getBidiTypeAt(j);
                        if (CharTypes.isRightToLeft(charType))
                            return;
                        if (CharTypes.isLeftToRight(charType)
                                || charType == CharTypes.EN) {
                            offsets.insertOffset(charTypes, separLocation);
                            return;
                        }
                    }
                    return;
                }
            }
            return;
        }

        // the structured text base direction is LTR
        boolean doneAN = false;
        for (int i = separLocation - 1; i >= 0; i--) {
            byte charType = charTypes.getBidiTypeAt(i);
            if (CharTypes.isLeftToRight(charType))
                return;
            if (CharTypes.isRightToLeft(charType)) {
                for (int j = separLocation; j < len; j++) {
                    charType = charTypes.getBidiTypeAt(j);
                    if (CharTypes.isLeftToRight(charType))
                        return;
                    if (CharTypes.isRightToLeftOrDigit(charType)) {
                        offsets.insertOffset(charTypes, separLocation);
                        return;
                    }
                }
                return;
            }
            if (charType == CharTypes.AN && !doneAN) {
                for (int j = separLocation; j < len; j++) {
                    charType = charTypes.getBidiTypeAt(j);
                    if (charType == CharTypes.L)
                        return;
                    if (charType == CharTypes.AL || charType == CharTypes.AN || charType == CharTypes.R) {
                        offsets.insertOffset(charTypes, separLocation);
                        return;
                    }
                }
                doneAN = true;
            }
        }
    }

    /**
     * This method should be invoked only through {@link Expert#leanToFullText};
     * however, see here for some implementations details.
     * <p>
     * When the orientation is <code>ORIENT_LTR</code> and the structured text
     * has a RTL base direction, {@link Expert#leanToFullText leanToFullText}
     * adds RLE+RLM at the head of the <i>full</i> text and RLM+PDF at its end.
     * <p>
     * When the orientation is <code>ORIENT_RTL</code> and the structured text
     * has a LTR base direction, {@link Expert#leanToFullText leanToFullText}
     * adds LRE+LRM at the head of the <i>full</i> text and LRM+PDF at its end.
     * <p>
     * When the orientation is <code>ORIENT_CONTEXTUAL_LTR</code> or
     * <code>ORIENT_CONTEXTUAL_RTL</code> and the data content would resolve to
     * a RTL orientation while the structured text has a LTR base direction,
     * {@link Expert#leanToFullText leanToFullText} adds LRM at the head of the
     * <i>full</i> text.
     * <p>
     * When the orientation is <code>ORIENT_CONTEXTUAL_LTR</code> or
     * <code>ORIENT_CONTEXTUAL_RTL</code> and the data content would resolve to
     * a LTR orientation while the structured text has a RTL base direction,
     * {@link Expert#leanToFullText leanToFullText} adds RLM at the head of the
     * <i>full</i> text.
     * <p>
     * When the orientation is <code>ORIENT_UNKNOWN</code> and the structured
     * text has a LTR base direction, {@link Expert#leanToFullText
     * leanToFullText} adds LRE+LRM at the head of the <i>full</i> text and
     * LRM+PDF at its end.
     * <p>
     * When the orientation is <code>ORIENT_UNKNOWN</code> and the structured
     * text has a RTL base direction, {@link Expert#leanToFullText
     * leanToFullText} adds RLE+RLM at the head of the <i>full</i> text and
     * RLM+PDF at its end.
     * <p>
     * When the orientation is <code>ORIENT_IGNORE</code>,
     * {@link Expert#leanToFullText leanToFullText} does not add any directional
     * formatting characters as either prefix or suffix of the <i>full</i> text.
     * <p>
     */
    public String leanToFullText(String text) {
        int len = text.length();
        if (len == 0)
            return text;
        CharTypes charTypes = new CharTypes(this, text);
        Offsets offsets = leanToFullCommon(text, charTypes, null);
        int prefixLength = offsets.getPrefixLength();
        BidiStructuredProcessor.Orientation direction = charTypes.getDirection();
        return insertMarks(text, offsets.getOffsets(), direction, prefixLength);
    }

    /**
     * This method should be invoked only through {@link Expert#leanToFullMap}.
     */
    public int[] leanToFullMap(String text) {
        int len = text.length();
        if (len == 0)
            return EMPTY_INT_ARRAY;
        CharTypes charTypes = new CharTypes(this, text);
        Offsets offsets = leanToFullCommon(text, charTypes, null);
        int prefixLength = offsets.getPrefixLength();
        int[] map = new int[len];
        int count = offsets.getCount(); // number of used entries
        int added = prefixLength;
        for (int pos = 0, i = 0; pos < len; pos++) {
            if (i < count && pos == offsets.getOffset(i)) {
                added++;
                i++;
            }
            map[pos] = pos + added;
        }
        return map;
    }

    /**
     * This method should be invoked only through
     * {@link Expert#leanBidiCharOffsets}.
     */
    public int[] leanBidiCharOffsets(String text) {
        int len = text.length();
        if (len == 0)
            return EMPTY_INT_ARRAY;
        CharTypes charTypes = new CharTypes(this, text);
        Offsets offsets = leanToFullCommon(text, charTypes, null);
        return offsets.getOffsets();
    }

    private Offsets leanToFullCommon(String text, CharTypes charTypes, BidiTransformState state) {
        int len = text.length();
        Offsets offsets = new Offsets();
        Orientation direction = handler.getDirection(this, text, charTypes);
        if (!handler.skipProcessing(this, text, charTypes)) {
            // initialize locations
            int separCount = handler.getSeparators(this).length();
            int[] locations = new int[separCount + handler.getSpecialsCount()];
            for (int i = 0, k = locations.length; i < k; i++) {
                locations[i] = -1;
            }
            // current position
            int curPos = 0;
            if (state != null) {
                curPos = handler.processSpecial(this, text, charTypes, offsets,
                        0, -1);
            }
            while (true) {
                // location of next token to handle
                int nextLocation;
                // index of next token to handle (if < separCount, this is a
                // separator; otherwise a special case
                int idxLocation;
                long res = computeNextLocation(text, charTypes, offsets,
                        locations, curPos);
                nextLocation = (int) (res & 0x00000000FFFFFFFF); /* low word */
                if (nextLocation >= len)
                    break;
                idxLocation = (int) (res >> 32); /* high word */
                if (idxLocation < separCount) {
                    processSeparator(text, charTypes, offsets, nextLocation);
                    curPos = nextLocation + 1;
                } else {
                    idxLocation -= (separCount - 1); // because caseNumber
                                                        // starts from 1
                    curPos = handler.processSpecial(this, text, charTypes,
                            offsets, idxLocation, nextLocation);
                }
                if (curPos >= len)
                    break;
            } // end while
        } // end if (!handler.skipProcessing())
        int prefixLength;
        BidiStructuredProcessor.Orientation orientation = environment.getOrientation();
        if (orientation == BidiStructuredProcessor.Orientation.IGNORE)
            prefixLength = 0;
        else {
            Orientation resolvedOrientation = charTypes.resolveOrientation();
            if (orientation != BidiStructuredProcessor.Orientation.UNKNOWN
                    && resolvedOrientation == direction)
                prefixLength = 0;
            else if (orientation.isContextual())
                prefixLength = 1;
            else
                prefixLength = 2;
        }
        offsets.setPrefixLength(prefixLength);
        return offsets;
    }

    /**
     * This method should be invoked only through {@link Expert#fullToLeanText}.
     */
    public String fullToLeanText(String full) {
        if (full.length() == 0)
            return full;
        Orientation dir = handler.getDirection(this, full);
        char curMark = getMark(dir);
        char curEmbed = getEmbed(dir);
        int i; // used as loop index
        // remove any prefix and leading mark
        int lenFull = full.length();
        for (i = 0; i < lenFull; i++) {
            char c = full.charAt(i);
            if (c != curEmbed && c != curMark)
                break;
        }
        if (i > 0) { // found at least one prefix or leading mark
            full = full.substring(i);
            lenFull = full.length();
        }
        // remove any suffix and trailing mark
        for (i = lenFull - 1; i >= 0; i--) {
            char c = full.charAt(i);
            if (c != PDF && c != curMark)
                break;
        }
        if (i < 0) // only suffix and trailing marks, no real data
            return EMPTY_STRING;
        if (i < (lenFull - 1)) { // found at least one suffix or trailing mark
            full = full.substring(0, i + 1);
            lenFull = full.length();
        }
        char[] chars = full.toCharArray();
        // remove marks from chars
        int cnt = 0;
        for (i = 0; i < lenFull; i++) {
            char c = chars[i];
            if (c == curMark)
                cnt++;
            else if (cnt > 0)
                chars[i - cnt] = c;
        }
        String lean = new String(chars, 0, lenFull - cnt);
        String full2 = leanToFullText(lean);
        // strip prefix and suffix
        int beginIndex = 0, endIndex = full2.length();
        if (full2.charAt(0) == curMark)
            beginIndex = 1;
        else {
            if (full2.charAt(0) == curEmbed) {
                beginIndex = 1;
                if (full2.charAt(0) == curMark)
                    beginIndex = 2;
            }
            if (full2.charAt(endIndex - 1) == PDF) {
                endIndex--;
                if (full2.charAt(endIndex - 1) == curMark)
                    endIndex--;
            }
        }
        if (beginIndex > 0 || endIndex < full2.length())
            full2 = full2.substring(beginIndex, endIndex);
        if (full2.equals(full))
            return lean;

        // There are some marks in full which are not in full2 and/or vice
        // versa.
        // We need to add to lean any mark appearing in full and not in full2.
        // The completed lean can never be longer than full itself.
        char[] newChars = new char[lenFull];
        char cFull, cFull2;
        int idxFull, idxFull2, idxLean, newCharsPos;
        int lenFull2 = full2.length();
        idxFull = idxFull2 = idxLean = newCharsPos = 0;
        while (idxFull < lenFull && idxFull2 < lenFull2) {
            cFull2 = full2.charAt(idxFull2);
            cFull = full.charAt(idxFull);
            if (cFull2 == cFull) { /* chars are equal, proceed */
                if (cFull2 != curMark)
                    newChars[newCharsPos++] = chars[idxLean++];
                idxFull++;
                idxFull2++;
                continue;
            }
            if (cFull2 == curMark) { /* extra Mark in full2 text */
                idxFull2++;
                continue;
            }
            if (cFull == curMark) { /* extra Mark in source full text */
                idxFull++;
                // idxFull-2 always >= 0 since leading Marks were removed from
                // full
                if (full.charAt(idxFull - 2) == curMark)
                    continue; // ignore successive Marks in full after the first
                                // one
                newChars[newCharsPos++] = curMark;
                continue;
            }
            // we should never get here (extra char which is not a Mark)
            throw new IllegalStateException(
                    "Internal error: extra character not a Mark.");
        }
        if (idxFull < lenFull) /*
                                 * full2 ended before full - this should never
                                 * happen since we removed all marks and PDFs at
                                 * the end of full
                                 */
            throw new IllegalStateException("Internal error: unexpected EOL.");

        lean = new String(newChars, 0, newCharsPos);
        return lean;
    }

    /**
     * This method should be invoked only through {@link Expert#fullToLeanMap}.
     */
    public int[] fullToLeanMap(String full) {
        int lenFull = full.length();
        if (lenFull == 0)
            return EMPTY_INT_ARRAY;
        String lean = fullToLeanText(full);
        int lenLean = lean.length();
        Orientation dir = handler.getDirection(this, lean);
        char curMark = getMark(dir);
        char curEmbed = getEmbed(dir);
        int[] map = new int[lenFull];
        int idxFull, idxLean;
        // skip any prefix and leading mark
        for (idxFull = 0; idxFull < lenFull; idxFull++) {
            char c = full.charAt(idxFull);
            if (c != curEmbed && c != curMark)
                break;
            map[idxFull] = -1;
        }
        // lean must be a subset of Full, so we only check on iLean < leanLen
        for (idxLean = 0; idxLean < lenLean; idxFull++) {
            if (full.charAt(idxFull) == lean.charAt(idxLean)) {
                map[idxFull] = idxLean;
                idxLean++;
            } else
                map[idxFull] = -1;
        }
        for (; idxFull < lenFull; idxFull++)
            map[idxFull] = -1;
        return map;
    }

    /**
     * This method should be invoked only through
     * {@link Expert#fullBidiCharOffsets}.
     */
    public int[] fullBidiCharOffsets(String full) {
        int lenFull = full.length();
        if (lenFull == 0)
            return EMPTY_INT_ARRAY;
        String lean = fullToLeanText(full);
        Offsets offsets = new Offsets();
        int lenLean = lean.length();
        int idxLean, idxFull;
        // lean must be a subset of Full, so we only check on iLean < leanLen
        for (idxLean = idxFull = 0; idxLean < lenLean; idxFull++) {
            if (full.charAt(idxFull) == lean.charAt(idxLean))
                idxLean++;
            else
                offsets.insertOffset(null, idxFull);
        }
        for (; idxFull < lenFull; idxFull++)
            offsets.insertOffset(null, idxFull);
        return offsets.getOffsets();
    }

    /**
     * This method should be invoked only through {@link Expert#insertMarks}.
     */
    public String insertMarks(String text, int[] offsets, BidiStructuredProcessor.Orientation direction,
            int affixLength) {
        if (!direction.isLtr() && !direction.isRtl())
            throw new IllegalArgumentException("Invalid direction");
        if (affixLength < 0 || affixLength > 2)
            throw new IllegalArgumentException("Invalid affix length");
        int count = offsets == null ? 0 : offsets.length;
        if (count == 0 && affixLength == 0)
            return text;
        int textLength = text.length();
        if (textLength == 0)
            return text;
        int newLen = textLength + count;
        if (affixLength == 1)
            newLen++; /* +1 for a mark char */
        else if (affixLength == 2)
            newLen += FIXES_LENGTH;
        char[] fullChars = new char[newLen];
        int added = affixLength;
        // add marks at offsets
        char curMark = getMark(direction);
        for (int i = 0, j = 0; i < textLength; i++) {
            char c = text.charAt(i);
            if (j < count && i == offsets[j]) {
                fullChars[i + added] = curMark;
                added++;
                j++;
            }
            fullChars[i + added] = c;
        }
        if (affixLength > 0) { /* add prefix/suffix ? */
            if (affixLength == 1) { /* contextual orientation */
                fullChars[0] = curMark;
            } else {
                // When the orientation is RTL, we need to add EMBED at the
                // start of the text and PDF at its end.
                // However, because of a bug in Windows' handling of
                // LRE/RLE/PDF,
                // we add LRM or RLM (according to the direction) after the
                // LRE/RLE and again before the PDF.
                char curEmbed = getEmbed(direction);
                fullChars[0] = curEmbed;
                fullChars[1] = curMark;
                fullChars[newLen - 1] = PDF;
                fullChars[newLen - 2] = curMark;
            }
        }
        return new String(fullChars);
    }

    protected char getMark(BidiStructuredProcessor.Orientation orientation)
    {
        return orientation.isLtr() ? LRM : RLM;
    }
    
    protected char getEmbed(BidiStructuredProcessor.Orientation orientation)
    {
        return orientation.isLtr() ? LRE : RLE;
    }
    
    
}
