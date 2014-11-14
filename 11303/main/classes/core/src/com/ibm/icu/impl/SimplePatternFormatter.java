/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiled version of a pattern such as "{1} was born in {0}".
 * <p>
 * Using SimplePatternFormatter objects is both faster and safer than adhoc replacement 
 * such as <code>pattern.replace("{0}", "Colorado").replace("{1} "Fred");</code>.
 * They are faster because they are precompiled; they are safer because they
 * account for curly braces escaped by apostrophe (').
 * 
 * Placeholders are of the form \{[0-9]+\}. If a curly brace is preceded
 * by a single quote, it becomes a curly brace instead of the start of a
 * placeholder. Two single quotes resolve to one single quote. 
 * <p>
 * SimplePatternFormatter objects are immutable and can be safely cached like strings.
 * <p>
 * Example:
 * <pre>
 * SimplePatternFormatter fmt = SimplePatternFormatter.compile("{1} '{born} in {0}");
 * 
 * // Output: "paul {born} in england"
 * System.out.println(fmt.format("england", "paul"));
 * </pre>
 */
public class SimplePatternFormatter {
    private final String patternWithoutPlaceholders;
    private final int placeholderCount;
    
    // [0] first offset; [1] first placeholderId; [2] second offset;
    // [3] second placeholderId etc.
    private final int[] placeholderIdsOrderedByOffset;

    private SimplePatternFormatter(String pattern, PlaceholdersBuilder builder) {
        this.patternWithoutPlaceholders = pattern;
        this.placeholderIdsOrderedByOffset =
                builder.getPlaceholderIdsOrderedByOffset();
        this.placeholderCount = builder.getPlaceholderCount();
    }

    /**
     * Compiles a string.
     * @param pattern The string.
     * @return the new SimplePatternFormatter object.
     */
    public static SimplePatternFormatter compile(String pattern) {
        PlaceholdersBuilder placeholdersBuilder = new PlaceholdersBuilder();
        PlaceholderIdBuilder idBuilder =  new PlaceholderIdBuilder();
        StringBuilder newPattern = new StringBuilder();
        State state = State.INIT;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            switch (state) {
            case INIT:
                if (ch == 0x27) {
                    state = State.APOSTROPHE;
                } else if (ch == '{') {
                    state = State.PLACEHOLDER;
                    idBuilder.reset();
                } else {
                    newPattern.append(ch);
                }
                break;
            case APOSTROPHE:
                if (ch == 0x27) {
                    newPattern.append("'");
                } else if (ch == '{') {
                    newPattern.append("{");
                } else {
                    newPattern.append("'");
                    newPattern.append(ch);
                }
                state = State.INIT;
                break;
            case PLACEHOLDER:
                if (ch >= '0' && ch <= '9') {
                    idBuilder.add(ch);
                } else if (ch == '}' && idBuilder.isValid()) {
                    placeholdersBuilder.add(idBuilder.getId(), newPattern.length());
                    state = State.INIT;
                } else {
                    newPattern.append('{');
                    idBuilder.appendTo(newPattern);
                    newPattern.append(ch);
                    state = State.INIT;
                }
                break;
            default:
                throw new IllegalStateException();
            }
        }
        switch (state) {
        case INIT:
            break;
        case APOSTROPHE:
            newPattern.append("'");
            break;
        case PLACEHOLDER:
            newPattern.append('{');
            idBuilder.appendTo(newPattern);
            break;
        default:
            throw new IllegalStateException();
        }
        return new SimplePatternFormatter(newPattern.toString(), placeholdersBuilder);
        
    }
      
    /**
     * Returns the max placeholder ID + 1.
     */
    public int getPlaceholderCount() {
        return placeholderCount;
    }
    
    /**
     * Formats the given values.
     */
    public String format(CharSequence... values) {
        return formatAndAppend(new StringBuilder(), null, values).toString();
    }

    /**
     * Formats the given values.
     * 
     * @param appendTo the result appended here. 
     * @param offsets position of first value in appendTo stored in offsets[0];
     *   second in offsets[1]; third in offsets[2] etc. An offset of -1 means that the
     *   corresponding value is not in appendTo. offsets.length and values.length may
     *   differ. If offsets.length < values.length then only the first offsets are written out;
     *   If offsets.length > values.length then the extra offsets get -1.
     *   If caller is not interested in offsets, caller may pass null here.
     * @param values the placeholder values. A placeholder value may be appendTo itself in which case
     *   the previous value of appendTo is used.
     * @return appendTo
     */
    public StringBuilder formatAndAppend(
            StringBuilder appendTo, int[] offsets, CharSequence... values) {
        if (values.length < placeholderCount) {
            throw new IllegalArgumentException("Too few values.");
        }
        CharSequence[] fixedValues = fixValues(appendTo, -1, values);
        formatReturningOffsetLength(appendTo, offsets, fixedValues);
        return appendTo;
    }
    
    /**
     * Formats the given values.
     * 
     * @param result The result is stored here overwriting any previously stored value. 
     * @param offsets position of first value in result stored in offsets[0];
     *   second in offsets[1]; third in offsets[2] etc. An offset of -1 means that the
     *   corresponding value is not in result. offsets.length and values.length may
     *   differ. If offsets.length < values.length then only the first offsets are written out;
     *   If offsets.length > values.length then the extra offsets get -1.
     *   If caller is not interested in offsets, caller may pass null here.
     * @param values the placeholder values. A placeholder value may be result itself in which case
     *   The previous value of result is used.
     * @return result
     */
    public StringBuilder formatAndReplace(
            StringBuilder result, int[] offsets, CharSequence... values) {
        if (values.length < placeholderCount) {
            throw new IllegalArgumentException("Too few values.");
        }
        int placeholderAtStart = getPlaceholderAtStart();
        
        // If patterns starts with a placeholder and the value for that placeholder
        // is result, then we can optimize by just appending to result.
        if (placeholderAtStart >= 0 && values[placeholderAtStart] == result) {
            
            // Append to result, but make the value of the placeholderAtStart
            // placeholder remain the same as result so that it is treated as the
            // empty string.
            CharSequence[] fixedValues = fixValues(result, placeholderAtStart, values);
            int offsetLength = formatReturningOffsetLength(result, offsets, fixedValues);
            
            // We have to make the offset for the placholderAtStart placeholder be 0.
            // Otherwise it would be the length of the previous value of result.
            if (offsetLength > placeholderAtStart) {
                offsets[placeholderAtStart] = 0;
            }
            return result;
        }
        CharSequence[] fixedValues = fixValues(result, -1, values);
        result.setLength(0);
        formatReturningOffsetLength(result, offsets, fixedValues);
        return result;
    }
    
    /**
     * Formats this object using values {0}, {1} etc. Note that this is
     * not the same as the original pattern string used to build this object.
     */
    @Override
    public String toString() {
        String[] values = new String[this.getPlaceholderCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.format("{%d}", i);
        }
        return formatAndAppend(new StringBuilder(), null, values).toString();
    }
    
    /**
     * Just like format, but uses placeholder values exactly as they are.
     * A placeholder value that is the same object as appendTo is treated
     * as the empty string. In addition, returns the length of the offsets
     * array. Returns 0 if offsets is null.
     */
    private int formatReturningOffsetLength(
            StringBuilder appendTo,
            int[] offsets,
            CharSequence... values) {
        int offsetLen = offsets == null ? 0 : offsets.length;
        for (int i = 0; i < offsetLen; i++) {
            offsets[i] = -1;
        }
        if (placeholderIdsOrderedByOffset.length == 0) {
            appendTo.append(patternWithoutPlaceholders);
            return offsetLen;
        }
        appendTo.append(
                patternWithoutPlaceholders,
                0,
                placeholderIdsOrderedByOffset[0]);
        setPlaceholderOffset(
                placeholderIdsOrderedByOffset[1],
                appendTo.length(),
                offsets,
                offsetLen);
        CharSequence placeholderValue = values[placeholderIdsOrderedByOffset[1]];
        if (placeholderValue != appendTo) {
            appendTo.append(placeholderValue);
        }
        for (int i = 2; i < placeholderIdsOrderedByOffset.length; i += 2) {
            appendTo.append(
                    patternWithoutPlaceholders,
                    placeholderIdsOrderedByOffset[i - 2],
                    placeholderIdsOrderedByOffset[i]);
            setPlaceholderOffset(
                    placeholderIdsOrderedByOffset[i + 1],
                    appendTo.length(),
                    offsets,
                    offsetLen);
            placeholderValue = values[placeholderIdsOrderedByOffset[i + 1]];
            if (placeholderValue != appendTo) {
                appendTo.append(placeholderValue);
            }
        }
        appendTo.append(
                patternWithoutPlaceholders,
                placeholderIdsOrderedByOffset[placeholderIdsOrderedByOffset.length - 2],
                patternWithoutPlaceholders.length());
        return offsetLen;
    }
    
    /**
     * Returns an array like values except that for each element in values that is
     * the same as builder, the corresponding element in returned array contains a
     * snapshot of builder as a string. Moreover if skipIndex >=0, the skipIndexth
     * element of values is not checked and is left as-is in returned array.
     * If no changes are needed, fixValues returns the values array unchanged;
     * when changes are needed, fixValues returns a new array with the changes.
     * In all cases, the values array remains unchanged.
     */
    private CharSequence[] fixValues(
            StringBuilder builder, int skipIndex, CharSequence... values) {
        boolean valuesOk = true;
        for (int i = 0; i < placeholderCount; i++) {
            if (i != skipIndex && values[i] == builder) {
                valuesOk = false;
                break;
            }
        }
        if (valuesOk) {
            return values;
        }
        CharSequence[] result = new CharSequence[placeholderCount];
        String builderCopy = null;
        for (int i = 0; i < placeholderCount; i++) {
            if (i != skipIndex && values[i] == builder) {
                if (builderCopy == null) {
                    builderCopy = builder.toString();
                }
                result[i] = builderCopy;
            } else {
                result[i] = values[i];
            }
        }
        return result;
    }
    
    /**
     * Returns the placeholder at the beginning of this pattern (e.g 3 for placeholder {3}). If the
     * beginning of pattern is text instead of a placeholder, returns -1.
     */
    private int getPlaceholderAtStart() {
        if (placeholderIdsOrderedByOffset.length == 0 || placeholderIdsOrderedByOffset[0] != 0) {
            return -1;
        }
        return placeholderIdsOrderedByOffset[1];
    }
    
    private static void setPlaceholderOffset(
            int placeholderId, int offset, int[] offsets, int offsetLen) {
        if (placeholderId < offsetLen) {
            offsets[placeholderId] = offset;
        }
    }

    private static enum State {
        INIT,
        APOSTROPHE,
        PLACEHOLDER,
    }
    
    private static class PlaceholderIdBuilder {
        private int id = 0;
        private int idLen = 0;
        
        public void reset() {
            id = 0;
            idLen = 0;
        }

        public int getId() {
           return id;
        }

        public void appendTo(StringBuilder appendTo) {
            if (idLen > 0) {
                appendTo.append(id);
            }
        }

        public boolean isValid() {
           return idLen > 0;
        }

        public void add(char ch) {
            id = id * 10 + ch - '0';
            idLen++;
        }     
    }
    
    private static class PlaceholdersBuilder {
        private List<Integer> placeholderIdsOrderedByOffset = new ArrayList<Integer>();
        private int placeholderCount = 0;
        
        public void add(int placeholderId, int offset) {
            placeholderIdsOrderedByOffset.add(offset);
            placeholderIdsOrderedByOffset.add(placeholderId);
            if (placeholderId >= placeholderCount) {
                placeholderCount = placeholderId + 1;
            }
        }
        
        public int getPlaceholderCount() {
            return placeholderCount;
        }
        
        public int[] getPlaceholderIdsOrderedByOffset() {
            int[] result = new int[placeholderIdsOrderedByOffset.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = placeholderIdsOrderedByOffset.get(i).intValue();
            }
            return result;
        }
    }

    /**
     * Returns this pattern with none of the placeholders.
     */
    public String getPatternWithNoPlaceholders() {
        return patternWithoutPlaceholders;
    }

   
}
