/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt;

import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.text.BidiStructuredProcessor;

/**
 * Provides services related to the bidi classification of characters.
 */
public class CharTypes {

    // In the following lines, B, L, R and AL represent bidi categories as
    // defined in the Unicode Bidirectional Algorithm
    // ( http://www.unicode.org/reports/tr9/ ).
    // B represents the category Block Separator.
    // L represents the category Left to Right character.
    // R represents the category Right to Left character.
    // AL represents the category Arabic Letter.
    // AN represents the category Arabic Number.
    // EN represents the category European Number.
    public static final byte B = Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR;
    public static final byte L = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
    public static final byte R = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
    public static final byte AL = Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    public static final byte AN = Character.DIRECTIONALITY_ARABIC_NUMBER;
    public static final byte EN = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
    public static final byte WS = Character.DIRECTIONALITY_WHITESPACE;

    private static final int CHARTYPES_ADD = 2;

    /**
     * The Expert instance which created this instance.
     */
    final protected Expert expert;
    /**
     * The TypeHandler instance utilized by the expert.
     */
    final protected TypeHandler handler;
    /**
     * The environment associated with the expert.
     */
    final protected Environment environment;
    /**
     * The source text whose characters are analyzed.
     */
    final protected String text;

    // 1 byte for each char in text
    private byte[] types;

    // structured text direction. null means not yet computed
    private BidiStructuredProcessor.Orientation direction = null;

    /**
     * Constructor
     * 
     * @param expert
     *            Expert instance through which this handler is invoked. The
     *            handler can use Expert methods to query items stored in the
     *            expert instance, like the current {@link Environment
     *            environment}.
     * 
     * @param text
     *            is the text whose characters are analyzed.
     */
    public CharTypes(Expert expert, String text) {
        this.expert = expert;
        this.handler = expert.getTypeHandler();
        this.environment = expert.getEnvironment();
        this.text = text;
        types = new byte[text.length()];
    }

    /**
     * Indicates the base text direction appropriate for an instance of
     * structured text.
     * 
     * @return the base direction of the structured text. This direction may not
     *         be the same depending on the environment and on whether the
     *         structured text contains Arabic or Hebrew letters.<br>
     *         The value returned is either {@link Expert#DIR_LTR DIR_LTR} or
     *         {@link Expert#DIR_RTL DIR_RTL}.
     */
    public BidiStructuredProcessor.Orientation getDirection() {
        if (direction == null)
            direction = handler.getDirection(expert, text, this);
        return direction;
    }

    private byte getCachedTypeAt(int index) {
        return (byte) (types[index] - CHARTYPES_ADD);
    }

    private boolean hasCachedTypeAt(int i) {
        return (types[i] != 0); // "0" means "unknown"
    }

    /**
     * Gets the directionality of the character in the original string at the
     * specified index.
     * 
     * @param index
     *            position of the character in the <i>lean</i> text
     * 
     * @return the bidi type of the character. It is one of the values which can
     *         be returned by {@link Character#getDirectionality(char)}.
     */
    public byte getBidiTypeAt(int index) {
        if (hasCachedTypeAt(index))
            return getCachedTypeAt(index);
        byte charType = Character.getDirectionality(text.charAt(index));
        if (charType == B) {
            if (direction == null) { // called by handler.getDirection
                    return charType; // avoid infinite recursion
            }
            // direction = handler.getDirection(expert, text, this);
            charType = direction.isLtr() ? L : R;
        }
        setBidiTypeAt(index, charType);
        return charType;
    }

    /**
     * Forces a bidi type on a character.
     * 
     * @param index
     *            position of the character whose bidi type is set.
     * 
     * @param charType
     *            bidirectional type of the character. It must be one of the
     *            values which can be returned by
     *            <code>java.lang.Character.getDirectionality</code>.
     */
    public void setBidiTypeAt(int index, byte charType) {
        types[index] = (byte) (charType + CHARTYPES_ADD);
    }

    /**
     * Gets the orientation of the component in which the text will be
     * displayed.
     * 
     * @return the orientation as either {@link Environment#ORIENT_LTR},
     *         {@link Environment#ORIENT_RTL},
     *         {@link Environment#ORIENT_UNKNOWN} or
     *         {@link Environment#ORIENT_IGNORE}.
     */
    public BidiStructuredProcessor.Orientation resolveOrientation() {
        BidiStructuredProcessor.Orientation orient = environment.getOrientation();
        if (!orient.isContextual()) { // absolute orientation
            return orient;
        }
        // contextual orientation:
        int len = text.length();
        byte charType;
        for (int i = 0; i < len; i++) {
            if (!hasCachedTypeAt(i)) {
                charType = Character.getDirectionality(text.charAt(i));
                if (charType == B) // B char resolves to L or R depending on
                                    // orientation
                    continue;
                setBidiTypeAt(i, charType);
            } else
                charType = getCachedTypeAt(i);
            if (isLeftToRight(charType))
                return BidiStructuredProcessor.Orientation.LTR;
            if (isRightToLeft(charType))
                return BidiStructuredProcessor.Orientation.RTL;
        }
        return orient;
    }

    public byte charTypeForDirection(BidiStructuredProcessor.Orientation direction) {
        return (direction != null && direction.isRtl()) ? R : L;
    }

    /**
     * @param charType
     * @return
     */
    public static boolean isLeftToRight(byte charType) {
        return L == charType;

    }

    /**
     * @param charType
     * @return
     */
    public static boolean isRightToLeft(byte charType) {
        return R == charType || AL == charType;
    }

    /**
     * @param charType
     * @return
     */
    public static boolean isRightToLeftOrDigit(byte charType) {
        return R == charType || AL == charType || EN == charType
                || AN == charType;
    }

    /**
     * @param charType
     * @return
     */
    public static boolean isStrongOrDigit(byte charType) {
        return L == charType || R == charType || AL == charType
                || EN == charType || AN == charType;
    }

}
